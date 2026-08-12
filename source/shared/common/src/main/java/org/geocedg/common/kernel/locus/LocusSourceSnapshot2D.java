/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Arrays;

/** Immutable values captured from normal kernel inputs during one recompute. */
public final class LocusSourceSnapshot2D {
	private final double[] values;

	/** Captures finite source values without exposing mutable storage. */
	public LocusSourceSnapshot2D(double[] values) {
		if (values == null) {
			throw new IllegalArgumentException("Source values are required");
		}
		this.values = values.clone();
		for (int index = 0; index < this.values.length; index++) {
			double value = this.values[index];
			if (!Double.isFinite(value)) {
				throw new IllegalArgumentException("Source values must be finite");
			}
			this.values[index] = value == 0 ? 0 : value;
		}
	}

	/** @return number of captured normal-DAG inputs */
	public int size() {
		return values.length;
	}

	/** @return captured finite value at the input index */
	public double get(int index) {
		return values[index];
	}

	/**
	 * Deterministic content used to decide whether a revision changed.
	 *
	 * @return stable value signature
	 */
	public String getSemanticSignature() {
		StringBuilder signature = new StringBuilder();
		for (double value : values) {
			signature.append('|').append(Double.toHexString(value));
		}
		return signature.toString();
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof LocusSourceSnapshot2D
				&& Arrays.equals(values, ((LocusSourceSnapshot2D) other).values);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(values);
	}

	@Override
	public String toString() {
		return Arrays.toString(values);
	}
}
