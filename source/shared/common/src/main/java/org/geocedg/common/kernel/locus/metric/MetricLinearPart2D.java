/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/**
 * Outward 2x2 interval matrix of a linear part, with rigorous bounds of its
 * singular values ({@code locus-v2-metrics.md} sections 25.1 and 25.6). For a unit
 * vector {@code w}, {@code |A w|} lies between the bounds; the bounds come from
 * the eigenvalues {@code m +/- rho} of {@code A^T A}, never from a sampled vector.
 */
final class MetricLinearPart2D {
	static final MetricLinearPart2D IDENTITY = new MetricLinearPart2D(
			MetricOutwardInterval2D.ONE, MetricOutwardInterval2D.ZERO,
			MetricOutwardInterval2D.ZERO, MetricOutwardInterval2D.ONE);

	private final MetricOutwardInterval2D a11;
	private final MetricOutwardInterval2D a12;
	private final MetricOutwardInterval2D a21;
	private final MetricOutwardInterval2D a22;

	MetricLinearPart2D(MetricOutwardInterval2D a11, MetricOutwardInterval2D a12,
			MetricOutwardInterval2D a21, MetricOutwardInterval2D a22) {
		this.a11 = a11;
		this.a12 = a12;
		this.a21 = a21;
		this.a22 = a22;
	}

	/** @return matrix of exact binary64 entries, row by row */
	static MetricLinearPart2D of(double a11, double a12, double a21, double a22) {
		return new MetricLinearPart2D(MetricOutwardInterval2D.point(a11),
				MetricOutwardInterval2D.point(a12), MetricOutwardInterval2D.point(a21),
				MetricOutwardInterval2D.point(a22));
	}

	/** @return {@code factor * I} */
	static MetricLinearPart2D scalar(double factor) {
		return of(factor, 0, 0, factor);
	}

	/** @return {@code this * right} */
	MetricLinearPart2D multiply(MetricLinearPart2D right) {
		if (this == IDENTITY) {
			return right;
		}
		if (right == IDENTITY) {
			return this;
		}
		return new MetricLinearPart2D(
				a11.multiply(right.a11).add(a12.multiply(right.a21)),
				a11.multiply(right.a12).add(a12.multiply(right.a22)),
				a21.multiply(right.a11).add(a22.multiply(right.a21)),
				a21.multiply(right.a12).add(a22.multiply(right.a22)));
	}

	/** @return enclosure of {@code this * (x, y)} as {@code {x', y'}} */
	MetricOutwardInterval2D[] apply(MetricOutwardInterval2D x,
			MetricOutwardInterval2D y) {
		return new MetricOutwardInterval2D[] {
				a11.multiply(x).add(a12.multiply(y)),
				a21.multiply(x).add(a22.multiply(y))};
	}

	/**
	 * @return {@code [sigma_min, sigma_max]}: for every unit {@code w} and every
	 *         matrix in this enclosure, {@code |A w|} lies inside
	 */
	MetricOutwardInterval2D singularValueBounds() {
		if (isExactScalar()) {
			double magnitude = Math.abs(a11.lower);
			return MetricOutwardInterval2D.point(magnitude);
		}
		MetricOutwardInterval2D g11 = a11.square().add(a21.square());
		MetricOutwardInterval2D g22 = a12.square().add(a22.square());
		MetricOutwardInterval2D g12 = a11.multiply(a12).add(a21.multiply(a22));
		MetricOutwardInterval2D mean = g11.add(g22).half();
		MetricOutwardInterval2D difference = g11.subtract(g22).half();
		double radius = MetricOutwardArithmetic2D.sqrtUp(MetricOutwardArithmetic2D
				.addUp(difference.square().upper, g12.square().upper));
		double lowerSquare = MetricOutwardArithmetic2D.nonNegativeDown(
				MetricOutwardArithmetic2D.subDown(mean.lower, radius));
		double upperSquare = MetricOutwardArithmetic2D.addUp(mean.upper, radius);
		return new MetricOutwardInterval2D(
				MetricOutwardArithmetic2D.sqrtDown(lowerSquare),
				MetricOutwardArithmetic2D.sqrtUp(upperSquare));
	}

	/** @return bounds of {@code |(x, y)|} for a vector enclosure */
	static MetricOutwardInterval2D norm(MetricOutwardInterval2D x,
			MetricOutwardInterval2D y) {
		return new MetricOutwardInterval2D(
				MetricOutwardArithmetic2D.hypotDown(x.mignitude(), y.mignitude()),
				MetricOutwardArithmetic2D.hypotUp(x.magnitude(), y.magnitude()));
	}

	private boolean isExactScalar() {
		return a12.isPoint() && a12.lower == 0 && a21.isPoint() && a21.lower == 0
				&& a11.isPoint() && a22.isPoint()
				&& Math.abs(a11.lower) == Math.abs(a22.lower);
	}
}
