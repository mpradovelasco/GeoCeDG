/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.BSplineEvaluation;
import org.geocedg.common.math.bspline.BSplinePoint;
import org.geocedg.common.math.bspline.ExactRationalBSplineReference;
import org.geocedg.common.math.bspline.ExactRationalBSplineReference.Fraction;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A-DXF0 fidelity: DX-1 (polynomial SPLINE, quarter circle Q and semicircle H
 * of matrix section 2.1 with the three separate checks and the reading rounding), DX-7
 * (render determinism, tolerance, incomplete mark), DX-8 (units and transformation) and
 * DX-9 (provenance), plus the committed fixtures.
 *
 * <p>Check (a) uses the rational criteria of the library derivation note
 * ({@code K_pos^rat(p) = 8p + 3}, {@code K_der^rat(p) = 8p + 4}, homogeneous scales) and the
 * polynomial ones ({@code 8p + 1}, {@code 8p - 3}).</p>
 */
final class PreG9BMaintADxf0FidelityTest {

	private static final double EPS = 0x1p-53;
	private static final MathContext MC = new MathContext(80, RoundingMode.HALF_EVEN);
	private static final long SEED = 20261124L;

	private static DxfImportReport read(byte[] bytes, DxfAffineTransform transform,
			String unit) {
		return DxfSplineReader.read(bytes, "fixture.dxf", transform, unit,
				DxfImportLimits.DEFAULT);
	}

	private static DxfSplineReference only(String fixture) {
		DxfImportReport report = read(DxfTestFiles.fixture(fixture), DxfAffineTransform.IDENTITY,
				"mm");
		assertNull(report.fileCause());
		assertEquals(1, report.records().size());
		return report.records().get(0);
	}

	/** Check (a): E1/E3 ratios of the library evaluation against the exact reference. */
	private static double[] checkA(BSplineData data, int span, double u) {
		int p = data.degree();
		double[] knots = data.knots();
		double[] xs = data.xs();
		double[] ys = data.ys();
		double[] ws = data.explicitWeights();
		BSplinePoint point = BSplineEvaluation.pointInSpan(data, span, u);
		BSplinePoint derivative = BSplineEvaluation.firstDerivativeInSpan(data, span, u);
		Fraction exactU = Fraction.of(u);
		Fraction[] h = ExactRationalBSplineReference.homogeneous(knots, p, xs, ys, ws, span,
				exactU);
		Fraction[] hd = ExactRationalBSplineReference.homogeneousDerivative(knots, p, xs, ys, ws,
				span, exactU);
		Fraction cx = h[0].div(h[2]);
		Fraction cy = h[1].div(h[2]);
		Fraction w2 = h[2].mul(h[2]);
		Fraction dx = hd[0].mul(h[2]).sub(h[0].mul(hd[2])).div(w2);
		Fraction dy = hd[1].mul(h[2]).sub(h[1].mul(hd[2])).div(w2);
		double error = ExactRationalBSplineReference.norm(Fraction.of(point.x()).sub(cx),
				Fraction.of(point.y()).sub(cy));
		double derivativeError = ExactRationalBSplineReference.norm(
				Fraction.of(derivative.x()).sub(dx), Fraction.of(derivative.y()).sub(dy));
		double w = h[2].approx();
		double cNorm = ExactRationalBSplineReference.norm(cx, cy);
		double dNorm = ExactRationalBSplineReference.norm(dx, dy);
		double maxW = 0;
		double maxWP = 0;
		for (int i = span - p; i <= span; i++) {
			double weight = ws == null ? 1 : ws[i];
			maxW = Math.max(maxW, weight);
			maxWP = Math.max(maxWP, weight * Math.hypot(xs[i], ys[i]));
		}
		double sA = 0;
		double sW = 0;
		for (int i = span - p; i <= span - 1; i++) {
			double width = knots[i + p + 1] - knots[i + 1];
			double wi = ws == null ? 1 : ws[i];
			double wj = ws == null ? 1 : ws[i + 1];
			sA = Math.max(sA, p * (wj * Math.hypot(xs[i + 1], ys[i + 1])
					+ wi * Math.hypot(xs[i], ys[i])) / width);
			sW = Math.max(sW, p * (wj + wi) / width);
		}
		double e1;
		double e3;
		if (ws == null) {
			e1 = error / (EPS * maxWP);
			e3 = derivativeError / (EPS * sA);
			assertTrue(e1 <= 8 * p + 1, "E1 " + e1);
			assertTrue(e3 <= 8 * p - 3, "E3 " + e3);
		} else {
			double sPos = (maxWP + cNorm * maxW) / w;
			double sDer = (sA + cNorm * sW + Math.abs(hd[2].approx()) * sPos + dNorm * maxW) / w;
			e1 = error / (EPS * sPos);
			e3 = derivativeError / (EPS * sDer);
			assertTrue(e1 <= 8 * p + 3, "rational E1 " + e1);
			assertTrue(e3 <= 8 * p + 4, "rational E3 " + e3);
		}
		return new double[] {e1, e3};
	}

