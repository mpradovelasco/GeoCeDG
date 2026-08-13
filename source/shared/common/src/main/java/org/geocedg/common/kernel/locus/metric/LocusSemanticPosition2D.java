/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Durable semantic position independent of revision-scoped component layout. */
public final class LocusSemanticPosition2D {
	private final String locusIdentity;
	private final String branchKey;
	private final String providerVersion;
	private final double providerCanonicalParameter;

	/** Creates a durable position from provider-owned semantic coordinates. */
	public LocusSemanticPosition2D(String locusIdentity, String branchKey,
			String providerVersion, double providerCanonicalParameter) {
		if (locusIdentity == null || locusIdentity.trim().isEmpty()
				|| branchKey == null || branchKey.trim().isEmpty()
				|| providerVersion == null || providerVersion.trim().isEmpty()
				|| !Double.isFinite(providerCanonicalParameter)) {
			throw new IllegalArgumentException("Complete finite semantic position required");
		}
		this.locusIdentity = locusIdentity;
		this.branchKey = branchKey;
		this.providerVersion = providerVersion;
		this.providerCanonicalParameter = providerCanonicalParameter == 0
				? 0 : providerCanonicalParameter;
	}

	public String getLocusIdentity() {
		return locusIdentity;
	}

	public String getBranchKey() {
		return branchKey;
	}

	public String getProviderVersion() {
		return providerVersion;
	}

	public double getProviderCanonicalParameter() {
		return providerCanonicalParameter;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusSemanticPosition2D)) {
			return false;
		}
		LocusSemanticPosition2D position = (LocusSemanticPosition2D) other;
		return locusIdentity.equals(position.locusIdentity)
				&& branchKey.equals(position.branchKey)
				&& providerVersion.equals(position.providerVersion)
				&& Double.doubleToLongBits(providerCanonicalParameter)
						== Double.doubleToLongBits(
								position.providerCanonicalParameter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(locusIdentity, branchKey, providerVersion,
				Double.doubleToLongBits(providerCanonicalParameter));
	}

	@Override
	public String toString() {
		return locusIdentity + "/" + branchKey + "@"
				+ Double.toHexString(providerCanonicalParameter) + "["
				+ providerVersion + "]";
	}
}
