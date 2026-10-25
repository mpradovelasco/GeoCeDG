/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * Exact real arithmetic of one exact evaluation in a finite tower of quadratic extensions over
 * the rationals ({@code locus-v2-certified-construction-model.md} section 12.1). Level 0 holds
 * exact rationals; an element of level {@code k} is {@code a + b sqrt(d_k)} with {@code a},
 * {@code b} and the radicand {@code d_k > 0} of level {@code k - 1}, where {@code sqrt} is the
 * positive real root.
 *
 * <p>Signs are decided exactly by recursion on the conjugate norm: when {@code a} and {@code b}
 * have opposite signs, {@code sign(a + b sqrt(d))} is {@code sign(a)} if
 * {@code a^2 - b^2 d > 0}, {@code sign(b)} if it is negative, and zero if it vanishes. This holds
 * whether or not {@code d} is a square in the lower field, so no irreducibility test is needed;
 * the arithmetic is that of real numbers. An inverse whose norm vanishes uses
 * {@code a - b sqrt(d) = 0}, hence {@code a + b sqrt(d) = 2a}.
 *
 * <p>Every operation is counted against the budget {@link ExactEvaluationBudget} of the
 * evaluation, with the counting of the gate G-M3 prototype that justified it: additions,
 * multiplications, inversions, sign decisions and irrational square roots, at every level of the
 * recursion. The bit length of every rational and the depth of the tower are checked too.
 * Exceeding a limit throws {@link ExactEvaluationBudgetExhaustedException}; nothing is decided.
 */
public final class ExactQuadraticTower {

	private final ExactEvaluationBudget budget;
	private final List<Element> radicands = new ArrayList<>();
	private long additions;
	private long multiplications;
	private long inversions;
	private long signDecisions;
	private long squareRoots;
	private long rationalSquareRoots;
	private int maximumBits;

	/** @param budget versioned budget of this evaluation */
	public ExactQuadraticTower(ExactEvaluationBudget budget) {
		this.budget = Objects.requireNonNull(budget);
	}

	/** @return the budget of this evaluation */
	public ExactEvaluationBudget getBudget() {
		return budget;
	}

	/** @return the number of quadratic extensions opened */
	public int depth() {
		return radicands.size();
	}

	/** @return a snapshot of the deterministic counters */
	public ExactEvaluationCounters counters() {
		return new ExactEvaluationCounters(additions, multiplications, inversions, signDecisions,
				squareRoots, rationalSquareRoots, radicands.size(), maximumBits);
	}

	/** @return the exact zero */
	public Element zero() {
		return rational(ExactRational2D.ZERO);
	}

	/** @return the exact one */
	public Element one() {
		return rational(ExactRational2D.ONE);
	}

	/**
	 * @param value finite binary64 value, which is its own exact definition
	 * @return the exact rational value
	 */
	public Element ofBinary64(double value) {
		return rational(ExactRational2D.ofBinary64(value));
	}

	/**
	 * @param value integer
	 * @return the exact value
	 */
	public Element of(long value) {
		return rational(ExactRational2D.of(value));
	}

	/**
	 * @param value exact rational
	 * @return the tower element of level 0
	 */
	public Element rational(ExactRational2D value) {
		Objects.requireNonNull(value);
		int bits = value.bitLength();
		if (bits > maximumBits) {
			maximumBits = bits;
			if (maximumBits > budget.getMaximumBits()) {
				throw exhausted(ExactEvaluationBudgetExhaustedException.Limit.BITS);
			}
		}
		return new Element(this, value);
	}

