/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Color;

import org.geogebra.common.awt.GColor;

/**
 * Closed set of GeoCeDG application presentation themes.
 *
 * <p>A theme is an application presentation preference. It never becomes geometry, never
 * enters the construction dependency graph, never changes object identity or style, and is
 * never serialized into a document. The persisted identity is the stable semantic
 * {@link #id()}, never the localized text shown to the user.
 */
enum GeoCeDGPresentationTheme {

	/** Historical GeoCeDG appearance; supplies no palette override at all. */
	ORIGINAL("original", "Presentation.Theme.Original", null),

	/** Warm, very light technical-paper appearance. */
	SCIENTIFIC_PAPER("scientific-paper", "Presentation.Theme.ScientificPaper",
			new Palette(0xE5E2DC, 0xECE9E3, 0xF5F3EE, 0xFFFDF8, 0xC8C4BC)),

	/** Modern technical appearance with a cool bluish cast. */
	COOL_GEOMETRY("cool-geometry", "Presentation.Theme.CoolGeometry",
			new Palette(0xDEE4E9, 0xE7ECF0, 0xEFF3F6, 0xF8FAFC, 0xBBC4CC));

	/** The single palette authority of one theme; all roles are presentation-only. */
	record Palette(int frame, int surface, int panel, int canvas, int border) {

		Color frameColor() {
			return new Color(frame);
		}

		Color surfaceColor() {
			return new Color(surface);
		}

		Color panelColor() {
			return new Color(panel);
		}

		Color borderColor() {
			return new Color(border);
		}

		GColor canvasColor() {
			return GColor.newColorRGB(canvas);
		}
	}

	/** Theme applied when a profile has no stored or no valid theme preference. */
	static final GeoCeDGPresentationTheme DEFAULT_THEME = ORIGINAL;

	private final String id;
	private final String textKey;
	private final Palette palette;

	GeoCeDGPresentationTheme(String id, String textKey, Palette palette) {
		this.id = id;
		this.textKey = textKey;
		this.palette = palette;
	}

	/** @return stable semantic identity used for persistence */
	String id() {
		return id;
	}

	/** @return GeoCeDG profile text key of the localized display name */
	String textKey() {
		return textKey;
	}

	/** @return palette of this theme, or {@code null} for the unmodified original */
	Palette palette() {
		return palette;
	}

	/**
	 * @param id persisted semantic identity
	 * @return matching theme, or {@code null} when the identity is absent or unknown
	 */
	static GeoCeDGPresentationTheme fromId(String id) {
		for (GeoCeDGPresentationTheme theme : values()) {
			if (theme.id.equals(id)) {
				return theme;
			}
		}
		return null;
	}
}
