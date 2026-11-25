/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.locus.metric.LocusConditionedRouteJumpGuard2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Curve;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Route;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Session;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-B5a-R1 (L-6; {@code locus-v2-metrics.md} sections 28.1 and 28.3.1): an
 * evaluation of the chord-scaling check that fails inside the stretch is a failure of the
 * result, never a value. The rich result is {@code NUMERICAL_FAILURE} with an absent value and
 * the typed cause {@code EVALUATION_FAILED}, traceable to the failed evaluation; the scalar is
 * not admissible; nothing of the estimate survives as the current state of the revision; a
 * signature, an inconclusive leaf and an exhausted budget keep their own rules; caches and the
 * order of queries change nothing; a revision whose evaluation is valid again is admitted.
 */
final class PreG9BMaintB5aR1GuardFailureTest {
	private static final double LINE = Math.atan2(2, 1);

	private static Curve line() {
		return u -> PreG9BMaintB5aRoutes.along(0, 0, LINE, Math.sqrt(5) * u);
	}

	/**
	 * A parameter that only the check evaluates on the route [0, 1] of the straight line: the
	 * last evaluations of the computation are those of the check, which runs after the
	 * estimate (the selection of the B5a test of the same case).
	 */
	private static double checkOnlyParameter() {
		List<Double> evaluated = new ArrayList<>();
		Curve straight = line();
		Route recorded = PreG9BMaintB5aRoutes.route(u -> {
			evaluated.add(u);
			return straight.at(u);
		}, straight, 0, 1, 0, 1);
		assertTrue(recorded.admitted(), recorded.toString());
		long checked = recorded.checkEvaluations();
		assertEquals(4L * 64, checked);
		return evaluated.get(evaluated.size() - (int) checked / 2);
	}

	private static Curve failingAt(double target) {
		Curve straight = line();
		return u -> u == target ? null : straight.at(u);
	}

	private static Route measure(Session session, long revision, Curve curve) {
		return PreG9BMaintB5aRoutes.route(session, revision, curve, line(), parameter -> "a",
				0, 1, 0, 1);
	}

	/** The typed failure that R1 requires, with the cause traced to the failed evaluation. */
	private static void assertTypedFailure(Route route, double target) {
		assertEquals(MetricComputationStatus.NUMERICAL_FAILURE,
				route.result.getComputationStatus(), route.toString());
		assertEquals(MetricValueKind.ABSENT, route.result.getMetricValue().getKind(),
				"no length value is published");
		assertFalse(route.result.getMetricValue().getFiniteValue().isPresent());
		assertEquals(MetricGuaranteeClass2D.NOT_ESTABLISHED, route.result.getGuaranteeClass());
		assertFalse(route.admitted(), "the scalar adapter refuses it");
		assertFalse(route.result.getConditionedRoute().isPresent());
		assertTrue(route.result.getContributions().isEmpty(),
				"no partial measure survives as a contribution");
		assertTrue(route.result.getRouteEvidence().isPresent(), "the route stays as evidence");
		assertTrue(route.has(MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE));
		assertTrue(route.has(MetricDiagnosticCode2D.EVALUATION_FAILED));
		assertFalse(route.anyCheckDiagnostic(), "not a signature, inconclusive or budget");
		assertFalse(route.has(MetricDiagnosticCode2D.CONDITIONED_ROUTE_ADMITTED));
		String cause = route.message(MetricDiagnosticCode2D.EVALUATION_FAILED);
		assertTrue(cause.startsWith(LocusConditionedRouteJumpGuard2D.VERSION + ": "), cause);
		assertTrue(cause.contains("at " + target + " inside the stretch"), cause);
		assertTrue(cause.contains("status EVALUATION_FAILED"), cause);
		assertTrue(cause.contains("b5a fixture failure"), cause);
		assertTrue(cause.contains("is a failure of the result, never a value"), cause);
		String refusal = route.message(MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE);
		assertTrue(refusal.contains("publishes no value"), refusal);
	}

	/** Main regression: it fails on 65ca487da (SUCCESS with the estimate) and passes on R1. */
	@Test
	void aFailedEvaluationOfTheCheckIsATypedFailureWithNoValue() {
		double target = checkOnlyParameter();
		Route failing = PreG9BMaintB5aRoutes.route(failingAt(target), line(), 0, 1, 0, 1);
		assertTypedFailure(failing, target);
	}

