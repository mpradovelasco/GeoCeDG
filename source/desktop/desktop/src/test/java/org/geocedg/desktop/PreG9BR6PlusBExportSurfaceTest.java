/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.count;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.define;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.exportPoints;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.mediaBox;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.png;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.sized;
import static org.geocedg.desktop.PreG9BR6PlusBPictureFidelityTest.svgViewBox;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.export.PictureExportPolicy;
import org.geocedg.desktop.export.ExportArea;
import org.geocedg.desktop.export.ExportAreaSession;
import org.geocedg.desktop.export.PictureExportCommandLine;
import org.geocedg.desktop.export.PictureExportService;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.export.pstricks.ExportFrameMinimal;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.export.pstricks.GeoGebraToAsymptote;
import org.geogebra.common.export.pstricks.GeoGebraToPgf;
import org.geogebra.common.export.pstricks.GeoGebraToPstricks;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.App;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.common.util.FileExtensions;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.GBufferedImageD;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.export.GraphicExportDialog;
import org.geogebra.desktop.export.PrintPreviewD;
import org.geogebra.desktop.export.pstricks.ExportGraphicsFactoryD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.MyImageD;
import org.geogebra.desktop.gui.dialog.DialogManagerD;
import org.geogebra.desktop.gui.util.GeoGebraFileChooser;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.AppD.UpstreamExportEntry;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.editor.share.util.KeyCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * PRE-G9B-R6-plus-B focal contract for the session ExportArea, its producers
 * and precedence, the File/Export surface and the v1 fallback, the shortcuts,
 * the Save preview, and every route that reaches the picture service (print,
 * clipboard, command line, ExportImage and the graphics APIs), with the
 * upstream defaults of the new seams.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR6PlusBExportSurfaceTest {
	private static final GColor RED = GColor.newColor(220, 0, 0);
	private static final GColor MARK = GColor.newColor(0, 0, 210);
	private static final GColor OVERLAY = GColor.newColor(230, 120, 0);

	// ---------------------------------------------------------- T-PRECEDENCE

	@Test
	void anExplicitProducerBeatsExportPointsWhichBeatTheViewport() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		ExportAreaSession session = app.getExportAreaSession();
		assertArea(session.resolve(view), ExportArea.Source.VISIBLE_VIEWPORT, -1, 7, -2, 4);

		exportPoints(app, new double[] {4, 3, 0, -1});
		assertArea(session.resolve(view), ExportArea.Source.EXPORT_POINTS_AUTOMATIC, 0, 4, -1, 3);
		assertTrue(session.defineManual(App.VIEW_EUCLIDIAN, 23, 20, 13, 10), "normalized");
		assertArea(session.resolve(view), ExportArea.Source.MANUAL, 20, 23, 10, 13);
		assertArea(session.resolve(app.getEuclidianView2(1)),
				ExportArea.Source.EXPORT_POINTS_AUTOMATIC, 0, 4, -1, 3);
		assertTrue(session.useExportPoints(App.VIEW_EUCLIDIAN));
		assertArea(session.resolve(view), ExportArea.Source.EXPORT_POINTS_EXPLICIT, 0, 4, -1, 3);
		assertNull(session.getManualArea(), "MANUAL is kept only while it is active");

		// a selection rectangle, also a leftover one, is never authority
		view.setSelectionRectangle(AwtFactory.getPrototype().newRectangle(10, 10, 30, 30));
		assertArea(session.resolve(view), ExportArea.Source.EXPORT_POINTS_EXPLICIT, 0, 4, -1, 3);
		assertEquals(200, png(app, view, 1).getWidth());
		assertEquals(200, app.getExportFrameWidth(view), 1e-9, "dialog and ExportImage sizes");
		session.clear();
		app.getKernel().lookupLabel("Export_1").remove();
		assertArea(session.resolve(view), ExportArea.Source.VISIBLE_VIEWPORT, -1, 7, -2, 4);
	}

	@Test
	void everyRouteRendersTheSameEffectiveArea(@TempDir Path directory) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		mark(app);
		define(app, 20, 23, 10, 13);

		assertSize(150, 150, png(app, view, 1), "service");
		File dialog = directory.resolve("dialog.png").toFile();
		GraphicExportDialog.exportPNG((EuclidianViewD) view, dialog, false, 72, 1, false,
				ExportType.PNG);
		assertSize(150, 150, ImageIO.read(dialog), "Picture dialog PNG");
		File api = directory.resolve("api.png").toFile();
		assertTrue(app.getGgbApi().writePNGtoFile(api.getPath(), 1, false, 72, false));
		assertSize(150, 150, ImageIO.read(api), "writePNGtoFile");
		String base64 = app.getGgbApi().getPNGBase64(1, false, 72, false, false);
		assertSize(150, 150, ImageIO.read(new ByteArrayInputStream(
				Base64.getDecoder().decode(base64))), "getPNGBase64 and ExportImage");
		for (BufferedImage image : new BufferedImage[] {ImageIO.read(dialog),
				ImageIO.read(api)}) {
			assertTrue(count(image, MARK) > 0, "the area content");
		}

		File command = directory.resolve("command.png").toFile();
		run(app, "ExportImage(\"type\",\"png\",\"scale\",1,\"filename\",\""
				+ slashes(command) + "\")");
		assertSize(150, 150, ImageIO.read(command), "ExportImage PNG");
		File svg = directory.resolve("command.svg").toFile();
		run(app, "ExportImage(\"type\",\"svg\",\"filename\",\"" + slashes(svg) + "\")");
		double[] viewBox = svgViewBox(Files.readString(svg.toPath()));
		assertEquals(150, viewBox[2], 1e-9, "ExportImage SVG");
		assertEquals(150, viewBox[3], 1e-9);
		File pdf = directory.resolve("command.pdf").toFile();
		run(app, "ExportImage(\"type\",\"pdf\",\"filename\",\"" + slashes(pdf) + "\")");
		double[] box = mediaBox(new String(Files.readAllBytes(pdf.toPath()),
				StandardCharsets.ISO_8859_1));
		assertEquals(150 * PictureExportService.pdfPointsPerPixel(view), box[2], 0.001,
				"ExportImage PDF");

		List<BufferedImage> copied = new ArrayList<>();
		app.getPictureExportService().setClipboardSink(copied::add);
		app.copyGraphicsViewToClipboard(view);
		assertEquals(1, copied.size(), "Ctrl+Shift+C and the dialog button");
		assertSize(300, 300, copied.get(0), "clipboard at the host clipboard scale");
		assertTrue(count(copied.get(0), MARK) > 0);
	}

	// -------------------------------------------------------- T-EXPORT-POINTS

	@Test
	void exportPointsAreDerivedLiveAndDegeneraciesFallBack() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		ExportAreaSession session = app.getExportAreaSession();
		exportPoints(app, new double[] {0, -1, 4, 3});
		eval(app, "Export_2=(6,3)");
		assertArea(session.resolve(view), ExportArea.Source.EXPORT_POINTS_AUTOMATIC, 0, 6, -1, 3);
		eval(app, "Export_2=(0,3)");
		assertArea(session.resolve(view), ExportArea.Source.VISIBLE_VIEWPORT, -1, 7, -2, 4);
		assertFalse(session.useExportPoints(App.VIEW_EUCLIDIAN), "a zero-width rectangle");
		eval(app, "Export_2=(1/0,3)");
		assertArea(session.resolve(view), ExportArea.Source.VISIBLE_VIEWPORT, -1, 7, -2, 4);
		eval(app, "Export_2=(4,3)");
		assertTrue(session.useExportPoints(App.VIEW_EUCLIDIAN));
		eval(app, "Export_2=(5,3)");
		assertArea(session.resolve(view), ExportArea.Source.EXPORT_POINTS_EXPLICIT, 0, 5, -1, 3);

		String xml = app.getXML();
		app.setSaved();
		assertTrue(app.clearConstruction());
		assertNull(session.getExplicitProducer(), "New resets the session");
		assertTrue(app.loadXML(xml));
		assertNull(session.getExplicitProducer(), "Open resets the session");
		assertArea(session.resolve(view), ExportArea.Source.EXPORT_POINTS_AUTOMATIC, 0, 5, -1, 3);
	}

	// --------------------------------------------------------------- T-MANUAL

	@Test
	void theManualAreaIsSessionStateWithoutUndoOrModification() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		ExportAreaSession session = app.getExportAreaSession();
		final GeoElement point = eval(app, "A=(1,1)");
		UndoManagerD undo = PreG9BR6PlusA1LayerWorkspaceTest.undo(app);
		PreG9BR6PlusA1LayerWorkspaceTest.baseline(undo);
		app.setSaved();
		double[][] answer = { {23, 20, 13, 10} };
		List<double[]> initials = new ArrayList<>();
		app.setExportAreaPrompt((owner, initial, visible) -> {
			initials.add(initial);
			assertArrayEquals(new double[] {-1, 7, -2, 4}, visible, 1e-9, "the visible view");
			return answer[0];
		});
		action(app, "export.area.define-rectangle");
		assertArrayEquals(new double[] {-1, 7, -2, 4}, initials.get(0), 1e-9,
				"starts from the effective area");
		assertArea(session.resolve(view), ExportArea.Source.MANUAL, 20, 23, 10, 13);
		assertTrue(app.isSaved(), "the document is not modified");
		assertFalse(undo.undoPossible(), "no undo point");

		try (MockedStatic<JOptionPane> pane = Mockito.mockStatic(JOptionPane.class)) {
			answer[0] = new double[] {1, 1, 0, 3};
			action(app, "export.area.define-rectangle");
			pane.verify(() -> JOptionPane.showMessageDialog(Mockito.any(), Mockito.any(),
					Mockito.any(), Mockito.anyInt()));
		}
		assertArea(session.resolve(view), ExportArea.Source.MANUAL, 20, 23, 10, 13);
		answer[0] = null;
		action(app, "export.area.define-rectangle");
		assertArea(session.resolve(view), ExportArea.Source.MANUAL, 20, 23, 10, 13);

		eval(app, "A=(2,2)");
		PreG9BR6PlusA1LayerWorkspaceTest.awaitStore(app, undo);
		app.getKernel().undo();
		app.getKernel().redo();
		assertArea(session.resolve(view), ExportArea.Source.MANUAL, 20, 23, 10, 13);
		assertEquals(point.getLabelSimple(), "A");

		action(app, "export.area.clear");
		assertArea(session.resolve(view), ExportArea.Source.VISIBLE_VIEWPORT, -1, 7, -2, 4);
		define(app, 20, 23, 10, 13);
		// New on a modified document first asks to save; the session resets once cleared
		app.setSaved();
		assertTrue(app.clearConstruction());
		assertArea(session.resolve(view), ExportArea.Source.VISIBLE_VIEWPORT,
				view.getXmin(), view.getXmax(), view.getYmin(), view.getYmax());
	}

	// ------------------------------------------------------ T-NO-SERIALIZATION

	@Test
	void noAreaStateReachesTheDocumentUndoOrClipboard(@TempDir Path directory)
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		final EuclidianView view = sized(app.getEuclidianView1());
		eval(app, "A=(1,1)");
		exportPoints(app, new double[] {0, -1, 4, 3});
		String xml = app.getXML();
		final String undoXml = app.getKernel().getConstruction().getCurrentUndoXML(true)
				.toString();
		File first = directory.resolve("first.cedg").toFile();
		assertTrue(app.saveGeoGebraFile(first));

		define(app, 20, 23, 10, 13);
		app.toggleExportAreaOverlay();
		assertTrue(app.getExportAreaSession().useExportPoints(App.VIEW_EUCLIDIAN));
		define(app, -3, 3, -3, 3);
		png(app, view, 1);

		assertEquals(xml, app.getXML());
		assertEquals(undoXml,
				app.getKernel().getConstruction().getCurrentUndoXML(true).toString());
		assertFalse(xml.contains("MANUAL") || xml.contains("exportArea"));
		File second = directory.resolve("second.cedg").toFile();
		assertTrue(app.saveGeoGebraFile(second));
		assertEquals(PreG9BR6PlusA1LayerWorkspaceTest.constructionXml(app),
				PreG9BR6PlusA1LayerWorkspaceTest.constructionXml(app));
		assertArrayEquals(entry(first, "geogebra.xml"), entry(second, "geogebra.xml"),
				"the document re-saves identically");
	}

	// -------------------------------------------------------------- T-OVERLAY

	@Test
	void theOverlayIsShownLiveAndAbsentFromEveryOutput() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		SwingUtilities.invokeAndWait(() -> view.showGrid(false));
		define(app, 0, 4, -1, 3);
		assertEquals(0, count(live(view), OVERLAY, 20));
		action(app, "export.area.show");
		assertTrue(app.isExportAreaOverlayShown());
		assertTrue(count(live(view), OVERLAY, 20) > 50, "Graphics 1 shows the overlay");
		assertEquals(0, count(png(app, view, 1), OVERLAY, 20), "not exported");
		BufferedImage preview = (BufferedImage) ((MyImageD) app.getSavePreviewImage(
				FileExtensions.PNG, 200, 200)).getImage();
		assertEquals(0, count(preview, OVERLAY, 20), "not in the preview");
		action(app, "export.area.show");
		assertFalse(app.isExportAreaOverlayShown());
	}

	// --------------------------------------------------- T-PREVIEW-FINAL

	@Test
	void theSavePreviewIsTheFinalExportInAreaAndVisibility() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		GeoElement hidden = redSegmentOnLayer(app, 3, "(20.5,11)", "(22.5,12)");
		define(app, 20, 24, 10, 13);
		for (FileExtensions extension : new FileExtensions[] {FileExtensions.PNG,
				FileExtensions.PDF, FileExtensions.SVG, FileExtensions.EMF}) {
			BufferedImage preview = preview(app, extension);
			assertEquals(4.0 / 3, (double) preview.getWidth() / preview.getHeight(), 0.02,
					"the area aspect " + extension);
			assertTrue(count(preview, RED) > 0, "visible " + extension);
		}
		app.getLayerWorkspace().setLayerHidden(3, true);
		for (FileExtensions extension : new FileExtensions[] {FileExtensions.PNG,
				FileExtensions.PDF, FileExtensions.SVG, FileExtensions.EMF}) {
			assertEquals(0, count(preview(app, extension), RED), "hidden " + extension);
		}
		assertEquals(0, count(png(app, view, 1), RED), "the final export agrees");
		assertTrue(hidden.isEuclidianVisible());
	}

	@Test
	void theNativeSavePreviewKeepsTheHostThumbnail() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		redSegmentOnLayer(app, 0, "(0,0)", "(4,0)");
		define(app, 20, 24, 10, 13);
		BufferedImage nativePreview = preview(app, FileExtensions.GEOCEDG);
		BufferedImage host = (BufferedImage) ((MyImageD) app.getExportImage(
				org.geogebra.common.io.MyXMLio.THUMBNAIL_PIXELS_X,
				org.geogebra.common.io.MyXMLio.THUMBNAIL_PIXELS_Y)).getImage();
		assertSize(host.getWidth(), host.getHeight(), nativePreview, "host thumbnail");
		assertTrue(count(nativePreview, RED) > 0, "the document thumbnail ignores the area");
	}

	// --------------------------------------------------- T-SAVE-PREVIEW-STALE

	@Test
	void reopeningSaveWithTheSameNameRegeneratesThePreview(@TempDir Path directory)
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		redSegmentOnLayer(app, 3, "(0,0)", "(4,0)");
		app.setCurrentFile(directory.resolve("drawing.cedg").toFile());
		// the earlier export with the layer visible
		File earlier = directory.resolve("drawing.png").toFile();
		app.getPictureExportService().writePNG(view, earlier, false, 72, 1, ExportType.PNG);
		RecordingChooser chooser = new RecordingChooser(app, directory.toFile());
		SwingUtilities.invokeAndWait(() -> {
			chooser.setSize(900, 500);
			((DialogManagerD) app.getDialogManager()).setFileChooser(chooser);
		});

		openPictureSave(app);
		assertTrue(count(chooser.previews.get(0), RED) > 0, "layer visible");
		openPictureSave(app);
		assertTrue(count(chooser.previews.get(1), RED) > 0, "same name, same state");
		app.getLayerWorkspace().setLayerHidden(3, true);
		openPictureSave(app);
		assertNotNull(chooser.previews.get(2), "the preview is regenerated");
		assertEquals(0, count(chooser.previews.get(2), RED), "and is not stale");
		assertEquals(earlier, chooser.getSelectedFile(), "the same name was reopened");

		// a reset selection that keeps the earlier name in the file-name field
		app.getLayerWorkspace().setLayerHidden(3, false);
		SwingUtilities.invokeAndWait(() -> {
			chooser.setSelectedFile(null);
			chooser.refreshPreview();
		});
		BufferedImage kept = RecordingChooser.preview(chooser);
		assertTrue(kept == null || count(kept, RED) > 0, "never the hidden-layer thumbnail");
	}

	// ------------------------------------------------------ T-SCREEN-ANCHORED
	// (with the fidelity class) and T-BACKGROUND-IMAGE

	@Test
	void backgroundImagesFollowTheBackgroundPassInLiveViewPreviewAndExport()
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		final EuclidianView view = sized(app.getEuclidianView1());
		BufferedImage pixels = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = pixels.createGraphics();
		g.setColor(new java.awt.Color(0, 0, 210));
		g.fillRect(0, 0, 8, 8);
		g.dispose();
		app.getImageManager().addExternalImage("b-background.png", new MyImageD(pixels));
		org.geogebra.common.kernel.geos.GeoImage image =
				new org.geogebra.common.kernel.geos.GeoImage(app.getKernel().getConstruction());
		image.setImageFileName("b-background.png");
		image.setCorner(new org.geogebra.common.kernel.geos.GeoPoint(
				app.getKernel().getConstruction(), 20, 10, 1), 0);
		image.setCorner(new org.geogebra.common.kernel.geos.GeoPoint(
				app.getKernel().getConstruction(), 23, 10, 1), 1);
		image.setInBackground(true);
		image.setLayer(3);
		image.setLabel("pic");
		image.updateRepaint();
		define(app, 20, 23, 10, 13);
		app.getLayerWorkspace().setLayerHidden(3, true);
		// OBS-A1-BACKGROUND-IMAGE-LAYER, re-characterized: the background pass is
		// not layered; the export and its preview agree with the live view
		assertTrue(count(png(app, view, 1), MARK) > 0, "export");
		assertTrue(count(preview(app, FileExtensions.PNG), MARK) > 0, "preview");
		assertTrue(app.isLayerShown(0) && !app.isLayerShown(3));
	}

	// ------------------------------------------------------------- T-SURFACE

	@Test
	void theFileExportSurfaceOffersOnlyTheAuthorizedEntries() {
		AppGeoCeDG app = G9U1ActionRegistryTest.app(true);
		app.getGuiManager().initMenubar();
		Set<String> ids = new HashSet<>();
		GeoCeDGMenuBar bar = (GeoCeDGMenuBar) app.getGuiManager().getMenuBar();
		for (Component component : bar.getComponents()) {
			G9U1WorkspaceSurfaceTest.collect(component, ids);
		}
		for (String id : new String[] {"export.picture", "export.dxf-2d", "export.pstricks",
				"export.pgf", "export.asymptote", "document.print-preview",
				"export.area.define-rectangle", "export.area.use-export-points",
				"export.area.show", "export.area.clear", "export.area.iso-a",
				"export.area.use-iso-a-border"}) {
			assertTrue(ids.contains(id), id);
		}
		// PRE-G9B-R6-plus-E3 adds the two ISO A export-area actions
		Set<String> authorized = Set.of("export.picture", "export.dxf-2d", "export.pstricks",
				"export.pgf", "export.asymptote", "export.area.define-rectangle",
				"export.area.use-export-points", "export.area.show", "export.area.clear",
				"export.area.iso-a", "export.area.use-iso-a-border");
		for (String id : ids) {
			assertTrue(!id.startsWith("export.") || authorized.contains(id), id);
			String lower = id.toLowerCase(java.util.Locale.ROOT);
			for (String excluded : new String[] {"stl", "collada", "worksheet", "gif",
					"webm", "upload"}) {
				assertFalse(lower.contains(excluded), id + " is excluded");
			}
		}
		for (UpstreamExportEntry entry : UpstreamExportEntry.values()) {
			assertFalse(app.isUpstreamExportEntryAvailable(entry), entry.name());
		}
		assertFalse(app.isAnimatedExportAvailable());
		String deferral = GeoCeDGProfile.getCatalog().toString();
		assertTrue(deferral.contains("PRE-G9B-R6-plus-B delivered the picture export"));
	}

	@Test
	void theV1FallbackFiltersTheExcludedEntriesAndKeepsTheServiceRoutes()
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		final EuclidianView view = sized(app.getEuclidianView1());
		Class<?> type = Class.forName("org.geogebra.desktop.gui.menubar.FileMenuD");
		Constructor<?> constructor = type.getDeclaredConstructor(AppD.class);
		constructor.setAccessible(true);
		JMenu menu = (JMenu) constructor.newInstance(app);
		// the host menu builds its actions and items when it is first opened
		SwingUtilities.invokeAndWait(() -> ((javax.swing.event.MenuListener) menu)
				.menuSelected(new javax.swing.event.MenuEvent(menu)));
		assertTrue(menu.getItemCount() > 0);
		Set<Object> actions = new HashSet<>();
		collectActions(menu, actions);
		for (String field : new String[] {"exportWorksheet", "saveOnlineAction",
				"exportAnimationAction", "exportSTLaction", "exportColladaAction",
				"exportColladaHTMLAction"}) {
			assertFalse(actions.contains(field(type, menu, field)), field);
		}
		for (String field : new String[] {"exportGraphicAction", "drawingPadToClipboardAction",
				"printEuclidianViewAction", "exportPgfAction", "exportPSTricksAction",
				"exportAsymptoteAction"}) {
			assertTrue(actions.contains(field(type, menu, field)), field);
		}
		define(app, 20, 23, 10, 13);
		java.util.concurrent.BlockingQueue<BufferedImage> copied =
				new java.util.concurrent.LinkedBlockingQueue<>();
		app.getPictureExportService().setClipboardSink(copied::add);
		// the host entry copies on its own thread
		((javax.swing.Action) field(type, menu, "drawingPadToClipboardAction"))
				.actionPerformed(new ActionEvent(menu, 0, "copy"));
		BufferedImage image = copied.poll(10, java.util.concurrent.TimeUnit.SECONDS);
		assertNotNull(image, "the fallback clipboard entry uses the service");
		assertSize(300, 300, image, "the effective area");
		assertSame(view, app.getActiveEuclidianView());
	}

	// ------------------------------------------------------------ T-SHORTCUTS

	@Test
	void hiddenExportShortcutsReachOnlyTheirGeoCeDGRoutes() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		GeoElement point = eval(app, "A=(1,1)");
		GeoElement slider = eval(app, "a=Slider(0,1)");
		boolean[] allowed = { point.isSelectionAllowed(app.getEuclidianView1()),
				slider.isSelectionAllowed(app.getEuclidianView1()) };
		final boolean sliderFixed = ((org.geogebra.common.kernel.geos.GeoNumeric) slider)
				.isLockedPosition();
		int[] dxf = new int[1];
		app.setDxfShortcutAction(() -> dxf[0]++);
		Object dispatcher = app.getGlobalKeyDispatcher();
		// the override is declared by the GeoCeDG dispatcher; asserting this first
		// keeps the host Ctrl+Shift+M clipboard branch unreachable from this test
		Method handle = dispatcher.getClass().getDeclaredMethod("handleCtrlKey",
				KeyCodes.class, boolean.class, boolean.class, boolean.class);
		handle.setAccessible(true);
		assertTrue((Boolean) handle.invoke(dispatcher, KeyCodes.W, true, false, true),
				"W consumed");
		assertTrue((Boolean) handle.invoke(dispatcher, KeyCodes.M, true, false, true),
				"M consumed");
		assertTrue((Boolean) handle.invoke(dispatcher, KeyCodes.D, true, false, true));
		assertEquals(1, dxf[0], "D runs the GeoCeDG DXF action once");
		assertEquals(allowed[0], point.isSelectionAllowed(app.getEuclidianView1()));
		assertEquals(allowed[1], slider.isSelectionAllowed(app.getEuclidianView1()));
		assertEquals(sliderFixed,
				((org.geogebra.common.kernel.geos.GeoNumeric) slider).isLockedPosition());
	}

	// --------------------------------------------------------------- T-PRINT

	@Test
	void printPreviewRendersTheEffectiveAreaAndVisibility() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianViewD view = (EuclidianViewD) sized(app.getEuclidianView1());
		redSegmentOnLayer(app, 3, "(20.5,11)", "(22.5,12)");
		mark(app);
		define(app, 20, 23, 10, 13);
		PrintPreviewD preview = Mockito.mock(PrintPreviewD.class);
		Mockito.when(preview.adjustIndex(0)).thenReturn(0);
		app.setPrintPreview(preview);
		try {
			assertTrue(count(printed(view), MARK) > 0, "the area outside the live view");
			assertTrue(count(printed(view), RED) > 0);
			app.getLayerWorkspace().setLayerHidden(3, true);
			assertEquals(0, count(printed(view), RED), "hidden layer omitted");
			assertTrue(count(printed(view), MARK) > 0);
		} finally {
			app.setPrintPreview(null);
		}
	}

	// ----------------------------------------------------------------- T-CLI

	@Test
	void theCommandLineWritesTheEffectiveAreaOrFailsWithoutAFile(@TempDir Path directory)
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		mark(app);
		define(app, 20, 23, 10, 13);
		File png = directory.resolve("out.png").toFile();
		assertEquals(PictureExportCommandLine.SUCCESS, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, png, 254, null));
		BufferedImage written = ImageIO.read(png);
		// 254 dpi = 100 px/cm at 1 cm per unit: 3 units = 300 px
		assertSize(300, 300, written, "--export of the area");
		assertTrue(count(written, MARK) > 0);
		File svg = directory.resolve("out.svg").toFile();
		assertEquals(PictureExportCommandLine.SUCCESS, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, svg, 72, null));
		assertEquals(150, svgViewBox(Files.readString(svg.toPath()))[2], 1e-9);

		File unsupported = directory.resolve("out.bmp").toFile();
		assertEquals(PictureExportCommandLine.FAILURE, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, unsupported, 72, null));
		assertFalse(unsupported.exists());
		File missingDirectory = directory.resolve("missing").resolve("out.png").toFile();
		assertEquals(PictureExportCommandLine.FAILURE, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, missingDirectory, 72, null));
		assertFalse(missingDirectory.exists());
		File huge = directory.resolve("huge.png").toFile();
		Files.writeString(huge.toPath(), "earlier");
		assertEquals(PictureExportCommandLine.FAILURE, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, huge, 2000000000, null));
		assertEquals("earlier", Files.readString(huge.toPath()), "an earlier file is kept");
		try (var listing = Files.list(directory)) {
			assertFalse(listing.anyMatch(p -> p.getFileName().toString()
					.startsWith(".geocedg-export-")), "no temporary file is left");
		}
		assertFalse(app.isAnimatedExportAvailable(), "--exportAnimation is rejected");
		assertTrue(PictureExportCommandLine.animationRejectedMessage().contains("Animated GIF"));
	}

	@Test
	void theCommandLineExportsADocumentWhoseViewIsNotLaidOut(@TempDir Path directory)
			throws Exception {
		AppGeoCeDG author = G9U1TestApp.create();
		exportPoints(author, new double[] {20, 10, 23, 13});
		mark(author);
		File document = directory.resolve("cli.cedg").toFile();
		assertTrue(author.saveGeoGebraFile(document));
		// the state of --export: the document is open, its view not laid out yet
		AppGeoCeDG app = G9U1TestApp.create();
		assertTrue(app.loadFile(document, false));
		EuclidianView view = app.getActiveEuclidianView();
		assertEquals(0, view.getWidth());
		assertEquals(0, view.getPrintingScale(), 0, "the host has no printing scale yet");
		File png = directory.resolve("cli.png").toFile();
		assertEquals(PictureExportCommandLine.SUCCESS, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, png, 72, ""));
		// 3 units at 50 px/unit and 1 cm/unit: 150 px * 72 / (2.54 * 50) = 85.04
		BufferedImage written = ImageIO.read(png);
		assertSize(85, 85, written, "--export of an opened document");
		assertTrue(count(written, MARK) > 0);
		File pdf = directory.resolve("cli.pdf").toFile();
		assertEquals(PictureExportCommandLine.SUCCESS, PictureExportCommandLine.export(
				app.getPictureExportRoute(), view, pdf, 72, ""));
		assertEquals(150 * 72 / (2.54 * 50), mediaBox(new String(Files.readAllBytes(
				pdf.toPath()), StandardCharsets.ISO_8859_1))[2], 0.001);
		assertEquals(0, view.getPrintingScale(), 0, "the view is not changed");
	}

	// --------------------------------------------------------- T-EXPORTIMAGE

	@Test
	void exportImageRejectsAnimatedTypesExplicitly(@TempDir Path directory) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		for (String type : new String[] {"gif", "webm"}) {
			File target = directory.resolve("anim." + type).toFile();
			List<String> errors = new ArrayList<>();
			app.getKernel().getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(
					"ExportImage(\"type\",\"" + type + "\",\"filename\",\"" + slashes(target)
							+ "\")", false, errorHandler(errors), false, null);
			assertEquals(1, errors.size(), type);
			assertTrue(errors.get(0).contains(type), errors.get(0));
			assertFalse(target.exists());
			assertTrue(PictureExportPolicy.rejectsAnimatedType(app,
					"gif".equals(type) ? ExportType.ANIMATED_GIF : ExportType.WEBM));
		}
	}

	// ----------------------------------------------------- T-API, T-INTERIM-C

	/**
	 * SHA-256 of the PGF/TikZ, PSTricks, Asymptote and DXF outputs of
	 * {@link #interimScene}, recorded by the same code on a {@code git archive}
	 * of the implementation base {@code f6194f09}.
	 */
	private static final String[] BASE_INTERIM_SHA256 = {
		"1d2570f9453ad70c64a5f7c9deac22c0362520358bf4c259d84795f04d364844",
		"ba1a29b2f9d1f499a4131235f1b210132c0118beba0869ef204cd136c098c514",
		"9e5cf21cfd5f5a52f595dad437817410a8301eede902e1e37c173577a3eeffbc",
		"e7ba040534c447cf40e3c005ab18c0d2780ceaa9c70e9ea0cc2ac0ccd980f0f2"};

	/**
	 * PRE-G9B-R6-plus-C replaces the interim expectation of B: without an
	 * explicit area the LaTeX and DXF bytes of a document without a construction
	 * unit stay the base bytes; an explicit area now bounds LaTeX and selects the
	 * DXF population by B1 participation (DQ-C13).
	 */
	@Test
	void latexAndDxfKeepTheBaseOutputWithoutAnAreaAndFollowAnExplicitArea()
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = app.getEuclidianView1();
		// sized exactly as the base probe: the default grid is part of the output
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(400, 300);
			view.updateSize();
			view.setCoordSystem(50, 200, 50, 50);
		});
		interimScene(app);
		assertArrayEquals(BASE_INTERIM_SHA256, interimDigests(app), "no area");
		define(app, 20, 23, 10, 13);
		String[] withArea = interimDigests(app);
		for (int i = 0; i < 3; i++) {
			assertNotEquals(BASE_INTERIM_SHA256[i], withArea[i],
					"C: LaTeX output " + i + " follows the explicit area");
		}
		assertEquals(BASE_INTERIM_SHA256[3], withArea[3],
				"the context-free service API reads no export area");
		org.geocedg.common.export.GeometryExportService service =
				new org.geocedg.common.export.GeometryExportService();
		String dxf = service.exportDxf(service.createModel(new ArrayList<>(app.getKernel()
				.getConstruction().getGeoSetConstructionOrder()),
				org.geocedg.common.export.GeometryExportModel.SelectionMode
						.COMPLETE_CONSTRUCTION, new GeoCeDGDxfExportController(app)
								.exportContext()));
		assertTrue(dxf.contains("geocedg-export-area-participation-b1/v1"), dxf);
		assertFalse(dxf.contains("AcDbPoint"), "every source lies outside the area");
	}

	// ---------------------------------------------------------------- T-LEGACY

	@Test
	void legacyExportPointDocumentsExportTheExactRectangleAndResaveIdentically(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		final EuclidianView view = sized(app.getEuclidianView1());
		exportPoints(app, new double[] {0, -1, 4, 3});
		mark(app);
		File document = directory.resolve("legacy.cedg").toFile();
		assertTrue(app.saveGeoGebraFile(document));
		AppGeoCeDG reopened = G9U1TestApp.create();
		EuclidianView reopenedView = sized(reopened.getEuclidianView1());
		assertTrue(reopened.loadFile(document, false));
		assertArea(reopened.getExportAreaSession().resolve(reopenedView),
				ExportArea.Source.EXPORT_POINTS_AUTOMATIC, 0, 4, -1, 3);
		assertSize(200, 200, png(reopened, reopenedView, 1), "exact, no +2 band");
		File resaved = directory.resolve("resaved.cedg").toFile();
		assertTrue(reopened.saveGeoGebraFile(resaved));
		assertArrayEquals(entry(document, "geogebra.xml"), entry(resaved, "geogebra.xml"));
		assertSame(view, app.getEuclidianView1());
	}

	// ---------------------------------------------------------------- T-CLASSIC

	@Test
	void theUpstreamDefaultsOfTheNewSeamsKeepTheBaseBehavior() throws Exception {
		AppCommon common = AppCommonFactory.create();
		EuclidianView commonView = common.getActiveEuclidianView();
		assertEquals(commonView.getExportWidth(), common.getExportFrameWidth(commonView));
		assertEquals(commonView.getExportHeight(), common.getExportFrameHeight(commonView));
		assertTrue(common.isAnimatedExportAvailable());
		assertFalse(PictureExportPolicy.rejectsAnimatedType(common, ExportType.ANIMATED_GIF));

		AppD classic = G9U1TestApp.withoutWindowDispatcher(new AppD(new CommandLineArguments(
				new String[] {"--silent"}), new JPanel(), true));
		EuclidianView view = sized(classic.getEuclidianView1());
		assertNull(classic.getPictureExportRoute());
		for (UpstreamExportEntry entry : UpstreamExportEntry.values()) {
			assertTrue(classic.isUpstreamExportEntryAvailable(entry), entry.name());
		}
		assertTrue(classic.isAnimatedExportAvailable());
		assertEquals(view.getExportWidth(), classic.getExportFrameWidth(view));
		// the host selection-rectangle precedence is untouched in Classic
		view.setSelectionRectangle(AwtFactory.getPrototype().newRectangle(10, 10, 30, 20));
		assertEquals(view.getExportWidth(), classic.getExportFrameWidth(view));
		assertEquals(30, view.getExportWidth());
		BufferedImage preview = (BufferedImage) ((MyImageD) classic.getSavePreviewImage(
				FileExtensions.PNG, 100, 100)).getImage();
		BufferedImage host = (BufferedImage) ((MyImageD) classic.getExportImage(100, 100))
				.getImage();
		assertSize(host.getWidth(), host.getHeight(), preview, "Classic preview");
		view.setSelectionRectangle(null);
	}

	// ----------------------------------------------------------------- helpers

	private static void assertArea(ExportArea area, ExportArea.Source source, double xmin,
			double xmax, double ymin, double ymax) {
		assertNotNull(area);
		assertEquals(source, area.getSource());
		assertEquals(xmin, area.getXmin(), 1e-9, "xmin");
		assertEquals(xmax, area.getXmax(), 1e-9, "xmax");
		assertEquals(ymin, area.getYmin(), 1e-9, "ymin");
		assertEquals(ymax, area.getYmax(), 1e-9, "ymax");
	}

	private static void assertSize(int width, int height, BufferedImage image, String label) {
		assertNotNull(image, label);
		assertEquals(width, image.getWidth(), label + " width");
		assertEquals(height, image.getHeight(), label + " height");
	}

	/** A marker inside the area (20,10)-(23,13), outside the live view. */
	private static void mark(AppGeoCeDG app) {
		GeoElement mark = eval(app, "Mk=Polygon({(20.5,10.5),(22.5,10.5),(22.5,12.5)})");
		mark.setObjColor(MARK);
		mark.setAlphaValue(1);
		mark.setLineOpacity(255);
		mark.setLabelVisible(false);
		mark.updateRepaint();
	}

	private static GeoElement redSegmentOnLayer(AppGeoCeDG app, int layer, String from,
			String to) {
		app.getLayerWorkspace().setWorkingLayer(layer);
		GeoElement segment = eval(app, "seg=Segment(" + from + "," + to + ")");
		app.getLayerWorkspace().setWorkingLayer(0);
		segment.setObjColor(RED);
		segment.setLineOpacity(255);
		segment.setLineThickness(13);
		segment.setLabelVisible(false);
		segment.updateRepaint();
		return segment;
	}

	private static BufferedImage preview(AppGeoCeDG app, FileExtensions extension) {
		return (BufferedImage) ((MyImageD) app.getSavePreviewImage(extension,
				org.geogebra.common.io.MyXMLio.THUMBNAIL_PIXELS_X,
				org.geogebra.common.io.MyXMLio.THUMBNAIL_PIXELS_Y)).getImage();
	}

	private static BufferedImage live(EuclidianView view) throws Exception {
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

	private static BufferedImage printed(EuclidianViewD view) {
		PageFormat format = new PageFormat();
		BufferedImage image = new BufferedImage((int) format.getWidth(),
				(int) format.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(java.awt.Color.WHITE);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		assertEquals(java.awt.print.Printable.PAGE_EXISTS, view.print(g, format, 0));
		g.dispose();
		return image;
	}

	private static void action(AppGeoCeDG app, String id) {
		((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry().get(id)
				.actionPerformed(new ActionEvent(app, 0, id));
	}

	private static void openPictureSave(AppGeoCeDG app) throws Exception {
		SwingUtilities.invokeAndWait(() -> ((GuiManagerD) app.getGuiManager())
				.showSaveDialog(FileExtensions.PNG, null, "PNG", true, false));
	}

	/** The Save chooser of the host flow; closing it records the preview shown. */
	private static final class RecordingChooser extends GeoGebraFileChooser {
		private static final long serialVersionUID = 1L;
		private final transient List<BufferedImage> previews = new ArrayList<>();

		RecordingChooser(AppD app, File directory) {
			super(app, directory);
		}

		@Override
		public int showSaveDialog(Component parent) {
			previews.add(preview(this));
			return CANCEL_OPTION;
		}

		static BufferedImage preview(GeoGebraFileChooser chooser) {
			try {
				Field panelField = GeoGebraFileChooser.class.getDeclaredField("previewPanel");
				panelField.setAccessible(true);
				Object panel = panelField.get(chooser);
				Field imageField = panel.getClass().getDeclaredField("img");
				imageField.setAccessible(true);
				MyImageD image = (MyImageD) imageField.get(panel);
				return image == null ? null : (BufferedImage) image.getImage();
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		}
	}

	private static void collectActions(Component component, Set<Object> actions) {
		if (component instanceof JMenu) {
			for (Component child : ((JMenu) component).getMenuComponents()) {
				collectActions(child, actions);
			}
		}
		if (component instanceof JMenuItem && ((JMenuItem) component).getAction() != null) {
			actions.add(((JMenuItem) component).getAction());
		}
	}

	private static Object field(Class<?> type, Object owner, String name)
			throws ReflectiveOperationException {
		Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(owner);
	}

	private static byte[] entry(File archive, String name) throws Exception {
		try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(archive)) {
			java.util.zip.ZipEntry entry = zip.getEntry(name);
			assertNotNull(entry, name);
			return zip.getInputStream(entry).readAllBytes();
		}
	}

	private static String slashes(File file) {
		return file.getAbsolutePath().replace('\\', '/');
	}

	/** Evaluates a scripting command; a command error fails instead of a dialog. */
	private static void run(AppGeoCeDG app, String command) {
		List<String> errors = new ArrayList<>();
		app.getKernel().getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(command,
				false, errorHandler(errors), false, null);
		assertEquals(List.of(), errors, command);
	}

	private static org.geogebra.common.main.error.ErrorHandler errorHandler(
			List<String> errors) {
		return new org.geogebra.common.main.error.ErrorHandler() {
			@Override
			public void showError(String msg) {
				errors.add(msg);
			}

			@Override
			public void showCommandError(String command, String message) {
				errors.add(message);
			}

			@Override
			public String getCurrentCommand() {
				return null;
			}

			@Override
			public boolean onUndefinedVariables(String string,
					org.geogebra.common.util.AsyncOperation<String[]> callback) {
				return false;
			}

			@Override
			public void resetError() {
				// nothing to reset
			}
		};
	}

	/** The scene of the base fingerprint probe; explicit colors keep it deterministic. */
	private static void interimScene(AppGeoCeDG app) {
		String[][] scene = { {"A=(1,1)", "200", "0", "0"}, {"B=(3,2)", "0", "150", "0"},
				{"s=Segment(A,B)", "0", "0", "200"}, {"c=Circle(A,1)", "150", "0", "150"},
				{"f(x)=x^2/4", "0", "150", "150"},
				{"poly=Polygon({(0,0),(1,0),(0,1)})", "150", "150", "0"},
				{"t=Text(\"abc\",(2,3))", "90", "40", "0"},
				{"g=Line((0,2),(1,3))", "0", "80", "160"}};
		for (String[] item : scene) {
			GeoElement geo = eval(app, item[0]);
			geo.setObjColor(GColor.newColor(Integer.parseInt(item[1]), Integer.parseInt(item[2]),
					Integer.parseInt(item[3])));
			geo.setLineOpacity(255);
			geo.updateRepaint();
		}
	}

	private static String[] interimDigests(AppGeoCeDG app) throws Exception {
		String[] latex = latex(app);
		org.geocedg.common.export.GeometryExportService service =
				new org.geocedg.common.export.GeometryExportService();
		String dxf = service.exportDxf(service.createModel(new ArrayList<>(app.getKernel()
				.getConstruction().getGeoSetConstructionOrder()),
				org.geocedg.common.export.GeometryExportModel.SelectionMode
						.COMPLETE_CONSTRUCTION));
		return new String[] {sha256(latex[0]), sha256(latex[1]), sha256(latex[2]),
				sha256(dxf)};
	}

	private static String sha256(String text) throws Exception {
		StringBuilder hex = new StringBuilder();
		for (byte b : java.security.MessageDigest.getInstance("SHA-256")
				.digest(text.getBytes(StandardCharsets.UTF_8))) {
			hex.append(String.format("%02x", b));
		}
		return hex.toString();
	}

	private static String[] latex(AppGeoCeDG app) {
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
		return code.toArray(new String[0]);
	}
}
