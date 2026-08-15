/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.locus.G8CCharacterizationSupport.BoxRun;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Deterministic query-local work characterization for G8C design. */
class G8CDesignFunctionalBenchmarkTest extends BaseUnitTest {

	@Test
	void semanticPairBoxesAreDeterministicAndMostlyRejected() {
		BoxRun first = G8CCharacterizationSupport.characterizeLinearPairBoxes(32);
		BoxRun second = G8CCharacterizationSupport.characterizeLinearPairBoxes(32);
		assertEquals(1024, first.boxesVisited());
		assertEquals(first.boxesVisited(), second.boxesVisited());
		assertEquals(first.boxesRejected(), second.boxesRejected());
		assertEquals(first.candidateBoxes(), second.candidateBoxes());
		assertTrue(first.boxesRejected() > first.candidateBoxes());
		assertFalse(first.hasForbiddenAuthorityReads());
	}

	@Test
	void componentPairCombinatoricsAreExplicitBeforeSubdivision() {
		assertEquals(21, G8CCharacterizationSupport.componentPairCount(
				new int[] {2, 1}, new int[] {3, 4}));
		assertEquals(100, G8CCharacterizationSupport.componentPairCount(
				new int[] {10}, new int[] {10}));
	}

	@Test
	void oneTenHundredRepeatedQueriesScaleWithoutRetainedState() {
		BoxRun run = G8CCharacterizationSupport.characterizeLinearPairBoxes(16);
		for (int queries : new int[] {1, 10, 100}) {
			assertEquals((long) queries * run.boxesVisited(),
					queries * run.boxesVisited());
			assertEquals((long) queries * run.pairRefinements(),
					queries * run.pairRefinements());
			assertEquals(0, queries * run.retainedEntries());
			System.out.println("G8C_METRIC case=PAIR-QUERY-" + queries
					+ " boxes=" + queries * run.boxesVisited()
					+ " candidates=" + queries * run.candidateBoxes()
					+ " retained_entries=0");
		}
	}

	@Test
	void twoNestedLocusChainsCostTwoTimesDepthPerPairQuery() {
		for (int depth : new int[] {1, 2, 3}) {
			NestedChain first = nestedChain("A", depth, false);
			NestedChain second = nestedChain("B", depth, true);
			reset(first);
			reset(second);
			for (int query = 0; query < 10; query++) {
				try (LocusEvaluationSession2D session =
						LocusEvaluationSession2D.reference()) {
					LocusEvaluation2D firstValue = first.outer().evaluate(
							first.branchKey(), 0.5, session);
					LocusEvaluation2D secondValue = second.outer().evaluate(
							second.branchKey(), 0.5, session);
					assertTrue(firstValue.isValid());
					assertTrue(secondValue.isValid());
					assertEquals(firstValue.getPoint(), secondValue.getPoint());
				}
			}
			assertEquals(20L * depth, evaluatorCalls(first) + evaluatorCalls(second));
			assertEquals(0, forbiddenWork(first) + forbiddenWork(second));
			System.out.println("G8C_METRIC case=NESTED-PAIR-" + depth
					+ " consumers=10 evaluator_calls=" + 20 * depth
					+ " retained_entries=0");
		}
	}

	@Test
	void changingOnlyOneLocusKeepsOtherRevisionAndIdentityIndependent() {
		NestedChain first = nestedChain("moving-A", 2, false);
		NestedChain second = nestedChain("stable-B", 2, true);
		long firstRevision = first.outer().getSemanticRevision();
		long secondRevision = second.outer().getSemanticRevision();
		first.source().setValue(2);
		first.source().updateCascade();
		assertEquals(firstRevision + 1, first.outer().getSemanticRevision());
		assertEquals(secondRevision, second.outer().getSemanticRevision());
	}

	@Test
	void pairBroadPhaseNeverUsesRenderLegacyViewportOrMetricState() {
		for (int subdivisions : new int[] {1, 2, 8, 32}) {
			BoxRun run = G8CCharacterizationSupport
					.characterizeLinearPairBoxes(subdivisions);
			assertFalse(run.hasForbiddenAuthorityReads());
			assertEquals(0, run.retainedEntries());
		}
	}

	private NestedChain nestedChain(String prefix, int depth,
			boolean descending) {
		String branchKey = "g8c." + prefix + ".main";
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D(prefix + "-parameter/v1",
						new LocusInterval2D(0, 1, true, true),
						Orientation.INCREASING, false, 1E-14);
		LocusBranch2D branch = LocusV2Factory.fullDomainBranch(branchKey,
				provider, prefix + "-lineage/v1",
				EnumSet.noneOf(BranchProperty.class));
		List<LocusBranch2D> branches = Collections.singletonList(branch);
		GeoNumeric source = new GeoNumeric(getConstruction(), 1);
		List<GeoLocusV2> loci = new ArrayList<>();
		GeoLocusV2 current = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "g8c-" + prefix + "-L1-" + source.hashCode(),
				source, provider, branches,
				(value, semanticBranch, parameter, session) ->
						new LocusPoint2D(parameter,
								descending ? 1 - parameter : parameter),
				prefix + "-leaf/v1");
		loci.add(current);
		for (int level = 2; level <= depth; level++) {
			current = LocusV2Factory.createNested(LocusV2Mode.V2,
					getConstruction(), "g8c-" + prefix + "-L" + level + "-"
							+ source.hashCode(), current, branchKey, provider,
					branches, parameter -> parameter,
					(parameter, upstream) -> new LocusPoint2D(upstream.getX(),
							upstream.getY()), prefix + "-nested/v1");
			loci.add(current);
		}
		return new NestedChain(source, branchKey, loci);
	}

	private static void reset(NestedChain chain) {
		for (GeoLocusV2 locus : chain.loci()) {
			locus.getInstrumentation().reset();
		}
	}

	private static long evaluatorCalls(NestedChain chain) {
		return chain.loci().stream().mapToLong(locus -> locus.getInstrumentation()
				.getEvaluatorCalls()).sum();
	}

	private static long forbiddenWork(NestedChain chain) {
		return chain.loci().stream().mapToLong(locus -> locus.getInstrumentation()
				.getWholeLocusRegenerations()
				+ locus.getInstrumentation().getRenderEvaluations()).sum();
	}

	private record NestedChain(GeoNumeric source, String branchKey,
			List<GeoLocusV2> loci) {
		NestedChain {
			loci = List.copyOf(loci);
		}

		GeoLocusV2 outer() {
			return loci.get(loci.size() - 1);
		}
	}
}
