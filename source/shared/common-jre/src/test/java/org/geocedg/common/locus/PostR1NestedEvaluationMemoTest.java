/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusNestedEvaluationMemo2D;
import org.geocedg.common.kernel.locus.LocusNestedEvaluationMemo2D.Mode;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Step C5 of the corrections after block 3B (decision DN-3 of the author, 2026-11-22): the
 * memo of nested semantic evaluations of one evaluator instance. The run without the memo
 * is the oracle: with the memo every observable result is the same bit for bit, fewer
 * nested evaluations are computed, the work budget is charged the same, a nested definition
 * that follows the outer driver is never served stale, and no memo is shared between
 * producers or kept across a revision.
 */
final class PostR1NestedEvaluationMemoTest extends G9U0PublicSurfaceTestBase {
	private static final double[] PARAMETERS = {2.6, 2.8, 3.0, 3.1, -3.0, -2.8, 2.9, 2.6, 3.0};

	@AfterEach
	void readTheModeFromTheProcessAgain() {
		LocusNestedEvaluationMemo2D.overrideMode(null);
	}

	/** A consumer whose slice intersects a nested locus that its driver does not change. */
	private GeoLocusV2 consumerOfAFixedNestedLocus(String label) {
		add("c=Circle((0,0),2)");
		add("E=Point(c)");
		add("QF=(1,0)");
		add("M=Midpoint(E,QF)");
		assertNotNull(add("L1=LocusV2(M,E)"));
		add("H=(4,0)");
		add("r=Ray(H,E)");
		moveOnCircle("E", 0, 0, 2, 3.0);
		GeoLocusIntersectionResult rich = add("v=Intersect(L1,r)");
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertTrue(roots.size() >= 1);
		assertNotNull(add("RA=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken()
				+ "\")"));
		add("ZP=Midpoint(RA,E)");
		GeoLocusV2 consumer = add(label + "=LocusV2(ZP,E)");
		assertNotNull(consumer);
		getKernel().updateConstruction(false);
		return consumer;
	}

	/** The composition of finding F8: the nested definition follows the outer driver. */
	private GeoLocusV2 consumerOfANestedLocusThatFollowsItsDriver() {
		add("c=Circle((0,0),2)");
		add("E2=Point(c)");
		add("k=Circle((0,0),0.6)");
		add("T=Point(k)");
		add("QO=(2,0)");
		add("QT=Midpoint(T,QO)");
		add("Mt=Midpoint(E2,QT)");
		assertNotNull(add("Lt=LocusV2(Mt,E2)"));
		add("HA=(4,0.1)");
		add("HB=(-3,0.2)");
		add("l=Line(HA,HB)");
		moveOnCircle("T", 0, 0, 0.6, 0.4);
		GeoLocusIntersectionResult rich = add("v=Intersect(Lt,l)");
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertEquals(2, roots.size());
		assertNotNull(add("RA=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken()
				+ "\")"));
		add("ZP=Midpoint(RA,T)");
		GeoLocusV2 consumer = add("Zt=LocusV2(ZP,T)");
		assertNotNull(consumer);
		getKernel().updateConstruction(false);
		return consumer;
	}

	private void moveOnCircle(String label, double cx, double cy, double radius, double angle) {
		GeoPoint point = (GeoPoint) getKernel().lookupLabel(label);
		point.setCoords(cx + radius * Math.cos(angle), cy + radius * Math.sin(angle), 1.0);
		point.updateCascade();
	}

	private static ReconstructibleLocusEvaluator2D evaluator(GeoLocusV2 locus) {
		return (ReconstructibleLocusEvaluator2D) locus.getSemanticDefinition()
				.getEvaluatorCapability();
	}

	/** Every observable of the consumer, as exact text. */
	private static List<String> observe(GeoLocusV2 consumer, double[] parameters) {
		List<String> seen = new ArrayList<>();
		for (double parameter : parameters) {
			LocusEvaluation2D evaluation = consumer.evaluate(BRANCH, parameter,
					LocusEvaluationSession2D.reference());
			seen.add(evaluation.isValid() ? Double.toHexString(evaluation.getPoint().getX())
					+ "," + Double.toHexString(evaluation.getPoint().getY())
					: String.valueOf(evaluation.getStatus()));
		}
		seen.add(String.valueOf(consumer.getSemanticDefinition().getBranch(BRANCH)
				.getExistenceStructure().getSemanticSignature()));
		LocusConditionedEvidence2D evidence = consumer.getConditionedEvidence(BRANCH);
		if (evidence != null) {
			seen.add(evidence.getContentIdentity());
			for (LocusConditionedCell2D cell : evidence.getCells()) {
				seen.add(cell.getKind() + ":" + cell.getSpanClass() + ":"
						+ Double.toHexString(cell.getLower()) + ":"
						+ Double.toHexString(cell.getUpper()));
			}
		}
		return seen;
	}

