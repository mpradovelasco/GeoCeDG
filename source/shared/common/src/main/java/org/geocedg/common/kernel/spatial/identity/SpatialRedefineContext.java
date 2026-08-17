/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

import org.geogebra.common.kernel.geos.GeoElement;

/** Immutable context captured from one explicit old target before parsing. */
public final class SpatialRedefineContext {
	private final GeoElement oldTarget;
	private final PersistentGeoId oldId;
	private final SpatialRedefineSignature oldSignature;
	private final int oldHostOutputCount;
	private final long oldDefinitionRevision;
	private final long oldTopologyRevision;
	private final String rollbackXml;

	SpatialRedefineContext(GeoElement oldTarget, PersistentGeoId oldId,
			SpatialRedefineSignature oldSignature, int oldHostOutputCount,
			long oldDefinitionRevision, long oldTopologyRevision, String rollbackXml) {
		this.oldTarget = Objects.requireNonNull(oldTarget);
		this.oldId = Objects.requireNonNull(oldId);
		this.oldSignature = Objects.requireNonNull(oldSignature);
		this.oldHostOutputCount = oldHostOutputCount;
		this.oldDefinitionRevision = oldDefinitionRevision;
		this.oldTopologyRevision = oldTopologyRevision;
		this.rollbackXml = Objects.requireNonNull(rollbackXml);
	}

	public GeoElement getOldTarget() {
		return oldTarget;
	}

	public PersistentGeoId getOldId() {
		return oldId;
	}

	public SpatialRedefineSignature getOldSignature() {
		return oldSignature;
	}

	/** @return actual old host algorithm output count captured before parsing */
	public int getOldHostOutputCount() {
		return oldHostOutputCount;
	}

	/** @return provider-owned definition revision captured before parsing */
	public long getOldDefinitionRevision() {
		return oldDefinitionRevision;
	}

	/** @return provider-owned topology revision captured before parsing */
	public long getOldTopologyRevision() {
		return oldTopologyRevision;
	}

	/** @return exact pre-parse construction snapshot for host rollback */
	public String getRollbackXml() {
		return rollbackXml;
	}

	/**
	 * Refreshes only the operation-local host rollback snapshot. The captured
	 * identity authority and revisions remain unchanged, so a stale context
	 * still fails the registry's last-moment authority check.
	 *
	 * @param currentRollbackXml construction snapshot taken before candidate evaluation
	 * @return context with the refreshed rollback snapshot
	 */
	public SpatialRedefineContext withRollbackXml(String currentRollbackXml) {
		return new SpatialRedefineContext(oldTarget, oldId, oldSignature,
				oldHostOutputCount, oldDefinitionRevision, oldTopologyRevision,
				currentRollbackXml);
	}
}
