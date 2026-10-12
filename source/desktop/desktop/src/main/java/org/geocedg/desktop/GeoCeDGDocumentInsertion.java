/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.units.UnitState;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.App;
import org.geogebra.common.main.undo.AppState;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.headless.GFileHandler;
import org.geogebra.desktop.io.DocumentArchivePreflight;
import org.geogebra.desktop.main.undo.FileAppState;
import org.geogebra.desktop.main.undo.UndoManagerD.PreparedUndoBaseline;

/**
 * PRE-G9B-R6-plus-F3 Insert File (restricted native insertion R with L1, M1, the
 * operation-scoped buffer and the recovery and isolation contract P6/P7). Admission
 * and a strict dry run precede any mutation; the commit is the kernel's own
 * construction copy; a strict verification decides success. The undo history is
 * moved aside behind a generation barrier and put back only when no store can still
 * change it (recovery design addendum successor section 2).
 *
 * <p>Outcomes: inserted (one undo step, document identity kept); rejected before
 * mutation; failed with certified recovery (K1-S, nothing reset); failed with certified
 * recovery and a declared session reset (K1-R-H guarded reload, or an inherited
 * rollback that rebuilt the drawing); serious recovery failure (isolation).
 */
final class GeoCeDGDocumentInsertion {
	/** Bounded wait for pending undo stores (EDT; never indefinite). */
	static final long STORE_WAIT_MS = 5000;
	private static final String ROLLBACK_FAILURE =
			"Clipboard identity rollback could not restore the construction";

	/** Final outcome categories (closeout record section 4.1). */
	enum Outcome {
		INSERTED,
		REJECTED_BEFORE_MUTATION,
		FAILED_RECOVERY_CERTIFIED,
		FAILED_RECOVERY_CERTIFIED_SESSION_RESET,
		SERIOUS_RECOVERY_FAILURE
	}

	/** Test seam for fault injection at the stages of the transaction; no-op in use. */
	interface Faults {
		/** inside the barrier block, before the native recovery snapshot */
		default void beforeSnapshot() throws Exception {
			// no fault
		}

		/** after the commit returned */
		default void afterCommit() throws Exception {
			// no fault
		}

		/** before the orchestration's own undo store */
		default void beforeOwnStore() throws Exception {
			// no fault
		}

		/** inside the subtractive compensation */
		default void duringRemoval() throws Exception {
			// no fault
		}

		/** before each guarded reload attempt (1-based) */
		default void beforeReload(int attempt) throws Exception {
			// no fault
		}

		/** after a recovery, before its certificate */
		default void beforeCertificate() throws Exception {
			// no fault
		}

		/** before the attach block */
		default void beforeAttach() throws Exception {
			// no fault
		}
	}

	private static final Faults NO_FAULTS = new Faults() {
	};

	/** Outcome of one Insert File operation. */
	static final class Result {
		private final Outcome outcome;
		private final String message;
		private final List<String> declaredResets;
		private final Throwable failure;

		Result(Outcome outcome, String message, List<String> declaredResets, Throwable failure) {
			this.outcome = outcome;
			this.message = message;
			this.declaredResets = List.copyOf(declaredResets);
			this.failure = failure;
		}

		Outcome getOutcome() {
			return outcome;
		}

		/** @return the user message (P6), or null for a silent success */
		String getMessage() {
			return message;
		}

		/** @return runtime state reset by the recovery (declared, never silent) */
		List<String> getDeclaredResets() {
			return declaredResets;
		}

		/** @return the original failure with cleanup failures suppressed, or null */
		Throwable getFailure() {
			return failure;
		}
	}

	private final AppGeoCeDG app;
	private Faults faults = NO_FAULTS;

	/**
	 * @param app target document window
	 */
	GeoCeDGDocumentInsertion(AppGeoCeDG app) {
		this.app = app;
	}

	void setFaults(Faults faults) {
		this.faults = faults == null ? NO_FAULTS : faults;
	}

	// ------------------------------------------------------------------ entry

