/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.metric.AnalyticLocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.DifferentialLocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.EvaluatorOnlyLocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.EvaluatorOnlyPolicy;
import org.geocedg.common.kernel.locus.metric.LocusAnalyticMetricEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricComponentBuildException;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexKey2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexStatistics2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentationSnapshot2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIntegrator2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricIntegrationResult2D;
import org.geocedg.common.kernel.locus.metric.MetricMultiplicityPolicy;
import org.geocedg.common.kernel.locus.metric.MetricRectifiability;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.locus.metric.MetricWorkBudget2D;
import org.geocedg.common.kernel.locus.metric.MetricWorkLimit2D;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.junit.jupiter.api.Test;

/** Productive total-variation, capability and bounded-index tests. */
class LocusMetricProductiveNumericalTest {
	private static final double ELLIPSE_3_2 =
			15.865439589290589791331663027783072496730082848327;
	private static final double EXPONENTIAL_GRAPH =
			2.0034971116273524785699027524202391308211427952321;
	private static final double CUSP_MINUS_ONE_ONE =
			2.8794197467431008007814723316382553556493967007399;
	private final LocusMetricEngine2D engine = new LocusMetricEngine2D();

	@Test
	void analyticTotalSumsEveryDisconnectedComponentWithoutGapChord() {
		LocusDefinition2D definition = definition("components", 1,
				G7BMetricFixtures.components(-4, -1, 1, 4),
				parameter -> new LocusPoint2D(parameter, 0));
		LocusMetricResult2D result = total(definition,
				G7BMetricFixtures.analytic(1, "line/v1"),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, "total");
		assertEquals(6, result.getMetricValue().getFiniteValue().orElseThrow(),
				0);
		assertEquals(2, result.getContributions().size());
		assertEquals(MetricCoverage.COMPLETE, result.getCoverage());
	}

	@Test
	void constructiveMultiplicityCountsCoincidentBranchesSeparately() {
		List<LocusBranch2D> branches = List.of(
				G7BMetricFixtures.branch("branch-a", -4, 4,
						G7BMetricFixtures.components(-4, 4)),
				G7BMetricFixtures.branch("branch-b", -4, 4,
						G7BMetricFixtures.components(-4, 4)));
		LocusDefinition2D definition =
				G7BMetricFixtures.definitionWithBranches("multiplicity", 1,
						false, branches,
						parameter -> new LocusPoint2D(parameter, 0));
		LocusMetricResult2D result = total(definition,
				G7BMetricFixtures.analytic(1, "line/v1"),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, "total");
		assertEquals(16, result.getMetricValue().getFiniteValue().orElseThrow(),
				0);
		assertEquals(2, result.getContributions().size());
	}

	@Test
	void periodicTotalUsesExactlyOneFundamentalCycle() {
		LocusDefinition2D definition = G7BMetricFixtures.definition(
				"periodic", 1, true,
				G7BMetricFixtures.components(-4, 4),
				parameter -> new LocusPoint2D(Math.cos(parameter),
						Math.sin(parameter)));
		LocusMetricResult2D result = total(definition,
				G7BMetricFixtures.analytic(1, "cycle/v1"),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, "total");
		assertEquals(8, result.getMetricValue().getFiniteValue().orElseThrow(),
				0);
		assertEquals(1, result.getContributions().size());
		assertTrue(result.getTraversalOutcome().isEmpty());
	}

	@Test
	void emptyAndIsolatedDomainsAreCompleteFiniteZeroNotAbsence() {
		LocusDefinition2D empty =
				G7BMetricFixtures.emptyDefinition("empty", 1);
		LocusMetricResult2D emptyResult = total(empty,
				G7BMetricFixtures.analytic(1, "empty/v1"),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, "empty");
		LocusDefinition2D isolated = definition("isolated", 1,
				G7BMetricFixtures.components(2, 2),
				parameter -> new LocusPoint2D(1, 1));
		LocusMetricResult2D isolatedResult = total(isolated,
				G7BMetricFixtures.analytic(1, "point/v1"),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, "point");
		assertEquals(MetricValueKind.FINITE,
				emptyResult.getMetricValue().getKind());
		assertEquals(0, emptyResult.getMetricValue().getFiniteValue()
				.orElseThrow(), 0);
		assertEquals(0, isolatedResult.getMetricValue().getFiniteValue()
				.orElseThrow(), 0);
		assertEquals(MetricCoverage.COMPLETE, emptyResult.getCoverage());
		assertEquals(MetricCoverage.COMPLETE, isolatedResult.getCoverage());
	}

