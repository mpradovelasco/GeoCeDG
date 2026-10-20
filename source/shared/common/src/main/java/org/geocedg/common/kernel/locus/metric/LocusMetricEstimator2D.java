/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;

/**
 * Estimator v2 of class 3 ({@code locus-v2-metrics.md} section 25.3), for the
 * evaluator-only and the differential routes.
 *
 * <p>The component is first partitioned uniformly into {@code 2^d_min} cells,
 * split at known semantic breakpoints. A leaf {@code [u, v]} is accepted only
 * when its reported error does not exceed its parameter share of the component
 * tolerance. The reported error is the larger of the safety factor times the
 * difference of the Richardson-corrected estimates of two consecutive levels
 * and the inconsistency revealed by an interior probe at a non-dyadic fraction
 * of the leaf. Otherwise the leaf is split. Exhausting a budget, the depth limit
 * or the parameter resolution without acceptance is a typed failure; nothing is
 * accepted silently. Agreement of samples never produces a certified bound.</p>
 *
 * <p>Evaluator-only leaf: chords {@code c} (one), {@code f} (two) and
 * {@code f2} (four); {@code R1 = f + (f - c)/3}, {@code R2 = f2 + (f2 - f)/3};
 * the probe replaces the chord of its quarter by two chords, and an inscribed
 * polyline longer than {@code R2} shows that {@code R2} underestimates the
 * length. A vanishing quarter chord contradicts the regularity hypothesis: the
 * correction is withheld there and the leaf has no demonstrated order.</p>
 *
 * <p>Differential leaf: Simpson values {@code S1} (one panel) and {@code S2}
 * (two panels) of the speed; the probe compares the speed with its quartic
 * interpolant through the five samples.</p>
 */
final class LocusMetricEstimator2D {
	/** Estimate route. */
	enum Mode {
		EVALUATOR_ONLY, DIFFERENTIAL
	}

	/** Keeps the summed leaf errors strictly inside the summed shares. */
	private static final double SHARE_MARGIN = 1 - 0x1p-40;
	private static final double[] NODES = {0, 0.25, 0.5, 0.75, 1};
	/** Bracket reduction of the golden-section search. */
	private static final double GOLDEN_SEARCH = 0.6180339887498949;

	private final LocusDefinition2D definition;
	private final String branchKey;
	private final Mode mode;
	private final LocusDifferentialEvaluator2D differential;
	private final LocusEvaluationSession2D session;
	private final MetricEstimatorPolicy2D estimatorPolicy;
	private final MetricWorkBudget2D budget;
	private final LocusMetricInstrumentation2D instrumentation;
	private long evaluations;
	private long subdivisions;
	private int locatedReversals;
	private boolean lastRejectionByProbe;

	LocusMetricEstimator2D(LocusDefinition2D definition, String branchKey,
			Mode mode, LocusDifferentialEvaluator2D differential,
			LocusEvaluationSession2D session,
			MetricEstimatorPolicy2D estimatorPolicy, MetricWorkBudget2D budget,
			LocusMetricInstrumentation2D instrumentation) {
		this.definition = definition;
		this.branchKey = branchKey;
		this.mode = mode;
		this.differential = differential;
		this.session = session;
		this.estimatorPolicy = estimatorPolicy;
		this.budget = budget;
		this.instrumentation = instrumentation;
		if (mode == Mode.DIFFERENTIAL && differential == null) {
			throw new IllegalArgumentException("Differential mode needs a derivative");
		}
	}

	Mode getMode() {
		return mode;
	}

	long getEvaluations() {
		return evaluations;
	}

