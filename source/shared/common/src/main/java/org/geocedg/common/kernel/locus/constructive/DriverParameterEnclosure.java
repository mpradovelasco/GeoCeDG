/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.math.BigInteger;
import java.util.Objects;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.constructive.ExactQuadraticTower.Element;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * The driver parameter of an exact point on the captured driver support of a locus (Stage A
 * design section 2.3, fact 3): the parameter that the inherited path map assigns to the point,
 * evaluated exactly on the captured driver constants of the class-v3 program. On a circle it is
 * {@code atan2(h0 py, h1 px)} of the point in the frame of the eigenvectors, exact when the point
 * lies on an axis of the frame ({@code 0}, {@code pi}, {@code pi/2} or {@code -pi/2}) and
 * otherwise enclosed with {@code StrictMath.atan2} widened by three binary64 on each side,
 * beyond the two ulps that the platform guarantees; an enclosure that
 * crosses the seam at {@code pi} is lifted above it. On a segment it is the exact projection
 * parameter. The enclosure never comes from the binary64 coordinates of the point alone.
 */
final class DriverParameterEnclosure {

	private final double lower;
	private final double upper;
	private final String exact;
	private final OutwardInterval[] drivenPoint;
	private final ExactQuadraticTower tower;
	private final Element value;

	private DriverParameterEnclosure(double lower, double upper, String exact,
			OutwardInterval[] drivenPoint) {
		this(lower, upper, exact, drivenPoint, null, null);
	}

	private DriverParameterEnclosure(double lower, double upper, String exact,
			OutwardInterval[] drivenPoint, ExactQuadraticTower tower, Element value) {
		this.lower = lower;
		this.upper = upper;
		this.exact = exact;
		this.drivenPoint = drivenPoint;
		this.tower = tower;
		this.value = value;
	}

	/**
	 * @param program the captured program, with a circle or segment driver
	 * @param tower the tower of the exact point
	 * @param x exact abscissa of the point
	 * @param y exact ordinate of the point
	 * @return the enclosure, or {@code null} when the point gives no parameter (the centre
	 *         of a circle, an unsupported driver or an undecided enclosure)
	 */
	static DriverParameterEnclosure of(CertifiedConstructionProgram2D program,
			ExactQuadraticTower tower, Element x, Element y) {
		Objects.requireNonNull(program);
		if (program.getDriver() == Driver.CIRCLE) {
			return circle(program, tower, x, y);
		}
		if (program.getDriver() == Driver.SEGMENT) {
			return segment(program, tower, x, y);
		}
		return null;
	}

	/**
	 * The enclosure of an exact parameter of a circle driver, for a closed decision at that
	 * parameter when no point of the document stands there (PRE-G9B-CU3 increment E3).
	 *
	 * @param program the captured program
	 * @param parameter {@code 0}, {@code pi/2}, {@code pi} or {@code -pi/2}
	 * @return the enclosure, or {@code null} for another driver or another parameter
	 */
	static DriverParameterEnclosure atExactParameter(CertifiedConstructionProgram2D program,
			String parameter) {
		Objects.requireNonNull(program);
		if (program.getDriver() != Driver.CIRCLE) {
			return null;
		}
		double[] c = constants(program, 8);
		double half = Math.PI / 2;
		switch (parameter) {
		case "0":
			return exact(0, 0, "0", circlePoint(c, 1, 0));
		case "pi/2":
			return exact(half, Math.nextUp(half), "pi/2", circlePoint(c, 0, 1));
		case "pi":
			return exact(Math.PI, Math.nextUp(Math.PI), "pi", circlePoint(c, -1, 0));
		case "-pi/2":
			return exact(-Math.nextUp(half), -half, "-pi/2", circlePoint(c, 0, -1));
		default:
			return null;
		}
	}

	private static DriverParameterEnclosure circle(CertifiedConstructionProgram2D program,
			ExactQuadraticTower tower, Element x, Element y) {
		double[] c = constants(program, 8);
		Element dx = x.subtract(tower.ofBinary64(c[6]));
		Element dy = y.subtract(tower.ofBinary64(c[7]));
		Element px = dx.multiply(tower.ofBinary64(c[2])).add(dy.multiply(tower.ofBinary64(c[3])));
		Element py = dx.multiply(tower.ofBinary64(c[4])).add(dy.multiply(tower.ofBinary64(c[5])));
		int signX = px.signum();
		int signY = py.signum();
		if (signX == 0 && signY == 0) {
			return null;
		}
		if (signY == 0) {
			return signX > 0 ? exact(0, 0, "0", circlePoint(c, 1, 0))
					: exact(Math.PI, Math.nextUp(Math.PI), "pi", circlePoint(c, -1, 0));
		}
		if (signX == 0) {
			double half = Math.PI / 2;
			return signY > 0 ? exact(half, Math.nextUp(half), "pi/2", circlePoint(c, 0, 1))
					: exact(-Math.nextUp(half), -half, "-pi/2", circlePoint(c, 0, -1));
		}
		OutwardInterval ex = OutwardInterval.point(c[1]).multiply(enclosure(px));
		OutwardInterval ey = OutwardInterval.point(c[0]).multiply(enclosure(py));
		double[] angle = atan2(ey, ex);
		if (angle == null) {
			return null;
		}
		OutwardInterval[] trig = cosSin(angle[0], angle[1]);
		if (trig == null) {
			return null;
		}
		return new DriverParameterEnclosure(angle[0], angle[1], null,
				circlePoint(c, trig[0], trig[1]));
	}

