/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.M08SnapshotPlacement2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.View;
import org.junit.jupiter.api.Test;

/** Local proof, exact address state and ordinary-DAG regressions for CU7. */
class GeneralPointContinuationTest extends G9U0PublicSurfaceTestBase {
	private final Map<GeoPoint, LocusPointContinuation2D.Context> contexts = new IdentityHashMap<>();

	private LocusPointContinuation2D.Context context(GeoPoint point) {
		return contexts.computeIfAbsent(point, LocusV2PublicOperations::beginInteractiveSemanticPointDrag);
	}

	private GeoLocusV2 circle() {
		add("c=Circle((0,0),1)");
		add("N=Point(c)");
		add("v=Vector((0,0),N)");
		add("Q=N+(2*v)");
		return add("L=LocusV2(Q,N)");
	}

	private GeoLocusV2 nativeGraph() {
		add("I=(0.4965866445356883,1.7854818601338847)");
		add("J=(-0.5082406249102692,2.5763181370126467)");
		add("f=Circle(I,J)");
		add("D=Point(f)");
		add("g=Line(D,xAxis)");
		add("K=Intersect(g,yAxis)");
		add("M=Midpoint(K,D)");
		return add("h=LocusV2(M,D)");
	}

	private org.geocedg.common.kernel.locus.LocusPoint2D at(GeoLocusV2 source, double parameter) {
		try (var session = org.geocedg.common.kernel.locus.LocusEvaluationSession2D.memoizing(100)) {
			var evaluated = source.getSemanticDefinition().evaluate(BRANCH, parameter, session);
			assertTrue(evaluated.isValid());
			return evaluated.getPoint();
		}
	}

	private GeoPoint point(GeoLocusV2 source, double x, double y) {
		try (var selection = LocusPointCreation2D.query(source, x, y,
				LocusPointInteractionPolicy2D.initial(0.1))) {
			assertFalse(selection.getChoices().isEmpty(), selection.getFailure());
			return selection.commit("P", selection.getChoices().get(0));
		}
	}

