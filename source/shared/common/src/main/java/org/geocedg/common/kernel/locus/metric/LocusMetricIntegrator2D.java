/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Small deterministic adaptive Simpson integrator with per-call mutable state.
 */
public final class LocusMetricIntegrator2D {

	/**
	 * Integrates a non-negative speed over a finite semantic interval.
	 *
	 * @return typed integration value, evidence and work counters
	 */
	public MetricIntegrationResult2D integrate(DoubleUnaryOperator speed,
			double start, double end, LocusMetricPolicy2D policy,
			double geometricScale,
			LocusMetricInstrumentation2D instrumentation) {
		return integrate(speed, start, end, policy, geometricScale,
				instrumentation, List.of());
	}

	/**
	 * Integrates with mandatory semantic partition boundaries (for example,
	 * polynomial spline knots) under one shared deterministic work budget.
	 *
	 * @return typed integration value, evidence and work counters
	 */
	public MetricIntegrationResult2D integrate(DoubleUnaryOperator speed,
			double start, double end, LocusMetricPolicy2D policy,
			double geometricScale,
			LocusMetricInstrumentation2D instrumentation,
			List<Double> semanticBreakpoints) {
		instrumentation.recordIntegratorCall();
		double lower = Math.min(start, end);
		double upper = Math.max(start, end);
		if (!Double.isFinite(lower) || !Double.isFinite(upper)) {
			return failure(MetricComputationStatus.NUMERICAL_FAILURE,
					MetricWorkLimit2D.NONE, lower, upper, 0, 0,
					"Integrator interval is not finite");
		}
		if (lower == upper) {
			double[] parameter = {lower};
			return new MetricIntegrationResult2D(new FiniteMetricValue2D(0),
					MetricErrorEvidence2D.estimated(0, 0,
							"zero-span differential quadrature",
							List.of("finite supported speed")),
					MetricComputationStatus.SUCCESS,
					new MetricComponentPartition2D(parameter, new double[0],
							new double[0]),
					new MetricArcCoordinateEvidence2D(parameter,
							new double[] {0}),
					0, 0, MetricWorkLimit2D.NONE);
		}
		Context context = new Context(policy.getWorkBudget(), instrumentation);
		try {
			List<Double> boundaries = boundaries(lower, upper,
					semanticBreakpoints);
			Node root = new Node(0, 0);
			for (int segment = 0; segment + 1 < boundaries.size(); segment++) {
				double segmentLower = boundaries.get(segment);
				double segmentUpper = boundaries.get(segment + 1);
				double middle = midpoint(segmentLower, segmentUpper);
				double fLower = context.evaluate(speed, segmentLower);
				double fMiddle = context.evaluate(speed, middle);
				double fUpper = context.evaluate(speed, segmentUpper);
				double whole = simpson(segmentLower, segmentUpper, fLower,
						fMiddle, fUpper);
				double threshold = policy.threshold(Math.max(geometricScale,
						Math.abs(whole))) / (boundaries.size() - 1);
				Node segmentNode = refine(speed, segmentLower, segmentUpper,
						fLower, fMiddle, fUpper, whole, threshold, 0, context);
				root = new Node(root.value + segmentNode.value,
						root.error + segmentNode.error);
			}
			context.leaves.sort(Comparator.comparingDouble(leaf -> leaf.start));
			return success(root, lower, context);
		} catch (WorkLimitException exception) {
			return failure(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
					exception.limit, lower, upper, context.evaluations,
					context.subdivisions,
					"Deterministic metric work budget exhausted: "
							+ exception.limit);
		} catch (RuntimeException exception) {
			return failure(MetricComputationStatus.NUMERICAL_FAILURE,
					MetricWorkLimit2D.NONE, lower, upper, context.evaluations,
					context.subdivisions,
					"Differential quadrature failed: "
							+ exception.getClass().getSimpleName());
		}
	}

	private static List<Double> boundaries(double lower, double upper,
			List<Double> semanticBreakpoints) {
		ArrayList<Double> result = new ArrayList<>();
		result.add(lower);
		if (semanticBreakpoints != null) {
			semanticBreakpoints.stream().filter(Double::isFinite)
					.filter(value -> value > lower && value < upper)
					.sorted().distinct().forEach(result::add);
		}
		result.add(upper);
		return result;
	}

	private static Node refine(DoubleUnaryOperator speed, double start,
			double end, double fStart, double fMiddle, double fEnd,
			double whole, double tolerance, int depth, Context context) {
		double middle = midpoint(start, end);
		double leftMiddle = midpoint(start, middle);
		double rightMiddle = midpoint(middle, end);
		double fLeftMiddle = context.evaluate(speed, leftMiddle);
		double fRightMiddle = context.evaluate(speed, rightMiddle);
		double left = simpson(start, middle, fStart, fLeftMiddle, fMiddle);
		double right = simpson(middle, end, fMiddle, fRightMiddle, fEnd);
		double defect = Math.abs(left + right - whole);
		double error = defect / 15;
		if (error <= tolerance) {
			double corrected = left + right + (left + right - whole) / 15;
			context.leaves.add(new Leaf(start, end, corrected, error));
			return new Node(corrected, error);
		}
		context.beforeSubdivision(depth);
		Node leftNode = refine(speed, start, middle, fStart, fLeftMiddle,
				fMiddle, left, tolerance / 2, depth + 1, context);
		Node rightNode = refine(speed, middle, end, fMiddle, fRightMiddle,
				fEnd, right, tolerance / 2, depth + 1, context);
		return new Node(leftNode.value + rightNode.value,
				leftNode.error + rightNode.error);
	}

