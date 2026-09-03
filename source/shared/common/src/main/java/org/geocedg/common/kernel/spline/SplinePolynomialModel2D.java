/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spline;

import java.math.BigDecimal;
import java.util.Arrays;

import org.geogebra.common.kernel.geos.GeoFunctionNVar;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * Immutable, viewport-independent polynomial representation of the 2D spline
 * family produced by the classic {@code Spline} interpolation equations.
 *
 * <p>The interpolation solve remains numerical. Native snapshots encode
 * interior and periodic continuity structurally through exact truncated-power
 * expansions. Rounded power spans are derived evaluation data, not exact
 * coefficient authority for a certified algorithm.</p>
 */
public final class SplinePolynomialModel2D {
	private static final int MAXIMUM_POINT_COUNT = 32;
	private static final int MAXIMUM_DEGREE = 12;
	private static final int MAXIMUM_SYSTEM_SIZE = 512;
	private static final double PIVOT_TOLERANCE_FACTOR = 128;
	private static final double BACKWARD_ERROR_TOLERANCE = 1E-9;
	private final int degree;
	private final double[] knots;
	private final double[][][] coefficients;
	private final boolean closed;
	private final SplineStructuralModel2D structural;
	private final String semanticSignature;

	private SplinePolynomialModel2D(int degree, double[] knots,
			double[][][] coefficients, boolean closed) {
		this.degree = degree;
		this.knots = knots.clone();
		this.coefficients = deepCopy(coefficients);
		this.closed = closed;
		structural = null;
		semanticSignature = buildSignature();
	}

	private SplinePolynomialModel2D(int degree, double[] knots,
			SplineStructuralModel2D structural, boolean closed) {
		this.degree = degree;
		this.knots = knots.clone();
		this.structural = structural;
		coefficients = structural.getRoundedCoefficients();
		this.closed = closed;
		semanticSignature = buildSignature();
	}

	/**
	 * Captures and solves the current ordinary command inputs.
	 *
	 * @return immutable semantic spline snapshot
	 */
	public static SplinePolynomialModel2D create(GeoList points, int degree,
			GeoFunctionNVar weight) {
		return create(points, degree, weight, SplinePrecisionSolve2D.Policy.ordinary());
	}

