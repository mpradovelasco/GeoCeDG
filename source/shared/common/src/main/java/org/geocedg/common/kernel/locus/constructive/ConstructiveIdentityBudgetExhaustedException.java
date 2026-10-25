/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

/**
 * An attempt of a constructive identity certificate exceeded its versioned budget, or an exponent
 * its representable range. Nothing is decided: the relation is {@code NOT_ESTABLISHED} with the
 * typed cause and the counters.
 */
public final class ConstructiveIdentityBudgetExhaustedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** The limit that was exceeded. */
	public enum Limit {
		/** The term products of the attempt. */
		TERM_PRODUCTS,
		/** The indeterminates of the attempt. */
		INDETERMINATES,
		/** The terms of one polynomial. */
		TERMS,
		/** The bit length of one coefficient. */
		BITS,
		/** An exponent beyond the 255 that a monomial represents; never wrapped. */
		EXPONENT_RANGE
	}

	private final Limit limit;
	private final transient ConstructiveIdentityCounters counters;

	ConstructiveIdentityBudgetExhaustedException(Limit limit,
			ConstructiveIdentityCounters counters) {
		super((limit == Limit.EXPONENT_RANGE ? "EXPONENT_RANGE_EXCEEDED"
				: "CONSTRUCTIVE_IDENTITY_BUDGET_EXHAUSTED: " + limit) + " (" + counters + ")");
		this.limit = Objects.requireNonNull(limit);
		this.counters = Objects.requireNonNull(counters);
	}

	/** @return the limit that was exceeded */
	public Limit getLimit() {
		return limit;
	}

	/** @return a copy of the counters at the moment the limit was exceeded */
	public ConstructiveIdentityCounters getCounters() {
		return counters;
	}
}
