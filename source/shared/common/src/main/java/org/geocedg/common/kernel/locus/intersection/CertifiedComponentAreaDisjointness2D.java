/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayDeque;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * PRE-G9B-R6-plus-C (author decision {@code DQ-C13}, {@code B1}): the one
 * read-only query that export uses to decide whether a certified Locus V2 or
 * Spline V2 component may be left out of an explicit export area.
 *
 * <p>It reads the existing ADR 0028 certified interval curve model of the
 * component's branch and proves disjointness from a closed axis-aligned
 * rectangle only when every parameter box of a deterministic bisection of the
 * component has an outward value enclosure that misses the rectangle. It adds
 * no semantics, no state and no dependency, never changes a certificate and
 * never decides identity. A branch without a certified model, a refused
 * enclosure, an indivisible box or an exhausted budget is reported as
 * {@link Status#NOT_PROVEN}: the caller keeps the component.
 */
public final class CertifiedComponentAreaDisjointness2D {

	/** Deterministic default number of parameter boxes inspected per query. */
	public static final int DEFAULT_MAXIMUM_BOXES = 4096;

	/** Result of one disjointness query. */
	public enum Status {
		/** Every outward enclosure misses the closed rectangle. */
		PROVEN_DISJOINT,
		/** No proof within the budget, or a refused enclosure. */
		NOT_PROVEN,
		/** The branch exposes no certified interval curve model. */
		NO_CERTIFIED_MODEL
	}

	private CertifiedComponentAreaDisjointness2D() {
		// query only
	}

	/**
	 * @param definition captured semantic definition
	 * @param branchKey branch of the component
	 * @param component certified continuous valid component interval
	 * @param xmin minimal rectangle x
	 * @param xmax maximal rectangle x
	 * @param ymin minimal rectangle y
	 * @param ymax maximal rectangle y
	 * @param maximumBoxes positive deterministic box budget
	 * @return whether the component is proven disjoint from the closed rectangle
	 */
	public static Status prove(LocusDefinition2D definition, String branchKey,
			LocusInterval2D component, double xmin, double xmax, double ymin,
			double ymax, int maximumBoxes) {
		if (definition == null || branchKey == null || component == null) {
			throw new IllegalArgumentException(
					"Definition, branch and component are required");
		}
		if (!Double.isFinite(xmin) || !Double.isFinite(xmax)
				|| !Double.isFinite(ymin) || !Double.isFinite(ymax)
				|| xmin > xmax || ymin > ymax) {
			throw new IllegalArgumentException("A finite closed rectangle is required");
		}
		if (maximumBoxes < 1) {
			throw new IllegalArgumentException("The box budget must be positive");
		}
		CertifiedIntervalCurveModel2D model;
		try {
			model = CertifiedIntervalCurveModel2D.capture(definition, branchKey);
		} catch (IllegalArgumentException | ArithmeticException exception) {
			return Status.NOT_PROVEN;
		}
		if (model == null) {
			return Status.NO_CERTIFIED_MODEL;
		}
		if (!Double.isFinite(component.getLower())
				|| !Double.isFinite(component.getUpper())
				|| !(component.getLower() < component.getUpper())) {
			return Status.NOT_PROVEN;
		}
		ArrayDeque<SplineOutwardInterval2D> queue = new ArrayDeque<>();
		double[] knots = model.getKnots();
		for (int span = 0; span + 1 < knots.length; span++) {
			double lower = Math.max(knots[span], component.getLower());
			double upper = Math.min(knots[span + 1], component.getUpper());
			if (lower < upper) {
				queue.addLast(new SplineOutwardInterval2D(lower, upper));
			}
		}
		if (queue.isEmpty()) {
			queue.addLast(new SplineOutwardInterval2D(component.getLower(),
					component.getUpper()));
		}
		int inspected = 0;
		while (!queue.isEmpty()) {
			if (inspected++ >= maximumBoxes) {
				return Status.NOT_PROVEN;
			}
			SplineOutwardInterval2D box = queue.removeFirst();
			SplineOutwardInterval2D[] value;
			try {
				value = model.evaluate(box, false);
			} catch (IllegalArgumentException | ArithmeticException exception) {
				return Status.NOT_PROVEN;
			}
			if (value == null || value.length < 2 || value[0] == null
					|| value[1] == null) {
				return Status.NOT_PROVEN;
			}
			if (value[0].upper < xmin || value[0].lower > xmax
					|| value[1].upper < ymin || value[1].lower > ymax) {
				continue;
			}
			double middle = box.midpoint();
			if (!(box.lower < middle && middle < box.upper)) {
				return Status.NOT_PROVEN;
			}
			queue.addLast(new SplineOutwardInterval2D(box.lower, middle));
			queue.addLast(new SplineOutwardInterval2D(middle, box.upper));
		}
		return Status.PROVEN_DISJOINT;
	}
}
