/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU0 characterization of the complete author model with
 * {@code e_2 = LocusV2(R_1, E_1)}, whose points depend on the semantic length
 * {@code w_1 = LocusLength(o_1, N_1, K_1)} of another Locus V2 driven by the same driver.
 *
 * <p>Preserved: the points of {@code e_2} and its continuity across the driver seam. Pinned
 * (reference evidence, not normative): B10.1, the per-point rebuild of the nested copy of
 * {@code o_1} and of its metric index (owner CU1); B10.2, the compatibility certificate that
 * declares {@code e_2} one closed periodic component and the render segment across the
 * oriented-length reset (owner CU2).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu0CompositionCharacterizationTest {

	@TempDir
	Path work;

	@Test
	void developmentCurvePointsArePreservedAndTheNestedRebuildIsPinned() throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Map<?, ?> values = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			return Map.of("e_2", PreG9BCu0References.e2Samples(app),
					"B10.1", PreG9BCu0References.nestedComposition(app));
		});
		PreG9BCu0Baseline.assertMatches("preserve.e_2",
				PreG9BCu0Baseline.at(baseline, "preserve", "e_2"), values.get("e_2"),
				PreG9BCu0References.LENGTH_TOLERANCE);
		Map<?, ?> pin = (Map<?, ?>) values.get("B10.1");
		for (Object step : (List<?>) pin.get("single_point_evaluations")) {
			Map<?, ?> row = (Map<?, ?>) step;
			assertTrue(((Number) row.get("nested_revision_delta")).longValue() >= 1,
					PreG9BCu0Baseline.pinMessage("B10.1", "CU1",
							"one point query of e_2 republishes the nested copy of o_1"));
			assertTrue(((Number) row.get("nested_metric_builds_delta")).longValue() >= 1,
					PreG9BCu0Baseline.pinMessage("B10.1", "CU1",
							"one point query of e_2 rebuilds the nested metric index"));
		}
		assertEquals(0L, ((Number) pin.get("outer_dependency_slice_builds")).longValue(),
				PreG9BCu0Baseline.pinMessage("B10.1", "CU1",
						"the outer slice counters do not see the nested work"));
	}

	@Test
	void orientedLengthResetIsDrawnAsAClosedPeriodicCurve() throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Map<String, Object> wrap = PreG9BR6PlusF3Fixtures.onEdt(() -> PreG9BCu0References
				.lengthWrap(PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
						PreG9BCu0Corpus.CEDG, work))));
		double total = baseline.getJSONObject("preserve").getDouble("o_1_total_length");
		assertTrue((Double) wrap.get("seam_distance") < 1e-6,
				"e_2 is continuous across the driver seam");
		assertEquals(total, (Double) wrap.get("jump_distance"), 1e-4,
				"e_2 jumps by the total length of o_1 where K_1 passes N_1");
		Map<?, ?> existence = (Map<?, ?>) wrap.get("existence");
		assertEquals("COMPLETE", existence.get("completeness"), PreG9BCu0Baseline.pinMessage(
				"B10.2", "CU2", "the dependent producer certifies completeness without proof"));
		assertEquals(Boolean.TRUE, existence.get("full_period_closure"),
				PreG9BCu0Baseline.pinMessage("B10.2", "CU2",
						"the renderer closes e_2 as one periodic component"));
		assertTrue((Double) wrap.get("render_segment_across_jump_length") > 0.5 * total,
				PreG9BCu0Baseline.pinMessage("B10.2", "CU2",
						"a render segment bridges the oriented-length reset"));
	}
}
