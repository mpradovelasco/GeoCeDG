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
import java.util.Set;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Currentness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SupportLevel;

/** Rich immutable authority for one internal Locus V2 intersection query. */
public final class LocusIntersectionResult2D {
	private final IntersectionSourceBinding2D sourceBinding;
	private final ComputationStatus computationStatus;
	private final IntersectionCompletenessEvidence2D completenessEvidence;
	private final GeometryKind geometryKind;
	private final Currentness currentness;
	private final SupportLevel supportLevel;
	private final NumericGuarantee numericGuarantee;
	private final List<LocusIntersectionSolution2D> finiteSolutions;
	private final List<IntersectionOverlapEvidence2D> overlapEvidence;
	private final List<String> unresolvedCandidateComponentKeys;
	private final LocusIntersectionInstrumentationSnapshot2D work;
	private final List<IntersectionDiagnostic2D> diagnostics;
	private final Map<String, LocusIntersectionSolution2D>
			pointAdmissibleByToken;

	/** Creates one deeply immutable atomic query result. */
	public LocusIntersectionResult2D(IntersectionSourceBinding2D sourceBinding,
			ComputationStatus computationStatus,
			IntersectionCompletenessEvidence2D completenessEvidence,
			GeometryKind geometryKind, Currentness currentness,
			SupportLevel supportLevel, NumericGuarantee numericGuarantee,
			List<LocusIntersectionSolution2D> finiteSolutions,
			List<IntersectionOverlapEvidence2D> overlapEvidence,
			LocusIntersectionInstrumentationSnapshot2D work,
			List<IntersectionDiagnostic2D> diagnostics) {
		this(sourceBinding, computationStatus, completenessEvidence, geometryKind,
				currentness, supportLevel, numericGuarantee, finiteSolutions,
				overlapEvidence, Collections.emptyList(), work, diagnostics);
	}

	/**
	 * Creates one atomic result retaining the semantic components on which a
	 * candidate could not be excluded or verified.
	 */
	public LocusIntersectionResult2D(IntersectionSourceBinding2D sourceBinding,
			ComputationStatus computationStatus,
			IntersectionCompletenessEvidence2D completenessEvidence,
			GeometryKind geometryKind, Currentness currentness,
			SupportLevel supportLevel, NumericGuarantee numericGuarantee,
			List<LocusIntersectionSolution2D> finiteSolutions,
			List<IntersectionOverlapEvidence2D> overlapEvidence,
			List<String> unresolvedCandidateComponentKeys,
			LocusIntersectionInstrumentationSnapshot2D work,
			List<IntersectionDiagnostic2D> diagnostics) {
		this.sourceBinding = java.util.Objects.requireNonNull(sourceBinding);
		this.computationStatus =
				java.util.Objects.requireNonNull(computationStatus);
		this.completenessEvidence =
				java.util.Objects.requireNonNull(completenessEvidence);
		this.geometryKind = java.util.Objects.requireNonNull(geometryKind);
		this.currentness = java.util.Objects.requireNonNull(currentness);
		this.supportLevel = java.util.Objects.requireNonNull(supportLevel);
		this.numericGuarantee = java.util.Objects.requireNonNull(numericGuarantee);
		this.finiteSolutions = immutableSolutions(finiteSolutions);
		this.overlapEvidence = immutableOverlap(overlapEvidence);
		this.unresolvedCandidateComponentKeys = immutableUniqueStrings(
				unresolvedCandidateComponentKeys);
		this.work = java.util.Objects.requireNonNull(work);
		this.diagnostics = immutableDiagnostics(diagnostics);
		validateShape();
		this.pointAdmissibleByToken = indexPointAdmissibleSolutions();
	}

	public IntersectionSourceBinding2D getSourceBinding() {
		return sourceBinding;
	}

	public ComputationStatus getComputationStatus() {
		return computationStatus;
	}

	public IntersectionCompletenessEvidence2D getCompletenessEvidence() {
		return completenessEvidence;
	}

	public GeometryKind getGeometryKind() {
		return geometryKind;
	}

	public Currentness getCurrentness() {
		return currentness;
	}

	public SupportLevel getSupportLevel() {
		return supportLevel;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	public List<LocusIntersectionSolution2D> getFiniteSolutions() {
		return finiteSolutions;
	}

	public List<IntersectionOverlapEvidence2D> getOverlapEvidence() {
		return overlapEvidence;
	}

	/**
	 * @return exact revision-scoped semantic components containing at least one
	 *         candidate that could neither be verified nor safely excluded
	 */
	public List<String> getUnresolvedCandidateComponentKeys() {
		return unresolvedCandidateComponentKeys;
	}

	public LocusIntersectionInstrumentationSnapshot2D getWork() {
		return work;
	}

	public List<IntersectionDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}

