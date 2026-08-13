/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexExperiment.ConstructionFixture;
import org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexExperiment.Counters;
import org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexExperiment.LocusFixture;
import org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexExperiment.MemoryStats;
import org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexExperiment.MetricConsumer;
import org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexExperiment.Policy;
import org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexExperiment.Scenario;
import org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexExperiment.Strategy;
import org.geocedg.common.locus.G7AR1MultiConsumerMetricIndexExperiment.WorkBudgetKey;
import org.junit.jupiter.api.Test;

/** Functional multi-result ownership evidence for G7A-R1. */
class G7AR1MultiConsumerMetricIndexTest {
	private static final int CAPACITY = 64;
	private static final Policy POLICY = policy("cap/v1", "metric/v1",
			"tol/1e-10-1e-9", "constructive", "improper/v1",
			new WorkBudgetKey(32768, 16384, 22));

	@Test
	void compatibleConsumerScalingExposesLocalDuplicationAndSharedOptimum() {
		for (int consumers : List.of(1, 3, 10, 100)) {
			for (Strategy strategy : Strategy.values()) {
				Scenario scenario = new Scenario(strategy, CAPACITY);
				LocusFixture locus = scenario.construction("construction-a")
						.locus("locus-object-a", "locus-a", List.of("C1"));
				for (int index = 0; index < consumers; index++) {
					locus.consumer("M" + index, POLICY).query("C1");
				}
				Counters counters = scenario.counters();
				MemoryStats memory = scenario.memoryStats();
				long expectedBuilds = strategy == Strategy.ALGO_LOCAL_INDEX
						? consumers : 1;
				assertEquals(expectedBuilds, counters.componentBuilds());
				assertEquals(strategy == Strategy.ALGO_LOCAL_INDEX
						? consumers - 1L : 0,
						counters.duplicateCompatibleBuilds());
				assertEquals(strategy == Strategy.ALGO_LOCAL_INDEX
						? 0 : consumers - 1L, counters.crossResultHits());
				assertEquals(strategy == Strategy.ALGO_LOCAL_INDEX
						? (consumers - 1L)
								* G7AR1MultiConsumerMetricIndexExperiment
										.COMPONENT_PAYLOAD_BYTES
						: 0, memory.duplicatePayloadBytes());
				System.out.println("G7A_R1_MULTI strategy=" + strategy
						+ " consumers=" + consumers + " builds="
						+ counters.componentBuilds() + " cross_hits="
						+ counters.crossResultHits() + " duplicate_builds="
						+ counters.duplicateCompatibleBuilds()
						+ " retained_entries=" + scenario.retainedEntries()
						+ " unique_payload_bytes="
						+ memory.uniquePayloadBytes()
						+ " duplicate_payload_bytes="
						+ memory.duplicatePayloadBytes() + " metadata_bytes="
						+ memory.entryMetadataBytes() + " owner_bytes="
						+ memory.ownerOverheadBytes() + " consumer_bytes="
						+ memory.consumerOverheadBytes() + " total_bytes="
						+ memory.totalRetainedBytes());
			}
		}
	}

	@Test
	void totalAndSubarcOrdersBuildThreeReusableComponentStates() {
		Map<String, Double> orderA = runMixedOrder(false);
		Map<String, Double> orderB = runMixedOrder(true);
		assertEquals(orderA, orderB);
	}

	@Test
	void everyResultAffectingPolicyDimensionIsolatedInFullKey() {
		Scenario scenario = new Scenario(Strategy.DEDICATED_SHARED_OWNER,
				CAPACITY);
		LocusFixture locus = scenario.construction("construction-a")
				.locus("locus-object-a", "locus-a", List.of("C1"));
		locus.consumer("base-a", POLICY).query("C1");
		locus.consumer("base-b", POLICY).query("C1");
		for (Policy different : List.of(
				policy("cap/v2", "metric/v1", "tol/1e-10-1e-9",
						"constructive", "improper/v1", POLICY.workBudget()),
				policy("cap/v1", "metric/v2", "tol/1e-10-1e-9",
						"constructive", "improper/v1", POLICY.workBudget()),
				policy("cap/v1", "metric/v1", "tol/tighter", "constructive",
						"improper/v1", POLICY.workBudget()),
				policy("cap/v1", "metric/v1", "tol/1e-10-1e-9", "union",
						"improper/v1", POLICY.workBudget()),
				policy("cap/v1", "metric/v1", "tol/1e-10-1e-9",
						"constructive", "improper/v2", POLICY.workBudget()),
				policy("cap/v1", "metric/v1", "tol/1e-10-1e-9",
						"constructive", "improper/v1",
						new WorkBudgetKey(65536, 32768, 22)))) {
			locus.consumer("different-" + scenario.counters().queries(),
					different).query("C1");
		}
		assertEquals(7, scenario.counters().componentBuilds());
		assertEquals(1, scenario.counters().crossResultHits());
		assertEquals(7, scenario.retainedEntries());
	}

