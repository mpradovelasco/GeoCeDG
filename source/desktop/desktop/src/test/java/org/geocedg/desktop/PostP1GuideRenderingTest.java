/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * POST-P1-DOC-HELP corrective contracts for the guide presentation: the tracked
 * Markdown stays the single source, the bounded renderer turns it into rich
 * content instead of visible source markup, and the viewer is a read-only,
 * resizable window whose default size never follows the content.
 *
 * <p>Appearance is deliberately left to the author smoke. These cases pin the
 * conversion, the configuration and the size contract, not pixel geometry.
 */
class PostP1GuideRenderingTest {

	// ------------------------------------------------------------- source input

	@Test
	void bothMarkdownSourcesAreFoundThroughTheLanguageContract() throws IOException {
		assertTrue(GeoCeDGActionRegistry.readUserGuide("en")
				.contains("# GeoCeDG user guide"));
		assertTrue(GeoCeDGActionRegistry.readUserGuide("es")
				.contains("# Guía de usuario de GeoCeDG"));
		// The fallback resolves to a real packaged document, not to an error.
		assertTrue(GeoCeDGActionRegistry.readUserGuide("fr")
				.contains("# GeoCeDG user guide"));
	}

	@Test
	void theRendererIsIndependentOfTheLanguageOfItsInput() throws IOException {
		for (String language : new String[] {"en", "es"}) {
			String html = GeoCeDGGuideRenderer.toHtml(
					GeoCeDGActionRegistry.readUserGuide(language));
			assertTrue(html.startsWith("<html><body>"), language);
			assertTrue(html.endsWith("</body></html>"), language);
			assertTrue(html.contains("<h1>"), language);
			assertTrue(html.contains("<table>"), language);
			assertTrue(html.contains("<pre>"), language);
		}
	}

	// ---------------------------------------------------------- rendered output

	@Test
	void headingsBecomeStructureInsteadOfVisibleHashes() throws IOException {
		for (String language : new String[] {"en", "es"}) {
			String html = GeoCeDGGuideRenderer.toHtml(
					GeoCeDGActionRegistry.readUserGuide(language));
			assertTrue(html.contains("<h2>"), language);
			assertTrue(html.contains("<h3>"), language);
			// No ATX marker may survive at the start of a rendered block.
			assertFalse(html.contains(">#"), language);
			assertFalse(html.contains("<p># "), language);
		}
	}

	@Test
	void fencedCodeBecomesPreformattedWithoutBackticks() throws IOException {
		for (String language : new String[] {"en", "es"}) {
			String html = GeoCeDGGuideRenderer.toHtml(
					GeoCeDGActionRegistry.readUserGuide(language));
			assertTrue(html.contains("<pre>"), language);
			// No fence marker and no code-span marker may reach the reader.
			assertFalse(html.contains("```"), language);
			assertFalse(html.contains("`"), language);
		}
	}

	@Test
	void sectionIdentifierCommentsAreNeverVisible() throws IOException {
		for (String language : new String[] {"en", "es"}) {
			String source = GeoCeDGActionRegistry.readUserGuide(language);
			assertTrue(source.contains("<!-- geocedg-guide-section:"), language);
			String html = GeoCeDGGuideRenderer.toHtml(source);
			assertFalse(html.contains("geocedg-guide-section"), language);
			assertFalse(html.contains("&lt;!--"), language);
		}
	}

	@Test
	void aRepresentativeTableBecomesStructuredRows() {
		String html = GeoCeDGGuideRenderer.toHtml(String.join("\n",
				"| Theme | Character |",
				"|---|---|",
				"| **Original** | The historical appearance. |",
				"| Scientific Paper | A warm palette. |"));
		assertTrue(html.contains("<table>"));
		assertTrue(html.contains("<th>Theme</th><th>Character</th>"));
		assertTrue(html.contains("<td><b>Original</b></td>"));
		assertTrue(html.contains("<td>A warm palette.</td>"));
		// The delimiter row is structure, never a visible row.
		assertFalse(html.contains("---"));
		assertEquals(3, occurrences(html, "<tr>"));
	}

