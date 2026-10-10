/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GProperty;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.SegmentStyle;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.export.GraphicExportDialog;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * POST-E2-P1-R2 compatibility (physical-pdf-style-sizes section 5.4): every output
 * outside GeoCeDG's physical PDF route is byte-identical to the base build. The
 * digests of a fixed scene are compared with a fixture produced by this same class on
 * the base tree (P_POST_E2_P1_R1 0bb34739); it uses only APIs present there.
 *
 * <p>With {@code GEOCEDG_POST_E2_P1_R2_FINGERPRINT_OUT=<file>} the digests are written
 * there instead of being compared (fixture production on the base tree).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostE2P1R2BaseFingerprintTest {
	private static final String FIXTURE = "post-e2-p1-r2/base-fingerprints.json";
	private static final String PHYSICAL_PGF_E3_R1 = "product.physical.pgf";
	private static final String PHYSICAL_PGF_E3_R1_SHA256 =
			"cc8ff265496f1e79dc1f229db8ff3a878430d4ce25093d57f7f20c02185dd3fa";

	@TempDir
	Path temporary;

	@Test
	void outputsOutsideThePhysicalPdfRouteAreIdenticalToTheBase() throws Exception {
		Map<String, String> digests = new TreeMap<>();
		AppGeoCeDG physical = product(UnitState.of(UnitToken.MM, null, null));
		EuclidianView view = physical.getEuclidianView1();
		BufferedImage png = GBufferedImageDHolder.png(physical, view);
		digests.put("product.physical.png", sha256(pixels(png)));
		ByteArrayOutputStream svg = new ByteArrayOutputStream();
		physical.getPictureExportService().writeSVG(view, svg, true, -1, 1, false);
		digests.put("product.physical.svg", sha256(svg.toString(StandardCharsets.UTF_8)
				.replaceAll("clip[0-9a-f-]+", "clip").getBytes(StandardCharsets.UTF_8)));
		File emf = temporary.resolve("physical.emf").toFile();
		physical.getPictureExportService().writeEMF(view, emf, true, 1, Double.NaN,
				Double.NaN);
		digests.put("product.physical.emf", sha256(Files.readAllBytes(emf.toPath())));
		digests.put("product.physical.pgf", sha256(PreG9BR6PlusCLatexExportTest.generate(
				PreG9BR6PlusCLatexExportTest.exporter(physical, "pgf"), view)
				.getBytes(StandardCharsets.UTF_8)));
		AppGeoCeDG unspecified = product(UnitState.EMPTY);
		File legacy = temporary.resolve("unspecified.pdf").toFile();
		unspecified.getPictureExportService().writePDF(unspecified.getEuclidianView1(),
				legacy, true);
		digests.put("product.unspecified.pdf", sha256(normalizePdf(legacy)));
		AppD classic = classic();
		EuclidianView classicView = classic.getEuclidianView1();
		classicView.setPrintingScale(0.1);
		double scale = classicView.getPrintingScale() * 72 / 2.54 / classicView.getXscale();
		File classicPdf = temporary.resolve("classic.pdf").toFile();
		GraphicExportDialog.exportPDF((EuclidianViewD) classicView, classicPdf, true,
				(int) Math.floor(classicView.getExportWidth() * scale),
				(int) Math.floor(classicView.getExportHeight() * scale), scale);
		digests.put("classic.pdf", sha256(normalizePdf(classicPdf)));
		java.util.concurrent.atomic.AtomicReference<
				org.geogebra.common.export.pstricks.GeoGebraExport> pgf =
				new java.util.concurrent.atomic.AtomicReference<>();
		classic.newGeoGebraToPgf(pgf::set);
		digests.put("classic.pgf", sha256(PreG9BR6PlusCLatexExportTest.generate(pgf.get(),
				classicView).getBytes(StandardCharsets.UTF_8)));

		String json = json(digests);
		String out = System.getenv("GEOCEDG_POST_E2_P1_R2_FINGERPRINT_OUT");
		if (out != null && !out.isEmpty()) {
			Files.writeString(Paths.get(out), json, StandardCharsets.UTF_8);
			return;
		}
		InputStream fixture = PostE2P1R2BaseFingerprintTest.class.getResourceAsStream(FIXTURE);
		assertNotNull(fixture, FIXTURE);
		String expected = new String(fixture.readAllBytes(), StandardCharsets.UTF_8);
		Matcher entry = Pattern.compile("\"([a-z.]+)\": \"([0-9a-f]{64})\"").matcher(expected);
		int count = 0;
		while (entry.find()) {
			String key = entry.group(1);
			// PRE-G9B-R6-plus-E3-R1 composes the physical PGF page (standalone,
			// border 0pt, unit in TeX points, export-area bounding box); the base
			// fixture stays immutable and this key pins PhysicalLatexComposition.pgf
			// applied to the base output (x=0.1cm)
			assertEquals(PHYSICAL_PGF_E3_R1.equals(key) ? PHYSICAL_PGF_E3_R1_SHA256
					: entry.group(2), digests.get(key), key);
			count++;
		}
		assertEquals(digests.size(), count, "every digest has a base value");
	}

	// ------------------------------------------------------------------ scenes

	private static AppGeoCeDG product(UnitState unit) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		app.getDocumentUnits().replace(unit);
		scene(app.getKernel().getAlgebraProcessor()::processAlgebraCommand);
		assertEquals(true, app.getExportAreaSession().defineManual(App.VIEW_EUCLIDIAN, -10,
				50, -10, 20));
		return app;
	}

	private static AppD classic() throws Exception {
		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true));
		sized(classic.getEuclidianView1());
		scene(classic.getKernel().getAlgebraProcessor()::processAlgebraCommand);
		classic.getKernel().getAlgebraProcessor().processAlgebraCommand("Export_1=(-10,-10)",
				false);
		classic.getKernel().getAlgebraProcessor().processAlgebraCommand("Export_2=(50,20)",
				false);
		for (String label : new String[] {"Export_1", "Export_2"}) {
			GeoElement point = classic.getKernel().lookupLabel(label);
			point.setEuclidianVisible(false);
			point.updateRepaint();
		}
		return classic;
	}

	private interface Evaluator {
		GeoElementND[] eval(String command, boolean storeUndo);
	}

	/** Strokes, dashes, end styles, middle decorations, vectors and point markers. */
	private static void scene(Evaluator evaluator) {
		GeoSegment arrowed = (GeoSegment) first(evaluator, "s=Segment((0,0),(40,0))");
		style(arrowed, GColor.newColor(255, 0, 0), 3, EuclidianStyleConstants.LINE_TYPE_FULL);
		arrowed.setStartStyle(SegmentStyle.ARROW_FILLED);
		arrowed.setEndStyle(SegmentStyle.SQUARE);
		arrowed.updateRepaint();
		GeoElement ticked = first(evaluator, "t=Segment((0,5),(20,5))");
		style(ticked, GColor.newColor(0, 128, 0), 2, EuclidianStyleConstants.LINE_TYPE_FULL);
		ticked.setDecorationType(GeoElementND.DECORATION_SEGMENT_TWO_ARROWS);
		ticked.updateRepaint();
		style(first(evaluator, "g=Line((0,15),(40,18))"), GColor.newColor(0, 0, 255), 5,
				EuclidianStyleConstants.LINE_TYPE_DASHED_LONG);
		style(first(evaluator, "c=Circle((30,8),4)"), GColor.newColor(128, 0, 128), 1,
				EuclidianStyleConstants.LINE_TYPE_DOTTED);
		style(first(evaluator, "v=Vector((2,10),(12,12))"), GColor.newColor(0, 128, 128), 3,
				EuclidianStyleConstants.LINE_TYPE_FULL);
		GeoPoint point = (GeoPoint) first(evaluator, "P=(10,8)");
		point.setObjColor(GColor.newColor(255, 128, 0));
		point.setPointStyle(EuclidianStyleConstants.POINT_STYLE_CROSS);
		point.setPointSize(5);
		point.updateVisualStyleRepaint(GProperty.POINT_STYLE);
		GeoPoint dot = (GeoPoint) first(evaluator, "Q=(14,8)");
		dot.setObjColor(GColor.newColor(128, 128, 0));
		dot.setPointSize(4);
		dot.updateVisualStyleRepaint(GProperty.POINT_STYLE);
		GeoElement[] dimension = first(evaluator,
				"d=AlignedDimension((0,-6),(40,-6),-2,2,1)").getParentAlgorithm().getOutput();
		for (int i = 1; i < 4; i++) {
			style(dimension[i], GColor.newColor(255, 0, 255), 2,
					EuclidianStyleConstants.LINE_TYPE_FULL);
		}
	}

	private static GeoElement first(Evaluator evaluator, String command) {
		return evaluator.eval(command, false)[0].toGeoElement();
	}

	private static void style(GeoElement geo, GColor color, int thickness, int type) {
		geo.setObjColor(color);
		geo.setLineThickness(thickness);
		geo.setLineType(type);
		geo.updateVisualStyleRepaint(GProperty.LINE_STYLE);
		geo.updateRepaint();
	}

	private static void sized(EuclidianView view) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(800, 600);
			view.updateSize();
			view.setCoordSystem(100, 500, 8, 8);
			view.setShowAxes(false, false);
			view.showGrid(false);
		});
	}

	// ----------------------------------------------------------------- digests

	/** Isolates the raster route of the B picture fidelity tests. */
	private static final class GBufferedImageDHolder {
		static BufferedImage png(AppGeoCeDG app, EuclidianView view) {
			return org.geogebra.desktop.awt.GBufferedImageD.getAwtBufferedImage(app
					.getPictureExportService().exportImage(view, 2, false, ExportType.PNG));
		}
	}

	private static byte[] pixels(BufferedImage image) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(image.getWidth() >> 8);
		out.write(image.getWidth());
		out.write(image.getHeight() >> 8);
		out.write(image.getHeight());
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int rgb = image.getRGB(x, y);
				out.write(rgb >> 24);
				out.write(rgb >> 16);
				out.write(rgb >> 8);
				out.write(rgb);
			}
		}
		return out.toByteArray();
	}

	private static byte[] normalizePdf(File file) throws Exception {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1)
				.replaceAll("\\(D:[0-9+\\-Z']*\\)", "(D:)")
				.getBytes(StandardCharsets.ISO_8859_1);
	}

	private static String sha256(byte[] bytes) throws Exception {
		StringBuilder hex = new StringBuilder();
		for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) {
			hex.append(String.format("%02x", b));
		}
		return hex.toString();
	}

	private static String json(Map<String, String> digests) {
		StringBuilder json = new StringBuilder("{\n");
		int i = 0;
		for (Map.Entry<String, String> entry : digests.entrySet()) {
			json.append("  \"").append(entry.getKey()).append("\": \"")
					.append(entry.getValue()).append('"')
					.append(++i < digests.size() ? ",\n" : "\n");
		}
		return json.append("}\n").toString();
	}

}
