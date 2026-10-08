/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.desktop.export.DrawingScale;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.export.GraphicExportDialog;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * POST-E2-P1-R1 characterization (characterization and design only, no product change)
 * of the physical stroke width in PDF exports. The produced PDF is interpreted: graphics
 * state stack ({@code q}/{@code Q}), concatenated transformation matrices ({@code cm}),
 * line width ({@code w}), cap ({@code J}), join ({@code j}), dash ({@code d}), path
 * construction, strokes and fills, and text size ({@code Tf} through {@code Tm}). Each
 * object carries its own colour, so every stroke and fill is attributed to its object.
 * The effective width is the user-space width times the uniform scale of the current
 * transformation; widths are never inferred from path extents.
 *
 * <p>With {@code GEOCEDG_POST_E2_P1_R1_REPORT=<file>} the measured rows are also written
 * as JSON, and with {@code GEOCEDG_POST_E2_P1_R1_PDF_DIR=<dir>} the exported PDFs are
 * kept for an independent rasterization cross-check; assertions never depend on them.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostE2P1R1PdfStrokeWidthCharacterizationTest {
	private static final double MM_PER_PT = 25.4 / 72;
	private static final int[] THICKNESSES = {1, 2, 3, 5};
	/** Zoom levels, pixels per model unit. */
	private static final double[] ZOOMS = {2, 8, 32};
	private static final GColor SEGMENT = GColor.newColor(255, 0, 0);
	private static final GColor LINE = GColor.newColor(0, 255, 0);
	private static final GColor CIRCLE = GColor.newColor(0, 0, 255);
	private static final GColor POINT = GColor.newColor(255, 255, 0);
	private static final GColor DIMENSION_LINE = GColor.newColor(255, 0, 255);
	private static final GColor EXTENSION_A = GColor.newColor(0, 255, 255);
	private static final GColor EXTENSION_B = GColor.newColor(255, 128, 0);
	private static final GColor TEXT = GColor.newColor(128, 0, 255);
	private static final GColor PLAIN_TEXT = GColor.newColor(0, 128, 255);
	private static final Map<String, List<Map<String, Object>>> REPORT =
			new LinkedHashMap<>();

	@TempDir
	Path temporary;

	// ------------------------------------------- primary: explicit area, mm 1:1
	// POST-E2-P1-R2 reconciliation: P1-R1 characterized the physical route before
	// POST-E2-P1-R2 (report and measurements kept as historical evidence); the route now
	// renders one style pixel as 0.8 pt (physical-pdf-style-sizes)

	@Test
	void physicalPdfStrokesFollowTheR2MappingAtEveryZoom() throws Exception {
		for (int thickness : THICKNESSES) {
			AppGeoCeDG app = scene(UnitToken.MM, DrawingScale.ONE_TO_ONE, 1, thickness);
			double p = app.getPhysicalExportScale();
			double[] segmentWidths = new double[ZOOMS.length];
			for (int z = 0; z < ZOOMS.length; z++) {
				zoom(app, ZOOMS[z]);
				Pdf pdf = interpret(pdf(app, "explicit-mm-t" + thickness + "-z" + ZOOMS[z]));
				Map<String, Object> row = base("explicit", "mm", "1:1", thickness, ZOOMS[z], app,
						pdf);
				double expected = 0.4 * thickness * MM_PER_PT;
				row.put("expectedWidthMm", expected);
				for (Object[] tag : tags()) {
					Stroke stroke = pdf.stroke((GColor) tag[1]);
					assertNotNull(stroke, tag[0] + " stroked");
					row.put(tag[0] + "UserWidth", stroke.userWidth);
					row.put(tag[0] + "WidthMm", stroke.effectivePt * MM_PER_PT);
					row.put(tag[0] + "Cap", stroke.cap);
					row.put(tag[0] + "Join", stroke.join);
					assertEquals(expected, stroke.effectivePt * MM_PER_PT, expected * 1E-4,
							tag[0] + " t=" + thickness + " zoom " + ZOOMS[z]);
				}
				// the decorated dimension line is a filled outline: its body height
				Box body = pdf.widestFill(DIMENSION_LINE);
				assertNotNull(body, "dimension line body");
				row.put("dimensionLineFilledBodyMm", body.height() * MM_PER_PT);
				assertEquals(expected, body.height() * MM_PER_PT, Math.max(expected * 2E-3, 0.006),
						"dimension line body t=" + thickness + " zoom " + ZOOMS[z]);
				// the centreline geometry and the page never depend on the zoom
				Stroke segment = pdf.stroke(SEGMENT);
				row.put("segmentLengthMm", segment.length() * MM_PER_PT);
				assertEquals(40, segment.length() * MM_PER_PT, 0.01);
				row.put("pageWidthMm", pdf.width() * MM_PER_PT);
				assertEquals(60, pdf.width() * MM_PER_PT, 0.01);
				Box arrows = pdf.fillBox(DIMENSION_LINE);
				row.put("arrowheadsBoxHeightMm", arrows == null ? null
						: arrows.height() * MM_PER_PT);
				Box point = pdf.fillBox(POINT);
				row.put("pointMarkerDiameterMm", point == null ? null : point.width() * MM_PER_PT);
				Box text = pdf.fillBox(TEXT);
				row.put("textGlyphHeightMm", text == null ? null : text.height() * MM_PER_PT);
				record("explicitArea", row);
				segmentWidths[z] = segment.effectivePt * MM_PER_PT;
			}
			assertEquals(1,
					segmentWidths[0] / segmentWidths[ZOOMS.length - 1], 1E-3,
					"width inversely proportional to the zoom at export");
		}
	}

	@Test
	void arrowheadsAndPointMarkersAreZoomIndependentAndTextIsUnchanged() throws Exception {
		AppGeoCeDG app = scene(UnitToken.MM, DrawingScale.ONE_TO_ONE, 1, 2);
		double[] arrow = new double[ZOOMS.length];
		double[] marker = new double[ZOOMS.length];
		double[] text = new double[ZOOMS.length];
		double[] plain = new double[ZOOMS.length];
		for (int z = 0; z < ZOOMS.length; z++) {
			zoom(app, ZOOMS[z]);
			Pdf pdf = interpret(pdf(app, "decorations-z" + ZOOMS[z]));
			arrow[z] = pdf.fillBox(DIMENSION_LINE).height();
			marker[z] = pdf.fillBox(POINT).width();
			text[z] = pdf.fillBox(TEXT).height();
			plain[z] = pdf.fillBox(PLAIN_TEXT).height();
		}
		for (int z = 1; z < ZOOMS.length; z++) {
			final double ratio = ZOOMS[0] / ZOOMS[z];
			// filled sizes: printed-coordinate resolution, below 2 % here
			assertEquals(1, arrow[z] / arrow[0], 2E-2, "arrowhead size");
			assertEquals(1, marker[z] / marker[0], 2E-2, "point marker size");
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("xscale", ZOOMS[z]);
			row.put("arrowRatio", arrow[z] / arrow[0]);
			row.put("markerRatio", marker[z] / marker[0]);
			row.put("dimensionTextHeightMm", text[z] * MM_PER_PT);
			row.put("dimensionTextRatio", text[z] / text[0]);
			row.put("plainTextHeightMm", plain[z] * MM_PER_PT);
			row.put("plainTextRatio", plain[z] / plain[0]);
			row.put("zoomRatio", ratio);
			record("decorationRatios", row);
			// text follows another rule (not 1/zoom; separate observation): the dimension
			// value is sized exactly like an ordinary text
			assertEquals(plain[z], text[z], plain[z] * 1E-6, "dimension text = plain text");
		}
	}

	// ------------------------------------------------ units and drawing scale

	@Test
	void unitsAndDrawingScaleNoLongerChangeThePhysicalPdfWidth() throws Exception {
		UnitToken[] units = {UnitToken.MM, UnitToken.CM, UnitToken.M};
		for (DrawingScale scale : new DrawingScale[] {DrawingScale.ONE_TO_ONE,
				DrawingScale.of(1, 10)}) {
			double ratio = (double) scale.getNumerator() / scale.getDenominator();
			double[] normalized = new double[units.length];
			for (int u = 0; u < units.length; u++) {
				double mmPerUnit = factor(units[u]) * 1000;
				AppGeoCeDG app = scene(units[u], scale, 1 / mmPerUnit, 2);
				for (String mode : new String[] {"normalized", "raw"}) {
					// normalized: 8 px per physical model millimetre; raw: 8 px per unit
					double zoom = "normalized".equals(mode) ? 8 * mmPerUnit : 8;
					zoom(app, zoom);
					Pdf pdf = interpret(pdf(app, "units-" + units[u] + "-" + mode + "-"
							+ scale.getDenominator()));
					Stroke segment = pdf.stroke(SEGMENT);
					Map<String, Object> row = base(mode, units[u].token(), scale.toString(), 2,
							zoom, app, pdf);
					double expected = 0.4 * 2 * MM_PER_PT;
					row.put("expectedWidthMm", expected);
					row.put("segmentWidthMm", segment.effectivePt * MM_PER_PT);
					row.put("segmentLengthMm", segment.length() * MM_PER_PT);
					record("unitsAndScale", row);
					assertEquals(expected, segment.effectivePt * MM_PER_PT, expected * 1E-4);
					assertEquals(40 * ratio, segment.length() * MM_PER_PT, 0.01);
					if ("normalized".equals(mode)) {
						normalized[u] = segment.effectivePt * MM_PER_PT;
					}
				}
			}
			for (int u = 1; u < units.length; u++) {
				assertEquals(normalized[0], normalized[u], normalized[0] * 1E-4,
						"the same pixels per physical millimetre give the same width");
			}
			assertEquals(0.4 * 2 * MM_PER_PT, normalized[0], 0.4 * 2 * MM_PER_PT * 1E-4,
					"t=2: 1 px at 8 px per model mm, times the drawing scale");
		}
	}

	// --------------------------------------------------------- unspecified unit

	@Test
	void anUnspecifiedDocumentUsesTheLegacyPrintingScale() throws Exception {
		AppGeoCeDG app = scene(null, DrawingScale.ONE_TO_ONE, 1, 2);
		for (double zoom : new double[] {2, 8, 32, 37.795275590551185}) {
			zoom(app, zoom);
			EuclidianView view = app.getEuclidianView1();
			Pdf pdf = interpret(pdf(app, "unspecified-z" + zoom));
			Stroke segment = pdf.stroke(SEGMENT);
			double printing = org.geocedg.desktop.export.ExportViewport.printingScaleOf(view);
			Map<String, Object> row = base("explicit", "unspecified", "n/a", 2, zoom, app, pdf);
			row.put("printingScaleCmPerUnit", printing);
			row.put("segmentWidthMm", segment.effectivePt * MM_PER_PT);
			row.put("segmentLengthMm", segment.length() * MM_PER_PT);
			record("unspecified", row);
			assertEquals(1.0 * printing * 10 / zoom, segment.effectivePt * MM_PER_PT,
					segment.effectivePt * MM_PER_PT * 1E-4);
		}
	}

	// ----------------------------------------------- viewport-derived export area

	@Test
	void theViewportFallbackChangesThePageButNotThePhysicalWidth()
			throws Exception {
		AppGeoCeDG app = scene(UnitToken.MM, DrawingScale.ONE_TO_ONE, 1, 2);
		app.getExportAreaSession().clear();
		for (double zoom : ZOOMS) {
			zoom(app, zoom);
			Pdf pdf = interpret(pdf(app, "viewport-z" + zoom));
			Stroke segment = pdf.stroke(SEGMENT);
			Map<String, Object> row = base("viewport", "mm", "1:1", 2, zoom, app, pdf);
			row.put("segmentWidthMm", segment == null ? null
					: segment.effectivePt * MM_PER_PT);
			record("viewportFallback", row);
			// 800 px of view are 800 / zoom model mm: the page follows the zoom
			assertEquals(800 / zoom * app.getPhysicalExportScale() * 10,
					pdf.width() * MM_PER_PT, 0.01);
			if (segment != null) {
				assertEquals(0.4 * 2 * MM_PER_PT,
						segment.effectivePt * MM_PER_PT, 0.4 * 2 * MM_PER_PT * 1E-4);
			}
		}
	}

	// --------------------------------------------- upstream Classic comparison

	@Test
	void classicGeoGebraFollowsTheSameRuleWithAFixedPrintingScale() throws Exception {
		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true));
		classicEval(classic, "s=Segment((0,0),(40,0))");
		classicEval(classic, "Export_1=(-10,-10)");
		classicEval(classic, "Export_2=(50,20)");
		GeoElement segment = classic.getKernel().lookupLabel("s");
		segment.setObjColor(SEGMENT);
		segment.setLineThickness(2);
		segment.setLabelVisible(false);
		segment.updateRepaint();
		for (String label : new String[] {"Export_1", "Export_2"}) {
			GeoElement point = classic.getKernel().lookupLabel(label);
			point.setEuclidianVisible(false);
			point.updateRepaint();
		}
		EuclidianView view = classic.getEuclidianView1();
		for (double zoom : ZOOMS) {
			SwingUtilities.invokeAndWait(() -> {
				((EuclidianViewD) view).getJPanel().setSize(800, 600);
				view.updateSize();
				view.setCoordSystem(100, 500, zoom, zoom);
				view.setShowAxes(false, false);
				view.showGrid(false);
			});
			// the user's fixed "1 unit = 0.1 cm" (a 1:1 millimetre drawing)
			view.setPrintingScale(0.1);
			double exportScale = view.getPrintingScale() * 72 / 2.54 / view.getXscale();
			int pixelWidth = (int) Math.floor(view.getExportWidth() * exportScale);
			int pixelHeight = (int) Math.floor(view.getExportHeight() * exportScale);
			File file = output("classic-z" + zoom);
			GraphicExportDialog.exportPDF((EuclidianViewD) view, file, true, pixelWidth,
					pixelHeight, exportScale);
			Pdf pdf = interpret(new String(Files.readAllBytes(file.toPath()),
					StandardCharsets.ISO_8859_1));
			Stroke stroke = pdf.stroke(SEGMENT);
			assertNotNull(stroke, "Classic stroke");
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("route", "Classic GraphicExportDialog.exportPDF (upstream body)");
			row.put("printingScaleCmPerUnit", 0.1);
			row.put("xscale", view.getXscale());
			row.put("pageWidthMm", pdf.width() * MM_PER_PT);
			row.put("segmentUserWidth", stroke.userWidth);
			row.put("segmentWidthMm", stroke.effectivePt * MM_PER_PT);
			row.put("segmentLengthMm", stroke.length() * MM_PER_PT);
			row.put("unfittedWidthMm", 0.1 * 10 / zoom);
			row.put("fitFactor", stroke.length() * MM_PER_PT / 40);
			row.put("widthToLength", stroke.effectivePt / stroke.length());
			record("classic", row);
			// upstream fits the content into its default page margins, so absolute sizes
			// carry a fit factor; the stroke-to-geometry proportion is the screen one
			assertEquals(1.0 / (40 * zoom), stroke.effectivePt / stroke.length(), 1E-6,
					"upstream: (t/2) px against the geometry at the export zoom");
			assertEquals(0.1 * 10 / zoom * (stroke.length() * MM_PER_PT / 40),
					stroke.effectivePt * MM_PER_PT, 1E-6);
		}
	}

	// ----------------------------------------------------------- PGF control

	@Test
	void pgfWritesAFixedPointWidthPerThicknessWhateverTheZoom() throws Exception {
		for (int thickness : THICKNESSES) {
			AppGeoCeDG app = scene(UnitToken.MM, DrawingScale.ONE_TO_ONE, 1, thickness);
			String first = null;
			for (double zoom : ZOOMS) {
				zoom(app, zoom);
				String pgf = PreG9BR6PlusCLatexExportTest.generate(
						PreG9BR6PlusCLatexExportTest.exporter(app, "pgf"),
						app.getEuclidianView1());
				Matcher any = Pattern.compile("line width=([0-9.]+)pt").matcher(pgf);
				java.util.TreeSet<Double> widths = new java.util.TreeSet<>();
				while (any.find()) {
					widths.add(Double.parseDouble(any.group(1)));
				}
				assertTrue(!widths.isEmpty(), pgf);
				String value = widths.toString();
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("thickness", thickness);
				row.put("xscale", zoom);
				row.put("pgfLineWidthsPt", value);
				row.put("pgfMaxLineWidthMm", widths.last() * MM_PER_PT);
				record("pgf", row);
				if (first == null) {
					first = value;
				}
				assertEquals(first, value, "PGF width does not depend on the zoom");
			}
		}
	}

	@AfterAll
	static void writeReport() throws Exception {
		String target = System.getenv("GEOCEDG_POST_E2_P1_R1_REPORT");
		if (target == null || target.isEmpty()) {
			return;
		}
		StringBuilder json = new StringBuilder("{\n");
		boolean firstFamily = true;
		for (Map.Entry<String, List<Map<String, Object>>> family : REPORT.entrySet()) {
			json.append(firstFamily ? "" : ",\n").append("  \"").append(family.getKey())
					.append("\": [\n");
			firstFamily = false;
			List<Map<String, Object>> rows = family.getValue();
			for (int i = 0; i < rows.size(); i++) {
				json.append("    {");
				boolean firstField = true;
				for (Map.Entry<String, Object> field : rows.get(i).entrySet()) {
					Object value = field.getValue();
					json.append(firstField ? "" : ", ").append('"').append(field.getKey())
							.append("\": ").append(value instanceof String
									? "\"" + value + "\"" : String.valueOf(value));
					firstField = false;
				}
				json.append(i + 1 < rows.size() ? "},\n" : "}\n");
			}
			json.append("  ]");
		}
		json.append("\n}\n");
		Files.writeString(Paths.get(target), json.toString(), StandardCharsets.UTF_8);
	}

	// ------------------------------------------------------------------ scenes

	private static Object[][] tags() {
		return new Object[][] {{"segment", SEGMENT}, {"line", LINE}, {"circle", CIRCLE},
			{"extensionA", EXTENSION_A}, {"extensionB", EXTENSION_B}};
	}

	/**
	 * One explicit-area scene of a 40 mm segment (in model units {@code 40 * s}), a line,
	 * a circle, a point and an aligned dimension with the given thickness.
	 */
	private AppGeoCeDG scene(UnitToken unit, DrawingScale scale, double s, int thickness)
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		zoom(app, 8 / s);
		app.getDocumentUnits().replace(unit == null ? UnitState.EMPTY
				: UnitState.of(unit, null, null));
		app.setDrawingScale(scale);
		style(app, G9U1TestApp.eval(app, "s=Segment((0,0),(" + 40 * s + ",0))"), SEGMENT,
				thickness);
		style(app, G9U1TestApp.eval(app, "g=Line((0," + 15 * s + "),(" + 40 * s + ","
				+ 18 * s + "))"), LINE, thickness);
		style(app, G9U1TestApp.eval(app, "c=Circle((" + 30 * s + "," + 8 * s + "),"
				+ 4 * s + ")"), CIRCLE, thickness);
		style(app, G9U1TestApp.eval(app, "P=(" + 10 * s + "," + 8 * s + ")"), POINT, 0);
		GeoElement[] dimension = app.getKernel().getAlgebraProcessor().processAlgebraCommand(
				"d=AlignedDimension((0," + -6 * s + "),(" + 40 * s + "," + -6 * s + "),"
						+ -2 * s + "," + 2 * s + "," + 1 * s + ")", false)[0].toGeoElement()
				.getParentAlgorithm().getOutput();
		AlgoNativeDimension algo = AlgoNativeDimension.ownerOf(dimension[0]);
		style(app, algo.getDimensionLine(), DIMENSION_LINE, thickness);
		style(app, algo.getExtensionA(), EXTENSION_A, thickness);
		style(app, algo.getExtensionB(), EXTENSION_B, thickness);
		style(app, algo.getPresentationText(), TEXT, 0);
		style(app, G9U1TestApp.eval(app, "T=Text(\"40 mm\",(" + 20 * s + "," + 2 * s + "))"),
				PLAIN_TEXT, 0);
		for (GeoElement input : algo.getInput()) {
			if (input.isGeoPoint()) {
				input.setEuclidianVisible(false);
				input.updateRepaint();
			}
		}
		assertTrue(app.getExportAreaSession().defineManual(App.VIEW_EUCLIDIAN, -10 * s,
				50 * s, -10 * s, 20 * s));
		return app;
	}

	private static void classicEval(AppD classic, String command) {
		assertNotNull(classic.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false), command);
	}

	private static void style(AppGeoCeDG app, GeoElement geo, GColor color, int thickness) {
		geo.setObjColor(color);
		geo.setLineOpacity(255);
		if (thickness > 0) {
			geo.setLineThickness(thickness);
		}
		geo.setLabelVisible(false);
		geo.updateVisualStyleRepaint(org.geogebra.common.kernel.geos.GProperty.COLOR);
		geo.updateRepaint();
	}

	private static void zoom(AppGeoCeDG app, double pxPerUnit) throws Exception {
		EuclidianView view = app.getEuclidianView1();
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(800, 600);
			view.updateSize();
			view.setCoordSystem(100, 500, pxPerUnit, pxPerUnit);
			view.setShowAxes(false, false);
			view.showGrid(false);
		});
	}

	private static double factor(UnitToken unit) {
		return UnitState.of(unit, null, null).effectiveConstructionMetresPerUnit();
	}

	private Map<String, Object> base(String area, String unit, String scale, int thickness,
			double zoom, AppGeoCeDG app, Pdf pdf) {
		EuclidianView view = app.getEuclidianView1();
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("exportArea", area);
		row.put("unit", unit);
		row.put("drawingScale", scale);
		row.put("lineThickness", thickness);
		row.put("xscale", view.getXscale());
		row.put("yscale", view.getYscale());
		row.put("xZero", view.getXZero());
		row.put("yZero", view.getYZero());
		row.put("viewportPx", view.getWidth() + "x" + view.getHeight());
		row.put("physicalExportScaleCmPerUnit", app.getPhysicalExportScale());
		row.put("pdfPointsPerViewPixel",
				org.geocedg.desktop.export.PictureExportService.pdfPointsPerPixel(view));
		row.put("pageWidthMm", pdf.width() * MM_PER_PT);
		row.put("pageHeightMm", pdf.height() * MM_PER_PT);
		return row;
	}

	private File output(String name) throws Exception {
		String keep = System.getenv("GEOCEDG_POST_E2_P1_R1_PDF_DIR");
		Path dir = keep == null || keep.isEmpty() ? temporary : Paths.get(keep);
		Files.createDirectories(dir);
		return dir.resolve(name + ".pdf").toFile();
	}

	private String pdf(AppGeoCeDG app, String name) throws Exception {
		File file = output(name);
		app.getPictureExportService().writePDF(app.getEuclidianView1(), file, true);
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
	}

	private static void record(String family, Map<String, Object> row) {
		synchronized (REPORT) {
			REPORT.computeIfAbsent(family, key -> new ArrayList<>()).add(row);
		}
	}

	// ------------------------------------------------------- PDF interpretation

	/** A stroked path: centreline points in page space and the effective width. */
	static final class Stroke {
		final double userWidth;
		final double effectivePt;
		final int cap;
		final int join;
		final List<double[]> points;
		/** effective dash lengths in points, or null for a solid stroke */
		double[] dashPt;

		Stroke(double userWidth, double effectivePt, int cap, int join, List<double[]> points) {
			this.userWidth = userWidth;
			this.effectivePt = effectivePt;
			this.cap = cap;
			this.join = join;
			this.points = points;
		}

		double length() {
			double sum = 0;
			for (int i = 1; i < points.size(); i++) {
				sum += Math.hypot(points.get(i)[0] - points.get(i - 1)[0],
						points.get(i)[1] - points.get(i - 1)[1]);
			}
			return sum;
		}
	}

	/** Axis-aligned bounds of the fills of one colour, page space. */
	static final class Box {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		void add(double[] p) {
			minX = Math.min(minX, p[0]);
			minY = Math.min(minY, p[1]);
			maxX = Math.max(maxX, p[0]);
			maxY = Math.max(maxY, p[1]);
		}

		double width() {
			return maxX - minX;
		}

		double height() {
			return maxY - minY;
		}
	}

	/** Interpreted page: strokes and fills by colour, the largest text size. */
	static final class Pdf {
		final double[] mediaBox;
		final Map<String, List<Stroke>> strokes = new LinkedHashMap<>();
		final Map<String, Box> fills = new LinkedHashMap<>();
		final Map<String, List<Box>> fillParts = new LinkedHashMap<>();
		double maxFontPt;

		Pdf(double[] mediaBox) {
			this.mediaBox = mediaBox;
		}

		double width() {
			return mediaBox[2] - mediaBox[0];
		}

		double height() {
			return mediaBox[3] - mediaBox[1];
		}

		/** @return the longest stroke of a colour, or null */
		Stroke stroke(GColor color) {
			Stroke best = null;
			for (Stroke s : strokes.getOrDefault(key(color), List.of())) {
				if (best == null || s.length() > best.length()) {
					best = s;
				}
			}
			return best;
		}

		Box fillBox(GColor color) {
			return fills.get(key(color));
		}

		/** @return the widest single fill of a colour, or null */
		Box widestFill(GColor color) {
			Box best = null;
			for (Box b : fillParts.getOrDefault(key(color), List.of())) {
				if (best == null || b.width() > best.width()) {
					best = b;
				}
			}
			return best;
		}
	}

	private static String key(GColor color) {
		return Math.round(color.getRed() / 2.55) + "/" + Math.round(color.getGreen() / 2.55)
				+ "/" + Math.round(color.getBlue() / 2.55);
	}

	private static String key(double r, double g, double b) {
		return Math.round(r * 100) + "/" + Math.round(g * 100) + "/" + Math.round(b * 100);
	}

	/** Interprets the decoded page content of a FreeHEP PDF. */
	static Pdf interpret(String file) throws Exception {
		Matcher box = Pattern.compile("/MediaBox \\[([^\\]]*)\\]").matcher(file);
		assertTrue(box.find(), "MediaBox");
		String[] numbers = box.group(1).trim().split("\\s+");
		Pdf pdf = new Pdf(new double[] {Double.parseDouble(numbers[0]),
			Double.parseDouble(numbers[1]), Double.parseDouble(numbers[2]),
			Double.parseDouble(numbers[3])});
		String content = PostE2P1DimensionCaptureCharacterizationTest.pdfContent(file);
		Deque<double[]> stack = new ArrayDeque<>();
		double[] ctm = {1, 0, 0, 1, 0, 0};
		double width = 1;
		int cap = 0;
		int join = 0;
		String stroke = "0/0/0";
		String fill = "0/0/0";
		double[] dash = null;
		Deque<double[]> dashStack = new ArrayDeque<>();
		double fontSize = 0;
		double[] textMatrix = {1, 0, 0, 1, 0, 0};
		List<double[]> path = new ArrayList<>();
		List<String> operands = new ArrayList<>();
		Matcher token = Pattern.compile("\\[[^\\]]*\\]|\\([^)]*\\)|<[^>]*>|/[^\\s/\\[\\]()<>]+"
				+ "|[^\\s/\\[\\]()<>]+").matcher(content);
		while (token.find()) {
			String t = token.group();
			if (t.startsWith("[") || t.startsWith("(") || t.startsWith("<")
					|| t.startsWith("/") || isNumber(t)) {
				operands.add(t);
				continue;
			}
			switch (t) {
			case "q":
				stack.push(new double[] {ctm[0], ctm[1], ctm[2], ctm[3], ctm[4], ctm[5], width,
					cap, join});
				dashStack.push(dash == null ? new double[0] : dash);
				break;
			case "Q":
				double[] saved = stack.pop();
				System.arraycopy(saved, 0, ctm, 0, 6);
				width = saved[6];
				cap = (int) saved[7];
				join = (int) saved[8];
				double[] savedDash = dashStack.pop();
				dash = savedDash.length == 0 ? null : savedDash;
				break;
			case "d":
				String array = operands.get(0).replace("[", "").replace("]", "").trim();
				if (array.isEmpty()) {
					dash = null;
				} else {
					String[] parts = array.split("\\s+");
					dash = new double[parts.length];
					for (int i = 0; i < parts.length; i++) {
						dash[i] = Double.parseDouble(parts[i]);
					}
				}
				break;
			case "cm":
				ctm = multiply(numbers(operands, 6), ctm);
				break;
			case "w":
				width = number(operands, 0);
				break;
			case "J":
				cap = (int) number(operands, 0);
				break;
			case "j":
				join = (int) number(operands, 0);
				break;
			case "RG":
				stroke = key(number(operands, 0), number(operands, 1), number(operands, 2));
				break;
			case "rg":
				fill = key(number(operands, 0), number(operands, 1), number(operands, 2));
				break;
			case "m":
				path.add(new double[] {Double.NaN});
				path.add(apply(ctm, number(operands, 0), number(operands, 1)));
				break;
			case "l":
				path.add(apply(ctm, number(operands, 0), number(operands, 1)));
				break;
			case "c":
				path.add(apply(ctm, number(operands, 4), number(operands, 5)));
				break;
			case "v":
			case "y":
				path.add(apply(ctm, number(operands, 2), number(operands, 3)));
				break;
			case "re":
				double x = number(operands, 0);
				double y = number(operands, 1);
				double w = number(operands, 2);
				double h = number(operands, 3);
				path.add(new double[] {Double.NaN});
				path.add(apply(ctm, x, y));
				path.add(apply(ctm, x + w, y));
				path.add(apply(ctm, x + w, y + h));
				path.add(apply(ctm, x, y + h));
				break;
			case "S":
			case "s":
				List<double[]> points = new ArrayList<>();
				for (double[] p : path) {
					if (p.length == 2) {
						points.add(p);
					}
				}
				double scale = Math.sqrt(Math.abs(ctm[0] * ctm[3] - ctm[1] * ctm[2]));
				Stroke drawn = new Stroke(width, width * scale, cap, join, points);
				if (dash != null) {
					drawn.dashPt = new double[dash.length];
					for (int i = 0; i < dash.length; i++) {
						drawn.dashPt[i] = dash[i] * scale;
					}
				}
				pdf.strokes.computeIfAbsent(stroke, k -> new ArrayList<>()).add(drawn);
				path.clear();
				break;
			case "f":
			case "F":
			case "f*":
			case "B":
			case "b":
				Box target = pdf.fills.computeIfAbsent(fill, k -> new Box());
				Box part = new Box();
				for (double[] p : path) {
					if (p.length == 2) {
						target.add(p);
						part.add(p);
					}
				}
				pdf.fillParts.computeIfAbsent(fill, k -> new ArrayList<>()).add(part);
				path.clear();
				break;
			case "n":
				path.clear();
				break;
			case "Tf":
				fontSize = number(operands, 1);
				break;
			case "Tm":
				textMatrix = numbers(operands, 6);
				break;
			case "Tj":
			case "TJ":
				double[] text = multiply(textMatrix, ctm);
				double effective = fontSize
						* Math.sqrt(Math.abs(text[0] * text[3] - text[1] * text[2]));
				pdf.maxFontPt = Math.max(pdf.maxFontPt, effective);
				break;
			default:
				break;
			}
			operands.clear();
		}
		return pdf;
	}

	private static boolean isNumber(String t) {
		return t.matches("[-+]?(\\d+\\.?\\d*|\\.\\d+)");
	}

	private static double number(List<String> operands, int index) {
		return Double.parseDouble(operands.get(index));
	}

	private static double[] numbers(List<String> operands, int count) {
		double[] values = new double[count];
		int offset = operands.size() - count;
		for (int i = 0; i < count; i++) {
			values[i] = Double.parseDouble(operands.get(offset + i));
		}
		return values;
	}

	/** {@code a x b} for PDF matrices [a b c d e f]. */
	private static double[] multiply(double[] a, double[] b) {
		return new double[] {a[0] * b[0] + a[1] * b[2], a[0] * b[1] + a[1] * b[3],
			a[2] * b[0] + a[3] * b[2], a[2] * b[1] + a[3] * b[3],
			a[4] * b[0] + a[5] * b[2] + b[4], a[4] * b[1] + a[5] * b[3] + b[5]};
	}

	private static double[] apply(double[] m, double x, double y) {
		return new double[] {m[0] * x + m[2] * y + m[4], m[1] * x + m[3] * y + m[5]};
	}

}