	@Test
	void locusAndConstructionIdentityPreventAccidentalSharing() {
		Scenario sameConstruction = new Scenario(
				Strategy.CONSTRUCTION_SCOPED_METRIC_REPOSITORY, CAPACITY);
		ConstructionFixture construction = sameConstruction.construction("A");
		construction.locus("locus-object-a", "locus-a", List.of("C1"))
				.consumer("M1", POLICY).query("C1");
		construction.locus("locus-object-b", "locus-b", List.of("C1"))
				.consumer("M2", POLICY).query("C1");
		assertEquals(2, sameConstruction.counters().componentBuilds());
		assertEquals(0, sameConstruction.counters().crossResultHits());

		Scenario differentConstructions = new Scenario(
				Strategy.CONSTRUCTION_SCOPED_METRIC_REPOSITORY, CAPACITY);
		differentConstructions.construction("A")
				.locus("locus-object-a", "same-looking-id", List.of("C1"))
				.consumer("M1", POLICY).query("C1");
		differentConstructions.construction("B")
				.locus("locus-object-b", "same-looking-id", List.of("C1"))
				.consumer("M2", POLICY).query("C1");
		assertEquals(2, differentConstructions.counters().componentBuilds());
		assertEquals(0, differentConstructions.counters().crossResultHits());
	}

	@Test
	void revisionTopologyAndUndefinedTransitionsRebuildOncePerSharedKey() {
		Scenario scenario = new Scenario(Strategy.DEDICATED_SHARED_OWNER,
				CAPACITY);
		LocusFixture locus = scenario.construction("construction-a")
				.locus("locus-object-a", "locus-a", List.of("C1"));
		MetricConsumer first = locus.consumer("M1", POLICY);
		MetricConsumer second = locus.consumer("M2", POLICY);
		first.query("C1");
		second.query("C1");

		locus.revise(List.of("C1"));
		first.query("C1");
		second.query("C1");
		locus.revise(List.of("C1a", "C1b"));
		first.query("C1a");
		second.query("C1a");
		locus.revise(List.of("C1"));
		first.query("C1");
		second.query("C1");
		locus.revise(List.of());
		locus.revise(List.of("C1"));
		first.query("C1");
		second.query("C1");
		locus.setUndefinedAndRecover();
		first.query("C1");
		second.query("C1");

		assertEquals(6, scenario.counters().componentBuilds());
		assertEquals(6, scenario.counters().crossResultHits());
		assertEquals(0, scenario.counters().duplicateCompatibleBuilds());
		assertTrue(scenario.counters().invalidations() >= 5);
		assertEquals(1, scenario.retainedEntries());
	}

	@Test
	void dedicatedOwnerReleasesOnLastConsumerAndLocusRemoval() {
		Scenario scenario = new Scenario(Strategy.DEDICATED_SHARED_OWNER,
				CAPACITY);
		LocusFixture locus = scenario.construction("construction-a")
				.locus("locus-object-a", "locus-a", List.of("C1"));
		MetricConsumer first = locus.consumer("M1", POLICY);
		MetricConsumer second = locus.consumer("M2", POLICY);
		first.query("C1");
		second.query("C1");
		first.remove();
		assertTrue(locus.hasSharedOwner());
		assertEquals(1, scenario.retainedEntries());
		second.remove();
		assertFalse(locus.hasSharedOwner());
		assertEquals(0, scenario.retainedEntries());

		MetricConsumer recovered = locus.consumer("M3", POLICY);
		recovered.query("C1");
		locus.remove();
		assertFalse(locus.hasSharedOwner());
		assertEquals(0, scenario.retainedEntries());
		assertThrows(IllegalStateException.class, () -> recovered.query("C1"));
	}

