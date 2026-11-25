/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import java.math.BigInteger;

/**
 * Minimal immutable exact rational over {@link BigInteger}, private to this package so that
 * the library imports no kernel type. Every value is reduced with a positive denominator. A
 * binary64 value converts without rounding; {@link #roundHalfEven()} is the only rounding.
 */
final class BSplineExactRational {

	static final BSplineExactRational ZERO =
			new BSplineExactRational(BigInteger.ZERO, BigInteger.ONE);
	static final BSplineExactRational ONE =
			new BSplineExactRational(BigInteger.ONE, BigInteger.ONE);

	private static final int SIGNIFICAND_BITS = 53;
	private static final int MIN_EXPONENT = -1074;
	private static final int MAX_EXPONENT = 971;

	private final BigInteger numerator;
	private final BigInteger denominator;

	private BSplineExactRational(BigInteger numerator, BigInteger denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}

	static BSplineExactRational of(BigInteger numerator, BigInteger denominator) {
		if (denominator.signum() == 0) {
			throw new ArithmeticException("zero denominator");
		}
		BigInteger num = numerator;
		BigInteger den = denominator;
		if (den.signum() < 0) {
			num = num.negate();
			den = den.negate();
		}
		BigInteger gcd = num.gcd(den);
		if (!gcd.equals(BigInteger.ONE) && gcd.signum() != 0) {
			num = num.divide(gcd);
			den = den.divide(gcd);
		}
		if (num.signum() == 0) {
			return ZERO;
		}
		return new BSplineExactRational(num, den);
	}

	static BSplineExactRational ofLong(long value) {
		return of(BigInteger.valueOf(value), BigInteger.ONE);
	}

	/** Exact value of a finite binary64 number ({@code -0.0} gives zero). */
	static BSplineExactRational ofDouble(double value) {
		if (!Double.isFinite(value)) {
			throw new ArithmeticException("nonfinite value");
		}
		if (value == 0.0) {
			return ZERO;
		}
		long bits = Double.doubleToRawLongBits(value);
		int exponentField = (int) ((bits >>> 52) & 0x7FF);
		long fraction = bits & ((1L << 52) - 1);
		long significand;
		int exponent;
		if (exponentField == 0) {
			significand = fraction;
			exponent = MIN_EXPONENT;
		} else {
			significand = fraction | (1L << 52);
			exponent = exponentField - 1075;
		}
		BigInteger num = BigInteger.valueOf(value < 0 ? -significand : significand);
		if (exponent >= 0) {
			return of(num.shiftLeft(exponent), BigInteger.ONE);
		}
		return of(num, BigInteger.ONE.shiftLeft(-exponent));
	}

	BSplineExactRational add(BSplineExactRational other) {
		return of(numerator.multiply(other.denominator).add(other.numerator.multiply(denominator)),
				denominator.multiply(other.denominator));
	}

	BSplineExactRational subtract(BSplineExactRational other) {
		return add(other.negate());
	}

	BSplineExactRational multiply(BSplineExactRational other) {
		return of(numerator.multiply(other.numerator), denominator.multiply(other.denominator));
	}

	BSplineExactRational divide(BSplineExactRational other) {
		return of(numerator.multiply(other.denominator), denominator.multiply(other.numerator));
	}

	BSplineExactRational negate() {
		return numerator.signum() == 0 ? this : new BSplineExactRational(numerator.negate(),
				denominator);
	}

	BigInteger numerator() {
		return numerator;
	}

	BigInteger denominator() {
		return denominator;
	}

	/**
	 * Rounds once to the nearest binary64 value, ties to even, with gradual underflow and
	 * overflow to an infinity (IEEE 754 round-to-nearest-even).
	 */
	double roundHalfEven() {
		int sign = numerator.signum();
		if (sign == 0) {
			return 0.0;
		}
		BigInteger a = numerator.abs();
		int exponent = a.bitLength() - denominator.bitLength() - SIGNIFICAND_BITS;
		BigInteger[] qr = scaledQuotient(a, exponent);
		while (qr[0].bitLength() > SIGNIFICAND_BITS) {
			exponent++;
			qr = scaledQuotient(a, exponent);
		}
		while (qr[0].bitLength() < SIGNIFICAND_BITS) {
			exponent--;
			qr = scaledQuotient(a, exponent);
		}
		if (exponent < MIN_EXPONENT) {
			exponent = MIN_EXPONENT;
			qr = scaledQuotient(a, exponent);
		}
		BigInteger q = qr[0];
		BigInteger twiceRemainder = qr[1].shiftLeft(1);
		BigInteger divisor = qr[2];
		int comparison = twiceRemainder.compareTo(divisor);
		if (comparison > 0 || comparison == 0 && q.testBit(0)) {
			q = q.add(BigInteger.ONE);
			if (q.bitLength() > SIGNIFICAND_BITS) {
				q = q.shiftRight(1);
				exponent++;
			}
		}
		if (exponent > MAX_EXPONENT) {
			return sign < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		}
		double magnitude = Math.scalb((double) q.longValue(), exponent);
		return sign < 0 ? -magnitude : magnitude;
	}

	/** Quotient, remainder and divisor of {@code a / (denominator * 2^exponent)}. */
	private BigInteger[] scaledQuotient(BigInteger a, int exponent) {
		BigInteger dividend = exponent >= 0 ? a : a.shiftLeft(-exponent);
		BigInteger divisor = exponent >= 0 ? denominator.shiftLeft(exponent) : denominator;
		BigInteger[] qr = dividend.divideAndRemainder(divisor);
		return new BigInteger[] {qr[0], qr[1], divisor};
	}
}
