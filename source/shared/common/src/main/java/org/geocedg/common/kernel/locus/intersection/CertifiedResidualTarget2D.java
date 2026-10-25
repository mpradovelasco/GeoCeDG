/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/**
 * A captured one-parameter target whose residual composes with outward enclosures of the traced
 * point (PRE-G9B-CU3 piece I-1): the line, segment, ray and circle adapters. Their authority is
 * their binary64 captured state; the residual text of their contract is unchanged.
 */
interface CertifiedResidualTarget2D {

	/**
	 * @param traced {@code {{x, y}, {x', y'}, {x'', y''}}} enclosures of the traced point and
	 *        of its first two derivatives
	 * @return {@code {h, h', h''}} of a residual with the zero set, the signs and the root
	 *         multiplicities of the adapter residual
	 */
	SplineOutwardInterval2D[] residualJet(SplineOutwardInterval2D[][] traced);

	/**
	 * @return outward enclosure of the normalized adapter residual at a point of the given
	 *         coordinate enclosures
	 */
	SplineOutwardInterval2D normalizedResidual(SplineOutwardInterval2D x,
			SplineOutwardInterval2D y);

	/** @return the extent of a bounded target, or {@code NaN} for an unbounded one */
	double boundedExtent();
}
