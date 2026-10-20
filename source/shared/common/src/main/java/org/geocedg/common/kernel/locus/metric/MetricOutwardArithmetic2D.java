/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/**
 * Outward-rounded binary64 helpers for the certified metric classes
 * ({@code locus-v2-metrics.md} section 25.1, N5). IEEE-754 sums, products and
 * {@link Math#sqrt} are correctly rounded. The exact rounding error of a sum
 * (TwoSum) or of a product (Dekker's TwoProduct) decides the direction of the
 * rounding, so an exact result stays exact and an inexact one moves one
 * adjacent representable value outward. Outside the range where the error
 * term is exact, and for divisions, the result is always widened by one
 * adjacent value. Non-finite operands or results throw
 * {@link ArithmeticException}, which makes a certificate fail closed.
 */
final class MetricOutwardArithmetic2D {
	/** Veltkamp splitter {@code 2^27 + 1}. */
	private static final double SPLITTER = 134217729.0;
	/** Largest operand or product for which the split cannot overflow. */
	private static final double PRODUCT_CEILING = 0x1p900;
	/** Smallest product whose error term is exact (no gradual underflow). */
	private static final double PRODUCT_FLOOR = 0x1p-900;
	private static final int UNKNOWN = 2;

	private MetricOutwardArithmetic2D() {
		// Utility class.
	}

	static double addDown(double left, double right) {
		double sum = finite(left + right);
		return twoSumError(left, right, sum) < 0 ? Math.nextDown(sum) : sum;
	}

	static double addUp(double left, double right) {
		double sum = finite(left + right);
		return twoSumError(left, right, sum) > 0 ? Math.nextUp(sum) : sum;
	}

	static double subDown(double left, double right) {
		return addDown(left, -right);
	}

	static double subUp(double left, double right) {
		return addUp(left, -right);
	}

	static double mulDown(double left, double right) {
		double product = finite(left * right);
		int sign = productErrorSign(left, right, product);
		return sign == 0 || sign == 1 ? product : Math.nextDown(product);
	}

	static double mulUp(double left, double right) {
		double product = finite(left * right);
		int sign = productErrorSign(left, right, product);
		return sign == 0 || sign == -1 ? product : Math.nextUp(product);
	}

	static double divDown(double left, double right) {
		if (right == 0) {
			throw new ArithmeticException("Outward division by zero");
		}
		return Math.nextDown(finite(left / right));
	}

	static double divUp(double left, double right) {
		if (right == 0) {
			throw new ArithmeticException("Outward division by zero");
		}
		return Math.nextUp(finite(left / right));
	}

	/** @return lower bound of the square root of a value at least {@code value} */
	static double sqrtDown(double value) {
		if (!(value >= 0)) {
			throw new ArithmeticException("Square root of a negative bound");
		}
		if (value == 0) {
			return 0;
		}
		double root = finite(Math.sqrt(value));
		int comparison = squareComparison(root, value);
		return comparison == 0 || comparison == -1 ? root
				: Math.max(0, Math.nextDown(root));
	}

	/** @return upper bound of the square root of a value at most {@code value} */
	static double sqrtUp(double value) {
		if (!(value >= 0)) {
			throw new ArithmeticException("Square root of a negative bound");
		}
		if (value == 0) {
			return 0;
		}
		double root = finite(Math.sqrt(value));
		int comparison = squareComparison(root, value);
		return comparison == 0 || comparison == 1 ? root : Math.nextUp(root);
	}

	/**
	 * @return lower bound of the magnitude of an interval {@code [low, high]}:
	 *         zero when it contains zero
	 */
	static double mignitude(double low, double high) {
		if (low > 0) {
			return low;
		}
		if (high < 0) {
			return -high;
		}
		return 0;
	}

	/** @return upper bound of the magnitude of an interval {@code [low, high]} */
	static double magnitude(double low, double high) {
		return Math.max(Math.abs(low), Math.abs(high));
	}

	/** @return upper bound of {@code sqrt(x*x + y*y)} for {@code |x|<=a, |y|<=b} */
	static double hypotUp(double a, double b) {
		return sqrtUp(addUp(mulUp(a, a), mulUp(b, b)));
	}

	/** @return lower bound of {@code sqrt(x*x + y*y)} for {@code |x|>=a, |y|>=b} */
	static double hypotDown(double a, double b) {
		return sqrtDown(nonNegativeDown(addDown(squareDown(a), squareDown(b))));
	}

	/** @return lower bound of {@code x*x} for {@code |x| >= a >= 0}, never negative */
	static double squareDown(double a) {
		return a == 0 ? 0 : nonNegativeDown(mulDown(a, a));
	}

	/** @return a lower bound clamped to zero for a quantity known to be non-negative */
	static double nonNegativeDown(double value) {
		return Math.max(0, value);
	}

	/** Exact rounding error of {@code sum = fl(a + b)} (TwoSum). */
	private static double twoSumError(double a, double b, double sum) {
		double virtual = sum - a;
		return (a - (sum - virtual)) + (b - virtual);
	}

	/**
	 * @return sign of {@code a*b - product}: 0 when exact, 1 when the exact
	 *         product is larger, -1 when smaller, {@link #UNKNOWN} outside the
	 *         range where Dekker's error term is exact
	 */
	private static int productErrorSign(double a, double b, double product) {
		if (a == 0 || b == 0) {
			return 0;
		}
		double magnitude = Math.abs(product);
		if (magnitude < PRODUCT_FLOOR || magnitude > PRODUCT_CEILING
				|| Math.abs(a) > PRODUCT_CEILING || Math.abs(b) > PRODUCT_CEILING) {
			return UNKNOWN;
		}
		double splitA = SPLITTER * a;
		double highA = splitA - (splitA - a);
		double lowA = a - highA;
		double splitB = SPLITTER * b;
		double highB = splitB - (splitB - b);
		double lowB = b - highB;
		double error = ((highA * highB - product) + highA * lowB + lowA * highB)
				+ lowA * lowB;
		return error > 0 ? 1 : error < 0 ? -1 : 0;
	}

	/**
	 * @return sign of {@code root*root - value}, or {@link #UNKNOWN} outside the
	 *         range where it is decided exactly
	 */
	private static int squareComparison(double root, double value) {
		double square = finite(root * root);
		int sign = productErrorSign(root, root, square);
		if (sign == UNKNOWN) {
			return UNKNOWN;
		}
		// The rounded square is within a few units of value, so square - value is
		// exact (Sterbenz) and the rounded sum keeps the sign of the exact one.
		double splitRoot = SPLITTER * root;
		double high = splitRoot - (splitRoot - root);
		double low = root - high;
		double error = ((high * high - square) + 2 * high * low) + low * low;
		double difference = (square - value) + error;
		return difference > 0 ? 1 : difference < 0 ? -1 : 0;
	}

	private static double finite(double value) {
		if (!Double.isFinite(value)) {
			throw new ArithmeticException("No finite outward bound");
		}
		return value;
	}
}
