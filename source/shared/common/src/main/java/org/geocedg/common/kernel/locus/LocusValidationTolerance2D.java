/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Approved uncertified G6B comparison envelope; it is not eps_domain/render. */
public final class LocusValidationTolerance2D {
	private LocusValidationTolerance2D() {
		// Utility class.
	}

	/**
	 * @param characteristicScale documented local geometric magnitude
	 * @return max(1e-12*max(1,S), 64*ulp(max(1,S)))
	 */
	public static double evaluationEnvelope(double characteristicScale) {
		if (!Double.isFinite(characteristicScale) || characteristicScale < 0) {
			throw new IllegalArgumentException("Characteristic scale must be finite");
		}
		double scale = Math.max(1, characteristicScale);
		return Math.max(1e-12 * scale, 64 * Math.ulp(scale));
	}
}
