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

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;

/** Ordered analytic, differential, evaluator-only, unsupported capability choice. */
public final class LocusMetricCapabilityHierarchy2D {
	private final List<LocusMetricCapability2D> capabilities;

	/** Creates an explicitly ordered immutable hierarchy. */
	public LocusMetricCapabilityHierarchy2D(
			List<LocusMetricCapability2D> capabilities) {
		Objects.requireNonNull(capabilities);
		ArrayList<LocusMetricCapability2D> copy = new ArrayList<>();
		int priorRank = -1;
		for (LocusMetricCapability2D capability : capabilities) {
			LocusMetricCapability2D checked = Objects.requireNonNull(capability);
			int rank = rank(checked.getEvaluatorMethod());
			if (rank < priorRank) {
				throw new IllegalArgumentException(
						"Metric capabilities must be ordered analytic to evaluator-only");
			}
			priorRank = rank;
			copy.add(checked);
		}
		this.capabilities = Collections.unmodifiableList(copy);
	}

	/**
	 * Selects the first explicitly supported capability, if any.
	 *
	 * @return selected capability or empty
	 */
	public Optional<LocusMetricCapability2D> select(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusMetricPolicy2D policy) {
		for (LocusMetricCapability2D capability : capabilities) {
			if (capability.supports(definition, branch, policy)) {
				return Optional.of(capability);
			}
		}
		return Optional.empty();
	}

	public List<LocusMetricCapability2D> getCapabilities() {
		return capabilities;
	}

	private static int rank(MetricEvaluatorMethod2D method) {
		switch (method) {
		case ANALYTIC:
			return 0;
		case DIFFERENTIAL:
			return 1;
		case POINT_EVALUATOR_ONLY:
			return 2;
		case NONE:
		default:
			return 3;
		}
	}
}
