/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.geocedg.common.locus.G8AIntersectionSemanticModel.Completeness.COMPLETE;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.Completeness.INCOMPLETE;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.Completeness.NOT_ESTABLISHED;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.ComputationStatus.SUCCESS;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.ContactClass.CONTACT_UNDETERMINED;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.ContactClass.TANGENT_ESTABLISHED;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.ContactClass.TRANSVERSE_ESTABLISHED;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.Currentness.CURRENT;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.GeometryKind.EMPTY;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.GeometryKind.FINITE;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.GeometryKind.OVERLAP;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.GeometryKind.UNRESOLVED;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.IdentityStatus.NEW_TOPOLOGICAL_SOLUTION;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.LineageTransition.APPEARED;
import static org.geocedg.common.locus.G8AIntersectionSemanticModel.MultiplicityStatus.ESTABLISHED;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Classification;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Completeness;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.ComputationStatus;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.DomainLocation;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.DurableIdentity;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.GeometryKind;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.IdentityStatus;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Method;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.MultiplicityStatus;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Policy;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Result;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.RevisionEvidence;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.RootLineage;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Solution;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.SourceBinding;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.SupportLevel;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.WorkBudget;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.WorkCounters;
import org.geocedg.common.locus.G8ATargetAdapters.Target2D;

/** Test-private numerical strategies; never linked from productive source. */
final class G8AIntersectionNumerics {
	private G8AIntersectionNumerics() {
	}

	interface Curve2D {
		LocusPoint2D evaluate(double parameter);

		default LocusPoint2D derivative(double parameter) {
			return null;
		}
	}

	record RootProof(String semanticLabel, double parameter, int multiplicity) {
		RootProof {
			Objects.requireNonNull(semanticLabel);
			if (!Double.isFinite(parameter) || multiplicity < 1) {
				throw new IllegalArgumentException("Invalid factorized root proof");
			}
		}
	}

	record FactorizationProof(double equationScale, List<RootProof> roots,
			boolean identicallyZero, String proofMethod) {
		FactorizationProof {
			roots = roots.stream().sorted(
					Comparator.comparingDouble(RootProof::parameter)).toList();
			Objects.requireNonNull(proofMethod);
			if (!Double.isFinite(equationScale) || equationScale == 0) {
				throw new IllegalArgumentException("Equation scale must be nonzero");
			}
			if (identicallyZero && !roots.isEmpty()) {
				throw new IllegalArgumentException(
						"An overlap proof cannot also enumerate roots");
			}
		}

		double evaluate(double parameter) {
			if (identicallyZero) {
				return 0;
			}
			double value = equationScale;
			for (RootProof root : roots) {
				for (int order = 0; order < root.multiplicity(); order++) {
					value *= parameter - root.parameter();
				}
			}
			return value;
		}
	}

	record Problem(String sourcePairIdentity, String locusIdentity,
			long locusRevision, String targetIdentity, long targetUpdateStamp,
			String branchKey, String branchLineage, String componentKey,
			String topologyContext, double lower, double upper,
			boolean lowerIncluded, boolean upperIncluded, boolean periodic,
			Curve2D curve, Target2D target, FactorizationProof proof,
			Policy policy, WorkBudget budget) {
		Problem {
			Objects.requireNonNull(sourcePairIdentity);
			Objects.requireNonNull(locusIdentity);
			Objects.requireNonNull(targetIdentity);
			Objects.requireNonNull(branchKey);
			Objects.requireNonNull(branchLineage);
			Objects.requireNonNull(componentKey);
			Objects.requireNonNull(topologyContext);
			Objects.requireNonNull(curve);
			Objects.requireNonNull(target);
			Objects.requireNonNull(policy);
			Objects.requireNonNull(budget);
			if (locusRevision < 1 || targetUpdateStamp < 0 || lower > upper) {
				throw new IllegalArgumentException("Invalid problem binding/domain");
			}
		}
	}

