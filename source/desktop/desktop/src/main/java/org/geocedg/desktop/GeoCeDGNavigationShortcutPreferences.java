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

/** One product-local application preference for the existing ZoomWindow action. */
final class GeoCeDGNavigationShortcutPreferences {

	static final String ACTION_ID = "navigation.zoom-window";
	static final String PREFERENCE_KEY = "geocedg.navigation.zoom-window.shortcut.v1";
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
		ACCEPTED, RESET, INVALID, CONFLICT
	}

	private final GeoCeDGActionRegistry registry;
	private KeyStroke binding;

	GeoCeDGNavigationShortcutPreferences(GeoCeDGActionRegistry registry) {
		this.registry = registry;
		KeyStroke stored = decode(GeoGebraPreferencesD.getPref().loadPreference(
				PREFERENCE_KEY, ""));
		if (stored != null && conflict(stored)) {
			stored = null;
		}
		binding = stored;
		apply();
	}

	KeyStroke getBinding() {
		return binding;
	}

	Result setBinding(KeyStroke proposed) {
		KeyStroke normalized = normalize(proposed);
		if (!valid(normalized)) {
			return Result.INVALID;
		}
		if (conflict(normalized)) {
			return Result.CONFLICT;
		}
		GeoGebraPreferencesD.getPref().savePreference(PREFERENCE_KEY, encode(normalized));
		binding = normalized;
		apply();
		return Result.ACCEPTED;
	}

	Result reset() {
		GeoGebraPreferencesD.getPref().savePreference(PREFERENCE_KEY, "");
		binding = null;
		apply();
		return Result.RESET;
	}

	private void apply() {
		registry.get(ACTION_ID).putValue(Action.ACCELERATOR_KEY, binding);
	}

	private boolean conflict(KeyStroke proposed) {
		for (String id : registry.ids()) {
			if (!ACTION_ID.equals(id)
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
		return VERSION + ":" + stroke.getKeyCode() + ":"
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
