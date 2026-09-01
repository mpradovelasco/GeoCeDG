/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusParameterPartition2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;

/** Deterministic differential-quadrature capability. */
public final class DifferentialLocusMetricCapability2D
		implements LocusMetricCapability2D {
	private final String capabilityVersion;
	private final LocusDifferentialEvaluator2D evaluator;
	private final LocusMetricIntegrator2D integrator =
			new LocusMetricIntegrator2D();
	private LocusDefinition2D currentDefinition;

	/** Creates an explicitly versioned differential capability. */
	public DifferentialLocusMetricCapability2D(String capabilityVersion,
			LocusDifferentialEvaluator2D evaluator) {
		if (capabilityVersion == null || capabilityVersion.trim().isEmpty()) {
			throw new IllegalArgumentException("Capability version is required");
		}
		this.capabilityVersion = capabilityVersion;
		this.evaluator = Objects.requireNonNull(evaluator);
	}

	/**
	 * Creates a capability that discovers the optional derivative authority from
	 * each immutable semantic definition rather than from an algorithm class.
	 */
	public DifferentialLocusMetricCapability2D(String capabilityVersion) {
		if (capabilityVersion == null || capabilityVersion.trim().isEmpty()) {
			throw new IllegalArgumentException("Capability version is required");
		}
		this.capabilityVersion = capabilityVersion;
		this.evaluator = null;
	}

	@Override
	public String getCapabilityVersion() {
		return capabilityVersion;
	}

	@Override
	public MetricEvaluatorMethod2D getEvaluatorMethod() {
		return MetricEvaluatorMethod2D.DIFFERENTIAL;
	}

	@Override
	public boolean supports(LocusDefinition2D definition, LocusBranch2D branch,
			LocusMetricPolicy2D policy) {
		currentDefinition = definition;
		LocusDifferentialEvaluator2D current = evaluator(definition);
		return current != null && current.supportsDifferential(definition);
	}

	@Override
	public LocusMetricComponentState2D buildComponentState(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component, LocusMetricIndexKey2D key,
			LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		currentDefinition = definition;
		if (component.getLower() == component.getUpper()) {
			return zeroState(branch, component, key);
		}
		if ((!component.isLowerClosed() || !component.isUpperClosed())
				&& !isPeriodicFundamentalCycle(definition, branch, component)) {
			return unresolvedImproperState(branch, component, key);
		}
		MetricIntegrationResult2D integration = integrate(definition,
				branch.getBranchKey(), component.getLower(),
				component.getUpper(), policy, instrumentation);
		if (integration.getComputationStatus()
				!= MetricComputationStatus.SUCCESS) {
			throw buildFailure(integration);
		}
		return state(branch, component, key, integration);
	}

	@Override
	public LocusMetricContribution2D evaluateRouteSegment(
			LocusMetricComponentState2D state,
			LocusMetricRouteSegment2D segment, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		requireCompatible(state);
		if (currentDefinition == null
				|| currentDefinition.getSemanticRevision()
						!= state.getSemanticRevision()) {
			throw new IllegalStateException(
					"Differential capability lacks the coherent source revision");
		}
		if (touchesExcludedBoundary(state, segment)) {
			return unresolvedContribution(state, policy,
					"Improper endpoint limit is not established");
		}
		MetricIntegrationResult2D integration = integrate(currentDefinition,
				state.getBranchKey(), segment.getStartCanonicalParameter(),
				segment.getEndCanonicalParameter(), policy, instrumentation);
		return contribution(state, policy, integration);
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
				MetricEvaluatorMethod2D.DIFFERENTIAL,
				MetricMethod2D.ADAPTIVE_DIFFERENTIAL_QUADRATURE,
				state.getComponentErrorEvidence(), provenance(state, policy),
				state.getDiagnostics());
	}

	private MetricIntegrationResult2D integrate(LocusDefinition2D definition,
			String branchKey, double start, double end,
			LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			LocusDifferentialEvaluator2D current = evaluator(definition);
			if (current == null || !current.supportsDifferential(definition)) {
				throw new ArithmeticException(
						"Semantic derivative capability is unavailable");
			}
			List<Double> breakpoints = definition.getEvaluatorCapability()
					instanceof LocusParameterPartition2D
							? ((LocusParameterPartition2D) definition
									.getEvaluatorCapability())
										.getInteriorBreakpoints(branchKey,
												Math.min(start, end),
												Math.max(start, end))
							: Collections.emptyList();
			return integrator.integrate(parameter -> {
				instrumentation.recordDerivativeCall();
				LocusDifferentialEvaluation2D differential =
						current.evaluateDifferential(definition, branchKey,
								parameter, session);
				if (differential == null || !differential.isValid()) {
					throw new ArithmeticException(differential == null
							? "Differential evaluator returned null"
							: differential.getDiagnostic());
				}
				return differential.getSpeed();
			}, start, end, policy, 0, instrumentation, breakpoints);
		}
	}

	private LocusDifferentialEvaluator2D evaluator(
			LocusDefinition2D definition) {
		if (evaluator != null) {
			return evaluator;
		}
		return definition.getEvaluatorCapability()
				instanceof LocusDifferentialEvaluator2D
						? (LocusDifferentialEvaluator2D) definition
								.getEvaluatorCapability()
						: null;
	}

	private LocusMetricComponentState2D state(LocusBranch2D branch,
			LocusInterval2D component, LocusMetricIndexKey2D key,
			MetricIntegrationResult2D integration) {
		return new LocusMetricComponentState2D(key, component,
				integration.getPartition(),
				integration.getArcCoordinateEvidence(),
				metadata(branch), integration.getMetricValue(),
				integration.getComputationStatus(),
				MetricRectifiability.RECTIFIABLE,
				integration.getErrorEvidence(), Collections.emptyList());
	}

	private LocusMetricComponentState2D zeroState(LocusBranch2D branch,
			LocusInterval2D component, LocusMetricIndexKey2D key) {
		double[] parameter = {component.getLower()};
		return new LocusMetricComponentState2D(key, component,
				new MetricComponentPartition2D(parameter, new double[0],
						new double[0]),
				new MetricArcCoordinateEvidence2D(parameter,
						new double[] {0}),
				metadata(branch), new FiniteMetricValue2D(0),
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				MetricErrorEvidence2D.estimated(0, 0,
						"isolated component total variation",
						List.of("isolated valid-domain component")),
				Collections.singletonList(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.ISOLATED_POINT,
						"An isolated valid-domain point has zero length")));
	}

	private LocusMetricComponentState2D unresolvedImproperState(
			LocusBranch2D branch, LocusInterval2D component,
			LocusMetricIndexKey2D key) {
		double[] parameters = {component.getLower(), component.getUpper()};
		MetricDiagnostic2D diagnostic = new MetricDiagnostic2D(
				MetricDiagnosticCode2D.LIMIT_NOT_ESTABLISHED,
				"Complete component has an excluded endpoint; no finite "
						+ "improper limit was established");
		return new LocusMetricComponentState2D(key, component,
				new MetricComponentPartition2D(parameters, new double[] {0},
						new double[] {0}),
				new MetricArcCoordinateEvidence2D(parameters,
						new double[] {0, 0}),
				metadata(branch), new AbsentMetricValue2D(),
				MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				MetricRectifiability.UNDETERMINED,
				MetricErrorEvidence2D.notApplicable(
						"improper limit not established"),
				Collections.singletonList(diagnostic));
	}

	private MetricCapabilityMetadata2D metadata(LocusBranch2D branch) {
		return new MetricCapabilityMetadata2D(capabilityVersion,
				MetricEvaluatorMethod2D.DIFFERENTIAL,
				MetricMethod2D.ADAPTIVE_DIFFERENTIAL_QUADRATURE,
				branch.getQuality().getConstructionFidelity());
	}

	private static LocusMetricContribution2D contribution(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			MetricIntegrationResult2D integration) {
		List<MetricDiagnostic2D> diagnostics =
				integration.getComputationStatus()
						== MetricComputationStatus.SUCCESS
								? Collections.emptyList()
								: Collections.singletonList(new MetricDiagnostic2D(
										integration.getComputationStatus()
												== MetricComputationStatus
														.LIMIT_NOT_ESTABLISHED
																? MetricDiagnosticCode2D
																		.WORK_BUDGET_EXHAUSTED
																: MetricDiagnosticCode2D
																		.NUMERICAL_FAILURE,
										"Differential metric integration did not "
												+ "produce a complete value"));
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(),
				integration.getMetricValue(),
				integration.getComputationStatus(),
				integration.getComputationStatus()
						== MetricComputationStatus.SUCCESS
								? MetricRectifiability.RECTIFIABLE
								: MetricRectifiability.UNDETERMINED,
				state.getCapabilityMetadata().getConstructionFidelity(),
				MetricEvaluatorMethod2D.DIFFERENTIAL,
				MetricMethod2D.ADAPTIVE_DIFFERENTIAL_QUADRATURE,
				integration.getErrorEvidence(), provenance(state, policy),
				diagnostics);
	}

	private static LocusMetricContribution2D unresolvedContribution(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			String message) {
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(),
				new AbsentMetricValue2D(),
				MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				MetricRectifiability.UNDETERMINED,
				state.getCapabilityMetadata().getConstructionFidelity(),
				MetricEvaluatorMethod2D.DIFFERENTIAL,
				MetricMethod2D.IMPROPER_LIMIT,
				MetricErrorEvidence2D.notApplicable(message),
				provenance(state, policy),
				Collections.singletonList(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.LIMIT_NOT_ESTABLISHED,
						message)));
	}

	private boolean touchesExcludedBoundary(
			LocusMetricComponentState2D state,
			LocusMetricRouteSegment2D segment) {
		LocusInterval2D extent = state.getComponentExtent();
		LocusBranch2D branch = currentDefinition == null ? null
				: currentDefinition.getBranch(state.getBranchKey());
		if (branch != null && isPeriodicFundamentalCycle(currentDefinition,
				branch, extent)) {
			return false;
		}
		return !extent.isLowerClosed()
						&& (segment.getStartCanonicalParameter()
										== extent.getLower()
								|| segment.getEndCanonicalParameter()
										== extent.getLower())
				|| !extent.isUpperClosed()
						&& (segment.getStartCanonicalParameter()
										== extent.getUpper()
								|| segment.getEndCanonicalParameter()
										== extent.getUpper());
	}

	private static boolean isPeriodicFundamentalCycle(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component) {
		return definition.getProvider().isPeriodic()
				&& branch.getProperties().contains(BranchProperty.PERIODIC)
				&& branch.getValidDomainComponents().size() == 1
				&& component.equals(branch.getDeclaredDriverDomain())
				&& component.equals(definition.getProvider().getDeclaredDomain());
	}

	private static LocusMetricComponentBuildException buildFailure(
			MetricIntegrationResult2D integration) {
		MetricDiagnosticCode2D code =
				integration.getComputationStatus()
						== MetricComputationStatus.LIMIT_NOT_ESTABLISHED
								? MetricDiagnosticCode2D
										.WORK_BUDGET_EXHAUSTED
								: MetricDiagnosticCode2D.NUMERICAL_FAILURE;
		return new LocusMetricComponentBuildException(
				integration.getComputationStatus(),
				Collections.singletonList(new MetricDiagnostic2D(code,
						"Complete differential component state was not built")));
	}

	private static MetricProvenance2D provenance(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy) {
		return new MetricProvenance2D(state.getLocusIdentity(),
				state.getSemanticRevision(),
				state.getCapabilityMetadata().getCapabilityVersion(),
				policy.getMetricAlgorithmVersion(),
				policy.getMetricPolicyVersion());
	}

	private void requireCompatible(LocusMetricComponentState2D state) {
		if (!capabilityVersion.equals(state.getCapabilityMetadata()
				.getCapabilityVersion())) {
			throw new IllegalArgumentException(
					"Differential capability does not match component state");
		}
	}
}
