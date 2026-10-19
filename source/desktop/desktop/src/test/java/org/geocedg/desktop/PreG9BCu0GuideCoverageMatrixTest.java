/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * Integrity of the PRE-G9B-CU0 initial user-guide coverage matrix
 * ({@code geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json}). The matrix is a
 * baseline, not a completeness gate: it may contain {@code PARTIAL} and {@code MISSING}
 * entries until CU9. It must however stay faithful to the product and to both editions:
 * every action of the live profile appears exactly once, every cited anchor exists in its
 * edition, and the totals match the entries.
 */
class PreG9BCu0GuideCoverageMatrixTest {
	private static final String MATRIX =
			"geocedg/validation/pre-g9b-cu/guide-coverage-matrix.json";
	private static final Set<String> COVERAGE = Set.of("COVERED", "PARTIAL", "MISSING",
			"EXCLUDED_WITH_REASON");

	@Test
	void everyProfileActionHasExactlyOneEntry() throws Exception {
		JSONArray entries = matrix().getJSONArray("entries");
		Set<String> ids = new HashSet<>();
		Set<String> actions = new TreeSet<>();
		for (int i = 0; i < entries.length(); i++) {
			JSONObject entry = entries.getJSONObject(i);
			assertTrue(ids.add(entry.getString("id")), "duplicate " + entry.getString("id"));
			if ("action".equals(entry.getString("kind"))) {
				assertTrue(entry.getString("id").startsWith("action:"), entry.getString("id"));
				actions.add(entry.getString("id").substring("action:".length()));
			}
		}
		Set<String> profile = new TreeSet<>();
		for (GeoCeDGProfile.ActionDefinition definition : GeoCeDGProfile.getActions()) {
			profile.add(definition.id());
		}
		assertEquals(profile, actions);
	}

	@Test
	void everyCitedAnchorExistsInItsEdition() throws Exception {
		Map<String, Set<String>> anchors = new HashMap<>();
		for (String language : List.of("en", "es")) {
			Set<String> edition = new HashSet<>();
			for (GeoCeDGGuideOutline.Entry entry : GeoCeDGGuideRenderer.render(guide(language))
					.outline()) {
				edition.add(entry.anchor());
			}
			anchors.put(language, edition);
		}
		JSONArray entries = matrix().getJSONArray("entries");
		for (int i = 0; i < entries.length(); i++) {
			JSONObject entry = entries.getJSONObject(i);
			JSONObject cited = entry.getJSONObject("anchors");
			for (String language : List.of("en", "es")) {
				JSONArray list = cited.getJSONArray(language);
				for (int j = 0; j < list.length(); j++) {
					assertTrue(anchors.get(language).contains(list.getString(j)),
							entry.getString("id") + " cites missing " + language + " anchor "
									+ list.getString(j));
				}
			}
		}
	}

	@Test
	void coverageValuesNotesAndTotalsAreConsistent() throws Exception {
		JSONObject matrix = matrix();
		JSONArray entries = matrix.getJSONArray("entries");
		Map<String, Integer> counts = new HashMap<>();
		for (int i = 0; i < entries.length(); i++) {
			JSONObject entry = entries.getJSONObject(i);
			String coverage = entry.getString("coverage");
			assertTrue(COVERAGE.contains(coverage), entry.getString("id") + ": " + coverage);
			counts.merge(coverage, 1, Integer::sum);
			JSONObject cited = entry.getJSONObject("anchors");
			if ("COVERED".equals(coverage)) {
				assertTrue(cited.getJSONArray("en").length() > 0
						&& cited.getJSONArray("es").length() > 0, entry.getString("id"));
			} else {
				assertFalse(entry.getString("notes").isBlank(), entry.getString("id")
						+ " needs a note");
			}
		}
		JSONObject totals = matrix.getJSONObject("totals");
		for (String coverage : COVERAGE) {
			assertEquals(counts.getOrDefault(coverage, 0), totals.getInt(coverage), coverage);
		}
	}

	private static JSONObject matrix() throws IOException, JSONException {
		return new JSONObject(Files.readString(PreG9BCu0Corpus.repositoryRoot().resolve(MATRIX),
				StandardCharsets.UTF_8));
	}

	private static String guide(String language) throws IOException {
		return Files.readString(PreG9BCu0Corpus.repositoryRoot().resolve(
				"docs/user/geocedg_user_guide_" + language + ".md"), StandardCharsets.UTF_8);
	}
}