	/**
	 * @param value nonnegative element
	 * @return its nonnegative square root: an exact rational when the value is the square of a
	 *         rational, otherwise the generator of a new level of the tower
	 * @throws ArithmeticException for a negative value
	 */
	public Element sqrt(Element value) {
		requireOwn(value);
		int sign = value.signum();
		if (sign < 0) {
			throw new ArithmeticException("Negative radicand");
		}
		if (sign == 0) {
			return zero();
		}
		if (value.level == 0) {
			BigInteger[] numeratorRoot = value.rational.numerator().sqrtAndRemainder();
			BigInteger[] denominatorRoot = value.rational.denominator().sqrtAndRemainder();
			if (numeratorRoot[1].signum() == 0 && denominatorRoot[1].signum() == 0) {
				rationalSquareRoots++;
				count();
				return rational(ExactRational2D.of(numeratorRoot[0], denominatorRoot[0]));
			}
		}
		squareRoots++;
		count();
		if (radicands.size() >= budget.getMaximumDepth()) {
			throw exhausted(ExactEvaluationBudgetExhaustedException.Limit.DEPTH);
		}
		radicands.add(value.lift(radicands.size()));
		int level = radicands.size();
		return new Element(this, level, zero().lift(level - 1), one().lift(level - 1));
	}

	private void count() {
		long operations = additions + multiplications + inversions + signDecisions + squareRoots;
		if (operations > budget.getMaximumOperations()) {
			throw exhausted(ExactEvaluationBudgetExhaustedException.Limit.OPERATIONS);
		}
	}

	private ExactEvaluationBudgetExhaustedException exhausted(
			ExactEvaluationBudgetExhaustedException.Limit limit) {
		return new ExactEvaluationBudgetExhaustedException(limit, counters(), budget);
	}

	private void requireOwn(Element element) {
		if (Objects.requireNonNull(element).tower != this) {
			throw new IllegalArgumentException("Element of another exact evaluation");
		}
	}

	/** One immutable element of the tower. */
	public static final class Element {
		private final ExactQuadraticTower tower;
		private final int level;
		private final ExactRational2D rational;
		private final Element a;
		private final Element b;

		private Element(ExactQuadraticTower tower, ExactRational2D rational) {
			this.tower = tower;
			this.level = 0;
			this.rational = rational;
			this.a = null;
			this.b = null;
		}

		private Element(ExactQuadraticTower tower, int level, Element a, Element b) {
			this.tower = tower;
			this.level = level;
			this.rational = null;
			this.a = a;
			this.b = b;
		}

		/** @return the level of the tower this element is represented at */
		public int getLevel() {
			return level;
		}

		/** @return whether the element is an exact rational of level 0 */
		public boolean isRational() {
			return level == 0;
		}

		/** @return the exact rational of a level-0 element */
		public ExactRational2D getRational() {
			if (level != 0) {
				throw new IllegalStateException("Not a rational element");
			}
			return rational;
		}

		private Element lift(int target) {
			if (target < level) {
				throw new IllegalArgumentException("Cannot lower a tower element");
			}
			Element result = this;
			while (result.level < target) {
				result = new Element(tower, result.level + 1, result,
						tower.zero().lift(result.level));
			}
			return result;
		}

		/**
		 * @param other addend of the same evaluation
		 * @return the exact sum
		 */
		public Element add(Element other) {
			tower.requireOwn(other);
			tower.additions++;
			tower.count();
			int top = Math.max(level, other.level);
			Element x = lift(top);
			Element y = other.lift(top);
			if (top == 0) {
				return tower.rational(x.rational.add(y.rational));
			}
			return new Element(tower, top, x.a.add(y.a), x.b.add(y.b));
		}

		/** @return the exact opposite (not counted, as in the gate prototype) */
		public Element negate() {
			if (level == 0) {
				return tower.rational(rational.negate());
			}
			return new Element(tower, level, a.negate(), b.negate());
		}

		/**
		 * @param other subtrahend of the same evaluation
		 * @return the exact difference
		 */
		public Element subtract(Element other) {
			return add(other.negate());
		}