	@Test
	void differentialQuadratureMatchesIndependentParabolaReference() {
		LocusDefinition2D definition = definition("parabola", 1,
				G7BMetricFixtures.components(-1, 1),
				parameter -> new LocusPoint2D(parameter,
						parameter * parameter));
		DifferentialLocusMetricCapability2D differential =
				new DifferentialLocusMetricCapability2D("parabola-diff/v1",
						(source, branch, parameter, session) ->
								LocusDifferentialEvaluation2D.valid(1,
										2 * parameter));
		LocusMetricResult2D result = total(definition,
				new LocusMetricCapabilityHierarchy2D(List.of(differential)),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, "parabola");
		double expected = Math.sqrt(5) + Math.log(2 + Math.sqrt(5)) / 2;
		assertEquals(expected,
				result.getMetricValue().getFiniteValue().orElseThrow(), 1E-9);
		assertEquals(MetricComputationStatus.SUCCESS,
				result.getComputationStatus());
	}

	@Test
	void productiveIntegratorMatchesVersionedScientificReferences() {
		LocusMetricIntegrator2D integrator = new LocusMetricIntegrator2D();
		LocusMetricPolicy2D policy = LocusMetricPolicy2D.initial();
		assertScientific(integrator.integrate(parameter -> 5, 0, 1, policy,
				5, new LocusMetricInstrumentation2D()), 5, 1E-12);
		assertScientific(integrator.integrate(parameter -> 3, -Math.PI,
				Math.PI, policy, 3,
				new LocusMetricInstrumentation2D()), 6 * Math.PI, 1E-10);
		assertScientific(integrator.integrate(parameter -> Math.hypot(
				3 * Math.sin(parameter), 2 * Math.cos(parameter)), -Math.PI,
				Math.PI, policy, 3,
				new LocusMetricInstrumentation2D()), ELLIPSE_3_2, 2E-9);
		assertScientific(integrator.integrate(parameter ->
				Math.sqrt(1 + Math.exp(2 * parameter)), 0, 1, policy, 2,
				new LocusMetricInstrumentation2D()), EXPONENTIAL_GRAPH, 2E-9);
		assertScientific(integrator.integrate(parameter -> Math.hypot(
				2 * parameter, 3 * parameter * parameter), -1, 1, policy, 3,
				new LocusMetricInstrumentation2D()), CUSP_MINUS_ONE_ONE, 2E-9);
	}

	@Test
	void differentialMetricIsTranslationInvariantAndScaleCovariant() {
		LocusMetricIntegrator2D integrator = new LocusMetricIntegrator2D();
		LocusMetricPolicy2D policy = LocusMetricPolicy2D.initial();
		MetricIntegrationResult2D original = integrator.integrate(
				parameter -> Math.hypot(1, 2 * parameter), -1, 1, policy,
				3, new LocusMetricInstrumentation2D());
		MetricIntegrationResult2D translated = integrator.integrate(
				parameter -> Math.hypot(1, 2 * parameter), -1, 1, policy,
				3, new LocusMetricInstrumentation2D());
		MetricIntegrationResult2D scaled = integrator.integrate(
				parameter -> 1000 * Math.hypot(1, 2 * parameter), -1, 1,
				policy, 3000, new LocusMetricInstrumentation2D());
		double originalValue = original.getMetricValue().getFiniteValue()
				.orElseThrow();
		assertEquals(originalValue, translated.getMetricValue().getFiniteValue()
				.orElseThrow(), 0);
		assertEquals(1000 * originalValue,
				scaled.getMetricValue().getFiniteValue().orElseThrow(), 2E-6);
	}

	@Test
	void regularAndEndpointDegenerateReparameterizationsPreserveLength() {
		LocusMetricIntegrator2D integrator = new LocusMetricIntegrator2D();
		LocusMetricPolicy2D policy = LocusMetricPolicy2D.initial();
		double c = 2;
		double denominator = Math.exp(c) - 1;
		MetricIntegrationResult2D regular = integrator.integrate(
				parameter -> c * Math.exp(c * parameter) / denominator,
				0, 1, policy, 1, new LocusMetricInstrumentation2D());
		MetricIntegrationResult2D degenerate = integrator.integrate(
				parameter -> 3 * parameter * parameter, 0, 1, policy, 1,
				new LocusMetricInstrumentation2D());
		assertEquals(1, regular.getMetricValue().getFiniteValue()
				.orElseThrow(), 1E-9);
		assertEquals(1, degenerate.getMetricValue().getFiniteValue()
				.orElseThrow(), 1E-9);
	}

