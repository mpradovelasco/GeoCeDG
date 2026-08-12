/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusBranchSnapshot2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSourceSnapshot2D;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.kernel.locus.StablePathDomainProvider2D;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Functional gates plus informational, non-absolute G6R distributions. */
class LocusV2HardeningBenchmarkTest extends BaseUnitTest {
	private static final String BRANCH_KEY = "g6r-benchmark.sheet.main";
	private static final int QUERY_COUNT = 128;
	private static final int MEASURED_RUNS = 7;

	@Test
	void nestedDepthDistributionsPreserveLinearFunctionalCostSessionOnOff() {
		for (int depth : new int[] {1, 2, 3, 5}) {
			Chain chain = chain(depth);
			measureNested(chain, false);
			measureNested(chain, true);
		}
	}

	@Test
	void repeatedKeysAndSmallCapacityHaveDeterministicBoundedBehavior() {
		Chain duplicateChain = chain(3);
		reset(duplicateChain);
		LocusEvaluationSession2D duplicateSession =
				LocusEvaluationSession2D.memoizing(512);
		evaluateBatch(duplicateChain.outer(), duplicateSession);
		evaluateBatch(duplicateChain.outer(), duplicateSession);
		assertEquals((long) QUERY_COUNT * 3, calls(duplicateChain));
		assertEquals(QUERY_COUNT, duplicateSession.getHits());
		assertEquals((long) QUERY_COUNT * 3, duplicateSession.getMisses());

		Chain evictionChain = chain(3);
		reset(evictionChain);
		LocusEvaluationSession2D evictionSession =
				LocusEvaluationSession2D.memoizing(32);
		evaluateBatch(evictionChain.outer(), evictionSession);
		assertEquals(32, evictionSession.getCachedEntryCount());
		assertTrue(evictionSession.getEvictions() > 0);
		assertEquals((long) QUERY_COUNT * 3, calls(evictionChain));
		assertForbiddenWorkIsZero(evictionChain);
		System.out.println("G6R_METRIC case=BM-SESSION-CAPACITY"
				+ " duplicate_hits=" + duplicateSession.getHits()
				+ " duplicate_misses=" + duplicateSession.getMisses()
				+ " capacity=32 evictions=" + evictionSession.getEvictions()
				+ " retained_entries=" + evictionSession.getCachedEntryCount());
	}

	@Test
	void innermostInvalidationAndComponentChangePublishOnlyNormalDagRevisions() {
		Chain chain = chain(5);
		long[] before = revisions(chain);
		long start = System.nanoTime();
		chain.source.setValue(2);
		chain.source.updateCascade();
		long elapsed = System.nanoTime() - start;
		for (int index = 0; index < chain.loci.size(); index++) {
			assertEquals(before[index] + 1,
					chain.loci.get(index).getSemanticRevision());
		}
		assertForbiddenWorkIsZero(chain);

		ExplicitNumericDomainProvider2D provider = provider();
		GeoNumeric control = new GeoNumeric(getConstruction(), 0);
		GeoLocusV2 topology = LocusV2Factory.createDynamicAnalytic(
				LocusV2Mode.V2, getConstruction(), "g6r-topology-benchmark",
				Collections.singletonList(control), provider,
				(sources, previous) -> componentSnapshot(sources, provider),
				(sources, branch, parameter, session) ->
						new LocusPoint2D(parameter, parameter * parameter),
				"g6r-topology-benchmark/v1");
		long topologyRevision = topology.getSemanticRevision();
		long topologyStart = System.nanoTime();
		control.setValue(0.25);
		control.updateCascade();
		long topologyElapsed = System.nanoTime() - topologyStart;
		assertEquals(topologyRevision + 1, topology.getSemanticRevision());
		assertEquals(2, topology.getSemanticDefinition().getBranch(BRANCH_KEY)
				.getValidDomainComponents().size());
		System.out.println("G6R_METRIC case=BM-INVALIDATION depth=5"
				+ " elapsed_ns=" + elapsed + " published_revisions=5"
				+ " topology_change_elapsed_ns=" + topologyElapsed);
	}