	/**
	 * Evaluates the minimum partition of {@code [lower, upper]}: {@code 2^d_min}
	 * uniform cells, split at the breakpoints strictly inside.
	 */
	Samples sample(double lower, double upper, List<Double> breakpoints) {
		double width = upper - lower;
		if (!Double.isFinite(width) || !(width > 0)) {
			throw new EstimatorFailure(MetricComputationStatus.NUMERICAL_FAILURE,
					MetricDiagnosticCode2D.NUMERICAL_FAILURE, MetricWorkLimit2D.NONE,
					"Metric component has no finite positive parameter width");
		}
		int cells = 1 << estimatorPolicy.getMinimumUniformDepth();
		ArrayList<Double> semantic = new ArrayList<>();
		if (breakpoints != null) {
			for (Double breakpoint : breakpoints) {
				if (breakpoint != null && Double.isFinite(breakpoint)
						&& breakpoint > lower && breakpoint < upper) {
					semantic.add(breakpoint);
				}
			}
		}
		// A uniform boundary that falls next to a semantic breakpoint (a knot
		// computed with another rounding) is replaced by the breakpoint, so no
		// cell is narrower than its refinement stencil.
		double separation = width / cells / 1024;
		ArrayList<Double> all = new ArrayList<>(semantic);
		for (int index = 0; index <= cells; index++) {
			double uniform = index == cells ? upper
					: lower + width * ((double) index / cells);
			boolean replaced = false;
			if (index > 0 && index < cells) {
				for (Double breakpoint : semantic) {
					replaced |= Math.abs(breakpoint - uniform) < separation;
				}
			}
			if (!replaced) {
				all.add(uniform);
			}
		}
		all.sort(Double::compare);
		ArrayList<Double> distinct = new ArrayList<>();
		for (Double value : all) {
			if (distinct.isEmpty()
					|| value.doubleValue() > distinct.get(distinct.size() - 1)) {
				distinct.add(value);
			}
		}
		ArrayList<LocusPoint2D> evaluated = new ArrayList<>();
		for (Double parameter : distinct) {
			evaluated.add(point(parameter));
		}
		if (mode == Mode.EVALUATOR_ONLY) {
			insertReversals(distinct, evaluated);
		}
		int count = distinct.size();
		double[] parameters = new double[count];
		LocusPoint2D[] points = new LocusPoint2D[count];
		double[] speeds = new double[count];
		for (int index = 0; index < count; index++) {
			parameters[index] = distinct.get(index);
			points[index] = evaluated.get(index);
			speeds[index] = mode == Mode.DIFFERENTIAL ? speed(parameters[index])
					: Double.NaN;
		}
		double extent = 0;
		double chordSum = 0;
		for (int index = 1; index < count; index++) {
			extent = Math.max(extent, distance(points[0], points[index]));
			chordSum += distance(points[index - 1], points[index]);
		}
		return new Samples(parameters, points, speeds, extent, chordSum);
	}

	/**
	 * Estimates the component on its sampled minimum partition.
	 *
	 * @param tolerance component tolerance distributed by parameter share
	 */
	Estimate estimate(Samples samples, double tolerance) {
		double[] parameters = samples.parameters;
		double width = parameters[parameters.length - 1] - parameters[0];
		ArrayList<Leaf> leaves = new ArrayList<>();
		int depth = estimatorPolicy.getMinimumUniformDepth();
		for (int index = 0; index + 1 < parameters.length; index++) {
			refine(parameters[index], parameters[index + 1], samples.points[index],
					samples.points[index + 1], samples.speeds[index],
					samples.speeds[index + 1], null, Double.NaN, depth, true,
					tolerance, width, leaves);
		}
		return new Estimate(leaves);
	}

	/**
	 * Estimates one parameter range starting from a single cell; used for the
	 * partial leaves of a between-position route (section 25.9).
	 *
	 * @param componentWidth parameter width that owns {@code tolerance}
	 */
	Estimate estimateRange(double lower, double upper, double tolerance,
			double componentWidth) {
		ArrayList<Leaf> leaves = new ArrayList<>();
		if (lower < upper) {
			refine(lower, upper, point(lower), point(upper),
					mode == Mode.DIFFERENTIAL ? speed(lower) : Double.NaN,
					mode == Mode.DIFFERENTIAL ? speed(upper) : Double.NaN, null,
					Double.NaN, 0, true, tolerance, componentWidth, leaves);
		}
		return new Estimate(leaves);
	}

