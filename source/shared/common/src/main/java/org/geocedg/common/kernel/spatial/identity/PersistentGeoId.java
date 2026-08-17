/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

/** Durable identity assigned only to an explicitly participating kernel geo. */
public final class PersistentGeoId extends SpatialIdentityId {
	/** Creates an ID from a canonical raw token. */
	public PersistentGeoId(String rawToken) {
		super(SpatialIdentityKind.GEO, rawToken);
	}

	/** @return the strictly parsed participating-geo ID */
	public static PersistentGeoId parse(String externalForm) {
		return new PersistentGeoId(parseToken(externalForm, SpatialIdentityKind.GEO));
	}
}
