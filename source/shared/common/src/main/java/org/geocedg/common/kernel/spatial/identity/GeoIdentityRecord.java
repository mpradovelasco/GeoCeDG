/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Provider-owned continuity contract for one participating ordinary geo. */
public final class GeoIdentityRecord implements SpatialIdentityRecord {
	public static final int SEMANTIC_VERSION = 1;

	private final PersistentGeoId id;
	private final String provider;
	private final String family;
	private final String schemaId;
	private final int schemaVersion;
	private final EditAuthorityMode authority;
	private final ProjectionBindingRole bindingRole;
	private final String stableOutputRole;
	private final int outputCardinality;
	private final List<PersistentGeoId> dependencies;
	private final List<SpatialIdentityId> references;
	private final long definitionRevision;
	private final long topologyRevision;
	private final PersistentGeoId copySourceId;

	/** Creates an original participating-geo record. */
	public GeoIdentityRecord(PersistentGeoId id, String provider, String family,
			String schemaId, int schemaVersion, EditAuthorityMode authority,
			ProjectionBindingRole bindingRole, String stableOutputRole,
			int outputCardinality, long definitionRevision, long topologyRevision) {
		this(id, provider, family, schemaId, schemaVersion, authority, bindingRole,
				stableOutputRole, outputCardinality,
				Collections.<PersistentGeoId>emptyList(), definitionRevision,
				topologyRevision, null);
	}

	/** Creates a participating-geo record with optional immediate copy lineage. */
	public GeoIdentityRecord(PersistentGeoId id, String provider, String family,
			String schemaId, int schemaVersion, EditAuthorityMode authority,
			ProjectionBindingRole bindingRole, String stableOutputRole,
			int outputCardinality, long definitionRevision, long topologyRevision,
			PersistentGeoId copySourceId) {
		this(id, provider, family, schemaId, schemaVersion, authority, bindingRole,
				stableOutputRole, outputCardinality,
				Collections.<PersistentGeoId>emptyList(), definitionRevision,
				topologyRevision, copySourceId);
	}

	/** Creates an original record with exact construction-DAG dependencies. */
	public GeoIdentityRecord(PersistentGeoId id, String provider, String family,
			String schemaId, int schemaVersion, EditAuthorityMode authority,
			ProjectionBindingRole bindingRole, String stableOutputRole,
			int outputCardinality, List<PersistentGeoId> dependencies,
			long definitionRevision, long topologyRevision) {
		this(id, provider, family, schemaId, schemaVersion, authority, bindingRole,
				stableOutputRole, outputCardinality, dependencies, definitionRevision,
				topologyRevision, null);
	}

	/**
	 * Creates a participating-geo record with exact live dependencies and optional
	 * immediate copy lineage.
	 */
	public GeoIdentityRecord(PersistentGeoId id, String provider, String family,
			String schemaId, int schemaVersion, EditAuthorityMode authority,
			ProjectionBindingRole bindingRole, String stableOutputRole,
			int outputCardinality, List<PersistentGeoId> dependencies,
			long definitionRevision, long topologyRevision,
			PersistentGeoId copySourceId) {
		this.id = Objects.requireNonNull(id);
		this.provider = SpatialRecordSupport.requireText(provider, "provider");
		this.family = SpatialRecordSupport.requireText(family, "family");
		this.schemaId = SpatialRecordSupport.requireText(schemaId, "schemaId");
		this.schemaVersion = SpatialRecordSupport.requirePositive(schemaVersion,
				"schemaVersion");
		this.authority = Objects.requireNonNull(authority);
		this.bindingRole = Objects.requireNonNull(bindingRole);
		boolean constructionDefined = authority == EditAuthorityMode.CONSTRUCTION_DEFINED;
		boolean notApplicable = bindingRole == ProjectionBindingRole.NOT_APPLICABLE;
		if (constructionDefined != notApplicable) {
			throw new IllegalArgumentException("Construction-defined authority and "
					+ "not-applicable projection role must be used together");
		}
		this.stableOutputRole = SpatialRecordSupport.requireText(stableOutputRole,
				"stableOutputRole");
		this.outputCardinality = SpatialRecordSupport.requirePositive(outputCardinality,
				"outputCardinality");
		Objects.requireNonNull(dependencies);
		if (new java.util.HashSet<PersistentGeoId>(dependencies).size()
				!= dependencies.size()) {
			throw new IllegalArgumentException("Geo dependencies must be unique");
		}
		this.dependencies = SpatialRecordSupport.immutableIds(dependencies);
		if (this.dependencies.contains(id)) {
			throw new IllegalArgumentException("A geo identity cannot depend on itself");
		}
		this.references = Collections.unmodifiableList(
				new ArrayList<SpatialIdentityId>(this.dependencies));
		this.definitionRevision = SpatialRecordSupport.requireRevision(definitionRevision,
				"definitionRevision");
		this.topologyRevision = SpatialRecordSupport.requireRevision(topologyRevision,
				"topologyRevision");
		this.copySourceId = copySourceId;
	}

