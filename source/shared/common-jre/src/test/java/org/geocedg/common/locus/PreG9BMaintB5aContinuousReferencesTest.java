/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.metric.LocusConditionedRouteJumpGuard2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Curve;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Route;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-B5a, no regression of admissibility ({@code locus-v2-metrics.md} section
 * 28.3.1, validation): continuous references stay numbers, with the evaluations of the check
 * recorded and bounded: the parametrizations {@code u -> u^3} and {@code u -> u^8},
 * stationary points of orders 1 and 2 (and higher, up to the order where the analysis of the
 * policy puts the first signature), narrow closed loops and high curvature, and a speed that
 * diverges with {@code alpha} below the threshold of the policy. The divergent speed beyond
 * the threshold is the conservative refusal that section 28.3.1 declares.
 */
final class PreG9BMaintB5aContinuousReferencesTest {
	private static final double LINE = Math.atan2(2, 1);

	private static void assertAdmittedAndBounded(Route route, String where) {
		assertTrue(route.admitted(), where + ": " + route);
		assertFalse(route.anyCheckDiagnostic(), where + ": " + route);
		long evaluations = route.checkEvaluations();
		int leaves = route.leavesTested();
		assertTrue(leaves > 0, where);
		assertTrue(evaluations <= (long) LocusConditionedRouteJumpGuard2D.LEVELS_PER_LEAF
				* leaves, where + ": " + evaluations + " for " + leaves + " leaves");
		assertTrue(evaluations <= LocusConditionedRouteJumpGuard2D.EVALUATION_CAP, where);
	}

	@Test
	void nonUniformParametrizationsStayNumbers() {
		Route cube = PreG9BMaintB5aRoutes.route(u -> new LocusPoint2D(u * u * u, u), 0, 1);
		assertAdmittedAndBounded(cube, "u^3");
		// A leaf without signature costs L - m + 1 = 4 evaluations.
		assertEquals(93, cube.leavesTested());
		assertEquals(4L * 93, cube.checkEvaluations());
		Route eighth = PreG9BMaintB5aRoutes.route(u -> new LocusPoint2D(Math.pow(u, 8), u * u),
				0, 1);
		assertAdmittedAndBounded(eighth, "u^8");
		assertEquals(145, eighth.leavesTested());
		assertEquals(4L * 145, eighth.checkEvaluations());
	}

	/** A stationary point of order {@code k} of the arc {@code (s, e^s)} at {@code at}. */
	private static Curve stationary(int order, double at) {
		int power = order + 1;
		return u -> {
			double d = u - at;
			double s = at + Math.signum(d) * Math.pow(Math.abs(d), power);
			return new LocusPoint2D(s, Math.exp(s));
		};
	}

	@Test
	void stationaryPointsStayNumbers() {
		for (int order : new int[] {1, 2, 3, 4, 6, 8, 12, 16, 20, 26, 30}) {
			for (double at : new double[] {0.37, 0.5, 0.25, 0.123456}) {
				Route route = PreG9BMaintB5aRoutes.route(stationary(order, at), 0, 1);
				assertAdmittedAndBounded(route, "order " + order + " at " + at);
				if (order <= 4) {
					// Low orders: every chord of the check is above its resolution.
					assertEquals(0, route.result.getConditionedRoute().orElseThrow()
							.getJumpGuard().orElseThrow().getLeavesBelowResolution(),
							"order " + order + " at " + at);
				}
			}
		}
	}

	@Test
	void narrowLoopsAndHighCurvatureStayNumbers() {
		assertAdmittedAndBounded(PreG9BMaintB5aRoutes.route(u -> new LocusPoint2D(
				Math.cos(2 * Math.PI * u), 1e-2 * Math.sin(2 * Math.PI * u)), 0, 1),
				"ellipse 1:100");
		assertAdmittedAndBounded(PreG9BMaintB5aRoutes.route(u -> new LocusPoint2D(
				Math.cos(2 * Math.PI * u), 1e-3 * Math.sin(2 * Math.PI * u)), 0, 1),
				"ellipse 1:1000");
		assertAdmittedAndBounded(PreG9BMaintB5aRoutes.route(u -> new LocusPoint2D(
				u + 1e-3 * Math.cos(20 * u), 1e-3 * Math.sin(20 * u)), 0, 1),
				"small loops of radius 1e-3");
		assertAdmittedAndBounded(PreG9BMaintB5aRoutes.route(u -> {
			double angle = 2 * Math.PI * u;
			double radius = 0.5 + Math.cos(angle);
			return new LocusPoint2D(radius * Math.cos(angle), radius * Math.sin(angle));
		}, 0, 1), "limacon with an inner loop");
		assertAdmittedAndBounded(PreG9BMaintB5aRoutes.route(u -> new LocusPoint2D(u - 0.4,
				1e4 * (u - 0.4) * (u - 0.4)), 0, 1), "parabola of curvature 2e4 at its vertex");
	}

	/** A speed {@code |u - at|^-alpha} along the line, at an end or inside the route. */
	private static Curve divergent(double alpha, double at) {
		double exponent = 1 - alpha;
		return u -> {
			double d = u - at;
			return PreG9BMaintB5aRoutes.along(0, 0, LINE,
					Math.signum(d) * Math.pow(Math.abs(d), exponent));
		};
	}

	@Test
	void aDivergentSpeedBelowTheThresholdStaysANumber() {
		// alpha < 1 + log2(rho_J) = 0.848 at an end (a node of the minimum partition) and
		// alpha < 0.783 at any position: every alpha up to 0.78 stays a number, the fold
		// (alpha = 1/2) and the cube root (alpha = 2/3) included.
		for (double alpha : new double[] {0.3, 0.5, 0.6, 2.0 / 3, 0.7, 0.75, 0.78}) {
			for (double at : new double[] {0.0, 0.5, 0.3712}) {
				assertAdmittedAndBounded(PreG9BMaintB5aRoutes.route(divergent(alpha, at), 0, 1),
						"alpha " + alpha + " at " + at);
			}
		}
		// Between the two thresholds an end stays a number.
		for (double at : new double[] {0.0, 0.5}) {
			assertAdmittedAndBounded(PreG9BMaintB5aRoutes.route(divergent(0.8, at), 0, 1),
					"alpha 0.8 at the node " + at);
		}
	}

	@Test
	void aDivergentSpeedBeyondTheThresholdIsAConservativeRefusal() {
		List<String> outcomes = new ArrayList<>();
		for (double alpha : new double[] {0.85, 0.9}) {
			for (double at : new double[] {0.0, 0.5, 0.3712}) {
				Route route = PreG9BMaintB5aRoutes.route(divergent(alpha, at), 0, 1);
				outcomes.add(route.outcome());
				// Continuous, of finite length: the signature is observed, no jump is
				// demonstrated, and the estimate stays in the rich result.
				assertTrue(route.has(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE),
						"alpha " + alpha + " at " + at + ": " + route);
				assertTrue(Double.isFinite(route.value()));
			}
		}
		// Inside the route, away from a node, the signature appears from alpha = 0.783.
		Route interior = PreG9BMaintB5aRoutes.route(divergent(0.8, 0.3712), 0, 1);
		assertTrue(interior.has(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE),
				interior.toString());
		assertEquals(6, outcomes.size());
	}
}
