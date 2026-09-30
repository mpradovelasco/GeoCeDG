/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.geocedg.common.kernel.algos.AlgoSplineV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.euclidian.draw.DrawPolyLine;
import org.geogebra.common.kernel.ModeSetter;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.inputbar.AlgebraInputD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/**
 * PRE-G9B-R4 focal contract for the three SplineV2 authoring tools: real mode
 * semantics, ordered collection, one commit and one undo point, the explicit
 * degree and closed-form gestures, cancel, preview authority, help and Input
 * Help. The kernel command remains the only spline authority throughout.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR4SplineV2AuthoringTest {

	private static final String[] OPEN_TOOL_ACTIONS = {"semantic.spline-v2.create",
			"semantic.spline-v2.create-degree", "semantic.spline-v2.create-closed"};
	private static final int[] SPLINE_MODES = {EuclidianConstants.MODE_SPLINE_V2,
			EuclidianConstants.MODE_SPLINE_V2_DEGREE,
			EuclidianConstants.MODE_SPLINE_V2_CLOSED};

	// ---------------------------------------------------------------- modes and help

	@Test
	void splineV2ActionsAreRealModesWithTheirOwnToolTextInBothLanguages() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		String[] englishNames = {"Spline V2", "Spline V2 with degree", "Closed Spline V2"};
		String[] englishTools = {"Semantic Spline V2 (experimental)",
				"Semantic Spline V2 with Degree (experimental)",
				"Closed Semantic Spline V2 (experimental)"};
		for (int i = 0; i < SPLINE_MODES.length; i++) {
			GeoCeDGProfile.ActionDefinition definition =
					GeoCeDGProfile.getAction(OPEN_TOOL_ACTIONS[i]);
			assertEquals("upstream-mode", definition.kind());
			assertEquals(SPLINE_MODES[i], definition.mode().intValue());
			assertEquals(137 + i, SPLINE_MODES[i]);
			assertEquals(englishNames[i],
					registry.get(OPEN_TOOL_ACTIONS[i]).getValue(Action.NAME));
			assertEquals(englishTools[i], app.getToolName(SPLINE_MODES[i]));
			String help = app.getToolHelp(SPLINE_MODES[i]);
			assertTrue(help.startsWith("Select at least three points in order"), help);
			assertEquals(help, registry.get(OPEN_TOOL_ACTIONS[i])
					.getValue(Action.SHORT_DESCRIPTION));
		}
		assertTrue(app.getToolHelp(EuclidianConstants.MODE_SPLINE_V2).contains("degree 3"));
		assertTrue(app.getToolHelp(EuclidianConstants.MODE_SPLINE_V2_DEGREE)
				.contains("enter the degree"));
		assertTrue(app.getToolHelp(EuclidianConstants.MODE_SPLINE_V2_CLOSED)
				.contains("closed"));

		app.setLocale(new Locale("es"));
		registry.refresh();
		assertEquals("Spline semántico V2 (experimental)",
				app.getToolName(EuclidianConstants.MODE_SPLINE_V2));
		assertEquals("Spline semántico V2 cerrado (experimental)",
				app.getToolName(EuclidianConstants.MODE_SPLINE_V2_CLOSED));
		assertTrue(app.getToolHelp(EuclidianConstants.MODE_SPLINE_V2_DEGREE)
				.contains("introduce el grado"));
		assertEquals("Spline V2 con grado",
				registry.get("semantic.spline-v2.create-degree").getValue(Action.NAME));
		assertEquals("Spline V2 cerrada",
				registry.get("semantic.spline-v2.create-closed").getValue(Action.NAME));
	}

	@Test
	void defaultEnglishAndSpanishBundlesDeclareEveryToolKey() throws IOException {
		Path properties = repository().resolve("source/shared/common-jre/src/main/"
				+ "resources/org/geogebra/common/jre/properties");
		for (String bundle : new String[] {"menu.properties", "menu_en.properties",
				"menu_es.properties"}) {
			Properties keys = new Properties();
			try (Reader reader = Files.newBufferedReader(properties.resolve(bundle),
					StandardCharsets.UTF_8)) {
				keys.load(reader);
			}
			for (String key : new String[] {"SplineV2.Tool", "SplineV2.Help",
					"SplineV2.Degree.Tool", "SplineV2.Degree.Help",
					"SplineV2.Closed.Tool", "SplineV2.Closed.Help",
					"SplineV2.DegreePrompt", "OrderedList.Tool", "OrderedList.Help"}) {
				assertFalse(keys.getProperty(key, "").isBlank(), bundle + " " + key);
			}
		}
		for (String bundle : new String[] {"command.properties", "command_en.properties",
				"command_es.properties"}) {
			Properties keys = new Properties();
			try (Reader reader = Files.newBufferedReader(properties.resolve(bundle),
					StandardCharsets.UTF_8)) {
				keys.load(reader);
			}
			assertTrue(keys.getProperty("SplineV2.Syntax", "").contains(", ... ]"), bundle);
		}
	}

	@Test
	void invokingTheToolSetsItsModeAndOpensInputHelpOnTheExistingSyntax() {
		for (Locale locale : new Locale[] {Locale.ENGLISH, new Locale("es")}) {
			AppGeoCeDG app = G9U1TestApp.create();
			app.buildApplicationPanel();
			app.setLocale(locale);
			app.setMode(EuclidianConstants.MODE_POINT);
			GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
			final int steps = app.getKernel().getConstruction().steps();

			registry.invoke("semantic.spline-v2.create", new ActionEvent(this, 1, "test"));

			assertEquals(EuclidianConstants.MODE_SPLINE_V2, app.getMode());
			GuiManagerD gui = (GuiManagerD) app.getGuiManager();
			assertTrue(gui.hasInputHelpPanel());
			Container panel = (Container) gui.getInputHelpPanel();
			DefaultMutableTreeNode selected = (DefaultMutableTreeNode)
					find(panel, JTree.class).getLastSelectedPathComponent();
			assertNotNull(selected, locale.toString());
			assertEquals("SplineV2", selected.getUserObject());
			String syntax = find(panel, JTextPane.class).getText();
			assertTrue(syntax.contains("es".equals(locale.getLanguage())
					? "<Lista de puntos>, <Grado>" : "<List of Points>, <Degree>"), syntax);
			AlgebraInputD input = (AlgebraInputD) gui.getAlgebraInput();
			assertEquals("", input.getTextField().getText(), "no command is typed in");
			assertEquals(GeoCeDGProfile.getText("Workspace.SplineHelp",
					locale.getLanguage()), input.getTextField().getToolTipText());
			assertEquals(steps, app.getKernel().getConstruction().steps());
		}
	}

	@Test
	void contextualHelpDescribesTheActiveSplineToolNotThePreviousTool() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.buildApplicationPanel();
		app.setLocale(Locale.ENGLISH);
		app.setMode(EuclidianConstants.MODE_POINT);
		String previousHelp = app.getToolHelp(EuclidianConstants.MODE_POINT);
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		registry.invoke("semantic.spline-v2.create-closed", new ActionEvent(this, 1, "t"));
		try (MockedStatic<JOptionPane> dialog = mockStatic(JOptionPane.class)) {
			registry.invoke("help.contextual-action", new ActionEvent(this, 1, "help"));
			ArgumentCaptor<Object> message = ArgumentCaptor.forClass(Object.class);
			dialog.verify(() -> JOptionPane.showMessageDialog(any(), message.capture(),
					any(), anyInt()));
			String text = ((JTextArea) ((JScrollPane) message.getValue()).getViewport()
					.getView()).getText();
			assertTrue(text.contains("Closed Semantic Spline V2 (experimental)"), text);
			assertTrue(text.contains(app.getToolHelp(
					EuclidianConstants.MODE_SPLINE_V2_CLOSED)), text);
			assertFalse(text.contains(previousHelp), text);
		}
		DefaultMutableTreeNode selected = (DefaultMutableTreeNode) find(
				(Container) ((GuiManagerD) app.getGuiManager()).getInputHelpPanel(),
				JTree.class).getLastSelectedPathComponent();
		assertEquals("SplineV2", selected.getUserObject());
	}

	@Test
	void tooltipExamplesAreFormsTheCommandActuallyAccepts() {
		Pattern example = Pattern.compile("SplineV2\\((?:\\{[^}]*\\}|[^()])*\\)");
		for (String language : new String[] {"en", "es"}) {
			String tooltip = GeoCeDGProfile.getText("Workspace.SplineHelp", language);
			assertFalse(tooltip.contains(",2)"), "degree 2 is rejected by the command");
			assertFalse(tooltip.contains("parameter") || tooltip.contains("paramétric"),
					"no parameter form exists");
			Matcher matcher = example.matcher(tooltip);
			int examples = 0;
			while (matcher.find()) {
				AppGeoCeDG app = G9U1TestApp.create();
				fourPoints(app);
				GeoElement created = eval(app, matcher.group());
				assertInstanceOf(GeoLocusV2.class, created, matcher.group());
				assertTrue(created.isDefined(), matcher.group());
				examples++;
			}
			assertEquals(4, examples, tooltip);
		}
	}

	// ----------------------------------------------------------- collection and commit

	@Test
	void orderedPointsCommitOneDefaultDegreeSplineWithOneUndoPoint() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(app);
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2);
		controller.getSplineAuthoring().setDegreeInput((host, mode, accept) -> {
			throw new AssertionError("the default path never prompts");
		});
		int before = size(app);
		AtomicInteger commits = new AtomicInteger();
		// Click order differs from construction order and is the only order used.
		GeoPoint[] clicks = {points[2], points[0], points[3], points[1]};
		for (GeoPoint point : clicks) {
			assertFalse(click(controller, point, commits));
		}
		assertEquals(before, size(app), "collection creates nothing");
		assertEquals(0, commits.get());

		assertTrue(click(controller, clicks[0], commits));

		assertEquals(1, commits.get(), "exactly one undo point");
		GeoLocusV2 spline = onlySpline(app);
		AlgoSplineV2 parent = assertInstanceOf(AlgoSplineV2.class,
				spline.getParentAlgorithm());
		GeoList list = (GeoList) parent.getInput(0);
		assertEquals(4, list.size());
		for (int i = 0; i < clicks.length; i++) {
			assertSame(clicks[i], list.get(i), "click order " + i);
		}
		assertEquals(3, ((GeoNumeric) parent.getInput(1)).getDouble());
		assertTrue(spline.isDefined());
		assertFalse(periodic(spline));
		assertTrue(app.getSelectionManager().getSelectedPointList().isEmpty());
		assertEquals(1, count(app, GeoList.class), "the command's own list only");
	}

	@Test
	void theToolBuildsExactlyTheConstructionOfTheTypedBarePointCommand() {
		AppGeoCeDG tool = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(tool);
		GeoCeDGEuclidianController controller = splineMode(tool,
				EuclidianConstants.MODE_SPLINE_V2);
		for (GeoPoint point : points) {
			click(controller, point, new AtomicInteger());
		}
		click(controller, points[0], new AtomicInteger());

		AppGeoCeDG typed = G9U1TestApp.create();
		fourPoints(typed);
		eval(typed, "SplineV2(A,B,C,D)");

		assertEquals(protocol(typed), protocol(tool));
	}

	@Test
	void fewerThanThreePointsCannotFinishAndReselectionDeselects() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(app);
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2);
		AtomicInteger commits = new AtomicInteger();
		final int before = size(app);
		click(controller, points[0], commits);
		click(controller, points[1], commits);
		assertFalse(click(controller, points[0], commits), "two points never finish");
		assertEquals(List.of(points[1]),
				app.getSelectionManager().getSelectedPointList());
		click(controller, points[2], commits);
		click(controller, points[3], commits);
		assertFalse(click(controller, points[2], commits), "a middle point deselects");
		assertEquals(List.of(points[1], points[3]),
				app.getSelectionManager().getSelectedPointList());
		assertEquals(before, size(app));
		assertEquals(0, commits.get());
	}

	@Test
	void explicitDegreeIsASeparateSecondGestureWithOneCommit() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(app);
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2_DEGREE);
		AtomicInteger requests = new AtomicInteger();
		controller.getSplineAuthoring().setDegreeInput((host, mode, accept) -> {
			requests.incrementAndGet();
			assertEquals(EuclidianConstants.MODE_SPLINE_V2_DEGREE, mode);
			accept.accept(new GeoNumeric(host.getKernel().getConstruction(), 4));
		});
		AtomicInteger commits = new AtomicInteger();
		for (GeoPoint point : points) {
			click(controller, point, commits);
		}
		assertEquals(0, requests.get(), "no prompt while collecting");
		assertTrue(click(controller, points[0], commits));
		assertEquals(1, requests.get());
		assertEquals(1, commits.get());
		AlgoSplineV2 parent = (AlgoSplineV2) onlySpline(app).getParentAlgorithm();
		assertEquals(4, ((GeoNumeric) parent.getInput(1)).getDouble());
		assertEquals(4, ((GeoList) parent.getInput(0)).size());
		assertEquals(GeoCeDGSplineV2Authoring.DEFAULT_DEGREE_TEXT, "3");
	}

	@Test
	void cancelledOrRejectedExplicitDegreeCreatesNothing() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(app);
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2_DEGREE);
		int before = size(app);
		AtomicInteger commits = new AtomicInteger();

		controller.getSplineAuthoring().setDegreeInput((host, mode, accept) -> { });
		for (GeoPoint point : points) {
			click(controller, point, commits);
		}
		assertFalse(click(controller, points[0], commits), "cancelled dialog");
		assertEquals(before, size(app));
		assertTrue(app.getSelectionManager().getSelectedPointList().isEmpty());

		// The command, not the Desktop, rejects an explicit degree below 3.
		controller.getSplineAuthoring().setDegreeInput((host, mode, accept) ->
				accept.accept(new GeoNumeric(host.getKernel().getConstruction(), 2)));
		for (GeoPoint point : points) {
			click(controller, point, commits);
		}
		assertFalse(click(controller, points[0], commits));
		assertNotNull(controller.getSplineAuthoring().getLastFailure());
		assertEquals(before, size(app));
		assertEquals(0, count(app, GeoLocusV2.class));
		assertEquals(0, commits.get());
	}

	@Test
	void oversizedPointSetsFollowTheCurrentCommandContractPerForm() {
		AppGeoCeDG app = G9U1TestApp.create();
		List<GeoPoint> points = new ArrayList<>();
		for (int i = 0; i < 33; i++) {
			points.add((GeoPoint) eval(app, "P_{" + i + "}=(" + i + "," + (i % 3) + ")"));
		}
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2);
		AtomicInteger commits = new AtomicInteger();
		for (GeoPoint point : points) {
			click(controller, point, commits);
		}
		assertTrue(click(controller, points.get(0), commits));
		// Default degree: no degree validation; the model guard leaves it undefined.
		GeoLocusV2 undefined = onlySpline(app);
		assertFalse(undefined.isDefined());
		assertNull(controller.getSplineAuthoring().getLastFailure());
		assertFalse(eval(app, "SplineV2(" + labels(points) + ")").isDefined(),
				"the typed default form behaves identically");

		// Explicit degree: the command rejects the arguments before creation.
		int before = size(app);
		app.setMode(EuclidianConstants.MODE_SPLINE_V2_DEGREE);
		controller.getSplineAuthoring().setDegreeInput((host, mode, accept) ->
				accept.accept(new GeoNumeric(host.getKernel().getConstruction(), 3)));
		for (GeoPoint point : points) {
			click(controller, point, commits);
		}
		assertFalse(click(controller, points.get(0), commits));
		assertNotNull(controller.getSplineAuthoring().getLastFailure());
		assertEquals(before, size(app));
	}

	// -------------------------------------------------------------------- closed form

	@Test
	void onlyTheClosedToolSynthesizesTheRepeatedFirstMember() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(app);
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2_CLOSED);
		AtomicInteger commits = new AtomicInteger();
		for (GeoPoint point : points) {
			click(controller, point, commits);
		}
		assertTrue(click(controller, points[0], commits));
		GeoLocusV2 closed = onlySpline(app);
		GeoList closedList = (GeoList) closed.getParentAlgorithm().getInput(0);
		assertEquals(5, closedList.size());
		assertSame(points[0], closedList.get(0));
		assertSame(points[0], closedList.get(4), "the same point object closes it");
		assertTrue(periodic(closed));
		assertEquals(1, commits.get());

		for (int mode : new int[] {EuclidianConstants.MODE_SPLINE_V2,
				EuclidianConstants.MODE_SPLINE_V2_DEGREE}) {
			AppGeoCeDG open = G9U1TestApp.create();
			GeoPoint[] openPoints = fourPoints(open);
			GeoCeDGEuclidianController openController = splineMode(open, mode);
			openController.getSplineAuthoring().setDegreeInput((host, active, accept) ->
					accept.accept(new GeoNumeric(host.getKernel().getConstruction(), 3)));
			for (GeoPoint point : openPoints) {
				click(openController, point, new AtomicInteger());
			}
			click(openController, openPoints[0], new AtomicInteger());
			GeoList openList = (GeoList) onlySpline(open).getParentAlgorithm().getInput(0);
			assertEquals(4, openList.size(), "no synthesized member in mode " + mode);
			assertFalse(periodic(onlySpline(open)));
		}
	}

	@Test
	void closednessIsNeverInferredFromProximityOrTheFinishClick() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint a = (GeoPoint) eval(app, "A=(0,0)");
		GeoPoint b = (GeoPoint) eval(app, "B=(1,1)");
		GeoPoint c = (GeoPoint) eval(app, "C=(2,0)");
		GeoPoint nearA = (GeoPoint) eval(app, "E=(0.001,0)");
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2);
		for (GeoPoint point : new GeoPoint[] {a, b, c, nearA}) {
			click(controller, point, new AtomicInteger());
		}
		click(controller, a, new AtomicInteger());
		GeoLocusV2 spline = onlySpline(app);
		GeoList list = (GeoList) spline.getParentAlgorithm().getInput(0);
		assertEquals(4, list.size());
		assertSame(nearA, list.get(3));
		assertFalse(periodic(spline), "a nearby endpoint and the finish click never close");

		// Two distinct points with exactly equal values: the Desktop appends nothing
		// and leaves the kernel's existing endpoint rule to decide, as for typed input.
		AppGeoCeDG coincident = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(coincident);
		GeoPoint twin = (GeoPoint) eval(coincident, "E=(0,0)");
		GeoCeDGEuclidianController twinController = splineMode(coincident,
				EuclidianConstants.MODE_SPLINE_V2);
		for (GeoPoint point : new GeoPoint[] {points[0], points[1], points[2], twin}) {
			click(twinController, point, new AtomicInteger());
		}
		click(twinController, points[0], new AtomicInteger());
		GeoLocusV2 twinSpline = onlySpline(coincident);
		GeoList twinList = (GeoList) twinSpline.getParentAlgorithm().getInput(0);
		assertEquals(4, twinList.size());
		assertSame(twin, twinList.get(3));
		assertNotSame(twinList.get(0), twinList.get(3));
		GeoLocusV2 typed = (GeoLocusV2) eval(coincident, "SplineV2(A,B,C,E)");
		assertEquals(periodic(typed), periodic(twinSpline));
	}

	// ------------------------------------------------------------ cancel, undo, preview

	@Test
	void escapeAndToolChangeCancelWithoutConstructionOrUndo() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(app);
		UndoManagerD undo = baseline(app);
		final int history = undo.getHistorySize();
		final int before = size(app);
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2_CLOSED);
		for (GeoPoint point : points) {
			click(controller, point, new AtomicInteger());
		}
		app.setMoveMode();
		assertTrue(app.getSelectionManager().getSelectedPointList().isEmpty(),
				"Escape path");

		app.setMode(EuclidianConstants.MODE_SPLINE_V2);
		click(controller, points[0], new AtomicInteger());
		click(controller, points[1], new AtomicInteger());
		app.setMode(EuclidianConstants.MODE_POINT);
		assertTrue(app.getSelectionManager().getSelectedPointList().isEmpty(),
				"tool change");

		assertEquals(before, size(app));
		assertEquals(0, count(app, GeoLocusV2.class));
		assertEquals(history, undo.getHistorySize());
	}

	@Test
	void undoAndRedoRemoveAndRestoreTheCommittedSpline() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(app);
		UndoManagerD undo = baseline(app);
		int history = undo.getHistorySize();
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2);
		for (GeoPoint point : points) {
			controller.processMode(hit(point), false, false);
		}
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		assertTrue(controller.processMode(hit(points[0]), false, false));
		assertTrue(stored.await(5, TimeUnit.SECONDS));
		assertEquals(history + 1, undo.getHistorySize());
		String definition = onlySpline(app).getDefinition(
				org.geogebra.common.kernel.StringTemplate.defaultTemplate);

		app.getKernel().undo();
		assertEquals(0, count(app, GeoLocusV2.class));
		app.getKernel().redo();
		GeoLocusV2 restored = onlySpline(app);
		assertTrue(restored.isDefined());
		assertEquals(definition, restored.getDefinition(
				org.geogebra.common.kernel.StringTemplate.defaultTemplate));
	}

	@Test
	void theControlPolygonPreviewIsPresentationOnly() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(app);
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2);
		assertInstanceOf(DrawPolyLine.class, app.getEuclidianView1().getPreviewDrawable());
		int before = size(app);
		for (GeoPoint point : points) {
			click(controller, point, new AtomicInteger());
			controller.wrapMouseMoved(G9U1SemanticPointInteractionTest.event(app, 5, 5));
		}
		assertEquals(before, size(app), "the preview is not a construction object");
		click(controller, points[0], new AtomicInteger());
		GeoLocusV2 spline = onlySpline(app);
		GeoList list = (GeoList) spline.getParentAlgorithm().getInput(0);
		for (int i = 0; i < list.size(); i++) {
			assertSame(points[i], list.get(i), "inputs are the selected points only");
		}
	}

	// ------------------------------------------------------- feature and compatibility

	@Test
	void featureOffToolsCreateNothing() {
		AppGeoCeDG app = G9U1TestApp.create(false);
		GeoPoint[] points = fourPoints(app);
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		for (String id : OPEN_TOOL_ACTIONS) {
			assertFalse(registry.get(id).isEnabled(), id);
		}
		// Drive the controller gate directly: the unavailable action cannot select it.
		GeoCeDGEuclidianController controller = controller(app);
		controller.setMode(EuclidianConstants.MODE_SPLINE_V2, ModeSetter.TOOLBAR);
		int before = size(app);
		AtomicInteger commits = new AtomicInteger();
		for (GeoPoint point : points) {
			click(controller, point, commits);
		}
		click(controller, points[0], commits);
		assertEquals(before, size(app));
		assertEquals(0, commits.get());
	}

	@Test
	void theCommandFormsAreUnchanged() {
		AppGeoCeDG app = G9U1TestApp.create();
		fourPoints(app);
		assertTrue(eval(app, "SplineV2({A,B,C,D})").isDefined());
		assertTrue(eval(app, "SplineV2({A,B,C,D},4)").isDefined());
		assertTrue(eval(app, "SplineV2(A,B,C)").isDefined());
		assertTrue(periodic((GeoLocusV2) eval(app, "SplineV2({A,B,C,D,A},3)")));
		assertTrue(eval(app, "Spline({A,B,C,D})").isDefined(), "Classic Spline");
	}

	@Test
	void aToolSplineReopensUnchangedFromTheNativeDocument(@TempDir Path directory)
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoPoint[] points = fourPoints(app);
		GeoCeDGEuclidianController controller = splineMode(app,
				EuclidianConstants.MODE_SPLINE_V2_CLOSED);
		for (GeoPoint point : points) {
			click(controller, point, new AtomicInteger());
		}
		click(controller, points[0], new AtomicInteger());
		GeoLocusV2 spline = onlySpline(app);
		String label = spline.getLabelSimple();
		String definition = spline.getDefinition(
				org.geogebra.common.kernel.StringTemplate.defaultTemplate);
		Path document = directory.resolve("r4-tool-spline.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(document.toFile()));

		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(document.toFile(), false));
		GeoLocusV2 restored = (GeoLocusV2) lookup(reopened, label);
		assertTrue(restored.isDefined());
		assertTrue(periodic(restored));
		assertEquals(definition, restored.getDefinition(
				org.geogebra.common.kernel.StringTemplate.defaultTemplate));
	}

	// ------------------------------------------------------------------------ helpers

	static GeoPoint[] fourPoints(AppGeoCeDG app) {
		return new GeoPoint[] {(GeoPoint) eval(app, "A=(0,0)"),
				(GeoPoint) eval(app, "B=(1,1)"), (GeoPoint) eval(app, "C=(2,0)"),
				(GeoPoint) eval(app, "D=(3,1)")};
	}

	/** Selects the tool through the application, as the toolbar does. */
	static GeoCeDGEuclidianController splineMode(AppGeoCeDG app, int mode) {
		app.setMode(mode);
		assertEquals(mode, app.getMode());
		return controller(app);
	}

	static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
		return (GeoCeDGEuclidianController) app.getEuclidianView1()
				.getEuclidianController();
	}

	static Hits hit(GeoElement geo) {
		Hits hits = new Hits();
		hits.add(geo);
		return hits;
	}

	/** Releases on one object with the host release callback, counting its undo stores. */
	static boolean click(GeoCeDGEuclidianController controller, GeoElement geo,
			AtomicInteger commits) {
		return controller.processMode(hit(geo), false, false, changed -> {
			if (changed) {
				commits.incrementAndGet();
				controller.getApplication().storeUndoInfoAndStateForModeStarting(true);
			}
		});
	}

	static int size(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
	}

	static long count(AppGeoCeDG app, Class<?> type) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(type::isInstance).count();
	}

	private static GeoLocusV2 onlySpline(AppGeoCeDG app) {
		List<GeoLocusV2> splines = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().stream().filter(GeoLocusV2.class::isInstance)
				.map(GeoLocusV2.class::cast).toList();
		assertEquals(1, splines.size());
		return splines.get(0);
	}

	private static boolean periodic(GeoLocusV2 spline) {
		return spline.getSemanticDefinition().getProvider().isPeriodic();
	}

	private static String labels(List<GeoPoint> points) {
		return String.join(",", points.stream().map(GeoElement::getLabelSimple).toList());
	}

	private static List<String> protocol(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.map(geo -> geo.getClass().getSimpleName() + " " + geo.getLabelSimple()
						+ " " + geo.getDefinition(
								org.geogebra.common.kernel.StringTemplate.defaultTemplate)
						+ " aux=" + geo.isAuxiliaryObject()).toList();
	}

	private static UndoManagerD baseline(AppGeoCeDG app) throws Exception {
		app.setUndoActive(true);
		UndoManagerD undo = (UndoManagerD) app.getKernel().getConstruction()
				.getUndoManager();
		try (UndoManagerD.PreparedUndoBaseline baseline = undo.prepareUndoBaseline()) {
			undo.commitUndoBaseline(baseline);
		}
		return undo;
	}

	static <T extends Component> T find(Container root, Class<T> type) {
		for (Component child : root.getComponents()) {
			if (type.isInstance(child)) {
				return type.cast(child);
			}
			if (child instanceof Container) {
				T nested = find((Container) child, type);
				if (nested != null) {
					return nested;
				}
			}
		}
		return null;
	}

	private static Path repository() {
		Path current = Path.of("").toAbsolutePath();
		while (current != null && !Files.exists(current.resolve("AGENTS.md"))) {
			current = current.getParent();
		}
		assertNotNull(current);
		return current;
	}
}
