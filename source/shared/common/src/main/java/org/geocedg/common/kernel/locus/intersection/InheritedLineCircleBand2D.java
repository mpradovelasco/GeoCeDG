/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.TreeSet;

/**
 * Rigorous band of an unlabelled second output of a line-circle intersection (finding
 * H-T13-B, author decision D-CU2-T13-R7; {@code locus-v2-certified-construction-model.md}
 * section 10.3). A box is outside the band only when, for every binary64 driver point of the
 * box, the first retry ({@code eps = 1e-15}) of the inherited computation of
 * {@code AlgoIntersectLineConic} is decisive and keeps that output defined:
 * <ol>
 * <li>its {@code u} is not below {@code eps}, so the degenerate branch is not taken;</li>
 * <li>its discriminant {@code dis} exceeds its tangency window {@code delta}, so the line
 * is not declared tangent;</li>
 * <li>the root it computes for the output passes the inherited incidence tests, so the
 * retries end there: the circle and the full line with {@code 1e-5}, and the parameter of
 * a ray or segment not more than {@code 1e-5} outside it;</li>
 * <li>the two roots it computes differ by more than {@code 1e-8} in some coordinate, so
 * {@code avoidDoubleTangentPoint} does not suppress the output.</li>
 * </ol>
 * Each inequality is decided with the enclosures of the box and a priori bounds of every
 * rounding of the inherited formulas (unit roundoff {@code u = 2^-53},
 * {@code gamma_n = n u / (1 - n u)}), of the driver point ({@code Math.cos} and
 * {@code Math.sin} within one ulp) and of the line coefficients. The exact discriminant of
 * the live coefficients is enclosed from the one of the box,
 * {@code D = (a^2 + b^2) R^2 - s^2}, never from {@code d^2 - u w}, whose terms cancel.
 * Elsewhere the box is in the band, whatever the reason; nothing in the band is certified.
 *
 * <p>It applies to a line through a captured constant point and the driver point of a
 * complete circle, and to a captured constant circle: the roots are those of its matrix,
 * the incidence test reads its midpoint, which the capture requires to be the one of the
 * matrix, and its captured half axis.</p>
 */
final class InheritedLineCircleBand2D {
	/** Unit roundoff of binary64. */
	private static final double UNIT = 0x1.0p-53;
	/** Relative slack for the binary64 evaluation of a nonnegative bound expression. */
	private static final double SLACK = 0x1.0p-40;
	/** First retry of {@code AlgoIntersectLineConic.intersect}. */
	private static final double FIRST_EPS = 1E-15;
	/** {@code Kernel.MIN_PRECISION}: incidence tolerance and cap of the tangency window. */
	private static final double MIN_PRECISION = 1E-5;
	/** {@code Kernel.STANDARD_PRECISION}: {@code DoubleUtil.isEqual} of two coordinates. */
	private static final double STANDARD_PRECISION = 1E-8;
	/** Bound of the driver rounding relative to the magnitudes of its formula. */
	private static final double DRIVER_BOUND = 0x1.0p-49;
	/**
	 * More normalization exponents than this on one box leave it in the band; a coefficient
	 * that vanishes in the box spans many.
	 */
	private static final int MAXIMUM_EXPONENTS = 64;

	private InheritedLineCircleBand2D() {
	}

