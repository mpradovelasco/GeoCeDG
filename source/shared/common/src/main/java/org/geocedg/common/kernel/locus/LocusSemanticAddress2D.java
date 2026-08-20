/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;

/** Durable preimage address, deliberately separate from revision binding. */
public final class LocusSemanticAddress2D {
	/** Explicit periodic seam evidence. */
	public enum SeamSide {
		/** Older G7 position has no persisted seam evidence. */
		UNSPECIFIED,
		NOT_PERIODIC,
		INTERIOR,
		LOWER_APPROACH,
		UPPER_APPROACH
	}

	private final PersistentGeoId sourceLocusId;
	private final String providerVersion;
	private final String branchKey;
	private final String componentLineageKey;
	private final double canonicalParameter;
	private final long periodicLift;
	private final SeamSide seamSide;

	/** Creates one complete durable semantic preimage address. */
	public LocusSemanticAddress2D(PersistentGeoId sourceLocusId,
			String providerVersion, String branchKey, String componentLineageKey,
			double canonicalParameter, long periodicLift, SeamSide seamSide) {
		this.sourceLocusId = Objects.requireNonNull(sourceLocusId);
		this.providerVersion = requireToken(providerVersion, "providerVersion");
		this.branchKey = requireToken(branchKey, "branchKey");
		this.componentLineageKey = requireToken(componentLineageKey,
				"componentLineageKey");
		if (!Double.isFinite(canonicalParameter)) {
			throw new IllegalArgumentException(
					"Semantic address parameter must be finite");
		}
		this.canonicalParameter = canonicalParameter == 0 ? 0 : canonicalParameter;
		this.periodicLift = periodicLift;
		this.seamSide = Objects.requireNonNull(seamSide);
		if (seamSide == SeamSide.NOT_PERIODIC && periodicLift != 0) {
			throw new IllegalArgumentException(
					"A nonperiodic address cannot carry a periodic lift");
		}
	}

	public PersistentGeoId getSourceLocusId() {
		return sourceLocusId;
	}

	public String getProviderVersion() {
		return providerVersion;
	}

	public String getBranchKey() {
		return branchKey;
	}

	public String getComponentLineageKey() {
		return componentLineageKey;
	}

	public double getCanonicalParameter() {
		return canonicalParameter;
	}

	public long getPeriodicLift() {
		return periodicLift;
	}

	public SeamSide getSeamSide() {
		return seamSide;
	}

	/** @return adapter to the frozen G7 metric position contract */
	public LocusSemanticPosition2D toMetricPosition() {
		return new LocusSemanticPosition2D(sourceLocusId.toExternalForm(), branchKey,
				providerVersion, canonicalParameter, componentLineageKey, periodicLift,
				seamSide);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusSemanticAddress2D)) {
			return false;
		}
		LocusSemanticAddress2D address = (LocusSemanticAddress2D) other;
		return sourceLocusId.equals(address.sourceLocusId)
				&& providerVersion.equals(address.providerVersion)
				&& branchKey.equals(address.branchKey)
				&& componentLineageKey.equals(address.componentLineageKey)
				&& Double.doubleToLongBits(canonicalParameter)
						== Double.doubleToLongBits(address.canonicalParameter)
				&& periodicLift == address.periodicLift
				&& seamSide == address.seamSide;
	}

	@Override
	public int hashCode() {
		return Objects.hash(sourceLocusId, providerVersion, branchKey,
				componentLineageKey, Double.doubleToLongBits(canonicalParameter),
				periodicLift, seamSide);
	}

	private static String requireToken(String value, String name) {
		if (value == null || value.trim().isEmpty() || !value.equals(value.trim())) {
			throw new IllegalArgumentException(name + " must be a canonical token");
		}
		return value;
	}
}
