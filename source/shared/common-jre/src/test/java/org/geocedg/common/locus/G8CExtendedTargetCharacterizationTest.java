/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.OptionalDouble;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.implicit.GeoImplicit;
import org.geogebra.common.kernel.implicit.GeoImplicitCurve;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.junit.jupiter.api.Test;

/** Real-upstream target authority probes for the proposed G8C1 contract. */
class G8CExtendedTargetCharacterizationTest extends BaseUnitTest {

	@Test
	void nondegenerateConicKindsExposeClosedCanonicalGeometry() {
		GeoConic ellipse = add("x^2 / 4 + y^2 = 1");
		GeoConic parabola = add("y^2 = 4x");
		GeoConic hyperbola = add("x^2 / 4 - y^2 = 1");
		assertEquals(GeoConicNDConstants.CONIC_ELLIPSE, ellipse.getType());
		assertEquals(GeoConicNDConstants.CONIC_PARABOLA, parabola.getType());
		assertEquals(GeoConicNDConstants.CONIC_HYPERBOLA, hyperbola.getType());
		assertFalse(ellipse.isDegenerate());
		assertFalse(parabola.isDegenerate());
		assertFalse(hyperbola.isDegenerate());
	}

	@Test
	void degenerateConicsRequireSubtypeSpecificPolicy() {
		GeoConic pair = add("x^2 - y^2 = 0");
		GeoConic doubleLine = add("x^2 = 0");
		assertTrue(pair.isDegenerate());
		assertTrue(doubleLine.isDegenerate());
		assertNotEquals(GeoConicNDConstants.CONIC_ELLIPSE, pair.getType());
		assertNotEquals(GeoConicNDConstants.CONIC_PARABOLA,
				doubleLine.getType());
	}

	@Test
	void conicNormalResidualIsInvariantUnderEquationScaling() {
		GeoConic first = add("x^2 / 4 + y^2 = 1");
		GeoConic scaled = add("1000x^2 + 4000y^2 = 4000");
		LocusPoint2D point = new LocusPoint2D(1.9, 0.2);
		double firstRaw = G8CCharacterizationSupport.conicRawResidual(first,
				point);
		double scaledRaw = G8CCharacterizationSupport.conicRawResidual(scaled,
				point);
		assertNotEquals(Math.abs(firstRaw), Math.abs(scaledRaw), 1E-15);
		assertEquals(Math.abs(normalResidual(first, point)),
				Math.abs(normalResidual(scaled, point)), 2E-14);
	}

	@Test
	void conicContactUsesNormalizedNormalAndSourceTangent() {
		GeoConic ellipse = add("x^2 / 4 + y^2 = 1");
		LocusPoint2D point = new LocusPoint2D(2, 0);
		OptionalDouble tangent = G8CCharacterizationSupport
				.conicContactIndicator(ellipse, point, new LocusPoint2D(0, 7));
		OptionalDouble transverse = G8CCharacterizationSupport
				.conicContactIndicator(ellipse, point, new LocusPoint2D(-3, 0));
		assertTrue(tangent.isPresent());
		assertTrue(transverse.isPresent());
		assertEquals(0, tangent.getAsDouble(), 1E-15);
		assertEquals(1, Math.abs(transverse.getAsDouble()), 1E-15);
	}

	@Test
	void translatedRotatedConicRetainsRegularNormalResidual() {
		GeoConic conic = add("x^2 + 2x y + 3y^2 - 4x + 5y = 1");
		OptionalDouble residual = G8CCharacterizationSupport
				.conicFirstOrderNormalResidual(conic,
						new LocusPoint2D(0.5, -0.25));
		assertTrue(residual.isPresent());
		assertTrue(Double.isFinite(residual.getAsDouble()));
	}

	@Test
	void functionExplicitIntervalIsIndependentOfViewDerivedPathBounds() {
		GeoFunction function = add("f(x)=x^2");
		assertTrue(function.setInterval(-1, 1));
		getApp().getEuclidianView1().setRealWorldCoordSystem(-10, 10, -3, 3);
		double firstPathMinimum = function.getMinParameter();
		getApp().getEuclidianView1().setRealWorldCoordSystem(-0.5, 0.5, -3, 3);
		double secondPathMinimum = function.getMinParameter();
		assertNotEquals(firstPathMinimum, secondPathMinimum);
		assertEquals(-1, function.getIntervalMin(), 0);
		assertEquals(1, function.getIntervalMax(), 0);
	}

