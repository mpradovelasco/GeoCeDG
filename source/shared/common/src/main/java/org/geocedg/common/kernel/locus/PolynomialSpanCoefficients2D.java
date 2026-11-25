/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/**
 * PRE-G9B-MAINT-A2 ({@code bspline-2d.md} section 11.3): the single shared accessor through
 * which every polynomial consumer reads the coefficients of one span. When the source
 * publishes local span coefficients ({@link LocalPiecewisePolynomialLocus2D}) it returns
 * them; otherwise it performs exactly the inherited call
 * {@link PiecewisePolynomialLocus2D#getPolynomialCoordinateCoefficients} once and returns its
 * arrays unchanged, so the arithmetic of every other source is bit for bit the previous one.
 *
 * <p>Residual rule (section 11.3, point 4): a reader that cannot be routed through this
 * accessor, for example a wrapper that does not forward the local extension, must gate its
 * polynomial capability on {@link #admitsGlobalPublication}; the declined reason is
 * {@link #POLYNOMIAL_PUBLICATION_ILL_CONDITIONED}. Every production reader is routed, so
 * the rule is dormant in production.</p>
 */
public final class PolynomialSpanCoefficients2D {
	/** Typed reason of a declined global polynomial publication (section 11.3). */
	public static final String POLYNOMIAL_PUBLICATION_ILL_CONDITIONED =
			"POLYNOMIAL_PUBLICATION_ILL_CONDITIONED";
	/**
	 * Candidate bound {@code kappa_max = 2^16} of the residual rule, subject to numerical
	 * validation and to the author's decision (approval record of A-N1).
	 */
	public static final double KAPPA_MAX_CANDIDATE = 0x1p16;

	private final boolean local;
	private final double[][] coordinates;

	private PolynomialSpanCoefficients2D(boolean local, double[][] coordinates) {
		this.local = local;
		this.coordinates = coordinates;
	}

	/**
	 * @param definition coherent semantic definition whose evaluator capability is the source
	 * @param source polynomial capability of that definition
	 * @param branchKey semantic branch
	 * @param spanIndex canonical span index
	 * @return the coefficients of the span in the representation the source authorizes
	 */
	public static PolynomialSpanCoefficients2D read(LocusDefinition2D definition,
			PiecewisePolynomialLocus2D source, String branchKey, int spanIndex) {
		if (source instanceof LocalPiecewisePolynomialLocus2D
				&& ((LocalPiecewisePolynomialLocus2D) source)
						.publishesLocalPolynomialCoefficients(definition)) {
			return new PolynomialSpanCoefficients2D(true,
					((LocalPiecewisePolynomialLocus2D) source)
							.getLocalPolynomialCoordinateCoefficients(branchKey, spanIndex));
		}
		return new PolynomialSpanCoefficients2D(false,
				source.getPolynomialCoordinateCoefficients(branchKey, spanIndex));
	}

	/**
	 * Residual rule of section 11.3: the global coefficients of a source that publishes
	 * local ones may be used by an unrouted reader only while their re-expansion
	 * amplification does not exceed {@link #KAPPA_MAX_CANDIDATE}.
	 *
	 * @param definition coherent semantic definition of the source
	 * @param source polynomial capability of that definition
	 * @return true for every source without local coefficients
	 */
	public static boolean admitsGlobalPublication(LocusDefinition2D definition,
			PiecewisePolynomialLocus2D source) {
		if (!(source instanceof LocalPiecewisePolynomialLocus2D)
				|| !((LocalPiecewisePolynomialLocus2D) source)
						.publishesLocalPolynomialCoefficients(definition)) {
			return true;
		}
		for (LocusBranch2D branch : definition.getBranches()) {
			double kappa = ((LocalPiecewisePolynomialLocus2D) source)
					.getGlobalReexpansionConditioning(branch.getBranchKey());
			if (!(kappa <= KAPPA_MAX_CANDIDATE)) {
				return false;
			}
		}
		return true;
	}

	/** @return whether the arrays are local coefficients in {@code s in [0, 1]} */
	public boolean isLocal() {
		return local;
	}

	/**
	 * @return x/y arrays in descending powers, exactly as returned by the source (global
	 *         parameter when not local); may be null when the source returned null
	 */
	public double[][] getCoordinates() {
		return coordinates;
	}

	/**
	 * @param descending finite nonempty coefficients in descending powers
	 * @return the same coefficients in ascending powers
	 */
	public static double[] ascending(double[] descending) {
		if (descending == null || descending.length == 0) {
			throw new IllegalArgumentException(
					"Polynomial source coefficients must be nonempty");
		}
		double[] result = new double[descending.length];
		for (int index = 0; index < descending.length; index++) {
			double coefficient = descending[descending.length - 1 - index];
			if (!Double.isFinite(coefficient)) {
				throw new IllegalArgumentException(
						"Polynomial source coefficients must be finite");
			}
			result[index] = coefficient;
		}
		return result;
	}
}
