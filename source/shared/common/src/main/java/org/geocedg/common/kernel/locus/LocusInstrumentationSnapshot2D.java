/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable observation of functional counters at one instant. */
public final class LocusInstrumentationSnapshot2D {
	private final long evaluatorCalls;
	private final long dependencySliceBuilds;
	private final long dependencySliceSynchronizations;
	private final long dependencyUpdates;
	private final long duplicatedRequests;
	private final long sessionHits;
	private final long sessionMisses;
	private final long revisionPublications;
	private final long revisionConsumptions;
	private final long renderEvaluations;
	private final long wholeLocusRegenerations;
	private final Map<String, Long> evaluatorCallsByLocus;
	private final long equivalentRecomputes;
	private final long nestedSliceBuilds;
	private final long nestedRevisionPublications;
	private final long nestedMetricComponentBuilds;
	private final long nestedEvaluatorCalls;

	/** Creates immutable evidence from the pre-CU1 instrumentation dimensions. */
	public LocusInstrumentationSnapshot2D(long evaluatorCalls,
			long dependencySliceBuilds, long dependencySliceSynchronizations,
			long dependencyUpdates, long duplicatedRequests, long sessionHits,
			long sessionMisses, long revisionPublications,
			long revisionConsumptions, long renderEvaluations,
			long wholeLocusRegenerations,
			Map<String, Long> evaluatorCallsByLocus) {
		this(evaluatorCalls, dependencySliceBuilds, dependencySliceSynchronizations,
				dependencyUpdates, duplicatedRequests, sessionHits, sessionMisses,
				revisionPublications, revisionConsumptions, renderEvaluations,
				wholeLocusRegenerations, evaluatorCallsByLocus, 0, 0, 0, 0, 0);
	}

	/**
	 * Creates immutable evidence including the PRE-G9B-CU1 equivalent-recompute
	 * and aggregated nested-work counters.
	 */
	public LocusInstrumentationSnapshot2D(long evaluatorCalls,
			long dependencySliceBuilds, long dependencySliceSynchronizations,
			long dependencyUpdates, long duplicatedRequests, long sessionHits,
			long sessionMisses, long revisionPublications,
			long revisionConsumptions, long renderEvaluations,
			long wholeLocusRegenerations,
			Map<String, Long> evaluatorCallsByLocus, long equivalentRecomputes,
			long nestedSliceBuilds, long nestedRevisionPublications,
			long nestedMetricComponentBuilds, long nestedEvaluatorCalls) {
		this.equivalentRecomputes = equivalentRecomputes;
		this.nestedSliceBuilds = nestedSliceBuilds;
		this.nestedRevisionPublications = nestedRevisionPublications;
		this.nestedMetricComponentBuilds = nestedMetricComponentBuilds;
		this.nestedEvaluatorCalls = nestedEvaluatorCalls;
		this.evaluatorCalls = evaluatorCalls;
		this.dependencySliceBuilds = dependencySliceBuilds;
		this.dependencySliceSynchronizations = dependencySliceSynchronizations;
		this.dependencyUpdates = dependencyUpdates;
		this.duplicatedRequests = duplicatedRequests;
		this.sessionHits = sessionHits;
		this.sessionMisses = sessionMisses;
		this.revisionPublications = revisionPublications;
		this.revisionConsumptions = revisionConsumptions;
		this.renderEvaluations = renderEvaluations;
		this.wholeLocusRegenerations = wholeLocusRegenerations;
		Objects.requireNonNull(evaluatorCallsByLocus);
		this.evaluatorCallsByLocus = Collections.unmodifiableMap(
				new LinkedHashMap<>(evaluatorCallsByLocus));
	}

	public long getEvaluatorCalls() {
		return evaluatorCalls;
	}

	public long getDependencySliceBuilds() {
		return dependencySliceBuilds;
	}

	public long getDependencySliceSynchronizations() {
		return dependencySliceSynchronizations;
	}

	public long getDependencyUpdates() {
		return dependencyUpdates;
	}

	public long getDuplicatedRequests() {
		return duplicatedRequests;
	}

	public long getSessionHits() {
		return sessionHits;
	}

	public long getSessionMisses() {
		return sessionMisses;
	}

	public long getRevisionPublications() {
		return revisionPublications;
	}

	public long getRevisionConsumptions() {
		return revisionConsumptions;
	}

	public long getRenderEvaluations() {
		return renderEvaluations;
	}

	public long getWholeLocusRegenerations() {
		return wholeLocusRegenerations;
	}

	public Map<String, Long> getEvaluatorCallsByLocus() {
		return evaluatorCallsByLocus;
	}

	public long getEquivalentRecomputes() {
		return equivalentRecomputes;
	}

	public long getNestedSliceBuilds() {
		return nestedSliceBuilds;
	}

	public long getNestedRevisionPublications() {
		return nestedRevisionPublications;
	}

	public long getNestedMetricComponentBuilds() {
		return nestedMetricComponentBuilds;
	}

	public long getNestedEvaluatorCalls() {
		return nestedEvaluatorCalls;
	}
}
