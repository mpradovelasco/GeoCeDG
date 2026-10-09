/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.geocedg.desktop.GeoCeDGNavigationShortcutPreferences.Configuration;
import org.geocedg.desktop.GeoCeDGNavigationShortcutPreferences.Result;
import org.geocedg.desktop.GeoCeDGNavigationShortcutPreferences.ShortcutStatus;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * POST-E2-P2-R1 (Option A): factor-zoom chords that type or edit text are refused,
 * stored ones are read as unassigned without rewriting the preference store, and
 * every other rule (reserved keys, conflicts, factor, independence of the two
 * bindings) is kept. The tests use the test preference store, never the author's
 * product settings file.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PostE2P2R1ShortcutValidationTest {
	private static final int CTRL = InputEvent.CTRL_DOWN_MASK;
	private static final int ALT = InputEvent.ALT_DOWN_MASK;
	private static final int SHIFT = InputEvent.SHIFT_DOWN_MASK;
	private static final int META = InputEvent.META_DOWN_MASK;
	private AppGeoCeDG app;

	@BeforeEach
	void setUp() {
		clearPreferences();
		app = G9U1TestApp.create();
	}

	@AfterEach
	void tearDown() {
		clearPreferences();
	}

	private GeoCeDGActionRegistry registry() {
		return ((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry();
	}

	private ShortcutStatus status(KeyStroke stroke) {
		return registry().getNavigationShortcuts().validateConfiguration("10", stroke, null)
				.zoomIn().status();
	}

	private static KeyStroke key(int keyCode, int modifiers) {
		return KeyStroke.getKeyStroke(keyCode, modifiers);
	}

	// ----------------------------------------------------------- 8.1 validation

	@Test
	void everyShiftOnlyCharacterKeyIsRefusedAsTextEntry() {
		List<Integer> keys = new ArrayList<>();
		for (int k = KeyEvent.VK_A; k <= KeyEvent.VK_Z; k++) {
			keys.add(k);
		}
		for (int k = KeyEvent.VK_0; k <= KeyEvent.VK_9; k++) {
			keys.add(k);
		}
		for (int k = KeyEvent.VK_NUMPAD0; k <= KeyEvent.VK_NUMPAD9; k++) {
			keys.add(k);
		}
		for (int k : new int[] {KeyEvent.VK_COMMA, KeyEvent.VK_PERIOD, KeyEvent.VK_MINUS,
			KeyEvent.VK_SLASH, KeyEvent.VK_SEMICOLON, KeyEvent.VK_EQUALS,
			KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_BACK_SLASH,
			KeyEvent.VK_QUOTE, KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_PLUS, KeyEvent.VK_LESS,
			KeyEvent.VK_NUMBER_SIGN, KeyEvent.VK_DEAD_ACUTE, KeyEvent.VK_DEAD_GRAVE,
			KeyEvent.VK_SPACE, KeyEvent.VK_MULTIPLY, KeyEvent.VK_ADD, KeyEvent.VK_DECIMAL,
			KeyEvent.VK_HOME, KeyEvent.VK_END, KeyEvent.VK_LEFT, KeyEvent.VK_DELETE,
			KeyEvent.VK_INSERT, KeyEvent.VK_ENTER, KeyEvent.VK_TAB, 0x10000 + 'ñ'}) {
			keys.add(k);
		}
		for (int k : keys) {
			KeyStroke shifted = key(k, SHIFT);
			assertEquals(ShortcutStatus.TEXT_ENTRY, status(shifted), shifted.toString());
		}
		// the author's chords and the normalized (old-style mask) form of them
		assertEquals(ShortcutStatus.TEXT_ENTRY, status(key(KeyEvent.VK_A, SHIFT)));
		assertEquals(ShortcutStatus.TEXT_ENTRY, status(key(KeyEvent.VK_Z, SHIFT)));
		@SuppressWarnings("deprecation")
		KeyStroke oldMask = key(KeyEvent.VK_B, InputEvent.SHIFT_MASK);
		assertEquals(ShortcutStatus.TEXT_ENTRY, status(oldMask));
	}

	@Test
	void altWithANumericKeypadDigitIsRefusedAsACharacterCode() {
		for (int k = KeyEvent.VK_NUMPAD0; k <= KeyEvent.VK_NUMPAD9; k++) {
			assertEquals(ShortcutStatus.TEXT_ENTRY, status(key(k, ALT)), "Alt+numpad " + k);
		}
	}

	@Test
	void chordsThatCannotTypeTextStayAvailableUnderTheExistingRules() {
		for (int k = KeyEvent.VK_F1; k <= KeyEvent.VK_F12; k++) {
			assertEquals(ShortcutStatus.AVAILABLE, status(key(k, SHIFT)), "Shift+F" + k);
		}
		assertEquals(ShortcutStatus.AVAILABLE, status(key(KeyEvent.VK_F13, SHIFT)));
		assertEquals(ShortcutStatus.AVAILABLE, status(key(KeyEvent.VK_F24, SHIFT)));
		assertEquals(ShortcutStatus.AVAILABLE, status(key(KeyEvent.VK_F11, CTRL | ALT | SHIFT)));
		assertEquals(ShortcutStatus.AVAILABLE, status(key(KeyEvent.VK_K, CTRL | SHIFT)));
		assertEquals(ShortcutStatus.AVAILABLE, status(key(KeyEvent.VK_K, ALT | SHIFT)));
		assertEquals(ShortcutStatus.AVAILABLE, status(key(KeyEvent.VK_K, META)));
		assertEquals(ShortcutStatus.AVAILABLE, status(key(KeyEvent.VK_NUMPAD5, CTRL)));
		// Ctrl+Alt on a character key: unchanged by R1, an open author decision (AltGr)
		assertEquals(ShortcutStatus.AVAILABLE, status(key(KeyEvent.VK_E, CTRL | ALT)));
		// existing rules are untouched: a plain key, a modifier key, reserved Ctrl keys
		assertEquals(ShortcutStatus.INVALID, status(key(KeyEvent.VK_F8, 0)));
		assertEquals(ShortcutStatus.INVALID, status(key(KeyEvent.VK_K, 0)));
		assertEquals(ShortcutStatus.INVALID, status(key(KeyEvent.VK_SHIFT, SHIFT)));
		assertEquals(ShortcutStatus.CONFLICT, status(key(KeyEvent.VK_Z, CTRL)));
		assertEquals(ShortcutStatus.CONFLICT, status(key(KeyEvent.VK_MINUS, CTRL)));
	}

	@Test
	void aRefusedChordCommitsNothingAndTheDialogExplainsWhy() {
		GeoCeDGNavigationShortcutPreferences preferences = registry().getNavigationShortcuts();
		KeyStroke zoomIn = key(KeyEvent.VK_F11, CTRL | ALT | SHIFT);
		KeyStroke zoomOut = key(KeyEvent.VK_F12, CTRL | ALT | SHIFT);
		assertEquals(Result.ACCEPTED, preferences.setConfiguration(7, zoomIn, zoomOut));
		String storedIn = load(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_PREFERENCE_KEY);
		assertEquals(Result.INVALID_SHORTCUT, preferences.setConfiguration(9,
				key(KeyEvent.VK_A, SHIFT), zoomOut));
		assertEquals(new Configuration(7, zoomIn, zoomOut), preferences.getConfiguration());
		assertEquals(storedIn, load(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_PREFERENCE_KEY));
		assertEquals("7.0", load(GeoCeDGNavigationShortcutPreferences.FACTOR_PREFERENCE_KEY));
		assertEquals(zoomIn, registry().get(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_ACTION_ID)
				.getValue(Action.ACCELERATOR_KEY), "the active accelerator is unchanged");
		assertFalse(app.getKernel().getConstruction().getUndoManager().undoPossible());

		for (Locale locale : new Locale[] {Locale.ENGLISH, new Locale("es")}) {
			app.setLocale(locale);
			GeoCeDGNavigationSettingsPanel panel = new GeoCeDGNavigationSettingsPanel(registry());
			panel.setDraft(7, key(KeyEvent.VK_A, SHIFT), zoomOut);
			assertFalse(panel.isDraftValid());
			assertTrue(panel.hasValidationError());
			assertEquals(registry().text("Navigation.Shortcut.TextEntry"),
					panel.zoomInValidationText());
			assertFalse(panel.applyConfiguration());
			assertEquals(new Configuration(7, zoomIn, zoomOut), preferences.getConfiguration());
		}
	}

	// ----------------------------------------------------- 8.2 stored preferences

	@Test
	void theAuthorsStoredShiftChordsLoadAsUnassignedWithoutRewritingTheStore() {
		save(GeoCeDGNavigationShortcutPreferences.FACTOR_PREFERENCE_KEY, "10.0");
		save(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_PREFERENCE_KEY, "v1:90:64");
		save(GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_PREFERENCE_KEY, "v1:65:64");
		save("geocedg.navigation.zoom-window.shortcut.v1", "v1:90:64");
		GeoCeDGNavigationShortcutPreferences loaded =
				new GeoCeDGNavigationShortcutPreferences(registry());
		assertEquals(new Configuration(10, null, null), loaded.getConfiguration());
		assertNull(registry().get(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_ACTION_ID)
				.getValue(Action.ACCELERATOR_KEY));
		assertNull(registry().get(GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_ACTION_ID)
				.getValue(Action.ACCELERATOR_KEY));
		// read-only loading: the stored values are left exactly as they were
		assertEquals("10.0", load(GeoCeDGNavigationShortcutPreferences.FACTOR_PREFERENCE_KEY));
		assertEquals("v1:90:64", load(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_PREFERENCE_KEY));
		assertEquals("v1:65:64",
				load(GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_PREFERENCE_KEY));
		assertEquals("v1:90:64", load("geocedg.navigation.zoom-window.shortcut.v1"));
		// the real menu items carry no accelerator
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar menuBar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		for (String id : new String[] {GeoCeDGNavigationShortcutPreferences.ZOOM_IN_ACTION_ID,
			GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_ACTION_ID}) {
			JMenuItem item = null;
			for (int index = 0; index < menuBar.getMenuCount() && item == null; index++) {
				item = G9U1WorkspaceSurfaceTest.findItem(menuBar.getMenu(index), id);
			}
			assertNull(item.getAccelerator(), id);
		}
	}

	@Test
	void aValidSecondBindingAndTheFactorSurviveAnInvalidStoredFirstBinding() {
		save(GeoCeDGNavigationShortcutPreferences.FACTOR_PREFERENCE_KEY, "7.5");
		save(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_PREFERENCE_KEY, "v1:90:64");
		// Ctrl+Alt+Shift+F12, valid and free
		save(GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_PREFERENCE_KEY, "v1:123:704");
		GeoCeDGNavigationShortcutPreferences loaded =
				new GeoCeDGNavigationShortcutPreferences(registry());
		KeyStroke valid = key(KeyEvent.VK_F12, CTRL | ALT | SHIFT);
		assertEquals(new Configuration(7.5, null, valid), loaded.getConfiguration());
		assertEquals(valid, registry().get(GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_ACTION_ID)
				.getValue(Action.ACCELERATOR_KEY));
		assertNull(registry().get(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_ACTION_ID)
				.getValue(Action.ACCELERATOR_KEY));
		// and the other way round
		save(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_PREFERENCE_KEY, "v1:123:704");
		save(GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_PREFERENCE_KEY, "v1:65:64");
		assertEquals(new Configuration(7.5, valid, null),
				new GeoCeDGNavigationShortcutPreferences(registry()).getConfiguration());
	}

	private static void save(String key, String value) {
		GeoGebraPreferencesD.getPref().savePreference(key, value);
	}

	private static String load(String key) {
		return GeoGebraPreferencesD.getPref().loadPreference(key, "");
	}

	private static void clearPreferences() {
		for (String key : new String[] {GeoCeDGNavigationShortcutPreferences.FACTOR_PREFERENCE_KEY,
			GeoCeDGNavigationShortcutPreferences.ZOOM_IN_PREFERENCE_KEY,
			GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_PREFERENCE_KEY,
			"geocedg.navigation.zoom-window.shortcut.v1"}) {
			save(key, "");
		}
	}
}
