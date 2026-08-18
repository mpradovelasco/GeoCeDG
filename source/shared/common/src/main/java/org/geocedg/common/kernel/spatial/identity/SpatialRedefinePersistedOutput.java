/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

import org.geogebra.common.kernel.geos.GeoElement;

/** Frozen persisted authority for one participating old output. */
public final class SpatialRedefinePersistedOutput implements SpatialRedefineOutput {
	private final GeoElement geo;
	private final PersistentGeoId id;
	private final SpatialRedefineSignature signature;
	private final long definitionRevision;
	private final long topologyRevision;
	private final SpatialRedefineHostState hostState;

	/** Creates one immutable old-output authority value. */
	public SpatialRedefinePersistedOutput(GeoElement geo, PersistentGeoId id,
			SpatialRedefineSignature signature, long definitionRevision,
			long topologyRevision) {
		this.geo = Objects.requireNonNull(geo);
		this.id = Objects.requireNonNull(id);
		this.signature = Objects.requireNonNull(signature);
		this.definitionRevision = SpatialRecordSupport.requireRevision(
				definitionRevision, "definitionRevision");
		this.topologyRevision = SpatialRecordSupport.requireRevision(
				topologyRevision, "topologyRevision");
		hostState = SpatialRedefineHostState.capture(geo);
	}

	@Override
	public GeoElement getGeo() {
		return geo;
	}

	public PersistentGeoId getId() {
		return id;
	}

	@Override
	public SpatialRedefineSignature getSignature() {
		return signature;
	}

	public long getDefinitionRevision() {
		return definitionRevision;
	}

	public long getTopologyRevision() {
		return topologyRevision;
	}

	boolean hasSameHostState(SpatialRedefineCandidateOutput candidate) {
		return hostState.equals(Objects.requireNonNull(candidate).getHostState());
	}
}
