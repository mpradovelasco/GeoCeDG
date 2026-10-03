/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.units;

/**
 * Result of a section 5.3 conversion: a finite value, the unspecified state, or the
 * presentation failure state of a non-finite result. It is never an exception and
 * never a silent infinity.
 */
public final class UnitQuantity {
	/** No physical interpretation exists. */
	public static final UnitQuantity UNSPECIFIED = new UnitQuantity(Status.UNSPECIFIED,
			Double.NaN);

	/** Conversion status. */
	public enum Status {
		/** A finite value. */
		FINITE,
		/** The construction unit is unspecified. */
		UNSPECIFIED,
		/** The result overflowed or is otherwise not finite. */
		NOT_FINITE
	}

	private final Status status;
	private final double value;

	private UnitQuantity(Status status, double value) {
		this.status = status;
		this.value = value;
	}

	static UnitQuantity of(double value) {
		return Double.isNaN(value) || Double.isInfinite(value)
				? new UnitQuantity(Status.NOT_FINITE, Double.NaN)
				: new UnitQuantity(Status.FINITE, value);
	}

	/**
	 * @return status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return the finite value
	 * @throws IllegalStateException when the status is not {@link Status#FINITE}
	 */
	public double getValue() {
		if (status != Status.FINITE) {
			throw new IllegalStateException("No finite quantity: " + status);
		}
		return value;
	}
}
