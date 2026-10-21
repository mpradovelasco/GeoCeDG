/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentSelectorResolver2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;

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
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		List<ContinuousComponent> components = structure.getContinuousValidComponents();
		double epsilon = definition.getProvider().getDomainEpsilon();
		ContinuousComponent component = null;
		if (position.getComponentLineageKey() != null) {
			// A retained component selector resolves only through a demonstrated
			// structural correspondence (locus-v2-existence-components.md section 15.6).
			LocusComponentSelectorResolver2D.Resolution resolution =
					LocusComponentSelectorResolver2D.resolve(
							position.getComponentLineageKey(), structure,
							Collections.singletonList(branch.getLineage()), parameter,
							epsilon);
			component = resolution.getStatus().isResolved() ? resolution.getComponent()
					: null;
		} else {
			for (ContinuousComponent candidate : components) {
				if (candidate.contains(parameter, epsilon)) {
					component = candidate;
					break;
				}
			}
		}
		if (component == null) {
			return structure.getPointComponentEnclosing(parameter) != null
					? invalid(position, definition.getSemanticRevision(),
							MetricPositionEvaluationStatus.POSITION_OUTSIDE_DOMAIN,
							MetricDiagnosticCode2D.ISOLATED_POINT,
							"A point component is never addressed by a binary64 parameter")
					: invalid(position, definition.getSemanticRevision(),
							MetricPositionEvaluationStatus.POSITION_OUTSIDE_DOMAIN,
							MetricDiagnosticCode2D.POSITION_OUTSIDE_DOMAIN,
							"Canonical parameter is outside every valid component");
		}
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.openServiceSession(false, 1)) {
			LocusEvaluation2D evaluation = definition.evaluate(
					branch.getBranchKey(), parameter, session);
			if (evaluation.isValid()) {
				return new MetricPositionBinding2D(position,
						definition.getSemanticRevision(),
						Optional.of(LocusMetricComponentKey2D.create(
								definition, branch, components.indexOf(component))),
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
