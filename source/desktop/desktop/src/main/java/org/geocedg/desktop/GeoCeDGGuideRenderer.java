/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bounded Markdown-to-HTML renderer for the two packaged GeoCeDG user guides.
 *
 * <p>This is presentation only. It converts the tracked Markdown sources into
 * the HTML subset that Swing's {@code HTMLEditorKit} renders reliably, so that
 * the guides stay authored and reviewed as Markdown while the application shows
 * formatted text instead of source markup. It never touches the kernel,
 * geometry, the dependency graph, serialization or semantic identity.
 *
 * <h2>Supported block constructs</h2>
 *
 * <ul>
 * <li>ATX headings {@code #} to {@code ######};</li>
 * <li>paragraphs, with consecutive lines joined;</li>
 * <li>fenced code blocks delimited by three backticks, with an optional
 * language word that is not rendered;</li>
 * <li>unordered lists introduced by {@code - } or {@code * };</li>
 * <li>ordered lists introduced by {@code 1. };</li>
 * <li>pipe tables with a delimiter row, whose first row is the header;</li>
 * <li>horizontal rules written as three or more hyphens on their own line;</li>
 * <li>HTML comments, which are removed and never shown, which is how the stable
 * {@code geocedg-guide-section} identifiers stay invisible to the reader.</li>
 * </ul>
 *
 * <h2>Supported inline constructs</h2>
 *
 * <ul>
 * <li>{@code `inline code`};</li>
 * <li>{@code **strong**};</li>
 * <li>{@code *emphasis*};</li>
 * <li>{@code [text](target)}, rendered as a visually recognizable link that
 * this viewer never navigates.</li>
 * </ul>
 *
 * <h2>Deliberate non-goals</h2>
 *
 * <p>This is not a general Markdown implementation and must not become one. It
 * covers exactly the audited subset that the two guides use. Nested lists,
 * setext headings, block quotes, images, reference links, inline HTML,
 * footnotes and tables without a delimiter row are outside the contract:
 * unrecognized markup degrades to escaped literal text rather than to broken
 * layout. Unbalanced inline markers stay literal for the same reason.
 */
final class GeoCeDGGuideRenderer {

	/** Closing marker of a list item; used to append wrapped continuations. */
	private static final String ITEM_END = "</li>\n";
	/** Placeholder marker for extracted code spans; never valid guide text. */
	private static final char CODE_SLOT = (char) 1;
	private static final Pattern HTML_COMMENT =
			Pattern.compile("^\\s*<!--.*-->\\s*$");
	private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");
	private static final Pattern FENCE = Pattern.compile("^```\\s*(\\w*)\\s*$");
	private static final Pattern RULE = Pattern.compile("^-{3,}\\s*$");
	private static final Pattern UNORDERED = Pattern.compile("^[-*]\\s+(.*)$");
	private static final Pattern ORDERED = Pattern.compile("^\\d+\\.\\s+(.*)$");
	private static final Pattern TABLE_DELIMITER =
			Pattern.compile("^\\|[\\s:|-]+\\|\\s*$");
	private static final Pattern CODE_SPAN = Pattern.compile("`([^`]+)`");
	private static final Pattern STRONG = Pattern.compile("\\*\\*(\\S(?:.*?\\S)?)\\*\\*");
	private static final Pattern EMPHASIS =
			Pattern.compile("(?<!\\*)\\*(?!\\*)(\\S(?:[^*]*\\S)?)\\*(?!\\*)");
	private static final Pattern LINK =
			Pattern.compile("\\[([^\\]]+)]\\(([^)\\s]+)\\)");

	private GeoCeDGGuideRenderer() {
		// Pure conversion; no instance state and no application authority.
	}

	/**
	 * Converts one tracked guide source into a complete HTML document.
	 *
	 * @param markdown packaged guide source, never {@code null}
	 * @return self-contained HTML for a read-only Swing editor pane
	 */
	static String toHtml(String markdown) {
		List<String> lines = List.of(markdown.replace("\r\n", "\n")
				.replace("\r", "\n").split("\n", -1));
		StringBuilder html = new StringBuilder(markdown.length() * 2);
		html.append("<html><body>\n");
		List<String> paragraph = new ArrayList<>();
		List<String> table = new ArrayList<>();
		List<String> item = new ArrayList<>();
		String list = null;
		boolean fenced = false;
		StringBuilder code = new StringBuilder();

		for (String raw : lines) {
			String line = trimTrailing(raw);
			if (fenced) {
				if (FENCE.matcher(line).matches()) {
					html.append("<pre>").append(escape(code.toString()))
							.append("</pre>\n");
					code.setLength(0);
					fenced = false;
				} else {
					code.append(line).append('\n');
				}
				continue;
			}
			if (FENCE.matcher(line).matches()) {
				paragraph = flushParagraph(html, paragraph);
				item = flushItem(html, item);
				list = flushList(html, list);
				table = flushTable(html, table);
				fenced = true;
				continue;
			}
			if (HTML_COMMENT.matcher(line).matches()) {
				// Stable section identifiers are structure, never reader text.
				paragraph = flushParagraph(html, paragraph);
				continue;
			}
			if (line.startsWith("|")) {
				paragraph = flushParagraph(html, paragraph);
				item = flushItem(html, item);
				list = flushList(html, list);
				table.add(line);
				continue;
			}
			table = flushTable(html, table);
			if (line.isBlank()) {
				paragraph = flushParagraph(html, paragraph);
				item = flushItem(html, item);
				list = flushList(html, list);
				continue;
			}
			Matcher heading = HEADING.matcher(line);
			if (heading.matches()) {
				paragraph = flushParagraph(html, paragraph);
				item = flushItem(html, item);
				list = flushList(html, list);
				String tag = "h" + heading.group(1).length();
				html.append('<').append(tag).append('>').append(inline(heading.group(2)))
						.append("</").append(tag).append(">\n");
				continue;
			}
			if (RULE.matcher(line).matches()) {
				paragraph = flushParagraph(html, paragraph);
				item = flushItem(html, item);
				list = flushList(html, list);
				html.append("<hr>\n");
				continue;
			}
			Matcher unordered = UNORDERED.matcher(line);
			if (unordered.matches()) {
				paragraph = flushParagraph(html, paragraph);
				item = flushItem(html, item);
				list = openList(html, list, "ul");
				item.add(unordered.group(1));
				continue;
			}
			Matcher ordered = ORDERED.matcher(line);
			if (ordered.matches()) {
				paragraph = flushParagraph(html, paragraph);
				item = flushItem(html, item);
				list = openList(html, list, "ol");
				item.add(ordered.group(1));
				continue;
			}
			if (!item.isEmpty() && line.startsWith("  ")) {
				// A wrapped continuation line; the item is rendered as one text so
				// that an inline span may straddle the source line break.
				item.add(line.strip());
				continue;
			}
			item = flushItem(html, item);
			list = flushList(html, list);
			paragraph.add(line);
		}
		if (fenced) {
			// An unterminated fence still shows its content, never raw markup.
			html.append("<pre>").append(escape(code.toString())).append("</pre>\n");
		}
		flushParagraph(html, paragraph);
		flushItem(html, item);
		flushList(html, list);
		flushTable(html, table);
		html.append("</body></html>");
		return html.toString();
	}

	private static List<String> flushItem(StringBuilder html, List<String> lines) {
		if (!lines.isEmpty()) {
			html.append("<li>").append(inline(String.join(" ", lines))).append(ITEM_END);
		}
		return new ArrayList<>();
	}

	private static List<String> flushParagraph(StringBuilder html, List<String> lines) {
		if (!lines.isEmpty()) {
			html.append("<p>").append(inline(String.join(" ", lines))).append("</p>\n");
		}
		return new ArrayList<>();
	}

	private static String openList(StringBuilder html, String open, String tag) {
		if (tag.equals(open)) {
			return open;
		}
		flushList(html, open);
		html.append('<').append(tag).append(">\n");
		return tag;
	}

	private static String flushList(StringBuilder html, String open) {
		if (open != null) {
			html.append("</").append(open).append(">\n");
		}
		return null;
	}

	private static List<String> flushTable(StringBuilder html, List<String> rows) {
		if (rows.isEmpty()) {
			return new ArrayList<>();
		}
		int delimiter = -1;
		for (int index = 0; index < rows.size(); index++) {
			if (TABLE_DELIMITER.matcher(rows.get(index)).matches()) {
				delimiter = index;
				break;
			}
		}
		if (delimiter < 1) {
			// Not an audited table; show the lines rather than invent structure.
			for (String row : rows) {
				html.append("<p>").append(inline(row)).append("</p>\n");
			}
			return new ArrayList<>();
		}
		html.append("<table>\n");
		for (int index = 0; index < rows.size(); index++) {
			if (index == delimiter) {
				continue;
			}
			String cellTag = index < delimiter ? "th" : "td";
			html.append("<tr>");
			for (String cell : cells(rows.get(index))) {
				html.append('<').append(cellTag).append('>').append(inline(cell))
						.append("</").append(cellTag).append('>');
			}
			html.append("</tr>\n");
		}
		html.append("</table>\n");
		return new ArrayList<>();
	}

	private static List<String> cells(String row) {
		String body = row.strip();
		if (body.startsWith("|")) {
			body = body.substring(1);
		}
		if (body.endsWith("|")) {
			body = body.substring(0, body.length() - 1);
		}
		List<String> cells = new ArrayList<>();
		for (String cell : body.split("\\|", -1)) {
			cells.add(cell.strip());
		}
		return cells;
	}

	/**
	 * Applies the audited inline subset. Code spans are extracted first so that
	 * a marker inside {@code `code`} is never read as emphasis.
	 *
	 * @param text one logical source line or cell
	 * @return escaped HTML fragment
	 */
	private static String inline(String text) {
		List<String> spans = new ArrayList<>();
		Matcher code = CODE_SPAN.matcher(text);
		StringBuilder masked = new StringBuilder();
		while (code.find()) {
			code.appendReplacement(masked,
					Matcher.quoteReplacement(CODE_SLOT + Integer.toString(spans.size())
							+ CODE_SLOT));
			spans.add(code.group(1));
		}
		code.appendTail(masked);

		String html = escape(masked.toString());
		html = STRONG.matcher(html).replaceAll("<b>$1</b>");
		html = EMPHASIS.matcher(html).replaceAll("<i>$1</i>");
		html = LINK.matcher(html).replaceAll("<a href=\"$2\">$1</a>");

		StringBuilder restored = new StringBuilder(html.length());
		for (int index = 0; index < html.length(); index++) {
			char current = html.charAt(index);
			if (current != CODE_SLOT) {
				restored.append(current);
				continue;
			}
			int end = html.indexOf(CODE_SLOT, index + 1);
			int slot = Integer.parseInt(html.substring(index + 1, end));
			restored.append("<code>").append(escape(spans.get(slot))).append("</code>");
			index = end;
		}
		return restored.toString();
	}

	private static String escape(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String trimTrailing(String line) {
		int end = line.length();
		while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
			end--;
		}
		return line.substring(0, end);
	}

	/**
	 * @param markdown packaged guide source
	 * @return the first ATX level-one heading, or an empty string
	 */
	static String title(String markdown) {
		for (String line : markdown.replace("\r\n", "\n").split("\n", -1)) {
			if (line.startsWith("# ")) {
				return line.substring(2).strip();
			}
		}
		return "";
	}
}
