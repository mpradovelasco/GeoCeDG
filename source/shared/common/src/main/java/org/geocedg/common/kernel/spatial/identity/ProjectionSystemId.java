/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Durable identity of an inert projection-system record. */
public final class ProjectionSystemId extends SpatialIdentityId {
	/** Creates an ID from a canonical raw token. */
	public ProjectionSystemId(String rawToken) {
		super(SpatialIdentityKind.PROJECTION_SYSTEM, rawToken);
	}

	/** @return the strictly parsed projection-system ID */
	public static ProjectionSystemId parse(String externalForm) {
		return new ProjectionSystemId(parseToken(externalForm,
				SpatialIdentityKind.PROJECTION_SYSTEM));
	}
}
