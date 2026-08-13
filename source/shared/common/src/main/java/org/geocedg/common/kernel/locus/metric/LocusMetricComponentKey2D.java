/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/** Deterministic revision-scoped valid-component key utility. */
public final class LocusMetricComponentKey2D {
	private LocusMetricComponentKey2D() {
		// Utility class.
	}

	/**
	 * Builds a key that is deliberately not durable semantic position identity.
	 *
	 * @return deterministic revision-scoped component key
	 */
	public static String create(LocusDefinition2D definition,
			LocusBranch2D branch, int componentIndex) {
		LocusInterval2D interval =
				branch.getValidDomainComponents().get(componentIndex);
		return branch.getBranchKey() + "|r=" + definition.getSemanticRevision()
				+ "|c=" + componentIndex + "|"
				+ Double.toHexString(interval.getLower()) + ":"
				+ Double.toHexString(interval.getUpper()) + ":"
				+ interval.isLowerClosed() + ":" + interval.isUpperClosed();
	}

	/**
	 * Finds the exact interval owned by a component key, or {@code null}.
	 *
	 * @return matching component extent or {@code null}
	 */
	public static LocusInterval2D find(LocusDefinition2D definition,
			LocusBranch2D branch, String componentKey) {
		for (int index = 0; index < branch.getValidDomainComponents().size();
				index++) {
			if (create(definition, branch, index).equals(componentKey)) {
				return branch.getValidDomainComponents().get(index);
			}
		}
		return null;
	}

	/**
	 * Finds a component's deterministic index, or {@code -1}.
	 *
	 * @return matching component index or {@code -1}
	 */
	public static int indexOf(LocusDefinition2D definition,
			LocusBranch2D branch, String componentKey) {
		for (int index = 0; index < branch.getValidDomainComponents().size();
				index++) {
			if (create(definition, branch, index).equals(componentKey)) {
				return index;
			}
		}
		return -1;
	}
}
