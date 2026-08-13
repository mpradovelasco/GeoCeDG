/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.Test;

/** Test-private L1 -> metric(L1) -> L2 -> metric(L2) -> L3 trace. */
class G7ANestedMetricCharacterizationTest {

	@Test
	void threeLevelCompositionHasNoRenderSampleOrPerPointIndexWork() {
		Trace trace = trace(true);
		double first = trace.metric2.total("policy/v1");
		for (int query = 0; query < 100; query++) {
			assertEquals(first, trace.metric2.total("policy/v1"), 0);
		}
		for (int point = 0; point < 64; point++) {
			trace.locus3.evaluate(point / 63.0);
		}

		assertEquals(1, trace.metric1.counters.indexBuilds);
		assertEquals(1, trace.metric2.counters.indexBuilds);
		assertTrue(trace.metric1.counters.cacheHits
				>= trace.locus2.counters.evaluatorCalls - 1);
		assertTrue(trace.metric2.counters.cacheHits >= 100 + 63);
		assertEquals(0, trace.totalRenderReads());
		assertEquals(0, trace.totalLegacySampleReads());
		assertEquals(0, trace.totalWholeLocusRegenerations());
		assertEquals(1, trace.metric1.counters.maximumActiveBuilds);
		assertEquals(1, trace.metric2.counters.maximumActiveBuilds);
		assertEquals(Thread.currentThread().getId(),
				trace.metric1.counters.kernelThreadId);
		assertEquals(Thread.currentThread().getId(),
				trace.metric2.counters.kernelThreadId);
		System.out.println("G7A_NESTED repeated_metric_requests="
				+ trace.metric2.counters.requests + " metric1_index_builds="
				+ trace.metric1.counters.indexBuilds + " metric2_index_builds="
				+ trace.metric2.counters.indexBuilds + " metric1_hits="
				+ trace.metric1.counters.cacheHits + " metric2_hits="
				+ trace.metric2.counters.cacheHits + " locus1_evaluations="
				+ trace.locus1.counters.evaluatorCalls + " locus2_evaluations="
				+ trace.locus2.counters.evaluatorCalls + " locus3_evaluations="
				+ trace.locus3.counters.evaluatorCalls
				+ " render_reads=0 legacy_sample_reads=0 whole_regenerations=0");
	}

	@Test
	void geometryChangeRecomputesOnceAndUnchangedRepeatDoesNot() {
		Trace trace = trace(true);
		double before = trace.metric2.total("policy/v1");
		long metric1Builds = trace.metric1.counters.indexBuilds;
		final long metric2Builds = trace.metric2.counters.indexBuilds;

		trace.locus1.changeGeometry(2);
		trace.metric1.invalidate();
		trace.locus2.refreshFromUpstream();
		trace.metric2.invalidate();
		trace.locus3.refreshFromUpstream();
		double after = trace.metric2.total("policy/v1");

		assertNotEquals(before, after);
		assertEquals(metric1Builds + 1, trace.metric1.counters.indexBuilds);
		assertEquals(metric2Builds + 1, trace.metric2.counters.indexBuilds);
		trace.metric2.total("policy/v1");
		assertEquals(metric2Builds + 1, trace.metric2.counters.indexBuilds);
		assertEquals(1, trace.metric1.counters.invalidations);
		assertEquals(1, trace.metric2.counters.invalidations);
		assertEquals(0, trace.totalWholeLocusRegenerations());
	}

	@Test
	void cacheOffKeepsSemanticValueButMakesWasteVisible() {
		Trace cached = trace(true);
		Trace reference = trace(false);
		double cachedValue = cached.metric2.total("policy/v1");
		double referenceValue = reference.metric2.total("policy/v1");
		assertEquals(referenceValue, cachedValue, 0);

		for (int query = 0; query < 10; query++) {
			cached.metric2.total("policy/v1");
			reference.metric2.total("policy/v1");
		}
		assertEquals(1, cached.metric2.counters.indexBuilds);
		assertEquals(11, reference.metric2.counters.indexBuilds);
		assertTrue(reference.metric1.counters.indexBuilds
				> cached.metric1.counters.indexBuilds);
		assertEquals(0, cached.totalRenderReads() + reference.totalRenderReads());
	}

	@Test
	void policyChangeCreatesOneNecessaryBuildPerAffectedMetric() {
		Trace trace = trace(true);
		trace.metric2.total("policy/v1");
		long firstBuilds = trace.metric2.counters.indexBuilds;
		trace.metric2.total("policy/v2");
		assertEquals(firstBuilds + 1, trace.metric2.counters.indexBuilds);
		trace.metric2.total("policy/v2");
		assertEquals(firstBuilds + 1, trace.metric2.counters.indexBuilds);
	}

