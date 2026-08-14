/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Revision-scoped finite root-localization interval. */
public final class IntersectionParameterInterval2D {
	private final double lower;
	private final double upper;

	/** Creates an ordered finite interval. */
	public IntersectionParameterInterval2D(double lower, double upper) {
		if (!Double.isFinite(lower) || !Double.isFinite(upper) || lower > upper) {
			throw new IllegalArgumentException("Invalid root-localization interval");
		}
		this.lower = lower == 0 ? 0 : lower;
		this.upper = upper == 0 ? 0 : upper;
	}

	public double getLower() {
		return lower;
	}

	public double getUpper() {
		return upper;
	}

	/** @return whether the parameter is inside this interval within tolerance */
	public boolean contains(double parameter, double tolerance) {
		return parameter >= lower - tolerance && parameter <= upper + tolerance;
	}

	/** @return whether the two localization intervals overlap within tolerance */
	public boolean overlaps(IntersectionParameterInterval2D other,
			double tolerance) {
		return lower <= other.upper + tolerance
				&& other.lower <= upper + tolerance;
	}

	@Override
	public String toString() {
		return "[" + lower + ", " + upper + "]";
	}
}
