/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Currentness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairCoverageStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairIsolationMethod;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.PairUniquenessStatus;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D.DomainKind;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D.SourceDescriptor;

/**
 * Identity of the roots of an intersection of two loci that lie, on at least one source, on
 * a conditioned region (ADR 0042; contract {@code post-r1-conditioned-use.md}, sections 6.5,
 * point 8, and 8.6).
 *
 * <p>Each source side binds the lineage of its certified component, or the branch frame of
 * that source when the root lies in a span of it. A pair with a branch-frame side has no
 * rank: only a selector that is unique among the current published roots of the result
 * allocates a token, each branch-frame side records its own origin, and the reuse guard must
 * hold on each of them. The roots of two certified components are not touched: their
 * identity stays that of the D2 publication.</p>
 */
public final class PublicConditionedPairRootIdentityResolver2D {
	/** Contract of the bindings of this resolver in the token ledger. */
	public static final String CONTRACT = "conditioned-use/branch-frame-pair-germ/v1";
	/**
	 * Half width of the local parameter rectangle of the isolation evidence, as a fraction
	 * of the width of the place where the root lies on each source.
	 */
	private static final double RECTANGLE = 1.0 / 1024;

	private PublicConditionedPairRootIdentityResolver2D() {
		// Utility class.
	}

