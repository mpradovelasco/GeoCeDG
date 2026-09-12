/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.geogebra.desktop.main.GeoGebraPreferencesD;

/** Bounded GeoCeDG application preferences for the two factor-zoom actions. */
final class GeoCeDGNavigationShortcutPreferences {

	static final String ZOOM_IN_ACTION_ID = "navigation.zoom-factor-in";
	static final String ZOOM_OUT_ACTION_ID = "navigation.zoom-factor-out";
	static final String FACTOR_PREFERENCE_KEY = "geocedg.navigation.zoom-factor.v1";
	static final String ZOOM_IN_PREFERENCE_KEY =
			"geocedg.navigation.zoom-factor-in.shortcut.v1";
	static final String ZOOM_OUT_PREFERENCE_KEY =
			"geocedg.navigation.zoom-factor-out.shortcut.v1";
	static final double DEFAULT_FACTOR = 10;
	private static final String VERSION = "v1";
	private static final int ALLOWED_MODIFIERS = InputEvent.CTRL_DOWN_MASK
			| InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
			| InputEvent.META_DOWN_MASK;
	private static final Set<Integer> RESERVED_CTRL_KEYS = Set.of(
			KeyEvent.VK_PLUS, KeyEvent.VK_ADD, KeyEvent.VK_MINUS, KeyEvent.VK_SUBTRACT,
			KeyEvent.VK_EQUALS, KeyEvent.VK_Z, KeyEvent.VK_Y, KeyEvent.VK_D,
			KeyEvent.VK_S, KeyEvent.VK_O, KeyEvent.VK_N, KeyEvent.VK_P,
			KeyEvent.VK_Q, KeyEvent.VK_J, KeyEvent.VK_1, KeyEvent.VK_2,
			KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6,
			KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0);

	enum Result {
		ACCEPTED, INVALID_FACTOR, INVALID_SHORTCUT, CONFLICT
	}

	record Configuration(double factor, KeyStroke zoomIn, KeyStroke zoomOut) {
	}

	private final GeoCeDGActionRegistry registry;
	private double factor;
	private KeyStroke zoomIn;
	private KeyStroke zoomOut;

	GeoCeDGNavigationShortcutPreferences(GeoCeDGActionRegistry registry) {
		this.registry = registry;
		factor = decodeFactor(GeoGebraPreferencesD.getPref().loadPreference(
				FACTOR_PREFERENCE_KEY, ""));
		zoomIn = loadBinding(ZOOM_IN_PREFERENCE_KEY);
		zoomOut = loadBinding(ZOOM_OUT_PREFERENCE_KEY);
		if (sameBinding(zoomIn, zoomOut) || conflict(zoomIn) || conflict(zoomOut)) {
			zoomIn = null;
			zoomOut = null;
		}
		applyAccelerators();
	}

	Configuration getConfiguration() {
		return new Configuration(factor, zoomIn, zoomOut);
	}

	double getFactor() {
		return factor;
	}

	Result setConfiguration(double proposedFactor, KeyStroke proposedIn,
			KeyStroke proposedOut) {
		if (!validFactor(proposedFactor)) {
			return Result.INVALID_FACTOR;
		}
		KeyStroke normalizedIn = normalize(proposedIn);
		KeyStroke normalizedOut = normalize(proposedOut);
		if ((proposedIn != null && !valid(normalizedIn))
				|| (proposedOut != null && !valid(normalizedOut))) {
			return Result.INVALID_SHORTCUT;
		}
		if (sameBinding(normalizedIn, normalizedOut)
				|| conflict(normalizedIn) || conflict(normalizedOut)) {
			return Result.CONFLICT;
		}

		// Validate the complete proposal before publishing any preference.
		GeoGebraPreferencesD preferences = GeoGebraPreferencesD.getPref();
		preferences.savePreference(FACTOR_PREFERENCE_KEY,
				Double.toString(proposedFactor));
		preferences.savePreference(ZOOM_IN_PREFERENCE_KEY, encode(normalizedIn));
		preferences.savePreference(ZOOM_OUT_PREFERENCE_KEY, encode(normalizedOut));
		factor = proposedFactor;
		zoomIn = normalizedIn;
		zoomOut = normalizedOut;
		applyAccelerators();
		return Result.ACCEPTED;
	}

	Result reset() {
		return setConfiguration(DEFAULT_FACTOR, null, null);
	}

	private KeyStroke loadBinding(String key) {
		KeyStroke stored = decode(GeoGebraPreferencesD.getPref().loadPreference(key, ""));
		return stored != null && !conflict(stored) ? stored : null;
	}

