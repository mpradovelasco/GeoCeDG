/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

import java.util.Locale;

import javax.annotation.CheckForNull;

/**
 * Explicit affine transformation of section 6: {@code x' = (a x + b y) + e},
 * {@code y' = (c x + d y) + f}, each coordinate computed in exactly that binary64 operation
 * order without fused multiply-add. It acts on control points only; knots and weights are
 * never transformed, so it is exact in exact arithmetic for polynomial and rational curves
 * alike, and its binary64 rounding is the declared transformation rounding. The default is
 * the identity: no implicit scaling, no unit conversion.
 */
public final class DxfAffineTransform {

	/** The identity: one DXF drawing unit becomes one model unit. */
	public static final DxfAffineTransform IDENTITY =
			new DxfAffineTransform(1, 0, 0, 1, 0, 0);

	private final double a;
	private final double b;
	private final double c;
	private final double d;
	private final double e;
	private final double f;

	private DxfAffineTransform(double a, double b, double c, double d, double e, double f) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
	}

	/**
	 * @param a x coefficient of x'
	 * @param b y coefficient of x'
	 * @param c x coefficient of y'
	 * @param d y coefficient of y'
	 * @param e translation of x'
	 * @param f translation of y'
	 * @return the transformation, or {@code null} for {@link DxfCause#SINGULAR_TRANSFORM}
	 *         (a nonfinite coefficient or {@code a d - b c = 0} in binary64)
	 */
	public static @CheckForNull DxfAffineTransform of(double a, double b, double c, double d,
			double e, double f) {
		for (double value : new double[] {a, b, c, d, e, f}) {
			if (!Double.isFinite(value)) {
				return null;
			}
		}
		double determinant = a * d - b * c;
		if (!(determinant != 0.0) || !Double.isFinite(determinant)) {
			return null;
		}
		return new DxfAffineTransform(a, b, c, d, e, f);
	}

	/**
	 * Uniform scale, the form of an explicitly chosen unit conversion.
	 *
	 * @param factor positive finite scale
	 * @return the transformation, or {@code null} when singular or nonfinite
	 */
	public static @CheckForNull DxfAffineTransform scale(double factor) {
		return of(factor, 0, 0, factor, 0, 0);
	}

	/**
	 * @param x control-point x
	 * @param y control-point y
	 * @return {@code (a x + b y) + e}
	 */
	public double applyX(double x, double y) {
		return (a * x + b * y) + e;
	}

	/**
	 * @param x control-point x
	 * @param y control-point y
	 * @return {@code (c x + d y) + f}
	 */
	public double applyY(double x, double y) {
		return (c * x + d * y) + f;
	}

	/**
	 * @return whether this is exactly the identity
	 */
	public boolean isIdentity() {
		return a == 1 && b == 0 && c == 0 && d == 1 && e == 0 && f == 0;
	}

	/**
	 * @return the six coefficients {@code a, b, c, d, e, f}
	 */
	public double[] coefficients() {
		return new double[] {a, b, c, d, e, f};
	}

	/**
	 * @return deterministic text with the exact binary64 values (shortest round-trip form)
	 */
	@Override
	public String toString() {
		return String.format(Locale.ROOT, "x' = (%s*x + %s*y) + %s; y' = (%s*x + %s*y) + %s",
				Double.toString(a), Double.toString(b), Double.toString(e), Double.toString(c),
				Double.toString(d), Double.toString(f));
	}
}
