/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Globally distinct kinds in one construction's spatial identity namespace. */
public enum SpatialIdentityKind {
	GEO("geo"),
	SPATIAL_OBJECT("object"),
	PROJECTION_FRAME("frame"),
	PROJECTION_SYSTEM("system"),
	PROJECTION_DIAGRAM_MAP("map"),
	PROJECTION_FRAME_RELATION("relation"),
	PROJECTION_BINDING("binding");

	private final String prefix;

	SpatialIdentityKind(String prefix) {
		this.prefix = prefix;
	}

	/** @return the canonical external kind prefix */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Resolves an external prefix without accepting aliases.
	 *
	 * @param prefix canonical external prefix
	 * @return the matching globally declared kind
	 */
	public static SpatialIdentityKind fromPrefix(String prefix) {
		for (SpatialIdentityKind kind : values()) {
			if (kind.prefix.equals(prefix)) {
				return kind;
			}
		}
		throw new IllegalArgumentException("Unknown spatial identity kind: " + prefix);
	}
}
