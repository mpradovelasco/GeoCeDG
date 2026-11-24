/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import org.geocedg.common.kernel.algos.AlgoBSpline;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.ExactRationalBSplineReference;
import org.geocedg.common.math.bspline.ExactRationalBSplineReference.Fraction;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.jre.headless.AppCommon;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A2, BS-8 (matrix A-N1; {@code bspline-2d.md} sections 9 and 15): the class-3
 * length estimate of a {@code BSpline} against an independent reference: composite
 * Gauss-Legendre quadrature in {@code BigDecimal} (60 significant digits) of the norm of the
 * exact rational derivative of the binary64 data, span by span, with its own accuracy shown by
 * doubling the subdivision. Each span is integrated with its own polynomial over the closed
 * span, so the derivative at a span end is the one-sided limit of that span: the reference
 * never takes the derivative of the neighbouring span at a {@code C^0} knot. Total length of
 * a {@code C^2} curve and of a curve with a multiplicity-3 knot, a partial length across that
 * knot, degree-1 curves (every interior knot is {@code C^0}) and closed curves (the seam);
 * monotone cumulative length; zoom and pixel-ratio invariance.
 */
public class MaintA2BSplineLengthTest extends BaseUnitTest {
	private static final MathContext MC = new MathContext(60, RoundingMode.HALF_EVEN);
	private static final int NODES = 20;
	private static final BigDecimal[][] RULE = gaussLegendre(NODES);

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/** Nodes and weights of the n-point Gauss-Legendre rule on [-1, 1], by Newton. */
	private static BigDecimal[][] gaussLegendre(int n) {
		MathContext mc = new MathContext(80, RoundingMode.HALF_EVEN);
		BigDecimal[] nodes = new BigDecimal[n];
		BigDecimal[] weights = new BigDecimal[n];
		BigDecimal two = BigDecimal.valueOf(2);
		for (int i = 0; i < n; i++) {
			BigDecimal x = new BigDecimal(Math.cos(Math.PI * (i + 0.75) / (n + 0.5)), mc);
			BigDecimal derivative = BigDecimal.ONE;
			for (int iteration = 0; iteration < 12; iteration++) {
				BigDecimal previous = BigDecimal.ONE;
				BigDecimal current = x;
				for (int k = 2; k <= n; k++) {
					BigDecimal next = BigDecimal.valueOf(2L * k - 1).multiply(x, mc)
							.multiply(current, mc).subtract(BigDecimal.valueOf(k - 1)
									.multiply(previous, mc), mc)
							.divide(BigDecimal.valueOf(k), mc);
					previous = current;
					current = next;
				}
				derivative = BigDecimal.valueOf(n).multiply(x.multiply(current, mc)
						.subtract(previous, mc), mc)
						.divide(x.multiply(x, mc).subtract(BigDecimal.ONE, mc), mc);
				x = x.subtract(current.divide(derivative, mc), mc);
			}
			nodes[i] = x;
			weights[i] = two.divide(BigDecimal.ONE.subtract(x.multiply(x, mc), mc)
					.multiply(derivative.multiply(derivative, mc), mc), mc);
		}
		return new BigDecimal[][] {nodes, weights};
	}

	private static Fraction fraction(BigDecimal value) {
		BigDecimal stripped = value.stripTrailingZeros();
		if (stripped.scale() <= 0) {
			return Fraction.of(stripped.toBigIntegerExact(), BigInteger.ONE);
		}
		return Fraction.of(stripped.unscaledValue(), BigInteger.TEN.pow(stripped.scale()));
	}

	private static BigDecimal speed(BSplineData data, int span, BigDecimal u) {
		Fraction[] derivative = ExactRationalBSplineReference.derivative(data.knots(),
				data.degree(), data.xs(), data.ys(), null, span, fraction(u));
		BigDecimal dx = derivative[0].toBigDecimal(MC);
		BigDecimal dy = derivative[1].toBigDecimal(MC);
		return dx.multiply(dx, MC).add(dy.multiply(dy, MC), MC).sqrt(MC);
	}