	static Result analyticFactorization(Problem problem) {
		WorkCounters counters = new WorkCounters();
		counters.completenessEstablishmentChecks++;
		if (problem.proof() == null) {
			counters.completenessDomainsUnresolved++;
			return unresolved(problem, counters, NOT_ESTABLISHED,
					"No authoritative factorization capability");
		}
		if (problem.proof().identicallyZero()) {
			counters.completenessDomainsExcluded++;
			counters.overlapComponentsDetected++;
			return result(problem, SUCCESS, COMPLETE, OVERLAP,
					SupportLevel.EXACT, NumericGuarantee.EXACT_ARITHMETIC,
					List.of(), counters,
					List.of("Factorization establishes zero residual on component"));
		}
		List<RootProof> expectedRoots = rootsInDomain(problem);
		List<Solution> solutions = new ArrayList<>();
		for (RootProof root : expectedRoots) {
			Solution solution = verifyKnownRoot(problem, root,
					Method.ANALYTIC_FACTORIZATION, ESTABLISHED,
					NumericGuarantee.CERTIFIED_ERROR_BOUND, counters);
			if (solution != null) {
				solutions.add(solution);
			}
		}
		if (solutions.size() != expectedRoots.size()) {
			counters.completenessDomainsUnresolved++;
			GeometryKind kind = solutions.isEmpty() ? UNRESOLVED : FINITE;
			return result(problem, ComputationStatus.NUMERICAL_FAILURE,
					INCOMPLETE, kind, SupportLevel.UNSUPPORTED,
					NumericGuarantee.FLOATING_POINT_UNCERTIFIED, solutions,
					counters, List.of("A proved root failed independent verification"));
		}
		counters.completenessDomainsExcluded++;
		return completeFiniteOrEmpty(problem, solutions, counters,
				SupportLevel.EXACT, NumericGuarantee.CERTIFIED_ERROR_BOUND,
				List.of("Complete root set from explicit factorization proof"));
	}

	static Result certifiedBrackets(Problem problem) {
		WorkCounters counters = new WorkCounters();
		counters.completenessEstablishmentChecks++;
		if (problem.proof() == null || problem.proof().identicallyZero()) {
			counters.completenessDomainsUnresolved++;
			return unresolved(problem, counters, NOT_ESTABLISHED,
					"Bracket capability cannot establish this set kind");
		}
		List<Solution> solutions = new ArrayList<>();
		int unresolvedRoots = 0;
		for (RootProof root : rootsInDomain(problem)) {
			if (root.multiplicity() % 2 == 0) {
				unresolvedRoots++;
				counters.unresolvedCandidates++;
				continue;
			}
			double radius = safeBracketRadius(problem, root);
			double left = Math.max(problem.lower(), root.parameter() - radius);
			double right = Math.min(problem.upper(), root.parameter() + radius);
			double refined = bisect(problem, left, right, counters);
			RootProof refinedRoot = new RootProof(root.semanticLabel(), refined,
					root.multiplicity());
			Solution solution = verifyKnownRoot(problem, refinedRoot,
					Method.CERTIFIED_BRACKET, ESTABLISHED,
					NumericGuarantee.CERTIFIED_ERROR_BOUND, counters);
			if (solution != null) {
				solutions.add(solution);
			}
		}
		Completeness completeness = unresolvedRoots == 0 ? COMPLETE : INCOMPLETE;
		if (completeness == COMPLETE) {
			counters.completenessDomainsExcluded++;
		} else {
			counters.completenessDomainsUnresolved++;
		}
		if (solutions.isEmpty()) {
			return unresolved(problem, counters, NOT_ESTABLISHED,
					"Even root has no sign-changing certified bracket");
		}
		return result(problem, SUCCESS, completeness, FINITE,
				SupportLevel.CERTIFIED, NumericGuarantee.CERTIFIED_ERROR_BOUND,
				solutions, counters, unresolvedRoots == 0
						? List.of("Every simple root bracketed by sign and continuity")
						: List.of("Verified simple roots retained; even contact unresolved"));
	}

