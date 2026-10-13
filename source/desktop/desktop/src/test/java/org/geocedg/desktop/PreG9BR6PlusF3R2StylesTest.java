/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.MM;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.awaitQuiescent;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.baseline;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.undo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.SwingUtilities;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.menubar.LoadFileListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-F3-R2 corrective regression (author smoke of Apply Styles, T0 /
 * {@code P5}): every applicable property of the six transfers from the source document's
 * construction defaults to segments, points and circles in both directions, and objects
 * that a document closed by Open, Open Recent or New left in the inherited selection are
 * never taken for a selection of the open drawing.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF3R2StylesTest {

	private static final String[] OBJECTS = {"A", "B", "c", "C", "D", "f"};
	private static final GColor POINT = GColor.DARK_GREEN;
	private static final GColor SEGMENT = GColor.RED;
	private static final GColor CIRCLE = GColor.BLUE;
	private static final int WIDTH = 400;
	private static final int HEIGHT = 300;

	@TempDir
	Path dir;

	/** The construction of the author's EstilosOrigen and EstilosDestino documents. */
	private static AppGeoCeDG construction() {
		AppGeoCeDG app = app(MM);
		for (String c : new String[] {"A=(1.6,2.44)", "B=(3.54,2.94)", "c=Circle(A,B)",
				"C=(1.6,4.98)", "D=(5.24,4.84)", "f=Segment(C,D)"}) {
			must(app, c);
		}
		return app;
	}

	/**
	 * EstilosDestino: black objects, a circle of thickness 5 and a segment of thickness 8,
	 * dot points; factory construction defaults.
	 */
	private static File plain(Path dir, String name) {
		AppGeoCeDG app = construction();
		for (String label : OBJECTS) {
			app.getKernel().lookupLabel(label).setObjColor(GColor.BLACK);
		}
		app.getKernel().lookupLabel("f").setLineThickness(8);
		return save(app, dir, name);
	}

	/**
	 * A style source whose construction defaults differ from the factory ones for every
	 * applicable property of points, segments and circles; its own objects carry other
	 * styles, which T0 never reads.
	 */
	private static File styled(Path dir, String name) {
		AppGeoCeDG app = construction();
		for (String label : OBJECTS) {
			GeoElement geo = app.getKernel().lookupLabel(label);
			geo.setObjColor(GColor.MAGENTA);
			geo.setLineThickness(9);
		}
		ConstructionDefaults defaults = app.getKernel().getConstruction().getConstructionDefaults();
		GeoPoint point = (GeoPoint) defaults.getDefaultGeo(ConstructionDefaults.DEFAULT_POINT_FREE);
		point.setObjColor(POINT);
		point.setPointSize(7);
		point.setPointStyle(EuclidianStyleConstants.POINT_STYLE_CROSS);
		GeoElement segment = defaults.getDefaultGeo(ConstructionDefaults.DEFAULT_SEGMENT);
		segment.setObjColor(SEGMENT);
		segment.setLineThickness(2);
		segment.setLineType(EuclidianStyleConstants.LINE_TYPE_DASHED_LONG);
		GeoElement conic = defaults.getDefaultGeo(ConstructionDefaults.DEFAULT_CONIC);
		conic.setObjColor(CIRCLE);
		conic.setLineThickness(2);
		conic.setLineType(EuclidianStyleConstants.LINE_TYPE_DOTTED);
		conic.setAlphaValue(0.25);
		return save(app, dir, name);
	}

	private static AppGeoCeDG opened(File file) throws Exception {
		AppGeoCeDG app = app(MM);
		assertTrue(onEdt(() -> app.loadFile(file, false)));
		baseline(app);
		return app;
	}

	/** Apply Styles; the source's hidden helper never stays registered as a window. */
	private static GeoCeDGStyleApplication.Result apply(AppGeoCeDG app, File source)
			throws Exception {
		int frames = GeoGebraFrame.getInstanceCount();
		GeoCeDGStyleApplication.Result result = onEdt(() -> app.getStyleApplication()
				.apply(source));
		awaitQuiescent(app);
		assertEquals(frames, GeoGebraFrame.getInstanceCount(), "no helper window left");
		return result;
	}

	/** colour | thickness | line type | point size / style | fill opacity */
	private static String style(GeoElement geo) {
		return rgb(geo.getObjectColor()) + "|" + geo.getLineThickness()
				+ "|" + geo.getLineType() + "|" + (geo instanceof GeoPoint p
						? p.getPointSize() + "/" + p.getPointStyle() : "-")
				+ "|" + geo.getAlphaValue();
	}

	private static Map<String, String> styles(AppGeoCeDG app) {
		Map<String, String> styles = new LinkedHashMap<>();
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			styles.put(geo.getLabelSimple(), style(geo));
		}
		return styles;
	}

	/** Geometry, definitions, layers and visibility: never touched by T0. */
	private static Map<String, String> invariants(AppGeoCeDG app) {
		Map<String, String> fixed = new LinkedHashMap<>();
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			fixed.put(geo.getLabelSimple(), geo.toValueString(StringTemplate.xmlTemplate) + "|"
					+ geo.getDefinitionForEditor() + "|" + geo.getLayer() + "|"
					+ geo.isEuclidianVisible() + "|" + geo.getLabelVisible());
		}
		return fixed;
	}

	private static String geo(AppGeoCeDG app, String label) {
		return style(app.getKernel().lookupLabel(label));
	}

	private static String point(GColor color, int size, int pointStyle) {
		return rgb(color) + "|5|0|" + size + "/" + pointStyle + "|0.0";
	}

	private static String line(GColor color, int thickness, int type, double alpha) {
		return rgb(color) + "|" + thickness + "|" + type + "|-|" + alpha;
	}

	private static String rgb(GColor color) {
		return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
	}

	private static void assertStyled(AppGeoCeDG app) {
		for (String label : new String[] {"A", "B", "C", "D"}) {
			assertEquals(point(POINT, 7, EuclidianStyleConstants.POINT_STYLE_CROSS),
					geo(app, label), label);
		}
		assertEquals(line(SEGMENT, 2, EuclidianStyleConstants.LINE_TYPE_DASHED_LONG, 0.0),
				geo(app, "f"));
		assertEquals(line(CIRCLE, 2, EuclidianStyleConstants.LINE_TYPE_DOTTED, 0.25),
				geo(app, "c"));
	}

	private static void select(AppGeoCeDG app, GeoElement... geos) throws Exception {
		onEdt(() -> {
			app.getSelectionManager().setSelectedGeos(new ArrayList<>(List.of(geos)));
		});
	}

	/** File -> Open, File -> Open Recent or File -> New on the user's document. */
	private static void replaceDocument(AppGeoCeDG app, String route, File file)
			throws Exception {
		onEdt(() -> {
			app.setSaved();
			switch (route) {
			case "open" -> ((GuiManagerD) app.getGuiManager()).loadFile(file, false);
			case "recent" -> new LoadFileListener(app, file).actionPerformed(null);
			default -> {
				app.fileNew();
				for (String c : new String[] {"A=(1.6,2.44)", "B=(3.54,2.94)", "c=Circle(A,B)",
						"C=(1.6,4.98)", "D=(5.24,4.84)", "f=Segment(C,D)"}) {
					must(app, c);
				}
				for (String label : OBJECTS) {
					app.getKernel().lookupLabel(label).setObjColor(GColor.BLACK);
				}
			}
			}
		});
		baseline(app);
	}

	@Test
	void f3r2m1EveryApplicablePropertyTransfersFromTheSourceDefaultsInBothDirections()
			throws Exception {
		File plain = plain(dir, "plain");
		File styled = styled(dir, "styled");
		final byte[] styledBytes = Files.readAllBytes(styled.toPath());
		final byte[] plainBytes = Files.readAllBytes(plain.toPath());
		AppGeoCeDG target = opened(plain);
		final Map<String, String> fixed = invariants(target);
		final String uuid = target.getUniqueId();
		final Object units = target.getDocumentUnits().getState();
		final int cursor = undo(target).getCursor();
		// thick -> thin, black -> colour, dot -> cross, default -> non-default
		GeoCeDGStyleApplication.Result result = apply(target, styled);
		assertTrue(result.isApplied());
		assertEquals(6, result.getRestyled());
		assertStyled(target);
		assertEquals(fixed, invariants(target), "geometry, definitions, layers, visibility");
		assertEquals(uuid, target.getUniqueId());
		assertEquals(units, target.getDocumentUnits().getState());
		assertEquals(cursor + 1, undo(target).getCursor(), "one undo step");
		assertFalse(target.isSaved());
		// persisted, not only in memory
		File restyled = save(target, dir, "restyled");
		AppGeoCeDG reopened = opened(restyled);
		assertStyled(reopened);
		// the reverse direction: thin -> thick, colour -> black, cross -> dot,
		// non-default -> the factory default values of the plain document
		ConstructionDefaults factory = app(MM).getKernel().getConstruction()
				.getConstructionDefaults();
		result = apply(reopened, plain);
		assertEquals(6, result.getRestyled());
		for (String label : OBJECTS) {
			GeoElement geo = reopened.getKernel().lookupLabel(label);
			assertEquals(style(factory.getDefaultGeo(reopened.getKernel().getConstruction()
					.getConstructionDefaults().getDefaultType(geo))), style(geo), label);
		}
		assertEquals(point(GColor.newColor(77, 77, 255), 5,
				EuclidianStyleConstants.POINT_STYLE_DOT), geo(reopened, "B"));
		assertEquals(line(GColor.BLACK, 5, EuclidianStyleConstants.LINE_TYPE_FULL, 0.0),
				geo(reopened, "f"));
		assertEquals(line(GColor.BLACK, 5, EuclidianStyleConstants.LINE_TYPE_FULL, 0.0),
				geo(reopened, "c"));
		// the sources are never written
		assertArrayEquals(styledBytes, Files.readAllBytes(styled.toPath()));
		assertArrayEquals(plainBytes, Files.readAllBytes(plain.toPath()));
	}

	@Test
	void f3r2m2AMixedSelectionRestylesExactlyTheSelectedObjectsAndUndoRedoRestoresThem()
			throws Exception {
		File styled = styled(dir, "styled");
		AppGeoCeDG target = opened(plain(dir, "plain"));
		for (String c : new String[] {"s=0", "Q=(s,1)", "Dl={false,{-2,2,true,true}}",
				"Lt=LocusV2(Q,s,Dl)"}) {
			must(target, c);
		}
		baseline(target);
		final GeoElement locus = target.getKernel().lookupLabel("Lt");
		assertTrue(GeoCeDGStyleApplication.isExcluded(locus));
		Map<String, String> before = styles(target);
		final String xml = GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML());
		final int cursor = undo(target).getCursor();
		select(target, target.getKernel().lookupLabel("f"), target.getKernel().lookupLabel("B"),
				target.getKernel().lookupLabel("c"), locus);
		GeoCeDGStyleApplication.Result result = apply(target, styled);
		assertEquals(3, result.getRestyled(), "segment, point and circle; Locus V2 excluded");
		Map<String, String> after = styles(target);
		assertEquals(point(POINT, 7, EuclidianStyleConstants.POINT_STYLE_CROSS), after.get("B"));
		assertEquals(line(SEGMENT, 2, EuclidianStyleConstants.LINE_TYPE_DASHED_LONG, 0.0),
				after.get("f"));
		assertEquals(line(CIRCLE, 2, EuclidianStyleConstants.LINE_TYPE_DOTTED, 0.25),
				after.get("c"));
		for (String label : before.keySet()) {
			if (!List.of("B", "c", "f").contains(label)) {
				assertEquals(before.get(label), after.get(label), "unchanged " + label);
			}
		}
		assertEquals(cursor + 1, undo(target).getCursor(), "one undo step");
		final String xmlAfter = GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML());
		onEdt(() -> {
			undo(target).undo();
		});
		awaitQuiescent(target);
		assertEquals(before, styles(target), "Undo restores every changed property");
		assertEquals(xml, GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML()));
		onEdt(() -> {
			undo(target).redo();
		});
		awaitQuiescent(target);
		assertEquals(after, styles(target), "Redo applies them again");
		assertEquals(xmlAfter, GeoCeDGDocumentInsertion.Certificate.normalize(target.getXML()));
	}

	@Test
	void f3r2s1ObjectsOfAClosedDocumentAreNeverASelection() throws Exception {
		File plain = plain(dir, "plain");
		File styled = styled(dir, "styled");
		for (String route : new String[] {"open", "recent", "new"}) {
			AppGeoCeDG target = opened(plain);
			GeoElement closed = target.getKernel().lookupLabel("f");
			select(target, closed);
			replaceDocument(target, route, plain(dir, "other-" + route));
			// inherited: the selection still holds the closed document's segment
			assertTrue(target.getSelectionManager().getSelectedGeos().contains(closed), route);
			assertNotSame(closed, target.getKernel().lookupLabel("f"), route);
			String closedStyle = style(closed);
			final int cursor = undo(target).getCursor();
			GeoCeDGStyleApplication.Result result = apply(target, styled);
			// nothing of this drawing is selected: every object is restyled
			assertEquals(6, result.getRestyled(), route);
			assertStyled(target);
			assertEquals(closedStyle, style(closed), "the closed document's object: " + route);
			assertEquals(cursor + 1, undo(target).getCursor(), route);
		}
	}

	@Test
	void f3r2s2DetachedObjectsLeaveNoInvisibleUndoStepAndALiveSelectionStillCounts()
			throws Exception {
		File plain = plain(dir, "plain");
		final File styled = styled(dir, "styled");
		File factory = save(construction(), dir, "factory");
		AppGeoCeDG target = opened(plain);
		final GeoElement closed = target.getKernel().lookupLabel("f");
		select(target, closed);
		replaceDocument(target, "recent", factory);
		final String closedStyle = style(closed);
		final Map<String, String> before = styles(target);
		final int cursor = undo(target).getCursor();
		// every live object already has the factory default styles: nothing to change
		GeoCeDGStyleApplication.Result result = apply(target, plain);
		assertTrue(result.isApplied());
		assertEquals(0, result.getRestyled());
		assertEquals(closedStyle, style(closed), "a detached object is never restyled");
		assertEquals(cursor, undo(target).getCursor(), "no undo step without a change");
		assertTrue(target.isSaved());
		assertEquals(before, styles(target));
		// a live object selected next to the detached one: only the live one
		onEdt(() -> {
			target.getSelectionManager().addSelectedGeo(target.getKernel().lookupLabel("B"));
		});
		result = apply(target, styled);
		assertEquals(1, result.getRestyled());
		assertEquals(point(POINT, 7, EuclidianStyleConstants.POINT_STYLE_CROSS),
				geo(target, "B"));
		assertEquals(before.get("f"), geo(target, "f"));
		assertEquals(closedStyle, style(closed));
		assertEquals(cursor + 1, undo(target).getCursor());
	}

	@Test
	void f3r2r1TheDrawingIsRepaintedAndARepeatedApplicationIsANoOp() throws Exception {
		File styled = styled(dir, "styled");
		AppGeoCeDG target = opened(plain(dir, "plain"));
		EuclidianView view = target.getEuclidianView1();
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(WIDTH, HEIGHT);
			view.updateSize();
			view.setCoordSystem(50, 280, 50, 50);
		});
		Predicate<Color> red = c -> c.getRed() > 200 && c.getGreen() < 140 && c.getBlue() < 140;
		Predicate<Color> blue = c -> c.getBlue() > 200 && c.getRed() < 140 && c.getGreen() < 140;
		assertEquals(0, count(paint(view), red));
		assertEquals(0, count(paint(view), blue));
		apply(target, styled);
		assertTrue(count(paint(view), red) > 0, "the segment is drawn in the new colour");
		assertTrue(count(paint(view), blue) > 0, "the circle is drawn in the new colour");
		final int cursor = undo(target).getCursor();
		GeoCeDGStyleApplication.Result again = apply(target, styled);
		assertTrue(again.isApplied());
		assertEquals(0, again.getRestyled(), "nothing left to change");
		assertEquals(cursor, undo(target).getCursor(), "no second undo step");
		onEdt(() -> {
			undo(target).undo();
		});
		awaitQuiescent(target);
		assertEquals(0, count(paint(view), red), "Undo repaints the previous styles");
		assertEquals(0, count(paint(view), blue), "Undo repaints the previous styles");
	}

	/** Paints the view as the window does, without forcing a drawable update. */
	private static BufferedImage paint(EuclidianView view) throws Exception {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		SwingUtilities.invokeAndWait(() -> {
			Graphics2D g = image.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			view.paint(new GGraphics2DD(g));
			g.dispose();
		});
		return image;
	}

	private static int count(BufferedImage image, Predicate<Color> colour) {
		int n = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (colour.test(new Color(image.getRGB(x, y)))) {
					n++;
				}
			}
		}
		return n;
	}
}
