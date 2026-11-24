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
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Curve;
import org.geocedg.common.locus.PreG9BMaintB5aRoutes.Route;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-B5a adversarial battery ({@code locus-v2-metrics.md} section 28.3.1, criterion
 * of the author of 2026-11-23): jumps nearly aligned with the chord, at angles from {@code 0}
 * to {@code 0.35 rad}, of sizes from the resolution of the check upwards by orders of
 * magnitude, at dyadic and non-dyadic positions and next to an end of the route; several
 * jumps; a jump and its return in one cell; fast oscillation next to a jump; concentrated
 * continuous variation. No route of the battery that crosses a jump above the resolution of
 * the check is a number. The traversal sees none of these jumps when they are aligned: the
 * route lies in one region. What the check cannot refuse is fixed in
 * {@code PreG9BMaintB5aDeclaredLimitsTest}.
 *
 * <p>The source is the straight line of direction {@code (1, 2)} at speed {@code sqrt 5} on
 * {@code [0, 1]}; the route is the whole domain, whose estimate is the minimum partition of
 * 64 leaves (quarter width {@code h0 = 1/256}). The analysis of the policy puts the smallest
 * aligned jump that the check observes at about {@code 0.5 * s * h0 = 0.0044}.</p>
 */
final class PreG9BMaintB5aAdversarialBatteryTest {
	private static final double LINE = Math.atan2(2, 1);
	private static final double SPEED = Math.sqrt(5);
	private static final double[] ANGLES = {0, 1e-9, 1e-6, 1e-3, 0.05, 0.1, 0.2, 0.3, 0.35};
	private static final double[] SIZES = {5e-3, 1e-2, 1e-1, 1};
	/** Dyadic, non-dyadic, next to the end and next to the start of the route. */
	private static final double[] POSITIONS = {0.75, 1.0 / 3, 0.6180339887498949,
			0.1234567, 0.9990234, 0.0002};

	/** The line with one jump of the given size and direction (angle to the chord). */
	static Curve jump(double angle, double size, double at) {
		return u -> {
			LocusPoint2D base = PreG9BMaintB5aRoutes.along(0, 0, LINE, SPEED * u);
			return u >= at ? new LocusPoint2D(base.getX() + size * Math.cos(LINE + angle),
					base.getY() + size * Math.sin(LINE + angle)) : base;
		};
	}

	@Test
	void noRouteAcrossAJumpAboveTheResolutionOfTheCheckIsANumber() {
		List<String> admitted = new ArrayList<>();
		int byTheCheck = 0;
		for (double angle : ANGLES) {
			for (double size : SIZES) {
				for (double at : POSITIONS) {
					Route route = PreG9BMaintB5aRoutes.route(jump(angle, size, at), 0, 1);
					String where = "angle " + angle + " size " + size + " at " + at + ": "
							+ route;
					if (route.admitted()) {
						admitted.add(where);
					}
					if (angle == 0) {
						// Exactly aligned: every chord level of the estimator agrees and the
						// traversal sees nothing; only the check refuses the route.
						assertTrue(route.has(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE),
								where);
						assertTrue(route.evidence.inOneRegion(0, 1), where);
						assertEquals(SPEED + size, route.value(), 1e-9, where);
					}
					if (route.has(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE)) {
						byTheCheck++;
					}
				}
			}
		}
		assertEquals(List.of(), admitted);
		assertTrue(byTheCheck >= SIZES.length * POSITIONS.length, "refusals by the check: "
				+ byTheCheck);
	}

	@Test
	void aRouteWithSeveralJumpsStopsAtTheFirstSignature() {
		Route route = PreG9BMaintB5aRoutes.route(u -> PreG9BMaintB5aRoutes.along(0, 0, LINE,
				SPEED * u + (u >= 0.3 ? 1e-2 : 0) + (u >= 0.8 ? 1e-2 : 0)), 0, 1);
		assertFalse(route.admitted(), route.toString());
		String signature = route.message(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE);
		// Leaf 20 is [0.296875, 0.3125], the leaf of the first jump.
		assertTrue(signature.contains("leaf 20 [0.296875, 0.3125]"), signature);
		assertEquals(SPEED + 2e-2, route.value(), 1e-9);
	}

	@Test
	void aJumpAndItsReturnThatReachAnEvaluatedNodeAreNoNumber() {
		// The excursion covers a node of the estimator: its chords see it.
		Route route = PreG9BMaintB5aRoutes.route(u -> PreG9BMaintB5aRoutes.along(0, 0, LINE,
				SPEED * u + (u >= 0.3001 && u < 0.3011 ? 1 : 0)), 0, 1);
		assertTrue(route.has(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE),
				route.toString());
		assertFalse(route.admitted());
	}

	@Test
	void fastOscillationNextToAJumpIsNoNumber() {
		for (double amplitude : new double[] {1e-4, 1e-3}) {
			Route route = PreG9BMaintB5aRoutes.route(u -> {
				LocusPoint2D point = PreG9BMaintB5aRoutes.along(0, 0, LINE,
						SPEED * u + (u >= 0.6 ? 0.05 : 0));
				double wave = Math.abs(u - 0.6) < 0.02 ? amplitude * Math.sin(2000 * u) : 0;
				return new LocusPoint2D(point.getX() - 2 * wave / SPEED,
						point.getY() + wave / SPEED);
			}, 0, 1);
			// The estimator does not converge next to the oscillation: no number, with the
			// cause of the estimate, before the check is reached.
			assertFalse(route.admitted(), route.toString());
			assertTrue(route.result.getMetricValue().getFiniteValue().isEmpty(),
					route.toString());
		}
	}

	/**
	 * Concentrated continuous variation (a conservative refusal of section 28.3.1): a ramp
	 * along the chord of height {@code H} over a parameter width {@code w} is continuous; when
	 * {@code w} is much narrower than the levels of the check it gives the signature that a
	 * jump of size {@code H} gives, and when {@code H} is below the resolution of the check it
	 * gives none.
	 */
	@Test
	void aSteepContinuousRampIsRefusedAsAJumpWouldBe() {
		double[] widths = {1e-2, 3e-3, 1e-3, 3e-4, 1e-4, 1e-5, 1e-6, 1e-8};
		double[] heights = {1e-3, 1e-2, 1e-1};
		StringBuilder observed = new StringBuilder();
		for (double height : heights) {
			for (double width : widths) {
				Route route = PreG9BMaintB5aRoutes.route(u -> PreG9BMaintB5aRoutes.along(0, 0,
						LINE, SPEED * u + height * Math.max(0, Math.min(1,
								(u - 0.4123) / width))), 0, 1);
				assertEquals(SPEED + height, route.value(), 1e-9, route.toString());
				observed.append(route.admitted() ? 'A' : route.has(MetricDiagnosticCode2D
						.CHORD_SCALING_JUMP_SIGNATURE) ? 'S' : '?');
			}
			observed.append('|');
		}
		// Rows: height 1e-3, 1e-2, 1e-1; columns: the widths above.
		assertEquals("AAAAAAAA|AAAAASSS|AAASSSSS|", observed.toString());
	}
}
