/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geocedg.common.kernel.sheet.IsoASheet;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoPolyLine;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-E3 shared-kernel contract of {@code IsoABorder}
 * ({@code geocedg/specs/sheets/iso-a-border.md}): signature, output roles, ISO 216
 * geometry, inner frame, the canonical conversion and its numerical obligations A, B
 * and C against independent {@code BigDecimal} references, reliability, coherence,
 * degenerations, dynamics, persistence and undo.
 */
class PreG9BR6PlusE3IsoABorderTest extends BaseUnitTest {

	private static final String DASH = String.valueOf((char) 0x2014);
	private static final MathContext MC = MathContext.DECIMAL128;
	private static final BigDecimal EPS = new BigDecimal(0x1p-53);
	/** Obligation B: (1+eps)^2 - 1. */
	private static final BigDecimal BOUND_B = EPS.add(BigDecimal.ONE).pow(2)
			.subtract(BigDecimal.ONE);
	/** Obligation C: 2^-50. */
	private static final BigDecimal BOUND_C = new BigDecimal(0x1p-50);
	private static final double[][] SCALES = {{1, 1}, {1, 2}, {1, 10}, {2, 1}, {1, 50},
			{1, 1E9}, {1E9, 1}, {3, 7}};

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private GeoElement[] sheet(String command) {
		ErrorAccumulator errors = new ErrorAccumulator();
		GeoElementND[] result = getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(
				command, false, errors, false, null);
		assertNotNull(result, command + " " + errors.getErrors());
		GeoElement[] out = new GeoElement[result.length];
		for (int i = 0; i < result.length; i++) {
			out[i] = result[i].toGeoElement();
		}
		return out;
	}

	private String errorsOf(String command) {
		ErrorAccumulator errors = new ErrorAccumulator();
		getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(command, false, errors,
				false, null);
		return errors.getErrors();
	}

	private static AlgoIsoABorder algo(GeoElement[] out) {
		return (AlgoIsoABorder) out[0].getParentAlgorithm();
	}

	private static double[] corners(GeoPolyLine polyLine) {
		GeoPointND[] points = polyLine.getPoints();
		double[] c = new double[points.length * 2];
		for (int i = 0; i < points.length; i++) {
			c[2 * i] = points[i].getInhomX();
			c[2 * i + 1] = points[i].getInhomY();
		}
		return c;
	}

	private static void assertBits(double expected, double actual, String message) {
		assertEquals(Double.doubleToLongBits(expected), Double.doubleToLongBits(actual),
				message + ": expected " + expected + " got " + actual);
	}

	// ------------------------------------------------------------------- contract

	@Test
	void signatureOutputsAndRoles() {
		add("P=(0,0)");
		GeoElement[] out = sheet("sheet1=IsoABorder(P,3,true,1,50,1,true)");
		assertEquals(3, out.length);
		assertTrue(out[AlgoIsoABorder.PAPER] instanceof GeoPolyLine);
		assertTrue(out[AlgoIsoABorder.FRAME] instanceof GeoPolyLine);
		assertTrue(out[AlgoIsoABorder.LABEL] instanceof GeoText);
		AlgoIsoABorder algo = algo(out);
		assertEquals(Commands.IsoABorder, algo.getClassName());
		assertEquals(EuclidianConstants.MODE_ISO_A_BORDER, algo.getRelatedModeID());
		assertEquals("sheet1", out[0].getLabelSimple());
		for (GeoElement geo : out) {
			assertSame(algo, AlgoIsoABorder.ownerOf(geo));
		}
		assertEquals(5, ((GeoPolyLine) out[0]).getNumPoints(), "closed by repetition");
		assertSame(((GeoPolyLine) out[0]).getPoint(0), ((GeoPolyLine) out[0]).getPoint(4));
		// initial presentation: hidden dotted paper, visible black frame of thickness 3
		assertFalse(out[0].isEuclidianVisible());
		assertEquals(20, out[0].getLineType());
		assertTrue(out[1].isEuclidianVisible());
		assertEquals(3, out[1].getLineThickness());
		assertTrue(out[2].isEuclidianVisible());
	}

