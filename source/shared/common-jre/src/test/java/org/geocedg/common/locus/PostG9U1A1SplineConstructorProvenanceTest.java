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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D.SeamSide;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResolver2D;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResult2D;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResult2D.Status;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/** Focused POST-G9U1-A1 constructor-occurrence and metric scenarios. */
class PostG9U1A1SplineConstructorProvenanceTest
		extends G9U0PublicSurfaceTestBase {

	private final SplineConstructorOccurrenceResolver2D resolver =
			new SplineConstructorOccurrenceResolver2D();

	@Test
	void uniqueOccurrencesFeedRichAndScalarMetricsAndAgreeWithAddresses() {
		GeoLocusV2 spline = lineSpline();
		GeoPoint a = point("A");
		GeoPoint c = point("C");
		GeoLocusMetricResult occurrence = add("R=LocusLength(S,A,C)");
		GeoNumeric scalar = add("d=Length(S,A,C)");
		add("P=Point(S,\"spline-v2/main\",0)");
		add("Q=Point(S,\"spline-v2/main\",1)");
		GeoLocusMetricResult addressed = add("E=LocusLength(S,P,Q)");

		assertEquals(Status.UNIQUE, resolver.resolve(spline, a).getStatus());
		assertEquals(Status.UNIQUE, resolver.resolve(spline, c).getStatus());
		assertEquals(4, finite(occurrence), 1E-9);
		assertEquals(finite(addressed), finite(occurrence), 0);
		assertTrue(scalar.isDefined());
		assertEquals(4, scalar.getDouble(), 1E-9);
	}

	@Test
	void repeatedSamePointIsMultipleButCoincidentDistinctPointsStayDistinct() {
		add("A=(0,0)");
		add("B=(1,1)");
		add("C=(2,0)");
		GeoLocusV2 repeated = add("S=SplineV2({A,B,A,C},3)");
		GeoLocusMetricResult ambiguous = add("R=LocusLength(S,A,C)");
		SplineConstructorOccurrenceResult2D repeatedResult =
				resolver.resolve(repeated, point("A"));
		assertEquals(Status.MULTIPLE, repeatedResult.getStatus());
		assertEquals(2, repeatedResult.getMatches().size());
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				ambiguous.getMetricResult().getComputationStatus());
		assertTrue(ambiguous.getMetricResult().getDiagnostics().get(0)
				.getMessage().contains("MULTIPLE"));

		add("E=(0,0)");
		GeoLocusV2 coincident = add("T=SplineV2({A,B,E,C},3)");
		add("Q=LocusLength(T,A,E)");
		SplineConstructorOccurrenceResult2D first =
				resolver.resolve(coincident, point("A"));
		SplineConstructorOccurrenceResult2D second =
				resolver.resolve(coincident, point("E"));
		assertEquals(Status.UNIQUE, first.getStatus());
		assertEquals(Status.UNIQUE, second.getStatus());
		assertNotEquals(first.getUniqueMatch().getOccurrenceKey(),
				second.getUniqueMatch().getOccurrenceKey());
		assertNotEquals(Double.doubleToLongBits(first.getUniqueMatch().getAddress()
				.getCanonicalParameter()), Double.doubleToLongBits(second
						.getUniqueMatch().getAddress().getCanonicalParameter()));
	}

	@Test
	void coincidenceAndSelfIntersectionNeverCreateExtraProvenance() {
		add("A=(-1,0)");
		add("B=(0,1)");
		add("C=(1,0)");
		add("D=(0,1)");
		add("E=(-1,0)");
		GeoLocusV2 spline = add("S=SplineV2({A,B,C,D,E},3)");
		add("R=LocusLength(S,B,C)");
		add("RD=LocusLength(S,D,C)");
		assertEquals(Status.UNIQUE, resolver.resolve(spline, point("B"))
				.getStatus());
		assertEquals(Status.UNIQUE, resolver.resolve(spline, point("D"))
				.getStatus());

		GeoPoint arbitrary = add("X=(0,1)");
		GeoLocusMetricResult absent = add("Q=LocusLength(S,X,C)");
		assertEquals(Status.NO_ADDRESS, resolver.resolve(spline, arbitrary)
				.getStatus());
		assertEquals(MetricValueKind.ABSENT,
				absent.getMetricResult().getMetricValue().getKind());
	}

	@Test
	void knotAndPeriodicSeamHaveCanonicalSingleOccurrenceAddresses() {
		GeoLocusV2 line = lineSpline();
		add("R=LocusLength(S,B,C)");
		SplineConstructorOccurrenceResult2D knot =
				resolver.resolve(line, point("B"));
		assertEquals(Status.UNIQUE, knot.getStatus());
		assertEquals(1, knot.getUniqueMatch().getSlot());
		assertEquals(0.5, knot.getUniqueMatch().getAddress()
				.getCanonicalParameter(), 0);

		add("P0=(1,0)");
		add("P1=(0,1)");
		add("P2=(-1,0)");
		add("P3=(0,-1)");
		GeoLocusV2 closed = add("T=SplineV2({P0,P1,P2,P3,P0},3)");
		add("Q=LocusLength(T,P0,P1)");
		SplineConstructorOccurrenceResult2D seam =
				resolver.resolve(closed, point("P0"));
		assertEquals(Status.MULTIPLE, seam.getStatus());
		assertEquals(0, seam.getMatches().get(0).getAddress()
				.getCanonicalParameter(), 0);
		assertEquals(0, seam.getMatches().get(1).getAddress()
				.getCanonicalParameter(), 0);
		assertEquals(SeamSide.LOWER_APPROACH,
				seam.getMatches().get(0).getAddress().getSeamSide());
		assertEquals(SeamSide.UPPER_APPROACH,
				seam.getMatches().get(1).getAddress().getSeamSide());
		assertEquals(0, seam.getMatches().get(0).getAddress().getPeriodicLift());
		assertEquals(1, seam.getMatches().get(1).getAddress().getPeriodicLift());
	}

	@Test
	void invalidityFailsClosedAndSameOccurrenceRecoversAfterRecompute() {
		add("h=1");
		add("A=(-2*h,0)");
		add("B=(0,0)");
		add("C=(2*h,0)");
		GeoLocusV2 spline = add("S=SplineV2({A,B,C},3)");
		GeoLocusMetricResult metric = add("R=LocusLength(S,A,C)");
		final String occurrenceKey = resolver.resolve(spline, point("A"))
				.getUniqueMatch().getOccurrenceKey();
		assertEquals(4, finite(metric), 1E-9);

		GeoNumeric h = assertInstanceOf(GeoNumeric.class, requireLookup("h"));
		h.setValue(0);
		h.updateCascade();
		assertFalse(spline.isDefined());
		assertEquals(MetricValueKind.ABSENT,
				metric.getMetricResult().getMetricValue().getKind());
		assertEquals(Status.UNRESOLVED_INVALID,
				resolver.resolve(spline, point("A")).getStatus());

		h.setValue(1);
		h.updateCascade();
		assertTrue(spline.isDefined());
		assertEquals(4, finite(metric), 1E-9);
		assertEquals(occurrenceKey, resolver.resolve(spline, point("A"))
				.getUniqueMatch().getOccurrenceKey());
	}

	@Test
	void saveReopenUndoRedoRenameAndDeterministicRerunReconstructProvenance() {
		activateUndo();
		GeoLocusV2 spline = lineSpline();
		GeoLocusMetricResult metric = add("R=LocusLength(S,A,C)");
		final PersistentGeoId sourceId = spline.getPersistentLocusId();
		final PersistentGeoId resultId = id(metric);
		final String key = resolver.resolve(spline, point("A"))
				.getUniqueMatch().getOccurrenceKey();
		getApp().storeUndoInfo();
		add("z=1");
		getApp().storeUndoInfo();
		getKernel().undo();
		getKernel().redo();
		assertEquals(4, finite(assertInstanceOf(GeoLocusMetricResult.class,
				requireLookup("R"))), 1E-9);

		String xml = getApp().getXML();
		assertFalse(xml.contains("spline-v2/input-occurrence/v1"));
		getApp().setXML(xml, true);
		spline = assertInstanceOf(GeoLocusV2.class, requireLookup("S"));
		metric = assertInstanceOf(GeoLocusMetricResult.class, requireLookup("R"));
		assertEquals(sourceId, spline.getPersistentLocusId());
		assertEquals(resultId, id(metric));
		assertEquals(key, resolver.resolve(spline, point("A"))
				.getUniqueMatch().getOccurrenceKey());
		assertEquals(4, finite(metric), 1E-9);
		spline.setLabel("RenamedSpline");
		point("A").setLabel("RenamedA");
		metric.update();
		assertEquals(4, finite(metric), 1E-9);
	}

	@Test
	void copiedMetricClosureRemapsSourceInputsAndOccurrenceKeys() {
		GeoLocusV2 spline = lineSpline();
		GeoNumeric scalar = add("d=Length(S,A,C)");
		AlgoLocusMetricScalarAdapter scalarParent = assertInstanceOf(
				AlgoLocusMetricScalarAdapter.class, scalar.getParentAlgorithm());
		GeoLocusMetricResult rich = scalarParent.getRichInput();
		PersistentGeoId scalarId = id(scalar);
		PersistentGeoId sourceId = spline.getPersistentLocusId();
		PersistentGeoId startId = id(point("A"));
		String originalKey = resolver.resolve(spline, point("A"))
				.getUniqueMatch().getOccurrenceKey();

		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(scalar), text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(Arrays.asList(
						clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));

		GeoIdentityRecord copiedScalarRecord = getConstruction()
				.getSpatialIdentityRegistry().getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> scalarId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoNumeric copiedScalar = assertInstanceOf(GeoNumeric.class,
				getConstruction().getSpatialIdentityRegistry().getGeo(
						copiedScalarRecord.getId()));
		GeoLocusMetricResult copiedRich = assertInstanceOf(
				GeoLocusMetricResult.class, assertInstanceOf(
						AlgoLocusMetricScalarAdapter.class,
						copiedScalar.getParentAlgorithm()).getRichInput());
		GeoLocusV2 copiedSource = assertInstanceOf(GeoLocusV2.class,
				copiedRich.getParentAlgorithm().getInput(0));
		GeoPoint copiedStart = assertInstanceOf(GeoPoint.class,
				copiedRich.getParentAlgorithm().getInput(1));
		assertTrue(copiedScalar.isDefined());
		assertEquals(4, copiedScalar.getDouble(), 1E-9);
		assertNotEquals(sourceId, copiedSource.getPersistentLocusId());
		assertNotEquals(startId, id(copiedStart));
		String copiedKey = resolver.resolve(copiedSource, copiedStart)
				.getUniqueMatch().getOccurrenceKey();
		assertNotEquals(originalKey, copiedKey);
	}

	@Test
	void r5SimilarityCovarianceUsesLineageAndUnsupportedSourcesFail() {
		GeoLocusV2 spline = lineSpline();
		GeoLocusV2 transformed = add("T=Translate(S,(2,3))");
		GeoLocusMetricResult transformedMetric =
				add("R=LocusLength(T,A,C)");
		SplineConstructorOccurrenceResult2D transformedA =
				resolver.resolve(transformed, point("A"));
		assertEquals(Status.UNIQUE, transformedA.getStatus());
		assertEquals(transformed.getPersistentLocusId(), transformedA
				.getUniqueMatch().getAddress().getSourceLocusId());
		assertNotEquals(spline.getPersistentLocusId(), transformedA
				.getUniqueMatch().getAddress().getSourceLocusId());
		assertEquals(4, finite(transformedMetric), 1E-9);

		add("u=0");
		add("U=(u,0)");
		add("Domain={false,{-2,2,true,true}}");
		GeoLocusV2 ordinary = add("L=LocusV2(U,u,Domain)");
		GeoLocusMetricResult rejected = add("Q=LocusLength(L,A,C)");
		assertEquals(Status.NO_ADDRESS, resolver.resolve(ordinary, point("A"))
				.getStatus());
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				rejected.getMetricResult().getComputationStatus());
	}

	@Test
	void computedListWithoutExactOccurrenceTopologyIsUnresolved() {
		add("A=(-2,0)");
		add("B=(0,0)");
		add("C=(2,0)");
		add("k=1");
		add("inputs=If(k>0,{A,B,C},{A,C,B})");
		GeoLocusV2 spline = add("S=SplineV2(inputs,3)");
		GeoLocusMetricResult metric = add("R=LocusLength(S,A,C)");
		assertEquals(Status.UNRESOLVED_INVALID,
				resolver.resolve(spline, point("A")).getStatus());
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				metric.getMetricResult().getComputationStatus());
	}

	@Test
	void existingSemanticPointAndA6PointBehaviorRemainUnchanged() {
		GeoLocusV2 spline = lineSpline();
		GeoPoint explicit = add("P=Point(S,\"spline-v2/main\",0.25)");
		GeoPoint omitted = add("Q=Point(S,0.75)");
		GeoLocusMetricResult metric = add("R=LocusLength(S,P,Q)");
		assertTrue(explicit.isDefined());
		assertTrue(omitted.isDefined());
		assertEquals(2, finite(metric), 1E-9);
		assertEquals(Status.NO_ADDRESS, resolver.resolve(spline, explicit)
				.getStatus());
	}

	private GeoLocusV2 lineSpline() {
		add("A=(-2,0)");
		add("B=(0,0)");
		add("C=(2,0)");
		return add("S=SplineV2({A,B,C},3)");
	}

	private GeoPoint point(String label) {
		return assertInstanceOf(GeoPoint.class, requireLookup(label));
	}

	private PersistentGeoId id(org.geogebra.common.kernel.geos.GeoElement geo) {
		return getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
	}

	private static double finite(GeoLocusMetricResult result) {
		assertNotNull(result.getMetricResult());
		return result.getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow();
	}
}
