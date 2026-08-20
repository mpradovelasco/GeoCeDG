/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.function.Function;

import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Narrow post-commit hook for geos whose reconstructible parent needs the
 * current lifecycle-owned identity before it may publish derived semantics.
 */
public interface PersistentGeoIdentityListener {
	/**
	 * Validates one prospective attachment without mutating the geo, registry or
	 * derived runtime state. Any rejection aborts the enclosing identity batch
	 * before graph installation.
	 *
	 * @param attachedId prospective attached identity
	 * @param attachedRecord prospective attached record
	 * @param prospectiveRecord exact staged-or-current geo record resolver
	 * @param immediateCopy whether this batch is an authorized copy publication
	 */
	default void validatePersistentGeoIdentityAttachment(
			PersistentGeoId attachedId, GeoIdentityRecord attachedRecord,
			Function<GeoElement, GeoIdentityRecord> prospectiveRecord,
			boolean immediateCopy) {
		// Most geos have no additional preattachment persistence context.
	}

	/**
	 * Called after the identity graph/runtime commit. Implementations must not
	 * throw; failure is represented by an undefined derived geo.
	 *
	 * @param attachedId current attached identity
	 */
	void onPersistentGeoIdentityAttached(PersistentGeoId attachedId);
}
