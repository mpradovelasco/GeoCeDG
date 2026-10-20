/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.locus.intersection.CertifiedCurveEnclosure2D;

/**
 * Certified enclosure of class 2 ({@code locus-v2-metrics.md} section 25.1): a rigorous
 * enclosure of the semantic length from outward value and first-derivative
 * enclosures of the semantic curve over a partition of parameter boxes.
 *
 * <p>Per box, the lower bound is the lower bound of the chord. The upper bound
 * is {@code chord_upper / cos_lower(alpha)} when the derivative enclosure lies in
 * a closed convex cone of half-angle {@code alpha < pi/2} around a fixed axis,
 * and {@code |box| * sup|F'|} otherwise; the smaller of the two is kept. The cone
 * condition is decided on the corners of the derivative rectangle: the set of
 * vectors within a half-angle below {@code pi/2} of the axis is a convex cone, so
 * the rectangle lies in it exactly when its corners do. A box that the model
 * refuses is split; one that stays refused at the parameter resolution or depth
 * limit leaves the component unresolved, and no enclosure is reported.</p>
 */
final class CertifiedConeEnclosure2D {
	static final String METHOD = "certified cone enclosure chord/cos(alpha)";
	/** Keeps the summed box widths strictly inside the summed shares. */
	private static final double SHARE_MARGIN = 1 - 0x1p-40;

	private final CertifiedCurveEnclosure2D model;
	private final double componentLower;
	private final double componentWidth;
	private final double tolerance;
	private final int maximumDepth;
	private final double[] parameters;
	private final double[] lowers;
	private final double[] uppers;
	private final boolean resolved;
	private final boolean certified;
	private final String outcome;
	private final Map<Double, double[]> values;

	private CertifiedConeEnclosure2D(CertifiedCurveEnclosure2D model,
			double componentLower, double componentWidth, double tolerance,
			int maximumDepth, List<Box> boxes, boolean resolved, boolean certified,
			String outcome, Map<Double, double[]> values) {
		this.model = model;
		this.componentLower = componentLower;
		this.componentWidth = componentWidth;
		this.tolerance = tolerance;
		this.maximumDepth = maximumDepth;
		this.resolved = resolved;
		this.certified = certified;
		this.outcome = outcome;
		this.values = values;
		parameters = new double[boxes.size() + 1];
		lowers = new double[boxes.size()];
		uppers = new double[boxes.size()];
		parameters[0] = boxes.isEmpty() ? componentLower : boxes.get(0).lower;
		for (int index = 0; index < boxes.size(); index++) {
			Box box = boxes.get(index);
			parameters[index + 1] = box.upper;
			lowers[index] = resolved ? box.low : 0;
			uppers[index] = resolved ? box.up : 0;
		}
	}

