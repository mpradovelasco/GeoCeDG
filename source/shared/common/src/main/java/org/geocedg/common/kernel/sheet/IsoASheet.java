/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.sheet;

/**
 * Pure numeric contract of the native ISO A sheet (PRE-G9B-R6-plus-E3): the ISO 216
 * nominal A-series table, the fixed inner-frame margins, the canonical conversion from
 * paper millimetres to model units, frame validity, the reliability classification and
 * the derived physical-size and scale-label coherence.
 *
 * <p>
 * Every method is a pure function of its arguments. Nothing here reads the document
 * unit state, the session drawing scale, a view or the screen; callers pass captured
 * construction inputs, and only presentation code passes the current unit factor and
 * session scale to {@link #coherence}. The conversion is
 * {@code conv(L) = ((L * b) / a) * u} in binary64, evaluated in exactly this order
 * (design section 7).
 * </p>
 */
public final class IsoASheet {

	/** Smallest ISO A index (A0). */
	public static final int MIN_INDEX = 0;
	/** Largest ISO A index (A10). */
	public static final int MAX_INDEX = 10;
	/** Largest index whose inner frame is enabled by default (A4). */
	public static final int LAST_DEFAULT_FRAME_INDEX = 4;
	/** Left inner-frame margin, paper millimetres. */
	public static final int MARGIN_LEFT_MM = 20;
	/** Right inner-frame margin, paper millimetres. */
	public static final int MARGIN_RIGHT_MM = 10;
	/** Top inner-frame margin, paper millimetres. */
	public static final int MARGIN_TOP_MM = 10;
	/** Bottom inner-frame margin, paper millimetres. */
	public static final int MARGIN_BOTTOM_MM = 10;
	/** Largest admissible scale term, as {@code DrawingScale.MAXIMUM_TERM}. */
	public static final double MAX_SCALE_TERM = 1E9;
	/** Physical-size coherence criterion, millimetres. */
	public static final double COHERENCE_TOLERANCE_MM = 1E-6;
	/** Binary64 unit roundoff. */
	static final double EPSILON = 0x1p-53;
	/** Relative bound of the effective-size arithmetic (design section 9). */
	static final double EFFECTIVE_SIZE_BOUND = 0x1p-50;
	private static final char EM_DASH = (char) 0x2014;

	/** ISO 216 nominal A-series sizes, short x long, millimetres, A0 to A10. */
	private static final int[][] SHORT_LONG_MM = {{841, 1189}, {594, 841}, {420, 594},
			{297, 420}, {210, 297}, {148, 210}, {105, 148}, {74, 105}, {52, 74}, {37, 52},
			{26, 37}};

	/** Accuracy classification of a representable sheet (design section 7.6). */
	public enum Reliability {
		/** the conventional relative bounds of design sections 7.3 and 7.4 hold */
		GUARANTEED,
		/** the sheet exists exactly as computed, but no accuracy bound is claimed */
		UNGUARANTEED
	}

	/** Physical-size coherence of a sheet at the current unit and session scale. */
	public enum PhysicalCoherence {
		/** both effective page sides are the nominal ISO sides within the criterion */
		COHERENT,
		/** a reliable effective page side differs from its nominal side */
		INCOHERENT,
		/** no reliable physical conclusion is possible */
		NOT_DETERMINABLE
	}

	/** Coherence of the captured sheet scale with the current session scale. */
	public enum ScaleCoherence {
		/** equal gcd-normal pairs */
		MATCH,
		/** different gcd-normal pairs */
		DIFFERENT,
		/** the comparison has no engineering meaning (no physical unit, invalid pair) */
		NOT_APPLICABLE
	}

	/** Result of {@link #coherence}: both states plus the sizes they compare. */
	public static final class Coherence {
		private final PhysicalCoherence physical;
		private final ScaleCoherence scale;
		private final double nominalWidthMm;
		private final double nominalHeightMm;
		private final double effectiveWidthMm;
		private final double effectiveHeightMm;

		Coherence(PhysicalCoherence physical, ScaleCoherence scale, double nominalWidthMm,
				double nominalHeightMm, double effectiveWidthMm, double effectiveHeightMm) {
			this.physical = physical;
			this.scale = scale;
			this.nominalWidthMm = nominalWidthMm;
			this.nominalHeightMm = nominalHeightMm;
			this.effectiveWidthMm = effectiveWidthMm;
			this.effectiveHeightMm = effectiveHeightMm;
		}

