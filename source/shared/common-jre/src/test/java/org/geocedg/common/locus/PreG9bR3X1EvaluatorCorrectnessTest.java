/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3-X1 repairs of the two retained reconstructible-evaluator debts. The
 * floating evaluator is corrected first; the certified model is only a consumer
 * of it. On the implementation base these tests fail: the segment fixture lags one
 * parameter behind (its between-points length is 2.25 instead of 1) and the
 * inline-literal fixture is created undefined.
 */
class PreG9bR3X1EvaluatorCorrectnessTest extends G9U0PublicSurfaceTestBase {

	@Test
	void aSegmentDriverIsCurrentAtEveryRequestedParameter() {
		// TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE, the recorded E0/E1 fixture: the
		// midpoint of the segment point (4t, 0) and K = (0, 2) is exactly (2t, 1).
		getKernel().setContinuous(false);
		add("sa=Segment((0,0),(4,0))");
		add("Cs=Point(sa)");
		add("K=(0,2)");
		GeoLocusV2 locus = add("ls=LocusV2(Midpoint(Cs,K),Cs)");
		double[] order = {0.1, 0.25, 0.5, 0.75, 0.9, 0, 1, 0.6, 0.2, 0.95, 0.05, 0.5, 0.1};
		for (double y : new double[] {1, 1.5, -0.25}) {
			moveTo("K", 0, 2 * y);
			LocusDefinition2D definition = locus.getSemanticDefinition();
			for (double t : order) {
				LocusEvaluation2D value = evaluate(definition, t);
				assertTrue(value.isValid(), "t=" + t);
				assertEquals(2 * t, value.getPoint().getX(), 0, "t=" + t + " y=" + y);
				assertEquals(y, value.getPoint().getY(), 0, "t=" + t + " y=" + y);
			}
		}
		moveTo("K", 0, 2);
		GeoPoint first = semanticPoint(locus, "Pa", 0.25);
		GeoPoint second = semanticPoint(locus, "Pb", 0.75);
		assertEquals(0.5, first.getInhomX(), 1E-12);
		assertEquals(1.5, second.getInhomX(), 1E-12);
		// The author's recorded wrong value: 2.25 on the base, where 1 is expected.
		assertEquals(1, finite(add("between=LocusLength(ls,Pa,Pb)")), 1E-9);
		GeoNumeric lengthBetween = add("lengthBetween=Length(ls,Pa,Pb)");
		assertEquals(1, lengthBetween.getDouble(), 1E-9);
		assertEquals(2, finite(add("total=LocusLength(ls)")), 1E-9);
		// The certified model now agrees with the evaluator, so it is available.
		assertEquals(CertifiedConstructionProgram2D.VERSION, program(locus).getVersion());
	}

	@Test
	void theEvaluatorReproducesTheLiveConstructionForEveryPathDriver() {
		getKernel().setContinuous(false);
		add("K=(0.5,2)");
		add("sa=Segment((-1,-2),(3,1))");
		add("Cs=Point(sa)");
		add("Ms=Midpoint(Cs,K)");
		GeoLocusV2 segment = add("segment=LocusV2(Ms,Cs)");
		add("c=Circle((1,0),2)");
		add("Cc=Point(c)");
		add("Mc=Midpoint(Cc,K)");
		GeoLocusV2 circle = add("circle=LocusV2(Mc,Cc)");
		add("arc=CircularArc((0,0),(2,0),(0,2))");
		add("Ca=Point(arc)");
		add("Ma=Midpoint(Ca,K)");
		GeoLocusV2 arc = add("arcLocus=LocusV2(Ma,Ca)");
		Object[][] drivers = {{segment, "Cs", "sa", "Ms"}, {circle, "Cc", "c", "Mc"},
				{arc, "Ca", "arc", "Ma"}};
		for (Object[] driver : drivers) {
			GeoLocusV2 locus = (GeoLocusV2) driver[0];
			LocusDefinition2D definition = locus.getSemanticDefinition();
			double lower = definition.getProvider().getDeclaredDomain().getLower();
			double upper = definition.getProvider().getDeclaredDomain().getUpper();
			for (int sample = 0; sample <= 16; sample++) {
				double t = lower + (upper - lower) * ((sample * 7) % 17) / 17.0;
				LocusEvaluation2D value = evaluate(definition, t);
				GeoPoint live = placeOnPath((String) driver[1], (String) driver[2], t,
						(String) driver[3]);
				// The same formula sequence on the same inputs gives the same binary64.
				assertEquals(live.getInhomX(), value.getPoint().getX(), 0,
						driver[1] + " t=" + t);
				assertEquals(live.getInhomY(), value.getPoint().getY(), 0,
						driver[1] + " t=" + t);
			}
		}
	}

