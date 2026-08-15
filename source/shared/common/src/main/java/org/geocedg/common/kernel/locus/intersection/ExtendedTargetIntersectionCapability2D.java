/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;

/**
 * Query-local adaptive one-parameter capability for the G8C1 target families.
 *
 * <p>It establishes individual regular transverse roots conservatively, finds
 * even-contact candidates through local minima, and never upgrades finite
 * sampling to global completeness or overlap proof.</p>
 */
public final class ExtendedTargetIntersectionCapability2D
		implements LocusIntersectionCapability2D {
	private static final int INITIAL_SUBDIVISIONS = 256;
	private static final int NON_ISOLATED_ZERO_RUN = 8;

	@Override
	public String getCapabilityId() {
		return "g8c1-query-local-adaptive-one-parameter/v1";
	}

	@Override
	public boolean supports(IntersectionCapabilityContext2D context) {
		TargetFamily family = context.getTarget().getFamily();
		return family == TargetFamily.ELLIPSE
				|| family == TargetFamily.PARABOLA
				|| family == TargetFamily.HYPERBOLA
				|| family == TargetFamily.BOUNDED_FUNCTION_GRAPH
				|| family == TargetFamily.REGULAR_POLYNOMIAL_IMPLICIT;
	}

	@Override
	public IntersectionCandidateSet2D isolate(
			IntersectionCapabilityContext2D context) {
		ArrayList<LocalizedRoot> roots = new ArrayList<>();
		ArrayList<String> examinedComponents = new ArrayList<>();
		boolean[] invalidTargetObserved = {false};
		boolean[] nonIsolatedZeroRun = {false};
		for (LocusBranch2D branch : context.getDefinition().getBranches()) {
			List<LocusInterval2D> components =
					branch.getValidDomainComponents();
			for (int componentIndex = 0;
					componentIndex < components.size(); componentIndex++) {
				String componentKey = IntersectionCapabilityContext2D.componentKey(
						branch.getBranchKey(), componentIndex);
				examinedComponents.add(componentKey);
				isolateComponent(context, branch.getBranchKey(), componentKey,
						components.get(componentIndex), roots,
						invalidTargetObserved, nonIsolatedZeroRun);
			}
		}
		ArrayList<IntersectionDiagnostic2D> diagnostics = new ArrayList<>();
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.COVERAGE_NOT_ESTABLISHED,
				"G8C1 adaptive finite evaluation verifies local candidates "
						+ "but does not prove exhaustive component coverage"));
		if (invalidTargetObserved[0]) {
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.TARGET_UNDEFINED,
					"Undefined, nonfinite, out-of-domain or non-normalizable "
							+ "target evaluations were hard isolation barriers"));
		}
		if (nonIsolatedZeroRun[0]) {
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.CAPABILITY_NOT_AVAILABLE,
					"A non-isolated candidate-level zero run could indicate "
							+ "overlap; sampling cannot establish overlap, so no "
							+ "finite sample projection is published"));
			return new IntersectionCandidateSet2D(Completeness.NOT_ESTABLISHED,
					CompletenessMethod.NOT_ESTABLISHED, GeometryKind.UNRESOLVED,
					SupportLevel.VERIFIED_UNCERTIFIED,
					NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
					examinedComponents, Collections.emptyList(),
					Collections.emptyList(), diagnostics);
		}

		List<LocalizedRoot> deduplicated = deduplicate(context, roots);
		Map<String, Integer> isolatedRootsPerComponent = new HashMap<>();
		for (LocalizedRoot root : deduplicated) {
			classify(context, root);
			if (root.localIsolation == LocalIsolationStatus.ESTABLISHED) {
				isolatedRootsPerComponent.merge(root.componentKey, 1,
						Integer::sum);
			}
		}
		ArrayList<IntersectionCandidate2D> candidates = new ArrayList<>();
		for (LocalizedRoot root : deduplicated) {
			Optional<String> continuationKey = root.localIsolation
					== LocalIsolationStatus.ESTABLISHED
							&& isolatedRootsPerComponent.getOrDefault(
									root.componentKey, 0) == 1
									? Optional.of("g8c1/unique-local-root/"
											+ root.componentKey)
									: Optional.empty();
			ArrayList<IntersectionDiagnostic2D> rootDiagnostics =
					new ArrayList<>();
			if (root.localIsolation == LocalIsolationStatus.ESTABLISHED) {
				rootDiagnostics.add(new IntersectionDiagnostic2D(
						DiagnosticCode.LOCAL_ISOLATION_ESTABLISHED,
						"Safeguarded semantic bracket plus normalized transverse "
								+ "contact establishes an isolated local root"));
			}
			if (!continuationKey.isPresent()) {
				rootDiagnostics.add(new IntersectionDiagnostic2D(
						DiagnosticCode.CONTINUATION_AMBIGUOUS,
						"No unique component-local continuation relation is "
								+ "established; parameter and result order are not identity"));
			}
			candidates.add(new IntersectionCandidate2D(root.branchKey,
					root.componentKey, root.parameter, OptionalDouble.empty(),
					new IntersectionParameterInterval2D(root.lower, root.upper),
					root.localIsolation, continuationKey, root.contact,
					MultiplicityStatus.NOT_ESTABLISHED, OptionalInt.empty(),
					root.method, root.guarantee, LineageEventKind.APPEARED,
					Collections.emptyList(), rootDiagnostics));
		}
		return new IntersectionCandidateSet2D(Completeness.NOT_ESTABLISHED,
				CompletenessMethod.NOT_ESTABLISHED,
				candidates.isEmpty() ? GeometryKind.UNRESOLVED
						: GeometryKind.FINITE,
				SupportLevel.VERIFIED_UNCERTIFIED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				examinedComponents, candidates, Collections.emptyList(),
				diagnostics);
	}

	private static void isolateComponent(
			IntersectionCapabilityContext2D context, String branchKey,
			String componentKey, LocusInterval2D component,
			List<LocalizedRoot> roots, boolean[] invalidTargetObserved,
			boolean[] nonIsolatedZeroRun) {
		double lower = component.isLowerClosed() ? component.getLower()
				: Math.nextUp(component.getLower());
		double upper = component.isUpperClosed() ? component.getUpper()
				: Math.nextDown(component.getUpper());
		if (lower > upper) {
			return;
		}
		if (lower == upper) {
			Sample sample = sample(context, branchKey, lower,
					invalidTargetObserved);
			if (sample.isFinite() && acceptable(context, sample)) {
				roots.add(new LocalizedRoot(branchKey, componentKey, component,
						lower, lower, lower, Origin.ENDPOINT));
			}
			return;
		}
		int subdivisions = Math.min(INITIAL_SUBDIVISIONS,
				context.getQuery().getPolicy().getWorkBudget()
						.getMaximumIsolationSubdivisions());
		ArrayList<Sample> samples = new ArrayList<>();
		int zeroRun = 0;
		for (int index = 0; index <= subdivisions; index++) {
			double parameter = index == subdivisions ? upper
					: lower + (upper - lower) * index / subdivisions;
			Sample current = sample(context, branchKey, parameter,
					invalidTargetObserved);
			samples.add(current);
			if (index > 0) {
				context.getInstrumentation().recordIsolationSubdivision(1);
			}
			zeroRun = current.isFinite() && acceptable(context, current)
					? zeroRun + 1 : 0;
			if (zeroRun >= NON_ISOLATED_ZERO_RUN) {
				nonIsolatedZeroRun[0] = true;
			}
		}
		if (nonIsolatedZeroRun[0]) {
			return;
		}
		for (int index = 0; index < samples.size(); index++) {
			Sample current = samples.get(index);
			if (!current.isFinite()) {
				continue;
			}
			if (acceptable(context, current)) {
				double radius = Math.max(context.getQuery().getPolicy()
						.getRootParameterTolerance().getValue(), Math.ulp(
								current.parameter) * 4);
				roots.add(new LocalizedRoot(branchKey, componentKey, component,
						current.parameter,
						Math.max(lower, current.parameter - radius),
						Math.min(upper, current.parameter + radius),
						index == 0 || index + 1 == samples.size()
								? Origin.ENDPOINT : Origin.SAMPLE_HIT));
			}
			if (index > 0) {
				Sample previous = samples.get(index - 1);
				if (previous.isFinite()
						&& oppositeSigns(previous.level, current.level)) {
					Refinement refined = refineSignChange(context, branchKey,
							previous, current, invalidTargetObserved);
					if (refined.valid && acceptable(context, refined.best)) {
						roots.add(new LocalizedRoot(branchKey, componentKey,
								component, refined.best.parameter,
								refined.lower, refined.upper,
								Origin.SIGN_CHANGE));
					}
				}
			}
			if (index > 0 && index + 1 < samples.size()) {
				Sample previous = samples.get(index - 1);
				Sample next = samples.get(index + 1);
				if (previous.isFinite() && next.isFinite()
						&& Math.abs(current.level) <= Math.abs(previous.level)
						&& Math.abs(current.level) <= Math.abs(next.level)) {
					Refinement minimum = refineAbsoluteMinimum(context,
							branchKey, previous.parameter, next.parameter,
							invalidTargetObserved);
					if (minimum.valid && acceptable(context, minimum.best)) {
						roots.add(new LocalizedRoot(branchKey, componentKey,
								component, minimum.best.parameter,
								minimum.lower, minimum.upper,
								Origin.LOCAL_MINIMUM));
					}
				}
			}
		}
	}

	private static Refinement refineSignChange(
			IntersectionCapabilityContext2D context, String branchKey,
			Sample left, Sample right, boolean[] invalidTargetObserved) {
		context.getInstrumentation().recordRefinementStarted();
		double tolerance = context.getQuery().getPolicy()
				.getRootParameterTolerance().getValue();
		Sample lower = left;
		Sample upper = right;
		Sample best = Math.abs(left.level) <= Math.abs(right.level)
				? left : right;
		for (long iteration = 1; iteration <= context.getQuery().getPolicy()
				.getWorkBudget().getMaximumRefinementIterations(); iteration++) {
			context.getInstrumentation().recordRefinementIteration(iteration);
			if (upper.parameter - lower.parameter <= tolerance) {
				break;
			}
			double middleParameter = lower.parameter
					+ (upper.parameter - lower.parameter) / 2;
			Sample middle = sample(context, branchKey, middleParameter,
					invalidTargetObserved);
			if (!middle.isFinite()) {
				return Refinement.invalid();
			}
			if (Math.abs(middle.level) < Math.abs(best.level)) {
				best = middle;
			}
			if (middle.level == 0) {
				double radius = Math.max(tolerance, Math.ulp(middle.parameter) * 4);
				return Refinement.valid(middle,
						Math.max(lower.parameter, middle.parameter - radius),
						Math.min(upper.parameter, middle.parameter + radius));
			}
			if (oppositeSigns(lower.level, middle.level)) {
				upper = middle;
			} else {
				lower = middle;
			}
		}
		return Refinement.valid(best, lower.parameter, upper.parameter);
	}

	private static Refinement refineAbsoluteMinimum(
			IntersectionCapabilityContext2D context, String branchKey,
			double initialLower, double initialUpper,
			boolean[] invalidTargetObserved) {
		context.getInstrumentation().recordRefinementStarted();
		double lower = initialLower;
		double upper = initialUpper;
		double tolerance = context.getQuery().getPolicy()
				.getRootParameterTolerance().getValue();
		Sample best = sample(context, branchKey,
				lower + (upper - lower) / 2, invalidTargetObserved);
		if (!best.isFinite()) {
			return Refinement.invalid();
		}
		for (long iteration = 1; iteration <= context.getQuery().getPolicy()
				.getWorkBudget().getMaximumRefinementIterations(); iteration++) {
			context.getInstrumentation().recordRefinementIteration(iteration);
			if (upper - lower <= tolerance) {
				break;
			}
			double firstParameter = lower + (upper - lower) / 3;
			double secondParameter = upper - (upper - lower) / 3;
			Sample first = sample(context, branchKey, firstParameter,
					invalidTargetObserved);
			Sample second = sample(context, branchKey, secondParameter,
					invalidTargetObserved);
			if (!first.isFinite() || !second.isFinite()) {
				return Refinement.invalid();
			}
			if (Math.abs(first.level) <= Math.abs(second.level)) {
				upper = secondParameter;
				if (Math.abs(first.level) < Math.abs(best.level)) {
					best = first;
				}
			} else {
				lower = firstParameter;
				if (Math.abs(second.level) < Math.abs(best.level)) {
					best = second;
				}
			}
		}
		return Refinement.valid(best, lower, upper);
	}

	private static Sample sample(IntersectionCapabilityContext2D context,
			String branchKey, double parameter,
			boolean[] invalidTargetObserved) {
		LocusEvaluation2D evaluation = context.evaluate(branchKey, parameter);
		if (!evaluation.isValid() || evaluation.getPoint() == null
				|| !finite(evaluation.getPoint())) {
			return Sample.invalid(parameter);
		}
		TargetCandidateEvaluation2D target =
				context.evaluateCandidateLevel(evaluation.getPoint());
		if (!target.isEstablished()) {
			invalidTargetObserved[0] = true;
			return Sample.invalid(parameter);
		}
		return new Sample(parameter, target.getLevel().getAsDouble(),
				target.getCharacteristicScale().getAsDouble(), true);
	}

	private static void classify(IntersectionCapabilityContext2D context,
			LocalizedRoot root) {
		LocusEvaluation2D evaluation = context.evaluate(root.branchKey,
				root.parameter);
		if (!evaluation.isValid() || evaluation.getPoint() == null) {
			return;
		}
		LocusDifferentialEvaluation2D differential =
				context.evaluateDifferential(root.branchKey, root.parameter,
						root.component);
		TargetContactEvidence2D contact = context.evaluateContact(
				evaluation.getPoint(), differential);
		if (!contact.isEstablished()) {
			return;
		}
		double indicator = Math.abs(contact.getNormalizedIndicator());
		if (indicator <= context.getQuery().getPolicy().getTangencyTolerance()
				.getThreshold()) {
			root.contact = ContactClass.TANGENT_ESTABLISHED;
			root.method = SolverMethod.SAFEGUARDED_DERIVATIVE;
			root.guarantee = NumericGuarantee.ESTIMATED_ERROR;
			return;
		}
		root.contact = ContactClass.TRANSVERSE_ESTABLISHED;
		root.method = SolverMethod.SAFEGUARDED_DERIVATIVE;
		root.guarantee = NumericGuarantee.ESTIMATED_ERROR;
		if (root.origin == Origin.SIGN_CHANGE
				|| root.origin == Origin.ENDPOINT
				|| root.origin == Origin.SAMPLE_HIT) {
			root.localIsolation = LocalIsolationStatus.ESTABLISHED;
		}
	}

	private static List<LocalizedRoot> deduplicate(
			IntersectionCapabilityContext2D context,
			List<LocalizedRoot> roots) {
		List<LocalizedRoot> sorted = roots.stream()
				.sorted(Comparator.comparing((LocalizedRoot root) -> root.branchKey)
						.thenComparing(root -> root.componentKey)
						.thenComparingDouble(root -> root.parameter))
				.toList();
		ArrayList<LocalizedRoot> result = new ArrayList<>();
		double tolerance = context.getQuery().getPolicy()
				.getDeduplicationTolerance().getValue();
		for (LocalizedRoot candidate : sorted) {
			boolean duplicate = false;
			for (int index = 0; index < result.size(); index++) {
				LocalizedRoot retained = result.get(index);
				if (!candidate.branchKey.equals(retained.branchKey)
						|| !candidate.componentKey.equals(retained.componentKey)) {
					continue;
				}
				context.getInstrumentation().recordDeduplicationComparison();
				if (Math.abs(candidate.parameter - retained.parameter)
						<= tolerance) {
					if (isStrongerLocalization(candidate.origin,
							retained.origin)) {
						result.set(index, candidate);
					}
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

	private static boolean isStrongerLocalization(Origin candidate,
			Origin retained) {
		return localizationRank(candidate) > localizationRank(retained);
	}

	private static int localizationRank(Origin origin) {
		switch (origin) {
		case SIGN_CHANGE:
			return 3;
		case ENDPOINT:
		case SAMPLE_HIT:
			return 2;
		case LOCAL_MINIMUM:
		default:
			return 1;
		}
	}

	private static boolean acceptable(IntersectionCapabilityContext2D context,
			Sample sample) {
		return Math.abs(sample.level) <= context.getQuery().getPolicy()
				.getResidualTolerance().threshold(sample.characteristicScale);
	}

	private static boolean oppositeSigns(double first, double second) {
		return first < 0 && second > 0 || first > 0 && second < 0;
	}

	private static boolean finite(LocusPoint2D point) {
		return Double.isFinite(point.getX()) && Double.isFinite(point.getY());
	}

	private enum Origin {
		SIGN_CHANGE, LOCAL_MINIMUM, SAMPLE_HIT, ENDPOINT
	}

	private static final class Sample {
		private final double parameter;
		private final double level;
		private final double characteristicScale;
		private final boolean finite;

		Sample(double parameter, double level, double characteristicScale,
				boolean finite) {
			this.parameter = parameter;
			this.level = level;
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

	private static final class Refinement {
		private final boolean valid;
		private final Sample best;
		private final double lower;
		private final double upper;

		private Refinement(boolean valid, Sample best, double lower,
				double upper) {
			this.valid = valid;
			this.best = best;
			this.lower = lower;
			this.upper = upper;
		}

		static Refinement valid(Sample best, double lower, double upper) {
			return new Refinement(true, best, Math.min(lower, best.parameter),
					Math.max(upper, best.parameter));
		}

		static Refinement invalid() {
			return new Refinement(false, null, 0, 0);
		}
	}

	private static final class LocalizedRoot {
		private final String branchKey;
		private final String componentKey;
		private final LocusInterval2D component;
		private final double parameter;
		private final double lower;
		private final double upper;
		private final Origin origin;
		private LocalIsolationStatus localIsolation =
				LocalIsolationStatus.NOT_ESTABLISHED;
		private ContactClass contact = ContactClass.CONTACT_UNDETERMINED;
		private SolverMethod method = SolverMethod.EVALUATOR_ADAPTIVE;
		private NumericGuarantee guarantee =
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED;

		LocalizedRoot(String branchKey, String componentKey,
				LocusInterval2D component, double parameter, double lower,
				double upper, Origin origin) {
			this.branchKey = branchKey;
			this.componentKey = componentKey;
			this.component = component;
			this.parameter = parameter;
			this.lower = Math.min(lower, parameter);
			this.upper = Math.max(upper, parameter);
			this.origin = origin;
		}
	}
}
