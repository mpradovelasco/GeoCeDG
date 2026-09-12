/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricValueKind;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResolver2D;
import org.geocedg.common.kernel.spline.SplineConstructorOccurrenceResult2D;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/** Focused POST-G9U1-A3 V2 compatibility, lifecycle and P3 evidence. */
class PostG9U1A3CompatibleV2RedefineTest
		extends G9U0PublicSurfaceTestBase {

	@Test
	void compatibleLocusRedefineRetainsIdentityAndAdvancesTopology() {
		activateUndo();
		add("s=0");
		add("Q=(s,0)");
		add("Q2=(s,2*s)");
		add("D={false,{-2,2,true,true}}");
		add("K=LocusV2(Q2,s,D)");
		GeoLocusV2 source = add("L=LocusV2(Q,s,D)");
		final GeoLocusV2 afterAnchor = add("After=LocusV2(Q,s,D)");
		GeoPoint first = add("P=Point(L,\"" + BRANCH + "\",-1)");
		GeoPoint second = add("R=Point(L,\"" + BRANCH + "\",1)");
		GeoLocusMetricResult metric = add("M=LocusLength(L,P,R,\"reverse\","
				+ "\"wrap-to-start\",\"zero-length\")");
		PersistentGeoId sourceId = id(source);
		PersistentGeoId firstId = id(first);
		PersistentGeoId metricId = id(metric);
		GeoIdentityRecord before = record(sourceId);
		getApp().storeUndoInfo();

		assertTrue(change(source, "LocusV2(Q2,s,D)", false).isBlank());

		GeoLocusV2 redefined = locus("L");
		GeoIdentityRecord after = record(sourceId);
		assertEquals(sourceId, id(redefined));
		assertEquals(firstId, id(requireLookup("P")));
		assertEquals(metricId, id(requireLookup("M")));
		assertEquals(before.getDefinitionRevision() + 1,
				after.getDefinitionRevision());
		assertEquals(before.getTopologyRevision() + 1,
				after.getTopologyRevision());
		assertTrue(redefined.isDefined());
		assertTrue(requireLookup("P").isDefined());
		assertTrue(requireLookup("M").isDefined());
		assertTrue(redefined.getConstructionIndex()
				< locus(afterAnchor.getLabelSimple()).getConstructionIndex());
		assertTrue(redefined.getConstructionIndex()
				< requireLookup("P").getConstructionIndex());
		assertTrue(redefined.getConstructionIndex()
				< requireLookup("M").getConstructionIndex());
		String retainedXml = getApp().getXML();
		getApp().storeUndoInfo();
		getKernel().undo();
		assertEquals(sourceId, id(locus("L")));
		assertEquals(before.getDefinitionRevision(),
				record(sourceId).getDefinitionRevision());
		getKernel().redo();
		assertEquals(retainedXml, getApp().getXML());
		assertEquals(sourceId, id(locus("L")));
	}

	@Test
	void splineRedefineRevalidatesA1AndA2BindingsWithoutStaleGeometry()
			throws Exception {
		add("A=(-2,0)");
		add("B=(0,1)");
		add("C=(2,0)");
		add("D=(-2,1)");
		add("E=(0,2)");
		add("F=(2,1)");
		add("pointsA={A,B,C}");
		add("pointsB={D,E,F}");
		add("degree=3");
		add("T=SplineV2(pointsB,degree)");
		GeoLocusV2 source = add("S=SplineV2(pointsA,degree)");
		GeoLocusMetricResult metric = add("M=LocusLength(S,A,C,\"forward\","
				+ "\"strict\",\"zero-length\")");
		PersistentGeoId sourceId = id(source);
		PersistentGeoId metricId = id(metric);

		assertTrue(change(source, "SplineV2(pointsB,degree)", false).isBlank());
		assertEquals(sourceId, id(locus("S")));
		assertEquals(metricId, id(requireLookup("M")));
		assertEquals(SplineConstructorOccurrenceResult2D.Status.NO_ADDRESS,
				new SplineConstructorOccurrenceResolver2D().resolve(locus("S"),
						(GeoPoint) requireLookup("A")).getStatus());
		GeoLocusMetricResult invalidMetric = (GeoLocusMetricResult) requireLookup("M");
		assertEquals(MetricValueKind.ABSENT,
				invalidMetric.getMetricResult().getMetricValue().getKind());
		assertEquals(MetricComputationStatus.INVALID_QUERY,
				invalidMetric.getMetricResult().getComputationStatus());

		assertTrue(change(locus("S"), "SplineV2(pointsA,degree)", false).isBlank());
		assertEquals(sourceId, id(locus("S")));
		assertEquals(metricId, id(requireLookup("M")));
		assertTrue(requireLookup("M").isDefined());
		String xml = getApp().getXML();
		getApp().setXML(xml, true);
		assertEquals(sourceId, id(locus("S")));
		assertEquals(metricId, id(requireLookup("M")));
		assertTrue(requireLookup("M").isDefined());
	}

	@Test
	void explicitReplacementIsFreshAndDifferentConstructorRejectsWithoutIntent() {
		activateUndo();
		add("A=(-2,0)");
		add("B=(0,1)");
		add("C=(2,0)");
		add("points={A,B,C}");
		add("degree=3");
		add("w(x,y)=1+x^2+y^2");
		GeoLocusV2 source = add("S=SplineV2(points,degree)");
		GeoPoint first = add("P=Point(S,\"" + BRANCH + "\",-1)");
		GeoPoint second = add("R=Point(S,\"" + BRANCH + "\",1)");
		GeoLocusMetricResult metric = add("M=LocusLength(S,P,R)");
		PersistentGeoId originalId = id(source);
		final PersistentGeoId firstId = id(first);
		final PersistentGeoId secondId = id(second);
		final PersistentGeoId metricId = id(metric);
		String before = getApp().getXML();
		getApp().storeUndoInfo();

		String rejected = change(source, "SplineV2(points,degree,w)", false);
		assertFalse(rejected.isBlank());
		assertEquals(before, getApp().getXML());
		assertEquals(originalId, id(locus("S")));

		String replacement = change(locus("S"),
				"SplineV2(points,degree,w)", true);
		assertTrue(replacement.isBlank(), replacement);
		PersistentGeoId replacementId = id(locus("S"));
		assertNotEquals(originalId, replacementId);
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(originalId));
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(firstId));
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(secondId));
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(metricId));
		GeoElement currentPoint = getKernel().lookupLabel("P");
		GeoElement currentMetric = getKernel().lookupLabel("M");
		assertTrue(currentPoint == null || getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(currentPoint) == null);
		assertTrue(currentMetric == null || getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(currentMetric) == null);

		final String replaced = getApp().getXML();
		getApp().storeUndoInfo();
		getKernel().undo();
		assertEquals(before, getApp().getXML());
		assertEquals(originalId, id(locus("S")));
		assertEquals(firstId, id(requireLookup("P")));
		assertEquals(metricId, id(requireLookup("M")));
		getKernel().redo();
		assertEquals(replaced, getApp().getXML());
		assertEquals(replacementId, id(locus("S")));
		GeoElement redonePoint = getKernel().lookupLabel("P");
		assertTrue(redonePoint == null || getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(redonePoint) == null);
	}

	@Test
	void transformContractRetainsOnlyTheSameSemanticConstructorFamily() {
		createLine();
		add("v=(1,0)");
		add("w=(0,2)");
		add("k=2");
		add("K=Translate(L,w)");
		GeoLocusV2 image = add("T=Translate(L,v)");
		PersistentGeoId originalId = id(image);
		GeoIdentityRecord before = record(originalId);

		assertTrue(change(image, "Translate(L,w)", false).isBlank());
		assertEquals(originalId, id(locus("T")));
		assertEquals(before.getDefinitionRevision() + 1,
				record(originalId).getDefinitionRevision());
		assertEquals(before.getTopologyRevision() + 1,
				record(originalId).getTopologyRevision());

		String retainedXml = getApp().getXML();
		assertFalse(change(locus("T"), "Dilate(L,k)", false).isBlank());
		assertEquals(retainedXml, getApp().getXML());
		assertEquals(originalId, id(locus("T")));
		assertTrue(change(locus("T"), "Dilate(L,k)", true).isBlank());
		assertNotEquals(originalId, id(locus("T")));
	}

	@Test
	void newPredecessorConstraintForcesOnlyTheMinimalLaterRelocation() {
		add("A=(-2,0)");
		add("B=(0,1)");
		add("C=(2,0)");
		add("pointsA={A,B,C}");
		add("degree=3");
		GeoLocusV2 target = add("S=SplineV2(pointsA,degree)");
		GeoElement leftUnaffected = add("LeftUnaffected=7");
		add("D=(-2,1)");
		add("E=(0,2)");
		add("F=(2,1)");
		GeoElement newPredecessor = add("pointsB={D,E,F}");
		GeoElement rightUnaffected = add("RightUnaffected=8");
		PersistentGeoId targetId = id(target);
		int leftBefore = leftUnaffected.getConstructionIndex();
		int newPredecessorBefore = newPredecessor.getConstructionIndex();
		final int rightBefore = rightUnaffected.getConstructionIndex();
		assertTrue(target.getConstructionIndex() < leftBefore);

		String retained = change(target, "SplineV2(pointsB,degree)", false);
		assertTrue(retained.isBlank(), retained);

		GeoLocusV2 actual = locus("S");
		assertEquals(targetId, id(actual));
		assertTrue(newPredecessor.getConstructionIndex()
				< actual.getConstructionIndex());
		assertTrue(actual.getConstructionIndex()
				< rightUnaffected.getConstructionIndex());
		assertTrue(leftUnaffected.getConstructionIndex()
				< newPredecessor.getConstructionIndex());
		assertTrue(leftBefore < newPredecessorBefore);
		assertTrue(newPredecessorBefore < rightBefore);
	}

	@Test
	void renameAndDeleteRecreateDoNotImpersonateContinuity() {
		GeoLocusV2 source = createLine();
		PersistentGeoId originalId = id(source);
		source.setLabel("Renamed");
		assertEquals(originalId, id(source));
		source.remove();
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(originalId));
		GeoLocusV2 recreated = add("L=LocusV2(Q,s,D)");
		assertNotEquals(originalId, id(recreated));

		GeoNumeric ordinary = add("ordinary=1");
		assertNull(getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(ordinary));
		assertTrue(change(ordinary, "2", false).isBlank());
		GeoElement ordinaryResult = requireLookup("ordinary");
		assertEquals(2, ((GeoNumeric) ordinaryResult).getDouble());
		assertNull(getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(ordinaryResult));
	}

	private String change(GeoElement target, String definition,
			boolean replacement) {
		ErrorAccumulator errors = new ErrorAccumulator();
		AtomicReference<GeoElementND> result = new AtomicReference<>();
		EvalInfo info = new EvalInfo(true, true)
				.withSymbolicMode(AlgebraProcessor.getRedefinitionMode(target,
						getKernel()))
				.withLabelRedefinitionAllowedFor(target.getLabelSimple())
				.withSymbolic(true).withSliders(true);
		if (replacement) {
			info = info.withSpatialReplacementOperation();
		}
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				target, definition, info, false, result::set, errors);
		return errors.getErrors();
	}

	private PersistentGeoId id(GeoElement geo) {
		PersistentGeoId id = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
		assertNotNull(id);
		return id;
	}

	private GeoIdentityRecord record(PersistentGeoId id) {
		GeoIdentityRecord record = getConstruction().getSpatialIdentityRegistry()
				.getGeoRecord(id);
		assertNotNull(record);
		return record;
	}

	private GeoLocusV2 locus(String label) {
		return (GeoLocusV2) requireLookup(label);
	}
}
