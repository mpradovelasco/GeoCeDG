/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R5-A focal contracts for the derived guide outline and its anchors.
 *
 * <p>The outline comes only from the packaged Markdown of each edition, in the
 * same parse that renders the document. Level-1 and level-2 headings take the
 * identifier of their stable section marker, level-3 headings take the enclosing
 * marker identifier and their numbering token, and translated heading text never
 * contributes to identity. Anchor emission is additive: heading tags, their
 * counts and the text a reader sees are unchanged.
 */
class PreG9BR5AGuideOutlineTest {

	private static final String[] LANGUAGES = {"en", "es"};
	private static final Pattern MARKER =
			Pattern.compile("<!-- geocedg-guide-section: ([a-z0-9-]+) -->");
	private static final Pattern NAMED_ANCHOR = Pattern.compile("<a name=\"([^\"]+)\">");

	// ---------------------------------------------------------- real editions

	@Test
	void everyHeadingOfLevelOneToThreeIsANavigationEntryInBothEditions()
			throws IOException {
		for (String language : LANGUAGES) {
			List<GeoCeDGGuideOutline.Entry> outline = outline(language);
			assertEquals(101, outline.size(), language);
			assertEquals(1, count(outline, 1), language);
			assertEquals(16, count(outline, 2), language);
			assertEquals(84, count(outline, 3), language);
		}
	}

	@Test
	void theEnglishAndSpanishStructuralVectorsAreEqualElementForElement()
			throws IOException {
		List<String> english = vector(outline("en"));
		List<String> spanish = vector(outline("es"));
		assertEquals(english.size(), spanish.size());
		for (int index = 0; index < english.size(); index++) {
			assertEquals(english.get(index), spanish.get(index),
					"structure diverged at entry " + index);
		}
		assertEquals(anchors(outline("en")), anchors(outline("es")),
				"the editions derived different navigation identities");
	}

	@Test
	void derivedAnchorsAreUniqueWithinEachEdition() throws IOException {
		for (String language : LANGUAGES) {
			List<String> anchors = anchors(outline(language));
			assertEquals(anchors.size(), new HashSet<>(anchors).size(), language);
		}
	}

	@Test
	void theTitleTakesTheFirstMarkerIdentifierAndNoSyntheticRootExists()
			throws IOException {
		for (String language : LANGUAGES) {
			List<GeoCeDGGuideOutline.Entry> outline = outline(language);
			GeoCeDGGuideOutline.Entry title = outline.get(0);
			assertEquals(1, title.level(), language);
			assertEquals("about-this-guide", title.anchor(), language);
			assertEquals("about-this-guide", title.sectionId(), language);
			assertEquals(markers(language).get(0), title.anchor(), language);
			assertEquals("", title.numbering(), language);
			assertNull(title.parentAnchor(), language);
			// The title is the only entry without a parent.
			assertEquals(1, outline.stream()
					.filter(entry -> entry.parentAnchor() == null).count(), language);
		}
	}

	@Test
	void levelTwoHeadingsTakeTheIdentifierOfTheirOwnMarker() throws IOException {
		for (String language : LANGUAGES) {
			List<String> sections = outline(language).stream()
					.filter(entry -> entry.level() <= 2)
					.map(GeoCeDGGuideOutline.Entry::anchor).collect(Collectors.toList());
			// 17 markers: the first on the title, one on each of the 16 sections.
			assertEquals(markers(language), sections, language);
			GeoCeDGGuideOutline.Entry spline = entry(outline(language), "spline-v2");
			assertEquals(2, spline.level(), language);
			assertEquals("7", spline.numbering(), language);
			assertEquals("about-this-guide", spline.parentAnchor(), language);
		}
	}

	@Test
	void levelThreeHeadingsTakeTheEnclosingMarkerAndTheirNumberingToken()
			throws IOException {
		for (String language : LANGUAGES) {
			for (GeoCeDGGuideOutline.Entry entry : outline(language)) {
				if (entry.level() != 3) {
					continue;
				}
				assertTrue(entry.numbering().matches("\\d+\\.\\d+"),
						language + " " + entry.anchor());
				assertEquals(entry.sectionId() + "/" + entry.numbering(), entry.anchor());
				assertEquals(entry.sectionId(), entry.parentAnchor(),
						"a level-3 entry belongs to its enclosing section");
			}
			GeoCeDGGuideOutline.Entry creating =
					entry(outline(language), "spline-v2/7.2");
			assertEquals("spline-v2", creating.sectionId(), language);
			assertTrue(outline(language).stream().anyMatch(
					entry -> entry.anchor().equals("command-and-workflow-reference/16.4")),
					language);
		}
	}

