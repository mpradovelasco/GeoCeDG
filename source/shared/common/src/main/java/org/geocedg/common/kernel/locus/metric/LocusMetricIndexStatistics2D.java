/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Immutable bounded-index statistics. */
public final class LocusMetricIndexStatistics2D {
	private final long hits;
	private final long misses;
	private final long builds;
	private final long failedBuilds;
	private final long evictions;
	private final long invalidations;
	private final int retainedEntries;
	private final int activeBuilds;
	private final long approximateRetainedBytes;

	LocusMetricIndexStatistics2D(long hits, long misses, long builds,
			long failedBuilds, long evictions, long invalidations,
			int retainedEntries, int activeBuilds,
			long approximateRetainedBytes) {
		this.hits = hits;
		this.misses = misses;
		this.builds = builds;
		this.failedBuilds = failedBuilds;
		this.evictions = evictions;
		this.invalidations = invalidations;
		this.retainedEntries = retainedEntries;
		this.activeBuilds = activeBuilds;
		this.approximateRetainedBytes = approximateRetainedBytes;
	}

	public long getHits() {
		return hits;
	}

	public long getMisses() {
		return misses;
	}

	public long getBuilds() {
		return builds;
	}

	public long getFailedBuilds() {
		return failedBuilds;
	}

	public long getEvictions() {
		return evictions;
	}

	public long getInvalidations() {
		return invalidations;
	}

	public int getRetainedEntries() {
		return retainedEntries;
	}

	public int getActiveBuilds() {
		return activeBuilds;
	}

	public long getApproximateRetainedBytes() {
		return approximateRetainedBytes;
	}
}
