/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Arrays;

/** Defensive immutable adaptive partition for one valid component. */
public final class MetricComponentPartition2D {
	private final double[] parameters;
	private final double[] intervalLengths;
	private final double[] intervalErrors;

	/** Creates an ordered finite component partition. */
	public MetricComponentPartition2D(double[] parameters,
			double[] intervalLengths, double[] intervalErrors) {
		if (parameters == null || intervalLengths == null
				|| intervalErrors == null || parameters.length < 1
				|| intervalLengths.length != Math.max(0, parameters.length - 1)
				|| intervalErrors.length != intervalLengths.length) {
			throw new IllegalArgumentException("Inconsistent metric partition shape");
		}
		this.parameters = parameters.clone();
		this.intervalLengths = intervalLengths.clone();
		this.intervalErrors = intervalErrors.clone();
		validate();
	}

	public int getNodeCount() {
		return parameters.length;
	}

	public int getIntervalCount() {
		return intervalLengths.length;
	}

	public double[] getParameters() {
		return parameters.clone();
	}

	public double[] getIntervalLengths() {
		return intervalLengths.clone();
	}

	public double[] getIntervalErrors() {
		return intervalErrors.clone();
	}

	/**
	 * Approximate immutable payload size for bounded-state evidence.
	 *
	 * @return approximate retained bytes
	 */
	public long getApproximateRetainedBytes() {
		return 48L + 8L * (parameters.length + intervalLengths.length
				+ intervalErrors.length);
	}

	private void validate() {
		for (int index = 0; index < parameters.length; index++) {
			if (!Double.isFinite(parameters[index])
					|| index > 0 && parameters[index] <= parameters[index - 1]) {
				throw new IllegalArgumentException(
						"Partition parameters must be finite and strictly ordered");
			}
		}
		for (double length : intervalLengths) {
			if (!Double.isFinite(length) || length < 0) {
				throw new IllegalArgumentException(
						"Partition lengths must be finite and non-negative");
			}
		}
		for (double error : intervalErrors) {
			if (!Double.isFinite(error) || error < 0) {
				throw new IllegalArgumentException(
						"Partition errors must be finite and non-negative");
			}
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MetricComponentPartition2D)) {
			return false;
		}
		MetricComponentPartition2D partition =
				(MetricComponentPartition2D) other;
		return Arrays.equals(parameters, partition.parameters)
				&& Arrays.equals(intervalLengths, partition.intervalLengths)
				&& Arrays.equals(intervalErrors, partition.intervalErrors);
	}

	@Override
	public int hashCode() {
		return 31 * (31 * Arrays.hashCode(parameters)
				+ Arrays.hashCode(intervalLengths))
				+ Arrays.hashCode(intervalErrors);
	}
}
