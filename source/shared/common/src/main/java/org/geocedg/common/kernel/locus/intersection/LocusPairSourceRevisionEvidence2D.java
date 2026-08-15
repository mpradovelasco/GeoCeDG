/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.OptionalDouble;

/** Revision-scoped localization evidence for one side of a locus pair. */
public final class LocusPairSourceRevisionEvidence2D {
	private final String locusIdentity;
	private final long semanticRevision;
	private final String branchKey;
	private final String componentKey;
	private final double semanticParameter;
	private final OptionalDouble liftedPeriodicParameter;
	private final IntersectionParameterInterval2D isolatingInterval;

	/** Creates one finite semantic address and its localization interval. */
	public LocusPairSourceRevisionEvidence2D(String locusIdentity,
			long semanticRevision, String branchKey, String componentKey,
			double semanticParameter, OptionalDouble liftedPeriodicParameter,
			IntersectionParameterInterval2D isolatingInterval) {
		this.locusIdentity = requireText(locusIdentity, "Locus identity");
		if (semanticRevision < 1 || !Double.isFinite(semanticParameter)) {
			throw new IllegalArgumentException("Invalid pair source evidence");
		}
		this.semanticRevision = semanticRevision;
		this.branchKey = requireText(branchKey, "Branch key");
		this.componentKey = requireText(componentKey, "Component key");
		this.semanticParameter = semanticParameter;
		this.liftedPeriodicParameter = java.util.Objects.requireNonNull(
				liftedPeriodicParameter);
		if (liftedPeriodicParameter.isPresent()
				&& !Double.isFinite(liftedPeriodicParameter.getAsDouble())) {
			throw new IllegalArgumentException("Lifted parameter must be finite");
		}
		this.isolatingInterval = java.util.Objects.requireNonNull(
				isolatingInterval);
		if (!isolatingInterval.contains(semanticParameter, 0)) {
			throw new IllegalArgumentException(
					"Parameter must lie in its localization interval");
		}
	}

	public String getLocusIdentity() {
		return locusIdentity;
	}

	public long getSemanticRevision() {
		return semanticRevision;
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

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
