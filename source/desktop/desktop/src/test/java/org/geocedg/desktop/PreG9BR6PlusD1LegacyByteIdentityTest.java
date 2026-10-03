/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-R6-plus-D1 T-LEGACY-BYTES and T-CLASSIC (byte identity): every tracked
 * document without unit metadata loads to exactly the construction XML and full XML
 * that the unchanged base produced, in the GeoCeDG preflight and the Classic default
 * configurations, and no unit element is written for it (no migration on load). The
 * fixture was produced by {@link PreG9BR6PlusD1LegacyFingerprints} on the unchanged
 * tree and reproduced byte for byte on a git archive of the published base.
 */
@ExtendWith(PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class)
class PreG9BR6PlusD1LegacyByteIdentityTest {

	@Test
	void legacyCorpusIsByteIdenticalToTheBaseInBothConfigurations() throws Exception {
		JSONObject fixture;
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(
				PreG9BR6PlusD1LegacyFingerprints.FIXTURE)) {
			assertNotNull(input, PreG9BR6PlusD1LegacyFingerprints.FIXTURE);
			fixture = new JSONObject(new String(input.readAllBytes(), StandardCharsets.UTF_8));
		}
		Path root = PreG9BR6PlusD1LegacyFingerprints.findRepositoryRoot();
		for (String configuration : new String[] {"geocedg", "classic"}) {
			Map<String, PreG9BR6PlusD1LegacyFingerprints.Fingerprint> actual =
					PreG9BR6PlusD1LegacyFingerprints.compute(root, "classic".equals(configuration),
							1);
			JSONArray expected = fixture.getJSONArray(configuration);
			Map<String, JSONObject> byPath = new TreeMap<>();
			for (int i = 0; i < expected.length(); i++) {
				byPath.put(expected.getJSONObject(i).getString("path"),
						expected.getJSONObject(i));
			}
			assertEquals(byPath.keySet(), actual.keySet(), configuration);
			for (Map.Entry<String, PreG9BR6PlusD1LegacyFingerprints.Fingerprint> entry
					: actual.entrySet()) {
				JSONObject row = byPath.get(entry.getKey());
				String context = configuration + " " + entry.getKey();
				if (row.has("excluded")) {
					assertEquals(row.getString("excluded"), entry.getValue().exclusion, context);
					continue;
				}
				assertEquals(row.getString("constructionXmlSha256"),
						entry.getValue().constructionXmlSha256, context);
				assertEquals(row.getString("fullXmlSha256"), entry.getValue().fullXmlSha256,
						context);
				assertFalse(entry.getValue().containsUnitElement, context);
			}
		}
	}
}