	/** Enclosures of one box that the bound reads. */
	static final class Box {
		/** Bound of the rounding of each coordinate of the live driver point. */
		double driverError;
		/** Captured constant point of the line, inhomogeneous coordinates. */
		double pointX;
		double pointY;
		/** Exact driver point on the box. */
		SplineOutwardInterval2D driverX;
		SplineOutwardInterval2D driverY;
		/** Exact line through the constant point and the driver point, as joined. */
		SplineOutwardInterval2D a;
		SplineOutwardInterval2D b;
		SplineOutwardInterval2D c;
		/** {@code a^2 + b^2}, {@code s = a mx + b my + c} and {@code D} of the box. */
		SplineOutwardInterval2D norm;
		SplineOutwardInterval2D distance;
		SplineOutwardInterval2D discriminant;
		/** Captured circle matrix entries and the enclosure of {@code A4^2 + A5^2 - A2}. */
		double a2;
		double a4;
		double a5;
		SplineOutwardInterval2D radiusSquared;
		/** Captured half axis {@code h0} that the inherited incidence test reads. */
		double halfAxis;
		/** Start of a ray or segment, {@code null} for a line; whether it is the driver. */
		SplineOutwardInterval2D startX;
		SplineOutwardInterval2D startY;
		boolean startIsDriver;
		/**
		 * Lower bounds of the path parameter of the selected exact root, in units of the
		 * direction {@code (b, -a)}, and of its distance to the end of a segment (infinite
		 * for a ray); zero where unknown.
		 */
		double pathLow;
		double endLow;
		/**
		 * Whether the two enclosures of each value of the box intersect; disjoint enclosures
		 * mean that one of them is unsound, and the box stays in the band (PRE-G9B-CU2, review
		 * of R7, finding R7-D).
		 */
		boolean consistent = true;
	}

	/**
	 * Bound of the rounding of each coordinate of the driver point on a complete circle:
	 * {@code GeoConicND.pathChangedWithoutCheckEllipse} computes {@code px = h0 cos t} and
	 * {@code py = h1 sin t} (each within {@code (2^-52 + u) |h|}: one ulp of the cosine or
	 * sine and one rounding), then {@code coordsEVtoRW} {@code px e0x + py e1x + mx} with
	 * three roundings; the total is within {@code 2^-49 (|h0| |e0x| + |h1| |e1x| + |mx|)},
	 * and likewise for the second coordinate.
	 *
	 * @return the bound for the captured driver constants
	 */
	static double circleDriverError(double h0, double h1, double e0x, double e0y,
			double e1x, double e1y, double mx, double my) {
		double first = Math.abs(h0) * Math.abs(e0x) + Math.abs(h1) * Math.abs(e1x)
				+ Math.abs(mx);
		double second = Math.abs(h0) * Math.abs(e0y) + Math.abs(h1) * Math.abs(e1y)
				+ Math.abs(my);
		return up(DRIVER_BOUND * Math.max(first, second));
	}

