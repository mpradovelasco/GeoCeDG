/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;

/**
 * Approved immutable semantic mappings for segment, circle and ellipse drivers.
 * This provider never adopts public normalized PathParameter as an identity.
 */
public final class StablePathDomainProvider2D
		implements LocusDriverDomainProvider2D {
	public static final String PROVIDER_ID = "stable-path-domain/v1";

	/** G6B path families whose native mapping was approved by G6A. */
	public enum PathFamily {
		SEGMENT, CIRCLE, ELLIPSE
	}

	private final String descriptor;
	private final PathFamily family;
	private final LocusPoint2D centerOrStart;
	private final LocusPoint2D firstAxisOrEnd;
	private final LocusPoint2D secondAxis;
	private final ExplicitNumericDomainProvider2D domainProvider;

	private StablePathDomainProvider2D(String descriptor, PathFamily family,
			LocusPoint2D centerOrStart, LocusPoint2D firstAxisOrEnd,
			LocusPoint2D secondAxis, ExplicitNumericDomainProvider2D domainProvider) {
		if (descriptor == null || descriptor.trim().isEmpty()) {
			throw new IllegalArgumentException("Path parameter descriptor is required");
		}
		this.descriptor = descriptor;
		this.family = Objects.requireNonNull(family);
		this.centerOrStart = Objects.requireNonNull(centerOrStart);
		this.firstAxisOrEnd = Objects.requireNonNull(firstAxisOrEnd);
		this.secondAxis = secondAxis;
		this.domainProvider = Objects.requireNonNull(domainProvider);
	}

	/**
	 * Creates the approved segment parameter t in [0,1].
	 *
	 * @return immutable segment provider
	 */
	public static StablePathDomainProvider2D segment(String descriptor,
			LocusPoint2D start, LocusPoint2D end, double domainEpsilon) {
		return new StablePathDomainProvider2D(descriptor, PathFamily.SEGMENT,
				start, end, null,
				new ExplicitNumericDomainProvider2D(descriptor,
						new LocusInterval2D(0, 1, true, true),
						Orientation.INCREASING, false, domainEpsilon));
	}

	/**
	 * Creates the approved angular circle parameter t in [-pi,pi).
	 *
	 * @return immutable circle provider
	 */
	public static StablePathDomainProvider2D circle(String descriptor,
			LocusPoint2D center, double radius, double domainEpsilon) {
		if (!Double.isFinite(radius) || radius <= 0) {
			throw new IllegalArgumentException("Circle radius must be positive");
		}
		return ellipseInternal(descriptor, PathFamily.CIRCLE, center,
				new LocusPoint2D(radius, 0), new LocusPoint2D(0, radius),
				domainEpsilon);
	}

	/**
	 * Creates the approved angular ellipse parameter t in [-pi,pi).
	 *
	 * @return immutable ellipse provider
	 */
	public static StablePathDomainProvider2D ellipse(String descriptor,
			LocusPoint2D center, LocusPoint2D firstAxis,
			LocusPoint2D secondAxis, double domainEpsilon) {
		if (firstAxis.getX() * secondAxis.getY()
				- firstAxis.getY() * secondAxis.getX() == 0) {
			throw new IllegalArgumentException("Ellipse axes must be independent");
		}
		return ellipseInternal(descriptor, PathFamily.ELLIPSE, center,
				firstAxis, secondAxis, domainEpsilon);
	}

	private static StablePathDomainProvider2D ellipseInternal(String descriptor,
			PathFamily family, LocusPoint2D center, LocusPoint2D firstAxis,
			LocusPoint2D secondAxis, double domainEpsilon) {
		return new StablePathDomainProvider2D(descriptor, family, center,
				firstAxis, secondAxis,
				new ExplicitNumericDomainProvider2D(descriptor,
						new LocusInterval2D(-Math.PI, Math.PI, true, false),
						Orientation.INCREASING, true, domainEpsilon));
	}

	/**
	 * Evaluates the immutable provider mapping, not a render or Path sample.
	 *
	 * @return finite driver point for the provider-canonical parameter
	 */
	public LocusPoint2D evaluateDriverPoint(double semanticParameter) {
		double parameter = canonicalize(semanticParameter);
		if (!contains(parameter)) {
			throw new IllegalArgumentException("Parameter outside semantic domain");
		}
		if (family == PathFamily.SEGMENT) {
			double x = centerOrStart.getX()
					+ parameter * (firstAxisOrEnd.getX() - centerOrStart.getX());
			double y = centerOrStart.getY()
					+ parameter * (firstAxisOrEnd.getY() - centerOrStart.getY());
			return new LocusPoint2D(x, y);
		}
		double cosine = Math.cos(parameter);
		double sine = Math.sin(parameter);
		return new LocusPoint2D(
				centerOrStart.getX() + firstAxisOrEnd.getX() * cosine
						+ secondAxis.getX() * sine,
				centerOrStart.getY() + firstAxisOrEnd.getY() * cosine
						+ secondAxis.getY() * sine);
	}

	public PathFamily getPathFamily() {
		return family;
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
		return domainProvider.getDeclaredDomain();
	}

	@Override
	public Orientation getOrientation() {
		return domainProvider.getOrientation();
	}

	@Override
	public boolean isPeriodic() {
		return domainProvider.isPeriodic();
	}

	@Override
	public double getDomainEpsilon() {
		return domainProvider.getDomainEpsilon();
	}

	@Override
	public double canonicalize(double parameter) {
		return domainProvider.canonicalize(parameter);
	}

	@Override
	public boolean contains(double canonicalParameter) {
		return domainProvider.contains(canonicalParameter);
	}

	@Override
	public String getSemanticSignature() {
		return PROVIDER_ID + "|" + family + "|" + descriptor + "|"
				+ centerOrStart + "|" + firstAxisOrEnd + "|" + secondAxis
				+ "|" + domainProvider.getSemanticSignature();
	}
}
