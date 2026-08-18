/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.List;
import java.util.Objects;

import org.geogebra.common.kernel.geos.GeoElement;

/** Provider gate that inspects the actual redefine result before host mutation. */
public interface SpatialRedefineProvider {
	/** @return the stable provider identifier stored in participating geo records */
	String getProviderId();

	/**
	 * Describes the actual parsed candidate from provider-owned semantics. Host
	 * class, label, command equality and output ordinal are not valid inputs.
	 *
	 * @return the provider-owned candidate signature
	 */
	SpatialRedefineSignature describeCandidate(SpatialRedefineContext context,
			GeoElement candidate);

	/**
	 * Proves whether this actual candidate preserves the provider-owned topology.
	 * Host class, label and instance reuse are not proof.
	 *
	 * @return true only with provider-owned topology evidence
	 */
	boolean isTopologyPreserving(SpatialRedefineContext context,
			GeoElement candidate);

	/**
	 * Assigns every candidate to a unique provider-owned stable role. The default
	 * adapter preserves G9A1 source compatibility and deliberately admits only a
	 * single old output and a single candidate. Multi-output providers must
	 * override this method; host ordinal is never exposed as a mapping key.
	 *
	 * @param context complete persisted old-output authority
	 * @param candidates host outputs used only as an unordered enumeration
	 * @return complete provider-owned candidate role map
	 */
	default SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
			describeCandidateGroup(SpatialRedefineContext context,
					List<GeoElement> candidates) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(candidates);
		if (context.getOldOutputs().size() != 1 || candidates.size() != 1) {
			throw new IllegalArgumentException(
					"Legacy redefine provider supports one output only");
		}
		GeoElement candidate = Objects.requireNonNull(candidates.get(0));
		SpatialRedefineSignature signature = Objects.requireNonNull(
				describeCandidate(context, candidate));
		return SpatialRedefineOutputGroup.singleton(
				new SpatialRedefineCandidateOutput(candidate, signature));
	}

	/**
	 * Freezes the semantic effect before host mutation. The compatibility adapter
	 * admits only the legacy topology-preserving case as a definition change;
	 * legacy providers cannot implicitly authorize topology changes.
	 *
	 * @return explicit provider-owned effect
	 */
	default SpatialRedefineEffect describeEffect(SpatialRedefineContext context,
			SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
					candidateOutputs) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(candidateOutputs);
		if (context.getOldOutputs().size() != 1 || candidateOutputs.size() != 1) {
			throw new IllegalArgumentException(
					"Legacy redefine provider cannot describe a group effect");
		}
		SpatialRedefineCandidateOutput candidate = candidateOutputs.get(
				context.getTargetedStableOutputRole());
		if (candidate == null
				|| !isTopologyPreserving(context, candidate.getGeo())) {
			throw new IllegalArgumentException(
					"Legacy redefine provider did not prove topology preservation");
		}
		return SpatialRedefineEffect.DEFINITION_CHANGE;
	}

	/** @return the provider's pre-mutation identity decision */
	SpatialRedefineDecision inspect(SpatialRedefineContext context,
			SpatialRedefineProposal proposal);
}
