/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.gui.toolbar.ToolbarD;
import org.geogebra.desktop.main.GeoGebraPreferencesD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Product-local A7 cursor lifecycle, ratio-safe ZoomWindow and factor zoom. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PostG9U1A7NavigationTest {

	private static final double VIEW_TOLERANCE = 1E-7;
	private AppGeoCeDG app;
	private GeoCeDGEuclidianController controller;

	@BeforeEach
	void setUp() {
		clearPreferences();
		app = G9U1TestApp.create();
		app.getEuclidianView1().setSize(new Dimension(800, 500));
		app.getEuclidianView1().updateSize();
		app.getEuclidianView1().setCoordSystem(400, 250, 100, 100);
		controller = (GeoCeDGEuclidianController)
				app.getEuclidianView1().getEuclidianController();
	}

	@AfterEach
	void resetPreference() {
		clearPreferences();
	}

	@Test
	void freshCursorAnchorsWindowWhileStaleCursorUsesWaitForDrag() {
		controller.wrapMouseEntered();
		assertFalse(controller.isMouseLocationValidForKeyboardNavigation());
		controller.wrapMouseMoved(event(MouseEvent.MOUSE_MOVED, 200, 150));
		assertTrue(controller.isMouseLocationValidForKeyboardNavigation());
		controller.activateZoomWindow();
		assertTrue(controller.isZoomWindowKeyboardAnchored());
		controller.cancelZoomWindow();

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
	void escapeCancelsWaitAndInProgressRectangleWithoutChangingView() {
		EuclidianView view = app.getEuclidianView1();
		double[] before = transform(view);
		controller.invalidateNavigationCursorContext();
		controller.activateZoomWindow();
		dispatchEscape();
		assertFalse(controller.isZoomWindowActive());
		assertNull(view.getSelectionRectangle());
		assertTransform(before, view);

		controller.invalidateNavigationCursorContext();
		controller.activateZoomWindow();
		controller.wrapMousePressed(event(MouseEvent.MOUSE_PRESSED, 100, 100));
		controller.wrapMouseDragged(event(MouseEvent.MOUSE_DRAGGED, 500, 350), false);
		assertTrue(view.getSelectionRectangle() != null);
		dispatchEscape();
		assertFalse(controller.isZoomWindowActive());
		assertNull(view.getSelectionRectangle());
		assertTransform(before, view);
	}

	@Test
	void windowFitsWideAndTallRegionsWithoutChangingAxisScaleRatio() throws Exception {
		EuclidianView view = app.getEuclidianView1();
		view.setCoordSystem(400, 250, 120, 80);
		assertWindowFit(80, 190, 720, 310);
		view.setCoordSystem(400, 250, 120, 80);
		assertWindowFit(330, 25, 470, 475);
	}

	@Test
	void factorActionsUseOneCurrentAnchorAndAreReciprocal() throws Exception {
		GeoCeDGActionRegistry registry = registry();
		GeoCeDGNavigationShortcutPreferences preferences =
				registry.getNavigationShortcuts();
		assertEquals(10, preferences.getFactor());
		controller.wrapMouseMoved(event(MouseEvent.MOUSE_MOVED, 260, 170));
		EuclidianView view = app.getEuclidianView1();
		double worldX = view.toRealWorldCoordX(260);
		double worldY = view.toRealWorldCoordY(170);
		double[] before = transform(view);
		registry.invoke(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_ACTION_ID, null);
		awaitScale(view, before[2]);
		assertEquals(before[2] * 10, view.getXscale(), VIEW_TOLERANCE);
		assertEquals(before[3] * 10, view.getYscale(), VIEW_TOLERANCE);
		assertEquals(260, view.toScreenCoordXd(worldX), VIEW_TOLERANCE);
		assertEquals(170, view.toScreenCoordYd(worldY), VIEW_TOLERANCE);
		double zoomed = view.getXscale();
		registry.invoke(GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_ACTION_ID, null);
		awaitScale(view, zoomed);
		assertTransform(before, view);

		controller.invalidateNavigationCursorContext();
		double centerWorldX = view.toRealWorldCoordX(view.getWidth() / 2);
		double centerWorldY = view.toRealWorldCoordY(view.getHeight() / 2);
		registry.invoke(GeoCeDGNavigationShortcutPreferences.ZOOM_IN_ACTION_ID, null);
		awaitScale(view, before[2]);
		assertEquals(view.getWidth() / 2,
				view.toScreenCoordXd(centerWorldX), VIEW_TOLERANCE);
		assertEquals(view.getHeight() / 2,
				view.toScreenCoordYd(centerWorldY), VIEW_TOLERANCE);
	}

	@Test
	void configurationIsPersistentAtomicAndCorrectableInTheSamePanel() {
		GeoCeDGActionRegistry registry = registry();
		GeoCeDGNavigationShortcutPreferences preferences =
				registry.getNavigationShortcuts();
		KeyStroke zoomIn = stroke(KeyEvent.VK_F11);
		KeyStroke zoomOut = stroke(KeyEvent.VK_F12);
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.ACCEPTED,
				preferences.setConfiguration(8, zoomIn, zoomOut));
		assertEquals(zoomIn, registry.get("navigation.zoom-factor-in")
				.getValue(Action.ACCELERATOR_KEY));
		assertEquals(zoomOut, registry.get("navigation.zoom-factor-out")
				.getValue(Action.ACCELERATOR_KEY));
		assertEquals(new GeoCeDGNavigationShortcutPreferences.Configuration(
				8, zoomIn, zoomOut),
				new GeoCeDGNavigationShortcutPreferences(registry).getConfiguration());

		GeoCeDGNavigationSettingsPanel panel =
				new GeoCeDGNavigationSettingsPanel(registry);
		JButton apply = new JButton();
		GeoCeDGHostMenuFactory.bindNavigationApplyState(panel, apply);
		assertTrue(panel.isDraftValid());
		assertTrue(apply.isEnabled());
		panel.setDraft(1, stroke(KeyEvent.VK_F9), stroke(KeyEvent.VK_F10));
		assertFalse(panel.isDraftValid());
		assertFalse(apply.isEnabled());
		assertTrue(panel.hasValidationError());
		assertEquals(8, preferences.getFactor());
		panel.setDraft(9, zoomIn, zoomIn);
		assertFalse(panel.isDraftValid());
		assertFalse(apply.isEnabled());
		assertTrue(panel.hasValidationError());
		assertEquals(GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_ACTION_ID,
				panel.zoomInConflictActionId());
		assertTrue(panel.zoomInValidationText().contains(String.valueOf(
				registry.get(GeoCeDGNavigationShortcutPreferences.ZOOM_OUT_ACTION_ID)
						.getValue(Action.NAME))));
		assertEquals(new GeoCeDGNavigationShortcutPreferences.Configuration(
				8, zoomIn, zoomOut), preferences.getConfiguration());
		panel.setDraft(9, stroke(KeyEvent.VK_F9), stroke(KeyEvent.VK_F10));
		assertTrue(panel.isDraftValid());
		assertTrue(apply.isEnabled());
		assertEquals(registry.text("Navigation.Shortcut.Available"),
				panel.zoomInValidationText());
		assertEquals(new GeoCeDGNavigationShortcutPreferences.Configuration(
				8, zoomIn, zoomOut), preferences.getConfiguration());
		assertTrue(panel.applyConfiguration());
		assertEquals(9, preferences.getFactor());
	}

	@Test
	void cancelDraftAndInvalidReservedShortcutLeavePreferencesUntouched() {
		GeoCeDGNavigationShortcutPreferences preferences =
				registry().getNavigationShortcuts();
		KeyStroke zoomIn = stroke(KeyEvent.VK_F11);
		KeyStroke zoomOut = stroke(KeyEvent.VK_F12);
		preferences.setConfiguration(7, zoomIn, zoomOut);
		GeoCeDGNavigationSettingsPanel panel =
				new GeoCeDGNavigationSettingsPanel(registry());
		panel.setDraft(12, stroke(KeyEvent.VK_F8), stroke(KeyEvent.VK_F9));
		// Dismissing the panel invokes no preference operation.
		assertEquals(7, preferences.getFactor());
		panel.setDraft(12, KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), null);
		assertFalse(panel.isDraftValid());
		assertEquals(registry().text("Navigation.Shortcut.Invalid"),
				panel.zoomInValidationText());
		panel.setDraft(12, null, null);
		assertTrue(panel.isDraftValid());
		assertEquals(registry().text("Navigation.Shortcut.Unassigned"),
				panel.zoomInValidationText());
		assertEquals(new GeoCeDGNavigationShortcutPreferences.Configuration(
				7, zoomIn, zoomOut), preferences.getConfiguration());
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.CONFLICT,
				preferences.setConfiguration(12,
						KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
						stroke(KeyEvent.VK_F9)));
		assertEquals(new GeoCeDGNavigationShortcutPreferences.Configuration(
				7, zoomIn, zoomOut), preferences.getConfiguration());
		assertFalse(app.getXML().contains(
				GeoCeDGNavigationShortcutPreferences.FACTOR_PREFERENCE_KEY));
		assertEquals(GeoCeDGNavigationShortcutPreferences.Result.ACCEPTED,
				preferences.reset());
		assertEquals(new GeoCeDGNavigationShortcutPreferences.Configuration(
				GeoCeDGNavigationShortcutPreferences.DEFAULT_FACTOR, null, null),
				preferences.getConfiguration());
	}

	@Test
	void explicitActionsReuseWindowAndNavigationNeverChangesGeometryIdentity()
			throws Exception {
		GeoLocusV2 source = (GeoLocusV2) G9U1TestApp.eval(app,
				"S=SplineV2({(-2,0),(-2/3,0),(2/3,0),(2,0)},3)");
		final PersistentGeoId id = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry()
				.getPersistentGeoId(source);
		final int constructionSize = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();
		final long revision = source.getSemanticRevision();
		GeoCeDGActionRegistry registry = registry();
		assertTrue(registry.ids().contains("navigation.zoom-window"));
		assertTrue(registry.ids().contains("navigation.zoom-factor-in"));
		assertTrue(registry.ids().contains("navigation.zoom-factor-out"));
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar menuBar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		JMenuItem menuItem = findMenuItem(menuBar, "navigation.zoom-window");
		assertNotNull(menuItem);
		assertSame(registry.get("navigation.zoom-window"), menuItem.getAction());
		double[] beforeMenuCancel = transform(app.getEuclidianView1());
		menuItem.doClick();
		flushEdt();
		assertTrue(controller.isZoomWindowActive());
		dispatchEscape();
		assertFalse(controller.isZoomWindowActive());
		assertTransform(beforeMenuCancel, app.getEuclidianView1());
		activateAndComplete(menuItem);

		app.getEuclidianView1().setCoordSystem(400, 250, 100, 100);
		GeoCeDGToolbarContainer container = new GeoCeDGToolbarContainer(app,
				((GuiManagerGeoCeDG) app.getGuiManager()).getWorkspaceController());
		container.buildGui();
		ToolbarD toolbar = container.getToolbar(-1);
		JComponent navigation = findToolbarGroup(toolbar, "view-navigation");
		assertNotNull(navigation);
		JToggleButton button = GeoCeDGToolbarContainer.toolbarButton(navigation);
		JPopupMenu popup = (JPopupMenu) button.getClientProperty("geocedg.toolbar.popup");
		JMenuItem toolbarItem = findPopupItem(popup, "navigation.zoom-window");
		assertNotNull(toolbarItem);
		assertSame(menuItem.getAction(), toolbarItem.getAction());
		double[] beforeToolbarCancel = transform(app.getEuclidianView1());
		toolbarItem.doClick();
		flushEdt();
		assertEquals("navigation.zoom-window",
				button.getClientProperty("geocedg.toolbar.active.action.id"));
		assertTrue(controller.isZoomWindowActive());
		dispatchEscape();
		assertFalse(controller.isZoomWindowActive());
		assertTransform(beforeToolbarCancel, app.getEuclidianView1());
		activateAndComplete(button);

		controller.zoomByFactor(1.1);
		assertTrue(source.isDefined());
		assertEquals(revision, source.getSemanticRevision());
		assertEquals(constructionSize, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertEquals(id, app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(source));
		app.getGuiManager().setShowView(true,
				org.geogebra.common.main.App.VIEW_EUCLIDIAN2);
		app.setActiveView(org.geogebra.common.main.App.VIEW_EUCLIDIAN2);
		assertTrue(registry.unavailableReason("navigation.zoom-window") != null);
		assertTrue(registry.unavailableReason("navigation.zoom-factor-in") != null);
	}

	private void activateAndComplete(AbstractButton item) throws Exception {
		final double oldScale = app.getEuclidianView1().getXscale();
		item.doClick();
		flushEdt();
		assertTrue(controller.isZoomWindowActive());
		controller.wrapMousePressed(event(MouseEvent.MOUSE_PRESSED, 120, 100));
		controller.wrapMouseDragged(event(MouseEvent.MOUSE_DRAGGED, 560, 390), false);
		assertNotNull(app.getEuclidianView1().getSelectionRectangle());
		controller.wrapMouseReleased(event(MouseEvent.MOUSE_RELEASED, 560, 390));
		awaitScale(app.getEuclidianView1(), oldScale);
		awaitTransformStable(app.getEuclidianView1());
		assertFalse(controller.isZoomWindowActive());
	}

	private static JComponent findToolbarGroup(ToolbarD toolbar, String id) {
		for (Component component : toolbar.getComponents()) {
			if (component instanceof JComponent && id.equals(((JComponent) component)
					.getClientProperty("geocedg.presentation.group.id"))) {
				return (JComponent) component;
			}
		}
		return null;
	}

	private static JMenuItem findPopupItem(JPopupMenu popup, String id) {
		for (Component component : popup.getComponents()) {
			if (component instanceof JMenuItem && id.equals(((JMenuItem) component)
					.getClientProperty(GeoCeDGActionRegistry.ACTION_ID))) {
				return (JMenuItem) component;
			}
		}
		return null;
	}

	private static JMenuItem findMenuItem(GeoCeDGMenuBar menuBar, String id) {
		for (Component component : menuBar.getComponents()) {
			JMenuItem item = G9U1WorkspaceSurfaceTest.findItem(component, id);
			if (item != null) {
				return item;
			}
		}
		return null;
	}

	private static void flushEdt() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			// Drain the deferred menu/toolbar activation after Swing focus handling.
		});
	}

	private void assertWindowFit(int startX, int startY, int endX, int endY)
			throws InterruptedException {
		EuclidianView view = app.getEuclidianView1();
		double ratio = view.getXscale() / view.getYscale();
		final double minX = view.toRealWorldCoordX(Math.min(startX, endX));
		final double maxX = view.toRealWorldCoordX(Math.max(startX, endX));
		final double minY = view.toRealWorldCoordY(Math.max(startY, endY));
		final double maxY = view.toRealWorldCoordY(Math.min(startY, endY));
		double oldScale = view.getXscale();
		controller.invalidateNavigationCursorContext();
		controller.activateZoomWindow();
		controller.wrapMousePressed(event(MouseEvent.MOUSE_PRESSED, startX, startY));
		controller.wrapMouseDragged(event(MouseEvent.MOUSE_DRAGGED, endX, endY), false);
		controller.wrapMouseReleased(event(MouseEvent.MOUSE_RELEASED, endX, endY));
		awaitScale(view, oldScale);
		assertEquals(ratio, view.getXscale() / view.getYscale(), VIEW_TOLERANCE);
		assertTrue(view.getXmin() <= minX + VIEW_TOLERANCE);
		assertTrue(view.getXmax() >= maxX - VIEW_TOLERANCE);
		assertTrue(view.getYmin() <= minY + VIEW_TOLERANCE);
		assertTrue(view.getYmax() >= maxY - VIEW_TOLERANCE);
	}

	private void dispatchEscape() {
		app.getGlobalKeyDispatcher().dispatchKeyEvent(new KeyEvent(
				app.getEuclidianView1().getJPanel(), KeyEvent.KEY_PRESSED,
				System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE,
				KeyEvent.CHAR_UNDEFINED));
	}

	private GeoCeDGActionRegistry registry() {
		return ((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry();
	}

	private KeyStroke stroke(int keyCode) {
		return KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_DOWN_MASK
				| InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
	}

	private AbstractEvent event(int id, int x, int y) {
		return MouseEventD.wrapEvent(new MouseEvent(app.getEuclidianView1().getJPanel(),
				id, 1, 0, x, y, 1, false, MouseEvent.BUTTON1));
	}

	private static double[] transform(EuclidianView view) {
		return new double[] {view.getXZero(), view.getYZero(), view.getXscale(),
				view.getYscale()};
	}

	private static void assertTransform(double[] expected, EuclidianView view) {
		assertEquals(expected[0], view.getXZero(), VIEW_TOLERANCE);
		assertEquals(expected[1], view.getYZero(), VIEW_TOLERANCE);
		assertEquals(expected[2], view.getXscale(), VIEW_TOLERANCE);
		assertEquals(expected[3], view.getYscale(), VIEW_TOLERANCE);
	}

	private static void awaitScale(EuclidianView view, double previous)
			throws InterruptedException {
		long deadline = System.nanoTime() + 2_000_000_000L;
		while (view.getXscale() == previous && System.nanoTime() < deadline) {
			Thread.sleep(5);
		}
		assertTrue(view.getXscale() != previous);
	}

	private static void awaitTransformStable(EuclidianView view)
			throws InterruptedException {
		long deadline = System.nanoTime() + 2_000_000_000L;
		double[] previous = transform(view);
		int stableSamples = 0;
		while (stableSamples < 3 && System.nanoTime() < deadline) {
			Thread.sleep(20);
			double[] current = transform(view);
			boolean stable = true;
			for (int i = 0; i < current.length; i++) {
				stable &= Math.abs(current[i] - previous[i]) <= VIEW_TOLERANCE;
			}
			stableSamples = stable ? stableSamples + 1 : 0;
			previous = current;
		}
		assertEquals(3, stableSamples);
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
