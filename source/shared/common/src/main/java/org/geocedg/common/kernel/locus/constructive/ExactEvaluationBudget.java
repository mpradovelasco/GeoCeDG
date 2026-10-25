/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

/**
 * The versioned budget {@code cu3-exact-evaluation-budget/v1} of one exact evaluation of a
 * structural point against its target, approved by the author on 2026-10-25
 * ({@code locus-v2-constructive-authority.md} section 10): 100 000 exact operations, tower depth
 * 8 and 4 096 bits. Each of the {@code N + 1} evaluations of {@code SUBSTITUTE_GENERIC} is one
 * such evaluation. Exhausting it fails closed with {@code EXACT_EVALUATION_BUDGET_EXHAUSTED}; the
 * limits are never raised automatically.
 */
public final class ExactEvaluationBudget {

	/** Versioned name of the budget. */
	public static final String VERSION = "cu3-exact-evaluation-budget/v1";
	/** Maximum of counted exact operations of version 1. */
	public static final long MAXIMUM_OPERATIONS = 100_000L;
	/** Maximum tower depth of version 1. */
	public static final int MAXIMUM_DEPTH = 8;
	/** Maximum bit length of every rational of version 1. */
	public static final int MAXIMUM_BITS = 4_096;

	/** The approved budget. */
	public static final ExactEvaluationBudget V1 = new ExactEvaluationBudget(MAXIMUM_OPERATIONS,
			MAXIMUM_DEPTH, MAXIMUM_BITS);

	private final long maximumOperations;
	private final int maximumDepth;
	private final int maximumBits;

	private ExactEvaluationBudget(long maximumOperations, int maximumDepth, int maximumBits) {
		this.maximumOperations = maximumOperations;
		this.maximumDepth = maximumDepth;
		this.maximumBits = maximumBits;
	}

	/**
	 * A budget restricted below the approved one, for the adversarial injection of the matrix
	 * (V-M3-8). It can only lower a limit, never raise it, so it can only make an evaluation fail
	 * closed more often.
	 *
	 * @return the restricted budget
	 * @throws IllegalArgumentException if a limit exceeds the approved one or is not positive
	 */
	public static ExactEvaluationBudget restrictedTo(long maximumOperations, int maximumDepth,
			int maximumBits) {
		if (maximumOperations <= 0 || maximumOperations > MAXIMUM_OPERATIONS
				|| maximumDepth < 0 || maximumDepth > MAXIMUM_DEPTH
				|| maximumBits <= 0 || maximumBits > MAXIMUM_BITS) {
			throw new IllegalArgumentException("A budget can only be restricted below " + VERSION);
		}
		return new ExactEvaluationBudget(maximumOperations, maximumDepth, maximumBits);
	}

	/** @return the maximum of counted exact operations */
	public long getMaximumOperations() {
		return maximumOperations;
	}

	/** @return the maximum tower depth */
	public int getMaximumDepth() {
		return maximumDepth;
	}

	/** @return the maximum bit length of every rational */
	public int getMaximumBits() {
		return maximumBits;
	}

	/** @return whether this is the approved budget itself */
	public boolean isApproved() {
		return maximumOperations == MAXIMUM_OPERATIONS && maximumDepth == MAXIMUM_DEPTH
				&& maximumBits == MAXIMUM_BITS;
	}

	@Override
	public String toString() {
		return VERSION + (isApproved() ? "" : " restricted") + "[operations=" + maximumOperations
				+ ", depth=" + maximumDepth + ", bits=" + maximumBits + "]";
	}
}
