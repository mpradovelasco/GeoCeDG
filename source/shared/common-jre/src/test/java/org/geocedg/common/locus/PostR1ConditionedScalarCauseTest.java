/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSample2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversal2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversalPolicy2D;
import org.geocedg.common.kernel.locus.conditioned.LocusSelectionTestimony2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricComponentBuildException;
import org.geocedg.common.kernel.locus.metric.LocusMetricComponentKey2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResults2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionEvaluationStatus;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.PolicyV2LocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Step C4 of the corrections after block 3B ({@code locus-v2-metrics.md} section 28.3,
 * decision DN-2): the cause that the certificate of a branch names on a route decides
 * whether its conditioned length is a number. The fixture is the diagonal {@code (t, t)} on
 * {@code [0, 1]}, with one conditioned region over the whole domain and an explicit
 * existence structure of each cause.
 */
final class PostR1ConditionedScalarCauseTest {

	private static LocusMetricResult2D between(LocusCoverageCause2D cause) {
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, true);
		LocusExistenceStructure2D structure = LocusExistenceStructure2D.notEstablished(
				G7BMetricFixtures.BRANCH, domain, DomainTopology.BOUNDED, cause, "c4/" + cause);
		ExplicitNumericDomainProvider2D provider = new ExplicitNumericDomainProvider2D(
				"c4-parameter/v1", domain, Orientation.INCREASING, false, 1E-14);
		LocusBranch2D branch = new LocusBranch2D(G7BMetricFixtures.BRANCH, domain, structure,
				Orientation.INCREASING, "c4-fixture/v1", LocusLineage2D.unchanged(),
				EnumSet.of(BranchProperty.FINITE), LocusQuality2D.analyticDoubleSemantic());
		String identity = "c4-" + cause;
		LocusDefinition2D definition = new LocusDefinition2D(identity, 1,
				DefinitionStatus.VALID, provider, List.of(branch),
				(source, semanticBranch, parameter, session) -> LocusEvaluation2D.valid(
						new LocusPoint2D(parameter, parameter), Regularity.UNKNOWN,
						semanticBranch.getQuality()),
				Determinism.POINTWISE_DETERMINISTIC, "c4-fixture/v1",
				new LocusInstrumentation2D());
		LocusConditionedEvidence2D evidence = LocusConditionedTraversal2D.traverse(
				G7BMetricFixtures.BRANCH, 1, 0, 1, LocusConditionedTraversalPolicy2D.v1(),
				Collections.emptyList(), t -> LocusConditionedSample2D.valid(t, t, List.of(
						LocusSelectionTestimony2D.established("root-of-the-slice",
								LocusSelectionTestimony2D.Kind.LOCUS_ROOT_TOKEN, "a", 1))));
		assertEquals(1, evidence.getRegions().size());
		LocusMetricPolicy2D policy = LocusMetricPolicy2D.cu1PublicV2();
		LocusMetricInstrumentation2D instrumentation = new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(identity,
				instrumentation);
		LocusMetricEngine2D engine = new LocusMetricEngine2D();
		engine.setConditionedSource(key -> G7BMetricFixtures.BRANCH.equals(key) ? evidence
				: null);
		BetweenPositionsMetricQuery query = new BetweenPositionsMetricQuery(
				addressed(definition, branch, 0.2), addressed(definition, branch, 0.7),
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH, policy);
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			return engine.compute(query, definition, new LocusMetricCapabilityHierarchy2D(
					List.of(new PolicyV2LocusMetricCapability2D())), owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION, instrumentation, "c4-test");
		} catch (LocusMetricComponentBuildException exception) {
			return LocusMetricResults2D.failure(identity, definition.getSemanticRevision(),
					policy, exception.getComputationStatus(), Optional.empty(),
					exception.getDiagnostics());
		}
	}

	/** A position addressed by parameter, as the resolver of such an address binds it. */
	private static MetricPositionBinding2D addressed(LocusDefinition2D definition,
			LocusBranch2D branch, double parameter) {
		return new MetricPositionBinding2D(new LocusSemanticPosition2D(
				definition.getLocusIdentity(), branch.getBranchKey(),
				definition.getProvider().getProviderId(), parameter),
				definition.getSemanticRevision(), Optional.of(
						LocusMetricComponentKey2D.createDeclared(definition, branch, 0,
								branch.getDeclaredDriverDomain())),
				MetricPositionEvaluationStatus.VALID,
				Optional.of(new LocusPoint2D(parameter, parameter)), Collections.emptyList());
	}

	private static boolean has(LocusMetricResult2D result, MetricDiagnosticCode2D code) {
		return result.getDiagnostics().stream().anyMatch(d -> d.getCode() == code);
	}

	@ParameterizedTest
	@EnumSource(value = LocusCoverageCause2D.class, names = {"NOT_CERTIFIED",
			"OPERATION_OUTSIDE_CLASS", "NUMERIC_PROOF_METHOD_LIMIT", "BUDGET_EXHAUSTED",
			"UPSTREAM_NOT_ESTABLISHED"})
	void aRouteUnderACauseWithoutAnEventOfItsOwnIsANumber(LocusCoverageCause2D cause) {
		LocusMetricResult2D result = between(cause);
		assertEquals(MetricComputationStatus.SUCCESS, result.getComputationStatus(),
				result.getDiagnostics().toString());
		assertEquals(0.5 * Math.sqrt(2), result.getMetricValue().getFiniteValue()
				.orElseThrow(), 1e-9);
		assertEquals(MetricCoverage.INCOMPLETE, result.getCoverage());
		assertTrue(result.isConditionedEstimate());
		assertTrue(result.isScalarAdmissible(), result.getDiagnostics().toString());
		assertTrue(result.getConditionedRoute().isPresent());
		assertTrue(has(result, MetricDiagnosticCode2D.CONDITIONED_ROUTE_ADMITTED));
		assertFalse(has(result, MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE));
	}

	@ParameterizedTest
	@EnumSource(value = LocusCoverageCause2D.class, names = {
			"INHERITED_TOLERANCE_BEHAVIOUR", "SELECTION_NOT_REPRODUCED",
			"RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED", "DEGENERATE_CONFIGURATION",
			"COMPUTATIONAL_DEPENDENCY_FAILURE", "UNLABELLED_COINCIDENT_OUTPUT"})
	void aRouteUnderACauseThatNamesAnEventIsARichResultOnly(LocusCoverageCause2D cause) {
		LocusMetricResult2D result = between(cause);
		// The same estimate is published, declared conditioned, and is no number.
		assertEquals(MetricComputationStatus.SUCCESS, result.getComputationStatus(),
				result.getDiagnostics().toString());
		assertEquals(0.5 * Math.sqrt(2), result.getMetricValue().getFiniteValue()
				.orElseThrow(), 1e-9);
		assertEquals(MetricCoverage.INCOMPLETE, result.getCoverage());
		assertTrue(result.isConditionedEstimate());
		assertFalse(result.isScalarAdmissible());
		assertFalse(result.getConditionedRoute().isPresent());
		assertTrue(has(result, MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE),
				result.getDiagnostics().toString());
		assertTrue(result.getDiagnostics().toString().contains(cause.name()));
	}
}