	@Test
	void theResultsAreTheSameBitForBitAndFewerNestedEvaluationsAreComputed() {
		LocusNestedEvaluationMemo2D.overrideMode(Mode.OFF);
		GeoLocusV2 oracle = consumerOfAFixedNestedLocus("Z");
		assertNull(evaluator(oracle).getNestedEvaluationMemo(), "no memo in the oracle");
		List<String> expected = observe(oracle, PARAMETERS);
		long computedWithout = oracle.getInstrumentation().getNestedEvaluatorCalls();
		long[] chargedWithout = charged(oracle);

		getKernel().clearConstruction(true);
		LocusNestedEvaluationMemo2D.overrideMode(Mode.ON);
		GeoLocusV2 consumer = consumerOfAFixedNestedLocus("Z");
		LocusNestedEvaluationMemo2D memo = evaluator(consumer).getNestedEvaluationMemo();
		assertNotNull(memo);
		assertEquals(expected, observe(consumer, PARAMETERS));
		assertTrue(memo.getHits() > 0, "requests of the nested locus are answered");
		assertTrue(memo.size() > 0 && memo.size() <= memo.getHits() + memo.size());
		long computedWith = consumer.getInstrumentation().getNestedEvaluatorCalls();
		assertTrue(computedWith < computedWithout, computedWith + " < " + computedWithout);
		// The work budget does not see the memo: a request that it answers is charged.
		long[] chargedWith = charged(consumer);
		assertEquals(chargedWithout[0], chargedWith[0], "charged requests");
		assertEquals(chargedWithout[1], chargedWith[1], "nested requests");
		assertTrue(chargedWith[1] > 0);
	}

	/** Requests charged to a root session, total and nested, over a fixed batch. */
	private static long[] charged(GeoLocusV2 consumer) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (double parameter : new double[] {2.7, 2.95, -2.9}) {
				consumer.evaluate(BRANCH, parameter, session);
			}
			return new long[] {session.getTotalChargedMisses(), session.getNestedMisses()};
		}
	}

	@Test
	void theVerificationModeRecomputesEveryAnsweredRequestAndFindsNoDifference() {
		LocusNestedEvaluationMemo2D.overrideMode(Mode.OFF);
		List<String> expected = observe(consumerOfAFixedNestedLocus("Z"), PARAMETERS);
		getKernel().clearConstruction(true);
		LocusNestedEvaluationMemo2D.overrideMode(Mode.VERIFY);
		GeoLocusV2 consumer = consumerOfAFixedNestedLocus("Z");
		assertEquals(expected, observe(consumer, PARAMETERS));
		LocusNestedEvaluationMemo2D memo = evaluator(consumer).getNestedEvaluationMemo();
		assertTrue(memo.getVerified() > 0, "stored results were compared");
		assertEquals(memo.getHits(), memo.getVerified(), "none was returned unverified");
	}

	@Test
	void aNestedDefinitionThatFollowsTheOuterDriverIsNeverServedStale() {
		double[] parameters = {0.4, 1.1, 2.0, -2.5, -1.2, 0.0, 2.9, 0.4, 2.0};
		LocusNestedEvaluationMemo2D.overrideMode(Mode.OFF);
		List<String> expected = observe(consumerOfANestedLocusThatFollowsItsDriver(),
				parameters);
		// Different nested functions at different samples of the outer driver.
		assertTrue(!expected.get(0).equals(expected.get(1)));
		getKernel().clearConstruction(true);
		LocusNestedEvaluationMemo2D.overrideMode(Mode.ON);
		assertEquals(expected, observe(consumerOfANestedLocusThatFollowsItsDriver(),
				parameters));
		getKernel().clearConstruction(true);
		LocusNestedEvaluationMemo2D.overrideMode(Mode.VERIFY);
		assertEquals(expected, observe(consumerOfANestedLocusThatFollowsItsDriver(),
				parameters));
	}

	@Test
	void aMemoBelongsToOneEvaluatorInstanceAndEndsWithItsRevision() {
		LocusNestedEvaluationMemo2D.overrideMode(Mode.ON);
		GeoLocusV2 consumer = consumerOfAFixedNestedLocus("Z");
		// A second producer over the same nested locus has a memo of its own.
		add("ZQ=Midpoint(RA,H)");
		GeoLocusV2 other = add("Y=LocusV2(ZQ,E)");
		assertNotNull(other);
		getKernel().updateConstruction(false);
		observe(consumer, PARAMETERS);
		observe(other, PARAMETERS);
		ReconstructibleLocusEvaluator2D first = evaluator(consumer);
		LocusNestedEvaluationMemo2D memo = first.getNestedEvaluationMemo();
		assertNotSame(memo, evaluator(other).getNestedEvaluationMemo());
		assertTrue(memo.size() > 0);
		// A real change of the nested function publishes another revision of the consumer,
		// with another evaluator instance and an empty memo of its own.
		long revision = consumer.getSemanticRevision();
		GeoPoint focus = (GeoPoint) lookup("QF");
		focus.setCoords(1.2, 0.1, 1.0);
		focus.updateCascade();
		getKernel().updateConstruction(false);
		assertTrue(consumer.getSemanticRevision() > revision);
		ReconstructibleLocusEvaluator2D next = evaluator(consumer);
		assertNotSame(first, next);
		assertNotSame(memo, next.getNestedEvaluationMemo());
		List<String> changed = observe(consumer, PARAMETERS);
		// And what it answers is what the oracle computes at the new configuration.
		getKernel().clearConstruction(true);
		LocusNestedEvaluationMemo2D.overrideMode(Mode.OFF);
		GeoLocusV2 oracle = consumerOfAFixedNestedLocus("Z");
		GeoPoint moved = (GeoPoint) lookup("QF");
		moved.setCoords(1.2, 0.1, 1.0);
		moved.updateCascade();
		getKernel().updateConstruction(false);
		List<String> expected = observe(oracle, PARAMETERS);
		assertEquals(expected.subList(0, PARAMETERS.length),
				changed.subList(0, PARAMETERS.length));
	}
}
