/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import java.math.BigInteger;

/**
 * Immutable exact rational number over {@link BigInteger}.
 *
 * <p>Every instance is reduced and has a positive denominator; zero is {@code 0/1}. A
 * binary64 value converts without rounding ({@link #ofBinary64(double)}) and the
 * arithmetic is exact. The only rounding is {@link #outwardDoubleEnclosure()}, which
 * brackets the value between binary64 numbers.</p>
 */
public final class ExactRational2D implements Comparable<ExactRational2D> {

	/** The rational zero, {@code 0/1}. */
	public static final ExactRational2D ZERO =
			new ExactRational2D(BigInteger.ZERO, BigInteger.ONE);
	/** The rational one, {@code 1/1}. */
	public static final ExactRational2D ONE =
			new ExactRational2D(BigInteger.ONE, BigInteger.ONE);

	private static final int SIGNIFICAND_BITS = 52;
	private static final int EXPONENT_BIAS = 1023;
	private static final int EXPONENT_FIELD_MASK = 0x7FF;
	private static final int MINIMUM_NORMAL_EXPONENT = -1022;
	private static final int MAXIMUM_EXPONENT = 1023;
	private static final int SUBNORMAL_SCALE = 1074;
	private static final long FRACTION_MASK = (1L << SIGNIFICAND_BITS) - 1L;

	private final BigInteger numerator;
	private final BigInteger denominator;

	private ExactRational2D(BigInteger numerator, BigInteger denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}

