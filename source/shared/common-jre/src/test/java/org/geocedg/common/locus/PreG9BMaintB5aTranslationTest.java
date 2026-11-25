/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleFunction;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.metric.LocusConditionedRouteJumpGuard2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Curve;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Route;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-B5a, numeric representation and translations ({@code locus-v2-metrics.md}
 * section 28.3.1): the same curves, translated by {@code (T, T)} with {@code T} growing by
 * orders of magnitude up to and beyond the magnitude at which {@code 2^8 * u * M_leaf}
 * exceeds the tolerance share of a leaf, compared with the untranslated curve: admissibility,
 * evaluations of the check and diagnostics. The comparison shows the range of magnitudes in
 * which the outcome was observed unchanged and pins the first divergences; it is no universal
 * guarantee of invariance.
 */
final class PreG9BMaintB5aTranslationTest {
	private static final double LINE = Math.atan2(2, 1);
	private static final double SPEED = Math.sqrt(5);

	private static Curve p1(double t) {
		return u -> PreG9BMaintB5aRoutes.along(t, t, LINE, SPEED * u + (u >= 0.75 ? SPEED : 0));
	}

	private static Map<String, DoubleFunction<Route>> curves() {
		Map<String, DoubleFunction<Route>> curves = new LinkedHashMap<>();
		curves.put("P-1 line across the jump", t -> PreG9BMaintB5aRoutes.route(p1(t), 0.7, 0.8));
		curves.put("P-1 line before the jump", t -> PreG9BMaintB5aRoutes.route(p1(t), 0.7,
				0.74));
		curves.put("u^3", t -> PreG9BMaintB5aRoutes.route(u -> new LocusPoint2D(t + u * u * u,
				t + u), 0, 1));
		curves.put("u^8", t -> PreG9BMaintB5aRoutes.route(u -> {
			double square = u * u;
			double fourth = square * square;
			return new LocusPoint2D(t + fourth * fourth, t + square);
		}, 0, 1));
		curves.put("exp", t -> PreG9BMaintB5aRoutes.route(u -> new LocusPoint2D(t + u,
				t + Math.exp(u)), 0.2, 0.7));
		curves.put("stationary point of order 2", t -> PreG9BMaintB5aRoutes.route(u -> {
			double d = u - 0.37;
			double s = 0.37 + d * d * d;
			return new LocusPoint2D(t + s, t + Math.exp(s));
		}, 0, 1));
		return curves;
	}

	private static String signature(Route route) {
		StringBuilder codes = new StringBuilder(route.outcome());
		route.result.getDiagnostics().forEach(d -> codes.append(',').append(d.getCode()));
		return codes + " check=" + route.checkEvaluations() + " leaves=" + route.leavesTested();
	}

	/**
	 * Up to {@code |T| = 1e5}, beyond the magnitude (about {@code 1.4e3} for these routes) at
	 * which the rounding term of {@code eps_res} exceeds the tolerance share of a leaf, every
	 * curve has the admissibility, the evaluations of the check, the leaves and the diagnostics
	 * of the untranslated curve.
	 */
	@Test
	void theOutcomeIsUnchangedUpToTenToTheFifth() {
		for (Map.Entry<String, DoubleFunction<Route>> curve : curves().entrySet()) {
			String reference = signature(curve.getValue().apply(0));
			for (int exponent = 1; exponent <= 5; exponent++) {
				double t = Math.pow(10, exponent);
				assertEquals(reference, signature(curve.getValue().apply(t)),
						curve.getKey() + " translated by 1e" + exponent);
			}
		}
	}

	@Test
	void theRoundingTermOfTheResolutionExceedsTheShareFromAboutOnePointFourThousand() {
		Route route = PreG9BMaintB5aRoutes.route(p1(0), 0.7, 0.8);
		double tolerance = route.result.getErrorEvidence().getRequestedTolerance()
				.orElseThrow();
		// Minimum partition of 64 leaves: the share of a leaf is a 64th of the tolerance.
		double share = tolerance / 64;
		assertEquals(share, LocusConditionedRouteJumpGuard2D.resolution(share, 1e3), 0);
		assertTrue(LocusConditionedRouteJumpGuard2D.resolution(share, 1e4) > share);
		assertEquals(Math.scalb(1e4, 8 - 53), LocusConditionedRouteJumpGuard2D.resolution(
				share, 1e4), 0);
	}

	/** The first divergences, at the magnitudes at which they appear. */
	@Test
	void theFirstDivergencesAppearWhereTheyWereMeasured() {
		// The jump of P-1 stays observed wherever the estimator measures the route.
		for (int exponent = 0; exponent <= 9; exponent++) {
			Route route = PreG9BMaintB5aRoutes.route(p1(exponent == 0 ? 0
					: Math.pow(10, exponent)), 0.7, 0.8);
			assertTrue(route.has(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE),
					"1e" + exponent + ": " + route);
		}
		// From 1e7 the rounding term of eps_res makes leaves near a stationary point end below
		// resolution: the route stays a number, with fewer chords examined.
		Route stationary = curves().get("stationary point of order 2").apply(1e7);
		assertTrue(stationary.admitted(), stationary.toString());
		assertTrue(stationary.result.getConditionedRoute().orElseThrow().getJumpGuard()
				.orElseThrow().getLeavesBelowResolution() > 0, stationary.toString());
		assertEquals(0, curves().get("stationary point of order 2").apply(1e6).result
				.getConditionedRoute().orElseThrow().getJumpGuard().orElseThrow()
				.getLeavesBelowResolution());
		// From 1e14 every chord of the line is at the rounding of its coordinates: every leaf
		// ends below resolution and the jump of P-1 is no longer observed (declared limits 3
		// and 5 widen with the coordinates). The estimate itself is then no longer that of the
		// untranslated route, which is a fact of the estimator, not of the check.
		Route far = PreG9BMaintB5aRoutes.route(p1(1e14), 0.7, 0.8);
		assertTrue(far.admitted(), far.toString());
		assertEquals(far.leavesTested(), far.result.getConditionedRoute().orElseThrow()
				.getJumpGuard().orElseThrow().getLeavesBelowResolution());
		assertEquals(0, far.checkEvaluations());
		assertFalse(far.anyCheckDiagnostic());
	}
}
