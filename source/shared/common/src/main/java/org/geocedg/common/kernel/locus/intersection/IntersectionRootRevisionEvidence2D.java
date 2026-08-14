/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.OptionalDouble;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.SolverMethod;

/** Revision-scoped root localization and verification evidence. */
public final class IntersectionRootRevisionEvidence2D {
	private final long locusSemanticRevision;
	private final long targetUpdateStamp;
	private final String branchSnapshotKey;
	private final String resolvedValidComponentKey;
	private final double semanticParameter;
	private final OptionalDouble liftedPeriodicParameter;
	private final IntersectionParameterInterval2D isolatingInterval;
	private final LocalIsolationStatus localIsolationStatus;
	private final TargetResidual2D residualEvidence;
	private final SolverMethod solverMethod;
	private final NumericGuarantee numericGuarantee;

	/** Creates immutable evidence for exactly one captured source revision. */
	public IntersectionRootRevisionEvidence2D(long locusSemanticRevision,
			long targetUpdateStamp, String branchSnapshotKey,
			String resolvedValidComponentKey, double semanticParameter,
			OptionalDouble liftedPeriodicParameter,
			IntersectionParameterInterval2D isolatingInterval,
			LocalIsolationStatus localIsolationStatus,
			TargetResidual2D residualEvidence, SolverMethod solverMethod,
			NumericGuarantee numericGuarantee) {
		if (locusSemanticRevision < 1 || targetUpdateStamp < 0
				|| !Double.isFinite(semanticParameter)) {
			throw new IllegalArgumentException("Invalid root revision evidence");
		}
		this.locusSemanticRevision = locusSemanticRevision;
		this.targetUpdateStamp = targetUpdateStamp;
		this.branchSnapshotKey = requireText(branchSnapshotKey, "Branch key");
		this.resolvedValidComponentKey = requireText(resolvedValidComponentKey,
				"Component key");
		this.semanticParameter = semanticParameter;
		this.liftedPeriodicParameter =
				java.util.Objects.requireNonNull(liftedPeriodicParameter);
		this.isolatingInterval =
				java.util.Objects.requireNonNull(isolatingInterval);
		this.localIsolationStatus =
				java.util.Objects.requireNonNull(localIsolationStatus);
		this.residualEvidence = java.util.Objects.requireNonNull(residualEvidence);
		this.solverMethod = java.util.Objects.requireNonNull(solverMethod);
		this.numericGuarantee = java.util.Objects.requireNonNull(numericGuarantee);
	}

	public long getLocusSemanticRevision() {
		return locusSemanticRevision;
	}

	public long getTargetUpdateStamp() {
		return targetUpdateStamp;
	}

	public String getBranchSnapshotKey() {
		return branchSnapshotKey;
	}

	public String getResolvedValidComponentKey() {
		return resolvedValidComponentKey;
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

	public TargetResidual2D getResidualEvidence() {
		return residualEvidence;
	}

	public SolverMethod getSolverMethod() {
		return solverMethod;
	}

	public NumericGuarantee getNumericGuarantee() {
		return numericGuarantee;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