	@Test
	void translatedHeadingTextIsDisplayOnlyAndNeverIdentity() throws IOException {
		Map<String, String> english = labels(outline("en"));
		Map<String, String> spanish = labels(outline("es"));
		assertEquals(english.keySet(), spanish.keySet());
		// Same identity, localized labels.
		assertEquals("1. What is GeoCeDG", english.get("what-is-geocedg"));
		assertEquals("1. Qué es GeoCeDG", spanish.get("what-is-geocedg"));
		assertEquals("7.2 Creating a Spline V2", english.get("spline-v2/7.2"));
		assertEquals("7.2 Crear una Spline V2", spanish.get("spline-v2/7.2"));
		long localized = english.keySet().stream()
				.filter(anchor -> !english.get(anchor).equals(spanish.get(anchor))).count();
		assertTrue(localized > 80, "most labels must be localized, got " + localized);

		// Rewriting every heading text leaves every identity unchanged.
		String fixture = String.join("\n",
				"<!-- geocedg-guide-section: overview -->",
				"# Title",
				"<!-- geocedg-guide-section: drawing -->",
				"## 1. Drawing",
				"### 1.1 Points");
		String renamed = fixture.replace("# Title", "# Titre")
				.replace("1. Drawing", "1. Dessin").replace("1.1 Points", "1.1 Des points");
		assertEquals(anchors(render(fixture)), anchors(render(renamed)));
		assertEquals(List.of("overview", "drawing", "drawing/1.1"), anchors(render(renamed)));
		assertNotEquals(labels(render(fixture)), labels(render(renamed)));
	}

	@Test
	void labelsAreTheReaderTextOfTheHeadingWithoutMarkup() throws IOException {
		assertEquals("11.4 Spline V2 and the SPLINE entity",
				labels(outline("en")).get("dxf-export/11.4"));
		assertEquals("11.4 Spline V2 y la entidad SPLINE",
				labels(outline("es")).get("dxf-export/11.4"));
		assertEquals("GeoCeDG user guide", labels(outline("en")).get("about-this-guide"));
		assertEquals("Guía de usuario de GeoCeDG",
				labels(outline("es")).get("about-this-guide"));
	}

	@Test
	void theOutlineFollowsDocumentOrderAndHeadingHierarchy() throws IOException {
		for (String language : LANGUAGES) {
			List<GeoCeDGGuideOutline.Entry> outline = outline(language);
			List<Integer> levels = outline.stream().map(GeoCeDGGuideOutline.Entry::level)
					.collect(Collectors.toList());
			assertEquals(sourceHeadingLevels(GeoCeDGActionRegistry.readUserGuide(language)),
					levels, language);
			Set<String> seen = new HashSet<>();
			for (GeoCeDGGuideOutline.Entry entry : outline) {
				if (entry.parentAnchor() != null) {
					// A parent always precedes its children and is one level up.
					assertTrue(seen.contains(entry.parentAnchor()), entry.anchor());
					GeoCeDGGuideOutline.Entry parent = entry(outline, entry.parentAnchor());
					assertEquals(entry.level() - 1, parent.level(), entry.anchor());
				}
				seen.add(entry.anchor());
			}
		}
	}

	// -------------------------------------------------------- renderer contract

	@Test
	void everyEntryIsANamedAnchorInsideItsHeadingAndNeverALink() throws IOException {
		for (String language : LANGUAGES) {
			GeoCeDGGuideRenderer.Rendering rendering = rendering(language);
			List<String> named = new ArrayList<>();
			Matcher matcher = NAMED_ANCHOR.matcher(rendering.html());
			while (matcher.find()) {
				named.add(matcher.group(1));
			}
			assertEquals(anchors(rendering.outline()), named, language);
			assertTrue(rendering.html().contains(
					"<h2><a name=\"spline-v2\">7. Spline V2</a></h2>"), language);
			assertTrue(rendering.html().contains("<h3><a name=\"spline-v2/7.2\">"), language);
			assertFalse(rendering.html().contains("href="), language);
		}
	}

