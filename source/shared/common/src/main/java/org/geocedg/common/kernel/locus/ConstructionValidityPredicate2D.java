/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

/**
 * Validity predicate of one operation of a class-v3 construction program
 * ({@code locus-v2-certified-construction-model.md} sections 10.4 and 10.5). A predicate
 * is a real function of the driver parameter whose sign decides the existence of the
 * operation's output.
 *
 * <p>Interval enclosures establish uniform invalidity only for the strict-inequality
 * predicates; an equality predicate (a vanishing divisor, direction or radius) is uniformly
 * invalid only through an exact structural identity. At a certified root of the predicate the
 * end of a component is closed for a discriminant and for a ray or segment parameter, and
 * open for a vanishing divisor, direction or radius.</p>
 */
public enum ConstructionValidityPredicate2D {
	/** Discriminant of a line-circle intersection; valid where it is nonnegative. */
	DISCRIMINANT("DISCRIMINANT_NEGATIVE", false, true),
	/** Parameter of a point along a ray; valid where it is nonnegative. */
	RAY_PARAMETER("RAY_PARAMETER_NEGATIVE", false, true),
	/** Parameter of a point along a segment; valid inside {@code [0, 1]}. */
	SEGMENT_PARAMETER("SEGMENT_PARAMETER_OUTSIDE", false, true),
	/** Direction of a line or vector; valid where it does not vanish. */
	DIRECTION("ZERO_DIRECTION", true, false),
	/** Radius of a circle through a point; valid where it does not vanish. */
	RADIUS("ZERO_RADIUS", true, false),
	/** Divisor of a homogeneous normalization; valid where it does not vanish. */
	DIVISOR("DIVISOR_ZERO", true, false);

	private final String invalidReason;
	private final boolean equality;
	private final boolean closedAtRoot;

	ConstructionValidityPredicate2D(String invalidReason, boolean equality,
			boolean closedAtRoot) {
		this.invalidReason = invalidReason;
		this.equality = equality;
		this.closedAtRoot = closedAtRoot;
	}

	/** @return typed reason of a uniformly invalid box, as named by the contract */
	public String getInvalidReason() {
		return invalidReason;
	}

	/**
	 * @return whether invalidity is the vanishing of the predicate, which interval
	 *         enclosures never establish
	 */
	public boolean isEquality() {
		return equality;
	}

	/** @return whether a component end at a certified root of the predicate is closed */
	public boolean isClosedAtRoot() {
		return closedAtRoot;
	}
}
