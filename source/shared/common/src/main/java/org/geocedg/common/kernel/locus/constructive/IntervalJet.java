/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * A second-order jet of outward intervals in the driver parameter of a locus slice: enclosures
 * of a value and of its first two derivatives on one parameter box (PRE-G9B-CU3 piece I-1 under
 * the constructive authority). Every operation applies the chain rule to enclosures, so the jet
 * of a composition encloses the value and the derivatives of the composed function at every
 * parameter of the box; a sign is that of the value enclosure.
 */
public final class IntervalJet {
	private static final OutwardInterval ZERO = OutwardInterval.point(0);
	private static final OutwardInterval TWO = OutwardInterval.point(2);

	private final OutwardInterval value;
	private final OutwardInterval first;
	private final OutwardInterval second;

	private IntervalJet(OutwardInterval value, OutwardInterval first, OutwardInterval second) {
		this.value = Objects.requireNonNull(value);
		this.first = Objects.requireNonNull(first);
		this.second = Objects.requireNonNull(second);
	}

	/**
	 * @param value enclosure of the value
	 * @param first enclosure of the first derivative
	 * @param second enclosure of the second derivative
	 * @return the jet
	 */
	public static IntervalJet of(OutwardInterval value, OutwardInterval first,
			OutwardInterval second) {
		return new IntervalJet(value, first, second);
	}

	/** @return the jet of a value that does not depend on the parameter */
	public static IntervalJet constant(OutwardInterval value) {
		return new IntervalJet(value, ZERO, ZERO);
	}

	/** @return enclosure of the value */
	public OutwardInterval getValue() {
		return value;
	}

	/** @return enclosure of the first derivative */
	public OutwardInterval getFirst() {
		return first;
	}

	/** @return enclosure of the second derivative */
	public OutwardInterval getSecond() {
		return second;
	}

	IntervalJet add(IntervalJet other) {
		return new IntervalJet(value.add(other.value), first.add(other.first),
				second.add(other.second));
	}

	IntervalJet subtract(IntervalJet other) {
		return new IntervalJet(value.subtract(other.value), first.subtract(other.first),
				second.subtract(other.second));
	}

	IntervalJet negate() {
		return new IntervalJet(value.negate(), first.negate(), second.negate());
	}

	/** {@code (uv)' = u'v + uv'} and {@code (uv)'' = u''v + 2u'v' + uv''}. */
	IntervalJet multiply(IntervalJet other) {
		return new IntervalJet(value.multiply(other.value),
				first.multiply(other.value).add(value.multiply(other.first)),
				second.multiply(other.value).add(TWO.multiply(first).multiply(other.first))
						.add(value.multiply(other.second)));
	}

	/** {@code w = u/v}: {@code w' = (u' - w v')/v} and {@code w'' = (u'' - 2w'v' - w v'')/v}. */
	IntervalJet divide(IntervalJet other) {
		OutwardInterval quotient = value.divide(other.value);
		OutwardInterval slope = first.subtract(quotient.multiply(other.first))
				.divide(other.value);
		OutwardInterval curvature = second.subtract(TWO.multiply(slope).multiply(other.first))
				.subtract(quotient.multiply(other.second)).divide(other.value);
		return new IntervalJet(quotient, slope, curvature);
	}

	/**
	 * {@code w = sqrt(u)} of a value whose enclosure is positive: {@code w' = u'/(2w)} and
	 * {@code w'' = (u'' - 2w'^2)/(2w)}.
	 */
	IntervalJet sqrt() {
		if (!(value.getLower() > 0)) {
			throw new ArithmeticException("The derivatives of a square root need a positive value");
		}
		OutwardInterval root = value.sqrt();
		OutwardInterval twice = TWO.multiply(root);
		OutwardInterval slope = first.divide(twice);
		OutwardInterval curvature = second.subtract(TWO.multiply(slope.square())).divide(twice);
		return new IntervalJet(root, slope, curvature);
	}

	@Override
	public String toString() {
		return "{" + value + ", " + first + ", " + second + "}";
	}

	/** The number system of interval jets for {@link ConstructiveConeEvaluator}. */
	static final class Arithmetic implements ConstructiveArithmetic<IntervalJet> {
		static final Arithmetic INSTANCE = new Arithmetic();

		private Arithmetic() {
		}

		@Override
		public IntervalJet ofBinary64(double value) {
			return constant(OutwardInterval.point(value));
		}

		@Override
		public IntervalJet ofRational(ExactRational2D value) {
			return constant(OutwardInterval.enclosing(value));
		}

		@Override
		public IntervalJet add(IntervalJet a, IntervalJet b) {
			return a.add(b);
		}

		@Override
		public IntervalJet subtract(IntervalJet a, IntervalJet b) {
			return a.subtract(b);
		}

		@Override
		public IntervalJet multiply(IntervalJet a, IntervalJet b) {
			return a.multiply(b);
		}

		@Override
		public IntervalJet divide(IntervalJet a, IntervalJet b) {
			return a.divide(b);
		}

		@Override
		public IntervalJet negate(IntervalJet a) {
			return a.negate();
		}

		@Override
		public IntervalJet sqrt(IntervalJet a) {
			return a.sqrt();
		}

		@Override
		public int sign(IntervalJet a) {
			return a.value.certifiedSign();
		}

		@Override
		public boolean isExact() {
			return false;
		}

		@Override
		public IntervalJet cos(double angle) {
			OutwardInterval cos = ConstructiveArithmetic.intervals().cos(angle);
			return cos == null ? null : constant(cos);
		}

		@Override
		public IntervalJet sin(double angle) {
			OutwardInterval sin = ConstructiveArithmetic.intervals().sin(angle);
			return sin == null ? null : constant(sin);
		}

		@Override
		public double[] enclosure(IntervalJet a) {
			return new double[] {a.value.getLower(), a.value.getUpper()};
		}
	}
}
