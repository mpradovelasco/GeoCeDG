/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/**
 * Versioned constants of metric policy v2 ({@code locus-v2-metrics.md} section 25).
 * They are implementation-policy constants justified by benchmarks within the
 * intervals approved for decision D-CU1-4, not mathematical constants.
 */
public final class MetricEstimatorPolicy2D {
	/** Golden-section fraction: a non-dyadic interior probe position. */
	public static final double GOLDEN_SECTION_FRACTION =
			0.6180339887498949;

	private final String version;
	private final int minimumUniformDepth;
	private final double probeFraction;
	private final double safetyFactor;
	private final int maximumEnclosureBoxes;

	/** Creates one immutable estimator policy. */
	public MetricEstimatorPolicy2D(String version, int minimumUniformDepth,
			double probeFraction, double safetyFactor,
			int maximumEnclosureBoxes) {
		if (version == null || version.trim().isEmpty()
				|| minimumUniformDepth < 0 || minimumUniformDepth > 16
				|| !(probeFraction > 0 && probeFraction < 1)
				|| probeFraction == 0.5 || probeFraction == 0.25
				|| probeFraction == 0.75 || !(safetyFactor >= 1)
				|| !Double.isFinite(safetyFactor) || maximumEnclosureBoxes < 1) {
			throw new IllegalArgumentException("Invalid metric estimator policy");
		}
		this.version = version;
		this.minimumUniformDepth = minimumUniformDepth;
		this.probeFraction = probeFraction;
		this.safetyFactor = safetyFactor;
		this.maximumEnclosureBoxes = maximumEnclosureBoxes;
	}

	/**
	 * Default PRE-G9B-CU1 estimator policy. The constants lie in the intervals
	 * approved with decision D-CU1-4 and were fixed by the CU1 calibration bench:
	 * {@code d_min = 6}, the golden-section probe fraction, safety factor 2 and
	 * 16 384 enclosure boxes.
	 *
	 * @return the default estimator policy
	 */
	public static MetricEstimatorPolicy2D cu1Default() {
		return new MetricEstimatorPolicy2D("cu1-estimator/v2", 6,
				GOLDEN_SECTION_FRACTION, 2, 16_384);
	}

	public String getVersion() {
		return version;
	}

	public int getMinimumUniformDepth() {
		return minimumUniformDepth;
	}

	public double getProbeFraction() {
		return probeFraction;
	}

	public double getSafetyFactor() {
		return safetyFactor;
	}

	public int getMaximumEnclosureBoxes() {
		return maximumEnclosureBoxes;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MetricEstimatorPolicy2D)) {
			return false;
		}
		MetricEstimatorPolicy2D policy = (MetricEstimatorPolicy2D) other;
		return version.equals(policy.version)
				&& minimumUniformDepth == policy.minimumUniformDepth
				&& Double.doubleToLongBits(probeFraction)
						== Double.doubleToLongBits(policy.probeFraction)
				&& Double.doubleToLongBits(safetyFactor)
						== Double.doubleToLongBits(policy.safetyFactor)
				&& maximumEnclosureBoxes == policy.maximumEnclosureBoxes;
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, minimumUniformDepth,
				Double.doubleToLongBits(probeFraction),
				Double.doubleToLongBits(safetyFactor), maximumEnclosureBoxes);
	}

	@Override
	public String toString() {
		return version + "|dmin=" + minimumUniformDepth + "|probe=" + probeFraction
				+ "|safety=" + safetyFactor + "|boxes=" + maximumEnclosureBoxes;
	}
}
