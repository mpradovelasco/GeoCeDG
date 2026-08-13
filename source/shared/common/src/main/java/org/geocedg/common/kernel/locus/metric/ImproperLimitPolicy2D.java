/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Versioned finite-stage policy for improper metric-limit evidence. */
public final class ImproperLimitPolicy2D {
	private final String version;
	private final int maximumStages;
	private final double contractionFactor;

	/** Creates a deterministic improper-limit policy. */
	public ImproperLimitPolicy2D(String version, int maximumStages,
			double contractionFactor) {
		if (version == null || version.trim().isEmpty() || maximumStages < 1
				|| !Double.isFinite(contractionFactor) || contractionFactor <= 0
				|| contractionFactor >= 1) {
			throw new IllegalArgumentException("Invalid improper-limit policy");
		}
		this.version = version;
		this.maximumStages = maximumStages;
		this.contractionFactor = contractionFactor;
	}

	public String getVersion() {
		return version;
	}

	public int getMaximumStages() {
		return maximumStages;
	}

	public double getContractionFactor() {
		return contractionFactor;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ImproperLimitPolicy2D)) {
			return false;
		}
		ImproperLimitPolicy2D policy = (ImproperLimitPolicy2D) other;
		return version.equals(policy.version)
				&& maximumStages == policy.maximumStages
				&& Double.doubleToLongBits(contractionFactor)
						== Double.doubleToLongBits(policy.contractionFactor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, maximumStages,
				Double.doubleToLongBits(contractionFactor));
	}

	@Override
	public String toString() {
		return version + "/" + maximumStages + "/"
				+ Double.toHexString(contractionFactor);
	}
}
