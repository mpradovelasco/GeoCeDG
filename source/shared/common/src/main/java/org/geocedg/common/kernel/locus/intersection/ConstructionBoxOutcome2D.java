/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.ConstructionValidityPredicate2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;

/**
 * Typed outcome of one class-v3 program evaluation on a parameter box
 * ({@code locus-v2-certified-construction-model.md} section 10.4). Only
 * {@link Kind#UNIFORMLY_INVALID} may produce certified nonexistence; an undecided or
 * tolerance-dependent box is {@link Kind#UNRESOLVED} with a typed reason and is never
 * excluded or certified.
 */
public final class ConstructionBoxOutcome2D {
	/** Outcome of the box. */
	public enum Kind {
		/** Every validity predicate of the program is certified true on the box. */
		VALID,
		/** A validity predicate is certified false on the whole box. */
		UNIFORMLY_INVALID,
		/** The box is not decided. */
		UNRESOLVED
	}

	/** Reason of an unresolved box. */
	public enum Unresolved {
		/** The enclosure of a validity predicate contains its threshold. */
		PREDICATE,
		/**
		 * An inherited tolerance that restricts definedness inside the exact validity
		 * set may apply (line and point definedness, circle type, normalization branch).
		 */
		TOLERANCE,
		/** An unlabelled second output may be suppressed at coincident roots. */
		UNLABELLED_COINCIDENT_OUTPUT,
		/** An enclosure is not finite or a divisor enclosure contains zero. */
		ENCLOSURE,
		/** The value of a structural metric input is needed; it is never evidence. */
		METRIC_VALUE,
		/** The box is outside the driver domain. */
		OUTSIDE_DOMAIN
	}

	/** Decision of one evaluated validity predicate. */
	public enum Decision {
		TRUE, FALSE, UNDECIDED, TOLERANCE
	}

	/** One validity predicate evaluated on the box. */
	public static final class PredicateEvaluation {
		final int step;
		final ConstructionValidityPredicate2D predicate;
		final int root;
		final SplineOutwardInterval2D value;
		final SplineOutwardInterval2D derivative;
		final Decision decision;

		PredicateEvaluation(int step, ConstructionValidityPredicate2D predicate, int root,
				SplineOutwardInterval2D value, SplineOutwardInterval2D derivative,
				Decision decision) {
			this.step = step;
			this.predicate = predicate;
			this.root = root;
			this.value = value;
			this.derivative = derivative;
			this.decision = decision;
		}

		/** @return node of the evaluated step, {@code 1} for the first step */
		public int getStep() {
			return step;
		}

		/** @return evaluated predicate */
		public ConstructionValidityPredicate2D getPredicate() {
			return predicate;
		}

		/** @return raw root of an intersection the predicate concerns, or {@code -1} */
		public int getRoot() {
			return root;
		}

		/** @return decision of the predicate on the box */
		public Decision getDecision() {
			return decision;
		}

		/** @return lower bound of the enclosure of the predicate value */
		public double getValueLower() {
			return value.lower;
		}

		/** @return upper bound of the enclosure of the predicate value */
		public double getValueUpper() {
			return value.upper;
		}

		@Override
		public String toString() {
			return "step" + step + ":" + predicate + (root < 0 ? "" : ":root" + root)
					+ ":" + decision;
		}
	}

	final Kind kind;
	final Unresolved unresolved;
	/** Node of the deciding step, {@code 1} for the first step; {@code -1} when none. */
	final int step;
	final ConstructionValidityPredicate2D predicate;
	/** Index of the deciding predicate within its step (box model addressing). */
	final int index;
	final String detail;
	/**
	 * Enclosure of the output point and of its derivative, for a valid box; {@code null}
	 * when an assumed predicate leaves the value unbounded, or the derivative unevaluated.
	 */
	final SplineOutwardInterval2D[] value;
	final SplineOutwardInterval2D[] derivative;
	/** Raw root selected by each intersection output, for a valid box. */
	final String selection;
	final List<PredicateEvaluation> evaluations;

