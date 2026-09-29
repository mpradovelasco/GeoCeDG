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
import java.util.Map;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionPolicy2D.ResidualTolerance;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3-X1 certified construction class v2
 * ({@code locus-v2-certified-construction-model.md} section 5.2): capture of the
 * bounded expression-point translation, program versioning, the unchanged
 * capability boundary, interval correctness and fail-closed boxes. Coordinate
 * agreement with the floating evaluator is validation evidence only.
 */
class PreG9bR3X1CertifiedExpressionPointModelTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";
	/** Circle driver constants of the witness circle, as captured on the base. */
	private static final String WITNESS_DRIVER = "driver=CIRCLE[0x1.8d01a34b826d7p1,"
			+ "0x1.8d01a34b826d7p1,0x1.0p0,0x0.0p0,-0x0.0p0,0x1.0p0,"
			+ "0x1.8f5c28f5c28f6p-1,0x1.1eb851eb851ecp1]";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void theBoundedGrammarCapturesEveryAuthorizedExpressionForm() {
		driver();
		add("u=(1,0)");
		// Sum with a literal or a vector object, either operand order, and a
		// difference, which translates by the exactly negated vector.
		Map<String, double[]> forms = Map.of(
				"C+(1,0)", new double[] {1, 0},
				"C+u", new double[] {1, 0},
				"(1,0)+C", new double[] {1, 0},
				"u+C", new double[] {1, 0},
				"C-(1,0)", new double[] {-1, 0},
				"C-u", new double[] {-1, 0},
				"C+(0.5,-0.25)", new double[] {0.5, -0.25},
				"C+Vector((1,0))", new double[] {1, 0});
		int index = 0;
		for (Map.Entry<String, double[]> form : forms.entrySet()) {
			String locus = "x" + index++;
			add("E" + locus + "=" + form.getKey());
			add(locus + "=LocusV2(E" + locus + ",C)");
			CertifiedConstructionProgram2D program = program(locus);
			assertNotNull(program, form.getKey());
			assertEquals(CertifiedConstructionProgram2D.VERSION_V2, program.getVersion(),
					form.getKey());
			assertTrue(program.getSignature().startsWith(
					CertifiedConstructionProgram2D.VERSION_V2 + "|"), form.getKey());
			assertEquals(1, program.getSteps().size(), form.getKey());
			Step step = program.getSteps().get(0);
			assertEquals(Operation.EXPRESSION_TRANSLATE, step.getOperation(), form.getKey());
			assertEquals(0, step.getOperands().get(0).getNode(), form.getKey());
			assertEquals(form.getValue()[0], step.getParameter(0), 0, form.getKey());
			assertEquals(form.getValue()[1], step.getParameter(1), 0, form.getKey());
			assertNotNull(model(locus), form.getKey());
		}
	}

	@Test
	void classV1ProgramsKeepTheirVersionIdentifierAndSignature() {
		// Signatures captured on the implementation base 78134aa8: an X1 build must
		// reproduce them byte for byte.
		driver();
		add("u=(1,0)");
		add("Et=Translate(C,u)");
		add("control=LocusV2(Et,C)");
		add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("E=Midpoint(D,C)");
		add("a=LocusV2(E,C)");
		assertEquals(CertifiedConstructionProgram2D.VERSION + "|" + WITNESS_DRIVER
				+ "|TRANSLATE(n0)[0x1.0p0,0x0.0p0]", program("control").getSignature());
		assertEquals(CertifiedConstructionProgram2D.VERSION + "|" + WITNESS_DRIVER
				+ "|PARALLEL(n0,line[0x0.0p0,0x1.0p0,0x0.0p0])[]"
				+ "|MEET(line[-0x1.0p0,0x0.0p0,0x0.0p0],n1)[]|MIDPOINT(n2,n0)[]",
				program("a").getSignature());
		assertEquals("certified-construction-program/v1", CertifiedConstructionProgram2D.VERSION);
	}

	@Test
	void theVersionIsDerivedFromTheSteps() {
		driver();
		add("u=(1,0)");
		add("Et=Translate(C,u)");
		add("control=LocusV2(Et,C)");
		add("Ee=C+u");
		add("expression=LocusV2(Ee,C)");
		add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("Ew=Midpoint(D,C+(1,0))");
		add("witness=LocusV2(Ew,C)");
		assertEquals(CertifiedConstructionProgram2D.VERSION, program("control").getVersion());
		assertEquals(CertifiedConstructionProgram2D.VERSION_V2,
				program("expression").getVersion());
		// The R3 witness form: v1 steps around one expression-point step make v2.
		CertifiedConstructionProgram2D witness = program("witness");
		assertEquals(CertifiedConstructionProgram2D.VERSION_V2, witness.getVersion());
		assertEquals(List.of(Operation.PARALLEL, Operation.MEET,
				Operation.EXPRESSION_TRANSLATE, Operation.MIDPOINT),
				witness.getSteps().stream().map(Step::getOperation).toList());
		// The translation and the expression are distinct operations of equal value.
		assertEquals(program("control").getSignature().substring(
				CertifiedConstructionProgram2D.VERSION.length()).replace("TRANSLATE",
				"EXPRESSION_TRANSLATE"), program("expression").getSignature().substring(
				CertifiedConstructionProgram2D.VERSION_V2.length()));
	}

	@Test
	void expressionShapesOutsideTheGrammarStayWithoutAProgram() {
		driver();
		add("u=(1,0)");
		add("w=Vector(A,C)");
		String[] outside = {
				"(x(C)/2,y(C))", // the existing E0/E1 negative fixture
				"C+2u", "C+u+u", "C+(1,0)+(0,1)", // scaled and nested sums
				"C+A", // a point used as the vector
				"C+(x(A),0)", "C+(1/3,0)", // literal components that are expressions
				"C+(1;0)", // polar literal
				"(1,0)-C", // V - P is a point reflection composite, not a translation
				"C+w", // a driver-dependent vector
				"A+w", // a constant point translated by a driver-dependent vector
		};
		for (int index = 0; index < outside.length; index++) {
			String locus = "o" + index;
			add("E" + locus + "=" + outside[index]);
			GeoLocusV2 created = add(locus + "=LocusV2(E" + locus + ",C)");
			assertNotNull(created, outside[index]);
			LocusDefinition2D definition = created.getSemanticDefinition();
			assertNotNull(definition, outside[index]);
			assertNull(CertifiedIntervalCurveModel2D.capture(definition, BRANCH),
					outside[index]);
			assertTrue(evaluator(locus).captureCertifiedProgram().isEmpty(), outside[index]);
		}
	}

	@Test
	void nonfiniteOrUndefinedConstantVectorsAreRefused() {
		assertThrows(IllegalArgumentException.class, () -> new Step(
				Operation.EXPRESSION_TRANSLATE,
				List.of(CertifiedConstructionProgram2D.Operand.node(0)),
				Double.POSITIVE_INFINITY, 0));
		assertThrows(IllegalArgumentException.class, () -> new Step(
				Operation.EXPRESSION_TRANSLATE,
				List.of(CertifiedConstructionProgram2D.Operand.node(0)), 0, Double.NaN));
		// Through the public path an undefined or nonfinite vector makes the traced
		// point undefined, so the locus is undefined and no pair root is admissible.
		// The capture rule itself is pinned on live geos by PreG9bR3X1CaptureBoundaryTest.
		driver();
		add("u=(1,0)");
		add("E=C+u");
		GeoLocusV2 locus = add("p=LocusV2(E,C)");
		add("d=SplineV2({(-3,1),(0.56,3.94),(6.28,-1.36)},3)");
		org.geocedg.common.kernel.geos.GeoLocusIntersectionResult rich = add(
				"R=Intersect(d,p)");
		assertTrue(admissibleRoots(rich) > 0);
		for (double[] vector : new double[][] {{Double.NaN, 0},
				{Double.POSITIVE_INFINITY, 0}}) {
			org.geogebra.common.kernel.geos.GeoVector u =
					(org.geogebra.common.kernel.geos.GeoVector) lookup("u");
			u.setCoords(vector[0], vector[1], 0);
			u.updateCascade();
			assertFalse(lookup("E").isDefined(), "vector " + vector[0]);
			assertFalse(locus.isDefined(), "vector " + vector[0]);
			assertEquals(0, admissibleRoots(rich), "vector " + vector[0]);
			u.setCoords(1, 0, 0);
			u.updateCascade();
			assertNotNull(model("p"));
			assertTrue(admissibleRoots(rich) > 0);
		}
	}

	private static long admissibleRoots(
			org.geocedg.common.kernel.geos.GeoLocusIntersectionResult rich) {
		LocusIntersectionResult2D result = rich.getIntersectionResult();
		return result == null ? 0 : result.getFiniteSolutions().stream().filter(root ->
				rich.isPointAdmissible(root.getIdentity().getRootToken())).count();
	}

	@Test
	void everyV2FormEnclosesTheEvaluatorAndItsDerivative() {
		driver();
		add("u=(1,0)");
		add("E1=C+(1,0)");
		add("p1=LocusV2(E1,C)");
		add("E2=C+u");
		add("p2=LocusV2(E2,C)");
		add("E3=(0.5,0.25)+C");
		add("p3=LocusV2(E3,C)");
		add("E4=C-u");
		add("p4=LocusV2(E4,C)");
		add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("E5=Midpoint(D,C+(1,0))");
		add("p5=LocusV2(E5,C)");
		add("al=20deg");
		add("E6=Rotate(C+u,al)");
		add("p6=LocusV2(E6,C)");
		// A point operand with a non-unit homogeneous coordinate: the pedal meet.
		add("Mj=(0,-1)");
		add("Aj=(-1.5,-1)");
		add("Bj=(1.5,-1)");
		add("lower=CircularArc(Mj,Aj,Bj)");
		add("Cj=Point(lower)");
		add("Kj=(0,3)");
		add("Oj=(0,0)");
		add("gj=Line(Cj,Kj)");
		add("gp=PerpendicularLine(Oj,gj)");
		add("Ej=Intersect(gj,gp)+(0.5,0.25)");
		add("pedal=LocusV2(Ej,Cj)");
		// A segment driver read through its inhomogeneous coordinates.
		add("sg=Segment((-3,-3),(6,6))");
		add("Cs=Point(sg)");
		add("Es=Cs+(1,-2)");
		add("seg=LocusV2(Es,Cs)");
		for (String label : List.of("p1", "p2", "p3", "p4", "p5", "p6", "pedal", "seg")) {
			LocusDefinition2D definition = locus(label).getSemanticDefinition();
			CertifiedIntervalCurveModel2D model = model(label);
			assertNotNull(model, label);
			assertEquals(CertifiedConstructionProgram2D.VERSION_V2,
					program(label).getVersion(), label);
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
	void unresolvedAndInvalidBoxesFailClosed() {
		getKernel().setContinuous(false);
		add("O0=(0,0)");
		add("c0=Circle(O0,2)");
		add("C0=Point(c0)");
		// Away from the degenerate parameter, where the expression point is undefined.
		org.geogebra.common.kernel.geos.GeoPoint driver =
				(org.geogebra.common.kernel.geos.GeoPoint) lookup("C0");
		driver.setCoords(0, 2, 1);
		driver.updateCascade();
		// The meet through the driver and the centre is at infinity at t=0.
		add("g1=Line(C0,O0)");
		add("h1:y=1");
		add("E1=Intersect(g1,h1)+(1,0)");
		add("parallel=LocusV2(E1,C0)");
		CertifiedIntervalCurveModel2D model = model("parallel");
		assertNotNull(model);
		assertThrows(ArithmeticException.class, () -> model.evaluate(
				new SplineOutwardInterval2D(-0.01, 0.01), false));
		assertNotNull(model.evaluate(new SplineOutwardInterval2D(1, 1.1), false));
		assertThrows(ArithmeticException.class, () -> model.evaluate(
				new SplineOutwardInterval2D(3, 3.2), false));
		// A finite vector whose sum overflows the outward bounds refuses the box.
		add("big=(1.7976931348623157E308,0)");
		add("E2=C0+big");
		add("huge=LocusV2(E2,C0)");
		CertifiedIntervalCurveModel2D overflow = model("huge");
		assertNotNull(overflow);
		assertThrows(ArithmeticException.class, () -> overflow.evaluate(
				new SplineOutwardInterval2D(0.1, 0.2), false));
	}

	@Test
	void theFloatingVerificationStillRefusesACertifiedSlotItRejects() {
		// The coherence gate is unchanged: a certified UNIQUE class whose root the
		// floating verification rejects is never bound. With a residual tolerance
		// no floating root can meet, every certified slot of an expression-point
		// pair is refused, and nothing becomes point admissible.
		driver();
		add("E=C+(1,0)");
		add("p=LocusV2(E,C)");
		add("F=(-3,1)");
		add("G=(0.56,3.94)");
		add("H=(6.28,-1.36)");
		add("d=SplineV2({F,G,H},3)");
		LocusDefinition2D spline = locus("d").getSemanticDefinition();
		LocusDefinition2D expression = locus("p").getSemanticDefinition();
		LocusPairIntersectionPolicy2D initial =
				LocusPairIntersectionPolicy2D.initial(spline, expression);
		ResidualTolerance loose = initial.getResidualTolerance();
		LocusPairIntersectionPolicy2D strict = new LocusPairIntersectionPolicy2D(
				initial.getPolicyVersion(), initial.getFirstRootTolerance(),
				initial.getSecondRootTolerance(), initial.getFirstDeduplicationTolerance(),
				initial.getSecondDeduplicationTolerance(), new ResidualTolerance(
						loose.getQuantityKind(), loose.getUnits(), Double.MIN_VALUE, 0,
						loose.getCharacteristicScalePolicy()),
				initial.getTangencyTolerance(), initial.getCoordinateTolerance(),
				initial.getCommonWorkBudget(), initial.getPairWorkBudget());
		LocusPairIntersectionQuery2D query = new LocusPairIntersectionQuery2D(spline,
				expression, "x1-gate", "x1-gate-topology", strict);
		LocusDefinition2D first = query.isCallerOrderCanonical() ? spline : expression;
		LocusDefinition2D second = query.isCallerOrderCanonical() ? expression : spline;
		LocusIntersectionTokenLedger2D.Evaluation evaluation =
				new LocusIntersectionTokenLedger2D().begin("x1-gate-owner",
						query.getSourcePairIdentity(), "x1-gate", "x1-gate-topology");
		LocusIntersectionResult2D result = new LocusPairIntersectionSolver2D().intersect(
				query, first, second, new IntersectionSourceBinding2D(query), null,
				lineage -> "x1-gate/" + lineage, evaluation);
		List<String> diagnostics = result.getDiagnostics().stream().map(Object::toString)
				.toList();
		assertTrue(diagnostics.stream().anyMatch(text -> text.contains("status=UNIQUE")),
				diagnostics.toString());
		assertTrue(diagnostics.stream().anyMatch(text -> text.contains(
				"Certified slot refused by current evaluator/contact validation")),
				diagnostics.toString());
		for (LocusIntersectionSolution2D root : result.getFiniteSolutions()) {
			assertTrue(result.findPointAdmissibleSolution(
					root.getIdentity().getRootToken()).isEmpty());
		}
	}

	@Test
	void theModelObligationsOfAV2ProgramMatchTheContract() {
		driver();
		add("E=C+(1,0)");
		add("p=LocusV2(E,C)");
		LocusDefinition2D definition = locus("p").getSemanticDefinition();
		ConstructionIntervalModel2D model = assertInstanceOf(
				ConstructionIntervalModel2D.class, model("p"));
		LocusInterval2D domain = definition.getProvider().getDeclaredDomain();
		assertEquals(0, model.period(domain));
		assertEquals(9, model.getKnots().length);
		assertFalse(model.isSmooth(new SplineOutwardInterval2D(3, 3.2)));
		assertTrue(model.isSmooth(new SplineOutwardInterval2D(-3, 3)));
		for (double parameter : new double[] {-7, -Math.PI, 0.3, Math.PI, 9}) {
			assertEquals(definition.getProvider().canonicalize(parameter),
					model.canonical(parameter));
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

	private void driver() {
		getKernel().setContinuous(false);
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
	}

	private CertifiedConstructionProgram2D program(String label) {
		return evaluator(label).captureCertifiedProgram().orElse(null);
	}

	private ReconstructibleLocusEvaluator2D evaluator(String label) {
		return assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
				locus(label).getSemanticDefinition().getEvaluatorCapability());
	}

	private CertifiedIntervalCurveModel2D model(String label) {
		LocusDefinition2D definition = locus(label).getSemanticDefinition();
		return definition == null ? null
				: CertifiedIntervalCurveModel2D.capture(definition, BRANCH);
	}

	private GeoLocusV2 locus(String label) {
		return assertInstanceOf(GeoLocusV2.class, lookup(label));
	}
}
