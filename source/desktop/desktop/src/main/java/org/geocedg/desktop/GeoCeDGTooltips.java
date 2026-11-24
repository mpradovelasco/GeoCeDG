/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact multi-line tooltips (PRE-G9B-MAINT-U2-R1). Swing shows a plain tooltip on one line,
 * so a long help text becomes a very wide window; these helpers break it into short lines with
 * the HTML that Swing tooltips already support. Line breaks are explicit, so the width follows
 * the font and scales with it at any HiDPI setting.
 */
final class GeoCeDGTooltips {

	/** Longest line, in characters, of a wrapped tooltip. */
	static final int COLUMNS = 60;

	private static final String HTML = "<html>";

	private GeoCeDGTooltips() {
	}

	/**
	 * @param lines the lines of the tooltip, in order, without markup
	 * @return one tooltip showing each line on its own row, long lines wrapped
	 */
	static String lines(String... lines) {
		List<String> rows = new ArrayList<>();
		for (String line : lines) {
			rows.addAll(rows(line));
		}
		StringBuilder html = new StringBuilder(HTML);
		for (int i = 0; i < rows.size(); i++) {
			html.append(i == 0 ? "" : "<br>").append(escape(rows.get(i)));
		}
		return html.append("</html>").toString();
	}

	/**
	 * @param text plain text; explicit line breaks are kept
	 * @return the text unchanged when it is short and has one line, otherwise a wrapped tooltip
	 */
	static String wrap(String text) {
		if (text == null || text.startsWith(HTML)
				|| (text.indexOf('\n') < 0 && text.length() <= COLUMNS)) {
			return text;
		}
		return lines(text.split("\r?\n"));
	}

	/**
	 * @param text plain text, such as a tool help
	 * @return a multi-line tooltip when the text has explicit line breaks, otherwise the text
	 */
	static String explicit(String text) {
		return text == null || text.indexOf('\n') < 0 ? text : lines(text.split("\r?\n"));
	}

	/**
	 * @param text a tooltip, plain or made by this class
	 * @return the same words as one plain line, for an accessible description
	 */
	static String plain(String text) {
		if (text == null) {
			return null;
		}
		if (text.startsWith(HTML)) {
			text = text.substring(HTML.length(), text.length() - "</html>".length())
					.replace("<br>", " ").replace("&lt;", "<").replace("&gt;", ">")
					.replace("&amp;", "&");
		}
		return text.replaceAll("\\s*\\r?\\n\\s*", " ");
	}

	/** Word-wraps one line at {@link #COLUMNS} characters; a longer word stays whole. */
	private static List<String> rows(String line) {
		List<String> rows = new ArrayList<>();
		StringBuilder row = new StringBuilder();
		for (String word : line.trim().split("\\s+")) {
			if (row.length() > 0 && row.length() + 1 + word.length() > COLUMNS) {
				rows.add(row.toString());
				row.setLength(0);
			}
			row.append(row.length() == 0 ? "" : " ").append(word);
		}
		rows.add(row.toString());
		return rows;
	}

	private static String escape(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
