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
import org.geocedg.common.kernel.locus.LocusPrincipalBranchState2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddressState2D;
import org.geocedg.common.kernel.locus.V2RedefineContractSource;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoText;

/**
 * Provider-validated redefine authority for neutral construction-defined geos.
 * Labels, coordinates, construction order and output ordinal are absent.
 */
public final class ConstructionGeoRedefineProvider
		implements SpatialRedefineProvider {
	public static final String PROVIDER_ID =
			"geocedg-construction-provider/v1";
	public static final String SCHEMA_ID = "geocedg-construction-geo";
	/** Historical direct durable-dependency projection (PRE-G9B-R3, ADR 0029). */
	public static final int SCHEMA_VERSION_DIRECT = 1;
	/** Transitive durable-frontier projection (PRE-G9B-R3, ADR 0029). */
	public static final int SCHEMA_VERSION_TRANSITIVE_FRONTIER = 2;
	/** Version of every new publication, replacement, copy or explicit upgrade. */
	public static final int SCHEMA_VERSION = SCHEMA_VERSION_TRANSITIVE_FRONTIER;
	public static final String STABLE_OUTPUT_ROLE = "VALUE";
	/** Durable role reserved for an interaction-owned semantic Locus V2 point. */
	public static final String INTERACTION_POINT_OUTPUT_ROLE =
			"LOCUS_INTERACTION_POINT";
	/** Durable role for a Point(L,u) output with one retained exact selector. */
	public static final String PRINCIPAL_BRANCH_POINT_OUTPUT_ROLE =
			"LOCUS_PRINCIPAL_BRANCH_POINT";

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
			throw SpatialRedefineDescriptionException.durableContractChange(
					"Candidate changes the construction-defined geo family");
		}
		return new SpatialRedefineSignature(old.getProvider(), old.getFamily(),
				old.getSchemaId(), old.getSchemaVersion(), old.getAuthority(),
				old.getBindingRole(), candidateStableOutputRole(candidate,
						candidateGraph),
				old.getOutputCardinality(), dependencyIds(candidate,
						Objects.requireNonNull(candidateGraph)));
	}

	@Override
	public boolean isTopologyPreserving(SpatialRedefineContext context,
			GeoElement candidate) {
		try {
			return context.getOldAssessmentSignature().equals(
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
		if (context.getOldOutputs().size() != 1) {
			throw SpatialRedefineDescriptionException.ambiguous(
					"Construction-defined redefine requires one output");
		}
		if (candidates.size() != 1) {
			throw SpatialRedefineDescriptionException.durableContractChange(
					"Candidate changes the construction-defined output cardinality");
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
			throw SpatialRedefineDescriptionException.ambiguous(
					"Construction-defined redefine requires one unambiguous output");
		}
		SpatialRedefineCandidateOutput candidate = candidateOutputs.get(
				context.getTargetedStableOutputRole());
		if (candidate == null || !sameBase(context.getOldAssessmentSignature(),
				candidate.getSignature())) {
			throw SpatialRedefineDescriptionException.durableContractChange(
					"Construction-defined candidate changes its durable role contract");
		}
		if (!context.getOldAssessmentSignature().getDependencies().equals(
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
				|| !sameBase(context.getOldAssessmentSignature(),
						proposal.getSignature())
				|| !context.getOldAssessmentSignature().getFamily().equals(
						familyFor(proposal.getCandidate()))) {
			return SpatialRedefineDecision.REJECT;
		}
		boolean dependenciesChanged = !context.getOldAssessmentSignature()
				.getDependencies().equals(proposal.getSignature().getDependencies());
		if (dependenciesChanged
				&& !isPublicTopologyCandidate(proposal.getCandidate())) {
			// PRE-G9B-R3: a durable-contract change of an ordinary participant is
			// never retained or replaced implicitly. Each path needs its own explicit
			// intent, and retention also needs the certified predicate.
			if (proposal.isReplacementOperationSelected()) {
				return SpatialRedefineDecision.FRESH;
			}
			return proposal.isContractUpdateSelected()
					&& isIdentityPreservingContractUpdate(context, proposal)
							? SpatialRedefineDecision.RETAIN
							: SpatialRedefineDecision.REJECT;
		}
		boolean oldV2 = isV2RedefineSource(context.getOldTarget());
		boolean candidateV2 = isV2RedefineSource(proposal.getCandidate());
		if (oldV2 || candidateV2) {
			if (!oldV2 || !candidateV2
					|| !sameV2RedefineContract(context.getOldTarget(),
							proposal.getCandidate())) {
				return proposal.isReplacementOperationSelected()
						? SpatialRedefineDecision.FRESH
						: SpatialRedefineDecision.REJECT;
			}
			return proposal.isReplacementOperationSelected()
					? SpatialRedefineDecision.FRESH
					: SpatialRedefineDecision.RETAIN;
		}
		if (proposal.isReplacementOperationSelected() || dependenciesChanged) {
			return SpatialRedefineDecision.FRESH;
		}
		return proposal.getEffect() == SpatialRedefineEffect.ADMITTED_TOPOLOGY_CHANGE
				? SpatialRedefineDecision.REJECT : SpatialRedefineDecision.RETAIN;
	}

	@Override
	public boolean requiresProceduralPositionPreservation(
			SpatialRedefineContext context, SpatialRedefineProposal proposal,
			SpatialRedefineDecision decision) {
		return decision == SpatialRedefineDecision.RETAIN
				&& sameV2RedefineContract(context.getOldTarget(),
						proposal.getCandidate());
	}

	/**
	 * A durable-contract change of an ordinary participant is a changed durable
	 * dependency frontier of a non-public candidate whose contract is otherwise
	 * identical. It is assessed as {@code DURABLE_CONTRACT_CHANGE} and never
	 * executed without an explicitly selected operation.
	 */
	@Override
	public boolean isDurableContractChange(SpatialRedefineContext context,
			SpatialRedefineProposal proposal) {
		return proposal.getEffect() == SpatialRedefineEffect.ADMITTED_TOPOLOGY_CHANGE
				&& !isPublicTopologyCandidate(proposal.getCandidate())
				&& !isV2RedefineSource(context.getOldTarget());
	}

	/**
	 * Closed identity-preserving contract-update predicate (PRE-G9B-R3 decision
	 * 5): the same provider, family, schema family, authority, binding role, stable
	 * role and cardinality; a stable role the candidate supports; and no V2 source
	 * on either side. The registry has already excluded cycles, stale state and
	 * incomplete groups. The frontier itself may change.
	 */
	private static boolean isIdentityPreservingContractUpdate(
			SpatialRedefineContext context, SpatialRedefineProposal proposal) {
		SpatialRedefineSignature old = context.getOldAssessmentSignature();
		SpatialRedefineSignature candidate = proposal.getSignature();
		return sameBase(old, candidate)
				&& old.getFamily().equals(familyFor(proposal.getCandidate()))
				&& supportsStableOutputRole(proposal.getCandidate(),
						candidate.getStableOutputRole())
				&& !isV2RedefineSource(context.getOldTarget())
				&& !isV2RedefineSource(proposal.getCandidate());
	}

	private SpatialRedefineSignature requireNeutralContext(
			SpatialRedefineContext context) {
		if (context == null || context.getOldOutputs().size() != 1) {
			throw SpatialRedefineDescriptionException.ambiguous(
					"Construction-defined redefine context is ambiguous");
		}
		SpatialRedefineSignature signature = context.getOldSignature();
		SpatialRedefineSignature current = context.getOldAssessmentSignature();
		if (!PROVIDER_ID.equals(signature.getProvider())
				|| !SCHEMA_ID.equals(signature.getSchemaId())
				|| !DurableDependencyProjection.isSupportedSchemaVersion(
						signature.getSchemaVersion())
				|| current.getSchemaVersion() != SCHEMA_VERSION
				|| signature.getAuthority()
						!= EditAuthorityMode.CONSTRUCTION_DEFINED
				|| signature.getBindingRole()
						!= ProjectionBindingRole.NOT_APPLICABLE
				|| signature.getOutputCardinality() != 1) {
			throw SpatialRedefineDescriptionException.undescribable(
					"Context is not a neutral construction-defined geo");
		}
		return current;
	}

	private static List<PersistentGeoId> dependencyIds(GeoElement geo,
			SpatialIdentityGraph identityGraph) {
		// PRE-G9B-R3 D3-a: the canonical projection reaches the nearest durable
		// ancestors through identity-free helpers instead of rejecting them.
		return DurableDependencyProjection.TRANSITIVE_DURABLE_FRONTIER.project(geo,
				identityGraph::getPersistentGeoId);
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

	/**
	 * Validates the stable role carried by one neutral construction output.
	 * Interaction ownership is meaningful only for the semantic-point algorithm;
	 * every other neutral construction output retains the ordinary VALUE role.
	 */
	static boolean supportsStableOutputRole(GeoElement geo, String role) {
		if (STABLE_OUTPUT_ROLE.equals(role)) {
			return true;
		}
		return (INTERACTION_POINT_OUTPUT_ROLE.equals(role)
				&& hasDedicatedInteractionPointState(geo))
				|| (PRINCIPAL_BRANCH_POINT_OUTPUT_ROLE.equals(role)
				&& hasDedicatedPrincipalBranchState(geo));
	}

	/**
	 * @return whether the semantic point owns independent hidden address inputs
	 *         rather than borrowing ordinary user construction state
	 */
	public static boolean hasDedicatedInteractionPointState(GeoElement geo) {
		if (!(geo.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D)) {
			return false;
		}
		AlgoSemanticLocusPoint2D parent =
				(AlgoSemanticLocusPoint2D) geo.getParentAlgorithm();
		GeoText branch = parent.getBranchInput();
		GeoElement parameter = parent.getParameterInput().toGeoElement();
		try {
			if (LocusSemanticAddressState2D.decode(branch.getTextString()) == null) {
				return false;
			}
		} catch (IllegalArgumentException exception) {
			return false;
		}
		return parameter instanceof GeoNumeric && branch.isIndependent()
				&& parameter.isIndependent()
				&& branch.getConstruction() == geo.getConstruction()
				&& parameter.getConstruction() == geo.getConstruction()
				&& branch.getAlgorithmList().size() == 1
				&& branch.getAlgorithmList().contains(parent)
				&& parameter.getAlgorithmList().size() == 1
				&& parameter.getAlgorithmList().contains(parent);
	}

	private static boolean sameV2RedefineContract(GeoElement first,
			GeoElement second) {
		String firstContract = v2RedefineContract(first);
		String secondContract = v2RedefineContract(second);
		return firstContract != null && firstContract.equals(secondContract);
	}

	private static boolean isV2RedefineSource(GeoElement geo) {
		return geo instanceof GeoLocusV2;
	}

	private static String v2RedefineContract(GeoElement geo) {
		AlgoElement parent = geo == null ? null : geo.getParentAlgorithm();
		if (!(parent instanceof V2RedefineContractSource)) {
			return null;
		}
		String contract = ((V2RedefineContractSource) parent)
				.getV2RedefineContractId();
		if (contract == null || contract.trim().isEmpty()
				|| !contract.equals(contract.trim())) {
			return null;
		}
		return contract;
	}

	/** @return whether only the hidden selector input is owned by Point(L,u) */
	public static boolean hasDedicatedPrincipalBranchState(GeoElement geo) {
		if (!(geo.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D)) {
			return false;
		}
		AlgoSemanticLocusPoint2D parent =
				(AlgoSemanticLocusPoint2D) geo.getParentAlgorithm();
		GeoText branch = parent.getBranchInput();
		try {
			if (LocusPrincipalBranchState2D.decode(branch.getTextString()) == null) {
				return false;
			}
		} catch (IllegalArgumentException exception) {
			return false;
		}
		return branch.isIndependent()
				&& branch.getConstruction() == geo.getConstruction()
				&& branch.getAlgorithmList().size() == 1
				&& branch.getAlgorithmList().contains(parent);
	}

	private static String candidateStableOutputRole(GeoElement candidate,
			SpatialIdentityGraph candidateGraph) {
		PersistentGeoId id = candidateGraph.getPersistentGeoId(candidate);
		SpatialIdentityRecord record = id == null ? null
				: candidateGraph.getRecord(id);
		if (record instanceof GeoIdentityRecord) {
			String role = ((GeoIdentityRecord) record).getStableOutputRole();
			if (supportsStableOutputRole(candidate, role)) {
				return role;
			}
		}
		return STABLE_OUTPUT_ROLE;
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
