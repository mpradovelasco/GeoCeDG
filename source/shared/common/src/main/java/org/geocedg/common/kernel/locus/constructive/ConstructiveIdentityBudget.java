/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

/**
 * The versioned budget {@code constructive-identity-certificate-budget/v1} of one attempt of a
 * constructive identity certificate, fixed by the author on 2026-10-25 as initial engineering
 * ceilings ({@code locus-v2-constructive-authority.md} section 10): 16 000 000 term products,
 * 16 indeterminates, 200 000 terms per polynomial and 4 096 bits per coefficient (provisional).
 * They are not a statement of sufficiency. Exhausting one fails closed with
 * {@code NOT_ESTABLISHED} and its counters; a limit is never raised by the implementation.
 */
public final class ConstructiveIdentityBudget {

	/** Versioned name of the budget. */
	public static final String VERSION = "constructive-identity-certificate-budget/v1";
	/** Maximum of term products of one attempt. */
	public static final long MAXIMUM_TERM_PRODUCTS = 16_000_000L;
	/** Maximum of indeterminates of one attempt. */
	public static final int MAXIMUM_INDETERMINATES = 16;
	/** Maximum of terms of one polynomial. */
	public static final int MAXIMUM_TERMS = 200_000;
	/** Maximum bit length of one coefficient (provisional). */
	public static final int MAXIMUM_BITS = 4_096;

	/** The budget fixed by the author. */
	public static final ConstructiveIdentityBudget V1 = new ConstructiveIdentityBudget(
			MAXIMUM_TERM_PRODUCTS, MAXIMUM_INDETERMINATES, MAXIMUM_TERMS, MAXIMUM_BITS);

	private final long maximumTermProducts;
	private final int maximumIndeterminates;
	private final int maximumTerms;
	private final int maximumBits;

	private ConstructiveIdentityBudget(long maximumTermProducts, int maximumIndeterminates,
			int maximumTerms, int maximumBits) {
		this.maximumTermProducts = maximumTermProducts;
		this.maximumIndeterminates = maximumIndeterminates;
		this.maximumTerms = maximumTerms;
		this.maximumBits = maximumBits;
	}

	/**
	 * A budget restricted below the fixed one, for the adversarial injections of G-AUT-8. It can
	 * only lower a limit, so it can only make an attempt fail closed more often.
	 *
	 * @return the restricted budget
	 * @throws IllegalArgumentException if a limit exceeds the fixed one or is not positive
	 */
	public static ConstructiveIdentityBudget restrictedTo(long maximumTermProducts,
			int maximumIndeterminates, int maximumTerms, int maximumBits) {
		if (maximumTermProducts <= 0 || maximumTermProducts > MAXIMUM_TERM_PRODUCTS
				|| maximumIndeterminates < 0 || maximumIndeterminates > MAXIMUM_INDETERMINATES
				|| maximumTerms <= 0 || maximumTerms > MAXIMUM_TERMS
				|| maximumBits <= 0 || maximumBits > MAXIMUM_BITS) {
			throw new IllegalArgumentException("A budget can only be restricted below " + VERSION);
		}
		return new ConstructiveIdentityBudget(maximumTermProducts, maximumIndeterminates,
				maximumTerms, maximumBits);
	}

	/** @return the maximum of term products of one attempt */
	public long getMaximumTermProducts() {
		return maximumTermProducts;
	}

	/** @return the maximum of indeterminates of one attempt */
	public int getMaximumIndeterminates() {
		return maximumIndeterminates;
	}

	/** @return the maximum of terms of one polynomial */
	public int getMaximumTerms() {
		return maximumTerms;
	}

	/** @return the maximum bit length of one coefficient */
	public int getMaximumBits() {
		return maximumBits;
	}

	/** @return whether this is the budget fixed by the author */
	public boolean isFixed() {
		return maximumTermProducts == MAXIMUM_TERM_PRODUCTS
				&& maximumIndeterminates == MAXIMUM_INDETERMINATES
				&& maximumTerms == MAXIMUM_TERMS && maximumBits == MAXIMUM_BITS;
	}

	@Override
	public String toString() {
		return VERSION + (isFixed() ? "" : " restricted") + "[termProducts="
				+ maximumTermProducts + ", indeterminates=" + maximumIndeterminates + ", terms="
				+ maximumTerms + ", bits=" + maximumBits + "]";
	}
}
