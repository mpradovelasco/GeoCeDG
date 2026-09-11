/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/** Stable public traversal vocabulary for between-position Locus V2 metrics. */
public final class PublicLocusMetricTraversalPolicy2D {
	/** Public forward token. */
	public static final String FORWARD = "forward";
	/** Public reverse token. */
	public static final String REVERSE = "reverse";
	/** Public strict-boundary token. */
	public static final String STRICT = "strict";
	/** Public stop-at-end token. */
	public static final String STOP_AT_END = "stop-at-end";
	/** Public wrap-to-start token. */
	public static final String WRAP_TO_START = "wrap-to-start";
	/** Public zero-length token. */
	public static final String ZERO_LENGTH = "zero-length";
	/** Public full-cycle token. */
	public static final String FULL_CYCLE = "full-cycle";

	private final TraversalDirection direction;
	private final OpenBoundaryPolicy boundaryPolicy;
	private final SamePositionPolicy samePositionPolicy;

	private PublicLocusMetricTraversalPolicy2D(
			TraversalDirection direction,
			OpenBoundaryPolicy boundaryPolicy,
			SamePositionPolicy samePositionPolicy) {
		this.direction = Objects.requireNonNull(direction);
		this.boundaryPolicy = Objects.requireNonNull(boundaryPolicy);
		this.samePositionPolicy = Objects.requireNonNull(samePositionPolicy);
	}

	/**
	 * Parses the stable public tokens without exposing internal enum names.
	 *
	 * @return parsed traversal policy
	 */
	public static PublicLocusMetricTraversalPolicy2D parse(String direction,
			String boundaryPolicy, String samePositionPolicy) {
		return new PublicLocusMetricTraversalPolicy2D(
				parseDirection(direction), parseBoundary(boundaryPolicy),
				parseSamePosition(samePositionPolicy));
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

	private static TraversalDirection parseDirection(String token) {
		switch (normalized(token, "direction")) {
		case FORWARD:
			return TraversalDirection.FORWARD;
		case REVERSE:
			return TraversalDirection.REVERSE;
		default:
			throw new IllegalArgumentException("Unknown traversal direction");
		}
	}

	private static OpenBoundaryPolicy parseBoundary(String token) {
		switch (normalized(token, "boundary policy")) {
		case STRICT:
			return OpenBoundaryPolicy.STRICT;
		case STOP_AT_END:
			return OpenBoundaryPolicy.STOP_AT_END;
		case WRAP_TO_START:
			return OpenBoundaryPolicy.WRAP_TO_START;
		default:
			throw new IllegalArgumentException("Unknown traversal boundary policy");
		}
	}

	private static SamePositionPolicy parseSamePosition(String token) {
		switch (normalized(token, "same-position policy")) {
		case ZERO_LENGTH:
			return SamePositionPolicy.ZERO_LENGTH;
		case FULL_CYCLE:
			return SamePositionPolicy.FULL_CYCLE;
		default:
			throw new IllegalArgumentException("Unknown same-position policy");
		}
	}

	private static String normalized(String token, String kind) {
		if (token == null || token.trim().isEmpty()) {
			throw new IllegalArgumentException("Traversal " + kind + " is required");
		}
		return token.trim();
	}
}