	@Test
	void argumentCountAndTypesAreChecked() {
		add("P=(0,0)");
		assertFalse(errorsOf("IsoABorder(P,3,true,1,50,1)").isEmpty(), "six arguments");
		assertFalse(errorsOf("IsoABorder(P,3,true,1,50,1,true,1)").isEmpty(), "eight");
		assertFalse(errorsOf("IsoABorder(3,3,true,1,50,1,true)").isEmpty(), "no point");
		assertFalse(errorsOf("IsoABorder(P,3,1,1,50,1,true)").isEmpty(), "no Boolean");
		assertFalse(errorsOf("IsoABorder(P,3,true,1,50,1,1)").isEmpty(), "no Boolean");
		assertFalse(errorsOf("IsoABorder(P,(1,1),true,1,50,1,true)").isEmpty(), "no number");
	}

	// ------------------------------------------------------------ ISO geometry

	@Test
	void isoTableAllSizesBothOrientations() {
		int[][] iso = {{841, 1189}, {594, 841}, {420, 594}, {297, 420}, {210, 297},
				{148, 210}, {105, 148}, {74, 105}, {52, 74}, {37, 52}, {26, 37}};
		add("P=(0,0)");
		for (int n = 0; n <= 10; n++) {
			for (boolean landscape : new boolean[] {true, false}) {
				GeoElement[] out = sheet("IsoABorder(P," + n + "," + landscape
						+ ",1,1,1,false)");
				double[] c = corners((GeoPolyLine) out[0]);
				double w = landscape ? iso[n][1] : iso[n][0];
				double h = landscape ? iso[n][0] : iso[n][1];
				String id = "A" + n + (landscape ? "L" : "P");
				assertEquals(0, c[0], id);
				assertEquals(0, c[1], id);
				assertEquals(w, c[2], id);
				assertEquals(0, c[3], id);
				assertEquals(w, c[4], id);
				assertEquals(-h, c[5], id);
				assertEquals(0, c[6], id);
				assertEquals(-h, c[7], id);
				assertEquals(0, c[8], id);
				assertEquals(0, c[9], id);
				assertEquals(w, IsoASheet.paperWidthMm(n, landscape));
				assertEquals(h, IsoASheet.paperHeightMm(n, landscape));
			}
		}
	}

	@Test
	void referenceCaseA3LandscapeMmOneToFifty() {
		add("P=(0,0)");
		GeoElement[] out = sheet("IsoABorder(P,3,true,1,50,1,true)");
		double[] c = corners((GeoPolyLine) out[0]);
		assertEquals(21000, c[2], 0);
		assertEquals(-14850, c[5], 0);
		assertEquals("A3 " + DASH + " 1:50", ((GeoText) out[2]).getTextString());
	}

	@Test
	void referencePointAndOrientation() {
		double[][] points = {{0, 0}, {-1234.5, 987.25}, {1E6, -1E6}};
		for (double[] p : points) {
			add("P=(" + p[0] + "," + p[1] + ")");
			for (boolean landscape : new boolean[] {true, false}) {
				GeoElement[] out = sheet("IsoABorder(P,4," + landscape + ",1,1,1,false)");
				double[] c = corners((GeoPolyLine) out[0]);
				double w = landscape ? 297 : 210;
				double h = landscape ? 210 : 297;
				assertBits(p[0], c[0], "UL x");
				assertBits(p[1], c[1], "UL y");
				assertBits(p[0] + w, c[2], "UR x");
				assertBits(p[1], c[3], "UR y");
				assertBits(p[0] + w, c[4], "LR x");
				assertBits(p[1] - h, c[5], "LR y");
				assertBits(p[0], c[6], "LL x");
				assertBits(p[1] - h, c[7], "LL y");
			}
		}
	}

