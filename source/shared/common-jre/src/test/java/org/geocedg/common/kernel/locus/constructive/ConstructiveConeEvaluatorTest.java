/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Failure;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Incidence;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.IncidenceResult;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Point;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Result;
import org.geocedg.common.kernel.locus.constructive.ExactQuadraticTower.Element;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.junit.jupiter.api.Test;

/**
 * The exact evaluation M3 in the constructive configuration and the rigorous enclosure of ideal
 * values ({@code locus-v2-constructive-authority.md} sections 1 to 3; E1 implementation design
 * section 6): the five mandatory contacts of the replica hold exactly with the rotation as a
 * substitute, points on paths keep their path (V-AUT-PROV-1), accidental binary64 coincidences
 * are decided exactly, the inherited decisions are replaced by strict signs and checked against
 * the live algorithm (V-AUT-NUM-3, V-AUT-NUM-4), and exhaustion of the budget fails closed.
 */
final class ConstructiveConeEvaluatorTest extends ConstructiveModelReplicaTest {

	private static final MathContext CONTEXT = new MathContext(60, RoundingMode.HALF_EVEN);

	@Test
	void theFiveMandatoryContactsHoldExactlyWithTheRotationAsTheOnlySubstitute() {
		buildAuthorReplica();
		String[][] contacts = {{"n", "J_1", "K"}, {"c_1", "J_1", "K"}, {"h_2", "K_1", "K"},
				{"c_1", "J_1", "J"}, {"r", "J_1", "J"}};
		for (String[] contact : contacts) {
			ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
			int target = builder.add(geo(contact[0]));
			int point = builder.addWithDriverSubstitution(geo(contact[1]), geo("E_1"),
					geo(contact[2]));
			ConstructiveCone cone = builder.build();
			ConstructiveConeEvaluator<Element> exact =
					ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1);

			IncidenceResult result = exact.incidence(point, target);

			String name = String.join("/", contact);
			assertEquals(Incidence.HOLDS, result.getIncidence(), name + ": " + result);
			assertEquals(List.of(cone.nodeOf(geo("Cr"))), result.getSubstitutes(),
					name + ": the decision holds for the substitute of the rotation");
			ExactEvaluationCounters counters = exact.getExactCounters();
			assertTrue(counters.getOperations() <= ExactEvaluationBudget.MAXIMUM_OPERATIONS,
					name + " " + counters);
			assertTrue(counters.getDepth() <= ExactEvaluationBudget.MAXIMUM_DEPTH);
		}
	}

	@Test
	void aRelationThatDoesNotHoldIsRefutedOnlyForTheSubstitutes() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int target = builder.add(geo("h"));
		int point = builder.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("K"));
		ConstructiveCone cone = builder.build();

		IncidenceResult result = ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1)
				.incidence(point, target);

		assertEquals(Incidence.REFUTED, result.getIncidence());
		assertFalse(result.getSubstitutes().isEmpty(),
				"a refutation with a substitute is not rigorous for the ideal objects");
	}

	@Test
	void withoutSubstitutesTheExactDecisionIsAboutTheIdealObjects() {
		add("A=(0.1,0.3)");
		add("B=(1.7,2.9)");
		add("f=Line(A,B)");
		add("M=Midpoint(A,B)");
		add("c=Circle(A,1)");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int line = builder.add(lookup("f"));
		int midpoint = builder.add(lookup("M"));
		int circle = builder.add(lookup("c"));
		ConstructiveConeEvaluator<Element> exact =
				ConstructiveConeEvaluator.exact(builder.build(), ExactEvaluationBudget.V1);

		IncidenceResult holds = exact.incidence(midpoint, line);
		IncidenceResult refuted = exact.incidence(midpoint, circle);

		assertEquals(Incidence.HOLDS, holds.getIncidence());
		assertTrue(holds.getSubstitutes().isEmpty());
		assertEquals(Incidence.REFUTED, refuted.getIncidence());
		assertTrue(refuted.getSubstitutes().isEmpty());
	}

	@Test
	void anAccidentalBinary64CoincidenceIsDecidedExactly() {
		add("O=(0,0)");
		add("P=(1,1)");
		add("first=Circle(O,P)");
		add("s=" + Math.sqrt(2));
		add("second=Circle(O,s)");
		add("X=Intersect(first,xAxis,2)");
		getKernel().updateConstruction(false);
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int point = builder.add(lookup("X"));
		int own = builder.add(lookup("first"));
		int other = builder.add(lookup("second"));
		ConstructiveConeEvaluator<Element> exact =
				ConstructiveConeEvaluator.exact(builder.build(), ExactEvaluationBudget.V1);

		assertEquals(Incidence.HOLDS, exact.incidence(point, own).getIncidence());
		assertEquals(Incidence.REFUTED, exact.incidence(point, other).getIncidence(),
				"V-CFG-3: the binary64 radius sqrt(2) is not the radius of the first circle");
	}

	@Test
	void aPointOnAPathKeepsItsPathAndItsBinary64CopyDoesNot() {
		add("A=(0.3,0.7)");
		add("B=(2.9,1.3)");
		add("f=Line(A,B)");
		add("c=Circle(A,B)");
		add("s=Segment(A,B)");
		add("P=Point(f)");
		place("P", 1.234567, 0.987654);
		add("R=Point(c)");
		place("R", 1.1, 2.3);
		add("S=Point(s)");
		place("S", 5, 2);
		GeoPoint onLine = (GeoPoint) lookup("P");
		GeoPoint onCircle = (GeoPoint) lookup("R");
		add("Pc=(" + onLine.getInhomX() + "," + onLine.getInhomY() + ")");
		add("Rc=(" + onCircle.getInhomX() + "," + onCircle.getInhomY() + ")");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int line = builder.add(lookup("f"));
		int circle = builder.add(lookup("c"));
		int segment = builder.add(lookup("s"));
		int pathPoint = builder.add(onLine);
		int circlePoint = builder.add(onCircle);
		int clipped = builder.add(lookup("S"));
		int end = builder.add(lookup("B"));
		int lineCopy = builder.add(lookup("Pc"));
		int circleCopy = builder.add(lookup("Rc"));
		ConstructiveCone cone = builder.build();
		ConstructiveConeEvaluator<Element> exact =
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1);

		assertEquals(Incidence.HOLDS, exact.incidence(pathPoint, line).getIncidence(),
				"V-AUT-PROV-1: the ideal point of a point on a line is on the line");
		assertEquals(Incidence.HOLDS, exact.incidence(circlePoint, circle).getIncidence());
		assertEquals(Incidence.HOLDS, exact.incidence(clipped, segment).getIncidence());
		assertSamePoint(exact, clipped, end, "a point beyond the end is clipped to it");
		assertEquals(Incidence.REFUTED, exact.incidence(lineCopy, line).getIncidence(),
				"a free point at the binary64 coordinates refutes the line");
		assertEquals(Incidence.REFUTED, exact.incidence(circleCopy, circle).getIncidence());
	}

	@Test
	void theInheritedSelectionGivesTheExactOutputs() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int first = builder.add(geo("J"));
		int second = builder.add(geo("K"));
		int centre = builder.add(geo("B"));
		ConstructiveConeEvaluator<Element> exact =
				ConstructiveConeEvaluator.exact(builder.build(), ExactEvaluationBudget.V1);
		Point<Element> j = point(exact, first);
		Point<Element> k = point(exact, second);
		Point<Element> b = point(exact, centre);
		ExactQuadraticTower tower = ((ConstructiveArithmetic.Exact) exact.getArithmetic())
				.getTower();

		assertTrue(k.getX().subtract(b.getX()).subtract(tower.of(5)).isZero(), "K = B + (5, 0)");
		assertTrue(k.getY().subtract(b.getY()).isZero());
		assertTrue(j.getX().subtract(b.getX()).add(tower.of(5)).isZero(), "J = B - (5, 0)");
		assertTrue(exact.getLiveChecks().stream().anyMatch(
				check -> check.getPredicate().equals("branch-correspondence")
						&& check.getSign() == 1), "the live outputs correspond");
	}

	@Test
	void aLiveOutputOnTheOtherBranchFailsClosed() {
		add("A=(0,0)");
		add("c=Circle(A,(3,0))");
		add("f=Line((-5,1),(5,1))");
		GeoElementND[] outputs = getElements("Intersect(c,f)");
		getKernel().updateConstruction(false);
		GeoPoint first = (GeoPoint) outputs[0].toGeoElement();
		GeoPoint second = (GeoPoint) outputs[1].toGeoElement();
		first.setCoords(second.getInhomX(), second.getInhomY(), 1);
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int node = builder.add(first);
		ConstructiveCone cone = builder.build();

		Result checked = ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1)
				.evaluate(node);
		Result unchecked = new ConstructiveConeEvaluator<>(cone,
				ConstructiveArithmetic.exact(new ExactQuadraticTower(ExactEvaluationBudget.V1)),
				false, Collections.emptyMap()).evaluate(node);

		assertEquals(Failure.BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED, checked.getFailure());
		assertTrue(unchecked.hasValue(), "without the live check the captured selection governs");
	}

	@Test
	void aLiveObjectUndefinedWhereTheIdealOneExistsFailsClosed() {
		add("A=(0,0)");
		add("B=(1E-12,2E-12)");
		GeoElement line = add("f=Line(A,B)");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int node = builder.add(line);
		ConstructiveCone cone = builder.build();

		Result result = ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1)
				.evaluate(node);

		if (line.isDefined()) {
			assertTrue(result.hasValue(), "the inherited line is defined and corresponds");
		} else {
			assertEquals(Failure.LIVE_DECISION_DISAGREES, result.getFailure(),
					"V-AUT-NUM-3: the tolerance of the live algorithm is never accepted");
		}
	}

	@Test
	void aNearTangencyThatTheInheritedToleranceDecidesFailsClosed() {
		// 1 + 2^-51 and 1 - 2^-52: the inherited tolerance declares both lines tangent to c,
		// with one output at the foot of the perpendicular from the centre and the unlabelled
		// second output suppressed or equal to it.
		double above = Math.nextUp(Math.nextUp(1.0));
		double below = Math.nextDown(Math.nextDown(1.0));
		add("O=(0,0)");
		add("c=Circle(O,1)");
		add("f=Line((-2," + above + "),(2," + above + "))");
		add("g=Line((-2," + below + "),(2," + below + "))");
		GeoElementND[] created = getElements("Intersect(c,f)");
		GeoElementND[] merged = getElements("Intersect(c,g)");
		getKernel().updateConstruction(false);
		double[] heights = {above, below};
		GeoElementND[][] pairs = {created, merged};
		for (int line = 0; line < 2; line++) {
			GeoPoint first = (GeoPoint) pairs[line][0].toGeoElement();
			GeoPoint second = (GeoPoint) pairs[line][1].toGeoElement();
			assertTrue(first.isDefined() && first.getInhomX() == 0
					&& first.getInhomY() == heights[line], "the inherited tangency: " + first);
			assertTrue(!second.isDefined() || second.getInhomX() == first.getInhomX()
					&& second.getInhomY() == first.getInhomY(), "one point: " + second);
		}
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int outside = builder.add(created[0].toGeoElement());
		int inside = builder.add(merged[0].toGeoElement());
		int circle = builder.add(lookup("c"));
		ConstructiveConeEvaluator<Element> exact =
				ConstructiveConeEvaluator.exact(builder.build(), ExactEvaluationBudget.V1);

		assertEquals(Failure.INVALID, exact.evaluate(outside).getFailure(),
				"V-AUT-NUM-3: the tolerance creates a point that the ideal line does not have");
		assertEquals(Incidence.NOT_DECIDED, exact.incidence(outside, circle).getIncidence());
		assertEquals(Failure.BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED,
				exact.evaluate(inside).getFailure(),
				"V-AUT-NUM-3: the tolerance merges the two ideal roots into one");
	}

	@Test
	void anExhaustedBudgetFailsClosedWithItsCounters() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int target = builder.add(geo("n"));
		int point = builder.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("K"));
		ConstructiveCone cone = builder.build();
		ExactEvaluationBudget small = ExactEvaluationBudget.restrictedTo(50, 8, 4096);

		IncidenceResult result = ConstructiveConeEvaluator.exact(cone, small)
				.incidence(point, target);

		assertEquals(Incidence.NOT_DECIDED, result.getIncidence());
		assertEquals(Failure.BUDGET_EXHAUSTED, result.getFailure().getFailure());
		assertTrue(result.getFailure().getMessage().contains("EXACT_EVALUATION_BUDGET_EXHAUSTED"),
				result.getFailure().getMessage());
	}

	@Test
	void theSignVectorIsDeterministic() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int target = builder.add(geo("c_1"));
		int point = builder.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("K"));
		ConstructiveCone cone = builder.build();
		ConstructiveConeEvaluator<Element> first =
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1);
		ConstructiveConeEvaluator<Element> second =
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1);

		first.incidence(point, target);
		second.incidence(point, target);

		assertFalse(first.getDecisions().isEmpty());
		assertEquals(first.getDecisions(), second.getDecisions());
		assertEquals(first.getExactCounters().toString(), second.getExactCounters().toString());
	}

	@Test
	void intervalsEncloseTheExactIdealValues() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		String[] labels = {"B", "J", "K", "I", "M", "L"};
		int[] nodes = new int[labels.length];
		for (int index = 0; index < labels.length; index++) {
			nodes[index] = builder.add(geo(labels[index]));
		}
		ConstructiveCone cone = builder.build();
		ConstructiveConeEvaluator<Element> exact =
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1);
		ConstructiveConeEvaluator<OutwardInterval> intervals =
				ConstructiveConeEvaluator.intervals(cone);
		ExactQuadraticTower tower = ((ConstructiveArithmetic.Exact) exact.getArithmetic())
				.getTower();

		for (int index = 0; index < labels.length; index++) {
			Point<Element> value = point(exact, nodes[index]);
			Point<OutwardInterval> enclosure = point(intervals, nodes[index]);
			assertEncloses(tower, enclosure.getX(), value.getX(), labels[index] + ".x");
			assertEncloses(tower, enclosure.getY(), value.getY(), labels[index] + ".y");
		}
	}

	@Test
	void theRotationOfAPointIsEnclosedRigorouslyAndIsASubstituteForTheExactEvaluation() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int rotation = builder.add(geo("Cr"));
		int centre = builder.add(geo("A"));
		int source = builder.add(geo("C"));
		ConstructiveCone cone = builder.build();
		Point<OutwardInterval> enclosure = point(ConstructiveConeEvaluator.intervals(cone),
				rotation);
		ConstructiveConeEvaluator<Element> exact =
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1);
		Point<Element> a = point(exact, centre);
		Point<Element> c = point(exact, source);

		BigDecimal angle = new BigDecimal(((org.geogebra.common.kernel.geos.GeoNumeric)
				geo("alpha")).getValue());
		BigDecimal cos = cos(angle);
		BigDecimal sin = sin(angle);
		BigDecimal dx = decimal(c.getX()).subtract(decimal(a.getX()));
		BigDecimal dy = decimal(c.getY()).subtract(decimal(a.getY()));
		BigDecimal x = decimal(a.getX()).add(cos.multiply(dx)).subtract(sin.multiply(dy));
		BigDecimal y = decimal(a.getY()).add(sin.multiply(dx)).add(cos.multiply(dy));
		assertTrue(new BigDecimal(enclosure.getX().getLower()).compareTo(x) <= 0
				&& x.compareTo(new BigDecimal(enclosure.getX().getUpper())) <= 0, "x of C'");
		assertTrue(new BigDecimal(enclosure.getY().getLower()).compareTo(y) <= 0
				&& y.compareTo(new BigDecimal(enclosure.getY().getUpper())) <= 0, "y of C'");
		assertTrue(enclosure.getX().width() < 1E-13 && enclosure.getY().width() < 1E-13);
		assertTrue(exact.dependsOnSubstitute(rotation),
				"the exact evaluation takes the binary64 state of the rotation");
	}

	@Test
	void aBoundaryObjectThatMovesWithTheDriverHasNoSubstitute() {
		add("A=(0,0)");
		add("c=Circle(A,(2,0))");
		add("E=Point(c)");
		add("w=30°");
		add("Q=Rotate(E,w,A)");
		add("D=(0,2)");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int traced = builder.addWithDriverSubstitution(lookup("Q"), lookup("E"), lookup("D"));
		ConstructiveCone cone = builder.build();

		assertEquals(Failure.OUT_OF_CLASS, ConstructiveConeEvaluator.exact(cone,
				ExactEvaluationBudget.V1).evaluate(traced).getFailure());
		Result enclosed = ConstructiveConeEvaluator.intervals(cone).evaluate(traced);
		assertTrue(enclosed.hasValue(), "the interval evaluation encloses the rotation of D");
	}

	@SuppressWarnings("unchecked")
	private static <T> Point<T> point(ConstructiveConeEvaluator<T> evaluator, int node) {
		Result result = evaluator.evaluate(node);
		assertTrue(result.hasValue(), String.valueOf(result));
		assertNotNull(result.getValue());
		return (Point<T>) result.getValue();
	}

	private static void assertSamePoint(ConstructiveConeEvaluator<Element> exact, int first,
			int second, String message) {
		Point<Element> p = point(exact, first);
		Point<Element> q = point(exact, second);
		assertTrue(p.getX().subtract(q.getX()).isZero() && p.getY().subtract(q.getY()).isZero(),
				message);
	}

	private static void assertEncloses(ExactQuadraticTower tower, OutwardInterval enclosure,
			Element value, String message) {
		assertTrue(value.subtract(tower.ofBinary64(enclosure.getLower())).signum() >= 0
				&& tower.ofBinary64(enclosure.getUpper()).subtract(value).signum() >= 0,
				message + " " + enclosure);
	}

	private static BigDecimal decimal(Element value) {
		return new BigDecimal(value.getRational().numerator())
				.divide(new BigDecimal(value.getRational().denominator()), CONTEXT);
	}

	/** Taylor series with 60 significant digits, an independent reference. */
	private static BigDecimal cos(BigDecimal angle) {
		return series(angle, 0);
	}

	private static BigDecimal sin(BigDecimal angle) {
		return series(angle, 1);
	}

	private static BigDecimal series(BigDecimal angle, int start) {
		BigDecimal term = start == 0 ? BigDecimal.ONE : angle;
		BigDecimal sum = term;
		BigDecimal square = angle.multiply(angle, CONTEXT);
		for (int n = start + 2; n < 80; n += 2) {
			term = term.multiply(square, CONTEXT).negate()
					.divide(BigDecimal.valueOf((long) n * (n - 1)), CONTEXT);
			sum = sum.add(term, CONTEXT);
		}
		return sum;
	}
}
