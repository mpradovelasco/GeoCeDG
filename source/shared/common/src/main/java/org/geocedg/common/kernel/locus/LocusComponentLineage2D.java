/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

/** Canonical durable lineage key for one semantic branch component. */
public final class LocusComponentLineage2D {
	private LocusComponentLineage2D() {
		// Utility class.
	}

	/**
	 * Builds a revision-independent key from approved semantic component data.
	 *
	 * @return canonical component-lineage key
	 */
	public static String create(String branchKey, LocusInterval2D component) {
		String branch = Objects.requireNonNull(branchKey);
		if (branch.trim().isEmpty() || !branch.equals(branch.trim())) {
			throw new IllegalArgumentException("Branch key must be canonical");
		}
		LocusInterval2D interval = Objects.requireNonNull(component);
		return branch + "/component/"
				+ Double.toHexString(interval.getLower()) + "/"
				+ Double.toHexString(interval.getUpper()) + "/"
				+ (interval.isLowerClosed() ? "closed" : "open") + "/"
				+ (interval.isUpperClosed() ? "closed" : "open");
	}
}
