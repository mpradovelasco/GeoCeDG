/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.algos.AlgoLocusLocusIntersectionV2;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionOverlapEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionParameterInterval2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.CompletenessMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapRelationKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.OverlapStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairCoverageStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairIsolationMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairUniquenessStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.LocalPairIsolationEvidence2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionCandidate2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionCandidateSet2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionCapability2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionContext2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionContext2D.ComponentAddress;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;

/** Deterministic fixtures and explicit proof capabilities for G8C2 tests. */
final class G8C2IntersectionTestSupport {
	private G8C2IntersectionTestSupport() {
	}

	static G8BIntersectionFixtures.Fixture curve(Construction construction,
			String identity, double lower, double upper, boolean periodic,
			G8BIntersectionFixtures.Curve curve) {
		return G8BIntersectionFixtures.single(construction, identity, lower,
				upper, true, true, periodic, curve);
	}

	static G8BIntersectionFixtures.Fixture line(Construction construction,
			String identity, double lower, double upper, double offset,
			boolean horizontal) {
		return curve(construction, identity, lower, upper, false,
				(source, branch, parameter) -> horizontal
						? new LocusPoint2D(parameter, source + offset)
						: new LocusPoint2D(source + offset, parameter));
	}

	static AlgoLocusLocusIntersectionV2 algorithm(Construction construction,
			G8BIntersectionFixtures.Fixture first,
			G8BIntersectionFixtures.Fixture second, String identity,
			LocusPairIntersectionCapability2D capability,
			GeoElement[] dependencies) {
		return new AlgoLocusLocusIntersectionV2(construction, first.locus(),
				second.locus(), identity + "/constructive-lineage",
				identity + "/topology", capability, dependencies);
	}

	static AlgoLocusLocusIntersectionV2 algorithm(Construction construction,
			G8BIntersectionFixtures.Fixture first,
			G8BIntersectionFixtures.Fixture second, String identity) {
		return algorithm(construction, first, second, identity, null,
				new GeoElement[0]);
	}

	static LocusIntersectionResult2D result(Construction construction,
			G8BIntersectionFixtures.Fixture first,
			G8BIntersectionFixtures.Fixture second, String identity,
			LocusPairIntersectionCapability2D capability) {
		return algorithm(construction, first, second, identity, capability,
				new GeoElement[0]).getResult().getIntersectionResult();
	}

	static LocusPairIntersectionCapability2D capability(String id,
			CandidateSetProvider provider) {
		return new LocusPairIntersectionCapability2D() {
			@Override
			public String getCapabilityId() {
				return id;
			}

			@Override
			public boolean supports(LocusPairIntersectionContext2D context) {
				return true;
			}

			@Override
			public LocusPairIntersectionCandidateSet2D isolate(
					LocusPairIntersectionContext2D context) {
				return provider.provide(context);
			}
		};
	}

	static PairRoot simple(double first, double second, String key) {
		return new PairRoot(first, second, key, key,
				ContactClass.TRANSVERSE_ESTABLISHED, 1, true,
				LineageEventKind.APPEARED, Collections.emptyList());
	}

	static PairRoot tangent(double first, double second, String key,
			int multiplicity, boolean isolated) {
		return new PairRoot(first, second, key, key,
				ContactClass.TANGENT_ESTABLISHED, multiplicity, isolated,
				LineageEventKind.APPEARED, Collections.emptyList());
	}

	static LocusPairIntersectionCandidateSet2D roots(
			LocusPairIntersectionContext2D context, List<PairRoot> roots,
			Completeness completeness) {
		ArrayList<LocusPairIntersectionCandidate2D> candidates =
				new ArrayList<>();
		for (PairRoot root : roots) {
			ComponentAddress first = context.getFirstComponents().get(0);
			ComponentAddress second = context.getSecondComponents().get(0);
			candidates.add(candidate(context, first, second, root));
		}
		CompletenessMethod method = completeness == Completeness.COMPLETE
				? CompletenessMethod.ANALYTIC_ROOT_ENUMERATION
				: completeness == Completeness.INCOMPLETE
						? CompletenessMethod.INCOMPLETE_CANDIDATE_COVERAGE
						: CompletenessMethod.NOT_ESTABLISHED;
		return new LocusPairIntersectionCandidateSet2D(completeness, method,
				GeometryKind.FINITE, SupportLevel.EXACT_CAPABILITY,
				NumericGuarantee.CERTIFIED_ERROR_BOUND,
				context.getAllComponentPairKeys(), candidates,
				Collections.emptyList(), Collections.emptyList());
	}

	static LocusPairIntersectionCandidateSet2D completeEmpty(
			LocusPairIntersectionContext2D context) {
		return new LocusPairIntersectionCandidateSet2D(Completeness.COMPLETE,
				CompletenessMethod.CERTIFIED_DOMAIN_EXCLUSION,
				GeometryKind.EMPTY, SupportLevel.CERTIFIED,
				NumericGuarantee.CERTIFIED_ERROR_BOUND,
				context.getAllComponentPairKeys(), Collections.emptyList(),
				Collections.emptyList(), Collections.emptyList());
	}