	@Override
	public PersistentGeoId getId() {
		return id;
	}

	@Override
	public int getSemanticVersion() {
		return SEMANTIC_VERSION;
	}

	@Override
	public String getXmlElementName() {
		return "geo";
	}

	@Override
	public List<SpatialIdentityId> getReferences() {
		return references;
	}

	/** @return exact sorted durable construction-DAG dependency identities */
	public List<PersistentGeoId> getDependencies() {
		return dependencies;
	}

	@Override
	public PersistentGeoId getCopySourceId() {
		return copySourceId;
	}

	public String getProvider() {
		return provider;
	}

	public String getFamily() {
		return family;
	}

	public String getSchemaId() {
		return schemaId;
	}

	public int getSchemaVersion() {
		return schemaVersion;
	}

	public EditAuthorityMode getAuthority() {
		return authority;
	}

	public ProjectionBindingRole getBindingRole() {
		return bindingRole;
	}

	public String getStableOutputRole() {
		return stableOutputRole;
	}

	public int getOutputCardinality() {
		return outputCardinality;
	}

	public long getDefinitionRevision() {
		return definitionRevision;
	}

	public long getTopologyRevision() {
		return topologyRevision;
	}

	/** @return the exact provider-owned signature used at redefine boundaries */
	public SpatialRedefineSignature toRedefineSignature() {
		return new SpatialRedefineSignature(provider, family, schemaId, schemaVersion,
				authority, bindingRole, stableOutputRole, outputCardinality,
				dependencies);
	}

	/** @return a copy with the supplied monotone revisions */
	public GeoIdentityRecord withRevisions(long newDefinitionRevision,
			long newTopologyRevision) {
		return new GeoIdentityRecord(id, provider, family, schemaId, schemaVersion,
				authority, bindingRole, stableOutputRole, outputCardinality,
				dependencies, newDefinitionRevision, newTopologyRevision, copySourceId);
	}

	/**
	 * Creates the same durable geo identity with one explicitly changed projection
	 * binding role and monotone lifecycle revisions.
	 *
	 * @return role-revised identity record
	 */
	public GeoIdentityRecord withBindingRoleAndRevisions(
			ProjectionBindingRole newBindingRole, long newDefinitionRevision,
			long newTopologyRevision) {
		return new GeoIdentityRecord(id, provider, family, schemaId, schemaVersion,
				authority, newBindingRole, stableOutputRole, outputCardinality,
				dependencies, newDefinitionRevision, newTopologyRevision, copySourceId);
	}

	@Override
	public GeoIdentityRecord remap(Map<SpatialIdentityId, SpatialIdentityId> remap,
			boolean recordImmediateCopySource) {
		PersistentGeoId remapped = SpatialRecordSupport.remap(id, remap);
		return new GeoIdentityRecord(remapped, provider, family, schemaId, schemaVersion,
				authority, bindingRole, stableOutputRole, outputCardinality,
				SpatialRecordSupport.remap(dependencies, remap), definitionRevision,
				topologyRevision,
				recordImmediateCopySource ? id : copySourceId);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GeoIdentityRecord)) {
			return false;
		}
		GeoIdentityRecord record = (GeoIdentityRecord) other;
		return id.equals(record.id) && provider.equals(record.provider)
				&& family.equals(record.family) && schemaId.equals(record.schemaId)
				&& schemaVersion == record.schemaVersion && authority == record.authority
				&& bindingRole == record.bindingRole
				&& stableOutputRole.equals(record.stableOutputRole)
				&& outputCardinality == record.outputCardinality
				&& dependencies.equals(record.dependencies)
				&& definitionRevision == record.definitionRevision
				&& topologyRevision == record.topologyRevision
				&& Objects.equals(copySourceId, record.copySourceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, provider, family, schemaId, schemaVersion, authority,
				bindingRole, stableOutputRole, outputCardinality, dependencies,
				definitionRevision, topologyRevision, copySourceId);
	}
}
