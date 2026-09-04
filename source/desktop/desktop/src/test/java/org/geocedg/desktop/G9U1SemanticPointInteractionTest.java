/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionCandidate2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionStatus2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.geogebra.desktop.util.LoggerD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/** Frontend R6 consumers; numerical correctness remains the R6 kernel authority. */
class G9U1SemanticPointInteractionTest {
	@BeforeAll
	static void initializeDesktop() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void straightLocusCreatesOwnedPointWithoutGenericPath() {
		AppGeoCeDG app = app();
		GeoLocusV2 source = line(app);
		GeoPoint point = new GeoCeDGPointInteraction(app).create(source, 0.25, 0.01,
				0.1, result -> {
					throw new AssertionError("Unique query opened chooser");
				});
		assertNotNull(point);
		assertTrue(GeoCeDGPointInteraction.owns(point));
		assertSame(source, parent(point).getSource());
		assertEquals(0.25, point.getInhomX(), 1E-8);
		assertNull(point.getPath());
	}

	@Test
	void splineCreationAndDragPreservePointAndSourceIdentity() {
		AppGeoCeDG app = app();
		GeoLocusV2 source = spline(app);
		GeoCeDGPointInteraction interaction = new GeoCeDGPointInteraction(app);
		GeoPoint point = interaction.create(source, -1, 0.01, 0.1, result -> null);
		Object id = app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point);
		assertTrue(interaction.move(point, 1, 0.01, 0.1));
		assertEquals(1, point.getInhomX(), 1E-8);
		assertEquals(id, app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point));
		assertSame(source, parent(point).getSource());
		assertEquals(0.75, parent(point).getCurrentSemanticAddress()
				.getCanonicalParameter(), 1E-8);
	}

	@Test
	void selfIntersectionCancelCreatesNothing() {
		AppGeoCeDG app = app();
		GeoLocusV2 source = selfIntersection(app);
		GeoCeDGPointInteraction interaction = new GeoCeDGPointInteraction(app);
		int count = size(app);
		assertNull(interaction.create(source, 0, 1, 0.01, result -> {
			assertEquals(LocusPointInteractionStatus2D.MULTIPLE_SEMANTIC_PREIMAGES,
					result.getStatus());
			return null;
		}));
		assertEquals(count, size(app));
	}

	@Test
	void selfIntersectionChooserPassesExactCandidateNotListIdentity() {
		AppGeoCeDG app = app();
		GeoLocusV2 source = selfIntersection(app);
		AtomicReference<LocusPointInteractionCandidate2D> chosen = new AtomicReference<>();
		GeoPoint point = new GeoCeDGPointInteraction(app).create(source, 0, 1,
				0.01, result -> {
					chosen.set(result.getCandidates().get(result.getCandidates().size() - 1));
					return chosen.get();
				});
		assertNotNull(point);
		assertEquals(chosen.get().getAddress(), parent(point).getCurrentSemanticAddress());
	}

	@Test
	void unresolvedDragNeverRetargets() {
		AppGeoCeDG app = app();
		GeoCeDGPointInteraction interaction = new GeoCeDGPointInteraction(app);
		GeoPoint point = interaction.create(line(app), 0, 0, 0.1, result -> null);
		final Object address = parent(point).getCurrentSemanticAddress();
		assertFalse(interaction.move(point, 100, 100, 0.01));
		assertEquals(address, parent(point).getCurrentSemanticAddress());
		assertEquals(0, point.getInhomX(), 1E-10);
	}

	@Test
	void negativeDilationAndCollapseRecoveryUseSameKernelPoint() {
		AppGeoCeDG app = app();
		spline(app);
		eval(app, "k=-2");
		GeoLocusV2 transformed = (GeoLocusV2) eval(app, "T=Dilate(S,k,(0,0))");
		GeoCeDGPointInteraction interaction = new GeoCeDGPointInteraction(app);
		GeoPoint point = interaction.create(transformed, 2, 0, 0.1, result -> null);
		final Object address = parent(point).getCurrentSemanticAddress();
		GeoNumeric factor = (GeoNumeric) app.getKernel().lookupLabel("k");
		factor.setValue(0);
		factor.updateCascade();
		assertTrue(point.isDefined());
		assertEquals(0, point.getInhomX(), 1E-10);
		int count = size(app);
		assertNull(interaction.create(transformed, 0, 0, 0.1, result -> null));
		assertEquals(LocusPointInteractionStatus2D.DEGENERATE_SOURCE_IMAGE,
				interaction.getLastResult().getStatus());
		assertEquals(count, size(app));
		factor.setValue(-2);
		factor.updateCascade();
		assertEquals(address, parent(point).getCurrentSemanticAddress());
		assertEquals(2, point.getInhomX(), 1E-8);
	}

	@Test
	void closedCurveInteriorIsNotAStrokeHit() {
		AppGeoCeDG app = app();
		eval(app, "u=0");
		eval(app, "Q=(cos(u),sin(u))");
		eval(app, "D={true,{0,2*pi,true,false}}");
		GeoLocusV2 circle = (GeoLocusV2) eval(app, "L=LocusV2(Q,u,D)");
		circle.setEuclidianVisible(true);
		EuclidianView view = app.getEuclidianView1();
		DrawLocusV2 drawable = new DrawLocusV2(view, circle);
		assertFalse(drawable.hit(view.toScreenCoordX(0), view.toScreenCoordY(0), 4));
		assertTrue(drawable.hit(view.toScreenCoordX(1), view.toScreenCoordY(0), 4));
	}

	@Test
	void realPointMousePathCreatesAndMovesWithoutReplacement() {
		AppGeoCeDG app = app();
		GeoLocusV2 source = line(app);
		source.setEuclidianVisible(true);
		source.updateRepaint();
		GeoCeDGEuclidianController controller = controller(app);
		app.setMode(EuclidianConstants.MODE_POINT);
		AbstractEvent click = event(app, 0.4, 0);
		controller.wrapMousePressed(click);
		controller.wrapMouseReleased(click);
		GeoPoint point = app.getKernel().getConstruction().getGeoSetConstructionOrder()
				.stream().filter(geo -> geo instanceof GeoPoint
						&& GeoCeDGPointInteraction.owns((GeoPoint) geo))
				.map(geo -> (GeoPoint) geo).findFirst().orElseThrow();
		String label = point.getLabelSimple();
		app.setMode(EuclidianConstants.MODE_MOVE);
		controller.wrapMousePressed(event(app, 0.4, 0));
		controller.wrapMouseDragged(event(app, 1.2, 0), true);
		controller.wrapMouseReleased(event(app, 1.2, 0));
		assertSame(point, app.getKernel().lookupLabel(label));
		assertSame(source, parent(point).getSource());
		assertEquals(1.2, point.getInhomX(), 0.04);
	}

	@Test
	void viewportChangeDoesNotChangeStoredSemanticIdentity() {
		AppGeoCeDG app = app();
		GeoPoint point = new GeoCeDGPointInteraction(app).create(spline(app), 1, 0,
				0.1, result -> null);
		Object address = parent(point).getCurrentSemanticAddress();
		app.getEuclidianView1().setCoordSystem(300, 200, 150, 100);
		assertEquals(address, parent(point).getCurrentSemanticAddress());
	}

	@Test
	void nativeReopenReusesOwnedPointAndMoveAdapter(@TempDir Path directory) {
		AppGeoCeDG app = app();
		GeoPoint point = new GeoCeDGPointInteraction(app).create(spline(app), 1, 0,
				0.1, result -> null);
		String label = point.getLabelSimple();
		Object address = parent(point).getCurrentSemanticAddress();
		Path file = directory.resolve("workspace-point.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = app();
		assertTrue(reopened.loadFile(file.toFile(), false));
		GeoPoint restored = (GeoPoint) reopened.getKernel().lookupLabel(label);
		assertTrue(GeoCeDGPointInteraction.owns(restored));
		assertEquals(address, parent(restored).getCurrentSemanticAddress());
		assertTrue(new GeoCeDGPointInteraction(reopened).move(restored, -1, 0, 0.1));
	}

	@Test
	void periodicSplineCrossesSeamAndReturnsSamePoint() {
		AppGeoCeDG app = app();
		eval(app, "A=(1,0)");
		eval(app, "B=(0,1)");
		eval(app, "C=(-1,0)");
		eval(app, "D=(0,-1)");
		GeoLocusV2 source = (GeoLocusV2) eval(app,
				"S=SplineV2({A,B,C,D,A},3)");
		GeoCeDGPointInteraction interaction = new GeoCeDGPointInteraction(app);
		LocusPoint2D start = evaluate(source, 0.98);
		GeoPoint point = interaction.create(source, start.getX(), start.getY(),
				0.001, result -> null);
		assertNotNull(point);
		LocusPoint2D end = evaluate(source, 0.02);
		assertTrue(interaction.move(point, end.getX(), end.getY(), 0.001));
		assertEquals(0.02, parent(point).getCurrentSemanticAddress()
				.getCanonicalParameter(), 1E-8);
		assertEquals(1, parent(point).getCurrentSemanticAddress().getPeriodicLift());
		assertTrue(interaction.move(point, start.getX(), start.getY(), 0.001));
		assertEquals(0.98, parent(point).getCurrentSemanticAddress()
				.getCanonicalParameter(), 1E-8);
	}

	@Test
	void zoomWindowIsPresentationOnlyAndCanBeCancelledByToolChange() {
		AppGeoCeDG app = app();
		line(app);
		int count = size(app);
		GeoCeDGEuclidianController controller = controller(app);
		controller.activateZoomWindow();
		assertTrue(controller.isZoomWindowActive());
		app.setMode(EuclidianConstants.MODE_POINT);
		assertFalse(controller.isZoomWindowActive());
		assertEquals(count, size(app));
	}

	@Test
	void everyInvertibleTransformUsesItsOwnSourceBinding() {
		AppGeoCeDG app = app();
		spline(app);
		String[] commands = {"Translate(S,Vector((0,0),(3,2)))",
				"Rotate(S,pi/2,(0,0))", "Reflect(S,x=0)",
				"Dilate(S,2,(0,0))", "Dilate(S,-2,(0,0))"};
		for (int index = 0; index < commands.length; index++) {
			GeoLocusV2 source = (GeoLocusV2) eval(app, "T" + index + "=" + commands[index]);
			LocusPoint2D position = evaluate(source, 0.3);
			GeoCeDGPointInteraction interaction = new GeoCeDGPointInteraction(app);
			GeoPoint point = interaction.create(source, position.getX(), position.getY(),
					0.01, result -> null);
			assertNotNull(point, commands[index]);
			assertSame(source, parent(point).getSource());
			LocusPoint2D destination = evaluate(source, 0.6);
			assertTrue(interaction.move(point, destination.getX(), destination.getY(), 0.01));
			assertEquals(source.getPersistentLocusId(), parent(point)
					.getCurrentSemanticAddress().getSourceLocusId());
		}
	}

	@Test
	void ownedDragUndoRedoRestoresExactSemanticAddress() throws Exception {
		AppGeoCeDG app = app();
		GeoPoint point = new GeoCeDGPointInteraction(app).create(spline(app), -1, 0,
				0.1, result -> null);
		final String label = point.getLabelSimple();
		Object initial = parent(point).getCurrentSemanticAddress();
		app.getKernel().initUndoInfo();
		var undo = app.getKernel().getConstruction().getUndoManager();
		await(() -> !undo.undoPossible());
		app.setMode(EuclidianConstants.MODE_MOVE);
		controller(app).wrapMousePressed(event(app, -1, 0));
		controller(app).wrapMouseDragged(event(app, 1, 0), true);
		controller(app).wrapMouseReleased(event(app, 1, 0));
		Object moved = parent(point).getCurrentSemanticAddress();
		assertFalse(initial.equals(moved));
		await(undo::undoPossible);
		app.getKernel().undo();
		await(undo::redoPossible);
		assertEquals(initial, parent((GeoPoint) app.getKernel().lookupLabel(label))
				.getCurrentSemanticAddress());
		app.getKernel().redo();
		await(() -> moved.equals(parent((GeoPoint) app.getKernel().lookupLabel(label))
				.getCurrentSemanticAddress()));
	}

	@Test
	void clipboardRemapsOwnedPointAndFrontendMovesOnlyCopiedSource() {
		AppGeoCeDG app = app();
		GeoPoint original = new GeoCeDGPointInteraction(app).create(spline(app), 1, 0,
				0.1, result -> null);
		var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
		var originalId = registry.getPersistentGeoId(original);
		String clipboard = InternalClipboard.getTextToSave(app, List.of(original), text -> text);
		paste(app, clipboard);
		GeoIdentityRecord copiedRecord = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(record -> originalId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoPoint copy = (GeoPoint) registry.getGeo(copiedRecord.getId());
		assertTrue(GeoCeDGPointInteraction.owns(copy));
		assertFalse(originalId.equals(copiedRecord.getId()));
		assertFalse(parent(copy).getSource().getPersistentLocusId()
				.equals(parent(original).getSource().getPersistentLocusId()));
		assertTrue(new GeoCeDGPointInteraction(app).move(copy, -1, 0, 0.1));
		assertEquals(1, original.getInhomX(), 1E-8);
		assertEquals(-1, copy.getInhomX(), 1E-8);
	}

	@Test
	void overlappingOwnedPointsRequireExplicitObjectSelection() {
		AppGeoCeDG app = app();
		spline(app);
		eval(app, "k=1");
		GeoLocusV2 source = (GeoLocusV2) eval(app, "T=Dilate(S,k,(0,0))");
		GeoCeDGPointInteraction interaction = new GeoCeDGPointInteraction(app);
		GeoPoint first = interaction.create(source, -1, 0, 0.1, result -> null);
		GeoPoint second = interaction.create(source, 1, 0, 0.1, result -> null);
		GeoNumeric factor = (GeoNumeric) app.getKernel().lookupLabel("k");
		factor.setValue(0);
		factor.updateCascade();
		app.setMode(EuclidianConstants.MODE_MOVE);
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showInputDialog(any(Component.class), any(),
					anyString(), eq(JOptionPane.QUESTION_MESSAGE), isNull(), any(), isNull()))
					.thenAnswer(call -> Arrays.stream((Object[]) call.getArgument(5))
							.filter(choice -> choice.toString()
									.startsWith(second.getLabelSimple() + " "))
							.findFirst().orElseThrow());
			controller(app).wrapMousePressed(event(app, 0, 0));
			controller(app).wrapMouseReleased(event(app, 0, 0));
		}
		assertEquals(List.of(second), app.getSelectionManager().getSelectedGeos());
		assertTrue(first.isDefined());
		assertTrue(second.isDefined());
	}

	@Test
	void collapsedStrokeClickDoesNotFallBackToAnOrdinaryFreePoint() {
		AppGeoCeDG app = app();
		line(app);
		GeoLocusV2 collapsed = (GeoLocusV2) eval(app,
				"T=Dilate(L,0,(1.2,1.3))");
		showOnly(app, collapsed);
		app.setMode(EuclidianConstants.MODE_POINT);
		int count = size(app);
		AbstractEvent click = event(app, 1.2, 1.3);
		controller(app).wrapMousePressed(click);
		controller(app).wrapMouseReleased(click);
		assertEquals(count, size(app), "A collapsed semantic stroke must not create a free point");
	}

	@Test
	void transformedOrdinaryLocusMouseCreationAndDragUseTransformedSource() {
		AppGeoCeDG app = app();
		line(app);
		GeoLocusV2 transformed = (GeoLocusV2) eval(app, "T=Dilate(L,-2,(1,0.5))");
		showOnly(app, transformed);
		app.setMode(EuclidianConstants.MODE_POINT);
		// (1, 0.5) - 2 * ((u, 0) - (1, 0.5)) = (3 - 2u, 1.5).
		AbstractEvent click = event(app, 2.2, 1.5);
		controller(app).wrapMousePressed(click);
		controller(app).wrapMouseReleased(click);
		GeoPoint point = app.getKernel().getConstruction().getGeoSetConstructionOrder()
				.stream().filter(geo -> geo instanceof GeoPoint
						&& GeoCeDGPointInteraction.owns((GeoPoint) geo))
				.map(geo -> (GeoPoint) geo).findFirst().orElseThrow();
		assertSame(transformed, parent(point).getSource());
		Object id = app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point);
		app.setMode(EuclidianConstants.MODE_MOVE);
		controller(app).wrapMousePressed(event(app, 2.2, 1.5));
		controller(app).wrapMouseDragged(event(app, 1, 1.5), true);
		controller(app).wrapMouseReleased(event(app, 1, 1.5));
		assertSame(transformed, parent(point).getSource());
		assertEquals(id, app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point));
		assertEquals(1, point.getInhomX(), 0.04);
		assertEquals(1.5, point.getInhomY(), 0.04);
	}

	@Test
	void toolChangeEndsOwnedGestureWithoutRetargetingOrExtraUndo() throws Exception {
		for (boolean moveBeforeChange : new boolean[] {false, true}) {
			AppGeoCeDG app = app();
			GeoPoint point = new GeoCeDGPointInteraction(app).create(spline(app), -1, 0,
					0.1, result -> null);
			final String label = point.getLabelSimple();
			final Object initial = parent(point).getCurrentSemanticAddress();
			app.getKernel().initUndoInfo();
			var undo = app.getKernel().getConstruction().getUndoManager();
			await(() -> undo.getHistorySize() == 0);
			app.setMode(EuclidianConstants.MODE_MOVE);
			controller(app).wrapMousePressed(event(app, -1, 0));
			if (moveBeforeChange) {
				controller(app).wrapMouseDragged(event(app, 1, 0), true);
			}
			Object beforeToolChange = parent(point).getCurrentSemanticAddress();
			int count = size(app);
			app.setMode(EuclidianConstants.MODE_JOIN);
			controller(app).wrapMouseDragged(event(app, 0.5, 0), true);
			controller(app).wrapMouseReleased(event(app, 0.5, 0));
			assertEquals(beforeToolChange, parent(point).getCurrentSemanticAddress());
			assertEquals(count, size(app));
			await(() -> undo.getHistorySize() == (moveBeforeChange ? 1 : 0));
			if (moveBeforeChange) {
				app.getKernel().undo();
				await(undo::redoPossible);
				assertEquals(initial, parent((GeoPoint) app.getKernel().lookupLabel(label))
						.getCurrentSemanticAddress());
			}
		}
	}

	private static void showOnly(AppGeoCeDG app, GeoLocusV2 source) {
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			geo.setEuclidianVisible(geo == source);
			geo.updateRepaint();
		}
		app.getEuclidianView1().updateAllDrawables(true);
	}

	static AppGeoCeDG app() {
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=true"}), new JPanel());
		app.setErrorDialogsActive(false);
		// Embedded test panels have no window manager to establish their viewport.
		// A real positive-sized canvas is required for clipping and stroke/point hits.
		app.getEuclidianView1().setSize(new Dimension(800, 600));
		app.getEuclidianView1().updateSize();
		app.getEuclidianView1().setCoordSystem(400, 300, 100, 100);
		return app;
	}

	static GeoElement eval(AppGeoCeDG app, String command) {
		var result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(result, command);
		assertEquals(1, result.length, command);
		return result[0].toGeoElement();
	}

	static GeoLocusV2 line(AppGeoCeDG app) {
		eval(app, "u=0");
		eval(app, "Q=(u,0)");
		eval(app, "D={false,{-2,2,true,true}}");
		return (GeoLocusV2) eval(app, "L=LocusV2(Q,u,D)");
	}

	static GeoLocusV2 spline(AppGeoCeDG app) {
		return (GeoLocusV2) eval(app,
				"S=SplineV2({(-2,0),(-2/3,0),(2/3,0),(2,0)},3)");
	}

	private static GeoLocusV2 selfIntersection(AppGeoCeDG app) {
		return (GeoLocusV2) eval(app,
				"S=SplineV2({(-1,0),(0,1),(1,0),(0,1),(-1,0)},3)");
	}

	static int size(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().size();
	}

	static GeoCeDGEuclidianController controller(AppGeoCeDG app) {
		return (GeoCeDGEuclidianController) app.getEuclidianView1().getEuclidianController();
	}

	private static AlgoSemanticLocusPoint2D parent(GeoPoint point) {
		return (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
	}

	private static LocusPoint2D evaluate(GeoLocusV2 source, double parameter) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.reference()) {
			return source.evaluate("spline-v2/main", parameter, session).getPoint();
		}
	}

	static AbstractEvent event(AppGeoCeDG app, double x, double y) {
		int screenX = app.getEuclidianView1().toScreenCoordX(x);
		int screenY = app.getEuclidianView1().toScreenCoordY(y);
		return MouseEventD.wrapEvent(new MouseEvent(app.getEuclidianView1().getJPanel(),
				MouseEvent.MOUSE_PRESSED, 1, 0, screenX, screenY, 1, false, MouseEvent.BUTTON1));
	}

	static void paste(AppGeoCeDG app, String clipboard) {
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(app, new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" "))), clipboard.substring(separator));
	}

	private static void await(BooleanSupplier condition) throws Exception {
		long deadline = System.nanoTime() + 5_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean());
	}
}
