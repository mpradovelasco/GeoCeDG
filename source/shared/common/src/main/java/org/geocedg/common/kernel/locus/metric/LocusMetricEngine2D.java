/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCertificationScope2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;

/**
 * Internal semantic metric service composing route, component and aggregation
 * responsibilities without render or legacy-sample authority.
 */
public final class LocusMetricEngine2D {
	private final LocusMetricRouteResolver2D routeResolver =
			new LocusMetricRouteResolver2D();
	private final LocusMetricAggregator2D aggregator =
			new LocusMetricAggregator2D();

	/**
	 * Computes one query against a coherent source revision.
	 *
	 * @return rich immutable metric result
	 */
	public LocusMetricResult2D compute(LocusMetricQuery2D query,
			LocusDefinition2D definition,
			LocusMetricCapabilityHierarchy2D capabilities,
			LocusMetricSharedOwner2D sharedOwner,
			LocusMetricIndexMode indexMode,
			LocusMetricInstrumentation2D instrumentation) {
		return compute(query, definition, capabilities, sharedOwner, indexMode,
				instrumentation, "");
	}

	/**
	 * Computes with an instrumentation-only metric-consumer token.
	 *
	 * @return rich immutable metric result
	 */
	public LocusMetricResult2D compute(LocusMetricQuery2D query,
			LocusDefinition2D definition,
			LocusMetricCapabilityHierarchy2D capabilities,
			LocusMetricSharedOwner2D sharedOwner,
			LocusMetricIndexMode indexMode,
			LocusMetricInstrumentation2D instrumentation,
			String consumerToken) {
		LocusCertificationScope2D.recordMetricRead();
		if (!query.getLocusIdentity().equals(definition.getLocusIdentity())
				|| query.getSemanticRevision()
						!= definition.getSemanticRevision()) {
			return invalidQuery(query, definition,
					MetricDiagnosticCode2D.POSITION_STALE,
					"Query identity/revision does not match current semantic source",
					Optional.empty());
		}
		if (definition.getDefinitionStatus() == DefinitionStatus.EMPTY_DOMAIN) {
			return emptyTotalOrInvalidBetween(query);
		}
		if (definition.getDefinitionStatus() != DefinitionStatus.VALID) {
			return invalidQuery(query, definition,
					MetricDiagnosticCode2D.EVALUATION_FAILED,
					"Source locus is not semantically defined",
					query instanceof BetweenPositionsMetricQuery
							? Optional.of(TraversalOutcome.TARGET_NOT_REACHABLE)
							: Optional.empty());
		}
		if (query instanceof TotalLocusMetricQuery) {
			return computeTotal((TotalLocusMetricQuery) query, definition,
					capabilities, sharedOwner, indexMode, instrumentation,
					consumerToken);
		}
		return computeBetween((BetweenPositionsMetricQuery) query, definition,
				capabilities, sharedOwner, indexMode, instrumentation,
				consumerToken);
	}

	private LocusMetricResult2D computeTotal(TotalLocusMetricQuery query,
			LocusDefinition2D definition,
			LocusMetricCapabilityHierarchy2D capabilities,
			LocusMetricSharedOwner2D owner, LocusMetricIndexMode indexMode,
			LocusMetricInstrumentation2D instrumentation,
			String consumerToken) {
		// A branch that is not COMPLETE has a total only when its construction is outside
		// class v3: the conditioned estimate of its declared pieces (section 26.5).
		Map<String, List<LocusInterval2D>> declared = new LinkedHashMap<>();
		for (LocusBranch2D branch : definition.getBranches()) {
			if (branch.getExistenceStructure().getCompleteness()
					== Completeness.COMPLETE) {
				continue;
			}
			List<LocusInterval2D> pieces = declaredPieces(branch);
			if (pieces.isEmpty()) {
				return incompleteTotal(query, definition);
			}
			declared.put(branch.getBranchKey(), pieces);
		}
		List<LocusMetricContribution2D> contributions = new ArrayList<>();
		for (LocusBranch2D branch : definition.getBranches()) {
			List<LocusInterval2D> pieces = declared.get(branch.getBranchKey());
			if (pieces != null) {
				for (int index = 0; index < pieces.size(); index++) {
					LocusInterval2D piece = pieces.get(index);
					CapabilityState selected = conditionedState(definition, branch, piece,
							LocusMetricComponentKey2D.createDeclared(definition, branch,
									index, piece),
							query.getPolicy(), capabilities, owner, indexMode,
							instrumentation, consumerToken);
					if (selected == null) {
						return incompleteTotal(query, definition);
					}
					contributions.add(selected.capability.evaluateCompleteComponent(
							selected.state, query.getPolicy(), instrumentation));
				}
				continue;
			}
			for (int index = 0;
					index < branch.getValidDomainComponents().size(); index++) {
				String componentKey = LocusMetricComponentKey2D.create(
						definition, branch, index);
				LocusInterval2D component =
						branch.getValidDomainComponents().get(index);
				CapabilityState selected = state(definition, branch, component,
						componentKey, query.getPolicy(), capabilities, owner,
						indexMode, instrumentation, consumerToken);
				contributions.add(selected.capability
						.evaluateCompleteComponent(selected.state,
								query.getPolicy(), instrumentation));
			}
		}
		return aggregator.aggregateTotal(query, contributions);
	}

