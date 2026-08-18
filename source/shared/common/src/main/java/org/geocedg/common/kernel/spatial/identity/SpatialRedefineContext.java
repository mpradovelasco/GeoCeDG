/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

import org.geogebra.common.kernel.geos.GeoElement;

/** Immutable context captured from one explicit old target before parsing. */
public final class SpatialRedefineContext {
	private final SpatialRedefineOutputGroup<SpatialRedefinePersistedOutput> oldOutputs;
	private final String targetedStableOutputRole;
	private final int oldHostOutputCount;
	private final String rollbackXml;
	private final long hostOperationEpoch;
	private final RollbackAuthority rollbackAuthority;

	SpatialRedefineContext(GeoElement oldTarget, PersistentGeoId oldId,
			SpatialRedefineSignature oldSignature, int oldHostOutputCount,
			long oldDefinitionRevision, long oldTopologyRevision, String rollbackXml,
			long hostOperationEpoch, long graphPublicationEpoch,
			long runtimePublicationEpoch) {
		this(SpatialRedefineOutputGroup.singleton(
				new SpatialRedefinePersistedOutput(oldTarget, oldId, oldSignature,
						oldDefinitionRevision, oldTopologyRevision)),
				oldSignature.getStableOutputRole(), oldHostOutputCount, rollbackXml,
				hostOperationEpoch, graphPublicationEpoch, runtimePublicationEpoch);
	}

	SpatialRedefineContext(
			SpatialRedefineOutputGroup<SpatialRedefinePersistedOutput> oldOutputs,
			String targetedStableOutputRole, int oldHostOutputCount,
			String rollbackXml, long hostOperationEpoch,
			long graphPublicationEpoch, long runtimePublicationEpoch) {
		this(oldOutputs, targetedStableOutputRole, oldHostOutputCount, rollbackXml,
				hostOperationEpoch, new RollbackAuthority(hostOperationEpoch,
						graphPublicationEpoch, runtimePublicationEpoch));
	}

	private SpatialRedefineContext(
			SpatialRedefineOutputGroup<SpatialRedefinePersistedOutput> oldOutputs,
			String targetedStableOutputRole, int oldHostOutputCount,
			String rollbackXml, long hostOperationEpoch,
			RollbackAuthority rollbackAuthority) {
		this.oldOutputs = Objects.requireNonNull(oldOutputs);
		this.targetedStableOutputRole = SpatialRecordSupport.requireText(
				targetedStableOutputRole, "targetedStableOutputRole");
		if (!oldOutputs.containsRole(this.targetedStableOutputRole)) {
			throw new IllegalArgumentException(
					"Targeted stable role is absent from the old output group");
		}
		this.oldHostOutputCount = SpatialRecordSupport.requirePositive(
				oldHostOutputCount, "oldHostOutputCount");
		this.rollbackXml = Objects.requireNonNull(rollbackXml);
		if (hostOperationEpoch <= 0) {
			throw new IllegalArgumentException(
					"hostOperationEpoch must be positive");
		}
		this.hostOperationEpoch = hostOperationEpoch;
		this.rollbackAuthority = Objects.requireNonNull(rollbackAuthority);
	}

	public GeoElement getOldTarget() {
		return targetedOutput().getGeo();
	}

	public PersistentGeoId getOldId() {
		return targetedOutput().getId();
	}

	public SpatialRedefineSignature getOldSignature() {
		return targetedOutput().getSignature();
	}

	/** @return complete participating sibling authority keyed by stable role */
	public SpatialRedefineOutputGroup<SpatialRedefinePersistedOutput>
			getOldOutputs() {
		return oldOutputs;
	}

	/** @return stable role of the explicit host target */
	public String getTargetedStableOutputRole() {
		return targetedStableOutputRole;
	}

	/** @return actual old host algorithm output count captured before parsing */
	public int getOldHostOutputCount() {
		return oldHostOutputCount;
	}

	/** @return provider-owned definition revision captured before parsing */
	public long getOldDefinitionRevision() {
		return targetedOutput().getDefinitionRevision();
	}

	/** @return provider-owned topology revision captured before parsing */
	public long getOldTopologyRevision() {
		return targetedOutput().getTopologyRevision();
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
		return new SpatialRedefineContext(oldOutputs, targetedStableOutputRole,
				oldHostOutputCount, currentRollbackXml, hostOperationEpoch,
				rollbackAuthority);
	}

	long getHostOperationEpoch() {
		return hostOperationEpoch;
	}

	boolean isRollbackAvailable(long currentHostOperationEpoch,
			long currentGraphPublicationEpoch, long currentRuntimePublicationEpoch) {
		return rollbackAuthority.isAvailable(currentHostOperationEpoch,
				currentGraphPublicationEpoch, currentRuntimePublicationEpoch);
	}

