/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.DoubleFunction;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.algos.AlgoLocusLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.LocusSnapshotPlacement2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DomainLocation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Post-1.2.0 maintenance of point creation and dragging on Locus V2: a locus whose
 * generator reads the length of another locus has no interval model, so its points are
 * created by the individual forward permission and moved by the forward parametric
 * continuation; a refused request never ends the gesture.
 */
class Maintenance120PointInteractionTest extends R4ContinuationTestBase {
	private static final String FORWARD = "LOCAL_FORWARD_PARAMETRIC_CONTINUATION";

	/**
	 * A half circle whose argument is a quarter of the arc length measured on another locus.
	 * Its component is a full-length open arc: the start {@code u = 0} is the included image
	 * (1, 0), and the excluded end is the one-sided limit (-1, 0).
	 */
	private GeoLocusV2 nestedMetric() {
		return nestedMetric("m/4");
	}

	/**
	 * The unit circle traced from (1, 0) up to the polar angle that {@code angle} gives to the
	 * whole length {@code m} of the support, which runs over {@code [0, 4 pi)}.
	 */
	private GeoLocusV2 nestedMetric(String angle) {
		add("C=Circle((0,0),2)");
		add("E=Point(C)");
		add("Q=E+(0,0)");
		add("L=LocusV2(Q,E)");
		add("P0=Point(L,\"generator.main\",0)");
		add("m=LocusLength(L,P0,Q)");
		add("lengthAngle=" + angle);
		add("T=Rotate((1,0),lengthAngle)");
		return add("W=LocusV2(T,E)");
	}

	/** @return the canonical parameter of {@code [-pi, pi)} of a lifted one of {@code [0, 2 pi)} */
	private static double canonical(double lifted) {
		return lifted < Math.PI ? lifted : lifted - 2 * Math.PI;
	}

	private static ContinuousComponent component(GeoLocusV2 source) {
		return source.getSemanticDefinition().getBranch(BRANCH).getExistenceStructure()
				.getContinuousValidComponents().get(0);
	}

	private static double parameterOf(double angle) {
		// The image at canonical u (lifted to [0, 2 pi)) has polar angle u / 2.
		double lifted = 2 * angle;
		return lifted < Math.PI ? lifted : lifted - 2 * Math.PI;
	}

	@Test void creationReplaysItsDiscoveryInsteadOfChargingItForEveryPermission() {
		GeoLocusV2 source = nestedMetric();
		var target = at(source, parameterOf(1));
		long discovery;
		try (var historical = LocusSnapshotPlacement2D.query(source, target.getX(), target.getY(), policy)) {
			discovery = historical.getSemanticMisses();
		}
		assertTrue(discovery > 0);
		try (var offer = LocusPointCreation2D.query(source, target.getX(), target.getY(), policy)) {
			assertEquals(LocusPointCreation2D.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION, offer.getRoute(),
					offer.getFailure());
			assertEquals(1, offer.getChoices().size());
			// The later permissions add their own proofs and confirmations only.
			assertTrue(offer.getSemanticMisses() < discovery + discovery / 2,
					"total=" + offer.getSemanticMisses() + " discovery=" + discovery);
		}
		// A budget that holds the discovery once, but not twice, is now sufficient.
		var once = new LocusPointInteractionPolicy2D(policy.getMaximumWorldDistance(), 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D((int) (discovery + discovery / 2), 512, 80, 1024));
		try (var offer = LocusPointCreation2D.query(source, target.getX(), target.getY(), once)) {
			assertEquals(LocusPointCreation2D.Route.INDIVIDUAL_FORWARD_ADDRESS_CREATION, offer.getRoute(),
					offer.getFailure());
		}
		// A budget below the discovery itself still refuses, with a typed cause and no detail.
		var starved = new LocusPointInteractionPolicy2D(policy.getMaximumWorldDistance(), 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D((int) (discovery / 2), 512, 80, 1024));
		try (var offer = LocusPointCreation2D.query(source, target.getX(), target.getY(), starved)) {
			assertEquals(LocusPointCreation2D.Route.REJECTED, offer.getRoute());
			assertEquals("BUDGET_EXHAUSTED", offer.getFailure());
			assertTrue(offer.getChoices().isEmpty());
		}
	}

