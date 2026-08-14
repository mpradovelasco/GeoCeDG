/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;

import org.geocedg.common.kernel.locus.LocusBranch2D;
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
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Currentness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DomainLocation;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MembershipStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;

/** Query-local G8B isolation, refinement and independent verification engine. */
public final class LocusIntersectionSolver2D {
	private final EvaluatorOnlyIntersectionCapability2D evaluatorFallback =
			new EvaluatorOnlyIntersectionCapability2D();

	/**
	 * Computes one private immutable result without shared intersection state.
	 *
	 * @param preferred strongest semantic capability available to the caller
	 * @return verified rich intersection result for the captured revision
	 */
	public LocusIntersectionResult2D intersect(LocusIntersectionQuery2D query,
			LocusDefinition2D definition, LocusIntersectionTarget2D target,
			IntersectionSourceBinding2D binding,
			LocusIntersectionCapability2D preferred,
			IntersectionRootTokenSource2D tokenSource) {
		LocusIntersectionInstrumentation2D instrumentation =
				new LocusIntersectionInstrumentation2D(
						query.getPolicy().getWorkBudget());
		try {
			validateBinding(query, definition, target);
			if (!binding.getSourcePairIdentity()
					.equals(query.getSourcePairIdentity())
					|| binding.getLocusSemanticRevision()
							!= query.getLocusSemanticRevision()
					|| binding.getTargetUpdateStamp()
							!= query.getTargetUpdateStamp()
					|| binding.getTargetFamily() != target.getFamily()) {
				throw new IllegalArgumentException(
						"Published binding disagrees with captured query");
			}
			if (definition.getDefinitionStatus() == DefinitionStatus.EMPTY_DOMAIN
					|| allComponents(definition).isEmpty()) {
				return publish(binding, ComputationStatus.SUCCESS,
						Completeness.COMPLETE,
						CompletenessMethod.CERTIFIED_DOMAIN_EXCLUSION,
						GeometryKind.EMPTY, SupportLevel.CERTIFIED,
						NumericGuarantee.CERTIFIED_ERROR_BOUND,
						Collections.emptyList(), Collections.emptyList(),
						Collections.emptyList(), instrumentation,
						List.of(new IntersectionDiagnostic2D(
								DiagnosticCode.DOMAIN_EXCLUSION_ESTABLISHED,
								"Semantic definition has no valid components")));
			}
			int sessionCapacity = (int) Math.min(8_192,
					query.getPolicy().getWorkBudget()
							.getMaximumSemanticEvaluations());
			try (LocusEvaluationSession2D session =
					LocusEvaluationSession2D.memoizing(sessionCapacity)) {
				IntersectionCapabilityContext2D context =
						new IntersectionCapabilityContext2D(query, definition,
								target, session, instrumentation);
				LocusIntersectionCapability2D selected = preferred != null
						&& preferred.supports(context) ? preferred : evaluatorFallback;
				IntersectionCandidateSet2D candidateSet =
						selected.isolate(context);
				return verifyAndPublish(binding, context, candidateSet, tokenSource);
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
					"Private intersection computation failed coherently: "
							+ exception.getClass().getSimpleName(), instrumentation);
		}
	}

