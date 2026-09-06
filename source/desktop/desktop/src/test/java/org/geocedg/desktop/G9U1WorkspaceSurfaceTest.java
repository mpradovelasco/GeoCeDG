/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.gui.view.algebra.AlgebraView.SortMode;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.io.layout.DockPanelData;
import org.geogebra.common.io.layout.DockSplitPaneData;
import org.geogebra.common.io.layout.Perspective;
import org.geogebra.common.main.App;
import org.geogebra.common.main.settings.LabelVisibility;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.properties.impl.general.RoundingIndexProperty;
import org.geogebra.common.util.Util;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.layout.DockManagerD;
import org.geogebra.desktop.gui.layout.LayoutD;
import org.geogebra.desktop.gui.toolbar.ModeToggleMenuD;
import org.geogebra.desktop.gui.toolbar.ToolbarD;
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
	void menuOrderPresentationAndActionIdentityComeFromOneCatalog() throws Exception {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		List<String> sections = new ArrayList<>();
		for (int i = 0; i < bar.getMenuCount(); i++) {
			sections.add((String) bar.getMenu(i).getClientProperty("geocedg.menu.id"));
		}
		assertEquals(List.of("file", "edit", "view", "construction", "options",
				"automation", "help"), sections);
		assertEquals(List.of("document.new", "document.open", "document.open-recent",
				"diagnostic.open-classic", "document.save", "document.save-as",
				"document.print-preview", "export.dxf-2d", "document.close"),
				directActionIds(bar.getMenu(0)));
		assertEquals(List.of("help.input-panel", "help.contextual-action",
				"help.command-list", "help.user-guide", "help.keyboard-shortcuts",
				"help.about-geocedg"), directActionIds(bar.getMenu(6)));

		JSONObject optionsDefinition = GeoCeDGMenuBar.find(
				GeoCeDGProfile.getCatalog().getJSONArray("menu_sections"), "options");
		List<String> optionKinds = new ArrayList<>();
		for (int i = 0; i < optionsDefinition.getJSONArray("entries").length(); i++) {
			optionKinds.add(optionsDefinition.getJSONArray("entries")
					.getJSONObject(i).getString("kind"));
		}
		assertEquals(List.of("group", "sort-by", "separator", "rounding",
				"labeling", "font-size", "separator", "actions", "separator",
				"save-settings"), optionKinds);
		JMenu algebraDisplay = findPresentationGroup(bar.getMenu(4),
				"options-algebra-display");
		assertNotNull(algebraDisplay);
		assertEquals(3, algebraDisplay.getItemCount());
		for (int i = 0; i < algebraDisplay.getItemCount(); i++) {
			assertTrue(algebraDisplay.getItem(i) instanceof JRadioButtonMenuItem);
		}

		Map<String, Integer> occurrences = new LinkedHashMap<>();
		for (Component component : bar.getComponents()) {
			collectOccurrences(component, occurrences);
		}
		assertEquals(((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry().ids(),
				occurrences.keySet());
		occurrences.forEach((id, count) -> assertEquals(1, count, id));
	}

	@Test
	void fileAndEditAreFlatWhileConstructionUsesDeclaredSemanticGroups() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		for (int menuIndex : new int[] {0, 1}) {
			for (Component component : bar.getMenu(menuIndex).getMenuComponents()) {
				assertTrue(component instanceof JMenuItem || component instanceof JSeparator);
				assertFalse(component instanceof JMenu);
			}
		}
		assertNotNull(findPresentationGroup(bar.getMenu(3),
				"construction-semantic-curves"));
		assertNotNull(findPresentationGroup(bar.getMenu(3),
				"construction-lines-vectors"));
		assertNotNull(findPresentationGroup(bar.getMenu(3), "construction-polygons"));
		assertNotNull(findPresentationGroup(bar.getMenu(3), "construction-derived"));
		assertNotNull(findItem(bar.getMenu(3), "presentation.text"));
		assertNotNull(findItem(bar.getMenu(3), "presentation.image"));
		assertNull(findItem(bar.getMenu(2), "presentation.text"));
		assertNull(findItem(bar.getMenu(2), "presentation.image"));
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
	void legacyV1CatalogBuildsSafeRealFrontendMenuWithoutV2Arrays() throws Exception {
		JSONObject legacy;
		try (InputStream stream = getClass().getResourceAsStream(
				"application-profile-v1.yml")) {
			assertNotNull(stream);
			legacy = new JSONObject(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
		}
		assertEquals(1, legacy.getInt("schema_version"));
		assertFalse(legacy.has("presentation_groups"));
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		GeoCeDGMenuBar bar = new GeoCeDGMenuBar(app);
		for (int i = 0; i < 2; i++) {
			bar.populateFromCatalog(legacy);
			assertEquals(8, bar.getMenuCount());
			JMenu diagnostic = bar.getMenu(7);
			assertEquals("GeoCeDG v1", diagnostic.getText());
			assertFalse(diagnostic.isEnabled());
			assertFalse(diagnostic.getToolTipText().isBlank());
			assertEquals(diagnostic.getText(),
					diagnostic.getAccessibleContext().getAccessibleName());
			assertEquals(diagnostic.getToolTipText(),
					diagnostic.getAccessibleContext().getAccessibleDescription());
		}
	}

	@Test
	void hostOptionsAndViewsReuseLiveHostStateWithoutCatalogDuplication() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		JMenu options = bar.getMenu(4);
		JMenu algebraDisplay = findPresentationGroup(options,
				"options-algebra-display");
		assertEquals(1, countSelected(algebraDisplay));
		assertEquals(1, countSelectedHostControls(options, "sort-by."));
		assertEquals(1, countSelectedHostControls(options, "rounding."));
		assertEquals(1, countSelectedHostControls(options, "labeling."));
		assertEquals(1, countSelectedHostControls(options, "font-size."));
		JMenuItem typeSort = findHostControl(bar.getMenu(4), "sort-by.TYPE");
		assertNotNull(typeSort);
		typeSort.doClick();
		assertEquals(SortMode.TYPE, app.getSettings().getAlgebra().getTreeMode());
		assertTrue(typeSort.isSelected());
		JMenuItem definition = findItem(options, "algebra.description.definition");
		assertNotNull(definition);
		definition.doClick();
		app.setLocale(new java.util.Locale("es"));
		bar.initMenubar();
		options = bar.getMenu(4);
		assertTrue(findItem(options, "algebra.description.definition").isSelected());
		assertEquals(1, countSelected(findPresentationGroup(options,
				"options-algebra-display")));
		assertTrue(findHostControl(options, "sort-by.TYPE").isSelected());
		assertEquals(1, countSelectedHostControls(options, "sort-by."));
		assertNotNull(findHostControl(bar.getMenu(4), "rounding.0"));
		assertNotNull(findHostControl(bar.getMenu(4), "labeling.Automatic"));
		assertNotNull(findHostControl(bar.getMenu(4), "font-size.12"));
		assertNotNull(findHostControl(bar.getMenu(4), "save-settings"));
		assertNotNull(findHostControl(bar.getMenu(2), "view." + App.VIEW_ALGEBRA));
		assertNull(findHostControl(bar.getMenu(2), "view." + App.VIEW_EUCLIDIAN3D));
	}

	@Test
	void hostViewCheckboxAndSortRadioRefreshFromAuthorityWhenSubmenusOpen() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		JMenu views = GeoCeDGHostMenuFactory.views(app);
		JMenuItem algebra = findHostControl(views, "view." + App.VIEW_ALGEBRA);
		boolean initiallyVisible = app.getGuiManager().showView(App.VIEW_ALGEBRA);
		assertEquals(initiallyVisible, algebra.isSelected());
		app.getGuiManager().setShowView(!initiallyVisible, App.VIEW_ALGEBRA);
		assertEquals(initiallyVisible, algebra.isSelected());
		openMenu(views);
		assertEquals(!initiallyVisible, algebra.isSelected());

		JMenu sort = GeoCeDGHostMenuFactory.sortBy(app);
		SortMode initialSort = app.getSettings().getAlgebra().getTreeMode();
		SortMode changedSort = initialSort == SortMode.TYPE ? SortMode.LAYER : SortMode.TYPE;
		app.getSettings().getAlgebra().setTreeMode(changedSort);
		JMenuItem changed = findHostControl(sort, "sort-by." + changedSort.name());
		assertFalse(changed.isSelected());
		openMenu(sort);
		assertTrue(changed.isSelected());
		assertEquals(1, countSelectedHostControls(sort, "sort-by."));
	}

	@Test
	void hostPreferenceRadiosRefreshFromAuthorityWhenSubmenusOpen() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);

		JMenu rounding = GeoCeDGHostMenuFactory.rounding(app);
		RoundingIndexProperty roundingAuthority = new RoundingIndexProperty(app,
				app.getLocalization());
		int currentRounding = roundingAuthority.getValues().indexOf(
				roundingAuthority.getValue());
		int nextRounding = (currentRounding + 1) % roundingAuthority.getValues().size();
		roundingAuthority.setIndex(nextRounding);
		openMenu(rounding);
		assertTrue(findHostControl(rounding, "rounding." + nextRounding).isSelected());
		assertEquals(1, countSelectedHostControls(rounding, "rounding."));

		JMenu labeling = GeoCeDGHostMenuFactory.labeling(app);
		LabelVisibility currentLabeling = app.getSettings().getLabelSettings()
				.getLabelVisibilityForMenu();
		LabelVisibility nextLabeling = currentLabeling == LabelVisibility.AlwaysOff
				? LabelVisibility.AlwaysOn : LabelVisibility.AlwaysOff;
		app.setLabelingStyle(nextLabeling.getValue());
		openMenu(labeling);
		assertTrue(findHostControl(labeling,
				"labeling." + nextLabeling.name()).isSelected());
		assertEquals(1, countSelectedHostControls(labeling, "labeling."));

		JMenu fontSize = GeoCeDGHostMenuFactory.fontSize(app);
		int nextFontSize = Util.menuFontSizes(0) == app.getFontSize()
				? Util.menuFontSizes(1) : Util.menuFontSizes(0);
		app.setFontSize(nextFontSize, false);
		openMenu(fontSize);
		assertTrue(findHostControl(fontSize,
				"font-size." + nextFontSize).isSelected());
		assertEquals(1, countSelectedHostControls(fontSize, "font-size."));
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
	void toolbarUsesExactProfileOrderAndNormalLastUsedMixedFlyoutsAtHighDpi() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		app.setFontSize(32, false);
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		GeoCeDGWorkspaceController workspace = new GeoCeDGWorkspaceController(app, registry);
		GeoCeDGToolbarContainer container = new GeoCeDGToolbarContainer(app, workspace);
		container.buildGui();
		final ToolbarD toolbar = container.getToolbar(-1);
		Map<String, List<String>> expected = new LinkedHashMap<>();
		expected.put("edit-selection", List.of("construction.move",
				"construction.move-rotate"));
		expected.put("construction-relations", List.of("construction.point",
				"construction.point-on-object", "construction.attach-detach",
				"relation.intersect"));
		expected.put("construction-lines-vectors", List.of("construction.line",
				"construction.segment", "construction.ray", "construction.vector",
				"construction.fixed-segment", "construction.vector-from-point"));
		expected.put("construction-polygons", List.of("construction.polygon",
				"construction.polyline", "construction.regular-polygon",
				"construction.rigid-polygon", "construction.vector-polygon"));
		expected.put("construction-derived", List.of("construction.parallel-line",
				"construction.perpendicular-line", "construction.midpoint",
				"construction.perpendicular-bisector", "construction.angle-bisector",
				"parameter.fixed-angle", "relation.tangent"));
		expected.put("construction-circles-conics", List.of("curve.circle-two-points",
				"curve.circle-three-points", "curve.circle-center-radius",
				"curve.arc-center", "curve.conic-five-points", "curve.ellipse",
				"curve.parabola"));
		expected.put("construction-semantic-curves", List.of("semantic.locus-v2.create",
				"semantic.spline-v2.create", "semantic.locus-v2.point-explicit"));
		expected.put("construction-metrics", List.of("measure.angle",
				"measure.distance-length", "measure.locus-v2-total-length",
				"measure.locus-v2-partial-length"));
		expected.put("construction-transforms", List.of("transform.reflect-point",
				"transform.reflect-line", "transform.translate-vector",
				"transform.rotate-angle", "transform.dilate-point"));
		expected.put("construction-parameters", List.of("parameter.slider",
				"parameter.checkbox", "parameter.button", "parameter.input-box"));
		expected.put("view-navigation", List.of("navigation.pan-view",
				"navigation.zoom-window", "navigation.zoom-in", "navigation.zoom-out",
				"presentation.copy-style"));
		List<Component> rendered = new ArrayList<>();
		for (Component component : toolbar.getComponents()) {
			if (component instanceof JToolBar.Separator) {
				continue;
			}
			rendered.add(component);
		}
		assertEquals(new ArrayList<>(expected.keySet()), rendered.stream()
				.map(component -> (String) ((JComponent) component)
						.getClientProperty("geocedg.presentation.group.id")).toList());
		toolbar.setSize(toolbar.getPreferredSize());
		toolbar.doLayout();
		JComponent nativeGroup = (JComponent) rendered.get(0);
		JToggleButton nativeButton = GeoCeDGToolbarContainer.toolbarButton(nativeGroup);
		for (Component component : rendered) {
			JComponent groupComponent = (JComponent) component;
			groupComponent.setSize(groupComponent.getPreferredSize());
			groupComponent.doLayout();
			String group = (String) groupComponent.getClientProperty(
					"geocedg.presentation.group.id");
			assertEquals(expected.get(group),
					groupComponent.getClientProperty("geocedg.toolbar.action.ids"));
			JToggleButton button = GeoCeDGToolbarContainer.toolbarButton(groupComponent);
			assertNativeToolbarGeometry(nativeButton, button);
			assertEquals(nativeGroup.getBounds().y, groupComponent.getBounds().y, group);
			assertEquals(nativeGroup.getBounds().height,
					groupComponent.getBounds().height, group);
			if (!group.equals("construction-semantic-curves")
					&& !group.equals("view-navigation")) {
				assertTrue(component instanceof ModeToggleMenuD, group);
				continue;
			}
			assertTrue(component instanceof JPanel, group);
			JPopupMenu popup = (JPopupMenu) button.getClientProperty("geocedg.toolbar.popup");
			assertNotNull(popup);
			assertEquals(expected.get(group).size(), popup.getComponentCount());
			for (int index = 0; index < popup.getComponentCount(); index++) {
				JMenuItem item = (JMenuItem) popup.getComponent(index);
				String actionId = expected.get(group).get(index);
				assertEquals(actionId,
						item.getClientProperty(GeoCeDGActionRegistry.ACTION_ID));
				assertSame(registry.get(actionId), item.getAction());
				assertNotNull(item.getIcon(), actionId);
			}
			JMenuItem lastUsed = (JMenuItem) popup.getComponent(1);
			String activeId = expected.get(group).get(1);
			workspace.selectProfileFlyoutAction(button, activeId);
			assertEquals(activeId,
					button.getClientProperty("geocedg.toolbar.active.action.id"));
			assertSame(lastUsed.getIcon(), button.getIcon());
			assertNull(button.getText());
			assertEquals(registry.get(activeId).getValue(javax.swing.Action.NAME),
					button.getAccessibleContext().getAccessibleName());
			assertNotNull(button.getAccessibleContext().getAccessibleName());
			assertNativeToolbarGeometry(nativeButton, button);
		}
		assertFalse(toolbar.isFloatable());
	}

	private static void assertNativeToolbarGeometry(JToggleButton reference,
			JToggleButton actual) {
		assertEquals(reference.getPreferredSize(), actual.getPreferredSize());
		assertEquals(reference.getMinimumSize(), actual.getMinimumSize());
		assertEquals(reference.getMaximumSize(), actual.getMaximumSize());
		assertEquals(reference.getMargin(), actual.getMargin());
		assertSame(reference.getBorder(), actual.getBorder());
		assertEquals(reference.getAlignmentX(), actual.getAlignmentX());
		assertEquals(reference.getAlignmentY(), actual.getAlignmentY());
		assertEquals(reference.getHorizontalAlignment(), actual.getHorizontalAlignment());
		assertEquals(reference.getVerticalAlignment(), actual.getVerticalAlignment());
		assertEquals(reference.getHorizontalTextPosition(),
				actual.getHorizontalTextPosition());
		assertEquals(reference.getVerticalTextPosition(), actual.getVerticalTextPosition());
		assertEquals(reference.getIconTextGap(), actual.getIconTextGap());
		assertEquals(reference.getComponentOrientation(), actual.getComponentOrientation());
	}

	@Test
	void toolbarExtensionsPreserveTheHostHelpControlsAtTheFarEdge() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		GeoCeDGWorkspaceController workspace = new GeoCeDGWorkspaceController(app,
				new GeoCeDGActionRegistry(app));
		GeoCeDGToolbarContainer container = new GeoCeDGToolbarContainer(app, workspace);
		container.buildGui();
		BorderLayout layout = (BorderLayout) container.getLayout();
		Component tools = layout.getLayoutComponent(app.getLocalization().borderWest());
		Component helpControls = layout.getLayoutComponent(app.getLocalization().borderEast());
		assertNotNull(tools);
		assertNotNull(helpControls);
		assertNotSame(tools, helpControls);
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
		assertEquals(52, toolbarIds.size());
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

	private static List<String> directActionIds(JMenu menu) {
		List<String> ids = new ArrayList<>();
		for (Component component : menu.getMenuComponents()) {
			if (component instanceof JMenuItem && !(component instanceof JMenu)) {
				Object id = ((JMenuItem) component).getClientProperty(
						GeoCeDGActionRegistry.ACTION_ID);
				if (id != null) {
					ids.add(id.toString());
				}
			}
		}
		return ids;
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

	private static JMenu findPresentationGroup(JMenu menu, String id) {
		for (Component component : menu.getMenuComponents()) {
			if (component instanceof JMenu && id.equals(((JMenu) component)
					.getClientProperty("geocedg.presentation.group.id"))) {
				return (JMenu) component;
			}
		}
		return null;
	}

	private static JMenuItem findHostControl(Component component, String id) {
		if (component instanceof JMenuItem && id.equals(((JMenuItem) component)
				.getClientProperty(GeoCeDGHostMenuFactory.HOST_CONTROL_ID))) {
			return (JMenuItem) component;
		}
		if (component instanceof JMenu) {
			for (Component child : ((JMenu) component).getMenuComponents()) {
				JMenuItem found = findHostControl(child, id);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	private static int countSelected(Component component) {
		int selected = component instanceof JMenuItem
				&& ((JMenuItem) component).isSelected() ? 1 : 0;
		if (component instanceof JMenu) {
			for (Component child : ((JMenu) component).getMenuComponents()) {
				selected += countSelected(child);
			}
		}
		return selected;
	}

	private static int countSelectedHostControls(Component component, String prefix) {
		int selected = 0;
		if (component instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) component;
			Object id = item.getClientProperty(GeoCeDGHostMenuFactory.HOST_CONTROL_ID);
			if (id instanceof String && ((String) id).startsWith(prefix)
					&& item.isSelected()) {
				selected++;
			}
		}
		if (component instanceof JMenu) {
			for (Component child : ((JMenu) component).getMenuComponents()) {
				selected += countSelectedHostControls(child, prefix);
			}
		}
		return selected;
	}

	private static void openMenu(JMenu menu) {
		MenuEvent event = new MenuEvent(menu);
		for (MenuListener listener : menu.getMenuListeners()) {
			listener.menuSelected(event);
		}
	}
}
