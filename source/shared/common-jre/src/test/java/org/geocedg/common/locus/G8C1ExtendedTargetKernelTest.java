/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ResidualQuantityKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetEvaluationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetSupportStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionTargetSupport2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTarget2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTargets2D;
import org.geocedg.common.kernel.locus.intersection.TargetResidual2D;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.implicit.GeoImplicitCurve;
import org.junit.jupiter.api.Test;

/** Productive G8C1 target, residual, isolation and classification tests. */
class G8C1ExtendedTargetKernelTest extends BaseUnitTest {

	@Test
	void closedTargetFamiliesAcceptOnlyTheAuthorizedSubsets() {
		assertEquals(TargetFamily.ELLIPSE,
				LocusIntersectionTargets2D.familyOf(
						add("x^2 / 4 + y^2 = 1")));
		assertEquals(TargetFamily.PARABOLA,
				LocusIntersectionTargets2D.familyOf(add("y^2 = 4x")));
		assertEquals(TargetFamily.HYPERBOLA,
				LocusIntersectionTargets2D.familyOf(
						add("x^2 / 4 - y^2 = 1")));
		GeoFunction function = bounded("f(x)=x^2", -2, 2);
		assertEquals(TargetFamily.BOUNDED_FUNCTION_GRAPH,
				LocusIntersectionTargets2D.familyOf(function));
		assertEquals(TargetFamily.REGULAR_POLYNOMIAL_IMPLICIT,
				LocusIntersectionTargets2D.familyOf(
						add("x^3 + y^3 = 1")));
	}

	@Test
	void unsupportedSubtypesHaveTypedClosedDecisions() {
		IntersectionTargetSupport2D degenerate =
				LocusIntersectionTargets2D.assess(add("x^2-y^2=0"));
		assertEquals(TargetSupportStatus.UNSUPPORTED_TARGET_SUBTYPE,
				degenerate.getStatus());
		GeoFunction unbounded = add("u(x)=x^2");
		assertEquals(TargetSupportStatus.DOMAIN_NOT_EXPLICIT,
				LocusIntersectionTargets2D.assess(unbounded).getStatus());
		GeoImplicitCurve nonPolynomial = add("sin(x)+y=0");
		assertEquals(TargetSupportStatus.NONPOLYNOMIAL_IMPLICIT,
				LocusIntersectionTargets2D.assess(nonPolynomial).getStatus());
	}

	@Test
	void conicResidualIsFirstOrderLengthAndEquationScaleInvariant() {
		GeoConic first = add("x^2 / 4 + y^2 = 1");
		GeoConic scaled = add("1000x^2 + 4000y^2 = 4000");
		LocusPoint2D point = new LocusPoint2D(1.9, 0.2);
		LocusIntersectionTarget2D firstTarget = capture(first, "conic-one");
		LocusIntersectionTarget2D scaledTarget = capture(scaled, "conic-two");
		TargetResidual2D one = firstTarget.evaluateResidual(point);
		TargetResidual2D two = scaledTarget.evaluateResidual(point);
		assertEquals(ResidualQuantityKind.FIRST_ORDER_NORMAL_LENGTH,
				one.getContract().getQuantityKind());
		assertTrue(one.getContract().getNormalizationProvenance()
				.contains("not exact Euclidean distance"));
		assertEquals(Math.abs(one.getNormalizedResidual()),
				Math.abs(two.getNormalizedResidual()), 2E-14);
	}

	@Test
	void translatedRotatedConicRetainsFiniteNormalizedAuthority() {
		GeoConic conic = add("x^2 + 2x y + 3y^2 - 4x + 5y = 1");
		TargetResidual2D residual = capture(conic, "rotated")
				.evaluateResidual(new LocusPoint2D(0.5, -0.25));
		assertTrue(Double.isFinite(residual.getNormalizedResidual()));
		assertTrue(residual.getCharacteristicScale() > 0);
	}

	@Test
	void functionResidualIsTypedVerticalLengthAndHonorsExplicitDomain() {
		GeoFunction function = bounded("f(x)=x^2", -1, 1);
		LocusIntersectionTarget2D target = capture(function, "function");
		assertEquals(ResidualQuantityKind.VERTICAL_MODEL_LENGTH,
				target.getResidualContract().getQuantityKind());
		assertTrue(target.getResidualContract().getNormalizationProvenance()
				.contains("not Euclidean distance"));
		assertEquals(1, target.evaluateResidual(new LocusPoint2D(1, 2))
				.getNormalizedResidual(), 0);
		assertEquals(TargetEvaluationStatus.OUTSIDE_EXPLICIT_DOMAIN,
				target.evaluateResidualEvidence(new LocusPoint2D(2, 4))
						.getStatus());
	}

