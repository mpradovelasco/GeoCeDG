/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.geocedg.desktop.GeoCeDGDistributionCondition.Expected;
import org.geocedg.desktop.GeoCeDGDistributionCondition.State;
import org.geocedg.desktop.export.GeoCeDGBuildProvenance;
import org.geocedg.desktop.export.GeoCeDGBuildProvenance.DistributionProfile;
import org.geocedg.desktop.export.GeoCeDGBuildProvenance.RepositoryState;
import org.geocedg.desktop.export.GeoCeDGBuildProvenance.ResolutionSource;
import org.geogebra.common.GeoGebraConstants;
import org.geogebra.desktop.gui.menubar.GeoGebraMenuBar;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * PRE-G9B-R6-plus-G focused contracts (product metadata contract PM-1 to PM-12, help
 * coverage): the distribution condition of the running copy from fixture records, the
 * About content and order in English and Spanish, the licensing summary against its
 * authorities, the inherited Classic About route and the bilingual guide coverage.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusGProductMetadataTest {
	private static final String COMMIT = "0123456789abcdef0123456789abcdef01234567";
	private static final String DIGEST =
			"1111111111111111111111111111111111111111111111111111111111111111";
	private static final String INTERNAL_MARKER =
			"INTERNAL EVALUATION \u2014 NOT FOR REDISTRIBUTION";
	private static final String NC_MARKER = "NON-COMMERCIAL DISTRIBUTION \u2014 PROFILE NC";
	private static final String COMMERCIAL_MARKER =
			"COMMERCIAL DISTRIBUTION \u2014 NOT AUTHORIZED";
	private static final List<DistributionProfile> TABLE = List.of(
			new DistributionProfile("INTERNAL", "internal-evaluation", INTERNAL_MARKER, false),
			new DistributionProfile("NC", "public-non-commercial", NC_MARKER, true),
			new DistributionProfile("COMMERCIAL", "not-authorized", COMMERCIAL_MARKER, false));
	private static final Expected EXPECTED = new Expected("9.8.7", "2001-02-03", COMMIT,
			DIGEST, TABLE);

	@TempDir
	Path temporary;

	// ---------------------------------------------------------------- PM-1

	@Test
	void productNameIsThePackageProfileConstant() throws IOException {
		Map<?, ?> profile = (Map<?, ?>) GeoCeDGStrictJson.parse(read(
				"packaging/windows/package.yml"));
		assertEquals(GeoCeDGProductInfo.PRODUCT_NAME,
				((Map<?, ?>) profile.get("application")).get("name"));
		Map<?, ?> schema = (Map<?, ?>) GeoCeDGStrictJson.parse(read(
				"geocedg/specs/operations/package-profile.schema.json"));
		Map<?, ?> name = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) schema
				.get("properties")).get("application")).get("properties")).get("name");
		assertEquals(GeoCeDGProductInfo.PRODUCT_NAME, name.get("const"));
		assertTrue(GeoCeDGProductInfo.applicationTitle().startsWith("GeoCeDG "));
	}

	// ------------------------------------------------- PM-2, PM-3 version transition

	@Test
	void theSingleAuthorityDeclaresTheVersionAndItsDesignatedDate() throws IOException {
		String text = read("packaging/windows/package.yml");
		assertEquals(1, count(text, "\"version\"\\s*:"), "exactly one version key");
		assertEquals(1, count(text, "\"version_date\"\\s*:"), "exactly one version_date key");
		Map<?, ?> application = (Map<?, ?>) ((Map<?, ?>) GeoCeDGStrictJson.parse(text))
				.get("application");
		assertEquals("1.1.0", application.get("version"));
		assertEquals("2026-10-15", application.get("version_date"));
		assertTrue(GeoCeDGBuildProvenance.isCalendarDate("2026-10-15"));
		assertEquals("1.1.0", GeoCeDGProductInfo.semanticVersion());
		assertEquals("2026-10-15", GeoCeDGProductInfo.versionDate());
		assertEquals("GeoCeDG 1.1.0", GeoCeDGProductInfo.applicationTitle());
		// the version date is the author-designated value of the authority, never a
		// build, commit or file time: the generated record carries no other date field
		java.util.Properties generated = generatedProvenance();
		assertEquals("2026-10-15", generated.getProperty("application.version.date"));
		for (String key : generated.stringPropertyNames()) {
			String lower = key.toLowerCase(java.util.Locale.ROOT);
			assertFalse(lower.contains("time") || lower.contains("timestamp")
					|| (lower.contains("date") && !key.equals("application.version.date")), key);
		}
	}

	@Test
	void theSchemasMoveTogetherAndFailClosedOnTheOldVersions() throws Exception {
		Map<?, ?> schema = (Map<?, ?>) GeoCeDGStrictJson.parse(read(
				"geocedg/specs/operations/package-profile.schema.json"));
		assertEquals("https://geocedg.local/schemas/package-profile-v2", schema.get("$id"));
		Map<?, ?> properties = (Map<?, ?>) schema.get("properties");
		assertEquals(2L, ((Map<?, ?>) properties.get("schema_version")).get("const"));
		Map<?, ?> application = (Map<?, ?>) properties.get("application");
		assertTrue(((List<?>) application.get("required")).contains("version_date"));
		assertNotNull(((Map<?, ?>) ((Map<?, ?>) application.get("properties"))
				.get("version_date")).get("pattern"));
		assertEquals(2L, ((Map<?, ?>) GeoCeDGStrictJson.parse(read(
				"packaging/windows/package.yml"))).get("schema_version"));
		// provenance schema 3 only
		java.util.Properties generated = generatedProvenance();
		assertEquals("3", generated.getProperty("schema.version"));
		assertNotNull(GeoCeDGBuildProvenance.read(generated));
		java.util.Properties schemaTwo = (java.util.Properties) generated.clone();
		schemaTwo.setProperty("schema.version", "2");
		assertThrows(IllegalStateException.class, () -> GeoCeDGBuildProvenance.read(schemaTwo));
		java.util.Properties undated = (java.util.Properties) generated.clone();
		undated.remove("application.version.date");
		assertThrows(IllegalStateException.class, () -> GeoCeDGBuildProvenance.read(undated));
		java.util.Properties extra = (java.util.Properties) generated.clone();
		extra.setProperty("build.time", "2026-10-15T00:00:00Z");
		assertThrows(IllegalStateException.class, () -> GeoCeDGBuildProvenance.read(extra));
		java.util.Properties flag = (java.util.Properties) generated.clone();
		flag.setProperty("distribution.profile.0.redistributable", "yes");
		assertThrows(IllegalStateException.class, () -> GeoCeDGBuildProvenance.read(flag));
		// the builder and the packaging check accept exactly package-profile schema 2
		String builder = read("tools/release/build-windows-package.ps1");
		assertTrue(builder.contains("($profile.schema_version -eq 2)"));
		assertFalse(builder.contains("($profile.schema_version -eq 1)"));
		String check = read("tools/agent/checks/packaging-product.ps1");
		assertTrue(check.contains("$profile.schema_version -eq 2 -and"));
		assertTrue(check.contains("package-profile-v2"));
		assertFalse(check.contains("package-profile-v1"));
	}

	@Test
	void theBuildEmbedsTheCanonicalBindingOfThePackageProfile() throws IOException {
		byte[] bytes = Files.readAllBytes(repositoryRoot().resolve("packaging/windows/package.yml"));
		String lf = new String(bytes, StandardCharsets.UTF_8).replace("\r\n", "\n");
		String digest = canonicalSha256(lf.getBytes(StandardCharsets.UTF_8));
		assertEquals(digest, canonicalSha256(lf.replace("\n", "\r\n")
				.getBytes(StandardCharsets.UTF_8)), "CRLF and LF copies have one digest");
		assertEquals(digest, canonicalSha256(lf.replace("\n", "\r")
				.getBytes(StandardCharsets.UTF_8)), "lone CR normalizes too");
		assertFalse(digest.equals(canonicalSha256((lf + " ").getBytes(StandardCharsets.UTF_8))));
		GeoCeDGBuildProvenance build = GeoCeDGProductInfo.provenance();
		assertEquals(digest, build.getPackageProfileSha256());
		assertEquals("2026-10-15", build.getApplicationVersionDate());
		List<?> declared = (List<?>) ((Map<?, ?>) ((Map<?, ?>) GeoCeDGStrictJson.parse(lf))
				.get("distribution")).get("profiles");
		assertEquals(declared.size(), build.getDistributionProfiles().size());
		for (int index = 0; index < declared.size(); index++) {
			Map<?, ?> profile = (Map<?, ?>) declared.get(index);
			DistributionProfile embedded = build.getDistributionProfiles().get(index);
			assertEquals(profile.get("id"), embedded.getId());
			assertEquals(profile.get("status"), embedded.getStatus());
			assertEquals(profile.get("marker"), embedded.getMarker());
			assertEquals(profile.get("redistributable"), embedded.isRedistributable());
		}
		if (build.isEstablished()) {
			assertNotNull(Expected.of(build), "the build carries a complete binding");
		}
	}

	@Test
	void aboutShowsTheDesignatedVersionDateOfThisVersion() {
		GeoCeDGDistributionCondition development = GeoCeDGDistributionCondition.evaluate(null,
				null);
		String english = GeoCeDGAboutText.compose("en", GeoCeDGProductInfo.provenance(),
				development);
		String spanish = GeoCeDGAboutText.compose("es", GeoCeDGProductInfo.provenance(),
				development);
		assertTrue(english.startsWith("GeoCeDG 1.1.0\nVersion date: 2026-10-15\nBuild: "),
				english);
		assertTrue(spanish.startsWith("GeoCeDG 1.1.0\nFecha de versión: 2026-10-15\n"
				+ "Compilación: "), spanish);
	}

	// ------------------------------------------------------- strict record reader

	@Test
	void theRecordReaderIsStrict() {
		Map<?, ?> parsed = (Map<?, ?>) GeoCeDGStrictJson.parse(
				" {\"a\": [1, -2, 3.5, true, false, null, \"x\\u00e9\\n\"], \"b\": {}} ");
		assertEquals(List.of("a", "b"), new ArrayList<>(parsed.keySet()));
		List<?> items = (List<?>) parsed.get("a");
		assertEquals(1L, items.get(0));
		assertEquals(-2L, items.get(1));
		assertEquals(new java.math.BigDecimal("3.5"), items.get(2));
		assertSame(GeoCeDGStrictJson.NULL, items.get(5));
		assertEquals("x\u00e9\n", items.get(6));
		for (String invalid : List.of("{a:1}", "{'a':1}", "{\"a\":1,}", "[1,]",
				"{\"a\":1,\"a\":2}", "{\"a\":01}", "{\"a\":1} x", "\ufeff{}",
				"{\"a\":\"\u0001\"}", "{\"a\":1 // c\n}", "{\"a\":tru}", "{\"a\":\"\\x\"}",
				"", "{\"a\":+1}", "{\"a\":.5}", "{\"a\":1.}", "{\"a\":NaN}")) {
			assertThrows(IllegalArgumentException.class, () -> GeoCeDGStrictJson.parse(invalid),
					invalid);
		}
	}

	// ------------------------------------------------------------- PM-5 states

	@Test
	void developmentWhenNoJpackageLauncherStartedTheCopy() {
		for (String launcher : new String[] {null, "", "  "}) {
			GeoCeDGDistributionCondition condition = GeoCeDGDistributionCondition
					.evaluate(launcher, EXPECTED);
			assertSame(State.DEVELOPMENT, condition.getState());
			assertFalse(condition.isPackaged());
			assertNull(condition.getMarker());
		}
		// the test JVM is not a jpackage launch
		assertSame(State.DEVELOPMENT, GeoCeDGDistributionCondition.current().getState());
	}

	@Test
	void verifiedInternalAndNcRecordsGiveTheirStates() throws IOException {
		Path launcher = install("internal", record("INTERNAL", unchanged()), "internal notice");
		GeoCeDGDistributionCondition internal = GeoCeDGDistributionCondition.evaluate(
				launcher.toString(), EXPECTED);
		assertSame(State.INTERNAL, internal.getState(), internal.getCause());
		assertEquals(INTERNAL_MARKER, internal.getMarker());
		assertEquals("INTERNAL_EVALUATION_ONLY.txt", internal.getNoticeFile());
		assertTrue(internal.isPackaged());

		Path nc = install("nc", record("NC", values -> {
			values.put("status", "public-non-commercial");
			values.put("redistributable", true);
			values.put("marker", NC_MARKER);
			values.put("notice_file", "NC_DISTRIBUTION_NOTICE.txt");
		}), "nc notice");
		GeoCeDGDistributionCondition verified = GeoCeDGDistributionCondition.evaluate(
				nc.toString(), EXPECTED);
		assertSame(State.NC, verified.getState(), verified.getCause());
		assertEquals(NC_MARKER, verified.getMarker());
		assertEquals("NC_DISTRIBUTION_NOTICE.txt", verified.getNoticeFile());
	}

	@Test
	void everyDefectGivesUnverifiedAndNeverARedistributableState() throws IOException {
		Map<String, Consumer<Map<String, Object>>> defects = new LinkedHashMap<>();
		defects.put("mismatched version", values -> app(values).put("version", "9.8.8"));
		defects.put("mismatched date", values -> app(values).put("version_date",
				"2001-02-04"));
		defects.put("mismatched name", values -> app(values).put("name", "GeoGebra"));
		defects.put("mismatched revision", values -> values.put("source_revision",
				COMMIT.replace('0', 'f')));
		defects.put("mismatched digest", values -> values.put("package_profile_sha256",
				DIGEST.replace('1', '2')));
		defects.put("mismatched notice hash", values -> values.put("notice_sha256",
				DIGEST));
		defects.put("mismatched marker", values -> values.put("marker", NC_MARKER));
		defects.put("mismatched status", values -> values.put("status",
				"public-non-commercial"));
		defects.put("mismatched flag", values -> values.put("redistributable", true));
		defects.put("COMMERCIAL", values -> {
			values.put("profile_id", "COMMERCIAL");
			values.put("status", "not-authorized");
			values.put("marker", COMMERCIAL_MARKER);
		});
		defects.put("combined profile", values -> values.put("profile_id", "INTERNAL | NC"));
		defects.put("undeclared profile", values -> values.put("profile_id", "PUBLIC"));
		defects.put("unsupported schema", values -> values.put("schema_version", 2));
		defects.put("wrong kind", values -> values.put("record_kind", "other"));
		defects.put("extra member", values -> values.put("approved", true));
		defects.put("missing member", values -> values.remove("notice_sha256"));
		defects.put("extra application member", values -> app(values).put("date", "x"));
		defects.put("path in notice name", values -> values.put("notice_file",
				"../INTERNAL_EVALUATION_ONLY.txt"));
		defects.put("absent notice", values -> values.put("notice_file", "OTHER.txt"));
		int index = 0;
		for (Map.Entry<String, Consumer<Map<String, Object>>> defect : defects.entrySet()) {
			Path launcher = install("defect" + index++, record("INTERNAL",
					defect.getValue()), "internal notice");
			assertUnverified(launcher, EXPECTED, defect.getKey());
		}
		// a profile whose declared status is not-authorized never verifies
		List<DistributionProfile> revoked = List.of(new DistributionProfile("INTERNAL",
				"not-authorized", INTERNAL_MARKER, false));
		assertUnverified(install("revoked", record("INTERNAL", values ->
				values.put("status", "not-authorized")), "internal notice"),
				new Expected("9.8.7", "2001-02-03", COMMIT, DIGEST, revoked),
				"not-authorized status");
	}

	@Test
	void missingMalformedOversizedOrUnboundRecordsGiveUnverified() throws IOException {
		Path missing = temporary.resolve("missing");
		Files.createDirectories(missing.resolve("app"));
		assertUnverified(missing.resolve("GeoCeDG.exe"), EXPECTED, "missing record");
		for (String[] bad : new String[][] {{"malformed", "{\"schema_version\": 1,"},
				{"bom", "\ufeff" + json(baseRecord("INTERNAL"))},
				{"duplicate", json(baseRecord("INTERNAL")).replace("{",
						"{\"profile_id\": \"INTERNAL\", ")},
				{"lenient", json(baseRecord("INTERNAL")).replace("\"profile_id\"",
						"profile_id")}}) {
			Path launcher = installRaw(bad[0], bad[1].getBytes(StandardCharsets.UTF_8),
					"internal notice");
			assertUnverified(launcher, EXPECTED, bad[0]);
		}
		byte[] invalidUtf8 = json(baseRecord("INTERNAL")).getBytes(StandardCharsets.UTF_8);
		invalidUtf8[invalidUtf8.length - 3] = (byte) 0xC3;
		assertUnverified(installRaw("utf8", invalidUtf8, "internal notice"), EXPECTED,
				"invalid UTF-8");
		byte[] oversized = (json(baseRecord("INTERNAL")) + " ".repeat(
				GeoCeDGDistributionCondition.MAX_RECORD_BYTES)).getBytes(StandardCharsets.UTF_8);
		assertUnverified(installRaw("oversized", oversized, "internal notice"), EXPECTED,
				"oversized record");
		Path valid = install("unbound", record("INTERNAL", unchanged()), "internal notice");
		assertUnverified(valid, null, "a build without a binding");
	}

	@Test
	void theBindingNeedsEveryEmbeddedPart() {
		GeoCeDGBuildProvenance full = provenance(COMMIT, RepositoryState.CLEAN, "2001-02-03",
				DIGEST, TABLE);
		Expected expected = Expected.of(full);
		assertNotNull(expected);
		assertSame(TABLE.get(1).getStatus(), expected.profile("NC").getStatus());
		assertNull(Expected.of(provenance(COMMIT, RepositoryState.CLEAN, null, DIGEST, TABLE)));
		assertNull(Expected.of(provenance(COMMIT, RepositoryState.CLEAN, "2001-02-03", null,
				TABLE)));
		assertNull(Expected.of(provenance(COMMIT, RepositoryState.CLEAN, "2001-02-03",
				DIGEST, List.of())));
		assertNull(Expected.of(new GeoCeDGBuildProvenance("9.8.7", "UNAVAILABLE",
				RepositoryState.UNAVAILABLE, ResolutionSource.UNAVAILABLE, "2001-02-03",
				DIGEST, TABLE)));
		assertThrows(IllegalArgumentException.class, () -> provenance(COMMIT,
				RepositoryState.CLEAN, "2001-02-30", DIGEST, TABLE), "not a calendar date");
		assertThrows(IllegalArgumentException.class, () -> provenance(COMMIT,
				RepositoryState.CLEAN, "2001-2-3", DIGEST, TABLE), "not ISO form");
		assertThrows(IllegalArgumentException.class, () -> provenance(COMMIT,
				RepositoryState.CLEAN, "2001-02-03", DIGEST.replace('1', 'A'), TABLE));
		assertThrows(IllegalArgumentException.class, () -> provenance(COMMIT,
				RepositoryState.CLEAN, "2001-02-03", DIGEST, List.of(TABLE.get(0),
						TABLE.get(0))), "duplicate profile");
	}

	// ------------------------------------------------------------------- PM-4

	@Test
	void buildIdentityRendersEveryRepositoryState() {
		assertEquals("Build: 0123456789ab (clean)", GeoCeDGAboutText.buildIdentity("en",
				provenance(COMMIT, RepositoryState.CLEAN, null, null, List.of())));
		assertEquals("Build: 0123456789ab (modified)", GeoCeDGAboutText.buildIdentity("en",
				provenance(COMMIT, RepositoryState.DIRTY, null, null, List.of())));
		assertEquals("Build: 0123456789ab (state unavailable)", GeoCeDGAboutText
				.buildIdentity("en", provenance(COMMIT, RepositoryState.UNAVAILABLE, null,
						null, List.of())));
		GeoCeDGBuildProvenance unavailable = new GeoCeDGBuildProvenance("9.8.7",
				"UNAVAILABLE", RepositoryState.UNAVAILABLE, ResolutionSource.UNAVAILABLE);
		assertEquals("Build: unavailable", GeoCeDGAboutText.buildIdentity("en", unavailable));
		assertEquals("Compilación: 0123456789ab (sin cambios)", GeoCeDGAboutText
				.buildIdentity("es", provenance(COMMIT, RepositoryState.CLEAN, null, null,
						List.of())));
		assertEquals("Compilación: 0123456789ab (modificada)", GeoCeDGAboutText
				.buildIdentity("es", provenance(COMMIT, RepositoryState.DIRTY, null, null,
						List.of())));
		assertEquals("Compilación: no disponible", GeoCeDGAboutText.buildIdentity("es",
				unavailable));
	}

	// ----------------------------------------------------------- PM-6 / PM-7

	@Test
	void aboutContentAndOrderAreTheSameInEnglishAndSpanish() throws IOException {
		GeoCeDGBuildProvenance build = provenance(COMMIT, RepositoryState.CLEAN, "2001-02-03",
				DIGEST, TABLE);
		Path launcher = install("about", record("INTERNAL", unchanged()), "internal notice");
		GeoCeDGDistributionCondition internal = GeoCeDGDistributionCondition.evaluate(
				launcher.toString(), EXPECTED);
		assertSame(State.INTERNAL, internal.getState());
		String english = GeoCeDGAboutText.compose("en", build, internal);
		assertOrder(english, "GeoCeDG 9.8.7\n", "Version date: 2001-02-03",
				"Build: 0123456789ab (clean)",
				"Distribution condition: " + INTERNAL_MARKER
						+ " (internal evaluation \u2014 not for redistribution)",
				"GeoCeDG \u2014 Computational extended Descriptive Geometry based on GeoGebra.",
				"GeoGebra upstream baseline: " + GeoGebraConstants.VERSION_STRING,
				"Manuel Prado-Velasco, Universidad de Sevilla",
				"Licensing. GeoCeDG is a composite work",
				"GeoCeDG-authored software: EUPL-1.2.",
				"documentation and ordinary artwork: CC BY 4.0",
				"Inherited GeoGebra source code keeps its EUPL-1.2 grant.",
				"Distribution notice of this copy: app/INTERNAL_EVALUATION_ONLY.txt.",
				"Full texts: app/legal/");
		String spanish = GeoCeDGAboutText.compose("es", build, internal);
		assertOrder(spanish, "GeoCeDG 9.8.7\n", "Fecha de versión: 2001-02-03",
				"Compilación: 0123456789ab (sin cambios)",
				"Condición de distribución: " + INTERNAL_MARKER
						+ " (evaluación interna \u2014 no redistribuible)",
				"GeoCeDG \u2014 Geometría Descriptiva computacional extendida",
				"Versión base upstream de GeoGebra: " + GeoGebraConstants.VERSION_STRING,
				"Manuel Prado-Velasco, Universidad de Sevilla",
				"Licencias. GeoCeDG es una obra compuesta",
				"Software de autoría GeoCeDG: EUPL-1.2.",
				"obra gráfica ordinaria de autoría GeoCeDG: CC BY 4.0",
				"El código fuente heredado de GeoGebra conserva su concesión EUPL-1.2.",
				"Aviso de distribución de esta copia: app/INTERNAL_EVALUATION_ONLY.txt.",
				"Textos completos: app/legal/");
		assertEquals(english.split("\n").length, spanish.split("\n").length,
				"same line structure");

		// without a declared version date there is no date line at all
		GeoCeDGBuildProvenance undated = provenance(COMMIT, RepositoryState.DIRTY, null,
				null, List.of());
		GeoCeDGDistributionCondition development = GeoCeDGDistributionCondition.evaluate(
				null, null);
		String plain = GeoCeDGAboutText.compose("en", undated, development);
		assertFalse(plain.contains("Version date"), plain);
		assertOrder(plain, "GeoCeDG 9.8.7\nBuild: 0123456789ab (modified)\n"
				+ "Distribution condition: development build \u2014 not a distribution; "
				+ "not for redistribution",
				"A development build has no distribution notice",
				"Full texts: LICENSE, NOTICE.md, THIRD_PARTY.md and LICENSES/ in the source "
						+ "repository, with docs/LICENSE.md and "
						+ "docs/licensing/component-matrix.md.");
		GeoCeDGDistributionCondition unverified = GeoCeDGDistributionCondition.evaluate(
				temporary.resolve("none").resolve("GeoCeDG.exe").toString(), EXPECTED);
		String notVerified = GeoCeDGAboutText.compose("es", undated, unverified);
		assertOrder(notVerified, "Condición de distribución: condición de distribución no "
				+ "verificada \u2014 no redistribuible",
				"No se ha podido verificar el aviso de distribución de esta copia",
				"Textos completos: app/legal/");
		Path nc = install("about-nc", record("NC", values -> {
			values.put("status", "public-non-commercial");
			values.put("redistributable", true);
			values.put("marker", NC_MARKER);
			values.put("notice_file", "NC_DISTRIBUTION_NOTICE.txt");
		}), "nc notice");
		String ncText = GeoCeDGAboutText.compose("en", build,
				GeoCeDGDistributionCondition.evaluate(nc.toString(), EXPECTED));
		assertOrder(ncText, NC_MARKER + " (non-commercial distribution under PROFILE NC)",
				"Distribution notice of this copy: app/NC_DISTRIBUTION_NOTICE.txt.",
				"Full texts: app/legal/");

		for (String text : List.of(english, spanish, plain, notVerified, ncText)) {
			String lower = text.toLowerCase(java.util.Locale.ROOT);
			assertFalse(lower.contains("solely under eupl"), text);
			assertFalse(lower.contains("únicamente bajo eupl"), text);
			assertFalse(lower.contains("open source"), text);
			assertFalse(lower.contains("free software"), text);
			assertFalse(text.contains(GeoGebraConstants.GGB_LICENSE_URL), text);
			assertFalse(text.contains(COMMERCIAL_MARKER), text);
		}
	}

	@Test
	void theAboutActionComposesTheRunningCopy() {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		String about = registry.aboutText();
		assertEquals(GeoCeDGAboutText.compose(app.getLocale().getLanguage(),
				GeoCeDGProductInfo.provenance(), GeoCeDGDistributionCondition.current()), about);
		assertTrue(about.startsWith(GeoCeDGProductInfo.applicationTitle() + "\n"), about);
		assertTrue(about.contains("development build"), about);
		assertFalse(about.contains("author-review implementation candidate"), about);
	}

	@Test
	void theLicensingSummaryStaysWithinItsAuthorities() throws IOException {
		String license = read("LICENSE");
		for (String fact : List.of("No single license", "GeoCeDG-authored software",
				"(EUPL-1.2)", "GeoCeDG-authored documentation", "(CC BY 4.0)",
				"Ordinary GeoCeDG-authored artwork is made available under CC BY 4.0",
				"permission to use the\nGeoCeDG name or official product marks",
				"GeoGebra-authored software source is governed by EUPL-1.2",
				"GeoGebra Language\nFiles", "GeoGebra Materials",
				"retain their own\nterms", "It is not licensed solely\nunder EUPL-1.2")) {
			assertTrue(license.contains(fact), "LICENSE no longer states: " + fact);
		}
		String notice = read("NOTICE.md");
		assertTrue(notice.contains("## Distribution condition"));
		assertTrue(notice.contains("INTERNAL_EVALUATION_ONLY.txt"));
		assertTrue(notice.contains("NC_DISTRIBUTION_NOTICE.txt"));
		String documentation = read("docs/LICENSE.md");
		assertTrue(documentation.contains("CC BY 4.0"));
		assertTrue(documentation.contains("This default does not relicense"));
		assertTrue(documentation.contains("trademark-policy.md"));
		assertTrue(read("docs/licensing/component-matrix.md").contains(
				"The complete binary is composite and cannot be labelled solely EUPL"));
		for (String route : List.of("LICENSE", "NOTICE.md", "THIRD_PARTY.md",
				"LICENSES/manifest.json", "LICENSES/EUPL-1.2.txt", "LICENSES/CC-BY-4.0.txt",
				"docs/LICENSE.md", "docs/licensing/component-matrix.md",
				"docs/licensing/trademark-policy.md",
				"packaging/windows/INTERNAL_EVALUATION_ONLY.txt",
				"packaging/windows/NC_DISTRIBUTION_NOTICE.txt")) {
			assertTrue(Files.isRegularFile(repositoryRoot().resolve(route)), route);
		}
	}

	// ------------------------------------------------------------------- PM-9

	@Test
	void classicHelpStillReachesTheInheritedAboutDialog() throws Exception {
		AppD classic = PreG9BR6PlusF3Fixtures.classic();
		Class<?> type = Class.forName("org.geogebra.desktop.gui.menubar.HelpMenuD");
		Constructor<?> constructor = type.getDeclaredConstructor(AppD.class);
		constructor.setAccessible(true);
		JMenu menu = (JMenu) constructor.newInstance(classic);
		for (String name : new String[] {"initActions", "initItems"}) {
			Method method = type.getDeclaredMethod(name);
			method.setAccessible(true);
			method.invoke(menu);
		}
		String aboutLabel = classic.getLocalization().getMenu("AboutLicense");
		JMenuItem about = null;
		for (Component component : menu.getMenuComponents()) {
			if (component instanceof JMenuItem item && aboutLabel.equals(item.getText())) {
				about = item;
			}
		}
		assertNotNull(about, "Classic Help keeps its inherited About entry");
		assertFalse(about.getText().contains("GeoCeDG"));
		JMenuItem entry = about;
		try (MockedStatic<GeoGebraMenuBar> bar = mockStatic(GeoGebraMenuBar.class)) {
			entry.getAction().actionPerformed(null);
			bar.verify(() -> GeoGebraMenuBar.showAboutDialog(classic), times(1));
		}
		assertFalse(classic.getVersionString().contains("GeoCeDG"));
		assertFalse(classic.getVersionString().contains(GeoCeDGProductInfo.semanticVersion()));
		for (String path : List.of(
				"source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/menubar/"
						+ "GeoGebraMenuBar.java",
				"source/desktop/desktop/src/main/java/org/geogebra/desktop/gui/menubar/"
						+ "HelpMenuD.java")) {
			String source = read(path);
			assertFalse(source.contains("org.geocedg"), path);
			assertFalse(source.contains("GeoCeDGProductInfo"), path);
		}
	}

	// ------------------------------------------------------------ help coverage

	@Test
	void bothGuidesCoverTheR6PlusCapabilitiesAtStructureLevel() throws IOException {
		String english = read("docs/user/geocedg_user_guide_en.md");
		String spanish = read("docs/user/geocedg_user_guide_es.md");
		for (String guide : List.of(english, spanish)) {
			String flowing = guide.replaceAll("\\s+", " ");
			assertEquals(1, count(guide, "(?m)^# "));
			assertEquals(16, count(guide, "(?m)^## "));
			assertEquals(87, count(guide, "(?m)^### "));
			assertEquals(17, count(guide, "geocedg-guide-section:"));
			for (String shared : List.of("0.4·t", "AutoCAD 2026", "Type 1", "Ghostscript",
					"Poppler", "Xpdf", "`SquarebyDiagonal`", "`SymmSymbol`", "EUPL-1.2",
					"CC BY 4.0", "INTERNAL EVALUATION — NOT FOR REDISTRIBUTION",
					"NON-COMMERCIAL DISTRIBUTION — PROFILE NC")) {
				assertTrue(flowing.contains(shared), shared);
			}
		}
		for (String token : List.of("**Text in PDF.**", "**Export Text as Shapes**",
				"**The GeoCeDG library.**", "numeric\nthickness field", "*distribution condition*",
				"*development\nbuild*", "listed in numeric order", "**Orientation cue** option")) {
			assertTrue(english.contains(token), "EN is missing " + token);
		}
		for (String token : List.of("**Texto en PDF.**", "**Exportar textos como\nfiguras**",
				"**La biblioteca GeoCeDG.**", "campo numérico de grosor",
				"*condición de distribución*", "*compilación de\ndesarrollo*",
				"se listan en orden numérico", "**Indicación de\norientación**")) {
			assertTrue(spanish.contains(token), "ES is missing " + token);
		}
		assertFalse(spanish.contains("hoja ISO A"), "ISO A terminology is lámina");
		assertFalse(spanish.contains("hojas ISO A"), "ISO A terminology is lámina");
	}

	// ------------------------------------------------------------------ helpers

	private static Consumer<Map<String, Object>> unchanged() {
		return values -> {
			// the base INTERNAL record unchanged
		};
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> app(Map<String, Object> values) {
		return (Map<String, Object>) values.get("application");
	}

	private static Map<String, Object> baseRecord(String profile) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("schema_version", 1);
		values.put("record_kind", "geocedg-distribution-record");
		values.put("profile_id", profile);
		values.put("status", "internal-evaluation");
		values.put("redistributable", false);
		values.put("marker", INTERNAL_MARKER);
		values.put("notice_file", "INTERNAL_EVALUATION_ONLY.txt");
		values.put("notice_sha256", null);
		Map<String, Object> application = new LinkedHashMap<>();
		application.put("name", "GeoCeDG");
		application.put("version", "9.8.7");
		application.put("version_date", "2001-02-03");
		values.put("application", application);
		values.put("source_revision", COMMIT);
		values.put("package_profile_sha256", DIGEST);
		return values;
	}

	private static Map<String, Object> record(String profile,
			Consumer<Map<String, Object>> change) {
		Map<String, Object> values = baseRecord(profile);
		change.accept(values);
		return values;
	}

	/** Writes app/ with the record and the notice it names; returns the launcher path. */
	private Path install(String name, Map<String, Object> values, String noticeText)
			throws IOException {
		Path root = temporary.resolve(name);
		Path app = root.resolve("app");
		Files.createDirectories(app);
		byte[] notice = noticeText.getBytes(StandardCharsets.UTF_8);
		Object noticeFile = values.get("notice_file");
		String noticeName = noticeFile instanceof String text && !text.contains("/")
				&& !"OTHER.txt".equals(text) ? text : "INTERNAL_EVALUATION_ONLY.txt";
		Files.write(app.resolve(noticeName), notice);
		if (values.containsKey("notice_sha256") && values.get("notice_sha256") == null) {
			values.put("notice_sha256", GeoCeDGDistributionCondition.sha256(notice));
		}
		Files.write(app.resolve(GeoCeDGDistributionCondition.RECORD_FILE),
				json(values).getBytes(StandardCharsets.UTF_8));
		return root.resolve("GeoCeDG.exe");
	}

	private Path installRaw(String name, byte[] record, String noticeText)
			throws IOException {
		Path root = temporary.resolve(name);
		Path app = root.resolve("app");
		Files.createDirectories(app);
		Files.write(app.resolve("INTERNAL_EVALUATION_ONLY.txt"),
				noticeText.getBytes(StandardCharsets.UTF_8));
		Files.write(app.resolve(GeoCeDGDistributionCondition.RECORD_FILE), record);
		return root.resolve("GeoCeDG.exe");
	}

	private static void assertUnverified(Path launcher, Expected expected, String why) {
		GeoCeDGDistributionCondition condition = GeoCeDGDistributionCondition.evaluate(
				launcher.toString(), expected);
		assertSame(State.UNVERIFIED, condition.getState(), why);
		assertTrue(condition.isPackaged(), why);
		assertNull(condition.getMarker(), why);
		assertNotNull(condition.getCause(), why);
	}

	private static GeoCeDGBuildProvenance provenance(String commit, RepositoryState state,
			String date, String digest, List<DistributionProfile> profiles) {
		return new GeoCeDGBuildProvenance("9.8.7", commit, state,
				ResolutionSource.GIT_HEAD, date, digest, profiles);
	}

	private static String json(Object value) {
		if (value instanceof Map<?, ?> map) {
			StringBuilder text = new StringBuilder("{");
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (text.length() > 1) {
					text.append(", ");
				}
				text.append(json(entry.getKey())).append(": ").append(json(entry.getValue()));
			}
			return text.append('}').toString();
		}
		if (value instanceof String text) {
			StringBuilder quoted = new StringBuilder("\"");
			for (char c : text.toCharArray()) {
				if (c == '"' || c == '\\') {
					quoted.append('\\');
				}
				quoted.append(c);
			}
			return quoted.append('"').toString();
		}
		return String.valueOf(value);
	}

	private static void assertOrder(String text, String... parts) {
		int from = 0;
		for (String part : parts) {
			int at = text.indexOf(part, from);
			assertTrue(at >= 0, "missing or out of order: " + part + "\n" + text);
			from = at + part.length();
		}
	}

	private static java.util.Properties generatedProvenance() throws IOException {
		java.util.Properties properties = new java.util.Properties();
		try (java.io.InputStream stream = GeoCeDGBuildProvenance.class.getResourceAsStream(
				"/org/geocedg/desktop/export/geocedg-build-provenance.properties")) {
			assertNotNull(stream, "generated provenance resource");
			properties.load(stream);
		}
		return properties;
	}

	/** The documented canonicalization: CRLF and lone CR become LF, then SHA-256. */
	private static String canonicalSha256(byte[] bytes) {
		java.io.ByteArrayOutputStream canonical = new java.io.ByteArrayOutputStream();
		for (int index = 0; index < bytes.length; index++) {
			if (bytes[index] == '\r') {
				canonical.write('\n');
				if (index + 1 < bytes.length && bytes[index + 1] == '\n') {
					index++;
				}
			} else {
				canonical.write(bytes[index]);
			}
		}
		return GeoCeDGDistributionCondition.sha256(canonical.toByteArray());
	}

	private static int count(String text, String regex) {
		return (int) java.util.regex.Pattern.compile(regex).matcher(text).results().count();
	}

	private static String read(String path) throws IOException {
		return Files.readString(repositoryRoot().resolve(path), StandardCharsets.UTF_8)
				.replace("\r\n", "\n");
	}

	static Path repositoryRoot() {
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
