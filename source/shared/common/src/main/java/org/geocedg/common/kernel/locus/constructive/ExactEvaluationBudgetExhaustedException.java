/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

/**
 * An exact evaluation exceeded its versioned budget. The decision is not made: the consumer
 * fails closed with {@code EXACT_EVALUATION_BUDGET_EXHAUSTED} and reports the counters.
 */
public final class ExactEvaluationBudgetExhaustedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** The limit that was exceeded. */
	public enum Limit {
		/** The counted exact operations. */
		OPERATIONS,
		/** The depth of the tower of quadratic extensions. */
		DEPTH,
		/** The bit length of a rational. */
		BITS
	}

	private final Limit limit;
	private final transient ExactEvaluationCounters counters;

	ExactEvaluationBudgetExhaustedException(Limit limit, ExactEvaluationCounters counters,
			ExactEvaluationBudget budget) {
		super("EXACT_EVALUATION_BUDGET_EXHAUSTED: " + limit + " (" + counters + "; "
				+ budget + ")");
		this.limit = Objects.requireNonNull(limit);
		this.counters = Objects.requireNonNull(counters);
	}

	/** @return the limit that was exceeded */
	public Limit getLimit() {
		return limit;
	}

	/** @return the deterministic counters at the moment the limit was exceeded */
	public ExactEvaluationCounters getCounters() {
		return counters;
	}
}
