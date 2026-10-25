/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Objects;

import org.geocedg.common.kernel.locus.constructive.ExactQuadraticTower.Element;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * The number system of one evaluation of a constructive cone: exact arithmetic in a quadratic
 * tower over the rationals, where every sign is decided, or outward-rounded intervals, where a
 * sign is certified or undecided ({@code locus-v2-constructive-authority.md} sections 3 and 6;
 * E1 implementation design section 6). One evaluator is written once over this interface, so
 * both number systems use the same formulas and decide the same predicates.
 *
 * @param <T> the type of a real value
 */
public interface ConstructiveArithmetic<T> {

	/** Sign of a value that the number system cannot decide. */
	int UNDECIDED = Integer.MIN_VALUE;

	/**
	 * @param value a finite binary64 value, its own exact definition
	 * @return the value
	 */
	T ofBinary64(double value);

	/**
	 * @param value an exact rational
	 * @return the value
	 */
	T ofRational(ExactRational2D value);

	/** @return {@code a + b} */
	T add(T a, T b);

	/** @return {@code a - b} */
	T subtract(T a, T b);

	/** @return {@code a b} */
	T multiply(T a, T b);

	/**
	 * @param b a divisor whose sign is certified nonzero
	 * @return {@code a / b}
	 */
	T divide(T a, T b);

	/** @return {@code -a} */
	T negate(T a);

	/**
	 * @param a a value whose sign is certified nonnegative
	 * @return its nonnegative square root
	 */
	T sqrt(T a);

	/** @return {@code -1}, {@code 0} or {@code 1}, or {@link #UNDECIDED} */
	int sign(T a);

	/** @return whether every sign is decided exactly */
	boolean isExact();

	/**
	 * @param angle a binary64 angle in radians, its own exact definition
	 * @return a rigorous value of its cosine, or {@code null} when the number system has none
	 */
	T cos(double angle);

	/**
	 * @param angle a binary64 angle in radians, its own exact definition
	 * @return a rigorous value of its sine, or {@code null} when the number system has none
	 */
	T sin(double angle);

	/** @return an outward binary64 enclosure {@code [lower, upper]} of the value */
	double[] enclosure(T a);

	/**
	 * @param tower the tower of one exact evaluation, with its budget
	 * @return exact arithmetic in the tower; it has no trigonometric values
	 */
	static ConstructiveArithmetic<Element> exact(ExactQuadraticTower tower) {
		return new Exact(tower);
	}

	/** @return outward-rounded interval arithmetic */
	static ConstructiveArithmetic<OutwardInterval> intervals() {
		return Intervals.INSTANCE;
	}

	/** Exact arithmetic in one quadratic tower. */
	final class Exact implements ConstructiveArithmetic<Element> {
		private final ExactQuadraticTower tower;

		private Exact(ExactQuadraticTower tower) {
			this.tower = Objects.requireNonNull(tower);
		}

		/** @return the tower of the evaluation */
		public ExactQuadraticTower getTower() {
			return tower;
		}

		@Override
		public Element ofBinary64(double value) {
			return tower.ofBinary64(value);
		}

		@Override
		public Element ofRational(ExactRational2D value) {
			return tower.rational(value);
		}

		@Override
		public Element add(Element a, Element b) {
			return a.add(b);
		}

		@Override
		public Element subtract(Element a, Element b) {
			return a.subtract(b);
		}

		@Override
		public Element multiply(Element a, Element b) {
			return a.multiply(b);
		}

		@Override
		public Element divide(Element a, Element b) {
			return a.divide(b);
		}

		@Override
		public Element negate(Element a) {
			return a.negate();
		}

		@Override
		public Element sqrt(Element a) {
			return tower.sqrt(a);
		}

		@Override
		public int sign(Element a) {
			return a.signum();
		}

		@Override
		public boolean isExact() {
			return true;
		}

		@Override
		public Element cos(double angle) {
			return null;
		}

		@Override
		public Element sin(double angle) {
			return null;
		}

		@Override
		public double[] enclosure(Element a) {
			return a.outwardEnclosure();
		}
	}

	/**
	 * Outward-rounded intervals. The sine and cosine of a binary64 angle are those of
	 * {@code StrictMath}, within one ulp of the exact value, widened by two binary64 on each
	 * side.
	 */
	final class Intervals implements ConstructiveArithmetic<OutwardInterval> {
		private static final Intervals INSTANCE = new Intervals();

		private Intervals() {
		}

		@Override
		public OutwardInterval ofBinary64(double value) {
			return OutwardInterval.point(value);
		}

		@Override
		public OutwardInterval ofRational(ExactRational2D value) {
			return OutwardInterval.enclosing(value);
		}

		@Override
		public OutwardInterval add(OutwardInterval a, OutwardInterval b) {
			return a.add(b);
		}

		@Override
		public OutwardInterval subtract(OutwardInterval a, OutwardInterval b) {
			return a.subtract(b);
		}

		@Override
		public OutwardInterval multiply(OutwardInterval a, OutwardInterval b) {
			return a.multiply(b);
		}

		@Override
		public OutwardInterval divide(OutwardInterval a, OutwardInterval b) {
			return a.divide(b);
		}

		@Override
		public OutwardInterval negate(OutwardInterval a) {
			return a.negate();
		}

		@Override
		public OutwardInterval sqrt(OutwardInterval a) {
			return a.sqrt();
		}

		@Override
		public int sign(OutwardInterval a) {
			return a.certifiedSign();
		}

		@Override
		public boolean isExact() {
			return false;
		}

		@Override
		public OutwardInterval cos(double angle) {
			return widened(StrictMath.cos(angle));
		}

		@Override
		public OutwardInterval sin(double angle) {
			return widened(StrictMath.sin(angle));
		}

		@Override
		public double[] enclosure(OutwardInterval a) {
			return new double[] {a.getLower(), a.getUpper()};
		}

		private static OutwardInterval widened(double value) {
			if (!Double.isFinite(value)) {
				return OutwardInterval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			}
			return OutwardInterval.of(Math.nextDown(Math.nextDown(value)),
					Math.nextUp(Math.nextUp(value)));
		}
	}
}