	private static MetricIntegrationResult2D success(Node root, double lower,
			Context context) {
		int count = context.leaves.size();
		double[] parameters = new double[count + 1];
		double[] lengths = new double[count];
		double[] errors = new double[count];
		double[] cumulative = new double[count + 1];
		parameters[0] = lower;
		for (int index = 0; index < count; index++) {
			Leaf leaf = context.leaves.get(index);
			parameters[index + 1] = leaf.end;
			lengths[index] = Math.max(0, leaf.value);
			errors[index] = Math.max(0, leaf.error);
			cumulative[index + 1] = cumulative[index] + lengths[index];
		}
		double value = Math.max(0, root.value);
		double relative = value == 0 ? 0 : root.error / value;
		return new MetricIntegrationResult2D(new FiniteMetricValue2D(value),
				MetricErrorEvidence2D.estimated(Math.max(0, root.error),
						Math.max(0, relative),
						"adaptive Simpson differential quadrature",
						List.of("finite smooth supported speed",
								"Richardson defect estimates remaining error")),
				MetricComputationStatus.SUCCESS,
				new MetricComponentPartition2D(parameters, lengths, errors),
				new MetricArcCoordinateEvidence2D(parameters, cumulative),
				context.evaluations, context.subdivisions,
				MetricWorkLimit2D.NONE);
	}

	private static MetricIntegrationResult2D failure(
			MetricComputationStatus status, MetricWorkLimit2D limit,
			double lower, double upper, long evaluations, long subdivisions,
			String message) {
		double safeLower = Double.isFinite(lower) ? lower : 0;
		double safeUpper = Double.isFinite(upper) && upper > safeLower
				? upper : Math.nextUp(safeLower);
		double[] parameters = {safeLower, safeUpper};
		return new MetricIntegrationResult2D(new AbsentMetricValue2D(),
				MetricErrorEvidence2D.notApplicable(message), status,
				new MetricComponentPartition2D(parameters, new double[] {0},
						new double[] {0}),
				new MetricArcCoordinateEvidence2D(parameters,
						new double[] {0, 0}),
				evaluations, subdivisions, limit);
	}

	private static double simpson(double start, double end, double fStart,
			double fMiddle, double fEnd) {
		double value = (end - start) * (fStart + 4 * fMiddle + fEnd) / 6;
		if (!Double.isFinite(value) || value < 0) {
			throw new ArithmeticException("Non-finite or negative Simpson value");
		}
		return value;
	}

	private static double midpoint(double start, double end) {
		return start + (end - start) / 2;
	}

	private static final class Context {
		private final MetricWorkBudget2D budget;
		private final LocusMetricInstrumentation2D instrumentation;
		private final List<Leaf> leaves = new ArrayList<>();
		private long evaluations;
		private long subdivisions;

		private Context(MetricWorkBudget2D budget,
				LocusMetricInstrumentation2D instrumentation) {
			this.budget = budget;
			this.instrumentation = instrumentation;
		}

		private double evaluate(DoubleUnaryOperator function, double parameter) {
			if (evaluations >= budget.getMaximumMetricEvaluations()) {
				throw new WorkLimitException(
						MetricWorkLimit2D.MAXIMUM_EVALUATIONS);
			}
			evaluations++;
			double value = function.applyAsDouble(parameter);
			if (!Double.isFinite(value) || value < 0) {
				throw new ArithmeticException(
						"Metric integrand must be finite and non-negative");
			}
			return value;
		}

		private void beforeSubdivision(int depth) {
			if (depth >= budget.getMaximumAdaptiveDepth()) {
				throw new WorkLimitException(MetricWorkLimit2D.MAXIMUM_DEPTH);
			}
			if (subdivisions >= budget.getMaximumMetricSubdivisions()) {
				throw new WorkLimitException(
						MetricWorkLimit2D.MAXIMUM_SUBDIVISIONS);
			}
			subdivisions++;
			instrumentation.recordSubdivision();
			instrumentation.recordRefinement();
		}
	}

	private static final class Node {
		private final double value;
		private final double error;

		private Node(double value, double error) {
			this.value = value;
			this.error = error;
		}
	}

	private static final class Leaf {
		private final double start;
		private final double end;
		private final double value;
		private final double error;

		private Leaf(double start, double end, double value, double error) {
			this.start = start;
			this.end = end;
			this.value = value;
			this.error = error;
		}
	}

	private static final class WorkLimitException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final MetricWorkLimit2D limit;

		private WorkLimitException(MetricWorkLimit2D limit) {
			this.limit = limit;
		}
	}
}