	/**
	 * Returns one point-admissible solution for an exact semantic token.
	 *
	 * <p>Admissibility is solution-local and deliberately independent of the
	 * parent result's global completeness. The returned rich solution retains
	 * that completeness provenance through this result.</p>
	 *
	 * @return current, verified, locally isolated and unambiguous solution
	 */
	public Optional<LocusIntersectionSolution2D> findPointAdmissibleSolution(
			String rootToken) {
		if (rootToken == null || rootToken.trim().isEmpty()
				|| computationStatus != ComputationStatus.SUCCESS
				|| geometryKind != GeometryKind.FINITE
						&& geometryKind != GeometryKind.MIXED_FINITE_OVERLAP
				|| currentness != Currentness.CURRENT
				|| supportLevel == SupportLevel.UNSUPPORTED) {
			return Optional.empty();
		}
		return Optional.ofNullable(pointAdmissibleByToken.get(rootToken));
	}

	private Map<String, LocusIntersectionSolution2D>
			indexPointAdmissibleSolutions() {
		LinkedHashMap<String, LocusIntersectionSolution2D> indexed =
				new LinkedHashMap<>();
		Set<String> duplicates = new HashSet<>();
		for (LocusIntersectionSolution2D solution : finiteSolutions) {
			if (!isLocallyPointAdmissible(solution)) {
				continue;
			}
			String token = solution.getIdentity().getRootToken();
			if (duplicates.contains(token) || indexed.put(token, solution) != null) {
				indexed.remove(token);
				duplicates.add(token);
			}
		}
		return Collections.unmodifiableMap(indexed);
	}

	/**
	 * Resolves an exact explicit lineage after an authorized owner-ID remap.
	 *
	 * <p>This is deliberately not a geometric fallback: both the established
	 * branch lineage and continuation key come from a previously validated
	 * semantic token. Ambiguity returns empty.</p>
	 *
	 * @return the unique locally admissible solution, or empty
	 */
	public Optional<LocusIntersectionSolution2D>
			findPointAdmissibleSolutionByLineage(String branchLineage,
					String continuationKey) {
		if (branchLineage == null || branchLineage.trim().isEmpty()
				|| continuationKey == null || continuationKey.trim().isEmpty()
				|| computationStatus != ComputationStatus.SUCCESS
				|| geometryKind != GeometryKind.FINITE
						&& geometryKind != GeometryKind.MIXED_FINITE_OVERLAP
				|| currentness != Currentness.CURRENT
				|| supportLevel == SupportLevel.UNSUPPORTED) {
			return Optional.empty();
		}
		LocusIntersectionSolution2D found = null;
		for (LocusIntersectionSolution2D solution : finiteSolutions) {
			IntersectionRootIdentity2D identity = solution.getIdentity();
			if (identity.getEstablishedBranchLineage().equals(branchLineage)
					&& identity.getExplicitContinuationKey()
							.filter(continuationKey::equals).isPresent()) {
				if (!isLocallyPointAdmissible(solution) || found != null) {
					return Optional.empty();
				}
				found = solution;
			}
		}
		return Optional.ofNullable(found);
	}

