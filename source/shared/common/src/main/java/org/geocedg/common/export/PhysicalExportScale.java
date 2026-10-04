/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import org.geocedg.common.kernel.units.UnitState;

/**
 * PRE-G9B-R6-plus-C (C4): the one format-independent physical export scale of
 * the unit-system specification section 13 and its explicit limits.
 *
 * <pre>
 * pPhysical = fb(effC) * 100 * a / b     centimetres on the output per model unit
 * </pre>
 *
 * <p>The evaluation order is fixed, so that every route derives identical
 * binary64 values: {@code ((fb * 100) * a) / b}. {@code presentationUnit},
 * zoom, DPI, the viewport and the view {@code printingScale} never enter it.
 * With {@code UNSPECIFIED_MODEL_UNIT} there is no physical scale (NaN): the
 * host device-scale mode applies, labelled as non-physical.
 */
public final class PhysicalExportScale {

	/** Centimetres per inch. */
	public static final double CENTIMETRES_PER_INCH = 2.54;
	/** PDF points per inch. */
	public static final double POINTS_PER_INCH = 72;

	private PhysicalExportScale() {
		// computations only
	}

	/**
	 * @param state document unit state
	 * @param numerator drawing-scale numerator a (output side), positive
	 * @param denominator drawing-scale denominator b (real side), positive
	 * @return centimetres on the output per model unit, or NaN when the unit is
	 *         unspecified
	 */
	public static double centimetresPerUnit(UnitState state, int numerator,
			int denominator) {
		if (numerator <= 0 || denominator <= 0) {
			throw new IllegalArgumentException(
					"A drawing scale needs positive integer terms");
		}
		if (state == null || !state.isPhysical()) {
			return Double.NaN;
		}
		double metresPerUnit = state.effectiveConstructionMetresPerUnit();
		return metresPerUnit * 100 * numerator / denominator;
	}

	/**
	 * Device extent of a physical output (raster pixels, EMF device units). A
	 * non-finite extent, one beyond the integer range, or one below one device
	 * unit fails explicitly; nothing is clamped.
	 *
	 * @param exact exact extent in device units
	 * @param quantity name used in the failure message
	 * @return the nearest integer extent, at least one
	 */
	public static int deviceExtent(double exact, String quantity) {
		if (!Double.isFinite(exact) || exact > Integer.MAX_VALUE) {
			throw new PhysicalExportLimitException(quantity + " of " + exact
					+ " device units cannot be represented by this format");
		}
		if (exact < 1) {
			throw new PhysicalExportLimitException(quantity + " of " + exact
					+ " device units is below one device unit at this scale");
		}
		return (int) Math.round(exact);
	}

	/**
	 * EMF {@code rclFrame} value: 32-bit integer hundredths of a millimetre,
	 * rounded to the nearest representable value (at most 0.005 mm away).
	 *
	 * @param centimetres exact physical extent in centimetres
	 * @param quantity name used in the failure message
	 * @return hundredths of a millimetre
	 */
	public static int emfFrameHundredthsOfMillimetre(double centimetres,
			String quantity) {
		double hundredths = centimetres * 1000;
		if (!Double.isFinite(hundredths) || !(hundredths > 0)
				|| Math.round(hundredths) > Integer.MAX_VALUE) {
			throw new PhysicalExportLimitException(quantity + " of " + centimetres
					+ " cm cannot be written as an EMF frame (0.01 mm, 32-bit)");
		}
		long rounded = Math.round(hundredths);
		if (rounded < 1) {
			throw new PhysicalExportLimitException(quantity + " of " + centimetres
					+ " cm is below the EMF frame resolution of 0.01 mm");
		}
		return (int) rounded;
	}

	/**
	 * Limits of the vendored PDF writer: its canvas is an integer number of
	 * points, so a page must be finite, positive and at most
	 * {@code Integer.MAX_VALUE} points.
	 *
	 * @param points exact page extent in points
	 * @param quantity name used in the failure message
	 * @return the same extent
	 */
	public static double requirePdfPageExtent(double points, String quantity) {
		if (!Double.isFinite(points) || !(points > 0)
				|| Math.ceil(points) > Integer.MAX_VALUE) {
			throw new PhysicalExportLimitException(quantity + " of " + points
					+ " pt cannot be written by the PDF writer");
		}
		return points;
	}
}
