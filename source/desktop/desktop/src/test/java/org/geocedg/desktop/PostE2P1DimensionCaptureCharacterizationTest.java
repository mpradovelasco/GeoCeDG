/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.desktop.export.DrawingScale;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.main.App;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * POST-E2-P1 focal characterization (characterization only, no product change): the
 * creation-time capture of {@code Gap} and {@code Overshoot} by the native dimension
 * tools, through the real tool route, against the analytical reference
 * {@code g = 0.001 / (f(c) * a / b)}, {@code o = 0.002 / (f(c) * a / b)} of the approved
 * contract (native-dimensions section 8.3; unit-system section 12). It separates model
 * space (captured parameters), physical output space (unit and drawing scale; PDF and
 * PGF measured on the produced output) and screen space (view transform only).
 *
 * <p>With the environment variable {@code GEOCEDG_POST_E2_P1_REPORT} set to a file path,
 * the measured rows are also written there as JSON; the assertions never depend on it.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostE2P1DimensionCaptureCharacterizationTest {
	private static final double PAPER_GAP_MM = 1;
	private static final double PAPER_OVERSHOOT_MM = 2;
	private static final double MM_PER_PT = 25.4 / 72;
	/** The LaTeX exporters print twelve significant figures (GeoGebraExport). */
	private static final int PGF_SIGNIFICANT_FIGURES = 12;
	private static final UnitToken[] PHYSICAL = {UnitToken.MM, UnitToken.CM, UnitToken.M};
	private static final DrawingScale[] SCALES = {DrawingScale.ONE_TO_ONE,
		DrawingScale.of(1, 10)};
	/** Experiment A zoom levels, pixels per model unit. */
	private static final double[] ZOOMS = {5, 20, 50};
	/** Experiment B normalized zoom levels, pixels per physical model millimetre. */
	private static final double[] PX_PER_MODEL_MM = {1, 4, 10};
	private static final Map<String, List<Map<String, Object>>> REPORT =
			new LinkedHashMap<>();

	@TempDir
	Path temporary;

	// ------------------------------------------------------------- analytical

	@Test
	void captureFollowsTheApprovedFormulaForEveryUnitAndScale() {
		DrawingScale[] scales = {DrawingScale.ONE_TO_ONE, DrawingScale.of(1, 10),
			DrawingScale.of(1, 2), DrawingScale.of(2, 1), DrawingScale.of(5, 1)};
		for (UnitToken unit : PHYSICAL) {
			for (DrawingScale scale : scales) {
				double[] captured = GeoCeDGDimensionTools.captureMagnitudes(
						UnitState.of(unit, null, null), scale, 50, 50);
				double ratio = (double) scale.getNumerator() / scale.getDenominator();
				double gap = 0.001 / (factor(unit) * ratio);
				double overshoot = 0.002 / (factor(unit) * ratio);
				assertEquals(overshoot, captured[0], overshoot * 1E-12, unit + " " + scale);
				assertEquals(gap, captured[1], gap * 1E-12, unit + " " + scale);
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("unit", unit.token());
				row.put("scale", scale.toString());
				row.put("gapModel", captured[1]);
				row.put("overshootModel", captured[0]);
				row.put("gapAnalytical", gap);
				row.put("overshootAnalytical", overshoot);
				record("analytical", row);
			}
		}
		// the viewport fallback without a physical unit: 4 px and 8 px at creation
		for (double zoom : ZOOMS) {
			double[] captured = GeoCeDGDimensionTools.captureMagnitudes(UnitState.EMPTY,
					DrawingScale.of(1, 10), zoom, zoom);
			assertEquals(8 / zoom, captured[0], 1E-15);
			assertEquals(4 / zoom, captured[1], 1E-15);
		}
	}

	// ------------------------------------------- P1-A same numerical geometry

	@Test
	void experimentASameNumericalGeometry() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		AtomicInteger index = new AtomicInteger();
		UnitToken[] units = {UnitToken.MM, UnitToken.CM, UnitToken.M, null};
		for (UnitToken unit : units) {
			for (DrawingScale scale : SCALES) {
				for (double zoom : ZOOMS) {
					zoom(app, zoom);
					app.getDocumentUnits().replace(unit == null ? UnitState.EMPTY
							: UnitState.of(unit, null, null));
					app.setDrawingScale(scale);
					int n = index.incrementAndGet();
					AlgoNativeDimension algo = aligned(app, n, 40, 0, 30);
					double gap = algo.getGapInput().getDouble();
					double overshoot = algo.getOvershootInput().getDouble();
					Map<String, Object> row = new LinkedHashMap<>();
					row.put("unit", unit == null ? "unspecified" : unit.token());
					row.put("scale", scale.toString());
					row.put("pxPerModelUnit", zoom);
					row.put("measuredLengthModel", 40.0);
					row.put("gapModel", gap);
					row.put("overshootModel", overshoot);
					row.put("gapScreenPx", screenGap(app, algo, n));
					row.put("overshootScreenPx", screenOvershoot(app, algo));
					row.put("gapToLength", gap / 40);
					if (unit == null) {
						assertEquals(4 / zoom, gap, 1E-12, "view fallback");
						assertEquals(8 / zoom, overshoot, 1E-12, "view fallback");
						assertEquals(4, screenGap(app, algo, n), 1E-9);
						assertEquals(8, screenOvershoot(app, algo), 1E-9);
					} else {
						double ratio = (double) scale.getNumerator() / scale.getDenominator();
						assertEquals(0.001 / (factor(unit) * ratio), gap, gap * 1E-12);
						assertEquals(0.002 / (factor(unit) * ratio), overshoot,
								overshoot * 1E-12);
						row.put("measuredLengthPhysicalMm", 40 * factor(unit) * 1000);
						row.put("gapPhysicalModelMm", gap * factor(unit) * 1000);
						row.put("gapPaperMm", gap * factor(unit) * 1000 * ratio);
						row.put("overshootPaperMm", overshoot * factor(unit) * 1000 * ratio);
						row.put("gapToPhysicalLength", gap * factor(unit) * 1000
								/ (40 * factor(unit) * 1000));
						assertEquals(PAPER_GAP_MM, gap * factor(unit) * 1000 * ratio, 1E-9);
					}
					record("experimentA", row);
				}
			}
		}
		// with a physical unit the zoom never enters the capture: same values at 5, 20
		// and 50 px per unit; the screen size of the same numbers is zoom times larger
		for (Map<String, Object> row : REPORT.get("experimentA")) {
			if (!"unspecified".equals(row.get("unit"))) {
				assertEquals((double) row.get("gapModel") * (double) row.get("pxPerModelUnit"),
						(double) row.get("gapScreenPx"), 1E-6);
			}
		}
	}

	// ------------------------------------- P1-B physically equivalent geometry

	@Test
	void experimentBPhysicallyEquivalentGeometry() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		AtomicInteger index = new AtomicInteger();
		for (DrawingScale scale : SCALES) {
			double ratio = (double) scale.getNumerator() / scale.getDenominator();
			for (double pxPerMm : PX_PER_MODEL_MM) {
				double[] gapsPx = new double[PHYSICAL.length];
				double[] paper = new double[PHYSICAL.length];
				for (int u = 0; u < PHYSICAL.length; u++) {
					UnitToken unit = PHYSICAL[u];
					double mmPerUnit = factor(unit) * 1000;
					double length = 40 / mmPerUnit;
					// normalized viewport: the same pixels per physical model millimetre
					zoom(app, pxPerMm * mmPerUnit);
					app.getDocumentUnits().replace(UnitState.of(unit, null, null));
					app.setDrawingScale(scale);
					int n = index.incrementAndGet();
					// the same physical offset on paper: 8 mm
					double offset = 0.008 / ratio / factor(unit);
					for (String tool : new String[] {"aligned", "linear"}) {
						AlgoNativeDimension algo = "aligned".equals(tool)
								? aligned(app, n, length, 0, offset)
								: linear(app, n, length, length / 2, offset);
						double gap = algo.getGapInput().getDouble();
						double overshoot = algo.getOvershootInput().getDouble();
						Map<String, Object> row = new LinkedHashMap<>();
						row.put("tool", tool);
						row.put("unit", unit.token());
						row.put("scale", scale.toString());
						row.put("pxPerPhysicalModelMm", pxPerMm);
						row.put("pxPerModelUnit", pxPerMm * mmPerUnit);
						row.put("measuredLengthModel", length);
						row.put("measuredLengthPhysicalMm", 40.0);
						row.put("offsetModel", algo.getOffsetInput().getDouble());
						row.put("gapModel", gap);
						row.put("overshootModel", overshoot);
						row.put("gapPaperMm", gap * mmPerUnit * ratio);
						row.put("overshootPaperMm", overshoot * mmPerUnit * ratio);
						row.put("gapScreenPx", screenGap(app, algo, n));
						row.put("overshootScreenPx", screenOvershoot(app, algo));
						record("experimentB", row);
						assertEquals(PAPER_GAP_MM, gap * mmPerUnit * ratio, 1E-9);
						assertEquals(PAPER_OVERSHOOT_MM, overshoot * mmPerUnit * ratio, 1E-9);
						if ("aligned".equals(tool)) {
							gapsPx[u] = screenGap(app, algo, n);
							paper[u] = gap * mmPerUnit * ratio;
						}
					}
				}
				// physically equivalent drawings under a normalized viewport: equal paper
				// sizes and equal screen sizes in every unit
				for (int u = 1; u < PHYSICAL.length; u++) {
					assertEquals(paper[0], paper[u], 1E-9);
					assertEquals(gapsPx[0], gapsPx[u], 1E-6);
				}
				assertEquals(pxPerMm / ratio, gapsPx[0], 1E-6,
						"screen gap = px per model mm times the model-space gap in mm");
			}
		}
	}

	// ---------------------------------------------- P1-C parameter persistence

	@Test
	void capturedParametersAreCapturedOnceAndPersist() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		zoom(app, 20);
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, null, null));
		app.setDrawingScale(DrawingScale.ONE_TO_ONE);
		AlgoNativeDimension first = aligned(app, 1, 40, 0, 10);
		final String gapLabel = first.getGapInput().toGeoElement().getLabelSimple();
		final String overshootLabel = first.getOvershootInput().toGeoElement()
				.getLabelSimple();
		for (GeoElement parameter : new GeoElement[] {first.getGapInput().toGeoElement(),
				first.getOvershootInput().toGeoElement()}) {
			assertTrue(parameter.isIndependent(), "a free number, no dependency");
			assertTrue(parameter.isLabelSet());
		}
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("initialGap", first.getGapInput().getDouble());
		row.put("initialOvershoot", first.getOvershootInput().getDouble());
		zoom(app, 3);
		assertCaptured(first, 1, 2, "zoom change");
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, UnitToken.M, null));
		assertCaptured(first, 1, 2, "presentation unit change");
		app.getDocumentUnits().replace(UnitState.of(UnitToken.CM, null, null));
		assertCaptured(first, 1, 2, "construction unit change: no retroactive conversion");
		app.setDrawingScale(DrawingScale.of(1, 10));
		assertCaptured(first, 1, 2, "drawing scale change");
		// a dimension created under the new settings captures its own values
		AlgoNativeDimension second = aligned(app, 2, 40, 0, 10);
		assertCaptured(second, 1, 2, "cm 1:10: 1 mm on paper = 1 cm of model = 1 unit");
		assertEquals(10, second.getGapInput().getDouble() * 10, 1E-12);
		File saved = temporary.resolve("p1.cedg").toFile();
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(saved));
		AppGeoCeDG reopened = G9U1TestApp.create();
		zoom(reopened, 77);
		reopened.setDrawingScale(DrawingScale.of(5, 1));
		assertTrue(reopened.loadFile(saved, false));
		AlgoNativeDimension restored = AlgoNativeDimension.ownerOf(reopened.getKernel()
				.lookupLabel(first.getDimensionLine().getLabelSimple()));
		assertNotNull(restored);
		assertCaptured(restored, 1, 2, "save and reopen under another zoom and scale");
		assertEquals(gapLabel, restored.getGapInput().toGeoElement().getLabelSimple());
		assertEquals(overshootLabel,
				restored.getOvershootInput().toGeoElement().getLabelSimple());
		row.put("afterZoomUnitScaleChangesGap", first.getGapInput().getDouble());
		row.put("afterReopenGap", restored.getGapInput().getDouble());
		row.put("afterReopenOvershoot", restored.getOvershootInput().getDouble());
		row.put("newDimensionCm1to10Gap", second.getGapInput().getDouble());
		record("persistence", row);
	}

	// ------------------------------------------------------------ P1-D exports

	@Test
	void exportsShowOneAndTwoMillimetresOnPaper() throws Exception {
		for (DrawingScale scale : SCALES) {
			double ratio = (double) scale.getNumerator() / scale.getDenominator();
			for (UnitToken unit : PHYSICAL) {
				AppGeoCeDG app = G9U1TestApp.create();
				double mmPerUnit = factor(unit) * 1000;
				double length = 40 / mmPerUnit;
				zoom(app, 4 * mmPerUnit);
				app.getDocumentUnits().replace(UnitState.of(unit, null, null));
				app.setDrawingScale(scale);
				double offset = 0.008 / ratio / factor(unit);
				AlgoNativeDimension algo = aligned(app, 1, length, 0, offset);
				String o = algo.getOffsetInput().toGeoElement().getLabelSimple();
				// visible references: AB itself and a line at the dimension-line height
				G9U1TestApp.eval(app, "ref1=Segment(A1,B1)");
				G9U1TestApp.eval(app, "ref2=Segment(A1+(0," + o + "),B1+(0," + o + "))");
				for (String hidden : new String[] {"A1", "B1"}) {
					GeoElement point = app.getKernel().lookupLabel(hidden);
					point.setEuclidianVisible(false);
					point.updateRepaint();
				}
				double overshoot = algo.getOvershootInput().getDouble();
				double top = algo.getOffsetInput().getDouble() + 2 * overshoot;
				assertTrue(app.getExportAreaSession().defineManual(App.VIEW_EUCLIDIAN,
						-length / 4, length * 1.25, -length / 4, top));
				EuclidianView view = app.getEuclidianView1();
				double[] pdf = measurePdf(pdf(app, view));
				double[] pgf = measurePgf(PreG9BR6PlusCLatexExportTest.generate(
						PreG9BR6PlusCLatexExportTest.exporter(app, "pgf"), view));
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("unit", unit.token());
				row.put("scale", scale.toString());
				row.put("pdfGapMm", pdf[0]);
				row.put("pdfOvershootMm", pdf[1]);
				row.put("pdfUncertaintyMm", pdf[2]);
				row.put("pdfStrokeWidthMm", pdf[3]);
				row.put("pgfGapMm", pgf[0]);
				row.put("pgfOvershootMm", pgf[1]);
				row.put("pgfUncertaintyMm", pgf[2]);
				row.put("pgfStrokeWidthMm", pgf[3]);
				record("export", row);
				String id = unit + " " + scale;
				assertTrue(pdf[2] < 0.01, "PDF resolution supports 0.01 mm: " + id);
				assertTrue(pgf[2] < 0.01, "PGF resolution supports 0.01 mm: " + id);
				assertEquals(PAPER_GAP_MM, pdf[0], 0.01, "PDF gap " + id);
				assertEquals(PAPER_OVERSHOOT_MM, pdf[1], 0.01, "PDF overshoot " + id);
				assertEquals(PAPER_GAP_MM, pgf[0], 0.01, "PGF gap " + id);
				assertEquals(PAPER_OVERSHOOT_MM, pgf[1], 0.01, "PGF overshoot " + id);
			}
		}
	}

	@Test
	void thePdfStrokeWidthIsPhysicalAndThePaperGeometryNeverMoves() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, null, null));
		app.setDrawingScale(DrawingScale.ONE_TO_ONE);
		zoom(app, 4);
		AlgoNativeDimension algo = aligned(app, 1, 40, 0, 8);
		String o = algo.getOffsetInput().toGeoElement().getLabelSimple();
		G9U1TestApp.eval(app, "ref1=Segment(A1,B1)");
		G9U1TestApp.eval(app, "ref2=Segment(A1+(0," + o + "),B1+(0," + o + "))");
		assertTrue(app.getExportAreaSession().defineManual(App.VIEW_EUCLIDIAN, -10, 50, -10,
				14));
		EuclidianView view = app.getEuclidianView1();
		double[] widths = new double[PX_PER_MODEL_MM.length];
		for (int i = 0; i < PX_PER_MODEL_MM.length; i++) {
			zoom(app, PX_PER_MODEL_MM[i]);
			double[] pdf = measurePdf(pdf(app, view));
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("unit", "mm");
			row.put("scale", "1:1");
			row.put("pxPerModelUnitAtExport", PX_PER_MODEL_MM[i]);
			row.put("pdfGapMm", pdf[0]);
			row.put("pdfOvershootMm", pdf[1]);
			row.put("pdfUncertaintyMm", pdf[2]);
			row.put("pdfStrokeWidthMm", pdf[3]);
			record("pdfZoomSweep", row);
			assertEquals(PAPER_GAP_MM, pdf[0], 0.01, "the zoom never moves the paper geometry");
			assertEquals(PAPER_OVERSHOOT_MM, pdf[1], 0.01);
			widths[i] = pdf[3];
		}
		// P1 characterized a stroke of one device unit, inversely proportional to the zoom
		// (OBS-POST-E2-P1-PDF-STROKE-WIDTH-FOLLOWS-EXPORT-ZOOM); POST-E2-P1-R2 makes it
		// 0.4 t pt on a physical PDF whatever the zoom (physical-pdf-style-sizes)
		for (double width : widths) {
			assertEquals(0.4 * 2 * 25.4 / 72, width, 0.4 * 2 * 25.4 / 72 * 1E-3);
		}
	}

	@AfterAll
	static void writeReport() throws Exception {
		String target = System.getenv("GEOCEDG_POST_E2_P1_REPORT");
		if (target == null || target.isEmpty()) {
			return;
		}
		StringBuilder json = new StringBuilder("{\n");
		boolean firstFamily = true;
		for (Map.Entry<String, List<Map<String, Object>>> family : REPORT.entrySet()) {
			json.append(firstFamily ? "" : ",\n").append("  \"").append(family.getKey())
					.append("\": [\n");
			firstFamily = false;
			for (int i = 0; i < family.getValue().size(); i++) {
				json.append("    {");
				boolean firstField = true;
				for (Map.Entry<String, Object> field : family.getValue().get(i).entrySet()) {
					json.append(firstField ? "" : ", ").append('"').append(field.getKey())
							.append("\": ");
					Object value = field.getValue();
					json.append(value instanceof String ? "\"" + value + "\""
							: String.valueOf(value));
					firstField = false;
				}
				json.append(i + 1 < family.getValue().size() ? "},\n" : "}\n");
			}
			json.append("  ]");
		}
		json.append("\n}\n");
		Files.writeString(Paths.get(target), json.toString(), StandardCharsets.UTF_8);
	}

	// ------------------------------------------------------------------ helpers

	private static void record(String family, Map<String, Object> row) {
		synchronized (REPORT) {
			REPORT.computeIfAbsent(family, key -> new ArrayList<>()).add(row);
		}
	}

	private static double factor(UnitToken unit) {
		return UnitState.of(unit, null, null).effectiveConstructionMetresPerUnit();
	}

	private static void assertCaptured(AlgoNativeDimension algo, double gap,
			double overshoot, String why) {
		assertEquals(gap, algo.getGapInput().getDouble(), 1E-12, why);
		assertEquals(overshoot, algo.getOvershootInput().getDouble(), 1E-12, why);
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

	/** Aligned Dimension tool: A, B, placement click at the requested offset. */
	private static AlgoNativeDimension aligned(AppGeoCeDG app, int n, double bx, double by,
			double offset) {
		GeoElement a = G9U1TestApp.eval(app, "A" + n + "=(0,0)");
		GeoElement b = G9U1TestApp.eval(app, "B" + n + "=(" + plain(bx) + "," + plain(by)
				+ ")");
		app.setMode(EuclidianConstants.MODE_ALIGNED_DIMENSION);
		GeoCeDGEuclidianController controller = controller(app);
		click(controller, a);
		click(controller, b);
		EuclidianView view = app.getEuclidianView1();
		controller.mouseLoc = new GPoint(view.toScreenCoordX(bx / 2),
				view.toScreenCoordY(offset));
		click(controller, null);
		return newest(app);
	}

	/** Linear Dimension tool along the x-axis: A, B, x-axis, placement click. */
	private static AlgoNativeDimension linear(AppGeoCeDG app, int n, double bx, double by,
			double offset) {
		GeoElement a = G9U1TestApp.eval(app, "L" + n + "=(0,0)");
		GeoElement b = G9U1TestApp.eval(app, "M" + n + "=(" + plain(bx) + "," + plain(by)
				+ ")");
		app.setMode(EuclidianConstants.MODE_LINEAR_DIMENSION);
		GeoCeDGEuclidianController controller = controller(app);
		click(controller, a);
		click(controller, b);
		click(controller, app.getKernel().getXAxis());
		EuclidianView view = app.getEuclidianView1();
		controller.mouseLoc = new GPoint(view.toScreenCoordX(bx / 2),
				view.toScreenCoordY(offset));
		click(controller, null);
		return newest(app);
	}

	private static String plain(double value) {
		return String.format(Locale.ROOT, "%.17g", value);
	}

	private static AlgoNativeDimension newest(AppGeoCeDG app) {
		AlgoNativeDimension last = null;
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			AlgoNativeDimension owner = AlgoNativeDimension.ownerOf(geo);
			if (owner != null) {
				last = owner;
			}
		}
		assertNotNull(last, "the tool created a dimension");
		return last;
	}

	/** Screen distance between the measured point and the start of extension A. */
	private static double screenGap(AppGeoCeDG app, AlgoNativeDimension algo, int n) {
		EuclidianView view = app.getEuclidianView1();
		GeoSegment extension = algo.getExtensionA();
		double startY = extension.getStartPoint().getInhomY();
		double endY = extension.getEndPoint().getInhomY();
		double nearY = Math.abs(startY) < Math.abs(endY) ? startY : endY;
		return Math.abs(view.toScreenCoordYd(nearY) - view.toScreenCoordYd(0));
	}

	/** Screen distance between the dimension line and the far end of extension A. */
	private static double screenOvershoot(AppGeoCeDG app, AlgoNativeDimension algo) {
		EuclidianView view = app.getEuclidianView1();
		GeoSegment extension = algo.getExtensionA();
		double lineY = algo.getDimensionLine().getStartPoint().getInhomY();
		double startY = extension.getStartPoint().getInhomY();
		double endY = extension.getEndPoint().getInhomY();
		double farY = Math.abs(startY) < Math.abs(endY) ? endY : startY;
		return Math.abs(view.toScreenCoordYd(farY) - view.toScreenCoordYd(lineY));
	}

	private static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
		return (GeoCeDGEuclidianController) app.getEuclidianView1().getEuclidianController();
	}

	private static void click(GeoCeDGEuclidianController controller, GeoElement geo) {
		Hits hits = new Hits();
		if (geo != null) {
			hits.add(geo);
		}
		controller.processMode(hits, false, false, changed -> {
			if (changed) {
				controller.getApplication().storeUndoInfoAndStateForModeStarting(true);
			}
		});
	}

	private static String pdf(AppGeoCeDG app, EuclidianView view) throws Exception {
		File file = Files.createTempFile("post-e2-p1", ".pdf").toFile();
		try {
			app.getPictureExportService().writePDF(view, file, true);
			return new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	/**
	 * Measures the produced PDF: the stroked horizontal references (AB and the line at
	 * the dimension-line height) and the two vertical extension strokes, in device units,
	 * converted to millimetres through the page transform.
	 *
	 * @return {gap mm, overshoot mm, uncertainty mm, extension stroke width mm}
	 */
	static double[] measurePdf(String pdf) throws Exception {
		String content = pdfContent(pdf);
		Matcher scaleMatcher = Pattern.compile(
				"(?m)^([0-9.]+) 0 0 \\1 0 0 cm$").matcher(content);
		double ptPerUnit = Double.NaN;
		while (scaleMatcher.find()) {
			double value = Double.parseDouble(scaleMatcher.group(1));
			if (value != 1) {
				ptPerUnit = value;
			}
		}
		assertFalse(Double.isNaN(ptPerUnit), "device-to-page transform");
		Matcher stroke = Pattern.compile("(?m)^(-?[0-9.]+) (-?[0-9.]+) m\\n(-?[0-9.]+) "
				+ "(-?[0-9.]+) l\\nS$").matcher(content);
		List<String[]> horizontal = new ArrayList<>();
		List<String[]> vertical = new ArrayList<>();
		while (stroke.find()) {
			String[] s = {stroke.group(1), stroke.group(2), stroke.group(3), stroke.group(4),
				width(content, stroke.start())};
			if (s[1].equals(s[3])) {
				horizontal.add(s);
			} else if (s[0].equals(s[2])) {
				vertical.add(s);
			}
		}
		return measure(horizontal, vertical, ptPerUnit * MM_PER_PT, ptPerUnit * MM_PER_PT, 0);
	}

	/**
	 * Measures the produced PGF: the dimension line ({@code <->}), the reference AB and
	 * the vertical extension lines in model coordinates, converted through x unit.
	 *
	 * @return {gap mm, overshoot mm, uncertainty mm, extension stroke width mm}
	 */
	static double[] measurePgf(String pgf) {
		// PRE-G9B-R6-plus-E3-R1 writes the physical unit in TeX points
		Matcher unit = Pattern.compile("x=([0-9.]+)(cm|pt)").matcher(pgf);
		assertTrue(unit.find(), pgf);
		double mmPerUnit = Double.parseDouble(unit.group(1))
				* ("pt".equals(unit.group(2)) ? 25.4 / 72.27 : 10);
		Matcher draw = Pattern.compile("\\\\draw \\[([^\\]]*)\\] \\((-?[0-9.E-]+),"
				+ "(-?[0-9.E-]+)\\)-- \\((-?[0-9.E-]+),(-?[0-9.E-]+)\\);").matcher(pgf);
		List<String[]> horizontal = new ArrayList<>();
		List<String[]> vertical = new ArrayList<>();
		String dimensionLine = null;
		while (draw.find()) {
			Matcher widthMatcher = Pattern.compile("line width=([0-9.]+)pt")
					.matcher(draw.group(1));
			String width = widthMatcher.find()
					? String.valueOf(Double.parseDouble(widthMatcher.group(1)) * MM_PER_PT
							/ mmPerUnit) : "NaN";
			String[] s = {draw.group(2), draw.group(3), draw.group(4), draw.group(5), width};
			if (draw.group(1).contains("<->")) {
				dimensionLine = s[1];
			} else if (s[1].equals(s[3])) {
				horizontal.add(s);
			} else if (s[0].equals(s[2])) {
				vertical.add(s);
			}
		}
		assertNotNull(dimensionLine, pgf);
		// the PGF dimension line is drawn directly; keep only AB as the lower reference
		List<String[]> references = new ArrayList<>();
		for (String[] h : horizontal) {
			if (!h[1].equals(dimensionLine)) {
				references.add(h);
			}
		}
		references.add(new String[] {"0", dimensionLine, "0", dimensionLine, "0"});
		return measure(references, vertical, mmPerUnit, mmPerUnit, PGF_SIGNIFICANT_FIGURES);
	}

	private static double[] measure(List<String[]> horizontal, List<String[]> vertical,
			double mmPerCoordinate, double widthMmPerCoordinate, int figures) {
		assertEquals(2, vertical.size(), "two extension lines");
		String[] extension = vertical.get(0);
		double y1 = Double.parseDouble(extension[1]);
		double y2 = Double.parseDouble(extension[3]);
		double low = Math.min(y1, y2);
		double high = Math.max(y1, y2);
		// the dimension line crosses the extension line; AB lies outside it, at the gap
		String[] ab = null;
		String[] line = null;
		for (String[] h : horizontal) {
			double y = Double.parseDouble(h[1]);
			if (y > low && y < high) {
				line = h;
			} else if (ab == null || distance(h, y1, y2) < distance(ab, y1, y2)) {
				ab = h;
			}
		}
		assertNotNull(ab, "reference AB");
		assertNotNull(line, "reference at the dimension line");
		double yAb = Double.parseDouble(ab[1]);
		String startText = Math.abs(y1 - yAb) < Math.abs(y2 - yAb) ? extension[1]
				: extension[3];
		String endText = startText.equals(extension[1]) && y1 != y2 ? extension[3]
				: extension[1];
		double gap = Math.abs(Double.parseDouble(startText) - yAb) * mmPerCoordinate;
		double overshoot = Math.abs(Double.parseDouble(endText) - Double.parseDouble(line[1]))
				* mmPerCoordinate;
		double uncertainty = Math.max(halfUlp(startText, figures) + halfUlp(ab[1], figures),
				halfUlp(endText, figures) + halfUlp(line[1], figures)) * mmPerCoordinate;
		double width = Double.parseDouble(extension[4]) * widthMmPerCoordinate;
		return new double[] {gap, overshoot, uncertainty, width};
	}

	private static double distance(String[] h, double y1, double y2) {
		double y = Double.parseDouble(h[1]);
		return Math.min(Math.abs(y - y1), Math.abs(y - y2));
	}

	/**
	 * @param number printed number
	 * @param figures significant figures of the writer, or 0 when the printed decimals are
	 *        all significant (fixed-width writer)
	 * @return half a unit in the last significant place
	 */
	private static double halfUlp(String number, int figures) {
		if (figures > 0) {
			double value = Math.abs(Double.parseDouble(number));
			return value == 0 ? 0
					: 0.5 * Math.pow(10, Math.floor(Math.log10(value)) - (figures - 1));
		}
		int dot = number.indexOf('.');
		int exponent = number.indexOf('E');
		int decimals = dot < 0 ? 0 : (exponent < 0 ? number.length() : exponent) - dot - 1;
		double half = 0.5 * Math.pow(10, -decimals);
		return exponent < 0 ? half
				: half * Math.pow(10, Integer.parseInt(number.substring(exponent + 1)));
	}

	private static String width(String content, int before) {
		Matcher w = Pattern.compile("(?m)^([0-9.]+) w$").matcher(content.substring(0, before));
		// without a w operator the PDF default line width, 1 user unit, applies
		String last = "1";
		while (w.find()) {
			last = w.group(1);
		}
		return last;
	}

	/** Decoded page content of a FreeHEP PDF (ASCII85 then Flate). */
	static String pdfContent(String pdf) throws Exception {
		StringBuilder all = new StringBuilder();
		int from = 0;
		while (true) {
			int filter = pdf.indexOf("/Filter [/ASCII85Decode /FlateDecode]", from);
			if (filter < 0) {
				return all.toString();
			}
			int start = pdf.indexOf("stream", filter) + "stream".length();
			while (pdf.charAt(start) == '\r' || pdf.charAt(start) == '\n') {
				start++;
			}
			int end = pdf.indexOf("~>", start);
			ByteArrayOutputStream raw = new ByteArrayOutputStream();
			long value = 0;
			int count = 0;
			for (int i = start; i < end; i++) {
				char c = pdf.charAt(i);
				if (Character.isWhitespace(c)) {
					continue;
				}
				if (c == 'z' && count == 0) {
					raw.write(new byte[4]);
					continue;
				}
				value = value * 85 + (c - 33);
				if (++count == 5) {
					for (int shift = 24; shift >= 0; shift -= 8) {
						raw.write((int) (value >> shift) & 255);
					}
					value = 0;
					count = 0;
				}
			}
			if (count > 0) {
				int kept = count - 1;
				while (count < 5) {
					value = value * 85 + 84;
					count++;
				}
				for (int k = 0, shift = 24; k < kept; k++, shift -= 8) {
					raw.write((int) (value >> shift) & 255);
				}
			}
			Inflater inflater = new Inflater();
			inflater.setInput(raw.toByteArray());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[1 << 16];
			while (!inflater.finished()) {
				int length = inflater.inflate(buffer);
				if (length == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
					break;
				}
				out.write(buffer, 0, length);
			}
			inflater.end();
			all.append(out.toString(StandardCharsets.ISO_8859_1).replace("\r\n", "\n"))
					.append('\n');
			from = end;
		}
	}
}
