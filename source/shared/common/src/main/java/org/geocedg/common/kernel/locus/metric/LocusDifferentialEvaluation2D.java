/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

/** Immutable differential evaluation carrying a finite derivative or failure. */
public final class LocusDifferentialEvaluation2D {
	private final boolean valid;
	private final double derivativeX;
	private final double derivativeY;
	private final String diagnostic;

	private LocusDifferentialEvaluation2D(boolean valid, double derivativeX,
			double derivativeY, String diagnostic) {
		this.valid = valid;
		this.derivativeX = derivativeX;
		this.derivativeY = derivativeY;
		this.diagnostic = diagnostic;
	}

	/**
	 * Creates a finite valid differential evaluation.
	 *
	 * @return valid differential evidence
	 */
	public static LocusDifferentialEvaluation2D valid(double derivativeX,
			double derivativeY) {
		if (!Double.isFinite(derivativeX) || !Double.isFinite(derivativeY)) {
			throw new IllegalArgumentException("Derivative must be finite");
		}
		return new LocusDifferentialEvaluation2D(true, derivativeX,
				derivativeY, "");
	}

	/**
	 * Creates a typed invalid differential evaluation.
	 *
	 * @return invalid differential evidence
	 */
	public static LocusDifferentialEvaluation2D invalid(String diagnostic) {
		if (diagnostic == null || diagnostic.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Invalid differential evaluation needs a diagnostic");
		}
		return new LocusDifferentialEvaluation2D(false, 0, 0, diagnostic);
	}

	/** @return whether a finite derivative is available */
	public boolean isValid() {
		return valid;
	}

	/** @return derivative x coordinate */
	public double getDerivativeX() {
		if (!valid) {
			throw new IllegalStateException("Invalid differential has no x value");
		}
		return derivativeX;
	}

	/** @return derivative y coordinate */
	public double getDerivativeY() {
		if (!valid) {
			throw new IllegalStateException("Invalid differential has no y value");
		}
		return derivativeY;
	}

	/** @return Euclidean derivative norm */
	public double getSpeed() {
		return Math.hypot(getDerivativeX(), getDerivativeY());
	}

	/** @return failure diagnostic, empty for a valid evaluation */
	public String getDiagnostic() {
		return diagnostic;
	}
}
