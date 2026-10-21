/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D;
import org.geocedg.common.export.ApproximationEvidence;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.SemanticCurveExportAdapter2D;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.Component;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.SourceResult;
import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.LocusPrincipalBranchSelector2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.LocusSimilarityEvaluator2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DomainLocation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionTokenLineage2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricQuery2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricRoute2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricRouteResolver2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionEvaluationStatus;
import org.geocedg.common.kernel.locus.metric.MetricRouteStatus;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.geocedg.common.kernel.locus.metric.TraversalOutcome;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T9: the consumers read the existence structure and its single closure
 * authority (consumption design sections 3 and 4; {@code locus-v2-existence-components.md}
 * sections 15.3, 15.5 and 15.8), on certified structures injected into the unit circle while
 * the compatibility certificate is still published: an arc across the seam (the first family
 * in a bite), a closed periodic component, an open arc of full length and point components
 * (V-CON-1 to V-CON-9, V-AVAIL-1, V-POINT-5, V-POINT-6). Step T11 reruns them on published
 * certificates.
 */
class PreG9BCu2ConsumerMigrationTest extends BaseUnitTest {
	private static final String BRANCH = "cu2-consumer.main";
	private static final LocusInterval2D CIRCLE = new LocusInterval2D(-Math.PI, Math.PI,
			true, false);
	private static final double PERIOD = 2 * Math.PI;
	private static final double START = 2.0;
	private static final double END = -2.0;
	private static final double TANGENCY = Math.atan2(-4, -3);
	/** One-sided limit at the excluded end of a structural event, away from the start. */
	private static final LocusPoint2D JUMP = new LocusPoint2D(2, 0);

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/** V-CON-1: drawing, export and routes close only a certified closed periodic arc. */
	@Test
	void theCertificateIsTheSingleClosureAuthority() {
		GeoLocusV2 closed = locus("cu2-closed", closed());
		GeoLocusV2 bite = locus("cu2-bite", bite(START, END));
		GeoLocusV2 open = locus("cu2-open-full", openFullLength());

		LocusRenderData2D closedDrawing = render(closed);
		assertEquals(1, subpaths(closedDrawing));
		assertEquals(0, distance(first(closedDrawing), last(closedDrawing)), 1e-12,
				"the closed component reaches its closing point");
		LocusRenderData2D biteDrawing = render(bite);
		assertEquals(1, subpaths(biteDrawing), "the arc is one stroke across the seam");
		assertEquals(0, distance(first(biteDrawing), circle(START)), 1e-12);
		assertEquals(0, distance(last(biteDrawing), circle(END)), 1e-12);
		LocusRenderData2D openDrawing = render(open);
		assertTrue(openDrawing.getVertices().stream().noneMatch(vertex ->
				vertex.getSemanticParameter() == -Math.PI + PERIOD),
				"the excluded end is never drawn as the image of its canonical parameter");

		assertTrue(polyline(export(closed)).isClosed());
		assertFalse(polyline(export(bite)).isClosed());
		SourceResult openExport = adapt(open);
		assertTrue(openExport.getComponents().stream().noneMatch(component ->
				component.isEmitted() && polyline(component).isClosed()));

		assertEquals(MetricRouteStatus.RESOLVED, route(definition("cu2-closed-route",
				closed()), 1, 1, TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.FULL_CYCLE).getRouteStatus());
		assertEquals(MetricRouteStatus.INVALID_QUERY, route(definition("cu2-bite-route",
				bite(START, END)), 3, 3, TraversalDirection.FORWARD,
				OpenBoundaryPolicy.STRICT, SamePositionPolicy.FULL_CYCLE).getRouteStatus());
		assertEquals(MetricRouteStatus.INVALID_QUERY, route(definition("cu2-open-route",
				openFullLength()), 1, 1, TraversalDirection.FORWARD,
				OpenBoundaryPolicy.STRICT, SamePositionPolicy.FULL_CYCLE).getRouteStatus());

		assertEquals(DomainLocation.PERIODIC_SEAM, seamRootLocation(closed()));
		assertEquals(DomainLocation.INTERIOR, seamRootLocation(bite(START, END)),
				"no seam quarantine on an arc");
	}

