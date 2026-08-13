/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable resolved route; it contains no integrated metric value. */
public final class LocusMetricRoute2D {
	private final String locusIdentity;
	private final long semanticRevision;
	private final String branchKey;
	private final List<LocusMetricRouteSegment2D> orderedRouteSegments;
	private final TraversalDirection direction;
	private final OpenBoundaryPolicy boundaryPolicy;
	private final boolean targetReached;
	private final boolean wrapped;
	private final boolean geometricallyConnected;
	private final MetricRouteStatus routeStatus;
	private final TraversalOutcome traversalOutcome;
	private final List<MetricDiagnostic2D> diagnostics;

	/** Creates a complete route-resolution result. */
	public LocusMetricRoute2D(String locusIdentity, long semanticRevision,
			String branchKey,
			List<LocusMetricRouteSegment2D> orderedRouteSegments,
			TraversalDirection direction, OpenBoundaryPolicy boundaryPolicy,
			boolean targetReached, boolean wrapped,
			boolean geometricallyConnected, MetricRouteStatus routeStatus,
			TraversalOutcome traversalOutcome,
			List<MetricDiagnostic2D> diagnostics) {
		if (locusIdentity == null || locusIdentity.trim().isEmpty()
				|| semanticRevision < 1 || branchKey == null
				|| branchKey.trim().isEmpty()) {
			throw new IllegalArgumentException("Complete route identity is required");
		}
		this.locusIdentity = locusIdentity;
		this.semanticRevision = semanticRevision;
		this.branchKey = branchKey;
		this.orderedRouteSegments = immutableSegments(orderedRouteSegments);
		this.direction = Objects.requireNonNull(direction);
		this.boundaryPolicy = Objects.requireNonNull(boundaryPolicy);
		this.targetReached = targetReached;
		this.wrapped = wrapped;
		this.geometricallyConnected = geometricallyConnected;
		this.routeStatus = Objects.requireNonNull(routeStatus);
		this.traversalOutcome = Objects.requireNonNull(traversalOutcome);
		this.diagnostics = immutableDiagnostics(diagnostics);
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

	public List<LocusMetricRouteSegment2D> getOrderedRouteSegments() {
		return orderedRouteSegments;
	}

	public TraversalDirection getDirection() {
		return direction;
	}

	public OpenBoundaryPolicy getBoundaryPolicy() {
		return boundaryPolicy;
	}

	public boolean isTargetReached() {
		return targetReached;
	}

	public boolean isWrapped() {
		return wrapped;
	}

	public boolean isGeometricallyConnected() {
		return geometricallyConnected;
	}

	public MetricRouteStatus getRouteStatus() {
		return routeStatus;
	}

	public TraversalOutcome getTraversalOutcome() {
		return traversalOutcome;
	}

	public List<MetricDiagnostic2D> getDiagnostics() {
		return diagnostics;
	}

	private static List<LocusMetricRouteSegment2D> immutableSegments(
			List<LocusMetricRouteSegment2D> input) {
		Objects.requireNonNull(input);
		ArrayList<LocusMetricRouteSegment2D> copy = new ArrayList<>();
		for (LocusMetricRouteSegment2D segment : input) {
			copy.add(Objects.requireNonNull(segment));
		}
		return Collections.unmodifiableList(copy);
	}

	private static List<MetricDiagnostic2D> immutableDiagnostics(
			List<MetricDiagnostic2D> input) {
		Objects.requireNonNull(input);
		ArrayList<MetricDiagnostic2D> copy = new ArrayList<>();
		for (MetricDiagnostic2D diagnostic : input) {
			copy.add(Objects.requireNonNull(diagnostic));
		}
		return Collections.unmodifiableList(copy);
	}
}
