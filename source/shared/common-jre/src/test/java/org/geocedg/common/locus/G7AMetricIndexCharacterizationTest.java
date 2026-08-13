/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.locus.G7AMetricIndexExperiment.Counters;
import org.geocedg.common.locus.G7AMetricIndexExperiment.Policy;
import org.geocedg.common.locus.G7AMetricIndexExperiment.Strategy;
import org.junit.jupiter.api.Test;

/** Functional-counter comparison of all three G7A metric-index strategies. */
class G7AMetricIndexCharacterizationTest {
	private static final List<String> COMPONENTS = List.of("c0", "c1", "c2");
	private static final Policy POLICY = new Policy("adaptive-simpson/v1",
			"route-policy/v1", "abs=1e-10;rel=1e-9",
			"constructive-traversal/v1", "improper-transform/v1");

	@Test
	void oneTenAndHundredSameQueriesDemonstrateLazyScope() {
		for (int queryCount : List.of(1, 10, 100)) {
			G7AMetricIndexExperiment reference = experiment(
					Strategy.REFERENCE_NO_INDEX_REUSE, 0);
			G7AMetricIndexExperiment eager = experiment(
					Strategy.EAGER_WHOLE_REVISION, 32);
			G7AMetricIndexExperiment lazy = experiment(
					Strategy.LAZY_COMPONENT_REVISION, 32);
			long referenceNanos = repeat(reference, queryCount, "c0", POLICY);
			long eagerNanos = repeat(eager, queryCount, "c0", POLICY);
			final long lazyNanos = repeat(lazy, queryCount, "c0", POLICY);

			assertEquals(queryCount, reference.counters().componentBuilds());
			assertEquals(3, eager.counters().componentBuilds());
			assertEquals(1, lazy.counters().componentBuilds());
			assertEquals(Math.max(0, queryCount - 1),
					lazy.counters().hits());
			assertEquals(1, lazy.retainedEntries());
			metric("same-ab", queryCount, reference, referenceNanos);
			metric("same-ab", queryCount, eager, eagerNanos);
			metric("same-ab", queryCount, lazy, lazyNanos);
		}
		for (Strategy strategy : Strategy.values()) {
			G7AMetricIndexExperiment latency = experiment(strategy,
					strategy == Strategy.REFERENCE_NO_INDEX_REUSE ? 0 : 32);
			long coldStart = System.nanoTime();
			latency.query(7, "c0", POLICY);
			long coldNanos = System.nanoTime() - coldStart;
			long warmStart = System.nanoTime();
			latency.query(7, "c0", POLICY);
			long warmNanos = System.nanoTime() - warmStart;
			System.out.println("G7A_INDEX_LATENCY strategy=" + strategy
					+ " cold_ns_informational=" + coldNanos
					+ " warm_ns_informational=" + warmNanos);
			assertTrue(coldNanos >= 0);
			assertTrue(warmNanos >= 0);
		}
	}

	@Test
	void commonRouteTracesReuseSameRevisionComponentIndex() {
		for (String trace : List.of("overlapping", "reverse", "periodic",
				"stop", "wrap", "strict")) {
			for (int queryCount : List.of(1, 10, 100)) {
				for (Strategy strategy : Strategy.values()) {
					int capacity = strategy == Strategy.REFERENCE_NO_INDEX_REUSE
							? 0 : 16;
					G7AMetricIndexExperiment experiment = experiment(strategy,
							capacity);
					if (!trace.equals("strict")) {
						repeat(experiment, queryCount, "c0", POLICY);
					}
					long expectedBuilds = trace.equals("strict") ? 0
							: switch (strategy) {
							case REFERENCE_NO_INDEX_REUSE -> queryCount;
							case EAGER_WHOLE_REVISION -> 3;
							case LAZY_COMPONENT_REVISION -> 1;
							};
					assertEquals(expectedBuilds,
							experiment.counters().componentBuilds(), trace);
					assertEquals(0, experiment.counters().evictions(), trace);
				}
			}
		}
	}

