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
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
	void nativeToolbarContainsFortyFourCuratedModesWhileCatalogRetainsAllSixtySix() {
		String[] modes = GeoCeDGProfile.getToolbarDefinition().split("[ |]+");
		assertEquals(44, modes.length);
		assertEquals(44, new HashSet<>(Arrays.asList(modes)).size());
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
	void duplicateMenuPresentationGroupFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		JSONObject duplicate = profile.getJSONArray("menu_sections").getJSONObject(1)
				.getJSONArray("entries").getJSONObject(0);
		profile.getJSONArray("menu_sections").getJSONObject(0)
				.getJSONArray("entries").put(duplicate);
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void dynamicUserToolsEntryIsRequiredExactlyOnceAndCannotBecomeAGroup()
			throws Exception {
		JSONObject staticGroup = GeoCeDGProfile.getCatalog();
		JSONObject userTools = userToolsEntry(staticGroup);
		userTools.put("kind", "group");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(staticGroup.toString()));

		JSONObject duplicated = GeoCeDGProfile.getCatalog();
		JSONObject duplicate = new JSONObject(userToolsEntry(duplicated).toString());
		GeoCeDGMenuBar.find(duplicated.getJSONArray("menu_sections"), "help")
				.getJSONArray("entries").put(duplicate);
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(duplicated.toString()));
	}

	@Test
	void globalPropertiesActionIsDeclaredAsPreferenceOnly() throws Exception {
		JSONObject properties = GeoCeDGMenuBar.find(
				GeoCeDGProfile.getCatalog().getJSONArray("actions"), "view.properties");
		assertEquals("preference-action", properties.getString("kind"));
		assertEquals("host.preference.global-properties",
				properties.getJSONObject("target").getString("symbol"));
		assertEquals("preference-only", properties.getString("effect_profile_id"));
	}

	@Test
	void unknownAndDuplicatePresentationActionsFailClosed() throws Exception {
		JSONObject unknown = GeoCeDGProfile.getCatalog();
		unknown.getJSONArray("presentation_groups").getJSONObject(0)
				.getJSONArray("action_ids").put("not.an.action");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(unknown.toString()));

		JSONObject duplicate = GeoCeDGProfile.getCatalog();
		duplicate.getJSONArray("presentation_groups").getJSONObject(0)
				.getJSONArray("action_ids").put("document.save");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(duplicate.toString()));
	}

	@Test
	void presentationGroupsCoverCatalogOnceAndDriveToolbarOrder() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		Set<String> ids = new HashSet<>();
		for (int i = 0; i < profile.getJSONArray("presentation_groups").length(); i++) {
			JSONObject group = profile.getJSONArray("presentation_groups").getJSONObject(i);
			for (String id : GeoCeDGProfile.strings(group.getJSONArray("action_ids"))) {
				assertTrue(ids.add(id), id);
			}
		}
		assertEquals(110, ids.size());
		assertEquals(List.of("edit-selection", "construction-relations",
				"construction-lines-vectors",
				"construction-polygons",
				"construction-derived", "construction-circles-conics",
				"construction-semantic-curves", "construction-metrics",
				"construction-transforms", "construction-parameters",
				"view-navigation"),
				GeoCeDGProfile.strings(profile.getJSONArray("toolbar_group_ids")));
	}

	@Test
	void toolbarProjectionRegroupsExistingActionsAndDeclaresOnlyTwoMixedFlyouts()
			throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		JSONObject relations = GeoCeDGMenuBar.find(
				profile.getJSONArray("presentation_groups"), "construction-relations");
		assertEquals(List.of("construction.point", "construction.point-on-object",
				"construction.attach-detach", "relation.intersect"),
				GeoCeDGProfile.strings(relations.getJSONArray("toolbar_action_ids")));

		JSONObject semantic = GeoCeDGMenuBar.find(
				profile.getJSONArray("presentation_groups"), "construction-semantic-curves");
		assertEquals("profile-flyout", semantic.getString("toolbar_rendering"));
		assertEquals(List.of("semantic.locus-v2.create", "semantic.spline-v2.create",
				"semantic.locus-v2.point-explicit"),
				GeoCeDGProfile.strings(semantic.getJSONArray("toolbar_action_ids")));

		JSONObject navigation = GeoCeDGMenuBar.find(
				profile.getJSONArray("presentation_groups"), "view-navigation");
		assertEquals("profile-flyout", navigation.getString("toolbar_rendering"));
		assertEquals(List.of("navigation.pan-view", "navigation.zoom-window",
				"navigation.zoom-in", "navigation.zoom-out", "presentation.copy-style"),
				GeoCeDGProfile.strings(navigation.getJSONArray("toolbar_action_ids")));
	}

	@Test
	void nativeToolbarFlyoutsMatchTheRequestedConstructionGroups() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		var groups = profile.getJSONArray("presentation_groups");
		assertEquals(List.of("construction.move", "construction.move-rotate"),
				toolbarIds(groups, "edit-selection"));
		assertEquals(List.of("construction.line", "construction.segment", "construction.ray",
				"construction.vector", "construction.fixed-segment",
				"construction.vector-from-point"),
				toolbarIds(groups, "construction-lines-vectors"));
		assertEquals(List.of("construction.polygon", "construction.polyline",
				"construction.regular-polygon", "construction.rigid-polygon",
				"construction.vector-polygon"),
				toolbarIds(groups, "construction-polygons"));
		assertEquals(List.of("construction.parallel-line", "construction.perpendicular-line",
				"construction.midpoint", "construction.perpendicular-bisector",
				"construction.angle-bisector", "parameter.fixed-angle",
				"relation.tangent"),
				toolbarIds(groups, "construction-derived"));
		assertEquals(List.of("parameter.slider", "parameter.checkbox",
				"parameter.button", "parameter.input-box"),
				toolbarIds(groups, "construction-parameters"));
		assertTrue(toolbarIds(groups, "construction-points").isEmpty());
	}

	@Test
	void unknownToolbarRenderingFailsClosed() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		GeoCeDGMenuBar.find(profile.getJSONArray("presentation_groups"),
				"construction-semantic-curves").put("toolbar_rendering", "invented");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(profile.toString()));
	}

	@Test
	void malformedPresentationEntryAndUnreachableToolbarGroupFailClosed() throws Exception {
		JSONObject missingGroup = GeoCeDGProfile.getCatalog();
		missingGroup.getJSONArray("menu_sections").getJSONObject(0)
				.getJSONArray("entries").getJSONObject(0).put("group_id", "not.a.group");
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(missingGroup.toString()));

		JSONObject unreachable = GeoCeDGProfile.getCatalog();
		unreachable.getJSONArray("toolbar_group_ids").remove(0);
		assertThrows(IllegalStateException.class,
				() -> GeoCeDGProfile.compileProfile(unreachable.toString()));
	}

	@Test
	void toolbarPresentationCannotDriftFromOperationalCatalog() throws Exception {
		JSONObject profile = GeoCeDGProfile.getCatalog();
		for (int i = 0; i < profile.getJSONArray("presentation_groups").length(); i++) {
			JSONObject group = profile.getJSONArray("presentation_groups").getJSONObject(i);
			if (group.getJSONArray("toolbar_action_ids").length() > 0) {
				group.getJSONArray("toolbar_action_ids").remove(0);
				break;
			}
		}
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
			String english = GeoCeDGProfile.getText(key, "en");
			String spanish = GeoCeDGProfile.getText(key, "es");
			assertFalse(english.isBlank(), key);
			assertFalse(spanish.isBlank(), key);
			assertFalse(english.contains(
					"Use the current construction and its explicit selection."), key);
			assertFalse(spanish.contains(
					"Usa la construcción actual y su selección explícita."), key);
			if (key.endsWith(".short_help") || key.endsWith(".long_help")) {
				assertFalse(english.toLowerCase(Locale.ROOT).matches(
						".*\\b(todo|tbd|placeholder|lorem ipsum)\\b.*"), key);
			}
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

	private static List<String> toolbarIds(
			org.geogebra.common.move.ggtapi.models.json.JSONArray groups, String id)
			throws Exception {
		return GeoCeDGProfile.strings(
				GeoCeDGMenuBar.find(groups, id).getJSONArray("toolbar_action_ids"));
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

	private static JSONObject userToolsEntry(JSONObject profile) throws Exception {
		JSONObject automation = GeoCeDGMenuBar.find(
				profile.getJSONArray("menu_sections"), "automation");
		for (int i = 0; i < automation.getJSONArray("entries").length(); i++) {
			JSONObject entry = automation.getJSONArray("entries").getJSONObject(i);
			if ("user-tools".equals(entry.getString("kind"))) {
				return entry;
			}
		}
		throw new AssertionError("Missing user-tools fixture entry");
	}
}
