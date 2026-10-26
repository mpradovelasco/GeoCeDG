/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * A polynomial with exact rational coefficients in the indeterminates of one constructive
 * identity certificate ({@code locus-v2-constructive-authority.md} sections 8 and 10). A
 * monomial packs the exponents of the 16 indeterminates of
 * {@code constructive-identity-certificate-budget/v1} in two 64-bit words, eight bits each and
 * the first indeterminate in the highest byte, so that the unsigned order of the words is the
 * lexicographic order of the exponents. An exponent beyond 255 fails closed with a typed cause and
 * is never wrapped or truncated. Terms are ordered by the descending graded lexicographic order,
 * so the first is the leading term. Every product charges its term products to the budget of the
 * attempt, and every result is checked against its term and coefficient limits.
 */
final class CertificatePolynomial {

	/** Number of indeterminates a monomial represents. */
	static final int INDETERMINATES = 16;
	/** Largest exponent of one indeterminate. */
	static final int MAXIMUM_EXPONENT = 255;

	private static final long HIGH_BITS = 0x8080808080808080L;
	private static final long LOW_BITS = 0x7F7F7F7F7F7F7F7FL;

	private final TreeMap<Monomial, ExactRational2D> terms;
	private int[] maximumExponents;

	private CertificatePolynomial(TreeMap<Monomial, ExactRational2D> terms) {
		this.terms = terms;
	}

	/** The exponents of one monomial. */
	static final class Monomial implements Comparable<Monomial> {
		/** The monomial of degree zero. */
		static final Monomial ONE = new Monomial(0L, 0L);

		private final long first;
		private final long second;
		private final int degree;

		private Monomial(long first, long second) {
			this.first = first;
			this.second = second;
			int total = 0;
			for (int index = 0; index < INDETERMINATES; index++) {
				total += exponent(index);
			}
			degree = total;
		}

		/**
		 * @param indeterminate index of an indeterminate
		 * @return the monomial of that indeterminate, of degree one
		 */
		static Monomial of(int indeterminate) {
			checkIndex(indeterminate);
			long unit = 1L << shift(indeterminate);
			return indeterminate < 8 ? new Monomial(unit, 0L) : new Monomial(0L, unit);
		}

		/** @return the exponent of one indeterminate */
		int exponent(int indeterminate) {
			long word = indeterminate < 8 ? first : second;
			return (int) ((word >>> shift(indeterminate)) & 0xFF);
		}

		/** @return the total degree */
		int getDegree() {
			return degree;
		}

		/**
		 * @return the product, whose exponents are the sums of the exponents
		 * @throws ConstructiveIdentityBudgetExhaustedException with
		 *         {@code EXPONENT_RANGE} when an exponent would exceed 255
		 */
		Monomial times(Monomial other, ConstructiveIdentityCounters counters) {
			return new Monomial(add(first, other.first, counters),
					add(second, other.second, counters));
		}

		/**
		 * @return the quotient when every exponent of the divisor is at most this one, otherwise
		 *         {@code null}
		 */
		Monomial dividedBy(Monomial divisor) {
			long[] words = new long[2];
			for (int index = 0; index < INDETERMINATES; index++) {
				int exponent = exponent(index) - divisor.exponent(index);
				if (exponent < 0) {
					return null;
				}
				words[index < 8 ? 0 : 1] |= ((long) exponent) << shift(index);
			}
			return new Monomial(words[0], words[1]);
		}

		/** @return the monomial with every exponent halved, or {@code null} if one is odd */
		Monomial half() {
			long[] words = new long[2];
			for (int index = 0; index < INDETERMINATES; index++) {
				int exponent = exponent(index);
				if (exponent % 2 != 0) {
					return null;
				}
				words[index < 8 ? 0 : 1] |= ((long) (exponent / 2)) << shift(index);
			}
			return new Monomial(words[0], words[1]);
		}

		/** @return the monomial with one exponent lowered by one; it must be positive */
		Monomial lowered(int indeterminate) {
			long unit = 1L << shift(indeterminate);
			return indeterminate < 8 ? new Monomial(first - unit, second)
					: new Monomial(first, second - unit);
		}

