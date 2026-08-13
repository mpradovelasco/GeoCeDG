/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Collections;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/** Explicit terminal unsupported metric capability. */
public final class UnsupportedLocusMetricCapability2D
		implements LocusMetricCapability2D {
	private final String capabilityVersion;

	/** Creates an explicit terminal capability marker. */
	public UnsupportedLocusMetricCapability2D(String capabilityVersion) {
		if (capabilityVersion == null || capabilityVersion.trim().isEmpty()) {
			throw new IllegalArgumentException("Capability version is required");
		}
		this.capabilityVersion = capabilityVersion;
	}

	@Override
	public String getCapabilityVersion() {
		return capabilityVersion;
	}

	@Override
	public MetricEvaluatorMethod2D getEvaluatorMethod() {
		return MetricEvaluatorMethod2D.NONE;
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
		double[] parameters = component.getLower() == component.getUpper()
				? new double[] {component.getLower()}
				: new double[] {component.getLower(), component.getUpper()};
		double[] intervals = parameters.length == 1 ? new double[0]
				: new double[] {0};
		MetricDiagnostic2D diagnostic = new MetricDiagnostic2D(
				MetricDiagnosticCode2D.UNSUPPORTED_CAPABILITY,
				"No approved analytic, differential or evaluator-only "
						+ "metric capability is available");
		return new LocusMetricComponentState2D(key, component,
				new MetricComponentPartition2D(parameters, intervals,
						intervals),
				new MetricArcCoordinateEvidence2D(parameters,
						parameters.length == 1 ? new double[] {0}
								: new double[] {0, 0}),
				new MetricCapabilityMetadata2D(capabilityVersion,
						MetricEvaluatorMethod2D.NONE, MetricMethod2D.NONE,
						branch.getQuality().getConstructionFidelity()),
				new AbsentMetricValue2D(),
				MetricComputationStatus.UNSUPPORTED,
				MetricRectifiability.UNDETERMINED,
				MetricErrorEvidence2D.notApplicable(
						"unsupported metric capability"),
				Collections.singletonList(diagnostic));
	}

	@Override
	public LocusMetricContribution2D evaluateRouteSegment(
			LocusMetricComponentState2D state,
			LocusMetricRouteSegment2D segment, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		return contribution(state, policy);
	}

	@Override
	public LocusMetricContribution2D evaluateCompleteComponent(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		return contribution(state, policy);
	}

	private static LocusMetricContribution2D contribution(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy) {
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(),
				new AbsentMetricValue2D(),
				MetricComputationStatus.UNSUPPORTED,
				MetricRectifiability.UNDETERMINED,
				state.getCapabilityMetadata().getConstructionFidelity(),
				MetricEvaluatorMethod2D.NONE, MetricMethod2D.NONE,
				MetricErrorEvidence2D.notApplicable(
						"unsupported metric capability"),
				new MetricProvenance2D(state.getLocusIdentity(),
						state.getSemanticRevision(),
						state.getCapabilityMetadata().getCapabilityVersion(),
						policy.getMetricAlgorithmVersion(),
						policy.getMetricPolicyVersion()),
				state.getDiagnostics());
	}
}