	/** V-CON-2: a route inside an arc follows it across the seam and never wraps. */
	@Test
	void routesFollowAnArcAcrossTheSeam() {
		LocusDefinition2D definition = definition("cu2-bite-routes", bite(START, END));
		LocusMetricRoute2D forward = route(definition, 2.5, -2.5,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(MetricRouteStatus.RESOLVED, forward.getRouteStatus());
		assertEquals(1, forward.getOrderedRouteSegments().size());
		assertEquals(2.5, forward.getOrderedRouteSegments().get(0)
				.getStartCanonicalParameter());
		assertEquals(-2.5 + PERIOD, forward.getOrderedRouteSegments().get(0)
				.getEndCanonicalParameter());
		assertFalse(forward.isWrapped());
		assertTrue(forward.isGeometricallyConnected());
		assertEquals(PERIOD - 5, length(definition, 2.5, -2.5,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT), 1e-12);

		LocusMetricRoute2D reverse = route(definition, -2.5, 2.5,
				TraversalDirection.REVERSE, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(MetricRouteStatus.RESOLVED, reverse.getRouteStatus());
		assertFalse(reverse.isWrapped());

		assertEquals(MetricRouteStatus.TARGET_NOT_REACHABLE, route(definition, -2.5, 2.5,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH).getRouteStatus());
		LocusMetricRoute2D wrap = route(definition, -2.5, 2.5,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.WRAP_TO_START,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(MetricRouteStatus.TARGET_NOT_REACHABLE, wrap.getRouteStatus());
		assertTrue(wrap.getDiagnostics().stream().anyMatch(diagnostic ->
				diagnostic.getCode() == MetricDiagnosticCode2D.TOPOLOGY_NOT_ESTABLISHED));
		LocusMetricRoute2D stop = route(definition, -2.5, 2.5,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STOP_AT_END,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(TraversalOutcome.STOPPED_AT_BOUNDARY, stop.getTraversalOutcome());
		assertEquals(END + PERIOD, stop.getOrderedRouteSegments().get(0)
				.getEndCanonicalParameter());
		LocusMetricRoute2D cycle = route(definition, -2.5, -2.5,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.FULL_CYCLE);
		assertEquals(MetricRouteStatus.INVALID_QUERY, cycle.getRouteStatus());
	}

	/** V-CON-3: the total length of an arc is its length; of a closed component, a cycle. */
	@Test
	void theTotalLengthFollowsTheComponents() {
		assertEquals(PERIOD - 4, total(definition("cu2-bite-total", bite(START, END))),
				1e-12);
		assertEquals(PERIOD, total(definition("cu2-closed-total", closed())), 1e-12);
	}

	/**
	 * V-CON-4: roots on both sides of the seam belong to the arc, with its phase frame, no seam
	 * quarantine and a token lineage that survives a boundary that moves with its source.
	 */
	@Test
	void intersectionsUseTheArcAndItsStructuralLineage() {
		double root = Math.acos(-0.8);
		LocusIntersectionResult2D first = intersect("cu2-bite-x", bite(START, END),
				"x=-0.8", root, -root);
		assertEquals(GeometryKind.FINITE, first.getGeometryKind());
		assertEquals(2, first.getFiniteSolutions().size(),
				"roots on both sides of the seam belong to the arc");
		for (LocusIntersectionSolution2D solution : first.getFiniteSolutions()) {
			assertEquals(DomainLocation.INTERIOR,
					solution.getClassification().getDomainLocation());
		}
		// Token material is the structural lineage of the component.
		LocusExistenceStructure2D before = bite(START, END);
		LocusExistenceStructure2D after = bite(2.1, -2.1);
		IntersectionTokenLineage2D beforeLineage = IntersectionTokenLineage2D
				.forComponentLineage(LocusComponentLineage2D.of(before,
						before.getContinuousValidComponents().get(0)), Optional.of("root"));
		IntersectionTokenLineage2D afterLineage = IntersectionTokenLineage2D
				.forComponentLineage(LocusComponentLineage2D.of(after,
						after.getContinuousValidComponents().get(0)), Optional.of("root"));
		assertEquals(beforeLineage.getSolutionLineageKey(),
				afterLineage.getSolutionLineageKey(),
				"moving a boundary without changing its source keeps the token lineage");
		assertTrue(beforeLineage.getEstablishedBranchLineage().contains(
				"bite-J/discriminant/start"));

		LocusIntersectionResult2D local = intersect("cu2-band-y", localBand(), "y=5");
		assertNotEquals(GeometryKind.EMPTY, local.getGeometryKind(),
				"EMPTY needs a complete inventory");
	}

	/**
	 * V-CON-5 and V-AVAIL-1: a position on the arc, at the seam inside it and in certified
	 * nonexistence; the evaluator's point there is not published as valid.
	 */
	@Test
	void positionsResolveOnTheArcAndCertifiedNonexistenceIsNeverValid() {
		LocusDefinition2D definition = definition("cu2-bite-points", bite(START, END));
		assertEquals(MetricPositionEvaluationStatus.VALID, bind(definition, 3.0)
				.getEvaluationStatus());
		assertEquals(MetricPositionEvaluationStatus.VALID, bind(definition, -Math.PI)
				.getEvaluationStatus(), "the seam is interior to the arc");
		MetricPositionBinding2D gap = bind(definition, 0);
		assertEquals(MetricPositionEvaluationStatus.POSITION_OUTSIDE_DOMAIN,
				gap.getEvaluationStatus());

		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
			LocusEvaluation2D inside = definition.evaluate(BRANCH, -2.5 + PERIOD, session);
			assertTrue(inside.isValid(), "a lifted parameter of the arc is evaluated");
			LocusEvaluation2D nonexistent = definition.evaluate(BRANCH, 0, session);
			assertEquals(EvaluationStatus.DEPENDENCY_UNDEFINED, nonexistent.getStatus());
			assertTrue(nonexistent.getDiagnostic().startsWith(
					LocusDefinition2D.CERTIFIED_NONEXISTENCE));
			assertTrue(nonexistent.getDiagnostic().contains(
					LocusDefinition2D.EVALUATOR_DISAGREEMENT));
		}

		LocusDefinition2D unresolved = definition("cu2-band", localBand());
		assertEquals(MetricPositionEvaluationStatus.POSITION_OUTSIDE_DOMAIN,
				bind(unresolved, 2.5).getEvaluationStatus(),
				"a position in an unresolved band is not addressed");
		assertEquals(MetricPositionEvaluationStatus.VALID,
				bind(unresolved, 0.5).getEvaluationStatus());
	}

	/**
	 * V-CON-6 on the published certificates (step T11): a dependent locus on a locus support
	 * takes the support's components, an arc across the seam included, as its declared
	 * domain, and its completeness never exceeds the support's. A locus support is outside
	 * class v3, so the dependent's own existence is not established: no compatibility
	 * certificate composes the support's components into it any more.
	 */
	@Test
	void aDependentOnALocusSupportInheritsTheArc() {
		add("c=Circle((0,0),1)");
		GeoPoint driver = add("P=Point(c)");
		driver.setCoords(1, 0, 1);
		driver.updateRepaint();
		add("d=Circle((2,0),3/2)");
		add("g=Line((0,0),P)");
		add("X=Intersect(d,g,1)");
		GeoLocusV2 support = add("L=LocusV2(X,P)");
		LocusExistenceStructure2D origin = support.getSemanticDefinition()
				.getBranch(SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY)
				.getExistenceStructure();
		assertEquals(Completeness.COMPLETE, origin.getCompleteness(),
				origin.getSemanticSignature());
		assertTrue(origin.getContinuousValidComponents().stream()
				.anyMatch(ContinuousComponent::crossesSeam), origin.getSemanticSignature());
		add("u=0");
		add("S=Point(L,\"" + SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY + "\",u)");
		add("Y=S+(1,0)");
		GeoLocusV2 dependent = add("M=LocusV2(Y,S)");
		List<LocusInterval2D> pieces = new ArrayList<>();
		for (ContinuousComponent component : origin.getContinuousValidComponents()) {
			pieces.addAll(component.getCanonicalPieces());
		}
		// The declared domain lists the pieces in canonical order of their lower ends.
		pieces.sort(java.util.Comparator.comparingDouble(LocusInterval2D::getLower));
		assertEquals(pieces, ((AlgoDependentPointLocusV2) dependent.getParentAlgorithm())
				.getGeneratorDescriptor().getValidComponents());
		LocusExistenceStructure2D structure = dependent.getSemanticDefinition()
				.getBranch(SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY)
				.getExistenceStructure();
		assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness());
		assertTrue(structure.getContinuousValidComponents().isEmpty());
		assertEquals(LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS,
				structure.getCoverage().get(0).getCause());
	}

	/**
	 * V-CON-7: a similarity image shares the branches of its source, as
	 * {@code AlgoLocusSimilarityTransform2D} publishes them, and the limit at an excluded end
	 * of an image component is the image of the limit of the source. The public command
	 * needs construction identities that injected structures lack; step T11 reruns the
	 * command on published certificates.
	 */
	@Test
	void aSimilarityImageComposesTheStructureAndTheOneSidedLimits() {
		LocusDefinition2D source = limitDefinition("cu2-open-source", openFullLength());
		LocusSimilarityEvaluator2D evaluator = new LocusSimilarityEvaluator2D(source,
				LocusSimilarityTransform2D.translation(1, 0));
		LocusDefinition2D image = new LocusDefinition2D("cu2-open-image", 1,
				DefinitionStatus.VALID, provider(), source.getBranches(), evaluator,
				Determinism.POINTWISE_DETERMINISTIC, evaluator.getEvaluatorSignature(),
				new LocusInstrumentation2D());
		LocusExistenceStructure2D structure = image.getBranch(BRANCH)
				.getExistenceStructure();
		assertEquals(source.getBranch(BRANCH).getExistenceStructure()
				.getSemanticSignature(), structure.getSemanticSignature());
		assertFalse(structure.isClosedPeriodic());
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
			LocusEvaluation2D limit = image.evaluateComponentEnd(BRANCH, -Math.PI, true,
					session);
			assertTrue(limit.isValid());
			assertEquals(JUMP.getX() + 1, limit.getPoint().getX(), 0);
			assertEquals(JUMP.getY(), limit.getPoint().getY(), 0);
			LocusEvaluation2D start = image.evaluateComponentEnd(BRANCH, -Math.PI, false,
					session);
			assertEquals(circle(-Math.PI).getX() + 1, start.getPoint().getX(), 0,
					"the included start is the image of its canonical parameter");
		}
	}

	/**
	 * An excluded end of a structural event is reached only through the explicit one-sided
	 * limit query; canonicalization never stands in for it (P2, P5).
	 */
	@Test
	void anExcludedEventEndIsReachedOnlyAsAOneSidedLimit() {
		LocusDefinition2D definition = limitDefinition("cu2-open-limit", openFullLength());
		LocusInterval2D extent = definition.getBranch(BRANCH).getExistenceStructure()
				.getContinuousValidComponents().get(0).getInterval();
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
			assertEquals(JUMP, definition.evaluateInComponent(BRANCH, extent,
					extent.getUpper(), session).getPoint());
			assertEquals(circle(-Math.PI), definition.evaluateInComponent(BRANCH, extent,
					extent.getLower(), session).getPoint());
			assertEquals(circle(-Math.PI), definition.evaluate(BRANCH, -Math.PI, session)
					.getPoint(), "the shared canonical parameter is the included start");
		}
		LocusDefinition2D without = definition("cu2-open-no-limit", openFullLength());
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
			LocusEvaluation2D failed = without.evaluateComponentEnd(BRANCH, -Math.PI, true,
					session);
			assertEquals(EvaluationStatus.EVALUATION_FAILED, failed.getStatus());
			assertTrue(failed.getDiagnostic().startsWith(
					LocusEvaluator2D.ONE_SIDED_LIMIT_NOT_AVAILABLE));
		}
	}

	/**
	 * V-CON-9: the principal branch selector operates on a local certified component and
	 * never addresses a point component through a binary64 parameter.
	 */
	@Test
	void thePrincipalSelectorUsesTheLocalCertifiedComponents() {
		GeoLocusV2 local = registeredLocus("cu2-principal", "Lprincipal", localBand());
		LocusExistenceStructure2D structure = local.getSemanticDefinition()
				.getBranch(BRANCH).getExistenceStructure();
		LocusSemanticAddress2D address = LocusPrincipalBranchSelector2D.select(local, 0.5);
		assertEquals(0.5, address.getCanonicalParameter());
		assertEquals(LocusComponentLineage2D.of(structure,
				structure.getContinuousValidComponents().get(0)),
				address.getComponentLineageKey());
		IllegalArgumentException band = assertThrows(IllegalArgumentException.class,
				() -> LocusPrincipalBranchSelector2D.select(local, 2.5));
		assertEquals("No eligible semantic component", band.getMessage());

		GeoLocusV2 point = registeredLocus("cu2-principal-point", "Lpoint", pointOnly());
		IllegalArgumentException isolated = assertThrows(IllegalArgumentException.class,
				() -> LocusPrincipalBranchSelector2D.select(point, TANGENCY));
		assertTrue(isolated.getMessage().contains("point component"));
	}

	/** V-CON-8: a global boundary is never claimed without a complete inventory. */
	@Test
	void anOpenBoundaryNeedsACompleteInventory() {
		LocusDefinition2D local = boundedDefinition("cu2-local-boundary", Completeness
				.NOT_ESTABLISHED);
		LocusMetricRoute2D stop = route(local, -2.5, -1.5, TraversalDirection.REVERSE,
				OpenBoundaryPolicy.STOP_AT_END, SamePositionPolicy.ZERO_LENGTH);
		assertNotEquals(MetricRouteStatus.RESOLVED, stop.getRouteStatus(),
				"a local first component is not the global start");
		assertTrue(stop.getDiagnostics().stream().anyMatch(diagnostic ->
				diagnostic.getCode() == MetricDiagnosticCode2D.TOPOLOGY_NOT_ESTABLISHED));
		LocusDefinition2D complete = boundedDefinition("cu2-complete-boundary",
				Completeness.COMPLETE);
		LocusMetricRoute2D stopComplete = route(complete, -2.5, -1.5,
				TraversalDirection.REVERSE, OpenBoundaryPolicy.STOP_AT_END,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(MetricRouteStatus.RESOLVED, stopComplete.getRouteStatus());
		assertEquals(TraversalOutcome.STOPPED_AT_BOUNDARY,
				stopComplete.getTraversalOutcome());
		LocusMetricRoute2D gap = route(complete, -2.5, -2.8,
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STOP_AT_END,
				SamePositionPolicy.ZERO_LENGTH);
		assertEquals(MetricRouteStatus.DISCONTINUITY_ENCOUNTERED, gap.getRouteStatus(),
				"an internal gap is never crossed");
	}

	/**
	 * V-POINT-5 and V-POINT-6: a point component contributes zero, is never addressed by a
	 * binary64 parameter, is never drawn and never lets an intersection claim emptiness.
	 */
	@Test
	void pointComponentsAreDeclaredAndNeverAddressed() {
		LocusDefinition2D definition = definition("cu2-point", pointOnly());
		assertEquals(0, total(definition), 0);
		MetricPositionBinding2D near = bind(definition, TANGENCY);
		assertEquals(MetricPositionEvaluationStatus.POSITION_OUTSIDE_DOMAIN,
				near.getEvaluationStatus());
		assertTrue(near.getDiagnostics().stream().anyMatch(diagnostic ->
				diagnostic.getCode() == MetricDiagnosticCode2D.ISOLATED_POINT));
		assertTrue(render(locus("cu2-point-render", pointOnly())).getVertices().isEmpty());

		LocusIntersectionResult2D result = intersect("cu2-point-x", pointOnly(), "y=5");
		assertNotEquals(GeometryKind.EMPTY, result.getGeometryKind(),
				"an unexcluded point component forbids emptiness");
	}

	// ---------------------------------------------------------------- fixtures

	private static LocusPoint2D circle(double parameter) {
		return new LocusPoint2D(Math.cos(parameter), Math.sin(parameter));
	}

	private static ExplicitNumericDomainProvider2D provider() {
		return new ExplicitNumericDomainProvider2D("cu2-consumer/parameter/v1", CIRCLE,
				Orientation.INCREASING, true, 1e-14);
	}

	private static LocusBranch2D branch(LocusExistenceStructure2D structure) {
		return new LocusBranch2D(BRANCH, structure.getCanonicalDomain(), structure,
				Orientation.INCREASING, "cu2-consumer/v1", LocusLineage2D.unchanged(),
				EnumSet.of(BranchProperty.FINITE, BranchProperty.PERIODIC),
				LocusQuality2D.analyticDoubleSemantic());
	}

	private static LocusDefinition2D definition(String identity,
			LocusExistenceStructure2D structure) {
		return new LocusDefinition2D(identity, 1, DefinitionStatus.VALID, provider(),
				List.of(branch(structure)), (semantic, branch, parameter, session) ->
						LocusEvaluation2D.valid(circle(parameter), Regularity.REGULAR,
								LocusQuality2D.analyticDoubleSemantic()),
				Determinism.POINTWISE_DETERMINISTIC, identity + "/v1",
				new LocusInstrumentation2D());
	}

	/** The circle, with a one-sided limit capability at excluded ends: the point JUMP. */
	private static LocusDefinition2D limitDefinition(String identity,
			LocusExistenceStructure2D structure) {
		LocusEvaluator2D evaluator = new LocusEvaluator2D() {
			@Override
			public LocusEvaluation2D evaluate(LocusDefinition2D definition,
					LocusBranch2D branch, double parameter, LocusEvaluationSession2D session) {
				return LocusEvaluation2D.valid(circle(parameter), Regularity.REGULAR,
						LocusQuality2D.analyticDoubleSemantic());
			}

			@Override
			public LocusEvaluation2D evaluateOneSidedLimit(LocusDefinition2D definition,
					LocusBranch2D branch, ContinuousComponent component, boolean upperEnd,
					LocusEvaluationSession2D session) {
				return LocusEvaluation2D.valid(JUMP, Regularity.REGULAR,
						LocusQuality2D.analyticDoubleSemantic());
			}
		};
		return new LocusDefinition2D(identity, 1, DefinitionStatus.VALID, provider(),
				List.of(branch(structure)), evaluator, Determinism.POINTWISE_DETERMINISTIC,
				identity + "/v1", new LocusInstrumentation2D());
	}

	/**
	 * A labeled injected fixture with the durable identity that semantic addresses cite.
	 */
	private GeoLocusV2 registeredLocus(String identity, String label,
			LocusExistenceStructure2D structure) {
		GeoNumeric driver = new GeoNumeric(getConstruction(), 0);
		driver.setLabel(label + "Driver");
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2, getConstruction(),
				identity, driver, provider(), List.of(branch(structure)),
				(source, branch, parameter, session) -> circle(parameter), identity + "/v1");
		getConstruction().addToConstructionList(locus.getParentAlgorithm(), true);
		locus.setLabel(label);
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		registry.registerParticipation(locus, new GeoIdentityRecord(
				registry.allocatePersistentGeoId(), "pre-g9b-cu2.consumer.test", "LOCUS",
				"cedg.pre-g9b-cu2.test", 1, EditAuthorityMode.PROJECTION_DEFINED,
				ProjectionBindingRole.DEFINING,
				ConstructionGeoRedefineProvider.STABLE_OUTPUT_ROLE, 1, 0, 0));
		return locus;
	}

	private GeoLocusV2 locus(String identity, LocusExistenceStructure2D structure) {
		return LocusV2Factory.createAnalytic(LocusV2Mode.V2, getConstruction(), identity,
				new GeoNumeric(getConstruction(), 0), provider(), List.of(branch(structure)),
				(source, branch, parameter, session) -> circle(parameter), identity + "/v1");
	}

	/** The first family in a bite: one arc across the seam between two transitions. */
	private static LocusExistenceStructure2D bite(double start, double end) {
		ContinuousComponent arc = ContinuousComponent.arc(CIRCLE, start, end, false,
				transition("start"), transition("end"), Maximality.ESTABLISHED,
				LocusEvidenceNature2D.EXACT, "bite/0");
		return periodic(List.of(exists(new LocusInterval2D(-Math.PI, end, true, true)),
				absent(new LocusInterval2D(end, start, false, false)),
				exists(new LocusInterval2D(start, Math.PI, true, false))), List.of(arc),
				Collections.emptyList(), Completeness.COMPLETE);
	}

	private static LocusExistenceStructure2D closed() {
		LocusBoundary2D closure = new LocusBoundary2D(LocusBoundary2D.Kind.PERIODIC_CLOSURE,
				new LocusBoundarySource2D("hybrid-certifier", "periodic-closure", "seam"),
				true, LocusEvidenceNature2D.EXACT);
		return periodic(List.of(exists(CIRCLE)), List.of(ContinuousComponent.arc(CIRCLE,
				-Math.PI, -Math.PI, true, closure, closure, Maximality.ESTABLISHED,
				LocusEvidenceNature2D.EXACT, "closed/0")), Collections.emptyList(),
				Completeness.COMPLETE);
	}

	/** An open arc of full length at the seam, cut by a structural event. */
	private static LocusExistenceStructure2D openFullLength() {
		LocusBoundary2D start = new LocusBoundary2D(LocusBoundary2D.Kind.STRUCTURAL_EVENT,
				new LocusBoundarySource2D("w", "ORIGIN_WRAP", "start"), true,
				LocusEvidenceNature2D.EXACT);
		LocusBoundary2D end = new LocusBoundary2D(LocusBoundary2D.Kind.STRUCTURAL_EVENT,
				new LocusBoundarySource2D("w", "ORIGIN_WRAP", "end"), false,
				LocusEvidenceNature2D.EXACT);
		return periodic(List.of(exists(CIRCLE)), List.of(ContinuousComponent.arc(CIRCLE,
				-Math.PI, -Math.PI, true, start, end, Maximality.ESTABLISHED,
				LocusEvidenceNature2D.EXACT, "open/0")), Collections.emptyList(),
				Completeness.COMPLETE);
	}

	/** A local component in a structure whose other parameters are unresolved. */
	private static LocusExistenceStructure2D localBand() {
		LocusInterval2D local = new LocusInterval2D(-1, 1, true, true);
		LocusBoundary2D lower = new LocusBoundary2D(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
				new LocusBoundarySource2D("band", "unresolved", "lower"), true,
				LocusEvidenceNature2D.NONE);
		LocusBoundary2D upper = new LocusBoundary2D(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
				new LocusBoundarySource2D("band", "unresolved", "upper"), true,
				LocusEvidenceNature2D.NONE);
		return periodic(List.of(unresolved(new LocusInterval2D(-Math.PI, -1, true, false)),
				exists(local), unresolved(new LocusInterval2D(1, Math.PI, false, false))),
				List.of(ContinuousComponent.interval(local, lower, upper,
						Maximality.LOCAL_CERTIFIED_SUBINTERVAL,
						LocusEvidenceNature2D.RIGOROUS_INTERVAL, "local/0")),
				Collections.emptyList(), Completeness.NOT_ESTABLISHED);
	}

	/** Existence reduced to the exterior tangency {@code t* = atan2(-4, -3)} (V-POINT-1). */
	private static LocusExistenceStructure2D pointOnly() {
		LocusPointComponent2D point = new LocusPointComponent2D(Math.nextDown(TANGENCY),
				Math.nextUp(TANGENCY), new LocusBoundarySource2D("line-circle",
						"discriminant", "even-root-1"), Maximality.ESTABLISHED,
				LocusEvidenceNature2D.EXACT, "tangency/0", null);
		return periodic(List.of(
				absent(new LocusInterval2D(-Math.PI, Math.nextDown(TANGENCY), true, false)),
				isolated(new LocusInterval2D(Math.nextDown(TANGENCY), Math.nextUp(TANGENCY),
						true, true)),
				absent(new LocusInterval2D(Math.nextUp(TANGENCY), Math.PI, false, false))),
				Collections.emptyList(), List.of(point), Completeness.COMPLETE);
	}

	private static LocusExistenceStructure2D periodic(List<Coverage> coverage,
			List<ContinuousComponent> components, List<LocusPointComponent2D> points,
			Completeness completeness) {
		return new LocusExistenceStructure2D(BRANCH, CIRCLE, DomainTopology.PERIODIC, 0,
				coverage, components, points, completeness, "fixture-hybrid/v1");
	}

	/** Two intervals of a bounded domain, from a complete or a local inventory. */
	private static LocusDefinition2D boundedDefinition(String identity,
			Completeness completeness) {
		LocusInterval2D domain = new LocusInterval2D(-3, 3, true, true);
		LocusInterval2D left = new LocusInterval2D(-3, -1, true, true);
		LocusInterval2D right = new LocusInterval2D(1, 3, true, true);
		boolean complete = completeness == Completeness.COMPLETE;
		LocusExistenceStructure2D structure = new LocusExistenceStructure2D(BRANCH, domain,
				DomainTopology.BOUNDED, 0, List.of(exists(left),
						complete ? absent(new LocusInterval2D(-1, 1, false, false))
								: unresolved(new LocusInterval2D(-1, 1, false, false)),
						exists(right)),
				List.of(ContinuousComponent.interval(left, domainEnd("lower"),
								transitionAt("left-upper"), complete ? Maximality.ESTABLISHED
										: Maximality.LOCAL_CERTIFIED_SUBINTERVAL,
								LocusEvidenceNature2D.EXACT, "left/0"),
						ContinuousComponent.interval(right, transitionAt("right-lower"),
								domainEnd("upper"), complete ? Maximality.ESTABLISHED
										: Maximality.LOCAL_CERTIFIED_SUBINTERVAL,
								LocusEvidenceNature2D.EXACT, "right/0")),
				Collections.emptyList(), completeness, "fixture-hybrid/v1");
		ExplicitNumericDomainProvider2D provider = new ExplicitNumericDomainProvider2D(
				"cu2-consumer/bounded/v1", domain, Orientation.INCREASING, false, 1e-14);
		LocusBranch2D branch = new LocusBranch2D(BRANCH, domain, structure,
				Orientation.INCREASING, "cu2-consumer/v1", LocusLineage2D.unchanged(),
				EnumSet.of(BranchProperty.FINITE), LocusQuality2D.analyticDoubleSemantic());
		return new LocusDefinition2D(identity, 1, DefinitionStatus.VALID, provider,
				List.of(branch), (semantic, current, parameter, session) ->
						LocusEvaluation2D.valid(new LocusPoint2D(parameter, 0),
								Regularity.REGULAR, LocusQuality2D.analyticDoubleSemantic()),
				Determinism.POINTWISE_DETERMINISTIC, identity + "/v1",
				new LocusInstrumentation2D());
	}

	private static LocusBoundary2D transition(String side) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.CERTIFIED_TRANSITION,
				new LocusBoundarySource2D("bite-J", "discriminant", side), true,
				LocusEvidenceNature2D.EXACT);
	}

	private static LocusBoundary2D transitionAt(String side) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.CERTIFIED_TRANSITION,
				new LocusBoundarySource2D("bounded-J", "predicate", side), true,
				LocusEvidenceNature2D.EXACT);
	}

	private static LocusBoundary2D domainEnd(String side) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.DOMAIN_END, "lower".equals(side)
				? LocusBoundarySource2D.declaredLower() : LocusBoundarySource2D.declaredUpper(),
				true, LocusEvidenceNature2D.EXACT);
	}

	private static Coverage exists(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.CERTIFIED_EXISTS,
				LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE, LocusEvidenceNature2D.EXACT,
				"fixture-exists");
	}

	private static Coverage absent(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.CERTIFIED_DOES_NOT_EXIST,
				LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE, LocusEvidenceNature2D.EXACT,
				"fixture-absent");
	}

	private static Coverage unresolved(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.UNRESOLVED,
				LocusCoverageCause2D.NOT_CERTIFIED, LocusEvidenceNature2D.NONE,
				"fixture-unresolved");
	}

	private static Coverage isolated(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.CERTIFIED_ISOLATED_POINTS,
				LocusCoverageCause2D.ISOLATED_EVEN_ROOT, LocusEvidenceNature2D.EXACT,
				"fixture-isolated");
	}

	// ---------------------------------------------------------------- consumers

	private static LocusRenderData2D render(GeoLocusV2 locus) {
		return new LocusRenderCache2D().getOrBuild(locus,
				LocusRenderPolicy2D.adaptive(1, 800, 600, 100, 100, 32, 0.75, 12));
	}

	private static long subpaths(LocusRenderData2D drawing) {
		return drawing.getVertices().stream()
				.filter(LocusRenderData2D.Vertex::startsSubpath).count();
	}

	private static LocusPoint2D first(LocusRenderData2D drawing) {
		return drawing.getVertices().get(0).getPoint();
	}

	private static LocusPoint2D last(LocusRenderData2D drawing) {
		return drawing.getVertices().get(drawing.getVertices().size() - 1).getPoint();
	}

	private static double distance(LocusPoint2D first, LocusPoint2D second) {
		return Math.hypot(first.getX() - second.getX(), first.getY() - second.getY());
	}

	private static SourceResult adapt(GeoLocusV2 locus) {
		GeometryExportRequest request = GeometryExportRequest.builder(0.01)
				.allowedGuarantees(EnumSet.of(ApproximationEvidence.Guarantee
						.ESTIMATED_ERROR)).build();
		return new SemanticCurveExportAdapter2D().adaptLocus(locus, 0, request,
				new AdaptiveCurveApproximationBuilder2D.WorkLedger(request), "cu2:",
				SemanticCurveExportAdapter2D.ALL_COMPONENTS);
	}

	private static Component export(GeoLocusV2 locus) {
		SourceResult result = adapt(locus);
		assertEquals(1, result.getComponents().size());
		assertTrue(result.getComponents().get(0).isEmitted());
		return result.getComponents().get(0);
	}

	private static PolylineGeometry polyline(Component component) {
		return (PolylineGeometry) component.getEntity().getGeometry();
	}

	private static MetricPositionBinding2D bind(LocusDefinition2D definition,
			double parameter) {
		return new LocusMetricPositionBinder2D().bind(new LocusSemanticPosition2D(
				definition.getLocusIdentity(), BRANCH,
				definition.getProvider().getProviderId(),
				definition.getProvider().canonicalize(parameter)), definition);
	}

	private static BetweenPositionsMetricQuery between(LocusDefinition2D definition,
			double start, double target, TraversalDirection direction,
			OpenBoundaryPolicy boundary, SamePositionPolicy same) {
		return new BetweenPositionsMetricQuery(bind(definition, start),
				bind(definition, target), direction, boundary, same,
				LocusMetricPolicy2D.initial());
	}

	private static LocusMetricRoute2D route(LocusDefinition2D definition, double start,
			double target, TraversalDirection direction, OpenBoundaryPolicy boundary,
			SamePositionPolicy same) {
		return new LocusMetricRouteResolver2D().resolve(between(definition, start, target,
				direction, boundary, same), definition);
	}

	private static double length(LocusDefinition2D definition, double start,
			double target, TraversalDirection direction, OpenBoundaryPolicy boundary) {
		return metric(definition, between(definition, start, target, direction, boundary,
				SamePositionPolicy.ZERO_LENGTH)).getMetricValue().getFiniteValue()
				.orElseThrow();
	}

	private static double total(LocusDefinition2D definition) {
		return metric(definition, new TotalLocusMetricQuery(definition.getLocusIdentity(),
				1, LocusMetricPolicy2D.initial())).getMetricValue().getFiniteValue()
				.orElseThrow();
	}

	private static LocusMetricResult2D metric(LocusDefinition2D definition,
			LocusMetricQuery2D query) {
		LocusMetricInstrumentation2D instrumentation = new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				definition.getLocusIdentity(), instrumentation);
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			return new LocusMetricEngine2D().compute(query, definition,
					G7BMetricFixtures.analytic(1, "cu2-consumer-length/v1"), owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION, instrumentation);
		}
	}

	private DomainLocation seamRootLocation(LocusExistenceStructure2D structure) {
		LocusIntersectionResult2D result = intersect("cu2-seam-" + structure.hashCode(),
				structure, "x=-1", -Math.PI);
		assertEquals(1, result.getFiniteSolutions().size());
		return result.getFiniteSolutions().get(0).getClassification().getDomainLocation();
	}

	private LocusIntersectionResult2D intersect(String identity,
			LocusExistenceStructure2D structure, String target, double... roots) {
		G8BIntersectionFixtures.Fixture fixture = G8BIntersectionFixtures.create(
				getConstruction(), identity, provider(), List.of(branch(structure)),
				(source, branch, parameter) -> circle(parameter));
		GeoElement line = add(target);
		java.util.ArrayList<G8BIntersectionFixtures.Root> candidates =
				new java.util.ArrayList<>();
		for (int index = 0; index < roots.length; index++) {
			candidates.add(G8BIntersectionFixtures.Root.simple(fixture.branchKey(),
					fixture.componentKey(), roots[index], "cu2-root-" + index));
		}
		AlgoLocusIntersectionV2 algorithm = new AlgoLocusIntersectionV2(
				getConstruction(), fixture.locus(), line, identity + "/pair",
				identity + "/lineage", identity + "/target", identity + "/topology",
				G8BIntersectionFixtures.capability("cu2-consumer-roots/v1", context ->
						G8BIntersectionFixtures.completeRoots(context, candidates)),
				new GeoElement[0]);
		return algorithm.getResult().getIntersectionResult();
	}
}
