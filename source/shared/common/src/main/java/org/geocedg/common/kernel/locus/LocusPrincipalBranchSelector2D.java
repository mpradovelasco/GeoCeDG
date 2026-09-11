/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;

/** Exact, order-independent selector for the optional-branch Point overload. */
public final class LocusPrincipalBranchSelector2D {
	private LocusPrincipalBranchSelector2D() {
		// Utility class.
	}

	/**
	 * Selects an address only from a complete, deterministic one-branch source.
	 *
	 * @return the concrete semantic address selected at {@code rawParameter}
	 * @throws IllegalArgumentException when eligibility is absent or ambiguous
	 */
	public static LocusSemanticAddress2D select(GeoLocusV2 source,
			double rawParameter) {
		if (source == null || !source.isDefined() || !Double.isFinite(rawParameter)) {
			throw new IllegalArgumentException(
					"Principal-branch eligibility is not established");
		}
		PersistentGeoId sourceId = source.getPersistentLocusId();
		LocusDefinition2D definition = source.getSemanticDefinition();
		if (sourceId == null || definition == null
				|| definition.getDefinitionStatus() != DefinitionStatus.VALID
				|| definition.getDeterminism()
						== Determinism.UNSUPPORTED_NONDETERMINISM) {
			throw new IllegalArgumentException(
					"Principal-branch eligibility is not established");
		}
		if (definition.getBranches().isEmpty()) {
			throw new IllegalArgumentException("No eligible principal branch");
		}
		if (definition.getBranches().size() != 1) {
			throw new IllegalArgumentException("Multiple eligible principal branches");
		}
		LocusDriverDomainProvider2D provider = definition.getProvider();
		double canonical = provider.canonicalize(rawParameter);
		if (!Double.isFinite(canonical) || !provider.contains(canonical)) {
			throw new IllegalArgumentException("No eligible semantic component");
		}
		LocusBranch2D branch = definition.getBranches().get(0);
		LocusInterval2D selected = null;
		for (LocusInterval2D component : branch.getValidDomainComponents()) {
			if (component.contains(canonical, provider.getDomainEpsilon())) {
				if (selected != null) {
					throw new IllegalArgumentException(
							"Multiple eligible semantic components");
				}
				selected = component;
			}
		}
		if (selected == null) {
			throw new IllegalArgumentException("No eligible semantic component");
		}
		Long lift = periodicLift(provider, rawParameter, canonical);
		if (lift == null) {
			throw new IllegalArgumentException(
					"Principal-branch periodic address is not established");
		}
		return new LocusSemanticAddress2D(sourceId, provider.getProviderId(),
				branch.getBranchKey(),
				LocusComponentLineage2D.create(branch.getBranchKey(), selected),
				canonical, lift,
				seamSide(provider, rawParameter, canonical, lift));
	}

	private static Long periodicLift(LocusDriverDomainProvider2D provider,
			double raw, double canonical) {
		if (!provider.isPeriodic()) {
			return 0L;
		}
		LocusInterval2D domain = provider.getDeclaredDomain();
		double period = domain.getUpper() - domain.getLower();
		double quotient = Math.floor((raw - domain.getLower()) / period);
		if (!Double.isFinite(quotient) || quotient < Long.MIN_VALUE
				|| quotient > Long.MAX_VALUE) {
			return null;
		}
		long lift = (long) quotient;
		if (!domain.isLowerClosed() && canonical == domain.getUpper()) {
			lift--;
		}
		return lift;
	}

	private static SeamSide seamSide(LocusDriverDomainProvider2D provider,
			double raw, double canonical, long lift) {
		if (!provider.isPeriodic()) {
			return SeamSide.NOT_PERIODIC;
		}
		LocusInterval2D domain = provider.getDeclaredDomain();
		if (canonical != domain.getLower() && canonical != domain.getUpper()) {
			return SeamSide.INTERIOR;
		}
		boolean nativeEndpoint = raw == canonical && lift == 0;
		return nativeEndpoint && canonical == domain.getLower()
				? SeamSide.LOWER_APPROACH : SeamSide.UPPER_APPROACH;
	}
}
