/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;

/** Approved construction-owned numeric semantic domain, version 1. */
public final class ExplicitNumericDomainProvider2D
		implements LocusDriverDomainProvider2D {
	public static final String PROVIDER_ID = "explicit-numeric-domain/v1";

	private final String descriptor;
	private final LocusInterval2D domain;
	private final Orientation orientation;
	private final boolean periodic;
	private final double domainEpsilon;

	/** Creates a provider with explicitly owned endpoint policy. */
	public ExplicitNumericDomainProvider2D(String descriptor,
			LocusInterval2D domain, Orientation orientation, boolean periodic,
			double domainEpsilon) {
		if (descriptor == null || descriptor.trim().isEmpty()) {
			throw new IllegalArgumentException("Parameter descriptor is required");
		}
		if (!Double.isFinite(domainEpsilon) || domainEpsilon < 0) {
			throw new IllegalArgumentException("eps_domain must be finite and nonnegative");
		}
		this.descriptor = descriptor;
		this.domain = Objects.requireNonNull(domain);
		this.orientation = Objects.requireNonNull(orientation);
		this.periodic = periodic;
		this.domainEpsilon = domainEpsilon;
		if (periodic && domain.getUpper() == domain.getLower()) {
			throw new IllegalArgumentException("A periodic domain needs positive span");
		}
	}

	@Override
	public String getProviderId() {
		return PROVIDER_ID;
	}

	@Override
	public String getParameterDescriptor() {
		return descriptor;
	}

	@Override
	public LocusInterval2D getDeclaredDomain() {
		return domain;
	}

	@Override
	public Orientation getOrientation() {
		return orientation;
	}

	@Override
	public boolean isPeriodic() {
		return periodic;
	}

	@Override
	public double getDomainEpsilon() {
		return domainEpsilon;
	}

	@Override
	public double canonicalize(double parameter) {
		if (!Double.isFinite(parameter) || !periodic) {
			return parameter == 0 ? 0 : parameter;
		}
		double lower = domain.getLower();
		double period = domain.getUpper() - lower;
		double canonical = lower + (parameter - lower)
				- Math.floor((parameter - lower) / period) * period;
		return canonical == 0 ? 0 : canonical;
	}

	@Override
	public boolean contains(double canonicalParameter) {
		return Double.isFinite(canonicalParameter)
				&& (periodic || domain.contains(canonicalParameter, domainEpsilon));
	}

	@Override
	public String getSemanticSignature() {
		return PROVIDER_ID + "|" + descriptor + "|" + domain + "|"
				+ orientation + "|periodic=" + periodic + "|eps="
				+ Double.toHexString(domainEpsilon);
	}
}