	@Test
	void innerFrameMarginsAndValidity() {
		add("P=(10,20)");
		GeoElement[] out = sheet("IsoABorder(P,3,true,1,50,1,true)");
		double[] f = corners((GeoPolyLine) out[1]);
		double u = 1;
		assertBits(10 + IsoASheet.conv(20, 1, 50, u), f[0], "frame left");
		assertBits(20 - IsoASheet.conv(10, 1, 50, u), f[1], "frame top");
		assertBits(f[0] + IsoASheet.conv(390, 1, 50, u), f[2], "frame right");
		assertBits(f[1] - IsoASheet.conv(277, 1, 50, u), f[5], "frame bottom");
		assertEquals(1000, f[0] - 10, 0, "20 mm at 1:50 in mm");
		for (int n = 0; n <= 10; n++) {
			for (boolean landscape : new boolean[] {true, false}) {
				boolean possible = IsoASheet.frameWidthMm(n, landscape) > 0
						&& IsoASheet.frameHeightMm(n, landscape) > 0;
				assertEquals(possible, IsoASheet.isFramePossible(n, landscape));
				assertEquals(!(n == 10 && !landscape), possible, "only A10 portrait");
				GeoElement[] s = sheet("IsoABorder(P," + n + "," + landscape + ",1,1,1,true)");
				assertEquals(possible, s[1].isDefined(), "A" + n + landscape);
				assertTrue(s[0].isDefined(), "paper independent of the frame");
				assertTrue(s[2].isDefined(), "label independent of the frame");
			}
			assertEquals(n <= 4, IsoASheet.isFrameDefault(n));
		}
		GeoElement[] off = sheet("IsoABorder(P,3,true,1,50,1,false)");
		assertFalse(off[1].isDefined());
		assertTrue(off[0].isDefined());
		assertTrue(off[2].isDefined());
	}

	@Test
	void labelAnchorDoesNotAssumeAFrame() {
		add("P=(0,0)");
		for (String frame : new String[] {"true", "false"}) {
			GeoText text = (GeoText) sheet("IsoABorder(P,3,true,1,1,1," + frame + ")")[2];
			GeoPointND anchor = text.getStartPoint();
			assertEquals(410, anchor.getInhomX(), 0);
			assertEquals(-287, anchor.getInhomY(), 0);
		}
	}

	// ------------------------------------------------------------- conversion

	@Test
	void conversionIsBitIdenticalToTheCanonicalExpression() {
		double[] units = {1E-3 / 1E-3, 1E-3 / 1E-2, 1E-3 / 1, 1E-3 / 0.0254};
		add("P=(0,0)");
		for (double u : units) {
			for (double[] s : SCALES) {
				GeoElement[] out = sheet("IsoABorder(P,3,true," + (long) s[0] + ","
						+ (long) s[1] + "," + Double.toString(u) + ",true)");
				double[] c = corners((GeoPolyLine) out[0]);
				assertBits(((420 * s[1]) / s[0]) * u, c[2], "width u=" + u);
				assertBits(-(((297 * s[1]) / s[0]) * u), c[5], "height u=" + u);
			}
		}
	}

	@Test
	void equivalentScalePairsAreBitIdentical() {
		add("P=(0.3,0.7)");
		double[] a = corners((GeoPolyLine) sheet("IsoABorder(P,3,true,1,50,0.1,true)")[0]);
		double[] b = corners((GeoPolyLine) sheet("IsoABorder(P,3,true,2,100,0.1,true)")[0]);
		for (int i = 0; i < a.length; i++) {
			assertBits(a[i], b[i], "vertex coordinate " + i);
		}
		assertEquals("A3 " + DASH + " 1:50", ((GeoText) sheet(
				"IsoABorder(P,3,true,2,100,0.1,true)")[2]).getTextString());
	}

	private static int[] allLengths() {
		List<Integer> lengths = new ArrayList<>();
		for (int n = 0; n <= 10; n++) {
			for (boolean landscape : new boolean[] {true, false}) {
				int w = IsoASheet.paperWidthMm(n, landscape);
				int h = IsoASheet.paperHeightMm(n, landscape);
				lengths.add(w);
				lengths.add(h);
				lengths.add(w - 10);
				lengths.add(h - 10);
				if (IsoASheet.isFramePossible(n, landscape)) {
					lengths.add(IsoASheet.frameWidthMm(n, landscape));
					lengths.add(IsoASheet.frameHeightMm(n, landscape));
				}
			}
		}
		lengths.add(20);
		lengths.add(10);
		return lengths.stream().mapToInt(Integer::intValue).toArray();
	}

	private static BigDecimal relative(double actual, BigDecimal exact) {
		return new BigDecimal(actual).subtract(exact).abs().divide(exact, MC);
	}

