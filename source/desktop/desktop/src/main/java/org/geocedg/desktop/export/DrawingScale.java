/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * PRE-G9B-R6-plus-C engineering drawing scale {@code a:b} (unit-system
 * section 12; author decision {@code DQ-C6}): output length over real model
 * length is {@code a / b}. {@code 1:1} is full size, {@code 1:2} half size and
 * {@code 2:1} double size.
 *
 * <p>The value is immutable and always in normal form {@code (a/g):(b/g)} with
 * {@code g = gcd(a, b)}, so equivalent pairs are equal. Both terms are positive
 * integers bounded by {@link #MAXIMUM_TERM}; invalid or overflowing input is
 * rejected explicitly, never clamped. It is session export state: never
 * serialized, never part of undo, never geometry.
 */
public final class DrawingScale {

	/** Largest admitted term after normalization. */
	public static final int MAXIMUM_TERM = 1_000_000_000;
	/** Full size, the default. */
	public static final DrawingScale ONE_TO_ONE = new DrawingScale(1, 1);
	/** Presets offered by the scale controls; free entry stays possible. */
	public static final List<DrawingScale> PRESETS = Collections.unmodifiableList(
			Arrays.asList(ONE_TO_ONE, new DrawingScale(1, 2), new DrawingScale(1, 5),
					new DrawingScale(1, 10), new DrawingScale(2, 1),
					new DrawingScale(5, 1)));

	private final int numerator;
	private final int denominator;

	private DrawingScale(int numerator, int denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}

	/**
	 * @param a output-side term
	 * @param b real-side term
	 * @return the normalized scale
	 * @throws IllegalArgumentException when a term is not positive or a
	 *         normalized term exceeds {@link #MAXIMUM_TERM}
	 */
	public static DrawingScale of(long a, long b) {
		if (a <= 0 || b <= 0) {
			throw new IllegalArgumentException(
					"A drawing scale needs two positive integers a:b");
		}
		long divisor = gcd(a, b);
		long normalizedA = a / divisor;
		long normalizedB = b / divisor;
		if (normalizedA > MAXIMUM_TERM || normalizedB > MAXIMUM_TERM) {
			throw new IllegalArgumentException("A drawing-scale term exceeds "
					+ MAXIMUM_TERM);
		}
		if (normalizedA == 1 && normalizedB == 1) {
			return ONE_TO_ONE;
		}
		return new DrawingScale((int) normalizedA, (int) normalizedB);
	}

	/**
	 * Parses {@code a:b} with ASCII digits and optional surrounding spaces.
	 *
	 * @param text user or caller text
	 * @return the normalized scale
	 * @throws IllegalArgumentException for any other text
	 */
	public static DrawingScale parse(String text) {
		if (text == null) {
			throw new IllegalArgumentException("A drawing scale is required");
		}
		String trimmed = text.trim();
		int separator = trimmed.indexOf(':');
		if (separator <= 0 || separator != trimmed.lastIndexOf(':')
				|| separator == trimmed.length() - 1) {
			throw new IllegalArgumentException(
					"A drawing scale is written a:b, for example 1:2");
		}
		return of(parseTerm(trimmed.substring(0, separator).trim()),
				parseTerm(trimmed.substring(separator + 1).trim()));
	}

	private static long parseTerm(String term) {
		if (term.isEmpty() || term.length() > 18) {
			throw new IllegalArgumentException(
					"A drawing-scale term must be a positive integer up to "
							+ MAXIMUM_TERM);
		}
		long value = 0;
		for (int index = 0; index < term.length(); index++) {
			char digit = term.charAt(index);
			if (digit < '0' || digit > '9') {
				throw new IllegalArgumentException(
						"A drawing-scale term must be a positive integer");
			}
			value = value * 10 + (digit - '0');
		}
		return value;
	}

	private static long gcd(long first, long second) {
		long a = first;
		long b = second;
		while (b != 0) {
			long remainder = a % b;
			a = b;
			b = remainder;
		}
		return a;
	}

	/** @return output-side term a */
	public int getNumerator() {
		return numerator;
	}

	/** @return real-side term b */
	public int getDenominator() {
		return denominator;
	}

	/** @return whether this is full size */
	public boolean isOneToOne() {
		return numerator == 1 && denominator == 1;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DrawingScale)) {
			return false;
		}
		DrawingScale scale = (DrawingScale) other;
		return numerator == scale.numerator && denominator == scale.denominator;
	}

	@Override
	public int hashCode() {
		return 31 * numerator + denominator;
	}

	/** @return normal form {@code a:b} */
	@Override
	public String toString() {
		return numerator + ":" + denominator;
	}
}