	/**
	 * Inserts a resolved source file into the target document. Runs on the event
	 * dispatch thread.
	 *
	 * @param file existing source file (already resolved by the chooser rule X1)
	 * @return the outcome
	 */
	Result insert(File file) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Insert File runs on the event dispatch thread");
		}
		String name = file == null ? "" : file.getName();
		if (app.isInsertionIsolated()) {
			return rejected(name, "Insert.Reject.Isolated", "");
		}
		String extensionReason = GeoCeDGDocumentPolicy.insertionRefusal(file);
		if (extensionReason != null) {
			return rejected(name, extensionReason, "");
		}
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(file.toPath());
		} catch (IOException unreadable) {
			return rejected(name, "Insert.Reject.Invalid", "");
		}
		if (!preflight(bytes)) {
			return rejected(name, "Insert.Reject.Invalid", "");
		}
		AppGeoCeDG helper = null;
		try {
			helper = app.newInsertionHelper();
			if (!helper.loadFile(file, false)) {
				return rejected(name, "Insert.Reject.Invalid", "");
			}
			GeoCeDGInsertionAdmission admission = GeoCeDGInsertionAdmission.admit(app, helper);
			if (!admission.isAdmitted()) {
				return rejected(name, admission.getReasonKey(), admission.getReasonArgument());
			}
			String dryRun = dryRun(file, admission);
			if (dryRun != null) {
				return rejected(name, "Insert.Reject.DryRun", dryRun);
			}
			return transact(name, helper, admission);
		} catch (RuntimeException unexpected) {
			Log.debug(unexpected);
			return new Result(Outcome.REJECTED_BEFORE_MUTATION,
					text("Insert.Rejected", name, text("Insert.Reject.Invalid")), List.of(),
					unexpected);
		} finally {
			app.releaseInsertionHelper(helper);
		}
	}

	private boolean preflight(byte[] bytes) {
		try {
			return DocumentArchivePreflight.validate(bytes, app.createDocumentPreflightConfig());
		} catch (Exception invalid) {
			return false;
		}
	}

	private Result rejected(String name, String key, String argument) {
		String reason = argument == null || argument.isEmpty() ? text(key) : text(key, argument);
		return new Result(Outcome.REJECTED_BEFORE_MUTATION, text("Insert.Rejected", name, reason),
				List.of(), null);
	}

	// ------------------------------------------------------------------ dry run

	/**
	 * The same insertion on a scratch host loaded from the target's native archive,
	 * strictly verified.
	 *
	 * @return null when the dry run succeeds, otherwise the failure class
	 */
	private String dryRun(File file, GeoCeDGInsertionAdmission admission) {
		AppGeoCeDG scratch = null;
		AppGeoCeDG helper = null;
		try {
			ByteArrayOutputStream target = new ByteArrayOutputStream();
			app.getXMLio().writeGeoGebraFile(target, false);
			scratch = app.newInsertionHelper();
			if (!GFileHandler.loadXML(scratch, new ByteArrayInputStream(target.toByteArray()),
					false)) {
				return "target";
			}
			helper = app.newInsertionHelper();
			if (!helper.loadFile(file, false)) {
				return "source";
			}
			GeoCeDGInsertionAdmission scratchAdmission = GeoCeDGInsertionAdmission.admit(scratch,
					helper);
			if (!scratchAdmission.isAdmitted()) {
				return "admission";
			}
			Set<GeoElement> before = identitySet(scratch);
			Map<GeoElement, PersistentGeoId> idsBefore = identities(scratch);
			new GeoCeDGInsertionCopyPaste(scratchAdmission.getTransferredMacros()).insertFrom(
					helper, scratch, scratchAdmission.getDuplicateLabels(), false);
			List<GeoElement> created = createdSince(scratch, before);
			String mismatch = verify(scratchAdmission.getSource(), created, scratch, helper,
					idsBefore);
			return mismatch != null ? mismatch : verifyMacros(scratchAdmission, scratch, helper);
		} catch (Exception | Error failure) {
			Log.debug(failure);
			return "commit";
		} finally {
			app.releaseInsertionHelper(helper);
			app.releaseInsertionHelper(scratch);
		}
	}

	// ------------------------------------------------------------------ transaction

	private Result transact(String name, AppGeoCeDG helper,
			GeoCeDGInsertionAdmission admission) {
		if (!app.getKernel().isUndoActive() || !(app.getKernel().getConstruction()
				.getUndoManager() instanceof GeoCeDGUndoManager undo)) {
			return rejected(name, "Insert.Reject.UndoUnavailable", "");
		}
		Certificate before = Certificate.of(app);
		String uuid = app.getUniqueId();
		String tube = app.getTubeId();
		boolean saved = app.isSaved();
		String operationId = UUID.randomUUID().toString();

		// 0 BARRIER BLOCK: nothing is mutated before it ends
		Barrier barrier;
		try {
			barrier = barrier(undo, operationId);
		} catch (BarrierRejection rejection) {
			return rejected(name, rejection.getMessage(), "");
		}
		Transaction tx = new Transaction(name, undo, barrier, admission, before, uuid, tube,
				saved);
		try {
			return tx.run(helper);
		} finally {
			tx.finish();
		}
	}

	private Barrier barrier(GeoCeDGUndoManager undo, String operationId)
			throws BarrierRejection {
		long deadline = System.nanoTime() + STORE_WAIT_MS * 1_000_000L;
		while (true) {
			synchronized (undo) {
				if (undo.isDegraded()) {
					throw new BarrierRejection("Insert.Reject.HistoryBusy");
				}
				if (undo.isQuiescent()) {
					return barrierBlock(undo, operationId);
				}
			}
			if (System.nanoTime() > deadline) {
				throw new BarrierRejection("Insert.Reject.HistoryBusy");
			}
			pause();
		}
	}

	/** Runs inside the monitor of the undo manager. */
	private Barrier barrierBlock(GeoCeDGUndoManager undo, String operationId)
			throws BarrierRejection {
		GeoCeDGRecoverySnapshot snapshot;
		try {
			faults.beforeSnapshot();
			snapshot = GeoCeDGRecoverySnapshot.capture(app, operationId);
		} catch (Exception failure) {
			Log.debug(failure);
			throw new BarrierRejection("Insert.Reject.Snapshot");
		}
		HistoryRecord history = HistoryRecord.of(undo);
		GeoCeDGUndoManager.HeldHistory held = undo.detach();
		UndoCommand baseline;
		try {
			try (PreparedUndoBaseline prepared = undo.prepareUndoBaseline()) {
				undo.commitUndoBaseline(prepared);
			}
			List<UndoCommand> temporary = undo.getCommands();
			if (temporary.size() != 1 || !(temporary.get(0).getAppState()
					instanceof FileAppState)) {
				throw new IOException("unexpected barrier baseline " + temporary.size());
			}
			// captured BY REFERENCE: the reload source of K1-R-H (G4)
			baseline = temporary.get(0);
			byte[] baselineArchive = Files.readAllBytes(
					((FileAppState) baseline.getAppState()).getFile().toPath());
			if (!preflight(baselineArchive)) {
				throw new IOException("barrier baseline failed the preflight");
			}
		} catch (IOException | RuntimeException failure) {
			Log.debug(failure);
			List<UndoCommand> temporary = undo.getCommands();
			undo.attach(held.getCommands(), held.getCursor());
			for (UndoCommand command : temporary) {
				deleteQuietly(command, null);
			}
			snapshot.release();
			throw new BarrierRejection("Insert.Reject.Snapshot");
		}
		return new Barrier(held, history, baseline, snapshot);
	}

	private static void pause() {
		try {
			Thread.sleep(5);
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	/** Waits outside every monitor until no store is pending; bounded. */
	private static boolean awaitQuiescence(GeoCeDGUndoManager undo) {
		long deadline = System.nanoTime() + STORE_WAIT_MS * 1_000_000L;
		while (!undo.isQuiescent()) {
			if (undo.isDegraded() || System.nanoTime() > deadline) {
				return false;
			}
			pause();
		}
		return true;
	}

	/** One insertion attempt after the barrier. */
	private final class Transaction {
		private final String name;
		private final GeoCeDGUndoManager undo;
		private final Barrier barrier;
		private final GeoCeDGInsertionAdmission admission;
		private final Certificate before;
		private final String uuid;
		private final String tube;
		private final boolean saved;
		private final Set<String> macrosBefore = new java.util.LinkedHashSet<>();
		private final List<UndoCommand> ownStores = new ArrayList<>();
		private List<GeoElement> created = List.of();
		private boolean attached;
		/** set when the serious path handed the history and snapshot to the isolation */
		private boolean handedOver;
		private Throwable failure;

		Transaction(String name, GeoCeDGUndoManager undo, Barrier barrier,
				GeoCeDGInsertionAdmission admission, Certificate before, String uuid, String tube,
				boolean saved) {
			this.name = name;
			this.undo = undo;
			this.barrier = barrier;
			this.admission = admission;
			this.before = before;
			this.uuid = uuid;
			this.tube = tube;
			this.saved = saved;
		}

		Result run(AppGeoCeDG helper) {
			macrosBefore.addAll(macroNames(app));
			Set<GeoElement> instancesBefore = identitySet(app);
			Map<GeoElement, PersistentGeoId> idsBefore = identities(app);
			GeoCeDGInsertionCopyPaste buffer = new GeoCeDGInsertionCopyPaste(
					admission.getTransferredMacros());
			boolean rollbackFailed = false;
			// 1 COMMIT
			try {
				buffer.insertFrom(helper, app, admission.getDuplicateLabels(), false);
				faults.afterCommit();
			} catch (Exception | Error commitFailure) {
				failure = commitFailure;
				rollbackFailed = isRollbackFailure(commitFailure);
			}
			created = createdSince(app, instancesBefore);
			String postCommit = currentUndoXml();
			restoreIdentity();
			try {
				if (failure == null) {
					// 2 IDENTIFY the paste's own store (identity-bearing payloads only)
					if (!awaitQuiescence(undo)) {
						throw new IllegalStateException("an undo store did not complete");
					}
					List<UndoCommand> temporary = undo.getCommands();
					if (temporary.isEmpty() || temporary.get(0) != barrier.baseline) {
						throw new IllegalStateException("the barrier baseline was replaced");
					}
					int expected = admission.isIdentityBearing() ? 2 : 1;
					if (temporary.size() != expected) {
						throw new IllegalStateException("unexpected undo stores: "
								+ (temporary.size() - 1));
					}
					if (expected == 2) {
						UndoCommand pasteStore = temporary.get(1);
						if (!postCommit.equals(undoXml(pasteStore))) {
							throw new IllegalStateException(
									"the paste store is not this insertion");
						}
						ownStores.add(pasteStore);
					}
					// 3 STRICT VERIFY
					String mismatch = verify(admission.getSource(), created, app, helper,
							idsBefore);
					if (mismatch == null) {
						mismatch = verifyMacros(admission, app, helper);
					}
					if (mismatch != null) {
						throw new IllegalStateException("verification failed: " + mismatch);
					}
					return succeed(buffer.getProvenance());
				}
			} catch (Exception | Error verificationFailure) {
				failure = verificationFailure;
			}
			return recover(rollbackFailed);
		}

		// ---------------------------------------------------- 4 SUCCESS

		private Result succeed(UnitState provenance) throws Exception {
			faults.beforeOwnStore();
			// one undo step made by the orchestration, carrying the kept document identity
			String live = currentUndoXml();
			app.getKernel().getConstruction().getUndoManager().storeUndoInfo();
			if (!awaitQuiescence(undo)) {
				throw new IllegalStateException("the insertion's undo store did not complete");
			}
			List<UndoCommand> temporary = undo.getCommands();
			UndoCommand own = temporary.get(temporary.size() - 1);
			if (temporary.size() != ownStores.size() + 2 || !live.equals(undoXml(own))) {
				throw new IllegalStateException("the insertion's undo store was not identified");
			}
			ownStores.add(own);
			List<UndoCommand> finalHistory = new ArrayList<>(barrier.held.getPrefix());
			finalHistory.add(own);
			List<UndoCommand> discarded = new ArrayList<>(barrier.held.getRedo());
			attachBlock(temporary, finalHistory, finalHistory.size(), discarded);
			app.setUnsaved();
			app.unitPasteCompleted(provenance, app);
			barrier.snapshot.release();
			return new Result(Outcome.INSERTED, null, List.of(), null);
		}

		// ---------------------------------------------------- 5-7 RECOVERY

		private Result recover(boolean rollbackFailed) {
			String reason = text("Insert.Failure.Commit");
			if (rollbackFailed) {
				// DQ21-Q5: the inherited rollback itself failed
				return serious(null);
			}
			if (!admission.isIdentityBearing()) {
				try {
					// 5 K1-S
					compensate();
					attachBlock(expectedTemporary(), barrier.held.getCommands(),
							barrier.held.getCursor(), List.of());
					restoreSaved();
					faults.beforeCertificate();
					Certificate after = Certificate.of(app);
					String mismatch = certify(after);
					if (mismatch == null) {
						if (after.sameInstances(before)) {
							barrier.snapshot.release();
							return new Result(Outcome.FAILED_RECOVERY_CERTIFIED,
									text("Insert.Failed.Compensated", name, reason), List.of(),
									failure);
						}
						// the inherited atomic import rebuilt the drawing: declared, not hidden
						barrier.snapshot.release();
						List<String> resets = declaredResets(after);
						return new Result(Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET,
								reloadedMessage(reason, resets), resets, failure);
					}
					return serious(new IllegalStateException("K1-S certificate: " + mismatch));
				} catch (Exception | Error compensationFailure) {
					addSuppressed(compensationFailure);
					if (attached) {
						// the history is back; the reload would need a new barrier
						return serious(null);
					}
				}
			}
			// 6 RELOAD (K1-R-H)
			try {
				if (!awaitQuiescence(undo)) {
					throw new IllegalStateException("an undo store did not complete");
				}
				guardedReload();
				removeTransferredMacros();
				restoreIdentity();
				attachBlock(expectedTemporary(), barrier.held.getCommands(),
						barrier.held.getCursor(), List.of());
				restoreSaved();
				faults.beforeCertificate();
				Certificate after = Certificate.of(app);
				String mismatch = certify(after);
				if (mismatch != null) {
					return serious(new IllegalStateException("K1-R-H certificate: " + mismatch));
				}
				barrier.snapshot.release();
				List<String> resets = declaredResets(after);
				return new Result(Outcome.FAILED_RECOVERY_CERTIFIED_SESSION_RESET,
						reloadedMessage(reason, resets), resets, failure);
			} catch (Exception | Error reloadFailure) {
				addSuppressed(reloadFailure);
				return serious(null);
			}
		}

		private List<UndoCommand> expectedTemporary() {
			List<UndoCommand> expected = new ArrayList<>();
			expected.add(barrier.baseline);
			for (UndoCommand own : ownStores) {
				if (!expected.contains(own)) {
					expected.add(own);
				}
			}
			return expected;
		}

		private void compensate() throws Exception {
			faults.duringRemoval();
			List<GeoElement> reverse = new ArrayList<>(created);
			Collections.reverse(reverse);
			for (GeoElement geo : reverse) {
				if (geo.isLabelSet()) {
					geo.remove();
				}
			}
			removeTransferredMacros();
			restoreIdentity();
			if (!currentUndoXml().equals(undoXml(barrier.baseline))) {
				throw new IllegalStateException("subtractive compensation left a difference");
			}
		}

		/** Reload of the barrier baseline only: no history access, no inherited fallback. */
		private void guardedReload() throws Exception {
			File source = ((FileAppState) barrier.baseline.getAppState()).getFile();
			Exception last = null;
			for (int attempt = 1; attempt <= 2; attempt++) {
				try {
					faults.beforeReload(attempt);
					synchronized (undo) {
						app.setActiveView(App.VIEW_EUCLIDIAN);
						app.getEventDispatcher().disableListeners();
						try (InputStream in = new FileInputStream(source)) {
							((MyXMLioJre) app.getXMLio()).readZipFromMemory(in);
						} finally {
							app.getEventDispatcher().enableListeners();
						}
					}
					return;
				} catch (Exception reloadFailure) {
					last = reloadFailure;
				}
			}
			throw last;
		}

		private void removeTransferredMacros() {
			List<Macro> extra = new ArrayList<>();
			for (int i = 0; i < app.getKernel().getMacroNumber(); i++) {
				Macro macro = app.getKernel().getMacro(i);
				if (!macrosBefore.contains(macro.getCommandName())) {
					extra.add(macro);
				}
			}
			for (Macro macro : extra) {
				app.getKernel().removeMacro(macro);
			}
		}

		private void restoreIdentity() {
			app.setUniqueId(uuid);
			app.setTubeId(tube);
		}

		private void restoreSaved() {
			if (saved) {
				app.setSaved();
			} else {
				app.setUnsaved();
			}
		}

		private String certify(Certificate after) {
			List<String> differences = before.serializableDifferences(after);
			String history = barrier.history.mismatch(undo, barrier.held);
			if (history != null) {
				differences.add("history " + history);
			}
			return differences.isEmpty() ? null : String.join("; ", differences);
		}

		private List<String> declaredResets(Certificate after) {
			List<String> resets = new ArrayList<>();
			resets.add("Insert.Reset.Instances");
			resets.add("Insert.Reset.Selection");
			resets.add("Insert.Reset.ReferenceMemory");
			if (before.linkedSheet != null && after.linkedSheet == null) {
				resets.add("Insert.Reset.ExportLink");
			}
			return resets;
		}

		private String reloadedMessage(String reason, List<String> resets) {
			return text("Insert.Failed.Reloaded", name, reason,
					resets.contains("Insert.Reset.ExportLink") ? text("Insert.Failed.ExportLink")
							: "");
		}

		// ---------------------------------------------------- 7 SERIOUS

		private Result serious(Throwable certificateFailure) {
			if (certificateFailure != null) {
				addSuppressed(certificateFailure);
			}
			GeoCeDGUndoManager.HeldHistory keptAside = null;
			if (!attached) {
				try {
					attachBlock(undo.getCommands(), barrier.held.getCommands(),
							barrier.held.getCursor(), List.of());
				} catch (Exception | Error attachFailure) {
					addSuppressed(attachFailure);
					keptAside = barrier.held;
				}
			}
			barrier.snapshot.retainForIsolation();
			handedOver = true;
			app.getInsertionIsolation().enter(barrier.snapshot, name, keptAside);
			return new Result(Outcome.SERIOUS_RECOVERY_FAILURE, text("Insert.Serious", name),
					List.of(), failure);
		}

		// ---------------------------------------------------- ATTACH BLOCK

		/**
		 * Re-attaches only when nothing can still change the history: no pending store,
		 * not degraded and exactly the expected temporary list; otherwise a closing
		 * barrier (a throw-away baseline) cancels every earlier store first (G3).
		 */
		private void attachBlock(List<UndoCommand> expectedTemporary,
				List<UndoCommand> finalHistory, int cursor, List<UndoCommand> discarded)
				throws Exception {
			faults.beforeAttach();
			synchronized (undo) {
				List<UndoCommand> temporary = undo.getCommands();
				if (!undo.isQuiescent() || !temporary.equals(expectedTemporary)) {
					try (PreparedUndoBaseline closing = undo.prepareUndoBaseline()) {
						undo.commitUndoBaseline(closing);
					}
					temporary.addAll(undo.getCommands());
				}
				RuntimeException refresh = undo.attach(finalHistory, cursor);
				attached = true;
				if (refresh != null) {
					// installed; the action refresh is best effort (G7)
					Log.debug(refresh);
				}
				for (UndoCommand command : temporary) {
					if (!finalHistory.contains(command)) {
						deleteQuietly(command, this);
					}
				}
				for (UndoCommand command : discarded) {
					deleteQuietly(command, this);
				}
			}
		}

		/** FINALLY: attach if still detached; the native snapshot is never released here. */
		void finish() {
			// after a serious failure the isolation owns the history: never re-attach it here
			if (!attached && !handedOver) {
				try {
					attachBlock(undo.getCommands(), barrier.held.getCommands(),
							barrier.held.getCursor(), List.of());
				} catch (Exception | Error attachFailure) {
					addSuppressed(attachFailure);
				}
			}
		}

		void addSuppressed(Throwable cleanupFailure) {
			if (failure == null) {
				failure = cleanupFailure;
			} else if (failure != cleanupFailure) {
				failure.addSuppressed(cleanupFailure);
			}
		}

		private String currentUndoXml() {
			return app.getKernel().getConstruction().getCurrentUndoXML(true).toString();
		}
	}

	private static boolean isRollbackFailure(Throwable failure) {
		for (Throwable t = failure; t != null; t = t.getCause()) {
			if (t instanceof IllegalStateException && ROLLBACK_FAILURE.equals(t.getMessage())) {
				return true;
			}
		}
		return false;
	}

	private static void deleteQuietly(UndoCommand command, Transaction owner) {
		try {
			command.delete();
		} catch (RuntimeException cleanupFailure) {
			if (owner != null) {
				owner.addSuppressed(cleanupFailure);
			} else {
				Log.debug(cleanupFailure);
			}
		}
	}

	/**
	 * @param command undo command with a file state
	 * @return the construction XML of its archive
	 */
	static String undoXml(UndoCommand command) throws IOException {
		AppState state = command.getAppState();
		if (!(state instanceof FileAppState)) {
			throw new IOException("not a file undo state");
		}
		try (ZipInputStream zip = new ZipInputStream(
				new FileInputStream(((FileAppState) state).getFile()))) {
			ZipEntry entry = zip.getNextEntry();
			if (entry == null) {
				throw new IOException("empty undo archive");
			}
			return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	// ------------------------------------------------------------------ verification

	/**
	 * Strict verification: count, pairwise class and parent-algorithm class, layer,
	 * value, closure inside the insertion, identity records and macro bindings.
	 *
	 * @return null when verified, otherwise the first failure class
	 */
	static String verify(List<GeoElement> source, List<GeoElement> created, AppGeoCeDG target,
			AppGeoCeDG helper, Map<GeoElement, PersistentGeoId> idsBefore) {
		if (created.size() != source.size()) {
			return "count " + created.size() + "/" + source.size();
		}
		Set<GeoElement> inserted = Collections.newSetFromMap(new IdentityHashMap<>());
		inserted.addAll(created);
		SpatialIdentityRegistry sourceIds = helper.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		SpatialIdentityRegistry targetIds = target.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		Set<PersistentGeoId> newIds = new java.util.HashSet<>();
		for (int i = 0; i < source.size(); i++) {
			GeoElement from = source.get(i);
			GeoElement to = created.get(i);
			String label = from.getLabelSimple();
			if (from.getClass() != to.getClass()) {
				return "class " + label;
			}
			AlgoElement fromAlgo = from.getParentAlgorithm();
			AlgoElement toAlgo = to.getParentAlgorithm();
			if (fromAlgo == null ? toAlgo != null
					: toAlgo == null || fromAlgo.getClass() != toAlgo.getClass()) {
				return "algorithm " + label;
			}
			if (from.getLayer() != to.getLayer()) {
				return "layer " + label;
			}
			if (!sameValue(from, to)) {
				return "value " + label;
			}
			if (toAlgo != null) {
				for (GeoElement input : toAlgo.getInput()) {
					if (input.isLabelSet() && !inserted.contains(input)) {
						return "closure " + label;
					}
				}
			}
			if (fromAlgo instanceof AlgoMacro fromMacro) {
				Macro bound = target.getKernel().getMacro(fromMacro.getMacro().getCommandName());
				if (bound == null || ((AlgoMacro) toAlgo).getMacro() != bound) {
					return "macro " + label;
				}
			}
			boolean identity = sourceIds.isParticipating(from);
			if (identity != targetIds.isParticipating(to)) {
				return "identity " + label;
			}
			if (identity) {
				PersistentGeoId id = targetIds.getPersistentGeoId(to);
				// a fresh durable identity: not the source's, not one the target had
				if (id == null || idsBefore.containsValue(id) || !newIds.add(id)
						|| id.equals(sourceIds.getPersistentGeoId(from))) {
					return "identity " + label;
				}
			}
		}
		return null;
	}

	/**
	 * M1 after the commit: every transferred macro is registered once with the source's
	 * definition digest; every reused macro is still the target's own.
	 *
	 * @return null when verified, otherwise the failing macro
	 */
	static String verifyMacros(GeoCeDGInsertionAdmission admission, AppGeoCeDG target,
			AppGeoCeDG helper) {
		Set<String> required = new java.util.LinkedHashSet<>(admission.getTransferredMacros());
		required.addAll(admission.getReusedMacros());
		for (String name : required) {
			Macro inTarget = target.getKernel().getMacro(name);
			Macro inSource = helper.getKernel().getMacro(name);
			try {
				if (inTarget == null || inSource == null
						|| !GeoCeDGUserToolLibrary.definitionDigest(inSource)
								.equals(GeoCeDGUserToolLibrary.definitionDigest(inTarget))) {
					return "macro " + name;
				}
			} catch (IOException | RuntimeException malformed) {
				return "macro " + name;
			}
		}
		return null;
	}

	private static boolean sameValue(GeoElement from, GeoElement to) {
		if (from.isDefined() != to.isDefined()) {
			return false;
		}
		if (!from.isDefined()) {
			return true;
		}
		if (from instanceof GeoPoint p && to instanceof GeoPoint q) {
			return close(p.getInhomX(), q.getInhomX()) && close(p.getInhomY(), q.getInhomY());
		}
		if (from instanceof GeoBoolean p && to instanceof GeoBoolean q) {
			return p.getBoolean() == q.getBoolean();
		}
		if (from instanceof GeoNumeric p && to instanceof GeoNumeric q) {
			return close(p.getValue(), q.getValue());
		}
		if (from instanceof GeoList p && to instanceof GeoList q) {
			return p.size() == q.size();
		}
		return true;
	}

	private static boolean close(double a, double b) {
		if (Double.isNaN(a) || Double.isNaN(b)) {
			return Double.isNaN(a) && Double.isNaN(b);
		}
		return Math.abs(a - b) <= 1e-9 * Math.max(1, Math.max(Math.abs(a), Math.abs(b)));
	}

	// ------------------------------------------------------------------ helpers

	static Set<GeoElement> identitySet(App app) {
		Set<GeoElement> set = Collections.newSetFromMap(new IdentityHashMap<>());
		set.addAll(app.getKernel().getConstruction().getGeoSetWithCasCellsConstructionOrder());
		return set;
	}

	static List<GeoElement> createdSince(App app, Set<GeoElement> before) {
		List<GeoElement> created = new ArrayList<>();
		for (GeoElement geo : app.getKernel().getConstruction()
				.getGeoSetWithCasCellsConstructionOrder()) {
			if (!before.contains(geo)) {
				created.add(geo);
			}
		}
		return created;
	}

	static Map<GeoElement, PersistentGeoId> identities(App app) {
		Map<GeoElement, PersistentGeoId> ids = new IdentityHashMap<>();
		Construction construction = app.getKernel().getConstruction();
		for (GeoElement geo : construction.getGeoSetConstructionOrder()) {
			PersistentGeoId id = construction.getSpatialIdentityRegistry().getPersistentGeoId(geo);
			if (id != null) {
				ids.put(geo, id);
			}
		}
		return ids;
	}

	static List<String> macroNames(App app) {
		List<String> names = new ArrayList<>();
		for (int i = 0; i < app.getKernel().getMacroNumber(); i++) {
			names.add(app.getKernel().getMacro(i).getCommandName());
		}
		return names;
	}

	private String text(String key, String... arguments) {
		return app.layerText(key, arguments);
	}

	/** State held between the barrier and the end of the operation. */
	private static final class Barrier {
		final GeoCeDGUndoManager.HeldHistory held;
		final HistoryRecord history;
		final UndoCommand baseline;
		final GeoCeDGRecoverySnapshot snapshot;

		Barrier(GeoCeDGUndoManager.HeldHistory held, HistoryRecord history, UndoCommand baseline,
				GeoCeDGRecoverySnapshot snapshot) {
			this.held = held;
			this.history = history;
			this.baseline = baseline;
			this.snapshot = snapshot;
		}
	}

	private static final class BarrierRejection extends Exception {
		private static final long serialVersionUID = 1L;

		BarrierRejection(String key) {
			super(key);
		}
	}

	/**
	 * Per-entry history metadata recorded before the detach: the original command
	 * objects, the cursor, labels and grouping, and the snapshot file of each state.
	 */
	static final class HistoryRecord {
		private final List<UndoCommand> commands;
		private final int cursor;
		private final List<List<String>> labels = new ArrayList<>();
		private final List<Boolean> grouping = new ArrayList<>();
		private final List<File> files = new ArrayList<>();

		private HistoryRecord(List<UndoCommand> commands, int cursor) {
			this.commands = commands;
			this.cursor = cursor;
			for (UndoCommand command : commands) {
				labels.add(command.labels == null ? null : new ArrayList<>(command.labels));
				grouping.add(command.shouldStitchToNext());
				files.add(command.getAppState() instanceof FileAppState state ? state.getFile()
						: null);
			}
		}

		static HistoryRecord of(GeoCeDGUndoManager undo) {
			synchronized (undo) {
				return new HistoryRecord(undo.getCommands(), undo.getCursor());
			}
		}

		/**
		 * @return null when the live history is exactly the recorded one, otherwise the
		 *         first difference
		 */
		String mismatch(GeoCeDGUndoManager undo, GeoCeDGUndoManager.HeldHistory held) {
			synchronized (undo) {
				List<UndoCommand> live = undo.getCommands();
				if (live.size() != commands.size()) {
					return "size " + live.size() + "/" + commands.size();
				}
				if (undo.getCursor() != cursor || held.getCursor() != cursor) {
					return "cursor";
				}
				for (int i = 0; i < live.size(); i++) {
					UndoCommand command = live.get(i);
					if (command != commands.get(i) || command != held.getCommands().get(i)) {
						return "command " + i;
					}
					if (!Objects.equals(labels.get(i), command.labels)) {
						return "labels " + i;
					}
					if (grouping.get(i) != command.shouldStitchToNext()) {
						return "grouping " + i;
					}
					File file = files.get(i);
					if (file != null && !file.exists()) {
						return "snapshot file " + i;
					}
				}
				return null;
			}
		}
	}

	/** Serializable and runtime state compared separately (feasibility report G1). */
	static final class Certificate {
		final String xml;
		final String identities;
		final Map<String, String> macroDigests;
		final boolean saved;
		final File file;
		final String uuid;
		final Map<String, GeoElement> instances;
		final GeoElement parallel;
		final GeoElement perpendicular;
		final Object linkedSheet;

		private Certificate(AppGeoCeDG app) {
			xml = normalize(app.getXML());
			StringBuilder ids = new StringBuilder();
			instances = new LinkedHashMap<>();
			Construction construction = app.getKernel().getConstruction();
			for (GeoElement geo : construction.getGeoSetConstructionOrder()) {
				instances.put(geo.getLabelSimple(), geo);
				PersistentGeoId id = construction.getSpatialIdentityRegistry()
						.getPersistentGeoId(geo);
				if (id != null) {
					ids.append(geo.getLabelSimple()).append('=').append(id).append(' ');
				}
			}
			identities = ids.toString();
			macroDigests = new LinkedHashMap<>();
			for (int i = 0; i < app.getKernel().getMacroNumber(); i++) {
				Macro macro = app.getKernel().getMacro(i);
				String digest;
				try {
					digest = GeoCeDGUserToolLibrary.definitionDigest(macro);
				} catch (IOException | RuntimeException malformed) {
					digest = "malformed";
				}
				macroDigests.put(macro.getCommandName(), digest);
			}
			saved = app.isSaved();
			file = app.getCurrentFile();
			uuid = app.getUniqueId();
			GeoCeDGLineReferenceMemory memory = referenceMemory(app);
			parallel = memory == null ? null
					: memory.peek(GeoCeDGLineReferenceMemory.Slot.PARALLEL);
			perpendicular = memory == null ? null
					: memory.peek(GeoCeDGLineReferenceMemory.Slot.PERPENDICULAR);
			linkedSheet = app.getLinkedIsoABorder();
		}

		static Certificate of(AppGeoCeDG app) {
			return new Certificate(app);
		}

		private static GeoCeDGLineReferenceMemory referenceMemory(AppGeoCeDG app) {
			if (app.getActiveEuclidianView() != null && app.getActiveEuclidianView()
					.getEuclidianController() instanceof GeoCeDGEuclidianController controller) {
				return controller.getReferenceMemory();
			}
			return null;
		}

		static String normalize(String xml) {
			// per-instance in-memory Locus V2 revision counters are not document content
			return xml.replaceAll("revision=\"\\d+\"", "revision=\"#\"");
		}

		List<String> serializableDifferences(Certificate other) {
			List<String> differences = new ArrayList<>();
			if (!xml.equals(other.xml)) {
				differences.add("document XML");
			}
			if (!identities.equals(other.identities)) {
				differences.add("durable identities");
			}
			if (!macroDigests.equals(other.macroDigests)) {
				differences.add("macros");
			}
			if (saved != other.saved) {
				differences.add("saved state");
			}
			if (!Objects.equals(file, other.file)) {
				differences.add("file association");
			}
			if (!Objects.equals(uuid, other.uuid)) {
				differences.add("document identity");
			}
			return differences;
		}

		boolean sameInstances(Certificate other) {
			if (!instances.keySet().equals(other.instances.keySet())) {
				return false;
			}
			for (Map.Entry<String, GeoElement> entry : instances.entrySet()) {
				if (other.instances.get(entry.getKey()) != entry.getValue()) {
					return false;
				}
			}
			return parallel == other.parallel && perpendicular == other.perpendicular
					&& linkedSheet == other.linkedSheet;
		}
	}
}
