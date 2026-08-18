/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Deterministic functional evidence for the G9A1 identity substrate. */
public final class SpatialIdentityInstrumentation {
	private long allocationAttempts;
	private long allocations;
	private long restores;
	private long remaps;
	private long collisions;
	private long unresolvedReferences;
	private long copyCommits;
	private long copyRollbacks;
	private long deleteCommits;
	private long deleteRollbacks;
	private long redefineCommits;
	private long redefineRollbacks;
	private long redefineRetainDecisions;
	private long redefineFreshDecisions;
	private long redefineRejectDecisions;
	private long redefineMissingContexts;
	private long redefineMultiOutputRejections;
	private long definitionRevisionChanges;
	private long topologyRevisionChanges;
	private long lifecyclePreparationAttempts;
	private long lifecyclePrepared;
	private long lifecyclePreflightRejects;
	private long lifecycleCommits;
	private long lifecycleRollbacks;
	private long lifecycleRecordCreates;
	private long lifecycleRecordReplacements;
	private long lifecycleRecordRetirements;
	private long lifecycleResolutionChanges;
	private long explicitMigrationCommits;
	private long explicitMigrationRollbacks;
	private long declaredExternalCopyCommits;
	private long declaredExternalCopyRollbacks;
	private long declaredExternalCopyReferences;
	private final Map<SpatialIdentityKind, Long> allocationsByKind =
			new EnumMap<>(SpatialIdentityKind.class);
	private final Map<SpatialIdentityKind, Long> restoresByKind =
			new EnumMap<>(SpatialIdentityKind.class);
	private final Map<SpatialIdentityKind, Long> remapsByKind =
			new EnumMap<>(SpatialIdentityKind.class);
	private final Map<SpatialIdentityKind, Long> collisionsByKind =
			new EnumMap<>(SpatialIdentityKind.class);
	private final Map<SpatialIdentityKind, Long> unresolvedByKind =
			new EnumMap<>(SpatialIdentityKind.class);

	void recordAllocationAttempt() {
		allocationAttempts++;
	}

	void recordAllocation(SpatialIdentityKind kind) {
		allocations++;
		Long current = allocationsByKind.get(kind);
		allocationsByKind.put(kind, current == null ? 1 : current + 1);
	}

	void recordRestore(SpatialIdentityKind kind) {
		restores++;
		increment(restoresByKind, kind);
	}

	void recordRemap(SpatialIdentityKind kind) {
		remaps++;
		increment(remapsByKind, kind);
	}

	void recordCollision(SpatialIdentityKind kind) {
		collisions++;
		increment(collisionsByKind, kind);
	}

	void recordUnresolvedReference(SpatialIdentityKind kind) {
		unresolvedReferences++;
		increment(unresolvedByKind, kind);
	}

	void recordCopyCommit() {
		copyCommits++;
	}

	void recordCopyRollback() {
		copyRollbacks++;
	}

	void recordDeleteCommit() {
		deleteCommits++;
	}

	void recordDeleteRollback() {
		deleteRollbacks++;
	}

	void recordRedefineCommit() {
		redefineCommits++;
	}

	void recordRedefineRollback() {
		redefineRollbacks++;
	}

	void recordRedefineDecision(SpatialRedefineDecision decision) {
		switch (decision) {
		case RETAIN:
			redefineRetainDecisions++;
			break;
		case FRESH:
			redefineFreshDecisions++;
			break;
		case REJECT:
			redefineRejectDecisions++;
			break;
		default:
			break;
		}
	}

	void recordRedefineMissingContext() {
		redefineMissingContexts++;
	}

	void recordRedefineMultiOutputRejection() {
		redefineMultiOutputRejections++;
	}

	void recordDefinitionRevisionChange() {
		definitionRevisionChanges++;
	}

	void recordTopologyRevisionChange() {
		topologyRevisionChanges++;
	}

	void recordLifecyclePreparationAttempt() {
		lifecyclePreparationAttempts++;
	}

	void recordLifecyclePrepared() {
		lifecyclePrepared++;
	}

	void recordLifecyclePreflightReject() {
		lifecyclePreflightRejects++;
	}

	void recordLifecycleCommit(int creates, int replacements, int retirements,
			int resolutionChanges) {
		lifecycleCommits++;
		lifecycleRecordCreates += creates;
		lifecycleRecordReplacements += replacements;
		lifecycleRecordRetirements += retirements;
		lifecycleResolutionChanges += resolutionChanges;
	}

	void recordLifecycleRollback() {
		lifecycleRollbacks++;
	}

	void recordExplicitMigrationCommit() {
		explicitMigrationCommits++;
	}

	void recordExplicitMigrationRollback() {
		explicitMigrationRollbacks++;
	}

	void recordDeclaredExternalCopyCommit(int externalReferences) {
		declaredExternalCopyCommits++;
		declaredExternalCopyReferences += externalReferences;
	}

	void recordDeclaredExternalCopyRollback() {
		declaredExternalCopyRollbacks++;
	}

	public long getAllocationAttempts() {
		return allocationAttempts;
	}

	public long getAllocations() {
		return allocations;
	}

	public long getRestores() {
		return restores;
	}

	public long getRemaps() {
		return remaps;
	}

	public long getCollisions() {
		return collisions;
	}

	public long getUnresolvedReferences() {
		return unresolvedReferences;
	}

	public long getCopyCommits() {
		return copyCommits;
	}

	public long getCopyRollbacks() {
		return copyRollbacks;
	}

	public long getDeleteCommits() {
		return deleteCommits;
	}

	public long getDeleteRollbacks() {
		return deleteRollbacks;
	}

	public long getRedefineCommits() {
		return redefineCommits;
	}

	public long getRedefineRollbacks() {
		return redefineRollbacks;
	}