	@Test
	void anchorEmissionKeepsHeadingTagsAndTheirCounts() throws IOException {
		for (String language : LANGUAGES) {
			String html = GeoCeDGGuideRenderer.toHtml(
					GeoCeDGActionRegistry.readUserGuide(language));
			assertEquals(1, occurrences(html, "<h1>"), language);
			assertEquals(16, occurrences(html, "<h2>"), language);
			assertEquals(84, occurrences(html, "<h3>"), language);
			assertEquals(0, occurrences(html, "<h1 "), language);
			assertEquals(0, occurrences(html, "<h2 "), language);
			assertEquals(0, occurrences(html, "<h3 "), language);
			assertFalse(html.contains("geocedg-guide-section"), language);
		}
	}

	@Test
	void anchorsAddNoCharacterToTheTextAReaderSees() throws Exception {
		for (String language : LANGUAGES) {
			String html = GeoCeDGGuideRenderer.toHtml(
					GeoCeDGActionRegistry.readUserGuide(language));
			// The guides contain no link, so every closing anchor is a navigation one.
			String withoutAnchors = html.replaceAll("<a name=\"[^\"]*\">", "")
					.replace("</a>", "");
			assertEquals(swingText(withoutAnchors), swingText(html), language);
		}
	}

	@Test
	void toHtmlIsTheDocumentOfTheSingleRenderPass() throws IOException {
		for (String language : LANGUAGES) {
			String markdown = GeoCeDGActionRegistry.readUserGuide(language);
			assertEquals(GeoCeDGGuideRenderer.render(markdown).html(),
					GeoCeDGGuideRenderer.toHtml(markdown), language);
		}
	}

	@Test
	void anchorsAreComputedAndNeverAuthoredInTheGuideSources() throws IOException {
		for (String language : LANGUAGES) {
			String source = GeoCeDGActionRegistry.readUserGuide(language);
			assertFalse(source.contains("<a "), language + " contains anchor markup");
			assertFalse(source.contains("]("), language + " contains a Markdown link");
			for (GeoCeDGGuideOutline.Entry entry : outline(language)) {
				if (entry.level() == 3) {
					assertFalse(source.contains(entry.anchor()),
							language + " authors the derived anchor " + entry.anchor());
				}
			}
		}
		// No separately packaged outline or index exists beside the guides.
		for (String name : new String[] {"geocedg_user_guide_en.json",
				"geocedg_user_guide_es.json", "geocedg_user_guide_outline.json",
				"geocedg_user_guide_outline.yml", "geocedg_user_guide_toc.md"}) {
			assertNull(getClass().getResource("/org/geocedg/desktop/" + name), name);
		}
	}

	// ------------------------------------------------------- negative fixtures

	@Test
	void aHeadingWithoutAMarkerHasNoIdentityAndNeitherHaveItsSubsections() {
		List<GeoCeDGGuideOutline.Entry> outline = render(String.join("\n",
				"<!-- geocedg-guide-section: overview -->",
				"# Title",
				"## 1. Unmarked",
				"### 1.1 Inside",
				"<!-- geocedg-guide-section: marked -->",
				"## 2. Marked",
				"### 2.1 Inside"));
		assertEquals(List.of("overview", "marked", "marked/2.1"), anchors(outline));
		assertFalse(GeoCeDGGuideRenderer.toHtml("## 1. Unmarked").contains("<a "));
	}

	@Test
	void aMarkerThatIsNotImmediatelyFollowedByTheHeadingIsIgnored() {
		List<GeoCeDGGuideOutline.Entry> outline = render(String.join("\n",
				"<!-- geocedg-guide-section: overview -->",
				"# Title",
				"<!-- geocedg-guide-section: misplaced -->",
				"",
				"## 1. Separated by a blank line",
				"<!-- geocedg-guide-section: paragraph -->",
				"A paragraph takes the marker's place.",
				"## 2. After the paragraph"));
		assertEquals(List.of("overview"), anchors(outline));
	}

	@Test
	void aLevelThreeHeadingWithoutANumberingTokenIsNoEntry() {
		List<GeoCeDGGuideOutline.Entry> outline = render(String.join("\n",
				"<!-- geocedg-guide-section: overview -->",
				"# Title",
				"### Unnumbered",
				"### 0.1 Numbered"));
		assertEquals(List.of("overview", "overview/0.1"), anchors(outline));
	}

	@Test
	void aDuplicateDerivedAnchorNeverShadowsTheFirstOne() {
		GeoCeDGGuideRenderer.Rendering rendering = GeoCeDGGuideRenderer.render(
				String.join("\n",
						"<!-- geocedg-guide-section: overview -->",
						"# Title",
						"### 1.1 First",
						"### 1.1 Repeated number"));
		assertEquals(List.of("overview", "overview/1.1"), anchors(rendering.outline()));
		assertEquals("1.1 First", rendering.outline().get(1).label());
		assertEquals(1, occurrences(rendering.html(), "<a name=\"overview/1.1\">"));
		assertTrue(rendering.html().contains("<h3>1.1 Repeated number</h3>"),
				rendering.html());
	}

