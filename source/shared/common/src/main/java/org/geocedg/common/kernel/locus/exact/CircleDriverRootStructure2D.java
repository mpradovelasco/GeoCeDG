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
 * Exact classification of a first-level predicate {@code Q(cos t, sin t)} over the canonical
 * circle domain {@code t in [-pi, pi)}.
 *
 * <p>With {@code n = Q.totalDegree()} and {@code u = tan(t/2)}, the predicate has the sign
 * and root multiplicities of {@code p(u) = (1 + u^2)^n Q((1 - u^2)/(1 + u^2), 2u/(1 + u^2))}
 * ({@link TrigonometricPolynomial2D#toTangentHalfAngle()}) for {@code t} in
 * {@code (-pi, pi)}; the finite roots come from {@link RealRootStructure1D} over the whole
 * line. The seam {@code t = pi} ({@code u} infinite) is analysed separately and exactly: its
 * value is {@code Q(-1, 0)}, a root there has multiplicity {@code 2n - deg p}, the side
 * {@code t -> pi^-} has the sign of {@code p} at plus infinity and the side
 * {@code t -> -pi^+} the sign at minus infinity. A root at the seam is reported first, with
 * the canonical parameter {@code -pi} of the half-open domain.</p>
 *
 * <p>Roots are listed in increasing canonical parameter. The gap signs follow the rule of
 * {@link RealRootStructure1D} over the cut points {@code -pi}, the roots and {@code pi}:
 * {@code roots().size() + 1} entries, the first gap is empty (sign 0) when a root lies at
 * {@code -pi}, and otherwise the first and the last gap are the same arc across the seam.
 * A predicate whose tangent half-angle form is the zero polynomial vanishes on the whole
 * circle and is reported as {@link Status#IDENTICALLY_ZERO}, with no roots and the single
 * gap sign 0.</p>
 */
public final class CircleDriverRootStructure2D {

	private final Status status;
	private final TrigonometricPolynomial2D predicate;
	private final ExactPolynomial1D tangentHalfAnglePolynomial;
	private final ExactRational2D valueAtPi;
	private final int multiplicityAtPi;
	private final RealRootStructure1D finiteRootStructure;
	private final ExactRational2D uWidthBound;
	private final List<Root> roots;
	private final List<Integer> gapSigns;

	/** Overall outcome of the classification. */
	public enum Status {
		/** The predicate is not identically zero; roots and gaps are classified. */
		CLASSIFIED,
		/** The predicate vanishes on the whole circle. */
		IDENTICALLY_ZERO
	}

	private CircleDriverRootStructure2D(Status status, TrigonometricPolynomial2D predicate,
			ExactPolynomial1D tangentHalfAnglePolynomial, ExactRational2D valueAtPi,
			int multiplicityAtPi, RealRootStructure1D finiteRootStructure,
			ExactRational2D uWidthBound, List<Root> roots, List<Integer> gapSigns) {
		this.status = status;
		this.predicate = predicate;
		this.tangentHalfAnglePolynomial = tangentHalfAnglePolynomial;
		this.valueAtPi = valueAtPi;
		this.multiplicityAtPi = multiplicityAtPi;
		this.finiteRootStructure = finiteRootStructure;
		this.uWidthBound = uWidthBound;
		this.roots = Collections.unmodifiableList(roots);
		this.gapSigns = Collections.unmodifiableList(gapSigns);
	}

	/**
	 * Classifies the predicate over {@code [-pi, pi)}.
	 *
	 * @param q predicate in {@code c = cos t} and {@code s = sin t}
	 * @param uWidthBound positive maximum width of an isolating interval in {@code u}
	 * @param budget work limits
	 * @return the classification
	 * @throws IllegalArgumentException for a nonpositive width bound
	 * @throws ExactBudgetExhaustedException if a limit of {@code budget} would be exceeded
	 */
	public static CircleDriverRootStructure2D classify(TrigonometricPolynomial2D q,
			ExactRational2D uWidthBound, ExactBudget1D budget) {
		Objects.requireNonNull(q, "q");
		Objects.requireNonNull(uWidthBound, "uWidthBound");
		ExactBudget1D.Meter meter = budget.newMeter();
		if (uWidthBound.signum() <= 0) {
			throw new IllegalArgumentException("The width bound must be positive");
		}
		ExactPolynomial1D p = q.toTangentHalfAngle();
		ExactRational2D valueAtPi = q.evaluate(ExactRational2D.of(-1), ExactRational2D.ZERO);
		List<Root> roots = new ArrayList<>();
		List<Integer> gaps = new ArrayList<>();
		if (p.isZero()) {
			gaps.add(0);
			return new CircleDriverRootStructure2D(Status.IDENTICALLY_ZERO, q, p, valueAtPi, 0,
					null, uWidthBound, roots, gaps);
		}
		RealRootStructure1D finite = RealRootStructure1D.compute(p, null, null, uWidthBound,
				meter);
		int multiplicityAtPi = 2 * q.totalDegree() - p.degree();
		if (valueAtPi.isZero() != (multiplicityAtPi > 0)) {
			throw new IllegalStateException("Seam value contradicts the degree of p");
		}
		if (multiplicityAtPi > 0) {
			roots.add(Root.atInfinity(multiplicityAtPi, p.signAtPositiveInfinity(),
					p.signAtNegativeInfinity()));
			gaps.add(0);
		}
		for (RealRootStructure1D.Root root : finite.roots()) {
			roots.add(Root.finite(root));
		}
		gaps.addAll(finite.gapSigns());
		return new CircleDriverRootStructure2D(Status.CLASSIFIED, q, p, valueAtPi,
				multiplicityAtPi, finite, uWidthBound, roots, gaps);
	}

	/** @return the overall outcome */
	public Status status() {
		return status;
	}

	/** @return whether the predicate vanishes on the whole circle */
	public boolean isIdenticallyZero() {
		return status == Status.IDENTICALLY_ZERO;
	}

	/** @return the classified predicate */
	public TrigonometricPolynomial2D predicate() {
		return predicate;
	}

	/** @return the total degree {@code n} of the predicate */
	public int totalDegree() {
		return predicate.totalDegree();
	}

	/** @return the tangent half-angle polynomial {@code p(u)} */
	public ExactPolynomial1D tangentHalfAnglePolynomial() {
		return tangentHalfAnglePolynomial;
	}

	/** @return the exact value {@code Q(-1, 0)} at the seam {@code t = pi} */
	public ExactRational2D valueAtPi() {
		return valueAtPi;
	}

	/** @return the multiplicity of a root at the seam; 0 when the seam is not a root */
	public int multiplicityAtPi() {
		return multiplicityAtPi;
	}

	/**
	 * @return the root structure of {@code p} over the whole line, or {@code null} for an
	 *         identically zero predicate
	 */
	public RealRootStructure1D finiteRootStructure() {
		return finiteRootStructure;
	}

	/** @return the maximum width of an isolating interval in {@code u} */
	public ExactRational2D uWidthBound() {
		return uWidthBound;
	}

	/** @return the unmodifiable roots in increasing canonical parameter */
	public List<Root> roots() {
		return roots;
	}

	/** @return the unmodifiable gap signs over {@code [-pi, pi)} */
	public List<Integer> gapSigns() {
		return gapSigns;
	}

	/** @return the number of exact bisections performed */
	public int bisections() {
		return finiteRootStructure == null ? 0 : finiteRootStructure.bisections();
	}

	/** @return the largest number of bisections on the path of one root */
	public int maximumRootBisections() {
		return finiteRootStructure == null ? 0 : finiteRootStructure.maximumRootBisections();
	}

	/** @return the largest coefficient bit length built during the classification */
	public int maximumCoefficientBitLength() {
		return finiteRootStructure == null ? 0
				: finiteRootStructure.maximumCoefficientBitLength();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CircleDriverRootStructure2D)) {
			return false;
		}
		CircleDriverRootStructure2D that = (CircleDriverRootStructure2D) other;
		return status == that.status && predicate.equals(that.predicate)
				&& tangentHalfAnglePolynomial.equals(that.tangentHalfAnglePolynomial)
				&& valueAtPi.equals(that.valueAtPi) && multiplicityAtPi == that.multiplicityAtPi
				&& Objects.equals(finiteRootStructure, that.finiteRootStructure)
				&& uWidthBound.equals(that.uWidthBound) && roots.equals(that.roots)
				&& gapSigns.equals(that.gapSigns);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status, predicate, valueAtPi, multiplicityAtPi,
				finiteRootStructure, uWidthBound, roots, gapSigns);
	}

	@Override
	public String toString() {
		return "CircleDriverRootStructure2D[" + status + ", p=" + tangentHalfAnglePolynomial
				+ ", valueAtPi=" + valueAtPi + ", multiplicityAtPi=" + multiplicityAtPi
				+ ", roots=" + roots + ", gapSigns=" + gapSigns + ", bisections="
				+ bisections() + ", maximumCoefficientBitLength="
				+ maximumCoefficientBitLength() + "]";
	}

	/**
	 * One root of the predicate on the circle.
	 *
	 * <p>A finite root carries its rational isolating interval in {@code u}. The angle
	 * enclosure {@code [tLower(), tUpper()]} contains {@code t = 2 atan(u)} for every
	 * {@code u} of that interval: each bound is {@code 2 * StrictMath.atan} of the outward
	 * binary64 bound of the interval, widened outward by two binary64 steps
	 * ({@code StrictMath.atan} is within one ulp). The root at the seam has no {@code u}
	 * interval; its angle enclosure is {@code [Math.PI, Math.nextUp(Math.PI)]}, which
	 * contains pi, and its canonical parameter is {@code -pi}, enclosed by
	 * {@code [-Math.nextUp(Math.PI), -Math.PI]}.</p>
	 */
	public static final class Root {
		private final boolean atInfinity;
		private final ExactRational2D uLower;
		private final ExactRational2D uUpper;
		private final double tLower;
		private final double tUpper;
		private final int multiplicity;
		private final int signLeft;
		private final int signRight;
		private final int bisections;

		private Root(boolean atInfinity, ExactRational2D uLower, ExactRational2D uUpper,
				double tLower, double tUpper, int multiplicity, int signLeft, int signRight,
				int bisections) {
			this.atInfinity = atInfinity;
			this.uLower = uLower;
			this.uUpper = uUpper;
			this.tLower = tLower;
			this.tUpper = tUpper;
			this.multiplicity = multiplicity;
			this.signLeft = signLeft;
			this.signRight = signRight;
			this.bisections = bisections;
		}

		private static Root atInfinity(int multiplicity, int signBeforePi, int signAfterMinusPi) {
			return new Root(true, null, null, Math.PI, Math.nextUp(Math.PI), multiplicity,
					signBeforePi, signAfterMinusPi, 0);
		}

		private static Root finite(RealRootStructure1D.Root root) {
			double lower = 2 * StrictMath.atan(root.lower().outwardDoubleEnclosure()[0]);
			double upper = 2 * StrictMath.atan(root.upper().outwardDoubleEnclosure()[1]);
			return new Root(false, root.lower(), root.upper(),
					Math.nextDown(Math.nextDown(lower)), Math.nextUp(Math.nextUp(upper)),
					root.multiplicity(), root.signLeft(), root.signRight(), root.bisections());
		}

		/** @return whether the root lies at the seam {@code t = pi}, where {@code u} is infinite */
		public boolean isAtInfinity() {
			return atInfinity;
		}

		/** @return whether the canonical parameter of the root is {@code -pi} */
		public boolean hasCanonicalParameterMinusPi() {
			return atInfinity;
		}

		/** @return the lower end of the isolating interval in {@code u}; null at the seam */
		public ExactRational2D uLower() {
			return uLower;
		}

		/** @return the upper end of the isolating interval in {@code u}; null at the seam */
		public ExactRational2D uUpper() {
			return uUpper;
		}

		/** @return whether the root is an exactly known rational {@code u} */
		public boolean isExactInU() {
			return !atInfinity && uLower.equals(uUpper);
		}

		/** @return lower bound of the angle enclosure; {@code Math.PI} at the seam */
		public double tLower() {
			return tLower;
		}

		/** @return upper bound of the angle enclosure; {@code Math.nextUp(Math.PI)} at the seam */
		public double tUpper() {
			return tUpper;
		}

		/** @return lower bound of the canonical parameter in {@code [-pi, pi)} */
		public double canonicalTLower() {
			return atInfinity ? -Math.nextUp(Math.PI) : tLower;
		}

		/** @return upper bound of the canonical parameter in {@code [-pi, pi)} */
		public double canonicalTUpper() {
			return atInfinity ? -Math.PI : tUpper;
		}

		/** @return the multiplicity of the root */
		public int multiplicity() {
			return multiplicity;
		}

		/** @return the sign of the predicate just before the root in increasing {@code t} */
		public int signLeft() {
			return signLeft;
		}

		/** @return the sign of the predicate just after the root in increasing {@code t} */
		public int signRight() {
			return signRight;
		}

		/** @return the sign pattern of the root */
		public RealRootStructure1D.RootKind kind() {
			return RealRootStructure1D.classify(multiplicity, signLeft, signRight);
		}

		/** @return the number of exact bisections on the path of this root; 0 at the seam */
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
			return atInfinity == that.atInfinity && Objects.equals(uLower, that.uLower)
					&& Objects.equals(uUpper, that.uUpper)
					&& Double.doubleToLongBits(tLower) == Double.doubleToLongBits(that.tLower)
					&& Double.doubleToLongBits(tUpper) == Double.doubleToLongBits(that.tUpper)
					&& multiplicity == that.multiplicity && signLeft == that.signLeft
					&& signRight == that.signRight && bisections == that.bisections;
		}

		@Override
		public int hashCode() {
			return Objects.hash(atInfinity, uLower, uUpper, tLower, tUpper, multiplicity,
					signLeft, signRight, bisections);
		}

		@Override
		public String toString() {
			String location = atInfinity ? "seam t=pi (canonical -pi)"
					: "u in [" + uLower + ", " + uUpper + "]";
			return "Root[" + location + ", t in [" + tLower + ", " + tUpper
					+ "]; multiplicity=" + multiplicity + ", signs=" + signLeft + "/"
					+ signRight + ", kind=" + kind() + "]";
		}
	}
}
