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
	private final MetricValue2D metricValue;
	private final MetricCoverage coverage;
	private final MetricComputationStatus computationStatus;
	private final MetricRectifiability rectifiability;
	private final Optional<TraversalOutcome> traversalOutcome;
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
		this.metricValue = Objects.requireNonNull(metricValue);
		this.coverage = Objects.requireNonNull(coverage);
		this.computationStatus = Objects.requireNonNull(computationStatus);
		this.rectifiability = Objects.requireNonNull(rectifiability);
		this.traversalOutcome = Objects.requireNonNull(traversalOutcome);
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
				|| coverage != MetricCoverage.COMPLETE
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
		return guarantee.isPresent()
				&& guarantee.get()
						!= NumericGuarantee.FLOATING_POINT_UNCERTIFIED;
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
