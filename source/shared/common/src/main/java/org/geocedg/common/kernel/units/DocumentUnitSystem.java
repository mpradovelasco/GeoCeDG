/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.units;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.geogebra.common.util.debug.Log;

/**
 * The one shared unit-state owner of a construction (PRE-G9B-R6-plus-D1; ADR 0032).
 * {@code Construction} holds exactly one instance. It is reset by
 * {@code clearConstruction} and applied only by clearing loads of the document
 * construction; the construction XML, and therefore every undo, redefine and rollback
 * snapshot, carries it. Nothing in the kernel depends on it: listeners are presentation
 * consumers and never trigger a construction recompute.
 */
public final class DocumentUnitSystem {
	private final boolean macroConstruction;
	private final List<Runnable> listeners = new ArrayList<>();
	private final Set<String> keyedListeners = new HashSet<>();
	private UnitState state = UnitState.EMPTY;

	/**
	 * @param macroConstruction whether the owner is a macro construction, whose state
	 *        stays {@code EMPTY}
	 */
	public DocumentUnitSystem(boolean macroConstruction) {
		this.macroConstruction = macroConstruction;
	}

	/**
	 * @return current stored state
	 */
	public UnitState getState() {
		return state;
	}

	/**
	 * @return whether the state holds unit metadata, so a document with it has
	 *         save-relevant content (DQ-D1-5); the dimension unit suffix policy of
	 *         unit-system v1.1 is presentation and, alone, never makes a document
	 *         save-relevant, although a saved document always carries it
	 */
	public boolean hasPersistentMetadata() {
		return state.hasUnitMetadata();
	}

	/**
	 * @param listener presentation listener, called after every change
	 */
	public void addListener(Runnable listener) {
		listeners.add(Objects.requireNonNull(listener));
	}

	/**
	 * Registers a presentation listener at most once per key, for consumers that serve a
	 * whole construction (PRE-G9B-R6-plus-E2 native dimension texts).
	 *
	 * @param key stable consumer key
	 * @param listener presentation listener, called after every change
	 */
	public void addListenerOnce(String key, Runnable listener) {
		if (keyedListeners.add(Objects.requireNonNull(key))) {
			addListener(listener);
		}
	}

	/**
	 * @param listener a registered listener
	 */
	public void removeListener(Runnable listener) {
		listeners.remove(listener);
	}

	/** Resets the state to {@code EMPTY}; called by {@code clearConstruction} only. */
	public void reset() {
		set(UnitState.EMPTY);
	}

	/**
	 * Applies the state read from a clearing load of the document construction.
	 *
	 * @param loaded validated state
	 */
	public void applyLoaded(UnitState loaded) {
		if (macroConstruction) {
			throw new IllegalStateException("A macro construction has no unit state");
		}
		set(Objects.requireNonNull(loaded));
	}

	/**
	 * Replaces the state without an undo point (document defaults, rollback of a
	 * failed edit); document operations go through {@link UnitDocumentOperations}.
	 *
	 * @param next validated state
	 * @return whether the state changed
	 */
	public boolean replace(UnitState next) {
		if (macroConstruction) {
			throw new IllegalStateException("A macro construction has no unit state");
		}
		return set(Objects.requireNonNull(next));
	}

	private boolean set(UnitState next) {
		if (state.equals(next)) {
			return false;
		}
		state = next;
		for (Runnable listener : new ArrayList<>(listeners)) {
			try {
				listener.run();
			} catch (RuntimeException failure) {
				// Presentation only: a listener can never change or block the state.
				Log.debug("unit-state listener failed: " + failure.getMessage());
			}
		}
		return true;
	}
}
