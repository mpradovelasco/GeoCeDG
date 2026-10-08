/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.units;

import java.util.Map;

import org.geocedg.common.kernel.units.UnitMetadataException.Code;
import org.geogebra.common.io.XMLStringBuilder;

/**
 * Writer and attribute reader of the flat, versioned, self-closing
 * {@code <geocedgUnits/>} element (unit-system v1.0, sections 8.2 to 8.4 and 8.7).
 * The writer emits nothing for {@code EMPTY}, the attributes in the order of section
 * 8.2 and the factor in its canonical form; it cannot fail for a valid state. The
 * reader validates a recognized element completely or throws
 * {@link UnitMetadataException}.
 *
 * <p>Unit-system v1.1: version 2 is version 1 plus the mandatory
 * {@code dimensionUnitSuffix} attribute (section 14.2). The writer emits version 2 only
 * for a hidden suffix and otherwise exactly the version-1 element, so every document
 * without the hidden policy keeps its version-1 bytes.
 */
public final class UnitStateXml {
	/** Element name. */
	public static final String ELEMENT = "geocedgUnits";
	/** Highest supported version. */
	public static final int VERSION = 2;
	/** Version of an element without the dimension unit suffix policy. */
	public static final int VERSION_1 = 1;

	static final String VERSION_ATTRIBUTE = "version";
	static final String CONSTRUCTION_ATTRIBUTE = "construction";
	static final String PRESENTATION_ATTRIBUTE = "presentation";
	static final String FACTOR_ATTRIBUTE = "usmMetersPerUnit";
	static final String NAME_ATTRIBUTE = "usmName";
	static final String SYMBOL_ATTRIBUTE = "usmSymbol";
	static final String SUFFIX_ATTRIBUTE = "dimensionUnitSuffix";
	static final String SUFFIX_HIDDEN = "hidden";
	static final String SUFFIX_SHOWN = "shown";

	private UnitStateXml() {
	}

	/**
	 * Appends the element on its own line, or nothing for {@code EMPTY} (section 8.3).
	 *
	 * @param sb construction XML builder
	 * @param state state to write
	 */
	public static void write(XMLStringBuilder sb, UnitState state) {
		if (state.isEmpty()) {
			return;
		}
		boolean hidden = !state.isDimensionUnitSuffixShown();
		sb.startTag(ELEMENT);
		sb.attr(VERSION_ATTRIBUTE, hidden ? VERSION : VERSION_1);
		if (state.getConstructionSelection() != null) {
			sb.attr(CONSTRUCTION_ATTRIBUTE, state.getConstructionSelection().token());
			if (state.getPresentationSelection() != null) {
				sb.attr(PRESENTATION_ATTRIBUTE, state.getPresentationSelection().token());
			}
		}
		UsmDefinition usm = state.getUsm();
		if (usm != null) {
			sb.attr(FACTOR_ATTRIBUTE, usm.canonicalFactor());
			if (usm.getName() != null) {
				sb.attr(NAME_ATTRIBUTE, usm.getName());
			}
			if (usm.getSymbol() != null) {
				sb.attr(SYMBOL_ATTRIBUTE, usm.getSymbol());
			}
		}
		if (hidden) {
			sb.attr(SUFFIX_ATTRIBUTE, SUFFIX_HIDDEN);
		}
		sb.endTag();
	}

