/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusParameterPartition2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;

/** Bounded query-local context supplied to one isolation capability. */
public final class IntersectionCapabilityContext2D {
	private final LocusIntersectionQuery2D query;
	private final LocusDefinition2D definition;
	private final LocusIntersectionTarget2D target;
	private final LocusEvaluationSession2D evaluationSession;
	private final LocusIntersectionInstrumentation2D instrumentation;

	/** Creates one coherent private capability context. */
	public IntersectionCapabilityContext2D(LocusIntersectionQuery2D query,
			LocusDefinition2D definition, LocusIntersectionTarget2D target,
			LocusEvaluationSession2D evaluationSession,
			LocusIntersectionInstrumentation2D instrumentation) {
		this.query = java.util.Objects.requireNonNull(query);
		this.definition = java.util.Objects.requireNonNull(definition);
		this.target = java.util.Objects.requireNonNull(target);
		this.evaluationSession = java.util.Objects.requireNonNull(evaluationSession);
		this.instrumentation = java.util.Objects.requireNonNull(instrumentation);
	}

	public LocusIntersectionQuery2D getQuery() {
		return query;
	}

	public LocusDefinition2D getDefinition() {
		return definition;
	}

	public LocusIntersectionTarget2D getTarget() {
		return target;
	}

	public LocusIntersectionInstrumentation2D getInstrumentation() {
		return instrumentation;
	}

	/**
	 * Returns semantic interior partition boundaries supplied by the immutable
	 * evaluator. Render samples never participate in this partition.
	 *
	 * @return immutable semantic breakpoint list
	 */
	public List<Double> getInteriorBreakpoints(String branchKey, double lower,
			double upper) {
		if (definition.getEvaluatorCapability()
				instanceof LocusParameterPartition2D) {
			return ((LocusParameterPartition2D) definition
					.getEvaluatorCapability()).getInteriorBreakpoints(branchKey,
						lower, upper);
		}
		return Collections.emptyList();
	}

	/**
	 * Evaluates one semantic point while charging the query budget.
	 *
	 * @return current semantic evaluation
	 */
	public LocusEvaluation2D evaluate(String branchKey, double parameter) {
		instrumentation.recordSemanticEvaluation();
		return definition.evaluate(branchKey, parameter, evaluationSession);
	}

	/**
	 * Moves a sampling parameter next to an excluded end of the lifted interval of an arc
	 * component onto that component's own side of the end.
	 *
	 * <p>An excluded end of an arc shares its canonical parameter with the other end, and
	 * the periodic canonicalization of the provider can round the lifted neighbour of the
	 * excluded end onto that shared parameter. Its evaluation would then be the image of
	 * the other end of the component, which is not a point of this neighbourhood and must
	 * never be read as a sample, a bracket end or a root of it
	 * ({@code locus-v2-existence-components.md} section 15.3). The parameter is moved
	 * inward until its canonical form has the arc coordinate of its lifted position.</p>
	 *
	 * <p>No contract bounds how far the canonicalization rounds: the rounding has the
	 * magnitude of the canonical domain, not of the parameter. When no parameter on the
	 * component's own side is found, none is returned: the given parameter is never handed
	 * back as a sample whose side is not established.</p>
	 *
	 * @param componentExtent lifted interval of a certified component of the branch
	 * @param parameter the lifted parameter next to the excluded end
	 * @param upperEnd whether the excluded end is the upper end of the lifted interval
	 * @return the nearest sampled lifted parameter that is evaluated on the component's own
	 *         side; empty when no such parameter is established, or the extent is unknown
	 */
	public OptionalDouble insideExcludedEnd(String branchKey, LocusInterval2D componentExtent,
			double parameter, boolean upperEnd) {
		return insideExcludedEnd(definition, branchKey, componentExtent, parameter, upperEnd);
	}

	/** The same question for a definition that a pair query evaluates. */
	static OptionalDouble insideExcludedEnd(LocusDefinition2D definition, String branchKey,
			LocusInterval2D componentExtent, double parameter, boolean upperEnd) {
		LocusBranch2D branch = definition.getBranch(branchKey);
		if (branch == null) {
			return OptionalDouble.empty();
		}
		for (LocusExistenceStructure2D.ContinuousComponent component
				: branch.getExistenceStructure().getContinuousValidComponents()) {
			if (component.getInterval().equals(componentExtent)) {
				return ownSideOfExcludedEnd(definition.getProvider(), component, parameter,
						upperEnd);
			}
		}
		return OptionalDouble.empty();
	}

