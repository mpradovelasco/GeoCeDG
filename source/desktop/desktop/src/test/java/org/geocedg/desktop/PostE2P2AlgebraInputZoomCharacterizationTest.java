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
 * POST-E2-P2 characterization, reconciled by POST-E2-P2-R1: the links of the observed
 * chain from typing a capital letter in Algebra Input to a factor zoom of the Graphics
 * view when that letter's Shift chord is assigned to a GeoCeDG factor-zoom action.
 * The characterization candidate {@code 216964dc} pinned the defect (links 1, 2 and 4
 * asserted that Shift+A was accepted, bound and zoomed); its report, evidence and the
 * windowed baseline runs of the R1 evidence keep that history. Since R1 the chain is
 * cut at its source: the chord is refused and never bound. Link 3 (the field leaves the
 * press to the window bindings) is inherited Swing behavior and stays as it was.
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

	/** Link 1 (R1): a printable Shift chord is refused as text entry. */
	@Test
	void aShiftLetterChordIsRefusedForAFactorZoomAction() {
		GeoCeDGNavigationShortcutPreferences preferences = registry().getNavigationShortcuts();
		GeoCeDGNavigationShortcutPreferences.DraftValidation draft =
				preferences.validateConfiguration("10", null, SHIFT_A);
		assertEquals(GeoCeDGNavigationShortcutPreferences.ShortcutStatus.TEXT_ENTRY,
				draft.zoomOut().status());
		assertFalse(draft.isValid());
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.INVALID_SHORTCUT,
				preferences.setConfiguration(10, null, SHIFT_A));
		assertNull(preferences.getConfiguration().zoomOut());
	}

	/**
	 * Link 2 (R1): a stored Shift chord (the author's encoding) is read as unassigned,
	 * so the real menu item binds nothing in the window scope.
	 */
	@Test
	void aStoredShiftChordBindsNoMenuAccelerator() {
		GeoGebraPreferencesD.getPref().savePreference(
				GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_PREFERENCE_KEY, "v1:65:64");
		GeoCeDGNavigationShortcutPreferences reloaded =
				new GeoCeDGNavigationShortcutPreferences(registry());
		assertNull(reloaded.getConfiguration().zoomOut());
		JMenuItem item = zoomOutItem();
		assertNull(item.getAccelerator());
		assertNull(item.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(SHIFT_A),
				"no window-scope binding for Shift+A");
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

	/**
	 * Link 4 (R1): a legitimate chord that cannot type text is still bound, and the
	 * accelerator's activation of the item still zooms the view by the factor.
	 */
	@Test
	void aLegitimateChordStillBindsAndZoomsTheViewOutByTheFactor() throws Exception {
		KeyStroke legitimate = KeyStroke.getKeyStroke(KeyEvent.VK_F7,
				InputEvent.SHIFT_DOWN_MASK);
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.ACCEPTED,
				registry().getNavigationShortcuts().setConfiguration(10, null, legitimate));
		JMenuItem item = zoomOutItem();
		assertEquals(legitimate, item.getAccelerator());
		assertNotNull(item.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(legitimate));
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
