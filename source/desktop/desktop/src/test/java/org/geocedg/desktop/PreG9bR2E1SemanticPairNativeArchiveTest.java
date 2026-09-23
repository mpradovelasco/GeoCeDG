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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.SemanticMetricEndpointResolver2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionEvidence2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairSourceRevisionEvidence2D;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R2-E0/E1 certified semantic-pair roots and the section 24 endpoint
 * families across a real native .cedg lifecycle, on the SeveralDefects witness.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9bR2E1SemanticPairNativeArchiveTest {

	/** Endpoint label to addressed source label, one per admitted family. */
	private static final Map<String, String> ENDPOINTS = Map.of("X0", "a", "X1", "d",
			"W0", "a", "E", "a", "Q", "a", "XR0", "aR");
	private static final List<String> METRICS = List.of("MX", "MD", "MW", "ME", "MQ",
			"MR");

	private final SemanticMetricEndpointResolver2D resolver =
			new SemanticMetricEndpointResolver2D();

	@Test
	void nativeArchiveReconstructsCertifiedPairSlotsAndEndpointFamilies(
			@TempDir Path directory) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		build(app);
		Map<String, String> tokens = tokens(app);
		Map<String, String> keys = keys(app);
		Map<String, Double> values = values(app);
		assertEquals(values.get("MX"), values.get("MQ"), 1E-9);
		assertEquals(values.get("MX"), values.get("MR"), 1E-9);

		Path file = directory.resolve("r2-e1-semantic-pair.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(file.toFile(), false));
		// Slots, keys and values are reconstructed from the DAG, the ledger and
		// durable identities; the certificate and the keys are never stored.
		assertEquals(tokens, tokens(reopened));
		assertEquals(keys, keys(reopened));
		assertValues(values, reopened);

		// Currentness is revalidated after reopen: saved coordinates never keep a
		// dormant slot admissible, and dormancy survives the archive.
		moveTo(reopened, "A", 40.78, 2.24);
		moveTo(reopened, "B", 42.56, 4.78);
		assertDormant(reopened);
		Path dormant = directory.resolve("r2-e1-dormant-pair.cedg");
		assertTrue(((GuiManagerGeoCeDG) reopened.getGuiManager())
				.saveAsTo(dormant.toFile()));
		AppGeoCeDG again = G9U1TestApp.create();
		assertTrue(again.loadFile(dormant.toFile(), false));
		assertDormant(again);
		moveTo(again, "A", 0.78, 2.24);
		moveTo(again, "B", 2.56, 4.78);
		assertEquals(tokens, tokens(again));
		assertEquals(keys, keys(again));
		assertValues(values, again);
	}

	@Test
	void copiedPointDrivenPairKeepsItsRelationAcrossNativeReopen(@TempDir Path directory)
			throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getKernel().setContinuous(false);
		for (String command : new String[] {"A=(0.78,2.24)", "B=(2.56,4.78)",
				"c=Circle(A,B)", "C=Point(c)", "K=(1,1)", "M=Midpoint(C,K)",
				"ring=LocusV2(M,C)", "HA=(-3,1)", "HB=(1,2)", "HC=(5,1)",
				"H=SplineV2({HA,HB,HC},3)"}) {
			eval(app, command);
		}
		GeoLocusV2 spline = (GeoLocusV2) lookup(app, "H");
		materialize(app, "R=Intersect(H,ring)", "R", "X",
				root -> side(root, spline).getSemanticParameter(), 2);
		GeoPoint original = (GeoPoint) lookup(app, "X0");
		final String originalToken = effectiveToken(original);
		String clipboard = InternalClipboard.getTextToSave(app, List.of(original),
				text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(app, new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" "))), clipboard.substring(separator));
		GeoPoint copied = (GeoPoint) copyOf(app, original);
		GeoLocusV2 copiedRing = (GeoLocusV2) copyOf(app, lookup(app, "ring"));
		GeoElement copiedDriver = copyOf(app, lookup(app, "C"));
		String label = copied.getLabelSimple();
		final String copiedToken = effectiveToken(copied);
		assertTrue(copied.isDefined());
		eval(app, "LX=LocusLength(ring,M,X0)");
		eval(app, "LC=LocusLength(" + copiedRing.getLabelSimple() + ","
				+ copyOf(app, lookup(app, "M")).getLabelSimple() + "," + label + ")");
		double length = finite((GeoLocusMetricResult) lookup(app, "LX"));
		assertEquals(length, finite((GeoLocusMetricResult) lookup(app, "LC")), 1E-9);
		final Set<String> copiedGraph = Set.of(id(app, copiedRing), id(app, copiedDriver),
				id(app, copyOf(app, spline)));

		Path file = directory.resolve("r2-e1-copied-pair.cedg");
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(file.toFile(), false));
		GeoPoint restored = (GeoPoint) lookup(reopened, label);
		assertTrue(restored.isDefined());
		assertEquals(copiedToken, effectiveToken(restored));
		assertEquals(originalToken, effectiveToken((GeoPoint) lookup(reopened, "X0")));
		GeoLocusIntersectionResult rich =
				((AlgoLocusIntersectionPointV2) restored.getParentAlgorithm()).getRichInput();
		PairSemanticSlotSelector2D selector = rich.getRetainedPairSelector(copiedToken)
				.orElseThrow();
		// The reopened copied selector names exactly the copied sources and driver.
		Set<String> named = new java.util.TreeSet<>(selector.getDeclaredParticipants());
		named.add(selector.getFirst().getSourceId());
		named.add(selector.getSecond().getSourceId());
		assertEquals(copiedGraph, named);
		assertEquals(length, finite((GeoLocusMetricResult) lookup(reopened, "LC")), 1E-9);
		// Deleting the original graph leaves the reopened copy current.
		lookup(reopened, "A").remove();
		assertTrue(restored.isDefined());
		assertEquals(length, finite((GeoLocusMetricResult) lookup(reopened, "LC")), 1E-9);
	}

	private static String effectiveToken(GeoPoint point) {
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
				.getEffectiveRootToken();
	}

	private static GeoElement copyOf(AppGeoCeDG app, GeoElement original) {
		var registry = app.getKernel().getConstruction().getSpatialIdentityRegistry();
		var source = registry.getPersistentGeoId(original);
		return registry.getGeo(registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(record -> source.equals(record.getCopySourceId()))
				.findFirst().orElseThrow().getId());
	}

	private static String id(AppGeoCeDG app, GeoElement geo) {
		return app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo).toExternalForm();
	}

	private static double finite(GeoLocusMetricResult result) {
		return result.getMetricResult().getMetricValue().getFiniteValue().orElseThrow();
	}

	private static void build(AppGeoCeDG app) {
		app.getKernel().setContinuous(false);
		for (String command : new String[] {"A=(0.78,2.24)", "B=(2.56,4.78)",
				"c=Circle(A,B)", "C=Point(c)", "f=Line(C,xAxis)", "D=Intersect(f,yAxis)",
				"E=Midpoint(D,C)", "a=LocusV2(E,C)", "F=(-3,1)", "G=(0.56,3.94)",
				"H=(6.28,-1.36)", "l1={F,G,H}", "b=3", "d=SplineV2(l1,b)"}) {
			eval(app, command);
		}
		GeoLocusV2 spline = (GeoLocusV2) lookup(app, "d");
		materialize(app, "e=Intersect(d,a)", "e", "X",
				root -> side(root, spline).getSemanticParameter(), 2);
		materialize(app, "g=Intersect(a,c)", "g", "W",
				root -> root.getRevisionEvidence().getSemanticParameter(), 4);
		for (String command : new String[] {"MX=LocusLength(a,X0,X1)",
				"MD=LocusLength(d,F,X1)", "MW=LocusLength(a,W0,W1)", "ME=LocusLength(a,E,W1)",
				"Q=X0", "MQ=LocusLength(a,Q,X1)", "alpha=30deg", "aR=Rotate(a,alpha,A)",
				"XR0=Rotate(X0,alpha,A)", "XR1=Rotate(X1,alpha,A)",
				"MR=LocusLength(aR,XR0,XR1)"}) {
			eval(app, command);
		}
	}

	private static void materialize(AppGeoCeDG app, String command, String rich,
			String prefix, ToDoubleFunction<LocusIntersectionSolution2D> order,
			int expected) {
		eval(app, command);
		GeoLocusIntersectionResult result = (GeoLocusIntersectionResult) lookup(app, rich);
		List<LocusIntersectionSolution2D> roots = new ArrayList<>(result
				.getIntersectionResult().getFiniteSolutions().stream()
				.filter(root -> result.isPointAdmissible(root.getIdentity().getRootToken()))
				.toList());
		assertEquals(expected, roots.size(), rich);
		roots.sort(Comparator.comparingDouble(order));
		for (int index = 0; index < roots.size(); index++) {
			String label = prefix + index;
			eval(app, label + "=Intersect(" + rich + ",\""
					+ roots.get(index).getIdentity().getRootToken() + "\")");
			assertTrue(lookup(app, label).isDefined(), label);
		}
	}

	private static LocusPairSourceRevisionEvidence2D side(
			LocusIntersectionSolution2D root, GeoLocusV2 source) {
		LocusPairIntersectionEvidence2D pair = root.getPairEvidence().orElseThrow();
		return pair.getFirst().getLocusIdentity().equals(source.getLocusIdentity())
				? pair.getFirst() : pair.getSecond();
	}

	private static Map<String, String> tokens(AppGeoCeDG app) {
		Map<String, String> tokens = new TreeMap<>();
		for (String label : List.of("X0", "X1", "W0", "W1")) {
			tokens.put(label, ((AlgoLocusIntersectionPointV2) lookup(app, label)
					.getParentAlgorithm()).getEffectiveRootToken());
		}
		return tokens;
	}

	private Map<String, String> keys(AppGeoCeDG app) {
		Map<String, String> keys = new TreeMap<>();
		ENDPOINTS.forEach((endpoint, source) -> {
			SemanticMetricEndpointResolver2D.Resolution resolution = resolver.resolve(
					(GeoLocusV2) lookup(app, source), (GeoPoint) lookup(app, endpoint));
			assertEquals(SemanticMetricEndpointResolver2D.Status.UNIQUE,
					resolution.getStatus(), endpoint + ": " + resolution.getDiagnostic());
			keys.put(endpoint, resolution.getFamily() + " " + resolution.getOccurrenceKey());
		});
		return keys;
	}

	private static Map<String, Double> values(AppGeoCeDG app) {
		Map<String, Double> values = new TreeMap<>();
		for (String label : METRICS) {
			values.put(label, ((GeoLocusMetricResult) lookup(app, label)).getMetricResult()
					.getMetricValue().getFiniteValue().orElseThrow());
		}
		return values;
	}

	private static void assertValues(Map<String, Double> expected, AppGeoCeDG app) {
		Map<String, Double> actual = values(app);
		for (String label : METRICS) {
			assertEquals(expected.get(label), actual.get(label), 1E-9, label);
		}
	}

	private void assertDormant(AppGeoCeDG app) {
		// The ellipse moves half as far as the circle, so both the pair and the
		// single-source roots are absent; only the generator stays addressable.
		for (String label : List.of("X0", "X1", "W0", "W1", "Q", "XR0")) {
			assertFalse(lookup(app, label).isDefined(), label);
		}
		for (String label : METRICS) {
			assertEquals(MetricComputationStatus.INVALID_QUERY,
					((GeoLocusMetricResult) lookup(app, label)).getMetricResult()
							.getComputationStatus(), label);
		}
		assertEquals(SemanticMetricEndpointResolver2D.Family.GENERATOR_OCCURRENCE,
				resolver.resolve((GeoLocusV2) lookup(app, "a"), (GeoPoint) lookup(app, "E"))
						.getFamily());
	}

	private static void moveTo(AppGeoCeDG app, String label, double x, double y) {
		GeoPoint point = (GeoPoint) lookup(app, label);
		point.setCoords(x, y, 1);
		point.updateCascade();
	}
}