	/**
	 * The sampling parameter next to an excluded end of a certified component, on the
	 * component's own side of that end.
	 *
	 * <p>A parameter is on the component's own side when the arc coordinate of its canonical
	 * form is the coordinate of its lifted position: the two differ by the rounding of the
	 * canonicalization on the same side, and by the whole length when the canonical form is
	 * the parameter that the two ends of a full-length open arc share. The search doubles
	 * the distance to the end, from the given parameter up to half the length of the
	 * component, where that comparison stops separating the two sides. A certified closed
	 * periodic component has no excluded end of its own, and an unbounded end shares no
	 * parameter: both keep the given parameter.</p>
	 *
	 * @param component the certified component
	 * @param parameter the lifted parameter next to the excluded end
	 * @param upperEnd whether the excluded end is the upper end of the lifted interval
	 * @return the parameter, or empty when the search finds none on the own side
	 */
	static OptionalDouble ownSideOfExcludedEnd(LocusDriverDomainProvider2D provider,
			LocusExistenceStructure2D.ContinuousComponent component, double parameter,
			boolean upperEnd) {
		LocusInterval2D extent = component.getInterval();
		double end = upperEnd ? extent.getUpper() : extent.getLower();
		if (component.isClosedPeriodic() || !Double.isFinite(end)) {
			return OptionalDouble.of(parameter);
		}
		double length = component.getLength();
		double offset = Math.abs((upperEnd ? Math.nextDown(end) : Math.nextUp(end)) - end);
		if (Math.abs(parameter - end) > offset) {
			offset = Math.abs(parameter - end);
		}
		for (; offset <= length / 2 && Double.isFinite(offset); offset *= 2) {
			double candidate = upperEnd ? end - offset : end + offset;
			double coordinate = component.getArcCoordinate(provider.canonicalize(candidate));
			if (!Double.isNaN(coordinate) && Math.abs(coordinate
					- (candidate - extent.getLower())) <= length / 2) {
				return OptionalDouble.of(candidate);
			}
		}
		return OptionalDouble.empty();
	}

	/**
	 * Evaluates one captured target residual while charging the query budget.
	 *
	 * @return normalized target residual evidence
	 */
	public TargetResidual2D evaluateResidual(LocusPoint2D point) {
		instrumentation.recordTargetEvaluation();
		return target.evaluateResidual(point);
	}

	/** @return typed candidate-level target state, never verification evidence */
	public TargetCandidateEvaluation2D evaluateCandidateLevel(
			LocusPoint2D point) {
		instrumentation.recordTargetEvaluation();
		TargetCandidateEvaluation2D evaluation =
				target.evaluateCandidateLevel(point);
		if (!evaluation.isEstablished()) {
			instrumentation.recordInvalidTargetEvaluation();
		}
		return evaluation;
	}

	/** @return independently recomputed typed target residual evidence */
	public TargetResidualEvaluation2D evaluateResidualEvidence(
			LocusPoint2D point) {
		instrumentation.recordTargetEvaluation();
		TargetResidualEvaluation2D evaluation =
				target.evaluateResidualEvidence(point);
		if (!evaluation.isEstablished()) {
			instrumentation.recordInvalidTargetEvaluation();
		}
		return evaluation;
	}

	/** @return captured target-domain/membership evidence */
	public TargetMembership2D evaluateMembership(LocusPoint2D point,
			double coordinateTolerance) {
		instrumentation.recordTargetDomainEvaluation();
		instrumentation.recordMembershipCheck();
		return target.evaluateMembership(point, coordinateTolerance);
	}

	/** @return normalized target contact evidence */
	public TargetContactEvidence2D evaluateContact(LocusPoint2D point,
			LocusDifferentialEvaluation2D differential) {
		instrumentation.recordTargetDerivativeEvaluation();
		return target.evaluateContact(point, differential);
	}

