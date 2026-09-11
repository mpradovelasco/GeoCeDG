/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricRoute2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricRouteStatus;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/** Focused POST-G9U1-A2 public traversal-policy and lifecycle scenarios. */
class PostG9U1A2ExplicitTraversalPolicyTest
		extends G9U0PublicSurfaceTestBase {

	@Test
	void existingDefaultsAndScalarMagnitudeRemainUnchanged() {
		final GeoLocusV2 locus = createLine();
		add("A=Point(L,\"" + BRANCH + "\",-1)");
		add("B=Point(L,\"" + BRANCH + "\",1)");
		GeoLocusMetricResult legacy = add("R=LocusLength(L,A,B)");
		GeoLocusMetricResult explicit = add("E=LocusLength(L,A,B,"
				+ "\"forward\",\"strict\",\"zero-length\")");
		GeoNumeric scalar = add("d=Length(L,A,B)");
		GeoLocusMetricResult total = add("T=LocusLength(L)");

		assertEquals(2, finite(legacy), 1E-9);
		assertEquals(finite(legacy), finite(explicit), 0);
		assertTrue(scalar.isDefined());
		assertEquals(2, scalar.getDouble(), 1E-9);
		assertTrue(scalar.getDouble() >= 0);
		assertEquals(4, finite(total), 1E-9);
		assertTrue(total.getMetricResult().getRouteEvidence().isEmpty());
		assertEquals(TraversalDirection.FORWARD,
				route(explicit).getDirection());
		assertEquals(OpenBoundaryPolicy.STRICT,
				route(explicit).getBoundaryPolicy());
		assertEquals(locus.getSemanticRevision(),
				route(explicit).getSemanticRevision());
	}

	@Test
	void forwardWrapAndReverseDirectChooseDifferentUnsignedRoutes() {
		createScalarLocus("L", "s", "Q", "(s,0)",
				"{false,{0,10,true,true}}");
		add("A=Point(L,\"" + BRANCH + "\",8)");
		add("B=Point(L,\"" + BRANCH + "\",2)");
		GeoLocusMetricResult forward = add("F=LocusLength(L,A,B,"
				+ "\"forward\",\"wrap-to-start\",\"zero-length\")");
		GeoLocusMetricResult reverse = add("R=LocusLength(L,A,B,"
				+ "\"reverse\",\"strict\",\"zero-length\")");

		assertEquals(4, finite(forward), 1E-9);
		assertEquals(6, finite(reverse), 1E-9);
		assertNotEquals(finite(forward), finite(reverse));
		assertTrue(finite(forward) >= 0);
		assertTrue(finite(reverse) >= 0);
		assertEquals(2, route(forward).getOrderedRouteSegments().size());
		assertTrue(route(forward).isWrapped());
		assertFalse(route(forward).isGeometricallyConnected());
		assertTrue(route(forward).isTargetReached());
		assertEquals(TraversalDirection.REVERSE, route(reverse).getDirection());
	}

	@Test
	void strictStopAndInternalGapRemainExplicit() {
		createScalarLocus("L", "s", "Q", "(s,0)",
				"{false,{0,10,true,true}}");
		add("A=Point(L,\"" + BRANCH + "\",8)");
		add("B=Point(L,\"" + BRANCH + "\",2)");
		GeoLocusMetricResult strict = add("S=LocusLength(L,A,B,"
				+ "\"forward\",\"strict\",\"zero-length\")");
		GeoLocusMetricResult stop = add("T=LocusLength(L,A,B,"
				+ "\"forward\",\"stop-at-end\",\"zero-length\")");
		assertEquals(MetricValueKind.ABSENT,
				strict.getMetricResult().getMetricValue().getKind());
		assertEquals(MetricRouteStatus.TARGET_NOT_REACHABLE,
				route(strict).getRouteStatus());
		assertEquals(2, finite(stop), 1E-9);
		assertEquals(MetricCoverage.INCOMPLETE,
				stop.getMetricResult().getCoverage());
		assertFalse(route(stop).isTargetReached());

		add("u=0");
		add("U=(u,1)");
		add("D2={false,{-2,-1,true,true},{1,2,true,true}}");
		add("G=LocusV2(U,u,D2)");
		add("C=Point(G,\"" + BRANCH + "\",-1.5)");
		add("E=Point(G,\"" + BRANCH + "\",1.5)");
		GeoLocusMetricResult gap = add("X=LocusLength(G,C,E,"
				+ "\"forward\",\"wrap-to-start\",\"zero-length\")");
		assertEquals(MetricValueKind.ABSENT,
				gap.getMetricResult().getMetricValue().getKind());
		assertEquals(MetricRouteStatus.DISCONTINUITY_ENCOUNTERED,
				route(gap).getRouteStatus());
	}

	@Test
	void periodicSeamAndSamePositionPoliciesUseSemanticAddresses() {
		createPeriodicCircle();
		add("A=Point(L,\"" + BRANCH + "\",5.5)");
		add("B=Point(L,\"" + BRANCH + "\",0.5)");
		GeoLocusMetricResult forward = add("F=LocusLength(L,A,B,"
				+ "\"forward\",\"strict\",\"zero-length\")");
		GeoLocusMetricResult reverse = add("R=LocusLength(L,A,B,"
				+ "\"reverse\",\"strict\",\"zero-length\")");
		assertTrue(route(forward).isWrapped());
		assertEquals(TraversalDirection.FORWARD,
				route(forward).getDirection());
		assertEquals(TraversalDirection.REVERSE,
				route(reverse).getDirection());
		assertNotEquals(finite(forward), finite(reverse), 1E-6);

		GeoLocusMetricResult zero = add("Z=LocusLength(L,A,A,"
				+ "\"forward\",\"strict\",\"zero-length\")");
		GeoLocusMetricResult cycle = add("C=LocusLength(L,A,A,"
				+ "\"forward\",\"strict\",\"full-cycle\")");
		assertEquals(0, finite(zero), 0);
		assertTrue(route(zero).getOrderedRouteSegments().isEmpty());
		assertEquals(2 * Math.PI, finite(cycle), 2E-7);
		assertTrue(route(cycle).isWrapped());

		add("u=0");
		add("U=(u,0)");
		add("D2={false,{-2,2,true,true}}");
		add("G=LocusV2(U,u,D2)");
		add("P=Point(G,\"" + BRANCH + "\",0)");
		GeoLocusMetricResult invalid = add("I=LocusLength(G,P,P,"
				+ "\"forward\",\"strict\",\"full-cycle\")");
		assertEquals(MetricValueKind.ABSENT,
				invalid.getMetricResult().getMetricValue().getKind());
	}

	@Test
	void a1AndExplicitEndpointAuthorityAreConsumedWithoutNewInference() {
		final GeoNumeric scale = add("h=1");
		add("A=(-2*h,0)");
		add("B=(0,0)");
		add("C=(2*h,0)");
		final GeoLocusV2 spline = add("S=SplineV2({A,B,C},3)");
		GeoLocusMetricResult constructor = add("R=LocusLength(S,C,A,"
				+ "\"reverse\",\"strict\",\"zero-length\")");
		assertEquals(4, finite(constructor), 1E-9);
		add("P=Point(S,\"spline-v2/main\",0)");
		add("Q=Point(S,\"spline-v2/main\",1)");
		GeoLocusMetricResult addressed = add("E=LocusLength(S,Q,P,"
				+ "\"reverse\",\"strict\",\"zero-length\")");
		assertEquals(finite(constructor), finite(addressed), 0);
		add("O=Point(S,0.75)");
		GeoLocusMetricResult omittedBranch = add("A6=LocusLength(S,P,O,"
				+ "\"forward\",\"strict\",\"zero-length\")");
		assertEquals(3, finite(omittedBranch), 1E-9);

		scale.setValue(0);
		scale.updateCascade();
		assertFalse(spline.isDefined());
		assertEquals(MetricValueKind.ABSENT,
				constructor.getMetricResult().getMetricValue().getKind());
		scale.setValue(1);
		scale.updateCascade();
		assertTrue(spline.isDefined());
		assertEquals(4, finite(constructor), 1E-9);

		add("T=SplineV2({A,B,A,C},3)");
		GeoLocusMetricResult ambiguous = add("X=LocusLength(T,A,C,"
				+ "\"forward\",\"strict\",\"zero-length\")");
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				ambiguous.getMetricResult().getComputationStatus());
	}

	@Test
	void policyInputsRecomputeAndSurviveUndoRedoSaveRenameAndRerun() {
		activateUndo();
		createScalarLocus("L", "s", "Q", "(s,0)",
				"{false,{0,10,true,true}}");
		add("A=Point(L,\"" + BRANCH + "\",8)");
		add("B=Point(L,\"" + BRANCH + "\",2)");
		GeoText direction = add("direction=\"forward\"");
		add("boundary=\"wrap-to-start\"");
		add("same=\"zero-length\"");
		GeoLocusMetricResult metric = add(
				"M=LocusLength(L,A,B,direction,boundary,same)");
		final PersistentGeoId resultId = id(metric);
		assertEquals(4, finite(metric), 1E-9);
		getApp().storeUndoInfo();

		direction.setTextString("reverse");
		direction.updateCascade();
		getApp().storeUndoInfo();
		assertEquals(6, finite(metric), 1E-9);
		getKernel().undo();
		assertEquals(4, finite(assertInstanceOf(GeoLocusMetricResult.class,
				requireLookup("M"))), 1E-9);
		getKernel().redo();
		assertEquals(6, finite(assertInstanceOf(GeoLocusMetricResult.class,
				requireLookup("M"))), 1E-9);

		String xml = getApp().getXML();
		getApp().setXML(xml, true);
		metric = assertInstanceOf(GeoLocusMetricResult.class, requireLookup("M"));
		assertEquals(resultId, id(metric));
		assertEquals(6, finite(metric), 1E-9);
		requireLookup("L").setLabel("RenamedLocus");
		requireLookup("direction").setLabel("renamedDirection");
		metric.update();
		assertEquals(6, finite(metric), 1E-9);
		double beforeRerun = finite(metric);
		metric.update();
		assertEquals(beforeRerun, finite(metric), 0);
	}

	@Test
	void copyRemapsPolicyClosureAndMalformedPolicyFailsClosed() {
		createLine();
		add("A=Point(L,\"" + BRANCH + "\",-1)");
		add("B=Point(L,\"" + BRANCH + "\",1)");
		GeoLocusMetricResult metric = add("M=LocusLength(L,A,B,"
				+ "\"forward\",\"strict\",\"zero-length\")");
		final PersistentGeoId originalId = id(metric);
		final PersistentGeoId originalSourceId = id(requireLookup("L"));
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(metric), text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(Arrays.asList(
						clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));
		GeoIdentityRecord copyRecord = getConstruction()
				.getSpatialIdentityRegistry().getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> originalId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoLocusMetricResult copy = assertInstanceOf(GeoLocusMetricResult.class,
				getConstruction().getSpatialIdentityRegistry()
						.getGeo(copyRecord.getId()));
		assertNotEquals(originalId, id(copy));
		assertEquals(2, finite(copy), 1E-9);
		assertEquals(6, copy.getParentAlgorithm().getInput().length);
		GeoLocusV2 copiedSource = assertInstanceOf(GeoLocusV2.class,
				copy.getParentAlgorithm().getInput(0));
		assertNotEquals(originalSourceId, id(copiedSource));
		assertEquals("forward", assertInstanceOf(GeoText.class,
				copy.getParentAlgorithm().getInput(3)).getTextString());
		assertEquals("strict", assertInstanceOf(GeoText.class,
				copy.getParentAlgorithm().getInput(4)).getTextString());
		assertEquals("zero-length", assertInstanceOf(GeoText.class,
				copy.getParentAlgorithm().getInput(5)).getTextString());

		GeoLocusMetricResult malformed = add("X=LocusLength(L,A,B,"
				+ "\"sideways\",\"strict\",\"zero-length\")");
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				malformed.getMetricResult().getComputationStatus());
		assertTrue(malformed.getMetricResult().getDiagnostics().stream()
				.anyMatch(diagnostic -> diagnostic.getMessage()
						.contains("Unknown traversal direction")));
	}

	private PersistentGeoId id(org.geogebra.common.kernel.geos.GeoElement geo) {
		return getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
	}

	private static double finite(GeoLocusMetricResult result) {
		LocusMetricResult2D metric = result.getMetricResult();
		assertNotNull(metric);
		return metric.getMetricValue().getFiniteValue().orElseThrow();
	}

	private static LocusMetricRoute2D route(GeoLocusMetricResult result) {
		assertNotNull(result.getMetricResult());
		return result.getMetricResult().getRouteEvidence().orElseThrow();
	}
}
