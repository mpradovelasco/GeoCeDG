/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import org.geocedg.common.math.bspline.BSplineDataCause;

/**
 * Typed causes of an undefined {@code BSpline} revision ({@code bspline-2d.md} section 7), in
 * their order of precedence. A revision publishes exactly one primary cause and no geometry.
 */
public enum BSplineInvalidity {
	/** An input object, or an element of an input list, is undefined or not of its type. */
	INPUT_UNDEFINED,
	/** A control-point coordinate or a knot is not finite. */
	NONFINITE_DATA,
	/** Degree not integral or outside {@code 1..7}. */
	DEGREE_INVALID,
	/** Fewer than {@code p + 1} control points. */
	TOO_FEW_CONTROL_POINTS,
	/** More than 128 control points. */
	TOO_MANY_CONTROL_POINTS,
	/** Explicit knot list length other than {@code n + p + 2}. */
	KNOT_COUNT_MISMATCH,
	/** Some knot smaller than its predecessor. */
	KNOTS_DECREASING,
	/** End multiplicities other than {@code p + 1}. */
	NOT_CLAMPED,
	/** {@code a = b}. */
	EMPTY_DOMAIN,
	/** An interior knot value with multiplicity above {@code p}. */
	INTERIOR_MULTIPLICITY_EXCEEDED;

	/**
	 * @param cause cause reported by the shared library for polynomial data
	 * @return the family cause
	 * @throws IllegalArgumentException for a weight cause, which the polynomial family never
	 *             produces
	 */
	public static BSplineInvalidity of(BSplineDataCause cause) {
		switch (cause) {
		case NONFINITE_DATA:
			return NONFINITE_DATA;
		case DEGREE_INVALID:
			return DEGREE_INVALID;
		case TOO_FEW_CONTROL_POINTS:
			return TOO_FEW_CONTROL_POINTS;
		case TOO_MANY_CONTROL_POINTS:
			return TOO_MANY_CONTROL_POINTS;
		case KNOT_COUNT_MISMATCH:
			return KNOT_COUNT_MISMATCH;
		case KNOTS_DECREASING:
			return KNOTS_DECREASING;
		case NOT_CLAMPED:
			return NOT_CLAMPED;
		case EMPTY_DOMAIN:
			return EMPTY_DOMAIN;
		case INTERIOR_MULTIPLICITY_EXCEEDED:
			return INTERIOR_MULTIPLICITY_EXCEEDED;
		default:
			throw new IllegalArgumentException("Weight cause in a polynomial family: " + cause);
		}
	}
}