	private boolean isLocallyPointAdmissible(
			LocusIntersectionSolution2D solution) {
		IntersectionRootIdentity2D identity = solution.getIdentity();
		IntersectionRootRevisionEvidence2D evidence =
				solution.getRevisionEvidence();
		IdentityStatus status = identity.getIdentityStatus();
		boolean identityAdmissible = status == IdentityStatus.CONTINUATION_ESTABLISHED
				|| status
						== IdentityStatus.DETERMINISTIC_SELECTION_ESTABLISHED
				|| status == IdentityStatus.NEW_TOPOLOGICAL_SOLUTION;
		LineageEventKind event = solution.getLineage().getEventKind();
		boolean lineageAdmissible = event == LineageEventKind.APPEARED
				|| event == LineageEventKind.UNCHANGED;
		boolean common = identityAdmissible
				&& lineageAdmissible
				&& identity.getExplicitContinuationKey().isPresent()
				&& evidence.getLocalIsolationStatus()
						== LocalIsolationStatus.ESTABLISHED
				&& identity.getSourcePairIdentity()
						.equals(sourceBinding.getSourcePairIdentity())
				&& identity.getConstructiveIntersectionLineage().equals(
						sourceBinding.getConstructiveIntersectionLineage())
				&& identity.getTopologyContext()
						.equals(sourceBinding.getTopologyContext())
				&& identity.getEstablishedBranchLineage()
						.equals(evidence.getBranchSnapshotKey())
				&& evidence.getLocusSemanticRevision()
						== sourceBinding.getLocusSemanticRevision()
				&& evidence.getTargetUpdateStamp()
						== sourceBinding.getTargetUpdateStamp()
				&& !unresolvedCandidateComponentKeys.contains(
						evidence.getResolvedValidComponentKey());
		if (!common || !sourceBinding.isLocusPair()) {
			return common;
		}
		if (!solution.getPairEvidence().isPresent()) {
			return false;
		}
		LocusPairIntersectionEvidence2D pair =
				solution.getPairEvidence().get();
		return pair.getLocalIsolation().getStatus()
					== LocalIsolationStatus.ESTABLISHED
				&& pair.getEstablishedBranchPairLineage().equals(
						identity.getEstablishedBranchLineage())
				&& pair.getFirst().getLocusIdentity().equals(
						sourceBinding.getFirstLocusIdentity())
				&& pair.getFirst().getSemanticRevision()
						== sourceBinding.getFirstLocusSemanticRevision()
				&& pair.getSecond().getLocusIdentity().equals(
						sourceBinding.getSecondLocusIdentity())
				&& pair.getSecond().getSemanticRevision()
						== sourceBinding.getSecondLocusSemanticRevision();
	}

	private void validateShape() {
		Completeness completeness = completenessEvidence.getCompleteness();
		if (geometryKind == GeometryKind.EMPTY
				&& completeness != Completeness.COMPLETE) {
			throw new IllegalArgumentException(
					"Only complete coverage may publish an empty result");
		}
		boolean mixed = geometryKind == GeometryKind.MIXED_FINITE_OVERLAP;
		if ((geometryKind == GeometryKind.FINITE || mixed)
				&& finiteSolutions.isEmpty()) {
			throw new IllegalArgumentException(
					"Finite result requires verified solutions");
		}
		boolean overlapKind = geometryKind == GeometryKind.OVERLAP
				|| geometryKind == GeometryKind.INFINITELY_MANY
				|| geometryKind == GeometryKind.UNSUPPORTED_OVERLAP || mixed;
		if (geometryKind != GeometryKind.FINITE && !mixed && !overlapKind
				&& !finiteSolutions.isEmpty()) {
			throw new IllegalArgumentException(
					"Empty or unresolved geometry cannot carry finite solutions");
		}
		if (overlapKind != !overlapEvidence.isEmpty()) {
			throw new IllegalArgumentException(
					"Overlap geometry requires typed overlap evidence only");
		}
		if (completenessEvidence.getVerifiedRootCount()
				!= finiteSolutions.size()) {
			throw new IllegalArgumentException(
					"Completeness evidence root count must match solutions");
		}
		if (work.getUnresolvedCandidates()
				< unresolvedCandidateComponentKeys.size()) {
			throw new IllegalArgumentException(
					"Unresolved component evidence exceeds the work count");
		}
	}

	private static List<LocusIntersectionSolution2D> immutableSolutions(
			List<LocusIntersectionSolution2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<LocusIntersectionSolution2D> copy = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : input) {
			copy.add(java.util.Objects.requireNonNull(solution));
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<IntersectionOverlapEvidence2D> immutableOverlap(
			List<IntersectionOverlapEvidence2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<IntersectionOverlapEvidence2D> copy = new ArrayList<>();
		for (IntersectionOverlapEvidence2D evidence : input) {
			copy.add(java.util.Objects.requireNonNull(evidence));
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<IntersectionDiagnostic2D> immutableDiagnostics(
			List<IntersectionDiagnostic2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<IntersectionDiagnostic2D> copy = new ArrayList<>();
		for (IntersectionDiagnostic2D diagnostic : input) {
			copy.add(java.util.Objects.requireNonNull(diagnostic));
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<String> immutableUniqueStrings(List<String> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<String> copy = new ArrayList<>();
		for (String value : input) {
			if (value == null || value.trim().isEmpty()) {
				throw new IllegalArgumentException(
						"Unresolved component keys must be nonblank");
			}
			if (!copy.contains(value)) {
				copy.add(value);
			}
		}
		return Collections.unmodifiableList(copy);
	}
}
