/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.ConstructionFidelity;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.metric.FiniteMetricValue2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricDiagnostic2D;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricEnclosure2D;
import org.geocedg.common.kernel.locus.metric.MetricErrorEvidence2D;
import org.geocedg.common.kernel.locus.metric.MetricEvaluatorMethod2D;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geocedg.common.kernel.locus.metric.MetricMethod2D;
import org.geocedg.common.kernel.locus.metric.MetricProvenance2D;
import org.geocedg.common.kernel.locus.metric.MetricRectifiability;
import org.geocedg.common.kernel.locus.metric.MetricRepresentationRole2D;
import org.geocedg.common.kernel.locus.metric.MetricUnit2D;
import org.geocedg.common.kernel.locus.metric.PolicyV2LocusMetricCapability2D;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU1 metric guarantee classes ({@code locus-v2-metrics.md} §25; validation
 * matrix V-MET-STRUCT-1..3, V-MET-ENCL-1..2, V-TRANS-1, V-CERT-RIGOR and V-ADM-1).
 * Every certified bound is checked against an independent reference computed in
 * exact decimal arithmetic or by an independent dense quadrature.
 */
class PreG9BCu1MetricClassesTest extends G9U0PublicSurfaceTestBase {

	private static final double[] RADII = {1, 7, 8, 20, 1E3, 1E-3};
	private static final MathContext DIGITS = new MathContext(40);
	private static final MathContext UP = new MathContext(60, RoundingMode.UP);
	private static final MathContext DOWN = new MathContext(60, RoundingMode.DOWN);
	private static final BigDecimal TWO_PI = new BigDecimal(
			"6.283185307179586476925286766559005768394338798750211641949889");

	/** V-MET-STRUCT-1, V-CERT-RIGOR: driver similarities of circles (H11). */
	@Test
	void driverSimilaritiesOfCirclesAreStructural() {
		add("O=(0,0)");
		String[][] forms = {{"P%1$d+(0,0)", "1"}, {"Rotate(P%1$d,0.7,O)", "1"},
				{"Dilate(P%1$d,2.5,O)", "2.5"}, {"Dilate(P%1$d,-0.5,O)", "0.5"},
				{"Midpoint(P%1$d,O)", "0.5"}};
		int index = 0;
		for (double radius : RADII) {
			for (String[] form : forms) {
				index++;
				add("c" + index + "=Circle((1,-2)," + decimal(radius) + ")");
				add("P" + index + "=Point(c" + index + ")");
				add("Q" + index + "=" + String.format(Locale.ROOT, form[0], index));
				GeoLocusV2 locus = add("L" + index + "=LocusV2(Q" + index + ",P"
						+ index + ")");
				final long calls = locus.getMetricInstrumentation().snapshot()
						.getEvaluatorCalls();
				LocusMetricResult2D rich = rich("M" + index + "=LocusLength(L"
						+ index + ")");
				String where = form[0] + " radius " + radius + ": " + describe(rich);
				assertEquals(MetricGuaranteeClass2D.STRUCTURAL, rich.getGuaranteeClass(),
						where);
				assertEquals(NumericGuarantee.CERTIFIED_ERROR_BOUND,
						rich.getErrorEvidence().getNumericGuarantee().orElseThrow(), where);
				assertEquals(MetricMethod2D.CLOSED_FORM, rich.getMetricMethod(), where);
				assertContainsExactCircleLength(locus, rich, where);
				// Geometric truth: 2 pi lambda r, up to the rounding of the driver domain.
				double truth = 2 * Math.PI * Double.parseDouble(form[1]) * radius;
				assertEquals(truth, value(rich), 1E-14 * truth, where);
				assertAdmissible(rich, where);
				assertNoWorkLimit(rich, where);
				// Class 1 spends only the minimum partition samples (2^6 + 1).
				assertTrue(locus.getMetricInstrumentation().snapshot().getEvaluatorCalls()
						- calls <= 65, where);
			}
		}
	}

