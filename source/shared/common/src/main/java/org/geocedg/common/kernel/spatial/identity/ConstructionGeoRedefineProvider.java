/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;

/**
 * Provider-validated redefine authority for neutral construction-defined geos.
 * Labels, coordinates, construction order and output ordinal are absent.
 */
public final class ConstructionGeoRedefineProvider
		implements SpatialRedefineProvider {
	public static final String PROVIDER_ID =
			"geocedg-construction-provider/v1";
	public static final String SCHEMA_ID = "geocedg-construction-geo";
	public static final int SCHEMA_VERSION = 1;
	public static final String STABLE_OUTPUT_ROLE = "VALUE";

	private final SpatialIdentityGraph graph;

	/** Creates the construction provider over one durable identity graph. */
	public ConstructionGeoRedefineProvider(SpatialIdentityGraph graph) {
		this.graph = Objects.requireNonNull(graph);
	}

	@Override
	public String getProviderId() {
		return PROVIDER_ID;
	}

	/** @return exact structural family persisted for a neutral ordinary geo */
	public static String familyFor(GeoElement geo) {
		return Objects.requireNonNull(geo).getGeoClassType().name();
	}

	@Override
	public SpatialRedefineSignature describeCandidate(
			SpatialRedefineContext context, GeoElement candidate) {
		return describeCandidate(context, candidate, graph);
	}

	@Override
	public SpatialRedefineSignature describeCandidate(
			SpatialRedefineContext context, GeoElement candidate,
			SpatialIdentityGraph candidateGraph) {
		SpatialRedefineSignature old = requireNeutralContext(context);
		if (!old.getFamily().equals(familyFor(candidate))) {
			throw new IllegalArgumentException(
					"Candidate changes the construction-defined geo family");
		}
		return new SpatialRedefineSignature(old.getProvider(), old.getFamily(),
				old.getSchemaId(), old.getSchemaVersion(), old.getAuthority(),
				old.getBindingRole(), old.getStableOutputRole(),
				old.getOutputCardinality(), dependencyIds(candidate,
						Objects.requireNonNull(candidateGraph)));
	}

	@Override
	public boolean isTopologyPreserving(SpatialRedefineContext context,
			GeoElement candidate) {
		try {
			return context.getOldSignature().equals(
					describeCandidate(context, candidate));
		} catch (RuntimeException exception) {
			return false;
		}
	}

	@Override
	public SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
			describeCandidateGroup(SpatialRedefineContext context,
					List<GeoElement> candidates,
					SpatialIdentityGraph candidateGraph) {
		if (context.getOldOutputs().size() != 1 || candidates.size() != 1) {
			throw new IllegalArgumentException(
					"Construction-defined redefine requires one output");
		}
		GeoElement candidate = Objects.requireNonNull(candidates.get(0));
		return SpatialRedefineOutputGroup.singleton(
				new SpatialRedefineCandidateOutput(candidate,
						describeCandidate(context, candidate, candidateGraph)));
	}

	@Override
	public SpatialRedefineEffect describeEffect(SpatialRedefineContext context,
			SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
					candidateOutputs) {
		if (context.getOldOutputs().size() != 1 || candidateOutputs.size() != 1) {
			throw new IllegalArgumentException(
					"Construction-defined redefine requires one unambiguous output");
		}
		SpatialRedefineCandidateOutput candidate = candidateOutputs.get(
				context.getTargetedStableOutputRole());
		if (candidate == null || !sameBase(context.getOldSignature(),
				candidate.getSignature())) {
			throw new IllegalArgumentException(
					"Construction-defined candidate changes its durable role contract");
		}
		if (!context.getOldSignature().getDependencies().equals(
				candidate.getSignature().getDependencies())) {
			return SpatialRedefineEffect.ADMITTED_TOPOLOGY_CHANGE;
		}
		SpatialRedefinePersistedOutput old = context.getOldOutputs().get(
				context.getTargetedStableOutputRole());
		return old != null && old.hasSameHostState(candidate)
				? SpatialRedefineEffect.NO_OP
				: SpatialRedefineEffect.DEFINITION_CHANGE;
	}

	@Override
	public SpatialRedefineDecision inspect(SpatialRedefineContext context,
			SpatialRedefineProposal proposal) {
		if (!proposal.isEffectExplicit()
				|| context.getOldOutputs().size() != 1
				|| proposal.getCandidateOutputs().size() != 1
				|| !context.getOldOutputs().getRoles().equals(
						proposal.getCandidateOutputs().getRoles())
				|| !sameBase(context.getOldSignature(), proposal.getSignature())
				|| !context.getOldSignature().getFamily().equals(
						familyFor(proposal.getCandidate()))) {
			return SpatialRedefineDecision.REJECT;
		}
		boolean dependenciesChanged = !context.getOldSignature().getDependencies()
				.equals(proposal.getSignature().getDependencies());
		if (dependenciesChanged
				&& !isPublicTopologyCandidate(proposal.getCandidate())) {
			return SpatialRedefineDecision.REJECT;
		}
		if (proposal.isReplacementOperationSelected() || dependenciesChanged) {
			return SpatialRedefineDecision.FRESH;
		}
		return proposal.getEffect() == SpatialRedefineEffect.ADMITTED_TOPOLOGY_CHANGE
				? SpatialRedefineDecision.REJECT : SpatialRedefineDecision.RETAIN;
	}

	private SpatialRedefineSignature requireNeutralContext(
			SpatialRedefineContext context) {
		if (context == null || context.getOldOutputs().size() != 1) {
			throw new IllegalArgumentException(
					"Construction-defined redefine context is ambiguous");
		}
		SpatialRedefineSignature signature = context.getOldSignature();
		if (!PROVIDER_ID.equals(signature.getProvider())
				|| !SCHEMA_ID.equals(signature.getSchemaId())
				|| signature.getSchemaVersion() != SCHEMA_VERSION
				|| signature.getAuthority()
						!= EditAuthorityMode.CONSTRUCTION_DEFINED
				|| signature.getBindingRole()
						!= ProjectionBindingRole.NOT_APPLICABLE
				|| signature.getOutputCardinality() != 1) {
			throw new IllegalArgumentException(
					"Context is not a neutral construction-defined geo");
		}
		return signature;
	}

	private List<PersistentGeoId> dependencyIds(GeoElement geo,
			SpatialIdentityGraph identityGraph) {
		ArrayList<PersistentGeoId> dependencies = new ArrayList<>();
		for (GeoElement input : durableDependencyGeos(geo)) {
			PersistentGeoId id = identityGraph.getPersistentGeoId(input);
			if (id == null) {
				throw new IllegalArgumentException(
						"Construction-defined candidate has an unregistered dependency");
			}
			if (!dependencies.contains(id)) {
				dependencies.add(id);
			}
		}
		Collections.sort(dependencies);
		return Collections.unmodifiableList(dependencies);
	}

	/**
	 * Returns the direct construction geos whose already-published identities form
	 * this output's durable dependency edge set. The productive Locus V2 parent has
	 * an explicit seam because its serialized command inputs are intentionally
	 * narrower than its reconstructible evaluator inputs.
	 */
	static List<GeoElement> durableDependencyGeos(GeoElement geo) {
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent == null) {
			return Collections.emptyList();
		}
		if (parent instanceof AlgoDependentPointLocusV2) {
			return ((AlgoDependentPointLocusV2) parent)
					.getDurableDependencyGeos();
		}
		ArrayList<GeoElement> inputs = new ArrayList<>();
		for (GeoElement input : parent.getInput()) {
			inputs.add(input);
		}
		return inputs;
	}

	private static boolean isPublicTopologyCandidate(GeoElement candidate) {
		return isPublicLocusV2Output(candidate);
	}

	/** @return whether this geo is one of the typed public G9U0 outputs */
	static boolean isPublicLocusV2Output(GeoElement candidate) {
		if (candidate instanceof GeoLocusV2
				|| candidate instanceof GeoLocusMetricResult
				|| candidate instanceof GeoLocusIntersectionResult) {
			return true;
		}
		AlgoElement parent = candidate.getParentAlgorithm();
		return parent instanceof AlgoSemanticLocusPoint2D
				|| parent instanceof AlgoLocusIntersectionPointV2
				|| parent instanceof AlgoLocusMetricScalarAdapter;
	}

	private static boolean sameBase(SpatialRedefineSignature first,
			SpatialRedefineSignature second) {
		return first.getProvider().equals(second.getProvider())
				&& first.getFamily().equals(second.getFamily())
				&& first.getSchemaId().equals(second.getSchemaId())
				&& first.getSchemaVersion() == second.getSchemaVersion()
				&& first.getAuthority() == second.getAuthority()
				&& first.getBindingRole() == second.getBindingRole()
				&& first.getStableOutputRole().equals(
						second.getStableOutputRole())
				&& first.getOutputCardinality()
						== second.getOutputCardinality();
	}
}
