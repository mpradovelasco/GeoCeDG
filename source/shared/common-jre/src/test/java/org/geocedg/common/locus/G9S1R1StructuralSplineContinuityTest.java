/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D;
import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.Failure;
import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.Path;
import org.geocedg.common.kernel.spline.SplinePolynomialModel2D;
import org.geogebra.common.kernel.algos.AlgoSpline;
import org.geogebra.common.kernel.geos.GeoFunctionNVar;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoCurveCartesianND;
import org.junit.jupiter.api.Test;

/** Native structural spline evidence, separate from diagnostic exact spans. */
final class G9S1R1StructuralSplineContinuityTest extends G9U0PublicSurfaceTestBase {

	@Test
	void historicalIndependentSpansHaveExactNonzeroNativeKnotDefect() {
		double epsilon = 0x1p-52;
		BigDecimal[] left = exact(new double[] {epsilon, 0, 2, -1});
		BigDecimal[] right = exact(new double[] {-epsilon, 4 * epsilon,
				2 - 2 * epsilon, -1});
		BigDecimal knot = new BigDecimal(0.5);
		assertEquals(0, jet(left, knot, 0).compareTo(new BigDecimal(0x1p-55)));
		assertEquals(0, jet(right, knot, 0).compareTo(new BigDecimal(-0x1p-55)));
		assertEquals(0, jet(right, knot, 1).subtract(jet(left, knot, 1))
				.compareTo(new BigDecimal(0x1p-53)));
	}

	@Test
	void nativeBlockerHasStructuralJetsThroughCubicOrderMinusOne() {
		SplinePolynomialModel2D model = model(new double[][] {{-1, 0}, {0, 0}, {1, 0}}, 3);
		assertEquals(0.5, model.getKnots()[1]);
		assertEquals(2, model.getStructuralContinuityOrder());
		assertEquals(5, model.getSolveSystemDimension());
		assertEquals(8, model.getLegacySystemDimension());
		assertAllJets(model);
		assertEquals(0, model.evaluate(0.5)[0], 1E-12);
		assertEquals(1, model.findOwningSpan(0.5));
	}

	@Test
	void asymmetricCubicKnotsAreStructuralRatherThanToleranceGlued() {
		double[][] values = {{-2, 1}, {-1.7, -0.8}, {-0.1, 2}, {0.4, -1}, {3, 0.5}};
		SplinePolynomialModel2D model = model(values, 3);
		assertFalse(model.isClosed());
		assertAllJets(model);
		assertInterpolation(model, values);
		assertNotEquals(model.getKnots()[1], 0.25);
	}

	@Test
	void intermediateDegreesRetainAllStructuralJetsAndOriginalBoundaryRows() {
		for (int degree : new int[] {4, 5, 7}) {
			double[][] values = linePoints(degree + 2, 1);
			SplinePolynomialModel2D model = model(values, degree);
			assertAllJets(model);
			assertInterpolation(model, values);
			assertOriginalOpenBoundary(model);
		}
	}

	@Test
	void degreeTwelveNativeModelRetainsElevenExactKnotJets() {
		double[][] values = linePoints(13, 1);
		SplinePolynomialModel2D model = model(values, 12);
		assertEquals(11, model.getStructuralContinuityOrder());
		assertEquals(24, model.getSolveSystemDimension());
		assertEquals(156, model.getLegacySystemDimension());
		assertAllJets(model);
		assertInterpolation(model, values);
		assertOriginalOpenBoundary(model);
	}

	@Test
	void maximumPointCountUsesReducedBoundedSystem() {
		double[][] values = linePoints(32, 1);
		SplinePolynomialModel2D model = model(values, 3);
		assertEquals(34, model.getSolveSystemDimension());
		assertEquals(124, model.getLegacySystemDimension());
		assertAllJets(model);
		assertInterpolation(model, values);
	}

	@Test
	void historicallyAdmittedTwentyFivePointQuinticRetainsNumericalAdmission() {
		GeoList points = new GeoList(getConstruction());
		for (int index = 0; index < 25; index++) {
			GeoPoint point = add("HistoricalP" + index + "=(" + (index - 12)
					+ "/4,sin(" + index + "/3))");
			points.add(point);
		}
		SplinePolynomialModel2D model = assertDoesNotThrow(
				() -> SplinePolynomialModel2D.create(points, 5, null),
				"Published G9S1 s29 requires this exact 25-point quintic to remain admitted");
		printEvidence(model);
		assertAllJets(model);
		SplineConstructionEvidence2D evidence = model.getConstructionEvidence();
		assertEquals(Path.HIGH_PRECISION_STRUCTURAL, evidence.getPath());
		assertTrue(evidence.getFailures().containsKey(Failure.BINARY64_REPRESENTATION_REJECTED),
				"Retained precision requires evidence that rounded free coefficients"
						+ " were insufficient");
		assertTrue(evidence.getPrecisionLevels() >= 2);
		assertTrue(evidence.getWorkingPrecision() <= 112);
		assertTrue(evidence.getRetainedPrecision() > 0);
	}