		/** @return physical-size coherence */
		public PhysicalCoherence getPhysical() {
			return physical;
		}

		/** @return scale-label coherence */
		public ScaleCoherence getScale() {
			return scale;
		}

		/** @return nominal paper width, mm, or NaN */
		public double getNominalWidthMm() {
			return nominalWidthMm;
		}

		/** @return nominal paper height, mm, or NaN */
		public double getNominalHeightMm() {
			return nominalHeightMm;
		}

		/** @return effective exported page width, mm, or NaN when not determinable */
		public double getEffectiveWidthMm() {
			return effectiveWidthMm;
		}

		/** @return effective exported page height, mm, or NaN when not determinable */
		public double getEffectiveHeightMm() {
			return effectiveHeightMm;
		}
	}

	private IsoASheet() {
	}

	/**
	 * @param index candidate ISO A index
	 * @return whether it is exactly an integer in 0 to 10 (no rounding)
	 */
	public static boolean isIndex(double index) {
		return index >= MIN_INDEX && index <= MAX_INDEX && index == Math.rint(index);
	}

	/**
	 * @param term candidate scale numerator or denominator
	 * @return whether it is exactly an integer in 1 to 10^9
	 */
	public static boolean isScaleTerm(double term) {
		return term >= 1 && term <= MAX_SCALE_TERM && term == Math.rint(term);
	}

	/**
	 * @param unitFactor candidate model units per millimetre
	 * @return whether it is finite and positive (normal or subnormal)
	 */
	public static boolean isUnitFactor(double unitFactor) {
		return unitFactor > 0 && unitFactor < Double.POSITIVE_INFINITY;
	}

	/**
	 * Creation-time capture {@code u = 10^-3 / fb(effC)} (unit-system section 18.4). The
	 * caller refuses creation when the result is not {@link #isUnitFactor a unit factor}.
	 *
	 * @param metresPerUnit effective construction metre factor (binary64)
	 * @return model units per millimetre
	 */
	public static double captureUnitFactor(double metresPerUnit) {
		return 1E-3 / metresPerUnit;
	}

	/**
	 * @param index ISO A index
	 * @return nominal short side, mm
	 */
	public static int shortSideMm(int index) {
		return SHORT_LONG_MM[index][0];
	}

	/**
	 * @param index ISO A index
	 * @return nominal long side, mm
	 */
	public static int longSideMm(int index) {
		return SHORT_LONG_MM[index][1];
	}

	/**
	 * @param index ISO A index
	 * @param landscape whether the width is the long side
	 * @return nominal paper width, mm
	 */
	public static int paperWidthMm(int index, boolean landscape) {
		return landscape ? longSideMm(index) : shortSideMm(index);
	}

	/**
	 * @param index ISO A index
	 * @param landscape whether the width is the long side
	 * @return nominal paper height, mm
	 */
	public static int paperHeightMm(int index, boolean landscape) {
		return landscape ? shortSideMm(index) : longSideMm(index);
	}

	/**
	 * @param index ISO A index
	 * @param landscape orientation
	 * @return nominal inner-frame width {@code W_p - m_L - m_R}, mm (may be non-positive)
	 */
	public static int frameWidthMm(int index, boolean landscape) {
		return paperWidthMm(index, landscape) - MARGIN_LEFT_MM - MARGIN_RIGHT_MM;
	}

	/**
	 * @param index ISO A index
	 * @param landscape orientation
	 * @return nominal inner-frame height {@code H_p - m_T - m_B}, mm (may be non-positive)
	 */
	public static int frameHeightMm(int index, boolean landscape) {
		return paperHeightMm(index, landscape) - MARGIN_TOP_MM - MARGIN_BOTTOM_MM;
	}

	/**
	 * Exact integer decision, before any conversion; with the approved margins only A10
	 * portrait is impossible.
	 *
	 * @param index ISO A index
	 * @param landscape orientation
	 * @return whether the approved margins leave a positive frame
	 */
	public static boolean isFramePossible(int index, boolean landscape) {
		return frameWidthMm(index, landscape) > 0 && frameHeightMm(index, landscape) > 0;
	}

	/**
	 * @param index ISO A index
	 * @return initial state of the inner-frame option: on for A0 to A4, off for A5 to A10
	 */
	public static boolean isFrameDefault(int index) {
		return index <= LAST_DEFAULT_FRAME_INDEX;
	}

