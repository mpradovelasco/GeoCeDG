/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable univariate polynomial with {@link ExactRational2D} coefficients.
 *
 * <p>Coefficient {@code i} multiplies {@code x^i}. Instances carry no leading zero
 * coefficient, so the zero polynomial has degree {@code -1}. Every operation is exact.</p>
 */
public final class ExactPolynomial1D {

	/** The zero polynomial, of degree -1. */
	public static final ExactPolynomial1D ZERO =
			new ExactPolynomial1D(new ExactRational2D[0]);
	/** The constant polynomial 1. */
	public static final ExactPolynomial1D ONE =
			new ExactPolynomial1D(new ExactRational2D[] {ExactRational2D.ONE});
	/** The polynomial x. */
	public static final ExactPolynomial1D X = new ExactPolynomial1D(
			new ExactRational2D[] {ExactRational2D.ZERO, ExactRational2D.ONE});

	private final ExactRational2D[] coefficients;

	private ExactPolynomial1D(ExactRational2D[] coefficients) {
		this.coefficients = coefficients;
	}

	/**
	 * @param coefficientsLowToHigh coefficients, the {@code i}-th multiplying {@code x^i};
	 *        trailing zeros are removed
	 * @return the polynomial
	 */
	public static ExactPolynomial1D of(ExactRational2D... coefficientsLowToHigh) {
		ExactRational2D[] copy = new ExactRational2D[coefficientsLowToHigh.length];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = Objects.requireNonNull(coefficientsLowToHigh[i], "coefficient");
		}
		return trimmed(copy);
	}

	/** Takes ownership of the array and removes its trailing zeros. */
	private static ExactPolynomial1D trimmed(ExactRational2D[] owned) {
		int length = owned.length;
		while (length > 0 && owned[length - 1].isZero()) {
			length--;
		}
		if (length == 0) {
			return ZERO;
		}
		return new ExactPolynomial1D(
				length == owned.length ? owned : Arrays.copyOf(owned, length));
	}

	/** Builds the polynomial with the given integer coefficients, low to high. */
	static ExactPolynomial1D ofIntegers(BigInteger[] coefficientsLowToHigh) {
		ExactRational2D[] values = new ExactRational2D[coefficientsLowToHigh.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = ExactRational2D.of(coefficientsLowToHigh[i], BigInteger.ONE);
		}
		return trimmed(values);
	}

	/** @return the degree, or -1 for the zero polynomial */
	public int degree() {
		return coefficients.length - 1;
	}

	/** @return whether this is the zero polynomial */
	public boolean isZero() {
		return coefficients.length == 0;
	}

	/**
	 * @param power nonnegative power of x
	 * @return the coefficient of {@code x^power}, zero above the degree
	 * @throws IllegalArgumentException for a negative power
	 */
	public ExactRational2D coefficient(int power) {
		if (power < 0) {
			throw new IllegalArgumentException("Negative power");
		}
		return power < coefficients.length ? coefficients[power] : ExactRational2D.ZERO;
	}

	/** @return the coefficient of the highest power, zero for the zero polynomial */
	public ExactRational2D leadingCoefficient() {
		return isZero() ? ExactRational2D.ZERO : coefficients[coefficients.length - 1];
	}

	/**
	 * @param other addend
	 * @return the exact sum
	 */
	public ExactPolynomial1D add(ExactPolynomial1D other) {
		int length = Math.max(coefficients.length, other.coefficients.length);
		ExactRational2D[] sum = new ExactRational2D[length];
		for (int i = 0; i < length; i++) {
			sum[i] = coefficient(i).add(other.coefficient(i));
		}
		return trimmed(sum);
	}

	/**
	 * @param other subtrahend
	 * @return the exact difference
	 */
	public ExactPolynomial1D subtract(ExactPolynomial1D other) {
		int length = Math.max(coefficients.length, other.coefficients.length);
		ExactRational2D[] difference = new ExactRational2D[length];
		for (int i = 0; i < length; i++) {
			difference[i] = coefficient(i).subtract(other.coefficient(i));
		}
		return trimmed(difference);
	}

	/** @return the exact negation */
	public ExactPolynomial1D negate() {
		ExactRational2D[] negated = new ExactRational2D[coefficients.length];
		for (int i = 0; i < negated.length; i++) {
			negated[i] = coefficients[i].negate();
		}
		return new ExactPolynomial1D(negated);
	}

	/**
	 * @param other factor
	 * @return the exact product
	 */
	public ExactPolynomial1D multiply(ExactPolynomial1D other) {
		if (isZero() || other.isZero()) {
			return ZERO;
		}
		ExactRational2D[] product = new ExactRational2D[
				coefficients.length + other.coefficients.length - 1];
		Arrays.fill(product, ExactRational2D.ZERO);
		for (int i = 0; i < coefficients.length; i++) {
			if (coefficients[i].isZero()) {
				continue;
			}
			for (int j = 0; j < other.coefficients.length; j++) {
				product[i + j] = product[i + j]
						.add(coefficients[i].multiply(other.coefficients[j]));
			}
		}
		return trimmed(product);
	}

	/**
	 * @param factor rational factor
	 * @return this polynomial multiplied by {@code factor}
	 */
	public ExactPolynomial1D scale(ExactRational2D factor) {
		if (factor.isZero()) {
			return ZERO;
		}
		ExactRational2D[] scaled = new ExactRational2D[coefficients.length];
		for (int i = 0; i < scaled.length; i++) {
			scaled[i] = coefficients[i].multiply(factor);
		}
		return new ExactPolynomial1D(scaled);
	}

	/**
	 * @param exponent nonnegative exponent
	 * @return this polynomial raised to {@code exponent}; {@code p^0} is {@link #ONE}
	 * @throws IllegalArgumentException for a negative exponent
	 */
	public ExactPolynomial1D pow(int exponent) {
		if (exponent < 0) {
			throw new IllegalArgumentException("Negative exponent");
		}
		ExactPolynomial1D result = ONE;
		ExactPolynomial1D base = this;
		int remaining = exponent;
		while (remaining > 0) {
			if ((remaining & 1) == 1) {
				result = result.multiply(base);
			}
			remaining >>= 1;
			if (remaining > 0) {
				base = base.multiply(base);
			}
		}
		return result;
	}

	/** @return the exact derivative */
	public ExactPolynomial1D derivative() {
		if (coefficients.length <= 1) {
			return ZERO;
		}
		ExactRational2D[] derivative = new ExactRational2D[coefficients.length - 1];
		for (int i = 1; i < coefficients.length; i++) {
			derivative[i - 1] = coefficients[i].multiply(ExactRational2D.of(i));
		}
		return trimmed(derivative);
	}

	/**
	 * Euclidean division.
	 *
	 * @param divisor nonzero divisor
	 * @return {@code {quotient, remainder}} with {@code this = quotient * divisor + remainder}
	 *         and {@code remainder.degree() < divisor.degree()}
	 * @throws ArithmeticException if {@code divisor} is the zero polynomial
	 */
	public ExactPolynomial1D[] divideAndRemainder(ExactPolynomial1D divisor) {
		if (divisor.isZero()) {
			throw new ArithmeticException("Division by the zero polynomial");
		}
		int divisorDegree = divisor.degree();
		if (degree() < divisorDegree) {
			return new ExactPolynomial1D[] {ZERO, this};
		}
		ExactRational2D[] remainder = coefficients.clone();
		ExactRational2D[] quotient = new ExactRational2D[degree() - divisorDegree + 1];
		ExactRational2D leading = divisor.leadingCoefficient();
		for (int k = quotient.length - 1; k >= 0; k--) {
			ExactRational2D factor = remainder[k + divisorDegree].divide(leading);
			quotient[k] = factor;
			if (factor.isZero()) {
				continue;
			}
			for (int j = 0; j < divisorDegree; j++) {
				remainder[k + j] = remainder[k + j]
						.subtract(factor.multiply(divisor.coefficients[j]));
			}
			remainder[k + divisorDegree] = ExactRational2D.ZERO;
		}
		return new ExactPolynomial1D[] {trimmed(quotient),
				trimmed(Arrays.copyOf(remainder, divisorDegree))};
	}

	/**
	 * Greatest common divisor by the Euclidean algorithm over the rationals.
	 *
	 * @param other second polynomial
	 * @return the monic greatest common divisor; the zero polynomial when both are zero
	 */
	public ExactPolynomial1D gcd(ExactPolynomial1D other) {
		return gcd(other, null);
	}

	/** Metered greatest common divisor; a null meter records nothing. */
	ExactPolynomial1D gcd(ExactPolynomial1D other, ExactBudget1D.Meter meter) {
		ExactPolynomial1D a = monic();
		ExactPolynomial1D b = other.monic();
		while (!b.isZero()) {
			ExactPolynomial1D remainder = a.divideAndRemainder(b)[1].monic();
			if (meter != null) {
				meter.observe(remainder);
			}
			a = b;
			b = remainder;
		}
		return a;
	}

	/**
	 * Exact quotient of a division known to leave no remainder.
	 *
	 * @throws IllegalStateException if the remainder is not zero
	 */
	ExactPolynomial1D exactQuotient(ExactPolynomial1D divisor) {
		ExactPolynomial1D[] division = divideAndRemainder(divisor);
		if (!division[1].isZero()) {
			throw new IllegalStateException("Inexact polynomial division");
		}
		return division[0];
	}

	/** @return this polynomial divided by its leading coefficient; zero stays zero */
	public ExactPolynomial1D monic() {
		if (isZero()) {
			return this;
		}
		ExactRational2D leading = leadingCoefficient();
		if (ExactRational2D.ONE.equals(leading)) {
			return this;
		}
		ExactRational2D[] normalized = new ExactRational2D[coefficients.length];
		for (int i = 0; i < normalized.length - 1; i++) {
			normalized[i] = coefficients[i].divide(leading);
		}
		normalized[normalized.length - 1] = ExactRational2D.ONE;
		return new ExactPolynomial1D(normalized);
	}

	/**
	 * @param x rational argument
	 * @return the exact value at {@code x}
	 */
	public ExactRational2D evaluate(ExactRational2D x) {
		ExactRational2D value = ExactRational2D.ZERO;
		for (int i = coefficients.length - 1; i >= 0; i--) {
			value = value.multiply(x).add(coefficients[i]);
		}
		return value;
	}

	/**
	 * @param x rational argument
	 * @return the sign of the value at {@code x}
	 */
	public int signAt(ExactRational2D x) {
		return evaluate(x).signum();
	}

	/** @return the sign for all sufficiently large arguments; 0 for the zero polynomial */
	public int signAtPositiveInfinity() {
		return leadingCoefficient().signum();
	}

	/** @return the sign for all sufficiently small arguments; 0 for the zero polynomial */
	public int signAtNegativeInfinity() {
		int sign = leadingCoefficient().signum();
		return (degree() & 1) == 0 ? sign : -sign;
	}

	/** @return the largest {@link ExactRational2D#bitLength()} of a coefficient; 0 for zero */
	public int maxCoefficientBitLength() {
		int bits = 0;
		for (ExactRational2D coefficient : coefficients) {
			bits = Math.max(bits, coefficient.bitLength());
		}
		return bits;
	}

	/**
	 * Primitive integer form: the coefficients multiplied by the positive rational that makes
	 * them coprime integers. A positive factor preserves every sign.
	 *
	 * @return the integer coefficients, low to high; empty for the zero polynomial
	 */
	BigInteger[] primitiveIntegerCoefficients() {
		BigInteger[] integers = new BigInteger[coefficients.length];
		BigInteger commonDenominator = BigInteger.ONE;
		for (ExactRational2D coefficient : coefficients) {
			BigInteger denominator = coefficient.denominator();
			commonDenominator = commonDenominator
					.divide(commonDenominator.gcd(denominator)).multiply(denominator);
		}
		BigInteger content = BigInteger.ZERO;
		for (int i = 0; i < integers.length; i++) {
			ExactRational2D coefficient = coefficients[i];
			integers[i] = coefficient.numerator()
					.multiply(commonDenominator.divide(coefficient.denominator()));
			content = content.gcd(integers[i]);
		}
		if (content.signum() > 0 && !BigInteger.ONE.equals(content)) {
			for (int i = 0; i < integers.length; i++) {
				integers[i] = integers[i].divide(content);
			}
		}
		return integers;
	}

	/** @return the primitive integer form as a polynomial (a positive multiple of this) */
	ExactPolynomial1D primitivePositive() {
		return ofIntegers(primitiveIntegerCoefficients());
	}

	/**
	 * Sign of an integer polynomial at a rational argument {@code a/b}, by the homogeneous
	 * evaluation {@code b^d * P(a/b)} in integer arithmetic.
	 */
	static int signOfIntegerPolynomial(BigInteger[] coefficientsLowToHigh,
			ExactRational2D x) {
		int degree = coefficientsLowToHigh.length - 1;
		if (degree < 0) {
			return 0;
		}
		BigInteger a = x.numerator();
		BigInteger b = x.denominator();
		boolean integerArgument = BigInteger.ONE.equals(b);
		BigInteger value = coefficientsLowToHigh[degree];
		BigInteger power = BigInteger.ONE;
		for (int i = degree - 1; i >= 0; i--) {
			if (!integerArgument) {
				power = power.multiply(b);
			}
			value = value.multiply(a);
			BigInteger coefficient = coefficientsLowToHigh[i];
			if (coefficient.signum() != 0) {
				value = value.add(integerArgument ? coefficient : coefficient.multiply(power));
			}
		}
		return value.signum();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ExactPolynomial1D)) {
			return false;
		}
		return Arrays.equals(coefficients, ((ExactPolynomial1D) other).coefficients);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(coefficients);
	}

	/**
	 * @return the terms from the highest power down, for example
	 *         {@code 3/2*x^2 + -1/1*x + 1/1}; {@code 0} for the zero polynomial
	 */
	@Override
	public String toString() {
		if (isZero()) {
			return "0";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = coefficients.length - 1; i >= 0; i--) {
			if (coefficients[i].isZero()) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append(" + ");
			}
			sb.append(coefficients[i]);
			if (i == 1) {
				sb.append("*x");
			} else if (i > 1) {
				sb.append("*x^").append(i);
			}
		}
		return sb.toString();
	}
}
