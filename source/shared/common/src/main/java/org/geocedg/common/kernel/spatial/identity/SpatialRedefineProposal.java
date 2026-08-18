/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.Objects;

import org.geogebra.common.kernel.geos.GeoElement;

/** Actual parsed result and provider signature inspected before host mutation. */
public final class SpatialRedefineProposal {
	private final SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
			candidateOutputs;
	private final String targetedStableOutputRole;
	private final int targetedOutputCount;
	private final SpatialRedefineEffect effect;
	private final boolean effectExplicit;
	private final boolean legacyTopologyPreserving;
	private final boolean replacementOperationSelected;

	/** Creates provider-described evidence for an actual parsed candidate. */
	public SpatialRedefineProposal(GeoElement candidate,
			SpatialRedefineSignature signature, int targetedOutputCount,
			boolean topologyPreserving) {
		this(candidate, signature, targetedOutputCount, topologyPreserving, false);
	}

	/** Creates evidence including explicit user selection of true replacement. */
	public SpatialRedefineProposal(GeoElement candidate,
			SpatialRedefineSignature signature, int targetedOutputCount,
			boolean topologyPreserving, boolean replacementOperationSelected) {
		this(SpatialRedefineOutputGroup.singleton(
				new SpatialRedefineCandidateOutput(candidate, signature)),
				signature.getStableOutputRole(), targetedOutputCount,
				topologyPreserving ? SpatialRedefineEffect.DEFINITION_CHANGE
						: SpatialRedefineEffect.ADMITTED_TOPOLOGY_CHANGE,
				false, topologyPreserving, replacementOperationSelected);
	}

	/** Creates provider-owned evidence for a complete stable-role group. */
	public SpatialRedefineProposal(
			SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
					candidateOutputs,
			String targetedStableOutputRole, SpatialRedefineEffect effect,
			boolean replacementOperationSelected) {
		this(candidateOutputs, targetedStableOutputRole, candidateOutputs.size(),
				effect, true,
				effect != SpatialRedefineEffect.ADMITTED_TOPOLOGY_CHANGE,
				replacementOperationSelected);
	}

	private SpatialRedefineProposal(
			SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
					candidateOutputs,
			String targetedStableOutputRole, int targetedOutputCount,
			SpatialRedefineEffect effect, boolean effectExplicit,
			boolean legacyTopologyPreserving,
			boolean replacementOperationSelected) {
		this.candidateOutputs = Objects.requireNonNull(candidateOutputs);
		this.targetedStableOutputRole = SpatialRecordSupport.requireText(
				targetedStableOutputRole, "targetedStableOutputRole");
		if (!candidateOutputs.containsRole(this.targetedStableOutputRole)) {
			throw new IllegalArgumentException(
					"Targeted stable role is absent from the candidate group");
		}
		this.targetedOutputCount = SpatialRecordSupport.requirePositive(
				targetedOutputCount, "targetedOutputCount");
		this.effect = Objects.requireNonNull(effect);
		this.effectExplicit = effectExplicit;
		this.legacyTopologyPreserving = legacyTopologyPreserving;
		this.replacementOperationSelected = replacementOperationSelected;
	}

	public GeoElement getCandidate() {
		return targetedOutput().getGeo();
	}

	public SpatialRedefineSignature getSignature() {
		return targetedOutput().getSignature();
	}

	/** @return complete provider-owned candidate map keyed by stable role */
	public SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
			getCandidateOutputs() {
		return candidateOutputs;
	}

	/** @return stable role corresponding to the explicit old target */
	public String getTargetedStableOutputRole() {
		return targetedStableOutputRole;
	}

	public int getTargetedOutputCount() {
		return targetedOutputCount;
	}

	public boolean isTopologyPreserving() {
		return effectExplicit
				? effect != SpatialRedefineEffect.ADMITTED_TOPOLOGY_CHANGE
				: legacyTopologyPreserving;
	}

	/** @return the frozen provider-owned semantic effect */
	public SpatialRedefineEffect getEffect() {
		return effect;
	}

	/** @return whether the effect came through the explicit G9A3 provider API */
	public boolean isEffectExplicit() {
		return effectExplicit;
	}

	/** @return whether the caller explicitly selected true semantic replacement */
	public boolean isReplacementOperationSelected() {
		return replacementOperationSelected;
	}

	private SpatialRedefineCandidateOutput targetedOutput() {
		return candidateOutputs.get(targetedStableOutputRole);
	}
}
