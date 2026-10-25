/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-CU0 characterization pins on synthetic constructions (owner CU3, reference
 * evidence, not normative): H02, transverse roots of a circular Locus V2 against a line and a
 * segment lose admissibility and finally disappear as the drawing is scaled, although the
 * geometry has two transverse roots at every scale; H06, the axes are not accepted as Locus V2
 * intersection targets while the equivalent line is. PRE-G9B-CU3 inverts H02 with the certified
 * route of {@code cu3-certified-admission/v1}.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu0SyntheticCharacterizationTest {

	@Test
	void lineTargetAdmissibilityNoLongerDependsOnTheScaleOfTheDrawing() throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Map<String, Object> sweep = PreG9BR6PlusF3Fixtures.onEdt(
				PreG9BCu0References::scaleSweep);
		// PRE-G9B-CU3 inversion of H02 (locus-v2-intersections.md CU3.4, the certified route of
		// cu3-certified-admission/v1; proof in PreG9BCu3CertifiedEvidenceTest): the geometric
		// truth of the baseline is unchanged and the admissible roots now reach it at every
		// scale. The baseline keeps the pinned counts as historical evidence.
		JSONObject pinned = baseline.getJSONObject("pins").getJSONObject("H02");
		assertEquals("two transverse roots for the line and for the segment at every scale",
				pinned.getString("geometric_truth"));
		assertEquals(0, pinned.getJSONObject("radius").getJSONObject("200.0")
				.getJSONObject("line").getInt("admissible_solutions"), "historical pin");
		Map<?, ?> radius = (Map<?, ?>) sweep.get("radius");
		for (String scale : new String[] {"2.0", "20.0", "200.0", "2000.0"}) {
			for (String target : new String[] {"line", "segment"}) {
				Map<?, ?> row = (Map<?, ?>) ((Map<?, ?>) radius.get(scale)).get(target);
				assertEquals(2, row.get("published_solutions"), target + " at " + scale);
				assertEquals(2, row.get("admissible_solutions"), PreG9BCu0Baseline
						.inversionMessage("H02", "CU3", target + " at radius " + scale
								+ ": both genuine transverse roots are admissible"));
			}
		}
	}

	/**
	 * H10/H11 pins (owner CU1): the evaluator-only metric reports the chord length 1 for
	 * {@code y = sin(2 pi k x)} on {@code [0, 1]} instead of its arc length, and the total
	 * length of a circular Locus V2 becomes undefined from radius 8 because the metric work
	 * budget is exhausted. The recorded reference lengths are the geometric truth.
	 */
	@Test
	void evaluatorOnlyMetricAliasesPeriodicCurvesAndExhaustsItsBudget() throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Map<String, Object> pin = PreG9BR6PlusF3Fixtures.onEdt(
				PreG9BCu0References::metricConformance);
		// PRE-G9B-CU1 inversion of H10/H11 (locus-v2-metrics.md 25): the geometric truth
		// recorded by the baseline is unchanged, and the reported lengths now reach it. The
		// baseline keeps the pinned chord lengths and missing values as historical evidence.
		JSONObject pinned = baseline.getJSONObject("pins").getJSONObject("H10_H11");
		Map<?, ?> sinus = (Map<?, ?>) pin.get("sinus_on_unit_segment");
		for (Object key : sinus.keySet()) {
			Map<?, ?> values = (Map<?, ?>) sinus.get(key);
			double reference = (Double) values.get("reference_length");
			assertEquals(pinned.getJSONObject("sinus_on_unit_segment")
					.getJSONObject(String.valueOf(key)).getDouble("reference_length"),
					reference, 1e-9, "reference " + key);
			assertTrue(reference > 4);
			assertEquals(reference, (Double) values.get("reported_length"), 1e-7,
					PreG9BCu0Baseline.inversionMessage("H10", "CU1",
							key + ": the reported length is the arc length, not the chord"));
		}
		Map<?, ?> circles = (Map<?, ?>) pin.get("circle_radius");
		for (String radius : new String[] {"1.0", "7.0", "8.0", "20.0"}) {
			Map<?, ?> values = (Map<?, ?>) circles.get(radius);
			double reference = (Double) values.get("reference_length");
			assertEquals(pinned.getJSONObject("circle_radius").getJSONObject(radius)
					.getDouble("reference_length"), reference, 1e-9, "reference " + radius);
			assertEquals(reference, (Double) values.get("reported_length"),
					1e-12 * reference, PreG9BCu0Baseline.inversionMessage("H11", "CU1",
							"radius " + radius + ": the structural length is defined"));
		}
	}

	@Test
	void axesAreNotAcceptedAsIntersectionTargets() throws Exception {
		Map<String, Object> pin = PreG9BR6PlusF3Fixtures.onEdt(PreG9BCu0References::axisTarget);
		assertFalse((Boolean) pin.get("Intersect(L,xAxis)_created"),
				PreG9BCu0Baseline.pinMessage("H06", "CU3", "Intersect(L, xAxis) fails"));
		assertFalse((Boolean) pin.get("Intersect(L,yAxis)_created"),
				PreG9BCu0Baseline.pinMessage("H06", "CU3", "Intersect(L, yAxis) fails"));
		assertTrue((Boolean) pin.get("Intersect(L,y=0)_created"),
				"the equivalent explicit line is accepted");
	}
}
