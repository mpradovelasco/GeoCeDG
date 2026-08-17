/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Durable identity of an inert object-to-projection binding. */
public final class ProjectionBindingId extends SpatialIdentityId {
	/** Creates an ID from a canonical raw token. */
	public ProjectionBindingId(String rawToken) {
		super(SpatialIdentityKind.PROJECTION_BINDING, rawToken);
	}

	/** @return the strictly parsed projection-binding ID */
	public static ProjectionBindingId parse(String externalForm) {
		return new ProjectionBindingId(parseToken(externalForm,
				SpatialIdentityKind.PROJECTION_BINDING));
	}
}
