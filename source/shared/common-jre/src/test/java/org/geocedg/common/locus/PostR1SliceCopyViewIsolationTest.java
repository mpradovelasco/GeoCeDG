/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Post-R1 repair F8: the load of an isolated slice copy never reaches the live view. The
 * copy replaces its anonymous inputs while it loads; that replacement used to sort the
 * drawables of the live view and to repaint it, and a repaint that evaluated a consumer of
 * a nested locus loaded another copy inside the first load, which was then left empty.
 */
final class PostR1SliceCopyViewIsolationTest extends G9U0PublicSurfaceTestBase {

	@Test
	void aLiveNestedLocusKeepsItsDefinitionWhileAConsumerFollowsItsInput() {
		// Lt traces over E2 and reads QT, which follows T; Zt traces over T through a root
		// of Lt. Both circles have an anonymous centre, so the slice of Lt writes two
		// anonymous inputs.
		add("c=Circle((0,0),2)");
		add("E2=Point(c)");
		add("k=Circle((0,0),0.6)");
		add("T=Point(k)");
		add("QO=(2,0)");
		add("QT=Midpoint(T,QO)");
		add("Mt=Midpoint(E2,QT)");
		GeoLocusV2 nested = add("Lt=LocusV2(Mt,E2)");
		assertNotNull(nested);
		add("HA=(4,0.1)");
		add("HB=(-3,0.2)");
		add("l=Line(HA,HB)");
		moveT(0.4);
		GeoLocusIntersectionResult rich = add("v=Intersect(Lt,l)");
		List<LocusIntersectionSolution2D> roots =
				rich.getIntersectionResult().getFiniteSolutions();
		assertEquals(2, roots.size());
		assertNotNull(add("RA=Intersect(v,\"" + roots.get(0).getIdentity().getRootToken()
				+ "\")"));
		add("ZP=Midpoint(RA,T)");
		GeoLocusV2 consumer = add("Zt=LocusV2(ZP,T)");
		assertNotNull(consumer);
		getKernel().updateConstruction(false);

		double parameter = 0.7;
		for (double angle : new double[] {0.4, 1.1, 2.0, -2.5, -1.2, 0.0, 2.9}) {
			moveT(angle);
			assertNotNull(nested.getSemanticDefinition(), "the nested locus at T = " + angle);
			assertNotNull(consumer.getSemanticDefinition(), "the consumer at T = " + angle);
			// Lt is the circle of radius 1 about QT / 2, with QT = (T + QO) / 2.
			LocusEvaluation2D evaluation = nested.evaluate(BRANCH, parameter,
					LocusEvaluationSession2D.reference());
			assertTrue(evaluation.isValid(), angle + ": " + evaluation.getStatus());
			double qx = (0.6 * Math.cos(angle) + 2) / 2;
			double qy = 0.6 * Math.sin(angle) / 2;
			assertEquals(Math.cos(parameter) + qx / 2, evaluation.getPoint().getX(), 1E-9,
					"x at T = " + angle);
			assertEquals(Math.sin(parameter) + qy / 2, evaluation.getPoint().getY(), 1E-9,
					"y at T = " + angle);
		}
	}

	private void moveT(double angle) {
		GeoPoint point = (GeoPoint) getKernel().lookupLabel("T");
		point.setCoords(0.6 * Math.cos(angle), 0.6 * Math.sin(angle), 1.0);
		point.updateCascade();
	}
}