	@Test
	void numericNormalBoundsAgainstExactReferences() {
		String[][] units = {{"0.001", "0.001"}, {"0.01", "0.01"}, {"1", "1"}};
		for (String[] unit : units) {
			double fb = Double.parseDouble(unit[0]);
			BigDecimal exactFactor = new BigDecimal(unit[1]);
			double u = IsoASheet.captureUnitFactor(fb);
			for (int length : allLengths()) {
				for (double[] s : SCALES) {
					double r = IsoASheet.conv(length, s[0], s[1], u);
					BigDecimal base = new BigDecimal(length).multiply(new BigDecimal(s[1]))
							.divide(new BigDecimal(s[0]), MC);
					BigDecimal exactB = base.multiply(new BigDecimal(u), MC);
					BigDecimal exactC = base.multiply(new BigDecimal("0.001"), MC)
							.divide(exactFactor, MC);
					String id = unit[0] + " L=" + length + " " + s[0] + ":" + s[1];
					assertTrue(relative(r, exactB).compareTo(BOUND_B) <= 0, "B " + id);
					assertTrue(relative(r, exactC).compareTo(BOUND_C) <= 0, "C " + id);
				}
			}
			for (int n = 0; n <= 10; n++) {
				assertSame(IsoASheet.Reliability.GUARANTEED,
						IsoASheet.reliability(n, n % 2 == 0, 1, 1E9, u, true));
				assertSame(IsoASheet.Reliability.GUARANTEED,
						IsoASheet.reliability(n, n % 2 == 0, 1E9, 1, u, true));
			}
		}
	}

	@Test
	void numericCaptureError() {
		assertBits(1, IsoASheet.captureUnitFactor(0.001), "mm captures exactly 1");
		assertBits(0.001 / 0.01, IsoASheet.captureUnitFactor(0.01), "cm");
		assertBits(0.001, IsoASheet.captureUnitFactor(1), "m");
		String[][] cases = {{"0.01", "3.0001"}, {"1", "1.0001"}, {"0.0254", "2.0001"}};
		for (String[] item : cases) {
			double fb = Double.parseDouble(item[0]);
			double u = IsoASheet.captureUnitFactor(fb);
			// cm and m against the exact SI decimal; usm against its stored binary64 k
			BigDecimal reference = new BigDecimal("0.001").divide(
					"0.0254".equals(item[0]) ? new BigDecimal(fb) : new BigDecimal(item[0]), MC);
			BigDecimal bound = EPS.multiply(new BigDecimal(item[1]));
			assertTrue(relative(u, reference).compareTo(bound) <= 0, "capture " + item[0]);
		}
	}

	@Test
	void numericSubnormalIsDefinedButUnguaranteed() {
		add("P=(0,0)");
		// subnormal u with a normal product
		double u = 1E-310;
		GeoNumeric uNumber = add("u=1");
		GeoElement[] out = sheet("IsoABorder(P,3,true,1,1000000000,u,false)");
		uNumber.setValue(u);
		uNumber.updateCascade();
		assertTrue(u > 0 && u < Double.MIN_NORMAL);
		assertTrue(out[0].isDefined());
		AlgoIsoABorder algo = algo(out);
		assertSame(IsoASheet.Reliability.UNGUARANTEED, algo.getReliability());
		assertBits(((420 * 1E9) / 1) * u, corners((GeoPolyLine) out[0])[2], "as computed");
		// normal u, subnormal product: (26 / 1e9) * 1e-301 = 2.6e-309
		double v = 1E-301;
		GeoNumeric vNumber = add("v=1");
		GeoElement[] small = sheet("IsoABorder(P,10,true,1000000000,1,v,false)");
		vNumber.setValue(v);
		vNumber.updateCascade();
		assertTrue(v >= Double.MIN_NORMAL);
		assertSame(IsoASheet.Reliability.UNGUARANTEED, algo(small).getReliability());
		assertTrue(small[0].isDefined());
		assertSame(IsoASheet.PhysicalCoherence.NOT_DETERMINABLE,
				algo(small).coherence(1E300, 1000000000, 1).getPhysical());
	}

