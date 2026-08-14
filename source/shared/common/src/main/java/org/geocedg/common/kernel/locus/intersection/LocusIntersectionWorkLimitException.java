/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Internal control-flow exception for an approved deterministic work ceiling. */
public final class LocusIntersectionWorkLimitException
		extends RuntimeException {

	/** Creates a typed work-limit exception. */
	public LocusIntersectionWorkLimitException(String quantity) {
		super("Intersection work limit reached: " + quantity);
	}
}
