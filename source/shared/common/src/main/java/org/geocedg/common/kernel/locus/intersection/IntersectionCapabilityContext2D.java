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
}