	/**
	 * Reads one recognized element (section 8.7); a newer version is rejected before
	 * any other attribute is interpreted.
	 *
	 * @param attributes element attributes
	 * @return the validated state
	 * @throws UnitMetadataException for every fail-closed case of section 8.7
	 */
	public static UnitState read(Map<String, String> attributes) {
		int version = readVersion(attributes.get(VERSION_ATTRIBUTE));
		if (version > VERSION) {
			throw new UnitMetadataException(Code.UNSUPPORTED_VERSION,
					"version " + attributes.get(VERSION_ATTRIBUTE) + " is newer than "
							+ VERSION);
		}
		for (String name : attributes.keySet()) {
			if (!isKnownAttribute(name, version)) {
				throw new UnitMetadataException(Code.MALFORMED_ELEMENT,
						"unknown attribute " + name);
			}
		}
		boolean suffixShown = true;
		if (version >= VERSION) {
			String suffix = attributes.get(SUFFIX_ATTRIBUTE);
			if (SUFFIX_HIDDEN.equals(suffix)) {
				suffixShown = false;
			} else if (!SUFFIX_SHOWN.equals(suffix)) {
				throw new UnitMetadataException(Code.MALFORMED_ELEMENT,
						suffix == null ? "version 2 without " + SUFFIX_ATTRIBUTE
								: SUFFIX_ATTRIBUTE + " is not hidden or shown: " + suffix);
			}
		}
		UnitToken construction = readToken(attributes, CONSTRUCTION_ATTRIBUTE);
		UnitToken presentation = readToken(attributes, PRESENTATION_ATTRIBUTE);
		if (presentation != null && construction == null) {
			throw new UnitMetadataException(Code.MALFORMED_ELEMENT,
					"presentation without construction");
		}
		String factorText = attributes.get(FACTOR_ATTRIBUTE);
		String name = attributes.get(NAME_ATTRIBUTE);
		String symbol = attributes.get(SYMBOL_ATTRIBUTE);
		UsmDefinition usm = null;
		if (factorText != null) {
			Double factor = CanonicalBinary64.tryParse(factorText);
			if (factor == null) {
				throw new UnitMetadataException(Code.INVALID_USM_FACTOR,
						"invalid usmMetersPerUnit " + factorText);
			}
			if (name != null && !UsmDefinition.isValidLabel(name)) {
				throw new UnitMetadataException(Code.MALFORMED_ELEMENT, "invalid usmName");
			}
			if (symbol != null && !UsmDefinition.isValidLabel(symbol)) {
				throw new UnitMetadataException(Code.MALFORMED_ELEMENT,
						"invalid usmSymbol");
			}
			usm = UsmDefinition.of(factor, name, symbol);
		} else if (name != null || symbol != null) {
			throw new UnitMetadataException(Code.MALFORMED_ELEMENT,
					"usmName or usmSymbol without usmMetersPerUnit");
		}
		if ((construction == UnitToken.USM || presentation == UnitToken.USM)
				&& usm == null) {
			throw new UnitMetadataException(Code.INVALID_USM_FACTOR,
					"usm is selected without usmMetersPerUnit");
		}
		// A bare version-1 element is grammatical and denotes EMPTY (section 8.2).
		return UnitState.of(construction, presentation, usm, suffixShown);
	}

	private static int readVersion(String text) {
		if (text == null || text.isEmpty()) {
			throw new UnitMetadataException(Code.MALFORMED_ELEMENT, "missing version");
		}
		long value = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c < '0' || c > '9') {
				throw new UnitMetadataException(Code.MALFORMED_ELEMENT,
						"non-integer version " + text);
			}
			value = Math.min(Integer.MAX_VALUE, value * 10 + (c - '0'));
		}
		if (value < 1) {
			throw new UnitMetadataException(Code.MALFORMED_ELEMENT, "version " + text);
		}
		return (int) value;
	}

	private static boolean isKnownAttribute(String name, int version) {
		return VERSION_ATTRIBUTE.equals(name) || CONSTRUCTION_ATTRIBUTE.equals(name)
				|| PRESENTATION_ATTRIBUTE.equals(name) || FACTOR_ATTRIBUTE.equals(name)
				|| NAME_ATTRIBUTE.equals(name) || SYMBOL_ATTRIBUTE.equals(name)
				|| (version >= VERSION && SUFFIX_ATTRIBUTE.equals(name));
	}

	private static UnitToken readToken(Map<String, String> attributes, String attribute) {
		String text = attributes.get(attribute);
		if (text == null) {
			return null;
		}
		UnitToken token = UnitToken.fromToken(text);
		if (token == null) {
			throw new UnitMetadataException(Code.MALFORMED_ELEMENT,
					attribute + " is not a unit token: " + text);
		}
		return token;
	}
}
