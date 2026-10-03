/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.units;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Deterministic canonical decimal form of a finite positive binary64 factor
 * (unit-system v1.0, section 8.4).
 *
 * <p>The writer implements the decimal selection that {@code Double.toString} has
 * specified since Java SE 19 exactly, with exact {@link BigDecimal} arithmetic: among
 * the decimals that round to the value under round-to-nearest-even, take those of
 * minimal length (length 1 or 2 when the minimal length is 1), choose the one closest
 * to the value, and the even significand on a tie; then lay it out in plain notation
 * for {@code 10^-3 <= k < 10^7} and in computerized scientific notation otherwise. It
 * never calls {@code Double.toString}, which on JDK 17 is not shortest, and it is
 * locale-independent.
 *
 * <p>The reader accepts exactly the section 8.4 lexical grammar
 * {@code 1*DIGIT ["." 1*DIGIT] [("E"/"e") ["+"/"-"] 1*DIGIT]}, ASCII only, before
 * converting with correct rounding.
 */
public final class CanonicalBinary64 {

	private CanonicalBinary64() {
	}

	/**
	 * @param value a factor
	 * @return whether it is finite and greater than zero (section 4.2)
	 */
	public static boolean isValidFactor(double value) {
		return value > 0 && !Double.isInfinite(value);
	}

	/**
	 * @param value finite binary64 greater than zero
	 * @return canonical decimal form
	 * @throws IllegalArgumentException for any other value
	 */
	public static String write(double value) {
		if (!isValidFactor(value)) {
			throw new IllegalArgumentException("Not a finite positive factor");
		}
		BigDecimal exact = new BigDecimal(value);
		boolean inclusive = (Double.doubleToRawLongBits(value) & 1L) == 0L;
		BigDecimal two = BigDecimal.valueOf(2);
		BigDecimal low = exact.add(new BigDecimal(Math.nextDown(value))).divide(two);
		BigDecimal high = value == Double.MAX_VALUE
				? exact.add(new BigDecimal(Math.ulp(value)).divide(two))
				: exact.add(new BigDecimal(Math.nextUp(value))).divide(two);
		int decimalExponent = exact.precision() - exact.scale() - 1;
		int minimalLength = -1;
		for (int length = 1; length <= 17 && minimalLength < 0; length++) {
			for (int exponent = decimalExponent - length;
					exponent <= decimalExponent - length + 2; exponent++) {
				if (firstCandidate(low, high, inclusive, exponent, 1, length) != null) {
					minimalLength = length;
					break;
				}
			}
		}
		if (minimalLength < 0) {
			throw new IllegalStateException("No decimal rounds to the factor");
		}
		int maximalLength = minimalLength >= 2 ? minimalLength : 2;
		BigInteger bestSignificand = null;
		int bestExponent = 0;
		BigDecimal bestDistance = null;
		for (int length = minimalLength; length <= maximalLength; length++) {
			for (int exponent = decimalExponent - length;
					exponent <= decimalExponent - length + 2; exponent++) {
				BigInteger[] range = range(low, high, inclusive, exponent, length, length);
				if (range == null) {
					continue;
				}
				for (BigInteger s = range[0]; s.compareTo(range[1]) <= 0;
						s = s.add(BigInteger.ONE)) {
					if (s.mod(BigInteger.TEN).signum() == 0) {
						continue;
					}
					BigDecimal distance = new BigDecimal(s, -exponent).subtract(exact).abs();
					int comparison = bestDistance == null ? -1
							: distance.compareTo(bestDistance);
					if (comparison < 0 || (comparison == 0 && !s.testBit(0)
							&& bestSignificand.testBit(0))) {
						bestSignificand = s;
						bestExponent = exponent;
						bestDistance = distance;
					}
				}
			}
		}
		return layout(bestSignificand.toString(), bestExponent);
	}

	private static BigInteger firstCandidate(BigDecimal low, BigDecimal high,
			boolean inclusive, int exponent, int minLength, int maxLength) {
		BigInteger[] range = range(low, high, inclusive, exponent, minLength, maxLength);
		return range == null ? null : range[0];
	}

