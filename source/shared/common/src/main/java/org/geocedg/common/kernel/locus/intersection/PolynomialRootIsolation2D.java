/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Deterministic derivative-partition isolation for one real polynomial.
 *
 * <p>Coefficients use ascending power order. The implementation recursively
 * isolates derivative roots, partitions the requested interval at those roots
 * and bisects every sign-changing monotone cell. A root cell is floating
 * evidence, not an exact-arithmetic or interval-arithmetic certificate.</p>
 */
public final class PolynomialRootIsolation2D {
	private static final double COEFFICIENT_EPSILON = 512 * Math.ulp(1.0);

	/** Work events emitted by the deterministic isolation process. */
	public interface WorkRecorder {
		/** Records one sign-changing cell and its historical isolation depth. */
		void recordIsolationSubdivision(int depth);

		/** Records the start of one bisection refinement. */
		void recordRefinementStarted();

		/** Records one bisection iteration, numbered from one. */
		void recordRefinementIteration(long iteration);
	}

	/** Immutable floating root-isolation cell. */
	public static final class RootCell {
		private final double parameter;
		private final double lower;
		private final double upper;

		private RootCell(double parameter, double lower, double upper) {
			this.parameter = parameter;
			this.lower = Math.min(lower, parameter);
			this.upper = Math.max(upper, parameter);
		}

		/** @return representative root parameter */
		public double getParameter() {
			return parameter;
		}

		/** @return lower boundary of the floating isolation cell */
		public double getLower() {
			return lower;
		}

		/** @return upper boundary of the floating isolation cell */
		public double getUpper() {
			return upper;
		}
	}

	/** Immutable isolation outcome, including explicit zero-polynomial evidence. */
	public static final class IsolationResult {
		private final boolean zeroPolynomial;
		private final List<RootCell> rootCells;

		private IsolationResult(boolean zeroPolynomial,
				List<MutableRootCell> roots) {
			this.zeroPolynomial = zeroPolynomial;
			ArrayList<RootCell> immutableRoots = new ArrayList<>(roots.size());
			for (MutableRootCell root : roots) {
				immutableRoots.add(new RootCell(root.parameter, root.lower,
						root.upper));
			}
			rootCells = Collections.unmodifiableList(immutableRoots);
		}

		/**
		 * @return whether the polynomial is identically zero under the shared
		 *         coefficient policy
		 */
		public boolean isZeroPolynomial() {
			return zeroPolynomial;
		}

		/** @return immutable root cells in ascending parameter order */
		public List<RootCell> getCells() {
			return rootCells;
		}
	}

	private PolynomialRootIsolation2D() {
		// Utility class.
	}

	/**
	 * Isolates roots on one finite interval using the historical GeoCeDG
	 * derivative-partition policy.
	 *
	 * @param polynomial coefficients in ascending power order
	 * @param lower finite lower interval boundary
	 * @param upper finite upper interval boundary
	 * @param parameterTolerance positive root-parameter tolerance
	 * @param maximumRefinementIterations maximum bisection iterations
	 * @param recorder deterministic work recorder
	 * @return immutable root cells and zero-polynomial evidence
	 */
	public static IsolationResult isolate(double[] polynomial, double lower,
			double upper, double parameterTolerance,
			int maximumRefinementIterations,
			WorkRecorder recorder) {
		if (polynomial == null || polynomial.length == 0) {
			throw new IllegalArgumentException(
					"Polynomial coefficients are required");
		}
		if (!Double.isFinite(lower) || !Double.isFinite(upper)
				|| !(lower < upper)) {
			throw new IllegalArgumentException(
					"Root-isolation interval must be finite and nonempty");
		}
		if (!Double.isFinite(parameterTolerance)
				|| !(parameterTolerance > 0)) {
			throw new IllegalArgumentException(
					"Root-parameter tolerance must be finite and positive");
		}
		if (maximumRefinementIterations < 0) {
			throw new IllegalArgumentException(
					"Maximum refinement iterations must be nonnegative");
		}
		if (recorder == null) {
			throw new IllegalArgumentException("Work recorder is required");
		}
		NormalizedPolynomial normalized = normalize(polynomial);
		if (normalized.zero) {
			return new IsolationResult(true, Collections.emptyList());
		}
		return new IsolationResult(false, roots(normalized.coefficients, lower,
				upper, parameterTolerance, maximumRefinementIterations, recorder));
	}

