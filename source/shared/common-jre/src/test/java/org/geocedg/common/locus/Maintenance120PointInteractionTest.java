/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geocedg.common.kernel.locus.interaction.LocusSnapshotPlacement2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DomainLocation;
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
		add("C=Circle((0,0),2)");
		add("E=Point(C)");
		add("Q=E+(0,0)");
		add("L=LocusV2(Q,E)");
		add("P0=Point(L,\"generator.main\",0)");
		add("m=LocusLength(L,P0,Q)");
		add("lengthAngle=m/4");
		add("T=Rotate((1,0),lengthAngle)");
		return add("W=LocusV2(T,E)");
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
