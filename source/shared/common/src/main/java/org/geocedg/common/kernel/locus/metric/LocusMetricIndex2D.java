/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Bounded insertion-order component-state index confined to its kernel thread.
 */
public final class LocusMetricIndex2D {
	private final int capacity;
	private final Thread ownerThread;
	private final LocusMetricInstrumentation2D instrumentation;
	private final Map<LocusMetricIndexKey2D, LocusMetricComponentState2D> entries =
			new LinkedHashMap<>();
	/**
	 * PRE-G9B-CU1 ({@code locus-v2-metrics.md} section 25.7): deterministic failures
	 * memoized under their complete key, so the same key is never rebuilt until it
	 * changes. A failure caused by a caller's session limit is never stored.
	 */
	private final Map<LocusMetricIndexKey2D, Object> failures =
			new LinkedHashMap<>();
	private final Set<LocusMetricIndexKey2D> activeBuilds = new HashSet<>();
	private long hits;
	private long misses;
	private long builds;
	private long failedBuilds;
	private long evictions;
	private long invalidations;

	/** Creates a bounded current-revision index. */
	public LocusMetricIndex2D(int capacity,
			LocusMetricInstrumentation2D instrumentation) {
		if (capacity < 1) {
			throw new IllegalArgumentException("Metric index capacity must be positive");
		}
		this.capacity = capacity;
		this.instrumentation = Objects.requireNonNull(instrumentation);
		this.ownerThread = Thread.currentThread();
	}

	/**
	 * Returns or atomically publishes one immutable complete component state.
	 *
	 * @return retained or privately built component state
	 */
	public LocusMetricComponentState2D getOrBuildComponentState(
			LocusMetricIndexKey2D key,
			LocusMetricComponentStateBuilder2D builder) {
		assertThread();
		Objects.requireNonNull(key);
		Objects.requireNonNull(builder);
		LocusMetricComponentState2D cached = entries.get(key);
		if (cached != null) {
			hits++;
			instrumentation.recordIndexHit();
			return cached;
		}
		Object failure = failures.get(key);
		if (failure != null) {
			hits++;
			instrumentation.recordIndexHit();
			if (failure instanceof LocusMetricComponentBuildException) {
				throw (LocusMetricComponentBuildException) failure;
			}
			return (LocusMetricComponentState2D) failure;
		}
		misses++;
		instrumentation.recordIndexMiss();
		if (!activeBuilds.add(key)) {
			throw new IllegalStateException(
					"Recursive component-state build for the same complete key");
		}
		instrumentation.recordComponentStateBuildStarted();
		try {
			LocusMetricComponentState2D candidate =
					Objects.requireNonNull(builder.buildComponentState(key));
			if (!key.equals(candidate.getIndexKey())) {
				throw new IllegalArgumentException(
						"Built component state does not match its complete key");
			}
			if (candidate.getComputationStatus()
					!= MetricComputationStatus.SUCCESS) {
				failedBuilds++;
				instrumentation.recordComponentStateBuildFailure();
				rememberFailure(key, candidate);
				return candidate;
			}
			if (entries.size() >= capacity) {
				Iterator<LocusMetricIndexKey2D> iterator =
						entries.keySet().iterator();
				iterator.next();
				iterator.remove();
				evictions++;
				instrumentation.recordEviction();
			}
			entries.put(key, candidate);
			builds++;
			return candidate;
		} catch (LocusMetricComponentBuildException exception) {
			failedBuilds++;
			instrumentation.recordComponentStateBuildFailure();
			rememberFailure(key, exception);
			throw exception;
		} catch (RuntimeException exception) {
			failedBuilds++;
			instrumentation.recordComponentStateBuildFailure();
			throw exception;
		} finally {
			activeBuilds.remove(key);
			instrumentation.recordComponentStateBuildFinished();
		}
	}

	/** Removes every obsolete revision while retaining the supplied revision. */
	public void invalidateObsoleteRevision(String locusIdentity,
			long currentRevision) {
		assertThread();
		long before = entries.size();
		entries.entrySet().removeIf(entry ->
				entry.getKey().getLocusIdentity().equals(locusIdentity)
						&& entry.getKey().getSemanticRevision() != currentRevision);
		failures.keySet().removeIf(key ->
				key.getLocusIdentity().equals(locusIdentity)
						&& key.getSemanticRevision() != currentRevision);
		recordInvalidation(before - entries.size());
	}

	/** Removes all state for one locus identity. */
	public void invalidateLocus(String locusIdentity) {
		assertThread();
		long before = entries.size();
		entries.entrySet().removeIf(entry ->
				entry.getKey().getLocusIdentity().equals(locusIdentity));
		failures.keySet().removeIf(key ->
				key.getLocusIdentity().equals(locusIdentity));
		recordInvalidation(before - entries.size());
	}

	/** Releases all entries; active private builds are never published. */
	public void clear() {
		assertThread();
		long removed = entries.size();
		entries.clear();
		failures.clear();
		recordInvalidation(removed);
	}

	/** @return whether a deterministic failure is memoized for the key */
	public boolean containsFailure(LocusMetricIndexKey2D key) {
		assertThread();
		return failures.containsKey(key);
	}

	private void rememberFailure(LocusMetricIndexKey2D key, Object failure) {
		if (failures.size() >= capacity) {
			Iterator<LocusMetricIndexKey2D> iterator = failures.keySet().iterator();
			iterator.next();
			iterator.remove();
		}
		failures.put(key, failure);
	}

	public int getCapacity() {
		return capacity;
	}

	/** @return whether the complete key is currently retained */
	public boolean contains(LocusMetricIndexKey2D key) {
		assertThread();
		return entries.containsKey(key);
	}

	/** @return immutable current index statistics */
	public LocusMetricIndexStatistics2D statistics() {
		assertThread();
		long retainedBytes = 0;
		for (LocusMetricComponentState2D state : entries.values()) {
			retainedBytes += state.getApproximateRetainedBytes();
		}
		return new LocusMetricIndexStatistics2D(hits, misses, builds,
				failedBuilds, evictions, invalidations, entries.size(),
				activeBuilds.size(), retainedBytes);
	}

	private void recordInvalidation(long removed) {
		if (removed > 0) {
			invalidations += removed;
			instrumentation.recordInvalidation(removed);
		}
	}

	private void assertThread() {
		if (Thread.currentThread() != ownerThread) {
			throw new IllegalStateException(
					"Metric index is confined to its creating kernel thread");
		}
	}
}
