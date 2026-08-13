/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** One ordered route segment wholly contained in one valid component. */
public final class LocusMetricRouteSegment2D {
	private final String resolvedValidComponentKey;
	private final double startCanonicalParameter;
	private final double endCanonicalParameter;
	private final TraversalDirection direction;
	private final MetricRouteSegmentRole role;

	/** Creates a finite component-confined route segment. */
	public LocusMetricRouteSegment2D(String resolvedValidComponentKey,
			double startCanonicalParameter, double endCanonicalParameter,
			TraversalDirection direction, MetricRouteSegmentRole role) {
		if (resolvedValidComponentKey == null
				|| resolvedValidComponentKey.trim().isEmpty()
				|| !Double.isFinite(startCanonicalParameter)
				|| !Double.isFinite(endCanonicalParameter)) {
			throw new IllegalArgumentException("Invalid metric route segment");
		}
		this.resolvedValidComponentKey = resolvedValidComponentKey;
		this.startCanonicalParameter = startCanonicalParameter == 0
				? 0 : startCanonicalParameter;
		this.endCanonicalParameter = endCanonicalParameter == 0
				? 0 : endCanonicalParameter;
		this.direction = Objects.requireNonNull(direction);
		this.role = Objects.requireNonNull(role);
	}

	public String getResolvedValidComponentKey() {
		return resolvedValidComponentKey;
	}

	public double getStartCanonicalParameter() {
		return startCanonicalParameter;
	}

	public double getEndCanonicalParameter() {
		return endCanonicalParameter;
	}

	public TraversalDirection getDirection() {
		return direction;
	}

	public MetricRouteSegmentRole getRole() {
		return role;
	}
}