	/** @return recorded hypotheses of an estimate (section 25.3d, N3) */
	List<String> hypotheses(Estimate estimate) {
		ArrayList<String> result = new ArrayList<>();
		if (mode == Mode.EVALUATOR_ONLY) {
			result.add("curve is C4 with nonvanishing speed on every accepted leaf "
					+ "(Richardson chord expansion s - c = kappa^2 s^3/24 + O(s^5))");
		} else {
			result.add("speed is C4 on every accepted leaf (Simpson error "
					+ "expansion)");
		}
		result.add("every accepted leaf lies in the asymptotic regime of that "
				+ "expansion");
		result.add("no feature narrower than h_probe = "
				+ estimate.probeSpacing + " in the driver parameter");
		result.add("minimum partition 2^" + estimatorPolicy.getMinimumUniformDepth()
				+ " cells, probe fraction " + estimatorPolicy.getProbeFraction()
				+ ", safety factor " + estimatorPolicy.getSafetyFactor() + " ("
				+ estimatorPolicy.getVersion() + ")");
		result.add("no convergence order is claimed");
		if (mode == Mode.EVALUATOR_ONLY) {
			result.add("no unlocated direction reversal between consecutive samples; "
					+ locatedReversals + " reversal(s) located by golden-section search "
					+ "of the projection extremum and used as partition boundaries");
		}
		if (estimate.subResolutionCells > 0) {
			result.add(estimate.subResolutionCells + " cell(s) narrower than the "
					+ "refinement stencil accepted as chords: no feature narrower "
					+ "than the parameter resolution");
		}
		if (estimate.unorderedLeaves > 0) {
			result.add((mode == Mode.EVALUATOR_ONLY
					? "correction withheld on " + estimate.unorderedLeaves
							+ " leaves with a vanishing chord"
					: "vanishing speed sampled on " + estimate.unorderedLeaves
							+ " leaves")
					+ ": estimate without demonstrated order");
		}
		return Collections.unmodifiableList(result);
	}

