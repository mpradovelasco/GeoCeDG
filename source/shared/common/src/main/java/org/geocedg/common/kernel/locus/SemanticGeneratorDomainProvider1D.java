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
		return "true-coordinate/" + descriptor.getCoordinateId().toExternalForm();
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

	@Override
	public boolean contains(double canonicalParameter) {
		if (!Double.isFinite(canonicalParameter)) {
			return false;
		}
		for (LocusInterval2D component : descriptor.getValidComponents()) {
			if (component.contains(canonicalParameter, getDomainEpsilon())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getSemanticSignature() {
		return descriptor.getSemanticSignature();
	}
}
