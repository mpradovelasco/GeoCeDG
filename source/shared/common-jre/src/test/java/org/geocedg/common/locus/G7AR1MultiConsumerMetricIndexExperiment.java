/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Test-private ownership alternatives for the focused G7A-R1 review. */
final class G7AR1MultiConsumerMetricIndexExperiment {

	static final long COMPONENT_PAYLOAD_BYTES = 512;
	static final long ENTRY_METADATA_BYTES = 192;
	static final long OWNER_OVERHEAD_BYTES = 256;
	static final long CONSUMER_OVERHEAD_BYTES = 128;

	enum Strategy {
		ALGO_LOCAL_INDEX,
		LOCUS_ATTACHED_SHARED_INDEX,
		CONSTRUCTION_SCOPED_METRIC_REPOSITORY,
		DEDICATED_SHARED_OWNER
	}

	record WorkBudgetKey(long maximumEvaluations, long maximumSubdivisions,
			int maximumDepth) {
		WorkBudgetKey {
			if (maximumEvaluations < 1 || maximumSubdivisions < 1
					|| maximumDepth < 1) {
				throw new IllegalArgumentException("Invalid work-budget key");
			}
		}
	}

	record Policy(String capabilityVersion, String metricAlgorithmVersion,
			String policyVersion, String toleranceVersion,
			String multiplicityPolicy, String improperLimitPolicy,
			WorkBudgetKey workBudget) {
		Policy {
			Objects.requireNonNull(capabilityVersion);
			Objects.requireNonNull(metricAlgorithmVersion);
			Objects.requireNonNull(policyVersion);
			Objects.requireNonNull(toleranceVersion);
			Objects.requireNonNull(multiplicityPolicy);
			Objects.requireNonNull(improperLimitPolicy);
			Objects.requireNonNull(workBudget);
		}
	}

	record FullKey(String locusIdentity, long semanticRevision,
			String branchKey, String resolvedComponentKey,
			String capabilityVersion, String metricAlgorithmVersion,
			String policyVersion, String toleranceVersion,
			String multiplicityPolicy, String improperLimitPolicy,
			WorkBudgetKey workBudget) {
		FullKey {
			Objects.requireNonNull(locusIdentity);
			Objects.requireNonNull(branchKey);
			Objects.requireNonNull(resolvedComponentKey);
			Objects.requireNonNull(capabilityVersion);
			Objects.requireNonNull(metricAlgorithmVersion);
			Objects.requireNonNull(policyVersion);
			Objects.requireNonNull(toleranceVersion);
			Objects.requireNonNull(multiplicityPolicy);
			Objects.requireNonNull(improperLimitPolicy);
			Objects.requireNonNull(workBudget);
		}
	}

	record LocusMetricComponentState2D(FullKey key, double componentLength,
			List<Double> cumulativeArcCoordinates, String builderConsumer,
			long payloadBytes) {
		LocusMetricComponentState2D {
			Objects.requireNonNull(key);
			cumulativeArcCoordinates = List.copyOf(cumulativeArcCoordinates);
			Objects.requireNonNull(builderConsumer);
		}
	}

	record LocusMetricRouteSegment2D(String componentKey, double fromFraction,
			double toFraction) {
		LocusMetricRouteSegment2D {
			Objects.requireNonNull(componentKey);
			if (fromFraction < 0 || fromFraction > 1 || toFraction < 0
					|| toFraction > 1) {
				throw new IllegalArgumentException(
						"Route-segment fractions must belong to [0, 1]");
			}
		}
	}

	record LocusMetricContribution2D(String consumerIdentity,
			String componentKey, double value, boolean completeComponentExtent) {
	}

	record MemoryStats(long uniquePayloadBytes, long duplicatePayloadBytes,
			long entryMetadataBytes, long ownerOverheadBytes,
			long consumerOverheadBytes, long totalRetainedBytes) {
	}

	static final class Counters {
		private long queries;
		private long componentBuilds;
		private long integratorCalls;
		private long evaluatorCalls;
		private long derivativeCalls;
		private long refinements;
		private long hits;
		private long misses;
		private long crossResultHits;
		private long duplicateCompatibleBuilds;
		private long invalidations;
		private long evictions;
		private long renderReads;
		private long legacySampleReads;
		private long wholeLocusRegenerations;
		private long indexBuildsInsideDownstreamPoint;