	/** Composite Gauss-Legendre over [lower, upper] in the given span, m pieces. */
	private static BigDecimal integrate(BSplineData data, int span, BigDecimal lower,
			BigDecimal upper, int pieces) {
		BigDecimal width = upper.subtract(lower, MC).divide(BigDecimal.valueOf(pieces), MC);
		BigDecimal half = width.divide(BigDecimal.valueOf(2), MC);
		BigDecimal sum = BigDecimal.ZERO;
		for (int piece = 0; piece < pieces; piece++) {
			BigDecimal center = lower.add(width.multiply(BigDecimal.valueOf(piece), MC), MC)
					.add(half, MC);
			for (int node = 0; node < NODES; node++) {
				BigDecimal u = center.add(half.multiply(RULE[0][node], MC), MC);
				sum = sum.add(RULE[1][node].multiply(speed(data, span, u), MC), MC);
			}
		}
		return sum.multiply(half, MC);
	}

	/**
	 * Reference length of [from, to], span by span; returns {length, quadrature difference
	 * between m = 4 and m = 8 pieces per span}.
	 */
	private static BigDecimal[] reference(BSplineData data, double from, double to) {
		BigDecimal coarse = BigDecimal.ZERO;
		BigDecimal fine = BigDecimal.ZERO;
		for (int s = 0; s < data.spanCount(); s++) {
			int j = data.spanKnotIndex(s);
			double lower = Math.max(from, data.knot(j));
			double upper = Math.min(to, data.knot(j + 1));
			if (!(lower < upper)) {
				continue;
			}
			BigDecimal a = new BigDecimal(lower);
			BigDecimal b = new BigDecimal(upper);
			coarse = coarse.add(integrate(data, j, a, b, 4), MC);
			fine = fine.add(integrate(data, j, a, b, 8), MC);
		}
		return new BigDecimal[] {fine, fine.subtract(coarse, MC).abs()};
	}

	private static BSplineData data(GeoLocusV2 curve) {
		return ((AlgoBSpline) curve.getParentAlgorithm()).getEvaluator().getData();
	}

	private LocusMetricResult2D metric(String command) {
		return AlgoLocusMetricScalarAdapter.richResultOf(add(command)).getMetricResult();
	}

	private static void assertWithinDeclaredError(String name, LocusMetricResult2D estimate,
			BigDecimal[] reference) {
		assertTrue(estimate.getMetricValue().getFiniteValue().isPresent(), name + ": "
				+ estimate.getComputationStatus() + " " + estimate.getCoverage() + " "
				+ estimate.getRectifiability());
		double value = estimate.getMetricValue().getFiniteValue().orElseThrow();
		double declared = estimate.getErrorEvidence().getEstablishedAbsoluteAmount();
		assertTrue(Double.isFinite(declared), name + ": the estimate declares its error");
		assertTrue(reference[1].compareTo(new BigDecimal("1E-20")) < 0,
				name + ": reference converged " + reference[1]);
		double error = new BigDecimal(value).subtract(reference[0], MC).abs().doubleValue();
		// The established amount bounds the discretization of the estimate; the floating
		// summation of its leaves is not part of it (a degree-1 curve is integrated exactly
		// and declares 0), so a rounding allowance of 16 ulp of the value is added.
		double allowed = declared + 16 * Math.ulp(value);
		System.out.println("A2-BS8 " + name + " estimate=" + value + " reference="
				+ reference[0].round(new MathContext(30)) + " |error|=" + error
				+ " declared=" + declared);
		assertTrue(error <= allowed, name + ": |error| " + error + " > declared " + declared
				+ " + 16 ulp");
	}

