/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedSample2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversal2D;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedTraversalPolicy2D;
import org.geocedg.common.kernel.locus.conditioned.LocusSelectionTestimony2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.LocusConditionedRouteJumpGuard2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricComponentBuildException;
import org.geocedg.common.kernel.locus.metric.LocusMetricComponentKey2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResults2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionEvaluationStatus;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.PolicyV2LocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;

/**
 * Engine-level fixture of PRE-G9B-MAINT-B5a ({@code locus-v2-metrics.md} section 28.3.1): a
 * branch outside the certified class whose existence is not established, under a cause
 * without an event of its own, with the conditioned evidence that the traversal of policy
 * {@code conditioned-traversal/v1} publishes for the same function, and the length of the
 * route between two positions addressed by parameter, as the public metric computes it. The
 * traversal, the estimator and the check read the same traced function unless a test gives
 * the traversal another one.
 */
final class PreG9BMaintB5aRoutes {
	static final String BRANCH = "b5a.route.main";

	/** A traced function of the driver parameter; {@code null} is a failed evaluation. */
	interface Curve {
		LocusPoint2D at(double parameter);
	}

	private PreG9BMaintB5aRoutes() {
	}

	/** One measured route with what the test reads from it. */
	static final class Route {
		final LocusMetricResult2D result;
		final LocusConditionedEvidence2D evidence;
		final long definitionEvaluations;

		private Route(LocusMetricResult2D result, LocusConditionedEvidence2D evidence,
				long definitionEvaluations) {
			this.result = result;
			this.evidence = evidence;
			this.definitionEvaluations = definitionEvaluations;
		}

		boolean admitted() {
			return result.isScalarAdmissible();
		}

		boolean has(MetricDiagnosticCode2D code) {
			return result.getDiagnostics().stream().anyMatch(d -> d.getCode() == code);
		}

		String message(MetricDiagnosticCode2D code) {
			return result.getDiagnostics().stream().filter(d -> d.getCode() == code)
					.map(MetricDiagnostic2D::getMessage).findFirst().orElse(null);
		}

		/** @return the evaluations of the check of an admitted route, or -1 */
		long checkEvaluations() {
			return result.getConditionedRoute().flatMap(route -> route.getJumpGuard())
					.map(LocusConditionedRouteJumpGuard2D.Record::getEvaluations).orElse(-1L);
		}

		/** @return the leaves tested by the check of an admitted route, or -1 */
		int leavesTested() {
			return result.getConditionedRoute().flatMap(route -> route.getJumpGuard())
					.map(LocusConditionedRouteJumpGuard2D.Record::getLeavesTested).orElse(-1);
		}

		/** @return whether some code of the chord-scaling check is present */
		boolean anyCheckDiagnostic() {
			return has(MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE)
					|| has(MetricDiagnosticCode2D.CHORD_SCALING_INCONCLUSIVE)
					|| has(MetricDiagnosticCode2D.CHORD_SCALING_BUDGET_EXHAUSTED);
		}

		double value() {
			return result.getMetricValue().getFiniteValue().orElse(Double.NaN);
		}

		/** @return a short deterministic description of the outcome */
		String outcome() {
			if (admitted()) {
				return "ADMITTED";
			}
			for (MetricDiagnosticCode2D code : new MetricDiagnosticCode2D[] {
					MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE,
					MetricDiagnosticCode2D.CHORD_SCALING_INCONCLUSIVE,
					MetricDiagnosticCode2D.CHORD_SCALING_BUDGET_EXHAUSTED,
					MetricDiagnosticCode2D.POSITIONS_IN_DIFFERENT_REGIONS,
					MetricDiagnosticCode2D.ROUTE_CROSSES_BAND,
					MetricDiagnosticCode2D.POSITION_IN_BAND,
					MetricDiagnosticCode2D.EVALUATION_FAILED,
					MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED,
					MetricDiagnosticCode2D.CONSISTENCY_NOT_ESTABLISHED,
					MetricDiagnosticCode2D.PARAMETER_RESOLUTION_REACHED}) {
				if (has(code) || result.getRouteEvidence().map(route -> route.getDiagnostics()
						.stream().anyMatch(d -> d.getCode() == code)).orElse(false)) {
					return code.name();
				}
			}
			return result.getComputationStatus() + " " + result.getDiagnostics();
		}

		@Override
		public String toString() {
			return outcome() + " value=" + value() + " check=" + checkEvaluations() + " "
					+ result.getDiagnostics();
		}
	}

	/** Measures the route {@code a -> b} of the curve on the domain {@code [0, 1]}. */
	static Route route(Curve curve, double a, double b) {
		return route(curve, curve, 0, 1, a, b);
	}

	/**
	 * Measures the route {@code a -> b} of a branch on {@code [lower, upper]}.
	 *
	 * @param curve traced function that the estimator and the check evaluate
	 * @param traversed traced function that the traversal samples (the same, unless a test
	 *        isolates the check)
	 */
	static Route route(Curve curve, Curve traversed, double lower, double upper, double a,
			double b) {
		return route(curve, traversed, parameter -> "a", lower, upper, a, b);
	}

