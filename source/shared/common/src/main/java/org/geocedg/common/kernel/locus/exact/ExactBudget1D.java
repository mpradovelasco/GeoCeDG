/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import java.math.BigInteger;

/**
 * Immutable work limits of the exact univariate algebra.
 *
 * <p>{@link #maximumCoefficientBits()} bounds the bit length of every coefficient of every
 * polynomial that a computation builds (input, derivatives, quotients, greatest common
 * divisors, square-free factors and Sturm sequences, in rational and in primitive integer
 * form). {@link #maximumBisectionsPerRoot()} bounds the number of exact bisections on the
 * path that isolates and refines one root. Exceeding either limit throws
 * {@link ExactBudgetExhaustedException}; nothing is decided by a computation that
 * throws.</p>
 */
public final class ExactBudget1D {

	/** Default maximum coefficient bit length. */
	public static final int DEFAULT_MAXIMUM_COEFFICIENT_BITS = 4096;
	/** Default maximum number of exact bisections for one root. */
	public static final int DEFAULT_MAXIMUM_BISECTIONS_PER_ROOT = 512;
	/** The default limits. */
	public static final ExactBudget1D DEFAULT = new ExactBudget1D(
			DEFAULT_MAXIMUM_COEFFICIENT_BITS, DEFAULT_MAXIMUM_BISECTIONS_PER_ROOT);

	private final int maximumCoefficientBits;
	private final int maximumBisectionsPerRoot;

	private ExactBudget1D(int maximumCoefficientBits, int maximumBisectionsPerRoot) {
		this.maximumCoefficientBits = maximumCoefficientBits;
		this.maximumBisectionsPerRoot = maximumBisectionsPerRoot;
	}

	/**
	 * @param maximumCoefficientBits positive maximum coefficient bit length
	 * @param maximumBisectionsPerRoot nonnegative maximum bisections for one root
	 * @return the limits
	 * @throws IllegalArgumentException for a value out of range
	 */
	public static ExactBudget1D of(int maximumCoefficientBits, int maximumBisectionsPerRoot) {
		if (maximumCoefficientBits < 1 || maximumBisectionsPerRoot < 0) {
			throw new IllegalArgumentException("Invalid exact budget");
		}
		return new ExactBudget1D(maximumCoefficientBits, maximumBisectionsPerRoot);
	}

	/** @return the maximum coefficient bit length */
	public int maximumCoefficientBits() {
		return maximumCoefficientBits;
	}

	/** @return the maximum number of exact bisections for one root */
	public int maximumBisectionsPerRoot() {
		return maximumBisectionsPerRoot;
	}

	/** @return a new mutable meter for one computation under these limits */
	Meter newMeter() {
		return new Meter(this);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ExactBudget1D)) {
			return false;
		}
		ExactBudget1D that = (ExactBudget1D) other;
		return maximumCoefficientBits == that.maximumCoefficientBits
				&& maximumBisectionsPerRoot == that.maximumBisectionsPerRoot;
	}

	@Override
	public int hashCode() {
		return 31 * maximumCoefficientBits + maximumBisectionsPerRoot;
	}

	@Override
	public String toString() {
		return "ExactBudget1D[maximumCoefficientBits=" + maximumCoefficientBits
				+ ", maximumBisectionsPerRoot=" + maximumBisectionsPerRoot + "]";
	}

	/** Mutable work counters of one computation; confined to the computing thread. */
	static final class Meter {
		private final ExactBudget1D budget;
		private int bisections;
		private int maximumRootBisections;
		private int maximumCoefficientBitLength;

		private Meter(ExactBudget1D budget) {
			this.budget = budget;
		}

		/** Records the coefficients of a polynomial built by the computation. */
		void observe(ExactPolynomial1D polynomial) {
			observeBits(polynomial.maxCoefficientBitLength());
		}

		/** Records the integers of a primitive integer polynomial. */
		void observe(BigInteger[] coefficients) {
			int bits = 0;
			for (BigInteger coefficient : coefficients) {
				bits = Math.max(bits, coefficient.abs().bitLength());
			}
			observeBits(bits);
		}

		private void observeBits(int bits) {
			if (bits > budget.maximumCoefficientBits) {
				throw new ExactBudgetExhaustedException(
						ExactBudgetExhaustedException.Reason.COEFFICIENT_BITS,
						budget.maximumCoefficientBits, bits);
			}
			maximumCoefficientBitLength = Math.max(maximumCoefficientBitLength, bits);
		}

		/**
		 * Records one exact bisection that brings the path of a root to the given depth.
		 * The limit is checked before the bisection is counted.
		 */
		void recordBisection(int rootDepth) {
			if (rootDepth > budget.maximumBisectionsPerRoot) {
				throw new ExactBudgetExhaustedException(
						ExactBudgetExhaustedException.Reason.BISECTIONS,
						budget.maximumBisectionsPerRoot, rootDepth);
			}
			bisections++;
			maximumRootBisections = Math.max(maximumRootBisections, rootDepth);
		}

		/** @return the number of exact bisections performed */
		int bisections() {
			return bisections;
		}

		/** @return the largest bisection depth reached by one root path */
		int maximumRootBisections() {
			return maximumRootBisections;
		}

		/** @return the largest coefficient bit length observed */
		int maximumCoefficientBitLength() {
			return maximumCoefficientBitLength;
		}
	}
}
