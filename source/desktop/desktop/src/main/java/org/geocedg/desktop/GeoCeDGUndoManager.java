/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.geocedg.common.kernel.locus.NativeTransactionalState2D;
import org.geocedg.common.kernel.locus.NativeTransactionalState2D.UndoTemplate;
import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.main.undo.AppState;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.main.undo.UndoHistory;
import org.geogebra.desktop.main.undo.FileAppState;
import org.geogebra.desktop.main.undo.UndoManagerD;

/**
 * PRE-G9B-R6-plus-F3 undo manager of every GeoCeDG document (recovery design addendum
 * successor sections 1 and 4), with the CU7 R3 focal native Undo companion.
 * Inherited scheduling and history transfer remain in place. Empty/unsupported
 * native captures keep the historical file state. The additions are:
 *
 * <ul>
 * <li>conservative pending-store accounting C1: {@code pending == 0 && !degraded}
 * implies that no asynchronous store of the current history generation can still
 * mutate the history. Over-counting only refuses an insertion; under-counting never
 * happens because a decrement requires the store body's own success path;</li>
 * <li>detaching and re-attaching the original command objects with their cursor, so
 * that labels, grouping, Redo states and snapshot files are moved, never copied (the
 * inherited {@code undoHistoryTo} copy drops labels and grouping, ER-F3R-3);</li>
 * <li>an owned native file state carries immutable inputs alongside its private
 * XML. Its normal reader uses a fresh typed session, with exact atomic failure
 * recovery and the original history cursor.</li>
 * </ul>
 *
 * <p>The constructor may run inside the host application constructor (the factory is
 * stateless), so this class has no instance field initializer.
 */
class GeoCeDGUndoManager extends UndoManagerD {
	/** Synchronous callers of the inherited notification (re-entrancy depth). */
	private static final ThreadLocal<int[]> SYNCHRONOUS = ThreadLocal.withInitial(() -> new int[1]);
	/** Set on a store thread once its body has added its command and pruned. */
	private static final ThreadLocal<Boolean> STORE_SUCCEEDED = new ThreadLocal<>();

	private int pending;
	private boolean degraded;
	private long epoch;
	private long historyActivity;
	// Lazily initialized: this manager may be called during application bootstrap.
	private Map<StringBuilder, UndoTemplate> pendingNativeStates;

	/**
	 * @param cons construction whose history this manager owns
	 */
	GeoCeDGUndoManager(Construction cons) {
		super(cons);
	}

	@Override
	protected synchronized StringBuilder captureUndoXML() {
		var captured = construction.captureNativeUndoSnapshot();
		if (captured == null) { return super.captureUndoXML(); }
		if (pendingNativeStates == null) { pendingNativeStates = new IdentityHashMap<>(); }
		pendingNativeStates.put(captured.xml(), captured.template());
		return captured.xml();
	}

	@Override
	protected synchronized FileAppState createUndoState(StringBuilder xml) throws IOException {
		UndoTemplate template = pendingNativeStates == null ? null : pendingNativeStates.remove(xml);
		if (template == null) {
			if (xml.indexOf("<" + NativeTransactionalState2D.BINDING_TAG) >= 0) {
				throw new IOException("Private native Undo XML has no pending owned companion");
			}
			return super.createUndoState(xml);
		}
		if (!template.matchesXml(xml.toString().getBytes(StandardCharsets.UTF_8))) {
			throw new IOException("Native Undo XML changed after caller capture");
		}
		return new NativeFileAppState(this, construction, xml, template);
	}

	/** File and immutable native companion move and are disposed as one original AppState. */
	static final class NativeFileAppState extends FileAppState {
		private final GeoCeDGUndoManager owner;
		private final Construction document;
		private volatile UndoTemplate template;

		private NativeFileAppState(GeoCeDGUndoManager manager, Construction construction,
				StringBuilder xml, UndoTemplate captured) throws IOException {
			super(xml);
			owner = manager; document = construction; template = captured;
		}

		private UndoTemplate requireTemplate(GeoCeDGUndoManager manager, Construction construction) {
			UndoTemplate current = template;
			if (owner != manager || document != construction || current == null
					|| !current.belongsTo(construction)) {
				throw new UndoStateLoadFailure("Foreign or disposed native Undo state", null);
			}
			return current;
		}

		@Override public void delete() {
			try { super.delete(); }
			finally { template = null; }
		}
	}

	/** Removes only authenticated private Undo bindings from an expert comparison. */
	String publicUndoXml(UndoCommand command, String xml) {
		if (command.getAppState() instanceof NativeFileAppState nativeState) {
			UndoTemplate template = nativeState.requireTemplate(this, construction);
			return new String(template.publicXml(xml.getBytes(StandardCharsets.UTF_8)),
					StandardCharsets.UTF_8);
		}
		return xml;
	}

