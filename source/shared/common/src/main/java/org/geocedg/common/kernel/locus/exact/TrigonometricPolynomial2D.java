/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Immutable polynomial {@code Q(c, s)} with {@link ExactRational2D} coefficients in the two
 * variables {@code c = cos t} and {@code s = sin t}.
 *
 * <p>The polynomial is formal: the relation {@code c^2 + s^2 = 1} is not applied, so equal
 * functions of {@code t} may have different representations and different total degrees.
 * Terms are kept in increasing order of the cosine power, then of the sine power, without
 * zero coefficients.</p>
 */
public final class TrigonometricPolynomial2D {

	/** The zero polynomial, of total degree -1. */
	public static final TrigonometricPolynomial2D ZERO =
			new TrigonometricPolynomial2D(new int[0], new int[0], new ExactRational2D[0]);

	private static final int POWER_BITS = 32;
	private static final long SINE_POWER_MASK = 0xFFFFFFFFL;

	private final int[] cosinePowers;
	private final int[] sinePowers;
	private final ExactRational2D[] coefficients;

	private TrigonometricPolynomial2D(int[] cosinePowers, int[] sinePowers,
			ExactRational2D[] coefficients) {
		this.cosinePowers = cosinePowers;
		this.sinePowers = sinePowers;
		this.coefficients = coefficients;
	}

	/**
	 * @param value constant value
	 * @return the constant polynomial
	 */
	public static TrigonometricPolynomial2D constant(ExactRational2D value) {
		return monomial(value, 0, 0);
	}

	/** @return the polynomial {@code c = cos t} */
	public static TrigonometricPolynomial2D cosine() {
		return monomial(ExactRational2D.ONE, 1, 0);
	}

	/** @return the polynomial {@code s = sin t} */
	public static TrigonometricPolynomial2D sine() {
		return monomial(ExactRational2D.ONE, 0, 1);
	}

	/**
	 * @param coefficient coefficient
	 * @param cosinePower nonnegative power of {@code c}
	 * @param sinePower nonnegative power of {@code s}
	 * @return {@code coefficient * c^cosinePower * s^sinePower}
	 * @throws IllegalArgumentException for a negative power
	 */
	public static TrigonometricPolynomial2D monomial(ExactRational2D coefficient,
			int cosinePower, int sinePower) {
		if (cosinePower < 0 || sinePower < 0) {
			throw new IllegalArgumentException("Negative power");
		}
		if (coefficient.isZero()) {
			return ZERO;
		}
		return new TrigonometricPolynomial2D(new int[] {cosinePower}, new int[] {sinePower},
				new ExactRational2D[] {coefficient});
	}

	private static long key(int cosinePower, int sinePower) {
		return ((long) cosinePower << POWER_BITS) | sinePower;
	}

	private static TrigonometricPolynomial2D fromTerms(Map<Long, ExactRational2D> terms) {
		int count = 0;
		for (ExactRational2D coefficient : terms.values()) {
			if (!coefficient.isZero()) {
				count++;
			}
		}
		int[] cosine = new int[count];
		int[] sine = new int[count];
		ExactRational2D[] values = new ExactRational2D[count];
		int index = 0;
		for (Map.Entry<Long, ExactRational2D> term : terms.entrySet()) {
			if (term.getValue().isZero()) {
				continue;
			}
			long key = term.getKey();
			cosine[index] = (int) (key >>> POWER_BITS);
			sine[index] = (int) (key & SINE_POWER_MASK);
			values[index] = term.getValue();
			index++;
		}
		return new TrigonometricPolynomial2D(cosine, sine, values);
	}

	private TreeMap<Long, ExactRational2D> terms() {
		TreeMap<Long, ExactRational2D> terms = new TreeMap<>();
		for (int i = 0; i < coefficients.length; i++) {
			terms.put(key(cosinePowers[i], sinePowers[i]), coefficients[i]);
		}
		return terms;
	}

	/**
	 * @param other addend
	 * @return the exact sum
	 */
	public TrigonometricPolynomial2D add(TrigonometricPolynomial2D other) {
		TreeMap<Long, ExactRational2D> terms = terms();
		for (int i = 0; i < other.coefficients.length; i++) {
			terms.merge(key(other.cosinePowers[i], other.sinePowers[i]), other.coefficients[i],
					ExactRational2D::add);
		}
		return fromTerms(terms);
	}

	/**
	 * @param other subtrahend
	 * @return the exact difference
	 */
	public TrigonometricPolynomial2D subtract(TrigonometricPolynomial2D other) {
		return add(other.negate());
	}

	/** @return the exact negation */
	public TrigonometricPolynomial2D negate() {
		return scale(ExactRational2D.of(-1));
	}

	/**
	 * @param other factor
	 * @return the exact product
	 */
	public TrigonometricPolynomial2D multiply(TrigonometricPolynomial2D other) {
		TreeMap<Long, ExactRational2D> terms = new TreeMap<>();
		for (int i = 0; i < coefficients.length; i++) {
			for (int j = 0; j < other.coefficients.length; j++) {
				terms.merge(key(cosinePowers[i] + other.cosinePowers[j],
						sinePowers[i] + other.sinePowers[j]),
						coefficients[i].multiply(other.coefficients[j]), ExactRational2D::add);
			}
		}
		return fromTerms(terms);
	}

	/**
	 * @param factor rational factor
	 * @return this polynomial multiplied by {@code factor}
	 */
	public TrigonometricPolynomial2D scale(ExactRational2D factor) {
		if (factor.isZero()) {
			return ZERO;
		}
		ExactRational2D[] scaled = new ExactRational2D[coefficients.length];
		for (int i = 0; i < scaled.length; i++) {
			scaled[i] = coefficients[i].multiply(factor);
		}
		return new TrigonometricPolynomial2D(cosinePowers, sinePowers, scaled);
	}