	/** A signature, an inconclusive leaf and an exhausted budget keep the rules of B5a. */
	@Test
	void theOtherRefusalsKeepTheirEstimateAndStatus() {
		Route jump = PreG9BMaintB5aRoutes.route(u -> PreG9BMaintB5aRoutes.along(0, 0, LINE,
				Math.sqrt(5) * u + (u >= 0.75 ? 1 : 0)), 0.7, 0.8);
		Route budget = PreG9BMaintB5aRoutes.route(u -> new org.geocedg.common.kernel.locus
				.LocusPoint2D(u, Math.sin(40 * u)), 0, 1);
		for (Route route : new Route[] {jump, budget}) {
			assertEquals(MetricComputationStatus.SUCCESS, route.result.getComputationStatus(),
					route.toString());
			assertTrue(Double.isFinite(route.value()), route.toString());
			assertFalse(route.admitted());
			assertTrue(route.anyCheckDiagnostic(), route.toString());
			assertFalse(route.has(MetricDiagnosticCode2D.EVALUATION_FAILED), route.toString());
		}
		assertTrue(jump.has(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE));
		assertTrue(budget.has(MetricDiagnosticCode2D.CHORD_SCALING_BUDGET_EXHAUSTED));
	}

	/**
	 * Caches and query order: in one session the failing query, repeated and interleaved with
	 * an admitted query of another stretch, always fails the same way, and the admitted query
	 * keeps its value bit for bit whatever came before.
	 */
	@Test
	void cachesAndQueryOrderChangeNothing() {
		double target = checkOnlyParameter();
		Curve failing = failingAt(target);
		Route fresh = PreG9BMaintB5aRoutes.route(failing, line(), 0, 1, 0, 0.5);
		assertTrue(fresh.admitted(), "the half route never evaluates the target: " + fresh);
		Session first = new Session();
		Route failFirst = measure(first, 1, failing);
		Route okAfter = PreG9BMaintB5aRoutes.route(first, 1, failing, line(), parameter -> "a",
				0, 1, 0, 0.5);
		Route failAgain = measure(first, 1, failing);
		Session second = new Session();
		Route okFirst = PreG9BMaintB5aRoutes.route(second, 1, failing, line(), parameter -> "a",
				0, 1, 0, 0.5);
		Route failAfter = measure(second, 1, failing);
		for (Route route : new Route[] {failFirst, failAgain, failAfter}) {
			assertTypedFailure(route, target);
			assertEquals(diagnostics(failFirst), diagnostics(route));
		}
		for (Route route : new Route[] {okAfter, okFirst}) {
			assertTrue(route.admitted(), route.toString());
			assertEquals(Double.doubleToRawLongBits(fresh.value()),
					Double.doubleToRawLongBits(route.value()));
		}
	}

	/**
	 * Recovery: after a change of the construction (a new semantic revision) whose
	 * evaluation is valid again, the same session admits the route with the value of a fresh
	 * computation; nothing of the failure survives, and a revision that fails again fails.
	 */
	@Test
	void aRevisionWithValidEvaluationsRecovers() {
		double target = checkOnlyParameter();
		Route reference = PreG9BMaintB5aRoutes.route(line(), 0, 1);
		assertTrue(reference.admitted(), reference.toString());
		Session session = new Session();
		Route failed = measure(session, 1, failingAt(target));
		assertTypedFailure(failed, target);
		Route recovered = measure(session, 2, line());
		assertTrue(recovered.admitted(), recovered.toString());
		assertEquals(MetricComputationStatus.SUCCESS, recovered.result.getComputationStatus());
		assertEquals(Double.doubleToRawLongBits(reference.value()),
				Double.doubleToRawLongBits(recovered.value()));
		assertFalse(recovered.has(MetricDiagnosticCode2D.EVALUATION_FAILED));
		assertEquals(reference.checkEvaluations(), recovered.checkEvaluations());
		Route failedAgain = measure(session, 3, failingAt(target));
		assertTypedFailure(failedAgain, target);
	}

	private static List<String> diagnostics(Route route) {
		List<String> all = new ArrayList<>();
		for (MetricDiagnostic2D diagnostic : route.result.getDiagnostics()) {
			all.add(diagnostic.getCode() + ": " + diagnostic.getMessage());
		}
		assertNotNull(all);
		return all;
	}
}
