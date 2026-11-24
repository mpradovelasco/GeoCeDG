/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import java.util.Objects;

import javax.annotation.CheckForNull;

/**
 * Immutable typed outcome of an operation that builds a value: either {@link BSplineStatus#OK}
 * with the value, or one non-OK status and no value.
 *
 * @param <T> value type
 */
public final class BSplineOutcome<T> {

	private final BSplineStatus status;
	private final @CheckForNull T value;

	private BSplineOutcome(BSplineStatus status, @CheckForNull T value) {
		this.status = status;
		this.value = value;
	}

	static <T> BSplineOutcome<T> ok(T value) {
		return new BSplineOutcome<>(BSplineStatus.OK, Objects.requireNonNull(value));
	}

	static <T> BSplineOutcome<T> failure(BSplineStatus status) {
		if (status == BSplineStatus.OK) {
			throw new IllegalArgumentException("A failure needs a non-OK status");
		}
		return new BSplineOutcome<>(status, null);
	}

	/**
	 * @return the status; {@link BSplineStatus#OK} exactly when a value is present
	 */
	public BSplineStatus status() {
		return status;
	}

	/**
	 * @return whether the operation produced its value
	 */
	public boolean isOk() {
		return status == BSplineStatus.OK;
	}

	/**
	 * Returns the value of a successful outcome.
	 *
	 * @return the value
	 * @throws IllegalStateException when the outcome is a failure (check {@link #isOk()}
	 *             first; this is a programming error, not a data outcome)
	 */
	public T value() {
		if (value == null) {
			throw new IllegalStateException("No value: " + status);
		}
		return value;
	}
}