	boolean claimRollback(long currentHostOperationEpoch,
			long currentGraphPublicationEpoch, long currentRuntimePublicationEpoch) {
		return rollbackAuthority.claim(currentHostOperationEpoch,
				currentGraphPublicationEpoch, currentRuntimePublicationEpoch);
	}

	void advanceRollbackPublicationEpoch(long expectedGraphPublicationEpoch,
			long currentGraphPublicationEpoch, long expectedRuntimePublicationEpoch,
			long currentRuntimePublicationEpoch) {
		rollbackAuthority.advancePublicationEpoch(expectedGraphPublicationEpoch,
				currentGraphPublicationEpoch, expectedRuntimePublicationEpoch,
				currentRuntimePublicationEpoch);
	}

	boolean canAdvanceRollbackPublicationEpoch(long expectedGraphPublicationEpoch,
			long currentGraphPublicationEpoch, long expectedRuntimePublicationEpoch,
			long currentRuntimePublicationEpoch) {
		return rollbackAuthority.canAdvancePublicationEpoch(
				expectedGraphPublicationEpoch, currentGraphPublicationEpoch,
				expectedRuntimePublicationEpoch, currentRuntimePublicationEpoch);
	}

	boolean hasAdvancedRollbackPublicationEpoch() {
		return rollbackAuthority.hasAdvancedPublicationEpoch();
	}

	void completeRollbackAuthority() {
		rollbackAuthority.complete();
	}

	private SpatialRedefinePersistedOutput targetedOutput() {
		return oldOutputs.get(targetedStableOutputRole);
	}

	private static final class RollbackAuthority {
		private final long hostOperationEpoch;
		private long graphPublicationEpoch;
		private long runtimePublicationEpoch;
		private boolean publicationAdvanced;
		private boolean available = true;

		private RollbackAuthority(long hostOperationEpoch,
				long graphPublicationEpoch, long runtimePublicationEpoch) {
			this.hostOperationEpoch = hostOperationEpoch;
			if (graphPublicationEpoch < 0) {
				throw new IllegalArgumentException(
						"graphPublicationEpoch must not be negative");
			}
			this.graphPublicationEpoch = graphPublicationEpoch;
			if (runtimePublicationEpoch < 0) {
				throw new IllegalArgumentException(
						"runtimePublicationEpoch must not be negative");
			}
			this.runtimePublicationEpoch = runtimePublicationEpoch;
		}

		private boolean isAvailable(long currentHostOperationEpoch,
				long currentGraphPublicationEpoch,
				long currentRuntimePublicationEpoch) {
			return available && hostOperationEpoch == currentHostOperationEpoch
					&& graphPublicationEpoch == currentGraphPublicationEpoch
					&& runtimePublicationEpoch == currentRuntimePublicationEpoch;
		}

		private boolean claim(long currentHostOperationEpoch,
				long currentGraphPublicationEpoch,
				long currentRuntimePublicationEpoch) {
			if (!isAvailable(currentHostOperationEpoch,
					currentGraphPublicationEpoch, currentRuntimePublicationEpoch)) {
				return false;
			}
			available = false;
			return true;
		}

		private void advancePublicationEpoch(long expectedGraphPublicationEpoch,
				long currentGraphPublicationEpoch,
				long expectedRuntimePublicationEpoch,
				long currentRuntimePublicationEpoch) {
			if (!canAdvancePublicationEpoch(expectedGraphPublicationEpoch,
					currentGraphPublicationEpoch, expectedRuntimePublicationEpoch,
					currentRuntimePublicationEpoch)) {
				throw new IllegalStateException(
						"Redefine publication lease is stale or already consumed");
			}
			graphPublicationEpoch = currentGraphPublicationEpoch;
			runtimePublicationEpoch = currentRuntimePublicationEpoch;
			if (currentGraphPublicationEpoch > expectedGraphPublicationEpoch
					|| currentRuntimePublicationEpoch
							> expectedRuntimePublicationEpoch) {
				publicationAdvanced = true;
			}
		}

		private boolean canAdvancePublicationEpoch(
				long expectedGraphPublicationEpoch,
				long currentGraphPublicationEpoch,
				long expectedRuntimePublicationEpoch,
				long currentRuntimePublicationEpoch) {
			boolean advances = currentGraphPublicationEpoch
					> expectedGraphPublicationEpoch
					|| currentRuntimePublicationEpoch
							> expectedRuntimePublicationEpoch;
			return available && (!advances || !publicationAdvanced)
					&& graphPublicationEpoch == expectedGraphPublicationEpoch
					&& runtimePublicationEpoch == expectedRuntimePublicationEpoch
					&& currentGraphPublicationEpoch >= expectedGraphPublicationEpoch
					&& currentRuntimePublicationEpoch >= expectedRuntimePublicationEpoch;
		}

		private boolean hasAdvancedPublicationEpoch() {
			return publicationAdvanced;
		}

		private void complete() {
			available = false;
		}
	}
}
