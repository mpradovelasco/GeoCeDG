/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

/** Normalized first-order contact evidence, distinct from multiplicity. */
public final class TargetContactEvidence2D {
	private final boolean established;
	private final double normalizedIndicator;
	private final String indicatorId;
	private final String diagnostic;

	private TargetContactEvidence2D(boolean established,
			double normalizedIndicator, String indicatorId, String diagnostic) {
		if (established && !Double.isFinite(normalizedIndicator)) {
			throw new IllegalArgumentException(
					"Established contact evidence must be finite");
		}
		this.established = established;
		this.normalizedIndicator = normalizedIndicator;
		this.indicatorId = requireText(indicatorId, "Contact indicator");
		this.diagnostic = requireText(diagnostic, "Contact diagnostic");
	}

	/** @return established normalized contact evidence */
	public static TargetContactEvidence2D established(double indicator,
			String indicatorId, String diagnostic) {
		return new TargetContactEvidence2D(true, indicator, indicatorId,
				diagnostic);
	}

	/** @return explicit unsupported/unknown contact evidence */
	public static TargetContactEvidence2D notEstablished(String diagnostic) {
		return new TargetContactEvidence2D(false, 0,
				"normalized-contact-not-established", diagnostic);
	}

	public boolean isEstablished() {
		return established;
	}

	/**
	 * Returns the normalized contact indicator.
	 *
	 * @return established normalized indicator
	 * @throws IllegalStateException when contact evidence is not established
	 */
	public double getNormalizedIndicator() {
		if (!established) {
			throw new IllegalStateException("Contact indicator is not established");
		}
		return normalizedIndicator;
	}

	public String getIndicatorId() {
		return indicatorId;
	}

	public String getDiagnostic() {
		return diagnostic;
	}

	private static String requireText(String value, String name) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(name + " is required");
		}
		return value;
	}
}
