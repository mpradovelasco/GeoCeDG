/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;

/**
 * Adaptive world-coordinate evaluator-only metric capability.
 *
 * <p>Refinement agreement is never treated as a certified bound.</p>
 */
public final class EvaluatorOnlyLocusMetricCapability2D
		implements LocusMetricCapability2D {
	private final String capabilityVersion;
	private final boolean directRouteRefinement;
	private LocusDefinition2D currentDefinition;

	/** Creates a versioned point-evaluator-only capability. */
	public EvaluatorOnlyLocusMetricCapability2D(String capabilityVersion) {
		this(capabilityVersion, false);
	}

	/**
	 * Creates an evaluator capability whose route values are refined on the
	 * exact semantic route instead of inheriting complete-component evidence.
	 *
	 * @param capabilityVersion version participating in metric index keys
	 * @return route-local evaluator capability
	 */
	public static EvaluatorOnlyLocusMetricCapability2D
			withDirectRouteRefinement(String capabilityVersion) {
		return new EvaluatorOnlyLocusMetricCapability2D(capabilityVersion, true);
	}

	private EvaluatorOnlyLocusMetricCapability2D(String capabilityVersion,
			boolean directRouteRefinement) {
		if (capabilityVersion == null || capabilityVersion.trim().isEmpty()) {
			throw new IllegalArgumentException("Capability version is required");
		}
		this.capabilityVersion = capabilityVersion;
		this.directRouteRefinement = directRouteRefinement;
	}

	@Override
	public String getCapabilityVersion() {
		return capabilityVersion;
	}

	@Override
	public MetricEvaluatorMethod2D getEvaluatorMethod() {
		return MetricEvaluatorMethod2D.POINT_EVALUATOR_ONLY;
	}

	@Override
	public boolean supports(LocusDefinition2D definition, LocusBranch2D branch,
			LocusMetricPolicy2D policy) {
		currentDefinition = definition;
		return policy.getEvaluatorOnlyPolicy()
				!= EvaluatorOnlyPolicy.UNSUPPORTED;
	}

	@Override
	public LocusMetricComponentState2D buildComponentState(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component, LocusMetricIndexKey2D key,
			LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		currentDefinition = definition;
		if (branch.getProperties().contains(BranchProperty.COLLAPSED_IMAGE)) {
			return collapsedState(branch, component, key);
		}
		if (component.getLower() == component.getUpper()) {
			return zeroState(branch, component, key);
		}
		if ((!component.isLowerClosed() || !component.isUpperClosed())
				&& !isPeriodicFundamentalCycle(definition, branch, component)) {
			throw new LocusMetricComponentBuildException(
					MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.LIMIT_NOT_ESTABLISHED,
							"Evaluator-only improper component limit is not "
									+ "established")));
		}
		Context context = new Context(definition, branch.getBranchKey(),
				policy, instrumentation);
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			context.session = session;
			double lower = component.getLower();
			double upper = component.getUpper();
			double middle = midpoint(lower, upper);
			LocusPoint2D start = context.evaluate(lower);
			LocusPoint2D mid = context.evaluate(middle);
			LocusPoint2D end = context.evaluate(upper);
			Node root = refine(lower, upper, start, mid, end, 0, context);
			context.leaves.sort(Comparator.comparingDouble(leaf -> leaf.start));
			return state(branch, component, key, root, context, policy);
		} catch (WorkLimitException exception) {
			throw new LocusMetricComponentBuildException(
					MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED,
							"Evaluator-only component exceeded "
									+ exception.limit)));
		} catch (LocusMetricComponentBuildException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			throw new LocusMetricComponentBuildException(
					MetricComputationStatus.NUMERICAL_FAILURE,
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.NUMERICAL_FAILURE,
							"Evaluator-only refinement failed: "
									+ exception.getClass().getSimpleName())));
		}
	}

	private static boolean isPeriodicFundamentalCycle(
			LocusDefinition2D definition, LocusBranch2D branch,
			LocusInterval2D component) {
		return definition.getProvider().isPeriodic()
				&& branch.getValidDomainComponents().size() == 1
				&& component.equals(branch.getDeclaredDriverDomain())
				&& component.isLowerClosed() != component.isUpperClosed();
	}

	@Override
	public LocusMetricContribution2D evaluateRouteSegment(
			LocusMetricComponentState2D state,
			LocusMetricRouteSegment2D segment, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		requireCompatible(state);
		if (!directRouteRefinement) {
			double value = state.getArcCoordinateEvidence().estimateLength(
					segment.getStartCanonicalParameter(),
					segment.getEndCanonicalParameter());
			return routeContribution(state, policy, value, subarcError(),
					Collections.emptyList());
		}
		if (currentDefinition == null
				|| currentDefinition.getSemanticRevision()
						!= state.getSemanticRevision()) {
			throw new IllegalStateException(
					"Evaluator-only capability lacks the coherent source revision");
		}
		LocusBranch2D branch = currentDefinition.getBranch(state.getBranchKey());
		if (branch == null) {
			throw new IllegalStateException(
					"Evaluator-only route branch is absent from the source revision");
		}
		if (branch.getProperties().contains(BranchProperty.COLLAPSED_IMAGE)) {
			return routeContribution(state, policy, 0,
					MetricErrorEvidence2D.exact(
							"semantic collapsed-image route"),
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.COLLAPSED_IMAGE,
							"Semantic route image is collapsed")));
		}
		return evaluateRouteDirectly(state, segment, policy, instrumentation);
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
				MetricEvaluatorMethod2D.POINT_EVALUATOR_ONLY,
				MetricMethod2D.ADAPTIVE_EVALUATOR_METRIC,
				state.getComponentErrorEvidence(), provenance(state, policy),
				state.getDiagnostics());
	}

	private LocusMetricComponentState2D state(LocusBranch2D branch,
			LocusInterval2D component, LocusMetricIndexKey2D key, Node root,
			Context context, LocusMetricPolicy2D policy) {
		int count = context.leaves.size();
		double[] parameters = new double[count + 1];
		double[] lengths = new double[count];
		double[] errors = new double[count];
		double[] cumulative = new double[count + 1];
		parameters[0] = component.getLower();
		for (int index = 0; index < count; index++) {
			Leaf leaf = context.leaves.get(index);
			parameters[index + 1] = leaf.end;
			lengths[index] = leaf.length;
			errors[index] = leaf.defect;
			cumulative[index + 1] = cumulative[index] + leaf.length;
		}
		double value = Math.max(0, root.length);
		MetricErrorEvidence2D error =
				policy.getEvaluatorOnlyPolicy()
						== EvaluatorOnlyPolicy
								.ESTIMATED_WITH_EXPLICIT_ASSUMPTIONS
										? MetricErrorEvidence2D.estimated(
												Math.max(0, root.defect),
												value == 0 ? 0
														: root.defect / value,
												"adaptive evaluator metric",
												List.of("curve is rectifiable",
														"refinement defect "
																+ "estimates remaining "
																+ "variation"))
										: MetricErrorEvidence2D.uncertified(
												"adaptive evaluator metric");
		List<MetricDiagnostic2D> diagnostics = value == 0
				? Collections.singletonList(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.COLLAPSED_IMAGE,
						"Evaluated component image is collapsed"))
				: Collections.emptyList();
		return new LocusMetricComponentState2D(key, component,
				new MetricComponentPartition2D(parameters, lengths, errors),
				new MetricArcCoordinateEvidence2D(parameters, cumulative),
				new MetricCapabilityMetadata2D(capabilityVersion,
						MetricEvaluatorMethod2D.POINT_EVALUATOR_ONLY,
						MetricMethod2D.ADAPTIVE_EVALUATOR_METRIC,
						branch.getQuality().getConstructionFidelity()),
				new FiniteMetricValue2D(value),
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE, error, diagnostics);
	}

	private LocusMetricComponentState2D zeroState(LocusBranch2D branch,
			LocusInterval2D component, LocusMetricIndexKey2D key) {
		double[] parameter = {component.getLower()};
		return new LocusMetricComponentState2D(key, component,
				new MetricComponentPartition2D(parameter, new double[0],
						new double[0]),
				new MetricArcCoordinateEvidence2D(parameter,
						new double[] {0}),
				new MetricCapabilityMetadata2D(capabilityVersion,
						MetricEvaluatorMethod2D.POINT_EVALUATOR_ONLY,
						MetricMethod2D.ADAPTIVE_EVALUATOR_METRIC,
						branch.getQuality().getConstructionFidelity()),
				new FiniteMetricValue2D(0),
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				MetricErrorEvidence2D.uncertified(
						"isolated component total variation"),
				Collections.singletonList(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.ISOLATED_POINT,
						"An isolated valid-domain point has zero length")));
	}

	private LocusMetricComponentState2D collapsedState(LocusBranch2D branch,
			LocusInterval2D component, LocusMetricIndexKey2D key) {
		// A collapsed image has identically zero arc coordinate on the whole
		// semantic component. One finite derived reference node is sufficient even
		// when the retained source component is open or unbounded; the component
		// interval itself remains the route/domain authority.
		double reference = Double.isFinite(component.getLower())
				? component.getLower()
				: Double.isFinite(component.getUpper()) ? component.getUpper() : 0;
		double[] parameters = {reference};
		double[] lengths = new double[0];
		double[] cumulative = {0};
		return new LocusMetricComponentState2D(key, component,
				new MetricComponentPartition2D(parameters, lengths,
						new double[lengths.length]),
				new MetricArcCoordinateEvidence2D(parameters, cumulative),
				new MetricCapabilityMetadata2D(capabilityVersion,
						MetricEvaluatorMethod2D.POINT_EVALUATOR_ONLY,
						MetricMethod2D.ADAPTIVE_EVALUATOR_METRIC,
						branch.getQuality().getConstructionFidelity()),
				new FiniteMetricValue2D(0), MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				MetricErrorEvidence2D.exact(
						"semantic collapsed-image branch property"),
				Collections.singletonList(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.COLLAPSED_IMAGE,
						"Semantic component image is collapsed")));
	}

	private static Node refine(double startParameter, double endParameter,
			LocusPoint2D start, LocusPoint2D middle, LocusPoint2D end,
			int depth, Context context) {
		double middleParameter = midpoint(startParameter, endParameter);
		double coarse = distance(start, end);
		double left = distance(start, middle);
		double right = distance(middle, end);
		double fine = left + right;
		double defect = Math.max(0, fine - coarse);
		double threshold = context.policy.threshold(Math.max(fine, coarse));
		if (defect <= threshold
				|| Math.nextUp(startParameter) >= endParameter) {
			if (middleParameter > startParameter
					&& middleParameter < endParameter) {
				context.leaves.add(new Leaf(startParameter, middleParameter,
						left, defect / 2));
				context.leaves.add(new Leaf(middleParameter, endParameter,
						right, defect / 2));
			} else {
				context.leaves.add(new Leaf(startParameter, endParameter,
						fine, defect));
			}
			return new Node(fine, defect);
		}
		context.beforeSubdivision(depth);
		double leftMiddleParameter = midpoint(startParameter, middleParameter);
		double rightMiddleParameter = midpoint(middleParameter, endParameter);
		LocusPoint2D leftMiddle = context.evaluate(leftMiddleParameter);
		LocusPoint2D rightMiddle = context.evaluate(rightMiddleParameter);
		Node leftNode = refine(startParameter, middleParameter, start,
				leftMiddle, middle, depth + 1, context);
		Node rightNode = refine(middleParameter, endParameter, middle,
				rightMiddle, end, depth + 1, context);
		return new Node(leftNode.length + rightNode.length,
				leftNode.defect + rightNode.defect);
	}

	private LocusMetricContribution2D evaluateRouteDirectly(
			LocusMetricComponentState2D state,
			LocusMetricRouteSegment2D segment, LocusMetricPolicy2D policy,
			LocusMetricInstrumentation2D instrumentation) {
		double lower = Math.min(segment.getStartCanonicalParameter(),
				segment.getEndCanonicalParameter());
		double upper = Math.max(segment.getStartCanonicalParameter(),
				segment.getEndCanonicalParameter());
		if (lower == upper) {
			return routeContribution(state, policy, 0,
					MetricErrorEvidence2D.exact(
							"equal semantic route endpoints"),
					Collections.emptyList());
		}
		Context context = new Context(currentDefinition, state.getBranchKey(),
				policy, instrumentation);
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			context.session = session;
			double middle = midpoint(lower, upper);
			LocusPoint2D start = context.evaluate(lower);
			LocusPoint2D mid = context.evaluate(middle);
			LocusPoint2D end = context.evaluate(upper);
			Node root = refine(lower, upper, start, mid, end, 0, context);
			double value = Math.max(0, root.length);
			List<MetricDiagnostic2D> diagnostics = value == 0
					? Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.COLLAPSED_IMAGE,
							"Evaluated route image is collapsed"))
					: Collections.emptyList();
			return routeContribution(state, policy, value,
					routeError(root, value, policy), diagnostics);
		} catch (WorkLimitException exception) {
			throw new LocusMetricComponentBuildException(
					MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED,
							"Evaluator-only route exceeded "
									+ exception.limit)));
		} catch (LocusMetricComponentBuildException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			throw new LocusMetricComponentBuildException(
					MetricComputationStatus.NUMERICAL_FAILURE,
					Collections.singletonList(new MetricDiagnostic2D(
							MetricDiagnosticCode2D.NUMERICAL_FAILURE,
							"Evaluator-only route refinement failed: "
									+ exception.getClass().getSimpleName())));
		}
	}

	private static LocusMetricContribution2D routeContribution(
			LocusMetricComponentState2D state, LocusMetricPolicy2D policy,
			double value, MetricErrorEvidence2D error,
			List<MetricDiagnostic2D> diagnostics) {
		return new LocusMetricContribution2D(state.getBranchKey(),
				state.getResolvedValidComponentKey(),
				new FiniteMetricValue2D(value),
				MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE,
				state.getCapabilityMetadata().getConstructionFidelity(),
				MetricEvaluatorMethod2D.POINT_EVALUATOR_ONLY,
				MetricMethod2D.ADAPTIVE_EVALUATOR_METRIC, error,
				provenance(state, policy), diagnostics);
	}

	private static MetricErrorEvidence2D routeError(Node root, double value,
			LocusMetricPolicy2D policy) {
		if (policy.getEvaluatorOnlyPolicy()
				== EvaluatorOnlyPolicy.ESTIMATED_WITH_EXPLICIT_ASSUMPTIONS) {
			return MetricErrorEvidence2D.estimated(Math.max(0, root.defect),
					value == 0 ? 0 : root.defect / value,
					"adaptive evaluator route metric",
					List.of("route subarc is rectifiable",
							"route-local refinement defect estimates remaining "
									+ "variation"));
		}
		return MetricErrorEvidence2D.uncertified(
				"adaptive evaluator route metric");
	}

	private static MetricErrorEvidence2D subarcError() {
		return MetricErrorEvidence2D.uncertified(
				"route-local error is not established for component "
						+ "arc-coordinate interpolation");
	}

	private static double distance(LocusPoint2D first, LocusPoint2D second) {
		return Math.hypot(first.getX() - second.getX(),
				first.getY() - second.getY());
	}

	private static double midpoint(double start, double end) {
		return start + (end - start) / 2;
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
					"Evaluator-only capability does not match component state");
		}
	}

	private static final class Context {
		private final LocusDefinition2D definition;
		private final String branchKey;
		private final LocusMetricPolicy2D policy;
		private final LocusMetricInstrumentation2D instrumentation;
		private final List<Leaf> leaves = new ArrayList<>();
		private LocusEvaluationSession2D session;
		private long evaluations;
		private long subdivisions;

		private Context(LocusDefinition2D definition, String branchKey,
				LocusMetricPolicy2D policy,
				LocusMetricInstrumentation2D instrumentation) {
			this.definition = definition;
			this.branchKey = branchKey;
			this.policy = policy;
			this.instrumentation = instrumentation;
		}

		private LocusPoint2D evaluate(double parameter) {
			if (evaluations >= policy.getWorkBudget()
					.getMaximumMetricEvaluations()) {
				throw new WorkLimitException(
						MetricWorkLimit2D.MAXIMUM_EVALUATIONS);
			}
			evaluations++;
			instrumentation.recordEvaluatorCall();
			LocusEvaluation2D evaluation =
					definition.evaluate(branchKey, parameter, session);
			if (!evaluation.isValid()) {
				throw new LocusMetricComponentBuildException(
						MetricComputationStatus.NUMERICAL_FAILURE,
						Collections.singletonList(new MetricDiagnostic2D(
								MetricDiagnosticCode2D.EVALUATION_FAILED,
								evaluation.getDiagnostic())));
			}
			return evaluation.getPoint();
		}

		private void beforeSubdivision(int depth) {
			if (depth >= policy.getWorkBudget().getMaximumAdaptiveDepth()) {
				throw new WorkLimitException(MetricWorkLimit2D.MAXIMUM_DEPTH);
			}
			if (subdivisions >= policy.getWorkBudget()
					.getMaximumMetricSubdivisions()) {
				throw new WorkLimitException(
						MetricWorkLimit2D.MAXIMUM_SUBDIVISIONS);
			}
			subdivisions++;
			instrumentation.recordSubdivision();
			instrumentation.recordRefinement();
		}
	}

	private static final class Node {
		private final double length;
		private final double defect;

		private Node(double length, double defect) {
			this.length = length;
			this.defect = defect;
		}
	}

	private static final class Leaf {
		private final double start;
		private final double end;
		private final double length;
		private final double defect;

		private Leaf(double start, double end, double length, double defect) {
			this.start = start;
			this.end = end;
			this.length = length;
			this.defect = defect;
		}
	}

	private static final class WorkLimitException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final MetricWorkLimit2D limit;

		private WorkLimitException(MetricWorkLimit2D limit) {
			this.limit = limit;
		}
	}
}
