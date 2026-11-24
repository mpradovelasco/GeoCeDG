/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;

/**
 * Validated immutable 2D B-spline data: degree {@code p}, clamped knot vector
 * {@code U = (u_0..u_m)}, {@code m = n + p + 1}, control points {@code P_0..P_n} and weights
 * that are either implicit unit weights (not materialized) or explicit positive weights.
 *
 * <p>All arrays are copied on entry and on exit. Every structural fact (domain,
 * non-degenerate spans, interior multiplicities, continuity orders, exact closure, equality
 * of weights) is decided by exact binary64 comparison, never with a tolerance
 * ({@code bspline-2d} section 11.4). Knot multiplicities use binary64 {@code ==}, so
 * {@code -0.0} and {@code 0.0} are the same knot value; the canonical encoding keeps their bit
 * patterns distinct.</p>
 *
 * <p>Data are built only by {@link #validate} (which enforces {@code 1 <= p}, clamped ends of
 * multiplicity exactly {@code p + 1}, {@code a < b} and interior multiplicity {@code <= p}),
 * by {@link BSplineKnotInsertion} and by {@link #hodograph()}. A hodograph has degree
 * {@code p - 1} and may carry interior multiplicity {@code p}, one more than its degree, where
 * the derivative is discontinuous; its continuity order there is {@code -1}.</p>
 */
public final class BSplineData {

	final int degree;
	final double[] knots;
	final double[] xs;
	final double[] ys;
	/** Explicit weights, or {@code null} for implicit unit weights. */
	final @CheckForNull double[] ws;
	/** Knot indices {@code j} of the non-degenerate spans, increasing. */
	private final int[] spans;
	private final List<BSplineInteriorKnot> interiorKnots;

	private BSplineData(int degree, double[] knots, double[] xs, double[] ys,
			@CheckForNull double[] ws) {
		this.degree = degree;
		this.knots = knots;
		this.xs = xs;
		this.ys = ys;
		this.ws = ws;
		int n = xs.length - 1;
		int count = 0;
		for (int j = degree; j <= n; j++) {
			if (knots[j] < knots[j + 1]) {
				count++;
			}
		}
		int[] spanIndices = new int[count];
		int next = 0;
		for (int j = degree; j <= n; j++) {
			if (knots[j] < knots[j + 1]) {
				spanIndices[next++] = j;
			}
		}
		this.spans = spanIndices;
		List<BSplineInteriorKnot> interior = new ArrayList<>();
		int i = degree + 1;
		while (i <= n) {
			int k = i;
			while (k + 1 <= n && knots[k + 1] == knots[i]) {
				k++;
			}
			int multiplicity = k - i + 1;
			interior.add(new BSplineInteriorKnot(knots[i], multiplicity, degree - multiplicity));
			i = k + 1;
		}
		this.interiorKnots = Collections.unmodifiableList(interior);
	}

	/**
	 * Builds data from arrays already known to satisfy the invariants (copies are made by the
	 * caller or are fresh arrays).
	 */
	static BSplineData trusted(int degree, double[] knots, double[] xs, double[] ys,
			@CheckForNull double[] ws) {
		return new BSplineData(degree, knots, xs, ys, ws);
	}

