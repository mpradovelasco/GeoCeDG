/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

/**
 * Immutable structural fact about one distinct interior knot value {@code a < v < b}: its
 * multiplicity {@code k} (count of knots exactly equal to {@code v} under binary64
 * {@code ==}) and the structural parametric continuity order {@code p - k} of the represented
 * function there ({@code bspline-2d} section 5). The order is the minimum guaranteed one; a
 * negative order (only possible for derived hodograph data) means the represented function is
 * not continuous there. No regularity or geometric continuity is implied.
 */
public final class BSplineInteriorKnot {

	private final double value;
	private final int multiplicity;
	private final int continuityOrder;

	BSplineInteriorKnot(double value, int multiplicity, int continuityOrder) {
		this.value = value;
		this.multiplicity = multiplicity;
		this.continuityOrder = continuityOrder;
	}

	/**
	 * @return the knot value (the first stored representative of the equal values)
	 */
	public double value() {
		return value;
	}

	/**
	 * @return the multiplicity {@code k}
	 */
	public int multiplicity() {
		return multiplicity;
	}

	/**
	 * @return the structural continuity order {@code p - k}
	 */
	public int continuityOrder() {
		return continuityOrder;
	}
}
