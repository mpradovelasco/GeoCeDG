/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

import java.util.ArrayDeque;
import java.util.Deque;

import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.BSplineEvaluation;
import org.geocedg.common.math.bspline.BSplinePoint;

/**
 * Render tessellation of a record (section 8): computed once, in model units, independent of
 * zoom, view and DPI; a view only transforms and draws it. It is derived data, never
 * authority, and its error is an estimate, never a certified bound.
 *
 * <h2>Algorithm (deterministic)</h2>
 * <p>Tolerance {@code tau = RENDER_REL_TOL * diag(bbox(control polygon))}. Each
 * non-degenerate span {@code [u_j, u_{j+1}]} is processed in increasing order, so the
 * tessellation splits at every distinct knot. An interval {@code [s, t]} is bisected at
 * {@code m = s + (t - s) / 2} (depth-first, left half first) until it has received
 * {@link DxfImportLimits#RENDER_MIN_DEPTH} levels and the points at {@code s + (t - s)/4},
 * {@code m} and {@code s + 3(t - s)/4} lie within {@code tau} of the chord, or until
 * {@link DxfImportLimits#RENDER_MAX_DEPTH}. Each span end is evaluated in its own span.
 * Evaluations are counted; when the work limit is reached every remaining interval is
 * emitted as its chord and the tessellation is marked incomplete. With a degenerate control
 * box ({@code diag = 0}) only the span ends are emitted.</p>
 */
public final class DxfTessellation {

	private final double tolerance;
	private final double[] parameters;
	private final double[] xs;
	private final double[] ys;
	private final boolean complete;
	private final int evaluations;

	private DxfTessellation(double tolerance, double[] parameters, double[] xs, double[] ys,
			boolean complete, int evaluations) {
		this.tolerance = tolerance;
		this.parameters = parameters;
		this.xs = xs;
		this.ys = ys;
		this.complete = complete;
		this.evaluations = evaluations;
	}

	/**
	 * Tessellates evaluation data (the transformed record data, or its polynomial equivalent
	 * when the weights are all equal).
	 *
	 * @param data curve data in model units
	 * @param maxEvaluations work limit
	 * @return the tessellation
	 */
	static DxfTessellation of(BSplineData data, int maxEvaluations) {
		double tau = DxfImportLimits.RENDER_REL_TOL * controlDiagonal(data);
		Builder builder = new Builder(data, tau, maxEvaluations);
		for (int s = 0; s < data.spanCount(); s++) {
			builder.span(data.spanKnotIndex(s));
		}
		return new DxfTessellation(tau, builder.parameters.toArray(), builder.xs.toArray(),
				builder.ys.toArray(), builder.complete, builder.evaluations);
	}

	static double controlDiagonal(BSplineData data) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < data.controlPointCount(); i++) {
			minX = Math.min(minX, data.x(i));
			maxX = Math.max(maxX, data.x(i));
			minY = Math.min(minY, data.y(i));
			maxY = Math.max(maxY, data.y(i));
		}
		return Math.hypot(maxX - minX, maxY - minY);
	}

	/**
	 * @return the chordal tolerance {@code tau} in model units
	 */
	public double tolerance() {
		return tolerance;
	}

	/**
	 * @return number of points
	 */
	public int size() {
		return xs.length;
	}

	/**
	 * @param i point index
	 * @return parameter of the point
	 */
	public double parameter(int i) {
		return parameters[i];
	}

	/**
	 * @param i point index
	 * @return x in model units
	 */
	public double x(int i) {
		return xs[i];
	}

	/**
	 * @param i point index
	 * @return y in model units
	 */
	public double y(int i) {
		return ys[i];
	}

	/**
	 * @return whether the tessellation finished within the work limit
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @return number of curve evaluations spent
	 */
	public int evaluations() {
		return evaluations;
	}

	/** Growable primitive array. */
	private static final class DoubleList {
		private double[] values = new double[64];
		private int size;

		void add(double value) {
			if (size == values.length) {
				double[] grown = new double[size * 2];
				System.arraycopy(values, 0, grown, 0, size);
				values = grown;
			}
			values[size++] = value;
		}

		double[] toArray() {
			double[] result = new double[size];
			System.arraycopy(values, 0, result, 0, size);
			return result;
		}
	}

	private static final class Builder {
		private final BSplineData data;
		private final double tau;
		private final int maxEvaluations;
		final DoubleList parameters = new DoubleList();
		final DoubleList xs = new DoubleList();
		final DoubleList ys = new DoubleList();
		boolean complete = true;
		int evaluations;

		Builder(BSplineData data, double tau, int maxEvaluations) {
			this.data = data;
			this.tau = tau;
			this.maxEvaluations = maxEvaluations;
		}

		private double[] evaluate(int span, double u) {
			evaluations++;
			BSplinePoint point = BSplineEvaluation.pointInSpan(data, span, u);
			return new double[] {u, point.x(), point.y()};
		}

		void span(int j) {
			double[] start = evaluate(j, data.knot(j));
			double[] end = evaluate(j, data.knot(j + 1));
			if (xs.size == 0) {
				emit(start);
			}
			if (tau > 0) {
				Deque<double[][]> stack = new ArrayDeque<>();
				// each entry: {start, end, {depth}}
				stack.push(new double[][] {start, end, {0}});
				while (!stack.isEmpty()) {
					double[][] interval = stack.pop();
					double[] left = interval[0];
					double[] right = interval[1];
					int depth = (int) interval[2][0];
					if (depth >= DxfImportLimits.RENDER_MAX_DEPTH) {
						emit(right);
						continue;
					}
					if (evaluations + 3 > maxEvaluations) {
						complete = false;
						emit(right);
						continue;
					}
					double s = left[0];
					double t = right[0];
					double[] quarter = evaluate(j, s + (t - s) / 4);
					double[] middle = evaluate(j, s + (t - s) / 2);
					double[] threeQuarter = evaluate(j, s + 3 * (t - s) / 4);
					boolean flat = depth >= DxfImportLimits.RENDER_MIN_DEPTH
							&& deviation(left, right, quarter) <= tau
							&& deviation(left, right, middle) <= tau
							&& deviation(left, right, threeQuarter) <= tau;
					if (flat) {
						emit(right);
					} else {
						stack.push(new double[][] {middle, right, {depth + 1}});
						stack.push(new double[][] {left, middle, {depth + 1}});
					}
				}
			} else {
				emit(end);
			}
		}

		private void emit(double[] point) {
			parameters.add(point[0]);
			xs.add(point[1]);
			ys.add(point[2]);
		}
	}

	/** Distance from a point to the chord segment of two points. */
	static double deviation(double[] a, double[] b, double[] p) {
		double dx = b[1] - a[1];
		double dy = b[2] - a[2];
		double length2 = dx * dx + dy * dy;
		double t = length2 == 0 ? 0 : ((p[1] - a[1]) * dx + (p[2] - a[2]) * dy) / length2;
		t = Math.max(0, Math.min(1, t));
		return Math.hypot(p[1] - (a[1] + t * dx), p[2] - (a[2] + t * dy));
	}
}
