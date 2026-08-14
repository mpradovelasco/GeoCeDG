/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

/** Immutable raw and normalized target-incidence residual evidence. */
public final class TargetResidual2D {
	private final double rawResidual;
	private final double normalizationScale;
	private final double normalizedResidual;
	private final double characteristicScale;
	private final IntersectionResidualContract2D contract;

	/** Creates finite residual evidence with an explicit normalization. */
	public TargetResidual2D(double rawResidual, double normalizationScale,
			double normalizedResidual, double characteristicScale,
			IntersectionResidualContract2D contract) {
		if (!Double.isFinite(rawResidual)
				|| !Double.isFinite(normalizationScale)
				|| normalizationScale <= 0
				|| !Double.isFinite(normalizedResidual)
				|| !Double.isFinite(characteristicScale)
				|| characteristicScale <= 0) {
			throw new IllegalArgumentException("Residual evidence must be finite");
		}
		this.rawResidual = rawResidual;
		this.normalizationScale = normalizationScale;
		this.normalizedResidual = normalizedResidual;
		this.characteristicScale = characteristicScale;
		this.contract = Objects.requireNonNull(contract);
	}

	public double getRawResidual() {
		return rawResidual;
	}

	public double getNormalizationScale() {
		return normalizationScale;
	}

	public double getNormalizedResidual() {
		return normalizedResidual;
	}

	public double getCharacteristicScale() {
		return characteristicScale;
	}

	public IntersectionResidualContract2D getContract() {
		return contract;
	}
}
