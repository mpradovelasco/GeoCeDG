/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Inert identity, type and definition-input record for a future spatial object. */
public final class SpatialObjectRecord implements SpatialIdentityRecord {
	/** The only spatial type admitted by the version-two G9A2 pilot. */
	public static final String POINT_TYPE = "POINT";

	/** The only canonical schema admitted by the version-two G9A2 pilot. */
	public static final String POINT_SCHEMA_ID = "cedg.spatial.point.projection";

	/** The only canonical schema version admitted by the version-two G9A2 pilot. */
	public static final int POINT_SCHEMA_VERSION = 1;

	private final SpatialObjectId id;
	private final int semanticVersion;
	private final String spatialType;
	private final EditAuthorityMode authority;
	private final String schemaId;
	private final int schemaVersion;
	private final List<PersistentGeoId> definitionGeoIds;
	private final ProjectionSystemId systemId;
	private final List<ProjectionBindingId> bindingIds;
	private final long definitionRevision;
	private final long topologyRevision;
	private final SpatialObjectId copySourceId;

	/** Creates an original inert spatial-object record. */
	public SpatialObjectRecord(SpatialObjectId id, int semanticVersion,
			String spatialType, EditAuthorityMode authority, String schemaId,
			int schemaVersion, List<PersistentGeoId> definitionGeoIds,
			long definitionRevision, long topologyRevision) {
		this(id, semanticVersion, spatialType, authority, schemaId, schemaVersion,
				definitionGeoIds, definitionRevision, topologyRevision, null);
	}

	/** Creates an inert spatial-object record with optional immediate copy lineage. */
	public SpatialObjectRecord(SpatialObjectId id, int semanticVersion,
			String spatialType, EditAuthorityMode authority, String schemaId,
			int schemaVersion, List<PersistentGeoId> definitionGeoIds,
			long definitionRevision, long topologyRevision,
			SpatialObjectId copySourceId) {
		if (semanticVersion != 1) {
			throw new IllegalArgumentException(
					"The inert spatial-object constructor requires semanticVersion 1");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.spatialType = SpatialRecordSupport.requireText(spatialType, "spatialType");
		this.authority = Objects.requireNonNull(authority);
		this.schemaId = SpatialRecordSupport.requireText(schemaId, "schemaId");
		this.schemaVersion = SpatialRecordSupport.requirePositive(schemaVersion,
				"schemaVersion");
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitionGeoIds);
		this.systemId = null;
		this.bindingIds = java.util.Collections.emptyList();
		this.definitionRevision = SpatialRecordSupport.requireRevision(definitionRevision,
				"definitionRevision");
		this.topologyRevision = SpatialRecordSupport.requireRevision(topologyRevision,
				"topologyRevision");
		this.copySourceId = copySourceId;
	}

	/** Creates a version-two projection-defined point object. */
	public SpatialObjectRecord(SpatialObjectId id, int semanticVersion,
			String spatialType, EditAuthorityMode authority, String schemaId,
			int schemaVersion, ProjectionSystemId systemId,
			List<ProjectionBindingId> bindingIds, long definitionRevision,
			long topologyRevision) {
		this(id, semanticVersion, spatialType, authority, schemaId, schemaVersion,
				systemId, bindingIds, definitionRevision, topologyRevision, null);
	}

	/** Creates a version-two point object with optional immediate copy lineage. */
	public SpatialObjectRecord(SpatialObjectId id, int semanticVersion,
			String spatialType, EditAuthorityMode authority, String schemaId,
			int schemaVersion, ProjectionSystemId systemId,
			List<ProjectionBindingId> bindingIds, long definitionRevision,
			long topologyRevision, SpatialObjectId copySourceId) {
		if (semanticVersion != 2) {
			throw new IllegalArgumentException(
					"The semantic spatial-object constructor requires semanticVersion 2");
		}
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = semanticVersion;
		this.spatialType = SpatialRecordSupport.requireText(spatialType, "spatialType");
		if (!POINT_TYPE.equals(this.spatialType)) {
			throw new IllegalArgumentException(
					"Version-two spatial objects support POINT only");
		}
		this.authority = Objects.requireNonNull(authority);
		if (authority != EditAuthorityMode.PROJECTION_DEFINED) {
			throw new IllegalArgumentException(
					"Version-two spatial objects require PROJECTION_DEFINED authority");
		}
		this.schemaId = SpatialRecordSupport.requireText(schemaId, "schemaId");
		this.schemaVersion = SpatialRecordSupport.requirePositive(schemaVersion,
				"schemaVersion");
		if (!POINT_SCHEMA_ID.equals(this.schemaId)
				|| this.schemaVersion != POINT_SCHEMA_VERSION) {
			throw new IllegalArgumentException(
					"Version-two spatial objects require the G9A2 point schema");
		}
		this.definitionGeoIds = java.util.Collections.emptyList();
		this.systemId = Objects.requireNonNull(systemId);
		this.bindingIds = SpatialRecordSupport.immutableIds(bindingIds);
		if (this.bindingIds.isEmpty()) {
			throw new IllegalArgumentException(
					"Version-two spatial objects require an explicit binding membership");
		}
		this.definitionRevision = SpatialRecordSupport.requireRevision(definitionRevision,
				"definitionRevision");
		this.topologyRevision = SpatialRecordSupport.requireRevision(topologyRevision,
				"topologyRevision");
		this.copySourceId = copySourceId;
	}

	@Override
	public SpatialObjectId getId() {
		return id;
	}

	@Override
	public int getSemanticVersion() {
		return semanticVersion;
	}

	@Override
	public String getXmlElementName() {
		return "object";
	}

	@Override
	public List<SpatialIdentityId> getReferences() {
		if (semanticVersion == 2) {
			return SpatialRecordSupport.references(bindingIds,
					java.util.Collections.<SpatialIdentityId>emptyList(), systemId);
		}
		return SpatialRecordSupport.references(definitionGeoIds,
				java.util.Collections.<SpatialIdentityId>emptyList());
	}

	@Override
	public SpatialObjectId getCopySourceId() {
		return copySourceId;
	}

	public String getSpatialType() {
		return spatialType;
	}

	public EditAuthorityMode getAuthority() {
		return authority;
	}

	public String getSchemaId() {
		return schemaId;
	}

	public int getSchemaVersion() {
		return schemaVersion;
	}

	public List<PersistentGeoId> getDefinitionGeoIds() {
		return definitionGeoIds;
	}

	/** @return the explicit version-two projection system, or {@code null} for v1 */
	public ProjectionSystemId getSystemId() {
		return systemId;
	}

	/** @return the explicit version-two binding membership, empty for v1 */
	public List<ProjectionBindingId> getBindingIds() {
		return bindingIds;
	}

	public long getDefinitionRevision() {
		return definitionRevision;
	}

	public long getTopologyRevision() {
		return topologyRevision;
	}

	@Override
	public SpatialObjectRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		if (semanticVersion == 2) {
			return new SpatialObjectRecord(SpatialRecordSupport.remap(id, remap),
					semanticVersion, spatialType, authority, schemaId, schemaVersion,
					SpatialRecordSupport.remap(systemId, remap),
					SpatialRecordSupport.remap(bindingIds, remap), definitionRevision,
					topologyRevision, recordImmediateCopySource ? id : copySourceId);
		}
		return new SpatialObjectRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, spatialType, authority, schemaId, schemaVersion,
				SpatialRecordSupport.remap(definitionGeoIds, remap), definitionRevision,
				topologyRevision, recordImmediateCopySource ? id : copySourceId);
	}
}