	/** @return the largest {@code i + j} of a term {@code c^i s^j}; -1 for zero */
	public int totalDegree() {
		int degree = -1;
		for (int i = 0; i < coefficients.length; i++) {
			degree = Math.max(degree, cosinePowers[i] + sinePowers[i]);
		}
		return degree;
	}

	/** @return whether this is the zero polynomial */
	public boolean isZero() {
		return coefficients.length == 0;
	}

	/** @return the number of terms with a nonzero coefficient */
	public int termCount() {
		return coefficients.length;
	}

	/**
	 * @param cosinePower power of {@code c}
	 * @param sinePower power of {@code s}
	 * @return the coefficient of {@code c^cosinePower * s^sinePower}, zero when absent
	 */
	public ExactRational2D coefficient(int cosinePower, int sinePower) {
		for (int i = 0; i < coefficients.length; i++) {
			if (cosinePowers[i] == cosinePower && sinePowers[i] == sinePower) {
				return coefficients[i];
			}
		}
		return ExactRational2D.ZERO;
	}

	/**
	 * @param c value of the cosine variable
	 * @param s value of the sine variable
	 * @return the exact value {@code Q(c, s)}
	 */
	public ExactRational2D evaluate(ExactRational2D c, ExactRational2D s) {
		ExactRational2D[] cosine = powers(c);
		ExactRational2D[] sine = powers(s);
		ExactRational2D value = ExactRational2D.ZERO;
		for (int i = 0; i < coefficients.length; i++) {
			value = value.add(coefficients[i].multiply(cosine[cosinePowers[i]])
					.multiply(sine[sinePowers[i]]));
		}
		return value;
	}

	private ExactRational2D[] powers(ExactRational2D base) {
		int degree = Math.max(totalDegree(), 0);
		ExactRational2D[] powers = new ExactRational2D[degree + 1];
		powers[0] = ExactRational2D.ONE;
		for (int i = 1; i <= degree; i++) {
			powers[i] = powers[i - 1].multiply(base);
		}
		return powers;
	}

	/**
	 * Tangent half-angle form. With {@code n = totalDegree()} it returns
	 * {@code p(u) = (1 + u^2)^n * Q((1 - u^2) / (1 + u^2), 2u / (1 + u^2))}, a polynomial of
	 * degree at most {@code 2n} whose coefficient of {@code u^(2n)} is {@code Q(-1, 0)}. For
	 * {@code t} in {@code (-pi, pi)} and {@code u = tan(t/2)}, {@code Q(cos t, sin t)} and
	 * {@code p(u)} have the same sign and the same root multiplicities.
	 *
	 * @return the polynomial {@code p}; the zero polynomial for zero
	 */
	public ExactPolynomial1D toTangentHalfAngle() {
		int n = totalDegree();
		if (n < 0) {
			return ExactPolynomial1D.ZERO;
		}
		ExactRational2D one = ExactRational2D.ONE;
		ExactRational2D zero = ExactRational2D.ZERO;
		ExactPolynomial1D[] cosine = polynomialPowers(
				ExactPolynomial1D.of(one, zero, ExactRational2D.of(-1)), n);
		ExactPolynomial1D[] sine = polynomialPowers(
				ExactPolynomial1D.of(zero, ExactRational2D.of(2)), n);
		ExactPolynomial1D[] weight = polynomialPowers(ExactPolynomial1D.of(one, zero, one), n);
		ExactPolynomial1D result = ExactPolynomial1D.ZERO;
		for (int i = 0; i < coefficients.length; i++) {
			int c = cosinePowers[i];
			int s = sinePowers[i];
			result = result.add(cosine[c].multiply(sine[s]).multiply(weight[n - c - s])
					.scale(coefficients[i]));
		}
		return result;
	}

	private static ExactPolynomial1D[] polynomialPowers(ExactPolynomial1D base, int degree) {
		ExactPolynomial1D[] powers = new ExactPolynomial1D[degree + 1];
		powers[0] = ExactPolynomial1D.ONE;
		for (int i = 1; i <= degree; i++) {
			powers[i] = powers[i - 1].multiply(base);
		}
		return powers;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TrigonometricPolynomial2D)) {
			return false;
		}
		TrigonometricPolynomial2D that = (TrigonometricPolynomial2D) other;
		return Arrays.equals(cosinePowers, that.cosinePowers)
				&& Arrays.equals(sinePowers, that.sinePowers)
				&& Arrays.equals(coefficients, that.coefficients);
	}

	@Override
	public int hashCode() {
		return 31 * (31 * Arrays.hashCode(cosinePowers) + Arrays.hashCode(sinePowers))
				+ Arrays.hashCode(coefficients);
	}

	/**
	 * @return the terms in storage order, for example {@code 15/1 + -12/1*s + 1/1*c*s^2};
	 *         {@code 0} for the zero polynomial
	 */
	@Override
	public String toString() {
		if (isZero()) {
			return "0";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < coefficients.length; i++) {
			if (i > 0) {
				sb.append(" + ");
			}
			sb.append(coefficients[i]);
			appendPower(sb, "c", cosinePowers[i]);
			appendPower(sb, "s", sinePowers[i]);
		}
		return sb.toString();
	}

	private static void appendPower(StringBuilder sb, String variable, int power) {
		if (power == 1) {
			sb.append('*').append(variable);
		} else if (power > 1) {
			sb.append('*').append(variable).append('^').append(power);
		}
	}
}
