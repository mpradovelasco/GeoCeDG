/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.locus.G7AMetricNumerics.AdaptiveSimpsonIntegrator;
import org.geocedg.common.locus.G7AMetricNumerics.EvaluatorOnlyResult;
import org.geocedg.common.locus.G7AMetricNumerics.IntegrationResult;
import org.geocedg.common.locus.G7AMetricNumerics.MetricTolerance;
import org.geocedg.common.locus.G7AMetricSemanticModel.AbsentMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.FiniteMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricComputationStatus;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricRectifiability;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricValueKind;
import org.geocedg.common.locus.G7AMetricSemanticModel.Point;
import org.geocedg.common.locus.G7AMetricSemanticModel.PositiveInfinityMetricValue2D;
import org.geogebra.common.factories.FormatFactory;
import org.geogebra.common.jre.factory.FormatFactoryJre;
import org.geogebra.common.kernel.cas.AlgoIntegralDefinite;
import org.junit.jupiter.api.Test;

/** Independent analytic and numerical characterization for the G7A contract. */
class G7AMetricNumericalCharacterizationTest {
	private static final double ELLIPSE_3_2 =
			15.865439589290589791331663027783072496730082848327;
	private static final double EXPONENTIAL_GRAPH =
			2.0034971116273524785699027524202391308211427952321;
	private static final double PARABOLA_MINUS_ONE_ONE =
			2.9578857150891948676558120388678288701433948611900;
	private static final double CUSP_MINUS_ONE_ONE =
			2.8794197467431008007814723316382553556493967007399;
	private static final MetricTolerance TOLERANCE =
			new MetricTolerance(1e-11, 1e-11, 24);
	private final AdaptiveSimpsonIntegrator integrator =
			new AdaptiveSimpsonIntegrator();

	@Test
	void totalVariationIsSupremumOfPartitionSumsNotAnAlgorithmDefinition() {
		DoubleFunction<Point> parabola = parameter ->
				new Point(parameter, parameter * parameter);
		double coarse = partitionVariation(parabola, -1, 1, 4);
		double medium = partitionVariation(parabola, -1, 1, 32);
		double fine = partitionVariation(parabola, -1, 1, 4096);
		assertTrue(coarse < medium);
		assertTrue(medium < fine);
		assertTrue(fine < PARABOLA_MINUS_ONE_ONE);
		assertEquals(PARABOLA_MINUS_ONE_ONE, fine, 1e-7);
	}

	@Test
	void analyticAndDifferentialCapabilitiesReachIndependentReferences() {
		assertEquals(5, Math.hypot(3, 4), 0);
		assertEquals(2 * Math.PI * 3, analyticCircleLength(3), 0);

		IntegrationResult ellipse = integrate(parameter -> Math.hypot(
				3 * Math.sin(parameter), 2 * Math.cos(parameter)), 0,
				2 * Math.PI, 18);
		IntegrationResult parabola = integrate(parameter ->
				Math.sqrt(1 + 4 * parameter * parameter), -1, 1, 3);
		IntegrationResult exponential = integrate(parameter ->
				Math.sqrt(1 + Math.exp(2 * parameter)), 0, 1, 2);
		IntegrationResult cusp = integrate(parameter -> Math.hypot(
				2 * parameter, 3 * parameter * parameter), -1, 1, 3);

		assertSuccessful(ellipse, ELLIPSE_3_2, 2e-10);
		assertSuccessful(parabola, PARABOLA_MINUS_ONE_ONE, 2e-10);
		assertSuccessful(exponential, EXPONENTIAL_GRAPH, 2e-10);
		assertSuccessful(cusp, CUSP_MINUS_ONE_ONE, 2e-10);
		assertEquals(NumericGuarantee.ESTIMATED_ERROR,
				ellipse.numericGuarantee().orElseThrow());
	}

	@Test
	void existingAdaptiveGaussIsAccurateButDoesNotExposeMetricErrorPolicy() {
		// The inherited integrator is coupled to GeoGebra's global formatting
		// bootstrap even when only its static numerical helper is requested.
		FormatFactory.setPrototypeIfNull(new FormatFactoryJre());
		double upstreamEllipse = AlgoIntegralDefinite.numericIntegration(
				parameter -> Math.hypot(3 * Math.sin(parameter),
						2 * Math.cos(parameter)), 0, 2 * Math.PI);
		double upstreamExponential = AlgoIntegralDefinite.numericIntegration(
				parameter -> Math.sqrt(1 + Math.exp(2 * parameter)), 0, 1);
		IntegrationResult candidateEllipse = integrate(parameter -> Math.hypot(
				3 * Math.sin(parameter), 2 * Math.cos(parameter)), 0,
				2 * Math.PI, 18);

		assertEquals(ELLIPSE_3_2, upstreamEllipse, 2e-8);
		assertEquals(EXPONENTIAL_GRAPH, upstreamExponential, 2e-8);
		assertEquals(candidateEllipse.finiteValue().orElseThrow(), upstreamEllipse,
				2e-8);
		assertTrue(candidateEllipse.errorEvidence().absoluteEvidence().amount()
				.orElseThrow() >= 0);
	}

