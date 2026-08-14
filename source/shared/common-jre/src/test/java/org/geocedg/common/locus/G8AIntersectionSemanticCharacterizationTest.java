/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;
import org.geocedg.common.locus.G8AIntersectionNumerics.FactorizationProof;
import org.geocedg.common.locus.G8AIntersectionNumerics.Problem;
import org.geocedg.common.locus.G8AIntersectionNumerics.RootProof;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Completeness;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.ComputationStatus;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Currentness;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.GeometryKind;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Result;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.SourceBinding;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.SupportLevel;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.WorkCounters;
import org.geocedg.common.locus.G8ATargetAdapters.ConicTarget;
import org.geocedg.common.locus.G8ATargetAdapters.FunctionTarget;
import org.geocedg.common.locus.G8ATargetAdapters.ImplicitTarget;
import org.geocedg.common.locus.G8ATargetAdapters.LineTarget;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.implicit.GeoImplicit;
import org.geogebra.common.kernel.implicit.GeoImplicitCurve;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.junit.jupiter.api.Test;

/** Geometry, target-authority and rich-axis characterization. */
class G8AIntersectionSemanticCharacterizationTest extends BaseUnitTest {

	@Test
	void lineAdapterUsesNormalizedHomogeneousEquationAndIgnoresScaling() {
		GeoLine first = add("x + 2y = 3");
		GeoLine scaled = add("1000x + 2000y = 3000");
		LineTarget firstTarget = new LineTarget("line-first", first);
		LineTarget scaledTarget = new LineTarget("line-scaled", scaled);
		for (LocusPoint2D point : List.of(new LocusPoint2D(1, 1),
				new LocusPoint2D(-4, 7), new LocusPoint2D(3, 0))) {
			assertEquals(firstTarget.normalizedResidual(point),
					scaledTarget.normalizedResidual(point), 2E-15);
		}
	}

	@Test
	void segmentAdapterSeparatesSupportResidualFromFiniteMembership() {
		GeoSegment segment = add("Segment((0,0),(2,0))");
		LineTarget target = new LineTarget("segment", segment);
		assertEquals(0, target.normalizedResidual(new LocusPoint2D(1, 0)), 0);
		assertTrue(target.isMember(new LocusPoint2D(1, 0), 0));
		assertTrue(target.isMember(new LocusPoint2D(0, 0), 0));
		assertFalse(target.isMember(new LocusPoint2D(-1, 0), 0));
		assertFalse(target.isMember(new LocusPoint2D(3, 0), 0));
	}

	@Test
	void rayAdapterRejectsBehindStartWithoutProjection() {
		GeoRay ray = add("Ray((0,0),(1,0))");
		LineTarget target = new LineTarget("ray", ray);
		assertTrue(target.isMember(new LocusPoint2D(0, 0), 0));
		assertTrue(target.isMember(new LocusPoint2D(3, 0), 0));
		assertFalse(target.isMember(new LocusPoint2D(-1, 0), 0));
	}

	@Test
	void circleAdapterUsesCapturedConicMatrixAndScaleInvariantResidual() {
		GeoConic circle = add("Circle((0,0),2)");
		ConicTarget target = new ConicTarget("circle", circle, true);
		assertEquals(GeoConicNDConstants.CONIC_CIRCLE, target.conicType());
		assertEquals(0, target.normalizedResidual(new LocusPoint2D(2, 0)),
				2E-15);
		assertNotEquals(0,
				target.normalizedResidual(new LocusPoint2D(1, 0)));
		assertEquals(6, target.matrixSnapshot().length);
	}

	@Test
	void fullConicMatrixExistsButDegenerateSubtypePolicyIsNotUniform() {
		GeoConic ellipse = add("x^2 + 2xy + 2y^2 = 1");
		GeoConic pairOfLines = add("x^2 - y^2 = 0");
		ConicTarget ellipseTarget = new ConicTarget("ellipse", ellipse, false);
		ConicTarget degenerateTarget = new ConicTarget("pair", pairOfLines, false);
		assertEquals(GeoConicNDConstants.CONIC_ELLIPSE,
				ellipseTarget.conicType());
		assertTrue(degenerateTarget.conicType()
				== GeoConicNDConstants.CONIC_INTERSECTING_LINES
				|| degenerateTarget.conicType()
						== GeoConicNDConstants.CONIC_DOUBLE_LINE);
		assertNotEquals(ellipseTarget.conicType(), degenerateTarget.conicType());
	}