	private static List<Double> quarterParameters() {
		List<Double> values = new ArrayList<>(List.of(0.0, 0.125, 0.25, 0.5, 0.75, 0.875, 1.0));
		Random random = new Random(SEED);
		for (int i = 0; i < 64; i++) {
			values.add(random.nextDouble());
		}
		return values;
	}

	private static BigDecimal[] oracle(BigDecimal t) {
		BigDecimal sqrt2 = BigDecimal.valueOf(2).sqrt(MC);
		BigDecimal r = t.divide(sqrt2.subtract(sqrt2.subtract(BigDecimal.ONE).multiply(t, MC),
				MC), MC);
		BigDecimal r2 = r.multiply(r, MC);
		BigDecimal d = BigDecimal.ONE.add(r2);
		return new BigDecimal[] {BigDecimal.ONE.subtract(r2).divide(d, MC),
				r.multiply(BigDecimal.valueOf(2)).divide(d, MC)};
	}

	private static BigDecimal[] exactWeightArc(BigDecimal t) {
		BigDecimal w = BigDecimal.valueOf(2).sqrt(MC).divide(BigDecimal.valueOf(2), MC);
		BigDecimal oneMinus = BigDecimal.ONE.subtract(t);
		BigDecimal a = oneMinus.multiply(oneMinus, MC);
		BigDecimal b = BigDecimal.valueOf(2).multiply(t).multiply(oneMinus, MC).multiply(w, MC);
		BigDecimal c = t.multiply(t, MC);
		BigDecimal d = a.add(b).add(c);
		return new BigDecimal[] {a.add(b).divide(d, MC), b.add(c).divide(d, MC)};
	}

	private static double distance(BigDecimal[] p, BigDecimal[] q) {
		return Math.hypot(p[0].subtract(q[0]).doubleValue(), p[1].subtract(q[1]).doubleValue());
	}

	@Test
	void dx1QuarterCircleThreeSeparateChecks() {
		DxfSplineReference record = only("dx1-quarter-circle-Q.dxf");
		assertEquals(DxfNature.RATIONAL, record.nature());
		assertTrue(record.rationalFlagSet());
		assertEquals(0x3FE6A09E667F3BCDL, Double.doubleToRawLongBits(record.data().weight(1)));
		double maxA = 0;
		double maxB = 0;
		double maxC = 0;
		for (double t : quarterParameters()) {
			int span = record.data().findSpan(t).spanKnotIndex();
			double[] ratios = checkA(record.data(), span, t);
			maxA = Math.max(maxA, Math.max(ratios[0], ratios[1]));
			BigDecimal exactT = new BigDecimal(t);
			maxB = Math.max(maxB, distance(exactWeightArc(exactT), oracle(exactT)));
			Fraction[] exact = ExactRationalBSplineReference.point(record.data().knots(), 2,
					record.data().xs(), record.data().ys(), record.data().explicitWeights(), span,
					Fraction.of(t));
			maxC = Math.max(maxC, distance(new BigDecimal[] {exact[0].toBigDecimal(MC),
				exact[1].toBigDecimal(MC)}, oracle(exactT)));
		}
		assertTrue(maxB < 1e-60);
		assertTrue(maxC < 1e-15);
		BigDecimal reading = new BigDecimal(record.sourceData().weight(1))
				.subtract(new BigDecimal(DxfTestFiles.W)).abs();
		System.out.printf(Locale.ROOT, "DXF0-DX1 Q maxRatioA=%.4f oracleCheckB=%.3e"
				+ " weightRoundingC=%.3e readingRounding=%.3e%n", maxA, maxB, maxC,
				reading.doubleValue());
	}

