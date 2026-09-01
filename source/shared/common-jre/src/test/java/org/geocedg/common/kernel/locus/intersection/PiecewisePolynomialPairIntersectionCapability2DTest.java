/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluator2D;
import org.junit.jupiter.api.Test;

/** Focused semantic-span pair isolation tests independent of render sampling. */
final class PiecewisePolynomialPairIntersectionCapability2DTest {
	private static final String BRANCH = "polynomial/main";
	private static final double EPSILON = 1E-9;

	@Test
	void multipleRootsInsideOneLegacyGridCellAreFoundSemantically() {
		double firstRoot = 0.5001;
		double secondRoot = 0.5201;
		LocusDefinition2D curve = definition("a-multiple", false,
				new double[] {0, 1},
				new double[][] {{1, 0}},
				new double[][] {{1, -(firstRoot + secondRoot),
						firstRoot * secondRoot}});
		LocusDefinition2D axis = definition("b-axis", false,
				new double[] {0, 1}, new double[][] {{1, 0}},
				new double[][] {{0}});

		LocusIntersectionResult2D result = solve(curve, axis);

		assertEquals(ComputationStatus.SUCCESS, result.getComputationStatus());
		assertEquals(2, result.getFiniteSolutions().size(),
				() -> "pair parameters=" + parameterPairs(result));
		assertEquals(firstRoot, firstParameters(result).get(0), EPSILON);
		assertEquals(secondRoot, firstParameters(result).get(1), EPSILON);
		assertTrue(result.getWork().getParameterBoxesVisited() > 0);
		assertTrue(result.getFiniteSolutions().stream().allMatch(solution ->
				solution.getPairEvidence().orElseThrow().getLocalIsolation()
						.getStatus() == LocalIsolationStatus.NOT_ESTABLISHED));
		assertTrue(result.getFiniteSolutions().stream().allMatch(solution ->
				result.findPointAdmissibleSolution(
						solution.getIdentity().getRootToken()).isEmpty()));
	}

	@Test
	void callerOperandSwapKeepsCanonicalSemanticPairs() {
		LocusDefinition2D first = definition("a-horizontal", false,
				new double[] {0, 1}, new double[][] {{1, 0}},
				new double[][] {{0}});
		LocusDefinition2D second = definition("b-vertical", false,
				new double[] {0, 1}, new double[][] {{0}},
				new double[][] {{1, 0}});

		LocusIntersectionResult2D forward = solve(first, second);
		LocusIntersectionResult2D reverse = solve(second, first);

		assertEquals(ComputationStatus.SUCCESS, forward.getComputationStatus());
		assertEquals(ComputationStatus.SUCCESS, reverse.getComputationStatus());
		assertEquals(1, forward.getFiniteSolutions().size());
		assertEquals(1, reverse.getFiniteSolutions().size());
		assertEquals(parameterPairs(forward), parameterPairs(reverse));
		assertEquals(tokens(forward), tokens(reverse));
		assertEquals(forward.getSourceBinding().getSourcePairIdentity(),
				reverse.getSourceBinding().getSourcePairIdentity());
	}

	@Test
	void transversePairRefinementIsInvariantUnderUniformScale() {
		for (double scale : new double[] {1, 1E-8}) {
			String suffix = Double.toHexString(scale);
			LocusDefinition2D horizontal = definition("a-scaled-" + suffix,
					false, new double[] {0, 1},
					new double[][] {{scale, -0.5 * scale}},
					new double[][] {{0}});
			LocusDefinition2D vertical = definition("b-scaled-" + suffix,
					false, new double[] {0, 1}, new double[][] {{0}},
					new double[][] {{scale, -0.5 * scale}});

			LocusIntersectionResult2D result = solve(horizontal, vertical);

			assertEquals(ComputationStatus.SUCCESS,
					result.getComputationStatus());
			assertEquals(1, result.getFiniteSolutions().size());
			LocusPairIntersectionEvidence2D evidence = result
					.getFiniteSolutions().get(0).getPairEvidence().orElseThrow();
			assertEquals(0.5, evidence.getFirst().getSemanticParameter(), EPSILON);
			assertEquals(0.5, evidence.getSecond().getSemanticParameter(), EPSILON);
			assertEquals(ContactClass.TRANSVERSE_ESTABLISHED,
					result.getFiniteSolutions().get(0).getClassification()
							.getContactClass());
		}
	}

