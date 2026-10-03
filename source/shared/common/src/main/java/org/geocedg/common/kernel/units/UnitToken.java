/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.units;

/**
 * Physical-unit tokens of the GeoCeDG unit system (unit-system v1.0, section 3.1).
 * {@code UNSPECIFIED_MODEL_UNIT} is a state, never a token: it is the absence of a
 * construction-unit selection.
 */
public enum UnitToken {
	/** Millimetre, exactly 10^-3 m. */
	MM("mm", 0.001),
	/** Centimetre, exactly 10^-2 m. */
	CM("cm", 0.01),
	/** Metre. */
	M("m", 1.0),
	/** The one document-scoped user-defined unit; its factor is the document's. */
	USM("usm", Double.NaN);

	private final String token;
	/** The binary64 nearest to the exact SI factor (section 3.2); NaN for usm. */
	private final double builtInMetresPerUnit;

	UnitToken(String token, double builtInMetresPerUnit) {
		this.token = token;
		this.builtInMetresPerUnit = builtInMetresPerUnit;
	}

	/**
	 * @return the serialized, case-sensitive token
	 */
	public String token() {
		return token;
	}

	/**
	 * @return whether this unit has a fixed SI definition
	 */
	public boolean isBuiltIn() {
		return this != USM;
	}

	/**
	 * @return the binary64 metre factor of a built-in unit
	 * @throws IllegalStateException for {@link #USM}, whose factor is document-scoped
	 */
	public double builtInMetresPerUnit() {
		if (!isBuiltIn()) {
			throw new IllegalStateException("usm has a document-scoped factor");
		}
		return builtInMetresPerUnit;
	}

	/**
	 * @param token serialized token, compared case-sensitively
	 * @return the unit, or {@code null} when the token is not a unit token
	 */
	public static UnitToken fromToken(String token) {
		if (token == null) {
			return null;
		}
		for (UnitToken unit : values()) {
			if (unit.token.equals(token)) {
				return unit;
			}
		}
		return null;
	}
}
