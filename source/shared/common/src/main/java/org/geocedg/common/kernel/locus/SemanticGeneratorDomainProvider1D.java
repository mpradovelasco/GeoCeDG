/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;

/** Domain provider backed by one reconstructible public generator descriptor. */
public final class SemanticGeneratorDomainProvider1D
		implements LocusDriverDomainProvider2D {
	/** Parameter grammar naming the durable true driving coordinate. */
	public static final String TRUE_COORDINATE_PREFIX = "true-coordinate/";

	private final SemanticGeneratorDescriptor1D descriptor;

	public SemanticGeneratorDomainProvider1D(
			SemanticGeneratorDescriptor1D descriptor) {
		this.descriptor = Objects.requireNonNull(descriptor);
	}

	@Override
	public String getProviderId() {
		return descriptor.getProviderId();
	}

	@Override
	public String getParameterDescriptor() {
		return TRUE_COORDINATE_PREFIX + descriptor.getCoordinateId().toExternalForm();
	}

	@Override
	public LocusInterval2D getDeclaredDomain() {
		return descriptor.getDeclaredDomain();
	}

	@Override
	public Orientation getOrientation() {
		return descriptor.getOrientation();
	}

	@Override
	public boolean isPeriodic() {
		return descriptor.isPeriodic();
	}

	@Override
	public double getDomainEpsilon() {
		return descriptor.getDomainEpsilon();
	}

	@Override
	public double canonicalize(double parameter) {
		if (!Double.isFinite(parameter) || !isPeriodic()) {
			return parameter == 0 ? 0 : parameter;
		}
		double lower = getDeclaredDomain().getLower();
		double period = getDeclaredDomain().getUpper() - lower;
		double canonical = lower + (parameter - lower)
				- Math.floor((parameter - lower) / period) * period;
		if (!getDeclaredDomain().isLowerClosed() && canonical == lower) {
			canonical = getDeclaredDomain().getUpper();
		}
		return canonical == 0 ? 0 : canonical;
	}

	/**
	 * Tests membership of a canonical parameter. The endpoint tolerance applies to the ends of a
	 * nonperiodic domain and to the included end of a periodic one. The excluded end of a
	 * periodic domain is its seam, which is no end of the circle: every canonical parameter
	 * below it belongs, so that the existence structure decides membership there (PRE-G9B-CU2;
	 * {@code locus-v2-existence-components.md} section 15: an arc across the seam contains the
	 * canonical parameters up to the seam, and endpoint inclusion is never an epsilon
	 * convenience).
	 */
	@Override
	public boolean contains(double canonicalParameter) {
		if (!Double.isFinite(canonicalParameter)) {
			return false;
		}
		LocusInterval2D domain = getDeclaredDomain();
		double epsilon = getDomainEpsilon();
		if (!isPeriodic()) {
			return domain.contains(canonicalParameter, epsilon);
		}
		boolean above = domain.isLowerClosed()
				? canonicalParameter >= domain.getLower() - epsilon
				: canonicalParameter > domain.getLower();
		boolean below = domain.isUpperClosed()
				? canonicalParameter <= domain.getUpper() + epsilon
				: canonicalParameter < domain.getUpper();
		return above && below;
	}

	@Override
	public String getSemanticSignature() {
		return descriptor.getSemanticSignature();
	}
}