	/**
	 * Converts a finite binary64 value without rounding.
	 *
	 * @param value finite value; both signed zeros give {@link #ZERO}
	 * @return the exact value of {@code value}
	 * @throws IllegalArgumentException if {@code value} is NaN or infinite
	 */
	public static ExactRational2D ofBinary64(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			throw new IllegalArgumentException("Not a finite binary64 value");
		}
		long bits = Double.doubleToRawLongBits(value);
		if ((bits & Long.MAX_VALUE) == 0L) {
			return ZERO;
		}
		int exponentField = ((int) (bits >>> SIGNIFICAND_BITS)) & EXPONENT_FIELD_MASK;
		long significand = bits & FRACTION_MASK;
		int exponent;
		if (exponentField == 0) {
			exponent = -SUBNORMAL_SCALE;
		} else {
			significand |= 1L << SIGNIFICAND_BITS;
			exponent = exponentField - EXPONENT_BIAS - SIGNIFICAND_BITS;
		}
		int shift = Long.numberOfTrailingZeros(significand);
		BigInteger magnitude = BigInteger.valueOf(significand >>> shift);
		BigInteger signed = bits < 0L ? magnitude.negate() : magnitude;
		int scale = exponent + shift;
		if (scale >= 0) {
			return new ExactRational2D(signed.shiftLeft(scale), BigInteger.ONE);
		}
		return new ExactRational2D(signed, BigInteger.ONE.shiftLeft(-scale));
	}

	/**
	 * @param value integer value
	 * @return the rational {@code value/1}
	 */
	public static ExactRational2D of(long value) {
		return value == 0L ? ZERO
				: new ExactRational2D(BigInteger.valueOf(value), BigInteger.ONE);
	}

	/**
	 * @param numerator numerator
	 * @param denominator nonzero denominator
	 * @return the reduced rational {@code numerator/denominator}
	 * @throws ArithmeticException if {@code denominator} is zero
	 */
	public static ExactRational2D of(long numerator, long denominator) {
		return of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
	}

	/**
	 * @param numerator numerator
	 * @param denominator nonzero denominator
	 * @return the reduced rational {@code numerator/denominator}
	 * @throws ArithmeticException if {@code denominator} is zero
	 */
	public static ExactRational2D of(BigInteger numerator, BigInteger denominator) {
		int denominatorSign = denominator.signum();
		if (denominatorSign == 0) {
			throw new ArithmeticException("Zero denominator");
		}
		if (numerator.signum() == 0) {
			return ZERO;
		}
		BigInteger n = denominatorSign < 0 ? numerator.negate() : numerator;
		BigInteger d = denominatorSign < 0 ? denominator.negate() : denominator;
		BigInteger g = n.gcd(d);
		if (!BigInteger.ONE.equals(g)) {
			n = n.divide(g);
			d = d.divide(g);
		}
		return new ExactRational2D(n, d);
	}

	/**
	 * @param a first value
	 * @param b second value
	 * @return the exact midpoint {@code (a + b) / 2}
	 */
	public static ExactRational2D midpoint(ExactRational2D a, ExactRational2D b) {
		return a.add(b).half();
	}

	/** @return the numerator; its sign is the sign of this value */
	public BigInteger numerator() {
		return numerator;
	}

	/** @return the positive denominator, coprime with the numerator */
	public BigInteger denominator() {
		return denominator;
	}

	/** @return -1, 0 or 1 as this value is negative, zero or positive */
	public int signum() {
		return numerator.signum();
	}

	/** @return whether this value is zero */
	public boolean isZero() {
		return numerator.signum() == 0;
	}

	/** @return the larger of the bit lengths of the numerator magnitude and the denominator */
	public int bitLength() {
		return Math.max(numerator.abs().bitLength(), denominator.bitLength());
	}

	/**
	 * @param other addend
	 * @return the exact sum
	 */
	public ExactRational2D add(ExactRational2D other) {
		if (other.isZero()) {
			return this;
		}
		if (isZero()) {
			return other;
		}
		if (denominator.equals(other.denominator)) {
			return of(numerator.add(other.numerator), denominator);
		}
		return of(numerator.multiply(other.denominator)
				.add(other.numerator.multiply(denominator)),
				denominator.multiply(other.denominator));
	}

	/**
	 * @param other subtrahend
	 * @return the exact difference
	 */
	public ExactRational2D subtract(ExactRational2D other) {
		return add(other.negate());
	}

	/**
	 * @param other factor
	 * @return the exact product
	 */
	public ExactRational2D multiply(ExactRational2D other) {
		if (isZero() || other.isZero()) {
			return ZERO;
		}
		BigInteger first = numerator.gcd(other.denominator);
		BigInteger second = other.numerator.gcd(denominator);
		BigInteger n = numerator.divide(first).multiply(other.numerator.divide(second));
		BigInteger d = denominator.divide(second).multiply(other.denominator.divide(first));
		return new ExactRational2D(n, d);
	}

	/**
	 * @param other nonzero divisor
	 * @return the exact quotient
	 * @throws ArithmeticException if {@code other} is zero
	 */
	public ExactRational2D divide(ExactRational2D other) {
		if (other.isZero()) {
			throw new ArithmeticException("Division by zero");
		}
		if (isZero()) {
			return ZERO;
		}
		BigInteger first = numerator.gcd(other.numerator);
		BigInteger second = denominator.gcd(other.denominator);
		BigInteger n = numerator.divide(first).multiply(other.denominator.divide(second));
		BigInteger d = denominator.divide(second).multiply(other.numerator.divide(first));
		if (d.signum() < 0) {
			n = n.negate();
			d = d.negate();
		}
		return new ExactRational2D(n, d);
	}

	/** @return the exact negation */
	public ExactRational2D negate() {
		return isZero() ? this : new ExactRational2D(numerator.negate(), denominator);
	}

	/** @return the exact absolute value */
	public ExactRational2D abs() {
		return numerator.signum() < 0 ? negate() : this;
	}

	private ExactRational2D half() {
		if (numerator.signum() == 0) {
			return this;
		}
		if (numerator.testBit(0)) {
			return new ExactRational2D(numerator, denominator.shiftLeft(1));
		}
		return new ExactRational2D(numerator.shiftRight(1), denominator);
	}

	/**
	 * Brackets this value between binary64 numbers.
	 *
	 * <p>When the value is a binary64 number both bounds equal it. Otherwise the bounds are
	 * adjacent binary64 numbers, with {@code Double.MAX_VALUE} and an infinity beyond the
	 * finite range and zero and {@code Double.MIN_VALUE} (with signs) below the smallest
	 * subnormal.</p>
	 *
	 * @return {@code {lower, upper}} with {@code lower <= value <= upper}
	 */
	public double[] outwardDoubleEnclosure() {
		int sign = numerator.signum();
		if (sign == 0) {
			return new double[] {0.0, 0.0};
		}
		double[] magnitude = magnitudeEnclosure(numerator.abs(), denominator);
		if (sign > 0) {
			return magnitude;
		}
		return new double[] {-magnitude[1], -magnitude[0]};
	}

	/** Encloses a / b for positive a and b. */
	private static double[] magnitudeEnclosure(BigInteger a, BigInteger b) {
		int exponent = a.bitLength() - b.bitLength();
		if (compareWithPowerOfTwo(a, b, exponent) < 0) {
			exponent--;
		}
		// 2^exponent <= a / b < 2^(exponent + 1)
		if (exponent > MAXIMUM_EXPONENT) {
			return new double[] {Double.MAX_VALUE, Double.POSITIVE_INFINITY};
		}
		boolean normal = exponent >= MINIMUM_NORMAL_EXPONENT;
		int scale = normal ? SIGNIFICAND_BITS - exponent : SUBNORMAL_SCALE;
		BigInteger[] quotient = scale >= 0
				? a.shiftLeft(scale).divideAndRemainder(b)
				: a.divideAndRemainder(b.shiftLeft(-scale));
		long significand = quotient[0].longValueExact();
		long bits = normal
				? ((long) (exponent + EXPONENT_BIAS) << SIGNIFICAND_BITS)
						| (significand & FRACTION_MASK)
				: significand;
		double lower = Double.longBitsToDouble(bits);
		double upper = quotient[1].signum() == 0 ? lower : Math.nextUp(lower);
		return new double[] {lower, upper};
	}

	/** Compares a with b * 2^exponent. */
	private static int compareWithPowerOfTwo(BigInteger a, BigInteger b, int exponent) {
		return exponent >= 0 ? a.compareTo(b.shiftLeft(exponent))
				: a.shiftLeft(-exponent).compareTo(b);
	}

	@Override
	public int compareTo(ExactRational2D other) {
		int sign = numerator.signum();
		int otherSign = other.numerator.signum();
		if (sign != otherSign) {
			return Integer.compare(sign, otherSign);
		}
		if (denominator.equals(other.denominator)) {
			return numerator.compareTo(other.numerator);
		}
		return numerator.multiply(other.denominator)
				.compareTo(other.numerator.multiply(denominator));
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ExactRational2D)) {
			return false;
		}
		ExactRational2D that = (ExactRational2D) other;
		return numerator.equals(that.numerator) && denominator.equals(that.denominator);
	}

	@Override
	public int hashCode() {
		return 31 * numerator.hashCode() + denominator.hashCode();
	}

	/**
	 * @return {@code numerator/denominator} in decimal, for example {@code -3/2} or
	 *         {@code 5/1}
	 */
	@Override
	public String toString() {
		return numerator + "/" + denominator;
	}
}
