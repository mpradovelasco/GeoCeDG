/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * Immutable evidence of a length measured on a route that lies in one conditioned region
 * (ADR 0042; {@code locus-v2-metrics.md} section 28.3, decision DN-2 of the author,
 * 2026-11-22). It records the numeric coverage of the requested route, which is not the
 * geometric coverage of the locus, the hypotheses under which the route was travelled and
 * the content that makes the evidence current. It supplies no geometric authority: it
 * proves no existence, continuity, completeness, incidence or intersection.
 */
public final class LocusMetricConditionedRoute2D {
	/** Version of the admission rule of section 28.3. */
	public static final String VERSION = "conditioned-length-scalar/v1";

	/** The hypotheses assumed between the evaluated nodes of the spans of the route. */
	public static final String HYPOTHESES = "H1, H2, H3, H4";

	private final String branchKey;
	private final LocusInterval2D region;
	private final double routeLower;
	private final double routeUpper;
	private final String evidenceContentIdentity;
	private final String traversalPolicyVersion;
	private final long semanticRevision;
	private final Set<String> spanClasses;

	LocusMetricConditionedRoute2D(String branchKey, LocusInterval2D region,
			double routeLower, double routeUpper, String evidenceContentIdentity,
			String traversalPolicyVersion, long semanticRevision, Set<String> spanClasses) {
		this.branchKey = Objects.requireNonNull(branchKey);
		this.region = Objects.requireNonNull(region);
		if (!(region.getLower() <= routeLower && routeLower <= routeUpper
				&& routeUpper <= region.getUpper())) {
			throw new IllegalArgumentException(
					"The route of a conditioned length lies in one region");
		}
		this.routeLower = routeLower;
		this.routeUpper = routeUpper;
		this.evidenceContentIdentity = Objects.requireNonNull(evidenceContentIdentity);
		this.traversalPolicyVersion = Objects.requireNonNull(traversalPolicyVersion);
		this.semanticRevision = semanticRevision;
		this.spanClasses = Collections.unmodifiableSet(new TreeSet<>(spanClasses));
	}

	/** @return the version of the admission rule */
	public String getPolicyVersion() {
		return VERSION;
	}

	public String getBranchKey() {
		return branchKey;
	}

	/** @return the conditioned region that contains the whole route */
	public LocusInterval2D getRegion() {
		return region;
	}

	/** @return lower canonical parameter of the stretch of the region that was measured */
	public double getRouteLower() {
		return routeLower;
	}

	/** @return upper canonical parameter of the stretch of the region that was measured */
	public double getRouteUpper() {
		return routeUpper;
	}

	/** @return the hypotheses assumed between the evaluated nodes */
	public String getHypotheses() {
		return HYPOTHESES;
	}

	/** @return content identity of the conditioned evidence that published the region */
	public String getEvidenceContentIdentity() {
		return evidenceContentIdentity;
	}

	/** @return version of the traversal policy of that evidence */
	public String getTraversalPolicyVersion() {
		return traversalPolicyVersion;
	}

	/** @return semantic revision of the source at which the route was measured */
	public long getSemanticRevision() {
		return semanticRevision;
	}

	/** @return the classes of the spans that the route travels */
	public Set<String> getSpanClasses() {
		return spanClasses;
	}

	@Override
	public String toString() {
		return VERSION + " branch=" + branchKey + " region=[" + region.getLower() + ", "
				+ region.getUpper() + "] route=[" + routeLower + ", " + routeUpper
				+ "] hypotheses=" + HYPOTHESES + " spans=" + spanClasses + " policy="
				+ traversalPolicyVersion + " content=" + evidenceContentIdentity
				+ " revision=" + semanticRevision;
	}
}