	private static List<MutableRootCell> roots(double[] polynomial,
			double lower, double upper, double parameterTolerance,
			int maximumRefinementIterations, WorkRecorder recorder) {
		NormalizedPolynomial normalized = normalize(polynomial);
		if (normalized.zero || normalized.coefficients.length == 1) {
			return Collections.emptyList();
		}
		int degree = normalized.coefficients.length - 1;
		if (degree == 1) {
			double root = -normalized.coefficients[0]
					/ normalized.coefficients[1];
			if (!Double.isFinite(root) || root < lower - parameterTolerance
					|| root > upper + parameterTolerance) {
				return Collections.emptyList();
			}
			root = Math.max(lower, Math.min(upper, root));
			return List.of(new MutableRootCell(root, root, root));
		}
		double[] derivative = new double[degree];
		for (int power = 1; power <= degree; power++) {
			derivative[power - 1] = power * normalized.coefficients[power];
		}
		ArrayList<Double> partition = new ArrayList<>();
		partition.add(lower);
		for (MutableRootCell critical : roots(derivative, lower, upper,
				parameterTolerance, maximumRefinementIterations, recorder)) {
			if (critical.parameter > lower && critical.parameter < upper) {
				partition.add(critical.parameter);
			}
		}
		partition.add(upper);
		partition.sort(Double::compare);
		partition = unique(partition,
				Math.max(Math.ulp(1.0) * 32, parameterTolerance));
		ArrayList<MutableRootCell> result = new ArrayList<>();
		double valueTolerance = COEFFICIENT_EPSILON
				* Math.max(1, normalized.coefficients.length);
		for (int index = 0; index < partition.size(); index++) {
			double point = partition.get(index);
			if (Math.abs(evaluate(normalized.coefficients, point))
					<= valueTolerance) {
				double cellLower = index == 0 ? point
						: partition.get(index - 1);
				double cellUpper = index + 1 == partition.size() ? point
						: partition.get(index + 1);
				result.add(new MutableRootCell(point, cellLower, cellUpper));
			}
		}
		for (int index = 0; index + 1 < partition.size(); index++) {
			double left = partition.get(index);
			double right = partition.get(index + 1);
			double leftValue = evaluate(normalized.coefficients, left);
			double rightValue = evaluate(normalized.coefficients, right);
			if (!oppositeSigns(leftValue, rightValue)) {
				continue;
			}
			recorder.recordIsolationSubdivision(1);
			result.add(bisect(normalized.coefficients, left, right,
					parameterTolerance, maximumRefinementIterations, recorder));
		}
		return deduplicateCells(result, parameterTolerance);
	}

	private static MutableRootCell bisect(double[] polynomial,
			double initialLower, double initialUpper, double parameterTolerance,
			int maximumRefinementIterations, WorkRecorder recorder) {
		recorder.recordRefinementStarted();
		double lower = initialLower;
		double upper = initialUpper;
		double lowerValue = evaluate(polynomial, lower);
		for (long iteration = 1; iteration <= maximumRefinementIterations;
				iteration++) {
			recorder.recordRefinementIteration(iteration);
			if (upper - lower <= parameterTolerance) {
				break;
			}
			double middle = lower + (upper - lower) / 2;
			double value = evaluate(polynomial, middle);
			if (value == 0) {
				return new MutableRootCell(middle, middle, middle);
			}
			if (oppositeSigns(lowerValue, value)) {
				upper = middle;
			} else {
				lower = middle;
				lowerValue = value;
			}
		}
		if (upper - lower > parameterTolerance) {
			throw new LocusIntersectionWorkLimitException(
					"polynomial root refinement");
		}
		double parameter = lower + (upper - lower) / 2;
		return new MutableRootCell(parameter, lower, upper);
	}

	private static ArrayList<Double> unique(List<Double> input,
			double tolerance) {
		ArrayList<Double> result = new ArrayList<>();
		for (double value : input) {
			if (result.isEmpty()
					|| Math.abs(value - result.get(result.size() - 1)) > tolerance) {
				result.add(value);
			}
		}
		return result;
	}

	private static List<MutableRootCell> deduplicateCells(
			List<MutableRootCell> input, double tolerance) {
		input.sort(Comparator.comparingDouble(root -> root.parameter));
		ArrayList<MutableRootCell> result = new ArrayList<>();
		for (MutableRootCell candidate : input) {
			if (!result.isEmpty() && Math.abs(candidate.parameter
					- result.get(result.size() - 1).parameter) <= tolerance) {
				result.get(result.size() - 1).include(candidate);
			} else {
				result.add(candidate);
			}
		}
		return result;
	}

	private static NormalizedPolynomial normalize(double[] polynomial) {
		double scale = 0;
		for (double coefficient : polynomial) {
			if (!Double.isFinite(coefficient)) {
				throw new IllegalArgumentException(
						"Polynomial coefficients must be finite");
			}
			scale = Math.max(scale, Math.abs(coefficient));
		}
		if (scale == 0) {
			return new NormalizedPolynomial(new double[] {0}, true);
		}
		double[] normalized = polynomial.clone();
		for (int index = 0; index < normalized.length; index++) {
			normalized[index] /= scale;
		}
		int degree = normalized.length - 1;
		while (degree > 0 && Math.abs(normalized[degree])
				<= COEFFICIENT_EPSILON) {
			degree--;
		}
		normalized = java.util.Arrays.copyOf(normalized, degree + 1);
		boolean zero = degree == 0
				&& Math.abs(normalized[0]) <= COEFFICIENT_EPSILON;
		return new NormalizedPolynomial(normalized, zero);
	}

	private static double evaluate(double[] ascending, double parameter) {
		double value = 0;
		for (int index = ascending.length - 1; index >= 0; index--) {
			value = value * parameter + ascending[index];
		}
		return value;
	}

	private static boolean oppositeSigns(double first, double second) {
		return first < 0 && second > 0 || first > 0 && second < 0;
	}

	private static final class NormalizedPolynomial {
		private final double[] coefficients;
		private final boolean zero;

		private NormalizedPolynomial(double[] coefficients, boolean zero) {
			this.coefficients = coefficients;
			this.zero = zero;
		}
	}

	private static final class MutableRootCell {
		private final double parameter;
		private double lower;
		private double upper;

		private MutableRootCell(double parameter, double lower, double upper) {
			this.parameter = parameter;
			this.lower = Math.min(lower, parameter);
			this.upper = Math.max(upper, parameter);
		}

		private void include(MutableRootCell other) {
			lower = Math.min(lower, other.lower);
			upper = Math.max(upper, other.upper);
		}
	}
}