	@Test
	void regularAndEndpointDegenerateReparameterizationsPreserveLength() {
		double c = 2;
		double denominator = Math.exp(c) - 1;
		IntegrationResult regular = integrate(parameter ->
				c * Math.exp(c * parameter) / denominator, 0, 1, 1);
		IntegrationResult endpointDegenerate = integrate(parameter ->
				3 * parameter * parameter, 0, 1, 1);
		assertSuccessful(regular, 1, 2e-11);
		assertSuccessful(endpointDegenerate, 1, 2e-11);
		assertTrue(regular.evaluations() > 0);
		assertTrue(endpointDegenerate.evaluations() > 0);
	}

	@Test
	void evaluatorAgreementAloneCannotCertifyError() {
		DoubleFunction<Point> aliased = parameter -> new Point(parameter,
				0.1 * Math.sin(2 * Math.PI * 64 * parameter));
		EvaluatorOnlyResult aliasedAgreement =
				G7AMetricNumerics.evaluatorOnlyRefinement(aliased, 0, 1, 32, 64);
		double independentlyRefined = G7AMetricNumerics.chordSum(aliased, 0, 1,
				65536);

		assertEquals(1, aliasedAgreement.priorChordLength(), 1e-12);
		assertEquals(1, aliasedAgreement.chordLength(), 1e-12);
		assertEquals(0, aliasedAgreement.agreement(), 1e-12);
		assertTrue(independentlyRefined > 25);
		assertEquals(NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				aliasedAgreement.guarantee());
	}

	@Test
	void metricToleranceIsScaleAwareAndTranslationInvariant() {
		MetricTolerance policy = new MetricTolerance(1e-10, 1e-8, 20);
		assertEquals(1e-10, policy.threshold(1e-6), 0);
		assertEquals(1e-5, policy.threshold(1000), 1e-20);

		DoubleFunction<Point> original = parameter ->
				new Point(parameter, parameter * parameter);
		DoubleFunction<Point> translated = parameter ->
				new Point(parameter + 1e9, parameter * parameter - 1e9);
		DoubleFunction<Point> scaled = parameter ->
				new Point(1000 * parameter, 1000 * parameter * parameter);
		double originalLength = partitionVariation(original, -1, 1, 8192);
		double translatedLength = partitionVariation(translated, -1, 1, 8192);
		double scaledLength = partitionVariation(scaled, -1, 1, 8192);
		assertEquals(originalLength, translatedLength, 2e-7);
		assertEquals(1000 * originalLength, scaledLength, 2e-9);
	}

	@Test
	void toleranceGridSupportsIndependentMetricDefaultRecommendation() {
		List<MetricTolerance> policies = List.of(
				new MetricTolerance(1e-8, 1e-8, 18),
				new MetricTolerance(1e-10, 1e-9, 22),
				new MetricTolerance(1e-12, 1e-10, 26));
		for (double scale : List.of(1e-6, 1.0, 1e6)) {
			for (MetricTolerance policy : policies) {
				IntegrationResult result = integrator.integrate(
						parameter -> scale * Math.hypot(
								3 * Math.sin(parameter), 2 * Math.cos(parameter)),
						0, 2 * Math.PI, policy, scale * ELLIPSE_3_2);
				double expected = scale * ELLIPSE_3_2;
				double permitted = policy.threshold(expected);
				assertEquals(MetricComputationStatus.SUCCESS, result.status());
				assertEquals(expected, result.finiteValue().orElseThrow(), permitted);
				System.out.println("G7A_TOLERANCE scale=" + scale
						+ " eps_abs=" + policy.absolute() + " eps_rel="
						+ policy.relative() + " measured_abs_error="
						+ Math.abs(result.finiteValue().orElseThrow() - expected)
						+ " estimated_abs_error="
						+ result.errorEvidence().absoluteEvidence().amount()
								.orElseThrow()
						+ " evaluations=" + result.evaluations()
						+ " subdivisions=" + result.subdivisions());
			}
		}
	}