	static Result derivativeAware(Problem problem) {
		WorkCounters counters = new WorkCounters();
		counters.completenessEstablishmentChecks++;
		if (problem.proof() == null || problem.proof().identicallyZero()) {
			counters.completenessDomainsUnresolved++;
			return unresolved(problem, counters, NOT_ESTABLISHED,
					"Derivative search lacks exhaustive component certificate");
		}
		List<RootProof> expectedRoots = rootsInDomain(problem);
		List<Solution> solutions = new ArrayList<>();
		for (RootProof root : expectedRoots) {
			counters.semanticDerivativeCalls++;
			counters.targetDerivativeEvaluations++;
			Solution solution = verifyKnownRoot(problem, root,
					Method.DERIVATIVE_AWARE, ESTABLISHED,
					NumericGuarantee.ESTIMATED_ERROR, counters);
			if (solution != null) {
				solutions.add(solution);
			}
		}
		if (solutions.size() != expectedRoots.size()) {
			counters.completenessDomainsUnresolved++;
			GeometryKind kind = solutions.isEmpty() ? UNRESOLVED : FINITE;
			return result(problem, ComputationStatus.NUMERICAL_FAILURE,
					INCOMPLETE, kind, SupportLevel.UNSUPPORTED,
					NumericGuarantee.FLOATING_POINT_UNCERTIFIED, solutions,
					counters, List.of("Derivative candidate failed independent verification"));
		}
		counters.completenessDomainsExcluded++;
		return completeFiniteOrEmpty(problem, solutions, counters,
				SupportLevel.VERIFIED_UNCERTIFIED,
				NumericGuarantee.ESTIMATED_ERROR,
				List.of("Derivative contacts plus factor-count completeness evidence"));
	}

	static Result evaluatorOnly(Problem problem, int subdivisions) {
		WorkCounters counters = new WorkCounters();
		counters.queryLocalIndexBuilds++;
		counters.indexMisses++;
		counters.completenessEstablishmentChecks++;
		if (!Double.isFinite(problem.lower()) || !Double.isFinite(problem.upper())) {
			counters.completenessDomainsUnresolved++;
			return unresolved(problem, counters, NOT_ESTABLISHED,
					"Evaluator-only search cannot exhaust an unbounded domain");
		}
		if (subdivisions < 1
				|| subdivisions > problem.budget().maximumIsolationSubdivisions()) {
			counters.failedPrivateComputations++;
			return result(problem, ComputationStatus.WORK_LIMIT_REACHED,
					NOT_ESTABLISHED, UNRESOLVED, SupportLevel.UNSUPPORTED,
					NumericGuarantee.FLOATING_POINT_UNCERTIFIED, List.of(),
					counters, List.of("Subdivision work limit reached"));
		}
		List<Double> parameters = new ArrayList<>();
		double step = (problem.upper() - problem.lower()) / subdivisions;
		double previousParameter = problem.lower();
		double previousResidual = evaluatedResidual(problem, previousParameter,
				counters);
		for (int index = 1; index <= subdivisions; index++) {
			counters.rootIsolationSubdivisions++;
			double parameter = index == subdivisions ? problem.upper()
					: problem.lower() + index * step;
			double residual = evaluatedResidual(problem, parameter, counters);
			if (Double.isFinite(previousResidual) && Double.isFinite(residual)) {
				if (oppositeSigns(previousResidual, residual)) {
					parameters.add(bisectTarget(problem, previousParameter,
							parameter, counters));
				} else if (Math.abs(residual)
						<= problem.policy().absoluteResidualTolerance()) {
					parameters.add(parameter);
				}
			}
			previousParameter = parameter;
			previousResidual = residual;
		}
		List<Double> deduplicated = deduplicate(parameters, problem, counters);
		List<Solution> solutions = new ArrayList<>();
		for (int index = 0; index < deduplicated.size(); index++) {
			double parameter = deduplicated.get(index);
			RootProof unproved = new RootProof("evaluator-candidate-" + index,
					parameter, 1);
			Solution solution = verifyKnownRoot(problem, unproved,
					Method.EVALUATOR_ONLY, MultiplicityStatus.NOT_ESTABLISHED,
					NumericGuarantee.FLOATING_POINT_UNCERTIFIED, counters);
			if (solution != null) {
				solutions.add(solution);
			}
		}
		counters.completenessDomainsUnresolved++;
		if (solutions.isEmpty()) {
			return unresolved(problem, counters, NOT_ESTABLISHED,
					"No verified root; evaluator samples do not prove absence");
		}
		return result(problem, SUCCESS, NOT_ESTABLISHED, FINITE,
				SupportLevel.VERIFIED_UNCERTIFIED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, solutions, counters,
				List.of("Verified candidates; exhaustive isolation not established"));
	}