		long queries() {
			return queries;
		}

		long componentBuilds() {
			return componentBuilds;
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

		long hits() {
			return hits;
		}

		long misses() {
			return misses;
		}

		long crossResultHits() {
			return crossResultHits;
		}

		long duplicateCompatibleBuilds() {
			return duplicateCompatibleBuilds;
		}

		long invalidations() {
			return invalidations;
		}

		long evictions() {
			return evictions;
		}

		long renderReads() {
			return renderReads;
		}

		long legacySampleReads() {
			return legacySampleReads;
		}

		long wholeLocusRegenerations() {
			return wholeLocusRegenerations;
		}

		long indexBuildsInsideDownstreamPoint() {
			return indexBuildsInsideDownstreamPoint;
		}
	}

	static final class Scenario {
		private final Strategy strategy;
		private final int capacity;
		private final Counters counters = new Counters();
		private final Map<ScopedKey, Long> buildCounts = new LinkedHashMap<>();
		private final List<ConstructionFixture> constructions = new ArrayList<>();
		private final List<MetricConsumer> consumers = new ArrayList<>();

		Scenario(Strategy strategy, int capacity) {
			this.strategy = Objects.requireNonNull(strategy);
			this.capacity = capacity;
			if (capacity < 1) {
				throw new IllegalArgumentException("Capacity must be positive");
			}
		}

		ConstructionFixture construction(String identity) {
			ConstructionFixture construction = new ConstructionFixture(this,
					identity);
			constructions.add(construction);
			return construction;
		}

		Counters counters() {
			return counters;
		}

		int retainedEntries() {
			return owners().stream().mapToInt(Owner::size).sum();
		}

		int ownerCount() {
			return owners().size();
		}

		MemoryStats memoryStats() {
			Map<ScopedKey, Integer> copies = new LinkedHashMap<>();
			int entries = 0;
			for (Owner owner : owners()) {
				for (LocusMetricComponentState2D entry : owner.entries()) {
					ScopedKey key = new ScopedKey(owner.constructionIdentity,
							entry.key());
					copies.merge(key, 1, Integer::sum);
					entries++;
				}
			}
			long uniquePayload = copies.size() * COMPONENT_PAYLOAD_BYTES;
			long duplicatePayload = copies.values().stream()
					.mapToLong(count -> (long) (count - 1)
							* COMPONENT_PAYLOAD_BYTES).sum();
			long metadata = entries * ENTRY_METADATA_BYTES;
			long ownerBytes = ownerCount() * OWNER_OVERHEAD_BYTES;
			long consumerBytes = consumers.stream().filter(consumer -> !consumer.removed)
					.count() * CONSUMER_OVERHEAD_BYTES;
			return new MemoryStats(uniquePayload, duplicatePayload, metadata,
					ownerBytes, consumerBytes, uniquePayload + duplicatePayload
							+ metadata + ownerBytes + consumerBytes);
		}

		private List<Owner> owners() {
			Map<Owner, Boolean> unique = new IdentityHashMap<>();
			for (ConstructionFixture construction : constructions) {
				construction.collectOwners(unique);
			}
			for (MetricConsumer consumer : consumers) {
				if (!consumer.removed && consumer.localOwner != null) {
					unique.put(consumer.localOwner, Boolean.TRUE);
				}
			}
			return List.copyOf(unique.keySet());
		}

		private LocusMetricComponentState2D build(Owner owner, FullKey key,
				String consumerIdentity) {
			counters.componentBuilds++;
			counters.integratorCalls += 8;
			counters.evaluatorCalls += 33;
			counters.derivativeCalls += 33;
			counters.refinements += 16;
			ScopedKey scoped = new ScopedKey(owner.constructionIdentity, key);
			long prior = buildCounts.getOrDefault(scoped, 0L);
			if (prior > 0) {
				counters.duplicateCompatibleBuilds++;
			}
			buildCounts.put(scoped, prior + 1);
			double componentLength = componentValue(key.resolvedComponentKey());
			return new LocusMetricComponentState2D(key, componentLength,
					List.of(0d, componentLength), consumerIdentity,
					COMPONENT_PAYLOAD_BYTES);
		}

		private static double componentValue(String componentKey) {
			int digit = Character.digit(
					componentKey.charAt(componentKey.length() - 1), 10);
			return digit < 1 ? 1 : digit;
		}
	}

