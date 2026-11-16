/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.metric;

import java.util.Objects;

/**
 * Revision-local outward enclosures of a metric function and its regular first derivative.
 * At an event the isolated value is ordinary and derivative bounds concern regular sides;
 * no classical derivative at the jump is asserted. Positive-width boxes use the closure
 * from their interior. These bounds come from the source program, never an estimated value.
 * The concrete provider establishes revision, component and side: this data carrier and
 * its provenance string alone are not a transferable certificate.
 */
public final class LocusMetricBoxEvidence2D {

	public static final String VERSION = "metric-function-box-evidence/v1";
	private final double lower;
	private final double upper;
	private final double derivativeLower;
	private final double derivativeUpper;
	private final String evidence;

	private LocusMetricBoxEvidence2D(double lower, double upper, double derivativeLower,
			double derivativeUpper, String evidence) {
		if (!Double.isFinite(lower) || !Double.isFinite(upper) || lower > upper
				|| !Double.isFinite(derivativeLower) || !Double.isFinite(derivativeUpper)
				|| derivativeLower > derivativeUpper) {
			throw new IllegalArgumentException("Nonfinite or reversed metric proof enclosure");
		}
		this.lower = lower;
		this.upper = upper;
		this.derivativeLower = derivativeLower;
		this.derivativeUpper = derivativeUpper;
		this.evidence = Objects.requireNonNull(evidence);
	}

	public static LocusMetricBoxEvidence2D enclosed(double lower, double upper,
			double derivativeLower, double derivativeUpper, String evidence) {
		return new LocusMetricBoxEvidence2D(lower, upper, derivativeLower, derivativeUpper,
				evidence);
	}

	public double getLower() { return lower; }
	public double getUpper() { return upper; }
	public double getDerivativeLower() { return derivativeLower; }
	public double getDerivativeUpper() { return derivativeUpper; }
	public String getEvidence() { return evidence; }
}