	@Test
	void evaluatorOnlyNeverClaimsCertificationFromRefinementAgreement() {
		LocusDefinition2D definition = definition("evaluator", 1,
				G7BMetricFixtures.components(-1, 1),
				parameter -> new LocusPoint2D(parameter,
						parameter * parameter));
		LocusMetricResult2D result = total(definition,
				new LocusMetricCapabilityHierarchy2D(List.of(
						new EvaluatorOnlyLocusMetricCapability2D(
								"evaluator/v1"))),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, "evaluator");
		assertEquals(org.geocedg.common.kernel.locus.LocusSemanticMetadata2D
				.NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				result.getErrorEvidence().getNumericGuarantee()
						.orElseThrow());
		assertFalse(result.isScalarAdmissible());
	}

	@Test
	void evaluatorOnlyEstimateRequiresExplicitPolicyAssumptions() {
		LocusDefinition2D definition = definition("estimate", 1,
				G7BMetricFixtures.components(-1, 1),
				parameter -> new LocusPoint2D(parameter,
						parameter * parameter));
		LocusMetricPolicy2D policy = policy("estimate/v1",
				EvaluatorOnlyPolicy.ESTIMATED_WITH_EXPLICIT_ASSUMPTIONS,
				LocusMetricPolicy2D.initial().getWorkBudget());
		LocusMetricResult2D result = total(definition,
				new LocusMetricCapabilityHierarchy2D(List.of(
						new EvaluatorOnlyLocusMetricCapability2D(
								"evaluator/v1"))),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, "estimate",
				policy);
		assertFalse(result.getErrorEvidence().getAssumptions().isEmpty());
		assertTrue(result.isScalarAdmissible());
	}

	@Test
	void evaluatorOnlySubarcDoesNotInheritComponentEstimate() {
		LocusDefinition2D definition = definition("estimated-subarc", 1,
				G7BMetricFixtures.components(0, 1),
				parameter -> new LocusPoint2D(parameter * parameter * parameter,
						0));
		LocusMetricPolicy2D policy = policy("estimated-subarc/v1",
				EvaluatorOnlyPolicy.ESTIMATED_WITH_EXPLICIT_ASSUMPTIONS,
				LocusMetricPolicy2D.initial().getWorkBudget());
		LocusMetricCapabilityHierarchy2D capabilities =
				new LocusMetricCapabilityHierarchy2D(List.of(
						new EvaluatorOnlyLocusMetricCapability2D(
								"evaluator/v1")));
		LocusMetricInstrumentation2D instrumentation =
				new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				definition.getLocusIdentity(), instrumentation);
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			LocusMetricResult2D complete = engine.compute(
					new TotalLocusMetricQuery(definition.getLocusIdentity(), 1,
							policy), definition, capabilities, owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
					instrumentation, "complete");
			BetweenPositionsMetricQuery subarc =
					new BetweenPositionsMetricQuery(
							G7BMetricFixtures.bind(definition,
									G7BMetricFixtures.BRANCH, 0.25),
							G7BMetricFixtures.bind(definition,
									G7BMetricFixtures.BRANCH, 0.75),
							TraversalDirection.FORWARD,
							OpenBoundaryPolicy.STRICT,
							SamePositionPolicy.ZERO_LENGTH, policy);
			LocusMetricResult2D between = engine.compute(subarc, definition,
					capabilities, owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
					instrumentation, "between");