	static Result conservativeBroadPhase(Problem problem,
			List<ParameterInterval> acceptedIntervals) {
		WorkCounters counters = new WorkCounters();
		counters.queryLocalIndexBuilds++;
		counters.indexMisses++;
		counters.completenessEstablishmentChecks++;
		if (problem.proof() == null || problem.proof().identicallyZero()) {
			counters.completenessDomainsUnresolved++;
			return unresolved(problem, counters, NOT_ESTABLISHED,
					"Broad phase cannot establish overlap or completeness alone");
		}
		List<Solution> solutions = new ArrayList<>();
		int omitted = 0;
		for (RootProof root : rootsInDomain(problem)) {
			counters.candidateBoxesCreated++;
			boolean accepted = acceptedIntervals.stream()
					.anyMatch(interval -> interval.contains(root.parameter()));
			if (accepted) {
				counters.broadPhaseCandidatesAccepted++;
				Solution solution = verifyKnownRoot(problem, root,
						Method.CONSERVATIVE_BROAD_PHASE, ESTABLISHED,
						NumericGuarantee.ESTIMATED_ERROR, counters);
				if (solution != null) {
					solutions.add(solution);
				}
			} else {
				counters.broadPhaseCandidatesRejected++;
				counters.unresolvedCandidates++;
				omitted++;
			}
		}
		Completeness completeness = omitted == 0 ? COMPLETE : INCOMPLETE;
		if (omitted == 0) {
			counters.completenessDomainsExcluded++;
		} else {
			counters.completenessDomainsUnresolved++;
		}
		if (solutions.isEmpty()) {
			return unresolved(problem, counters, NOT_ESTABLISHED,
					"Broad phase rejected every semantic candidate");
		}
		return result(problem, SUCCESS, completeness, FINITE,
				SupportLevel.VERIFIED_UNCERTIFIED,
				NumericGuarantee.ESTIMATED_ERROR, solutions, counters,
				List.of(omitted == 0
						? "Conservative coverage independently checked against proof"
						: "Returned roots verified; omitted domain makes set incomplete"));
	}

	record ParameterInterval(double lower, double upper) {
		ParameterInterval {
			if (!Double.isFinite(lower) || !Double.isFinite(upper)
					|| lower > upper) {
				throw new IllegalArgumentException("Invalid candidate interval");
			}
		}

		boolean contains(double parameter) {
			return parameter >= lower && parameter <= upper;
		}
	}

	private static List<RootProof> rootsInDomain(Problem problem) {
		List<RootProof> roots = problem.proof().roots().stream().filter(root -> {
			double parameter = root.parameter();
			boolean lower = parameter > problem.lower()
					|| problem.lowerIncluded() && parameter == problem.lower();
			boolean upper = parameter < problem.upper()
					|| problem.upperIncluded() && parameter == problem.upper();
			return lower && upper;
		}).toList();
		if (!problem.periodic()) {
			return roots;
		}
		boolean lowerSeamPresent = roots.stream().anyMatch(
				root -> root.parameter() == problem.lower());
		if (!lowerSeamPresent) {
			return roots;
		}
		return roots.stream().filter(root -> root.parameter() != problem.upper())
				.toList();
	}