	/**
	 * Builds the box partition of {@code [lower, upper]} until every box lies
	 * within its share of the tolerance, the box budget would be exceeded, or
	 * the predicted box count exceeds it.
	 *
	 * @param boundaries strictly increasing initial boundaries, {@code lower}
	 *        and {@code upper} included
	 */
	static CertifiedConeEnclosure2D build(CertifiedCurveEnclosure2D model,
			double[] boundaries, double tolerance, int maximumBoxes,
			int maximumDepth, int initialDepth) {
		double lower = boundaries[0];
		double upper = boundaries[boundaries.length - 1];
		double width = upper - lower;
		Map<Double, double[]> values = new HashMap<>();
		ArrayList<Box> boxes = new ArrayList<>();
		ArrayList<Double> cuts = new ArrayList<>();
		for (double knot : model.getKnots()) {
			if (knot > lower && knot < upper) {
				cuts.add(knot);
			}
		}
		double[] merged = merge(boundaries, cuts);
		for (int index = 0; index + 1 < merged.length; index++) {
			boxes.add(new Box(merged[index], merged[index + 1], initialDepth));
		}
		CertifiedConeEnclosure2D probe = new CertifiedConeEnclosure2D(model, lower,
				width, tolerance, maximumDepth, new ArrayList<>(), false, false,
				"", values);
		for (Box box : boxes) {
			probe.evaluate(box, values);
		}
		while (true) {
			long predicted = 0;
			int marked = 0;
			boolean refused = false;
			for (Box box : boxes) {
				if (box.refused) {
					refused = true;
					marked++;
					predicted += 2;
					continue;
				}
				double share = probe.share(box.lower, box.upper);
				double boxWidth = box.up - box.low;
				if (boxWidth > share) {
					// Width over share decreases with the square of the box width,
					// so k pieces with k*k >= width/share are predicted.
					marked++;
					predicted += share <= 0 ? Integer.MAX_VALUE
							: (long) Math.min(Integer.MAX_VALUE,
									Math.ceil(Math.sqrt(boxWidth / share)));
				} else {
					predicted++;
				}
			}
			if (marked == 0) {
				return new CertifiedConeEnclosure2D(model, lower, width, tolerance,
						maximumDepth, boxes, true, true,
						"certified with " + boxes.size() + " boxes", values);
			}
			if (boxes.size() + marked > maximumBoxes
					|| !refused && predicted > maximumBoxes) {
				return new CertifiedConeEnclosure2D(model, lower, width, tolerance,
						maximumDepth, boxes, !refused, false,
						(refused ? "unresolved" : "wider than the tolerance")
								+ " at " + boxes.size() + " boxes (predicted "
								+ predicted + ", budget " + maximumBoxes + ")",
						values);
			}
			ArrayList<Box> next = new ArrayList<>(boxes.size() + marked);
			for (Box box : boxes) {
				boolean split = box.refused || box.up - box.low
						> probe.share(box.lower, box.upper);
				if (!split) {
					next.add(box);
					continue;
				}
				double middle = box.lower + (box.upper - box.lower) / 2;
				if (!(middle > box.lower && middle < box.upper)
						|| box.depth >= maximumDepth) {
					if (box.refused) {
						return new CertifiedConeEnclosure2D(model, lower, width,
								tolerance, maximumDepth, boxes, false, false,
								"refused box at the parameter resolution", values);
					}
					return new CertifiedConeEnclosure2D(model, lower, width,
							tolerance, maximumDepth, boxes, true, false,
							"box at the parameter resolution is wider than its share",
							values);
				}
				Box left = new Box(box.lower, middle, box.depth + 1);
				Box right = new Box(middle, box.upper, box.depth + 1);
				probe.evaluate(left, values);
				probe.evaluate(right, values);
				next.add(left);
				next.add(right);
			}
			boxes = next;
		}
	}

	/** @return whether every box lies within its share of the tolerance */
	boolean isCertified() {
		return certified;
	}

	/** @return whether every box has a rigorous enclosure */
	boolean isResolved() {
		return resolved;
	}

	String getOutcome() {
		return outcome;
	}

	int getBoxCount() {
		return lowers.length;
	}

	double[] getParameters() {
		return parameters.clone();
	}

	double[] getLowers() {
		return lowers.clone();
	}

	double[] getUppers() {
		return uppers.clone();
	}

	/** @return outward sum of the box enclosures */
	MetricEnclosure2D total() {
		if (!resolved) {
			throw new IllegalStateException("Unresolved enclosure has no total");
		}
		double low = 0;
		double high = 0;
		for (int index = 0; index < lowers.length; index++) {
			low = index == 0 ? lowers[index]
					: MetricOutwardArithmetic2D.addDown(low, lowers[index]);
			high = index == 0 ? uppers[index]
					: MetricOutwardArithmetic2D.addUp(high, uppers[index]);
		}
		return new MetricEnclosure2D(MetricOutwardArithmetic2D.nonNegativeDown(low),
				high, METHOD);
	}

