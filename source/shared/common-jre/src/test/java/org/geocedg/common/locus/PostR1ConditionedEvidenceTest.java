/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleFunction;

import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCellKind2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D.ReportOutcome;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceUse2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedHypothesis2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSample2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSampler2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSpanClass2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversal2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversalPolicy2D;
import org.geocedg.common.kernel.locus.conditioned.LocusSelectionTestimony2D;
import org.junit.jupiter.api.Test;

/**
 * Post-R1, preparatory work of increment I0 of the conditioned use of Locus V2 (candidate
 * contract {@code post-r1-conditioned-use.md}, pending the normative approval of the
 * author): the conditioned evidence and its deterministic traversal on evaluators of known
 * truth, the adverse bench of the design. Passing the bench certifies nothing: several
 * cases assert that an event between nodes is <em>not</em> detected, which is the declared
 * limit of a finite traversal, and that a report to the producer then corrects the content.
 * No public operation reads this evidence yet.
 */
final class PostR1ConditionedEvidenceTest {
	private static final LocusConditionedTraversalPolicy2D POLICY =
			LocusConditionedTraversalPolicy2D.v1();
	private static final double MINIMUM_STEP = 1.0 / (1 << 20);

	@Test
	void aSmoothClosedCurveIsOneRegionOfSpansThatCertifiesNothing() {
		LocusConditionedEvidence2D evidence = traverse(PostR1ConditionedEvidenceTest::circle);
		assertPartition(evidence);
		assertEquals(128, evidence.getCells().size());
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			assertEquals(LocusConditionedCellKind2D.SPAN, cell.getKind());
			assertEquals(LocusConditionedSpanClass2D.MARGIN_ESTIMATED, cell.getSpanClass());
			assertTrue(cell.getChordDeviation() <= POLICY.getMaximumChordDeviation());
		}
		assertEquals(1, evidence.getRegions().size());
		assertTrue(evidence.inOneRegion(0.05, 0.95));
		assertEquals(LocusEvidenceNature2D.NUMERICAL_UNDER_HYPOTHESES, evidence.getNature());
		assertFalse(evidence.getNature().certifies());
		assertEquals(257, evidence.getWork());
	}

	@Test
	void c1AValidJumpIsIsolatedToTheMinimumStep() {
		LocusConditionedEvidence2D evidence = traverse(t -> {
			double phase = 2 * Math.PI * (t - 0.37);
			return valid(t, Math.atan2(Math.sin(phase), Math.cos(phase)), "a", 1);
		});
		assertPartition(evidence);
		List<LocusConditionedCell2D> bands = bands(evidence);
		assertEquals(1, bands.size());
		assertEquals(LocusConditionedCellKind2D.DISCONTINUITY_BAND, bands.get(0).getKind());
		assertTrue(bands.get(0).contains(0.87));
		assertEquals(MINIMUM_STEP, bands.get(0).getUpper() - bands.get(0).getLower(), 1e-15);
		assertEquals(2, evidence.getRegions().size());
		assertFalse(evidence.inOneRegion(0.86, 0.88));
	}

	@Test
	void c2AValidityGapIsDetectedOnlyWhenANodeFallsInsideAndAReportCorrectsIt() {
		LocusConditionedEvidence2D wide = traverse(gap(0.4171, 1e-2));
		assertTrue(kinds(wide).contains(LocusConditionedCellKind2D.TRANSITION_BAND));
		assertTrue(kinds(wide).contains(LocusConditionedCellKind2D.ABSENT_BAND));
		assertFalse(wide.inOneRegion(0.41, 0.43));

		for (double width : new double[] { 1e-4, 1e-7 }) {
			LocusConditionedEvidenceOwner2D owner = owner();
			LocusConditionedEvidence2D missed = owner.startRevision(1, sampler(gap(0.4171, width)));
			// Declared limit: the event lies between nodes, so the span stands under H1.
			assertEquals(EnumSet.of(LocusConditionedCellKind2D.SPAN), kinds(missed));
			double inside = 0.4171 + width / 2;
			assertEquals(ReportOutcome.CONFIRMED, owner.report(inside));
			LocusConditionedEvidence2D corrected = owner.getPublished();
			assertPartition(corrected);
			for (LocusConditionedCell2D cell : corrected.cellsContaining(inside)) {
				assertNotEquals(LocusConditionedCellKind2D.SPAN, cell.getKind());
			}
			assertTrue(kinds(corrected).contains(LocusConditionedCellKind2D.TRANSITION_BAND));
			assertFalse(corrected.inOneRegion(0.41, 0.42));
			// The event is localized to the minimum step: the cell is split, not relabelled.
			assertTrue(bandWidth(corrected) <= width + 2 * MINIMUM_STEP,
					"band width " + bandWidth(corrected));
		}
	}

	@Test
	void c3ASelectionExchangeBetweenNodesIsNotDetectedUntilItIsReported() {
		LocusConditionedEvidenceOwner2D owner = owner();
		LocusConditionedEvidence2D missed = owner.startRevision(1, sampler(exchange()));
		assertEquals(EnumSet.of(LocusConditionedCellKind2D.SPAN), kinds(missed));
		assertEquals(ReportOutcome.CONFIRMED, owner.report(0.61131));
		LocusConditionedEvidence2D corrected = owner.getPublished();
		assertPartition(corrected);
		assertFalse(corrected.inOneRegion(0.61, 0.612));
		// One band of the minimum step at each exchange; the cells between them are spans of
		// the other selection.
		List<LocusConditionedCell2D> bands = bands(corrected);
		assertEquals(2, bands.size());
		for (LocusConditionedCell2D band : bands) {
			assertEquals(LocusConditionedCellKind2D.SELECTION_BAND, band.getKind());
			assertEquals(MINIMUM_STEP, band.getUpper() - band.getLower(), 1e-15);
		}
		assertTrue(bands.get(0).contains(0.6113) && bands.get(1).contains(0.6113 + 3e-5));
	}

	@Test
	void c4AMarginObservedAtTheNodesIsAnEstimateNeverABound() {
		LocusConditionedEvidence2D evidence = traverse(t -> {
			double dip = (t - 0.2931) / 2e-4;
			return valid(Math.cos(2 * Math.PI * t), Math.sin(2 * Math.PI * t), "a",
					1 - Math.exp(-dip * dip));
		});
		for (LocusConditionedCell2D cell : evidence.cellsContaining(0.2931)) {
			assertEquals(LocusConditionedSpanClass2D.MARGIN_ESTIMATED, cell.getSpanClass());
		}
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			assertNotEquals(LocusConditionedSpanClass2D.MARGIN_BOUNDED, cell.getSpanClass());
		}
	}

	@Test
	void c5AFailureIsNotAnAbsenceAndItsInteriorIsNotSubdivided() {
		LocusConditionedEvidence2D evidence = traverse(t -> t > 0.55 && t < 0.58
				? LocusConditionedSample2D.failed("work limit of the slice") : circle(t));
		assertPartition(evidence);
		assertFalse(kinds(evidence).contains(LocusConditionedCellKind2D.ABSENT_BAND));
		for (LocusConditionedCell2D cell : evidence.cellsBetween(0.551, 0.579)) {
			assertEquals(LocusConditionedCellKind2D.FAILURE_BAND, cell.getKind());
		}
		assertTrue(evidence.getWork() < 400, "work " + evidence.getWork());
		assertFalse(evidence.inOneRegion(0.54, 0.59));
	}

	@Test
	void c6ANarrowExcursionBetweenNodesIsNotDetectedUntilItIsReported() {
		DoubleFunction<LocusConditionedSample2D> spike = t -> {
			double s = (t - 0.5013) / 1e-5;
			return valid(t, 5 * Math.exp(-s * s), "a", 1);
		};
		LocusConditionedEvidenceOwner2D owner = owner();
		LocusConditionedEvidence2D missed = owner.startRevision(1, sampler(spike));
		assertEquals(EnumSet.of(LocusConditionedCellKind2D.SPAN), kinds(missed));
		assertEquals(32, missed.getCells().size());
		assertTrue(missed.inOneRegion(0.4, 0.6));
		assertEquals(ReportOutcome.CONFIRMED, owner.report(0.5013));
		LocusConditionedEvidence2D corrected = owner.getPublished();
		assertPartition(corrected);
		assertFalse(corrected.inOneRegion(0.4, 0.6));
		assertTrue(bandWidth(corrected) < 1e-3, "band width " + bandWidth(corrected));
		assertTrue(corrected.inOneRegion(0.4, 0.5));
	}

	@Test
	void theContentIsAFunctionOfTheContradictionSetAndNeverOfItsOrder() {
		double[] holes = { 0.1337, 0.4171, 0.7713 };
		DoubleFunction<LocusConditionedSample2D> function = t -> {
			for (double hole : holes) {
				if (t > hole && t < hole + 1e-5) {
					return LocusConditionedSample2D.absent("no real intersection");
				}
			}
			return circle(t);
		};
		Double[] reports = { 0.133705, 0.417105, 0.771305 };
		LocusConditionedEvidence2D direct = LocusConditionedTraversal2D.traverse("main", 1, 0,
				1, POLICY, List.of(reports), sampler(function));
		assertPartition(direct);
		int[][] orders = { { 0, 1, 2 }, { 0, 2, 1 }, { 1, 0, 2 }, { 1, 2, 0 }, { 2, 0, 1 },
				{ 2, 1, 0 } };
		for (int[] order : orders) {
			LocusConditionedEvidenceOwner2D owner = owner();
			owner.startRevision(1, sampler(function));
			for (int index : order) {
				// Queries, a report that is not confirmed and a repeated report in between.
				owner.getPublished().cellsContaining(0.9);
				owner.getPublished().getRegions();
				assertEquals(ReportOutcome.NOT_CONFIRMED, owner.report(0.25));
				long before = owner.getPublicationCounter();
				String identity = owner.getPublished().getContentIdentity();
				assertEquals(ReportOutcome.CONFIRMED, owner.report(reports[index]));
				assertEquals(before + 1, owner.getPublicationCounter());
				assertNotEquals(identity, owner.getPublished().getContentIdentity());
				assertEquals(ReportOutcome.ALREADY_KNOWN, owner.report(reports[index]));
				assertEquals(before + 1, owner.getPublicationCounter());
			}
			LocusConditionedEvidence2D incremental = owner.getPublished();
			assertEquals(direct.getContentIdentity(), incremental.getContentIdentity());
			assertEquals(direct.getCells(), incremental.getCells());
			assertEquals(direct.getWork(), incremental.getWork());
			assertEquals(List.of(reports), incremental.getContradictions());
		}
	}

	@Test
	void aResultIsCurrentWhileTheCellsItUsedAreUnchanged() {
		DoubleFunction<LocusConditionedSample2D> function = gap(0.4171, 1e-5);
		LocusConditionedEvidenceOwner2D owner = owner();
		LocusConditionedEvidence2D first = owner.startRevision(1, sampler(function));
		LocusConditionedEvidenceUse2D away = new LocusConditionedEvidenceUse2D(first,
				first.cellsBetween(0.1, 0.2));
		LocusConditionedEvidenceUse2D across = new LocusConditionedEvidenceUse2D(first,
				first.cellsBetween(0.41, 0.42));
		assertTrue(away.isCurrent(first));
		assertTrue(across.isCurrent(first));

		assertEquals(ReportOutcome.CONFIRMED, owner.report(0.417105));
		LocusConditionedEvidence2D second = owner.getPublished();
		List<LocusConditionedCell2D> withdrawn = first.withdrawnBy(second);
		assertEquals(1, withdrawn.size());
		assertTrue(withdrawn.get(0).contains(0.417105));
		assertTrue(bandWidth(second) <= 1e-5 + 2 * MINIMUM_STEP);
		assertTrue(second.inOneRegion(0.4141, 0.417) && second.inOneRegion(0.4172, 0.4218));
		assertTrue(away.isCurrent(second));
		assertFalse(across.isCurrent(second));
		assertEquals(first.getContentIdentity(), away.getContentIdentity());

		// A new revision discards the evidence and the contradiction set of the previous one.
		LocusConditionedEvidence2D next = owner.startRevision(2, sampler(function));
		assertFalse(away.isCurrent(next));
		assertTrue(next.getContradictions().isEmpty());
		assertEquals(1, owner.getPublicationCounter());
	}

	@Test
	void aValidEvaluationInsideAnAbsenceOrAFailureBandIsConfirmedAndReclassified() {
		DoubleFunction<LocusConditionedSample2D> absence = t -> t > 0.3 && t < 0.5
				&& !(t > 0.39 && t < 0.3901)
						? LocusConditionedSample2D.absent("no real intersection") : circle(t);
		DoubleFunction<LocusConditionedSample2D> failure = t -> t > 0.55 && t < 0.58
				&& !(t > 0.565 && t < 0.5651)
						? LocusConditionedSample2D.failed("work limit of the slice") : circle(t);
		double[] islands = { 0.39005, 0.56505 };
		LocusConditionedCellKind2D[] before = { LocusConditionedCellKind2D.ABSENT_BAND,
				LocusConditionedCellKind2D.FAILURE_BAND };
		List<DoubleFunction<LocusConditionedSample2D>> functions = List.of(absence, failure);
		for (int i = 0; i < islands.length; i++) {
			LocusConditionedEvidenceOwner2D owner = owner();
			LocusConditionedEvidence2D missed = owner.startRevision(1,
					sampler(functions.get(i)));
			for (LocusConditionedCell2D cell : missed.cellsContaining(islands[i])) {
				assertEquals(before[i], cell.getKind());
			}
			assertEquals(ReportOutcome.CONFIRMED, owner.report(islands[i]));
			LocusConditionedEvidence2D corrected = owner.getPublished();
			assertPartition(corrected);
			for (LocusConditionedCell2D cell : corrected.cellsContaining(islands[i])) {
				assertEquals(LocusConditionedCellKind2D.SPAN, cell.getKind());
			}
		}
	}

	@Test
	void aSeparationBoundEstablishesNoSelectionAndGivesNoPermission() {
		// The roots stay separated on every cell while the dependency changes of root inside
		// one of them: the cell is of the bounded class and H2 is still a hypothesis.
		DoubleFunction<LocusConditionedSample2D> function = exchange();
		LocusConditionedSampler2D bounded = new LocusConditionedSampler2D() {
			@Override
			public LocusConditionedSample2D sample(double canonicalParameter) {
				return function.apply(canonicalParameter);
			}

			@Override
			public boolean boundsSeparation(double lower, double upper) {
				return true;
			}
		};
		LocusConditionedEvidenceOwner2D owner = owner();
		LocusConditionedEvidence2D evidence = owner.startRevision(1, bounded);
		List<LocusConditionedCell2D> cells = evidence.cellsContaining(0.61131);
		assertEquals(1, cells.size());
		assertEquals(LocusConditionedSpanClass2D.MARGIN_BOUNDED, cells.get(0).getSpanClass());
		Set<LocusConditionedHypothesis2D> open = cells.get(0).getSpanClass()
				.getOpenHypotheses();
		assertTrue(open.contains(LocusConditionedHypothesis2D.H2_SELECTION));
		for (LocusConditionedSpanClass2D each : LocusConditionedSpanClass2D.values()) {
			assertEquals(EnumSet.allOf(LocusConditionedHypothesis2D.class),
					each.getOpenHypotheses());
		}
		// The bound gives the cell no immunity: the exchange is a contradiction of H2.
		assertEquals(ReportOutcome.CONFIRMED, owner.report(0.61131));
		assertTrue(evidence.withdrawnBy(owner.getPublished()).contains(cells.get(0)));
	}

	@Test
	void absencesWithDifferentCausesAreNotOneConditionedAbsence() {
		LocusConditionedEvidence2D evidence = traverse(t -> {
			if (t > 0.2 && t <= 0.3) {
				return LocusConditionedSample2D.absent("no real intersection");
			}
			if (t > 0.3 && t < 0.4) {
				return LocusConditionedSample2D.absent("undefined input");
			}
			return circle(t);
		});
		assertPartition(evidence);
		List<LocusConditionedCell2D> meeting = evidence.cellsContaining(0.3);
		assertEquals(1, meeting.size());
		assertEquals(LocusConditionedCellKind2D.FAILURE_BAND, meeting.get(0).getKind());
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			if (cell.getKind() == LocusConditionedCellKind2D.ABSENT_BAND) {
				assertTrue(cell.getUpper() < 0.3 || cell.getLower() > 0.3);
			}
		}
	}

	@Test
	void theWorkLimitLeavesUnexploredCellsAndNeverSpans() {
		LocusConditionedTraversalPolicy2D scarce = new LocusConditionedTraversalPolicy2D(
				"test-policy/scarce-work", 5, 20, 1e-3, 0.35, 1e-3, 40);
		LocusConditionedEvidence2D first = LocusConditionedTraversal2D.traverse("main", 1, 0,
				1, scarce, Collections.emptyList(),
				sampler(PostR1ConditionedEvidenceTest::circle));
		LocusConditionedEvidence2D second = LocusConditionedTraversal2D.traverse("main", 1, 0,
				1, scarce, Collections.emptyList(),
				sampler(PostR1ConditionedEvidenceTest::circle));
		assertPartition(first);
		assertEquals(EnumSet.of(LocusConditionedCellKind2D.UNEXPLORED), kinds(first));
		assertTrue(first.getWork() <= 40);
		assertTrue(first.getRegions().isEmpty());
		assertEquals(first.getCells(), second.getCells());
	}

	@Test
	void theContentUnderAWorkLimitDoesNotDependOnWhatWasAlreadyEvaluated() {
		// A straight line with two holes between nodes. The work limit admits the refinement
		// of one confirmed contradiction and not of two. Each distinct node is charged once,
		// cached or not: the owner, which already holds the nodes that refined the first
		// hole, publishes for the set of two what a fresh traversal publishes, in either
		// order of confirmation.
		LocusConditionedTraversalPolicy2D scarce = new LocusConditionedTraversalPolicy2D(
				"test-policy/scarce-refinement", 5, 20, 1e-3, 0.35, 1e-3, 140);
		DoubleFunction<LocusConditionedSample2D> function = t -> (t > 0.1337 && t < 0.13371)
				|| (t > 0.7713 && t < 0.77131)
						? LocusConditionedSample2D.absent("no real intersection")
						: valid(t, 0, "a", 1);
		Double[] reports = { 0.133705, 0.771305 };
		LocusConditionedEvidence2D fresh = LocusConditionedTraversal2D.traverse("main", 1, 0,
				1, scarce, List.of(reports), sampler(function));
		assertPartition(fresh);
		assertTrue(kinds(fresh).contains(LocusConditionedCellKind2D.UNEXPLORED));
		for (int[] order : new int[][] { { 0, 1 }, { 1, 0 } }) {
			LocusConditionedEvidenceOwner2D owner = new LocusConditionedEvidenceOwner2D("main",
					0, 1, scarce);
			LocusConditionedEvidence2D first = owner.startRevision(1, sampler(function));
			assertEquals(65, first.getWork());
			assertEquals(ReportOutcome.CONFIRMED, owner.report(reports[order[0]]));
			assertFalse(kinds(owner.getPublished())
					.contains(LocusConditionedCellKind2D.UNEXPLORED),
					"work " + owner.getPublished().getWork());
			assertEquals(ReportOutcome.CONFIRMED, owner.report(reports[order[1]]));
			LocusConditionedEvidence2D incremental = owner.getPublished();
			assertEquals(fresh.getCells(), incremental.getCells());
			assertEquals(fresh.getWork(), incremental.getWork());
			assertEquals(fresh.getContentIdentity(), incremental.getContentIdentity());
		}
	}

	@Test
	void theEvidenceOfAnotherPolicyVersionHasAnotherIdentity() {
		LocusConditionedTraversalPolicy2D deeper = new LocusConditionedTraversalPolicy2D(
				"test-policy/initial-64", 6, 20, 1e-3, 0.35, 1e-3, 2048);
		LocusConditionedEvidence2D first = traverse(PostR1ConditionedEvidenceTest::circle);
		LocusConditionedEvidence2D second = LocusConditionedTraversal2D.traverse("main", 1, 0,
				1, deeper, Collections.emptyList(),
				sampler(PostR1ConditionedEvidenceTest::circle));
		assertNotEquals(first.getContentIdentity(), second.getContentIdentity());
		assertEquals(128, second.getCells().size());
	}

	private static LocusConditionedEvidence2D traverse(
			DoubleFunction<LocusConditionedSample2D> function) {
		return LocusConditionedTraversal2D.traverse("main", 1, 0, 1, POLICY,
				Collections.emptyList(), sampler(function));
	}

	private static LocusConditionedEvidenceOwner2D owner() {
		return new LocusConditionedEvidenceOwner2D("main", 0, 1, POLICY);
	}

	private static LocusConditionedSampler2D sampler(
			DoubleFunction<LocusConditionedSample2D> function) {
		return function::apply;
	}

	private static LocusConditionedSample2D circle(double t) {
		return valid(Math.cos(2 * Math.PI * t), Math.sin(2 * Math.PI * t), "a", 1);
	}

	private static LocusConditionedSample2D valid(double x, double y, String selection,
			double separation) {
		return LocusConditionedSample2D.valid(x, y, List.of(LocusSelectionTestimony2D
				.established("root-of-the-slice", LocusSelectionTestimony2D.Kind.LOCUS_ROOT_TOKEN,
						selection, separation)));
	}

	private static DoubleFunction<LocusConditionedSample2D> gap(double start, double width) {
		return t -> t > start && t < start + width
				? LocusConditionedSample2D.absent("no real intersection") : circle(t);
	}

	/** Two roots exchange and exchange back inside one cell; both keep a large margin. */
	private static DoubleFunction<LocusConditionedSample2D> exchange() {
		return t -> t > 0.6113 && t < 0.6113 + 3e-5
				? valid(-Math.cos(2 * Math.PI * t), -Math.sin(2 * Math.PI * t), "b", 1)
				: circle(t);
	}

	private static Set<LocusConditionedCellKind2D> kinds(LocusConditionedEvidence2D evidence) {
		EnumSet<LocusConditionedCellKind2D> kinds = EnumSet
				.noneOf(LocusConditionedCellKind2D.class);
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			kinds.add(cell.getKind());
		}
		return kinds;
	}

	private static List<LocusConditionedCell2D> bands(LocusConditionedEvidence2D evidence) {
		List<LocusConditionedCell2D> bands = new ArrayList<>();
		for (LocusConditionedCell2D cell : evidence.getCells()) {
			if (!cell.getKind().isSpan()) {
				bands.add(cell);
			}
		}
		return bands;
	}

	private static double bandWidth(LocusConditionedEvidence2D evidence) {
		double width = 0;
		for (LocusConditionedCell2D band : bands(evidence)) {
			width += band.getUpper() - band.getLower();
		}
		return width;
	}

	/** Every content is a partition of the declared domain: no cell is ever dropped. */
	private static void assertPartition(LocusConditionedEvidence2D evidence) {
		List<LocusConditionedCell2D> cells = evidence.getCells();
		assertEquals(0, cells.get(0).getLowerIndex());
		assertEquals(evidence.getDomainLower(), cells.get(0).getLower(), 0);
		for (int i = 1; i < cells.size(); i++) {
			assertEquals(cells.get(i - 1).getUpperIndex(), cells.get(i).getLowerIndex());
		}
		assertEquals(1L << 21, cells.get(cells.size() - 1).getUpperIndex());
		assertEquals(evidence.getDomainUpper(), cells.get(cells.size() - 1).getUpper(), 0);
	}
}
