/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/**
 * The residual of one source and one target in one decision configuration, enclosed on
 * parameter boxes (PRE-G9B-CU3 piece I-1). The captured configuration of a document under
 * {@code legacy-representation-authority/v1} composes the class-v3 second-order enclosure of the
 * traced point with the binary64 target adapter; a document under
 * {@code constructive-authority/v1} encloses the constructive source and target
 * ({@code locus-v2-intersections.md}, constructive authority amendment).
 */
public interface ResidualJetSource2D {

	/** @return name of the decision configuration, recorded in the evidence it produces */
	String getConfiguration();

	/**
	 * Encloses {@code h}, {@code h'} and {@code h''} on one parameter box. A non-null result
	 * certifies that the source is valid and continuous on the whole box.
	 *
	 * @param lower lower end of the box
	 * @param upper upper end of the box, not below {@code lower}
	 * @return the enclosures, or {@code null} when the source is not certified valid on the
	 *         box or a derivative is not enclosed
	 */
	ResidualJetEnclosure2D enclose(double lower, double upper);

	/**
	 * @param parameter a driver parameter
	 * @return outward enclosure {@code {lower, upper}} of the normalized target residual of the
	 *         source point at the parameter, in the distance units of the target adapter, or
	 *         {@code null} when it is not enclosed
	 */
	double[] normalizedResidual(double parameter);
}
