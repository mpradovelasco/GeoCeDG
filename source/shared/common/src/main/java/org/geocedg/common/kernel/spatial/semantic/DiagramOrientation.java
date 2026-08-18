/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Declared orientation of an intrinsic-to-diagram map. */
public enum DiagramOrientation {
	PRESERVING(1),
	REVERSING(-1);

	private final int determinantSign;

	DiagramOrientation(int determinantSign) {
		this.determinantSign = determinantSign;
	}

	public int getDeterminantSign() {
		return determinantSign;
	}
}
