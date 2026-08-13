/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Arrays;

/**
 * Per-component derived arc coordinate. It is never semantic position identity.
 */
public final class MetricArcCoordinateEvidence2D {
	private final double[] parameters;
	private final double[] cumulativeLengths;

	/** Creates a monotone component-confined arc-coordinate table. */
	public MetricArcCoordinateEvidence2D(double[] parameters,
			double[] cumulativeLengths) {
		if (parameters == null || cumulativeLengths == null
				|| parameters.length < 1
				|| parameters.length != cumulativeLengths.length) {
			throw new IllegalArgumentException(
					"Inconsistent arc-coordinate evidence shape");
		}
		this.parameters = parameters.clone();
		this.cumulativeLengths = cumulativeLengths.clone();
		validate();
	}

	public double[] getParameters() {
		return parameters.clone();
	}

	public double[] getCumulativeLengths() {
		return cumulativeLengths.clone();
	}

	/**
	 * Uses only this component's immutable cumulative evidence.
	 *
	 * @return non-negative subarc estimate
	 */
	public double estimateLength(double from, double to) {
		return Math.abs(cumulativeAt(to) - cumulativeAt(from));
	}

	public long getApproximateRetainedBytes() {
		return 32L + 8L * (parameters.length + cumulativeLengths.length);
	}

	private double cumulativeAt(double parameter) {
		if (parameter <= parameters[0]) {
			return cumulativeLengths[0];
		}
		int last = parameters.length - 1;
		if (parameter >= parameters[last]) {
			return cumulativeLengths[last];
		}
		int insertion = Arrays.binarySearch(parameters, parameter);
		if (insertion >= 0) {
			return cumulativeLengths[insertion];
		}
		int high = -insertion - 1;
		int low = high - 1;
		double fraction = (parameter - parameters[low])
				/ (parameters[high] - parameters[low]);
		return cumulativeLengths[low] + fraction
				* (cumulativeLengths[high] - cumulativeLengths[low]);
	}

	private void validate() {
		for (int index = 0; index < parameters.length; index++) {
			if (!Double.isFinite(parameters[index])
					|| !Double.isFinite(cumulativeLengths[index])
					|| cumulativeLengths[index] < 0
					|| index > 0 && parameters[index] <= parameters[index - 1]
					|| index > 0
							&& cumulativeLengths[index]
									< cumulativeLengths[index - 1]) {
				throw new IllegalArgumentException(
						"Arc-coordinate evidence must be finite and monotone");
			}
		}
		if (cumulativeLengths[0] != 0) {
			throw new IllegalArgumentException(
					"Component arc coordinate must start at zero");
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MetricArcCoordinateEvidence2D)) {
			return false;
		}
		MetricArcCoordinateEvidence2D evidence =
				(MetricArcCoordinateEvidence2D) other;
		return Arrays.equals(parameters, evidence.parameters)
				&& Arrays.equals(cumulativeLengths, evidence.cumulativeLengths);
	}

	@Override
	public int hashCode() {
		return 31 * Arrays.hashCode(parameters)
				+ Arrays.hashCode(cumulativeLengths);
	}
}
