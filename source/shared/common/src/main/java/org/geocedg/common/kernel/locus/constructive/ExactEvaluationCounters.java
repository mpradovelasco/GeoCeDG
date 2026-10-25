/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

/**
 * Immutable deterministic counters of one exact evaluation: they depend only on the evaluated
 * construction and its exact inputs, never on timing.
 */
public final class ExactEvaluationCounters {

	private final long additions;
	private final long multiplications;
	private final long inversions;
	private final long signDecisions;
	private final long squareRoots;
	private final long rationalSquareRoots;
	private final int depth;
	private final int maximumBits;

	ExactEvaluationCounters(long additions, long multiplications, long inversions,
			long signDecisions, long squareRoots, long rationalSquareRoots, int depth,
			int maximumBits) {
		this.additions = additions;
		this.multiplications = multiplications;
		this.inversions = inversions;
		this.signDecisions = signDecisions;
		this.squareRoots = squareRoots;
		this.rationalSquareRoots = rationalSquareRoots;
		this.depth = depth;
		this.maximumBits = maximumBits;
	}

	/**
	 * @return counted exact operations: additions, multiplications, inversions, sign decisions
	 *         and irrational square roots, counted at every level of the tower, as in the gate
	 *         G-M3 prototype whose counters justified the approved budget
	 */
	public long getOperations() {
		return additions + multiplications + inversions + signDecisions + squareRoots;
	}

	/** @return counted additions and subtractions */
	public long getAdditions() {
		return additions;
	}

	/** @return counted multiplications */
	public long getMultiplications() {
		return multiplications;
	}

	/** @return counted inversions */
	public long getInversions() {
		return inversions;
	}

	/** @return counted sign decisions */
	public long getSignDecisions() {
		return signDecisions;
	}

	/** @return square roots that opened a new level of the tower */
	public long getSquareRoots() {
		return squareRoots;
	}

	/** @return square roots that were exact rationals; they are not counted as operations */
	public long getRationalSquareRoots() {
		return rationalSquareRoots;
	}

	/** @return the tower depth reached */
	public int getDepth() {
		return depth;
	}

	/** @return the largest bit length of a numerator or denominator */
	public int getMaximumBits() {
		return maximumBits;
	}

	@Override
	public String toString() {
		return "operations=" + getOperations() + ", depth=" + depth + ", bits=" + maximumBits;
	}
}
