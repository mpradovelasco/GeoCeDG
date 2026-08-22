/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D.SamplingStrategy;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;

/** Small per-drawable cache of view-specific tessellations. */
public final class LocusRenderCache2D {
	private static final int MAX_ENTRIES = 4;
	private static final int MINIMUM_ADAPTIVE_DEPTH = 2;
	private final Map<CacheKey, LocusRenderData2D> entries = new LinkedHashMap<>();
	private long hits;
	private long misses;
	private long evictions;
	private long builds;

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
			hits++;
			return cached;
		}
		misses++;
		LocusRenderData2D built = build(locus, policy);
		builds++;
		if (entries.size() == MAX_ENTRIES) {
			CacheKey oldest = entries.keySet().iterator().next();
			entries.remove(oldest);
			evictions++;
		}
		entries.put(key, built);
		return built;
	}

	/** @return current bounded entry count */
	public int size() {
		return entries.size();
	}

	public long getHits() {
		return hits;
	}

	public long getMisses() {
		return misses;
	}

	public long getEvictions() {
		return evictions;
	}

	public long getBuilds() {
		return builds;
	}

	/** Drops view-specific data without touching semantic state. */
	public void clear() {
		entries.clear();
	}

	private static LocusRenderData2D build(GeoLocusV2 locus,
			LocusRenderPolicy2D policy) {
		List<LocusRenderData2D.Vertex> vertices = new ArrayList<>();
		LocusDefinition2D definition = locus.getSemanticDefinition();
		if (definition == null || !locus.isDefined()) {
			return new LocusRenderData2D(locus.getSemanticRevision(), policy,
					vertices);
		}
		try (LocusEvaluationSession2D session =
				new LocusEvaluationSession2D(true, 4096)) {
			for (LocusBranch2D branch : definition.getBranches()) {
				for (LocusInterval2D component
						: branch.getValidDomainComponents()) {
					appendComponent(locus, definition, branch, component, policy,
							session, vertices);
				}
			}
		}
		return new LocusRenderData2D(locus.getSemanticRevision(), policy,
				vertices);
	}

	private static void appendComponent(GeoLocusV2 locus,
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component, LocusRenderPolicy2D policy,
			LocusEvaluationSession2D session,
			List<LocusRenderData2D.Vertex> vertices) {
		double lower = component.getLower();
		double upper = component.getUpper();
		double span = upper - lower;
		double inset = span * policy.getEndpointInsetFraction();
		boolean unbounded = branch.getProperties()
				.contains(BranchProperty.UNBOUNDED);
		boolean fullPeriodicCycle = hasFullPeriodClosure(definition, branch,
				component);
		if (unbounded || (!component.isLowerClosed() && !fullPeriodicCycle)) {
			lower += inset;
		}
		if (unbounded || (!component.isUpperClosed() && !fullPeriodicCycle)) {
			upper -= inset;
		}
		if (!(lower <= upper)) {
			return;
		}
		if (policy.getSamplingStrategy() == SamplingStrategy.ADAPTIVE_VISUAL
				&& appendAdaptiveComponent(locus, branch, lower, upper, policy,
						session, vertices)) {
			return;
		}
		appendUniformComponent(locus, branch, lower, upper, policy, session,
				vertices);
	}

	private static boolean hasFullPeriodClosure(LocusDefinition2D definition,
			LocusBranch2D branch, LocusInterval2D component) {
		return definition.getProvider().isPeriodic()
				&& branch.getProperties().contains(BranchProperty.PERIODIC)
				&& branch.getValidDomainComponents().size() == 1
				&& component.equals(branch.getDeclaredDriverDomain())
				&& component.equals(definition.getProvider().getDeclaredDomain());
	}

	private static void appendUniformComponent(GeoLocusV2 locus,
			LocusBranch2D branch, double lower, double upper,
			LocusRenderPolicy2D policy, LocusEvaluationSession2D session,
			List<LocusRenderData2D.Vertex> vertices) {
		boolean startsSubpath = true;
		int samples = policy.getSamplesPerComponent();
		for (int index = 0; index <= samples; index++) {
			double ratio = index / (double) samples;
			double parameter = lower + ratio * (upper - lower);
			LocusEvaluation2D evaluation = locus.evaluateForRender(
					branch.getBranchKey(), parameter, session);
			if (evaluation.isValid()) {
				vertices.add(new LocusRenderData2D.Vertex(evaluation.getPoint(),
						parameter, startsSubpath));
				startsSubpath = false;
			} else {
				startsSubpath = true;
			}
		}
	}

	private static boolean appendAdaptiveComponent(GeoLocusV2 locus,
			LocusBranch2D branch, double lower, double upper,
			LocusRenderPolicy2D policy, LocusEvaluationSession2D session,
			List<LocusRenderData2D.Vertex> vertices) {
		LocusEvaluation2D lowerEvaluation = locus.evaluateForRender(
				branch.getBranchKey(), lower, session);
		LocusEvaluation2D upperEvaluation = locus.evaluateForRender(
				branch.getBranchKey(), upper, session);
		if (!lowerEvaluation.isValid() || !upperEvaluation.isValid()) {
			return false;
		}
		List<LocusRenderData2D.Vertex> adaptive = new ArrayList<>();
		adaptive.add(new LocusRenderData2D.Vertex(lowerEvaluation.getPoint(), lower,
				true));
		if (!subdivide(locus, branch, lower, lowerEvaluation, upper,
				upperEvaluation, 0, policy, session, adaptive)) {
			return false;
		}
		vertices.addAll(adaptive);
		return true;
	}

	private static boolean subdivide(GeoLocusV2 locus, LocusBranch2D branch,
			double lower, LocusEvaluation2D lowerEvaluation, double upper,
			LocusEvaluation2D upperEvaluation, int depth,
			LocusRenderPolicy2D policy, LocusEvaluationSession2D session,
			List<LocusRenderData2D.Vertex> vertices) {
		double midpoint = lower + (upper - lower) / 2;
		if (midpoint == lower || midpoint == upper) {
			vertices.add(new LocusRenderData2D.Vertex(upperEvaluation.getPoint(),
					upper, false));
			return true;
		}
		LocusEvaluation2D midpointEvaluation = locus.evaluateForRender(
				branch.getBranchKey(), midpoint, session);
		if (!midpointEvaluation.isValid()) {
			return false;
		}
		double error = screenChordError(lowerEvaluation.getPoint(),
				midpointEvaluation.getPoint(), upperEvaluation.getPoint(), policy);
		boolean split = depth < MINIMUM_ADAPTIVE_DEPTH
				|| error > policy.getVisualTolerancePixels();
		if (!split || depth >= policy.getMaximumAdaptiveDepth()) {
			vertices.add(new LocusRenderData2D.Vertex(upperEvaluation.getPoint(),
					upper, false));
			return true;
		}
		return subdivide(locus, branch, lower, lowerEvaluation, midpoint,
				midpointEvaluation, depth + 1, policy, session, vertices)
				&& subdivide(locus, branch, midpoint, midpointEvaluation, upper,
						upperEvaluation, depth + 1, policy, session, vertices);
	}

	private static double screenChordError(LocusPoint2D lower,
			LocusPoint2D midpoint, LocusPoint2D upper,
			LocusRenderPolicy2D policy) {
		double ax = lower.getX() * policy.getXScale();
		double ay = lower.getY() * policy.getYScale();
		double bx = upper.getX() * policy.getXScale();
		double by = upper.getY() * policy.getYScale();
		double px = midpoint.getX() * policy.getXScale();
		double py = midpoint.getY() * policy.getYScale();
		double dx = bx - ax;
		double dy = by - ay;
		double lengthSquared = dx * dx + dy * dy;
		if (lengthSquared == 0) {
			return Math.hypot(px - ax, py - ay);
		}
		double ratio = ((px - ax) * dx + (py - ay) * dy) / lengthSquared;
		ratio = Math.max(0, Math.min(1, ratio));
		return Math.hypot(px - (ax + ratio * dx), py - (ay + ratio * dy));
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
