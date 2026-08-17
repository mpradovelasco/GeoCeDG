/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Durable identity of an inert projection-frame record. */
public final class ProjectionFrameId extends SpatialIdentityId {
	/** Creates an ID from a canonical raw token. */
	public ProjectionFrameId(String rawToken) {
		super(SpatialIdentityKind.PROJECTION_FRAME, rawToken);
	}

	/** @return the strictly parsed projection-frame ID */
	public static ProjectionFrameId parse(String externalForm) {
		return new ProjectionFrameId(parseToken(externalForm,
				SpatialIdentityKind.PROJECTION_FRAME));
	}
}
