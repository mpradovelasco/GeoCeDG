/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportService;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GPoint;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.kernel.geos.SegmentStyle;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.awt.GBufferedImageD;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * PRE-G9B-R6-plus-F1 orientation cue (specification
 * {@code geocedg/specs/presentation/orientation-cue.md}): the eligibility matrix v1
 * (A1-A17 admitted, X1-X10 excluded) with the kernel sense of
 * {@link GeoLine#getDirection}, faithful reversals, degeneracies, effective visibility,
 * placement and the 10 x 4 hollow arrowhead in the live Graphics 1 paint only; no
 * Graphics 2 or export appearance; no construction, XML, undo, identity or hit-testing
 * change; the global user preference and its two synchronized controls; the catalog.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class,
		PreG9BR6PlusF1OrientationCueTest.MemoryCuePreference.class})
class PreG9BR6PlusF1OrientationCueTest {
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private static final double SCALE = 40;

	/** In-memory store for the cue preference, so tests never touch the real store. */
	static final class MemoryStore implements GeoCeDGPresentationPreferences.Store {
		final Map<String, String> values = new HashMap<>();

		@Override
		public String load(String key) {
			return values.get(key);
		}

		@Override
		public void save(String key, String value) {
			values.put(key, value);
		}
	}

	/** Installs a fresh in-memory cue store around each test. */
	static final class MemoryCuePreference implements BeforeEachCallback, AfterEachCallback {
		private static final ExtensionContext.Namespace NAMESPACE =
				ExtensionContext.Namespace.create(MemoryCuePreference.class);
		static MemoryStore current;

		@Override
		public void beforeEach(ExtensionContext context) {
			current = new MemoryStore();
			context.getStore(NAMESPACE).put("handle",
					GeoCeDGOrientationCuePreference.useStoreForTesting(current));
		}

		@Override
		public void afterEach(ExtensionContext context) throws Exception {
			((AutoCloseable) context.getStore(NAMESPACE).remove("handle")).close();
			current = null;
		}
	}

	// ------------------------------------------------------------- T-F1-SENSE

	@Test
	void everyAdmittedRouteShowsTheKernelSense() throws Exception {
		AppGeoCeDG app = scene();
		final EuclidianView view = app.getEuclidianView1();
		run(app, "A=(-3,-2)");
		run(app, "B=(2,1)");
		run(app, "C=(3,4)");
		run(app, "P=(-2,3)");
		run(app, "Q=(1,-3)");
		run(app, "v=Vector((2,1))");
		run(app, "c=Circle((0,0),2)");
		Map<String, String> admitted = new java.util.LinkedHashMap<>();
		admitted.put("A1", "lAB=Line(A,B)");
		admitted.put("A2", "sAB=Segment(A,B)");
		admitted.put("A3", "rAB=Ray(A,B)");
		admitted.put("A4", "rAv=Ray(A,v)");
		admitted.put("A5", "lPv=Line(P,v)");
		admitted.put("A6", "par=Line(P,lAB)");
		admitted.put("A7", "perp=PerpendicularLine(P,lAB)");
		admitted.put("A8", "perpV=PerpendicularLine(Q,v)");
		admitted.put("A9", "pb=PerpendicularBisector(A,B)");
		admitted.put("A10", "pbS=PerpendicularBisector(sAB)");
		admitted.put("A13p", "refP=Reflect(lAB,P)");
		admitted.put("A13l", "refL=Reflect(lAB,yAxis)");
		admitted.put("A14p", "rotP=Rotate(lAB,30°,P)");
		admitted.put("A14o", "rotO=Rotate(lAB,20°)");
		admitted.put("A15", "tra=Translate(lAB,v)");
		admitted.put("A16", "dil=Dilate(lAB,-1,Q)");
		for (Map.Entry<String, String> route : admitted.entrySet()) {
			assertAdmitted(app, view, route.getKey(), geo(app, route.getValue()));
		}
		run(app, "m=Line(B,C)");
		for (GeoElementND bisector : outputs(app, "AngleBisector(lAB,m)")) {
			assertAdmitted(app, view, "A11", bisector.toGeoElement());
		}
		for (GeoElementND tangent : outputs(app, "Tangent(lAB,c)")) {
			assertAdmitted(app, view, "A12", tangent.toGeoElement());
		}
		for (GeoElementND edge : outputs(app, "Polygon(A,B,C)")) {
			if (edge instanceof GeoSegment) {
				assertAdmitted(app, view, "A2 polygon edge", edge.toGeoElement());
			}
		}
		GeoElementND[] reflectedSegment = outputs(app, "Reflect(sAB,xAxis)");
		assertAdmitted(app, view, "A2 segment image", reflectedSegment[0].toGeoElement());
		// A17: macros whose inner route and orientation-bearing inputs are admitted
		macro(app, "F1Join", new String[] {"A", "B"}, "lAB");
		macro(app, "F1Parallel", new String[] {"P", "lAB"}, "par");
		run(app, "M1=(-4,1)");
		run(app, "M2=(1,-1)");
		assertAdmitted(app, view, "A17 join", geo(app, "mj=F1Join(M1,M2)"));
		assertAdmitted(app, view, "A17 parallel", geo(app, "mp=F1Parallel(M1,rotP)"));
		GeoCeDGOrientationCue.Cue joined = cue(app, "mj");
		assertTrue(joined.worldDx() > 0 && joined.worldDy() < 0, "the inner M1 -> M2 sense");
	}

