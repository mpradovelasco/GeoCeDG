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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.metric.LocusConditionedRouteJumpGuard2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Curve;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Route;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-B5a ({@code locus-v2-metrics.md} section 28.3.1): each of the three
 * diagnostics of the check is produced by a route and is distinct from the other two; a
 * failed evaluation of the check inside the stretch is a failure of the result; the version
 * and the evaluations are recorded. In each case the estimate stays in the rich result and
 * is no number.
 */
final class PreG9BMaintB5aDiagnosticsTest {
	private static final double LINE = Math.atan2(2, 1);
	private static final Set<MetricDiagnosticCode2D> CHECK = EnumSet.of(
			MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE,
			MetricDiagnosticCode2D.CHORD_SCALING_INCONCLUSIVE,
			MetricDiagnosticCode2D.CHORD_SCALING_BUDGET_EXHAUSTED);

	private static Curve line() {
		return u -> PreG9BMaintB5aRoutes.along(0, 0, LINE, Math.sqrt(5) * u);
	}

	private static Curve jump(double size, double at) {
		return u -> PreG9BMaintB5aRoutes.along(0, 0, LINE, Math.sqrt(5) * u
				+ (u >= at ? size : 0));
	}

	/** @return the check diagnostics of the route, in their order */
	private static List<MetricDiagnosticCode2D> checkCodes(Route route) {
		List<MetricDiagnosticCode2D> codes = new ArrayList<>();
		route.result.getDiagnostics().forEach(d -> {
			if (CHECK.contains(d.getCode())) {
				codes.add(d.getCode());
			}
		});
		return codes;
	}

	private static void assertRefusedWithItsEstimate(Route route) {
		assertEquals(MetricComputationStatus.SUCCESS, route.result.getComputationStatus(),
				route.toString());
		assertTrue(Double.isFinite(route.value()), route.toString());
		assertFalse(route.admitted(), route.toString());
		assertFalse(route.result.getConditionedRoute().isPresent());
		assertTrue(route.has(MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE));
		assertTrue(route.has(MetricDiagnosticCode2D.EXISTENCE_NOT_CERTIFIED));
		assertFalse(route.has(MetricDiagnosticCode2D.CONDITIONED_ROUTE_ADMITTED));
	}

	private static void assertRecorded(String message) {
		assertTrue(message.startsWith(LocusConditionedRouteJumpGuard2D.VERSION + ": "),
				message);
		assertTrue(message.contains("leaf "), message);
		assertTrue(message.contains("ratios ["), message);
		assertTrue(message.contains("evaluation(s) spent by the check"), message);
	}

	@Test
	void aJumpSignatureIsItsOwnDiagnostic() {
		Route route = PreG9BMaintB5aRoutes.route(jump(1, 0.75), 0.7, 0.8);
		assertRefusedWithItsEstimate(route);
		assertEquals(List.of(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE),
				checkCodes(route));
		String message = route.message(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE);
		assertRecorded(message);
		assertTrue(message.contains("decision interval ["), message);
		// The jump at 0.75 is the upper end of leaf 32, [0.7484375, 0.75]; three ratios above
		// the threshold, three evaluations.
		assertTrue(message.contains("leaf 32 [0.7484375, 0.75]"), message);
		assertTrue(message.contains("3 evaluation(s) on the leaf"), message);
	}

	/**
	 * A route a few units in the last place wide around a jump: its cells are narrower than
	 * the refinement stencil, the estimator accepts their chords (section 25.12, item 3), and
	 * the check reaches the parameter resolution with the chord of the jump above its
	 * resolution: it cannot decide.
	 */
	@Test
	void anInconclusiveLeafIsItsOwnDiagnostic() {
		double at = 0.5 + 0x1p-50;
		Route route = PreG9BMaintB5aRoutes.route(jump(1, at), at - 4 * Math.ulp(at),
				at + 4 * Math.ulp(at));
		assertRefusedWithItsEstimate(route);
		assertEquals(List.of(MetricDiagnosticCode2D.CHORD_SCALING_INCONCLUSIVE),
				checkCodes(route));
		String message = route.message(MetricDiagnosticCode2D.CHORD_SCALING_INCONCLUSIVE);
		assertRecorded(message);
		assertTrue(message.contains("parameter resolution reached"), message);
		assertTrue(message.contains("could not decide on "), message);
		assertTrue(route.value() > 1, "the estimate includes the jump: " + route.value());
	}

	/**
	 * A continuous route with more leaves than the budget of the check covers (4 096 leaves
	 * at 4 evaluations each): the check stops, concludes nothing about the leaves left
	 * untested, and the route is no number. The estimator itself succeeds.
	 */
	@Test
	void anExhaustedBudgetIsItsOwnDiagnostic() {
		Route route = PreG9BMaintB5aRoutes.route(u -> new LocusPoint2D(u, Math.sin(40 * u)),
				0, 1);
		assertRefusedWithItsEstimate(route);
		assertEquals(List.of(MetricDiagnosticCode2D.CHORD_SCALING_BUDGET_EXHAUSTED),
				checkCodes(route));
		String message = route.message(MetricDiagnosticCode2D.CHORD_SCALING_BUDGET_EXHAUSTED);
		assertTrue(message.startsWith(LocusConditionedRouteJumpGuard2D.VERSION), message);
		assertTrue(message.contains("budget of 16384 evaluations per route"), message);
		assertTrue(message.contains("nothing is concluded about the leaves left untested"),
				message);
		assertTrue(message.contains("16384 evaluation(s) spent by the check"), message);
		// A route of the same curve that the budget covers is a number.
		Route shorter = PreG9BMaintB5aRoutes.route(u -> new LocusPoint2D(u,
				Math.sin(40 * u)), 0, 0.5);
		assertTrue(shorter.admitted(), shorter.toString());
	}

	@Test
	void theThreeDiagnosticsAreDistinct() {
		assertEquals(3, CHECK.size());
		Set<String> names = new java.util.HashSet<>();
		for (MetricDiagnosticCode2D code : CHECK) {
			names.add(code.name());
			assertNotEquals(MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE, code);
		}
		assertEquals(3, names.size());
	}

	/**
	 * A failed evaluation of the traced point at a parameter of the check, inside the
	 * stretch, is a failure of the result (section 28.1): no number. The parameter is one
	 * that only the check evaluates (the last evaluations of the computation are those of the
	 * check, which runs after the estimate).
	 */
	@Test
	void aFailedEvaluationOfTheCheckIsNoNumber() {
		List<Double> evaluated = new ArrayList<>();
		Curve straight = line();
		Route recorded = PreG9BMaintB5aRoutes.route(u -> {
			evaluated.add(u);
			return straight.at(u);
		}, straight, 0, 1, 0, 1);
		assertTrue(recorded.admitted(), recorded.toString());
		long checked = recorded.checkEvaluations();
		assertEquals(4L * 64, checked);
		double target = evaluated.get(evaluated.size() - (int) checked / 2);
		Route failing = PreG9BMaintB5aRoutes.route(u -> u == target ? null : straight.at(u),
				straight, 0, 1, 0, 1);
		assertRefusedWithItsEstimate(failing);
		assertTrue(failing.has(MetricDiagnosticCode2D.EVALUATION_FAILED), failing.toString());
		assertEquals(List.of(), checkCodes(failing));
		assertTrue(failing.message(MetricDiagnosticCode2D.EVALUATION_FAILED)
				.contains("is a failure of the result, never a value"));
		assertEquals(recorded.value(), failing.value(), 0, "the estimate is the same");
	}
}
