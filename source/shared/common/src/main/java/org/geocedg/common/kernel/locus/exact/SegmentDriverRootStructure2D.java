/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.exact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Exact classification of a first-level predicate {@code P(t)}, a polynomial in the driver
 * parameter itself, over the closed segment domain {@code t in [0, 1]}.
 *
 * <p>The roots come from {@link RealRootStructure1D#isolateIn} over {@code [0, 1]}; roots
 * exactly at {@code 0} or {@code 1} are detected exactly and marked with their
 * {@link DomainEnd}. An end root has a single side in the domain: the outer side sign is 0
 * (empty gap) and {@link Root#innerSign()} is the sign on the domain side. Its
 * {@link Root#kind()} classifies the root of the polynomial on the real line, from the
 * multiplicity and the inner sign; whether the end belongs to a component of positive
 * length is decided by the inner sign. The gap signs follow the rule of
 * {@link RealRootStructure1D}: {@code roots().size() + 1} entries over the cut points
 * {@code 0}, the roots and {@code 1}, with 0 for an empty end gap. The zero polynomial is
 * reported as {@link Status#IDENTICALLY_ZERO}, with no roots and the single gap sign 0.</p>
 */
public final class SegmentDriverRootStructure2D {

	private final Status status;
	private final ExactPolynomial1D predicate;
	private final RealRootStructure1D rootStructure;
	private final ExactRational2D widthBound;
	private final List<Root> roots;
	private final List<Integer> gapSigns;

	/** Overall outcome of the classification. */
	public enum Status {
		/** The predicate is not identically zero; roots and gaps are classified. */
		CLASSIFIED,
		/** The predicate vanishes on the whole segment. */
		IDENTICALLY_ZERO
	}

	/** Position of a root with respect to the ends of the domain. */
	public enum DomainEnd {
		/** The root lies strictly inside {@code (0, 1)}. */
		NONE,
		/** The root is exactly {@code t = 0}. */
		LOWER,
		/** The root is exactly {@code t = 1}. */
		UPPER
	}

	private SegmentDriverRootStructure2D(Status status, ExactPolynomial1D predicate,
			RealRootStructure1D rootStructure, ExactRational2D widthBound, List<Root> roots,
			List<Integer> gapSigns) {
		this.status = status;
		this.predicate = predicate;
		this.rootStructure = rootStructure;
		this.widthBound = widthBound;
		this.roots = Collections.unmodifiableList(roots);
		this.gapSigns = Collections.unmodifiableList(gapSigns);
	}

	/**
	 * Classifies the predicate over {@code [0, 1]}.
	 *
	 * @param predicate polynomial in the driver parameter {@code t}
	 * @param widthBound positive maximum width of an isolating interval in {@code t}
	 * @param budget work limits
	 * @return the classification
	 * @throws IllegalArgumentException for a nonpositive width bound
	 * @throws ExactBudgetExhaustedException if a limit of {@code budget} would be exceeded
	 */
	public static SegmentDriverRootStructure2D classify(ExactPolynomial1D predicate,
			ExactRational2D widthBound, ExactBudget1D budget) {
		Objects.requireNonNull(predicate, "predicate");
		Objects.requireNonNull(widthBound, "widthBound");
		Objects.requireNonNull(budget, "budget");
		if (widthBound.signum() <= 0) {
			throw new IllegalArgumentException("The width bound must be positive");
		}
		List<Root> roots = new ArrayList<>();
		if (predicate.isZero()) {
			List<Integer> gaps = new ArrayList<>();
			gaps.add(0);
			return new SegmentDriverRootStructure2D(Status.IDENTICALLY_ZERO, predicate, null,
					widthBound, roots, gaps);
		}
		RealRootStructure1D structure = RealRootStructure1D.isolateIn(predicate,
				ExactRational2D.ZERO, ExactRational2D.ONE, widthBound, budget);
		for (RealRootStructure1D.Root root : structure.roots()) {
			DomainEnd end = DomainEnd.NONE;
			if (root.isExact() && root.lower().isZero()) {
				end = DomainEnd.LOWER;
			} else if (root.isExact() && root.lower().equals(ExactRational2D.ONE)) {
				end = DomainEnd.UPPER;
			}
			roots.add(new Root(root, end));
		}
		return new SegmentDriverRootStructure2D(Status.CLASSIFIED, predicate, structure,
				widthBound, roots, new ArrayList<>(structure.gapSigns()));
	}

	/** @return the overall outcome */
	public Status status() {
		return status;
	}

	/** @return whether the predicate vanishes on the whole segment */
	public boolean isIdenticallyZero() {
		return status == Status.IDENTICALLY_ZERO;
	}

	/** @return the classified predicate */
	public ExactPolynomial1D predicate() {
		return predicate;
	}

	/**
	 * @return the root structure over {@code [0, 1]}, or {@code null} for an identically
	 *         zero predicate
	 */
	public RealRootStructure1D rootStructure() {
		return rootStructure;
	}

	/** @return the maximum width of an isolating interval in {@code t} */
	public ExactRational2D widthBound() {
		return widthBound;
	}

	/** @return the unmodifiable roots in increasing order */
	public List<Root> roots() {
		return roots;
	}

	/** @return the unmodifiable gap signs over {@code [0, 1]} */
	public List<Integer> gapSigns() {
		return gapSigns;
	}

	/** @return the number of exact bisections performed */
	public int bisections() {
		return rootStructure == null ? 0 : rootStructure.bisections();
	}

	/** @return the largest number of bisections on the path of one root */
	public int maximumRootBisections() {
		return rootStructure == null ? 0 : rootStructure.maximumRootBisections();
	}

	/** @return the largest coefficient bit length built during the classification */
	public int maximumCoefficientBitLength() {
		return rootStructure == null ? 0 : rootStructure.maximumCoefficientBitLength();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SegmentDriverRootStructure2D)) {
			return false;
		}
		SegmentDriverRootStructure2D that = (SegmentDriverRootStructure2D) other;
		return status == that.status && predicate.equals(that.predicate)
				&& Objects.equals(rootStructure, that.rootStructure)
				&& widthBound.equals(that.widthBound) && roots.equals(that.roots)
				&& gapSigns.equals(that.gapSigns);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, predicate, rootStructure, widthBound, roots, gapSigns);
	}

	@Override
	public String toString() {
		return "SegmentDriverRootStructure2D[" + status + ", predicate=" + predicate
				+ ", roots=" + roots + ", gapSigns=" + gapSigns + ", bisections="
				+ bisections() + ", maximumCoefficientBitLength="
				+ maximumCoefficientBitLength() + "]";
	}

	/**
	 * One root of the predicate on the segment, with its exact rational isolating interval
	 * in {@code t} and the outward binary64 enclosure {@code [tLower(), tUpper()]} of that
	 * interval.
	 */
	public static final class Root {
		private final ExactRational2D lower;
		private final ExactRational2D upper;
		private final double tLower;
		private final double tUpper;
		private final int multiplicity;
		private final int signLeft;
		private final int signRight;
		private final DomainEnd end;
		private final int bisections;

		private Root(RealRootStructure1D.Root root, DomainEnd end) {
			this.lower = root.lower();
			this.upper = root.upper();
			this.tLower = root.lower().outwardDoubleEnclosure()[0];
			this.tUpper = root.upper().outwardDoubleEnclosure()[1];
			this.multiplicity = root.multiplicity();
			this.signLeft = root.signLeft();
			this.signRight = root.signRight();
			this.end = end;
			this.bisections = root.bisections();
		}

		/** @return the lower end of the isolating interval in {@code t} */
		public ExactRational2D lower() {
			return lower;
		}

		/** @return the upper end of the isolating interval in {@code t} */
		public ExactRational2D upper() {
			return upper;
		}

		/** @return whether the root is exactly known, {@code lower() == upper()} */
		public boolean isExact() {
			return lower.equals(upper);
		}

		/** @return lower bound of the binary64 enclosure of the root */
		public double tLower() {
			return tLower;
		}

		/** @return upper bound of the binary64 enclosure of the root */
		public double tUpper() {
			return tUpper;
		}

		/** @return the multiplicity of the root */
		public int multiplicity() {
			return multiplicity;
		}

		/** @return the sign of the predicate on the gap to the left; 0 outside the domain */
		public int signLeft() {
			return signLeft;
		}

		/** @return the sign of the predicate on the gap to the right; 0 outside the domain */
		public int signRight() {
			return signRight;
		}

		/** @return the position of the root with respect to the ends of the domain */
		public DomainEnd end() {
			return end;
		}

		/** @return whether the root is an end of the domain */
		public boolean isDomainEnd() {
			return end != DomainEnd.NONE;
		}

		/**
		 * @return the sign of the predicate on the domain side of an end root
		 * @throws IllegalStateException for a root strictly inside the domain
		 */
		public int innerSign() {
			if (end == DomainEnd.LOWER) {
				return signRight;
			}
			if (end == DomainEnd.UPPER) {
				return signLeft;
			}
			throw new IllegalStateException("Interior root: both sides are in the domain");
		}

		/** @return the sign pattern of the root of the polynomial */
		public RealRootStructure1D.RootKind kind() {
			return RealRootStructure1D.classify(multiplicity, signLeft, signRight);
		}

		/** @return the number of exact bisections on the path of this root */
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
					&& Double.doubleToLongBits(tLower) == Double.doubleToLongBits(that.tLower)
					&& Double.doubleToLongBits(tUpper) == Double.doubleToLongBits(that.tUpper)
					&& multiplicity == that.multiplicity && signLeft == that.signLeft
					&& signRight == that.signRight && end == that.end
					&& bisections == that.bisections;
		}

		@Override
		public int hashCode() {
			return Objects.hash(lower, upper, tLower, tUpper, multiplicity, signLeft,
					signRight, end, bisections);
		}

		@Override
		public String toString() {
			return "Root[t in [" + lower + ", " + upper + "], end=" + end + "; multiplicity="
					+ multiplicity + ", signs=" + signLeft + "/" + signRight + ", kind="
					+ kind() + "]";
		}
	}
}
