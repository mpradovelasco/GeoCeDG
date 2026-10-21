/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayDeque;
import java.util.Deque;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * Rigorous facts of the source of an oriented length that the structural certificate of
 * the length needs ({@code locus-v2-metrics.md} section 26; oriented length design section
 * 5): an outward-rounded lower bound of a chord between two certified points, which
 * certifies that the jump of the length is positive, and an upper bound of the length of a
 * closed periodic source from enclosures of its speed ({@code L <= sum of width times
 * sup |F'|}), which certifies that it is rectifiable. Both come from interval enclosures of
 * the class-v3 program of the source; neither reads a metric value.
 */
public final class RouteSourceFacts2D {
	/** Version of the method. */
	public static final String VERSION = "route-source-facts/v1";
	/** Depth of the initial dyadic cover of the length bound. */
	static final int COVER_DEPTH = 6;
	/** Deepest refinement of a box of the cover. */
	static final int MAXIMUM_DEPTH = 24;
	/** Boxes evaluated for one length bound. */
	static final int COVER_BUDGET = 2_048;

	private RouteSourceFacts2D() {
		// Utility class.
	}

	/**
	 * Lower bound of a chord of the source from its origin: the traced point is enclosed at
	 * the origin and at the three quarter-period parameters after it, and the largest
	 * outward-rounded separation between the origin enclosure and another one is returned.
	 * Every parameter is a valid point of a closed periodic source.
	 *
	 * @param model box model of the class-v3 program of the source
	 * @param domain canonical driver domain of the source
	 * @param origin canonical address of the origin
	 * @return positive lower bound of a chord, or {@code NaN} when none is certified
	 */
	public static double chordLowerBound(ConstructionBoxModel2D model, LocusInterval2D domain,
			double origin) {
		boolean periodic = model.getProgram().getDriver() == Driver.CIRCLE;
		SplineOutwardInterval2D[] base = pointEnclosure(model, origin, periodic);
		if (base == null) {
			return Double.NaN;
		}
		double period = domain.getUpper() - domain.getLower();
		double best = Double.NaN;
		for (int quarter = 1; quarter <= 3; quarter++) {
			double other = origin + period * quarter / 4;
			if (other >= domain.getUpper()) {
				other -= period;
			}
			if (!domain.contains(other, 0)) {
				continue;
			}
			SplineOutwardInterval2D[] point = pointEnclosure(model, other, periodic);
			double gap = point == null ? 0 : separation(base, point);
			if (gap > 0 && !(gap <= best)) {
				best = gap;
			}
		}
		return best;
	}

	/**
	 * Upper bound of the length of a closed periodic source over one full period of its
	 * circle driver: a cover of the circle by boxes, refined where the enclosure of the
	 * speed is not finite, with {@code |F'| <= |x'| + |y'|} and outward rounding.
	 *
	 * @param model box model of the class-v3 program of the source
	 * @param domain canonical driver domain of the source, the half-open circle
	 * @return finite upper bound of the length, or {@code NaN} when the cover fails
	 */
	public static double lengthUpperBound(ConstructionBoxModel2D model,
			LocusInterval2D domain) {
		if (model.getProgram().getDriver() != Driver.CIRCLE || domain.getLower() != -Math.PI
				|| domain.getUpper() != Math.PI) {
			return Double.NaN;
		}
		Deque<double[]> pending = new ArrayDeque<>();
		int count = 1 << COVER_DEPTH;
		double period = domain.getUpper() - domain.getLower();
		for (int index = count - 1; index >= 0; index--) {
			double upper = index == count - 1 ? domain.getUpper()
					: domain.getLower() + period * (index + 1) / count;
			pending.push(new double[] {domain.getLower() + period * index / count, upper,
				COVER_DEPTH});
		}
		// The seam box closes the real circle over the binary64 gap at pi.
		pending.addLast(new double[] {Math.PI, Math.nextUp(Math.PI), COVER_DEPTH});
		SplineOutwardInterval2D total = SplineOutwardInterval2D.point(0);
		int spent = 0;
		while (!pending.isEmpty()) {
			double[] box = pending.pop();
			if (++spent > COVER_BUDGET) {
				return Double.NaN;
			}
			SplineOutwardInterval2D speed = speed(model, box[0], box[1]);
			if (speed != null) {
				SplineOutwardInterval2D width = SplineOutwardInterval2D.point(box[1])
						.subtract(SplineOutwardInterval2D.point(box[0]));
				total = total.add(width.multiply(speed));
				continue;
			}
			double middle = box[0] + (box[1] - box[0]) / 2;
			if (box[2] >= MAXIMUM_DEPTH || !(box[0] < middle && middle < box[1])) {
				return Double.NaN;
			}
			pending.push(new double[] {middle, box[1], box[2] + 1});
			pending.push(new double[] {box[0], middle, box[2] + 1});
		}
		return total.upper;
	}

	/** @return enclosure of {@code |x'| + |y'|} on a valid box, or {@code null} */
	private static SplineOutwardInterval2D speed(ConstructionBoxModel2D model, double lower,
			double upper) {
		ConstructionBoxOutcome2D outcome;
		try {
			outcome = model.evaluateCertifying(new SplineOutwardInterval2D(lower, upper),
					true);
		} catch (ArithmeticException exception) {
			return null;
		}
		if (outcome.kind != ConstructionBoxOutcome2D.Kind.VALID || outcome.derivative == null) {
			return null;
		}
		try {
			return SplineOutwardInterval2D.point(magnitude(outcome.derivative[0]))
					.add(SplineOutwardInterval2D.point(magnitude(outcome.derivative[1])));
		} catch (ArithmeticException exception) {
			return null;
		}
	}

	private static double magnitude(SplineOutwardInterval2D value) {
		return Math.max(Math.abs(value.lower), Math.abs(value.upper));
	}

	/** @return enclosure of the traced point at one parameter, or {@code null} */
	private static SplineOutwardInterval2D[] pointEnclosure(ConstructionBoxModel2D model,
			double parameter, boolean periodic) {
		try {
			ConstructionBoxOutcome2D outcome = model.evaluateCertifying(
					SplineOutwardInterval2D.point(parameter), periodic);
			return outcome.kind == ConstructionBoxOutcome2D.Kind.VALID ? outcome.value : null;
		} catch (ArithmeticException exception) {
			return null;
		}
	}

	/**
	 * @return lower bound of the distance between two boxes: the larger gap of their
	 *         coordinate enclosures, rounded down; zero when they overlap in both
	 */
	private static double separation(SplineOutwardInterval2D[] first,
			SplineOutwardInterval2D[] second) {
		double gap = 0;
		for (int axis = 0; axis < 2; axis++) {
			double apart = Math.max(second[axis].lower - first[axis].upper,
					first[axis].lower - second[axis].upper);
			if (apart > 0) {
				gap = Math.max(gap, Math.nextDown(apart));
			}
		}
		return gap;
	}
}
