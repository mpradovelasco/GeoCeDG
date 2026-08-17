/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Durable identity of an inert map-to-map frame relation. */
public final class ProjectionFrameRelationId extends SpatialIdentityId {
	/** Creates an ID from a canonical raw token. */
	public ProjectionFrameRelationId(String rawToken) {
		super(SpatialIdentityKind.PROJECTION_FRAME_RELATION, rawToken);
	}

	/** @return the strictly parsed frame-relation ID */
	public static ProjectionFrameRelationId parse(String externalForm) {
		return new ProjectionFrameRelationId(parseToken(externalForm,
				SpatialIdentityKind.PROJECTION_FRAME_RELATION));
	}
}