	/**
	 * Encloses the length of {@code [from, to]}: whole boxes contribute their
	 * enclosures and partial boxes are enclosed locally, split until within their
	 * share or until {@code localBudget} boxes are spent.
	 *
	 * @throws ArithmeticException when a partial box stays refused
	 */
	MetricEnclosure2D enclose(double from, double to, int localBudget) {
		if (!resolved) {
			throw new ArithmeticException("Unresolved component enclosure");
		}
		double low = Math.min(from, to);
		double high = Math.max(from, to);
		double sumLow = 0;
		double sumHigh = 0;
		int[] budget = {localBudget};
		// Route work never mutates the shared component state.
		Map<Double, double[]> scratch = new HashMap<>();
		for (int index = 0; index < lowers.length; index++) {
			double boxLower = parameters[index];
			double boxUpper = parameters[index + 1];
			if (boxUpper <= low || boxLower >= high) {
				continue;
			}
			double[] part;
			if (boxLower >= low && boxUpper <= high) {
				part = new double[] {lowers[index], uppers[index]};
			} else {
				part = refine(Math.max(boxLower, low), Math.min(boxUpper, high), 0,
						budget, scratch);
			}
			sumLow = MetricOutwardArithmetic2D.addDown(sumLow, part[0]);
			sumHigh = MetricOutwardArithmetic2D.addUp(sumHigh, part[1]);
		}
		return new MetricEnclosure2D(MetricOutwardArithmetic2D.nonNegativeDown(sumLow),
				Math.max(0, sumHigh), METHOD);
	}

	/** @return share of the tolerance owned by a parameter range */
	double share(double lower, double upper) {
		return tolerance * ((upper - lower) / componentWidth) * SHARE_MARGIN;
	}

	private double[] refine(double lower, double upper, int depth, int[] budget,
			Map<Double, double[]> scratch) {
		if (lower == upper) {
			return new double[] {0, 0};
		}
		Box box = new Box(lower, upper, depth);
		evaluate(box, scratch);
		if (!box.refused && box.up - box.low <= share(lower, upper)) {
			return new double[] {box.low, box.up};
		}
		double middle = lower + (upper - lower) / 2;
		if (budget[0] < 2 || depth >= maximumDepth
				|| !(middle > lower && middle < upper)) {
			if (box.refused) {
				throw new ArithmeticException("Partial box stays refused");
			}
			return new double[] {box.low, box.up};
		}
		budget[0] -= 2;
		double[] left = refine(lower, middle, depth + 1, budget, scratch);
		double[] right = refine(middle, upper, depth + 1, budget, scratch);
		return new double[] {MetricOutwardArithmetic2D.addDown(left[0], right[0]),
				MetricOutwardArithmetic2D.addUp(left[1], right[1])};
	}

	private void evaluate(Box box, Map<Double, double[]> scratch) {
		try {
			double[] bounds = boxBounds(box.lower, box.upper, scratch);
			box.low = bounds[0];
			box.up = bounds[1];
			box.refused = false;
		} catch (ArithmeticException | IllegalArgumentException exception) {
			box.refused = true;
		}
	}

