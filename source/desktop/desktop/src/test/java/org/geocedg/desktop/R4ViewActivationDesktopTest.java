/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.App;
import org.geogebra.desktop.gui.GuiManagerD;
import org.geogebra.desktop.gui.layout.DockManagerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** P2-C regressions at real Desktop callback boundaries; no physical smoke claim. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class R4ViewActivationDesktopTest {

	@BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }

	private static GeoElement eval(AppGeoCeDG app, String command) {
		return G9U1SemanticPointInteractionTest.eval(app, command);
	}
	private static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
		return G9U1SemanticPointInteractionTest.controller(app);
	}
	private static DockManagerD manager(AppGeoCeDG app) {
		return ((GuiManagerD) app.getGuiManager()).getLayout().getDockManager();
	}
	private static void prepareViews(AppGeoCeDG app) {
		app.getGuiManager().setShowView(true, App.VIEW_EUCLIDIAN2);
		app.setActiveView(App.VIEW_EUCLIDIAN);
		assertSame(app.getEuclidianView1(), app.getActiveEuclidianView());
	}
	private static void roundTrip(AppGeoCeDG app, boolean docking) {
		Object original = app.getActiveEuclidianView();
		long generation = manager(app).getEuclidianViewActivationGeneration();
		if (docking) {
			manager(app).setFocusedPanel(manager(app).getPanel(App.VIEW_EUCLIDIAN2), false);
		} else { app.setActiveView(App.VIEW_EUCLIDIAN2); }
		assertNotSame(original, app.getActiveEuclidianView());
		if (docking) {
			manager(app).setFocusedPanel(manager(app).getPanel(App.VIEW_EUCLIDIAN), false);
		} else { app.setActiveView(App.VIEW_EUCLIDIAN); }
		assertSame(original, app.getActiveEuclidianView());
		assertTrue(manager(app).getEuclidianViewActivationGeneration() > generation);
	}
	private static GeoLocusV2 circle(AppGeoCeDG app) {
		for (String command : List.of("Centre=(0,0)", "c=Circle(Centre,1)", "N=Point(c)",
				"v=Vector(Centre,N)", "Q=N+(2*v)", "L=LocusV2(Q,N)")) { eval(app, command); }
		var source = (GeoLocusV2) app.getKernel().lookupLabel("L");
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			geo.setEuclidianVisible(geo == source); geo.updateRepaint();
		}
		return source;
	}
	private static long owned(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast)
				.filter(GeoCeDGPointInteraction::owns).count();
	}
	private record Before(String construction, int geos, Set<?> ids, int reservations, int history) {
		static Before capture(AppGeoCeDG app) {
			var cons = app.getKernel().getConstruction();
			var xml = new XMLStringBuilder(); cons.getConstructionXML(xml, false);
			return new Before(xml.toString(), cons.getGeoSetConstructionOrder().size(),
					cons.getSpatialIdentityRegistry().getRecords().stream()
							.map(record -> record.getId()).collect(Collectors.toSet()),
					cons.getSpatialIdentityRegistry().getReservedIdentityCount(),
					cons.getUndoManager().getHistorySize());
		}
		void assertUnchanged(AppGeoCeDG app) { assertEquals(this, capture(app)); }
	}

	private void sourceChoiceAba(int mutation) throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app);
			var source = G9U1SemanticPointInteractionTest.line(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			var tool = controller(app).referencePointTool();
			var originalPolicy = LocusPointInteractionPolicy2D.initial(.1);
			tool.setPolicy(originalPolicy);
			var before = Before.capture(app); long points = owned(app);
			var entered = new AtomicBoolean();
			controller(app).semanticSourceChooser = sources -> {
				entered.set(true); assertTrue(sources.contains(source));
				if (mutation < 2) { roundTrip(app, mutation == 1); }
				else if (mutation == 2) {
					app.setMode(EuclidianConstants.MODE_JOIN); app.setMode(EuclidianConstants.MODE_POINT);
				} else {
					tool.setPolicy(LocusPointInteractionPolicy2D.initial(.3)); tool.setPolicy(originalPolicy);
				}
				return source;
			};
			Cu7M08SDesktopEventsTest.pointer(app, .4, 0);
			assertTrue(entered.get()); assertEquals(points, owned(app));
			assertNull(controller(app).referencePointTool().pendingSelection());
			before.assertUnchanged(app);
		});
	}
	@Test void programmaticViewRoundTripCannotReviveSourceChoice() throws Exception { sourceChoiceAba(0); }
	@Test void directDockingRoundTripCannotReviveSourceChoice() throws Exception { sourceChoiceAba(1); }
	@Test void toolRoundTripStillInvalidatesSourceChoice() throws Exception { sourceChoiceAba(2); }
	@Test void policyRoundTripStillInvalidatesSourceChoice() throws Exception { sourceChoiceAba(3); }

	@Test void preimageCallbackCannotReviveARequestByReturningToItsView() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app);
			var source = (GeoLocusV2) eval(app, "S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
			var before = Before.capture(app); var entered = new AtomicBoolean();
			var point = new GeoCeDGPointInteraction(app).create(source, 0, 1, .01, result -> {
				entered.set(true); roundTrip(app, false); return result.getCandidates().get(0);
			});
			assertTrue(entered.get()); assertNull(point); before.assertUnchanged(app);
		});
	}

	private void semanticPublicationAba(boolean forward) throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app);
			var source = forward ? circle(app) : G9U1SemanticPointInteractionTest.line(app);
			double x = forward ? 3 : .4;
			var before = Before.capture(app); var entered = new AtomicBoolean(); View listener = mock(View.class);
			doAnswer(invocation -> {
				GeoElement geo = invocation.getArgument(0);
				if (geo instanceof GeoPoint point && point.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D
						&& entered.compareAndSet(false, true)) { roundTrip(app, true); }
				return null;
			}).when(listener).add(any(GeoElement.class));
			try (var offer = new GeoCeDGReferencePlacement(source, x, .02,
					LocusPointInteractionPolicy2D.initial(.1))) {
				assertEquals(forward ? "INDIVIDUAL_FORWARD_ADDRESS_CREATION" : "R1_R6", offer.getRoute().name());
				app.getKernel().attach(listener);
				try {
					assertEquals("STALE_SELECTION", assertThrows(IllegalStateException.class,
							() -> offer.commit(offer.getChoices().get(0))).getMessage());
				} finally { app.getKernel().detach(listener); }
			}
			assertTrue(entered.get()); before.assertUnchanged(app);
		});
	}
	@Test void historicalAtomicPublicationRejectsViewAbaAfterOutputAdd() throws Exception { semanticPublicationAba(false); }
	@Test void forwardAtomicPublicationRejectsViewAbaAfterOutputAdd() throws Exception { semanticPublicationAba(true); }

	@Test void nativeAtomicPublicationRejectsViewAbaAfterOutputAdd() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app);
			var source = eval(app, "s=Segment((0,0),(2,0))");
			var reference = (GeoPoint) eval(app, "Ref=(1,.02)");
			var before = Before.capture(app); var entered = new AtomicBoolean(); View listener = mock(View.class);
			doAnswer(invocation -> {
				GeoElement geo = invocation.getArgument(0);
				if (geo instanceof GeoPoint point && point.getPath() == source
						&& entered.compareAndSet(false, true)) { roundTrip(app, false); }
				return null;
			}).when(listener).add(any(GeoElement.class));
			try (var offer = new GeoCeDGNativePointPlacement(source, reference, 1, .02, .1)) {
				app.getKernel().attach(listener);
				try { assertEquals("STALE_SELECTION", assertThrows(IllegalStateException.class,
						() -> offer.commit(true)).getMessage()); }
				finally { app.getKernel().detach(listener); }
			}
			assertTrue(entered.get()); before.assertUnchanged(app);
		});
	}

	@Test void pendingForwardOfferCannotBeConfirmedAfterAViewRoundTrip() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app); var source = circle(app);
			var before = Before.capture(app);
			try (var offer = new GeoCeDGReferencePlacement(source, 3, .02,
					LocusPointInteractionPolicy2D.initial(.1))) {
				assertFalse(offer.getChoices().isEmpty()); roundTrip(app, false);
				assertEquals("STALE_SELECTION", assertThrows(IllegalStateException.class,
						() -> offer.commit(offer.getChoices().get(0))).getMessage());
			}
			before.assertUnchanged(app);
		});
	}

	@Test void nullFocusAndHideShowDoNotReviveOldTickets() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app);
			var source = G9U1SemanticPointInteractionTest.line(app); var before = Before.capture(app);
			var first = GeoCeDGPointRequest.forSource(app, source, .4, 0,
					LocusPointInteractionPolicy2D.initial(.1));
			manager(app).setFocusedPanel(null, false); app.setActiveView(App.VIEW_EUCLIDIAN);
			assertSame(app.getEuclidianView1(), app.getActiveEuclidianView()); assertFalse(first.isCurrent());
			var second = GeoCeDGPointRequest.forSource(app, source, .4, 0,
					LocusPointInteractionPolicy2D.initial(.1));
			assertTrue(manager(app).hide(App.VIEW_EUCLIDIAN2, true));
			manager(app).show(App.VIEW_EUCLIDIAN2); app.setActiveView(App.VIEW_EUCLIDIAN);
			assertFalse(second.isCurrent()); before.assertUnchanged(app);
		});
	}

	@Test void samePanelAndNonEuclidianFocusRetainTheActiveViewTicket() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app);
			var source = G9U1SemanticPointInteractionTest.line(app);
			var request = GeoCeDGPointRequest.forSource(app, source, .4, 0,
					LocusPointInteractionPolicy2D.initial(.1));
			long generation = manager(app).getEuclidianViewActivationGeneration();
			app.setActiveView(App.VIEW_EUCLIDIAN);
			manager(app).setFocusedPanel(manager(app).getPanel(App.VIEW_ALGEBRA), false);
			assertSame(app.getEuclidianView1(), app.getActiveEuclidianView());
			assertEquals(generation, manager(app).getEuclidianViewActivationGeneration());
			assertTrue(request.isCurrent());
		});
	}

	@Test void sourceChooserPanAndZoomPreserveCapturedWorldCoordinatesAndRadius() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app); var source = circle(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			var canvas = app.getEuclidianView1(); canvas.setCoordSystem(100, 100, 20, 20);
			double radius = Math.max(1, app.getCapturingThreshold(PointerEventType.MOUSE))
					* Math.hypot(canvas.getInvXscale(), canvas.getInvYscale());
			long generation = manager(app).getEuclidianViewActivationGeneration();
			var entered = new AtomicBoolean();
			controller(app).semanticSourceChooser = sources -> {
				entered.set(true); assertTrue(sources.contains(source));
				canvas.setCoordSystem(500, 450, 1000, 1000); return source;
			};
			Cu7M08SDesktopEventsTest.pointer(app, 3.15, 0);
			assertTrue(entered.get()); var offer = controller(app).referencePointTool().pendingSelection();
			assertNotNull(offer, controller(app).referencePointTool().lastFailure());
			assertEquals(generation, manager(app).getEuclidianViewActivationGeneration());
			assertTrue(offer.getRequest().isCurrent()); assertEquals(3.15, offer.getRequest().getX(), 1e-12);
			assertEquals(0, offer.getRequest().getY(), 0); assertEquals(radius, offer.getRequest().getRadius(), 0);
			assertEquals(20, offer.getRequest().getCapturedViewTransform()[2], 0);
			Cu7M08SDesktopEventsTest.confirm(app);
			var point = Cu7M08SDesktopEventsTest.point(app);
			assertSame(source, ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getSource());
			assertTrue(Math.hypot(point.inhomX - 3.15, point.inhomY) <= radius);
		});
	}

	private static GeoPoint pressSplinePoint(AppGeoCeDG app) {
		var source = G9U1SemanticPointInteractionTest.spline(app);
		var point = new GeoCeDGPointInteraction(app).create(source, -1, 0, .1, result -> null);
		assertNotNull(point); point.setLabel("DraggedP"); eval(app, "Dependent=DraggedP+(1,0)");
		app.setMode(EuclidianConstants.MODE_MOVE);
		Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
		return point;
	}

	@Test void viewAbaInsideDagCallbackRestoresTheLastAddressIdentityAndDependent() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app); var point = pressSplinePoint(app);
			var cons = app.getKernel().getConstruction(); var id = cons.getSpatialIdentityRegistry().getPersistentGeoId(point);
			var address = ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getCurrentSemanticAddress();
			var before = Before.capture(app); var entered = new AtomicBoolean(); View listener = mock(View.class);
			doAnswer(invocation -> {
				if (invocation.getArgument(0) == point && entered.compareAndSet(false, true)) { roundTrip(app, true); }
				return null;
			}).when(listener).update(any(GeoElement.class));
			app.getKernel().attach(listener);
			try { Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, -.9, 0); }
			finally { app.getKernel().detach(listener); }
			assertTrue(entered.get()); Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, -.9, 0);
			before.assertUnchanged(app); var restored = (GeoPoint) app.getKernel().lookupLabel("DraggedP");
			assertEquals(id, cons.getSpatialIdentityRegistry().getPersistentGeoId(restored));
			assertEquals(address, ((AlgoSemanticLocusPoint2D) restored.getParentAlgorithm()).getCurrentSemanticAddress());
			var dependent = (GeoPoint) app.getKernel().lookupLabel("Dependent");
			assertEquals(restored.inhomX + 1, dependent.inhomX, 0); assertEquals(restored.inhomY, dependent.inhomY, 0);
		});
	}

	@Test void viewAbaBetweenDragEventsCannotReviveThePressTicket() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app); pressSplinePoint(app);
			var before = Before.capture(app); roundTrip(app, false);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, -.9, 0);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, -.9, 0);
			before.assertUnchanged(app);
		});
	}

	@Test void sameViewPanAndZoomDuringDagKeepTheCapturedDragRequest() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app); var point = pressSplinePoint(app);
			var canvas = app.getEuclidianView1();
			double x = canvas.toRealWorldCoordX(canvas.toScreenCoordX(-.9));
			double y = canvas.toRealWorldCoordY(canvas.toScreenCoordY(0));
			var cons = app.getKernel().getConstruction(); var id = cons.getSpatialIdentityRegistry().getPersistentGeoId(point);
			int count = cons.getGeoSetConstructionOrder().size();
			long generation = manager(app).getEuclidianViewActivationGeneration();
			var entered = new AtomicBoolean(); View listener = mock(View.class);
			doAnswer(invocation -> {
				if (invocation.getArgument(0) == point && entered.compareAndSet(false, true)) {
					canvas.setCoordSystem(650, 200, 400, 400);
				}
				return null;
			}).when(listener).update(any(GeoElement.class));
			app.getKernel().attach(listener);
			try { Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, -.9, 0); }
			finally { app.getKernel().detach(listener); }
			assertTrue(entered.get()); assertEquals(generation, manager(app).getEuclidianViewActivationGeneration());
			assertSame(point, app.getKernel().lookupLabel("DraggedP"));
			assertEquals(x, point.inhomX, 1e-8); assertEquals(y, point.inhomY, 1e-8);
			assertEquals(id, cons.getSpatialIdentityRegistry().getPersistentGeoId(point));
			assertEquals(count, cons.getGeoSetConstructionOrder().size());
			var dependent = (GeoPoint) app.getKernel().lookupLabel("Dependent");
			assertEquals(point.inhomX + 1, dependent.inhomX, 1e-8);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, -.9, 0);
		});
	}

	@Test void exhaustedGenerationCannotWrapOrAuthorizeFurtherRequests() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareViews(app);
			var source = G9U1SemanticPointInteractionTest.line(app); var before = Before.capture(app);
			var field = DockManagerD.class.getDeclaredField("euclidianViewActivationGeneration");
			field.setAccessible(true); field.setLong(manager(app), Long.MAX_VALUE - 1);
			var request = GeoCeDGPointRequest.forSource(app, source, .4, 0,
					LocusPointInteractionPolicy2D.initial(.1));
			app.setActiveView(App.VIEW_EUCLIDIAN2); app.setActiveView(App.VIEW_EUCLIDIAN);
			assertEquals(Long.MAX_VALUE, manager(app).getEuclidianViewActivationGeneration());
			assertFalse(request.isCurrent());
			assertThrows(GeoCeDGPointRequest.StaleContext.class, () -> GeoCeDGPointRequest.forSource(app,
					source, .4, 0, LocusPointInteractionPolicy2D.initial(.1)));
			manager(app).setFocusedPanel(manager(app).getPanel(App.VIEW_EUCLIDIAN2), false);
			manager(app).setFocusedPanel(manager(app).getPanel(App.VIEW_EUCLIDIAN), false);
			assertSame(app.getEuclidianView1(), app.getActiveEuclidianView());
			assertEquals(Long.MAX_VALUE, manager(app).getEuclidianViewActivationGeneration());
			before.assertUnchanged(app);
		});
	}
}