		/**
		 * Bytewise sum of two words of exponents: the low seven bits of every byte are added
		 * without a carry between bytes, and a carry out of the high bit of a byte is the
		 * overflow of that exponent.
		 */
		private static long add(long a, long b, ConstructiveIdentityCounters counters) {
			long low = (a & LOW_BITS) + (b & LOW_BITS);
			long overflow = ((a & b) | (low & (a | b))) & HIGH_BITS;
			if (overflow != 0) {
				throw counters.exhausted(
						ConstructiveIdentityBudgetExhaustedException.Limit.EXPONENT_RANGE);
			}
			return low ^ ((a ^ b) & HIGH_BITS);
		}

		private static int shift(int indeterminate) {
			return 8 * (7 - (indeterminate & 7));
		}

		/** Descending graded lexicographic order: the leading monomial comes first. */
		@Override
		public int compareTo(Monomial other) {
			if (degree != other.degree) {
				return Integer.compare(other.degree, degree);
			}
			int words = Long.compareUnsigned(other.first, first);
			return words != 0 ? words : Long.compareUnsigned(other.second, second);
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Monomial && first == ((Monomial) other).first
					&& second == ((Monomial) other).second;
		}

		@Override
		public int hashCode() {
			return Long.hashCode(first) * 31 + Long.hashCode(second);
		}

		@Override
		public String toString() {
			StringBuilder text = new StringBuilder();
			for (int index = 0; index < INDETERMINATES; index++) {
				int exponent = exponent(index);
				if (exponent > 0) {
					text.append(text.length() == 0 ? "" : "*").append('v').append(index);
					if (exponent > 1) {
						text.append('^').append(exponent);
					}
				}
			}
			return text.length() == 0 ? "1" : text.toString();
		}
	}

	private static void checkIndex(int indeterminate) {
		if (indeterminate < 0 || indeterminate >= INDETERMINATES) {
			throw new IllegalArgumentException("indeterminate " + indeterminate);
		}
	}

	/**
	 * @param value a constant
	 * @return the constant polynomial
	 */
	static CertificatePolynomial constant(ExactRational2D value) {
		TreeMap<Monomial, ExactRational2D> terms = new TreeMap<>();
		if (!value.isZero()) {
			terms.put(Monomial.ONE, value);
		}
		return new CertificatePolynomial(terms);
	}

	/**
	 * @param indeterminate index of an indeterminate
	 * @return the polynomial of degree one of that indeterminate
	 */
	static CertificatePolynomial indeterminate(int indeterminate) {
		TreeMap<Monomial, ExactRational2D> terms = new TreeMap<>();
		terms.put(Monomial.of(indeterminate), ExactRational2D.of(1));
		return new CertificatePolynomial(terms);
	}

	/** @return whether this is the zero polynomial */
	boolean isZero() {
		return terms.isEmpty();
	}

	/** @return whether it has no indeterminate */
	boolean isConstant() {
		return terms.isEmpty() || terms.size() == 1 && terms.firstKey().getDegree() == 0;
	}

	/** @return the constant term */
	ExactRational2D constantTerm() {
		ExactRational2D value = terms.get(Monomial.ONE);
		return value == null ? ExactRational2D.of(0) : value;
	}

	/** @return the number of terms */
	int size() {
		return terms.size();
	}

	/** @return the leading monomial; the polynomial must not be zero */
	Monomial leadingMonomial() {
		return terms.firstKey();
	}

	/** @return the leading coefficient; the polynomial must not be zero */
	ExactRational2D leadingCoefficient() {
		return terms.firstEntry().getValue();
	}

	/** @return the terms in descending order, unmodifiable */
	Map<Monomial, ExactRational2D> getTerms() {
		return Collections.unmodifiableMap(terms);
	}

	/** @return whether the two polynomials are equal */
	boolean sameAs(CertificatePolynomial other) {
		return terms.equals(other.terms);
	}

	CertificatePolynomial add(CertificatePolynomial other, ConstructiveIdentityCounters counters) {
		if (other.isZero()) {
			return this;
		}
		if (isZero()) {
			return other;
		}
		TreeMap<Monomial, ExactRational2D> sum = new TreeMap<>(terms);
		for (Map.Entry<Monomial, ExactRational2D> term : other.terms.entrySet()) {
			accumulate(sum, term.getKey(), term.getValue());
		}
		return checked(sum, counters);
	}

	CertificatePolynomial negate() {
		TreeMap<Monomial, ExactRational2D> negated = new TreeMap<>();
		for (Map.Entry<Monomial, ExactRational2D> term : terms.entrySet()) {
			negated.put(term.getKey(), term.getValue().negate());
		}
		return new CertificatePolynomial(negated);
	}

