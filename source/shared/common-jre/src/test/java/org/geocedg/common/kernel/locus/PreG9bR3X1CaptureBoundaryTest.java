/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoVector;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3-X1 capture rules of {@code locus-v2-certified-construction-model.md}
 * section 3 and 5.2, exercised on live geos, and the version rule of section 8.
 * The evaluator normally hands the capture its isolated copies; the rules do not
 * depend on which construction the geos belong to.
 */
class PreG9bR3X1CaptureBoundaryTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void aTracedPointThatDoesNotDependOnTheDriverHasNoProgram() {
		// Rule 5 of section 3: a slice disconnected from its driver, the shape the
		// inline-literal evaluator defect produced, is never certified.
		SemanticGeneratorDescriptor1D descriptor = expressionLocus("C+(1,0)");
		GeoElement state = lookup("C");
		GeoElement support = lookup("c");
		assertTrue(capture(descriptor, lookup("E"), state, support).isPresent());
		add("K=(1,1)");
		add("F=K+(1,0)");
		for (String disconnected : List.of("A", "K", "F")) {
			assertTrue(capture(descriptor, lookup(disconnected), state, support).isEmpty(),
					disconnected);
		}
		assertTrue(capture(descriptor, state, state, support).isEmpty());
	}

	@Test
	void undefinedOrNonfiniteVectorObjectsAreRefused() {
		add("u=(1,0)");
		SemanticGeneratorDescriptor1D descriptor = expressionLocus("C+u");
		GeoElement traced = lookup("E");
		GeoElement state = lookup("C");
		GeoElement support = lookup("c");
		GeoVector u = assertInstanceOf(GeoVector.class, lookup("u"));
		assertTrue(capture(descriptor, traced, state, support).isPresent());
		u.setUndefined();
		assertTrue(capture(descriptor, traced, state, support).isEmpty());
		u.setCoords(Double.POSITIVE_INFINITY, 0, 0);
		assertTrue(capture(descriptor, traced, state, support).isEmpty());
		u.setCoords(0, Double.NEGATIVE_INFINITY, 0);
		assertTrue(capture(descriptor, traced, state, support).isEmpty());
		u.setCoords(2, 0.5, 0);
		Step step = capture(descriptor, traced, state, support).orElseThrow()
				.getSteps().get(0);
		assertEquals(2, step.getParameter(0));
		assertEquals(0.5, step.getParameter(1));
	}

	@Test
	void theProgramVersionIsDerivedFromItsSteps() {
		double[] circle = {1, 1, 1, 0, 0, 1, 0, 0};
		LocusInterval2D domain = new LocusInterval2D(-Math.PI, Math.PI, true, false);
		CertifiedConstructionProgram2D translation = new CertifiedConstructionProgram2D(
				Driver.CIRCLE, circle, domain, List.of(new Step(Operation.TRANSLATE,
						List.of(Operand.node(0)), 1, 0)));
		CertifiedConstructionProgram2D expression = new CertifiedConstructionProgram2D(
				Driver.CIRCLE, circle, domain, List.of(new Step(
						Operation.EXPRESSION_TRANSLATE, List.of(Operand.node(0)), 1, 0),
						new Step(Operation.TRANSLATE, List.of(Operand.node(1)), 0, 1)));
		CertifiedConstructionProgram2D affine = new CertifiedConstructionProgram2D(
				Driver.SCALAR_AFFINE, new double[] {1, 0, 0, 1},
				new LocusInterval2D(-2, 2, true, true), List.of());
		assertEquals(CertifiedConstructionProgram2D.VERSION, translation.getVersion());
		assertEquals(CertifiedConstructionProgram2D.VERSION, affine.getVersion());
		assertEquals(CertifiedConstructionProgram2D.VERSION_V2, expression.getVersion());
		assertTrue(translation.getSignature().startsWith(
				CertifiedConstructionProgram2D.VERSION + "|"));
		assertTrue(expression.getSignature().startsWith(
				CertifiedConstructionProgram2D.VERSION_V2 + "|"));
		for (Operation operation : Operation.values()) {
			assertEquals(operation == Operation.EXPRESSION_TRANSLATE, operation.isClassV2(),
					operation.name());
		}
	}

	private SemanticGeneratorDescriptor1D expressionLocus(String definition) {
		getKernel().setContinuous(false);
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("E=" + definition);
		GeoLocusV2 locus = add("p=LocusV2(E,C)");
		return assertInstanceOf(AlgoDependentPointLocusV2.class, locus.getParentAlgorithm())
				.getGeneratorDescriptor();
	}

	private static Optional<CertifiedConstructionProgram2D> capture(
			SemanticGeneratorDescriptor1D descriptor, GeoElement traced, GeoElement state,
			GeoElement support) {
		return CertifiedConstructionCapture2D.capture(descriptor, traced, state, support,
				null);
	}
}
