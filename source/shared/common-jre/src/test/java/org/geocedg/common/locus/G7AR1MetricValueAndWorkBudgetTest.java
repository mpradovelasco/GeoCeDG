/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.DoubleUnaryOperator;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.locus.G7AMetricNumerics.AdaptiveSimpsonIntegrator;
import org.geocedg.common.locus.G7AMetricNumerics.ExhaustedWorkLimit;
import org.geocedg.common.locus.G7AMetricNumerics.IntegrationResult;
import org.geocedg.common.locus.G7AMetricNumerics.MetricTolerance;
import org.geocedg.common.locus.G7AMetricNumerics.MetricWorkBudget;
import org.geocedg.common.locus.G7AMetricSemanticModel.AbsentMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.FiniteMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricComputationStatus;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricErrorAmount2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricErrorAmountState;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricErrorEvidence2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricValueKind;
import org.geocedg.common.locus.G7AMetricSemanticModel.Point;
import org.geocedg.common.locus.G7AMetricSemanticModel.PositiveInfinityMetricValue2D;
import org.junit.jupiter.api.Test;

/** Safe value/error and deterministic work-ceiling characterization for R1. */
class G7AR1MetricValueAndWorkBudgetTest {
	private static final MetricWorkBudget PROPOSED_INITIAL_BUDGET =
			new MetricWorkBudget(32768, 16384, 22);
	private static final MetricTolerance INITIAL_POLICY =
			new MetricTolerance(1e-10, 1e-9, PROPOSED_INITIAL_BUDGET);
	private final AdaptiveSimpsonIntegrator integrator =
			new AdaptiveSimpsonIntegrator();

	@Test
	void closedMetricValueNeverUsesDoubleSentinels() {
		MetricValue2D finite = new FiniteMetricValue2D(0);
		MetricValue2D infinity = new PositiveInfinityMetricValue2D();
		MetricValue2D absent = new AbsentMetricValue2D();
		assertEquals(MetricValueKind.FINITE, finite.kind());
		assertEquals(0, finite.finiteValue().orElseThrow(), 0);
		assertEquals(MetricValueKind.POSITIVE_INFINITY, infinity.kind());
		assertTrue(infinity.finiteValue().isEmpty());
		assertEquals(MetricValueKind.ABSENT, absent.kind());
		assertTrue(absent.finiteValue().isEmpty());
		assertThrows(IllegalArgumentException.class,
				() -> new FiniteMetricValue2D(Double.NaN));
		assertThrows(IllegalArgumentException.class,
				() -> new FiniteMetricValue2D(Double.POSITIVE_INFINITY));
		assertThrows(IllegalArgumentException.class,
				() -> new FiniteMetricValue2D(-1));
	}

	@Test
	void errorEvidenceDistinguishesExactEstimatedUnknownAndNotApplicable() {
		MetricErrorEvidence2D exact = MetricErrorEvidence2D.exact(false,
				"integer segment identity");
		MetricErrorEvidence2D certified = MetricErrorEvidence2D.certified(1e-12,
				OptionalDouble.of(5e-13), "interval proof", "certificate/v1");
		List<String> assumptions = new ArrayList<>(List.of("smooth speed"));
		MetricErrorEvidence2D estimated = MetricErrorEvidence2D.estimated(1e-9,
				OptionalDouble.of(1e-10), "adaptive quadrature", assumptions);
		assumptions.add("mutated after construction");
		MetricErrorEvidence2D uncertified =
				MetricErrorEvidence2D.uncertified("evaluator refinement", List.of());
		final MetricErrorEvidence2D notApplicable =
				MetricErrorEvidence2D.notApplicable("absent metric value");

		assertEquals(NumericGuarantee.EXACT_ARITHMETIC,
				exact.numericGuarantee().orElseThrow());
		assertEquals(0, exact.absoluteEvidence().amount().orElseThrow(), 0);
		assertEquals(MetricErrorAmountState.NOT_APPLICABLE,
				exact.relativeEvidence().state());
		assertEquals(NumericGuarantee.CERTIFIED_ERROR_BOUND,
				certified.numericGuarantee().orElseThrow());
		assertTrue(certified.certificateMetadata().isPresent());
		assertEquals(List.of("smooth speed"), estimated.assumptions());
		assertEquals(MetricErrorAmountState.NOT_ESTABLISHED,
				uncertified.absoluteEvidence().state());
		assertTrue(uncertified.absoluteEvidence().amount().isEmpty());
		assertTrue(notApplicable.numericGuarantee().isEmpty());
		assertEquals(MetricErrorAmountState.NOT_APPLICABLE,
				notApplicable.absoluteEvidence().state());
		assertThrows(IllegalArgumentException.class,
				() -> MetricErrorAmount2D.established(Double.NaN));
		assertThrows(IllegalArgumentException.class,
				() -> MetricErrorAmount2D.established(-1));
	}

