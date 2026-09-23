/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResolver2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResult2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** PRE-G9B-R2 intersection metric endpoints across a real native .cedg lifecycle. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9bR2IntersectionEndpointNativeArchiveTest {

	private final IntersectionEndpointProvenanceResolver2D resolver =
			new IntersectionEndpointProvenanceResolver2D();

	@Test
	void nativeArchiveReconstructsAndRevalidatesIntersectionEndpoints(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		build(app);
		GeoLocusV2 spline = (GeoLocusV2) lookup(app, "S");
		String key = key(spline, (GeoPoint) lookup(app, "X"));
		String token = token((GeoPoint) lookup(app, "X"));
		assertEquals(1, finite((GeoLocusMetricResult) lookup(app, "M")), 1E-9);

		Path file = directory.resolve("r2-intersection-endpoints.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(file.toFile(), false));
		GeoLocusV2 restored = (GeoLocusV2) lookup(reopened, "S");
		// The key is reconstructed from the DAG and durable identities, not stored.
		assertEquals(key, key(restored, (GeoPoint) lookup(reopened, "X")));
		assertEquals(token, token((GeoPoint) lookup(reopened, "X")));
		assertEquals(1, finite((GeoLocusMetricResult) lookup(reopened, "M")), 1E-9);
		assertEquals(1, ((GeoNumeric) lookup(reopened, "LenXY")).getDouble(), 1E-9);

		// Currentness is revalidated after reopen: saved coordinates do not keep
		// a dormant endpoint admissible.
		move(reopened, "Ux", 5);
		assertFalse(lookup(reopened, "Y").isDefined());
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				((GeoLocusMetricResult) lookup(reopened, "M")).getMetricResult()
						.getComputationStatus());
		Path dormant = directory.resolve("r2-dormant-endpoint.cedg");
		assertTrue(((GuiManagerGeoCeDG) reopened.getGuiManager())
				.saveAsTo(dormant.toFile()));
		AppGeoCeDG again = G9U1TestApp.create();
		assertTrue(again.loadFile(dormant.toFile(), false));
		assertFalse(lookup(again, "Y").isDefined());
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				((GeoLocusMetricResult) lookup(again, "M")).getMetricResult()
						.getComputationStatus());
		move(again, "Ux", 0.5);
		assertTrue(lookup(again, "Y").isDefined());
		assertEquals(1, finite((GeoLocusMetricResult) lookup(again, "M")), 1E-9);
	}

	private static void build(AppGeoCeDG app) {
		app.getKernel().setContinuous(false);
		for (String command : new String[] {"A=(-1,0)", "B=(0,0)", "C=(1,0)",
				"S=SplineV2({A,B,C},3)"}) {
			eval(app, command);
		}
		vertical(app, "T", "-0.5");
		vertical(app, "U", "0.5");
		materialize(app, "R=Intersect(S,T)", "R", "X");
		materialize(app, "V=Intersect(S,U)", "V", "Y");
		eval(app, "M=LocusLength(S,X,Y)");
		eval(app, "LenXY=Length(S,X,Y)");
	}

	private static void vertical(AppGeoCeDG app, String label, String x) {
		eval(app, label + "x=" + x);
		eval(app, label + "E=(" + label + "x,-2)");
		eval(app, label + "F=(" + label + "x,-2/3)");
		eval(app, label + "G=(" + label + "x,2/3)");
		eval(app, label + "H=(" + label + "x,2)");
		eval(app, label + "=SplineV2({" + label + "E," + label + "F," + label + "G,"
				+ label + "H},3)");
	}

	private static void materialize(AppGeoCeDG app, String command, String rich,
			String point) {
		eval(app, command);
		GeoLocusIntersectionResult result = (GeoLocusIntersectionResult) lookup(app, rich);
		List<String> tokens = result.getIntersectionResult().getFiniteSolutions().stream()
				.map(root -> root.getIdentity().getRootToken())
				.filter(result::isPointAdmissible).toList();
		assertEquals(1, tokens.size(), rich);
		eval(app, point + "=Intersect(" + rich + ",\"" + tokens.get(0) + "\")");
		assertTrue(lookup(app, point).isDefined());
	}

	private String key(GeoLocusV2 source, GeoPoint endpoint) {
		IntersectionEndpointProvenanceResult2D result = resolver.resolve(source, endpoint);
		assertEquals(IntersectionEndpointProvenanceResult2D.Status.UNIQUE,
				result.getStatus(), result.getDiagnostic());
		return result.getUniqueMatch().getOccurrenceKey();
	}

	private static String token(GeoPoint point) {
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
				.getEffectiveRootToken();
	}

	private static void move(AppGeoCeDG app, String label, double value) {
		GeoNumeric numeric = (GeoNumeric) lookup(app, label);
		numeric.setValue(value);
		numeric.updateCascade();
	}

	private static double finite(GeoLocusMetricResult result) {
		return result.getMetricResult().getMetricValue().getFiniteValue().orElseThrow();
	}
}
