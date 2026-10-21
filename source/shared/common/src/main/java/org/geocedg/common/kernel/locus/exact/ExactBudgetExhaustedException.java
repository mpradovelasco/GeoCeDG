/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

/**
 * Typed failure of an exact computation that would exceed its {@link ExactBudget1D}.
 *
 * <p>It never carries a partial result: the computation that throws it decides nothing.</p>
 */
public final class ExactBudgetExhaustedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final Reason reason;
	private final int limit;
	private final int required;

	/** Exhausted limit. */
	public enum Reason {
		/** A coefficient or a derived integer exceeded the maximum bit length. */
		COEFFICIENT_BITS,
		/** One root needed more exact bisections than allowed. */
		BISECTIONS
	}

	ExactBudgetExhaustedException(Reason reason, int limit, int required) {
		super("Exact budget exhausted: " + reason + " limit " + limit + ", required "
				+ required);
		this.reason = reason;
		this.limit = limit;
		this.required = required;
	}

	/** @return the exhausted limit */
	public Reason reason() {
		return reason;
	}

	/** @return the configured value of the exhausted limit */
	public int limit() {
		return limit;
	}

	/** @return the value that exceeded the limit (bits, or bisections for one root) */
	public int required() {
		return required;
	}
}
