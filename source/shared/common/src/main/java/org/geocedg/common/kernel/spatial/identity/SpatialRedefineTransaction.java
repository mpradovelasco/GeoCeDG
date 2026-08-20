/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.geogebra.common.kernel.geos.GeoElement;

/** Prepared redefine decision that can commit once or roll back without mutation. */
public final class SpatialRedefineTransaction {
	/** Observable lifecycle of the transaction object. */
	public enum State {
		PREPARED,
		COMMITTED,
		ROLLED_BACK
	}

	private final SpatialIdentityRegistry registry;
	private final SpatialRedefineContext context;
	private final SpatialRedefineProposal proposal;
	private final SpatialRedefineDecision decision;
	private final Map<String, PersistentGeoId> decidedIds;
	private final Set<SpatialIdentityId> retiredIds;
	private final SpatialRedefineCandidateParticipation candidateParticipation;
	private boolean rebuildViewWritten;
	private State state = State.PREPARED;

	SpatialRedefineTransaction(SpatialIdentityRegistry registry,
			SpatialRedefineContext context, SpatialRedefineProposal proposal,
			SpatialRedefineDecision decision, PersistentGeoId freshId,
			Set<SpatialIdentityId> retiredIds) {
		this.registry = registry;
		this.context = context;
		this.proposal = proposal;
		this.decision = decision;
		this.decidedIds = singletonDecidedIds(context, proposal, decision, freshId);
		this.retiredIds = Collections.unmodifiableSet(
				new LinkedHashSet<>(retiredIds));
		this.candidateParticipation = null;
	}

	SpatialRedefineTransaction(SpatialIdentityRegistry registry,
			SpatialRedefineContext context, SpatialRedefineProposal proposal,
			SpatialRedefineDecision decision,
			Map<String, PersistentGeoId> decidedIds,
			Set<SpatialIdentityId> retiredIds) {
		this(registry, context, proposal, decision, decidedIds, retiredIds, null);
	}

	SpatialRedefineTransaction(SpatialIdentityRegistry registry,
			SpatialRedefineContext context, SpatialRedefineProposal proposal,
			SpatialRedefineDecision decision,
			Map<String, PersistentGeoId> decidedIds,
			Set<SpatialIdentityId> retiredIds,
			SpatialRedefineCandidateParticipation candidateParticipation) {
		this.registry = registry;
		this.context = context;
		this.proposal = proposal;
		this.decision = decision;
		TreeMap<String, PersistentGeoId> ordered = new TreeMap<>();
		for (Map.Entry<String, PersistentGeoId> entry : decidedIds.entrySet()) {
			String role = SpatialRecordSupport.requireText(entry.getKey(),
					"stableOutputRole");
			if (ordered.put(role, java.util.Objects.requireNonNull(entry.getValue()))
					!= null) {
				throw new IllegalArgumentException(
						"Duplicate decided stable output role: " + role);
			}
		}
		this.decidedIds = Collections.unmodifiableMap(ordered);
		this.retiredIds = Collections.unmodifiableSet(
				new LinkedHashSet<>(retiredIds));
		this.candidateParticipation = candidateParticipation;
	}

	public SpatialRedefineContext getContext() {
		return context;
	}

	public SpatialRedefineProposal getProposal() {
		return proposal;
	}

	public SpatialRedefineDecision getDecision() {
		return decision;
	}

	public PersistentGeoId getDecidedId() {
		return decidedIds.get(proposal.getTargetedStableOutputRole());
	}

	/** @return decided durable identity for the exact stable role, or {@code null} */
	public PersistentGeoId getDecidedId(String stableOutputRole) {
		return decidedIds.get(stableOutputRole);
	}

	/** @return complete immutable role-to-decided-identity map */
	public Map<String, PersistentGeoId> getDecidedIds() {
		return decidedIds;
	}

	public State getState() {
		return state;
	}

	/** @return the complete old closure retired by a committed FRESH decision */
	public Set<SpatialIdentityId> getRetiredIds() {
		return retiredIds;
	}

	/**
	 * Activates only ordinary persistence labels for the already provider-approved
	 * staged set. Identity records remain unpublished until transaction commit.
	 */
	public void activateCandidateParticipation() {
		registry.activateRedefineCandidateParticipation(this);
	}

	/** Commits the prepared decision against the actual installed result. */
	public void commit(GeoElement actualResult) {
		registry.commitRedefine(this, actualResult);
	}

	/** Abandons an uncommitted decision and releases any fresh-ID reservation. */
	public void rollback() {
		registry.rollbackRedefine(this);
	}

	void markCommitted() {
		state = State.COMMITTED;
	}

	void markRolledBack() {
		state = State.ROLLED_BACK;
	}

	void markRebuildViewWritten() {
		rebuildViewWritten = true;
	}

	boolean isRebuildViewWritten() {
		return rebuildViewWritten;
	}

	boolean isOwnedBy(SpatialIdentityRegistry candidateRegistry) {
		return registry == candidateRegistry;
	}

	SpatialRedefineCandidateParticipation getCandidateParticipation() {
		return candidateParticipation;
	}

	private static Map<String, PersistentGeoId> singletonDecidedIds(
			SpatialRedefineContext context, SpatialRedefineProposal proposal,
			SpatialRedefineDecision decision, PersistentGeoId freshId) {
		if (decision == SpatialRedefineDecision.REJECT) {
			return Collections.emptyMap();
		}
		PersistentGeoId decided = decision == SpatialRedefineDecision.RETAIN
				? context.getOldId() : java.util.Objects.requireNonNull(freshId);
		LinkedHashMap<String, PersistentGeoId> result = new LinkedHashMap<>();
		result.put(proposal.getTargetedStableOutputRole(), decided);
		return Collections.unmodifiableMap(result);
	}
}
