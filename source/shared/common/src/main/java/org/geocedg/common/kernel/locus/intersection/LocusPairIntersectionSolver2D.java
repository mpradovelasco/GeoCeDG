/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Currentness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DomainLocation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ResidualQuantityKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionContext2D.ComponentAddress;

/** Query-local dual-parameter isolation, refinement and publication engine. */
public final class LocusPairIntersectionSolver2D {
	private static final IntersectionResidualContract2D PAIR_RESIDUAL =
			new IntersectionResidualContract2D("g8c2-locus-pair-residual/v1",
					ResidualQuantityKind.MODEL_COORDINATE_DISTANCE,
					"model-coordinate",
					"Euclidean norm of two independently evaluated semantic points",
					"max(1,coordinate-magnitude-of-both-sources)");
	private final EvaluatorPairIntersectionCapability2D evaluatorFallback =
			new EvaluatorPairIntersectionCapability2D();

	/**
	 * Computes one immutable pair result with no retained intersection state.
	 *
	 * @return current atomic rich intersection result
	 */
	public LocusIntersectionResult2D intersect(
			LocusPairIntersectionQuery2D query,
			LocusDefinition2D firstDefinition,
			LocusDefinition2D secondDefinition,
			IntersectionSourceBinding2D binding,
			LocusPairIntersectionCapability2D preferred,
			LocusPairRootTokenSource2D tokenSource) {
		LocusPairIntersectionInstrumentation2D instrumentation =
				new LocusPairIntersectionInstrumentation2D(query.getPolicy());
		try {
			validateBinding(query, firstDefinition, secondDefinition, binding);
			if (empty(firstDefinition) || empty(secondDefinition)) {
				return publish(binding, ComputationStatus.SUCCESS,
						Completeness.COMPLETE,
						CompletenessMethod.CERTIFIED_DOMAIN_EXCLUSION,
						GeometryKind.EMPTY, SupportLevel.CERTIFIED,
						NumericGuarantee.CERTIFIED_ERROR_BOUND,
						Collections.emptyList(), Collections.emptyList(),
						Collections.emptyList(), instrumentation,
						List.of(new IntersectionDiagnostic2D(
								DiagnosticCode.DOMAIN_EXCLUSION_ESTABLISHED,
								"At least one semantic locus has no valid component")));
			}
			int sessionCapacity = (int) Math.min(16_384,
					query.getPolicy().getCommonWorkBudget()
							.getMaximumSemanticEvaluations());
			try (LocusEvaluationSession2D session =
					LocusEvaluationSession2D.memoizing(sessionCapacity)) {
				LocusPairIntersectionContext2D context =
						new LocusPairIntersectionContext2D(query, firstDefinition,
								secondDefinition, session, instrumentation);
				for (int firstBranch = 0;
						firstBranch < firstDefinition.getBranches().size();
						firstBranch++) {
					for (int secondBranch = 0;
							secondBranch < secondDefinition.getBranches().size();
							secondBranch++) {
						instrumentation.recordBranchPair();
					}
				}
				for (int pair = 0; pair < context.getAllComponentPairKeys().size();
						pair++) {
					instrumentation.recordComponentPair();
				}
				LocusPairIntersectionCapability2D capability = preferred != null
						&& preferred.supports(context) ? preferred
								: evaluatorFallback;
				if (!capability.supports(context)) {
					return failure(binding, ComputationStatus.UNSUPPORTED,
							DiagnosticCode.PAIR_DOMAIN_UNSUPPORTED,
							"Only finite components and periodic fundamental domains "
									+ "are supported", instrumentation);
				}
				return verifyAndPublish(binding, context,
						capability.isolate(context),
						java.util.Objects.requireNonNull(tokenSource));
			}
		} catch (LocusIntersectionWorkLimitException exception) {
			instrumentation.recordFailedPrivateComputation();
			return failure(binding, ComputationStatus.WORK_LIMIT_REACHED,
					DiagnosticCode.WORK_LIMIT_REACHED, exception.getMessage(),
					instrumentation);
		} catch (RuntimeException exception) {
			instrumentation.recordFailedPrivateComputation();
			return failure(binding, ComputationStatus.NUMERICAL_FAILURE,
					DiagnosticCode.NUMERICAL_FAILURE,
					"Private pair computation failed coherently: "
							+ exception.getClass().getSimpleName(), instrumentation);
		}
	}

