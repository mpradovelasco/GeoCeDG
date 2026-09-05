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
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.io.layout.DockPanelData;
import org.geogebra.common.io.layout.DockSplitPaneData;
import org.geogebra.common.io.layout.Perspective;
import org.geogebra.common.main.App;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.layout.DockManagerD;
import org.geogebra.desktop.gui.layout.LayoutD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.DockBarInterface;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1WorkspaceSurfaceTest {

	@BeforeAll
	static void initializeDesktop() {
		G9U1ActionRegistryTest.initializeDesktop();
	}

	@Test
	void embeddedHostTeardownReleasesOnlyNewDockListenersAndLazyFrames() throws Exception {
		try (G9U1TestApp.GlobalRoots outer = G9U1TestApp.GlobalRoots.capture()) {
			AppGeoCeDG existing = G9U1TestApp.create();
			final DockManagerD existingManager = ((LayoutD) existing.getGuiManager().getLayout())
					.getDockManager();
			existing.getDrawEquation();
			final GeoGebraFrame existingFrame = (GeoGebraFrame) existing.getFrame();
			assertTrue(G9U1TestApp.dockManagers().contains(existingManager));
			assertTrue(GeoGebraFrame.getInstances().contains(existingFrame));

			final AppGeoCeDG added;
			final DockManagerD addedManager;
			final GeoGebraFrame addedFrame;
			try (G9U1TestApp.GlobalRoots inner = G9U1TestApp.GlobalRoots.capture()) {
				added = G9U1TestApp.create();
				addedManager = ((LayoutD) added.getGuiManager().getLayout()).getDockManager();
				assertSame(added, addedManager.getLayout().getApplication());
				assertTrue(G9U1TestApp.dockManagers().contains(addedManager));
				added.getDrawEquation();
				addedFrame = (GeoGebraFrame) added.getFrame();
				assertSame(added, addedFrame.getApplication());
				assertTrue(GeoGebraFrame.getInstances().contains(addedFrame));
				assertFalse(addedFrame.isVisible());
				assertFalse(addedFrame.isDisplayable());
			}
			assertFalse(G9U1TestApp.dockManagers().contains(addedManager));
			assertFalse(GeoGebraFrame.getInstances().contains(addedFrame));
			assertTrue(G9U1TestApp.dockManagers().contains(existingManager));
			assertTrue(GeoGebraFrame.getInstances().contains(existingFrame));
			assertSame(existing, existingManager.getLayout().getApplication());
			assertSame(existing, existingFrame.getApplication());
			assertSame(added, addedManager.getLayout().getApplication());
			assertSame(added, addedFrame.getApplication());
		}
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
		assertEquals(7, bar.getMenuCount());
		assertTrue(ids.contains("navigation.zoom-window"));
	}

	@Test
	void menuOrderOptionsProjectionAndActionIdentityComeFromOneCatalog() throws Exception {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		List<String> sections = new ArrayList<>();
		for (int i = 0; i < bar.getMenuCount(); i++) {
			sections.add((String) bar.getMenu(i).getClientProperty("geocedg.menu.id"));
		}
		assertEquals(List.of("file", "edit", "view", "construction", "options",
				"automation", "help"), sections);

		JMenu options = bar.getMenu(4);
		List<String> projected = new ArrayList<>();
		for (Component component : options.getMenuComponents()) {
			if (component instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) component;
				projected.add((String) item.getClientProperty(
						GeoCeDGActionRegistry.ACTION_ID));
				assertSame(((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry()
						.get(projected.get(projected.size() - 1)), item.getAction());
			}
		}
		JSONObject optionsDefinition = GeoCeDGMenuBar.find(
				GeoCeDGProfile.getCatalog().getJSONArray("menu_sections"), "options");
		assertEquals(GeoCeDGProfile.strings(optionsDefinition.getJSONArray("action_ids")),
				projected);

		Map<String, Integer> occurrences = new LinkedHashMap<>();
		for (Component component : bar.getComponents()) {
			collectOccurrences(component, occurrences);
		}
		assertEquals(((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry().ids(),
				occurrences.keySet());
		occurrences.forEach((id, count) -> assertEquals(1, count, id));
	}

	@Test
	void repeatedMenuRebuildKeepsOneProjectionPerDeclaredPlacement() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		for (int i = 0; i < 3; i++) {
			bar.initMenubar();
			bar.updateFonts();
			assertEquals(7, bar.getMenuCount());
		}
	}

	@Test
	void constructionStartupDoesNotOpenClassicChooserButClassicStillDoes() throws Exception {
		AppGeoCeDG product = spy(G9U1ActionRegistryTest.app(true));
		DockBarInterface productBar = mock(DockBarInterface.class);
		doReturn(productBar).when(product).getDockBar();
		product.setAllowPopups(true);
		product.setShowDockBar(true, false);
		final String before = product.getXML();
		product.showPopUps();

		AppD classic = mock(AppD.class, CALLS_REAL_METHODS);
		DockBarInterface classicBar = mock(DockBarInterface.class);
		doReturn(classicBar).when(classic).getDockBar();
		doReturn(true).when(classic).isAllowPopups();
		doReturn(true).when(classic).isShowDockBar();
		classic.showPopUps();
		EventQueue.invokeAndWait(() -> {
			// Flush the inherited deferred startup callback in both applications.
		});
		verify(product).showPerspectivePopup();
		verify(productBar, never()).showPopup();
		verify(classicBar).showPopup();
		assertEquals(before, product.getXML());
	}

	@Test
	void compactProductToolbarUsesOnlyDeclaredNonModeActionsAtHighDpi() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		GeoCeDGWorkspaceController workspace = new GeoCeDGWorkspaceController(app,
				new GeoCeDGActionRegistry(app));
		JToolBar toolbar = workspace.createProductToolbar();
		assertEquals(2, toolbar.getComponentCount());
		Set<String> ids = new HashSet<>();
		for (Component component : toolbar.getComponents()) {
			JButton button = (JButton) component;
			ids.add((String) button.getClientProperty(GeoCeDGActionRegistry.ACTION_ID));
			button.setFont(button.getFont().deriveFont(28f));
			assertNull(button.getClientProperty("geocedg.family.id"));
			assertNotNull(button.getAccessibleContext().getAccessibleName());
			assertTrue(button.getPreferredSize().height >= 28);
		}
		assertEquals(Set.of("semantic.spline-v2.create", "navigation.zoom-window"), ids);
		assertFalse(toolbar.isFloatable());
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
		assertFalse(ids.contains("view.properties"));
		assertFalse(ids.contains("semantic.curve.inspect-definition"));
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
		assertEquals(Boolean.TRUE, workspace.createWorkspaceMenu()
				.getClientProperty("geocedg.document-layout"));
		app.setTmpPerspective(null);
		assertFalse(workspace.usesDocumentLayout());
		assertEquals(construction, constructionXml(app));
	}

	@Test
	void realApplicationMenuIsAboveContentAndToolbarIsItsStrictSubset() throws Exception {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		JPanel content = new JPanel();
		JPanel host = AppD.getMenuBarPanel(app, content);
		assertSame(bar, ((BorderLayout) host.getLayout()).getLayoutComponent(BorderLayout.NORTH));
		assertSame(content, ((BorderLayout) host.getLayout())
				.getLayoutComponent(BorderLayout.CENTER));
		Set<String> menuIds = new HashSet<>();
		for (Component component : bar.getComponents()) {
			collect(component, menuIds);
		}
		Set<String> toolbarIds = new HashSet<>();
		var clusters = GeoCeDGProfile.getCatalog().getJSONArray("clusters");
		for (int i = 0; i < clusters.length(); i++) {
			toolbarIds.addAll(GeoCeDGProfile.strings(clusters.getJSONObject(i)
					.getJSONArray("toolbar_action_ids")));
		}
		assertEquals(34, toolbarIds.size());
		assertEquals(110, menuIds.size());
		assertTrue(menuIds.containsAll(toolbarIds));
		GeoCeDGActionRegistry registry = ((GuiManagerGeoCeDG) app.getGuiManager())
				.getActionRegistry();
		for (String id : toolbarIds) {
			JMenuItem item = null;
			for (Component component : bar.getComponents()) {
				JMenuItem found = findItem(component, id);
				if (found != null) {
					item = found;
					break;
				}
			}
			assertNotNull(item, id);
			assertSame(registry.get(id), item.getAction(), id);
			assertNotNull(item.getToolTipText(), id);
		}
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

	private static void collectOccurrences(Component component,
			Map<String, Integer> occurrences) {
		if (component instanceof JMenu) {
			for (Component child : ((JMenu) component).getMenuComponents()) {
				collectOccurrences(child, occurrences);
			}
		} else if (component instanceof JMenuItem) {
			Object id = ((JMenuItem) component).getClientProperty(
					GeoCeDGActionRegistry.ACTION_ID);
			if (id != null) {
				occurrences.merge(id.toString(), 1, Integer::sum);
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