	/**
	 * Validates and captures data. The arrays are copied first, so later changes by the
	 * caller have no effect. Preconditions are checked in the order of
	 * {@link BSplineDataCause}; the first failing one is the only cause reported. All
	 * comparisons are exact binary64 comparisons.
	 *
	 * @param degree degree {@code p}
	 * @param knots knot vector {@code u_0..u_m}
	 * @param xs control-point x coordinates {@code x_0..x_n}
	 * @param ys control-point y coordinates {@code y_0..y_n}
	 * @param weightsOrNull explicit weights {@code w_0..w_n}, or {@code null} for implicit
	 *            unit weights (an explicit absence; implicit weights are never materialized)
	 * @param limits the caller's admissible degree and size
	 * @return the validated data or exactly one typed cause
	 * @throws NullPointerException when {@code knots}, {@code xs}, {@code ys} or
	 *             {@code limits} is {@code null} (a programming error)
	 * @throws IllegalArgumentException when {@code xs} and {@code ys} differ in length (a
	 *             programming error of the caller, which builds both from the same points)
	 */
	public static BSplineValidation validate(int degree, double[] knots, double[] xs,
			double[] ys, @CheckForNull double[] weightsOrNull, BSplineLimits limits) {
		Objects.requireNonNull(limits, "limits");
		double[] u = Objects.requireNonNull(knots, "knots").clone();
		double[] x = Objects.requireNonNull(xs, "xs").clone();
		double[] y = Objects.requireNonNull(ys, "ys").clone();
		double[] w = weightsOrNull == null ? null : weightsOrNull.clone();
		if (x.length != y.length) {
			throw new IllegalArgumentException("xs and ys differ in length");
		}
		if (!allFinite(u) || !allFinite(x) || !allFinite(y) || w != null && !allFinite(w)) {
			return BSplineValidation.rejected(BSplineDataCause.NONFINITE_DATA);
		}
		if (degree < 1 || degree > limits.maxDegree()) {
			return BSplineValidation.rejected(BSplineDataCause.DEGREE_INVALID);
		}
		int pointCount = x.length;
		if (pointCount < degree + 1) {
			return BSplineValidation.rejected(BSplineDataCause.TOO_FEW_CONTROL_POINTS);
		}
		if (pointCount > limits.maxControlPoints()) {
			return BSplineValidation.rejected(BSplineDataCause.TOO_MANY_CONTROL_POINTS);
		}
		if (u.length != pointCount + degree + 1) {
			return BSplineValidation.rejected(BSplineDataCause.KNOT_COUNT_MISMATCH);
		}
		for (int i = 0; i + 1 < u.length; i++) {
			if (u[i + 1] < u[i]) {
				return BSplineValidation.rejected(BSplineDataCause.KNOTS_DECREASING);
			}
		}
		int n = pointCount - 1;
		int m = u.length - 1;
		for (int i = 1; i <= degree; i++) {
			if (u[i] != u[0] || u[m - i] != u[m]) {
				return BSplineValidation.rejected(BSplineDataCause.NOT_CLAMPED);
			}
		}
		if (u[degree] == u[n + 1]) {
			return BSplineValidation.rejected(BSplineDataCause.EMPTY_DOMAIN);
		}
		// Non-empty domain: the end values must have multiplicity exactly p + 1.
		if (u[degree + 1] == u[degree] || u[n] == u[n + 1]) {
			return BSplineValidation.rejected(BSplineDataCause.NOT_CLAMPED);
		}
		int run = 1;
		for (int i = degree + 2; i <= n; i++) {
			run = u[i] == u[i - 1] ? run + 1 : 1;
			if (run > degree) {
				return BSplineValidation.rejected(
						BSplineDataCause.INTERIOR_MULTIPLICITY_EXCEEDED);
			}
		}
		if (w != null) {
			if (w.length != pointCount) {
				return BSplineValidation.rejected(BSplineDataCause.WEIGHT_COUNT_MISMATCH);
			}
			for (double weight : w) {
				if (!(weight > 0.0)) {
					return BSplineValidation.rejected(BSplineDataCause.NONPOSITIVE_WEIGHT);
				}
			}
		}
		return BSplineValidation.valid(new BSplineData(degree, u, x, y, w));
	}

