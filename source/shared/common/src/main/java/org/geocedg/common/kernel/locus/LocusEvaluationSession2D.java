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
	/** Shared stack-safe ceiling for nested semantic evaluator composition. */
	public static final int MAXIMUM_SAFE_ACTIVE_DEPTH = 128;

	private final boolean memoizationEnabled;
	private final int maximumEntries;
	private final long maximumMisses;
	private final int maximumActiveDepth;
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
		this(memoizationEnabled, maximumEntries, Long.MAX_VALUE,
				MAXIMUM_SAFE_ACTIVE_DEPTH);
	}

	private LocusEvaluationSession2D(boolean memoizationEnabled,
			int maximumEntries, long maximumMisses, int maximumActiveDepth) {
		if (maximumEntries < 1) {
			throw new IllegalArgumentException("A semantic session must be bounded");
		}
		if (maximumMisses < 1) {
			throw new IllegalArgumentException(
					"A semantic session work limit must be positive");
		}
		if (maximumActiveDepth < 1) {
			throw new IllegalArgumentException(
					"A semantic session depth limit must be positive");
		}
		this.memoizationEnabled = memoizationEnabled;
		this.maximumEntries = maximumEntries;
		this.maximumMisses = maximumMisses;
		this.maximumActiveDepth = maximumActiveDepth;
		this.cache = new LinkedHashMap<>();
	}

	/** @return a bounded memoizing batch session */
	public static LocusEvaluationSession2D memoizing(int maximumEntries) {
		return new LocusEvaluationSession2D(true, maximumEntries);
	}

	/**
	 * Creates a memoizing session whose complete nested evaluator work is bounded.
	 * Every cache miss, including one issued by a composed semantic evaluator,
	 * consumes this limit.
	 *
	 * @return bounded semantic session
	 */
	public static LocusEvaluationSession2D memoizingWithMissLimit(
			int maximumEntries, long maximumMisses) {
		return new LocusEvaluationSession2D(true, maximumEntries, maximumMisses,
				MAXIMUM_SAFE_ACTIVE_DEPTH);
	}

	/** @return memoizing session with explicit nested-evaluator depth limit */
	public static LocusEvaluationSession2D memoizingWithLimits(
			int maximumEntries, long maximumMisses, int maximumActiveDepth) {
		return new LocusEvaluationSession2D(true, maximumEntries, maximumMisses,
				maximumActiveDepth);
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
		if (misses >= maximumMisses) {
			throw new EvaluationWorkLimitException(maximumMisses);
		}
		if (activeStack.size() >= maximumActiveDepth) {
			throw EvaluationWorkLimitException.forDepth(maximumActiveDepth);
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

	/** Typed hard stop for a caller-supplied semantic evaluation work limit. */
	public static final class EvaluationWorkLimitException
			extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private EvaluationWorkLimitException(long maximumMisses) {
			super("Semantic evaluation work limit exhausted at " + maximumMisses
					+ " cache misses");
		}

		private EvaluationWorkLimitException(String message) {
			super(message);
		}

		private static EvaluationWorkLimitException forDepth(int maximumDepth) {
			return new EvaluationWorkLimitException(
					"Semantic evaluator composition depth exhausted at "
							+ maximumDepth + " active definitions");
		}
	}
}
