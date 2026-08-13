/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable analytic total-variation evaluation for one subarc. */
public final class LocusAnalyticMetricEvaluation2D {
	private final MetricValue2D metricValue;
	private final MetricComputationStatus computationStatus;
	private final MetricRectifiability rectifiability;
	private final MetricErrorEvidence2D errorEvidence;
	private final List<MetricDiagnostic2D> diagnostics;

	/** Creates a complete analytic evaluation. */
	public LocusAnalyticMetricEvaluation2D(MetricValue2D metricValue,
			MetricComputationStatus computationStatus,
			MetricRectifiability rectifiability,
			MetricErrorEvidence2D errorEvidence,
			List<MetricDiagnostic2D> diagnostics) {
		this.metricValue = Objects.requireNonNull(metricValue);
		this.computationStatus = Objects.requireNonNull(computationStatus);
		this.rectifiability = Objects.requireNonNull(rectifiability);
		this.errorEvidence = Objects.requireNonNull(errorEvidence);
		ArrayList<MetricDiagnostic2D> copy = new ArrayList<>();
		for (MetricDiagnostic2D diagnostic : Objects.requireNonNull(diagnostics)) {
			copy.add(Objects.requireNonNull(diagnostic));
		}
		this.diagnostics = Collections.unmodifiableList(copy);
	}

	/**
	 * Creates an exact finite non-negative analytic value.
	 *
	 * @return exact analytic evaluation
	 */
	public static LocusAnalyticMetricEvaluation2D exactFinite(double value) {
		return new LocusAnalyticMetricEvaluation2D(
				new FiniteMetricValue2D(value),
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				MetricErrorEvidence2D.exact("analytic total variation"),
				Collections.emptyList());
	}

	/**
	 * Creates an established positive-infinite analytic value.
	 *
	 * @return positive-infinite analytic evaluation
	 */
	public static LocusAnalyticMetricEvaluation2D positiveInfinity(
			boolean nonRectifiable, String message) {
		return new LocusAnalyticMetricEvaluation2D(
				new PositiveInfinityMetricValue2D(),
				MetricComputationStatus.SUCCESS,
				nonRectifiable ? MetricRectifiability.NON_RECTIFIABLE
						: MetricRectifiability.UNDETERMINED,
				MetricErrorEvidence2D.notApplicable(
						"analytic positive-infinite variation"),
				Collections.singletonList(new MetricDiagnostic2D(
						nonRectifiable
								? MetricDiagnosticCode2D.NON_RECTIFIABLE
								: MetricDiagnosticCode2D.POSITIVE_INFINITY,
						message)));
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

	public MetricErrorEvidence2D getErrorEvidence() {
		return errorEvidence;
	}

	public List<MetricDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}
}