	@Test
	void dx1SemicircleThreeSeparateChecks() {
		DxfSplineReference record = only("dx1-semicircle-H.dxf");
		assertEquals(DxfNature.RATIONAL, record.nature());
		assertEquals(0, record.continuity().get(0).continuityOrder(), "published C^0 at u = 1");
		List<Double> parameters = new ArrayList<>();
		for (double t : quarterParameters()) {
			parameters.add(t);
			parameters.add(1.0 + t);
		}
		for (int k : new int[] {10, 20, 30}) {
			parameters.add(1.0 - Math.scalb(1.0, -k));
			parameters.add(1.0 + Math.scalb(1.0, -k));
		}
		parameters.add(1.0);
		double maxA = 0;
		double maxB = 0;
		double maxC = 0;
		for (double u : parameters) {
			int span = record.data().findSpan(u).spanKnotIndex();
			double[] ratios = checkA(record.data(), span, u);
			maxA = Math.max(maxA, Math.max(ratios[0], ratios[1]));
			boolean second = u >= 1.0;
			BigDecimal t = new BigDecimal(second ? u - 1.0 : u);
			BigDecimal[] q = oracle(t);
			BigDecimal[] o = second ? new BigDecimal[] {q[1].negate(), q[0]} : q;
			BigDecimal[] a = exactWeightArc(t);
			maxB = Math.max(maxB, distance(second ? new BigDecimal[] {a[1].negate(), a[0]} : a,
					o));
			Fraction[] exact = ExactRationalBSplineReference.point(record.data().knots(), 2,
					record.data().xs(), record.data().ys(), record.data().explicitWeights(), span,
					Fraction.of(u));
			maxC = Math.max(maxC, distance(new BigDecimal[] {exact[0].toBigDecimal(MC),
				exact[1].toBigDecimal(MC)}, o));
		}
		// One-sided first derivatives at u = 1 within E3 (observed C^1, published C^0).
		checkA(record.data(), 2, 1.0);
		checkA(record.data(), 4, 1.0);
		BSplinePoint top = BSplineEvaluation.point(record.data(), 1.0);
		assertEquals(4, top.spanKnotIndex(), "u = 1 is owned by the right span");
		assertEquals(0.0, top.x());
		assertEquals(1.0, top.y());
		assertTrue(maxB < 1e-60);
		assertTrue(maxC < 1e-15);
		System.out.printf(Locale.ROOT, "DXF0-DX1 H maxRatioA=%.4f oracleCheckB=%.3e"
				+ " weightRoundingC=%.3e%n", maxA, maxB, maxC);
	}

	@Test
	void dx1PolynomialSpline() {
		DxfSplineReference record = only("dx1-polynomial-cubic.dxf");
		assertEquals(DxfNature.POLYNOMIAL, record.nature());
		assertFalse(record.rationalFlagSet());
		double max = 0;
		Random random = new Random(SEED);
		List<Double> parameters = new ArrayList<>(List.of(0.0, 0.25, 0.5, 0.75, 1.0));
		for (int i = 0; i < 64; i++) {
			parameters.add(random.nextDouble());
		}
		for (double u : parameters) {
			double[] ratios = checkA(record.data(), record.data().findSpan(u).spanKnotIndex(), u);
			max = Math.max(max, Math.max(ratios[0], ratios[1]));
		}
		assertEquals(1, record.continuity().get(0).multiplicity());
		assertEquals(2, record.continuity().get(0).continuityOrder());
		System.out.printf(Locale.ROOT, "DXF0-DX1 polynomial maxRatioA=%.4f%n", max);
	}

