/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import javax.annotation.CheckForNull;

/**
 * Immutable result of {@link BSplineData#validate}: either validated data or exactly one
 * typed cause, never both.
 */
public final class BSplineValidation {

	private final @CheckForNull BSplineData data;
	private final @CheckForNull BSplineDataCause cause;

	private BSplineValidation(@CheckForNull BSplineData data,
			@CheckForNull BSplineDataCause cause) {
		this.data = data;
		this.cause = cause;
	}

	static BSplineValidation valid(BSplineData data) {
		return new BSplineValidation(data, null);
	}

	static BSplineValidation rejected(BSplineDataCause cause) {
		return new BSplineValidation(null, cause);
	}

	/**
	 * @return whether the data are valid
	 */
	public boolean isValid() {
		return data != null;
	}

	/**
	 * Returns the validated data.
	 *
	 * @return the data
	 * @throws IllegalStateException when the data were rejected (check {@link #isValid()}
	 *             first; this is a programming error, not a data outcome)
	 */
	public BSplineData data() {
		if (data == null) {
			throw new IllegalStateException("Rejected data: " + cause);
		}
		return data;
	}

	/**
	 * @return the single cause of a rejection, {@code null} when valid
	 */
	public @CheckForNull BSplineDataCause cause() {
		return cause;
	}
}
