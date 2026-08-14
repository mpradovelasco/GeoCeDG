/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Objects;

/** Immutable query binding for one source pair and captured revisions. */
public final class LocusIntersectionQuery2D {
	private final String sourcePairIdentity;
	private final String constructiveIntersectionLineage;
	private final String locusIdentity;
	private final long locusSemanticRevision;
	private final String targetIdentity;
	private final long targetUpdateStamp;
	private final String topologyContext;
	private final LocusIntersectionPolicy2D policy;

	/** Creates one revision-bound query. */
	public LocusIntersectionQuery2D(String sourcePairIdentity,
			String constructiveIntersectionLineage, String locusIdentity,
			long locusSemanticRevision, String targetIdentity,
			long targetUpdateStamp, String topologyContext,
			LocusIntersectionPolicy2D policy) {
		this.sourcePairIdentity = requireText(sourcePairIdentity,
				"Source-pair identity");
		this.constructiveIntersectionLineage = requireText(
				constructiveIntersectionLineage, "Intersection lineage");
		this.locusIdentity = requireText(locusIdentity, "Locus identity");
		this.targetIdentity = requireText(targetIdentity, "Target identity");
		this.topologyContext = requireText(topologyContext, "Topology context");
		if (locusSemanticRevision < 1 || targetUpdateStamp < 0) {
			throw new IllegalArgumentException("Invalid captured source revision");
		}
		this.locusSemanticRevision = locusSemanticRevision;
		this.targetUpdateStamp = targetUpdateStamp;
		this.policy = Objects.requireNonNull(policy);
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

	public String getTopologyContext() {
		return topologyContext;
	}

	public LocusIntersectionPolicy2D getPolicy() {
		return policy;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
