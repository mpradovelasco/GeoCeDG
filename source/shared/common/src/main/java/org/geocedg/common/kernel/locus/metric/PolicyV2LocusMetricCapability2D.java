/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusParameterPartition2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSimilarityEvaluator2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.LocusSourceSnapshot2D;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.intersection.CertifiedCurveEnclosure2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricEstimator2D.Estimate;
import org.geocedg.common.kernel.locus.metric.LocusMetricEstimator2D.EstimatorFailure;
import org.geocedg.common.kernel.locus.metric.LocusMetricEstimator2D.Mode;
import org.geocedg.common.kernel.locus.metric.LocusMetricEstimator2D.Samples;

/**
 * Public metric capability of policy v2 (PRE-G9B-CU1,
 * {@code locus-v2-metrics.md} section 25; ADR 0037).
 *
 * <p>For one complete component it tries, in order: a structural identity
 * (class 1), the certified cone enclosure (class 2), and the estimator v2, by
 * the differential route when the source has a semantic derivative and by the
 * evaluator-only route otherwise (class 3). A capability that cannot produce
 * its class falls through; a rigorous enclosure wider than the tolerance stays
 * attached to the estimate as evidence, and an estimate outside it is rejected.
 * Every contribution records the requested tolerance of its share (section 25.4).</p>
 *
 * <p>A similarity image is measured through its source (N1, section 25.6): the source
 * is measured with a tolerance that the image's length factor cannot spoil, and
 * the image takes the source's class with its value and error scaled by the
 * rigorously enclosed factor. A transformation therefore never improves the
 * source's class; a collapsed image has exact zero length.</p>
 *
 * <p>Between-position routes reuse the cached component state (section 25.9): whole
 * leaves or boxes contribute their evidence and only the partial ones at the
 * route ends are refined locally.</p>
 */