	@Test
	void inlineCodeIsDistinguishedAndItsContentIsEscaped() {
		String html = GeoCeDGGuideRenderer.toHtml(
				"Use `Point(S,\"spline-v2/main\",0.25)` and `<Locus V2>` here.");
		assertTrue(html.contains("<code>Point(S,&quot;spline-v2/main&quot;,0.25)</code>")
				|| html.contains("<code>Point(S,\"spline-v2/main\",0.25)</code>"), html);
		// Angle brackets inside code must not be parsed as markup.
		assertTrue(html.contains("<code>&lt;Locus V2&gt;</code>"), html);
	}

	@Test
	void emphasisStrongAndListsRenderAsStructure() {
		String html = GeoCeDGGuideRenderer.toHtml(String.join("\n",
				"- **Constructive traceability.** Every derived object stays linked to",
				"  its defining inputs.",
				"- The *x*-axis is emphasised.",
				"",
				"1. First step.",
				"2. Second step."));
		assertTrue(html.contains("<ul>"), html);
		assertTrue(html.contains("<ol>"), html);
		assertTrue(html.contains("<b>Constructive traceability.</b>"), html);
		assertTrue(html.contains("<i>x</i>-axis"), html);
		// The wrapped continuation joins its own item instead of starting a block.
		assertTrue(html.contains("its defining inputs.</li>"), html);
		assertEquals(4, occurrences(html, "<li>"));
	}

	@Test
	void fencedAngleBracketSyntaxSurvivesAsLiteralText() throws IOException {
		String html = GeoCeDGGuideRenderer.toHtml(
				GeoCeDGActionRegistry.readUserGuide("en"));
		// The command syntax summary must arrive intact, not swallowed as markup.
		assertTrue(html.contains(
				"Point( &lt;Locus V2&gt;, &lt;Branch Key&gt;, &lt;Canonical Parameter&gt; )"),
				"the syntax summary lost its angle brackets");
		assertTrue(html.contains(
				"DXF SPLINE exact entity      = NOT IMPLEMENTED"));
	}

	@Test
	void anInlineSpanMayStraddleASourceLineBreakInsideAListItem() {
		// Regression: a list item is one logical text, so a bold span that the
		// source wraps across two lines still closes instead of leaking markers.
		String html = GeoCeDGGuideRenderer.toHtml(String.join("\n",
				"- **Protocolo** muestra la construcción. **Vista → Mostrar barra de",
				"  navegación de la construcción** activa el control por pasos."));
		assertFalse(html.contains("**"), html);
		assertTrue(html.contains("<b>Vista → Mostrar barra de navegación"
				+ " de la construcción</b>"), html);
	}

	@Test
	void neitherEditionLeavesAnyVisibleSourceMarkerAfterRendering()
			throws IOException {
		for (String language : new String[] {"en", "es"}) {
			String html = GeoCeDGGuideRenderer.toHtml(
					GeoCeDGActionRegistry.readUserGuide(language));
			assertFalse(html.contains("**"), language + " leaked a strong marker");
			assertFalse(html.contains("`"), language + " leaked a code marker");
			assertFalse(html.contains("<p>|"), language + " leaked a table row");
			assertFalse(html.contains("<p># "), language + " leaked a heading");
		}
	}

	@Test
	void bothEditionsRenderTheSameDocumentStructure() throws IOException {
		String english = GeoCeDGGuideRenderer.toHtml(
				GeoCeDGActionRegistry.readUserGuide("en"));
		String spanish = GeoCeDGGuideRenderer.toHtml(
				GeoCeDGActionRegistry.readUserGuide("es"));
		// Semantic parity is visible in the rendered structure, not only in source.
		for (String tag : new String[] {"<h1>", "<h2>", "<h3>", "<table>", "<pre>",
				"<li>", "<hr>"}) {
			assertEquals(occurrences(english, tag), occurrences(spanish, tag),
					"the editions diverged in " + tag);
		}
	}

	@Test
	void unauditedMarkupDegradesToTextInsteadOfBrokenLayout() {
		String html = GeoCeDGGuideRenderer.toHtml("An unbalanced **marker stays literal.");
		assertFalse(html.contains("<b>"), html);
		assertTrue(html.contains("**marker"), html);
	}

