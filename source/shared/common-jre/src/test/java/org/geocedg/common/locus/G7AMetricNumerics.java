/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.locus.G7AMetricSemanticModel.AbsentMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.FiniteMetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricComputationStatus;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricErrorEvidence2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.MetricValue2D;
import org.geocedg.common.locus.G7AMetricSemanticModel.Point;

/** Test-private deterministic numerical candidates for G7A experiments. */
final class G7AMetricNumerics {

	private G7AMetricNumerics() {
		// Utility class.
	}

	record MetricWorkBudget(long maximumEvaluations, long maximumSubdivisions,
			int maximumDepth) {
		MetricWorkBudget {
			if (maximumEvaluations < 3 || maximumSubdivisions < 1
					|| maximumDepth < 1) {
				throw new IllegalArgumentException("Invalid metric work budget");
			}
		}
	}

	record MetricTolerance(double absolute, double relative,
			MetricWorkBudget workBudget) {
		MetricTolerance {
			if (!(absolute > 0) || !(relative >= 0) || workBudget == null) {
				throw new IllegalArgumentException("Invalid metric tolerance");
			}
		}

		MetricTolerance(double absolute, double relative, int maximumDepth) {
			this(absolute, relative,
					new MetricWorkBudget(1_000_001, 500_000, maximumDepth));
		}

		double threshold(double geometricScale) {
			return Math.max(absolute, relative * Math.abs(geometricScale));
		}
	}

	enum ExhaustedWorkLimit {
		NONE,
		MAXIMUM_EVALUATIONS,
		MAXIMUM_SUBDIVISIONS,
		MAXIMUM_DEPTH
	}

	record IntegrationResult(MetricValue2D metricValue,
			MetricErrorEvidence2D errorEvidence,
			MetricComputationStatus status, long evaluations, long subdivisions,
			ExhaustedWorkLimit exhaustedWorkLimit) {
		OptionalDouble finiteValue() {
			return metricValue.finiteValue();
		}

		Optional<NumericGuarantee> numericGuarantee() {
			return errorEvidence.numericGuarantee();
		}
	}

	static final class AdaptiveSimpsonIntegrator {
		IntegrationResult integrate(DoubleUnaryOperator integrand, double start,
				double end, MetricTolerance tolerance, double geometricScale) {
			Counters counters = new Counters();
			try {
				MetricWorkBudget budget = tolerance.workBudget();
				double a = Math.min(start, end);
				double b = Math.max(start, end);
				double middle = (a + b) / 2;
				double fa = evaluate(integrand, a, budget, counters);
				double fm = evaluate(integrand, middle, budget, counters);
				double fb = evaluate(integrand, b, budget, counters);
				double whole = simpson(a, b, fa, fm, fb);
				Node result = refine(integrand, a, b, fa, fm, fb, whole,
						tolerance.threshold(geometricScale),
						budget.maximumDepth(), budget, counters);
				MetricComputationStatus status = result.established
						? MetricComputationStatus.SUCCESS
						: MetricComputationStatus.LIMIT_NOT_ESTABLISHED;
				MetricValue2D value = result.established
						? new FiniteMetricValue2D(result.value)
						: new AbsentMetricValue2D();
				MetricErrorEvidence2D evidence = result.established
						? MetricErrorEvidence2D.estimated(result.error,
								result.value > 0
										? OptionalDouble.of(result.error / result.value)
										: OptionalDouble.empty(),
								"adaptive Simpson differential quadrature",
								List.of("smooth supported speed on finite interval"))
						: MetricErrorEvidence2D.notApplicable(
								"maximum depth exhausted before requested tolerance");
				return new IntegrationResult(value, evidence, status,
						counters.evaluations, counters.subdivisions,
						result.exhaustedWorkLimit);
			} catch (WorkBudgetExceeded exception) {
				return new IntegrationResult(new AbsentMetricValue2D(),
						MetricErrorEvidence2D.notApplicable(
								"deterministic work budget exhausted"),
						MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
						counters.evaluations, counters.subdivisions,
						exception.limit);
			} catch (ArithmeticException | IllegalArgumentException exception) {
				return new IntegrationResult(new AbsentMetricValue2D(),
						MetricErrorEvidence2D.notApplicable(
								"numerical or evaluator failure"),
						MetricComputationStatus.NUMERICAL_FAILURE,
						counters.evaluations, counters.subdivisions,
						ExhaustedWorkLimit.NONE);
			}
		}

