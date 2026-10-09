/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.inputbar.AlgebraInputD;
import org.geogebra.desktop.gui.inputfield.AutoCompleteTextFieldD;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * POST-E2-P2 characterization (no product change): the links of the observed chain
 * from typing a capital letter in Algebra Input to a factor zoom of the Graphics view
 * when that letter's Shift chord is assigned to a GeoCeDG factor-zoom action. It pins
 * the current behavior; it is not an acceptance criterion of a correction.
 *
 * <p>The windowed end-to-end path (Swing fires window-scope bindings only for showing
 * components) is evidenced by the scratch probe of the characterization report:
 * {@code MyTextFieldD.processKeyEvent} → {@code JComponent.processKeyBindings} →
 * {@code KeyboardManager} → {@code JMenuBar} → {@code BasicMenuItemUI} accelerator →
 * {@code GeoCeDGActionRegistry} → {@code GeoCeDGEuclidianController.zoomByFactor}.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PostE2P2AlgebraInputZoomCharacterizationTest {
	private static final KeyStroke SHIFT_A = KeyStroke.getKeyStroke(KeyEvent.VK_A,
			InputEvent.SHIFT_DOWN_MASK);
	private AppGeoCeDG app;

	@BeforeEach
	void setUp() {
		clearPreferences();
		app = G9U1TestApp.create();
		app.getEuclidianView1().setSize(new Dimension(800, 500));
		app.getEuclidianView1().updateSize();
		app.getEuclidianView1().setCoordSystem(400, 250, 100, 100);
	}

	@AfterEach
	void tearDown() {
		clearPreferences();
	}

	/** Link 1: a printable Shift chord is accepted as an available shortcut. */
	@Test
	void aShiftLetterChordIsAcceptedForAFactorZoomAction() {
		GeoCeDGNavigationShortcutPreferences preferences = registry().getNavigationShortcuts();
		GeoCeDGNavigationShortcutPreferences.DraftValidation draft =
				preferences.validateConfiguration("10", null, SHIFT_A);
		assertEquals(GeoCeDGNavigationShortcutPreferences.ShortcutStatus.AVAILABLE,
				draft.zoomOut().status());
		assertTrue(draft.isValid());
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.ACCEPTED,
				preferences.setConfiguration(10, null, SHIFT_A));
		assertEquals(SHIFT_A, preferences.getConfiguration().zoomOut());
	}

	/** Link 2: the real menu item binds that chord in the window scope. */
	@Test
	void theRealMenuItemBindsTheChordForTheWholeWindow() {
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.ACCEPTED,
				registry().getNavigationShortcuts().setConfiguration(10, null, SHIFT_A));
		JMenuItem item = zoomOutItem();
		assertEquals(SHIFT_A, item.getAccelerator());
		assertNotNull(item.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(SHIFT_A),
				"menu accelerators are window-scope bindings");
	}

	/** Link 3: the Algebra Input field types the letter and leaves the press unconsumed. */
	@Test
	void theAlgebraInputTypesTheLetterAndLeavesTheShiftPressUnconsumed() throws Exception {
		JTextComponent field = ((AlgebraInputD) ((GuiManagerD) app.getGuiManager())
				.getAlgebraInput()).getTextField();
		for (Container c = field; c != null; c = c.getParent()) {
			if (c instanceof JComponent) {
				assertNull(((JComponent) c).getInputMap(JComponent.WHEN_FOCUSED).get(SHIFT_A),
						"no focused binding for Shift+A in " + c.getClass().getName());
				assertNull(((JComponent) c).getInputMap(
						JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(SHIFT_A),
						"no ancestor binding for Shift+A in " + c.getClass().getName());
			}
		}
		assertNull(field.getKeymap().getAction(SHIFT_A), "the field keymap has no action");
		AtomicReference<KeyEvent> pressed = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			field.setText("Segment(");
			KeyEvent press = new KeyEvent(field, KeyEvent.KEY_PRESSED,
					System.currentTimeMillis(), InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_A,
					KeyEvent.CHAR_UNDEFINED);
			// the field's own handling: key listeners, then its key bindings
			((AutoCompleteTextFieldD) field).processKeyEvent(press);
			pressed.set(press);
		});
		assertFalse(pressed.get().isConsumed(),
				"the press is left to the window-scope bindings (menu accelerators)");
		assertEquals("Segment(", field.getText(), "the press itself edits nothing");
	}

	/** Link 4: the accelerator's activation of the item zooms the view by the factor. */
	@Test
	void activatingTheBoundItemZoomsTheViewOutByTheFactor() throws Exception {
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.ACCEPTED,
				registry().getNavigationShortcuts().setConfiguration(10, null, SHIFT_A));
		JMenuItem item = zoomOutItem();
		EuclidianView view = app.getEuclidianView1();
		double before = view.getXscale();
		// BasicMenuItemUI's accelerator action calls doClick on the item
		SwingUtilities.invokeAndWait(() -> item.doClick(0));
		awaitScale(view, before);
		assertEquals(before / 10, view.getXscale(), 1E-9);
		assertEquals(before / 10, view.getYscale(), 1E-9);
	}

	private GeoCeDGActionRegistry registry() {
		return ((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry();
	}

	private JMenuItem zoomOutItem() {
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar menuBar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		for (int index = 0; index < menuBar.getMenuCount(); index++) {
			JMenuItem item = G9U1WorkspaceSurfaceTest.findItem(menuBar.getMenu(index),
					GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_ACTION_ID);
			if (item != null) {
				return item;
			}
		}
		throw new AssertionError("Missing factor zoom-out menu item");
	}

	private static void awaitScale(EuclidianView view, double previous)
			throws InterruptedException {
		long deadline = System.nanoTime() + 2_000_000_000L;
		while (view.getXscale() == previous && System.nanoTime() < deadline) {
			Thread.sleep(5);
		}
		// let the animation finish
		Thread.sleep(400);
		SwingUtilities.invokeLater(() -> { });
	}

	private static void clearPreferences() {
		GeoGebraPreferencesD preferences = GeoGebraPreferencesD.getPref();
		preferences.savePreference(
				GeoCeDGNavigationShortcutPreferences.FACTOR_PREFERENCE_KEY, "");
		preferences.savePreference(
				GeoCeDGNavigationShortcutPreferences.ZOOM_IN_PREFERENCE_KEY, "");
		preferences.savePreference(
				GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_PREFERENCE_KEY, "");
	}
}
