/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.desktop.PostE2P1R1PdfStrokeWidthCharacterizationTest.Box;
import org.geocedg.desktop.PostE2P1R1PdfStrokeWidthCharacterizationTest.Pdf;
import org.geocedg.desktop.PostE2P1R1PdfStrokeWidthCharacterizationTest.Stroke;
import org.geocedg.desktop.export.DrawingScale;
import org.geocedg.desktop.export.PictureExportService;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GProperty;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.App;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * POST-E2-P1-R2 (physical-pdf-style-sizes, ADR 0035, PROPOSED): on GeoCeDG's physical
 * PDF route one style pixel is 0.8 pt, so a stroke of line thickness t is 0.4 t pt and
 * every lineThickness-derived size keeps GeoGebra's proportion to it; smoke follow-up A:
 * point markers follow 0.5 pt per marker style pixel (PGF/TikZ marker geometry);
 * whatever the zoom, unit, presentation unit, drawing scale and export-area mode. The
 * produced PDF is interpreted (P1-R1 interpreter: graphics-state stack, CTM, w, caps,
 * joins, dash, paths, strokes, fills; colour-attributed objects).
 *
 * <p>With {@code GEOCEDG_POST_E2_P1_R2_REPORT=<file>} the measured rows are written as
 * JSON and with {@code GEOCEDG_POST_E2_P1_R2_PDF_DIR=<dir>} the PDFs (and the PGF code of
 * the marker comparison) are kept for an independent rasterization; assertions never
 * depend on them.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostE2P1R2PhysicalPdfStyleTest {
	private static final double MM_PER_PT = 25.4 / 72;
	private static final double PT_PER_STYLE_PX = 0.8;
	/** Smoke follow-up A: PDF points per point-marker style pixel. */
	private static final double PT_PER_MARKER_PX = 0.5;
	/** TikZ's default line width, used by the PGF exporter for marker outlines. */
	private static final double PGF_DEFAULT_LINE_PT = 0.4;
	/** PDF numbers have five significant figures: a relative tolerance of 1e-3. */
	private static final double RELATIVE = 1E-3;
	private static final int[] THICKNESSES = {1, 2, 3, 5};
	private static final double[] ZOOMS = {2, 8, 32};
	private static final GColor SEGMENT = GColor.newColor(255, 0, 0);
	private static final GColor LINE = GColor.newColor(0, 255, 0);
	private static final GColor CIRCLE = GColor.newColor(0, 0, 255);
	private static final GColor CURVE = GColor.newColor(128, 128, 0);
	private static final GColor POLYGON = GColor.newColor(0, 128, 128);
	private static final GColor DASHED = GColor.newColor(128, 0, 0);
	private static final GColor DIMENSION_LINE = GColor.newColor(255, 0, 255);
	private static final GColor EXTENSION_A = GColor.newColor(0, 255, 255);
	private static final GColor EXTENSION_B = GColor.newColor(255, 128, 0);
	private static final GColor POINT = GColor.newColor(255, 255, 0);
	private static final Map<String, List<Map<String, Object>>> REPORT =
			new LinkedHashMap<>();

	@TempDir
	Path temporary;

	// -------------------------------------------------- §8.1 ordinary objects

	@Test
	void everyStrokeIsZeroPointFourTimesTheThicknessAtEveryZoom() throws Exception {
		for (int thickness : THICKNESSES) {
			AppGeoCeDG app = scene(UnitToken.MM, DrawingScale.ONE_TO_ONE, 1, thickness);
			double expectedPt = 0.4 * thickness;
			for (double zoom : ZOOMS) {
				zoom(app, zoom);
				Pdf pdf = pdf(app, "objects-t" + thickness + "-z" + zoom);
				Map<String, Object> row = row("objects", "mm", "1:1", "explicit", thickness,
						zoom, app, pdf);
				for (Object[] tag : new Object[][] {{"segment", SEGMENT}, {"line", LINE},
						{"circle", CIRCLE}, {"curve", CURVE}, {"polygonEdge", POLYGON},
						{"extensionA", EXTENSION_A}, {"extensionB", EXTENSION_B},
						{"dashed", DASHED}}) {
					Stroke stroke = pdf.stroke((GColor) tag[1]);
					assertNotNull(stroke, tag[0] + " stroked");
					row.put(tag[0] + "WidthMm", stroke.effectivePt * MM_PER_PT);
					row.put(tag[0] + "Cap", stroke.cap);
					row.put(tag[0] + "Join", stroke.join);
					assertEquals(expectedPt, stroke.effectivePt, expectedPt * RELATIVE,
							tag[0] + " t=" + thickness + " zoom " + zoom);
				}
				// dash pattern: GeoGebra's long dash (8 + w, 8 style px) at 0.8 pt per px
				Stroke dashed = pdf.stroke(DASHED);
				assertNotNull(dashed.dashPt, "dash array");
				double styleWidth = thickness / 2.0;
				assertEquals((8 + styleWidth) * PT_PER_STYLE_PX, dashed.dashPt[0],
						dashed.dashPt[0] * RELATIVE, "dash");
				assertEquals(8 * PT_PER_STYLE_PX, dashed.dashPt[1], dashed.dashPt[1] * RELATIVE,
						"gap");
				row.put("dashedDashPt", dashed.dashPt[0]);
				row.put("dashedGapPt", dashed.dashPt[1]);
				// the centreline geometry and the page do not depend on the zoom
				assertEquals(40, pdf.stroke(SEGMENT).length() * MM_PER_PT, 0.01);
				assertEquals(60, pdf.width() * MM_PER_PT, 0.01);
				assertEquals(30, pdf.height() * MM_PER_PT, 0.01);
				record("objects", row);
			}
		}
	}

	// --------------------------------------------------- §8.2 native dimension

	@Test
	void dimensionBodyExtensionsAndArrowheadsHavePhysicalSizes() throws Exception {
		for (int thickness : THICKNESSES) {
			AppGeoCeDG app = scene(UnitToken.MM, DrawingScale.ONE_TO_ONE, 1, thickness);
			AlgoNativeDimension algo = dimension(app);
			double[] before = endpoints(algo);
			for (double zoom : ZOOMS) {
				zoom(app, zoom);
				Pdf pdf = pdf(app, "dimension-t" + thickness + "-z" + zoom);
				List<Box> parts = new ArrayList<>(pdf.fillParts.get(key(DIMENSION_LINE)));
				parts.sort(Comparator.comparingDouble(b -> -b.width()));
				Box body = parts.get(0);
				List<Box> heads = new ArrayList<>(parts.subList(1, parts.size()));
				heads.sort(Comparator.comparingDouble(b -> b.minX));
				assertEquals(2, heads.size(), "two arrowheads");
				double bodyPt = body.height();
				double headA = heads.get(0).height();
				double headB = heads.get(1).height();
				Map<String, Object> row = row("dimension", "mm", "1:1", "explicit", thickness,
						zoom, app, pdf);
				row.put("bodyMm", bodyPt * MM_PER_PT);
				row.put("arrowheadAHeightMm", headA * MM_PER_PT);
				row.put("arrowheadBHeightMm", headB * MM_PER_PT);
				row.put("arrowheadALengthMm", heads.get(0).width() * MM_PER_PT);
				row.put("extensionAWidthMm", pdf.stroke(EXTENSION_A).effectivePt * MM_PER_PT);
				record("dimension", row);
				// fills are measured between two printed coordinates: their resolution
				double resolution = resolutionPt(app, pdf);
				row.put("fillResolutionMm", resolution * MM_PER_PT);
				assertEquals(0.4 * thickness, bodyPt, resolution, "body");
				// end-style arrow: 2 t style px across plus its outline of t / 2 style px, i.e.
				// 2.5 t style px, five times the line width as on the screen
				assertEquals(2.5 * thickness * PT_PER_STYLE_PX, headA, resolution,
						"arrowhead A");
				assertEquals(headA, headB, resolution, "arrowheads A and B");
				assertEquals(0.4 * thickness, pdf.stroke(EXTENSION_A).effectivePt,
						0.4 * thickness * RELATIVE);
				assertEquals(0.4 * thickness, pdf.stroke(EXTENSION_B).effectivePt,
						0.4 * thickness * RELATIVE);
			}
			assertArrayEquals(before, endpoints(algo), 0, "the dimension geometry is untouched");
		}
	}

	// ---------------------------------------------------- §8.3 point markers

	/**
	 * Smoke follow-up A (amends the R2 expectation of 1.6 s pt, kept in the history of
	 * 542a5adc): the whole marker follows 0.5 pt per marker style pixel, so the marker
	 * geometry spans the point size s in points, as in PGF/TikZ, and its outline keeps
	 * GeoGebra's s / 2 style px, i.e. s / 4 pt.
	 */
	@Test
	void pointMarkersSpanThePointSizeInPointsAtEveryZoom() throws Exception {
		int[] styles = {EuclidianStyleConstants.POINT_STYLE_DOT,
			EuclidianStyleConstants.POINT_STYLE_FILLED_DIAMOND,
			EuclidianStyleConstants.POINT_STYLE_EMPTY_DIAMOND,
			EuclidianStyleConstants.POINT_STYLE_CROSS,
			EuclidianStyleConstants.POINT_STYLE_PLUS,
			EuclidianStyleConstants.POINT_STYLE_CIRCLE};
		for (int style : styles) {
			for (int size : new int[] {3, 5, 9}) {
				AppGeoCeDG app = pointScene(style, size);
				double[] centre = null;
				for (double zoom : ZOOMS) {
					zoom(app, zoom);
					Pdf pdf = pdf(app, "point-s" + style + "-" + size + "-z" + zoom);
					Box box = markerBox(pdf);
					assertNotNull(box, "marker drawn");
					double extent = box.width();
					Map<String, Object> row = row("points", "mm", "1:1", "explicit", 2, zoom,
							app, pdf);
					row.put("pointStyle", style);
					row.put("pointSize", size);
					row.put("markerExtentMm", extent * MM_PER_PT);
					record("points", row);
					// filled markers: the fill spans 2 s marker px; stroked markers: the path
					// spans 2 s marker px (centrelines)
					row.put("fillResolutionMm", resolutionPt(app, pdf) * MM_PER_PT);
					assertEquals(2 * size * PT_PER_MARKER_PX, extent, resolutionPt(app, pdf)
							+ 2 * size * PT_PER_MARKER_PX * (filled(style) ? RELATIVE : 0.02),
							"style " + style + " size " + size + " zoom " + zoom);
					assertEquals(extent, box.height(), resolutionPt(app, pdf)
							+ extent * (filled(style) ? RELATIVE : 0.02), "square extent");
					double outline = outlinePt(pdf, style);
					row.put("markerOutlineMm", outline * MM_PER_PT);
					double expectedOutline = style == EuclidianStyleConstants.POINT_STYLE_DOT
							? PT_PER_MARKER_PX : size / 2.0 * PT_PER_MARKER_PX;
					assertEquals(expectedOutline, outline, expectedOutline * RELATIVE,
							"outline of style " + style + " size " + size);
					double[] c = {(box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2};
					if (centre == null) {
						centre = c;
					} else {
						assertArrayEquals(centre, c, 0.01 / MM_PER_PT, "the point position");
					}
				}
				GeoPoint point = (GeoPoint) app.getKernel().lookupLabel("P");
				assertEquals(size, point.getPointSize(), "the stored point size");
				assertEquals(style, point.getPointStyle(), "the stored point style");
			}
		}
	}

	/**
	 * Smoke follow-up A: the PDF marker geometry measured in the exported PDF equals the
	 * PGF/TikZ marker geometry written for the same point, for every style whose shape
	 * GeoGebra's screen and the PGF exporter share, at every zoom. Two shape-related
	 * differences are measured and reported: PGF triangles have a circumradius of 3 s / 4
	 * pt against s / 2 pt (the screen proportion of every marker), and PGF strokes every
	 * outline with TikZ's default 0.4 pt line and round caps, against GeoGebra's s / 2
	 * style px (dot border 1 style px) with square caps and mitre joins.
	 */
	@Test
	void pointMarkersMatchThePgfMarkerGeometry() throws Exception {
		for (int style = EuclidianStyleConstants.POINT_STYLE_DOT;
				style <= EuclidianStyleConstants.POINT_STYLE_TRIANGLE_WEST; style++) {
			for (int size : new int[] {3, 5, 9}) {
				AppGeoCeDG app = pointScene(style, size);
				double[] pgfFirst = null;
				for (double zoom : ZOOMS) {
					zoom(app, zoom);
					Pdf pdf = pdf(app, "pgf-point-s" + style + "-" + size + "-z" + zoom);
					Box box = markerBox(pdf);
					assertNotNull(box, "PDF marker drawn");
					String pgf = PreG9BR6PlusCLatexExportTest.generate(
							PreG9BR6PlusCLatexExportTest.exporter(app, "pgf"),
							app.getEuclidianView1());
					keepPgf(pgf, "pgf-point-s" + style + "-" + size + "-z" + zoom);
					double[] pgfExtent = pgfMarkerExtent(pgf, POINT);
					if (pgfFirst == null) {
						pgfFirst = pgfExtent;
					} else {
						assertArrayEquals(pgfFirst, pgfExtent, 0, "PGF marker and zoom");
					}
					boolean triangle = style >= EuclidianStyleConstants.POINT_STYLE_TRIANGLE_NORTH;
					final double ratio = triangle ? 2 / 3.0 : 1;
					final double tolerance = resolutionPt(app, pdf)
							+ pgfExtent[0] * (filled(style) ? 0.01 : 0.03);
					Map<String, Object> row = row("pointsPgf", "mm", "1:1", "explicit", 2, zoom,
							app, pdf);
					row.put("pointStyle", style);
					row.put("pointSize", size);
					row.put("pdfWidthPt", box.width());
					row.put("pdfHeightPt", box.height());
					row.put("pgfWidthPt", pgfExtent[0]);
					row.put("pgfHeightPt", pgfExtent[1]);
					row.put("pdfOutlinePt", outlinePt(pdf, style));
					row.put("pgfOutlinePt", PGF_DEFAULT_LINE_PT);
					row.put("pdfToPgfGeometry", box.width() / pgfExtent[0]);
					record("pointsPgf", row);
					assertEquals(ratio * pgfExtent[0], box.width(), tolerance,
							"width of style " + style + " size " + size + " zoom " + zoom);
					assertEquals(ratio * pgfExtent[1], box.height(), tolerance,
							"height of style " + style + " size " + size + " zoom " + zoom);
				}
			}
		}
	}

	private AppGeoCeDG pointScene(int style, int size) throws Exception {
		AppGeoCeDG app = scene(UnitToken.MM, DrawingScale.ONE_TO_ONE, 1, 2);
		GeoPoint point = (GeoPoint) app.getKernel().lookupLabel("P");
		point.setPointStyle(style);
		point.setPointSize(size);
		point.updateVisualStyleRepaint(GProperty.POINT_STYLE);
		return app;
	}

	private static boolean filled(int style) {
		return style == EuclidianStyleConstants.POINT_STYLE_DOT
				|| style == EuclidianStyleConstants.POINT_STYLE_FILLED_DIAMOND
				|| style >= EuclidianStyleConstants.POINT_STYLE_TRIANGLE_NORTH;
	}

	/** @return the outline width of the marker: the black dot border or its own stroke */
	private static double outlinePt(Pdf pdf, int style) {
		Stroke stroke = pdf.stroke(style == EuclidianStyleConstants.POINT_STYLE_DOT
				? GColor.BLACK : POINT);
		assertNotNull(stroke, "marker outline of style " + style);
		return stroke.effectivePt;
	}

	/**
	 * @return width and height in points of the PGF marker path of the given colour:
	 *         {@code circle (r pt)} or the relative {@code ++(x pt,y pt)} steps, rotated
	 *         by the TikZ {@code rotate} option
	 */
	static double[] pgfMarkerExtent(String pgf, GColor color) {
		Matcher definition = Pattern.compile("\\\\definecolor\\{([^}]*)\\}\\{rgb\\}\\{"
				+ pgfComponent(color.getRed()) + "," + pgfComponent(color.getGreen()) + ","
				+ pgfComponent(color.getBlue()) + "\\}").matcher(pgf);
		assertTrue(definition.find(), pgf);
		String name = Pattern.quote(definition.group(1));
		Matcher line = Pattern.compile("\\\\draw \\[(?:fill|color)=" + name + "[^\\n]*")
				.matcher(pgf);
		assertTrue(line.find(), "PGF marker of " + definition.group(1) + "\n" + pgf);
		String code = line.group();
		Matcher circle = Pattern.compile("circle \\(([-0-9.E]+)pt\\)").matcher(code);
		if (circle.find()) {
			double radius = Double.parseDouble(circle.group(1));
			return new double[] {2 * radius, 2 * radius};
		}
		Matcher rotate = Pattern.compile("rotate=([-0-9.]+)").matcher(code);
		double angle = rotate.find() ? Math.toRadians(Double.parseDouble(rotate.group(1))) : 0;
		Box box = new Box();
		double x = 0;
		double y = 0;
		box.add(new double[] {0, 0});
		Matcher step = Pattern.compile(
				"\\+\\+\\(\\s*([-0-9.E]+)\\s*(?:pt)?\\s*,\\s*([-0-9.E]+)\\s*(?:pt)?\\s*\\)")
				.matcher(code);
		boolean any = false;
		while (step.find()) {
			x += Double.parseDouble(step.group(1));
			y += Double.parseDouble(step.group(2));
			box.add(new double[] {x * Math.cos(angle) - y * Math.sin(angle),
				x * Math.sin(angle) + y * Math.cos(angle)});
			any = true;
		}
		assertTrue(any, code);
		return new double[] {box.width(), box.height()};
	}

	private static String pgfComponent(int value) {
		return value == 255 ? "1\\.?" : value == 0 ? "0\\.?" : "[0-9.]+";
	}

	// ------------------------------------------------------- §8.4 drawing scales

	@Test
	void drawingScaleScalesTheGeometryButNeverTheStroke() throws Exception {
		for (DrawingScale scale : new DrawingScale[] {DrawingScale.ONE_TO_ONE,
				DrawingScale.of(1, 2), DrawingScale.of(1, 10), DrawingScale.of(2, 1)}) {
			double ratio = (double) scale.getNumerator() / scale.getDenominator();
			AppGeoCeDG app = scene(UnitToken.MM, scale, 1, 2);
			for (double zoom : ZOOMS) {
				zoom(app, zoom);
				Pdf pdf = pdf(app, "scale-" + scale.getNumerator() + "-"
						+ scale.getDenominator() + "-z" + zoom);
				Stroke segment = pdf.stroke(SEGMENT);
				Map<String, Object> row = row("scales", "mm", scale.toString(), "explicit", 2,
						zoom, app, pdf);
				row.put("segmentWidthMm", segment.effectivePt * MM_PER_PT);
				row.put("segmentLengthMm", segment.length() * MM_PER_PT);
				record("scales", row);
				assertEquals(0.8, segment.effectivePt, 0.8 * RELATIVE);
				assertEquals(40 * ratio, segment.length() * MM_PER_PT, 0.01);
				assertEquals(60 * ratio, pdf.width() * MM_PER_PT, 0.01);
			}
		}
	}

	// ----------------------------------------- §8.5 units and presentation unit

	@Test
	void physicallyEquivalentUnitsGiveTheSameWidthAndThePresentationUnitNothing()
			throws Exception {
		Object[][] units = {{UnitState.of(UnitToken.MM, null, null), 1.0},
			{UnitState.of(UnitToken.CM, null, null), 0.1},
			{UnitState.of(UnitToken.M, null, null), 0.001},
			{UnitState.of(UnitToken.USM, null, UsmDefinition.of(0.0254, "inch", "in")),
				1 / 25.4}};
		for (Object[] unit : units) {
			double s = (double) unit[1];
			AppGeoCeDG app = scene((UnitState) unit[0], DrawingScale.ONE_TO_ONE, s, 3);
			for (double pxPerMm : new double[] {2, 8, 32}) {
				zoom(app, pxPerMm / s);
				Pdf pdf = pdf(app, "unit-" + s + "-z" + pxPerMm);
				Stroke segment = pdf.stroke(SEGMENT);
				Map<String, Object> row = row("units", unit[0].toString(), "1:1", "explicit",
						3, pxPerMm / s, app, pdf);
				row.put("segmentWidthMm", segment.effectivePt * MM_PER_PT);
				row.put("segmentLengthMm", segment.length() * MM_PER_PT);
				record("units", row);
				assertEquals(1.2, segment.effectivePt, 1.2 * RELATIVE);
				assertEquals(40, segment.length() * MM_PER_PT, 0.01);
			}
		}
		// changing only the presentation unit changes no byte of the PDF
		AppGeoCeDG app = scene(UnitToken.CM, DrawingScale.ONE_TO_ONE, 0.1, 2);
		zoom(app, 80);
		String first = raw(app, "presentation-default");
		app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, UnitToken.MM, null));
		String second = raw(app, "presentation-mm");
		assertEquals(normalize(first), normalize(second));
	}

	// --------------------------------------- §8.6/§8.7 zoom and export areas

	@Test
	void theViewportFallbackAreaKeepsThePhysicalWidth() throws Exception {
		AppGeoCeDG app = scene(UnitToken.MM, DrawingScale.ONE_TO_ONE, 1, 2);
		app.getExportAreaSession().clear();
		for (double zoom : ZOOMS) {
			zoom(app, zoom);
			Pdf pdf = pdf(app, "viewport-z" + zoom);
			Stroke segment = pdf.stroke(SEGMENT);
			Map<String, Object> row = row("viewport", "mm", "1:1", "viewport", 2, zoom, app,
					pdf);
			row.put("segmentWidthMm", segment == null ? null : segment.effectivePt * MM_PER_PT);
			record("viewport", row);
			// the page follows the visible view (800 px), the stroke does not
			assertEquals(800 / zoom, pdf.width() * MM_PER_PT, 0.01);
			assertNotNull(segment);
			assertEquals(0.8, segment.effectivePt, 0.8 * RELATIVE);
		}
	}

	// ------------------------------------------- §8.8 document and live view

	@Test
	void anExportChangesNeitherTheDocumentNorTheLiveView() throws Exception {
		AppGeoCeDG app = scene(UnitToken.MM, DrawingScale.ONE_TO_ONE, 1, 2);
		String xml = app.getXML();
		pdf(app, "document");
		assertEquals(xml, app.getXML(), "no document or style change");
		assertEquals(1, app.getEuclidianView1().getPhysicalStyleScale(), 0);
		assertEquals(1, PictureExportService.physicalStyleScale(0.8), 0,
				"0.8 pt per view pixel needs no style scale");
		assertEquals(1, app.getEuclidianView1().getPhysicalMarkerScale(), 0);
		assertEquals(1, PictureExportService.physicalMarkerScale(0.5), 0,
				"0.5 pt per view pixel needs no marker scale");
	}

	@AfterAll
	static void writeReport() throws Exception {
		String target = System.getenv("GEOCEDG_POST_E2_P1_R2_REPORT");
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

	private AppGeoCeDG scene(UnitToken unit, DrawingScale scale, double s, int thickness)
			throws Exception {
		return scene(UnitState.of(unit, null, null), scale, s, thickness);
	}

	/** A 40 mm scene in model units of {@code s} per millimetre of the drawing. */
	private AppGeoCeDG scene(UnitState unit, DrawingScale scale, double s, int thickness)
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		zoom(app, 8 / s);
		app.getDocumentUnits().replace(unit);
		app.setDrawingScale(scale);
		style(G9U1TestApp.eval(app, "s=Segment((0,0),(" + 40 * s + ",0))"), SEGMENT,
				thickness, EuclidianStyleConstants.LINE_TYPE_FULL);
		style(G9U1TestApp.eval(app, "g=Line((0," + 15 * s + "),(" + 40 * s + ","
				+ 18 * s + "))"), LINE, thickness, EuclidianStyleConstants.LINE_TYPE_FULL);
		style(G9U1TestApp.eval(app, "c=Circle((" + 30 * s + "," + 8 * s + "),"
				+ 4 * s + ")"), CIRCLE, thickness, EuclidianStyleConstants.LINE_TYPE_FULL);
		style(G9U1TestApp.eval(app, "f=Curve(" + 2 * s + "+t," + 10 * s + "+(t/" + s
				+ ")^2*" + s / 20 + ",t,0," + 8 * s + ")"), CURVE, thickness,
				EuclidianStyleConstants.LINE_TYPE_FULL);
		style(G9U1TestApp.eval(app, "dsh=Segment((0," + 4 * s + "),(" + 20 * s + "," + 4 * s
				+ "))"), DASHED, thickness, EuclidianStyleConstants.LINE_TYPE_DASHED_LONG);
		GeoElement point = G9U1TestApp.eval(app, "P=(" + 10 * s + "," + 8 * s + ")");
		point.setObjColor(POINT);
		point.setLabelVisible(false);
		point.updateRepaint();
		GeoElement[] outputs = app.getKernel().getAlgebraProcessor().processAlgebraCommand(
				"d=AlignedDimension((0," + -6 * s + "),(" + 40 * s + "," + -6 * s + "),"
						+ -2 * s + "," + 2 * s + "," + s + ")", false)[0].toGeoElement()
				.getParentAlgorithm().getOutput();
		AlgoNativeDimension algo = AlgoNativeDimension.ownerOf(outputs[0]);
		style(algo.getDimensionLine(), DIMENSION_LINE, thickness,
				EuclidianStyleConstants.LINE_TYPE_FULL);
		style(algo.getExtensionA(), EXTENSION_A, thickness,
				EuclidianStyleConstants.LINE_TYPE_FULL);
		style(algo.getExtensionB(), EXTENSION_B, thickness,
				EuclidianStyleConstants.LINE_TYPE_FULL);
		algo.getPresentationText().setEuclidianVisible(false);
		algo.getPresentationText().updateRepaint();
		for (GeoElement input : algo.getInput()) {
			if (input.isGeoPoint()) {
				input.setEuclidianVisible(false);
				input.updateRepaint();
			}
		}
		GeoElement polygon = G9U1TestApp.eval(app, "q=Polygon((" + 44 * s + "," + 2 * s
				+ "),(" + 48 * s + "," + 2 * s + "),(" + 46 * s + "," + 6 * s + "))");
		style(polygon, POLYGON, thickness, EuclidianStyleConstants.LINE_TYPE_FULL);
		for (org.geogebra.common.kernel.kernelND.GeoSegmentND edge
				: ((org.geogebra.common.kernel.geos.GeoPolygon) polygon).getSegments()) {
			style(edge.toGeoElement(), POLYGON, thickness, EuclidianStyleConstants.LINE_TYPE_FULL);
		}
		assertTrue(app.getExportAreaSession().defineManual(App.VIEW_EUCLIDIAN, -10 * s,
				50 * s, -10 * s, 20 * s));
		return app;
	}

	private static AlgoNativeDimension dimension(AppGeoCeDG app) {
		return AlgoNativeDimension.ownerOf(app.getKernel().lookupLabel("d"));
	}

	private static double[] endpoints(AlgoNativeDimension algo) {
		return new double[] {algo.getDimensionLine().getStartPoint().getInhomX(),
			algo.getDimensionLine().getStartPoint().getInhomY(),
			algo.getDimensionLine().getEndPoint().getInhomX(),
			algo.getExtensionA().getStartPoint().getInhomY(),
			algo.getExtensionB().getEndPoint().getInhomY(), algo.getValue().getDouble()};
	}

	private static void style(GeoElement geo, GColor color, int thickness, int lineType) {
		geo.setObjColor(color);
		geo.setLineOpacity(255);
		geo.setAlphaValue(0);
		geo.setLineThickness(thickness);
		geo.setLineType(lineType);
		geo.setLabelVisible(false);
		geo.updateVisualStyleRepaint(GProperty.LINE_STYLE);
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

	/**
	 * @return the resolution of a distance between two printed PDF coordinates: FreeHEP
	 *         prints device coordinates with five significant figures
	 */
	private static double resolutionPt(AppGeoCeDG app, Pdf pdf) {
		double pointsPerPixel = PictureExportService.pdfPointsPerPixel(app.getEuclidianView1());
		double extent = Math.max(pdf.width(), pdf.height()) / pointsPerPixel;
		double ulp = Math.pow(10, Math.floor(Math.log10(extent)) - 4);
		return ulp * pointsPerPixel;
	}

	private static Box markerBox(Pdf pdf) {
		Box fill = pdf.fillBox(POINT);
		if (fill != null) {
			return fill;
		}
		Box strokes = new Box();
		boolean any = false;
		for (Stroke stroke : pdf.strokes.getOrDefault(key(POINT), List.of())) {
			for (double[] p : stroke.points) {
				strokes.add(p);
				any = true;
			}
		}
		return any ? strokes : null;
	}

	private static String key(GColor color) {
		return Math.round(color.getRed() / 2.55) + "/" + Math.round(color.getGreen() / 2.55)
				+ "/" + Math.round(color.getBlue() / 2.55);
	}

	private Map<String, Object> row(String family, String unit, String scale, String area,
			int thickness, double zoom, AppGeoCeDG app, Pdf pdf) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("unit", unit);
		row.put("drawingScale", scale);
		row.put("exportArea", area);
		row.put("lineThickness", thickness);
		row.put("xscale", zoom);
		row.put("pdfPointsPerViewPixel",
				PictureExportService.pdfPointsPerPixel(app.getEuclidianView1()));
		row.put("styleScale", PictureExportService.physicalStyleScale(
				PictureExportService.pdfPointsPerPixel(app.getEuclidianView1())));
		row.put("markerScale", PictureExportService.physicalMarkerScale(
				PictureExportService.pdfPointsPerPixel(app.getEuclidianView1())));
		row.put("pageWidthMm", pdf.width() * MM_PER_PT);
		row.put("pageHeightMm", pdf.height() * MM_PER_PT);
		return row;
	}

	private File output(String name) throws Exception {
		String keep = System.getenv("GEOCEDG_POST_E2_P1_R2_PDF_DIR");
		Path dir = keep == null || keep.isEmpty() ? temporary : Paths.get(keep);
		Files.createDirectories(dir);
		return dir.resolve(name + ".pdf").toFile();
	}

	/** Keeps the PGF code beside the kept PDFs for an independent rasterization. */
	private static void keepPgf(String pgf, String name) throws Exception {
		String keep = System.getenv("GEOCEDG_POST_E2_P1_R2_PDF_DIR");
		if (keep != null && !keep.isEmpty()) {
			Files.createDirectories(Paths.get(keep));
			Files.writeString(Paths.get(keep).resolve(name + ".tex"), pgf,
					StandardCharsets.UTF_8);
		}
	}

	private String raw(AppGeoCeDG app, String name) throws Exception {
		File file = output(name);
		app.getPictureExportService().writePDF(app.getEuclidianView1(), file, true);
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
	}

	private Pdf pdf(AppGeoCeDG app, String name) throws Exception {
		return PostE2P1R1PdfStrokeWidthCharacterizationTest.interpret(raw(app, name));
	}

	private static String normalize(String pdf) {
		return pdf.replaceAll("\\(D:[0-9+\\-Z']*\\)", "(D:)");
	}

	private static void record(String family, Map<String, Object> row) {
		synchronized (REPORT) {
			REPORT.computeIfAbsent(family, key -> new ArrayList<>()).add(row);
		}
	}
}
