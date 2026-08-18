/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Internal constructor checks shared by immutable semantic value types. */
final class SemanticValueChecks {
	private SemanticValueChecks() {
	}

	static long requireRevision(long revision) {
		if (revision < 0) {
			throw new IllegalArgumentException("revision must be non-negative");
		}
		return revision;
	}

	static String requireUnit(String unit) {
		if (unit == null || unit.trim().isEmpty()) {
			throw new IllegalArgumentException("unit must not be blank");
		}
		if (!unit.equals(unit.trim())) {
			throw new IllegalArgumentException("unit must use canonical spelling");
		}
		return unit;
	}

	static boolean sameUnit(String first, String second) {
		return first.equals(second);
	}
}
