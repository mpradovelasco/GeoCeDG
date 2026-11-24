/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

/**
 * Typed cause of rejected data. The declaration order is the order in which
 * {@link BSplineData#validate} checks the preconditions; the first failing one is the only
 * cause reported. The order follows {@code bspline-2d} section 7 (causes 2 to 10; cause 1,
 * {@code INPUT_UNDEFINED}, belongs to the kernel family), followed by the weight causes.
 * Consumers map these causes to their own typed causes.
 */
public enum BSplineDataCause {
	/** A knot, a control-point coordinate or an explicit weight is NaN or infinite. */
	NONFINITE_DATA,
	/** The degree is outside {@code 1..maxDegree} of the caller's limits. */
	DEGREE_INVALID,
	/** {@code n + 1 < p + 1}. */
	TOO_FEW_CONTROL_POINTS,
	/** {@code n + 1} exceeds the caller's maximum number of control points. */
	TOO_MANY_CONTROL_POINTS,
	/** The knot count differs from {@code n + p + 2}. */
	KNOT_COUNT_MISMATCH,
	/** Some {@code u_{i+1} < u_i}. */
	KNOTS_DECREASING,
	/**
	 * The ends are not clamped: {@code u_0..u_p} or {@code u_{n+1}..u_{n+p+1}} are not all
	 * exactly equal, or, for a non-empty domain, an end value has multiplicity above
	 * {@code p + 1} (end multiplicities must be exactly {@code p + 1}).
	 */
	NOT_CLAMPED,
	/** {@code a = u_p} equals {@code b = u_{n+1}}. */
	EMPTY_DOMAIN,
	/** An interior knot value has multiplicity above {@code p}. */
	INTERIOR_MULTIPLICITY_EXCEEDED,
	/** Explicit weights are given and their count differs from {@code n + 1}. */
	WEIGHT_COUNT_MISMATCH,
	/** An explicit weight is zero (either sign) or negative. */
	NONPOSITIVE_WEIGHT
}
