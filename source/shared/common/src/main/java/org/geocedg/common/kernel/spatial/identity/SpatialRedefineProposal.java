/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

import org.geogebra.common.kernel.geos.GeoElement;

/** Actual parsed result and provider signature inspected before host mutation. */
public final class SpatialRedefineProposal {
	private final GeoElement candidate;
	private final SpatialRedefineSignature signature;
	private final int targetedOutputCount;
	private final boolean topologyPreserving;
	private final boolean replacementOperationSelected;

	/** Creates provider-described evidence for an actual parsed candidate. */
	public SpatialRedefineProposal(GeoElement candidate,
			SpatialRedefineSignature signature, int targetedOutputCount,
			boolean topologyPreserving) {
		this(candidate, signature, targetedOutputCount, topologyPreserving, false);
	}

	/** Creates evidence including explicit user selection of true replacement. */
	public SpatialRedefineProposal(GeoElement candidate,
			SpatialRedefineSignature signature, int targetedOutputCount,
			boolean topologyPreserving, boolean replacementOperationSelected) {
		this.candidate = Objects.requireNonNull(candidate);
		this.signature = Objects.requireNonNull(signature);
		this.targetedOutputCount = SpatialRecordSupport.requirePositive(
				targetedOutputCount, "targetedOutputCount");
		this.topologyPreserving = topologyPreserving;
		this.replacementOperationSelected = replacementOperationSelected;
	}

	public GeoElement getCandidate() {
		return candidate;
	}

	public SpatialRedefineSignature getSignature() {
		return signature;
	}

	public int getTargetedOutputCount() {
		return targetedOutputCount;
	}

	public boolean isTopologyPreserving() {
		return topologyPreserving;
	}

	/** @return whether the caller explicitly selected true semantic replacement */
	public boolean isReplacementOperationSelected() {
		return replacementOperationSelected;
	}
}
