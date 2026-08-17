/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Durable identity of an inert projection-system diagram map. */
public final class ProjectionDiagramMapId extends SpatialIdentityId {
	/** Creates an ID from a canonical raw token. */
	public ProjectionDiagramMapId(String rawToken) {
		super(SpatialIdentityKind.PROJECTION_DIAGRAM_MAP, rawToken);
	}

	/** @return the strictly parsed diagram-map ID */
	public static ProjectionDiagramMapId parse(String externalForm) {
		return new ProjectionDiagramMapId(parseToken(externalForm,
				SpatialIdentityKind.PROJECTION_DIAGRAM_MAP));
	}
}
