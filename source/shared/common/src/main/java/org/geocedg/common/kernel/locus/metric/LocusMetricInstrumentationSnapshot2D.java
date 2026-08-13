/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Immutable functional-counter snapshot for G7B validation. */
public final class LocusMetricInstrumentationSnapshot2D {
	private final long componentStateBuilds;
	private final long componentStateBuildFailures;
	private final long integratorCalls;
	private final long evaluatorCalls;
	private final long derivativeCalls;
	private final long subdivisions;
	private final long refinements;
	private final long indexHits;
	private final long indexMisses;
	private final long crossResultHits;
	private final long duplicateCompatibleBuilds;
	private final long invalidations;
	private final long evictions;
	private final long activeBuilds;
	private final long maximumActiveBuilds;
	private final long renderReads;
	private final long legacySampleReads;
	private final long wholeLocusRegenerations;
	private final long indexBuildsInsideDownstreamPoint;

	LocusMetricInstrumentationSnapshot2D(long componentStateBuilds,
			long componentStateBuildFailures, long integratorCalls,
			long evaluatorCalls, long derivativeCalls, long subdivisions,
			long refinements, long indexHits, long indexMisses,
			long crossResultHits, long duplicateCompatibleBuilds,
			long invalidations, long evictions, long activeBuilds,
			long maximumActiveBuilds, long renderReads,
			long legacySampleReads, long wholeLocusRegenerations,
			long indexBuildsInsideDownstreamPoint) {
		this.componentStateBuilds = componentStateBuilds;
		this.componentStateBuildFailures = componentStateBuildFailures;
		this.integratorCalls = integratorCalls;
		this.evaluatorCalls = evaluatorCalls;
		this.derivativeCalls = derivativeCalls;
		this.subdivisions = subdivisions;
		this.refinements = refinements;
		this.indexHits = indexHits;
		this.indexMisses = indexMisses;
		this.crossResultHits = crossResultHits;
		this.duplicateCompatibleBuilds = duplicateCompatibleBuilds;
		this.invalidations = invalidations;
		this.evictions = evictions;
		this.activeBuilds = activeBuilds;
		this.maximumActiveBuilds = maximumActiveBuilds;
		this.renderReads = renderReads;
		this.legacySampleReads = legacySampleReads;
		this.wholeLocusRegenerations = wholeLocusRegenerations;
		this.indexBuildsInsideDownstreamPoint =
				indexBuildsInsideDownstreamPoint;
	}

	public long getComponentStateBuilds() {
		return componentStateBuilds;
	}

	public long getComponentStateBuildFailures() {
		return componentStateBuildFailures;
	}

	public long getIntegratorCalls() {
		return integratorCalls;
	}

	public long getEvaluatorCalls() {
		return evaluatorCalls;
	}

	public long getDerivativeCalls() {
		return derivativeCalls;
	}

	public long getSubdivisions() {
		return subdivisions;
	}

	public long getRefinements() {
		return refinements;
	}

	public long getIndexHits() {
		return indexHits;
	}

	public long getIndexMisses() {
		return indexMisses;
	}

	public long getCrossResultHits() {
		return crossResultHits;
	}

	public long getDuplicateCompatibleBuilds() {
		return duplicateCompatibleBuilds;
	}

	public long getInvalidations() {
		return invalidations;
	}

	public long getEvictions() {
		return evictions;
	}

	public long getActiveBuilds() {
		return activeBuilds;
	}

	public long getMaximumActiveBuilds() {
		return maximumActiveBuilds;
	}

	public long getRenderReads() {
		return renderReads;
	}

	public long getLegacySampleReads() {
		return legacySampleReads;
	}

	public long getWholeLocusRegenerations() {
		return wholeLocusRegenerations;
	}

	public long getIndexBuildsInsideDownstreamPoint() {
		return indexBuildsInsideDownstreamPoint;
	}
}