	@Test
	void parabolaHyperbolaAndRotatedConicExposeTypesButNotRootCertificates() {
		GeoConic parabola = add("y=x^2");
		GeoConic hyperbola = add("x*y=1");
		GeoConic rotated = add("x^2+2x*y+3y^2=1");
		ConicTarget parabolaTarget = new ConicTarget("parabola", parabola,
				false);
		ConicTarget hyperbolaTarget = new ConicTarget("hyperbola", hyperbola,
				false);
		ConicTarget rotatedTarget = new ConicTarget("rotated", rotated, false);
		assertEquals(GeoConicNDConstants.CONIC_PARABOLA,
				parabolaTarget.conicType());
		assertEquals(GeoConicNDConstants.CONIC_HYPERBOLA,
				hyperbolaTarget.conicType());
		assertEquals(GeoConicNDConstants.CONIC_ELLIPSE,
				rotatedTarget.conicType());
		assertTrue(Double.isFinite(parabolaTarget.rawResidual(
				new LocusPoint2D(1, 1))));
		assertTrue(Double.isFinite(hyperbolaTarget.rawResidual(
				new LocusPoint2D(1, 1))));
		assertTrue(Double.isFinite(rotatedTarget.rawResidual(
				new LocusPoint2D(0, 0))));
	}

	@Test
	void functionAdapterUsesExplicitFunctionDomainNotViewBounds() {
		GeoFunction unrestricted = add("f(x)=x^2");
		GeoFunction restricted = add("g(x)=x^2");
		assertTrue(restricted.setInterval(-1, 1));
		FunctionTarget unrestrictedTarget =
				new FunctionTarget("function-unrestricted", unrestricted);
		FunctionTarget restrictedTarget =
				new FunctionTarget("function-restricted", restricted);
		assertFalse(unrestricted.hasInterval());
		assertTrue(restricted.hasInterval());
		assertTrue(unrestrictedTarget.isMember(new LocusPoint2D(20, 400), 0));
		assertFalse(restrictedTarget.isMember(new LocusPoint2D(2, 4), 0));
		assertEquals(0,
				restrictedTarget.normalizedResidual(new LocusPoint2D(0.5, 0.25)),
				2E-15);
	}

	@Test
	void implicitInterfaceExposesEvaluationDerivativeAndPolynomialAuthority()
			throws ReflectiveOperationException {
		GeoImplicitCurve implicit = add("x^3 + y^3 = 1");
		ImplicitTarget target = new ImplicitTarget("implicit", implicit);
		assertEquals(0, target.rawResidual(new LocusPoint2D(1, 0)), 2E-15);
		assertEquals(3, implicit.derivativeX(1, 0), 2E-15);
		assertTrue(implicit.getCoeff().length > 0);
		for (String methodName : List.of("evaluateImplicitCurve", "derivativeX",
				"derivativeY", "getCoeff")) {
			Method method = findMethod(GeoImplicit.class, methodName);
			assertTrue(method.getDeclaringClass().isAssignableFrom(
					GeoImplicitCurve.class));
		}
	}

	@Test
	void completeEmptyAndVerifiedIncompleteFiniteAreIndependentStates() {
		Result empty = emptyResult();
		assertEquals(GeometryKind.EMPTY, empty.geometryKind());
		assertEquals(Completeness.COMPLETE, empty.completeness());
		assertFalse(empty.pointProjectionAdmissible());

		Problem problem = parabolaProblem(1);
		Result incomplete = G8AIntersectionNumerics.conservativeBroadPhase(problem,
				List.of(new G8AIntersectionNumerics.ParameterInterval(-1.1, -0.9)));
		assertEquals(GeometryKind.FINITE, incomplete.geometryKind());
		assertEquals(Completeness.INCOMPLETE, incomplete.completeness());
		assertEquals(1, incomplete.solutions().size());
		assertFalse(incomplete.pointProjectionAdmissible());
	}

