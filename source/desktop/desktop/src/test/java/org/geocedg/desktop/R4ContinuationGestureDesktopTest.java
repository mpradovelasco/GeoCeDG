/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** One AWT drag event across the independent MEET chord; distinct from physical smoke. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class R4ContinuationGestureDesktopTest {
	private static final double START = .11, END = 1.91;
	private record Fixture(AppGeoCeDG app, PersistentGeoId pointId, PersistentGeoId sourceId,
			LocusSemanticAddress2D initial) {
		GeoPoint point() { return (GeoPoint) app.getKernel().lookupLabel("GestureP"); }
		GeoPoint dependent() { return (GeoPoint) app.getKernel().lookupLabel("Dependent"); }
	}
	@BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }
	private static GeoElement eval(AppGeoCeDG app, String command) {
		return G9U1SemanticPointInteractionTest.eval(app, command);
	}
	private static LocusSemanticAddress2D address(GeoPoint point) {
		return AlgoSemanticLocusPoint2D.requireSemanticParent(point).getCurrentSemanticAddress();
	}
	private static void configureWorldRadius(AppGeoCeDG app) {
		// Explicit fixture policy: this integer threshold and scale give r_world=.5.
		app.setCapturingThreshold(100);
		double scale = 100 * Math.sqrt(2) / .5;
		app.getEuclidianView1().setCoordSystem(400, 400, scale, scale);
		double actual = app.getCapturingThreshold(PointerEventType.MOUSE)
				* Math.hypot(app.getEuclidianView1().getInvXscale(), app.getEuclidianView1().getInvYscale());
		assertEquals(.5, actual, Math.ulp(.5));
	}
	private static Fixture fixture() {
		var app = G9U1SemanticPointInteractionTest.app();
		for (String command : List.of("c=Circle((0,0),1)", "N=Point(c)", "horizontal=Line(N,xAxis)",
				"vertical=Line(N,yAxis)", "Q=Intersect(horizontal,vertical)", "L=LocusV2(Q,N)")) { eval(app, command); }
		var source = (GeoLocusV2) app.getKernel().lookupLabel("L");
		double x, y;
		try (var session = LocusEvaluationSession2D.memoizing(100)) {
			var value = source.getSemanticDefinition().evaluate(SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY, START, session);
			assertTrue(value.isValid()); x = value.getPoint().getX(); y = value.getPoint().getY();
		}
		GeoPoint point;
		try (var offer = new GeoCeDGReferencePlacement(source, x, y, LocusPointInteractionPolicy2D.initial(.5))) {
			// The facade preserves the admitted historical H1 offer ahead of its forward fallback.
			assertEquals(GeoCeDGReferencePlacement.Route.R2_H1, offer.getRoute());
			assertNotNull(offer.getLocalSelection());
			assertEquals("EXPLICIT_LOCAL_SELECTION_REQUIRED",
					offer.getLocalSelection().getResult().getPermission().name());
			point = offer.commit(offer.getChoices().stream().min(Comparator.comparingDouble(
					choice -> Math.abs(choice.getAddress().getCanonicalParameter() - START))).orElseThrow());
		}
		point.setLabel("GestureP"); eval(app, "Dependent=GestureP+(2,3)");
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			geo.setEuclidianVisible(geo == source || geo == point); geo.updateRepaint();
		}
		configureWorldRadius(app); app.setMode(EuclidianConstants.MODE_MOVE);
		assertTrue(GeoCeDGPointInteraction.owns(point)); assertFalse(point.isLocked());
		var id = app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		assertNotNull(id); app.getKernel().initUndoInfo();
		return new Fixture(app, id, source.getPersistentLocusId(), address(point));
	}
	private static double[] capturedTarget(AppGeoCeDG app, double x, double y) {
		var view = app.getEuclidianView1();
		return new double[] {view.toRealWorldCoordX(view.toScreenCoordX(x)),
				view.toRealWorldCoordY(view.toScreenCoordY(y))};
	}
	private static void press(Fixture fixture) {
		Cu7M08SDesktopEventsTest.mouse(fixture.app, MouseEvent.MOUSE_PRESSED,
				fixture.point().inhomX, fixture.point().inhomY);
	}
	private static void release(Fixture fixture, double x, double y) {
		Cu7M08SDesktopEventsTest.mouse(fixture.app, MouseEvent.MOUSE_RELEASED, x, y);
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
	}
	private record Before(String xml, LocusSemanticAddress2D address, int count, int reservations, int history) {
		static Before capture(Fixture fixture) {
			var cons = fixture.app.getKernel().getConstruction();
			return new Before(fixture.app.getXML(), R4ContinuationGestureDesktopTest.address(fixture.point()),
					cons.getGeoSetConstructionOrder().size(),
					cons.getSpatialIdentityRegistry().getReservedIdentityCount(), cons.getUndoManager().getHistorySize());
		}
		void assertRestored(Fixture fixture) { assertEquals(this, capture(fixture)); assertIdentityAndDependent(fixture); }
	}

	@Test void singleLargeAwtDragHasOneFinalDagUpdateOneUndoAndSurvivesReopen(@TempDir Path directory) throws Exception {
		var holder = new AtomicReference<Fixture>(); var moved = new AtomicReference<LocusSemanticAddress2D>();
		Cu7M08SDesktopEventsTest.edt(() -> holder.set(fixture()));
		var fixture = holder.get(); var app = fixture.app; var cons = app.getKernel().getConstruction();
		var undo = cons.getUndoManager(); Cu7M08SDesktopEventsTest.await(() -> undo.getHistorySize() == 0);
		Cu7M08SDesktopEventsTest.edt(() -> {
			GeoPoint point = fixture.point(), dependent = fixture.dependent();
			int count = cons.getGeoSetConstructionOrder().size();
			var previousMetrics = cons.getLastNativeTransactionMetrics();
			var pointUpdates = new AtomicInteger(); var dependentUpdates = new AtomicInteger(); View observer = mock(View.class);
			doAnswer(invocation -> {
				if (invocation.getArgument(0) == point) { pointUpdates.incrementAndGet(); }
				if (invocation.getArgument(0) == dependent) { dependentUpdates.incrementAndGet(); }
				return null;
			}).when(observer).update(any(GeoElement.class));
			var target = capturedTarget(app, Math.cos(END), Math.sin(END)); press(fixture);
			app.getKernel().attach(observer);
			try {
				// Exactly ONE MOUSE_DRAGGED, with no external substeps or intermediate publication.
				Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, Math.cos(END), Math.sin(END));
			} finally { app.getKernel().detach(observer); }
			assertEquals(1, pointUpdates.get()); assertEquals(1, dependentUpdates.get());
			assertSame(point, fixture.point()); assertEquals(count, cons.getGeoSetConstructionOrder().size());
			assertEquals(Math.atan2(target[1], target[0]), address(point).getCanonicalParameter(), 3e-11);
			assertTrue(Math.abs(address(point).getCanonicalParameter() - END) < 2 * app.getEuclidianView1().getInvXscale(),
					"Screen quantization is declared; exact command-level endpoint is tested separately");
			assertIdentityAndDependent(fixture); moved.set(address(point));
			var metrics = cons.getLastNativeTransactionMetrics(); assertNotSame(previousMetrics, metrics);
			assertNotNull(metrics); assertNull(metrics.unsupported); assertEquals(0, metrics.dagCalls);
			assertEquals(1, metrics.capturedPoints,
					"Only N=Point(c) is a native editable path input; semantic P and its dependent use normal DAG reconstruction");
			release(fixture, Math.cos(END), Math.sin(END));
		});
		Cu7M08SDesktopEventsTest.await(undo::undoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertEquals(1, undo.getHistorySize(), "The whole large event/gesture is one Undo"); app.getKernel().undo();
		});
		Cu7M08SDesktopEventsTest.await(undo::redoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertEquals(fixture.initial, address(fixture.point())); assertIdentityAndDependent(fixture);
			assertFalse(undo.undoPossible()); app.getKernel().redo();
		});
		Cu7M08SDesktopEventsTest.await(() -> undo.getHistorySize() == 1);
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertEquals(moved.get(), address(fixture.point())); assertIdentityAndDependent(fixture);
			Path file = directory.resolve("meet-large-gesture.cedg");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
			var reopened = G9U1SemanticPointInteractionTest.app(); assertTrue(reopened.loadFile(file.toFile(), false));
			var restored = new Fixture(reopened, fixture.pointId, fixture.sourceId, fixture.initial);
			assertEquals(moved.get(), address(restored.point())); assertIdentityAndDependent(restored);
			assertTrue(GeoCeDGPointInteraction.owns(restored.point())); configureWorldRadius(reopened);
			reopened.setMode(EuclidianConstants.MODE_MOVE); var target = capturedTarget(reopened, Math.cos(START), Math.sin(START));
			press(restored);
			Cu7M08SDesktopEventsTest.mouse(reopened, MouseEvent.MOUSE_DRAGGED, Math.cos(START), Math.sin(START));
			release(restored, Math.cos(START), Math.sin(START));
			assertNotEquals(moved.get(), address(restored.point()));
			assertEquals(Math.atan2(target[1], target[0]), address(restored.point()).getCanonicalParameter(), 3e-11);
			assertIdentityAndDependent(restored);
		});
	}

	@Test void uncertifiableTailOfOneLargeAwtRequestPublishesNoPrefixAndNoUndo() throws Exception {
		var holder = new AtomicReference<Fixture>(); Cu7M08SDesktopEventsTest.edt(() -> holder.set(fixture()));
		var fixture = holder.get(); var app = fixture.app; var cons = app.getKernel().getConstruction();
		Cu7M08SDesktopEventsTest.await(() -> cons.getUndoManager().getHistorySize() == 0);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var before = Before.capture(fixture); var metrics = cons.getLastNativeTransactionMetrics();
			GeoPoint point = fixture.point(); var updates = new AtomicInteger(); View observer = mock(View.class);
			doAnswer(invocation -> { if (invocation.getArgument(0) == point) { updates.incrementAndGet(); } return null; })
					.when(observer).update(any(GeoElement.class));
			press(fixture); app.getKernel().attach(observer);
			try { Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, 0, 0); }
			finally { app.getKernel().detach(observer); }
			release(fixture, 0, 0);
			assertEquals(0, updates.get()); assertSame(metrics, cons.getLastNativeTransactionMetrics());
			before.assertRestored(fixture); assertFalse(cons.getUndoManager().undoPossible());
		});
	}

	@Test void failureDuringTheOnlyFinalPublicationRestoresPointDependentIdentityAndHistory() throws Exception {
		var holder = new AtomicReference<Fixture>(); Cu7M08SDesktopEventsTest.edt(() -> holder.set(fixture()));
		var fixture = holder.get(); var app = fixture.app; var cons = app.getKernel().getConstruction();
		Cu7M08SDesktopEventsTest.await(() -> cons.getUndoManager().getHistorySize() == 0);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var before = Before.capture(fixture); GeoPoint point = fixture.point(); var entered = new AtomicBoolean();
			var observedAddress = new AtomicReference<LocusSemanticAddress2D>();
			View observer = mock(View.class);
			doAnswer(invocation -> {
				if (invocation.getArgument(0) == point && entered.compareAndSet(false, true)) {
					observedAddress.set(address(point));
					throw new IllegalStateException("R4_EXPECTED_FINAL_PUBLICATION_FAILURE");
				}
				return null;
			}).when(observer).update(any(GeoElement.class));
			press(fixture); app.getKernel().attach(observer);
			try { assertDoesNotThrow(() -> Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED,
					Math.cos(END), Math.sin(END))); }
			finally { app.getKernel().detach(observer); }
			release(fixture, Math.cos(END), Math.sin(END)); assertTrue(entered.get());
			assertNotEquals(before.address, observedAddress.get(), "The whole proof resolved before this final update");
			before.assertRestored(fixture); assertFalse(cons.getUndoManager().undoPossible());
			var metrics = cons.getLastNativeTransactionMetrics(); assertNotNull(metrics); assertTrue(metrics.dagCalls >= 1);
		});
	}
}
