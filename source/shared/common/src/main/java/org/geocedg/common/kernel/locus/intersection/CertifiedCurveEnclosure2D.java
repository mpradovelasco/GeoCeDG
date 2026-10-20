/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.Optional;

import org.geocedg.common.kernel.locus.LocusDefinition2D;

/**
 * Read-only public facade over the certified interval curve models (ADR 0028;
 * {@code locus-v2-certified-construction-model.md}) for the certified metric
 * enclosure of PRE-G9B-CU1 ({@code locus-v2-metrics.md} section 25.1 class 2).
 *
 * <p>It exposes the outward enclosures of the semantic curve and of its first
 * derivative over parameter boxes without changing the models, their admitted
 * operations or their contract. A refused box throws
 * {@link ArithmeticException}; it is unresolved and never certified.</p>
 */
public final class CertifiedCurveEnclosure2D {
	private final CertifiedIntervalCurveModel2D model;

	private CertifiedCurveEnclosure2D(CertifiedIntervalCurveModel2D model) {
		this.model = model;
	}

	/**
	 * @return the certified model of one branch, or empty when the definition
	 *         exposes none (outside the structural spline or certified
	 *         construction classes)
	 */
	public static Optional<CertifiedCurveEnclosure2D> capture(
			LocusDefinition2D definition, String branchKey) {
		try {
			CertifiedIntervalCurveModel2D model =
					CertifiedIntervalCurveModel2D.capture(definition, branchKey);
			return model == null ? Optional.empty()
					: Optional.of(new CertifiedCurveEnclosure2D(model));
		} catch (RuntimeException exception) {
			return Optional.empty();
		}
	}

	/**
	 * @return outward enclosure {@code {xLow, xHigh, yLow, yHigh}} of the point at
	 *         one parameter
	 */
	public double[] valueBounds(double parameter) {
		return flatten(model.evaluate(SplineOutwardInterval2D.point(parameter),
				false));
	}

	/**
	 * @return outward enclosure {@code {x'Low, x'High, y'Low, y'High}} of the
	 *         first derivative over {@code [lower, upper]}
	 */
	public double[] derivativeBounds(double lower, double upper) {
		return flatten(model.evaluate(new SplineOutwardInterval2D(lower, upper),
				true));
	}

	/** @return whether the represented curve is C1 throughout the box */
	public boolean isSmooth(double lower, double upper) {
		try {
			return model.isSmooth(new SplineOutwardInterval2D(lower, upper));
		} catch (ArithmeticException exception) {
			return false;
		}
	}

	/** @return computational partition of the domain; never identity */
	public double[] getKnots() {
		return model.getKnots();
	}

	private static double[] flatten(SplineOutwardInterval2D[] enclosure) {
		if (enclosure == null || enclosure.length != 2) {
			throw new ArithmeticException("Incomplete interval enclosure");
		}
		return new double[] {enclosure[0].lower, enclosure[0].upper,
				enclosure[1].lower, enclosure[1].upper};
	}
}
