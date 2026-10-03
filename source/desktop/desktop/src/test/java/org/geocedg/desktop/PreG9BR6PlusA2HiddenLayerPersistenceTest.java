/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.algebraView;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.awaitStore;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.baseline;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.constructionXml;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.layerPath;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.pressEye;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.undo;
import static org.geocedg.desktop.PreG9BR6PlusA2LayerDomainDesktopTest.documentXml;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.count;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.png;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.printed;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.sized;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.svg;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.layers.HiddenLayerMetadataException;
import org.geocedg.common.kernel.layers.HiddenLayerSet;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.export.pstricks.ExportFrameMinimal;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.export.pstricks.GeoGebraToAsymptote;
import org.geogebra.common.export.pstricks.GeoGebraToPgf;
import org.geogebra.common.export.pstricks.GeoGebraToPstricks;
import org.geogebra.common.gui.view.algebra.AlgebraView.SortMode;
import org.geogebra.common.jre.main.TemplateHelper;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.error.ErrorHelper;
import org.geogebra.common.util.FileExtensions;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.export.pstricks.ExportGraphicsFactoryD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.MyImageD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.LocalizationD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * PRE-G9B-R6-plus-A-2 Desktop contract of the persistent, non-undoable hidden-layer
 * set: T-SAVED, T-PERSIST-NO-UNDO, T-XML-READER, T-NEW-OPEN, T-OPEN-WORKING-LAYER,
 * T-FAIL-CLOSED, T-ROUTES, T-EFFECTIVE-REOPEN, T-UNITS-INDEPENDENCE and T-INVARIANCE.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusA2HiddenLayerPersistenceTest {
	private static final GColor RED = GColor.newColor(220, 0, 0);
	private static final GColor BLUE = GColor.newColor(0, 0, 210);
	private static final int WIDTH = 400;
	private static final int HEIGHT = 300;
	private static final String MACRO = "<macro cmdName=\"Twice\" toolName=\"Twice\""
			+ " toolHelp=\"\" iconFile=\"\" showInToolBar=\"true\" copyCaptions=\"true\">"
			+ "<macroInput a0=\"A\"/><macroOutput a0=\"B\"/><construction>"
			+ "<element type=\"point\" label=\"A\"><coords x=\"0\" y=\"0\" z=\"1\"/></element>"
			+ "<command name=\"Dilate\"><input a0=\"A\" a1=\"2\"/><output a0=\"B\"/></command>"
			+ "<element type=\"point\" label=\"B\"><coords x=\"0\" y=\"0\" z=\"1\"/></element>"
			+ "</construction></macro>";

	@TempDir
	Path temporary;

	// ------------------------------------------------- T-SAVED, T-PERSIST-NO-UNDO

	@Test
	void aToggleMarksTheDocumentUnsavedButNeverStoresAnUndoPoint() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		onLayer(app, 3, "A=(1,1)");
		onLayer(app, 7, "B=(2,2)");
		workspace.setWorkingLayer(0);
		UndoManagerD undo = undo(app);
		baseline(undo);
		app.setSaved();
		final int history = undo.getHistorySize();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);

		workspace.setWorkingLayer(5);
		assertTrue(app.isSaved(), "a working-layer change alone is session state");
		assertTrue(workspace.setLayerHidden(3, true));
		assertFalse(app.isSaved(), "a toggle changes the persisted set");
		app.setSaved();
		GeoCeDGAlgebraView view = algebraView(app);
		view.setTreeMode(SortMode.LAYER);
		view.setSize(WIDTH, HEIGHT);
		view.doLayout();
		pressEye(view, 7);
		assertFalse(workspace.isLayerShown(7));
		assertFalse(app.isSaved(), "the Algebra View eye");
		app.setSaved();
		workspace.setWorkingLayer(7);
		assertTrue(workspace.isLayerShown(7), "AQ-L7(2)");
		assertFalse(app.isSaved(), "choosing a hidden layer shows it: a persisted change");
		app.setSaved();
		assertFalse(workspace.setLayerHidden(7, true), "the working layer cannot be hidden");
		assertFalse(workspace.setLayerHidden(5, false), "already shown");
		assertTrue(app.isSaved(), "no change, no modification");

		assertFalse(stored.await(2, TimeUnit.SECONDS), "never an undo point");
		assertEquals(history, undo.getHistorySize());
		assertFalse(undo.undoPossible());
		assertEquals(List.of(3), workspace.getHiddenLayers());
	}

	@Test
	void aNonEmptySetIsSaveRelevantEvenInAGeometricallyEmptyDocument() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		assertTrue(app.isSaved(), "an untouched empty document");
		app.setUnsaved();
		assertTrue(app.isSaved(), "upstream: changes of an empty document are ignored");
		app.setSaved();
		assertTrue(workspace.setLayerHidden(4, true));
		assertFalse(app.isSaved(), "New and close prompt: the set is save-relevant");
		assertFalse(app.getKernel().getConstruction().hasSaveRelevantContent(),
				"the construction seam is unchanged");
		assertFalse(app.getKernel().getConstruction().isStarted());
		assertTrue(workspace.setLayerHidden(4, false));
		assertTrue(app.isSaved(), "an empty set is not save-relevant");

		assertTrue(workspace.setLayerHidden(4, true));
		Path file = save(app, "set-only.cedg");
		assertTrue(app.isSaved());
		assertTrue(documentXml(file).contains(element("4")), "a set-only document");
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(file.toFile(), false));
		assertEquals(List.of(4), reopened.getLayerWorkspace().getHiddenLayers());
		assertTrue(reopened.isSaved(), "a commit never marks the document modified");
		assertTrue(reopened.getLayerWorkspace().setLayerHidden(5, true));
		assertFalse(reopened.isSaved(), "the user's change of the opened set");
		reopened.setSaved();
		assertTrue(reopened.getLayerWorkspace().setLayerHidden(4, false));
		assertTrue(reopened.getLayerWorkspace().setLayerHidden(5, false));
		// the host counts an opened document as started, so clearing its set prompts too
		assertTrue(reopened.getKernel().getConstruction().isStarted());
		assertFalse(reopened.isSaved(), "clearing the opened set is a document change");
	}

	@Test
	void undoAndRedoNeverChangeTheSetBeforeOrAfterSaveAndReopen() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		onLayer(app, 3, "A=(1,1)");
		onLayer(app, 7, "B=(2,2)");
		app.getLayerWorkspace().setWorkingLayer(0);
		app.getLayerWorkspace().setLayerHidden(3, true);
		Path file = save(app, "undo.cedg");

		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(file.toFile(), false));
		GeoCeDGLayerWorkspace workspace = reopened.getLayerWorkspace();
		assertEquals(7, workspace.getWorkingLayer(), "the highest shown drawable layer");
		workspace.setWorkingLayer(0);
		UndoManagerD undo = undo(reopened);
		eval(reopened, "C=(3,3)");
		awaitStore(reopened, undo);
		workspace.setLayerHidden(7, true);
		List<Integer> set = List.of(3, 7);
		assertEquals(set, workspace.getHiddenLayers());
		reopened.getKernel().undo();
		assertNull(reopened.getKernel().lookupLabel("C"), "the undo happened");
		assertEquals(set, workspace.getHiddenLayers(), "undo");
		reopened.getKernel().redo();
		assertNotNull(reopened.getKernel().lookupLabel("C"), "the redo happened");
		assertEquals(set, workspace.getHiddenLayers(), "redo");

		reopened.getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				lookup(reopened, "A"), "Midpoint(B,C)", new EvalInfo(true, true), false, null,
				ErrorHelper.silent());
		assertFalse(lookup(reopened, "A").isIndependent(), "the redefinition rebuilt");
		assertEquals(set, workspace.getHiddenLayers(), "redefine rebuild");
		InternalClipboard.duplicate(reopened, List.of(lookup(reopened, "B")));
		assertEquals(set, workspace.getHiddenLayers(), "paste");
		awaitStore(reopened, undo);
		reopened.getKernel().undo();
		reopened.getKernel().undo();
		assertEquals(set, workspace.getHiddenLayers(), "undo of a rebuild and a paste");
		assertTrue(reopened.getXML().contains(element("3 7")));
		assertFalse(reopened.getKernel().getConstruction().getCurrentUndoXML(true).toString()
				.contains("geocedgHiddenLayers"), "never in the undo snapshot");
	}

	// ------------------------------------------------- T-XML-READER, T-NEW-OPEN

	@Test
	void saveAndOpenRoundTripLayersAboveNineAndTheSet() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		onLayer(app, 10, "A=(1,1)");
		onLayer(app, 50, "B=(2,1)");
		onLayer(app, 99, "C=(3,1)");
		app.getLayerWorkspace().setWorkingLayer(0);
		app.getLayerWorkspace().setLayerHidden(10, true);
		app.getLayerWorkspace().setLayerHidden(50, true);
		Path file = save(app, "domain.cedg");
		String saved = documentXml(file);
		for (int layer : new int[] {10, 50, 99}) {
			assertTrue(saved.contains("<layer val=\"" + layer + "\"/>"), "layer " + layer);
		}
		assertTrue(saved.contains("</construction>\n" + element("10 50") + "\n</geogebra>"),
				"DQ-A2-3: its own line after the construction");

		AppGeoCeDG reopened = G9U1TestApp.create();
		reopened.getLayerWorkspace().setLayerHidden(5, true);
		reopened.setSaved();
		assertTrue(reopened.loadFile(file.toFile(), false));
		assertEquals(10, lookup(reopened, "A").getLayer());
		assertEquals(50, lookup(reopened, "B").getLayer());
		assertEquals(99, lookup(reopened, "C").getLayer());
		assertEquals(99, reopened.getMaxLayerUsed());
		assertEquals(List.of(10, 50), reopened.getLayerWorkspace().getHiddenLayers(),
				"the document's set replaces the session's");
		assertEquals(99, reopened.getLayerWorkspace().getWorkingLayer());
		assertTrue(reopened.isSaved());
		Path again = save(reopened, "again.cedg");
		assertEquals(saved.substring(saved.indexOf("<construction")),
				documentXml(again).substring(documentXml(again).indexOf("<construction")),
				"an unchanged document re-saves its construction and set identically");
	}

	@Test
	void openTakesTheDocumentSetAndChoosesAShownWorkingLayer() throws Exception {
		Object[][] cases = {
			// used layers, persisted set, working layer after Open (C8, DQ-A2-1)
			{new int[] {2, 6}, new int[] {}, 6},
			{new int[] {2, 6}, new int[] {6}, 2},
			{new int[] {0, 4}, new int[] {0}, 4},
			{new int[] {}, new int[] {0}, 1},
			{new int[] {2, 6}, new int[] {2, 6}, 0},
			{new int[] {2, 6}, new int[] {0, 1, 2, 6}, 3},
			{new int[] {}, new int[] {}, 0},
			{new int[] {5}, new int[] {5}, 0},
			{new int[] {3, 50}, new int[] {}, 50},
			{new int[] {3, 99}, new int[] {99}, 3}
		};
		for (int i = 0; i < cases.length; i++) {
			int[] hidden = (int[]) cases[i][1];
			Path file = document("case-" + i + ".cedg", (int[]) cases[i][0], hidden);
			AppGeoCeDG reopened = G9U1TestApp.create();
			GeoCeDGLayerWorkspace workspace = reopened.getLayerWorkspace();
			workspace.setWorkingLayer(9);
			workspace.setLayerHidden(8, true);
			reopened.setSaved();
			assertTrue(reopened.loadFile(file.toFile(), false));
			List<Integer> persisted = Arrays.stream(hidden).boxed().toList();
			assertEquals(persisted, workspace.getHiddenLayers(), "never unhides: case " + i);
			assertEquals(cases[i][2], workspace.getWorkingLayer(), "case " + i);
			assertTrue(workspace.isLayerShown(workspace.getWorkingLayer()));
			assertTrue(reopened.isSaved(), "Open never marks the document modified");
			assertEquals(persisted.isEmpty(), !reopened.getXML().contains("geocedgHiddenLayers"),
					"the persisted set is unchanged by Open");
			for (GeoElement geo : reopened.getKernel().getConstruction()
					.getGeoSetConstructionOrder()) {
				assertTrue(geo.isEuclidianVisible(), "object visibility is never written");
			}
		}
	}

	// -------------------------------------------------------------- T-FAIL-CLOSED

	@Test
	void everyRejectedOpenRestoresTheSetAndTheWorkingLayerExactly() throws Exception {
		AppGeoCeDG live = G9U1TestApp.create();
		live.setLocale(Locale.ENGLISH);
		GeoCeDGLayerWorkspace workspace = live.getLayerWorkspace();
		onLayer(live, 3, "A=(1,2)");
		onLayer(live, 7, "B=(3,4)");
		workspace.setLayerHidden(3, true);
		Path livePath = save(live, "live.cedg");
		eval(live, "C=(5,6)");
		workspace.setWorkingLayer(5);
		live.setUnsaved();
		UndoManagerD undo = undo(live);
		List<String> messages = new ArrayList<>();
		live.setUnitLoadErrorSink(messages::add);
		final String liveXml = live.getXML();
		final File currentFile = live.getCurrentFile();
		final byte[] liveBytes = Files.readAllBytes(livePath);
		final int history = undo.getHistorySize();
		String base = live.getXML().replace(element("3") + "\n", "");
		int construction = base.indexOf('>', base.indexOf("<construction")) + 1;
		Object[][] defects = {
			{withRootChild(base, "<geocedgHiddenLayers version=\"2\" layers=\"4\"/>"),
				HiddenLayerMetadataException.Code.UNSUPPORTED_VERSION},
			{withRootChild(base, "<geocedgHiddenLayers version=\"1\" layers=\"4,6\"/>"),
				HiddenLayerMetadataException.Code.MALFORMED_ELEMENT},
			{withRootChild(base, element("100")),
				HiddenLayerMetadataException.Code.OUT_OF_DOMAIN_LAYER},
			{withRootChild(base, element(IntStream.rangeClosed(0, 99)
					.mapToObj(Integer::toString).collect(Collectors.joining(" ")))),
				HiddenLayerMetadataException.Code.ALL_LAYERS_HIDDEN},
			{withRootChild(base, element("4") + element("6")),
				HiddenLayerMetadataException.Code.DUPLICATE_ELEMENT},
			{base.substring(0, construction) + element("4") + base.substring(construction),
				HiddenLayerMetadataException.Code.MISPLACED_ELEMENT},
			{withRootChild(base, "<geocedgHiddenLayers version=\"1\" layers=\"4\">x"
					+ "</geocedgHiddenLayers>"),
				HiddenLayerMetadataException.Code.MALFORMED_ELEMENT}
		};
		for (int i = 0; i < defects.length; i++) {
			for (String extension : new String[] {".cedg", ".ggb"}) {
				Path file = temporary.resolve("defect-" + i + extension);
				byte[] bytes = archive("geogebra.xml", (String) defects[i][0]);
				Files.write(file, bytes);
				messages.clear();
				assertFalse(live.loadFile(file.toFile(), false), file.toString());
				assertArrayEquals(bytes, Files.readAllBytes(file));
				assertArrayEquals(liveBytes, Files.readAllBytes(livePath));
				assertEquals(liveXml, live.getXML(), file.toString());
				assertEquals(List.of(3), workspace.getHiddenLayers());
				assertEquals(5, workspace.getWorkingLayer());
				assertEquals(currentFile, live.getCurrentFile());
				assertFalse(live.isSaved());
				assertEquals(history, undo.getHistorySize());
				assertEquals(List.of(live.layerText("Workspace.Layer.LoadError."
						+ defects[i][1], file.getFileName().toString(), "99")), messages);
			}
		}
		live.setLocale(Locale.forLanguageTag("es"));
		messages.clear();
		Path spanish = temporary.resolve("defecto.cedg");
		Files.write(spanish, archive("geogebra.xml", (String) defects[3][0]));
		assertFalse(live.loadFile(spanish.toFile(), false));
		assertEquals(List.of("No se puede abrir defecto.cedg: sus metadatos de capas ocultas"
				+ " ocultan todas las capas de 0 a 99."), messages);

		// a live load that fails after the preflight accepted the document
		Path valid = document("valid.cedg", new int[] {2, 6}, 6);
		live.getKernel().setUserStopsLoading(true);
		assertFalse(live.loadFile(valid.toFile(), false));
		assertEquals(List.of(3), workspace.getHiddenLayers());
		assertEquals(5, workspace.getWorkingLayer());
		assertEquals(Archives.constructionSection(liveXml),
				Archives.constructionSection(live.getXML()));
		assertTrue(live.getXML().contains(element("3")));
		assertFalse(live.isSaved());
		assertTrue(live.loadFile(valid.toFile(), false), "the same document opens later");
		assertEquals(List.of(6), workspace.getHiddenLayers());
		assertEquals(2, workspace.getWorkingLayer());
	}

	@Test
	void aRejectedClearingSetXmlRestoresTheSetAndTheWorkingLayer() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		onLayer(app, 3, "A=(1,2)");
		onLayer(app, 7, "B=(3,4)");
		workspace.setLayerHidden(3, true);
		workspace.setWorkingLayer(5);
		List<String> messages = new ArrayList<>();
		app.setUnitLoadErrorSink(messages::add);
		String xml = app.getXML();
		String rejected = withRootChild(xml.replace(element("3") + "\n", ""),
				element(IntStream.rangeClosed(0, 99).mapToObj(Integer::toString)
						.collect(Collectors.joining(" "))));
		app.getGgbApi().setXML(rejected);
		assertEquals(List.of(3), workspace.getHiddenLayers());
		assertEquals(5, workspace.getWorkingLayer(),
				"the rejected-parse restore is no transition: the working layer stays");
		assertEquals(Archives.constructionSection(xml),
				Archives.constructionSection(app.getXML()));
		assertTrue(app.getXML().contains(element("3")));
		assertEquals(List.of(app.layerText("Workspace.Layer.LoadError.ALL_LAYERS_HIDDEN",
				app.layerText("Units.LoadError.Document"), "99")), messages);
	}

	@Test
	void aLoadThatFailsAfterTheParseReportedNeverReachesTheCommitHook() throws Exception {
		Path valid = document("late.cedg", new int[] {2, 6}, 6);
		LateFailingApp host = new LateFailingApp();
		host.failNextCommit = true;
		assertFalse(host.loadFile(valid.toFile(), false), "fails after the whole parse");
		assertTrue(host.reports().contains(HiddenLayerSet.of(6)), "the parse had reported");
		assertEquals(0, host.commits, "a failed load never reaches the commit hook");
		assertTrue(host.loadFile(valid.toFile(), false));
		assertEquals(1, host.commits);
		assertEquals(HiddenLayerSet.of(6), host.reports().get(host.reports().size() - 1));
	}

	// ------------------------------------------------------------------- T-ROUTES

	@Test
	void onlyDocumentTransitionsCommitAndEveryOtherRouteLeavesTheSet() throws Exception {
		Path sourceFile = document("source.cedg", new int[] {2, 6}, 6);
		final String sourceXml = documentXml(sourceFile);
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGLayerWorkspace workspace = app.getLayerWorkspace();
		onLayer(app, 4, "A=(1,1)");
		workspace.setWorkingLayer(0);
		workspace.setLayerHidden(4, true);
		List<Integer> own = List.of(4);

		app.getGgbApi().evalXML("<element type=\"point\" label=\"E\"><coords x=\"1\" y=\"1\""
				+ " z=\"1\"/></element>" + element("6"));
		assertNotNull(app.getKernel().lookupLabel("E"));
		assertEquals(own, workspace.getHiddenLayers(), "evalXML");
		String mergeXml = "<geogebra format=\"5.0\"><construction title=\"\" author=\"\""
				+ " date=\"\"><element type=\"point\" label=\"Mm\"><coords x=\"7\" y=\"7\""
				+ " z=\"1\"/></element></construction>\n" + element("1") + "\n</geogebra>";
		app.setXML(mergeXml, false);
		assertNotNull(app.getKernel().lookupLabel("Mm"), "the merge happened");
		assertEquals(own, workspace.getHiddenLayers(), "a non-clearing merge");
		InternalClipboard.duplicate(app, List.of(lookup(app, "A")));
		assertEquals(own, workspace.getHiddenLayers(), "paste");

		AppGeoCeDG helper = G9U1TestApp.withoutWindowDispatcher(
				(AppGeoCeDG) app.newAppForTemplateOrInsertFile());
		assertTrue(helper.loadFile(sourceFile.toFile(), false));
		assertEquals(List.of(6), helper.getLayerWorkspace().getHiddenLayers(),
				"the hidden helper's own document");
		int before = app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
		((GeoCeDGCopyPaste) app.getCopyPaste()).insertFrom(helper, app,
				new LinkedHashSet<>(), false);
		assertTrue(app.getKernel().getConstruction().getGeoSetConstructionOrder().size()
				> before, "Insert File inserted the objects");
		assertEquals(own, workspace.getHiddenLayers(), "Insert File never imports a set");
		new TemplateHelper(app).applyTemplate(helper);
		assertEquals(own, workspace.getHiddenLayers(), "Apply Template never imports a set");

		Path tools = temporary.resolve("tools.ggt");
		Files.write(tools, archive("geogebra_macro.xml", "<geogebra format=\"5.0\">" + MACRO
				+ element("9") + "</geogebra>"));
		assertTrue(app.loadFile(tools.toFile(), true));
		assertNotNull(app.getKernel().getMacro("Twice"));
		assertEquals(own, workspace.getHiddenLayers(), "a tool file");

		UndoManagerD undo = undo(app);
		baseline(undo);
		app.documentHiddenLayersParsed(HiddenLayerSet.of(8));
		eval(app, "Z=(9,9)");
		awaitStore(app, undo);
		app.getKernel().undo();
		assertNull(app.getKernel().lookupLabel("Z"), "the undo restore parsed");
		assertEquals(own, workspace.getHiddenLayers(), "a report outside a transition");

		// document transitions
		app.getGgbApi().setXML(sourceXml);
		assertEquals(List.of(6), workspace.getHiddenLayers(), "the clearing setXML");
		assertEquals(2, workspace.getWorkingLayer());
		app.getGgbApi().setXML(sourceXml.replace(element("6") + "\n", ""));
		assertTrue(workspace.getHiddenLayers().isEmpty(), "a legacy document via setXML");
		workspace.setLayerHidden(4, true);
		assertTrue(app.loadXML(sourceXml), "loadXML(String)");
		assertEquals(List.of(6), workspace.getHiddenLayers());
		workspace.setWorkingLayer(0);
		workspace.setLayerHidden(2, true);
		Path other = document("other.cedg", new int[] {1, 3}, 3);
		app.getGgbApi().openFile(other.toUri().toString());
		assertEquals(List.of(3), workspace.getHiddenLayers(), "the API file route");
		assertEquals(1, workspace.getWorkingLayer());
		app.setSaved();
		assertTrue(app.loadFile(sourceFile.toFile(), false));
		assertEquals(List.of(6), workspace.getHiddenLayers(), "File > Open");
		workspace.setLayerHidden(6, false);
		app.setSaved();
		app.reset();
		assertEquals(List.of(6), workspace.getHiddenLayers(), "reset reloads the file");
		app.setSaved();
		app.fileNew();
		assertTrue(workspace.getHiddenLayers().isEmpty(), "New");
		assertEquals(0, workspace.getWorkingLayer());
		assertTrue(app.isSaved());
	}

	@Test
	void aPasteAcrossDocumentsNeverCarriesTheSourceSet() throws Exception {
		Path target = document("target.cedg", new int[] {1, 6}, 6);
		AppGeoCeDG app = G9U1TestApp.create();
		onLayer(app, 4, "K=(1,1)");
		app.getLayerWorkspace().setWorkingLayer(0);
		app.getLayerWorkspace().setLayerHidden(4, true);
		app.getSelectionManager().setSelectedGeos(new ArrayList<>(List.of(lookup(app, "K"))));
		app.getGlobalKeyDispatcher().dispatchKeyEvent(ctrl(app, KeyEvent.VK_C));
		app.setSaved();
		assertTrue(app.loadFile(target.toFile(), false));
		assertEquals(List.of(6), app.getLayerWorkspace().getHiddenLayers());
		List<GeoElement> before = new ArrayList<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
		app.getGlobalKeyDispatcher().dispatchKeyEvent(ctrl(app, KeyEvent.VK_V));
		List<GeoElement> created = new ArrayList<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
		created.removeAll(before);
		assertEquals(1, created.size(), "the paste happened");
		assertEquals(4, created.get(0).getLayer(), "AQ-L8: the copied layer");
		assertEquals(List.of(6), app.getLayerWorkspace().getHiddenLayers(),
				"the source document's set never travels with the clipboard");
		assertTrue(app.isLayerShown(4), "so the pasted object is shown in the target");
	}

	@Test
	void anArchiveWithMacrosKeepsItsSet() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		assertTrue(app.addMacroXML(MACRO));
		onLayer(app, 5, "P=(1,1)");
		app.getLayerWorkspace().setWorkingLayer(0);
		app.getLayerWorkspace().setLayerHidden(5, true);
		Path file = save(app, "macros.cedg");
		assertTrue(Archives.entryNames(file)
				.contains("geogebra_macro.xml"), "the archive really contains macros");
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(file.toFile(), false));
		assertEquals(List.of(5), reopened.getLayerWorkspace().getHiddenLayers());
		assertFalse(reopened.getMacroXML(reopened.getKernel().getMacro("Twice"))
				.contains("geocedgHiddenLayers"), "tool XML never carries the element");
	}

	@Test
	void startupWithADocumentTakesItsSetAndAFailedStartupEndsBlank() throws Exception {
		Path file = document("startup.cedg", new int[] {2, 6}, 6);
		AppGeoCeDG started = G9U1TestApp.withoutWindowDispatcher(new AppGeoCeDG(
				new CommandLineArguments(new String[] {"--silent", file.toString()}),
				new JPanel()));
		started.setErrorDialogsActive(false);
		assertEquals(List.of(6), started.getLayerWorkspace().getHiddenLayers());
		assertEquals(2, started.getLayerWorkspace().getWorkingLayer());
		assertTrue(started.isSaved());

		AppGeoCeDG author = G9U1TestApp.create();
		Path defect = temporary.resolve("startup-defect.cedg");
		Files.write(defect, archive("geogebra.xml", withRootChild(author.getXML(),
				element(IntStream.rangeClosed(0, 99).mapToObj(Integer::toString)
						.collect(Collectors.joining(" "))))));
		// the host queues its error dialog on the EDT, so the static mock lives there
		List<String> shown = new CopyOnWriteArrayList<>();
		AtomicReference<MockedStatic<JOptionPane>> pane = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			MockedStatic<JOptionPane> mock = Mockito.mockStatic(JOptionPane.class);
			mock.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(), anyInt(),
					anyInt())).thenAnswer(invocation -> {
						shown.add(String.valueOf(invocation.getArgument(1, Object.class)));
						return JOptionPane.OK_OPTION;
					});
			pane.set(mock);
		});
		try {
			AppGeoCeDG failed = G9U1TestApp.withoutWindowDispatcher(new AppGeoCeDG(
					new CommandLineArguments(new String[] {"--silent", defect.toString()}),
					new JPanel()));
			failed.setErrorDialogsActive(false);
			SwingUtilities.invokeAndWait(() -> { });
			assertTrue(failed.getLayerWorkspace().getHiddenLayers().isEmpty(),
					"a failed startup load ends in a blank document with an empty set");
			assertEquals(0, failed.getLayerWorkspace().getWorkingLayer());
			assertTrue(failed.getKernel().getConstruction().getGeoSetConstructionOrder()
					.isEmpty());
			assertEquals(1, shown.size(), String.valueOf(shown));
			assertTrue(shown.get(0).contains("startup-defect.cedg"), shown.get(0));
		} finally {
			SwingUtilities.invokeAndWait(() -> pane.get().close());
		}
	}

	// --------------------------------------------------------- T-EFFECTIVE-REOPEN

	@Test
	void effectiveVisibilityIsTheSameBeforeSavingAndAfterReopening() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianViewD second = secondView(app);
		GeoElement hidden = segment(app, 3, "seg=Segment((0,0),(4,0))", RED);
		GeoElement shown = segment(app, 0, "top=Segment((0,2),(4,2))", BLUE);
		for (GeoElement geo : new GeoElement[] {hidden, shown}) {
			geo.setVisibility(app.getEuclidianView1().getViewID(), true);
			geo.setVisibility(second.getViewID(), true);
			second.add(geo);
		}
		app.getLayerWorkspace().setLayerHidden(3, true);
		List<Object> before = effective(app, second);
		assertEquals(List.of(false, true, false, true), before.subList(0, 4),
				"Graphics 1 and 2 omit layer 3");
		assertEquals(List.of(false, true), before.subList(7, 9), "the PNG export agrees");
		assertEquals(List.of(false, false, true), before.subList(9, 12),
				"print omits it, SVG writes layer 0 only");
		for (Object preview : before.subList(12, 16)) {
			assertTrue(preview.toString().endsWith(":false:true"), "Save preview " + preview);
		}
		Path file = save(app, "effective.cedg");

		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(file.toFile(), false));
		EuclidianViewD reopenedSecond = secondView(reopened);
		assertEquals(before, effective(reopened, reopenedSecond),
				"Graphics 1 and 2, hit testing, the Algebra View, every picture export and"
						+ " its Save preview, LaTeX and DXF");
		reopened.getLayerWorkspace().setLayerHidden(3, false);
		List<Object> visible = effective(reopened, reopenedSecond);
		assertEquals(List.of(true, true, true, true), visible.subList(0, 4),
				"showing the layer again paints it in both views");
	}

	// ------------------------------------------------------- T-UNITS-INDEPENDENCE

	@Test
	void unitsAndTheSetRoundTripAndUndoIndependently() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		onLayer(app, 3, "A=(1,2)");
		app.getLayerWorkspace().setWorkingLayer(0);
		UndoManagerD undo = undo(app);
		baseline(undo);
		int history = undo.getHistorySize();
		app.getLayerWorkspace().setLayerHidden(3, true);
		assertEquals(history, undo.getHistorySize(), "a set change: no unit undo point");
		assertTrue(app.getDocumentUnits().getState().isEmpty());
		assertFalse(app.getKernel().getConstruction().getCurrentUndoXML(true).toString()
				.contains("geocedgUnits"));

		UnitState mm = UnitState.of(UnitToken.MM, null, null);
		app.setDocumentUnitsPrompt((owner, initial) -> new GeoCeDGDocumentUnits.Request(
				UnitToken.MM, null, false, null, null, null));
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);
		assertTrue(app.editDocumentUnits());
		assertTrue(stored.await(5, TimeUnit.SECONDS), "the unit change is one undo point");
		assertEquals(mm, app.getDocumentUnits().getState());
		assertEquals(List.of(3), app.getLayerWorkspace().getHiddenLayers());
		app.getKernel().undo();
		assertTrue(app.getDocumentUnits().getState().isEmpty(), "the unit change is undone");
		assertEquals(List.of(3), app.getLayerWorkspace().getHiddenLayers(), "the set is not");
		app.getKernel().redo();
		assertEquals(mm, app.getDocumentUnits().getState());
		assertEquals(List.of(3), app.getLayerWorkspace().getHiddenLayers());

		Path file = save(app, "both.cedg");
		String xml = documentXml(file);
		AppGeoCeDG both = open(file);
		assertEquals(mm, both.getDocumentUnits().getState());
		assertEquals(List.of(3), both.getLayerWorkspace().getHiddenLayers());
		AppGeoCeDG unitsOnly = open(write("units-only.cedg",
				xml.replace(element("3") + "\n", "")));
		assertEquals(mm, unitsOnly.getDocumentUnits().getState());
		assertTrue(unitsOnly.getLayerWorkspace().getHiddenLayers().isEmpty());
		AppGeoCeDG layersOnly = open(write("layers-only.cedg",
				xml.replaceAll("\\s*<geocedgUnits[^>]*/>", "")));
		assertTrue(layersOnly.getDocumentUnits().getState().isEmpty());
		assertEquals(List.of(3), layersOnly.getLayerWorkspace().getHiddenLayers());

		AppGeoCeDG live = G9U1TestApp.create();
		live.setLocale(Locale.ENGLISH);
		List<String> messages = new ArrayList<>();
		live.setUnitLoadErrorSink(messages::add);
		Path unitDefect = write("unit-defect.cedg",
				xml.replaceAll("<geocedgUnits[^>]*/>", "<geocedgUnits version=\"2\"/>"));
		assertFalse(live.loadFile(unitDefect.toFile(), false));
		Path layerDefect = write("layer-defect.cedg", xml.replace(element("3"),
				"<geocedgHiddenLayers version=\"2\" layers=\"3\"/>"));
		assertFalse(live.loadFile(layerDefect.toFile(), false));
		assertEquals(List.of(live.layerText("Units.LoadError.UNSUPPORTED_VERSION",
				"unit-defect.cedg"), live.layerText(
						"Workspace.Layer.LoadError.UNSUPPORTED_VERSION", "layer-defect.cedg",
						"99")), messages, "each family reports its own diagnostic");
	}

	// ---------------------------------------------------------------- T-INVARIANCE

	@Test
	void geometryIsInvariantUnderEveryLayerRoute() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		List<String> labels = List.of("A", "B", "C", "E", "f", "c", "poly", "d", "t", "M",
				"s", "Q", "D", "L", "S");
		for (String command : new String[] {"A=(0,0)", "B=(4,1)", "C=(1,3)", "E=(5,0)",
				"f=Line(A,B)", "c=Circle(A,2)", "poly=Polygon(A,B,C)", "d=Distance(A,B)",
				"t=Slider(0,1,0.1)", "M=(t,t^2)", "s=0", "Q=(s,s^2)",
				"D={false,{-2,-0.5,true,true},{0.5,2,true,true}}", "L=LocusV2(Q,s,D)",
				"S=SplineV2({A,B,C,E},3)"}) {
			eval(app, command);
		}
		assertTrue(((GeoLocusV2) lookup(app, "L")).getSemanticDefinition().getBranches()
				.get(0).getValidDomainComponents().size() > 1,
				"a Locus V2 whose branch has several components");
		final List<String> reference = invariants(app, labels);

		run(app, "SetLayer(A,10)");
		app.getGgbApi().setLayer("f", 50);
		lookup(app, "L").setLayer(99);
		lookup(app, "S").setLayer(42);
		assertEquals(reference, invariants(app, labels), "domain-widening routes");
		app.getLayerWorkspace().setLayerHidden(10, true);
		app.getLayerWorkspace().setLayerHidden(99, true);
		assertEquals(reference, invariants(app, labels), "hidden-layer changes");
		Path file = save(app, "invariants.cedg");
		AppGeoCeDG reopened = open(file);
		assertEquals(reference, invariants(reopened, labels), "save and reopen");
		assertEquals(List.of(10, 99), reopened.getLayerWorkspace().getHiddenLayers());
		UndoManagerD undo = undo(reopened);
		eval(reopened, "Z=(9,9)");
		awaitStore(reopened, undo);
		reopened.getKernel().undo();
		assertEquals(reference, invariants(reopened, labels), "undo");
		reopened.getKernel().redo();
		assertNotNull(reopened.getKernel().lookupLabel("Z"), "the redo happened");
		assertEquals(reference.subList(0, labels.size()),
				invariants(reopened, labels).subList(0, labels.size()), "redo");
	}

	// --------------------------------------------------------------------- helpers

	/** A GeoCeDG-configured host whose next native undo-baseline commit fails late. */
	private static final class LateFailingApp extends AppD {
		// No field initializers: the host constructor already parses XML.
		private boolean failNextCommit;
		private int commits;
		private List<HiddenLayerSet> reports;

		private LateFailingApp() {
			super(new CommandLineArguments(new String[] {"--silent"}), null, new JPanel(),
					true, new LocalizationD(3), new AppConfigGeoCeDG(true));
			setErrorDialogsActive(false);
		}

		List<HiddenLayerSet> reports() {
			if (reports == null) {
				reports = new ArrayList<>();
			}
			return reports;
		}

		@Override
		public void documentHiddenLayersParsed(HiddenLayerSet hiddenLayers) {
			reports().add(hiddenLayers);
		}

		@Override
		protected void beforeNativeUndoBaselineCommit() {
			if (failNextCommit) {
				failNextCommit = false;
				throw new SecurityException("injected live-load failure");
			}
		}

		@Override
		protected void nativeDocumentLoadCommitted() {
			commits++;
		}
	}

	/** Document archive inspection. */
	private static final class Archives {
		static String constructionSection(String documentXml) {
			int start = documentXml.indexOf("<construction");
			int end = documentXml.indexOf("</construction>") + "</construction>".length();
			assertTrue(start >= 0 && end > start, "a construction section");
			return documentXml.substring(start, end);
		}

		static List<String> entryNames(Path file) throws IOException {
			List<String> names = new ArrayList<>();
			try (java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(
					Files.newInputStream(file))) {
				for (ZipEntry entry = zip.getNextEntry(); entry != null;
						entry = zip.getNextEntry()) {
					names.add(entry.getName());
				}
			}
			return names;
		}
	}

	private static KeyEvent ctrl(AppGeoCeDG app, int keyCode) {
		return new KeyEvent(app.getEuclidianView1().getJPanel(), KeyEvent.KEY_PRESSED,
				System.currentTimeMillis(), InputEvent.CTRL_DOWN_MASK, keyCode,
				KeyEvent.CHAR_UNDEFINED);
	}

	private static String element(String layers) {
		return "<geocedgHiddenLayers version=\"1\" layers=\"" + layers + "\"/>";
	}

	private static String withRootChild(String documentXml, String child) {
		return documentXml.replace("</geogebra>", child + "\n</geogebra>");
	}

	private static byte[] archive(String... entries) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
			for (int i = 0; i < entries.length; i += 2) {
				zip.putNextEntry(new ZipEntry(entries[i]));
				zip.write(entries[i + 1].getBytes(StandardCharsets.UTF_8));
				zip.closeEntry();
			}
		}
		return bytes.toByteArray();
	}

	private Path write(String name, String documentXml) throws IOException {
		Path file = temporary.resolve(name);
		Files.write(file, archive("geogebra.xml", documentXml));
		return file;
	}

	private static GeoElement onLayer(AppGeoCeDG app, int layer, String command) {
		app.getLayerWorkspace().setWorkingLayer(layer);
		GeoElement geo = eval(app, command);
		assertEquals(layer, geo.getLayer(), command);
		return geo;
	}

	private Path save(AppGeoCeDG app, String name) {
		Path file = temporary.resolve(name);
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		return file;
	}

	private static AppGeoCeDG open(Path file) {
		AppGeoCeDG app = G9U1TestApp.create();
		assertTrue(app.loadFile(file.toFile(), false), file.toString());
		return app;
	}

	/** Saves a document whose drawable points use the given layers and hidden set. */
	private Path document(String name, int[] used, int... hidden) {
		AppGeoCeDG author = G9U1TestApp.create();
		for (int i = 0; i < used.length; i++) {
			onLayer(author, used[i], "P_{" + i + "}=(" + i + ",0)");
		}
		int working = 0;
		while (contains(hidden, working)) {
			working++;
		}
		author.getLayerWorkspace().setWorkingLayer(working);
		for (int layer : hidden) {
			assertTrue(author.getLayerWorkspace().setLayerHidden(layer, true));
		}
		return save(author, name);
	}

	private static boolean contains(int[] values, int value) {
		return Arrays.stream(values).anyMatch(v -> v == value);
	}

	private static void run(AppGeoCeDG app, String command) {
		try (MockedStatic<JOptionPane> pane = Mockito.mockStatic(JOptionPane.class)) {
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
			pane.verifyNoInteractions();
		}
	}

	private static EuclidianViewD secondView(AppGeoCeDG app) throws Exception {
		sized(app.getEuclidianView1());
		EuclidianViewD second = ((GuiManagerD) app.getGuiManager()).getEuclidianView2(1);
		second.attachView();
		sized(second);
		return second;
	}

	private static GeoElement segment(AppGeoCeDG app, int layer, String command,
			GColor color) {
		GeoElement segment = onLayer(app, layer, command);
		app.getLayerWorkspace().setWorkingLayer(0);
		segment.setObjColor(color);
		segment.setLineOpacity(255);
		segment.setLineThickness(13);
		segment.setLabelVisible(false);
		segment.updateRepaint();
		return segment;
	}

	/**
	 * Effective visibility of the red segment on layer 3 and the blue one on layer 0:
	 * Graphics 1 red, blue; Graphics 2 red, blue; hits; the Algebra View group; PNG,
	 * print and SVG export; the Save preview of every picture format; LaTeX and DXF.
	 */
	private static List<Object> effective(AppGeoCeDG app, EuclidianViewD second)
			throws Exception {
		EuclidianView first = app.getEuclidianView1();
		List<Object> facts = new ArrayList<>();
		BufferedImage live = paint(first);
		BufferedImage other = paint(second);
		facts.add(count(live, RED) > 0);
		facts.add(count(live, BLUE) > 0);
		facts.add(count(other, RED) > 0);
		facts.add(count(other, BLUE) > 0);
		facts.add(hitLabels(first, 2, 0));
		facts.add(hitLabels(first, 2, 2));
		facts.add(algebraGroup(app, 3));
		facts.add(count(png(app, first, 1), RED) > 0);
		facts.add(count(png(app, first, 1), BLUE) > 0);
		facts.add(count(printed(app, first), RED) > 0);
		String svg = svg(app, first, -1);
		facts.add(svg.contains("layer3"));
		facts.add(svg.contains("layer0"));
		for (FileExtensions extension : new FileExtensions[] {FileExtensions.PNG,
				FileExtensions.PDF, FileExtensions.SVG, FileExtensions.EMF}) {
			BufferedImage preview = (BufferedImage) ((MyImageD) app.getSavePreviewImage(
					extension, org.geogebra.common.io.MyXMLio.THUMBNAIL_PIXELS_X,
					org.geogebra.common.io.MyXMLio.THUMBNAIL_PIXELS_Y)).getImage();
			facts.add(extension + ":" + (count(preview, RED) > 0) + ":"
					+ (count(preview, BLUE) > 0));
		}
		facts.addAll(latex(app));
		GeometryExportService service = new GeometryExportService();
		facts.add(service.exportDxf(service.createModel(new ArrayList<>(app.getKernel()
				.getConstruction().getGeoSetConstructionOrder()),
				SelectionMode.COMPLETE_CONSTRUCTION)));
		return facts;
	}

	private static BufferedImage paint(EuclidianView view) throws Exception {
		BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		SwingUtilities.invokeAndWait(() -> {
			Graphics2D g = image.createGraphics();
			g.setColor(java.awt.Color.WHITE);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			view.updateAllDrawables(true);
			view.paint(new GGraphics2DD(g));
			g.dispose();
		});
		return image;
	}

	private static List<String> hitLabels(EuclidianView view, double x, double y) {
		view.updateAllDrawables(true);
		view.setHits(new GPoint(view.toScreenCoordX(x), view.toScreenCoordY(y)),
				PointerEventType.MOUSE);
		return view.getHits().stream().map(GeoElement::getLabelSimple).sorted().toList();
	}

	private static String algebraGroup(AppGeoCeDG app, int layer) {
		GeoCeDGAlgebraView view = algebraView(app);
		view.setTreeMode(SortMode.LAYER);
		view.setSize(WIDTH, HEIGHT);
		view.doLayout();
		TreePath path = layerPath(view, layer);
		Component cell = view.getCellRenderer().getTreeCellRendererComponent(view,
				path.getLastPathComponent(), false, true, false, 0, false);
		return ((JLabel) cell).getText();
	}

	private static List<String> latex(AppGeoCeDG app) {
		EuclidianView view = app.getEuclidianView1();
		List<String> code = new ArrayList<>();
		for (GeoGebraExport export : new GeoGebraExport[] {
				new GeoGebraToPgf(app, new ExportGraphicsFactoryD()),
				new GeoGebraToPstricks(app, new ExportGraphicsFactoryD()),
				new GeoGebraToAsymptote(app, new ExportGraphicsFactoryD())}) {
			ExportFrameMinimal frame = new ExportFrameMinimal(view.getYmin(), view.getYmax());
			frame.setKeepColor();
			export.setFrame(frame);
			export.generateAllCode();
			code.add(frame.getCode());
		}
		return code;
	}

	/** C12: everything but the layer number and the hidden-layer presentation. */
	private static List<String> invariants(AppGeoCeDG app, List<String> labels) {
		List<String> facts = new ArrayList<>();
		List<GeoElement> order = new ArrayList<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
		for (String label : labels) {
			GeoElement geo = lookup(app, label);
			StringBuilder fact = new StringBuilder(label).append('|')
					.append(order.indexOf(geo)).append('|')
					.append(geo.getGeoClassType()).append('|')
					.append(geo.getDefinition(StringTemplate.xmlTemplate)).append('|')
					.append(geo.isEuclidianVisible()).append('|')
					.append(app.getKernel().getConstruction().getSpatialIdentityRegistry()
							.getPersistentGeoId(geo));
			if (geo instanceof GeoLocusV2) {
				// The value string carries the session revision counter; the semantic
				// content is the identity, the status, the branches and their components.
				GeoLocusV2 locus = (GeoLocusV2) geo;
				fact.append('|').append(locus.getLocusIdentity()).append('|')
						.append(locus.getSemanticDefinition().getDefinitionStatus());
				for (LocusBranch2D branch : locus.getSemanticDefinition().getBranches()) {
					fact.append('|').append(branch.getBranchKey());
					for (LocusInterval2D component : branch.getValidDomainComponents()) {
						fact.append(':').append(component.getLower()).append(',')
								.append(component.getUpper());
					}
				}
			} else {
				fact.append('|').append(geo.toValueString(StringTemplate.maxPrecision));
			}
			facts.add(fact.toString());
		}
		facts.add(constructionXml(app).replaceAll("\\s*<layer val=\"\\d+\"/>", ""));
		return facts;
	}
}
