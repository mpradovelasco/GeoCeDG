/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;

/** Immutable source-pair and revision binding for a rich result. */
public final class IntersectionSourceBinding2D {
	private final String sourcePairIdentity;
	private final String constructiveIntersectionLineage;
	private final String locusIdentity;
	private final long locusSemanticRevision;
	private final String targetIdentity;
	private final long targetUpdateStamp;
	private final TargetFamily targetFamily;
	private final String topologyContext;
	private final String policyVersion;

	/** Creates one coherent captured binding. */
	public IntersectionSourceBinding2D(LocusIntersectionQuery2D query,
			TargetFamily targetFamily) {
		this.sourcePairIdentity = query.getSourcePairIdentity();
		this.constructiveIntersectionLineage =
				query.getConstructiveIntersectionLineage();
		this.locusIdentity = query.getLocusIdentity();
		this.locusSemanticRevision = query.getLocusSemanticRevision();
		this.targetIdentity = query.getTargetIdentity();
		this.targetUpdateStamp = query.getTargetUpdateStamp();
		this.targetFamily = java.util.Objects.requireNonNull(targetFamily);
		this.topologyContext = query.getTopologyContext();
		this.policyVersion = query.getPolicy().getPolicyVersion();
	}

	public String getSourcePairIdentity() {
		return sourcePairIdentity;
	}

	public String getConstructiveIntersectionLineage() {
		return constructiveIntersectionLineage;
	}

	public String getLocusIdentity() {
		return locusIdentity;
	}

	public long getLocusSemanticRevision() {
		return locusSemanticRevision;
	}

	public String getTargetIdentity() {
		return targetIdentity;
	}

	public long getTargetUpdateStamp() {
		return targetUpdateStamp;
	}

	public TargetFamily getTargetFamily() {
		return targetFamily;
	}

	public String getTopologyContext() {
		return topologyContext;
	}

	public String getPolicyVersion() {
		return policyVersion;
	}
}
