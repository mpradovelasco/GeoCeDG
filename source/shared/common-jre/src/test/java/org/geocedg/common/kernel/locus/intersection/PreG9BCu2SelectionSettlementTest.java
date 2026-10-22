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
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusBoundaryImage2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusSingularParameter2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D.SettlementOutcome;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
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
 * V-SEL-7, V-COINC-3, V-COINC-8 and V-PUB-3 (synthetic part); corrective descendant T13-R2
 * (decision D-CU2-T13-R2): V-COINC-6 and V-COINC-9, whose roots are checked against an
 * extended-precision reference computed from the captured driver.
 */
class PreG9BCu2SelectionSettlementTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";
	/** Double root of the exact synthetic critical configuration (matrix section 1). */
	private static final double CRITICAL = Math.atan2(4, 3);
	/** Loci of the two variants: labelled outputs Q and R, unlabelled second output R2. */
	private static final List<String> VARIANTS = List.of("lq", "lr", "lr2");
	private static final List<String> OUTPUTS = List.of("Q", "R", "R2");
	/** Root that the certified selection designates for each output, by ray parameter. */
	private static final List<Integer> ROOTS = List.of(0, 1, 1);
	/** Outputs compared where the binary64 driver leaves no real root (V-COINC-9). */
	private int rootless;

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

	/**
	 * V-COINC-6: both variants of the synthetic critical configuration (radius 40) and of a
	 * bite (radius {@code 40 + 2^-10}), started with the driver at the parameter of the double
	 * root, where the unlabelled output R2 has no valid sample (suppressed at the double root
	 * of the critical configuration) and, in the gap of the bite, no output has one. Started by
	 * loading the construction with the driver there, and reopened again, both intersections
	 * stay in their first initialization, which each copy reproduces with no settlement or
	 * leaves with a typed departure; the rule captured is the compaction in raw order, the rule
	 * of the same variants created with the driver outside (the permutation [0, 1], both outputs
	 * labelled or both unlabelled) in its other mode; the certificates are those of the
	 * variants created outside, bit for bit and durable identities included (the kinds of
	 * V-COINC-1 and V-COINC-2). Created by command with the driver there, the unlabelled
	 * variant captures the same compaction, and the loci of both variants created there on
	 * intersections created outside settle to their permutation; their certificates are the
	 * same up to the durable identities of the new geos, named by their labels or algorithms.
	 * The labelled pair itself is not created there by command, which labels its first output
	 * only: the label of the second stays pending until that output is defined.
	 */
	@Test
	void variantsCreatedWithoutIntersectionCaptureTheSameCertificates() throws Exception {
		for (String radius : List.of("40", "40+2^(-10)")) {
			variants(radius);
			assertTrue(lookup("R2").isDefined(), radius);
			assertFalse(liveIntersection("Q").isSelectionFirstIntersectionPending(), radius);
			Map<String, String> reference = certificates();
			Map<String, String> named = new LinkedHashMap<>();
			Map<String, String> rules = new LinkedHashMap<>();
			for (String label : VARIANTS) {
				named.put(label, named(reference.get(label)));
				rules.put(label, evaluator(label).captureCertifiedProgramV3().orElseThrow()
						.getSelectionSignature());
			}
			assertEquals("2LINE_CIRCLE[0x0.0p0,0x0.0p0,0x1.0p0,0x1.0p0,0x1.0p0];"
					+ "3INTERSECTION_OUTPUT[0x0.0p0];", rules.get("lq"), radius);
			assertEquals("2LINE_CIRCLE[0x0.0p0,0x0.0p0,0x1.0p0,0x1.0p0,0x1.0p0];"
					+ "3INTERSECTION_OUTPUT[0x1.0p0];", rules.get("lr"), radius);
			assertEquals("2LINE_CIRCLE[0x0.0p0,0x0.0p0,0x1.0p0,0x0.0p0,0x0.0p0];"
					+ "3INTERSECTION_OUTPUT[0x1.0p0];", rules.get("lr2"), radius);

			moveDriver(driver(), CRITICAL);
			reload();
			for (String pass : List.of("started", "reopened")) {
				String context = radius + " " + pass + " at the double root";
				assertEquals("40".equals(radius), lookup("Q").isDefined(), context);
				assertFalse(lookup("R2").isDefined(), context);
				assertTrue(liveIntersection("Q").isSelectionFirstIntersectionPending(), context);
				assertTrue(liveSource("R2").isSelectionFirstIntersectionPending(), context);
				for (String label : VARIANTS) {
					assertCaptured(context, label, SettlementOutcome.NOT_NEEDED,
							compaction(rules.get(label)));
				}
				assertEquals(reference, certificates(), context);
				reload();
			}
			getKernel().clearConstruction(true);

			String context = radius + " lr2 created at the double root";
			configuration(radius);
			moveDriver(driver(), CRITICAL);
			add("t2=Ray(P,N)");
			add("R2=Intersect(p,t2,2)");
			add("lr2=LocusV2(R2,N)");
			assertFalse(lookup("R2").isDefined(), context);
			assertTrue(liveSource("R2").isSelectionFirstIntersectionPending(), context);
			assertCaptured(context, "lr2", SettlementOutcome.NOT_NEEDED,
					compaction(rules.get("lr2")));
			assertEquals(named.get("lr2"), named(certificate("lr2")), context);
			getKernel().clearConstruction(true);

			context = radius + " loci created at the double root";
			configuration(radius);
			moveDriver(driver(), 0.3);
			add("t=Ray(P,N)");
			getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
					(GeoLine) lookup("t"), (GeoConic) lookup("p"));
			add("t2=Ray(P,N)");
			add("R2=Intersect(p,t2,2)");
			moveDriver(driver(), CRITICAL);
			assertFalse(lookup("R2").isDefined(), context);
			for (String label : VARIANTS) {
				add(label + "=LocusV2(" + OUTPUTS.get(VARIANTS.indexOf(label)) + ",N)");
				assertCaptured(context, label, SettlementOutcome.SETTLED, rules.get(label));
				assertEquals(named.get(label), named(certificate(label)), context + " " + label);
			}
			getKernel().clearConstruction(true);
		}
	}

	/**
	 * The copy of the locus, validated again, is faithful with the settlement outcome; it
	 * reproduces the selection state of the live intersection, or a point query of the
	 * certification took it out of the first initialization that it reproduced, which the
	 * report types as a departure named by the first output of the intersection; and the rule
	 * captured for the revision is the given one.
	 */
	private void assertCaptured(String context, String label, SettlementOutcome outcome,
			String rule) throws Exception {
		String where = context + " " + label;
		ReconstructibleLocusEvaluator2D evaluator = evaluator(label);
		// Validates the fidelity again on the current copy, as assertReproduced does.
		evaluator.captureCertifiedProgram();
		assertTrue(evaluator.isReconstructionFaithful(), where + " "
				+ evaluator.getFidelityReport());
		assertEquals(outcome, evaluator.getSettlementOutcome(), where);
		String output = OUTPUTS.get(VARIANTS.indexOf(label));
		boolean indexed = "lr2".equals(label);
		AlgoIntersectLineConic live = indexed ? liveSource(output) : liveIntersection(output);
		AlgoIntersectLineConic copy = indexed ? copySource(label, output)
				: copyIntersection(label, output);
		if (!state(live).equals(state(copy))) {
			assertTrue(live.isSelectionFirstIntersectionPending(), where);
			assertEquals("[0, 1]/f/p", state(copy), where);
			assertEquals(List.of(indexed ? output + "/source" : "Q"),
					evaluator.getFidelityReport().getDepartedSelections(), where);
		}
		assertEquals(rule, evaluator.captureCertifiedProgramV3()
				.map(CertifiedConstructionProgram2D::getSelectionSignature).orElse(null), where);
	}

	/**
	 * @return the rule with its line-circle step in compaction mode, the raw order and the
	 *         label state unchanged
	 */
	private static String compaction(String rule) {
		String compaction = rule.replace("LINE_CIRCLE[0x0.0p0,", "LINE_CIRCLE[0x1.0p0,");
		assertNotEquals(rule, compaction, rule);
		return compaction;
	}

	/**
	 * V-COINC-9 (configurations of V-COINC-1 to V-COINC-8: the critical radius and the bite of
	 * radius {@code 40 + 2^-30}, both variants, created with both roots at the driver,
	 * recomputed, undone and redone, and started again by loading with the driver there or at
	 * the double root, where the intersections stay in their first initialization): in every
	 * sample outside the unresolved bands, the operative output of the live construction and of
	 * the slice is the root that the certified selection designates, Q the one with the smaller
	 * ray parameter and R the larger, checked against an extended-precision reference computed
	 * from the captured binary64 driver; where the two reference roots are closer than the
	 * error of a binary64 computation, the output is that coincident root, and where the
	 * binary64 driver leaves no real root (rounding at the critical radius, the gap of the bite)
	 * a defined output is the foot of the double root, the inherited tangency window (finding
	 * F-CU2-1). The samples include offsets of 10^-7 to 10^-2 from the double root, where the
	 * roots separate. Where the certificate states existence the slice output is defined, an
	 * undefined slice output is never published as a point, and no point is published where it
	 * states nonexistence. The certified limit value of the coincident root, the limit form of
	 * the singular parameter of each labelled variant, encloses the exact double root
	 * {@code (15, 20)} with no tolerance and is evidence only: the unlabelled output, whose root
	 * the certificate keeps inside its band, gets no limit value and stays undefined there.
	 */
	@Test
	void theOperativeOutputIsTheRootThatTheCertifiedSelectionDesignates() throws Exception {
		activateUndo();
		List<String> problems = new ArrayList<>();
		int compared = 0;
		for (String radius : List.of("40", "40+2^(-30)")) {
			variants(radius);
			// A free number outside the construction, whose change is undone and redone.
			add("spare=1");
			getApp().storeUndoInfo();
			for (String state : List.of("created", "recomputed", "undone", "redone",
					"started at 0.3", "started at the double root")) {
				if ("recomputed".equals(state)) {
					getKernel().updateConstruction(false);
				} else if ("undone".equals(state)) {
					GeoNumeric number = (GeoNumeric) lookup("spare");
					number.setValue(2);
					number.updateCascade();
					getApp().storeUndoInfo();
					getKernel().undo();
					assertEquals(1, ((GeoNumeric) lookup("spare")).getValue(), state);
				} else if ("redone".equals(state)) {
					getKernel().redo();
					assertEquals(2, ((GeoNumeric) lookup("spare")).getValue(), state);
				} else if (state.startsWith("started")) {
					moveDriver(driver(), state.endsWith("0.3") ? 0.3 : CRITICAL);
					reload();
				}
				String context = radius + " " + state;
				compared += designations(context, problems);
				if ("40".equals(radius)) {
					limitValueIsEvidenceOnly(context, problems);
				}
			}
			getKernel().clearConstruction(true);
		}
		assertEquals(List.of(), problems);
		assertTrue(compared > 4000, "compared outputs: " + compared);
		assertTrue(rootless > 100, "compared outputs without a real root: " + rootless);
	}

	/**
	 * Both variants of the synthetic critical configuration: the labelled outputs Q and R of a
	 * ray through the driver, and the unlabelled second output R2 of another ray, created with
	 * the driver at {@code t = 0.3}, where both roots exist.
	 */
	private void variants(String radius) {
		configuration(radius);
		moveDriver(driver(), 0.3);
		add("t=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		add("lr=LocusV2(R,N)");
		add("t2=Ray(P,N)");
		add("R2=Intersect(p,t2,2)");
		add("lr2=LocusV2(R2,N)");
		getKernel().updateConstruction(false);
	}

	/** The circle p, the point P and the driver N on the circle g1 of the given radius. */
	private void configuration(String radius) {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45)," + radius + ")");
		add("N=Point(g1)");
	}

	/** @return structure, roots and exact evidence of each variant */
	private Map<String, String> certificates() {
		Map<String, String> certificates = new LinkedHashMap<>();
		for (String label : VARIANTS) {
			certificates.put(label, certificate(label));
		}
		return certificates;
	}

	/** @return structure, roots and exact evidence of the certificate of the locus */
	private String certificate(String label) {
		HybridExistenceCertifier2D.Result result = ((AlgoDependentPointLocusV2) locus(label)
				.getParentAlgorithm()).getHybridExistenceCertificate();
		StringBuilder text = new StringBuilder(result.getStructure().getSemanticSignature());
		for (HybridExistenceCertifier2D.CertifiedRoot root : result.getRoots()) {
			text.append("|root=").append(root.getSignature());
		}
		for (HybridExistenceCertifier2D.ExactEvidence evidence : result.getExactEvidence()) {
			text.append("|exact=").append(evidence.getSignature());
		}
		return text.toString();
	}

	/**
	 * @return the text with every durable identity, which a new construction allocates anew,
	 *         replaced by the label of its geo, or by the algorithm, labelled inputs and output
	 *         index of an unlabelled one
	 */
	private String named(String text) {
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		Matcher matcher = Pattern.compile("geo:[0-9a-f]{32}").matcher(text);
		StringBuilder named = new StringBuilder();
		while (matcher.find()) {
			GeoElement geo = registry.getGeo(PersistentGeoId.parse(matcher.group()));
			String name = matcher.group();
			if (geo != null && geo.isLabelSet()) {
				name = geo.getLabelSimple();
			} else if (geo != null && geo.getParentAlgorithm() != null) {
				AlgoElement parent = geo.getParentAlgorithm();
				StringBuilder algorithm = new StringBuilder(parent.getClass().getSimpleName())
						.append('(');
				for (GeoElement input : parent.getInput()) {
					algorithm.append(input.isLabelSet() ? input.getLabelSimple() : "?")
							.append(',');
				}
				name = algorithm.append(")#").append(Arrays.asList(parent.getOutput())
						.indexOf(geo)).toString();
			}
			matcher.appendReplacement(named, Matcher.quoteReplacement(name));
		}
		matcher.appendTail(named);
		return named.toString();
	}

	/** @return number of compared outputs; records every output that is not designated */
	private int designations(String context, List<String> problems) throws Exception {
		int compared = 0;
		for (int variant = 0; variant < VARIANTS.size(); variant++) {
			String label = VARIANTS.get(variant);
			LocusExistenceStructure2D structure = structure(label);
			ReconstructibleLocusEvaluator2D slice = evaluator(label);
			try (LocusEvaluationSession2D copySession = LocusEvaluationSession2D.memoizing(8);
					LocusEvaluationSession2D publishedSession =
							LocusEvaluationSession2D.memoizing(8)) {
				for (double parameter : samples()) {
					CoverageState state = structure.getCoverageAt(parameter).getState();
					if (state == CoverageState.UNRESOLVED) {
						continue;
					}
					moveDriver(driver(), parameter);
					Reference reference = Reference.of(driver());
					String where = context + " " + label + " t=" + parameter;
					GeoPoint live = (GeoPoint) lookup(OUTPUTS.get(variant));
					LocusEvaluation2D copy = slice.evaluate(null, null, parameter, copySession);
					int checked = reference.check(where + " live", live.isDefined()
							? new double[] {live.getInhomX(), live.getInhomY()} : null,
							ROOTS.get(variant), problems)
							+ reference.check(where + " slice", copy.isValid()
									? new double[] {copy.getPoint().getX(), copy.getPoint().getY()}
									: null, ROOTS.get(variant), problems);
					compared += checked;
					rootless += reference.hasRoots() ? 0 : checked;
					boolean published = locus(label).getSemanticDefinition().evaluate(BRANCH,
							parameter, publishedSession).isValid();
					if (state == CoverageState.CERTIFIED_EXISTS && !copy.isValid()) {
						problems.add(where + ": certified existence without a slice output");
					}
					if (!copy.isValid() && published) {
						problems.add(where + ": an undefined slice output is published");
					}
					if (state == CoverageState.CERTIFIED_DOES_NOT_EXIST && published) {
						problems.add(where + ": a point is published where nonexistence is"
								+ " certified");
					}
				}
			}
		}
		return compared;
	}

	/**
	 * At the coincident root of the critical configuration the certified limit value is
	 * evidence: for each labelled variant the exact certificate states a singular parameter of
	 * multiplicity 2, whose limit form encloses, with no tolerance, the exact double root
	 * {@code (15, 20)} (matrix section 1) and the 60-digit feet of the double root at the
	 * binary64 ends of the parameter enclosure, computed from the captured constants. The
	 * certificate of the unlabelled second output places the root inside its unresolved band
	 * ({@code locus-v2-certified-construction-model.md} section 10.3): it states no singular
	 * parameter and no limit form of that output there, the output stays undefined, and neither
	 * the slice nor the published evaluation replaces it.
	 */
	private void limitValueIsEvidenceOnly(String context, List<String> problems)
			throws Exception {
		moveDriver(driver(), CRITICAL);
		for (String label : List.of("lq", "lr")) {
			List<LocusSingularParameter2D> singular = structure(label).getSingularParameters();
			if (singular.size() != 1 || singular.get(0).getMultiplicity() != 2
					|| !singular.get(0).getNature().certifies()
					|| !(singular.get(0).getParameterLower() <= CRITICAL
							&& CRITICAL <= singular.get(0).getParameterUpper())) {
				problems.add(context + " " + label + ": no certified coincident root "
						+ singular);
				continue;
			}
			LocusBoundaryImage2D image = locus(label).getSemanticDefinition()
					.encloseSingularImage(BRANCH, singular.get(0));
			List<BigDecimal[]> points = new ArrayList<>();
			points.add(new BigDecimal[] {BigDecimal.valueOf(15), BigDecimal.valueOf(20)});
			double[] constants = captured(label);
			points.add(foot(constants, singular.get(0).getParameterLower()));
			points.add(foot(constants, singular.get(0).getParameterUpper()));
			for (BigDecimal[] point : points) {
				if (!image.isEnclosed() || !encloses(image, point)) {
					problems.add(context + " " + label + ": the limit value " + image
							+ " does not enclose (" + point[0] + ", " + point[1] + ")");
				}
			}
		}
		HybridExistenceCertifier2D.Result result = ((AlgoDependentPointLocusV2) locus("lr2")
				.getParentAlgorithm()).getHybridExistenceCertificate();
		int registered = 0;
		for (HybridExistenceCertifier2D.CertifiedRoot root : result.getRoots()) {
			if (root.getLower() <= CRITICAL && CRITICAL <= root.getUpper()) {
				registered += root.isInsideBand() && root.getMultiplicity() == 2 ? 1 : 0;
				LocusBoundaryImage2D image = evaluator("lr2").encloseBoundaryImage(null, null,
						root.getLower(), root.getUpper(), root.getSource());
				if (image.isEnclosed()) {
					problems.add(context + ": the suppressed output has a limit value " + image);
				}
			}
		}
		if (registered != 1 || !structure("lr2").getSingularParameters().isEmpty()) {
			problems.add(context + ": the coincident root of lr2 is not only evidence inside"
					+ " its band " + result.getRoots());
		}
		if (lookup("R2").isDefined()) {
			problems.add(context + ": R2 is defined at the coincident root");
		}
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
			if (evaluator("lr2").evaluate(null, null, CRITICAL, session).isValid()
					|| locus("lr2").getSemanticDefinition().evaluate(BRANCH, CRITICAL, session)
							.isValid()) {
				problems.add(context + ": the limit value replaces an undefined output");
			}
		}
	}

	/**
	 * Uniform samples of the circle, 201 samples within 10^-8 of the double root and samples at
	 * 10^-7 to 10^-2 on both sides of it, where the roots separate.
	 */
	private static List<Double> samples() {
		List<Double> samples = new ArrayList<>();
		for (int index = 0; index < 64; index++) {
			samples.add(-Math.PI + 2 * Math.PI * (index + 0.5) / 64);
		}
		for (int index = -100; index <= 100; index++) {
			samples.add(CRITICAL + index * 1e-10);
		}
		for (double offset : new double[] {1e-7, 1e-6, 3e-6, 1e-5, 1e-4, 1e-3, 1e-2}) {
			samples.add(CRITICAL - offset);
			samples.add(CRITICAL + offset);
		}
		return samples;
	}

	/**
	 * @return the constants that the class-v3 program of the locus captured
	 *         ({@code locus-v2-certified-construction-model.md} section 10.6): the driver
	 *         circle by its half axes, eigenvectors and midpoint (0 to 7), the point P (8, 9)
	 *         and the circle p by its matrix entries A2, A4 and A5 (10 to 12)
	 */
	private double[] captured(String label) throws Exception {
		CertifiedConstructionProgram2D program = evaluator(label).captureCertifiedProgramV3()
				.orElseThrow();
		double[] constants = new double[13];
		for (int index = 0; index < 8; index++) {
			constants[index] = program.getDriverConstant(index);
		}
		int found = 0;
		for (CertifiedConstructionProgram2D.Step step : program.getSteps()) {
			List<CertifiedConstructionProgram2D.Operand> operands = step.getOperands();
			if (step.getOperation() == CertifiedConstructionProgram2D.Operation.RAY
					&& operands.get(0).getKind()
							== CertifiedConstructionProgram2D.OperandKind.POINT) {
				constants[8] = operands.get(0).getValue(3);
				constants[9] = operands.get(0).getValue(4);
				found++;
			} else if (step.getOperation()
					== CertifiedConstructionProgram2D.Operation.LINE_CIRCLE
					&& operands.get(1).getKind()
							== CertifiedConstructionProgram2D.OperandKind.CIRCLE) {
				for (int index = 0; index < 3; index++) {
					constants[10 + index] = operands.get(1).getValue(index);
				}
				found++;
			}
		}
		assertEquals(2, found, program.getSignature());
		return constants;
	}

	/**
	 * @return the 60-digit foot of the perpendicular from the centre of p to the line from P
	 *         through the driver at {@code t}, the limit of a double root, from the captured
	 *         constants and the 60-digit cosine and sine of the binary64 parameter
	 */
	private static BigDecimal[] foot(double[] constants, double t) {
		MathContext context = Reference.MC;
		BigDecimal[] k = new BigDecimal[constants.length];
		for (int index = 0; index < constants.length; index++) {
			k[index] = new BigDecimal(constants[index]);
		}
		// The driver as GeoConicND.pathChangedWithoutCheckEllipse places it.
		BigDecimal along = k[0].multiply(series(new BigDecimal(t), true), context);
		BigDecimal across = k[1].multiply(series(new BigDecimal(t), false), context);
		BigDecimal dx = along.multiply(k[2], context).add(across.multiply(k[4], context), context)
				.add(k[6], context).subtract(k[8], context);
		BigDecimal dy = along.multiply(k[3], context).add(across.multiply(k[5], context), context)
				.add(k[7], context).subtract(k[9], context);
		// The circle x^2 + y^2 + 2 A4 x + 2 A5 y + A2 = 0 has the centre (-A4, -A5).
		BigDecimal wx = k[8].add(k[11], context);
		BigDecimal wy = k[9].add(k[12], context);
		BigDecimal lambda = dx.multiply(wx, context).add(dy.multiply(wy, context), context)
				.negate().divide(dx.multiply(dx, context).add(dy.multiply(dy, context), context),
						context);
		return new BigDecimal[] {k[8].add(lambda.multiply(dx, context), context),
			k[9].add(lambda.multiply(dy, context), context)};
	}

	private static BigDecimal series(BigDecimal x, boolean cosine) {
		MathContext context = Reference.MC;
		BigDecimal square = x.multiply(x, context);
		BigDecimal term = cosine ? BigDecimal.ONE : x;
		BigDecimal sum = term;
		BigDecimal limit = new BigDecimal("1e-70");
		for (int k = cosine ? 2 : 3; term.abs().compareTo(limit) > 0; k += 2) {
			term = term.multiply(square, context).negate()
					.divide(BigDecimal.valueOf((long) k * (k - 1)), context);
			sum = sum.add(term, context);
		}
		return sum;
	}

	/** @return whether the image encloses the point, with no tolerance */
	private static boolean encloses(LocusBoundaryImage2D image, BigDecimal[] point) {
		return new BigDecimal(image.getXLower()).compareTo(point[0]) <= 0
				&& point[0].compareTo(new BigDecimal(image.getXUpper())) <= 0
				&& new BigDecimal(image.getYLower()).compareTo(point[1]) <= 0
				&& point[1].compareTo(new BigDecimal(image.getYUpper())) <= 0;
	}

	/**
	 * Extended-precision roots of the ray from P = (-25, 50) through the captured binary64
	 * driver with the circle of centre (0, 0) and radius 25, in increasing ray parameter, and
	 * the foot of the perpendicular from the centre, the limit of a double root.
	 */
	private static final class Reference {
		private static final MathContext MC = new MathContext(60, RoundingMode.HALF_EVEN);
		/** Error bound of a binary64 computation of a root of this configuration. */
		private static final double TOLERANCE = 1e-6;
		private final double[][] roots;
		private final double[] foot;

		private Reference(double[][] roots, double[] foot) {
			this.roots = roots;
			this.foot = foot;
		}

		static Reference of(GeoPoint driver) {
			BigDecimal px = BigDecimal.valueOf(-25);
			BigDecimal py = BigDecimal.valueOf(50);
			BigDecimal dx = new BigDecimal(driver.getInhomX()).subtract(px, MC);
			BigDecimal dy = new BigDecimal(driver.getInhomY()).subtract(py, MC);
			BigDecimal a = dx.multiply(dx, MC).add(dy.multiply(dy, MC), MC);
			BigDecimal b = px.multiply(dx, MC).add(py.multiply(dy, MC), MC)
					.multiply(BigDecimal.valueOf(2), MC);
			BigDecimal c = px.multiply(px).add(py.multiply(py)).subtract(BigDecimal.valueOf(625));
			BigDecimal discriminant = b.multiply(b, MC).subtract(a.multiply(c, MC)
					.multiply(BigDecimal.valueOf(4), MC), MC);
			BigDecimal twice = a.multiply(BigDecimal.valueOf(2), MC);
			BigDecimal limit = b.negate().divide(twice, MC);
			double[] foot = {px.add(limit.multiply(dx, MC), MC).doubleValue(),
				py.add(limit.multiply(dy, MC), MC).doubleValue()};
			if (discriminant.signum() < 0) {
				return new Reference(null, foot);
			}
			BigDecimal root = discriminant.sqrt(MC);
			BigDecimal[] lambdas = {b.negate().subtract(root, MC).divide(twice, MC),
					b.negate().add(root, MC).divide(twice, MC)};
			double[][] points = new double[2][];
			for (int index = 0; index < 2; index++) {
				points[index] = new double[] {
						px.add(lambdas[index].multiply(dx, MC), MC).doubleValue(),
						py.add(lambdas[index].multiply(dy, MC), MC).doubleValue(),
						lambdas[index].doubleValue()};
			}
			return new Reference(points, foot);
		}

		/** @return 1 when an output was compared with the designated root, else 0 */
		int check(String where, double[] output, int designated, List<String> problems) {
			if (output == null) {
				return 0;
			}
			if (roots == null) {
				// No real root at the binary64 driver: a defined output is the coincident root
				// that the inherited tangency window gives (finding F-CU2-1), the foot.
				if (distance(foot, output) > 4 * TOLERANCE) {
					problems.add(where + ": output " + Arrays.toString(output)
							+ " without a real root is not the foot " + Arrays.toString(foot));
				}
				return 1;
			}
			if (roots[0][2] < 0) {
				problems.add(where + ": a reference root lies behind P");
			}
			boolean coincident = distance(roots[0], roots[1]) < 4 * TOLERANCE;
			double error = distance(roots[coincident ? 0 : designated], output);
			if (error > (coincident ? 4 * TOLERANCE : TOLERANCE)) {
				problems.add(where + ": output " + Arrays.toString(output) + " is not root "
						+ designated + " of " + Arrays.deepToString(roots));
			}
			return 1;
		}

		/** @return whether the binary64 driver leaves two real roots */
		boolean hasRoots() {
			return roots != null;
		}

		private static double distance(double[] first, double[] second) {
			return Math.hypot(first[0] - second[0], first[1] - second[1]);
		}
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

	/** @return the line-conic intersection whose output an indexed intersection selects */
	private AlgoIntersectLineConic liveSource(String output) {
		return (AlgoIntersectLineConic) ((AlgoIntersectSingle) lookup(output)
				.getParentAlgorithm()).getSourceIntersection();
	}

	private AlgoIntersectLineConic copySource(String label, String output) throws Exception {
		return (AlgoIntersectLineConic) ((AlgoIntersectSingle) copy(label).lookupLabel(output)
				.getParentAlgorithm()).getSourceIntersection();
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
