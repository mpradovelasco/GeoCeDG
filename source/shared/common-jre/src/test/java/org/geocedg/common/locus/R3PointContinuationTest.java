/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** R3 transverse continuation; none of these cases adds a model-specific evaluator. */
class R3PointContinuationTest extends G9U0PublicSurfaceTestBase {

	private final LocusPointInteractionPolicy2D policy = LocusPointInteractionPolicy2D.initial(.1);

	private LocusPoint2D at(GeoLocusV2 source, double parameter) {
		try (var session = LocusEvaluationSession2D.memoizing(100)) {
			var evaluated = source.getSemanticDefinition().evaluate(BRANCH, parameter, session);
			assertTrue(evaluated.isValid(), evaluated.toString());
			return evaluated.getPoint();
		}
	}

	private GeoPoint point(GeoLocusV2 source, double parameter) {
		var target = at(source, parameter);
		try (var selection = LocusPointCreation2D.query(source, target.getX(), target.getY(), policy)) {
			assertFalse(selection.getChoices().isEmpty(), selection.getFailure());
			var choice = selection.getChoices().stream().min(Comparator.comparingDouble(
					candidate -> Math.abs(candidate.getAddress().getCanonicalParameter() - parameter)))
					.orElseThrow();
			return selection.commit("P", choice);
		}
	}

	private LocusSemanticAddress2D address(GeoPoint point) {
		return AlgoSemanticLocusPoint2D.requireSemanticParent(point).getCurrentSemanticAddress();
	}

