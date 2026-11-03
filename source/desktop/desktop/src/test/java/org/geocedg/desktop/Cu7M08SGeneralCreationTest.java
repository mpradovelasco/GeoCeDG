/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JToggleButton;
import javax.swing.JList;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Creation regressions on the native PHASE test path; AWT is not physical input. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Cu7M08SGeneralCreationTest {
	private static final String NATIVE_SHA =
			"4f6e1be3c42a892f63f6791f3b8ca6994443863af5c5ad2039531604e27c898d";

	@BeforeAll static void initialize() {
		G9U1SemanticPointInteractionTest.initializeDesktop();
	}

	private static AlgoSemanticLocusPoint2D parent(GeoPoint point) {
		return (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
	}

	private static GeoCeDGReferencePointTool tool(AppGeoCeDG app) {
		return G9U1SemanticPointInteractionTest.controller(app).referencePointTool();
	}

	@Test void worldAndReferenceUseTheSameDirectionWithoutAQParent() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = Cu7M08SDesktopEventsTest.general(app);
			var q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
					"ReferenceR1=(0.8976484384297626,1.7854818601338847)");
			var policy = LocusPointInteractionPolicy2D.initial(.1);
			try (var world = new GeoCeDGReferencePlacement(source, q.inhomX, q.inhomY, policy);
					var reference = new GeoCeDGReferencePlacement(source, q, policy)) {
				assertEquals(GeoCeDGReferencePlacement.Route.R2_H1, world.getRoute());
				assertEquals(world.getRoute(), reference.getRoute());
				var address = world.getChoices().get(0).getAddress();
				assertEquals(address, reference.getChoices().get(0).getAddress());
				var p = world.commit(world.getChoices().get(0));
				var r = reference.commit(reference.getChoices().get(0));
				assertEquals(address, parent(p).getCurrentSemanticAddress());
				assertEquals(address, parent(r).getCurrentSemanticAddress());
				assertFalse(p.getAllPredecessors().contains(q));
				assertFalse(r.getAllPredecessors().contains(q));
				assertNull(app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(q));
				p.setLabel("WorldP");
				var z = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app, "Z=WorldP+(1,0)");
				double x = p.inhomX, y = p.inhomY;
				q.setCoords(20, 30, 1); q.updateCascade();
				assertEquals(x, p.inhomX, 0); assertEquals(y, p.inhomY, 0);
				var center = (GeoPoint) app.getKernel().lookupLabel("I");
				center.setCoords(center.inhomX + .1, center.inhomY, 1); center.updateCascade();
				assertTrue(Math.hypot(p.inhomX - x, p.inhomY - y) > 1e-8);
				assertEquals(p.inhomX + 1, z.inhomX, 1e-10);
				assertEquals(p.inhomY, z.inhomY, 1e-10);
				assertEquals(address, parent(p).getCurrentSemanticAddress());
			}
		});
	}

	@Test void realCanvasClickRequiresConfirmationAndDoesNotStartGeneralDrag() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = Cu7M08SDesktopEventsTest.general(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			String before = app.getXML();
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, .8, 2.4);
			var selection = tool(app).pendingSelection();
			assertNotNull(selection);
			assertEquals(GeoCeDGReferencePlacement.Route.R2_H1, selection.getRoute());
			var address = selection.getChoices().get(0).getAddress();
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, -.7, 2.2);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, -.7, 2.2);
			assertSame(selection, tool(app).pendingSelection());
			assertEquals(before, app.getXML());
			Cu7M08SDesktopEventsTest.pointer(app, 2, 3);
			assertSame(selection, tool(app).pendingSelection());
			assertEquals(before, app.getXML());
			Cu7M08SDesktopEventsTest.confirm(app);
			var p = Cu7M08SDesktopEventsTest.point(app);
			assertSame(source, parent(p).getSource());
			assertEquals(address, parent(p).getCurrentSemanticAddress());
			assertNull(tool(app).pendingSelection());
			assertFalse(tool(app).isWorldPlacementActive());
			assertEquals(EuclidianConstants.MODE_POINT, app.getMode());
		});
	}

	@Test void changingToolOrCancellingClosesTheWorldSelection() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			Cu7M08SDesktopEventsTest.general(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			String before = app.getXML();
			Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
			var first = tool(app).pendingSelection();
			assertNotNull(first);
			G9U1SemanticPointInteractionTest.controller(app).cancelReferencePoint();
			assertNull(tool(app).pendingSelection());
			assertEquals(M08SnapshotPlacement2D.Validity.CONSUMED, first.getValidity());
			assertEquals(before, app.getXML());
			Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
			var second = tool(app).pendingSelection();
			assertNotNull(second);
			app.setMode(EuclidianConstants.MODE_MOVE);
			assertNull(tool(app).pendingSelection());
			assertFalse(tool(app).isWorldPlacementActive());
			assertEquals(M08SnapshotPlacement2D.Validity.CONSUMED, second.getValidity());
			assertEquals(before, app.getXML());
		});
	}

	@Test void changingTheSourceMakesAWorldOfferStale() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			Cu7M08SDesktopEventsTest.general(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
			var selection = tool(app).pendingSelection();
			assertNotNull(selection);
			var center = (GeoPoint) app.getKernel().lookupLabel("I");
			center.setCoords(center.inhomX + .1, center.inhomY, 1); center.updateCascade();
			String before = app.getXML();
			assertNull(tool(app).commit(selection.getChoices().get(0)));
			assertEquals("STALE_SELECTION", tool(app).lastFailure());
			assertEquals(before, app.getXML());
			tool(app).reset();
		});
	}

	@Test void directR6SplineClickAndDragKeepTheirExistingGesture() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = G9U1SemanticPointInteractionTest.spline(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, -1, 0);
			var p = Cu7M08SDesktopEventsTest.point(app);
			assertSame(source, parent(p).getSource());
			assertNull(tool(app).pendingSelection());
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, 1, 0);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, 1, 0);
			assertEquals(1, p.inhomX, 1e-8);
			assertNull(tool(app).pendingSelection());
		});
	}

	@Test void worldCreationThroughEventsHasOneUndoAndStableRedo() throws Exception {
		AtomicReference<AppGeoCeDG> host = new AtomicReference<>();
		AtomicReference<String> label = new AtomicReference<>();
		AtomicReference<Object> id = new AtomicReference<>();
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app(); host.set(app);
			Cu7M08SDesktopEventsTest.general(app); app.getKernel().initUndoInfo();
		});
		var app = host.get();
		var undo = app.getKernel().getConstruction().getUndoManager();
		Cu7M08SDesktopEventsTest.await(() -> !undo.undoPossible());
		Cu7M08SDesktopEventsTest.edt(() -> {
			app.setMode(EuclidianConstants.MODE_POINT);
			Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
			assertNotNull(tool(app).pendingSelection()); Cu7M08SDesktopEventsTest.confirm(app);
			var p = Cu7M08SDesktopEventsTest.point(app);
			label.set(p.getLabelSimple());
			id.set(app.getKernel().getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(p));
		});
		Cu7M08SDesktopEventsTest.await(undo::undoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> app.getKernel().undo());
		Cu7M08SDesktopEventsTest.await(undo::redoPossible);
		Cu7M08SDesktopEventsTest.edt(() -> {
			assertNull(app.getKernel().lookupLabel(label.get()));
			assertFalse(undo.undoPossible()); app.getKernel().redo();
		});
		Cu7M08SDesktopEventsTest.await(() -> app.getKernel().lookupLabel(label.get()) != null);
		Cu7M08SDesktopEventsTest.edt(() -> assertEquals(id.get(),
				app.getKernel().getConstruction().getSpatialIdentityRegistry()
						.getPersistentGeoId(app.getKernel().lookupLabel(label.get()))));
	}

	@Test void directR6CreationClosesAnEditedWorldOfferWithoutRevivingItsButton() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var spline = G9U1SemanticPointInteractionTest.spline(app);
			Cu7M08SDesktopEventsTest.general(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
			var old = tool(app).pendingSelection(); assertNotNull(old);
			var oldConfirm = Cu7M08SDesktopEventsTest.button(tool(app).contextMenu(), "Confirm");
			assertNotNull(oldConfirm);
			Cu7M08SDesktopEventsTest.radius(app).setText("0.2");
			Cu7M08SDesktopEventsTest.radius(app).postActionEvent();
			assertEquals(M08SnapshotPlacement2D.Validity.CONSUMED, old.getValidity());
			assertNull(tool(app).pendingSelection());
			assertTrue(tool(app).isWorldPlacementActive());
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_PRESSED, -1, 0);
			var p = Cu7M08SDesktopEventsTest.point(app);
			assertSame(spline, parent(p).getSource());
			assertFalse(tool(app).isWorldPlacementActive());
			assertNotNull(Cu7M08SDesktopEventsTest.panel(app), "Point options remain available");
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_DRAGGED, 1, 0);
			Cu7M08SDesktopEventsTest.mouse(app, MouseEvent.MOUSE_RELEASED, 1, 0);
			assertEquals(1, p.inhomX, 1e-8);
			String before = app.getXML(); oldConfirm.doClick();
			assertEquals(before, app.getXML());
		});
	}

	@Test void invalidWorldPolicyBlocksANewPressBeforeAnyInheritedCreation() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			Cu7M08SDesktopEventsTest.general(app);
			app.setMode(EuclidianConstants.MODE_POINT);
			Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
			var old = tool(app).pendingSelection(); assertNotNull(old);
			var oldConfirm = Cu7M08SDesktopEventsTest.button(tool(app).contextMenu(), "Confirm");
			Cu7M08SDesktopEventsTest.radius(app).setText("NaN");
			String before = app.getXML();
			Cu7M08SDesktopEventsTest.pointer(app, 3, -2);
			assertEquals(before, app.getXML());
			assertTrue(tool(app).isWorldPlacementActive());
			assertNull(tool(app).pendingSelection());
			assertEquals(M08SnapshotPlacement2D.Validity.CONSUMED, old.getValidity());
			Cu7M08SDesktopEventsTest.radius(app).setText("0.2");
			Cu7M08SDesktopEventsTest.pointer(app, 3, -2);
			assertFalse(tool(app).isWorldPlacementActive());
			assertNotNull(Cu7M08SDesktopEventsTest.panel(app), "Point options remain available");
			assertNotEquals(before, app.getXML(), "Ordinary Point mode resumes only after valid policy");
			String after = app.getXML(); oldConfirm.doClick();
			assertEquals(after, app.getXML());
		});
	}

	private static JToggleButton origin(AppGeoCeDG app) {
		return Cu7M08SDesktopEventsTest.find(Cu7M08SDesktopEventsTest.panel(app),
				JToggleButton.class, button -> "m08-click-position".equals(button.getName()));
	}

	@Test void explicitClickOriginUsesTheWorldToolAndReactivatesAfterConfirmation() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = Cu7M08SDesktopEventsTest.general(app);
			Cu7M08SDesktopEventsTest.start(app);
			assertNotNull(origin(app));
			assertFalse(origin(app).isSelected(), "Existing L then Q remains the default");
			origin(app).doClick();
			String before = app.getXML();
			Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
			var selection = tool(app).pendingSelection(); assertNotNull(selection);
			assertEquals(GeoCeDGReferencePlacement.Route.R2_H1, selection.getRoute());
			assertEquals(before, app.getXML());
			var address = selection.getChoices().get(0).getAddress();
			Cu7M08SDesktopEventsTest.confirm(app);
			var p = Cu7M08SDesktopEventsTest.point(app);
			assertSame(source, parent(p).getSource());
			assertEquals(address, parent(p).getCurrentSemanticAddress());
			assertEquals(EuclidianConstants.MODE_LOCUS_V2_REFERENCE_POINT, app.getMode());
			assertNotNull(Cu7M08SDesktopEventsTest.panel(app));
			assertNull(tool(app).pendingSelection());
			assertTrue(origin(app).isSelected(), "Origin choice is an ephemeral session preference");
			origin(app).doClick();
			assertFalse(origin(app).isSelected());
			tool(app).reset();
		});
	}

	@Test void clickOriginAndQHaveTheSameWorldPolicyAtDifferentZoomsAndRadii() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = Cu7M08SDesktopEventsTest.general(app);
			Cu7M08SDesktopEventsTest.start(app); origin(app).doClick();
			int index = 0, offered = 0, rejected = 0;
			for (double zoom : new double[] {50, 150}) {
				app.getEuclidianView1().setCoordSystem(400, 500, zoom, zoom);
				for (double radius : new double[] {.2, .0001}) {
					var view = app.getEuclidianView1();
					double x = view.toRealWorldCoordX(view.toScreenCoordX(.807));
					double y = view.toRealWorldCoordY(view.toScreenCoordY(2.407));
					var q = (GeoPoint) G9U1SemanticPointInteractionTest.eval(app,
							"ParityQ" + (++index) + "=(" + x + "," + y + ")");
					q.setEuclidianVisible(false); q.updateRepaint();
					var policy = LocusPointInteractionPolicy2D.initial(radius);
					tool(app).setPolicy(policy);
					try (var expected = new GeoCeDGReferencePlacement(source, q, policy)) {
						String before = app.getXML();
						Cu7M08SDesktopEventsTest.pointer(app, .807, 2.407);
						assertTrue(view.getHits().contains(source), "The canvas must actually select L");
						assertEquals(before, app.getXML());
						if (expected.getRoute() == GeoCeDGReferencePlacement.Route.REJECTED) {
							rejected++;
							assertNull(tool(app).pendingSelection());
							assertEquals(expected.getFailure(), tool(app).lastFailure());
						} else {
							offered++;
							var actual = tool(app).pendingSelection(); assertNotNull(actual);
							assertEquals(expected.getRoute(), actual.getRoute());
							assertEquals(expected.getChoices().stream().map(GeoCeDGReferencePlacement.Choice::getAddress).toList(),
									actual.getChoices().stream().map(GeoCeDGReferencePlacement.Choice::getAddress).toList());
						}
					}
					G9U1SemanticPointInteractionTest.controller(app).cancelReferencePoint();
					assertTrue(origin(app).isSelected());
				}
			}
			assertTrue(offered > 0); assertTrue(rejected > 0);
			tool(app).reset();
		});
	}

	@Test void sourceChooserKeepsTheWorldRequestCapturedBeforeZoomAndPan() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			var source = Cu7M08SDesktopEventsTest.general(app);
			var overlap = (GeoLocusV2) G9U1SemanticPointInteractionTest.eval(app,
					"h2=Translate(h,(0,0))");
			overlap.setEuclidianVisible(true); overlap.updateRepaint();
			Cu7M08SDesktopEventsTest.start(app); origin(app).doClick();
			var view = app.getEuclidianView1();
			int screenX = view.toScreenCoordX(.8), screenY = view.toScreenCoordY(2.4);
			double worldX = view.toRealWorldCoordX(screenX);
			double worldY = view.toRealWorldCoordY(screenY);
			try (var expected = new GeoCeDGReferencePlacement(source, worldX, worldY,
					LocusPointInteractionPolicy2D.initial(.1))) {
				assertEquals(GeoCeDGReferencePlacement.Route.R2_H1, expected.getRoute());
				Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
				assertTrue(view.getHits().contains(source));
				assertTrue(view.getHits().contains(overlap));
				assertNull(tool(app).pendingSelection(), "The construction source must be chosen first");
				var sourceList = Cu7M08SDesktopEventsTest.find(tool(app).contextMenu(),
						JList.class, list -> true);
				assertNotNull(sourceList); assertEquals(2, sourceList.getModel().getSize());
				int selected = -1;
				for (int i = 0; i < sourceList.getModel().getSize(); i++) {
					if (source.toString().equals(sourceList.getModel().getElementAt(i))) { selected = i; }
				}
				assertTrue(selected >= 0);
				view.setCoordSystem(500, 350, 250, 250);
				assertNotEquals(worldX, view.toRealWorldCoordX(screenX));
				assertNotEquals(worldY, view.toRealWorldCoordY(screenY));
				String before = app.getXML();
				sourceList.setSelectedIndex(selected); Cu7M08SDesktopEventsTest.confirm(app);
				var actual = tool(app).pendingSelection(); assertNotNull(actual);
				assertEquals(expected.getRoute(), actual.getRoute());
				assertEquals(expected.getChoices().stream().map(GeoCeDGReferencePlacement.Choice::getAddress).toList(),
						actual.getChoices().stream().map(GeoCeDGReferencePlacement.Choice::getAddress).toList());
				assertEquals(before, app.getXML(), "Choosing L only queries the captured world request");
			}
			tool(app).reset();
		});
	}

	@Test void exactNativeModelCreatesByCanvasAndPersistsForwardState(@TempDir Path directory)
			throws Exception {
		Path original = directory.resolve("TestM08-S.cedg");
		try (var stream = getClass().getResourceAsStream("cu7-general-creation/TestM08-S.cedg")) {
			assertNotNull(stream, "Author fixture must be bundled, never skipped");
			Files.copy(stream, original);
		}
		assertEquals(NATIVE_SHA, hash(original));
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			assertTrue(app.loadFile(original.toFile(), false));
			var h = (GeoLocusV2) app.getKernel().lookupLabel("h"); assertNotNull(h);
			var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
			var prior = registry.getRecords().stream().filter(GeoIdentityRecord.class::isInstance)
					.map(GeoIdentityRecord.class::cast).map(GeoIdentityRecord::getId)
					.collect(java.util.stream.Collectors.toSet());
			app.getEuclidianView1().setCoordSystem(220, 420, 100, 100);
			app.setMode(EuclidianConstants.MODE_POINT);
			String before = app.getXML();
			Cu7M08SDesktopEventsTest.pointer(app, .8, 2.4);
			var selection = tool(app).pendingSelection(); assertNotNull(selection);
			assertEquals(GeoCeDGReferencePlacement.Route.R2_H1, selection.getRoute());
			assertEquals(before, app.getXML());
			var address = selection.getChoices().get(0).getAddress();
			Cu7M08SDesktopEventsTest.confirm(app);
			var added = registry.getRecords().stream().filter(GeoIdentityRecord.class::isInstance)
					.map(GeoIdentityRecord.class::cast).filter(r -> !prior.contains(r.getId()))
					.map(r -> registry.getGeo(r.getId())).filter(GeoPoint.class::isInstance)
					.map(GeoPoint.class::cast).filter(GeoCeDGPointInteraction::owns).toList();
			assertEquals(1, added.size());
			var p = added.get(0); p.setLabel("NativeCanvasP");
			assertSame(h, parent(p).getSource()); assertEquals(address, parent(p).getCurrentSemanticAddress());
			assertNull(app.getKernel().lookupLabel("ReferenceR1"));
			var id = registry.getPersistentGeoId(p); assertNotNull(id);
			var saved = directory.resolve("canvas-created.cedg");
			assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(saved.toFile()));
			var reopened = G9U1SemanticPointInteractionTest.app();
			assertTrue(reopened.loadFile(saved.toFile(), false));
			var restored = (GeoPoint) reopened.getKernel().lookupLabel("NativeCanvasP");
			assertEquals(id, reopened.getKernel().getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(restored));
			assertEquals(address, parent(restored).getCurrentSemanticAddress());
			String clipboard = InternalClipboard.getTextToSave(app, List.of(p), text -> text);
			G9U1SemanticPointInteractionTest.paste(app, clipboard);
			var copy = registry.getRecords().stream().filter(GeoIdentityRecord.class::isInstance)
					.map(GeoIdentityRecord.class::cast).filter(r -> id.equals(r.getCopySourceId()))
					.findFirst().orElseThrow();
			assertNotEquals(id, copy.getId());
			assertEquals(NATIVE_SHA, hash(original));
		});
	}

	private static String hash(Path path) throws Exception {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
				.digest(Files.readAllBytes(path)));
	}
}