	@Test
	void numericExtremeUsmFactors() {
		assertTrue(IsoASheet.captureUnitFactor(1E300) >= Double.MIN_NORMAL, "1e300 normal");
		double sub = IsoASheet.captureUnitFactor(1E305);
		assertTrue(sub > 0 && sub < Double.MIN_NORMAL, "1e305 subnormal");
		assertSame(IsoASheet.Reliability.UNGUARANTEED,
				IsoASheet.reliability(3, true, 1, 1, sub, false));
		assertTrue(IsoASheet.isUnitFactor(IsoASheet.captureUnitFactor(1E-311)), "huge, finite");
		assertFalse(IsoASheet.isUnitFactor(IsoASheet.captureUnitFactor(1E-313)),
				"1e-313 overflows: capture refused");
		assertFalse(IsoASheet.isUnitFactor(IsoASheet.captureUnitFactor(-1)));
		assertFalse(IsoASheet.isUnitFactor(Double.NaN));
		assertFalse(IsoASheet.isUnitFactor(0));
	}

	@Test
	void numericOverflowAndUnderflowAreUndefinedAndRecover() {
		add("P=(0,0)");
		GeoNumeric u = add("u=1");
		GeoElement[] out = sheet("IsoABorder(P,0,false,1,1000000000,u,true)");
		u.setValue(1E297);
		u.updateCascade();
		assertFalse(out[0].isDefined(), "overflow");
		assertFalse(out[2].isDefined());
		u.setValue(Double.MIN_VALUE);
		u.updateCascade();
		GeoElement[] under = sheet("IsoABorder(P,10,true,1000000000,1,u,false)");
		assertFalse(under[0].isDefined(), "underflow to zero");
		u.setValue(1);
		u.updateCascade();
		assertTrue(out[0].isDefined(), "recovery keeps the same objects");
		assertTrue(under[0].isDefined());
	}

	@Test
	void numericCoordinateAbsorption() {
		add("P=(1E20,0)");
		GeoElement[] out = sheet("IsoABorder(P,10,true,1,1,1,false)");
		assertFalse(out[0].isDefined(), "1e20 + 37 is absorbed");
		add("Q=(1E15,1E15)");
		GeoElement[] far = sheet("IsoABorder(Q,3,true,1,1,1,false)");
		assertTrue(far[0].isDefined(), "not absorbed: defined");
		assertSame(IsoASheet.PhysicalCoherence.NOT_DETERMINABLE,
				algo(far).coherence(0.001, 1, 1).getPhysical(),
				"realized corners too uncertain for 1e-6 mm");
		add("R=(0,0)");
		assertSame(IsoASheet.PhysicalCoherence.COHERENT,
				algo(sheet("IsoABorder(R,3,true,1,1,1,false)")).coherence(0.001, 1, 1)
						.getPhysical());
	}

	@Test
	void numericReproducibilityAcrossSaveReopenAndUndo() {
		activateUndo();
		add("P=(0.1,0.2)");
		getApp().storeUndoInfo();
		sheet("s1=IsoABorder(P,3,true,1,50," + Double.toString(1E-3 / 0.0254) + ",true)");
		getApp().storeUndoInfo();
		double[] before = corners((GeoPolyLine) lookup("s1"));
		reload();
		double[] reopened = corners((GeoPolyLine) lookup("s1"));
		for (int i = 0; i < before.length; i++) {
			assertBits(before[i], reopened[i], "reopen " + i);
		}
		getKernel().undo();
		assertNull(lookup("s1"));
		getKernel().redo();
		double[] redone = corners((GeoPolyLine) lookup("s1"));
		for (int i = 0; i < before.length; i++) {
			assertBits(before[i], redone[i], "redo " + i);
		}
	}

	// --------------------------------------------------------------- coherence

