/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * POST-P1-DOC-HELP focused contracts: the two official bilingual user guides
 * exist, are packaged from their single tracked sources, are selected by the
 * active product language with an English fallback, keep structural parity, and
 * carry no wording that is known to be stale at this baseline.
 *
 * <p>This track changes documentation, the help selection seam and packaged
 * resources only. It changes no kernel, geometry, dependency graph,
 * serialization, semantic identity or Classic behaviour.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PostP1BilingualUserGuideTest {

	private static final String EN = "geocedg_user_guide_en.md";
	private static final String ES = "geocedg_user_guide_es.md";
	private static final Pattern SECTION =
			Pattern.compile("<!-- geocedg-guide-section: ([a-z0-9-]+) -->");

	/** Wording that must never reappear in a live user-facing document. */
	private static final List<String> STALE = List.of(
			"--enableLocusV2=true",
			"--enableExtendedDxf=true",
			"P1 pending",
			"P0 pending",
			"PUBLIC REDISTRIBUTION = BLOCKED PENDING LICENSE/ASSET APPROVAL",
			"IMPLEMENTATION CANDIDATE");

	// ------------------------------------------------------- existence and parity

	@Test
	void bothOfficialGuidesExistAndAreSubstantial() throws IOException {
		for (String guide : List.of(EN, ES)) {
			String text = read("docs/user/" + guide);
			assertTrue(text.length() > 20000, guide + " is too short to be the guide");
		}
	}

	@Test
	void bothEditionsDeclareTheSameStableSectionIdentifiers() throws IOException {
		List<String> english = sectionIds(read("docs/user/" + EN));
		List<String> spanish = sectionIds(read("docs/user/" + ES));
		assertEquals(17, english.size(), "unexpected section count");
		assertEquals(english, spanish,
				"the two editions diverged in section identity or order");
		// Visible numbering is never the identity; the markers are.
		assertTrue(english.contains("locus-v2"));
		assertTrue(english.contains("spline-v2"));
		assertTrue(english.contains("known-limitations"));
		assertTrue(english.contains("command-and-workflow-reference"));
	}

	@Test
	void bothEditionsCarryTheSameCommandsAndWorkedExamples() throws IOException {
		String english = read("docs/user/" + EN);
		String spanish = read("docs/user/" + ES);
		for (String token : List.of(
				"LocusV2(G,u,dom)",
				"dom={false,{0,4,true,true}}",
				"U=Point(L,\"generator.main\",1)",
				"SplineV2({A,B,C,D},3)",
				"P=Point(S,\"spline-v2/main\",0.25)",
				"M=Length(S)",
				"MP=Length(S,P,Q)",
				"c=Circle((0,0),1)",
				"R=Intersect(S,c)",
				"T=Dilate(S,k,O)",
				"LocusLength(",
				"Intersect( <Rich Intersection Result>, <Solution Token> )",
				"Point( <Locus V2>, <Branch Key>, <Canonical Parameter> )")) {
			assertTrue(english.contains(token), "EN is missing " + token);
			assertTrue(spanish.contains(token), "ES is missing " + token);
		}
	}

	@Test
	void bothEditionsStateTheSameCurrentLimitations() throws IOException {
		String english = read("docs/user/" + EN);
		String spanish = read("docs/user/" + ES);
		// The metric/spline-pair endpoint debt must appear in both editions.
		assertTrue(english.contains("Spline V2 × Spline V2"));
		assertTrue(spanish.contains("Spline V2 × Spline V2"));
		// The DXF boundary is stated identically, as a verbatim contract block.
		String dxf = "DXF SPLINE exact entity      = NOT IMPLEMENTED";
		assertTrue(english.contains(dxf));
		assertTrue(spanish.contains(dxf));
		// The approximation guarantee must not be overstated in either edition.
		assertTrue(english.contains("not a certified global error bound"));
		assertTrue(spanish.contains("una cota de error global certificada"));
		// Experimental maturity survives the default-on promotion.
		assertTrue(english.contains("experimental"));
		assertTrue(spanish.contains("experimental"));
	}

	// -------------------------------------------------------------- stale wording

	@Test
	void noKnownStaleWordingSurvivesInAnyLiveUserDocument() throws IOException {
		for (String path : List.of("docs/user/" + EN, "docs/user/" + ES,
				"docs/user/geocedg_user_guide.md",
				"docs/user/geocedg_construction_quick_guide.md")) {
			String document = read(path);
			for (String stale : STALE) {
				assertFalse(document.contains(stale), path + " still contains " + stale);
			}
		}
	}

	@Test
	void guidesNeverPresentCompatibilityInputAsTheNativeFormat() throws IOException {
		for (String guide : List.of(EN, ES)) {
			String text = read("docs/user/" + guide);
			assertTrue(text.contains(".cedg"), guide);
			assertTrue(text.contains(".ggb"), guide);
			assertFalse(text.contains("native `.ggb`"), guide);
			assertFalse(text.contains("`.ggb` nativo"), guide);
		}
	}

	// --------------------------------------------------------- packaged resources

	@Test
	void bothGuidesArePackagedAsByteCopiesOfTheirTrackedSources() throws IOException {
		for (String guide : List.of(EN, ES)) {
			byte[] source = Files.readAllBytes(
					repositoryRoot().resolve("docs/user/" + guide));
			try (var stream = getClass().getResourceAsStream(guide)) {
				assertNotNull(stream, guide + " is not packaged");
				assertArrayEquals(source, stream.readAllBytes(),
						guide + " diverged from its single tracked source");
			}
		}
	}

	@Test
	void theSupersededGuidesAreNoLongerPackaged() {
		// A second, divergent packaged manual would reintroduce the problem this
		// track removes.
		assertNull(getClass().getResource(
				"/org/geocedg/desktop/geocedg_construction_quick_guide.md"));
		assertNull(getClass().getResource(
				"/org/geocedg/desktop/geocedg_user_guide.md"));
	}

	@Test
	void processResourcesPackagesExactlyTheTwoTrackedGuides() throws IOException {
		String build = read("source/desktop/desktop/build.gradle.kts");
		assertTrue(build.contains("../../docs/user/geocedg_user_guide_en.md"));
		assertTrue(build.contains("../../docs/user/geocedg_user_guide_es.md"));
		// The packaged copy must come from the versioned source, never from a
		// second hand-edited copy under src/main/resources.
		assertFalse(Files.exists(repositoryRoot().resolve(
				"source/desktop/desktop/src/main/resources/org/geocedg/desktop/"
						+ EN)));
		assertFalse(Files.exists(repositoryRoot().resolve(
				"source/desktop/desktop/src/main/resources/org/geocedg/desktop/"
						+ ES)));
	}

	// ------------------------------------------------------------ help selection

	@Test
	void helpSelectsTheEditionOfTheActiveProductLanguage() {
		assertEquals("/org/geocedg/desktop/" + ES,
				GeoCeDGActionRegistry.userGuideResource("es"));
		assertEquals("/org/geocedg/desktop/" + EN,
				GeoCeDGActionRegistry.userGuideResource("en"));
	}

	@Test
	void helpFallsBackToEnglishForAnyOtherLanguage() {
		for (String language : new String[] {"fr", "de", "", "ES", null}) {
			assertEquals("/org/geocedg/desktop/" + EN,
					GeoCeDGActionRegistry.userGuideResource(language),
					"fallback failed for " + language);
		}
	}

	@Test
	void everySelectedResourceResolvesToTheDocumentOfThatLanguage() throws IOException {
		// language -> expected first heading of the resolved edition
		Map<String, String> expected = Map.of(
				"en", "# GeoCeDG user guide",
				"es", "# Guía de usuario de GeoCeDG",
				"fr", "# GeoCeDG user guide");
		for (Map.Entry<String, String> entry : expected.entrySet()) {
			String resource = GeoCeDGActionRegistry.userGuideResource(entry.getKey());
			try (var stream = getClass().getResourceAsStream(resource)) {
				assertNotNull(stream, resource);
				String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
				assertTrue(text.contains("geocedg-guide-section: about-this-guide"),
						resource);
				assertTrue(text.contains(entry.getValue()),
						entry.getKey() + " resolved to the wrong edition");
				// Fenced examples must survive packaging intact.
				assertTrue(text.contains("```text\nh=0\n"), resource);
			}
		}
	}

	@Test
	void helpNoLongerDependsOnTheSupersededQuickGuide() throws IOException {
		String registry = read("source/desktop/desktop/src/main/java/org/geocedg/"
				+ "desktop/GeoCeDGActionRegistry.java");
		assertFalse(registry.contains("geocedg_construction_quick_guide"),
				"Help still reads the superseded quick guide");
		assertTrue(registry.contains("userGuideResource"));
	}

	// ------------------------------------------------------- unchanged boundaries

	@Test
	void theSupersededPathsRemainAsIndexOrLegacyMarkersOnly() throws IOException {
		String index = read("docs/user/geocedg_user_guide.md");
		assertTrue(index.contains("geocedg_user_guide_en.md"));
		assertTrue(index.contains("geocedg_user_guide_es.md"));
		// A third full manual at this path is exactly what must not return.
		assertTrue(index.length() < 4000, "the index grew back into a manual");

		String legacy = read("docs/user/geocedg_construction_quick_guide.md");
		assertTrue(legacy.toUpperCase(Locale.ROOT).contains("SUPERSEDED"));
		assertTrue(legacy.length() < 4000, "the quick guide grew back into a manual");
	}

	@Test
	void thisTrackChangesNoClassicOrPersistenceContract() throws IOException {
		// Classic containment and the document policy are stated by their own
		// authorities; this track must leave both untouched.
		String profile = read("apps/geocedg/application-profile.yml");
		assertTrue(profile.contains("\"native_extension\": \".cedg\""));
		assertTrue(profile.contains("\"compatibility_extension\": \".ggb\""));
		assertTrue(profile.contains("\"app_code\": \"classic\""));
		assertTrue(profile.contains("\"classic_policy\":"
				+ " \"retain-upstream-language-selection\""));
		// The guide action keeps its existing catalog binding and labels.
		assertTrue(profile.contains("\"symbol\": \"geocedg.help.user-guide\""));
		assertEquals("GeoCeDG user guide",
				GeoCeDGProfile.getText("geocedg.help.UserGuide.name", "en"));
		assertEquals("Guía de usuario de GeoCeDG",
				GeoCeDGProfile.getText("geocedg.help.UserGuide.name", "es"));
	}

	// -------------------------------------------------------------------- helpers

	private static List<String> sectionIds(String document) {
		List<String> ids = new ArrayList<>();
		Matcher matcher = SECTION.matcher(document);
		while (matcher.find()) {
			ids.add(matcher.group(1));
		}
		return ids;
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