	private void points() {
		for (String point : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2.5)", "D=(4,0.5)",
				"E=(6,1)", "F=(7,3)", "G=(5,4)"}) {
			add(point);
		}
	}

	private static BigDecimal[] total(GeoLocusV2 curve) {
		BSplineData data = data(curve);
		return reference(data, data.domainStart(), data.domainEnd());
	}

	@Test
	void totalPartialAcrossATripleKnotAndClosedLengthsMatchTheReference() {
		points();
		GeoLocusV2 open = add("b=BSpline({A,B,C,D,E,F},3)");
		assertWithinDeclaredError("total C2", metric("M=LocusLength(b)"), total(open));
		GeoLocusV2 triple = add("t=BSpline({A,B,C,D,E,F,G},3,{0,0,0,0,1,1,1,2,2,2,2})");
		assertWithinDeclaredError("total with multiplicity 3", metric("MT=LocusLength(t)"),
				total(triple));
		add("P=Point(t,\"bspline-2d/main\",0.5)");
		add("Q=Point(t,\"bspline-2d/main\",1.5)");
		assertWithinDeclaredError("partial across multiplicity 3",
				metric("MP=LocusLength(t,P,Q)"), reference(data(triple), 0.5, 1.5));
		GeoLocusV2 closed = add("cs=BSpline({A,B,C,D,A},3)");
		assertTrue(closed.getSemanticDefinition().getProvider().isPeriodic());
		assertWithinDeclaredError("closed", metric("MC=LocusLength(cs)"), total(closed));
	}

	@Test
	void degreeOneLengthsMatchTheReference() {
		points();
		GeoLocusV2 polyline = add("l=BSpline({A,B,C,D,E},1)");
		assertWithinDeclaredError("degree 1 total", metric("ML=LocusLength(l)"),
				total(polyline));
		double chords = Math.hypot(1, 2) + Math.hypot(2, 0.5) + Math.hypot(1, 2)
				+ Math.hypot(2, 0.5);
		assertEquals(chords, total(polyline)[0].doubleValue(), 4 * Math.ulp(chords));
		add("P=Point(l,\"bspline-2d/main\",0.5)");
		add("Q=Point(l,\"bspline-2d/main\",2.5)");
		assertWithinDeclaredError("degree 1 partial across two knots",
				metric("MP=LocusLength(l,P,Q)"), reference(data(polyline), 0.5, 2.5));
		GeoLocusV2 closed = add("cp=BSpline({A,B,C,D,A},1)");
		assertTrue(closed.getSemanticDefinition().getProvider().isPeriodic());
		assertWithinDeclaredError("degree 1 closed", metric("MK=LocusLength(cp)"),
				total(closed));
	}

	private static double finite(String name, LocusMetricResult2D result) {
		assertTrue(result.getMetricValue().getFiniteValue().isPresent(), name + ": "
				+ result.getComputationStatus() + " " + result.getCoverage());
		return result.getMetricValue().getFiniteValue().getAsDouble();
	}

	/** Cumulative lengths from the start at u = k / 4, k = 1..steps; strictly increasing. */
	private void assertMonotone(String curve, int steps) {
		add("S" + curve + "=Point(" + curve + ",\"bspline-2d/main\",0)");
		double previous = 0;
		for (int k = 1; k <= steps; k++) {
			String position = "P" + curve + k;
			add(position + "=Point(" + curve + ",\"bspline-2d/main\"," + (k * 0.25) + ")");
			double value = finite(curve + " at " + k, metric("L" + curve + k + "=LocusLength("
					+ curve + ",S" + curve + "," + position + ")"));
			assertTrue(value > previous, curve + " monotone at " + k);
			previous = value;
		}
	}

	@Test
	void cumulativeLengthIsMonotoneAndIndependentOfZoomAndPixelRatio() {
		points();
		add("b=BSpline({A,B,C,D,E,F},3)");
		add("t=BSpline({A,B,C,D,E,F,G},3,{0,0,0,0,1,1,1,2,2,2,2})");
		assertMonotone("b", 12);
		assertMonotone("t", 8);
		LocusMetricResult2D before = metric("T=LocusLength(b)");
		LocusMetricResult2D cornerBefore = metric("TT=LocusLength(t)");
		EuclidianView view = getApp().getEuclidianView1();
		view.setCoordSystem(13, 17, view.getXscale() * 7.5, view.getYscale() * 7.5);
		view.setPixelRatio(2);
		getKernel().updateConstruction(false);
		LocusMetricResult2D after = metric("T2=LocusLength(b)");
		LocusMetricResult2D cornerAfter = metric("TT2=LocusLength(t)");
		assertEquals(finite("b before", before), finite("b after", after), 0);
		assertEquals(before.getErrorEvidence().getEstablishedAbsoluteAmount(),
				after.getErrorEvidence().getEstablishedAbsoluteAmount(), 0);
		assertEquals(finite("t before", cornerBefore), finite("t after", cornerAfter), 0);
		assertEquals(cornerBefore.getErrorEvidence().getEstablishedAbsoluteAmount(),
				cornerAfter.getErrorEvidence().getEstablishedAbsoluteAmount(), 0);
	}
}
