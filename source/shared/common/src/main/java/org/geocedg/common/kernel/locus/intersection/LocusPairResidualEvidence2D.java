/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;

/** Independently evaluated two-sided model-coordinate residual evidence. */
public final class LocusPairResidualEvidence2D {
	private final LocusPoint2D firstPoint;
	private final LocusPoint2D secondPoint;
	private final double modelCoordinateResidual;
	private final double acceptedThreshold;
	private final NumericGuarantee numericGuarantee;

	/** Creates finite symmetric pair-residual evidence. */
	public LocusPairResidualEvidence2D(LocusPoint2D firstPoint,
			LocusPoint2D secondPoint, double modelCoordinateResidual,
			double acceptedThreshold, NumericGuarantee numericGuarantee) {
		this.firstPoint = finite(firstPoint);
		this.secondPoint = finite(secondPoint);
		if (!Double.isFinite(modelCoordinateResidual)
				|| modelCoordinateResidual < 0
				|| !Double.isFinite(acceptedThreshold)
				|| acceptedThreshold <= 0) {
			throw new IllegalArgumentException("Invalid pair residual evidence");
		}
		this.modelCoordinateResidual = modelCoordinateResidual;
		this.acceptedThreshold = acceptedThreshold;
		this.numericGuarantee = java.util.Objects.requireNonNull(
				numericGuarantee);
	}

	/** @return evidence with ordered semantic points exchanged */
	public LocusPairResidualEvidence2D reversed() {
		return new LocusPairResidualEvidence2D(secondPoint, firstPoint,
				modelCoordinateResidual, acceptedThreshold, numericGuarantee);
	}

	public LocusPoint2D getFirstPoint() {
		return firstPoint;
	}

	public LocusPoint2D getSecondPoint() {
		return secondPoint;
	}

	public double getModelCoordinateResidual() {
		return modelCoordinateResidual;
	}

	public double getAcceptedThreshold() {
		return acceptedThreshold;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	private static LocusPoint2D finite(LocusPoint2D point) {
		java.util.Objects.requireNonNull(point);
		if (!Double.isFinite(point.getX()) || !Double.isFinite(point.getY())) {
			throw new IllegalArgumentException("Pair point must be finite");
		}
		return point;
	}
}
