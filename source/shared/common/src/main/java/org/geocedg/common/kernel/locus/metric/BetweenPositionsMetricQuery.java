/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Directed metric query between two revision-bound semantic positions. */
public final class BetweenPositionsMetricQuery implements LocusMetricQuery2D {
	private final MetricPositionBinding2D start;
	private final MetricPositionBinding2D target;
	private final TraversalDirection direction;
	private final OpenBoundaryPolicy boundaryPolicy;
	private final SamePositionPolicy samePositionPolicy;
	private final LocusMetricPolicy2D policy;

	/** Creates an immutable between-position query. */
	public BetweenPositionsMetricQuery(MetricPositionBinding2D start,
			MetricPositionBinding2D target, TraversalDirection direction,
			OpenBoundaryPolicy boundaryPolicy,
			SamePositionPolicy samePositionPolicy, LocusMetricPolicy2D policy) {
		this.start = Objects.requireNonNull(start);
		this.target = Objects.requireNonNull(target);
		this.direction = Objects.requireNonNull(direction);
		this.boundaryPolicy = Objects.requireNonNull(boundaryPolicy);
		this.samePositionPolicy = Objects.requireNonNull(samePositionPolicy);
		this.policy = Objects.requireNonNull(policy);
	}

	public MetricPositionBinding2D getStart() {
		return start;
	}

	public MetricPositionBinding2D getTarget() {
		return target;
	}

	public TraversalDirection getDirection() {
		return direction;
	}

	public OpenBoundaryPolicy getBoundaryPolicy() {
		return boundaryPolicy;
	}

	public SamePositionPolicy getSamePositionPolicy() {
		return samePositionPolicy;
	}

	@Override
	public String getLocusIdentity() {
		return start.getSemanticPosition().getLocusIdentity();
	}

	@Override
	public long getSemanticRevision() {
		return start.getSemanticRevision();
	}

	@Override
	public LocusMetricPolicy2D getPolicy() {
		return policy;
	}
}
