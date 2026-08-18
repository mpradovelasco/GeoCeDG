/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Inert identity and typed-reference record for one diagram map. */
public final class ProjectionDiagramMapRecord implements SpatialIdentityRecord {
	private final ProjectionDiagramMapId id;
	private final int semanticVersion;
	private final ProjectionSystemId systemId;
	private final ProjectionFrameId frameId;
	private final ProjectionFrameUseRole frameUseRole;
	private final String family;
	private final String orientation;
	private final String units;
	private final String fidelity;
	private final List<ProjectionFrameRelationId> relationIds;
	private final List<PersistentGeoId> definitionGeoIds;
	private final PersistentGeoId a00GeoId;
	private final PersistentGeoId a01GeoId;
	private final PersistentGeoId a10GeoId;
	private final PersistentGeoId a11GeoId;
	private final PersistentGeoId b0GeoId;
	private final PersistentGeoId b1GeoId;
	private final PersistentGeoId declaredScaleGeoId;
	private final long revision;
	private final ProjectionDiagramMapId copySourceId;

	/** Creates an original inert diagram-map record. */
	public ProjectionDiagramMapRecord(ProjectionDiagramMapId id, int semanticVersion,
			ProjectionSystemId systemId, ProjectionFrameId frameId,
			ProjectionFrameUseRole frameUseRole, String family,
			List<ProjectionFrameRelationId> relationIds,
			List<PersistentGeoId> definitionGeoIds, long revision) {
		this(id, semanticVersion, systemId, frameId, frameUseRole, family, relationIds,
				definitionGeoIds, revision, null);
	}

