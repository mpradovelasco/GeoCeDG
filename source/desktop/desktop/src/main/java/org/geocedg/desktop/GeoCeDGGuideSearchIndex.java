/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/** Searches the displayed guide text and maps matches back to the Swing document. */
final class GeoCeDGGuideSearchIndex {

	static final int RESULT_LIMIT = 200;
	private static final Pattern MARKS = Pattern.compile("\\p{M}+");
	private static final Pattern SPACES = Pattern.compile("[\\s\\p{Z}]+");
	private static final int CONTEXT = 32;

	record Match(String anchor, String section, String context, int start, int end) {
		@Override
		public String toString() {
			return section + " — " + context;
		}
	}

	record Results(List<Match> matches, boolean truncated) {
	}

	private record Folded(String text, int[] offsets) {
		int start(int index) {
			return offsets[index];
		}

		int end(int index) {
			// A single source character may expand into more than one search character.
			while (offsets[index] == offsets[index - 1]) {
				index++;
			}
			return offsets[index];
		}
	}

	private record Section(String anchor, String title, int start, String text, Folded folded) {
		Section(String anchor, String title, int start, String text) {
			this(anchor, title, start, text, fold(text));
		}

		Match match(int from, int to) {
			int relativeStart = folded.start(from);
			int relativeEnd = folded.end(to);
			return new Match(anchor, title, excerpt(text, relativeStart, relativeEnd),
					start + relativeStart, start + relativeEnd);
		}
	}

	private final List<Section> sections = new ArrayList<>();

	GeoCeDGGuideSearchIndex(Document document, List<GeoCeDGGuideOutline.Entry> outline,
			GeoCeDGGuideNavigator navigator, String title) {
		String text;
		try {
			text = document.getText(0, document.getLength());
		} catch (BadLocationException exception) {
			throw new IllegalStateException("Cannot read guide text", exception);
		}
		List<GeoCeDGGuideOutline.Entry> entries = outline.stream()
				.filter(entry -> navigator.anchorOffset(entry.anchor()) >= 0).toList();
		if (entries.isEmpty()) {
			sections.add(new Section(null, title, 0, text));
		}
		for (int index = 0; index < entries.size(); index++) {
			GeoCeDGGuideOutline.Entry entry = entries.get(index);
			int start = navigator.anchorOffset(entry.anchor());
			int end = index + 1 == entries.size() ? text.length()
					: navigator.anchorOffset(entries.get(index + 1).anchor());
			sections.add(new Section(entry.anchor(), entry.label(), start,
					text.substring(start, end)));
		}
	}

	Results find(String query) {
		String needle = fold(query).text().strip();
		if (needle.isEmpty()) {
			return new Results(List.of(), false);
		}
		List<Match> matches = new ArrayList<>();
		for (Section section : sections) {
			String haystack = section.folded().text();
			int offset = haystack.indexOf(needle);
			while (offset >= 0) {
				if (matches.size() == RESULT_LIMIT) {
					return new Results(List.copyOf(matches), true);
				}
				int end = offset + needle.length();
				matches.add(section.match(offset, end));
				offset = haystack.indexOf(needle, end);
			}
		}
		return new Results(List.copyOf(matches), false);
	}

	private static Folded fold(String source) {
		StringBuilder text = new StringBuilder(source.length());
		int[] offsets = new int[source.length() + 1];
		for (int offset = 0; offset < source.length();) {
			if (text.length() + 1 >= offsets.length) {
				offsets = Arrays.copyOf(offsets, offsets.length * 2);
			}
			int codePoint = source.codePointAt(offset);
			if (Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint)) {
				if (!text.isEmpty() && text.charAt(text.length() - 1) != ' ') {
					offsets[text.length()] = offset;
					text.append(' ');
				}
			} else if (codePoint < 128) {
				offsets[text.length()] = offset;
				text.append((char) Character.toLowerCase(codePoint));
			} else {
				String decomposed = Normalizer.normalize(Character.toString(codePoint),
						Normalizer.Form.NFD);
				String folded = MARKS.matcher(decomposed).replaceAll("")
						.toLowerCase(Locale.ROOT);
				int length = text.length() + folded.length();
				if (length >= offsets.length) {
					offsets = Arrays.copyOf(offsets, Math.max(length + 1, offsets.length * 2));
				}
				Arrays.fill(offsets, text.length(), length, offset);
				text.append(folded);
			}
			offset += Character.charCount(codePoint);
		}
		offsets[text.length()] = source.length();
		return new Folded(text.toString(), offsets);
	}

	private static String excerpt(String text, int start, int end) {
		int from = Math.max(0, start - CONTEXT);
		int to = Math.min(text.length(), Math.min(end, start + 2 * CONTEXT) + CONTEXT);
		if (from > 0 && Character.isLowSurrogate(text.charAt(from))) {
			from--;
		}
		if (to < text.length() && Character.isLowSurrogate(text.charAt(to))) {
			to++;
		}
		String excerpt = SPACES.matcher(text.substring(from, to)).replaceAll(" ").strip();
		return (from > 0 ? "…" : "") + excerpt + (to < text.length() ? "…" : "");
	}
}
