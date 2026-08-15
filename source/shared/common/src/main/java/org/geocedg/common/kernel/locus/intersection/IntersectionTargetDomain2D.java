/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.OptionalDouble;

/** Immutable authoritative target-domain contract for one captured query. */
public final class IntersectionTargetDomain2D {
	/** Closed domain forms required by the G8B/G8C1 target adapters. */
	public enum Kind {
		ALL_MODEL_PLANE, EXPLICIT_FINITE_X_INTERVAL
	}

	private final Kind kind;
	private final OptionalDouble lowerX;
	private final OptionalDouble upperX;
	private final String provenance;

	private IntersectionTargetDomain2D(Kind kind, OptionalDouble lowerX,
			OptionalDouble upperX, String provenance) {
		this.kind = java.util.Objects.requireNonNull(kind);
		this.lowerX = java.util.Objects.requireNonNull(lowerX);
		this.upperX = java.util.Objects.requireNonNull(upperX);
		if (provenance == null || provenance.trim().isEmpty()) {
			throw new IllegalArgumentException("Domain provenance is required");
		}
		this.provenance = provenance;
	}

	/** @return unrestricted model-plane target domain */
	public static IntersectionTargetDomain2D allModelPlane(
			String provenance) {
		return new IntersectionTargetDomain2D(Kind.ALL_MODEL_PLANE,
				OptionalDouble.empty(), OptionalDouble.empty(), provenance);
	}

	/** @return closed, explicit, finite function x-domain */
	public static IntersectionTargetDomain2D finiteClosedXInterval(double lower,
			double upper, String provenance) {
		if (!Double.isFinite(lower) || !Double.isFinite(upper)
				|| lower > upper) {
			throw new IllegalArgumentException(
					"Explicit function domain must be finite and ordered");
		}
		return new IntersectionTargetDomain2D(
				Kind.EXPLICIT_FINITE_X_INTERVAL, OptionalDouble.of(lower),
				OptionalDouble.of(upper), provenance);
	}

	public Kind getKind() {
		return kind;
	}

	public OptionalDouble getLowerX() {
		return lowerX;
	}

	public OptionalDouble getUpperX() {
		return upperX;
	}

	public String getProvenance() {
		return provenance;
	}

	/** @return whether x belongs to this target domain */
	public boolean containsX(double x, double tolerance) {
		if (!Double.isFinite(x) || !Double.isFinite(tolerance)
				|| tolerance < 0) {
			return false;
		}
		return kind == Kind.ALL_MODEL_PLANE
				|| x >= lowerX.getAsDouble() - tolerance
						&& x <= upperX.getAsDouble() + tolerance;
	}

	/** @return whether x is on an included finite-domain boundary */
	public boolean isIncludedBoundary(double x, double tolerance) {
		return kind == Kind.EXPLICIT_FINITE_X_INTERVAL && containsX(x, tolerance)
				&& (Math.abs(x - lowerX.getAsDouble()) <= tolerance
						|| Math.abs(x - upperX.getAsDouble()) <= tolerance);
	}
}
