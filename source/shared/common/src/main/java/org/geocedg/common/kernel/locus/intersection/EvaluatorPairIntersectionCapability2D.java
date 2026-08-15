/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapRelationKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionContext2D.ComponentAddress;

/**
 * Conservative semantic evaluator fallback for bounded component products.
 *
 * <p>Its boxes are non-authoritative candidate isolation only. It never claims
 * global completeness, certified local isolation or established overlap.</p>
 */
public final class EvaluatorPairIntersectionCapability2D
		implements LocusPairIntersectionCapability2D {
	private static final int SUBDIVISIONS = 32;
	private static final int MAXIMUM_REFINEMENT_ITERATIONS = 24;

	@Override
	public String getCapabilityId() {
		return "g8c2-evaluator-parameter-boxes/v1";
	}

	@Override
	public boolean supports(LocusPairIntersectionContext2D context) {
		return finiteComponents(context.getFirstComponents())
				&& finiteComponents(context.getSecondComponents());
	}

	@Override
	public LocusPairIntersectionCandidateSet2D isolate(
			LocusPairIntersectionContext2D context) {
		ArrayList<LocusPairIntersectionCandidate2D> candidates =
				new ArrayList<>();
		ArrayList<IntersectionOverlapEvidence2D> overlap = new ArrayList<>();
		ArrayList<IntersectionDiagnostic2D> diagnostics = new ArrayList<>();
		ArrayList<String> covered = new ArrayList<>();
		for (ComponentAddress first : context.getFirstComponents()) {
			SampledComponent firstSamples = sample(context, true, first);
			for (ComponentAddress second : context.getSecondComponents()) {
				String pairKey = LocusPairIntersectionContext2D.componentPairKey(
						first.getBranchKey(), first.getComponentKey(),
						second.getBranchKey(), second.getComponentKey());
				covered.add(pairKey);
				SampledComponent secondSamples = sample(context, false, second);
				OverlapRelationKind suspicion = suspectedOverlap(context,
						firstSamples, secondSamples);
				if (suspicion != null) {
					overlap.add(new IntersectionOverlapEvidence2D(
							first.getBranchKey(), first.getComponentKey(),
							second.getBranchKey(), second.getComponentKey(),
							OverlapStatus.OVERLAP_SUSPECTED_NOT_ESTABLISHED,
							suspicion, getCapabilityId(),
							"No semantic parameter map is available",
							NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
							"Matching semantic evaluations only suspect overlap"));
					continue;
				}
				isolateComponentPair(context, first, second, firstSamples,
						secondSamples, candidates);
			}
		}
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.PAIR_COVERAGE_NOT_ESTABLISHED,
				"Finite semantic parameter boxes were inspected, but evaluator-only "
						+ "sampling supplies no exhaustive coverage proof"));
		if (!overlap.isEmpty()) {
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.OVERLAP_SUSPECTED,
					"Sample agreement was retained only as suspected overlap"));
		}
		GeometryKind kind = !overlap.isEmpty() && !candidates.isEmpty()
				? GeometryKind.MIXED_FINITE_OVERLAP
				: !overlap.isEmpty() ? GeometryKind.UNSUPPORTED_OVERLAP
						: !candidates.isEmpty() ? GeometryKind.FINITE
								: GeometryKind.UNRESOLVED;
		return new LocusPairIntersectionCandidateSet2D(
				Completeness.NOT_ESTABLISHED,
				CompletenessMethod.NOT_ESTABLISHED, kind,
				SupportLevel.VERIFIED_UNCERTIFIED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, covered,
				candidates, overlap, diagnostics);
	}

	private static void isolateComponentPair(
			LocusPairIntersectionContext2D context, ComponentAddress first,
			ComponentAddress second, SampledComponent firstSamples,
			SampledComponent secondSamples,
			List<LocusPairIntersectionCandidate2D> candidates) {
		for (int firstIndex = 0; firstIndex < SUBDIVISIONS; firstIndex++) {
			for (int secondIndex = 0;
					secondIndex < SUBDIVISIONS; secondIndex++) {
				context.getInstrumentation().recordParameterBox(1);
				Segment firstSegment = firstSamples.segment(firstIndex);
				Segment secondSegment = secondSamples.segment(secondIndex);
				if (!firstSegment.overlaps(secondSegment,
						context.getQuery().getPolicy().getCoordinateTolerance()
								.getValue())) {
					context.getInstrumentation().recordRejectedBox();
					continue;
				}
				context.getInstrumentation().recordCandidateBox();
				RefinedPair root = refine(context, first, second, firstSegment,
						secondSegment);
				if (root != null) {
					candidates.add(candidate(context, first, second, firstIndex,
							secondIndex, firstSegment, secondSegment, root));
				}
			}
		}
	}

	private static LocusPairIntersectionCandidate2D candidate(
			LocusPairIntersectionContext2D context, ComponentAddress first,
			ComponentAddress second, int firstIndex, int secondIndex,
			Segment firstSegment, Segment secondSegment, RefinedPair root) {
		ContactClass contact = ContactClass.CONTACT_UNDETERMINED;
		LocusDifferentialEvaluation2D firstDifferential =
				context.evaluateFirstDifferential(first.getBranchKey(), root.first,
						first.getInterval());
		LocusDifferentialEvaluation2D secondDifferential =
				context.evaluateSecondDifferential(second.getBranchKey(), root.second,
						second.getInterval());
		if (firstDifferential.getRegularity() == Regularity.REGULAR
				&& secondDifferential.getRegularity() == Regularity.REGULAR) {
			context.getInstrumentation().recordJacobianEvaluation();
			double determinant = normalizedDeterminant(
					firstDifferential.getDerivative(),
					secondDifferential.getDerivative());
			if (Math.abs(determinant) > context.getQuery().getPolicy()
					.getTangencyTolerance().getThreshold()) {
				contact = ContactClass.TRANSVERSE_ESTABLISHED;
			}
		}
		String lineage = LocusPairIntersectionContext2D.componentPairKey(
				first.getBranchKey(), first.getComponentKey(),
				second.getBranchKey(), second.getComponentKey())
				+ "/candidate-box-" + firstIndex + "-" + secondIndex;
		return new LocusPairIntersectionCandidate2D(first.getBranchKey(),
				first.getComponentKey(), root.first, OptionalDouble.empty(),
				firstSegment.parameterInterval(), second.getBranchKey(),
				second.getComponentKey(), root.second, OptionalDouble.empty(),
				secondSegment.parameterInterval(),
				LocalPairIsolationEvidence2D.notEstablished(
						"Residual and safeguarded convergence do not prove uniqueness"),
				lineage, Optional.empty(), contact,
				MultiplicityStatus.NOT_ESTABLISHED, OptionalInt.empty(),
				SolverMethod.EVALUATOR_PARAMETER_BOXES,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				LineageEventKind.APPEARED, Collections.emptyList(),
				Collections.emptyList());
	}

	private static RefinedPair refine(LocusPairIntersectionContext2D context,
			ComponentAddress first, ComponentAddress second,
			Segment firstSegment, Segment secondSegment) {
		context.getInstrumentation().recordPairRefinementStarted();
		double firstParameter = firstSegment.midParameter();
		double secondParameter = secondSegment.midParameter();
		for (int iteration = 1;
				iteration <= MAXIMUM_REFINEMENT_ITERATIONS; iteration++) {
			context.getInstrumentation().recordPairRefinementIteration(iteration);
			LocusEvaluation2D firstValue = context.evaluateFirst(
					first.getBranchKey(), firstParameter);
			LocusEvaluation2D secondValue = context.evaluateSecond(
					second.getBranchKey(), secondParameter);
			if (!valid(firstValue) || !valid(secondValue)) {
				return null;
			}
			double dx = firstValue.getPoint().getX()
					- secondValue.getPoint().getX();
			double dy = firstValue.getPoint().getY()
					- secondValue.getPoint().getY();
			double scale = characteristicScale(firstValue.getPoint(),
					secondValue.getPoint());
			double threshold = context.getQuery().getPolicy()
					.getResidualTolerance().threshold(scale);
			if (Math.hypot(dx, dy) <= threshold) {
				return new RefinedPair(firstParameter, secondParameter);
			}
			LocusDifferentialEvaluation2D firstDifferential =
					context.evaluateFirstDifferential(first.getBranchKey(),
							firstParameter, first.getInterval());
			LocusDifferentialEvaluation2D secondDifferential =
					context.evaluateSecondDifferential(second.getBranchKey(),
							secondParameter, second.getInterval());
			if (firstDifferential.getRegularity() != Regularity.REGULAR
					|| secondDifferential.getRegularity() != Regularity.REGULAR) {
				return null;
			}
			context.getInstrumentation().recordJacobianEvaluation();
			LocusPoint2D firstDerivative = firstDifferential.getDerivative();
			LocusPoint2D secondDerivative = secondDifferential.getDerivative();
			double a = firstDerivative.getX();
			double b = -secondDerivative.getX();
			double c = firstDerivative.getY();
			double d = -secondDerivative.getY();
			double determinant = a * d - b * c;
			if (!Double.isFinite(determinant) || determinant == 0) {
				return null;
			}
			double deltaFirst = (-dx * d + b * dy) / determinant;
			double deltaSecond = (-a * dy + dx * c) / determinant;
			firstParameter = firstSegment.safeguard(firstParameter + deltaFirst);
			secondParameter = secondSegment.safeguard(
					secondParameter + deltaSecond);
		}
		return null;
	}

	private static SampledComponent sample(
			LocusPairIntersectionContext2D context, boolean first,
			ComponentAddress address) {
		ArrayList<Double> parameters = new ArrayList<>();
		ArrayList<LocusPoint2D> points = new ArrayList<>();
		for (int index = 0; index <= SUBDIVISIONS; index++) {
			double parameter = parameter(address.getInterval(), index,
					SUBDIVISIONS);
			LocusEvaluation2D evaluation = first
					? context.evaluateFirst(address.getBranchKey(), parameter)
					: context.evaluateSecond(address.getBranchKey(), parameter);
			if (!valid(evaluation)) {
				throw new IllegalArgumentException(
						"Pair component contains an invalid semantic evaluation");
			}
			parameters.add(parameter);
			points.add(evaluation.getPoint());
		}
		return new SampledComponent(parameters, points);
	}

	private static OverlapRelationKind suspectedOverlap(
			LocusPairIntersectionContext2D context, SampledComponent first,
			SampledComponent second) {
		context.getInstrumentation().recordOverlapCheck();
		boolean direct = true;
		boolean reverse = true;
		for (int index = 0; index <= SUBDIVISIONS; index += 4) {
			direct &= close(context, first.point(index), second.point(index));
			reverse &= close(context, first.point(index),
					second.point(SUBDIVISIONS - index));
		}
		if (direct) {
			return OverlapRelationKind.FULL_COMPONENT;
		}
		if (reverse) {
			return OverlapRelationKind.REVERSE_PARAMETERIZATION;
		}
		return null;
	}

	private static boolean close(LocusPairIntersectionContext2D context,
			LocusPoint2D first, LocusPoint2D second) {
		double threshold = context.getQuery().getPolicy().getResidualTolerance()
				.threshold(characteristicScale(first, second));
		return Math.hypot(first.getX() - second.getX(),
				first.getY() - second.getY()) <= threshold;
	}

	private static double normalizedDeterminant(LocusPoint2D first,
			LocusPoint2D second) {
		double denominator = Math.hypot(first.getX(), first.getY())
				* Math.hypot(second.getX(), second.getY());
		return (first.getX() * second.getY()
				- first.getY() * second.getX()) / denominator;
	}

	private static double characteristicScale(LocusPoint2D first,
			LocusPoint2D second) {
		return Math.max(1, Math.max(Math.hypot(first.getX(), first.getY()),
				Math.hypot(second.getX(), second.getY())));
	}

	private static double parameter(LocusInterval2D interval, int index,
			int subdivisions) {
		double value = interval.getLower()
				+ (interval.getUpper() - interval.getLower()) * index
						/ subdivisions;
		if (index == 0 && !interval.isLowerClosed()) {
			return Math.nextUp(value);
		}
		if (index == subdivisions && !interval.isUpperClosed()) {
			return Math.nextDown(value);
		}
		return value;
	}

	private static boolean finiteComponents(List<ComponentAddress> components) {
		for (ComponentAddress component : components) {
			if (!Double.isFinite(component.getInterval().getLower())
					|| !Double.isFinite(component.getInterval().getUpper())) {
				return false;
			}
		}
		return true;
	}

	private static boolean valid(LocusEvaluation2D evaluation) {
		return evaluation.isValid() && evaluation.getPoint() != null
				&& Double.isFinite(evaluation.getPoint().getX())
				&& Double.isFinite(evaluation.getPoint().getY());
	}

	private static final class SampledComponent {
		private final List<Double> parameters;
		private final List<LocusPoint2D> points;

		SampledComponent(List<Double> parameters, List<LocusPoint2D> points) {
			this.parameters = parameters;
			this.points = points;
		}

		Segment segment(int index) {
			return new Segment(parameters.get(index), parameters.get(index + 1),
					points.get(index), points.get(index + 1));
		}

		LocusPoint2D point(int index) {
			return points.get(index);
		}
	}

	private static final class Segment {
		private final double lowerParameter;
		private final double upperParameter;
		private final LocusPoint2D first;
		private final LocusPoint2D second;

		Segment(double lowerParameter, double upperParameter,
				LocusPoint2D first, LocusPoint2D second) {
			this.lowerParameter = Math.min(lowerParameter, upperParameter);
			this.upperParameter = Math.max(lowerParameter, upperParameter);
			this.first = first;
			this.second = second;
		}

		boolean overlaps(Segment other, double tolerance) {
			return minimumX() <= other.maximumX() + tolerance
					&& other.minimumX() <= maximumX() + tolerance
					&& minimumY() <= other.maximumY() + tolerance
					&& other.minimumY() <= maximumY() + tolerance;
		}

		double midParameter() {
			return 0.5 * (lowerParameter + upperParameter);
		}

		double safeguard(double value) {
			return Math.max(lowerParameter, Math.min(upperParameter, value));
		}

		IntersectionParameterInterval2D parameterInterval() {
			return new IntersectionParameterInterval2D(lowerParameter,
					upperParameter);
		}

		private double minimumX() {
			return Math.min(first.getX(), second.getX());
		}

		private double maximumX() {
			return Math.max(first.getX(), second.getX());
		}

		private double minimumY() {
			return Math.min(first.getY(), second.getY());
		}

		private double maximumY() {
			return Math.max(first.getY(), second.getY());
		}
	}

	private static final class RefinedPair {
		private final double first;
		private final double second;

		RefinedPair(double first, double second) {
			this.first = first;
			this.second = second;
		}
	}
}
