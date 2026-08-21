/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;
import org.geocedg.common.kernel.locus.SemanticGeneratorFamily1D;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLocus;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Regression for the reported public circle-point construction. */
class LocusV2PointDrivenCreationRegressionTest
		extends G9U0PublicSurfaceTestBase {

	@Test
	void reportedCirclePointConstructionUsesNormalV2DagAndRecomputes() {
		ReportedConstruction fixture = reportedConstruction();
		GeoLocusV2 locus = add("L=LocusV2(E,C)");

		assertNotNull(locus);
		assertFalse(GeoLocus.class.isInstance(locus));
		assertTrue(locus.getParentAlgorithm()
				instanceof AlgoDependentPointLocusV2);
		AlgoDependentPointLocusV2 parent =
				(AlgoDependentPointLocusV2) locus.getParentAlgorithm();
		assertSame(fixture.dependent, parent.getInput(0));
		assertSame(fixture.driver, parent.getInput(1));
		assertEquals(SemanticGeneratorFamily1D.CIRCLE_POINT,
				parent.getGeneratorDescriptor().getFamily());
		assertNotNull(locus.getPersistentLocusId());

		assertTrue(fixture.driver.isParentOf(fixture.line));
		assertTrue(fixture.line.isParentOf(fixture.intersection));
		assertTrue(fixture.driver.isParentOf(fixture.dependent));
		assertTrue(fixture.intersection.isParentOf(fixture.dependent));
		assertTrue(fixture.dependent.isParentOf(locus));
		assertTrue(fixture.driver.isParentOf(locus));
		assertMidpoint(fixture);
		assertCircleImage(fixture, locus);

		String persistentId = locus.getPersistentLocusId().toExternalForm();
		long revision = locus.getSemanticRevision();
		long dependencyUpdates = locus.getInstrumentation().getDependencyUpdates();
		fixture.driver.getPathParameter().setT(0.75);
		fixture.circle.pathChanged(fixture.driver);
		fixture.driver.updateCascade();

		assertTrue(locus.getInstrumentation().getDependencyUpdates()
				> dependencyUpdates);
		assertTrue(locus.getSemanticRevision() > revision);
		assertEquals(persistentId,
				locus.getPersistentLocusId().toExternalForm());
		assertMidpoint(fixture);

		revision = locus.getSemanticRevision();
		fixture.radius.setValue(3);
		fixture.radius.updateCascade();

		assertTrue(locus.getSemanticRevision() > revision);
		assertTrue(locus.isDefined());
		assertEquals(persistentId,
				locus.getPersistentLocusId().toExternalForm());
		assertCircleImage(fixture, locus);
	}

	private ReportedConstruction reportedConstruction() {
		GeoPoint center = add("O=(1,2)");
		GeoNumeric radius = add("r=2");
		GeoConic circle = add("c=Circle(O,r)");
		GeoPoint driver = add("C=Point(c)");
		GeoElement line = add("g=PerpendicularLine(C,yAxis)");
		GeoPoint intersection = add("D=Intersect(g,yAxis)");
		GeoPoint dependent = add("E=Midpoint(C,D)");
		return new ReportedConstruction(center, radius, circle, driver, line,
				intersection, dependent);
	}

	private static void assertMidpoint(ReportedConstruction fixture) {
		assertEquals((fixture.driver.getInhomX()
				+ fixture.intersection.getInhomX()) / 2,
				fixture.dependent.getInhomX(), 1E-10);
		assertEquals((fixture.driver.getInhomY()
				+ fixture.intersection.getInhomY()) / 2,
				fixture.dependent.getInhomY(), 1E-10);
	}

	private static void assertCircleImage(ReportedConstruction fixture,
			GeoLocusV2 locus) {
		double[] parameters = {-Math.PI, -1, 0, 1, Math.PI - 1E-9};
		for (double parameter : parameters) {
			LocusEvaluation2D evaluation = locus.evaluate(
					SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY, parameter,
					LocusEvaluationSession2D.memoizing(8));
			assertTrue(evaluation.isValid());
			double x = 2 * evaluation.getPoint().getX()
					- fixture.center.getInhomX();
			double y = evaluation.getPoint().getY()
					- fixture.center.getInhomY();
			assertEquals(fixture.radius.getDouble() * fixture.radius.getDouble(),
					x * x + y * y, 1E-8);
		}
	}

	private static final class ReportedConstruction {
		private final GeoPoint center;
		private final GeoNumeric radius;
		private final GeoConic circle;
		private final GeoPoint driver;
		private final GeoElement line;
		private final GeoPoint intersection;
		private final GeoPoint dependent;

		private ReportedConstruction(GeoPoint center, GeoNumeric radius,
				GeoConic circle, GeoPoint driver, GeoElement line,
				GeoPoint intersection, GeoPoint dependent) {
			this.center = center;
			this.radius = radius;
			this.circle = circle;
			this.driver = driver;
			this.line = line;
			this.intersection = intersection;
			this.dependent = dependent;
		}
	}
}
