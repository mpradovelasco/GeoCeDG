/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Orientation of an explicitly constructed frame-relation support line. */
public enum ProjectionRelationOrientation {
	POSITIVE(1),
	NEGATIVE(-1);

	private final int directionSign;

	ProjectionRelationOrientation(int directionSign) {
		this.directionSign = directionSign;
	}

	public int getDirectionSign() {
		return directionSign;
	}
}