	CertificatePolynomial subtract(CertificatePolynomial other,
			ConstructiveIdentityCounters counters) {
		return add(other.negate(), counters);
	}

	CertificatePolynomial scale(ExactRational2D factor, ConstructiveIdentityCounters counters) {
		if (factor.isZero()) {
			return constant(factor);
		}
		TreeMap<Monomial, ExactRational2D> scaled = new TreeMap<>();
		for (Map.Entry<Monomial, ExactRational2D> term : terms.entrySet()) {
			scaled.put(term.getKey(), term.getValue().multiply(factor));
		}
		return checked(scaled, counters);
	}

	CertificatePolynomial multiply(CertificatePolynomial other,
			ConstructiveIdentityCounters counters) {
		if (isZero() || other.isZero()) {
			return constant(ExactRational2D.of(0));
		}
		counters.chargeTermProducts((long) terms.size() * other.terms.size());
		int[] top = maximumExponents();
		int[] bottom = other.maximumExponents();
		for (int index = 0; index < INDETERMINATES; index++) {
			if (top[index] + bottom[index] > MAXIMUM_EXPONENT) {
				throw counters.exhausted(
						ConstructiveIdentityBudgetExhaustedException.Limit.EXPONENT_RANGE);
			}
		}
		Map<Monomial, ExactRational2D> product = new HashMap<>();
		for (Map.Entry<Monomial, ExactRational2D> left : terms.entrySet()) {
			for (Map.Entry<Monomial, ExactRational2D> right : other.terms.entrySet()) {
				Monomial monomial = left.getKey().times(right.getKey(), counters);
				ExactRational2D value = left.getValue().multiply(right.getValue());
				ExactRational2D previous = product.get(monomial);
				product.put(monomial, previous == null ? value : previous.add(value));
			}
		}
		TreeMap<Monomial, ExactRational2D> sorted = new TreeMap<>();
		for (Map.Entry<Monomial, ExactRational2D> term : product.entrySet()) {
			if (!term.getValue().isZero()) {
				sorted.put(term.getKey(), term.getValue());
			}
		}
		return checked(sorted, counters);
	}

	/**
	 * Exact division: the quotient when the divisor divides this polynomial exactly, found by the
	 * division algorithm on leading terms; {@code null} when it does not, or when the sizes
	 * exceed the given limits, which only control the cost of an attempt and never its result.
	 */
	CertificatePolynomial divideExactly(CertificatePolynomial divisor, int maximumDivisorTerms,
			int maximumDividendTerms, ConstructiveIdentityCounters counters) {
		if (divisor.isZero() || divisor.terms.size() > maximumDivisorTerms
				|| terms.size() > maximumDividendTerms) {
			return null;
		}
		TreeMap<Monomial, ExactRational2D> remainder = new TreeMap<>(terms);
		TreeMap<Monomial, ExactRational2D> quotient = new TreeMap<>();
		Monomial lead = divisor.leadingMonomial();
		ExactRational2D leadValue = divisor.leadingCoefficient();
		while (!remainder.isEmpty()) {
			if (quotient.size() > maximumDividendTerms) {
				return null;
			}
			Map.Entry<Monomial, ExactRational2D> first = remainder.firstEntry();
			Monomial factorMonomial = first.getKey().dividedBy(lead);
			if (factorMonomial == null) {
				return null;
			}
			ExactRational2D factor = first.getValue().divide(leadValue);
			quotient.put(factorMonomial, factor);
			counters.chargeTermProducts(divisor.terms.size());
			for (Map.Entry<Monomial, ExactRational2D> term : divisor.terms.entrySet()) {
				accumulate(remainder, factorMonomial.times(term.getKey(), counters),
						factor.multiply(term.getValue()).negate());
			}
		}
		return checked(quotient, counters);
	}

