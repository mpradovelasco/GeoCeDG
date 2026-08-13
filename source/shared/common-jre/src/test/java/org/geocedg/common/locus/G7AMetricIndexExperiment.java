/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Test-private implementation of the three G7A index alternatives. */
final class G7AMetricIndexExperiment {

	enum Strategy {
		REFERENCE_NO_INDEX_REUSE,
		EAGER_WHOLE_REVISION,
		LAZY_COMPONENT_REVISION
	}

	record Policy(String metricAlgorithmVersion, String metricPolicyVersion,
			String metricTolerancePolicy, String multiplicityPolicy,
			String improperLimitPolicy) {
		Policy {
			Objects.requireNonNull(metricAlgorithmVersion);
			Objects.requireNonNull(metricPolicyVersion);
			Objects.requireNonNull(metricTolerancePolicy);
			Objects.requireNonNull(multiplicityPolicy);
			Objects.requireNonNull(improperLimitPolicy);
		}
	}

	record IndexKey(String locusIdentity, long semanticRevision,
			String branchKey, String resolvedValidComponentKey,
			String providerEvaluatorCapabilityVersion,
			String metricAlgorithmVersion, String metricPolicyVersion,
			String metricTolerancePolicy, String multiplicityPolicy,
			String improperLimitPolicy) {
		IndexKey {
			Objects.requireNonNull(locusIdentity);
			Objects.requireNonNull(branchKey);
			Objects.requireNonNull(resolvedValidComponentKey);
			Objects.requireNonNull(providerEvaluatorCapabilityVersion);
			Objects.requireNonNull(metricAlgorithmVersion);
			Objects.requireNonNull(metricPolicyVersion);
			Objects.requireNonNull(metricTolerancePolicy);
			Objects.requireNonNull(multiplicityPolicy);
			Objects.requireNonNull(improperLimitPolicy);
		}
	}

	record ComponentIndex(IndexKey key, double totalLength,
			long approximateBytes, List<Double> arcCoordinates) {
		ComponentIndex {
			arcCoordinates = List.copyOf(arcCoordinates);
		}
	}

	static final class Counters {
		private long queries;
		private long componentBuilds;
		private long indexBuilds;
		private long integratorCalls;
		private long evaluatorCalls;
		private long derivativeCalls;
		private long refinements;
		private long reusedIntervals;
		private long hits;
		private long misses;
		private long evictions;
		private long invalidations;
		private long failedBuilds;
		private long activeBuilds;
		private long maximumActiveBuilds;

		long queries() {
			return queries;
		}

		long componentBuilds() {
			return componentBuilds;
		}

		long indexBuilds() {
			return indexBuilds;
		}

		long integratorCalls() {
			return integratorCalls;
		}

		long evaluatorCalls() {
			return evaluatorCalls;
		}

		long derivativeCalls() {
			return derivativeCalls;
		}

		long refinements() {
			return refinements;
		}

		long reusedIntervals() {
			return reusedIntervals;
		}

		long hits() {
			return hits;
		}

		long misses() {
			return misses;
		}

		long evictions() {
			return evictions;
		}

		long invalidations() {
			return invalidations;
		}

		long failedBuilds() {
			return failedBuilds;
		}

		long activeBuilds() {
			return activeBuilds;
		}

		long maximumActiveBuilds() {
			return maximumActiveBuilds;
		}
	}

	private final Strategy strategy;
	private final int capacity;
	private final String locusIdentity;
	private final String branchKey;
	private final String capabilityVersion;
	private final List<String> componentKeys;
	private final Map<IndexKey, ComponentIndex> retained =
			new LinkedHashMap<>();
	private final Counters counters = new Counters();
	private boolean failNextBuild;

	G7AMetricIndexExperiment(Strategy strategy, int capacity,
			String locusIdentity, String branchKey, String capabilityVersion,
			List<String> componentKeys) {
		this.strategy = Objects.requireNonNull(strategy);
		this.capacity = capacity;
		this.locusIdentity = Objects.requireNonNull(locusIdentity);
		this.branchKey = Objects.requireNonNull(branchKey);
		this.capabilityVersion = Objects.requireNonNull(capabilityVersion);
		this.componentKeys = List.copyOf(componentKeys);
		if (capacity < 0 || componentKeys.isEmpty()) {
			throw new IllegalArgumentException("Invalid index experiment");
		}
	}

