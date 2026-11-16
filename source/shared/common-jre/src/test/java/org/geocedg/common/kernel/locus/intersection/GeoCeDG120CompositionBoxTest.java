/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.intersection;

import static org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricBoxEvidence2D;
import org.junit.jupiter.api.Test;

/** Analytic oracles unrelated to the cone, including proof/value dependency separation. */
class GeoCeDG120CompositionBoxTest {

	private static CertifiedConstructionProgram2D program(Step... steps) {
		return new CertifiedConstructionProgram2D(Driver.SEGMENT, new double[] {0, 0, 0, 1},
				new LocusInterval2D(0, 1, true, true), List.of(steps));
	}

	private static Step step(CertifiedConstructionProgram2D.Operation operation,
			Operand... operands) {
		return new Step(operation, List.of(operands));
	}

	private static void contains(SplineOutwardInterval2D enclosure, double expected) {
		assertTrue(enclosure.lower <= expected && expected <= enclosure.upper,
				() -> enclosure.lower + " <= " + expected + " <= " + enclosure.upper);
	}

	@Test void nonlinearScalarCompositionEnclosesValuesAndAnalyticDerivatives() {
		var model = new ConstructionBoxModel2D(program(
				step(POINT_X, Operand.node(0)),
				step(SCALAR_SIN, Operand.node(1)),
				step(SCALAR_COS, Operand.node(1)),
				step(SCALAR_ADD, Operand.node(2), Operand.node(3)),
				step(SCALAR_ADD, Operand.node(1), Operand.scalar(2)),
				step(SCALAR_SQRT, Operand.node(5)),
				step(SCALAR_ADD, Operand.node(1), Operand.scalar(3)),
				step(SCALAR_DIVIDE, Operand.node(6), Operand.node(7)),
				step(POINT_COORDINATES, Operand.node(4), Operand.node(8))));
		var outcome = model.evaluate(.2, .4);
		assertEquals(ConstructionBoxOutcome2D.Kind.VALID, outcome.kind, outcome.detail);
		for (int i = 0; i <= 128; i++) {
			double t = .2 + .2 * i / 128;
			contains(outcome.value[0], Math.sin(t) + Math.cos(t));
			contains(outcome.value[1], Math.sqrt(t + 2) / (t + 3));
			contains(outcome.derivative[0], Math.cos(t) - Math.sin(t));
			contains(outcome.derivative[1], 1 / (2 * Math.sqrt(t + 2) * (t + 3))
					- Math.sqrt(t + 2) / ((t + 3) * (t + 3)));
		}
	}

	@Test void dependentRotationEnclosesADifferentCurveFromItsInitialPosition() {
		var model = new ConstructionBoxModel2D(program(
				step(POINT_X, Operand.node(0)),
				step(ROTATE_BY_ANGLE, Operand.point(3, 1, 1, 3, 1), Operand.node(1),
						Operand.point(1, 1, 1, 1, 1))));
		var outcome = model.evaluate(.2, .4);
		assertEquals(ConstructionBoxOutcome2D.Kind.VALID, outcome.kind, outcome.detail);
		for (int i = 0; i <= 128; i++) {
			double t = .2 + .2 * i / 128;
			contains(outcome.value[0], 1 + 2 * Math.cos(t));
			contains(outcome.value[1], 1 + 2 * Math.sin(t));
			contains(outcome.derivative[0], -2 * Math.sin(t));
			contains(outcome.derivative[1], 2 * Math.cos(t));
		}
	}

	@Test void undefinedArithmeticCannotAcquireAValidityCertificate() {
		for (var operation : List.of(SCALAR_DIVIDE, SCALAR_SQRT)) {
			var scalar = operation == SCALAR_DIVIDE
					? step(operation, Operand.node(2), Operand.scalar(0))
					: step(operation, Operand.node(2));
			var model = new ConstructionBoxModel2D(program(step(POINT_X, Operand.node(0)),
					step(SCALAR_NEGATE, Operand.node(1)), scalar,
					step(POINT_COORDINATES, Operand.node(3), Operand.scalar(0))));
			assertFalse(model.isStructurallyTotal());
			assertNotEquals(ConstructionBoxOutcome2D.Kind.VALID, model.evaluate(.2, .4).kind);
		}
	}

