/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;

/**
 * Aggregates immutable contributions without integrating a valid component.
 */
public final class LocusMetricAggregator2D {

	/**
	 * Aggregates a total query in stable constructive branch/component order.
	 *
	 * @return rich total metric result
	 */
	public LocusMetricResult2D aggregateTotal(TotalLocusMetricQuery query,
			List<LocusMetricContribution2D> contributions) {
		List<LocusMetricContribution2D> ordered = new ArrayList<>(contributions);
		ordered.sort(Comparator.comparing(
				LocusMetricContribution2D::getBranchKey).thenComparing(
						LocusMetricContribution2D
								::getResolvedValidComponentKey));
		return aggregate(query, ordered, Optional.empty(), false,
				Collections.emptyList());
	}

	/**
	 * Aggregates a between-position route in route-segment order.
	 *
	 * @return rich between-position metric result
	 */
	public LocusMetricResult2D aggregateBetween(
			BetweenPositionsMetricQuery query, LocusMetricRoute2D route,
			List<LocusMetricContribution2D> contributions) {
		if (route.getRouteStatus() != MetricRouteStatus.RESOLVED) {
			return routeFailure(query, route);
		}
		boolean partial =
				route.getTraversalOutcome() == TraversalOutcome.STOPPED_AT_BOUNDARY;
		return aggregate(query, new ArrayList<>(contributions),
				Optional.of(route.getTraversalOutcome()), partial,
				route.getDiagnostics());
	}

	private LocusMetricResult2D aggregate(LocusMetricQuery2D query,
			List<LocusMetricContribution2D> ordered,
			Optional<TraversalOutcome> traversalOutcome, boolean partial,
			List<MetricDiagnostic2D> routeDiagnostics) {
		boolean hasInfinity = false;
		boolean hasFinite = false;
		boolean unresolved = partial;
		double sum = 0;
		double compensation = 0;
		MetricComputationStatus status = MetricComputationStatus.SUCCESS;
		MetricRectifiability rectifiability =
				MetricRectifiability.RECTIFIABLE;
		ConstructionFidelity fidelity =
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED;
		MetricEvaluatorMethod2D evaluatorMethod =
				MetricEvaluatorMethod2D.NONE;
		MetricMethod2D metricMethod = MetricMethod2D.NONE;
		List<MetricDiagnostic2D> diagnostics =
				new ArrayList<>(routeDiagnostics);
		for (LocusMetricContribution2D contribution : ordered) {
			MetricValueKind kind = contribution.getMetricValue().getKind();
			if (kind == MetricValueKind.POSITIVE_INFINITY) {
				hasInfinity = true;
			} else if (kind == MetricValueKind.FINITE) {
				hasFinite = true;
				double value = contribution.getMetricValue().getFiniteValue()
						.orElseThrow();
				double corrected = value - compensation;
				double next = sum + corrected;
				compensation = (next - sum) - corrected;
				sum = next;
			} else {
				unresolved = true;
			}
			if (contribution.getComputationStatus()
					!= MetricComputationStatus.SUCCESS) {
				unresolved = true;
			}
			status = strongestStatus(status,
					contribution.getComputationStatus());
			rectifiability = weakestRectifiability(rectifiability,
					contribution.getRectifiability());
			if (contribution.getConstructionFidelity()
					== ConstructionFidelity.EXPLICIT_APPROXIMATION) {
				fidelity = ConstructionFidelity.EXPLICIT_APPROXIMATION;
			}
			evaluatorMethod = combineEvaluator(evaluatorMethod,
					contribution.getEvaluatorMethod());
			metricMethod = combineMethod(metricMethod,
					contribution.getMetricMethod());
			diagnostics.addAll(contribution.getDiagnostics());
		}
		if (ordered.isEmpty()) {
			hasFinite = true;
		}
		MetricValue2D value = hasInfinity
				? new PositiveInfinityMetricValue2D()
				: hasFinite ? new FiniteMetricValue2D(sum)
						: new AbsentMetricValue2D();
		MetricCoverage coverage = unresolved ? MetricCoverage.INCOMPLETE
				: MetricCoverage.COMPLETE;
		MetricErrorEvidence2D error = aggregateError(value, ordered, coverage);
		if (unresolved && diagnostics.stream().noneMatch(diagnostic ->
				diagnostic.getCode()
						== MetricDiagnosticCode2D.INCOMPLETE_AGGREGATE)) {
			diagnostics.add(new MetricDiagnostic2D(
					MetricDiagnosticCode2D.INCOMPLETE_AGGREGATE,
					"At least one semantically required contribution is unresolved"));
		}
		MetricRepresentationRole2D role = coverage == MetricCoverage.COMPLETE
				? MetricRepresentationRole2D.SEMANTIC_METRIC
				: MetricRepresentationRole2D.DIAGNOSTIC_PARTIAL_VALUE;
		String capability = ordered.isEmpty() ? "empty-total/v1"
				: ordered.get(0).getProvenance().getCapabilityVersion();
		MetricProvenance2D provenance = new MetricProvenance2D(
				query.getLocusIdentity(), query.getSemanticRevision(), capability,
				query.getPolicy().getMetricAlgorithmVersion(),
				query.getPolicy().getMetricPolicyVersion());
		return new LocusMetricResult2D(value, coverage, status, rectifiability,
				traversalOutcome, fidelity, evaluatorMethod, metricMethod, role,
				error, MetricUnit2D.CONSTRUCTION_LENGTH_UNIT, provenance, ordered,
				diagnostics);
	}