	/**
	 * The canonical conversion; the evaluation order is part of the reproducibility
	 * contract and must not be rearranged.
	 *
	 * @param lengthMm nominal millimetre integer (1 to 1189)
	 * @param numerator scale numerator a
	 * @param denominator scale denominator b
	 * @param unitFactor captured model units per millimetre u
	 * @return {@code ((L * b) / a) * u}
	 */
	public static double conv(double lengthMm, double numerator, double denominator,
			double unitFactor) {
		return ((lengthMm * denominator) / numerator) * unitFactor;
	}

	/**
	 * @param a positive integer
	 * @param b positive integer
	 * @return greatest common divisor
	 */
	public static long gcd(long a, long b) {
		long x = a;
		long y = b;
		while (y != 0) {
			long r = x % y;
			x = y;
			y = r;
		}
		return x;
	}

	/**
	 * Construction label: the nominal size and the captured scale in gcd-normal form,
	 * independent of the language, the unit state and the session scale.
	 *
	 * @param index ISO A index
	 * @param numerator scale numerator
	 * @param denominator scale denominator
	 * @return for example "A3", a space, an em dash (U+2014), a space and "1:50"
	 */
	public static String label(int index, long numerator, long denominator) {
		long g = gcd(numerator, denominator);
		return "A" + index + " " + EM_DASH + " " + numerator / g + ":" + denominator / g;
	}

	/**
	 * @param index ISO A index
	 * @param landscape orientation
	 * @param frame whether the inner frame is enabled
	 * @return every nominal length the construction converts, mm
	 */
	static int[] convertedLengths(int index, boolean landscape, boolean frame) {
		int width = paperWidthMm(index, landscape);
		int height = paperHeightMm(index, landscape);
		if (frame && isFramePossible(index, landscape)) {
			return new int[] {width, height, width - MARGIN_RIGHT_MM, height - MARGIN_BOTTOM_MM,
					MARGIN_LEFT_MM, MARGIN_TOP_MM, frameWidthMm(index, landscape),
					frameHeightMm(index, landscape)};
		}
		return new int[] {width, height, width - MARGIN_RIGHT_MM, height - MARGIN_BOTTOM_MM};
	}

	/**
	 * Reliability of a representable sheet (design section 7.6): {@code GUARANTEED}
	 * exactly when {@code u} and every converted length are normal binary64 numbers.
	 *
	 * @param index valid ISO A index
	 * @param landscape orientation
	 * @param numerator valid scale numerator
	 * @param denominator valid scale denominator
	 * @param unitFactor valid unit factor
	 * @param frame whether the inner frame is enabled
	 * @return the classification
	 */
	public static Reliability reliability(int index, boolean landscape, double numerator,
			double denominator, double unitFactor, boolean frame) {
		if (!(unitFactor >= Double.MIN_NORMAL)) {
			return Reliability.UNGUARANTEED;
		}
		for (int length : convertedLengths(index, landscape, frame)) {
			double converted = conv(length, numerator, denominator, unitFactor);
			if (!(converted >= Double.MIN_NORMAL) || converted == Double.POSITIVE_INFINITY) {
				return Reliability.UNGUARANTEED;
			}
		}
		return Reliability.GUARANTEED;
	}