	// ------------------------------------------------------------------- viewer

	@Test
	void theViewIsAReadOnlyHtmlDocument() {
		JEditorPane pane = GeoCeDGGuideWindow.createView(new Font("SansSerif",
				Font.PLAIN, 12));
		assertFalse(pane.isEditable(), "the guide view must stay read-only");
		assertEquals("text/html", pane.getContentType());
		assertNotNull(pane.getEditorKit());
	}

	@Test
	void theDefaultSizeIsAReadingSizeAndNeverFollowsTheContent() {
		Dimension large = GeoCeDGGuideWindow.defaultSize(
				new Rectangle(0, 0, 3840, 2160));
		assertEquals(GeoCeDGGuideWindow.PREFERRED_WIDTH, large.width,
				"a large screen must not widen the reading window");
		assertEquals(GeoCeDGGuideWindow.PREFERRED_HEIGHT, large.height);
		assertTrue(large.width <= 900, "the window must stay a reading window");

		// A small screen clamps down instead of overflowing.
		Dimension small = GeoCeDGGuideWindow.defaultSize(new Rectangle(0, 0, 800, 600));
		assertEquals(720, small.width);
		assertEquals(520, small.height);
		assertTrue(small.width <= 800 && small.height <= 600);

		// A very small screen still yields a usable minimum.
		Dimension tiny = GeoCeDGGuideWindow.defaultSize(new Rectangle(0, 0, 320, 240));
		assertEquals(GeoCeDGGuideWindow.MINIMUM_WIDTH, tiny.width);
		assertEquals(GeoCeDGGuideWindow.MINIMUM_HEIGHT, tiny.height);
	}

	@Test
	void theWindowIsResizableNonModalAndScrollable() throws IOException {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"the window contract needs a display");
		GeoCeDGGuideWindow window = new GeoCeDGGuideWindow(null,
				new Font("SansSerif", Font.PLAIN, 12));
		String html = GeoCeDGGuideRenderer.toHtml(
				GeoCeDGActionRegistry.readUserGuide("en"));
		try {
			window.show(html, "GeoCeDG user guide");
			JDialog dialog = window.getDialog();
			assertNotNull(dialog);
			assertTrue(dialog.isResizable(), "the guide window must be resizable");
			assertEquals(JDialog.ModalityType.MODELESS, dialog.getModalityType(),
					"the guide must not block the construction");
			assertEquals(JDialog.HIDE_ON_CLOSE, dialog.getDefaultCloseOperation());
			assertNotNull(dialog.getMinimumSize());
			assertTrue(dialog.getMinimumSize().width <= dialog.getWidth());
			assertTrue(dialog.getWidth() <= GeoCeDGGuideWindow.PREFERRED_WIDTH,
					"the default width must not follow the widest table");
			assertFalse(window.getView().isEditable());

			// Reopening replaces the content in the same window, per language.
			window.show(GeoCeDGGuideRenderer.toHtml(
					GeoCeDGActionRegistry.readUserGuide("es")), "Guía");
			assertEquals(dialog, window.getDialog(),
					"a language change must not open a second window");
		} finally {
			if (window.getDialog() != null) {
				window.getDialog().dispose();
			}
		}
	}

	@Test
	void theGuideNeverReachesAnExternalBrowser() throws IOException {
		String registry = java.nio.file.Files.readString(
				java.nio.file.Path.of("src/main/java/org/geocedg/desktop/"
						+ "GeoCeDGActionRegistry.java"));
		String viewer = java.nio.file.Files.readString(
				java.nio.file.Path.of("src/main/java/org/geocedg/desktop/"
						+ "GeoCeDGGuideWindow.java"));
		for (String source : new String[] {registry, viewer}) {
			assertFalse(source.contains("Desktop.browse"), source.length() + "");
			assertFalse(source.contains("openHelp"), source.length() + "");
			assertFalse(source.contains("BrowserLauncher"), source.length() + "");
		}
		// The viewer must not activate a hyperlink either.
		assertFalse(viewer.contains("addHyperlinkListener"));
	}

	// ------------------------------------------------------------------ helpers

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
