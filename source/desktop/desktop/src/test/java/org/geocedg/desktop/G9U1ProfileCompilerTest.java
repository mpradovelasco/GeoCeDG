/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;

import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1ProfileCompilerTest {

	@BeforeAll
	static void initializeAwt() {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
	}

	@Test
	void liveProfileStrictlyCompilesOneHundredTenActions() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		assertEquals(2, profile.getInt("schema_version"));
		assertEquals(11, profile.getJSONObject("taxonomy").getJSONArray("broad_families").length());
		assertEquals(18, profile.getJSONArray("clusters").length());
		assertEquals(110, GeoCeDGProfile.getActions().size());
		assertEquals(GeoCeDGProfile.getToolbarDefinition(),
				GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void toolbarContainsThirtyTwoCuratedModesWhileCatalogRetainsAllSixtySix() {
		String[] modes = GeoCeDGProfile.getToolbarDefinition().split("[ |]+");
		assertEquals(32, modes.length);
		assertEquals(32, new HashSet<>(Arrays.asList(modes)).size());
		assertEquals(66, GeoCeDGProfile.getActions().stream()
				.filter(action -> action.mode() != null).count());
		assertFalse(Arrays.asList(modes).contains("47"));
		assertFalse(Arrays.asList(modes).contains("54"));
		assertFalse(Arrays.asList(modes).contains(
				Integer.toString(EuclidianConstants.MODE_DELETE)));
		assertFalse(Arrays.asList(modes).contains(
				Integer.toString(EuclidianConstants.MODE_SHOW_HIDE_OBJECT)));
	}

	@Test
	void historicalV1FixtureRetainsExactOriginalToolbar() throws Exception {
		try (InputStream stream = getClass().getResourceAsStream("application-profile-v1.yml")) {
			String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			assertEquals("0 | 1 2 15 18 7 16 3 4 | 10 11 12 | 5 47 | 30 31 32 | 36 38",
					GeoCeDGProfile.compileProfile(json));
		}
	}

	@Test
	void unknownTopLevelPropertyFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog().put("invented", true);
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void unknownNestedPropertyFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		profile.getJSONObject("application").put("alternate_authority", true);
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void wrongProfileIdentityFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog().put("profile_id", "other");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void wrongModeConstantFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		profile.getJSONArray("actions").getJSONObject(0).getJSONObject("target")
				.put("audited_numeric_id", 999);
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void unknownModeSymbolFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		profile.getJSONArray("actions").getJSONObject(0).getJSONObject("target")
				.put("symbol", "MODE_UNKNOWN");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void duplicateActionFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		profile.getJSONArray("actions").getJSONObject(1).put("id", "construction.move");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void missingSpanishTextFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		profile.getJSONObject("localized_text").getJSONObject("Menu.File").remove("es");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void unresolvedActionPlacementFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		profile.getJSONArray("clusters").getJSONObject(0)
				.getJSONArray("toolbar_action_ids").put("not.an.action");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void unresolvedPolicyReferenceFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		profile.getJSONArray("actions").getJSONObject(0).put("effect_profile_id", "not.a.policy");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void duplicateMenuClusterFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		profile.getJSONArray("menu_sections").getJSONObject(0)
				.getJSONArray("cluster_ids").put("document-lifecycle");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void continuityAndLanguagePolicyCannotBeWeakened() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		profile.getJSONObject("product_policies").getJSONObject("continuity").put("value", true);
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void unknownSchemaVersionFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog().put("schema_version", 3);
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void allOwnedTextHasEnglishSpanishAndDeterministicFallback() throws Exception {
		JSONObject texts = GeoCeDGProfile.getCatalog().getJSONObject("localized_text");
		for (String key : texts.keySet()) {
			assertFalse(GeoCeDGProfile.getText(key, "en").isBlank(), key);
			assertFalse(GeoCeDGProfile.getText(key, "es").isBlank(), key);
			assertEquals(GeoCeDGProfile.getText(key, "en"), GeoCeDGProfile.getText(key, "fr"));
		}
		assertNotEquals(GeoCeDGProfile.getText("Menu.File", "en"),
				GeoCeDGProfile.getText("Menu.File", "es"));
	}

	@Test
	void schemaCannotSilentlyIgnoreUnsupportedKeyword() throws Exception {
		JSONObject schema = new JSONObject("{\"type\":\"object\",\"inventedConstraint\":true}");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfileSchema.validate(new JSONObject(), schema));
	}

	@Test
	void catalogCopiesCannotMutateLiveAuthority() throws Exception {
		JSONObject copy = GeoCeDGProfile.getCatalog();
		copy.put("profile_id", "changed");
		assertEquals("geocedg-desktop", GeoCeDGProfile.getProfileId());
		assertTrue(GeoCeDGProfile.getAction("relation.intersect").mode() != null);
	}

	@Test
	void invalidV2FallsBackToValidatedV1WithoutSynthesizingActions() throws Exception {
		try (InputStream stream = getClass().getResourceAsStream("application-profile-v1.yml")) {
			String fallback = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			var selected = GeoCeDGProfile.loadDefinition("{\"schema_version\":2}", fallback);
			assertTrue(selected.legacyFallback);
			assertEquals(0, selected.actionCount());
			assertEquals(GeoCeDGProfile.compileProfile(fallback), selected.toolbar());
			assertFalse(GeoCeDGProfile.getFallbackDiagnostic("en").isBlank());
			assertFalse(GeoCeDGProfile.getFallbackDiagnostic("es").isBlank());
		}
	}

	@Test
	void validV2DoesNotUseFallback() {
		var selected = GeoCeDGProfile.loadDefinition(GeoCeDGProfile.getCatalog().toString(), "");
		assertFalse(selected.legacyFallback);
		assertEquals(110, selected.actionCount());
	}

	@Test
	void invalidFallbackCannotBecomeAnUnvalidatedWorkspace() {
		assertThrows(IllegalStateException.class, () -> GeoCeDGProfile.loadDefinition("", ""));
	}

	@Test
	void dockTreeCannotPlaceAPanelOnItsOwnSplitNode() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		profile.getJSONObject("runtime_layout").getJSONArray("views")
				.getJSONObject(1).put("embedded_definition", "3");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void jsonSchemaPatternsMatchSubstringsUnlessExplicitlyAnchored() throws Exception {
		JSONObject schema = new JSONObject("{\"type\":\"object\",\"properties\":{"
				+ "\"name\":{\"type\":\"string\",\"pattern\":\"^cedg\\\\.\"}}}");
		GeoCeDGProfileSchema.validate(new JSONObject().put("name", "cedg.frontend.profile"),
				schema);
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfileSchema.validate(new JSONObject().put("name", "other"), schema));
	}
}
