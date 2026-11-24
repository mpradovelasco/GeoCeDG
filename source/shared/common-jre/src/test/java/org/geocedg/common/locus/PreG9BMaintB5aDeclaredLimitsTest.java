/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Curve;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Route;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-B5a: the declared limits of {@code locus-v2-metrics.md} section 28.3.1, one
 * test each. Each is a real discontinuity, a violation of H3 (or, for the last one, of H2),
 * that finite sampling cannot observe: the route stays a number, an estimate under a
 * hypothesis that is false, exactly as under {@code conditioned-length-scalar/v1}. Each test
 * documents one instance of its limit; none claims that the instances shown are the only
 * ones.
 */
final class PreG9BMaintB5aDeclaredLimitsTest {
	private static final double LINE = Math.atan2(2, 1);
	private static final double SPEED = Math.sqrt(5);

	private static Curve line(double offset, double at) {
		return u -> PreG9BMaintB5aRoutes.along(0, 0, LINE, SPEED * u + (u >= at ? offset : 0));
	}

	private static void assertUndetected(Route route, String where) {
		assertTrue(route.admitted(), where + ": " + route);
		assertFalse(route.anyCheckDiagnostic(), where + ": " + route);
		assertTrue(route.result.getConditionedRoute().orElseThrow().getJumpGuard().isPresent(),
				where);
	}

	/** Limit 1: one different value at a parameter that is never evaluated. */
	@Test
	void aRemovableDiscontinuityIsNotDetected() {
		Curve straight = line(0, 2);
		Route route = PreG9BMaintB5aRoutes.route(u -> u == 0.3 ? new LocusPoint2D(5, 5)
				: straight.at(u), 0, 1);
		assertUndetected(route, "removable discontinuity at 0.3");
		assertEquals(SPEED, route.value(), 1e-12);
	}

	/** Limit 2: an excursion that leaves and comes back between two evaluated parameters. */
	@Test
	void aJumpAndItsReturnBetweenEvaluatedParametersAreNotDetected() {
		for (double width : new double[] {1e-4, 1e-6, 1e-9}) {
			Route route = PreG9BMaintB5aRoutes.route(u -> PreG9BMaintB5aRoutes.along(0, 0, LINE,
					SPEED * u + (u >= 0.3001 && u < 0.3001 + width ? 1 : 0)), 0, 1);
			assertUndetected(route, "excursion of width " + width);
			assertEquals(SPEED, route.value(), 1e-12, "the excursion is not in the estimate");
		}
	}

	/**
	 * Limit 3: a jump below the resolution of the check, too small for its chord to dominate
	 * the chord of the continuous part at the levels examined. The estimate includes it.
	 */
	@Test
	void aJumpBelowTheResolutionOfTheCheckIsNotDetected() {
		for (double size : new double[] {1e-12, 1e-10, 1e-8, 1e-6, 1e-4, 1e-3, 3e-3}) {
			for (double at : new double[] {0.75, 1.0 / 3, 0.6180339887498949}) {
				Route route = PreG9BMaintB5aRoutes.route(line(size, at), 0, 1);
				assertUndetected(route, "jump " + size + " at " + at);
				assertEquals(SPEED + size, route.value(), 1e-12, "the jump is in the estimate");
			}
		}
	}

	/**
	 * Limit 4: a jump in a quarter that the check does not follow, because another quarter of
	 * the same leaf carries a greater chord. Leaf 21 is {@code [20/64, 21/64]}: its last
	 * quarter runs ten times faster than the others, and the jump of {@code 0.01} lies in its
	 * first quarter, whose chord stays the smaller one.
	 */
	@Test
	void aJumpInAQuarterThatTheCheckDoesNotFollowIsNotDetected() {
		double start = 20.0 / 64 + 3.0 / 256;
		Route route = PreG9BMaintB5aRoutes.route(u -> PreG9BMaintB5aRoutes.along(0, 0, LINE,
				u + 9 * Math.max(0, Math.min(u - start, 1.0 / 256))
						+ (u >= 20.0 / 64 + 0.5 / 256 ? 0.01 : 0)), 0, 1);
		assertUndetected(route, "jump in a quarter that is not followed");
		assertEquals(1 + 9.0 / 256 + 0.01, route.value(), 1e-12);
		// The same jump alone in its leaf is observed.
		Route alone = PreG9BMaintB5aRoutes.route(u -> PreG9BMaintB5aRoutes.along(0, 0, LINE,
				u + (u >= 20.0 / 64 + 0.5 / 256 ? 0.01 : 0)), 0, 1);
		assertFalse(alone.admitted(), alone.toString());
	}

	/**
	 * Limit 5: a jump in a cell narrower than the refinement stencil whose chord does not
	 * exceed the chord resolution {@code eps_res}: every leaf of this route of a few units in
	 * the last place ends below resolution, without an evaluation.
	 */
	@Test
	void aJumpInACellNarrowerThanTheStencilAndBelowTheResolutionIsNotDetected() {
		double at = 0.5 + 0x1p-50;
		for (double size : new double[] {1e-15, 1e-14, 1e-13, 1e-12}) {
			Route route = PreG9BMaintB5aRoutes.route(line(size, at), at - 4 * Math.ulp(at),
					at + 4 * Math.ulp(at));
			assertUndetected(route, "jump " + size + " in a cell of a few ulps");
			assertEquals(route.leavesTested(), route.result.getConditionedRoute().orElseThrow()
					.getJumpGuard().orElseThrow().getLeavesBelowResolution());
			assertEquals(0, route.checkEvaluations());
		}
	}

	/**
	 * Limit 6: a change of selection that keeps the traced position continuous is a violation
	 * of H2, not a jump of the traced function. Here the producer testifies another selection
	 * on a window that no node of the traversal reaches, and the position is the same line: the
	 * check, which reads only the traced point, observes nothing.
	 */
	@Test
	void aChangeOfSelectionThatKeepsThePositionContinuousIsNotDetected() {
		Curve straight = line(0, 2);
		Route route = PreG9BMaintB5aRoutes.route(straight, straight,
				u -> u > 0.3000001 && u < 0.3000001 + 1e-8 ? "b" : "a", 0, 1, 0, 1);
		assertUndetected(route, "selection window");
		assertTrue(route.evidence.inOneRegion(0, 1));
		assertEquals(SPEED, route.value(), 1e-12);
	}
}