	private static Solution verifyKnownRoot(Problem problem, RootProof root,
			Method method,
			G8AIntersectionSemanticModel.MultiplicityStatus multiplicityStatus,
			NumericGuarantee guarantee, WorkCounters counters) {
		if (counters.verifiedFiniteSolutions
				>= problem.budget().maximumPublishedFiniteSolutions()) {
			counters.unresolvedCandidates++;
			return null;
		}
		counters.candidateIntervalsCreated++;
		counters.residualVerificationCalls++;
		counters.semanticEvaluatorCalls++;
		LocusPoint2D point;
		try {
			point = problem.curve().evaluate(root.parameter());
		} catch (RuntimeException exception) {
			counters.failedPrivateComputations++;
			return null;
		}
		if (point == null || !Double.isFinite(point.getX())
				|| !Double.isFinite(point.getY())) {
			counters.rejectedCandidates++;
			return null;
		}
		counters.targetEquationEvaluations++;
		double residual = Math.abs(problem.target().normalizedResidual(point));
		double acceptance = problem.policy().absoluteResidualTolerance()
				+ problem.policy().relativeResidualTolerance();
		if (!Double.isFinite(residual) || residual > acceptance) {
			counters.rejectedCandidates++;
			return null;
		}
		counters.targetMembershipChecks++;
		if (!problem.target().isMember(point,
				problem.policy().coordinateVerificationTolerance())) {
			counters.rejectedCandidates++;
			return null;
		}
		counters.verifiedFiniteSolutions++;
		counters.verifiedRootCount++;
		double radius = problem.policy().rootParameterTolerance();
		double lower = Math.max(problem.lower(), root.parameter() - radius);
		double upper = Math.min(problem.upper(), root.parameter() + radius);
		DomainLocation location = domainLocation(problem, root.parameter());
		Classification classification;
		if (multiplicityStatus == ESTABLISHED) {
			classification = new Classification(root.multiplicity() == 1
					? TRANSVERSE_ESTABLISHED : TANGENT_ESTABLISHED,
					ESTABLISHED, root.multiplicity(), location);
		} else {
			classification = new Classification(CONTACT_UNDETERMINED,
					MultiplicityStatus.NOT_ESTABLISHED, 0, location);
		}
		String rootToken = problem.sourcePairIdentity() + "/"
				+ root.semanticLabel();
		DurableIdentity identity = new DurableIdentity(rootToken,
				problem.sourcePairIdentity(), "intersection-lineage/v1",
				problem.branchLineage(), problem.topologyContext(),
				NEW_TOPOLOGICAL_SOLUTION);
		RevisionEvidence evidence = new RevisionEvidence(problem.locusRevision(),
				problem.targetUpdateStamp(), root.parameter(), lower, upper,
				residual, method, guarantee);
		return new Solution(identity, evidence, problem.branchKey(),
				problem.componentKey(), point, classification,
				new RootLineage(APPEARED, List.of(), List.of(rootToken), true),
				List.of("independent semantic and target verification passed"));
	}

	private static Result completeFiniteOrEmpty(Problem problem,
			List<Solution> solutions, WorkCounters counters,
			SupportLevel supportLevel, NumericGuarantee guarantee,
			List<String> diagnostics) {
		GeometryKind kind = solutions.isEmpty() ? EMPTY : FINITE;
		return result(problem, SUCCESS, COMPLETE, kind, supportLevel, guarantee,
				solutions, counters, diagnostics);
	}

	private static Result unresolved(Problem problem, WorkCounters counters,
			Completeness completeness, String diagnostic) {
		return result(problem, SUCCESS, completeness, UNRESOLVED,
				SupportLevel.UNSUPPORTED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, List.of(), counters,
				List.of(diagnostic));
	}

	private static Result result(Problem problem, ComputationStatus status,
			Completeness completeness, GeometryKind kind,
			SupportLevel supportLevel, NumericGuarantee guarantee,
			List<Solution> solutions, WorkCounters counters,
			List<String> diagnostics) {
		if (kind == FINITE || kind == EMPTY || kind == OVERLAP) {
			counters.publishedSnapshots++;
		}
		switch (completeness) {
		case COMPLETE -> counters.completenessCompleteResults++;
		case INCOMPLETE -> counters.completenessIncompleteResults++;
		case NOT_ESTABLISHED -> counters.completenessNotEstablishedResults++;
		default -> throw new IllegalStateException("Unknown completeness axis");
		}
		SourceBinding binding = new SourceBinding(problem.sourcePairIdentity(),
				problem.locusIdentity(), problem.locusRevision(),
				problem.targetIdentity(), problem.targetUpdateStamp(),
				problem.policy().policyId());
		return new Result(binding, status, completeness, kind, CURRENT,
				supportLevel, guarantee, solutions, counters.snapshot(), diagnostics);
	}

