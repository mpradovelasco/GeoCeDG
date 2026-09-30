/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Navigation outline derived from one user-guide edition while it is rendered.
 *
 * <p>The authored Markdown is the only source. Identity follows the stable
 * section markers and the decimal numbering, as fixed by section 10 of the
 * documentation maintenance contract: a level-1 or level-2 heading takes the
 * identifier of the marker on the line before it, and a level-3 heading takes the
 * enclosing marker identifier, a {@code /} and its numbering token. Heading text
 * is only the display label; it never contributes to identity.
 *
 * <p>A heading that does not satisfy that rule gets no anchor and no entry, so
 * the viewer never invents an identity. The focal tests and the structural
 * diagnostic require every level-1 to level-3 heading of both editions to have
 * one. The outline is presentation state: it is never persisted and never
 * consulted by the kernel, geometry, dependency graph or serialization.
 */
final class GeoCeDGGuideOutline {

	/** The stable section marker, exactly as both editions author it. */
	static final Pattern SECTION_MARKER =
			Pattern.compile("^<!-- geocedg-guide-section: ([a-z0-9-]+) -->$");
	/** Leading decimal numbering token of a heading, without a trailing period. */
	private static final Pattern NUMBERING = Pattern.compile("^(\\d+(?:\\.\\d+)*)\\.?\\s");
	/** Deepest heading level that is a navigation entry. */
	static final int DEEPEST_LEVEL = 3;

	/**
	 * One navigation entry.
	 *
	 * @param level heading level, 1 to 3
	 * @param numbering decimal numbering token, or an empty string
	 * @param sectionId identifier of the enclosing section marker
	 * @param anchor derived navigation identity, unique within the edition
	 * @param label localized display text; never an identity
	 * @param parentAnchor anchor of the enclosing entry, or {@code null}
	 */
	record Entry(int level, String numbering, String sectionId, String anchor,
			String label, String parentAnchor) {
	}

	private final List<Entry> entries = new ArrayList<>();
	private final Set<String> anchors = new HashSet<>();
	private final Deque<Entry> open = new ArrayDeque<>();
	private String section;

	/**
	 * @param line one source line with trailing whitespace removed
	 * @return the marker identifier, or {@code null} if the line is no marker
	 */
	static String sectionMarker(String line) {
		Matcher marker = SECTION_MARKER.matcher(line);
		return marker.matches() ? marker.group(1) : null;
	}

	/**
	 * @param headingText heading source text after the ATX marker
	 * @return its decimal numbering token, or an empty string
	 */
	static String numbering(String headingText) {
		Matcher number = NUMBERING.matcher(headingText);
		return number.find() ? number.group(1) : "";
	}

	/**
	 * Records one heading in document order.
	 *
	 * @param level heading level, 1 to 6
	 * @param marker identifier of the marker on the immediately preceding line,
	 *        or {@code null}
	 * @param headingText heading source text after the ATX marker
	 * @param label display text of the rendered heading
	 * @return the anchor of the new entry, or {@code null} if the heading is not
	 *         a navigation entry
	 */
	String heading(int level, String marker, String headingText, String label) {
		String number = numbering(headingText);
		if (level <= 2) {
			// The heading opens a section; without a marker it has no identity and
			// neither have the level-3 headings inside it.
			section = marker;
			return marker == null ? null : add(level, number, marker, marker, label);
		}
		if (level > DEEPEST_LEVEL || section == null || number.isEmpty()) {
			return null;
		}
		return add(level, number, section, section + "/" + number, label);
	}

	private String add(int level, String number, String sectionId, String anchor,
			String label) {
		if (!anchors.add(anchor)) {
			// The first heading keeps an identity; a duplicate never shadows it.
			return null;
		}
		while (!open.isEmpty() && open.peek().level() >= level) {
			open.pop();
		}
		Entry entry = new Entry(level, number, sectionId, anchor, label,
				open.isEmpty() ? null : open.peek().anchor());
		entries.add(entry);
		open.push(entry);
		return anchor;
	}

	/** @return the entries in document order */
	List<Entry> entries() {
		return List.copyOf(entries);
	}
}
