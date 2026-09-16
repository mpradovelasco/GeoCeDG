/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geocedg.desktop.GeoCeDGPresentationPreferences.Store;
import org.geocedg.desktop.GeoCeDGPresentationPreferences.ValueSource;

/**
 * Independent user preference holding the selected application presentation theme.
 *
 * <p>The value is stored under one versioned key in the same user-preference store used by
 * the bounded presentation sizes. It never reaches the construction, the document XML or
 * the preferences XML.
 */
final class GeoCeDGThemePreference {

	static final String PREFERENCE_KEY = "geocedg.presentation.theme.v1";

	private final Store store;
	private GeoCeDGPresentationTheme theme;
	private ValueSource source;

	GeoCeDGThemePreference() {
		this(GeoCeDGPresentationPreferences.systemStore());
	}

	GeoCeDGThemePreference(Store store) {
		this.store = store;
		String stored = store.load(PREFERENCE_KEY);
		GeoCeDGPresentationTheme resolved = GeoCeDGPresentationTheme.fromId(stored);
		if (resolved == null) {
			// Absent or unreadable identity: historical profiles keep the original look.
			this.theme = GeoCeDGPresentationTheme.DEFAULT_THEME;
			this.source = ValueSource.FRESH_DEFAULT;
			store.save(PREFERENCE_KEY, this.theme.id());
		} else {
			this.theme = resolved;
			this.source = ValueSource.EXPLICIT_PERSISTED;
		}
	}

	GeoCeDGPresentationTheme get() {
		return theme;
	}

	ValueSource source() {
		return source;
	}

	void set(GeoCeDGPresentationTheme selected) {
		if (selected == null) {
			throw new IllegalArgumentException("Unsupported presentation theme: null");
		}
		store.save(PREFERENCE_KEY, selected.id());
		theme = selected;
		source = ValueSource.EXPLICIT_PERSISTED;
	}
}