	private LocusPointContinuation2D.Result move(GeoPoint point,
			LocusPointContinuation2D.Context context, GeoLocusV2 source, double parameter) {
		var target = at(source, parameter);
		var previous = address(point);
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				target.getX(), target.getY(), policy, context);
		assertTrue(result.isEstablished(), () -> "targetParameter=" + parameter
				+ " previous=" + previous.getCanonicalParameter() + " lift=" + previous.getPeriodicLift()
				+ " status=" + result.getDiagnostic() + " proofPasses=" + result.getIntervalPasses());
		assertEquals("LOCAL_STRICT_PROJECTION_CHART", result.getMethod());
		assertEquals(parameter, address(point).getCanonicalParameter(), 2E-11);
		return result;
	}

	private GeoLocusV2 lineCircle() {
		add("c=Circle((0,0),2)");
		GeoPoint driver = add("N=Point(c)");
		driver.updatePathParameter(Math.PI / 2);
		driver.updateCascade();
		add("g=Line(N,yAxis)");
		add("f=Circle((0,0),1)");
		add("Q=Intersect(g,f,1)");
		return add("L=LocusV2(Q,N)");
	}

	private GeoLocusV2 h() {
		add("I=(0.4965866445356883,1.7854818601338847)");
		add("J=(-0.5082406249102692,2.5763181370126467)");
		add("f=Circle(I,J)");
		add("D=Point(f)");
		add("g=Line(D,xAxis)");
		add("K=Intersect(g,yAxis)");
		add("M=Midpoint(K,D)");
		return add("h=LocusV2(M,D)");
	}

	@Test
	void regularLineCircleUsesExistingUniformSelectorAndKeepsIdentityAndDag() {
		GeoLocusV2 source = lineCircle();
		GeoPoint point = point(source, Math.PI / 2);
		GeoPoint dependent = add("Dependent=P+(1,2)");
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		var initial = address(point);
		int count = getConstruction().getGeoSetConstructionOrder().size();
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		for (double parameter : new double[] {Math.PI / 2 + .001, Math.PI / 2 + .05,
				Math.PI / 2 + .1, Math.PI / 2 + .02, Math.PI / 2 - .05}) {
			move(point, context, source, parameter);
			assertEquals(2 * Math.cos(parameter), point.getInhomX(), 5E-11);
			assertEquals(1, point.getInhomX() * point.getInhomX()
					+ point.getInhomY() * point.getInhomY(), 5E-11);
			assertEquals(initial.getComponentLineageKey(), address(point).getComponentLineageKey());
			assertSame(point, lookup("P"));
			assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point));
			assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
			assertEquals(point.getInhomX() + 1, dependent.getInhomX(), 1E-12);
			assertEquals(point.getInhomY() + 2, dependent.getInhomY(), 1E-12);
		}
	}

	@Test
	void coincidentDisconnectedImageKeepsChosenComponentAndGapStopsWithoutMutation() {
		GeoLocusV2 source = lineCircle();
		GeoPoint point = point(source, Math.PI / 2);
		var initial = address(point);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var otherImage = at(source, -Math.PI / 2 + .02);
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				otherImage.getX(), otherImage.getY(), policy, context);
		assertTrue(result.isEstablished(), result.getDiagnostic());
		assertTrue(address(point).getCanonicalParameter() > 0);
		assertEquals(initial.getComponentLineageKey(), address(point).getComponentLineageKey());
		String before = getApp().getXML();
		var last = address(point);
		var gap = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				2, 0, policy, context);
		assertFalse(gap.isEstablished());
		assertTrue(context.isStopped());
		assertEquals(last, address(point));
		assertEquals(before, getApp().getXML());
	}

	@Test
	void v3SegmentMovesBothWaysAndUsesOnlyRealClosedEndpoints() {
		add("s=Segment((0,0),(1,0))");
		add("N=Point(s)");
		add("v=Vector((0,0),N)");
		add("Q=N+(2*v)");
		GeoLocusV2 source = add("L=LocusV2(Q,N)");
		GeoPoint point = point(source, .5);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, source, .6);
		move(point, context, source, .9);
		var upper = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				3.03, 0, policy, context);
		assertTrue(upper.isEstablished(), upper.getDiagnostic());
		assertEquals(1, address(point).getCanonicalParameter(), 0);
		move(point, context, source, .7);
		move(point, context, source, .1);
		var lower = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				-.03, 0, policy, context);
		assertTrue(lower.isEstablished(), lower.getDiagnostic());
		assertEquals(0, address(point).getCanonicalParameter(), 0);
		move(point, context, source, .2);
	}

	@Test
	void v3CircularArcPreservesItsParameterOrientationAndEndpointRecovery() {
		add("a=CircularArc((0,0),(1,0),(0,1))");
		add("N=Point(a)");
		add("v=Vector((0,0),N)");
		add("Q=N+(2*v)");
		GeoLocusV2 source = add("L=LocusV2(Q,N)");
		GeoPoint point = point(source, .5);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, source, .7);
		move(point, context, source, .9);
		var upper = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				-.02, 3, policy, context);
		assertTrue(upper.isEstablished(), upper.getDiagnostic());
		assertEquals(1, address(point).getCanonicalParameter(), 0);
		move(point, context, source, .6);
		move(point, context, source, .2);
		var lower = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				3, -.02, policy, context);
		assertTrue(lower.isEstablished(), lower.getDiagnostic());
		assertEquals(0, address(point).getCanonicalParameter(), 0);
		move(point, context, source, .1);
	}

	@Test
	void hChangesLocalChartsAndCrossesItsCertifiedSeamInOneGesture() {
		GeoLocusV2 source = h();
		GeoPoint point = point(source, .1);
		var initial = address(point);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		for (double parameter = .3; parameter < 3; parameter += .2) {
			move(point, context, source, parameter);
		}
		move(point, context, source, Math.PI - .04);
		long beforeSeam = address(point).getPeriodicLift();
		move(point, context, source, -Math.PI + .04);
		assertEquals(beforeSeam + 1, address(point).getPeriodicLift());
		move(point, context, source, Math.PI - .04);
		assertEquals(beforeSeam, address(point).getPeriodicLift());
		for (double parameter = 2.8; parameter > .2; parameter -= .2) {
			move(point, context, source, parameter);
		}
		assertEquals(initial.getComponentLineageKey(), address(point).getComponentLineageKey());
	}

	@Test
	void unitVectorDotAndMeetShareTheSameTransversalProvider() {
		add("c=Circle((0,0),1)");
		add("N=Point(c)");
		add("A=(3,4)");
		add("v=Vector(A,N)");
		add("w=UnitVector(v)");
		add("k=v*w");
		add("E=A+(k*w)");
		add("g=Line(E,xAxis)");
		add("j=Line(N,yAxis)");
		add("Q=Intersect(g,j)");
		GeoLocusV2 source = add("L=LocusV2(Q,N)");
		GeoPoint point = point(source, .2);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		for (double parameter : new double[] {.25, .4, .6, .4, .1}) {
			move(point, context, source, parameter);
			assertEquals(Math.cos(parameter), point.getInhomX(), 5E-11);
			assertEquals(Math.sin(parameter), point.getInhomY(), 5E-11);
		}
	}

	@Test
	void rationalHyperbolicMeetVectorConstructionMovesBothWaysWithoutSpecialSolver() {
		add("s=Segment((.5,0),(2,0))");
		add("N=Point(s)");
		add("g=Line(N,(0,1))");
		add("k=Line((1,0),(1,1))");
		add("T=Intersect(g,k)");
		add("w=Vector((0,0),T)");
		add("Q=N+w");
		GeoLocusV2 source = add("L=LocusV2(Q,N)");
		assertEquals(org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness.COMPLETE,
				source.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure().getCompleteness());
		GeoPoint point = point(source, .4);
		var initial = address(point);
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		int count = getConstruction().getGeoSetConstructionOrder().size();
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		for (double parameter : new double[] {.2, .35, .6, .8, .6, .4, .2}) {
			move(point, context, source, parameter);
			double x = .5 + 1.5 * parameter;
			assertEquals(x + 1, point.getInhomX(), 5E-11);
			assertEquals(1 - 1 / x, point.getInhomY(), 5E-11);
			assertSame(point, lookup("P"));
			assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point));
			assertEquals(initial.getComponentLineageKey(), address(point).getComponentLineageKey());
			assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
		}
	}

	@Test
	void directCertifiedParabolaContinuesAcrossCoordinateTurningPoint() {
		GeoLocusV2 source = certifiedParabola();
		GeoPoint point = point(source, .4);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		for (double parameter : new double[] {.45, .49, .5, .51, .55, .6, .5, .4}) {
			move(point, context, source, parameter);
			double expectedX = 4 * parameter - 2;
			assertEquals(expectedX, point.getInhomX(), 5E-11);
			assertEquals(expectedX * expectedX, point.getInhomY(), 5E-11);
		}
	}

	@Test
	void offCurveRepeatedTargetsKeepExactRepresentativeAcrossFormerAxisTie() {
		GeoLocusV2 source = h();
		GeoPoint point = point(source, .7);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		for (double parameter : new double[] {.75, .78, .8, .9, 1.1, 1.3, 1.5, 1.7}) {
			var target = at(source, parameter);
			double x = target.getX() + .001;
			double y = target.getY() + .001;
			var first = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					x, y, policy, context);
			assertTrue(first.isEstablished(), first.getDiagnostic());
			var selected = address(point);
			String before = getApp().getXML();
			for (int repeat = 0; repeat < 3; repeat++) {
				var repeated = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
						x, y, policy, context);
				assertTrue(repeated.isEstablished(), repeated.getDiagnostic());
				assertFalse(repeated.hasChanged());
				assertEquals(selected, address(point));
				assertEquals(before, getApp().getXML());
			}
		}
	}

	@Test
	void smallOffCurveMovesNearRepeatedImagesRespectTheDeclaredParameterBound() {
		add("c=Circle((0,0),1)");
		add("N=Point(c)");
		add("g=Line(N,xAxis)");
		add("Q=Intersect(g,yAxis)");
		GeoLocusV2 source = add("L=LocusV2(Q,N)");
		GeoPoint point = point(source, .2);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		// Establish the normal offset first; the following requests are genuinely tiny
		// displacements from that accepted off-curve target, not from F(a0).
		var offset = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				.001, Math.sin(.2), policy, context);
		assertTrue(offset.isEstablished(), offset.getDiagnostic());
		for (int event = 1; event <= 12; event++) {
			double targetParameter = .2 + event * 1E-4;
			double previous = address(point).getCanonicalParameter();
			var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					.001, Math.sin(targetParameter), policy, context);
			assertTrue(result.isEstablished(), result.getDiagnostic());
			assertEquals("LOCAL_STRICT_PROJECTION_CHART", result.getMethod());
			assertTrue(Double.isFinite(result.getMaximumParameterChange()));
			assertTrue(result.getMaximumParameterChange() < .001,
					() -> "bound=" + result.getMaximumParameterChange() + " target=" + targetParameter);
			assertTrue(Math.abs(address(point).getCanonicalParameter() - previous)
					<= result.getMaximumParameterChange());
			assertTrue(result.getParameterError() <= policy.getParameterTolerance());
			assertEquals(targetParameter, address(point).getCanonicalParameter(), 2E-11);
			assertTrue(address(point).getCanonicalParameter() < .3,
					"The close preimage pi-u is never substituted for the retained sheet");
			var selected = address(point);
			var repeated = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					.001, Math.sin(targetParameter), policy, context);
			assertTrue(repeated.isEstablished(), repeated.getDiagnostic());
			assertFalse(repeated.hasChanged());
			assertEquals(0, repeated.getMaximumParameterChange(), 0);
			assertEquals(selected, address(point));
		}
	}

	@Test
	void exactForwardBudgetAndExhaustedProofRemainAtomic() {
		GeoLocusV2 source = lineCircle();
		GeoPoint point = point(source, Math.PI / 2);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var target = at(source, Math.PI / 2 + .02);
		var oneForward = new LocusPointInteractionPolicy2D(.1, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D(1, 512, 80, 1024));
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				target.getX(), target.getY(), oneForward, context);
		assertTrue(result.isEstablished(), result.getDiagnostic());
		assertEquals(1, result.getSemanticEvaluations());
		assertTrue(result.getIntervalPasses() > 2);
		String before = getApp().getXML();
		var previous = address(point);
		var exhausted = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				target.getX(), target.getY(), new LocusPointInteractionPolicy2D(.1, 1E-10, 1E-12,
						new LocusPointInteractionWorkBudget2D(1, 2, 80, 1024)), context);
		assertEquals(LocusPointContinuation2D.Status.BUDGET_EXHAUSTED, exhausted.getStatus());
		assertEquals(previous, address(point));
		assertEquals(before, getApp().getXML());
	}

	@Test
	void movedGeneralPointReopensWithSameIdAddressAndNewGesture() {
		GeoLocusV2 source = lineCircle();
		GeoPoint point = point(source, Math.PI / 2);
		move(point, LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point),
				source, Math.PI / 2 + .02);
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		var selected = address(point);
		getApp().setXML(getApp().getXML(), true);
		GeoPoint reopened = (GeoPoint) lookup("P");
		GeoLocusV2 reopenedSource = (GeoLocusV2) lookup("L");
		assertNotNull(reopened);
		assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(reopened));
		assertEquals(selected, address(reopened));
		move(reopened, LocusV2PublicOperations.beginInteractiveSemanticPointDrag(reopened),
				reopenedSource, Math.PI / 2 + .04);
	}
}