	@Test void nestedMetricPointMovesFluidlyAndKeepsIdentityAndDependents() {
		GeoLocusV2 source = nestedMetric();
		GeoPoint point = offered(source, parameterOf(1), "P");
		GeoPoint dependent = add("Dependent=P+(1,2)");
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		var component = address(point).getComponentLineageKey();
		int count = getConstruction().getGeoSetConstructionOrder().size();
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		// Requests off the trace on both sides, forwards and backwards.
		double[] angles = {1.05, 1.2, 1.6, 2.0, 1.7, 1.1, 0.6};
		double[] offsets = {.02, -.03, .05, 0, -.05, .04, 0};
		for (int index = 0; index < angles.length; index++) {
			double radius = 1 + offsets[index];
			var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					radius * Math.cos(angles[index]), radius * Math.sin(angles[index]), policy, context);
			assertTrue(result.isEstablished(), result.getDiagnostic());
			assertEquals(FORWARD, result.getMethod());
			assertTrue(result.getForwardContinuationEvaluations() <= policy.getWorkBudget()
					.getMaximumRefinementIterations());
			// The point is the semantic image of its argument: on the unit circle, at the
			// projection of the request up to the interactive resolution.
			assertEquals(1, Math.hypot(point.getInhomX(), point.getInhomY()), 1E-9);
			assertEquals(angles[index], Math.atan2(point.getInhomY(), point.getInhomX()), 2E-3);
			assertEquals(component, address(point).getComponentLineageKey());
			assertSame(point, lookup("P"));
			assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point));
			assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
			assertEquals(point.getInhomX() + 1, dependent.getInhomX(), 1E-12);
			assertEquals(point.getInhomY() + 2, dependent.getInhomY(), 1E-12);
		}
		assertFalse(context.isStopped());
		// A distant request on the same component is reached by the same continuous descent,
		// never by a search: it is accepted only as a converged projection.
		var far = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(2.9), Math.sin(2.9), LocusPointInteractionPolicy2D.initial(3), context);
		assertTrue(far.isEstablished(), far.getDiagnostic());
		assertEquals(2.9, Math.atan2(point.getInhomY(), point.getInhomX()), 2E-3);
		assertEquals(component, address(point).getComponentLineageKey());
	}

	@Test void excludedEndOfTheComponentIsABarrierThatDoesNotEndTheGesture() {
		GeoLocusV2 source = nestedMetric();
		GeoPoint point = offered(source, parameterOf(2.9), "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var wide = LocusPointInteractionPolicy2D.initial(3);
		// Requests beyond the excluded end (-1, 0), on the continuation that the locus does
		// not have: the argument never leaves its component and never reaches the start.
		for (double angle : new double[] {3.05, 3.3, 3.6, -2.0}) {
			var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					Math.cos(angle), Math.sin(angle), wide, context);
			double polar = Math.atan2(point.getInhomY(), point.getInhomX());
			assertTrue(polar > 2.8 && polar < Math.PI, "status=" + result.getStatus() + " polar=" + polar);
			assertTrue(address(point).getCanonicalParameter() < 0);
			assertFalse(context.isStopped());
		}
		// The same gesture then moves back along the component.
		var back = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(2.5), Math.sin(2.5), wide, context);
		assertTrue(back.isEstablished(), back.getDiagnostic());
		assertEquals(2.5, Math.atan2(point.getInhomY(), point.getInhomX()), 2E-3);
	}

	@Test void illConditionedRequestIsRefusedWithoutMutationAndThenRecovers() {
		GeoLocusV2 source = nestedMetric();
		GeoPoint point = offered(source, parameterOf(1), "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var wide = LocusPointInteractionPolicy2D.initial(3);
		var old = address(point);
		String xml = getApp().getXML();
		// The centre of curvature: every argument is equally distant from the request.
		var centre = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point, 0, 0, wide, context);
		assertEquals(LocusPointContinuation2D.Status.NO_CONTINUATION_ESTABLISHED, centre.getStatus());
		assertTrue(centre.isRecoverableRefusal());
		assertEquals(old, address(point));
		assertEquals(xml, getApp().getXML());
		assertFalse(context.isStopped());
		var resumed = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(1.1), Math.sin(1.1), wide, context);
		assertTrue(resumed.isEstablished(), resumed.getDiagnostic());
	}

	@Test void forwardContinuationSpendsABoundedExplicitBudget() {
		GeoLocusV2 source = nestedMetric();
		GeoPoint point = offered(source, parameterOf(1), "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var old = address(point);
		String xml = getApp().getXML();
		var starved = new LocusPointInteractionPolicy2D(.5, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D(32768, 512, 2, 1024));
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(1.3), Math.sin(1.3), starved, context);
		assertEquals(LocusPointContinuation2D.Status.BUDGET_EXHAUSTED, result.getStatus());
		assertEquals(old, address(point));
		assertEquals(xml, getApp().getXML());
		assertFalse(context.isStopped());
	}

	@Test void exhaustedBudgetOnAChartedLocusRefusesWithoutMutationAndRecovers() {
		GeoLocusV2 source = meetCircle();
		GeoPoint point = offered(source, .1, "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		move(point, context, Math.cos(.11), Math.sin(.11));
		var old = address(point);
		String xml = getApp().getXML();
		var snapshot = getConstruction().getLastNativeTransactionMetrics();
		// Neither a chart nor the forward continuation fits one refinement iteration.
		var limited = new LocusPointInteractionPolicy2D(.5, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D(32768, 100, 1, 1024));
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(1.91), Math.sin(1.91), limited, context);
		assertEquals(LocusPointContinuation2D.Status.BUDGET_EXHAUSTED, result.getStatus());
		assertEquals(old, address(point));
		assertEquals(xml, getApp().getXML());
		assertSame(snapshot, getConstruction().getLastNativeTransactionMetrics());
		assertEquals(0, result.getDagPropagationNanos());
		assertFalse(context.isStopped());
		// The certified chart is established again by the next request of the gesture.
		move(point, context, Math.cos(.12), Math.sin(.12));
	}

	@Test void rootAtTheIncludedStartIsNotRepeatedAtTheExcludedEnd() {
		GeoLocusV2 source = nestedMetric();
		// The line meets the half circle only at its included start (1, 0), transversally.
		GeoLocusIntersectionResult result = intersect(source, "y=x-1");
		var solutions = result.getIntersectionResult().getFiniteSolutions();
		assertEquals(1, solutions.size(), () -> solutions.stream().map(solution -> solution
				.getRevisionEvidence().getSemanticParameter() + "@" + solution.getEvaluatedPoint().getX()
				+ "," + solution.getEvaluatedPoint().getY()).toList().toString());
		var root = solutions.get(0);
		assertEquals(0, root.getRevisionEvidence().getSemanticParameter(), 1E-9);
		assertEquals(DomainLocation.INCLUDED_ENDPOINT, root.getClassification().getDomainLocation());
		assertEquals(1, root.getEvaluatedPoint().getX(), 1E-9);
		assertEquals(0, root.getEvaluatedPoint().getY(), 1E-9);
	}

	@Test void argumentNeverReachesTheIncludedStartThroughTheExcludedEnd() {
		// The circle traced up to the polar angle 1.6 pi: its excluded end is the one-sided
		// limit at -0.4 pi, and the included start (1, 0) lies 0.4 pi beyond it.
		GeoLocusV2 source = nestedMetric("m/2.5");
		ContinuousComponent component = component(source);
		assertTrue(component.isArc() && component.isFullLength() && !component.isClosedPeriodic());
		// Binary64 proximity: the lifted neighbour of the excluded end canonicalizes onto the
		// parameter that the two ends share, which belongs to the component as its start.
		var provider = source.getSemanticDefinition().getProvider();
		double neighbour = Math.nextDown(component.getInterval().getUpper());
		assertEquals(component.getStart(), provider.canonicalize(neighbour));
		assertEquals(0, component.getArcCoordinate(provider.canonicalize(neighbour)));

		double end = -.4 * Math.PI;
		var wide = LocusPointInteractionPolicy2D.initial(1.5);
		GeoPoint point = offered(source, canonical((2 * Math.PI - 1.5) / .8), "P");
		assertEquals(-1.5, Math.atan2(point.getInhomY(), point.getInhomX()), 1E-6);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var old = address(point);
		String xml = getApp().getXML();
		// Requests in the gap, nearer to the start than to the end limit and than the point.
		for (double angle : new double[] {-.6, -.3, 0, .3}) {
			var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					Math.cos(angle), Math.sin(angle), wide, context);
			assertFalse(result.isEstablished(), "angle=" + angle + " parameter="
					+ address(point).getCanonicalParameter());
			assertTrue(result.isRecoverableRefusal(), result.getStatus().toString());
			assertEquals(old, address(point));
			assertEquals(xml, getApp().getXML());
			assertFalse(context.isStopped());
		}
		// The same gesture still moves inside the component, up to the neighbourhood of its end.
		var inside = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(end - .01), Math.sin(end - .01), wide, context);
		assertTrue(inside.isEstablished(), inside.getDiagnostic());
		assertEquals(end - .01, Math.atan2(point.getInhomY(), point.getInhomX()), 2E-3);
		assertTrue(address(point).getCanonicalParameter() < 0);
	}

	@Test void stationaryRequestNextToAnEndNeedsConditioningEvidence() {
		GeoLocusV2 source = nestedMetric();
		var wide = LocusPointInteractionPolicy2D.initial(3);
		// Next to the included start and next to the excluded end a two-sided reading does
		// not fit: the centre of curvature, and the far side of it where the argument is a
		// local maximum of the distance, are still refused.
		double[] angles = {.004, Math.PI - .004};
		for (int index = 0; index < angles.length; index++) {
			GeoPoint point = offered(source, parameterOf(index == 0 ? .05 : 3), "S" + index);
			var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
			// A well-conditioned request brings the point next to the end.
			assertTrue(LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					1.1 * Math.cos(angles[index]), 1.1 * Math.sin(angles[index]), policy, context)
					.isEstablished());
			assertEquals(angles[index], Math.atan2(point.getInhomY(), point.getInhomX()), 1E-3);
			var old = address(point);
			String xml = getApp().getXML();
			for (double radius : new double[] {0, -.5}) {
				var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
						radius * Math.cos(angles[index]), radius * Math.sin(angles[index]), wide, context);
				assertEquals(LocusPointContinuation2D.Status.NO_CONTINUATION_ESTABLISHED,
						result.getStatus(), "angle=" + angles[index] + " radius=" + radius);
				assertEquals(old, address(point));
				assertEquals(xml, getApp().getXML());
				assertFalse(context.isStopped());
			}
			// A well-conditioned request next to the same end is established there.
			double near = angles[index] < 1 ? .006 : Math.PI - .006;
			var resumed = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					1.2 * Math.cos(near), 1.2 * Math.sin(near), policy, context);
			assertTrue(resumed.isEstablished(), resumed.getDiagnostic());
			assertEquals(near, Math.atan2(point.getInhomY(), point.getInhomX()), 1E-3);
		}
	}

	@Test void includedStartIsReachedAsAConstrainedMinimizerAndTheExcludedEndIsNot() {
		GeoLocusV2 source = nestedMetric();
		var wide = LocusPointInteractionPolicy2D.initial(3);
		GeoPoint point = offered(source, parameterOf(.05), "P");
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		// Beyond the included start the nearest point of the component is the start itself.
		var start = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(-.1), Math.sin(-.1), wide, context);
		assertTrue(start.isEstablished(), start.getDiagnostic());
		assertEquals(0, address(point).getCanonicalParameter());
		assertEquals(1, point.getInhomX(), 1E-9);
		assertEquals(0, point.getInhomY(), 1E-9);
		// The gesture leaves the start again, and crosses the component.
		var away = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				Math.cos(.4), Math.sin(.4), wide, context);
		assertTrue(away.isEstablished(), away.getDiagnostic());
		assertEquals(.4, Math.atan2(point.getInhomY(), point.getInhomX()), 2E-3);
		for (double angle : new double[] {1, 1.8, 2.6, 3.0}) {
			assertTrue(LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					Math.cos(angle), Math.sin(angle), wide, context).isEstablished());
		}
		// Beyond the excluded end there is no nearest point of the component: the distance
		// has no minimum on it, and its last binary64 argument is not offered as one.
		var old = address(point);
		String xml = getApp().getXML();
		for (double angle : new double[] {3.3, 3.6, -2.0}) {
			var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
					Math.cos(angle), Math.sin(angle), wide, context);
			assertEquals(LocusPointContinuation2D.Status.NO_CONTINUATION_ESTABLISHED,
					result.getStatus(), "angle=" + angle);
			assertEquals(old, address(point));
			assertEquals(xml, getApp().getXML());
			assertFalse(context.isStopped());
		}
	}

	@Test void pairRootAtTheIncludedStartIsNotRepeatedAtTheExcludedEnd() {
		GeoLocusV2 source = nestedMetric();
		// A second locus through the included start (1, 0), transversally, and through
		// (0.6, 0.8): two intersections, each with one preimage on the half circle.
		add("c2=Circle((1,0.5),0.5)");
		add("F=Point(c2)");
		add("G=F+(0,0)");
		add("L2=LocusV2(G,F)");
		GeoLocusIntersectionResult result = add("R=Intersect(W,L2)");
		var solutions = result.getIntersectionResult().getFiniteSolutions();
		String listed = solutions.stream().map(solution -> solution.getRevisionEvidence()
				.getSemanticParameter() + "@" + solution.getEvaluatedPoint().getX() + ","
				+ solution.getEvaluatedPoint().getY()).toList().toString();
		assertEquals(2, solutions.size(), listed);
		assertNotNull(source);
	}

	@Test void transverseRootNextToTheExcludedEndKeepsItsOwnTangent() {
		GeoLocusV2 source = nestedMetric();
		// The line meets the half circle next to each end; next to the excluded end (-1, 0)
		// the tangent is read on the component's own side, never across to the start (1, 0).
		GeoLocusIntersectionResult result = intersect(source, "y=0.00000005");
		var root = result.getIntersectionResult().getFiniteSolutions().stream()
				.filter(solution -> solution.getEvaluatedPoint().getX() < 0).findFirst().orElseThrow();
		assertEquals(-1, root.getEvaluatedPoint().getX(), 1E-9);
		assertEquals(ContactClass.TRANSVERSE_ESTABLISHED, root.getClassification().getContactClass());
	}

	// ------------------------------------------------- hand-built minimal fixtures

	private static final String HAND = "maintenance.main";
	private static final LocusInterval2D TURN = new LocusInterval2D(-Math.PI, Math.PI, true, false);

	private static LocusBoundary2D end(LocusBoundary2D.Kind kind, String side, boolean included) {
		return new LocusBoundary2D(kind, new LocusBoundarySource2D("maintenance", "JUMP", side), included,
				LocusEvidenceNature2D.EXACT);
	}

	private static Coverage exists(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.CERTIFIED_EXISTS,
				LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE, LocusEvidenceNature2D.EXACT, "fixture-exists");
	}

	private GeoLocusV2 analytic(String identity, LocusDriverDomainProvider2D provider,
			LocusExistenceStructure2D structure, DoubleFunction<LocusPoint2D> image) {
		LocusBranch2D branch = new LocusBranch2D(HAND, structure.getCanonicalDomain(), structure,
				Orientation.INCREASING, "maintenance/v1", LocusLineage2D.unchanged(),
				provider.isPeriodic() ? EnumSet.of(BranchProperty.FINITE, BranchProperty.PERIODIC)
						: EnumSet.of(BranchProperty.FINITE), LocusQuality2D.analyticDoubleSemantic());
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2, getConstruction(), identity,
				new GeoNumeric(getConstruction(), 0), provider, List.of(branch),
				(source, current, parameter, session) -> image.apply(parameter), identity + "/v1");
		getConstruction().addToConstructionList(locus.getParentAlgorithm(), true);
		locus.setLabel("H" + getConstruction().getGeoSetConstructionOrder().size());
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		registry.registerParticipation(locus, new GeoIdentityRecord(registry.allocatePersistentGeoId(),
				"maintenance120.test", "LOCUS", "cedg.maintenance120.test", 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				ConstructionGeoRedefineProvider.STABLE_OUTPUT_ROLE, 1, 0, 0));
		return locus;
	}

	/**
	 * A full-length open arc with an excluded start and an included end at the shared
	 * parameter, traced as the segment from the one-sided limit (2 pi, 0) to the end (0, 0).
	 */
	private GeoLocusV2 openArc(String identity, LocusDriverDomainProvider2D provider, double start) {
		ContinuousComponent arc = ContinuousComponent.arc(TURN, start, start, true,
				end(LocusBoundary2D.Kind.STRUCTURAL_EVENT, "start", false),
				end(LocusBoundary2D.Kind.STRUCTURAL_EVENT, "end", true), Maximality.ESTABLISHED,
				LocusEvidenceNature2D.EXACT, "open/0");
		LocusExistenceStructure2D structure = new LocusExistenceStructure2D(HAND, TURN,
				DomainTopology.PERIODIC, 0, List.of(exists(TURN)), List.of(arc), Collections.emptyList(),
				Completeness.COMPLETE, "fixture-hybrid/v1");
		return analytic(identity, provider, structure, parameter -> new LocusPoint2D(
				2 * Math.PI - (parameter > start ? parameter - start : parameter - start + 2 * Math.PI), 0));
	}

	/** @return a canonical start near zero whose lifted end is itself and whose neighbours are it */
	private static double startNearZero(LocusDriverDomainProvider2D provider) {
		for (int multiple = 1; multiple < 64; multiple++) {
			double start = provider.canonicalize(multiple * 1E-10);
			double neighbour = Math.nextUp(start);
			boolean absorbed = provider.canonicalize(start) == start
					&& provider.canonicalize(start + 2 * Math.PI) == start;
			for (int step = 0; absorbed && step < 64; step++) {
				absorbed = provider.canonicalize(neighbour) == start;
				neighbour = Math.nextUp(neighbour);
			}
			if (absorbed) {
				return start;
			}
		}
		throw new AssertionError("No start whose 64 binary64 neighbours canonicalize onto it");
	}

	private LocusIntersectionResult2D meet(GeoLocusV2 locus, String target) {
		GeoElement line = add("target:" + target);
		return new AlgoLocusIntersectionV2(getConstruction(), locus, line, "maintenance/pair",
				"maintenance/lineage", "maintenance/target", "maintenance/topology").getResult()
				.getIntersectionResult();
	}

	@Test void sampleNextToAnExcludedEndIsSoughtBeyondSixtyFourNeighbours() {
		var provider = new ExplicitNumericDomainProvider2D("maintenance/parameter/v1", TURN,
				Orientation.INCREASING, true, 1E-14);
		// The canonicalization rounds with the magnitude of the domain: the 64 binary64
		// neighbours of this excluded start all canonicalize onto it, where it is the
		// included end of the component, on the other side.
		double start = startNearZero(provider);
		LocusIntersectionResult2D result = meet(openArc("maintenance-near-zero", provider, start), "x=0");
		// One intersection, at the included end (0, 0); its image is not read again as a
		// sample next to the excluded start, whose one-sided limit is (2 pi, 0).
		var solutions = result.getFiniteSolutions();
		assertEquals(1, solutions.size(), () -> solutions.stream().map(solution -> solution
				.getRevisionEvidence().getSemanticParameter() + "@" + solution.getEvaluatedPoint().getX())
				.toList().toString());
		assertEquals(0, solutions.get(0).getEvaluatedPoint().getX(), 1E-12);
		assertEquals(DomainLocation.INCLUDED_ENDPOINT,
				solutions.get(0).getClassification().getDomainLocation());
	}

	@Test void componentWithoutAnAdmissibleEndSampleIsNotExaminedAndNothingIsClaimed() {
		var exact = new ExplicitNumericDomainProvider2D("maintenance/coarse/v1", TURN,
				Orientation.INCREASING, true, 1E-14);
		double start = startNearZero(exact);
		// A canonicalization that cannot address the half of the component next to its
		// excluded start: every parameter there is the shared one, the included end.
		LocusDriverDomainProvider2D coarse = new LocusDriverDomainProvider2D() {
			@Override public String getProviderId() { return exact.getProviderId(); }
			@Override public String getParameterDescriptor() { return exact.getParameterDescriptor(); }
			@Override public LocusInterval2D getDeclaredDomain() { return exact.getDeclaredDomain(); }
			@Override public Orientation getOrientation() { return exact.getOrientation(); }
			@Override public boolean isPeriodic() { return true; }
			@Override public double getDomainEpsilon() { return exact.getDomainEpsilon(); }
			@Override public boolean contains(double canonical) { return exact.contains(canonical); }
			@Override public String getSemanticSignature() { return exact.getSemanticSignature() + "|coarse"; }
			@Override public double canonicalize(double parameter) {
				double canonical = exact.canonicalize(parameter);
				return canonical > start ? start : canonical;
			}
		};
		LocusIntersectionResult2D result = meet(openArc("maintenance-coarse", coarse, start), "x=0");
		// The search is exhausted. No sample is taken on a side that is not established: no
		// root is fabricated, and no absence of roots is claimed either.
		assertTrue(result.getFiniteSolutions().isEmpty());
		assertEquals(GeometryKind.UNRESOLVED, result.getGeometryKind());
		assertNotEquals(IntersectionSemanticMetadata2D.Completeness.COMPLETE,
				result.getCompletenessEvidence().getCompleteness());
		assertTrue(result.getCompletenessEvidence().getCoveredComponentKeys().isEmpty(),
				result.getCompletenessEvidence().getCoveredComponentKeys().toString());
		assertTrue(result.getDiagnostics().stream().anyMatch(diagnostic -> diagnostic.getCode()
				== DiagnosticCode.NUMERICAL_FAILURE && diagnostic.getMessage().contains("excluded end")),
				result.getDiagnostics().toString());
	}

	@Test void tangentNextToAnExcludedEndIsReadOnTheComponentsOwnSide() {
		var provider = new ExplicitNumericDomainProvider2D("maintenance/tangent/v1", TURN,
				Orientation.INCREASING, true, 1E-14);
		double start = startNearZero(provider);
		GeoLocusV2 locus = openArc("maintenance-tangent", provider, start);
		LocusInterval2D extent = locus.getSemanticDefinition().getBranch(HAND)
				.getCertifiedContinuousValidComponents().get(0);
		// Next to the excluded start the curve leaves (2 pi, 0) with derivative (-1, 0). A
		// difference read at the shared parameter would span the jump to the end (0, 0).
		double parameter = start + 1E-7;
		LocusDifferentialEvaluation2D[] read = new LocusDifferentialEvaluation2D[2];
		new AlgoLocusIntersectionV2(getConstruction(), locus, add("target:x=0"), "maintenance/pair",
				"maintenance/lineage", "maintenance/target", "maintenance/topology",
				G8BIntersectionFixtures.capability("maintenance-tangent/v1", context -> {
					read[0] = context.evaluateDifferential(HAND, parameter, extent);
					return G8BIntersectionFixtures.completeEmpty(context);
				}), new GeoElement[0]).getResult();
		GeoLocusV2 other = foldedSegment();
		new AlgoLocusLocusIntersectionV2(getConstruction(), locus, other, "maintenance/pair-lineage",
				"maintenance/pair-topology", G8C2IntersectionTestSupport.capability(
						"maintenance-pair-tangent/v1", context -> {
							read[1] = context.getFirstDefinition() == locus.getSemanticDefinition()
									? context.evaluateFirstDifferential(HAND, parameter, extent)
									: context.evaluateSecondDifferential(HAND, parameter, extent);
							return G8C2IntersectionTestSupport.completeEmpty(context);
						}), new GeoElement[0]).getResult();
		for (LocusDifferentialEvaluation2D differential : read) {
			assertNotNull(differential);
			assertEquals(Regularity.REGULAR, differential.getRegularity(), differential.toString());
			assertEquals(-1, differential.getDerivative().getX(), 1E-6);
			assertEquals(0, differential.getDerivative().getY(), 1E-6);
		}
	}

	/** The segment (u, 0) of a bounded domain, with a certified fold enclosure inside it. */
	private GeoLocusV2 foldedSegment() {
		LocusInterval2D domain = new LocusInterval2D(0, 4, true, true);
		LocusExistenceStructure2D structure = new LocusExistenceStructure2D(HAND, domain,
				DomainTopology.BOUNDED, 0, List.of(exists(domain)),
				List.of(ContinuousComponent.interval(domain,
						new LocusBoundary2D(LocusBoundary2D.Kind.DOMAIN_END,
								LocusBoundarySource2D.declaredLower(), true, LocusEvidenceNature2D.EXACT),
						new LocusBoundary2D(LocusBoundary2D.Kind.DOMAIN_END,
								LocusBoundarySource2D.declaredUpper(), true, LocusEvidenceNature2D.EXACT),
						Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, "segment/0")),
				Collections.emptyList(), Completeness.COMPLETE, "fixture-hybrid/v1")
				.withCertifiedFolds(List.of(new LocusCertifiedFold2D(2, 2.2, 1.9, 2.3, "fixture",
						LocusEvidenceNature2D.EXACT)));
		return analytic("maintenance-fold", new ExplicitNumericDomainProvider2D(
				"maintenance/bounded/v1", domain, Orientation.INCREASING, false, 1E-14), structure,
				parameter -> new LocusPoint2D(parameter, 0));
	}

	private LocusPointContinuation2D.Result continued(GeoLocusV2 locus, double parameter, double x,
			double y) {
		var definition = locus.getSemanticDefinition();
		var structure = definition.getBranch(HAND).getExistenceStructure();
		var address = new LocusSemanticAddress2D(locus.getPersistentLocusId(),
				definition.getProvider().getProviderId(), HAND, LocusComponentLineage2D.identityKey(
						structure, structure.getContinuousValidComponents().get(0)),
				parameter, 0, LocusSemanticAddress2D.SeamSide.NOT_PERIODIC);
		return LocusPointContinuation2D.resolve(locus, address, x, y, policy);
	}

	@Test void limitOfAnExcludedFoldIsNotAnEndAndAnIncludedEndIs() {
		GeoLocusV2 locus = foldedSegment();
		// Toward the fold the admissible interval stops next to its enclosure. That limit is
		// set by the exclusion, not by the component: the argument is not left there.
		for (double start : new double[] {1.5, 1.9, 2.5, 2.3}) {
			var toward = continued(locus, start, 2.1, .05);
			assertEquals(LocusPointContinuation2D.Status.NO_CONTINUATION_ESTABLISHED, toward.getStatus(),
					"start=" + start);
			assertNull(toward.getAddress());
		}
		// Away from the fold the same argument continues.
		var away = continued(locus, 1.5, 1.2, .05);
		assertTrue(away.isEstablished(), away.getDiagnostic());
		assertEquals(FORWARD, away.getMethod());
		assertEquals(1.2, away.getAddress().getCanonicalParameter(), 1E-3);
		// Beyond an end that the component includes, the end itself is the nearest point.
		var upper = continued(locus, 3.7, 4.3, .1);
		assertTrue(upper.isEstablished(), upper.getDiagnostic());
		assertEquals(4, upper.getAddress().getCanonicalParameter());
		var lower = continued(locus, .2, -.3, .1);
		assertTrue(lower.isEstablished(), lower.getDiagnostic());
		assertEquals(0, lower.getAddress().getCanonicalParameter());
		// At the included end a request back inside leaves it, and a request beyond keeps it.
		var back = continued(locus, 4, 3.8, .05);
		assertTrue(back.isEstablished(), back.getDiagnostic());
		assertEquals(3.8, back.getAddress().getCanonicalParameter(), 1E-3);
		var kept = continued(locus, 4, 4.2, .05);
		assertTrue(kept.isEstablished(), kept.getDiagnostic());
		assertEquals(4, kept.getAddress().getCanonicalParameter());
	}

	@Test void twoPreimagesOfOneImageStayDistinctChoicesAndKeepTheirSheet() {
		// A segment traced twice: every interior image has two semantic preimages.
		add("c=Circle((0,0),1)");
		add("D=Point(c)");
		add("g=Line(D,xAxis)");
		add("K=Intersect(g,yAxis)");
		GeoLocusV2 source = add("L=LocusV2(K,D)");
		GeoPoint point;
		try (var selection = LocusPointCreation2D.query(source, 0, .5,
				LocusPointInteractionPolicy2D.initial(.1))) {
			assertTrue(selection.getChoices().size() >= 2, selection.getFailure());
			var chosen = selection.getChoices().stream().min(Comparator.comparingDouble(
					choice -> Math.abs(choice.getAddress().getCanonicalParameter() - Math.PI / 6)))
					.orElseThrow();
			point = selection.commit("P", chosen);
		}
		double chosenParameter = address(point).getCanonicalParameter();
		assertEquals(Math.PI / 6, chosenParameter, 1E-6);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var wide = LocusPointInteractionPolicy2D.initial(.5);
		for (double y : new double[] {.6, .8, .3, -.2, -.6}) {
			var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point, 0.03, y, wide, context);
			assertTrue(result.isEstablished(), result.getDiagnostic());
			assertEquals(y, point.getInhomY(), 1E-3);
			// The other preimage of the same image is never selected by proximity.
			assertEquals(Math.asin(y), address(point).getCanonicalParameter(), 1E-3);
		}
	}
}