	private static DriverParameterEnclosure segment(CertifiedConstructionProgram2D program,
			ExactQuadraticTower tower, Element x, Element y) {
		double[] c = constants(program, 4);
		Element a = tower.ofBinary64(c[2]);
		Element b = tower.ofBinary64(c[3]);
		Element norm = a.square().add(b.square());
		if (norm.signum() <= 0) {
			return null;
		}
		Element t = x.subtract(tower.ofBinary64(c[0])).multiply(b)
				.subtract(y.subtract(tower.ofBinary64(c[1])).multiply(a)).divide(norm);
		double[] bounds = t.outwardEnclosure();
		OutwardInterval interval = OutwardInterval.of(bounds[0], bounds[1]);
		OutwardInterval[] point = {OutwardInterval.point(c[0]).add(interval
				.multiply(OutwardInterval.point(c[3]))), OutwardInterval.point(c[1])
						.subtract(interval.multiply(OutwardInterval.point(c[2])))};
		return new DriverParameterEnclosure(bounds[0], bounds[1],
				t.isRational() ? text(t.getRational()) : null, point, tower, t);
	}

	/**
	 * The domain of a segment driver, decided exactly: a point of the support line whose
	 * parameter lies outside it is no position of the driver, whatever an inherited algorithm
	 * with outlying intersections accepts.
	 *
	 * @param domain the canonical driver domain
	 * @return whether the exact parameter lies outside the domain, or {@code null} for a
	 *         periodic parameter
	 */
	Boolean isOutside(LocusInterval2D domain) {
		if (value == null) {
			return null;
		}
		int below = value.subtract(tower.ofBinary64(domain.getLower())).signum();
		int above = value.subtract(tower.ofBinary64(domain.getUpper())).signum();
		return below < 0 || below == 0 && !domain.isLowerClosed() || above > 0
				|| above == 0 && !domain.isUpperClosed();
	}

	/** @return an integer without its denominator, otherwise the fraction */
	private static String text(ExactRational2D value) {
		return BigInteger.ONE.equals(value.denominator()) ? value.numerator().toString()
				: value.toString();
	}

	/** @return the lower bound of the parameter, lifted above the seam when it crosses it */
	double getLower() {
		return lower;
	}

	/** @return the upper bound of the parameter */
	double getUpper() {
		return upper;
	}

	/** @return the exact value when it is known exactly, otherwise {@code null} */
	String getExact() {
		return exact;
	}

	/**
	 * @return an enclosure of the captured driver point at every parameter of the enclosure:
	 *         abscissa and ordinate
	 */
	OutwardInterval[] getDrivenPoint() {
		return drivenPoint.clone();
	}

	@Override
	public String toString() {
		return exact != null ? exact : "[" + lower + ", " + upper + "]";
	}

	private static DriverParameterEnclosure exact(double lower, double upper, String exact,
			OutwardInterval[] point) {
		return new DriverParameterEnclosure(lower, upper, exact, point);
	}

	private static double[] constants(CertifiedConstructionProgram2D program, int count) {
		double[] c = new double[count];
		for (int index = 0; index < count; index++) {
			c[index] = program.getDriverConstant(index);
		}
		return c;
	}

	/** The captured driver point {@code m + h0 cos t e0 + h1 sin t e1} for exact cos and sin. */
	private static OutwardInterval[] circlePoint(double[] c, double cos, double sin) {
		return circlePoint(c, OutwardInterval.point(cos), OutwardInterval.point(sin));
	}