	/**
	 * @return the exact square root when this polynomial is the square of a polynomial with a
	 *         positive leading coefficient, otherwise {@code null}
	 */
	CertificatePolynomial squareRoot(ConstructiveIdentityCounters counters) {
		if (isZero()) {
			return this;
		}
		Monomial half = leadingMonomial().half();
		ExactRational2D leadRoot = rationalSquareRoot(leadingCoefficient());
		if (half == null || leadRoot == null) {
			return null;
		}
		TreeMap<Monomial, ExactRational2D> first = new TreeMap<>();
		first.put(half, leadRoot);
		CertificatePolynomial root = new CertificatePolynomial(first);
		CertificatePolynomial remainder = subtract(root.multiply(root, counters), counters);
		ExactRational2D twice = leadRoot.add(leadRoot);
		for (int guard = 0; !remainder.isZero(); guard++) {
			if (guard > terms.size()) {
				return null;
			}
			Monomial next = remainder.leadingMonomial().dividedBy(half);
			if (next == null || next.compareTo(half) <= 0) {
				return null;
			}
			TreeMap<Monomial, ExactRational2D> single = new TreeMap<>();
			single.put(next, remainder.leadingCoefficient().divide(twice));
			CertificatePolynomial term = new CertificatePolynomial(single);
			remainder = remainder.subtract(root.multiply(term, counters)
					.scale(ExactRational2D.of(2), counters), counters)
					.subtract(term.multiply(term, counters), counters);
			root = root.add(term, counters);
		}
		return root;
	}

	/**
	 * @return the partial derivative in one indeterminate, computed afresh: a certificate keeps
	 *         its conditions and never their derivatives
	 */
	CertificatePolynomial partial(int indeterminate, ConstructiveIdentityCounters counters) {
		checkIndex(indeterminate);
		TreeMap<Monomial, ExactRational2D> derivative = new TreeMap<>();
		for (Map.Entry<Monomial, ExactRational2D> term : terms.entrySet()) {
			int exponent = term.getKey().exponent(indeterminate);
			if (exponent > 0) {
				derivative.put(term.getKey().lowered(indeterminate),
						term.getValue().multiply(ExactRational2D.of(exponent)));
			}
		}
		return checked(derivative, counters);
	}

	/**
	 * @param point exact values of the indeterminates; {@code null} for one that does not occur
	 * @return the exact value of the polynomial at the point, from the powers of every value
	 *         computed once; exact arithmetic makes it the value of the term-by-term products
	 */
	ExactRational2D valueAt(ExactRational2D[] point) {
		int[] occurring = maximumExponents();
		ExactRational2D[][] powers = new ExactRational2D[INDETERMINATES][];
		for (int index = 0; index < INDETERMINATES; index++) {
			if (occurring[index] > 0) {
				powers[index] = powers(Objects.requireNonNull(point[index]), occurring[index]);
			}
		}
		ExactRational2D sum = ExactRational2D.of(0);
		for (Map.Entry<Monomial, ExactRational2D> term : terms.entrySet()) {
			ExactRational2D product = term.getValue();
			Monomial monomial = term.getKey();
			for (int index = 0; index < INDETERMINATES; index++) {
				int exponent = monomial.exponent(index);
				if (exponent > 0) {
					product = product.multiply(powers[index][exponent]);
				}
			}
			sum = sum.add(product);
		}
		return sum;
	}

	/** @return the powers {@code value^0} to {@code value^maximum} */
	private static ExactRational2D[] powers(ExactRational2D value, int maximum) {
		ExactRational2D[] powers = new ExactRational2D[maximum + 1];
		powers[0] = ExactRational2D.of(1);
		for (int power = 1; power <= maximum; power++) {
			powers[power] = powers[power - 1].multiply(value);
		}
		return powers;
	}

	/**
	 * Rigorous enclosure on a box in the mean-value form: the exact value at the centre of the
	 * box plus the enclosures of the partial derivatives times the half-widths. The first-order
	 * form encloses the derivatives naively; when it certifies no sign, the second-order form
	 * encloses them in the first-order form, with naive second derivatives, and both enclosures
	 * are intersected. The naive enclosure of an expanded polynomial loses its sign to
	 * cancellation on small boxes; the exact values at the centre do not. An unbounded side of
	 * the box gives the naive enclosure.
	 *
	 * @param box one interval for each indeterminate that occurs
	 * @return the enclosure of the polynomial over the box
	 */
	OutwardInterval enclose(OutwardInterval[] box, ConstructiveIdentityCounters counters) {
		OutwardInterval first = enclose(box, 1, counters);
		if (first.certifiedSign() != ConstructiveArithmetic.UNDECIDED || !first.isFinite()) {
			return first;
		}
		OutwardInterval second = enclose(box, 2, counters);
		return OutwardInterval.of(Math.max(first.getLower(), second.getLower()),
				Math.min(first.getUpper(), second.getUpper()));
	}