	// Test hosts may reduce the fixed policy through its checked package-private
	// constructor. Ordinary commands always take the public entry above.
	static SplinePolynomialModel2D create(GeoList points, int degree,
			GeoFunctionNVar weight, SplinePrecisionSolve2D.Policy precisionPolicy) {
		if (points == null || !points.isDefined() || degree < 3
				|| degree > points.size() || points.size() < 3) {
			throw new IllegalArgumentException(
					"SplineV2 needs at least three finite 2D points and degree "
							+ "at most the point count");
		}
		if (!isWithinWorkPolicy(points.size(), degree)) {
			throw new IllegalArgumentException(
					"SplineV2 exceeds the bounded semantic solve policy");
		}
		int pointCount = points.size();
		double[][] values = new double[pointCount][2];
		for (int index = 0; index < pointCount; index++) {
			if (!(points.get(index) instanceof GeoPointND)
					|| points.get(index).isGeoElement3D()) {
				throw new IllegalArgumentException(
						"SplineV2 interpolation data must be finite 2D points");
			}
			GeoPointND point = (GeoPointND) points.get(index);
			if (!point.isDefined() || point.isInfinite()) {
				throw new IllegalArgumentException(
						"SplineV2 interpolation points must be finite");
			}
			values[index][0] = point.getInhomX();
			values[index][1] = point.getInhomY();
			if (!Double.isFinite(values[index][0])
					|| !Double.isFinite(values[index][1])) {
				throw new IllegalArgumentException(
						"SplineV2 interpolation points must be finite");
			}
		}
		double[] cumulative = cumulativeParameter(values, weight);
		double total = cumulative[pointCount - 1];
		if (!Double.isFinite(total) || total <= 0) {
			throw new IllegalArgumentException(
					"SplineV2 parameterization must be finite and strictly increasing");
		}
		double[] knots = new double[pointCount];
		for (int index = 1; index < pointCount; index++) {
			knots[index] = cumulative[index] / total;
			if (!Double.isFinite(knots[index])
					|| knots[index] <= knots[index - 1]) {
				throw new IllegalArgumentException(
						"SplineV2 contains a zero or invalid parameter span");
			}
		}
		knots[pointCount - 1] = 1;
		boolean closed = points.get(0).isEqual(points.get(pointCount - 1));
		if (closed && (values[0][0] != values[pointCount - 1][0]
				|| values[0][1] != values[pointCount - 1][1])) {
			throw new IllegalArgumentException(
					"SplineV2 periodic closure requires exactly equal finite endpoints");
		}
		int order = degree + 1;
		double[][][] originalSystems = new double[2][][];
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			originalSystems[coordinate] = linearSystem(values, cumulative,
					coordinate, order, closed);
		}
		SplineStructuralModel2D structural = SplineStructuralModel2D.create(
				values, knots, degree, closed, originalSystems,
				precisionPolicy);
		double[][][] coefficients = structural.getRoundedCoefficients();
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			double[][] system = originalSystems[coordinate];
			double[] solution = new double[(pointCount - 1) * order];
			for (int span = 0; span < pointCount - 1; span++) {
				System.arraycopy(coefficients[span][coordinate], 0,
						solution, span * order, order);
			}
			if (!hasAcceptableBackwardError(system, solution)) {
				throw new IllegalArgumentException(
						"SplineV2 structural model fails original equation revalidation");
			}
		}
		return new SplinePolynomialModel2D(degree, knots, structural,
				closed);
	}

	/**
	 * Bounded experimental solve policy. Classic {@code Spline} remains
	 * unchanged; unsupported V2 sizes fail before dense allocation.
	 *
	 * @return whether the requested dense solve is inside the bounded policy
	 */
	public static boolean isWithinWorkPolicy(int pointCount, int degree) {
		if (pointCount < 3 || pointCount > MAXIMUM_POINT_COUNT || degree < 3
				|| degree > pointCount || degree > MAXIMUM_DEGREE) {
			return false;
		}
		long systemSize = (long) (pointCount - 1) * (degree + 1);
		return systemSize <= MAXIMUM_SYSTEM_SIZE;
	}

	/** @return polynomial degree on every span */
	public int getDegree() {
		return degree;
	}

	/** @return number of canonical polynomial spans */
	public int getSpanCount() {
		return coefficients.length;
	}

	/** @return immutable copy of normalized knot parameters */
	public double[] getKnots() {
		return knots.clone();
	}

	/** @return whether the input endpoints define a closed periodic image */
	public boolean isClosed() {
		return closed;
	}

	/** @return deterministic semantic content signature */
	public String getSemanticSignature() {
		return semanticSignature;
	}

	/** @return canonical owning span, with every interior knot owned on the right */
	public int findOwningSpan(double parameter) {
		if (!Double.isFinite(parameter) || parameter < 0 || parameter > 1) {
			return -1;
		}
		if (parameter == 1) {
			return coefficients.length - 1;
		}
		int position = Arrays.binarySearch(knots, parameter);
		if (position >= 0) {
			return Math.min(position, coefficients.length - 1);
		}
		int insertion = -position - 1;
		return Math.max(0, insertion - 1);
	}

	/**
	 * Evaluates one point in the normalized semantic parameter.
	 *
	 * @return finite Cartesian coordinates
	 */
	public double[] evaluate(double parameter) {
		int span = findOwningSpan(parameter);
		if (span < 0) {
			throw new IllegalArgumentException("SplineV2 parameter is out of domain");
		}
		return new double[] {evaluatePolynomial(coefficients[span][0], parameter),
				evaluatePolynomial(coefficients[span][1], parameter)};
	}

	/**
	 * Evaluates the analytic first derivative on the canonical owning span.
	 *
	 * @return derivative with respect to normalized semantic parameter
	 */
	public double[] evaluateDerivative(double parameter) {
		int span = findOwningSpan(parameter);
		if (span < 0) {
			throw new IllegalArgumentException("SplineV2 parameter is out of domain");
		}
		return new double[] {evaluateDerivative(coefficients[span][0], parameter),
				evaluateDerivative(coefficients[span][1], parameter)};
	}

	/** @return defensive descending-power coefficients for one span/coordinate */
	public double[] getCoefficients(int span, int coordinate) {
		return coefficients[span][coordinate].clone();
	}

	/**
	 * Exact descending span numerator coefficients over the common denominator.
	 * A certificate must enclose these rationals, not the rounded double cache.
	 *
	 * @return defensive exact numerator array
	 * @throws IllegalStateException for a nonstructural diagnostic snapshot
	 */
	public BigDecimal[] getExactCoefficientNumerators(int span, int coordinate) {
		if (structural == null) {
			throw new IllegalStateException("Spline snapshot has no structural authority");
		}
		return structural.getNumerators(span, coordinate);
	}

	/** @return positive exact common denominator, or one for diagnostic spans */
	public int getCoefficientDenominator() {
		return structural == null ? 1 : structural.getDenominator();
	}

	/** @return structural knot/seam continuity order, or -1 if not established */
	public int getStructuralContinuityOrder() {
		return structural == null ? -1 : degree - 1;
	}

	/** @return dimension of the actual numerical interpolation solve */
	public int getSolveSystemDimension() {
		return structural == null ? getLegacySystemDimension() : structural.getDimension();
	}

	/** @return dimension of the retained original-equation revalidation system */
	public int getLegacySystemDimension() {
		return (knots.length - 1) * (degree + 1);
	}

	/** @return immutable arithmetic-path/work evidence for the native construction */
	public SplineConstructionEvidence2D getConstructionEvidence() {
		if (structural == null) {
			throw new IllegalStateException("Diagnostic spans have no construction evidence");
		}
		return structural.getEvidence();
	}

	private static double[] cumulativeParameter(double[][] points,
			GeoFunctionNVar weight) {
		double[] cumulative = new double[points.length];
		for (int index = 1; index < points.length; index++) {
			double dx = points[index][0] - points[index - 1][0];
			double dy = points[index][1] - points[index - 1][1];
			double increment = weight == null ? Math.hypot(dx, dy)
					: weight.evaluate(new double[] {dx, dy});
			if (!Double.isFinite(increment) || increment <= 0) {
				throw new IllegalArgumentException(
						"SplineV2 weights must induce positive finite spans");
			}
			cumulative[index] = cumulative[index - 1] + increment;
		}
		return cumulative;
	}

	private static double[][] linearSystem(double[][] points,
			double[] cumulative, int coordinate, int order, boolean closed) {
		int pointCount = points.length;
		int length = (pointCount - 1) * order;
		double total = cumulative[pointCount - 1];
		double[][] matrix = new double[length][length + 1];
		int row = 0;
		int column = 0;
		for (int point = 0; point < pointCount - 1; point++) {
			double parameter = cumulative[point] / total;
			evaluateRow(matrix[row], column, order, parameter);
			matrix[row][length] = points[point][coordinate];
			row++;
			column += order;
		}
		column = 0;
		for (int point = 1; point < pointCount; point++) {
			double parameter = cumulative[point] / total;
			evaluateRow(matrix[row], column, order, parameter);
			matrix[row][length] = points[point][coordinate];
			row++;
			column += order;
		}
		for (int derivative = order - 2; derivative > 0; derivative--) {
			column = 0;
			for (int point = 1; point < pointCount - 1; point++) {
				double parameter = cumulative[point] / total;
				derivativeContinuityRow(matrix[row], column, order, derivative,
						parameter);
				row++;
				column += order;
			}
		}
		if (closed) {
			for (int derivative = order - 2; derivative > 0; derivative--) {
				for (int index = 0; index < order; index++) {
					matrix[row][index] = derivativeCoefficient(index, order,
							derivative, 0);
					matrix[row][length - order + index] = -derivativeCoefficient(
							index, order, derivative, 1);
				}
				row++;
			}
		} else {
			matrix[row][0] = 0;
			matrix[row][1] = factorial(order - 2);
			row++;
			matrix[row][length - order] = factorial(order - 1);
			matrix[row][length - order + 1] = factorial(order - 2);
		}
		row++;
		int offset = 2;
		for (; row < length; row++) {
			matrix[row][length - offset * order] = factorial(order - 1)
					* cumulative[offset - 1] / total;
			matrix[row][length - offset * order + 1] = factorial(order - 2);
			offset++;
		}
		return matrix;
	}

	private static void evaluateRow(double[] row, int column, int order,
			double parameter) {
		for (int power = order - 1; power >= 0; power--) {
			row[column + order - power - 1] = Math.pow(parameter, power);
		}
	}

	private static void derivativeContinuityRow(double[] row, int column,
			int order, int derivative, double parameter) {
		for (int index = 0; index < order; index++) {
			double value = derivativeCoefficient(index, order, derivative,
					parameter);
			row[column + index] = value;
			row[column + order + index] = -value;
		}
	}

	private static double derivativeCoefficient(int column, int order,
			int derivative, double parameter) {
		int exponent = order - column - 1;
		if (exponent < derivative) {
			return 0;
		}
		double coefficient = 1;
		for (int index = 0; index < derivative; index++) {
			coefficient *= exponent - index;
		}
		return coefficient * Math.pow(parameter, exponent - derivative);
	}

	static double[] solve(double[][] matrix) {
		return solve(matrix, null);
	}

	static double[] solve(double[][] matrix, SplinePrecisionSolve2D.Work work) {
		int length = matrix.length;
		double[][] original = deepCopy(matrix);
		for (double[] row : matrix) {
			double scale = 0;
			for (double value : row) {
				scale = Math.max(scale, Math.abs(value));
			}
			if (!Double.isFinite(scale) || scale == 0) {
				return null;
			}
			for (int column = 0; column < row.length; column++) {
				row[column] /= scale;
				recordSolve(work, 1);
			}
		}
		// The truncated-power variables can have very different units. This
		// invertible diagonal change equilibrates those variables; it does not
		// change the spline space, pivot guard or original-equation residual.
		double[] columnScale = new double[length];
		for (int column = 0; column < length; column++) {
			for (int row = 0; row < length; row++) {
				columnScale[column] = Math.max(columnScale[column],
						Math.abs(matrix[row][column]));
			}
			if (!Double.isFinite(columnScale[column]) || columnScale[column] == 0) {
				return null;
			}
			for (int row = 0; row < length; row++) {
				matrix[row][column] /= columnScale[column];
				recordSolve(work, 1);
			}
		}
		double pivotTolerance = PIVOT_TOLERANCE_FACTOR * Math.ulp(1.0)
				* Math.max(1, length);
		for (int pivot = 0; pivot < length; pivot++) {
			int best = pivot;
			for (int row = pivot + 1; row < length; row++) {
				if (Math.abs(matrix[row][pivot]) > Math.abs(matrix[best][pivot])) {
					best = row;
				}
			}
			if (!Double.isFinite(matrix[best][pivot])
					|| Math.abs(matrix[best][pivot]) <= pivotTolerance) {
				return null;
			}
			double[] swap = matrix[pivot];
			matrix[pivot] = matrix[best];
			matrix[best] = swap;
			double divisor = matrix[pivot][pivot];
			for (int column = pivot; column <= length; column++) {
				matrix[pivot][column] /= divisor;
				recordSolve(work, 1);
			}
			for (int row = 0; row < length; row++) {
				if (row == pivot) {
					continue;
				}
				double factor = matrix[row][pivot];
				if (factor == 0) {
					continue;
				}
				for (int column = pivot; column <= length; column++) {
					matrix[row][column] -= factor * matrix[pivot][column];
					recordSolve(work, 2);
				}
			}
		}
		double[] solution = new double[length];
		for (int row = 0; row < length; row++) {
			solution[row] = matrix[row][length] / columnScale[row];
			recordSolve(work, 1);
			if (!Double.isFinite(solution[row])) {
				return null;
			}
		}
		return hasAcceptableBackwardError(original, solution) ? solution : null;
	}

	private static void recordSolve(SplinePrecisionSolve2D.Work work, long operations) {
		if (work != null) {
			work.solve(operations);
		}
	}

	static boolean hasAcceptableBackwardError(double[][] system,
			double[] solution) {
		double maximumRelativeError = 0;
		for (double[] row : system) {
			double reconstructed = 0;
			double scale = Math.abs(row[solution.length]);
			for (int column = 0; column < solution.length; column++) {
				reconstructed += row[column] * solution[column];
				scale += Math.abs(row[column]) * Math.abs(solution[column]);
			}
			if (!Double.isFinite(reconstructed) || !Double.isFinite(scale)) {
				return false;
			}
			double relative = Math.abs(reconstructed - row[solution.length])
					/ Math.max(1, scale);
			maximumRelativeError = Math.max(maximumRelativeError, relative);
		}
		return maximumRelativeError <= BACKWARD_ERROR_TOLERANCE;
	}

	private static double evaluatePolynomial(double[] descending,
			double parameter) {
		double value = 0;
		for (double coefficient : descending) {
			value = value * parameter + coefficient;
		}
		return value;
	}

	private static double evaluateDerivative(double[] descending,
			double parameter) {
		double value = 0;
		int degree = descending.length - 1;
		for (int index = 0; index < degree; index++) {
			value = value * parameter + descending[index] * (degree - index);
		}
		return value;
	}

	private static double factorial(int value) {
		double result = 1;
		for (int index = 2; index <= value; index++) {
			result *= index;
			if (!Double.isFinite(result)) {
				throw new IllegalArgumentException(
						"SplineV2 derivative constraint overflow");
			}
		}
		return result;
	}

	private static double[][] deepCopy(double[][] source) {
		double[][] copy = new double[source.length][];
		for (int row = 0; row < source.length; row++) {
			copy[row] = source[row].clone();
		}
		return copy;
	}

	private String buildSignature() {
		StringBuilder signature = new StringBuilder(
				structural == null ? "semantic-spline-polynomial/v1|degree="
						: "semantic-spline-polynomial/v2|degree=")
				.append(degree).append("|closed=").append(closed);
		for (double knot : knots) {
			signature.append("|k=").append(Double.toHexString(knot));
		}
		if (structural != null) {
			structural.appendSignature(signature);
			return signature.toString();
		}
		for (double[][] span : coefficients) {
			for (double[] coordinate : span) {
				for (double coefficient : coordinate) {
					signature.append("|c=")
							.append(Double.toHexString(coefficient == 0 ? 0
									: coefficient));
				}
			}
		}
		return signature.toString();
	}

	private static double[][][] deepCopy(double[][][] source) {
		double[][][] copy = new double[source.length][][];
		for (int span = 0; span < source.length; span++) {
			copy[span] = new double[source[span].length][];
			for (int coordinate = 0; coordinate < source[span].length;
					coordinate++) {
				copy[span][coordinate] = source[span][coordinate].clone();
			}
		}
		return copy;
	}
}
