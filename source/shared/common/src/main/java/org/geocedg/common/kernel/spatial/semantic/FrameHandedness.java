/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Declared orientation of a frame's ordered in-plane basis. */
public enum FrameHandedness {
	RIGHT_HANDED(1),
	LEFT_HANDED(-1);

	private final int sign;

	FrameHandedness(int sign) {
		this.sign = sign;
	}

	public int getSign() {
		return sign;
	}
}
