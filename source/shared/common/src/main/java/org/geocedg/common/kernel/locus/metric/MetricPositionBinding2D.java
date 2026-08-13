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

import org.geocedg.common.kernel.locus.LocusPoint2D;

/** Revision-bound evaluation of a durable semantic position. */
public final class MetricPositionBinding2D {
	private final LocusSemanticPosition2D semanticPosition;
	private final long semanticRevision;
	private final Optional<String> resolvedValidComponentKey;
	private final MetricPositionEvaluationStatus evaluationStatus;
	private final Optional<LocusPoint2D> evaluatedPoint;
	private final List<MetricDiagnostic2D> diagnostics;

	/** Creates a structurally valid position binding. */
	public MetricPositionBinding2D(LocusSemanticPosition2D semanticPosition,
			long semanticRevision, Optional<String> resolvedValidComponentKey,
			MetricPositionEvaluationStatus evaluationStatus,
			Optional<LocusPoint2D> evaluatedPoint,
			List<MetricDiagnostic2D> diagnostics) {
		if (semanticRevision < 1) {
			throw new IllegalArgumentException("Binding revision must be positive");
		}
		this.semanticPosition = Objects.requireNonNull(semanticPosition);
		this.semanticRevision = semanticRevision;
		this.resolvedValidComponentKey =
				Objects.requireNonNull(resolvedValidComponentKey);
		this.evaluationStatus = Objects.requireNonNull(evaluationStatus);
		this.evaluatedPoint = Objects.requireNonNull(evaluatedPoint);
		this.diagnostics = immutableDiagnostics(diagnostics);
		boolean valid = evaluationStatus == MetricPositionEvaluationStatus.VALID;
		if (valid != resolvedValidComponentKey.isPresent()
				|| valid != evaluatedPoint.isPresent()) {
			throw new IllegalArgumentException(
					"Only a valid binding carries a component and evaluated point");
		}
	}

	public LocusSemanticPosition2D getSemanticPosition() {
		return semanticPosition;
	}

	public long getSemanticRevision() {
		return semanticRevision;
	}

	public Optional<String> getResolvedValidComponentKey() {
		return resolvedValidComponentKey;
	}

	public MetricPositionEvaluationStatus getEvaluationStatus() {
		return evaluationStatus;
	}

	public Optional<LocusPoint2D> getEvaluatedPoint() {
		return evaluatedPoint;
	}

	public List<MetricDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}

	public boolean isValid() {
		return evaluationStatus == MetricPositionEvaluationStatus.VALID;
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
