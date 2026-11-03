/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.awt.event.MouseEvent;
import java.awt.KeyboardFocusManager;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.View;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Native AWT listener regressions; physical mouse smoke is recorded separately. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class GeneralPointInteractionDesktopTest {

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
	private static GeoPoint nativePoint(AppGeoCeDG app, GeoElement source) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast)
				.filter(point -> point.getPath() == source).findFirst().orElseThrow();
	}
	private static AlgoSemanticLocusPoint2D parent(GeoPoint point) {
		return (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
	}
	private static GeoLocusV2 circle(AppGeoCeDG app) {
		for (String command : List.of("c=Circle((0,0),1)", "N=Point(c)",
				"v=Vector((0,0),N)", "Q=N+(2*v)", "L=LocusV2(Q,N)")) { eval(app, command); }
		GeoLocusV2 locus = (GeoLocusV2) app.getKernel().lookupLabel("L");
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			geo.setEuclidianVisible(geo == locus); geo.updateRepaint();
		}
		return locus;
	}

	@Test void ordinaryPointDefaultsToDraggableAndRetainsNativePath() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = eval(app, "s=Segment((0,0),(2,0))");
			app.setMode(EuclidianConstants.MODE_POINT);
			assertNotNull(free(app)); assertTrue(free(app).isSelected());
			Cu7M08SDesktopEventsTest.pointer(app, .5, 0);
			var point = nativePoint(app, source);
			assertFalse(point.isLocked()); assertFalse(GeoCeDGPointInteraction.owns(point));
			assertEquals(.5, point.inhomX, .02);
			assertTrue(point.isPointerChangeable());
			// Native constrained motion is checked here; physical mouse drag has its own smoke.
			point.setCoords(1.2, .1, 1); point.updateCascade();
			assertEquals(1.2, point.inhomX, .04);
			assertEquals(0, point.inhomY, 1e-10);
		});
	}

	@Test void fixedNativePointStillFollowsItsSourceAndPersists(@TempDir Path directory) throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var a = (GeoPoint) eval(app, "A=(0,0)");
			var b = (GeoPoint) eval(app, "B=(2,0)");
			var source = eval(app, "s=Segment(A,B)");
			app.setMode(EuclidianConstants.MODE_POINT_ON_OBJECT);
			free(app).doClick(); assertFalse(free(app).isSelected());
			Cu7M08SDesktopEventsTest.pointer(app, .5, 0);
			var point = nativePoint(app, source);
			assertTrue(point.isLocked());
			String label = point.getLabelSimple();
			b.setCoords(4, 0, 1); b.updateCascade();
			assertEquals(1, point.inhomX, .04); assertTrue(point.isLocked());
			a.setCoords(0, 1, 1); a.updateCascade();
			assertTrue(point.inhomY > 0);
			Path file = directory.resolve("native-fixed.cedg");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
			var reopened = G9U1SemanticPointInteractionTest.app();
			assertTrue(reopened.loadFile(file.toFile(), false));
			var restored = (GeoPoint) reopened.getKernel().lookupLabel(label);
			assertTrue(restored.isLocked());
			assertSame(reopened.getKernel().lookupLabel("s"), restored.getPath());
		});
	}

	@Test void ordinaryPointReferenceOptionCopiesQAndUsesNativeConstruction() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = eval(app, "s=Segment((0,0),(2,0))");
			var q = (GeoPoint) eval(app, "Ref=(1,.03)");
			app.setMode(EuclidianConstants.MODE_POINT);
			var toggle = Cu7M08SDesktopEventsTest.find(Cu7M08SDesktopEventsTest.panel(app),
					JToggleButton.class, value -> "m08-from-reference".equals(value.getName()));
			assertNotNull(toggle); toggle.doClick(); free(app).doClick();
			Cu7M08SDesktopEventsTest.pointer(app, .5, 0);
			Cu7M08SDesktopEventsTest.pointer(app, q.inhomX, q.inhomY);
			assertNotNull(controller(app).referencePointTool().contextMenu());
			Cu7M08SDesktopEventsTest.confirm(app);
			var point = nativePoint(app, source);
			assertTrue(point.isLocked()); assertFalse(point.getAllPredecessors().contains(q));
			assertEquals(1, point.inhomX, 1e-10);
			q.setCoords(8, 9, 1); q.updateCascade();
			assertEquals(1, point.inhomX, 1e-10); assertEquals(0, point.inhomY, 1e-10);
		});
	}

	@Test void nativeOffersRejectChangedQAndFailedDistanceWithoutMutation() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = eval(app, "s=Segment((0,0),(2,0))");
			var q = (GeoPoint) eval(app, "Ref=(1,.03)");
			try (var offer = new GeoCeDGNativePointPlacement(source, q, q.inhomX, q.inhomY, .1)) {
				q.setCoords(1.2, .03, 1); q.updateCascade();
				String before = app.getXML();
				assertEquals("STALE_SELECTION", assertThrows(IllegalStateException.class,
						() -> offer.commit(true)).getMessage());
				assertEquals(before, app.getXML());
			}
			try (var offer = new GeoCeDGNativePointPlacement(source, null, 1, 4, .1)) {
				String before = app.getXML();
				assertEquals("NATIVE_PLACEMENT_REJECTED", assertThrows(IllegalStateException.class,
						() -> offer.commit(false)).getMessage());
				assertEquals(before, app.getXML());
			}
		});
	}

	@Test void nativeLinearAndConicSourcesKeepClickAndQParity() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			String[] definitions = {"s=Line((0,0),(2,0))", "s=Ray((0,0),(1,0))",
					"s=Circle((0,0),1)", "s=Ellipse((-1,0),(1,0),2)"};
			double[][] requests = {{.5, .03}, {.5, .03}, {1.03, 0}, {2.03, 0}};
			for (int i = 0; i < definitions.length; i++) {
				var app = G9U1SemanticPointInteractionTest.app();
				var source = eval(app, definitions[i]);
				double x = requests[i][0], y = requests[i][1];
				var q = (GeoPoint) eval(app, "Ref=(" + x + "," + y + ")");
				try (var world = new GeoCeDGNativePointPlacement(source, null, x, y, .1);
						var reference = new GeoCeDGNativePointPlacement(source, q, x, y, .1)) {
					// Native Q and world placement retain the ordinary path constructor.
					var b = reference.commit(false); var a = world.commit(true);
					assertEquals(a.inhomX, b.inhomX, 1e-10);
					assertEquals(a.inhomY, b.inhomY, 1e-10);
					assertFalse(a.isLocked()); assertTrue(b.isLocked());
					assertSame(source, a.getPath()); assertSame(source, b.getPath());
					assertFalse(b.getAllPredecessors().contains(q));
				}
			}
		});
	}

	private void assertNativePublicationMutationRejected(Consumer<AppGeoCeDG> mutation) throws Exception {
		var host = new AtomicReference<AppGeoCeDG>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); host.set(app);
			eval(app, "A=(0,0)"); eval(app, "B=(2,0)"); eval(app, "s=Segment(A,B)");
			eval(app, "Ref=(1,.03)"); app.getKernel().initUndoInfo();
		});
		var app = host.get(); var construction = app.getKernel().getConstruction();
		var undo = construction.getUndoManager();
		Cu7M08SDesktopEventsTest.await(() -> undo.getHistorySize() == 0);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var source = app.getKernel().lookupLabel("s");
			var reference = (GeoPoint) app.getKernel().lookupLabel("Ref");
			String original = app.getXML(); int geos = construction.getGeoSetConstructionOrder().size();
			var registry = construction.getSpatialIdentityRegistry();
			var identities = registry.getRecords().stream().map(record -> record.getId())
					.collect(Collectors.toSet());
			int reservations = registry.getReservedIdentityCount(), history = undo.getHistorySize();
			AtomicBoolean entered = new AtomicBoolean(); View view = mock(View.class);
			doAnswer(invocation -> {
				GeoElement added = invocation.getArgument(0);
				if (added instanceof GeoPoint point && point.getPath() == source
						&& entered.compareAndSet(false, true)) { mutation.accept(app); }
				return null;
			}).when(view).add(any(GeoElement.class));
			try (var offer = new GeoCeDGNativePointPlacement(source, reference, 1, .03, .1)) {
				app.getKernel().attach(view);
				try {
					assertEquals("STALE_SELECTION", assertThrows(IllegalStateException.class,
							() -> offer.commit(false)).getMessage());
				} finally { app.getKernel().detach(view); }
				assertTrue(entered.get(), "The mutation must occur inside synchronous native publication");
				assertEquals("CONSUMED_SELECTION", assertThrows(IllegalStateException.class,
						() -> offer.commit(true)).getMessage());
			}
			assertEquals(original, app.getXML()); assertEquals(geos, construction.getGeoSetConstructionOrder().size());
			assertEquals(identities, registry.getRecords().stream().map(record -> record.getId())
					.collect(Collectors.toSet()));
			assertEquals(reservations, registry.getReservedIdentityCount());
			assertEquals(history, undo.getHistorySize());
		});
	}

	@Test void nativeReferenceChangedInsideOutputAddRejectsAndRestoresExactly() throws Exception {
		assertNativePublicationMutationRejected(app -> {
			var reference = (GeoPoint) app.getKernel().lookupLabel("Ref");
			reference.setCoords(1, .04, 1); reference.updateCascade();
		});
	}

	@Test void nativeSourceChangedInsideOutputAddRejectsEvenWhenPlacementRemainsNearRequest()
			throws Exception {
		assertNativePublicationMutationRejected(app -> {
			var endpoint = (GeoPoint) app.getKernel().lookupLabel("B");
			endpoint.setCoords(2, .02, 1); endpoint.updateCascade();
		});
	}

	@Test void nativeReferenceWithExistingOrdinaryPathPointKeepsBothCreationRoutesAvailable() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = eval(app, "s=Segment((0,0),(2,0))"); eval(app, "Existing=Point(s)");
			var reference = (GeoPoint) eval(app, "Ref=(1,.03)");
			var existing = (GeoPoint) app.getKernel().lookupLabel("Existing");
			long parameter = Double.doubleToLongBits(existing.getPathParameter().t);
			var construction = app.getKernel().getConstruction();
			int geos = construction.getGeoSetConstructionOrder().size();
			int history = construction.getUndoManager().getHistorySize();
			try (var offer = new GeoCeDGNativePointPlacement(source, reference, 1, .03, .1)) {
				var point = offer.commit(true);
				assertNotNull(point); assertSame(source, point.getPath());
				assertFalse(point.getAllPredecessors().contains(reference));
			}
			assertEquals(parameter, Double.doubleToLongBits(existing.getPathParameter().t));
			assertEquals(geos + 1, construction.getGeoSetConstructionOrder().size());
			assertEquals(history, construction.getUndoManager().getHistorySize());
			app.setMode(EuclidianConstants.MODE_POINT);
			Cu7M08SDesktopEventsTest.pointer(app, 1.5, 0);
			assertEquals(3, construction.getGeoSetConstructionOrder().stream()
					.filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast)
					.filter(point -> point.getPath() == source).count());
		});
	}

	@Test void pointSourceChooserUsesWorldPositionAndRadiusCapturedBeforeViewChange() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = G9U1SemanticPointInteractionTest.line(app);
			var overlap = (GeoLocusV2) eval(app, "T=Translate(L,(0,0))");
			source.setEuclidianVisible(true); source.updateRepaint();
			overlap.setEuclidianVisible(true); overlap.updateRepaint();
			app.setMode(EuclidianConstants.MODE_POINT);
			var view = app.getEuclidianView1();
			double expectedX = view.toRealWorldCoordX(view.toScreenCoordX(.4));
			double expectedY = view.toRealWorldCoordY(view.toScreenCoordY(.03));
			controller(app).semanticSourceChooser = sources -> {
				assertEquals(2, sources.size());
				view.setCoordSystem(100, 100, 1000, 1000);
				return source;
			};
			Cu7M08SDesktopEventsTest.pointer(app, .4, .03);
			var point = Cu7M08SDesktopEventsTest.point(app);
			assertSame(source, parent(point).getSource());
			assertEquals(expectedX, point.inhomX, 1e-8);
			assertEquals(0, point.inhomY, 1e-8);
			assertTrue(expectedY > .02, "Old world radius must admit the captured offset");
		});
	}

	@Test void generalCircleClickAndReferenceCreateOwnedPointsAndDragSameIdentity() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = circle(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			Cu7M08SDesktopEventsTest.pointer(app, 3, .02);
			var selection = controller(app).referencePointTool().pendingSelection();
			assertNotNull(selection);
			assertEquals(GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION, selection.getRoute());
			Cu7M08SDesktopEventsTest.confirm(app);
			var point = Cu7M08SDesktopEventsTest.point(app);
			point.setLabel("InteractiveP");
			assertFalse(point.isLocked()); assertSame(source, parent(point).getSource());
			var dependent = (GeoPoint) eval(app, "Dependent=InteractiveP+(1,0)");
			var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
			var identity = registry.getPersistentGeoId(point);
			var q = (GeoPoint) eval(app, "Ref=(2.99,.02)"); q.setEuclidianVisible(false);
			try (var offer = new GeoCeDGReferencePlacement(source, q,
					LocusPointInteractionPolicy2D.initial(.1))) {
				assertEquals(GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION, offer.getRoute());
				var second = offer.commit(offer.getChoices().get(0));
				assertTrue(GeoCeDGPointInteraction.owns(second));
				assertFalse(second.getAllPredecessors().contains(q));
				second.setEuclidianVisible(false); second.updateRepaint();
			}
			int before = G9U1SemanticPointInteractionTest.size(app);
			app.setMode(EuclidianConstants.MODE_MOVE);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
			for (double angle : new double[] {.02, .04, .06, .04, .02}) {
				Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED,
						3 * Math.cos(angle), 3 * Math.sin(angle));
			}
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED,
					3 * Math.cos(.02), 3 * Math.sin(.02));
			assertSame(point, app.getKernel().lookupLabel("InteractiveP"));
			assertEquals(identity, registry.getPersistentGeoId(point));
			assertEquals(before, G9U1SemanticPointInteractionTest.size(app));
			assertEquals(3 * Math.sin(.02), point.inhomY, .03);
			assertEquals(point.inhomX + 1, dependent.inhomX, 1e-9);
			assertEquals(point.inhomY, dependent.inhomY, 1e-9);
		});
	}

	@Test void fixedSemanticPointRefusesDragWithoutChangingItsAddress() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = G9U1SemanticPointInteractionTest.spline(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			free(app).doClick();
			Cu7M08SDesktopEventsTest.pointer(app, -1, 0);
			var point = Cu7M08SDesktopEventsTest.point(app);
			assertTrue(point.isLocked()); assertSame(source, parent(point).getSource());
			var address = parent(point).getCurrentSemanticAddress();
			String before = app.getXML();
			assertFalse(new GeoCeDGPointInteraction(app).move(point, 1, 0, .1));
			assertEquals(address, parent(point).getCurrentSemanticAddress());
			assertEquals(before, app.getXML());
		});
	}

	@Test void forwardConfirmationBudgetFailureHasNoEdtExceptionPointOrUndo() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = circle(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			var tool = controller(app).referencePointTool();
			tool.queryWorld(source, 3, .02);
			var measured = tool.pendingSelection(); assertNotNull(measured);
			assertEquals(GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
					measured.getRoute());
			int queryCost = Math.toIntExact(measured.getSemanticMisses());
			assertTrue(queryCost > 0);
			var original = LocusPointInteractionPolicy2D.initial(.1);
			var work = original.getWorkBudget();
			tool.setPolicy(new LocusPointInteractionPolicy2D(.1, original.getEqualDistanceTolerance(),
					original.getParameterTolerance(), new LocusPointInteractionWorkBudget2D(queryCost,
							work.getMaximumSubdivisions(), work.getMaximumRefinementIterations(),
							work.getMaximumCandidates())));
			tool.queryWorld(source, 3, .02);
			var exhausted = tool.pendingSelection(); assertNotNull(exhausted);
			assertEquals(GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
					exhausted.getRoute());
			String before = app.getXML();
			var undo = app.getKernel().getConstruction().getUndoManager();
			int history = undo.getHistorySize();
			assertDoesNotThrow(() -> Cu7M08SDesktopEventsTest.confirm(app));
			assertEquals("BUDGET_EXHAUSTED", tool.lastFailure());
			assertNull(tool.pendingSelection());
			assertEquals(before, app.getXML());
			assertEquals(history, undo.getHistorySize());
		});
	}

	@Test void unchangedSemanticDragDoesNotChangeStateOrUndo() throws Exception {
		var host = new AtomicReference<AppGeoCeDG>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); host.set(app);
			new GeoCeDGPointInteraction(app).create(G9U1SemanticPointInteractionTest.spline(app),
					-1, 0, .1, ignored -> null);
			app.getKernel().initUndoInfo();
		});
		var app = host.get();
		var undo = app.getKernel().getConstruction().getUndoManager();
		Cu7M08SDesktopEventsTest.await(() -> undo.getHistorySize() == 0);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var point = Cu7M08SDesktopEventsTest.point(app);
			app.setMode(EuclidianConstants.MODE_MOVE);
			String before = app.getXML(); int history = undo.getHistorySize();
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, point.inhomX, point.inhomY);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, point.inhomX, point.inhomY);
			assertEquals(before, app.getXML()); assertEquals(history, undo.getHistorySize());
		});
	}

	@Test void newForwardDragUndoRedoReopenAndFullCopyPreserveSemanticIdentity(@TempDir Path directory)
			throws Exception {
		var host = new AtomicReference<AppGeoCeDG>();
		var identity = new AtomicReference<Object>();
		var sourceIdentity = new AtomicReference<Object>();
		var initialAddress = new AtomicReference<LocusSemanticAddress2D>();
		var movedAddress = new AtomicReference<LocusSemanticAddress2D>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); host.set(app);
			var source = circle(app);
			try (var offer = new GeoCeDGReferencePlacement(source, 3, .02,
					LocusPointInteractionPolicy2D.initial(.1))) {
				assertEquals(GeoCeDGReferencePlacement.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION,
						offer.getRoute());
				var point = offer.commit(offer.getChoices().get(0)); point.setLabel("PersistentP");
				point.setEuclidianVisible(true); point.updateRepaint();
				identity.set(app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(point));
				sourceIdentity.set(source.getPersistentLocusId());
				initialAddress.set(parent(point).getCurrentSemanticAddress());
			}
			app.getKernel().initUndoInfo();
		});
		var app = host.get();
		var undo = app.getKernel().getConstruction().getUndoManager();
		Cu7M08SDesktopEventsTest.await(() -> undo.getHistorySize() == 0);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var point = (GeoPoint) app.getKernel().lookupLabel("PersistentP");
			app.setMode(EuclidianConstants.MODE_MOVE);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED,
					3 * Math.cos(.04), 3 * Math.sin(.04));
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED,
					3 * Math.cos(.04), 3 * Math.sin(.04));
			assertSame(point, app.getKernel().lookupLabel("PersistentP"));
			movedAddress.set(parent(point).getCurrentSemanticAddress());
			assertNotEquals(initialAddress.get(), movedAddress.get());
		});
		Cu7M08SDesktopEventsTest.await(undo::undoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertEquals(1, undo.getHistorySize(), "One complete controller drag is one Undo");
			app.getKernel().undo();
		});
		Cu7M08SDesktopEventsTest.await(undo::redoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var point = (GeoPoint) app.getKernel().lookupLabel("PersistentP");
			assertEquals(initialAddress.get(), parent(point).getCurrentSemanticAddress());
			assertEquals(identity.get(), app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(point));
			assertFalse(undo.undoPossible());
			app.getKernel().redo();
		});
		Cu7M08SDesktopEventsTest.await(() -> undo.getHistorySize() == 1);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var point = (GeoPoint) app.getKernel().lookupLabel("PersistentP");
			assertEquals(movedAddress.get(), parent(point).getCurrentSemanticAddress());
			assertEquals(identity.get(), app.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(point));
			Path file = directory.resolve("new-forward-drag.cedg");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
			var reopened = G9U1SemanticPointInteractionTest.app();
			assertTrue(reopened.loadFile(file.toFile(), false));
			var restored = (GeoPoint) reopened.getKernel().lookupLabel("PersistentP");
			var registry = reopened.getKernel().getConstruction().getSpatialIdentityRegistry();
			assertEquals(identity.get(), registry.getPersistentGeoId(restored));
			assertEquals(movedAddress.get(), parent(restored).getCurrentSemanticAddress());
			assertEquals(sourceIdentity.get(), parent(restored).getSource().getPersistentLocusId());
			assertTrue(GeoCeDGPointInteraction.owns(restored));
			reopened.setMode(EuclidianConstants.MODE_MOVE);
			Cu7M08SDesktopEventsTest.mouse(reopened, MouseEvent.MOUSE_PRESSED, restored.inhomX, restored.inhomY);
			Cu7M08SDesktopEventsTest.mouse(reopened, MouseEvent.MOUSE_DRAGGED,
					3 * Math.cos(.06), 3 * Math.sin(.06));
			Cu7M08SDesktopEventsTest.mouse(reopened, MouseEvent.MOUSE_RELEASED,
					3 * Math.cos(.06), 3 * Math.sin(.06));
			assertNotEquals(movedAddress.get(), parent(restored).getCurrentSemanticAddress());
			var retained = parent(restored).getCurrentSemanticAddress();
			var originalSource = parent(restored).getSource();
			String clipboard = InternalClipboard.getTextToSave(reopened,
					List.of(restored, originalSource), text -> text);
			G9U1SemanticPointInteractionTest.paste(reopened, clipboard);
			registry = reopened.getKernel().getConstruction().getSpatialIdentityRegistry();
			var copiedRecord = registry.getRecords().stream().filter(GeoIdentityRecord.class::isInstance)
					.map(GeoIdentityRecord.class::cast)
					.filter(record -> identity.get().equals(record.getCopySourceId()))
					.findFirst().orElseThrow();
			var copy = (GeoPoint) registry.getGeo(copiedRecord.getId());
			var copiedSource = parent(copy).getSource();
			assertNotEquals(identity.get(), copiedRecord.getId());
			assertNotSame(originalSource, copiedSource);
			assertNotEquals(sourceIdentity.get(), copiedSource.getPersistentLocusId());
			assertEquals(sourceIdentity.get(), registry.getGeoRecord(copiedSource.getPersistentLocusId())
					.getCopySourceId());
			assertTrue(GeoCeDGPointInteraction.owns(copy));
			restored.setEuclidianVisible(false); restored.updateRepaint();
			copy.setEuclidianVisible(true); copy.updateRepaint();
			var copiedAddress = parent(copy).getCurrentSemanticAddress();
			Cu7M08SDesktopEventsTest.mouse(reopened, MouseEvent.MOUSE_PRESSED, copy.inhomX, copy.inhomY);
			Cu7M08SDesktopEventsTest.mouse(reopened, MouseEvent.MOUSE_DRAGGED,
					3 * Math.cos(.08), 3 * Math.sin(.08));
			Cu7M08SDesktopEventsTest.mouse(reopened, MouseEvent.MOUSE_RELEASED,
					3 * Math.cos(.08), 3 * Math.sin(.08));
			assertNotEquals(copiedAddress, parent(copy).getCurrentSemanticAddress());
			assertEquals(copiedRecord.getId(), registry.getPersistentGeoId(copy));
			assertEquals(retained, parent(restored).getCurrentSemanticAddress());
		});
	}

	@Test void barrierNoticeRemainsVisibleWithoutTooltipsAndExpiresWithoutFocusOrUndoChanges()
			throws Exception {
		var host = new AtomicReference<AppGeoCeDG>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); host.set(app);
			var source = circle(app);
			try (var offer = new GeoCeDGReferencePlacement(source, 3, .02,
					LocusPointInteractionPolicy2D.initial(.1))) {
				var point = offer.commit(offer.getChoices().get(0)); point.setLabel("FeedbackP");
				point.setEuclidianVisible(true); point.updateRepaint();
			}
			app.getKernel().initUndoInfo();
		});
		var app = host.get();
		var undo = app.getKernel().getConstruction().getUndoManager();
		Cu7M08SDesktopEventsTest.await(() -> undo.getHistorySize() == 0);
		Cu7M08SDesktopEventsTest.edt(() -> {
			var point = (GeoPoint) app.getKernel().lookupLabel("FeedbackP");
			app.setMode(EuclidianConstants.MODE_MOVE);
			app.getEuclidianView1().setAllowToolTips(EuclidianStyleConstants.TOOLTIPS_OFF);
			var bar = app.getStatusBar();
			var expiries = new ArrayList<Runnable>();
			bar.setNoticeTimer((delay, expiry) -> {
				assertTrue(delay > 0); expiries.add(expiry);
				return new Runnable() { @Override public void run() { } };
			});
			bar.showPasteNotice("Unit notice retained");
			int height = bar.getPreferredSize().height;
			String before = app.getXML(); int history = undo.getHistorySize();
			var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
			var identity = registry.getPersistentGeoId(point);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
			var focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, 0, 0);
			JLabel notice = bar.getSegment(GeoCeDGStatusBar.POINT_INTERACTION_NOTICE_SEGMENT);
			assertTrue(notice.isVisible()); assertFalse(notice.getText().isEmpty());
			int firstExpiry = expiries.size() - 1;
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, 0, 0);
			int latestExpiry = expiries.size() - 1;
			String diagnostic = notice.getText();
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, 0, 0);
			assertEquals(latestExpiry + 1, expiries.size(), "Repeated feedback does not restart a timer");
			expiries.get(firstExpiry).run();
			assertTrue(notice.isVisible(), "An old expiry cannot clear the new notice");
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, 0, 0);
			assertTrue(notice.isVisible(), "Feedback remains readable after release with tooltips OFF");
			assertEquals(diagnostic, notice.getText()); assertFalse(notice.isFocusable());
			assertEquals(height, bar.getPreferredSize().height, "Status feedback adds no layout row");
			bar.setSize(800, height); bar.doLayout();
			assertTrue(notice.getWidth() > 0);
			assertEquals(0, bar.getComponentZOrder(notice), "Feedback has priority over long unit labels");
			assertSame(focus, KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
			assertTrue(app.getSelectionManager().getSelectedGeos().contains(point));
			assertEquals(before, app.getXML()); assertEquals(history, undo.getHistorySize());
			assertEquals(identity, registry.getPersistentGeoId(point));
			expiries.get(latestExpiry).run(); assertFalse(notice.isVisible());
			assertTrue(bar.getSegment(GeoCeDGStatusBar.PASTE_NOTICE_SEGMENT).isVisible());
			assertEquals("Unit notice retained", bar.getSegment(GeoCeDGStatusBar.PASTE_NOTICE_SEGMENT).getText());
			bar.showPointInteractionNotice("Retry");
			app.setMode(EuclidianConstants.MODE_POINT); assertFalse(notice.isVisible());
			eval(app, "nativeSource=Segment((-1,-1),(1,-1))");
			eval(app, "Existing=Point(nativeSource)");
			app.setMode(EuclidianConstants.MODE_MOVE);
			String unsupportedHost = app.getXML(); int unsupportedHistory = undo.getHistorySize();
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, point.inhomX, point.inhomY);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, 3, .1);
			String hostDiagnostic = "es".equals(app.getLocale().getLanguage())
					? "El estado actual del documento no permite una restauración exacta"
					: "Current document state does not permit exact restoration";
			assertEquals(hostDiagnostic, notice.getText()); assertTrue(notice.isVisible());
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, 3, .1);
			assertEquals(hostDiagnostic, notice.getText()); assertTrue(notice.isVisible());
			assertEquals(unsupportedHost, app.getXML()); assertEquals(unsupportedHistory, undo.getHistorySize());
		});
	}
}