	@Test
	void everyExcludedRouteKeepsItsKernelSenseButShowsNoCue() throws Exception {
		AppGeoCeDG app = scene();
		final EuclidianView view = app.getEuclidianView1();
		run(app, "A=(-3,-2)");
		run(app, "B=(2,1)");
		run(app, "P=(-2,3)");
		run(app, "T=(2,0)");
		run(app, "c=Circle((0,0),2)");
		run(app, "f(x)=x/2+1");
		run(app, "k=1");
		run(app, "lAB=Line(A,B)");
		Map<String, String> excluded = new java.util.LinkedHashMap<>();
		excluded.put("X1 explicit", "e1: y=2x+1");
		excluded.put("X1 implicit", "e2: 2x-y+1=0");
		excluded.put("X1 parametric", "e3: X=(1,2)+t*(3,4)");
		excluded.put("X2", "e4: k x + y = 1");
		excluded.put("X3 on conic", "Tangent(T,c)");
		excluded.put("X3 external", "Tangent(P,c)");
		excluded.put("X4", "AngleBisector(A,B,P)");
		excluded.put("X5", "Tangent(1,f)");
		excluded.put("X6 parallel", "Line(P,f)");
		excluded.put("X6 perpendicular", "PerpendicularLine(P,f)");
		excluded.put("X7 asymptote", "Asymptote(x^2-y^2=1)");
		excluded.put("X7 polar", "Polar(P,c)");
		excluded.put("X8 parallel to an equation", "Line(P,e2)");
		excluded.put("X8 image of an equation", "Reflect(e2,P)");
		excluded.put("X9 shear", "Shear(lAB,xAxis,1)");
		int checked = 0;
		for (Map.Entry<String, String> route : excluded.entrySet()) {
			for (GeoElementND out : outputs(app, route.getValue())) {
				if (!(out instanceof GeoLine line) || !line.isDefined()) {
					continue;
				}
				double[] d = new double[2];
				line.getDirection(d);
				assertTrue(Math.hypot(d[0], d[1]) > 0, route.getKey() + ": kernel sense kept");
				assertFalse(GeoCeDGOrientationCue.isAdmitted(line), route.getKey());
				assertNull(GeoCeDGOrientationCue.cue(view, line), route.getKey());
				checked++;
			}
		}
		assertTrue(checked >= 17, "every excluded route produced its defined lines: " + checked);
		// X10: axes, list members, native dimensions and macros with an excluded chain
		assertNull(GeoCeDGOrientationCue.cue(view, app.getKernel().lookupLabel("xAxis")));
		run(app, "L={Line(A,P)}");
		GeoElement member = (GeoElement) ((org.geogebra.common.kernel.geos.GeoList) app
				.getKernel().lookupLabel("L")).get(0);
		assertNull(GeoCeDGOrientationCue.cue(view, member), "list member");
		for (GeoElementND out : outputs(app, "AlignedDimension((-4,-5),(4,-5),1)")) {
			assertNull(GeoCeDGOrientationCue.cue(view, out.toGeoElement()), "dimension");
		}
		macro(app, "F1Parallel", new String[] {"P", "lAB"}, "Line(P,lAB)");
		assertNull(GeoCeDGOrientationCue.cue(view, geo(app, "mx=F1Parallel(B,e2)")),
				"macro with an excluded orientation-bearing input");
		assertNotNull(GeoCeDGOrientationCue.cue(view, geo(app, "my=F1Parallel(B,lAB)")),
				"the same macro with an admitted orientation-bearing input");
	}

	// --------------------------------------------- T-F1-REVERSAL, T-F1-DYNAMIC

	@Test
	void reversalsAndMotionFollowTheKernelWithoutSmoothing() throws Exception {
		AppGeoCeDG app = scene();
		EuclidianView view = app.getEuclidianView1();
		run(app, "A=(-3,-2)");
		run(app, "B=(2,1)");
		run(app, "P=(-2,3)");
		run(app, "v=Vector((2,1))");
		GeoLine forward = (GeoLine) geo(app, "f1=Line(A,B)");
		GeoLine backward = (GeoLine) geo(app, "f2=Line(B,A)");
		assertOpposite(cueOf(view, forward), cueOf(view, backward));
		assertOpposite(cueOf(view, geo(app, "s1=Segment(A,B)")),
				cueOf(view, geo(app, "s2=Segment(B,A)")));
		assertOpposite(cueOf(view, geo(app, "r1=Ray(A,B)")),
				cueOf(view, geo(app, "r2=Ray(B,A)")));
		assertOpposite(cueOf(view, geo(app, "p1=Line(P,f1)")),
				cueOf(view, geo(app, "p2=Line(P,f2)")));
		assertOpposite(cueOf(view, geo(app, "q1=PerpendicularLine(P,f1)")),
				cueOf(view, geo(app, "q2=PerpendicularLine(P,f2)")));
		GeoLine pv = (GeoLine) geo(app, "pv=Line(P,v)");
		GeoOrientationSnapshot before = new GeoOrientationSnapshot(cueOf(view, pv));
		GeoVector vector = (GeoVector) app.getKernel().lookupLabel("v");
		vector.setCoords(-2, -1, 0);
		vector.updateCascade();
		assertTrue(before.dot(cueOf(view, pv)) < 0, "v reversed");
		GeoPoint b = (GeoPoint) app.getKernel().lookupLabel("B");
		GeoElement p1 = app.getKernel().lookupLabel("p1");
		b.setCoords(-3, -2, 1);
		b.updateCascade();
		assertNull(GeoCeDGOrientationCue.cue(view, forward), "B on A: undefined");
		assertNull(GeoCeDGOrientationCue.cue(view, p1), "dependent undefined");
		b.setCoords(-6, -4, 1);
		b.updateCascade();
		GeoOrientationSnapshot reversed = new GeoOrientationSnapshot(cueOf(view, forward));
		double[] d = new double[2];
		forward.getDirection(d);
		assertTrue(reversed.worldDot(d) > 0, "the cue follows the current kernel sense");
		assertTrue(d[0] < 0, "B behind A reverses the line");
		b.setCoords(2, 1, 1);
		b.updateCascade();
		assertSame(forward, app.getKernel().lookupLabel("f1"), "identity kept");
		assertSame(p1, app.getKernel().lookupLabel("p1"), "identity kept");
		assertTrue(cueOf(view, forward).worldDx() > 0, "restored");
		// redefinition: a new object with the new sense
		app.getKernel().getAlgebraProcessor().changeGeoElement(forward, "Line(B,A)", true,
				false, app.getDefaultErrorHandler(), null);
		GeoLine redefined = (GeoLine) app.getKernel().lookupLabel("f1");
		assertTrue(cueOf(view, redefined).worldDx() < 0, "redefined sense");
	}