	@Test
	void metricGuaranteeReusesTheNormativeG6TypeAndVocabulary() {
		assertEquals(LocusSemanticMetadata2D.NumericGuarantee.class,
				NumericGuarantee.class);
		assertEquals(List.of("EXACT_ARITHMETIC", "CERTIFIED_ERROR_BOUND",
				"ESTIMATED_ERROR", "FLOATING_POINT_UNCERTIFIED"),
				Arrays.stream(NumericGuarantee.values()).map(Enum::name).toList());
		assertTrue(List.of(NumericGuarantee.EXACT_ARITHMETIC,
				NumericGuarantee.CERTIFIED_ERROR_BOUND,
				NumericGuarantee.ESTIMATED_ERROR).stream()
				.allMatch(this::candidateScalarGuarantee));
		assertFalse(candidateScalarGuarantee(
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED));
	}

	@Test
	void evaluationSubdivisionAndDepthCeilingsAreIndependentTypedGuards() {
		DoubleUnaryOperator difficult = parameter ->
				1 + Math.abs(Math.sin(10000 * parameter));
		IntegrationResult evaluation = integrator.integrate(difficult, 0, 1,
				new MetricTolerance(1e-30, 0,
						new MetricWorkBudget(3, 100, 22)), 1);
		IntegrationResult subdivision = integrator.integrate(difficult, 0, 1,
				new MetricTolerance(1e-30, 0,
						new MetricWorkBudget(100, 1, 22)), 1);
		IntegrationResult depth = integrator.integrate(difficult, 0, 1,
				new MetricTolerance(1e-30, 0,
						new MetricWorkBudget(1000, 1000, 1)), 1);

		assertBudgetFailure(evaluation, ExhaustedWorkLimit.MAXIMUM_EVALUATIONS);
		assertBudgetFailure(subdivision,
				ExhaustedWorkLimit.MAXIMUM_SUBDIVISIONS);
		assertBudgetFailure(depth, ExhaustedWorkLimit.MAXIMUM_DEPTH);
		assertEquals(3, evaluation.evaluations());
		assertEquals(1, subdivision.subdivisions());
	}

