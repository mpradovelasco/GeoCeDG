/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spline;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.AdmissionException;
import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.Failure;
import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.Path;
import org.geocedg.common.kernel.spline.SplinePrecisionSolve2D.Policy;
import org.geocedg.common.kernel.spline.SplinePrecisionSolve2D.Work;

/**
 * Bounded truncated-power authority for the existing interpolation family.
 * Knots denote their exact binary64 values; stored free coefficients denote
 * exact binary64 or bounded finite decimal values. Expanded numerators are
 * exact; interpolation solving and rounded spans remain numerical.
 */
final class SplineStructuralModel2D {

	private final int degree;
	private final double[] knots;
	private final int denominator;
	private final boolean closed;
	private final BigDecimal[][] baseBasis;
	private final BigDecimal[][] freeCoordinates;
	private final BigDecimal[][][] numerators;
	private final double[][][] rounded;
	private final Work work;
	private SplineConstructionEvidence2D evidence;

	private SplineStructuralModel2D(double[][] values, double[] knots,
			int degree, boolean closed, double[][][] originalSystems, Policy policy) {
		this.degree = degree;
		this.knots = knots.clone();
		this.closed = closed;
		work = new Work(policy);
		denominator = closed ? factorial(degree) : 1;
		int dimension = closed ? knots.length - 1 : knots.length + degree - 1;
		baseBasis = new BigDecimal[dimension][degree + 1];
		for (BigDecimal[] basis : baseBasis) {
			Arrays.fill(basis, BigDecimal.ZERO);
		}
		if (closed) {
			baseBasis[0][0] = BigDecimal.valueOf(denominator);
			for (int index = 1; index < dimension; index++) {
				periodicBasis(index);
			}
		} else {
			for (int power = 0; power <= degree; power++) {
				baseBasis[power][power] = BigDecimal.ONE;
			}
		}
		freeCoordinates = new BigDecimal[2][];
		numerators = new BigDecimal[knots.length - 1][2][degree + 1];
		rounded = new double[knots.length - 1][2][degree + 1];
		construct(values, originalSystems);
	}

	// Package-private preflight used by the existing historical diagnostic only.
	static SplineStructuralModel2D create(double[][] values, double[] knots,
			int degree, boolean closed) {
		return create(values, knots, degree, closed, null, Policy.ordinary());
	}

	static SplineStructuralModel2D create(double[][] values, double[] knots,
			int degree, boolean closed, double[][][] originalSystems, Policy policy) {
		return new SplineStructuralModel2D(values, knots, degree, closed,
				originalSystems, policy);
	}

