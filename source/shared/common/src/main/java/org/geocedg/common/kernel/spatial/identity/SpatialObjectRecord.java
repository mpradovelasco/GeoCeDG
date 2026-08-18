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
	/** Ordinary construction-owned creation provenance. */
	public static final String CONSTRUCTION_OWNED = "CONSTRUCTION_OWNED";

	/** Durable provenance for a caller-selected explicit legacy association. */
	public static final String EXPLICIT_ASSOCIATION = "EXPLICIT_ASSOCIATION";

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
	private final String associationProvenance;

	/** Creates an original inert spatial-object record. */
	public SpatialObjectRecord(SpatialObjectId id, int semanticVersion,
			String spatialType, EditAuthorityMode authority, String schemaId,
			int schemaVersion, List<PersistentGeoId> definitionGeoIds,
			long definitionRevision, long topologyRevision) {
		this(id, semanticVersion, spatialType, authority, schemaId, schemaVersion,
				definitionGeoIds, definitionRevision, topologyRevision, null,
				CONSTRUCTION_OWNED);
	}

	/** Creates an inert spatial-object record with optional immediate copy lineage. */
	public SpatialObjectRecord(SpatialObjectId id, int semanticVersion,
			String spatialType, EditAuthorityMode authority, String schemaId,
			int schemaVersion, List<PersistentGeoId> definitionGeoIds,
			long definitionRevision, long topologyRevision,
			SpatialObjectId copySourceId) {
		this(id, semanticVersion, spatialType, authority, schemaId, schemaVersion,
				definitionGeoIds, definitionRevision, topologyRevision, copySourceId,
				CONSTRUCTION_OWNED);
	}

	private SpatialObjectRecord(SpatialObjectId id, int semanticVersion,
			String spatialType, EditAuthorityMode authority, String schemaId,
			int schemaVersion, List<PersistentGeoId> definitionGeoIds,
			long definitionRevision, long topologyRevision,
			SpatialObjectId copySourceId, String associationProvenance) {
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
		this.associationProvenance = requireAssociationProvenance(
				associationProvenance, false);
	}

	/** Creates a version-two projection-defined point object. */
	public SpatialObjectRecord(SpatialObjectId id, int semanticVersion,
			String spatialType, EditAuthorityMode authority, String schemaId,
			int schemaVersion, ProjectionSystemId systemId,
			List<ProjectionBindingId> bindingIds, long definitionRevision,
			long topologyRevision) {
		this(id, semanticVersion, spatialType, authority, schemaId, schemaVersion,
				systemId, bindingIds, definitionRevision, topologyRevision, null,
				CONSTRUCTION_OWNED);
	}

	/** Creates a version-two point object with optional immediate copy lineage. */
	public SpatialObjectRecord(SpatialObjectId id, int semanticVersion,
			String spatialType, EditAuthorityMode authority, String schemaId,
			int schemaVersion, ProjectionSystemId systemId,
			List<ProjectionBindingId> bindingIds, long definitionRevision,
			long topologyRevision, SpatialObjectId copySourceId) {
		this(id, semanticVersion, spatialType, authority, schemaId, schemaVersion,
				systemId, bindingIds, definitionRevision, topologyRevision,
				copySourceId, CONSTRUCTION_OWNED);
	}

	SpatialObjectRecord(SpatialObjectId id, int semanticVersion,
			String spatialType, EditAuthorityMode authority, String schemaId,
			int schemaVersion, ProjectionSystemId systemId,
			List<ProjectionBindingId> bindingIds, long definitionRevision,
			long topologyRevision, SpatialObjectId copySourceId,
			String associationProvenance) {
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
		this.associationProvenance = requireAssociationProvenance(
				associationProvenance, true);
	}

	private static String requireAssociationProvenance(String provenance,
			boolean allowExplicitAssociation) {
		String checked = SpatialRecordSupport.requireText(provenance,
				"associationProvenance");
		if (!CONSTRUCTION_OWNED.equals(checked)
				&& !(allowExplicitAssociation && EXPLICIT_ASSOCIATION.equals(checked))) {
			throw new IllegalArgumentException(
					"Unsupported spatial-object association provenance");
		}
		return checked;
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

	/** @return durable creation or explicit-association provenance */
	public String getAssociationProvenance() {
		return associationProvenance;
	}

	SpatialObjectRecord withAssociationProvenance(String provenance) {
		if (semanticVersion != 2) {
			throw new IllegalStateException(
					"Only a version-two POINT can record explicit association");
		}
		return new SpatialObjectRecord(id, semanticVersion, spatialType, authority,
				schemaId, schemaVersion, systemId, bindingIds, definitionRevision,
				topologyRevision, copySourceId, provenance);
	}

	/** @return an immutable same-ID object with the supplied revisions */
	public SpatialObjectRecord withRevisions(long newDefinitionRevision,
			long newTopologyRevision) {
		if (semanticVersion == 2) {
			return new SpatialObjectRecord(id, semanticVersion, spatialType, authority,
					schemaId, schemaVersion, systemId, bindingIds,
					newDefinitionRevision, newTopologyRevision, copySourceId,
					associationProvenance);
		}
		return new SpatialObjectRecord(id, semanticVersion, spatialType, authority,
				schemaId, schemaVersion, definitionGeoIds, newDefinitionRevision,
				newTopologyRevision, copySourceId);
	}

	/**
	 * @return a same-ID version-two POINT object with explicit binding membership
	 *         and revisions
	 */
	public SpatialObjectRecord withBindingsAndRevisions(
			List<ProjectionBindingId> newBindingIds, long newDefinitionRevision,
			long newTopologyRevision) {
		if (semanticVersion != 2) {
			throw new IllegalStateException(
					"Only a version-two POINT object has binding membership");
		}
		return new SpatialObjectRecord(id, semanticVersion, spatialType, authority,
				schemaId, schemaVersion, systemId, newBindingIds,
				newDefinitionRevision, newTopologyRevision, copySourceId,
				associationProvenance);
	}

	@Override
	public SpatialObjectRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		if (semanticVersion == 2) {
			return new SpatialObjectRecord(SpatialRecordSupport.remap(id, remap),
					semanticVersion, spatialType, authority, schemaId, schemaVersion,
					SpatialRecordSupport.remap(systemId, remap),
					SpatialRecordSupport.remap(bindingIds, remap), definitionRevision,
					topologyRevision, recordImmediateCopySource ? id : copySourceId,
					associationProvenance);
		}
		return new SpatialObjectRecord(SpatialRecordSupport.remap(id, remap),
				semanticVersion, spatialType, authority, schemaId, schemaVersion,
				SpatialRecordSupport.remap(definitionGeoIds, remap), definitionRevision,
				topologyRevision, recordImmediateCopySource ? id : copySourceId);
	}
}
