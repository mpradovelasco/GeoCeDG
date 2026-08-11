/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;

/**
 * Bounded disposable context for one coherent semantic evaluation or batch.
 * It memoizes results but never owns dependency edges or semantic definitions.
 */
public final class LocusEvaluationSession2D {
	private final boolean memoizationEnabled;
	private final int maximumEntries;
	private final Map<LocusSemanticKey2D, LocusEvaluation2D> cache;
	private final Map<String, Long> coherentRevisions = new LinkedHashMap<>();
	private final Set<LocusSemanticKey2D> activeKeys = new HashSet<>();
	private final Deque<LocusSemanticKey2D> activeStack = new ArrayDeque<>();
	private long hits;
	private long misses;
	private long evictions;
	private long cycles;

	/**
	 * @param memoizationEnabled whether exact semantic keys are cached
	 * @param maximumEntries positive bounded capacity
	 */
	public LocusEvaluationSession2D(boolean memoizationEnabled, int maximumEntries) {
		if (maximumEntries < 1) {
			throw new IllegalArgumentException("A semantic session must be bounded");
		}
		this.memoizationEnabled = memoizationEnabled;
		this.maximumEntries = maximumEntries;
		this.cache = new LinkedHashMap<>();
	}

	/** @return a bounded memoizing batch session */
	public static LocusEvaluationSession2D memoizing(int maximumEntries) {
		return new LocusEvaluationSession2D(true, maximumEntries);
	}

	/** @return a reference session with memoization disabled */
	public static LocusEvaluationSession2D reference() {
		return new LocusEvaluationSession2D(false, 1);
	}

	LocusEvaluation2D evaluate(LocusDefinition2D definition, LocusBranch2D branch,
			double canonicalParameter) {
		LocusSemanticKey2D key = new LocusSemanticKey2D(
				definition.getLocusIdentity(), definition.getSemanticRevision(),
				branch.getBranchKey(), canonicalParameter);
		Long coherentRevision = coherentRevisions.get(key.getLocusIdentity());
		if (coherentRevision != null
				&& coherentRevision.longValue() != key.getSemanticRevision()) {
			return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
					branch.getQuality(), "Incoherent semantic revisions in one session for "
							+ key.getLocusIdentity());
		}
		coherentRevisions.put(key.getLocusIdentity(), key.getSemanticRevision());

		if (memoizationEnabled) {
			LocusEvaluation2D cached = cache.get(key);
			if (cached != null) {
				hits++;
				definition.getInstrumentation().recordSessionHit();
				definition.getInstrumentation().recordDuplicateRequest();
				return cached;
			}
		}
		misses++;
		definition.getInstrumentation().recordSessionMiss();
		if (!activeKeys.add(key)) {
			cycles++;
			return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
					branch.getQuality(), "Semantic evaluator re-entry cycle: "
							+ cycleDiagnostic(key));
		}

		activeStack.push(key);
		LocusEvaluation2D result;
		try {
			result = definition.compute(branch, canonicalParameter, this);
		} finally {
			activeStack.pop();
			activeKeys.remove(key);
		}
		if (memoizationEnabled) {
			putBounded(key, result);
		}
		return result;
	}

	public boolean isMemoizationEnabled() {
		return memoizationEnabled;
	}

	public int getMaximumEntries() {
		return maximumEntries;
	}

	public int getCachedEntryCount() {
		return cache.size();
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

	public long getCycles() {
		return cycles;
	}

	public Map<String, Long> getCoherentRevisions() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(coherentRevisions));
	}

	private void putBounded(LocusSemanticKey2D key, LocusEvaluation2D value) {
		if (cache.size() >= maximumEntries) {
			LocusSemanticKey2D oldest = cache.keySet().iterator().next();
			cache.remove(oldest);
			evictions++;
		}
		cache.put(key, value);
	}

	private String cycleDiagnostic(LocusSemanticKey2D repeated) {
		StringBuilder diagnostic = new StringBuilder();
		for (LocusSemanticKey2D active : activeStack) {
			if (diagnostic.length() > 0) {
				diagnostic.append(" -> ");
			}
			diagnostic.append(active);
		}
		return diagnostic.append(" -> ").append(repeated).toString();
	}
}