	private ConstructionBoxOutcome2D(Kind kind, Unresolved unresolved, int step,
			ConstructionValidityPredicate2D predicate, int index, String detail,
			SplineOutwardInterval2D[] value, SplineOutwardInterval2D[] derivative,
			String selection, List<PredicateEvaluation> evaluations) {
		this.kind = kind;
		this.unresolved = unresolved;
		this.step = step;
		this.predicate = predicate;
		this.index = index;
		this.detail = detail;
		this.value = value;
		this.derivative = derivative;
		this.selection = selection;
		this.evaluations = Collections.unmodifiableList(evaluations);
	}

	static ConstructionBoxOutcome2D valid(SplineOutwardInterval2D[] value,
			SplineOutwardInterval2D[] derivative, String selection,
			List<PredicateEvaluation> evaluations) {
		return new ConstructionBoxOutcome2D(Kind.VALID, null, -1, null, -1, "", value,
				derivative, selection, evaluations);
	}

	static ConstructionBoxOutcome2D invalid(int step,
			ConstructionValidityPredicate2D predicate, int index, String detail,
			List<PredicateEvaluation> evaluations) {
		return new ConstructionBoxOutcome2D(Kind.UNIFORMLY_INVALID, null, step, predicate,
				index, detail, null, null, "", evaluations);
	}

	static ConstructionBoxOutcome2D unresolved(Unresolved reason, int step,
			ConstructionValidityPredicate2D predicate, String detail,
			List<PredicateEvaluation> evaluations) {
		return unresolved(reason, step, predicate, -1, detail, evaluations);
	}

	static ConstructionBoxOutcome2D unresolved(Unresolved reason, int step,
			ConstructionValidityPredicate2D predicate, int index, String detail,
			List<PredicateEvaluation> evaluations) {
		return new ConstructionBoxOutcome2D(Kind.UNRESOLVED, reason, step, predicate,
				index, detail, null, null, "", evaluations);
	}

	/** @return target of the deciding predicate, or {@code null} for a valid box */
	ConstructionBoxModel2D.Target getTarget() {
		return predicate == null ? null
				: new ConstructionBoxModel2D.Target(step, predicate, index);
	}

	/** @return outcome of the box */
	public Kind getKind() {
		return kind;
	}

	/** @return reason of an unresolved box, otherwise {@code null} */
	public Unresolved getUnresolved() {
		return unresolved;
	}

	/** @return node of the deciding step, or {@code -1} */
	public int getStep() {
		return step;
	}

	/** @return predicate of the deciding step, or {@code null} */
	public ConstructionValidityPredicate2D getPredicate() {
		return predicate;
	}

	/** @return raw root selected by each intersection output of a valid box */
	public String getSelection() {
		return selection;
	}

	/** @return validity predicates evaluated on the box, in step order */
	public List<PredicateEvaluation> getEvaluations() {
		return evaluations;
	}

	/**
	 * @param axis 0 for x, 1 for y
	 * @return lower bound of the enclosure of the output coordinate on a valid box
	 */
	public double getValueLower(int axis) {
		return value[axis].lower;
	}

	/**
	 * @param axis 0 for x, 1 for y
	 * @return upper bound of the enclosure of the output coordinate on a valid box
	 */
	public double getValueUpper(int axis) {
		return value[axis].upper;
	}

	/** @return typed reason of a uniformly invalid box, as named by the contract */
	public String getInvalidReason() {
		return kind == Kind.UNIFORMLY_INVALID ? predicate.getInvalidReason() : null;
	}

	/**
	 * @return coverage cause this outcome justifies on the box
	 *         ({@code locus-v2-existence-components.md} section 15.5)
	 */
	public LocusCoverageCause2D getCoverageCause() {
		switch (kind) {
		case VALID:
			return LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE;
		case UNIFORMLY_INVALID:
			return LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE;
		default:
			switch (unresolved) {
			case TOLERANCE:
				return LocusCoverageCause2D.INHERITED_TOLERANCE_BEHAVIOUR;
			case UNLABELLED_COINCIDENT_OUTPUT:
				return LocusCoverageCause2D.UNLABELLED_COINCIDENT_OUTPUT;
			default:
				return LocusCoverageCause2D.NOT_CERTIFIED;
			}
		}
	}

	@Override
	public String toString() {
		return kind + (unresolved == null ? "" : ":" + unresolved)
				+ (step < 0 ? "" : ":step" + step) + (predicate == null ? "" : ":" + predicate)
				+ (detail.isEmpty() ? "" : ":" + detail);
	}
}
