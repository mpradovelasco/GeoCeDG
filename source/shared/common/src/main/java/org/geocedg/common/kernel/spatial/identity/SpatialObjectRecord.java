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
	private final SpatialObjectId id;
	private final int semanticVersion;
	private final String spatialType;
	private final EditAuthorityMode authority;
	private final String schemaId;
	private final int schemaVersion;
	private final List<PersistentGeoId> definitionGeoIds;
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
		this.id = Objects.requireNonNull(id);
		this.semanticVersion = SpatialRecordSupport.requirePositive(semanticVersion,
				"semanticVersion");
		this.spatialType = SpatialRecordSupport.requireText(spatialType, "spatialType");
		this.authority = Objects.requireNonNull(authority);
		this.schemaId = SpatialRecordSupport.requireText(schemaId, "schemaId");
		this.schemaVersion = SpatialRecordSupport.requirePositive(schemaVersion,
				"schemaVersion");
		this.definitionGeoIds = SpatialRecordSupport.immutableIds(definitionGeoIds);
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

	public long getDefinitionRevision() {
		return definitionRevision;
	}

	public long getTopologyRevision() {
		return topologyRevision;
	}

	@Override
	public SpatialObjectRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		return new SpatialObjectRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, spatialType, authority, schemaId, schemaVersion,
				SpatialRecordSupport.remap(definitionGeoIds, remap), definitionRevision,
				topologyRevision, recordImmediateCopySource ? id : copySourceId);
	}
}
