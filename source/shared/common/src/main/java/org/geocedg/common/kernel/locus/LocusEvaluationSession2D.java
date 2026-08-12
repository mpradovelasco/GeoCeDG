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
import org.geocedg.common.kernel.locus.LocusSessionDiagnostic2D.Kind;

/**
 * Bounded disposable context for one coherent semantic evaluation or batch.
 * It memoizes results but never owns dependency edges or semantic definitions.
 */
public final class LocusEvaluationSession2D implements AutoCloseable {
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
	private boolean closed;
	private LocusSessionDiagnostic2D lastDiagnostic =
			LocusSessionDiagnostic2D.none();

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
		if (activeStack.isEmpty()) {
			lastDiagnostic = LocusSessionDiagnostic2D.none();
		}
		LocusSemanticKey2D key = new LocusSemanticKey2D(
				definition.getLocusIdentity(), definition.getSemanticRevision(),
				branch.getBranchKey(), canonicalParameter);
		if (closed) {
			lastDiagnostic = diagnostic(Kind.CLOSED_SESSION,
					"A disposed semantic evaluation session cannot be reused", key);
			return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
					branch.getQuality(), lastDiagnostic.toString());
		}
		Long coherentRevision = coherentRevisions.get(key.getLocusIdentity());
		if (coherentRevision != null
				&& coherentRevision.longValue() != key.getSemanticRevision()) {
			lastDiagnostic = diagnostic(Kind.INCOHERENT_REVISION,
					"One session cannot mix semantic revisions for "
							+ key.getLocusIdentity(), key);
			return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
					branch.getQuality(), lastDiagnostic.toString());
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
			lastDiagnostic = diagnostic(Kind.CYCLE_REENTRY,
					"Semantic evaluator re-entry cycle", key);
			return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
					branch.getQuality(), lastDiagnostic.toString());
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

	/** @return number of currently active semantic keys, normally zero */
	public int getActiveDepth() {
		return activeStack.size();
	}

	/** @return most recent typed session-level diagnostic */
	public LocusSessionDiagnostic2D getLastDiagnostic() {
		return lastDiagnostic;
	}

	public boolean isClosed() {
		return closed;
	}

	public Map<String, Long> getCoherentRevisions() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(coherentRevisions));
	}

	/**
	 * Drops memoized values and revision observations while retaining counters.
	 * Active evaluation cannot be cleared because that would hide re-entry.
	 */
	public void clear() {
		if (!activeStack.isEmpty()) {
			throw new IllegalStateException("Cannot clear an active semantic session");
		}
		cache.clear();
		coherentRevisions.clear();
		activeKeys.clear();
		lastDiagnostic = LocusSessionDiagnostic2D.none();
	}

	/** Disposes this kernel-thread-confined session and releases all revisions. */
	@Override
	public void close() {
		if (!closed) {
			clear();
			closed = true;
		}
	}

	private void putBounded(LocusSemanticKey2D key, LocusEvaluation2D value) {
		if (cache.size() >= maximumEntries) {
			LocusSemanticKey2D oldest = cache.keySet().iterator().next();
			cache.remove(oldest);
			evictions++;
		}
		cache.put(key, value);
	}

	private LocusSessionDiagnostic2D diagnostic(Kind kind, String message,
			LocusSemanticKey2D repeated) {
		java.util.ArrayList<LocusSemanticKey2D> path = new java.util.ArrayList<>();
		java.util.Iterator<LocusSemanticKey2D> iterator =
				activeStack.descendingIterator();
		while (iterator.hasNext()) {
			path.add(iterator.next());
		}
		path.add(repeated);
		return new LocusSessionDiagnostic2D(kind, message, path);
	}
}
