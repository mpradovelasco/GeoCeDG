/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.intersection.LocusSemanticIntersectionToken2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

class GeoCeDG120ConstructiveCutsTest extends G9U0PublicSurfaceTestBase {

	@Test
	void fullCircleCutKeepsDistinctEndsAndLengthThroughXmlReload() {
		add("C=Circle((0,0),2)");
		add("E=Point(C)");
		add("K=Point(C,0.5)");
		add("u=Vector((0,0),(0,1))");
		add("Q=Translate(E,u)");
		GeoLocusV2 locus = add("L=LocusV2(Q,E,\"constructive-cuts/v1\",K)");
		var structure = topology(locus);
		assertEquals(LocusExistenceStructure2D.Completeness.COMPLETE, structure.getCompleteness());
		assertFalse(structure.isClosedPeriodic());
		assertEquals(1, structure.getContinuousValidComponents().size());
		var component = structure.getContinuousValidComponents().get(0);
		assertTrue(component.isFullLength());
		assertEquals(0, component.getStart());
		assertTrue(component.getLowerBoundary().isIncluded());
		assertFalse(component.getUpperBoundary().isIncluded());
		assertEquals(LocusBoundary2D.Kind.DECLARED_CONSTRUCTIVE_CUT,
				component.getUpperBoundary().getKind());
		assertNotEquals(component.getLowerBoundary().getSource(),
				component.getUpperBoundary().getSource());
		assertSameLimit(locus, structure.getBranchKey(), component.getStart());
		GeoNumeric length = add("m=LocusLength(L)");
		assertEquals(4 * Math.PI, length.getDouble(), 1e-6);
		String key = LocusComponentLineage2D.of(structure, component);
		assertTrue(((GeoPoint) lookup("K")).rename("CutWitness"));
		getApp().setXML(getApp().getXML(), true);
		GeoLocusV2 restored = (GeoLocusV2) lookup("L");
		assertNotNull(restored);
		assertEquals(List.of(key), LocusComponentLineage2D.keys(topology(restored)));
		assertEquals(4 * Math.PI, ((GeoNumeric) lookup("m")).getDouble(), 1e-6);
	}

