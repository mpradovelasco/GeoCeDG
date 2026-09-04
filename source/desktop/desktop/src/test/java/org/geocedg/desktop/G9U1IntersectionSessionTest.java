/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1SemanticPointInteractionTest.app;
import static org.geocedg.desktop.G9U1SemanticPointInteractionTest.controller;
import static org.geocedg.desktop.G9U1SemanticPointInteractionTest.eval;
import static org.geocedg.desktop.G9U1SemanticPointInteractionTest.event;
import static org.geocedg.desktop.G9U1SemanticPointInteractionTest.line;
import static org.geocedg.desktop.G9U1SemanticPointInteractionTest.paste;
import static org.geocedg.desktop.G9U1SemanticPointInteractionTest.size;
import static org.geocedg.desktop.G9U1SemanticPointInteractionTest.spline;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.Hits;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

/** G9U1 consumes rich-result authority; tests do not implement or repeat solvers. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1IntersectionSessionTest {
	@BeforeAll
	static void initializeDesktop() {
		G9U1SemanticPointInteractionTest.initializeDesktop();
	}

	@Test
	void materializeAllUsesExactTokensAndDoesNotDuplicateClaims() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = circleSession(app);
		List<String> tokens = session.eligibleTokens();
		assertEquals(2, tokens.size());
		long work = session.getActive().getIntersectionResult().getWork()
				.getSemanticEvaluations();
		List<GeoPoint> points = session.materializeAll(false);
		assertEquals(2, points.size());
		assertEquals(Set.copyOf(tokens), session.materializedTokens());
		assertTrue(session.materializeAll(false).isEmpty());
		assertEquals(work, session.getActive().getIntersectionResult().getWork()
				.getSemanticEvaluations());
		assertTrue(points.stream().allMatch(GeoPoint::isDefined));
	}

	@Test
	void singleActionNeverCreatesSeveralSelectedRoots() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = circleSession(app);
		session.select(session.eligibleTokens());
		int count = size(app);
		assertTrue(session.materializeSelected(true, false).isEmpty());
		assertEquals(count, size(app));
		assertEquals(2, session.materializeSelected(false, false).size());
	}

	@Test
	void invalidBatchIsRejectedBeforeAnyPointOrHiddenInputIsCreated() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = circleSession(app);
		int count = size(app);
		assertThrows(IllegalArgumentException.class, () -> session.materialize(
				List.of(session.eligibleTokens().get(0), "not-a-kernel-token"), false));
		assertEquals(count, size(app));
		assertTrue(session.materializedTokens().isEmpty());
	}

	@Test
	void certifiedPairMaterializesWithoutGlobalCompleteness() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = pairSession(app);
		assertEquals(Completeness.NOT_ESTABLISHED, session.getActive().getIntersectionResult()
				.getCompletenessEvidence().getCompleteness());
		assertEquals(1, session.eligibleTokens().size());
		GeoPoint point = session.materializeAll(false).get(0);
		assertTrue(point.isDefined());
		assertSame(session.getActive(), ((AlgoLocusIntersectionPointV2)
				point.getParentAlgorithm()).getRichInput());
	}

	@Test
	void operandSwapConsumesEachOwnersExactTokenNotPresentationOrder() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession forward = pairSession(app);
		GeoCeDGIntersectionSession reverse = new GeoCeDGIntersectionSession(app);
		reverse.activate((GeoLocusIntersectionResult) eval(app, "V=Intersect(T,S)"));
		assertEquals(forward.eligibleTokens().size(), reverse.eligibleTokens().size());
		// Separate rich owners intentionally have distinct public handles.
		assertNotEquals(forward.eligibleTokens(), reverse.eligibleTokens());
		GeoPoint first = forward.materializeAll(false).get(0);
		GeoPoint second = reverse.materializeAll(false).get(0);
		assertEquals(first.getInhomX(), second.getInhomX(), 1E-10);
		assertEquals(first.getInhomY(), second.getInhomY(), 1E-10);
	}

	@Test
	void pairRegularMotionRetainsExactPointAndToken() {
		AppGeoCeDG app = app();
		spline(app);
		eval(app, "h=0");
		eval(app, "T=SplineV2({(h,-2),(h,-2/3),(h,2/3),(h,2)},3)");
		GeoCeDGIntersectionSession session = new GeoCeDGIntersectionSession(app);
		session.activate((GeoLocusIntersectionResult) eval(app, "R=Intersect(S,T)"));
		GeoPoint point = session.materializeAll(false).get(0);
		String token = ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
				.getEffectiveRootToken();
		GeoNumeric h = (GeoNumeric) app.getKernel().lookupLabel("h");
		for (double value : new double[] {0.25, -0.25, 0}) {
			h.setValue(value);
			h.updateCascade();
			assertTrue(point.isDefined());
			assertEquals(token, ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
					.getEffectiveRootToken());
			assertEquals(value, point.getInhomX(), 1E-8);
		}
	}

	@Test
	void tangentPairRemainsRichOnlyAndCreatesNoHiddenInput() {
		AppGeoCeDG app = app();
		spline(app);
		eval(app, "T=SplineV2({(-2,4),(-2/3,4/9),(2/3,4/9),(2,4)},3)");
		GeoCeDGIntersectionSession session = new GeoCeDGIntersectionSession(app);
		session.activate((GeoLocusIntersectionResult) eval(app, "R=Intersect(S,T)"));
		int count = size(app);
		assertTrue(session.eligibleTokens().isEmpty());
		assertTrue(session.materializeAll(false).isEmpty());
		assertEquals(count, size(app));
	}

	@Test
	void overlapRemainsRichAndHasNoArbitraryPoint() {
		AppGeoCeDG app = app();
		spline(app);
		GeoCeDGIntersectionSession session = new GeoCeDGIntersectionSession(app);
		session.activate((GeoLocusIntersectionResult) eval(app, "R=Intersect(S,S)"));
		int count = size(app);
		assertTrue(session.materializeAll(false).isEmpty());
		assertEquals(count, size(app));
	}

	@Test
	void markersAndAutoOptInDoNotCreateNodesOnRecompute() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = circleSession(app);
		app.getSelectionManager().addSelectedGeo(session.getActive());
		GeoCeDGEuclidianController controller = controller(app);
		int count = size(app);
		controller.setIntersectionMarkersVisible(true);
		controller.setAutoMaterializeIntersectionSolutions(true);
		assertTrue(controller.isIntersectionMarkersVisible());
		assertTrue(controller.isAutoMaterializeIntersectionSolutions());
		app.getKernel().updateConstruction(false);
		assertEquals(count, size(app));
		assertTrue(controller.getIntersectionSession().materializedTokens().isEmpty());
	}

	@Test
	void persistentInspectorMaterializesSeveralWithoutClosing() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = circleSession(app);
		app.getSelectionManager().addSelectedGeo(session.getActive());
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showOptionDialog(any(Component.class), any(),
					anyString(), eq(JOptionPane.DEFAULT_OPTION), eq(JOptionPane.PLAIN_MESSAGE),
					isNull(), any(Object[].class), any()))
					.thenAnswer(call -> {
						Container panel = call.getArgument(1);
						String language = app.getLocale().getLanguage();
						button(panel, GeoCeDGProfile.getText("intersection.one", language))
								.doClick();
						assertEquals(1, controller(app).getIntersectionSession()
								.materializedTokens().size());
						button(panel, GeoCeDGProfile.getText("intersection.all", language))
								.doClick();
						assertEquals(2, controller(app).getIntersectionSession()
								.materializedTokens().size());
						return JOptionPane.CANCEL_OPTION;
					});
			controller(app).inspectRichResultSelection();
		}
		assertEquals(2, controller(app).getIntersectionSession().materializedTokens().size());
	}

	@Test
	void inspectorCloseAfterSelectedDoesNotCreateTheDefaultToken() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = circleSession(app);
		String expected = session.eligibleTokens().get(1);
		app.getSelectionManager().addSelectedGeo(session.getActive());
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showOptionDialog(any(Component.class), any(),
					anyString(), eq(JOptionPane.DEFAULT_OPTION), eq(JOptionPane.PLAIN_MESSAGE),
					isNull(), any(Object[].class), any())).thenAnswer(call -> {
						Container panel = call.getArgument(1);
						String language = app.getLocale().getLanguage();
						Object[] options = call.getArgument(6);
						assertEquals(GeoCeDGProfile.getText("intersection.one-close", language),
								options[0]);
						assertEquals(GeoCeDGProfile.getText("intersection.close", language),
								options[1]);
						assertEquals(options[1], call.getArgument(7));
						findComponent(panel, JList.class).setSelectedIndex(1);
						button(panel, GeoCeDGProfile.getText("intersection.selected", language))
								.doClick();
						assertEquals(Set.of(expected), session.materializedTokens());
						return 1; // Explicit Close, not a create-one action.
					});
			controller(app).inspectRichResultSelection();
		}
		assertEquals(Set.of(expected), session.materializedTokens());
	}

	@Test
	void inspectorImmediatelyRejectsStaleButtonsWithoutWaitingForTimer() {
		AppGeoCeDG app = app();
		line(app);
		GeoNumeric radius = (GeoNumeric) eval(app, "r=1");
		eval(app, "c=Circle((0,0),r)");
		GeoCeDGIntersectionSession session = new GeoCeDGIntersectionSession(app);
		session.activate((GeoLocusIntersectionResult) eval(app, "R=Intersect(L,c)"));
		app.getSelectionManager().addSelectedGeo(session.getActive());
		Object snapshot = session.getActive().getIntersectionResult();
		List<Timer> refreshTimers = new ArrayList<>();
		try (MockedConstruction<Timer> timers = mockConstruction(Timer.class,
				(timer, context) -> {
					if (Integer.valueOf(250).equals(context.arguments().get(0))) {
						refreshTimers.add(timer);
					}
				});
				MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showOptionDialog(any(Component.class), any(),
					anyString(), eq(JOptionPane.DEFAULT_OPTION), eq(JOptionPane.PLAIN_MESSAGE),
					isNull(), any(Object[].class), any())).thenAnswer(call -> {
						Container panel = call.getArgument(1);
						findComponent(panel, JList.class).setSelectedIndex(0);
						radius.setValue(1.25);
						radius.updateCascade();
						assertFalse(snapshot == session.getActive().getIntersectionResult());
						assertFalse(session.eligibleTokens().isEmpty());
						int count = size(app);
						for (String key : List.of("intersection.one", "intersection.selected",
								"intersection.all")) {
							JButton action = button(panel, GeoCeDGProfile.getText(key,
									app.getLocale().getLanguage()));
							assertTrue(action.isEnabled());
							action.doClick(0);
							assertTrue(session.materializedTokens().isEmpty());
							assertEquals(count, size(app));
						}
						return 1;
					});
			app.setErrorDialogsActive(true);
			controller(app).inspectRichResultSelection();
			// Swing controls can create other timers; none of the intercepted
			// timers execute, so rejection cannot depend on a refresh tick.
			assertFalse(timers.constructed().isEmpty());
			assertEquals(1, refreshTimers.size());
			verify(refreshTimers.get(0)).start();
			verify(refreshTimers.get(0)).stop();
			dialogs.verify(() -> JOptionPane.showMessageDialog(any(Component.class),
					any(), anyString(), eq(JOptionPane.WARNING_MESSAGE)), times(3));
		}
		assertTrue(session.materializedTokens().isEmpty());
	}

	@Test
	void nativeReopenIdentifiesAlreadyMaterializedChoices(@TempDir Path directory) {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = pairSession(app);
		String token = session.eligibleTokens().get(0);
		String label = session.materializeAll(false).get(0).getLabelSimple();
		Path file = directory.resolve("workspace-pair.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = app();
		assertTrue(reopened.loadFile(file.toFile(), false));
		GeoCeDGIntersectionSession restored = new GeoCeDGIntersectionSession(reopened);
		restored.activate((GeoLocusIntersectionResult) reopened.getKernel().lookupLabel("R"));
		assertEquals(Set.of(token), restored.materializedTokens());
		assertTrue(restored.eligibleTokens().isEmpty());
		assertTrue(((GeoPoint) reopened.getKernel().lookupLabel(label)).isDefined());
	}

	@Test
	void batchUndoRedoIsOneCoherentAction() throws Exception {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = circleSession(app);
		app.getKernel().initUndoInfo();
		var undo = app.getKernel().getConstruction().getUndoManager();
		await(() -> !undo.undoPossible());
		session.materializeAll(true);
		await(undo::undoPossible);
		assertEquals(2, session.materializedTokens().size());
		app.getKernel().undo();
		await(undo::redoPossible);
		session.activate((GeoLocusIntersectionResult) app.getKernel().lookupLabel("R"));
		assertTrue(session.materializedTokens().isEmpty());
		app.getKernel().redo();
		await(() -> {
			session.activate((GeoLocusIntersectionResult) app.getKernel().lookupLabel("R"));
			return session.materializedTokens().size() == 2;
		});
	}

	@Test
	void severalCertifiedRootsInOneSpanPairAreIndividuallyEligible() {
		AppGeoCeDG app = app();
		eval(app, "w(x,y)=1");
		eval(app, "S=SplineV2({(-0.1,-4),(-0.1,-4/3),(-0.1,4/3),(-0.1,4)},3,w)");
		eval(app, "T=SplineV2({(3,-6),(-5/9,10/27),(-5/9,-10/27),(3,6)},3,w)");
		GeoCeDGIntersectionSession session = activate(app, "Intersect(S,T)");
		assertEquals(2, session.eligibleTokens().size());
		assertEquals(2, session.materializeAll(false).size());
		assertEquals(2, session.materializedTokens().size());
	}

	@Test
	void sameGermAmbiguityDoesNotHideOtherIndependentlyCertifiedRoot() {
		AppGeoCeDG app = app();
		eval(app, "w(x,y)=1");
		eval(app, "S=SplineV2({(-3,-1),(-1,1),(1,-1),(3,1)},3,w)");
		eval(app, "T=SplineV2({(-4,0),(-4/3,0),(4/3,0),(4,0)},3,w)");
		GeoCeDGIntersectionSession session = activate(app, "Intersect(S,T)");
		assertEquals(3, session.solutions().size());
		assertEquals(1, session.eligibleTokens().size());
		assertEquals(1, session.materializeAll(false).size());
		assertEquals(1, session.materializedTokens().size());
	}

	@Test
	void canonicalKnotCrossingKeepsExistingMaterializedChoice() {
		AppGeoCeDG app = app();
		eval(app, "h=-0.1");
		eval(app, "S=SplineV2({(-1,0),(0,0),(1,0)},3)");
		eval(app, "T=SplineV2({(h,-1),(h,0),(h,1)},3)");
		GeoCeDGIntersectionSession session = activate(app, "Intersect(S,T)");
		GeoPoint point = session.materializeAll(false).get(0);
		Set<String> tokens = session.materializedTokens();
		for (double value : new double[] {0, 0.1, -0.1}) {
			move(app, "h", value);
			assertTrue(point.isDefined());
			assertEquals(tokens, session.materializedTokens());
			assertTrue(session.eligibleTokens().isEmpty());
		}
	}

	@Test
	void periodicPairSeamAndNegativeTransformConsumeExistingKernelCertificates() {
		AppGeoCeDG app = app();
		periodicPair(app, false);
		GeoCeDGIntersectionSession original = activate(app, "Intersect(S,T)");
		List<GeoPoint> points = original.materializeAll(false);
		assertEquals(2, points.size());
		Set<String> tokens = original.materializedTokens();
		for (double value : new double[] {0.1, 0, -0.1, 0.25}) {
			move(app, "h", value);
			assertTrue(points.stream().allMatch(GeoPoint::isDefined));
			assertEquals(tokens, original.materializedTokens());
		}
		eval(app, "DS=Dilate(S,-2,(0,0))");
		eval(app, "DT=Dilate(T,-2,(0,0))");
		GeoCeDGIntersectionSession transformed = new GeoCeDGIntersectionSession(app);
		transformed.activate((GeoLocusIntersectionResult) eval(app, "V=Intersect(DS,DT)"));
		List<GeoPoint> transformedPoints = transformed.materializeAll(false);
		assertEquals(2, transformedPoints.size());
		assertNotEquals(tokens, transformed.materializedTokens());
		assertTrue(transformedPoints.stream().allMatch(GeoPoint::isDefined));
	}

	@Test
	void periodicAmbiguousSheetsStayRichOnlyInMarkerAndBatchConsumers() {
		AppGeoCeDG app = app();
		periodicPair(app, true);
		GeoCeDGIntersectionSession session = activate(app, "Intersect(S,T)");
		assertEquals(4, session.solutions().size());
		assertTrue(session.markerSolutions().isEmpty());
		int count = size(app);
		assertTrue(session.eligibleTokens().isEmpty());
		assertTrue(session.materializeAll(false).isEmpty());
		assertEquals(count, size(app));
	}

	@Test
	void intervalDepthExhaustionNeverGetsPromotedByFrontend() {
		AppGeoCeDG app = app();
		eval(app, "S=SplineV2({(-1,1),(0,-1/65536),(1,1)},3)");
		eval(app, "T=SplineV2({(-2,0),(0,0),(2,0)},3)");
		GeoCeDGIntersectionSession session = activate(app, "Intersect(S,T)");
		assertEquals(2, session.solutions().size());
		int count = size(app);
		assertTrue(session.eligibleTokens().isEmpty());
		assertTrue(session.materializeAll(false).isEmpty());
		assertEquals(count, size(app));
	}

	@Test
	void dormantAndCollapsedBindingsReactivateWithoutCreatingReplacementPoints() {
		AppGeoCeDG app = app();
		periodicPair(app, false);
		eval(app, "k=1");
		eval(app, "DS=Dilate(S,k,(0,0))");
		eval(app, "DT=Dilate(T,k,(0,0))");
		GeoCeDGIntersectionSession session = activate(app, "Intersect(DS,DT)");
		GeoPoint point = session.materializeAll(false).get(0);
		Set<String> tokens = session.materializedTokens();
		int count = size(app);
		for (String parameter : List.of("h", "k")) {
			move(app, parameter, "h".equals(parameter) ? 9 : 0);
			assertFalse(point.isDefined());
			assertTrue(session.eligibleTokens().isEmpty());
			assertTrue(session.materializeAll(false).isEmpty());
			move(app, parameter, "h".equals(parameter) ? 0.25 : 1);
			assertTrue(point.isDefined());
			assertEquals(tokens, session.materializedTokens());
			assertEquals(count, size(app));
		}
	}

	@Test
	void staleSnapshotAndRemovedOwnerCannotProducePoints() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = circleSession(app);
		List<String> tokens = session.eligibleTokens();
		session.getActive().setUndefined();
		int count = size(app);
		assertTrue(session.markerSolutions().isEmpty());
		assertThrows(IllegalArgumentException.class, () -> session.materialize(tokens, false));
		assertEquals(count, size(app));
		session.getActive().remove();
		assertTrue(session.materializeAll(false).isEmpty());
	}

	@Test
	void renameAndCopyUseDurableOwnerRatherThanDisplayedLabel() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = pairSession(app);
		GeoPoint point = session.materializeAll(false).get(0);
		String token = token(point);
		var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
		var pointId = registry.getPersistentGeoId(point);
		session.getActive().rename("RenamedResult");
		point.rename("RenamedPoint");
		assertEquals(Set.of(token), session.materializedTokens());
		String clipboard = InternalClipboard.getTextToSave(app, List.of(point), text -> text);
		paste(app, clipboard);
		GeoIdentityRecord record = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(candidate -> pointId.equals(candidate.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoPoint copy = (GeoPoint) registry.getGeo(record.getId());
		GeoCeDGIntersectionSession copiedSession = new GeoCeDGIntersectionSession(app);
		copiedSession.activate(((AlgoLocusIntersectionPointV2) copy.getParentAlgorithm())
				.getRichInput());
		assertTrue(copy.isDefined());
		assertNotEquals(token, token(copy));
		assertEquals(Set.of(token(copy)), copiedSession.materializedTokens());
		assertTrue(copiedSession.materializeAll(false).isEmpty());
	}

	@Test
	void markerClickSeedsExactSolutionWithoutAnotherSolve() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = circleSession(app);
		var root = session.solutions().get(1);
		String expected = root.getIdentity().getRootToken();
		app.getSelectionManager().addSelectedGeo(session.getActive());
		controller(app).setIntersectionMarkersVisible(true);
		app.setMode(EuclidianConstants.MODE_MOVE);
		Object snapshot = session.getActive().getIntersectionResult();
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showOptionDialog(any(Component.class), any(),
					anyString(), eq(JOptionPane.DEFAULT_OPTION), eq(JOptionPane.PLAIN_MESSAGE),
					isNull(), any(Object[].class), any()))
					.thenReturn(JOptionPane.OK_OPTION);
			var click = event(app, root.getEvaluatedPoint().getX(),
					root.getEvaluatedPoint().getY());
			controller(app).wrapMousePressed(click);
			controller(app).wrapMouseReleased(click);
		}
		assertEquals(Set.of(expected), controller(app).getIntersectionSession()
				.materializedTokens());
		assertSame(snapshot, session.getActive().getIntersectionResult());
	}

	@Test
	void existingMaterializedPointTakesPrecedenceOverItsMarker() {
		AppGeoCeDG app = app();
		GeoCeDGIntersectionSession session = circleSession(app);
		GeoPoint point = session.materializeAll(false).get(0);
		app.getSelectionManager().addSelectedGeo(session.getActive());
		controller(app).setIntersectionMarkersVisible(true);
		app.setMode(EuclidianConstants.MODE_MOVE);
		app.getActiveEuclidianView().updateAllDrawables(true);
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			var click = event(app, point.getInhomX(), point.getInhomY());
			controller(app).wrapMousePressed(click);
			controller(app).wrapMouseReleased(click);
			dialogs.verifyNoInteractions();
		}
		assertTrue(app.getSelectionManager().getSelectedGeos().contains(point));
		assertEquals(2, session.materializedTokens().size());
	}

	@Test
	void intersectionPreviewWithAutoOptInCreatesNoRichOrPoint() {
		AppGeoCeDG app = app();
		GeoElement source = line(app);
		GeoElement target = eval(app, "c=Circle((0,0),1)");
		GeoCeDGEuclidianController controller = controller(app);
		app.setMode(EuclidianConstants.MODE_INTERSECT);
		controller.setAutoMaterializeIntersectionSolutions(true);
		controller.intersect(hits(source), false);
		int count = size(app);
		assertNull(controller.intersect(hits(target), true));
		assertNull(controller.getIntersectionSession().getActive());
		assertEquals(count, size(app));
	}

	@Test
	void automaticMaterializationHasOneCompoundRichAndPointUndo() throws Exception {
		AppGeoCeDG app = app();
		GeoElement source = line(app);
		GeoElement target = eval(app, "c=Circle((0,0),1)");
		GeoCeDGEuclidianController controller = controller(app);
		app.setMode(EuclidianConstants.MODE_INTERSECT);
		controller.setAutoMaterializeIntersectionSolutions(true);
		app.getKernel().initUndoInfo();
		var undo = app.getKernel().getConstruction().getUndoManager();
		await(() -> undo.getHistorySize() == 0);
		controller.switchModeForProcessMode(hits(source), false, false, changed -> {
			assertFalse(changed);
		}, false);
		controller.switchModeForProcessMode(hits(target), false, false, changed -> {
			assertTrue(changed);
			assertNotNull(controller.getIntersectionSession().getActive());
			assertEquals(2, controller.getIntersectionSession().materializedTokens().size());
			// This is the inherited productive process callback's undo operation.
			app.storeUndoInfo();
		}, false);
		GeoCeDGIntersectionSession session = controller.getIntersectionSession();
		String label = session.getActive().getLabelSimple();
		assertEquals(2, session.materializedTokens().size());
		await(() -> undo.getHistorySize() == 1);
		app.getKernel().undo();
		await(() -> undo.getHistorySize() == 0);
		assertNull(app.getKernel().lookupLabel(label));
		assertTrue(session.materializedTokens().isEmpty());
		app.getKernel().redo();
		await(() -> undo.getHistorySize() == 1);
		session.activate((GeoLocusIntersectionResult) app.getKernel().lookupLabel(label));
		assertEquals(2, session.materializedTokens().size());
	}

	private static Hits hits(GeoElement geo) {
		Hits hits = new Hits();
		hits.add(geo);
		return hits;
	}

	private static GeoCeDGIntersectionSession circleSession(AppGeoCeDG app) {
		line(app);
		eval(app, "c=Circle((0,0),1)");
		GeoCeDGIntersectionSession session = new GeoCeDGIntersectionSession(app);
		session.activate((GeoLocusIntersectionResult) eval(app, "R=Intersect(L,c)"));
		return session;
	}

	private static GeoCeDGIntersectionSession pairSession(AppGeoCeDG app) {
		spline(app);
		eval(app, "T=SplineV2({(0,-2),(0,-2/3),(0,2/3),(0,2)},3)");
		GeoCeDGIntersectionSession session = new GeoCeDGIntersectionSession(app);
		session.activate((GeoLocusIntersectionResult) eval(app, "R=Intersect(S,T)"));
		return session;
	}

	private static GeoCeDGIntersectionSession activate(AppGeoCeDG app, String command) {
		GeoCeDGIntersectionSession session = new GeoCeDGIntersectionSession(app);
		session.activate((GeoLocusIntersectionResult) eval(app, "R=" + command));
		return session;
	}

	private static void periodicPair(AppGeoCeDG app, boolean repeated) {
		eval(app, "A=(1,0)");
		eval(app, "B=(0,1)");
		eval(app, "C=(-1,0)");
		eval(app, "D=(0,-1)");
		eval(app, repeated ? "S=SplineV2({A,B,C,D,A,B,C,D,A},3)"
				: "S=SplineV2({A,B,C,D,A},3)");
		eval(app, "h=0.25");
		eval(app, "T=SplineV2({(-2,h),(0,h),(2,h)},3)");
	}

	private static void move(AppGeoCeDG app, String name, double value) {
		GeoNumeric numeric = (GeoNumeric) app.getKernel().lookupLabel(name);
		numeric.setValue(value);
		numeric.updateCascade();
	}

	private static String token(GeoPoint point) {
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm()).getEffectiveRootToken();
	}

	private static JButton button(Container root, String label) {
		List<JButton> buttons = new ArrayList<>();
		collectButtons(root, buttons);
		JButton button = buttons.stream().filter(value -> label.equals(value.getText()))
				.findFirst().orElse(null);
		assertNotNull(button, label);
		return button;
	}

	private static <T extends Component> T findComponent(Container root, Class<T> type) {
		for (Component child : root.getComponents()) {
			if (type.isInstance(child)) {
				return type.cast(child);
			}
			if (child instanceof Container) {
				T nested = findComponent((Container) child, type);
				if (nested != null) {
					return nested;
				}
			}
		}
		return null;
	}

	private static void collectButtons(Container root, List<JButton> buttons) {
		for (Component child : root.getComponents()) {
			if (child instanceof JButton) {
				buttons.add((JButton) child);
			}
			if (child instanceof Container) {
				collectButtons((Container) child, buttons);
			}
		}
	}

	private static void await(BooleanSupplier condition) throws Exception {
		long deadline = System.nanoTime() + 5_000_000_000L;
		while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
			Thread.sleep(10);
		}
		assertTrue(condition.getAsBoolean());
	}
}
