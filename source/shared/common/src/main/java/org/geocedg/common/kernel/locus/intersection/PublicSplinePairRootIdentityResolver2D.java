/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairCoverageStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairIsolationMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairUniquenessStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D.DomainKind;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D.SourceDescriptor;
import org.geocedg.common.kernel.locus.intersection.SplinePairIntervalCertification2D.ClassCertificate;
import org.geocedg.common.kernel.locus.intersection.SplinePairIntervalCertification2D.RootCertificate;

/**
 * D2 current-state publication for authenticated spline pairs only. Numerical
 * neighborhoods certify the current slot; no neighborhood, parameter or previous
 * snapshot participates in the durable selector. Slot reactivation is not a
 * claim of physical-root continuation through intervening topology events.
 */
final class PublicSplinePairRootIdentityResolver2D {

	private static final String CONTRACT = "d2-spline-pair-singleton-germ/v1";

	private PublicSplinePairRootIdentityResolver2D() {
	}

	static LocusIntersectionResult2D publish(LocusPairIntersectionContext2D context,
			LocusIntersectionResult2D discovered,
			LocusIntersectionTokenLedger2D.Evaluation evaluation) {
		SplinePairIntervalCertification2D.Result certificate =
				SplinePairIntervalCertification2D.certify(context);
		if (!certificate.isSupported()) {
			return discovered;
		}
		ArrayList<LocusIntersectionSolution2D> solutions =
				new ArrayList<>(discovered.getFiniteSolutions());
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(discovered.getDiagnostics());
		ArrayList<ClassCertificate> groups = new ArrayList<>(certificate.getClasses());
		groups.sort(Comparator.comparing(group -> selector(context, group).toExternalForm()));
		for (ClassCertificate group : groups) {
			PairSemanticSlotSelector2D selector = selector(context, group);
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.PAIR_SELECTOR_CLASS_EVIDENCE,
					"D2 selector germ=" + selector.getGerm() + " status="
							+ group.getStatus() + "; " + group.getDiagnostic()));
			switch (group.getStatus()) {
			case MULTIPLE:
				evaluation.quarantineCurrentPairSelector(selector);
				break;
			case UNIQUE:
				RootCertificate root = group.getUniqueRoot();
				LocusIntersectionSolution2D verified =
						LocusPairIntersectionSolver2D.verifyCandidate(context,
								candidate(root), key -> "d2-unpublished/" + key);
				if (verified != null && verified.getClassification().getContactClass()
						== ContactClass.TRANSVERSE_ESTABLISHED) {
					LocusIntersectionSolution2D bound = bind(context, verified,
							selector, evaluation);
					// Representation replacement only, using a certified unique
					// semantic rectangle. Never matching a prior root or point.
					solutions.removeIf(old -> inside(context, old, root));
					solutions.add(bound);
				} else {
					diagnostics.add(new IntersectionDiagnostic2D(
							DiagnosticCode.PAIR_SELECTOR_CLASS_EVIDENCE,
							"Certified slot refused by current evaluator/contact validation; u="
									+ root.getFirstParameter() + ", v="
									+ root.getSecondParameter() + ", contact="
									+ (verified == null ? "unverified"
											: verified.getClassification().getContactClass())));
				}
				break;
			default:
				// No current proof: the committed evaluation leaves claims dormant.
				break;
			}
		}
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.PAIR_CERTIFICATION_WORK,
				"D2 certification boxes=" + certificate.getBoxesVisited()
						+ ", Krawczyk attempts=" + certificate.getKrawczykAttempts()
						+ "; " + certificate.getDiagnostic()));
		IntersectionCompletenessEvidence2D previous =
				discovered.getCompletenessEvidence();
		IntersectionCompletenessEvidence2D completeness =
				new IntersectionCompletenessEvidence2D(previous.getCompleteness(),
						previous.getMethod(), solutions.size(),
						previous.getCoveredComponentKeys(), previous.getDiagnostics());
		GeometryKind kind = solutions.isEmpty() ? discovered.getGeometryKind()
				: discovered.getOverlapEvidence().isEmpty() ? GeometryKind.FINITE
						: GeometryKind.MIXED_FINITE_OVERLAP;
		return new LocusIntersectionResult2D(discovered.getSourceBinding(),
				discovered.getComputationStatus(), completeness, kind,
				discovered.getCurrentness(), discovered.getSupportLevel(),
				discovered.getNumericGuarantee(), solutions,
				discovered.getOverlapEvidence(),
				discovered.getUnresolvedCandidateComponentKeys(),
				context.getInstrumentation().snapshot(), diagnostics);
	}

	private static PairSemanticSlotSelector2D selector(
			LocusPairIntersectionContext2D context, ClassCertificate group) {
		return PairSemanticSlotSelector2D.of(descriptor(context.getFirstDefinition(),
				group.getFirstBranchKey(), group.getFirstComponentKey()),
				descriptor(context.getSecondDefinition(), group.getSecondBranchKey(),
						group.getSecondComponentKey()), group.getGerm());
	}

	private static SourceDescriptor descriptor(LocusDefinition2D definition,
			String branchKey, String componentKey) {
		for (LocusBranch2D branch : definition.getBranches()) {
			if (branch.getBranchKey().equals(branchKey)) {
				return new SourceDescriptor(definition.getLocusIdentity(), branchKey,
						componentKey, branch.getOrientation(),
						definition.getProvider().isPeriodic() ? DomainKind.PERIODIC
								: DomainKind.NON_PERIODIC,
						definition.getProvider().getProviderId() + "/"
								+ definition.getProvider().getParameterDescriptor());
			}
		}
		throw new IllegalArgumentException("Certified branch no longer current");
	}

	private static LocusPairIntersectionCandidate2D candidate(RootCertificate root) {
		LocalPairIsolationEvidence2D isolation = new LocalPairIsolationEvidence2D(
				LocalIsolationStatus.ESTABLISHED, PairIsolationMethod.CERTIFIED_RECTANGLE,
				PairCoverageStatus.EXHAUSTIVE_RECTANGLE,
				PairUniquenessStatus.CERTIFIED_UNIQUE,
				NumericGuarantee.CERTIFIED_ERROR_BOUND,
				"Outward-rounded Krawczyk existence/uniqueness and component-product "
						+ "singleton transverse-germ proof; coordinates remain floating");
		return new LocusPairIntersectionCandidate2D(root.getFirstBranchKey(),
				root.getFirstComponentKey(), root.getFirstParameter(),
				OptionalDouble.empty(), interval(root.getFirstIsolationInterval(),
						root.getFirstParameter()), root.getSecondBranchKey(),
				root.getSecondComponentKey(), root.getSecondParameter(),
				OptionalDouble.empty(), interval(root.getSecondIsolationInterval(),
						root.getSecondParameter()), isolation,
				CONTRACT + "/" + root.getGerm(), Optional.of(CONTRACT),
				ContactClass.TRANSVERSE_ESTABLISHED, MultiplicityStatus.ESTABLISHED,
				OptionalInt.of(1), SolverMethod.CERTIFIED_PARAMETER_RECTANGLE,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, LineageEventKind.APPEARED,
				List.of(), List.of());
	}

	private static IntersectionParameterInterval2D interval(LocusInterval2D box,
			double canonical) {
		if (!box.contains(canonical, 0)) {
			throw new IllegalArgumentException("Canonical root outside certified chart");
		}
		return new IntersectionParameterInterval2D(box.getLower(), box.getUpper());
	}

	private static LocusIntersectionSolution2D bind(
			LocusPairIntersectionContext2D context, LocusIntersectionSolution2D verified,
			PairSemanticSlotSelector2D selector,
			LocusIntersectionTokenLedger2D.Evaluation evaluation) {
		LocusPairIntersectionEvidence2D pair = verified.getPairEvidence().get();
		PairRootAddressProof2D proof = PairRootAddressProof2D.of(
				pair.getFirst().getLocusIdentity(),
				context.getFirstDefinition().getProvider().getSemanticSignature(),
				pair.getFirst().getSemanticParameter(), pair.getSecond().getLocusIdentity(),
				context.getSecondDefinition().getProvider().getSemanticSignature(),
				pair.getSecond().getSemanticParameter());
		IntersectionRootIdentity2D old = verified.getIdentity();
		IntersectionRootAllocation2D allocation = evaluation.resolveCurrentPairRoot(
				old.getEstablishedBranchLineage(), CONTRACT, selector, proof);
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				allocation.getRootToken(), old.getSourcePairIdentity(),
				old.getConstructiveIntersectionLineage(), old.getEstablishedBranchLineage(),
				old.getTopologyContext(), Optional.of(allocation.getContinuationKey()),
				allocation.isReused() ? IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED
						: IdentityStatus.NEW_TOPOLOGICAL_SOLUTION);
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(
				allocation.isReused() ? LineageEventKind.UNCHANGED : LineageEventKind.APPEARED,
				List.of(), List.of(allocation.getRootToken()), List.of(), false);
		return new LocusIntersectionSolution2D(identity, verified.getRevisionEvidence(),
				verified.getEvaluatedPoint(), verified.getClassification(), lineage,
				List.of(new IntersectionDiagnostic2D(
						DiagnosticCode.DETERMINISTIC_SELECTION_ESTABLISHED,
						"Unique current D2 semantic slot; no claim of physical continuation")),
				verified.getPairEvidence());
	}

	private static boolean inside(LocusPairIntersectionContext2D context,
			LocusIntersectionSolution2D old, RootCertificate root) {
		if (!old.getPairEvidence().isPresent()) {
			return false;
		}
		LocusPairIntersectionEvidence2D pair = old.getPairEvidence().get();
		return sameComponent(pair.getFirst(), root.getFirstBranchKey(),
				root.getFirstComponentKey())
				&& sameComponent(pair.getSecond(), root.getSecondBranchKey(),
						root.getSecondComponentKey())
				&& chartContains(context.getFirstDefinition(),
						root.getFirstIsolationInterval(), pair.getFirst().getSemanticParameter())
				&& chartContains(context.getSecondDefinition(),
						root.getSecondIsolationInterval(), pair.getSecond().getSemanticParameter());
	}

	private static boolean sameComponent(LocusPairSourceRevisionEvidence2D source,
			String branch, String component) {
		return source.getBranchKey().equals(branch) && source.getComponentKey().equals(component);
	}

	private static boolean chartContains(LocusDefinition2D source,
			LocusInterval2D interval, double parameter) {
		if (interval.contains(parameter, 0)) {
			return true;
		}
		if (!source.getProvider().isPeriodic()) {
			return false;
		}
		LocusInterval2D domain = source.getProvider().getDeclaredDomain();
		double period = domain.getUpper() - domain.getLower();
		return interval.contains(parameter - period, 0)
				|| interval.contains(parameter + period, 0);
	}
}
