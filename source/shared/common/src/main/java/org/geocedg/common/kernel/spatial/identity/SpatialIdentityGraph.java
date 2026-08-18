/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.List;

import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Read-only identity graph consumed by semantic topology preparation.
 *
 * <p>The live registry and an immutable prospective lifecycle graph implement
 * the same view. Semantic preparation can therefore inspect a candidate graph
 * without temporarily publishing it.</p>
 */
public interface SpatialIdentityGraph {
	/** @return all records in deterministic kind-and-ID order */
	List<SpatialIdentityRecord> getRecords();

	/** @return the record for the typed identity, or {@code null} */
	SpatialIdentityRecord getRecord(SpatialIdentityId id);

	/** @return current reference-resolution evidence, or {@code null} */
	SpatialRecordResolution getResolution(SpatialIdentityId id);

	/** @return the ordinary geo attached to the durable identity, or {@code null} */
	GeoElement getGeo(PersistentGeoId id);

	/** @return the durable identity attached to the exact geo, or {@code null} */
	PersistentGeoId getPersistentGeoId(GeoElement geo);
}
