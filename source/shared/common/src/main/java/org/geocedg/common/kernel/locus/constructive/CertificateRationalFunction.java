/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * A rational function, numerator over denominator, in the indeterminates of one constructive
 * identity certificate. The denominator is never the zero polynomial: a constant denominator is
 * one, and any other is scaled to a leading coefficient one. Exact division keeps the sizes down
 * when it applies; it never changes the value. A rational function is zero exactly when its
 * numerator is the zero polynomial.
 */
final class CertificateRationalFunction {

	/** Exact division is attempted only below these sizes; they control cost, never results. */
	private static final int DIVISION_DIVISOR_TERMS = 24;
	private static final int DIVISION_DIVIDEND_TERMS = 6_000;

	private final CertificatePolynomial numerator;
	private final CertificatePolynomial denominator;

	private CertificateRationalFunction(CertificatePolynomial numerator,
			CertificatePolynomial denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}

	/**
	 * @return the normalized quotient
	 * @throws IllegalArgumentException if the denominator is the zero polynomial
	 */
	static CertificateRationalFunction of(CertificatePolynomial numerator,
			CertificatePolynomial denominator, ConstructiveIdentityCounters counters) {
		Objects.requireNonNull(numerator);
		if (denominator.isZero()) {
			throw new IllegalArgumentException("a zero denominator");
		}
		ExactRational2D one = ExactRational2D.of(1);
		if (numerator.isZero()) {
			return new CertificateRationalFunction(numerator, CertificatePolynomial.constant(one));
		}
		if (denominator.isConstant()) {
			return new CertificateRationalFunction(numerator.scale(
					one.divide(denominator.constantTerm()), counters),
					CertificatePolynomial.constant(one));
		}
		CertificatePolynomial quotient = numerator.divideExactly(denominator,
				DIVISION_DIVISOR_TERMS, DIVISION_DIVIDEND_TERMS, counters);
		if (quotient != null) {
			return new CertificateRationalFunction(quotient, CertificatePolynomial.constant(one));
		}
		ExactRational2D scale = one.divide(denominator.leadingCoefficient());
		return new CertificateRationalFunction(numerator.scale(scale, counters),
				denominator.scale(scale, counters));
	}

	/** @return the polynomial as a rational function */
	static CertificateRationalFunction of(CertificatePolynomial polynomial) {
		return new CertificateRationalFunction(polynomial,
				CertificatePolynomial.constant(ExactRational2D.of(1)));
	}

	/** @return the numerator */
	CertificatePolynomial getNumerator() {
		return numerator;
	}

	/** @return the denominator */
	CertificatePolynomial getDenominator() {
		return denominator;
	}

	/** @return whether it is the zero function */
	boolean isZero() {
		return numerator.isZero();
	}

	/** @return whether it has no indeterminate */
	boolean isConstant() {
		return numerator.isConstant() && denominator.isConstant();
	}

	/** @return the terms of its numerator and denominator */
	int size() {
		return numerator.size() + denominator.size();
	}

	CertificateRationalFunction add(CertificateRationalFunction other,
			ConstructiveIdentityCounters counters) {
		if (other.isZero()) {
			return this;
		}
		if (isZero()) {
			return other;
		}
		if (denominator.sameAs(other.denominator)) {
			return of(numerator.add(other.numerator, counters), denominator, counters);
		}
		CertificatePolynomial factor = other.denominator.divideExactly(denominator,
				DIVISION_DIVISOR_TERMS, DIVISION_DIVIDEND_TERMS, counters);
		if (factor != null) {
			return of(numerator.multiply(factor, counters).add(other.numerator, counters),
					other.denominator, counters);
		}
		factor = denominator.divideExactly(other.denominator, DIVISION_DIVISOR_TERMS,
				DIVISION_DIVIDEND_TERMS, counters);
		if (factor != null) {
			return of(numerator.add(other.numerator.multiply(factor, counters), counters),
					denominator, counters);
		}
		return of(numerator.multiply(other.denominator, counters)
				.add(other.numerator.multiply(denominator, counters), counters),
				denominator.multiply(other.denominator, counters), counters);
	}

	CertificateRationalFunction negate() {
		return new CertificateRationalFunction(numerator.negate(), denominator);
	}

	CertificateRationalFunction subtract(CertificateRationalFunction other,
			ConstructiveIdentityCounters counters) {
		return add(other.negate(), counters);
	}

	CertificateRationalFunction multiply(CertificateRationalFunction other,
			ConstructiveIdentityCounters counters) {
		if (isZero() || other.isZero()) {
			return of(CertificatePolynomial.constant(ExactRational2D.of(0)));
		}
		return of(numerator.multiply(other.numerator, counters),
				denominator.multiply(other.denominator, counters), counters);
	}

	/**
	 * @throws IllegalArgumentException if the divisor is the zero function
	 */
	CertificateRationalFunction divide(CertificateRationalFunction other,
			ConstructiveIdentityCounters counters) {
		if (other.isZero()) {
			throw new IllegalArgumentException("a division by the zero function");
		}
		return of(numerator.multiply(other.denominator, counters),
				denominator.multiply(other.numerator, counters), counters);
	}

	/**
	 * @return a rigorous enclosure over the box, or {@code null} when the enclosure of the
	 *         denominator contains zero
	 */
	OutwardInterval enclose(OutwardInterval[] box, ConstructiveIdentityCounters counters) {
		OutwardInterval top = numerator.enclose(box, counters);
		if (denominator.isConstant()) {
			return top.multiply(OutwardInterval.enclosing(ExactRational2D.of(1)
					.divide(denominator.constantTerm())));
		}
		OutwardInterval bottom = denominator.enclose(box, counters);
		int sign = bottom.certifiedSign();
		if (sign != 1 && sign != -1) {
			return null;
		}
		return top.divide(bottom);
	}

	@Override
	public String toString() {
		return denominator.isConstant() ? numerator.toString()
				: "(" + numerator + ") / (" + denominator + ")";
	}
}