	@Test
	void functionPoleIsTypedUndefinedAndNeverAResidualNumber() {
		GeoFunction function = bounded("f(x)=1/x", -1, 1);
		LocusIntersectionTarget2D target = capture(function, "pole");
		assertEquals(TargetEvaluationStatus.TARGET_UNDEFINED,
				target.evaluateCandidateLevel(new LocusPoint2D(0, 0)).getStatus());
		assertFalse(target.evaluateResidualEvidence(new LocusPoint2D(0, 0))
				.getResidual().isPresent());
	}

	@Test
	void polynomialImplicitResidualIsFirstOrderAndScaleInvariant() {
		GeoImplicitCurve first = add("x^3+y^3=1");
		GeoImplicitCurve scaled = add("1000x^3+1000y^3=1000");
		LocusPoint2D point = new LocusPoint2D(0.8, 0.8);
		TargetResidual2D one = capture(first, "implicit-one")
				.evaluateResidual(point);
		TargetResidual2D two = capture(scaled, "implicit-two")
				.evaluateResidual(point);
		assertEquals(ResidualQuantityKind.FIRST_ORDER_NORMAL_LENGTH,
				one.getContract().getQuantityKind());
		assertEquals(Math.abs(one.getNormalizedResidual()),
				Math.abs(two.getNormalizedResidual()), 2E-14);
	}

	@Test
	void singularImplicitPointIsExplicitlyOutsideRegularAdapter() {
		GeoImplicitCurve cusp = add("y^2=x^3");
		LocusIntersectionTarget2D target = capture(cusp, "cusp");
		assertEquals(TargetEvaluationStatus.UNSUPPORTED_LOCAL_GEOMETRY,
				target.evaluateResidualEvidence(new LocusPoint2D(0, 0))
						.getStatus());
		assertFalse(target.evaluateResidualEvidence(new LocusPoint2D(0, 0))
				.getResidual().isPresent());
	}