	@Test
	void theInlineLiteralFixtureKeepsItsDriverAndMatchesItsSource() {
		// TD-LOCUS-EVALUATOR-INLINE-LITERAL-DEPENDENCY, the retained fixture: two
		// commands of the slice take an inline literal.
		getKernel().setContinuous(false);
		add("Mj=(0,-1)");
		add("Aj=(-1.5,-1)");
		add("Bj=(1.5,-1)");
		add("lower=CircularArc(Mj,Aj,Bj)");
		add("Cj=Point(lower)");
		add("gj=Line(Cj,(0,3))");
		add("gp=PerpendicularLine((0,0),gj)");
		add("Ej=Intersect(gj,gp)");
		GeoLocusV2 literal = add("literal=LocusV2(Ej,Cj)");
		assertTrue(literal.isDefined());
		// The same construction with named points, as independent validation.
		add("Kj=(0,3)");
		add("Oj=(0,0)");
		add("gn=Line(Cj,Kj)");
		add("gq=PerpendicularLine(Oj,gn)");
		add("En=Intersect(gn,gq)");
		GeoLocusV2 named = add("named=LocusV2(En,Cj)");
		assertLiteralLocusIsCurrent(literal, named);
		// The certified model is available only now that the evaluator is correct.
		CertifiedConstructionProgram2D program = program(literal);
		assertEquals(List.of(CertifiedConstructionProgram2D.Operation.JOIN,
				CertifiedConstructionProgram2D.Operation.PERPENDICULAR,
				CertifiedConstructionProgram2D.Operation.MEET),
				program.getSteps().stream().map(CertifiedConstructionProgram2D.Step::getOperation)
						.toList());
		double length = finite(add("Lliteral=LocusLength(literal)"));
		assertEquals(finite(add("Lnamed=LocusLength(named)")), length, 1E-9);
		assertTrue(length > 2, String.valueOf(length));
		// A reopened document evaluates the same curve.
		getApp().setXML(getApp().getXML(), true);
		assertLiteralLocusIsCurrent((GeoLocusV2) requireLookup("literal"),
				(GeoLocusV2) requireLookup("named"));
	}

	private void assertLiteralLocusIsCurrent(GeoLocusV2 literal, GeoLocusV2 named) {
		assertTrue(literal.isDefined());
		LocusDefinition2D definition = literal.getSemanticDefinition();
		assertNotNull(definition);
		LocusDefinition2D reference = named.getSemanticDefinition();
		double minimum = Double.MAX_VALUE;
		double maximum = -Double.MAX_VALUE;
		for (int sample = 1; sample < 20; sample++) {
			double t = sample / 20.0;
			LocusEvaluation2D value = evaluate(definition, t);
			assertTrue(value.isValid(), "t=" + t);
			GeoPoint live = placeOnPath("Cj", "lower", t, "Ej");
			assertEquals(live.getInhomX(), value.getPoint().getX(), 0, "t=" + t);
			assertEquals(live.getInhomY(), value.getPoint().getY(), 0, "t=" + t);
			LocusEvaluation2D expected = evaluate(reference, t);
			assertEquals(expected.getPoint().getX(), value.getPoint().getX(), 1E-12);
			assertEquals(expected.getPoint().getY(), value.getPoint().getY(), 1E-12);
			minimum = Math.min(minimum, value.getPoint().getX());
			maximum = Math.max(maximum, value.getPoint().getX());
		}
		// Not one fixed point: the locus moves with its driver.
		assertTrue(maximum - minimum > 1, minimum + " " + maximum);
	}

	/**
	 * Places the live driver as {@code AlgoPointOnPath} does and returns the live
	 * traced point.
	 */
	private GeoPoint placeOnPath(String driver, String path, double t, String traced) {
		GeoPoint point = (GeoPoint) requireLookup(driver);
		point.getPathParameter().setT(t);
		((Path) requireLookup(path)).pathChanged(point);
		point.updateCoords();
		point.updateCascade();
		return (GeoPoint) requireLookup(traced);
	}

	private static LocusEvaluation2D evaluate(LocusDefinition2D definition, double t) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(1)) {
			return definition.evaluate(BRANCH, t, session);
		}
	}

	private GeoPoint semanticPoint(GeoLocusV2 locus, String label, double parameter) {
		GeoPoint point = add(label + "=Point(" + locus.getLabelSimple() + ",\"" + BRANCH
				+ "\"," + parameter + ")");
		assertNotNull(point);
		return point;
	}

	private static CertifiedConstructionProgram2D program(GeoLocusV2 locus) {
		return assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
				locus.getSemanticDefinition().getEvaluatorCapability())
				.captureCertifiedProgram().orElseThrow();
	}

	private static double finite(GeoElement result) {
		GeoLocusMetricResult metric = assertInstanceOf(GeoLocusMetricResult.class, result);
		return metric.getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow(() -> new AssertionError(metric.getMetricResult()
						.getComputationStatus() + " " + metric.getMetricResult().getDiagnostics()));
	}

	private void moveTo(String label, double x, double y) {
		GeoPoint point = (GeoPoint) requireLookup(label);
		point.setCoords(x, y, 1);
		point.updateCascade();
	}
}
