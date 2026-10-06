/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.define;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.emfBounds;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.emfFrame;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.mediaBox;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.normalizePdf;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.normalizeSvg;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.pdf;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.png;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.sized;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.svg;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.geocedg.common.export.PhysicalExportLimitException;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.desktop.export.DrawingScale;
import org.geocedg.desktop.export.DrawingScaleControl;
import org.geocedg.desktop.export.PictureExportCommandLine;
import org.geocedg.desktop.export.PictureExportService;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.common.util.FileExtensions;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.export.PrintPreviewD;
import org.geogebra.desktop.export.PrintScalePanel;
import org.geogebra.desktop.gui.MyImageD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

/**
 * PRE-G9B-R6-plus-C T-PHYSICAL-PICTURE, T-PRESENTATION-INVARIANCE,
 * T-ZOOM-INVARIANCE, T-UNSPECIFIED, T-EXTREMES, T-API-ROUTES and T-SCALE-UI for the
 * picture and print routes: with a physical construction unit every size is
 * {@code W * fb(c) * 100 * a / b} centimetres (C4); the EMF frame is the requested
 * size to the nearest 0.01 mm (DQ-C9); DPI, zoom, printingScale and
 * presentationUnit never change a physical size (DQ-C8, AQ-U1); explicit API and
 * command-line scales stay device parameters (DQ-C16).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusCPhysicalExportTest {
	private static final double W = 4;
	private static final double H = 3;
	private static final GColor RED = GColor.newColor(255, 0, 0);

	@TempDir
	Path temporary;

	private AppGeoCeDG app(UnitState state, DrawingScale scale) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		GeoElement segment = eval(app, "s=Segment((0,1),(4,1))");
		segment.setObjColor(RED);
		segment.setLineOpacity(255);
		segment.setLineThickness(13);
		segment.updateRepaint();
		define(app, 0, W, 0, H);
		app.getDocumentUnits().replace(state);
		app.setDrawingScale(scale);
		return app;
	}

	private static UnitState mm() {
		return UnitState.of(UnitToken.MM, null, null);
	}

	private static UnitState cm() {
		return UnitState.of(UnitToken.CM, null, null);
	}

	// ------------------------------------------------------ T-PHYSICAL-PICTURE

	@Test
	void everyPictureFormatIsSizedByUnitAndDrawingScale() throws Exception {
		Object[][] cases = {
			{mm(), DrawingScale.ONE_TO_ONE, 0.1},
			{mm(), DrawingScale.of(1, 2), 0.05},
			{cm(), DrawingScale.of(2, 1), 2.0},
			{cm(), DrawingScale.of(2, 4), 0.5},
			// m at 1:100: 1 cm per unit; a large raster would only load the shared heap
			{UnitState.of(UnitToken.M, null, null), DrawingScale.of(1, 100), 1.0},
			{UnitState.of(UnitToken.USM, null, UsmDefinition.of(0.0254, "inch", "in")),
					DrawingScale.ONE_TO_ONE, 2.54}};
		for (Object[] item : cases) {
			AppGeoCeDG app = app((UnitState) item[0], (DrawingScale) item[1]);
			EuclidianView view = app.getEuclidianView1();
			double p = (double) item[2];
			String id = item[0] + " " + item[1];
			assertEquals(p, app.getPhysicalExportScale(), 1E-12, id);
			double[] box = mediaBox(pdf(app, view));
			assertEquals(W * p * 72 / 2.54, box[2] - box[0], 0.001, "PDF width " + id);
			assertEquals(H * p * 72 / 2.54, box[3] - box[1], 0.001, "PDF height " + id);
			double exportScale = dialogScale(app, view, 300);
			BufferedImage raster = png(app, view, exportScale);
			assertEquals(Math.round(W * p * 300 / 2.54), raster.getWidth(), "PNG " + id);
			assertEquals(Math.round(H * p * 300 / 2.54), raster.getHeight(), "PNG " + id);
			String svg = svg(app, view, -1);
			assertEquals(W * p, attribute(svg, "width", "cm"), 1E-9, "SVG " + id);
			assertEquals(H * p, attribute(svg, "height", "cm"), 1E-9, "SVG " + id);
			if (W * p * 1000 < Integer.MAX_VALUE) {
				int[] frame = emfFrame(emf(app, view, exportScale, W * p, H * p));
				assertEquals(Math.round(W * p * 1000), frame[2] - frame[0], "EMF " + id);
				assertEquals(Math.round(H * p * 1000), frame[3] - frame[1], "EMF " + id);
			}
		}
	}

	@Test
	void theEmfFrameIsTheRequestedSizeAtEveryResolutionAndTheDefaultIsUnchanged()
			throws Exception {
		AppGeoCeDG app = app(cm(), DrawingScale.ONE_TO_ONE);
		EuclidianView view = app.getEuclidianView1();
		int[] reference = null;
		for (int dpi : new int[] {72, 300, 600}) {
			byte[] emf = emf(app, view, dialogScale(app, view, dpi), W, H);
			int[] frame = emfFrame(emf);
			assertArrayEquals(new int[] {0, 0, 4000, 3000}, frame,
					"40 x 30 mm at " + dpi + " DPI");
			int[] bounds = emfBounds(emf);
			assertEquals(Math.round(W * dpi / 2.54), bounds[2] - bounds[0] + 0L,
					"the device raster follows the resolution");
			reference = frame;
		}
		assertNotNull(reference);
		int[] fine = emfFrame(emf(app, view, dialogScale(app, view, 300), 4.0003, 2.99996));
		assertArrayEquals(new int[] {0, 0, 4000, 3000}, fine,
				"nearest 0.01 mm: at most 0.005 mm away");
		int[] hostFrame = emfFrame(emf(app, view, dialogScale(app, view, 300),
				Double.NaN, Double.NaN));
		int[] hostBounds = emfBounds(emf(app, view, dialogScale(app, view, 300),
				Double.NaN, Double.NaN));
		assertEquals((int) ((hostBounds[2] - hostBounds[0]) * 100 * 320.0 / 1024),
				hostFrame[2] - hostFrame[0],
				"without the opt-in the FreeHEP reference frame is unchanged");
	}

	@Test
	void printingUsesThePhysicalScaleAndPrintsTheDrawingScale() throws Exception {
		AppGeoCeDG app = app(cm(), DrawingScale.of(1, 2));
		EuclidianViewD view = (EuclidianViewD) app.getEuclidianView1();
		PrintPreviewD preview = Mockito.mock(PrintPreviewD.class);
		Mockito.when(preview.adjustIndex(0)).thenReturn(0);
		app.setPrintPreview(preview);
		try {
			int length = redExtent(printed(view));
			// 4 units at 0.5 cm per unit = 2 cm = 56.7 page units of 1/72 inch
			assertEquals(2 * 72 / 2.54, length, 4, "physical printed length");
			SwingUtilities.invokeAndWait(() -> view.setCoordSystem(50, 200, 120, 120));
			assertEquals(length, redExtent(printed(view)), 1, "zoom never matters");
			view.setPrintScaleString(true);
			assertEquals("Scale 1:2 (construction unit cm)", app
					.getExportScalePresentation().printScaleTitle());
		} finally {
			app.setPrintPreview(null);
		}
	}

	// --------------------------------------- T-ZOOM- and T-PRESENTATION-INVARIANCE

	@Test
	void zoomDpiPrintingScaleAndPresentationUnitNeverChangeAPhysicalSize()
			throws Exception {
		AppGeoCeDG app = app(mm(), DrawingScale.of(1, 2));
		EuclidianView view = app.getEuclidianView1();
		String pdf = normalizePdf(pdf(app, view));
		String svg = normalizeSvg(svg(app, view, -1));
		double[] box = mediaBox(pdf);
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, UnitToken.M, null));
		assertEquals(pdf, normalizePdf(pdf(app, view)), "presentationUnit: PDF bytes");
		assertEquals(svg, normalizeSvg(svg(app, view, -1)), "presentationUnit: SVG");
		view.setPrintingScale(1234);
		assertArrayEquals(box, mediaBox(pdf(app, view)), 0,
				"the view printingScale is never read in physical mode");
		SwingUtilities.invokeAndWait(() -> view.setCoordSystem(10, 280, 13, 13));
		assertArrayEquals(box, mediaBox(pdf(app, view)), 1E-6, "zoom never matters");
		assertEquals(attribute(svg, "width", "cm"),
				attribute(svg(app, view, -1), "width", "cm"), 1E-12);
		BufferedImage at72 = png(app, view, dialogScale(app, view, 72));
		BufferedImage at600 = png(app, view, dialogScale(app, view, 600));
		assertEquals(Math.round(W * 0.05 * 72 / 2.54), at72.getWidth());
		assertEquals(Math.round(W * 0.05 * 600 / 2.54), at600.getWidth(),
				"DPI changes only the number of pixels");
	}

	// ------------------------------------------------------------ T-UNSPECIFIED

	@Test
	void anUnspecifiedDocumentKeepsTheLabelledDeviceScale() throws Exception {
		AppGeoCeDG app = app(UnitState.EMPTY, DrawingScale.of(1, 2));
		EuclidianView view = app.getEuclidianView1();
		assertTrue(Double.isNaN(app.getPhysicalExportScale()));
		assertFalse(app.getExportScalePresentation().isPhysical());
		double printing = view.getPrintingScale();
		double[] box = mediaBox(pdf(app, view));
		assertEquals(W * printing * 72 / 2.54, box[2] - box[0], 0.001,
				"the B device rule; the session drawing scale has no meaning here");
		PrintScalePanel panel = new PrintScalePanel(app, view);
		List<String> items = comboItems(panel);
		assertEquals(List.of("Device scale (non-physical):",
				"Device scale from the screen (non-physical):",
				"Size in pixels (device):"), items);
		assertTrue(texts(panel).stream().anyMatch(text -> text.contains(
				"no construction unit")), texts(panel).toString());
		assertTrue(components(panel, DrawingScaleControl.class).isEmpty(),
				"no a:b control without a construction unit");
		app.getDocumentUnits().replace(cm());
		assertEquals(0.5, app.getPhysicalExportScale(), 0,
				"the session value applies as soon as a unit is declared");
	}

	// ------------------------------------------------------------------ C-UX-1

	@Test
	void theNonPhysicalStatementIsCompactAndKeepsItsFullMeaning() throws Exception {
		String name = "geocedg.exportScale.deviceStatement";
		String full = "This document has no construction unit: the sizes below are a "
				+ "non-physical device scale, not an engineering scale.";
		AppGeoCeDG app = app(UnitState.EMPTY, DrawingScale.ONE_TO_ONE);
		PrintScalePanel panel = new PrintScalePanel(app, app.getEuclidianView1());
		JLabel statement = named(panel, name);
		assertEquals("Non-physical: no construction unit", statement.getText());
		assertEquals(full, statement.getToolTipText(), "the full meaning stays available");
		assertEquals(full, statement.getAccessibleContext().getAccessibleDescription());
		assertTrue(2 * statement.getPreferredSize().width
				< new JLabel(full).getPreferredSize().width, "a compact note");
		JComboBox<?> modes = components(panel, JComboBox.class).get(0);
		assertTrue(statement.getPreferredSize().width <= modes.getPreferredSize().width,
				"never wider than the device-scale selector");
		JComponent latex = app.getExportScalePresentation().createLatexPanel(
				PreG9BR6PlusCLatexExportTest.exporter(app, "pgf"), () -> { });
		JLabel latexStatement = named(latex, name);
		assertEquals("Non-physical: no construction unit", latexStatement.getText());
		assertEquals("This document has no construction unit: the x and y units, width "
				+ "and height are non-physical device parameters.",
				latexStatement.getToolTipText());
		AppGeoCeDG physical = app(cm(), DrawingScale.ONE_TO_ONE);
		assertNull(named(new PrintScalePanel(physical, physical.getEuclidianView1()), name),
				"the physical dialog has no device statement");
	}

	private static JLabel named(Container container, String name) {
		for (JLabel label : components(container, JLabel.class)) {
			if (name.equals(label.getName())) {
				return label;
			}
		}
		return null;
	}

	@Test
	void aPhysicalDocumentOffersOnlyItsDrawingScale() throws Exception {
		AppGeoCeDG app = app(cm(), DrawingScale.ONE_TO_ONE);
		PrintScalePanel panel = new PrintScalePanel(app, app.getEuclidianView1());
		assertTrue(comboItems(panel).isEmpty(), "no device or fixed-size mode");
		List<DrawingScaleControl> controls = components(panel, DrawingScaleControl.class);
		assertEquals(1, controls.size());
		assertTrue(controls.get(0).commit("1:5"));
		assertEquals(DrawingScale.of(1, 5), app.getDrawingScale());
		assertEquals(PrintScalePanel.PrintScaleModes.SIZEINCM, panel.getMode());
		assertTrue(texts(panel).contains("Construction unit: cm"), texts(panel).toString());
	}

	// -------------------------------------------------------------- T-EXTREMES

	@Test
	void unrepresentableSizesFailExplicitlyInsteadOfBeingClamped() throws Exception {
		AppGeoCeDG huge = app(UnitState.of(UnitToken.M, null, null),
				DrawingScale.of(1000000, 1));
		EuclidianView view = huge.getEuclidianView1();
		assertThrows(PhysicalExportLimitException.class, () -> pdf(huge, view),
				"a page beyond the PDF writer");
		assertThrows(PhysicalExportLimitException.class, () -> emf(huge, view, 1E-6,
				W * huge.getPhysicalExportScale(), H * huge.getPhysicalExportScale()),
				"an EMF frame beyond 2^31 - 1 hundredths of a millimetre");
		AppGeoCeDG tiny = app(mm(), DrawingScale.of(1, 1000000000));
		EuclidianView tinyView = tiny.getEuclidianView1();
		double scale = dialogScale(tiny, tinyView, 72);
		assertThrows(PhysicalExportLimitException.class, () -> PictureExportService
				.requirePhysicalRaster(tiny, tinyView, scale), "below one pixel");
		assertThrows(PhysicalExportLimitException.class, () -> emf(tiny, tinyView, scale,
				W * tiny.getPhysicalExportScale(), H * tiny.getPhysicalExportScale()));
		File png = temporary.resolve("tiny.png").toFile();
		assertEquals(PictureExportCommandLine.FAILURE, PictureExportCommandLine.export(
				tiny.getPictureExportRoute(), tinyView, png, 72, null));
		assertFalse(png.exists(), "no empty file");
	}

	// ------------------------------------------------------------- T-API-ROUTES

	@Test
	void routesWithoutAScaleArePhysicalAndExplicitScalesStayDeviceParameters()
			throws Exception {
		AppGeoCeDG app = app(cm(), DrawingScale.of(1, 2));
		EuclidianView view = app.getEuclidianView1();
		File cliPng = temporary.resolve("cli.png").toFile();
		assertEquals(PictureExportCommandLine.SUCCESS, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, cliPng, 254, null));
		// 254 dpi = 100 px/cm; 4 units * 0.5 cm = 2 cm = 200 px
		assertEquals(200, ImageIO.read(cliPng).getWidth(), "--export is physical");
		File cliSvg = temporary.resolve("cli.svg").toFile();
		assertEquals(PictureExportCommandLine.SUCCESS, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, cliSvg, 72, null));
		assertEquals(2.0, attribute(Files.readString(cliSvg.toPath()), "width", "cm"),
				1E-9, "--export SVG is physical");
		File cliEmf = temporary.resolve("cli.emf").toFile();
		assertEquals(PictureExportCommandLine.SUCCESS, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, cliEmf, 300, null));
		assertArrayEquals(new int[] {0, 0, 2000, 1500},
				emfFrame(Files.readAllBytes(cliEmf.toPath())), "--export EMF frame");
		File maxSize = temporary.resolve("max.png").toFile();
		assertEquals(PictureExportCommandLine.SUCCESS, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, maxSize, 72, "100"));
		assertEquals(100, ImageIO.read(maxSize).getWidth(),
				"--maxSize stays a device parameter");
		File api = temporary.resolve("api.png").toFile();
		assertTrue(app.getGgbApi().writePNGtoFile(api.getAbsolutePath(), 2, false, 72,
				false));
		assertEquals(Math.round(W * view.getXscale() * 2), ImageIO.read(api).getWidth(),
				"an explicit API scale is a device scale");
		StringBuilder svg = new StringBuilder();
		app.getGgbApi().exportSVG(temporary.resolve("api.svg").toString(), svg::append);
		assertEquals(2.0, attribute(svg.toString(), "width", "cm"), 1E-9,
				"exportSVG without a scale is physical");
		Path apiPdf = temporary.resolve("api.pdf");
		app.getGgbApi().exportPDF(7, apiPdf.toString(), text -> { }, null, 72);
		double[] box = mediaBox(new String(Files.readAllBytes(apiPdf),
				java.nio.charset.StandardCharsets.ISO_8859_1));
		assertEquals(2 * 72 / 2.54, box[2] - box[0], 0.001,
				"exportPDF: the render scale never becomes a page scale");
		assertEquals(DrawingScale.of(1, 2), app.getDrawingScale(),
				"no route mutates the drawing scale");
		assertEquals(cm(), app.getDocumentUnits().getState());
	}

	// ---------------------------------------------------------------- T-ROUTES

	@Test
	void clipboardSavePreviewAndExportImageKeepTheirRouteContract() throws Exception {
		AppGeoCeDG app = app(cm(), DrawingScale.ONE_TO_ONE);
		EuclidianView view = app.getEuclidianView1();
		List<BufferedImage> copied = new ArrayList<>();
		app.getPictureExportService().setClipboardSink(copied::add);
		File png = temporary.resolve("command.png").toFile();
		for (DrawingScale scale : new DrawingScale[] {DrawingScale.ONE_TO_ONE,
				DrawingScale.of(1, 2)}) {
			app.setDrawingScale(scale);
			app.copyGraphicsViewToClipboard(view);
			BufferedImage clipboard = copied.get(copied.size() - 1);
			// 4 x 3 units at 50 px per unit, at the host clipboard scale 2
			assertEquals(400, clipboard.getWidth(), "the clipboard is a device route " + scale);
			assertEquals(300, clipboard.getHeight());
			BufferedImage preview = (BufferedImage) ((MyImageD) app.getSavePreviewImage(
					FileExtensions.PNG, 512, 512)).getImage();
			assertEquals(512, preview.getWidth(), "the Save preview is a thumbnail (DQ-C10)");
			assertEquals(384, preview.getHeight());
			run(app, "ExportImage(\"type\",\"png\",\"filename\",\"" + slashes(png) + "\")");
			assertEquals(Math.round(300 / 2.54 * W), ImageIO.read(png).getWidth(),
					"ExportImage PNG keeps its own scalecm default, an explicit command "
							+ "parameter (DQ-C16) " + scale);
		}
		File svg = temporary.resolve("command.svg").toFile();
		run(app, "ExportImage(\"type\",\"svg\",\"filename\",\"" + slashes(svg) + "\")");
		assertEquals(W * 0.5, attribute(Files.readString(svg.toPath()), "width", "cm"), 1E-9,
				"ExportImage SVG has no scale: physical");
		File pdf = temporary.resolve("command.pdf").toFile();
		run(app, "ExportImage(\"type\",\"pdf\",\"filename\",\"" + slashes(pdf) + "\")");
		double[] box = mediaBox(new String(Files.readAllBytes(pdf.toPath()),
				java.nio.charset.StandardCharsets.ISO_8859_1));
		assertEquals(W * 0.5 * 72 / 2.54, box[2] - box[0], 0.001,
				"ExportImage PDF: the scalecm render scale never becomes a page scale");
		assertEquals(DrawingScale.of(1, 2), app.getDrawingScale());
	}

	// ------------------------------------------------------------------ helpers

	private static String slashes(File file) {
		return file.getAbsolutePath().replace('\\', '/');
	}

	/** Evaluates a scripting command; a command error fails instead of a dialog. */
	private static void run(AppGeoCeDG app, String command) {
		List<String> errors = new ArrayList<>();
		app.getKernel().getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(command,
				false, new ErrorHandler() {
					@Override
					public void showError(String msg) {
						errors.add(msg);
					}

					@Override
					public void showCommandError(String name, String message) {
						errors.add(message);
					}

					@Override
					public String getCurrentCommand() {
						return null;
					}

					@Override
					public boolean onUndefinedVariables(String string,
							AsyncOperation<String[]> callback) {
						return false;
					}

					@Override
					public void resetError() {
						// nothing to reset
					}
				}, false, null);
		assertEquals(List.of(), errors, command);
	}

	private static double dialogScale(AppGeoCeDG app, EuclidianView view, int dpi) {
		return app.getPhysicalExportScale() * dpi / 2.54 / view.getXscale();
	}

	private static byte[] emf(AppGeoCeDG app, EuclidianView view, double scale,
			double frameWidthCm, double frameHeightCm) throws Exception {
		File file = Files.createTempFile("c-physical", ".emf").toFile();
		try {
			app.getPictureExportService().writeEMF(view, file, true, scale,
					frameWidthCm, frameHeightCm);
			return Files.readAllBytes(file.toPath());
		} finally {
			Files.deleteIfExists(file.toPath());
		}
	}

	private static double attribute(String svg, String name, String unit) {
		Matcher m = Pattern.compile("\\s" + name + "=\"([0-9.Ee+-]+)" + unit + "\"")
				.matcher(svg);
		assertTrue(m.find(), name + " in " + svg.substring(0, Math.min(400, svg.length())));
		return Double.parseDouble(m.group(1));
	}

	private static BufferedImage printed(EuclidianViewD view) {
		PageFormat format = new PageFormat();
		BufferedImage image = new BufferedImage((int) format.getWidth(),
				(int) format.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		assertEquals(Printable.PAGE_EXISTS, view.print(g, format, 0));
		g.dispose();
		return image;
	}

	private static int redExtent(BufferedImage image) {
		int min = Integer.MAX_VALUE;
		int max = -1;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				Color c = new Color(image.getRGB(x, y));
				if (c.getRed() > 200 && c.getGreen() < 160 && c.getBlue() < 160) {
					min = Math.min(min, x);
					max = Math.max(max, x);
				}
			}
		}
		assertTrue(max >= 0, "the segment is printed");
		return max - min;
	}

	private static List<String> comboItems(Container panel) {
		List<String> items = new ArrayList<>();
		for (Component component : panel.getComponents()) {
			if (component instanceof JComboBox) {
				JComboBox<?> combo = (JComboBox<?>) component;
				for (int i = 0; i < combo.getItemCount(); i++) {
					items.add(String.valueOf(combo.getItemAt(i)));
				}
			}
		}
		return items;
	}

	private static List<String> texts(Container container) {
		List<String> texts = new ArrayList<>();
		for (Component component : container.getComponents()) {
			if (component instanceof JLabel) {
				texts.add(((JLabel) component).getText());
			}
			if (component instanceof Container) {
				texts.addAll(texts((Container) component));
			}
		}
		return texts;
	}

	private static <T> List<T> components(Container container, Class<T> type) {
		List<T> found = new ArrayList<>();
		for (Component component : container.getComponents()) {
			if (type.isInstance(component)) {
				found.add(type.cast(component));
			}
			if (component instanceof Container) {
				found.addAll(components((Container) component, type));
			}
		}
		return found;
	}
}
