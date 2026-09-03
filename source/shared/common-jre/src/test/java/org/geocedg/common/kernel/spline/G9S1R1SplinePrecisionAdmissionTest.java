/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;

import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.AdmissionException;
import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.Failure;
import org.geocedg.common.kernel.spline.SplineConstructionEvidence2D.Path;
import org.geocedg.common.kernel.spline.SplinePrecisionSolve2D.Policy;
import org.geocedg.common.kernel.spline.SplinePrecisionSolve2D.Work;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Bounded arithmetic policy tests, distinct from root-certification evidence. */
final class G9S1R1SplinePrecisionAdmissionTest extends BaseUnitTest {

	@Test
	void ordinaryCubicUsesBinary64WithoutPrecisionEscalation() {
		SplinePolynomialModel2D model = create(line(4), 3);
		SplineConstructionEvidence2D evidence = model.getConstructionEvidence();
		assertEquals(Path.BINARY64, evidence.getPath());
		assertEquals(0, evidence.getPrecisionLevels());
		assertEquals(0, evidence.getWorkingPrecision());
		assertEquals(0, evidence.getRetainedPrecision());
		assertTrue(evidence.getSolveOperations() > 0);
		assertTrue(evidence.getExpansionOperations() > 0);
		assertTrue(evidence.getAdmissionOperations() > 0);
		assertTrue(evidence.getFailures().isEmpty());
	}

	@Test
	void historicalQuinticPrecisionAndCanonicalExpansionRepeatExactly() {
		SplinePolynomialModel2D first = create(historicalQuintic(), 5);
		SplinePolynomialModel2D second = create(historicalQuintic(), 5);
		assertEquals(first.getSemanticSignature(), second.getSemanticSignature());
		assertEquals(Path.HIGH_PRECISION_STRUCTURAL, first.getConstructionEvidence().getPath());
		assertEquals(first.getConstructionEvidence().getWorkingPrecision(),
				second.getConstructionEvidence().getWorkingPrecision());
		assertEquals(first.getConstructionEvidence().getSolveOperations(),
				second.getConstructionEvidence().getSolveOperations());
		assertEquals(first.getConstructionEvidence().getExpansionOperations(),
				second.getConstructionEvidence().getExpansionOperations());
		assertEquals(first.getConstructionEvidence().getAdmissionOperations(),
				second.getConstructionEvidence().getAdmissionOperations());
		assertEquals(first.getConstructionEvidence().getFailures(),
				second.getConstructionEvidence().getFailures());
		for (int span = 0; span < first.getSpanCount(); span++) {
			for (int coordinate = 0; coordinate < 2; coordinate++) {
				assertEquals(Arrays.asList(first.getExactCoefficientNumerators(span, coordinate)),
						Arrays.asList(second.getExactCoefficientNumerators(span, coordinate)));
			}
		}
	}

	@Test
	void oneFallbackLevelFailsClosedWithoutSuccessiveStability() {
		AdmissionException failure = assertThrows(AdmissionException.class,
				() -> SplinePolynomialModel2D.create(list(historicalQuintic()), 5, null,
						new Policy(1, SplinePrecisionSolve2D.MAXIMUM_OPERATIONS)));
		print(failure.getEvidence());
		assertEquals(Path.REJECTED, failure.getEvidence().getPath());
		assertEquals(48, failure.getEvidence().getWorkingPrecision());
		assertEquals(1, failure.getEvidence().getPrecisionLevels());
		assertTrue(failure.getEvidence().getFailures().containsKey(Failure.PRECISION_EXHAUSTED));
	}

	@Test
	void arithmeticWorkCapRejectsBeforeReturningAModel() {
		AdmissionException failure = assertThrows(AdmissionException.class,
				() -> SplinePolynomialModel2D.create(list(line(4)), 3, null,
						new Policy(3, 2)));
		print(failure.getEvidence());
		assertEquals(Path.REJECTED, failure.getEvidence().getPath());
		assertTrue(failure.getEvidence().getFailures().containsKey(Failure.WORK_EXHAUSTED));
		assertEquals(0, failure.getEvidence().getPrecisionLevels());
	}

	@Test
	void precisionPolicyMaximumsCannotBeExpanded() {
		assertEquals(Arrays.asList(48, 80, 112), SplinePrecisionSolve2D.PRECISIONS);
		assertThrows(UnsupportedOperationException.class,
				() -> SplinePrecisionSolve2D.PRECISIONS.set(0, 113));
		assertThrows(IllegalArgumentException.class, () -> new Policy(4, 100));
		assertThrows(IllegalArgumentException.class,
				() -> new Policy(3, SplinePrecisionSolve2D.MAXIMUM_OPERATIONS + 1));
		assertThrows(IllegalArgumentException.class, () -> new Policy(3, 0));
	}

