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
	private final SpatialRedefineSignature assessmentSignature;
	private final long definitionRevision;
	private final long topologyRevision;
	private final SpatialRedefineHostState hostState;

	/** Creates one immutable old-output authority value. */
	public SpatialRedefinePersistedOutput(GeoElement geo, PersistentGeoId id,
			SpatialRedefineSignature signature, long definitionRevision,
			long topologyRevision) {
		this(geo, id, signature, signature, definitionRevision, topologyRevision);
	}

	/**
	 * Creates one old-output authority value whose persisted signature differs from
	 * the signature it is compared under. A historical construction record keeps
	 * its persisted version-1 signature for currentness, while assessment compares
	 * its lifted current-rule signature (PRE-G9B-R3 lazy migration).
	 */
	SpatialRedefinePersistedOutput(GeoElement geo, PersistentGeoId id,
			SpatialRedefineSignature signature,
			SpatialRedefineSignature assessmentSignature, long definitionRevision,
			long topologyRevision) {
		this.geo = Objects.requireNonNull(geo);
		this.id = Objects.requireNonNull(id);
		this.signature = Objects.requireNonNull(signature);
		this.assessmentSignature = Objects.requireNonNull(assessmentSignature);
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

	/**
	 * @return the signature under the current durable-dependency rule, used only to
	 *         compare this old output with a candidate
	 */
	public SpatialRedefineSignature getAssessmentSignature() {
		return assessmentSignature;
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