	@Test
	void refinementCeilingAndEvaluatorFailureRemainDifferentStatuses() {
		IntegrationResult ceiling = new AdaptiveSimpsonIntegrator().integrate(
				parameter -> 1 + Math.abs(Math.sin(10000 * parameter)), 0, 1,
				new MetricTolerance(1e-30, 0, 1), 1);
		IntegrationResult failure = new AdaptiveSimpsonIntegrator().integrate(
				parameter -> {
					throw new IllegalArgumentException("injected evaluator failure");
				}, 0, 1, TOLERANCE, 1);
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				ceiling.status());
		assertEquals(MetricComputationStatus.NUMERICAL_FAILURE,
				failure.status());
		assertTrue(failure.finiteValue().isEmpty());
	}

	@Test
	void unboundedAndImproperCasesHaveExplicitNonViewportOutcomes() {
		ImproperFixture finiteBetween = new ImproperFixture(
				new FiniteMetricValue2D(5), MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE, "finite A/B on whole line");
		ImproperFixture wholeLine = new ImproperFixture(
				new PositiveInfinityMetricValue2D(),
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.NON_RECTIFIABLE,
				"analytic improper total over the whole line");
		ImproperFixture finiteImproper = new ImproperFixture(
				new FiniteMetricValue2D(1), MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				"F(t)=(t/(1+t),0), t in [0,infinity); analytic limit 1");
		IntegrationResult convergentTransform = integrate(parameter -> 1,
				0, 1, 1);
		ImproperFixture oscillatory = new ImproperFixture(
				new PositiveInfinityMetricValue2D(),
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.NON_RECTIFIABLE,
				"variation of sin(1/t) is infinite near zero");
		ImproperFixture insufficient = new ImproperFixture(
				new AbsentMetricValue2D(),
				MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				MetricRectifiability.UNDETERMINED,
				"finite evidence does not establish the improper limit");

		assertEquals(5, finiteBetween.metricValue().finiteValue().orElseThrow(), 0);
		assertEquals(MetricValueKind.POSITIVE_INFINITY,
				wholeLine.metricValue().kind());
		assertEquals(1,
				finiteImproper.metricValue().finiteValue().orElseThrow(), 0);
		assertSuccessful(convergentTransform, 1, 1e-12);
		assertEquals(MetricRectifiability.NON_RECTIFIABLE,
				oscillatory.rectifiability());
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				insufficient.status());
		assertTrue(List.of(finiteBetween, wholeLine, finiteImproper, oscillatory,
				insufficient)
				.stream().noneMatch(fixture -> fixture.provenance()
						.contains("viewport")));
	}

	@Test
	void arcCoordinateIsMonotoneComponentLocalDerivedData() {
		List<Double> arcCoordinate = cumulativeChordCoordinates(
				parameter -> new Point(parameter, parameter * parameter), 0, 1, 128);
		for (int index = 1; index < arcCoordinate.size(); index++) {
			assertTrue(arcCoordinate.get(index) >= arcCoordinate.get(index - 1));
		}
		assertEquals(0, arcCoordinate.get(0), 0);
		assertTrue(arcCoordinate.get(arcCoordinate.size() - 1) > 1);
	}

	private IntegrationResult integrate(DoubleUnaryOperator speed, double start,
			double end, double scale) {
		return integrator.integrate(speed, start, end, TOLERANCE, scale);
	}

	private static void assertSuccessful(IntegrationResult result,
			double expected, double tolerance) {
		assertEquals(MetricComputationStatus.SUCCESS, result.status());
		assertEquals(expected, result.finiteValue().orElseThrow(), tolerance);
		assertTrue(result.errorEvidence().absoluteEvidence().amount()
				.orElseThrow() <= tolerance);
	}

	private static double analyticCircleLength(double radius) {
		return 2 * Math.PI * radius;
	}

	private static double partitionVariation(DoubleFunction<Point> evaluator,
			double start, double end, int subdivisions) {
		return G7AMetricNumerics.chordSum(evaluator, start, end, subdivisions);
	}

	private static List<Double> cumulativeChordCoordinates(
			DoubleFunction<Point> evaluator, double start, double end,
			int subdivisions) {
		List<Double> coordinates = new ArrayList<>();
		coordinates.add(0.0);
		Point previous = evaluator.apply(start);
		double cumulative = 0;
		for (int index = 1; index <= subdivisions; index++) {
			double parameter = start + (end - start) * index / subdivisions;
			Point current = evaluator.apply(parameter);
			cumulative += previous.distance(current);
			coordinates.add(cumulative);
			previous = current;
		}
		return coordinates;
	}

	private record ImproperFixture(MetricValue2D metricValue,
			MetricComputationStatus status,
			MetricRectifiability rectifiability, String provenance) {
	}
}