	@Test
	void coherenceExamplesAreTwoIndependentStates() {
		add("P=(0,0)");
		AlgoIsoABorder algo = algo(sheet("IsoABorder(P,3,true,1,50,1,true)"));
		IsoASheet.Coherence same = algo.coherence(0.001, 1, 50);
		assertSame(IsoASheet.PhysicalCoherence.COHERENT, same.getPhysical());
		assertSame(IsoASheet.ScaleCoherence.MATCH, same.getScale());
		IsoASheet.Coherence half = algo.coherence(0.001, 1, 100);
		assertSame(IsoASheet.PhysicalCoherence.INCOHERENT, half.getPhysical());
		assertSame(IsoASheet.ScaleCoherence.DIFFERENT, half.getScale());
		assertEquals(210, half.getEffectiveWidthMm(), 1E-9);
		assertEquals(148.5, half.getEffectiveHeightMm(), 1E-9);
		IsoASheet.Coherence cm = algo.coherence(0.01, 1, 50);
		assertSame(IsoASheet.PhysicalCoherence.INCOHERENT, cm.getPhysical());
		assertSame(IsoASheet.ScaleCoherence.MATCH, cm.getScale());
		assertEquals(4200, cm.getEffectiveWidthMm(), 1E-9);
		IsoASheet.Coherence cm500 = algo.coherence(0.01, 1, 500);
		assertSame(IsoASheet.PhysicalCoherence.COHERENT, cm500.getPhysical(),
				"physical coherence can hold while the label scale differs");
		assertSame(IsoASheet.ScaleCoherence.DIFFERENT, cm500.getScale());
		assertSame(IsoASheet.ScaleCoherence.MATCH, algo.coherence(0.001, 2, 100).getScale());
	}

	@Test
	void coherenceIsNeverClaimedWhenUndeterminable() {
		add("P=(0,0)");
		AlgoIsoABorder algo = algo(sheet("IsoABorder(P,3,true,1,50,1,true)"));
		IsoASheet.Coherence unspecified = algo.coherence(Double.NaN, 1, 50);
		assertSame(IsoASheet.PhysicalCoherence.NOT_DETERMINABLE, unspecified.getPhysical());
		assertSame(IsoASheet.ScaleCoherence.NOT_APPLICABLE, unspecified.getScale());
		GeoNumeric u = add("u=0.0393700787401574");
		AlgoIsoABorder inch = algo(sheet("IsoABorder(P,4,false,1,2,u,true)"));
		assertSame(IsoASheet.PhysicalCoherence.COHERENT, inch.coherence(0.0254, 1, 2)
				.getPhysical());
		u.setValue(-1);
		u.updateCascade();
		assertSame(IsoASheet.PhysicalCoherence.NOT_DETERMINABLE,
				inch.coherence(0.0254, 1, 2).getPhysical(), "paper undefined");
		assertSame(IsoASheet.PhysicalCoherence.NOT_DETERMINABLE,
				algo.coherence(Double.POSITIVE_INFINITY, 1, 50).getPhysical());
		assertSame(IsoASheet.PhysicalCoherence.NOT_DETERMINABLE,
				algo.coherence(1E300, 1000000000, 1).getPhysical(), "non-finite page");
	}

	// ---------------------------------------------------------- degenerations

	@Test
	void degenerationsKeepIdentitiesAndRecover() {
		GeoPoint p = add("P=(0,0)");
		GeoNumeric n = add("n=3");
		GeoNumeric a = add("a=1");
		GeoNumeric b = add("b=50");
		GeoNumeric u = add("u=1");
		final GeoBoolean frame = add("f=true");
		GeoElement[] out = sheet("IsoABorder(P,n,true,a,b,u,f)");
		Object[][] invalid = {{n, -1.0}, {n, 11.0}, {n, 3.5}, {n, Double.NaN}, {a, 0.0},
				{a, -1.0}, {a, 1.5}, {b, 1E10}, {u, 0.0}, {u, -1.0}, {u, Double.NaN},
				{u, Double.POSITIVE_INFINITY}};
		for (Object[] item : invalid) {
			GeoNumeric number = (GeoNumeric) item[0];
			double previous = number.getDouble();
			number.setValue((Double) item[1]);
			number.updateCascade();
			String id = number.getLabelSimple() + "=" + item[1];
			assertFalse(out[0].isDefined(), id);
			assertFalse(out[1].isDefined(), id);
			assertFalse(out[2].isDefined(), id);
			number.setValue(previous);
			number.updateCascade();
			assertTrue(out[0].isDefined(), "recovered " + id);
			assertTrue(out[1].isDefined(), "recovered " + id);
		}
		p.setUndefined();
		p.updateCascade();
		assertFalse(out[0].isDefined());
		p.setCoords(1, 1, 1);
		p.updateCascade();
		assertTrue(out[0].isDefined());
		frame.setValue(false);
		frame.updateCascade();
		assertFalse(out[1].isDefined());
		assertTrue(out[0].isDefined());
		assertTrue(out[2].isDefined());
		AlgoIsoABorder algo = algo(out);
		assertSame(algo.getPaper(), out[0]);
		assertSame(algo.getFrame(), out[1]);
		assertSame(algo.getLabelText(), out[2]);
	}

