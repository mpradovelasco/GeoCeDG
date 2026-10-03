/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.units;

import java.util.Objects;

/**
 * The one document-scoped user-defined unit {@code 1 usm = k m} (unit-system v1.0,
 * section 4). Its semantic identity is the token {@code usm} plus the binary64 factor
 * {@code k}; the optional name and symbol are presentation metadata. Instances are
 * immutable and always valid.
 */
public final class UsmDefinition {
	/** Maximum number of Unicode scalar values of a name or symbol. */
	public static final int MAX_LABEL_LENGTH = 64;

	private final double metresPerUnit;
	private final String name;
	private final String symbol;

	private UsmDefinition(double metresPerUnit, String name, String symbol) {
		this.metresPerUnit = metresPerUnit;
		this.name = name;
		this.symbol = symbol;
	}

	/**
	 * @param metresPerUnit factor k, finite and greater than zero
	 * @param name optional name, or {@code null}
	 * @param symbol optional symbol, or {@code null}
	 * @return the definition
	 * @throws IllegalArgumentException when the factor, name or symbol is invalid
	 */
	public static UsmDefinition of(double metresPerUnit, String name, String symbol) {
		if (!CanonicalBinary64.isValidFactor(metresPerUnit)) {
			throw new IllegalArgumentException("usm factor must be finite and > 0");
		}
		if (name != null && !isValidLabel(name)) {
			throw new IllegalArgumentException("Invalid usm name");
		}
		if (symbol != null && !isValidLabel(symbol)) {
			throw new IllegalArgumentException("Invalid usm symbol");
		}
		return new UsmDefinition(metresPerUnit, name, symbol);
	}

	/**
	 * Section 4.4: 1 to 64 Unicode scalar values, no control character (general
	 * category Cc), no leading or trailing white space (Unicode White_Space).
	 *
	 * @param label candidate name or symbol
	 * @return whether it is valid
	 */
	public static boolean isValidLabel(String label) {
		if (label == null || label.isEmpty()) {
			return false;
		}
		int count = 0;
		int first = -1;
		int last = -1;
		int index = 0;
		while (index < label.length()) {
			char c = label.charAt(index);
			int codePoint;
			if (Character.isHighSurrogate(c) && index + 1 < label.length()
					&& Character.isLowSurrogate(label.charAt(index + 1))) {
				codePoint = Character.toCodePoint(c, label.charAt(index + 1));
				index += 2;
			} else if (Character.isSurrogate(c)) {
				return false; // an unpaired surrogate is not a scalar value
			} else {
				codePoint = c;
				index++;
			}
			if (isControl(codePoint)) {
				return false;
			}
			if (first < 0) {
				first = codePoint;
			}
			last = codePoint;
			count++;
		}
		return count <= MAX_LABEL_LENGTH && !isWhiteSpace(first) && !isWhiteSpace(last);
	}

	private static boolean isControl(int codePoint) {
		return codePoint <= 0x1F || (codePoint >= 0x7F && codePoint <= 0x9F);
	}

	/** Unicode White_Space property (PropList), independent of the JDK version. */
	private static boolean isWhiteSpace(int codePoint) {
		return (codePoint >= 0x09 && codePoint <= 0x0D) || codePoint == 0x20
				|| codePoint == 0x85 || codePoint == 0xA0 || codePoint == 0x1680
				|| (codePoint >= 0x2000 && codePoint <= 0x200A) || codePoint == 0x2028
				|| codePoint == 0x2029 || codePoint == 0x202F || codePoint == 0x205F
				|| codePoint == 0x3000;
	}

	/**
	 * @return factor k: one usm in metres, a binary64 value
	 */
	public double getMetresPerUnit() {
		return metresPerUnit;
	}

	/**
	 * @return optional name, or {@code null}
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return optional symbol, or {@code null}
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * @return the presentation suffix: the symbol, else the literal {@code usm}
	 */
	public String displaySymbol() {
		return symbol == null ? UnitToken.USM.token() : symbol;
	}

	/**
	 * @return canonical decimal form of the factor (section 8.4)
	 */
	public String canonicalFactor() {
		return CanonicalBinary64.write(metresPerUnit);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UsmDefinition)) {
			return false;
		}
		UsmDefinition definition = (UsmDefinition) other;
		return metresPerUnit == definition.metresPerUnit
				&& Objects.equals(name, definition.name)
				&& Objects.equals(symbol, definition.symbol);
	}

	@Override
	public int hashCode() {
		return Objects.hash(metresPerUnit, name, symbol);
	}

	@Override
	public String toString() {
		return "usm(" + canonicalFactor() + " m" + (name == null ? "" : ", " + name)
				+ (symbol == null ? "" : ", " + symbol) + ")";
	}
}
