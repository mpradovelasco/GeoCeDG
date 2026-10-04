/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.util.UserProperties;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.desktop.export.ExportArea;
import org.geocedg.desktop.export.ExportViewport;
import org.geocedg.desktop.export.PictureExportService;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.MyPoint;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLocus;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.main.App;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.desktop.awt.GBufferedImageD;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R6-plus-B focal contract for exact and complete picture output: the
 * offscreen export viewport, exact sizes, the raster rule, the PDF page box,
 * the SVG viewBox, EMF bounds, content outside the live viewport, hidden
 * layers, screen-anchored objects, Locus V2 resolution and the documented
 * legacy-Locus behavior.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR6PlusBPictureFidelityTest {
	private static final int WIDTH = 400;
	private static final int HEIGHT = 300;
	private static final GColor POINT = GColor.newColor(220, 0, 0);
	private static final GColor SEGMENT = GColor.newColor(0, 150, 0);
	private static final GColor RAY = GColor.newColor(0, 0, 210);
	private static final GColor VECTOR = GColor.newColor(150, 0, 150);
	private static final GColor CIRCLE = GColor.newColor(0, 150, 150);
	private static final GColor POLYGON = GColor.newColor(150, 150, 0);
	private static final GColor FUNCTION = GColor.newColor(255, 120, 0);
	private static final GColor LINE = GColor.newColor(90, 40, 0);
	private static final GColor TEXT = GColor.newColor(0, 80, 160);
	private static final GColor LOCUS_V2 = GColor.newColor(160, 0, 80);
	private static final GColor ANCHORED = GColor.newColor(200, 0, 200);

	// ---------------------------------------------------------------- T-VIEWPORT

	@Test
	void theViewportLeavesTheLiveViewAndTheConstructionUntouched() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		buildDisjointScene(app);
		UndoManagerD undo = PreG9BR6PlusA1LayerWorkspaceTest.undo(app);
		PreG9BR6PlusA1LayerWorkspaceTest.baseline(undo);
		String xml = PreG9BR6PlusA1LayerWorkspaceTest.constructionXml(app);
		int drawables = view.getAllDrawableList().size();
		double[] bounds = { view.getXmin(), view.getXmax(), view.getYmin(), view.getYmax() };
		final double[] kernelBounds = kernelBounds(app.getKernel());
		final List<View> views = new ArrayList<>(views(app.getKernel()));
		final Object liveLocusDrawable = view.getDrawableFor(app.getKernel().lookupLabel("LV"));
		final boolean saved = app.isSaved();

		define(app, 20, 23, 10, 13);
		BufferedImage image = png(app, view, 2);
		assertTrue(count(image, POINT) > 0);

		assertEquals(xml, PreG9BR6PlusA1LayerWorkspaceTest.constructionXml(app));
		assertEquals(drawables, view.getAllDrawableList().size());
		assertEquals(bounds[0], view.getXmin());
		assertEquals(bounds[1], view.getXmax());
		assertEquals(bounds[2], view.getYmin());
		assertEquals(bounds[3], view.getYmax());
		assertTrue(java.util.Arrays.equals(kernelBounds, kernelBounds(app.getKernel())),
				"no kernel view bound changes");
		assertEquals(views, views(app.getKernel()), "the viewport is never attached");
		assertSame(view, view.getEuclidianController().getView());
		assertSame(view, app.getActiveEuclidianView());
		assertSame(liveLocusDrawable, view.getDrawableFor(app.getKernel().lookupLabel("LV")));
		assertEquals(saved, app.isSaved());
		assertFalse(undo.undoPossible(), "no undo point");
	}

	@Test
	void theViewportHasItsOwnIdentityAndController() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		buildDisjointScene(app);
		ExportArea area = ExportArea.of(20, 23, 10, 13, view.getViewID(),
				ExportArea.Source.MANUAL);
		ExportViewport[] viewport = new ExportViewport[1];
		SwingUtilities.invokeAndWait(() -> viewport[0] = ExportViewport.create(view, area));
		assertEquals(ExportViewport.VIEW_ID, viewport[0].getViewID());
		assertNotEquals(App.VIEW_EUCLIDIAN, viewport[0].getViewID());
		assertNotEquals(App.VIEW_EUCLIDIAN2, viewport[0].getViewID());
		assertNotEquals(view.getEuclidianController(), viewport[0].getEuclidianController());
		assertEquals(150.0, viewport[0].getExactWidth(), 1e-9);
		assertEquals(150.0, viewport[0].getExactHeight(), 1e-9);
		assertFalse(views(app.getKernel()).contains(viewport[0]));
		assertNotEquals(view.getDrawableFor(app.getKernel().lookupLabel("LV")),
				viewport[0].getDrawableFor(app.getKernel().lookupLabel("LV")),
				"Locus V2 render caches are owned by the viewport's own drawables");
	}

	// ------------------------------------------------------- T-EXACT-SIZE

	@Test
	void exportPointsAreasHaveExactSizesWithoutTheHostBand() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		// inside, larger than and disjoint from the 400x300 viewport
		double[][] areas = { {0, -1, 4, 3}, {-5, -6, 19, 12}, {20, 10, 23, 13} };
		int[][] sizes = { {200, 200}, {1200, 900}, {150, 150} };
		for (int i = 0; i < areas.length; i++) {
			exportPoints(app, areas[i]);
			assertEquals(sizes[i][0], png(app, view, 1).getWidth());
			assertEquals(sizes[i][1], png(app, view, 1).getHeight());
			assertEquals(2 * sizes[i][0], png(app, view, 2).getWidth());
			assertEquals(2 * sizes[i][1], png(app, view, 2).getHeight());
			double[] box = mediaBox(pdf(app, view));
			double points = PictureExportService.pdfPointsPerPixel(view);
			assertEquals(sizes[i][0] * points, box[2], 0.001);
			assertEquals(sizes[i][1] * points, box[3], 0.001);
			double[] viewBox = svgViewBox(svg(app, view, 2.54 / 72));
			assertEquals(sizes[i][0], viewBox[2], 1e-9);
			assertEquals(sizes[i][1], viewBox[3], 1e-9);
			int[] emf = emfBounds(emf(app, view, 1));
			assertEquals(sizes[i][0], emf[2]);
			assertEquals(sizes[i][1], emf[3]);
		}
	}

	// --------------------------------------------------- T-RASTER-BOUNDS

	@Test
	void theRasterGridMapsTheExactWorldBoundsWithoutPaddingOrCrop() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		SwingUtilities.invokeAndWait(() -> view.showGrid(false));
		// 4.003 x 3.0071 units: 200.15 x 150.355 view pixels, not integers
		define(app, 20, 24.003, 10, 13.0071);
		// the list form creates no boundary segments
		GeoElement fill = eval(app,
				"fill=Polygon({(20,10),(24.003,10),(24.003,13.0071),(20,13.0071)})");
		fill.setObjColor(POLYGON);
		fill.setAlphaValue(1);
		fill.setLineThickness(0);
		fill.setLabelVisible(false);
		fill.updateRepaint();
		ExportArea area = app.getExportAreaSession().resolve(view);
		for (double scale : new double[] {1, 2, 3.7}) {
			BufferedImage image = png(app, view, scale);
			assertEquals(Math.round(area.pixelWidth(view.getXscale()) * scale),
					image.getWidth(), "round(w·s)");
			assertEquals(Math.round(area.pixelHeight(view.getYscale()) * scale),
					image.getHeight(), "round(h·s)");
			int notFilled = image.getWidth() * image.getHeight() - count(image, POLYGON, 40);
			assertTrue(notFilled <= 2 * (image.getWidth() + image.getHeight()),
					"the area fills the grid: no padding band (" + notFilled + ")");
			assertEquals(POLYGON.getRed(), (image.getRGB(image.getWidth() / 2, 0) >> 16) & 0xff,
					40);
			assertEquals(POLYGON.getRed(),
					(image.getRGB(image.getWidth() - 1, image.getHeight() / 2) >> 16) & 0xff, 40);
		}
		assertEquals(20, area.getXmin(), 0);
		assertEquals(24.003, area.getXmax(), 0, "world bounds unchanged by the grid");
	}

	// ------------------------------------------------------- T-PDF-EXACT

	@Test
	void thePdfPageBoxIsExactWithoutMarginsOrFitting() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		define(app, 20, 24.003, 10, 13.0071);
		double points = PictureExportService.pdfPointsPerPixel(view);
		ExportArea area = app.getExportAreaSession().resolve(view);
		double requestedWidth = area.pixelWidth(view.getXscale()) * points;
		double requestedHeight = area.pixelHeight(view.getYscale()) * points;
		// the page box is written uncompressed; the content stream is read with the
		// default compression switched off for this export only
		double[] box = mediaBox(pdf(app, view));
		String pdf;
		UserProperties defaults = (UserProperties) PDFGraphics2D.getDefaultProperties();
		boolean compress = defaults.isProperty(PDFGraphics2D.COMPRESS);
		defaults.setProperty(PDFGraphics2D.COMPRESS, false);
		try {
			pdf = pdf(app, view);
		} finally {
			defaults.setProperty(PDFGraphics2D.COMPRESS, compress);
		}
		assertTrue(java.util.Arrays.equals(box, mediaBox(pdf)));
		assertEquals(0, box[0], 0);
		assertEquals(0, box[1], 0);
		assertTrue(Math.abs(box[2] - requestedWidth) <= 0.001, "width " + box[2]);
		assertTrue(Math.abs(box[3] - requestedHeight) <= 0.001, "height " + box[3]);
		Matcher matrix = Pattern.compile("(?m)^1 0 0 -1 0 ([0-9.]+) cm\\r?$").matcher(pdf);
		assertTrue(matrix.find(), "the page transform is a pure flip: no margin, no fit");
		assertEquals(requestedHeight, Double.parseDouble(matrix.group(1)), 1e-8);
		Matcher clip = Pattern.compile("(?m)^0 0 ([0-9.]+) ([0-9.]+) re\\r?$").matcher(pdf);
		assertTrue(clip.find(), "the page clip is the exact page");
		assertEquals(requestedWidth, Double.parseDouble(clip.group(1)), 1e-8);
		assertEquals(requestedHeight, Double.parseDouble(clip.group(2)), 1e-8);
		// the first non-identity scale after the page transform is the viewport's
		Matcher scale = Pattern.compile("(?m)^(?!1 0 0 1 )([0-9.]+) 0 0 ([0-9.]+) 0 0 cm\\r?$")
				.matcher(pdf);
		assertTrue(scale.find(), "the viewport transform");
		assertEquals(points, Double.parseDouble(scale.group(1)), 1e-8, "no additional scaling");
		assertEquals(points, Double.parseDouble(scale.group(2)), 1e-8, "isotropic");
		assertTrue(clip.find(), "the area clip");
		assertEquals(area.pixelWidth(view.getXscale()), Double.parseDouble(clip.group(1)), 1e-8,
				"no crop of the area");
		assertEquals(area.pixelHeight(view.getYscale()), Double.parseDouble(clip.group(2)), 1e-8);
		System.out.println("B-EVIDENCE pdf requested=" + requestedWidth + "x" + requestedHeight
				+ " mediaBox=" + box[2] + "x" + box[3] + " error="
				+ Math.max(Math.abs(box[2] - requestedWidth), Math.abs(box[3] - requestedHeight))
				+ " pageTransform=\"" + matrix.group().trim() + "\" viewportTransform=\""
				+ scale.group().trim() + "\" areaClip=\"" + clip.group().trim() + "\"");
	}

	@Test
	void theFreeHepPdfWriterWithoutOptInKeepsTheHostOutput() throws Exception {
		String pdf = hostPdf();
		assertTrue(pdf.contains("/MediaBox [0.0000 0.0000 595.00 791.00]"));
		assertTrue(pdf.contains("4.9115 0.0000 0.0000 -4.9115 20.000 673.00 cm"),
				"the host keeps its default page, margins and fit-to-page");
		assertEquals(HOST_PDF_SHA256, sha256(normalizePdf(pdf)),
				"byte-identical to the base writer apart from the creation date");
	}

	/**
	 * Normalized SHA-256 of {@link #hostPdf()} written by the base
	 * ({@code f6194f09}) FreeHEP PDF writer, compiled apart from this tree.
	 */
	static final String HOST_PDF_SHA256 =
			"d392d83957e4724d34c81efd18a814ba33761234a5039cc8f8a4a91ea219fbb7";

	/** A host-style FreeHEP PDF, without the GeoCeDG exact-page opt-in. */
	static String hostPdf() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PDFGraphics2D graphics = new PDFGraphics2D(out, new java.awt.Dimension(113, 113));
		UserProperties properties = new UserProperties();
		properties.setProperty(PDFGraphics2D.COMPRESS, false);
		graphics.setProperties(properties);
		graphics.setCreator("probe");
		graphics.startExport();
		graphics.setColor(java.awt.Color.RED);
		graphics.fill(new java.awt.geom.Rectangle2D.Double(10.25, 20.5, 60.125, 30.75));
		graphics.setColor(java.awt.Color.BLUE);
		graphics.draw(new java.awt.geom.Ellipse2D.Double(5.5, 5.5, 100, 50));
		graphics.endExport();
		return out.toString(StandardCharsets.ISO_8859_1);
	}

	static String normalizePdf(String pdf) {
		return pdf.replaceAll("\\(D:[0-9+\\-Z']*\\)", "(D:)");
	}

	// ------------------------------------------------------- T-SVG-EXACT

	@Test
	void theSvgViewBoxIsTheExactAreaWithTheSameAspectAndNoAnisotropy() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		buildDisjointScene(app);
		define(app, 20, 24.003, 10, 13.0071);
		double cmPerPixel = 2.54 / 72;
		String svg = svg(app, view, cmPerPixel);
		double[] viewBox = svgViewBox(svg);
		ExportArea area = app.getExportAreaSession().resolve(view);
		double width = area.pixelWidth(view.getXscale());
		double height = area.pixelHeight(view.getYscale());
		assertEquals(0, viewBox[0], 0);
		assertEquals(0, viewBox[1], 0);
		assertEquals(width, viewBox[2], 1e-9);
		assertEquals(height, viewBox[3], 1e-9);
		double cmWidth = attribute(svg, "width", "cm");
		double cmHeight = attribute(svg, "height", "cm");
		assertEquals(viewBox[2] / viewBox[3], cmWidth / cmHeight, 1e-12,
				"viewBox and physical size describe the same rectangle");
		assertFalse(svg.contains("preserveAspectRatio=\"none\""));
		Matcher matrix = Pattern.compile("matrix\\(([^)]*)\\)").matcher(svg);
		while (matrix.find()) {
			String[] m = matrix.group(1).trim().split("[ ,]+");
			assertEquals(Math.abs(Double.parseDouble(m[0])), Math.abs(Double.parseDouble(m[3])),
					1e-9, "isotropic drawing: circles stay circles");
		}
	}

	// ------------------------------------------------- T-EMF-QUANTIZATION

	@Test
	void emfBoundsAreTheNearestDeviceUnitsOfTheExactArea() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		define(app, 20, 24.003, 10, 13.0071);
		ExportArea area = app.getExportAreaSession().resolve(view);
		for (double scale : new double[] {1, 2.5}) {
			double requestedWidth = area.pixelWidth(view.getXscale()) * scale;
			double requestedHeight = area.pixelHeight(view.getYscale()) * scale;
			byte[] emf = emf(app, view, scale);
			int[] bounds = emfBounds(emf);
			int[] frame = emfFrame(emf);
			assertEquals(0, bounds[0]);
			assertEquals(0, bounds[1]);
			assertTrue(Math.abs(bounds[2] - requestedWidth) <= 0.5, "rclBounds width");
			assertTrue(Math.abs(bounds[3] - requestedHeight) <= 0.5, "rclBounds height");
			// rclFrame (0.01 mm) is outside the physical contract of B
			// (OBS-B-EMF-RCLFRAME-PHYSICAL-QUANTIZATION); the evidence records it
			System.out.println("B-EVIDENCE emf scale=" + scale + " requestedBounds="
					+ requestedWidth + "x" + requestedHeight + " emittedBounds=" + bounds[2]
					+ "x" + bounds[3] + " requestedFrame=" + requestedWidth * 31.25 + "x"
					+ requestedHeight * 31.25 + " emittedFrame=" + frame[2] + "x" + frame[3]);
		}
	}

	// ------------------------------------------------------------ T-CONTENT

	@Test
	void everyDrawableKindIsCompleteInADisjointArea() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		buildDisjointScene(app);
		BufferedImage live = png(app, view, 1);
		assertEquals(0, count(live, POINT), "the visible viewport does not contain them");
		define(app, 20, 23, 10, 13);
		BufferedImage image = png(app, view, 2);
		for (GColor color : new GColor[] {POINT, SEGMENT, RAY, VECTOR, CIRCLE, POLYGON,
				FUNCTION, LINE, TEXT, LOCUS_V2}) {
			assertTrue(count(image, color) > 0, "missing " + color);
		}
		assertTrue(count(image, view.getGridColor(), 30) > 0, "grid of the area");
		String svg = svg(app, view, -1);
		for (GColor color : new GColor[] {POINT, SEGMENT, RAY, VECTOR, CIRCLE, POLYGON,
				FUNCTION, LINE, TEXT, LOCUS_V2}) {
			assertTrue(svg.contains(String.format("#%02x%02x%02x", color.getRed(),
					color.getGreen(), color.getBlue())), "SVG holds " + color);
		}
	}

	@Test
	void axesAndGridAreCompleteInALargerArea() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		SwingUtilities.invokeAndWait(() -> {
			view.setShowAxis(0, true, false);
			view.setShowAxis(1, true, false);
			view.showGrid(true);
		});
		define(app, -30, 30, -20, 20);
		BufferedImage image = png(app, view, 1);
		assertEquals(3000, image.getWidth());
		assertTrue(countIn(image, view.getAxesColor(), 30, 0, 0, 200, image.getHeight()) > 0
				|| countIn(image, view.getGridColor(), 30, 0, 0, 200, image.getHeight()) > 0,
				"grid/axes left of the live viewport");
		assertTrue(countIn(image, view.getGridColor(), 30, 2800, 0, 3000, image.getHeight()) > 0,
				"grid right of the live viewport");
		assertTrue(countIn(image, view.getAxesColor(), 30, 0, 995, 3000, 1005) > 100,
				"x-axis across the whole area");
	}

	// --------------------------------------------------- T-HIDDEN-LAYERS

	@Test
	void everyPictureFormatOmitsHiddenLayers() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		app.getLayerWorkspace().setWorkingLayer(3);
		GeoElement hidden = eval(app, "H=Segment((21,11),(22,12))");
		app.getLayerWorkspace().setWorkingLayer(0);
		color(hidden, POINT).setLineThickness(9);
		define(app, 20, 23, 10, 13);
		assertTrue(count(png(app, view, 1), POINT) > 0);
		assertTrue(svg(app, view, -1).contains("layer3"));
		assertTrue(svg(app, view, -1).contains("#dc0000"));
		app.getLayerWorkspace().setLayerHidden(3, true);
		assertEquals(0, count(png(app, view, 1), POINT), "PNG");
		String svg = svg(app, view, -1);
		assertFalse(svg.contains("layer3"), "SVG omits the hidden layer");
		assertFalse(svg.contains("#dc0000"), "SVG omits the hidden object");
		assertEquals(0, count(printed(app, view), POINT), "print");
		app.getLayerWorkspace().setLayerHidden(3, false);
		assertTrue(count(png(app, view, 1), POINT) > 0, "shown again");
		assertTrue(hidden.isEuclidianVisible(), "object visibility untouched");
	}

	// -------------------------------------------------- T-SCREEN-ANCHORED

	@Test
	void screenAnchoredObjectsStayAnchoredToTheExportCanvas() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		GeoText text = (GeoText) eval(app, "anchored=Text(\"WWWW\")");
		text.setAbsoluteScreenLocActive(true);
		text.setAbsoluteScreenLoc(10, 20);
		text.setFontSizeMultiplier(3);
		color(text, ANCHORED);
		define(app, 20, 23, 10, 13);
		BufferedImage disjoint = png(app, view, 1);
		assertTrue(countIn(disjoint, ANCHORED, 12, 0, 0, 120, 60) > 0,
				"at its presentation position on the canvas, never converted to world");
		define(app, -1, 3, 1, 4);
		BufferedImage inside = png(app, view, 1);
		assertTrue(countIn(inside, ANCHORED, 12, 0, 0, 120, 60) > 0);
		// legacy characterization: the host Export_1/Export_2 export translates the
		// canvas by the area corner, so the same text leaves the picture there
		app.clearExportArea();
		exportPoints(app, new double[] {0, -1, 4, 3});
		BufferedImage host = GBufferedImageD.getAwtBufferedImage(view.getExportImage(1));
		assertEquals(0, countIn(host, ANCHORED, 12, 0, 0, 60, 20),
				"base behavior differs: documented in the candidate report");
	}

	// ------------------------------------------------------ Locus resolution

	@Test
	void locusV2IsTessellatedAtTheExportResolution() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		double live = LocusRenderPolicy2D.from(view).getVisualTolerancePixels();
		app.setExporting(ExportType.PNG, 4);
		try {
			assertEquals(live / 4, LocusRenderPolicy2D.from(view).getVisualTolerancePixels(),
					1e-12);
		} finally {
			app.setExporting(ExportType.NONE, 1);
		}
		app.setExporting(ExportType.PNG, 0.25);
		try {
			assertEquals(live, LocusRenderPolicy2D.from(view).getVisualTolerancePixels(),
					1e-12, "a thumbnail never coarsens the policy");
		} finally {
			app.setExporting(ExportType.NONE, 1);
		}
	}

	@Test
	void legacyLocusIsRenderedOnlyFromItsExistingSamples() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		eval(app, "PL=Point(xAxis)");
		eval(app, "QL=(x(PL), 11+0.2*sin(5*x(PL)))");
		GeoLocus locus = (GeoLocus) eval(app, "LL=Locus(QL,PL)");
		String xml = PreG9BR6PlusA1LayerWorkspaceTest.constructionXml(app);
		List<MyPoint> before = copy(locus);
		double[] kernelBounds = kernelBounds(app.getKernel());
		define(app, 20, 23, 10, 13);
		png(app, view, 2);
		svg(app, view, -1);
		assertEquals(before.size(), locus.getPoints().size(),
				"OBS-B-LEGACY-LOCUS-OFFSCREEN-COVERAGE: no resampling");
		for (int i = 0; i < before.size(); i++) {
			assertEquals(before.get(i).getX(), locus.getPoints().get(i).getX(), 0);
			assertEquals(before.get(i).getY(), locus.getPoints().get(i).getY(), 0);
		}
		assertTrue(java.util.Arrays.equals(kernelBounds, kernelBounds(app.getKernel())));
		assertEquals(xml, PreG9BR6PlusA1LayerWorkspaceTest.constructionXml(app));
		long inside = before.stream().filter(p -> p.getX() >= 20 && p.getX() <= 23).count();
		assertTrue(inside < 5, "the existing window holds few samples there: " + inside);
	}

	// ------------------------------------------------------- T-DETERMINISM

	@Test
	void picturesAreDeterministic() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		buildDisjointScene(app);
		define(app, 20, 23, 10, 13);
		BufferedImage first = png(app, view, 2);
		BufferedImage second = png(app, view, 2);
		for (int x = 0; x < first.getWidth(); x++) {
			for (int y = 0; y < first.getHeight(); y++) {
				assertEquals(first.getRGB(x, y), second.getRGB(x, y));
			}
		}
		assertEquals(normalizeSvg(svg(app, view, -1)), normalizeSvg(svg(app, view, -1)));
	}

	// ------------------------------------------------------------- helpers

	static EuclidianView sized(EuclidianView view) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(WIDTH, HEIGHT);
			view.updateSize();
			view.setCoordSystem(50, 200, 50, 50);
			view.showGrid(true);
		});
		return view;
	}

	static void define(AppGeoCeDG app, double x1, double x2, double y1, double y2) {
		assertTrue(app.getExportAreaSession().defineManual(App.VIEW_EUCLIDIAN, x1, x2, y1, y2));
	}

	static void exportPoints(AppGeoCeDG app, double[] corners) {
		eval(app, "Export_1=(" + corners[0] + "," + corners[1] + ")");
		eval(app, "Export_2=(" + corners[2] + "," + corners[3] + ")");
		for (String label : new String[] {"Export_1", "Export_2"}) {
			GeoElement point = app.getKernel().lookupLabel(label);
			point.setEuclidianVisible(false);
			point.updateRepaint();
		}
	}

	private static void buildDisjointScene(AppGeoCeDG app) {
		GeoPoint point = (GeoPoint) color(eval(app, "A=(21,11)"), POINT);
		point.setPointSize(7);
		point.updateRepaint();
		color(eval(app, "s=Segment((20.2,10.2),(22.8,10.6))"), SEGMENT).setLineThickness(9);
		color(eval(app, "r=Ray((20.2,12.8),(20.6,12.4))"), RAY).setLineThickness(9);
		color(eval(app, "u=Vector((21.5,11.5),(22.5,12))"), VECTOR).setLineThickness(9);
		color(eval(app, "c=Circle((22.3,12.3),0.4)"), CIRCLE).setLineThickness(9);
		GeoElement polygon = color(eval(app, "poly=Polygon({(20.4,11.6),(21,11.6),(20.7,12.1)})"),
				POLYGON);
		polygon.setAlphaValue(1);
		polygon.updateRepaint();
		color(eval(app, "f(x)=11.6+0.2*sin(3*x)"), FUNCTION).setLineThickness(9);
		color(eval(app, "g=Line((0,12.95),(1,12.95))"), LINE).setLineThickness(9);
		GeoText text = (GeoText) eval(app, "t=Text(\"WWW\",(22.2,10.3))");
		text.setFontSizeMultiplier(3);
		color(text, TEXT);
		eval(app, "sv=20");
		eval(app, "Qv=(sv,10.8+0.1*sin(4*sv))");
		eval(app, "Dv={false,{20,23,true,true}}");
		color(eval(app, "LV=LocusV2(Qv,sv,Dv)"), LOCUS_V2).setLineThickness(9);
		for (String label : new String[] {"Qv", "sv", "Dv"}) {
			GeoElement geo = app.getKernel().lookupLabel(label);
			geo.setEuclidianVisible(false);
			geo.updateRepaint();
		}
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (geo.isGeoPoint() && !"A".equals(geo.getLabelSimple())) {
				geo.setEuclidianVisible(false);
				geo.updateRepaint();
			}
		}
	}

	static GeoElement color(GeoElement geo, GColor color) {
		geo.setObjColor(color);
		geo.setLineOpacity(255);
		geo.setLabelVisible(false);
		geo.updateRepaint();
		return geo;
	}

	static BufferedImage png(AppGeoCeDG app, EuclidianView view, double scale) {
		return GBufferedImageD.getAwtBufferedImage(app.getPictureExportService()
				.exportImage(view, scale, false, ExportType.PNG));
	}

	static BufferedImage printed(AppGeoCeDG app, EuclidianView view) {
		ExportArea area = app.getExportAreaSession().resolve(view);
		BufferedImage image = new BufferedImage(
				(int) Math.ceil(area.pixelWidth(view.getXscale())),
				(int) Math.ceil(area.pixelHeight(view.getYscale())), BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D graphics = image.createGraphics();
		graphics.setColor(java.awt.Color.WHITE);
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		app.getPictureExportService().exportPaint(view, new GGraphics2DD(graphics), 1, false,
				ExportType.PRINTING);
		graphics.dispose();
		return image;
	}

	static String pdf(AppGeoCeDG app, EuclidianView view) throws Exception {
		File file = Files.createTempFile("b-fidelity", ".pdf").toFile();
		try {
			app.getPictureExportService().writePDF(view, file, true);
			return new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	static String svg(AppGeoCeDG app, EuclidianView view, double cmPerPixel)
			throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		app.getPictureExportService().writeSVG(view, out, false, cmPerPixel, 1, false);
		return out.toString(StandardCharsets.UTF_8);
	}

	static byte[] emf(AppGeoCeDG app, EuclidianView view, double scale) throws Exception {
		File file = Files.createTempFile("b-fidelity", ".emf").toFile();
		try {
			// PRE-G9B-R6-plus-C: no exact frame requested, the B device bounds
			app.getPictureExportService().writeEMF(view, file, true, scale,
					Double.NaN, Double.NaN);
			return Files.readAllBytes(file.toPath());
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	static double[] mediaBox(String pdf) {
		Matcher m = Pattern.compile("/MediaBox \\[([^\\]]*)\\]").matcher(pdf);
		assertTrue(m.find(), "MediaBox");
		String[] numbers = m.group(1).trim().split("\\s+");
		double[] box = new double[4];
		for (int i = 0; i < 4; i++) {
			box[i] = Double.parseDouble(numbers[i]);
		}
		return box;
	}

	static double[] svgViewBox(String svg) {
		Matcher m = Pattern.compile("viewBox=\"([^\"]*)\"").matcher(svg);
		assertTrue(m.find(), "viewBox");
		String[] numbers = m.group(1).trim().split("\\s+");
		double[] box = new double[4];
		for (int i = 0; i < 4; i++) {
			box[i] = Double.parseDouble(numbers[i]);
		}
		return box;
	}

	private static double attribute(String svg, String name, String unit) {
		Matcher m = Pattern.compile("\\s" + name + "=\"([0-9.Ee+-]+)" + unit + "\"")
				.matcher(svg);
		assertTrue(m.find(), name);
		return Double.parseDouble(m.group(1));
	}

	static int[] emfBounds(byte[] emf) {
		ByteBuffer b = ByteBuffer.wrap(emf).order(ByteOrder.LITTLE_ENDIAN);
		return new int[] {b.getInt(8), b.getInt(12), b.getInt(16), b.getInt(20)};
	}

	static int[] emfFrame(byte[] emf) {
		ByteBuffer b = ByteBuffer.wrap(emf).order(ByteOrder.LITTLE_ENDIAN);
		return new int[] {b.getInt(24), b.getInt(28), b.getInt(32), b.getInt(36)};
	}

	static String normalizeSvg(String svg) {
		return svg.replaceAll("clip[0-9a-f-]+", "clip");
	}

	static int count(BufferedImage image, GColor color) {
		return count(image, color, 12);
	}

	static int count(BufferedImage image, GColor color, int tolerance) {
		return countIn(image, color, tolerance, 0, 0, image.getWidth(), image.getHeight());
	}

	static int countIn(BufferedImage image, GColor color, int tolerance, int x0, int y0,
			int x1, int y1) {
		int n = 0;
		for (int x = Math.max(0, x0); x < Math.min(x1, image.getWidth()); x++) {
			for (int y = Math.max(0, y0); y < Math.min(y1, image.getHeight()); y++) {
				int rgb = image.getRGB(x, y);
				if (Math.abs(((rgb >> 16) & 0xff) - color.getRed()) <= tolerance
						&& Math.abs(((rgb >> 8) & 0xff) - color.getGreen()) <= tolerance
						&& Math.abs((rgb & 0xff) - color.getBlue()) <= tolerance) {
					n++;
				}
			}
		}
		return n;
	}

	private static List<MyPoint> copy(GeoLocus locus) {
		List<MyPoint> points = new ArrayList<>();
		for (MyPoint point : locus.getPoints()) {
			points.add(new MyPoint(point.getX(), point.getY(), point.getSegmentType()));
		}
		return points;
	}

	static double[] kernelBounds(Kernel kernel) {
		return new double[] {kernel.getXmin(0), kernel.getXmax(0), kernel.getYmin(0),
				kernel.getYmax(0), kernel.getXscale(0), kernel.getYscale(0)};
	}

	@SuppressWarnings("unchecked")
	static List<View> views(Kernel kernel) throws Exception {
		Field field = Kernel.class.getDeclaredField("views");
		field.setAccessible(true);
		return (List<View>) field.get(kernel);
	}

	static String sha256(String text) throws Exception {
		java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
		StringBuilder hex = new StringBuilder();
		for (byte b : digest.digest(text.getBytes(StandardCharsets.ISO_8859_1))) {
			hex.append(String.format("%02x", b));
		}
		return hex.toString();
	}

	static void requireNotNull(Object value) {
		assertNotNull(value);
	}
}
