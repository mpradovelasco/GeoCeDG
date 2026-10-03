/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.units;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.SplittableRandom;

import org.geocedg.common.kernel.units.CanonicalBinary64;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-D1 T-CANONICAL-FACTOR, T-LEXICAL and T-DETERMINISM: the
 * unit-system v1.0 section 8.4 factor form. The reference table and the sweep hash
 * were produced by the D1 preparation probe P-BINARY64 with an exact implementation of
 * the Java SE 19+ Double.toString specification, byte-identical on JDK 17, 22 and 25
 * and equal to Double.toString on JDK 22 and 25; JDK 17 Double.toString differs on
 * eight of the entries, so this test, which runs on the JDK 17 test JVM, also proves
 * that the writer does not delegate to it.
 */
class PreG9BR6PlusD1CanonicalFactorTest {
	/** Raw IEEE bits and canonical form; P-BINARY64 reference corpus, 48 entries. */
	private static final String[][] REFERENCE = {
			{"3F9A027525460AA6", "0.0254"},
			{"3FD381D7DBF487FD", "0.3048"},
			{"3FED42C3C9EECBFB", "0.9144"},
			{"409925604189374C", "1609.344"},
			{"409CF00000000000", "1852.0"},
			{"3FF0000000000000", "1.0"},
			{"3F50624DD2F1A9FC", "0.001"},
			{"3F847AE147AE147B", "0.01"},
			{"3FB999999999999A", "0.1"},
			{"3FC999999999999A", "0.2"},
			{"3FD3333333333333", "0.3"},
			{"3EB0C6F7A0B5ED8D", "1.0E-6"},
			{"3EFAA242B58735EA", "2.54E-5"},
			{"3E112E0BE826D695", "1.0E-9"},
			{"416312D000000000", "1.0E7"},
			{"416312CFFFFFFFFF", "9999999.999999998"},
			{"3F50624DD2F1A9FB", "9.999999999999998E-4"},
			{"0000000000000001", "4.9E-324"},
			{"0010000000000000", "2.2250738585072014E-308"},
			{"7FEFFFFFFFFFFFFF", "1.7976931348623157E308"},
			{"01A56E1FC2F8F359", "1.0E-300"},
			{"7E37E43C8800759C", "1.0E300"},
			{"42416A5D2D360000", "1.495978707E11"},
			{"4340CE3DFB912360", "9.4607304725808E15"},
			{"435B6804BE5727A2", "3.085677581491367E16"},
			{"38B57BD4AD4EFA96", "1.616255E-35"},
			{"3F50000000000000", "9.765625E-4"},
			{"3EB0000000000000", "9.5367431640625E-7"},
			{"43B0000000000000", "1.152921504606847E18"},
			{"44B52D02C7E14AF6", "1.0E23"},
			{"44C52D02C7E14AF6", "2.0E23"},
			{"3FD3333333333334", "0.30000000000000004"},
			{"3FD5555555555555", "0.3333333333333333"},
			{"400921FB54442D18", "3.141592653589793"},
			{"4005BF0A8B145769", "2.718281828459045"},
			{"3FF0000000000001", "1.0000000000000002"},
			{"3FE9999999999999", "0.7999999999999999"},
			{"438F67EA69ED3795", "2.82879384806159E17"},
			{"00000000016E3600", "1.18575755E-316"},
			{"7BE0000000000000", "4.8726570057E288"},
			{"3EE4F8B588E368F1", "1.0E-5"},
			{"44B52D02C7E14AF6", "1.0E23"},
			{"447C7E83209E90B2", "8.41E21"},
			{"0000000000000001", "4.9E-324"},
			{"4480F0CF064DD592", "1.0E22"},
			{"444B1AE4D6E2EF50", "1.0E21"},
			{"419D6F3454000000", "1.23456789E8"},
			{"3FD3333333333334", "0.30000000000000004"},
	};
	/** Entries (1-based) where JDK 17 Double.toString is not canonical. */
	private static final int[] JDK17_DIVERGENT = {25, 29, 30, 31, 38, 40, 42, 43};
	/** P-BINARY64 sweep: SHA-256 of the 400 000 canonical strings, one per line. */
	private static final String SWEEP_SHA256 =
			"5f2cec099ad0f70c4a2398e1ae676f47911c02e7e10b453876e209ba73352303";
	private static final Locale[] LOCALES = {Locale.ROOT, Locale.forLanguageTag("es-ES"),
			Locale.GERMANY, Locale.forLanguageTag("ar-EG"), Locale.forLanguageTag("hi-IN"),
			Locale.FRANCE};

	private static double value(String[] entry) {
		return Double.longBitsToDouble(Long.parseUnsignedLong(entry[0], 16));
	}

	@Test
	void referenceCorpusIsReproducedUnderEveryDefaultLocale() {
		Locale previous = Locale.getDefault();
		try {
			for (Locale locale : LOCALES) {
				Locale.setDefault(locale);
				for (String[] entry : REFERENCE) {
					assertEquals(entry[1], CanonicalBinary64.write(value(entry)),
							entry[0] + " " + locale);
				}
			}
		} finally {
			Locale.setDefault(previous);
		}
	}

	@Test
	void referenceCorpusRoundTrips() {
		for (String[] entry : REFERENCE) {
			Double parsed = CanonicalBinary64.tryParse(entry[1]);
			assertEquals(Double.doubleToRawLongBits(value(entry)),
					Double.doubleToRawLongBits(parsed), entry[1]);
		}
	}