	// -------------------------------------------------------- T-F1-DEGENERATE

	@Test
	void degeneraciesShowNoCueAndNeverThrow() throws Exception {
		AppGeoCeDG app = scene();
		final EuclidianView view = app.getEuclidianView1();
		run(app, "A=(-3,-2)");
		run(app, "B=(-3,-2)");
		run(app, "P=(1,1)");
		run(app, "w=Vector((0,0))");
		run(app, "k=1");
		run(app, "h=0");
		GeoLine line = (GeoLine) geo(app, "l1=Line(A,B)");
		GeoSegment segment = (GeoSegment) geo(app, "s1=Segment(A,B)");
		assertFalse(line.isDefined());
		assertTrue(segment.isDefined(), "a zero-length segment stays defined");
		assertNull(GeoCeDGOrientationCue.cue(view, line));
		assertNull(GeoCeDGOrientationCue.cue(view, segment));
		assertNull(GeoCeDGOrientationCue.cue(view, geo(app, "r1=Ray(A,B)")));
		assertNull(GeoCeDGOrientationCue.cue(view, geo(app, "pb=PerpendicularBisector(A,B)")));
		assertNull(GeoCeDGOrientationCue.cue(view, geo(app, "lw=Line(P,w)")), "zero vector");
		assertNull(GeoCeDGOrientationCue.cue(view, geo(app, "rw=Ray(P,w)")), "zero vector");
		GeoPoint b = (GeoPoint) app.getKernel().lookupLabel("B");
		b.setCoords(-3 + 1e-9, -2, 1);
		b.updateCascade();
		assertNull(GeoCeDGOrientationCue.cue(view, line), "below the kernel tolerance");
		run(app, "n=Line((1,0),(3,1))");
		GeoLine dilated = (GeoLine) geo(app, "d0=Dilate(n,h,P)");
		assertNull(GeoCeDGOrientationCue.cue(view, dilated), "dilation by 0");
		// infinite and NaN coefficients, and a parallel through them
		GeoLine infinite = (GeoLine) geo(app, "q: k x + y = 0");
		run(app, "SetValue(k,1/0)");
		assertTrue(infinite.isDefined(), "the kernel reports infinite coefficients as defined");
		assertNull(GeoCeDGOrientationCue.cue(view, infinite));
		GeoLine parallel = (GeoLine) geo(app, "pq=Line(P,q)");
		assertNull(GeoCeDGOrientationCue.cue(view, parallel), "non-finite coefficients");
		run(app, "SetValue(k,0/0)");
		assertNull(GeoCeDGOrientationCue.cue(view, parallel));
		// parallel inputs of two-line bisectors: one output is undefined
		run(app, "g1=Line((0,0),(1,0))");
		run(app, "g2=Line((0,1),(1,1))");
		int shown = 0;
		for (GeoElementND out : outputs(app, "AngleBisector(g1,g2)")) {
			if (GeoCeDGOrientationCue.cue(view, out.toGeoElement()) != null) {
				shown++;
			}
		}
		assertEquals(1, shown, "parallel lines have one defined bisector");
		app.setOrientationCueShown(true);
		paint(view);
	}

	// ------------------------------------------------------- T-F1-VISIBILITY

