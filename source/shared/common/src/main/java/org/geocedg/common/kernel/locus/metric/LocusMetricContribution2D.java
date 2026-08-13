/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;

/** Immutable route- or extent-specific contribution from one valid component. */
public final class LocusMetricContribution2D {
	private final String branchKey;
	private final String resolvedValidComponentKey;
	private final MetricValue2D metricValue;
	private final MetricComputationStatus computationStatus;
	private final MetricRectifiability rectifiability;
	private final ConstructionFidelity constructionFidelity;
	private final MetricEvaluatorMethod2D evaluatorMethod;
	private final MetricMethod2D metricMethod;
	private final MetricErrorEvidence2D errorEvidence;
	private final MetricProvenance2D provenance;
	private final List<MetricDiagnostic2D> diagnostics;

	/** Creates a complete immutable contribution. */
	public LocusMetricContribution2D(String branchKey,
			String resolvedValidComponentKey, MetricValue2D metricValue,
			MetricComputationStatus computationStatus,
			MetricRectifiability rectifiability,
			ConstructionFidelity constructionFidelity,
			MetricEvaluatorMethod2D evaluatorMethod, MetricMethod2D metricMethod,
			MetricErrorEvidence2D errorEvidence,
			MetricProvenance2D provenance,
			List<MetricDiagnostic2D> diagnostics) {
		if (branchKey == null || branchKey.trim().isEmpty()
				|| resolvedValidComponentKey == null
				|| resolvedValidComponentKey.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Contribution branch and component are required");
		}
		this.branchKey = branchKey;
		this.resolvedValidComponentKey = resolvedValidComponentKey;
		this.metricValue = Objects.requireNonNull(metricValue);
		this.computationStatus = Objects.requireNonNull(computationStatus);
		this.rectifiability = Objects.requireNonNull(rectifiability);
		this.constructionFidelity = Objects.requireNonNull(constructionFidelity);
		this.evaluatorMethod = Objects.requireNonNull(evaluatorMethod);
		this.metricMethod = Objects.requireNonNull(metricMethod);
		this.errorEvidence = Objects.requireNonNull(errorEvidence);
		this.provenance = Objects.requireNonNull(provenance);
		this.diagnostics = immutableDiagnostics(diagnostics);
		if (computationStatus != MetricComputationStatus.SUCCESS
				&& metricValue.getKind() == MetricValueKind.FINITE
				&& metricValue.getFiniteValue().orElseThrow() == 0
				&& diagnostics.isEmpty()) {
			throw new IllegalArgumentException(
					"A failed zero contribution requires an explicit diagnostic");
		}
	}

	public String getBranchKey() {
		return branchKey;
	}

	public String getResolvedValidComponentKey() {
		return resolvedValidComponentKey;
	}

	public MetricValue2D getMetricValue() {
		return metricValue;
	}

	public MetricComputationStatus getComputationStatus() {
		return computationStatus;
	}

	public MetricRectifiability getRectifiability() {
		return rectifiability;
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

	public MetricErrorEvidence2D getErrorEvidence() {
		return errorEvidence;
	}

	public MetricProvenance2D getProvenance() {
		return provenance;
	}

	public List<MetricDiagnostic2D> getDiagnostics() {
		return diagnostics;
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