	@Test
	void renderAndSegmentProviderDistributionsSeparateVisualAndSemanticCost() {
		GeoLocusV2 line = lineLocus();
		LocusRenderPolicy2D uniform = new LocusRenderPolicy2D(1, 800, 600,
				200, 200, 256);
		LocusRenderPolicy2D adaptive = LocusRenderPolicy2D.adaptive(1, 800, 600,
				200, 200, 256, 0.75, 12);
		RenderMeasurement uniformMeasurement = measureRender(line, uniform);
		RenderMeasurement adaptiveMeasurement = measureRender(line, adaptive);

		assertEquals(257, uniformMeasurement.vertices);
		assertEquals(5, adaptiveMeasurement.vertices);
		assertEquals(0, uniformMeasurement.warmEvaluations);
		assertEquals(0, adaptiveMeasurement.warmEvaluations);
		assertTrue(adaptiveMeasurement.coldEvaluations
				< uniformMeasurement.coldEvaluations);

		StablePathDomainProvider2D segment = StablePathDomainProvider2D.segment(
				"g6r-segment-native-t/v1", new LocusPoint2D(-2, 1),
				new LocusPoint2D(3, 6), 1E-14);
		List<Long> segmentTimes = new ArrayList<>();
		LocusPoint2D last = null;
		for (int run = 0; run < MEASURED_RUNS; run++) {
			long start = System.nanoTime();
			for (int index = 0; index < 4096; index++) {
				last = segment.evaluateDriverPoint(index / 4095.0);
			}
			segmentTimes.add(System.nanoTime() - start);
		}
		assertEquals(new LocusPoint2D(3, 6), last);
		Collections.sort(segmentTimes);
		System.out.println("G6R_METRIC case=BM-RENDER"
				+ " uniform_vertices=" + uniformMeasurement.vertices
				+ " adaptive_vertices=" + adaptiveMeasurement.vertices
				+ " uniform_cold_evaluations="
				+ uniformMeasurement.coldEvaluations
				+ " adaptive_cold_evaluations="
				+ adaptiveMeasurement.coldEvaluations
				+ " uniform_cold_ns=" + uniformMeasurement.coldNanos
				+ " adaptive_cold_ns=" + adaptiveMeasurement.coldNanos
				+ " uniform_warm_ns=" + uniformMeasurement.warmNanos
				+ " adaptive_warm_ns=" + adaptiveMeasurement.warmNanos);
		System.out.println("G6R_METRIC case=BM-SEGMENT-PROVIDER evaluations=4096"
				+ distribution(segmentTimes));
	}

	private void measureNested(Chain chain, boolean memoizing) {
		List<Long> times = new ArrayList<>();
		for (int run = 0; run < MEASURED_RUNS; run++) {
			reset(chain);
			LocusEvaluationSession2D session = memoizing
					? LocusEvaluationSession2D.memoizing(1024)
					: LocusEvaluationSession2D.reference();
			long start = System.nanoTime();
			evaluateBatch(chain.outer(), session);
			times.add(System.nanoTime() - start);
			assertEquals((long) QUERY_COUNT * chain.loci.size(), calls(chain));
			assertForbiddenWorkIsZero(chain);
		}
		Collections.sort(times);
		System.out.println("G6R_METRIC case=BM-NESTED-" + chain.loci.size()
				+ " session=" + (memoizing ? "ON" : "OFF")
				+ " queries=" + QUERY_COUNT
				+ " evaluator_calls=" + calls(chain) + distribution(times));
	}

	private RenderMeasurement measureRender(GeoLocusV2 locus,
			LocusRenderPolicy2D policy) {
		locus.getInstrumentation().reset();
		LocusRenderCache2D cache = new LocusRenderCache2D();
		long coldStart = System.nanoTime();
		LocusRenderData2D data = cache.getOrBuild(locus, policy);
		long coldNanos = System.nanoTime() - coldStart;
		long coldEvaluations = locus.getInstrumentation().getRenderEvaluations();
		locus.getInstrumentation().reset();
		long warmStart = System.nanoTime();
		cache.getOrBuild(locus, policy);
		long warmNanos = System.nanoTime() - warmStart;
		long warmEvaluations = locus.getInstrumentation().getRenderEvaluations();
		assertEquals(1, cache.getHits());
		assertEquals(1, cache.getMisses());
		return new RenderMeasurement(data.getVertices().size(), coldEvaluations,
				warmEvaluations, coldNanos, warmNanos);
	}

