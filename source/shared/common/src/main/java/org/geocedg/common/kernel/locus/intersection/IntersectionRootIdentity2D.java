/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Optional;

import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;

/** Durable identity layer, separate from all revision-scoped numerics. */
public final class IntersectionRootIdentity2D {
	private final String rootToken;
	private final String sourcePairIdentity;
	private final String constructiveIntersectionLineage;
	private final String establishedBranchLineage;
	private final String topologyContext;
	private final Optional<String> explicitContinuationKey;
	private final IdentityStatus identityStatus;

	/** Creates one opaque semantic solution identity. */
	public IntersectionRootIdentity2D(String rootToken,
			String sourcePairIdentity, String constructiveIntersectionLineage,
			String establishedBranchLineage, String topologyContext,
			Optional<String> explicitContinuationKey,
			IdentityStatus identityStatus) {
		this.rootToken = requireText(rootToken, "Root token");
		this.sourcePairIdentity = requireText(sourcePairIdentity,
				"Source-pair identity");
		this.constructiveIntersectionLineage = requireText(
				constructiveIntersectionLineage, "Intersection lineage");
		this.establishedBranchLineage = requireText(establishedBranchLineage,
				"Branch lineage");
		this.topologyContext = requireText(topologyContext, "Topology context");
		this.explicitContinuationKey = checkedOptional(explicitContinuationKey);
		this.identityStatus = java.util.Objects.requireNonNull(identityStatus);
	}

	public String getRootToken() {
		return rootToken;
	}

	public String getSourcePairIdentity() {
		return sourcePairIdentity;
	}

	public String getConstructiveIntersectionLineage() {
		return constructiveIntersectionLineage;
	}

	public String getEstablishedBranchLineage() {
		return establishedBranchLineage;
	}

	public String getTopologyContext() {
		return topologyContext;
	}

	public Optional<String> getExplicitContinuationKey() {
		return explicitContinuationKey;
	}

	public IdentityStatus getIdentityStatus() {
		return identityStatus;
	}

	private static Optional<String> checkedOptional(Optional<String> value) {
		java.util.Objects.requireNonNull(value);
		if (value.isPresent() && value.get().trim().isEmpty()) {
			throw new IllegalArgumentException("Continuation key cannot be blank");
		}
		return value;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