	@Test
	void functionPoleIsInvalidDomainEvidenceNotNoIntersection() {
		GeoFunction function = add("f(x)=1/x");
		assertFalse(Double.isFinite(function.value(0)));
		assertTrue(Double.isFinite(function.value(-0.5)));
		assertTrue(Double.isFinite(function.value(0.5)));
	}

	@Test
	void functionResidualDistinguishesVerticalFromFirstOrderNormalQuantity() {
		GeoFunction function = add("f(x)=x^2");
		LocusPoint2D point = new LocusPoint2D(2, 5);
		double vertical = G8CCharacterizationSupport.functionVerticalResidual(
				function, point);
		OptionalDouble normal = G8CCharacterizationSupport
				.functionFirstOrderNormalResidual(function, point);
		assertEquals(1, vertical, 1E-15);
		assertTrue(normal.isPresent());
		assertEquals(1 / Math.sqrt(17), normal.getAsDouble(), 2E-15);
	}

	@Test
	void piecewiseFunctionKeepsUndefinedAndDiscontinuousCasesExplicit() {
		GeoFunction restricted = add("f(x)=If(x < 0, -1, 1)");
		assertEquals(-1, restricted.value(-0.25), 0);
		assertEquals(1, restricted.value(0.25), 0);
		assertNotEquals(restricted.value(-1E-4), restricted.value(1E-4));
	}

	@Test
	void polynomialImplicitNormalResidualIsInvariantUnderScalarScaling() {
		GeoImplicitCurve first = add("x^3 + y^3 = 1");
		GeoImplicitCurve scaled = add("1000x^3 + 1000y^3 = 1000");
		LocusPoint2D point = new LocusPoint2D(0.8, 0.8);
		assertNotEquals(Math.abs(G8CCharacterizationSupport.implicitRawResidual(
				first, point)), Math.abs(G8CCharacterizationSupport
						.implicitRawResidual(scaled, point)), 1E-12);
		assertEquals(Math.abs(implicitNormal(first, point)),
				Math.abs(implicitNormal(scaled, point)), 2E-14);
	}

	@Test
	void singularImplicitRootCannotUseRegularGradientNormalization() {
		GeoImplicitCurve cusp = add("y^2 = x^3");
		LocusPoint2D singular = new LocusPoint2D(0, 0);
		assertEquals(0, cusp.evaluateImplicitCurve(0, 0), 0);
		assertEquals(0, cusp.derivativeX(0, 0), 0);
		assertEquals(0, cusp.derivativeY(0, 0), 0);
		assertTrue(G8CCharacterizationSupport
				.implicitFirstOrderNormalResidual(cusp, singular).isEmpty());
	}

	@Test
	void nonPolynomialImplicitHasEvaluationButNoPolynomialCoefficientAuthority() {
		GeoImplicitCurve implicit = add("sin(x) + y = 0");
		assertEquals(0, implicit.evaluateImplicitCurve(0, 0), 1E-15);
		assertTrue(implicit.getCoeff() == null);
		assertTrue(Double.isFinite(implicit.derivativeX(0, 0)));
	}

	@Test
	void implicitFactorizationIsNotAStablePublicAdapterContract() {
		GeoImplicitCurve components = add(
				"(x^2 + y^2 - 1)(x^2 + y^2 - 4) = 0");
		assertTrue(components.getCoeff() != null);
		assertEquals(0, components.evaluateImplicitCurve(1, 0), 1E-12);
		assertEquals(0, components.evaluateImplicitCurve(2, 0), 1E-12);
		assertTrue(Arrays.stream(GeoImplicit.class.getMethods())
				.noneMatch(method -> method.getName().equals("getFactor")));
	}

	private static double normalResidual(GeoConic conic, LocusPoint2D point) {
		return G8CCharacterizationSupport.conicFirstOrderNormalResidual(conic,
				point).orElseThrow();
	}

	private static double implicitNormal(GeoImplicit implicit,
			LocusPoint2D point) {
		return G8CCharacterizationSupport.implicitFirstOrderNormalResidual(
				implicit, point).orElseThrow();
	}
}