	/** Creates an inert diagram-map record with optional immediate copy lineage. */
	public ProjectionDiagramMapRecord(ProjectionDiagramMapId id, int semanticVersion,
			ProjectionSystemId systemId, ProjectionFrameId frameId,
			ProjectionFrameUseRole frameUseRole, String family,
			List<ProjectionFrameRelationId> relationIds,
			List<PersistentGeoId> definitionGeoIds, long revision,
			ProjectionDiagramMapId copySourceId) {
		if (semanticVersion != 1) {
			throw new IllegalArgumentException(
					"The inert diagram-map constructor requires semanticVersion 1");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.systemId = Objects.requireNonNull(systemId);
		this.frameId = Objects.requireNonNull(frameId);
		this.frameUseRole = Objects.requireNonNull(frameUseRole);
		this.family = SpatialRecordSupport.requireText(family, "family");
		this.orientation = null;
		this.units = null;
		this.fidelity = null;
		this.relationIds = SpatialRecordSupport.immutableIds(relationIds);
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitionGeoIds);
		this.a00GeoId = null;
		this.a01GeoId = null;
		this.a10GeoId = null;
		this.a11GeoId = null;
		this.b0GeoId = null;
		this.b1GeoId = null;
		this.declaredScaleGeoId = null;
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	/** Creates a version-two map with explicit coefficient input identities. */
	public ProjectionDiagramMapRecord(ProjectionDiagramMapId id,
			int semanticVersion, ProjectionSystemId systemId,
			ProjectionFrameId frameId, ProjectionFrameUseRole frameUseRole,
			String family, String orientation, String units, String fidelity,
			PersistentGeoId a00GeoId, PersistentGeoId a01GeoId, PersistentGeoId a10GeoId,
			PersistentGeoId a11GeoId, PersistentGeoId b0GeoId,
			PersistentGeoId b1GeoId, PersistentGeoId declaredScaleGeoId,
			List<ProjectionFrameRelationId> relationIds, long revision) {
		this(id, semanticVersion, systemId, frameId, frameUseRole, family,
				orientation, units, fidelity, a00GeoId, a01GeoId, a10GeoId, a11GeoId,
				b0GeoId, b1GeoId, declaredScaleGeoId, relationIds, revision, null);
	}

	/** Creates a version-two map with optional immediate copy lineage. */
	public ProjectionDiagramMapRecord(ProjectionDiagramMapId id,
			int semanticVersion, ProjectionSystemId systemId,
			ProjectionFrameId frameId, ProjectionFrameUseRole frameUseRole,
			String family, String orientation, String units, String fidelity,
			PersistentGeoId a00GeoId, PersistentGeoId a01GeoId, PersistentGeoId a10GeoId,
			PersistentGeoId a11GeoId, PersistentGeoId b0GeoId,
			PersistentGeoId b1GeoId, PersistentGeoId declaredScaleGeoId,
			List<ProjectionFrameRelationId> relationIds, long revision,
			ProjectionDiagramMapId copySourceId) {
		if (semanticVersion != 2) {
			throw new IllegalArgumentException(
					"The semantic diagram-map constructor requires semanticVersion 2");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.systemId = Objects.requireNonNull(systemId);
		this.frameId = Objects.requireNonNull(frameId);
		this.frameUseRole = Objects.requireNonNull(frameUseRole);
		this.family = SpatialRecordSupport.requireText(family, "family");
		this.orientation = SpatialRecordSupport.requireText(orientation,
				"orientation");
		this.units = SpatialRecordSupport.requireText(units, "units");
		this.fidelity = SpatialRecordSupport.requireText(fidelity, "fidelity");
		this.relationIds = SpatialRecordSupport.immutableIds(relationIds);
		this.a00GeoId = Objects.requireNonNull(a00GeoId);
		this.a01GeoId = Objects.requireNonNull(a01GeoId);
		this.a10GeoId = Objects.requireNonNull(a10GeoId);
		this.a11GeoId = Objects.requireNonNull(a11GeoId);
		this.b0GeoId = Objects.requireNonNull(b0GeoId);
		this.b1GeoId = Objects.requireNonNull(b1GeoId);
		this.declaredScaleGeoId = Objects.requireNonNull(declaredScaleGeoId);
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(java.util.Arrays.asList(
				a00GeoId, a01GeoId, a10GeoId, a11GeoId, b0GeoId, b1GeoId,
				declaredScaleGeoId));
		this.revision = SpatialRecordSupport.requireRevision(revision, "revision");
		this.copySourceId = copySourceId;
	}

	@Override
	public ProjectionDiagramMapId getId() {
		return id;
	}

	@Override
	public int getSemanticVersion() {
		return semanticVersion;
	}

	@Override
	public String getXmlElementName() {
		return "diagramMap";
	}

	@Override
	public List<SpatialIdentityId> getReferences() {
		return SpatialRecordSupport.references(relationIds, definitionGeoIds,
				systemId, frameId);
	}

	@Override
	public ProjectionDiagramMapId getCopySourceId() {
		return copySourceId;
	}

	public ProjectionSystemId getSystemId() {
		return systemId;
	}

	public ProjectionFrameId getFrameId() {
		return frameId;
	}

	public ProjectionFrameUseRole getFrameUseRole() {
		return frameUseRole;
	}

	public String getFamily() {
		return family;
	}

	/** @return the explicit version-two orientation token, or {@code null} for v1 */
	public String getOrientation() {
		return orientation;
	}

	/** @return the explicit version-two unit token, or {@code null} for v1 */
	public String getUnits() {
		return units;
	}

	/** @return the explicit version-two fidelity token, or {@code null} for v1 */
	public String getFidelity() {
		return fidelity;
	}

	public List<ProjectionFrameRelationId> getRelationIds() {
		return relationIds;
	}

	public List<PersistentGeoId> getDefinitionGeoIds() {
		return definitionGeoIds;
	}

	public PersistentGeoId getA00GeoId() {
		return a00GeoId;
	}

	public PersistentGeoId getA01GeoId() {
		return a01GeoId;
	}

	public PersistentGeoId getA10GeoId() {
		return a10GeoId;
	}

	public PersistentGeoId getA11GeoId() {
		return a11GeoId;
	}

	public PersistentGeoId getB0GeoId() {
		return b0GeoId;
	}

	public PersistentGeoId getB1GeoId() {
		return b1GeoId;
	}

	public PersistentGeoId getDeclaredScaleGeoId() {
		return declaredScaleGeoId;
	}

	public long getRevision() {
		return revision;
	}

	/** @return an immutable same-ID map with the supplied revision */
	public ProjectionDiagramMapRecord withRevision(long newRevision) {
		return withRelationsAndRevision(relationIds, newRevision);
	}

	/** @return a same-ID map with explicit relation membership and revision */
	public ProjectionDiagramMapRecord withRelationsAndRevision(
			List<ProjectionFrameRelationId> newRelationIds, long newRevision) {
		if (semanticVersion == 2) {
			return new ProjectionDiagramMapRecord(id, semanticVersion, systemId,
					frameId, frameUseRole, family, orientation, units, fidelity,
					a00GeoId, a01GeoId, a10GeoId, a11GeoId, b0GeoId, b1GeoId,
					declaredScaleGeoId, newRelationIds, newRevision, copySourceId);
		}
		return new ProjectionDiagramMapRecord(id, semanticVersion, systemId,
				frameId, frameUseRole, family, newRelationIds, definitionGeoIds,
				newRevision, copySourceId);
	}

	/**
	 * Creates the fresh map identity required by an explicit frame-use re-role.
	 *
	 * @return fresh map with the requested frame-use role and relations
	 */
	public ProjectionDiagramMapRecord asFreshReroled(
			ProjectionDiagramMapId freshId, ProjectionFrameUseRole newRole,
			List<ProjectionFrameRelationId> newRelationIds) {
		if (semanticVersion != 2) {
			throw new IllegalStateException(
					"Only a version-two POINT map can be re-roled productively");
		}
		return new ProjectionDiagramMapRecord(freshId, semanticVersion, systemId,
				frameId, newRole, family, orientation, units, fidelity, a00GeoId,
				a01GeoId, a10GeoId, a11GeoId, b0GeoId, b1GeoId,
				declaredScaleGeoId, newRelationIds, 0, null);
	}

	@Override
	public ProjectionDiagramMapRecord remap(
			Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		if (semanticVersion == 2) {
			return new ProjectionDiagramMapRecord(
					SpatialRecordSupport.remap(id, remap), semanticVersion,
					SpatialRecordSupport.remap(systemId, remap),
					SpatialRecordSupport.remap(frameId, remap), frameUseRole, family,
					orientation, units, fidelity,
					SpatialRecordSupport.remap(a00GeoId, remap),
					SpatialRecordSupport.remap(a01GeoId, remap),
					SpatialRecordSupport.remap(a10GeoId, remap),
					SpatialRecordSupport.remap(a11GeoId, remap),
					SpatialRecordSupport.remap(b0GeoId, remap),
					SpatialRecordSupport.remap(b1GeoId, remap),
					SpatialRecordSupport.remap(declaredScaleGeoId, remap),
					SpatialRecordSupport.remap(relationIds, remap), revision,
					recordImmediateCopySource ? id : copySourceId);
		}
		return new ProjectionDiagramMapRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, SpatialRecordSupport.remap(systemId, remap),
				SpatialRecordSupport.remap(frameId, remap), frameUseRole, family,
				SpatialRecordSupport.remap(relationIds, remap),
				SpatialRecordSupport.remap(definitionGeoIds, remap), revision,
				recordImmediateCopySource ? id : copySourceId);
	}
}
