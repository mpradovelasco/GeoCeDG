/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Typed private-build failure; the index never publishes a partial entry. */
public final class LocusMetricComponentBuildException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final MetricComputationStatus computationStatus;
	private final List<MetricDiagnostic2D> diagnostics;

	/** Creates a handled component build failure. */
	public LocusMetricComponentBuildException(
			MetricComputationStatus computationStatus,
			List<MetricDiagnostic2D> diagnostics) {
		super(diagnostics == null || diagnostics.isEmpty()
				? computationStatus.toString() : diagnostics.get(0).toString());
		if (computationStatus == MetricComputationStatus.SUCCESS) {
			throw new IllegalArgumentException("A build failure cannot be SUCCESS");
		}
		this.computationStatus = computationStatus;
		ArrayList<MetricDiagnostic2D> copy = new ArrayList<>();
		if (diagnostics != null) {
			copy.addAll(diagnostics);
		}
		this.diagnostics = Collections.unmodifiableList(copy);
	}

	public MetricComputationStatus getComputationStatus() {
		return computationStatus;
	}

	public List<MetricDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}
}
