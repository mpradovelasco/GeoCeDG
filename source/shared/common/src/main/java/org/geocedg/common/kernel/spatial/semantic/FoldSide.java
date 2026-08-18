/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Declared relative side of the two unfolded frame half-planes. */
public enum FoldSide {
	SAME_DIAGRAM_SIDE(1),
	OPPOSITE_DIAGRAM_SIDE(-1),
	NOT_APPLICABLE(0);

	private final int expectedSign;

	FoldSide(int expectedSign) {
		this.expectedSign = expectedSign;
	}

	public int getExpectedSign() {
		return expectedSign;
	}
}
