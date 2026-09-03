/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/**
 * Optional immutable semantic capability for an oriented piecewise-polynomial
 * locus. Coefficients use descending powers of the provider-canonical
 * parameter and are never recovered from render samples or expression trees.
 * These binary64 arrays may be rounded expansions of a structural source model.
 * They support floating discovery/evaluation, not an implicit exact-coefficient
 * or cross-span continuity certificate. Certified consumers must enclose the
 * actual captured source authority (including its structural constraints).
 */
public interface PiecewisePolynomialLocus2D extends LocusParameterPartition2D {
	/**
	 * Shared hard ceiling for recursive semantic-composition capability access.
	 * Query-specific policies may impose a smaller limit.
	 */
	int MAXIMUM_SAFE_COMPOSITION_DEPTH =
			LocusEvaluationSession2D.MAXIMUM_SAFE_ACTIVE_DEPTH;

	/** @return whether this captured revision exposes polynomial spans */
	default boolean supportsPiecewisePolynomial(LocusDefinition2D definition) {
		return true;
	}

	/** @return number of canonical spans for the requested branch */
	int getPolynomialSpanCount(String branchKey);

	/** @return inclusive lower semantic parameter of one canonical span */
	double getPolynomialSpanLower(String branchKey, int spanIndex);

	/** @return upper semantic parameter of one canonical span */
	double getPolynomialSpanUpper(String branchKey, int spanIndex);

	/**
	 * @param coordinate 0 for x, 1 for y
	 * @return defensive descending-power floating coefficients, not an exactness claim
	 */
	double[] getPolynomialCoefficients(String branchKey, int spanIndex,
			int coordinate);

	/**
	 * Returns both coordinate polynomials from one coherent capability snapshot.
	 * Implementations that compose semantic evaluators should override this method
	 * so requesting x and y does not duplicate the complete composition traversal.
	 *
	 * @return defensive x/y descending-power coefficient arrays
	 */
	default double[][] getPolynomialCoordinateCoefficients(String branchKey,
			int spanIndex) {
		return new double[][] {
				getPolynomialCoefficients(branchKey, spanIndex, 0),
				getPolynomialCoefficients(branchKey, spanIndex, 1)};
	}

	/**
	 * @return positive semantic-composition depth, available without traversing
	 *         the evaluator chain
	 */
	default int getPolynomialCompositionDepth() {
		return 1;
	}

	/**
	 * Evaluates the analytic first derivative on the canonical owning span.
	 *
	 * <p>This is a semantic polynomial operation shared by kernel consumers;
	 * it is not metric or render authority. Interior boundaries belong to the
	 * span that starts at that boundary, matching the canonical spline
	 * evaluator.</p>
	 *
	 * @return derivative with respect to the provider-canonical parameter
	 */
	LocusPoint2D evaluatePolynomialDerivative(String branchKey,
			double providerCanonicalParameter);

	/** @return deterministic captured polynomial capability signature */
	String getPolynomialCapabilitySignature();
}