	@Test
	void canonicalKnotAndPeriodicSeamOwnershipPreventDuplicates() {
		LocusDefinition2D knotCurve = definition("a-knot", false,
				new double[] {0, 0.5, 1},
				new double[][] {{1, 0}, {1, 0}},
				new double[][] {{1, -0.5}, {1, -0.5}});
		LocusDefinition2D axis = definition("b-knot-axis", false,
				new double[] {0, 1}, new double[][] {{1, 0}},
				new double[][] {{0}});
		LocusIntersectionResult2D knot = solve(knotCurve, axis);
		assertEquals(1, knot.getFiniteSolutions().size());
		assertEquals(0.5, firstParameters(knot).get(0), EPSILON);

		LocusDefinition2D periodic = definition("a-periodic", true,
				new double[] {0, 1}, new double[][] {{-1, 1, 0}},
				new double[][] {{0}});
		LocusDefinition2D vertical = definition("b-seam-axis", false,
				new double[] {0, 1}, new double[][] {{0}},
				new double[][] {{1, 0}});
		LocusIntersectionResult2D seam = solve(periodic, vertical);
		assertEquals(1, seam.getFiniteSolutions().size());
		assertEquals(0, firstParameters(seam).get(0), EPSILON);
	}

	@Test
	void tangencyRemainsRichOnlyAndFailClosed() {
		LocusDefinition2D tangent = definition("a-tangent", false,
				new double[] {0, 1}, new double[][] {{1, 0}},
				new double[][] {{1, -1, 0.25}});
		LocusDefinition2D axis = definition("b-tangent-axis", false,
				new double[] {0, 1}, new double[][] {{1, 0}},
				new double[][] {{0}});

		LocusIntersectionResult2D result = solve(tangent, axis);

		assertEquals(ComputationStatus.SUCCESS, result.getComputationStatus());
		assertTrue(result.getFiniteSolutions().size() <= 1,
				() -> "tangent pair parameters=" + parameterPairs(result)
						+ "; diagnostics=" + result.getDiagnostics());
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			assertEquals(LocalIsolationStatus.NOT_ESTABLISHED,
					solution.getPairEvidence().orElseThrow().getLocalIsolation()
							.getStatus());
			assertTrue(result.findPointAdmissibleSolution(
					solution.getIdentity().getRootToken()).isEmpty());
		}
	}

	@Test
	void polynomialOverlapNeverManufacturesFiniteRoots() {
		LocusDefinition2D first = definition("a-overlap", false,
				new double[] {0, 1}, new double[][] {{1, 0}},
				new double[][] {{1, 0}});
		LocusDefinition2D second = definition("b-overlap", false,
				new double[] {0, 1}, new double[][] {{1, 0}},
				new double[][] {{1, 0}});

		LocusIntersectionResult2D result = solve(first, second);

		assertEquals(GeometryKind.UNSUPPORTED_OVERLAP,
				result.getGeometryKind());
		assertTrue(result.getFiniteSolutions().isEmpty());
		assertFalse(result.getOverlapEvidence().isEmpty());
	}

	@Test
	void pairBoxBudgetFailsCoherently() {
		LocusDefinition2D first = definition("a-budget", false,
				new double[] {0, 1}, new double[][] {{1, 0}},
				new double[][] {{1, -1, 0.25}});
		LocusDefinition2D second = definition("b-budget", false,
				new double[] {0, 1}, new double[][] {{1, 0}},
				new double[][] {{0}});
		LocusPairIntersectionPolicy2D initial =
				LocusPairIntersectionPolicy2D.initial(first, second);
		LocusPairIntersectionWorkBudget2D pairBudget =
				new LocusPairIntersectionWorkBudget2D(4, 4, 2, 16, 2, 2,
						80, 32, 4, 4, 4, 0);
		LocusPairIntersectionPolicy2D bounded = new LocusPairIntersectionPolicy2D(
				initial.getPolicyVersion(), initial.getFirstRootTolerance(),
				initial.getSecondRootTolerance(),
				initial.getFirstDeduplicationTolerance(),
				initial.getSecondDeduplicationTolerance(),
				initial.getResidualTolerance(), initial.getTangencyTolerance(),
				initial.getCoordinateTolerance(), initial.getCommonWorkBudget(),
				pairBudget);

		LocusIntersectionResult2D result = solve(first, second, bounded);

		assertEquals(ComputationStatus.WORK_LIMIT_REACHED,
				result.getComputationStatus());
		assertTrue(result.getFiniteSolutions().isEmpty());
	}

	private static LocusIntersectionResult2D solve(LocusDefinition2D first,
			LocusDefinition2D second) {
		return solve(first, second,
				LocusPairIntersectionPolicy2D.initial(first, second));
	}

	private static LocusIntersectionResult2D solve(LocusDefinition2D callerFirst,
			LocusDefinition2D callerSecond,
			LocusPairIntersectionPolicy2D policy) {
		LocusPairIntersectionQuery2D query = new LocusPairIntersectionQuery2D(
				callerFirst, callerSecond, "test/polynomial-pair",
				"test/regular-topology", policy);
		LocusDefinition2D first = query.isCallerOrderCanonical()
				? callerFirst : callerSecond;
		LocusDefinition2D second = query.isCallerOrderCanonical()
				? callerSecond : callerFirst;
		IntersectionSourceBinding2D binding =
				new IntersectionSourceBinding2D(query);
		return new LocusPairIntersectionSolver2D().intersect(query, first, second,
				binding, null, lineage -> LocusPairIdentity2D.solutionToken(
						query.getSourcePairIdentity(),
						query.getConstructiveIntersectionLineage(),
						query.getTopologyContext(), lineage));
	}

	private static List<Double> firstParameters(
			LocusIntersectionResult2D result) {
		ArrayList<Double> parameters = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			parameters.add(solution.getPairEvidence().orElseThrow().getFirst()
					.getSemanticParameter());
		}
		Collections.sort(parameters);
		return parameters;
	}

	private static List<String> parameterPairs(
			LocusIntersectionResult2D result) {
		ArrayList<String> pairs = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			LocusPairIntersectionEvidence2D evidence =
					solution.getPairEvidence().orElseThrow();
			pairs.add(Double.toHexString(evidence.getFirst().getSemanticParameter())
					+ "/" + Double.toHexString(
							evidence.getSecond().getSemanticParameter()));
		}
		Collections.sort(pairs);
		return pairs;
	}

	private static List<String> tokens(LocusIntersectionResult2D result) {
		ArrayList<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			tokens.add(solution.getIdentity().getRootToken());
		}
		Collections.sort(tokens);
		return tokens;
	}

	private static LocusDefinition2D definition(String identity,
			boolean periodic, double[] knots, double[][] x, double[][] y) {
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, !periodic);
		ExplicitNumericDomainProvider2D provider =
				new ExplicitNumericDomainProvider2D(identity + "/parameter", domain,
						Orientation.INCREASING, periodic, 1E-12);
		EnumSet<BranchProperty> properties = EnumSet.of(BranchProperty.FINITE);
		if (periodic) {
			properties.add(BranchProperty.PERIODIC);
		}
		LocusBranch2D branch = new LocusBranch2D(BRANCH, domain,
				List.of(domain), Orientation.INCREASING,
				identity + "/polynomial-branch", LocusLineage2D.unchanged(),
				properties, LocusQuality2D.analyticDoubleSemantic());
		PolynomialEvaluator evaluator = new PolynomialEvaluator(knots, x, y);
		return new LocusDefinition2D(identity, 1, DefinitionStatus.VALID,
				provider, List.of(branch), evaluator,
				Determinism.POINTWISE_DETERMINISTIC,
				evaluator.getPolynomialCapabilitySignature(),
				new LocusInstrumentation2D());
	}

	private static final class PolynomialEvaluator implements LocusEvaluator2D,
			LocusDifferentialEvaluator2D, PiecewisePolynomialLocus2D {
		private final double[] knots;
		private final double[][] x;
		private final double[][] y;

		PolynomialEvaluator(double[] knots, double[][] x, double[][] y) {
			this.knots = knots.clone();
			this.x = copy(x);
			this.y = copy(y);
		}

		@Override
		public LocusEvaluation2D evaluate(LocusDefinition2D definition,
				LocusBranch2D branch, double parameter,
				LocusEvaluationSession2D session) {
			int span = owningSpan(parameter);
			double px = evaluate(x[span], parameter);
			double py = evaluate(y[span], parameter);
			double dx = derivative(x[span], parameter);
			double dy = derivative(y[span], parameter);
			return LocusEvaluation2D.valid(new LocusPoint2D(px, py),
					dx == 0 && dy == 0 ? Regularity.SINGULAR
							: Regularity.REGULAR,
					LocusQuality2D.analyticDoubleSemantic());
		}

		@Override
		public org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D
				evaluateDifferential(
						LocusDefinition2D definition, String branchKey,
						double parameter, LocusEvaluationSession2D session) {
			int span = owningSpan(parameter);
			return org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D.valid(
							derivative(x[span], parameter),
							derivative(y[span], parameter));
		}

		@Override
		public List<Double> getInteriorBreakpoints(String branchKey,
				double lower, double upper) {
			ArrayList<Double> values = new ArrayList<>();
			for (int index = 1; index + 1 < knots.length; index++) {
				if (knots[index] > lower && knots[index] < upper) {
					values.add(knots[index]);
				}
			}
			return Collections.unmodifiableList(values);
		}

		@Override
		public int getPolynomialSpanCount(String branchKey) {
			return x.length;
		}

		@Override
		public double getPolynomialSpanLower(String branchKey, int spanIndex) {
			return knots[spanIndex];
		}

		@Override
		public double getPolynomialSpanUpper(String branchKey, int spanIndex) {
			return knots[spanIndex + 1];
		}

		@Override
		public double[] getPolynomialCoefficients(String branchKey,
				int spanIndex, int coordinate) {
			return (coordinate == 0 ? x : y)[spanIndex].clone();
		}

		@Override
		public LocusPoint2D evaluatePolynomialDerivative(String branchKey,
				double providerCanonicalParameter) {
			int span = owningSpan(providerCanonicalParameter);
			return new LocusPoint2D(
					derivative(x[span], providerCanonicalParameter),
					derivative(y[span], providerCanonicalParameter));
		}

		@Override
		public String getPolynomialCapabilitySignature() {
			return "test-piecewise-polynomial/v1";
		}

		private int owningSpan(double parameter) {
			if (parameter == knots[knots.length - 1]) {
				return knots.length - 2;
			}
			for (int index = 0; index + 1 < knots.length; index++) {
				if (parameter >= knots[index] && parameter < knots[index + 1]) {
					return index;
				}
			}
			throw new IllegalArgumentException("Parameter outside test domain");
		}

		private static double evaluate(double[] coefficients, double parameter) {
			double result = 0;
			for (double coefficient : coefficients) {
				result = result * parameter + coefficient;
			}
			return result;
		}

		private static double derivative(double[] coefficients,
				double parameter) {
			double result = 0;
			int degree = coefficients.length - 1;
			for (int index = 0; index < degree; index++) {
				result = result * parameter
						+ coefficients[index] * (degree - index);
			}
			return result;
		}

		private static double[][] copy(double[][] input) {
			double[][] result = new double[input.length][];
			for (int index = 0; index < input.length; index++) {
				result[index] = input[index].clone();
			}
			return result;
		}
	}
}
