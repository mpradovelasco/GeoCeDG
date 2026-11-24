/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

/** Typed outcome of an operation on validated data; never conveyed by an exception. */
public enum BSplineStatus {
	/** The operation produced its value. */
	OK,
	/** The parameter or inserted knot value is NaN or infinite. */
	PARAMETER_NONFINITE,
	/** The parameter is outside the closed domain {@code [a, b]}. */
	PARAMETER_OUTSIDE_DOMAIN,
	/** The given knot index is not the knot index of a non-degenerate span. */
	SPAN_NOT_NON_DEGENERATE,
	/** The knot value to insert is not strictly inside {@code (a, b)}. */
	KNOT_NOT_INTERIOR,
	/** The insertion would raise the multiplicity of the knot value above the degree. */
	MULTIPLICITY_LIMIT,
	/** The result would exceed {@link BSplineLimits#ABSOLUTE_MAX_CONTROL_POINTS}. */
	SIZE_LIMIT,
	/** The operation is defined for implicit unit weights only. */
	EXPLICIT_WEIGHTS_UNSUPPORTED,
	/** The operation needs degree at least 1 (the hodograph of degree-0 data). */
	DEGREE_ZERO
}