	// ----------------------------------------------------------------- dynamics

	@Test
	void movingThePointTranslatesWithoutResizing() {
		GeoPoint p = add("P=(0,0)");
		GeoElement[] out = sheet("IsoABorder(P,4,true,1,10,1,true)");
		GeoElement vertex = add("V=Vertex(" + out[0].getLabelSimple() + ",3)");
		double[] before = corners((GeoPolyLine) out[0]);
		p.setCoords(5, -7, 1);
		p.updateCascade();
		double[] after = corners((GeoPolyLine) out[0]);
		assertEquals(before[2] - before[0], after[2] - after[0], 0, "width unchanged");
		assertEquals(5, after[0], 0);
		assertEquals(-7, after[1], 0);
		assertEquals(after[4], ((GeoPoint) vertex).getInhomX(), 0, "Vertex follows");
		assertSame(out[0], lookup(out[0].getLabelSimple()), "same object");
	}

	@Test
	void unitStateChangesNothingGeometric() {
		add("P=(0,0)");
		GeoElement[] out = sheet("IsoABorder(P,3,true,1,50,1,true)");
		String xmlBefore = commandXml();
		double[] before = corners((GeoPolyLine) out[0]);
		String text = ((GeoText) out[2]).getTextString();
		for (UnitState state : new UnitState[] {UnitState.of(UnitToken.CM, null, null),
				UnitState.of(UnitToken.USM, null, UsmDefinition.of(0.0254, "inch", "in")),
				UnitState.EMPTY}) {
			getConstruction().getUnitSystem().replace(state);
			double[] now = corners((GeoPolyLine) out[0]);
			for (int i = 0; i < now.length; i++) {
				assertBits(before[i], now[i], state + " " + i);
			}
			assertEquals(text, ((GeoText) out[2]).getTextString());
			assertEquals(xmlBefore, commandXml());
		}
	}

	private String commandXml() {
		String xml = getApp().getXML();
		int start = xml.indexOf("<command name=\"IsoABorder\">");
		return xml.substring(start, xml.indexOf("</command>", start));
	}

	// -------------------------------------------------------------- persistence

	@Test
	void serializationUsesExistingElementTypesAndRoundTrips() {
		add("P=(0,0)");
		sheet("sheet1=IsoABorder(P,3,true,1,50,1,true)");
		String xml = getApp().getXML();
		assertTrue(xml.contains("<command name=\"IsoABorder\">"), xml);
		assertTrue(xml.contains("a1=\"3\"") && xml.contains("a2=\"true\"")
				&& xml.contains("a4=\"50\"") && xml.contains("a6=\"true\""), xml);
		Matcher m = Pattern.compile("<element type=\"([^\"]+)\"").matcher(xml);
		while (m.find()) {
			assertTrue(List.of("point", "polyline", "text").contains(m.group(1)),
					"existing element type only: " + m.group(1));
		}
		reload();
		assertEquals(xml, getApp().getXML(), "byte-identical round trip");
		GeoElement paper = lookup("sheet1");
		assertTrue(paper.getParentAlgorithm() instanceof AlgoIsoABorder);
		assertFalse(paper.isEuclidianVisible(), "hidden after reopen");
	}

	@Test
	void determinism() {
		add("P=(0,0)");
		sheet("IsoABorder(P,2,false,1,20,0.1,true)");
		String first = getApp().getXML();
		reload();
		getApp().getKernel().clearConstruction(true);
		add("P=(0,0)");
		sheet("IsoABorder(P,2,false,1,20,0.1,true)");
		assertEquals(first, getApp().getXML());
	}
}
