/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.Objects;

/**
 * Testimony of one selecting dependency at one evaluation (candidate contract
 * {@code post-r1-conditioned-use.md}, section 3.2): which selection the dependency made and
 * whether that selection is established. It is read from evidence that the slice computed
 * for that evaluation. A selection is never identified by index, proximity, order of
 * coordinates or visual coincidence.
 */
public final class LocusSelectionTestimony2D {
	/** Kind of the selecting dependency. */
	public enum Kind {
		/** Token point of a Locus V2 intersection. */
		LOCUS_ROOT_TOKEN,
		/** Inherited line-conic selection with a settled state. */
		LINE_CONIC_SELECTION,
		/** Structural point with its resolution and integrity. */
		STRUCTURAL_POINT
	}

	private final String operationIdentity;
	private final Kind kind;
	private final boolean established;
	private final String selectionKey;
	private final double separation;

	private LocusSelectionTestimony2D(String operationIdentity, Kind kind,
			boolean established, String selectionKey, double separation) {
		this.operationIdentity = requireText(operationIdentity, "Operation identity");
		this.kind = Objects.requireNonNull(kind);
		this.established = established;
		this.selectionKey = established ? requireText(selectionKey, "Selection key") : "";
		if (separation < 0) {
			throw new IllegalArgumentException("A separation cannot be negative");
		}
		this.separation = separation;
	}

	/**
	 * @param operationIdentity durable identity of the selecting operation in the slice
	 * @param kind kind of the dependency
	 * @param selectionKey exact token or settled selection state, never an index
	 * @param separation distance to the nearest other root of the same result, or
	 *        {@link Double#NaN} when the dependency does not testify one
	 * @return testimony of an established selection
	 */
	public static LocusSelectionTestimony2D established(String operationIdentity, Kind kind,
			String selectionKey, double separation) {
		return new LocusSelectionTestimony2D(operationIdentity, kind, true, selectionKey,
				separation);
	}

	/**
	 * @param operationIdentity durable identity of the selecting operation in the slice
	 * @param kind kind of the dependency
	 * @return testimony of a selection that is not established at this evaluation
	 */
	public static LocusSelectionTestimony2D notEstablished(String operationIdentity,
			Kind kind) {
		return new LocusSelectionTestimony2D(operationIdentity, kind, false, "", Double.NaN);
	}

	/** @return durable identity of the selecting operation in the slice */
	public String getOperationIdentity() {
		return operationIdentity;
	}

	/** @return kind of the dependency */
	public Kind getKind() {
		return kind;
	}

	/** @return whether the deterministic selection is established */
	public boolean isEstablished() {
		return established;
	}

	/** @return exact token or settled selection state; empty when not established */
	public String getSelectionKey() {
		return selectionKey;
	}

	/** @return whether the dependency testifies a separation */
	public boolean hasSeparation() {
		return !Double.isNaN(separation);
	}

	/** @return testified separation, or {@link Double#NaN} */
	public double getSeparation() {
		return separation;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty() || !value.equals(value.trim())) {
			throw new IllegalArgumentException(name + " must be canonical");
		}
		return value;
	}
}