			assertEquals(org.geocedg.common.kernel.locus
					.LocusSemanticMetadata2D.NumericGuarantee.ESTIMATED_ERROR,
					complete.getErrorEvidence().getNumericGuarantee()
							.orElseThrow());
			assertTrue(complete.isScalarAdmissible());
			assertEquals(org.geocedg.common.kernel.locus
					.LocusSemanticMetadata2D.NumericGuarantee
							.FLOATING_POINT_UNCERTIFIED,
					between.getErrorEvidence().getNumericGuarantee()
							.orElseThrow());
			assertFalse(between.isScalarAdmissible());
			assertEquals(1, owner.statistics().getBuilds());
			assertEquals(1, owner.statistics().getHits());
		}
	}

	@Test
	void threeIndependentWorkCeilingsReturnLimitNotEstablished() {
		LocusMetricIntegrator2D integrator = new LocusMetricIntegrator2D();
		java.util.function.DoubleUnaryOperator difficult =
				parameter -> 1 + Math.abs(Math.sin(10000 * parameter));
		MetricIntegrationResult2D evaluations = integrator.integrate(difficult,
				0, 1, policy("eval-limit", EvaluatorOnlyPolicy.UNCERTIFIED,
						new MetricWorkBudget2D(3, 100, 22)),
				1, new LocusMetricInstrumentation2D());
		MetricIntegrationResult2D subdivisions = integrator.integrate(difficult,
				0, 1, policy("sub-limit", EvaluatorOnlyPolicy.UNCERTIFIED,
						new MetricWorkBudget2D(100, 1, 22)),
				1, new LocusMetricInstrumentation2D());
		MetricIntegrationResult2D depth = integrator.integrate(difficult,
				0, 1, policy("depth-limit", EvaluatorOnlyPolicy.UNCERTIFIED,
						new MetricWorkBudget2D(1000, 1000, 1)),
				1, new LocusMetricInstrumentation2D());
		assertLimit(evaluations, MetricWorkLimit2D.MAXIMUM_EVALUATIONS);
		assertLimit(subdivisions, MetricWorkLimit2D.MAXIMUM_SUBDIVISIONS);
		assertLimit(depth, MetricWorkLimit2D.MAXIMUM_DEPTH);
	}

	@Test
	void strictUnreachableRejectsBeforeComponentStateBuild() {
		LocusDefinition2D definition = definition("strict", 1,
				G7BMetricFixtures.components(-4, 4),
				parameter -> new LocusPoint2D(parameter, 0));
		BetweenPositionsMetricQuery query = G7BMetricFixtures.between(
				definition, 2, -2, TraversalDirection.FORWARD,
				OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH);
		LocusMetricInstrumentation2D instrumentation =
				new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				definition.getLocusIdentity(), instrumentation);
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			LocusMetricResult2D result = engine.compute(query, definition,
					G7BMetricFixtures.analytic(1, "line/v1"), owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
					instrumentation, "strict");
			assertEquals(MetricComputationStatus.INVALID_QUERY,
					result.getComputationStatus());
			assertEquals(0, owner.statistics().getBuilds());
		}
	}

	@Test
	void hundredCompatibleConsumersShareOneComponentState() {
		LocusDefinition2D definition = definition("shared", 1,
				G7BMetricFixtures.components(-4, 4),
				parameter -> new LocusPoint2D(parameter, 0));
		LocusMetricInstrumentation2D instrumentation =
				new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				definition.getLocusIdentity(), instrumentation);
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			for (int consumer = 0; consumer < 100; consumer++) {
				TotalLocusMetricQuery query = new TotalLocusMetricQuery(
						definition.getLocusIdentity(),
						definition.getSemanticRevision(),
						LocusMetricPolicy2D.initial());
				engine.compute(query, definition,
						G7BMetricFixtures.analytic(1, "line/v1"), owner,
						LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
						instrumentation, "M" + consumer);
			}
			LocusMetricIndexStatistics2D statistics = owner.statistics();
			LocusMetricInstrumentationSnapshot2D counters =
					instrumentation.snapshot();
			assertEquals(1, statistics.getBuilds());
			assertEquals(99, statistics.getHits());
			assertEquals(99, counters.getCrossResultHits());
			assertEquals(0, counters.getDuplicateCompatibleBuilds());
			assertEquals(1, statistics.getRetainedEntries());
		}
	}

	@Test
	void completeKeyPolicyChangesMissAndCapacityEvictsDeterministically() {
		LocusDefinition2D definition = definition("capacity", 1,
				G7BMetricFixtures.components(-4, 4),
				parameter -> new LocusPoint2D(parameter, 0));
		LocusMetricInstrumentation2D instrumentation =
				new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				definition.getLocusIdentity(), instrumentation);
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			for (int version = 0; version < 65; version++) {
				LocusMetricPolicy2D policy = policy("policy-" + version,
						EvaluatorOnlyPolicy.UNCERTIFIED,
						LocusMetricPolicy2D.initial().getWorkBudget());
				engine.compute(new TotalLocusMetricQuery(
						definition.getLocusIdentity(), 1, policy),
						definition,
						G7BMetricFixtures.analytic(1, "line/v1"), owner,
						LocusMetricIndexMode.LAZY_COMPONENT_REVISION,
						instrumentation, "M");
			}
			assertEquals(65, owner.statistics().getBuilds());
			assertEquals(64, owner.statistics().getRetainedEntries());
			assertEquals(1, owner.statistics().getEvictions());
		}
	}

	@Test
	void failedPrivateBuildPublishesNoEntryAndCleansActiveBuild() {
		LocusDefinition2D definition = definition("failure", 1,
				G7BMetricFixtures.components(-4, 4),
				parameter -> new LocusPoint2D(parameter, 0));
		LocusMetricInstrumentation2D instrumentation =
				new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				definition.getLocusIdentity(), instrumentation);
		LocusBranch2D branch = definition.getBranches().get(0);
		String component = org.geocedg.common.kernel.locus.metric
				.LocusMetricComponentKey2D.create(definition, branch, 0);
		LocusMetricIndexKey2D key = new LocusMetricIndexKey2D(
				definition.getLocusIdentity(), 1, branch.getBranchKey(),
				component, "failing/v1", LocusMetricPolicy2D.initial());
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			assertThrows(IllegalStateException.class,
					() -> owner.getOrBuildComponentState(key, ignored -> {
						throw new IllegalStateException("injected");
					}, "M"));
			assertEquals(0, owner.statistics().getRetainedEntries());
			assertEquals(0, owner.statistics().getActiveBuilds());
			assertEquals(1, owner.statistics().getFailedBuilds());
		}
	}

	@Test
	void cacheOffOracleAndSharedPathHaveEqualRichSemantics() {
		LocusDefinition2D definition = definition("oracle", 1,
				G7BMetricFixtures.components(-4, -1, 1, 4),
				parameter -> new LocusPoint2D(parameter, 0));
		LocusMetricResult2D shared = total(definition,
				G7BMetricFixtures.analytic(2, "line/v1"),
				LocusMetricIndexMode.LAZY_COMPONENT_REVISION, "shared");
		LocusMetricResult2D reference = total(definition,
				G7BMetricFixtures.analytic(2, "line/v1"),
				LocusMetricIndexMode.REFERENCE_NO_INDEX_REUSE, "reference");
		assertEquals(shared.getMetricValue().getKind(),
				reference.getMetricValue().getKind());
		assertEquals(shared.getMetricValue().getFiniteValue().orElseThrow(),
				reference.getMetricValue().getFiniteValue().orElseThrow(), 0);
		assertEquals(shared.getCoverage(), reference.getCoverage());
		assertEquals(shared.getComputationStatus(),
				reference.getComputationStatus());
		assertEquals(shared.getRectifiability(),
				reference.getRectifiability());
		assertEquals(shared.getContributions().size(),
				reference.getContributions().size());
	}

	private LocusMetricResult2D total(LocusDefinition2D definition,
			LocusMetricCapabilityHierarchy2D capabilities,
			LocusMetricIndexMode mode, String consumer) {
		return total(definition, capabilities, mode, consumer,
				LocusMetricPolicy2D.initial());
	}

	private LocusMetricResult2D total(LocusDefinition2D definition,
			LocusMetricCapabilityHierarchy2D capabilities,
			LocusMetricIndexMode mode, String consumer,
			LocusMetricPolicy2D policy) {
		LocusMetricInstrumentation2D instrumentation =
				new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(
				definition.getLocusIdentity(), instrumentation);
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			return engine.compute(new TotalLocusMetricQuery(
					definition.getLocusIdentity(),
					definition.getSemanticRevision(), policy),
					definition, capabilities, owner, mode, instrumentation,
					consumer);
		}
	}

	private static LocusDefinition2D definition(String identity,
			long revision, List<LocusInterval2D> components,
			java.util.function.DoubleFunction<LocusPoint2D> function) {
		return G7BMetricFixtures.definition(identity, revision, false,
				components, function);
	}

	private static LocusMetricPolicy2D policy(String version,
			EvaluatorOnlyPolicy evaluatorOnly,
			MetricWorkBudget2D budget) {
		LocusMetricPolicy2D base = LocusMetricPolicy2D.initial();
		return new LocusMetricPolicy2D(base.getAbsoluteTolerance(),
				base.getRelativeTolerance(), budget,
				base.getMetricAlgorithmVersion(), version,
				base.getTolerancePolicyVersion(),
				MetricMultiplicityPolicy.CONSTRUCTIVE_TRAVERSAL_LENGTH,
				base.getImproperLimitPolicy(), evaluatorOnly);
	}

	private static void assertLimit(MetricIntegrationResult2D result,
			MetricWorkLimit2D expected) {
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				result.getComputationStatus());
		assertEquals(expected, result.getExhaustedWorkLimit());
		assertEquals(MetricValueKind.ABSENT,
				result.getMetricValue().getKind());
	}

	private static void assertScientific(MetricIntegrationResult2D result,
			double expected, double tolerance) {
		assertEquals(MetricComputationStatus.SUCCESS,
				result.getComputationStatus());
		assertEquals(expected,
				result.getMetricValue().getFiniteValue().orElseThrow(), tolerance);
	}
}