	/**
	 * Estimates a source derivative inside one semantic component.
	 *
	 * <p>The result is query-local estimated evidence. It neither strengthens
	 * the G6 evaluator contract nor establishes global completeness.</p>
	 *
	 * @return finite derivative evidence, or explicit unknown regularity
	 */
	public LocusDifferentialEvaluation2D evaluateDifferential(String branchKey,
			double parameter, LocusInterval2D component) {
		instrumentation.recordDerivativeEvaluation();
		if (definition.getEvaluatorCapability()
				instanceof PiecewisePolynomialLocus2D) {
			PiecewisePolynomialLocus2D polynomial =
					(PiecewisePolynomialLocus2D) definition
							.getEvaluatorCapability();
			if (polynomial.supportsPiecewisePolynomial(definition)) {
				try {
					LocusPoint2D derivative = polynomial
							.evaluatePolynomialDerivative(branchKey, parameter);
					double speed = Math.hypot(derivative.getX(),
							derivative.getY());
					if (Double.isFinite(speed)) {
						return new LocusDifferentialEvaluation2D(derivative,
								speed == 0 ? Regularity.SINGULAR
										: Regularity.REGULAR,
								NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
								"semantic piecewise-polynomial derivative");
					}
				} catch (IllegalArgumentException exception) {
					// Fall through to bounded semantic finite differences.
				}
			}
		}
		double span = component.getUpper() - component.getLower();
		// An arc lifted past the canonical domain addresses its canonical parameters
		// after the seam in its lifted range (PRE-G9B-CU2).
		LocusInterval2D domain = definition.getProvider().getDeclaredDomain();
		if (definition.getProvider().isPeriodic() && parameter < component.getLower()
				&& component.getUpper() > domain.getUpper()) {
			parameter += domain.getUpper() - domain.getLower();
		}
		double rootTolerance = query.getPolicy().getRootParameterTolerance()
				.getValue();
		double step = Math.max(16 * rootTolerance,
				Math.max(Math.ulp(parameter) * 32, span * 1E-6));
		double lower = Math.max(component.getLower(), parameter - step);
		double upper = Math.min(component.getUpper(), parameter + step);
		// The neighbour of an excluded end is read on the component's own side, or not at all.
		if (!component.isLowerClosed() && lower == component.getLower()) {
			OptionalDouble inside = insideExcludedEnd(branchKey, component,
					Math.nextUp(lower), false);
			if (inside.isEmpty()) {
				return unknownDifferential("No finite-difference sample is established on "
						+ "the component's side of its excluded end");
			}
			lower = inside.getAsDouble();
		}
		if (!component.isUpperClosed() && upper == component.getUpper()) {
			OptionalDouble inside = insideExcludedEnd(branchKey, component,
					Math.nextDown(upper), true);
			if (inside.isEmpty()) {
				return unknownDifferential("No finite-difference sample is established on "
						+ "the component's side of its excluded end");
			}
			upper = inside.getAsDouble();
		}
		if (!(lower < upper)) {
			return unknownDifferential("Semantic component has no finite difference span");
		}
		LocusEvaluation2D first = evaluate(branchKey, lower);
		LocusEvaluation2D second = evaluate(branchKey, upper);
		if (!first.isValid() || !second.isValid()
				|| first.getPoint() == null || second.getPoint() == null) {
			return unknownDifferential(
					"Semantic finite-difference evaluations are invalid");
		}
		double inverseWidth = 1 / (upper - lower);
		double derivativeX = (second.getPoint().getX()
				- first.getPoint().getX()) * inverseWidth;
		double derivativeY = (second.getPoint().getY()
				- first.getPoint().getY()) * inverseWidth;
		double speed = Math.hypot(derivativeX, derivativeY);
		if (!Double.isFinite(speed) || speed == 0) {
			return unknownDifferential(
					"Estimated source derivative is nonfinite or singular");
		}
		return new LocusDifferentialEvaluation2D(
				new LocusPoint2D(derivativeX, derivativeY), Regularity.REGULAR,
				NumericGuarantee.ESTIMATED_ERROR,
				"query-local symmetric/one-sided semantic finite difference");
	}

	/** @return deterministic keys for every current valid-domain component */
	public List<String> getAllComponentKeys() {
		ArrayList<String> keys = new ArrayList<>();
		for (LocusBranch2D branch : definition.getBranches()) {
			List<LocusInterval2D> components =
					branch.getCertifiedContinuousValidComponents();
			for (int index = 0; index < components.size(); index++) {
				keys.add(componentKey(branch.getBranchKey(), index));
			}
		}
		return Collections.unmodifiableList(keys);
	}

	/** @return deterministic revision-scoped component key */
	public static String componentKey(String branchKey, int componentIndex) {
		return branchKey + "/component-" + componentIndex;
	}

	private static LocusDifferentialEvaluation2D unknownDifferential(
			String diagnostic) {
		return new LocusDifferentialEvaluation2D(new LocusPoint2D(0, 0),
				Regularity.UNKNOWN, NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				diagnostic);
	}
}
