/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.io.layout.DockPanelData;
import org.geogebra.common.io.layout.DockSplitPaneData;
import org.geogebra.common.io.layout.Perspective;
import org.geogebra.common.main.App;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.gui.layout.LayoutD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class G9U1WorkspaceSurfaceTest {

	@BeforeAll
	static void initializeDesktop() {
		G9U1ActionRegistryTest.initializeDesktop();
	}

	@Test
	void allActionsAreReachableThroughTheManifestMenuProjection() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		Set<String> ids = new HashSet<>();
		for (Component component : bar.getComponents()) {
			collect(component, ids);
		}
		assertEquals(110, ids.size());
		assertEquals(5, bar.getMenuCount());
		assertTrue(ids.contains("navigation.zoom-window"));
	}

	@Test
	void repeatedMenuRebuildKeepsOneProjectionPerDeclaredPlacement() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		for (int i = 0; i < 3; i++) {
			bar.initMenubar();
			bar.updateFonts();
			assertEquals(5, bar.getMenuCount());
		}
	}

	@Test
	void familyPaletteRetainsAllElevenFamiliesAtHighDpi() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		GeoCeDGWorkspaceController workspace = new GeoCeDGWorkspaceController(app,
				new GeoCeDGActionRegistry(app));
		JScrollPane pane = workspace.createActionPalette();
		JPanel row = (JPanel) pane.getViewport().getView();
		assertEquals(11, row.getComponentCount());
		for (Component component : row.getComponents()) {
			JButton button = (JButton) component;
			button.setFont(button.getFont().deriveFont(28f));
			assertNotNull(button.getAccessibleContext().getAccessibleName());
			assertTrue(button.getPreferredSize().height >= 28);
		}
		assertTrue(pane.getMinimumSize().height > 0);
		assertEquals(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
				pane.getHorizontalScrollBarPolicy());
	}

	@Test
	void contextProjectionUsesSameStableActionIds() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		GeoCeDGWorkspaceController workspace = new GeoCeDGWorkspaceController(app,
				new GeoCeDGActionRegistry(app));
		Set<String> ids = new HashSet<>();
		for (Component component : workspace.createContextMenu().getComponents()) {
			collect(component, ids);
		}
		assertTrue(ids.contains("inspect.definition"));
		assertTrue(ids.contains("result.materialize-selected"));
	}

	@Test
	void layoutRoundTripIsPreferenceOnlyAndRetainsProfileAuthority() throws Exception {
		Perspective original = GeoCeDGProfile.createInitialPerspective();
		String state = GeoCeDGWorkspaceController.encodeLayout(original);
		Perspective restored = GeoCeDGWorkspaceController.restoreLayout(state);
		assertEquals(GeoCeDGWorkspaceController.encodeLayout(original),
				GeoCeDGWorkspaceController.encodeLayout(restored));
		assertEquals("cedg-construction", restored.getId());
		assertFalse(state.contains("construction"));
		assertFalse(state.contains("token"));
	}

	@Test
	void liveHostLayoutRoundTripRetainsHiddenPanelsAndEditedDivider() throws Exception {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		Perspective original = ((LayoutD) app.getGuiManager().getLayout()).createPerspective();
		assertNotNull(original);
		Set<Integer> hidden = new HashSet<>();
		for (DockPanelData panel : original.getDockPanelData()) {
			if (!panel.isVisible()) {
				hidden.add(panel.getViewId());
			}
		}
		assertTrue(hidden.containsAll(Set.of(App.VIEW_EUCLIDIAN2,
				App.VIEW_PROBABILITY_CALCULATOR, App.VIEW_DATA_ANALYSIS)));
		DockSplitPaneData[] panes = original.getSplitPaneData();
		assertTrue(panes.length > 0);
		for (int i = 0; i < panes.length; i++) {
			// An explicit test presentation edit supplies finite ratios even before display.
			panes[i] = new DockSplitPaneData(panes[i].getLocation(), i == 0 ? 0.37 : 0.61,
					panes[i].getOrientation());
		}
		String state = GeoCeDGWorkspaceController.encodeLayout(original);
		Perspective restored = GeoCeDGWorkspaceController.restoreLayout(state);
		assertEquals(state, GeoCeDGWorkspaceController.encodeLayout(restored));
		assertEquals(0.37, restored.getSplitPaneData()[0].getDividerLocation(), 0);
		assertEquals(GeoCeDGProfile.getToolbarDefinition(), restored.getToolbarDefinition());
	}

	@Test
	void savedLayoutWithUnknownViewIdStillFailsClosed() throws Exception {
		String initial = GeoCeDGWorkspaceController.encodeLayout(
				GeoCeDGProfile.createInitialPerspective());
		JSONObject broken = new JSONObject(initial);
		broken.getJSONArray("views").getJSONObject(0).put("id", 987654);
		assertEquals(initial, GeoCeDGWorkspaceController.encodeLayout(
				GeoCeDGWorkspaceController.restoreLayout(broken.toString())));
	}

	@Test
	void corruptedLayoutFailsToSafeProfileWithoutGeometryParsing() {
		Perspective restored = GeoCeDGWorkspaceController.restoreLayout("<construction/>");
		assertEquals("cedg-construction", restored.getId());
		assertEquals(GeoCeDGProfile.getToolbarDefinition(), restored.getToolbarDefinition());
	}

	@Test
	void guideIsPackagedWithDeclaredSourceContent() throws Exception {
		try (var stream = getClass().getResourceAsStream("geocedg_user_guide.md")) {
			assertNotNull(stream);
			assertTrue(stream.readAllBytes().length > 1000);
		}
	}

	@Test
	void workspaceReapplyCancelsToolWithoutConstructionOrUndoMutation() throws Exception {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		G9U1TestApp.eval(app, "A=(1,2)");
		app.getKernel().initUndoInfo();
		String before = constructionXml(app);
		app.setMode(EuclidianConstants.MODE_SEGMENT);
		GeoCeDGWorkspaceController workspace = new GeoCeDGWorkspaceController(app,
				new GeoCeDGActionRegistry(app));
		assertTrue(workspace.applyWorkspace("cedg-construction"), workspace.getLastFailure());
		assertEquals(EuclidianConstants.MODE_MOVE, app.getMode());
		assertEquals(before, constructionXml(app));
		assertFalse(app.getKernel().getConstruction().getUndoManager().undoPossible());
	}

	@Test
	void unavailableWorkspaceChangesNoConstructionOrPresentation() throws Exception {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		GeoCeDGWorkspaceController workspace = new GeoCeDGWorkspaceController(app,
				new GeoCeDGActionRegistry(app));
		String before = app.getXML();
		assertFalse(workspace.applyWorkspace("cedg-dihedral-procedures"));
		assertEquals(before, app.getXML());
	}

	@Test
	void presentationFailureRollsBackWithoutConstructionMutation() throws Exception {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		G9U1TestApp.eval(app, "A=(1,2)");
		LayoutD layout = (LayoutD) app.getGuiManager().getLayout();
		String before = constructionXml(app);
		String toolbar = layout.createPerspective().getToolbarDefinition();
		GeoCeDGWorkspaceController workspace = new GeoCeDGWorkspaceController(app,
				new GeoCeDGActionRegistry(app));
		assertFalse(workspace.applyWorkspace("cedg-construction", next -> {
			app.getGuiManager().setToolBarDefinition("0");
			throw new IllegalStateException("injected presentation failure");
		}));
		assertEquals(before, constructionXml(app));
		assertEquals(toolbar, layout.createPerspective().getToolbarDefinition());
	}

	@Test
	void savedLayoutWithOverlappingLeafFailsClosedBeforeHostApplication() throws Exception {
		String initial = GeoCeDGWorkspaceController.encodeLayout(
				GeoCeDGProfile.createInitialPerspective());
		JSONObject broken = new JSONObject(initial);
		broken.getJSONArray("views").getJSONObject(1).put("location", "3");
		assertEquals(initial, GeoCeDGWorkspaceController.encodeLayout(
				GeoCeDGWorkspaceController.restoreLayout(broken.toString())));
	}

	private static String constructionXml(AppGeoCeDG app) {
		XMLStringBuilder xml = new XMLStringBuilder();
		app.getKernel().getConstruction().getConstructionXML(xml, true);
		return xml.toString();
	}

	@Test
	void unrealizedDockRatiosCannotBePersistedOrInvented() {
		Perspective layout = GeoCeDGProfile.createInitialPerspective();
		assertTrue(GeoCeDGWorkspaceController.isMeasurableLayout(layout));
		for (double invalid : new double[] {Double.NaN, Double.POSITIVE_INFINITY, 0, 1}) {
			layout.setSplitPaneData(new DockSplitPaneData[] {
					new DockSplitPaneData("", invalid, 1)});
			assertFalse(GeoCeDGWorkspaceController.isMeasurableLayout(layout));
		}
	}

	@Test
	void laterDocumentLayoutIsNotMistakenForPreviouslyReappliedWorkspace() throws Exception {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		GeoCeDGWorkspaceController workspace = ((GuiManagerGeoCeDG) app.getGuiManager())
				.getWorkspaceController();
		final String construction = constructionXml(app);
		app.setTmpPerspective(new Perspective());
		assertTrue(workspace.usesDocumentLayout());
		assertTrue(workspace.applyWorkspace("cedg-construction"), workspace.getLastFailure());
		assertFalse(workspace.usesDocumentLayout());
		app.setTmpPerspective(new Perspective());
		assertTrue(workspace.usesDocumentLayout());
		assertEquals(GeoCeDGProfile.getText("Workspace.DocumentLayout", "en"),
				workspace.createWorkspaceSelector().getText());
		app.setTmpPerspective(null);
		assertFalse(workspace.usesDocumentLayout());
		assertEquals(construction, constructionXml(app));
	}

	@Test
	void keyboardFocusScrollsOffscreenFamilyIntoNarrowViewport() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		GeoCeDGWorkspaceController workspace = new GeoCeDGWorkspaceController(app,
				new GeoCeDGActionRegistry(app));
		JScrollPane scroll = workspace.createActionPalette();
		JPanel row = (JPanel) scroll.getViewport().getView();
		row.setSize(row.getPreferredSize());
		row.doLayout();
		scroll.setSize(260, scroll.getPreferredSize().height);
		scroll.doLayout();
		scroll.getViewport().doLayout();
		JButton last = (JButton) row.getComponent(row.getComponentCount() - 1);
		assertFalse(scroll.getViewport().getViewRect().contains(last.getBounds()));
		for (FocusListener listener : last.getFocusListeners()) {
			listener.focusGained(new FocusEvent(last, FocusEvent.FOCUS_GAINED));
		}
		assertTrue(scroll.getViewport().getViewPosition().x > 0);
		assertTrue(scroll.getViewport().getViewRect().intersects(last.getBounds()));
	}

	static void collect(Component component, Set<String> ids) {
		if (component instanceof JMenu) {
			for (Component child : ((JMenu) component).getMenuComponents()) {
				collect(child, ids);
			}
		} else if (component instanceof JMenuItem) {
			Object id = ((JMenuItem) component).getClientProperty(GeoCeDGActionRegistry.ACTION_ID);
			if (id != null) {
				ids.add(id.toString());
			}
		}
	}

	static JMenuItem findItem(Component component, String id) {
		if (component instanceof JMenuItem && id.equals(((JMenuItem) component)
				.getClientProperty(GeoCeDGActionRegistry.ACTION_ID))) {
			return (JMenuItem) component;
		}
		if (component instanceof JMenu) {
			for (Component child : ((JMenu) component).getMenuComponents()) {
				JMenuItem match = findItem(child, id);
				if (match != null) {
					return match;
				}
			}
		}
		return null;
	}
}
