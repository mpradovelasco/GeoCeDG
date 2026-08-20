/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;

/**
 * G9U0 public-command adapter for the approved one-parameter target families.
 *
 * <p>The adapter deliberately reuses the author-approved G8C1 adaptive local
 * proof. The ordinary line, segment, ray and circle adapters expose the same
 * normalized residual, membership and differential-contact contract as the
 * G8C1 extended targets, so they may use that proof without changing the
 * historical G8 solver fallback. The delegated candidate set still reports
 * global completeness as not established and never promotes a tangency or
 * residual-only hit to an admissible token. Public candidate keys replace the
 * G8 revision-local component ordinal with exact branch/component lineage only
 * after validating the delegated G8 explicit unique-root relation. Component
 * lineage alone is never promoted to continuation proof. The public token
 * ledger separately requires compatible provider and target contracts.
 * Isolating intervals and candidate order never enter durable identity.</p>
 *
 * <p>This productive adapter intentionally contains no expression-tree
 * algebra, analytic fixture or sample-derived completeness claim. Closed
 * analytic enumerators remain test-private until a typed productive generator
 * certificate or interval-safe proof authority is approved.</p>
 */
public final class PublicTargetIntersectionCapability2D
		implements LocusIntersectionCapability2D {
	private static final String G8_UNIQUE_ROOT_PREFIX =
			"g8c1/unique-local-root/";
	private static final String DURABLE_UNIQUE_ROOT_PREFIX =
			"g9u0/g8c1-explicit-unique-local-root/v1/";
	private final ExtendedTargetIntersectionCapability2D adaptiveProof =
			new ExtendedTargetIntersectionCapability2D();

	@Override
	public String getCapabilityId() {
		return "g9u0-public-query-local-one-parameter/v1";
	}

	@Override
	public boolean supports(IntersectionCapabilityContext2D context) {
		TargetFamily family = context.getTarget().getFamily();
		return family == TargetFamily.LINE
				|| family == TargetFamily.SEGMENT
				|| family == TargetFamily.RAY
				|| family == TargetFamily.CIRCLE
				|| family == TargetFamily.ELLIPSE
				|| family == TargetFamily.PARABOLA
				|| family == TargetFamily.HYPERBOLA
				|| family == TargetFamily.BOUNDED_FUNCTION_GRAPH
				|| family == TargetFamily.REGULAR_POLYNOMIAL_IMPLICIT;
	}

	@Override
	public IntersectionCandidateSet2D isolate(
			IntersectionCapabilityContext2D context) {
		if (!supports(context)) {
			throw new IllegalArgumentException(
					"Target family is outside the G9U0 public capability");
		}
		IntersectionCandidateSet2D delegated = adaptiveProof.isolate(context);
		ArrayList<IntersectionCandidate2D> publicCandidates = new ArrayList<>();
		for (IntersectionCandidate2D candidate : delegated.getCandidates()) {
			publicCandidates.add(withStableCandidateLineage(context, candidate));
		}
		return new IntersectionCandidateSet2D(delegated.getCompleteness(),
				delegated.getCompletenessMethod(), delegated.getGeometryKind(),
				delegated.getSupportLevel(), delegated.getNumericGuarantee(),
				delegated.getCoveredComponentKeys(), publicCandidates,
				delegated.getOverlapEvidence(), delegated.getDiagnostics());
	}

	private static IntersectionCandidate2D withStableCandidateLineage(
			IntersectionCapabilityContext2D context,
			IntersectionCandidate2D candidate) {
		if (!candidate.getContinuationKey().isPresent()) {
			return candidate;
		}
		String delegatedProof = candidate.getContinuationKey().get();
		String expectedProof = G8_UNIQUE_ROOT_PREFIX
				+ candidate.getComponentKey();
		if (!expectedProof.equals(delegatedProof)) {
			throw new IllegalStateException(
					"Unrecognized delegated root-continuation proof");
		}
		LocusInterval2D component = exactComponent(context, candidate);
		String lineage = IntersectionTokenLineage2D.stableComponentLineage(
				candidate.getBranchKey(), component);
		Optional<String> continuation = Optional.of(
				DURABLE_UNIQUE_ROOT_PREFIX + lineage.length() + ":" + lineage);
		return new IntersectionCandidate2D(candidate.getBranchKey(),
				candidate.getComponentKey(), candidate.getSemanticParameter(),
				candidate.getLiftedPeriodicParameter(),
				candidate.getIsolatingInterval(),
				candidate.getLocalIsolationStatus(), continuation,
				candidate.getContactClass(), candidate.getMultiplicityStatus(),
				candidate.getEstablishedMultiplicity(), candidate.getSolverMethod(),
				candidate.getNumericGuarantee(), candidate.getLineageEventKind(),
				candidate.getCandidateParentContinuationKeys(),
				candidate.getDiagnostics());
	}

	private static LocusInterval2D exactComponent(
			IntersectionCapabilityContext2D context,
			IntersectionCandidate2D candidate) {
		LocusBranch2D branch = context.getDefinition().getBranch(
				candidate.getBranchKey());
		if (branch != null) {
			List<LocusInterval2D> components = branch.getValidDomainComponents();
			for (int index = 0; index < components.size(); index++) {
				if (candidate.getComponentKey().equals(
						IntersectionCapabilityContext2D.componentKey(
								candidate.getBranchKey(), index))) {
					return components.get(index);
				}
			}
		}
		throw new IllegalStateException(
				"Adaptive candidate lost its exact semantic component");
	}
}
