/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;

/**
 * Origin of a branch-frame binding of the token ledger (ADR 0042; contract {@code
 * post-r1-conditioned-use.md}, section 6.5, points 4 and 5): the parameter address of the
 * root at the publication that allocated its token.
 *
 * <p>It is a creation datum, of the same nature as the parameter of a parameter address. It
 * is no part of the selector and none of the token, it is never updated by a later
 * resolution, and it can only withhold a token: the reuse guard asks whether the origin and
 * the current root lie in one conditioned region of the published content. It is an
 * address, not evidence: no cell, region, node or class is stored.</p>
 */
public final class IntersectionBranchFrameOrigin2D {
	private final long canonicalParameterBits;
	private final long periodicLift;
	private final SeamSide seamSide;

	/**
	 * @param canonicalParameter provider-canonical parameter of the root at the allocation
	 * @param periodicLift lift of the address
	 * @param seamSide seam side of the address
	 */
	public IntersectionBranchFrameOrigin2D(double canonicalParameter, long periodicLift,
			SeamSide seamSide) {
		if (!Double.isFinite(canonicalParameter)) {
			throw new IllegalArgumentException("Origin parameter must be finite");
		}
		this.canonicalParameterBits = Double.doubleToLongBits(
				canonicalParameter == 0 ? 0 : canonicalParameter);
		this.periodicLift = periodicLift;
		this.seamSide = java.util.Objects.requireNonNull(seamSide);
		if (seamSide == SeamSide.NOT_PERIODIC && periodicLift != 0) {
			throw new IllegalArgumentException(
					"A nonperiodic origin cannot carry a periodic lift");
		}
	}

	/** @return provider-canonical parameter of the origin */
	public double getCanonicalParameter() {
		return Double.longBitsToDouble(canonicalParameterBits);
	}

	/** @return periodic lift of the origin */
	public long getPeriodicLift() {
		return periodicLift;
	}

	/** @return seam side of the origin */
	public SeamSide getSeamSide() {
		return seamSide;
	}

	/** @return canonical durable encoding */
	public String toExternalForm() {
		return Long.toHexString(canonicalParameterBits) + "/" + periodicLift + "/"
				+ seamSide.name();
	}

	/** Restores one canonically encoded origin from durable ledger state. */
	static IntersectionBranchFrameOrigin2D parse(String externalForm) {
		String[] fields = java.util.Objects.requireNonNull(externalForm).split("/", -1);
		if (fields.length != 3) {
			throw new IllegalArgumentException("Malformed branch-frame origin");
		}
		IntersectionBranchFrameOrigin2D parsed;
		try {
			parsed = new IntersectionBranchFrameOrigin2D(Double.longBitsToDouble(
					Long.parseUnsignedLong(fields[0], 16)), Long.parseLong(fields[1]),
					SeamSide.valueOf(fields[2]));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Malformed branch-frame origin", exception);
		}
		if (!parsed.toExternalForm().equals(externalForm)) {
			throw new IllegalArgumentException("Branch-frame origin is not canonical");
		}
		return parsed;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof IntersectionBranchFrameOrigin2D
				&& toExternalForm().equals(
						((IntersectionBranchFrameOrigin2D) other).toExternalForm());
	}

	@Override
	public int hashCode() {
		return toExternalForm().hashCode();
	}

	@Override
	public String toString() {
		return toExternalForm();
	}
}
