/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.DoubleSupplier;

import org.geogebra.common.kernel.algos.AlgoCirclePointRadius;
import org.geogebra.common.kernel.algos.AlgoCircleTwoPoints;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
import org.geogebra.common.kernel.algos.AlgoJoinPoints;
import org.geogebra.common.kernel.algos.AlgoJoinPointsRay;
import org.geogebra.common.kernel.algos.AlgoJoinPointsSegment;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoMidpoint;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointLine;
import org.geogebra.common.kernel.geos.GeoAxis;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 consolidation prototype of a generic constructive-identity certificate, run only
 * through {@code tools/benchmark/pre-g9b-cu/run-cu3-constructive-identity.ps1}. The objects of a
 * ruler-and-compass construction are evaluated exactly in the field of rational functions of a
 * set of indeterminates, extended by a square root, with boundary inputs as indeterminates: in the
 * boundary scope of the author model, the ordinate of B on its line path and the non-constructible
 * point H; in the automatic scope, every free input, with points computed outside the class cut
 * as free points. Every branch, validity, membership and non-degeneracy decision is a sign
 * condition certified by outward-rounded interval evaluation over a box of the indeterminates. An
 * incidence whose residual is identically zero in the field then holds for every value of the
 * indeterminates in the box where all those sign conditions are certified, in particular for the
 * ideal values when the box encloses them; at another configuration the same proof holds again
 * when the recorded sign conditions are certified there with the same signs and the inherited
 * selection states are unchanged. It also proves or refutes statements of synthetic constructions
 * built in a fresh host. It reads copies of the author model only, assigns no verdict and changes
 * no product behaviour. It is not part of any source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-constructive-identity")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3ConstructiveIdentityProbe {

	/** Largest number of indeterminates of one evaluation (allocated in evaluation order). */
	static final int NV = 9;
	/** Exact division is attempted only below these sizes (cost control). */
	private static final int DIVISION_DIVISOR_TERMS = 24;
	private static final int DIVISION_DIVIDEND_TERMS = 6000;
	/** Bits per exponent in a packed monomial key (NV * BITS must not exceed 63). */
	private static final int BITS = 7;
	private static final int TERM_BUDGET = 200_000;
	/** Deterministic work budgets, in polynomial term products, of one evaluation. */
	private static final long INCIDENCE_BUDGET = 400_000_000L;
	private static final long ORDER_BUDGET = 400_000_000L;
	private static final double[] HALF_WIDTHS = {1e-9, 1e-6, 1e-3, 1e-1};
	/** Work done by the current evaluation, and its budget (the probe runs on one thread). */
	static long workDone;
	static long workBudget = Long.MAX_VALUE;

	private static final String[][] CONTACTS = {
			{"e_1*n at K", "J_1", "K", "n"},
			{"e_1*c_1 at K", "J_1", "K", "c_1"},
			{"o_1*h_2 at K", "K_1", "K", "h_2"},
			{"e_1*c_1 at J", "J_1", "J", "c_1"},
			{"e_1*r at J", "J_1", "J", "r"}};

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-constructive-identity");
		root.put("status", "CU3 CONSTRUCTIVE IDENTITY CERTIFICATE PROTOTYPE EVIDENCE - NO VERDICT OF ITS "
				+ "OWN");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner", "tools/benchmark/pre-g9b-cu/run-cu3-constructive-identity.ps1");
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.getBoolean("geocedg.cu0.productDirty"));
		root.put("generator", generator);
		root.put("method", method());
		AppGeoCeDG app = onEdt(() -> PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work)));
		Map<String, Evaluation> boundary = new LinkedHashMap<>();
		Map<String, Evaluation> automatic = new LinkedHashMap<>();
		root.put("mandatory_incidences_boundary_scope", onEdt(() -> incidences(app, boundary,
				false)));
		root.put("mandatory_incidences_automatic_scope", onEdt(() -> incidences(app, automatic,
				true)));
		root.put("configuration_rechecks", onEdt(() -> configurations(app, boundary, automatic)));
		root.put("adversarial_controls", onEdt(() -> controls(app, boundary)));
		root.put("contact_order", onEdt(() -> contactOrder(app)));
		root.put("synthetic_constructions", onEdt(PreG9BCu3ConstructiveIdentityProbe::synthetic));
		Files.writeString(output.resolve("cu3-constructive-identity.json"),
				PreG9BCu0References.json(root), StandardCharsets.UTF_8);
	}

	private static Map<String, Object> method() {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("boundary_scope", "indeterminates B.y (B on its constructible line path g: one "
				+ "coordinate free, the other solved from the exact equation of g), H.x and H.y (H, "
				+ "on the non-constructible line j, as a free point of the plane); every other free "
				+ "input is an exact constant at its binary64 value, recorded so that a change of it "
				+ "makes the proof not applicable");
		row.put("automatic_scope", "boundary rule with no model-specific code: a free point or "
				+ "number becomes indeterminates; a point on a constructible line path, one "
				+ "indeterminate; a point computed outside the class, or on a path that depends on "
				+ "objects outside the class (here H, on j = Line[A, C'] with C' = Rotate[C, 47 "
				+ "degrees, A]), is cut as a free point of the plane, which forgets relations and "
				+ "never invents them; the axes are constants");
		row.put("field", "rational functions over Q in the indeterminates, extended by at most one "
				+ "square root that is not a perfect square; perfect squares are extracted exactly");
		row.put("enclosures", "second-order mean-value form of every polynomial: exact rational "
				+ "values of the polynomial and of its gradient at the centre of the box, naive "
				+ "interval enclosures of the second derivatives");
		row.put("driver", "E_1 replaced by the exact driver point, itself evaluated symbolically and "
				+ "checked identically on the circle path p; for the contact order, by the exact jet "
				+ "of p at that point");
		row.put("semantics", "ideal ruler-and-compass operations; validity is mathematical (distinct "
				+ "points, nonparallel lines, positive discriminant, positive radius), never an "
				+ "inherited tolerance; selection by the captured inherited selection state");
		row.put("sign_conditions", "outward-rounded interval evaluation; every decision is taken "
				+ "on the decision box (half-width 1e-9) and recorded; a condition whose interval "
				+ "contains zero is undecided and the result is NOT_ESTABLISHED");
		row.put("rechecks", "a proof holds on another box or configuration when every recorded "
				+ "sign is certified there with the same value and the inherited selection states "
				+ "are unchanged; no new symbolic work");
		row.put("box_half_widths", List.of(1e-9, 1e-6, 1e-3, 1e-1));
		Map<String, Object> budgets = new LinkedHashMap<>();
		budgets.put("incidence", INCIDENCE_BUDGET);
		budgets.put("contact_order", ORDER_BUDGET);
		row.put("work_budgets_term_products", budgets);
		return row;
	}

	// ======================================================================== exact algebra

	/** Raised when a sign condition is not certified on the box. */
	static final class Undecided extends RuntimeException {
		private static final long serialVersionUID = 1L;

		Undecided(String condition) {
			super(condition);
		}
	}

	/** Raised when the evaluation leaves the supported class. */
	static final class OutOfClass extends RuntimeException {
		private static final long serialVersionUID = 1L;

		OutOfClass(String reason) {
			super(reason);
		}
	}

	/** Raised when a validity condition is certified false. */
	static final class Invalid extends RuntimeException {
		private static final long serialVersionUID = 1L;

		Invalid(String reason) {
			super(reason);
		}
	}

	/** Raised when an evaluation exceeds its deterministic work budget. */
	static final class Budget extends RuntimeException {
		private static final long serialVersionUID = 1L;

		Budget() {
			super("deterministic work budget exceeded");
		}
	}

	static void charge(long products) {
		workDone += products;
		if (workDone > workBudget) {
			throw new Budget();
		}
	}

	/** Exact rational, reduced, positive denominator. */
	static final class Q {
		static final Q ZERO = new Q(BigInteger.ZERO, BigInteger.ONE);
		static final Q ONE = new Q(BigInteger.ONE, BigInteger.ONE);
		final BigInteger n;
		final BigInteger d;

		private Q(BigInteger n, BigInteger d) {
			this.n = n;
			this.d = d;
		}

		static Q of(BigInteger n, BigInteger d) {
			if (d.signum() == 0) {
				throw new ArithmeticException("zero denominator");
			}
			if (n.signum() == 0) {
				return ZERO;
			}
			if (d.signum() < 0) {
				n = n.negate();
				d = d.negate();
			}
			BigInteger g = n.gcd(d);
			return new Q(n.divide(g), d.divide(g));
		}

		static Q of(long value) {
			return of(BigInteger.valueOf(value), BigInteger.ONE);
		}

		static Q of(double value) {
			if (!Double.isFinite(value)) {
				throw new OutOfClass("nonfinite binary64 value");
			}
			BigDecimal exact = new BigDecimal(value);
			int scale = exact.scale();
			return scale >= 0 ? of(exact.unscaledValue(), BigInteger.TEN.pow(scale))
					: of(exact.unscaledValue().multiply(BigInteger.TEN.pow(-scale)), BigInteger.ONE);
		}

		Q add(Q o) {
			return of(n.multiply(o.d).add(o.n.multiply(d)), d.multiply(o.d));
		}

		Q mul(Q o) {
			return of(n.multiply(o.n), d.multiply(o.d));
		}

		Q div(Q o) {
			return of(n.multiply(o.d), d.multiply(o.n));
		}

		Q neg() {
			return n.signum() == 0 ? this : new Q(n.negate(), d);
		}

		boolean isZero() {
			return n.signum() == 0;
		}

		Q sqrt() {
			if (n.signum() < 0) {
				return null;
			}
			BigInteger rn = n.sqrt();
			BigInteger rd = d.sqrt();
			return rn.multiply(rn).equals(n) && rd.multiply(rd).equals(d) ? of(rn, rd) : null;
		}

		Iv iv() {
			double value = new BigDecimal(n).divide(new BigDecimal(d),
					new MathContext(40, RoundingMode.HALF_EVEN)).doubleValue();
			return new Iv(Math.nextDown(value), Math.nextUp(value));
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Q && n.equals(((Q) other).n) && d.equals(((Q) other).d);
		}

		@Override
		public int hashCode() {
			return n.hashCode() * 31 + d.hashCode();
		}
	}

	/** Outward-rounded binary64 interval. */
	static final class Iv {
		final double lo;
		final double hi;

		Iv(double lo, double hi) {
			this.lo = lo;
			this.hi = hi;
		}

		Iv add(Iv o) {
			return new Iv(Math.nextDown(lo + o.lo), Math.nextUp(hi + o.hi));
		}

		Iv mul(Iv o) {
			double a = lo * o.lo;
			double b = lo * o.hi;
			double c = hi * o.lo;
			double d = hi * o.hi;
			return new Iv(Math.nextDown(Math.min(Math.min(a, b), Math.min(c, d))),
					Math.nextUp(Math.max(Math.max(a, b), Math.max(c, d))));
		}

		Iv div(Iv o) {
			if (o.lo <= 0 && o.hi >= 0) {
				throw new Undecided("a divisor interval contains zero");
			}
			return mul(new Iv(Math.nextDown(1 / o.hi), Math.nextUp(1 / o.lo)));
		}

		Iv sqrt() {
			if (lo < 0) {
				throw new Undecided("a square-root argument interval reaches below zero");
			}
			return new Iv(Math.max(0, Math.nextDown(Math.sqrt(lo))), Math.nextUp(Math.sqrt(hi)));
		}

		int sign() {
			return lo > 0 ? 1 : hi < 0 ? -1 : 0;
		}
	}

	static int exponent(long key, int i) {
		return (int) ((key >>> (BITS * i)) & ((1 << BITS) - 1));
	}

	/** Product of two monomials, refusing an exponent beyond the key width. */
	static long addKeys(long a, long b) {
		for (int i = 0; i < NV; i++) {
			if (exponent(a, i) + exponent(b, i) >= 1 << BITS) {
				throw new OutOfClass("exponent of indeterminate " + i + " beyond the key width");
			}
		}
		return a + b;
	}

	static int degree(long key) {
		int total = 0;
		for (int i = 0; i < NV; i++) {
			total += exponent(key, i);
		}
		return total;
	}

	/** Descending graded lexicographic order: the first key is the leading monomial. */
	static final Comparator<Long> ORDER = (a, b) -> {
		int da = degree(a);
		int db = degree(b);
		if (da != db) {
			return Integer.compare(db, da);
		}
		for (int i = 0; i < NV; i++) {
			int ea = exponent(a, i);
			int eb = exponent(b, i);
			if (ea != eb) {
				return Integer.compare(eb, ea);
			}
		}
		return 0;
	};

	/** Polynomial with exact rational coefficients in the indeterminates. */
	static final class Poly {
		final TreeMap<Long, Q> terms;

		private Poly(TreeMap<Long, Q> terms) {
			this.terms = terms;
			if (terms.size() > TERM_BUDGET) {
				throw new OutOfClass("polynomial term budget exceeded");
			}
		}

		static Poly constant(Q value) {
			TreeMap<Long, Q> terms = new TreeMap<>(ORDER);
			if (!value.isZero()) {
				terms.put(0L, value);
			}
			return new Poly(terms);
		}

		static Poly variable(int index) {
			TreeMap<Long, Q> terms = new TreeMap<>(ORDER);
			terms.put(1L << (BITS * index), Q.ONE);
			return new Poly(terms);
		}

		boolean isZero() {
			return terms.isEmpty();
		}

		boolean isConstant() {
			return terms.isEmpty() || terms.size() == 1 && terms.firstKey() == 0L;
		}

		Q constantValue() {
			return terms.isEmpty() ? Q.ZERO : terms.get(0L);
		}

		Poly add(Poly o) {
			TreeMap<Long, Q> sum = new TreeMap<>(terms);
			for (Map.Entry<Long, Q> term : o.terms.entrySet()) {
				Q previous = sum.get(term.getKey());
				Q next = previous == null ? term.getValue() : previous.add(term.getValue());
				if (next.isZero()) {
					sum.remove(term.getKey());
				} else {
					sum.put(term.getKey(), next);
				}
			}
			return new Poly(sum);
		}

		Poly neg() {
			TreeMap<Long, Q> negated = new TreeMap<>(ORDER);
			for (Map.Entry<Long, Q> term : terms.entrySet()) {
				negated.put(term.getKey(), term.getValue().neg());
			}
			return new Poly(negated);
		}

		Poly sub(Poly o) {
			return add(o.neg());
		}

		Poly scale(Q factor) {
			if (factor.isZero()) {
				return constant(Q.ZERO);
			}
			TreeMap<Long, Q> scaled = new TreeMap<>(ORDER);
			for (Map.Entry<Long, Q> term : terms.entrySet()) {
				scaled.put(term.getKey(), term.getValue().mul(factor));
			}
			return new Poly(scaled);
		}

		Poly mul(Poly o) {
			charge((long) terms.size() * o.terms.size());
			if (!terms.isEmpty() && !o.terms.isEmpty()) {
				for (int i = 0; i < NV; i++) {
					int top = 0;
					for (long key : terms.keySet()) {
						top = Math.max(top, exponent(key, i));
					}
					int other = 0;
					for (long key : o.terms.keySet()) {
						other = Math.max(other, exponent(key, i));
					}
					if (top + other >= 1 << BITS) {
						throw new OutOfClass("exponent of indeterminate " + i + " beyond the key "
								+ "width");
					}
				}
			}
			TreeMap<Long, Q> product = new TreeMap<>(ORDER);
			for (Map.Entry<Long, Q> left : terms.entrySet()) {
				for (Map.Entry<Long, Q> right : o.terms.entrySet()) {
					long key = left.getKey() + right.getKey();
					Q value = left.getValue().mul(right.getValue());
					Q previous = product.get(key);
					Q next = previous == null ? value : previous.add(value);
					if (next.isZero()) {
						product.remove(key);
					} else {
						product.put(key, next);
					}
				}
			}
			return new Poly(product);
		}

		boolean equalsPoly(Poly o) {
			return terms.equals(o.terms);
		}

		int maxDegree() {
			return terms.isEmpty() ? 0 : degree(terms.firstKey());
		}

		/**
		 * Second-order mean-value enclosure on a box: the exact rational value at the centre of the
		 * box plus the enclosures of the partial derivatives, themselves mean-value enclosures with
		 * naive second derivatives, times the half-widths. An expanded polynomial evaluated naively
		 * loses its sign to cancellation; the exact centre values do not.
		 */
		Iv eval(Iv[] box) {
			return eval(box, 2);
		}

		private Iv eval(Iv[] box, int depth) {
			if (depth == 0) {
				return evalNaive(box);
			}
			if (terms.isEmpty()) {
				return new Iv(0, 0);
			}
			Q[] centre = new Q[NV];
			Q half = Q.of(BigInteger.ONE, BigInteger.TWO);
			Q sum = Q.ZERO;
			for (Map.Entry<Long, Q> term : terms.entrySet()) {
				Q product = term.getValue();
				for (int i = 0; i < NV; i++) {
					int e = exponent(term.getKey(), i);
					if (e > 0 && centre[i] == null) {
						centre[i] = Q.of(box[i].lo).add(Q.of(box[i].hi)).mul(half);
					}
					for (int k = 0; k < e; k++) {
						product = product.mul(centre[i]);
					}
				}
				sum = sum.add(product);
			}
			Iv value = sum.iv();
			for (int i = 0; i < NV; i++) {
				if (centre[i] == null) {
					continue;
				}
				double radius = Q.of(box[i].hi).add(Q.of(box[i].lo).neg()).mul(half).iv().hi;
				value = value.add(partial(i).eval(box, depth - 1).mul(new Iv(-radius, radius)));
			}
			return value;
		}

		/** Naive interval evaluation, term by term. */
		Iv evalNaive(Iv[] box) {
			Iv sum = new Iv(0, 0);
			for (Map.Entry<Long, Q> term : terms.entrySet()) {
				Iv value = term.getValue().iv();
				for (int i = 0; i < NV; i++) {
					for (int e = 0; e < exponent(term.getKey(), i); e++) {
						value = value.mul(box[i]);
					}
				}
				sum = sum.add(value);
			}
			return sum;
		}

		private Poly[] partials;

		/** The partial derivative in one indeterminate, computed once (the polynomial is fixed). */
		Poly partial(int i) {
			if (partials == null) {
				partials = new Poly[NV];
			}
			if (partials[i] == null) {
				TreeMap<Long, Q> derivative = new TreeMap<>(ORDER);
				for (Map.Entry<Long, Q> term : terms.entrySet()) {
					int e = exponent(term.getKey(), i);
					if (e > 0) {
						derivative.put(term.getKey() - (1L << (BITS * i)), term.getValue().mul(Q.of(e)));
					}
				}
				partials[i] = new Poly(derivative);
			}
			return partials[i];
		}

		/** Exact quotient when {@code divisor} divides this polynomial, else null. */
		Poly divideExact(Poly divisor) {
			if (divisor.isZero() || divisor.terms.size() > DIVISION_DIVISOR_TERMS
					|| terms.size() > DIVISION_DIVIDEND_TERMS) {
				return null;
			}
			TreeMap<Long, Q> remainder = new TreeMap<>(terms);
			TreeMap<Long, Q> quotient = new TreeMap<>(ORDER);
			long lead = divisor.terms.firstKey();
			Q leadValue = divisor.terms.get(lead);
			while (!remainder.isEmpty()) {
				if (quotient.size() > DIVISION_DIVIDEND_TERMS) {
					return null;
				}
				long key = remainder.firstKey();
				long q = 0;
				for (int i = 0; i < NV; i++) {
					int e = exponent(key, i) - exponent(lead, i);
					if (e < 0) {
						return null;
					}
					q |= ((long) e) << (BITS * i);
				}
				Q factor = remainder.get(key).div(leadValue);
				quotient.put(q, factor);
				charge(divisor.terms.size());
				// Subtract factor * x^q * divisor in place; the leading term cancels exactly.
				for (Map.Entry<Long, Q> term : divisor.terms.entrySet()) {
					long k = addKeys(q, term.getKey());
					Q previous = remainder.get(k);
					Q next = (previous == null ? Q.ZERO : previous).add(factor.mul(term.getValue())
							.neg());
					if (next.isZero()) {
						remainder.remove(k);
					} else {
						remainder.put(k, next);
					}
				}
			}
			return new Poly(quotient);
		}

		/** Exact square root of a perfect square, else null (positive leading coefficient). */
		static Poly sqrt(Poly p) {
			if (p.isZero()) {
				return p;
			}
			long lead = p.terms.firstKey();
			Q leadValue = p.terms.get(lead);
			long half = 0;
			for (int i = 0; i < NV; i++) {
				int e = exponent(lead, i);
				if (e % 2 != 0) {
					return null;
				}
				half |= ((long) (e / 2)) << (BITS * i);
			}
			Q rootValue = leadValue.sqrt();
			if (rootValue == null) {
				return null;
			}
			TreeMap<Long, Q> first = new TreeMap<>(ORDER);
			first.put(half, rootValue);
			Poly root = new Poly(first);
			Poly remainder = p.sub(root.mul(root));
			Q twice = rootValue.add(rootValue);
			for (int guard = 0; !remainder.isZero(); guard++) {
				if (guard > 4096) {
					return null;
				}
				long key = remainder.terms.firstKey();
				long t = 0;
				for (int i = 0; i < NV; i++) {
					int e = exponent(key, i) - exponent(half, i);
					if (e < 0) {
						return null;
					}
					t |= ((long) e) << (BITS * i);
				}
				if (ORDER.compare(t, half) <= 0) {
					return null;
				}
				TreeMap<Long, Q> single = new TreeMap<>(ORDER);
				single.put(t, remainder.terms.get(key).div(twice));
				Poly term = new Poly(single);
				remainder = remainder.sub(root.mul(term).scale(Q.of(2))).sub(term.mul(term));
				root = root.add(term);
			}
			return root;
		}
	}

	/** Rational function num / den. */
	static final class RF {
		final Poly num;
		final Poly den;

		RF(Poly num, Poly den) {
			if (den.isZero()) {
				throw new ArithmeticException("zero denominator");
			}
			if (num.isZero()) {
				this.num = num;
				this.den = Poly.constant(Q.ONE);
				return;
			}
			if (den.isConstant()) {
				this.num = num.scale(Q.ONE.div(den.constantValue()));
				this.den = Poly.constant(Q.ONE);
				return;
			}
			Poly quotient = num.divideExact(den);
			if (quotient != null) {
				this.num = quotient;
				this.den = Poly.constant(Q.ONE);
				return;
			}
			Q lead = den.terms.get(den.terms.firstKey());
			this.num = num.scale(Q.ONE.div(lead));
			this.den = den.scale(Q.ONE.div(lead));
		}

		static RF of(Poly p) {
			return new RF(p, Poly.constant(Q.ONE));
		}

		boolean isZero() {
			return num.isZero();
		}

		RF add(RF o) {
			if (den.equalsPoly(o.den)) {
				return new RF(num.add(o.num), den);
			}
			Poly factor = o.den.divideExact(den);
			if (factor != null) {
				return new RF(num.mul(factor).add(o.num), o.den);
			}
			factor = den.divideExact(o.den);
			if (factor != null) {
				return new RF(num.add(o.num.mul(factor)), den);
			}
			return new RF(num.mul(o.den).add(o.num.mul(den)), den.mul(o.den));
		}

		RF neg() {
			return new RF(num.neg(), den);
		}

		RF sub(RF o) {
			return add(o.neg());
		}

		RF mul(RF o) {
			return new RF(num.mul(o.num), den.mul(o.den));
		}

		RF div(RF o) {
			if (o.isZero()) {
				throw new Invalid("division by an identically zero value");
			}
			return new RF(num.mul(o.den), den.mul(o.num));
		}

		Iv eval(Iv[] box) {
			return num.eval(box).div(den.eval(box));
		}

		int size() {
			return num.terms.size() + den.terms.size();
		}
	}

	/** A sign condition decided during the evaluation, kept for other boxes. */
	static final class Check {
		final String what;
		final Ext element;
		final int sign;

		Check(String what, Ext element, int sign) {
			this.what = what;
			this.element = element;
			this.sign = sign;
		}
	}

	/** A boundary coordinate of a proof: an indeterminate (index >= 0) or a constant. */
	static final class Coordinate {
		final String name;
		final DoubleSupplier live;
		final int index;
		final double value;

		Coordinate(String name, DoubleSupplier live, int index, double value) {
			this.name = name;
			this.live = live;
			this.index = index;
			this.value = value;
		}
	}

	static Iv interval(double centre, double halfWidth) {
		return new Iv(Math.nextDown(centre - halfWidth), Math.nextUp(centre + halfWidth));
	}

	/** The arithmetic context of one evaluation: box, extension and recorded conditions. */
	static final class Field {
		final double halfWidth;
		final Iv[] box = new Iv[NV];
		final List<Coordinate> coordinates = new ArrayList<>();
		final List<String> cuts = new ArrayList<>();
		int variables;
		Poly delta;
		final List<Check> checks = new ArrayList<>();
		int divisions;
		int maxSize;

		Field(double halfWidth) {
			this.halfWidth = halfWidth;
		}

		/** A new indeterminate centred on the live value of a boundary coordinate. */
		Ext indeterminate(String name, DoubleSupplier live) {
			if (variables == NV) {
				throw new OutOfClass("more than " + NV + " indeterminates");
			}
			int index = variables++;
			double value = live.getAsDouble();
			box[index] = interval(value, halfWidth);
			coordinates.add(new Coordinate(name, live, index, value));
			return variable(index);
		}

		/** A boundary coordinate kept as an exact constant: a change of it needs a new proof. */
		Ext constant(String name, DoubleSupplier live, Q exact) {
			coordinates.add(new Coordinate(name, live, -1, live.getAsDouble()));
			return constant(exact);
		}

		List<String> names(boolean indeterminates) {
			List<String> names = new ArrayList<>();
			for (Coordinate coordinate : coordinates) {
				if ((coordinate.index >= 0) == indeterminates) {
					names.add(coordinate.name);
				}
			}
			return names;
		}

		/**
		 * Every sign decided on the decision box, re-certified on another box: the evaluation is
		 * valid on that box only if every sign is certified there with the same value.
		 *
		 * @return null when all are certified, else the first condition that is not
		 */
		String recheck(Iv[] other) {
			for (Check check : checks) {
				int s;
				try {
					s = check.element.eval(other).sign();
				} catch (Undecided undecided) {
					s = 0;
				}
				if (s != check.sign) {
					return check.what;
				}
			}
			return null;
		}

		Ext constant(Q value) {
			return new Ext(this, RF.of(Poly.constant(value)), RF.of(Poly.constant(Q.ZERO)));
		}

		Ext variable(int index) {
			return new Ext(this, RF.of(Poly.variable(index)), RF.of(Poly.constant(Q.ZERO)));
		}

		/**
		 * The square root of a value num / den of the base field, positive on the box, written as
		 * sqrt(s) / scale: s = num and scale = sqrt(den) when den is a perfect square, else
		 * s = num * den and scale = den.
		 */
		Ext sqrtBase(RF value, String what) {
			Poly denRoot = Poly.sqrt(value.den);
			Poly s = denRoot != null ? value.num : value.num.mul(value.den);
			Poly scale = denRoot != null ? denRoot : value.den;
			Poly root = Poly.sqrt(s);
			Ext candidate;
			if (root != null) {
				candidate = new Ext(this, new RF(root, scale), RF.of(Poly.constant(Q.ZERO)));
			} else if (delta == null) {
				delta = s;
				candidate = new Ext(this, RF.of(Poly.constant(Q.ZERO)),
						new RF(Poly.constant(Q.ONE), scale));
			} else {
				Poly joint = Poly.sqrt(s.mul(delta));
				if (joint == null) {
					throw new OutOfClass("a second independent square root (" + what + ")");
				}
				candidate = new Ext(this, RF.of(Poly.constant(Q.ZERO)),
						new RF(joint, delta.mul(scale)));
			}
			return candidate.sign(what + ": branch of the square root") > 0 ? candidate
					: candidate.neg();
		}

		void track(Ext value) {
			maxSize = Math.max(maxSize, value.a.size() + value.b.size());
		}
	}

	/** Element a + b sqrt(delta) of the field. */
	static final class Ext {
		final Field f;
		final RF a;
		final RF b;

		Ext(Field f, RF a, RF b) {
			this.f = f;
			this.a = a;
			this.b = b;
			f.track(this);
		}

		boolean isZero() {
			return a.isZero() && b.isZero();
		}

		Ext add(Ext o) {
			return new Ext(f, a.add(o.a), b.add(o.b));
		}

		Ext neg() {
			return new Ext(f, a.neg(), b.neg());
		}

		Ext sub(Ext o) {
			return add(o.neg());
		}

		Ext mul(Ext o) {
			if (b.isZero() && o.b.isZero()) {
				return new Ext(f, a.mul(o.a), b);
			}
			RF d = RF.of(f.delta);
			return new Ext(f, a.mul(o.a).add(b.mul(o.b).mul(d)), a.mul(o.b).add(b.mul(o.a)));
		}

		Ext div(Ext o, String what) {
			if (o.isZero()) {
				throw new Invalid("division by zero: " + what);
			}
			f.divisions++;
			o.signQuiet(what + ": divisor nonzero");
			if (o.b.isZero()) {
				return new Ext(f, a.div(o.a), b.div(o.a));
			}
			RF d = RF.of(f.delta);
			RF norm = o.a.mul(o.a).sub(o.b.mul(o.b).mul(d));
			Ext inverse = new Ext(f, o.a.div(norm), o.b.neg().div(norm));
			return mul(inverse);
		}

		Iv eval() {
			return eval(f.box);
		}

		Iv eval(Iv[] box) {
			Iv value = a.eval(box);
			if (!b.isZero()) {
				value = value.add(b.eval(box).mul(f.delta.eval(box).sqrt()));
			}
			return value;
		}

		/** Certified sign on the box, recorded as a precondition. */
		int sign(String what) {
			int s = signOn(what);
			f.checks.add(new Check(what, this, s));
			return s;
		}

		private void signQuiet(String what) {
			f.checks.add(new Check(what, this, signOn(what)));
		}

		private int signOn(String what) {
			int s;
			try {
				s = eval().sign();
			} catch (Undecided undecided) {
				throw new Undecided(what + " (" + undecided.getMessage() + ")");
			}
			if (s == 0) {
				throw new Undecided(what);
			}
			return s;
		}
	}

	/** Value with its first two derivatives in the driver parameter (null derivative = 0). */
	static final class SJ {
		final Ext v;
		final Ext d1;
		final Ext d2;

		SJ(Ext v, Ext d1, Ext d2) {
			this.v = v;
			this.d1 = d1;
			this.d2 = d2;
		}

		private Ext first() {
			return d1 != null ? d1 : v.f.constant(Q.ZERO);
		}

		private Ext second() {
			return d2 != null ? d2 : v.f.constant(Q.ZERO);
		}

		private boolean any(SJ o) {
			return d1 != null || d2 != null || o.d1 != null || o.d2 != null;
		}

		SJ add(SJ o) {
			return any(o) ? new SJ(v.add(o.v), first().add(o.first()), second().add(o.second()))
					: new SJ(v.add(o.v), null, null);
		}

		SJ neg() {
			return new SJ(v.neg(), d1 == null ? null : d1.neg(), d2 == null ? null : d2.neg());
		}

		SJ sub(SJ o) {
			return add(o.neg());
		}

		SJ mul(SJ o) {
			if (!any(o)) {
				return new SJ(v.mul(o.v), null, null);
			}
			Ext one = first().mul(o.v).add(v.mul(o.first()));
			Ext two = second().mul(o.v).add(first().mul(o.first()).mul(v.f.constant(Q.of(2))))
					.add(v.mul(o.second()));
			return new SJ(v.mul(o.v), one, two);
		}

		SJ square() {
			return mul(this);
		}

		SJ div(SJ o, String what) {
			Ext quotient = v.div(o.v, what);
			if (!any(o)) {
				return new SJ(quotient, null, null);
			}
			Ext one = first().sub(quotient.mul(o.first())).div(o.v, what);
			Ext two = second().sub(one.mul(o.first()).mul(v.f.constant(Q.of(2))))
					.sub(quotient.mul(o.second())).div(o.v, what);
			return new SJ(quotient, one, two);
		}

		SJ sqrt(String what) {
			if (!v.b.isZero()) {
				throw new OutOfClass("nested square root (" + what + ")");
			}
			Ext root = v.f.sqrtBase(v.a, what);
			if (d1 == null && d2 == null) {
				return new SJ(root, null, null);
			}
			Ext twice = root.add(root);
			Ext one = first().div(twice, what);
			Ext two = second().sub(one.mul(one).mul(v.f.constant(Q.of(2)))).div(twice, what);
			return new SJ(root, one, two);
		}
	}

	// ======================================================================== geometry

	static final class SPt {
		final SJ x;
		final SJ y;

		SPt(SJ x, SJ y) {
			this.x = x;
			this.y = y;
		}
	}

	static final class SLine {
		final SJ a;
		final SJ b;
		final SJ c;
		final boolean bounded;
		final boolean ray;
		final SJ sx;
		final SJ sy;

		SLine(SJ a, SJ b, SJ c, boolean bounded, boolean ray, SJ sx, SJ sy) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.bounded = bounded;
			this.ray = ray;
			this.sx = sx;
			this.sy = sy;
		}
	}

	static final class SCircle {
		final SJ mx;
		final SJ my;
		final SJ r2;

		SCircle(SJ mx, SJ my, SJ r2) {
			this.mx = mx;
			this.my = my;
			this.r2 = r2;
		}
	}

	/**
	 * Ideal constructive evaluation of a GeoGebra construction in the symbolic field. With the
	 * automatic boundary rule, a free point or number becomes indeterminates, a point on a line
	 * path of the class one indeterminate (its other coordinate solved from the exact line), and a
	 * point computed outside the class is cut: it becomes a free point of the plane, which forgets
	 * its relations to its own inputs, so an identity found with the cut also holds without it.
	 */
	static final class Sym {
		final Field f;
		final boolean automatic;
		final Map<GeoElement, Object> values = new IdentityHashMap<>();
		/** The inherited selection state of every intersection the evaluation used, in order. */
		final Map<AlgoIntersectLineConic, String> selections = new IdentityHashMap<>();
		final List<AlgoIntersectLineConic> selectionOrder = new ArrayList<>();

		Sym(Field f, boolean automatic) {
			this.f = f;
			this.automatic = automatic;
		}

		SJ unknown(String name, DoubleSupplier live) {
			return new SJ(f.indeterminate(name, live), null, null);
		}

		SPt freePoint(GeoPoint p) {
			String label = p.getLabelSimple();
			return new SPt(unknown(label + ".x", p::getInhomX), unknown(label + ".y", p::getInhomY));
		}

		/**
		 * A point on a constructible line path: one coordinate free, the other solved from the
		 * exact line. On any other path the point itself is cut: a line through a cut point is an
		 * arbitrary line, so keeping the point on it adds indeterminates and no information.
		 */
		SPt pathPoint(GeoPoint p) {
			String label = p.getLabelSimple();
			GeoElement path = p.getPath().toGeoElement();
			if (!(path instanceof GeoLine) || !constructible(path)) {
				f.cuts.add(label + ": point on a path " + (path instanceof GeoLine
						? "that depends on objects outside the class" : "that is not a line")
						+ ", cut as a free point");
				return freePoint(p);
			}
			SLine l = line(path);
			GeoLine live = (GeoLine) path;
			SPt point;
			if (Math.abs(live.getY()) >= Math.abs(live.getX())) {
				SJ x = unknown(label + ".x", p::getInhomX);
				point = new SPt(x, l.a.mul(x).add(l.c).neg().div(l.b, label
						+ ": ordinate on its line path"));
			} else {
				SJ y = unknown(label + ".y", p::getInhomY);
				point = new SPt(l.b.mul(y).add(l.c).neg().div(l.a, label
						+ ": abscissa on its line path"), y);
			}
			if (!member(l, point, label + ": on its limited path")) {
				throw new Invalid(label + " outside its limited path");
			}
			return point;
		}

		SJ k(Q value) {
			return new SJ(f.constant(value), null, null);
		}

		SJ k(double value) {
			return k(Q.of(value));
		}

		void positive(SJ value, String what) {
			if (value.v.isZero()) {
				throw new Invalid(what + " is identically zero");
			}
			if (value.v.sign(what) < 0) {
				throw new Invalid(what + " is negative");
			}
		}

		SLine join(SPt p, SPt q, boolean bounded, boolean ray, String what) {
			SJ a = p.y.sub(q.y);
			SJ b = q.x.sub(p.x);
			SJ c = p.x.mul(q.y).sub(p.y.mul(q.x));
			positive(a.square().add(b.square()), what + ": distinct points");
			return new SLine(a, b, c, bounded, ray, bounded ? p.x : null, bounded ? p.y : null);
		}

		SLine parallel(SPt p, SLine l) {
			return new SLine(l.a, l.b, l.a.mul(p.x).add(l.b.mul(p.y)).neg(), false, false, null,
					null);
		}

		SLine perpendicular(SPt p, SLine l) {
			return new SLine(l.b.neg(), l.a, p.x.mul(l.b).sub(p.y.mul(l.a)), false, false, null,
					null);
		}

		SPt meet(SLine g, SLine h, String what) {
			SJ x = g.b.mul(h.c).sub(g.c.mul(h.b));
			SJ y = g.c.mul(h.a).sub(g.a.mul(h.c));
			SJ z = g.a.mul(h.b).sub(g.b.mul(h.a));
			if (z.v.isZero()) {
				throw new Invalid(what + ": parallel lines");
			}
			z.v.sign(what + ": lines not parallel");
			SPt point = new SPt(x.div(z, what), y.div(z, what));
			member(g, point, what);
			member(h, point, what);
			return point;
		}

		SCircle through(SPt c, SPt p, String what) {
			SJ r2 = p.x.sub(c.x).square().add(p.y.sub(c.y).square());
			positive(r2, what + ": positive radius");
			return new SCircle(c.x, c.y, r2);
		}

		/** Membership of a point in a bounded line; true for an unbounded line. */
		boolean member(SLine l, SPt p, String what) {
			if (!l.bounded) {
				return true;
			}
			SJ parameter = p.x.sub(l.sx).mul(l.b).sub(p.y.sub(l.sy).mul(l.a));
			if (!parameter.v.isZero() && parameter.v.sign(what + ": parameter on the "
					+ (l.ray ? "ray" : "segment")) < 0) {
				return false;
			}
			if (l.ray) {
				return true;
			}
			SJ rest = l.a.square().add(l.b.square()).sub(parameter);
			return rest.v.isZero() || rest.v.sign(what + ": parameter before the segment end") > 0;
		}

		/** The two intersections of the support line of l with c, in the raw root order. */
		SPt[] rawRoots(SLine l, SCircle c, String what) {
			SJ norm = l.a.square().add(l.b.square());
			SJ dist = l.a.mul(c.mx).add(l.b.mul(c.my)).add(l.c);
			SJ disc = c.r2.mul(norm).sub(dist.square());
			positive(disc, what + ": positive discriminant");
			SJ root = disc.sqrt(what);
			SJ footX = c.mx.sub(dist.mul(l.a).div(norm, what));
			SJ footY = c.my.sub(dist.mul(l.b).div(norm, what));
			SJ offX = root.mul(l.b).div(norm, what);
			SJ offY = root.mul(l.a).div(norm, what).neg();
			return new SPt[] {new SPt(footX.sub(offX), footY.sub(offY)),
					new SPt(footX.add(offX), footY.add(offY))};
		}

		SPt lineCircle(SLine l, SCircle c, boolean compaction, int[] permutation, int index,
				String what) {
			SPt[] raw = rawRoots(l, c, what);
			int selected;
			if (compaction) {
				selected = -1;
				int count = 0;
				for (int r = 0; r < 2 && selected < 0; r++) {
					if (member(l, raw[r], what + ": raw root " + r)) {
						if (count == index) {
							selected = r;
						}
						count++;
					}
				}
				if (selected < 0) {
					throw new Invalid(what + ": fewer roots on the path than the index");
				}
			} else {
				selected = permutation[index];
				if (!member(l, raw[selected], what + ": selected root")) {
					throw new Invalid(what + ": the selected root is not on the path");
				}
			}
			return raw[selected];
		}

		private final Map<GeoElement, Boolean> constructible = new IdentityHashMap<>();

		/** Whether the object is defined from free inputs by operations of the class only. */
		boolean constructible(GeoElement geo) {
			Boolean known = constructible.get(geo);
			if (known != null) {
				return known;
			}
			AlgoElement parent = geo.getParentAlgorithm();
			boolean result;
			if (parent == null) {
				result = geo instanceof GeoPoint || geo instanceof GeoNumeric || geo instanceof GeoAxis;
			} else if (geo instanceof GeoPoint && ((GeoPoint) geo).isPointOnPath()) {
				GeoElement path = ((GeoPoint) geo).getPath().toGeoElement();
				result = path instanceof GeoLine && constructible(path);
			} else if (parent instanceof AlgoIntersectSingle) {
				AlgoElement source = ((AlgoIntersectSingle) parent).getSourceIntersection();
				result = source instanceof AlgoIntersectLineConic && constructible(
						(AlgoIntersectLineConic) source);
			} else if (parent instanceof AlgoIntersectLineConic) {
				result = constructible((AlgoIntersectLineConic) parent);
			} else if (parent instanceof AlgoJoinPoints || parent instanceof AlgoJoinPointsSegment
					|| parent instanceof AlgoJoinPointsRay || parent instanceof AlgoLinePointLine
					|| parent instanceof AlgoOrthoLinePointLine || parent instanceof AlgoIntersectLines
					|| parent instanceof AlgoMidpoint || parent instanceof AlgoCircleTwoPoints
					|| parent instanceof AlgoCirclePointRadius) {
				result = true;
				for (int i = 0; i < parent.getInputLength() && result; i++) {
					result = constructible(parent.getInput(i).toGeoElement());
				}
			} else {
				result = false;
			}
			constructible.put(geo, result);
			return result;
		}

		private boolean constructible(AlgoIntersectLineConic algorithm) {
			return algorithm.getConic() instanceof GeoConic
					&& ((GeoConic) algorithm.getConic()).isCircle()
					&& constructible(algorithm.getLine().toGeoElement())
					&& constructible(((GeoConic) algorithm.getConic()).toGeoElement());
		}

		Object eval(GeoElement geo) {
			Object known = values.get(geo);
			if (known != null) {
				return known;
			}
			Object value = compute(geo);
			values.put(geo, value);
			return value;
		}

		SPt point(GeoElement geo) {
			return (SPt) eval(geo);
		}

		SLine line(GeoElement geo) {
			return (SLine) eval(geo);
		}

		SCircle circle(GeoElement geo) {
			return (SCircle) eval(geo);
		}

		private Object compute(GeoElement geo) {
			String label = geo.getLabelSimple();
			AlgoElement parent = geo.getParentAlgorithm();
			if (parent == null) {
				if (geo instanceof GeoPoint) {
					GeoPoint p = (GeoPoint) geo;
					if (automatic) {
						return freePoint(p);
					}
					Q z = Q.of(p.getZ());
					return new SPt(new SJ(f.constant(label + ".x", p::getInhomX, Q.of(p.getX()).div(z)),
							null, null), new SJ(f.constant(label + ".y", p::getInhomY,
									Q.of(p.getY()).div(z)), null, null));
				}
				if (geo instanceof GeoAxis) {
					GeoLine l = (GeoLine) geo;
					return new SLine(k(l.getX()), k(l.getY()), k(l.getZ()), false, false, null, null);
				}
				if (geo instanceof GeoNumeric) {
					GeoNumeric number = (GeoNumeric) geo;
					return automatic ? unknown(label, number::getValue)
							: new SJ(f.constant(label, number::getValue, Q.of(number.getValue())),
									null, null);
				}
				throw new OutOfClass("free object " + label);
			}
			if (geo instanceof GeoPoint && ((GeoPoint) geo).isPointOnPath()) {
				if (!automatic) {
					throw new OutOfClass("point on a path without an indeterminate: " + label);
				}
				return pathPoint((GeoPoint) geo);
			}
			if (parent instanceof AlgoJoinPointsSegment) {
				AlgoJoinPointsSegment join = (AlgoJoinPointsSegment) parent;
				return join(point(join.getP()), point(join.getQ()), true, false, label);
			}
			if (parent instanceof AlgoJoinPointsRay) {
				AlgoJoinPointsRay join = (AlgoJoinPointsRay) parent;
				return join(point(join.getP()), point(join.getQ()), true, true, label);
			}
			if (parent instanceof AlgoJoinPoints) {
				AlgoJoinPoints join = (AlgoJoinPoints) parent;
				return join(point(join.getP()), point(join.getQ()), false, false, label);
			}
			if (parent instanceof AlgoLinePointLine) {
				AlgoLinePointLine parallel = (AlgoLinePointLine) parent;
				return parallel(point(parallel.getP()), line(parallel.getInput(1).toGeoElement()));
			}
			if (parent instanceof AlgoOrthoLinePointLine) {
				AlgoOrthoLinePointLine orthogonal = (AlgoOrthoLinePointLine) parent;
				return perpendicular(point(orthogonal.getP()),
						line(orthogonal.getInput(1).toGeoElement()));
			}
			if (parent instanceof AlgoIntersectLines) {
				AlgoIntersectLines meet = (AlgoIntersectLines) parent;
				return meet(line(meet.getg()), line(meet.geth()), label);
			}
			if (parent instanceof AlgoMidpoint) {
				AlgoMidpoint midpoint = (AlgoMidpoint) parent;
				SPt p = point(midpoint.getP());
				SPt q = point(midpoint.getQ());
				SJ half = k(Q.of(BigInteger.ONE, BigInteger.TWO));
				return new SPt(p.x.add(q.x).mul(half), p.y.add(q.y).mul(half));
			}
			if (parent instanceof AlgoCircleTwoPoints) {
				AlgoCircleTwoPoints circle = (AlgoCircleTwoPoints) parent;
				return through(point(circle.getCenter().toGeoElement()),
						point(circle.getExternalPoint().toGeoElement()), label);
			}
			if (parent instanceof AlgoCirclePointRadius) {
				AlgoCirclePointRadius circle = (AlgoCirclePointRadius) parent;
				SPt centre = point(circle.getCenter().toGeoElement());
				SJ r = (SJ) eval(circle.getRadiusGeo());
				return new SCircle(centre.x, centre.y, r.square());
			}
			if (parent instanceof AlgoIntersectSingle && ((AlgoIntersectSingle) parent)
					.getSourceIntersection() instanceof AlgoIntersectLineConic) {
				AlgoIntersectSingle single = (AlgoIntersectSingle) parent;
				return lineConic((AlgoIntersectLineConic) single.getSourceIntersection(),
						single.getSelectedIndex(), label);
			}
			if (parent instanceof AlgoIntersectLineConic) {
				AlgoIntersectLineConic algorithm = (AlgoIntersectLineConic) parent;
				for (int output = 0; output < algorithm.getOutputLength(); output++) {
					if (algorithm.getOutput(output) == geo) {
						return lineConic(algorithm, output, label);
					}
				}
			}
			if (automatic && geo instanceof GeoPoint) {
				f.cuts.add(label + ": " + parent.getClass().getSimpleName() + " outside the class, "
						+ "cut as a free point");
				return freePoint((GeoPoint) geo);
			}
			throw new OutOfClass(parent.getClass().getSimpleName() + " (" + label + ")");
		}

		private SPt lineConic(AlgoIntersectLineConic algorithm, int index, String label) {
			if (!(algorithm.getConic() instanceof GeoConic)
					|| !((GeoConic) algorithm.getConic()).isCircle()) {
				throw new OutOfClass("conic that is not a circle (" + label + ")");
			}
			boolean compaction = algorithm.isSelectionFirstIntersectionPending();
			int[] permutation = compaction ? new int[] {0, 1}
					: algorithm.getSelectionPermutation();
			if (selections.put(algorithm, selectionState(algorithm)) == null) {
				selectionOrder.add(algorithm);
			}
			return lineCircle(line(algorithm.getLine()), circle((GeoConic) algorithm.getConic()),
					compaction, permutation, index, label);
		}
	}

	// ======================================================================== contacts

	private static final String ALPHA = String.valueOf((char) 0x03b1) + "_{cone}";

	/** The live binary64 values of the free inputs of the dependency cone, for the report. */
	private static Map<String, Object> configuration(AppGeoCeDG app) {
		Map<String, Object> row = new LinkedHashMap<>();
		GeoPoint b = (GeoPoint) lookup(app, "B");
		GeoPoint h = (GeoPoint) lookup(app, "H");
		GeoPoint a = (GeoPoint) lookup(app, "A");
		GeoPoint d1 = (GeoPoint) lookup(app, "D_1");
		row.put("B.y", b.getInhomY());
		row.put("H", List.of(h.getInhomX(), h.getInhomY()));
		row.put("A", List.of(a.getInhomX(), a.getInhomY()));
		row.put("D_1", List.of(d1.getInhomX(), d1.getInhomY()));
		row.put("rbCone", number(app, "rbCone"));
		return row;
	}

	/**
	 * The boundary scope of the author model. B lies on its constructible line path g: one
	 * coordinate is an indeterminate and the other is solved from the exact equation of g. H lies
	 * on j = Line[A, C'], which is not constructible (C' is a rotation by 47 degrees), so H is a free
	 * point of the plane: a certificate then holds whatever the exact position of H on j is. Every
	 * other free input is an exact constant.
	 */
	private static Sym boundaryModel(AppGeoCeDG app, Field field) {
		Sym sym = new Sym(field, false);
		GeoPoint b = (GeoPoint) lookup(app, "B");
		sym.values.put(b, sym.pathPoint(b));
		GeoPoint h = (GeoPoint) lookup(app, "H");
		sym.values.put(h, sym.freePoint(h));
		return sym;
	}

	/**
	 * Sets the driver E_1 to the exact driver point D (K or J), evaluated symbolically, after
	 * checking that D lies identically on the circle path p of E_1; with jets, E_1 also carries the
	 * exact first and second derivatives of its path at D. Records whether the contact parameter is
	 * structurally 0 or pi (cos and sin identically 1 or -1, and 0).
	 */
	private static String driver(AppGeoCeDG app, Sym sym, String dLabel, boolean jets) {
		SCircle p = sym.circle(lookup(app, "p"));
		SPt d = sym.point(lookup(app, dLabel));
		if (!residual(p, d).v.isZero()) {
			throw new OutOfClass("the driver point is not identically on its path");
		}
		SJ r = p.r2.sqrt("radius of p");
		SJ cos = d.x.sub(p.mx).div(r, "cos t*");
		SJ sin = d.y.sub(p.my).div(r, "sin t*");
		Field f = sym.f;
		String parameter = sin.v.isZero() && cos.v.sub(f.constant(Q.ONE)).isZero()
				? "0 for every value of the indeterminates"
				: sin.v.isZero() && cos.v.add(f.constant(Q.ONE)).isZero()
						? "pi for every value of the indeterminates" : "not structural";
		Ext rv = r.v;
		SJ x = jets ? new SJ(d.x.v, rv.mul(sin.v).neg(), rv.mul(cos.v).neg())
				: new SJ(d.x.v, null, null);
		SJ y = jets ? new SJ(d.y.v, rv.mul(cos.v), rv.mul(sin.v).neg())
				: new SJ(d.y.v, null, null);
		sym.values.put(lookup(app, "E_1"), new SPt(x, y));
		return parameter;
	}

	private static SJ residual(Object target, SPt point) {
		if (target instanceof SLine) {
			SLine l = (SLine) target;
			return l.a.mul(point.x).add(l.b.mul(point.y)).add(l.c);
		}
		SCircle c = (SCircle) target;
		return point.x.sub(c.mx).square().add(point.y.sub(c.my).square()).sub(c.r2);
	}

	/** One symbolic evaluation of an incidence, decided on the smallest box. */
	static final class Evaluation {
		final Map<String, Object> row = new LinkedHashMap<>();
		final Field field = new Field(HALF_WIDTHS[0]);
		Sym sym;
		boolean certified;

		/**
		 * The proof holds at the live configuration when its constants are unchanged, the
		 * inherited selection states it used are unchanged and every sign it decided is certified
		 * on the box around the live values of its indeterminates with the same value.
		 */
		String recheck(double halfWidth) {
			Iv[] box = new Iv[NV];
			for (Coordinate coordinate : field.coordinates) {
				double value = coordinate.live.getAsDouble();
				if (coordinate.index >= 0) {
					box[coordinate.index] = interval(value, halfWidth);
				} else if (Double.compare(value, coordinate.value) != 0) {
					return "NOT_APPLICABLE: the constant " + coordinate.name + " of this proof "
							+ "changed (a new proof is needed)";
				}
			}
			for (AlgoIntersectLineConic algorithm : sym.selectionOrder) {
				if (!selectionState(algorithm).equals(sym.selections.get(algorithm))) {
					return "NOT_ESTABLISHED: inherited selection state of " + algorithm.getOutput(0)
							.getLabelSimple() + " changed";
				}
			}
			String failed = field.recheck(box);
			return failed == null ? "CERTIFIED"
					: "NOT_ESTABLISHED: " + failed + ": not certified with the same sign";
		}
	}

	/**
	 * One proof attempt of an incidence of a point with a target, with all its decisions taken on
	 * the smallest box; with a driver label, the driver E_1 is first set to that point.
	 */
	private static Evaluation evaluate(AppGeoCeDG app, String pointLabel, String driverLabel,
			String targetLabel, String variant, boolean jets, boolean automatic, long budget) {
		Evaluation evaluation = new Evaluation();
		Field field = evaluation.field;
		Map<String, Object> row = evaluation.row;
		workDone = 0;
		workBudget = budget;
		long started = System.nanoTime();
		try {
			Sym sym = automatic ? new Sym(field, true) : boundaryModel(app, field);
			evaluation.sym = sym;
			if (driverLabel != null) {
				row.put("contact_parameter", driver(app, sym, driverLabel, jets));
			}
			SPt point = sym.point(lookup(app, pointLabel));
			Object target = target(app, sym, targetLabel, variant);
			SJ h = residual(target, point);
			boolean identity = h.v.isZero();
			row.put("incidence", identity ? "IDENTITY" : "NOT_AN_IDENTITY (residual numerator terms "
					+ h.v.a.num.terms.size() + " + " + h.v.b.num.terms.size() + ")");
			boolean member = true;
			if (target instanceof SLine && ((SLine) target).bounded) {
				member = sym.member((SLine) target, point, "limited part of " + targetLabel);
				row.put("limited_part", member ? "member (certified)" : "not a member (certified)");
			}
			if (jets && identity) {
				row.put("contact_order", order(h));
				row.put("locus_point_velocity", velocity(point));
			}
			evaluation.certified = identity && member;
			row.put("outcome", evaluation.certified ? "CERTIFIED: identity with every precondition "
					+ "certified on the decision box" : identity ? "NOT CERTIFIED: identity with the "
							+ "support, outside its limited part" : "NOT CERTIFIED: not an identity");
		} catch (Undecided undecided) {
			row.put("outcome", "NOT_ESTABLISHED: precondition undecided on the decision box ("
					+ undecided.getMessage() + ")");
		} catch (Budget budgetExceeded) {
			row.put("outcome", "NOT_ESTABLISHED: " + budgetExceeded.getMessage());
		} catch (Invalid invalid) {
			row.put("outcome", "INVALID: " + invalid.getMessage());
		} catch (OutOfClass outOfClass) {
			row.put("outcome", "OUT_OF_CLASS: " + outOfClass.getMessage());
		} catch (RuntimeException failure) {
			StackTraceElement[] trace = failure.getStackTrace();
			row.put("outcome", "PROBE_FAILURE: " + failure + (trace.length > 0 ? " at " + trace[0]
					: ""));
		}
		System.err.printf("CIC %s on %s at %s %s jets=%s automatic=%s: %.2f s, work %d%n",
				pointLabel, targetLabel, driverLabel, variant, jets, automatic,
				(System.nanoTime() - started) / 1e9, workDone);
		row.put("indeterminates", field.names(true));
		if (!automatic) {
			row.put("constants", field.names(false));
		}
		if (!field.cuts.isEmpty()) {
			row.put("cuts", field.cuts);
		}
		row.put("sign_conditions_decided", field.checks.size());
		row.put("divisions_certified_nonzero", field.divisions);
		row.put("extension", field.delta == null ? "none"
				: "sqrt of a polynomial of degree " + field.delta.maxDegree() + " with "
						+ field.delta.terms.size() + " terms");
		row.put("largest_element_terms", field.maxSize);
		row.put("work_term_products", workDone);
		return evaluation;
	}

	private static Evaluation evaluate(AppGeoCeDG app, String[] contact, String variant,
			boolean jets, boolean automatic, long budget) {
		return evaluate(app, contact[1], contact[2], contact[3], variant, jets, automatic, budget);
	}

	private static Object target(AppGeoCeDG app, Sym sym, String targetLabel, String variant) {
		switch (variant) {
		case "binary64_target":
			return binaryTarget(sym, lookup(app, targetLabel));
		case "other_branch_of_M_1": {
			// The other intersection of the support line of n with c_1, outside the segment.
			GeoElement m1 = lookup(app, "M_1");
			AlgoIntersectLineConic algorithm = (AlgoIntersectLineConic) m1.getParentAlgorithm();
			SPt selected = sym.point(m1);
			SLine n = sym.line(algorithm.getLine());
			SLine support = new SLine(n.a, n.b, n.c, false, false, null, null);
			SPt[] raw = sym.rawRoots(support, sym.circle((GeoConic) algorithm.getConic()),
					"M_1 roots of the support line");
			SPt other = same(raw[0], selected) ? raw[1] : same(raw[1], selected) ? raw[0] : null;
			if (other == null) {
				throw new OutOfClass("M_1 is not identically a root of its support line");
			}
			return sym.parallel(other, sym.line(lookup(app, "k")));
		}
		default:
			return sym.eval(lookup(app, targetLabel));
		}
	}

	private static boolean same(SPt p, SPt q) {
		return p.x.v.sub(q.x.v).isZero() && p.y.v.sub(q.y.v).isZero();
	}

	/** Contact order from the exact jets of an identically vanishing residual. */
	private static String order(SJ h) {
		if (h.d1 != null && !h.d1.isZero()) {
			int s = signOrZero(h.d1);
			return s == 0 ? "h' not identically zero, sign undecided on the box"
					: "h' " + (s > 0 ? "positive" : "negative") + " on the box (certified): "
							+ "transverse contact";
		}
		if (h.d2 == null || h.d2.isZero()) {
			return "h' and h'' identically zero";
		}
		int s = signOrZero(h.d2);
		return "h' identically zero; h'' " + (s == 0 ? "sign undecided on the box"
				: (s > 0 ? "positive" : "negative") + " on the box (certified): contact of order "
						+ "exactly two in the driver parameter");
	}

	/** Whether the traced point is stationary at the contact (a fold of its parametrization). */
	private static String velocity(SPt point) {
		Ext dx = point.x.d1 != null ? point.x.d1 : point.x.v.f.constant(Q.ZERO);
		Ext dy = point.y.d1 != null ? point.y.d1 : point.y.v.f.constant(Q.ZERO);
		if (dx.isZero() && dy.isZero()) {
			return "F'(t*) identically zero: stationary point of the parametrization (fold)";
		}
		int s = signOrZero(dx.mul(dx).add(dy.mul(dy)));
		return s > 0 ? "|F'(t*)| positive on the box (certified): regular point, tangency"
				: "F'(t*) not identically zero, nonvanishing undecided on the box";
	}

	private static int signOrZero(Ext value) {
		try {
			return value.eval().sign();
		} catch (Undecided undecided) {
			return 0;
		}
	}

	private static Object binaryTarget(Sym sym, GeoElement geo) {
		if (geo instanceof GeoLine) {
			GeoLine l = (GeoLine) geo;
			boolean bounded = l instanceof GeoSegment || l instanceof GeoRay;
			GeoPoint start = bounded ? l.getStartPoint() : null;
			return new SLine(sym.k(l.getX()), sym.k(l.getY()), sym.k(l.getZ()), bounded,
					l instanceof GeoRay, start == null ? null : sym.k(start.getInhomX()),
					start == null ? null : sym.k(start.getInhomY()));
		}
		GeoConic c = (GeoConic) geo;
		return new SCircle(sym.k(c.getTranslationVector().getX()),
				sym.k(c.getTranslationVector().getY()), sym.k(c.getHalfAxes()[0]).square());
	}

	private static Map<String, Object> incidences(AppGeoCeDG app, Map<String, Evaluation> proofs,
			boolean automatic) {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String[] contact : CONTACTS) {
			Evaluation evaluation = evaluate(app, contact, "constructive", false, automatic,
					INCIDENCE_BUDGET);
			if (evaluation.certified) {
				Map<String, Object> boxes = new LinkedHashMap<>();
				for (double halfWidth : HALF_WIDTHS) {
					boxes.put("half-width " + halfWidth, evaluation.recheck(halfWidth));
				}
				evaluation.row.put("boxes", boxes);
				proofs.put(contact[0], evaluation);
			}
			rows.put(contact[0], evaluation.row);
		}
		return rows;
	}

	/**
	 * A proof of a mandatory incidence is a formal identity: at another configuration it holds
	 * again when its constants are unchanged, every recorded sign is certified there with the same
	 * value and the inherited selection states are unchanged, with no new symbolic work. The
	 * positions are the twelve of the authority study, then moves of the boundary inputs; at the
	 * two positions where rechecks fail closed, new proofs are attempted.
	 */
	private static Map<String, Object> configurations(AppGeoCeDG app,
			Map<String, Evaluation> boundary, Map<String, Evaluation> automatic) {
		Map<String, Object> rows = new LinkedHashMap<>();
		rows.put("base", recheckAll(app, boundary, automatic));
		double rbCone = number(app, "rbCone");
		for (double value : new double[] {3, 4, 6, 7.5}) {
			PreG9BCu0Corpus.setNumber(app, "rbCone", value);
			rows.put("rbCone=" + value, recheckAll(app, boundary, automatic));
		}
		PreG9BCu0Corpus.setNumber(app, "rbCone", rbCone);
		double angle = number(app, ALPHA);
		for (double degrees : new double[] {35, 60}) {
			PreG9BCu0Corpus.setNumber(app, ALPHA, Math.toRadians(degrees));
			rows.put("alpha_cone=" + degrees, recheckAll(app, boundary, automatic));
		}
		PreG9BCu0Corpus.setNumber(app, ALPHA, angle);
		GeoPoint d1 = (GeoPoint) lookup(app, "D_1");
		GeoPoint h = (GeoPoint) lookup(app, "H");
		double dx = d1.getInhomX();
		double dy = d1.getInhomY();
		for (double factor : new double[] {0.5, 1.3, 3.0}) {
			d1.setCoords(h.getInhomX() + factor * (dx - h.getInhomX()),
					h.getInhomY() + factor * (dy - h.getInhomY()), 1);
			d1.updateRepaint();
			rows.put("D_1 radius factor " + factor, recheckAll(app, boundary, automatic));
			if (factor == 3.0) {
				rows.put("D_1 radius factor 3.0: new proofs at this position", newProofs(app));
			}
		}
		d1.setCoords(dx, dy, 1);
		d1.updateRepaint();
		double exc = number(app, "excCyl");
		PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7);
		rows.put("excCyl=3.7 (outside the dependency cone)", recheckAll(app, boundary,
				automatic));
		PreG9BCu0Corpus.setNumber(app, "excCyl", exc);
		rows.put("restored", recheckAll(app, boundary, automatic));
		double tH = PreG9BCu0Corpus.pathParameter(app, "H");
		for (double delta : new double[] {-20, 20, 40}) {
			PreG9BCu0Corpus.movePathPoint(app, "H", tH + delta);
			rows.put("H path parameter " + (delta > 0 ? "+" : "") + delta, recheckAll(app,
					boundary, automatic));
			if (delta == 40) {
				rows.put("H path parameter +40.0: new proofs at this position", newProofs(app));
			}
		}
		PreG9BCu0Corpus.movePathPoint(app, "H", tH);
		double tB = PreG9BCu0Corpus.pathParameter(app, "B");
		for (double delta : new double[] {-3, 3}) {
			PreG9BCu0Corpus.movePathPoint(app, "B", tB + delta);
			rows.put("B path parameter " + (delta > 0 ? "+" : "") + delta, recheckAll(app,
					boundary, automatic));
		}
		PreG9BCu0Corpus.movePathPoint(app, "B", tB);
		rows.put("restored again", recheckAll(app, boundary, automatic));
		return rows;
	}

	private static Map<String, Object> recheckAll(AppGeoCeDG app,
			Map<String, Evaluation> boundary, Map<String, Evaluation> automatic) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("configuration", configuration(app));
		for (String[] contact : CONTACTS) {
			Map<String, Object> entry = new LinkedHashMap<>();
			Evaluation local = boundary.get(contact[0]);
			Evaluation general = automatic.get(contact[0]);
			entry.put("boundary_proof", local == null ? "no proof" : local.recheck(HALF_WIDTHS[0]));
			entry.put("automatic_proof", general == null ? "no proof"
					: general.recheck(HALF_WIDTHS[0]));
			row.put(contact[0], entry);
		}
		return row;
	}

	/**
	 * Where a recheck fails, the configuration may lie in another cell: a new proof, in the
	 * automatic scope, either proves the incidence there or reports why it does not hold.
	 */
	private static Map<String, Object> newProofs(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String[] contact : CONTACTS) {
			Map<String, Object> row = evaluate(app, contact, "constructive", false, true,
					INCIDENCE_BUDGET).row;
			Map<String, Object> summary = new LinkedHashMap<>();
			summary.put("incidence", row.getOrDefault("incidence", "not decided"));
			if (row.containsKey("limited_part")) {
				summary.put("limited_part", row.get("limited_part"));
			}
			summary.put("outcome", row.get("outcome"));
			rows.put(contact[0], summary);
		}
		return rows;
	}

	private static Map<String, Object> contactOrder(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String[] contact : CONTACTS) {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("boundary_scope", evaluate(app, contact, "constructive", true, false,
					ORDER_BUDGET).row);
			entry.put("automatic_scope", evaluate(app, contact, "constructive", true, true,
					ORDER_BUDGET).row);
			rows.put(contact[0], entry);
		}
		return rows;
	}

	private static Map<String, Object> controls(AppGeoCeDG app, Map<String, Evaluation> proofs) {
		Map<String, Object> rows = new LinkedHashMap<>();
		rows.put("C1 binary64 target n (representation, not the construction): e_1*n at K",
				evaluate(app, CONTACTS[0], "binary64_target", false, false, INCIDENCE_BUDGET).row);
		rows.put("C1 binary64 target h_2: o_1*h_2 at K", evaluate(app, CONTACTS[2],
				"binary64_target", false, false, INCIDENCE_BUDGET).row);
		rows.put("C2 other branch of M_1 for h_2: o_1*h_2 at K", evaluate(app, CONTACTS[2],
				"other_branch_of_M_1", false, false, INCIDENCE_BUDGET).row);
		rows.put("C3 accidental equality: I on the constant vertical line through its stand-in",
				accidental(app));
		Evaluation proof = proofs.get(CONTACTS[0][0]);
		rows.put("C4 box too large (half-width 5) for the proof of e_1*n at K", proof == null
				? "no proof to recheck" : proof.recheck(5));
		rows.put("C5 wrong pairing: J_1 at J against n", evaluate(app, "J_1", "J", "n",
				"constructive", false, false, INCIDENCE_BUDGET).row);
		return rows;
	}

	/**
	 * I = (H.x, B.y) lies exactly on the constant vertical line x = I.x of its live stand-in, where
	 * the residual vanishes; as a function of the indeterminates it does not vanish identically,
	 * so the equality at one configuration is not certified.
	 */
	private static Map<String, Object> accidental(AppGeoCeDG app) {
		Map<String, Object> row = new LinkedHashMap<>();
		Field field = new Field(HALF_WIDTHS[0]);
		workDone = 0;
		workBudget = INCIDENCE_BUDGET;
		try {
			Sym sym = boundaryModel(app, field);
			SPt point = sym.point(lookup(app, "I"));
			GeoPoint live = (GeoPoint) lookup(app, "I");
			SLine vertical = new SLine(sym.k(-1), sym.k(0), sym.k(live.getInhomX()), false, false,
					null, null);
			SJ h = residual(vertical, point);
			row.put("incidence", h.v.isZero() ? "IDENTITY" : "NOT_AN_IDENTITY");
			Iv value = h.v.eval();
			row.put("residual_interval_on_the_box", List.of(value.lo, value.hi));
			row.put("outcome", h.v.isZero() ? "CERTIFIED" : "NOT CERTIFIED: not an identity");
		} catch (RuntimeException failure) {
			row.put("outcome", failure.getClass().getSimpleName() + ": " + failure.getMessage());
		}
		return row;
	}

	// ======================================================================== synthetic

	/** Constructions outside the author model, built in a fresh host with English commands. */
	private static final String[] SYNTHETIC = {
			"SA=(0.37,-1.21)", "SB=(5.83,0.44)", "SP=(2.9,3.3)", "SC=Rotate(SP,47*pi/180,SA)",
			"mAB=Midpoint(SA,SB)", "mBC=Midpoint(SB,SC)", "mCA=Midpoint(SC,SA)",
			"sAB=Line(SA,SB)", "sBC=Line(SB,SC)", "sCA=Line(SC,SA)",
			"pAB=OrthogonalLine(mAB,sAB)", "pBC=OrthogonalLine(mBC,sBC)",
			"pCA=OrthogonalLine(mCA,sCA)", "SO=Intersect(pAB,pBC)", "cc=Circle(SO,SA)",
			"gA=Line(SA,mBC)", "gB=Line(SB,mCA)", "gC=Line(SC,mAB)", "SG=Intersect(gA,gB)",
			"TA=(-2.3,0.7)", "TB=(3.1,1.9)", "TO=Midpoint(TA,TB)", "tc=Circle(TO,TA)",
			"TP=(0.4,4.6)", "tl=Line(TA,TP)", "TQ=Intersect(tc,tl,1)", "TR=Intersect(tc,tl,2)",
			"tp=OrthogonalLine(TB,tl)",
			"UA=(1.1,0.9)", "UB=(4.2,2.3)", "UR=Rotate(UB,29*pi/180,UA)", "ul=Line(UA,UR)",
			"uf=OrthogonalLine(UB,ul)", "UF=Intersect(uf,ul)", "UM=Midpoint(UA,UB)",
			"uw=Circle(UM,UA)", "sl=Line(UA,UB)", "UX=Point(sl)", "UY=Point(ul)"};

	/** Statement, point, target: theorems, a branch-sensitive pair and false conjectures. */
	private static final String[][] STATEMENTS = {
			{"S1 the circumcentre lies on the third perpendicular bisector (theorem)", "SO", "pCA"},
			{"S1 the second vertex lies on the circumcircle (theorem)", "SB", "cc"},
			{"S1 the rotated, non-constructible vertex lies on the circumcircle (theorem)", "SC",
					"cc"},
			{"S1 the centroid lies on the third median (theorem)", "SG", "gC"},
			{"S1 false conjecture: the centroid lies on the circumcircle", "SG", "cc"},
			{"S2 Thales, intersection 1 of tc and tl on the perpendicular from TB", "TQ", "tp"},
			{"S2 Thales, intersection 2 of tc and tl on the perpendicular from TB", "TR", "tp"},
			{"S3 foot of the perpendicular from UB to a non-constructible line on the circle with "
					+ "diameter UA UB (Thales)", "UF", "uw"},
			{"S3 a point on a constructible line lies on it (one indeterminate on the path)", "UX",
					"sl"},
			{"S3 false conjecture: that point lies on the circle", "UX", "uw"},
			{"S3 limitation: a point on a non-constructible line is cut, so its own path "
					+ "membership, a provenance relation, is not re-proved", "UY", "ul"}};

	private static Map<String, Object> synthetic() {
		AppGeoCeDG app = G9U1TestApp.create();
		for (String command : SYNTHETIC) {
			GeoElementND[] output = app.getKernel().getAlgebraProcessor()
					.processAlgebraCommand(command, false);
			if (output == null || output.length == 0) {
				throw new IllegalStateException("command failed: " + command);
			}
		}
		Map<String, Object> rows = new LinkedHashMap<>();
		rows.put("commands", List.of(SYNTHETIC));
		for (String[] statement : STATEMENTS) {
			rows.put(statement[0], evaluate(app, statement[1], null, statement[2], "constructive",
					false, true, INCIDENCE_BUDGET).row);
		}
		return rows;
	}

	private static double number(AppGeoCeDG app, String label) {
		return ((GeoNumeric) lookup(app, label)).getValue();
	}

	private static String selectionState(AlgoIntersectLineConic algorithm) {
		return algorithm.isSelectionFirstIntersectionPending() ? "first intersection pending"
				: Arrays.toString(algorithm.getSelectionPermutation());
	}

	// ======================================================================== helpers

	private static GeoElement lookup(AppGeoCeDG app, String label) {
		GeoElement geo = app.getKernel().lookupLabel(label);
		if (geo == null) {
			throw new IllegalStateException("missing " + label);
		}
		return geo;
	}

	private static <T> T onEdt(Callable<T> action) throws Exception {
		return PreG9BR6PlusF3Fixtures.onEdt(action);
	}
}
