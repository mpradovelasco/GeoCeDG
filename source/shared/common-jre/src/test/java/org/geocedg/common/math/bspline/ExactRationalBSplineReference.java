/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Test-only exact reference for 2D B-splines, independent of the library under test.
 *
 * <p>Every value is an exact rational ({@link Fraction}) built from the binary64 inputs
 * without rounding. Points use the Cox-de Boor basis of the owning span computed by the
 * triangular recurrence over {@code Q}; first derivatives use the basis-derivative formula
 * {@code N'_{i,p} = p N_{i,p-1} / (u_{i+p} - u_i) - p N_{i+1,p-1} / (u_{i+p+1} - u_{i+1})}
 * and, for rational data, the exact quotient {@code C' = (A' W - A W') / W^2}. None of these
 * shares an operation sequence with the library (de Boor, hodograph de Boor). Span ownership
 * is an independent linear scan. Knot insertion is an exact Boehm insertion over {@code Q}.
 * The class is reusable by later tests (MAINT-A2, MAINT-A-DXF0).</p>
 */
public final class ExactRationalBSplineReference {

	private ExactRationalBSplineReference() {
	}

	/** Exact reduced rational with a positive denominator. */
	public static final class Fraction implements Comparable<Fraction> {
		/** Zero. */
		public static final Fraction ZERO = new Fraction(BigInteger.ZERO, BigInteger.ONE);
		/** One. */
		public static final Fraction ONE = new Fraction(BigInteger.ONE, BigInteger.ONE);

		private final BigInteger num;
		private final BigInteger den;

		private Fraction(BigInteger num, BigInteger den) {
			this.num = num;
			this.den = den;
		}

		/**
		 * @param num numerator
		 * @param den nonzero denominator
		 * @return the reduced fraction
		 */
		public static Fraction of(BigInteger num, BigInteger den) {
			if (den.signum() == 0) {
				throw new ArithmeticException("zero denominator");
			}
			BigInteger n = den.signum() < 0 ? num.negate() : num;
			BigInteger d = den.abs();
			if (n.signum() == 0) {
				return ZERO;
			}
			BigInteger g = n.gcd(d);
			return new Fraction(n.divide(g), d.divide(g));
		}

		/**
		 * @param value integer
		 * @return the fraction
		 */
		public static Fraction of(long value) {
			return of(BigInteger.valueOf(value), BigInteger.ONE);
		}

		/**
		 * Exact value of a finite binary64 number, through its exact decimal expansion.
		 *
		 * @param value finite double
		 * @return the fraction
		 */
		public static Fraction of(double value) {
			if (!Double.isFinite(value)) {
				throw new ArithmeticException("nonfinite");
			}
			BigDecimal exact = new BigDecimal(value);
			BigInteger unscaled = exact.unscaledValue();
			int scale = exact.scale();
			if (scale <= 0) {
				return of(unscaled.multiply(BigInteger.TEN.pow(-scale)), BigInteger.ONE);
			}
			return of(unscaled, BigInteger.TEN.pow(scale));
		}

		/**
		 * @param o other
		 * @return sum
		 */
		public Fraction add(Fraction o) {
			return of(num.multiply(o.den).add(o.num.multiply(den)), den.multiply(o.den));
		}

		/**
		 * @param o other
		 * @return difference
		 */
		public Fraction sub(Fraction o) {
			return of(num.multiply(o.den).subtract(o.num.multiply(den)), den.multiply(o.den));
		}

		/**
		 * @param o other
		 * @return product
		 */
		public Fraction mul(Fraction o) {
			return of(num.multiply(o.num), den.multiply(o.den));
		}

		/**
		 * @param o nonzero other
		 * @return quotient
		 */
		public Fraction div(Fraction o) {
			return of(num.multiply(o.den), den.multiply(o.num));
		}

		/**
		 * @return negation
		 */
		public Fraction neg() {
			return of(num.negate(), den);
		}

		/**
		 * @return sign
		 */
		public int signum() {
			return num.signum();
		}

		/**
		 * @return numerator
		 */
		public BigInteger numerator() {
			return num;
		}

		/**
		 * @return denominator
		 */
		public BigInteger denominator() {
			return den;
		}

		/**
		 * @param mc precision
		 * @return decimal approximation
		 */
		public BigDecimal toBigDecimal(MathContext mc) {
			return new BigDecimal(num).divide(new BigDecimal(den), mc);
		}

		/**
		 * @return a double approximation (for reporting magnitudes only, never a reference
		 *         value)
		 */
		public double approx() {
			return toBigDecimal(new MathContext(40, RoundingMode.HALF_EVEN)).doubleValue();
		}

		@Override
		public int compareTo(Fraction o) {
			return num.multiply(o.den).compareTo(o.num.multiply(den));
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Fraction && ((Fraction) o).num.equals(num)
					&& ((Fraction) o).den.equals(den);
		}

		@Override
		public int hashCode() {
			return num.hashCode() * 31 + den.hashCode();
		}

		@Override
		public String toString() {
			return num + "/" + den;
		}
	}

	/**
	 * Owning span by an independent linear scan: the largest {@code j} in {@code p..n} with
	 * {@code u_j <= u < u_{j+1}}; for {@code u = b} the largest {@code j <= n} with
	 * {@code u_j < u_{j+1}}.
	 *
	 * @param knots knot vector
	 * @param p degree
	 * @param n last control-point index
	 * @param u parameter inside {@code [a, b]}
	 * @return span knot index
	 */
	public static int ownerSpan(double[] knots, int p, int n, double u) {
		if (u == knots[n + 1]) {
			for (int j = n; j >= p; j--) {
				if (knots[j] < knots[j + 1]) {
					return j;
				}
			}
			throw new IllegalArgumentException("no span");
		}
		for (int j = p; j <= n; j++) {
			if (knots[j] <= u && u < knots[j + 1]) {
				return j;
			}
		}
		throw new IllegalArgumentException("outside domain");
	}

	/**
	 * Nonzero basis functions {@code N_{j-p..j, p}(u)} of the polynomial piece of span
	 * {@code j}, by the triangular Cox-de Boor recurrence over {@code Q}.
	 *
	 * @param knots knot vector
	 * @param p degree
	 * @param j span knot index ({@code u_j < u_{j+1}})
	 * @param u exact parameter
	 * @return {@code p + 1} values
	 */
	public static Fraction[] basis(double[] knots, int p, int j, Fraction u) {
		Fraction[] n = new Fraction[p + 1];
		Fraction[] left = new Fraction[p + 1];
		Fraction[] right = new Fraction[p + 1];
		n[0] = Fraction.ONE;
		for (int r = 1; r <= p; r++) {
			left[r] = u.sub(Fraction.of(knots[j + 1 - r]));
			right[r] = Fraction.of(knots[j + r]).sub(u);
			Fraction saved = Fraction.ZERO;
			for (int k = 0; k < r; k++) {
				Fraction denominator = Fraction.of(knots[j + k + 1])
						.sub(Fraction.of(knots[j + 1 - r + k]));
				Fraction temp = n[k].div(denominator);
				n[k] = saved.add(right[k + 1].mul(temp));
				saved = left[r - k].mul(temp);
			}
			n[r] = saved;
		}
		return n;
	}

	/**
	 * Derivatives {@code N'_{j-p..j, p}(u)} of the basis of span {@code j}.
	 *
	 * @param knots knot vector
	 * @param p degree, at least 1
	 * @param j span knot index
	 * @param u exact parameter
	 * @return {@code p + 1} values
	 */
	public static Fraction[] basisDerivative(double[] knots, int p, int j, Fraction u) {
		Fraction[] lower = basis(knots, p - 1, j, u);
		Fraction[] d = new Fraction[p + 1];
		Fraction degree = Fraction.of(p);
		for (int k = 0; k <= p; k++) {
			int i = j - p + k;
			Fraction value = Fraction.ZERO;
			if (k >= 1) {
				Fraction width = Fraction.of(knots[i + p]).sub(Fraction.of(knots[i]));
				value = value.add(degree.mul(lower[k - 1]).div(width));
			}
			if (k <= p - 1) {
				Fraction width = Fraction.of(knots[i + p + 1]).sub(Fraction.of(knots[i + 1]));
				value = value.sub(degree.mul(lower[k]).div(width));
			}
			d[k] = value;
		}
		return d;
	}

	/**
	 * Homogeneous values {@code (A_x, A_y, W)} of the piece of span {@code j} at {@code u}.
	 *
	 * @param knots knot vector
	 * @param p degree
	 * @param xs x coordinates
	 * @param ys y coordinates
	 * @param ws weights or {@code null} for unit weights
	 * @param j span knot index
	 * @param u exact parameter
	 * @return three fractions
	 */
	public static Fraction[] homogeneous(double[] knots, int p, double[] xs, double[] ys,
			double[] ws, int j, Fraction u) {
		return combine(basis(knots, p, j, u), p, xs, ys, ws, j);
	}

	/**
	 * Homogeneous derivatives {@code (A'_x, A'_y, W')} of the piece of span {@code j}.
	 *
	 * @param knots knot vector
	 * @param p degree, at least 1
	 * @param xs x coordinates
	 * @param ys y coordinates
	 * @param ws weights or {@code null}
	 * @param j span knot index
	 * @param u exact parameter
	 * @return three fractions
	 */
	public static Fraction[] homogeneousDerivative(double[] knots, int p, double[] xs,
			double[] ys, double[] ws, int j, Fraction u) {
		return combine(basisDerivative(knots, p, j, u), p, xs, ys, ws, j);
	}

	private static Fraction[] combine(Fraction[] basis, int p, double[] xs, double[] ys,
			double[] ws, int j) {
		Fraction ax = Fraction.ZERO;
		Fraction ay = Fraction.ZERO;
		Fraction w = Fraction.ZERO;
		for (int k = 0; k <= p; k++) {
			int i = j - p + k;
			Fraction weight = ws == null ? Fraction.ONE : Fraction.of(ws[i]);
			Fraction b = basis[k].mul(weight);
			ax = ax.add(b.mul(Fraction.of(xs[i])));
			ay = ay.add(b.mul(Fraction.of(ys[i])));
			w = w.add(b);
		}
		return new Fraction[] {ax, ay, w};
	}

	/**
	 * Exact point of the piece of span {@code j} at {@code u}.
	 *
	 * @param knots knot vector
	 * @param p degree
	 * @param xs x coordinates
	 * @param ys y coordinates
	 * @param ws weights or {@code null}
	 * @param j span knot index
	 * @param u exact parameter
	 * @return {@code (x, y)}
	 */
	public static Fraction[] point(double[] knots, int p, double[] xs, double[] ys,
			double[] ws, int j, Fraction u) {
		Fraction[] h = homogeneous(knots, p, xs, ys, ws, j, u);
		return new Fraction[] {h[0].div(h[2]), h[1].div(h[2])};
	}

	/**
	 * Exact first derivative of the piece of span {@code j} at {@code u}.
	 *
	 * @param knots knot vector
	 * @param p degree, at least 1
	 * @param xs x coordinates
	 * @param ys y coordinates
	 * @param ws weights or {@code null}
	 * @param j span knot index
	 * @param u exact parameter
	 * @return {@code (x', y')}
	 */
	public static Fraction[] derivative(double[] knots, int p, double[] xs, double[] ys,
			double[] ws, int j, Fraction u) {
		Fraction[] h = homogeneous(knots, p, xs, ys, ws, j, u);
		Fraction[] d = homogeneousDerivative(knots, p, xs, ys, ws, j, u);
		Fraction w2 = h[2].mul(h[2]);
		return new Fraction[] {d[0].mul(h[2]).sub(h[0].mul(d[2])).div(w2),
				d[1].mul(h[2]).sub(h[1].mul(d[2])).div(w2)};
	}

	/**
	 * Exact Boehm insertion of {@code t} into polynomial or homogeneous data over {@code Q}.
	 *
	 * @param knots knot vector
	 * @param p degree
	 * @param cx control values, first coordinate (homogeneous when rational)
	 * @param cy control values, second coordinate
	 * @param cw control values, weight coordinate, or {@code null}
	 * @param t interior value
	 * @return new knots as fractions followed by the new control coordinates: index 0 the
	 *         knots, 1 x, 2 y, 3 w (or {@code null})
	 */
	public static Fraction[][] insert(Fraction[] knots, int p, Fraction[] cx, Fraction[] cy,
			Fraction[] cw, Fraction t) {
		int n = cx.length - 1;
		int k = -1;
		for (int i = 0; i + 1 < knots.length; i++) {
			if (knots[i].compareTo(t) <= 0 && t.compareTo(knots[i + 1]) < 0) {
				k = i;
			}
		}
		Fraction[] newKnots = new Fraction[knots.length + 1];
		for (int i = 0; i <= k; i++) {
			newKnots[i] = knots[i];
		}
		newKnots[k + 1] = t;
		for (int i = k + 1; i < knots.length; i++) {
			newKnots[i + 1] = knots[i];
		}
		Fraction[] nx = new Fraction[n + 2];
		Fraction[] ny = new Fraction[n + 2];
		Fraction[] nw = cw == null ? null : new Fraction[n + 2];
		for (int i = 0; i <= n + 1; i++) {
			if (i <= k - p) {
				nx[i] = cx[i];
				ny[i] = cy[i];
				if (nw != null) {
					nw[i] = cw[i];
				}
			} else if (i > k) {
				nx[i] = cx[i - 1];
				ny[i] = cy[i - 1];
				if (nw != null) {
					nw[i] = cw[i - 1];
				}
			} else {
				Fraction alpha = t.sub(knots[i]).div(knots[i + p].sub(knots[i]));
				Fraction beta = Fraction.ONE.sub(alpha);
				nx[i] = beta.mul(cx[i - 1]).add(alpha.mul(cx[i]));
				ny[i] = beta.mul(cy[i - 1]).add(alpha.mul(cy[i]));
				if (nw != null) {
					nw[i] = beta.mul(cw[i - 1]).add(alpha.mul(cw[i]));
				}
			}
		}
		return new Fraction[][] {newKnots, nx, ny, nw};
	}

	/**
	 * Exact value of a Bernstein polynomial by the Bernstein sum.
	 *
	 * @param coefficients Bernstein coefficients {@code b_0..b_p}
	 * @param s exact local parameter
	 * @return {@code sum_i b_i C(p,i) s^i (1-s)^{p-i}}
	 */
	public static Fraction bernsteinSum(Fraction[] coefficients, Fraction s) {
		int p = coefficients.length - 1;
		Fraction oneMinus = Fraction.ONE.sub(s);
		Fraction sum = Fraction.ZERO;
		for (int i = 0; i <= p; i++) {
			Fraction term = coefficients[i].mul(Fraction.of(binomial(p, i)));
			for (int e = 0; e < i; e++) {
				term = term.mul(s);
			}
			for (int e = 0; e < p - i; e++) {
				term = term.mul(oneMinus);
			}
			sum = sum.add(term);
		}
		return sum;
	}

	/**
	 * @param n n
	 * @param k k
	 * @return exact binomial coefficient
	 */
	public static long binomial(int n, int k) {
		long c = 1;
		for (int i = 1; i <= k; i++) {
			c = c * (n - k + i) / i;
		}
		return c;
	}

	/**
	 * Euclidean norm of an exact error vector, approximated in binary64 (for ratios only).
	 *
	 * @param ex x component
	 * @param ey y component
	 * @return approximate norm
	 */
	public static double norm(Fraction ex, Fraction ey) {
		return Math.hypot(ex.approx(), ey.approx());
	}
}