	@Test
	void deeperHeadingsAndFencedMarkersAreNotNavigationEntries() {
		List<GeoCeDGGuideOutline.Entry> outline = render(String.join("\n",
				"<!-- geocedg-guide-section: overview -->",
				"# Title",
				"#### 0.1.1 Too deep",
				"```text",
				"<!-- geocedg-guide-section: fenced -->",
				"## 9. Inside a fence",
				"```"));
		assertEquals(List.of("overview"), anchors(outline));
	}

	@Test
	void numberingTokensAreStructuralPrefixesOnly() {
		assertEquals("7", GeoCeDGGuideOutline.numbering("7. Spline V2"));
		assertEquals("7.2", GeoCeDGGuideOutline.numbering("7.2 Creating a Spline V2"));
		assertEquals("11.10", GeoCeDGGuideOutline.numbering("11.10 Viewport independence"));
		assertEquals("", GeoCeDGGuideOutline.numbering("GeoCeDG user guide"));
		assertEquals("", GeoCeDGGuideOutline.numbering("Spline V2 7.2"));
		assertNull(GeoCeDGGuideOutline.sectionMarker(" <!-- geocedg-guide-section: x -->"));
		assertEquals("x", GeoCeDGGuideOutline.sectionMarker(
				"<!-- geocedg-guide-section: x -->"));
	}

	// ------------------------------------------------------------------ helpers

	private static GeoCeDGGuideRenderer.Rendering rendering(String language)
			throws IOException {
		return GeoCeDGGuideRenderer.render(GeoCeDGActionRegistry.readUserGuide(language));
	}

	private static List<GeoCeDGGuideOutline.Entry> outline(String language)
			throws IOException {
		return rendering(language).outline();
	}

	private static List<GeoCeDGGuideOutline.Entry> render(String markdown) {
		return GeoCeDGGuideRenderer.render(markdown).outline();
	}

	/** Fence-aware levels of the level-1 to level-3 headings of a source. */
	private static List<Integer> sourceHeadingLevels(String source) {
		List<Integer> levels = new ArrayList<>();
		boolean fenced = false;
		for (String line : source.replace("\r\n", "\n").split("\n", -1)) {
			if (line.startsWith("```")) {
				fenced = !fenced;
			} else if (!fenced && line.matches("#{1,3} .*")) {
				levels.add(line.indexOf(' '));
			}
		}
		return levels;
	}

	private static List<String> markers(String language) throws IOException {
		List<String> ids = new ArrayList<>();
		Matcher matcher = MARKER.matcher(GeoCeDGActionRegistry.readUserGuide(language));
		while (matcher.find()) {
			ids.add(matcher.group(1));
		}
		return ids;
	}

	private static List<String> vector(List<GeoCeDGGuideOutline.Entry> outline) {
		return outline.stream().map(entry -> entry.level() + "|" + entry.numbering()
				+ "|" + entry.sectionId()).collect(Collectors.toList());
	}

	private static List<String> anchors(List<GeoCeDGGuideOutline.Entry> outline) {
		return outline.stream().map(GeoCeDGGuideOutline.Entry::anchor)
				.collect(Collectors.toList());
	}

	private static Map<String, String> labels(List<GeoCeDGGuideOutline.Entry> outline) {
		return outline.stream().collect(Collectors.toMap(
				GeoCeDGGuideOutline.Entry::anchor, GeoCeDGGuideOutline.Entry::label));
	}

	private static GeoCeDGGuideOutline.Entry entry(List<GeoCeDGGuideOutline.Entry> outline,
			String anchor) {
		Map<String, GeoCeDGGuideOutline.Entry> byAnchor = outline.stream().collect(
				Collectors.toMap(GeoCeDGGuideOutline.Entry::anchor, Function.identity()));
		assertTrue(byAnchor.containsKey(anchor), anchor);
		return byAnchor.get(anchor);
	}

	private static long count(List<GeoCeDGGuideOutline.Entry> outline, int level) {
		return outline.stream().filter(entry -> entry.level() == level).count();
	}

	private static String swingText(String html) throws BadLocationException {
		JEditorPane pane = GeoCeDGGuideWindow.createView(null);
		pane.setText(html);
		Document document = pane.getDocument();
		return document.getText(0, document.getLength());
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
}