	@Test
	void jdk17DivergentEntriesAreShortest() {
		for (int index : JDK17_DIVERGENT) {
			String canonical = REFERENCE[index - 1][1];
			String mantissa = canonical.contains("E")
					? canonical.substring(0, canonical.indexOf('E')) : canonical;
			assertTrue(mantissa.replace(".", "").replaceAll("^0+|0+$", "").length() <= 16,
					canonical);
		}
		assertEquals("1.0E23", CanonicalBinary64.write(1.0E23));
		assertEquals("4.9E-324", CanonicalBinary64.write(Double.MIN_VALUE),
				"the length-1-or-2 rule");
	}

	@Test
	void seededSweepReproducesThePreparationHashAndRoundTrips() throws Exception {
		SplittableRandom random = new SplittableRandom(20261002L);
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		for (int k = 0; k < 400_000; k++) {
			double value;
			if ((k & 1) == 0) {
				long bits;
				do {
					bits = random.nextLong() & 0x7FFFFFFFFFFFFFFFL;
				} while (bits == 0 || bits >= 0x7FF0000000000000L);
				value = Double.longBitsToDouble(bits);
			} else {
				long mantissa = 1 + random.nextLong(999_999);
				int exponent = random.nextInt(-12, 9);
				value = new BigDecimal(BigInteger.valueOf(mantissa), -exponent).doubleValue();
			}
			String canonical = CanonicalBinary64.write(value);
			Double parsed = CanonicalBinary64.tryParse(canonical);
			if (parsed == null || parsed != value) {
				throw new AssertionError("round trip failed for " + canonical);
			}
			digest.update((canonical + "\n").getBytes(StandardCharsets.US_ASCII));
		}
		StringBuilder hex = new StringBuilder();
		for (byte b : digest.digest()) {
			hex.append(Character.forDigit((b >> 4) & 0xF, 16))
					.append(Character.forDigit(b & 0xF, 16));
		}
		assertEquals(SWEEP_SHA256, hex.toString());
	}

	@Test
	void canonicalFormIsNeverLongerThanARoundingThatRoundTrips() {
		SplittableRandom random = new SplittableRandom(7L);
		for (int k = 0; k < 20_000; k++) {
			double value = Math.abs(random.nextDouble() * Math.pow(10, random.nextInt(-20,
					20)));
			if (value == 0) {
				continue;
			}
			String canonical = CanonicalBinary64.write(value);
			int digits = significantDigits(canonical);
			BigDecimal exact = new BigDecimal(value);
			for (int n = 1; n < digits; n++) {
				if (n == 1 && digits == 2) {
					continue; // the specification keeps two digits when one would do
				}
				BigDecimal rounded = exact.round(new MathContext(n, RoundingMode.HALF_EVEN));
				assertNotEquals(value, Double.parseDouble(rounded.toString()),
						canonical + " vs " + rounded);
			}
		}
	}

	private static int significantDigits(String canonical) {
		String mantissa = canonical.contains("E")
				? canonical.substring(0, canonical.indexOf('E')) : canonical;
		return new BigDecimal(mantissa).stripTrailingZeros().precision();
	}

	@Test
	void validNonCanonicalInputReadsToTheSameValueAndWritesCanonically() {
		String[][] cases = {{"0.02540", "0.0254"}, {"2.54e-5", "2.54E-5"}, {"1E7", "1.0E7"},
				{"0001.5", "1.5"}, {"1e+2", "100.0"}, {"25.4E-3", "0.0254"},
				{"0.00100", "0.001"}, {"10000000", "1.0E7"}};
		for (String[] c : cases) {
			Double parsed = CanonicalBinary64.tryParse(c[0]);
			assertEquals(c[1], CanonicalBinary64.write(parsed), c[0]);
		}
	}

	@Test
	void lexicalGrammarIsExact() {
		for (String valid : new String[] {"1", "0.5", "00.5", "1.0", "1e5", "1E5", "1e+5",
				"1e-5", "123.456E-7"}) {
			assertTrue(CanonicalBinary64.isLexical(valid), valid);
		}
		for (String invalid : new String[] {"", "+1", "-1", " 1", "1 ", "1.", ".5", "1e",
				"1E+", "1e-", "0x1p3", "0x10", "NaN", "Infinity", "1d", "1f", "1D", "1,5",
				"1_000", "1e1.5", "1..2", "\u0661", "1\u00A0", "1.5.5", "e5", "1ee5"}) {
			assertFalse(CanonicalBinary64.isLexical(invalid), invalid);
			assertNull(CanonicalBinary64.tryParse(invalid), invalid);
		}
		for (String outOfRange : new String[] {"0", "0.0", "000", "1e400", "1e-400",
				"0e5"}) {
			assertTrue(CanonicalBinary64.isLexical(outOfRange), outOfRange);
			assertNull(CanonicalBinary64.tryParse(outOfRange), outOfRange);
		}
	}

	@Test
	void writerRejectsInvalidFactors() {
		for (double invalid : new double[] {0, -0.0, -1, Double.NaN,
				Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
			assertThrows(IllegalArgumentException.class,
					() -> CanonicalBinary64.write(invalid));
		}
	}

	@Test
	void writingIsDeterministic() {
		for (String[] entry : REFERENCE) {
			assertEquals(CanonicalBinary64.write(value(entry)),
					CanonicalBinary64.write(value(entry)));
		}
	}
}
