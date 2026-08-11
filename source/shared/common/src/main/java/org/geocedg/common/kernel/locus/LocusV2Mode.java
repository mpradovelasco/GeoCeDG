/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/** Internal diagnostic seam; it never redirects the public Locus command. */
public enum LocusV2Mode {
	LEGACY, V2, DUAL;

	/**
	 * Parses an external developer setting; absent or invalid means LEGACY.
	 *
	 * @return diagnostic mode, defaulting safely to legacy
	 */
	public static LocusV2Mode parse(String value) {
		if (value == null) {
			return LEGACY;
		}
		try {
			return valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException exception) {
			return LEGACY;
		}
	}
}
