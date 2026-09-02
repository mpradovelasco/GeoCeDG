/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.interaction;

/** Immutable R6 work evidence; forbidden presentation reads remain explicit. */
public final class LocusPointInteractionInstrumentationSnapshot2D {
	private final long semanticEvaluations;
	private final long branchesInspected;
	private final long componentsInspected;
	private final long polynomialSpans;
	private final long subdivisions;
	private final long refinementIterations;
	private final long retainedCandidates;
	private final long localSearches;
	private final long globalSearches;
	private final long globalFallbacks;
	private final long cacheHits;
	private final long cacheMisses;
	private final long renderReads;
	private final long viewportReads;
	private final long pixelReads;

	LocusPointInteractionInstrumentationSnapshot2D(long semanticEvaluations,
			long branchesInspected, long componentsInspected, long polynomialSpans,
			long subdivisions, long refinementIterations, long retainedCandidates,
			long localSearches, long globalSearches, long globalFallbacks,
			long cacheHits, long cacheMisses) {
		this.semanticEvaluations = semanticEvaluations;
		this.branchesInspected = branchesInspected;
		this.componentsInspected = componentsInspected;
		this.polynomialSpans = polynomialSpans;
		this.subdivisions = subdivisions;
		this.refinementIterations = refinementIterations;
		this.retainedCandidates = retainedCandidates;
		this.localSearches = localSearches;
		this.globalSearches = globalSearches;
		this.globalFallbacks = globalFallbacks;
		this.cacheHits = cacheHits;
		this.cacheMisses = cacheMisses;
		renderReads = 0;
		viewportReads = 0;
		pixelReads = 0;
	}

	public long getSemanticEvaluations() {
		return semanticEvaluations;
	}

	public long getBranchesInspected() {
		return branchesInspected;
	}

	public long getComponentsInspected() {
		return componentsInspected;
	}

	public long getPolynomialSpans() {
		return polynomialSpans;
	}

	public long getSubdivisions() {
		return subdivisions;
	}

	public long getRefinementIterations() {
		return refinementIterations;
	}

	public long getRetainedCandidates() {
		return retainedCandidates;
	}

	public long getLocalSearches() {
		return localSearches;
	}

	public long getGlobalSearches() {
		return globalSearches;
	}

	public long getGlobalFallbacks() {
		return globalFallbacks;
	}

	/** @return exact semantic-session cache hits during this query */
	public long getCacheHits() {
		return cacheHits;
	}

	/** @return exact semantic-session cache misses during this query */
	public long getCacheMisses() {
		return cacheMisses;
	}

	public long getRenderReads() {
		return renderReads;
	}

	public long getViewportReads() {
		return viewportReads;
	}

	public long getPixelReads() {
		return pixelReads;
	}
}
