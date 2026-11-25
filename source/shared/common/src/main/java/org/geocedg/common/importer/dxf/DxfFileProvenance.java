/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

import javax.annotation.CheckForNull;

/**
 * File-level provenance shared by every record of one import (section 9): the SHA-256 of the
 * file bytes, the base name only (a full path is session information and is never kept
 * here), {@code $ACADVER} or {@code UNKNOWN}, {@code $INSUNITS} with its name, and the
 * document construction unit at import. Nothing depends on the time of import.
 */
public final class DxfFileProvenance {

	/** Version recorded when {@code $ACADVER} is missing. */
	public static final String UNKNOWN_VERSION = "UNKNOWN";
	/** Document unit recorded for {@code UNSPECIFIED_MODEL_UNIT}. */
	public static final String UNSPECIFIED_MODEL_UNIT = "UNSPECIFIED_MODEL_UNIT";

	private static final String[] INSUNITS_NAMES = {"Unitless", "Inches", "Feet", "Miles",
		"Millimeters", "Centimeters", "Meters", "Kilometers", "Microinches", "Mils", "Yards",
		"Angstroms", "Nanometers", "Microns", "Decimeters", "Decameters", "Hectometers",
		"Gigameters", "Astronomical units", "Light years", "Parsecs", "US Survey Feet",
		"US Survey Inch", "US Survey Yard", "US Survey Mile"};

	private final String fileSha256;
	private final String fileBaseName;
	private final String acadVersion;
	private final @CheckForNull Integer insUnits;
	private final String documentUnit;

	DxfFileProvenance(String fileSha256, String fileBaseName, String acadVersion,
			@CheckForNull Integer insUnits, String documentUnit) {
		this.fileSha256 = fileSha256;
		this.fileBaseName = fileBaseName;
		this.acadVersion = acadVersion;
		this.insUnits = insUnits;
		this.documentUnit = documentUnit;
	}

	/**
	 * @return SHA-256 of the file bytes, 64 lowercase hexadecimal digits
	 */
	public String fileSha256() {
		return fileSha256;
	}

	/**
	 * @return base name of the file
	 */
	public String fileBaseName() {
		return fileBaseName;
	}

	/**
	 * @return {@code $ACADVER} as read, or {@link #UNKNOWN_VERSION}
	 */
	public String acadVersion() {
		return acadVersion;
	}

	/**
	 * @return {@code $INSUNITS} code, or {@code null} when absent
	 */
	public @CheckForNull Integer insUnits() {
		return insUnits;
	}

	/**
	 * @return the name of the {@code $INSUNITS} code ("absent" or "code n" when unknown)
	 */
	public String insUnitsName() {
		if (insUnits == null) {
			return "absent";
		}
		int code = insUnits;
		return code >= 0 && code < INSUNITS_NAMES.length ? INSUNITS_NAMES[code]
				: "code " + code;
	}

	/**
	 * @return document construction unit token at import, or {@link #UNSPECIFIED_MODEL_UNIT}
	 */
	public String documentUnit() {
		return documentUnit;
	}

	/**
	 * Section 6: both units specified and different. {@code $INSUNITS} 4, 5 and 6 match the
	 * document units {@code mm}, {@code cm} and {@code m}; every other specified pair,
	 * {@code usm} included, differs.
	 *
	 * @return whether the non-blocking units notice applies
	 */
	public boolean unitsDiffer() {
		if (insUnits == null || insUnits == 0 || UNSPECIFIED_MODEL_UNIT.equals(documentUnit)) {
			return false;
		}
		String matching = insUnits == 4 ? "mm" : insUnits == 5 ? "cm" : insUnits == 6 ? "m"
				: null;
		return !documentUnit.equals(matching);
	}
}
