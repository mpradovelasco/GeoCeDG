/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.Objects;

/**
 * One cell of a conditioned evidence. Its ends are dyadic fractions of the declared domain,
 * kept exactly as indices of the traversal grid. Two cells are the same cell when they have
 * the same ends, kind and class: that is the comparison of withdrawal and of currentness
 * (candidate contract {@code post-r1-conditioned-use.md}, section 7). A cell is never
 * identity material of a point, a root or a locus.
 */
public final class LocusConditionedCell2D {
	private final long lowerIndex;
	private final long upperIndex;
	private final double lower;
	private final double upper;
	private final LocusConditionedCellKind2D kind;
	private final LocusConditionedSpanClass2D spanClass;
	private final double chordDeviation;
	private final String cause;

	LocusConditionedCell2D(long lowerIndex, long upperIndex, double lower, double upper,
			LocusConditionedCellKind2D kind, LocusConditionedSpanClass2D spanClass,
			double chordDeviation, String cause) {
		this.lowerIndex = lowerIndex;
		this.upperIndex = upperIndex;
		this.lower = lower;
		this.upper = upper;
		this.kind = Objects.requireNonNull(kind);
		if (kind.isSpan() != (spanClass != null)) {
			throw new IllegalArgumentException("Only a span has an evidence class");
		}
		this.spanClass = spanClass;
		this.chordDeviation = chordDeviation;
		this.cause = cause == null ? "" : cause;
	}

	/** @return lower end, as an index of the traversal grid */
	public long getLowerIndex() {
		return lowerIndex;
	}

	/** @return upper end, as an index of the traversal grid */
	public long getUpperIndex() {
		return upperIndex;
	}

	/** @return lower end, as a canonical parameter */
	public double getLower() {
		return lower;
	}

	/** @return upper end, as a canonical parameter */
	public double getUpper() {
		return upper;
	}

	/** @return kind of the cell */
	public LocusConditionedCellKind2D getKind() {
		return kind;
	}

	/** @return evidence class of a span; {@code null} for every other kind */
	public LocusConditionedSpanClass2D getSpanClass() {
		return spanClass;
	}

	/**
	 * @return chord deviation checked at the nodes of a span, in model units of the locus
	 *         that publishes the evidence; {@link Double#NaN} for every other kind
	 */
	public double getChordDeviation() {
		return chordDeviation;
	}

	/** @return cause that the nodes of a band reported, as information; may be empty */
	public String getCause() {
		return cause;
	}

	/**
	 * @param parameter canonical parameter
	 * @return whether the closed cell contains the parameter
	 */
	public boolean contains(double parameter) {
		return lower <= parameter && parameter <= upper;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusConditionedCell2D)) {
			return false;
		}
		LocusConditionedCell2D cell = (LocusConditionedCell2D) other;
		return lowerIndex == cell.lowerIndex && upperIndex == cell.upperIndex
				&& kind == cell.kind && spanClass == cell.spanClass;
	}

	@Override
	public int hashCode() {
		return Objects.hash(lowerIndex, upperIndex, kind, spanClass);
	}

	@Override
	public String toString() {
		return lowerIndex + ":" + upperIndex + ":" + kind
				+ (spanClass == null ? "" : "/" + spanClass);
	}
}
