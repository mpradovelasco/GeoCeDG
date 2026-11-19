/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Post-1.2.0 maintenance review of the two point tools. Point on Object is not covered by
 * Point on native regions: only it places a point in the interior of a polygon or of a
 * filled conic. Both tools therefore stay in the profile-driven toolbar.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class Maintenance120PointToolsDesktopTest {
	@BeforeAll static void initialize() { G9U1SemanticPointInteractionTest.initializeDesktop(); }

	private static List<GeoPoint> points(AppGeoCeDG app) {
		return app.getKernel().getConstruction().getGeoSetConstructionOrder().stream()
				.filter(GeoPoint.class::isInstance).map(GeoPoint.class::cast).toList();
	}

	private static GeoPoint click(AppGeoCeDG app, int mode, double x, double y) {
		List<GeoPoint> before = points(app);
		app.setMode(mode);
		Cu7M08SDesktopEventsTest.pointer(app, x, y);
		List<GeoPoint> created = points(app).stream().filter(point -> !before.contains(point)).toList();
		assertEquals(1, created.size(), "mode " + mode + " at " + x + "," + y);
		return created.get(0);
	}

	@Test void bothPointToolsStayInTheProfileToolbar() {
		List<String> modes = Arrays.asList(GeoCeDGProfile.getToolbarDefinition().split("[^0-9]+"));
		assertTrue(modes.contains(String.valueOf(EuclidianConstants.MODE_POINT)));
		assertTrue(modes.contains(String.valueOf(EuclidianConstants.MODE_POINT_ON_OBJECT)));
	}

	@Test void onlyPointOnObjectPlacesAPointInANativeRegion() throws Exception {
		Cu7M08SDesktopEventsTest.edt(() -> {
			var app = G9U1SemanticPointInteractionTest.app();
			assertNotNull(app.getKernel().getAlgebraProcessor().processAlgebraCommand(
					"square=Polygon((0,0),(2,0),(2,2),(0,2))", false));
			G9U1SemanticPointInteractionTest.eval(app, "disc=Circle((-2,0),1)");
			var disc = app.getKernel().lookupLabel("disc");
			disc.setAlphaValue(0.3); disc.updateRepaint();

			// Point: the interior of a polygon or of a filled conic gives a free point.
			GeoPoint freeInSquare = click(app, EuclidianConstants.MODE_POINT, 1, 1);
			assertTrue(freeInSquare.isIndependent());
			assertFalse(freeInSquare.hasRegion());
			GeoPoint freeInDisc = click(app, EuclidianConstants.MODE_POINT, -2, .2);
			assertTrue(freeInDisc.isIndependent());
			assertFalse(freeInDisc.hasRegion());

			// Point on Object: the same interiors give a point bound to the region.
			GeoPoint inSquare = click(app, EuclidianConstants.MODE_POINT_ON_OBJECT, 1.3, .7);
			assertTrue(inSquare.hasRegion());
			assertSame(app.getKernel().lookupLabel("square"), inSquare.getRegion());
			GeoPoint inDisc = click(app, EuclidianConstants.MODE_POINT_ON_OBJECT, -2.2, -.2);
			assertTrue(inDisc.hasRegion());
			assertSame(disc, inDisc.getRegion());

			// Both tools place a point on an ordinary native path alike.
			var segment = G9U1SemanticPointInteractionTest.eval(app, "edge=Segment((3,-1),(3,1))");
			GeoPoint onSegmentByPoint = click(app, EuclidianConstants.MODE_POINT, 3, .5);
			GeoPoint onSegmentByObject = click(app, EuclidianConstants.MODE_POINT_ON_OBJECT, 3, -.5);
			assertSame(segment, onSegmentByPoint.getPath());
			assertSame(segment, onSegmentByObject.getPath());
		});
	}
}
