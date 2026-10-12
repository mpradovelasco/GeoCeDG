/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.main.undo.UndoCommand;
import org.geogebra.common.main.undo.UndoHistory;
import org.geogebra.desktop.main.undo.UndoManagerD;

/**
 * PRE-G9B-R6-plus-F3 undo manager of every GeoCeDG document (recovery design addendum
 * successor sections 1 and 4). The inherited manager keeps scheduling, storing,
 * undoing and redoing exactly as before: each override only observes and delegates
 * unchanged. Two additions serve Insert File only:
 *
 * <ul>
 * <li>conservative pending-store accounting C1: {@code pending == 0 && !degraded}
 * implies that no asynchronous store of the current history generation can still
 * mutate the history. Over-counting only refuses an insertion; under-counting never
 * happens because a decrement requires the store body's own success path;</li>
 * <li>detaching and re-attaching the original command objects with their cursor, so
 * that labels, grouping, Redo states and snapshot files are moved, never copied (the
 * inherited {@code undoHistoryTo} copy drops labels and grouping, ER-F3R-3).</li>
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

	/**
	 * @param cons construction whose history this manager owns
	 */
	GeoCeDGUndoManager(Construction cons) {
		super(cons);
	}

	/** Registration: counted before the inherited manager schedules the store. */
	@Override
	public void storeUndoInfo(StringBuilder currentUndoXML) {
		synchronized (this) {
			pending++;
		}
		try {
			super.storeUndoInfo(currentUndoXML);
		} catch (RuntimeException | Error schedulingFailure) {
			// e.g. thread creation failed: the store may or may not exist
			synchronized (this) {
				degraded = true;
			}
			throw schedulingFailure;
		}
	}

	@Override
	protected void storeAndNotifyUnsaved(UndoCommand command) {
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