	private static LocusIntersectionResult2D verifyAndPublish(
			IntersectionSourceBinding2D binding,
			IntersectionCapabilityContext2D context,
			IntersectionCandidateSet2D candidateSet,
			IntersectionRootTokenSource2D tokenSource) {
		Completeness completeness = candidateSet.getCompleteness();
		CompletenessMethod completenessMethod =
				candidateSet.getCompletenessMethod();
		GeometryKind kind = candidateSet.getGeometryKind();
		List<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(candidateSet.getDiagnostics());
		List<String> allComponents = context.getAllComponentKeys();
		if (completeness == Completeness.COMPLETE
				&& !sameCoverage(allComponents,
						candidateSet.getCoveredComponentKeys())) {
			completeness = Completeness.INCOMPLETE;
			completenessMethod =
					CompletenessMethod.INCOMPLETE_CANDIDATE_COVERAGE;
			if (kind == GeometryKind.EMPTY) {
				kind = GeometryKind.UNRESOLVED;
			} else if (kind == GeometryKind.OVERLAP
					|| kind == GeometryKind.INFINITELY_MANY) {
				kind = GeometryKind.UNSUPPORTED_OVERLAP;
			}
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.COVERAGE_NOT_ESTABLISHED,
					"Capability did not cover every current semantic component"));
		}
		boolean overlapKind = kind == GeometryKind.OVERLAP
				|| kind == GeometryKind.INFINITELY_MANY
				|| kind == GeometryKind.UNSUPPORTED_OVERLAP;
		if (kind != GeometryKind.FINITE && !overlapKind) {
			return publish(binding, ComputationStatus.SUCCESS, completeness,
					completenessMethod, kind, candidateSet.getSupportLevel(),
					candidateSet.getNumericGuarantee(), Collections.emptyList(),
					candidateSet.getOverlapEvidence(),
					candidateSet.getCoveredComponentKeys(),
					context.getInstrumentation(), diagnostics);
		}

		List<IntersectionCandidate2D> candidates = deduplicate(context,
				candidateSet.getCandidates());
		List<LocusIntersectionSolution2D> solutions = new ArrayList<>();
		boolean unresolvedCandidate = false;
		for (IntersectionCandidate2D candidate : candidates) {
			Verification verification = verifyCandidate(context, candidate,
					tokenSource);
			if (verification.solution != null) {
				solutions.add(verification.solution);
			} else if (!verification.safeMembershipExclusion) {
				unresolvedCandidate = true;
				context.getInstrumentation().recordUnresolvedCandidate();
			}
		}
		if (unresolvedCandidate && completeness == Completeness.COMPLETE) {
			completeness = Completeness.INCOMPLETE;
			completenessMethod =
					CompletenessMethod.INCOMPLETE_CANDIDATE_COVERAGE;
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.CANDIDATE_REJECTED,
					"At least one exhaustive candidate could not be verified"));
		}
		GeometryKind publishedKind;
		if (overlapKind) {
			publishedKind = kind;
		} else if (!solutions.isEmpty()) {
			publishedKind = GeometryKind.FINITE;
		} else if (completeness == Completeness.COMPLETE) {
			publishedKind = GeometryKind.EMPTY;
		} else {
			publishedKind = GeometryKind.UNRESOLVED;
		}
		return publish(binding, ComputationStatus.SUCCESS, completeness,
				completenessMethod, publishedKind,
				candidateSet.getSupportLevel(), candidateSet.getNumericGuarantee(),
				solutions, candidateSet.getOverlapEvidence(),
				candidateSet.getCoveredComponentKeys(),
				context.getInstrumentation(), diagnostics);
	}

	private static Verification verifyCandidate(
			IntersectionCapabilityContext2D context,
			IntersectionCandidate2D candidate,
			IntersectionRootTokenSource2D tokenSource) {
		ComponentAddress address = resolveComponent(context.getDefinition(),
				candidate.getBranchKey(), candidate.getComponentKey());
		if (address == null) {
			context.getInstrumentation().recordRejectedCandidate();
			return Verification.unresolved();
		}
		double canonical = context.getDefinition().getProvider()
				.canonicalize(candidate.getSemanticParameter());
		if (!address.interval.contains(canonical,
				context.getDefinition().getProvider().getDomainEpsilon())) {
			context.getInstrumentation().recordRejectedCandidate();
			return Verification.unresolved();
		}
		LocusEvaluation2D evaluation = context.evaluate(candidate.getBranchKey(),
				canonical);
		if (!evaluation.isValid() || evaluation.getPoint() == null
				|| !finite(evaluation.getPoint())) {
			context.getInstrumentation().recordRejectedCandidate();
			return Verification.unresolved();
		}
		TargetResidual2D residual;
		try {
			context.getInstrumentation().recordResidualVerification();
			residual = context.evaluateResidual(evaluation.getPoint());
		} catch (IllegalArgumentException exception) {
			context.getInstrumentation().recordRejectedCandidate();
			return Verification.unresolved();
		}
		if (!residualCompatible(context.getQuery().getPolicy(), residual)
				|| Math.abs(residual.getNormalizedResidual())
						> context.getQuery().getPolicy().getResidualTolerance()
								.threshold(residual.getCharacteristicScale())) {
			context.getInstrumentation().recordRejectedCandidate();
			return Verification.unresolved();
		}
		context.getInstrumentation().recordMembershipCheck();
		TargetMembership2D membership = context.getTarget().evaluateMembership(
				evaluation.getPoint(), context.getQuery().getPolicy()
						.getCoordinateTolerance().getValue());
		if (membership.getStatus() == MembershipStatus.NOT_MEMBER) {
			context.getInstrumentation().recordRejectedCandidate();
			return Verification.safeMembershipExclusion();
		}
		if (membership.getStatus() != MembershipStatus.MEMBER) {
			context.getInstrumentation().recordRejectedCandidate();
			return Verification.unresolved();
		}
		context.getInstrumentation().recordVerifiedSolution();
		String token = tokenSource.nextToken();
		IdentityStatus identityStatus = candidate.getContinuationKey().isPresent()
				? IdentityStatus.NEW_TOPOLOGICAL_SOLUTION
				: IdentityStatus.NOT_ESTABLISHED;
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				token, context.getQuery().getSourcePairIdentity(),
				context.getQuery().getConstructiveIntersectionLineage(),
				candidate.getBranchKey(), context.getQuery().getTopologyContext(),
				candidate.getContinuationKey(), identityStatus);
		OptionalDouble lifted = candidate.getLiftedPeriodicParameter();
		IntersectionParameterInterval2D interval =
				candidate.getIsolatingInterval().contains(canonical, 0)
						? candidate.getIsolatingInterval()
						: new IntersectionParameterInterval2D(canonical, canonical);
		IntersectionRootRevisionEvidence2D revisionEvidence =
				new IntersectionRootRevisionEvidence2D(
						context.getQuery().getLocusSemanticRevision(),
						context.getQuery().getTargetUpdateStamp(),
						candidate.getBranchKey(), candidate.getComponentKey(),
						canonical, lifted, interval,
						candidate.getLocalIsolationStatus(), residual,
						candidate.getSolverMethod(),
						candidate.getNumericGuarantee());
		IntersectionClassification2D classification =
				new IntersectionClassification2D(candidate.getContactClass(),
						candidate.getMultiplicityStatus(),
						candidate.getEstablishedMultiplicity(),
						domainLocation(context, address.interval, canonical),
						membership.isIncludedBoundary(), Regularity.UNKNOWN);
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(
				candidate.getLineageEventKind(), Collections.emptyList(),
				List.of(token), candidate.getCandidateParentContinuationKeys(),
				false);
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(candidate.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.CONTINUATION_ESTABLISHED,
				"Independent semantic evaluation, normalized residual and target "
						+ "membership verification passed"));
		return Verification.accepted(new LocusIntersectionSolution2D(identity,
				revisionEvidence, evaluation.getPoint(), classification, lineage,
				diagnostics));
	}

	private static List<IntersectionCandidate2D> deduplicate(
			IntersectionCapabilityContext2D context,
			List<IntersectionCandidate2D> input) {
		ArrayList<IntersectionCandidate2D> result = new ArrayList<>();
		double tolerance = context.getQuery().getPolicy()
				.getDeduplicationTolerance().getValue();
		for (IntersectionCandidate2D candidate : input) {
			boolean duplicate = false;
			for (IntersectionCandidate2D retained : result) {
				if (!candidate.getBranchKey().equals(retained.getBranchKey())
						|| !candidate.getComponentKey()
								.equals(retained.getComponentKey())) {
					continue;
				}
				if (candidate.getContinuationKey().isPresent()
						&& retained.getContinuationKey().isPresent()
						&& !candidate.getContinuationKey().get()
								.equals(retained.getContinuationKey().get())) {
					continue;
				}
				context.getInstrumentation().recordDeduplicationComparison();
				if (Math.abs(candidate.getSemanticParameter()
						- retained.getSemanticParameter()) <= tolerance
						&& candidate.getIsolatingInterval().overlaps(
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

	private static LocusIntersectionResult2D publish(
			IntersectionSourceBinding2D binding, ComputationStatus status,
			Completeness completeness, CompletenessMethod completenessMethod,
			GeometryKind kind, SupportLevel supportLevel,
			NumericGuarantee guarantee,
			List<LocusIntersectionSolution2D> solutions,
			List<IntersectionOverlapEvidence2D> overlap,
			List<String> coveredComponents,
			LocusIntersectionInstrumentation2D instrumentation,
			List<IntersectionDiagnostic2D> diagnostics) {
		instrumentation.recordPublishedSnapshot();
		IntersectionCompletenessEvidence2D completenessEvidence =
				new IntersectionCompletenessEvidence2D(completeness,
						completenessMethod, solutions.size(), coveredComponents,
						diagnostics);
		return new LocusIntersectionResult2D(binding, status,
				completenessEvidence, kind, Currentness.CURRENT, supportLevel,
				guarantee, solutions, overlap, instrumentation.snapshot(),
				diagnostics);
	}

	private static LocusIntersectionResult2D failure(
			IntersectionSourceBinding2D binding, ComputationStatus status,
			DiagnosticCode code, String message,
			LocusIntersectionInstrumentation2D instrumentation) {
		return publish(binding, status, Completeness.NOT_ESTABLISHED,
				CompletenessMethod.NOT_ESTABLISHED, GeometryKind.UNRESOLVED,
				SupportLevel.UNSUPPORTED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				Collections.emptyList(), Collections.emptyList(),
				Collections.emptyList(), instrumentation,
				List.of(new IntersectionDiagnostic2D(code, message)));
	}

	private static void validateBinding(LocusIntersectionQuery2D query,
			LocusDefinition2D definition, LocusIntersectionTarget2D target) {
		if (!query.getLocusIdentity().equals(definition.getLocusIdentity())
				|| query.getLocusSemanticRevision()
						!= definition.getSemanticRevision()
				|| !query.getTargetIdentity().equals(target.getTargetIdentity())
				|| query.getTargetUpdateStamp() != target.getTargetUpdateStamp()) {
			throw new IllegalArgumentException("Captured source revisions disagree");
		}
		if (definition.getDefinitionStatus() != DefinitionStatus.VALID
				&& definition.getDefinitionStatus()
						!= DefinitionStatus.EMPTY_DOMAIN) {
			throw new IllegalArgumentException("Locus definition is not valid");
		}
		IntersectionResidualContract2D contract = target.getResidualContract();
		LocusIntersectionPolicy2D.ResidualTolerance tolerance =
				query.getPolicy().getResidualTolerance();
		if (contract.getQuantityKind() != tolerance.getQuantityKind()
				|| !contract.getUnits().equals(tolerance.getUnits())) {
			throw new IllegalArgumentException(
					"Target residual and tolerance quantities are incompatible");
		}
	}

	private static boolean residualCompatible(LocusIntersectionPolicy2D policy,
			TargetResidual2D residual) {
		return residual.getContract().getQuantityKind()
					== policy.getResidualTolerance().getQuantityKind()
				&& residual.getContract().getUnits()
						.equals(policy.getResidualTolerance().getUnits());
	}

	private static boolean sameCoverage(List<String> expected,
			List<String> actual) {
		return expected.size() == actual.size()
				&& new HashSet<>(expected).equals(new HashSet<>(actual));
	}

	private static List<String> allComponents(LocusDefinition2D definition) {
		ArrayList<String> keys = new ArrayList<>();
		for (LocusBranch2D branch : definition.getBranches()) {
			for (int index = 0;
					index < branch.getValidDomainComponents().size(); index++) {
				keys.add(IntersectionCapabilityContext2D.componentKey(
						branch.getBranchKey(), index));
			}
		}
		return keys;
	}

	private static ComponentAddress resolveComponent(
			LocusDefinition2D definition, String branchKey, String componentKey) {
		LocusBranch2D branch = definition.getBranch(branchKey);
		if (branch == null) {
			return null;
		}
		for (int index = 0; index < branch.getValidDomainComponents().size();
				index++) {
			if (IntersectionCapabilityContext2D.componentKey(branchKey, index)
					.equals(componentKey)) {
				return new ComponentAddress(
						branch.getValidDomainComponents().get(index));
			}
		}
		return null;
	}

	private static DomainLocation domainLocation(
			IntersectionCapabilityContext2D context, LocusInterval2D interval,
			double parameter) {
		double tolerance = context.getQuery().getPolicy()
				.getRootParameterTolerance().getValue();
		if (interval.getLower() == interval.getUpper()) {
			return DomainLocation.ISOLATED_COMPONENT;
		}
		if (context.getDefinition().getProvider().isPeriodic()
				&& Math.abs(parameter - context.getDefinition().getProvider()
						.getDeclaredDomain().getLower()) <= tolerance) {
			return DomainLocation.PERIODIC_SEAM;
		}
		if (interval.isLowerClosed()
				&& Math.abs(parameter - interval.getLower()) <= tolerance
				|| interval.isUpperClosed()
				&& Math.abs(parameter - interval.getUpper()) <= tolerance) {
			return DomainLocation.INCLUDED_ENDPOINT;
		}
		return DomainLocation.INTERIOR;
	}

	private static boolean finite(LocusPoint2D point) {
		return Double.isFinite(point.getX()) && Double.isFinite(point.getY());
	}

	private static final class ComponentAddress {
		private final LocusInterval2D interval;

		ComponentAddress(LocusInterval2D interval) {
			this.interval = interval;
		}
	}

	private static final class Verification {
		private final LocusIntersectionSolution2D solution;
		private final boolean safeMembershipExclusion;

		Verification(LocusIntersectionSolution2D solution,
				boolean safeMembershipExclusion) {
			this.solution = solution;
			this.safeMembershipExclusion = safeMembershipExclusion;
		}

		static Verification accepted(LocusIntersectionSolution2D solution) {
			return new Verification(solution, false);
		}

		static Verification safeMembershipExclusion() {
			return new Verification(null, true);
		}

		static Verification unresolved() {
			return new Verification(null, false);
		}
	}
}
