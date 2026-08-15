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
	private final boolean locusPair;
	private final String secondLocusIdentity;
	private final long secondLocusSemanticRevision;

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
		this.locusPair = false;
		this.secondLocusIdentity = null;
		this.secondLocusSemanticRevision = 0;
	}

	/** Creates a canonical two-locus binding for one captured pair query. */
	public IntersectionSourceBinding2D(LocusPairIntersectionQuery2D query) {
		java.util.Objects.requireNonNull(query);
		this.sourcePairIdentity = query.getSourcePairIdentity();
		this.constructiveIntersectionLineage =
				query.getConstructiveIntersectionLineage();
		this.locusIdentity = query.getFirst().getLocusIdentity();
		this.locusSemanticRevision = query.getFirst().getSemanticRevision();
		this.targetIdentity = query.getSecond().getLocusIdentity();
		this.targetUpdateStamp = query.getSecond().getSemanticRevision();
		this.targetFamily = TargetFamily.LOCUS_V2;
		this.topologyContext = query.getTopologyContext();
		this.policyVersion = query.getPolicy().getPolicyVersion();
		this.locusPair = true;
		this.secondLocusIdentity = query.getSecond().getLocusIdentity();
		this.secondLocusSemanticRevision =
				query.getSecond().getSemanticRevision();
	}

	/**
	 * Creates a typed invalid-input pair binding before definitions exist.
	 *
	 * @return canonical unavailable pair binding
	 */
	public static IntersectionSourceBinding2D unavailableLocusPair(
			String firstIdentity, String secondIdentity,
			String constructiveLineage, String topologyContext) {
		return new IntersectionSourceBinding2D(firstIdentity, secondIdentity,
				constructiveLineage, topologyContext);
	}

	private IntersectionSourceBinding2D(String firstIdentity,
			String secondIdentity, String constructiveLineage,
			String topologyContext) {
		boolean canonical = firstIdentity.compareTo(secondIdentity) <= 0;
		this.locusIdentity = canonical ? firstIdentity : secondIdentity;
		this.secondLocusIdentity = canonical ? secondIdentity : firstIdentity;
		this.sourcePairIdentity = LocusPairIdentity2D.sourcePair(firstIdentity,
				secondIdentity);
		this.constructiveIntersectionLineage = constructiveLineage;
		this.locusSemanticRevision = 1;
		this.secondLocusSemanticRevision = 1;
		this.targetIdentity = secondLocusIdentity;
		this.targetUpdateStamp = 1;
		this.targetFamily = TargetFamily.LOCUS_V2;
		this.topologyContext = topologyContext;
		this.policyVersion = LocusPairIntersectionPolicy2D.POLICY_VERSION;
		this.locusPair = true;
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

	public boolean isLocusPair() {
		return locusPair;
	}

	public String getFirstLocusIdentity() {
		return locusIdentity;
	}

	public long getFirstLocusSemanticRevision() {
		return locusSemanticRevision;
	}

	/** @return second identity for a pair binding, otherwise {@code null} */
	public String getSecondLocusIdentity() {
		return secondLocusIdentity;
	}

	/** @return second revision for a pair binding, otherwise zero */
	public long getSecondLocusSemanticRevision() {
		return secondLocusSemanticRevision;
	}
}