	@Test
	void multipleSegmentCutsRetainLineageAcrossMotionLossAndRecovery() {
		add("S=Segment((0,0),(4,0))");
		add("E=Point(S)");
		GeoNumeric control = add("a=0.25");
		add("K=Point(S,a)");
		add("J=Point(S,0.75)");
		add("u=Vector((0,0),(0,1))");
		add("Q=Translate(E,u)");
		GeoLocusV2 locus = add("L=LocusV2(Q,E,\"constructive-cuts/v1\",J,K)");
		var first = topology(locus);
		assertEquals(List.of(0.0, 0.25, 0.75), componentStarts(first));
		var pieces = first.getContinuousValidComponents();
		assertTrue(pieces.get(0).contains(Math.nextDown(0.25), 1e-9));
		assertFalse(pieces.get(1).contains(Math.nextDown(0.25), 1e-9));
		GeoNumeric length = add("m=LocusLength(L)");
		assertEquals(4, length.getDouble(), 1e-10);
		List<String> keys = LocusComponentLineage2D.keys(first);
		control.setValue(0.5);
		control.updateCascade();
		assertEquals(List.of(0.0, 0.5, 0.75), componentStarts(topology(locus)));
		assertEquals(keys, LocusComponentLineage2D.keys(topology(locus)));
		control.setUndefined();
		control.updateCascade();
		var lost = topology(locus);
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED,
				lost.getCompleteness());
		assertEquals(LocusCoverageCause2D.CONSTRUCTIVE_CUT_WITNESS_NOT_ESTABLISHED,
				lost.getCoverage().get(0).getCause());
		control.setValue(0.25);
		control.updateCascade();
		assertEquals(List.of(0.0, 0.25, 0.75), componentStarts(topology(locus)));
		assertEquals(keys, LocusComponentLineage2D.keys(topology(locus)));
	}

	@Test
	void provesCanonicalDirectionFromSelectedHorizontalDiameterRoot() {
		add("B=(1,2)");
		add("C=Circle(B,2)");
		add("i=Line(B,xAxis)");
		add("E=Point(C)");
		add("u=Vector((0,0),(0,1))");
		add("Q=Translate(E,u)");
		GeoPoint root = add("Intersect(i,C)");
		// The first computation assigns roots, the next settles the inherited permutation.
		root.getParentAlgorithm().update();
		GeoLocusV2 locus = add("L=LocusV2(Q,E,\"constructive-cuts/v1\","
				+ root.getLabelSimple() + ")");
		var structure = topology(locus);
		assertEquals(LocusExistenceStructure2D.Completeness.COMPLETE, structure.getCompleteness());
		assertEquals(List.of(root.getInhomX() > 1 ? 0.0 : -Math.PI), componentStarts(structure));
	}

	@Test
	void unsupportedPeriodicCompositionRetainsItsUpstreamEventWithoutClosureClaim() {
		add("C=Circle((0,0),2)");
		add("E=Point(C)");
		add("Q=E+(0,0)");
		add("L=LocusV2(Q,E)");
		add("P=Point(L,\"generator.main\",0)");
		add("m=LocusLength(L,P,Q)");
		add("lengthAngle=m/2");
		add("T=Rotate((1,0),lengthAngle)");
		GeoLocusV2 image = add("W=LocusV2(T,E)");
		var structure = topology(image);
		assertFalse(structure.isClosedPeriodic());
		assertEquals(LocusExistenceStructure2D.Completeness.NOT_ESTABLISHED,
				structure.getCompleteness());
		assertEquals(LocusCoverageCause2D.COMPOSED_EVENT_CONTINUITY_NOT_ESTABLISHED,
				structure.getCoverageAt(0).getCause());
		var samples = image.getExploratoryEvidence(structure.getBranchKey());
		assertNotNull(samples);
		assertTrue(samples.getValidRuns().stream().anyMatch(run -> run.getUpper() < 0));
		assertTrue(samples.getValidRuns().stream().anyMatch(run -> run.getLower() > 0));
		assertTrue(samples.getValidRuns().stream().noneMatch(run ->
				run.getLower() < 0 && run.getUpper() >= 0));
	}

	@Test
	void fullTurnCancellationRequiresTheActualCircleRadiusInput() {
		GeoNumeric radius = add("radius=2");
		add("O=(0,0)");
		add("A=(1,0)");
		add("C=Circle(O,radius)");
		add("E=Point(C)");
		add("u=Vector((0,0),(0,1))");
		add("Q=Translate(E,u)");
		add("L=LocusV2(Q,E)");
		add("P=Point(L,\"generator.main\",0)");
		add("m=LocusLength(L,P,Q)");
		add("lengthAngle=m/radius");
		add("T=Rotate(A,lengthAngle,O)");
		GeoLocusV2 image = add("W=LocusV2(T,E)");
		assertTrue(topology(image).isClosedPeriodic(), topology(image).getSemanticSignature());
		radius.setValue(3);
		radius.updateCascade();
		assertTrue(topology(image).isClosedPeriodic(), topology(image).getSemanticSignature());
		add("unrelatedRadius=3");
		add("otherAngle=m/unrelatedRadius");
		add("Z=Rotate(A,otherAngle,O)");
		GeoLocusV2 unrelated = add("V=LocusV2(Z,E)");
		assertFalse(topology(unrelated).isClosedPeriodic());
		assertEquals(LocusCoverageCause2D.COMPOSED_EVENT_CONTINUITY_NOT_ESTABLISHED,
				topology(unrelated).getCoverageAt(0).getCause());
	}

	@Test
	void explicitCutRecipeReplaysAndCopiesItsWitnessWithoutIdentityReuse() {
		add("S=Segment((0,0),(4,0))");
		add("E=Point(S)");
		GeoPoint witness = add("K=Point(S,0.25)");
		add("u=Vector((0,0),(0,1))");
		add("Q=Translate(E,u)");
		activateUndo();
		getApp().storeUndoInfo();
		GeoLocusV2 locus = add("L=LocusV2(Q,E,\"constructive-cuts/v1\",K)");
		var locusId = locus.getPersistentLocusId();
		var registry = getConstruction().getSpatialIdentityRegistry();
		var witnessId = registry.getPersistentGeoId(witness);
		getApp().storeUndoInfo();
		getKernel().undo();
		assertNull(lookup("L"));
		getKernel().redo();
		locus = (GeoLocusV2) requireLookup("L");
		assertEquals(locusId, locus.getPersistentLocusId());
		assertEquals(List.of(0.0, 0.25), componentStarts(topology(locus)));
		String clipboard = InternalClipboard.getTextToSave(getApp(), List.of(locus), text -> text);
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), labels, clipboard.substring(separator));
		var copyRecord = registry.getRecords().stream().filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> locusId.equals(record.getCopySourceId())).findFirst().orElseThrow();
		GeoLocusV2 copy = (GeoLocusV2) registry.getGeo(copyRecord.getId());
		assertNotEquals(locusId, copy.getPersistentLocusId());
		var copyWitness = copy.getParentAlgorithm().getInput(3).toGeoElement();
		var copyWitnessId = registry.getPersistentGeoId(copyWitness);
		assertNotEquals(witnessId, copyWitnessId);
		assertEquals(witnessId, registry.getGeoRecord(copyWitnessId).getCopySourceId());
		assertTrue(copyRecord.getDependencies().contains(copyWitnessId));
		assertEquals(List.of(0.0, 0.25), componentStarts(topology(copy)));
		assertEquals(LocusComponentLineage2D.keys(topology(locus)),
				LocusComponentLineage2D.keys(topology(copy)));
	}

	@Test
	void exactMaterializedRootSurvivesTwoClosureCopiesAndReopenWithOrWithoutCut() {
		String empty = getApp().getXML();
		for (boolean cut : List.of(false, true)) {
			getApp().setXML(empty, true);
			add("S=Segment((0,0),(4,0))");
			add("E=Point(S)");
			add("u=Vector((0,0),(0,1))");
			add("Q=Translate(E,u)");
			if (cut) {
				add("J=Point(S,0.5)");
				add("L=LocusV2(Q,E,\"constructive-cuts/v1\",J)");
			} else {
				add("L=LocusV2(Q,E)");
			}
			add("vertical=Line((1,0),(1,2))");
			GeoLocusIntersectionResult rich = add("R=Intersect(L,vertical)");
			var roots = rich.getIntersectionResult().getFiniteSolutions();
			assertEquals(1, roots.size());
			String token = roots.get(0).getIdentity().getRootToken();
			assertTrue(rich.findExactPointAdmissibleSolution(token).isPresent());
			GeoPoint point = add("X=Intersect(R,\"" + token + "\")");
			assertTrue(point.isDefined());
			var original = LocusSemanticIntersectionToken2D.decode(token).orElseThrow();
			for (int generation = 0; generation < 2; generation++) {
				String copiedXml = InternalClipboard.getTextToSave(getApp(), List.of(point), text -> text);
				int separator = copiedXml.indexOf('\n');
				getApp().setXML(empty, true);
				InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
						new ArrayList<>(List.of(copiedXml.substring(0, separator).split(" "))),
						copiedXml.substring(separator));
				point = (GeoPoint) requireLookup("X");
				String expected = assertExactCopiedRoot(point);
				assertNotEquals(token, expected);
				assertEquals(original.getIncarnation(), LocusSemanticIntersectionToken2D
						.decode(expected).orElseThrow().getIncarnation());
				getApp().setXML(getApp().getXML(), true);
				point = (GeoPoint) requireLookup("X");
				assertEquals(expected, assertExactCopiedRoot(point));
				token = expected;
			}
		}
	}

	private static String assertExactCopiedRoot(GeoPoint point) {
		assertTrue(point.isDefined());
		assertEquals(1, point.getInhomX(), 1e-12);
		assertEquals(1, point.getInhomY(), 1e-12);
		var parent = (AlgoLocusIntersectionPointV2) point.getParentAlgorithm();
		String token = parent.getEffectiveRootToken();
		assertEquals(parent.getSelectedRootToken(), token);
		assertTrue(parent.getRichInput().findExactPointAdmissibleSolution(token).isPresent());
		return token;
	}

	@Test
	void clipboardCutBindingsWithoutAnAttachedOwnerCannotEditALiveLocus() {
		add("S=Segment((0,0),(4,0))");
		add("E=Point(S)");
		GeoPoint witness = add("K=Point(S,0.5)");
		add("u=Vector((0,0),(0,1))");
		add("Q=Translate(E,u)");
		GeoLocusV2 locus = add("L=LocusV2(Q,E,\"constructive-cuts/v1\",K)");
		String witnessId = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(witness)
				.toExternalForm();
		String injected = "<element type=\"locusv2\" label=\"L\"><locusCutWitnessBindings state=\""
				+ "cut-witness-bindings/v1|" + witnessId + "=geo:11111111111111111111111111111111"
				+ "\"/></element>";
		String before = getApp().getXML();
		assertThrows(SpatialIdentityException.class,
				() -> InternalClipboard.evalClipboardXMLAtomically(getApp(), injected));
		assertEquals(before, getApp().getXML());
		assertEquals(List.of(0.0, 0.5), componentStarts(topology(locus)));
	}

	private static LocusExistenceStructure2D topology(GeoLocusV2 locus) {
		assertTrue(locus.isDefined());
		return locus.getSemanticDefinition().getBranches().get(0).getExistenceStructure();
	}

	private static List<Double> componentStarts(LocusExistenceStructure2D structure) {
		return structure.getContinuousValidComponents().stream()
				.map(LocusExistenceStructure2D.ContinuousComponent::getStart).toList();
	}

	private static void assertSameLimit(GeoLocusV2 locus, String branch, double parameter) {
		var definition = locus.getSemanticDefinition();
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
			var start = definition.evaluateComponentEnd(branch, parameter, false, session);
			var end = definition.evaluateComponentEnd(branch, parameter, true, session);
			assertTrue(start.isValid());
			assertTrue(end.isValid());
			assertEquals(start.getPoint().getX(), end.getPoint().getX());
			assertEquals(start.getPoint().getY(), end.getPoint().getY());
		}
	}
}
