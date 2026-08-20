/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;

/** Durable semantic position independent of revision-scoped component layout. */
public final class LocusSemanticPosition2D {
	private final String locusIdentity;
	private final String branchKey;
	private final String providerVersion;
	private final double providerCanonicalParameter;
	private final String componentLineageKey;
	private final long periodicLift;
	private final SeamSide seamSide;

	/** Creates a durable position from provider-owned semantic coordinates. */
	public LocusSemanticPosition2D(String locusIdentity, String branchKey,
			String providerVersion, double providerCanonicalParameter) {
		this(locusIdentity, branchKey, providerVersion, providerCanonicalParameter,
				null, 0, SeamSide.UNSPECIFIED);
	}

	/**
	 * Creates a durable position with G9U0 component and periodic evidence.
	 *
	 * @param locusIdentity durable source identity
	 * @param branchKey durable branch lineage
	 * @param providerVersion provider contract version
	 * @param providerCanonicalParameter provider-owned canonical coordinate
	 * @param componentLineageKey durable component lineage, or {@code null} for
	 *        a legacy G7 position
	 * @param periodicLift signed periodic lift
	 * @param seamSide explicit seam evidence
	 */
	public LocusSemanticPosition2D(String locusIdentity, String branchKey,
			String providerVersion, double providerCanonicalParameter,
			String componentLineageKey, long periodicLift, SeamSide seamSide) {
		if (locusIdentity == null || locusIdentity.trim().isEmpty()
				|| branchKey == null || branchKey.trim().isEmpty()
				|| providerVersion == null || providerVersion.trim().isEmpty()
				|| !Double.isFinite(providerCanonicalParameter)) {
			throw new IllegalArgumentException("Complete finite semantic position required");
		}
		if (componentLineageKey != null && (componentLineageKey.trim().isEmpty()
				|| !componentLineageKey.equals(componentLineageKey.trim()))) {
			throw new IllegalArgumentException(
					"Component lineage must be null or a canonical token");
		}
		this.locusIdentity = locusIdentity;
		this.branchKey = branchKey;
		this.providerVersion = providerVersion;
		this.providerCanonicalParameter = providerCanonicalParameter == 0
				? 0 : providerCanonicalParameter;
		this.componentLineageKey = componentLineageKey;
		this.periodicLift = periodicLift;
		this.seamSide = Objects.requireNonNull(seamSide);
		if ((seamSide == SeamSide.NOT_PERIODIC
				|| seamSide == SeamSide.UNSPECIFIED) && periodicLift != 0) {
			throw new IllegalArgumentException(
					"A position without periodic evidence cannot carry a lift");
		}
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

	/** @return durable component lineage, or {@code null} for legacy G7 */
	public String getComponentLineageKey() {
		return componentLineageKey;
	}

	public long getPeriodicLift() {
		return periodicLift;
	}

	public SeamSide getSeamSide() {
		return seamSide;
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
								position.providerCanonicalParameter)
				&& Objects.equals(componentLineageKey,
						position.componentLineageKey)
				&& periodicLift == position.periodicLift
				&& seamSide == position.seamSide;
	}

	@Override
	public int hashCode() {
		return Objects.hash(locusIdentity, branchKey, providerVersion,
				Double.doubleToLongBits(providerCanonicalParameter),
				componentLineageKey, periodicLift, seamSide);
	}

	@Override
	public String toString() {
		if (componentLineageKey == null && periodicLift == 0
				&& seamSide == SeamSide.UNSPECIFIED) {
			return locusIdentity + "/" + branchKey + "@"
					+ Double.toHexString(providerCanonicalParameter) + "["
					+ providerVersion + "]";
		}
		return locusIdentity + "/" + branchKey + "@"
				+ Double.toHexString(providerCanonicalParameter) + "["
				+ providerVersion + ";component=" + componentLineageKey
				+ ";lift=" + periodicLift + ";seam=" + seamSide + "]";
	}
}