	private static boolean allFinite(double[] values) {
		for (double value : values) {
			if (!Double.isFinite(value)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return degree {@code p}
	 */
	public int degree() {
		return degree;
	}

	/**
	 * @return number of control points {@code n + 1}
	 */
	public int controlPointCount() {
		return xs.length;
	}

	/**
	 * @return number of knots {@code m + 1 = n + p + 2}
	 */
	public int knotCount() {
		return knots.length;
	}

	/**
	 * @param i knot index {@code 0..m}
	 * @return knot {@code u_i}
	 */
	public double knot(int i) {
		return knots[i];
	}

	/**
	 * @return a copy of the knot vector
	 */
	public double[] knots() {
		return knots.clone();
	}

	/**
	 * @param i control-point index {@code 0..n}
	 * @return {@code x_i}
	 */
	public double x(int i) {
		return xs[i];
	}

	/**
	 * @param i control-point index {@code 0..n}
	 * @return {@code y_i}
	 */
	public double y(int i) {
		return ys[i];
	}

	/**
	 * @return a copy of the control-point x coordinates
	 */
	public double[] xs() {
		return xs.clone();
	}

	/**
	 * @return a copy of the control-point y coordinates
	 */
	public double[] ys() {
		return ys.clone();
	}

	/**
	 * @return the weight representation
	 */
	public BSplineWeightMode weightMode() {
		return ws == null ? BSplineWeightMode.IMPLICIT_UNIT : BSplineWeightMode.EXPLICIT;
	}

	/**
	 * @param i control-point index {@code 0..n}
	 * @return the effective weight {@code w_i} (1 for implicit unit weights)
	 */
	public double weight(int i) {
		return ws == null ? 1.0 : ws[i];
	}

	/**
	 * @return a copy of the explicit weights, or {@code null} for implicit unit weights
	 */
	public @CheckForNull double[] explicitWeights() {
		return ws == null ? null : ws.clone();
	}

	/**
	 * @return whether all effective weights are exactly equal (always true for implicit unit
	 *         weights)
	 */
	public boolean allWeightsEqual() {
		if (ws == null) {
			return true;
		}
		for (double weight : ws) {
			if (weight != ws[0]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return whether all effective weights are exactly 1 (always true for implicit unit
	 *         weights)
	 */
	public boolean allWeightsUnit() {
		if (ws == null) {
			return true;
		}
		for (double weight : ws) {
			if (weight != 1.0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the same degree, knots and control points with implicit unit weights when all
	 * effective weights are exactly equal; a common weight cancels, so the represented curve
	 * is the same in exact arithmetic. The binary64 evaluations of the two data may differ.
	 *
	 * @return polynomial data of the same curve, or {@code null} when the weights are not
	 *         all equal
	 */
	public @CheckForNull BSplineData polynomialEquivalent() {
		if (ws == null) {
			return this;
		}
		if (!allWeightsEqual()) {
			return null;
		}
		return new BSplineData(degree, knots, xs, ys, null);
	}

	/**
	 * @return domain start {@code a = u_p}
	 */
	public double domainStart() {
		return knots[degree];
	}

	/**
	 * @return domain end {@code b = u_{n+1}}
	 */
	public double domainEnd() {
		return knots[xs.length];
	}

	/**
	 * @return number of non-degenerate spans
	 */
	public int spanCount() {
		return spans.length;
	}

	/**
	 * @param s ordinal of the non-degenerate span, {@code 0..spanCount() - 1}
	 * @return its knot index {@code j}, with {@code u_j < u_{j+1}}
	 */
	public int spanKnotIndex(int s) {
		return spans[s];
	}

	/**
	 * @return a copy of the knot indices of the non-degenerate spans, increasing
	 */
	public int[] spanKnotIndices() {
		return spans.clone();
	}

	/**
	 * @param j a knot index
	 * @return whether {@code j} is the knot index of a non-degenerate span
	 *         ({@code p <= j <= n} and {@code u_j < u_{j+1}})
	 */
	public boolean isSpanKnotIndex(int j) {
		return j >= degree && j < xs.length && knots[j] < knots[j + 1];
	}

	/**
	 * Finds the owning span of a parameter by the rule of {@code bspline-2d} section 3.3; see
	 * {@link BSplineSpanLookup}. The search is a binary search over the non-degenerate spans.
	 *
	 * @param u parameter
	 * @return the owning span or a typed status
	 */
	public BSplineSpanLookup findSpan(double u) {
		if (!Double.isFinite(u)) {
			return BSplineSpanLookup.failure(BSplineStatus.PARAMETER_NONFINITE);
		}
		if (u < domainStart() || u > domainEnd()) {
			return BSplineSpanLookup.failure(BSplineStatus.PARAMETER_OUTSIDE_DOMAIN);
		}
		int low = 0;
		int high = spans.length - 1;
		// Largest span ordinal whose start knot is <= u; u = b falls into the last span.
		while (low < high) {
			int mid = (low + high + 1) >>> 1;
			if (knots[spans[mid]] <= u) {
				low = mid;
			} else {
				high = mid - 1;
			}
		}
		return BSplineSpanLookup.of(spans[low]);
	}

	/**
	 * @return the distinct interior knot values with multiplicity and structural continuity
	 *         order, increasing; unmodifiable
	 */
	public List<BSplineInteriorKnot> interiorKnots() {
		return interiorKnots;
	}

	/**
	 * Multiplicity of a value in the knot vector, by binary64 {@code ==}.
	 *
	 * @param value knot value
	 * @return number of knots equal to {@code value}
	 */
	public int multiplicity(double value) {
		int count = 0;
		for (double knot : knots) {
			if (knot == value) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Exact closure ({@code bspline-2d} section 4.3): {@code P_0} and {@code P_n} have equal
	 * coordinates under binary64 {@code ==}. Point-object identity is the consumer's concern.
	 *
	 * @return whether the curve is exactly closed
	 */
	public boolean isExactlyClosed() {
		int n = xs.length - 1;
		return xs[0] == xs[n] && ys[0] == ys[n];
	}

	/**
	 * @return 0 (a {@code C^0} seam) for an exactly closed curve; -1 for an open curve, which
	 *         has no seam
	 */
	public int seamContinuityOrder() {
		return isExactlyClosed() ? 0 : -1;
	}

	/**
	 * Hodograph: the derivative curve as data of degree {@code p - 1} on the inner knots
	 * {@code u_1..u_{m-1}} with control points
	 * {@code Q_i = p * ((P_{i+1} - P_i) / (u_{i+p+1} - u_{i+1}))}, {@code i = 0..n-1}, computed
	 * per coordinate in exactly that operation order (a zero denominator, which only occurs
	 * for a coefficient whose basis function has empty support, gives 0 by the convention
	 * {@code 0/0 := 0}). Evaluating the hodograph at {@code u} is bit-identical to
	 * {@link BSplineEvaluation#firstDerivative}. Repeated application gives every derivative
	 * order up to {@code p}.
	 *
	 * @return the hodograph, or {@link BSplineStatus#EXPLICIT_WEIGHTS_UNSUPPORTED} for
	 *         explicit weights (a rational derivative is not a B-spline), or
	 *         {@link BSplineStatus#DEGREE_ZERO}
	 */
	public BSplineOutcome<BSplineData> hodograph() {
		if (ws != null) {
			return BSplineOutcome.failure(BSplineStatus.EXPLICIT_WEIGHTS_UNSUPPORTED);
		}
		if (degree == 0) {
			return BSplineOutcome.failure(BSplineStatus.DEGREE_ZERO);
		}
		int n = xs.length - 1;
		double[] qx = new double[n];
		double[] qy = new double[n];
		for (int i = 0; i < n; i++) {
			qx[i] = BSplineEvaluation.hodographCoefficient(degree, knots, xs, i);
			qy[i] = BSplineEvaluation.hodographCoefficient(degree, knots, ys, i);
		}
		double[] inner = new double[knots.length - 2];
		System.arraycopy(knots, 1, inner, 0, inner.length);
		return BSplineOutcome.ok(new BSplineData(degree - 1, inner, qx, qy, null));
	}
}