	@Override
	protected void readUndoState(FileAppState state, InputStream input)
			throws IOException, XMLParseException {
		if (!(state instanceof NativeFileAppState nativeState)) {
			super.readUndoState(state, input);
			return;
		}
		boolean wasFileLoading = construction.isFileLoading();
		try {
			UndoTemplate template = nativeState.requireTemplate(this, construction);
			byte[] archive = input.readAllBytes();
			byte[] xml;
			try (var zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
				var entry = zip.getNextEntry();
				if (entry == null || !MyXMLio.XML_FILE.equals(entry.getName())) {
					throw new UndoStateLoadFailure("Native Undo snapshot has no construction entry", null);
				}
				xml = zip.readAllBytes();
				if (!template.matchesXml(xml) || zip.getNextEntry() != null) {
					throw new UndoStateLoadFailure("Native Undo snapshot bytes do not match their companion", null);
				}
			}
			var currentReason = NativeTransactionalState2D.unsupportedReason(construction);
			// GeoCeDG CU7 R4: exhausted work is not an unsupported-history permission.
			NativeTransactionalState2D.requireAvailable(currentReason);
			if (currentReason != null) {
				// No exact pre-state exists for this legacy history transition. Project
				// only this authenticated snapshot's private bindings, validating the
				// original public digest; never grant a new CU7 editing permission.
				byte[] publicXml = template.publicXml(xml);
				var historical = new ByteArrayOutputStream();
				MyXMLioJre.writeZipped(historical,
						new StringBuilder(new String(publicXml, StandardCharsets.UTF_8)));
				super.readUndoState(state, new ByteArrayInputStream(historical.toByteArray()));
				return;
			}
			construction.restoreNativeUndoSnapshot(template, () -> {
				try {
					super.readUndoState(state, new ByteArrayInputStream(archive));
				} catch (IOException | XMLParseException | RuntimeException | Error failure) {
					throw new UndoStateLoadFailure("Native Undo reader failed", failure);
				}
			});
		} catch (IOException | RuntimeException | Error failure) {
			throw failure instanceof UndoStateLoadFailure ? (UndoStateLoadFailure) failure
					: new UndoStateLoadFailure("Native Undo restoration failed", failure);
		} finally {
			construction.setFileLoading(wasFileLoading);
			app.getEventDispatcher().enableListeners();
		}
	}

	@Override
	protected void handleUndoLoadFailure(AppState state, Throwable failure) {
		if (state instanceof NativeFileAppState) {
			app.getEventDispatcher().enableListeners();
			throw failure instanceof UndoStateLoadFailure ? (UndoStateLoadFailure) failure
					: new UndoStateLoadFailure("Owned native Undo state could not be read", failure);
		}
		super.handleUndoLoadFailure(state, failure);
	}

	@Override
	public synchronized void undo() {
		historyActivity++;
		runOwnedHistoryOperation(super::undo);
	}

	@Override
	public synchronized void redo() {
		historyActivity++;
		runOwnedHistoryOperation(super::redo);
	}

	private void runOwnedHistoryOperation(Runnable operation) {
		boolean nativeHistory = undoInfoList.stream()
				.anyMatch(command -> command.getAppState() instanceof NativeFileAppState);
		if (!nativeHistory) { operation.run(); return; }
		var currentReason = NativeTransactionalState2D.unsupportedReason(construction);
		NativeTransactionalState2D.requireAvailable(currentReason);
		boolean exactCurrent = currentReason == null;
		List<UndoCommand> originalCommands = new ArrayList<>(undoInfoList);
		int originalCursor = getCursor();
		try {
			if (!exactCurrent) {
				// Legacy construction fidelity only. Still restore the history cursor
				// when an owned target fails owner/digest validation before mutation.
				operation.run();
				return;
			}
			construction.runNativeUndoOperation(() -> {
				try { operation.run(); }
				catch (RuntimeException | Error failure) {
					// The inherited reader re-enables listeners on exit. Recovery of
					// the enclosing exact pre-state must keep replay callbacks disabled.
					app.getEventDispatcher().disableListeners();
					throw failure;
				}
			});
		} catch (RuntimeException | Error failure) {
			try {
				if (undoInfoList.equals(originalCommands)) { restoreUndoCursor(originalCursor); }
				else {
					RuntimeException refresh = attach(originalCommands, originalCursor);
					if (refresh != null) { failure.addSuppressed(refresh); }
				}
			} catch (RuntimeException | Error cursorFailure) { failure.addSuppressed(cursorFailure); }
			throw failure;
		} finally {
			app.getEventDispatcher().enableListeners();
		}
	}

	/** Registration: counted before the inherited manager schedules the store. */
	@Override
	public void storeUndoInfo(StringBuilder currentUndoXML) {
		synchronized (this) {
			historyActivity++;
			pending++;
		}
		try {
			super.storeUndoInfo(currentUndoXML);
		} catch (RuntimeException | Error schedulingFailure) {
			// e.g. thread creation failed: the store may or may not exist
			synchronized (this) {
				degraded = true;
				if (pendingNativeStates != null) { pendingNativeStates.remove(currentUndoXML); }
			}
			throw schedulingFailure;
		}
	}

