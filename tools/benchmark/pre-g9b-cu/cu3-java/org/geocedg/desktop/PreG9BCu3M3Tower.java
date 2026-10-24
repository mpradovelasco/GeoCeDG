/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * PRE-G9B-CU3 gate G-M3 prototype, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-m3-gate.ps1}: exact real arithmetic in a finite
 * tower of quadratic extensions over the rationals. Level 0 holds exact rationals; an element
 * of level {@code k} is {@code a + b sqrt(d_k)} with {@code a}, {@code b} and the radicand
 * {@code d_k >= 0} of level {@code k - 1}, and {@code sqrt} is the nonnegative real root.
 *
 * <p>Signs are decided exactly by recursion on the conjugate norm: when {@code a} and
 * {@code b} have opposite signs, {@code sign(a + b sqrt(d)) = sign(a)} if
 * {@code a^2 - b^2 d > 0}, {@code sign(b)} if it is negative, and zero if it vanishes. This is
 * correct whether or not {@code d} is a square in the lower field, so no irreducibility test is
 * needed. An inverse whose norm vanishes uses {@code a - b sqrt(d) = 0}, hence
 * {@code a + b sqrt(d) = 2a}. Decimal approximations serve reports only and never decide.
 *
 * <p>Every operation is counted, and the tower depth, the operation count and the bit length
 * of every rational are checked against the budgets of the run; exceeding one throws
 * {@link BudgetExhausted}. It is not part of any source set.
 */
final class PreG9BCu3M3Tower {

	/** A budget of the tower was exceeded; the decision is not made. */
	static final class BudgetExhausted extends RuntimeException {
		private static final long serialVersionUID = 1L;

		BudgetExhausted(String message) {
			super(message);
		}
	}

	/** An exact divisor is zero. */
	static final class ExactDivisionByZero extends ArithmeticException {
		private static final long serialVersionUID = 1L;

		ExactDivisionByZero(String message) {
			super(message);
		}
	}

	private static final MathContext REPORT = new MathContext(120);

	private final List<Num> radicands = new ArrayList<>();
	private final long operationBudget;
	private final int bitBudget;
	private final int depthBudget;
	long additions;
	long multiplications;
	long inversions;
	long signDecisions;
	long squareRoots;
	long rationalSquareRoots;
	int maxBits;

	PreG9BCu3M3Tower(long operationBudget, int bitBudget, int depthBudget) {
		this.operationBudget = operationBudget;
		this.bitBudget = bitBudget;
		this.depthBudget = depthBudget;
	}

	/** @return number of quadratic extensions of the tower */
	int depth() {
		return radicands.size();
	}

	/** @return total number of counted exact operations */
	long operations() {
		return additions + multiplications + inversions + signDecisions + squareRoots;
	}

	Num zero() {
		return rational(BigInteger.ZERO, BigInteger.ONE);
	}

	Num one() {
		return rational(BigInteger.ONE, BigInteger.ONE);
	}

	Num of(long value) {
		return rational(BigInteger.valueOf(value), BigInteger.ONE);
	}

	/** @return the exact rational value of a finite binary64 number */
	Num of(double value) {
		if (!Double.isFinite(value)) {
			throw new ArithmeticException("Nonfinite binary64 value");
		}
		if (value == 0) {
			return zero();
		}
		long bits = Double.doubleToLongBits(value);
		int exponent = (int) ((bits >> 52) & 0x7ffL);
		long mantissa = bits & 0xfffffffffffffL;
		if (exponent == 0) {
			exponent = 1;
		} else {
			mantissa |= 1L << 52;
		}
		exponent -= 1075;
		BigInteger numerator = BigInteger.valueOf(mantissa);
		if (bits < 0) {
			numerator = numerator.negate();
		}
		if (exponent >= 0) {
			return rational(numerator.shiftLeft(exponent), BigInteger.ONE);
		}
		return rational(numerator, BigInteger.ONE.shiftLeft(-exponent));
	}

	Num rational(BigInteger numerator, BigInteger denominator) {
		if (denominator.signum() == 0) {
			throw new ExactDivisionByZero("Zero denominator");
		}
		if (denominator.signum() < 0) {
			numerator = numerator.negate();
			denominator = denominator.negate();
		}
		BigInteger gcd = numerator.gcd(denominator);
		if (gcd.signum() != 0 && !gcd.equals(BigInteger.ONE)) {
			numerator = numerator.divide(gcd);
			denominator = denominator.divide(gcd);
		}
		int bits = Math.max(numerator.bitLength(), denominator.bitLength());
		if (bits > maxBits) {
			maxBits = bits;
			if (maxBits > bitBudget) {
				throw new BudgetExhausted("bit length " + maxBits + " > " + bitBudget);
			}
		}
		return new Num(numerator, denominator);
	}

