/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Explicit functional counters; wall-clock speed is not their authority.
 *
 * <p>PRE-G9B-CU1 ({@code locus-v2-semantics.md} v1.2): a locus copy inside a
 * reconstruction slice reports its slice builds, revision publications,
 * evaluator calls and metric component builds to the locus that owns the slice,
 * which aggregates them as nested work.</p>
 */
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
	private long equivalentRecomputes;
	private long nestedSliceBuilds;
	private long nestedRevisionPublications;
	private long nestedMetricComponentBuilds;
	private long nestedEvaluatorCalls;
	private LocusInstrumentation2D nestedParent;
	private final Map<String, Long> evaluatorCallsByLocus = new LinkedHashMap<>();

	/** Records one semantic evaluator invocation for a locus identity. */
	public void recordEvaluatorCall(String locusIdentity) {
		evaluatorCalls++;
		Long current = evaluatorCallsByLocus.get(locusIdentity);
		evaluatorCallsByLocus.put(locusIdentity, current == null ? 1 : current + 1);
		if (nestedParent != null) {
			nestedParent.recordNestedEvaluatorCalls(1);
		}
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
		if (nestedParent != null) {
			nestedParent.recordNestedRevisionPublications(1);
		}
	}

	/**
	 * Records one recompute whose semantic snapshot was unchanged, so the
	 * published revision and evaluator were retained.
	 */
	public void recordEquivalentRecompute() {
		equivalentRecomputes++;
	}

	/**
	 * Records one reconstruction-slice build; a build always synchronizes the
	 * slice with its inputs once.
	 */
	public void recordSliceBuild() {
		dependencySliceBuilds++;
		dependencySliceSynchronizations++;
		if (nestedParent != null) {
			nestedParent.recordNestedSliceBuilds(1);
		}
	}

	/** Records one metric component-state build of a nested locus copy. */
	public void recordNestedMetricComponentBuild() {
		recordNestedMetricComponentBuilds(1);
	}

	/** Records one downstream consumption of an upstream semantic revision. */
	public void recordRevisionConsumption() {
		revisionConsumptions++;
	}

	/** Records one render-only use of the semantic evaluator. */
	public void recordRenderEvaluation() {
		renderEvaluations++;
	}

	/**
	 * Attaches this instrumentation, owned by a locus copy inside a
	 * reconstruction slice, to the instrumentation of the locus that owns the
	 * slice. The work already recorded here becomes nested work of the parent.
	 *
	 * @param parent owner instrumentation; never this instance
	 */
	public void attachNestedParent(LocusInstrumentation2D parent) {
		if (parent == null || parent == this || nestedParent == parent) {
			return;
		}
		if (nestedParent != null) {
			throw new IllegalStateException(
					"A nested locus reports to one slice owner only");
		}
		nestedParent = parent;
		parent.recordNestedSliceBuilds(dependencySliceBuilds + nestedSliceBuilds);
		parent.recordNestedRevisionPublications(
				revisionPublications + nestedRevisionPublications);
		parent.recordNestedEvaluatorCalls(evaluatorCalls + nestedEvaluatorCalls);
		parent.recordNestedMetricComponentBuilds(nestedMetricComponentBuilds);
	}

	/** @return owner instrumentation of the enclosing slice, or {@code null} */
	public LocusInstrumentation2D getNestedParent() {
		return nestedParent;
	}

	private void recordNestedSliceBuilds(long count) {
		if (count > 0) {
			nestedSliceBuilds += count;
			if (nestedParent != null) {
				nestedParent.recordNestedSliceBuilds(count);
			}
		}
	}

	private void recordNestedRevisionPublications(long count) {
		if (count > 0) {
			nestedRevisionPublications += count;
			if (nestedParent != null) {
				nestedParent.recordNestedRevisionPublications(count);
			}
		}
	}

	private void recordNestedEvaluatorCalls(long count) {
		if (count > 0) {
			nestedEvaluatorCalls += count;
			if (nestedParent != null) {
				nestedParent.recordNestedEvaluatorCalls(count);
			}
		}
	}

	/** Records metric component-state builds of nested locus copies. */
	public void recordNestedMetricComponentBuilds(long count) {
		if (count > 0) {
			nestedMetricComponentBuilds += count;
			if (nestedParent != null) {
				nestedParent.recordNestedMetricComponentBuilds(count);
			}
		}
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

	public Map<String, Long> getEvaluatorCallsByLocus() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(evaluatorCallsByLocus));
	}

	/** @return immutable diagnostic evidence without exposing mutable counters */
	public LocusInstrumentationSnapshot2D snapshot() {
		return new LocusInstrumentationSnapshot2D(evaluatorCalls,
				dependencySliceBuilds, dependencySliceSynchronizations,
				dependencyUpdates, duplicatedRequests, sessionHits, sessionMisses,
				revisionPublications, revisionConsumptions, renderEvaluations,
				wholeLocusRegenerations, evaluatorCallsByLocus,
				equivalentRecomputes, nestedSliceBuilds, nestedRevisionPublications,
				nestedMetricComponentBuilds, nestedEvaluatorCalls);
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
		equivalentRecomputes = 0;
		nestedSliceBuilds = 0;
		nestedRevisionPublications = 0;
		nestedMetricComponentBuilds = 0;
		nestedEvaluatorCalls = 0;
		evaluatorCallsByLocus.clear();
	}
}
