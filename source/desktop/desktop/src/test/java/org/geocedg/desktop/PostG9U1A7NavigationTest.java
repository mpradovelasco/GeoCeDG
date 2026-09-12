/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Product-local A7 cursor lifecycle, ZoomWindow reuse and shortcut preference. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PostG9U1A7NavigationTest {

	private AppGeoCeDG app;
	private GeoCeDGEuclidianController controller;

	@BeforeEach
	void setUp() {
		GeoGebraPreferencesD.getPref().savePreference(
				GeoCeDGNavigationShortcutPreferences.PREFERENCE_KEY, "");
		app = G9U1TestApp.create();
		app.getEuclidianView1().setSize(new Dimension(800, 500));
		app.getEuclidianView1().updateSize();
		app.getEuclidianView1().setCoordSystem(400, 250, 100, 100);
		controller = (GeoCeDGEuclidianController)
				app.getEuclidianView1().getEuclidianController();
	}

	@AfterEach
	void resetPreference() {
		GeoGebraPreferencesD.getPref().savePreference(
				GeoCeDGNavigationShortcutPreferences.PREFERENCE_KEY, "");
	}

	@Test
	void freshCursorAnchorsExistingZoomWindowWhileStaleCursorWaitsForDrag() {
		controller.wrapMouseEntered();
		assertFalse(controller.isMouseLocationValidForKeyboardNavigation());
		controller.wrapMouseMoved(event(MouseEvent.MOUSE_MOVED, 200, 150));
		assertTrue(controller.isMouseLocationValidForKeyboardNavigation());
		controller.activateZoomWindow();
		assertTrue(controller.isZoomWindowKeyboardAnchored());
		controller.wrapMouseMoved(event(MouseEvent.MOUSE_MOVED, 600, 350));
		controller.wrapMousePressed(event(MouseEvent.MOUSE_PRESSED, 600, 350));
		controller.wrapMouseReleased(event(MouseEvent.MOUSE_RELEASED, 600, 350));
		assertFalse(controller.isZoomWindowActive());

		controller.wrapMouseMoved(event(MouseEvent.MOUSE_MOVED, 250, 175));
		controller.textfieldHasFocus(true);
		controller.wrapMouseExited(null);
		controller.textfieldHasFocus(false);
		controller.wrapMouseEntered();
		assertFalse(controller.isMouseLocationValidForKeyboardNavigation());
		controller.activateZoomWindow();
		assertTrue(controller.isZoomWindowActive());
		assertFalse(controller.isZoomWindowKeyboardAnchored());
		assertNull(app.getEuclidianView1().getSelectionRectangle());

		controller.wrapMouseMoved(event(MouseEvent.MOUSE_MOVED, 300, 200));
		controller.activateZoomWindow();
		for (var listener : app.getEuclidianView1().getJPanel().getFocusListeners()) {
			listener.focusLost(new FocusEvent(app.getEuclidianView1().getJPanel(),
					FocusEvent.FOCUS_LOST));
		}
		assertFalse(controller.isMouseLocationValidForKeyboardNavigation());
		assertFalse(controller.isZoomWindowActive());
	}

	@Test
	void navigationChangesOnlyViewPresentationNotGeometryOrIdentity() {
		GeoLocusV2 source = (GeoLocusV2) G9U1TestApp.eval(app,
				"S=SplineV2({(-2,0),(-2/3,0),(2/3,0),(2,0)},3)");
		PersistentGeoId id = app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(source);
		int constructionSize = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		long revision = source.getSemanticRevision();
		controller.zoomInOut(1.1, 1, 300, 200);
		assertTrue(source.isDefined());
		assertEquals(revision, source.getSemanticRevision());
		assertEquals(constructionSize, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertEquals(id, app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(source));
	}

	@Test
	void shortcutIsUnassignedPersistentAndConflictRejectionIsAtomic() {
		GeoCeDGActionRegistry registry = ((GuiManagerGeoCeDG) app.getGuiManager())
				.getActionRegistry();
		GeoCeDGNavigationShortcutPreferences preferences =
				registry.getNavigationShortcuts();
		assertNull(preferences.getBinding());
		KeyStroke accepted = KeyStroke.getKeyStroke(KeyEvent.VK_F12,
				InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK
						| InputEvent.SHIFT_DOWN_MASK);
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.ACCEPTED,
				preferences.setBinding(accepted));
		assertEquals(accepted, registry.get("navigation.zoom-window")
				.getValue(Action.ACCELERATOR_KEY));
		assertEquals(accepted,
				new GeoCeDGNavigationShortcutPreferences(registry).getBinding());
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.CONFLICT,
				preferences.setBinding(KeyStroke.getKeyStroke(KeyEvent.VK_D,
						InputEvent.CTRL_DOWN_MASK)));
		assertEquals(accepted, preferences.getBinding());
		assertFalse(app.getXML().contains(
				GeoCeDGNavigationShortcutPreferences.PREFERENCE_KEY));
	}

	@Test
	void configuredActionReusesG9U1ZoomWindowAndResetReturnsToUnassigned() {
		GeoCeDGActionRegistry registry = ((GuiManagerGeoCeDG) app.getGuiManager())
				.getActionRegistry();
		registry.invoke("navigation.zoom-window", null);
		assertTrue(controller.isZoomWindowActive());
		controller.setMode(0, org.geogebra.common.kernel.ModeSetter.TOOLBAR);
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.RESET,
				registry.getNavigationShortcuts().reset());
		assertNull(registry.get("navigation.zoom-window").getValue(Action.ACCELERATOR_KEY));
		app.getGuiManager().setShowView(true,
				org.geogebra.common.main.App.VIEW_EUCLIDIAN2);
		app.setActiveView(org.geogebra.common.main.App.VIEW_EUCLIDIAN2);
		assertTrue(registry.unavailableReason("navigation.zoom-window") != null);
	}

	private AbstractEvent event(int id, int x, int y) {
		return MouseEventD.wrapEvent(new MouseEvent(app.getEuclidianView1().getJPanel(),
				id, 1, 0, x, y, 1, false, MouseEvent.BUTTON1));
	}
}
