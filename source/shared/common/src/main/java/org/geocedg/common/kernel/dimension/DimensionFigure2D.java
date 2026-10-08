/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.dimension;

/**
 * Pure model-space geometry of a native dimension (PRE-G9B-R6-plus-E2,
 * {@code geocedg/specs/dimensions/native-dimensions.md}). It reads no unit, view or
 * screen state and applies no tolerance: every degeneracy is an exact test.
 */
public final class DimensionFigure2D {

	/** The measured value (model units), or NaN when undefined. */
	public final double value;
	/** Dimension-line end points {x1, y1, x2, y2}, or null when undefined. */
	public final double[] line;
	/** Extension line at A {x1, y1, x2, y2}, or null when undefined. */
	public final double[] extensionA;
	/** Extension line at B {x1, y1, x2, y2}, or null when undefined. */
	public final double[] extensionB;
	/** Text frame {anchorX, anchorY, dirX, dirY} (unit direction), or null. */
	public final double[] frame;
	/** Unit normal n = rot90(d) of the measurement direction, or null. */
	public final double[] normal;

	private DimensionFigure2D(double value, double[] line, double[] extensionA,
			double[] extensionB, double[] frame, double[] normal) {
		this.value = value;
		this.line = line;
		this.extensionA = extensionA;
		this.extensionB = extensionB;
		this.frame = frame;
		this.normal = normal;
	}

	private static final DimensionFigure2D UNDEFINED = new DimensionFigure2D(Double.NaN,
			null, null, null, null, null);

	/**
	 * Aligned dimension: {@code L = |B - A|}, direction from the ordered pair A to B.
	 *
	 * @param ax A x
	 * @param ay A y
	 * @param bx B x
	 * @param by B y
	 * @param offset signed offset along the left normal of A to B
	 * @param overshoot extension beyond the dimension line
	 * @param gap gap between a measured point and its extension line
	 * @return the figure
	 */
	public static DimensionFigure2D aligned(double ax, double ay, double bx, double by,
			double offset, double overshoot, double gap) {
		if (!finite(ax, ay, bx, by)) {
			return UNDEFINED;
		}
		double vx = bx - ax;
		double vy = by - ay;
		double length = Math.hypot(vx, vy);
		if (length == 0) {
			return new DimensionFigure2D(0, null, null, null, null, null);
		}
		return figure(ax, ay, bx, by, vx / length, vy / length, length, offset,
				overshoot, gap);
	}

	/**
	 * Linear dimension: {@code L = |<B - A, unit(direction)>|}.
	 *
	 * @param ax A x
	 * @param ay A y
	 * @param bx B x
	 * @param by B y
	 * @param dx direction x (kernel orientation)
	 * @param dy direction y (kernel orientation)
	 * @param offset signed offset along the left normal of the direction
	 * @param overshoot extension beyond the dimension line
	 * @param gap gap between a measured point and its extension line
	 * @return the figure
	 */
	public static DimensionFigure2D linear(double ax, double ay, double bx, double by,
			double dx, double dy, double offset, double overshoot, double gap) {
		if (!finite(ax, ay, bx, by) || !finite(dx, dy, 0, 0)) {
			return UNDEFINED;
		}
		double norm = Math.hypot(dx, dy);
		if (norm == 0 || !Double.isFinite(norm)) {
			return UNDEFINED;
		}
		double ux = dx / norm;
		double uy = dy / norm;
		double length = Math.abs((bx - ax) * ux + (by - ay) * uy);
		return figure(ax, ay, bx, by, ux, uy, length, offset, overshoot, gap);
	}

	private static DimensionFigure2D figure(double ax, double ay, double bx, double by,
			double ux, double uy, double length, double offset, double overshoot,
			double gap) {
		if (!finite(offset, overshoot, gap, 0) || overshoot < 0 || gap < 0) {
			return new DimensionFigure2D(length, null, null, null, null, null);
		}
		double nx = -uy;
		double ny = ux;
		double tA = offset;
		double tB = offset - ((bx - ax) * nx + (by - ay) * ny);
		double a1x = ax + tA * nx;
		double a1y = ay + tA * ny;
		double b1x = bx + tB * nx;
		double b1y = by + tB * ny;
		double[] line = a1x == b1x && a1y == b1y ? null
				: new double[] {a1x, a1y, b1x, b1y};
		double[] frame = {(a1x + b1x) / 2, (a1y + b1y) / 2, ux, uy};
		return new DimensionFigure2D(length, line,
				extension(ax, ay, a1x, a1y, tA, nx, ny, overshoot, gap),
				extension(bx, by, b1x, b1y, tB, nx, ny, overshoot, gap), frame,
				new double[] {nx, ny});
	}

	private static double[] extension(double px, double py, double fx, double fy,
			double distance, double nx, double ny, double overshoot, double gap) {
		if (!(Math.abs(distance) > gap)) {
			return null;
		}
		double sign = Math.signum(distance);
		return new double[] {px + sign * gap * nx, py + sign * gap * ny,
				fx + sign * overshoot * nx, fy + sign * overshoot * ny};
	}

	/**
	 * Signed offset that places the dimension line through a model point; the
	 * Desktop drag maps the pointer to the offset with this function.
	 *
	 * @param ax A x
	 * @param ay A y
	 * @param nx unit normal x
	 * @param ny unit normal y
	 * @param px point x
	 * @param py point y
	 * @return the offset, or NaN when undefined
	 */
	public static double offsetThrough(double ax, double ay, double nx, double ny,
			double px, double py) {
		double offset = (px - ax) * nx + (py - ay) * ny;
		return Double.isFinite(offset) ? offset : Double.NaN;
	}

	private static boolean finite(double a, double b, double c, double d) {
		return Double.isFinite(a) && Double.isFinite(b) && Double.isFinite(c)
				&& Double.isFinite(d);
	}
}
