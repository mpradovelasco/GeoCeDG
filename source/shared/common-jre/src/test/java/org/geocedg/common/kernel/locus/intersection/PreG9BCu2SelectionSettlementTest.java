/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D.SettlementOutcome;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T11, normative precision D-CU2-T11-R1: the settlement of the inherited
 * selection of a reconstructed slice ({@code selection-settlement/v1}), the departure of a
 * copy from its first initialization through a point query, and the proof of nonexistence that
 * uses no unreproduced selection ({@code selection-independent-nonexistence/v1}), on synthetic
 * line-circle constructions whose live state is known: a settled permutation [0, 1] or
 * [1, 0], a first initialization pending in a gap, at a double root or on a segment. The
 * reproduction of the selection state is asserted on the accessors of both algorithms, never
 * inferred from coordinates. Matrix rows V-REV-EXACT-1 (synthetic part), V-SEL-1, V-SEL-6,
 * V-SEL-7, V-COINC-3, V-COINC-8 and V-PUB-3 (synthetic part).
 */
class PreG9BCu2SelectionSettlementTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";
	/** Double root of the exact synthetic critical configuration (matrix section 1). */
	private static final double CRITICAL = Math.atan2(4, 3);

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/**
	 * Contract steps 1 to 7: a live permutation [0, 1], a slice rebuilt with the driver in a
	 * gap and a copy that ended in its first initialization. The settlement uses the first
	 * auxiliary parameter, restores the exact driver state, reproduces both selection states
	 * on the accessors, and the certificate is the one obtained when the same configuration is
	 * rebuilt with the driver outside the gap (V-SEL-1).
	 */
	@Test
	void aCopyRebuiltInAGapIsSettledToTheLiveState() throws Exception {
		horizontal("(0,0)", 5);
		settledIntersection("m", "k", 0);
		add("ls=LocusV2(S,N)");
		AlgoIntersectLineConic live = liveIntersection("S");
		assertEquals("[0, 1]/f/p", state(live));
		moveDriver(driver(), Math.PI / 2);
		assertFalse(lookup("S").isDefined());
		Map<LocusCertificationWork2D, Long> before = counters("ls");
		move("C", 0, 0.5);
		ReconstructibleLocusEvaluator2D evaluator = evaluator("ls");
		assertEquals(SettlementOutcome.SETTLED, evaluator.getSettlementOutcome());
		assertEquals(1, evaluator.getSettlementParameters());
		assertTrue(evaluator.isReconstructionFaithful(), evaluator.getFidelityReport()
				.toString());
		assertEquals(state(live), state(copyIntersection("ls", "S")));
		assertEquals(1, delta(before, "ls", LocusCertificationWork2D.SELECTION_SETTLEMENT));
		assertEquals(1, delta(before, "ls",
				LocusCertificationWork2D.SELECTION_SETTLEMENT_PARAMETER));
		assertEquals(0, delta(before, "ls",
				LocusCertificationWork2D.SELECTION_SETTLEMENT_FAILURE));
		// Step 6: the copy's driver is back at the live position, bit for bit.
		GeoPoint copyDriver = (GeoPoint) copy("ls").lookupLabel("N");
		assertEquals(exact(driver()), exact(copyDriver));
		LocusExistenceStructure2D settled = structure("ls");
		assertEquals(Completeness.COMPLETE, settled.getCompleteness());
		// V-SEL-1: the same configuration rebuilt with the driver outside the gap.
		moveDriver(driver(), 0.1);
		assertTrue(lookup("S").isDefined());
		move("C", 0, 0.6);
		move("C", 0, 0.5);
		assertEquals(SettlementOutcome.NOT_NEEDED, evaluator("ls").getSettlementOutcome());
		assertEquals(settled.getSemanticSignature(), structure("ls").getSemanticSignature());
	}

	/**
	 * V-SEL-6: settlement only produces the identity permutation, so a live permutation [1, 0]
	 * whose slice is rebuilt in a gap fails closed with its typed outcome; nonexistence is not
	 * proved, because the intersection has roots elsewhere.
	 */
	@Test
	void aLivePermutationThatSettlementCannotProduceFailsClosed() throws Exception {
		add("C=(0,0)");
		add("k=Circle(C,5)");
		add("g1=Circle((0,0),20)");
		add("N=Point(g1)");
		add("F=(0,6)");
		add("m=Line(F,N)");
		moveDriver(driver(), Math.PI / 2);
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"S", "T"},
				(GeoLine) lookup("m"), (GeoConic) lookup("k"));
		// Half a turn reverses the direction of the line through F: S keeps raw root 1.
		moveDriver(driver(), -Math.PI / 2);
		AlgoIntersectLineConic live = liveIntersection("S");
		assertEquals("[1, 0]/f/p", state(live));
		add("ls=LocusV2(S,N)");
		assertTrue(evaluator("ls").isReconstructionFaithful());
		// The horizontal line through F misses the circle of radius 5.
		moveDriver(driver(), Math.asin(0.3));
		assertFalse(lookup("S").isDefined());
		move("C", 0, 0.1);
		ReconstructibleLocusEvaluator2D evaluator = evaluator("ls");
		assertEquals(SettlementOutcome.STATE_DIFFERS, evaluator.getSettlementOutcome());
		assertFalse(evaluator.isReconstructionFaithful());
		assertEquals("[1, 0]/f/p", state(live));
		assertEquals("[0, 1]/f/p", state(copyIntersection("ls", "S")));
		LocusExistenceStructure2D structure = structure("ls");
		assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness());
		assertTrue(structure.getCoverage().stream().allMatch(item -> item.getCause()
				== LocusCoverageCause2D.SELECTION_NOT_REPRODUCED), structure.toString());
		assertTrue(structure.getCoverage().get(0).getEvidence()
				.contains("selection-settlement/v1=state-differs"), structure.toString());
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
			LocusEvaluation2D evaluation = evaluator.evaluate(null, null, -Math.PI / 2,
					session);
			assertFalse(evaluation.isValid());
			assertTrue(evaluation.getDiagnostic().startsWith(
					"RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED"), evaluation.getDiagnostic());
		}
	}

	/**
	 * Contract step 5: a settlement never takes out of its first initialization a copy
	 * intersection whose live one is still in it; the outcome is typed and the selection
	 * fails closed.
	 */
	@Test
	void settlementNeverTakesAFirstPendingIntersectionOutOfIt() throws Exception {
		horizontal("(0,0)", 5);
		add("k2=Circle(C,3)");
		// y(N) = 20 sin(t): two roots on k only at t = asin(0.2).
		settledIntersection("m", "k", Math.asin(0.2));
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"U", "V"},
				(GeoLine) lookup("m"), (GeoConic) lookup("k2"));
		moveDriver(driver(), Math.asin(0.225));
		assertEquals("[0, 1]/f/p", state(liveIntersection("S")));
		assertTrue(liveIntersection("U").isSelectionFirstIntersectionPending());
		add("X=Midpoint(S,U)");
		add("lx=LocusV2(X,N)");
		assertTrue(evaluator("lx").isReconstructionFaithful());
		moveDriver(driver(), Math.PI / 2);
		move("C", 0, 0.25);
		ReconstructibleLocusEvaluator2D evaluator = evaluator("lx");
		assertEquals(SettlementOutcome.FIRST_PENDING_INITIALIZED,
				evaluator.getSettlementOutcome());
		assertFalse(evaluator.isReconstructionFaithful());
		assertTrue(liveIntersection("U").isSelectionFirstIntersectionPending());
		assertEquals(Completeness.NOT_ESTABLISHED, structure("lx").getCompleteness());
	}

	/**
	 * Contract step 8 and V-SEL-6: a document reopened with the driver in a gap has its live
	 * intersection in its first initialization, reproduced by the copy. The first query with
	 * two distinct roots takes the copy out of it, and the evaluator settles it at that
	 * parameter: the outputs do not depend on the order of the queries, a recapture types the
	 * pair as departed and never as reproduced, and no query is refused on unbounded paths.
	 */
	@Test
	void aCopyInItsFirstInitializationDepartsAtOneParameterInAnyQueryOrder() throws Exception {
		horizontal("(0,0)", 5);
		settledIntersection("m", "k", 0);
		add("ls=LocusV2(S,N)");
		moveDriver(driver(), Math.PI / 2);
		reload();
		assertTrue(liveIntersection("S").isSelectionFirstIntersectionPending());
		assertTrue(evaluator("ls").isReconstructionFaithful());
		assertEquals(SettlementOutcome.NOT_NEEDED, evaluator("ls").getSettlementOutcome());
		List<Double> parameters = new ArrayList<>();
		for (int index = 0; index < 64; index++) {
			parameters.add(-Math.PI + 2 * Math.PI * index / 64 + 0.001);
		}
		final Map<Double, String> forward = query("ls", parameters);
		ReconstructibleLocusEvaluator2D first = evaluator("ls");
		assertEquals(1, first.getDepartedSelections());
		assertEquals("[0, 1]/f/p", state(copyIntersection("ls", "S")));
		assertTrue(liveIntersection("S").isSelectionFirstIntersectionPending());
		first.captureCertifiedProgram();
		assertTrue(first.isReconstructionFaithful(), first.getFidelityReport().toString());
		assertEquals(List.of("S"), first.getFidelityReport().getDepartedSelections());
		assertEquals(0, locus("ls").getInstrumentation().getCertificationWork(
				LocusCertificationWork2D.SELECTION_DEPARTURE_REFUSAL));
		reload();
		List<Double> shuffled = new ArrayList<>(parameters);
		Collections.shuffle(shuffled, new Random(20261021));
		Map<Double, String> reordered = query("ls", shuffled);
		assertEquals(1, evaluator("ls").getDepartedSelections());
		assertEquals(forward, reordered);
		assertTrue(forward.values().stream().anyMatch(value -> !value.startsWith("invalid")),
				forward.toString());
	}

	/**
	 * Contract step 8 on a bounded path: after the departure, every valid output of the copy
	 * is, bit for bit, the output of the rule of the revision (the compaction that the live
	 * intersection computes in its first initialization), read on fresh documents; where the
	 * identity permutation differs, because the only root on the segment is raw root 1, the
	 * query fails explicitly.
	 */
	@Test
	void departedOutputsOnABoundedPathFollowTheRuleOrFailClosed() throws Exception {
		add("A=(-10,0)");
		add("g1=Circle((3,0),4)");
		add("N=Point(g1)");
		add("s=Segment(N,A)");
		add("k=Circle((0,0),5)");
		// N = (-1, 0) is inside the circle: one root on the segment, no initialization.
		moveDriver(driver(), Math.PI);
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"S", "T"},
				(GeoLine) lookup("s"), (GeoConic) lookup("k"));
		assertTrue(liveIntersection("S").isSelectionFirstIntersectionPending());
		String document = getApp().getXML();
		List<Double> parameters = new ArrayList<>();
		for (int index = 0; index < 48; index++) {
			parameters.add(-Math.PI + 2 * Math.PI * index / 48 + 0.002);
		}
		Map<Double, String> reference = new LinkedHashMap<>();
		for (double parameter : parameters) {
			getApp().setXML(document, true);
			moveDriver(driver(), parameter);
			reference.put(parameter, exact((GeoPoint) lookup("S")));
		}
		getApp().setXML(document, true);
		add("ls=LocusV2(S,N)");
		assertTrue(evaluator("ls").isReconstructionFaithful());
		List<Double> shuffled = new ArrayList<>(parameters);
		Collections.shuffle(shuffled, new Random(7));
		int refused = 0;
		int valid = 0;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
			for (double parameter : shuffled) {
				LocusEvaluation2D evaluation = evaluator("ls").evaluate(null, null, parameter,
						session);
				if (evaluation.isValid()) {
					valid++;
					assertEquals(reference.get(parameter), Double.toHexString(evaluation
							.getPoint().getX()) + "," + Double.toHexString(evaluation
									.getPoint().getY()), "t=" + parameter);
				} else if (evaluation.getDiagnostic().startsWith("SELECTION_NOT_REPRODUCED")) {
					refused++;
					assertNotEquals("undefined", reference.get(parameter), "t=" + parameter);
				} else {
					assertEquals("undefined", reference.get(parameter), "t=" + parameter + " "
							+ evaluation.getDiagnostic());
				}
			}
		}
		assertEquals(1, evaluator("ls").getDepartedSelections());
		assertTrue(valid > 0);
		assertTrue(refused > 0);
		// The counter also counts the queries of the view that draws the locus.
		assertTrue(locus("ls").getInstrumentation().getCertificationWork(
				LocusCertificationWork2D.SELECTION_DEPARTURE_REFUSAL) >= refused);
	}

	/**
	 * GC-1 and GC-3 after a settlement: moving only the driver, in and out of the gap,
	 * publishes no revision, rebuilds nothing, settles nothing and computes no certificate
	 * (each recompute only computes the fingerprint that proves it equivalent); a sequence of
	 * point queries does none of these.
	 */
	@Test
	void driverMovesAndQueriesDoNotRebuildSettleOrRecertify() throws Exception {
		horizontal("(0,0)", 5);
		settledIntersection("m", "k", 0);
		add("ls=LocusV2(S,N)");
		moveDriver(driver(), Math.PI / 2);
		move("C", 0, 0.5);
		assertEquals(SettlementOutcome.SETTLED, evaluator("ls").getSettlementOutcome());
		long revision = locus("ls").getSemanticRevision();
		long builds = locus("ls").getInstrumentation().getDependencySliceBuilds();
		Map<LocusCertificationWork2D, Long> before = counters("ls");
		double[] moves = {0.1, 1.5, 3.0, -0.2, Math.PI / 2};
		for (double parameter : moves) {
			moveDriver(driver(), parameter);
		}
		assertEquals(moves.length, delta(before, "ls",
				LocusCertificationWork2D.FINGERPRINT_COMPUTATION));
		Map<LocusCertificationWork2D, Long> moved = counters("ls");
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (int index = 0; index < 64; index++) {
				locus("ls").getSemanticDefinition().evaluate(BRANCH,
						-Math.PI + 2 * Math.PI * index / 64, session);
			}
		}
		assertEquals(revision, locus("ls").getSemanticRevision());
		assertEquals(builds, locus("ls").getInstrumentation().getDependencySliceBuilds());
		assertEquals(moved, counters("ls"));
		moved.remove(LocusCertificationWork2D.FINGERPRINT_COMPUTATION);
		before.remove(LocusCertificationWork2D.FINGERPRINT_COMPUTATION);
		assertEquals(before, moved);
	}

	/**
	 * V-SEL-7 and V-COINC-8 after a settlement: reopening, undo and redo read the selection
	 * state again; the copy reproduces the reloaded live state, here a first initialization
	 * in the gap, without a settlement, and with the same certificate.
	 */
	@Test
	void theSettledStateIsReadAgainOnReopenUndoAndRedo() throws Exception {
		activateUndo();
		horizontal("(0,0)", 5);
		settledIntersection("m", "k", 0);
		add("ls=LocusV2(S,N)");
		moveDriver(driver(), Math.PI / 2);
		getApp().storeUndoInfo();
		move("C", 0, 0.5);
		getApp().storeUndoInfo();
		assertEquals(SettlementOutcome.SETTLED, evaluator("ls").getSettlementOutcome());
		String settled = structure("ls").getSemanticSignature();
		getKernel().undo();
		assertReproduced("undo");
		getKernel().redo();
		assertReproduced("redo");
		assertEquals(settled, structure("ls").getSemanticSignature());
		reload();
		assertReproduced("reopen");
		assertEquals(settled, structure("ls").getSemanticSignature());
	}

	/**
	 * V-PUB-3 (synthetic part) and D-CU2-8: after a dynamic change of a non-driver input that
	 * leaves no root anywhere, settlement cannot reproduce the live permutation, and the proof
	 * that uses no unreproduced selection publishes the locus valid and COMPLETE with no
	 * component, its intersection with a line a complete empty set and its total length zero;
	 * the copy is never declared faithful. Opened in that state, the ordinary certificate
	 * proves the same; moved back, the settlement reproduces the selection again.
	 */
	@Test
	void aGloballyEmptyDomainIsPublishedCompleteAfterADynamicChange() throws Exception {
		horizontal("(0,0)", 5);
		settledIntersection("m", "k", 0);
		add("ls=LocusV2(S,N)");
		moveDriver(driver(), Math.PI / 2);
		move("C", 0, 30);
		ReconstructibleLocusEvaluator2D evaluator = evaluator("ls");
		assertEquals(SettlementOutcome.BUDGET_EXHAUSTED, evaluator.getSettlementOutcome());
		assertEquals(ReconstructibleLocusEvaluator2D.SETTLEMENT_PARAMETERS,
				evaluator.getSettlementParameters());
		assertFalse(evaluator.isReconstructionFaithful());
		assertEmpty(LocusCoverageCause2D.SELECTION_INDEPENDENT_NONEXISTENCE);
		assertTrue(structure("ls").getCoverage().get(0).getEvidence().contains(
				HybridExistenceCertifier2D.SELECTION_INDEPENDENT_CONTRACT));
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
			assertFalse(evaluator.evaluate(null, null, 0, session).isValid());
		}
		reload();
		assertTrue(evaluator("ls").isReconstructionFaithful());
		assertEmpty(LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE);
		getKernel().clearConstruction(true);
		horizontal("(0,0)", 5);
		settledIntersection("m", "k", 0);
		add("ls=LocusV2(S,N)");
		moveDriver(driver(), Math.PI / 2);
		move("C", 0, 30);
		move("C", 0, 0.5);
		assertEquals(SettlementOutcome.SETTLED, evaluator("ls").getSettlementOutcome());
		assertEquals(Completeness.COMPLETE, structure("ls").getCompleteness());
		assertFalse(structure("ls").getContinuousValidComponents().isEmpty());
	}

	/**
	 * No extrapolation: two roots exist only on two parameter sets narrower than the spacing
	 * of the auxiliary parameters. The settlement misses them, and the proof of nonexistence
	 * fails on the boxes that contain them: the revision stays NOT_ESTABLISHED.
	 */
	@Test
	void partialExistenceThatSettlementMissesStaysNotEstablished() throws Exception {
		// Roots where |20 sin t - 0.6| <= 0.05, around t = 0.03 and t = pi - 0.03.
		horizontal("(0,0.6)", 0.05);
		settledIntersection("m", "k", 0.03);
		add("ls=LocusV2(S,N)");
		moveDriver(driver(), Math.PI / 2);
		move("C", 0, 0.6 + 1e-3);
		ReconstructibleLocusEvaluator2D evaluator = evaluator("ls");
		assertEquals(SettlementOutcome.BUDGET_EXHAUSTED, evaluator.getSettlementOutcome());
		LocusExistenceStructure2D structure = structure("ls");
		assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness());
		assertTrue(structure.getCoverage().stream().allMatch(item -> item.getCause()
				== LocusCoverageCause2D.SELECTION_NOT_REPRODUCED), structure.toString());
	}

	/**
	 * V-COINC-3: the intersection created with the driver exactly at the double root of the
	 * synthetic critical configuration stays in its first initialization, which the copy
	 * reproduces; queries in any order give the same outputs; the first live movement that
	 * separates the roots publishes a new revision with the same components.
	 */
	@Test
	void aDoubleRootAtTheFirstInitializationKeepsItsRuleInAnyQueryOrder() throws Exception {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45),40)");
		add("N=Point(g1)");
		moveDriver(driver(), CRITICAL);
		add("t=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		assertTrue(liveIntersection("Q").isSelectionFirstIntersectionPending());
		assertTrue(evaluator("lq").isReconstructionFaithful());
		CertifiedConstructionProgram2D program =
				evaluator("lq").captureCertifiedProgramV3().orElseThrow();
		assertEquals(1, program.getSteps().get(1).getParameter(0), "compaction rule");
		final String components = components("lq");
		List<Double> parameters = new ArrayList<>();
		for (int index = 0; index < 32; index++) {
			parameters.add(-Math.PI + 2 * Math.PI * index / 32 + 0.003);
		}
		String document = getApp().getXML();
		Map<Double, String> forward = query("lq", parameters);
		getApp().setXML(document, true);
		List<Double> reversed = new ArrayList<>(parameters);
		Collections.reverse(reversed);
		assertEquals(forward, query("lq", reversed));
		long revision = locus("lq").getSemanticRevision();
		moveDriver(driver(), CRITICAL + 0.1);
		moveDriver(driver(), CRITICAL + 0.2);
		assertTrue(locus("lq").getSemanticRevision() > revision);
		assertEquals("[0, 1]/f/p", state(liveIntersection("Q")));
		assertTrue(evaluator("lq").isReconstructionFaithful());
		assertEquals(components, components("lq"));
	}

	private void assertEmpty(LocusCoverageCause2D cause) {
		GeoLocusV2 locus = locus("ls");
		assertTrue(locus.isDefined());
		LocusExistenceStructure2D structure = structure("ls");
		assertEquals(Completeness.COMPLETE, structure.getCompleteness(), structure.toString());
		assertTrue(structure.getContinuousValidComponents().isEmpty());
		assertTrue(structure.getPointComponents().isEmpty());
		assertTrue(structure.getCoverage().stream().allMatch(item -> item.getState()
				== CoverageState.CERTIFIED_DOES_NOT_EXIST && item.getCause() == cause),
				structure.toString());
		add("cut" + cause.ordinal() + ":y=x");
		GeoLocusIntersectionResult cut = (GeoLocusIntersectionResult) add(
				"emptyCut" + cause.ordinal() + "=Intersect(ls,cut" + cause.ordinal() + ")");
		assertEquals("EMPTY", cut.getIntersectionResult().getGeometryKind().name());
		assertEquals(Completeness.COMPLETE.name(), cut.getIntersectionResult()
				.getCompletenessEvidence().getCompleteness().name());
		GeoLocusMetricResult length = (GeoLocusMetricResult) add(
				"emptyLength" + cause.ordinal() + "=LocusLength(ls)");
		assertEquals(0.0, length.getMetricResult().getMetricValue().getFiniteValue()
				.orElse(-1), length.getMetricResult().toString());
	}

	/**
	 * The copy reproduces the reloaded live state, or a point query has taken it out of a
	 * first initialization that it reproduced, which the report types as a departure.
	 */
	private void assertReproduced(String step) throws Exception {
		AlgoIntersectLineConic live = liveIntersection("S");
		ReconstructibleLocusEvaluator2D evaluator = evaluator("ls");
		evaluator.captureCertifiedProgram();
		assertTrue(evaluator.isReconstructionFaithful(), step + " "
				+ evaluator.getFidelityReport());
		String copyState = state(copyIntersection("ls", "S"));
		if (!state(live).equals(copyState)) {
			assertTrue(live.isSelectionFirstIntersectionPending(), step);
			assertEquals("[0, 1]/f/p", copyState, step);
			assertEquals(List.of("S"), evaluator.getFidelityReport().getDepartedSelections(),
					step);
		}
		assertEquals(Completeness.COMPLETE, structure("ls").getCompleteness(), step);
	}

	/** A horizontal line through the driver and a circle of the given centre and radius. */
	private void horizontal(String centre, double radius) {
		add("C=" + centre);
		add("k=Circle(C," + radius + ")");
		add("g1=Circle((0,0),20)");
		add("N=Point(g1)");
		add("m=Line(N,N+(1,0))");
	}

	/** Creates S and T at a driver parameter with two roots and settles them nearby. */
	private void settledIntersection(String line, String conic, double parameter) {
		moveDriver(driver(), parameter);
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"S", "T"},
				(GeoLine) lookup(line), (GeoConic) lookup(conic));
		moveDriver(driver(), parameter + 1e-4);
		assertFalse(liveIntersection("S").isSelectionPermutationPending());
	}

	private Map<Double, String> query(String label, List<Double> parameters)
			throws Exception {
		Map<Double, String> values = new java.util.TreeMap<>();
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
			for (double parameter : parameters) {
				LocusEvaluation2D evaluation = evaluator(label).evaluate(null, null, parameter,
						session);
				values.put(parameter, evaluation.isValid()
						? Double.toHexString(evaluation.getPoint().getX()) + ","
								+ Double.toHexString(evaluation.getPoint().getY())
						: "invalid " + evaluation.getStatus());
			}
		}
		return values;
	}

	private String components(String label) {
		StringBuilder text = new StringBuilder();
		for (LocusExistenceStructure2D.ContinuousComponent component
				: structure(label).getContinuousValidComponents()) {
			text.append(component.getInterval()).append(';');
		}
		return text.toString();
	}

	private Map<LocusCertificationWork2D, Long> counters(String label) {
		Map<LocusCertificationWork2D, Long> counters = new LinkedHashMap<>();
		for (LocusCertificationWork2D kind : LocusCertificationWork2D.values()) {
			counters.put(kind, locus(label).getInstrumentation().getCertificationWork(kind));
		}
		return counters;
	}

	private long delta(Map<LocusCertificationWork2D, Long> before, String label,
			LocusCertificationWork2D kind) {
		return locus(label).getInstrumentation().getCertificationWork(kind) - before.get(kind);
	}

	private void move(String label, double x, double y) {
		GeoPoint point = (GeoPoint) lookup(label);
		point.setCoords(x, y, 1);
		point.updateCascade();
	}

	private GeoPoint driver() {
		return (GeoPoint) lookup("N");
	}

	private static void moveDriver(GeoPoint driver, double parameter) {
		driver.getPathParameter().setT(parameter);
		driver.getPath().pathChanged(driver);
		driver.updateCoords();
		driver.updateCascade();
	}

	private static String exact(GeoPoint point) {
		if (!point.isDefined()) {
			return "undefined";
		}
		return point.getPath() == null
				? Double.toHexString(point.getInhomX()) + "," + Double.toHexString(
						point.getInhomY())
				: Double.toHexString(point.x) + "," + Double.toHexString(point.y) + ","
						+ Double.toHexString(point.z) + ",t=" + Double.toHexString(
								point.getPathParameter().getT());
	}

	private static String state(AlgoIntersectLineConic intersection) {
		return Arrays.toString(intersection.getSelectionPermutation()) + "/"
				+ (intersection.isSelectionFirstIntersectionPending() ? "F" : "f") + "/"
				+ (intersection.isSelectionPermutationPending() ? "P" : "p");
	}

	private AlgoIntersectLineConic liveIntersection(String output) {
		return (AlgoIntersectLineConic) lookup(output).getParentAlgorithm();
	}

	private AlgoIntersectLineConic copyIntersection(String label, String output)
			throws Exception {
		GeoElement copy = copy(label).lookupLabel(output);
		return (AlgoIntersectLineConic) copy.getParentAlgorithm();
	}

	private Construction copy(String label) throws Exception {
		Field field = ReconstructibleLocusEvaluator2D.class
				.getDeclaredField("isolatedConstruction");
		field.setAccessible(true);
		return (Construction) field.get(evaluator(label));
	}

	private LocusExistenceStructure2D structure(String label) {
		return locus(label).getSemanticDefinition().getBranch(BRANCH).getExistenceStructure();
	}

	private GeoLocusV2 locus(String label) {
		return assertInstanceOf(GeoLocusV2.class, lookup(label), label);
	}

	private ReconstructibleLocusEvaluator2D evaluator(String label) throws Exception {
		Field field = AlgoDependentPointLocusV2.class.getDeclaredField("evaluator");
		field.setAccessible(true);
		return assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
				field.get(locus(label).getParentAlgorithm()), label);
	}
}
