/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.intersection;

import org.geogebra.common.kernel.Kernel;

/**
 * Angle-independent eligibility of the native homogeneous rotation of two fixed points.
 * This is a value/error proof for every finite angle, never a continuity certificate of
 * native floating values and never an enclosure of an estimated metric angle.
 */
public final class NativeVariableRotationGuard2D {

	private static final double UNIT_ROUNDOFF = 0x1.0p-53;
	/** A representable upper bound of the half-minimum-subnormal rounding allowance. */
	private static final double UNDERFLOW_ALLOWANCE = Double.MIN_VALUE;
	private static final double COSINE_ZERO_THRESHOLD = 1E-16;
	/** One ulp at 1 bounds the absolute error of either Math trigonometric function. */
	private static final double TRIGONOMETRIC_ERROR = Math.ulp(1.0);
	/** Retain one extra ulp rather than assuming a stronger range guarantee for Math. */
	private static final double TRIGONOMETRIC_MAGNITUDE = Math.nextUp(1.0);

	private NativeVariableRotationGuard2D() { }

	/** Immutable bounds; all errors are absolute world-coordinate bounds. */
	public static final class Proof {
		private final SplineOutwardInterval2D nativeX;
		private final SplineOutwardInterval2D nativeY;
		private final double directionLower;
		private final double directionUpper;
		private final double errorX;
		private final double errorY;
		private final double roundoffX;
		private final double roundoffY;
		private final double directionErrorUpper;

		private Proof(SplineOutwardInterval2D nativeX, SplineOutwardInterval2D nativeY,
				double directionLower, double directionUpper, double errorX, double errorY,
				double roundoffX, double roundoffY, double directionErrorUpper) {
			this.nativeX = nativeX;
			this.nativeY = nativeY;
			this.directionLower = directionLower;
			this.directionUpper = directionUpper;
			this.errorX = errorX;
			this.errorY = errorY;
			this.roundoffX = roundoffX;
			this.roundoffY = roundoffY;
			this.directionErrorUpper = directionErrorUpper;
		}

		public double getNativeXLower() { return nativeX.lower; }
		public double getNativeXUpper() { return nativeX.upper; }
		public double getNativeYLower() { return nativeY.lower; }
		public double getNativeYUpper() { return nativeY.upper; }
		/** Norm of the native differences used as the ray's direction coefficients. */
		public double getDirectionLower() { return directionLower; }
		public double getDirectionUpper() { return directionUpper; }
		/** Error against exact geometric rotation at the SAME finite native angle. */
		public double getErrorX() { return errorX; }
		public double getErrorY() { return errorY; }
		/** Arithmetic error with the represented native cosine/sine held fixed. */
		public double getRoundoffX() { return roundoffX; }
		public double getRoundoffY() { return roundoffY; }
		/** Includes trigonometric error and the final native ray subtractions. */
		public double getDirectionErrorUpper() { return directionErrorUpper; }
	}

