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

/** Coherent query-local context for one canonical pair of semantic loci. */
public final class LocusPairIntersectionContext2D {
	private final LocusPairIntersectionQuery2D query;
	private final LocusDefinition2D firstDefinition;
	private final LocusDefinition2D secondDefinition;
	private final LocusEvaluationSession2D session;
	private final LocusPairIntersectionInstrumentation2D instrumentation;

	/** Creates one context over a single coherent two-source session. */
	public LocusPairIntersectionContext2D(LocusPairIntersectionQuery2D query,
			LocusDefinition2D firstDefinition,
			LocusDefinition2D secondDefinition,
			LocusEvaluationSession2D session,
			LocusPairIntersectionInstrumentation2D instrumentation) {
		this.query = java.util.Objects.requireNonNull(query);
		this.firstDefinition = java.util.Objects.requireNonNull(firstDefinition);
		this.secondDefinition = java.util.Objects.requireNonNull(secondDefinition);
		this.session = java.util.Objects.requireNonNull(session);
		this.instrumentation = java.util.Objects.requireNonNull(instrumentation);
	}

	/** @return immutable pair query */
	public LocusPairIntersectionQuery2D getQuery() {
		return query;
	}

	/** @return first definition in canonical source order */
	public LocusDefinition2D getFirstDefinition() {
		return firstDefinition;
	}

	/** @return second definition in canonical source order */
	public LocusDefinition2D getSecondDefinition() {
		return secondDefinition;
	}

	/** @return query-local bounded instrumentation */
	public LocusPairIntersectionInstrumentation2D getInstrumentation() {
		return instrumentation;
	}

	/** @return current first-source semantic evaluation */
	public LocusEvaluation2D evaluateFirst(String branchKey,
			double parameter) {
		instrumentation.recordSemanticEvaluation();
		return firstDefinition.evaluate(branchKey, parameter, session);
	}

	/** @return current second-source semantic evaluation */
	public LocusEvaluation2D evaluateSecond(String branchKey,
			double parameter) {
		instrumentation.recordSemanticEvaluation();
		return secondDefinition.evaluate(branchKey, parameter, session);
	}

	/** @return query-local finite-difference evidence on the first source */
	public LocusDifferentialEvaluation2D evaluateFirstDifferential(
			String branchKey, double parameter, LocusInterval2D component) {
		return differential(true, branchKey, parameter, component,
				query.getPolicy().getFirstRootTolerance().getValue());
	}

	/** @return query-local finite-difference evidence on the second source */
	public LocusDifferentialEvaluation2D evaluateSecondDifferential(
			String branchKey, double parameter, LocusInterval2D component) {
		return differential(false, branchKey, parameter, component,
				query.getPolicy().getSecondRootTolerance().getValue());
	}

	/** @return deterministic addresses for all valid first-source components */
	public List<ComponentAddress> getFirstComponents() {
		return components(firstDefinition);
	}

	/** @return deterministic addresses for all valid second-source components */
	public List<ComponentAddress> getSecondComponents() {
		return components(secondDefinition);
	}

	/** @return deterministic keys for the complete component product */
	public List<String> getAllComponentPairKeys() {
		ArrayList<String> result = new ArrayList<>();
		for (ComponentAddress first : getFirstComponents()) {
			for (ComponentAddress second : getSecondComponents()) {
				result.add(componentPairKey(first.getBranchKey(),
						first.getComponentKey(), second.getBranchKey(),
						second.getComponentKey()));
			}
		}
		return Collections.unmodifiableList(result);
	}

	/** @return canonical component-pair key independent of source order */
	public static String componentPairKey(String firstBranch,
			String firstComponent, String secondBranch,
			String secondComponent) {
		return LocusPairIdentity2D.componentPair(firstBranch, firstComponent,
				secondBranch, secondComponent);
	}

	private LocusDifferentialEvaluation2D differential(boolean first,
			String branchKey, double parameter, LocusInterval2D component,
			double rootTolerance) {
		instrumentation.recordDerivativeEvaluation();
		double span = component.getUpper() - component.getLower();
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
			return unknownDifferential(
					"Semantic component has no finite-difference span");
		}
		LocusEvaluation2D low = first ? evaluateFirst(branchKey, lower)
				: evaluateSecond(branchKey, lower);
		LocusEvaluation2D high = first ? evaluateFirst(branchKey, upper)
				: evaluateSecond(branchKey, upper);
		if (!valid(low) || !valid(high)) {
			return unknownDifferential(
					"Semantic finite-difference evaluations are invalid");
		}
		double inverseWidth = 1 / (upper - lower);
		double dx = (high.getPoint().getX() - low.getPoint().getX())
				* inverseWidth;
		double dy = (high.getPoint().getY() - low.getPoint().getY())
				* inverseWidth;
		double speed = Math.hypot(dx, dy);
		if (!Double.isFinite(speed) || speed == 0) {
			return unknownDifferential(
					"Estimated pair-source derivative is singular");
		}
		return new LocusDifferentialEvaluation2D(new LocusPoint2D(dx, dy),
				Regularity.REGULAR, NumericGuarantee.ESTIMATED_ERROR,
				"query-local two-source finite difference");
	}

	private static List<ComponentAddress> components(
			LocusDefinition2D definition) {
		ArrayList<ComponentAddress> result = new ArrayList<>();
		for (LocusBranch2D branch : definition.getBranches()) {
			for (int index = 0;
					index < branch.getValidDomainComponents().size(); index++) {
				result.add(new ComponentAddress(branch.getBranchKey(),
						IntersectionCapabilityContext2D.componentKey(
								branch.getBranchKey(), index),
						branch.getValidDomainComponents().get(index)));
			}
		}
		return Collections.unmodifiableList(result);
	}

	private static boolean valid(LocusEvaluation2D evaluation) {
		return evaluation.isValid() && evaluation.getPoint() != null
				&& Double.isFinite(evaluation.getPoint().getX())
				&& Double.isFinite(evaluation.getPoint().getY());
	}

	private static LocusDifferentialEvaluation2D unknownDifferential(
			String diagnostic) {
		return new LocusDifferentialEvaluation2D(new LocusPoint2D(0, 0),
				Regularity.UNKNOWN,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, diagnostic);
	}

	/** Immutable revision-local address of one valid semantic component. */
	public static final class ComponentAddress {
		private final String branchKey;
		private final String componentKey;
		private final LocusInterval2D interval;

		ComponentAddress(String branchKey, String componentKey,
				LocusInterval2D interval) {
			this.branchKey = branchKey;
			this.componentKey = componentKey;
			this.interval = interval;
		}

		public String getBranchKey() {
			return branchKey;
		}

		public String getComponentKey() {
			return componentKey;
		}

		public LocusInterval2D getInterval() {
			return interval;
		}
	}
}