	@Test
	void initialTolerancesStayWithinMeasuredDeterministicSafetyMargin() {
		Map<String, IntegrationResult> measurements = new LinkedHashMap<>();
		measurements.put("ellipse", integrate(parameter -> Math.hypot(
				3 * Math.sin(parameter), 2 * Math.cos(parameter)), 0,
				2 * Math.PI, 16));
		measurements.put("parabola", integrate(parameter ->
				Math.sqrt(1 + 4 * parameter * parameter), -1, 1, 3));
		measurements.put("transcendental", integrate(parameter ->
				Math.sqrt(1 + Math.exp(2 * parameter)), 0, 1, 2));
		measurements.put("cusp", integrate(parameter -> Math.hypot(
				2 * parameter, 3 * parameter * parameter), -1, 1, 3));
		double c = 2;
		double denominator = Math.exp(c) - 1;
		measurements.put("regular-reparameterization", integrate(parameter ->
				c * Math.exp(c * parameter) / denominator, 0, 1, 1));
		measurements.put("u3-endpoint-degenerate", integrate(parameter ->
				3 * parameter * parameter, 0, 1, 1));
		measurements.put("finite-improper-transform", integrate(parameter -> 1,
				0, 1, 1));
		measurements.put("difficult-improper-transform", integrate(parameter ->
				1 / Math.sqrt(parameter + 1e-6), 0, 1, 2));

		long maximumEvaluations = 0;
		long maximumSubdivisions = 0;
		for (Map.Entry<String, IntegrationResult> measurement
				: measurements.entrySet()) {
			IntegrationResult result = measurement.getValue();
			maximumEvaluations = Math.max(maximumEvaluations,
					result.evaluations());
			maximumSubdivisions = Math.max(maximumSubdivisions,
					result.subdivisions());
			System.out.println("G7A_R1_WORK fixture=" + measurement.getKey()
					+ " evaluations=" + result.evaluations()
					+ " subdivisions=" + result.subdivisions()
					+ " status=" + result.status() + " exhausted="
					+ result.exhaustedWorkLimit());
			if (measurement.getKey().equals("difficult-improper-transform")) {
				assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
						result.status());
				assertEquals(ExhaustedWorkLimit.MAXIMUM_DEPTH,
						result.exhaustedWorkLimit());
			} else {
				assertEquals(MetricComputationStatus.SUCCESS, result.status(),
						measurement.getKey());
			}
		}
		long evaluatorOnlyCalls = G7AMetricNumerics.evaluatorOnlyRefinement(
				parameter -> new Point(parameter,
						0.1 * Math.sin(2 * Math.PI * 64 * parameter)),
				0, 1, 64, 4096).evaluatorCalls();
		assertTrue(maximumEvaluations * 4
				< PROPOSED_INITIAL_BUDGET.maximumEvaluations());
		assertTrue(maximumSubdivisions * 4
				< PROPOSED_INITIAL_BUDGET.maximumSubdivisions());
		assertTrue(evaluatorOnlyCalls * 4
				< PROPOSED_INITIAL_BUDGET.maximumEvaluations());
		System.out.println("G7A_R1_WORK observed_max_evaluations="
				+ maximumEvaluations + " observed_max_subdivisions="
				+ maximumSubdivisions + " evaluator_only_calls="
				+ evaluatorOnlyCalls + " proposed_max_evaluations="
				+ PROPOSED_INITIAL_BUDGET.maximumEvaluations()
				+ " proposed_max_subdivisions="
				+ PROPOSED_INITIAL_BUDGET.maximumSubdivisions()
				+ " proposed_max_depth="
				+ PROPOSED_INITIAL_BUDGET.maximumDepth());
	}

	@Test
	void numericalFailureAndBudgetExhaustionRemainDifferentOutcomes() {
		IntegrationResult budget = integrator.integrate(parameter ->
				1 + Math.abs(Math.sin(10000 * parameter)), 0, 1,
				new MetricTolerance(1e-30, 0,
						new MetricWorkBudget(3, 100, 22)), 1);
		IntegrationResult failure = integrator.integrate(parameter -> {
			throw new IllegalArgumentException("injected evaluator failure");
		}, 0, 1, INITIAL_POLICY, 1);
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				budget.status());
		assertEquals(MetricComputationStatus.NUMERICAL_FAILURE,
				failure.status());
		assertTrue(budget.finiteValue().isEmpty());
		assertTrue(failure.finiteValue().isEmpty());
	}

	private IntegrationResult integrate(DoubleUnaryOperator speed, double start,
			double end, double scale) {
		return integrator.integrate(speed, start, end, INITIAL_POLICY, scale);
	}

	private boolean candidateScalarGuarantee(NumericGuarantee guarantee) {
		return guarantee != NumericGuarantee.FLOATING_POINT_UNCERTIFIED;
	}

	private static void assertBudgetFailure(IntegrationResult result,
			ExhaustedWorkLimit expectedLimit) {
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				result.status());
		assertEquals(expectedLimit, result.exhaustedWorkLimit());
		assertTrue(result.finiteValue().isEmpty());
		assertTrue(result.errorEvidence().numericGuarantee().isEmpty());
	}
}