	static LocusPairIntersectionCandidateSet2D overlap(
			LocusPairIntersectionContext2D context, OverlapStatus status,
			OverlapRelationKind relation, GeometryKind kind) {
		ComponentAddress first = context.getFirstComponents().get(0);
		ComponentAddress second = context.getSecondComponents().get(0);
		IntersectionOverlapEvidence2D evidence = overlapEvidence(first, second,
				status, relation);
		return new LocusPairIntersectionCandidateSet2D(
				status == OverlapStatus.OVERLAP_ESTABLISHED
						? Completeness.COMPLETE : Completeness.NOT_ESTABLISHED,
				status == OverlapStatus.OVERLAP_ESTABLISHED
						? CompletenessMethod.ANALYTIC_ROOT_ENUMERATION
						: CompletenessMethod.NOT_ESTABLISHED,
				kind, status == OverlapStatus.OVERLAP_ESTABLISHED
						? SupportLevel.EXACT_CAPABILITY : SupportLevel.UNSUPPORTED,
				status == OverlapStatus.OVERLAP_ESTABLISHED
						? NumericGuarantee.CERTIFIED_ERROR_BOUND
						: NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				context.getAllComponentPairKeys(), Collections.emptyList(),
				List.of(evidence), Collections.emptyList());
	}

	static LocusPairIntersectionCandidateSet2D mixed(
			LocusPairIntersectionContext2D context, PairRoot root) {
		ComponentAddress first = context.getFirstComponents().get(0);
		ComponentAddress second = context.getSecondComponents().get(0);
		return new LocusPairIntersectionCandidateSet2D(
				Completeness.NOT_ESTABLISHED,
				CompletenessMethod.NOT_ESTABLISHED,
				GeometryKind.MIXED_FINITE_OVERLAP,
				SupportLevel.EXACT_CAPABILITY,
				NumericGuarantee.CERTIFIED_ERROR_BOUND,
				context.getAllComponentPairKeys(),
				List.of(candidate(context, first, second, root)),
				List.of(overlapEvidence(first, second,
						OverlapStatus.OVERLAP_ESTABLISHED,
						OverlapRelationKind.PARTIAL_COMPONENT)),
				Collections.emptyList());
	}

	private static LocusPairIntersectionCandidate2D candidate(
			LocusPairIntersectionContext2D context, ComponentAddress first,
			ComponentAddress second, PairRoot root) {
		double firstRadius = context.getQuery().getPolicy()
				.getFirstRootTolerance().getValue() * 8;
		double secondRadius = context.getQuery().getPolicy()
				.getSecondRootTolerance().getValue() * 8;
		LocalPairIsolationEvidence2D isolation = root.isolated()
				? new LocalPairIsolationEvidence2D(
						LocalIsolationStatus.ESTABLISHED,
						PairIsolationMethod.ANALYTIC_UNIQUE_PAIR,
						PairCoverageStatus.EXHAUSTIVE_RECTANGLE,
						PairUniquenessStatus.CERTIFIED_UNIQUE,
						NumericGuarantee.CERTIFIED_ERROR_BOUND,
						"Analytic fixture proves one root in the rectangle")
				: LocalPairIsolationEvidence2D.notEstablished(
						"Analytic contact exists but local uniqueness is unresolved");
		return new LocusPairIntersectionCandidate2D(first.getBranchKey(),
				first.getComponentKey(), root.first(), OptionalDouble.empty(),
				interval(first.getInterval(), root.first(), firstRadius),
				second.getBranchKey(), second.getComponentKey(), root.second(),
				OptionalDouble.empty(),
				interval(second.getInterval(), root.second(), secondRadius),
				isolation, root.solutionLineage(),
				root.continuationKey() == null ? Optional.empty()
						: Optional.of(root.continuationKey()),
				root.contact(), root.multiplicity() > 0
						? MultiplicityStatus.ESTABLISHED
						: MultiplicityStatus.NOT_ESTABLISHED,
				root.multiplicity() > 0
						? OptionalInt.of(root.multiplicity()) : OptionalInt.empty(),
				SolverMethod.ANALYTIC_ROOT_ENUMERATION,
				NumericGuarantee.CERTIFIED_ERROR_BOUND, root.event(),
				root.parentContinuationKeys(),
				Collections.<IntersectionDiagnostic2D>emptyList());
	}

	private static IntersectionOverlapEvidence2D overlapEvidence(
			ComponentAddress first, ComponentAddress second,
			OverlapStatus status, OverlapRelationKind relation) {
		return new IntersectionOverlapEvidence2D(first.getBranchKey(),
				first.getComponentKey(), second.getBranchKey(),
				second.getComponentKey(), status, relation,
				"analytic test parameter map",
				status == OverlapStatus.OVERLAP_ESTABLISHED
						? "u=t is established over the component"
						: "No certified component-wide parameter map",
				status == OverlapStatus.OVERLAP_ESTABLISHED
						? NumericGuarantee.CERTIFIED_ERROR_BOUND
						: NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				"Typed overlap fixture");
	}

	private static IntersectionParameterInterval2D interval(
			org.geocedg.common.kernel.locus.LocusInterval2D component,
			double parameter, double radius) {
		return new IntersectionParameterInterval2D(
				Math.max(component.getLower(), parameter - radius),
				Math.min(component.getUpper(), parameter + radius));
	}

	@FunctionalInterface
	interface CandidateSetProvider {
		LocusPairIntersectionCandidateSet2D provide(
				LocusPairIntersectionContext2D context);
	}

	record PairRoot(double first, double second, String solutionLineage,
			String continuationKey, ContactClass contact, int multiplicity,
			boolean isolated, LineageEventKind event,
			List<String> parentContinuationKeys) {
	}
}
