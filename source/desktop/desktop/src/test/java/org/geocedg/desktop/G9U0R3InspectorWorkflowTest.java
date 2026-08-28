/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.main.undo.UndoManager;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/** G9U0-R3 inspector, exact-token and negative-scope scenarios. */
class G9U0R3InspectorWorkflowTest {

	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void i01MenuClickInspectsAnAlgebraSelectedIntersectionResult() {
		AppGeoCeDG app = enabledApp();
		GeoLocusIntersectionResult rich = createCircleIntersection(app, "R");
		app.getSelectionManager().addSelectedGeo(rich);

		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			inspectorItem(app).doClick();
			verifyReadOnlyInspector(dialogs, app);
		}
	}

	@Test
	void i02SingleUnselectedRichResultIsDiscoveredFromConstruction() {
		AppGeoCeDG app = enabledApp();
		createLineIntersection(app, "R", "u:x=0");
		app.getSelectionManager().clearSelectedGeos();

		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			stubIntersectionDecision(dialogs, JOptionPane.CANCEL_OPTION);
			inspectorItem(app).doClick();
			verifyIntersectionInspector(dialogs);
		}
	}

	@Test
	void i03SeveralRichResultsUseDeterministicConstructionOrderChooser() {
		AppGeoCeDG app = enabledApp();
		createLineIntersection(app, "R1", "u1:x=0");
		createLineIntersection(app, "R2", "u2:x=1");
		app.getSelectionManager().clearSelectedGeos();
		AtomicReference<List<String>> choicesSeen = new AtomicReference<>();

		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showInputDialog(
					any(Component.class), any(), anyString(),
					eq(JOptionPane.PLAIN_MESSAGE), isNull(),
					any(Object[].class), any())).thenAnswer(invocation -> {
						Object[] choices = invocation.getArgument(5);
						choicesSeen.set(Arrays.stream(choices)
								.map(Object::toString).toList());
						return choices[0];
					});
			stubIntersectionDecision(dialogs, JOptionPane.CANCEL_OPTION);
			inspectorItem(app).doClick();
			verifyIntersectionInspector(dialogs);
		}

		assertNotNull(choicesSeen.get());
		assertEquals(2, choicesSeen.get().size());
		assertTrue(choicesSeen.get().get(0).startsWith("R1 "));
		assertTrue(choicesSeen.get().get(1).startsWith("R2 "));
	}

	@Test
	void i04CancelCreatesNoPointOrTokenChild() {
		AppGeoCeDG app = enabledApp();
		GeoLocusIntersectionResult rich = createLineIntersection(app, "R",
				"u:x=0");
		app.getSelectionManager().addSelectedGeo(rich);
		long pointsBefore = exactTokenPoints(app).size();
		long textsBefore = geos(app, GeoText.class).size();

		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			stubIntersectionDecision(dialogs, JOptionPane.CANCEL_OPTION);
			inspectorItem(app).doClick();
		}

		assertEquals(pointsBefore, exactTokenPoints(app).size());
		assertEquals(textsBefore, geos(app, GeoText.class).size());
	}

	@Test
	void i05AcceptCreatesExactlyOneExactTokenPoint() {
		AppGeoCeDG app = enabledApp();
		GeoLocusIntersectionResult rich = createLineIntersection(app, "R",
				"u:x=0");
		app.getSelectionManager().addSelectedGeo(rich);

		GeoPoint point = acceptFirstToken(app);

		assertEquals(1, exactTokenPoints(app).size());
		AlgoLocusIntersectionPointV2 parent =
				(AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
		assertSame(rich, parent.getInput(0));
		assertTrue(parent.getInput(1) instanceof GeoText);
		assertEquals(rich.getIntersectionResult().getFiniteSolutions().get(0)
				.getIdentity().getRootToken(), parent.getSelectedRootToken());
	}

	@Test
	void i06ExactTokenPointSurvivesEstablishedRecompute() {
		AppGeoCeDG app = enabledApp();
		GeoNumeric verticalOffset = (GeoNumeric) eval(app, "b=0");
		eval(app, "s=0");
		eval(app, "Q=(s,b)");
		eval(app, "D={false,{-2,2,true,true}}");
		eval(app, "L=LocusV2(Q,s,D)");
		GeoLocusIntersectionResult rich = createLineIntersection(app, "R",
				"u:x=0");
		app.getSelectionManager().addSelectedGeo(rich);
		GeoPoint point = acceptFirstToken(app);
		AlgoLocusIntersectionPointV2 parent =
				(AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
		String token = parent.getSelectedRootToken();

		verticalOffset.setValue(1);
		verticalOffset.updateCascade();

		assertTrue(point.isDefined());
		assertEquals(0, point.getInhomX(), 1E-10);
		assertEquals(1, point.getInhomY(), 1E-10);
		assertEquals(token, parent.getSelectedRootToken());
	}

	@Test
	void i07MaterializationUsesNormalUndoAndRedo() throws Exception {
		AppGeoCeDG app = enabledApp();
		GeoLocusIntersectionResult rich = createLineIntersection(app, "R",
				"u:x=0");
		app.getSelectionManager().addSelectedGeo(rich);
		app.getKernel().initUndoInfo();
		UndoManager undoManager = app.getKernel().getConstruction()
				.getUndoManager();
		await(() -> !undoManager.undoPossible());

		acceptFirstToken(app);
		await(undoManager::undoPossible);
		assertEquals(1, exactTokenPoints(app).size());

		app.getKernel().undo();
		await(() -> exactTokenPoints(app).isEmpty()
				&& undoManager.redoPossible());
		app.getKernel().redo();
		await(() -> exactTokenPoints(app).size() == 1);
	}

	@Test
	void i08MetricRichResultRemainsSupportedByTheSameMenuAction() {
		AppGeoCeDG app = enabledApp();
		createBaseLocus(app);
		GeoLocusMetricResult metric = (GeoLocusMetricResult)
				eval(app, "M=LocusLength(L)");
		app.getSelectionManager().addSelectedGeo(metric);

		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			inspectorItem(app).doClick();
			dialogs.verify(() -> JOptionPane.showMessageDialog(
					any(Component.class), any(),
					eq(app.getLocalization().getMenu(
							"LocusV2.Results.Inspect")),
					eq(JOptionPane.INFORMATION_MESSAGE)), times(1));
		}
	}

	@Test
	void i09NoRichResultsShowsTheExistingTypedMessage() {
		AppGeoCeDG app = enabledApp();

		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			inspectorItem(app).doClick();
			dialogs.verify(() -> JOptionPane.showMessageDialog(
					eq(app.getMainComponent()),
					eq(app.getLocalization().getMenu("LocusV2.Results.None")),
					eq(app.getLocalization().getMenu(
							"LocusV2.Results.Inspect")),
					eq(JOptionPane.INFORMATION_MESSAGE)), times(1));
		}
	}

	@Test
	void i10LongExactTokensHaveBoundedDistinctAccessibleChoices() {
		AppGeoCeDG app = enabledApp();
		GeoLocusIntersectionResult rich = createDoubleIntersection(app, "R");
		app.getSelectionManager().addSelectedGeo(rich);
		List<String> exactTokens = admissibleTokens(rich);
		assertEquals(2, exactTokens.size());
		assertTrue(exactTokens.stream().allMatch(token -> token.length() > 256),
				"The regression construction must produce deliberately long tokens");
		AtomicReference<JPanel> chooserSeen = new AtomicReference<>();

		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showConfirmDialog(
					any(Component.class), any(), anyString(),
					eq(JOptionPane.OK_CANCEL_OPTION),
					eq(JOptionPane.PLAIN_MESSAGE))).thenAnswer(invocation -> {
				chooserSeen.set(invocation.getArgument(1));
				return JOptionPane.CANCEL_OPTION;
			});
			inspectorItem(app).doClick();
		}

		JPanel chooser = chooserSeen.get();
		assertNotNull(chooser);
		JComboBox<?> combo = findComponent(chooser, JComboBox.class);
		JTextArea diagnostic = findComponent(chooser, JTextArea.class);
		assertNotNull(combo);
		assertNotNull(diagnostic);
		assertEquals(2, combo.getItemCount());
		String solutionLabel = app.getLocalization().getMenu(
				"LocusV2.Results.Field.Solution");
		String first = combo.getItemAt(0).toString();
		String second = combo.getItemAt(1).toString();
		assertTrue(first.startsWith(solutionLabel + " 1 \u2014 "));
		assertTrue(second.startsWith(solutionLabel + " 2 \u2014 "));
		assertNotEquals(first, second);
		for (String token : exactTokens) {
			assertFalse(first.contains(token));
			assertFalse(second.contains(token));
			assertTrue(diagnostic.getText().contains(token));
		}
		assertTrue(diagnostic.getLineWrap());
		assertTrue(diagnostic.getWrapStyleWord());
		int diagnosticWidth = diagnostic.getPreferredScrollableViewportSize().width;
		assertTrue(combo.getPreferredSize().width <= diagnosticWidth,
				"Token choice must not outgrow the bounded diagnostic viewport");
		assertTrue(chooser.getPreferredSize().width <= diagnosticWidth + 64,
				"Inspector must fit its bounded diagnostic viewport");
		assertEquals(app.getLocalization().getMenu(
				"LocusV2.Results.ExactToken"),
				combo.getAccessibleContext().getAccessibleName());
		assertTrue(combo.isFocusable());
		combo.setSelectedIndex(1);
		assertSame(combo.getItemAt(1), combo.getSelectedItem());
	}

	@Test
	void i11CompactChoiceUsesAndPersistsTheCompleteExactToken(
			@TempDir Path temporaryDirectory) {
		AppGeoCeDG app = enabledApp();
		GeoLocusIntersectionResult rich = createDoubleIntersection(app, "R");
		app.getSelectionManager().addSelectedGeo(rich);
		List<String> exactTokens = admissibleTokens(rich);
		String expectedToken = exactTokens.get(1);
		AtomicReference<String> compactLabel = new AtomicReference<>();

		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showConfirmDialog(
					any(Component.class), any(), anyString(),
					eq(JOptionPane.OK_CANCEL_OPTION),
					eq(JOptionPane.PLAIN_MESSAGE))).thenAnswer(invocation -> {
				JPanel chooser = invocation.getArgument(1);
				JComboBox<?> combo = findComponent(chooser, JComboBox.class);
				assertNotNull(combo);
				combo.setSelectedIndex(1);
				compactLabel.set(combo.getSelectedItem().toString());
				return JOptionPane.OK_OPTION;
			});
			inspectorItem(app).doClick();
		}

		List<GeoPoint> points = exactTokenPoints(app);
		assertEquals(1, points.size());
		GeoPoint point = points.get(0);
		AlgoLocusIntersectionPointV2 parent =
				(AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
		assertEquals(expectedToken, parent.getSelectedRootToken());
		assertEquals(expectedToken, tokenInput(point).getTextString());
		assertNotEquals(compactLabel.get(), tokenInput(point).getTextString());
		assertTrue(geos(app, GeoText.class).stream().noneMatch(text ->
				compactLabel.get().equals(text.getTextString())));

		PersistentGeoId pointId = persistentId(app, point);
		Path document = temporaryDirectory.resolve("r3-long-token.cedg");
		assertTrue(app.saveGeoGebraFile(document.toFile()));
		AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(document.toFile(), false));
		GeoPoint reopenedPoint = (GeoPoint) reopened.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getGeo(pointId);
		assertNotNull(reopenedPoint);
		assertEquals(expectedToken,
				((AlgoLocusIntersectionPointV2) reopenedPoint.getParentAlgorithm())
						.getSelectedRootToken());
		assertEquals(expectedToken, tokenInput(reopenedPoint).getTextString());
		assertTrue(geos(reopened, GeoText.class).stream().noneMatch(text ->
				compactLabel.get().equals(text.getTextString())));
	}

	@Test
	void t01TokenAuxiliaryIsNotEuclidianVisible() {
		AppGeoCeDG app = enabledApp();
		GeoLocusIntersectionResult rich = createLineIntersection(app, "R",
				"u:x=0");
		app.getSelectionManager().addSelectedGeo(rich);

		GeoText token = tokenInput(acceptFirstToken(app));

		assertTrue(token.isAuxiliaryObject());
		assertFalse(token.isEuclidianVisible());
		assertFalse(token.getTextString().isBlank());
	}

	@Test
	void t02TokenRemainsExactAndHiddenAfterNativeSaveReopen(
			@TempDir Path temporaryDirectory) {
		AppGeoCeDG app = enabledApp();
		GeoLocusIntersectionResult rich = createLineIntersection(app, "R",
				"u:x=0");
		app.getSelectionManager().addSelectedGeo(rich);
		GeoPoint point = acceptFirstToken(app);
		GeoText token = tokenInput(point);
		String tokenValue = token.getTextString();
		PersistentGeoId pointId = persistentId(app, point);
		Path document = temporaryDirectory.resolve("r3-token.cedg");

		assertTrue(app.saveGeoGebraFile(document.toFile()));
		AppGeoCeDG reopened = enabledApp();
		assertTrue(reopened.loadFile(document.toFile(), false));
		GeoPoint reopenedPoint = (GeoPoint) reopened.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getGeo(pointId);
		assertNotNull(reopenedPoint);
		assertTrue(reopenedPoint.isDefined());
		GeoText reopenedToken = tokenInput(reopenedPoint);
		assertEquals(tokenValue, reopenedToken.getTextString());
		assertTrue(reopenedToken.isAuxiliaryObject());
		assertFalse(reopenedToken.isEuclidianVisible());
	}

	@Test
	void t03ClosureCopyRemapsExactTokenAndKeepsAuxiliaryHidden() {
		AppGeoCeDG app = enabledApp();
		GeoLocusIntersectionResult rich = createLineIntersection(app, "R",
				"u:x=0");
		app.getSelectionManager().addSelectedGeo(rich);
		GeoPoint point = acceptFirstToken(app);
		String originalToken = tokenInput(point).getTextString();
		String clipboard = InternalClipboard.getTextToSave(app,
				Collections.singletonList(point), text -> text);

		paste(app, clipboard);

		List<GeoPoint> points = exactTokenPoints(app);
		assertEquals(2, points.size());
		List<String> tokens = points.stream().map(
				G9U0R3InspectorWorkflowTest::tokenInput).peek(token -> {
					assertTrue(token.isAuxiliaryObject());
					assertFalse(token.isEuclidianVisible());
				}).map(GeoText::getTextString).toList();
		assertTrue(tokens.contains(originalToken));
		assertEquals(2, tokens.stream().distinct().count());
		assertNotEquals(tokens.get(0), tokens.get(1));
		assertTrue(points.stream().allMatch(GeoElement::isDefined));
	}

	@Test
	void n01IntersectAloneCreatesNoPersistentPointChildren() {
		AppGeoCeDG app = enabledApp();
		createBaseLocus(app);
		eval(app, "u:x=0");
		long pointsBefore = geos(app, GeoPoint.class).size();

		GeoLocusIntersectionResult rich = (GeoLocusIntersectionResult)
				eval(app, "R=Intersect(L,u)");

		assertTrue(rich.isDefined());
		assertEquals(pointsBefore, geos(app, GeoPoint.class).size());
		assertTrue(exactTokenPoints(app).isEmpty());
	}

	@Test
	void n02R3IntroducesNoRichResultDrawableOrCandidateMarkerGeo() {
		AppGeoCeDG app = enabledApp();
		GeoLocusIntersectionResult rich = createCircleIntersection(app, "R");
		int constructionSize = app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size();

		app.getSelectionManager().addSelectedGeo(rich);
		app.getKernel().notifyRepaint();

		assertFalse(rich.isEuclidianVisible());
		assertEquals(constructionSize, app.getKernel().getConstruction()
				.getGeoSetConstructionOrder().size());
		assertTrue(exactTokenPoints(app).isEmpty());
	}

	private static AppGeoCeDG enabledApp() {
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=true"}), new JPanel());
		app.setErrorDialogsActive(false);
		return app;
	}

	private static GeoLocusV2 createBaseLocus(AppGeoCeDG app) {
		eval(app, "s=0");
		eval(app, "Q=(s,0)");
		eval(app, "D={false,{-2,2,true,true}}");
		return (GeoLocusV2) eval(app, "L=LocusV2(Q,s,D)");
	}

	private static GeoLocusIntersectionResult createCircleIntersection(
			AppGeoCeDG app, String label) {
		createBaseLocus(app);
		eval(app, "c=Circle((0,0),1)");
		return (GeoLocusIntersectionResult) eval(app,
				label + "=Intersect(L,c)");
	}

	private static GeoLocusIntersectionResult createLineIntersection(
			AppGeoCeDG app, String label, String lineCommand) {
		if (app.getKernel().lookupLabel("L") == null) {
			createBaseLocus(app);
		}
		String lineLabel = lineCommand.substring(0, lineCommand.indexOf(':'));
		eval(app, lineCommand);
		return (GeoLocusIntersectionResult) eval(app,
				label + "=Intersect(L," + lineLabel + ")");
	}

	private static GeoLocusIntersectionResult createDoubleIntersection(
			AppGeoCeDG app, String label) {
		eval(app, "s=0");
		eval(app, "Q=(s^2,s)");
		eval(app, "D={false,{-2,0,true,false},{0,2,false,true}}");
		eval(app, "L=LocusV2(Q,s,D)");
		eval(app, "u:x=1");
		return (GeoLocusIntersectionResult) eval(app,
				label + "=Intersect(L,u)");
	}

	private static GeoPoint acceptFirstToken(AppGeoCeDG app) {
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			stubIntersectionDecision(dialogs, JOptionPane.OK_OPTION);
			inspectorItem(app).doClick();
		}
		List<GeoPoint> points = exactTokenPoints(app);
		assertFalse(points.isEmpty());
		return points.get(points.size() - 1);
	}

	private static void stubIntersectionDecision(
			MockedStatic<JOptionPane> dialogs, int decision) {
		dialogs.when(() -> JOptionPane.showConfirmDialog(
				any(Component.class), any(), anyString(),
				eq(JOptionPane.OK_CANCEL_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE))).thenReturn(decision);
	}

	private static void verifyIntersectionInspector(
			MockedStatic<JOptionPane> dialogs) {
		dialogs.verify(() -> JOptionPane.showConfirmDialog(
				any(Component.class), any(), anyString(),
				eq(JOptionPane.OK_CANCEL_OPTION),
				eq(JOptionPane.PLAIN_MESSAGE)), times(1));
	}

	private static void verifyReadOnlyInspector(
			MockedStatic<JOptionPane> dialogs, AppGeoCeDG app) {
		dialogs.verify(() -> JOptionPane.showMessageDialog(
				any(Component.class), any(),
				eq(app.getLocalization().getMenu(
						"LocusV2.Results.Inspect")),
				eq(JOptionPane.INFORMATION_MESSAGE)), times(1));
	}

	private static JMenuItem inspectorItem(AppGeoCeDG app) {
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar menuBar =
				(GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		String inspectorText = app.getLocalization()
				.getMenu("LocusV2.Results.Inspect");
		for (int menuIndex = 0; menuIndex < menuBar.getMenuCount(); menuIndex++) {
			JMenu menu = menuBar.getMenu(menuIndex);
			if (menu == null || !"GeoCeDG".equals(menu.getText())) {
				continue;
			}
			for (Component component : menu.getMenuComponents()) {
				if (component instanceof JMenuItem
						&& inspectorText.equals(
								((JMenuItem) component).getText())) {
					return (JMenuItem) component;
				}
			}
		}
		throw new AssertionError("Rich-result inspector action is missing");
	}

	private static GeoElement eval(AppGeoCeDG app, String command) {
		var output = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(output, command);
		assertTrue(output.length > 0, command);
		return output[0].toGeoElement();
	}

	private static List<GeoPoint> exactTokenPoints(AppGeoCeDG app) {
		return geos(app, GeoPoint.class).stream()
				.filter(point -> point.getParentAlgorithm()
						instanceof AlgoLocusIntersectionPointV2).toList();
	}

	private static List<String> admissibleTokens(
			GeoLocusIntersectionResult rich) {
		return rich.getIntersectionResult().getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken())
				.filter(rich::isPointAdmissible).toList();
	}

	private static <T extends Component> T findComponent(Container root,
			Class<T> type) {
		for (Component component : root.getComponents()) {
			if (type.isInstance(component)) {
				return type.cast(component);
			}
			if (component instanceof Container) {
				T nested = findComponent((Container) component, type);
				if (nested != null) {
					return nested;
				}
			}
		}
		return null;
	}

	private static <T extends GeoElement> List<T> geos(AppGeoCeDG app,
			Class<T> type) {
		List<T> matching = new ArrayList<>();
		for (GeoElement geo : app.getKernel().getConstruction()
				.getGeoSetConstructionOrder()) {
			if (type.isInstance(geo)) {
				matching.add(type.cast(geo));
			}
		}
		return matching;
	}

	private static GeoText tokenInput(GeoPoint point) {
		return (GeoText) point.getParentAlgorithm().getInput(1);
	}

	private static PersistentGeoId persistentId(AppGeoCeDG app,
			GeoElement geo) {
		SpatialIdentityRegistry registry = app.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		PersistentGeoId id = registry.getPersistentGeoId(geo);
		assertNotNull(id);
		return id;
	}

	private static void paste(AppGeoCeDG app, String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(app, labels,
				clipboard.substring(separator));
	}

	private static void await(BooleanSupplier condition) throws Exception {
		long deadline = System.nanoTime() + 5_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean(),
				"Desktop lifecycle state did not converge");
	}
}
