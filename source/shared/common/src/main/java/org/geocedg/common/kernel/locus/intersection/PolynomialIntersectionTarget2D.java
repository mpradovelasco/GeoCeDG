/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/**
 * Optional captured implicit-polynomial authority for an intersection target.
 *
 * <p>The returned matrix uses ascending powers, so entry {@code [i][j]} is
 * the coefficient of {@code x^i y^j}. It is source-semantic input for
 * span-wise isolation and is never reconstructed from rendering or labels.</p>
 */
public interface PolynomialIntersectionTarget2D
		extends LocusIntersectionTarget2D {
	/** @return defensive finite implicit-polynomial coefficient matrix */
	double[][] getImplicitPolynomialCoefficients();
}
