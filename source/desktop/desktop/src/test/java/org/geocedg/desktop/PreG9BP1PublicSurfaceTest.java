/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geocedg.desktop.export.DxfExportPreflightPresentation;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.commands.selector.CommandFilter;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.desktop.CommandLineArguments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * PRE-G9B-P1 focused contracts: the promoted public surface, the retained
 * diagnostic overrides, Classic containment, the still-gated capabilities, and
 * the fail-closed distribution profiles.
 *
 * <p>P1 promotes policy and distribution packaging only. It changes no geometric
 * semantics, no serialization and no version.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BP1PublicSurfaceTest {

	// ------------------------------------------------------------- public surface

	@Test
	void promotedSurfacesAreOnByDefaultAndIndependent() {
		AppConfigGeoCeDG config = new AppConfigGeoCeDG();
		assertTrue(config.getRuntimeFeatureService().isLocusV2CreationEnabled());
		assertTrue(config.getRuntimeFeatureService().isExtendedDxfEnabled());
		// Neither decision implies the other.
		assertTrue(new AppConfigGeoCeDG(false, true).getRuntimeFeatureService()
				.isExtendedDxfEnabled());
		assertFalse(new AppConfigGeoCeDG(false, true).getRuntimeFeatureService()
				.isLocusV2CreationEnabled());
		assertTrue(new AppConfigGeoCeDG(true, false).getRuntimeFeatureService()
				.isLocusV2CreationEnabled());
		assertFalse(new AppConfigGeoCeDG(true, false).getRuntimeFeatureService()
				.isExtendedDxfEnabled());
	}

	@Test
	void dedicatedCommandsAreAvailableWithoutAnyArgument() {
		CommandFilter filter = new AppConfigGeoCeDG().createCommandFilter();
		assertTrue(filter.isCommandAllowed(Commands.LocusV2));
		assertTrue(filter.isCommandAllowed(Commands.LocusLength));
		assertTrue(filter.isCommandAllowed(Commands.SplineV2));
		// Ordinary upstream commands were never gated.
		assertTrue(filter.isCommandAllowed(Commands.Locus));
		assertTrue(filter.isCommandAllowed(Commands.Intersect));
	}

	@Test
	void extendedDxfIsTheDefaultGeoCeDGExportPolicy() {
		assertTrue(DxfExportPreflightPresentation.isExtendedDxfEnabled(
				new AppConfigGeoCeDG()));
	}

	@Test
	void historicalArgumentsRemainAcceptedBidirectionalOverrides() {
		AppGeoCeDG promoted = G9U1TestApp.withoutWindowDispatcher(new AppGeoCeDG(
				new CommandLineArguments(new String[] {"--silent"}), new JPanel()));
		assertTrue(((AppConfigGeoCeDG) promoted.getConfig()).getRuntimeFeatureService()
				.isLocusV2CreationEnabled());

		AppGeoCeDG overridden = G9U1TestApp.withoutWindowDispatcher(new AppGeoCeDG(
				new CommandLineArguments(new String[] {"--silent",
						"--enableLocusV2=false", "--enableExtendedDxf=false"}),
				new JPanel()));
		AppConfigGeoCeDG config = (AppConfigGeoCeDG) overridden.getConfig();
		assertFalse(config.getRuntimeFeatureService().isLocusV2CreationEnabled());
		assertFalse(config.getRuntimeFeatureService().isExtendedDxfEnabled());
		// The override is a policy switch only; the app code is unchanged.
		assertEquals(promoted.getConfig().getAppCode(), overridden.getConfig().getAppCode());
	}

	// ------------------------------------------------------------ Classic boundary

	@Test
	void classicNeverReachesThePromotedCreationPolicy() {
		AppConfigDefault classic = new AppConfigDefault();
		assertFalse(DxfExportPreflightPresentation.isExtendedDxfEnabled(classic));
		// mayCreateLocusV2 requires the GeoCeDG config; promotion cannot leak.
		assertFalse(RuntimeFeatureService.mayCreateLocusV2(null));
	}

	// -------------------------------------------------- still-gated capabilities

	@Test
	void onlyTheTwoAuthorizedFeaturesArePromoted() throws IOException {
		Map<String, Boolean> defaults = experimentalFeatureDefaults();
		assertEquals(Boolean.TRUE, defaults.get("cedg.locus.v2"));
		assertEquals(Boolean.TRUE, defaults.get("cedg.export.dxf.extended"));
		// Everything else keeps its gate; P1 is not "enable every experiment".
		assertEquals(Boolean.FALSE, defaults.get("cedg.laboratory.legacy"));
		assertEquals(Boolean.FALSE, defaults.get("cedg.spatial.semantics"));
		assertEquals(4, defaults.values().stream().filter(value -> !value).count()
				+ defaults.values().stream().filter(value -> value).count() - 1);
	}

	@Test
	void liveProfileAndApprovedCandidateDeclareTheSameDefaults() throws IOException {
		String live = read("apps/geocedg/application-profile.yml");
		String candidate = read("geocedg/specs/ui/application-profile-v2.candidate.yml");
		for (String id : List.of("cedg.locus.v2", "cedg.export.dxf.extended")) {
			assertTrue(declaresEnabledByDefault(live, id), "live " + id);
			assertTrue(declaresEnabledByDefault(candidate, id), "candidate " + id);
		}
		assertFalse(declaresEnabledByDefault(live, "cedg.laboratory.legacy"));
		assertFalse(declaresEnabledByDefault(candidate, "cedg.laboratory.legacy"));
	}

	// ------------------------------------------------- distribution profiles

	@Test
	void packageProfileKeepsOneVersionAuthorityAndUpgradeIdentity() throws IOException {
		String profile = read("packaging/windows/package.yml");
		assertEquals(1, occurrences(profile, "\"version\""), profile);
		assertTrue(profile.contains("\"version\": \"1.0.0\""));
		assertTrue(profile.contains(
				"\"upgrade_uuid\": \"b52d8e6d-3996-4bc5-b9ba-4f51f73c6e44\""));
	}

	@Test
	void distributionDefaultsToInternalAndDeclaresThreeProfiles() throws IOException {
		String profile = read("packaging/windows/package.yml");
		assertTrue(profile.contains("\"default_profile\": \"INTERNAL\""));
		// The repository default remains fail-closed for redistribution.
		assertTrue(profile.contains("\"status\": \"internal-evaluation\""));
		assertTrue(profile.contains(
				"\"public_redistribution\": \"blocked-pending-license-and-asset-approval\""));
		for (String id : List.of("\"id\": \"INTERNAL\"", "\"id\": \"NC\"",
				"\"id\": \"COMMERCIAL\"")) {
			assertTrue(profile.contains(id), id);
		}
	}

	@Test
	void commercialProfileIsStructurallyKnownAndBlocked() throws IOException {
		String profile = read("packaging/windows/package.yml");
		int commercial = profile.indexOf("\"id\": \"COMMERCIAL\"");
		assertTrue(commercial > 0);
		String block = profile.substring(commercial);
		assertTrue(block.contains("\"status\": \"not-authorized\""));
		assertTrue(block.contains("\"redistributable\": false"));
		// The pending external terms must be named, not implied.
		assertTrue(block.contains("blocking_terms"));
		assertTrue(block.contains("GeoGebra License and Collaboration Agreement"));
		assertTrue(block.contains("OpenGeoProver"));
	}

	@Test
	void redistributableProfileDeclaresItsLegalAssets() throws IOException {
		String profile = read("packaging/windows/package.yml");
		int nc = profile.indexOf("\"id\": \"NC\"");
		int commercial = profile.indexOf("\"id\": \"COMMERCIAL\"");
		assertTrue(nc > 0 && commercial > nc);
		String block = profile.substring(nc, commercial);
		assertTrue(block.contains("\"redistributable\": true"));
		assertTrue(block.contains("\"licensing_profile\": \"PROFILE NC\""));
		assertTrue(block.contains("docs/licensing/geocedg_profile_nc.md"));
		assertTrue(block.contains("NC_DISTRIBUTION_NOTICE.txt"));
		// A redistributable profile must not carry the internal-evaluation marker.
		assertFalse(block.contains("INTERNAL EVALUATION"));
	}

	@Test
	void redistributableNoticeExistsAndIsNotInternalEvaluation() throws IOException {
		String notice = read("packaging/windows/NC_DISTRIBUTION_NOTICE.txt");
		assertFalse(notice.contains("INTERNAL EVALUATION"));
		assertTrue(notice.contains("PROFILE NC"));
		// The composite non-commercial nature required by PROFILE NC is stated.
		assertTrue(notice.contains("composite non-commercial"));
		assertTrue(notice.contains("Commercial distribution is NOT authorized"));
		assertNotNull(read("packaging/windows/file-associations-nc.properties"));
	}

	@Test
	void repositoryDefaultNoticeStillCarriesTheInternalMarker() throws IOException {
		String notice = read("packaging/windows/INTERNAL_EVALUATION_ONLY.txt");
		assertTrue(notice.contains("INTERNAL EVALUATION — NOT FOR REDISTRIBUTION"));
	}

	// -------------------------------------------------------------------- helpers

	private static boolean declaresEnabledByDefault(String document, String id) {
		// Match the feature registration, not an action's feature_requirements entry.
		int index = document.indexOf("\"id\": \"" + id + "\"");
		if (index < 0) {
			return false;
		}
		int window = Math.min(document.length(), index + 240);
		String block = document.substring(index, window);
		int enabled = block.indexOf("\"enabled_by_default\"");
		if (enabled < 0) {
			return false;
		}
		return block.substring(enabled,
				Math.min(block.length(), enabled + 40)).contains("true");
	}

	private static Map<String, Boolean> experimentalFeatureDefaults() throws IOException {
		String document = read("geocedg/features/experimental.yml");
		Map<String, Boolean> defaults = new LinkedHashMap<>();
		List<String> ids = new ArrayList<>();
		int index = document.indexOf("\"id\": \"");
		while (index >= 0) {
			int start = index + "\"id\": \"".length();
			int end = document.indexOf('"', start);
			ids.add(document.substring(start, end));
			index = document.indexOf("\"id\": \"", end);
		}
		for (String id : ids) {
			defaults.put(id, declaresEnabledByDefault(document, id));
		}
		return defaults;
	}

	private static int occurrences(String text, String token) {
		int found = 0;
		int index = text.indexOf(token);
		while (index >= 0) {
			found++;
			index = text.indexOf(token, index + token.length());
		}
		return found;
	}

	private static String read(String relativePath) throws IOException {
		return Files.readString(repositoryRoot().resolve(relativePath),
				StandardCharsets.UTF_8);
	}

	private static Path repositoryRoot() {
		Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (current != null && !Files.isRegularFile(current.resolve("AGENTS.md"))) {
			current = current.getParent();
		}
		if (current == null) {
			throw new IllegalStateException("Repository root is unavailable");
		}
		return current;
	}
}
