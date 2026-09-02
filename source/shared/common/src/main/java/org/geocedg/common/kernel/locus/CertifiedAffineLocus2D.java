/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/**
 * Optional immutable semantic certificate for an affine locus branch
 * {@code F(u) = a u + b}. The certificate is provider-owned evidence; it is
 * never inferred from samples, rendering or a numerical fit.
 */
public interface CertifiedAffineLocus2D {
	/** @return whether this captured definition has certified affine data */
	boolean supportsCertifiedAffine(LocusDefinition2D definition);

	/**
	 * @param coordinate 0 for x, 1 for y
	 * @return {@code [slope, intercept]} for the requested semantic branch
	 */
	double[] getCertifiedAffineCoefficients(String branchKey, int coordinate);

	/** @return deterministic captured certificate signature */
	String getCertifiedAffineSignature();
}
