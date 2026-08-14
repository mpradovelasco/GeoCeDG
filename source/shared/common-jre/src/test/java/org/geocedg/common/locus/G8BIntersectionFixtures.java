/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.kernel.locus.intersection.IntersectionCandidate2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionCandidateSet2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionCapabilityContext2D;
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
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionCapability2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoNumeric;

/** Productive G8B test fixtures and explicit analytic proof capabilities. */
final class G8BIntersectionFixtures {
	static final double DOMAIN_EPSILON = 1E-14;

	private G8BIntersectionFixtures() {
	}

	@FunctionalInterface
	interface Curve {
		LocusPoint2D evaluate(double sourceValue, LocusBranch2D branch,
				double parameter);
	}

	@FunctionalInterface
	interface CandidateSetProvider {
		IntersectionCandidateSet2D provide(IntersectionCapabilityContext2D context);
	}

	record Fixture(GeoNumeric source, GeoLocusV2 locus,
			ExplicitNumericDomainProvider2D provider, List<LocusBranch2D> branches) {
		String branchKey() {
			return branches.get(0).getBranchKey();
		}

		String componentKey() {
			return IntersectionCapabilityContext2D.componentKey(branchKey(), 0);
		}
	}

	record Root(String branchKey, String componentKey, double parameter,
			String continuationKey, int multiplicity, ContactClass contact,
			LineageEventKind event, List<String> parentContinuationKeys) {
		static Root simple(String branchKey, String componentKey,
				double parameter, String continuationKey) {
			return new Root(branchKey, componentKey, parameter, continuationKey,
					1, ContactClass.TRANSVERSE_ESTABLISHED,
					LineageEventKind.APPEARED, Collections.emptyList());
		}

		static Root tangent(String branchKey, String componentKey,
				double parameter, String continuationKey, int multiplicity) {
			return new Root(branchKey, componentKey, parameter, continuationKey,
					multiplicity, ContactClass.TANGENT_ESTABLISHED,
					LineageEventKind.APPEARED, Collections.emptyList());
		}
	}

	static Fixture single(Construction construction, String identity,
			double lower, double upper, boolean lowerClosed,
			boolean upperClosed, boolean periodic, Curve curve) {
		ExplicitNumericDomainProvider2D provider = provider(identity, lower,
				upper, lowerClosed, upperClosed, periodic, Orientation.INCREASING);
		LocusBranch2D branch = branch(identity + "/branch-main", provider,
				List.of(provider.getDeclaredDomain()),
				EnumSet.of(BranchProperty.FINITE));
		return create(construction, identity, provider, List.of(branch), curve);
	}

	static Fixture create(Construction construction, String identity,
			ExplicitNumericDomainProvider2D provider,
			List<LocusBranch2D> branches, Curve curve) {
		GeoNumeric source = new GeoNumeric(construction, 0);
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				construction, identity, source, provider, branches,
				(sourceValue, branch, parameter, session) ->
						curve.evaluate(sourceValue, branch, parameter),
				identity + "/g8b-evaluator/v1");
		return new Fixture(source, locus, provider, List.copyOf(branches));
	}

	static ExplicitNumericDomainProvider2D provider(String identity,
			double lower, double upper, boolean lowerClosed,
			boolean upperClosed, boolean periodic, Orientation orientation) {
		return new ExplicitNumericDomainProvider2D(identity + "/parameter/v1",
				new LocusInterval2D(lower, upper, lowerClosed, upperClosed),
				orientation, periodic, DOMAIN_EPSILON);
	}

	static LocusBranch2D branch(String branchKey,
			ExplicitNumericDomainProvider2D provider,
			List<LocusInterval2D> components,
			EnumSet<BranchProperty> properties) {
		return new LocusBranch2D(branchKey, provider.getDeclaredDomain(),
				components, provider.getOrientation(), branchKey + "/semantic-v1",
				LocusLineage2D.unchanged(), properties,
				LocusQuality2D.analyticDoubleSemantic());
	}

	static LocusIntersectionCapability2D capability(String id,
			CandidateSetProvider provider) {
		return new LocusIntersectionCapability2D() {
			@Override
			public String getCapabilityId() {
				return id;
			}

			@Override
			public boolean supports(IntersectionCapabilityContext2D context) {
				return true;
			}

			@Override
			public IntersectionCandidateSet2D isolate(
					IntersectionCapabilityContext2D context) {
				return provider.provide(context);
			}
		};
	}

	static IntersectionCandidateSet2D completeRoots(
			IntersectionCapabilityContext2D context, List<Root> roots) {
		return roots(context, roots, Completeness.COMPLETE);
	}

	static IntersectionCandidateSet2D roots(
			IntersectionCapabilityContext2D context, List<Root> roots,
			Completeness completeness) {
		ArrayList<IntersectionCandidate2D> candidates = new ArrayList<>();
		for (Root root : roots) {
			double radius = context.getQuery().getPolicy()
					.getRootParameterTolerance().getValue();
			candidates.add(new IntersectionCandidate2D(root.branchKey(),
					root.componentKey(), root.parameter(), OptionalDouble.empty(),
					new IntersectionParameterInterval2D(root.parameter() - radius,
							root.parameter() + radius),
					LocalIsolationStatus.ESTABLISHED,
					Optional.of(root.continuationKey()), root.contact(),
					MultiplicityStatus.ESTABLISHED,
					OptionalInt.of(root.multiplicity()),
					SolverMethod.ANALYTIC_ROOT_ENUMERATION,
					NumericGuarantee.CERTIFIED_ERROR_BOUND, root.event(),
					root.parentContinuationKeys(), Collections.emptyList()));
		}
		CompletenessMethod method = completeness == Completeness.COMPLETE
				? CompletenessMethod.ANALYTIC_ROOT_ENUMERATION
				: completeness == Completeness.INCOMPLETE
						? CompletenessMethod.INCOMPLETE_CANDIDATE_COVERAGE
						: CompletenessMethod.NOT_ESTABLISHED;
		return new IntersectionCandidateSet2D(completeness, method,
				GeometryKind.FINITE, SupportLevel.EXACT_CAPABILITY,
				NumericGuarantee.CERTIFIED_ERROR_BOUND,
				context.getAllComponentKeys(), candidates, Collections.emptyList(),
				Collections.emptyList());
	}

	static IntersectionCandidateSet2D completeEmpty(
			IntersectionCapabilityContext2D context) {
		return new IntersectionCandidateSet2D(Completeness.COMPLETE,
				CompletenessMethod.CERTIFIED_DOMAIN_EXCLUSION,
				GeometryKind.EMPTY, SupportLevel.CERTIFIED,
				NumericGuarantee.CERTIFIED_ERROR_BOUND,
				context.getAllComponentKeys(), Collections.emptyList(),
				Collections.emptyList(), Collections.emptyList());
	}

	static IntersectionCandidateSet2D overlap(
			IntersectionCapabilityContext2D context, GeometryKind kind,
			String branchKey, String componentKey) {
		IntersectionOverlapEvidence2D evidence =
				new IntersectionOverlapEvidence2D(branchKey, componentKey,
						"test analytic identity proof",
						"Residual is identically zero on the semantic component");
		return new IntersectionCandidateSet2D(Completeness.COMPLETE,
				CompletenessMethod.ANALYTIC_ROOT_ENUMERATION, kind,
				SupportLevel.EXACT_CAPABILITY,
				NumericGuarantee.CERTIFIED_ERROR_BOUND,
				context.getAllComponentKeys(), Collections.emptyList(),
				List.of(evidence), Collections.<IntersectionDiagnostic2D>emptyList());
	}
}