		/**
		 * @param other factor of the same evaluation
		 * @return the exact product
		 */
		public Element multiply(Element other) {
			tower.requireOwn(other);
			tower.multiplications++;
			tower.count();
			int top = Math.max(level, other.level);
			Element x = lift(top);
			Element y = other.lift(top);
			if (top == 0) {
				return tower.rational(x.rational.multiply(y.rational));
			}
			Element radicand = tower.radicands.get(top - 1);
			Element real = x.a.multiply(y.a).add(x.b.multiply(y.b).multiply(radicand));
			Element irrational = x.a.multiply(y.b).add(x.b.multiply(y.a));
			return new Element(tower, top, real, irrational);
		}

		/** @return the exact square */
		public Element square() {
			return multiply(this);
		}

		/**
		 * @return the exact inverse
		 * @throws ArithmeticException for zero
		 */
		public Element inverse() {
			tower.inversions++;
			tower.count();
			if (level == 0) {
				if (rational.isZero()) {
					throw new ArithmeticException("Inverse of zero");
				}
				return tower.rational(ExactRational2D.ONE.divide(rational));
			}
			Element radicand = tower.radicands.get(level - 1);
			Element norm = a.square().subtract(b.square().multiply(radicand));
			if (norm.signum() != 0) {
				Element inverseNorm = norm.inverse();
				return new Element(tower, level, a.multiply(inverseNorm),
						b.negate().multiply(inverseNorm));
			}
			if (signum() == 0) {
				throw new ArithmeticException("Inverse of zero");
			}
			// a - b sqrt(d) = 0, so this element equals 2a.
			return a.add(a).inverse().lift(level);
		}

		/**
		 * @param other nonzero divisor of the same evaluation
		 * @return the exact quotient
		 */
		public Element divide(Element other) {
			return multiply(other.inverse());
		}

		/** @return the exact sign, -1, 0 or 1 */
		public int signum() {
			tower.signDecisions++;
			tower.count();
			if (level == 0) {
				return rational.signum();
			}
			int sa = a.signum();
			int sb = b.signum();
			if (sb == 0) {
				return sa;
			}
			Element radicand = tower.radicands.get(level - 1);
			if (radicand.signum() == 0) {
				return sa;
			}
			if (sa == 0 || sa == sb) {
				return sa == 0 ? sb : sa;
			}
			int sn = a.square().subtract(b.square().multiply(radicand)).signum();
			return sn == 0 ? 0 : sn > 0 ? sa : sb;
		}

		/**
		 * @param other element of the same evaluation
		 * @return the exact sign of {@code this - other}
		 */
		public int compareTo(Element other) {
			return subtract(other).signum();
		}

		/** @return whether the element is exactly zero */
		public boolean isZero() {
			return signum() == 0;
		}

		/**
		 * @return an outward binary64 enclosure {@code [lower, upper]} of the exact value, for
		 *         presentation and diagnostics only; it never decides anything
		 */
		public double[] outwardEnclosure() {
			if (level == 0) {
				return rational.outwardDoubleEnclosure();
			}
			double[] ea = a.outwardEnclosure();
			double[] eb = b.outwardEnclosure();
			double[] ed = tower.radicands.get(level - 1).outwardEnclosure();
			double rootLower = Math.nextDown(Math.sqrt(Math.max(0, ed[0])));
			double rootUpper = Math.nextUp(Math.sqrt(ed[1]));
			double rootLowerClamped = Math.max(0, rootLower);
			double p1 = eb[0] * rootLowerClamped;
			double p2 = eb[0] * rootUpper;
			double p3 = eb[1] * rootLowerClamped;
			double p4 = eb[1] * rootUpper;
			double productLower = Math.nextDown(Math.min(Math.min(p1, p2), Math.min(p3, p4)));
			double productUpper = Math.nextUp(Math.max(Math.max(p1, p2), Math.max(p3, p4)));
			return new double[] {Math.nextDown(ea[0] + productLower),
					Math.nextUp(ea[1] + productUpper)};
		}
	}
}
