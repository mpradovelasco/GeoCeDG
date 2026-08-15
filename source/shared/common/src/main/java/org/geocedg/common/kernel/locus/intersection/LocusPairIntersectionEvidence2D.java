/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.OptionalDouble;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;

/** Complete ordered, revision-scoped evidence for one verified pair root. */
public final class LocusPairIntersectionEvidence2D {
	private final LocusPairSourceRevisionEvidence2D first;
	private final LocusPairSourceRevisionEvidence2D second;
	private final LocalPairIsolationEvidence2D localIsolation;
	private final LocusPairResidualEvidence2D residual;
	private final OptionalDouble normalizedTangentDeterminant;
	private final SolverMethod solverMethod;
	private final NumericGuarantee numericGuarantee;

	/** Creates immutable ordered evidence; durable identity lives elsewhere. */
	public LocusPairIntersectionEvidence2D(
			LocusPairSourceRevisionEvidence2D first,
			LocusPairSourceRevisionEvidence2D second,
			LocalPairIsolationEvidence2D localIsolation,
			LocusPairResidualEvidence2D residual,
			OptionalDouble normalizedTangentDeterminant,
			SolverMethod solverMethod, NumericGuarantee numericGuarantee) {
		this.first = java.util.Objects.requireNonNull(first);
		this.second = java.util.Objects.requireNonNull(second);
		this.localIsolation = java.util.Objects.requireNonNull(localIsolation);
		this.residual = java.util.Objects.requireNonNull(residual);
		this.normalizedTangentDeterminant = java.util.Objects.requireNonNull(
				normalizedTangentDeterminant);
		if (normalizedTangentDeterminant.isPresent()
				&& (!Double.isFinite(normalizedTangentDeterminant.getAsDouble())
						|| Math.abs(normalizedTangentDeterminant.getAsDouble())
								> 1 + 1E-12)) {
			throw new IllegalArgumentException(
					"Normalized determinant must be finite and dimensionless");
		}
		this.solverMethod = java.util.Objects.requireNonNull(solverMethod);
		this.numericGuarantee = java.util.Objects.requireNonNull(
				numericGuarantee);
	}

	/** @return ordered evidence reversed without changing semantic identity */
	public LocusPairIntersectionEvidence2D reversed() {
		OptionalDouble determinant = normalizedTangentDeterminant.isPresent()
				? OptionalDouble.of(-normalizedTangentDeterminant.getAsDouble())
				: OptionalDouble.empty();
		return new LocusPairIntersectionEvidence2D(second, first,
				localIsolation, residual.reversed(), determinant, solverMethod,
				numericGuarantee);
	}

	public LocusPairSourceRevisionEvidence2D getFirst() {
		return first;
	}

	public LocusPairSourceRevisionEvidence2D getSecond() {
		return second;
	}

	public LocalPairIsolationEvidence2D getLocalIsolation() {
		return localIsolation;
	}

	public LocusPairResidualEvidence2D getResidual() {
		return residual;
	}

	public OptionalDouble getNormalizedTangentDeterminant() {
		return normalizedTangentDeterminant;
	}

	public SolverMethod getSolverMethod() {
		return solverMethod;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	/** @return canonical branch/component-pair lineage */
	public String getEstablishedBranchPairLineage() {
		return LocusPairIdentity2D.componentPair(first.getBranchKey(),
				first.getComponentKey(), second.getBranchKey(),
				second.getComponentKey());
	}
}
