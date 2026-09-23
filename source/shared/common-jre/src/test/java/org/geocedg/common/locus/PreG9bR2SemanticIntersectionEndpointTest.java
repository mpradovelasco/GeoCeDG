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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResolver2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResult2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResult2D.Status;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResolver2D;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResult2D;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/**
 * Focused PRE-G9B-R2 scenarios: a point materialized from a Locus V2 pair root is
 * a semantic metric endpoint on either intersected source (metrics section 23).
 */
class PreG9bR2SemanticIntersectionEndpointTest extends G9U0PublicSurfaceTestBase {

	private final IntersectionEndpointProvenanceResolver2D resolver =
			new IntersectionEndpointProvenanceResolver2D();

	@Test
	void pairIntersectionPointsAreEndpointsForRichAndScalarMetrics() {
		GeoLocusV2 spline = straight();
		vertical("T", "-0.5");
		vertical("U", "0.5");
		GeoPoint x = only(add("R=Intersect(S,T)"), "X");
		GeoPoint y = only(add("V=Intersect(S,U)"), "Y");
		GeoLocusMetricResult rich = add("M=LocusLength(S,X,Y)");
		GeoNumeric scalar = add("d=Length(S,X,Y)");
		GeoLocusMetricResult mixed = add("N=LocusLength(S,A,X)");

		assertEquals(0.25, unique(spline, x).getUniqueMatch().getAddress()
				.getCanonicalParameter(), 1E-9);
		assertEquals(0.75, unique(spline, y).getUniqueMatch().getAddress()
				.getCanonicalParameter(), 1E-9);
		assertEquals(1, finite(rich), 1E-9);
		assertTrue(scalar.isDefined());
		assertEquals(1, scalar.getDouble(), 1E-9);
		// A1 constructor occurrence and the new family combine in one query.
		assertEquals(0.5, finite(mixed), 1E-9);
	}

	@Test
	void sideSelectionFollowsIdentityForEitherSourceAndEitherCallerOrder() {
		GeoLocusV2 spline = straight();
		GeoLocusV2 vertical = vertical("T", "-0.5");
		vertical("U", "0.5");
		GeoPoint forward = only(add("R=Intersect(S,T)"), "X");
		GeoPoint reversed = only(add("W=Intersect(T,S)"), "Z");
		GeoPoint y = only(add("V=Intersect(S,U)"), "Y");

		IntersectionEndpointProvenanceResult2D.Match onSpline = unique(spline, forward)
				.getUniqueMatch();
		IntersectionEndpointProvenanceResult2D.Match reversedOnSpline =
				unique(spline, reversed).getUniqueMatch();
		assertEquals(onSpline.getAddress().getCanonicalParameter(),
				reversedOnSpline.getAddress().getCanonicalParameter(), 1E-12);
		assertNotEquals(onSpline.getOccurrenceKey(), reversedOnSpline.getOccurrenceKey());
		assertEquals(1, finite(add("M=LocusLength(S,X,Y)")), 1E-9);
		assertEquals(1, finite(add("MR=LocusLength(S,Z,Y)")), 1E-9);

		// The same point also addresses the other intersected source.
		assertEquals(vertical.getPersistentLocusId(),
				unique(vertical, forward).getUniqueMatch().getAddress().getSourceLocusId());
		assertEquals(2, finite(add("MT=LocusLength(T,TE,X)")), 1E-9);
		assertEquals(2, finite(add("MTR=LocusLength(T,TE,Z)")), 1E-9);
	}

	@Test
	void dormantEndpointFailsClosedAndTheSameTokenRecovers() {
		GeoLocusV2 spline = straight();
		vertical("T", "-0.5");
		vertical("U", "0.5");
		only(add("R=Intersect(S,T)"), "X");
		GeoPoint y = only(add("V=Intersect(S,U)"), "Y");
		GeoLocusMetricResult rich = add("M=LocusLength(S,X,Y)");
		GeoNumeric scalar = add("d=Length(S,X,Y)");
		final String token = token(y);
		final String key = unique(spline, y).getUniqueMatch().getOccurrenceKey();

		move("Ux", 5);
		assertFalse(y.isDefined());
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				rich.getMetricResult().getComputationStatus());
		assertFalse(scalar.isDefined());