	private LocusMetricResult2D computeBetween(
			BetweenPositionsMetricQuery query, LocusDefinition2D definition,
			LocusMetricCapabilityHierarchy2D capabilities,
			LocusMetricSharedOwner2D owner, LocusMetricIndexMode indexMode,
			LocusMetricInstrumentation2D instrumentation,
			String consumerToken) {
		LocusMetricRoute2D route = routeResolver.resolve(query, definition);
		if (route.getRouteStatus() != MetricRouteStatus.RESOLVED) {
			return aggregator.aggregateBetween(query, route,
					Collections.emptyList());
		}
		List<LocusMetricContribution2D> contributions = new ArrayList<>();
		if (route.getOrderedRouteSegments().isEmpty()) {
			contributions.add(zeroContribution(query, definition, route));
		} else {
			LocusBranch2D branch = definition.getBranch(route.getBranchKey());
			Map<String, CapabilityState> queryStates =
					new LinkedHashMap<>();
			for (LocusMetricRouteSegment2D segment
					: route.getOrderedRouteSegments()) {
				String componentKey =
						segment.getResolvedValidComponentKey();
				CapabilityState selected = queryStates.get(componentKey);
				if (selected == null) {
					LocusInterval2D component =
							LocusMetricComponentKey2D.find(definition, branch,
									componentKey);
					selected = state(definition, branch, component,
							componentKey, query.getPolicy(), capabilities,
							owner, indexMode, instrumentation, consumerToken);
					queryStates.put(componentKey, selected);
				}
				contributions.add(selected.capability.evaluateRouteSegment(
						selected.state, segment, query.getPolicy(),
						instrumentation));
			}
		}
		return aggregator.aggregateBetween(query, route, contributions);
	}

	private static CapabilityState state(LocusDefinition2D definition,
			LocusBranch2D branch, LocusInterval2D component,
			String componentKey, LocusMetricPolicy2D policy,
			LocusMetricCapabilityHierarchy2D hierarchy,
			LocusMetricSharedOwner2D owner, LocusMetricIndexMode indexMode,
			LocusMetricInstrumentation2D instrumentation,
			String consumerToken) {
		LocusMetricCapability2D capability = select(definition, branch, policy,
				hierarchy);
		LocusMetricIndexKey2D key = indexKey(definition, branch, componentKey,
				policy, capability);
		return new CapabilityState(capability, build(key, ignored ->
				capability.buildComponentState(definition, branch, component,
						key, policy, instrumentation), owner, indexMode,
				instrumentation, consumerToken));
	}

