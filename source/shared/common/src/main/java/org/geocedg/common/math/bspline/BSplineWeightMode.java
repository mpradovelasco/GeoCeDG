/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

/** Representation of the weights of validated data. */
public enum BSplineWeightMode {
	/**
	 * All weights are 1 and are not materialized; evaluation follows the polynomial
	 * operation sequence exactly (no multiplication by 1, no division by {@code W}).
	 */
	IMPLICIT_UNIT,
	/** The weights are stored as given, even when they are all equal (or all 1). */
	EXPLICIT
}