	@Test void variableRadiusAndAngleComposeThroughNativeEligibleRaySelection() {
		Operand center = Operand.point(0, 0, 1, 0, 0);
		var model = new ConstructionBoxModel2D(program(step(POINT_X, Operand.node(0)),
				step(SCALAR_ADD, Operand.node(1), Operand.scalar(2)),
				step(CIRCLE_DYNAMIC_RADIUS, center, Operand.node(2)),
				step(ROTATE_BY_ANGLE, Operand.point(1, 0, 1, 1, 0), Operand.node(1), center),
				step(RAY, center, Operand.node(4)),
				new Step(LINE_CIRCLE, List.of(Operand.node(5), Operand.node(3)), 1, 0, 1, 1, 0),
				new Step(INTERSECTION_OUTPUT, List.of(Operand.node(6)), 0)));
		var result = model.evaluate(.2, .21);
		assertEquals(ConstructionBoxOutcome2D.Kind.VALID, result.kind, result.detail);
		for (int i = 0; i <= 128; i++) {
			double t = .2 + .01 * i / 128;
			contains(result.value[0], (2 + t) * Math.cos(t));
			contains(result.value[1], (2 + t) * Math.sin(t));
			contains(result.derivative[0], Math.cos(t) - (2 + t) * Math.sin(t));
			contains(result.derivative[1], Math.sin(t) + (2 + t) * Math.cos(t));
		}
	}

	@Test void nestedMetricProofDependenciesAreNotIndependentImageInputs() {
		var program = program(new Step(METRIC_INPUT, List.of(Operand.node(0)), "inner"),
				step(POINT_COORDINATES, Operand.node(1), Operand.scalar(0)),
				new Step(METRIC_INPUT, List.of(Operand.node(2)), "outer"),
				step(POINT_COORDINATES, Operand.node(3), Operand.scalar(0)));
		List<Integer> requested = new ArrayList<>();
		var model = new ConstructionBoxModel2D(program, (node, lo, hi) -> {
			requested.add(node);
			return LocusMetricBoxEvidence2D.enclosed(2 * lo, 2 * hi, 2, 2, "analytic/v1");
		});
		assertEquals(List.of(3), model.metricInputs());
		var result = model.evaluate(.2, .4);
		assertEquals(ConstructionBoxOutcome2D.Kind.VALID, result.kind, result.detail);
		assertEquals(List.of(3), requested);
		contains(result.value[0], .6);
		contains(result.derivative[0], 2);
		assertFalse(model.isStructurallyTotal());
		assertNotEquals(ConstructionBoxOutcome2D.Kind.VALID,
				new ConstructionBoxModel2D(program).evaluate(.2, .4).kind);
	}

	@Test void scalarNativeZeroBranchesCannotCreateFiniteReciprocalCosineOrSine() {
		for (var operation : List.of(SCALAR_COS, SCALAR_SIN)) {
			double origin = operation == SCALAR_COS ? Math.PI / 2 : Math.PI;
			var model = new ConstructionBoxModel2D(program(step(POINT_X, Operand.node(0)),
					step(SCALAR_ADD, Operand.node(1), Operand.scalar(origin)),
					step(operation, Operand.node(2)),
					step(SCALAR_DIVIDE, Operand.scalar(1), Operand.node(3)),
					step(POINT_COORDINATES, Operand.node(4), Operand.scalar(0))));
			assertNotEquals(ConstructionBoxOutcome2D.Kind.VALID,
					model.evaluate(1E-10, 1E-10).kind);
		}
		var smallSine = new ConstructionBoxModel2D(program(step(POINT_X, Operand.node(0)),
				step(SCALAR_SIN, Operand.node(1)),
				step(SCALAR_DIVIDE, Operand.scalar(1), Operand.node(2)),
				step(POINT_COORDINATES, Operand.node(3), Operand.scalar(0))));
		assertEquals(ConstructionBoxOutcome2D.Kind.VALID,
				smallSine.evaluate(1E-10, 1E-10).kind, "MyDouble exempts a small sine argument");
	}