	@Test
	void periodicCubicSeamAndInteriorJetsAreExact() {
		double[][] values = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 0}};
		SplinePolynomialModel2D model = model(values, 3);
		assertTrue(model.isClosed());
		assertEquals(4, model.getSolveSystemDimension());
		assertEquals(6, model.getCoefficientDenominator());
		assertAllJets(model);
		assertPeriodicJets(model);
		assertInterpolation(model, values);
	}

	@Test
	void periodicIntermediateAndDegreeTwelveSeamsAreStructural() {
		for (int degree : new int[] {4, 6, 12}) {
			double[][] values = closedPolygon(degree + 2);
			SplinePolynomialModel2D model = model(values, degree);
			assertEquals(degree + 1, model.getSolveSystemDimension());
			assertAllJets(model);
			assertPeriodicJets(model);
			assertInterpolation(model, values);
		}
	}

	@Test
	void roundedNormalizedKnotsKeepExactStructuralMeaning() {
		double[][] values = {{-2, -1}, {-1, 0}, {0, 1}, {1, 2}};
		SplinePolynomialModel2D model = model(values, 3);
		assertEquals(1.0 / 3, model.getKnots()[1]);
		assertNotEquals(0, new BigDecimal(model.getKnots()[1])
				.multiply(BigDecimal.valueOf(3)).compareTo(BigDecimal.ONE));
		assertAllJets(model);
	}

	@Test
	void finiteSmallLargeAndMixedSignCoordinatesRetainStructuralJets() {
		for (double scale : new double[] {1E-4, 1, 1E4}) {
			double[][] values = {{-2 * scale, scale}, {-scale, -scale},
					{scale, 2 * scale}, {3 * scale, -scale}};
			SplinePolynomialModel2D model = model(values, 3);
			assertAllJets(model);
			assertInterpolation(model, values);
		}
	}

	@Test
	void nontrivialHighDegreeCurvesPreserveClassicFamilyGeometryAndBoundaryRows() {
		GeoFunctionNVar weight = add("structuralWeight(x,y)=1");
		for (int degree : new int[] {4, 7}) {
			double[][] values = curvedPoints(degree + 2);
			GeoList points = list(values);
			SplinePolynomialModel2D model = SplinePolynomialModel2D.create(points,
					degree, weight);
			GeoCurveCartesianND classic = new AlgoSpline(getConstruction(), null,
					points, new GeoNumeric(getConstruction(), degree), weight).getSpline();
			assertTrue(classic.isDefined());
			assertAllJets(model);
			assertInterpolation(model, values);
			assertOriginalOpenBoundary(model);
			for (int sample = 0; sample <= 40; sample++) {
				double parameter = sample / 40.0;
				double[] point = model.evaluate(parameter);
				for (int coordinate = 0; coordinate < 2; coordinate++) {
					assertEquals(classic.getFun(coordinate).value(parameter),
							point[coordinate], 1E-8,
							"Classic-family agreement degree=" + degree + " u=" + parameter);
				}
			}
			printEvidence(model);
		}
	}

	@Test
	void defaultClassicFloatChordKnotsAreNotTheNativeDoubleKnotAuthority() {
		double[][] values = curvedPoints(9);
		GeoList points = list(values);
		SplinePolynomialModel2D model = SplinePolynomialModel2D.create(points, 7, null);
		AlgoSpline algorithm = new AlgoSpline(getConstruction(), null, points,
				new GeoNumeric(getConstruction(), 7), null);
		double[] classicKnots;
		try {
			java.lang.reflect.Field field = AlgoSpline.class
					.getDeclaredField("parameterIntervalLimits");
			field.setAccessible(true);
			classicKnots = ((double[]) field.get(algorithm)).clone();
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Cannot inspect actual Classic knot diagnostic", exception);
		}
		double[] nativeKnots = model.getKnots();
		assertNotEquals(Arrays.toString(nativeKnots), Arrays.toString(classicKnots));
		double maximumKnotDifference = 0;
		for (int index = 0; index < nativeKnots.length; index++) {
			maximumKnotDifference = Math.max(maximumKnotDifference,
					Math.abs(nativeKnots[index] - classicKnots[index]));
		}
		double nativeX = model.evaluate(0.025)[0];
		double classicX = algorithm.getSpline().getFun(0).value(0.025);
		System.out.println("G9S1_R1_DEFAULT_KNOT_DIAGNOSTIC degree=7"
				+ "|native=double-Math.hypot|classic=float-squared-chord"
				+ "|maximumKnotDifference=" + maximumKnotDifference
				+ "|u=0.025|nativeX=" + nativeX + "|classicX=" + classicX);
	}

	@Test
	void admittedNonlinearDegreeSevenMatchesIndependentNativeEquationOracle() {
		int degree = 7;
		double[][] values = curvedPoints(9);
		SplinePolynomialModel2D model = SplinePolynomialModel2D.create(list(values), degree, null);
		printEvidence(model);
		assertAllJets(model);
		assertInterpolation(model, values);
		assertOriginalOpenBoundary(model);
		double maximumForwardError = 0;
		double maximumSolveError = 0;
		double maximumCacheError = 0;
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			NativeEquationOracle ordinary = new NativeEquationOracle(values, model.getKnots(),
					degree, coordinate, 80);
			NativeEquationOracle refined = new NativeEquationOracle(values, model.getKnots(),
					degree, coordinate, 120);
			for (int sample = 0; sample <= 40; sample++) {
				double parameter = sample / 40.0;
				BigDecimal reference = refined.evaluate(parameter);
				assertTrue(reference.subtract(ordinary.evaluate(parameter)).abs()
						.compareTo(new BigDecimal("1E-50")) < 0,
						"Independent precision rerun must agree before serving as an oracle");
				double actual = model.evaluate(parameter)[coordinate];
				double exactStructural = jet(model.getExactCoefficientNumerators(
						model.findOwningSpan(parameter), coordinate), new BigDecimal(parameter), 0)
						.divide(BigDecimal.valueOf(model.getCoefficientDenominator()),
								MathContext.DECIMAL128).doubleValue();
				double error = Math.abs(reference.doubleValue() - actual);
				maximumForwardError = Math.max(maximumForwardError, error);
				maximumSolveError = Math.max(maximumSolveError,
						Math.abs(reference.doubleValue() - exactStructural));
				maximumCacheError = Math.max(maximumCacheError, Math.abs(exactStructural - actual));
				System.out.println("G9S1_R1_NATIVE_ORACLE degree=" + degree
						+ "|coordinate=" + coordinate
						+ "|u=" + parameter + "|reference=" + reference.doubleValue()
						+ "|exactStructural=" + exactStructural + "|actual=" + actual
						+ "|forwardError=" + error);
			}
		}
		System.out.println("G9S1_R1_NATIVE_ORACLE_SUMMARY degree=" + degree
				+ "|maximumForwardError="
				+ maximumForwardError + "|maximumSolveError=" + maximumSolveError
				+ "|maximumCacheError=" + maximumCacheError);
		// This is a focused forward-agreement regression, not a claim that the
		// published backward-error admission contract implies a universal bound.
		assertTrue(maximumForwardError <= 1E-8,
				"Admitted native forward agreement: maximum error=" + maximumForwardError);
	}

	@Test
	void previouslyRejectedOpenDegreeTwelveInputsRemainExplicitNumericalRejections() {
		GeoFunctionNVar constantWeight = add("rejectedWeight(x,y)=1");
		double[][] values = curvedPoints(14);
		for (GeoFunctionNVar weight : new GeoFunctionNVar[] {null, constantWeight}) {
			String[] baselineStatuses = printOriginalGuardDiagnostic(values, 12, weight);
			assertTrue(Arrays.stream(baselineStatuses).allMatch(status ->
					status.startsWith("REJECTED_")),
					"This diagnostic must prove the same source was rejected"
							+ " by the entry baseline");
			IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
					() -> SplinePolynomialModel2D.create(list(values), 12, weight));
			assertTrue(failure.getMessage().contains("original equation revalidation"));
		}
	}

	@Test
	void fullRankDyadicDegreeTwelveInputStillRequiresNumericalAdmission() {
		double[][] values = dyadicCubicPoints();
		double[] knots = new double[values.length];
		for (int index = 0; index < knots.length; index++) {
			knots[index] = index / 16.0;
		}
		NativeEquationOracle oracle = new NativeEquationOracle(values, knots, 12, 0, 80);
		oracle.assertExactFullRank();
		GeoFunctionNVar weight = add("dyadicRejectedWeight(x,y)=1");
		String[] baselineStatuses = printOriginalGuardDiagnostic(values, 12, weight);
		assertTrue(Arrays.stream(baselineStatuses)
				.allMatch(status -> status.startsWith("REJECTED_")));
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> SplinePolynomialModel2D.create(list(values), 12, weight));
		assertTrue(failure.getMessage().contains("numerically inadmissible"));
	}

	@Test
	void differentCoordinateScalesDoNotBecomeContinuityTolerance() {
		double[][] values = {{1E-4, -1E4}, {0.25, -10}, {1, 200}, {3, 3000}};
		SplinePolynomialModel2D model = model(values, 3);
		assertAllJets(model);
		assertInterpolation(model, values);
		assertOriginalOpenBoundary(model);
	}

	@Test
	void exactStructuralNumeratorsAreDefensiveAndSignatureIsVersioned() {
		SplinePolynomialModel2D model = model(linePoints(4, 1), 3);
		BigDecimal[] first = model.getExactCoefficientNumerators(0, 0);
		BigDecimal saved = first[0];
		first[0] = BigDecimal.TEN;
		assertEquals(saved, model.getExactCoefficientNumerators(0, 0)[0]);
		assertTrue(model.getSemanticSignature().startsWith("semantic-spline-polynomial/v2|"));
		assertEquals(model.getSemanticSignature(), model(linePoints(4, 1), 3)
				.getSemanticSignature());
	}

	@Test
	void approximateClosedEndpointsAreRejectedWithoutAveraging() {
		GeoList points = list(new double[][] {{0, 0}, {1, 1}, {-1, 1}, {1E-10, 0}});
		assertTrue(points.get(0).isEqual(points.get(3)));
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> SplinePolynomialModel2D.create(points, 3, null));
		assertTrue(exception.getMessage().contains("exactly equal finite endpoints"));
		assertEquals(1E-10, ((GeoPoint) points.get(3)).getInhomX());
	}

	@Test
	void signedZeroClosingCoordinatesDoNotBreakExactPeriodicity() {
		SplinePolynomialModel2D model = model(new double[][] {{0, -0.0}, {1, 1},
				{-1, 1}, {-0.0, 0}}, 3);
		assertTrue(model.isClosed());
		assertPeriodicJets(model);
	}

	@Test
	void existingPointDegreeAndDenseAdmissionPolicyRemainBounded() {
		assertTrue(SplinePolynomialModel2D.isWithinWorkPolicy(3, 3));
		assertTrue(SplinePolynomialModel2D.isWithinWorkPolicy(32, 12));
		assertFalse(SplinePolynomialModel2D.isWithinWorkPolicy(33, 3));
		assertFalse(SplinePolynomialModel2D.isWithinWorkPolicy(12, 13));
		assertFalse(SplinePolynomialModel2D.isWithinWorkPolicy(2, 3));
	}

	private SplinePolynomialModel2D model(double[][] points, int degree) {
		SplinePolynomialModel2D model = SplinePolynomialModel2D.create(list(points), degree,
				null);
		printEvidence(model);
		return model;
	}

	private static void printEvidence(SplinePolynomialModel2D model) {
		try {
			SplineConstructionEvidence2D construction = model.getConstructionEvidence();
			System.out.println("G9S1_R1_STRUCTURAL_PRECISION policy=" + construction.getPolicy()
					+ "|path=" + construction.getPath()
					+ "|working=" + construction.getWorkingPrecision()
					+ "|retained=" + construction.getRetainedPrecision()
					+ "|levels=" + construction.getPrecisionLevels()
					+ "|solveOperations=" + construction.getSolveOperations()
					+ "|expansionOperations=" + construction.getExpansionOperations()
					+ "|admissionOperations=" + construction.getAdmissionOperations()
					+ "|failures=" + construction.getFailures());
			MessageDigest hash = MessageDigest.getInstance("SHA-256");
			String header = "degree=" + model.getDegree() + "|closed=" + model.isClosed()
					+ "|denominator=" + model.getCoefficientDenominator();
			hash.update(header.getBytes(StandardCharsets.UTF_8));
			for (double knot : model.getKnots()) {
				hash.update(("|k=" + Double.toHexString(knot)).getBytes(StandardCharsets.UTF_8));
			}
			int maximumDigits = 0;
			for (int span = 0; span < model.getSpanCount(); span++) {
				for (int coordinate = 0; coordinate < 2; coordinate++) {
					for (BigDecimal numerator : model.getExactCoefficientNumerators(
							span, coordinate)) {
						BigDecimal normalized = numerator.stripTrailingZeros();
						maximumDigits = Math.max(maximumDigits, normalized.precision());
						hash.update(("|n=" + normalized.toEngineeringString())
								.getBytes(StandardCharsets.UTF_8));
					}
				}
			}
			System.out.println("G9S1_R1_STRUCTURAL " + header + "|legacyDimension="
					+ model.getLegacySystemDimension() + "|reducedDimension="
					+ model.getSolveSystemDimension() + "|maximumNumeratorDigits="
					+ maximumDigits + "|numeratorSha256="
					+ HexFormat.of().formatHex(hash.digest()));
		} catch (NoSuchAlgorithmException exception) {
			throw new AssertionError("Required SHA-256 unavailable", exception);
		}
	}

	private GeoList list(double[][] coordinates) {
		GeoList result = new GeoList(getConstruction());
		for (double[] coordinate : coordinates) {
			result.add(new GeoPoint(getConstruction(), coordinate[0], coordinate[1], 1));
		}
		return result;
	}

	private static void assertAllJets(SplinePolynomialModel2D model) {
		assertEquals(model.getDegree() - 1, model.getStructuralContinuityOrder());
		double[] knots = model.getKnots();
		for (int span = 1; span < model.getSpanCount(); span++) {
			BigDecimal knot = new BigDecimal(knots[span]);
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				BigDecimal[] left = model.getExactCoefficientNumerators(span - 1, coordinate);
				BigDecimal[] right = model.getExactCoefficientNumerators(span, coordinate);
				for (int order = 0; order < model.getDegree(); order++) {
					assertEquals(0, jet(left, knot, order).compareTo(jet(right, knot, order)),
							"Exact knot jet differs: span=" + span + " order=" + order);
				}
			}
		}
	}

	private static void assertPeriodicJets(SplinePolynomialModel2D model) {
		assertTrue(model.isClosed());
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			BigDecimal[] first = model.getExactCoefficientNumerators(0, coordinate);
			BigDecimal[] last = model.getExactCoefficientNumerators(model.getSpanCount() - 1,
					coordinate);
			for (int order = 0; order < model.getDegree(); order++) {
				assertEquals(0, jet(first, BigDecimal.ZERO, order)
						.compareTo(jet(last, BigDecimal.ONE, order)),
						"Exact periodic jet differs: order=" + order);
			}
		}
	}

	private static void assertOriginalOpenBoundary(SplinePolynomialModel2D model) {
		assertBoundary(model, 0, 0);
		assertBoundary(model, model.getSpanCount() - 1, 1);
		for (int offset = 2; offset <= model.getDegree() - 2; offset++) {
			assertBoundary(model, model.getSpanCount() - offset,
					model.getKnots()[offset - 1]);
		}
	}

	private static void assertBoundary(SplinePolynomialModel2D model,
			int span, double parameter) {
		for (int coordinate = 0; coordinate < 2; coordinate++) {
			double[] coefficients = model.getCoefficients(span, coordinate);
			double scale = Arrays.stream(coefficients).map(Math::abs).sum();
			double value = jet(model.getExactCoefficientNumerators(span, coordinate),
					new BigDecimal(parameter), model.getDegree() - 1).doubleValue()
					/ model.getCoefficientDenominator();
			assertEquals(0, value, 1E-9 * Math.max(1, scale));
		}
	}

	private static void assertInterpolation(SplinePolynomialModel2D model,
			double[][] expected) {
		for (int index = 0; index < expected.length; index++) {
			double[] actual = model.evaluate(model.getKnots()[index]);
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				assertEquals(expected[index][coordinate], actual[coordinate],
						1E-8 * Math.max(1, Math.abs(expected[index][coordinate])));
			}
		}
	}

	private static BigDecimal jet(BigDecimal[] descending, BigDecimal parameter, int order) {
		int degree = descending.length - 1;
		BigDecimal value = BigDecimal.ZERO;
		for (int index = 0; index <= degree - order; index++) {
			long multiplier = 1;
			for (int derivative = 0; derivative < order; derivative++) {
				multiplier *= degree - index - derivative;
			}
			value = value.multiply(parameter)
					.add(descending[index].multiply(BigDecimal.valueOf(multiplier)));
		}
		return value;
	}

	private static BigDecimal[] exact(double[] values) {
		return Arrays.stream(values).mapToObj(BigDecimal::new).toArray(BigDecimal[]::new);
	}

	private static double[][] linePoints(int count, double scale) {
		double[][] values = new double[count][2];
		for (int index = 0; index < count; index++) {
			values[index][0] = scale * (index - count / 2.0);
			values[index][1] = scale * (0.5 * index - 1);
		}
		return values;
	}

	private static double[][] closedPolygon(int count) {
		double[][] values = new double[count][2];
		for (int index = 0; index < count - 1; index++) {
			double angle = 2 * Math.PI * index / (count - 1);
			values[index][0] = Math.cos(angle);
			values[index][1] = Math.sin(angle);
		}
		values[count - 1] = values[0].clone();
		return values;
	}

	private static double[][] curvedPoints(int count) {
		double[][] values = new double[count][2];
		for (int index = 0; index < count; index++) {
			double x = -1 + 2 * index / (double) (count - 1);
			values[index][0] = x;
			values[index][1] = 0.2 * x * x + 0.03 * x * x * x + 0.02 * Math.sin(1.5 * x);
		}
		return values;
	}

	private static double[][] dyadicCubicPoints() {
		double[][] values = new double[17][2];
		for (int index = 0; index < values.length; index++) {
			double x = -1 + index / 8.0;
			values[index][0] = x;
			values[index][1] = 0.5 * x * x + 0.125 * x * x * x - 0.25;
		}
		return values;
	}

	private static String[] printOriginalGuardDiagnostic(double[][] values, int degree,
			GeoFunctionNVar weight) {
		// Inspect a private, nonpublished snapshot before admission. The actual
		// tests still call create and retain every production guard unchanged.
		try {
			String[] baselineStatuses = new String[2];
			java.lang.reflect.Method cumulativeMethod = SplinePolynomialModel2D.class
					.getDeclaredMethod("cumulativeParameter", double[][].class,
							GeoFunctionNVar.class);
			cumulativeMethod.setAccessible(true);
			double[] cumulative = (double[]) cumulativeMethod.invoke(null, values, weight);
			double[] knots = Arrays.stream(cumulative)
					.map(value -> value / cumulative[cumulative.length - 1]).toArray();
			java.lang.reflect.Method original = SplinePolynomialModel2D.class
					.getDeclaredMethod("linearSystem", double[][].class, double[].class,
							int.class, int.class, boolean.class);
			original.setAccessible(true);
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				double[][] rows = (double[][]) original.invoke(null, values, cumulative,
						coordinate, degree + 1, false);
				baselineStatuses[coordinate] = publishedBaselineStatus(rows);
				System.out.println("G9S1_R1_PUBLISHED_BASELINE_DIAGNOSTIC degree=" + degree
						+ "|pointCount=" + values.length
						+ "|weight=" + (weight == null ? "default" : "constant-one")
						+ "|coordinate=" + coordinate + "|status=" + baselineStatuses[coordinate]);
			}
			Class<?> structuralClass = Class.forName(
					"org.geocedg.common.kernel.spline.SplineStructuralModel2D");
			java.lang.reflect.Method create = structuralClass.getDeclaredMethod("create",
					double[][].class, double[].class, int.class, boolean.class);
			create.setAccessible(true);
			Object structural;
			try {
				structural = create.invoke(null, values, knots, degree, false);
			} catch (java.lang.reflect.InvocationTargetException failure) {
				if (!(failure.getCause() instanceof IllegalArgumentException)) {
					throw failure;
				}
				System.out.println("G9S1_R1_STRUCTURAL_PREFLIGHT_DIAGNOSTIC degree=" + degree
						+ "|pointCount=" + values.length + "|rejection="
						+ failure.getCause().getMessage());
				return baselineStatuses;
			}
			java.lang.reflect.Method coefficientsMethod = structuralClass
					.getDeclaredMethod("getRoundedCoefficients");
			coefficientsMethod.setAccessible(true);
			double[][][] coefficients = (double[][][]) coefficientsMethod.invoke(structural);
			java.lang.reflect.Field freeField = structuralClass.getDeclaredField("freeCoordinates");
			freeField.setAccessible(true);
			BigDecimal[][] free = (BigDecimal[][]) freeField.get(structural);
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				double[][] rows = (double[][]) original.invoke(null, values, cumulative,
						coordinate, degree + 1, false);
				double[] solution = new double[rows.length];
				for (int span = 0; span < coefficients.length; span++) {
					System.arraycopy(coefficients[span][coordinate], 0, solution,
							span * (degree + 1), degree + 1);
				}
				for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
					double[] row = rows[rowIndex];
					double value = 0;
					double scale = Math.abs(row[solution.length]);
					BigDecimal exactResidual = new BigDecimal(row[solution.length]).negate();
					int nonzero = 0;
					for (int column = 0; column < solution.length; column++) {
						value += row[column] * solution[column];
						scale += Math.abs(row[column]) * Math.abs(solution[column]);
						exactResidual = exactResidual.add(new BigDecimal(row[column])
								.multiply(new BigDecimal(solution[column])));
						if (row[column] != 0) {
							nonzero++;
						}
					}
					double relative = Math.abs(value - row[solution.length]) / Math.max(1, scale);
					if (relative > 1E-9) {
						System.out.println("G9S1_R1_ORIGINAL_GUARD_DIAGNOSTIC degree=" + degree
								+ "|weight=" + (weight == null ? "default" : "constant-one")
								+ "|coordinate=" + coordinate + "|row=" + rowIndex
								+ "|nonzero=" + nonzero + "|relative=" + relative
								+ "|residual=" + exactResidual.doubleValue() + "|scale=" + scale
								+ "|firstSpanPenultimate=" + coefficients[0][coordinate][1]
								+ "|maximumFreeWeight=" + Arrays.stream(free[coordinate])
									.mapToDouble(BigDecimal::doubleValue).map(Math::abs)
									.max().orElseThrow());
					}
				}
			}
			return baselineStatuses;
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Cannot inspect bounded original-equation diagnostic",
					exception);
		}
	}

	private static String publishedBaselineStatus(double[][] original) {
		// Diagnostic reproduction of SplinePolynomialModel2D.solve at the entry
		// commit 109f077fc5e2a40bcde45d3271eb928ee66fdfcc: row scaling only,
		// the exact same pivot guard and the exact same backward-error guard.
		double[][] matrix = Arrays.stream(original).map(double[]::clone).toArray(double[][]::new);
		int length = matrix.length;
		for (double[] row : matrix) {
			double scale = Arrays.stream(row).map(Math::abs).max().orElseThrow();
			if (!Double.isFinite(scale) || scale == 0) {
				return "REJECTED_ROW_SCALE";
			}
			for (int column = 0; column < row.length; column++) {
				row[column] /= scale;
			}
		}
		double threshold = 128 * Math.ulp(1.0) * Math.max(1, length);
		for (int pivot = 0; pivot < length; pivot++) {
			int best = pivot;
			for (int row = pivot + 1; row < length; row++) {
				if (Math.abs(matrix[row][pivot]) > Math.abs(matrix[best][pivot])) {
					best = row;
				}
			}
			if (!Double.isFinite(matrix[best][pivot])
					|| Math.abs(matrix[best][pivot]) <= threshold) {
				return "REJECTED_PIVOT:index=" + pivot + ",value=" + matrix[best][pivot]
						+ ",threshold=" + threshold;
			}
			double[] swap = matrix[pivot];
			matrix[pivot] = matrix[best];
			matrix[best] = swap;
			double divisor = matrix[pivot][pivot];
			for (int column = pivot; column <= length; column++) {
				matrix[pivot][column] /= divisor;
			}
			for (int row = 0; row < length; row++) {
				if (row == pivot || matrix[row][pivot] == 0) {
					continue;
				}
				double factor = matrix[row][pivot];
				for (int column = pivot; column <= length; column++) {
					matrix[row][column] -= factor * matrix[pivot][column];
				}
			}
		}
		double maximum = 0;
		for (double[] row : original) {
			double value = 0;
			double scale = Math.abs(row[length]);
			for (int column = 0; column < length; column++) {
				value += row[column] * matrix[column][length];
				scale += Math.abs(row[column]) * Math.abs(matrix[column][length]);
			}
			if (!Double.isFinite(value) || !Double.isFinite(scale)) {
				return "REJECTED_NONFINITE";
			}
			maximum = Math.max(maximum, Math.abs(value - row[length]) / Math.max(1, scale));
		}
		return (maximum <= 1E-9 ? "ADMITTED" : "REJECTED_BACKWARD_ERROR")
				+ ":maximum=" + maximum;
	}

	/**
	 * Independent high-precision solve of the native interpolation and original
	 * open boundary equations, with continuity eliminated symbolically. It calls
	 * neither the productive structural basis nor its floating solver. Exact
	 * binary64 input knots/values remain the same mathematical data.
	 */
	private static final class NativeEquationOracle {
		private final double[] knots;
		private final int degree;
		private final BigDecimal[][] equations;
		private final BigDecimal[] solution;

		private NativeEquationOracle(double[][] values, double[] knots, int degree,
				int coordinate, int precision) {
			this.knots = knots.clone();
			this.degree = degree;
			int dimension = knots.length + degree - 1;
			equations = new BigDecimal[dimension][dimension + 1];
			int row = 0;
			for (; row < knots.length; row++) {
				equations[row] = equation(Math.min(row, knots.length - 2), knots[row], 0);
				equations[row][dimension] = new BigDecimal(values[row][coordinate]);
			}
			equations[row++] = equation(0, 0, degree - 1);
			equations[row++] = equation(knots.length - 2, 1, degree - 1);
			for (int offset = 2; offset <= degree - 2; offset++) {
				equations[row++] = equation(knots.length - 1 - offset,
						knots[offset - 1], degree - 1);
			}
			assertEquals(dimension, row);
			solution = solve(equations, new MathContext(precision));
			for (BigDecimal[] equation : equations) {
				BigDecimal residual = equation[dimension].negate();
				for (int index = 0; index < dimension; index++) {
					residual = residual.add(equation[index].multiply(solution[index]));
				}
				assertTrue(residual.abs().compareTo(new BigDecimal("1E-50")) < 0,
						"High-precision reference must satisfy unchanged native equations");
			}
		}

		private void assertExactFullRank() {
			int size = equations.length;
			BigInteger[][] matrix = new BigInteger[size][size];
			for (int row = 0; row < size; row++) {
				int scale = Arrays.stream(equations[row]).mapToInt(BigDecimal::scale).max()
						.orElseThrow();
				for (int column = 0; column < size; column++) {
					matrix[row][column] = equations[row][column].movePointRight(scale)
							.toBigIntegerExact();
				}
			}
			BigInteger previous = BigInteger.ONE;
			for (int column = 0; column < size - 1; column++) {
				int pivot = column;
				while (pivot < size && matrix[pivot][column].signum() == 0) {
					pivot++;
				}
				assertTrue(pivot < size,
						"Exact fraction-free elimination requires a nonzero pivot");
				BigInteger[] swap = matrix[column];
				matrix[column] = matrix[pivot];
				matrix[pivot] = swap;
				BigInteger diagonal = matrix[column][column];
				for (int row = column + 1; row < size; row++) {
					for (int index = column + 1; index < size; index++) {
						BigInteger numerator = matrix[row][index].multiply(diagonal)
								.subtract(matrix[row][column].multiply(matrix[column][index]));
						BigInteger[] quotient = numerator.divideAndRemainder(previous);
						assertEquals(BigInteger.ZERO, quotient[1]);
						matrix[row][index] = quotient[0];
					}
					matrix[row][column] = BigInteger.ZERO;
				}
				previous = diagonal;
			}
			assertNotEquals(0, matrix[size - 1][size - 1].signum());
			System.out.println("G9S1_R1_EXACT_RANK_DIAGNOSTIC degree=" + degree
					+ "|pointCount=" + knots.length + "|rank=" + size + "|dimension=" + size);
		}

		private BigDecimal[] equation(int span, double parameter, int order) {
			BigDecimal[] row = new BigDecimal[knots.length + degree];
			Arrays.fill(row, BigDecimal.ZERO);
			BigDecimal t = new BigDecimal(parameter);
			for (int power = order; power <= degree; power++) {
				row[power] = t.pow(power - order).multiply(factor(power, order));
			}
			for (int hinge = 1; hinge <= span; hinge++) {
				row[degree + hinge] = t.subtract(new BigDecimal(knots[hinge]))
						.pow(degree - order).multiply(factor(degree, order));
			}
			return row;
		}

		private BigDecimal evaluate(double parameter) {
			int span = 0;
			while (span + 1 < knots.length - 1 && parameter >= knots[span + 1]) {
				span++;
			}
			BigDecimal[] coefficients = equation(span, parameter, 0);
			BigDecimal value = BigDecimal.ZERO;
			for (int index = 0; index < solution.length; index++) {
				value = value.add(coefficients[index].multiply(solution[index]));
			}
			return value;
		}

		private static BigDecimal factor(int power, int order) {
			long result = 1;
			for (int index = 0; index < order; index++) {
				result *= power - index;
			}
			return BigDecimal.valueOf(result);
		}

		private static BigDecimal[] solve(BigDecimal[][] equations, MathContext precision) {
			int size = equations.length;
			BigDecimal[][] rows = Arrays.stream(equations).map(BigDecimal[]::clone)
					.toArray(BigDecimal[][]::new);
			for (int column = 0; column < size; column++) {
				int pivot = column;
				for (int row = column + 1; row < size; row++) {
					if (rows[row][column].abs().compareTo(rows[pivot][column].abs()) > 0) {
						pivot = row;
					}
				}
				assertNotEquals(0, rows[pivot][column].signum(),
						"Independent native equation system must be nonsingular");
				BigDecimal[] swap = rows[column];
				rows[column] = rows[pivot];
				rows[pivot] = swap;
				BigDecimal divisor = rows[column][column];
				for (int index = column; index <= size; index++) {
					rows[column][index] = rows[column][index].divide(divisor, precision);
				}
				for (int row = column + 1; row < size; row++) {
					BigDecimal multiplier = rows[row][column];
					if (multiplier.signum() == 0) {
						continue;
					}
					for (int index = column; index <= size; index++) {
						rows[row][index] = rows[row][index].subtract(
								multiplier.multiply(rows[column][index], precision), precision);
					}
				}
			}
			BigDecimal[] answer = new BigDecimal[size];
			for (int row = size - 1; row >= 0; row--) {
				BigDecimal value = rows[row][size];
				for (int column = row + 1; column < size; column++) {
					value = value.subtract(rows[row][column].multiply(answer[column], precision),
							precision);
				}
				answer[row] = value;
			}
			return answer;
		}
	}
}
