/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geogebra.common.kernel.algos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.PathMover;
import org.geogebra.common.kernel.geos.GeoLocus;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Legacy view-cache refresh must visit later views even after an earlier change. */
class AlgoLocusViewBoundsRegressionTest extends BaseUnitTest {
	@ParameterizedTest @ValueSource(ints = {3, 5, 7})
	void everyVisibleViewRefreshesItsBoundsAndConnectionThresholds(int visibleMask) {
		GeoPoint p = add("P=Point(xAxis)");
		GeoPoint q = add("Q=P+(1,0)");
		AlgoLocus algo = new AlgoLocus(getConstruction(), q, p, PathMover.MIN_STEPS, false);
		GeoLocus visibility = mock(GeoLocus.class);
		for (int i = 0; i < 3; i++) {
			when(visibility.isVisibleInEV(i + 1)).thenReturn((visibleMask & (1 << i)) != 0);
		}
		// Only visibility is controlled; the real algorithm, cache and Kernel are used.
		algo.locus = visibility;
		double[] hiddenBefore = algo.xmax.clone();
		for (int revision = 1; revision <= 2; revision++) {
			for (int i = 0; i < 3; i++) {
				double k = revision * 10 + i;
				getKernel().setEuclidianViewBounds(i + 1, -k, k + 2, -k - 1, k + 3,
						50 + i, 70 + i);
			}
			assertTrue(algo.updateScreenBorders());
			for (int i = 0; i < 3; i++) {
				if ((visibleMask & (1 << i)) == 0) {
					assertEquals(hiddenBefore[i], algo.xmax[i], "Hidden view cache is not refreshed");
					continue;
				}
				assertEquals(getKernel().getXmin(i), algo.xmin[i], "xmin view " + i);
				assertEquals(getKernel().getXmax(i), algo.xmax[i], "xmax view " + i);
				assertEquals(getKernel().getYmin(i), algo.ymin[i], "ymin view " + i);
				assertEquals(getKernel().getYmax(i), algo.ymax[i], "ymax view " + i);
				assertEquals(5.0 / getKernel().getXscale(i), algo.maxXdist[i]);
				assertEquals(5.0 / getKernel().getYscale(i), algo.maxYdist[i]);
			}
			assertFalse(algo.updateScreenBorders(), "No bounds changed after the complete refresh");
		}
	}
}
