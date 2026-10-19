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
 * intersection targets while the equivalent line is.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu0SyntheticCharacterizationTest {

	@Test
	void lineTargetAdmissibilityDependsOnTheScaleOfTheDrawing() throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Map<String, Object> sweep = PreG9BR6PlusF3Fixtures.onEdt(
				PreG9BCu0References::scaleSweep);
		PreG9BCu0Baseline.assertMatches("pins.H02", baseline.getJSONObject("pins").get("H02"),
				sweep, 0);
		Map<?, ?> radius = (Map<?, ?>) sweep.get("radius");
		Map<?, ?> small = (Map<?, ?>) ((Map<?, ?>) radius.get("2.0")).get("line");
		Map<?, ?> large = (Map<?, ?>) ((Map<?, ?>) radius.get("200.0")).get("line");
		assertEquals(2, small.get("admissible_solutions"));
		assertEquals(0, large.get("admissible_solutions"), PreG9BCu0Baseline.pinMessage("H02",
				"CU3", "two genuine transverse roots are not admissible at radius 300"));
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
		PreG9BCu0Baseline.assertMatches("pins.H10_H11",
				baseline.getJSONObject("pins").get("H10_H11"), pin, 1e-9);
		for (Object row : ((Map<?, ?>) pin.get("sinus_on_unit_segment")).values()) {
			Map<?, ?> values = (Map<?, ?>) row;
			assertTrue((Double) values.get("reference_length") > 4);
			assertEquals(1.0, (Double) values.get("reported_length"), 1e-9,
					PreG9BCu0Baseline.pinMessage("H10", "CU1",
							"the reported length is the chord, not the arc length"));
		}
		Map<?, ?> circles = (Map<?, ?>) pin.get("circle_radius");
		for (String radius : new String[] {"1.0", "7.0"}) {
			Map<?, ?> values = (Map<?, ?>) circles.get(radius);
			assertEquals((Double) values.get("reference_length"),
					(Double) values.get("reported_length"), 1e-6, "radius " + radius);
		}
		for (String radius : new String[] {"8.0", "20.0"}) {
			assertEquals(null, ((Map<?, ?>) circles.get(radius)).get("reported_length"),
					PreG9BCu0Baseline.pinMessage("H11", "CU1", "radius " + radius
							+ ": the metric work budget is exhausted"));
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
