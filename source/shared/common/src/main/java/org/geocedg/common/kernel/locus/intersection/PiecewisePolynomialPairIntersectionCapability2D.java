/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
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
 * Deterministic span-pair candidate isolation for two semantic polynomial loci.
 *
 * <p>Bernstein convex-hull rejection is applied to semantic polynomial spans,
 * never to render samples or a generic fixed grid. The resulting floating
 * boxes are candidate evidence only: without interval-rounded rectangle
 * coverage and uniqueness they deliberately remain rich-only and carry no
 * public continuation key.</p>
 */
final class PiecewisePolynomialPairIntersectionCapability2D
		implements LocusPairIntersectionCapability2D {
	private static final int MAXIMUM_COEFFICIENT_COUNT = 33;
	private static final double COEFFICIENT_COMPARISON_FACTOR = 512;

	@Override
	public String getCapabilityId() {
		return "g9s1-piecewise-polynomial-pair-boxes/v1";
	}

	@Override
	public boolean supports(LocusPairIntersectionContext2D context) {
		if (!(context.getFirstDefinition().getEvaluatorCapability()
				instanceof PiecewisePolynomialLocus2D)
				|| !(context.getSecondDefinition().getEvaluatorCapability()
						instanceof PiecewisePolynomialLocus2D)) {
			return false;
		}
		PiecewisePolynomialLocus2D first = firstSource(context);
		PiecewisePolynomialLocus2D second = secondSource(context);
		if (!hasSafeCompositionDepth(first)
				|| !hasSafeCompositionDepth(second)
				|| !first.supportsPiecewisePolynomial(context.getFirstDefinition())
				|| !second.supportsPiecewisePolynomial(
						context.getSecondDefinition())) {
			return false;
		}
		try {
			return supportedComponents(context.getFirstComponents(), first)
					&& supportedComponents(context.getSecondComponents(), second);
		} catch (RuntimeException exception) {
			return false;
		}
	}

	@Override
	public LocusPairIntersectionCandidateSet2D isolate(
			LocusPairIntersectionContext2D context) {
		PiecewisePolynomialLocus2D firstSource = firstSource(context);
		PiecewisePolynomialLocus2D secondSource = secondSource(context);
		ArrayList<LocatedRoot> roots = new ArrayList<>();
		ArrayList<IntersectionOverlapEvidence2D> overlap = new ArrayList<>();

		for (ComponentAddress firstComponent : context.getFirstComponents()) {
			List<PolynomialSpan> firstSpans = spans(context.getFirstDefinition(),
					firstSource, firstComponent);
			for (ComponentAddress secondComponent
					: context.getSecondComponents()) {
				List<PolynomialSpan> secondSpans = spans(
						context.getSecondDefinition(), secondSource,
						secondComponent);
				for (PolynomialSpan firstSpan : firstSpans) {
					for (PolynomialSpan secondSpan : secondSpans) {
						context.getInstrumentation().recordOverlapCheck();
						OverlapRelationKind relation = overlapRelation(firstSpan,
								secondSpan);
						if (relation != null) {
							overlap.add(overlapEvidence(firstComponent,
									secondComponent, firstSpan, secondSpan,
									relation));
							continue;
						}
						isolateSpanPair(context, firstComponent, secondComponent,
								firstSpan, secondSpan, roots);
					}
				}
			}
		}

		List<LocatedRoot> unique = deduplicate(context, roots);
		ArrayList<LocusPairIntersectionCandidate2D> candidates =
				new ArrayList<>();
		for (LocatedRoot root : unique) {
			candidates.add(candidate(context, root));
		}
		ArrayList<IntersectionDiagnostic2D> diagnostics = new ArrayList<>();
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.PAIR_COVERAGE_NOT_ESTABLISHED,
				"Semantic polynomial span pairs used deterministic Bernstein "
						+ "convex-hull rejection, but floating boxes do not prove "
						+ "exhaustive interval-rounded pair coverage"));
		if (!overlap.isEmpty()) {
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.OVERLAP_SUSPECTED,
					"Polynomial span equivalence is retained as suspected overlap; "
							+ "no isolated point is manufactured"));
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
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				context.getAllComponentPairKeys(), candidates, overlap,
				diagnostics);
	}

	private void isolateSpanPair(LocusPairIntersectionContext2D context,
			ComponentAddress firstComponent, ComponentAddress secondComponent,
			PolynomialSpan firstSpan, PolynomialSpan secondSpan,
			List<LocatedRoot> roots) {
		ArrayDeque<ParameterBox> queue = new ArrayDeque<>();
		queue.add(new ParameterBox(firstSpan.lower, firstSpan.upper,
				secondSpan.lower, secondSpan.upper, 1));
		int maximumDepth = context.getQuery().getPolicy().getPairWorkBudget()
				.getMaximumBoxDepth();
		while (!queue.isEmpty()) {
			ParameterBox box = queue.removeFirst();
			context.getInstrumentation().recordParameterBox(box.depth);
			if (excluded(context, firstSpan, secondSpan, box)) {
				context.getInstrumentation().recordRejectedBox();
				continue;
			}
			if (box.depth >= maximumDepth) {
				context.getInstrumentation().recordCandidateBox();
				for (RefinedPair refined : refine(context, firstSpan, secondSpan,
						box)) {
					LocatedRoot root = located(context, firstComponent,
							secondComponent, firstSpan, secondSpan, box, refined);
					if (root != null) {
						roots.add(root);
					}
				}
				continue;
			}
			// The query has already canonicalized source order. A width tie is
			// therefore resolved on the canonical first semantic axis, not on
			// caller order or candidate enumeration.
			double firstWidth = (box.firstUpper - box.firstLower)
					/ firstSpan.width();
			double secondWidth = (box.secondUpper - box.secondLower)
					/ secondSpan.width();
			if (firstWidth >= secondWidth) {
				double middle = midpoint(box.firstLower, box.firstUpper);
				queue.addLast(new ParameterBox(box.firstLower, middle,
						box.secondLower, box.secondUpper, box.depth + 1));
				queue.addLast(new ParameterBox(middle, box.firstUpper,
						box.secondLower, box.secondUpper, box.depth + 1));
			} else {
				double middle = midpoint(box.secondLower, box.secondUpper);
				queue.addLast(new ParameterBox(box.firstLower, box.firstUpper,
						box.secondLower, middle, box.depth + 1));
				queue.addLast(new ParameterBox(box.firstLower, box.firstUpper,
						middle, box.secondUpper, box.depth + 1));
			}
		}
	}

	private static boolean excluded(LocusPairIntersectionContext2D context,
			PolynomialSpan first, PolynomialSpan second, ParameterBox box) {
		double[] firstX = restrictedBernstein(first.xBernstein, first,
				box.firstLower,
				box.firstUpper);
		double[] firstY = restrictedBernstein(first.yBernstein, first,
				box.firstLower,
				box.firstUpper);
		double[] secondX = restrictedBernstein(second.xBernstein, second,
				box.secondLower, box.secondUpper);
		double[] secondY = restrictedBernstein(second.yBernstein, second,
				box.secondLower, box.secondUpper);
		double tolerance = context.getQuery().getPolicy()
				.getCoordinateTolerance().getValue();
		return excludesZero(firstX, secondX, tolerance)
				|| excludesZero(firstY, secondY, tolerance);
	}

	private static boolean excludesZero(double[] first, double[] second,
			double policyTolerance) {
		double minimum = Double.POSITIVE_INFINITY;
		double maximum = Double.NEGATIVE_INFINITY;
		double scale = 1;
		for (double firstValue : first) {
			for (double secondValue : second) {
				double difference = firstValue - secondValue;
				minimum = Math.min(minimum, difference);
				maximum = Math.max(maximum, difference);
				scale = Math.max(scale, Math.abs(firstValue) + Math.abs(secondValue));
			}
		}
		double roundoff = Math.max(policyTolerance,
				1024 * Math.ulp(scale) * (first.length + second.length));
		return minimum > roundoff || maximum < -roundoff;
	}

	private static List<RefinedPair> refine(
			LocusPairIntersectionContext2D context, PolynomialSpan firstSpan,
			PolynomialSpan secondSpan,
			ParameterBox box) {
		double firstMiddle = midpoint(box.firstLower, box.firstUpper);
		double secondMiddle = midpoint(box.secondLower, box.secondUpper);
		double firstQuarter = midpoint(box.firstLower, firstMiddle);
		double firstThreeQuarter = midpoint(firstMiddle, box.firstUpper);
		double secondQuarter = midpoint(box.secondLower, secondMiddle);
		double secondThreeQuarter = midpoint(secondMiddle, box.secondUpper);
		double[][] seeds = {
				{firstMiddle, secondMiddle},
				{firstQuarter, secondQuarter},
				{firstQuarter, secondThreeQuarter},
				{firstThreeQuarter, secondQuarter},
				{firstThreeQuarter, secondThreeQuarter}};
		ArrayList<RefinedPair> roots = new ArrayList<>();
		for (double[] seed : seeds) {
			RefinedPair root = refineFrom(context, firstSpan, secondSpan, box,
					seed[0], seed[1]);
			if (root != null && roots.stream().noneMatch(retained ->
					close(context, retained, root))) {
				roots.add(root);
			}
		}
		return roots;
	}

	private static RefinedPair refineFrom(
			LocusPairIntersectionContext2D context, PolynomialSpan firstSpan,
			PolynomialSpan secondSpan, ParameterBox box,
			double firstParameter, double secondParameter) {
		context.getInstrumentation().recordPairRefinementStarted();
		int maximumIterations = context.getQuery().getPolicy()
				.getPairWorkBudget().getMaximumPairRefinementIterations();
		for (int iteration = 1; iteration <= maximumIterations; iteration++) {
			context.getInstrumentation().recordPairRefinementIteration(iteration);
			context.getInstrumentation().recordSemanticEvaluation();
			context.getInstrumentation().recordSemanticEvaluation();
			LocusPoint2D firstValue = polynomialValue(firstSpan, firstParameter);
			LocusPoint2D secondValue = polynomialValue(secondSpan,
					secondParameter);
			if (!finite(firstValue) || !finite(secondValue)) {
				return null;
			}
			double dx = firstValue.getX() - secondValue.getX();
			double dy = firstValue.getY() - secondValue.getY();
			double threshold = context.getQuery().getPolicy()
					.getResidualTolerance().threshold(
							characteristicScale(firstValue, secondValue));
			context.getInstrumentation().recordDerivativeEvaluation();
			context.getInstrumentation().recordDerivativeEvaluation();
			LocusPoint2D firstDerivative = polynomialDerivative(firstSpan,
					firstParameter);
			LocusPoint2D secondDerivative = polynomialDerivative(secondSpan,
					secondParameter);
			double firstSpeed = Math.hypot(firstDerivative.getX(),
					firstDerivative.getY());
			double secondSpeed = Math.hypot(secondDerivative.getX(),
					secondDerivative.getY());
			if (!finite(firstDerivative) || !finite(secondDerivative)
					|| !Double.isFinite(firstSpeed) || !(firstSpeed > 0)
					|| !Double.isFinite(secondSpeed) || !(secondSpeed > 0)) {
				return null;
			}
			context.getInstrumentation().recordJacobianEvaluation();
			double a = firstDerivative.getX() / firstSpeed;
			double b = -secondDerivative.getX() / secondSpeed;
			double c = firstDerivative.getY() / firstSpeed;
			double d = -secondDerivative.getY() / secondSpeed;
			double determinant = a * d - b * c;
			if (!Double.isFinite(determinant)
					|| Math.abs(determinant) <= 128 * Math.ulp(1.0)) {
				return null;
			}
			double deltaFirst = (-dx * d + b * dy) / determinant
					/ firstSpeed;
			double deltaSecond = (-a * dy + dx * c) / determinant
					/ secondSpeed;
			double firstTolerance = context.getQuery().getPolicy()
					.getFirstRootTolerance().getValue();
			double secondTolerance = context.getQuery().getPolicy()
					.getSecondRootTolerance().getValue();
			if (Math.hypot(dx, dy) <= threshold
					&& Math.abs(deltaFirst) <= firstTolerance
					&& Math.abs(deltaSecond) <= secondTolerance) {
				return new RefinedPair(firstParameter, secondParameter);
			}
			double nextFirst = clamp(firstParameter + deltaFirst,
					box.firstLower, box.firstUpper);
			double nextSecond = clamp(secondParameter + deltaSecond,
					box.secondLower, box.secondUpper);
			if (nextFirst == firstParameter && nextSecond == secondParameter) {
				return null;
			}
			firstParameter = nextFirst;
			secondParameter = nextSecond;
		}
		return null;
	}

	private static LocatedRoot located(LocusPairIntersectionContext2D context,
			ComponentAddress firstComponent, ComponentAddress secondComponent,
			PolynomialSpan firstSpan, PolynomialSpan secondSpan, ParameterBox box,
			RefinedPair refined) {
		double firstTolerance = context.getQuery().getPolicy()
				.getFirstRootTolerance().getValue();
		double secondTolerance = context.getQuery().getPolicy()
				.getSecondRootTolerance().getValue();
		double firstParameter = ownedCanonicalParameter(
				context.getFirstDefinition(), firstComponent, firstSpan,
				refined.first, firstTolerance);
		double secondParameter = ownedCanonicalParameter(
				context.getSecondDefinition(), secondComponent, secondSpan,
				refined.second, secondTolerance);
		if (!Double.isFinite(firstParameter)
				|| !Double.isFinite(secondParameter)) {
			return null;
		}
		return new LocatedRoot(firstComponent, secondComponent, firstSpan,
				secondSpan, firstParameter, secondParameter,
				new IntersectionParameterInterval2D(box.firstLower,
						box.firstUpper),
				new IntersectionParameterInterval2D(box.secondLower,
						box.secondUpper));
	}

	private static double ownedCanonicalParameter(LocusDefinition2D definition,
			ComponentAddress component, PolynomialSpan span, double parameter,
			double tolerance) {
		double endpointTolerance = Math.max(32 * tolerance,
				64 * Math.max(Math.ulp(span.lower), Math.ulp(span.upper)));
		if (!span.ownsUpper
				&& Math.abs(parameter - span.upper) <= endpointTolerance) {
			return Double.NaN;
		}
		double snapped = Math.abs(parameter - span.lower) <= endpointTolerance
				? span.lower
				: Math.abs(parameter - span.upper) <= endpointTolerance
						? span.upper : parameter;
		double canonical = definition.getProvider().canonicalize(snapped);
		return component.getInterval().contains(canonical,
				definition.getProvider().getDomainEpsilon()) ? canonical
						: Double.NaN;
	}

	private LocusPairIntersectionCandidate2D candidate(
			LocusPairIntersectionContext2D context, LocatedRoot root) {
		LocusDifferentialEvaluation2D firstDifferential =
				context.evaluateFirstDifferential(root.first.getBranchKey(),
						root.firstParameter, root.first.getInterval());
		LocusDifferentialEvaluation2D secondDifferential =
				context.evaluateSecondDifferential(root.second.getBranchKey(),
						root.secondParameter, root.second.getInterval());
		ContactClass contact = ContactClass.CONTACT_UNDETERMINED;
		if (firstDifferential.getRegularity() == Regularity.REGULAR
				&& secondDifferential.getRegularity() == Regularity.REGULAR) {
			context.getInstrumentation().recordJacobianEvaluation();
			double determinant = normalizedDeterminant(
					firstDifferential.getDerivative(),
					secondDifferential.getDerivative());
			if (Double.isFinite(determinant)
					&& Math.abs(determinant) > context.getQuery().getPolicy()
							.getTangencyTolerance().getThreshold()) {
				contact = ContactClass.TRANSVERSE_ESTABLISHED;
			}
		}
		String lineage = semanticLineage(root);
		List<IntersectionDiagnostic2D> diagnostics = List.of(
				new IntersectionDiagnostic2D(
						DiagnosticCode.PAIR_LOCAL_ISOLATION_NOT_ESTABLISHED,
						"Polynomial semantic span boxes localized the pair root, "
								+ "but interval-rounded rectangle uniqueness is not established"));
		return new LocusPairIntersectionCandidate2D(
				root.first.getBranchKey(), root.first.getComponentKey(),
				root.firstParameter, OptionalDouble.empty(), root.firstInterval,
				root.second.getBranchKey(), root.second.getComponentKey(),
				root.secondParameter, OptionalDouble.empty(), root.secondInterval,
				LocalPairIsolationEvidence2D.notEstablished(
						"Floating Bernstein boxes and Newton residual do not prove "
								+ "unique pair isolation"),
				lineage, Optional.empty(), contact,
				MultiplicityStatus.NOT_ESTABLISHED, OptionalInt.empty(),
				SolverMethod.SAFEGUARDED_DUAL_PARAMETER,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				LineageEventKind.APPEARED, Collections.emptyList(), diagnostics);
	}

	private String semanticLineage(LocatedRoot root) {
		// Parameters distinguish diagnostic rich candidates only. With no
		// continuation key and no established pair isolation, this material is
		// deliberately not durable public identity authority.
		return LocusPairIntersectionContext2D.componentPairKey(
				root.first.getBranchKey(), root.first.getComponentKey(),
				root.second.getBranchKey(), root.second.getComponentKey())
				+ "/semantic-polynomial-spans/"
				+ spanKey(root.firstSpan) + "/" + spanKey(root.secondSpan)
				+ "/parameters/" + Double.toHexString(root.firstParameter)
				+ "/" + Double.toHexString(root.secondParameter);
	}

	private static String spanKey(PolynomialSpan span) {
		return Double.toHexString(span.lower) + ":"
				+ Double.toHexString(span.upper);
	}

	private static List<LocatedRoot> deduplicate(
			LocusPairIntersectionContext2D context, List<LocatedRoot> input) {
		ArrayList<LocatedRoot> ordered = new ArrayList<>(input);
		ordered.sort(Comparator.comparing((LocatedRoot root) ->
				root.first.getBranchKey())
				.thenComparing(root -> root.first.getComponentKey())
				.thenComparingDouble(root -> root.firstParameter)
				.thenComparing(root -> root.second.getBranchKey())
				.thenComparing(root -> root.second.getComponentKey())
				.thenComparingDouble(root -> root.secondParameter));
		ArrayList<LocatedRoot> output = new ArrayList<>();
		double firstTolerance = context.getQuery().getPolicy()
				.getFirstDeduplicationTolerance().getValue();
		double secondTolerance = context.getQuery().getPolicy()
				.getSecondDeduplicationTolerance().getValue();
		for (LocatedRoot candidate : ordered) {
			boolean duplicate = false;
			for (LocatedRoot retained : output) {
				if (!sameComponents(candidate, retained)) {
					continue;
				}
				context.getInstrumentation().recordDeduplicationComparison();
				if (equivalentParameter(context.getFirstDefinition(),
						candidate.firstParameter, retained.firstParameter,
						firstTolerance)
						&& equivalentParameter(context.getSecondDefinition(),
								candidate.secondParameter,
								retained.secondParameter, secondTolerance)) {
					duplicate = true;
					break;
				}
			}
			if (!duplicate) {
				output.add(candidate);
			}
		}
		return output;
	}

	private static boolean sameComponents(LocatedRoot first,
			LocatedRoot second) {
		return first.first.getBranchKey().equals(second.first.getBranchKey())
				&& first.first.getComponentKey()
						.equals(second.first.getComponentKey())
				&& first.second.getBranchKey().equals(second.second.getBranchKey())
				&& first.second.getComponentKey()
						.equals(second.second.getComponentKey());
	}

	private static boolean equivalentParameter(LocusDefinition2D definition,
			double first, double second, double tolerance) {
		double firstCanonical = definition.getProvider().canonicalize(first);
		double secondCanonical = definition.getProvider().canonicalize(second);
		double difference = Math.abs(firstCanonical - secondCanonical);
		if (!definition.getProvider().isPeriodic()) {
			return difference <= tolerance;
		}
		LocusInterval2D domain = definition.getProvider().getDeclaredDomain();
		double period = domain.getUpper() - domain.getLower();
		return Math.min(difference, Math.abs(period - difference)) <= tolerance;
	}

	private IntersectionOverlapEvidence2D overlapEvidence(
			ComponentAddress first, ComponentAddress second,
			PolynomialSpan firstSpan, PolynomialSpan secondSpan,
			OverlapRelationKind relation) {
		return new IntersectionOverlapEvidence2D(first.getBranchKey(),
				first.getComponentKey(), second.getBranchKey(),
				second.getComponentKey(),
				OverlapStatus.OVERLAP_SUSPECTED_NOT_ESTABLISHED, relation,
				getCapabilityId(),
				"Equivalent normalized semantic span polynomials; no certified "
						+ "global parameter map",
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				"span " + spanKey(firstSpan) + " versus "
						+ spanKey(secondSpan));
	}

	private static OverlapRelationKind overlapRelation(PolynomialSpan first,
			PolynomialSpan second) {
		if (samePolynomial(first.x, second.x)
				&& samePolynomial(first.y, second.y)) {
			return first.wholeComponent && second.wholeComponent
					? OverlapRelationKind.FULL_COMPONENT
					: OverlapRelationKind.PARTIAL_COMPONENT;
		}
		if (samePolynomial(first.x, reverse(second.x))
				&& samePolynomial(first.y, reverse(second.y))) {
			return OverlapRelationKind.REVERSE_PARAMETERIZATION;
		}
		return null;
	}

	private static List<PolynomialSpan> spans(LocusDefinition2D definition,
			PiecewisePolynomialLocus2D source, ComponentAddress component) {
		ArrayList<PolynomialSpan> result = new ArrayList<>();
		int spanCount = source.getPolynomialSpanCount(component.getBranchKey());
		for (int span = 0; span < spanCount; span++) {
			double sourceLower = source.getPolynomialSpanLower(
					component.getBranchKey(), span);
			double sourceUpper = source.getPolynomialSpanUpper(
					component.getBranchKey(), span);
			double lower = Math.max(sourceLower,
					component.getInterval().getLower());
			double upper = Math.min(sourceUpper,
					component.getInterval().getUpper());
			if (!(Double.isFinite(lower) && Double.isFinite(upper)
					&& lower < upper)) {
				continue;
			}
			double[][] coordinates = source.getPolynomialCoordinateCoefficients(
					component.getBranchKey(), span);
			double[] x = localPolynomial(coordinates[0], lower, upper);
			double[] y = localPolynomial(coordinates[1], lower, upper);
			result.add(new PolynomialSpan(lower, upper, x, y, false,
					lower == component.getInterval().getLower()
							&& upper == component.getInterval().getUpper()));
		}
		if (!result.isEmpty()) {
			int last = result.size() - 1;
			PolynomialSpan value = result.get(last);
			boolean ownsUpper = component.getInterval().isUpperClosed()
					&& !definition.getProvider().isPeriodic();
			result.set(last, value.withUpperOwnership(ownsUpper));
		}
		return result;
	}

	private static boolean supportedComponents(List<ComponentAddress> components,
			PiecewisePolynomialLocus2D source) {
		for (ComponentAddress component : components) {
			if (!Double.isFinite(component.getInterval().getLower())
					|| !Double.isFinite(component.getInterval().getUpper())) {
				return false;
			}
			int spans = source.getPolynomialSpanCount(component.getBranchKey());
			if (spans < 1) {
				return false;
			}
			for (int span = 0; span < spans; span++) {
				double[][] coordinates = source
						.getPolynomialCoordinateCoefficients(
								component.getBranchKey(), span);
				if (coordinates == null || coordinates.length != 2) {
					return false;
				}
				for (double[] coefficients : coordinates) {
					if (coefficients == null || coefficients.length < 1
							|| coefficients.length > MAXIMUM_COEFFICIENT_COUNT) {
						return false;
					}
					for (double coefficient : coefficients) {
						if (!Double.isFinite(coefficient)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private static boolean hasSafeCompositionDepth(
			PiecewisePolynomialLocus2D source) {
		int compositionDepth = source.getPolynomialCompositionDepth();
		return compositionDepth > 0 && compositionDepth
				<= PiecewisePolynomialLocus2D.MAXIMUM_SAFE_COMPOSITION_DEPTH;
	}

	private static PiecewisePolynomialLocus2D firstSource(
			LocusPairIntersectionContext2D context) {
		return (PiecewisePolynomialLocus2D) context.getFirstDefinition()
				.getEvaluatorCapability();
	}

	private static PiecewisePolynomialLocus2D secondSource(
			LocusPairIntersectionContext2D context) {
		return (PiecewisePolynomialLocus2D) context.getSecondDefinition()
				.getEvaluatorCapability();
	}

	private static double[] localPolynomial(double[] descending, double lower,
			double upper) {
		double width = upper - lower;
		if (descending == null || descending.length == 0
				|| !Double.isFinite(lower) || !Double.isFinite(upper)
				|| !Double.isFinite(width) || !(width > 0)) {
			throw new IllegalArgumentException(
					"Polynomial source span must be finite and nonempty");
		}
		int degree = descending.length - 1;
		double[] local = new double[descending.length];
		for (int globalPower = 0; globalPower <= degree; globalPower++) {
			double coefficient = descending[degree - globalPower];
			if (!Double.isFinite(coefficient)) {
				throw new IllegalArgumentException(
						"Polynomial source coefficients must be finite");
			}
			for (int localPower = 0; localPower <= globalPower;
					localPower++) {
				local[localPower] += coefficient
						* binomial(globalPower, localPower)
						* Math.pow(lower, globalPower - localPower)
						* Math.pow(width, localPower);
			}
		}
		return local;
	}

	private static LocusPoint2D polynomialValue(PolynomialSpan span,
			double parameter) {
		double localParameter = (parameter - span.lower) / span.width();
		return new LocusPoint2D(
				evaluateBernstein(span.xBernstein, localParameter),
				evaluateBernstein(span.yBernstein, localParameter));
	}

	private static LocusPoint2D polynomialDerivative(PolynomialSpan span,
			double parameter) {
		double localParameter = (parameter - span.lower) / span.width();
		double inverseWidth = 1 / span.width();
		return new LocusPoint2D(
				evaluateBernsteinDerivative(span.xBernstein, localParameter)
						* inverseWidth,
				evaluateBernsteinDerivative(span.yBernstein, localParameter)
						* inverseWidth);
	}

	private static double evaluateBernstein(double[] coefficients,
			double parameter) {
		double[] work = coefficients.clone();
		for (int level = work.length - 1; level > 0; level--) {
			for (int index = 0; index < level; index++) {
				work[index] = (1 - parameter) * work[index]
						+ parameter * work[index + 1];
			}
		}
		return work[0];
	}

	private static double evaluateBernsteinDerivative(double[] coefficients,
			double parameter) {
		int degree = coefficients.length - 1;
		if (degree == 0) {
			return 0;
		}
		double[] derivative = new double[degree];
		for (int index = 0; index < degree; index++) {
			derivative[index] = degree
					* (coefficients[index + 1] - coefficients[index]);
		}
		return evaluateBernstein(derivative, parameter);
	}

	private static double[] restrictedBernstein(double[] bernstein,
			PolynomialSpan span, double lower, double upper) {
		double localLower = (lower - span.lower) / span.width();
		double localUpper = (upper - span.lower) / span.width();
		double[] right = localLower == 0 ? bernstein.clone()
				: splitBernstein(bernstein, localLower)[1];
		if (localUpper == 1) {
			return right;
		}
		double remainingUpper = (localUpper - localLower) / (1 - localLower);
		return splitBernstein(right, remainingUpper)[0];
	}

	private static double[][] splitBernstein(double[] coefficients,
			double parameter) {
		int degree = coefficients.length - 1;
		double[] work = coefficients.clone();
		double[] left = new double[coefficients.length];
		double[] right = new double[coefficients.length];
		left[0] = work[0];
		right[degree] = work[degree];
		for (int level = 1; level <= degree; level++) {
			for (int index = 0; index <= degree - level; index++) {
				work[index] = (1 - parameter) * work[index]
						+ parameter * work[index + 1];
			}
			left[level] = work[0];
			right[degree - level] = work[degree - level];
		}
		return new double[][] {left, right};
	}

	private static double[] powerToBernstein(double[] ascending) {
		int degree = ascending.length - 1;
		double[] bernstein = new double[ascending.length];
		for (int index = 0; index <= degree; index++) {
			for (int power = 0; power <= index; power++) {
				bernstein[index] += ascending[power]
						* binomial(index, power) / binomial(degree, power);
			}
		}
		return bernstein;
	}

	private static double[] reverse(double[] ascending) {
		double[] result = new double[ascending.length];
		for (int power = 0; power < ascending.length; power++) {
			for (int resultPower = 0; resultPower <= power; resultPower++) {
				result[resultPower] += ascending[power]
						* binomial(power, resultPower)
						* ((resultPower & 1) == 0 ? 1 : -1);
			}
		}
		requireFinite(result);
		return result;
	}

	private static boolean samePolynomial(double[] first, double[] second) {
		int length = Math.max(first.length, second.length);
		double scale = 1;
		for (double value : first) {
			scale = Math.max(scale, Math.abs(value));
		}
		for (double value : second) {
			scale = Math.max(scale, Math.abs(value));
		}
		double tolerance = COEFFICIENT_COMPARISON_FACTOR * Math.ulp(scale)
				* length;
		for (int index = 0; index < length; index++) {
			double firstValue = index < first.length ? first[index] : 0;
			double secondValue = index < second.length ? second[index] : 0;
			if (Math.abs(firstValue - secondValue) > tolerance) {
				return false;
			}
		}
		return true;
	}

	private static double binomial(int n, int k) {
		int symmetric = Math.min(k, n - k);
		double value = 1;
		for (int index = 1; index <= symmetric; index++) {
			value *= (double) (n - symmetric + index) / index;
		}
		return value;
	}

	private static boolean close(LocusPairIntersectionContext2D context,
			RefinedPair first, RefinedPair second) {
		return Math.abs(first.first - second.first) <= context.getQuery()
				.getPolicy().getFirstDeduplicationTolerance().getValue()
				&& Math.abs(first.second - second.second) <= context.getQuery()
						.getPolicy().getSecondDeduplicationTolerance().getValue();
	}

	private static double normalizedDeterminant(LocusPoint2D first,
			LocusPoint2D second) {
		double firstSpeed = Math.hypot(first.getX(), first.getY());
		double secondSpeed = Math.hypot(second.getX(), second.getY());
		if (!Double.isFinite(firstSpeed) || !(firstSpeed > 0)
				|| !Double.isFinite(secondSpeed) || !(secondSpeed > 0)) {
			return Double.NaN;
		}
		double firstX = first.getX() / firstSpeed;
		double firstY = first.getY() / firstSpeed;
		double secondX = second.getX() / secondSpeed;
		double secondY = second.getY() / secondSpeed;
		return firstX * secondY - firstY * secondX;
	}

	private static boolean finite(LocusPoint2D point) {
		return Double.isFinite(point.getX()) && Double.isFinite(point.getY());
	}

	private static void requireFinite(double[] coefficients) {
		for (double coefficient : coefficients) {
			if (!Double.isFinite(coefficient)) {
				throw new IllegalArgumentException(
						"Local polynomial conversion must remain finite");
			}
		}
	}

	private static double characteristicScale(LocusPoint2D first,
			LocusPoint2D second) {
		return Math.max(1, Math.max(Math.hypot(first.getX(), first.getY()),
				Math.hypot(second.getX(), second.getY())));
	}

	private static double midpoint(double lower, double upper) {
		return lower + (upper - lower) / 2;
	}

	private static double clamp(double value, double lower, double upper) {
		return Math.max(lower, Math.min(upper, value));
	}

	private static final class PolynomialSpan {
		private final double lower;
		private final double upper;
		private final double[] x;
		private final double[] y;
		private final double[] xBernstein;
		private final double[] yBernstein;
		private final boolean ownsUpper;
		private final boolean wholeComponent;

		PolynomialSpan(double lower, double upper, double[] x, double[] y,
				boolean ownsUpper, boolean wholeComponent) {
			this.lower = lower;
			this.upper = upper;
			this.x = x;
			this.y = y;
			requireFinite(x);
			requireFinite(y);
			xBernstein = powerToBernstein(x);
			yBernstein = powerToBernstein(y);
			requireFinite(xBernstein);
			requireFinite(yBernstein);
			this.ownsUpper = ownsUpper;
			this.wholeComponent = wholeComponent;
		}

		PolynomialSpan withUpperOwnership(boolean value) {
			return new PolynomialSpan(lower, upper, x, y, value,
					wholeComponent);
		}

		double width() {
			return upper - lower;
		}
	}

	private static final class ParameterBox {
		private final double firstLower;
		private final double firstUpper;
		private final double secondLower;
		private final double secondUpper;
		private final int depth;

		ParameterBox(double firstLower, double firstUpper, double secondLower,
				double secondUpper, int depth) {
			this.firstLower = firstLower;
			this.firstUpper = firstUpper;
			this.secondLower = secondLower;
			this.secondUpper = secondUpper;
			this.depth = depth;
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

	private static final class LocatedRoot {
		private final ComponentAddress first;
		private final ComponentAddress second;
		private final PolynomialSpan firstSpan;
		private final PolynomialSpan secondSpan;
		private final double firstParameter;
		private final double secondParameter;
		private final IntersectionParameterInterval2D firstInterval;
		private final IntersectionParameterInterval2D secondInterval;

		LocatedRoot(ComponentAddress first, ComponentAddress second,
				PolynomialSpan firstSpan, PolynomialSpan secondSpan,
				double firstParameter, double secondParameter,
				IntersectionParameterInterval2D firstInterval,
				IntersectionParameterInterval2D secondInterval) {
			this.first = first;
			this.second = second;
			this.firstSpan = firstSpan;
			this.secondSpan = secondSpan;
			this.firstParameter = firstParameter;
			this.secondParameter = secondParameter;
			this.firstInterval = firstInterval;
			this.secondInterval = secondInterval;
		}
	}
}