	private static DomainLocation domainLocation(Problem problem,
			double parameter) {
		if (problem.periodic()
				&& (parameter == problem.lower() || parameter == problem.upper())) {
			return DomainLocation.PERIODIC_SEAM;
		}
		if (problem.lower() == problem.upper()) {
			return DomainLocation.ISOLATED_COMPONENT;
		}
		if (parameter == problem.lower() || parameter == problem.upper()) {
			return DomainLocation.INCLUDED_ENDPOINT;
		}
		return DomainLocation.INTERIOR;
	}

	private static double safeBracketRadius(Problem problem, RootProof root) {
		double radius = Math.max(problem.policy().rootParameterTolerance() * 16,
				Math.ulp(Math.max(1, Math.abs(root.parameter()))) * 64);
		List<RootProof> roots = rootsInDomain(problem);
		for (RootProof other : roots) {
			if (other != root) {
				radius = Math.min(radius,
						Math.abs(other.parameter() - root.parameter()) / 3);
			}
		}
		return Math.max(radius,
				problem.policy().rootParameterTolerance() * 2);
	}

	private static double bisect(Problem problem, double left, double right,
			WorkCounters counters) {
		double leftValue = problem.proof().evaluate(left);
		double rightValue = problem.proof().evaluate(right);
		counters.rootRefinementCalls++;
		if (!oppositeSigns(leftValue, rightValue)) {
			return (left + right) / 2;
		}
		for (int iteration = 0;
				iteration < problem.budget().maximumRefinementIterationsPerCandidate();
				iteration++) {
			counters.rootRefinementIterations++;
			double middle = (left + right) / 2;
			double middleValue = problem.proof().evaluate(middle);
			if (middleValue == 0 || right - left
					<= problem.policy().rootParameterTolerance()) {
				return middle;
			}
			if (oppositeSigns(leftValue, middleValue)) {
				right = middle;
				rightValue = middleValue;
			} else {
				left = middle;
				leftValue = middleValue;
			}
		}
		return Math.abs(leftValue) <= Math.abs(rightValue) ? left : right;
	}

	private static double bisectTarget(Problem problem, double left,
			double right, WorkCounters counters) {
		double leftValue = evaluatedResidual(problem, left, counters);
		counters.rootRefinementCalls++;
		for (int iteration = 0;
				iteration < problem.budget().maximumRefinementIterationsPerCandidate();
				iteration++) {
			counters.rootRefinementIterations++;
			double middle = (left + right) / 2;
			double middleValue = evaluatedResidual(problem, middle, counters);
			if (middleValue == 0 || right - left
					<= problem.policy().rootParameterTolerance()) {
				return middle;
			}
			if (oppositeSigns(leftValue, middleValue)) {
				right = middle;
			} else {
				left = middle;
				leftValue = middleValue;
			}
		}
		return (left + right) / 2;
	}

	private static double evaluatedResidual(Problem problem, double parameter,
			WorkCounters counters) {
		if (counters.semanticEvaluatorCalls
				>= problem.budget().maximumSemanticEvaluations()
				|| counters.targetEquationEvaluations
						>= problem.budget().maximumTargetEvaluations()) {
			return Double.NaN;
		}
		counters.semanticEvaluatorCalls++;
		LocusPoint2D point;
		try {
			point = problem.curve().evaluate(parameter);
		} catch (RuntimeException exception) {
			counters.failedPrivateComputations++;
			return Double.NaN;
		}
		if (point == null || !Double.isFinite(point.getX())
				|| !Double.isFinite(point.getY())) {
			return Double.NaN;
		}
		counters.targetEquationEvaluations++;
		return problem.target().normalizedResidual(point);
	}

	private static List<Double> deduplicate(List<Double> parameters,
			Problem problem, WorkCounters counters) {
		List<Double> sorted = parameters.stream().sorted().toList();
		List<Double> result = new ArrayList<>();
		for (double parameter : sorted) {
			boolean duplicate = false;
			for (double retained : result) {
				counters.deduplicationComparisons++;
				if (Math.abs(parameter - retained)
						<= problem.policy().deduplicationParameterTolerance()) {
					duplicate = true;
					break;
				}
			}
			if (!duplicate) {
				result.add(parameter);
			}
		}
		return result;
	}

	private static boolean oppositeSigns(double first, double second) {
		return first < 0 && second > 0 || first > 0 && second < 0;
	}
}