	private LocusPointContinuation2D.Result move(GeoPoint point, double angle) {
		var gesture = context(point);
		long started = System.nanoTime();
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				3 * Math.cos(angle), 3 * Math.sin(angle),
				LocusPointInteractionPolicy2D.initial(0.1), gesture);
		sample("A", result, started);
		return result;
	}

	private static void sample(String provider, LocusPointContinuation2D.Result result, long started) {
		long elapsed = System.nanoTime() - started;
		if (result.isEstablished()) {
			System.out.println("CU7 continuation provider=" + provider + " method=" + result.getMethod()
					+ " proofPointEvaluations=" + result.getSemanticEvaluations()
					+ " proofPasses=" + result.getIntervalPasses() + " wholeMutationNanos=" + elapsed);
		}
	}

	@Test
	void v3CircleMovesBothDirectionsWithIdentityAndDependents() {
		GeoPoint point = point(circle(), 3, 0);
		GeoPoint dependent = add("Dependent=P+(1,0)");
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		int size = getConstruction().getGeoSetConstructionOrder().size();
		for (double angle : new double[] {0.2, 0.4, 0.6, 0.4, 0.2, 0}) {
			var result = move(point, angle);
			assertTrue(result.isEstablished(), result.getDiagnostic());
			assertEquals("STRUCTURAL_AFFINE_CIRCLE_CHART", result.getMethod());
			assertSame(point, lookup("P"));
			assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point));
			assertEquals(size, getConstruction().getGeoSetConstructionOrder().size());
			assertEquals(3 * Math.cos(angle), point.getInhomX(), 1E-10);
			assertEquals(3 * Math.sin(angle), point.getInhomY(), 1E-10);
			assertEquals(point.getInhomX() + 1, dependent.getInhomX(), 1E-12);
			assertTrue(result.getIntervalPasses() > 0);
		}
	}

	@Test
	void v3CircleCrossesProviderSeamAndReversesWithoutChangingPoint() {
		GeoPoint point = point(circle(), 3, 0);
		for (double angle : new double[] {0.7, 1.4, 2.1, 2.8, Math.PI - 0.05}) {
			assertTrue(move(point, angle).isEstablished());
		}
		var before = ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getCurrentSemanticAddress();
		assertTrue(move(point, Math.PI + 0.05).isEstablished());
		var after = ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getCurrentSemanticAddress();
		assertEquals(before.getPeriodicLift() + 1, after.getPeriodicLift());
		assertEquals(before.getComponentLineageKey(), after.getComponentLineageKey());
		var reverse = move(point, Math.PI - 0.05);
		assertTrue(reverse.isEstablished(), reverse.getDiagnostic());
		assertEquals(before, ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm())
				.getCurrentSemanticAddress());
	}

	@Test
	void antipodeCentreAndLockedRequestsKeepExactConstruction() {
		GeoPoint point = point(circle(), 3, 0);
		String before = getApp().getXML();
		assertFalse(move(point, Math.PI).isEstablished());
		assertEquals(before, getApp().getXML());
		assertFalse(LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point, 0, 0,
				LocusPointInteractionPolicy2D.initial(0.1), context(point)).isEstablished());
		assertEquals(before, getApp().getXML());
		point.setFixed(true);
		before = getApp().getXML();
		assertEquals(LocusPointContinuation2D.Status.FIXED_POINT, move(point, 0.1).getStatus());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void proofBudgetExhaustionDoesNotChangeAddressOrInputs() {
		GeoPoint point = point(circle(), 3, 0);
		String before = getApp().getXML();
		var policy = new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D(1, 2, 1, 1));
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				3 * Math.cos(0.1), 3 * Math.sin(0.1), policy, context(point));
		assertEquals(LocusPointContinuation2D.Status.BUDGET_EXHAUSTED, result.getStatus());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void hRegularChartUsesWholeIntervalAndPropagates() {
		GeoLocusV2 source = nativeGraph();
		GeoPoint point;
		try (var selection = M08SnapshotPlacement2D.query(source,
				0.8976484384297626, 1.7854818601338847,
				LocusPointInteractionPolicy2D.initial(0.1))) {
			assertFalse(selection.getResult().getChoices().isEmpty());
			point = selection.commit("P", selection.getResult().getChoices().get(0));
		}
		var parent = (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
		double original = parent.getCurrentSemanticAddress().getCanonicalParameter();
		try (var session = org.geocedg.common.kernel.locus.LocusEvaluationSession2D.memoizing(100)) {
			var target = source.getSemanticDefinition().evaluate(
					parent.getCurrentSemanticAddress().getBranchKey(), original + 0.05, session);
			assertTrue(target.isValid());
			long started = System.nanoTime();
			var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					target.getPoint().getX(), target.getPoint().getY(),
					LocusPointInteractionPolicy2D.initial(0.1), context(point));
			sample("B", result, started);
			assertTrue(result.isEstablished(), result.getDiagnostic());
			assertEquals("CERTIFIED_MONOTONE_COORDINATE_CHART", result.getMethod());
			assertEquals(original + 0.05, parent.getCurrentSemanticAddress()
					.getCanonicalParameter(), 1E-10);
		}
	}

	@Test
	void reopenPreservesMovedAddressRoleAndContinuedMotion() {
		GeoPoint point = point(circle(), 3, 0);
		assertTrue(move(point, 0.1).isEstablished());
		var address = ((AlgoSemanticLocusPoint2D) point.getParentAlgorithm()).getCurrentSemanticAddress();
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		getApp().setXML(getApp().getXML(), true);
		GeoPoint reopened = (GeoPoint) lookup("P");
		assertNotNull(reopened);
		assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(reopened));
		assertEquals(address, ((AlgoSemanticLocusPoint2D) reopened.getParentAlgorithm())
				.getCurrentSemanticAddress());
		assertTrue(move(reopened, 0.2).isEstablished());
	}

	@Test
	void exactPointBudgetPublishesOneEvaluationAndReportsObservedMutationLatency() {
		GeoLocusV2 source = circle();
		GeoPoint point = point(source, 3, 0);
		var parent = (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
		var policy = LocusPointInteractionPolicy2D.initial(0.1);
		var preview = LocusPointContinuation2D.resolve(source, parent.getCurrentSemanticAddress(),
				3 * Math.cos(0.1), 3 * Math.sin(0.1), policy, context(point));
		assertTrue(preview.isEstablished());
		var exact = new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D((int) preview.getSemanticEvaluations(),
						(int) preview.getIntervalPasses(), 80, 1));
		long calls = source.getInstrumentation().getEvaluatorCalls();
		long started = System.nanoTime();
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				3 * Math.cos(0.1), 3 * Math.sin(0.1), exact, context(point));
		long elapsed = System.nanoTime() - started;
		assertTrue(result.isEstablished(), result.getDiagnostic());
		assertEquals(preview.getSemanticEvaluations(), result.getSemanticEvaluations());
		assertEquals(result.getSemanticEvaluations(), source.getInstrumentation().getEvaluatorCalls() - calls);
		System.out.println("CU7 continuation proofPointEvaluations=" + result.getSemanticEvaluations()
				+ " proofPasses=" + result.getIntervalPasses() + " wholeMutationNanos=" + elapsed);
		AtomicBoolean notified = new AtomicBoolean();
		View view = mock(View.class);
		doAnswer(invocation -> { notified.set(true); return null; })
				.when(view).update(any(GeoElement.class));
		getKernel().attach(view);
		try {
			var repeated = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					3 * Math.cos(0.1), 3 * Math.sin(0.1), exact, context(point));
			assertTrue(repeated.isEstablished());
			assertFalse(repeated.hasChanged());
			assertFalse(notified.get());
		} finally {
			getKernel().detach(view);
		}
	}

	@Test
	void preparedBindingAndContextRejectForeignStaleAndConsumedUse() {
		GeoLocusV2 source = circle();
		GeoPoint point = point(source, 3, 0);
		var parent = (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
		var policy = LocusPointInteractionPolicy2D.initial(0.1);
		var firstContext = context(point);
		var stale = LocusPointContinuation2D.resolve(source, parent.getCurrentSemanticAddress(),
				3 * Math.cos(0.1), 3 * Math.sin(0.1), policy, firstContext);
		assertTrue(move(point, 0.2).isEstablished());
		String before = getApp().getXML();
		assertThrows(IllegalStateException.class, () -> parent.prepareContinuationBinding(stale));
		assertEquals(before, getApp().getXML());
		var fresh = LocusPointContinuation2D.resolve(source, parent.getCurrentSemanticAddress(),
				3 * Math.cos(0.3), 3 * Math.sin(0.3), policy, firstContext);
		parent.prepareContinuationBinding(fresh);
		parent.discardContinuationBinding();
		assertThrows(IllegalStateException.class, () -> parent.prepareContinuationBinding(fresh));
		assertEquals(before, getApp().getXML());
		GeoPoint other = add("Other=Point(L,0.2)");
		assertThrows(IllegalArgumentException.class,
				() -> LocusV2PublicOperations.beginInteractiveSemanticPointDrag(other));
		var foreign = LocusPointContinuation2D.begin(other);
		before = getApp().getXML();
		assertEquals(LocusPointContinuation2D.Status.INVALID_SOURCE,
				LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point, 3, 0,
						policy, foreign).getStatus());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void identicalOffCurveRequestsDoNotOscillateWhenAnAxisWouldOtherwiseSwitch() {
		GeoLocusV2 source = nativeGraph();
		var initial = at(source, 0.784);
		GeoPoint point = point(source, initial.getX(), initial.getY());
		var policy = LocusPointInteractionPolicy2D.initial(0.1);
		var target = at(source, Math.PI / 4);
		var first = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				target.getX() + 0.001, target.getY() + 0.001, policy, context(point));
		assertTrue(first.isEstablished(), first.getDiagnostic());
		double selected = first.getAddress().getCanonicalParameter();
		assertTrue(selected > Math.PI / 4);
		for (int event = 0; event < 8; event++) {
			var next = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					target.getX() + 0.001, target.getY() + 0.001, policy, context(point));
			assertTrue(next.isEstablished(), next.getDiagnostic());
			assertEquals(selected, next.getAddress().getCanonicalParameter(), 2E-12);
		}
	}

	@Test
	void sameComponentClosePreimagesDoNotJumpAndFoldStopsWithoutMutation() {
		add("c=Circle((0,0),1)");
		add("D=Point(c)");
		add("g=Line(D,xAxis)");
		add("K=Intersect(g,yAxis)");
		GeoLocusV2 source = add("L=LocusV2(K,D)");
		GeoPoint point;
		try (var selection = LocusPointCreation2D.query(source, 0, 0,
				LocusPointInteractionPolicy2D.initial(0.1))) {
			assertFalse(selection.getChoices().isEmpty(), selection.getFailure());
			var chosen = selection.getChoices().stream().min(java.util.Comparator.comparingDouble(
					choice -> Math.abs(choice.getAddress().getCanonicalParameter()))).orElseThrow();
			point = selection.commit("P", chosen);
		}
		var policy = LocusPointInteractionPolicy2D.initial(0.1);
		var sameImage = at(source, Math.PI - 0.2);
		var move = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				sameImage.getX(), sameImage.getY(), policy, context(point));
		assertTrue(move.isEstablished(), move.getDiagnostic());
		assertEquals(0.2, move.getAddress().getCanonicalParameter(), 1E-10);
		String before = getApp().getXML();
		var barrier = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				0, 1, policy, context(point));
		assertFalse(barrier.isEstablished());
		assertEquals(before, getApp().getXML());
		assertEquals(LocusPointContinuation2D.Status.CONTINUATION_STOPPED,
				LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
						sameImage.getX(), sameImage.getY(), policy, context(point)).getStatus());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void sourceMutationDuringPublicationRollsBackBeforeContextAdvance() {
		add("Radius=1");
		add("c=Circle((0,0),Radius)");
		add("N=Point(c)");
		add("v=Vector((0,0),N)");
		add("Q=N+(2*v)");
		GeoLocusV2 source = add("L=LocusV2(Q,N)");
		GeoPoint point = point(source, 3, 0);
		var gesture = context(point);
		String before = getApp().getXML();
		AtomicBoolean changed = new AtomicBoolean();
		View hostile = mock(View.class);
		doAnswer(invocation -> {
			if (invocation.getArgument(0) == point && changed.compareAndSet(false, true)) {
				// Explicit undefined state retains the exact same semantic definition pointer.
				source.setUndefined();
			}
			return null;
		}).when(hostile).update(any(GeoElement.class));
		getKernel().attach(hostile);
		try {
			assertThrows(RuntimeException.class, () -> move(point, 0.1));
		} finally {
			getKernel().detach(hostile);
		}
		assertTrue(changed.get());
		assertTrue(gesture.isStopped());
		assertEquals(before, getApp().getXML());
	}

	@Test
	void dilateCancellationCannotGrantAnUnboundedParameterRepresentative() {
		add("c=Circle((0,0),1)");
		add("D=Point(c)");
		add("W=Dilate(D,0.25,(10^16,10^16))");
		GeoLocusV2 source = add("L=LocusV2(W,D)");
		var definition = source.getSemanticDefinition();
		var branch = definition.getBranch(BRANCH);
		var component = branch.getExistenceStructure().getContinuousValidComponents().get(0);
		var address = new LocusSemanticAddress2D(source.getPersistentLocusId(),
				definition.getProvider().getProviderId(), BRANCH,
				LocusComponentLineage2D.of(branch.getExistenceStructure(), component), 0, 0,
				LocusSemanticAddress2D.SeamSide.INTERIOR);
		var target = at(source, 0.1);
		var proposal = org.geocedg.common.kernel.locus.intersection.LocalContinuationEvidence2D.establish(
				definition, address, component, target.getX(), target.getY(),
				LocusPointInteractionPolicy2D.initial(0.1), -1, () -> { });
		assertNull(proposal);
	}

	@Test
	void historicalSplineRefusalAndBudgetRetainApprovedGestureRecovery() {
		add("A=(-2,0)");
		add("B=(-2/3,0)");
		add("C=(2/3,0)");
		add("D=(2,0)");
		GeoLocusV2 source = add("L=SplineV2({A,B,C,D},3)");
		GeoPoint point = point(source, 0, 0);
		var policy = LocusPointInteractionPolicy2D.initial(0.1);
		String before = getApp().getXML();
		var refused = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				100, 100, policy, context(point));
		assertFalse(refused.isEstablished());
		assertNotNull(refused.getHistoricalResult());
		assertFalse(context(point).isStopped());
		assertEquals(before, getApp().getXML());
		long started = System.nanoTime();
		var resumed = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				1, 0, policy, context(point));
		sample("C", resumed, started);
		assertTrue(resumed.isEstablished(), resumed.getDiagnostic());
		var exhausted = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				0.5, 0, new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-12,
						new LocusPointInteractionWorkBudget2D(1, 2, 1, 1)), context(point));
		assertEquals(LocusPointContinuation2D.Status.BUDGET_EXHAUSTED, exhausted.getStatus());
		assertFalse(context(point).isStopped());
		assertTrue(LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				-1, 0, policy, context(point)).isEstablished());
	}

	@Test
	void providerCanonicalRoundingCannotExceedDeclaredArgumentTolerance() {
		GeoPoint point = point(circle(), 3, 0);
		String before = getApp().getXML();
		var policy = new LocusPointInteractionPolicy2D(0.1, 1E-10, 1E-20,
				LocusPointInteractionWorkBudget2D.initial());
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				3 * Math.cos(1E-10), 3 * Math.sin(1E-10), policy, context(point));
		assertFalse(result.isEstablished());
		assertEquals(before, getApp().getXML());
	}
}