	@Test
	void ellipseSecantPublishesTwoVerifiedRootsWithoutFalseCompleteness() {
		var locus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"ellipse-secant", -3, 3, 0);
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), locus, add("x^2 / 4 + y^2 = 1"),
				"ellipse-secant");
		assertFiniteRoots(result, List.of(-2.0, 2.0), 2E-11);
		assertEquals(Completeness.NOT_ESTABLISHED,
				result.getCompletenessEvidence().getCompleteness());
	}

	@Test
	void uniqueEllipseRootIsLocallyEstablishedForOptionB() {
		var locus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"ellipse-unique", 0, 3, 0);
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), locus, add("x^2 / 4 + y^2 = 1"),
				"ellipse-unique");
		assertFiniteRoots(result, List.of(2.0), 2E-11);
		var solution = result.getFiniteSolutions().get(0);
		assertEquals(LocalIsolationStatus.ESTABLISHED,
				solution.getRevisionEvidence().getLocalIsolationStatus());
		assertTrue(solution.getIdentity().getExplicitContinuationKey().isPresent());
		assertTrue(result.findPointAdmissibleSolution(
				solution.getIdentity().getRootToken()).isPresent());
	}

	@Test
	void ellipseTangencyUsesLocalMinimumAndDoesNotInventIsolation() {
		var locus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"ellipse-tangent", -2, 2, 1);
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), locus, add("x^2 / 4 + y^2 = 1"),
				"ellipse-tangent");
		assertFiniteRoots(result, List.of(0.0), 2E-11);
		var solution = result.getFiniteSolutions().get(0);
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				solution.getClassification().getContactClass());
		assertEquals(LocalIsolationStatus.NOT_ESTABLISHED,
				solution.getRevisionEvidence().getLocalIsolationStatus());
		assertFalse(result.findPointAdmissibleSolution(
				solution.getIdentity().getRootToken()).isPresent());
	}

	@Test
	void ellipseNearTangencyRetainsTwoConstructivePreimages() {
		var locus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"ellipse-near", -1, 1, 0.99);
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), locus, add("x^2 / 4 + y^2 = 1"),
				"ellipse-near");
		assertEquals(2, result.getFiniteSolutions().size());
		assertTrue(result.getFiniteSolutions().get(0).getEvaluatedPoint().getX()
				< result.getFiniteSolutions().get(1).getEvaluatedPoint().getX());
	}

	@Test
	void parabolaAndHyperbolaUseTheSameOneParameterPipeline() {
		var parabolaLocus = G8C1IntersectionTestSupport.horizontal(
				getConstruction(), "parabola-target", -2, 6, 2);
		LocusIntersectionResult2D parabola = G8C1IntersectionTestSupport.result(
				getConstruction(), parabolaLocus, add("y^2=4x"),
				"parabola-target");
		assertFiniteRoots(parabola, List.of(1.0), 2E-11);
		var hyperbolaLocus = G8C1IntersectionTestSupport.horizontal(
				getConstruction(), "hyperbola-target", -3, 3, 0);
		LocusIntersectionResult2D hyperbola = G8C1IntersectionTestSupport.result(
				getConstruction(), hyperbolaLocus, add("x^2-y^2=1"),
				"hyperbola-target");
		assertFiniteRoots(hyperbola, List.of(-1.0, 1.0), 2E-11);
	}

	@Test
	void boundedPolynomialAndTrigonometricFunctionsFindRegularRoots() {
		var polynomialLocus = G8C1IntersectionTestSupport.horizontal(
				getConstruction(), "function-polynomial", -2, 2, 1);
		LocusIntersectionResult2D polynomial = G8C1IntersectionTestSupport.result(
				getConstruction(), polynomialLocus,
				bounded("f(x)=x^2", -2, 2), "function-polynomial");
		assertFiniteRoots(polynomial, List.of(-1.0, 1.0), 2E-11);
		var trigLocus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"function-trig", -1, 1, 0);
		LocusIntersectionResult2D trig = G8C1IntersectionTestSupport.result(
				getConstruction(), trigLocus,
				bounded("g(x)=sin(x)", -1, 1), "function-trig");
		assertFiniteRoots(trig, List.of(0.0), 2E-11);
	}

	@Test
	void functionPoleAndConditionalJumpAreHardIsolationBarriers() {
		var locus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"function-gaps", -1, 1, 0);
		LocusIntersectionResult2D pole = G8C1IntersectionTestSupport.result(
				getConstruction(), locus, bounded("p(x)=1/x", -1, 1),
				"function-pole");
		assertEquals(GeometryKind.UNRESOLVED, pole.getGeometryKind());
		assertTrue(pole.getWork().getInvalidTargetEvaluations() > 0);
		LocusIntersectionResult2D jump = G8C1IntersectionTestSupport.result(
				getConstruction(), locus,
				bounded("j(x)=If(x<0,-1,1)", -1, 1), "function-jump");
		assertEquals(GeometryKind.UNRESOLVED, jump.getGeometryKind());
		assertEquals(0, jump.getFiniteSolutions().size());
	}

	@Test
	void functionDomainEndpointIsAnIncludedTargetBoundary() {
		var locus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"function-endpoint", -2, 2, 1);
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), locus, bounded("f(x)=x", -1, 1),
				"function-endpoint");
		assertFiniteRoots(result, List.of(1.0), 2E-11);
		assertTrue(result.getFiniteSolutions().get(0).getClassification()
				.isTargetIncludedBoundary());
	}

	@Test
	void regularPolynomialImplicitFindsOneRootWithVerifiedResidual() {
		var locus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"implicit-cubic", -2, 2, 0);
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), locus, add("x^3+y^3=1"),
				"implicit-cubic");
		assertFiniteRoots(result, List.of(1.0), 2E-11);
		assertTrue(Math.abs(result.getFiniteSolutions().get(0)
				.getRevisionEvidence().getResidualEvidence()
				.getNormalizedResidual()) <= 2E-12);
	}

	@Test
	void polynomialImplicitComponentsPreserveDistinctSemanticRoots() {
		var locus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"implicit-components", 0, 3, 0);
		GeoImplicitCurve components = add(
				"(x^2+y^2-1)(x^2+y^2-4)=0");
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), locus, components, "implicit-components");
		assertFiniteRoots(result, List.of(1.0, 2.0), 2E-11);
		assertFalse(result.getFiniteSolutions().get(0).getIdentity().getRootToken()
				.equals(result.getFiniteSolutions().get(1).getIdentity()
						.getRootToken()));
	}

	@Test
	void singularImplicitRootIsNotPublishedAsRegularFiniteSolution() {
		var locus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"implicit-singular", -1, 1, 0);
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), locus, add("y^2=x^3"),
				"implicit-singular");
		assertEquals(GeometryKind.UNRESOLVED, result.getGeometryKind());
		assertEquals(0, result.getFiniteSolutions().size());
		assertTrue(result.getWork().getInvalidTargetEvaluations() > 0);
	}

	@Test
	void sampledZeroRunDoesNotManufactureOverlapPoints() {
		var locus = G8C1IntersectionTestSupport.curve(getConstruction(),
				"implicit-overlap", -1, 1,
				(source, branch, parameter) ->
						new LocusPoint2D(parameter, parameter * parameter));
		LocusIntersectionResult2D result = G8C1IntersectionTestSupport.result(
				getConstruction(), locus, add("y-x^2=0"),
				"implicit-overlap");
		assertEquals(GeometryKind.UNRESOLVED, result.getGeometryKind());
		assertEquals(0, result.getFiniteSolutions().size());
		assertEquals(Completeness.NOT_ESTABLISHED,
				result.getCompletenessEvidence().getCompleteness());
	}

	@Test
	void functionAndImplicitEvenRootsAreFoundWithoutSignChanges() {
		var functionLocus = G8C1IntersectionTestSupport.horizontal(
				getConstruction(), "function-even", -1, 1, 0);
		LocusIntersectionResult2D function = G8C1IntersectionTestSupport.result(
				getConstruction(), functionLocus,
				bounded("f(x)=x^2", -1, 1), "function-even");
		assertFiniteRoots(function, List.of(0.0), 2E-11);
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				function.getFiniteSolutions().get(0).getClassification()
						.getContactClass());
		var implicitLocus = G8C1IntersectionTestSupport.horizontal(
				getConstruction(), "implicit-even", -1, 1, 0);
		LocusIntersectionResult2D implicit = G8C1IntersectionTestSupport.result(
				getConstruction(), implicitLocus, add("y-x^2=0"),
				"implicit-even");
		assertFiniteRoots(implicit, List.of(0.0), 2E-11);
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				implicit.getFiniteSolutions().get(0).getClassification()
						.getContactClass());
	}

	@Test
	void authoritativeCapabilityCanProveCompleteEmptyExtendedQuery() {
		var locus = G8C1IntersectionTestSupport.horizontal(getConstruction(),
				"complete-empty", -1, 1, 0);
		var algorithm = G8C1IntersectionTestSupport.algorithm(getConstruction(),
				locus, add("x^2/4+y^2=1"), "complete-empty",
				G8BIntersectionFixtures.capability("complete-empty/v1",
						G8BIntersectionFixtures::completeEmpty),
				new org.geogebra.common.kernel.geos.GeoElement[0]);
		LocusIntersectionResult2D result = algorithm.getResult()
				.getIntersectionResult();
		assertEquals(GeometryKind.EMPTY, result.getGeometryKind());
		assertEquals(Completeness.COMPLETE,
				result.getCompletenessEvidence().getCompleteness());
		assertEquals(0, result.getFiniteSolutions().size());
	}

	private GeoFunction bounded(String expression, double lower, double upper) {
		GeoFunction function = add(expression);
		assertTrue(function.setInterval(lower, upper));
		return function;
	}

	private static LocusIntersectionTarget2D capture(
			org.geogebra.common.kernel.geos.GeoElement target, String identity) {
		return LocusIntersectionTargets2D.capture(target, identity, 1);
	}

	private static void assertFiniteRoots(LocusIntersectionResult2D result,
			List<Double> expected, double tolerance) {
		assertEquals(ComputationStatus.SUCCESS, result.getComputationStatus());
		assertEquals(GeometryKind.FINITE, result.getGeometryKind());
		List<Double> actual = G8C1IntersectionTestSupport.parameters(result);
		assertEquals(expected.size(), actual.size());
		for (int index = 0; index < expected.size(); index++) {
			assertEquals(expected.get(index), actual.get(index), tolerance);
		}
	}
}
