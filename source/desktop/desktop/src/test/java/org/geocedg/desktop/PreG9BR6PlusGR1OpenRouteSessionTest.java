/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geocedg.desktop.export.DrawingScale;
import org.geocedg.desktop.export.ExportAreaSession;
import org.geogebra.common.gui.dialog.ToolCreationDialogModel;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * PRE-G9B-R6-plus-G-R1: regression fingerprint of the corrected Open-route session reset
 * (SC-01, SC-02 and SC-11 of the G document-replacement characterization) and of the
 * routes that keep their session behaviour. Embedded hosts: File &gt; Open runs through
 * {@code GuiManagerGeoCeDG.openResolved} after the chooser and URL drops through
 * {@code GuiManagerD.loadURL}; no real chooser, window or drag gesture is exercised.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusGR1OpenRouteSessionTest {
	private static final DrawingScale HALF = DrawingScale.of(1, 2);
	private static final String NOTICE = "G-R1 PASTE NOTICE";

	@TempDir
	Path dir;
	private File docB;
	private File bin;
	private File html;
	private File zip;
	private File docC;
	private String base64;
	private String xmlB;

	interface Route {
		void run(AppGeoCeDG app) throws Exception;
	}

	/**
	 * The replacement document B (mm; Z on layer 0, Y on the hidden layer 5) as a native
	 * file, raw bytes under a non-native name, Base64, an .html page and a stream URL
	 * target; the insertion source C (m).
	 */
	@BeforeEach
	void documents() throws Exception {
		AppGeoCeDG b = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.MM);
		PreG9BR6PlusF3Fixtures.must(b, "Z=(7,7)");
		PreG9BR6PlusF3Fixtures.must(b, "Y=(8,1)");
		b.getKernel().lookupLabel("Y").setLayer(5);
		b.getLayerWorkspace().setLayerHidden(5, true);
		docB = PreG9BR6PlusF3Fixtures.save(b, dir, "docB");
		xmlB = b.getXML();
		byte[] archive = Files.readAllBytes(docB.toPath());
		base64 = Base64.getEncoder().encodeToString(archive);
		bin = dir.resolve("docB.bin").toFile();
		Files.write(bin.toPath(), archive);
		html = page("docB.html", "<param name='ggbBase64' value='" + base64 + "'/>");
		zip = dir.resolve("docB-stream.zip").toFile();
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
			out.putNextEntry(new ZipEntry("docB.cedg"));
			out.write(archive);
			out.closeEntry();
		}
		docC = PreG9BR6PlusF3Documents.single(dir, "docC", "W=(3,3)");
	}

	private File page(String name, String body) throws Exception {
		File file = dir.resolve(name).toFile();
		Files.write(file.toPath(), ("<html><body>" + body + "</body></html>")
				.getBytes(StandardCharsets.UTF_8));
		return file;
	}

	// ------------------------------------------------------------------ routes

	private Map<String, Route> routes() {
		Map<String, Route> routes = new LinkedHashMap<>();
		routes.put("R01 File > Open .html", app -> assertEquals(List.of(html),
				PreG9BR6PlusF3Fixtures.onEdt(() -> gui(app).openResolved(new File[] {html}))));
		routes.put("R02 API openFile native", app -> app.getGgbApi()
				.openFile(docB.toURI().toString()));
		routes.put("R03 URL drop file:", app -> assertTrue(PreG9BR6PlusF3Fixtures.onEdt(
				() -> gui(app).loadURL(docB.toURI().toString(), true))));
		routes.put("R04 URL drop stream", app -> {
			// an uncached jar: connection closes the archive, so the temp directory can go
			boolean cached = URLConnection.getDefaultUseCaches("jar");
			URLConnection.setDefaultUseCaches("jar", false);
			try {
				assertTrue(PreG9BR6PlusF3Fixtures.onEdt(() -> gui(app).loadURL(
						"jar:" + zip.toURI() + "!/docB.cedg", true)));
			} finally {
				URLConnection.setDefaultUseCaches("jar", cached);
			}
		});
		routes.put("R05 API setBase64", app -> app.getGgbApi().setBase64(base64));
		routes.put("R06 API openFile non-native", app -> app.getGgbApi()
				.openFile(bin.toURI().toString()));
		routes.put("R07 loadXML(File) non-native", app -> assertTrue(app.loadXML(bin, false)));
		routes.put("R08 API setXML", app -> app.getGgbApi().setXML(xmlB));
		routes.put("R09 loadFile native", app -> assertTrue(app.loadFile(docB, false)));
		routes.put("R10 loadFile non-native", app -> assertTrue(app.loadFile(bin, false)));
		routes.put("R11 loadXML(String)", app -> assertTrue(app.loadXML(xmlB)));
		routes.put("R13 tool replacement", app -> {
			assertTrue(buildTool(app, "WorkLine"));
			assertTrue(buildTool(app, "WorkLine"));
		});
		routes.put("R14 clearing setXML without the API marker",
				app -> app.setXML(app.getXML(), true));
		routes.put("R15 Insert File", app -> assertEquals(
				GeoCeDGDocumentInsertion.Outcome.INSERTED,
				PreG9BR6PlusF3Fixtures.insert(app, docC).getOutcome()));
		routes.put("R16 Apply Styles", app -> assertTrue(PreG9BR6PlusF3Fixtures.onEdt(
				() -> app.getStyleApplication().apply(docC).isApplied())));
		routes.put("R17 v1 Open URL, save prompt cancelled", app -> {
			app.setUnsaved();
			try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
				dialogs.when(() -> JOptionPane.showOptionDialog(any(), any(), any(),
						anyInt(), anyInt(), any(), any(), any())).thenReturn(2);
				dialogs.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(),
						anyInt(), anyInt(), any())).thenReturn(2);
				assertFalse(app.clearConstruction());
			}
			assertTrue(PreG9BR6PlusF3Fixtures.onEdt(() -> gui(app).loadURL(base64, true)));
		});
		return routes;
	}

	private static GuiManagerGeoCeDG gui(AppGeoCeDG app) {
		return (GuiManagerGeoCeDG) app.getGuiManager();
	}

	private static boolean buildTool(AppGeoCeDG app, String name) {
		ToolCreationDialogModel builder = new ToolCreationDialogModel(app, () -> {
			// no dialog
		});
		builder.addToInput(app.getKernel().lookupLabel("A"));
		builder.addToInput(app.getKernel().lookupLabel("B"));
		builder.addToOutput(app.getKernel().lookupLabel("lw"));
		builder.createTool();
		return builder.finish(app, name, name, "two points", false, null);
	}

	/**
	 * A saved mm window with A, B, lw on layer 0 and K on layer 6, working layer 2, a
	 * MANUAL area with its overlay (or an ISO A border link), a visible paste notice and
	 * the drawing scale 1:2.
	 */
	private static AppGeoCeDG primed(boolean isoLink) throws Exception {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.MM);
		PreG9BR6PlusF3Fixtures.must(app, "A=(1,1)");
		PreG9BR6PlusF3Fixtures.must(app, "B=(4,3)");
		PreG9BR6PlusF3Fixtures.must(app, "lw=Line(A,B)");
		PreG9BR6PlusF3Fixtures.must(app, "K=(2,6)");
		app.getKernel().lookupLabel("K").setLayer(6);
		app.getLayerWorkspace().setWorkingLayer(2);
		int view = app.getEuclidianView1().getViewID();
		ExportAreaSession session = app.getExportAreaSession();
		if (isoLink) {
			PreG9BR6PlusF3Fixtures.must(app, "Tp=(40,40)");
			PreG9BR6PlusF3Fixtures.must(app, "Tsheet=IsoABorder(Tp,4,true,1,1,1.0,true)");
			assertTrue(session.useIsoABorder(view, AlgoIsoABorder.ownerOf(
					app.getKernel().lookupLabel("Tsheet"))));
		} else {
			assertTrue(session.defineManual(view, -2, 5, -1, 4));
		}
		session.setOverlayShown(true);
		app.getStatusBar().showPasteNotice(NOTICE);
		app.setDrawingScale(HALF);
		PreG9BR6PlusF3Fixtures.baseline(app);
		app.setSaved();
		return app;
	}

	private static AtomicInteger sessionChanges(AppGeoCeDG app) {
		AtomicInteger changes = new AtomicInteger();
		app.getExportAreaSession().addListener(changes::incrementAndGet);
		return changes;
	}

	private static void flush() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			// deferred presentation refreshes
		});
	}

	private static boolean has(AppD app, String label) {
		return app.getKernel().lookupLabel(label) != null;
	}

	private void assertManualKept(String route, AppGeoCeDG app, AtomicInteger changes) {
		ExportAreaSession session = app.getExportAreaSession();
		assertSame(ExportAreaSession.Producer.MANUAL, session.getExplicitProducer(), route);
		assertNotNull(session.getManualArea(), route);
		assertTrue(session.isOverlayShown(), route);
		assertEquals(0, changes.get(), route + ": no export-area reset");
	}

	private static void assertDocumentB(String route, AppGeoCeDG app) {
		assertTrue(has(app, "Z") && !has(app, "A"), route + ": document B replaced A");
		assertEquals(DrawingScale.ONE_TO_ONE, app.getDrawingScale(), route + ": DQ-C7");
		assertEquals(0, app.getLayerWorkspace().getWorkingLayer(), route + ": DQ-A2-1");
		assertEquals("[5]", String.valueOf(app.getLayerWorkspace().getHiddenLayers()),
				route + ": the document's hidden set");
	}

	// ------------------------------------------------------------------ SC-01 / SC-02

	@Test
	void approvedOpenRoutesStartANewExportAreaSessionExactlyOnce() throws Exception {
		Map<String, Route> all = routes();
		for (String id : List.of("R01", "R02", "R03", "R04", "R09", "R10", "R11")) {
			String route = key(all, id);
			AppGeoCeDG app = primed(false);
			AtomicInteger changes = sessionChanges(app);
			all.get(route).run(app);
			flush();
			ExportAreaSession session = app.getExportAreaSession();
			assertNull(session.getExplicitProducer(), route);
			assertNull(session.getManualArea(), route);
			assertFalse(session.isOverlayShown(), route);
			assertEquals(1, changes.get(), route + ": exactly one export-area reset");
			assertNull(PreG9BR6PlusF3Fixtures.pasteNotice(app), route + ": notice cleared");
			assertDocumentB(route, app);
		}
	}

	@Test
	void openRoutesReleaseTheIsoALinkWithoutALostLinkNotice() throws Exception {
		Map<String, Route> all = routes();
		for (String id : List.of("R01", "R02", "R03", "R04")) {
			String route = key(all, id);
			AppGeoCeDG app = primed(true);
			all.get(route).run(app);
			flush();
			ExportAreaSession session = app.getExportAreaSession();
			assertNull(session.getLinkedBorder(), route);
			assertFalse(session.isLinkLost(), route + ": reset, not a stale link");
			assertNull(session.getExplicitProducer(), route);
		}
	}

	// ------------------------------------------------------------------ SC-11

	@Test
	void everyOtherDocumentTransitionClearsOnlyThePasteNotice() throws Exception {
		Map<String, Route> all = routes();
		for (String id : List.of("R05", "R06", "R07", "R08", "R17")) {
			String route = key(all, id);
			AppGeoCeDG app = primed(false);
			AtomicInteger changes = sessionChanges(app);
			all.get(route).run(app);
			flush();
			assertNull(PreG9BR6PlusF3Fixtures.pasteNotice(app), route + ": DQ-D1-9");
			// SC-03 to SC-05 and SC-16: no approved export-area contract; unchanged
			assertManualKept(route, app, changes);
			assertDocumentB(route, app);
		}
	}

	@Test
	void apiRoutesBehaveAlikeOnTheEventThread() throws Exception {
		Map<String, Route> all = routes();
		for (String id : List.of("R02", "R05", "R08")) {
			String route = key(all, id);
			AppGeoCeDG app = primed(false);
			AtomicInteger changes = sessionChanges(app);
			PreG9BR6PlusF3Fixtures.onEdt(() -> {
				all.get(route).run(app);
				return null;
			});
			flush();
			assertNull(PreG9BR6PlusF3Fixtures.pasteNotice(app), route);
			assertEquals("R02".equals(id) ? 1 : 0, changes.get(), route);
			assertDocumentB(route, app);
		}
	}

	// ------------------------------------------------------------------ failure

	@Test
	void cancelledAndFailedOpensKeepTheDocumentAndItsSession() throws Exception {
		File noArchive = page("noArchive.html", "<p>no GeoCeDG archive</p>");
		File corrupt = dir.resolve("corrupt.cedg").toFile();
		Files.write(corrupt.toPath(), "not an archive".getBytes(StandardCharsets.UTF_8));
		Map<String, Route> failures = new LinkedHashMap<>();
		failures.put("chooser cancelled", app -> assertTrue(PreG9BR6PlusF3Fixtures.onEdt(
				() -> gui(app).openResolved(null)).isEmpty()));
		failures.put(".html without an archive", app -> PreG9BR6PlusF3Fixtures.onEdt(
				() -> gui(app).openResolved(new File[] {noArchive})));
		failures.put("corrupt native file, File > Open", app -> assertFalse(
				app.loadFile(corrupt, false)));
		failures.put("corrupt native file, API openFile", app -> app.getGgbApi()
				.openFile(corrupt.toURI().toString()));
		for (Map.Entry<String, Route> failure : failures.entrySet()) {
			String route = failure.getKey();
			AppGeoCeDG app = primed(false);
			AtomicInteger changes = sessionChanges(app);
			failure.getValue().run(app);
			flush();
			assertTrue(has(app, "A") && !has(app, "Z"), route + ": document kept");
			assertManualKept(route, app, changes);
			assertEquals(NOTICE, PreG9BR6PlusF3Fixtures.pasteNotice(app), route);
			assertEquals(HALF, app.getDrawingScale(), route);
			assertEquals(2, app.getLayerWorkspace().getWorkingLayer(), route);
		}
	}

	// ------------------------------------------------------------------ preserved

	@Test
	void toolReplacementAndSameDocumentReloadsKeepTheSession() throws Exception {
		Map<String, Route> all = routes();
		for (String id : List.of("R13", "R14")) {
			String route = key(all, id);
			AppGeoCeDG app = primed(false);
			AtomicInteger changes = sessionChanges(app);
			all.get(route).run(app);
			flush();
			assertTrue(has(app, "A"), route + ": same document");
			assertManualKept(route, app, changes);
			assertEquals(NOTICE, PreG9BR6PlusF3Fixtures.pasteNotice(app), route + ": SC-12");
			assertEquals(HALF, app.getDrawingScale(), route);
			// SC-17: the characterized recomputation stays
			// (OBS-R6PLUS-TOOL-REPLACE-DOCUMENT-COMMIT, no correction authorized)
			assertEquals(6, app.getLayerWorkspace().getWorkingLayer(), route);
		}
	}

	@Test
	void macroEditOverwriteKeepsTheOriginalWindowSession() throws Exception {
		AppGeoCeDG original = primed(false);
		assertTrue(buildTool(original, "EditMe"));
		original.getLayerWorkspace().setWorkingLayer(2);
		AtomicInteger changes = sessionChanges(original);
		Macro macro = original.getKernel().getMacro("EditMe");
		AppGeoCeDG editor = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.MM);
		editor.openEditMacro(macro);
		ToolCreationDialogModel model = new ToolCreationDialogModel(editor, () -> {
			// no dialog
		});
		for (GeoElement geo : macro.getMacroInput()) {
			GeoElement here = editor.getKernel().lookupLabel(geo.getLabelSimple());
			model.addToInput(here != null ? here : geo);
		}
		for (GeoElement geo : macro.getMacroOutput()) {
			GeoElement here = editor.getKernel().lookupLabel(geo.getLabelSimple());
			model.addToOutput(here != null ? here : geo);
		}
		model.createTool();
		assertTrue(model.overwriteMacro(macro));
		flush();
		assertManualKept("SC-18", original, changes);
		assertEquals(NOTICE, PreG9BR6PlusF3Fixtures.pasteNotice(original));
		assertEquals(HALF, original.getDrawingScale());
		assertEquals(6, original.getLayerWorkspace().getWorkingLayer(), "SC-18");
	}

	@Test
	void insertFileAndApplyStylesKeepTheirSessionBehaviour() throws Exception {
		Map<String, Route> all = routes();
		for (String id : List.of("R15", "R16")) {
			String route = key(all, id);
			AppGeoCeDG app = primed(false);
			AtomicInteger changes = sessionChanges(app);
			all.get(route).run(app);
			flush();
			assertTrue(has(app, "A"), route);
			assertManualKept(route, app, changes);
			assertEquals(HALF, app.getDrawingScale(), route + ": SC-15");
			assertEquals(2, app.getLayerWorkspace().getWorkingLayer(), route + ": SC-19");
			String notice = PreG9BR6PlusF3Fixtures.pasteNotice(app);
			if ("R15".equals(id)) {
				// SC-13: Insert File shows its own unit paste notice (m into mm)
				assertNotNull(notice, route);
				assertNotEquals(NOTICE, notice, route);
				assertTrue(has(app, "W"), route);
			} else {
				assertEquals(NOTICE, notice, route + ": SC-13");
			}
		}
	}

	// ------------------------------------------------------------------ Classic

	@Test
	void classicKeepsTheInheritedLoadRoutes() throws Exception {
		AppD classic = PreG9BR6PlusF3Fixtures.classic();
		assertSame(AppD.class, classic.getClass());
		assertTrue(classic.loadBase64File(html));
		assertTrue(has(classic, "Z"));
		classic.getGgbApi().newConstruction();
		classic.getGgbApi().openFile(docB.toURI().toString());
		assertTrue(has(classic, "Z"));
	}

	private static String key(Map<String, Route> routes, String id) {
		return routes.keySet().stream().filter(name -> name.startsWith(id + " "))
				.findFirst().orElseThrow();
	}
}
