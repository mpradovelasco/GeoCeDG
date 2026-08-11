/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Deterministic functional budgets plus informational nested timings. */
class LocusV2FunctionalBenchmarkTest extends BaseUnitTest {
	private static final String BRANCH_KEY = "benchmark.sheet.main";
	private static final int QUERY_COUNT = 64;
	private static final int MEASURED_RUNS = 5;

	@Test
	void nestedOneTwoThreeAndFiveHaveLinearFunctionalCost() {
		for (int depth : new int[] {1, 2, 3, 5}) {
			Chain chain = chain(depth);
			evaluateBatch(chain.outer(), LocusEvaluationSession2D.reference());
			List<Long> elapsed = new ArrayList<>();
			for (int run = 0; run < MEASURED_RUNS; run++) {
				reset(chain);
				LocusEvaluationSession2D session =
						LocusEvaluationSession2D.memoizing(512);
				long start = System.nanoTime();
				evaluateBatch(chain.outer(), session);
				elapsed.add(System.nanoTime() - start);
				assertEquals((long) QUERY_COUNT * depth, calls(chain));
				assertEquals((long) QUERY_COUNT * depth, session.getMisses());
				assertEquals(0, session.getHits());
				assertEquals(0, forbiddenWork(chain));
			}
			Collections.sort(elapsed);
			System.out.println("G6B_METRIC case=BM-NESTED-" + depth
					+ " outer_queries=" + QUERY_COUNT + " depth=" + depth
					+ " evaluator_calls=" + calls(chain)
					+ " cached_entries=" + QUERY_COUNT * depth
					+ " median_elapsed_ns=" + elapsed.get(MEASURED_RUNS / 2));
		}
	}

	@Test
	void scopedSessionEliminatesOnlyExactDuplicateRequests() {
		Chain chain = chain(3);
		reset(chain);
		LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(256);
		evaluateBatch(chain.outer(), session);
		evaluateBatch(chain.outer(), session);
		assertEquals((long) QUERY_COUNT * 3, calls(chain));
		assertEquals((long) QUERY_COUNT * 3, session.getMisses());
		assertEquals(QUERY_COUNT, session.getHits());
		assertEquals(QUERY_COUNT, duplicates(chain));
		assertEquals(0, session.getEvictions());
		assertEquals(QUERY_COUNT * 3, session.getCachedEntryCount());
		assertEquals(0, forbiddenWork(chain));
		System.out.println("G6B_METRIC case=BM-NESTED-3-DUPLICATES"
				+ " outer_queries=" + (2 * QUERY_COUNT)
				+ " evaluator_calls=" + calls(chain)
				+ " session_hits=" + session.getHits()
				+ " session_misses=" + session.getMisses()
				+ " retained_cache_entries=" + session.getCachedEntryCount());
	}

	private Chain chain(int depth) {
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D("benchmark-parameter/v1",
						new LocusInterval2D(-1, 1, true, true),
						Orientation.INCREASING, false, 1E-14);
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(BRANCH_KEY,
				provider, "nested-legacy-mechanism-reproduction/v1",
				EnumSet.noneOf(BranchProperty.class));
		List<LocusBranch2D> branches = Collections.singletonList(branch);
		GeoNumeric source = new GeoNumeric(getConstruction(), 1);
		List<GeoLocusV2> loci = new ArrayList<>();
		GeoLocusV2 current = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "benchmark-L1-" + depth + "-" + source.hashCode(),
				source, provider, branches,
				(value, semanticBranch, parameter, session) ->
						new LocusPoint2D(value + parameter, parameter * parameter),
				"benchmark-leaf/v1");
		loci.add(current);
		for (int level = 2; level <= depth; level++) {
			final int capturedLevel = level;
			current = LocusV2Factory.createNested(LocusV2Mode.V2,
					getConstruction(), "benchmark-L" + level + "-" + depth + "-"
							+ source.hashCode(), current, BRANCH_KEY, provider, branches,
					parameter -> parameter / 2,
					(parameter, upstream) -> new LocusPoint2D(
							upstream.getX() + capturedLevel * parameter,
							upstream.getY() - parameter),
					"benchmark-nested-level-" + level + "/v1");
			loci.add(current);
		}
		return new Chain(loci);
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
		long calls = 0;
		for (GeoLocusV2 locus : chain.loci) {
			calls += locus.getInstrumentation().getEvaluatorCalls();
		}
		return calls;
	}

	private static long duplicates(Chain chain) {
		long duplicates = 0;
		for (GeoLocusV2 locus : chain.loci) {
			duplicates += locus.getInstrumentation().getDuplicatedRequests();
		}
		return duplicates;
	}

	private static long forbiddenWork(Chain chain) {
		long forbidden = 0;
		for (GeoLocusV2 locus : chain.loci) {
			LocusInstrumentation2D counters = locus.getInstrumentation();
			forbidden += counters.getDependencySliceBuilds()
					+ counters.getDependencySliceSynchronizations()
					+ counters.getWholeLocusRegenerations()
					+ counters.getRenderEvaluations();
		}
		return forbidden;
	}

	private static final class Chain {
		private final List<GeoLocusV2> loci;

		Chain(List<GeoLocusV2> loci) {
			this.loci = loci;
		}

		GeoLocusV2 outer() {
			return loci.get(loci.size() - 1);
		}
	}
}
