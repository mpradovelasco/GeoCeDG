/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Collections;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/** Explicit binder; no Cartesian proximity repair exists. */
public final class LocusMetricPositionBinder2D {

	/**
	 * Resolves a durable position in exactly one semantic revision.
	 *
	 * @return immutable revision binding
	 */
	public MetricPositionBinding2D bind(LocusSemanticPosition2D position,
			LocusDefinition2D definition) {
		if (!position.getLocusIdentity().equals(definition.getLocusIdentity())) {
			return invalid(position, definition.getSemanticRevision(),
					MetricPositionEvaluationStatus.BRANCH_MISSING,
					MetricDiagnosticCode2D.BRANCH_MISSING,
					"Position locus identity does not match the definition");
		}
		if (!position.getProviderVersion().equals(
				definition.getProvider().getProviderId())) {
			return invalid(position, definition.getSemanticRevision(),
					MetricPositionEvaluationStatus.PROVIDER_VERSION_MISMATCH,
					MetricDiagnosticCode2D.PROVIDER_VERSION_MISMATCH,
					"Provider version changed; explicit semantic migration is required");
		}
		LocusBranch2D branch = definition.getBranch(position.getBranchKey());
		if (branch == null) {
			return invalid(position, definition.getSemanticRevision(),
					MetricPositionEvaluationStatus.BRANCH_MISSING,
					MetricDiagnosticCode2D.BRANCH_MISSING,
					"Constructive branch is absent in this revision");
		}
		double parameter = position.getProviderCanonicalParameter();
		for (int index = 0; index < branch.getValidDomainComponents().size();
				index++) {
			LocusInterval2D component =
					branch.getValidDomainComponents().get(index);
			if (component.contains(parameter,
					definition.getProvider().getDomainEpsilon())) {
				if (position.getComponentLineageKey() != null
						&& !position.getComponentLineageKey().equals(
								LocusComponentLineage2D.create(
										branch.getBranchKey(), component))) {
					continue;
				}
				try (LocusEvaluationSession2D session =
						LocusEvaluationSession2D.reference()) {
					LocusEvaluation2D evaluation = definition.evaluate(
							branch.getBranchKey(), parameter, session);
					if (evaluation.isValid()) {
						return new MetricPositionBinding2D(position,
								definition.getSemanticRevision(),
								Optional.of(LocusMetricComponentKey2D.create(
										definition, branch, index)),
								MetricPositionEvaluationStatus.VALID,
								Optional.of(evaluation.getPoint()),
								Collections.emptyList());
					}
					return invalid(position, definition.getSemanticRevision(),
							MetricPositionEvaluationStatus.EVALUATION_FAILED,
							MetricDiagnosticCode2D.EVALUATION_FAILED,
							evaluation.getDiagnostic());
				}
			}
		}
		return invalid(position, definition.getSemanticRevision(),
				MetricPositionEvaluationStatus.POSITION_OUTSIDE_DOMAIN,
				MetricDiagnosticCode2D.POSITION_OUTSIDE_DOMAIN,
				"Canonical parameter is outside every valid component");
	}

	/**
	 * Marks a binding stale without re-evaluating or repairing it.
	 *
	 * @return original binding or explicit stale binding
	 */
	public MetricPositionBinding2D requireRevision(
			MetricPositionBinding2D binding, long currentRevision) {
		if (binding.getSemanticRevision() == currentRevision) {
			return binding;
		}
		return invalid(binding.getSemanticPosition(), binding.getSemanticRevision(),
				MetricPositionEvaluationStatus.POSITION_STALE,
				MetricDiagnosticCode2D.POSITION_STALE,
				"Explicit rebind is required for the current semantic revision");
	}

	private static MetricPositionBinding2D invalid(
			LocusSemanticPosition2D position, long revision,
			MetricPositionEvaluationStatus status, MetricDiagnosticCode2D code,
			String message) {
		return new MetricPositionBinding2D(position, revision, Optional.empty(),
				status, Optional.empty(), Collections.singletonList(
						new MetricDiagnostic2D(code, message)));
	}
}
