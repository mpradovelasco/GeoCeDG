/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;

/** Rich immutable semantic metric result with orthogonal status axes. */
public final class LocusMetricResult2D {
	private final LocusMetricNumericTraversal2D numericTraversal;
	/** Evidence of an admitted route of one conditioned region, or {@code null}. */
	private final LocusMetricConditionedRoute2D conditionedRoute;
	private final MetricValue2D metricValue;
	private final MetricCoverage coverage;
	private final MetricComputationStatus computationStatus;
	private final MetricRectifiability rectifiability;
	private final Optional<TraversalOutcome> traversalOutcome;
	private final Optional<LocusMetricRoute2D> routeEvidence;
	private final ConstructionFidelity constructionFidelity;
	private final MetricEvaluatorMethod2D evaluatorMethod;
	private final MetricMethod2D metricMethod;
	private final MetricRepresentationRole2D representationRole;
	private final MetricErrorEvidence2D errorEvidence;
	private final MetricUnit2D unit;
	private final MetricProvenance2D provenance;
	private final List<LocusMetricContribution2D> contributions;
	private final List<MetricDiagnostic2D> diagnostics;

	/** Creates a deeply immutable rich metric result. */
	public LocusMetricResult2D(MetricValue2D metricValue,
			MetricCoverage coverage,
			MetricComputationStatus computationStatus,
			MetricRectifiability rectifiability,
			Optional<TraversalOutcome> traversalOutcome,
			ConstructionFidelity constructionFidelity,
			MetricEvaluatorMethod2D evaluatorMethod, MetricMethod2D metricMethod,
			MetricRepresentationRole2D representationRole,
			MetricErrorEvidence2D errorEvidence, MetricUnit2D unit,
			MetricProvenance2D provenance,
			List<LocusMetricContribution2D> contributions,
			List<MetricDiagnostic2D> diagnostics) {
		this(metricValue, coverage, computationStatus, rectifiability,
				traversalOutcome, Optional.empty(), constructionFidelity,
				evaluatorMethod, metricMethod, representationRole, errorEvidence,
				unit, provenance, contributions, diagnostics);
	}

	/** Creates a rich result retaining the complete resolved route evidence. */
	public LocusMetricResult2D(MetricValue2D metricValue,
			MetricCoverage coverage,
			MetricComputationStatus computationStatus,
			MetricRectifiability rectifiability,
			Optional<TraversalOutcome> traversalOutcome,
			Optional<LocusMetricRoute2D> routeEvidence,
			ConstructionFidelity constructionFidelity,
			MetricEvaluatorMethod2D evaluatorMethod, MetricMethod2D metricMethod,
			MetricRepresentationRole2D representationRole,
			MetricErrorEvidence2D errorEvidence, MetricUnit2D unit,
			MetricProvenance2D provenance,
			List<LocusMetricContribution2D> contributions,
			List<MetricDiagnostic2D> diagnostics) {
		this.numericTraversal = null;
		this.conditionedRoute = null;
		this.metricValue = Objects.requireNonNull(metricValue);
		this.coverage = Objects.requireNonNull(coverage);
		this.computationStatus = Objects.requireNonNull(computationStatus);
		this.rectifiability = Objects.requireNonNull(rectifiability);
		this.traversalOutcome = Objects.requireNonNull(traversalOutcome);
		this.routeEvidence = Objects.requireNonNull(routeEvidence);
		this.constructionFidelity =
				Objects.requireNonNull(constructionFidelity);
		this.evaluatorMethod = Objects.requireNonNull(evaluatorMethod);
		this.metricMethod = Objects.requireNonNull(metricMethod);
		this.representationRole = Objects.requireNonNull(representationRole);
		this.errorEvidence = Objects.requireNonNull(errorEvidence);
		this.unit = Objects.requireNonNull(unit);
		this.provenance = Objects.requireNonNull(provenance);
		this.contributions = immutableContributions(contributions);
		this.diagnostics = immutableDiagnostics(diagnostics);
	}

	public MetricValue2D getMetricValue() {
		return metricValue;
	}