	/**
	 * @param x fixed raw homogeneous source coordinate
	 * @param y fixed raw homogeneous source coordinate
	 * @param z fixed raw homogeneous source coordinate
	 * @param cx fixed inhomogeneous centre coordinate
	 * @param cy fixed inhomogeneous centre coordinate
	 * @return proof for every finite angle, or null if any premise is not established
	 */
	public static Proof enclose(double x, double y, double z, double cx, double cy) {
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
				|| !Double.isFinite(cx) || !Double.isFinite(cy)
				|| !(Math.abs(z) > Kernel.STANDARD_PRECISION)) { return null; }
		try {
			Enclosure rawX = Enclosure.constant(x);
			Enclosure rawY = Enclosure.constant(y);
			Enclosure rawZ = Enclosure.constant(z);
			Enclosure centerX = Enclosure.constant(cx);
			Enclosure centerY = Enclosure.constant(cy);
			Enclosure cosine = Enclosure.trigonometric();
			Enclosure sine = Enclosure.trigonometric();
			// The literal GeoPoint.rotate order; no inhomogeneous substitution may hide overflow.
			Enclosure qx = rawZ.multiply(centerX);
			Enclosure qy = rawZ.multiply(centerY);
			Enclosure u = rawX.subtract(qx);
			Enclosure rotatedX = u.multiply(cosine)
					.add(qy.subtract(rawY).multiply(sine)).add(qx).divide(z);
			Enclosure rotatedY = u.multiply(sine)
					.add(rawY.subtract(qy).multiply(cosine)).add(qy).divide(z);

			// Exact-real displacement is raw/z - C, not an independently cached inhomogeneous field.
			SplineOutwardInterval2D dx = point(x).divide(point(z)).subtract(point(cx));
			SplineOutwardInterval2D dy = point(y).divide(point(z)).subtract(point(cy));
			double distanceLower = lowerNorm(minimumMagnitude(dx), minimumMagnitude(dy));
			double distanceUpper = upperNorm(magnitude(dx), magnitude(dy));
			double cosineError = add(COSINE_ZERO_THRESHOLD, TRIGONOMETRIC_ERROR);
			double errorX = add(rotatedX.error, add(multiply(magnitude(dx), cosineError),
					multiply(magnitude(dy), TRIGONOMETRIC_ERROR)));
			double errorY = add(rotatedY.error, add(multiply(magnitude(dx), TRIGONOMETRIC_ERROR),
					multiply(magnitude(dy), cosineError)));
			SplineOutwardInterval2D nativeX = rotatedX.rounded.intersection(
					globalCoordinate(cx, distanceUpper, errorX));
			SplineOutwardInterval2D nativeY = rotatedY.rounded.intersection(
					globalCoordinate(cy, distanceUpper, errorY));
			if (nativeX == null || nativeY == null) { return null; }
			// GeoVec3D.lineThroughPoints uses inhomogeneous native differences for finite points.
			double directionErrorX = add(errorX, rounding(nativeX.subtract(point(cx))));
			double directionErrorY = add(errorY, rounding(nativeY.subtract(point(cy))));
			double directionError = upperNorm(directionErrorX, directionErrorY);
			double directionLower = Math.max(0,
					point(distanceLower).subtract(point(directionError)).lower);
			double directionUpper = add(distanceUpper, directionError);
			double threshold = multiply(Math.nextUp(Math.sqrt(2)), Kernel.STANDARD_PRECISION);
			if (!(directionLower > threshold)) { return null; }
			return new Proof(nativeX, nativeY, directionLower, directionUpper, errorX, errorY,
					rotatedX.error, rotatedY.error, directionError);
		} catch (ArithmeticException failure) {
			return null;
		}
	}

	private static SplineOutwardInterval2D globalCoordinate(double center, double distance,
			double error) {
		double reach = add(distance, error);
		return point(center).add(new SplineOutwardInterval2D(-reach, reach));
	}

	/** Exact formula with represented trig inputs, rounded formula, and their error. */
	private static final class Enclosure {
		private final SplineOutwardInterval2D reference;
		private final SplineOutwardInterval2D rounded;
		private final double error;

		private Enclosure(SplineOutwardInterval2D reference,
				SplineOutwardInterval2D rounded, double error) {
			this.reference = reference;
			this.rounded = rounded;
			this.error = error;
		}

		static Enclosure constant(double value) {
			return new Enclosure(point(value), point(value), 0);
		}

		static Enclosure trigonometric() {
			SplineOutwardInterval2D range = new SplineOutwardInterval2D(
					-TRIGONOMETRIC_MAGNITUDE, TRIGONOMETRIC_MAGNITUDE);
			return new Enclosure(range, range, 0);
		}

		Enclosure add(Enclosure other) {
			SplineOutwardInterval2D result = rounded.add(other.rounded);
			return new Enclosure(reference.add(other.reference), result,
					NativeVariableRotationGuard2D.add(
							NativeVariableRotationGuard2D.add(error, other.error), rounding(result)));
		}

		Enclosure subtract(Enclosure other) {
			SplineOutwardInterval2D result = rounded.subtract(other.rounded);
			return new Enclosure(reference.subtract(other.reference), result,
					NativeVariableRotationGuard2D.add(
							NativeVariableRotationGuard2D.add(error, other.error), rounding(result)));
		}

		Enclosure multiply(Enclosure other) {
			SplineOutwardInterval2D result = rounded.multiply(other.rounded);
			double propagated = NativeVariableRotationGuard2D.add(
					NativeVariableRotationGuard2D.multiply(magnitude(reference), other.error),
					NativeVariableRotationGuard2D.multiply(magnitude(other.reference), error));
			propagated = NativeVariableRotationGuard2D.add(propagated,
					NativeVariableRotationGuard2D.multiply(error, other.error));
			return new Enclosure(reference.multiply(other.reference), result,
					NativeVariableRotationGuard2D.add(propagated, rounding(result)));
		}

		Enclosure divide(double denominator) {
			if (denominator == 1) { return this; }
			SplineOutwardInterval2D result = rounded.divide(point(denominator));
			double propagated = point(error).divide(point(Math.abs(denominator))).upper;
			return new Enclosure(reference.divide(point(denominator)), result,
					NativeVariableRotationGuard2D.add(propagated, rounding(result)));
		}
	}

	private static SplineOutwardInterval2D point(double value) {
		return SplineOutwardInterval2D.point(value);
	}

	/** The exact pre-round result is also contained in the outward result interval. */
	private static double rounding(SplineOutwardInterval2D result) {
		return add(multiply(UNIT_ROUNDOFF, magnitude(result)), UNDERFLOW_ALLOWANCE);
	}

	private static double add(double a, double b) { return point(a).add(point(b)).upper; }
	private static double multiply(double a, double b) { return point(a).multiply(point(b)).upper; }
	private static double magnitude(SplineOutwardInterval2D value) {
		return Math.max(Math.abs(value.lower), Math.abs(value.upper));
	}
	private static double minimumMagnitude(SplineOutwardInterval2D value) {
		return value.containsZero() ? 0 : Math.min(Math.abs(value.lower), Math.abs(value.upper));
	}
	private static double lowerNorm(double x, double y) {
		double result = Math.max(0, Math.nextDown(Math.nextDown(Math.hypot(x, y))));
		if (!Double.isFinite(result)) { throw new ArithmeticException("Nonfinite lower norm"); }
		return result;
	}
	private static double upperNorm(double x, double y) {
		double result = Math.nextUp(Math.nextUp(Math.hypot(x, y)));
		if (!Double.isFinite(result)) { throw new ArithmeticException("Nonfinite upper norm"); }
		return result;
	}
}