	/** @return the nonnegative square root, exact rational when possible, else a new level */
	Num sqrt(Num value) {
		int sign = value.signum();
		if (sign < 0) {
			throw new ArithmeticException("Negative radicand");
		}
		if (sign == 0) {
			return zero();
		}
		if (value.level == 0) {
			BigInteger[] numeratorRoot = value.num.sqrtAndRemainder();
			BigInteger[] denominatorRoot = value.den.sqrtAndRemainder();
			if (numeratorRoot[1].signum() == 0 && denominatorRoot[1].signum() == 0) {
				rationalSquareRoots++;
				count();
				return rational(numeratorRoot[0], denominatorRoot[0]);
			}
		}
		squareRoots++;
		count();
		if (radicands.size() >= depthBudget) {
			throw new BudgetExhausted("tower depth " + (radicands.size() + 1) + " > "
					+ depthBudget);
		}
		radicands.add(value.lift(radicands.size()));
		int level = radicands.size();
		return new Num(level, zero().lift(level - 1), one().lift(level - 1));
	}

	private void count() {
		if (operations() > operationBudget) {
			throw new BudgetExhausted("operations " + operations() + " > " + operationBudget);
		}
	}

	/** One element of the tower. */
	final class Num {
		final int level;
		private final BigInteger num;
		private final BigInteger den;
		private final Num a;
		private final Num b;

		private Num(BigInteger num, BigInteger den) {
			this.level = 0;
			this.num = num;
			this.den = den;
			this.a = null;
			this.b = null;
		}

		private Num(int level, Num a, Num b) {
			this.level = level;
			this.num = null;
			this.den = null;
			this.a = a;
			this.b = b;
		}

		/** @return the same value represented at a higher level */
		Num lift(int target) {
			if (target < level) {
				throw new IllegalArgumentException("Cannot lower a tower element");
			}
			Num result = this;
			while (result.level < target) {
				result = new Num(result.level + 1, result, zero().lift(result.level));
			}
			return result;
		}

		Num add(Num other) {
			additions++;
			count();
			int top = Math.max(level, other.level);
			Num x = lift(top);
			Num y = other.lift(top);
			if (top == 0) {
				return rational(x.num.multiply(y.den).add(y.num.multiply(x.den)),
						x.den.multiply(y.den));
			}
			return new Num(top, x.a.add(y.a), x.b.add(y.b));
		}

		Num negate() {
			if (level == 0) {
				return rational(num.negate(), den);
			}
			return new Num(level, a.negate(), b.negate());
		}

		Num subtract(Num other) {
			return add(other.negate());
		}

		Num multiply(Num other) {
			multiplications++;
			count();
			int top = Math.max(level, other.level);
			Num x = lift(top);
			Num y = other.lift(top);
			if (top == 0) {
				return rational(x.num.multiply(y.num), x.den.multiply(y.den));
			}
			Num radicand = radicands.get(top - 1);
			Num real = x.a.multiply(y.a).add(x.b.multiply(y.b).multiply(radicand));
			Num irrational = x.a.multiply(y.b).add(x.b.multiply(y.a));
			return new Num(top, real, irrational);
		}

		Num square() {
			return multiply(this);
		}

		Num inverse() {
			inversions++;
			count();
			if (level == 0) {
				if (num.signum() == 0) {
					throw new ExactDivisionByZero("Inverse of zero");
				}
				return rational(den, num);
			}
			Num radicand = radicands.get(level - 1);
			Num norm = a.square().subtract(b.square().multiply(radicand));
			if (norm.signum() != 0) {
				Num inverseNorm = norm.inverse();
				return new Num(level, a.multiply(inverseNorm), b.negate().multiply(inverseNorm));
			}
			if (signum() == 0) {
				throw new ExactDivisionByZero("Inverse of zero");
			}
			// a - b sqrt(d) = 0, so this element equals 2a.
			return a.add(a).inverse().lift(level);
		}

		Num divide(Num other) {
			return multiply(other.inverse());
		}

		Num half() {
			return multiply(rational(BigInteger.ONE, BigInteger.TWO));
		}

		/** @return the exact sign, -1, 0 or 1 */
		int signum() {
			signDecisions++;
			count();
			if (level == 0) {
				return num.signum();
			}
			int sa = a.signum();
			int sb = b.signum();
			if (sb == 0) {
				return sa;
			}
			Num radicand = radicands.get(level - 1);
			if (radicand.signum() == 0) {
				return sa;
			}
			if (sa == 0 || sa == sb) {
				return sa == 0 ? sb : sa;
			}
			int sn = a.square().subtract(b.square().multiply(radicand)).signum();
			return sn == 0 ? 0 : sn > 0 ? sa : sb;
		}

		/** @return a decimal approximation for reports; it never decides anything */
		BigDecimal approximate() {
			if (level == 0) {
				return new BigDecimal(num).divide(new BigDecimal(den), REPORT);
			}
			BigDecimal root = radicands.get(level - 1).approximate().max(BigDecimal.ZERO)
					.sqrt(REPORT);
			return a.approximate().add(b.approximate().multiply(root, REPORT), REPORT);
		}

		@Override
		public String toString() {
			return approximate().round(new MathContext(20)).toString();
		}
	}
}
