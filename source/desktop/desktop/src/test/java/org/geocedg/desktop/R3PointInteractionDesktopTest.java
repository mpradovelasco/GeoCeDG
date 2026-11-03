/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.kernel.View;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.App;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Permanent F4–F6 adversaries; AWT callbacks are distinct from physical mouse smoke. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class R3PointInteractionDesktopTest {

	@BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }
	private static GeoElement eval(AppGeoCeDG app, String command) {
		return G9U1SemanticPointInteractionTest.eval(app, command);
	}
	private static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
		return G9U1SemanticPointInteractionTest.controller(app);
	}
	private static JCheckBox free(AppGeoCeDG app) {
		return Cu7M08SDesktopEventsTest.find(Cu7M08SDesktopEventsTest.panel(app),
				JCheckBox.class, value -> "m08-allow-dragging".equals(value.getName()));
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
	private static void switchView(AppGeoCeDG app) {
		app.getGuiManager().setShowView(true, App.VIEW_EUCLIDIAN2);
		app.setActiveView(App.VIEW_EUCLIDIAN2);
	}
	private static void prepareOtherView(AppGeoCeDG app) {
		app.getGuiManager().setShowView(true, App.VIEW_EUCLIDIAN2);
		app.setActiveView(App.VIEW_EUCLIDIAN);
	}
	private static Set<?> identities(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getSpatialIdentityRegistry().getRecords().stream()
				.map(record -> record.getId()).collect(Collectors.toSet());
	}
	private record Before(String xml, int geos, Set<?> ids, int reservations, int history) {
		static Before capture(AppGeoCeDG app) {
			var cons = app.getKernel().getConstruction();
			return new Before(cons.getCurrentUndoXML(false).toString(),
					cons.getGeoSetConstructionOrder().size(), identities(app),
					cons.getSpatialIdentityRegistry().getReservedIdentityCount(),
					cons.getUndoManager().getHistorySize());
		}
		void assertUnchanged(AppGeoCeDG app) {
			var cons = app.getKernel().getConstruction();
			assertEquals(xml, cons.getCurrentUndoXML(false).toString());
			assertEquals(geos, cons.getGeoSetConstructionOrder().size());
			assertEquals(ids, identities(app));
			assertEquals(reservations, cons.getSpatialIdentityRegistry().getReservedIdentityCount());
			assertEquals(history, cons.getUndoManager().getHistorySize());
		}
	}

	private void assertCapturedRadius(boolean changeViewTransform) throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = circle(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			var view = app.getEuclidianView1(); view.setCoordSystem(100, 100, 20, 20);
			double radius = Math.max(1, app.getCapturingThreshold(PointerEventType.MOUSE))
					* Math.hypot(view.getInvXscale(), view.getInvYscale());
			AtomicBoolean chosen = new AtomicBoolean();
			controller(app).semanticSourceChooser = values -> {
				chosen.set(true); assertTrue(values.contains(source));
				if (changeViewTransform) { view.setCoordSystem(500, 450, 1000, 1000); }
				return source;
			};
			// The actual canvas hit is three pixels / .15 world units outside L.
			Cu7M08SDesktopEventsTest.pointer(app, 3.15, 0);
			assertTrue(chosen.get());
			var tool = controller(app).referencePointTool();
			var offer = tool.pendingSelection();
			assertNotNull(offer, tool.lastFailure());
			assertEquals(GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
					offer.getRoute());
			assertEquals(radius, offer.getRequest().getRadius(), 0);
			assertEquals(20, offer.getRequest().getCapturedViewTransform()[2], 0);
			assertEquals(3.15, offer.getRequest().getX(), 1e-12);
			assertEquals(radius, Double.parseDouble(Cu7M08SDesktopEventsTest.radius(app).getText()), 0);
			Cu7M08SDesktopEventsTest.confirm(app);
			var point = Cu7M08SDesktopEventsTest.point(app);
			assertTrue(Math.hypot(point.inhomX - 3.15, point.inhomY) <= radius);
			assertSame(source, ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getSource());
		});
	}

	@Test void capturedCanvasRadiusReachesGeneralForwardDiscoveryAtLowZoom() throws Exception {
		assertCapturedRadius(false);
	}
	@Test void sourceChooserZoomKeepsCapturedWorldRequestAndRadius() throws Exception {
		assertCapturedRadius(true);
	}

	@Test void facadeHandoffRetainsTheQueryAndExplicitNumericalPolicy() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); var source = circle(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			var tool = controller(app).referencePointTool();
			var budget = LocusPointInteractionWorkBudget2D.initial();
			tool.setPolicy(new LocusPointInteractionPolicy2D(.7, 3e-10, 2e-12, budget));
			var request = tool.bindContext(GeoCeDGPointRequest.forSource(app, source,
					3.15, 0, tool.canvasPolicy(.25)));
			var interaction = new GeoCeDGPointInteraction(app);
			try (var offer = interaction.query(source, request)) {
				long misses = offer.getSemanticMisses(); assertTrue(misses > 0);
				tool.offerWorld(request, offer);
				assertSame(offer, tool.pendingSelection());
				assertEquals(misses, tool.pendingSelection().getSemanticMisses());
				assertSame(budget, offer.getRequest().getPolicy().getWorkBudget());
				assertEquals(.25, offer.getRequest().getRadius(), 0);
				assertEquals(3e-10, offer.getRequest().getPolicy().getEqualDistanceTolerance(), 0);
				assertEquals(2e-12, offer.getRequest().getPolicy().getParameterTolerance(), 0);
				Cu7M08SDesktopEventsTest.confirm(app);
				assertNotNull(Cu7M08SDesktopEventsTest.point(app));
			}
		});
	}

	@Test void translatedCollinearSegmentInvalidatesAnOfferWithIdenticalSourceXml() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var a = (GeoPoint) eval(app, "A=(0,0)"); var b = (GeoPoint) eval(app, "B=(2,0)");
			var source = eval(app, "s=Segment(A,B)"); var q = (GeoPoint) eval(app, "Ref=(.5,1)");
			String sourceXml = source.getXML();
			try (var offer = new GeoCeDGNativePointPlacement(source, q, .5, 1, 3)) {
				a.setCoords(3, 0, 1); b.setCoords(5, 0, 1); a.updateCascade(); b.updateCascade();
				assertEquals(sourceXml, source.getXML());
				var before = Before.capture(app);
				assertEquals("STALE_SELECTION", assertThrows(IllegalStateException.class,
						() -> offer.commit(true)).getMessage());
				before.assertUnchanged(app);
			}
		});
	}

	@Test void nativeLineAndRayParameterOriginsInvalidatePendingOffers() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			for (String family : List.of("Line", "Ray")) {
				var app = G9U1SemanticPointInteractionTest.app();
				var a = (GeoPoint) eval(app, "A=(0,0)"); var b = (GeoPoint) eval(app, "B=(2,0)");
				var source = eval(app, "s=" + family + "(A,B)");
				var q = (GeoPoint) eval(app, "Ref=(1,.02)");
				try (var offer = new GeoCeDGNativePointPlacement(source, q, 1, .02, .1)) {
					a.setCoords(.25, 0, 1); b.setCoords(2.25, 0, 1); a.updateCascade(); b.updateCascade();
					var before = Before.capture(app);
					assertEquals("STALE_SELECTION", assertThrows(IllegalStateException.class,
							() -> offer.commit(true)).getMessage(), family);
					before.assertUnchanged(app);
				}
			}
		});
	}

	@Test void nativeArcDomainChangeInvalidatesItsPendingOffer() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = (GeoConicPart) eval(app, "arc=CircularArc((0,0),(1,0),(0,1))");
			var q = (GeoPoint) eval(app, "Ref=(1,.02)");
			try (var offer = new GeoCeDGNativePointPlacement(source, q, 1, .02, .1)) {
				source.setParameters(.1, .9, false); source.updateRepaint();
				var before = Before.capture(app);
				assertEquals("STALE_SELECTION", assertThrows(IllegalStateException.class,
						() -> offer.commit(true)).getMessage());
				before.assertUnchanged(app);
			}
		});
	}

	@Test void nativeStyleChangeDoesNotInvalidateUnchangedGeometry() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = eval(app, "s=Segment((0,0),(2,0))");
			var q = (GeoPoint) eval(app, "Ref=(1,.02)"); String original = source.getXML();
			try (var offer = new GeoCeDGNativePointPlacement(source, q, 1, .02, .1)) {
				source.setObjColor(GColor.RED); assertNotEquals(original, source.getXML());
				var point = offer.commit(true); assertNotNull(point); assertSame(source, point.getPath());
			}
		});
	}

	@Test void preimageCallbackCannotPublishAfterSwitchingTheActiveView() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = (GeoLocusV2) eval(app,
					"S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
			Object originalView = app.getActiveEuclidianView();
			var before = Before.capture(app); AtomicBoolean chosen = new AtomicBoolean();
			var point = new GeoCeDGPointInteraction(app).create(source, 0, 1, .01, result -> {
				chosen.set(true); switchView(app); return result.getCandidates().get(0);
			});
			assertTrue(chosen.get()); assertNull(point);
			assertNotSame(originalView, app.getActiveEuclidianView());
			before.assertUnchanged(app);
		});
	}

	@Test void preimageCallbackCannotPublishAfterTheReferenceMoves() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = (GeoLocusV2) eval(app,
					"S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
			var reference = (GeoPoint) eval(app, "Ref=(0,1)");
			int count = app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
			AtomicBoolean chosen = new AtomicBoolean();
			var point = new GeoCeDGPointInteraction(app).createFromReference(source, reference,
					LocusPointInteractionPolicy2D.initial(.01), result -> {
						chosen.set(true); reference.setCoords(.2, 1, 1); reference.updateCascade();
						return result.getCandidates().get(0);
					});
			assertTrue(chosen.get()); assertNull(point);
			assertEquals(count, app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
			assertEquals(.2, reference.inhomX, 0);
		});
	}

	@Test void preimageCallbackCannotReuseContextAfterAChangeOfPolicy() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = (GeoLocusV2) eval(app,
					"S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
			app.setMode(EuclidianConstants.MODE_POINT);
			var tool = controller(app).referencePointTool();
			var request = tool.bindContext(GeoCeDGPointRequest.forSource(app, source,
					0, 1, LocusPointInteractionPolicy2D.initial(.01)));
			var interaction = new GeoCeDGPointInteraction(app); var before = Before.capture(app);
			try (var offer = interaction.query(source, request)) {
				var point = interaction.createHistorical(offer, request, result -> {
					tool.setPolicy(LocusPointInteractionPolicy2D.initial(.2));
					return result.getCandidates().get(0);
				});
				assertNull(point); before.assertUnchanged(app);
			}
		});
	}

	@Test void preimageCallbackCannotReuseContextAfterReturningToTheSameTool() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = (GeoLocusV2) eval(app,
					"S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
			app.setMode(EuclidianConstants.MODE_POINT);
			var tool = controller(app).referencePointTool();
			var request = tool.bindContext(GeoCeDGPointRequest.forSource(app, source,
					0, 1, LocusPointInteractionPolicy2D.initial(.01)));
			var interaction = new GeoCeDGPointInteraction(app); var before = Before.capture(app);
			try (var offer = interaction.query(source, request)) {
				var point = interaction.createHistorical(offer, request, result -> {
					app.setMode(EuclidianConstants.MODE_POINT_ON_OBJECT);
					app.setMode(EuclidianConstants.MODE_POINT);
					return result.getCandidates().get(0);
				});
				assertNull(point); assertEquals(EuclidianConstants.MODE_POINT, app.getMode());
				before.assertUnchanged(app);
			}
		});
	}

	@Test void sourceChooserCannotReuseAChangedAndRestoredSemanticRevision() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); var source = circle(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			var construction = app.getKernel().getConstruction();
			int count = construction.getGeoSetConstructionOrder().size();
			var id = source.getPersistentLocusId();
			AtomicBoolean chosen = new AtomicBoolean();
			controller(app).semanticSourceChooser = values -> {
				chosen.set(true); var centre = (GeoPoint) app.getKernel().lookupLabel("Centre");
				centre.setCoords(.1, 0, 1); centre.updateCascade();
				centre.setCoords(0, 0, 1); centre.updateCascade();
				return source;
			};
			Cu7M08SDesktopEventsTest.pointer(app, 3, .02);
			assertTrue(chosen.get());
			assertEquals(count, construction.getGeoSetConstructionOrder().size());
			assertNull(controller(app).referencePointTool().pendingSelection());
			assertEquals(id, source.getPersistentLocusId());
			assertTrue(construction.getGeoSetConstructionOrder().stream()
					.noneMatch(geo -> geo instanceof GeoPoint point && GeoCeDGPointInteraction.owns(point)));
		});
	}

	private void assertSemanticPublicationViewGuard(String route) throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			prepareOtherView(app);
			GeoLocusV2 source; double x; double y;
			if ("R1_R6".equals(route)) { source = G9U1SemanticPointInteractionTest.line(app); x = .4; y = .02; }
			else if ("R2_H1".equals(route)) {
				source = Cu7M08SDesktopEventsTest.general(app); x = .8976484384297626; y = 1.7854818601338847;
			} else { source = circle(app); x = 3; y = .02; }
			AtomicBoolean entered = new AtomicBoolean(); var before = Before.capture(app);
			View view = mock(View.class);
			doAnswer(invocation -> {
				GeoElement geo = invocation.getArgument(0);
				if (geo instanceof GeoPoint point && point.getParentAlgorithm()
						instanceof AlgoSemanticLocusPoint2D && entered.compareAndSet(false, true)) {
					switchView(app);
				}
				return null;
			}).when(view).add(any(GeoElement.class));
			try (var offer = new GeoCeDGReferencePlacement(source, x, y,
					LocusPointInteractionPolicy2D.initial(.1))) {
				assertEquals(route, offer.getRoute().name());
				app.getKernel().attach(view);
				try {
					assertEquals("STALE_SELECTION", assertThrows(IllegalStateException.class,
							() -> offer.commit(offer.getChoices().get(0))).getMessage());
				} finally { app.getKernel().detach(view); }
			}
			assertTrue(entered.get()); before.assertUnchanged(app);
		});
	}

	@Test void historicalPublicationRechecksViewAfterOutputAdd() throws Exception {
		assertSemanticPublicationViewGuard("R1_R6");
	}
	@Test void localH1PublicationRechecksViewAfterOutputAdd() throws Exception {
		assertSemanticPublicationViewGuard("R2_H1");
	}
	@Test void individualForwardPublicationRechecksViewAfterOutputAdd() throws Exception {
		assertSemanticPublicationViewGuard("INDIVIDUAL_FORWARD_ADDRESS_CREATION");
	}

	private void assertNativePublicationGuard(boolean changeView) throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			if (changeView) { prepareOtherView(app); }
			var a = (GeoPoint) eval(app, "A=(0,0)"); var b = (GeoPoint) eval(app, "B=(2,0)");
			var source = eval(app, "s=Segment(A,B)");
			var existing = (GeoPoint) eval(app, "Existing=Point(s)");
			var q = (GeoPoint) eval(app, "Ref=(1,.02)");
			long parameter = Double.doubleToLongBits(existing.getPathParameter().t);
			long rawX = Double.doubleToLongBits(existing.x), rawY = Double.doubleToLongBits(existing.y);
			var existingId = app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(existing);
			var before = Before.capture(app); AtomicBoolean entered = new AtomicBoolean();
			View view = mock(View.class);
			doAnswer(invocation -> {
				GeoElement geo = invocation.getArgument(0);
				if (geo instanceof GeoPoint point && point.getPath() == source
						&& entered.compareAndSet(false, true)) {
					if (changeView) { switchView(app); }
					else {
						a.setCoords(.25, 0, 1); b.setCoords(2.25, 0, 1);
						a.updateCascade(); b.updateCascade();
					}
				}
				return null;
			}).when(view).add(any(GeoElement.class));
			try (var offer = new GeoCeDGNativePointPlacement(source, q, 1, .02, .1)) {
				app.getKernel().attach(view);
				try {
					assertEquals("STALE_SELECTION", assertThrows(IllegalStateException.class,
							() -> offer.commit(false)).getMessage());
				} finally { app.getKernel().detach(view); }
			}
			assertTrue(entered.get()); before.assertUnchanged(app);
			var restored = (GeoPoint) app.getKernel().lookupLabel("Existing");
			assertEquals(parameter, Double.doubleToLongBits(restored.getPathParameter().t));
			assertEquals(rawX, Double.doubleToLongBits(restored.x));
			assertEquals(rawY, Double.doubleToLongBits(restored.y));
			assertEquals(existingId, app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(restored));
		});
	}

	@Test void nativePublicationRechecksTheViewAndRestoresOrdinaryEditablePoints() throws Exception {
		assertNativePublicationGuard(true);
	}
	@Test void nativePublicationRechecksTranslatedDomainAndRestoresOrdinaryEditablePoints() throws Exception {
		assertNativePublicationGuard(false);
	}

	@Test void repeatedNativeReferenceCreationIsNotBlockedByAnOrdinaryPathPoint() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = eval(app, "s=Segment((0,0),(2,0))");
			var existing = (GeoPoint) eval(app, "Existing=Point(s)");
			var q = (GeoPoint) eval(app, "Ref=(1,.02)");
			long parameter = Double.doubleToLongBits(existing.getPathParameter().t);
			for (boolean draggable : new boolean[] {true, false, true}) {
				try (var offer = new GeoCeDGNativePointPlacement(source, q, 1, .02, .1)) {
					var point = offer.commit(draggable);
					assertSame(source, point.getPath()); assertEquals(!draggable, point.isLocked());
					assertFalse(GeoCeDGPointInteraction.owns(point));
					assertFalse(point.getAllPredecessors().contains(q));
				}
			}
			assertEquals(parameter, Double.doubleToLongBits(existing.getPathParameter().t));
		});
	}

	@Test void fixedForwardPointStillTracksItsMutableCentre() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); var source = circle(app);
			app.setMode(EuclidianConstants.MODE_POINT); assertTrue(free(app).isSelected()); free(app).doClick();
			Cu7M08SDesktopEventsTest.pointer(app, 3, .02); Cu7M08SDesktopEventsTest.confirm(app);
			var point = Cu7M08SDesktopEventsTest.point(app); assertTrue(point.isLocked());
			var parent = (AlgoSemanticLocusPoint2D) point.getParentAlgorithm(); assertSame(source, parent.getSource());
			var address = parent.getCurrentSemanticAddress(); double x = point.inhomX, y = point.inhomY;
			var centre = (GeoPoint) app.getKernel().lookupLabel("Centre");
			centre.setCoords(1, 1, 1); centre.updateCascade();
			assertTrue(point.isLocked()); assertTrue(point.isDefined());
			assertEquals(address, parent.getCurrentSemanticAddress());
			assertEquals(x + 1, point.inhomX, 1e-8); assertEquals(y + 1, point.inhomY, 1e-8);
		});
	}

	@Test void confirmedDraggabilityWinsOverAnOutputAddFixedFlagChange() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			for (boolean draggable : new boolean[] {true, false}) {
				var app = G9U1SemanticPointInteractionTest.app(); circle(app);
				app.setMode(EuclidianConstants.MODE_POINT);
				if (!draggable) { free(app).doClick(); }
				Cu7M08SDesktopEventsTest.pointer(app, 3, .02);
				AtomicBoolean entered = new AtomicBoolean(); View view = mock(View.class);
				doAnswer(invocation -> {
					GeoElement geo = invocation.getArgument(0);
					if (geo instanceof GeoPoint point && point.getParentAlgorithm()
							instanceof AlgoSemanticLocusPoint2D && entered.compareAndSet(false, true)) {
						point.setFixed(draggable);
					}
					return null;
				}).when(view).add(any(GeoElement.class));
				app.getKernel().attach(view);
				try { Cu7M08SDesktopEventsTest.confirm(app); }
				finally { app.getKernel().detach(view); }
				assertTrue(entered.get()); assertEquals(!draggable, Cu7M08SDesktopEventsTest.point(app).isLocked());
			}
		});
	}

	private static GeoPoint draggableSplinePoint(AppGeoCeDG app) {
		var source = G9U1SemanticPointInteractionTest.spline(app);
		var point = new GeoCeDGPointInteraction(app).create(source, -1, 0, .1, result -> null);
		assertNotNull(point); point.setLabel("DraggedP");
		eval(app, "DragDependent=DraggedP+(1,0)");
		app.setMode(EuclidianConstants.MODE_MOVE);
		Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
		return point;
	}

	@Test void dragViewUpdateSwitchRestoresTheLastSemanticStateAndDependent() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); prepareOtherView(app);
			var point = draggableSplinePoint(app);
			var id = app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
			var address = ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getCurrentSemanticAddress();
			Object originalView = app.getActiveEuclidianView();
			var before = Before.capture(app); AtomicBoolean entered = new AtomicBoolean(); View view = mock(View.class);
			doAnswer(invocation -> {
				if (invocation.getArgument(0) == point && entered.compareAndSet(false, true)) { switchView(app); }
				return null;
			}).when(view).update(any(GeoElement.class));
			app.getKernel().attach(view);
			try { Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, -.9, 0); }
			finally { app.getKernel().detach(view); }
			assertTrue(entered.get()); assertNotSame(originalView, app.getActiveEuclidianView());
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, -.9, 0);
			before.assertUnchanged(app);
			var restored = (GeoPoint) app.getKernel().lookupLabel("DraggedP");
			assertEquals(id, app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(restored));
			assertEquals(address, ((AlgoSemanticLocusPoint2D) restored.getParentAlgorithm()).getCurrentSemanticAddress());
		});
	}

	@Test void dragViewUpdatePolicyOrToolChangeCannotPublishAnObsoleteRequest() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			for (boolean toolChange : new boolean[] {false, true}) {
				var app = G9U1SemanticPointInteractionTest.app(); var point = draggableSplinePoint(app);
				var before = Before.capture(app); AtomicBoolean entered = new AtomicBoolean(); View view = mock(View.class);
				doAnswer(invocation -> {
					if (invocation.getArgument(0) == point && entered.compareAndSet(false, true)) {
						if (toolChange) { app.setMode(EuclidianConstants.MODE_JOIN); }
						else { controller(app).referencePointTool().setPolicy(LocusPointInteractionPolicy2D.initial(.3)); }
					}
					return null;
				}).when(view).update(any(GeoElement.class));
				app.getKernel().attach(view);
				try { Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, -.9, 0); }
				finally { app.getKernel().detach(view); }
				assertTrue(entered.get());
				Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, -.9, 0);
				before.assertUnchanged(app);
				assertEquals(toolChange ? EuclidianConstants.MODE_JOIN : EuclidianConstants.MODE_MOVE, app.getMode());
			}
		});
	}

	@Test void dragSameViewPanKeepsTheWorldRequestCapturedBeforeDagCallbacks() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); var point = draggableSplinePoint(app);
			var cons = app.getKernel().getConstruction();
			var id = cons.getSpatialIdentityRegistry().getPersistentGeoId(point);
			int count = cons.getGeoSetConstructionOrder().size();
			var canvas = app.getEuclidianView1();
			double expectedX = canvas.toRealWorldCoordX(canvas.toScreenCoordX(-.9));
			double expectedY = canvas.toRealWorldCoordY(canvas.toScreenCoordY(0));
			AtomicBoolean entered = new AtomicBoolean(); View view = mock(View.class);
			doAnswer(invocation -> {
				if (invocation.getArgument(0) == point && entered.compareAndSet(false, true)) {
					canvas.setCoordSystem(650, 200, 400, 400);
				}
				return null;
			}).when(view).update(any(GeoElement.class));
			app.getKernel().attach(view);
			try { Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, -.9, 0); }
			finally { app.getKernel().detach(view); }
			assertTrue(entered.get()); assertSame(canvas, app.getActiveEuclidianView());
			assertSame(point, app.getKernel().lookupLabel("DraggedP"));
			assertEquals(expectedX, point.inhomX, 1e-8); assertEquals(expectedY, point.inhomY, 1e-8);
			assertEquals(id, cons.getSpatialIdentityRegistry().getPersistentGeoId(point));
			assertEquals(count, cons.getGeoSetConstructionOrder().size());
			var dependent = (GeoPoint) app.getKernel().lookupLabel("DragDependent");
			assertEquals(point.inhomX + 1, dependent.inhomX, 1e-8);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, -.9, 0);
		});
	}

	private static LocusSemanticAddress2D address(GeoPoint point) {
		return AlgoSemanticLocusPoint2D.requireSemanticParent(point).getCurrentSemanticAddress();
	}

	private static LocusPoint2D authorTarget(GeoLocusV2 source, String branch, double parameter) {
		try (var session = LocusEvaluationSession2D.memoizing(100)) {
			var evaluated = source.getSemanticDefinition().evaluate(branch, parameter, session);
			assertTrue(evaluated.isValid(), evaluated.toString());
			return evaluated.getPoint();
		}
	}

	@Test void exactAuthorDocumentHReferenceCreationDragsBothWaysAndReopens(@TempDir Path directory)
			throws Exception {
		Path original = directory.resolve("TestM08-S.cedg");
		try (var stream = getClass().getResourceAsStream("cu7-general-creation/TestM08-S.cedg")) {
			assertNotNull(stream, "The exact author fixture must be bundled, never skipped");
			Files.copy(stream, original);
		}
		String originalHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(Files.readAllBytes(original)));
		assertEquals("4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d", originalHash);
		var host = new AtomicReference<AppGeoCeDG>();
		var pointId = new AtomicReference<Object>();
		var sourceId = new AtomicReference<Object>();
		var initial = new AtomicReference<LocusSemanticAddress2D>();
		var moved = new AtomicReference<LocusSemanticAddress2D>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); host.set(app);
			assertTrue(app.loadFile(original.toFile(), false));
			var h = (GeoLocusV2) app.getKernel().lookupLabel("h"); assertNotNull(h);
			var priorIds = identities(app);
			var reference = (GeoPoint) eval(app,
					"ReferenceR1=(0.8976484384297626,1.7854818601338847)");
			try (var offer = new GeoCeDGReferencePlacement(h, reference,
					LocusPointInteractionPolicy2D.initial(.1))) {
				assertEquals(GeoCeDGReferencePlacement.Route.R2_H1, offer.getRoute());
				assertEquals(1, offer.getChoices().size());
				var point = offer.commit(offer.getChoices().get(0)); point.setLabel("AuthorHPoint");
				assertTrue(GeoCeDGPointInteraction.owns(point)); assertFalse(point.isLocked());
				assertFalse(point.getAllPredecessors().contains(reference));
				assertSame(h, AlgoSemanticLocusPoint2D.requireSemanticParent(point).getSource());
				initial.set(address(point)); sourceId.set(h.getPersistentLocusId());
				pointId.set(app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(point)); assertNotNull(pointId.get());
				assertTrue(identities(app).containsAll(priorIds));
			}
			eval(app, "AuthorHDependent=AuthorHPoint+(1,2)");
			for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
				geo.setEuclidianVisible(geo == h || "AuthorHPoint".equals(geo.getLabelSimple()));
				geo.updateRepaint();
			}
			app.getEuclidianView1().setCoordSystem(220, 420, 100, 100);
			app.getKernel().initUndoInfo();
		});
		var app = host.get();
		var undo = app.getKernel().getConstruction().getUndoManager();
		Cu7M08SDesktopEventsTest.await(() -> undo.getHistorySize() == 0);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var h = (GeoLocusV2) app.getKernel().lookupLabel("h");
			var point = (GeoPoint) app.getKernel().lookupLabel("AuthorHPoint");
			var dependent = (GeoPoint) app.getKernel().lookupLabel("AuthorHDependent");
			int count = app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
			app.setMode(EuclidianConstants.MODE_MOVE);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
			double previousParameter = initial.get().getCanonicalParameter(), previousOffset = 0;
			LocusPoint2D target = null;
			for (double offset : new double[] {.08, .16, .08, -.04}) {
				target = authorTarget(h, initial.get().getBranchKey(),
						initial.get().getCanonicalParameter() + offset);
				Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, target.getX(), target.getY());
				var current = address(point);
				assertTrue((offset - previousOffset) * (current.getCanonicalParameter() - previousParameter) > 0,
						"The exact author h must continue in both requested directions");
				assertEquals(initial.get().getBranchKey(), current.getBranchKey());
				assertEquals(initial.get().getComponentLineageKey(), current.getComponentLineageKey());
				assertEquals(initial.get().getSourceLocusId(), current.getSourceLocusId());
				assertSame(point, app.getKernel().lookupLabel("AuthorHPoint"));
				assertEquals(pointId.get(), app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(point));
				assertEquals(count, app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
				assertEquals(point.inhomX + 1, dependent.inhomX, 1e-10);
				assertEquals(point.inhomY + 2, dependent.inhomY, 1e-10);
				var forward = authorTarget(h, current.getBranchKey(), current.getCanonicalParameter());
				assertEquals(forward.getX(), point.inhomX, 1e-11);
				assertEquals(forward.getY(), point.inhomY, 1e-11);
				previousOffset = offset; previousParameter = current.getCanonicalParameter();
			}
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, target.getX(), target.getY());
			moved.set(address(point)); assertNotEquals(initial.get(), moved.get());
		});
		Cu7M08SDesktopEventsTest.await(undo::undoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertEquals(1, undo.getHistorySize(), "The author-h controller gesture is one Undo");
			app.getKernel().undo();
		});
		Cu7M08SDesktopEventsTest.await(undo::redoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var point = (GeoPoint) app.getKernel().lookupLabel("AuthorHPoint");
			assertEquals(initial.get(), address(point));
			assertEquals(pointId.get(), app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(point)); app.getKernel().redo();
		});
		Cu7M08SDesktopEventsTest.await(() -> undo.getHistorySize() == 1);
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertEquals(moved.get(), address((GeoPoint) app.getKernel().lookupLabel("AuthorHPoint")));
			Path file = directory.resolve("author-h-drag.cedg");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
			var reopened = G9U1SemanticPointInteractionTest.app();
			assertTrue(reopened.loadFile(file.toFile(), false));
			var point = (GeoPoint) reopened.getKernel().lookupLabel("AuthorHPoint");
			var h = (GeoLocusV2) reopened.getKernel().lookupLabel("h");
			assertEquals(pointId.get(), reopened.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(point));
			assertEquals(sourceId.get(), h.getPersistentLocusId()); assertEquals(moved.get(), address(point));
			assertSame(h, AlgoSemanticLocusPoint2D.requireSemanticParent(point).getSource());
			assertTrue(GeoCeDGPointInteraction.owns(point));
			assertNotNull(reopened.getKernel().lookupLabel("ReferenceR1"));
			var target = authorTarget(h, moved.get().getBranchKey(), moved.get().getCanonicalParameter() + .06);
			reopened.setMode(EuclidianConstants.MODE_MOVE);
			Cu7M08SDesktopEventsTest.mouse(reopened, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
			Cu7M08SDesktopEventsTest.mouse(reopened, MouseEvent.MOUSE_DRAGGED, target.getX(), target.getY());
			Cu7M08SDesktopEventsTest.mouse(reopened, MouseEvent.MOUSE_RELEASED, target.getX(), target.getY());
			assertTrue(address(point).getCanonicalParameter() > moved.get().getCanonicalParameter());
			var dependent = (GeoPoint) reopened.getKernel().lookupLabel("AuthorHDependent");
			assertEquals(point.inhomX + 1, dependent.inhomX, 1e-10);
			assertEquals(point.inhomY + 2, dependent.inhomY, 1e-10);
		});
		assertEquals(originalHash, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(Files.readAllBytes(original))));
	}
}
