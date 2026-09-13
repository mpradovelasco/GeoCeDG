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

	enum ShortcutStatus {
		AVAILABLE, UNASSIGNED, INVALID, CONFLICT
	}

	record Configuration(double factor, KeyStroke zoomIn, KeyStroke zoomOut) {
	}

	record ShortcutValidation(ShortcutStatus status, String conflictingActionId) {
	}

	record DraftValidation(boolean factorValid, ShortcutValidation zoomIn,
			ShortcutValidation zoomOut, Configuration configuration) {
		boolean isValid() {
			return factorValid && zoomIn.status() != ShortcutStatus.INVALID
					&& zoomIn.status() != ShortcutStatus.CONFLICT
					&& zoomOut.status() != ShortcutStatus.INVALID
					&& zoomOut.status() != ShortcutStatus.CONFLICT;
		}

		Result result() {
			if (!factorValid) {
				return Result.INVALID_FACTOR;
			}
			if (zoomIn.status() == ShortcutStatus.INVALID
					|| zoomOut.status() == ShortcutStatus.INVALID) {
				return Result.INVALID_SHORTCUT;
			}
			return isValid() ? Result.ACCEPTED : Result.CONFLICT;
		}
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
		if (sameBinding(zoomIn, zoomOut) || conflictingAction(zoomIn) != null
				|| conflictingAction(zoomOut) != null) {
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
		return setConfiguration(Double.toString(proposedFactor), proposedIn, proposedOut);
	}

	Result setConfiguration(String proposedFactor, KeyStroke proposedIn,
			KeyStroke proposedOut) {
		DraftValidation validation = validateConfiguration(proposedFactor,
				proposedIn, proposedOut);
		if (!validation.isValid()) {
			return validation.result();
		}
		Configuration accepted = validation.configuration();

		// Validate the complete proposal before publishing any preference.
		GeoGebraPreferencesD preferences = GeoGebraPreferencesD.getPref();
		preferences.savePreference(FACTOR_PREFERENCE_KEY,
				Double.toString(accepted.factor()));
		preferences.savePreference(ZOOM_IN_PREFERENCE_KEY, encode(accepted.zoomIn()));
		preferences.savePreference(ZOOM_OUT_PREFERENCE_KEY, encode(accepted.zoomOut()));
		factor = accepted.factor();
		zoomIn = accepted.zoomIn();
		zoomOut = accepted.zoomOut();
		applyAccelerators();
		return Result.ACCEPTED;
	}

	DraftValidation validateConfiguration(String proposedFactor, KeyStroke proposedIn,
			KeyStroke proposedOut) {
		double parsedFactor = Double.NaN;
		try {
			parsedFactor = Double.parseDouble(proposedFactor.trim());
		} catch (RuntimeException exception) {
			// The typed draft result is the single validation authority for the dialog.
		}
		boolean factorValid = validFactor(parsedFactor);
		KeyStroke normalizedIn = normalize(proposedIn);
		KeyStroke normalizedOut = normalize(proposedOut);
		ShortcutValidation in = validateShortcut(proposedIn, normalizedIn);
		ShortcutValidation out = validateShortcut(proposedOut, normalizedOut);
		if (sameBinding(normalizedIn, normalizedOut)) {
			in = new ShortcutValidation(ShortcutStatus.CONFLICT, ZOOM_OUT_ACTION_ID);
			out = new ShortcutValidation(ShortcutStatus.CONFLICT, ZOOM_IN_ACTION_ID);
		}
		return new DraftValidation(factorValid, in, out,
				new Configuration(parsedFactor, normalizedIn, normalizedOut));
	}

	Result reset() {
		return setConfiguration(DEFAULT_FACTOR, null, null);
	}

	private KeyStroke loadBinding(String key) {
		KeyStroke stored = decode(GeoGebraPreferencesD.getPref().loadPreference(key, ""));
		return stored != null && conflictingAction(stored) == null ? stored : null;
	}

	private void applyAccelerators() {
		registry.get(ZOOM_IN_ACTION_ID).putValue(Action.ACCELERATOR_KEY, zoomIn);
		registry.get(ZOOM_OUT_ACTION_ID).putValue(Action.ACCELERATOR_KEY, zoomOut);
	}

	private ShortcutValidation validateShortcut(KeyStroke proposed, KeyStroke normalized) {
		if (proposed == null) {
			return new ShortcutValidation(ShortcutStatus.UNASSIGNED, null);
		}
		if (!valid(normalized)) {
			return new ShortcutValidation(ShortcutStatus.INVALID, null);
		}
		String conflict = conflictingAction(normalized);
		return conflict == null
				? new ShortcutValidation(ShortcutStatus.AVAILABLE, null)
				: new ShortcutValidation(ShortcutStatus.CONFLICT, conflict);
	}

	private String conflictingAction(KeyStroke proposed) {
		if (proposed == null) {
			return null;
		}
		for (String id : registry.ids()) {
			if (!ZOOM_IN_ACTION_ID.equals(id) && !ZOOM_OUT_ACTION_ID.equals(id)
					&& proposed.equals(registry.get(id).getValue(Action.ACCELERATOR_KEY))) {
				return id;
			}
		}
		if ((proposed.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0
				&& RESERVED_CTRL_KEYS.contains(proposed.getKeyCode())) {
			return "GeoGebra";
		}
		Component main = registry.getApp().getMainComponent();
		JComponent root = main == null ? null : SwingUtilities.getRootPane(main);
		if (root != null) {
			for (int condition : new int[] {JComponent.WHEN_FOCUSED,
					JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
					JComponent.WHEN_IN_FOCUSED_WINDOW}) {
				InputMap map = root.getInputMap(condition);
				if (map != null && map.get(proposed) != null) {
					return String.valueOf(map.get(proposed));
				}
			}
		}
		return null;
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
