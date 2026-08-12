/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Explicit functional counters; wall-clock speed is not their authority. */
public final class LocusInstrumentation2D {
	private long evaluatorCalls;
	private long dependencySliceBuilds;
	private long dependencySliceSynchronizations;
	private long dependencyUpdates;
	private long duplicatedRequests;
	private long sessionHits;
	private long sessionMisses;
	private long revisionPublications;
	private long revisionConsumptions;
	private long renderEvaluations;
	private long wholeLocusRegenerations;
	private final Map<String, Long> evaluatorCallsByLocus = new LinkedHashMap<>();

	/** Records one semantic evaluator invocation for a locus identity. */
	public void recordEvaluatorCall(String locusIdentity) {
		evaluatorCalls++;
		Long current = evaluatorCallsByLocus.get(locusIdentity);
		evaluatorCallsByLocus.put(locusIdentity, current == null ? 1 : current + 1);
	}

	/** Records one normal-DAG algorithm update. */
	public void recordDependencyUpdate() {
		dependencyUpdates++;
	}

	/** Records one exact-key request served more than once in a session. */
	public void recordDuplicateRequest() {
		duplicatedRequests++;
	}

	/** Records one scoped-session cache hit. */
	public void recordSessionHit() {
		sessionHits++;
	}

	/** Records one scoped-session cache miss. */
	public void recordSessionMiss() {
		sessionMisses++;
	}

	/** Records one newly published semantic revision. */
	public void recordRevisionPublication() {
		revisionPublications++;
	}

	/** Records one downstream consumption of an upstream semantic revision. */
	public void recordRevisionConsumption() {
		revisionConsumptions++;
	}

	/** Records one render-only use of the semantic evaluator. */
	public void recordRenderEvaluation() {
		renderEvaluations++;
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
		return Collections.unmodifiableMap(new LinkedHashMap<>(evaluatorCallsByLocus));
	}

	/** @return immutable diagnostic evidence without exposing mutable counters */
	public LocusInstrumentationSnapshot2D snapshot() {
		return new LocusInstrumentationSnapshot2D(evaluatorCalls,
				dependencySliceBuilds, dependencySliceSynchronizations,
				dependencyUpdates, duplicatedRequests, sessionHits, sessionMisses,
				revisionPublications, revisionConsumptions, renderEvaluations,
				wholeLocusRegenerations, evaluatorCallsByLocus);
	}

	/** Resets evidence counters without changing semantic state. */
	public void reset() {
		evaluatorCalls = 0;
		dependencySliceBuilds = 0;
		dependencySliceSynchronizations = 0;
		dependencyUpdates = 0;
		duplicatedRequests = 0;
		sessionHits = 0;
		sessionMisses = 0;
		revisionPublications = 0;
		revisionConsumptions = 0;
		renderEvaluations = 0;
		wholeLocusRegenerations = 0;
		evaluatorCallsByLocus.clear();
	}
}