	@Test
	void dx7RenderIsDeterministicWithinTheDeclaredToleranceAndMarksExhaustion() {
		for (String fixture : new String[] {"dx1-quarter-circle-Q.dxf", "dx1-semicircle-H.dxf",
			"dx1-polynomial-cubic.dxf"}) {
			DxfSplineReference first = only(fixture);
			DxfSplineReference second = only(fixture);
			DxfTessellation a = first.tessellation();
			DxfTessellation b = second.tessellation();
			assertTrue(a.isComplete());
			assertEquals(a.size(), b.size());
			for (int i = 0; i < a.size(); i++) {
				assertEquals(Double.doubleToRawLongBits(a.x(i)),
						Double.doubleToRawLongBits(b.x(i)));
				assertEquals(Double.doubleToRawLongBits(a.y(i)),
						Double.doubleToRawLongBits(b.y(i)));
			}
			double tau = a.tolerance();
			assertEquals(DxfImportLimits.RENDER_REL_TOL * DxfTessellation.controlDiagonal(
					first.data()), tau);
			double worst = 0;
			for (int i = 0; i + 1 < a.size(); i++) {
				double s = a.parameter(i);
				double t = a.parameter(i + 1);
				assertTrue(s < t, "parameters increase");
				for (int k = 1; k < 16; k++) {
					double u = s + (t - s) * k / 16;
					BSplinePoint p = BSplineEvaluation.point(first.data(), u);
					worst = Math.max(worst, DxfTessellation.deviation(
							new double[] {s, a.x(i), a.y(i)}, new double[] {t, a.x(i + 1),
								a.y(i + 1)}, new double[] {u, p.x(), p.y()}));
				}
			}
			assertTrue(worst <= tau, fixture + " " + worst + " > " + tau);
			// The tessellation contains every distinct knot of the domain.
			for (int s = 0; s < first.data().spanCount(); s++) {
				double knot = first.data().knot(first.data().spanKnotIndex(s));
				boolean found = false;
				for (int i = 0; i < a.size(); i++) {
					found |= a.parameter(i) == knot;
				}
				assertTrue(found, "split at knot " + knot);
			}
			System.out.printf(Locale.ROOT, "DXF0-DX7 %s points=%d evaluations=%d tau=%.3e"
					+ " worstSampledDeviation=%.3e%n", fixture, a.size(), a.evaluations(), tau,
					worst);
		}
		DxfImportReport limited = DxfSplineReader.read(
				DxfTestFiles.fixture("dx1-semicircle-H.dxf"), "h.dxf", DxfAffineTransform.IDENTITY,
				"mm", DxfImportLimits.reduced(DxfImportLimits.FILE_BYTES_MAX, 10, 12));
		DxfSplineReference incomplete = limited.records().get(0);
		assertFalse(incomplete.tessellation().isComplete());
		assertTrue(incomplete.diagnostics().contains(DxfDiagnostic.RENDER_INCOMPLETE));
		// The limit bounds the bisection work; the two ends of each span are always evaluated.
		assertTrue(incomplete.tessellation().evaluations()
				<= 12 + 2 * incomplete.data().spanCount());
		DxfTessellation partial = incomplete.tessellation();
		assertEquals(incomplete.domainEnd(), partial.parameter(partial.size() - 1),
				"the partial render still reaches the domain end by chords");
	}

	@Test
	void dx8UnitsAndExplicitTransformation() {
		byte[] inches = DxfTestFiles.file(1, DxfTestFiles.cubic());
		DxfImportReport differ = read(inches, DxfAffineTransform.IDENTITY, "mm");
		assertTrue(differ.diagnostics().contains(DxfDiagnostic.UNITS_DIFFER));
		assertEquals(Integer.valueOf(1), differ.file().insUnits());
		assertEquals("Inches", differ.file().insUnitsName());
		assertEquals("mm", differ.file().documentUnit());
		DxfSplineReference identity = differ.records().get(0);
		assertTrue(identity.transform().isIdentity(), "no implicit scaling");
		assertArrayEquals(identity.sourceData().xs(), identity.data().xs());
		assertFalse(read(DxfTestFiles.file(4, DxfTestFiles.cubic()), DxfAffineTransform.IDENTITY,
				"mm").diagnostics().contains(DxfDiagnostic.UNITS_DIFFER));
		assertFalse(read(inches, DxfAffineTransform.IDENTITY,
				DxfFileProvenance.UNSPECIFIED_MODEL_UNIT).diagnostics()
				.contains(DxfDiagnostic.UNITS_DIFFER));
		assertFalse(read(DxfTestFiles.file(0, DxfTestFiles.cubic()), DxfAffineTransform.IDENTITY,
				"cm").diagnostics().contains(DxfDiagnostic.UNITS_DIFFER), "unitless DXF");
		// Explicit transform: control points only, in the declared order; knots and weights kept.
		DxfAffineTransform transform = DxfAffineTransform.of(25.4, 0.1, -0.3, 25.4, 7.0, -2.5);
		assertNotNull(transform);
		DxfSplineReference q = read(DxfTestFiles.fixture("dx1-quarter-circle-Q.dxf"), transform,
				"mm").records().get(0);
		for (int i = 0; i < 3; i++) {
			double x = q.sourceData().x(i);
			double y = q.sourceData().y(i);
			assertEquals(Double.doubleToRawLongBits((25.4 * x + 0.1 * y) + 7.0),
					Double.doubleToRawLongBits(q.data().x(i)));
			assertEquals(Double.doubleToRawLongBits((-0.3 * x + 25.4 * y) + -2.5),
					Double.doubleToRawLongBits(q.data().y(i)));
		}
		assertArrayEquals(q.sourceData().knots(), q.data().knots());
		assertArrayEquals(q.sourceData().explicitWeights(), q.data().explicitWeights());
		assertEquals(q.sourceData().domainStart(), q.domainStart());
		assertArrayEquals(new double[] {25.4, 0.1, -0.3, 25.4, 7.0, -2.5},
				q.transform().coefficients());
		assertNull(DxfAffineTransform.of(1, 2, 2, 4, 0, 0), "singular");
		assertNull(DxfAffineTransform.of(Double.NaN, 0, 0, 1, 0, 0));
		assertNull(DxfAffineTransform.scale(0));
		assertNotNull(DxfAffineTransform.scale(25.4));
	}