	@Test
	void nestedMultiConsumerCompositionHasNoForbiddenWork() {
		Scenario scenario = new Scenario(Strategy.DEDICATED_SHARED_OWNER,
				CAPACITY);
		ConstructionFixture construction = scenario.construction("construction-a");
		LocusFixture locus1 = construction.locus("L1-object", "L1",
				List.of("C1"));
		MetricConsumer m1a = locus1.consumer("M1a", POLICY);
		MetricConsumer m1b = locus1.consumer("M1b", POLICY);
		MetricConsumer m1total = locus1.consumer("M1total", POLICY);
		m1a.query("C1");
		m1b.query("C1");
		m1total.total();
		LocusFixture locus2 = construction.locus("L2-object", "L2",
				List.of("C1"));
		MetricConsumer m2a = locus2.consumer("M2a", POLICY);
		MetricConsumer m2total = locus2.consumer("M2total", POLICY);
		m2a.query("C1");
		m2total.total();
		construction.locus("L3-object", "L3", List.of("C1"));
		for (int point = 0; point < 100; point++) {
			m1total.downstreamPointEvaluation(() -> {
				// A downstream point consumes the already published metric value.
			});
			m2total.downstreamPointEvaluation(() -> {
				// No index lookup/build belongs inside point evaluation.
			});
		}

		Counters counters = scenario.counters();
		assertEquals(2, counters.componentBuilds());
		assertEquals(3, counters.crossResultHits());
		assertEquals(0, counters.duplicateCompatibleBuilds());
		assertEquals(0, counters.renderReads());
		assertEquals(0, counters.legacySampleReads());
		assertEquals(0, counters.wholeLocusRegenerations());
		assertEquals(0, counters.indexBuildsInsideDownstreamPoint());
	}

	@Test
	void ownerStrategiesAndCacheOffOracleReturnIdenticalSemanticValues() {
		Map<Strategy, List<Double>> values = new LinkedHashMap<>();
		for (Strategy strategy : Strategy.values()) {
			Scenario scenario = new Scenario(strategy, CAPACITY);
			LocusFixture locus = scenario.construction("construction-a")
					.locus("locus-object-a", "locus-a",
							List.of("C1", "C2", "C3"));
			values.put(strategy, List.of(
					locus.consumer("M1", POLICY).query("C1"),
					locus.consumer("M2", POLICY).query("C2"),
					locus.consumer("M3", POLICY).total()));
		}
		List<Double> oracle = values.get(Strategy.ALGO_LOCAL_INDEX);
		for (List<Double> value : values.values()) {
			assertEquals(oracle, value);
		}
	}

	private static Map<String, Double> runMixedOrder(boolean totalFirst) {
		Scenario scenario = new Scenario(Strategy.DEDICATED_SHARED_OWNER,
				CAPACITY);
		LocusFixture locus = scenario.construction("construction-a")
				.locus("locus-object-a", "locus-a",
						List.of("C1", "C2", "C3"));
		MetricConsumer m1 = locus.consumer("M1", POLICY);
		MetricConsumer m2 = locus.consumer("M2", POLICY);
		MetricConsumer m3 = locus.consumer("M3", POLICY);
		MetricConsumer m4 = locus.consumer("M4", POLICY);
		Map<String, Double> values = new LinkedHashMap<>();
		if (totalFirst) {
			values.put("M3", m3.total());
			values.put("M4", m4.queryArc("C2", 0.25, 0.75).value());
			values.put("M2", m2.query("C1"));
			values.put("M1", m1.queryArc("C1", 0.2, 0.8).value());
		} else {
			values.put("M1", m1.queryArc("C1", 0.2, 0.8).value());
			values.put("M2", m2.query("C1"));
			values.put("M3", m3.total());
			values.put("M4", m4.queryArc("C2", 0.25, 0.75).value());
		}
		assertEquals(3, scenario.counters().componentBuilds());
		assertEquals(3, scenario.counters().crossResultHits());
		assertEquals(0, scenario.counters().duplicateCompatibleBuilds());
		assertEquals(3, scenario.retainedEntries());
		return values;
	}

	private static Policy policy(String capability, String algorithm,
			String tolerance, String multiplicity, String improper,
			WorkBudgetKey workBudget) {
		return new Policy(capability, algorithm, "policy/v1", tolerance,
				multiplicity, improper, workBudget);
	}
}
