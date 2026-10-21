/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sturm sequence of a square-free polynomial, for counting distinct real roots exactly.
 *
 * <p>The sequence is {@code s0 = s}, {@code s1 = s'} and {@code s(k+1) = -rem(s(k-1), s(k))},
 * each element replaced by its primitive integer form; that positive scaling preserves every
 * sign. When the polynomial given is not square-free its square-free part is used, so the
 * counts are always of distinct roots. For {@code a < b} the number of distinct real roots
 * in the half-open interval {@code (a, b]} is {@code V(a) - V(b)}, where {@code V} counts
 * the sign changes of the sequence and ignores zeros; this holds also when {@code a} or
 * {@code b} is a root.</p>
 */
public final class SturmSequence1D {

	private final List<ExactPolynomial1D> polynomials;
	private final BigInteger[][] integerPolynomials;

	private SturmSequence1D(List<ExactPolynomial1D> polynomials,
			BigInteger[][] integerPolynomials) {
		this.polynomials = Collections.unmodifiableList(polynomials);
		this.integerPolynomials = integerPolynomials;
	}

	/**
	 * Builds the sequence under {@link ExactBudget1D#DEFAULT}.
	 *
	 * @param squareFree nonzero polynomial, normally square-free; otherwise its square-free
	 *        part is used
	 * @return the Sturm sequence
	 * @throws IllegalArgumentException for the zero polynomial
	 * @throws ExactBudgetExhaustedException if a coefficient exceeds the default bit limit
	 */
	public static SturmSequence1D of(ExactPolynomial1D squareFree) {
		return of(squareFree, ExactBudget1D.DEFAULT.newMeter());
	}

	/** Metered construction. */
	static SturmSequence1D of(ExactPolynomial1D squareFree, ExactBudget1D.Meter meter) {
		if (squareFree.isZero()) {
			throw new IllegalArgumentException("The zero polynomial has no Sturm sequence");
		}
		meter.observe(squareFree);
		List<ExactPolynomial1D> sequence = new ArrayList<>();
		sequence.add(observed(squareFree.primitivePositive(), meter));
		if (squareFree.degree() > 0) {
			sequence.add(observed(sequence.get(0).derivative().primitivePositive(), meter));
			while (true) {
				int last = sequence.size() - 1;
				ExactPolynomial1D remainder =
						sequence.get(last - 1).divideAndRemainder(sequence.get(last))[1];
				meter.observe(remainder);
				if (remainder.isZero()) {
					break;
				}
				sequence.add(observed(remainder.negate().primitivePositive(), meter));
			}
			ExactPolynomial1D common = sequence.get(sequence.size() - 1);
			if (common.degree() > 0) {
				ExactPolynomial1D part = squareFree.exactQuotient(common);
				return of(part, meter);
			}
		}
		BigInteger[][] integers = new BigInteger[sequence.size()][];
		for (int i = 0; i < integers.length; i++) {
			integers[i] = sequence.get(i).primitiveIntegerCoefficients();
			meter.observe(integers[i]);
		}
		return new SturmSequence1D(sequence, integers);
	}

	private static ExactPolynomial1D observed(ExactPolynomial1D polynomial,
			ExactBudget1D.Meter meter) {
		meter.observe(polynomial);
		return polynomial;
	}

	/**
	 * @return the unmodifiable sequence, each element in primitive integer form; the first
	 *         element is the square-free polynomial whose roots are counted
	 */
	public List<ExactPolynomial1D> polynomials() {
		return polynomials;
	}

	/** @return the square-free polynomial whose roots are counted, in primitive form */
	public ExactPolynomial1D polynomial() {
		return polynomials.get(0);
	}

	/**
	 * Counts distinct real roots in a half-open interval.
	 *
	 * @param lowerExclusive excluded lower end, or {@code null} for minus infinity
	 * @param upperInclusive included upper end, or {@code null} for plus infinity
	 * @return the number of distinct real roots {@code r} with
	 *         {@code lowerExclusive < r <= upperInclusive}; 0 when the ends are equal
	 * @throws IllegalArgumentException if the lower end is greater than the upper end
	 */
	public int countDistinctRootsIn(ExactRational2D lowerExclusive,
			ExactRational2D upperInclusive) {
		if (lowerExclusive != null && upperInclusive != null) {
			int order = lowerExclusive.compareTo(upperInclusive);
			if (order > 0) {
				throw new IllegalArgumentException("Lower end above upper end");
			}
			if (order == 0) {
				return 0;
			}
		}
		int lowerVariations = lowerExclusive == null ? variationsAtNegativeInfinity()
				: variationsAndSignAt(lowerExclusive)[0];
		int upperVariations = upperInclusive == null ? variationsAtPositiveInfinity()
				: variationsAndSignAt(upperInclusive)[0];
		return lowerVariations - upperVariations;
	}

	/** @return the number of distinct real roots */
	public int countAllRealRoots() {
		return variationsAtNegativeInfinity() - variationsAtPositiveInfinity();
	}

	/**
	 * @return {@code {V(x), sign of the first element at x}}; the second entry is 0 exactly
	 *         when {@code x} is a root
	 */
	int[] variationsAndSignAt(ExactRational2D x) {
		int variations = 0;
		int previous = 0;
		int first = 0;
		for (int i = 0; i < integerPolynomials.length; i++) {
			int sign = ExactPolynomial1D.signOfIntegerPolynomial(integerPolynomials[i], x);
			if (i == 0) {
				first = sign;
			}
			if (sign != 0) {
				if (previous != 0 && sign != previous) {
					variations++;
				}
				previous = sign;
			}
		}
		return new int[] {variations, first};
	}

	private int variationsAtPositiveInfinity() {
		int variations = 0;
		int previous = 0;
		for (ExactPolynomial1D element : polynomials) {
			int sign = element.signAtPositiveInfinity();
			if (previous != 0 && sign != previous) {
				variations++;
			}
			previous = sign;
		}
		return variations;
	}

	private int variationsAtNegativeInfinity() {
		int variations = 0;
		int previous = 0;
		for (ExactPolynomial1D element : polynomials) {
			int sign = element.signAtNegativeInfinity();
			if (previous != 0 && sign != previous) {
				variations++;
			}
			previous = sign;
		}
		return variations;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SturmSequence1D)) {
			return false;
		}
		return polynomials.equals(((SturmSequence1D) other).polynomials);
	}

	@Override
	public int hashCode() {
		return polynomials.hashCode();
	}

	@Override
	public String toString() {
		return "SturmSequence1D" + polynomials;
	}
}
