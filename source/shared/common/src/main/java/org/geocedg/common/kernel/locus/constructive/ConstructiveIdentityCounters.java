/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

/**
 * Deterministic counters of one attempt of a constructive identity certificate, checked against
 * its budget at every step: term products, indeterminates, the largest polynomial and the
 * longest coefficient. Times are never counted. The counters of two attempts on the same cone are
 * equal.
 */
public final class ConstructiveIdentityCounters {

	private final ConstructiveIdentityBudget budget;
	private long termProducts;
	private int indeterminates;
	private int largestTerms;
	private int largestBits;

	/** @param budget the budget of the attempt */
	public ConstructiveIdentityCounters(ConstructiveIdentityBudget budget) {
		this.budget = Objects.requireNonNull(budget);
	}

	private ConstructiveIdentityCounters(ConstructiveIdentityCounters other) {
		budget = other.budget;
		termProducts = other.termProducts;
		indeterminates = other.indeterminates;
		largestTerms = other.largestTerms;
		largestBits = other.largestBits;
	}

	/** @return the budget of the attempt */
	public ConstructiveIdentityBudget getBudget() {
		return budget;
	}

	/** @return the term products so far */
	public long getTermProducts() {
		return termProducts;
	}

	/** @return the indeterminates allocated */
	public int getIndeterminates() {
		return indeterminates;
	}

	/** @return the terms of the largest polynomial built */
	public int getLargestTerms() {
		return largestTerms;
	}

	/** @return the bit length of the longest coefficient built */
	public int getLargestBits() {
		return largestBits;
	}

	void chargeTermProducts(long products) {
		termProducts += products;
		if (termProducts > budget.getMaximumTermProducts()) {
			throw exhausted(ConstructiveIdentityBudgetExhaustedException.Limit.TERM_PRODUCTS);
		}
	}

	/** @return the index of a new indeterminate */
	int allocateIndeterminate() {
		if (indeterminates >= Math.min(budget.getMaximumIndeterminates(),
				CertificatePolynomial.INDETERMINATES)) {
			throw exhausted(ConstructiveIdentityBudgetExhaustedException.Limit.INDETERMINATES);
		}
		return indeterminates++;
	}

	void recordPolynomial(int terms, int bits) {
		largestTerms = Math.max(largestTerms, terms);
		largestBits = Math.max(largestBits, bits);
		if (terms > budget.getMaximumTerms()) {
			throw exhausted(ConstructiveIdentityBudgetExhaustedException.Limit.TERMS);
		}
		if (bits > budget.getMaximumBits()) {
			throw exhausted(ConstructiveIdentityBudgetExhaustedException.Limit.BITS);
		}
	}

	ConstructiveIdentityBudgetExhaustedException exhausted(
			ConstructiveIdentityBudgetExhaustedException.Limit limit) {
		return new ConstructiveIdentityBudgetExhaustedException(limit,
				new ConstructiveIdentityCounters(this));
	}

	@Override
	public String toString() {
		return "termProducts=" + termProducts + ", indeterminates=" + indeterminates
				+ ", largestTerms=" + largestTerms + ", largestBits=" + largestBits + "; "
				+ budget;
	}
}