	public long getRedefineRetainDecisions() {
		return redefineRetainDecisions;
	}

	public long getRedefineFreshDecisions() {
		return redefineFreshDecisions;
	}

	public long getRedefineRejectDecisions() {
		return redefineRejectDecisions;
	}

	public long getRedefineMissingContexts() {
		return redefineMissingContexts;
	}

	public long getRedefineMultiOutputRejections() {
		return redefineMultiOutputRejections;
	}

	public long getDefinitionRevisionChanges() {
		return definitionRevisionChanges;
	}

	public long getTopologyRevisionChanges() {
		return topologyRevisionChanges;
	}

	public long getLifecyclePreparationAttempts() {
		return lifecyclePreparationAttempts;
	}

	public long getLifecyclePrepared() {
		return lifecyclePrepared;
	}

	public long getLifecyclePreflightRejects() {
		return lifecyclePreflightRejects;
	}

	public long getLifecycleCommits() {
		return lifecycleCommits;
	}

	public long getLifecycleRollbacks() {
		return lifecycleRollbacks;
	}

	public long getLifecycleRecordCreates() {
		return lifecycleRecordCreates;
	}

	public long getLifecycleRecordReplacements() {
		return lifecycleRecordReplacements;
	}

	public long getLifecycleRecordRetirements() {
		return lifecycleRecordRetirements;
	}

	public long getLifecycleResolutionChanges() {
		return lifecycleResolutionChanges;
	}

	public long getExplicitMigrationCommits() {
		return explicitMigrationCommits;
	}

	public long getExplicitMigrationRollbacks() {
		return explicitMigrationRollbacks;
	}

	public long getDeclaredExternalCopyCommits() {
		return declaredExternalCopyCommits;
	}

	public long getDeclaredExternalCopyRollbacks() {
		return declaredExternalCopyRollbacks;
	}

	public long getDeclaredExternalCopyReferences() {
		return declaredExternalCopyReferences;
	}

	public Map<SpatialIdentityKind, Long> getAllocationsByKind() {
		return Collections.unmodifiableMap(new EnumMap<>(allocationsByKind));
	}

	public Map<SpatialIdentityKind, Long> getRestoresByKind() {
		return Collections.unmodifiableMap(new EnumMap<>(restoresByKind));
	}

	public Map<SpatialIdentityKind, Long> getRemapsByKind() {
		return Collections.unmodifiableMap(new EnumMap<>(remapsByKind));
	}

	public Map<SpatialIdentityKind, Long> getCollisionsByKind() {
		return Collections.unmodifiableMap(new EnumMap<>(collisionsByKind));
	}

	public Map<SpatialIdentityKind, Long> getUnresolvedByKind() {
		return Collections.unmodifiableMap(new EnumMap<>(unresolvedByKind));
	}

	// These forbidden authority mechanisms intentionally have no mutator.
	public long getLabelAuthorityUses() {
		return 0;
	}

	public long getCoordinateAuthorityUses() {
		return 0;
	}

	public long getConstructionOrderAuthorityUses() {
		return 0;
	}

	public long getXmlPositionAuthorityUses() {
		return 0;
	}

	public long getOutputOrdinalAuthorityUses() {
		return 0;
	}

	public long getViewportAuthorityUses() {
		return 0;
	}

	public long getDpiAuthorityUses() {
		return 0;
	}

	public long getCameraAuthorityUses() {
		return 0;
	}

	public long getJavaInstanceAuthorityUses() {
		return 0;
	}

	public long getRendererAuthorityUses() {
		return 0;
	}

	public long getScreenStateAuthorityUses() {
		return 0;
	}

	// G9A2+ semantic work is outside this phase and has no recording API.
	public long getProjectionEvaluations() {
		return 0;
	}

	public long getReconstructionEvaluations() {
		return 0;
	}

	public long getDiagramMapEvaluations() {
		return 0;
	}

	public long getHingeEvaluations() {
		return 0;
	}

	public long getCertificatePublications() {
		return 0;
	}

	public long getStaleSpatialPayloadPublications() {
		return 0;
	}

	/** Resets functional evidence without changing registry state. */
	public void reset() {
		allocationAttempts = 0;
		allocations = 0;
		restores = 0;
		remaps = 0;
		collisions = 0;
		unresolvedReferences = 0;
		copyCommits = 0;
		copyRollbacks = 0;
		deleteCommits = 0;
		deleteRollbacks = 0;
		redefineCommits = 0;
		redefineRollbacks = 0;
		redefineRetainDecisions = 0;
		redefineFreshDecisions = 0;
		redefineRejectDecisions = 0;
		redefineMissingContexts = 0;
		redefineMultiOutputRejections = 0;
		definitionRevisionChanges = 0;
		topologyRevisionChanges = 0;
		lifecyclePreparationAttempts = 0;
		lifecyclePrepared = 0;
		lifecyclePreflightRejects = 0;
		lifecycleCommits = 0;
		lifecycleRollbacks = 0;
		lifecycleRecordCreates = 0;
		lifecycleRecordReplacements = 0;
		lifecycleRecordRetirements = 0;
		lifecycleResolutionChanges = 0;
		explicitMigrationCommits = 0;
		explicitMigrationRollbacks = 0;
		declaredExternalCopyCommits = 0;
		declaredExternalCopyRollbacks = 0;
		declaredExternalCopyReferences = 0;
		allocationsByKind.clear();
		restoresByKind.clear();
		remapsByKind.clear();
		collisionsByKind.clear();
		unresolvedByKind.clear();
	}

	private static void increment(Map<SpatialIdentityKind, Long> counters,
			SpatialIdentityKind kind) {
		Long current = counters.get(kind);
		counters.put(kind, current == null ? 1 : current + 1);
	}
}
