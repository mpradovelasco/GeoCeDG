/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Exact real-root structure of a nonzero rational polynomial {@code p}, over the whole real
 * line or over a closed rational interval {@code [a, b]}.
 *
 * <p>The method ({@link #METHOD_VERSION}) works in exact rational and integer arithmetic:
 * square-free decomposition (Yun), a Sturm sequence of the square-free part, and exact
 * bisection from a power-of-two bound on the roots (whole line) or from {@code [a, b]}.
 * Every distinct real root gets a rational isolating interval {@code [lower, upper]} that
 * contains exactly one root of {@code p} and is not wider than the width bound; the
 * closed intervals are pairwise disjoint. A non-degenerate interval has ends that are not
 * roots, so the square-free part changes sign between them. A degenerate interval ({@code lower == upper}) is an exact rational
 * root found at an interval end or at a bisection point. Each root also gets its
 * multiplicity in {@code p} and the signs of {@code p} on the neighbouring gaps.</p>
 *
 * <p>The gap signs are listed in increasing order: entry {@code i} is the sign of {@code p}
 * on the open gap between cut points {@code i} and {@code i + 1}, where the cut points are
 * the start of the domain, the roots and the end of the domain. There are
 * {@code roots().size() + 1} entries. The sign 0 marks an empty gap, which occurs only when a
 * root lies exactly at an end of a closed interval.</p>
 *
 * <p>The result depends only on the inputs: two computations with equal inputs give equal
 * results, work counters included. A computation that would exceed its
 * {@link ExactBudget1D} throws {@link ExactBudgetExhaustedException} and decides
 * nothing.</p>
 */
public final class RealRootStructure1D {

	/** Identifier of the method. */
	public static final String METHOD_VERSION = "exact-univariate-sturm/v1";

	private final ExactPolynomial1D polynomial;
	private final ExactRational2D lowerBound;
	private final ExactRational2D upperBound;
	private final ExactRational2D widthBound;
	private final SquareFreeDecomposition1D decomposition;
	private final List<Root> roots;
	private final List<Integer> gapSigns;
	private final int bisections;
	private final int maximumRootBisections;
	private final int maximumCoefficientBitLength;

	/**
	 * Sign pattern of a root of a validity predicate that holds where {@code p >= 0}.
	 *
	 * <p>At a root with a single side (an end of a closed interval) the missing side is the
	 * sign that {@code p} takes beyond the end, which follows from the multiplicity and the
	 * other side.</p>
	 */
	public enum RootKind {
		/** Odd multiplicity: the sign changes across the root. */
		TRANSITION,
		/** Even multiplicity with a positive sign on both sides. */
		SINGULAR_INTERIOR,
		/** Even multiplicity with a negative sign on both sides. */
		ISOLATED_POINT
	}

	private RealRootStructure1D(ExactPolynomial1D polynomial, ExactRational2D lowerBound,
			ExactRational2D upperBound, ExactRational2D widthBound,
			SquareFreeDecomposition1D decomposition, List<Root> roots, List<Integer> gapSigns,
			ExactBudget1D.Meter meter) {
		this.polynomial = polynomial;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.widthBound = widthBound;
		this.decomposition = decomposition;
		this.roots = Collections.unmodifiableList(roots);
		this.gapSigns = Collections.unmodifiableList(gapSigns);
		this.bisections = meter.bisections();
		this.maximumRootBisections = meter.maximumRootBisections();
		this.maximumCoefficientBitLength = meter.maximumCoefficientBitLength();
	}

	/**
	 * Isolates every distinct real root of {@code p}.
	 *
	 * @param p nonzero polynomial
	 * @param widthBound positive maximum width of an isolating interval
	 * @param budget work limits
	 * @return the root structure over the whole real line
	 * @throws IllegalArgumentException for the zero polynomial or a nonpositive width bound
	 * @throws ExactBudgetExhaustedException if a limit of {@code budget} would be exceeded
	 */
	public static RealRootStructure1D isolate(ExactPolynomial1D p, ExactRational2D widthBound,
			ExactBudget1D budget) {
		return compute(p, null, null, widthBound, budget.newMeter());
	}

	/**
	 * Isolates every distinct root of {@code p} in the closed interval {@code [a, b]}; a root
	 * exactly at {@code a} or {@code b} is reported exactly, as a degenerate interval.
	 *
	 * @param p nonzero polynomial
	 * @param a lower end
	 * @param b upper end, greater than {@code a}
	 * @param widthBound positive maximum width of an isolating interval
	 * @param budget work limits
	 * @return the root structure over {@code [a, b]}
	 * @throws IllegalArgumentException for the zero polynomial, a nonpositive width bound or
	 *         {@code a >= b}
	 * @throws ExactBudgetExhaustedException if a limit of {@code budget} would be exceeded
	 */
	public static RealRootStructure1D isolateIn(ExactPolynomial1D p, ExactRational2D a,
			ExactRational2D b, ExactRational2D widthBound, ExactBudget1D budget) {
		Objects.requireNonNull(a, "a");
		Objects.requireNonNull(b, "b");
		if (a.compareTo(b) >= 0) {
			throw new IllegalArgumentException("The interval must satisfy a < b");
		}
		return compute(p, a, b, widthBound, budget.newMeter());
	}

	/** Metered computation; null ends mean the whole real line. */
	static RealRootStructure1D compute(ExactPolynomial1D p, ExactRational2D a,
			ExactRational2D b, ExactRational2D widthBound, ExactBudget1D.Meter meter) {
		Objects.requireNonNull(p, "p");
		Objects.requireNonNull(widthBound, "widthBound");
		if (p.isZero()) {
			throw new IllegalArgumentException("The zero polynomial has no root structure");
		}
		if (widthBound.signum() <= 0) {
			throw new IllegalArgumentException("The width bound must be positive");
		}
		meter.observe(p);
		SquareFreeDecomposition1D decomposition = SquareFreeDecomposition1D.of(p, meter);
		Isolation isolation = new Isolation(decomposition.squareFreePart(), widthBound, meter);
		List<Bracket> brackets = a == null ? isolation.wholeLine() : isolation.interval(a, b);
		List<ExactPolynomial1D> factors = decomposition.factors();
		BigInteger[][] factorIntegers = new BigInteger[factors.size()][];
		for (int i = 0; i < factorIntegers.length; i++) {
			factorIntegers[i] = factors.get(i).primitiveIntegerCoefficients();
			meter.observe(factorIntegers[i]);
		}
		BigInteger[] polynomialIntegers = p.primitiveIntegerCoefficients();
		meter.observe(polynomialIntegers);
		List<Integer> gaps = gapSigns(p, polynomialIntegers, brackets, a, b);
		List<Root> roots = new ArrayList<>(brackets.size());
		for (int i = 0; i < brackets.size(); i++) {
			Bracket bracket = brackets.get(i);
			int multiplicity = multiplicity(bracket, factorIntegers);
			Root root = new Root(bracket.lower, bracket.upper, multiplicity, gaps.get(i),
					gaps.get(i + 1), bracket.depth);
			// self-check: rejects a sign pattern that contradicts the multiplicity
			classify(multiplicity, root.signLeft, root.signRight);
			roots.add(root);
		}
		return new RealRootStructure1D(p, a, b, widthBound, decomposition, roots, gaps, meter);
	}

	/**
	 * Classifies a root from its multiplicity and side signs; a side sign 0 stands for an
	 * empty side and is replaced by the sign beyond the end.
	 *
	 * @throws IllegalStateException if the signs contradict the multiplicity
	 */
	static RootKind classify(int multiplicity, int signLeft, int signRight) {
		boolean odd = (multiplicity & 1) == 1;
		if (multiplicity < 1 || (signLeft == 0 && signRight == 0)) {
			throw new IllegalStateException("Root without multiplicity or sides");
		}
		int left = signLeft;
		int right = signRight;
		if (left == 0) {
			left = odd ? -right : right;
		} else if (right == 0) {
			right = odd ? -left : left;
		}
		if (odd == (left == right)) {
			throw new IllegalStateException("Side signs contradict the multiplicity");
		}
		if (odd) {
			return RootKind.TRANSITION;
		}
		return left > 0 ? RootKind.SINGULAR_INTERIOR : RootKind.ISOLATED_POINT;
	}

	private static int multiplicity(Bracket bracket, BigInteger[][] factors) {
		int multiplicity = 0;
		for (int i = 0; i < factors.length; i++) {
			if (factors[i].length < 2) {
				continue;
			}
			boolean contains = bracket.isExact()
					? sign(factors[i], bracket.lower) == 0
					: sign(factors[i], bracket.lower) != sign(factors[i], bracket.upper);
			if (contains) {
				if (multiplicity != 0) {
					throw new IllegalStateException("Root claimed by two square-free factors");
				}
				multiplicity = i + 1;
			}
		}
		if (multiplicity == 0) {
			throw new IllegalStateException("Root claimed by no square-free factor");
		}
		return multiplicity;
	}

	private static List<Integer> gapSigns(ExactPolynomial1D p, BigInteger[] integers,
			List<Bracket> brackets, ExactRational2D a, ExactRational2D b) {
		int count = brackets.size();
		List<Integer> gaps = new ArrayList<>(count + 1);
		if (count == 0) {
			gaps.add(a == null ? p.signAtNegativeInfinity() : nonzeroSign(integers, a));
			return gaps;
		}
		Bracket first = brackets.get(0);
		if (a == null) {
			gaps.add(p.signAtNegativeInfinity());
		} else if (first.isExact() && first.lower.equals(a)) {
			gaps.add(0);
		} else {
			gaps.add(nonzeroSign(integers, a));
		}
		for (int i = 0; i + 1 < count; i++) {
			gaps.add(nonzeroSign(integers, sampleBetween(brackets.get(i), brackets.get(i + 1))));
		}
		Bracket last = brackets.get(count - 1);
		if (b == null) {
			gaps.add(p.signAtPositiveInfinity());
		} else if (last.isExact() && last.upper.equals(b)) {
			gaps.add(0);
		} else {
			gaps.add(nonzeroSign(integers, b));
		}
		return gaps;
	}

	/** A point strictly between two consecutive roots. */
	private static ExactRational2D sampleBetween(Bracket left, Bracket right) {
		if (!left.isExact()) {
			return left.upper;
		}
		if (!right.isExact()) {
			return right.lower;
		}
		return ExactRational2D.midpoint(left.lower, right.lower);
	}

	private static int nonzeroSign(BigInteger[] integers, ExactRational2D x) {
		int sign = sign(integers, x);
		if (sign == 0) {
			throw new IllegalStateException("Gap sample is a root");
		}
		return sign;
	}

	private static int sign(BigInteger[] integers, ExactRational2D x) {
		return ExactPolynomial1D.signOfIntegerPolynomial(integers, x);
	}

	/** @return the polynomial */
	public ExactPolynomial1D polynomial() {
		return polynomial;
	}

	/** @return whether the domain is the whole real line */
	public boolean isWholeLine() {
		return lowerBound == null;
	}

	/** @return the lower end of the closed domain, or {@code null} for the whole line */
	public ExactRational2D lowerBound() {
		return lowerBound;
	}

	/** @return the upper end of the closed domain, or {@code null} for the whole line */
	public ExactRational2D upperBound() {
		return upperBound;
	}

	/** @return the maximum width of an isolating interval */
	public ExactRational2D widthBound() {
		return widthBound;
	}

	/** @return the square-free decomposition of the polynomial */
	public SquareFreeDecomposition1D squareFreeDecomposition() {
		return decomposition;
	}

	/** @return the unmodifiable distinct roots in the domain, in increasing order */
	public List<Root> roots() {
		return roots;
	}

	/** @return the unmodifiable gap signs, {@code roots().size() + 1} entries */
	public List<Integer> gapSigns() {
		return gapSigns;
	}

	/** @return the number of exact bisections performed */
	public int bisections() {
		return bisections;
	}

	/** @return the largest number of bisections on the path of one root */
	public int maximumRootBisections() {
		return maximumRootBisections;
	}

	/** @return the largest coefficient bit length built during the computation */
	public int maximumCoefficientBitLength() {
		return maximumCoefficientBitLength;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RealRootStructure1D)) {
			return false;
		}
		RealRootStructure1D that = (RealRootStructure1D) other;
		return polynomial.equals(that.polynomial)
				&& Objects.equals(lowerBound, that.lowerBound)
				&& Objects.equals(upperBound, that.upperBound)
				&& widthBound.equals(that.widthBound)
				&& decomposition.equals(that.decomposition)
				&& roots.equals(that.roots) && gapSigns.equals(that.gapSigns)
				&& bisections == that.bisections
				&& maximumRootBisections == that.maximumRootBisections
				&& maximumCoefficientBitLength == that.maximumCoefficientBitLength;
	}

	@Override
	public int hashCode() {
		return Objects.hash(polynomial, lowerBound, upperBound, widthBound, roots, gapSigns,
				bisections, maximumRootBisections, maximumCoefficientBitLength);
	}

	@Override
	public String toString() {
		String domain = lowerBound == null ? "(-inf, inf)"
				: "[" + lowerBound + ", " + upperBound + "]";
		return "RealRootStructure1D[" + METHOD_VERSION + ", domain=" + domain
				+ ", roots=" + roots + ", gapSigns=" + gapSigns + ", bisections=" + bisections
				+ ", maximumRootBisections=" + maximumRootBisections
				+ ", maximumCoefficientBitLength=" + maximumCoefficientBitLength + "]";
	}

	/** One distinct real root with its rational isolating interval. */
	public static final class Root {
		private final ExactRational2D lower;
		private final ExactRational2D upper;
		private final int multiplicity;
		private final int signLeft;
		private final int signRight;
		private final int bisections;

		Root(ExactRational2D lower, ExactRational2D upper, int multiplicity, int signLeft,
				int signRight, int bisections) {
			this.lower = lower;
			this.upper = upper;
			this.multiplicity = multiplicity;
			this.signLeft = signLeft;
			this.signRight = signRight;
			this.bisections = bisections;
		}

		/** @return the lower end of the isolating interval */
		public ExactRational2D lower() {
			return lower;
		}

		/** @return the upper end of the isolating interval */
		public ExactRational2D upper() {
			return upper;
		}

		/** @return whether the interval is degenerate, so that the root is exactly lower() */
		public boolean isExact() {
			return lower.equals(upper);
		}

		/** @return the multiplicity of the root in the polynomial */
		public int multiplicity() {
			return multiplicity;
		}

		/** @return the sign of the polynomial on the gap to the left; 0 for an empty gap */
		public int signLeft() {
			return signLeft;
		}

		/** @return the sign of the polynomial on the gap to the right; 0 for an empty gap */
		public int signRight() {
			return signRight;
		}

		/** @return the sign pattern of the root */
		public RootKind kind() {
			return classify(multiplicity, signLeft, signRight);
		}

		/** @return the number of exact bisections on the path that isolated this root */
		public int bisections() {
			return bisections;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof Root)) {
				return false;
			}
			Root that = (Root) other;
			return lower.equals(that.lower) && upper.equals(that.upper)
					&& multiplicity == that.multiplicity && signLeft == that.signLeft
					&& signRight == that.signRight && bisections == that.bisections;
		}

		@Override
		public int hashCode() {
			return Objects.hash(lower, upper, multiplicity, signLeft, signRight, bisections);
		}

		@Override
		public String toString() {
			return "Root[" + lower + ", " + upper + "; multiplicity=" + multiplicity
					+ ", signs=" + signLeft + "/" + signRight + ", bisections=" + bisections
					+ "]";
		}
	}

	/** Isolating interval found by the bisection, with the depth of its path. */
	private static final class Bracket {
		private final ExactRational2D lower;
		private final ExactRational2D upper;
		private final int depth;

		private Bracket(ExactRational2D lower, ExactRational2D upper, int depth) {
			this.lower = lower;
			this.upper = upper;
			this.depth = depth;
		}

		private boolean isExact() {
			return lower.equals(upper);
		}
	}

	/** Open interval with its Sturm variations and root count, or an exact root marker. */
	private static final class Task {
		private final ExactRational2D lower;
		private final int lowerVariations;
		private final ExactRational2D upper;
		private final int upperVariations;
		private final int count;
		private final int depth;
		private final boolean exactRoot;

		private Task(ExactRational2D lower, int lowerVariations, ExactRational2D upper,
				int upperVariations, int count, int depth, boolean exactRoot) {
			this.lower = lower;
			this.lowerVariations = lowerVariations;
			this.upper = upper;
			this.upperVariations = upperVariations;
			this.count = count;
			this.depth = depth;
			this.exactRoot = exactRoot;
		}

		private static Task interval(ExactRational2D lower, int lowerVariations,
				ExactRational2D upper, int upperVariations, int count, int depth) {
			return new Task(lower, lowerVariations, upper, upperVariations, count, depth, false);
		}

		private static Task root(ExactRational2D root, int depth) {
			return new Task(root, 0, root, 0, 1, depth, true);
		}
	}

	/** Mutable state of one isolation; confined to the computing thread. */
	private static final class Isolation {
		private final ExactPolynomial1D squareFree;
		private final SturmSequence1D sturm;
		private final BigInteger[] squareFreeIntegers;
		private final BigInteger[] derivativeIntegers;
		private final ExactRational2D widthBound;
		private final ExactBudget1D.Meter meter;
		private final List<Bracket> brackets = new ArrayList<>();

		private Isolation(ExactPolynomial1D squareFree, ExactRational2D widthBound,
				ExactBudget1D.Meter meter) {
			this.squareFree = squareFree;
			this.widthBound = widthBound;
			this.meter = meter;
			if (squareFree.degree() > 0) {
				sturm = SturmSequence1D.of(squareFree, meter);
				squareFreeIntegers = squareFree.primitiveIntegerCoefficients();
				derivativeIntegers = squareFree.derivative().primitiveIntegerCoefficients();
				meter.observe(squareFreeIntegers);
				meter.observe(derivativeIntegers);
			} else {
				sturm = null;
				squareFreeIntegers = new BigInteger[0];
				derivativeIntegers = new BigInteger[0];
			}
		}

		private List<Bracket> wholeLine() {
			if (sturm == null) {
				return brackets;
			}
			ExactRational2D upper = rootBound(squareFree);
			ExactRational2D lower = upper.negate();
			int lowerVariations = sturm.variationsAndSignAt(lower)[0];
			int upperVariations = sturm.variationsAndSignAt(upper)[0];
			isolate(Task.interval(lower, lowerVariations, upper, upperVariations,
					lowerVariations - upperVariations, 0));
			separateNeighbours();
			return brackets;
		}

		private List<Bracket> interval(ExactRational2D a, ExactRational2D b) {
			if (sturm == null) {
				return brackets;
			}
			int[] atLower = sturm.variationsAndSignAt(a);
			int[] atUpper = sturm.variationsAndSignAt(b);
			if (atLower[1] == 0) {
				brackets.add(new Bracket(a, a, 0));
			}
			int rootAtUpper = atUpper[1] == 0 ? 1 : 0;
			isolate(Task.interval(a, atLower[0], b, atUpper[0],
					atLower[0] - atUpper[0] - rootAtUpper, 0));
			if (rootAtUpper == 1) {
				brackets.add(new Bracket(b, b, 0));
			}
			separateNeighbours();
			return brackets;
		}

		/**
		 * Power of two strictly greater than the modulus of every root of the monic
		 * polynomial (Cauchy bound {@code 1 + max |s_i|}).
		 */
		private static ExactRational2D rootBound(ExactPolynomial1D monic) {
			ExactRational2D largest = ExactRational2D.ZERO;
			for (int i = 0; i < monic.degree(); i++) {
				ExactRational2D modulus = monic.coefficient(i).abs();
				if (modulus.compareTo(largest) > 0) {
					largest = modulus;
				}
			}
			ExactRational2D cauchy = largest.add(ExactRational2D.ONE);
			BigInteger ceiling = cauchy.numerator().add(cauchy.denominator())
					.subtract(BigInteger.ONE).divide(cauchy.denominator());
			int exponent = ceiling.subtract(BigInteger.ONE).bitLength();
			return ExactRational2D.of(BigInteger.ONE.shiftLeft(exponent), BigInteger.ONE);
		}

		/** Depth-first bisection, left part first, so brackets come out in increasing order. */
		private void isolate(Task initial) {
			Deque<Task> pending = new ArrayDeque<>();
			pending.push(initial);
			while (!pending.isEmpty()) {
				Task task = pending.pop();
				if (task.exactRoot) {
					brackets.add(new Bracket(task.lower, task.lower, task.depth));
				} else if (task.count == 1) {
					brackets.add(refine(task));
				} else if (task.count > 1) {
					split(task, pending);
				}
			}
		}

		private void split(Task task, Deque<Task> pending) {
			int depth = task.depth + 1;
			meter.recordBisection(depth);
			ExactRational2D middle = ExactRational2D.midpoint(task.lower, task.upper);
			int[] atMiddle = sturm.variationsAndSignAt(middle);
			int rootAtMiddle = atMiddle[1] == 0 ? 1 : 0;
			int leftCount = task.lowerVariations - atMiddle[0] - rootAtMiddle;
			int rightCount = task.count - leftCount - rootAtMiddle;
			if (leftCount < 0 || rightCount < 0) {
				throw new IllegalStateException("Inconsistent Sturm counts");
			}
			pending.push(Task.interval(middle, atMiddle[0], task.upper, task.upperVariations,
					rightCount, depth));
			if (rootAtMiddle == 1) {
				pending.push(Task.root(middle, depth));
			}
			pending.push(Task.interval(task.lower, task.lowerVariations, middle, atMiddle[0],
					leftCount, depth));
		}

		/**
		 * Narrows an open interval with exactly one root until it is not wider than the
		 * bound and both ends are not roots, or until a bisection point is the root.
		 */
		private Bracket refine(Task task) {
			ExactRational2D lower = task.lower;
			ExactRational2D upper = task.upper;
			int lowerSign = sign(squareFreeIntegers, lower);
			int upperSign = sign(squareFreeIntegers, upper);
			// one-sided signs just inside the ends; at a simple root the derivative decides
			int insideLower = lowerSign != 0 ? lowerSign : sign(derivativeIntegers, lower);
			int insideUpper = upperSign != 0 ? upperSign : -sign(derivativeIntegers, upper);
			if (insideLower == 0 || insideLower != -insideUpper) {
				throw new IllegalStateException("No sign change across an isolated root");
			}
			int depth = task.depth;
			while (lowerSign == 0 || upperSign == 0
					|| upper.subtract(lower).compareTo(widthBound) > 0) {
				depth++;
				meter.recordBisection(depth);
				ExactRational2D middle = ExactRational2D.midpoint(lower, upper);
				int middleSign = sign(squareFreeIntegers, middle);
				if (middleSign == 0) {
					return new Bracket(middle, middle, depth);
				}
				if (middleSign == insideLower) {
					lower = middle;
					lowerSign = middleSign;
				} else {
					upper = middle;
					upperSign = middleSign;
				}
			}
			return new Bracket(lower, upper, depth);
		}

		/**
		 * Makes consecutive brackets strictly disjoint. Two neighbours that come from one
		 * split share the split point, which is not a root; they are narrowed in turn until
		 * one of them leaves it.
		 */
		private void separateNeighbours() {
			for (int i = 0; i + 1 < brackets.size(); i++) {
				Bracket left = brackets.get(i);
				Bracket right = brackets.get(i + 1);
				if (left.isExact() && right.isExact()) {
					continue;
				}
				boolean narrowLeft = true;
				while (left.upper.compareTo(right.lower) >= 0) {
					if (right.isExact() || (narrowLeft && !left.isExact())) {
						left = narrow(left);
					} else {
						right = narrow(right);
					}
					narrowLeft = !narrowLeft;
				}
				brackets.set(i, left);
				brackets.set(i + 1, right);
			}
		}

		/** One exact bisection of a non-degenerate bracket whose ends are not roots. */
		private Bracket narrow(Bracket bracket) {
			int depth = bracket.depth + 1;
			meter.recordBisection(depth);
			ExactRational2D middle = ExactRational2D.midpoint(bracket.lower, bracket.upper);
			int middleSign = sign(squareFreeIntegers, middle);
			if (middleSign == 0) {
				return new Bracket(middle, middle, depth);
			}
			if (middleSign == sign(squareFreeIntegers, bracket.lower)) {
				return new Bracket(middle, bracket.upper, depth);
			}
			return new Bracket(bracket.lower, middle, depth);
		}
	}
}