	private void construct(double[][] values, double[][][] originalSystems) {
		BigDecimal[][][] systems = {linearSystem(values, 0), linearSystem(values, 1)};
		BigDecimal[][] fast = new BigDecimal[2][];
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			double[][] matrix = new double[systems[coordinate].length][];
			for (int row = 0; row < matrix.length; row++) {
				matrix[row] = Arrays.stream(systems[coordinate][row])
						.mapToDouble(this::approximate).toArray();
			}
			double[] solution = SplinePolynomialModel2D.solve(matrix, work);
			if (solution == null) {
				work.fail(Failure.FAST_SOLVE_REJECTED);
				break;
			}
			fast[coordinate] = Arrays.stream(solution).mapToObj(BigDecimal::new)
					.toArray(BigDecimal[]::new);
		}
		if (fast[0] != null && fast[1] != null && install(fast, originalSystems)) {
			evidence = work.snapshot(Path.BINARY64, 0);
			return;
		}
		BigDecimal[][] previous = null;
		int previousPrecision = 0;
		for (int level = 0; level < work.policy.levels; level++) {
			work.precision = SplinePrecisionSolve2D.PRECISIONS.get(level);
			work.levels++;
			BigDecimal[][] current = new BigDecimal[2][];
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				current[coordinate] = SplinePrecisionSolve2D.solve(systems[coordinate],
						work.precision, work);
			}
			if (current[0] == null || current[1] == null) {
				previous = null;
				continue;
			}
			if (previous != null) {
				// Option A is always tried before retaining decimal authority. Exact
				// equality is coefficient stability, never coordinate proximity.
				if (stableInstall(previous, current, 0, originalSystems)) {
					evidence = work.snapshot(Path.HIGH_PRECISION_BINARY64, 0);
					return;
				}
				// 48/80 -> 32 retained digits; 80/112 -> 64 retained digits. The
				// fixed 16-digit guard is arithmetic policy, not a geometry tolerance.
				int retained = previousPrecision - 16;
				if (stableInstall(previous, current, retained, originalSystems)) {
					evidence = work.snapshot(Path.HIGH_PRECISION_STRUCTURAL, retained);
					return;
				}
			}
			previous = current;
			previousPrecision = work.precision;
		}
		work.fail(Failure.PRECISION_EXHAUSTED);
		throw work.rejection("SplineV2 structural interpolation is singular or numerically "
				+ "inadmissible at bounded PMAX; original equation revalidation and "
				+ "successive-precision stability are required");
	}

	private boolean stableInstall(BigDecimal[][] previous, BigDecimal[][] current,
			int retained, double[][][] originalSystems) {
		BigDecimal[][] first = canonical(previous, retained);
		BigDecimal[][] second = canonical(current, retained);
		if (first == null || second == null) {
			work.fail(Failure.NONFINITE_CACHE);
			return false;
		}
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			for (int index = 0; index < first[coordinate].length; index++) {
				if (first[coordinate][index].compareTo(second[coordinate][index]) != 0) {
					work.fail(Failure.STABILITY_NOT_ESTABLISHED);
					return false;
				}
			}
		}
		// Identical exact weights imply identical exact expansions and the same
		// complete original-equation classification at both precision levels.
		boolean accepted = install(second, originalSystems);
		if (!accepted && retained == 0) {
			work.fail(Failure.BINARY64_REPRESENTATION_REJECTED);
		}
		return accepted;
	}

	private static BigDecimal[][] canonical(BigDecimal[][] values, int retained) {
		BigDecimal[][] result = new BigDecimal[2][];
		MathContext precision = retained == 0 ? null
				: new MathContext(retained, RoundingMode.HALF_EVEN);
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			result[coordinate] = new BigDecimal[values[coordinate].length];
			for (int index = 0; index < values[coordinate].length; index++) {
				BigDecimal value = values[coordinate][index];
				if (retained == 0) {
					double binary = value.doubleValue();
					if (!Double.isFinite(binary)) {
						return null;
					}
					result[coordinate][index] = new BigDecimal(binary);
				} else {
					result[coordinate][index] = value.round(precision).stripTrailingZeros();
				}
			}
		}
		return result;
	}

	private boolean install(BigDecimal[][] coordinates, double[][][] originalSystems) {
		try {
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				freeCoordinates[coordinate] = coordinates[coordinate].clone();
				expand(coordinate);
				if (originalSystems != null) {
					double[] flattened = new double[rounded.length * (degree + 1)];
					for (int span = 0; span < rounded.length; span++) {
						System.arraycopy(rounded[span][coordinate], 0, flattened,
								span * (degree + 1), degree + 1);
					}
					if (!SplinePolynomialModel2D.hasAcceptableBackwardError(
							originalSystems[coordinate], flattened)
							|| !exactOriginalAdmission(originalSystems[coordinate], coordinate)) {
						work.fail(Failure.ORIGINAL_EQUATION_REJECTED);
						return false;
					}
				}
			}
			return true;
		} catch (AdmissionException exception) {
			throw exception;
		} catch (ArithmeticException | IllegalArgumentException exception) {
			work.fail(Failure.NONFINITE_CACHE);
			return false;
		}
	}

	private boolean exactOriginalAdmission(double[][] system, int coordinate) {
		BigDecimal divisor = BigDecimal.valueOf(denominator);
		BigDecimal limit = new BigDecimal(1E-9);
		int size = rounded.length * (degree + 1);
		for (double[] row : system) {
			BigDecimal rhs = new BigDecimal(row[size]).multiply(divisor);
			BigDecimal residual = rhs.negate();
			BigDecimal scale = rhs.abs();
			for (int column = 0; column < size; column++) {
				if (row[column] == 0) {
					continue;
				}
				BigDecimal coefficient = numerators[column / (degree + 1)][coordinate]
						[column % (degree + 1)];
				BigDecimal product = new BigDecimal(row[column]).multiply(coefficient);
				residual = residual.add(product);
				scale = scale.add(product.abs());
				work.admit(3);
			}
			work.admit(2);
			if (residual.abs().compareTo(scale.max(divisor).multiply(limit)) > 0) {
				return false;
			}
		}
		return true;
	}

	private void periodicBasis(int basis) {
		BigDecimal distance = BigDecimal.ONE.subtract(new BigDecimal(knots[basis]));
		BigDecimal scale = BigDecimal.valueOf(denominator);
		for (int derivative = degree - 1; derivative >= 0; derivative--) {
			BigDecimal sum = scale.multiply(BigDecimal.valueOf(
					binomial(degree, derivative)))
					.multiply(distance.pow(degree - derivative));
			for (int power = derivative + 2; power <= degree; power++) {
				sum = sum.add(baseBasis[basis][power]
						.multiply(BigDecimal.valueOf(binomial(power, derivative))));
			}
			// D=d! makes this division terminate exactly in the dyadic ring.
			// A rounded divide here would destroy structural periodicity.
			baseBasis[basis][derivative + 1] = sum.negate()
					.divide(BigDecimal.valueOf(derivative + 1));
		}
	}

	private BigDecimal[][] linearSystem(double[][] values, int coordinate) {
		int dimension = baseBasis.length;
		BigDecimal[][] system = new BigDecimal[dimension][dimension + 1];
		for (BigDecimal[] row : system) {
			Arrays.fill(row, BigDecimal.ZERO);
		}
		int interpolationRows = closed ? knots.length - 1 : knots.length;
		int row = 0;
		for (; row < interpolationRows; row++) {
			fillRow(system[row], Math.min(row, knots.length - 2), knots[row], 0);
			system[row][dimension] = new BigDecimal(values[row][coordinate])
					.multiply(BigDecimal.valueOf(denominator));
		}
		if (!closed) {
			fillRow(system[row++], 0, 0, degree - 1);
			fillRow(system[row++], knots.length - 2, 1, degree - 1);
			for (int offset = 2; offset <= degree - 2; offset++) {
				// Preserve the original selected-span polynomial extension row,
				// even when its evaluation parameter lies outside that span.
				fillRow(system[row++], knots.length - 1 - offset,
						knots[offset - 1], degree - 1);
			}
		}
		return system;
	}

	private void fillRow(BigDecimal[] row, int span, double parameter, int derivative) {
		BigDecimal t = new BigDecimal(parameter);
		for (int basis = 0; basis < baseBasis.length; basis++) {
			BigDecimal value = BigDecimal.ZERO;
			for (int power = degree; power >= derivative; power--) {
				value = value.multiply(t).add(baseBasis[basis][power]
						.multiply(BigDecimal.valueOf(fallingFactorial(power, derivative))));
			}
			int hinge = hingeIndex(basis);
			if (hinge >= 1 && hinge <= span) {
				BigDecimal delta = t.subtract(new BigDecimal(knots[hinge]));
				value = value.add(delta.pow(degree - derivative)
						.multiply(BigDecimal.valueOf((long) denominator
								* fallingFactorial(degree, derivative))));
			}
			row[basis] = value;
		}
	}

	private int hingeIndex(int basis) {
		return closed ? basis : basis - degree;
	}

	private void expand(int coordinate) {
		BigDecimal[] base = new BigDecimal[degree + 1];
		Arrays.fill(base, BigDecimal.ZERO);
		for (int basis = 0; basis < baseBasis.length; basis++) {
			BigDecimal weight = freeCoordinates[coordinate][basis];
			for (int power = 0; power <= degree; power++) {
				base[power] = base[power].add(weight.multiply(baseBasis[basis][power]));
				work.expand(2);
			}
		}
		for (int span = 0; span < knots.length - 1; span++) {
			if (span > 0) {
				int basis = closed ? span : degree + span;
				BigDecimal hinge = freeCoordinates[coordinate][basis]
						.multiply(BigDecimal.valueOf(denominator));
				BigDecimal negativeKnot = new BigDecimal(knots[span]).negate();
				for (int power = 0; power <= degree; power++) {
					base[power] = base[power].add(hinge
							.multiply(BigDecimal.valueOf(binomial(degree, power)))
							.multiply(negativeKnot.pow(degree - power)));
					work.expand(5);
				}
			}
			for (int power = 0; power <= degree; power++) {
				int descending = degree - power;
				numerators[span][coordinate][descending] = base[power];
				rounded[span][coordinate][descending] = approximate(base[power]);
				work.expand(1);
			}
		}
	}

	private double approximate(BigDecimal numerator) {
		double value = numerator.divide(BigDecimal.valueOf(denominator),
				MathContext.DECIMAL128).doubleValue();
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("SplineV2 structural coefficient overflow");
		}
		return value == 0 ? 0 : value;
	}

	BigDecimal[] getNumerators(int span, int coordinate) {
		return numerators[span][coordinate].clone();
	}

	int getDenominator() {
		return denominator;
	}

	int getDimension() {
		return baseBasis.length;
	}

	double[][][] getRoundedCoefficients() {
		double[][][] copy = new double[rounded.length][2][];
		for (int span = 0; span < rounded.length; span++) {
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				copy[span][coordinate] = rounded[span][coordinate].clone();
			}
		}
		return copy;
	}

	void appendSignature(StringBuilder signature) {
		signature.append("|denominator=").append(denominator)
				.append("|precisionPolicy=").append(evidence.getPolicy())
				.append("|retainedDigits=").append(evidence.getRetainedPrecision());
		for (BigDecimal[] coordinate : freeCoordinates) {
			for (BigDecimal value : coordinate) {
				signature.append("|f=").append(value.stripTrailingZeros().toEngineeringString());
			}
		}
	}

	SplineConstructionEvidence2D getEvidence() {
		return evidence;
	}

	private static int factorial(int value) {
		return fallingFactorial(value, value);
	}

	private static int fallingFactorial(int value, int order) {
		int result = 1;
		for (int index = 0; index < order; index++) {
			result *= value - index;
		}
		return result;
	}

	private static int binomial(int n, int k) {
		int result = 1;
		for (int index = 1; index <= k; index++) {
			result = result * (n - index + 1) / index;
		}
		return result;
	}
}
