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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.SwingUtilities;

import org.geocedg.common.document.controls.ControlLayout.Panel;
import org.geocedg.common.document.controls.ControlLayoutException;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.units.DocumentUnitSystem;
import org.geocedg.common.kernel.units.MixedAngleInsertionProof;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.desktop.GeoCeDGAngularSourceArchive.Finding;
import org.geocedg.desktop.GeoCeDGAngularSourceArchive.Refusal;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.LayerView;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.main.App;
import org.geogebra.common.main.undo.AppState;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventDispatcher;
import org.geogebra.common.plugin.EventListener;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.layout.DockManagerD;
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
	private static final long EXPERT_OFFER_NANOS = TimeUnit.MINUTES.toNanos(10);
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
		private final UndoCommand expertStep;

		Result(Outcome outcome, String message, List<String> declaredResets, Throwable failure) {
			this(outcome, message, declaredResets, failure, null);
		}

		private Result(Outcome outcome, String message, List<String> declaredResets, Throwable failure,
				UndoCommand expertStep) {
			this.outcome = outcome;
			this.message = message;
			this.declaredResets = List.copyOf(declaredResets);
			this.failure = failure;
			this.expertStep = expertStep;
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

		/** The original attached history command for a successful expert publication. */
		UndoCommand getExpertStep() {
			return expertStep;
		}
	}

	private final AppGeoCeDG app;
	private Faults faults = NO_FAULTS;
	private boolean awaitingAngularConfirmation;
	private ExpertReview activeExpertReview;

	record AngularSample(String label, String original, String adapted) {
	}

	record AngularPreview(String sourceName, int sourceMode, int targetMode,
			List<AngularSample> samples, List<AngularSample> dynamicTexts) {
		AngularPreview {
			samples = List.copyOf(samples);
			dynamicTexts = List.copyOf(dynamicTexts);
		}
	}

	/**
	 * @param app target document window
	 */
	GeoCeDGDocumentInsertion(AppGeoCeDG app) {
		this.app = app;
	}

	void setFaults(Faults faults) {
		this.faults = faults == null ? NO_FAULTS : faults;
	}

	/** Opens the one live review for this document. Invalidations start before capture. */
	ExpertReview beginExpertReview() throws IOException {
		requireExpertEdt();
		if (!app.getKernel().getConstruction().getControlLayout().isEmpty()) {
			throw new IOException(text("Insert.Reject.ControlLayoutReceiver"));
		}
		if (activeExpertReview != null || awaitingAngularConfirmation || app.isInsertionIsolated()
				|| !app.getKernel().isUndoActive() || !(app.getUndoManager() instanceof GeoCeDGUndoManager undo)
				|| !awaitQuiescence(undo)) {
			throw new IOException("Receiver cannot start an expert review");
		}
		ExpertReview review = new ExpertReview(undo);
		try {
			review.snapshot = GeoCeDGLiveInspectionSnapshot.capture(app);
			if (!review.watch.isCurrent()) {
				throw new IOException("Receiver changed during expert capture");
			}
			activeExpertReview = review;
			return review;
		} catch (IOException | RuntimeException failure) {
			review.close();
			throw failure;
		}
	}

	final class ExpertReview implements AutoCloseable {
		private final ExpertInvalidation watch;
		private GeoCeDGLiveInspectionSnapshot snapshot;
		private ExpertOffer offer;
		private boolean consumed;
		private boolean closed;
		private boolean invalidated;

		private ExpertReview(GeoCeDGUndoManager undo) {
			watch = new ExpertInvalidation(app, undo);
		}

		GeoCeDGLiveInspectionSnapshot snapshot() {
			return snapshot;
		}

		boolean isCurrent() {
			requireExpertEdt();
			return !consumed && isFresh();
		}

		private boolean isFresh() {
			if (!invalidated) {
				invalidated = closed || activeExpertReview != this || !watch.isCurrent()
						|| offer != null && !offer.sourceVersionCurrent();
			}
			return !invalidated;
		}

		ExpertOffer offer(File source, byte[] sourceBytes, String fingerprint, String reportSha256,
				BooleanSupplier sourceCurrent) throws IOException {
			if (!isCurrent() || offer != null || !sourceStillCurrent(sourceCurrent)) {
				throw new IOException("Expert review is no longer current");
			}
			if (GeoCeDGDocumentPolicy.insertionRefusal(source) != null || sourceBytes == null
					|| sourceBytes.length == 0 || sourceBytes.length > GeoCeDGInspectionArchive.MAX_ARCHIVE_BYTES
					|| !isSha256(fingerprint) || !isSha256(reportSha256)) {
				throw new IOException("Expert evidence is incomplete");
			}
			offer = new ExpertOffer(this, source, sourceBytes, fingerprint, reportSha256,
					sourceCurrent);
			return offer;
		}

		@Override
		public void close() {
			requireExpertEdt();
			if (!closed) {
				closed = true;
				watch.close();
				if (snapshot != null) {
					snapshot.close();
				}
				if (activeExpertReview == this) {
					activeExpertReview = null;
				}
			}
		}
	}

	/** Opaque, one-use local handle for the exact report and trial shown to the user. */
	static final class ExpertOffer {
		private final ExpertReview review;
		private final File source;
		private final byte[] sourceBytes;
		private final String fingerprint;
		private final String reportSha256;
		private final BooleanSupplier sourceCurrent;
		private final long issuedAt = System.nanoTime();

		private ExpertOffer(ExpertReview review, File source, byte[] sourceBytes, String fingerprint,
				String reportSha256, BooleanSupplier sourceCurrent) {
			this.review = review;
			this.source = source;
			this.sourceBytes = sourceBytes.clone();
			this.fingerprint = fingerprint;
			this.reportSha256 = reportSha256;
			this.sourceCurrent = sourceCurrent;
		}

		private boolean sourceVersionCurrent() {
			return System.nanoTime() - issuedAt < EXPERT_OFFER_NANOS
					&& sourceStillCurrent(sourceCurrent);
		}

		private boolean exactSourceCurrent() {
			return sourceVersionCurrent() && sameSource(source, sourceBytes) && sourceVersionCurrent();
		}
	}

	/** Consumes the consent before any native source load or barrier entry. */
	Result publishExpert(ExpertOffer offer, String acceptedReportSha256) {
		requireExpertEdt();
		ExpertReview review = offer.review;
		String name = offer.source.getName();
		if (activeExpertReview != review || review.offer != offer || review.consumed) {
			return staleExpertOffer(name);
		}
		review.consumed = true;
		AppGeoCeDG helper = null;
		try {
			if (!offer.reportSha256.equals(acceptedReportSha256) || !review.isFresh()) {
				return staleExpertOffer(name);
			}
			try {
				if (!app.getKernel().getConstruction().getControlLayout().isEmpty()) {
					throw new IOException(text("Insert.Reject.ControlLayoutReceiver"));
				}
				if (!passiveReceiver()) {
					throw new IOException("The receiver has active content or animation");
				}
				helper = GeoCeDGExpertIntegrity.loadSource(app, offer.sourceBytes);
			} catch (IOException | RuntimeException failure) {
				Log.debug(failure);
				return new Result(Outcome.REJECTED_BEFORE_MUTATION,
						text("Insert.Expert.CommitRejected", name, describe(failure)), List.of(), failure);
			}
			return transactExpert(name, helper, offer);
		} finally {
			try {
				GeoCeDGExpertIntegrity.releaseSource(app, helper);
			} finally {
				review.close();
			}
		}
	}

	private static boolean sourceStillCurrent(BooleanSupplier watch) {
		try {
			return watch.getAsBoolean();
		} catch (RuntimeException unavailable) {
			return false;
		}
	}

	private static boolean isSha256(String digest) {
		return digest != null && digest.matches("[0-9a-f]{64}");
	}

	private static void requireExpertEdt() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Expert insertion runs on the event dispatch thread");
		}
	}

	private Result staleExpertOffer(String name) {
		return new Result(Outcome.REJECTED_BEFORE_MUTATION,
				text("Insert.Expert.Stale", name), List.of(), null);
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
		return insert(file, null);
	}

	/** Inserts with explicit consent available for a certified mixed-mode source. */
	Result insert(File file, Predicate<AngularPreview> confirmation) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Insert File runs on the event dispatch thread");
		}
		String name = file == null ? "" : file.getName();
		if (app.isInsertionIsolated()) {
			return rejected(name, "Insert.Reject.Isolated", "");
		}
		if (awaitingAngularConfirmation) {
			return rejected(name, "Insert.Angle.Pending", "");
		}
		String extensionReason = GeoCeDGDocumentPolicy.insertionRefusal(file);
		if (extensionReason != null) {
			return rejected(name, extensionReason, "");
		}
		byte[] bytes;
		try {
			bytes = Files.readAllBytes(file.toPath());
			if (DocumentArchivePreflight.hasControlLayout(bytes)) {
				return rejected(name, "Insert.Reject.ControlLayoutSource", "");
			}
		} catch (ControlLayoutException rejectedLayout) {
			return rejected(name, "Insert.Reject.ControlLayoutSource", "");
		} catch (IOException | XMLParseException unreadable) {
			return rejected(name, "Insert.Reject.Invalid", "");
		}
		int targetMode = app.getKernel().getAngleUnit();
		GeoCeDGAngularSourceArchive.Inspection angular = confirmation == null ? null
				: GeoCeDGAngularSourceArchive.inspect(bytes, targetMode);
		if (angular != null && angular.status() == GeoCeDGAngularSourceArchive.Status.REJECTED) {
			if (!angular.findings().isEmpty()) {
				return rejectedArchive(name, angular.findings());
			}
			return angular.differentModes() ? unverifiedAdaptation(name, angular.detail())
					: rejected(name, "Insert.Angle.Unverified", "");
		}
		if (!preflight(bytes)) {
			return rejected(name, "Insert.Reject.Invalid", "");
		}
		AppGeoCeDG helper = null;
		try {
			if (angular != null && angular.status() == GeoCeDGAngularSourceArchive.Status.ADAPTABLE) {
				return insertMixedAngles(file, bytes, angular.sourceMode(), targetMode, confirmation);
			}
			helper = app.newInsertionHelper();
			if (!helper.loadFile(file, false)) {
				return rejected(name, "Insert.Reject.Invalid", "");
			}
			if (!helper.getKernel().getConstruction().getControlLayout().isEmpty()) {
				return rejected(name, "Insert.Reject.ControlLayoutSource", "");
			}
			helper.getKernel().getAnimationManager().stopAnimation();
			GeoCeDGInsertionAdmission admission = GeoCeDGInsertionAdmission.admit(app, helper);
			if (!admission.isAdmitted()) {
				return rejected(name, admission.getReasonKey(), admission.getReasonArgument());
			}
			String dryRun = dryRun(file, null, null);
			if (dryRun != null) {
				return rejected(name, "Insert.Reject.DryRun", dryRun);
			}
			return transact(name, helper, admission, null);
		} catch (RuntimeException unexpected) {
			Log.debug(unexpected);
			// PRE-G9B-R6-plus-F3-R1: the stage and cause are kept, never a generic reason
			return new Result(Outcome.REJECTED_BEFORE_MUTATION,
					text("Insert.Rejected", name, text("Insert.Reject.Internal",
							describe(unexpected))), List.of(), unexpected);
		} finally {
			app.releaseInsertionHelper(helper);
		}
	}

	private Result insertMixedAngles(File file, byte[] bytes, int sourceMode, int targetMode,
			Predicate<AngularPreview> confirmation) {
		String name = file.getName();
		AppGeoCeDG original = null;
		AppGeoCeDG adapted = null;
		try {
			if (!passiveReceiver()) {
				return rejected(name, "Insert.Angle.UnverifiedReceiver", "");
			}
			original = app.newInsertionHelper();
			adapted = app.newInsertionHelper();
			if (!GeoCeDGAngularSourceArchive.load(original, bytes, targetMode, sourceMode)
					|| !GeoCeDGAngularSourceArchive.load(adapted, bytes, sourceMode, sourceMode)) {
				return rejected(name, "Insert.Reject.Invalid", "");
			}
			GeoCeDGInsertionAdmission originalAdmission = GeoCeDGInsertionAdmission.admit(app,
					original);
			if (!"Insert.Reject.AngleUnit".equals(originalAdmission.getReasonKey())) {
				return originalAdmission.isAdmitted() ? unverifiedAdaptation(name, "")
						: rejected(name, originalAdmission.getReasonKey(), originalAdmission.getReasonArgument());
			}
			Construction originalConstruction = original.getKernel().getConstruction();
			Construction isolatedConstruction = adapted.getKernel().getConstruction();
			StringBuilder detail = new StringBuilder();
			Consumer<GeoElement> report = geo -> {
				if (detail.length() == 0) {
					detail.append(geo.getLabelSimple()).append(" (").append(geo.getTypeString()).append(')');
				}
			};
			MixedAngleInsertionProof source = MixedAngleInsertionProof.capture(originalConstruction, report);
			MixedAngleInsertionProof copy = MixedAngleInsertionProof.capture(isolatedConstruction, report);
			if (source == null || !source.sameState(copy)) {
				return unverifiedAdaptation(name, detail.toString());
			}
			adapted.getKernel().setAngleUnit(targetMode);
			adapted.getKernel().updateConstruction(false);
			MixedAngleInsertionProof proof = MixedAngleInsertionProof.capture(isolatedConstruction, report);
			GeoCeDGInsertionAdmission admission = GeoCeDGInsertionAdmission.admit(app, adapted);
			if (!admission.isAdmitted()) {
				return rejected(name, admission.getReasonKey(), admission.getReasonArgument());
			}
			if (!copy.sameState(proof)) {
				return unverifiedAdaptation(name, detail.toString());
			}
			if (!app.getKernel().isUndoActive() || !(app.getKernel().getConstruction()
					.getUndoManager() instanceof GeoCeDGUndoManager undo)) {
				return rejected(name, "Insert.Reject.UndoUnavailable", "");
			}
			if (!awaitQuiescence(undo)) {
				return rejected(name, "Insert.Reject.HistoryBusy", "");
			}
			AngularOfferState offer = new AngularOfferState(app, undo);
			AngularPreview preview = preview(name, original, adapted, admission.getSource());
			app.getEventDispatcher().addEventListener(offer);
			awaitingAngularConfirmation = true;
			try {
				if (!confirmation.test(preview)) {
					return new Result(Outcome.REJECTED_BEFORE_MUTATION, null, List.of(), null);
				}
			} finally {
				awaitingAngularConfirmation = false;
				app.getEventDispatcher().removeEventListener(offer);
			}
			if (!offer.isCurrent(app) || !passiveReceiver() || !sameSource(file, bytes)) {
				return staleOffer(name);
			}
			try {
				prepareCopyBaseline(adapted);
			} catch (IOException failure) {
				Log.debug(failure);
				return rejected(name, "Insert.Reject.DryRun", text("Insert.Reject.TrialSourceUndo"));
			}
			String mismatch = dryRun(file, adapted, proof);
			if (mismatch != null) {
				return rejected(name, "Insert.Reject.DryRun", mismatch);
			}
			if (!offer.isCurrent(app) || !passiveReceiver() || !sameSource(file, bytes)) {
				return staleOffer(name);
			}
			return transact(name, adapted, admission, proof);
		} catch (IOException | XMLParseException invalid) {
			Log.debug(invalid);
			return rejected(name, "Insert.Reject.Invalid", "");
		} finally {
			app.releaseInsertionHelper(adapted);
			app.releaseInsertionHelper(original);
		}
	}

	private boolean passiveReceiver() {
		Kernel kernel = app.getKernel();
		String script = kernel.getLibraryJavaScript();
		return !kernel.wantAnimationStarted() && !kernel.getAnimationManager().isRunning()
				&& !kernel.getAnimationManager().isPaused()
				&& (script == null || script.isBlank() || Kernel.defaultLibraryJavaScript.equals(script.strip()))
				&& !app.getScriptManager().hasListeners()
				&& kernel.getConstruction().getGeoSetConstructionOrder().stream()
						.noneMatch(GeoCeDGInsertionAdmission::hasScript);
	}

	private AngularPreview preview(String name, AppGeoCeDG original, AppGeoCeDG adapted,
			List<GeoElement> source) {
		List<AngularSample> samples = new ArrayList<>();
		List<AngularSample> dynamicTexts = new ArrayList<>();
		for (GeoElement geo : source) {
			String label = geo.getLabelSimple();
			if (geo instanceof GeoAngle angle) {
				GeoElement prior = original.getKernel().lookupLabel(label);
				samples.add(new AngularSample(label, prior.toValueString(StringTemplate.defaultTemplate),
						formatForReceiver(angle, StringTemplate.defaultTemplate)));
			} else if (geo instanceof GeoText angleText && !angleText.isIndependent()) {
				// The mixed-mode certificate only admits native terminal angle text here.
				GeoAngle angle = (GeoAngle) angleText.getParentAlgorithm().getInput(0);
				GeoText prior = (GeoText) original.getKernel().lookupLabel(label);
				dynamicTexts.add(new AngularSample(label, prior.getTextString(),
						formatForReceiver(angle, angleText.getStringTemplate())));
			}
		}
		return new AngularPreview(name, original.getKernel().getAngleUnit(),
				adapted.getKernel().getAngleUnit(), samples, dynamicTexts);
	}

	private String formatForReceiver(GeoAngle angle, StringTemplate template) {
		Kernel kernel = app.getKernel();
		boolean unbounded = angle.getAngleStyle() == GeoAngle.AngleStyle.UNBOUNDED;
		return angle.isEuclidianVisible()
				? kernel.formatAngle(angle.getValue(), angle.toDecimal(), 1 / angle.getAnimationStep(),
						template, unbounded, angle.isKeepDegrees()).toString()
				: kernel.formatAngle(angle.getValue(), angle.toDecimal(), template, unbounded,
						angle.isKeepDegrees()).toString();
	}

	private static boolean sameSource(File file, byte[] bytes) {
		try (InputStream input = Files.newInputStream(file.toPath())) {
			return Arrays.equals(bytes, input.readNBytes(bytes.length + 1));
		} catch (IOException unreadable) {
			return false;
		}
	}

	private Result staleOffer(String name) {
		return new Result(Outcome.REJECTED_BEFORE_MUTATION, text("Insert.Angle.Stale", name),
				List.of(), null);
	}

	private Result unverifiedAdaptation(String name, String detail) {
		String explanation = detail.isEmpty() ? "" : "\n" + text("Insert.Angle.Detail", detail);
		return new Result(Outcome.REJECTED_BEFORE_MUTATION,
				text("Insert.Angle.ManualAlternative", name, explanation), List.of(), null);
	}

	private Result rejectedArchive(String name, List<Finding> findings) {
		String message = text("Insert.Rejected", name, describeFinding(findings.get(0)));
		List<String> additional = findings.stream().skip(1).filter(Finding::independentlyKnown)
				.map(this::describeFinding).toList();
		if (!additional.isEmpty()) {
			message += "\n" + text("Insert.Reject.AlsoDetected", String.join(" · ", additional));
		}
		return new Result(Outcome.REJECTED_BEFORE_MUTATION, message, List.of(), null);
	}

	private String describeFinding(Finding finding) {
		String reason = refusalText(finding.cause().key, finding.detail());
		return finding.cause() == Refusal.UNDETERMINED && !finding.detail().isEmpty()
				? reason + " (" + finding.detail() + ")" : reason;
	}

	private static final class AngularOfferState implements EventListener {
		private final Certificate document;
		private final ReceiverGeometry geometry;
		private final GeoCeDGUndoManager undo;
		private final HistoryRecord history;
		private final int tool;
		private final boolean displayable;
		private final DockManagerD viewActivationOwner;
		private final long viewActivationGeneration;
		private final List<GeoElement> selection;
		private volatile boolean changed;

		AngularOfferState(AppGeoCeDG app, GeoCeDGUndoManager undo) {
			document = Certificate.of(app);
			geometry = new ReceiverGeometry(app);
			this.undo = undo;
			history = HistoryRecord.of(undo);
			tool = app.getMode();
			displayable = app.getMainComponent().isDisplayable();
			viewActivationOwner = app.getGuiManager() instanceof GuiManagerD gui && gui.getLayout() != null
					? gui.getLayout().getDockManager() : null;
			viewActivationGeneration = viewActivationOwner == null ? 0
					: viewActivationOwner.getEuclidianViewActivationGeneration();
			selection = List.copyOf(app.getSelectionManager().getSelectedGeos());
		}

		@Override
		public void sendEvent(Event event) {
			switch (event.type) {
				case ADD, REMOVE, RENAME, REDEFINE, UPDATE, UPDATE_STYLE, STOREUNDO, UNDO, REDO,
						SET_MODE, SELECT, DESELECT, VIEW_PROPERTIES_CHANGED -> changed = true;
				default -> { }
			}
		}

		@Override
		public void reset() {
			changed = true;
		}

		boolean isCurrent(AppGeoCeDG app) {
			if (changed || app.isInsertionIsolated() || !app.getKernel().isUndoActive()
					|| app.getKernel().getConstruction().getUndoManager() != undo
					|| app.getMainComponent().isDisplayable() != displayable
					|| app.getMode() != tool
					|| viewActivationOwner != null && viewActivationOwner.getEuclidianViewActivationGeneration()
							!= viewActivationGeneration
					|| !selection.equals(app.getSelectionManager().getSelectedGeos())
					|| !geometry.sameGraph(app)) {
				return false;
			}
			synchronized (undo) {
				if (!undo.isQuiescent() || history.mismatch(undo, null) != null) {
					return false;
				}
			}
			Certificate current = Certificate.of(app);
			return document.sameInstances(current) && document.serializableDifferences(current).isEmpty();
		}
	}

	/** A latched invalidation, including presentation changes omitted by native XML. */
	private static final class ExpertInvalidation implements EventListener, AutoCloseable {
		private final AppGeoCeDG app;
		private final GeoCeDGUndoManager undo;
		private final DocumentUnitSystem units;
		private final EventDispatcher dispatcher;
		private final LayerEvents layerEvents;
		private final Runnable workspaceListener = this::reset;
		private final DockManagerD dock;
		private final long viewActivation;
		private final boolean displayable;
		private final Construction construction;
		private final long protectedLabelRevision;
		private final long pathRegionHandlingRevision;
		private final long historyActivity;
		private volatile boolean changed;

		ExpertInvalidation(AppGeoCeDG app, GeoCeDGUndoManager undo) {
			this.app = app;
			this.undo = undo;
			dock = app.getGuiManager() instanceof GuiManagerD gui && gui.getLayout() != null
					? gui.getLayout().getDockManager() : null;
			viewActivation = dock == null ? 0 : dock.getEuclidianViewActivationGeneration();
			displayable = app.getMainComponent().isDisplayable();
			construction = app.getKernel().getConstruction();
			protectedLabelRevision = construction.getProtectedLabelRevision();
			pathRegionHandlingRevision = app.getKernel().getPathRegionHandlingRevision();
			dispatcher = app.getEventDispatcher();
			synchronized (undo) {
				historyActivity = undo.getHistoryActivity();
				dispatcher.addEventListener(this);
			}
			layerEvents = new LayerEvents(app, this::reset);
			layerEvents.addEventListener(this);
			app.getLayerWorkspace().addListener(workspaceListener);
			units = app.getDocumentUnits();
			units.addListener(workspaceListener);
		}

		@Override
		public void sendEvent(Event event) {
			switch (event.type) {
			case ADD, REMOVE, RENAME, REDEFINE, UPDATE, UPDATE_STYLE, STOREUNDO, UNDO, REDO,
					SET_MODE, SELECT, DESELECT, VIEW_PROPERTIES_CHANGED, VIEW_CHANGED_2D,
					VIEW_CHANGED_3D, PERSPECTIVE_CHANGE, PASTE_ELMS, CLEAR, ADD_MACRO, REMOVE_MACRO,
					RENAME_MACRO, MOVED_GEOS, DELETE_GEOS, START_ANIMATION, STOP_ANIMATION -> reset();
			default -> { }
			}
		}

		@Override
		public void reset() {
			changed = true;
		}

		boolean isCurrent() {
			return !changed && !app.isInsertionIsolated()
					&& undo.getHistoryActivity() == historyActivity
					&& app.getKernel().getConstruction() == construction
					&& construction.getProtectedLabelRevision() == protectedLabelRevision
					&& app.getKernel().getPathRegionHandlingRevision() == pathRegionHandlingRevision
					&& app.getMainComponent().isDisplayable() == displayable
					&& (dock == null || dock.getEuclidianViewActivationGeneration() == viewActivation);
		}

		@Override
		public void close() {
			synchronized (undo) {
				dispatcher.removeEventListener(this);
			}
			layerEvents.removeEventListener(this);
			app.getKernel().detach(layerEvents);
			app.getLayerWorkspace().removeListener(workspaceListener);
			units.removeListener(workspaceListener);
		}
	}

	/** Receives native layer events even though the script dispatcher ignores them. */
	private static final class LayerEvents extends EventDispatcher implements LayerView {
		private final Runnable invalidation;

		LayerEvents(AppGeoCeDG app, Runnable invalidation) {
			super(app);
			this.invalidation = invalidation;
		}

		@Override
		public void changeLayer(GeoElement geo, int oldLayer, int newLayer) {
			if (oldLayer != newLayer) {
				invalidation.run();
			}
		}
	}

	/** The preexisting graph and styles must survive B2 in both the trial and the commit. */
	private static final class ReceiverGeometry {
		private final Construction construction;
		private final int mode;
		private final Map<GeoElement, ReceiverObject> objects = new IdentityHashMap<>();
		private final Map<GeoElement, PersistentGeoId> ids;
		private final Map<AlgoElement, AlgorithmEdges> edges = new IdentityHashMap<>();

		ReceiverGeometry(AppGeoCeDG app) {
			construction = app.getKernel().getConstruction();
			mode = app.getKernel().getAngleUnit();
			identitySet(app).forEach(geo -> objects.put(geo, new ReceiverObject(
					Certificate.normalize(geo.getXML()), displayedText(geo))));
			ids = identities(app);
			construction.getAlgoList().forEach(algo -> edges.put(algo, new AlgorithmEdges(algo)));
		}

		boolean sameGraph(AppGeoCeDG app) {
			return identitySet(app).size() == objects.size()
					&& construction.getAlgoList().size() == edges.size() && preservedBy(app)
					&& objects.entrySet().stream().allMatch(e ->
							Objects.equals(e.getValue().text(), displayedText(e.getKey())));
		}

		boolean preservedBy(AppGeoCeDG app) {
			if (app.getKernel().getConstruction() != construction
					|| app.getKernel().getAngleUnit() != mode
					|| !identitySet(app).containsAll(objects.keySet())) {
				return false;
			}
			Map<GeoElement, PersistentGeoId> liveIds = identities(app);
			for (Map.Entry<GeoElement, ReceiverObject> entry : objects.entrySet()) {
				GeoElement geo = entry.getKey();
				if (!entry.getValue().xml().equals(Certificate.normalize(geo.getXML()))
						|| !Objects.equals(ids.get(geo), liveIds.get(geo))) {
					return false;
				}
			}
			Set<AlgoElement> algorithms = Collections.newSetFromMap(new IdentityHashMap<>());
			algorithms.addAll(construction.getAlgoList());
			return algorithms.containsAll(edges.keySet())
					&& edges.entrySet().stream().allMatch(e -> e.getValue().matches(e.getKey()));
		}

		private static String displayedText(GeoElement geo) {
			return geo instanceof GeoText text ? text.getTextString() : null;
		}
	}

	private record ReceiverObject(String xml, String text) {
	}

	private record AlgorithmEdges(GeoElement[] input, GeoElement[] output) {
		AlgorithmEdges(AlgoElement algo) {
			this(copy(algo.getInput()), copy(algo.getOutput()));
		}

		private static GeoElement[] copy(GeoElement[] geos) {
			return geos == null ? null : geos.clone();
		}

		boolean matches(AlgoElement algo) {
			return Arrays.equals(input, algo.getInput()) && Arrays.equals(output, algo.getOutput());
		}
	}

	/** @return exception class and message, for a rejection diagnosis */
	static String describe(Throwable failure) {
		String message = failure.getMessage();
		return failure.getClass().getSimpleName()
				+ (message == null || message.isEmpty() ? "" : ": " + message);
	}

	private boolean preflight(byte[] bytes) {
		try {
			return DocumentArchivePreflight.validate(bytes, app.createDocumentPreflightConfig());
		} catch (Exception invalid) {
			return false;
		}
	}

	private Result rejected(String name, String key, String argument) {
		return new Result(Outcome.REJECTED_BEFORE_MUTATION,
				text("Insert.Rejected", name, refusalText(key, argument)),
				List.of(), null);
	}

	private String refusalText(String key, String argument) {
		return argument == null || argument.isEmpty() ? text(key) : text(key, argument);
	}

	// ------------------------------------------------------------------ dry run

	/**
	 * The same insertion on a scratch host loaded from the target's native archive,
	 * strictly verified.
	 *
	 * @return null when the dry run succeeds, otherwise the failure class
	 */
	private String dryRun(File file, AppGeoCeDG adapted, MixedAngleInsertionProof proof) {
		AppGeoCeDG scratch = null;
		AppGeoCeDG helper = adapted;
		try {
			ReceiverControlLayout layout = ReceiverControlLayout.capture(app);
			ByteArrayOutputStream target = new ByteArrayOutputStream();
			app.getXMLio().writeGeoGebraFile(target, false);
			scratch = app.newInsertionHelper();
			boolean nativeLayout = !layout.panels().isEmpty();
			if (proof != null || nativeLayout) {
				// The native stream loader normally schedules its initial Undo store.
				// The trial paste needs a current state before it can start.
				scratch.getKernel().setUndoActive(false);
			}
			ByteArrayInputStream trialArchive = new ByteArrayInputStream(target.toByteArray());
			if (nativeLayout) {
				if (!GFileHandler.loadPreflightedNativeXML(scratch, trialArchive)
						|| scratch.getXMLio().hasErrors()) {
					return text("Insert.Reject.ControlLayoutMismatch");
				}
				scratch.updateCommandDictionary();
				scratch.documentReplacementCommitted();
			} else if (!GFileHandler.loadXML(scratch, trialArchive, false)) {
				return "target";
			}
			ReceiverControlLayout trialLayout = ReceiverControlLayout.capture(scratch);
			if (!layout.samePresentation(trialLayout)) {
				return text("Insert.Reject.ControlLayoutMismatch");
			}
			if (proof != null || nativeLayout) {
				scratch.getKernel().setUndoActive(true);
				prepareCopyBaseline(scratch);
			}
			if (helper == null) {
				helper = app.newInsertionHelper();
				if (!helper.loadFile(file, false)) {
					return "source";
				}
			}
			if (!helper.getKernel().getConstruction().getControlLayout().isEmpty()) {
				return text("Insert.Reject.ControlLayoutSource");
			}
			helper.getKernel().getAnimationManager().stopAnimation();
			scratch.getKernel().getAnimationManager().stopAnimation();
			GeoCeDGInsertionAdmission scratchAdmission = GeoCeDGInsertionAdmission.admit(scratch,
					helper);
			if (!scratchAdmission.isAdmitted()) {
				return refusalText(scratchAdmission.getReasonKey(), scratchAdmission.getReasonArgument());
			}
			Set<GeoElement> before = identitySet(scratch);
			Map<GeoElement, PersistentGeoId> idsBefore = identities(scratch);
			ReceiverGeometry geometry = proof == null ? null : new ReceiverGeometry(scratch);
			GeoCeDGInsertionCopyPaste buffer = new GeoCeDGInsertionCopyPaste(
					scratchAdmission.getTransferredMacros());
			buffer.insertFrom(helper, scratch, scratchAdmission.getDuplicateLabels(), false);
			if (!trialLayout.sameOwners(ReceiverControlLayout.capture(scratch))) {
				return text("Insert.Reject.ControlLayoutMismatch");
			}
			List<GeoElement> created = createdSince(scratch, before);
			String mismatch = verify(scratchAdmission.getSource(), created, scratch, helper,
					idsBefore, buffer.getCopies());
			if (mismatch == null) {
				mismatch = angularCopyMismatch(proof, geometry, helper, scratch, buffer);
			}
			return mismatch != null ? mismatch : verifyMacros(scratchAdmission, scratch, helper);
		} catch (Exception | Error failure) {
			Log.debug(failure);
			return "commit: " + describe(failure);
		} finally {
			if (adapted == null) {
				app.releaseInsertionHelper(helper);
			}
			app.releaseInsertionHelper(scratch);
		}
	}

	/** The native clipboard reads the current Undo state of both disposable hosts. */
	private static void prepareCopyBaseline(AppGeoCeDG helper) throws IOException {
		GeoCeDGUndoManager undo = (GeoCeDGUndoManager) helper.getUndoManager();
		try (PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
	}

	private static String angularCopyMismatch(MixedAngleInsertionProof proof,
			ReceiverGeometry geometry, AppGeoCeDG helper, AppGeoCeDG target,
			GeoCeDGInsertionCopyPaste buffer) {
		if (proof == null) {
			return null;
		}
		if (!geometry.preservedBy(target)) {
			return "preexisting receiver geometry";
		}
		if (!proof.preservesCopy(buffer.getCopies())) {
			return "certified angular values or dependencies";
		}
		for (Map.Entry<GeoElement, GeoElement> copy : buffer.getCopies().entrySet()) {
			if (copy.getKey() instanceof GeoText from && !from.isIndependent()
					&& !GeoCeDGInsertionFidelity.sameDynamicTextPresentation(from,
							(GeoText) copy.getValue(), buffer.getCopies())) {
				return "angular text presentation " + from.getLabelSimple();
			}
		}
		return proof.sameState(MixedAngleInsertionProof.capture(helper.getKernel().getConstruction()))
				? null : "adapted source changed during copy";
	}

	// ------------------------------------------------------------------ transaction

	private Result transact(String name, AppGeoCeDG helper,
			GeoCeDGInsertionAdmission admission, MixedAngleInsertionProof proof) {
		if (!app.getKernel().isUndoActive() || !(app.getKernel().getConstruction()
				.getUndoManager() instanceof GeoCeDGUndoManager undo)) {
			return rejected(name, "Insert.Reject.UndoUnavailable", "");
		}
		Certificate before = Certificate.of(app);
		ReceiverGeometry geometry = proof == null ? null : new ReceiverGeometry(app);
		String uuid = app.getUniqueId();
		String tube = app.getTubeId();
		boolean saved = app.isSaved();
		String operationId = UUID.randomUUID().toString();

		// 0 BARRIER BLOCK: nothing is mutated before it ends
		Barrier barrier;
		try {
			barrier = barrier(undo, operationId, null, null);
		} catch (BarrierRejection rejection) {
			return rejected(name, rejection.getMessage(), "");
		}
		Transaction tx = new Transaction(name, undo, barrier, admission, before, uuid, tube,
				saved, null);
		try {
			return tx.run(helper, proof, geometry);
		} finally {
			tx.finish();
		}
	}

	private Result transactExpert(String name, AppGeoCeDG helper, ExpertOffer offer) {
		if (!app.getKernel().isUndoActive() || !(app.getUndoManager() instanceof GeoCeDGUndoManager undo)) {
			return rejected(name, "Insert.Reject.UndoUnavailable", "");
		}
		Barrier barrier;
		Certificate before;
		ExpertRecoveryState recovery;
		try {
			before = Certificate.of(app);
			recovery = ExpertRecoveryState.capture(app);
			barrier = barrier(undo, UUID.randomUUID().toString(), () -> offer.review.isFresh()
					&& offer.review.snapshot.matchesCurrentState(app) && offer.review.isFresh(),
					offer::exactSourceCurrent);
		} catch (BarrierRejection rejection) {
			return "Insert.Expert.Stale".equals(rejection.getMessage())
					? staleExpertOffer(name) : rejected(name, rejection.getMessage(), "");
		} catch (IOException | RuntimeException failure) {
			Log.debug(failure);
			return new Result(Outcome.REJECTED_BEFORE_MUTATION,
					text("Insert.Expert.CommitRejected", name, describe(failure)), List.of(), failure);
		}
		Transaction tx = new Transaction(name, undo, barrier, null, before, before.uuid, recovery.tube,
				before.saved, recovery);
		try {
			return tx.runExpert(helper, offer);
		} finally {
			tx.finish();
		}
	}

	private Barrier barrier(GeoCeDGUndoManager undo, String operationId, BooleanSupplier freshness,
			BooleanSupplier sourceFreshness)
			throws BarrierRejection {
		long deadline = System.nanoTime() + STORE_WAIT_MS * 1_000_000L;
		while (true) {
			synchronized (undo) {
				if (undo.isDegraded()) {
					throw new BarrierRejection("Insert.Reject.HistoryBusy");
				}
				if (undo.isQuiescent()) {
					return barrierBlock(undo, operationId, freshness, sourceFreshness);
				}
			}
			if (System.nanoTime() > deadline) {
				throw new BarrierRejection("Insert.Reject.HistoryBusy");
			}
			pause();
		}
	}

	/** Runs inside the monitor of the undo manager. */
	private Barrier barrierBlock(GeoCeDGUndoManager undo, String operationId, BooleanSupplier freshness,
			BooleanSupplier sourceFreshness)
			throws BarrierRejection {
		GeoCeDGRecoverySnapshot snapshot;
		try {
			faults.beforeSnapshot();
			snapshot = GeoCeDGRecoverySnapshot.capture(app, operationId);
		} catch (Exception failure) {
			Log.debug(failure);
			throw new BarrierRejection("Insert.Reject.Snapshot");
		}
		if (freshness != null && !sourceStillCurrent(freshness)) {
			snapshot.release();
			throw new BarrierRejection("Insert.Expert.Stale");
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
			if (sourceFreshness != null && !sourceStillCurrent(sourceFreshness)) {
				throw new BarrierRejection("Insert.Expert.Stale");
			}
		} catch (IOException | RuntimeException | BarrierRejection failure) {
			Log.debug(failure);
			List<UndoCommand> temporary = undo.getCommands();
			undo.attach(held.getCommands(), held.getCursor());
			for (UndoCommand command : temporary) {
				deleteQuietly(command, null);
			}
			snapshot.release();
			if (failure instanceof BarrierRejection rejection) {
				throw rejection;
			}
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

	/** Session state the F3 archive and Undo XML can omit. */
	private record ExpertRecoveryState(Map<String, EffectiveLayer> layers, Set<String> reservations,
			int workingLayer, int maxLayer, int activeView, int tool, List<GeoElement> selection,
			String tube, boolean scriptsBlocked, Map<String, String> resources) {
		private record EffectiveLayer(GeoElement instance, int layer) {
		}

		static ExpertRecoveryState capture(AppGeoCeDG app) throws IOException {
			Construction construction = app.getKernel().getConstruction();
			Map<String, EffectiveLayer> layers = new LinkedHashMap<>();
			for (GeoElement geo : construction.getGeoSetWithCasCellsConstructionOrder()) {
				layers.put(geo.getLabelSimple(), new EffectiveLayer(geo, geo.getLayer()));
			}
			return new ExpertRecoveryState(layers, construction.snapshotProtectedLabels(),
					app.getLayerWorkspace().getWorkingLayer(), app.getMaxLayerUsed(),
					app.getActiveEuclidianView().getViewID(), app.getMode(),
					List.copyOf(app.getSelectionManager().getSelectedGeos()), app.getTubeId(),
					app.isBlockUpdateScripts(), GeoCeDGExpertIntegrity.receiverResources(app));
		}

		void restore(AppGeoCeDG app) {
			Construction construction = app.getKernel().getConstruction();
			boolean sameInstances = true;
			for (var entry : layers.entrySet()) {
				// This address belongs to the certified barrier snapshot of this same document.
				GeoElement geo = app.getKernel().lookupLabel(entry.getKey());
				EffectiveLayer previous = entry.getValue();
				if (geo == null || geo.getClass() != previous.instance().getClass()) {
					throw new IllegalStateException("Missing recovery layer address " + entry.getKey());
				}
				sameInstances &= geo == previous.instance();
				geo.setLayer(previous.layer());
			}
			construction.restoreProtectedLabels(reservations);
			app.restoreMaxLayerUsed(maxLayer);
			if (app.getLayerWorkspace().getWorkingLayer() != workingLayer) {
				if (!app.getLayerWorkspace().isLayerShown(workingLayer)) {
					throw new IllegalStateException("The pre-insertion working layer became hidden");
				}
				app.getLayerWorkspace().setWorkingLayer(workingLayer);
			}
			if (app.getActiveEuclidianView().getViewID() != activeView) {
				app.setActiveView(activeView);
			}
			if (sameInstances) {
				if (app.getMode() != tool) {
					app.setMode(tool);
				}
				if (!selection.equals(app.getSelectionManager().getSelectedGeos())) {
					app.getSelectionManager().clearSelectedGeos(false, false);
					for (GeoElement geo : selection) {
						app.getSelectionManager().addSelectedGeo(geo, false, false);
					}
					app.updateSelection(false);
				}
			}
			app.setBlockUpdateScripts(scriptsBlocked);
		}

		void requireResources(AppGeoCeDG app) throws IOException {
			if (!resources.equals(GeoCeDGExpertIntegrity.receiverResources(app))) {
				throw new IOException("Receiver defaults or resources differ after expert recovery");
			}
		}

		boolean matches(AppGeoCeDG app, boolean sameInstances) {
			Construction construction = app.getKernel().getConstruction();
			return reservations.equals(construction.snapshotProtectedLabels())
					&& app.getLayerWorkspace().getWorkingLayer() == workingLayer
					&& app.getMaxLayerUsed() == maxLayer
					&& app.getActiveEuclidianView().getViewID() == activeView
					&& Objects.equals(app.getTubeId(), tube)
					&& app.isBlockUpdateScripts() == scriptsBlocked
					&& (!sameInstances || app.getMode() == tool
							&& selection.equals(app.getSelectionManager().getSelectedGeos()))
					&& construction.getGeoSetWithCasCellsConstructionOrder().size() == layers.size()
					&& construction.getGeoSetWithCasCellsConstructionOrder().stream().allMatch(geo -> {
						EffectiveLayer layer = layers.get(geo.getLabelSimple());
						return layer != null && geo.getLayer() == layer.layer();
					});
		}
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
		private final ExpertRecoveryState expertBefore;
		private final Set<String> macrosBefore = new java.util.LinkedHashSet<>();
		private final List<UndoCommand> ownStores = new ArrayList<>();
		private List<GeoElement> created = List.of();
		private boolean attached;
		/** set when the serious path handed the history and snapshot to the isolation */
		private boolean handedOver;
		private Throwable failure;

		Transaction(String name, GeoCeDGUndoManager undo, Barrier barrier,
				GeoCeDGInsertionAdmission admission, Certificate before, String uuid, String tube,
				boolean saved, ExpertRecoveryState expertBefore) {
			this.name = name;
			this.undo = undo;
			this.barrier = barrier;
			this.admission = admission;
			this.before = before;
			this.uuid = uuid;
			this.tube = tube;
			this.saved = saved;
			this.expertBefore = expertBefore;
		}

		Result runExpert(AppGeoCeDG helper, ExpertOffer offer) {
			Set<GeoElement> instancesBefore = identitySet(app);
			macrosBefore.addAll(macroNames(app));
			try {
				var trial = GeoCeDGExpertIntegrity.copy(helper, app);
				created = createdSince(app, instancesBefore);
				faults.afterCommit();
				restoreIdentity();
				if (!awaitQuiescence(undo) || !undo.getCommands().equals(List.of(barrier.baseline))) {
					throw new IllegalStateException("Unexpected history change in expert publication");
				}
				verifyExpertFingerprint(offer, trial.fingerprint(), "native copy");
				return succeed(helper.getDocumentUnits().getState(), offer);
			} catch (Exception | Error commitFailure) {
				failure = commitFailure;
				try {
					created = createdSince(app, instancesBefore);
				} catch (RuntimeException | Error observationFailure) {
					addSuppressed(observationFailure);
					return serious(null);
				}
				return recover(isRollbackFailure(commitFailure));
			}
		}

		private void verifyExpertFingerprint(ExpertOffer offer, String actual, String stage) {
			if (!offer.fingerprint.equals(actual)) {
				throw new IllegalStateException("Expert publication differs from the consented trial at "
						+ stage + " (expected " + offer.fingerprint + ", observed " + actual + ")");
			}
		}

		private void verifyExpertLiveState(ExpertOffer offer, String stage) throws IOException {
			if (!Objects.equals(app.getCurrentFile(), before.file)
					|| !Objects.equals(app.getTubeId(), expertBefore.tube())
					|| app.isBlockUpdateScripts() != expertBefore.scriptsBlocked()) {
				throw new IllegalStateException("Expert publication changed file association or scripting policy");
			}
			verifyExpertFingerprint(offer, GeoCeDGExpertIntegrity.fingerprint(app, expertBefore.resources()),
					stage);
		}

		Result run(AppGeoCeDG helper, MixedAngleInsertionProof proof, ReceiverGeometry geometry) {
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
							idsBefore, buffer.getCopies());
					if (mismatch == null) {
						mismatch = angularCopyMismatch(proof, geometry, helper, app, buffer);
					}
					if (mismatch == null) {
						mismatch = verifyMacros(admission, app, helper);
					}
					if (mismatch != null) {
						throw new IllegalStateException("verification failed: " + mismatch);
					}
					return succeed(buffer.getProvenance(), null);
				}
			} catch (Exception | Error verificationFailure) {
				failure = verificationFailure;
			}
			return recover(rollbackFailed);
		}

		// ---------------------------------------------------- 4 SUCCESS

		private Result succeed(UnitState provenance, ExpertOffer offer) throws Exception {
			faults.beforeOwnStore();
			if (offer != null) {
				verifyExpertLiveState(offer, "pre-history state");
			} else {
				requireUnchangedControlLayout();
			}
			// one undo step made by the orchestration, carrying the kept document identity
			String live = currentUndoXml();
			app.getKernel().getConstruction().getUndoManager().storeUndoInfo();
			if (!awaitQuiescence(undo)) {
				throw new IllegalStateException("the insertion's undo store did not complete");
			}
			List<UndoCommand> temporary = undo.getCommands();
			UndoCommand own = temporary.get(temporary.size() - 1);
			if (temporary.size() != ownStores.size() + 2 || !live.equals(transactionUndoXml(own))) {
				throw new IllegalStateException("the insertion's undo store was not identified");
			}
			ownStores.add(own);
			List<UndoCommand> finalHistory = new ArrayList<>(barrier.held.getPrefix());
			if (expertBefore != null && !topMatchesLiveBaseline(finalHistory)) {
				// Preserve unsaved live edits that had not received their own checkpoint.
				finalHistory.add(barrier.baseline);
			}
			finalHistory.add(own);
			List<UndoCommand> discarded = new ArrayList<>(barrier.held.getRedo());
			if (expertBefore != null) {
				app.setUnsaved();
			}
			attachBlock(temporary, finalHistory, finalHistory.size(), discarded, offer, true);
			if (expertBefore != null) {
				barrier.snapshot.release();
				try {
					app.unitPasteCompleted(provenance, app);
				} catch (RuntimeException noticeFailure) {
					Log.debug(noticeFailure);
				}
				return new Result(Outcome.INSERTED, null, List.of(), null, own);
			}
			app.setUnsaved();
			app.unitPasteCompleted(provenance, app);
			barrier.snapshot.release();
			return new Result(Outcome.INSERTED, null, List.of(), null);
		}

		private void requireUnchangedControlLayout() {
			if (!before.controlLayout.sameOwners(ReceiverControlLayout.capture(app))) {
				throw new IllegalStateException(text("Insert.Reject.ControlLayoutMismatch"));
			}
		}

		private boolean topMatchesLiveBaseline(List<UndoCommand> prefix) throws IOException {
			if (prefix.isEmpty() || !(prefix.get(prefix.size() - 1).getAppState() instanceof FileAppState)) {
				return false;
			}
			return transactionUndoXml(barrier.baseline)
					.equals(transactionUndoXml(prefix.get(prefix.size() - 1)));
		}

		private String transactionUndoXml(UndoCommand command) throws IOException {
			String xml = undoXml(command);
			return expertBefore == null ? xml : undo.publicUndoXml(command, xml);
		}

		// ---------------------------------------------------- 5-7 RECOVERY

		private Result recover(boolean rollbackFailed) {
			String reason = text(failure instanceof ExpertSourceChanged
					? "Insert.Expert.SourceChanged" : "Insert.Failure.Commit");
			if (rollbackFailed) {
				// DQ21-Q5: the inherited rollback itself failed
				return serious(null);
			}
			if (expertBefore != null || !admission.isIdentityBearing()) {
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
				if (expertBefore != null) {
					expertBefore.restore(app);
					expertBefore.requireResources(app);
				}
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
			if (expertBefore != null) {
				expertBefore.restore(app);
				expertBefore.requireResources(app);
			}
			if (!currentUndoXml().equals(transactionUndoXml(barrier.baseline))) {
				throw new IllegalStateException("subtractive compensation left a difference");
			}
		}

		/** Reload of the barrier baseline only: no history access, no inherited fallback. */
		private void guardedReload() throws Exception {
			File source = ((FileAppState) barrier.baseline.getAppState()).getFile();
			byte[] publicBaseline = null;
			if (expertBefore != null) {
				ByteArrayOutputStream archive = new ByteArrayOutputStream();
				MyXMLioJre.writeZipped(archive, new StringBuilder(transactionUndoXml(barrier.baseline)));
				publicBaseline = archive.toByteArray();
			}
			Exception last = null;
			for (int attempt = 1; attempt <= 2; attempt++) {
				try {
					faults.beforeReload(attempt);
					synchronized (undo) {
						app.setActiveView(App.VIEW_EUCLIDIAN);
						app.getEventDispatcher().disableListeners();
						try (InputStream in = publicBaseline == null ? new FileInputStream(source)
								: new ByteArrayInputStream(publicBaseline)) {
							((MyXMLioJre) app.getXMLio()).readZipFromMemory(in);
							if (expertBefore != null && app.getXMLio().hasErrors()) {
								throw new IOException("Native recovery reported reader errors");
							}
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

		private String certify(Certificate after) throws IOException {
			List<String> differences = before.serializableDifferences(after);
			if (expertBefore != null) {
				expertBefore.requireResources(app);
				if (!expertBefore.matches(app, before.sameInstances(after))) {
					differences.add("expert recovery session state");
				}
			}
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
			if (expertBefore != null) {
				Log.error("Expert insertion recovery could not be certified");
				if (failure != null) {
					Log.error(failure);
				}
			}
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
			attachBlock(expectedTemporary, finalHistory, cursor, discarded, null, false);
		}

		private void attachBlock(List<UndoCommand> expectedTemporary,
				List<UndoCommand> finalHistory, int cursor, List<UndoCommand> discarded,
				ExpertOffer offer, boolean publishing) throws Exception {
			faults.beforeAttach();
			synchronized (undo) {
				List<UndoCommand> temporary = undo.getCommands();
				if (!undo.isQuiescent() || !temporary.equals(expectedTemporary)) {
					try (PreparedUndoBaseline closing = undo.prepareUndoBaseline()) {
						undo.commitUndoBaseline(closing);
					}
					temporary.addAll(undo.getCommands());
				}
				if (offer != null) {
					verifyExpertLiveState(offer, "history attachment");
					if (!offer.exactSourceCurrent()) {
						throw new ExpertSourceChanged();
					}
				} else if (publishing) {
					requireUnchangedControlLayout();
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

	private static final class ExpertSourceChanged extends IOException {
		private static final long serialVersionUID = 1L;

		private ExpertSourceChanged() {
			super("The consented source changed before history attachment");
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
		return verify(source, created, target, helper, idsBefore, Map.of());
	}

	private static String verify(List<GeoElement> source, List<GeoElement> created,
			AppGeoCeDG target, AppGeoCeDG helper, Map<GeoElement, PersistentGeoId> idsBefore,
			Map<GeoElement, GeoElement> copies) {
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
			if (fromAlgo == null && from instanceof GeoAngle angle) {
				String mismatch = copies.get(from) == to
						? GeoCeDGInsertionFidelity.angleMismatch(angle, (GeoAngle) to, copies)
						: "copy correspondence";
				if (mismatch != null) {
					return "angle slider " + label + " [" + mismatch + "]";
				}
			}
			if (fromAlgo == null && from instanceof GeoText literal
					&& (copies.get(from) != to
							|| !GeoCeDGInsertionFidelity.sameText(literal, (GeoText) to, copies))) {
				return "static text " + label;
			}
			if (toAlgo != null && !closed(fromAlgo, toAlgo, inserted, helper, target, copies)) {
				return "closure " + label;
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
	 * Closure of one inserted algorithm: every labeled input is an inserted object, or
	 * (PRE-G9B-R6-plus-F3-R1, author smoke SF-F3-1) the same built-in coordinate axis of
	 * the target that the source algorithm used at that position. The axes are
	 * construction constants of every document, never objects of the target, so a
	 * dependency on them is internal; any other labeled outside input is a closure
	 * failure, as before.
	 */
	private static boolean closed(AlgoElement fromAlgo, AlgoElement toAlgo,
			Set<GeoElement> inserted, AppGeoCeDG helper, AppGeoCeDG target,
			Map<GeoElement, GeoElement> copies) {
		GeoElement[] toInputs = toAlgo.getInput();
		GeoElement[] fromInputs = fromAlgo.getInput();
		if (toInputs.length != fromInputs.length) {
			return false;
		}
		Construction from = helper.getKernel().getConstruction();
		Construction to = target.getKernel().getConstruction();
		for (int k = 0; k < toInputs.length; k++) {
			GeoElement input = toInputs[k];
			GeoElement original = fromInputs[k];
			if (original.isIndependent() && (original instanceof GeoAngle
					|| original instanceof GeoText) && copies.containsKey(original)
					&& copies.get(original) != input) {
				return false;
			}
			if (!input.isLabelSet() || inserted.contains(input)) {
				continue;
			}
			boolean xAxis = input == to.getXAxis() && fromInputs[k] == from.getXAxis();
			boolean yAxis = input == to.getYAxis() && fromInputs[k] == from.getYAxis();
			if (!xAxis && !yAxis) {
				return false;
			}
		}
		return true;
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
				if (undo.getCursor() != cursor || held != null && held.getCursor() != cursor) {
					return "cursor";
				}
				for (int i = 0; i < live.size(); i++) {
					UndoCommand command = live.get(i);
					if (command != commands.get(i)
							|| held != null && command != held.getCommands().get(i)) {
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

	private record LayoutSlot(String panelId, String id, Class<?> family, double dx, double dy) {
	}

	/** Compare the presentation records independently of the native construction addresses. */
	private record ReceiverControlLayout(List<Panel> panels, Map<LayoutSlot, GeoElement> owners) {
		static ReceiverControlLayout capture(AppGeoCeDG app) {
			Construction construction = app.getKernel().getConstruction();
			var layout = construction.getControlLayout();
			List<Panel> panels = layout.panels();
			Map<LayoutSlot, GeoElement> owners = new HashMap<>();
			for (Panel panel : panels) {
				for (var slot : layout.slots(panel.id())) {
					GeoElement control = slot.control();
					if (control.getConstruction() != construction
							|| !construction.isInConstructionList(control)
							|| layout.slot(control) != slot) {
						throw new IllegalStateException("Receiver control slot has no live owner");
					}
					owners.put(new LayoutSlot(slot.panelId(), slot.id(), control.getClass(),
							slot.dx(), slot.dy()), control);
				}
			}
			return new ReceiverControlLayout(panels, Map.copyOf(owners));
		}

		boolean samePresentation(ReceiverControlLayout other) {
			return panels.equals(other.panels) && owners.keySet().equals(other.owners.keySet());
		}

		boolean sameOwners(ReceiverControlLayout other) {
			return samePresentation(other) && owners.entrySet().stream()
					.allMatch(entry -> other.owners.get(entry.getKey()) == entry.getValue());
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
		final ReceiverControlLayout controlLayout;

		private Certificate(AppGeoCeDG app) {
			xml = normalize(app.getXML());
			controlLayout = ReceiverControlLayout.capture(app);
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
			if (!controlLayout.samePresentation(other.controlLayout)) {
				differences.add("control layout");
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
					&& linkedSheet == other.linkedSheet && controlLayout.sameOwners(other.controlLayout);
		}
	}
}