	@Test
	void dx9ProvenanceIsCompleteAndIdenticalAcrossImports() {
		byte[] bytes = DxfTestFiles.fixture("smoke-mixed.dxf");
		DxfImportReport first = DxfSplineReader.read(bytes, "C:\\x\\y\\smoke-mixed.dxf",
				DxfAffineTransform.IDENTITY, "mm", DxfImportLimits.DEFAULT);
		DxfImportReport second = DxfSplineReader.read(bytes.clone(), "/other/smoke-mixed.dxf",
				DxfAffineTransform.IDENTITY, "mm", DxfImportLimits.DEFAULT);
		assertEquals("smoke-mixed.dxf", first.file().fileBaseName(), "base name only");
		assertEquals(64, first.file().fileSha256().length());
		assertEquals(first.file().fileSha256(), second.file().fileSha256());
		assertEquals("AC1015", first.file().acadVersion());
		assertEquals(Integer.valueOf(4), first.file().insUnits());
		assertEquals(first.entityCounts(), second.entityCounts());
		assertEquals(3, first.records().size());
		assertEquals(1, first.rejected().size());
		assertEquals(DxfCause.NON_UNIT_WEIGHTS_WITHOUT_RATIONAL_FLAG,
				first.rejected().get(0).cause());
		for (int i = 0; i < first.records().size(); i++) {
			DxfSplineReference a = first.records().get(i);
			DxfSplineReference b = second.records().get(i);
			assertEquals(DxfSplineReference.CONTRACT, a.contract());
			assertEquals(a.handle(), b.handle());
			assertEquals(a.layer(), b.layer());
			assertEquals(a.ordinal(), b.ordinal());
			assertEquals(a.planeZ(), b.planeZ());
			assertEquals(a.sourceDigest(), b.sourceDigest());
			assertEquals(a.dataDigest(), b.dataDigest());
			assertEquals(a.diagnostics(), b.diagnostics());
			assertEquals(a.transform().toString(), b.transform().toString());
			assertEquals(64, a.sourceDigest().length());
		}
		assertEquals(List.of(2, 3, 5), List.of(first.records().get(0).ordinal(),
				first.records().get(1).ordinal(), first.records().get(2).ordinal()));
		assertTrue(first.records().get(2).diagnostics()
				.contains(DxfDiagnostic.CLOSED_FLAG_CONTRADICTED));
		assertTrue(first.records().get(2).isExactlyClosed());
		// Identity transform: source and data digests coincide; another transform changes data.
		assertEquals(first.records().get(0).sourceDigest(), first.records().get(0).dataDigest());
		DxfImportReport moved = DxfSplineReader.read(bytes, "smoke-mixed.dxf",
				DxfAffineTransform.of(1, 0, 0, 1, 10, 0), "mm", DxfImportLimits.DEFAULT);
		assertEquals(first.records().get(0).sourceDigest(),
				moved.records().get(0).sourceDigest());
		assertFalse(first.records().get(0).dataDigest().equals(
				moved.records().get(0).dataDigest()));
	}

	@Test
	void committedFixturesAreTheDocumentedBuilderOutput() throws Exception {
		for (String name : DxfTestFiles.FIXTURES) {
			assertArrayEquals(DxfTestFiles.fixture(name), DxfTestFiles.resource(name), name);
		}
	}
}
