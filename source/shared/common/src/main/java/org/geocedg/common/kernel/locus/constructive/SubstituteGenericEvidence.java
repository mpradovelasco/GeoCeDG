/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Decision;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Failure;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Incidence;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.IncidenceResult;
import org.geocedg.common.kernel.locus.constructive.ExactQuadraticTower.Element;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;

/**
 * The relaxed kind {@code SUBSTITUTE_GENERIC} version 1 ({@code
 * locus-v2-constructive-authority.md} section 7.1) for an incidence whose exact evaluation uses
 * declared boundary substitutes. The relation must hold exactly at the base point, the binary64
 * substitutes, and at every point of the deterministic perturbation set of the version, with the
 * same strict sign vector of every validity, branch and membership predicate, each evaluation
 * within {@code cu3-exact-evaluation-budget/v1}; the live algorithm is checked at the base only.
 * The evidence is {@code RELAXED}: positive computational evidence, never a proof of identity, with
 * every substitute named as an unestablished premise. It is recomputed at each revision and never
 * reused from another.
 */
public final class SubstituteGenericEvidence {

	/** Version of the kind. */
	public static final String VERSION = SubstituteGenericPerturbationSet.VERSION;

	/** Typed cause of an incidence that the kind leaves {@code NOT_ESTABLISHED}. */
	public enum Cause {
		/** The exact evaluation uses no substitute: it is rigorous by itself. */
		NO_SUBSTITUTE,
		/** A precondition fails at the base point: the evaluation decides nothing. */
		BASE_NOT_DECIDED,
		/**
		 * The relation does not hold for the substitutes: a refutation with a non-constructible
		 * premise is not rigorous for the ideal objects.
		 */
		REFUTED_FOR_THE_SUBSTITUTES,
		/** A predicate has a zero sign at the base point. */
		BASE_SIGN_NOT_STRICT,
		/** The generator cap of the version is exhausted before the set is complete. */
		PERTURBATION_GENERATOR_EXHAUSTED,
		/** The evaluation at a perturbed point decides nothing. */
		PERTURBATION_NOT_DECIDED,
		/** A perturbed point has another sign vector: two cells are never mixed. */
		PERTURBATION_LEFT_CELL,
		/** The relation does not hold at a perturbed point. */
		PERTURBATION_REFUTED,
		/** An exact evaluation exhausts its versioned budget. */
		BUDGET_EXHAUSTED
	}

	/** The evidence of one incidence. */
	public static final class Result {
		private final RelationIntegrityRecord record;
		private final Cause cause;
		private final String message;
		private final List<Integer> substitutes;
		private final int coordinates;
		private final int perturbations;
		private final List<ExactEvaluationCounters> counters;
		private final List<Decision> cell;
		private final IncidenceResult base;

		private Result(RelationIntegrityRecord record, Cause cause, String message,
				List<Integer> substitutes, int coordinates, int perturbations,
				List<ExactEvaluationCounters> counters, List<Decision> cell,
				IncidenceResult base) {
			this.record = record;
			this.cause = cause;
			this.message = message;
			this.substitutes = Collections.unmodifiableList(new ArrayList<>(substitutes));
			this.coordinates = coordinates;
			this.perturbations = perturbations;
			this.counters = Collections.unmodifiableList(new ArrayList<>(counters));
			this.cell = Collections.unmodifiableList(new ArrayList<>(cell));
			this.base = base;
		}

		/** @return the integrity record of the incidence */
		public RelationIntegrityRecord getRecord() {
			return record;
		}

		/** @return the typed cause of an incidence left {@code NOT_ESTABLISHED}, or {@code null} */
		public Cause getCause() {
			return cause;
		}

		/** @return a description of the cause, or {@code null} */
		public String getMessage() {
			return message;
		}

		/** @return the boundary substitute nodes, in cone order */
		public List<Integer> getSubstitutes() {
			return substitutes;
		}

		/** @return the number {@code m} of boundary coordinates */
		public int getCoordinates() {
			return coordinates;
		}

		/** @return the number {@code N} of perturbed points evaluated */
		public int getPerturbations() {
			return perturbations;
		}

		/** @return the counters of every exact evaluation, the base first */
		public List<ExactEvaluationCounters> getCounters() {
			return counters;
		}

		/** @return the sign vector of the base point, the operational cell */
		public List<Decision> getCell() {
			return cell;
		}

		/** @return the incidence decided at the base point */
		public IncidenceResult getBase() {
			return base;
		}

