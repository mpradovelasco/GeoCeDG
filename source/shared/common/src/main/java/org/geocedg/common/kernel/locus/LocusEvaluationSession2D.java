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
import java.util.Objects;
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSessionDiagnostic2D.Kind;

/**
 * Bounded disposable context for one coherent semantic evaluation or batch.
 * It memoizes results but never owns dependency edges or semantic definitions.
 *
 * <p>PRE-G9B-CU1 ({@code locus-v2-semantics.md} v1.2): services that run
 * inside a reconstruction slice open slice-scoped child sessions. A child
 * shares the active-key stack (cycle detection and composition depth) and the
 * work budget of its root; nested evaluations are charged to the root, which
 * enforces its limit at its own evaluation boundaries, so a nested service is
 * never interrupted by its caller's limit. Memoization, revision coherence and
 * active keys are qualified by the slice scope.</p>
 */
public final class LocusEvaluationSession2D implements AutoCloseable {
	/** Shared stack-safe ceiling for nested semantic evaluator composition. */
	public static final int MAXIMUM_SAFE_ACTIVE_DEPTH = 128;

	private static final Deque<ServiceFrame> SERVICE_FRAMES = new ArrayDeque<>();
	private static long nextSliceScope = 1;

	private final Core core;
	private final long scope;
	private final boolean memoizationEnabled;
	private final int maximumEntries;
	private final Map<LocusSemanticKey2D, LocusEvaluation2D> cache;
	private final Map<String, Long> coherentRevisions = new LinkedHashMap<>();
	private int ownActiveDepth;
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
		this(new Core(validatedMisses(maximumMisses),
				validatedDepth(maximumActiveDepth)), 0, memoizationEnabled,
				maximumEntries);
	}

	private LocusEvaluationSession2D(Core core, long scope,
			boolean memoizationEnabled, int maximumEntries) {
		if (maximumEntries < 1) {
			throw new IllegalArgumentException("A semantic session must be bounded");
		}
		this.core = core;
		this.scope = scope;
		this.memoizationEnabled = memoizationEnabled;
		this.maximumEntries = maximumEntries;
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

	/**
	 * Opens the session of a kernel service (metric, position binding,
	 * intersection). Inside a reconstruction-slice update it is a slice-scoped
	 * child of the evaluating session; otherwise it is a new root.
	 *
	 * @param memoize whether the service memoizes exact keys
	 * @param maximumEntries memo capacity when memoizing
	 * @return child or root session; the caller closes it
	 */
	public static LocusEvaluationSession2D openServiceSession(boolean memoize,
			int maximumEntries) {
		ServiceFrame frame = currentFrame();
		int entries = memoize ? maximumEntries : 1;
		if (frame == null) {
			return new LocusEvaluationSession2D(memoize, entries);
		}
		return new LocusEvaluationSession2D(frame.parent.core, frame.scope,
				memoize, entries);
	}

	/** @return a new unique slice scope for one evaluator instance */
	public static synchronized long newSliceScope() {
		return nextSliceScope++;
	}

	/**
	 * Runs one slice update with {@code parent} as the parent of the service
	 * sessions opened on this thread for {@code scope}.
	 */
	public static void runWithNestedServiceParent(LocusEvaluationSession2D parent,
			long scope, Runnable work) {
		Objects.requireNonNull(parent);
		Objects.requireNonNull(work);
		ServiceFrame frame = new ServiceFrame(parent, scope, Thread.currentThread());
		synchronized (SERVICE_FRAMES) {
			SERVICE_FRAMES.push(frame);
		}
		try {
			work.run();
		} finally {
			synchronized (SERVICE_FRAMES) {
				SERVICE_FRAMES.remove(frame);
			}
		}
	}

	private static ServiceFrame currentFrame() {
		Thread current = Thread.currentThread();
		synchronized (SERVICE_FRAMES) {
			for (ServiceFrame frame : SERVICE_FRAMES) {
				if (frame.thread == current) {
					return frame.parent.closed ? null : frame;
				}
			}
		}
		return null;
	}

	LocusEvaluation2D evaluate(LocusDefinition2D definition, LocusBranch2D branch,
			double canonicalParameter) {
		if (ownActiveDepth == 0) {
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
		if (scope == 0 && core.totalMisses >= core.maximumMisses) {
			throw new EvaluationWorkLimitException(core.maximumMisses);
		}
		if (core.activeStack.size() >= core.maximumActiveDepth) {
			throw EvaluationWorkLimitException.forDepth(core.maximumActiveDepth);
		}
		misses++;
		core.totalMisses++;
		if (scope != 0) {
			core.nestedMisses++;
		}
		definition.getInstrumentation().recordSessionMiss();
		ScopedKey active = new ScopedKey(scope, key);
		if (!core.activeKeys.add(active)) {
			cycles++;
			lastDiagnostic = diagnostic(Kind.CYCLE_REENTRY,
					"Semantic evaluator re-entry cycle", key);
			return LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
					branch.getQuality(), lastDiagnostic.toString());
		}

		core.activeStack.push(active);
		ownActiveDepth++;
		LocusEvaluation2D result;
		try {
			result = definition.compute(branch, canonicalParameter, this);
		} finally {
			ownActiveDepth--;
			core.activeStack.pop();
			core.activeKeys.remove(active);
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

	/** @return misses of this session; nested child misses are not included */
	public long getMisses() {
		return misses;
	}

	/**
	 * @return misses issued by slice-scoped child sessions sharing this
	 *         session's root, charged to the root budget
	 */
	public long getNestedMisses() {
		return core.nestedMisses;
	}

	/** @return all misses charged to the root budget of this session */
	public long getTotalChargedMisses() {
		return core.totalMisses;
	}

	/** @return whether this is a slice-scoped child session */
	public boolean isNestedServiceSession() {
		return scope != 0;
	}

	public long getEvictions() {
		return evictions;
	}

	public long getCycles() {
		return cycles;
	}

	/** @return number of currently active semantic keys of this session */
	public int getActiveDepth() {
		return ownActiveDepth;
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
		if (ownActiveDepth != 0) {
			throw new IllegalStateException("Cannot clear an active semantic session");
		}
		cache.clear();
		coherentRevisions.clear();
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
		java.util.Iterator<ScopedKey> iterator =
				core.activeStack.descendingIterator();
		while (iterator.hasNext()) {
			path.add(iterator.next().key);
		}
		path.add(repeated);
		return new LocusSessionDiagnostic2D(kind, message, path);
	}

	private static long validatedMisses(long maximumMisses) {
		if (maximumMisses < 1) {
			throw new IllegalArgumentException(
					"A semantic session work limit must be positive");
		}
		return maximumMisses;
	}

	private static int validatedDepth(int maximumActiveDepth) {
		if (maximumActiveDepth < 1) {
			throw new IllegalArgumentException(
					"A semantic session depth limit must be positive");
		}
		return maximumActiveDepth;
	}

	/** State shared by one root session and all its slice-scoped children. */
	private static final class Core {
		private final long maximumMisses;
		private final int maximumActiveDepth;
		private final Set<ScopedKey> activeKeys = new HashSet<>();
		private final Deque<ScopedKey> activeStack = new ArrayDeque<>();
		private long totalMisses;
		private long nestedMisses;

		private Core(long maximumMisses, int maximumActiveDepth) {
			this.maximumMisses = maximumMisses;
			this.maximumActiveDepth = maximumActiveDepth;
		}
	}

	private static final class ScopedKey {
		private final long scope;
		private final LocusSemanticKey2D key;

		private ScopedKey(long scope, LocusSemanticKey2D key) {
			this.scope = scope;
			this.key = key;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ScopedKey)) {
				return false;
			}
			ScopedKey scoped = (ScopedKey) other;
			return scope == scoped.scope && key.equals(scoped.key);
		}

		@Override
		public int hashCode() {
			return Objects.hash(scope, key);
		}
	}

	private static final class ServiceFrame {
		private final LocusEvaluationSession2D parent;
		private final long scope;
		private final Thread thread;

		private ServiceFrame(LocusEvaluationSession2D parent, long scope,
				Thread thread) {
			this.parent = parent;
			this.scope = scope;
			this.thread = thread;
		}
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
