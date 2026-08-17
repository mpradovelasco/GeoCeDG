/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Durable identity of an inert spatial-object record. */
public final class SpatialObjectId extends SpatialIdentityId {
	/** Creates an ID from a canonical raw token. */
	public SpatialObjectId(String rawToken) {
		super(SpatialIdentityKind.SPATIAL_OBJECT, rawToken);
	}

	/** @return the strictly parsed spatial-object ID */
	public static SpatialObjectId parse(String externalForm) {
		return new SpatialObjectId(parseToken(externalForm,
				SpatialIdentityKind.SPATIAL_OBJECT));
	}
}