	/** The selection that the producer testifies at a driver parameter. */
	interface Selection {
		String at(double parameter);
	}

	/**
	 * Measures the route {@code a -> b} of a branch on {@code [lower, upper]} whose traversal
	 * also reads the selection that the producer testifies at each node.
	 */
	static Route route(Curve curve, Curve traversed, Selection selection, double lower,
			double upper, double a, double b) {
		LocusInterval2D domain = new LocusInterval2D(lower, upper, true, true);
		LocusExistenceStructure2D structure = LocusExistenceStructure2D.notEstablished(BRANCH,
				domain, DomainTopology.BOUNDED, LocusCoverageCause2D.NOT_CERTIFIED,
				"b5a/route");
		ExplicitNumericDomainProvider2D provider = new ExplicitNumericDomainProvider2D(
				"b5a-parameter/v1", domain, Orientation.INCREASING, false, 1E-14);
		LocusBranch2D branch = new LocusBranch2D(BRANCH, domain, structure,
				Orientation.INCREASING, "b5a-fixture/v1", LocusLineage2D.unchanged(),
				EnumSet.of(BranchProperty.FINITE), LocusQuality2D.analyticDoubleSemantic());
		long[] evaluations = {0};
		LocusDefinition2D definition = new LocusDefinition2D("b5a-route", 1,
				DefinitionStatus.VALID, provider, List.of(branch),
				(source, semanticBranch, parameter, session) -> {
					evaluations[0]++;
					LocusPoint2D point = curve.at(parameter);
					return point == null
							? LocusEvaluation2D.invalid(EvaluationStatus.EVALUATION_FAILED,
									semanticBranch.getQuality(), "b5a fixture failure")
							: LocusEvaluation2D.valid(point, Regularity.UNKNOWN,
									semanticBranch.getQuality());
				}, Determinism.POINTWISE_DETERMINISTIC, "b5a-fixture/v1",
				new LocusInstrumentation2D());
		LocusConditionedEvidence2D evidence = LocusConditionedTraversal2D.traverse(BRANCH, 1,
				lower, upper, LocusConditionedTraversalPolicy2D.v1(), Collections.emptyList(),
				t -> {
					LocusPoint2D point = traversed.at(t);
					return point == null ? LocusConditionedSample2D.failed("b5a fixture")
							: LocusConditionedSample2D.valid(point.getX(), point.getY(),
									List.of(LocusSelectionTestimony2D.established(
											"root-of-the-slice",
											LocusSelectionTestimony2D.Kind.LOCUS_ROOT_TOKEN,
											selection.at(t), 1)));
				});
		LocusMetricPolicy2D policy = LocusMetricPolicy2D.cu1PublicV2();
		LocusMetricInstrumentation2D instrumentation = new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D("b5a-route",
				instrumentation);
		LocusMetricEngine2D engine = new LocusMetricEngine2D();
		engine.setConditionedSource(key -> BRANCH.equals(key) ? evidence : null);
		BetweenPositionsMetricQuery query = new BetweenPositionsMetricQuery(
				addressed(definition, branch, a, curve), addressed(definition, branch, b, curve),
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
				SamePositionPolicy.ZERO_LENGTH, policy);
		long before = evaluations[0];
		LocusMetricResult2D result;
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			result = engine.compute(query, definition, new LocusMetricCapabilityHierarchy2D(
					List.of(new PolicyV2LocusMetricCapability2D())), owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION, instrumentation, "b5a-test");
		} catch (LocusMetricComponentBuildException exception) {
			result = LocusMetricResults2D.failure("b5a-route", 1, policy,
					exception.getComputationStatus(), Optional.empty(),
					exception.getDiagnostics());
		}
		return new Route(result, evidence, evaluations[0] - before);
	}

	/** A position addressed by parameter, as the resolver of such an address binds it. */
	private static MetricPositionBinding2D addressed(LocusDefinition2D definition,
			LocusBranch2D branch, double parameter, Curve curve) {
		return new MetricPositionBinding2D(new LocusSemanticPosition2D(
				definition.getLocusIdentity(), branch.getBranchKey(),
				definition.getProvider().getProviderId(), parameter),
				definition.getSemanticRevision(), Optional.of(
						LocusMetricComponentKey2D.createDeclared(definition, branch, 0,
								branch.getDeclaredDriverDomain())),
				MetricPositionEvaluationStatus.VALID, Optional.ofNullable(curve.at(parameter)),
				Collections.emptyList());
	}

	/** @return the point {@code origin + s * (cos angle, sin angle)} */
	static LocusPoint2D along(double originX, double originY, double angle, double s) {
		return new LocusPoint2D(originX + s * Math.cos(angle), originY + s * Math.sin(angle));
	}
}