	static final class ConstructionFixture {
		private final Scenario scenario;
		private final String identity;
		private final Map<String, LocusFixture> loci = new LinkedHashMap<>();
		private Owner constructionRepository;

		ConstructionFixture(Scenario scenario, String identity) {
			this.scenario = scenario;
			this.identity = Objects.requireNonNull(identity);
		}

		LocusFixture locus(String instanceToken, String locusIdentity,
				List<String> components) {
			LocusFixture locus = new LocusFixture(this, instanceToken,
					locusIdentity, components);
			loci.put(instanceToken, locus);
			return locus;
		}

		private Owner constructionRepository() {
			if (constructionRepository == null) {
				constructionRepository = new Owner(scenario, identity,
						scenario.capacity);
			}
			return constructionRepository;
		}

		private void collectOwners(Map<Owner, Boolean> owners) {
			if (constructionRepository != null) {
				owners.put(constructionRepository, Boolean.TRUE);
			}
			for (LocusFixture locus : loci.values()) {
				locus.collectOwner(owners);
			}
		}
	}

	static final class LocusFixture {
		private final ConstructionFixture construction;
		private final String instanceToken;
		private final String locusIdentity;
		private long revision = 1;
		private List<String> components;
		private Owner sharedOwner;
		private int activeConsumers;
		private boolean removed;

		LocusFixture(ConstructionFixture construction, String instanceToken,
				String locusIdentity, List<String> components) {
			this.construction = construction;
			this.instanceToken = Objects.requireNonNull(instanceToken);
			this.locusIdentity = Objects.requireNonNull(locusIdentity);
			this.components = List.copyOf(components);
		}

		MetricConsumer consumer(String identity, Policy policy) {
			if (removed) {
				throw new IllegalStateException("Removed locus has no consumers");
			}
			MetricConsumer consumer = new MetricConsumer(this, identity, policy);
			activeConsumers++;
			construction.scenario.consumers.add(consumer);
			return consumer;
		}

		void revise(List<String> newComponents) {
			revision++;
			components = List.copyOf(newComponents);
			invalidateEntries();
		}

		void setUndefinedAndRecover() {
			invalidateEntries();
			revision++;
		}

		void remove() {
			for (MetricConsumer consumer : construction.scenario.consumers) {
				if (consumer.locus == this && !consumer.removed) {
					consumer.remove();
				}
			}
			invalidateEntries();
			sharedOwner = null;
			construction.loci.remove(instanceToken);
			removed = true;
		}

		boolean hasSharedOwner() {
			return sharedOwner != null;
		}

		private Owner ownerFor(MetricConsumer consumer) {
			return switch (construction.scenario.strategy) {
			case ALGO_LOCAL_INDEX -> consumer.localOwner;
			case CONSTRUCTION_SCOPED_METRIC_REPOSITORY ->
				construction.constructionRepository();
			case LOCUS_ATTACHED_SHARED_INDEX,
					DEDICATED_SHARED_OWNER -> sharedOwner();
			};
		}

		private Owner sharedOwner() {
			if (sharedOwner == null) {
				sharedOwner = new Owner(construction.scenario,
						construction.identity, construction.scenario.capacity);
			}
			return sharedOwner;
		}

		private void invalidateEntries() {
			if (construction.constructionRepository != null) {
				construction.constructionRepository.invalidateLocus(locusIdentity);
			}
			if (sharedOwner != null) {
				sharedOwner.clear();
			}
			for (MetricConsumer consumer : construction.scenario.consumers) {
				if (consumer.locus == this && consumer.localOwner != null) {
					consumer.localOwner.clear();
				}
			}
		}

		private void consumerRemoved() {
			activeConsumers--;
			if (activeConsumers == 0 && sharedOwner != null) {
				sharedOwner.clear();
				sharedOwner = null;
			}
		}

		private void collectOwner(Map<Owner, Boolean> owners) {
			if (sharedOwner != null) {
				owners.put(sharedOwner, Boolean.TRUE);
			}
		}
	}

	static final class MetricConsumer {
		private final LocusFixture locus;
		private final String identity;
		private final Policy policy;
		private final Owner localOwner;
		private boolean removed;