	private OutwardInterval enclose(OutwardInterval[] box, int order,
			ConstructiveIdentityCounters counters) {
		if (terms.isEmpty()) {
			return OutwardInterval.point(0);
		}
		int[] occurring = maximumExponents();
		boolean bounded = true;
		for (int index = 0; index < INDETERMINATES && bounded; index++) {
			bounded = occurring[index] == 0 || box[index].isFinite();
		}
		if (order == 0 || !bounded) {
			return encloseNaively(box);
		}
		ExactRational2D[] centre = new ExactRational2D[INDETERMINATES];
		for (int index = 0; index < INDETERMINATES; index++) {
			if (occurring[index] > 0) {
				centre[index] = ExactRational2D.midpoint(
						ExactRational2D.ofBinary64(box[index].getLower()),
						ExactRational2D.ofBinary64(box[index].getUpper()));
			}
		}
		OutwardInterval value = OutwardInterval.enclosing(valueAt(centre));
		for (int index = 0; index < INDETERMINATES; index++) {
			if (occurring[index] == 0) {
				continue;
			}
			double radius = ExactRational2D.ofBinary64(box[index].getUpper())
					.subtract(centre[index]).outwardDoubleEnclosure()[1];
			value = value.add(partial(index, counters).enclose(box, order - 1, counters)
					.multiply(OutwardInterval.of(-radius, radius)));
		}
		return value;
	}

	private OutwardInterval encloseNaively(OutwardInterval[] box) {
		OutwardInterval sum = OutwardInterval.point(0);
		for (Map.Entry<Monomial, ExactRational2D> term : terms.entrySet()) {
			OutwardInterval value = OutwardInterval.enclosing(term.getValue());
			Monomial monomial = term.getKey();
			for (int index = 0; index < INDETERMINATES; index++) {
				int exponent = monomial.exponent(index);
				for (int power = 0; power < exponent; power++) {
					value = value.multiply(box[index]);
				}
			}
			sum = sum.add(value);
		}
		return sum;
	}

	/** @return the largest exponent of every indeterminate, computed once */
	int[] maximumExponents() {
		if (maximumExponents == null) {
			int[] maximum = new int[INDETERMINATES];
			for (Monomial monomial : terms.keySet()) {
				for (int index = 0; index < INDETERMINATES; index++) {
					maximum[index] = Math.max(maximum[index], monomial.exponent(index));
				}
			}
			maximumExponents = maximum;
		}
		return maximumExponents;
	}

	private static void accumulate(TreeMap<Monomial, ExactRational2D> terms, Monomial monomial,
			ExactRational2D value) {
		ExactRational2D previous = terms.get(monomial);
		ExactRational2D next = previous == null ? value : previous.add(value);
		if (next.isZero()) {
			terms.remove(monomial);
		} else {
			terms.put(monomial, next);
		}
	}

	private static CertificatePolynomial checked(TreeMap<Monomial, ExactRational2D> terms,
			ConstructiveIdentityCounters counters) {
		counters.recordPolynomial(terms.size(), maximumBits(terms));
		return new CertificatePolynomial(terms);
	}

	private static int maximumBits(Map<Monomial, ExactRational2D> terms) {
		int bits = 0;
		for (ExactRational2D value : terms.values()) {
			bits = Math.max(bits, value.bitLength());
		}
		return bits;
	}

	/** @return the exact square root of a nonnegative rational square, otherwise {@code null} */
	static ExactRational2D rationalSquareRoot(ExactRational2D value) {
		if (value.signum() < 0) {
			return null;
		}
		java.math.BigInteger numerator = value.numerator().sqrt();
		java.math.BigInteger denominator = value.denominator().sqrt();
		return numerator.multiply(numerator).equals(value.numerator())
				&& denominator.multiply(denominator).equals(value.denominator())
				? ExactRational2D.of(numerator, denominator) : null;
	}

	@Override
	public String toString() {
		if (terms.isEmpty()) {
			return "0";
		}
		StringBuilder text = new StringBuilder();
		for (Map.Entry<Monomial, ExactRational2D> term : terms.entrySet()) {
			if (text.length() > 0) {
				text.append(" + ");
			}
			text.append(term.getValue()).append(term.getKey().getDegree() == 0 ? ""
					: "*" + term.getKey());
		}
		return text.toString();
	}
}