		private static Node refine(DoubleUnaryOperator integrand, double a,
				double b, double fa, double fm, double fb, double whole,
				double tolerance, int depth, MetricWorkBudget budget,
				Counters counters) {
			double middle = (a + b) / 2;
			double leftMiddle = (a + middle) / 2;
			double rightMiddle = (middle + b) / 2;
			double flm = evaluate(integrand, leftMiddle, budget, counters);
			double frm = evaluate(integrand, rightMiddle, budget, counters);
			double left = simpson(a, middle, fa, flm, fm);
			double right = simpson(middle, b, fm, frm, fb);
			double delta = left + right - whole;
			if (counters.subdivisions >= budget.maximumSubdivisions()) {
				throw new WorkBudgetExceeded(
						ExhaustedWorkLimit.MAXIMUM_SUBDIVISIONS);
			}
			counters.subdivisions++;
			if (Math.abs(delta) <= 15 * tolerance) {
				return new Node(left + right + delta / 15,
						Math.abs(delta) / 15, true, ExhaustedWorkLimit.NONE);
			}
			if (depth == 0) {
				return new Node(left + right, Math.abs(delta) / 15, false,
						ExhaustedWorkLimit.MAXIMUM_DEPTH);
			}
			Node leftResult = refine(integrand, a, middle, fa, flm, fm,
					left, tolerance / 2, depth - 1, budget, counters);
			Node rightResult = refine(integrand, middle, b, fm, frm, fb,
					right, tolerance / 2, depth - 1, budget, counters);
			return new Node(leftResult.value + rightResult.value,
					leftResult.error + rightResult.error,
					leftResult.established && rightResult.established,
					leftResult.established ? rightResult.exhaustedWorkLimit
							: leftResult.exhaustedWorkLimit);
		}

		private static double evaluate(DoubleUnaryOperator integrand,
				double parameter, MetricWorkBudget budget, Counters counters) {
			if (counters.evaluations >= budget.maximumEvaluations()) {
				throw new WorkBudgetExceeded(
						ExhaustedWorkLimit.MAXIMUM_EVALUATIONS);
			}
			counters.evaluations++;
			double value = integrand.applyAsDouble(parameter);
			if (!Double.isFinite(value) || value < 0) {
				throw new ArithmeticException("Non-finite or negative speed");
			}
			return value;
		}

		private static double simpson(double a, double b, double fa,
				double fm, double fb) {
			return (b - a) * (fa + 4 * fm + fb) / 6;
		}
	}

	record EvaluatorOnlyResult(double chordLength, double priorChordLength,
			double agreement, NumericGuarantee guarantee, long evaluatorCalls,
			int subdivisions) {
	}

	static EvaluatorOnlyResult evaluatorOnlyRefinement(
			DoubleFunction<Point> evaluator, double start, double end,
			int priorSubdivisions, int subdivisions) {
		double prior = chordSum(evaluator, start, end, priorSubdivisions);
		double current = chordSum(evaluator, start, end, subdivisions);
		return new EvaluatorOnlyResult(current, prior,
				Math.abs(current - prior),
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				priorSubdivisions + 1L + subdivisions + 1L, subdivisions);
	}

	static double chordSum(DoubleFunction<Point> evaluator, double start,
			double end, int subdivisions) {
		Point previous = evaluator.apply(start);
		double length = 0;
		for (int index = 1; index <= subdivisions; index++) {
			double parameter = start + (end - start) * index / subdivisions;
			Point current = evaluator.apply(parameter);
			length += previous.distance(current);
			previous = current;
		}
		return length;
	}

	private static final class Counters {
		private long evaluations;
		private long subdivisions;
	}

	private static final class WorkBudgetExceeded extends RuntimeException {
		private final ExhaustedWorkLimit limit;

		WorkBudgetExceeded(ExhaustedWorkLimit limit) {
			this.limit = limit;
		}
	}

	private record Node(double value, double error, boolean established,
			ExhaustedWorkLimit exhaustedWorkLimit) {
	}
}
