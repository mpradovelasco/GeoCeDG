/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

/**
 * Structural source of a component end or of an isolated parameter
 * ({@code locus-v2-existence-components.md} sections 15.4 and 15.6): the durable operation
 * or event that produces it, the predicate or event kind, and the side or root.
 *
 * <p>It is the lineage of a boundary: moving a boundary without changing its source keeps
 * the lineage. It is never a coordinate, an index or a label.</p>
 */
public final class LocusBoundarySource2D {
	private static final String DOMAIN = "declared-domain";

	private final String origin;
	private final String predicate;
	private final String side;

	/**
	 * @param origin durable operation identity, event source or declared domain
	 * @param predicate predicate kind, event kind or domain end
	 * @param side side or isolated root that the source selects
	 */
	public LocusBoundarySource2D(String origin, String predicate, String side) {
		this.origin = requireToken(origin, "Boundary origin");
		this.predicate = requireToken(predicate, "Boundary predicate");
		this.side = requireToken(side, "Boundary side");
	}

	/** @return source of the lower end of the declared domain */
	public static LocusBoundarySource2D declaredLower() {
		return new LocusBoundarySource2D(DOMAIN, "domain-end", "lower");
	}

	/** @return source of the upper end of the declared domain */
	public static LocusBoundarySource2D declaredUpper() {
		return new LocusBoundarySource2D(DOMAIN, "domain-end", "upper");
	}

	/** @return durable operation identity, event source or declared domain */
	public String getOrigin() {
		return origin;
	}

	/** @return predicate kind, event kind or domain end */
	public String getPredicate() {
		return predicate;
	}

	/** @return side or isolated root selected by the source */
	public String getSide() {
		return side;
	}

	/** @return canonical text of the source */
	public String getSignature() {
		return origin + "/" + predicate + "/" + side;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusBoundarySource2D)) {
			return false;
		}
		LocusBoundarySource2D source = (LocusBoundarySource2D) other;
		return origin.equals(source.origin) && predicate.equals(source.predicate)
				&& side.equals(source.side);
	}

	@Override
	public int hashCode() {
		return Objects.hash(origin, predicate, side);
	}

	@Override
	public String toString() {
		return getSignature();
	}

	private static String requireToken(String value, String name) {
		if (value == null || value.isEmpty() || !value.equals(value.trim())
				|| value.indexOf('/') >= 0 || value.indexOf('|') >= 0) {
			throw new IllegalArgumentException(name + " must be a canonical token");
		}
		return value;
	}
}