	private void refine(double lower, double upper, LocusPoint2D start,
			LocusPoint2D end, double startSpeed, double endSpeed,
			LocusPoint2D knownMiddle, double knownMiddleSpeed, int depth,
			boolean initialCell, double tolerance, double componentWidth,
			List<Leaf> leaves) {
		double middle = midpoint(lower, upper);
		double firstQuarter = midpoint(lower, middle);
		double thirdQuarter = midpoint(middle, upper);
		double probe = lower + (upper - lower) * estimatorPolicy.getProbeFraction();
		double share = tolerance * ((upper - lower) / componentWidth)
				* SHARE_MARGIN;
		if (!(lower < firstQuarter && firstQuarter < middle && middle < probe
				&& probe < thirdQuarter && thirdQuarter < upper)) {
			if (initialCell) {
				// A cell born narrower than its stencil (a route end a few ulps
				// from a leaf boundary or knot) has no representable interior
				// parameter. Its chord, or its trapezoid, is accepted under the
				// recorded hypothesis that no feature is narrower than the
				// parameter resolution, without a demonstrated order.
				double value = mode == Mode.EVALUATOR_ONLY ? distance(start, end)
						: (upper - lower) * (startSpeed / 2 + endSpeed / 2);
				if (Double.isFinite(value)) {
					leaves.add(new Leaf(lower, upper, Math.max(0, value), 0, true,
							true));
					return;
				}
			}
			throw new EstimatorFailure(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
					MetricDiagnosticCode2D.PARAMETER_RESOLUTION_REACHED,
					MetricWorkLimit2D.NONE,
					"Parameter resolution reached without an accepted leaf at ["
							+ lower + ", " + upper + "]");
		}
		if (mode == Mode.EVALUATOR_ONLY) {
			LocusPoint2D mid = knownMiddle != null ? knownMiddle : point(middle);
			LocusPoint2D q1 = point(firstQuarter);
			LocusPoint2D q3 = point(thirdQuarter);
			// A direction reversal inside the leaf hides the same overshoot from
			// every level; the leaf is split at the located reversal instead.
			LocusPoint2D[] chain = {start, q1, mid, q3, end};
			double[] at = {lower, firstQuarter, middle, thirdQuarter, upper};
			for (int index = 1; index <= 3; index++) {
				if (isReversal(chain[index - 1], chain[index], chain[index + 1])) {
					double split = locateReversal(at[index - 1], at[index + 1],
							chain[index - 1], chain[index]);
					if (canHostStencil(lower, split) && canHostStencil(split, upper)) {
						lastRejectionByProbe = false;
						beforeSubdivision(depth);
						LocusPoint2D turn = point(split);
						locatedReversals++;
						refine(lower, split, start, turn, Double.NaN, Double.NaN, null,
								Double.NaN, depth + 1, false, tolerance, componentWidth,
								leaves);
						refine(split, upper, turn, end, Double.NaN, Double.NaN, null,
								Double.NaN, depth + 1, false, tolerance, componentWidth,
								leaves);
						return;
					}
				}
			}
			LocusPoint2D p = point(probe);
			double coarse = distance(start, end);
			double fine = distance(start, mid) + distance(mid, end);
			double a1 = distance(start, q1);
			double a2 = distance(q1, mid);
			double a3 = distance(mid, q3);
			double a4 = distance(q3, end);
			double finest = a1 + a2 + a3 + a4;
			double probed = a1 + a2 + distance(mid, p) + distance(p, q3) + a4;
			boolean vanishing = a1 == 0 || a2 == 0 || a3 == 0 || a4 == 0;
			double value;
			double levelError;
			double probeError;
			if (vanishing) {
				value = finest;
				levelError = estimatorPolicy.getSafetyFactor()
						* Math.abs(finest - fine);
				probeError = Math.max(0, probed - finest);
			} else {
				double corrected1 = fine + (fine - coarse) / 3;
				double corrected2 = finest + (finest - fine) / 3;
				value = corrected2;
				levelError = estimatorPolicy.getSafetyFactor()
						* Math.abs(corrected2 - corrected1);
				probeError = Math.max(0, probed - corrected2);
			}
			double error = Math.max(levelError, probeError);
			if (Double.isFinite(value) && Double.isFinite(error) && error <= share) {
				leaves.add(new Leaf(lower, upper, Math.max(0, value), error,
						vanishing));
				return;
			}
			lastRejectionByProbe = probeError > share && levelError <= share;
			beforeSubdivision(depth);
			refine(lower, middle, start, mid, Double.NaN, Double.NaN, q1,
					Double.NaN, depth + 1, false, tolerance, componentWidth, leaves);
			refine(middle, upper, mid, end, Double.NaN, Double.NaN, q3,
					Double.NaN, depth + 1, false, tolerance, componentWidth, leaves);
			return;
		}
		double sMid = Double.isNaN(knownMiddleSpeed) ? speed(middle)
				: knownMiddleSpeed;
		double sQ1 = speed(firstQuarter);
		double sQ3 = speed(thirdQuarter);
		double sProbe = speed(probe);
		double width = upper - lower;
		double simpson1 = width * (startSpeed + 4 * sMid + endSpeed) / 6;
		double simpson2 = width
				* (startSpeed + 4 * sQ1 + 2 * sMid + 4 * sQ3 + endSpeed) / 12;
		double fraction = (probe - lower) / width;
		double[] samples = {startSpeed, sQ1, sMid, sQ3, endSpeed};
		double interpolated = 0;
		for (int node = 0; node < NODES.length; node++) {
			double weight = 1;
			for (int other = 0; other < NODES.length; other++) {
				if (other != node) {
					weight *= (fraction - NODES[other]) / (NODES[node] - NODES[other]);
				}
			}
			interpolated += weight * samples[node];
		}
		boolean vanishing = startSpeed == 0 || sQ1 == 0 || sMid == 0 || sQ3 == 0
				|| endSpeed == 0 || sProbe == 0;
		double levelError = estimatorPolicy.getSafetyFactor()
				* Math.abs(simpson2 - simpson1);
		double probeError = Math.abs(sProbe - interpolated) * width;
		double error = Math.max(levelError, probeError);
		if (Double.isFinite(simpson2) && Double.isFinite(error) && error <= share) {
			leaves.add(new Leaf(lower, upper, Math.max(0, simpson2), error,
					vanishing));
			return;
		}
		lastRejectionByProbe = probeError > share && levelError <= share;
		beforeSubdivision(depth);
		refine(lower, middle, start, null, startSpeed, sMid, null, sQ1,
				depth + 1, false, tolerance, componentWidth, leaves);
		refine(middle, upper, null, end, sMid, endSpeed, null, sQ3, depth + 1,
				false, tolerance, componentWidth, leaves);
	}