	private Chain chain(int depth) {
		ExplicitNumericDomainProvider2D provider = provider();
		LocusBranch2D branch = branch(provider,
				Collections.singletonList(provider.getDeclaredDomain()));
		List<LocusBranch2D> branches = Collections.singletonList(branch);
		GeoNumeric source = new GeoNumeric(getConstruction(), 1);
		List<GeoLocusV2> loci = new ArrayList<>();
		GeoLocusV2 current = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "g6r-benchmark-L1-" + depth + "-"
						+ source.hashCode(), source, provider, branches,
				(value, semanticBranch, parameter, session) ->
						new LocusPoint2D(value + parameter, parameter * parameter),
				"g6r-benchmark-leaf/v1");
		loci.add(current);
		for (int level = 2; level <= depth; level++) {
			final int capturedLevel = level;
			current = LocusV2Factory.createNested(LocusV2Mode.V2,
					getConstruction(), "g6r-benchmark-L" + level + "-" + depth
							+ "-" + source.hashCode(), current, BRANCH_KEY, provider,
					branches, parameter -> parameter / 2,
					(parameter, upstream) -> new LocusPoint2D(
							upstream.getX() + capturedLevel * parameter,
							upstream.getY() - parameter),
					"g6r-benchmark-nested-" + level + "/v1");
			loci.add(current);
		}
		return new Chain(source, loci);
	}

	private GeoLocusV2 lineLocus() {
		ExplicitNumericDomainProvider2D provider = provider();
		return LocusV2Factory.createAnalytic(LocusV2Mode.V2, getConstruction(),
				"g6r-render-benchmark", new GeoNumeric(getConstruction(), 0),
				provider, Collections.singletonList(branch(provider,
						Collections.singletonList(provider.getDeclaredDomain()))),
				(source, semanticBranch, parameter, session) ->
						new LocusPoint2D(parameter, 2 * parameter),
				"g6r-render-benchmark/v1");
	}

	private static LocusBranchSnapshot2D componentSnapshot(
			LocusSourceSnapshot2D sources,
			ExplicitNumericDomainProvider2D provider) {
		double gap = Math.sqrt(Math.max(0, sources.get(0)));
		List<LocusInterval2D> components = gap == 0
				? Collections.singletonList(provider.getDeclaredDomain())
				: Arrays.asList(new LocusInterval2D(-1, -gap, true, true),
						new LocusInterval2D(gap, 1, true, true));
		return new LocusBranchSnapshot2D(DefinitionStatus.VALID,
				Collections.singletonList(branch(provider, components)));
	}

	private static LocusBranch2D branch(
			ExplicitNumericDomainProvider2D provider,
			List<LocusInterval2D> components) {
		return new LocusBranch2D(BRANCH_KEY, provider.getDeclaredDomain(),
				components, Orientation.INCREASING, "g6r-benchmark/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
	}

	private static ExplicitNumericDomainProvider2D provider() {
		return new ExplicitNumericDomainProvider2D("g6r-benchmark-parameter/v1",
				new LocusInterval2D(-1, 1, true, true), Orientation.INCREASING,
				false, 1E-14);
	}

	private static void evaluateBatch(GeoLocusV2 locus,
			LocusEvaluationSession2D session) {
		for (int index = 0; index < QUERY_COUNT; index++) {
			double parameter = -1 + 2 * index / (double) (QUERY_COUNT - 1);
			locus.evaluate(BRANCH_KEY, parameter, session);
		}
	}

	private static void reset(Chain chain) {
		for (GeoLocusV2 locus : chain.loci) {
			locus.getInstrumentation().reset();
		}
	}

	private static long calls(Chain chain) {
		long total = 0;
		for (GeoLocusV2 locus : chain.loci) {
			total += locus.getInstrumentation().getEvaluatorCalls();
		}
		return total;
	}

	private static void assertForbiddenWorkIsZero(Chain chain) {
		for (GeoLocusV2 locus : chain.loci) {
			LocusInstrumentation2D counters = locus.getInstrumentation();
			assertEquals(0, counters.getDependencySliceBuilds());
			assertEquals(0, counters.getDependencySliceSynchronizations());
			assertEquals(0, counters.getWholeLocusRegenerations());
			assertEquals(0, counters.getRenderEvaluations());
		}
	}

	private static long[] revisions(Chain chain) {
		long[] revisions = new long[chain.loci.size()];
		for (int index = 0; index < chain.loci.size(); index++) {
			revisions[index] = chain.loci.get(index).getSemanticRevision();
		}
		return revisions;
	}

	private static String distribution(List<Long> values) {
		return " min_ns=" + values.get(0)
				+ " median_ns=" + values.get(values.size() / 2)
				+ " max_ns=" + values.get(values.size() - 1);
	}

	private static final class Chain {
		private final GeoNumeric source;
		private final List<GeoLocusV2> loci;

		Chain(GeoNumeric source, List<GeoLocusV2> loci) {
			this.source = source;
			this.loci = loci;
		}

		GeoLocusV2 outer() {
			return loci.get(loci.size() - 1);
		}
	}

	private static final class RenderMeasurement {
		private final int vertices;
		private final long coldEvaluations;
		private final long warmEvaluations;
		private final long coldNanos;
		private final long warmNanos;

		RenderMeasurement(int vertices, long coldEvaluations,
				long warmEvaluations, long coldNanos, long warmNanos) {
			this.vertices = vertices;
			this.coldEvaluations = coldEvaluations;
			this.warmEvaluations = warmEvaluations;
			this.coldNanos = coldNanos;
			this.warmNanos = warmNanos;
		}
	}
}
