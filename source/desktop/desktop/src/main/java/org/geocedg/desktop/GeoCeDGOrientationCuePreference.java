/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

/**
 * PRE-G9B-R6-plus-F1 global user preference of the orientation cue (DQ-F1-6): one
 * portable GeoCeDG properties key, default OFF, never part of the preferences XML,
 * a document, a macro or an undo step. Only the exact value {@code shown} turns the
 * cue on; reading never writes, so an invalid stored value behaves as OFF.
 */
final class GeoCeDGOrientationCuePreference {
	static final String KEY = "geocedg.view.orientation-cue.v1";
	static final String SHOWN = "shown";
	static final String HIDDEN = "hidden";

	private static GeoCeDGPresentationPreferences.Store storeOverride;

	private final GeoCeDGPresentationPreferences.Store store;
	private boolean shown;

	GeoCeDGOrientationCuePreference() {
		this(storeOverride != null ? storeOverride : GeoCeDGPresentationPreferences.systemStore());
	}

	GeoCeDGOrientationCuePreference(GeoCeDGPresentationPreferences.Store store) {
		this.store = store;
		reload();
	}

	/**
	 * Test seam: every instance created by the no-argument constructor uses the given
	 * store until the returned handle is closed, so tests never touch the real store.
	 */
	static AutoCloseable useStoreForTesting(GeoCeDGPresentationPreferences.Store store) {
		GeoCeDGPresentationPreferences.Store previous = storeOverride;
		storeOverride = store;
		return () -> storeOverride = previous;
	}

	/** @return whether the cue is shown */
	boolean isShown() {
		return shown;
	}

	/** @param value whether the cue is shown; written to the store at once */
	void setShown(boolean value) {
		shown = value;
		store.save(KEY, value ? SHOWN : HIDDEN);
	}

	/** Re-reads the store, for a change made by another window of the process. */
	void reload() {
		shown = SHOWN.equals(store.load(KEY));
	}
}
