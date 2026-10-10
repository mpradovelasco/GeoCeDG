/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.geocedg.desktop.PreG9BR4SplineV2AuthoringTest.hit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.gui.dialog.ToolCreationDialogModel;
import org.geogebra.common.gui.toolbar.ToolBar;
import org.geogebra.common.gui.view.algebra.AlgebraView.SortMode;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.error.ErrorHelper;
import org.geogebra.common.plugin.Event;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.plugin.script.GgbScript;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.view.algebra.AlgebraViewD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * PRE-G9B-R6-plus-A-1 focal contract for the session working layer, its
 * one-shot mode, the status bar and the Algebra View layer groups.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR6PlusA1LayerWorkspaceTest {
	private static final String ACTION = "construction.working-layer";

	@TempDir
	Path temporary;

	// ---------------------------------------------------------------- T-WL-ROUTES

	@Test
	void algebraInputAndInputBarCreateOnTheWorkingLayer() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(3);
		assertEquals(3, eval(app, "A=(1,1)").getLayer());
		PreG9BR6ScriptHarness.Sink errors = new PreG9BR6ScriptHarness.Sink();
		assertNotNull(PreG9BR6ScriptHarness.submit(app, "B=(2,1)", errors), errors.joined());
		assertEquals(3, lookup(app, "B").getLayer());
		assertEquals(3, eval(app, "c=Circle(A,B)").getLayer());
	}

	@Test
	void toolsCreateOnTheWorkingLayer() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement a = eval(app, "A=(0,0)");
		GeoElement b = eval(app, "B=(3,1)");
		app.getLayerWorkspace().setWorkingLayer(6);
		app.setMode(EuclidianConstants.MODE_JOIN);
		GeoCeDGEuclidianController controller = controller(app);
		AtomicInteger changes = new AtomicInteger();
		controller.processMode(hit(a), false, false, changed -> {
			if (changed) {
				changes.incrementAndGet();
			}
		});
		controller.processMode(hit(b), false, false, changed -> {
			if (changed) {
				changes.incrementAndGet();
			}
		});
		assertEquals(1, changes.get());
		GeoElement line = only(app, g -> g.isGeoLine());
		assertEquals(6, line.getLayer());
		assertEquals(0, a.getLayer(), "inputs keep their own layer");
	}

	@Test
	void ggbScriptAndNestedExecuteCreateOnTheWorkingLayer() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement trigger = eval(app, "T=(9,9)");
		app.getLayerWorkspace().setWorkingLayer(5);
		assertTrue(new GgbScript(app, "P=(5,5)").run(new Event(EventType.CLICK, trigger)));
		assertTrue(new GgbScript(app, "Execute({\"R=(6,6)\"})")
				.run(new Event(EventType.CLICK, trigger)));
		assertEquals(5, lookup(app, "P").getLayer());
		assertEquals(5, lookup(app, "R").getLayer());
	}

	@Test
	void macroOutputsCreateOnTheWorkingLayer() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoElement a = eval(app, "A=(1,1)");
		GeoElement b = eval(app, "B=(2,2)");
		GeoElement f = eval(app, "f=Line(A,B)");
		ToolCreationDialogModel builder = new ToolCreationDialogModel(app, () -> {
			// no dialog to update
		});
		builder.addToInput(a);
		builder.addToInput(b);
		builder.addToOutput(f);
		builder.createTool();
		builder.finish(app, "WorkLine", "WorkLine", "two points", false, null);
		app.getLayerWorkspace().setWorkingLayer(4);
		GeoElement g = eval(app, "g=WorkLine((1,3),(2,3))");
		assertEquals(4, g.getLayer());
	}

	@Test
	void locusV2AndSplineV2CreateOnTheWorkingLayer() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(7);
		eval(app, "s=0");
		eval(app, "Q=(s,0)");
		eval(app, "D={false,{-2,2,true,true}}");
		GeoElement locus = eval(app, "L=LocusV2(Q,s,D)");
		assertInstanceOf(GeoLocusV2.class, locus);
		assertEquals(7, locus.getLayer(), "closes the Locus V2 layer-0 gap");
		eval(app, "A=(0,0)");
		eval(app, "B=(1,1)");
		eval(app, "C=(2,0)");
		eval(app, "E=(3,1)");
		GeoElement spline = eval(app, "S=SplineV2({A,B,C,E},3)");
		assertInstanceOf(GeoLocusV2.class, spline);
		assertEquals(7, spline.getLayer());
	}

	// --------------------------------------------------------------- T-WL-REBUILD

	@Test
	void undoAndRedoRestoreStoredLayersAndNeverUseTheWorkingLayer() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		workspace.setWorkingLayer(6);
		eval(app, "A=(1,1)");
		workspace.setWorkingLayer(1);
		GeoElement n = eval(app, "n=x(A)+1");
		assertEquals(1, n.getLayer(), "interactive creation");
		UndoManagerD undo = undo(app);
		baseline(undo);
		workspace.setWorkingLayer(2);
		eval(app, "B=(2,2)");
		awaitStore(app, undo);
		workspace.setWorkingLayer(8);

		app.getKernel().undo();
		assertEquals(8, workspace.getWorkingLayer(), "undo never resets the session");
		assertEquals(6, lookup(app, "A").getLayer());
		assertEquals(null, app.getKernel().lookupLabel("B"));
		GeoElement reloaded = lookup(app, "n");
		int upstream = Math.min(app.getConfig().getMaxLayer() - 1, app.getMaxLayerUsed());
		assertEquals(upstream, reloaded.getLayer(),
				"a numeric without <layer> takes the host value on reload");
		assertNotEquals(workspace.getWorkingLayer(), reloaded.getLayer());
		app.getKernel().redo();
		assertEquals(2, lookup(app, "B").getLayer());
		assertEquals(8, workspace.getWorkingLayer());
	}

	@Test
	void redefinitionRebuildKeepsStoredLayers() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		workspace.setWorkingLayer(2);
		eval(app, "C=(0,0)");
		eval(app, "D=(4,0)");
		GeoElement a = eval(app, "A=(1,1)");
		workspace.setWorkingLayer(4);
		eval(app, "B=A+(1,0)");
		workspace.setWorkingLayer(7);
		app.getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(a,
				"Midpoint(C,D)", new EvalInfo(true, true), false, null, ErrorHelper.silent());
		assertFalse(lookup(app, "A").isIndependent(), "the rebuild happened");
		assertEquals(2, lookup(app, "A").getLayer());
		assertEquals(4, lookup(app, "B").getLayer());
		assertEquals(7, workspace.getWorkingLayer());
	}

	// ------------------------------------------------------------- T-WL-LIFECYCLE

	@Test
	void newStartsAtZeroAndClearsHiddenLayers() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		assertEquals(0, workspace.getWorkingLayer(), "a fresh session is a New document");
		workspace.setWorkingLayer(5);
		workspace.setLayerHidden(2, true);
		eval(app, "A=(1,1)");
		app.setSaved();
		app.fileNew();
		assertEquals(0, workspace.getWorkingLayer());
		assertTrue(workspace.getHiddenLayers().isEmpty());
	}

	@Test
	void openInitializesFromTheDocumentAndWritesNothing() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		workspace.setWorkingLayer(2);
		eval(app, "A=(1,1)");
		workspace.setWorkingLayer(6);
		eval(app, "B=(2,1)");
		workspace.setWorkingLayer(1);
		eval(app, "n=x(A)+1");
		GeoElement hiddenObject = eval(app, "C=(3,1)");
		hiddenObject.setEuclidianVisible(false);
		Path file = temporary.resolve("layers.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		String saved = constructionXml(app);

		AppGeoCeDG reopened = G9U1TestApp.create();
		reopened.getLayerWorkspace().setWorkingLayer(9);
		reopened.getLayerWorkspace().setLayerHidden(3, true);
		assertTrue(reopened.loadFile(file.toFile(), false));
		assertEquals(6, reopened.getLayerWorkspace().getWorkingLayer(),
				"highest layer used by a drawable object");
		assertTrue(reopened.getLayerWorkspace().getHiddenLayers().isEmpty());
		assertFalse(lookup(reopened, "C").isEuclidianVisible(),
				"object visibility is never touched");
		assertEquals(saved, constructionXml(reopened), "Open writes nothing");
		Path again = temporary.resolve("again.cedg");
		assertTrue(((GuiManagerGeoCeDG) reopened.getGuiManager()).saveAsTo(again.toFile()));
		AppGeoCeDG third = G9U1TestApp.create();
		assertTrue(third.loadFile(again.toFile(), false));
		assertEquals(saved, constructionXml(third), "an unchanged document re-saves identically");
	}

	@Test
	void anOpenedDocumentWithoutDrawablesStartsAtZero() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		Path file = temporary.resolve("empty.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = G9U1TestApp.create();
		reopened.getLayerWorkspace().setWorkingLayer(8);
		assertTrue(reopened.loadFile(file.toFile(), false));
		assertEquals(0, reopened.getLayerWorkspace().getWorkingLayer());
	}

	// -------------------------------------------------------------------- T-PASTE

	@Test
	void pasteKeepsTheCopiedLayersForEveryWorkingLayer() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(2);
		GeoElement a = eval(app, "A=(1,1)");
		for (int working = 0; working <= app.getLayerWorkspace().getMaxLayer(); working++) {
			app.getLayerWorkspace().setWorkingLayer(working);
			Set<GeoElement> before = new HashSet<>(app.getKernel().getConstruction()
					.getGeoSetConstructionOrder());
			InternalClipboard.duplicate(app, List.of(a));
			List<GeoElement> created = new ArrayList<>(app.getKernel().getConstruction()
					.getGeoSetConstructionOrder());
			created.removeAll(before);
			assertFalse(created.isEmpty(), "paste created a copy");
			for (GeoElement copy : created) {
				if (copy.isGeoPoint()) {
					assertEquals(2, copy.getLayer(), "working layer " + working);
				}
			}
		}
	}

	// --------------------------------------------------------------------- T-MODE

	@Test
	void theModeIsAGeoCeDGMoveGroupModeWithLocalizedText() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		assertEquals(141, EuclidianConstants.MODE_WORKING_LAYER);
		GeoCeDGProfile.ActionDefinition definition = GeoCeDGProfile.getAction(ACTION);
		assertEquals("upstream-mode", definition.kind());
		assertEquals(EuclidianConstants.MODE_WORKING_LAYER, definition.mode().intValue());
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		assertEquals("Working Layer", registry.get(ACTION).getValue(Action.NAME));
		assertTrue(app.getToolHelp(EuclidianConstants.MODE_WORKING_LAYER)
				.contains("working layer"));
		app.setLocale(Locale.forLanguageTag("es"));
		assertEquals("Capa de trabajo", app.getToolName(EuclidianConstants.MODE_WORKING_LAYER));
		registry.invoke(ACTION, new ActionEvent(this, 1, "tool"));
		assertEquals(EuclidianConstants.MODE_WORKING_LAYER, app.getMode());
	}

	@Test
	void aClickOnAnObjectAdoptsItsLayerAndReturnsToMove() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(4);
		GeoElement a = eval(app, "A=(1,1)");
		app.getLayerWorkspace().setWorkingLayer(1);
		AtomicInteger dialogs = new AtomicInteger();
		app.setWorkingLayerChooser(current -> {
			dialogs.incrementAndGet();
			return 9;
		});
		app.setMode(EuclidianConstants.MODE_WORKING_LAYER);
		AtomicInteger stores = new AtomicInteger();
		controller(app).processMode(hit(a), false, false, changed -> {
			if (changed) {
				stores.incrementAndGet();
			}
		});
		assertEquals(4, app.getLayerWorkspace().getWorkingLayer());
		assertEquals(EuclidianConstants.MODE_MOVE, app.getMode(), "one-shot (AQ-L4)");
		assertEquals(0, dialogs.get());
		assertEquals(0, stores.get(), "no construction change, no undo point");
	}

	@Test
	void axesAreNotObjectsOfTheMode() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(3);
		AtomicInteger dialogs = new AtomicInteger();
		app.setWorkingLayerChooser(current -> {
			dialogs.incrementAndGet();
			return null;
		});
		app.setMode(EuclidianConstants.MODE_WORKING_LAYER);
		Hits hits = new Hits();
		hits.add(app.getKernel().getXAxis());
		controller(app).processMode(hits, false, false, null);
		assertEquals(1, dialogs.get(), "an axis click is an empty-space click");
		assertEquals(3, app.getLayerWorkspace().getWorkingLayer());
	}

	@Test
	void emptySpaceOpensTheBoundedChooserAndCancelChangesNothing() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(2);
		AtomicReference<Integer> answer = new AtomicReference<>(null);
		AtomicInteger offered = new AtomicInteger(-1);
		app.setWorkingLayerChooser(current -> {
			offered.set(current);
			return answer.get();
		});
		app.setMode(EuclidianConstants.MODE_WORKING_LAYER);
		controller(app).processMode(new Hits(), false, false, null);
		assertEquals(2, offered.get(), "the chooser starts at the current layer");
		assertEquals(2, app.getLayerWorkspace().getWorkingLayer(), "cancel");
		assertEquals(EuclidianConstants.MODE_WORKING_LAYER, app.getMode(),
				"only a valid choice ends the mode");
		answer.set(6);
		controller(app).processMode(new Hits(), false, false, null);
		assertEquals(6, app.getLayerWorkspace().getWorkingLayer());
		assertEquals(EuclidianConstants.MODE_MOVE, app.getMode());
	}

	@Test
	void selectionPreviewNeverChoosesALayer() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(4);
		GeoElement a = eval(app, "A=(1,1)");
		app.getLayerWorkspace().setWorkingLayer(0);
		app.setMode(EuclidianConstants.MODE_WORKING_LAYER);
		controller(app).switchModeForProcessMode(hit(a), false, false, null, true);
		assertEquals(0, app.getLayerWorkspace().getWorkingLayer());
		assertEquals(EuclidianConstants.MODE_WORKING_LAYER, app.getMode());
	}

	@Test
	void theProductDialogOffersExactlyTheDomainAndNeverWraps() {
		AppGeoCeDG app = G9U1TestApp.create();
		AtomicReference<Object[]> offered = new AtomicReference<>();
		try (MockedStatic<JOptionPane> pane = Mockito.mockStatic(JOptionPane.class)) {
			pane.when(() -> JOptionPane.showInputDialog(any(), any(), any(), anyInt(), any(),
					any(Object[].class), any())).thenAnswer(invocation -> {
						offered.set(invocation.getArgument(5));
						return 8;
					});
			assertTrue(app.chooseWorkingLayer());
		}
		// PRE-G9B-R6-plus-A-2 (DQ-A2-11): the configured product domain 0..99
		List<Integer> expected = new ArrayList<>();
		for (int layer = 0; layer <= app.getConfig().getMaxLayer(); layer++) {
			expected.add(layer);
		}
		assertEquals(100, expected.size());
		assertEquals(expected, List.of(offered.get()));
		assertEquals(8, app.getLayerWorkspace().getWorkingLayer());
		assertThrows(IllegalArgumentException.class,
				() -> app.getLayerWorkspace().setWorkingLayer(100));
		assertThrows(IllegalArgumentException.class,
				() -> app.getLayerWorkspace().setWorkingLayer(-1));
		assertEquals(8, app.getLayerWorkspace().getWorkingLayer());
		try (MockedStatic<JOptionPane> pane = Mockito.mockStatic(JOptionPane.class)) {
			pane.when(() -> JOptionPane.showInputDialog(any(), any(), any(), anyInt(), any(),
					any(Object[].class), any())).thenReturn(null);
			assertFalse(app.chooseWorkingLayer());
		}
		assertEquals(8, app.getLayerWorkspace().getWorkingLayer());
	}

	// ------------------------------------------------------------------ T-PROFILE

	@Test
	void theMoveGroupGainsExactlyOneActionAndClassicIsUnchanged() throws Exception {
		var groups = GeoCeDGProfile.getCatalog().getJSONArray("presentation_groups");
		List<String> toolbar = null;
		List<String> actions = null;
		for (int i = 0; i < groups.length(); i++) {
			if ("edit-selection".equals(groups.getJSONObject(i).getString("id"))) {
				toolbar = GeoCeDGProfile.strings(groups.getJSONObject(i)
						.getJSONArray("toolbar_action_ids"));
				actions = GeoCeDGProfile.strings(groups.getJSONObject(i)
						.getJSONArray("action_ids"));
			}
		}
		assertEquals(List.of("construction.move", "construction.move-rotate", ACTION), toolbar);
		assertEquals(List.of("construction.move", "construction.move-rotate", ACTION,
				"construction.select", "construction.attach-detach"), actions);
		assertEquals(130, GeoCeDGProfile.getActions().size());
		String classicTools = ToolBar.getAllToolsNoMacros(false, false, false);
		for (String token : classicTools.split("[ |,]+")) {
			assertNotEquals("141", token, "the Classic toolbar never offers mode 141");
		}
	}

	// ------------------------------------------------------------------- T-STATUS

	@Test
	void theStatusBarSurvivesLayoutRebuildsAndTracksTheWorkingLayer() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		JPanel panel = app.buildApplicationPanel();
		GeoCeDGStatusBar bar = app.getStatusBar();
		assertSame(bar, south(panel));
		assertEquals("Layer: 0", layerLabel(bar).getText());
		app.updateApplicationLayout();
		assertSame(bar, south(panel), "side-panel rebuilds never remove it");
		JPanel rebuilt = app.buildApplicationPanel();
		assertSame(bar, south(rebuilt), "a panel rebuild reattaches the same bar");
		app.getLayerWorkspace().setWorkingLayer(4);
		assertEquals("Layer: 4", layerLabel(bar).getText());
		app.setLocale(Locale.forLanguageTag("es"));
		assertEquals("Capa: 4", layerLabel(bar).getText());
		AtomicInteger dialogs = new AtomicInteger();
		app.setWorkingLayerChooser(current -> {
			dialogs.incrementAndGet();
			return 2;
		});
		JLabel label = layerLabel(bar);
		label.dispatchEvent(new MouseEvent(label, MouseEvent.MOUSE_CLICKED,
				System.currentTimeMillis(), 0, 2, 2, 1, false, MouseEvent.BUTTON1));
		assertEquals(1, dialogs.get());
		assertEquals("Capa: 2", layerLabel(bar).getText());
	}

	// ----------------------------------------------------------------- T-AV-ORDER

	@Test
	void sortByLayerOrdersGroupsNumerically() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		AlgebraViewD view = algebraView(app);
		view.setTreeMode(SortMode.LAYER);
		GeoElement a = eval(app, "A=(1,1)");
		Method parent = AlgebraViewD.class.getDeclaredMethod("getParentNode",
				GeoElement.class, int.class);
		parent.setAccessible(true);
		for (int layer : new int[] {10, 2, 9, 0}) {
			parent.invoke(view, a, layer);
		}
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) view.getModel().getRoot();
		List<Object> order = new ArrayList<>();
		for (int i = 0; i < root.getChildCount(); i++) {
			order.add(((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject());
		}
		assertEquals(List.of(0, 2, 9, 10), order, "10 sorts after 9, not before 2");
	}

	// ------------------------------------------------------------- T-AV-EYE, T-L7

	@Test
	void theEyeTogglesOnlyTheSessionStateAndNeverTheObjects() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		app.getLayerWorkspace().setWorkingLayer(3);
		final GeoElement a = eval(app, "A=(1,1)");
		app.getLayerWorkspace().setWorkingLayer(0);
		GeoCeDGAlgebraView view = algebraView(app);
		view.setTreeMode(SortMode.LAYER);
		view.setSize(400, 300);
		view.doLayout();
		app.getSelectionManager().clearSelectedGeos();
		String before = constructionXml(app);

		pressEye(view, 3);
		assertFalse(app.getLayerWorkspace().isLayerShown(3));
		assertTrue(a.isEuclidianVisible(), "the marble still shows object visibility");
		assertEquals(before, constructionXml(app));
		assertEquals(0, app.getSelectionManager().getSelectedGeos().size(),
				"the eye press does not select the group");
		assertTrue(rendererText(view, 3).endsWith("(hidden)"));

		pressEye(view, 3);
		assertTrue(app.getLayerWorkspace().isLayerShown(3));
		assertEquals(before, constructionXml(app));
	}

	@Test
	void theWorkingLayerCannotBeHiddenAndAChosenHiddenLayerIsShown() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		workspace.setWorkingLayer(5);
		eval(app, "A=(1,1)");
		GeoCeDGAlgebraView view = algebraView(app);
		view.setTreeMode(SortMode.LAYER);
		view.setSize(400, 300);
		view.doLayout();
		pressEye(view, 5);
		assertTrue(workspace.isLayerShown(5), "the working layer's eye is disabled");
		assertFalse(workspace.setLayerHidden(5, true));
		assertTrue(workspace.isLayerShown(5));

		// The chooser, from the mode and from the status bar, names a layer (AQ-L7).
		workspace.setWorkingLayer(0);
		assertTrue(workspace.setLayerHidden(5, true));
		app.setWorkingLayerChooser(current -> 5);
		app.setMode(EuclidianConstants.MODE_WORKING_LAYER);
		controller(app).processMode(new Hits(), false, false, null);
		assertEquals(5, workspace.getWorkingLayer());
		assertTrue(workspace.isLayerShown(5));

		workspace.setWorkingLayer(0);
		assertTrue(workspace.setLayerHidden(5, true));
		JLabel label = layerLabel(app.getStatusBar());
		label.dispatchEvent(new MouseEvent(label, MouseEvent.MOUSE_CLICKED,
				System.currentTimeMillis(), 0, 2, 2, 1, false, MouseEvent.BUTTON1));
		assertEquals(5, workspace.getWorkingLayer());
		assertTrue(workspace.isLayerShown(5));
	}

	// --------------------------------------------------------------------- helpers

	static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
		return (GeoCeDGEuclidianController) app.getActiveEuclidianView()
				.getEuclidianController();
	}

	static String constructionXml(AppGeoCeDG app) {
		XMLStringBuilder xml = new XMLStringBuilder();
		app.getKernel().getConstruction().getConstructionXML(xml, false);
		return xml.toString();
	}

	static UndoManagerD undo(AppGeoCeDG app) {
		return (UndoManagerD) app.getKernel().getConstruction().getUndoManager();
	}

	static void baseline(UndoManagerD undo) throws java.io.IOException {
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
	}

	static void awaitStore(AppGeoCeDG app, UndoManagerD undo) throws InterruptedException {
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		app.storeUndoInfo();
		assertTrue(stored.await(5, TimeUnit.SECONDS));
	}

	private static GeoElement only(AppGeoCeDG app,
			java.util.function.Predicate<GeoElement> filter) {
		List<GeoElement> matches = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream().filter(filter).toList();
		assertEquals(1, matches.size());
		return matches.get(0);
	}

	static GeoCeDGAlgebraView algebraView(AppGeoCeDG app) {
		return (GeoCeDGAlgebraView) ((GuiManagerD) app.getGuiManager()).getAlgebraView();
	}

	private static Component south(JPanel panel) {
		return ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
	}

	private static JLabel layerLabel(GeoCeDGStatusBar bar) {
		return bar.getSegment(GeoCeDGStatusBar.LAYER_SEGMENT);
	}

	static TreePath layerPath(GeoCeDGAlgebraView view, int layer) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) view.getModel().getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
			if (Integer.valueOf(layer).equals(node.getUserObject())) {
				return new TreePath(new Object[] {root, node});
			}
		}
		throw new AssertionError("No group for layer " + layer);
	}

	static void pressEye(GeoCeDGAlgebraView view, int layer) {
		TreePath path = layerPath(view, layer);
		view.expandPath(path);
		Rectangle bounds = view.getPathBounds(path);
		assertNotNull(bounds);
		int x = bounds.x + view.getOpenIconHeight() + 2;
		int y = bounds.y + bounds.height / 2;
		long now = System.currentTimeMillis();
		for (int id : new int[] {MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED,
				MouseEvent.MOUSE_CLICKED}) {
			view.dispatchEvent(new MouseEvent(view, id, now, MouseEvent.BUTTON1_DOWN_MASK,
					x, y, 1, false, MouseEvent.BUTTON1));
		}
	}

	private static String rendererText(GeoCeDGAlgebraView view, int layer) {
		TreePath path = layerPath(view, layer);
		Component cell = view.getCellRenderer().getTreeCellRendererComponent(view,
				path.getLastPathComponent(), false, true, false, 0, false);
		return ((JLabel) cell).getText();
	}
}