	@Test void idealRotationRetainsASeparateNativeGuardAndHomogeneousOverflowRefusal() {
		var snapped = new ConstructionBoxModel2D(program(step(POINT_X, Operand.node(0)),
				step(SCALAR_ADD, Operand.node(1), Operand.scalar(Math.PI / 2)),
				step(ROTATE_BY_ANGLE, Operand.point(1, 0, 1, 1, 0), Operand.node(2),
						Operand.point(0, 0, 1, 0, 0))));
		var result = snapped.evaluate(0, 0);
		assertEquals(ConstructionBoxOutcome2D.Kind.VALID, result.kind, result.detail);
		contains(result.value[0], Math.cos(Math.PI / 2));
		contains(result.value[1], 1);
		var overflow = new ConstructionBoxModel2D(program(step(POINT_X, Operand.node(0)),
				step(ROTATE_BY_ANGLE, Operand.point(1, 0, 2, .5, 0), Operand.node(1),
						Operand.point(1E308, 0, 1, 1E308, 0))));
		assertNotEquals(ConstructionBoxOutcome2D.Kind.VALID, overflow.evaluate(0, 0).kind);
	}

	@Test void admittedMetricAngleIsOpaqueToNativeAngleSensitiveConsumers() {
		Operand center = Operand.point(0, 0, 1, 0, 0);
		var rotation = program(new Step(METRIC_INPUT, List.of(Operand.node(0)), "angle-source"),
				step(SCALAR_DIVIDE, Operand.node(1), Operand.scalar(2)),
				step(ROTATE_BY_ANGLE, Operand.point(1, 0, 1, 1, 0), Operand.node(2), center));
		ConstructionBoxModel2D.MetricEnclosureProvider semantic = (node, lo, hi) ->
				LocusMetricBoxEvidence2D.enclosed(lo, hi, 1, 1, "analytic-angle/v1");
		assertEquals(ConstructionBoxOutcome2D.Kind.VALID,
				new ConstructionBoxModel2D(rotation, semantic, node -> true).evaluate(.2, .4).kind);
		assertNotEquals(ConstructionBoxOutcome2D.Kind.VALID,
				new ConstructionBoxModel2D(rotation, semantic, node -> false).evaluate(.2, .4).kind);
		var steps = new ArrayList<>(rotation.getSteps());
		steps.add(step(POINT_X, Operand.node(3)));
		steps.add(step(SCALAR_DIVIDE, Operand.scalar(1), Operand.node(4)));
		steps.add(step(POINT_COORDINATES, Operand.node(5), Operand.scalar(0)));
		var reciprocal = program(steps.toArray(Step[]::new));
		assertNotEquals(ConstructionBoxOutcome2D.Kind.VALID,
				new ConstructionBoxModel2D(reciprocal, semantic, node -> true).evaluate(.2, .4).kind,
				"True-integral bounds cannot prove a nonzero estimated native coordinate");
		assertTrue(rotation.getSignature().contains("composition-rotation-geometry/v1"));
	}

	@Test void coincidentCentresCannotSupplyTheSharedSourcePremiseOfRadialNormalization() {
		Operand center = Operand.point(0, 0, 1, 0, 0);
		Operand coincident = Operand.point(0, 0, 1, 0, 0);
		assertFalse(center.hasSameCapturedSource(coincident));
		var model = new ConstructionBoxModel2D(program(step(POINT_X, Operand.node(0)),
				step(ROTATE_BY_ANGLE, Operand.point(1, 0, 1, 1, 0), Operand.node(1), coincident),
				step(RAY, center, Operand.node(2)),
				step(SCALAR_ADD, Operand.node(1), Operand.scalar(2)),
				step(CIRCLE_DYNAMIC_RADIUS, center, Operand.node(4)),
				new Step(LINE_CIRCLE, List.of(Operand.node(3), Operand.node(5)), 1, 0, 1, 1, 0),
				new Step(INTERSECTION_OUTPUT, List.of(Operand.node(6)), 0)));
		assertNotEquals(ConstructionBoxOutcome2D.Kind.VALID, model.evaluate(.2, .3).kind);
	}

	@Test void proofServiceFailureRemainsUnestablishedRatherThanUsingAnEstimate() {
		AtomicInteger requested = new AtomicInteger();
		var model = new ConstructionBoxModel2D(program(
				new Step(METRIC_INPUT, List.of(Operand.node(0)), "unestablished"),
				step(POINT_COORDINATES, Operand.node(1), Operand.scalar(0))), (node, lo, hi) -> {
			requested.incrementAndGet();
			return null;
		});
		assertEquals(ConstructionBoxOutcome2D.Unresolved.METRIC_VALUE,
				model.evaluate(.2, .4).unresolved);
		assertEquals(1, requested.get());
	}
}