	double query(long revision, String componentKey, Policy policy) {
		counters.queries++;
		IndexKey key = key(revision, componentKey, policy);
		if (strategy == Strategy.REFERENCE_NO_INDEX_REUSE || capacity == 0) {
			counters.misses++;
			return build(key).totalLength();
		}
		ComponentIndex found = retained.get(key);
		if (found != null) {
			counters.hits++;
			counters.reusedIntervals += found.arcCoordinates().size() - 1L;
			return found.totalLength();
		}
		counters.misses++;
		if (strategy == Strategy.EAGER_WHOLE_REVISION) {
			for (String candidate : componentKeys) {
				IndexKey candidateKey = key(revision, candidate, policy);
				if (!retained.containsKey(candidateKey)) {
					publish(build(candidateKey));
				}
			}
		} else {
			publish(build(key));
		}
		return retained.get(key).totalLength();
	}

	double queryTotal(long revision, Policy policy) {
		double total = 0;
		for (String componentKey : componentKeys) {
			total += query(revision, componentKey, policy);
		}
		return total;
	}

	void invalidateLocusRevision(long obsoleteRevision) {
		int before = retained.size();
		retained.entrySet().removeIf(entry ->
				entry.getKey().locusIdentity().equals(locusIdentity)
						&& entry.getKey().semanticRevision() == obsoleteRevision);
		if (retained.size() != before) {
			counters.invalidations++;
		}
	}

	void invalidateAll() {
		if (!retained.isEmpty()) {
			retained.clear();
			counters.invalidations++;
		}
	}

	void failNextBuild() {
		failNextBuild = true;
	}

	Counters counters() {
		return counters;
	}

	Strategy strategy() {
		return strategy;
	}

	int retainedEntries() {
		return retained.size();
	}

	long approximateRetainedBytes() {
		return retained.values().stream()
				.mapToLong(ComponentIndex::approximateBytes).sum();
	}

	List<IndexKey> retainedKeys() {
		return List.copyOf(retained.keySet());
	}

	private IndexKey key(long revision, String componentKey, Policy policy) {
		if (!componentKeys.contains(componentKey)) {
			throw new IllegalArgumentException("Unknown component");
		}
		return new IndexKey(locusIdentity, revision, branchKey, componentKey,
				capabilityVersion, policy.metricAlgorithmVersion(),
				policy.metricPolicyVersion(), policy.metricTolerancePolicy(),
				policy.multiplicityPolicy(), policy.improperLimitPolicy());
	}

	private ComponentIndex build(IndexKey key) {
		counters.activeBuilds++;
		counters.maximumActiveBuilds = Math.max(counters.maximumActiveBuilds,
				counters.activeBuilds);
		try {
			counters.componentBuilds++;
			counters.indexBuilds++;
			counters.integratorCalls += 8;
			counters.evaluatorCalls += 33;
			counters.derivativeCalls += 33;
			counters.refinements += 16;
			if (failNextBuild) {
				failNextBuild = false;
				counters.failedBuilds++;
				throw new IllegalStateException("injected component build failure");
			}
			int ordinal = componentKeys.indexOf(
					key.resolvedValidComponentKey());
			double length = ordinal + 1;
			List<Double> arcCoordinates = new ArrayList<>();
			for (int index = 0; index <= 16; index++) {
				arcCoordinates.add(length * index / 16);
			}
			return new ComponentIndex(key, length, 512, arcCoordinates);
		} finally {
			counters.activeBuilds--;
		}
	}

	private void publish(ComponentIndex entry) {
		while (retained.size() >= capacity && !retained.isEmpty()) {
			IndexKey oldest = retained.keySet().iterator().next();
			retained.remove(oldest);
			counters.evictions++;
		}
		retained.put(entry.key(), entry);
	}
}
