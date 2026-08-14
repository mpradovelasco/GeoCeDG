/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;

/**
 * Query-local adaptive candidate discovery using semantic evaluations only.
 *
 * <p>This capability deliberately never claims complete coverage. In addition
 * to sign changes, it refines local minima of absolute normalized residual so
 * that even contact can become a verified candidate without being falsely
 * classified or used as an absence proof.</p>
 */
public final class EvaluatorOnlyIntersectionCapability2D
		implements LocusIntersectionCapability2D {
	public static final String CAPABILITY_ID = "g8b-evaluator-adaptive/v1";
	public static final int DEFAULT_INITIAL_SUBDIVISIONS = 256;

	@Override
	public String getCapabilityId() {
		return CAPABILITY_ID;
	}

	@Override
	public boolean supports(IntersectionCapabilityContext2D context) {
		return true;
	}

	@Override
	public IntersectionCandidateSet2D isolate(
			IntersectionCapabilityContext2D context) {
		List<IntersectionCandidate2D> candidates = new ArrayList<>();
		List<String> examinedComponents = new ArrayList<>();
		for (LocusBranch2D branch : context.getDefinition().getBranches()) {
			List<LocusInterval2D> components = branch.getValidDomainComponents();
			for (int componentIndex = 0; componentIndex < components.size();
					componentIndex++) {
				String componentKey = IntersectionCapabilityContext2D.componentKey(
						branch.getBranchKey(), componentIndex);
				examinedComponents.add(componentKey);
				isolateComponent(context, branch, componentKey,
						components.get(componentIndex), candidates);
			}
		}
		List<IntersectionCandidate2D> deduplicated = deduplicate(context,
				candidates);
		List<IntersectionDiagnostic2D> diagnostics = List.of(
				new IntersectionDiagnostic2D(
						DiagnosticCode.COVERAGE_NOT_ESTABLISHED,
						"Evaluator-only adaptive search verifies candidates but "
								+ "cannot establish exhaustive component coverage"));
		return new IntersectionCandidateSet2D(Completeness.NOT_ESTABLISHED,
				CompletenessMethod.NOT_ESTABLISHED,
				deduplicated.isEmpty() ? GeometryKind.UNRESOLVED
						: GeometryKind.FINITE,
				SupportLevel.VERIFIED_UNCERTIFIED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				examinedComponents, deduplicated, Collections.emptyList(),
				diagnostics);
	}

	private static void isolateComponent(IntersectionCapabilityContext2D context,
			LocusBranch2D branch, String componentKey, LocusInterval2D interval,
			List<IntersectionCandidate2D> candidates) {
		double lower = interval.isLowerClosed() ? interval.getLower()
				: Math.nextUp(interval.getLower());
		double upper = interval.isUpperClosed() ? interval.getUpper()
				: Math.nextDown(interval.getUpper());
		if (lower > upper) {
			return;
		}
		if (lower == upper) {
			Sample sample = sample(context, branch.getBranchKey(), lower);
			if (sample.isFinite() && isResidualAcceptable(context, sample)) {
				candidates.add(candidate(branch.getBranchKey(), componentKey,
						sample.parameter, sample.parameter, sample.parameter));
			}
			return;
		}
		int subdivisions = Math.min(DEFAULT_INITIAL_SUBDIVISIONS,
				context.getQuery().getPolicy().getWorkBudget()
						.getMaximumIsolationSubdivisions());
		List<Sample> samples = new ArrayList<>();
		for (int index = 0; index <= subdivisions; index++) {
			double parameter = index == subdivisions ? upper
					: lower + (upper - lower) * index / subdivisions;
			samples.add(sample(context, branch.getBranchKey(), parameter));
			if (index > 0) {
				context.getInstrumentation().recordIsolationSubdivision(1);
			}
		}
		for (int index = 0; index < samples.size(); index++) {
			Sample current = samples.get(index);
			if (!current.isFinite()) {
				continue;
			}
			if (isResidualAcceptable(context, current)) {
				double candidateLower = index == 0 ? current.parameter
						: samples.get(index - 1).parameter;
				double candidateUpper = index + 1 == samples.size()
						? current.parameter : samples.get(index + 1).parameter;
				candidates.add(candidate(branch.getBranchKey(), componentKey,
						current.parameter, candidateLower, candidateUpper));
			}
			if (index > 0) {
				Sample previous = samples.get(index - 1);
				if (previous.isFinite() && oppositeSigns(previous.residual,
						current.residual)) {
					Sample refined = refineSignChange(context,
							branch.getBranchKey(), previous, current);
					candidates.add(candidate(branch.getBranchKey(), componentKey,
							refined.parameter, previous.parameter,
							current.parameter));
				}
			}
			if (index > 0 && index + 1 < samples.size()) {
				Sample previous = samples.get(index - 1);
				Sample next = samples.get(index + 1);
				if (previous.isFinite() && next.isFinite()
						&& Math.abs(current.residual)
								<= Math.abs(previous.residual)
						&& Math.abs(current.residual)
								<= Math.abs(next.residual)) {
					Sample minimum = refineAbsoluteMinimum(context,
							branch.getBranchKey(), previous.parameter,
							next.parameter);
					if (minimum.isFinite()
							&& isResidualAcceptable(context, minimum)) {
						candidates.add(candidate(branch.getBranchKey(), componentKey,
								minimum.parameter, previous.parameter,
								next.parameter));
					}
				}
			}
		}
	}

	private static Sample refineSignChange(IntersectionCapabilityContext2D context,
			String branchKey, Sample left, Sample right) {
		context.getInstrumentation().recordRefinementStarted();
		double tolerance = context.getQuery().getPolicy()
				.getRootParameterTolerance().getValue();
		Sample lower = left;
		Sample upper = right;
		for (long iteration = 1; iteration <= context.getQuery().getPolicy()
				.getWorkBudget().getMaximumRefinementIterations(); iteration++) {
			context.getInstrumentation().recordRefinementIteration(iteration);
			double middleParameter = lower.parameter
					+ (upper.parameter - lower.parameter) / 2;
			Sample middle = sample(context, branchKey, middleParameter);
			if (!middle.isFinite() || middle.residual == 0
					|| upper.parameter - lower.parameter <= tolerance) {
				return middle;
			}
			if (oppositeSigns(lower.residual, middle.residual)) {
				upper = middle;
			} else {
				lower = middle;
			}
		}
		return Math.abs(lower.residual) <= Math.abs(upper.residual)
				? lower : upper;
	}

	private static Sample refineAbsoluteMinimum(
			IntersectionCapabilityContext2D context, String branchKey,
			double initialLower, double initialUpper) {
		context.getInstrumentation().recordRefinementStarted();
		double lower = initialLower;
		double upper = initialUpper;
		double tolerance = context.getQuery().getPolicy()
				.getRootParameterTolerance().getValue();
		Sample best = sample(context, branchKey, lower + (upper - lower) / 2);
		for (long iteration = 1; iteration <= context.getQuery().getPolicy()
				.getWorkBudget().getMaximumRefinementIterations(); iteration++) {
			context.getInstrumentation().recordRefinementIteration(iteration);
			if (upper - lower <= tolerance) {
				break;
			}
			double firstParameter = lower + (upper - lower) / 3;
			double secondParameter = upper - (upper - lower) / 3;
			Sample first = sample(context, branchKey, firstParameter);
			Sample second = sample(context, branchKey, secondParameter);
			if (!first.isFinite() || !second.isFinite()) {
				return best;
			}
			if (Math.abs(first.residual) <= Math.abs(second.residual)) {
				upper = secondParameter;
				if (!best.isFinite()
						|| Math.abs(first.residual) < Math.abs(best.residual)) {
					best = first;
				}
			} else {
				lower = firstParameter;
				if (!best.isFinite()
						|| Math.abs(second.residual) < Math.abs(best.residual)) {
					best = second;
				}
			}
		}
		return best;
	}

	private static Sample sample(IntersectionCapabilityContext2D context,
			String branchKey, double parameter) {
		LocusEvaluation2D evaluation = context.evaluate(branchKey, parameter);
		if (!evaluation.isValid()) {
			return Sample.invalid(parameter);
		}
		LocusPoint2D point = evaluation.getPoint();
		try {
			TargetResidual2D residual = context.evaluateResidual(point);
			return new Sample(parameter, residual.getNormalizedResidual(),
					residual.getCharacteristicScale(), true);
		} catch (IllegalArgumentException exception) {
			return Sample.invalid(parameter);
		}
	}

	private static boolean isResidualAcceptable(
			IntersectionCapabilityContext2D context, Sample sample) {
		double threshold = context.getQuery().getPolicy().getResidualTolerance()
				.threshold(sample.characteristicScale);
		return Math.abs(sample.residual) <= threshold;
	}

	private static IntersectionCandidate2D candidate(String branchKey,
			String componentKey, double parameter, double lower, double upper) {
		return new IntersectionCandidate2D(branchKey, componentKey, parameter,
				OptionalDouble.empty(),
				new IntersectionParameterInterval2D(Math.min(lower, parameter),
						Math.max(upper, parameter)),
				IntersectionSemanticMetadata2D.LocalIsolationStatus.NOT_ESTABLISHED,
				Optional.empty(), ContactClass.CONTACT_UNDETERMINED,
				MultiplicityStatus.NOT_ESTABLISHED, OptionalInt.empty(),
				SolverMethod.EVALUATOR_ADAPTIVE,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				LineageEventKind.APPEARED, Collections.emptyList(),
				Collections.emptyList());
	}

	private static List<IntersectionCandidate2D> deduplicate(
			IntersectionCapabilityContext2D context,
			List<IntersectionCandidate2D> candidates) {
		List<IntersectionCandidate2D> sorted = candidates.stream()
				.sorted(Comparator.comparing(IntersectionCandidate2D::getBranchKey)
						.thenComparing(IntersectionCandidate2D::getComponentKey)
						.thenComparingDouble(
								IntersectionCandidate2D::getSemanticParameter))
				.toList();
		ArrayList<IntersectionCandidate2D> result = new ArrayList<>();
		double tolerance = context.getQuery().getPolicy()
				.getDeduplicationTolerance().getValue();
		for (IntersectionCandidate2D candidate : sorted) {
			boolean duplicate = false;
			for (IntersectionCandidate2D retained : result) {
				if (!candidate.getBranchKey().equals(retained.getBranchKey())
						|| !candidate.getComponentKey()
								.equals(retained.getComponentKey())) {
					continue;
				}
				context.getInstrumentation().recordDeduplicationComparison();
				if (Math.abs(candidate.getSemanticParameter()
						- retained.getSemanticParameter()) <= tolerance
						|| candidate.getIsolatingInterval().overlaps(
								retained.getIsolatingInterval(), tolerance)) {
					duplicate = true;
					break;
				}
			}
			if (!duplicate) {
				context.getInstrumentation().recordCandidateInterval();
				result.add(candidate);
			}
		}
		return result;
	}

	private static boolean oppositeSigns(double first, double second) {
		return first < 0 && second > 0 || first > 0 && second < 0;
	}

	private static final class Sample {
		private final double parameter;
		private final double residual;
		private final double characteristicScale;
		private final boolean finite;

		Sample(double parameter, double residual, double characteristicScale,
				boolean finite) {
			this.parameter = parameter;
			this.residual = residual;
			this.characteristicScale = characteristicScale;
			this.finite = finite;
		}

		static Sample invalid(double parameter) {
			return new Sample(parameter, 0, 1, false);
		}

		boolean isFinite() {
			return finite;
		}
	}
}