		MetricConsumer(LocusFixture locus, String identity, Policy policy) {
			this.locus = locus;
			this.identity = Objects.requireNonNull(identity);
			this.policy = Objects.requireNonNull(policy);
			this.localOwner = locus.construction.scenario.strategy
					== Strategy.ALGO_LOCAL_INDEX
							? new Owner(locus.construction.scenario,
									locus.construction.identity,
									locus.construction.scenario.capacity)
							: null;
		}

		double query(String componentKey) {
			return queryArc(componentKey, 0, 1).value();
		}

		LocusMetricContribution2D queryArc(String componentKey,
				double fromFraction, double toFraction) {
			ensureActive();
			if (!locus.components.contains(componentKey)) {
				throw new IllegalArgumentException("Component is absent in revision");
			}
			LocusMetricComponentState2D state = locus.ownerFor(this)
					.getOrBuildState(key(componentKey), identity);
			return evaluate(state, new LocusMetricRouteSegment2D(componentKey,
					fromFraction, toFraction));
		}

		double total() {
			double total = 0;
			for (String component : locus.components) {
				total += query(component);
			}
			return total;
		}

		void downstreamPointEvaluation(Runnable evaluation) {
			long builds = locus.construction.scenario.counters.componentBuilds;
			evaluation.run();
			if (locus.construction.scenario.counters.componentBuilds != builds) {
				locus.construction.scenario.counters
						.indexBuildsInsideDownstreamPoint++;
			}
		}

		void remove() {
			if (!removed) {
				if (localOwner != null) {
					localOwner.clear();
				}
				removed = true;
				locus.consumerRemoved();
			}
		}

		private FullKey key(String component) {
			return new FullKey(locus.locusIdentity, locus.revision, "branch.main",
					component, policy.capabilityVersion(),
					policy.metricAlgorithmVersion(), policy.policyVersion(),
					policy.toleranceVersion(), policy.multiplicityPolicy(),
					policy.improperLimitPolicy(), policy.workBudget());
		}

		private LocusMetricContribution2D evaluate(
				LocusMetricComponentState2D state,
				LocusMetricRouteSegment2D segment) {
			if (!state.key().resolvedComponentKey()
					.equals(segment.componentKey())) {
				throw new IllegalArgumentException(
						"Route segment and component state do not match");
			}
			double fraction = Math.abs(segment.toFraction()
					- segment.fromFraction());
			return new LocusMetricContribution2D(identity,
					segment.componentKey(), state.componentLength() * fraction,
					fraction == 1);
		}

		private void ensureActive() {
			if (removed || locus.removed) {
				throw new IllegalStateException("Removed metric consumer");
			}
		}
	}

	private static final class Owner {
		private final Scenario scenario;
		private final String constructionIdentity;
		private final int capacity;
		private final Map<FullKey, LocusMetricComponentState2D> entries =
				new LinkedHashMap<>();

		Owner(Scenario scenario, String constructionIdentity, int capacity) {
			this.scenario = scenario;
			this.constructionIdentity = constructionIdentity;
			this.capacity = capacity;
		}

		LocusMetricComponentState2D getOrBuildState(FullKey key,
				String consumerIdentity) {
			scenario.counters.queries++;
			LocusMetricComponentState2D found = entries.get(key);
			if (found != null) {
				scenario.counters.hits++;
				if (!found.builderConsumer().equals(consumerIdentity)) {
					scenario.counters.crossResultHits++;
				}
				return found;
			}
			scenario.counters.misses++;
			LocusMetricComponentState2D built = scenario.build(this, key,
					consumerIdentity);
			while (entries.size() >= capacity) {
				FullKey oldest = entries.keySet().iterator().next();
				entries.remove(oldest);
				scenario.counters.evictions++;
			}
			entries.put(key, built);
			return built;
		}

		void invalidateLocus(String locusIdentity) {
			int oldSize = entries.size();
			entries.entrySet().removeIf(entry ->
					entry.getKey().locusIdentity().equals(locusIdentity));
			if (entries.size() != oldSize) {
				scenario.counters.invalidations++;
			}
		}

		void clear() {
			if (!entries.isEmpty()) {
				entries.clear();
				scenario.counters.invalidations++;
			}
		}

		int size() {
			return entries.size();
		}

		List<LocusMetricComponentState2D> entries() {
			return List.copyOf(entries.values());
		}
	}

	private record ScopedKey(String constructionIdentity, FullKey key) {
	}

	private G7AR1MultiConsumerMetricIndexExperiment() {
		// Utility holder.
	}
}