	private void applyAccelerators() {
		registry.get(ZOOM_IN_ACTION_ID).putValue(Action.ACCELERATOR_KEY, zoomIn);
		registry.get(ZOOM_OUT_ACTION_ID).putValue(Action.ACCELERATOR_KEY, zoomOut);
	}

	private boolean conflict(KeyStroke proposed) {
		if (proposed == null) {
			return false;
		}
		for (String id : registry.ids()) {
			if (!ZOOM_IN_ACTION_ID.equals(id) && !ZOOM_OUT_ACTION_ID.equals(id)
					&& proposed.equals(registry.get(id).getValue(Action.ACCELERATOR_KEY))) {
				return true;
			}
		}
		if ((proposed.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0
				&& RESERVED_CTRL_KEYS.contains(proposed.getKeyCode())) {
			return true;
		}
		Component main = registry.getApp().getMainComponent();
		JComponent root = main == null ? null : SwingUtilities.getRootPane(main);
		if (root != null) {
			for (int condition : new int[] {JComponent.WHEN_FOCUSED,
					JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
					JComponent.WHEN_IN_FOCUSED_WINDOW}) {
				InputMap map = root.getInputMap(condition);
				if (map != null && map.get(proposed) != null) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean sameBinding(KeyStroke first, KeyStroke second) {
		return first != null && first.equals(second);
	}

	private static boolean validFactor(double proposed) {
		return Double.isFinite(proposed) && proposed > 1;
	}

	private static double decodeFactor(String encoded) {
		if (encoded != null && !encoded.isBlank()) {
			try {
				double decoded = Double.parseDouble(encoded);
				if (validFactor(decoded)) {
					return decoded;
				}
			} catch (NumberFormatException exception) {
				// Invalid external preference input fails to the documented default.
			}
		}
		return DEFAULT_FACTOR;
	}

	private static boolean valid(KeyStroke proposed) {
		return proposed != null && proposed.getKeyEventType() == KeyEvent.KEY_PRESSED
				&& proposed.getKeyCode() != KeyEvent.VK_UNDEFINED
				&& proposed.getKeyCode() != KeyEvent.VK_CONTROL
				&& proposed.getKeyCode() != KeyEvent.VK_ALT
				&& proposed.getKeyCode() != KeyEvent.VK_SHIFT
				&& proposed.getKeyCode() != KeyEvent.VK_META
				&& proposed.getKeyCode() != KeyEvent.VK_ALT_GRAPH
				&& (proposed.getModifiers() & InputEvent.ALT_GRAPH_DOWN_MASK) == 0
				&& (proposed.getModifiers() & ALLOWED_MODIFIERS) != 0;
	}

	@SuppressWarnings("deprecation")
	private static KeyStroke normalize(KeyStroke stroke) {
		if (stroke == null) {
			return null;
		}
		int raw = stroke.getModifiers();
		int modifiers = 0;
		if ((raw & (InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK)) != 0) {
			modifiers |= InputEvent.CTRL_DOWN_MASK;
		}
		if ((raw & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) != 0) {
			modifiers |= InputEvent.ALT_DOWN_MASK;
		}
		if ((raw & (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK)) != 0) {
			modifiers |= InputEvent.SHIFT_DOWN_MASK;
		}
		if ((raw & (InputEvent.META_DOWN_MASK | InputEvent.META_MASK)) != 0) {
			modifiers |= InputEvent.META_DOWN_MASK;
		}
		if ((raw & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
			modifiers |= InputEvent.ALT_GRAPH_DOWN_MASK;
		}
		return KeyStroke.getKeyStroke(stroke.getKeyCode(), modifiers);
	}

	private static String encode(KeyStroke stroke) {
		return stroke == null ? "" : VERSION + ":" + stroke.getKeyCode() + ":"
				+ (stroke.getModifiers() & ALLOWED_MODIFIERS);
	}

	private static KeyStroke decode(String encoded) {
		if (encoded == null || encoded.isBlank()) {
			return null;
		}
		String[] fields = encoded.split(":", -1);
		if (fields.length != 3 || !VERSION.equals(fields[0])) {
			return null;
		}
		try {
			KeyStroke stroke = KeyStroke.getKeyStroke(Integer.parseInt(fields[1]),
					Integer.parseInt(fields[2]));
			return valid(stroke) ? stroke : null;
		} catch (NumberFormatException exception) {
			return null;
		}
	}
}
