/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;

/** Parameter derivative plus its regularity and numerical guarantee. */
public final class LocusDifferentialEvaluation2D {
	private final LocusPoint2D derivative;
	private final Regularity regularity;
	private final NumericGuarantee numericGuarantee;
	private final String provenance;

	/** Creates finite differential evidence. */
	public LocusDifferentialEvaluation2D(LocusPoint2D derivative,
			Regularity regularity, NumericGuarantee numericGuarantee,
			String provenance) {
		this.derivative = java.util.Objects.requireNonNull(derivative);
		if (!Double.isFinite(derivative.getX())
				|| !Double.isFinite(derivative.getY())) {
			throw new IllegalArgumentException("Derivative must be finite");
		}
		this.regularity = java.util.Objects.requireNonNull(regularity);
		this.numericGuarantee = java.util.Objects.requireNonNull(numericGuarantee);
		if (provenance == null || provenance.trim().isEmpty()) {
			throw new IllegalArgumentException("Derivative provenance is required");
		}
		this.provenance = provenance;
	}

	public LocusPoint2D getDerivative() {
		return derivative;
	}

	public Regularity getRegularity() {
		return regularity;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	public String getProvenance() {
		return provenance;
	}
}
