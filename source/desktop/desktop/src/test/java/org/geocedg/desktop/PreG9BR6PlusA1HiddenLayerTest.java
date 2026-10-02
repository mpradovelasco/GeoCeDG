/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.awaitStore;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.baseline;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.constructionXml;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.controller;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.undo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportService;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.export.pstricks.ExportFrameMinimal;
import org.geogebra.common.export.pstricks.GeoGebraExport;
import org.geogebra.common.export.pstricks.GeoGebraToAsymptote;
import org.geogebra.common.export.pstricks.GeoGebraToPgf;
import org.geogebra.common.export.pstricks.GeoGebraToPstricks;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoLocus;
import org.geogebra.common.main.App;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.export.GraphicExportDialog;
import org.geogebra.desktop.export.pstricks.ExportGraphicsFactoryD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.geogebra.desktop.plugin.GgbAPID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R6-plus-A-1 focal contract for session hidden layers: effective
 * visibility in the painting and hit testing of Graphics 1 and Graphics 2,
 * session-only state, unchanged geometry and object visibility, and the export
 * boundary to B and C.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR6PlusA1HiddenLayerTest {
	private static final int WIDTH = 400;
	private static final int HEIGHT = 300;

	// -------------------------------------------------------------- T-HIDE-PAINT

	@Test
	void graphicsOneOmitsObjectsOnHiddenLayersWithoutLosingDrawables() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		GeoElement segment = redSegment(app, 3);
		int drawables = view.getAllDrawableList().size();
		Drawable drawable = (Drawable) view.getDrawableFor(segment);
		assertTrue(red(paint(view)) > 0);

		app.getLayerWorkspace().setLayerHidden(3, true);
		assertEquals(0, red(paint(view)), "hidden layer");
		assertEquals(drawables, view.getAllDrawableList().size());
		assertSame(drawable, view.getDrawableFor(segment));

		app.getLayerWorkspace().setLayerHidden(3, false);
		assertTrue(red(paint(view)) > 0, "shown again");
		assertEquals(drawables, view.getAllDrawableList().size());
		assertSame(drawable, view.getDrawableFor(segment));
		assertTrue(segment.isEuclidianVisible());
	}

	@Test
	void graphicsTwoIsAHostViewAndObeysTheSameRule() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianViewD second = ((GuiManagerD) app.getGuiManager()).getEuclidianView2(1);
		assertFalse(second instanceof GeoCeDGEuclidianView, "an upstream view class");
		second.attachView();
		sized(second);
		GeoElement segment = redSegment(app, 2);
		segment.setVisibility(second.getViewID(), true);
		second.add(segment);
		assertTrue(red(paint(second)) > 0);
		app.getLayerWorkspace().setLayerHidden(2, true);
		assertEquals(0, red(paint(second)));
		app.getLayerWorkspace().setLayerHidden(2, false);
		assertTrue(red(paint(second)) > 0);
	}

	@Test
	void otherLayersAndHiddenObjectsBehaveAsAtTheBase() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		GeoElement segment = redSegment(app, 3);
		app.getLayerWorkspace().setLayerHidden(5, true);
		assertTrue(red(paint(view)) > 0, "another layer is hidden");
		segment.setEuclidianVisible(false);
		segment.updateRepaint();
		assertEquals(0, red(paint(view)), "object hidden, layer shown");
	}

	// ---------------------------------------------------------------- T-HIDE-HIT

	@Test
	void objectsOnHiddenLayersAreNotHittableAndNeverShadowShownOnes() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		app.getLayerWorkspace().setWorkingLayer(2);
		GeoElement low = eval(app, "B=(2,1)");
		app.getLayerWorkspace().setWorkingLayer(6);
		GeoElement top = eval(app, "A=(2,1)");
		app.getLayerWorkspace().setWorkingLayer(0);
		GPoint at = screen(view, 2, 1);

		assertEquals(List.of(top), hits(view, at), "the host keeps only the top layer");
		app.getLayerWorkspace().setLayerHidden(6, true);
		assertEquals(List.of(low), hits(view, at), "a hidden top layer never shadows");
		app.getLayerWorkspace().setLayerHidden(2, true);
		assertTrue(hits(view, at).isEmpty());
		assertNull(view.getLabelHit(at, PointerEventType.MOUSE));
		GRectangle all = org.geogebra.common.awt.AwtFactory.getPrototype()
				.newRectangle(0, 0, WIDTH, HEIGHT);
		view.getHitDetector().setHits(all);
		assertTrue(view.getHits().isEmpty(), "rectangle selection");
		app.getLayerWorkspace().setLayerHidden(6, false);
		app.getLayerWorkspace().setLayerHidden(2, false);
		assertEquals(List.of(top), hits(view, at));
	}

	@Test
	void theWorkingLayerModeCannotAdoptAHiddenLayerByClicking() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		app.getLayerWorkspace().setWorkingLayer(6);
		eval(app, "A=(2,1)");
		app.getLayerWorkspace().setWorkingLayer(0);
		app.getLayerWorkspace().setLayerHidden(6, true);
		AtomicInteger dialogs = new AtomicInteger();
		app.setWorkingLayerChooser(current -> {
			dialogs.incrementAndGet();
			return null;
		});
		app.setMode(EuclidianConstants.MODE_WORKING_LAYER);
		Hits real = new Hits();
		real.addAll(hits(view, screen(view, 2, 1)));
		controller(app).processMode(real, false, false, null);
		assertEquals(1, dialogs.get(), "treated as an empty-space click");
		assertEquals(0, app.getLayerWorkspace().getWorkingLayer());
		assertFalse(app.getLayerWorkspace().isLayerShown(6));
	}

	// -------------------------------------------------------------- T-HIDE-STATE

	@Test
	void togglingIsSessionOnlyAndSurvivesUndoAndRedo() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(3);
		eval(app, "A=(1,1)");
		UndoManagerD undo = undo(app);
		baseline(undo);
		app.setSaved();
		final String document = app.getXML();
		final String undoXml = app.getKernel().getConstruction().getCurrentUndoXML(false).toString();
		final String preferences = app.getPreferencesXML();
		int history = undo.getHistorySize();
		CountDownLatch stored = new CountDownLatch(1);
		undo.addUndoInfoStoredListener(stored::countDown);

		app.getLayerWorkspace().setWorkingLayer(0);
		app.getLayerWorkspace().setLayerHidden(3, true);
		app.getLayerWorkspace().setLayerHidden(7, true);
		assertFalse(stored.await(2, TimeUnit.SECONDS), "no undo point");
		assertEquals(history, undo.getHistorySize());
		assertTrue(app.isSaved(), "the document is not modified");
		assertEquals(document, app.getXML(), "no document state");
		assertEquals(undoXml,
				app.getKernel().getConstruction().getCurrentUndoXML(false).toString());
		assertEquals(preferences, app.getPreferencesXML());
		assertFalse(app.getXML().contains("hidden"), "nothing is serialized");

		eval(app, "B=(2,2)");
		awaitStore(app, undo);
		app.getKernel().undo();
		assertEquals(List.of(3, 7), app.getLayerWorkspace().getHiddenLayers());
		app.getKernel().redo();
		assertEquals(List.of(3, 7), app.getLayerWorkspace().getHiddenLayers());
	}

	@Test
	void macroAndClipboardXmlCarryNoLayerState() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(3);
		GeoElement a = eval(app, "A=(1,1)");
		String macros = app.getXMLio().getFullMacroXML(new ArrayList<>());
		org.geogebra.common.util.InternalClipboard.copyToXMLInternal(app, List.of(a));
		String clipboard = copiedXml();
		app.getLayerWorkspace().setWorkingLayer(0);
		app.getLayerWorkspace().setLayerHidden(3, true);
		assertEquals(macros, app.getXMLio().getFullMacroXML(new ArrayList<>()));
		org.geogebra.common.util.InternalClipboard.copyToXMLInternal(app, List.of(a));
		assertEquals(clipboard, copiedXml());
	}

	// ----------------------------------------------------------- T-HIDE-GEOMETRY

	@Test
	void slopeFieldAndOdeGeometryIsUnchangedByHiding() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		sized(app.getEuclidianView1());
		app.getLayerWorkspace().setWorkingLayer(4);
		GeoElement field = eval(app, "sf=SlopeField(x+y)");
		GeoElement ode = eval(app, "ode=SolveODE(x*y,(0,1))");
		app.getLayerWorkspace().setWorkingLayer(0);
		String fieldBefore = points(field);
		String odeBefore = points(ode);
		app.getLayerWorkspace().setLayerHidden(4, true);
		app.getKernel().updateConstruction(false);
		assertEquals(fieldBefore, points(field));
		assertEquals(odeBefore, points(ode));
	}

	// -------------------------------------------------------- T-OBJECT-VISIBILITY

	@Test
	void hostLayerCommandsStillWriteObjectVisibilityOnly() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getLayerWorkspace().setWorkingLayer(3);
		GeoElement a = eval(app, "A=(1,1)");
		app.getLayerWorkspace().setWorkingLayer(0);
		run(app, "HideLayer(3)");
		assertFalse(a.isEuclidianVisible(), "HideLayer writes object visibility");
		assertTrue(app.getLayerWorkspace().isLayerShown(3), "and never the session state");
		run(app, "ShowLayer(3)");
		assertTrue(a.isEuclidianVisible());
		new GgbAPID(app).setLayerVisible(3, false);
		assertFalse(a.isEuclidianVisible());
		new GgbAPID(app).setLayerVisible(3, true);
		assertTrue(a.isEuclidianVisible());

		app.getLayerWorkspace().setLayerHidden(3, true);
		assertTrue(a.isEuclidianVisible(), "a hidden layer never writes the object");
		// The host has no IsVisible command; the object-visibility query is the API.
		assertTrue(new GgbAPID(app).getVisible("A"));
		assertTrue(new GgbAPID(app).getVisible("A", 1));
		assertTrue(constructionXml(app).contains("<show object=\"true\""));
	}

	// --------------------------------------------------------------- T-CLASSIC

	@Test
	void theHostSeamsKeepTheUpstreamRuleUntilTheProductIsReady() {
		AppGeoCeDG app = G9U1TestApp.create();
		App host = org.geogebra.common.AppCommonFactory.create();
		assertEquals(7, host.getLayerForNewObject(host.getKernel().getConstruction(), 7));
		for (int layer = 0; layer <= 9; layer++) {
			assertTrue(host.isLayerShown(layer));
		}
		assertEquals(5, app.getLayerForNewObject(
				new org.geogebra.common.kernel.Construction(app.getKernel()), 5),
				"a construction other than the document keeps the host value");
	}

	// ----------------------------------------------------- T-EXPORT-INHERITED

	@Test
	void picturesExportedThroughTheViewPaintingOmitHiddenLayers() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		redSegment(app, 3);
		assertTrue(red(png(view)) > 0);
		assertTrue(red(printed((EuclidianViewD) view)) > 0);
		app.getLayerWorkspace().setLayerHidden(3, true);
		assertEquals(0, red(png(view)), "PNG uses exportPaint");
		assertEquals(0, red(printed((EuclidianViewD) view)), "print uses exportPaint");
	}

	// -------------------------------------------------------- T-EXPORT-INTERIM

	@Test
	void svgLatexAndDxfAreUnchangedByAHiddenLayerUntilBAndC() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		app.getLayerWorkspace().setWorkingLayer(3);
		eval(app, "A=(1,1)");
		eval(app, "B=(3,2)");
		final GeoElement s = eval(app, "s=Segment(A,B)");
		eval(app, "c=Circle(A,1)");
		app.getLayerWorkspace().setWorkingLayer(0);
		String svg = svg(app, view);
		assertEquals(svg, svg(app, view), "SVG export is deterministic once ids are normalized");
		List<String> latex = latex(app, view);
		String dxf = dxf(app);
		assertTrue(svg.contains("layer3"), "the object is written");
		for (String code : latex) {
			assertTrue(code.toLowerCase(java.util.Locale.ROOT).contains("circle"),
					"each LaTeX dialect writes the circle on the hidden layer");
		}
		assertTrue(dxf.contains("CIRCLE") && dxf.contains("LINE"), "DXF writes them");

		app.getLayerWorkspace().setLayerHidden(3, true);
		// PRE-G9B-R6-plus-B delivered the SVG part: the picture service omits it
		String hiddenSvg = svg(app, view);
		assertNotEquals(svg, hiddenSvg, "SVG belongs to B");
		assertFalse(hiddenSvg.contains("layer3"), "B omits the hidden layer from SVG");
		assertEquals(latex, latex(app, view), "LaTeX exclusion belongs to C");
		assertEquals(dxf, dxf(app), "DXF policy belongs to C");
		assertTrue(s.isEuclidianVisible());
	}

	// --------------------------------------------------------------------- helpers

	private static EuclidianView sized(EuclidianView view) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(WIDTH, HEIGHT);
			view.updateSize();
			view.setCoordSystem(50, 200, 50, 50);
		});
		return view;
	}

	private static GeoElement redSegment(AppGeoCeDG app, int layer) {
		app.getLayerWorkspace().setWorkingLayer(layer);
		GeoElement segment = eval(app, "seg=Segment((0,0),(4,0))");
		app.getLayerWorkspace().setWorkingLayer(0);
		segment.setObjColor(GColor.RED);
		segment.setLineThickness(13);
		segment.setLabelVisible(false);
		segment.updateRepaint();
		assertEquals(layer, segment.getLayer());
		return segment;
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

	private static BufferedImage png(EuclidianView view) {
		return org.geogebra.desktop.awt.GBufferedImageD.getAwtBufferedImage(view.getExportImage(1));
	}

	private static BufferedImage printed(EuclidianViewD view) {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		view.exportPaint(g, 1, ExportType.PRINTING);
		g.dispose();
		return image;
	}

	/** Strokes are drawn at 0.698 opacity, so pure red blends to about (255,77,77). */
	private static int red(BufferedImage image) {
		int count = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				Color c = new Color(image.getRGB(x, y));
				if (c.getRed() > 200 && c.getGreen() < 140 && c.getBlue() < 140) {
					count++;
				}
			}
		}
		return count;
	}

	private static GPoint screen(EuclidianView view, double x, double y) {
		return new GPoint(view.toScreenCoordX(x), view.toScreenCoordY(y));
	}

	private static List<GeoElement> hits(EuclidianView view, GPoint at) {
		view.updateAllDrawables(true);
		view.setHits(at, PointerEventType.MOUSE);
		return new ArrayList<>(view.getHits());
	}

	private static String points(GeoElement geo) {
		if (geo instanceof GeoLocus) {
			return ((GeoLocus) geo).getPoints().toString();
		}
		if (geo instanceof GeoList) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < ((GeoList) geo).size(); i++) {
				sb.append(points(((GeoList) geo).get(i))).append(';');
			}
			return sb.toString();
		}
		return geo.toValueString(org.geogebra.common.kernel.StringTemplate.maxPrecision);
	}

	private static String svg(AppGeoCeDG app, EuclidianView view) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GraphicExportDialog.exportSVG(app, (EuclidianViewD) view, out, false, WIDTH, HEIGHT,
				8, 6, 1, false);
		// Generated clip-path ids differ between any two exports; they carry no content.
		return new String(out.toByteArray(), StandardCharsets.UTF_8)
				.replaceAll("clip[0-9a-f-]+", "clip");
	}

	private static List<String> latex(AppGeoCeDG app, EuclidianView view) {
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
		assertNotEquals(code.get(0), code.get(1));
		return code;
	}

	/** Evaluates a command; a command error fails instead of opening a modal dialog. */
	private static void run(AppGeoCeDG app, String command) {
		try (org.mockito.MockedStatic<javax.swing.JOptionPane> pane =
				org.mockito.Mockito.mockStatic(javax.swing.JOptionPane.class)) {
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
			pane.verifyNoInteractions();
		}
	}

	private static String copiedXml() {
		try {
			java.lang.reflect.Field field = org.geogebra.common.util.InternalClipboard.class
					.getDeclaredField("copiedXml");
			field.setAccessible(true);
			return field.get(null).toString();
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static String dxf(AppGeoCeDG app) {
		GeometryExportService service = new GeometryExportService();
		return service.exportDxf(service.createModel(new ArrayList<>(app.getKernel()
				.getConstruction().getGeoSetConstructionOrder()),
				SelectionMode.COMPLETE_CONSTRUCTION));
	}
}