	public MetricCoverage getCoverage() {
		return coverage;
	}

	public MetricComputationStatus getComputationStatus() {
		return computationStatus;
	}

	public MetricRectifiability getRectifiability() {
		return rectifiability;
	}

	public Optional<TraversalOutcome> getTraversalOutcome() {
		return traversalOutcome;
	}

	/**
	 * Returns the semantic route selected for a between-position query.
	 *
	 * @return route evidence, absent for total metrics or pre-route failures
	 */
	public Optional<LocusMetricRoute2D> getRouteEvidence() {
		return routeEvidence;
	}

	public ConstructionFidelity getConstructionFidelity() {
		return constructionFidelity;
	}

	public MetricEvaluatorMethod2D getEvaluatorMethod() {
		return evaluatorMethod;
	}

	public MetricMethod2D getMetricMethod() {
		return metricMethod;
	}

	public MetricRepresentationRole2D getRepresentationRole() {
		return representationRole;
	}

	public MetricErrorEvidence2D getErrorEvidence() {
		return errorEvidence;
	}

	public MetricUnit2D getUnit() {
		return unit;
	}

	public MetricProvenance2D getProvenance() {
		return provenance;
	}

	public List<LocusMetricContribution2D> getContributions() {
		return contributions;
	}

	public List<MetricDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}

	/**
	 * Scalar admissibility is stricter than rich-result defined state.
	 *
	 * @return whether the explicit scalar adapter may publish a finite value
	 */
	public boolean isScalarAdmissible() {
		if (metricValue.getKind() != MetricValueKind.FINITE
				|| (coverage != MetricCoverage.COMPLETE && !hasCompleteConditionalTraversal()
						&& !hasCurrentConditionedRoute())
				|| computationStatus != MetricComputationStatus.SUCCESS
				|| rectifiability != MetricRectifiability.RECTIFIABLE) {
			return false;
		}
		if (traversalOutcome.isPresent()
				&& traversalOutcome.get() != TraversalOutcome.TARGET_REACHED
				&& traversalOutcome.get()
						!= TraversalOutcome.WRAPPED_TO_START) {
			return false;
		}
		Optional<NumericGuarantee> guarantee =
				errorEvidence.getNumericGuarantee();
		if (guarantee.isEmpty()
				|| guarantee.get() == NumericGuarantee.FLOATING_POINT_UNCERTIFIED) {
			return false;
		}
		// PRE-G9B-CU1 (locus-v2-metrics.md section 25.4): under a policy that records its
		// requested tolerance, a certified value needs a certified error and an
		// estimate an estimated error not exceeding it. Without a recorded
		// tolerance the pre-CU1 rule applies.
		Optional<Double> tolerance = errorEvidence.getRequestedTolerance();
		if (tolerance.isEmpty()
				|| guarantee.get() == NumericGuarantee.EXACT_ARITHMETIC) {
			return true;
		}
		double amount = errorEvidence.getEstablishedAbsoluteAmount();
		return !Double.isNaN(amount) && amount <= tolerance.get();
	}

	/** Current entire numeric traversal, independently of geometric coverage. */
	public Optional<LocusMetricNumericTraversal2D> getNumericTraversal() {
		return Optional.ofNullable(numericTraversal);
	}
	public boolean isConditionedEstimate() {
		return numericTraversal != null || diagnostics.stream().anyMatch(d ->
				d.getCode() == MetricDiagnosticCode2D.EXISTENCE_NOT_CERTIFIED);
	}
	private boolean hasCompleteConditionalTraversal() {
		return numericTraversal != null
				&& numericTraversal.getNumericCoverage() == MetricCoverage.COMPLETE
				&& numericTraversal.getSemanticRevision() == provenance.getSemanticRevision()
				&& representationRole == MetricRepresentationRole2D.CONDITIONAL_NUMERIC_ESTIMATE
				&& errorEvidence.getNumericGuarantee().orElse(null) == NumericGuarantee.ESTIMATED_ERROR
				&& errorEvidence.getRequestedTolerance().isPresent()
				&& Double.isFinite(errorEvidence.getEstablishedAbsoluteAmount());
	}
	/**
	 * @return the evidence of a length admitted on a route of one conditioned region
	 *         ({@code locus-v2-metrics.md} section 28.3), independently of geometric coverage
	 */
	public Optional<LocusMetricConditionedRoute2D> getConditionedRoute() {
		return Optional.ofNullable(conditionedRoute);
	}

	/**
	 * Numeric coverage of the whole requested route, in one conditioned region, with an
	 * estimated error under a recorded tolerance, at the revision of this result. It is
	 * the only thing that the bypass of geometric completeness of section 28.3 reads; the
	 * tolerance itself is compared by {@link #isScalarAdmissible()}.
	 */
	private boolean hasCurrentConditionedRoute() {
		return conditionedRoute != null
				&& conditionedRoute.getSemanticRevision() == provenance.getSemanticRevision()
				&& !isConditionedSubtotal()
				&& errorEvidence.getNumericGuarantee().orElse(null)
						== NumericGuarantee.ESTIMATED_ERROR
				&& errorEvidence.getRequestedTolerance().isPresent()
				&& Double.isFinite(errorEvidence.getEstablishedAbsoluteAmount());
	}

	/** Package-only publication after the engine's guarded full-domain estimate. */
	LocusMetricResult2D withNumericTraversal(org.geocedg.common.kernel.locus.LocusBranch2D branch) {
		return new LocusMetricResult2D(this, branch);
	}
	private LocusMetricResult2D(LocusMetricResult2D source,
			org.geocedg.common.kernel.locus.LocusBranch2D branch) {
		numericTraversal = new LocusMetricNumericTraversal2D(branch, source.coverage,
				source.provenance.getSemanticRevision());
		conditionedRoute = null;
		metricValue = source.metricValue;
		coverage = MetricCoverage.INCOMPLETE;
		computationStatus = source.computationStatus;
		rectifiability = source.rectifiability;
		traversalOutcome = source.traversalOutcome;
		routeEvidence = source.routeEvidence;
		constructionFidelity = source.constructionFidelity;
		evaluatorMethod = source.evaluatorMethod;
		metricMethod = source.metricMethod;
		representationRole = MetricRepresentationRole2D.CONDITIONAL_NUMERIC_ESTIMATE;
		errorEvidence = source.errorEvidence;
		unit = source.unit;
		provenance = source.provenance;
		contributions = source.contributions;
		diagnostics = source.diagnostics;
	}

	/**
	 * Package-only publication of a length measured on conditioned regions (ADR 0042;
	 * contract {@code post-r1-conditioned-use.md}, sections 5.3, 8.3 and 8.4): the same
	 * value with incomplete geometric coverage and, for a subtotal, the diagnostic that
	 * lists the omitted bands. Geometric coverage is not complete where existence and
	 * continuity are hypotheses, so {@link #isScalarAdmissible()} refuses the value: the
	 * scalar admissions of sections 26.5 and 26.6 of the metric contract are unchanged, and
	 * the only conditioned length that a scalar publishes is the route of section 28.3
	 * ({@link #asConditionedRoute}).
	 */
	LocusMetricResult2D asConditioned(List<MetricDiagnostic2D> omitted) {
		return new LocusMetricResult2D(this, omitted, null);
	}

	/**
	 * Package-only publication of a length measured on a route that lies in one
	 * conditioned region and meets the admission of section 28.3 of the metric contract
	 * (decision DN-2): the same value, with incomplete geometric coverage, declared
	 * conditioned, with the evidence of the numeric coverage of its route. The scalar
	 * adapter may publish it; it certifies nothing.
	 */
	LocusMetricResult2D asConditionedRoute(LocusMetricConditionedRoute2D route) {
		return new LocusMetricResult2D(this, Collections.singletonList(
				new MetricDiagnostic2D(MetricDiagnosticCode2D.CONDITIONED_ROUTE_ADMITTED,
						"Length of a route of one conditioned region, admissible as a number: "
								+ "numeric coverage of the whole route, estimated error "
								+ "within the tolerance, hypotheses " + route.getHypotheses()
								+ " between the evaluated nodes; it proves no existence, "
								+ "continuity, completeness, incidence or intersection. "
								+ route)), Objects.requireNonNull(route));
	}

	private LocusMetricResult2D(LocusMetricResult2D source, List<MetricDiagnostic2D> omitted,
			LocusMetricConditionedRoute2D route) {
		numericTraversal = null;
		conditionedRoute = route;
		metricValue = source.metricValue;
		coverage = MetricCoverage.INCOMPLETE;
		computationStatus = source.computationStatus;
		rectifiability = source.rectifiability;
		traversalOutcome = source.traversalOutcome;
		routeEvidence = source.routeEvidence;
		constructionFidelity = source.constructionFidelity;
		evaluatorMethod = source.evaluatorMethod;
		metricMethod = source.metricMethod;
		representationRole = source.representationRole;
		errorEvidence = source.errorEvidence;
		unit = source.unit;
		provenance = source.provenance;
		contributions = source.contributions;
		ArrayList<MetricDiagnostic2D> all = new ArrayList<>(source.diagnostics);
		if (source.diagnostics.stream().noneMatch(d ->
				d.getCode() == MetricDiagnosticCode2D.EXISTENCE_NOT_CERTIFIED)) {
			all.add(new MetricDiagnostic2D(MetricDiagnosticCode2D.EXISTENCE_NOT_CERTIFIED,
					"Measured on conditioned regions: existence and continuity are assumed "
							+ "under the declared hypotheses, not certified"));
		}
		all.addAll(omitted);
		diagnostics = immutableDiagnostics(all);
	}

	/** @return whether the value is a subtotal that omits listed conditioned bands */
	public boolean isConditionedSubtotal() {
		return diagnostics.stream().anyMatch(d ->
				d.getCode() == MetricDiagnosticCode2D.CONDITIONED_SUBTOTAL);
	}

	/**
	 * @return guarantee class of this result ({@code locus-v2-metrics.md} section 25.1)
	 */
	public MetricGuaranteeClass2D getGuaranteeClass() {
		if (metricValue.getKind() != MetricValueKind.FINITE
				|| computationStatus != MetricComputationStatus.SUCCESS) {
			return MetricGuaranteeClass2D.NOT_ESTABLISHED;
		}
		Optional<NumericGuarantee> guarantee =
				errorEvidence.getNumericGuarantee();
		if (guarantee.isEmpty()) {
			return MetricGuaranteeClass2D.NOT_ESTABLISHED;
		}
		switch (guarantee.get()) {
		case EXACT_ARITHMETIC:
			return MetricGuaranteeClass2D.STRUCTURAL;
		case CERTIFIED_ERROR_BOUND:
			return metricMethod == MetricMethod2D.CLOSED_FORM
					? MetricGuaranteeClass2D.STRUCTURAL
					: MetricGuaranteeClass2D.CERTIFIED_ENCLOSURE;
		case ESTIMATED_ERROR:
			return MetricGuaranteeClass2D.ESTIMATED;
		case FLOATING_POINT_UNCERTIFIED:
		default:
			return MetricGuaranteeClass2D.UNCERTIFIED;
		}
	}

	private static List<LocusMetricContribution2D> immutableContributions(
			List<LocusMetricContribution2D> input) {
		Objects.requireNonNull(input);
		ArrayList<LocusMetricContribution2D> copy = new ArrayList<>();
		for (LocusMetricContribution2D contribution : input) {
			copy.add(Objects.requireNonNull(contribution));
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<MetricDiagnostic2D> immutableDiagnostics(
			List<MetricDiagnostic2D> input) {
		Objects.requireNonNull(input);
		ArrayList<MetricDiagnostic2D> copy = new ArrayList<>();
		for (MetricDiagnostic2D diagnostic : input) {
			copy.add(Objects.requireNonNull(diagnostic));
		}
		return Collections.unmodifiableList(copy);
	}
}