	@Override
	protected void storeAndNotifyUnsaved(UndoCommand command) {
		synchronized (this) {
			historyActivity++;
		}
		enterSynchronous();
		try {
			super.storeAndNotifyUnsaved(command);
		} finally {
			exitSynchronous();
		}
	}

	/**
	 * A committed baseline advances the inherited history generation: every earlier
	 * store is then discarded by the store body before it can touch the history, so the
	 * accounting is re-established here and nowhere else.
	 */
	@Override
	public synchronized void commitUndoBaseline(PreparedUndoBaseline baseline) {
		enterSynchronous();
		try {
			super.commitUndoBaseline(baseline);
		} finally {
			exitSynchronous();
		}
		pending = 0;
		degraded = false;
		epoch++;
		historyActivity++;
	}

	/**
	 * Only the store body's success path calls this method outside a synchronous
	 * caller; it runs after the command was added. The flag is set after the inherited
	 * method returned, so a failure inside it never counts as success.
	 */
	@Override
	public void pruneStateList() {
		super.pruneStateList();
		if (!isSynchronousCaller()) {
			STORE_SUCCEEDED.set(Boolean.TRUE);
		}
	}

	@Override
	protected void onStoreUndo() {
		synchronized (this) {
			if (!isSynchronousCaller()) {
				if (Boolean.TRUE.equals(STORE_SUCCEEDED.get())) {
					if (pending == 0) {
						degraded = true;
					} else {
						pending--;
					}
				} else {
					// an exception exit of a current or stale store: never a decrement
					degraded = true;
				}
			}
			STORE_SUCCEEDED.remove();
		}
		super.onStoreUndo();
	}

	private static void enterSynchronous() {
		SYNCHRONOUS.get()[0]++;
		STORE_SUCCEEDED.remove();
	}

	private static void exitSynchronous() {
		SYNCHRONOUS.get()[0]--;
	}

	private static boolean isSynchronousCaller() {
		return SYNCHRONOUS.get()[0] > 0;
	}

	/** @return whether no relevant asynchronous store can still change the history */
	synchronized boolean isQuiescent() {
		return pending == 0 && !degraded;
	}

	/** @return stores registered and not yet observed on their success path */
	synchronized int getPendingStores() {
		return pending;
	}

	/** @return whether the accounting became uncertain since the last baseline */
	synchronized boolean isDegraded() {
		return degraded;
	}

	/** @return number of committed baselines (accounting re-establishments) */
	synchronized long getEpoch() {
		return epoch;
	}

	/** @return monotonic history activity, including discarded stores and reverts */
	synchronized long getHistoryActivity() {
		return historyActivity;
	}

	/** @return the live command objects in history order (a new list) */
	synchronized List<UndoCommand> getCommands() {
		return new ArrayList<>(undoInfoList);
	}

	/** @return the iterator position: the number of states up to the current one */
	synchronized int getCursor() {
		return getHistorySize() + 1;
	}

	/**
	 * Takes the whole history aside: the original command objects and the cursor. The
	 * live list becomes empty; nothing is deleted.
	 *
	 * @return the held history
	 */
	synchronized HeldHistory detach() {
		HeldHistory held = new HeldHistory(new ArrayList<>(undoInfoList), getCursor());
		clearUndoInfo();
		return held;
	}

	/**
	 * Installs the given original commands and cursor in place of the live list. The
	 * caller owns (and deletes) whatever the live list held. The history is installed
	 * first; the inherited refresh of the Undo and Redo actions that follows is best
	 * effort.
	 *
	 * @param commands original command objects in history order
	 * @param cursor iterator position
	 * @return the refresh failure, or null
	 */
	synchronized RuntimeException attach(List<UndoCommand> commands, int cursor) {
		if (cursor < 0 || cursor > commands.size()) {
			throw new IllegalArgumentException("cursor " + cursor + " outside 0.."
					+ commands.size());
		}
		clearUndoInfo();
		Map<String, UndoHistory> installed = new HashMap<>();
		installed.put(app.getConfig().getSubAppCode(), new UndoHistory(
				new ArrayList<>(commands), cursor));
		try {
			undoHistoryFrom(installed);
			return null;
		} catch (RuntimeException refreshFailure) {
			if (!undoInfoList.equals(commands) || getCursor() != cursor) {
				throw refreshFailure;
			}
			return refreshFailure;
		}
	}

	/** A detached history: the original commands, never copies, and the cursor. */
	static final class HeldHistory {
		private final List<UndoCommand> commands;
		private final int cursor;

		HeldHistory(List<UndoCommand> commands, int cursor) {
			this.commands = Collections.unmodifiableList(commands);
			this.cursor = cursor;
		}

		/** @return original command objects in history order */
		List<UndoCommand> getCommands() {
			return commands;
		}

		/** @return iterator position */
		int getCursor() {
			return cursor;
		}

		/** @return commands up to the cursor (Undo states) */
		List<UndoCommand> getPrefix() {
			return commands.subList(0, cursor);
		}

		/** @return commands after the cursor (Redo states) */
		List<UndoCommand> getRedo() {
			return commands.subList(cursor, commands.size());
		}
	}
}