	/**
	 * Derived physical-size and scale-label coherence (design section 9). The effective
	 * page side is {@code E = conv(P) * fb * a_n / b_n * 1000} mm, computed from the
	 * captured inputs; its uncertainty adds the binary64 arithmetic bound and the
	 * realized-corner coordinate term. A missing, unreliable, non-finite or too
	 * uncertain result is {@code NOT_DETERMINABLE}, never {@code COHERENT}.
	 *
	 * @param paperDefined whether the paper boundary is currently defined
	 * @param index captured ISO A index
	 * @param landscape captured orientation
	 * @param numerator captured scale numerator
	 * @param denominator captured scale denominator
	 * @param unitFactor captured unit factor
	 * @param frame captured inner-frame state
	 * @param x reference point x
	 * @param y reference point y
	 * @param metresPerUnit current effective construction metre factor, NaN when the
	 *            unit is unspecified
	 * @param sessionNumerator current session scale numerator
	 * @param sessionDenominator current session scale denominator
	 * @return both coherence states and the compared sizes
	 */
	public static Coherence coherence(boolean paperDefined, double index, boolean landscape,
			double numerator, double denominator, double unitFactor, boolean frame, double x,
			double y, double metresPerUnit, long sessionNumerator, long sessionDenominator) {
		boolean inputs = isIndex(index) && isScaleTerm(numerator) && isScaleTerm(denominator)
				&& isUnitFactor(unitFactor);
		boolean physicalUnit = metresPerUnit > 0 && metresPerUnit < Double.POSITIVE_INFINITY;
		ScaleCoherence scale = ScaleCoherence.NOT_APPLICABLE;
		if (inputs && physicalUnit && sessionNumerator > 0 && sessionDenominator > 0) {
			scale = sameScale((long) numerator, (long) denominator, sessionNumerator,
					sessionDenominator) ? ScaleCoherence.MATCH : ScaleCoherence.DIFFERENT;
		}
		if (!inputs) {
			return new Coherence(PhysicalCoherence.NOT_DETERMINABLE, scale, Double.NaN,
					Double.NaN, Double.NaN, Double.NaN);
		}
		int n = (int) index;
		int width = paperWidthMm(n, landscape);
		int height = paperHeightMm(n, landscape);
		if (!paperDefined || !physicalUnit || sessionNumerator <= 0 || sessionDenominator <= 0
				|| reliability(n, landscape, numerator, denominator, unitFactor, frame)
						== Reliability.UNGUARANTEED) {
			return new Coherence(PhysicalCoherence.NOT_DETERMINABLE, scale, width, height,
					Double.NaN, Double.NaN);
		}
		double w = conv(width, numerator, denominator, unitFactor);
		double h = conv(height, numerator, denominator, unitFactor);
		double effectiveWidth = effectiveMm(w, metresPerUnit, sessionNumerator,
				sessionDenominator);
		double effectiveHeight = effectiveMm(h, metresPerUnit, sessionNumerator,
				sessionDenominator);
		double widthUncertainty = uncertaintyMm(effectiveWidth, x, x + w, metresPerUnit,
				sessionNumerator, sessionDenominator);
		double heightUncertainty = uncertaintyMm(effectiveHeight, y, y - h, metresPerUnit,
				sessionNumerator, sessionDenominator);
		if (!(effectiveWidth >= Double.MIN_NORMAL) || !(effectiveHeight >= Double.MIN_NORMAL)
				|| !Double.isFinite(effectiveWidth) || !Double.isFinite(effectiveHeight)
				|| !Double.isFinite(widthUncertainty) || !Double.isFinite(heightUncertainty)
				|| widthUncertainty > COHERENCE_TOLERANCE_MM
				|| heightUncertainty > COHERENCE_TOLERANCE_MM) {
			return new Coherence(PhysicalCoherence.NOT_DETERMINABLE, scale, width, height,
					Double.NaN, Double.NaN);
		}
		boolean match = Math.abs(effectiveWidth - width) <= COHERENCE_TOLERANCE_MM
				&& Math.abs(effectiveHeight - height) <= COHERENCE_TOLERANCE_MM;
		return new Coherence(match ? PhysicalCoherence.COHERENT : PhysicalCoherence.INCOHERENT,
				scale, width, height, effectiveWidth, effectiveHeight);
	}

	private static double effectiveMm(double modelLength, double metresPerUnit,
			long sessionNumerator, long sessionDenominator) {
		return (((modelLength * metresPerUnit) * sessionNumerator) / sessionDenominator) * 1E3;
	}

	private static double uncertaintyMm(double effectiveMm, double corner1, double corner2,
			double metresPerUnit, long sessionNumerator, long sessionDenominator) {
		double coordinate = EPSILON * (Math.abs(corner1) + Math.abs(corner2));
		return effectiveMm * EFFECTIVE_SIZE_BOUND
				+ effectiveMm(coordinate, metresPerUnit, sessionNumerator, sessionDenominator);
	}

	/**
	 * @param a1 first numerator
	 * @param b1 first denominator
	 * @param a2 second numerator
	 * @param b2 second denominator
	 * @return whether both pairs have the same gcd-normal form
	 */
	public static boolean sameScale(long a1, long b1, long a2, long b2) {
		long g1 = gcd(a1, b1);
		long g2 = gcd(a2, b2);
		return a1 / g1 == a2 / g2 && b1 / g1 == b2 / g2;
	}
}