	@Test
	void illegalIncompleteEmptyStateIsRejectedRatherThanEncodedByMagicValue() {
		SourceBinding source = new SourceBinding("pair", "locus", 1, "target",
				1, "policy");
		WorkCounters counters = new WorkCounters();
		assertThrows(IllegalArgumentException.class, () -> new Result(source,
				ComputationStatus.SUCCESS, Completeness.NOT_ESTABLISHED,
				GeometryKind.EMPTY, Currentness.CURRENT, SupportLevel.UNSUPPORTED,
				NumericGuarantee.FLOATING_POINT_UNCERTIFIED, List.of(),
				counters.snapshot(), List.of()));
	}

	@Test
	void overlapIsTypedAndCannotBecomeAnArbitraryPointSample() {
		G8AIntersectionFixtures.Fixture fixture = G8AIntersectionFixtures.create(
				getConstruction(), "overlap", -1, 1, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, 0),
				parameter -> new LocusPoint2D(1, 0));
		FactorizationProof overlap = new FactorizationProof(1, List.of(), true,
				"identically-zero polynomial");
		Result result = G8AIntersectionNumerics.analyticFactorization(
				fixture.problem(new LineTarget("axis", 0, 1, 0), overlap,
						"topology-1"));
		assertEquals(GeometryKind.OVERLAP, result.geometryKind());
		assertEquals(Completeness.COMPLETE, result.completeness());
		assertTrue(result.solutions().isEmpty());
		assertFalse(result.pointProjectionAdmissible());
	}

	@Test
	void repeatedCoordinatesRetainDistinctConstructivePreimages() {
		G8AIntersectionFixtures.Fixture fixture = G8AIntersectionFixtures.create(
				getConstruction(), "repeated-preimage", -2, 2, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter * parameter - 1, 0),
				parameter -> new LocusPoint2D(2 * parameter, 0));
		FactorizationProof proof = new FactorizationProof(1,
				List.of(new RootProof("negative-preimage", -1, 1),
						new RootProof("positive-preimage", 1, 1)), false,
				"(t+1)(t-1)");
		Result result = G8AIntersectionNumerics.analyticFactorization(
				fixture.problem(new LineTarget("vertical-axis", 1, 0, 0), proof,
						"topology-1"));
		assertEquals(2, result.solutions().size());
		assertEquals(result.solutions().get(0).point(),
				result.solutions().get(1).point());
		assertNotEquals(result.solutions().get(0).durableIdentity().rootToken(),
				result.solutions().get(1).durableIdentity().rootToken());
	}

	@Test
	void allSemanticStrategiesKeepForbiddenAuthorityCountersAtZero() {
		Problem problem = parabolaProblem(1);
		for (Result result : List.of(
				G8AIntersectionNumerics.analyticFactorization(problem),
				G8AIntersectionNumerics.certifiedBrackets(problem),
				G8AIntersectionNumerics.derivativeAware(problem),
				G8AIntersectionNumerics.evaluatorOnly(problem, 128))) {
			assertTrue(result.work().hardZeroAuthorityReads());
		}
	}

	private Problem parabolaProblem(double height) {
		double root = Math.sqrt(height);
		G8AIntersectionFixtures.Fixture fixture = G8AIntersectionFixtures.create(
				getConstruction(), "parabola-" + height, -2, 2, true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, parameter * parameter),
				parameter -> new LocusPoint2D(1, 2 * parameter));
		FactorizationProof proof = new FactorizationProof(1,
				List.of(new RootProof("left", -root, 1),
						new RootProof("right", root, 1)), false,
				"t^2-height");
		return fixture.problem(new LineTarget("horizontal-" + height,
				0, 1, -height), proof, "topology-1");
	}

	private static Result emptyResult() {
		SourceBinding source = new SourceBinding("pair", "locus", 1, "target",
				1, "policy");
		return new Result(source, ComputationStatus.SUCCESS, Completeness.COMPLETE,
				GeometryKind.EMPTY, Currentness.CURRENT, SupportLevel.EXACT,
				NumericGuarantee.EXACT_ARITHMETIC, List.of(),
				new WorkCounters().snapshot(), List.of("exhaustive exclusion"));
	}

	private static Method findMethod(Class<?> type, String name) {
		for (Method method : type.getDeclaredMethods()) {
			if (method.getName().equals(name)) {
				return method;
			}
		}
		throw new IllegalArgumentException("Missing method: " + name);
	}
}
