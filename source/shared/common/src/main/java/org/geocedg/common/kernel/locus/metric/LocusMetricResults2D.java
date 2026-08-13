/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;

/** Coherent rich-result factories for lifecycle and P1 failure publication. */
public final class LocusMetricResults2D {
	private LocusMetricResults2D() {
		// Utility class.
	}

	/**
	 * Creates one current-revision absent rich failure snapshot.
	 *
	 * @return coherent P1 rich failure result
	 */
	public static LocusMetricResult2D failure(String locusIdentity,
			long semanticRevision, LocusMetricPolicy2D policy,
			MetricComputationStatus status,
			Optional<TraversalOutcome> traversalOutcome,
			List<MetricDiagnostic2D> diagnostics) {
		if (status == MetricComputationStatus.SUCCESS) {
			throw new IllegalArgumentException("Failure status cannot be SUCCESS");
		}
		ArrayList<MetricDiagnostic2D> copy =
				new ArrayList<>(diagnostics == null
						? Collections.emptyList() : diagnostics);
		if (copy.isEmpty()) {
			copy.add(new MetricDiagnostic2D(MetricDiagnosticCode2D.P1_FAILURE,
					"Current-revision metric computation failed coherently"));
		}
		MetricProvenance2D provenance = new MetricProvenance2D(locusIdentity,
				semanticRevision, "p1-failure/v1",
				policy.getMetricAlgorithmVersion(),
				policy.getMetricPolicyVersion());
		return new LocusMetricResult2D(new AbsentMetricValue2D(),
				MetricCoverage.INCOMPLETE, status,
				MetricRectifiability.UNDETERMINED, traversalOutcome,
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				MetricEvaluatorMethod2D.NONE, MetricMethod2D.NONE,
				MetricRepresentationRole2D.DIAGNOSTIC_PARTIAL_VALUE,
				MetricErrorEvidence2D.notApplicable(
						"current-revision P1 failure"),
				MetricUnit2D.CONSTRUCTION_LENGTH_UNIT, provenance,
				Collections.emptyList(), copy);
	}
}
