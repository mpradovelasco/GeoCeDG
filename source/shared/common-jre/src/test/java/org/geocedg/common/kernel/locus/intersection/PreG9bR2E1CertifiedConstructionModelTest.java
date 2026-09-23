/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R2-E0/E1 certified construction interval model of class v1
 * ({@code locus-v2-certified-construction-model.md}): enclosure against the
 * evaluator, uniform predicates, refusal, obligations and bounded work.
 */
class PreG9bR2E1CertifiedConstructionModelTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void modelEnclosesTheEvaluatorAndItsDerivativeForEveryOperation() {
		getKernel().setContinuous(false);
		witness();
		// Similarity chain: every in-slice point transform of class v1.
		add("v=Vector((0.5,0.3))");
		add("al=20deg");
		add("be=15deg");
		add("K=(1,2)");
		add("Q0=(0.5,1.5)");
		add("g0:y=0.2x+2");
		add("k1=0.8");
		add("k2=1.1");
		add("P1=Translate(C,v)");
		add("P2=Rotate(P1,al)");
		add("P3=Rotate(P2,be,K)");
		add("P4=Mirror(P3,Q0)");
		add("P5=Mirror(P4,g0)");
		add("P6=Dilate(P5,k1)");
		add("P7=Dilate(P6,k2,K)");
		add("chain=LocusV2(P7,C)");
		// Arc driver with join, perpendicular and meet. Labeled inputs: a slice with
		// inline literal command arguments is not replayed by the evaluator (retained
		// debt RECONSTRUCTIBLE-INLINE-LITERAL-SLICE-DISCONNECTED) and has no program.
		add("Mj=(0,-1)");
		add("Aj=(-1.5,-1)");
		add("Bj=(1.5,-1)");
		add("lower=CircularArc(Mj,Aj,Bj)");
		add("Cj=Point(lower)");
		add("Kj=(0,3)");
		add("Oj=(0,0)");
		add("gj=Line(Cj,Kj)");
		add("gp=PerpendicularLine(Oj,gj)");
		add("Ej=Intersect(gj,gp)");
		add("pedal=LocusV2(Ej,Cj)");
		// Segment driver read only through homogeneous coordinates.
		add("sg=Segment((-3,-3),(6,6))");
		add("Cs=Point(sg)");
		add("gs=Line(Cs,xAxis)");
		add("hs:2x-y=0");
		add("Ms=Intersect(gs,hs)");
		add("seg=LocusV2(Ms,Cs)");
		// Direct scalar affine certificate and an R5 image of a construction locus.
		add("t=0");
		add("V=(0,t)");
		add("dom={false,{-2,2,true,true}}");
		add("affine=LocusV2(V,t,dom)");
		add("alpha=30deg");
		add("image=Rotate(a,alpha,A)");
		for (String label : List.of("a", "chain", "pedal", "seg", "affine", "image")) {
			LocusDefinition2D definition = locus(label).getSemanticDefinition();
			CertifiedIntervalCurveModel2D model =
					CertifiedIntervalCurveModel2D.capture(definition, BRANCH);
			assertNotNull(model, label);
			LocusInterval2D domain = definition.getProvider().getDeclaredDomain();
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
				for (int sample = 1; sample < 40; sample++) {
					double parameter = domain.getLower()
							+ (domain.getUpper() - domain.getLower()) * sample / 40;
					assertEncloses(label, model, definition, parameter, session);
				}
			}
		}
	}

	@Test
	void undecidedPredicatesRefuseTheBox() {
		getKernel().setContinuous(false);
		add("O0=(0,0)");
		add("c0=Circle(O0,2)");
		add("C0=Point(c0)");
		// The line through the driver and the center meets y=1 at infinity at t=0.
		add("g1=Line(C0,O0)");
		add("h1:y=1");
		add("E1=Intersect(g1,h1)");
		add("parallel=LocusV2(E1,C0)");
		// A join through the driver and the circle point at t=pi/2 is degenerate there.
		add("T0=(0,2)");
		add("g2=Line(C0,T0)");
		add("h2:y=-5");
		add("E2=Intersect(g2,h2)");
		add("coincident=LocusV2(E2,C0)");
		double[] degenerate = {0, Math.PI / 2};
		String[] labels = {"parallel", "coincident"};
		for (int index = 0; index < labels.length; index++) {
			String label = labels[index];
			CertifiedIntervalCurveModel2D model = CertifiedIntervalCurveModel2D.capture(
					locus(label).getSemanticDefinition(), BRANCH);
			assertNotNull(model, label);
			SplineOutwardInterval2D around = new SplineOutwardInterval2D(
					degenerate[index] - 0.01, degenerate[index] + 0.01);
			assertThrows(ArithmeticException.class, () -> model.evaluate(around, false),
					label);
			SplineOutwardInterval2D away = new SplineOutwardInterval2D(
					degenerate[index] + 1, degenerate[index] + 1.1);
			assertNotNull(model.evaluate(away, false), label);
		}
	}

	@Test
	void captureRefusesEveryShapeOutsideClassV1() {
		getKernel().setContinuous(false);
		witness();
		add("Ex=(x(C)/2,y(C))");
		add("expression=LocusV2(Ex,C)");
		add("lc=Line(C,(8,9))");
		add("far=Circle((0,0),20)");
		add("El=Intersect(lc,far,1)");
		add("conic=LocusV2(El,C)");
		add("s=0");
		add("Q=(s,s^2)");
		add("D2={false,{-2,2,true,true}}");
		add("parabola=LocusV2(Q,s,D2)");
		add("u=0");
		add("U=(u,0)");
		add("Du={false,{-2,2,true,true}}");
		add("inner=LocusV2(U,u,Du)");
		add("P=Point(inner,\"" + BRANCH + "\",0.5)");
		add("Q2=(x(P),x(P)^2)");
		add("nested=LocusV2(Q2,P)");
		for (String label : List.of("expression", "conic", "parabola", "nested")) {
			LocusDefinition2D definition = locus(label).getSemanticDefinition();
			assertNull(CertifiedIntervalCurveModel2D.capture(definition, BRANCH), label);
			assertTrue(assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
					definition.getEvaluatorCapability()).captureCertifiedProgram().isEmpty(),
					label);
		}
	}

	@Test
	void modelObligationsMatchTheContract() {
		getKernel().setContinuous(false);
		witness();
		LocusDefinition2D definition = locus("a").getSemanticDefinition();
		ConstructionIntervalModel2D model = assertInstanceOf(
				ConstructionIntervalModel2D.class,
				CertifiedIntervalCurveModel2D.capture(definition, BRANCH));
		LocusInterval2D domain = definition.getProvider().getDeclaredDomain();
		assertEquals(0, model.period(domain));
		double[] knots = model.getKnots();
		assertEquals(9, knots.length);
		assertEquals(domain.getLower(), knots[0]);
		assertEquals(domain.getUpper(), knots[8]);
		for (double parameter : new double[] {-7, -Math.PI, 0.3, Math.PI, 9}) {
			assertEquals(definition.getProvider().canonicalize(parameter),
					model.canonical(parameter));
		}
		SplineOutwardInterval2D outside = new SplineOutwardInterval2D(3, 3.2);
		assertFalse(model.isSmooth(outside));
		assertThrows(ArithmeticException.class, () -> model.evaluate(outside, false));
		assertTrue(model.isSmooth(new SplineOutwardInterval2D(-3, 3)));
		CertifiedConstructionProgram2D program = model.getProgram();
		assertEquals(CertifiedConstructionProgram2D.Driver.CIRCLE, program.getDriver());
		assertEquals(List.of(CertifiedConstructionProgram2D.Operation.PARALLEL,
				CertifiedConstructionProgram2D.Operation.MEET,
				CertifiedConstructionProgram2D.Operation.MIDPOINT),
				program.getSteps().stream().map(CertifiedConstructionProgram2D.Step::getOperation)
						.toList());
		assertTrue(program.getSignature().startsWith(CertifiedConstructionProgram2D.VERSION));
	}

	@Test
	void trigonometricEnclosureIsRigorousAcrossCriticalPoints() {
		Random random = new Random(20260923L);
		for (int index = 0; index < 4000; index++) {
			double a = (random.nextDouble() - 0.5) * 20;
			double b = a + random.nextDouble() * (index % 3 == 0 ? 4 : 1e-3);
			SplineOutwardInterval2D argument = new SplineOutwardInterval2D(a, b);
			SplineOutwardInterval2D cosine = ConstructionIntervalModel2D.cosine(argument);
			SplineOutwardInterval2D sine = ConstructionIntervalModel2D.sine(argument);
			assertTrue(cosine.lower >= -1 && cosine.upper <= 1);
			assertTrue(sine.lower >= -1 && sine.upper <= 1);
			for (int sample = 0; sample <= 16; sample++) {
				double x = a + (b - a) * sample / 16;
				assertTrue(cosine.lower <= StrictMath.cos(x) && StrictMath.cos(x) <= cosine.upper);
				assertTrue(sine.lower <= StrictMath.sin(x) && StrictMath.sin(x) <= sine.upper);
			}
		}
		for (int multiple = -8; multiple <= 8; multiple++) {
			double critical = multiple * Math.PI / 2;
			SplineOutwardInterval2D around = new SplineOutwardInterval2D(
					critical - 1e-9, critical + 1e-9);
			double extreme = Math.floorMod(multiple, 4) == 0 || Math.floorMod(multiple, 4) == 1
					? 1 : -1;
			SplineOutwardInterval2D value = Math.floorMod(multiple, 2) == 0
					? ConstructionIntervalModel2D.cosine(around)
					: ConstructionIntervalModel2D.sine(around);
			assertTrue(value.lower <= extreme && extreme <= value.upper, "k=" + multiple);
		}
		SplineOutwardInterval2D point = SplineOutwardInterval2D.point(0.7);
		assertTrue(ConstructionIntervalModel2D.cosine(point).width() < 1e-15);
		assertThrows(ArithmeticException.class, () -> ConstructionIntervalModel2D.sine(
				new SplineOutwardInterval2D(0, 2000)));
	}

	@Test
	void exhaustedCertificationBudgetIsUnresolved() {
		getKernel().setContinuous(false);
		witness();
		add("F=(-3,1)");
		add("G=(0.56,3.94)");
		add("H=(6.28,-1.36)");
		add("d=SplineV2({F,G,H},3)");
		LocusDefinition2D spline = locus("d").getSemanticDefinition();
		LocusDefinition2D ellipse = locus("a").getSemanticDefinition();
		LocusPairIntersectionPolicy2D initial =
				LocusPairIntersectionPolicy2D.initial(spline, ellipse);
		LocusPairIntersectionPolicy2D tiny = new LocusPairIntersectionPolicy2D(
				initial.getPolicyVersion(), initial.getFirstRootTolerance(),
				initial.getSecondRootTolerance(), initial.getFirstDeduplicationTolerance(),
				initial.getSecondDeduplicationTolerance(), initial.getResidualTolerance(),
				initial.getTangencyTolerance(), initial.getCoordinateTolerance(),
				initial.getCommonWorkBudget(), new LocusPairIntersectionWorkBudget2D(256,
						1024, 3, 16, 4096, 1024, 80, 16384, 4096, 4096, 256, 0));
		LocusPairIntersectionQuery2D query = new LocusPairIntersectionQuery2D(spline,
				ellipse, "e0e1-budget", "e0e1-topology", tiny);
		LocusDefinition2D first = query.isCallerOrderCanonical() ? spline : ellipse;
		LocusDefinition2D second = query.isCallerOrderCanonical() ? ellipse : spline;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(256)) {
			SplinePairIntervalCertification2D.Result result =
					SplinePairIntervalCertification2D.certify(new LocusPairIntersectionContext2D(
							query, first, second, session,
							new LocusPairIntersectionInstrumentation2D(query.getPolicy())));
			assertTrue(result.isSupported());
			assertEquals(2, result.getClasses().size());
			assertTrue(result.getClasses().stream().allMatch(group -> group.getStatus()
					== SplinePairIntervalCertification2D.ClassStatus.UNRESOLVED));
			assertTrue(result.getBoxesVisited() <= 3);
		}
	}

	@Test
	void globalCompletenessNeverMakesAnAmbiguousRootAdmissible() {
		getKernel().setContinuous(false);
		witness();
		add("W=SplineV2({(-3,2),(-1,2),(0.39,7),(1.8,2),(3.5,2)},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(W,a)");
		LocusIntersectionResult2D current = rich.getIntersectionResult();
		assertEquals(4, current.getFiniteSolutions().size());
		assertTrue(current.getDiagnostics().stream().map(Object::toString)
				.anyMatch(text -> text.contains("status=MULTIPLE")));
		// No capability publishes COMPLETE with roots today; a hypothetical complete
		// result over the same real solutions pins that completeness never upgrades
		// admissibility. Only the completeness evidence differs.
		IntersectionCompletenessEvidence2D evidence = current.getCompletenessEvidence();
		LocusIntersectionResult2D complete = new LocusIntersectionResult2D(
				current.getSourceBinding(), current.getComputationStatus(),
				new IntersectionCompletenessEvidence2D(Completeness.COMPLETE,
						evidence.getMethod(), evidence.getVerifiedRootCount(),
						evidence.getCoveredComponentKeys(), evidence.getDiagnostics()),
				current.getGeometryKind(), current.getCurrentness(),
				current.getSupportLevel(), current.getNumericGuarantee(),
				current.getFiniteSolutions(), current.getOverlapEvidence(),
				current.getWork(), current.getDiagnostics());
		for (LocusIntersectionSolution2D root : complete.getFiniteSolutions()) {
			assertTrue(complete.findPointAdmissibleSolution(
					root.getIdentity().getRootToken()).isEmpty());
		}
	}

	private static void assertEncloses(String label, CertifiedIntervalCurveModel2D model,
			LocusDefinition2D definition, double parameter,
			LocusEvaluationSession2D session) {
		LocusEvaluation2D evaluation = definition.evaluate(BRANCH, parameter, session);
		assertTrue(evaluation.isValid(), label + " t=" + parameter);
		SplineOutwardInterval2D[] value = model.evaluate(
				SplineOutwardInterval2D.point(parameter), false);
		double[] point = {evaluation.getPoint().getX(), evaluation.getPoint().getY()};
		for (int axis = 0; axis < 2; axis++) {
			double slack = 1e-11 * (1 + Math.abs(point[axis]));
			assertTrue(value[axis].lower - slack <= point[axis]
					&& point[axis] <= value[axis].upper + slack,
					label + " t=" + parameter + " axis=" + axis + " value=" + point[axis]
							+ " enclosure=[" + value[axis].lower + "," + value[axis].upper + "]");
			assertTrue(value[axis].width() < 1e-10 * (1 + Math.abs(point[axis])),
					label + " enclosure is not tight");
		}
		double step = 1e-6;
		LocusInterval2D domain = definition.getProvider().getDeclaredDomain();
		if (parameter - step > domain.getLower() && parameter + step < domain.getUpper()) {
			LocusEvaluation2D before = definition.evaluate(BRANCH, parameter - step, session);
			LocusEvaluation2D after = definition.evaluate(BRANCH, parameter + step, session);
			SplineOutwardInterval2D[] derivative = model.evaluate(
					SplineOutwardInterval2D.point(parameter), true);
			double[] difference = {
				(after.getPoint().getX() - before.getPoint().getX()) / (2 * step),
				(after.getPoint().getY() - before.getPoint().getY()) / (2 * step)};
			for (int axis = 0; axis < 2; axis++) {
				double midpoint = derivative[axis].midpoint();
				assertEquals(difference[axis], midpoint, 1e-5 * (1 + Math.abs(midpoint)),
						label + " derivative t=" + parameter + " axis=" + axis);
			}
		}
		SplineOutwardInterval2D box = new SplineOutwardInterval2D(
				Math.max(domain.getLower(), parameter - 1e-3),
				Math.min(domain.getUpper(), parameter + 1e-3));
		SplineOutwardInterval2D[] enclosure = model.evaluate(box, false);
		for (double inside : new double[] {box.lower, box.midpoint(), box.upper}) {
			if (!domain.contains(inside, 0)) {
				continue;
			}
			LocusEvaluation2D sample = definition.evaluate(BRANCH, inside, session);
			double[] values = {sample.getPoint().getX(), sample.getPoint().getY()};
			for (int axis = 0; axis < 2; axis++) {
				double slack = 1e-11 * (1 + Math.abs(values[axis]));
				assertTrue(enclosure[axis].lower - slack <= values[axis]
						&& values[axis] <= enclosure[axis].upper + slack,
						label + " box sample t=" + inside + " axis=" + axis);
			}
		}
	}

	private void witness() {
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("E=Midpoint(D,C)");
		add("a=LocusV2(E,C)");
	}

	private GeoLocusV2 locus(String label) {
		return assertInstanceOf(GeoLocusV2.class, lookup(label));
	}
}