	/**
	 * @param current result of the pair search over the search views of the two sources
	 * @param first search view of the canonical first source
	 * @param second search view of the canonical second source
	 * @param evaluation open token evaluation of the result
	 * @param constructive whether the document is governed by the constructive authority,
	 *        under which a root on a span without evidence of its own is not admitted
	 * @return the result with the identity of its roots on conditioned regions resolved
	 */
	public static LocusIntersectionResult2D resolve(LocusIntersectionResult2D current,
			LocusDefinition2D first, LocusDefinition2D second,
			LocusIntersectionTokenLedger2D.Evaluation evaluation, boolean constructive) {
		if (current.getComputationStatus() != ComputationStatus.SUCCESS
				|| current.getCurrentness() != Currentness.CURRENT
				|| current.getGeometryKind() != GeometryKind.FINITE
						&& current.getGeometryKind() != GeometryKind.MIXED_FINITE_OVERLAP) {
			return current;
		}
		ArrayList<LocusIntersectionSolution2D> resolved =
				new ArrayList<>(current.getFiniteSolutions());
		Map<PairSemanticSlotSelector2D, List<Root>> bySelector = new LinkedHashMap<>();
		Set<String> unselected = new HashSet<>();
		int conditionedRoots = 0;
		for (int index = 0; index < resolved.size(); index++) {
			LocusIntersectionSolution2D solution = resolved.get(index);
			if (!solution.getPairEvidence().isPresent()) {
				continue;
			}
			LocusPairIntersectionEvidence2D pair = solution.getPairEvidence().get();
			Side firstSide = Side.of(first, pair.getFirst(), true);
			Side secondSide = Side.of(second, pair.getSecond(), false);
			if (firstSide == null || secondSide == null
					|| !firstSide.conditioned && !secondSide.conditioned) {
				continue;
			}
			conditionedRoots++;
			solution = declared(isolated(solution, firstSide, secondSide, first, second),
					firstSide, secondSide, constructive);
			pair = solution.getPairEvidence().get();
			resolved.set(index, unidentified(solution, IdentityStatus.NOT_ESTABLISHED,
					null));
			OptionalDouble determinant = pair.getNormalizedTangentDeterminant();
			String frames = PairSemanticSlotSelector2D.of(firstSide.descriptor,
					secondSide.descriptor, 1).toExternalForm() + "|"
					+ PairSemanticSlotSelector2D.of(firstSide.descriptor,
							secondSide.descriptor, -1).toExternalForm();
			if (pair.getLocalIsolation().getStatus() != LocalIsolationStatus.ESTABLISHED
					|| solution.getClassification().getContactClass()
							!= ContactClass.TRANSVERSE_ESTABLISHED
					|| !determinant.isPresent() || determinant.getAsDouble() == 0
					|| Double.isNaN(determinant.getAsDouble())) {
				// A published root without a selector of its own leaves no selector of its
				// frames known to be unique among the published roots.
				unselected.add(frames);
				continue;
			}
			// The orientation-normalized transverse sign det[e1 C1', -e2 C2'] of the D2
			// selector, in the axes of the canonical sources.
			int germ = -firstSide.orientation * secondSide.orientation
					* (determinant.getAsDouble() > 0 ? 1 : -1);
			PairSemanticSlotSelector2D selector = PairSemanticSlotSelector2D.of(
					firstSide.descriptor, secondSide.descriptor, germ);
			bySelector.computeIfAbsent(selector, unused -> new ArrayList<>()).add(
					new Root(index, solution, firstSide, secondSide, frames));
		}
		if (conditionedRoots == 0) {
			return current;
		}
		int withheld = 0;
		int ambiguous = 0;
		for (Map.Entry<PairSemanticSlotSelector2D, List<Root>> entry
				: bySelector.entrySet()) {
			PairSemanticSlotSelector2D selector = entry.getKey();
			if (entry.getValue().size() > 1
					|| unselected.contains(entry.getValue().get(0).frames)) {
				evaluation.withholdBranchFramePair(CONTRACT, selector);
				for (Root root : entry.getValue()) {
					resolved.set(root.index, unidentified(root.solution,
							IdentityStatus.AMBIGUOUS_CONTINUATION,
							new IntersectionDiagnostic2D(
									DiagnosticCode.CONTINUATION_AMBIGUOUS,
									"The pair selector of this root on a conditioned region "
											+ "is not unique among the published roots; no "
											+ "rank, order or parameter was used")));
					ambiguous++;
				}
				continue;
			}
			Root root = entry.getValue().get(0);
			LocusIntersectionSolution2D bound = bind(root, selector, first, second,
					evaluation);
			if (bound.getIdentity().getIdentityStatus() == IdentityStatus.NOT_ESTABLISHED) {
				withheld++;
			}
			resolved.set(root.index, bound);
		}
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(current.getDiagnostics());
		if (withheld != 0) {
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.BRANCH_FRAME_TOKEN_WITHHELD,
					"The token of " + withheld + " pair selector(s) with a branch-frame side "
							+ "is withheld: on a side, the origin of the binding and the "
							+ "current root do not lie in one conditioned region"));
		}
		if (ambiguous != 0) {
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.CONTINUATION_AMBIGUOUS,
					ambiguous + " pair root(s) on conditioned regions keep revision-local "
							+ "identities: their selectors are not unique"));
		}
		// The roots found with a conditioned source are never known to be all the roots.
		IntersectionCompletenessEvidence2D previous = current.getCompletenessEvidence();
		IntersectionCompletenessEvidence2D completeness =
				new IntersectionCompletenessEvidence2D(
						IntersectionSemanticMetadata2D.Completeness.NOT_ESTABLISHED,
						IntersectionSemanticMetadata2D.CompletenessMethod.NOT_ESTABLISHED,
						resolved.size(), previous.getCoveredComponentKeys(),
						previous.getDiagnostics());
		return new LocusIntersectionResult2D(current.getSourceBinding(),
				current.getComputationStatus(), completeness, current.getGeometryKind(),
				current.getCurrentness(), current.getSupportLevel(),
				current.getNumericGuarantee(), resolved, current.getOverlapEvidence(),
				current.getUnresolvedCandidateComponentKeys(), current.getWork(),
				diagnostics);
	}

	private static LocusIntersectionSolution2D bind(Root root,
			PairSemanticSlotSelector2D selector, LocusDefinition2D first,
			LocusDefinition2D second,
			LocusIntersectionTokenLedger2D.Evaluation evaluation) {
		LocusIntersectionSolution2D solution = root.solution;
		LocusPairIntersectionEvidence2D pair = solution.getPairEvidence().get();
		Optional<IntersectionBranchFrameOrigin2D> firstHere = root.first.origin(
				first.getProvider(), pair.getFirst());
		Optional<IntersectionBranchFrameOrigin2D> secondHere = root.second.origin(
				second.getProvider(), pair.getSecond());
		// The selector orders its sources by identity; the origins follow their sources.
		boolean sameOrder = selector.getFirst().getSourceId().equals(
				root.first.descriptor.getSourceId());
		Side firstOfSelector = sameOrder ? root.first : root.second;
		Side secondOfSelector = sameOrder ? root.second : root.first;
		LocusPairSourceRevisionEvidence2D firstEvidence = sameOrder ? pair.getFirst()
				: pair.getSecond();
		LocusPairSourceRevisionEvidence2D secondEvidence = sameOrder ? pair.getSecond()
				: pair.getFirst();
		PairRootAddressProof2D proof = PairRootAddressProof2D.of(
				pair.getFirst().getLocusIdentity(),
				first.getProvider().getSemanticSignature(),
				pair.getFirst().getSemanticParameter(), pair.getSecond().getLocusIdentity(),
				second.getProvider().getSemanticSignature(),
				pair.getSecond().getSemanticParameter());
		// The frames of the pair, without any revision-local component key.
		String lineage = LocusPairIdentity2D.componentPair(
				root.first.descriptor.getBranchLineage(),
				root.first.descriptor.getComponentLineage(),
				root.second.descriptor.getBranchLineage(),
				root.second.descriptor.getComponentLineage());
		Optional<IntersectionRootAllocation2D> allocation =
				evaluation.resolveBranchFramePairRoot(lineage, CONTRACT, selector, proof,
						sameOrder ? firstHere : secondHere, sameOrder ? secondHere : firstHere,
						(firstOrigin, secondOrigin) -> firstOfSelector.guard(firstOrigin,
								firstEvidence) && secondOfSelector.guard(secondOrigin,
										secondEvidence));
		if (!allocation.isPresent()) {
			return unidentified(solution, IdentityStatus.NOT_ESTABLISHED,
					new IntersectionDiagnostic2D(
							DiagnosticCode.BRANCH_FRAME_TOKEN_WITHHELD,
							"The token of the pair selector is withheld: on a branch-frame "
									+ "side, the origin of the binding and this root do not "
									+ "lie in one conditioned region; the root keeps a "
									+ "revision-local identity"));
		}
		IntersectionRootIdentity2D old = solution.getIdentity();
		boolean reused = allocation.get().isReused();
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				allocation.get().getRootToken(), old.getSourcePairIdentity(),
				old.getConstructiveIntersectionLineage(), old.getEstablishedBranchLineage(),
				old.getTopologyContext(),
				Optional.of(allocation.get().getContinuationKey()),
				reused ? IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED
						: IdentityStatus.NEW_TOPOLOGICAL_SOLUTION);
		IntersectionRootLineage2D rootLineage = new IntersectionRootLineage2D(
				reused ? LineageEventKind.UNCHANGED : LineageEventKind.APPEARED,
				Collections.emptyList(), List.of(allocation.get().getRootToken()),
				Collections.emptyList(), false);
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(solution.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(
				reused ? DiagnosticCode.DETERMINISTIC_SELECTION_ESTABLISHED
						: DiagnosticCode.LOCAL_ISOLATION_ESTABLISHED,
				reused ? "Unique pair selector with a branch-frame side restored its "
						+ "durable allocation under the reuse guard of each side"
						: "Unique pair selector with a branch-frame side received a fresh "
								+ "opaque ledger identity with its origins"));
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.CONDITIONED_EVIDENCE_USED,
				"Premises of the resolution: H1 to H3 of the spans between the origin of "
						+ "the binding and the root, on each branch-frame side"
						+ root.first.used() + root.second.used()));
		return new LocusIntersectionSolution2D(identity, solution.getRevisionEvidence(),
				solution.getEvaluatedPoint(), solution.getClassification(), rootLineage,
				diagnostics, solution.getPairEvidence());
	}

	/**
	 * Local isolation of a transverse root of two loci on a conditioned region (contract
	 * {@code post-r1-conditioned-use.md}, sections 8.5 and 8.6: each root needs its evidence
	 * on both sources). It is the pair form of the safeguarded bracket of a transverse root
	 * on one source: on a parameter rectangle around the root, inside the place of the root
	 * on each source, the difference of the two curves, read in the coordinates of its
	 * Jacobian at the root, changes sign across each pair of opposite sides, and the
	 * Jacobian keeps its orientation at every sampled end. It is numerical evidence under
	 * H1 to H4 of the spans, with no certified enclosure.
	 *
	 * @return the solution with its isolation established, or unchanged
	 */
	private static LocusIntersectionSolution2D isolated(
			LocusIntersectionSolution2D solution, Side firstSide, Side secondSide,
			LocusDefinition2D first, LocusDefinition2D second) {
		LocusPairIntersectionEvidence2D pair = solution.getPairEvidence().get();
		if (pair.getLocalIsolation().getStatus() == LocalIsolationStatus.ESTABLISHED
				|| solution.getClassification().getContactClass()
						!= ContactClass.TRANSVERSE_ESTABLISHED) {
			return solution;
		}
		double u = pair.getFirst().getSemanticParameter();
		double v = pair.getSecond().getSemanticParameter();
		double a = RECTANGLE * (firstSide.place.getUpper() - firstSide.place.getLower());
		double b = RECTANGLE * (secondSide.place.getUpper() - secondSide.place.getLower());
		if (!(a > 0) || !(b > 0) || u - a < firstSide.place.getLower()
				|| u + a > firstSide.place.getUpper() || v - b < secondSide.place.getLower()
				|| v + b > secondSide.place.getUpper()) {
			return solution;
		}
		LocusPoint2D[] c1 = new LocusPoint2D[3];
		LocusPoint2D[] c2 = new LocusPoint2D[3];
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.openServiceSession(false, 1)) {
			for (int index = 0; index < 3; index++) {
				LocusEvaluation2D p = first.evaluate(pair.getFirst().getBranchKey(),
						u + (index - 1) * a, session);
				LocusEvaluation2D q = second.evaluate(pair.getSecond().getBranchKey(),
						v + (index - 1) * b, session);
				if (!p.isValid() || p.getPoint() == null || !q.isValid()
						|| q.getPoint() == null) {
					return solution;
				}
				c1[index] = p.getPoint();
				c2[index] = q.getPoint();
			}
		}
		// Jacobian of F(u, v) = C1(u) - C2(v) at the root, by central differences.
		double j11 = (c1[2].getX() - c1[0].getX()) / (2 * a);
		double j21 = (c1[2].getY() - c1[0].getY()) / (2 * a);
		double j12 = -(c2[2].getX() - c2[0].getX()) / (2 * b);
		double j22 = -(c2[2].getY() - c2[0].getY()) / (2 * b);
		double determinant = j11 * j22 - j12 * j21;
		if (!Double.isFinite(determinant) || determinant == 0) {
			return solution;
		}
		// The orientation of the two tangents at every pair of sampled ends.
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				double side = (c1[i + 1].getX() - c1[i].getX()) * -(c2[j + 1].getY()
						- c2[j].getY()) + (c2[j + 1].getX() - c2[j].getX())
								* (c1[i + 1].getY() - c1[i].getY());
				if (!(side * determinant > 0)) {
					return solution;
				}
			}
		}
		// G, the inverse Jacobian applied to F, is close to (u - u0, v - v0): its first
		// coordinate changes sign across the sides u = u0 -+ a, and its second across the
		// sides v = v0 -+ b.
		for (int k = 0; k < 3; k++) {
			double lowU = g(c1[0], c2[k], j22, -j12, determinant);
			double highU = g(c1[2], c2[k], j22, -j12, determinant);
			double lowV = g(c1[k], c2[0], -j21, j11, determinant);
			double highV = g(c1[k], c2[2], -j21, j11, determinant);
			if (!(lowU < 0 && highU > 0 && lowV < 0 && highV > 0)) {
				return solution;
			}
		}
		LocalPairIsolationEvidence2D isolation = new LocalPairIsolationEvidence2D(
				LocalIsolationStatus.ESTABLISHED,
				PairIsolationMethod.SAFEGUARDED_REGULAR_JACOBIAN,
				PairCoverageStatus.SAMPLED_RECTANGLE_UNDER_HYPOTHESES,
				PairUniquenessStatus.REGULAR_JACOBIAN_UNIQUE,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED,
				"Sign change across the opposite sides of the parameter rectangle ["
						+ (u - a) + ", " + (u + a) + "] x [" + (v - b) + ", " + (v + b)
						+ "] in the coordinates of a regular Jacobian of constant "
						+ "orientation; numerical evidence under the hypotheses of the "
						+ "spans, not a certified enclosure");
		LocusPairIntersectionEvidence2D established = new LocusPairIntersectionEvidence2D(
				pair.getFirst(), pair.getSecond(), isolation, pair.getResidual(),
				pair.getNormalizedTangentDeterminant(), pair.getSolverMethod(),
				pair.getNumericGuarantee());
		IntersectionRootRevisionEvidence2D evidence = solution.getRevisionEvidence();
		IntersectionRootRevisionEvidence2D revision = new IntersectionRootRevisionEvidence2D(
				evidence.getLocusSemanticRevision(), evidence.getTargetUpdateStamp(),
				evidence.getBranchSnapshotKey(), evidence.getResolvedValidComponentKey(),
				evidence.getSemanticParameter(), evidence.getLiftedPeriodicParameter(),
				evidence.getIsolatingInterval(), LocalIsolationStatus.ESTABLISHED,
				evidence.getResidualEvidence(), evidence.getSolverMethod(),
				evidence.getNumericGuarantee(), evidence.getCurrentRootGerm(),
				evidence.getExistenceEvidence());
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(solution.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.LOCAL_ISOLATION_ESTABLISHED, isolation.getDiagnostic()));
		return new LocusIntersectionSolution2D(solution.getIdentity(), revision,
				solution.getEvaluatedPoint(), solution.getClassification(),
				solution.getLineage(), diagnostics, Optional.of(established));
	}

	/** One coordinate of the inverse Jacobian applied to {@code p - q}. */
	private static double g(LocusPoint2D p, LocusPoint2D q, double rowX, double rowY,
			double determinant) {
		return (rowX * (p.getX() - q.getX()) + rowY * (p.getY() - q.getY())) / determinant;
	}

	/** The root with a revision-local identity: its token is not a durable token. */
	private static LocusIntersectionSolution2D unidentified(
			LocusIntersectionSolution2D solution, IdentityStatus status,
			IntersectionDiagnostic2D reason) {
		IntersectionRootIdentity2D old = solution.getIdentity();
		IntersectionRootIdentity2D identity = new IntersectionRootIdentity2D(
				old.getRootToken(), old.getSourcePairIdentity(),
				old.getConstructiveIntersectionLineage(), old.getEstablishedBranchLineage(),
				old.getTopologyContext(), Optional.empty(), status);
		IntersectionRootLineage2D lineage = new IntersectionRootLineage2D(
				LineageEventKind.AMBIGUOUS_EVENT, Collections.emptyList(),
				List.of(old.getRootToken()), Collections.emptyList(), false);
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(solution.getDiagnostics());
		if (reason != null) {
			diagnostics.add(reason);
		}
		return new LocusIntersectionSolution2D(identity, solution.getRevisionEvidence(),
				solution.getEvaluatedPoint(), solution.getClassification(), lineage,
				diagnostics, solution.getPairEvidence());
	}

	/**
	 * The root with the declaration of its conditioned sides and, under the constructive
	 * authority, with its admission not established: a root on a span has no evidence of
	 * its own ({@code locus-v2-constructive-authority.md} section 7.4).
	 */
	private static LocusIntersectionSolution2D declared(
			LocusIntersectionSolution2D solution, Side first, Side second,
			boolean constructive) {
		ArrayList<IntersectionDiagnostic2D> diagnostics =
				new ArrayList<>(solution.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(
				DiagnosticCode.CONDITIONED_REGION_ROOT,
				"Root of two loci on a conditioned region of "
						+ (first.conditioned && second.conditioned ? "both sources"
								: first.conditioned ? "the first source" : "the second source")
						+ ": existence and continuity between the nodes are the declared "
						+ "hypotheses H1 to H4, not certified" + first.used() + second.used()));
		IntersectionRootRevisionEvidence2D evidence = solution.getRevisionEvidence();
		if (constructive) {
			evidence = new IntersectionRootRevisionEvidence2D(
					evidence.getLocusSemanticRevision(), evidence.getTargetUpdateStamp(),
					evidence.getBranchSnapshotKey(), evidence.getResolvedValidComponentKey(),
					evidence.getSemanticParameter(), evidence.getLiftedPeriodicParameter(),
					evidence.getIsolatingInterval(), evidence.getLocalIsolationStatus(),
					evidence.getResidualEvidence(), evidence.getSolverMethod(),
					evidence.getNumericGuarantee(), evidence.getCurrentRootGerm(),
					IntersectionExistenceEvidence2D.of(
							evidence.getExistenceEvidence().getGrade(),
							IntersectionExistenceEvidence2D.NOT_ENCLOSED_CONSTRUCTIVE, null,
							null).withConstructiveAdmission(
									ConstructiveAdmission.NOT_ESTABLISHED));
			diagnostics.add(new IntersectionDiagnostic2D(
					DiagnosticCode.CONSTRUCTIVE_ADMISSION_NOT_ESTABLISHED,
					"A root on a conditioned region has no enclosure in the constructive "
							+ "configuration: it is not admitted by the constructive "
							+ "authority"));
		}
		return new LocusIntersectionSolution2D(solution.getIdentity(), evidence,
				solution.getEvaluatedPoint(), solution.getClassification(),
				solution.getLineage(), diagnostics, solution.getPairEvidence());
	}

	/** One source side of a root: its frame, its orientation and its evidence. */
	private static final class Side {
		private final SourceDescriptor descriptor;
		private final boolean conditioned;
		private final int orientation;
		private final LocusConditionedEvidence2D evidence;
		private final LocusInterval2D place;

		private Side(SourceDescriptor descriptor, boolean conditioned, int orientation,
				LocusConditionedEvidence2D evidence, LocusInterval2D place) {
			this.place = place;
			this.descriptor = descriptor;
			this.conditioned = conditioned;
			this.orientation = orientation;
			this.evidence = evidence;
		}

		static Side of(LocusDefinition2D definition,
				LocusPairSourceRevisionEvidence2D source, boolean first) {
			LocusBranch2D branch = definition.getBranch(source.getBranchKey());
			if (branch == null) {
				return null;
			}
			for (LocusPairIntersectionContext2D.ComponentAddress address
					: LocusPairIntersectionContext2D.components(definition, first)) {
				if (!address.getBranchKey().equals(source.getBranchKey())
						|| !address.getComponentKey().equals(source.getComponentKey())) {
					continue;
				}
				ContinuousComponent component = address.getComponent();
				boolean conditioned = LocusBranch2D.isConditionedRegion(component);
				return new Side(new SourceDescriptor(definition.getLocusIdentity(),
						branch.getBranchKey(), branch.searchFrame(component),
						branch.getOrientation(), definition.getProvider().isPeriodic()
								? DomainKind.PERIODIC : DomainKind.NON_PERIODIC,
						PairSemanticSlotSelector2D.parameterizationContract(
								definition.getProvider())),
						conditioned, branch.getOrientation() == Orientation.INCREASING
								? 1 : -1,
						conditioned ? branch.getSearchEvidence() : null,
						component.getInterval());
			}
			return null;
		}

		Optional<IntersectionBranchFrameOrigin2D> origin(
				LocusDriverDomainProvider2D provider,
				LocusPairSourceRevisionEvidence2D source) {
			if (!conditioned) {
				return Optional.empty();
			}
			double canonical = provider.canonicalize(source.getSemanticParameter());
			return Optional.of(PublicIntersectionRootIdentityResolver2D.originOf(provider,
					source.getLiftedPeriodicParameter().orElse(
							source.getSemanticParameter()), canonical));
		}

		/** The reuse guard of this side; a certified side has none. */
		boolean guard(Optional<IntersectionBranchFrameOrigin2D> origin,
				LocusPairSourceRevisionEvidence2D source) {
			if (!conditioned) {
				return !origin.isPresent();
			}
			if (!origin.isPresent() || evidence == null) {
				return false;
			}
			double parameter = source.getSemanticParameter();
			double lower = Math.min(parameter, source.getIsolatingInterval().getLower());
			double upper = Math.max(parameter, source.getIsolatingInterval().getUpper());
			return evidence.inOneRegion(origin.get().getCanonicalParameter(), lower)
					&& evidence.inOneRegion(origin.get().getCanonicalParameter(), upper);
		}

		String used() {
			return evidence == null ? "" : "; evidence revision " + evidence.getRevision()
					+ ", content " + evidence.getContentIdentity();
		}
	}

	private static final class Root {
		private final int index;
		private final LocusIntersectionSolution2D solution;
		private final Side first;
		private final Side second;
		private final String frames;

		private Root(int index, LocusIntersectionSolution2D solution, Side first,
				Side second, String frames) {
			this.index = index;
			this.solution = solution;
			this.first = first;
			this.second = second;
			this.frames = frames;
		}
	}
}
