/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Kernel-thread-confined functional counters; no wall-clock authority. */
public final class LocusMetricInstrumentation2D {
	private long componentStateBuilds;
	private long componentStateBuildFailures;
	private long integratorCalls;
	private long evaluatorCalls;
	private long derivativeCalls;
	private long subdivisions;
	private long refinements;
	private long indexHits;
	private long indexMisses;
	private long crossResultHits;
	private long duplicateCompatibleBuilds;
	private long invalidations;
	private long evictions;
	private long activeBuilds;
	private long maximumActiveBuilds;
	private long renderReads;
	private long legacySampleReads;
	private long wholeLocusRegenerations;
	private long indexBuildsInsideDownstreamPoint;

	void recordComponentStateBuildStarted() {
		componentStateBuilds++;
		activeBuilds++;
		maximumActiveBuilds = Math.max(maximumActiveBuilds, activeBuilds);
	}

	void recordComponentStateBuildFinished() {
		if (activeBuilds < 1) {
			throw new IllegalStateException("No active component build to finish");
		}
		activeBuilds--;
	}

	void recordComponentStateBuildFailure() {
		componentStateBuildFailures++;
	}

	void recordIntegratorCall() {
		integratorCalls++;
	}

	void recordEvaluatorCall() {
		evaluatorCalls++;
	}

	void recordDerivativeCall() {
		derivativeCalls++;
	}

	void recordSubdivision() {
		subdivisions++;
	}

	void recordRefinement() {
		refinements++;
	}

	void recordIndexHit() {
		indexHits++;
	}

	void recordIndexMiss() {
		indexMisses++;
	}

	void recordCrossResultHit() {
		crossResultHits++;
	}

	void recordDuplicateCompatibleBuild() {
		duplicateCompatibleBuilds++;
	}

	void recordInvalidation(long removedEntries) {
		invalidations += Math.max(1, removedEntries);
	}

	void recordEviction() {
		evictions++;
	}

	void recordRenderRead() {
		renderReads++;
	}

	void recordLegacySampleRead() {
		legacySampleReads++;
	}

	void recordWholeLocusRegeneration() {
		wholeLocusRegenerations++;
	}

	void recordIndexBuildInsideDownstreamPoint() {
		indexBuildsInsideDownstreamPoint++;
	}

	/** @return immutable functional-counter snapshot */
	public LocusMetricInstrumentationSnapshot2D snapshot() {
		return new LocusMetricInstrumentationSnapshot2D(componentStateBuilds,
				componentStateBuildFailures, integratorCalls, evaluatorCalls,
				derivativeCalls, subdivisions, refinements, indexHits,
				indexMisses, crossResultHits, duplicateCompatibleBuilds,
				invalidations, evictions, activeBuilds, maximumActiveBuilds,
				renderReads, legacySampleReads, wholeLocusRegenerations,
				indexBuildsInsideDownstreamPoint);
	}
}
