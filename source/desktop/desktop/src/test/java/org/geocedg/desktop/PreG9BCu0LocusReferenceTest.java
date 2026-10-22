/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU0 references of the author model without {@code e_2} (derived variant, see
 * {@link PreG9BCu0Corpus#withoutE2}): preserved geometric facts and the characterization pins
 * of B06, B07, H01, H03 and B08/B09. Preserved values must survive the corrections of the
 * track; pins name their owning package and are inverted or replaced by it. PRE-G9B-CU1
 * inverted H03 and PRE-G9B-CU2 inverted B06, B07 and H01; the committed baseline keeps their
 * pinned signatures as historical evidence. Since PRE-G9B-CU2 the branch ends are localized
 * on the evaluator of each locus, which they measure (matrix row V-TR-4).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu0LocusReferenceTest {

	@TempDir
	Path work;

	@Test
	void constructionStateAndLocusSamplesArePreserved() throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Map<?, ?> values = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			return Map.of("state", PreG9BCu0References.state(app),
					"loci", PreG9BCu0References.locusSamples(app),
					"H03", PreG9BCu0References.revisionChurn(app));
		});
		PreG9BCu0Baseline.assertMatches("preserve.state",
				PreG9BCu0Baseline.at(baseline, "preserve", "state"), values.get("state"),
				PreG9BCu0References.LENGTH_TOLERANCE);
		PreG9BCu0Baseline.assertMatches("preserve.loci",
				PreG9BCu0Baseline.at(baseline, "preserve", "loci"), values.get("loci"),
				PreG9BCu0References.POINT_TOLERANCE);
		// PRE-G9B-CU1 inversion of H03 (locus-v2-semantics.md v1.2, decision D-CU1-8): a pure
		// driver move is not a semantic change of o_1.
		Map<?, ?> churn = (Map<?, ?>) values.get("H03");
		assertEquals(0L, ((Number) churn.get("o_1_revision_delta_on_driver_move")).longValue(),
				PreG9BCu0Baseline.inversionMessage("H03", "CU1",
						"a pure driver move keeps the revision of o_1"));
		assertEquals(0L, ((Number) churn.get("o_1_metric_builds_delta_on_driver_move"))
				.longValue(), PreG9BCu0Baseline.inversionMessage("H03", "CU1",
						"a pure driver move keeps the metric states of o_1"));
	}

	/**
	 * Preserve: {@code d_2 = Length(LocusLength(o_1, N_1, K_1))} follows the current
	 * forward/strict/zero-length semantics, which reset the oriented length where {@code K_1}
	 * passes the origin {@code N_1}; it is continuous across the driver seam.
	 */
	@Test
	void orientedLengthKeepsItsCurrentSemantics() throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Map<?, ?> values = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			List<Object> profile = PreG9BCu0References.orientedLengthProfile(app);
			Double total = PreG9BCu0References.totalLength(app, "o_1");
			return Map.of("profile", profile, "total", total == null ? Double.NaN : total);
		});
		PreG9BCu0Baseline.assertMatches("preserve.oriented_length_profile",
				PreG9BCu0Baseline.at(baseline, "preserve", "oriented_length_profile"),
				values.get("profile"), PreG9BCu0References.LENGTH_TOLERANCE);
		double total = (Double) values.get("total");
		assertEquals(baseline.getJSONObject("preserve").getDouble("o_1_total_length"), total,
				PreG9BCu0References.LENGTH_TOLERANCE);
		assertEquals(baseline.getJSONObject("preserve").getDouble(
				"o_1_total_length_independent"), total, PreG9BCu0References.LENGTH_TOLERANCE,
				"the semantic length of o_1 agrees with an independent chord-sum reference");
		List<?> profile = (List<?>) values.get("profile");
		double before = (Double) ((List<?>) profile.get(2)).get(1);
		double after = (Double) ((List<?>) profile.get(3)).get(1);
		double seamLow = (Double) ((List<?>) profile.get(6)).get(1);
		double seamHigh = (Double) ((List<?>) profile.get(7)).get(1);
		assertEquals(total, before - after, 0.02, "the reset at the origin is one full length");
		assertEquals(seamLow, seamHigh, 0.02, "continuous across the driver seam");
	}

	@Test
	void existenceSweepPreservesBranchEndsAndPinsTheCompatibilityCertificate()
			throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Map<String, Object> sweep = PreG9BR6PlusF3Fixtures.onEdt(() -> PreG9BCu0References
				.existenceSweep(PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work))));
		JSONObject ends = baseline.getJSONObject("preserve").getJSONObject("branch_ends");
		Map<String, Object> computed = PreG9BCu0References.branchEnds(sweep);
		for (double value : PreG9BCu0References.SWEEP) {
			String key = PreG9BCu0References.key(value);
			for (String label : PreG9BCu0References.FIRST_FAMILY) {
				JSONArray expected = ends.getJSONObject(key).getJSONArray(label);
				List<?> actual = (List<?>) ((Map<?, ?>) computed.get(key)).get(label);
				assertEquals(expected.length(), actual.size(), key + " " + label);
				for (int i = 0; i < expected.length(); i++) {
					JSONArray row = expected.getJSONArray(i);
					List<?> end = (List<?>) actual.get(i);
					assertEquals(row.getDouble(0), (Double) end.get(0),
							PreG9BCu0References.BOUNDARY_TOLERANCE, key + " " + label);
					// Near a branch end the curve has a square-root speed singularity.
					assertEquals(row.getDouble(1), (Double) end.get(1), 1e-4);
					assertEquals(row.getDouble(2), (Double) end.get(2), 1e-4);
				}
			}
		}
		// PRE-G9B-CU2 inversion of B07 (existence-components section 15; CU2 tests
		// PreG9BCu2AuthorModelCertificationTest.everyBiteIsOneArcAcrossTheSeamWithTheAnalyticEnds
		// and PreG9BCu2AuthorModelRepresentationTest.aBiteIsDrawnToTheImagesOfItsCertifiedEnds):
		// in a bite the hybrid certificate proves one arc across the seam that excludes the
		// real gap, never the whole domain, and the drawing is one stroke that reaches both
		// branch ends.
		for (String key : List.of("3.7", "3.85", "3.9")) {
			for (String label : PreG9BCu0References.FIRST_FAMILY) {
				Map<?, ?> row = (Map<?, ?>) ((Map<?, ?>) sweep.get(key)).get(label);
				String where = "excCyl=" + key + " " + label;
				assertEquals("COMPLETE", row.get("completeness"), PreG9BCu0Baseline
						.inversionMessage("B07", "CU2", where + ": completeness is proved"));
				assertEquals(HybridExistenceCertifier2D.CONTRACT, row.get("certifier_contract"),
						PreG9BCu0Baseline.inversionMessage("B07", "CU2",
								where + ": the hybrid certificate is published"));
				List<?> components = (List<?>) row.get("certified_components");
				assertEquals(1, components.size(), PreG9BCu0Baseline.inversionMessage("B07",
						"CU2", where + ": one certified arc"));
				assertNotEquals(row.get("declared_domain"), components.get(0),
						PreG9BCu0Baseline.inversionMessage("B07", "CU2",
								where + ": the certified arc is not the whole domain"));
				assertEquals(Boolean.FALSE, row.get("full_period_closure"),
						PreG9BCu0Baseline.inversionMessage("B07", "CU2",
								where + ": the arc is not closed"));
				assertEquals(2, ((List<?>) row.get("sampled_valid_runs")).size(),
						where + ": sampled evidence shows a gap");
				assertEquals(1, ((Number) row.get("render_subpaths")).intValue(),
						PreG9BCu0Baseline.inversionMessage("B07", "CU2",
								where + ": the drawing is one stroke across the seam"));
				assertTrue((Double) row.get("max_render_gap_at_branch_end_px") <= 0.75,
						PreG9BCu0Baseline.inversionMessage("B07", "CU2",
								where + ": the drawing reaches the branch ends (V-B07-1)"));
			}
		}
		// PRE-G9B-CU2 inversion of H01 (publication rule of locus-v2-semantics.md v1.3; CU2
		// test PreG9BCu2AuthorModelCertificationTest
		// .theLociArePublishedWhileTheSavedDriverIsInTheGap): the loci are published although
		// the traced point is undefined at the saved driver.
		Map<?, ?> h01 = (Map<?, ?>) sweep.get("H01");
		assertFalse((Boolean) h01.get("traced_point_W_defined"));
		for (String label : PreG9BCu0References.FIRST_FAMILY) {
			assertTrue((Boolean) ((Map<?, ?>) h01.get("defined_at_saved_driver")).get(label),
					PreG9BCu0Baseline.inversionMessage("H01", "CU2", label
							+ " is published at the saved driver position"));
			assertTrue((Boolean) ((Map<?, ?>) h01.get("defined_with_driver_at_minus_one"))
					.get(label), label + " exists elsewhere on the driver domain");
		}
	}

	@Test
	void contactsAndTessellationArePreservedAndPinned() throws Exception {
		JSONObject baseline = PreG9BCu0Baseline.load();
		Map<?, ?> values = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			Map<String, Object> tessellation = PreG9BCu0References.tessellation(app);
			Map<String, Object> contacts = PreG9BCu0References.contacts(app);
			return Map.of("B06", tessellation, "contacts", contacts);
		});
		Map<String, Object> contacts = castMap(values.get("contacts"));
		JSONObject preserved = baseline.getJSONObject("preserve").getJSONObject("contacts");
		for (String pair : List.of("Intersect(o_1,h_2)", "Intersect(e_1,n)",
				"Intersect(e_1,c_1)")) {
			PreG9BCu0Baseline.assertMatches("preserve.contacts." + pair, preserved.get(pair),
					PreG9BCu0References.contactPoints(contacts).get(pair),
					PreG9BCu0References.POINT_TOLERANCE);
		}
		JSONObject residuals = preserved.getJSONObject("signed_residual_at_t0");
		Map<?, ?> computed = (Map<?, ?>) contacts.get("signed_residual_at_t0");
		for (String pair : residuals.keySet()) {
			Map<?, ?> row = (Map<?, ?>) computed.get(pair);
			assertTrue(Math.abs((Double) row.get("h")) <= 1e-12, pair + ": contact residual");
			assertEquals(residuals.getJSONObject(pair).getDouble("d2h"), (Double) row.get("d2h"),
					1e-6, pair + ": contact curvature");
		}
		PreG9BCu0Baseline.assertMatches("pins.B08_B09",
				baseline.getJSONObject("pins").get("B08_B09"),
				PreG9BCu0References.contactPins(contacts), 0);
		for (String pair : List.of("Intersect(o_1,h_2)", "Intersect(e_1,n)")) {
			Map<?, ?> tangent = (Map<?, ?>) ((List<?>) contacts.get(pair)).get(0);
			assertFalse((Boolean) tangent.get("admissible"), PreG9BCu0Baseline.pinMessage(
					"B08/B09", "CU3", pair + ": the tangent contact is not materializable"));
		}
		Map<?, ?> seamRoot = (Map<?, ?>) ((List<?>) contacts.get("Intersect(e_1,c_1)")).get(0);
		assertEquals("TRANSVERSE_ESTABLISHED", seamRoot.get("contact_class"));
		assertTrue((Boolean) seamRoot.get("admissible"), "the transverse seam root stays"
				+ " materializable");

		// PRE-G9B-CU2 inversion of B06 (drawing policy v2; CU2 test
		// PreG9BCu2AuthorModelRepresentationTest.turnsAreBoundedOutsideTheCertifiedFolds): no
		// drawing turns by more than theta_max between visible chords; e_1 turns more only at
		// its two certified folds, which that test checks.
		Map<?, ?> loci = (Map<?, ?>) castMap(values.get("B06")).get("loci");
		for (String label : List.of("o", "v", "w", "a_1", "o_1")) {
			Map<?, ?> row = (Map<?, ?>) loci.get(label);
			assertTrue((Double) row.get("render_max_turn_degrees")
					<= LocusRenderPolicy2D.DEFAULT_MAXIMUM_TURN_DEGREES,
					PreG9BCu0Baseline.inversionMessage("B06", "CU2", label
							+ ": the drawing turns by at most theta_max"));
		}
		for (Object entry : loci.values()) {
			Map<?, ?> row = (Map<?, ?>) entry;
			double coarse = (Double) row.get("midpoint_defect_h1e-3");
			double fine = (Double) row.get("midpoint_defect_h1e-4");
			assertNotNull(row.get("render_vertices"));
			assertTrue(coarse < 1e-5 && coarse / fine > 50 && coarse / fine < 200,
					"the semantic evaluation is smooth (midpoint defect scales with h^2)");
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object value) {
		return (Map<String, Object>) value;
	}
}
