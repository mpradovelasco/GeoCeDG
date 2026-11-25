/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.importer.dxf.DxfAffineTransform;
import org.geocedg.common.importer.dxf.DxfImportReport;
import org.geocedg.common.importer.dxf.DxfSplineReference;
import org.geocedg.common.importer.dxf.DxfTessellation;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.desktop.awt.GBufferedImageD;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-MAINT-A-DXF0 Desktop: the File action and its EN/ES strings, DX-5 (file,
 * construction XML and Undo history untouched by import, hide, removal, Undo and Redo), DX-6
 * (no hit, no snap, no incidence for a Point-tool click; semantic exports without
 * references), the Q-DXF-3 characterization of picture exports, DX-7 (same tessellation at
 * two zooms and two DPI; live paint only) and DX-12 (session lifecycle R-a: New, Open,
 * Close, Save notice, Undo/Redo).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BMaintADxf0DesktopTest {
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private static final double SCALE = 100;
	private static final String W = "0.7071067811865476";

	/**
	 * Reference presentation colour (150, 60, 170), also when antialiasing blends it with
	 * the white background: red and blue both clearly above green.
	 */
	private static boolean isReference(int rgb) {
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		return r - g > 30 && b - g > 30;
	}

	/** Quarter circle Q and a cubic, written as in the shared fixtures. */
	private static String dxf() {
		StringBuilder text = new StringBuilder();
		String[] lines = {"0", "SECTION", "2", "HEADER", "9", "$ACADVER", "1", "AC1015", "9",
			"$INSUNITS", "70", "4", "0", "ENDSEC", "0", "SECTION", "2", "ENTITIES",
			"0", "SPLINE", "5", "Q1", "8", "REF", "70", "12", "71", "2", "72", "6", "73", "3",
			"40", "0.0", "40", "0.0", "40", "0.0", "40", "1.0", "40", "1.0", "40", "1.0",
			"41", "1.0", "41", W, "41", "1.0",
			"10", "1.0", "20", "0.0", "30", "0.0", "10", "1.0", "20", "1.0", "30", "0.0",
			"10", "0.0", "20", "1.0", "30", "0.0",
			"0", "SPLINE", "5", "C1", "8", "REF", "70", "8", "71", "3", "72", "9", "73", "5",
			"40", "0", "40", "0", "40", "0", "40", "0", "40", "0.5", "40", "1", "40", "1",
			"40", "1", "40", "1",
			"10", "-2", "20", "-1", "10", "-1", "20", "1", "10", "0", "20", "-2", "10", "1",
			"20", "1", "10", "2", "20", "-1",
			"0", "ENDSEC", "0", "EOF"};
		for (String line : lines) {
			text.append(line).append('\n');
		}
		return text.toString();
	}

	private static File dxfFile() throws Exception {
		Path file = Files.createTempFile("a-dxf0-", ".dxf");
		file.toFile().deleteOnExit();
		Files.write(file, dxf().getBytes(StandardCharsets.US_ASCII));
		return file.toFile();
	}

	private static AppGeoCeDG app() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = app.getEuclidianView1();
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(WIDTH, HEIGHT);
			view.updateSize();
			view.setCoordSystem(WIDTH / 2.0, HEIGHT / 2.0, SCALE, SCALE);
			view.setShowAxes(false, false);
			view.showGrid(false);
		});
		return app;
	}

	/** Injects non-interactive ports; returns the texts the presenter received. */
	private static List<String> ports(AppGeoCeDG app, File file, DxfAffineTransform transform,
			AtomicInteger managerOpenings) {
		List<String> shown = new ArrayList<>();
		app.getDxfReferenceImport().setPorts(host -> file, (host, preview) -> transform,
				(host, title, text, details) -> shown.add(title + "\n" + text
						+ (details == null ? "" : "\n" + details)),
				host -> managerOpenings.incrementAndGet());
		return shown;
	}

	private static DxfImportReport importFile(AppGeoCeDG app, File file) {
		ports(app, file, DxfAffineTransform.IDENTITY, new AtomicInteger());
		DxfImportReport report = app.getDxfReferenceImport().importFile(file);
		assertNotNull(report);
		return report;
	}

	private static GeoElement geo(AppGeoCeDG app, String command) {
		try (org.mockito.MockedStatic<javax.swing.JOptionPane> pane =
				org.mockito.Mockito.mockStatic(javax.swing.JOptionPane.class)) {
			GeoElementND[] result = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommand(command, false);
			pane.verifyNoInteractions();
			assertNotNull(result, command);
			return result[0].toGeoElement();
		}
	}

	private static String constructionXml(AppGeoCeDG app) {
		XMLStringBuilder xml = new XMLStringBuilder();
		app.getKernel().getConstruction().getConstructionXML(xml, true);
		return xml.toString();
	}

	private static String sha256(File file) throws Exception {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(Files.readAllBytes(file.toPath())));
	}

	private static BufferedImage paint(EuclidianView view) {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		view.updateAllDrawables(true);
		view.paint(new GGraphics2DD(g));
		g.dispose();
		return image;
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

	/** Reference-coloured pixels within 3 pixels of the screen image of a model point. */
	private static boolean drawnNear(BufferedImage image, EuclidianView view, double x,
			double y) {
		int sx = (int) Math.round(view.toScreenCoordXd(x));
		int sy = (int) Math.round(view.toScreenCoordYd(y));
		for (int dy = -3; dy <= 3; dy++) {
			for (int dx = -3; dx <= 3; dx++) {
				int px = sx + dx;
				int py = sy + dy;
				if (px >= 0 && py >= 0 && px < image.getWidth() && py < image.getHeight()
						&& isReference(image.getRGB(px, py))) {
					return true;
				}
			}
		}
		return false;
	}

	@Test
	void fileActionIsDeclaredBilingualAndImportsThroughTheRegistry() throws Exception {
		AppGeoCeDG app = app();
		GeoCeDGActionRegistry registry = ((GuiManagerGeoCeDG) app.getGuiManager())
				.getActionRegistry();
		assertTrue(registry.ids().contains("import.dxf-reference"));
		assertEquals("Import DXF Reference…",
				registry.get("import.dxf-reference").getValue(Action.NAME));
		assertEquals("Importar referencia DXF…", GeoCeDGProfile.getText(
				"geocedg.action.ImportDxfReference.name", "es"));
		app.getGuiManager().initMenubar();
		Set<String> menu = new HashSet<>();
		for (Component component : ((GeoCeDGMenuBar) app.getGuiManager().getMenuBar())
				.getComponents()) {
			G9U1WorkspaceSurfaceTest.collect(component, menu);
		}
		assertTrue(menu.contains("import.dxf-reference"));
		AtomicInteger managers = new AtomicInteger();
		List<String> shown = ports(app, dxfFile(), DxfAffineTransform.IDENTITY, managers);
		registry.invoke("import.dxf-reference", new ActionEvent(this, 1, "test"));
		assertEquals(2, app.getDxfReferenceSession().size());
		assertEquals(1, managers.get(), "the session manager opens after an import");
		assertEquals(1, shown.size());
		assertTrue(shown.get(0).contains("Admitted references: 2"), shown.get(0));
		assertTrue(shown.get(0).contains("Q1 [REF] RATIONAL"), shown.get(0));
		String inspector = GeoCeDGDxfReferenceText.inspector(app,
				app.getDxfReferenceSession().entries().get(0));
		assertTrue(inspector.startsWith("Imported reference, read-only"), inspector);
		assertTrue(inspector.contains("dxf-spline-reference/v1"));
		app.setLanguage(Locale.forLanguageTag("es"));
		assertTrue(GeoCeDGDxfReferenceText.inspector(app,
				app.getDxfReferenceSession().entries().get(0))
				.startsWith("Referencia importada, solo lectura"));
		app.setLanguage(Locale.ENGLISH);
	}

	@Test
	void dx5FileConstructionXmlAndUndoHistoryAreUntouched() throws Exception {
		AppGeoCeDG app = app();
		UndoManagerD undo = PreG9BR6PlusA1LayerWorkspaceTest.undo(app);
		PreG9BR6PlusA1LayerWorkspaceTest.baseline(undo);
		geo(app, "A=(0,0)");
		PreG9BR6PlusA1LayerWorkspaceTest.awaitStore(app, undo);
		geo(app, "B=(1,0)");
		PreG9BR6PlusA1LayerWorkspaceTest.awaitStore(app, undo);
		File file = dxfFile();
		final String fileDigest = sha256(file);
		final long modified = file.lastModified();
		String xml = constructionXml(app);
		int history = undo.getHistorySize();
		boolean undoPossible = undo.undoPossible();
		final boolean redoPossible = undo.redoPossible();
		importFile(app, file);
		app.getDxfReferenceSession().setVisible(0, false);
		app.getDxfReferenceSession().remove(1);
		assertEquals(1, app.getDxfReferenceSession().size());
		assertEquals(xml, constructionXml(app));
		assertEquals(history, undo.getHistorySize());
		assertEquals(undoPossible, undo.undoPossible());
		assertEquals(redoPossible, undo.redoPossible());
		undo.undo();
		assertEquals(1, app.getDxfReferenceSession().size(), "Undo never touches references");
		assertTrue(app.getKernel().lookupLabel("B") == null);
		undo.redo();
		assertEquals(1, app.getDxfReferenceSession().size(), "Redo never touches references");
		assertEquals(xml, constructionXml(app));
		assertEquals(history, undo.getHistorySize());
		assertEquals(fileDigest, sha256(file), "the DXF file is never modified");
		assertEquals(modified, file.lastModified());
	}

	@Test
	void dx6ReferencesAreNeitherHitNorSnappedNorIncident() throws Exception {
		AppGeoCeDG app = app();
		importFile(app, dxfFile());
		EuclidianView view = app.getEuclidianView1();
		DxfSplineReference quarter = app.getDxfReferenceSession().entries().get(0).reference();
		DxfTessellation render = quarter.tessellation();
		int middle = render.size() / 2;
		double x = render.x(middle);
		double y = render.y(middle);
		GPoint at = new GPoint((int) Math.round(view.toScreenCoordXd(x)),
				(int) Math.round(view.toScreenCoordYd(y)));
		view.setHits(at, PointerEventType.MOUSE);
		assertTrue(view.getHits().isEmpty(), "a reference is never a hit");
		int before = app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
		Cu7M08SDesktopEventsTest.edt(() -> {
			app.setMode(EuclidianConstants.MODE_POINT);
			Cu7M08SDesktopEventsTest.pointer(app, x, y);
			app.setMode(EuclidianConstants.MODE_MOVE);
		});
		List<GeoElement> created = new ArrayList<>(app.getKernel().getConstruction()
				.getGeoSetConstructionOrder());
		assertTrue(created.size() <= before + 1);
		for (GeoElement geo : created) {
			assertTrue(geo instanceof GeoPoint, geo.toString());
			GeoPoint point = (GeoPoint) geo;
			assertTrue(point.isIndependent(), "no point on a reference: " + point);
			assertFalse(point.isPointOnPath(), "no incidence with a reference: " + point);
		}
	}

	private static Map<String, Object> exports(AppGeoCeDG app, EuclidianView view)
			throws Exception {
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("PNG (picture service, also the clipboard image)",
				PreG9BR6PlusBPictureFidelityTest.png(app, view, 1));
		out.put("print (picture service)", PreG9BR6PlusBPictureFidelityTest.printed(app, view));
		out.put("PDF", PreG9BR6PlusBPictureFidelityTest.normalizePdf(
				PreG9BR6PlusBPictureFidelityTest.pdf(app, view)));
		out.put("SVG", PreG9BR6PlusBPictureFidelityTest.normalizeSvg(
				PreG9BR6PlusBPictureFidelityTest.svg(app, view, 0.0264583)));
		out.put("EMF", PreG9BR6PlusBPictureFidelityTest.emf(app, view, 1));
		out.put("host export image", GBufferedImageD.getAwtBufferedImage(
				view.getExportImage(1)));
		BufferedImage printed = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = printed.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, WIDTH, HEIGHT);
		((EuclidianViewD) view).exportPaint(graphics, 1, ExportType.PRINTING);
		graphics.dispose();
		out.put("host print paint", printed);
		for (String dialect : new String[] {"pgf", "pstricks", "asymptote"}) {
			out.put(dialect, PreG9BR6PlusCLatexExportTest.generate(
					PreG9BR6PlusCLatexExportTest.exporter(app, dialect), view));
		}
		GeometryExportService service = new GeometryExportService();
		out.put("DXF", service.exportDxf(service.createModel(new ArrayList<>(app.getKernel()
				.getConstruction().getGeoSetConstructionOrder()),
				SelectionMode.COMPLETE_CONSTRUCTION)));
		return out;
	}

	private static boolean same(Object a, Object b) {
		if (a instanceof BufferedImage image) {
			BufferedImage other = (BufferedImage) b;
			if (image.getWidth() != other.getWidth() || image.getHeight() != other.getHeight()) {
				return false;
			}
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					if (image.getRGB(x, y) != other.getRGB(x, y)) {
						return false;
					}
				}
			}
			return true;
		}
		if (a instanceof byte[] bytes) {
			return java.util.Arrays.equals(bytes, (byte[]) b);
		}
		return a.equals(b);
	}

	/**
	 * DX-6 / SEP-4 (partial): semantic exports (DXF and the three LaTeX dialects) exclude
	 * references. Q-DXF-3, resolved by the author on 2026-11-25 (A-DXF0-R1): the picture
	 * routes of the picture-export service (PNG and clipboard, print, PDF, SVG, EMF) present
	 * visible references of Graphics 1 as the live view does; the host's own exportPaint of the
	 * live view (export image, print paint, used for the document thumbnail) does not.
	 */
	@Test
	void semanticExportsExcludeReferencesAndPictureRoutesPresentThem() throws Exception {
		AppGeoCeDG app = app();
		EuclidianView view = app.getEuclidianView1();
		geo(app, "s=Segment((-3,-2),(3,-2))");
		BufferedImage liveBefore = paint(view);
		Map<String, Object> without = exports(app, view);
		importFile(app, dxfFile());
		Map<String, Object> with = exports(app, view);
		Set<String> pictureRoutes = Set.of("PNG (picture service, also the clipboard image)",
				"print (picture service)", "PDF", "SVG", "EMF");
		for (String route : without.keySet()) {
			boolean included = !same(without.get(route), with.get(route));
			assertEquals(pictureRoutes.contains(route), included, route);
			System.out.println("DXF0-R1-Q-DXF-3 route=" + route + " referencesIncluded="
					+ included);
		}
		BufferedImage liveAfter = paint(view);
		assertEquals(0, referencePixels(liveBefore));
		assertTrue(referencePixels(liveAfter) > 100, "the live view draws the references");
	}

	@Test
	void dx7TessellationIsViewIndependentAndHiddenReferencesAreNotDrawn() throws Exception {
		AppGeoCeDG app = app();
		importFile(app, dxfFile());
		EuclidianView view = app.getEuclidianView1();
		DxfTessellation render = app.getDxfReferenceSession().entries().get(1).reference()
				.tessellation();
		double[] xs = new double[render.size()];
		double[] ys = new double[render.size()];
		for (int i = 0; i < xs.length; i++) {
			xs[i] = render.x(i);
			ys[i] = render.y(i);
		}
		int[] fonts = {12, 24};
		double[] scales = {SCALE, 3 * SCALE};
		for (int font : fonts) {
			for (double scale : scales) {
				SwingUtilities.invokeAndWait(() -> {
					app.setFontSize(font, true);
					view.setCoordSystem(WIDTH / 2.0, HEIGHT / 2.0, scale, scale);
				});
				BufferedImage image = paint(view);
				DxfTessellation now = app.getDxfReferenceSession().entries().get(1).reference()
						.tessellation();
				assertTrue(now == render, "computed once, never per view");
				for (int i = 0; i < xs.length; i++) {
					assertEquals(Double.doubleToRawLongBits(xs[i]),
							Double.doubleToRawLongBits(now.x(i)));
					assertEquals(Double.doubleToRawLongBits(ys[i]),
							Double.doubleToRawLongBits(now.y(i)));
				}
				int middle = render.size() / 2;
				assertTrue(drawnNear(image, view, render.x(middle), render.y(middle)),
						"font " + font + " scale " + scale);
				// A device scale of 2 (HiDPI) maps the same model polyline.
				BufferedImage hiDpi = new BufferedImage(2 * WIDTH, 2 * HEIGHT,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g = hiDpi.createGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, 2 * WIDTH, 2 * HEIGHT);
				g.scale(2, 2);
				view.paint(new GGraphics2DD(g));
				g.dispose();
				int sx = (int) Math.round(2 * view.toScreenCoordXd(render.x(middle)));
				int sy = (int) Math.round(2 * view.toScreenCoordYd(render.y(middle)));
				boolean found = false;
				for (int dy = -5; dy <= 5 && !found; dy++) {
					for (int dx = -5; dx <= 5 && !found; dx++) {
						int px = sx + dx;
						int py = sy + dy;
						found = px >= 0 && py >= 0 && px < hiDpi.getWidth()
								&& py < hiDpi.getHeight()
								&& isReference(hiDpi.getRGB(px, py));
					}
				}
				assertTrue(found, "device scale 2, font " + font + " scale " + scale);
			}
		}
		app.getDxfReferenceSession().setVisible(0, false);
		app.getDxfReferenceSession().setVisible(1, false);
		assertEquals(0, referencePixels(paint(view)), "hidden references are not drawn");
	}

	@Test
	void dx12SessionLifecycle() throws Exception {
		AppGeoCeDG app = app();
		File file = dxfFile();
		geo(app, "A=(0,0)");
		importFile(app, file);
		assertEquals(2, app.getDxfReferenceSession().size());
		// Save: nothing of the references is written; a non-blocking notice says so.
		JLabel notice = app.getStatusBar().getSegment(GeoCeDGStatusBar.PASTE_NOTICE_SEGMENT);
		File saved = Files.createTempFile("a-dxf0-", ".cedg").toFile();
		saved.deleteOnExit();
		assertTrue(app.saveGeoGebraFile(saved));
		assertEquals("Imported DXF references are session-only: they were not saved and must"
				+ " be imported again after reopening.", notice.getText());
		assertTrue(notice.isVisible());
		assertEquals(2, app.getDxfReferenceSession().size(), "Save keeps the session store");
		int entries = 0;
		try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(saved.toPath()))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				String content = new String(zip.readAllBytes(), StandardCharsets.ISO_8859_1);
				assertFalse(content.contains("dxf-spline-reference"), entry.getName());
				assertFalse(content.contains("SPLINE"), entry.getName());
				entries++;
			}
		}
		assertTrue(entries > 0, "the native document is an archive");
		// New clears the store.
		app.setSaved();
		app.fileNew();
		assertTrue(app.getDxfReferenceSession().isEmpty(), "New");
		// Open clears the store, and reopening needs a new import.
		importFile(app, file);
		app.setSaved();
		Cu7M08SDesktopEventsTest.edt(() -> assertTrue(app.loadExistingFile(saved, false)));
		assertTrue(app.getDxfReferenceSession().isEmpty(), "Open");
		assertNotNull(app.getKernel().lookupLabel("A"));
		// Close (window-less host route) clears the store.
		importFile(app, file);
		app.closeWithoutSavePrompt();
		assertTrue(app.getDxfReferenceSession().isEmpty(), "Close");
		// A Save without references shows no session notice.
		importFile(app, file);
		app.getDxfReferenceSession().remove(1);
		app.getDxfReferenceSession().remove(0);
		app.getStatusBar().clearPasteNotice();
		assertTrue(app.saveGeoGebraFile(saved));
		assertFalse(notice.isVisible());
	}

	@Test
	void transformAndUnitsAreExplicitAndRecorded() throws Exception {
		AppGeoCeDG app = app();
		DxfAffineTransform scale = DxfAffineTransform.scale(10);
		List<String> shown = ports(app, dxfFile(), scale, new AtomicInteger());
		DxfImportReport report = app.getDxfReferenceImport().importFile(dxfFile());
		assertNotNull(report);
		DxfSplineReference quarter = report.records().get(0);
		assertArrayEquals(new double[] {10, 10, 0}, quarter.data().xs());
		assertArrayEquals(new double[] {1, 1, 0}, quarter.sourceData().xs());
		assertEquals(scale.toString(), quarter.transform().toString());
		assertTrue(shown.get(0).contains("Transformation: x' = (10.0*x"), shown.get(0));
		assertEquals(GeoCeDGDxfReferenceImport.documentUnit(app), report.file().documentUnit());
		assertEquals("Millimeters", report.file().insUnitsName());
		// Cancelling the transformation imports nothing.
		app.getDxfReferenceImport().setPorts(host -> null, (host, preview) -> null,
				(host, title, text, details) -> { }, host -> { });
		int size = app.getDxfReferenceSession().size();
		assertTrue(app.getDxfReferenceImport().importFile(dxfFile()) == null);
		assertEquals(size, app.getDxfReferenceSession().size());
	}
}
