/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PRE-G9B-R6-plus-G strict RFC 8259 reader for small GeoCeDG-owned records, such as
 * the installed distribution record (PM-5). Unlike a lenient reader it refuses
 * unquoted keys, single quotes, comments, trailing commas, duplicate keys, leading
 * zeros, unescaped control characters, a byte-order mark and any content after the
 * root value. Objects are returned as insertion-ordered maps, arrays as lists,
 * integral numbers as {@link Long}, other numbers as {@link BigDecimal}, and
 * {@code null} as {@link #NULL}.
 */
final class GeoCeDGStrictJson {

	/** The JSON {@code null} value, distinct from an absent member. */
	static final Object NULL = new Object() {
		@Override
		public String toString() {
			return "null";
		}
	};

	private static final int MAX_DEPTH = 16;

	private final String text;
	private int position;

	private GeoCeDGStrictJson(String text) {
		this.text = text;
	}

	/**
	 * @param text complete JSON text
	 * @return the root value
	 * @throws IllegalArgumentException when the text is not strict JSON
	 */
	static Object parse(String text) {
		if (text == null) {
			throw new IllegalArgumentException("JSON text is required");
		}
		GeoCeDGStrictJson reader = new GeoCeDGStrictJson(text);
		reader.skipWhitespace();
		Object value = reader.value(0);
		reader.skipWhitespace();
		if (reader.position != text.length()) {
			throw reader.error("content after the root value");
		}
		return value;
	}

	private Object value(int depth) {
		if (depth > MAX_DEPTH) {
			throw error("nesting too deep");
		}
		if (position >= text.length()) {
			throw error("value expected");
		}
		char c = text.charAt(position);
		switch (c) {
		case '{':
			return object(depth + 1);
		case '[':
			return array(depth + 1);
		case '"':
			return string();
		case 't':
			literal("true");
			return Boolean.TRUE;
		case 'f':
			literal("false");
			return Boolean.FALSE;
		case 'n':
			literal("null");
			return NULL;
		default:
			if (c == '-' || (c >= '0' && c <= '9')) {
				return number();
			}
			throw error("unexpected character");
		}
	}

	private Map<String, Object> object(int depth) {
		position++;
		Map<String, Object> members = new LinkedHashMap<>();
		skipWhitespace();
		if (peek() == '}') {
			position++;
			return Collections.unmodifiableMap(members);
		}
		while (true) {
			skipWhitespace();
			if (peek() != '"') {
				throw error("quoted member name expected");
			}
			String name = string();
			if (members.containsKey(name)) {
				throw error("duplicate member " + name);
			}
			skipWhitespace();
			expect(':');
			skipWhitespace();
			members.put(name, value(depth));
			skipWhitespace();
			char next = peek();
			position++;
			if (next == '}') {
				return Collections.unmodifiableMap(members);
			}
			if (next != ',') {
				throw error("',' or '}' expected");
			}
		}
	}

	private List<Object> array(int depth) {
		position++;
		List<Object> items = new ArrayList<>();
		skipWhitespace();
		if (peek() == ']') {
			position++;
			return Collections.unmodifiableList(items);
		}
		while (true) {
			skipWhitespace();
			items.add(value(depth));
			skipWhitespace();
			char next = peek();
			position++;
			if (next == ']') {
				return Collections.unmodifiableList(items);
			}
			if (next != ',') {
				throw error("',' or ']' expected");
			}
		}
	}

	private String string() {
		expect('"');
		StringBuilder result = new StringBuilder();
		while (true) {
			if (position >= text.length()) {
				throw error("unterminated string");
			}
			char c = text.charAt(position++);
			if (c == '"') {
				return result.toString();
			}
			if (c < 0x20) {
				throw error("unescaped control character");
			}
			if (c != '\\') {
				result.append(c);
				continue;
			}
			if (position >= text.length()) {
				throw error("unterminated escape");
			}
			char escape = text.charAt(position++);
			switch (escape) {
			case '"':
			case '\\':
			case '/':
				result.append(escape);
				break;
			case 'b':
				result.append('\b');
				break;
			case 'f':
				result.append('\f');
				break;
			case 'n':
				result.append('\n');
				break;
			case 'r':
				result.append('\r');
				break;
			case 't':
				result.append('\t');
				break;
			case 'u':
				result.append(unicodeEscape());
				break;
			default:
				throw error("invalid escape");
			}
		}
	}

	private char unicodeEscape() {
		if (position + 4 > text.length()) {
			throw error("incomplete unicode escape");
		}
		int code = 0;
		for (int index = 0; index < 4; index++) {
			int digit = Character.digit(text.charAt(position++), 16);
			if (digit < 0) {
				throw error("invalid unicode escape");
			}
			code = code * 16 + digit;
		}
		return (char) code;
	}

	private Object number() {
		int start = position;
		if (peek() == '-') {
			position++;
		}
		if (peek() == '0') {
			position++;
			if (isDigit(peek())) {
				throw error("leading zero");
			}
		} else if (isDigit(peek())) {
			digits();
		} else {
			throw error("digit expected");
		}
		boolean integral = true;
		if (peek() == '.') {
			integral = false;
			position++;
			if (!isDigit(peek())) {
				throw error("fraction digit expected");
			}
			digits();
		}
		if (peek() == 'e' || peek() == 'E') {
			integral = false;
			position++;
			if (peek() == '+' || peek() == '-') {
				position++;
			}
			if (!isDigit(peek())) {
				throw error("exponent digit expected");
			}
			digits();
		}
		String literal = text.substring(start, position);
		if (integral) {
			try {
				return Long.valueOf(literal);
			} catch (NumberFormatException overflow) {
				return new BigDecimal(literal);
			}
		}
		return new BigDecimal(literal);
	}

	private void digits() {
		while (isDigit(peek())) {
			position++;
		}
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private void literal(String expected) {
		if (!text.startsWith(expected, position)) {
			throw error("invalid literal");
		}
		position += expected.length();
	}

	private void expect(char expected) {
		if (peek() != expected) {
			throw error("'" + expected + "' expected");
		}
		position++;
	}

	/** @return the current character, or NUL at the end (never valid JSON there) */
	private char peek() {
		return position < text.length() ? text.charAt(position) : '\0';
	}

	private void skipWhitespace() {
		while (position < text.length()) {
			char c = text.charAt(position);
			if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
				return;
			}
			position++;
		}
	}

	private IllegalArgumentException error(String message) {
		return new IllegalArgumentException("Invalid JSON at offset " + position + ": "
				+ message);
	}
}