	/**
	 * Locates the direction reversals of the sampled polyline (consecutive chords
	 * at more than a right angle) and inserts each located parameter, with its
	 * point, as a partition boundary.
	 */
	private void insertReversals(List<Double> parameters, List<LocusPoint2D> points) {
		ArrayList<Double> located = new ArrayList<>();
		for (int index = 1; index + 1 < parameters.size(); index++) {
			if (isReversal(points.get(index - 1), points.get(index),
					points.get(index + 1))) {
				located.add(locateReversal(parameters.get(index - 1),
						parameters.get(index + 1), points.get(index - 1),
						points.get(index)));
				// One reversal shows at one or two consecutive samples.
				index++;
			}
		}
		for (double parameter : located) {
			int insertion = 0;
			while (insertion < parameters.size()
					&& parameters.get(insertion) < parameter) {
				insertion++;
			}
			if (insertion > 0 && insertion < parameters.size()
					&& parameters.get(insertion) != parameter) {
				parameters.add(insertion, parameter);
				points.add(insertion, point(parameter));
				locatedReversals++;
			}
		}
	}

	/**
	 * @return whether the chords {@code p0 p1} and {@code p1 p2} turn by more than
	 *         a right angle, both chords exceeding the rounding noise of the points
	 */
	private static boolean isReversal(LocusPoint2D p0, LocusPoint2D p1,
			LocusPoint2D p2) {
		double ax = p1.getX() - p0.getX();
		double ay = p1.getY() - p0.getY();
		double bx = p2.getX() - p1.getX();
		double by = p2.getY() - p1.getY();
		double scale = Math.max(Math.max(Math.abs(p0.getX()), Math.abs(p0.getY())),
				Math.max(Math.max(Math.abs(p1.getX()), Math.abs(p1.getY())),
						Math.max(Math.abs(p2.getX()), Math.abs(p2.getY()))));
		double noise = 64 * Math.ulp(scale);
		return Math.hypot(ax, ay) > noise && Math.hypot(bx, by) > noise
				&& ax * bx + ay * by < 0;
	}

	/**
	 * Golden-section search of the extremum of the projection of the curve on the
	 * incoming chord direction {@code anchor -> towards}, over a bracket that
	 * contains one reversal.
	 *
	 * @return located reversal parameter inside {@code (lower, upper)}
	 */
	private double locateReversal(double lower, double upper, LocusPoint2D anchor,
			LocusPoint2D towards) {
		double dx = towards.getX() - anchor.getX();
		double dy = towards.getY() - anchor.getY();
		double low = lower;
		double high = upper;
		double resolution = Math.max(
				8 * Math.ulp(Math.max(Math.abs(lower), Math.abs(upper))),
				(upper - lower) * 1E-12);
		double left = high - GOLDEN_SEARCH * (high - low);
		double right = low + GOLDEN_SEARCH * (high - low);
		double leftValue = projection(left, anchor, dx, dy);
		double rightValue = projection(right, anchor, dx, dy);
		while (high - low > resolution && low < left && left < right
				&& right < high) {
			if (leftValue >= rightValue) {
				high = right;
				right = left;
				rightValue = leftValue;
				left = high - GOLDEN_SEARCH * (high - low);
				leftValue = projection(left, anchor, dx, dy);
			} else {
				low = left;
				left = right;
				leftValue = rightValue;
				right = low + GOLDEN_SEARCH * (high - low);
				rightValue = projection(right, anchor, dx, dy);
			}
		}
		return leftValue >= rightValue ? left : right;
	}

