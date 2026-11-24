/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/**
 * PRE-G9B-MAINT-A2 ({@code bspline-2d.md} section 11.3): optional additive extension of
 * {@link PiecewisePolynomialLocus2D} for a source whose polynomial authority is a local
 * representation per span. For a span {@code [lower, upper]} of the inherited span
 * inventory the local variable is {@code s in [0, 1]} with
 * {@code u = lower + (upper - lower) s}. The inherited global coefficients of such a source
 * are a derived expansion without authority; consumers read span coefficients through
 * {@link PolynomialSpanCoefficients2D}, which returns these local coefficients whenever the
 * source publishes them. A source that does not implement this interface is read exactly as
 * before.
 */
public interface LocalPiecewisePolynomialLocus2D extends PiecewisePolynomialLocus2D {
	/**
	 * @param definition coherent semantic definition of the captured revision
	 * @return whether this revision publishes authoritative local span coefficients
	 */
	boolean publishesLocalPolynomialCoefficients(LocusDefinition2D definition);

	/**
	 * @param branchKey semantic branch
	 * @param spanIndex canonical span index, as for the inherited span inventory
	 * @return defensive x/y coefficient arrays in descending powers of the local variable
	 *         {@code s}; floating values, not an exactness claim
	 */
	double[][] getLocalPolynomialCoordinateCoefficients(String branchKey, int spanIndex);

	/**
	 * Re-expansion amplification of the inherited global coefficients of one branch,
	 * {@code max_j ((|lower_j| + h_j) / h_j)^p} over its spans (section 11.3). It decides the
	 * residual rule for readers that cannot be routed to the local coefficients
	 * ({@link PolynomialSpanCoefficients2D#admitsGlobalPublication}).
	 *
	 * @param branchKey semantic branch
	 * @return amplification, at least 1; positive infinity when it overflows
	 */
	double getGlobalReexpansionConditioning(String branchKey);
}