	/** @return {@code {lower, upper}} bounds of the semantic length of the box */
	private double[] boxBounds(double lower, double upper,
			Map<Double, double[]> scratch) {
		if (!model.isSmooth(lower, upper)) {
			throw new ArithmeticException("Box is not smooth");
		}
		double[] start = value(lower, scratch);
		double[] end = value(upper, scratch);
		MetricOutwardInterval2D dx = new MetricOutwardInterval2D(
				MetricOutwardArithmetic2D.subDown(end[0], start[1]),
				MetricOutwardArithmetic2D.subUp(end[1], start[0]));
		MetricOutwardInterval2D dy = new MetricOutwardInterval2D(
				MetricOutwardArithmetic2D.subDown(end[2], start[3]),
				MetricOutwardArithmetic2D.subUp(end[3], start[2]));
		double chordLower = MetricOutwardArithmetic2D.hypotDown(dx.mignitude(),
				dy.mignitude());
		double chordUpper = MetricOutwardArithmetic2D.hypotUp(dx.magnitude(),
				dy.magnitude());
		double[] derivative = model.derivativeBounds(lower, upper);
		double speedUpper = MetricOutwardArithmetic2D.hypotUp(
				MetricOutwardArithmetic2D.magnitude(derivative[0], derivative[1]),
				MetricOutwardArithmetic2D.magnitude(derivative[2], derivative[3]));
		double bound = MetricOutwardArithmetic2D.mulUp(
				MetricOutwardArithmetic2D.subUp(upper, lower), speedUpper);
		double cosine = coneCosineLower(derivative);
		if (cosine > 0) {
			bound = Math.min(bound,
					MetricOutwardArithmetic2D.divUp(chordUpper, cosine));
		}
		if (chordLower > bound) {
			throw new ArithmeticException("Inconsistent box enclosure");
		}
		return new double[] {chordLower, bound};
	}

	private double[] value(double parameter, Map<Double, double[]> scratch) {
		Double key = parameter;
		double[] cached = values.get(key);
		if (cached == null) {
			cached = scratch.get(key);
		}
		if (cached == null) {
			cached = model.valueBounds(parameter);
			scratch.put(key, cached);
		}
		return cached;
	}

	/**
	 * @return lower bound of the cosine of the half-angle of a cone around the
	 *         rectangle's centre direction that contains the rectangle, or zero
	 *         when no such cone with half-angle below {@code pi/2} is established
	 */
	static double coneCosineLower(double[] rectangle) {
		double xLow = rectangle[0];
		double xHigh = rectangle[1];
		double yLow = rectangle[2];
		double yHigh = rectangle[3];
		if (xLow <= 0 && xHigh >= 0 && yLow <= 0 && yHigh >= 0) {
			return 0;
		}
		double axisX = xLow / 2 + xHigh / 2;
		double axisY = yLow / 2 + yHigh / 2;
		if (axisX == 0 && axisY == 0) {
			return 0;
		}
		double axisNorm = MetricOutwardArithmetic2D.hypotUp(Math.abs(axisX),
				Math.abs(axisY));
		double result = 1;
		double[][] corners = {{xLow, yLow}, {xLow, yHigh}, {xHigh, yLow},
				{xHigh, yHigh}};
		for (double[] corner : corners) {
			double dot = MetricOutwardArithmetic2D.addDown(
					MetricOutwardArithmetic2D.mulDown(corner[0], axisX),
					MetricOutwardArithmetic2D.mulDown(corner[1], axisY));
			if (!(dot > 0)) {
				return 0;
			}
			double norm = MetricOutwardArithmetic2D.hypotUp(Math.abs(corner[0]),
					Math.abs(corner[1]));
			double cosine = MetricOutwardArithmetic2D.divDown(dot,
					MetricOutwardArithmetic2D.mulUp(norm, axisNorm));
			result = Math.min(result, cosine);
		}
		return Math.max(0, Math.min(1, result));
	}

	private static double[] merge(double[] boundaries, List<Double> cuts) {
		ArrayList<Double> all = new ArrayList<>();
		for (double boundary : boundaries) {
			all.add(boundary);
		}
		all.addAll(cuts);
		all.sort(Double::compare);
		ArrayList<Double> distinct = new ArrayList<>();
		for (Double value : all) {
			if (distinct.isEmpty()
					|| value.doubleValue() > distinct.get(distinct.size() - 1)) {
				distinct.add(value);
			}
		}
		double[] result = new double[distinct.size()];
		for (int index = 0; index < result.length; index++) {
			result[index] = distinct.get(index);
		}
		return result;
	}

	private static final class Box {
		private final double lower;
		private final double upper;
		private final int depth;
		private double low;
		private double up;
		private boolean refused;

		private Box(double lower, double upper, int depth) {
			this.lower = lower;
			this.upper = upper;
			this.depth = depth;
		}
	}
}