	/**
	 * @return whether the inherited computation keeps the unlabelled second output defined
	 *         at every binary64 driver point of the box; {@code false} puts it in the band
	 */
	static boolean keepsOutput(Box box) {
		if (!box.consistent) {
			return false;
		}
		Live live = new Live(box);
		if (!live.bounded()) {
			return false;
		}
		int[] exponents = exponents(Math.max(live.aLow, Math.max(live.bLow, live.cLow)),
				Math.max(live.aHigh, Math.max(live.bHigh, live.cHigh)),
				Math.min(live.aLow, Math.min(live.bLow, live.cLow)),
				Math.min(live.aHigh, Math.min(live.bHigh, live.cHigh)));
		if (exponents == null) {
			return false;
		}
		for (int exponent : exponents) {
			if (!live.keepsOutput(exponent)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The live line of one box: {@code GeoVec3D.lineThroughPoints} of the constant point and
	 * the live driver point, rounded differences and a rounded cross product. Each
	 * coefficient differs from the exact one of the box by the driver error and one rounding
	 * (two for {@code c}); its value at the centre of the circle differs by the driver error
	 * times the distance from the constant point to the centre, the roundings and the
	 * residual of the point {@code p} of the inherited computation on the line.
	 */
	private static final class Live {
		private final Box box;
		private final double aExact;
		private final double bExact;
		private final double aHigh;
		private final double bHigh;
		private final double cHigh;
		private final double aLow;
		private final double bLow;
		private final double cLow;
		private final double direction;
		private final double directionError;
		private final double constantError;
		private final double normError;
		private final double distanceError;
		private final double centreX;
		private final double centreY;

		Live(Box box) {
			this.box = box;
			double driverX = magnitude(box.driverX) + box.driverError;
			double driverY = magnitude(box.driverY) + box.driverError;
			double pointX = Math.abs(box.pointX);
			double pointY = Math.abs(box.pointY);
			aExact = magnitude(box.a);
			bExact = magnitude(box.b);
			double crossTerms = up(pointX * driverY + pointY * driverX);
			directionError = up(box.driverError + UNIT * (Math.max(aExact, bExact)
					+ box.driverError));
			constantError = up((pointX + pointY) * box.driverError + gamma(2) * crossTerms);
			aHigh = up(aExact + directionError);
			bHigh = up(bExact + directionError);
			cHigh = up(magnitude(box.c) + constantError);
			aLow = down(least(box.a) - directionError);
			bLow = down(least(box.b) - directionError);
			cLow = down(least(box.c) - constantError);
			direction = Math.max(aLow, bLow);
			normError = up(2 * (aExact + bExact) * directionError
					+ 2 * directionError * directionError);
			centreX = -box.a4;
			centreY = -box.a5;
			distanceError = up(box.driverError * (Math.abs(box.pointX - centreX)
					+ Math.abs(box.pointY - centreY)) + UNIT * (aHigh * Math.abs(centreX)
							+ bHigh * Math.abs(centreY)) + gamma(2) * crossTerms + UNIT * cHigh);
		}

		boolean bounded() {
			return direction > 0 && box.norm.lower > normError && box.radiusSquared.lower > 0
					&& Double.isFinite(cHigh) && Double.isFinite(distanceError);
		}

		boolean keepsOutput(int exponent) {
			double scale = Math.scalb(1.0, exponent);
			double squared = scale * scale;
			double xHigh = scale * Math.max(aHigh, bHigh);
			double xLow = scale * direction;
			// u = x^2 + y^2 from a^2 + b^2 of the box.
			double uLow = down(squared * down(box.norm.lower - normError));
			double uHigh = up(squared * (box.norm.upper + normError));
			double uComputedLow = down(uLow * (1 - gamma(2)));
			// 1. The degenerate branch needs u < eps.
			if (!(uComputedLow >= FIRST_EPS)) {
				return false;
			}
			Axis axis = axis();
			if (axis == null) {
				return false;
			}
			double centre = Math.max(Math.abs(box.a4), Math.abs(box.a5));
			// d = p.S.v + a.v and w = evaluate(A, p), term by term, and their roundings.
			double dTerms = up((axis.point + 2 * centre) * xHigh);
			double dError = up(gamma(3) * dTerms);
			double wTerms = up(Math.abs(box.a2) + 2 * centre * axis.point
					+ axis.point * axis.point);
			double wError = up(gamma(4) * wTerms);
			double wHigh = Math.min(wTerms, axis.wHigh);
			// Exact dis of the live inputs: 4^k ((a_f^2 + b_f^2) R^2 - (s_f - e_p)^2).
			double distance = magnitude(box.distance);
			double perturbation = up(normError * box.radiusSquared.upper
					+ 2 * distance * distanceError + distanceError * distanceError);
			double innerLow = down(box.discriminant.lower - perturbation);
			if (!(innerLow > 0)) {
				return false;
			}
			double exactLow = down(squared * innerLow);
			double exactHigh = up(squared * (box.discriminant.upper + perturbation));
			// |d| = sqrt(dis + u w) for the exact inputs.
			double dHigh = Math.min(dTerms, up(Math.sqrt(up(exactHigh + uHigh * wHigh))));
			// A negative product is rounded down: the sum may cancel, and its slack would not
			// cover the rounding of the product (review of R7, finding R7-B).
			double product = axis.wLow >= 0 ? uLow * axis.wLow
					: Math.nextDown(uHigh * axis.wLow);
			double dLow = down(Math.sqrt(down(exactLow + product)));
			double uError = up(gamma(2) * uHigh);
			// dis = d * d - u * w: rounding of its evaluation from d, u and w of exact inputs.
			double rounding = up((2 * dHigh + dError) * dError + (uHigh + uError) * wError
					+ uError * wHigh + gamma(2) * ((dHigh + dError) * (dHigh + dError)
							+ (uHigh + uError) * (wHigh + wError)));
			// 2. Tangent when |dis| <= delta = min(1e-5, max(1, |2d| + |u| + |w|) eps).
			double window = Math.min(MIN_PRECISION, up(Math.max(1, up((2 * (dHigh + dError)
					+ uHigh + uError + wHigh + wError) * (1 + gamma(3)))) * FIRST_EPS
					* (1 + UNIT)));
			double computedLow = down(exactLow - rounding);
			if (!(computedLow > window)) {
				return false;
			}
			// The roots: s = sqrt(dis) with the sign of d, q = -(d + s), q / u and w / q.
			double rootLow = down(Math.sqrt(exactLow));
			double rootHigh = up(Math.sqrt(exactHigh));
			// |fl(sqrt(dis_c)) - sqrt(dis)| <= |dis_c - dis| / (sqrt(dis_c) + sqrt(dis))
			// + u sqrt(dis_c): times sqrt(dis), at most the rounding plus sqrt(dis) u sqrt(dis_c)
			// at every point of the box.
			double sqrtRounding = up(UNIT * up(Math.sqrt(up(exactHigh + rounding))));
			double sqrtError = up(rounding / (down(Math.sqrt(computedLow)) + rootLow)
					+ sqrtRounding);
			double rootSqrtError = up(rounding + rootHigh * sqrtRounding);
			double qRest = up(dError + UNIT * (dHigh + dError + rootHigh + sqrtError));
			double qError = up(qRest + sqrtError);
			// s takes the sign of the computed d, which is that of d unless |d| is within the
			// error of d: then |q| = |d| + s, otherwise only |q| >= s - |d|.
			double qLow = dLow > dError ? down(dLow + rootLow) : down(rootLow - dError);
			if (!(qLow > qError)) {
				return false;
			}
			double qHigh = up(dHigh + rootHigh);
			double quotient = up((qError + qHigh * gamma(2) + UNIT * (qHigh + qError))
					/ uComputedLow);
			double inverse = up((wError + wHigh * qError / qLow + UNIT * (wHigh + wError))
					/ down(qLow - qError));
			double rootError = Math.max(quotient, inverse);
			// sqrt(dis) times the error of each root, with the error of the square root bounded
			// at every point rather than by the extremes of the box.
			double slope = Math.max(up((rootHigh * (qRest + qHigh * gamma(2)
					+ UNIT * (qHigh + qError)) + rootSqrtError) / uComputedLow),
					up((rootHigh * (wError + UNIT * (wHigh + wError))
							+ wHigh * (rootHigh * qRest + rootSqrtError) / qLow)
							/ down(qLow - qError)));
			double parameter = up((dHigh + rootHigh) / uLow);
			double radius = up(Math.sqrt(box.radiusSquared.upper));
			double coordinates = up(2 * (Math.hypot(centreX, centreY) + radius));
			// px + t y and py - t x: each coordinate within this of the exact root, of which
			// only the rounding takes the point off the line through p.
			double coordinateRounding = up(UNIT * (parameter + rootError) * xHigh
					+ UNIT * coordinates);
			double coordinateError = up(rootError * xHigh + coordinateRounding);
			// 4. The roots differ by 2 sqrt(dis) / u along (y, -x).
			double separation = down(down(2 * rootLow / uHigh) * xLow) - 2 * coordinateError;
			if (!(down(separation) > up(STANDARD_PRECISION
					+ 2 * UNIT * (coordinates + STANDARD_PRECISION)))) {
				return false;
			}
			return incident(slope, coordinates, coordinateError, coordinateRounding, radius);
		}

		/**
		 * The point {@code p} that the inherited computation takes on an axis,
		 * {@code (-z / x, 0)} when {@code |x| > |y|} and {@code (0, -z / y)} otherwise (one
		 * rounding; the exponent cancels), over the branches possible on the box, with
		 * {@code w = |p - m|^2 - R^2}.
		 *
		 * @return bounds of {@code |p|} and {@code w}, or {@code null} when unbounded
		 */
		private Axis axis() {
			SplineOutwardInterval2D a = widen(box.a, directionError);
			SplineOutwardInterval2D b = widen(box.b, directionError);
			SplineOutwardInterval2D c = widen(box.c, constantError);
			Axis axis = null;
			try {
				if (aHigh > bLow) {
					axis = Axis.merge(axis, branch(a, c, bLow, centreX, centreY));
				}
				if (bHigh >= aLow) {
					axis = Axis.merge(axis, branch(b, c, aLow, centreY, centreX));
				}
			} catch (ArithmeticException exception) {
				return null;
			}
			return axis;
		}

		/**
		 * @param divisor coefficient of the axis of {@code p}, at least {@code other} in
		 *        magnitude in this branch
		 * @param along coordinate of the centre on that axis
		 * @param across the other coordinate of the centre
		 */
		private Axis branch(SplineOutwardInterval2D divisor, SplineOutwardInterval2D c,
				double other, double along, double across) {
			SplineOutwardInterval2D coordinate;
			if (!divisor.containsZero()) {
				SplineOutwardInterval2D quotient = c.divide(divisor).negate();
				coordinate = new SplineOutwardInterval2D(
						fall(quotient.lower, Math.abs(quotient.lower) * UNIT),
						rise(quotient.upper, Math.abs(quotient.upper) * UNIT));
			} else if (other > 0) {
				double bound = up(magnitude(c) / other * (1 + UNIT));
				coordinate = new SplineOutwardInterval2D(-bound, bound);
			} else {
				throw new ArithmeticException("Unbounded point of the line on an axis");
			}
			SplineOutwardInterval2D w = square(coordinate.subtract(
					SplineOutwardInterval2D.point(along)))
					.add(square(SplineOutwardInterval2D.point(across)))
					.subtract(box.radiusSquared);
			return new Axis(magnitude(coordinate), w.lower, magnitude(w));
		}

		/**
		 * 3. The inherited incidence tests of the root computed for the output. The exact
		 * root has {@code (X - m).v = +/- sqrt(dis)}, so the computed one is at
		 * {@code |dist^2 - R^2| <= 2 sqrt(dis) |dt| + 2 R |r| + |dX|^2} from the circle, with
		 * {@code r} the rounding of its coordinates and {@code sqrt(dis) |dt|} at most the
		 * slope; the inherited test divides by the square of the captured half axis
		 * {@code h0}, whose gap to {@code R^2} is enclosed exactly. The computed point
		 * {@code p + t v} is on the line through {@code p} whatever the error of {@code t}:
		 * only the residual of {@code p} and the rounding of the coordinates take it off the
		 * full line.
		 */
		private boolean incident(double slope, double coordinates, double coordinateError,
				double coordinateRounding, double radius) {
			double square = box.halfAxis * box.halfAxis;
			double squareLow = Math.nextDown(square);
			if (!(squareLow > 0)) {
				return false;
			}
			double representation = Math.nextUp(Math.max(
					Math.nextUp(square) - box.radiusSquared.lower,
					box.radiusSquared.upper - squareLow));
			double residual = up(2 * slope + 2 * radius * Math.sqrt(2) * coordinateRounding
					+ 2 * coordinateError * coordinateError);
			double circle = up((residual + representation) / squareLow * (1 + gamma(7))
					+ gamma(7));
			if (!(circle <= MIN_PRECISION - 0x1.0p-50)) {
				return false;
			}
			double line = up(UNIT * cHigh + (aHigh + bHigh) * coordinateRounding
					+ gamma(3) * ((aHigh + bHigh) * coordinates + cHigh));
			if (!(line < down(MIN_PRECISION * (aLow + bLow) * (1 - gamma(2)) * (1 - UNIT)))) {
				return false;
			}
			return box.startX == null || onPath(coordinates, coordinateError);
		}

		/**
		 * The parameter of a ray or segment that {@code GeoLine.isOnPath} computes, in units
		 * of the live direction, stays within an error of the parameter of the exact root of
		 * the box, which the box certified inside the path: the exact roots of the live line
		 * move from those of the box with their foot and offset, the computed root by its
		 * coordinate error, and the projection and the division add their roundings. The test
		 * admits {@code 1e-5} outside the path, so the error may reach the margin of the exact
		 * root inside it plus {@code 1e-5}.
		 */
		private boolean onPath(double coordinates, double coordinateError) {
			double normLow = down(box.norm.lower - normError);
			double root = Math.sqrt(normLow);
			// Unit normal: |n_f / |n_f| - n / |n|| <= 2 |n_f - n| / |n|.
			double turn = up(2 * Math.sqrt(2) * directionError / root);
			double distance = magnitude(box.distance);
			double height = up(distance / root);
			double heightShift = up(distanceError / root
					+ distance * normError / (2 * normLow * root));
			double footShift = up(heightShift + height * turn);
			double offsetLow = down(Math.sqrt(down(box.discriminant.lower
					/ up(box.norm.upper + normError))));
			if (!(offsetLow > 0)) {
				return false;
			}
			double radius = up(Math.sqrt(box.radiusSquared.upper));
			double offsetShift = up((2 * height + heightShift) * heightShift / offsetLow
					+ radius * turn);
			double start = Math.max(magnitude(box.startX), magnitude(box.startY))
					+ (box.startIsDriver ? box.driverError : 0);
			double projection = up(Math.sqrt(2) * coordinateError
					+ gamma(4) * (cHigh + (aHigh + bHigh) * coordinates)
							* Math.max(aHigh, bHigh) / down(aLow * aLow + bLow * bLow)
					+ 2 * UNIT * coordinates);
			double shift = up(footShift + offsetShift + projection
					+ (box.startIsDriver ? Math.sqrt(2) * box.driverError : 0));
			// The start is a maximum norm: its Euclidean norm, which the change of the units of
			// the direction multiplies, is at most sqrt(2) times it (review of R7, R7-C).
			double reach = up(coordinates + Math.sqrt(2) * start);
			double error = up((shift + UNIT * reach) / direction * (1 + UNIT)
					+ reach * (Math.sqrt(2) * directionError / normLow + UNIT / direction));
			return error < (Math.min(box.pathLow, box.endLow) + MIN_PRECISION)
					* (1 - 0x1.0p-20);
		}
	}

	/** Bounds of the point {@code p} of the inherited computation and of {@code w}. */
	private static final class Axis {
		/** Upper bound of {@code |p|}. */
		private final double point;
		/** Lower bound of {@code w}, which may be negative. */
		private final double wLow;
		/** Upper bound of {@code |w|}. */
		private final double wHigh;

		Axis(double point, double wLow, double wHigh) {
			this.point = point;
			this.wLow = wLow;
			this.wHigh = wHigh;
		}

		static Axis merge(Axis first, Axis second) {
			if (first == null) {
				return second;
			}
			return new Axis(Math.max(first.point, second.point),
					Math.min(first.wLow, second.wLow), Math.max(first.wHigh, second.wHigh));
		}
	}

	/** @return the enclosure widened by an absolute error, outward */
	private static SplineOutwardInterval2D widen(SplineOutwardInterval2D value, double error) {
		return new SplineOutwardInterval2D(fall(value.lower, error), rise(value.upper, error));
	}

	/** @return a lower bound of {@code value - error}, rounded outward */
	private static double fall(double value, double error) {
		return Math.nextDown(value - error);
	}

	/** @return an upper bound of {@code value + error}, rounded outward */
	private static double rise(double value, double error) {
		return Math.nextUp(value + error);
	}

	/** @return a nonnegative enclosure of the square */
	private static SplineOutwardInterval2D square(SplineOutwardInterval2D value) {
		if (value.containsZero()) {
			double magnitude = magnitude(value);
			return new SplineOutwardInterval2D(0, Math.nextUp(magnitude * magnitude));
		}
		SplineOutwardInterval2D squared = value.multiply(value);
		return squared.lower < 0 ? new SplineOutwardInterval2D(0, squared.upper) : squared;
	}

	/**
	 * Exponents {@code k} that {@code GeoLine.getNormalizedCoefficients(xyz, 2, 0.5)} may
	 * apply on the box: {@code 0} when the three coefficients may all be zero for
	 * {@code DoubleUtil.isZero}; doublings while the largest magnitude is below
	 * {@code 0.5}; halvings while the smallest is above {@code 2}.
	 *
	 * @return the exponents, or {@code null} when there are too many
	 */
	private static int[] exponents(double largestLow, double largestHigh, double smallestLow,
			double smallestHigh) {
		TreeSet<Integer> exponents = new TreeSet<>();
		if (!(largestLow >= STANDARD_PRECISION)) {
			exponents.add(0);
		}
		if (!(largestLow >= 0.5)) {
			int fewest = doublings(largestHigh);
			int most = doublings(largestLow);
			if (fewest < 0 || most < 0 || most - fewest > MAXIMUM_EXPONENTS) {
				return null;
			}
			for (int exponent = fewest; exponent <= most; exponent++) {
				exponents.add(exponent);
			}
		}
		if (largestHigh >= 0.5) {
			int fewest = halvings(smallestLow);
			int most = halvings(smallestHigh);
			if (fewest < 0 || most < 0 || most - fewest > MAXIMUM_EXPONENTS) {
				return null;
			}
			for (int exponent = fewest; exponent <= most; exponent++) {
				exponents.add(-exponent);
			}
		}
		if (exponents.size() > MAXIMUM_EXPONENTS) {
			return null;
		}
		int[] values = new int[exponents.size()];
		int index = 0;
		for (int exponent : exponents) {
			values[index++] = exponent;
		}
		return values;
	}

	/** @return doublings of the loop while the value is below 0.5, or -1 */
	private static int doublings(double value) {
		if (!(value > 0) || Double.isInfinite(value)) {
			return -1;
		}
		int count = 0;
		while (Math.scalb(value, count) < 0.5) {
			count++;
		}
		return count;
	}

	/** @return halvings of the loop while the value is above 2, or -1 */
	private static int halvings(double value) {
		if (!(value >= 0) || Double.isInfinite(value)) {
			return -1;
		}
		int count = 0;
		while (Math.scalb(value, -count) > 2) {
			count++;
		}
		return count;
	}

	private static double magnitude(SplineOutwardInterval2D value) {
		return Math.max(Math.abs(value.lower), Math.abs(value.upper));
	}

	/** @return the least magnitude on the enclosure, zero when it contains zero */
	private static double least(SplineOutwardInterval2D value) {
		if (value.lower <= 0 && value.upper >= 0) {
			return 0;
		}
		return Math.min(Math.abs(value.lower), Math.abs(value.upper));
	}

	private static double gamma(int count) {
		return count * UNIT / (1 - count * UNIT);
	}

	/** @return a nonnegative bound raised by the slack of its binary64 evaluation */
	private static double up(double value) {
		return value * (1 + SLACK);
	}

	/** @return a nonnegative lower bound lowered by the slack, or zero */
	private static double down(double value) {
		return value > 0 ? value * (1 - SLACK) : 0;
	}
}