	/** @return the significands s with s*10^exponent rounding to the value, or null */
	private static BigInteger[] range(BigDecimal low, BigDecimal high, boolean inclusive,
			int exponent, int minLength, int maxLength) {
		BigDecimal scale = BigDecimal.ONE.scaleByPowerOfTen(exponent);
		BigDecimal lowScaled = low.divide(scale);
		BigDecimal highScaled = high.divide(scale);
		BigInteger first = lowScaled.setScale(0, RoundingMode.CEILING).toBigIntegerExact();
		if (!inclusive && new BigDecimal(first).compareTo(lowScaled) == 0) {
			first = first.add(BigInteger.ONE);
		}
		BigInteger last = highScaled.setScale(0, RoundingMode.FLOOR).toBigIntegerExact();
		if (!inclusive && new BigDecimal(last).compareTo(highScaled) == 0) {
			last = last.subtract(BigInteger.ONE);
		}
		BigInteger smallest = BigInteger.TEN.pow(minLength - 1);
		BigInteger largest = BigInteger.TEN.pow(maxLength).subtract(BigInteger.ONE);
		if (first.compareTo(smallest) < 0) {
			first = smallest;
		}
		if (last.compareTo(largest) > 0) {
			last = largest;
		}
		return first.compareTo(last) > 0 ? null : new BigInteger[] {first, last};
	}

	private static String layout(String digits, int exponent) {
		int length = digits.length();
		int scientificExponent = length + exponent - 1;
		StringBuilder sb = new StringBuilder();
		if (-3 <= scientificExponent && scientificExponent < 0) {
			sb.append("0.");
			for (int i = 0; i < -(length + exponent); i++) {
				sb.append('0');
			}
			sb.append(digits);
		} else if (0 <= scientificExponent && scientificExponent < 7) {
			if (exponent >= 0) {
				sb.append(digits);
				for (int i = 0; i < exponent; i++) {
					sb.append('0');
				}
				sb.append(".0");
			} else {
				sb.append(digits, 0, length + exponent).append('.')
						.append(digits, length + exponent, length);
			}
		} else {
			sb.append(digits.charAt(0)).append('.');
			sb.append(length == 1 ? "0" : digits.substring(1));
			sb.append('E').append(scientificExponent);
		}
		return sb.toString();
	}

	/**
	 * @param lexical candidate factor text
	 * @return whether it matches the section 8.4 lexical grammar exactly
	 */
	public static boolean isLexical(String lexical) {
		if (lexical == null) {
			return false;
		}
		int length = lexical.length();
		int index = digits(lexical, 0);
		if (index == 0) {
			return false;
		}
		if (index < length && lexical.charAt(index) == '.') {
			int fractionEnd = digits(lexical, index + 1);
			if (fractionEnd == index + 1) {
				return false;
			}
			index = fractionEnd;
		}
		if (index < length && (lexical.charAt(index) == 'E'
				|| lexical.charAt(index) == 'e')) {
			index++;
			if (index < length && (lexical.charAt(index) == '+'
					|| lexical.charAt(index) == '-')) {
				index++;
			}
			int exponentEnd = digits(lexical, index);
			if (exponentEnd == index) {
				return false;
			}
			index = exponentEnd;
		}
		return index == length;
	}

	private static int digits(String text, int start) {
		int index = start;
		while (index < text.length() && text.charAt(index) >= '0'
				&& text.charAt(index) <= '9') {
			index++;
		}
		return index;
	}

	/**
	 * @param lexical factor text
	 * @return the binary64 value, or {@code null} when the text is not in the
	 *         section 8.4 grammar or does not denote a finite factor greater than zero
	 */
	public static Double tryParse(String lexical) {
		if (!isLexical(lexical)) {
			return null;
		}
		double value;
		try {
			value = Double.parseDouble(lexical);
		} catch (NumberFormatException e) {
			return null;
		}
		return isValidFactor(value) ? value : null;
	}
}
