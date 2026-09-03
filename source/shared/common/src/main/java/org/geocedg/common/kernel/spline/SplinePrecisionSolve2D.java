/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spline;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;

import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.AdmissionException;
import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.Failure;
import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.Path;

/** Fixed-work numerical fallback, never an exact linear-solve certificate. */
final class SplinePrecisionSolve2D {
	static final List<Integer> PRECISIONS = List.of(48, 80, 112);
	static final long MAXIMUM_OPERATIONS = 10_000_000;
	private static final BigDecimal BACKWARD_LIMIT = new BigDecimal(1E-9);

	/** A test may reduce these ceilings, never expand the productive policy. */
	static final class Policy {
		final int levels;
		final long operations;

		Policy(int levels, long operations) {
			if (levels < 0 || levels > PRECISIONS.size() || operations < 1
					|| operations > MAXIMUM_OPERATIONS) {
				throw new IllegalArgumentException("Invalid bounded spline precision policy");
			}
			this.levels = levels;
			this.operations = operations;
		}

		static Policy ordinary() {
			return new Policy(PRECISIONS.size(), MAXIMUM_OPERATIONS);
		}
	}

	/** Construction-local work; snapshots do not expose this mutable accumulator. */
	static final class Work {
		final Policy policy;
		final EnumMap<Failure, Integer> failures = new EnumMap<>(Failure.class);
		long solve;
		long expansion;
		long admission;
		int precision;
		int levels;

		Work(Policy policy) {
			this.policy = policy;
		}

		void solve(long count) {
			solve += count;
			check();
		}

		void expand(long count) {
			expansion += count;
			check();
		}

		void admit(long count) {
			admission += count;
			check();
		}

		void fail(Failure reason) {
			failures.merge(reason, 1, Integer::sum);
		}

		private void check() {
			if (solve + expansion + admission > policy.operations) {
				fail(Failure.WORK_EXHAUSTED);
				throw rejection("SplineV2 structural arithmetic work budget exhausted");
			}
		}

		SplineConstructionEvidence2D snapshot(Path path, int retained) {
			return new SplineConstructionEvidence2D(path, precision, retained, levels,
					solve, expansion, admission, failures);
		}

		AdmissionException rejection(String message) {
			return new AdmissionException(message, snapshot(Path.REJECTED, 0));
		}
	}

	private SplinePrecisionSolve2D() {
		// Construction-local bounded solver only.
	}

	static BigDecimal[] solve(BigDecimal[][] original, int precision, Work work) {
		MathContext arithmetic = new MathContext(precision, RoundingMode.HALF_EVEN);
		int size = original.length;
		BigDecimal[][] matrix = new BigDecimal[size][];
		for (int row = 0; row < size; row++) {
			matrix[row] = original[row].clone();
			BigDecimal scale = BigDecimal.ZERO;
			for (BigDecimal value : matrix[row]) {
				scale = scale.max(value.abs());
			}
			if (scale.signum() == 0) {
				work.fail(Failure.PIVOT_REJECTED);
				return null;
			}
			for (int column = 0; column <= size; column++) {
				matrix[row][column] = matrix[row][column].divide(scale, arithmetic);
				work.solve(1);
			}
		}
		BigDecimal[] columnScale = new BigDecimal[size];
		for (int column = 0; column < size; column++) {
			BigDecimal scale = BigDecimal.ZERO;
			for (int row = 0; row < size; row++) {
				scale = scale.max(matrix[row][column].abs());
			}
			if (scale.signum() == 0) {
				work.fail(Failure.PIVOT_REJECTED);
				return null;
			}
			columnScale[column] = scale;
			for (int row = 0; row < size; row++) {
				matrix[row][column] = matrix[row][column].divide(scale, arithmetic);
				work.solve(1);
			}
		}
		BigDecimal pivotLimit = new BigDecimal(128 * Math.ulp(1.0) * Math.max(1, size));
		for (int pivot = 0; pivot < size; pivot++) {
			int best = pivot;
			for (int row = pivot + 1; row < size; row++) {
				if (matrix[row][pivot].abs().compareTo(matrix[best][pivot].abs()) > 0) {
					best = row;
				}
			}
			if (matrix[best][pivot].abs().compareTo(pivotLimit) <= 0) {
				work.fail(Failure.PIVOT_REJECTED);
				return null;
			}
			BigDecimal[] swap = matrix[pivot];
			matrix[pivot] = matrix[best];
			matrix[best] = swap;
			BigDecimal divisor = matrix[pivot][pivot];
			for (int column = pivot; column <= size; column++) {
				matrix[pivot][column] = matrix[pivot][column].divide(divisor, arithmetic);
				work.solve(1);
			}
			for (int row = 0; row < size; row++) {
				if (row == pivot || matrix[row][pivot].signum() == 0) {
					continue;
				}
				BigDecimal factor = matrix[row][pivot];
				for (int column = pivot; column <= size; column++) {
					matrix[row][column] = matrix[row][column].subtract(
							factor.multiply(matrix[pivot][column], arithmetic), arithmetic);
					work.solve(2);
				}
			}
		}
		BigDecimal[] solution = new BigDecimal[size];
		for (int row = 0; row < size; row++) {
			solution[row] = matrix[row][size].divide(columnScale[row], arithmetic);
			work.solve(1);
		}
		preserveExactSingletonRows(original, solution, work);
		if (!acceptable(original, solution, work)) {
			work.fail(Failure.REDUCED_RESIDUAL_REJECTED);
			return null;
		}
		return solution;
	}

	private static void preserveExactSingletonRows(BigDecimal[][] system,
			BigDecimal[] solution, Work work) {
		for (BigDecimal[] row : system) {
			int only = -1;
			for (int column = 0; column < solution.length; column++) {
				if (row[column].signum() != 0) {
					if (only >= 0) {
						only = -2;
						break;
					}
					only = column;
				}
			}
			if (only >= 0) {
				try {
					// This is an exact defining equation, not near-zero snapping.
					// Every reduced/original equation is revalidated afterwards.
					solution[only] = row[solution.length].divide(row[only]);
					work.solve(1);
				} catch (ArithmeticException exception) {
					// A nonterminating quotient has no exact finite-decimal replacement.
				}
			}
		}
	}

	private static boolean acceptable(BigDecimal[][] system, BigDecimal[] solution, Work work) {
		for (BigDecimal[] row : system) {
			BigDecimal residual = row[solution.length].negate();
			BigDecimal scale = row[solution.length].abs();
			for (int column = 0; column < solution.length; column++) {
				BigDecimal product = row[column].multiply(solution[column]);
				residual = residual.add(product);
				scale = scale.add(product.abs());
				work.solve(3);
			}
			work.solve(1);
			if (residual.abs().compareTo(scale.max(BigDecimal.ONE)
					.multiply(BACKWARD_LIMIT)) > 0) {
				return false;
			}
		}
		return true;
	}
}
