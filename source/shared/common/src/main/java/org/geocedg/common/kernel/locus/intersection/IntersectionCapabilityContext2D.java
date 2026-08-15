/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;

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
	 * Evaluates one semantic point while charging the query budget.
	 *
	 * @return current semantic evaluation
	 */
	public LocusEvaluation2D evaluate(String branchKey, double parameter) {
		instrumentation.recordSemanticEvaluation();
		return definition.evaluate(branchKey, parameter, evaluationSession);
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
		double span = component.getUpper() - component.getLower();
		double rootTolerance = query.getPolicy().getRootParameterTolerance()
				.getValue();
		double step = Math.max(16 * rootTolerance,
				Math.max(Math.ulp(parameter) * 32, span * 1E-6));
		double lower = Math.max(component.getLower(), parameter - step);
		double upper = Math.min(component.getUpper(), parameter + step);
		if (!component.isLowerClosed() && lower == component.getLower()) {
			lower = Math.nextUp(lower);
		}
		if (!component.isUpperClosed() && upper == component.getUpper()) {
			upper = Math.nextDown(upper);
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
			List<LocusInterval2D> components = branch.getValidDomainComponents();
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
