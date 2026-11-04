/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Public AWT listener path with OS-shaped drag events; distinct from physical GUI smoke. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class R5SemanticPointPointerDesktopTest {

	private record Fixture(AppGeoCeDG app, PersistentGeoId pointId,
			PersistentGeoId sourceId, LocusSemanticAddress2D initial) {
		GeoPoint point() { return (GeoPoint) app.getKernel().lookupLabel("P"); }
		GeoPoint dependent() { return (GeoPoint) app.getKernel().lookupLabel("Dependent"); }
		GeoLocusV2 source() { return (GeoLocusV2) app.getKernel().lookupLabel("L"); }
	}
	private record World(double x, double y) { }

	@BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }

	private static GeoElement eval(AppGeoCeDG app, String command) {
		return G9U1SemanticPointInteractionTest.eval(app, command);
	}
	private static LocusSemanticAddress2D address(GeoPoint point) {
		return AlgoSemanticLocusPoint2D.requireSemanticParent(point).getCurrentSemanticAddress();
	}
	private static Fixture fixture(boolean meet) {
		var app = G9U1SemanticPointInteractionTest.app();
		List<String> commands = meet ? List.of("c=Circle((0,0),1)", "N=Point(c)",
				"horizontal=Line(N,xAxis)", "vertical=Line(N,yAxis)",
				"Trace=Intersect(horizontal,vertical)", "L=LocusV2(Trace,N)")
				: List.of("Focus=(0,1/4)", "directrix=Line((-2,-1/4),(2,-1/4))",
						"segment=Segment((-2,-1/4),(2,-1/4))", "N=Point(segment)",
						"normal=PerpendicularLine(N,directrix)", "chord=Line(Focus,N)",
						"Middle=Midpoint(Focus,N)", "bisector=PerpendicularLine(Middle,chord)",
						"Trace=Intersect(normal,bisector)", "L=LocusV2(Trace,N)");
		for (var command : commands) { eval(app, command); }
		var source = (GeoLocusV2) app.getKernel().lookupLabel("L");
		double parameter = meet ? .11 : .6;
		double x = meet ? Math.cos(parameter) : .4;
		double y = meet ? Math.sin(parameter) : .16;
		GeoPoint point;
		try (var offer = new GeoCeDGReferencePlacement(source, x, y,
				LocusPointInteractionPolicy2D.initial(.1))) {
			assertFalse(offer.getChoices().isEmpty(), offer.getFailure());
			point = offer.commit(offer.getChoices().stream().min(Comparator.comparingDouble(
					choice -> Math.abs(choice.getAddress().getCanonicalParameter() - parameter))).orElseThrow());
		}
		point.setLabel("P"); eval(app, "Dependent=P+(2,3)");
		for (var geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			geo.setEuclidianVisible(geo == point || geo == source); geo.updateRepaint();
		}
		app.setMode(EuclidianConstants.MODE_MOVE);
		assertTrue(GeoCeDGPointInteraction.owns(point)); assertFalse(point.isLocked());
		var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
		return new Fixture(app, registry.getPersistentGeoId(point), source.getPersistentLocusId(), address(point));
	}
	private static double hitRadius(AppGeoCeDG app) {
		var view = app.getEuclidianView1();
		return Math.max(1, app.getCapturingThreshold(PointerEventType.MOUSE))
				* Math.hypot(view.getInvXscale(), view.getInvYscale());
	}
	private static World captured(AppGeoCeDG app, double x, double y) {
		var view = app.getEuclidianView1();
		return new World(view.toRealWorldCoordX(view.toScreenCoordX(x)),
				view.toRealWorldCoordY(view.toScreenCoordY(y)));
	}
	private static void mouse(AppGeoCeDG app, int id, double x, double y) {
		var view = app.getEuclidianView1(); var canvas = view.getJPanel();
		boolean held = id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_DRAGGED;
		int button = id == MouseEvent.MOUSE_DRAGGED || id == MouseEvent.MOUSE_MOVED
				? MouseEvent.NOBUTTON : MouseEvent.BUTTON1;
		canvas.dispatchEvent(new MouseEvent(canvas, id, System.currentTimeMillis(),
				held ? InputEvent.BUTTON1_DOWN_MASK : 0,
				view.toScreenCoordX(x), view.toScreenCoordY(y),
				id == MouseEvent.MOUSE_DRAGGED || id == MouseEvent.MOUSE_MOVED ? 0 : 1,
				false, button));
	}
	private static void press(Fixture fixture) {
		mouse(fixture.app, MouseEvent.MOUSE_MOVED, fixture.point().inhomX, fixture.point().inhomY);
		mouse(fixture.app, MouseEvent.MOUSE_PRESSED, fixture.point().inhomX, fixture.point().inhomY);
		assertTrue(fixture.app.getSelectionManager().getSelectedGeos().contains(fixture.point()));
	}
	private static void dragAndRelease(Fixture fixture, double x, double y) {
		press(fixture);
		// Real drags may deliver the first sample at the same quantized position.
		mouse(fixture.app, MouseEvent.MOUSE_DRAGGED, fixture.point().inhomX, fixture.point().inhomY);
		mouse(fixture.app, MouseEvent.MOUSE_DRAGGED, x, y);
		mouse(fixture.app, MouseEvent.MOUSE_RELEASED, x, y);
	}
	private static void assertIdentityAndDependent(Fixture fixture) {
		var point = fixture.point(); var registry = fixture.app.getKernel().getConstruction().getSpatialIdentityRegistry();
		assertEquals(fixture.pointId, registry.getPersistentGeoId(point));
		assertEquals(fixture.sourceId, address(point).getSourceLocusId());
		assertEquals(fixture.initial.getBranchKey(), address(point).getBranchKey());
		assertEquals(fixture.initial.getComponentLineageKey(), address(point).getComponentLineageKey());
		assertEquals(fixture.initial.getPeriodicLift(), address(point).getPeriodicLift());
		assertEquals(point.inhomX + 2, fixture.dependent().inhomX, 0);
		assertEquals(point.inhomY + 3, fixture.dependent().inhomY, 0);
		assertTrue(GeoCeDGPointInteraction.owns(point));
	}
	private static void compareOldHitRadiusAndNewMovement(boolean meet) throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var fixture = fixture(meet); var app = fixture.app; var point = fixture.point();
			var target = captured(app, meet ? 1.3 : .85, point.inhomY);
			var before = address(point); String xml = app.getXML();
			var oldPolicy = LocusPointInteractionPolicy2D.initial(hitRadius(app));
			var previous = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					target.x, target.y, oldPolicy, LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point));
			assertFalse(previous.isEstablished());
			assertEquals(LocusPointContinuation2D.Status.FORWARD_REJECTED, previous.getStatus());
			assertEquals("LOCAL_STRICT_PROJECTION_CHART", previous.getMethod(),
					"The scientific local chart exists; the old final proximity ceiling refused it");
			assertEquals(before, address(point)); assertEquals(xml, app.getXML());
			double radius = Math.nextUp(Math.hypot(point.inhomX - target.x, point.inhomY - target.y)
					+ hitRadius(app));
			var proof = LocusPointContinuation2D.resolve(fixture.source(), before, target.x, target.y,
					LocusPointInteractionPolicy2D.initial(radius));
			assertTrue(proof.isEstablished(), proof.getDiagnostic());
			assertEquals("LOCAL_STRICT_PROJECTION_CHART", proof.getMethod());
			assertTrue(proof.getIntervalPasses() <= oldPolicy.getWorkBudget().getMaximumSubdivisions());
			int count = app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
			dragAndRelease(fixture, target.x, target.y);
			assertNotEquals(before, address(point)); assertSame(point, fixture.point());
			assertEquals(count, app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
			assertTrue(Math.hypot(point.inhomX - target.x, point.inhomY - target.y) > hitRadius(app));
			assertIdentityAndDependent(fixture);
		});
	}

	@Test void parabolaOffTraceDragUsesIndependentMovementPolicyWithoutWeakeningTheChart() throws Exception {
		compareOldHitRadiusAndNewMovement(false);
	}
	@Test void meetOffTraceDragUsesIndependentMovementPolicyWithoutWeakeningTheChart() throws Exception {
		compareOldHitRadiusAndNewMovement(true);
	}
	@Test void offTraceMotionBothWaysHasOneGestureUndoAndCanDragAfterNativeReopen(@TempDir Path directory)
			throws Exception {
		var holder = new AtomicReference<Fixture>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var fixture = fixture(false); fixture.app.getKernel().initUndoInfo(); holder.set(fixture);
		});
		var fixture = holder.get(); var app = fixture.app;
		var undo = app.getKernel().getConstruction().getUndoManager();
		Cu7M08SDesktopEventsTest.await(() -> undo.getHistorySize() == 0);
		var moved = new AtomicReference<LocusSemanticAddress2D>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			press(fixture);
			mouse(app, MouseEvent.MOUSE_DRAGGED, .85, .16);
			assertTrue(address(fixture.point()).getCanonicalParameter() > fixture.initial.getCanonicalParameter());
			assertIdentityAndDependent(fixture);
			mouse(app, MouseEvent.MOUSE_DRAGGED, .15, .16);
			mouse(app, MouseEvent.MOUSE_RELEASED, .15, .16);
			assertTrue(address(fixture.point()).getCanonicalParameter() < fixture.initial.getCanonicalParameter());
			assertIdentityAndDependent(fixture); moved.set(address(fixture.point()));
		});
		Cu7M08SDesktopEventsTest.await(undo::undoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> { assertEquals(1, undo.getHistorySize()); app.getKernel().undo(); });
		Cu7M08SDesktopEventsTest.await(undo::redoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertEquals(fixture.initial, address(fixture.point())); assertIdentityAndDependent(fixture);
			app.getKernel().redo();
		});
		Cu7M08SDesktopEventsTest.await(undo::undoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertEquals(moved.get(), address(fixture.point())); assertIdentityAndDependent(fixture);
			var file = directory.resolve("r5-off-trace.cedg").toFile();
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file));
			var reopened = G9U1SemanticPointInteractionTest.app(); assertTrue(reopened.loadFile(file, false));
			reopened.setMode(EuclidianConstants.MODE_MOVE);
			var restored = new Fixture(reopened, fixture.pointId, fixture.sourceId, fixture.initial);
			assertEquals(moved.get(), address(restored.point()));
			dragAndRelease(restored, .85, restored.point().inhomY);
			assertNotEquals(moved.get(), address(restored.point())); assertIdentityAndDependent(restored);
		});
	}
	@Test void fixedPointRefusesPointerEditingButContinuesFollowingItsSource() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var fixture = fixture(false); var point = fixture.point(); point.setFixed(true);
			var before = address(point); double x = point.inhomX, y = point.inhomY;
			mouse(fixture.app, MouseEvent.MOUSE_PRESSED, x, y);
			mouse(fixture.app, MouseEvent.MOUSE_DRAGGED, .85, y);
			mouse(fixture.app, MouseEvent.MOUSE_RELEASED, .85, y);
			assertEquals(before, address(point)); assertEquals(x, point.inhomX, 0); assertEquals(y, point.inhomY, 0);
			var focus = (GeoPoint) fixture.app.getKernel().lookupLabel("Focus");
			focus.setCoords(.1, .25, 1); focus.updateCascade();
			assertTrue(point.isDefined()); assertTrue(point.isLocked());
			assertTrue(point.inhomX != x || point.inhomY != y);
			assertIdentityAndDependent(fixture);
		});
	}
	@Test void uncertifiableRequestRetainsTheWholeLastValidState() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var fixture = fixture(true); var app = fixture.app; var before = address(fixture.point());
			String xml = app.getXML(); var metrics = app.getKernel().getConstruction().getLastNativeTransactionMetrics();
			press(fixture);
			mouse(app, MouseEvent.MOUSE_DRAGGED, 0, 0);
			mouse(app, MouseEvent.MOUSE_RELEASED, 0, 0);
			assertEquals(before, address(fixture.point())); assertEquals(xml, app.getXML());
			assertSame(metrics, app.getKernel().getConstruction().getLastNativeTransactionMetrics());
			assertIdentityAndDependent(fixture);
		});
	}
	@Test void exhaustedPolicyCannotUseTheLargerNeighborhoodAsPermission() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var fixture = fixture(false); var app = fixture.app;
			G9U1SemanticPointInteractionTest.controller(app).referencePointTool().setPolicy(
					new LocusPointInteractionPolicy2D(.1, 1e-10, 1e-12,
							new LocusPointInteractionWorkBudget2D(32, 2, 1, 4)));
			var before = address(fixture.point()); String xml = app.getXML();
			dragAndRelease(fixture, .85, .16);
			assertEquals(before, address(fixture.point())); assertEquals(xml, app.getXML());
			assertIdentityAndDependent(fixture);
		});
	}
}
