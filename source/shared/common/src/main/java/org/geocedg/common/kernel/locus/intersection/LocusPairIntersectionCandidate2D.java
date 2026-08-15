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
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MultiplicityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;

/** Capability-produced pair root; never published without two-sided checks. */
public final class LocusPairIntersectionCandidate2D {
	private final String firstBranchKey;
	private final String firstComponentKey;
	private final double firstParameter;
	private final OptionalDouble firstLiftedParameter;
	private final IntersectionParameterInterval2D firstInterval;
	private final String secondBranchKey;
	private final String secondComponentKey;
	private final double secondParameter;
	private final OptionalDouble secondLiftedParameter;
	private final IntersectionParameterInterval2D secondInterval;
	private final LocalPairIsolationEvidence2D localIsolation;
	private final String solutionLineageKey;
	private final Optional<String> continuationKey;
	private final ContactClass contactClass;
	private final MultiplicityStatus multiplicityStatus;
	private final OptionalInt establishedMultiplicity;
	private final SolverMethod solverMethod;
	private final NumericGuarantee numericGuarantee;
	private final LineageEventKind lineageEventKind;
	private final List<String> candidateParentContinuationKeys;
	private final List<IntersectionDiagnostic2D> diagnostics;

	/** Creates one finite semantic parameter-pair candidate. */
	public LocusPairIntersectionCandidate2D(String firstBranchKey,
			String firstComponentKey, double firstParameter,
			OptionalDouble firstLiftedParameter,
			IntersectionParameterInterval2D firstInterval,
			String secondBranchKey, String secondComponentKey,
			double secondParameter, OptionalDouble secondLiftedParameter,
			IntersectionParameterInterval2D secondInterval,
			LocalPairIsolationEvidence2D localIsolation,
			String solutionLineageKey, Optional<String> continuationKey,
			ContactClass contactClass, MultiplicityStatus multiplicityStatus,
			OptionalInt establishedMultiplicity, SolverMethod solverMethod,
			NumericGuarantee numericGuarantee,
			LineageEventKind lineageEventKind,
			List<String> candidateParentContinuationKeys,
			List<IntersectionDiagnostic2D> diagnostics) {
		this.firstBranchKey = requireText(firstBranchKey, "First branch");
		this.firstComponentKey = requireText(firstComponentKey,
				"First component");
		this.firstParameter = finiteParameter(firstParameter);
		this.firstLiftedParameter = checkedLifted(firstLiftedParameter);
		this.firstInterval = checkedInterval(firstInterval, firstParameter);
		this.secondBranchKey = requireText(secondBranchKey, "Second branch");
		this.secondComponentKey = requireText(secondComponentKey,
				"Second component");
		this.secondParameter = finiteParameter(secondParameter);
		this.secondLiftedParameter = checkedLifted(secondLiftedParameter);
		this.secondInterval = checkedInterval(secondInterval, secondParameter);
		this.localIsolation = java.util.Objects.requireNonNull(localIsolation);
		this.solutionLineageKey = requireText(solutionLineageKey,
				"Solution lineage");
		this.continuationKey = checkedOptional(continuationKey);
		this.contactClass = java.util.Objects.requireNonNull(contactClass);
		this.multiplicityStatus = java.util.Objects.requireNonNull(
				multiplicityStatus);
		this.establishedMultiplicity = java.util.Objects.requireNonNull(
				establishedMultiplicity);
		if (multiplicityStatus == MultiplicityStatus.ESTABLISHED
				&& (!establishedMultiplicity.isPresent()
						|| establishedMultiplicity.getAsInt() < 1)) {
			throw new IllegalArgumentException(
					"Established multiplicity must be positive");
		}
		if (multiplicityStatus == MultiplicityStatus.NOT_ESTABLISHED
				&& establishedMultiplicity.isPresent()) {
			throw new IllegalArgumentException(
					"Unknown multiplicity cannot carry an integer");
		}
		this.solverMethod = java.util.Objects.requireNonNull(solverMethod);
		this.numericGuarantee = java.util.Objects.requireNonNull(
				numericGuarantee);
		this.lineageEventKind = java.util.Objects.requireNonNull(
				lineageEventKind);
		this.candidateParentContinuationKeys = immutableStrings(
				candidateParentContinuationKeys);
		this.diagnostics = immutableDiagnostics(diagnostics);
	}

	public String getFirstBranchKey() {
		return firstBranchKey;
	}

	public String getFirstComponentKey() {
		return firstComponentKey;
	}

	public double getFirstParameter() {
		return firstParameter;
	}

	public OptionalDouble getFirstLiftedParameter() {
		return firstLiftedParameter;
	}

	public IntersectionParameterInterval2D getFirstInterval() {
		return firstInterval;
	}

	public String getSecondBranchKey() {
		return secondBranchKey;
	}

	public String getSecondComponentKey() {
		return secondComponentKey;
	}

	public double getSecondParameter() {
		return secondParameter;
	}

	public OptionalDouble getSecondLiftedParameter() {
		return secondLiftedParameter;
	}

	public IntersectionParameterInterval2D getSecondInterval() {
		return secondInterval;
	}

	public LocalPairIsolationEvidence2D getLocalIsolation() {
		return localIsolation;
	}

	public String getSolutionLineageKey() {
		return solutionLineageKey;
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

	private static double finiteParameter(double value) {
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("Pair parameter must be finite");
		}
		return value;
	}

	private static OptionalDouble checkedLifted(OptionalDouble value) {
		java.util.Objects.requireNonNull(value);
		if (value.isPresent() && !Double.isFinite(value.getAsDouble())) {
			throw new IllegalArgumentException("Lifted parameter must be finite");
		}
		return value;
	}

	private static IntersectionParameterInterval2D checkedInterval(
			IntersectionParameterInterval2D interval, double parameter) {
		java.util.Objects.requireNonNull(interval);
		if (!interval.contains(parameter, 0)) {
			throw new IllegalArgumentException(
					"Pair parameter must lie in its rectangle axis");
		}
		return interval;
	}

	private static Optional<String> checkedOptional(Optional<String> value) {
		java.util.Objects.requireNonNull(value);
		if (value.isPresent() && value.get().trim().isEmpty()) {
			throw new IllegalArgumentException("Continuation key cannot be blank");
		}
		return value;
	}

	private static List<String> immutableStrings(List<String> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<String> copy = new ArrayList<>();
		for (String value : input) {
			copy.add(requireText(value, "Parent continuation key"));
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<IntersectionDiagnostic2D> immutableDiagnostics(
			List<IntersectionDiagnostic2D> input) {
		java.util.Objects.requireNonNull(input);
		ArrayList<IntersectionDiagnostic2D> copy = new ArrayList<>();
		for (IntersectionDiagnostic2D value : input) {
			copy.add(java.util.Objects.requireNonNull(value));
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
