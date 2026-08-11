/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;

/** Small per-drawable cache of view-specific tessellations. */
public final class LocusRenderCache2D {
	private static final int MAX_ENTRIES = 4;
	private final Map<CacheKey, LocusRenderData2D> entries = new LinkedHashMap<>();

	/**
	 * Returns or derives render data for the exact view policy and revision.
	 *
	 * @return bounded derived render data
	 */
	public LocusRenderData2D getOrBuild(GeoLocusV2 locus,
			LocusRenderPolicy2D policy) {
		CacheKey key = new CacheKey(locus.getSemanticRevision(), policy);
		LocusRenderData2D cached = entries.get(key);
		if (cached != null) {
			return cached;
		}
		LocusRenderData2D built = build(locus, policy);
		if (entries.size() == MAX_ENTRIES) {
			CacheKey oldest = entries.keySet().iterator().next();
			entries.remove(oldest);
		}
		entries.put(key, built);
		return built;
	}

	/** @return current bounded entry count */
	public int size() {
		return entries.size();
	}

	private static LocusRenderData2D build(GeoLocusV2 locus,
			LocusRenderPolicy2D policy) {
		List<LocusRenderData2D.Vertex> vertices = new ArrayList<>();
		if (locus.getSemanticDefinition() == null || !locus.isDefined()) {
			return new LocusRenderData2D(locus.getSemanticRevision(), policy,
					vertices);
		}
		LocusEvaluationSession2D session =
				new LocusEvaluationSession2D(true, 4096);
		for (LocusBranch2D branch
				: locus.getSemanticDefinition().getBranches()) {
			for (LocusInterval2D component
					: branch.getValidDomainComponents()) {
				appendComponent(locus, branch, component, policy, session,
						vertices);
			}
		}
		return new LocusRenderData2D(locus.getSemanticRevision(), policy,
				vertices);
	}

	private static void appendComponent(GeoLocusV2 locus, LocusBranch2D branch,
			LocusInterval2D component, LocusRenderPolicy2D policy,
			LocusEvaluationSession2D session,
			List<LocusRenderData2D.Vertex> vertices) {
		double lower = component.getLower();
		double upper = component.getUpper();
		double span = upper - lower;
		double inset = span * policy.getEndpointInsetFraction();
		if (!component.isLowerClosed()
				|| branch.getProperties().contains(BranchProperty.UNBOUNDED)) {
			lower += inset;
		}
		if (!component.isUpperClosed()
				|| branch.getProperties().contains(BranchProperty.UNBOUNDED)) {
			upper -= inset;
		}
		if (!(lower <= upper)) {
			return;
		}
		boolean startsSubpath = true;
		int samples = policy.getSamplesPerComponent();
		for (int index = 0; index <= samples; index++) {
			double ratio = index / (double) samples;
			double parameter = lower + ratio * (upper - lower);
			LocusEvaluation2D evaluation = locus.evaluateForRender(
					branch.getBranchKey(), parameter, session);
			if (evaluation.isValid()) {
				vertices.add(new LocusRenderData2D.Vertex(evaluation.getPoint(),
						startsSubpath));
				startsSubpath = false;
			} else {
				startsSubpath = true;
			}
		}
	}

	private static final class CacheKey {
		private final long revision;
		private final LocusRenderPolicy2D policy;

		CacheKey(long revision, LocusRenderPolicy2D policy) {
			this.revision = revision;
			this.policy = policy;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof CacheKey)) {
				return false;
			}
			CacheKey that = (CacheKey) other;
			return revision == that.revision && policy.equals(that.policy);
		}

		@Override
		public int hashCode() {
			return 31 * Long.hashCode(revision) + policy.hashCode();
		}
	}
}
