/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.ConstructionValidityPredicate2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructionFidelity2D.DiscrepancyKind;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.Kind;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.Unresolved;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T3: class v3 of the certified construction model
 * ({@code locus-v2-certified-construction-model.md} section 10): capture of rays, segments,
 * circles, line-circle intersections with their selection state, multi-output steps,
 * vector operations, expanded macros and the structural metric input; typed box outcomes;
 * value and derivative enclosures against an independent extended-precision reference;
 * the versioned step limit; and the effective outputs at coincident roots. Matrix rows
 * V-V3-1 to V-V3-4, V-UNIF-1, V-SEL-1, V-SEL-6, V-SEL-7, V-COINC-1, V-COINC-2 and
 * V-COINC-8.
 */
class PreG9BCu2ClassV3ModelTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";
	private static final MathContext MC = new MathContext(60, RoundingMode.HALF_EVEN);
	/** Double root of the exact synthetic critical configuration (matrix section 1). */
	private static final double CRITICAL = Math.atan2(4, 3);
	private static final LocusInterval2D CIRCLE_DOMAIN =
			new LocusInterval2D(-Math.PI, Math.PI, true, false);
	/** Driver circle of centre (10, 0) and radius 1, as GeoGebra captures it. */
	private static final double[] DRIVER = {1, 1, 1, 0, 0, 1, 10, 0};

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/**
	 * Programs of classes v1 and v2 keep their version, signature and enclosures through the
	 * class-v3 capture and box model.
	 */
	@Test
	void classV1AndV2ProgramsAreUnchangedThroughTheClassV3Path() throws Exception {
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("E=Midpoint(D,C)");
		add("a=LocusV2(E,C)");
		add("v=Vector((0.5,0.3))");
		add("al=20deg");
		add("P1=Translate(C,v)");
		add("P2=Rotate(P1,al,(1,2))");
		add("P3=Mirror(P2,y=0.2x+2)");
		add("P4=Dilate(P3,1.1,(1,2))");
		add("chain=LocusV2(P4,C)");
		add("X=C+(1.5,-0.25)");
		add("expression=LocusV2(X,C)");
		getKernel().updateConstruction(false);
		for (String label : List.of("a", "chain", "expression")) {
			ReconstructibleLocusEvaluator2D evaluator = evaluator(label);
			CertifiedConstructionProgram2D legacy =
					evaluator.captureCertifiedProgram().orElseThrow();
			CertifiedConstructionProgram2D program =
					evaluator.captureCertifiedProgramV3().orElseThrow();
			assertEquals(legacy.getSignature(), program.getSignature(), label);
			assertNotEquals(CertifiedConstructionProgram2D.VERSION_V3, program.getVersion());
			ConstructionIntervalModel2D classV1 = assertInstanceOf(
					ConstructionIntervalModel2D.class, CertifiedIntervalCurveModel2D.capture(
							locus(label).getSemanticDefinition(), BRANCH), label);
			ConstructionBoxModel2D classV3 = new ConstructionBoxModel2D(program);
			for (int index = -8; index < 8; index++) {
				SplineOutwardInterval2D box = new SplineOutwardInterval2D(
						index * Math.PI / 8 + 0.01, index * Math.PI / 8 + 0.02);
				ConstructionBoxOutcome2D outcome = classV3.evaluate(box);
				assertEquals(Kind.VALID, outcome.kind, label + " " + outcome);
				SplineOutwardInterval2D[] value = classV1.evaluate(box, false);
				SplineOutwardInterval2D[] derivative = classV1.evaluate(box, true);
				for (int axis = 0; axis < 2; axis++) {
					// The same formula sequence with the same outward arithmetic.
					assertEquals(value[axis].lower, outcome.value[axis].lower, label);
					assertEquals(value[axis].upper, outcome.value[axis].upper, label);
					assertEquals(derivative[axis].lower, outcome.derivative[axis].lower,
							label);
					assertEquals(derivative[axis].upper, outcome.derivative[axis].upper,
							label);
				}
			}
		}
	}

	/**
	 * V-V3-1: the value and derivative enclosures of every new capability contain an
	 * independent 60-digit reference on deterministic random boxes, and the captured program
	 * agrees with the floating evaluator.
	 */
	@Test
	void newCapabilitiesEncloseTheExtendedPrecisionReference() throws Exception {
		critical(40);
		add("H=(0,0)");
		add("c1=Circle(H,2)");
		add("s1=Ray(H,N)");
		add("G=Intersect(c1,s1,1)");
		add("lgx=LocusV2(G,N)");
		add("I=(1,1)");
		add("d1=Circle(I,N)");
		add("li=Line((-100,1),(100,1.5))");
		add("H1=Intersect(d1,li,1)");
		add("lh=LocusV2(H1,N)");
		add("A=(3,4)");
		add("v1=Vector(A,N)");
		add("w1=UnitVector(v1)");
		add("k1=v1*w1");
		add("E=A+(k1*w1)");
		add("le=LocusV2(E,N)");
		add("sg=Segment(N,(0,0))");
		add("hz=Line((-100,-3),(100,-3))");
		add("X=Intersect(sg,hz)");
		add("ls=LocusV2(X,N)");
		add("rr=Ray((0,0),N)");
		add("vt=Line((10,-100),(10,100))");
		add("Xr=Intersect(rr,vt)");
		add("lrr=LocusV2(Xr,N)");
		macros();
		add("K=Midpoint(N,(5,0))");
		add("g2=Line((1,2),(4,3))");
		add("Q3=jumpT(K,g2,7)");
		add("lj=LocusV2(Q3,N)");
		add("O1=(2,-1)");
		add("kk=Line((0,0),(2,1))");
		add("f2=Line((0,0),(1,-3))");
		add("j3=Line(Q3,f2)");
		add("R5=translationT(K,O1,kk,Q3,j3)");
		add("lt=LocusV2(R5,N)");
		getKernel().updateConstruction(false);
		Random random = new Random(20261021L);
		int checked = 0;
		for (String label : List.of("lq", "lr", "lgx", "lh", "le", "ls", "lrr", "lj", "lt")) {
			LocusDefinition2D definition = locus(label).getSemanticDefinition();
			ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(definition);
			assertNotNull(model, label);
			assertEquals(CertifiedConstructionProgram2D.VERSION_V3,
					model.getProgram().getVersion(), label);
			int valid = 0;
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
				for (int sample = 0; sample < 60; sample++) {
					double lower = -Math.PI + 2 * Math.PI * random.nextDouble();
					double width = sample % 3 == 0 ? 0 : 1e-3 * random.nextDouble();
					double upper = Math.min(Math.nextDown(Math.PI), lower + width);
					SplineOutwardInterval2D box = new SplineOutwardInterval2D(lower, upper);
					ConstructionBoxOutcome2D outcome = model.evaluate(box);
					if (outcome.kind != Kind.VALID) {
						continue;
					}
					valid++;
					for (double parameter : new double[] {lower, box.midpoint(), upper}) {
						BigDecimal[] reference = Reference.evaluate(model.getProgram(),
								new BigDecimal(parameter));
						assertEncloses(label + " t=" + parameter, outcome.value, reference,
								BigDecimal.ZERO);
						BigDecimal[] derivative = Reference.derivative(model.getProgram(),
								new BigDecimal(parameter));
						assertEncloses(label + " derivative t=" + parameter,
								outcome.derivative, derivative, new BigDecimal("1e-25"));
						LocusEvaluation2D evaluation = definition.evaluate(BRANCH,
								parameter, session);
						assertTrue(evaluation.isValid(), label + " t=" + parameter);
						assertEquals(reference[0].doubleValue(),
								evaluation.getPoint().getX(),
								1e-9 * (1 + Math.abs(reference[0].doubleValue())), label);
						assertEquals(reference[1].doubleValue(),
								evaluation.getPoint().getY(),
								1e-9 * (1 + Math.abs(reference[1].doubleValue())), label);
					}
				}
			}
			assertTrue(valid >= 20, label + " valid boxes " + valid);
			checked += valid;
		}
		assertTrue(checked > 200);
	}

	/**
	 * V-V3-2: one case per typed reason; equality predicates by structural identity; only a
	 * uniformly invalid box justifies certified nonexistence.
	 */
	@Test
	void everyInvalidReasonIsTypedAndOnlyItCertifiesNonexistence() {
		SplineOutwardInterval2D box = new SplineOutwardInterval2D(0.3, 0.4);
		Map<String, CertifiedConstructionProgram2D> programs = new LinkedHashMap<>();
		// A line through the driver and the origin misses the circle of radius 1 at (0,5).
		programs.put("DISCRIMINANT_NEGATIVE", program(
				step(Operation.JOIN, Operand.node(0), point(0, 0)),
				step(Operation.LINE_CIRCLE, List.of(Operand.node(1),
						Operand.circle(24, 0, -5, 1)), 0, 0, 1, 1, 1),
				step(Operation.INTERSECTION_OUTPUT, List.of(Operand.node(2)), 0)));
		// A ray from the origin through the driver meets x = -5 behind its start.
		programs.put("RAY_PARAMETER_NEGATIVE", program(
				step(Operation.RAY, point(0, 0), Operand.node(0)),
				step(Operation.MEET, Operand.node(1), Operand.line(1, 0, 5))));
		// A segment from the origin to the driver meets x = 20 beyond its end.
		programs.put("SEGMENT_PARAMETER_OUTSIDE", program(
				step(Operation.SEGMENT, point(0, 0), Operand.node(0)),
				step(Operation.MEET, Operand.node(1), Operand.line(1, 0, -20))));
		programs.put("ZERO_DIRECTION", program(
				step(Operation.VECTOR_POINTS, Operand.node(0), Operand.node(0)),
				step(Operation.UNIT_VECTOR, List.of(Operand.node(1)), 1),
				step(Operation.POINT_PLUS_VECTOR, List.of(Operand.node(0),
						Operand.node(2)), 1)));
		programs.put("ZERO_RADIUS", program(
				step(Operation.CIRCLE_THROUGH, Operand.node(0), Operand.node(0)),
				step(Operation.LINE_CIRCLE, List.of(Operand.line(0, 1, 0),
						Operand.node(1)), 0, 0, 1, 1, 1),
				step(Operation.INTERSECTION_OUTPUT, List.of(Operand.node(2)), 0)));
		// A parallel to y = 0 through the driver never meets y = 0.
		programs.put("DIVISOR_ZERO", program(
				step(Operation.PARALLEL, Operand.node(0), Operand.line(0, 1, 0)),
				step(Operation.MEET, Operand.node(1), Operand.line(0, 1, 0))));
		for (Map.Entry<String, CertifiedConstructionProgram2D> entry : programs.entrySet()) {
			ConstructionBoxOutcome2D outcome =
					new ConstructionBoxModel2D(entry.getValue()).evaluate(box);
			assertEquals(Kind.UNIFORMLY_INVALID, outcome.kind,
					entry.getKey() + " " + outcome);
			assertEquals(entry.getKey(), outcome.getInvalidReason(), outcome.toString());
			assertEquals(LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE,
					outcome.getCoverageCause());
			assertTrue(outcome.getCoverageCause().admits(
					CoverageState.CERTIFIED_DOES_NOT_EXIST));
		}
		for (Unresolved reason : Unresolved.values()) {
			LocusCoverageCause2D cause = ConstructionBoxOutcome2D.unresolved(reason, 1, null,
					"", List.of()).getCoverageCause();
			assertFalse(cause.admits(CoverageState.CERTIFIED_DOES_NOT_EXIST), reason.name());
			assertFalse(cause.admits(CoverageState.CERTIFIED_EXISTS), reason.name());
		}
	}

	/**
	 * V-UNIF-1: a divisor, a direction or a radius whose enclosure contains zero without
	 * vanishing identically is unresolved; a strict inequality is decided by intervals.
	 */
	@Test
	void equalityPredicatesAreInvalidOnlyByStructuralIdentity() {
		// The driver passes through (11, 0) at t = 0.
		SplineOutwardInterval2D around = new SplineOutwardInterval2D(-0.01, 0.01);
		SplineOutwardInterval2D away = new SplineOutwardInterval2D(1, 1.01);
		CertifiedConstructionProgram2D radius = program(
				step(Operation.CIRCLE_THROUGH, point(11, 0), Operand.node(0)),
				step(Operation.LINE_CIRCLE, List.of(Operand.line(0, 1, 0), Operand.node(1)),
						0, 0, 1, 1, 1),
				step(Operation.INTERSECTION_OUTPUT, List.of(Operand.node(2)), 0));
		CertifiedConstructionProgram2D direction = program(
				step(Operation.VECTOR_POINTS, point(11, 0), Operand.node(0)),
				step(Operation.UNIT_VECTOR, List.of(Operand.node(1)), 1),
				step(Operation.POINT_PLUS_VECTOR, List.of(Operand.node(0),
						Operand.node(2)), 1));
		// The join of the driver and (11, 0) is vertical at t = 0, parallel to x = 3.
		CertifiedConstructionProgram2D divisor = program(
				step(Operation.JOIN, Operand.node(0), point(11, 5)),
				step(Operation.MEET, Operand.node(1), Operand.line(1, 0, -3)));
		Map<ConstructionValidityPredicate2D, CertifiedConstructionProgram2D> cases =
				new LinkedHashMap<>();
		cases.put(ConstructionValidityPredicate2D.RADIUS, radius);
		cases.put(ConstructionValidityPredicate2D.DIRECTION, direction);
		cases.put(ConstructionValidityPredicate2D.DIVISOR, divisor);
		for (Map.Entry<ConstructionValidityPredicate2D, CertifiedConstructionProgram2D> entry
				: cases.entrySet()) {
			ConstructionBoxModel2D model = new ConstructionBoxModel2D(entry.getValue());
			ConstructionBoxOutcome2D outcome = model.evaluate(around);
			assertEquals(Kind.UNRESOLVED, outcome.kind, entry.getKey() + " " + outcome);
			assertEquals(Unresolved.PREDICATE, outcome.unresolved, outcome.toString());
			assertEquals(entry.getKey(), outcome.predicate, outcome.toString());
			assertTrue(entry.getKey().isEquality());
			assertEquals(Kind.VALID, model.evaluate(away).kind, entry.getKey().name());
		}
		// A strict inequality is uniformly invalid by intervals.
		CertifiedConstructionProgram2D discriminant = program(
				step(Operation.JOIN, Operand.node(0), point(0, 0)),
				step(Operation.LINE_CIRCLE, List.of(Operand.node(1),
						Operand.circle(24, 0, -5, 1)), 0, 0, 1, 1, 1),
				step(Operation.INTERSECTION_OUTPUT, List.of(Operand.node(2)), 0));
		assertEquals(Kind.UNIFORMLY_INVALID,
				new ConstructionBoxModel2D(discriminant).evaluate(away).kind);
	}

	/**
	 * V-V3-3: a reference-point intersection, an unsettled live selection and the continuous
	 * mode, which the product policy keeps off, have no class-v3 program.
	 */
	@Test
	void outOfClassShapesHaveNoProgram() throws Exception {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45),40)");
		add("N=Point(g1)");
		add("t=Ray(P,N)");
		add("Z=(10,10)");
		add("Rz=Intersect(p,t,Z)");
		add("lz=LocusV2(Rz,N)");
		add("tq=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("tq"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		// Created by the command, the intersection is still to settle its permutation.
		assertEquals(List.of("Q"), evaluator("lq").getFidelityReport()
				.getUnsettledSelections());
		assertTrue(evaluator("lq").captureCertifiedProgramV3().isEmpty());
		assertTrue(evaluator("lz").captureCertifiedProgramV3().isEmpty());
		// The existence outcome of a slice without program is the certificate's (step T4,
		// published since step T11): NOT_ESTABLISHED with its typed cause.
		getKernel().setContinuous(true);
		assertFalse(getKernel().isContinuous());
		getKernel().updateConstruction(false);
		CertifiedConstructionProgram2D settled =
				evaluator("lq").captureCertifiedProgramV3().orElseThrow();
		Step intersection = settled.getSteps().get(1);
		assertEquals(Operation.LINE_CIRCLE, intersection.getOperation());
		assertEquals(0, intersection.getParameter(0));
		assertTrue(evaluator("lz").captureCertifiedProgramV3().isEmpty());
	}

	/**
	 * V-V3-4, synthetic part: the driver-dependent step limit of class v3 is versioned and
	 * counted after macro expansion; a slice above it has no program, whose existence outcome
	 * the certificate of step T4, published since step T11, types.
	 */
	@Test
	void theStepLimitIsVersionedAfterMacroExpansion() throws Exception {
		assertEquals(256, CertifiedConstructionProgram2D.MAXIMUM_STEPS_V3);
		add("c=Circle((0,0),3)");
		add("N=Point(c)");
		add("v=Vector((0.001,0))");
		String previous = "N";
		for (int index = 1; index <= CertifiedConstructionProgram2D.MAXIMUM_STEPS_V3 + 1;
				index++) {
			add("T" + index + "=Translate(" + previous + ",v)");
			previous = "T" + index;
		}
		add("inside=LocusV2(T" + CertifiedConstructionProgram2D.MAXIMUM_STEPS_V3 + ",N)");
		add("outside=LocusV2(" + previous + ",N)");
		CertifiedConstructionProgram2D program =
				evaluator("inside").captureCertifiedProgramV3().orElseThrow();
		assertEquals(CertifiedConstructionProgram2D.MAXIMUM_STEPS_V3,
				program.getSteps().size());
		assertEquals(CertifiedConstructionProgram2D.VERSION_V3, program.getVersion());
		assertTrue(evaluator("inside").captureCertifiedProgram().isEmpty());
		assertTrue(evaluator("outside").captureCertifiedProgramV3().isEmpty());
	}

	/**
	 * V-SEL-1, synthetic part: the rule is read from the algorithm (a settled permutation,
	 * the compaction of a first initialization, the output of an indexed single
	 * intersection), never from a sample, and the program does not depend on the driver
	 * position.
	 */
	@Test
	void theSelectionRuleIsReadFromTheAlgorithm() throws Exception {
		critical(40);
		add("H=(0,0)");
		add("c1=Circle(H,2)");
		add("s1=Ray(H,N)");
		add("G=Intersect(c1,s1,1)");
		add("lgx=LocusV2(G,N)");
		getKernel().updateConstruction(false);
		Step lq = evaluator("lq").captureCertifiedProgramV3().orElseThrow().getSteps().get(1);
		assertSelection(lq, 0, 0, 1, true, true);
		Step lgx = evaluator("lgx").captureCertifiedProgramV3().orElseThrow().getSteps()
				.get(1);
		// The ray leaves the centre: one root on the ray, so the algorithm never settles.
		assertSelection(lgx, 1, 0, 1, false, false);
		Map<String, String> signatures = new LinkedHashMap<>();
		Map<String, Long> revisions = new LinkedHashMap<>();
		for (String label : List.of("lq", "lr", "lgx")) {
			signatures.put(label, signature(label));
			revisions.put(label, locus(label).getSemanticRevision());
		}
		GeoPoint driver = (GeoPoint) lookup("N");
		for (double parameter : new double[] {-3, -1, CRITICAL, 2.5}) {
			driver.getPathParameter().setT(parameter);
			driver.getPath().pathChanged(driver);
			driver.updateCoords();
			driver.updateCascade();
			for (String label : List.of("lq", "lr", "lgx")) {
				assertEquals(signatures.get(label), signature(label), label);
				assertEquals(revisions.get(label), locus(label).getSemanticRevision(), label);
			}
		}
	}

	/**
	 * V-SEL-6: a live permutation [1, 0] is reproduced by the copy, which reads the saved
	 * output positions of the slice. Neither the order of the queries that reach the copy,
	 * through its gaps included, nor a recapture changes the copy's selection state, its
	 * fidelity or the program of the revision.
	 */
	@Test
	void anInheritedPermutationIsReproducedInAnyQueryOrder() throws Exception {
		add("k=Circle((0,0),5)");
		add("g1=Circle((0,0),20)");
		add("N=Point(g1)");
		add("F=(0,6)");
		add("m=Line(F,N)");
		GeoPoint driver = (GeoPoint) lookup("N");
		moveDriver(driver, Math.PI / 2);
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"S", "T"},
				(GeoLine) lookup("m"), (GeoConic) lookup("k"));
		AlgoIntersectLineConic live = (AlgoIntersectLineConic) lookup("S")
				.getParentAlgorithm();
		// The next computation settles the permutation by proximity; half a turn reverses
		// the direction of the line through F, so S keeps its side through raw root 1.
		moveDriver(driver, -Math.PI / 2);
		assertEquals("[1, 0]", java.util.Arrays.toString(live.getSelectionPermutation()));
		add("ls=LocusV2(S,N)");
		CertifiedConstructionProgram2D first =
				evaluator("ls").captureCertifiedProgramV3().orElseThrow();
		assertSelection(first.getSteps().get(1), 0, 1, 0, true, true);
		// Point queries through the gap of the copy and a recapture by the pair certifier
		// do not change the program of the revision.
		LocusDefinition2D definition = locus("ls").getSemanticDefinition();
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (int sample = -16; sample < 16; sample++) {
				definition.evaluate(BRANCH, sample * Math.PI / 16, session);
			}
		}
		AlgoIntersectLineConic copy = (AlgoIntersectLineConic) copyAlgorithm("ls", "S");
		assertEquals("[1, 0]", java.util.Arrays.toString(copy.getSelectionPermutation()));
		evaluator("ls").captureCertifiedProgram();
		assertEquals("[1, 0]", java.util.Arrays.toString(copy.getSelectionPermutation()));
		assertTrue(evaluator("ls").isReconstructionFaithful(),
				evaluator("ls").getFidelityReport().toString());
		assertEquals(first.getSignature(), signature("ls"));
	}

	/**
	 * V-SEL-6: a settled live selection that the copy cannot reproduce fails closed with its
	 * typed diagnostic. A copy rebuilt where only one root lies on the ray initializes by
	 * compaction and never settles, while the live intersection keeps its permutation.
	 */
	@Test
	void aSelectionTheCopyCannotReproduceFailsClosed() throws Exception {
		add("k=Circle((0,0),5)");
		add("g1=Circle((0,0),20)");
		add("N=Point(g1)");
		add("A=(-10,0)");
		add("r=Ray(A,N)");
		GeoPoint driver = (GeoPoint) lookup("N");
		moveDriver(driver, 0);
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"S", "T"},
				(GeoLine) lookup("r"), (GeoConic) lookup("k"));
		moveDriver(driver, 0.05);
		AlgoIntersectLineConic live = (AlgoIntersectLineConic) lookup("T")
				.getParentAlgorithm();
		assertFalse(live.isSelectionPermutationPending());
		add("lt=LocusV2(T,N)");
		assertTrue(evaluator("lt").isReconstructionFaithful());
		assertTrue(evaluator("lt").captureCertifiedProgramV3().isPresent());
		// The start of the ray enters the circle: raw root 0 leaves the ray, T stays.
		GeoPoint start = (GeoPoint) lookup("A");
		start.setCoords(1, 0, 1);
		start.updateCascade();
		assertTrue(lookup("T").isDefined());
		assertFalse(live.isSelectionPermutationPending());
		assertFalse(evaluator("lt").isReconstructionFaithful());
		assertTrue(evaluator("lt").getFidelityReport().isSelectionOnly(),
				evaluator("lt").getFidelityReport().toString());
		assertTrue(evaluator("lt").captureCertifiedProgramV3().isEmpty());
		LocusExistenceStructure2D structure = locus("lt").getSemanticDefinition()
				.getBranch(BRANCH).getExistenceStructure();
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED,
				structure.getCompleteness());
		assertTrue(structure.getCoverage().stream().allMatch(coverage
				-> coverage.getCause() == LocusCoverageCause2D.SELECTION_NOT_REPRODUCED),
				structure.toString());
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			assertFalse(locus("lt").getSemanticDefinition().evaluate(BRANCH, 0.05, session)
					.isValid());
		}
	}

	/**
	 * V-SEL-7 and V-COINC-8: the selection and label states are read again on reopening,
	 * undo, redo and recomputation; an unchanged state gives the identical program.
	 */
	@Test
	void selectionAndLabelStatesAreReadAgainOnReopenUndoRedoAndRecompute() throws Exception {
		activateUndo();
		critical(40);
		getKernel().updateConstruction(false);
		getApp().storeUndoInfo();
		List<String> labels = List.of("lq", "lr", "lr2");
		Map<String, String> original = new LinkedHashMap<>();
		for (String label : labels) {
			original.put(label, signature(label));
		}
		getKernel().updateConstruction(false);
		for (String label : labels) {
			assertEquals(original.get(label), signature(label), label + " recompute");
		}
		GeoPoint driver = (GeoPoint) lookup("N");
		moveDriver(driver, 2.5);
		getApp().storeUndoInfo();
		getKernel().undo();
		for (String label : labels) {
			assertEquals(original.get(label), signature(label), label + " undo");
		}
		getKernel().redo();
		for (String label : labels) {
			assertEquals(original.get(label), signature(label), label + " redo");
		}
		reload();
		for (String label : labels) {
			assertEquals(original.get(label), signature(label), label + " reopen");
		}
	}

	/**
	 * V-COINC-1, step T3: the capture reads the label state of both outputs, equal in the live
	 * construction and in the copy; at the double root both outputs are defined and coincide;
	 * a box around it is never uniformly invalid.
	 */
	@Test
	void labelledOutputsAtTheCoincidentRootAreBothDefined() throws Exception {
		critical(40);
		getKernel().updateConstruction(false);
		for (String label : List.of("lq", "lr")) {
			assertTrue(evaluator(label).isReconstructionFaithful(), label);
			assertSelection(evaluator(label).captureCertifiedProgramV3().orElseThrow()
					.getSteps().get(1), 0, 0, 1, true, true);
		}
		moveDriver((GeoPoint) lookup("N"), CRITICAL);
		GeoPoint q = (GeoPoint) lookup("Q");
		GeoPoint r = (GeoPoint) lookup("R");
		assertTrue(q.isDefined() && r.isDefined());
		assertEquals(q.getInhomX(), r.getInhomX(), 1e-6);
		assertEquals(q.getInhomY(), r.getInhomY(), 1e-6);
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			LocusEvaluation2D lq = locus("lq").getSemanticDefinition().evaluate(BRANCH,
					CRITICAL, session);
			LocusEvaluation2D lr = locus("lr").getSemanticDefinition().evaluate(BRANCH,
					CRITICAL, session);
			assertTrue(lq.isValid() && lr.isValid());
			assertEquals(lq.getPoint().getX(), lr.getPoint().getX(), 1e-6);
		}
		ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(
				locus("lr").getSemanticDefinition());
		ConstructionBoxOutcome2D around = model.evaluate(
				new SplineOutwardInterval2D(CRITICAL - 1e-6, CRITICAL + 1e-6));
		assertEquals(Kind.UNRESOLVED, around.kind, around.toString());
		assertEquals(Unresolved.PREDICATE, around.unresolved, around.toString());
		assertEquals(ConstructionValidityPredicate2D.DISCRIMINANT, around.predicate);
		for (int exponent = 1; exponent <= 8; exponent++) {
			ConstructionBoxOutcome2D near = model.evaluate(
					SplineOutwardInterval2D.point(CRITICAL + Math.pow(10, -exponent)));
			// Both outputs are labelled: never the inherited suppression band.
			assertTrue(near.kind == Kind.VALID || near.unresolved == Unresolved.PREDICATE,
					near.toString());
		}
		assertEquals(Kind.VALID, model.evaluate(
				SplineOutwardInterval2D.point(CRITICAL + 0.05)).kind);
	}

	/**
	 * V-COINC-2, step T3: the unlabelled second output gets a typed unresolved band that
	 * contains every undefined sample of the live construction and of the copy near the
	 * coincident root, and is valid away from it.
	 */
	@Test
	void anUnlabelledSecondOutputHasATypedUnresolvedBand() throws Exception {
		critical(40);
		getKernel().updateConstruction(false);
		CertifiedConstructionProgram2D program =
				evaluator("lr2").captureCertifiedProgramV3().orElseThrow();
		assertSelection(program.getSteps().get(1), 0, 0, 1, false, false);
		assertEquals(1, program.getSteps().get(2).getParameter(0));
		ConstructionBoxModel2D model = new ConstructionBoxModel2D(program);
		LocusDefinition2D definition = locus("lr2").getSemanticDefinition();
		GeoPoint driver = (GeoPoint) lookup("N");
		GeoPoint live = (GeoPoint) lookup("R2");
		int undefined = 0;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (int sample = -100; sample <= 100; sample++) {
				double parameter = CRITICAL + sample * 1e-10;
				moveDriver(driver, parameter);
				boolean liveDefined = live.isDefined();
				boolean copyDefined = definition.evaluate(BRANCH, parameter, session)
						.isValid();
				ConstructionBoxOutcome2D outcome = model.evaluate(
						SplineOutwardInterval2D.point(parameter));
				if (!liveDefined || !copyDefined) {
					undefined++;
					assertEquals(Kind.UNRESOLVED, outcome.kind, "t=" + parameter);
					assertTrue(outcome.unresolved == Unresolved.UNLABELLED_COINCIDENT_OUTPUT
							|| outcome.unresolved == Unresolved.PREDICATE, outcome.toString());
				}
			}
		}
		assertTrue(undefined > 0);
		int banded = 0;
		for (int exponent = 1; exponent <= 8; exponent++) {
			ConstructionBoxOutcome2D near = model.evaluate(
					SplineOutwardInterval2D.point(CRITICAL + Math.pow(10, -exponent)));
			if (near.unresolved == Unresolved.UNLABELLED_COINCIDENT_OUTPUT) {
				banded++;
				assertEquals(LocusCoverageCause2D.UNLABELLED_COINCIDENT_OUTPUT,
						near.getCoverageCause());
			} else {
				assertTrue(near.kind == Kind.VALID
						|| near.unresolved == Unresolved.PREDICATE, near.toString());
			}
		}
		assertTrue(banded > 0);
		assertEquals(Kind.VALID, model.evaluate(
				SplineOutwardInterval2D.point(CRITICAL + 0.05)).kind);
	}

	/** Exact synthetic critical configuration with labelled and unlabelled outputs. */
	private void critical(double radius) {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45)," + radius + ")");
		add("N=Point(g1)");
		add("t=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		add("lr=LocusV2(R,N)");
		add("t2=Ray(P,N)");
		add("R2=Intersect(p,t2,2)");
		add("lr2=LocusV2(R2,N)");
	}

	/** Analogues of the author's macros {@code pointJump} and {@code translationCoor}. */
	private void macros() throws Exception {
		GeoElement start = add("Am=(0,0)");
		GeoElement direction = add("fm=Line((0,0),(1,1))");
		GeoElement advance = add("Av=1");
		add("um=UnitVector(fm)");
		GeoElement jumped = add("Bm=Am+(Av*um)");
		getKernel().addMacro(new Macro(getKernel(), "jumpT",
				new GeoElement[] {start, direction, advance}, new GeoElement[] {jumped}));
		final GeoElement measured = add("Dm=(1,0)");
		final GeoElement reference = add("Cm=(0,0)");
		final GeoElement measure = add("fk=Line((0,0),(1,2))");
		final GeoElement translated = add("Fp=(3,3)");
		final GeoElement translation = add("im=Line((3,3),(4,1))");
		add("gm=PerpendicularLine(Dm,fk)");
		add("hm=Line(Cm,fk)");
		add("Em=Intersect(gm,hm)");
		add("uv=Vector(Cm,Em)");
		add("vv=UnitVector(fk)");
		add("relx=uv*vv");
		add("wv=UnitVector(im)");
		GeoElement output = add("Im=Fp+(relx*wv)");
		getKernel().addMacro(new Macro(getKernel(), "translationT",
				new GeoElement[] {measured, reference, measure, translated, translation},
				new GeoElement[] {output}));
	}

	private static void moveDriver(GeoPoint driver, double parameter) {
		driver.getPathParameter().setT(parameter);
		driver.getPath().pathChanged(driver);
		driver.updateCoords();
		driver.updateCascade();
	}

	private static void assertSelection(Step step, double mode, double first, double second,
			boolean firstLabelled, boolean secondLabelled) {
		assertEquals(Operation.LINE_CIRCLE, step.getOperation());
		assertEquals(mode, step.getParameter(0), "mode");
		assertEquals(first, step.getParameter(1), "first");
		assertEquals(second, step.getParameter(2), "second");
		assertEquals(firstLabelled ? 1 : 0, step.getParameter(3), "first label");
		assertEquals(secondLabelled ? 1 : 0, step.getParameter(4), "second label");
	}

	private static void assertEncloses(String label, SplineOutwardInterval2D[] enclosure,
			BigDecimal[] reference, BigDecimal slack) {
		for (int axis = 0; axis < 2; axis++) {
			BigDecimal tolerance = slack.multiply(BigDecimal.ONE.add(reference[axis].abs()));
			assertTrue(new BigDecimal(enclosure[axis].lower).subtract(tolerance)
					.compareTo(reference[axis]) <= 0
					&& reference[axis].compareTo(new BigDecimal(enclosure[axis].upper)
							.add(tolerance)) <= 0,
					label + " axis=" + axis + " reference=" + reference[axis].doubleValue()
							+ " enclosure=[" + enclosure[axis].lower + ","
							+ enclosure[axis].upper + "]");
		}
	}

	private static CertifiedConstructionProgram2D program(Step... steps) {
		return new CertifiedConstructionProgram2D(Driver.CIRCLE, DRIVER, CIRCLE_DOMAIN,
				List.of(steps));
	}

	private static Step step(Operation operation, Operand... operands) {
		return new Step(operation, List.of(operands));
	}

	private static Step step(Operation operation, List<Operand> operands,
			double... parameters) {
		return new Step(operation, operands, parameters);
	}

	private static Operand point(double x, double y) {
		return Operand.point(x, y, 1, x, y);
	}

	private String signature(String label) throws Exception {
		return evaluator(label).captureCertifiedProgramV3()
				.map(CertifiedConstructionProgram2D::getSignature).orElse("none");
	}

	private GeoLocusV2 locus(String label) {
		return assertInstanceOf(GeoLocusV2.class, lookup(label), label);
	}

	private org.geogebra.common.kernel.algos.AlgoElement copyAlgorithm(String label,
			String output) throws Exception {
		Field field = ReconstructibleLocusEvaluator2D.class
				.getDeclaredField("isolatedConstruction");
		field.setAccessible(true);
		org.geogebra.common.kernel.Construction copy =
				(org.geogebra.common.kernel.Construction) field.get(evaluator(label));
		return copy.lookupLabel(output).getParentAlgorithm();
	}

	private ReconstructibleLocusEvaluator2D evaluator(String label) throws Exception {
		Field field = AlgoDependentPointLocusV2.class.getDeclaredField("evaluator");
		field.setAccessible(true);
		return assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
				field.get(locus(label).getParentAlgorithm()), label);
	}

	/**
	 * Independent 60-digit interpretation of a program: Taylor series for the driver,
	 * GeoGebra's own line parametrization for the line-circle roots, exact membership signs.
	 */
	private static final class Reference {
		private static final BigDecimal STEP = new BigDecimal("1e-25");

		static BigDecimal[] derivative(CertifiedConstructionProgram2D program,
				BigDecimal parameter) {
			BigDecimal[] after = evaluate(program, parameter.add(STEP));
			BigDecimal[] before = evaluate(program, parameter.subtract(STEP));
			BigDecimal twice = STEP.multiply(BigDecimal.valueOf(2));
			return new BigDecimal[] {after[0].subtract(before[0]).divide(twice, MC),
				after[1].subtract(before[1]).divide(twice, MC)};
		}

		static BigDecimal[] evaluate(CertifiedConstructionProgram2D program,
				BigDecimal parameter) {
			List<Step> steps = program.getSteps();
			Object[] nodes = new Object[steps.size() + 1];
			BigDecimal cosine = cos(parameter);
			BigDecimal sine = sin(parameter);
			BigDecimal px = constant(program, 0).multiply(cosine, MC);
			BigDecimal py = constant(program, 1).multiply(sine, MC);
			nodes[0] = unit(px.multiply(constant(program, 2), MC)
					.add(py.multiply(constant(program, 4), MC)).add(constant(program, 6)),
					px.multiply(constant(program, 3), MC)
							.add(py.multiply(constant(program, 5), MC))
							.add(constant(program, 7)));
			for (int index = 0; index < steps.size(); index++) {
				nodes[index + 1] = apply(steps.get(index), nodes);
			}
			BigDecimal[] output = (BigDecimal[]) nodes[steps.size()];
			return new BigDecimal[] {output[0].divide(output[2], MC),
				output[1].divide(output[2], MC)};
		}

		private static Object apply(Step step, Object[] nodes) {
			List<Operand> operands = step.getOperands();
			switch (step.getOperation()) {
			case JOIN:
			case RAY:
			case SEGMENT:
				// A line has three entries, a ray adds its start, a segment a marker.
				BigDecimal[] p = inhom(point(operands.get(0), nodes));
				BigDecimal[] q = inhom(point(operands.get(1), nodes));
				BigDecimal[] joined = {p[1].subtract(q[1]), q[0].subtract(p[0]),
					p[0].multiply(q[1], MC).subtract(p[1].multiply(q[0], MC)), p[0], p[1],
					BigDecimal.ZERO};
				int entries = step.getOperation() == Operation.JOIN ? 3
						: step.getOperation() == Operation.RAY ? 5 : 6;
				return java.util.Arrays.copyOf(joined, entries);
			case PARALLEL: {
				BigDecimal[] s = point(operands.get(0), nodes);
				BigDecimal[] l = line(operands.get(1), nodes);
				return new BigDecimal[] {s[2].multiply(l[0], MC), s[2].multiply(l[1], MC),
					s[0].multiply(l[0], MC).add(s[1].multiply(l[1], MC)).negate()};
			}
			case PERPENDICULAR: {
				BigDecimal[] s = point(operands.get(0), nodes);
				BigDecimal[] l = line(operands.get(1), nodes);
				return new BigDecimal[] {s[2].multiply(l[1], MC).negate(),
					s[2].multiply(l[0], MC),
					s[0].multiply(l[1], MC).subtract(s[1].multiply(l[0], MC))};
			}
			case MEET: {
				BigDecimal[] g = line(operands.get(0), nodes);
				BigDecimal[] h = line(operands.get(1), nodes);
				return new BigDecimal[] {g[1].multiply(h[2], MC).subtract(g[2].multiply(h[1],
						MC)), g[2].multiply(h[0], MC).subtract(g[0].multiply(h[2], MC)),
					g[0].multiply(h[1], MC).subtract(g[1].multiply(h[0], MC))};
			}
			case MIDPOINT: {
				BigDecimal[] first = inhom(point(operands.get(0), nodes));
				BigDecimal[] second = inhom(point(operands.get(1), nodes));
				BigDecimal half = new BigDecimal("0.5");
				return unit(first[0].add(second[0]).multiply(half),
						first[1].add(second[1]).multiply(half));
			}
			case CIRCLE_RADIUS: {
				BigDecimal[] center = inhom(point(operands.get(0), nodes));
				BigDecimal radius = new BigDecimal(step.getParameter(0));
				return new BigDecimal[] {center[0], center[1], radius.multiply(radius)};
			}
			case CIRCLE_THROUGH: {
				BigDecimal[] center = inhom(point(operands.get(0), nodes));
				BigDecimal[] through = inhom(point(operands.get(1), nodes));
				BigDecimal dx = through[0].subtract(center[0]);
				BigDecimal dy = through[1].subtract(center[1]);
				return new BigDecimal[] {center[0], center[1],
					dx.multiply(dx, MC).add(dy.multiply(dy, MC))};
			}
			case LINE_CIRCLE:
				return new Object[] {line(operands.get(0), nodes),
					circle(operands.get(1), nodes), step};
			case INTERSECTION_OUTPUT:
				return output((Object[]) nodes[operands.get(0).getNode()],
						(int) step.getParameter(0));
			case UNIT_VECTOR_LINE: {
				BigDecimal[] l = line(operands.get(0), nodes);
				BigDecimal length = l[0].multiply(l[0], MC).add(l[1].multiply(l[1], MC))
						.sqrt(MC);
				return new BigDecimal[] {l[1].divide(length, MC),
					l[0].negate().divide(length, MC)};
			}
			case UNIT_VECTOR: {
				BigDecimal[] v = vector(operands.get(0), nodes);
				BigDecimal length = v[0].multiply(v[0], MC).add(v[1].multiply(v[1], MC))
						.sqrt(MC);
				return new BigDecimal[] {v[0].divide(length, MC), v[1].divide(length, MC)};
			}
			case VECTOR_POINTS: {
				BigDecimal[] from = inhom(point(operands.get(0), nodes));
				BigDecimal[] to = inhom(point(operands.get(1), nodes));
				return new BigDecimal[] {to[0].subtract(from[0]), to[1].subtract(from[1])};
			}
			case SCALE: {
				BigDecimal factor = scalar(operands.get(0), nodes);
				BigDecimal[] v = vector(operands.get(1), nodes);
				return new BigDecimal[] {factor.multiply(v[0], MC), factor.multiply(v[1], MC)};
			}
			case DOT: {
				BigDecimal[] u = vector(operands.get(0), nodes);
				BigDecimal[] v = vector(operands.get(1), nodes);
				return u[0].multiply(v[0], MC).add(u[1].multiply(v[1], MC));
			}
			case POINT_PLUS_VECTOR: {
				BigDecimal[] base = inhom(point(operands.get(0), nodes));
				BigDecimal[] v = vector(operands.get(1), nodes);
				BigDecimal sign = new BigDecimal(step.getParameter(0));
				return unit(base[0].add(sign.multiply(v[0])), base[1].add(sign.multiply(v[1])));
			}
			default:
				throw new IllegalStateException("No reference for " + step.getOperation());
			}
		}

		/** GeoGebra's parametrization: a point of the line plus s (b, -a). */
		private static BigDecimal[] output(Object[] pair, int index) {
			BigDecimal[] l = (BigDecimal[]) pair[0];
			BigDecimal[] c = (BigDecimal[]) pair[1];
			Step step = (Step) pair[2];
			BigDecimal a = l[0];
			BigDecimal b = l[1];
			BigDecimal x0 = a.abs().compareTo(b.abs()) > 0
					? l[2].negate().divide(a, MC) : BigDecimal.ZERO;
			BigDecimal y0 = a.abs().compareTo(b.abs()) > 0
					? BigDecimal.ZERO : l[2].negate().divide(b, MC);
			BigDecimal u = a.multiply(a, MC).add(b.multiply(b, MC));
			BigDecimal ex = x0.subtract(c[0]);
			BigDecimal ey = y0.subtract(c[1]);
			BigDecimal d = ex.multiply(b, MC).subtract(ey.multiply(a, MC));
			BigDecimal w = ex.multiply(ex, MC).add(ey.multiply(ey, MC)).subtract(c[2]);
			BigDecimal root = d.multiply(d, MC).subtract(u.multiply(w, MC)).sqrt(MC);
			BigDecimal[][] roots = new BigDecimal[2][];
			for (int raw = 0; raw < 2; raw++) {
				BigDecimal s = d.negate().add(raw == 0 ? root.negate() : root).divide(u, MC);
				roots[raw] = unit(x0.add(s.multiply(b, MC)), y0.subtract(s.multiply(a, MC)));
			}
			int selected;
			if (step.getParameter(0) == 1) {
				List<Integer> onPath = new ArrayList<>();
				for (int raw = 0; raw < 2; raw++) {
					if (l.length < 5 || member(l, roots[raw])) {
						onPath.add(raw);
					}
				}
				selected = onPath.get(index);
			} else {
				selected = (int) step.getParameter(1 + index);
			}
			return roots[selected];
		}

		private static boolean member(BigDecimal[] line, BigDecimal[] point) {
			BigDecimal parameter = point[0].subtract(line[3]).multiply(line[1], MC)
					.subtract(point[1].subtract(line[4]).multiply(line[0], MC));
			BigDecimal norm = line[0].multiply(line[0], MC).add(line[1].multiply(line[1], MC));
			return parameter.signum() >= 0
					&& (line.length < 6 || parameter.compareTo(norm) <= 0);
		}

		private static BigDecimal[] point(Operand operand, Object[] nodes) {
			if (operand.getKind() == OperandKind.NODE) {
				return (BigDecimal[]) nodes[operand.getNode()];
			}
			return new BigDecimal[] {new BigDecimal(operand.getValue(0)),
				new BigDecimal(operand.getValue(1)), new BigDecimal(operand.getValue(2))};
		}

		private static BigDecimal[] line(Operand operand, Object[] nodes) {
			if (operand.getKind() == OperandKind.NODE) {
				return (BigDecimal[]) nodes[operand.getNode()];
			}
			if (operand.getKind() == OperandKind.LINE) {
				return new BigDecimal[] {new BigDecimal(operand.getValue(0)),
					new BigDecimal(operand.getValue(1)), new BigDecimal(operand.getValue(2))};
			}
			BigDecimal[] bounded = {new BigDecimal(operand.getValue(0)),
				new BigDecimal(operand.getValue(1)), new BigDecimal(operand.getValue(2)),
				new BigDecimal(operand.getValue(3)), new BigDecimal(operand.getValue(4)),
				BigDecimal.ZERO};
			return java.util.Arrays.copyOf(bounded,
					operand.getKind() == OperandKind.RAY ? 5 : 6);
		}

		private static BigDecimal[] circle(Operand operand, Object[] nodes) {
			if (operand.getKind() == OperandKind.NODE) {
				return (BigDecimal[]) nodes[operand.getNode()];
			}
			BigDecimal a2 = new BigDecimal(operand.getValue(0));
			BigDecimal a4 = new BigDecimal(operand.getValue(1));
			BigDecimal a5 = new BigDecimal(operand.getValue(2));
			return new BigDecimal[] {a4.negate(), a5.negate(),
				a4.multiply(a4).add(a5.multiply(a5)).subtract(a2)};
		}

		private static BigDecimal[] vector(Operand operand, Object[] nodes) {
			if (operand.getKind() == OperandKind.NODE) {
				return (BigDecimal[]) nodes[operand.getNode()];
			}
			return new BigDecimal[] {new BigDecimal(operand.getValue(0)),
				new BigDecimal(operand.getValue(1))};
		}

		private static BigDecimal scalar(Operand operand, Object[] nodes) {
			if (operand.getKind() == OperandKind.NODE) {
				return (BigDecimal) nodes[operand.getNode()];
			}
			return new BigDecimal(operand.getValue(0));
		}

		private static BigDecimal[] inhom(BigDecimal[] point) {
			return new BigDecimal[] {point[0].divide(point[2], MC),
				point[1].divide(point[2], MC)};
		}

		private static BigDecimal[] unit(BigDecimal x, BigDecimal y) {
			return new BigDecimal[] {x, y, BigDecimal.ONE};
		}

		private static BigDecimal constant(CertifiedConstructionProgram2D program, int index) {
			return new BigDecimal(program.getDriverConstant(index));
		}

		private static BigDecimal cos(BigDecimal x) {
			return series(x, true);
		}

		private static BigDecimal sin(BigDecimal x) {
			return series(x, false);
		}

		private static BigDecimal series(BigDecimal x, boolean cosine) {
			BigDecimal square = x.multiply(x, MC);
			BigDecimal term = cosine ? BigDecimal.ONE : x;
			BigDecimal sum = term;
			BigDecimal limit = new BigDecimal("1e-70");
			for (int k = cosine ? 2 : 3; term.abs().compareTo(limit) > 0; k += 2) {
				term = term.multiply(square, MC).negate()
						.divide(BigDecimal.valueOf((long) k * (k - 1)), MC);
				sum = sum.add(term, MC);
			}
			return sum;
		}
	}
}