	private static LocusMetricResult2D routeFailure(
			BetweenPositionsMetricQuery query, LocusMetricRoute2D route) {
		MetricProvenance2D provenance = new MetricProvenance2D(
				query.getLocusIdentity(), query.getSemanticRevision(),
				"route-resolution/v1",
				query.getPolicy().getMetricAlgorithmVersion(),
				query.getPolicy().getMetricPolicyVersion());
		return new LocusMetricResult2D(new AbsentMetricValue2D(),
				MetricCoverage.INCOMPLETE,
				MetricComputationStatus.INVALID_QUERY,
				MetricRectifiability.UNDETERMINED,
				Optional.of(route.getTraversalOutcome()),
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				MetricEvaluatorMethod2D.NONE, MetricMethod2D.NONE,
				MetricRepresentationRole2D.DIAGNOSTIC_PARTIAL_VALUE,
				MetricErrorEvidence2D.notApplicable("route resolution"),
				MetricUnit2D.CONSTRUCTION_LENGTH_UNIT, provenance,
				Collections.emptyList(), route.getDiagnostics());
	}

	private static MetricErrorEvidence2D aggregateError(MetricValue2D value,
			List<LocusMetricContribution2D> contributions,
			MetricCoverage coverage) {
		if (value.getKind() != MetricValueKind.FINITE) {
			return MetricErrorEvidence2D.notApplicable(
					"aggregate has no finite error amount");
		}
		if (contributions.isEmpty()) {
			return MetricErrorEvidence2D.exact("empty/zero aggregate");
		}
		NumericGuarantee weakest = NumericGuarantee.EXACT_ARITHMETIC;
		double absolute = 0;
		boolean amountsEstablished = true;
		List<String> assumptions = new ArrayList<>();
		for (LocusMetricContribution2D contribution : contributions) {
			MetricErrorEvidence2D evidence = contribution.getErrorEvidence();
			if (evidence.getNumericGuarantee().isEmpty()) {
				return MetricErrorEvidence2D.notApplicable(
						"unresolved aggregate error");
			}
			weakest = weakest(weakest,
					evidence.getNumericGuarantee().orElseThrow());
			if (evidence.getAbsoluteEvidence()
					instanceof EstablishedMetricErrorAmount2D) {
				absolute += ((EstablishedMetricErrorAmount2D)
						evidence.getAbsoluteEvidence())
								.getNonNegativeFiniteAmount();
			} else {
				amountsEstablished = false;
			}
			assumptions.addAll(evidence.getAssumptions());
		}
		MetricErrorEvidenceScope scope =
				coverage == MetricCoverage.COMPLETE
						? MetricErrorEvidenceScope.COMPLETE_VALUE
						: MetricErrorEvidenceScope.REPORTED_PARTIAL_VALUE;
		if (!amountsEstablished) {
			return new MetricErrorEvidence2D(Optional.of(weakest),
					new NotEstablishedMetricErrorAmount2D(),
					new NotEstablishedMetricErrorAmount2D(), scope,
					"aggregate of component error evidence", assumptions,
					Optional.empty());
		}
		double finiteValue = value.getFiniteValue().orElseThrow();
		double relative = finiteValue == 0 ? 0 : absolute / finiteValue;
		if (weakest == NumericGuarantee.ESTIMATED_ERROR
				&& assumptions.isEmpty()) {
			assumptions.add("component estimates use their declared assumptions");
		}
		return MetricErrorEvidence2D.established(weakest, absolute, relative,
				"stable sum of component error evidence", assumptions,
				Optional.empty(), scope);
	}

	private static NumericGuarantee weakest(NumericGuarantee left,
			NumericGuarantee right) {
		return guaranteeRank(left) >= guaranteeRank(right) ? left : right;
	}

	private static int guaranteeRank(NumericGuarantee guarantee) {
		switch (guarantee) {
		case EXACT_ARITHMETIC:
			return 0;
		case CERTIFIED_ERROR_BOUND:
			return 1;
		case ESTIMATED_ERROR:
			return 2;
		case FLOATING_POINT_UNCERTIFIED:
		default:
			return 3;
		}
	}

	private static MetricComputationStatus strongestStatus(
			MetricComputationStatus left, MetricComputationStatus right) {
		return statusRank(left) >= statusRank(right) ? left : right;
	}

	private static int statusRank(MetricComputationStatus status) {
		switch (status) {
		case INVALID_QUERY:
			return 4;
		case NUMERICAL_FAILURE:
			return 3;
		case LIMIT_NOT_ESTABLISHED:
			return 2;
		case UNSUPPORTED:
			return 1;
		case SUCCESS:
		default:
			return 0;
		}
	}

	private static MetricRectifiability weakestRectifiability(
			MetricRectifiability left, MetricRectifiability right) {
		if (left == MetricRectifiability.NON_RECTIFIABLE
				|| right == MetricRectifiability.NON_RECTIFIABLE) {
			return MetricRectifiability.NON_RECTIFIABLE;
		}
		if (left == MetricRectifiability.UNDETERMINED
				|| right == MetricRectifiability.UNDETERMINED) {
			return MetricRectifiability.UNDETERMINED;
		}
		return MetricRectifiability.RECTIFIABLE;
	}

	private static MetricEvaluatorMethod2D combineEvaluator(
			MetricEvaluatorMethod2D current,
			MetricEvaluatorMethod2D candidate) {
		if (current == MetricEvaluatorMethod2D.NONE) {
			return candidate;
		}
		return current == candidate ? current
				: MetricEvaluatorMethod2D.POINT_EVALUATOR_ONLY;
	}

	private static MetricMethod2D combineMethod(MetricMethod2D current,
			MetricMethod2D candidate) {
		if (current == MetricMethod2D.NONE) {
			return candidate;
		}
		return current == candidate ? current : MetricMethod2D.NONE;
	}
}