	@Test
	void repeatedTotalMakesLazyAndEagerConvergeWithoutRedundantBuilds() {
		for (int queryCount : List.of(1, 10, 100)) {
			for (Strategy strategy : List.of(Strategy.EAGER_WHOLE_REVISION,
					Strategy.LAZY_COMPONENT_REVISION)) {
				G7AMetricIndexExperiment experiment = experiment(strategy, 16);
				for (int query = 0; query < queryCount; query++) {
					assertEquals(6, experiment.queryTotal(7, POLICY), 0);
				}
				assertEquals(3, experiment.counters().componentBuilds());
				long expectedHits = strategy == Strategy.EAGER_WHOLE_REVISION
						? 3L * queryCount - 1 : 3L * (queryCount - 1);
				assertEquals(expectedHits, experiment.counters().hits());
				assertEquals(3, experiment.retainedEntries());
			}

			G7AMetricIndexExperiment reference = experiment(
					Strategy.REFERENCE_NO_INDEX_REUSE, 0);
			for (int query = 0; query < queryCount; query++) {
				assertEquals(6, reference.queryTotal(7, POLICY), 0);
			}
			assertEquals(3L * queryCount,
					reference.counters().componentBuilds());
		}
	}

	@Test
	void policyAndToleranceChangesCannotHitIncompleteKeys() {
		G7AMetricIndexExperiment lazy = experiment(
				Strategy.LAZY_COMPONENT_REVISION, 16);
		List<Policy> policies = List.of(
				POLICY,
				new Policy("adaptive-simpson/v2", "route-policy/v1",
						"abs=1e-10;rel=1e-9", "constructive-traversal/v1",
						"improper-transform/v1"),
				new Policy("adaptive-simpson/v1", "route-policy/v2",
						"abs=1e-10;rel=1e-9", "constructive-traversal/v1",
						"improper-transform/v1"),
				new Policy("adaptive-simpson/v1", "route-policy/v1",
						"abs=1e-12;rel=1e-10", "constructive-traversal/v1",
						"improper-transform/v1"),
				new Policy("adaptive-simpson/v1", "route-policy/v1",
						"abs=1e-10;rel=1e-9", "image-union/v1",
						"improper-transform/v1"),
				new Policy("adaptive-simpson/v1", "route-policy/v1",
						"abs=1e-10;rel=1e-9", "constructive-traversal/v1",
						"improper-cutoff/v2"));
		for (Policy policy : policies) {
			assertEquals(1, lazy.query(7, "c0", policy), 0);
		}
		assertEquals(policies.size(), lazy.counters().misses());
		assertEquals(policies.size(), lazy.counters().componentBuilds());
		assertEquals(policies.size(), lazy.retainedEntries());
		assertEquals(0, lazy.counters().hits());

		for (int queryCount : List.of(1, 10, 100)) {
			for (Strategy strategy : Strategy.values()) {
				int capacity = strategy == Strategy.REFERENCE_NO_INDEX_REUSE
						? 0 : 512;
				G7AMetricIndexExperiment changed = experiment(strategy,
						capacity);
				for (int query = 0; query < queryCount; query++) {
					changed.query(7, "c0", policyVersion(query));
				}
				long expectedBuilds = strategy == Strategy.EAGER_WHOLE_REVISION
						? 3L * queryCount : queryCount;
				assertEquals(expectedBuilds,
						changed.counters().componentBuilds());
				assertEquals(0, changed.counters().hits());
			}
		}
	}

	@Test
	void boundedDeterministicEvictionAndRevisionInvalidationAreObservable() {
		G7AMetricIndexExperiment lazy = experiment(
				Strategy.LAZY_COMPONENT_REVISION, 2);
		lazy.query(7, "c0", POLICY);
		lazy.query(7, "c1", POLICY);
		lazy.query(7, "c2", POLICY);
		assertEquals(2, lazy.retainedEntries());
		assertEquals(1, lazy.counters().evictions());
		assertEquals(List.of("c1", "c2"), lazy.retainedKeys().stream()
				.map(key -> key.resolvedValidComponentKey()).toList());
		assertTrue(lazy.approximateRetainedBytes() <= 1024);

		lazy.invalidateLocusRevision(7);
		assertEquals(0, lazy.retainedEntries());
		assertEquals(1, lazy.counters().invalidations());
		lazy.query(8, "c0", POLICY);
		assertEquals(4, lazy.counters().componentBuilds());
		assertTrue(lazy.retainedKeys().stream()
				.allMatch(key -> key.semanticRevision() == 8));

		for (String cause : List.of("source", "topology", "branch", "remove")) {
			G7AMetricIndexExperiment owner = experiment(
					Strategy.LAZY_COMPONENT_REVISION, 2);
			owner.query(7, "c0", POLICY);
			owner.invalidateAll();
			assertEquals(0, owner.retainedEntries(), cause);
			assertEquals(1, owner.counters().invalidations(), cause);
			owner.query(8, "c0", POLICY);
			assertEquals(2, owner.counters().componentBuilds(), cause);
		}
	}