		move("Ux", 0.5);
		assertTrue(y.isDefined());
		assertEquals(token, token(y));
		assertEquals(key, unique(spline, y).getUniqueMatch().getOccurrenceKey());
		assertEquals(1, finite(rich), 1E-9);
		assertEquals(1, scalar.getDouble(), 1E-9);
	}

	@Test
	void changingTheSelectedTokenIsRetargetingAndFailsClosed() {
		GeoLocusV2 spline = straight();
		add("WA=(-0.5,-1)");
		add("WB=(0,1)");
		add("WC=(0.5,-1)");
		add("W=SplineV2({WA,WB,WC},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(S,W)");
		List<LocusIntersectionSolution2D> roots = eligible(rich);
		assertEquals(2, roots.size(), "the arch must cross S twice");
		GeoText selected = new GeoText(getConstruction(),
				roots.get(0).getIdentity().getRootToken());
		GeoPoint moving = LocusV2PublicOperations.selectIntersectionPoint(
				getConstruction(), "X0", rich, selected);
		GeoPoint other = materialize(rich, "X1", roots.get(1));
		GeoLocusMetricResult metric = add("M=LocusLength(S,X0,C)");
		assertTrue(metric.getMetricResult().getMetricValue().getFiniteValue().isPresent());
		String retainedKey = unique(spline, moving).getUniqueMatch().getOccurrenceKey();

		other.remove();
		selected.setTextString(roots.get(1).getIdentity().getRootToken());
		selected.updateCascade();
		assertTrue(moving.isDefined());
		assertNotEquals(retainedKey, unique(spline, moving).getUniqueMatch()
				.getOccurrenceKey());
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				metric.getMetricResult().getComputationStatus());
		assertTrue(metric.getMetricResult().getDiagnostics().get(0).getMessage()
				.contains("retargeting is forbidden"));
	}

	@Test
	void xmlReopenAndUndoRedoRevalidateWithTheSameKeys() {
		activateUndo();
		GeoLocusV2 spline = straight();
		vertical("T", "-0.5");
		vertical("U", "0.5");
		GeoPoint x = only(add("R=Intersect(S,T)"), "X");
		only(add("V=Intersect(S,U)"), "Y");
		GeoLocusMetricResult metric = add("M=LocusLength(S,X,Y)");
		final PersistentGeoId metricId = id(metric);
		final String key = unique(spline, x).getUniqueMatch().getOccurrenceKey();
		getApp().storeUndoInfo();
		add("z=1");
		getApp().storeUndoInfo();
		getKernel().undo();
		getKernel().redo();
		assertEquals(1, finite(assertInstanceOf(GeoLocusMetricResult.class,
				requireLookup("M"))), 1E-9);

		String xml = getApp().getXML();
		assertFalse(xml.contains("metric-endpoint/intersection-occurrence/v1"));
		getApp().setXML(xml, true);
		spline = assertInstanceOf(GeoLocusV2.class, requireLookup("S"));
		metric = assertInstanceOf(GeoLocusMetricResult.class, requireLookup("M"));
		assertEquals(metricId, id(metric));
		assertEquals(key, unique(spline, point("X")).getUniqueMatch().getOccurrenceKey());
		assertEquals(1, finite(metric), 1E-9);
	}

	@Test
	void closureCopyRemapsIdentitiesIntoNewConsistentKeys() {
		GeoLocusV2 spline = straight();
		vertical("T", "-0.5");
		vertical("U", "0.5");
		GeoPoint x = only(add("R=Intersect(S,T)"), "X");
		only(add("V=Intersect(S,U)"), "Y");
		GeoNumeric scalar = add("d=Length(S,X,Y)");
		PersistentGeoId scalarId = id(scalar);
		String originalKey = unique(spline, x).getUniqueMatch().getOccurrenceKey();

		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(scalar), text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(Arrays.asList(clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));

		GeoIdentityRecord copiedRecord = getConstruction().getSpatialIdentityRegistry()
				.getRecords().stream().filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> scalarId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoNumeric copied = assertInstanceOf(GeoNumeric.class, getConstruction()
				.getSpatialIdentityRegistry().getGeo(copiedRecord.getId()));
		GeoLocusMetricResult copiedRich = assertInstanceOf(AlgoLocusMetricScalarAdapter.class,
				copied.getParentAlgorithm()).getRichInput();
		GeoLocusV2 copiedSource = assertInstanceOf(GeoLocusV2.class,
				copiedRich.getParentAlgorithm().getInput(0));
		GeoPoint copiedStart = assertInstanceOf(GeoPoint.class,
				copiedRich.getParentAlgorithm().getInput(1));
		assertTrue(copied.isDefined());
		assertEquals(1, copied.getDouble(), 1E-9);
		assertNotEquals(spline.getPersistentLocusId(), copiedSource.getPersistentLocusId());
		assertNotEquals(id(x), id(copiedStart));
		assertNotEquals(originalKey, unique(copiedSource, copiedStart).getUniqueMatch()
				.getOccurrenceKey());
	}

	@Test
	void compatibleNumericRedefineKeepsTheEndpointAndFollowsTheGeometry() {
		final GeoLocusV2 spline = straight();
		vertical("T", "-0.5");
		vertical("U", "0.5");
		only(add("R=Intersect(S,T)"), "X");
		GeoPoint y = only(add("V=Intersect(S,U)"), "Y");
		// An ordinary scalar-locus control makes Ux an explicit registered
		// dependency, so the redefine exercises the real G9A lifecycle.
		add("Qu=(Ux,0)");
		add("Du={false,{-2,2,true,true}}");
		add("Lu=LocusV2(Qu,Ux,Du)");
		final GeoLocusMetricResult metric = add("M=LocusLength(S,X,Y)");
		final PersistentGeoId pointId = id(y);
		final String token = token(y);
		GeoNumeric control = assertInstanceOf(GeoNumeric.class, requireLookup("Ux"));

		ErrorAccumulator errors = new ErrorAccumulator();
		AtomicReference<GeoElementND> callback = new AtomicReference<>();
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(control,
				"0.25", redefineInfo(control), false, callback::set, errors);
		assertTrue(errors.getErrors().isBlank(), errors.getErrors());
		assertNotNull(callback.get());
		GeoPoint redefined = point("Y");
		assertEquals(pointId, id(redefined));
		assertEquals(token, token(redefined));
		assertEquals(0.625, unique(spline, redefined).getUniqueMatch().getAddress()
				.getCanonicalParameter(), 1E-9);
		assertEquals(0.75, finite(assertInstanceOf(GeoLocusMetricResult.class,
				requireLookup("M"))), 1E-9);
		assertNotNull(metric);
	}

	@Test
	void unadmittedShapesStayInvalidAndA1FallThroughIsPreserved() {
		final GeoLocusV2 spline = straight();
		vertical("T", "-0.5");
		vertical("U", "0.5");
		GeoPoint x = only(add("R=Intersect(S,T)"), "X");
		only(add("V=Intersect(S,U)"), "Y");

		// A free point at the same coordinates carries no provenance.
		GeoPoint coincident = add("K=(-0.5,0)");
		assertEquals(Status.NO_ADDRESS, resolver.resolve(spline, coincident).getStatus());
		assertInvalid(add("MK=LocusLength(S,K,Y)"));

		// A coincident similarity image is a different source: no lineage is followed.
		GeoLocusV2 image = add("SI=Translate(S,(0,0))");
		assertEquals(Status.NO_ADDRESS, resolver.resolve(image, x).getStatus());
		assertInvalid(add("MI=LocusLength(SI,X,Y)"));

		// A single-source intersection has no per-side pair evidence, so the
		// section 23 pair walk stays NO_ADDRESS. Section 24.4 (PRE-G9B-R2-E0/E1)
		// supersedes the former exclusion: the single-source family admits it.
		add("vl:x=0.2");
		GeoLocusIntersectionResult single = add("RS=Intersect(S,vl)");
		List<LocusIntersectionSolution2D> singleRoots = eligible(single);
		assertEquals(1, singleRoots.size(), "S x line must materialize one root");
		GeoPoint z = materialize(single, "Q", singleRoots.get(0));
		assertEquals(Status.NO_ADDRESS, resolver.resolve(spline, z).getStatus());
		assertEquals(0.3, finite(add("MS=LocusLength(S,Q,Y)")), 1E-9);

		// An intersection point used as a constructor of another spline still
		// resolves there through A1: NO_ADDRESS falls through unchanged.
		add("OA=(-0.5,1)");
		add("OC=(-0.5,-1)");
		GeoLocusV2 through = add("O=SplineV2({OA,X,OC},3)");
		assertEquals(Status.NO_ADDRESS, resolver.resolve(through, x).getStatus());
		assertEquals(SplineConstructorOccurrenceResult2D.Status.UNIQUE,
				new SplineConstructorOccurrenceResolver2D().resolve(through, x).getStatus());
		assertEquals(1, finite(add("MO=LocusLength(O,OA,X)")), 1E-9);
	}

	private GeoLocusV2 straight() {
		getKernel().setContinuous(false);
		add("A=(-1,0)");
		add("B=(0,0)");
		add("C=(1,0)");
		return add("S=SplineV2({A,B,C},3)");
	}

	private GeoLocusV2 vertical(String label, String x) {
		add(label + "x=" + x);
		add(label + "E=(" + label + "x,-2)");
		add(label + "F=(" + label + "x,-2/3)");
		add(label + "G=(" + label + "x,2/3)");
		add(label + "H=(" + label + "x,2)");
		return add(label + "=SplineV2({" + label + "E," + label + "F," + label + "G,"
				+ label + "H},3)");
	}

	private GeoPoint only(GeoLocusIntersectionResult rich, String label) {
		List<LocusIntersectionSolution2D> roots = eligible(rich);
		assertEquals(1, roots.size(), "expected exactly one admissible pair root");
		return materialize(rich, label, roots.get(0));
	}

	private GeoPoint materialize(GeoLocusIntersectionResult rich, String label,
			LocusIntersectionSolution2D root) {
		return tokenPointAs(rich, label, root.getIdentity().getRootToken());
	}

	private GeoPoint tokenPointAs(GeoLocusIntersectionResult rich, String label,
			String token) {
		GeoPoint point = add(label + "=Intersect(" + rich.getLabelSimple() + ",\""
				+ token + "\")");
		assertNotNull(point);
		assertTrue(point.isDefined());
		return point;
	}

	private IntersectionEndpointProvenanceResult2D unique(GeoLocusV2 source,
			GeoPoint endpoint) {
		IntersectionEndpointProvenanceResult2D result = resolver.resolve(source,
				endpoint);
		assertEquals(Status.UNIQUE, result.getStatus(), result.getDiagnostic());
		return result;
	}

	private void move(String label, double value) {
		GeoNumeric numeric = assertInstanceOf(GeoNumeric.class, requireLookup(label));
		numeric.setValue(value);
		numeric.updateCascade();
	}

	private EvalInfo redefineInfo(GeoElement target) {
		return new EvalInfo(true, true).withSymbolicMode(
				AlgebraProcessor.getRedefinitionMode(target, getKernel()))
				.withLabelRedefinitionAllowedFor(target.getLabelSimple())
				.withSymbolic(true).withSliders(true);
	}

	private GeoPoint point(String label) {
		return assertInstanceOf(GeoPoint.class, requireLookup(label));
	}

	private PersistentGeoId id(GeoElement geo) {
		return getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(geo);
	}

	private static String token(GeoPoint point) {
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
				.getEffectiveRootToken();
	}

	private static List<LocusIntersectionSolution2D> eligible(
			GeoLocusIntersectionResult rich) {
		assertNotNull(rich.getIntersectionResult());
		return rich.getIntersectionResult().getFiniteSolutions().stream()
				.filter(root -> rich.isPointAdmissible(root.getIdentity().getRootToken()))
				.toList();
	}

	private static void assertInvalid(GeoLocusMetricResult result) {
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				result.getMetricResult().getComputationStatus());
	}

	private static double finite(GeoLocusMetricResult result) {
		assertNotNull(result.getMetricResult());
		return result.getMetricResult().getMetricValue().getFiniteValue().orElseThrow();
	}
}