	private static Trace trace(boolean cacheEnabled) {
		LocusNode locus1 = LocusNode.leaf(parameter -> parameter * parameter);
		MetricNode metric1 = new MetricNode(locus1, cacheEnabled);
		LocusNode locus2 = LocusNode.downstream(metric1,
				parameter -> parameter);
		MetricNode metric2 = new MetricNode(locus2, cacheEnabled);
		LocusNode locus3 = LocusNode.downstream(metric2,
				parameter -> 2 * parameter);
		return new Trace(locus1, metric1, locus2, metric2, locus3);
	}

	private static final class LocusNode {
		private final MetricNode upstreamMetric;
		private final DoubleUnaryOperator localFunction;
		private final LocusCounters counters = new LocusCounters();
		private long revision = 1;
		private double geometryScale = 1;

		private LocusNode(MetricNode upstreamMetric,
				DoubleUnaryOperator localFunction) {
			this.upstreamMetric = upstreamMetric;
			this.localFunction = localFunction;
		}

		static LocusNode leaf(DoubleUnaryOperator localFunction) {
			return new LocusNode(null, localFunction);
		}

		static LocusNode downstream(MetricNode upstreamMetric,
				DoubleUnaryOperator localFunction) {
			return new LocusNode(upstreamMetric, localFunction);
		}

		double evaluate(double parameter) {
			counters.evaluatorCalls++;
			double upstream = upstreamMetric == null ? 0
					: upstreamMetric.total("policy/v1");
			return geometryScale * localFunction.applyAsDouble(parameter)
					+ upstream * parameter;
		}

		void changeGeometry(double scale) {
			geometryScale = scale;
			revision++;
		}

		void refreshFromUpstream() {
			revision++;
		}
	}

	private static final class MetricNode {
		private final LocusNode source;
		private final boolean cacheEnabled;
		private final MetricCounters counters = new MetricCounters();
		private String retainedKey;
		private double retainedValue;

		MetricNode(LocusNode source, boolean cacheEnabled) {
			this.source = source;
			this.cacheEnabled = cacheEnabled;
		}

		double total(String policy) {
			counters.requests++;
			String key = source.revision + ":" + policy;
			if (cacheEnabled && key.equals(retainedKey)) {
				counters.cacheHits++;
				return retainedValue;
			}
			counters.cacheMisses++;
			double value = build();
			if (cacheEnabled) {
				retainedKey = key;
				retainedValue = value;
			}
			return value;
		}

		void invalidate() {
			retainedKey = null;
			counters.invalidations++;
		}

		private double build() {
			counters.activeBuilds++;
			counters.maximumActiveBuilds = Math.max(
					counters.maximumActiveBuilds, counters.activeBuilds);
			try {
				counters.kernelThreadId = Thread.currentThread().getId();
				counters.indexBuilds++;
				counters.componentBuilds++;
				counters.integrationCalls++;
				double total = 0;
				double previous = source.evaluate(0);
				for (int index = 1; index <= 16; index++) {
					double current = source.evaluate(index / 16.0);
					total += Math.hypot(1.0 / 16, current - previous);
					previous = current;
				}
				return total;
			} finally {
				counters.activeBuilds--;
			}
		}
	}

	private record Trace(LocusNode locus1, MetricNode metric1, LocusNode locus2,
			MetricNode metric2, LocusNode locus3) {
		long totalRenderReads() {
			return locus1.counters.renderReads + locus2.counters.renderReads
					+ locus3.counters.renderReads;
		}

		long totalLegacySampleReads() {
			return locus1.counters.legacySampleReads
					+ locus2.counters.legacySampleReads
					+ locus3.counters.legacySampleReads;
		}

		long totalWholeLocusRegenerations() {
			return locus1.counters.wholeLocusRegenerations
					+ locus2.counters.wholeLocusRegenerations
					+ locus3.counters.wholeLocusRegenerations;
		}
	}

	private static final class LocusCounters {
		private long evaluatorCalls;
		private long renderReads;
		private long legacySampleReads;
		private long wholeLocusRegenerations;
	}

	private static final class MetricCounters {
		private long requests;
		private long indexBuilds;
		private long componentBuilds;
		private long integrationCalls;
		private long cacheHits;
		private long cacheMisses;
		private long invalidations;
		private long activeBuilds;
		private long maximumActiveBuilds;
		private long kernelThreadId;
	}
}
