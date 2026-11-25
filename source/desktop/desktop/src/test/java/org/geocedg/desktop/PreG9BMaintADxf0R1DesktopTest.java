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
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.geocedg.common.importer.dxf.DxfAffineTransform;
import org.geocedg.common.importer.dxf.DxfImportReport;
import org.geocedg.common.importer.dxf.DxfTessellation;
import org.geocedg.desktop.export.ExportArea;
import org.geocedg.desktop.export.ExportPresentationOverlay;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

/**
 * PRE-G9B-MAINT-A-DXF0-R1 (author decision of 2026-11-25): the import action at the end of
 * File &gt; Insert with its EN/ES names; stable maturity without a feature flag; DXF export names
 * without the experimental qualifier; visible references in every picture output of Graphics
 * 1 (PNG, clipboard, print, PDF, SVG, EMF) drawn from the model-unit tessellation as a vector
 * path where the format allows it, independent of zoom, output scale and font size; hidden
 * references and Graphics 2 without references; semantic exports and the construction
 * untouched; compact report, list and inspector with separate details.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BMaintADxf0R1DesktopTest {
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private static final double SCALE = 100;
	private static final String W = "0.7071067811865476";
	private static final double XMIN = -3;
	private static final double XMAX = 3;
	private static final double YMIN = -2.5;
	private static final double YMAX = 2;

	/** Quarter circle Q (rational, open) only, as in the A-DXF0 fixtures. */
	private static String dxf() {
		StringBuilder text = new StringBuilder();
		String[] lines = {"0", "SECTION", "2", "HEADER", "9", "$ACADVER", "1", "AC1015", "9",
			"$INSUNITS", "70", "4", "0", "ENDSEC", "0", "SECTION", "2", "ENTITIES",
			"0", "SPLINE", "5", "Q1", "8", "REF", "70", "12", "71", "2", "72", "6", "73", "3",
			"40", "0.0", "40", "0.0", "40", "0.0", "40", "1.0", "40", "1.0", "40", "1.0",
			"41", "1.0", "41", W, "41", "1.0",
			"10", "1.0", "20", "0.0", "30", "0.0", "10", "1.0", "20", "1.0", "30", "0.0",
			"10", "0.0", "20", "1.0", "30", "0.0",
			"0", "ENDSEC", "0", "EOF"};
		for (String line : lines) {
			text.append(line).append('\n');
		}
		return text.toString();
	}

	private static File dxfFile() throws Exception {
		Path file = Files.createTempFile("a-dxf0-r1-", ".dxf");
		file.toFile().deleteOnExit();
		Files.write(file, dxf().getBytes(StandardCharsets.US_ASCII));
		return file.toFile();
	}

	private static AppGeoCeDG app(double scale) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = app.getEuclidianView1();
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(WIDTH, HEIGHT);
			view.updateSize();
			view.setCoordSystem(WIDTH / 2.0, HEIGHT / 2.0, scale, scale);
			view.setShowAxes(false, false);
			view.showGrid(false);
		});
		// one fixed export area in model units, whatever the zoom
		assertTrue(app.getExportAreaSession().defineManual(view.getViewID(), XMIN, XMAX,
				YMIN, YMAX));
		return app;
	}

	private static List<String> importFile(AppGeoCeDG app, File file) {
		List<String> shown = new ArrayList<>();
		app.getDxfReferenceImport().setPorts(host -> file,
				(host, preview) -> DxfAffineTransform.IDENTITY,
				(host, title, text, details) -> {
					shown.add(text);
					shown.add(details);
				}, host -> { });
		DxfImportReport report = app.getDxfReferenceImport().importFile(file);
		assertNotNull(report);
		return shown;
	}

	private static String constructionXml(AppGeoCeDG app) {
		XMLStringBuilder xml = new XMLStringBuilder();
		app.getKernel().getConstruction().getConstructionXML(xml, true);
		return xml.toString();
	}

	private static boolean isReference(int rgb) {
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		return r - g > 30 && b - g > 30;
	}

	private static int referencePixels(BufferedImage image) {
		int count = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (isReference(image.getRGB(x, y))) {
					count++;
				}
			}
		}
		return count;
	}

	/** Reference-coloured pixels within a few pixels of the output image of a model point. */
	private static boolean drawnNear(BufferedImage image, double x, double y) {
		double px = (x - XMIN) / (XMAX - XMIN) * image.getWidth();
		double py = (YMAX - y) / (YMAX - YMIN) * image.getHeight();
		int radius = 3 + image.getWidth() / 600;
		for (int dy = -radius; dy <= radius; dy++) {
			for (int dx = -radius; dx <= radius; dx++) {
				int ix = (int) Math.round(px) + dx;
				int iy = (int) Math.round(py) + dy;
				if (ix >= 0 && iy >= 0 && ix < image.getWidth() && iy < image.getHeight()
						&& isReference(image.getRGB(ix, iy))) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean same(BufferedImage a, BufferedImage b) {
		if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
			return false;
		}
		for (int y = 0; y < a.getHeight(); y++) {
			for (int x = 0; x < a.getWidth(); x++) {
				if (a.getRGB(x, y) != b.getRGB(x, y)) {
					return false;
				}
			}
		}
		return true;
	}

	/** The SVG group the picture service writes for the presentation overlay. */
	private static String presentationGroup(String svg) {
		int start = svg.indexOf("<g id=\"presentation\">");
		if (start < 0) {
			// written only when the overlay draws something
			return "";
		}
		int end = svg.indexOf("</g><!-- presentation -->", start);
		if (end < 0) {
			end = svg.length();
		}
		// the export clip rectangle is a clip definition, not drawn content
		return svg.substring(start, end).replaceAll("(?s)<clipPath.*?</clipPath>", "");
	}

	/** Points (x, y) of all {@code d="..."} path data in a fragment, in order. */
	private static List<double[]> pathPoints(String fragment) {
		List<double[]> points = new ArrayList<>();
		Matcher path = Pattern.compile("d=\"([^\"]*)\"").matcher(fragment);
		Pattern pair = Pattern.compile("[ML] (-?[0-9.Ee+-]+) (-?[0-9.Ee+-]+)");
		while (path.find()) {
			Matcher m = pair.matcher(path.group(1));
			while (m.find()) {
				points.add(new double[] {Double.parseDouble(m.group(1)),
						Double.parseDouble(m.group(2))});
			}
		}
		return points;
	}

	@Test
	void r11ActionIsTheLastEntryOfFileInsertWithItsNames() throws Exception {
		AppGeoCeDG app = app(SCALE);
		GeoCeDGActionRegistry registry = ((GuiManagerGeoCeDG) app.getGuiManager())
				.getActionRegistry();
		assertEquals("Import DXF Reference…",
				registry.get("import.dxf-reference").getValue(Action.NAME));
		assertEquals("Importar referencia DXF…", GeoCeDGProfile.getText(
				"geocedg.action.ImportDxfReference.name", "es"));
		app.getGuiManager().initMenubar();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		JMenu file = bar.getMenu(0);
		JMenu insert = null;
		List<String> direct = new ArrayList<>();
		for (Component component : file.getMenuComponents()) {
			if (component instanceof JMenu menu && "file-insert".equals(
					menu.getClientProperty("geocedg.presentation.group.id"))) {
				insert = menu;
			} else if (component instanceof JMenuItem item) {
				Object id = item.getClientProperty(GeoCeDGActionRegistry.ACTION_ID);
				if (id != null) {
					direct.add(id.toString());
				}
			}
		}
		assertNotNull(insert, "File > Insert");
		assertFalse(direct.contains("import.dxf-reference"), "not a direct File entry");
		List<String> inserted = new ArrayList<>();
		for (Component component : insert.getMenuComponents()) {
			if (component instanceof JMenuItem item) {
				Object id = item.getClientProperty(GeoCeDGActionRegistry.ACTION_ID);
				if (id != null) {
					inserted.add(id.toString());
				}
			}
		}
		assertEquals(List.of("document.insert-file", "document.insert-expert",
				"document.insert-clipboard-image", "document.apply-styles",
				"import.dxf-reference"), inserted);
		// the action still imports through the registry
		File dxf = dxfFile();
		app.getDxfReferenceImport().setPorts(host -> dxf,
				(host, preview) -> DxfAffineTransform.IDENTITY,
				(host, title, text, details) -> { }, host -> { });
		registry.invoke("import.dxf-reference", new java.awt.event.ActionEvent(this, 1, "t"));
		assertEquals(1, app.getDxfReferenceSession().size());
	}

	@Test
	void r12MaturityAndVisibleNamesCarryNoExperimentalQualifier() throws Exception {
		JSONObject catalog = GeoCeDGProfile.getCatalog();
		JSONObject action = null;
		JSONObject export = null;
		for (int i = 0; i < catalog.getJSONArray("actions").length(); i++) {
			JSONObject candidate = catalog.getJSONArray("actions").getJSONObject(i);
			if ("import.dxf-reference".equals(candidate.getString("id"))) {
				action = candidate;
			}
			if ("export.dxf-2d".equals(candidate.getString("id"))) {
				export = candidate;
			}
		}
		assertNotNull(action);
		assertEquals("stable", action.getString("maturity"));
		assertEquals(0, action.getJSONArray("feature_requirements").length());
		assertTrue(action.getString("notes").contains("bounded to the implemented contract"));
		// the export keeps its formal state, algorithms and guarantees; only names change
		assertNotNull(export);
		assertEquals("experimental", export.getString("maturity"));
		for (String suffix : new String[] {"name", "short_help", "long_help", "status",
			"error"}) {
			for (String language : new String[] {"en", "es"}) {
				String text = GeoCeDGProfile.getText("geocedg.action.ExportDxf." + suffix,
						language);
				assertFalse(text.toLowerCase(Locale.ROOT).contains("experimental"),
						suffix + " " + language + ": " + text);
			}
		}
		assertEquals("Export 2D geometry as DXF…",
				GeoCeDGProfile.getText("geocedg.action.ExportDxf.name", "en"));
		assertEquals("Exportar geometría 2D como DXF…",
				GeoCeDGProfile.getText("geocedg.action.ExportDxf.name", "es"));
		assertEquals("Export 2D geometry as DXF...", GeoCeDGMenuBar.DXF_ACTION_TEXT);
	}

	@Test
	void r13EveryPictureRouteOfGraphicsOnePresentsVisibleReferences() throws Exception {
		AppGeoCeDG app = app(SCALE);
		EuclidianView view = app.getEuclidianView1();
		PreG9BMaintADxf0DesktopTestSupport.geo(app, "s=Segment((-2,-2),(2,-2))");
		UndoManagerD undo = PreG9BR6PlusA1LayerWorkspaceTest.undo(app);
		BufferedImage pngBefore = PreG9BR6PlusBPictureFidelityTest.png(app, view, 1);
		BufferedImage printBefore = PreG9BR6PlusBPictureFidelityTest.printed(app, view);
		final String pdfBefore = PreG9BR6PlusBPictureFidelityTest.normalizePdf(
				PreG9BR6PlusBPictureFidelityTest.pdf(app, view));
		final byte[] emfBefore = PreG9BR6PlusBPictureFidelityTest.emf(app, view, 1);
		String svgBefore = PreG9BR6PlusBPictureFidelityTest.svg(app, view, 0.0264583);
		assertTrue(pathPoints(presentationGroup(svgBefore)).isEmpty(), "no reference yet");
		importFile(app, dxfFile());
		final String xml = constructionXml(app);
		final int history = undo.getHistorySize();
		DxfTessellation render = app.getDxfReferenceSession().entries().get(0).reference()
				.tessellation();
		int middle = render.size() / 2;

		BufferedImage png = PreG9BR6PlusBPictureFidelityTest.png(app, view, 1);
		assertEquals(0, referencePixels(pngBefore));
		assertTrue(referencePixels(png) > 50, "PNG");
		assertTrue(drawnNear(png, render.x(middle), render.y(middle)), "PNG position");
		BufferedImage printed = PreG9BR6PlusBPictureFidelityTest.printed(app, view);
		assertEquals(0, referencePixels(printBefore));
		assertTrue(referencePixels(printed) > 50, "print");
		assertFalse(PreG9BR6PlusBPictureFidelityTest.normalizePdf(
				PreG9BR6PlusBPictureFidelityTest.pdf(app, view)).equals(pdfBefore), "PDF");
		assertFalse(java.util.Arrays.equals(emfBefore,
				PreG9BR6PlusBPictureFidelityTest.emf(app, view, 1)), "EMF");
		String pdf = PreG9BR6PlusBPictureFidelityTest.pdf(app, view);
		assertFalse(pdf.contains("/Subtype /Image"), "PDF stays vector");
		// clipboard: the same service at the host clipboard scale
		List<BufferedImage> clipboard = new ArrayList<>();
		app.getPictureExportService().setClipboardSink(clipboard::add);
		app.copyGraphicsViewToClipboard(view);
		assertEquals(1, clipboard.size(), "clipboard");
		assertTrue(referencePixels(clipboard.get(0)) > 50, "clipboard");
		// SVG: one vector path whose points are the model tessellation
		String svg = PreG9BR6PlusBPictureFidelityTest.svg(app, view, 0.0264583);
		String group = presentationGroup(svg);
		assertFalse(group.contains("<image"), "no raster in the presentation group");
		List<double[]> points = pathPoints(group);
		assertEquals(render.size(), points.size(), "one SVG point per tessellation point");
		double[] box = PreG9BR6PlusBPictureFidelityTest.svgViewBox(svg);
		for (int i = 0; i < render.size(); i++) {
			double u = (points.get(i)[0] - box[0]) / box[2];
			double v = (points.get(i)[1] - box[1]) / box[3];
			assertEquals((render.x(i) - XMIN) / (XMAX - XMIN), u, 1e-4, "x " + i);
			assertEquals((YMAX - render.y(i)) / (YMAX - YMIN), v, 1e-4, "y " + i);
		}
		// no construction mutation: XML and Undo history unchanged by all picture routes
		assertEquals(xml, constructionXml(app));
		assertEquals(history, undo.getHistorySize());
		assertEquals(1, app.getDxfReferenceSession().size());
		// hidden references are not drawn
		app.getDxfReferenceSession().setVisible(0, false);
		assertTrue(same(pngBefore, PreG9BR6PlusBPictureFidelityTest.png(app, view, 1)),
				"hidden reference");
		assertTrue(pathPoints(presentationGroup(PreG9BR6PlusBPictureFidelityTest.svg(app,
				view, 0.0264583))).isEmpty(), "hidden reference in SVG");
	}

	@Test
	void r13PictureOutputsAreIndependentOfZoomOutputScaleAndFontSize() throws Exception {
		List<List<double[]>> normalized = new ArrayList<>();
		for (double scale : new double[] {SCALE, 3 * SCALE}) {
			for (int font : new int[] {12, 24}) {
				AppGeoCeDG app = app(scale);
				SwingUtilities.invokeAndWait(() -> app.setFontSize(font, true));
				importFile(app, dxfFile());
				EuclidianView view = app.getEuclidianView1();
				String svg = PreG9BR6PlusBPictureFidelityTest.svg(app, view, 0.0264583);
				double[] box = PreG9BR6PlusBPictureFidelityTest.svgViewBox(svg);
				List<double[]> points = new ArrayList<>();
				for (double[] p : pathPoints(presentationGroup(svg))) {
					points.add(new double[] {(p[0] - box[0]) / box[2], (p[1] - box[1]) / box[3]});
				}
				normalized.add(points);
				DxfTessellation render = app.getDxfReferenceSession().entries().get(0)
						.reference().tessellation();
				// raster at output scales 1 and 2 (a DPI change): same model positions
				for (double outputScale : new double[] {1, 2}) {
					BufferedImage png = PreG9BR6PlusBPictureFidelityTest.png(app, view,
							outputScale);
					for (int i = 0; i < render.size(); i += Math.max(1, render.size() / 8)) {
						assertTrue(drawnNear(png, render.x(i), render.y(i)), "zoom " + scale
								+ " font " + font + " output scale " + outputScale + " i " + i);
					}
				}
			}
		}
		for (List<double[]> points : normalized) {
			assertEquals(normalized.get(0).size(), points.size());
			for (int i = 0; i < points.size(); i++) {
				assertEquals(normalized.get(0).get(i)[0], points.get(i)[0], 1e-4);
				assertEquals(normalized.get(0).get(i)[1], points.get(i)[1], 1e-4);
			}
		}
	}

	@Test
	void r13GraphicsTwoAndSemanticExportsStayWithoutReferences() throws Exception {
		AppGeoCeDG app = app(SCALE);
		importFile(app, dxfFile());
		ExportPresentationOverlay overlay = app.getPictureExportService()
				.getPresentationOverlay();
		assertNotNull(overlay);
		EuclidianView second = Mockito.mock(EuclidianView.class);
		Mockito.when(second.getViewID()).thenReturn(App.VIEW_EUCLIDIAN2);
		GGraphics2D graphics = Mockito.mock(GGraphics2D.class);
		assertFalse(overlay.hasContent(second));
		assertTrue(overlay.hasContent(app.getEuclidianView1()));
		overlay.paint(second, null, graphics);
		Mockito.verifyNoInteractions(graphics);
		// semantic DXF and LaTeX exports never include references
		EuclidianView view = app.getEuclidianView1();
		org.geocedg.common.export.GeometryExportService service =
				new org.geocedg.common.export.GeometryExportService();
		String dxf = service.exportDxf(service.createModel(new ArrayList<>(app.getKernel()
				.getConstruction().getGeoSetConstructionOrder()),
				org.geocedg.common.export.GeometryExportModel.SelectionMode
						.COMPLETE_CONSTRUCTION));
		assertFalse(dxf.contains("SPLINE"), "semantic DXF");
		for (String dialect : new String[] {"pgf", "pstricks", "asymptote"}) {
			String latex = PreG9BR6PlusCLatexExportTest.generate(
					PreG9BR6PlusCLatexExportTest.exporter(app, dialect), view);
			assertFalse(latex.contains("150,60,170") || latex.contains("0.59,0.24,0.67"),
					dialect);
		}
		ExportArea area = app.getExportAreaSession().resolve(view);
		assertNotNull(area);
	}

	@Test
	void r14ReportListAndInspectorAreCompactWithSeparateDetails() throws Exception {
		AppGeoCeDG app = app(SCALE);
		List<String> shown = importFile(app, dxfFile());
		String summary = shown.get(0);
		String details = shown.get(1);
		assertNotNull(details, "details exist");
		assertTrue(summary.startsWith("File: "), summary);
		assertTrue(summary.contains("Admitted references: 1"), summary);
		assertTrue(summary.contains("REF · Q1 — Rational spline"), summary);
		for (String technical : new String[] {"SHA-256", "AC1015", "p=", "n+1=",
			"Transformation", "Entities by type"}) {
			assertFalse(summary.contains(technical), technical + " in " + summary);
			assertTrue(details.contains(technical), technical + " in details");
		}
		GeoCeDGDxfReferenceSession.Entry entry = app.getDxfReferenceSession().entries().get(0);
		String line = GeoCeDGDxfReferenceText.listLine(app, entry);
		assertTrue(line.startsWith("REF · Q1 ("), line);
		assertTrue(line.endsWith("Rational spline"), line);
		assertFalse(line.contains("#"), line);
		String inspector = GeoCeDGDxfReferenceText.inspector(app, entry);
		String[] parts = inspector.split("\nDetails\n", 2);
		assertEquals(2, parts.length, inspector);
		assertTrue(parts[0].contains("Reference: REF · Q1"), parts[0]);
		assertTrue(parts[0].contains("Nature: Rational spline"), parts[0]);
		assertTrue(parts[0].contains("Visible in this session: yes"), parts[0]);
		assertFalse(parts[0].contains("SHA-256"), parts[0]);
		assertTrue(parts[1].contains("dxf-spline-reference/v1, RATIONAL"), parts[1]);
		assertTrue(parts[1].contains("SHA-256"), parts[1]);
		app.setLanguage(Locale.forLanguageTag("es"));
		try {
			assertTrue(GeoCeDGDxfReferenceText.listLine(app, entry).endsWith("Spline racional"));
			assertTrue(GeoCeDGDxfReferenceText.inspector(app, entry).contains("\nDetalles\n"));
		} finally {
			app.setLanguage(Locale.ENGLISH);
		}
		// failures keep their explanation in the ordinary text
		List<String> failure = new ArrayList<>();
		File empty = Files.createTempFile("a-dxf0-r1-empty-", ".dxf").toFile();
		empty.deleteOnExit();
		Files.write(empty.toPath(), "0\nEOF\n".getBytes(StandardCharsets.US_ASCII));
		app.getDxfReferenceImport().setPorts(host -> empty,
				(host, preview) -> DxfAffineTransform.IDENTITY,
				(host, title, text, more) -> failure.add(text), host -> { });
		app.getDxfReferenceImport().importFile(empty);
		assertEquals(1, failure.size());
		assertTrue(failure.get(0).contains("Admitted references: 0"), failure.get(0));
	}

	/** Shared construction helper (the A-DXF0 test keeps its own private copy). */
	static final class PreG9BMaintADxf0DesktopTestSupport {
		private PreG9BMaintADxf0DesktopTestSupport() {
		}

		static void geo(AppGeoCeDG app, String command) {
			try (org.mockito.MockedStatic<javax.swing.JOptionPane> pane =
					Mockito.mockStatic(javax.swing.JOptionPane.class)) {
				GeoElementND[] result = app.getKernel().getAlgebraProcessor()
						.processAlgebraCommand(command, false);
				pane.verifyNoInteractions();
				assertNotNull(result, command);
			}
		}
	}
}
