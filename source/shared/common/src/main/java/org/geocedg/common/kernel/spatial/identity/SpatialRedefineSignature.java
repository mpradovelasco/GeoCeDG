/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable provider/type/schema/role/cardinality continuity signature. */
public final class SpatialRedefineSignature {
	private final String provider;
	private final String family;
	private final String schemaId;
	private final int schemaVersion;
	private final EditAuthorityMode authority;
	private final ProjectionBindingRole bindingRole;
	private final String stableOutputRole;
	private final int outputCardinality;
	private final List<PersistentGeoId> dependencies;

	/** Creates the complete provider-owned continuity signature. */
	public SpatialRedefineSignature(String provider, String family, String schemaId,
			int schemaVersion, EditAuthorityMode authority,
			ProjectionBindingRole bindingRole, String stableOutputRole,
			int outputCardinality) {
		this(provider, family, schemaId, schemaVersion, authority, bindingRole,
				stableOutputRole, outputCardinality,
				Collections.<PersistentGeoId>emptyList());
	}

	/** Creates the complete dependency-sensitive continuity signature. */
	public SpatialRedefineSignature(String provider, String family, String schemaId,
			int schemaVersion, EditAuthorityMode authority,
			ProjectionBindingRole bindingRole, String stableOutputRole,
			int outputCardinality, List<PersistentGeoId> dependencies) {
		this.provider = SpatialRecordSupport.requireText(provider, "provider");
		this.family = SpatialRecordSupport.requireText(family, "family");
		this.schemaId = SpatialRecordSupport.requireText(schemaId, "schemaId");
		this.schemaVersion = SpatialRecordSupport.requirePositive(schemaVersion,
				"schemaVersion");
		this.authority = Objects.requireNonNull(authority);
		this.bindingRole = Objects.requireNonNull(bindingRole);
		this.stableOutputRole = SpatialRecordSupport.requireText(stableOutputRole,
				"stableOutputRole");
		this.outputCardinality = SpatialRecordSupport.requirePositive(outputCardinality,
				"outputCardinality");
		Objects.requireNonNull(dependencies);
		if (new java.util.HashSet<PersistentGeoId>(dependencies).size()
				!= dependencies.size()) {
			throw new IllegalArgumentException("Redefine dependencies must be unique");
		}
		this.dependencies = SpatialRecordSupport.immutableIds(dependencies);
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

	/** @return sorted exact dependency identities participating in continuity */
	public List<PersistentGeoId> getDependencies() {
		return dependencies;
	}

	/**
	 * Host class, label and command equality are deliberately absent here.
	 *
	 * @return whether every provider-owned continuity field is equal
	 */
	public boolean isExactlyCompatibleWith(SpatialRedefineSignature other) {
		return equals(other);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SpatialRedefineSignature)) {
			return false;
		}
		SpatialRedefineSignature signature = (SpatialRedefineSignature) other;
		return provider.equals(signature.provider) && family.equals(signature.family)
				&& schemaId.equals(signature.schemaId)
				&& schemaVersion == signature.schemaVersion
				&& authority == signature.authority
				&& bindingRole == signature.bindingRole
				&& stableOutputRole.equals(signature.stableOutputRole)
				&& outputCardinality == signature.outputCardinality
				&& dependencies.equals(signature.dependencies);
	}

	@Override
	public int hashCode() {
		return Objects.hash(provider, family, schemaId, schemaVersion, authority,
				bindingRole, stableOutputRole, outputCardinality, dependencies);
	}
}
