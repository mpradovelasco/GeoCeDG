/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ResidualQuantityKind;

/** Typed physical meaning and provenance of one target residual. */
public final class IntersectionResidualContract2D {
	private final String adapterVersion;
	private final ResidualQuantityKind quantityKind;
	private final String units;
	private final String normalizationProvenance;
	private final String characteristicScalePolicy;

	/** Creates one immutable target-family residual contract. */
	public IntersectionResidualContract2D(String adapterVersion,
			ResidualQuantityKind quantityKind, String units,
			String normalizationProvenance,
			String characteristicScalePolicy) {
		this.adapterVersion = requireText(adapterVersion, "Adapter version");
		this.quantityKind = Objects.requireNonNull(quantityKind);
		this.units = requireText(units, "Residual units");
		this.normalizationProvenance = requireText(normalizationProvenance,
				"Normalization provenance");
		this.characteristicScalePolicy = requireText(
				characteristicScalePolicy, "Characteristic-scale policy");
	}

	public String getAdapterVersion() {
		return adapterVersion;
	}

	public ResidualQuantityKind getQuantityKind() {
		return quantityKind;
	}

	public String getUnits() {
		return units;
	}

	public String getNormalizationProvenance() {
		return normalizationProvenance;
	}

	public String getCharacteristicScalePolicy() {
		return characteristicScalePolicy;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