		@Override
		public String toString() {
			return record + (cause == null ? "" : " " + cause + ": " + message);
		}
	}

	private SubstituteGenericEvidence() {
	}

	/**
	 * @param relation stable description of the relation, for instance {@code P on r}
	 * @param cone the cone of the relation
	 * @param point node of the point
	 * @param target node of the target
	 * @param budget the budget of each exact evaluation
	 * @return the evidence
	 */
	public static Result incidence(String relation, ConstructiveCone cone, int point,
			int target, ExactEvaluationBudget budget) {
		Objects.requireNonNull(relation);
		ConstructiveConeEvaluator<Element> evaluator = ConstructiveConeEvaluator.exact(cone,
				budget);
		IncidenceResult base = evaluator.incidence(point, target);
		List<ExactEvaluationCounters> counters = new ArrayList<>();
		counters.add(evaluator.getExactCounters());
		List<Integer> substitutes = base.getSubstitutes();
		List<String> named = names(cone, substitutes);
		if (base.getIncidence() == Incidence.NOT_DECIDED) {
			Failure failure = base.getFailure().getFailure();
			return refuse(relation, failure == Failure.BUDGET_EXHAUSTED ? Cause.BUDGET_EXHAUSTED
					: Cause.BASE_NOT_DECIDED, String.valueOf(base.getFailure()), substitutes,
					named, 0, 0, counters, Collections.emptyList(), base);
		}
		if (substitutes.isEmpty()) {
			return refuse(relation, Cause.NO_SUBSTITUTE, "the exact evaluation is rigorous",
					substitutes, named, 0, 0, counters, Collections.emptyList(), base);
		}
		List<Decision> cell = evaluator.getDecisions();
		if (base.getIncidence() != Incidence.HOLDS) {
			return refuse(relation, Cause.REFUTED_FOR_THE_SUBSTITUTES, base.getIncidence().name(),
					substitutes, named, 0, 0, counters, cell, base);
		}
		for (Decision decision : cell) {
			if (decision.getSign() == 0) {
				return refuse(relation, Cause.BASE_SIGN_NOT_STRICT, decision.toString(),
						substitutes, named, 0, 0, counters, cell, base);
			}
		}
		List<ExactRational2D> coordinates = coordinates(cone, substitutes);
		SubstituteGenericPerturbationSet set = SubstituteGenericPerturbationSet.of(coordinates);
		if (set == null) {
			return refuse(relation, Cause.PERTURBATION_GENERATOR_EXHAUSTED,
					"cap of the generator outputs", substitutes, named, coordinates.size(), 0,
					counters, cell, base);
		}
		int evaluated = 0;
		for (List<ExactRational2D> perturbed : set.getPoints()) {
			ConstructiveConeEvaluator<Element> at = new ConstructiveConeEvaluator<>(cone,
					ConstructiveArithmetic.exact(new ExactQuadraticTower(budget)), false,
					overrides(cone, substitutes, perturbed));
			IncidenceResult result = at.incidence(point, target);
			counters.add(at.getExactCounters());
			evaluated++;
			List<Decision> signs = at.getDecisions();
			boolean decided = result.getIncidence() != Incidence.NOT_DECIDED;
			if (leftCell(cell, signs, decided)) {
				return refuse(relation, Cause.PERTURBATION_LEFT_CELL, "perturbation "
						+ evaluated + ": " + signs, substitutes, named, coordinates.size(),
						evaluated, counters, cell, base);
			}
			if (!decided) {
				Failure failure = result.getFailure().getFailure();
				return refuse(relation, failure == Failure.BUDGET_EXHAUSTED
						? Cause.BUDGET_EXHAUSTED : Cause.PERTURBATION_NOT_DECIDED,
						"perturbation " + evaluated + ": " + result.getFailure(), substitutes,
						named, coordinates.size(), evaluated, counters, cell, base);
			}
			if (result.getIncidence() != Incidence.HOLDS) {
				return refuse(relation, Cause.PERTURBATION_REFUTED, "perturbation " + evaluated
						+ ": " + result.getIncidence(), substitutes, named, coordinates.size(),
						evaluated, counters, cell, base);
			}
		}
		RelationIntegrityRecord record = RelationIntegrityRecord.derive(relation,
				RelationIntegrity.STRICT, RelationIntegrityRecord.Evidence.SUBSTITUTE_GENERIC,
				Collections.emptyList(), named, VERSION + "|"
						+ DocumentAuthorityRecord.RELAXED_ADMISSION + "|m=" + coordinates.size()
						+ "|N=" + evaluated);
		return new Result(record, null, null, substitutes, coordinates.size(), evaluated,
				counters, cell, base);
	}

