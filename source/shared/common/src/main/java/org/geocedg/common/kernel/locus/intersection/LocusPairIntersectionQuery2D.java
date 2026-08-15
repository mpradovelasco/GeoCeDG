/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusDefinition2D;

/** Immutable canonical-source query for two semantic Locus V2 revisions. */
public final class LocusPairIntersectionQuery2D {
	private final LocusSourceBinding2D first;
	private final LocusSourceBinding2D second;
	private final String sourcePairIdentity;
	private final String constructiveIntersectionLineage;
	private final String topologyContext;
	private final boolean callerOrderCanonical;
	private final LocusPairIntersectionPolicy2D policy;

	/** Captures both sources and canonicalizes their geometric order. */
	public LocusPairIntersectionQuery2D(LocusDefinition2D callerFirst,
			LocusDefinition2D callerSecond,
			String constructiveIntersectionLineage, String topologyContext,
			LocusPairIntersectionPolicy2D callerOrderedPolicy) {
		java.util.Objects.requireNonNull(callerFirst);
		java.util.Objects.requireNonNull(callerSecond);
		LocusSourceBinding2D firstCapture =
				new LocusSourceBinding2D(callerFirst);
		LocusSourceBinding2D secondCapture =
				new LocusSourceBinding2D(callerSecond);
		this.callerOrderCanonical = firstCapture.getLocusIdentity().compareTo(
				secondCapture.getLocusIdentity()) <= 0;
		this.first = callerOrderCanonical ? firstCapture : secondCapture;
		this.second = callerOrderCanonical ? secondCapture : firstCapture;
		this.policy = callerOrderCanonical
				? java.util.Objects.requireNonNull(callerOrderedPolicy)
				: java.util.Objects.requireNonNull(callerOrderedPolicy).reversed();
		this.sourcePairIdentity = LocusPairIdentity2D.sourcePair(
				first.getLocusIdentity(), second.getLocusIdentity());
		this.constructiveIntersectionLineage = requireText(
				constructiveIntersectionLineage, "Intersection lineage");
		this.topologyContext = requireText(topologyContext, "Topology context");
	}

	public LocusSourceBinding2D getFirst() {
		return first;
	}

	public LocusSourceBinding2D getSecond() {
		return second;
	}

	public String getSourcePairIdentity() {
		return sourcePairIdentity;
	}

	public String getConstructiveIntersectionLineage() {
		return constructiveIntersectionLineage;
	}

	public String getTopologyContext() {
		return topologyContext;
	}

	public boolean isCallerOrderCanonical() {
		return callerOrderCanonical;
	}

	public LocusPairIntersectionPolicy2D getPolicy() {
		return policy;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