	/**
	 * State of the conditioned estimate of one declared piece (section 26.5): only the
	 * public metric policy v2 of CU1 has one.
	 *
	 * @return the state, or {@code null} when the selected capability has no conditioned
	 *         estimate
	 */
	private static CapabilityState conditionedState(LocusDefinition2D definition,
			LocusBranch2D branch, LocusInterval2D piece, String pieceKey,
			LocusMetricPolicy2D policy, LocusMetricCapabilityHierarchy2D hierarchy,
			LocusMetricSharedOwner2D owner, LocusMetricIndexMode indexMode,
			LocusMetricInstrumentation2D instrumentation,
			String consumerToken) {
		LocusMetricCapability2D capability = select(definition, branch, policy,
				hierarchy);
		if (!(capability instanceof PolicyV2LocusMetricCapability2D)) {
			return null;
		}
		PolicyV2LocusMetricCapability2D estimator =
				(PolicyV2LocusMetricCapability2D) capability;
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		boolean wholePeriod = structure.getTopology() == DomainTopology.PERIODIC
				&& piece.equals(structure.getCanonicalDomain());
		LocusMetricIndexKey2D key = indexKey(definition, branch, pieceKey, policy,
				capability);
		return new CapabilityState(capability, build(key, ignored ->
				estimator.buildConditionedEstimateState(definition, branch, piece,
						wholePeriod, key, policy, instrumentation), owner, indexMode,
				instrumentation, consumerToken));
	}

	/**
	 * The declared pieces of a branch whose existence is not established only because its
	 * construction is outside class v3 (section 26.5): no certified fact, every coverage
	 * item unresolved by {@code OPERATION_OUTSIDE_CLASS} or the exact complement of the
	 * declared domain. Certified nonexistence, a demonstrated discontinuity, a partial
	 * certificate or any other cause leaves the branch without a total.
	 *
	 * @return the declared pieces in canonical order, or an empty list
	 */
	private static List<LocusInterval2D> declaredPieces(LocusBranch2D branch) {
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		if (!structure.getContinuousValidComponents().isEmpty()
				|| !structure.getPointComponents().isEmpty()
				|| !structure.getSingularParameters().isEmpty()
				|| !structure.getCertifiedFolds().isEmpty()) {
			return Collections.emptyList();
		}
		List<LocusInterval2D> pieces = new ArrayList<>();
		for (LocusExistenceStructure2D.Coverage item : structure.getCoverage()) {
			if (item.getState() == CoverageState.UNRESOLVED
					&& item.getCause() == LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS) {
				pieces.add(item.getInterval());
			} else if (item.getState() != CoverageState.CERTIFIED_DOES_NOT_EXIST
					|| item.getCause() != LocusCoverageCause2D.PROVIDER_COMPLEMENT) {
				return Collections.emptyList();
			}
		}
		return pieces;
	}

	private static LocusMetricCapability2D select(LocusDefinition2D definition,
			LocusBranch2D branch, LocusMetricPolicy2D policy,
			LocusMetricCapabilityHierarchy2D hierarchy) {
		return hierarchy.select(definition, branch, policy).orElseGet(() ->
				new UnsupportedLocusMetricCapability2D("unsupported/v1"));
	}

	private static LocusMetricIndexKey2D indexKey(LocusDefinition2D definition,
			LocusBranch2D branch, String componentKey, LocusMetricPolicy2D policy,
			LocusMetricCapability2D capability) {
		String capabilityVersion = definition.getProvider().getProviderId()
				+ "|" + capability.getCapabilityVersion();
		return new LocusMetricIndexKey2D(definition.getLocusIdentity(),
				definition.getSemanticRevision(), branch.getBranchKey(),
				componentKey, capabilityVersion, policy);
	}

	private static LocusMetricComponentState2D build(LocusMetricIndexKey2D key,
			LocusMetricComponentStateBuilder2D builder,
			LocusMetricSharedOwner2D owner, LocusMetricIndexMode indexMode,
			LocusMetricInstrumentation2D instrumentation,
			String consumerToken) {
		LocusMetricComponentState2D componentState;
		if (indexMode == LocusMetricIndexMode.LAZY_COMPONENT_REVISION) {
			componentState = owner.getOrBuildComponentState(key, builder,
					consumerToken);
		} else {
			instrumentation.recordComponentStateBuildStarted();
			try {
				componentState = builder.buildComponentState(key);
			} catch (RuntimeException exception) {
				instrumentation.recordComponentStateBuildFailure();
				throw exception;
			} finally {
				instrumentation.recordComponentStateBuildFinished();
			}
		}
		return componentState;
	}

