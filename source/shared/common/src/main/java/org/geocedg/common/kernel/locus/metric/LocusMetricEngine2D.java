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
import org.geocedg.common.kernel.locus.LocusDefinition2D;
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
		List<LocusMetricContribution2D> contributions = new ArrayList<>();
		for (LocusBranch2D branch : definition.getBranches()) {
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
		LocusMetricCapability2D capability = hierarchy.select(definition, branch,
				policy).orElseGet(() ->
						new UnsupportedLocusMetricCapability2D(
								"unsupported/v1"));
		String capabilityVersion = definition.getProvider().getProviderId()
				+ "|" + capability.getCapabilityVersion();
		LocusMetricIndexKey2D key = new LocusMetricIndexKey2D(
				definition.getLocusIdentity(),
				definition.getSemanticRevision(), branch.getBranchKey(),
				componentKey, capabilityVersion, policy);
		LocusMetricComponentStateBuilder2D builder = ignored ->
				capability.buildComponentState(definition, branch, component,
						key, policy, instrumentation);
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
		return new CapabilityState(capability, componentState);
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