public final class PolicyV2LocusMetricCapability2D
		implements LocusMetricCapability2D {
	/** Capability version participating in every complete index key. */
	public static final String VERSION = "cu1-public-metric-v2/v1";
	private static final int ROUTE_BOX_BUDGET = 256;
	private static final long ROUTE_EVALUATION_DIVISOR = 32;

	private LocusDefinition2D currentDefinition;

	@Override
	public String getCapabilityVersion() {
		return VERSION;
	}

	@Override
	public MetricEvaluatorMethod2D getEvaluatorMethod() {
		return MetricEvaluatorMethod2D.ANALYTIC;
	}

	@Override
	public boolean supports(LocusDefinition2D definition, LocusBranch2D branch,
			LocusMetricPolicy2D policy) {
		currentDefinition = definition;
		return policy.getEstimatorPolicy() != null
				&& policy.getEvaluatorOnlyPolicy() != EvaluatorOnlyPolicy.UNSUPPORTED;
	}

	@Override
	public LocusMetricComponentState2D buildComponentState(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component, LocusMetricIndexKey2D key,
			LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		currentDefinition = definition;
		if (policy.getEstimatorPolicy() == null) {
			throw new IllegalArgumentException("Metric policy v2 is required");
		}
		Lineage lineage = Lineage.of(definition);
		if (lineage.collapsed
				|| branch.getProperties().contains(BranchProperty.COLLAPSED_IMAGE)) {
			return zeroState(key, branch, component, policy,
					"semantic collapsed image", new MetricDiagnostic2D(
							MetricDiagnosticCode2D.COLLAPSED_IMAGE,
							"Semantic component image is collapsed"));
		}
		if (component.getLower() == component.getUpper()) {
			return zeroState(key, branch, component, policy,
					"isolated component total variation", new MetricDiagnostic2D(
							MetricDiagnosticCode2D.ISOLATED_POINT,
							"An isolated valid-domain point has zero length"));
		}
		LocusBranch2D rootBranch = lineage.root.getBranch(branch.getBranchKey());
		if (rootBranch == null) {
			throw new LocusMetricComponentBuildException(
					MetricComputationStatus.NUMERICAL_FAILURE,
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.BRANCH_MISSING,
							"Similarity source lacks the image branch")));
		}
		LocusDifferentialEvaluator2D differential = differential(lineage.root);
		if ((!component.isLowerClosed() || !component.isUpperClosed())
				&& !isPeriodicFundamentalCycle(definition, branch, component)) {
			if (differential != null) {
				return improperState(key, branch, component);
			}
			throw new LocusMetricComponentBuildException(
					MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.LIMIT_NOT_ESTABLISHED,
							"Evaluator-only improper component limit is not "
									+ "established")));
		}
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.openServiceSession(false, 1)) {
			RootResult root = measureRoot(lineage, branch.getBranchKey(), component,
					policy, instrumentation, session, differential);
			return assemble(key, branch, component, policy, lineage.scale, root);
		} catch (LocusEvaluationSession2D.EvaluationWorkLimitException exception) {
			// A caller's session limit depends on the caller: never memoized.
			throw exception;
		} catch (EstimatorFailure failure) {
			throw failure.toBuildException();
		} catch (LocusMetricComponentBuildException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			throw new LocusMetricComponentBuildException(
					MetricComputationStatus.NUMERICAL_FAILURE,
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.NUMERICAL_FAILURE,
							"Metric policy v2 failed: "
									+ exception.getClass().getSimpleName())));
		}
	}

	@Override
	public LocusMetricContribution2D evaluateCompleteComponent(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		Extension extension = extension(state);
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(),
				state.getCompleteComponentValue(), state.getComputationStatus(),
				state.getRectifiability(),
				state.getCapabilityMetadata().getConstructionFidelity(),
				state.getCapabilityMetadata().getEvaluatorMethod(),
				state.getCapabilityMetadata().getMetricMethod(),
				state.getComponentErrorEvidence(),
				provenance(state, policy, extension.tag), state.getDiagnostics());
	}

	@Override
	public LocusMetricContribution2D evaluateRouteSegment(
			LocusMetricComponentState2D state, LocusMetricRouteSegment2D segment,
			LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		Extension extension = extension(state);
		double low = Math.min(segment.getStartCanonicalParameter(),
				segment.getEndCanonicalParameter());
		double high = Math.max(segment.getStartCanonicalParameter(),
				segment.getEndCanonicalParameter());
		if (low == high) {
			return exactContribution(state, policy, extension,
					"equal semantic route endpoints", 0, Collections.emptyList());
		}
		switch (extension.kind) {
		case ZERO:
			return exactContribution(state, policy, extension,
					"semantic collapsed-image route", extension.share(low, high),
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.COLLAPSED_IMAGE,
							"Semantic route image is collapsed")));
		case IMPROPER:
			if (touchesExcludedBoundary(state, segment)) {
				return unresolvedContribution(state, policy, extension,
						"Improper endpoint limit is not established");
			}
			return routeEstimate(state, policy, instrumentation, extension, low,
					high, true);
		case STRUCTURAL:
			return certifiedContribution(state, policy, extension,
					extension.scale.apply(extension.structural.enclose(low, high)),
					extension.share(low, high), MetricEvaluatorMethod2D.ANALYTIC,
					MetricMethod2D.CLOSED_FORM, extension.structural.getMethod());
		case ENCLOSURE:
			try {
				MetricEnclosure2D enclosure = extension.scale.apply(
						extension.cone.enclose(low, high, ROUTE_BOX_BUDGET));
				double share = extension.share(low, high);
				if (enclosure.certifiedErrorOf(midpoint(enclosure)) <= share) {
					return certifiedContribution(state, policy, extension, enclosure,
							share, MetricEvaluatorMethod2D.DIFFERENTIAL,
							MetricMethod2D.CERTIFIED_CONE_ENCLOSURE,
							CertifiedConeEnclosure2D.METHOD);
				}
			} catch (ArithmeticException | IllegalArgumentException exception) {
				// The route falls through to its route-local estimate.
			}
			return routeEstimate(state, policy, instrumentation, extension, low,
					high, false);
		case ESTIMATE:
			return leafRouteEstimate(state, policy, instrumentation, extension, low,
					high);
		case EVIDENCE:
		default:
			return routeEstimate(state, policy, instrumentation, extension, low,
					high, false);
		}
	}

	private RootResult measureRoot(Lineage lineage, String branchKey,
			LocusInterval2D component, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation,
			LocusEvaluationSession2D session,
			LocusDifferentialEvaluator2D differential) {
		LocusDefinition2D root = lineage.root;
		MetricEstimatorPolicy2D estimatorPolicy = policy.getEstimatorPolicy();
		double lower = component.getLower();
		double upper = component.getUpper();
		LocusMetricEstimator2D estimator = new LocusMetricEstimator2D(root,
				branchKey, differential == null ? Mode.EVALUATOR_ONLY
						: Mode.DIFFERENTIAL,
				differential, session, estimatorPolicy, policy.getWorkBudget(),
				instrumentation);
		Samples samples = estimator.sample(lower, upper,
				breakpoints(root, branchKey, lower, upper));
		double[] tolerances = tolerances(policy, lineage.scale, samples.getExtent());
		double effective = tolerances[0];
		double recorded = tolerances[1];
		List<MetricDiagnostic2D> diagnostics = new ArrayList<>();
		MetricEnclosure2D evidence = null;
		StructuralLocusMetric2D structural =
				StructuralLocusMetric2D.capture(root, branchKey, component);
		if (structural != null) {
			MetricEnclosure2D enclosure = structural.enclose(lower, upper);
			if (consistent(samples, enclosure, effective)) {
				if (enclosure.certifiedErrorOf(midpoint(enclosure)) <= effective) {
					return RootResult.certified(Kind.STRUCTURAL, enclosure, structural,
							null, effective, recorded, diagnostics);
				}
				evidence = enclosure;
			} else {
				diagnostics.add(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.CONSISTENCY_NOT_ESTABLISHED,
						"Structural identity is inconsistent with the evaluated curve"));
			}
		}
		CertifiedConeEnclosure2D cone = null;
		Optional<CertifiedCurveEnclosure2D> model =
				CertifiedCurveEnclosure2D.capture(root, branchKey);
		if (model.isPresent()) {
			cone = CertifiedConeEnclosure2D.build(model.get(),
					samples.getParameters(), effective,
					estimatorPolicy.getMaximumEnclosureBoxes(),
					policy.getWorkBudget().getMaximumAdaptiveDepth(),
					estimatorPolicy.getMinimumUniformDepth());
			if (cone.isResolved()) {
				MetricEnclosure2D total = cone.total();
				if (consistent(samples, total, effective)) {
					if (cone.isCertified()) {
						return RootResult.certified(Kind.ENCLOSURE, total, null, cone,
								effective, recorded, diagnostics);
					}
					evidence = intersect(evidence, total);
				} else {
					cone = null;
					diagnostics.add(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.CONSISTENCY_NOT_ESTABLISHED,
							"Certified enclosure is inconsistent with the evaluated "
									+ "curve"));
				}
			} else {
				cone = null;
			}
		}
		Estimate estimate;
		try {
			estimate = estimator.estimate(samples, effective);
		} catch (EstimatorFailure failure) {
			if (evidence == null) {
				throw failure;
			}
			diagnostics.add(failure.toDiagnostic());
			return RootResult.certified(Kind.EVIDENCE, evidence, structural, cone,
					effective, recorded, diagnostics);
		}
		if (evidence != null && !evidence.contains(estimate.getValue())) {
			diagnostics.add(new MetricDiagnostic2D(
					MetricDiagnosticCode2D.CONSISTENCY_NOT_ESTABLISHED,
					"Estimate lies outside the certified enclosure; only the "
							+ "certified evidence is published"));
			return RootResult.certified(Kind.EVIDENCE, evidence, structural, cone,
					effective, recorded, diagnostics);
		}
		return RootResult.estimate(estimate, estimator.getMode(),
				estimator.hypotheses(estimate), evidence, effective, recorded,
				diagnostics);
	}

	private LocusMetricComponentState2D assemble(LocusMetricIndexKey2D key,
			LocusBranch2D branch, LocusInterval2D component,
			LocusMetricPolicy2D policy, Scale scale, RootResult root) {
		double width = component.getUpper() - component.getLower();
		if (root.kind == Kind.ESTIMATE) {
			Estimate estimate = root.estimate;
			double value = scale.value(estimate.getValue());
			double error = scale.error(estimate.getValue(), estimate.getError(), value);
			List<String> hypotheses = scale.withHypothesis(root.hypotheses);
			boolean differentialRoute = root.mode == Mode.DIFFERENTIAL;
			String method = differentialRoute
					? "estimator v2 differential quadrature"
					: "estimator v2 evaluator-only refinement";
			MetricErrorEvidence2D evidence = estimatedEvidence(policy, error, value,
					method, hypotheses, root.recordedTolerance,
					root.enclosure == null ? null : scale.apply(root.enclosure));
			List<MetricDiagnostic2D> diagnostics = new ArrayList<>(root.diagnostics);
			if (value == 0) {
				diagnostics.add(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.COLLAPSED_IMAGE,
						"Evaluated component image is collapsed"));
			}
			Extension extension = new Extension(Kind.ESTIMATE, root.mode,
					differentialRoute ? "estimate-differential" : "estimate-evaluator",
					width, root.tolerance,
					root.recordedTolerance, scale, null, null, estimate, hypotheses);
			return state(key, branch, component, estimate.getParameters(),
					scale.values(estimate.getValues()),
					scale.errors(estimate.getValues(), estimate.getErrors()),
					differentialRoute ? MetricEvaluatorMethod2D.DIFFERENTIAL
							: MetricEvaluatorMethod2D.POINT_EVALUATOR_ONLY,
					differentialRoute ? MetricMethod2D.ADAPTIVE_DIFFERENTIAL_QUADRATURE
							: MetricMethod2D.ADAPTIVE_EVALUATOR_METRIC,
					value, evidence, diagnostics, extension);
		}
		MetricEnclosure2D enclosure = scale.apply(root.enclosure);
		double value = midpoint(enclosure);
		double error = enclosure.certifiedErrorOf(value);
		boolean closedForm = root.kind == Kind.STRUCTURAL
				|| root.kind == Kind.EVIDENCE && root.cone == null;
		String method = closedForm ? root.structural.getMethod()
				: CertifiedConeEnclosure2D.METHOD + " (" + root.cone.getOutcome()
						+ ")";
		MetricErrorEvidence2D evidence = MetricErrorEvidence2D.certified(error,
				relative(error, value), scale.describe(method),
				"rigorous enclosure " + enclosure)
				.withRequestedTolerance(root.recordedTolerance)
				.withRigorousEnclosure(enclosure);
		double[] parameters;
		double[] lengths;
		double[] errors;
		if (closedForm) {
			parameters = new double[] {component.getLower(), component.getUpper()};
			lengths = new double[] {value};
			errors = new double[] {error};
		} else {
			parameters = root.cone.getParameters();
			double[] lowers = root.cone.getLowers();
			double[] uppers = root.cone.getUppers();
			double[] middles = new double[lowers.length];
			double[] halves = new double[lowers.length];
			for (int index = 0; index < lowers.length; index++) {
				middles[index] = lowers[index] / 2 + uppers[index] / 2;
				halves[index] = Math.max(0, uppers[index] - middles[index]);
			}
			lengths = scale.values(middles);
			errors = scale.errors(middles, halves);
		}
		Kind kind = root.kind == Kind.EVIDENCE ? Kind.EVIDENCE : root.kind;
		String tag = kind == Kind.STRUCTURAL ? "structural"
				: kind == Kind.ENCLOSURE ? "certified-enclosure" : "certified-evidence";
		Extension extension = new Extension(kind, null, tag,
				width, root.tolerance, root.recordedTolerance, scale,
				root.structural, root.cone, null, Collections.emptyList());
		return state(key, branch, component, parameters, lengths, errors,
				closedForm ? MetricEvaluatorMethod2D.ANALYTIC
						: MetricEvaluatorMethod2D.DIFFERENTIAL,
				closedForm ? MetricMethod2D.CLOSED_FORM
						: MetricMethod2D.CERTIFIED_CONE_ENCLOSURE,
				value, evidence, root.diagnostics, extension);
	}

	private LocusMetricComponentState2D state(LocusMetricIndexKey2D key,
			LocusBranch2D branch, LocusInterval2D component, double[] parameters,
			double[] lengths, double[] errors, MetricEvaluatorMethod2D evaluatorMethod,
			MetricMethod2D metricMethod, double value,
			MetricErrorEvidence2D evidence, List<MetricDiagnostic2D> diagnostics,
			Extension extension) {
		double[] cumulative = new double[parameters.length];
		for (int index = 0; index < lengths.length; index++) {
			cumulative[index + 1] = cumulative[index] + lengths[index];
		}
		return new LocusMetricComponentState2D(key, component,
				new MetricComponentPartition2D(parameters, lengths, errors),
				new MetricArcCoordinateEvidence2D(parameters, cumulative),
				new MetricCapabilityMetadata2D(VERSION, evaluatorMethod, metricMethod,
						branch.getQuality().getConstructionFidelity()),
				new FiniteMetricValue2D(value), MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE, evidence, diagnostics, extension);
	}

	private LocusMetricComponentState2D zeroState(LocusMetricIndexKey2D key,
			LocusBranch2D branch, LocusInterval2D component,
			LocusMetricPolicy2D policy, String method,
			MetricDiagnostic2D diagnostic) {
		double reference = Double.isFinite(component.getLower())
				? component.getLower()
				: Double.isFinite(component.getUpper()) ? component.getUpper() : 0;
		double[] parameters = {reference};
		Extension extension = new Extension(Kind.ZERO, null, "structural-zero",
				0, policy.getAbsoluteTolerance(),
				policy.getAbsoluteTolerance(), Scale.IDENTITY, null, null, null,
				Collections.emptyList());
		return new LocusMetricComponentState2D(key, component,
				new MetricComponentPartition2D(parameters, new double[0],
						new double[0]),
				new MetricArcCoordinateEvidence2D(parameters, new double[] {0}),
				new MetricCapabilityMetadata2D(VERSION,
						MetricEvaluatorMethod2D.ANALYTIC, MetricMethod2D.CLOSED_FORM,
						branch.getQuality().getConstructionFidelity()),
				new FiniteMetricValue2D(0), MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				MetricErrorEvidence2D.exact(method)
						.withRequestedTolerance(policy.getAbsoluteTolerance()),
				Collections.singletonList(diagnostic), extension);
	}

	private LocusMetricComponentState2D improperState(LocusMetricIndexKey2D key,
			LocusBranch2D branch, LocusInterval2D component) {
		boolean finite = Double.isFinite(component.getLower())
				&& Double.isFinite(component.getUpper());
		double[] parameters = finite
				? new double[] {component.getLower(), component.getUpper()}
				: new double[] {Double.isFinite(component.getLower())
						? component.getLower()
						: Double.isFinite(component.getUpper()) ? component.getUpper()
								: 0};
		double[] lengths = finite ? new double[] {0} : new double[0];
		Extension extension = new Extension(Kind.IMPROPER, Mode.DIFFERENTIAL,
				"improper", 0, 0, 0, Scale.IDENTITY, null,
				null, null, Collections.emptyList());
		return new LocusMetricComponentState2D(key, component,
				new MetricComponentPartition2D(parameters, lengths,
						new double[lengths.length]),
				new MetricArcCoordinateEvidence2D(parameters,
						new double[parameters.length]),
				new MetricCapabilityMetadata2D(VERSION,
						MetricEvaluatorMethod2D.DIFFERENTIAL,
						MetricMethod2D.ADAPTIVE_DIFFERENTIAL_QUADRATURE,
						branch.getQuality().getConstructionFidelity()),
				new AbsentMetricValue2D(),
				MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				MetricRectifiability.UNDETERMINED,
				MetricErrorEvidence2D.notApplicable("improper limit not established"),
				Collections.singletonList(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.LIMIT_NOT_ESTABLISHED,
						"Complete component has an excluded endpoint; no finite "
								+ "improper limit was established")),
				extension);
	}

	/** Whole cached leaves plus route-local estimates of the partial leaves. */
	private LocusMetricContribution2D leafRouteEstimate(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation, Extension extension,
			double low, double high) {
		Estimate cached = extension.estimate;
		double[] parameters = cached.getParameters();
		double[] values = cached.getValues();
		double[] errors = cached.getErrors();
		Lineage lineage = Lineage.of(coherentDefinition(state));
		String branchKey = state.getBranchKey();
		double sum = 0;
		double compensation = 0;
		double errorSum = 0;
		int unordered = 0;
		int subResolution = 0;
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.openServiceSession(false, 1)) {
			LocusMetricEstimator2D estimator = new LocusMetricEstimator2D(
					lineage.root, branchKey, extension.mode,
					extension.mode == Mode.DIFFERENTIAL ? differential(lineage.root)
							: null,
					session, policy.getEstimatorPolicy(), routeBudget(policy),
					instrumentation);
			for (int index = 0; index < values.length; index++) {
				double leafLower = parameters[index];
				double leafUpper = parameters[index + 1];
				if (leafUpper <= low || leafLower >= high) {
					continue;
				}
				double value;
				double error;
				if (leafLower >= low && leafUpper <= high) {
					value = values[index];
					error = errors[index];
				} else {
					Estimate local = estimator.estimateRange(Math.max(leafLower, low),
							Math.min(leafUpper, high), extension.tolerance,
							extension.width);
					value = local.getValue();
					error = local.getError();
					unordered += local.getUnorderedLeaves();
					subResolution += local.getSubResolutionCells();
				}
				double next = sum + value;
				compensation += Math.abs(sum) >= Math.abs(value)
						? (sum - next) + value : (value - next) + sum;
				sum = next;
				errorSum = MetricOutwardArithmetic2D.addUp(errorSum, error);
			}
		} catch (LocusEvaluationSession2D.EvaluationWorkLimitException exception) {
			throw exception;
		} catch (EstimatorFailure failure) {
			return failedContribution(state, policy, extension, failure);
		}
		double rootValue = Math.max(0, sum + compensation);
		double value = extension.scale.value(rootValue);
		double error = extension.scale.error(rootValue, Math.max(0, errorSum), value);
		ArrayList<String> hypotheses = new ArrayList<>(extension.hypotheses);
		hypotheses.add("between-position route: whole cached leaves and route-local "
				+ "refinement of the partial leaves (locus-v2-metrics.md 25.9)");
		if (unordered > 0) {
			hypotheses.add("route-local refinement withheld the correction on "
					+ unordered + " leaves: estimate without demonstrated order");
		}
		if (subResolution > 0) {
			hypotheses.add(subResolution + " route-end cell(s) narrower than the "
					+ "refinement stencil accepted as chords: no feature narrower "
					+ "than the parameter resolution");
		}
		return estimateContribution(state, policy, extension, value, error,
				extension.share(low, high), hypotheses);
	}

	/** A complete route-local estimate with its own minimum partition. */
	private LocusMetricContribution2D routeEstimate(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation, Extension extension,
			double low, double high, boolean ownTolerance) {
		Lineage lineage = Lineage.of(coherentDefinition(state));
		String branchKey = state.getBranchKey();
		LocusDifferentialEvaluator2D differential = differential(lineage.root);
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.openServiceSession(false, 1)) {
			LocusMetricEstimator2D estimator = new LocusMetricEstimator2D(
					lineage.root, branchKey,
					differential == null ? Mode.EVALUATOR_ONLY : Mode.DIFFERENTIAL,
					differential, session, policy.getEstimatorPolicy(),
					policy.getWorkBudget(), instrumentation);
			Samples samples = estimator.sample(low, high,
					breakpoints(lineage.root, branchKey, low, high));
			double effective;
			double recorded;
			if (ownTolerance) {
				double[] tolerances = tolerances(policy, lineage.scale,
						samples.getExtent());
				effective = tolerances[0];
				recorded = tolerances[1];
			} else {
				effective = extension.tolerance * ((high - low) / extension.width);
				recorded = extension.share(low, high);
			}
			Estimate estimate = estimator.estimate(samples, effective);
			double value = lineage.scale.value(estimate.getValue());
			double error = lineage.scale.error(estimate.getValue(),
					estimate.getError(), value);
			ArrayList<String> hypotheses = new ArrayList<>(
					lineage.scale.withHypothesis(estimator.hypotheses(estimate)));
			hypotheses.add("route-local estimate with its own minimum partition");
			Extension routeExtension = new Extension(Kind.ESTIMATE,
					estimator.getMode(), estimator.getMode() == Mode.DIFFERENTIAL
							? "estimate-differential" : "estimate-evaluator",
					high - low, effective, recorded, lineage.scale, null, null,
					estimate, hypotheses);
			return estimateContribution(state, policy, routeExtension, value, error,
					recorded, hypotheses);
		} catch (LocusEvaluationSession2D.EvaluationWorkLimitException exception) {
			throw exception;
		} catch (EstimatorFailure failure) {
			return failedContribution(state, policy, extension, failure);
		}
	}

	private static LocusMetricContribution2D estimateContribution(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			Extension extension, double value, double error, double share,
			List<String> hypotheses) {
		boolean differentialRoute = extension.mode == Mode.DIFFERENTIAL;
		MetricErrorEvidence2D evidence = estimatedEvidence(policy, error, value,
				differentialRoute ? "estimator v2 differential route quadrature"
						: "estimator v2 evaluator-only route refinement",
				hypotheses, share, null);
		List<MetricDiagnostic2D> diagnostics = value == 0
				? Collections.singletonList(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.COLLAPSED_IMAGE,
						"Evaluated route image is collapsed"))
				: Collections.emptyList();
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(), new FiniteMetricValue2D(value),
				MetricComputationStatus.SUCCESS, MetricRectifiability.RECTIFIABLE,
				state.getCapabilityMetadata().getConstructionFidelity(),
				differentialRoute ? MetricEvaluatorMethod2D.DIFFERENTIAL
						: MetricEvaluatorMethod2D.POINT_EVALUATOR_ONLY,
				differentialRoute ? MetricMethod2D.ADAPTIVE_DIFFERENTIAL_QUADRATURE
						: MetricMethod2D.ADAPTIVE_EVALUATOR_METRIC,
				evidence, provenance(state, policy, extension.tag), diagnostics);
	}

	private static LocusMetricContribution2D certifiedContribution(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			Extension extension, MetricEnclosure2D enclosure, double share,
			MetricEvaluatorMethod2D evaluatorMethod, MetricMethod2D metricMethod,
			String method) {
		double value = midpoint(enclosure);
		double error = enclosure.certifiedErrorOf(value);
		MetricErrorEvidence2D evidence = MetricErrorEvidence2D.certified(error,
				relative(error, value), extension.scale.describe(method),
				"rigorous route enclosure " + enclosure)
				.withRequestedTolerance(share).withRigorousEnclosure(enclosure);
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(), new FiniteMetricValue2D(value),
				MetricComputationStatus.SUCCESS, MetricRectifiability.RECTIFIABLE,
				state.getCapabilityMetadata().getConstructionFidelity(),
				evaluatorMethod, metricMethod, evidence,
				provenance(state, policy, extension.tag), Collections.emptyList());
	}

	private static LocusMetricContribution2D exactContribution(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			Extension extension, String method, double share,
			List<MetricDiagnostic2D> diagnostics) {
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(), new FiniteMetricValue2D(0),
				MetricComputationStatus.SUCCESS, MetricRectifiability.RECTIFIABLE,
				state.getCapabilityMetadata().getConstructionFidelity(),
				MetricEvaluatorMethod2D.ANALYTIC, MetricMethod2D.CLOSED_FORM,
				MetricErrorEvidence2D.exact(method).withRequestedTolerance(share),
				provenance(state, policy, extension.tag), diagnostics);
	}

	private static LocusMetricContribution2D unresolvedContribution(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			Extension extension, String message) {
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(), new AbsentMetricValue2D(),
				MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				MetricRectifiability.UNDETERMINED,
				state.getCapabilityMetadata().getConstructionFidelity(),
				MetricEvaluatorMethod2D.DIFFERENTIAL, MetricMethod2D.IMPROPER_LIMIT,
				MetricErrorEvidence2D.notApplicable(message),
				provenance(state, policy, extension.tag),
				Collections.singletonList(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.LIMIT_NOT_ESTABLISHED, message)));
	}

	private static LocusMetricContribution2D failedContribution(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			Extension extension, EstimatorFailure failure) {
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(), new AbsentMetricValue2D(),
				failure.getStatus(), MetricRectifiability.UNDETERMINED,
				state.getCapabilityMetadata().getConstructionFidelity(),
				extension.mode == Mode.DIFFERENTIAL
						? MetricEvaluatorMethod2D.DIFFERENTIAL
						: MetricEvaluatorMethod2D.POINT_EVALUATOR_ONLY,
				MetricMethod2D.NONE,
				MetricErrorEvidence2D.notApplicable("route estimate not established"),
				provenance(state, policy, extension.tag),
				Collections.singletonList(failure.toDiagnostic()));
	}

	private static MetricErrorEvidence2D estimatedEvidence(
			LocusMetricPolicy2D policy, double error, double value, String method,
			List<String> hypotheses, double tolerance, MetricEnclosure2D enclosure) {
		MetricErrorEvidence2D evidence = policy.getEvaluatorOnlyPolicy()
				== EvaluatorOnlyPolicy.ESTIMATED_WITH_EXPLICIT_ASSUMPTIONS
						? MetricErrorEvidence2D.estimated(error, relative(error, value),
								method, hypotheses)
						: MetricErrorEvidence2D.uncertified(method);
		evidence = evidence.withRequestedTolerance(tolerance);
		return enclosure == null ? evidence
				: evidence.withRigorousEnclosure(enclosure);
	}

	private static double[] tolerances(LocusMetricPolicy2D policy, Scale scale,
			double extent) {
		double own = policy.threshold(extent);
		if (scale.isIdentity()) {
			return new double[] {own, own};
		}
		double imageExtent = extent == 0 ? 0
				: MetricOutwardArithmetic2D.nonNegativeDown(
						MetricOutwardArithmetic2D.mulDown(extent, scale.factor.lower));
		double recorded = policy.threshold(imageExtent);
		double cap = scale.factor.upper > 0
				? MetricOutwardArithmetic2D.divDown(recorded, scale.factor.upper)
				: own;
		return new double[] {Math.max(0, Math.min(own, cap)), recorded};
	}

	private static boolean consistent(Samples samples, MetricEnclosure2D enclosure,
			double tolerance) {
		// An inscribed polyline never exceeds the length of the curve; a longer
		// one shows that the certificate does not describe the evaluated curve.
		return samples.getChordSum()
				<= MetricOutwardArithmetic2D.addUp(enclosure.getUpper(), tolerance);
	}

	private static MetricEnclosure2D intersect(MetricEnclosure2D first,
			MetricEnclosure2D second) {
		if (first == null) {
			return second;
		}
		double lower = Math.max(first.getLower(), second.getLower());
		double upper = Math.min(first.getUpper(), second.getUpper());
		return lower <= upper
				? new MetricEnclosure2D(lower, upper, "intersection of certified "
						+ "enclosures")
				: first.getWidth() <= second.getWidth() ? first : second;
	}

	private static double midpoint(MetricEnclosure2D enclosure) {
		double middle = enclosure.getLower() / 2 + enclosure.getUpper() / 2;
		return Math.max(enclosure.getLower(), Math.min(enclosure.getUpper(), middle));
	}

	private static double relative(double error, double value) {
		return value > 0 ? MetricOutwardArithmetic2D.divUp(error, value) : 0;
	}

	private static MetricWorkBudget2D routeBudget(LocusMetricPolicy2D policy) {
		MetricWorkBudget2D budget = policy.getWorkBudget();
		return new MetricWorkBudget2D(
				Math.max(16, budget.getMaximumMetricEvaluations()
						/ ROUTE_EVALUATION_DIVISOR),
				Math.max(8, budget.getMaximumMetricSubdivisions()
						/ ROUTE_EVALUATION_DIVISOR),
				budget.getMaximumAdaptiveDepth());
	}

	private static List<Double> breakpoints(LocusDefinition2D root,
			String branchKey, double lower, double upper) {
		if (root.getEvaluatorCapability() instanceof LocusParameterPartition2D) {
			return ((LocusParameterPartition2D) root.getEvaluatorCapability())
					.getInteriorBreakpoints(branchKey, lower, upper);
		}
		return Collections.emptyList();
	}

	private static LocusDifferentialEvaluator2D differential(
			LocusDefinition2D root) {
		Object capability = root.getEvaluatorCapability();
		if (capability instanceof LocusDifferentialEvaluator2D
				&& ((LocusDifferentialEvaluator2D) capability)
						.supportsDifferential(root)) {
			return (LocusDifferentialEvaluator2D) capability;
		}
		return null;
	}

	private static boolean isPeriodicFundamentalCycle(LocusDefinition2D definition,
			LocusBranch2D branch, LocusInterval2D component) {
		if (!definition.getProvider().isPeriodic()
				|| branch.getValidDomainComponents().size() != 1
				|| !component.equals(branch.getDeclaredDriverDomain())) {
			return false;
		}
		return component.isLowerClosed() != component.isUpperClosed()
				|| branch.getProperties().contains(BranchProperty.PERIODIC)
						&& component.equals(definition.getProvider()
								.getDeclaredDomain());
	}

	private boolean touchesExcludedBoundary(LocusMetricComponentState2D state,
			LocusMetricRouteSegment2D segment) {
		LocusInterval2D extent = state.getComponentExtent();
		LocusDefinition2D definition = coherentDefinition(state);
		LocusBranch2D branch = definition.getBranch(state.getBranchKey());
		if (branch != null && isPeriodicFundamentalCycle(definition, branch, extent)) {
			return false;
		}
		return !extent.isLowerClosed()
						&& (segment.getStartCanonicalParameter() == extent.getLower()
								|| segment.getEndCanonicalParameter()
										== extent.getLower())
				|| !extent.isUpperClosed()
						&& (segment.getStartCanonicalParameter() == extent.getUpper()
								|| segment.getEndCanonicalParameter()
										== extent.getUpper());
	}

	private LocusDefinition2D coherentDefinition(LocusMetricComponentState2D state) {
		if (currentDefinition == null
				|| currentDefinition.getSemanticRevision() != state.getSemanticRevision()
				|| !currentDefinition.getLocusIdentity()
						.equals(state.getLocusIdentity())) {
			throw new IllegalStateException(
					"Metric policy v2 lacks the coherent source revision");
		}
		return currentDefinition;
	}

	private static MetricProvenance2D provenance(LocusMetricComponentState2D state,
			LocusMetricPolicy2D policy, String tag) {
		return new MetricProvenance2D(state.getLocusIdentity(),
				state.getSemanticRevision(), VERSION + "#" + tag,
				policy.getMetricAlgorithmVersion(), policy.getMetricPolicyVersion());
	}

	private static Extension extension(LocusMetricComponentState2D state) {
		if (!VERSION.equals(state.getCapabilityMetadata().getCapabilityVersion())
				|| !(state.getCapabilityExtension() instanceof Extension)) {
			throw new IllegalArgumentException(
					"Metric policy v2 does not match the component state");
		}
		return (Extension) state.getCapabilityExtension();
	}

	/** Result kinds of a component state. */
	private enum Kind {
		ZERO, STRUCTURAL, ENCLOSURE, ESTIMATE, EVIDENCE, IMPROPER
	}

	/** Similarity lineage of a definition down to its measured source. */
	private static final class Lineage {
		private final LocusDefinition2D root;
		private final boolean collapsed;
		private final Scale scale;

		private Lineage(LocusDefinition2D root, boolean collapsed, Scale scale) {
			this.root = root;
			this.collapsed = collapsed;
			this.scale = scale;
		}

		private static Lineage of(LocusDefinition2D definition) {
			LocusDefinition2D current = definition;
			ArrayList<LocusSimilarityTransform2D> transforms = new ArrayList<>();
			boolean collapsed = false;
			while (current.getEvaluatorCapability()
					instanceof LocusSimilarityEvaluator2D) {
				if (transforms.size()
						>= PiecewisePolynomialLocus2D.MAXIMUM_SAFE_COMPOSITION_DEPTH) {
					// Too deep to measure through the source: measured directly.
					return new Lineage(definition, false, Scale.IDENTITY);
				}
				LocusSimilarityEvaluator2D similarity =
						(LocusSimilarityEvaluator2D) current.getEvaluatorCapability();
				LocusSimilarityTransform2D transform =
						similarity.getCapturedTransform();
				collapsed |= transform.isCollapsed();
				transforms.add(transform);
				current = similarity.getCapturedSourceDefinition();
			}
			return new Lineage(current, collapsed,
					collapsed ? Scale.IDENTITY : Scale.of(transforms));
		}
	}

	/** Rigorously enclosed length factor of a similarity lineage (N1). */
	private static final class Scale {
		private static final Scale IDENTITY =
				new Scale(MetricOutwardInterval2D.ONE);

		private final MetricOutwardInterval2D factor;

		private Scale(MetricOutwardInterval2D factor) {
			this.factor = factor;
		}

		private static Scale of(List<LocusSimilarityTransform2D> transforms) {
			if (transforms.isEmpty()) {
				return IDENTITY;
			}
			MetricLinearPart2D product = MetricLinearPart2D.IDENTITY;
			for (LocusSimilarityTransform2D transform : transforms) {
				// Outermost first: product = L_outer * ... * L_inner.
				product = product.multiply(linearPart(transform));
			}
			MetricOutwardInterval2D bounds = product.singularValueBounds();
			return bounds.isPoint() && bounds.lower == 1 ? IDENTITY
					: new Scale(bounds);
		}

		/**
		 * Linear part in the semantics of the certified interval models: the
		 * rotation uses the evaluator's binary64 cosine and sine.
		 */
		private static MetricLinearPart2D linearPart(
				LocusSimilarityTransform2D transform) {
			LocusSourceSnapshot2D values = transform.getCapturedValues();
			switch (transform.getKind()) {
			case TRANSLATION:
				return MetricLinearPart2D.IDENTITY;
			case POINT_REFLECTION:
				return MetricLinearPart2D.scalar(-1);
			case ROTATION:
				double cosine = Math.cos(values.get(0));
				double sine = Math.sin(values.get(0));
				return MetricLinearPart2D.of(cosine, -sine, sine, cosine);
			case LINE_REFLECTION:
				MetricOutwardInterval2D a = MetricOutwardInterval2D.point(values.get(0));
				MetricOutwardInterval2D b = MetricOutwardInterval2D.point(values.get(1));
				MetricOutwardInterval2D two = MetricOutwardInterval2D.point(2);
				MetricOutwardInterval2D ab = two.multiply(a).multiply(b).negate();
				return new MetricLinearPart2D(
						MetricOutwardInterval2D.ONE.subtract(two.multiply(a.square())),
						ab, ab,
						MetricOutwardInterval2D.ONE.subtract(two.multiply(b.square())));
			case DILATION:
				return MetricLinearPart2D.scalar(values.get(0));
			default:
				throw new ArithmeticException("Unsupported similarity family");
			}
		}

		private boolean isIdentity() {
			return this == IDENTITY;
		}

		private MetricEnclosure2D apply(MetricEnclosure2D enclosure) {
			if (isIdentity()) {
				return enclosure;
			}
			double lower = enclosure.getLower() == 0 ? 0
					: MetricOutwardArithmetic2D.nonNegativeDown(
							MetricOutwardArithmetic2D.mulDown(enclosure.getLower(),
									factor.lower));
			double upper = enclosure.getUpper() == 0 ? 0
					: MetricOutwardArithmetic2D.mulUp(enclosure.getUpper(),
							factor.upper);
			return new MetricEnclosure2D(lower, Math.max(lower, upper),
					describe(enclosure.getMethod()));
		}

		private double middle() {
			return factor.isPoint() ? factor.lower
					: factor.lower / 2 + factor.upper / 2;
		}

		private double value(double rootValue) {
			return isIdentity() ? rootValue : middle() * rootValue;
		}

		/**
		 * @return {@code lambda_up * e} plus the factor uncertainty times the value and
		 *         the rounding bound of the product
		 */
		private double error(double rootValue, double rootError, double imageValue) {
			if (isIdentity()) {
				return rootError;
			}
			double middle = middle();
			double spread = factor.isPoint() ? 0
					: Math.max(MetricOutwardArithmetic2D.subUp(factor.upper, middle),
							MetricOutwardArithmetic2D.subUp(middle, factor.lower));
			double error = rootError == 0 ? 0
					: MetricOutwardArithmetic2D.mulUp(rootError, factor.upper);
			if (spread > 0 && rootValue > 0) {
				error = MetricOutwardArithmetic2D.addUp(error,
						MetricOutwardArithmetic2D.mulUp(spread, rootValue));
			}
			return imageValue == 0 ? error
					: MetricOutwardArithmetic2D.addUp(error, Math.ulp(imageValue));
		}

		private double[] values(double[] rootValues) {
			double[] result = new double[rootValues.length];
			for (int index = 0; index < result.length; index++) {
				result[index] = Math.max(0, value(rootValues[index]));
			}
			return result;
		}

		private double[] errors(double[] rootValues, double[] rootErrors) {
			double[] result = new double[rootErrors.length];
			for (int index = 0; index < result.length; index++) {
				double rootValue = rootValues[index];
				result[index] = error(rootValue, rootErrors[index], value(rootValue));
			}
			return result;
		}

		private String describe(String method) {
			return isIdentity() ? method
					: method + " of the similarity source, length factor in "
							+ factor;
		}

		private List<String> withHypothesis(List<String> hypotheses) {
			if (isIdentity()) {
				return hypotheses;
			}
			ArrayList<String> result = new ArrayList<>(hypotheses);
			result.add("similarity image of the measured source with length factor "
					+ "in " + factor + " (locus-v2-metrics.md 25.6)");
			return Collections.unmodifiableList(result);
		}
	}

	/** Root-level measurement before the similarity factor is applied. */
	private static final class RootResult {
		private final Kind kind;
		private final MetricEnclosure2D enclosure;
		private final StructuralLocusMetric2D structural;
		private final CertifiedConeEnclosure2D cone;
		private final Estimate estimate;
		private final Mode mode;
		private final List<String> hypotheses;
		private final double tolerance;
		private final double recordedTolerance;
		private final List<MetricDiagnostic2D> diagnostics;

		private RootResult(Kind kind, MetricEnclosure2D enclosure,
				StructuralLocusMetric2D structural, CertifiedConeEnclosure2D cone,
				Estimate estimate, Mode mode, List<String> hypotheses,
				double tolerance, double recordedTolerance,
				List<MetricDiagnostic2D> diagnostics) {
			this.kind = kind;
			this.enclosure = enclosure;
			this.structural = structural;
			this.cone = cone;
			this.estimate = estimate;
			this.mode = mode;
			this.hypotheses = hypotheses;
			this.tolerance = tolerance;
			this.recordedTolerance = recordedTolerance;
			this.diagnostics = Collections.unmodifiableList(
					new ArrayList<>(diagnostics));
		}

		private static RootResult certified(Kind kind, MetricEnclosure2D enclosure,
				StructuralLocusMetric2D structural, CertifiedConeEnclosure2D cone,
				double tolerance, double recordedTolerance,
				List<MetricDiagnostic2D> diagnostics) {
			return new RootResult(kind, enclosure, structural, cone, null, null,
					Collections.emptyList(), tolerance, recordedTolerance,
					diagnostics);
		}

		private static RootResult estimate(Estimate estimate, Mode mode,
				List<String> hypotheses, MetricEnclosure2D evidence,
				double tolerance, double recordedTolerance,
				List<MetricDiagnostic2D> diagnostics) {
			return new RootResult(Kind.ESTIMATE, evidence, null, null, estimate,
					mode, hypotheses, tolerance, recordedTolerance, diagnostics);
		}
	}

	/** Immutable capability-specific data of one component state. */
	private static final class Extension {
		private final Kind kind;
		private final Mode mode;
		private final String tag;
		private final double width;
		private final double tolerance;
		private final double recordedTolerance;
		private final Scale scale;
		private final StructuralLocusMetric2D structural;
		private final CertifiedConeEnclosure2D cone;
		private final Estimate estimate;
		private final List<String> hypotheses;

		private Extension(Kind kind, Mode mode, String tag,
				double width, double tolerance, double recordedTolerance,
				Scale scale, StructuralLocusMetric2D structural,
				CertifiedConeEnclosure2D cone, Estimate estimate,
				List<String> hypotheses) {
			this.kind = kind;
			this.mode = mode;
			this.tag = tag;
			this.width = width;
			this.tolerance = tolerance;
			this.recordedTolerance = recordedTolerance;
			this.scale = scale;
			this.structural = structural;
			this.cone = cone;
			this.estimate = estimate;
			this.hypotheses = Collections.unmodifiableList(
					new ArrayList<>(hypotheses));
		}

		/** @return share of the recorded tolerance owned by a route range */
		private double share(double low, double high) {
			return width > 0 ? recordedTolerance * ((high - low) / width)
					: recordedTolerance;
		}
	}
}