	private static OutwardInterval[] circlePoint(double[] c, OutwardInterval cos,
			OutwardInterval sin) {
		OutwardInterval px = OutwardInterval.point(c[0]).multiply(cos);
		OutwardInterval py = OutwardInterval.point(c[1]).multiply(sin);
		return new OutwardInterval[] {
				px.multiply(OutwardInterval.point(c[2])).add(py.multiply(OutwardInterval
						.point(c[4]))).add(OutwardInterval.point(c[6])),
				px.multiply(OutwardInterval.point(c[3])).add(py.multiply(OutwardInterval
						.point(c[5]))).add(OutwardInterval.point(c[7]))};
	}

	private static OutwardInterval enclosure(Element value) {
		double[] bounds = value.outwardEnclosure();
		return OutwardInterval.of(bounds[0], bounds[1]);
	}

	/**
	 * Enclosure of {@code atan2(y, x)} over a box that does not contain the origin: the
	 * extremes lie at its corners; a box on the negative abscissa that crosses the axis is
	 * lifted across the seam, its angles below zero taken plus {@code 2 pi}.
	 *
	 * @return lower and upper bounds, or {@code null} when the box may contain the origin
	 */
	static double[] atan2(OutwardInterval y, OutwardInterval x) {
		if (!x.isFinite() || !y.isFinite()) {
			return null;
		}
		boolean crossesSeam = x.getUpper() < 0 && y.getLower() <= 0 && y.getUpper() >= 0;
		if (!crossesSeam && !(x.getLower() > 0 || y.getLower() > 0 || y.getUpper() < 0)) {
			return null;
		}
		double low = Double.POSITIVE_INFINITY;
		double high = Double.NEGATIVE_INFINITY;
		for (double cy : new double[] {y.getLower(), y.getUpper()}) {
			for (double cx : new double[] {x.getLower(), x.getUpper()}) {
				double angle = StrictMath.atan2(cy == 0 ? 0.0 : cy, cx);
				double lowAngle = Math.nextDown(Math.nextDown(Math.nextDown(angle)));
				double highAngle = Math.nextUp(Math.nextUp(Math.nextUp(angle)));
				if (crossesSeam && angle < 0) {
					lowAngle = Math.nextDown(lowAngle + 2 * Math.PI);
					highAngle = Math.nextUp(Math.nextUp(highAngle + Math.nextUp(2 * Math.PI)));
				}
				low = Math.min(low, lowAngle);
				high = Math.max(high, highAngle);
			}
		}
		return new double[] {low, high};
	}

	/**
	 * Enclosures of the cosine and sine of every angle of a short interval: the values at
	 * its ends, widened beyond the error of {@code StrictMath}, with the extreme {@code 1} or
	 * {@code -1} wherever a multiple of {@code pi/2} may lie inside.
	 *
	 * @return cosine and sine, or {@code null} for an interval longer than one radian
	 */
	static OutwardInterval[] cosSin(double lower, double upper) {
		if (!(upper - lower <= 1) || !Double.isFinite(lower) || !Double.isFinite(upper)) {
			return null;
		}
		OutwardInterval cos = widened(StrictMath.cos(lower)).hull(widened(StrictMath.cos(upper)));
		OutwardInterval sin = widened(StrictMath.sin(lower)).hull(widened(StrictMath.sin(upper)));
		double halfPiLow = Math.PI / 2;
		double halfPiHigh = Math.nextUp(halfPiLow);
		long first = (long) Math.floor(lower / halfPiHigh) - 1;
		long last = (long) Math.ceil(upper / halfPiLow) + 1;
		for (long k = first; k <= last; k++) {
			double low = k >= 0 ? k * halfPiLow : k * halfPiHigh;
			double high = k >= 0 ? k * halfPiHigh : k * halfPiLow;
			if (Math.nextUp(high) < lower || Math.nextDown(low) > upper) {
				continue;
			}
			int quarter = (int) Math.floorMod(k, 4L);
			if (quarter == 0) {
				cos = cos.hull(OutwardInterval.point(1));
			} else if (quarter == 2) {
				cos = cos.hull(OutwardInterval.point(-1));
			} else if (quarter == 1) {
				sin = sin.hull(OutwardInterval.point(1));
			} else {
				sin = sin.hull(OutwardInterval.point(-1));
			}
		}
		return new OutwardInterval[] {clamp(cos), clamp(sin)};
	}

	private static OutwardInterval widened(double value) {
		return OutwardInterval.of(Math.nextDown(Math.nextDown(value)),
				Math.nextUp(Math.nextUp(value)));
	}

	private static OutwardInterval clamp(OutwardInterval value) {
		return OutwardInterval.of(Math.max(-1, value.getLower()), Math.min(1, value.getUpper()));
	}

	/**
	 * @param exactValue an exact rational
	 * @return its tightest binary64 enclosure
	 */
	static OutwardInterval enclosing(ExactRational2D exactValue) {
		return OutwardInterval.enclosing(exactValue);
	}
}