	private static LocusIntersectionResult2D verifyAndPublish(
			IntersectionSourceBinding2D binding,
			LocusPairIntersectionContext2D context,
			LocusPairIntersectionCandidateSet2D candidateSet,
			LocusPairRootTokenSource2D tokenSource) {
		Completeness completeness = candidateSet.getCompleteness();
		CompletenessMethod method = candidateSet.getCompletenessMethod();
		GeometryKind kind = candidateSet.getGeometryKind();
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(candidateSet.getDiagnostics());
		List<String> expectedCoverage = context.getAllComponentPairKeys();
		if (completeness == Completeness.COMPLETE
				&& !sameCoverage(expectedCoverage,
						candidateSet.getCoveredComponentPairKeys())) {
			completeness = Completeness.INCOMPLETE;
			method = CompletenessMethod.INCOMPLETE_CANDIDATE_COVERAGE;
			if (kind == GeometryKind.EMPTY) {
				kind = GeometryKind.UNRESOLVED;
			}
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.PAIR_COVERAGE_NOT_ESTABLISHED,
					"Capability omitted at least one current component product"));
		}

		List<LocusPairIntersectionCandidate2D> candidates = deduplicate(context,
				candidateSet.getCandidates());
		ArrayList<LocusIntersectionSolution2D> solutions = new ArrayList<>();
		HashSet<String> tokens = new HashSet<>();
		boolean unresolved = false;
		for (LocusPairIntersectionCandidate2D candidate : candidates) {
			LocusIntersectionSolution2D solution = verifyCandidate(context,
					candidate, tokenSource);
			if (solution == null) {
				unresolved = true;
				context.getInstrumentation().recordUnresolvedCandidate();
			} else if (!tokens.add(solution.getIdentity().getRootToken())) {
				throw new IllegalArgumentException(
						"Distinct pair lineages produced the same opaque token");
			} else {
				solutions.add(solution);
			}
		}
		if (unresolved && completeness == Completeness.COMPLETE) {
			completeness = Completeness.INCOMPLETE;
			method = CompletenessMethod.INCOMPLETE_CANDIDATE_COVERAGE;
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.CANDIDATE_REJECTED,
					"An exhaustive pair candidate failed independent verification"));
		}
		GeometryKind publishedKind = publishedKind(kind, completeness, solutions,
				candidateSet.getOverlapEvidence());
		return publish(binding, ComputationStatus.SUCCESS, completeness, method,
				publishedKind, candidateSet.getSupportLevel(),
				candidateSet.getNumericGuarantee(), solutions,
				candidateSet.getOverlapEvidence(),
				candidateSet.getCoveredComponentPairKeys(),
				context.getInstrumentation(), diagnostics);
	}

	private static LocusIntersectionSolution2D verifyCandidate(
			LocusPairIntersectionContext2D context,
			LocusPairIntersectionCandidate2D candidate,
			LocusPairRootTokenSource2D tokenSource) {
		ComponentAddress firstComponent = resolve(context.getFirstComponents(),
				candidate.getFirstBranchKey(), candidate.getFirstComponentKey());
		ComponentAddress secondComponent = resolve(context.getSecondComponents(),
				candidate.getSecondBranchKey(), candidate.getSecondComponentKey());
		if (firstComponent == null || secondComponent == null) {
			context.getInstrumentation().recordRejectedCandidate();
			return null;
		}
		double firstParameter = context.getFirstDefinition().getProvider()
				.canonicalize(candidate.getFirstParameter());
		double secondParameter = context.getSecondDefinition().getProvider()
				.canonicalize(candidate.getSecondParameter());
		if (!contains(firstComponent, context.getFirstDefinition(),
				firstParameter)
				|| !contains(secondComponent, context.getSecondDefinition(),
						secondParameter)) {
			context.getInstrumentation().recordRejectedCandidate();
			return null;
		}
		LocusEvaluation2D first = context.evaluateFirst(
				candidate.getFirstBranchKey(), firstParameter);
		LocusEvaluation2D second = context.evaluateSecond(
				candidate.getSecondBranchKey(), secondParameter);
		if (!valid(first) || !valid(second)) {
			context.getInstrumentation().recordRejectedCandidate();
			return null;
		}
		context.getInstrumentation().recordResidualVerification();
		double residual = Math.hypot(first.getPoint().getX()
				- second.getPoint().getX(), first.getPoint().getY()
						- second.getPoint().getY());
		double scale = characteristicScale(first.getPoint(), second.getPoint());
		double threshold = context.getQuery().getPolicy().getResidualTolerance()
				.threshold(scale);
		if (!Double.isFinite(residual) || residual > threshold) {
			context.getInstrumentation().recordRejectedCandidate();
			return null;
		}

		LocusDifferentialEvaluation2D firstDifferential =
				context.evaluateFirstDifferential(candidate.getFirstBranchKey(),
						firstParameter, firstComponent.getInterval());
		LocusDifferentialEvaluation2D secondDifferential =
				context.evaluateSecondDifferential(candidate.getSecondBranchKey(),
						secondParameter, secondComponent.getInterval());
		OptionalDouble normalizedDeterminant = normalizedDeterminant(context,
				firstDifferential, secondDifferential);
		ContactClass contact = verifiedContact(context, candidate,
				normalizedDeterminant);
		MultiplicityStatus multiplicityStatus =
				candidate.getMultiplicityStatus();
		if (contact == ContactClass.CONTACT_UNDETERMINED) {
			multiplicityStatus = MultiplicityStatus.NOT_ESTABLISHED;
		}

		String branchPair = LocusPairIdentity2D.componentPair(
				candidate.getFirstBranchKey(), candidate.getFirstComponentKey(),
				candidate.getSecondBranchKey(), candidate.getSecondComponentKey());
		String token = tokenSource.nextToken(candidate.getSolutionLineageKey(),
				IntersectionTokenLineage2D.forCanonicalComponentPair(
						candidate.getFirstBranchKey(), firstComponent.getInterval(),
						candidate.getSecondBranchKey(), secondComponent.getInterval(),
						candidate.getContinuationKey()));
		boolean identityEstablished = candidate.getContinuationKey().isPresent()
				&& candidate.getLocalIsolation().getStatus()
						== LocalIsolationStatus.ESTABLISHED;
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				token, context.getQuery().getSourcePairIdentity(),
				context.getQuery().getConstructiveIntersectionLineage(), branchPair,
				context.getQuery().getTopologyContext(),
				candidate.getContinuationKey(), identityEstablished
						? IdentityStatus.NEW_TOPOLOGICAL_SOLUTION
						: IdentityStatus.NOT_ESTABLISHED);

		LocusPairSourceRevisionEvidence2D firstEvidence =
				new LocusPairSourceRevisionEvidence2D(
						context.getQuery().getFirst().getLocusIdentity(),
						context.getQuery().getFirst().getSemanticRevision(),
						candidate.getFirstBranchKey(),
						candidate.getFirstComponentKey(), firstParameter,
						candidate.getFirstLiftedParameter(),
						intervalContaining(candidate.getFirstInterval(),
								firstParameter));
		LocusPairSourceRevisionEvidence2D secondEvidence =
				new LocusPairSourceRevisionEvidence2D(
						context.getQuery().getSecond().getLocusIdentity(),
						context.getQuery().getSecond().getSemanticRevision(),
						candidate.getSecondBranchKey(),
						candidate.getSecondComponentKey(), secondParameter,
						candidate.getSecondLiftedParameter(),
						intervalContaining(candidate.getSecondInterval(),
								secondParameter));
		LocusPairResidualEvidence2D residualEvidence =
				new LocusPairResidualEvidence2D(first.getPoint(), second.getPoint(),
						residual, threshold, candidate.getNumericGuarantee());
		LocusPairIntersectionEvidence2D pairEvidence =
				new LocusPairIntersectionEvidence2D(firstEvidence, secondEvidence,
						candidate.getLocalIsolation(), residualEvidence,
						normalizedDeterminant, candidate.getSolverMethod(),
						candidate.getNumericGuarantee());
		TargetResidual2D compatibilityResidual = new TargetResidual2D(residual,
				1, residual, scale, PAIR_RESIDUAL);
		IntersectionRootRevisionEvidence2D compatibilityEvidence =
				new IntersectionRootRevisionEvidence2D(
						firstEvidence.getSemanticRevision(),
						secondEvidence.getSemanticRevision(), branchPair, branchPair,
						firstParameter, firstEvidence.getLiftedPeriodicParameter(),
						firstEvidence.getIsolatingInterval(),
						candidate.getLocalIsolation().getStatus(),
						compatibilityResidual, candidate.getSolverMethod(),
						candidate.getNumericGuarantee(),
						candidate.getContinuationKey());
		IntersectionClassification2D classification =
				new IntersectionClassification2D(contact, multiplicityStatus,
						multiplicityStatus == MultiplicityStatus.ESTABLISHED
								? candidate.getEstablishedMultiplicity()
								: java.util.OptionalInt.empty(),
						domainLocation(context, firstComponent, secondComponent,
								firstParameter, secondParameter), false,
						regularity(firstDifferential, secondDifferential));
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(
				candidate.getLineageEventKind(), Collections.emptyList(),
				List.of(token), candidate.getCandidateParentContinuationKeys(),
				false);
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(candidate.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.CONTINUATION_ESTABLISHED,
				"Independent evaluation of both semantic sources and pair residual "
						+ "verification passed"));
		context.getInstrumentation().recordVerifiedSolution();
		LocusPoint2D publishedPoint = new LocusPoint2D(
				0.5 * (first.getPoint().getX() + second.getPoint().getX()),
				0.5 * (first.getPoint().getY() + second.getPoint().getY()));
		return new LocusIntersectionSolution2D(identity,
				compatibilityEvidence, publishedPoint, classification, lineage,
				diagnostics, Optional.of(pairEvidence));
	}

	private static List<LocusPairIntersectionCandidate2D> deduplicate(
			LocusPairIntersectionContext2D context,
			List<LocusPairIntersectionCandidate2D> input) {
		ArrayList<LocusPairIntersectionCandidate2D> output = new ArrayList<>();
		double firstTolerance = context.getQuery().getPolicy()
				.getFirstDeduplicationTolerance().getValue();
		double secondTolerance = context.getQuery().getPolicy()
				.getSecondDeduplicationTolerance().getValue();
		for (LocusPairIntersectionCandidate2D candidate : input) {
			boolean duplicate = false;
			for (LocusPairIntersectionCandidate2D retained : output) {
				if (!sameComponentPair(candidate, retained)) {
					continue;
				}
				if (candidate.getContinuationKey().isPresent()
						&& retained.getContinuationKey().isPresent()
						&& !candidate.getContinuationKey().get().equals(
								retained.getContinuationKey().get())) {
					continue;
				}
				context.getInstrumentation().recordDeduplicationComparison();
				if (Math.abs(candidate.getFirstParameter()
						- retained.getFirstParameter()) <= firstTolerance
						&& Math.abs(candidate.getSecondParameter()
								- retained.getSecondParameter()) <= secondTolerance
						&& candidate.getFirstInterval().overlaps(
								retained.getFirstInterval(), firstTolerance)
						&& candidate.getSecondInterval().overlaps(
								retained.getSecondInterval(), secondTolerance)) {
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

	private static ContactClass verifiedContact(
			LocusPairIntersectionContext2D context,
			LocusPairIntersectionCandidate2D candidate,
			OptionalDouble determinant) {
		if (!determinant.isPresent()) {
			return ContactClass.CONTACT_UNDETERMINED;
		}
		double magnitude = Math.abs(determinant.getAsDouble());
		double threshold = context.getQuery().getPolicy()
				.getTangencyTolerance().getThreshold();
		if (magnitude > threshold) {
			return ContactClass.TRANSVERSE_ESTABLISHED;
		}
		if (candidate.getContactClass() == ContactClass.TANGENT_ESTABLISHED
				&& (candidate.getNumericGuarantee()
						== NumericGuarantee.EXACT_ARITHMETIC
						|| candidate.getNumericGuarantee()
								== NumericGuarantee.CERTIFIED_ERROR_BOUND)) {
			return ContactClass.TANGENT_ESTABLISHED;
		}
		return ContactClass.CONTACT_UNDETERMINED;
	}

	private static OptionalDouble normalizedDeterminant(
			LocusPairIntersectionContext2D context,
			LocusDifferentialEvaluation2D first,
			LocusDifferentialEvaluation2D second) {
		if (first.getRegularity() != Regularity.REGULAR
				|| second.getRegularity() != Regularity.REGULAR) {
			return OptionalDouble.empty();
		}
		context.getInstrumentation().recordJacobianEvaluation();
		LocusPoint2D firstValue = first.getDerivative();
		LocusPoint2D secondValue = second.getDerivative();
		double denominator = Math.hypot(firstValue.getX(), firstValue.getY())
				* Math.hypot(secondValue.getX(), secondValue.getY());
		if (!Double.isFinite(denominator) || denominator == 0) {
			return OptionalDouble.empty();
		}
		return OptionalDouble.of((firstValue.getX() * secondValue.getY()
				- firstValue.getY() * secondValue.getX()) / denominator);
	}

	private static GeometryKind publishedKind(GeometryKind requested,
			Completeness completeness,
			List<LocusIntersectionSolution2D> solutions,
			List<IntersectionOverlapEvidence2D> overlap) {
		if (!overlap.isEmpty() && !solutions.isEmpty()) {
			return GeometryKind.MIXED_FINITE_OVERLAP;
		}
		if (!overlap.isEmpty()) {
			boolean established = overlap.stream().anyMatch(evidence ->
					evidence.getStatus() == OverlapStatus.OVERLAP_ESTABLISHED);
			return established && (requested == GeometryKind.OVERLAP
					|| requested == GeometryKind.INFINITELY_MANY)
						? requested : GeometryKind.UNSUPPORTED_OVERLAP;
		}
		if (!solutions.isEmpty()) {
			return GeometryKind.FINITE;
		}
		return completeness == Completeness.COMPLETE ? GeometryKind.EMPTY
				: GeometryKind.UNRESOLVED;
	}

	private static LocusIntersectionResult2D publish(
			IntersectionSourceBinding2D binding, ComputationStatus status,
			Completeness completeness, CompletenessMethod method,
			GeometryKind kind, SupportLevel support,
			NumericGuarantee guarantee,
			List<LocusIntersectionSolution2D> solutions,
			List<IntersectionOverlapEvidence2D> overlap,
			List<String> coveredPairs,
			LocusPairIntersectionInstrumentation2D instrumentation,
			List<IntersectionDiagnostic2D> diagnostics) {
		instrumentation.recordPublishedSnapshot();
		return new LocusIntersectionResult2D(binding, status,
				new IntersectionCompletenessEvidence2D(completeness, method,
						solutions.size(), coveredPairs, diagnostics), kind,
				Currentness.CURRENT, support, guarantee, solutions, overlap,
				instrumentation.snapshot(), diagnostics);
	}

	private static LocusIntersectionResult2D failure(
			IntersectionSourceBinding2D binding, ComputationStatus status,
			DiagnosticCode code, String message,
			LocusPairIntersectionInstrumentation2D instrumentation) {
		return publish(binding, status, Completeness.NOT_ESTABLISHED,
				CompletenessMethod.NOT_ESTABLISHED, GeometryKind.UNRESOLVED,
				SupportLevel.UNSUPPORTED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				Collections.emptyList(), Collections.emptyList(),
				Collections.emptyList(), instrumentation,
				List.of(new IntersectionDiagnostic2D(code, message)));
	}

	private static void validateBinding(LocusPairIntersectionQuery2D query,
			LocusDefinition2D first, LocusDefinition2D second,
			IntersectionSourceBinding2D binding) {
		if (!binding.isLocusPair()
				|| !query.getFirst().getLocusIdentity()
						.equals(first.getLocusIdentity())
				|| query.getFirst().getSemanticRevision()
						!= first.getSemanticRevision()
				|| !query.getSecond().getLocusIdentity()
						.equals(second.getLocusIdentity())
				|| query.getSecond().getSemanticRevision()
						!= second.getSemanticRevision()
				|| !binding.getSourcePairIdentity()
						.equals(query.getSourcePairIdentity())
				|| binding.getFirstLocusSemanticRevision()
						!= first.getSemanticRevision()
				|| binding.getSecondLocusSemanticRevision()
						!= second.getSemanticRevision()) {
			throw new IllegalArgumentException(
					"Pair source revisions or canonical binding disagree");
		}
		if (!supportedDefinition(first) || !supportedDefinition(second)) {
			throw new IllegalArgumentException("Pair locus definition is invalid");
		}
	}

	private static boolean supportedDefinition(LocusDefinition2D definition) {
		return definition.getDefinitionStatus() == DefinitionStatus.VALID
				|| definition.getDefinitionStatus()
						== DefinitionStatus.EMPTY_DOMAIN;
	}

	private static boolean empty(LocusDefinition2D definition) {
		return definition.getDefinitionStatus() == DefinitionStatus.EMPTY_DOMAIN
				|| definition.getBranches().stream().allMatch(branch ->
						branch.getValidDomainComponents().isEmpty());
	}

	private static ComponentAddress resolve(List<ComponentAddress> components,
			String branch, String component) {
		for (ComponentAddress value : components) {
			if (value.getBranchKey().equals(branch)
					&& value.getComponentKey().equals(component)) {
				return value;
			}
		}
		return null;
	}

	private static boolean contains(ComponentAddress component,
			LocusDefinition2D definition, double parameter) {
		return component.getInterval().contains(parameter,
				definition.getProvider().getDomainEpsilon());
	}

	private static boolean valid(LocusEvaluation2D evaluation) {
		return evaluation.isValid() && evaluation.getPoint() != null
				&& Double.isFinite(evaluation.getPoint().getX())
				&& Double.isFinite(evaluation.getPoint().getY());
	}

	private static IntersectionParameterInterval2D intervalContaining(
			IntersectionParameterInterval2D interval, double parameter) {
		return interval.contains(parameter, 0) ? interval
				: new IntersectionParameterInterval2D(parameter, parameter);
	}

	private static boolean sameComponentPair(
			LocusPairIntersectionCandidate2D first,
			LocusPairIntersectionCandidate2D second) {
		return first.getFirstBranchKey().equals(second.getFirstBranchKey())
				&& first.getFirstComponentKey()
						.equals(second.getFirstComponentKey())
				&& first.getSecondBranchKey().equals(second.getSecondBranchKey())
				&& first.getSecondComponentKey()
						.equals(second.getSecondComponentKey());
	}

	private static boolean sameCoverage(List<String> expected,
			List<String> actual) {
		return expected.size() == actual.size()
				&& new HashSet<>(expected).equals(new HashSet<>(actual));
	}

	private static double characteristicScale(LocusPoint2D first,
			LocusPoint2D second) {
		return Math.max(1, Math.max(Math.hypot(first.getX(), first.getY()),
				Math.hypot(second.getX(), second.getY())));
	}

	private static Regularity regularity(
			LocusDifferentialEvaluation2D first,
			LocusDifferentialEvaluation2D second) {
		return first.getRegularity() == Regularity.REGULAR
				&& second.getRegularity() == Regularity.REGULAR
						? Regularity.REGULAR : Regularity.UNKNOWN;
	}

	private static DomainLocation domainLocation(
			LocusPairIntersectionContext2D context, ComponentAddress first,
			ComponentAddress second, double firstParameter,
			double secondParameter) {
		if (seam(context.getFirstDefinition(), firstParameter,
				context.getQuery().getPolicy().getFirstRootTolerance().getValue())
				|| seam(context.getSecondDefinition(), secondParameter,
						context.getQuery().getPolicy().getSecondRootTolerance()
								.getValue())) {
			return DomainLocation.PERIODIC_SEAM;
		}
		if (endpoint(first.getInterval(), firstParameter,
				context.getQuery().getPolicy().getFirstRootTolerance().getValue())
				|| endpoint(second.getInterval(), secondParameter,
						context.getQuery().getPolicy().getSecondRootTolerance()
								.getValue())) {
			return DomainLocation.INCLUDED_ENDPOINT;
		}
		return DomainLocation.INTERIOR;
	}

	private static boolean seam(LocusDefinition2D definition,
			double parameter, double tolerance) {
		return definition.getProvider().isPeriodic()
				&& Math.abs(parameter - definition.getProvider()
						.getDeclaredDomain().getLower()) <= tolerance;
	}

	private static boolean endpoint(LocusInterval2D interval,
			double parameter, double tolerance) {
		return interval.isLowerClosed()
				&& Math.abs(parameter - interval.getLower()) <= tolerance
				|| interval.isUpperClosed()
						&& Math.abs(parameter - interval.getUpper()) <= tolerance;
	}
}