	/**
	 * The rule of version 1 on a relation given directly as a predicate of the boundary
	 * coordinates: it holds at the base point and at every point of the perturbation set. It is
	 * the engine of {@link #incidence}, exposed for the tests of what the kind excludes and what
	 * it cannot exclude.
	 *
	 * @param base the boundary coordinates {@code z}
	 * @param relation whether the relation holds exactly at given coordinates
	 * @return whether it holds at the {@code N + 1} points; {@code false} when the generator cap
	 *         is exhausted
	 */
	static boolean holdsOnTheSet(List<ExactRational2D> base,
			java.util.function.Predicate<List<ExactRational2D>> relation) {
		if (!relation.test(base)) {
			return false;
		}
		SubstituteGenericPerturbationSet set = SubstituteGenericPerturbationSet.of(base);
		if (set == null) {
			return false;
		}
		for (List<ExactRational2D> point : set.getPoints()) {
			if (!relation.test(point)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * A perturbed point leaves the cell when one of its decided signs differs from the base, or
	 * when a complete evaluation decided other predicates; an evaluation that stopped on a
	 * failure with every sign so far equal to the base has not left it.
	 */
	private static boolean leftCell(List<Decision> cell, List<Decision> signs,
			boolean decided) {
		int common = Math.min(cell.size(), signs.size());
		for (int index = 0; index < common; index++) {
			if (!cell.get(index).equals(signs.get(index))) {
				return true;
			}
		}
		return decided ? cell.size() != signs.size() : signs.size() > cell.size();
	}

	private static Result refuse(String relation, Cause cause, String message,
			List<Integer> substitutes, List<String> named, int coordinates, int perturbations,
			List<ExactEvaluationCounters> counters, List<Decision> cell, IncidenceResult base) {
		RelationIntegrityRecord record = RelationIntegrityRecord.derive(relation,
				RelationIntegrity.NOT_ESTABLISHED, RelationIntegrityRecord.Evidence.NONE,
				Collections.emptyList(), named, VERSION + ":" + cause);
		return new Result(record, cause, message, substitutes, coordinates, perturbations,
				counters, cell, base);
	}

	/** @return the boundary coordinates {@code z}: two for a point, one for a number */
	private static List<ExactRational2D> coordinates(ConstructiveCone cone,
			List<Integer> substitutes) {
		List<ExactRational2D> coordinates = new ArrayList<>();
		for (int node : substitutes) {
			ConstructiveConeNode substitute = cone.getNode(node);
			if (substitute.getOperation().getShape() == ConstructiveOperation.Shape.POINT) {
				ExactRational2D z = ExactRational2D.ofBinary64(substitute.getLiveState(2));
				coordinates.add(ExactRational2D.ofBinary64(substitute.getLiveState(0)).divide(z));
				coordinates.add(ExactRational2D.ofBinary64(substitute.getLiveState(1)).divide(z));
			} else {
				coordinates.add(ExactRational2D.ofBinary64(substitute.getLiveState(0)));
			}
		}
		return coordinates;
	}

	private static Map<Integer, ExactRational2D[]> overrides(ConstructiveCone cone,
			List<Integer> substitutes, List<ExactRational2D> perturbed) {
		Map<Integer, ExactRational2D[]> overrides = new HashMap<>();
		int position = 0;
		for (int node : substitutes) {
			if (cone.getNode(node).getOperation().getShape()
					== ConstructiveOperation.Shape.POINT) {
				overrides.put(node, new ExactRational2D[] {perturbed.get(position),
						perturbed.get(position + 1)});
				position += 2;
			} else {
				overrides.put(node, new ExactRational2D[] {perturbed.get(position)});
				position++;
			}
		}
		return overrides;
	}

	private static List<String> names(ConstructiveCone cone, List<Integer> substitutes) {
		List<String> named = new ArrayList<>();
		for (int node : substitutes) {
			ConstructiveConeNode substitute = cone.getNode(node);
			named.add("boundary substitute " + (substitute.getPresentationName() == null
					? "n" + node : substitute.getPresentationName()) + " ("
					+ substitute.getCategory() + ": " + substitute.getReason() + ")");
		}
		return named;
	}
}