	@Test
	void genuinelySingularLinearSystemIsRejectedAtEveryPrecision() {
		BigDecimal two = BigDecimal.valueOf(2);
		BigDecimal[][] singular = {{BigDecimal.ONE, two, BigDecimal.ONE},
				{two, BigDecimal.valueOf(4), two}};
		for (int precision : SplinePrecisionSolve2D.PRECISIONS) {
			Work work = new Work(Policy.ordinary());
			work.precision = precision;
			work.levels = 1;
			assertNull(SplinePrecisionSolve2D.solve(singular, precision, work));
			assertTrue(work.failures.containsKey(Failure.PIVOT_REJECTED));
			print(work.snapshot(Path.REJECTED, 0));
		}
	}

	@Test
	void originalBackwardErrorRejectsAnIncorrectSolution() {
		double[][] original = {{1, 0, 2}, {0, 1, -3}};
		assertTrue(SplinePolynomialModel2D.hasAcceptableBackwardError(original,
				new double[] {2, -3}));
		assertFalse(SplinePolynomialModel2D.hasAcceptableBackwardError(original,
				new double[] {2, -2}));
		assertFalse(SplinePolynomialModel2D.hasAcceptableBackwardError(original,
				new double[] {Double.NaN, -3}));
	}

	@Test
	void precisionFallbackCannotBypassInconsistentOriginalEquation() {
		double[][] values = {{-1, 0}, {0, 0}, {1, 0}};
		double[] impossible = new double[9];
		impossible[8] = 1;
		// Deliberately inconsistent 0=1 original-row evidence in this bounded
		// test seam. The public constructor always derives its original rows.
		double[][][] original = {{impossible}, {impossible.clone()}};
		AdmissionException failure = assertThrows(AdmissionException.class,
				() -> SplineStructuralModel2D.create(values, new double[] {0, 0.5, 1},
						3, false, original, Policy.ordinary()));
		print(failure.getEvidence());
		assertEquals(112, failure.getEvidence().getWorkingPrecision());
		assertTrue(failure.getEvidence().getFailures()
				.containsKey(Failure.ORIGINAL_EQUATION_REJECTED));
		assertTrue(failure.getEvidence().getFailures().containsKey(Failure.PRECISION_EXHAUSTED));
	}

	@Test
	void invalidSemanticInputsNeverEnterPrecisionFallback() {
		assertThrows(IllegalArgumentException.class,
				() -> SplinePolynomialModel2D.create(list(line(4)), 2, null));
		double[][] repeated = {{0, 0}, {0, 0}, {1, 1}};
		assertThrows(IllegalArgumentException.class,
				() -> SplinePolynomialModel2D.create(list(repeated), 3, null));
		double[][] nonfinite = {{0, 0}, {1, 1}, {Double.POSITIVE_INFINITY, 0}};
		assertThrows(IllegalArgumentException.class,
				() -> SplinePolynomialModel2D.create(list(nonfinite), 3, null));
	}

	@Test
	void highSupportedDegreeAndPeriodicModelsReportArithmeticRoutes() {
		for (int degree : new int[] {3, 4, 5, 7, 12}) {
			SplinePolynomialModel2D model = create(line(degree + 1), degree);
			assertEquals(degree - 1, model.getStructuralContinuityOrder());
			assertTrue(model.getConstructionEvidence().getWorkingPrecision() <= 112);
		}
		SplinePolynomialModel2D maximumPoints = create(line(32), 3);
		assertEquals(34, maximumPoints.getSolveSystemDimension());
		double[][] periodic = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 0}};
		SplinePolynomialModel2D closed = create(periodic, 3);
		assertTrue(closed.isClosed());
		assertEquals(2, closed.getStructuralContinuityOrder());
	}

	private SplinePolynomialModel2D create(double[][] coordinates, int degree) {
		SplinePolynomialModel2D model = SplinePolynomialModel2D.create(list(coordinates),
				degree, null);
		print(model.getConstructionEvidence());
		return model;
	}

	private static void print(SplineConstructionEvidence2D evidence) {
		System.out.println("G9S1_R1_PRECISION_ADMISSION policy=" + evidence.getPolicy()
				+ "|path=" + evidence.getPath() + "|working=" + evidence.getWorkingPrecision()
				+ "|retained=" + evidence.getRetainedPrecision()
				+ "|levels=" + evidence.getPrecisionLevels()
				+ "|solveOperations=" + evidence.getSolveOperations()
				+ "|expansionOperations=" + evidence.getExpansionOperations()
				+ "|admissionOperations=" + evidence.getAdmissionOperations()
				+ "|failures=" + evidence.getFailures());
	}

	private GeoList list(double[][] coordinates) {
		GeoList result = new GeoList(getConstruction());
		for (double[] coordinate : coordinates) {
			result.add(new GeoPoint(getConstruction(), coordinate[0], coordinate[1], 1));
		}
		return result;
	}

	private static double[][] line(int count) {
		double[][] result = new double[count][2];
		for (int index = 0; index < count; index++) {
			result[index][0] = index - count / 2.0;
			result[index][1] = 0.5 * index - 1;
		}
		return result;
	}

	private static double[][] historicalQuintic() {
		double[][] result = new double[25][2];
		for (int index = 0; index < result.length; index++) {
			result[index] = new double[] {(index - 12) / 4.0, Math.sin(index / 3.0)};
		}
		return result;
	}
}
