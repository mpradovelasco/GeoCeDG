/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

/**
 * Image of a certified root of a component, computed with the limit form of the certificate
 * (PRE-G9B-CU2, certification design section 3.4; consumption design section 5.2): an outward
 * enclosure of the traced point at a certified transition whose root belongs to its component,
 * or at a certified singular parameter, where the evaluator may not define the point. It is a
 * representation input for a drawing or export vertex, never an evaluation: it neither
 * publishes a point as valid nor replaces an undefined output by a limit.
 */
public final class LocusBoundaryImage2D {
	/** Version of the limit-form method. */
	public static final String METHOD = "locus-limit-form/v1";
	private final double xLower;
	private final double xUpper;
	private final double yLower;
	private final double yUpper;
	private final String diagnostic;

	private LocusBoundaryImage2D(double xLower, double xUpper, double yLower, double yUpper,
			String diagnostic) {
		this.xLower = xLower;
		this.xUpper = xUpper;
		this.yLower = yLower;
		this.yUpper = yUpper;
		this.diagnostic = diagnostic;
	}

	/**
	 * @param enclosure {@code {xLower, xUpper, yLower, yUpper}}, finite and ordered
	 * @return enclosed image
	 */
	public static LocusBoundaryImage2D enclosed(double[] enclosure) {
		Objects.requireNonNull(enclosure);
		if (enclosure.length != 4) {
			throw new IllegalArgumentException("An image enclosure has four bounds");
		}
		for (double bound : enclosure) {
			if (!Double.isFinite(bound)) {
				throw new IllegalArgumentException("An image enclosure is finite");
			}
		}
		if (!(enclosure[0] <= enclosure[1]) || !(enclosure[2] <= enclosure[3])) {
			throw new IllegalArgumentException("An image enclosure is ordered");
		}
		return new LocusBoundaryImage2D(enclosure[0], enclosure[1], enclosure[2], enclosure[3],
				"");
	}

	/**
	 * @param diagnostic typed reason why the limit form does not enclose the image
	 * @return unavailable image
	 */
	public static LocusBoundaryImage2D notAvailable(String diagnostic) {
		return new LocusBoundaryImage2D(Double.NaN, Double.NaN, Double.NaN, Double.NaN,
				Objects.requireNonNull(diagnostic));
	}

	/** @return whether the limit form encloses the image */
	public boolean isEnclosed() {
		return !Double.isNaN(xLower);
	}

	/** @return midpoint of the enclosure, the vertex a representation uses */
	public LocusPoint2D getPoint() {
		if (!isEnclosed()) {
			throw new IllegalStateException("The limit form does not enclose the image");
		}
		return new LocusPoint2D(xLower + (xUpper - xLower) / 2,
				yLower + (yUpper - yLower) / 2);
	}

	/** @return half-diagonal of the enclosure, a bound of the distance to the exact image */
	public double getRadius() {
		return isEnclosed() ? Math.nextUp(Math.hypot(xUpper - xLower, yUpper - yLower) / 2)
				: Double.NaN;
	}

	/** @return lower bound of the enclosure of the x coordinate */
	public double getXLower() {
		return xLower;
	}

	/** @return upper bound of the enclosure of the x coordinate */
	public double getXUpper() {
		return xUpper;
	}

	/** @return lower bound of the enclosure of the y coordinate */
	public double getYLower() {
		return yLower;
	}

	/** @return upper bound of the enclosure of the y coordinate */
	public double getYUpper() {
		return yUpper;
	}

	/** @return typed reason of an unavailable image, empty for an enclosed one */
	public String getDiagnostic() {
		return diagnostic;
	}

	@Override
	public String toString() {
		return isEnclosed() ? METHOD + ":x[" + Double.toHexString(xLower) + ","
				+ Double.toHexString(xUpper) + "]:y[" + Double.toHexString(yLower) + ","
				+ Double.toHexString(yUpper) + "]" : "not-available:" + diagnostic;
	}
}