	/**
	 * V-INV-1 for class 1 and V-CERT-RIGOR: the structural class does not depend on the
	 * position of the curve, and a length factor whose product overflows has no rigorous
	 * enclosure, so the certification fails closed instead of publishing a bound.
	 */
	@Test
	void structuralClassIsTranslationRobustAndFailsClosedOnOverflow() {
		add("cf=Circle((1E6,-1E6),1)");
		add("Pf=Point(cf)");
		add("Qf=Pf+(0,0)");
		add("Lf=LocusV2(Qf,Pf)");
		LocusMetricResult2D far = rich("Mf=LocusLength(Lf)");
		assertEquals(MetricGuaranteeClass2D.STRUCTURAL, far.getGuaranteeClass(),
				describe(far));
		assertContainsExactCircleLength((GeoLocusV2) lookup("Lf"), far, describe(far));
		assertEquals(2 * Math.PI, value(far), 1E-14 * 2 * Math.PI, describe(far));
		assertAdmissible(far, "far circle");

		add("k=1E308");
		add("Lo=Dilate(Lf,k,(1E6,-1E6))");
		LocusMetricResult2D overflow = rich("Mo=LocusLength(Lo)");
		String where = "overflowing factor: " + describe(overflow);
		assertNotEquals(MetricGuaranteeClass2D.STRUCTURAL, overflow.getGuaranteeClass(),
				where);
		assertNotEquals(MetricGuaranteeClass2D.CERTIFIED_ENCLOSURE,
				overflow.getGuaranteeClass(), where);
		assertFalse(overflow.isScalarAdmissible(), where);
	}

