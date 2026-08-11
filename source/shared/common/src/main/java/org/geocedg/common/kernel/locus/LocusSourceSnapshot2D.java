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
		this.values = values.clone();
		for (double value : this.values) {
			if (!Double.isFinite(value)) {
				throw new IllegalArgumentException("Source values must be finite");
			}
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
	public String toString() {
		return Arrays.toString(values);
	}
}
