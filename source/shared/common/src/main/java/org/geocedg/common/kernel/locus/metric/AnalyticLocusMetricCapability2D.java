/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Collections;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/** Explicit analytic/closed-form component metric capability. */
public final class AnalyticLocusMetricCapability2D
		implements LocusMetricCapability2D {
	private final String capabilityVersion;
	private final LocusAnalyticMetricEvaluator2D evaluator;

	/** Creates a versioned analytic capability. */
	public AnalyticLocusMetricCapability2D(String capabilityVersion,
			LocusAnalyticMetricEvaluator2D evaluator) {
		if (capabilityVersion == null || capabilityVersion.trim().isEmpty()) {
			throw new IllegalArgumentException("Capability version is required");
		}
		this.capabilityVersion = capabilityVersion;
		this.evaluator = Objects.requireNonNull(evaluator);
	}

	@Override
	public String getCapabilityVersion() {
		return capabilityVersion;
	}

	@Override
	public MetricEvaluatorMethod2D getEvaluatorMethod() {
		return MetricEvaluatorMethod2D.ANALYTIC;
	}

	@Override
	public boolean supports(LocusDefinition2D definition, LocusBranch2D branch,
			LocusMetricPolicy2D policy) {
		return true;
	}

	@Override
	public LocusMetricComponentState2D buildComponentState(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component, LocusMetricIndexKey2D key,
			LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		LocusAnalyticMetricEvaluation2D evaluation = Objects.requireNonNull(
				evaluator.evaluateLength(branch.getBranchKey(),
						component.getLower(), component.getUpper()));
		if (evaluation.getComputationStatus()
				!= MetricComputationStatus.SUCCESS) {
			throw new LocusMetricComponentBuildException(
					evaluation.getComputationStatus(),
					evaluation.getDiagnostics());
		}
		double[] parameters;
		double[] lengths;
		double[] errors;
		double[] cumulative;
		if (component.getLower() == component.getUpper()) {
			parameters = new double[] {component.getLower()};
			lengths = new double[0];
			errors = new double[0];
			cumulative = new double[] {0};
		} else {
			parameters = new double[] {component.getLower(),
					component.getUpper()};
			double finite = evaluation.getMetricValue().getFiniteValue()
					.orElse(0);
			lengths = new double[] {finite};
			errors = new double[] {establishedAbsolute(
					evaluation.getErrorEvidence())};
			cumulative = new double[] {0, finite};
		}
		return new LocusMetricComponentState2D(key, component,
				new MetricComponentPartition2D(parameters, lengths, errors),
				new MetricArcCoordinateEvidence2D(parameters, cumulative),
				new MetricCapabilityMetadata2D(capabilityVersion,
						MetricEvaluatorMethod2D.ANALYTIC,
						MetricMethod2D.CLOSED_FORM,
						branch.getQuality().getConstructionFidelity()),
				evaluation.getMetricValue(), evaluation.getComputationStatus(),
				evaluation.getRectifiability(),
				evaluation.getErrorEvidence(), evaluation.getDiagnostics());
	}

	@Override
	public LocusMetricContribution2D evaluateRouteSegment(
			LocusMetricComponentState2D state,
			LocusMetricRouteSegment2D segment, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		requireCompatible(state);
		LocusAnalyticMetricEvaluation2D evaluation = Objects.requireNonNull(
				evaluator.evaluateLength(state.getBranchKey(),
						segment.getStartCanonicalParameter(),
						segment.getEndCanonicalParameter()));
		return contribution(state, policy, evaluation);
	}

	@Override
	public LocusMetricContribution2D evaluateCompleteComponent(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		requireCompatible(state);
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(),
				state.getCompleteComponentValue(), state.getComputationStatus(),
				state.getRectifiability(),
				state.getCapabilityMetadata().getConstructionFidelity(),
				MetricEvaluatorMethod2D.ANALYTIC,
				MetricMethod2D.CLOSED_FORM,
				state.getComponentErrorEvidence(), provenance(state, policy),
				state.getDiagnostics());
	}

	private LocusMetricContribution2D contribution(
			LocusMetricComponentState2D state,
			LocusMetricPolicy2D policy,
			LocusAnalyticMetricEvaluation2D evaluation) {
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(), evaluation.getMetricValue(),
				evaluation.getComputationStatus(),
				evaluation.getRectifiability(),
				state.getCapabilityMetadata().getConstructionFidelity(),
				MetricEvaluatorMethod2D.ANALYTIC,
				MetricMethod2D.CLOSED_FORM, evaluation.getErrorEvidence(),
				provenance(state, policy), evaluation.getDiagnostics());
	}

	private void requireCompatible(LocusMetricComponentState2D state) {
		if (!capabilityVersion.equals(state.getCapabilityMetadata()
				.getCapabilityVersion())) {
			throw new IllegalArgumentException(
					"Analytic capability does not match component state");
		}
	}

	private static MetricProvenance2D provenance(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy) {
		return new MetricProvenance2D(state.getLocusIdentity(),
				state.getSemanticRevision(),
				state.getCapabilityMetadata().getCapabilityVersion(),
				policy.getMetricAlgorithmVersion(),
				policy.getMetricPolicyVersion());
	}

	private static double establishedAbsolute(
			MetricErrorEvidence2D evidence) {
		if (evidence.getAbsoluteEvidence()
				instanceof EstablishedMetricErrorAmount2D) {
			return ((EstablishedMetricErrorAmount2D)
					evidence.getAbsoluteEvidence())
							.getNonNegativeFiniteAmount();
		}
		return 0;
	}

}