	/** V-MET-STRUCT-2, V-CERT-RIGOR: affine images {@code |a|·Δu}. */
	@Test
	void affineImagesAreStructural() {
		add("s=Segment((1,1),(4,5))");
		add("P=Point(s)");
		add("Q=Dilate(P,2,(0,0))");
		add("Ls=LocusV2(Q,P)");
		LocusMetricResult2D segment = rich("Ms=LocusLength(Ls)");
		assertEquals(MetricGuaranteeClass2D.STRUCTURAL, segment.getGuaranteeClass(),
				describe(segment));
		assertContains(enclosure(segment), BigDecimal.TEN, describe(segment));
		assertAdmissible(segment, "segment image");

		createScalarLocus("La", "u", "Qa", "(u,u)", "{false,{-2,2,true,true}}");
		LocusMetricResult2D affine = rich("Ma=LocusLength(La)");
		assertEquals(MetricGuaranteeClass2D.STRUCTURAL, affine.getGuaranteeClass(),
				describe(affine));
		assertContains(enclosure(affine), new BigDecimal(2).sqrt(DIGITS)
				.multiply(new BigDecimal(4)), describe(affine));
		assertAdmissible(affine, "scalar affine");

		// Class 1 needs provider-owned certificate material: an affine expression outside
		// the certified direct forms stays an honest estimate of the same length.
		add("w=0");
		add("Qb=(2w+1,3w-1)");
		add("Db={false,{-2,2,true,true}}");
		add("Lb=LocusV2(Qb,w,Db)");
		LocusMetricResult2D expression = rich("Mb=LocusLength(Lb)");
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, expression.getGuaranteeClass(),
				describe(expression));
		assertTrue(expression.getErrorEvidence().getRigorousEnclosure().isEmpty(),
				describe(expression));
		assertEquals(4 * Math.sqrt(13), value(expression),
				expression.getErrorEvidence().getEstablishedAbsoluteAmount()
						+ 4 * Math.ulp(4 * Math.sqrt(13)), describe(expression));
		assertAdmissible(expression, "affine expression");
	}

	/**
	 * V-MET-STRUCT-3, V-TRANS-1 (N1): a similarity image keeps the class of its
	 * source and scales value and error by the length factor; a zero dilation is
	 * an exact zero.
	 */
	@Test
	void similarityImagesKeepTheSourceClassAndScaleValueAndError() {
		add("c=Circle((1,2),3)");
		add("P=Point(c)");
		add("Q=P+(0,0)");
		GeoLocusV2 circle = add("L=LocusV2(Q,P)");
		add("s=Segment((0,0),(1,0))");
		add("R=Point(s)");
		add("W=(x(R),sin(2*pi*x(R)))");
		GeoLocusV2 sine = add("K=LocusV2(W,R)");
		String[][] images = {{"Translate(%s,(3,-2))", "1"},
				{"Rotate(%s,pi/3,(1,2))", "1"}, {"Reflect(%s,y=x)", "1"},
				{"Reflect(%s,(1,2))", "1"}, {"Dilate(%s,2,(1,1))", "2"},
				{"Dilate(%s,-3,(1,2))", "3"}};
		for (GeoLocusV2 source : List.of(circle, sine)) {
			String name = source.getLabelSimple();
			LocusMetricResult2D base = rich("M" + name + "=LocusLength(" + name + ")");
			MetricGuaranteeClass2D expected = source == circle
					? MetricGuaranteeClass2D.STRUCTURAL : MetricGuaranteeClass2D.ESTIMATED;
			assertEquals(expected, base.getGuaranteeClass(), describe(base));
			double baseValue = value(base);
			double baseError = base.getErrorEvidence().getEstablishedAbsoluteAmount();
			int index = 0;
			for (String[] image : images) {
				index++;
				String label = name + "i" + index;
				add(label + "=" + String.format(Locale.ROOT, image[0], name));
				LocusMetricResult2D transformed = rich("N" + label + "=LocusLength("
						+ label + ")");
				double factor = Double.parseDouble(image[1]);
				String where = image[0] + " of " + name + ": " + describe(transformed);
				assertEquals(base.getGuaranteeClass(), transformed.getGuaranteeClass(),
						where);
				double scaled = factor * baseValue;
				assertEquals(scaled, value(transformed), 1E-12 * scaled, where);
				double error = transformed.getErrorEvidence()
						.getEstablishedAbsoluteAmount();
				assertTrue(error >= factor * baseError * (1 - 1E-12), where);
				assertTrue(error <= factor * baseError * (1 + 1E-12)
						+ 16 * Math.ulp(scaled), where);
				assertAdmissible(transformed, where);
			}
			add(name + "z=Dilate(" + name + ",0,(4,5))");
			LocusMetricResult2D zero = rich("Z" + name + "=LocusLength(" + name + "z)");
			assertEquals(0, value(zero), 0, describe(zero));
			assertEquals(NumericGuarantee.EXACT_ARITHMETIC,
					zero.getErrorEvidence().getNumericGuarantee().orElseThrow());
			assertEquals(MetricGuaranteeClass2D.STRUCTURAL, zero.getGuaranteeClass());
		}
	}

	/**
	 * V-MET-ENCL-1, V-CERT-RIGOR: construction programs of classes v1/v2 either
	 * get a certified enclosure within the tolerance or carry it as evidence of
	 * their estimate; the enclosure contains the independent reference and the
	 * published value.
	 */
	@Test
	void constructionProgramsAreCertifiedOrCarryTheirEnclosure() {
		add("A=(-3,-1)");
		add("B=(4,2)");
		add("l=Line(A,B)");
		add("c=Circle((0.5,0.25),2)");
		add("P=Point(c)");
		add("f=PerpendicularLine(P,l)");
		add("F=Intersect(f,l)");
		add("Lf=LocusV2(F,P)");
		// The foot traverses the projection of the circle, of length 2r, twice.
		LocusMetricResult2D foot = rich("Mf=LocusLength(Lf)");
		assertCertifiedOrEvidence(foot, new BigDecimal(8), "perpendicular foot");
		// The foot reverses its direction twice; the located reversals keep the
		// estimate within its declared error.
		assertEquals(8, value(foot), foot.getErrorEvidence()
				.getEstablishedAbsoluteAmount() + 4 * Math.ulp(8.0), describe(foot));

		add("O=(0,0)");
		add("R=Rotate(P,pi/2,O)");
		add("N=Midpoint(P,R)");
		add("Lm=LocusV2(N,P)");
		// (P + R(P))/2 traces a circle of radius r/sqrt(2).
		LocusMetricResult2D middle = rich("Mm=LocusLength(Lm)");
		assertCertifiedOrEvidence(middle, TWO_PI.multiply(new BigDecimal(2))
				.divide(new BigDecimal(2).sqrt(DIGITS), DIGITS), "two-point midpoint");
	}

	/** V-MET-ENCL-2: SplineV2 of degrees 3 and 7. */
	@Test
	void splineLengthsLieInsideTheirCertifiedEvidence() {
		GeoLocusV2 cubic = add("S3=SplineV2({(0,0),(1,2),(3,1),(4,3),(6,0)},3)");
		GeoLocusV2 septic = add(
				"S7=SplineV2({(0,0),(1,2),(2,-1),(3,1),(4,3),(5,0),(6,2),(7,1)},7)");
		for (GeoLocusV2 spline : List.of(cubic, septic)) {
			String label = spline.getLabelSimple();
			LocusMetricResult2D rich = rich("M" + label + "=LocusLength(" + label + ")");
			double reference = denseChordLength(spline);
			assertCertifiedOrEvidence(rich, new BigDecimal(reference), label);
			assertEquals(reference, value(rich), 1E-8 * reference, describe(rich));
			assertAdmissible(rich, label);
		}
	}

	/** V-ADM-1: the scalar adapter publishes exactly the admissible rich values. */
	@Test
	void scalarAdapterPublishesExactlyTheAdmissibleRichValues() {
		GeoLocusV2 line = createLine();
		GeoNumeric scalar = scalarLength(line);
		LocusMetricResult2D lineRich = rich("Ml=LocusLength(L)");
		assertEquals(MetricGuaranteeClass2D.STRUCTURAL, lineRich.getGuaranteeClass());
		assertTrue(scalar.isDefined());
		assertEquals(value(lineRich), scalar.getValue(), 0);

		add("t=0");
		add("Qt=(t,sin(t))");
		add("Dt={false,{0,3,true,true}}");
		add("Lt=LocusV2(Qt,t,Dt)");
		GeoNumeric estimated = add("b=Length(Lt)");
		LocusMetricResult2D estimate = rich("Mt=LocusLength(Lt)");
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, estimate.getGuaranteeClass());
		assertTrue(estimated.isDefined());
		assertEquals(value(estimate), estimated.getValue(), 0);

		double tolerance = 1E-9;
		assertTrue(gate(MetricErrorEvidence2D.estimated(tolerance, 0, "estimate",
				List.of("hypothesis")).withRequestedTolerance(tolerance))
				.isScalarAdmissible());
		assertFalse(gate(MetricErrorEvidence2D.estimated(2 * tolerance, 0, "estimate",
				List.of("hypothesis")).withRequestedTolerance(tolerance))
				.isScalarAdmissible());
		assertTrue(gate(MetricErrorEvidence2D.certified(tolerance, 0, "enclosure",
				"certificate").withRequestedTolerance(tolerance)).isScalarAdmissible());
		assertFalse(gate(MetricErrorEvidence2D.certified(2 * tolerance, 0,
				"enclosure", "certificate").withRequestedTolerance(tolerance))
				.isScalarAdmissible());
		assertFalse(gate(MetricErrorEvidence2D.uncertified("uncertified")
				.withRequestedTolerance(tolerance)).isScalarAdmissible());
		assertTrue(gate(MetricErrorEvidence2D.exact("zero")
				.withRequestedTolerance(0)).isScalarAdmissible());
	}

	private LocusMetricResult2D rich(String command) {
		GeoLocusMetricResult result = add(command);
		assertNotNull(result, command);
		assertNotNull(result.getMetricResult(), command);
		return result.getMetricResult();
	}

	private static void assertCertifiedOrEvidence(LocusMetricResult2D rich,
			BigDecimal reference, String where) {
		String message = where + ": " + describe(rich);
		MetricGuaranteeClass2D guarantee = rich.getGuaranteeClass();
		assertTrue(guarantee == MetricGuaranteeClass2D.CERTIFIED_ENCLOSURE
				|| guarantee == MetricGuaranteeClass2D.ESTIMATED, message);
		MetricEnclosure2D enclosure = enclosure(rich);
		assertContains(enclosure, reference, message);
		assertTrue(enclosure.contains(value(rich)), message);
		if (guarantee == MetricGuaranteeClass2D.CERTIFIED_ENCLOSURE) {
			assertEquals(MetricMethod2D.CERTIFIED_CONE_ENCLOSURE, rich.getMetricMethod(),
					message);
			assertAdmissible(rich, message);
		}
	}

	/**
	 * V-CERT-RIGOR reference for a circle-driver locus: the exact semantic length is
	 * re-derived from the captured certified program in exact decimal arithmetic. The
	 * speed is {@code |P (-sin t, cos t)|} with {@code P = M D}, so it lies between the
	 * square roots of the eigenvalues {@code m - rho} and {@code m + rho} of
	 * {@code P^T P}; the certified enclosure must contain the whole exact interval.
	 */
	private static void assertContainsExactCircleLength(GeoLocusV2 locus,
			LocusMetricResult2D rich, String where) {
		LocusDefinition2D definition = locus.getSemanticDefinition();
		CertifiedConstructionProgram2D program = ((ReconstructibleLocusEvaluator2D)
				definition.getEvaluatorCapability()).captureCertifiedProgram()
						.orElseThrow(() -> new AssertionError("no program: " + where));
		assertEquals(CertifiedConstructionProgram2D.Driver.CIRCLE, program.getDriver());
		BigDecimal[][] linear = {{BigDecimal.ONE, BigDecimal.ZERO},
				{BigDecimal.ZERO, BigDecimal.ONE}};
		for (Step step : program.getSteps()) {
			linear = product(exactLinearPart(step), linear);
		}
		BigDecimal[][] circle = {
				{exact(program, 0).multiply(exact(program, 2)),
						exact(program, 1).multiply(exact(program, 4))},
				{exact(program, 0).multiply(exact(program, 3)),
						exact(program, 1).multiply(exact(program, 5))}};
		BigDecimal[][] p = product(linear, circle);
		BigDecimal g11 = p[0][0].pow(2).add(p[1][0].pow(2));
		BigDecimal g22 = p[0][1].pow(2).add(p[1][1].pow(2));
		BigDecimal g12 = p[0][0].multiply(p[0][1]).add(p[1][0].multiply(p[1][1]));
		BigDecimal half = new BigDecimal("0.5");
		BigDecimal mean = g11.add(g22).multiply(half);
		BigDecimal spread = g11.subtract(g22).multiply(half);
		BigDecimal radius = spread.pow(2).add(g12.pow(2)).sqrt(UP);
		LocusInterval2D component = definition.getBranches().get(0)
				.getValidDomainComponents().get(0);
		BigDecimal width = new BigDecimal(component.getUpper())
				.subtract(new BigDecimal(component.getLower()));
		BigDecimal lower = mean.subtract(radius).max(BigDecimal.ZERO).sqrt(DOWN)
				.multiply(width);
		BigDecimal upper = mean.add(radius).sqrt(UP).multiply(width);
		MetricEnclosure2D enclosure = enclosure(rich);
		assertTrue(new BigDecimal(enclosure.getLower()).compareTo(lower) <= 0
				&& upper.compareTo(new BigDecimal(enclosure.getUpper())) <= 0,
				where + ": " + enclosure + " does not contain [" + lower + ", " + upper
						+ "]");
	}

	private static BigDecimal[][] exactLinearPart(Step step) {
		BigDecimal one = BigDecimal.ONE;
		BigDecimal zero = BigDecimal.ZERO;
		switch (step.getOperation()) {
		case TRANSLATE:
		case EXPRESSION_TRANSLATE:
			return new BigDecimal[][] {{one, zero}, {zero, one}};
		case ROTATE:
		case ROTATE_ABOUT:
			BigDecimal cosine = new BigDecimal(step.getParameter(0));
			BigDecimal sine = new BigDecimal(step.getParameter(1));
			return new BigDecimal[][] {{cosine, sine.negate()}, {sine, cosine}};
		case MIRROR_POINT:
			return new BigDecimal[][] {{one.negate(), zero}, {zero, one.negate()}};
		case MIRROR_LINE:
			BigDecimal c = new BigDecimal(step.getParameter(4));
			BigDecimal s = new BigDecimal(step.getParameter(5));
			return new BigDecimal[][] {{c, s}, {s, c.negate()}};
		case DILATE:
			BigDecimal factor = new BigDecimal(step.getParameter(0));
			return new BigDecimal[][] {{factor, zero}, {zero, factor}};
		case MIDPOINT:
			BigDecimal half = new BigDecimal("0.5");
			return new BigDecimal[][] {{half, zero}, {zero, half}};
		default:
			throw new AssertionError("not a point similarity: " + step.getOperation());
		}
	}

	private static BigDecimal[][] product(BigDecimal[][] left, BigDecimal[][] right) {
		BigDecimal[][] result = new BigDecimal[2][2];
		for (int row = 0; row < 2; row++) {
			for (int column = 0; column < 2; column++) {
				result[row][column] = left[row][0].multiply(right[0][column])
						.add(left[row][1].multiply(right[1][column]));
			}
		}
		return result;
	}

	private static BigDecimal exact(CertifiedConstructionProgram2D program, int index) {
		return new BigDecimal(program.getDriverConstant(index));
	}

	private static void assertContains(MetricEnclosure2D enclosure,
			BigDecimal reference, String where) {
		assertTrue(new BigDecimal(enclosure.getLower()).compareTo(reference) <= 0
				&& reference.compareTo(new BigDecimal(enclosure.getUpper())) <= 0,
				where + ": " + enclosure + " does not contain " + reference);
	}

	private static void assertAdmissible(LocusMetricResult2D rich, String where) {
		double tolerance = rich.getErrorEvidence().getRequestedTolerance().orElseThrow();
		assertTrue(rich.getErrorEvidence().getEstablishedAbsoluteAmount() <= tolerance,
				where);
		assertTrue(rich.isScalarAdmissible(), where);
	}

	private static void assertNoWorkLimit(LocusMetricResult2D rich, String where) {
		for (MetricDiagnostic2D diagnostic : rich.getDiagnostics()) {
			assertFalse(diagnostic.getCode()
					== MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED, where);
		}
	}

	private static MetricEnclosure2D enclosure(LocusMetricResult2D rich) {
		Optional<MetricEnclosure2D> enclosure =
				rich.getErrorEvidence().getRigorousEnclosure();
		assertTrue(enclosure.isPresent(), describe(rich));
		return enclosure.get();
	}

	private static double value(LocusMetricResult2D rich) {
		return rich.getMetricValue().getFiniteValue().orElseThrow(
				() -> new AssertionError(describe(rich)));
	}

	private static String describe(LocusMetricResult2D rich) {
		return rich.getGuaranteeClass() + " " + rich.getComputationStatus() + " "
				+ rich.getMetricValue().getFiniteValue() + " "
				+ rich.getErrorEvidence().getMethod() + " "
				+ rich.getErrorEvidence().getRigorousEnclosure() + " "
				+ rich.getDiagnostics();
	}

	private static String decimal(double value) {
		return new BigDecimal(value).toPlainString();
	}

	/**
	 * Independent reference: Richardson extrapolation of uniform chord sums of
	 * the semantic curve with 2^17 and 2^18 cells per valid component.
	 */
	private static double denseChordLength(GeoLocusV2 locus) {
		LocusDefinition2D definition = locus.getSemanticDefinition();
		double total = 0;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.reference()) {
			for (LocusBranch2D branch : definition.getBranches()) {
				for (LocusInterval2D component : branch.getValidDomainComponents()) {
					double coarse = chordSum(definition, branch.getBranchKey(),
							component, 1 << 17, session);
					double fine = chordSum(definition, branch.getBranchKey(), component,
							1 << 18, session);
					total += fine + (fine - coarse) / 3;
				}
			}
		}
		return total;
	}

	private static double chordSum(LocusDefinition2D definition, String branchKey,
			LocusInterval2D component, int cells, LocusEvaluationSession2D session) {
		double sum = 0;
		LocusPoint2D previous = null;
		for (int index = 0; index <= cells; index++) {
			double parameter = index == cells ? component.getUpper()
					: component.getLower() + (component.getUpper()
							- component.getLower()) * ((double) index / cells);
			LocusEvaluation2D evaluation = definition.evaluate(branchKey, parameter,
					session);
			assertTrue(evaluation.isValid(), evaluation.getDiagnostic());
			LocusPoint2D point = evaluation.getPoint();
			if (previous != null) {
				sum += Math.hypot(point.getX() - previous.getX(),
						point.getY() - previous.getY());
			}
			previous = point;
		}
		return sum;
	}

	private static LocusMetricResult2D gate(MetricErrorEvidence2D evidence) {
		return new LocusMetricResult2D(new FiniteMetricValue2D(1),
				MetricCoverage.COMPLETE, MetricComputationStatus.SUCCESS,
				MetricRectifiability.RECTIFIABLE, Optional.empty(),
				ConstructionFidelity.SEMANTICALLY_CONSTRUCTED,
				MetricEvaluatorMethod2D.ANALYTIC, MetricMethod2D.CLOSED_FORM,
				MetricRepresentationRole2D.SEMANTIC_METRIC, evidence,
				MetricUnit2D.CONSTRUCTION_LENGTH_UNIT,
				new MetricProvenance2D("cu1-admissibility", 1,
						PolicyV2LocusMetricCapability2D.VERSION, "algorithm", "policy"),
				Collections.emptyList(), Collections.emptyList());
	}
}
