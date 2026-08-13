/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Non-GeoElement per-source owner sharing only immutable component metric state.
 */
public final class LocusMetricSharedOwner2D {
	public static final int PROVISIONAL_CAPACITY = 64;

	private final String locusIdentity;
	private final Thread ownerThread;
	private final LocusMetricInstrumentation2D instrumentation;
	private final LocusMetricIndex2D index;
	private final Map<LocusMetricIndexKey2D, String> firstConsumers =
			new LinkedHashMap<>();
	private int activeLeases;
	private boolean released;
	private boolean sourceRemoved;

	/** Creates the dedicated owner for one source locus. */
	public LocusMetricSharedOwner2D(String locusIdentity,
			LocusMetricInstrumentation2D instrumentation) {
		if (locusIdentity == null || locusIdentity.trim().isEmpty()) {
			throw new IllegalArgumentException("Owner locus identity is required");
		}
		this.locusIdentity = locusIdentity;
		this.ownerThread = Thread.currentThread();
		this.instrumentation = Objects.requireNonNull(instrumentation);
		this.index = new LocusMetricIndex2D(PROVISIONAL_CAPACITY,
				instrumentation);
	}

	/**
	 * Acquires one direct metric-consumer lease.
	 *
	 * @return active owner lease
	 */
	public LocusMetricOwnerLease2D acquireLease() {
		assertThread();
		if (released || sourceRemoved) {
			throw new IllegalStateException("Metric owner is no longer active");
		}
		activeLeases++;
		return new Lease(this);
	}

	/**
	 * Gets or builds only an immutable component-level state.
	 *
	 * @return retained or privately built component state
	 */
	public LocusMetricComponentState2D getOrBuildComponentState(
			LocusMetricIndexKey2D key,
			LocusMetricComponentStateBuilder2D builder) {
		return getOrBuildComponentState(key, builder, "");
	}

	/**
	 * Gets/builds state while recording cross-result reuse by stable consumer
	 * token. The token is instrumentation only and owns no dependency edge.
	 *
	 * @return retained or privately built component state
	 */
	public LocusMetricComponentState2D getOrBuildComponentState(
			LocusMetricIndexKey2D key,
			LocusMetricComponentStateBuilder2D builder,
			String consumerToken) {
		requireActive();
		if (!locusIdentity.equals(key.getLocusIdentity())) {
			throw new IllegalArgumentException(
					"Different loci cannot share one metric owner");
		}
		boolean retained = index.contains(key);
		String firstConsumer = firstConsumers.get(key);
		LocusMetricComponentState2D state =
				index.getOrBuildComponentState(key, builder);
		if (retained && firstConsumer != null
				&& !firstConsumer.equals(consumerToken)) {
			instrumentation.recordCrossResultHit();
		}
		if (!retained && index.contains(key)) {
			if (firstConsumers.size() >= PROVISIONAL_CAPACITY) {
				Iterator<LocusMetricIndexKey2D> iterator =
						firstConsumers.keySet().iterator();
				iterator.next();
				iterator.remove();
			}
			firstConsumers.put(key, consumerToken);
		}
		return state;
	}

	/** Synchronously drops old semantic revisions. */
	public void invalidateObsoleteRevision(long currentRevision) {
		assertThread();
		if (!released && !sourceRemoved) {
			index.invalidateObsoleteRevision(locusIdentity, currentRevision);
			firstConsumers.entrySet().removeIf(entry ->
					entry.getKey().getSemanticRevision() != currentRevision);
		}
	}

	/** Synchronously drops all source state while retaining active leases. */
	public void clear() {
		assertThread();
		if (!released && !sourceRemoved) {
			index.clear();
			firstConsumers.clear();
		}
	}

	/** Releases source-owned state before normal dependent-algorithm removal. */
	public void releaseSource() {
		assertThread();
		if (!sourceRemoved) {
			index.clear();
			firstConsumers.clear();
			sourceRemoved = true;
			released = true;
		}
	}

	/** @return immutable bounded-index statistics */
	public LocusMetricIndexStatistics2D statistics() {
		assertThread();
		return index.statistics();
	}

	/** @return owner-scoped functional instrumentation */
	public LocusMetricInstrumentation2D getInstrumentation() {
		assertThread();
		return instrumentation;
	}

	/** @return number of direct metric consumers retaining the owner */
	public int getActiveLeaseCount() {
		assertThread();
		return activeLeases;
	}

	/** @return whether source removal or the last lease released this owner */
	public boolean isReleased() {
		assertThread();
		return released;
	}

	public String getLocusIdentity() {
		return locusIdentity;
	}

	private void releaseLease() {
		assertThread();
		if (activeLeases < 1) {
			throw new IllegalStateException("Metric owner lease already released");
		}
		activeLeases--;
		if (activeLeases == 0 && !released) {
			index.clear();
			firstConsumers.clear();
			released = true;
		}
	}

	private void requireActive() {
		assertThread();
		if (released || sourceRemoved || activeLeases < 1) {
			throw new IllegalStateException(
					"Component state requires an active metric-consumer lease");
		}
	}

	private void assertThread() {
		if (Thread.currentThread() != ownerThread) {
			throw new IllegalStateException(
					"Metric owner is confined to its creating kernel thread");
		}
	}

	private static final class Lease implements LocusMetricOwnerLease2D {
		private LocusMetricSharedOwner2D owner;

		private Lease(LocusMetricSharedOwner2D owner) {
			this.owner = owner;
		}

		@Override
		public LocusMetricSharedOwner2D getOwner() {
			if (owner == null) {
				throw new IllegalStateException("Metric owner lease is closed");
			}
			owner.assertThread();
			return owner;
		}

		@Override
		public void close() {
			if (owner != null) {
				owner.releaseLease();
				owner = null;
			}
		}
	}
}