	@Test
	void onlyEffectivelyVisibleObjectsCarryACue() throws Exception {
		AppGeoCeDG app = scene();
		EuclidianView view = app.getEuclidianView1();
		GeoSegment segment = (GeoSegment) geo(app, "s=Segment((-4,0),(4,0))");
		assertNotNull(GeoCeDGOrientationCue.cue(view, segment));
		segment.setEuclidianVisible(false);
		segment.updateRepaint();
		assertNull(GeoCeDGOrientationCue.cue(view, segment), "hidden object");
		segment.setEuclidianVisible(true);
		segment.updateRepaint();
		segment.setLayer(4);
		app.getLayerWorkspace().setLayerHidden(4, true);
		assertNull(GeoCeDGOrientationCue.cue(view, segment), "hidden layer");
		app.getLayerWorkspace().setLayerHidden(4, false);
		assertNotNull(GeoCeDGOrientationCue.cue(view, segment));
		segment.setLineThickness(0);
		segment.updateRepaint();
		assertNull(GeoCeDGOrientationCue.cue(view, segment), "thickness 0");
		segment.setLineThickness(5);
		segment.setAuxiliaryObject(true);
		segment.updateRepaint();
		assertNotNull(GeoCeDGOrientationCue.cue(view, segment), "auxiliary objects are drawn");
		run(app, "show=false");
		segment.setShowObjectCondition((org.geogebra.common.kernel.geos.GeoBoolean) app
				.getKernel().lookupLabel("show"));
		segment.updateRepaint();
		assertNull(GeoCeDGOrientationCue.cue(view, segment), "condition to show is false");
		segment.setShowObjectCondition(null);
		segment.updateRepaint();
		segment.setDecorationType(GeoElementND.DECORATION_SEGMENT_ONE_ARROW);
		assertNull(GeoCeDGOrientationCue.cue(view, segment), "midpoint arrow");
		segment.setDecorationType(GeoElementND.DECORATION_SEGMENT_TWO_ARROWS);
		assertNull(GeoCeDGOrientationCue.cue(view, segment), "midpoint arrows");
		segment.setDecorationType(GeoElementND.DECORATION_SEGMENT_THREE_ARROWS);
		assertNull(GeoCeDGOrientationCue.cue(view, segment), "midpoint arrows");
		segment.setDecorationType(GeoElementND.DECORATION_NONE);
		segment.setEndStyle(SegmentStyle.ARROW_OUTLINE);
		segment.updateRepaint();
		assertNotNull(GeoCeDGOrientationCue.cue(view, segment),
				"endpoint styles keep their own semantics and do not suppress");
		segment.setVisibility(view.getViewID(), false);
		assertNull(GeoCeDGOrientationCue.cue(view, segment), "not visible in Graphics 1");
	}

	// -------------------------------------------------------- T-F1-PLACEMENT

