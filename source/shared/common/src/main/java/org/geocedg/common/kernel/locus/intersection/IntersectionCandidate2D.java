/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LineageEventKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;

/** Capability-produced root candidate; never a published solution by itself. */
public final class IntersectionCandidate2D {
	private final String branchKey;
	private final String componentKey;
	private final double semanticParameter;
	private final OptionalDouble liftedPeriodicParameter;
	private final IntersectionParameterInterval2D isolatingInterval;
	private final LocalIsolationStatus localIsolationStatus;
	private final Optional<String> continuationKey;
	private final ContactClass contactClass;
	private final MultiplicityStatus multiplicityStatus;
	private final OptionalInt establishedMultiplicity;
	private final SolverMethod solverMethod;
	private final NumericGuarantee numericGuarantee;
	private final LineageEventKind lineageEventKind;
	private final List<String> candidateParentContinuationKeys;
	private final List<IntersectionDiagnostic2D> diagnostics;
	private final SplineImplicitIntervalCertification2D.Proof structuralCertificate;

	/** Creates an immutable candidate with explicit evidence strength. */
	public IntersectionCandidate2D(String branchKey, String componentKey,
			double semanticParameter, OptionalDouble liftedPeriodicParameter,
			IntersectionParameterInterval2D isolatingInterval,
			LocalIsolationStatus localIsolationStatus,
			Optional<String> continuationKey, ContactClass contactClass,
			MultiplicityStatus multiplicityStatus,
			OptionalInt establishedMultiplicity, SolverMethod solverMethod,
			NumericGuarantee numericGuarantee,
			LineageEventKind lineageEventKind,
			List<String> candidateParentContinuationKeys,
			List<IntersectionDiagnostic2D> diagnostics) {
		this(branchKey, componentKey, semanticParameter, liftedPeriodicParameter,
				isolatingInterval, localIsolationStatus, continuationKey, contactClass,
				multiplicityStatus, establishedMultiplicity, solverMethod, numericGuarantee,
				lineageEventKind, candidateParentContinuationKeys, diagnostics, null);
	}

	private IntersectionCandidate2D(String branchKey, String componentKey,
			double semanticParameter, OptionalDouble liftedPeriodicParameter,
			IntersectionParameterInterval2D isolatingInterval,
			LocalIsolationStatus localIsolationStatus, Optional<String> continuationKey,
			ContactClass contactClass, MultiplicityStatus multiplicityStatus,
			OptionalInt establishedMultiplicity, SolverMethod solverMethod,
			NumericGuarantee numericGuarantee, LineageEventKind lineageEventKind,
			List<String> candidateParentContinuationKeys,
			List<IntersectionDiagnostic2D> diagnostics,
			SplineImplicitIntervalCertification2D.Proof structuralCertificate) {
		this.branchKey = requireText(branchKey, "Branch key");
		this.componentKey = requireText(componentKey, "Component key");
		if (!Double.isFinite(semanticParameter)) {
			throw new IllegalArgumentException("Candidate parameter must be finite");
		}
		this.semanticParameter = semanticParameter;
		this.liftedPeriodicParameter =
				java.util.Objects.requireNonNull(liftedPeriodicParameter);
		if (liftedPeriodicParameter.isPresent()
				&& !Double.isFinite(liftedPeriodicParameter.getAsDouble())) {
			throw new IllegalArgumentException("Lifted parameter must be finite");
		}
		this.isolatingInterval =
				java.util.Objects.requireNonNull(isolatingInterval);
		if (!isolatingInterval.contains(semanticParameter, 0)) {
			throw new IllegalArgumentException(
					"Candidate parameter must lie in its isolating interval");
		}
		this.localIsolationStatus =
				java.util.Objects.requireNonNull(localIsolationStatus);
		this.continuationKey = checkedContinuationKey(continuationKey);
		this.contactClass = java.util.Objects.requireNonNull(contactClass);
		this.multiplicityStatus =
				java.util.Objects.requireNonNull(multiplicityStatus);
		this.establishedMultiplicity =
				java.util.Objects.requireNonNull(establishedMultiplicity);
		if (multiplicityStatus == MultiplicityStatus.ESTABLISHED
				&& (!establishedMultiplicity.isPresent()
						|| establishedMultiplicity.getAsInt() < 1)) {
			throw new IllegalArgumentException(
					"Established multiplicity must be positive");
		}
		if (multiplicityStatus == MultiplicityStatus.NOT_ESTABLISHED
				&& establishedMultiplicity.isPresent()) {
			throw new IllegalArgumentException(
					"Unknown multiplicity cannot carry a magic integer");
		}
		this.solverMethod = java.util.Objects.requireNonNull(solverMethod);
		this.numericGuarantee = java.util.Objects.requireNonNull(numericGuarantee);
		this.lineageEventKind =
				java.util.Objects.requireNonNull(lineageEventKind);
		this.candidateParentContinuationKeys = immutableStrings(
				candidateParentContinuationKeys);
		this.diagnostics = immutableDiagnostics(diagnostics);
		this.structuralCertificate = structuralCertificate;
	}

	IntersectionCandidate2D withStructuralCertificate(
			SplineImplicitIntervalCertification2D.Proof certificate) {
		return new IntersectionCandidate2D(branchKey, componentKey, semanticParameter,
				liftedPeriodicParameter, isolatingInterval, localIsolationStatus,
				continuationKey, contactClass, multiplicityStatus, establishedMultiplicity,
				solverMethod, numericGuarantee, lineageEventKind,
				candidateParentContinuationKeys, diagnostics,
				java.util.Objects.requireNonNull(certificate));
	}

	SplineImplicitIntervalCertification2D.Proof getStructuralCertificate() {
		return structuralCertificate;
	}

	public String getBranchKey() {
		return branchKey;
	}

	public String getComponentKey() {
		return componentKey;
	}

	public double getSemanticParameter() {
		return semanticParameter;
	}

	public OptionalDouble getLiftedPeriodicParameter() {
		return liftedPeriodicParameter;
	}

	public IntersectionParameterInterval2D getIsolatingInterval() {
		return isolatingInterval;
	}

	public LocalIsolationStatus getLocalIsolationStatus() {
		return localIsolationStatus;
	}

	public Optional<String> getContinuationKey() {
		return continuationKey;
	}

	public ContactClass getContactClass() {
		return contactClass;
	}

	public MultiplicityStatus getMultiplicityStatus() {
		return multiplicityStatus;
	}

	public OptionalInt getEstablishedMultiplicity() {
		return establishedMultiplicity;
	}

	public SolverMethod getSolverMethod() {
		return solverMethod;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	public LineageEventKind getLineageEventKind() {
		return lineageEventKind;
	}

	public List<String> getCandidateParentContinuationKeys() {
		return candidateParentContinuationKeys;
	}

	public List<IntersectionDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}

	private static Optional<String> checkedContinuationKey(
			Optional<String> key) {
		java.util.Objects.requireNonNull(key);
		if (key.isPresent() && key.get().trim().isEmpty()) {
			throw new IllegalArgumentException("Continuation key cannot be blank");
		}
		return key;
	}

	private static List<String> immutableStrings(List<String> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<String> copy = new ArrayList<>();
		for (String value : input) {
			copy.add(requireText(value, "Continuation lineage key"));
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

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
