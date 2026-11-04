/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.*;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointContinuation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionWorkBudget2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Captured similarities transport original predicates/jets instead of changing preimages. */
class R4SimilarityContinuationTest extends R4ContinuationTestBase {
	@ParameterizedTest @ValueSource(strings = {"Translate(L,(3,4))", "Rotate(L,pi/6)",
			"Dilate(L,-2)", "Rotate(Translate(L,(3,4)),pi/6)", "Mirror(L,(1,2))",
			"Mirror(L,xAxis)", "Mirror(Rotate(Dilate(L,-2),pi/6),xAxis)"})
	void offeredParabolaMovesBothWaysThroughTypedInvertibleAndNestedImages(String expression) {
		certifiedParabola(); GeoLocusV2 image = add("Image=" + expression);
		GeoPoint point = offered(image, .6, "P"); var old = address(point);
		GeoPoint dependent = add("Dependent=P+(1,2)");
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		for (double parameter : new double[] {.61, .65, .63, .6}) {
			var target = at(image, parameter); var result = move(point, context, target.getX(), target.getY());
			assertNull(result.getHistoricalResult());
			assertTrue(result.getContinuationSimilarityApplications() > 0);
			assertTrue(result.getContinuationCorrespondenceChecks() > 1);
			assertEquals(parameter, address(point).getCanonicalParameter(), 3e-11);
			assertEquals(old.getComponentLineageKey(), address(point).getComponentLineageKey());
			assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point));
			assertEquals(point.inhomX + 1, dependent.inhomX, 0); assertEquals(point.inhomY + 2, dependent.inhomY, 0);
		}
	}

	@Test void nestedSimilarityAlsoSupportsOneEventChainAcrossACertifiedSeam() {
		meetCircle(); GeoLocusV2 image = add("Image=Rotate(Translate(Dilate(L,-2),(3,4)),pi/6)");
		GeoPoint point = offered(image, 2.9, "P");
		policy = LocusPointInteractionPolicy2D.initial(1);
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point);
		var target = at(image, 3); move(point, context, target.getX(), target.getY());
		target = at(image, 4.18); var result = move(point, context, target.getX(), target.getY());
		assertTrue(result.getContinuationCharts() > 1); assertEquals(4.18, lifted(point), 3e-11);
		assertEquals(1, address(point).getPeriodicLift());
	}

	@Test void collapsedImageDoesNotGainPermissionThroughTheAdapter() {
		GeoLocusV2 source = certifiedParabola(); GeoLocusV2 image = add("Image=Dilate(L,0)");
		GeoPoint explicit = add("Explicit=Point(Image,\"" + BRANCH + "\",.6)");
		String xml = getApp().getXML();
		var proof = LocusPointContinuation2D.resolve(image, address(explicit), 0, 0, policy);
		assertFalse(proof.isEstablished()); assertEquals(xml, getApp().getXML());
		assertNotNull(source.getSemanticDefinition());
	}

	@Test void compositionBudgetExhaustionDoesNotMutateTheOfferedImagePoint() {
		certifiedParabola(); GeoLocusV2 image = add("Image=Rotate(Translate(L,(3,4)),pi/6)");
		GeoPoint point = offered(image, .6, "P"); var old = address(point); String xml = getApp().getXML();
		var context = LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point); var target = at(image, .61);
		var limit = new LocusPointInteractionPolicy2D(.5, 1E-10, 1E-12,
				new LocusPointInteractionWorkBudget2D(32768, 3, 80, 1024));
		var result = LocusV2PublicOperations.moveGeneralInteractiveSemanticPoint(point,
				target.getX(), target.getY(), limit, context);
		assertEquals(LocusPointContinuation2D.Status.BUDGET_EXHAUSTED, result.getStatus());
		assertEquals(3, result.getIntervalPasses()); assertEquals(old, address(point)); assertEquals(xml, getApp().getXML());
	}

	@Test void nestedImagePersistsItsOriginalSemanticAddressAndContinuesAfterReopen() {
		certifiedParabola(); GeoLocusV2 image = add("Image=Rotate(Translate(L,(3,4)),pi/6)");
		GeoPoint point = offered(image, .6, "P"); var target = at(image, .61);
		move(point, LocusV2PublicOperations.beginInteractiveSemanticPointDrag(point), target.getX(), target.getY());
		var id = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point); var saved = address(point);
		getApp().setXML(getApp().getXML(), true);
		GeoPoint reopened = (GeoPoint) lookup("P"); GeoLocusV2 source = (GeoLocusV2) lookup("Image");
		assertEquals(saved, address(reopened)); assertEquals(id, getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(reopened));
		target = at(source, .6); move(reopened, LocusV2PublicOperations.beginInteractiveSemanticPointDrag(reopened), target.getX(), target.getY());
		assertEquals(.6, address(reopened).getCanonicalParameter(), 3e-11);
	}
}