	@Test
	void exceptionCannotPublishPartialEntryAndCleanupRunsFinally() {
		G7AMetricIndexExperiment lazy = experiment(
				Strategy.LAZY_COMPONENT_REVISION, 4);
		lazy.failNextBuild();
		assertThrows(IllegalStateException.class,
				() -> lazy.query(7, "c0", POLICY));
		assertEquals(0, lazy.retainedEntries());
		assertEquals(1, lazy.counters().failedBuilds());
		assertEquals(0, lazy.counters().activeBuilds());
		assertEquals(1, lazy.counters().maximumActiveBuilds());
		assertEquals(1, lazy.query(7, "c0", POLICY), 0);
		assertEquals(1, lazy.retainedEntries());
	}

	@Test
	void cacheOffAndAllIndexStrategiesAreSemanticallyEqual() {
		List<Double> totals = new ArrayList<>();
		for (Strategy strategy : Strategy.values()) {
			G7AMetricIndexExperiment experiment = experiment(strategy,
					strategy == Strategy.REFERENCE_NO_INDEX_REUSE ? 0 : 16);
			totals.add(experiment.queryTotal(7, POLICY));
			assertEquals(0, experiment.counters().activeBuilds());
		}
		assertEquals(List.of(6.0, 6.0, 6.0), totals);
	}

	@Test
	void arcCoordinateReuseRemainsComponentLocal() {
		G7AMetricIndexExperiment lazy = experiment(
				Strategy.LAZY_COMPONENT_REVISION, 16);
		lazy.query(7, "c0", POLICY);
		long callsAfterBuild = lazy.counters().integratorCalls();
		for (int query = 0; query < 100; query++) {
			lazy.query(7, "c0", POLICY);
		}
		assertEquals(callsAfterBuild, lazy.counters().integratorCalls());
		assertEquals(100, lazy.counters().hits());
		assertEquals(1600, lazy.counters().reusedIntervals());
		assertFalse(lazy.retainedKeys().get(0).resolvedValidComponentKey()
				.contains("semantic-position"));
	}

	private static G7AMetricIndexExperiment experiment(Strategy strategy,
			int capacity) {
		return new G7AMetricIndexExperiment(strategy, capacity,
				"g7a.index.fixture", "sheet.main", "provider+derivative/v1",
				COMPONENTS);
	}

	private static long repeat(G7AMetricIndexExperiment experiment,
			int queryCount, String component, Policy policy) {
		long start = System.nanoTime();
		for (int query = 0; query < queryCount; query++) {
			experiment.query(7, component, policy);
		}
		return System.nanoTime() - start;
	}

	private static Policy policyVersion(int revision) {
		return new Policy("adaptive-simpson/v1", "route-policy/" + revision,
				"abs=1e-10;rel=1e-9", "constructive-traversal/v1",
				"improper-transform/v1");
	}

	private static void metric(String trace, int queries,
			G7AMetricIndexExperiment experiment, long elapsedNanos) {
		Counters counters = experiment.counters();
		System.out.println("G7A_INDEX trace=" + trace + " queries=" + queries
				+ " strategy=" + experiment.strategy()
				+ " component_builds=" + counters.componentBuilds()
				+ " integrator_calls=" + counters.integratorCalls()
				+ " evaluator_calls=" + counters.evaluatorCalls()
				+ " derivative_calls=" + counters.derivativeCalls()
				+ " hits=" + counters.hits() + " misses=" + counters.misses()
				+ " retained=" + experiment.retainedEntries()
				+ " elapsed_ns_informational=" + elapsedNanos);
	}
}
