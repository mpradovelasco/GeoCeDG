/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedCell2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidenceOwner2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSample2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricComponentKey2D;
import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionEvaluationStatus;

/**
 * Point validity of a parameter address (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, sections 6.3 and 8.1).
 *
 * <p>A parameter address resolves, and its point is defined, when the evaluation at its
 * parameter is valid and every selecting dependency has an established selection at that
 * parameter. Nothing else decides it: no interval evidence is read, and the kind of the
 * cell that contains the parameter does not enter. An undefined point has a typed cause of
 * its own and never becomes a neighbouring point. The evaluation is the producer's own,
 * with its testimony; a producer that gives no testimony has no selecting dependency.</p>
 *
 * <p>An evaluation that the kind of its published cell excludes is reported to the
 * producer, which decides by its own evaluation (contract section 7, point 4). The report
 * never changes the result of this resolution.</p>
 */
public final class LocusParameterPointResolver2D {
	/** Result of one resolution. */
	public static final class Resolution {
		private final LocusSemanticPointCause2D cause;
		private final MetricPositionBinding2D binding;
		private final boolean contradictionConfirmed;

		private Resolution(LocusSemanticPointCause2D cause, MetricPositionBinding2D binding,
				boolean contradictionConfirmed) {
			this.cause = cause;
			this.binding = binding;
			this.contradictionConfirmed = contradictionConfirmed;
		}

		/** @return whether the point is defined */
		public boolean isDefined() {
			return cause == LocusSemanticPointCause2D.RESOLVED;
		}

		/** @return typed cause; {@code RESOLVED} while the point is defined */
		public LocusSemanticPointCause2D getCause() {
			return cause;
		}

		/** @return binding of the revision, valid or with its explicit status */
		public MetricPositionBinding2D getBinding() {
			return binding;
		}

		/**
		 * @return whether the producer confirmed a contradiction of its published evidence
		 *         at this parameter and published a new content
		 */
		public boolean isContradictionConfirmed() {
			return contradictionConfirmed;
		}
	}

	private LocusParameterPointResolver2D() {
		// Utility class.
	}

	/**
	 * @param source locus of the address
	 * @param definition current definition of the source
	 * @param branch branch of the address in that definition
	 * @param address parameter address
	 * @return point validity at the parameter of the address
	 */
	public static Resolution resolve(GeoLocusV2 source, LocusDefinition2D definition,
			LocusBranch2D branch, LocusSemanticAddress2D address) {
		Objects.requireNonNull(source);
		Objects.requireNonNull(definition);
		Objects.requireNonNull(branch);
		if (!address.isParameterAddress()) {
			throw new IllegalArgumentException("A parameter address is required");
		}
		LocusSemanticPosition2D position = address.toMetricPosition();
		long revision = definition.getSemanticRevision();
		double parameter = address.getCanonicalParameter();
		LocusInterval2D domain = branch.getDeclaredDriverDomain();
		if (!domain.contains(parameter, 0)) {
			return new Resolution(LocusSemanticPointCause2D.PARAMETER_OUTSIDE_DOMAIN,
					invalid(position, revision,
							MetricPositionEvaluationStatus.POSITION_OUTSIDE_DOMAIN,
							MetricDiagnosticCode2D.POSITION_OUTSIDE_DOMAIN,
							"Parameter is outside the declared driver domain"), false);
		}
		LocusExistenceStructure2D structure = branch.getExistenceStructure();
		if (structure.getPointComponentEnclosing(parameter) != null) {
			return new Resolution(LocusSemanticPointCause2D.POINT_COMPONENT,
					invalid(position, revision,
							MetricPositionEvaluationStatus.POSITION_OUTSIDE_DOMAIN,
							MetricDiagnosticCode2D.ISOLATED_POINT,
							"A point component is never addressed by a binary64 parameter"),
					false);
		}
		LocusConditionedSample2D sample = source.sampleConditioned(branch.getBranchKey(),
				parameter);
		if (sample == null) {
			sample = sampleWithoutTestimony(definition, branch, parameter);
		}
		boolean confirmed = reportContradiction(source, branch.getBranchKey(), parameter,
				sample);
		if (!sample.isValid()) {
			return new Resolution(LocusSemanticPointCause2D.EVALUATION_FAILED,
					invalid(position, revision,
							MetricPositionEvaluationStatus.EVALUATION_FAILED,
							MetricDiagnosticCode2D.EVALUATION_FAILED, sample.getCause()),
					confirmed);
		}
		if (!sample.hasEstablishedSelections()) {
			return new Resolution(LocusSemanticPointCause2D.SELECTION_NOT_ESTABLISHED,
					invalid(position, revision,
							MetricPositionEvaluationStatus.EVALUATION_FAILED,
							MetricDiagnosticCode2D.EVALUATION_FAILED,
							"A selecting dependency has no established selection"),
					confirmed);
		}
		// The key names the declared domain: it is not the key of a component, so no route
		// of a certified component ever resolves through it.
		return new Resolution(LocusSemanticPointCause2D.RESOLVED,
				new MetricPositionBinding2D(position, revision,
						Optional.of(LocusMetricComponentKey2D.createDeclared(definition, branch,
								0, domain)),
						MetricPositionEvaluationStatus.VALID,
						Optional.of(new LocusPoint2D(sample.getX(), sample.getY())),
						Collections.emptyList()),
				confirmed);
	}

	private static LocusConditionedSample2D sampleWithoutTestimony(
			LocusDefinition2D definition, LocusBranch2D branch, double parameter) {
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.openServiceSession(false, 1)) {
			LocusEvaluation2D evaluation = definition.evaluateCanonicalParameter(
					branch.getBranchKey(), parameter, session);
			if (evaluation.isValid()) {
				return LocusConditionedSample2D.valid(evaluation.getPoint().getX(),
						evaluation.getPoint().getY(), List.of());
			}
			String diagnostic = evaluation.getDiagnostic();
			return LocusConditionedSample2D.failed(evaluation.getStatus().name()
					+ (diagnostic == null || diagnostic.trim().isEmpty() ? ""
							: ": " + diagnostic.trim()));
		}
	}

	/**
	 * Reports the evaluation to the producer when the kind of a cell that the producer has
	 * already published excludes it. The evidence is never computed for this.
	 */
	private static boolean reportContradiction(GeoLocusV2 source, String branchKey,
			double parameter, LocusConditionedSample2D sample) {
		LocusConditionedEvidence2D published = source.getPublishedConditionedEvidence(
				branchKey);
		if (published == null || parameter < published.getDomainLower()
				|| parameter > published.getDomainUpper()) {
			return false;
		}
		boolean excluded = false;
		for (LocusConditionedCell2D cell : published.cellsContaining(parameter)) {
			switch (cell.getKind()) {
			case SPAN:
				excluded |= !sample.isValid() || !sample.hasEstablishedSelections();
				break;
			case ABSENT_BAND:
			case FAILURE_BAND:
				excluded |= sample.isValid();
				break;
			default:
				break;
			}
		}
		return excluded && source.reportConditionedContradiction(branchKey, parameter)
				== LocusConditionedEvidenceOwner2D.ReportOutcome.CONFIRMED;
	}

	private static MetricPositionBinding2D invalid(LocusSemanticPosition2D position,
			long revision, MetricPositionEvaluationStatus status,
			MetricDiagnosticCode2D code, String message) {
		return new MetricPositionBinding2D(position, revision, Optional.empty(), status,
				Optional.empty(), Collections.singletonList(new MetricDiagnostic2D(code,
						message == null || message.isEmpty() ? status.name() : message)));
	}
}