	private double projection(double parameter, LocusPoint2D anchor, double dx,
			double dy) {
		LocusPoint2D value = point(parameter);
		return (value.getX() - anchor.getX()) * dx
				+ (value.getY() - anchor.getY()) * dy;
	}

	/** @return whether {@code [lower, upper]} can host the refinement stencil */
	private boolean canHostStencil(double lower, double upper) {
		double middle = midpoint(lower, upper);
		double firstQuarter = midpoint(lower, middle);
		double thirdQuarter = midpoint(middle, upper);
		double probe = lower + (upper - lower) * estimatorPolicy.getProbeFraction();
		return lower < firstQuarter && firstQuarter < middle && middle < probe
				&& probe < thirdQuarter && thirdQuarter < upper;
	}

	private LocusPoint2D point(double parameter) {
		chargeEvaluation();
		instrumentation.recordEvaluatorCall();
		LocusEvaluation2D evaluation =
				definition.evaluate(branchKey, parameter, session);
		if (!evaluation.isValid()) {
			throw new EstimatorFailure(MetricComputationStatus.NUMERICAL_FAILURE,
					MetricDiagnosticCode2D.EVALUATION_FAILED, MetricWorkLimit2D.NONE,
					evaluation.getDiagnostic());
		}
		return evaluation.getPoint();
	}

	private double speed(double parameter) {
		chargeEvaluation();
		instrumentation.recordDerivativeCall();
		LocusDifferentialEvaluation2D evaluation = differential
				.evaluateDifferential(definition, branchKey, parameter, session);
		if (evaluation == null || !evaluation.isValid()) {
			throw new EstimatorFailure(MetricComputationStatus.NUMERICAL_FAILURE,
					MetricDiagnosticCode2D.EVALUATION_FAILED, MetricWorkLimit2D.NONE,
					evaluation == null ? "Differential evaluator returned null"
							: evaluation.getDiagnostic());
		}
		double speed = evaluation.getSpeed();
		if (!Double.isFinite(speed) || speed < 0) {
			throw new EstimatorFailure(MetricComputationStatus.NUMERICAL_FAILURE,
					MetricDiagnosticCode2D.NUMERICAL_FAILURE, MetricWorkLimit2D.NONE,
					"Metric speed must be finite and non-negative");
		}
		return speed;
	}

	private void chargeEvaluation() {
		if (evaluations >= budget.getMaximumMetricEvaluations()) {
			throw limit(MetricWorkLimit2D.MAXIMUM_EVALUATIONS);
		}
		evaluations++;
	}

	private void beforeSubdivision(int depth) {
		if (depth >= budget.getMaximumAdaptiveDepth()) {
			throw limit(MetricWorkLimit2D.MAXIMUM_DEPTH);
		}
		if (subdivisions >= budget.getMaximumMetricSubdivisions()) {
			throw limit(MetricWorkLimit2D.MAXIMUM_SUBDIVISIONS);
		}
		subdivisions++;
		instrumentation.recordSubdivision();
		instrumentation.recordRefinement();
	}

	private EstimatorFailure limit(MetricWorkLimit2D dimension) {
		if (lastRejectionByProbe) {
			return new EstimatorFailure(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
					MetricDiagnosticCode2D.CONSISTENCY_NOT_ESTABLISHED, dimension,
					"Probe consistency was not established within " + dimension);
		}
		return new EstimatorFailure(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED, dimension,
				"Metric estimate exceeded " + dimension);
	}

	private static double distance(LocusPoint2D first, LocusPoint2D second) {
		return Math.hypot(first.getX() - second.getX(),
				first.getY() - second.getY());
	}

	private static double midpoint(double start, double end) {
		return start + (end - start) / 2;
	}

	/** Positions (and speeds) of the minimum partition. */
	static final class Samples {
		private final double[] parameters;
		private final LocusPoint2D[] points;
		private final double[] speeds;
		private final double extent;
		private final double chordSum;

		private Samples(double[] parameters, LocusPoint2D[] points,
				double[] speeds, double extent, double chordSum) {
			this.parameters = parameters;
			this.points = points;
			this.speeds = speeds;
			this.extent = extent;
			this.chordSum = chordSum;
		}

