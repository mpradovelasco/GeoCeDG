/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Square-free decomposition of a nonzero rational polynomial by Yun's algorithm.
 *
 * <p>{@code p = content * product of factors().get(i)^(i + 1)}, where factor {@code i} is
 * the monic square-free product of the irreducible factors of multiplicity {@code i + 1}
 * ({@link ExactPolynomial1D#ONE} when there is none) and {@code content} is the leading
 * coefficient of {@code p}. The list has one entry per multiplicity up to the largest, so
 * its last factor is never constant; it is empty for a constant polynomial. The factors
 * are pairwise coprime.</p>
 */
public final class SquareFreeDecomposition1D {

	private final ExactPolynomial1D polynomial;
	private final ExactRational2D content;
	private final List<ExactPolynomial1D> factors;
	private final ExactPolynomial1D squareFreePart;

	private SquareFreeDecomposition1D(ExactPolynomial1D polynomial, ExactRational2D content,
			List<ExactPolynomial1D> factors, ExactPolynomial1D squareFreePart) {
		this.polynomial = polynomial;
		this.content = content;
		this.factors = Collections.unmodifiableList(factors);
		this.squareFreePart = squareFreePart;
	}

	/**
	 * Decomposes under {@link ExactBudget1D#DEFAULT}.
	 *
	 * @param p nonzero polynomial
	 * @return the decomposition
	 * @throws IllegalArgumentException for the zero polynomial
	 * @throws ExactBudgetExhaustedException if a coefficient exceeds the default bit limit
	 */
	public static SquareFreeDecomposition1D of(ExactPolynomial1D p) {
		return of(p, ExactBudget1D.DEFAULT.newMeter());
	}

	/** Metered decomposition. */
	static SquareFreeDecomposition1D of(ExactPolynomial1D p, ExactBudget1D.Meter meter) {
		if (p.isZero()) {
			throw new IllegalArgumentException("The zero polynomial has no decomposition");
		}
		meter.observe(p);
		ExactRational2D content = p.leadingCoefficient();
		List<ExactPolynomial1D> factors = new ArrayList<>();
		if (p.degree() == 0) {
			return new SquareFreeDecomposition1D(p, content, factors, ExactPolynomial1D.ONE);
		}
		ExactPolynomial1D monic = p.monic();
		ExactPolynomial1D derivative = monic.derivative();
		ExactPolynomial1D common = monic.gcd(derivative, meter);
		ExactPolynomial1D b = observed(monic.exactQuotient(common), meter);
		ExactPolynomial1D c = observed(derivative.exactQuotient(common), meter);
		ExactPolynomial1D d = observed(c.subtract(b.derivative()), meter);
		ExactPolynomial1D squareFreePart = b;
		while (b.degree() > 0) {
			ExactPolynomial1D factor = b.gcd(d, meter);
			factors.add(factor);
			b = observed(b.exactQuotient(factor), meter);
			c = observed(d.exactQuotient(factor), meter);
			d = observed(c.subtract(b.derivative()), meter);
		}
		return new SquareFreeDecomposition1D(p, content, factors, squareFreePart);
	}

	private static ExactPolynomial1D observed(ExactPolynomial1D polynomial,
			ExactBudget1D.Meter meter) {
		meter.observe(polynomial);
		return polynomial;
	}

	/** @return the decomposed polynomial */
	public ExactPolynomial1D polynomial() {
		return polynomial;
	}

	/** @return the leading coefficient of the decomposed polynomial */
	public ExactRational2D content() {
		return content;
	}

	/**
	 * @return the unmodifiable monic factors; entry {@code i} collects the roots of
	 *         multiplicity {@code i + 1}
	 */
	public List<ExactPolynomial1D> factors() {
		return factors;
	}

	/** @return the monic square-free part, the product of all factors */
	public ExactPolynomial1D squareFreePart() {
		return squareFreePart;
	}

	/** @return the largest multiplicity of a root (real or complex); 0 for a constant */
	public int maximumMultiplicity() {
		return factors.size();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SquareFreeDecomposition1D)) {
			return false;
		}
		SquareFreeDecomposition1D that = (SquareFreeDecomposition1D) other;
		return polynomial.equals(that.polynomial) && content.equals(that.content)
				&& factors.equals(that.factors);
	}

	@Override
	public int hashCode() {
		return 31 * (31 * polynomial.hashCode() + content.hashCode()) + factors.hashCode();
	}

	@Override
	public String toString() {
		return "SquareFreeDecomposition1D[content=" + content + ", factors=" + factors + "]";
	}
}