	@Test
	void placementIsDeterministicAndScreenOnly() throws Exception {
		AppGeoCeDG app = scene();
		EuclidianView view = app.getEuclidianView1();
		GeoOrientationCue segment = new GeoOrientationCue(cueOf(view,
				geo(app, "s=Segment((-2,1),(2,1))")));
		assertEquals(400, segment.cue.anchorX(), 1e-9);
		assertEquals(300 - SCALE, segment.cue.anchorY(), 1e-9);
		assertEquals(1, segment.cue.unitX(), 1e-12);
		GeoOrientationCue ray = new GeoOrientationCue(cueOf(view,
				geo(app, "r=Ray((0,-1),(0,-3))")));
		assertEquals(400, ray.cue.anchorX(), 1e-9);
		assertEquals(300 + SCALE + 3 * GeoCeDGOrientationCue.LENGTH, ray.cue.anchorY(), 1e-9);
		assertEquals(1, ray.cue.unitY(), 1e-12, "screen y grows downwards");
		GeoOrientationCue line = new GeoOrientationCue(cueOf(view,
				geo(app, "l=Line((-20,2),(20,2))")));
		assertEquals(400, line.cue.anchorX(), 1e-9);
		assertEquals(300 - 2 * SCALE, line.cue.anchorY(), 1e-9);
		// a ray whose start is off screen: nearest visible point to the centre
		GeoOrientationCue far = new GeoOrientationCue(cueOf(view,
				geo(app, "rf=Ray((-60,3),(-59,3))")));
		assertEquals(400, far.cue.anchorX(), 1e-9);
		// a line cutting a corner: the anchor is clamped onto the inset chord
		GeoLine corner = (GeoLine) geo(app, "lc=Line((9,6),(10,5))");
		GeoCeDGOrientationCue.Cue cornerCue = cueOf(view, corner);
		assertTrue(cornerCue.anchorX() >= GeoCeDGOrientationCue.INSET - 1e-9
				&& cornerCue.anchorX() <= WIDTH - GeoCeDGOrientationCue.INSET + 1e-9);
		assertTrue(cornerCue.anchorY() >= GeoCeDGOrientationCue.INSET - 1e-9
				&& cornerCue.anchorY() <= HEIGHT - GeoCeDGOrientationCue.INSET + 1e-9);
		double worldX = view.toRealWorldCoordX(cornerCue.anchorX());
		double worldY = view.toRealWorldCoordY(cornerCue.anchorY());
		assertEquals(0, corner.getX() * worldX + corner.getY() * worldY + corner.getZ(), 1e-9,
				"the anchor lies on the line");
		// too short on screen: 0.5 units = 20 px < 40 px
		assertNull(GeoCeDGOrientationCue.cue(view, geo(app, "tiny=Segment((0,0),(0.5,0))")));
		// pan, zoom and an unequal axis ratio move the cue but never reverse it
		double[] before = {segment.cue.worldDx(), segment.cue.worldDy()};
		SwingUtilities.invokeAndWait(() -> view.setCoordSystem(150, 420, 25, 60));
		GeoCeDGOrientationCue.Cue moved = cueOf(view, app.getKernel().lookupLabel("s"));
		assertArrayEquals(before, new double[] {moved.worldDx(), moved.worldDy()});
		GeoLine oblique = (GeoLine) geo(app, "ob=Line((0,0),(1,1))");
		GeoCeDGOrientationCue.Cue skew = cueOf(view, oblique);
		assertEquals(25 / Math.hypot(25, 60), skew.unitX(), 1e-12);
		assertEquals(-60 / Math.hypot(25, 60), skew.unitY(), 1e-12);
		// resize
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(300, 200);
			view.updateSize();
		});
		GeoCeDGOrientationCue.Cue small = GeoCeDGOrientationCue.cue(view, oblique);
		if (small != null) {
			assertTrue(small.anchorX() <= 300 - GeoCeDGOrientationCue.INSET + 1e-9);
		}
		// logarithmic axes make straight lines curves on screen: no cue
		view.setLogAxis(EuclidianView.AXIS_X, true, false);
		assertTrue(GeoCeDGOrientationCue.cues(view).isEmpty());
		assertNull(GeoCeDGOrientationCue.cue(view, oblique));
	}

	// ---------------------------------------------- T-F1-RENDERING, T-F1-HIT

	@Test
	void theLivePaintDrawsAHollowTenByFourArrowheadOnly() throws Exception {
		AppGeoCeDG app = scene();
		EuclidianView view = app.getEuclidianView1();
		GeoElement segment = geo(app, "s=Segment((-4,-1),(4,-1))");
		segment.setObjColor(GColor.newColor(220, 0, 0));
		segment.setLineThickness(1);
		segment.setLabelVisible(false);
		segment.updateRepaint();
		GeoCeDGOrientationCue.Cue cue = cueOf(view, segment);
		double[] t = cue.triangle();
		assertEquals(10, Math.hypot((t[2] + t[4]) / 2 - t[0], (t[3] + t[5]) / 2 - t[1]),
				1e-9, "axial length");
		assertEquals(4, Math.hypot(t[2] - t[4], t[3] - t[5]), 1e-9, "base width");
		BufferedImage off = paint(view);
		app.setOrientationCueShown(true);
		BufferedImage on = paint(view);
		int changed = 0;
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				if (off.getRGB(x, y) != on.getRGB(x, y)) {
					changed++;
					assertTrue(Math.abs(x - cue.anchorX()) <= 9 && Math.abs(y - cue.anchorY()) <= 9,
							"only the arrowhead changes the picture: " + x + "," + y);
				}
			}
		}
		assertTrue(changed > 0, "the arrowhead is drawn");
		int red = 0;
		int white = 0;
		for (int x = (int) cue.anchorX() - 8; x <= cue.anchorX() + 8; x++) {
			Color colour = new Color(on.getRGB(x, (int) Math.round(cue.anchorY()) - 1));
			if (colour.getRed() > 180 && colour.getGreen() < 120) {
				red++;
			}
			if (colour.getRed() > 240 && colour.getGreen() > 240 && colour.getBlue() > 240) {
				white++;
			}
		}
		assertTrue(red > 0, "outline in the object colour");
		assertTrue(white > 0, "hollow interior and background around it");
		// hit testing and selection are unchanged
		GPoint at = new GPoint((int) cue.anchorX(), (int) cue.anchorY());
		app.setOrientationCueShown(false);
		List<GeoElement> hitsOff = hits(view, at);
		app.setOrientationCueShown(true);
		assertEquals(hitsOff, hits(view, at));
		GPoint beside = new GPoint((int) cue.anchorX(), (int) cue.anchorY() - 8);
		app.setOrientationCueShown(false);
		List<GeoElement> besideOff = hits(view, beside);
		app.setOrientationCueShown(true);
		assertEquals(besideOff, hits(view, beside), "the arrowhead has no hit area");
	}

	// -------------------------------------------------------------- T-F1-VIEWS

	@Test
	void graphicsTwoNeverShowsTheCue() throws Exception {
		AppGeoCeDG app = scene();
		EuclidianViewD second = ((GuiManagerD) app.getGuiManager()).getEuclidianView2(1);
		assertFalse(second instanceof GeoCeDGEuclidianView, "an upstream view class");
		second.attachView();
		sized(second);
		GeoElement segment = geo(app, "s=Segment((-4,0),(4,0))");
		// a first view flag replaces the implicit Graphics 1 visibility: keep both
		segment.setVisibility(app.getEuclidianView1().getViewID(), true);
		segment.setVisibility(second.getViewID(), true);
		second.add(segment);
		paint(second);
		// the first paint of a freshly attached host view settles its own decorations
		BufferedImage off = paint(second);
		assertImagesEqual(off, paint(second), "Graphics 2 paint is deterministic");
		app.setOrientationCueShown(true);
		assertImagesEqual(off, paint(second), "Graphics 2");
		assertNotNull(GeoCeDGOrientationCue.cue(app.getEuclidianView1(), segment));
	}

	// ------------------------------------------------------------- T-F1-EXPORT

	@Test
	void noExportPrintOrClipboardRouteShowsTheCue() throws Exception {
		AppGeoCeDG app = scene();
		EuclidianView view = app.getEuclidianView1();
		geo(app, "s=Segment((-4,-1),(4,-1))");
		geo(app, "l=Line((-3,2),(3,3))");
		geo(app, "r=Ray((1,-3),(3,-2))");
		assertEquals(3, GeoCeDGOrientationCue.cues(view).size());
		Map<String, Object> off = exports(app, view);
		app.setOrientationCueShown(true);
		assertTrue(app.isOrientationCueShown());
		assertImagesEqual(paint(view), paint(view), "live paint is deterministic");
		Map<String, Object> on = exports(app, view);
		for (String route : off.keySet()) {
			Object a = off.get(route);
			Object b = on.get(route);
			if (a instanceof BufferedImage image) {
				assertImagesEqual(image, (BufferedImage) b, route);
			} else if (a instanceof byte[] bytes) {
				assertArrayEquals(bytes, (byte[]) b, route);
			} else {
				assertEquals(a, b, route);
			}
		}
	}

	// -------------------------------------------------------- T-F1-NO-MUTATION

	@Test
	void togglingChangesNoConstructionXmlUndoIdentityOrSavedState() throws Exception {
		AppGeoCeDG app = scene();
		final GeoElement line = geo(app, "l=Line((-3,2),(3,3))");
		GeoElement segment = geo(app, "s=Segment((-4,-1),(4,-1))");
		UndoManagerD undo = PreG9BR6PlusA1LayerWorkspaceTest.undo(app);
		PreG9BR6PlusA1LayerWorkspaceTest.baseline(undo);
		app.setSaved();
		String construction = PreG9BR6PlusA1LayerWorkspaceTest.constructionXml(app);
		String xml = app.getXML();
		boolean undoPossible = undo.undoPossible();
		boolean redoPossible = undo.redoPossible();
		final String style = segment.getXML();
		for (int i = 0; i < 3; i++) {
			app.toggleOrientationCue();
			paint(app.getEuclidianView1());
		}
		assertTrue(app.isOrientationCueShown());
		assertEquals(construction, PreG9BR6PlusA1LayerWorkspaceTest.constructionXml(app));
		assertEquals(xml, app.getXML());
		assertEquals(undoPossible, undo.undoPossible());
		assertEquals(redoPossible, undo.redoPossible());
		assertTrue(app.isSaved(), "the document is not modified");
		assertSame(line, app.getKernel().lookupLabel("l"));
		assertSame(segment, app.getKernel().lookupLabel("s"));
		assertEquals(style, segment.getXML());
		assertFalse(xml.contains("orientation-cue"), "nothing reaches the document");
		assertFalse(app.getPreferencesXML().contains("orientation-cue"),
				"nothing reaches the preferences XML");
	}

	// ------------------------------------------------------ T-F1-PREFERENCE

	@Test
	void theGlobalPreferenceIsOffByDefaultPersistentAndDocumentIndependent()
			throws Exception {
		MemoryStore store = MemoryCuePreference.current;
		AppGeoCeDG app = scene();
		assertFalse(app.isOrientationCueShown(), "default OFF");
		assertTrue(store.values.isEmpty(), "reading never writes");
		app.setOrientationCueShown(true);
		assertEquals("shown", store.values.get(GeoCeDGOrientationCuePreference.KEY));
		AppGeoCeDG restarted = G9U1TestApp.create();
		assertTrue(restarted.isOrientationCueShown(), "survives a restart");
		restarted.setOrientationCueShown(false);
		assertEquals("hidden", store.values.get(GeoCeDGOrientationCuePreference.KEY));
		store.values.put(GeoCeDGOrientationCuePreference.KEY, "true");
		AppGeoCeDG invalid = G9U1TestApp.create();
		assertFalse(invalid.isOrientationCueShown(), "an invalid value reads as OFF");
		assertEquals("true", store.values.get(GeoCeDGOrientationCuePreference.KEY),
				"and is not rewritten");
		app.setOrientationCueShown(true);
		app.setSaved();
		app.fileNew();
		assertTrue(app.isOrientationCueShown(), "New keeps the preference");
		geo(app, "l=Line((-3,2),(3,3))");
		app.setXML(app.getXML(), true);
		assertTrue(app.isOrientationCueShown(), "Open keeps the preference");
		assertNotNull(GeoCeDGOrientationCue.cue(app.getEuclidianView1(),
				app.getKernel().lookupLabel("l")));
	}

	// ---------------------------------------------------------- T-F1-UI-SYNC

	@Test
	void theMenuActionAndThePanelCheckboxShareOnePreference() throws Exception {
		AppGeoCeDG app = scene();
		GeoCeDGActionRegistry registry = ((GuiManagerGeoCeDG) app.getGuiManager())
				.getActionRegistry();
		Action action = registry.get("presentation.orientation-cue");
		assertNotNull(action);
		registry.refresh();
		assertEquals(Boolean.FALSE, action.getValue(Action.SELECTED_KEY));
		GeoCeDGPresentationOptionsPanel panel =
				(GeoCeDGPresentationOptionsPanel) app.newProductPresentationOptionsPanel();
		JCheckBox checkbox = panel.orientationCueControl();
		assertFalse(checkbox.isSelected());
		int combos = 0;
		for (java.awt.Component child : panel.getComponents()) {
			if (child instanceof JComboBox) {
				combos++;
			}
		}
		assertEquals(6, combos, "the presentation-size controls are preserved");
		String language = app.getLocale().getLanguage();
		assertEquals(GeoCeDGProfile.getText("Presentation.OrientationCue", language),
				checkbox.getText());
		action.actionPerformed(new java.awt.event.ActionEvent(app, 0,
				"presentation.orientation-cue"));
		assertTrue(app.isOrientationCueShown(), "the View menu action turns it on");
		assertTrue(checkbox.isSelected(), "the checkbox follows");
		assertEquals(Boolean.TRUE, action.getValue(Action.SELECTED_KEY));
		SwingUtilities.invokeAndWait(checkbox::doClick);
		assertFalse(app.isOrientationCueShown(), "the checkbox turns it off");
		assertEquals(Boolean.FALSE, action.getValue(Action.SELECTED_KEY),
				"the menu checkbox follows");
		SwingUtilities.invokeAndWait(checkbox::doClick);
		assertTrue(app.isOrientationCueShown());
		assertEquals(GeoCeDGProfile.getText("geocedg.action.OrientationCue.name", language),
				action.getValue(Action.NAME));
	}

	// ----------------------------------------------------------- T-F1-PROFILE

	@Test
	void theCatalogHoldsOneDeclarativePreferenceActionInTheViewMenu() throws Exception {
		assertEquals(133, GeoCeDGProfile.getActions().size());
		GeoCeDGProfile.ActionDefinition definition = GeoCeDGProfile.getActions().stream()
				.filter(a -> "presentation.orientation-cue".equals(a.id())).findFirst()
				.orElseThrow();
		assertEquals("product-action", definition.kind());
		assertEquals("geocedg.presentation.orientation-cue", definition.target());
		assertNull(definition.mode());
		JSONObject profile = new JSONObject(new String(GeoCeDGProfile.class
				.getResourceAsStream("application-profile.yml").readAllBytes(),
				java.nio.charset.StandardCharsets.UTF_8));
		JSONObject record = null;
		JSONArray actions = profile.getJSONArray("actions");
		for (int i = 0; i < actions.length(); i++) {
			if ("presentation.orientation-cue".equals(actions.getJSONObject(i).getString("id"))) {
				record = actions.getJSONObject(i);
			}
		}
		assertNotNull(record);
		assertEquals("preference-only", record.getString("effect_profile_id"));
		assertEquals("product-only", record.getString("availability_profile_id"));
		assertEquals("action-only", record.getString("command_surface_profile_id"));
		assertTrue(contains(group(profile, "presentation_groups", "view-visibility-style")
				.getJSONArray("action_ids"), "presentation.orientation-cue"), "View menu");
		assertTrue(contains(group(profile, "clusters", "presentation-visibility-style")
				.getJSONArray("menu_action_ids"), "presentation.orientation-cue"), "cluster");
		JSONObject texts = profile.getJSONObject("localized_text");
		for (String field : new String[] {"name", "short_help", "long_help", "status",
				"error"}) {
			JSONObject text = texts.getJSONObject("geocedg.action.OrientationCue." + field);
			assertFalse(text.getString("en").isEmpty());
			assertFalse(text.getString("es").isEmpty());
		}
		assertEquals("Mostrar indicación de orientación", texts
				.getJSONObject("geocedg.action.OrientationCue.name").getString("es"));
		for (String key : new String[] {"Presentation.OrientationCue",
				"Presentation.OrientationCue.Title", "Presentation.OrientationCue.Help"}) {
			assertFalse(texts.getJSONObject(key).getString("es").isEmpty(), key);
		}
	}

	// ----------------------------------------------------------- T-F1-CLASSIC

	@Test
	void classicHasNoCueAndNeverReadsThePreference() throws Exception {
		MemoryCuePreference.current.values.put(GeoCeDGOrientationCuePreference.KEY, "shown");
		org.geogebra.desktop.headless.AppDNoGui classic =
				new org.geogebra.desktop.headless.AppDNoGui(
						new org.geogebra.desktop.main.LocalizationD(3), true,
						new org.geogebra.common.main.settings.config.AppConfigDefault());
		assertFalse(classic.getEuclidianView1() instanceof GeoCeDGEuclidianView);
		assertEquals(1, MemoryCuePreference.current.values.size());
	}

	// ---------------------------------------------------------------- helpers

	private static AppGeoCeDG scene() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		EuclidianView view = sized(app.getEuclidianView1());
		view.setShowAxes(false, false);
		view.showGrid(false);
		return app;
	}

	static EuclidianView sized(EuclidianView view) throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			((EuclidianViewD) view).getJPanel().setSize(WIDTH, HEIGHT);
			view.updateSize();
			view.setCoordSystem(WIDTH / 2.0, HEIGHT / 2.0, SCALE, SCALE);
		});
		return view;
	}

	private static void run(AppGeoCeDG app, String command) {
		outputs(app, command);
	}

	/** Evaluates a command; a command error fails instead of opening a modal dialog. */
	private static GeoElementND[] outputs(AppGeoCeDG app, String command) {
		try (org.mockito.MockedStatic<javax.swing.JOptionPane> pane =
				org.mockito.Mockito.mockStatic(javax.swing.JOptionPane.class)) {
			GeoElementND[] result = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommand(command, false);
			pane.verifyNoInteractions();
			assertNotNull(result, command);
			return result;
		}
	}

	private static GeoElement geo(AppGeoCeDG app, String command) {
		GeoElementND[] result = outputs(app, command);
		assertTrue(result.length > 0, command);
		return result[0].toGeoElement();
	}

	private static void macro(AppGeoCeDG app, String name, String[] inputs, String output)
			throws Exception {
		GeoElement[] in = new GeoElement[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			in[i] = app.getKernel().lookupLabel(inputs[i]);
		}
		GeoElement out = app.getKernel().lookupLabel(output);
		if (out == null) {
			out = geo(app, name + "Out=" + output);
		}
		app.getKernel().addMacro(new Macro(app.getKernel(), name, in, new GeoElement[] {out}));
	}

	private static GeoCeDGOrientationCue.Cue cue(AppGeoCeDG app, String label) {
		return cueOf(app.getEuclidianView1(), app.getKernel().lookupLabel(label));
	}

	private static GeoCeDGOrientationCue.Cue cueOf(EuclidianView view, GeoElement geo) {
		GeoCeDGOrientationCue.Cue cue = GeoCeDGOrientationCue.cue(view, geo);
		assertNotNull(cue, geo.getLabelSimple());
		return cue;
	}

	private static void assertAdmitted(AppGeoCeDG app, EuclidianView view, String route,
			GeoElement geo) {
		assertTrue(geo instanceof GeoLine, route);
		GeoLine line = (GeoLine) geo;
		assertTrue(line.isDefined(), route);
		assertTrue(GeoCeDGOrientationCue.isAdmitted(line), route);
		GeoCeDGOrientationCue.Cue cue = GeoCeDGOrientationCue.cue(view, line);
		assertNotNull(cue, route + " is drawn");
		assertTrue(dot(cue, line) > 0, route + ": the cue shows getDirection");
		double[] path = pathDisplacement(line);
		assertTrue(path[0] * cue.worldDx() + path[1] * cue.worldDy() > 0,
				route + ": the cue agrees with the path parameter");
		double sx = cue.worldDx() * view.getXscale();
		double sy = -cue.worldDy() * view.getYscale();
		double norm = Math.hypot(sx, sy);
		assertEquals(sx / norm, cue.unitX(), 1e-9, route);
		assertEquals(sy / norm, cue.unitY(), 1e-9, route);
	}

	private static double dot(GeoCeDGOrientationCue.Cue cue, GeoLine line) {
		double[] d = new double[2];
		line.getDirection(d);
		return cue.worldDx() * d[0] + cue.worldDy() * d[1];
	}

	private static double[] pathDisplacement(GeoLine line) {
		double t0 = line instanceof GeoSegment ? 0.25 : 0.5;
		double t1 = line instanceof GeoSegment ? 0.75 : 1.5;
		double[] p0 = pathPoint(line, t0);
		double[] p1 = pathPoint(line, t1);
		return new double[] {p1[0] - p0[0], p1[1] - p0[1]};
	}

	private static double[] pathPoint(GeoLine line, double t) {
		GeoPoint p = new GeoPoint(line.getConstruction());
		p.setPath(line);
		p.getPathParameter().t = t;
		line.pathChanged(p);
		p.updateCoords();
		return new double[] {p.getInhomX(), p.getInhomY()};
	}

	private static void assertOpposite(GeoCeDGOrientationCue.Cue a, GeoCeDGOrientationCue.Cue b) {
		assertTrue(a.worldDx() * b.worldDx() + a.worldDy() * b.worldDy() < 0,
				a.geo().getLabelSimple() + " / " + b.geo().getLabelSimple());
		assertTrue(a.unitX() * b.unitX() + a.unitY() * b.unitY() < 0);
	}

	/** Snapshot of a cue sense for later comparison. */
	private record GeoOrientationSnapshot(GeoCeDGOrientationCue.Cue cue) {
		double dot(GeoCeDGOrientationCue.Cue other) {
			return cue.worldDx() * other.worldDx() + cue.worldDy() * other.worldDy();
		}

		double worldDot(double[] d) {
			return cue.worldDx() * d[0] + cue.worldDy() * d[1];
		}
	}

	/** A cue held for placement assertions. */
	private record GeoOrientationCue(GeoCeDGOrientationCue.Cue cue) {
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

	private static void assertImagesEqual(BufferedImage a, BufferedImage b, String route) {
		assertEquals(a.getWidth(), b.getWidth(), route);
		assertEquals(a.getHeight(), b.getHeight(), route);
		for (int y = 0; y < a.getHeight(); y++) {
			for (int x = 0; x < a.getWidth(); x++) {
				if (a.getRGB(x, y) != b.getRGB(x, y)) {
					throw new AssertionError(route + " differs at " + x + "," + y);
				}
			}
		}
	}

	private static List<GeoElement> hits(EuclidianView view, GPoint at) {
		view.setHits(at, PointerEventType.MOUSE);
		return new ArrayList<>(view.getHits());
	}

	private static Map<String, Object> exports(AppGeoCeDG app, EuclidianView view)
			throws Exception {
		Map<String, Object> out = new java.util.LinkedHashMap<>();
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

	private static JSONObject group(JSONObject profile, String array, String id)
			throws JSONException {
		JSONArray groups = profile.getJSONArray(array);
		for (int i = 0; i < groups.length(); i++) {
			if (id.equals(groups.getJSONObject(i).getString("id"))) {
				return groups.getJSONObject(i);
			}
		}
		throw new AssertionError(id);
	}

	private static boolean contains(JSONArray values, String value) throws JSONException {
		for (int i = 0; i < values.length(); i++) {
			if (value.equals(values.getString(i))) {
				return true;
			}
		}
		return false;
	}
}