		/** @return boundaries of the minimum partition */
		double[] getParameters() {
			return parameters.clone();
		}

		/**
		 * @return translation- and rotation-invariant extent of the component
		 *         image: largest sampled distance from the component start
		 */
		double getExtent() {
			return extent;
		}

		/** @return length of the inscribed sample polyline */
		double getChordSum() {
			return chordSum;
		}
	}

	/** Accepted leaves of one estimate. */
	static final class Estimate {
		private final double[] parameters;
		private final double[] values;
		private final double[] errors;
		private final double value;
		private final double error;
		private final int unorderedLeaves;
		private final int subResolutionCells;
		private final double probeSpacing;

		private Estimate(List<Leaf> leaves) {
			int count = leaves.size();
			parameters = new double[count + 1];
			values = new double[count];
			errors = new double[count];
			double sum = 0;
			double compensation = 0;
			double errorSum = 0;
			int unordered = 0;
			int subResolution = 0;
			double spacing = 0;
			parameters[0] = count == 0 ? 0 : leaves.get(0).lower;
			for (int index = 0; index < count; index++) {
				Leaf leaf = leaves.get(index);
				parameters[index + 1] = leaf.upper;
				values[index] = leaf.value;
				errors[index] = leaf.error;
				double next = sum + leaf.value;
				compensation += Math.abs(sum) >= Math.abs(leaf.value)
						? (sum - next) + leaf.value : (leaf.value - next) + sum;
				sum = next;
				errorSum = index == 0 ? leaf.error
						: MetricOutwardArithmetic2D.addUp(errorSum, leaf.error);
				if (leaf.unordered) {
					unordered++;
				}
				if (leaf.subResolution) {
					subResolution++;
				}
				spacing = Math.max(spacing, (leaf.upper - leaf.lower) / 4);
			}
			value = Math.max(0, sum + compensation);
			error = Math.max(0, errorSum);
			unorderedLeaves = unordered;
			subResolutionCells = subResolution;
			probeSpacing = spacing;
		}

		int getSubResolutionCells() {
			return subResolutionCells;
		}

		double[] getParameters() {
			return parameters.clone();
		}

		double[] getValues() {
			return values.clone();
		}

		double[] getErrors() {
			return errors.clone();
		}

		int getLeafCount() {
			return values.length;
		}

		double getValue() {
			return value;
		}

		double getError() {
			return error;
		}

		int getUnorderedLeaves() {
			return unorderedLeaves;
		}

		double getProbeSpacing() {
			return probeSpacing;
		}
	}

	private static final class Leaf {
		private final double lower;
		private final double upper;
		private final double value;
		private final double error;
		private final boolean unordered;
		private final boolean subResolution;

		private Leaf(double lower, double upper, double value, double error,
				boolean unordered) {
			this(lower, upper, value, error, unordered, false);
		}

		private Leaf(double lower, double upper, double value, double error,
				boolean unordered, boolean subResolution) {
			this.lower = lower;
			this.upper = upper;
			this.value = value;
			this.error = error;
			this.unordered = unordered;
			this.subResolution = subResolution;
		}
	}

	/** Typed estimator failure (sections 25.3e and 25.7). */
	static final class EstimatorFailure extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final MetricComputationStatus status;
		private final MetricDiagnosticCode2D code;
		private final MetricWorkLimit2D workLimit;

		EstimatorFailure(MetricComputationStatus status, MetricDiagnosticCode2D code,
				MetricWorkLimit2D workLimit, String message) {
			super(message == null || message.trim().isEmpty() ? code.toString()
					: message);
			this.status = status;
			this.code = code;
			this.workLimit = workLimit;
		}

		MetricComputationStatus getStatus() {
			return status;
		}

		/** @return typed diagnostic carrying the exhausted work dimension */
		MetricDiagnostic2D toDiagnostic() {
			return new MetricDiagnostic2D(code, getMessage(), workLimit);
		}

		LocusMetricComponentBuildException toBuildException() {
			return new LocusMetricComponentBuildException(status,
					Collections.singletonList(toDiagnostic()));
		}
	}
}