	private static LocusMetricContribution2D zeroContribution(
			BetweenPositionsMetricQuery query, LocusDefinition2D definition,
			LocusMetricRoute2D route) {
		String component = query.getStart().getResolvedValidComponentKey()
				.orElse("zero-position");
		return new LocusMetricContribution2D(route.getBranchKey(), component,
				new FiniteMetricValue2D(0),
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				MetricEvaluatorMethod2D.ANALYTIC,
				MetricMethod2D.CLOSED_FORM,
				MetricErrorEvidence2D.exact(
						"equal semantic position ZERO_LENGTH"),
				new MetricProvenance2D(definition.getLocusIdentity(),
						definition.getSemanticRevision(),
						"semantic-zero/v1",
						query.getPolicy().getMetricAlgorithmVersion(),
						query.getPolicy().getMetricPolicyVersion()),
				Collections.emptyList());
	}

	private static LocusMetricResult2D emptyTotalOrInvalidBetween(
			LocusMetricQuery2D query) {
		if (query instanceof BetweenPositionsMetricQuery) {
			return invalidQuery(query, null,
					MetricDiagnosticCode2D.EMPTY_DOMAIN,
					"Between-position query has no valid semantic position",
					Optional.of(TraversalOutcome.TARGET_NOT_REACHABLE));
		}
		MetricProvenance2D provenance = new MetricProvenance2D(
				query.getLocusIdentity(), query.getSemanticRevision(),
				"empty-domain/v1",
				query.getPolicy().getMetricAlgorithmVersion(),
				query.getPolicy().getMetricPolicyVersion());
		MetricDiagnostic2D diagnostic = new MetricDiagnostic2D(
				MetricDiagnosticCode2D.EMPTY_DOMAIN,
				"Empty locus domain has complete zero constructive length");
		return new LocusMetricResult2D(new FiniteMetricValue2D(0),
				MetricCoverage.COMPLETE,
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE, Optional.empty(),
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				MetricEvaluatorMethod2D.ANALYTIC,
				MetricMethod2D.CLOSED_FORM,
				MetricRepresentationRole2D.SEMANTIC_METRIC,
				MetricErrorEvidence2D.exact("empty-domain total variation"),
				MetricUnit2D.CONSTRUCTION_LENGTH_UNIT, provenance,
				Collections.emptyList(),
				Collections.singletonList(diagnostic));
	}

	private static LocusMetricResult2D incompleteTotal(TotalLocusMetricQuery query,
			LocusDefinition2D definition) {
		return invalidQuery(query, definition,
				MetricDiagnosticCode2D.INCOMPLETE_AGGREGATE,
				"Total locus metric requires complete existence coverage",
				Optional.empty());
	}

	private static LocusMetricResult2D invalidQuery(
			LocusMetricQuery2D query, LocusDefinition2D definition,
			MetricDiagnosticCode2D code, String message,
			Optional<TraversalOutcome> outcome) {
		long revision = definition == null ? query.getSemanticRevision()
				: definition.getSemanticRevision();
		MetricProvenance2D provenance = new MetricProvenance2D(
				query.getLocusIdentity(), revision, "query-validation/v1",
				query.getPolicy().getMetricAlgorithmVersion(),
				query.getPolicy().getMetricPolicyVersion());
		return new LocusMetricResult2D(new AbsentMetricValue2D(),
				MetricCoverage.INCOMPLETE,
				MetricComputationStatus.INVALID_QUERY,
				MetricRectifiability.UNDETERMINED, outcome,
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				MetricEvaluatorMethod2D.NONE, MetricMethod2D.NONE,
				MetricRepresentationRole2D.DIAGNOSTIC_PARTIAL_VALUE,
				MetricErrorEvidence2D.notApplicable("invalid metric query"),
				MetricUnit2D.CONSTRUCTION_LENGTH_UNIT, provenance,
				Collections.emptyList(),
				Collections.singletonList(new MetricDiagnostic2D(code, message)));
	}

	private static final class CapabilityState {
		private final LocusMetricCapability2D capability;
		private final LocusMetricComponentState2D state;

		private CapabilityState(LocusMetricCapability2D capability,
				LocusMetricComponentState2D state) {
			this.capability = capability;
			this.state = state;
		}
	}
}
