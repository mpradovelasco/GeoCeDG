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
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoProvenLocusEndpoint2D;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.algos.SemanticMetricEndpointResolver2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.interaction.LocusPointCreation2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.algos.AlgoIf;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

class GeoCeDG120EndpointAssistanceTest extends G9U0PublicSurfaceTestBase {

	@Test
	void selectedRecipeKeepsIndependentIdentityAndReopens() {
		GeoLocusV2 locus = createTranslatedSegment();
		GeoPoint reference = add("P=Translate(K,u)");
		var resolver = new SemanticMetricEndpointResolver2D();
		assertEquals(SemanticMetricEndpointResolver2D.Status.NO_ADDRESS,
				resolver.resolve(locus, reference).getStatus());
		var proposal = AlgoProvenLocusEndpoint2D.propose(locus, reference);
		assertNotNull(proposal);
		activateUndo();
		getApp().storeUndoInfo();
		GeoPoint point = publish(locus, reference, proposal);
		assertTrue(point.isDefined());
		assertEquals(reference.getInhomX(), point.getInhomX(), 1e-12);
		assertEquals(reference.getInhomY(), point.getInhomY(), 1e-12);
		assertEquals(SemanticMetricEndpointResolver2D.Status.UNIQUE,
				resolver.resolve(locus, point).getStatus());
		var id = locus.getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		getApp().storeUndoInfo();
		getKernel().undo();
		assertNull(lookup("Pexact"));
		getKernel().redo();
		getApp().setXML(getApp().getXML(), true);
		GeoPoint reloaded = (GeoPoint) lookup("Pexact");
		GeoLocusV2 reloadedLocus = (GeoLocusV2) lookup("L");
		assertNotNull(reloaded);
		assertTrue(reloaded.isDefined());
		assertEquals(id, reloaded.getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(reloaded));
		assertEquals(SemanticMetricEndpointResolver2D.Status.UNIQUE,
				resolver.resolve(reloadedLocus, reloaded).getStatus());
		String clipboard = InternalClipboard.getTextToSave(getApp(), List.of(reloaded), text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(List.of(clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));
		var registry = reloaded.getConstruction().getSpatialIdentityRegistry();
		var copied = registry.getRecords().stream().filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast).filter(record -> id.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoPoint copiedPoint = (GeoPoint) registry.getGeo(copied.getId());
		assertTrue(copiedPoint.isDefined());
		var copiedRecipe = (AlgoProvenLocusEndpoint2D) copiedPoint.getParentAlgorithm();
		assertNotEquals(reloadedLocus, copiedRecipe.getSource());
		assertEquals(SemanticMetricEndpointResolver2D.Status.UNIQUE,
				resolver.resolve(copiedRecipe.getSource(), copiedPoint).getStatus());
	}

	@Test
	void referenceOnCoincidentDistinctSupportDoesNotProveEquivalence() {
		GeoLocusV2 locus = createTranslatedSegment();
		add("other=Segment((0,0),(4,0))");
		add("O=Point(other,0.25)");
		GeoPoint reference = add("P=Translate(O,u)");
		assertNull(AlgoProvenLocusEndpoint2D.propose(locus, reference));
	}

	@Test
	void lostVectorMakesTheSameRecipeUndefinedUntilItRecovers() {
		GeoLocusV2 locus = createTranslatedSegment();
		GeoPoint reference = add("P=Translate(K,u)");
		var proposal = AlgoProvenLocusEndpoint2D.propose(locus, reference);
		assertNotNull(proposal);
		GeoPoint point = publish(locus, reference, proposal);
		var parent = (AlgoProvenLocusEndpoint2D) point.getParentAlgorithm();
		proposal.vector().setUndefined();
		proposal.vector().updateCascade();
		assertFalse(point.isDefined());
		assertNull(parent.getAddress());
		proposal.vector().setCoords(0, 2, 0);
		proposal.vector().updateCascade();
		assertTrue(point.isDefined());
		assertEquals(2, point.getInhomY(), 1e-12);
	}

	@Test
	void undefinedNativeParameterWithdrawsCachedWitnessAndMetricUntilItRecovers() {
		GeoLocusV2 locus = createTranslatedSegment();
		GeoPoint reference = add("P=Translate(K,u)");
		GeoPoint point = publish(locus, reference, AlgoProvenLocusEndpoint2D.propose(locus, reference));
		var proof = (AlgoProvenLocusEndpoint2D) point.getParentAlgorithm();
		var registry = getConstruction().getSpatialIdentityRegistry();
		var identity = registry.getPersistentGeoId(point);
		add("T=Point(L,\"generator.main\",0.75)");
		GeoNumeric metric = add("measure=LocusLength(L,Pexact,T)");
		assertEquals(2, metric.getDouble(), 1e-12);
		GeoNumeric parameter = (GeoNumeric) lookup("witnessParameter");
		parameter.setUndefined();
		parameter.updateCascade();
		assertTrue(lookup("K").isDefined(), "Native producer still exposes its cached point");
		assertNull(AlgoProvenLocusEndpoint2D.propose(locus, reference));
		assertFalse(point.isDefined());
		assertEquals(AlgoProvenLocusEndpoint2D.Cause.PREMISE_LOST, proof.getCause());
		assertNull(proof.getAddress());
		assertFalse(metric.isDefined());
		parameter.setValue(0.25);
		parameter.updateCascade();
		assertEquals(identity, registry.getPersistentGeoId(point));
		assertEquals(AlgoProvenLocusEndpoint2D.Cause.RESOLVED, proof.getCause());
		assertEquals(2, metric.getDouble(), 1e-12);
	}

	@Test
	void clipboardRemapsBothCoincidentCutWitnessesAcrossDocumentsAndReopens() {
		String emptyDocument = getApp().getXML();
		createTranslatedSegment();
		GeoPoint reference = add("P=Translate(K,u)");
		GeoPoint first = add("J_1=Point(seg,0.5)");
		GeoPoint second = add("J_2=Point(seg,0.5)");
		GeoLocusV2 cutLocus = add("LCut=LocusV2(Q,E,\"constructive-cuts/v1\",J_1,J_2)");
		GeoPoint point = publish(cutLocus, reference,
				AlgoProvenLocusEndpoint2D.propose(cutLocus, reference));
		var registry = getConstruction().getSpatialIdentityRegistry();
		var identity = registry.getPersistentGeoId(point);
		var firstCutId = registry.getPersistentGeoId(first);
		var secondCutId = registry.getPersistentGeoId(second);
		String originalKey = ((AlgoProvenLocusEndpoint2D) point.getParentAlgorithm())
				.getAddress().getComponentLineageKey();
		add("T=Point(LCut,0.375)");
		GeoPoint placed;
		try (var selection = LocusPointCreation2D.queryFromReference(cutLocus, reference,
				LocusPointInteractionPolicy2D.initial(0.1))) {
			assertFalse(selection.getChoices().isEmpty(), selection::getFailure);
			placed = selection.commit("Placed", selection.getChoices().get(0));
		}
		double placedX = placed.getInhomX();
		GeoNumeric metric = add("measure=LocusLength(LCut,Pexact,T)");
		assertEquals(0.5, metric.getDouble(), 1e-12);
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				List.of(point, metric, placed), text -> text);
		int separator = clipboard.indexOf('\n');
		getApp().setXML(emptyDocument, true);
		assertNull(lookup("Pexact"));
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(List.of(clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));
		GeoIdentityRecord copied = copiedRecord(identity);
		GeoPoint pasted = (GeoPoint) getConstruction().getSpatialIdentityRegistry().getGeo(copied.getId());
		assertTrue(pasted.isDefined());
		String pastedLabel = pasted.getLabelSimple();
		String newKey = ((AlgoProvenLocusEndpoint2D) pasted.getParentAlgorithm())
				.getAddress().getComponentLineageKey();
		assertNotEquals(originalKey, newKey);
		assertTrue(newKey.contains(copiedRecord(firstCutId).getId().toExternalForm()));
		assertTrue(newKey.contains(copiedRecord(secondCutId).getId().toExternalForm()));
		assertEquals(0.5, ((GeoNumeric) lookup("measure")).getDouble(), 1e-12);
		assertCopiedPlacement(newKey, placedX);
		getApp().setXML(getApp().getXML(), true);
		GeoPoint reopened = (GeoPoint) lookup(pastedLabel);
		assertTrue(reopened.isDefined());
		assertEquals(copied.getId(), getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(reopened));
		assertEquals(newKey, ((AlgoProvenLocusEndpoint2D) reopened.getParentAlgorithm())
				.getAddress().getComponentLineageKey());
		assertEquals(0.5, ((GeoNumeric) lookup("measure")).getDouble(), 1e-12);
		assertCopiedPlacement(newKey, placedX);
	}

	private void assertCopiedPlacement(String component, double x) {
		GeoPoint point = (GeoPoint) lookup("Placed");
		assertEquals(x, point.getInhomX(), 1e-12);
		var parent = (AlgoSemanticLocusPoint2D) point.getParentAlgorithm();
		assertEquals(component, parent.getCurrentSemanticAddress().getComponentLineageKey());
	}

	@Test
	void dependentInlineTextCannotSupplyTheRetainedProofSelector() {
		GeoLocusV2 locus = createTranslatedSegment();
		GeoPoint reference = add("P=Translate(K,u)");
		var proposal = AlgoProvenLocusEndpoint2D.propose(locus, reference);
		assertNotNull(proposal);
		var construction = getConstruction();
		GeoBoolean condition = add("condition=witnessParameter>0");
		boolean suppressed = construction.isSuppressLabelsActive();
		construction.setSuppressLabelCreation(true);
		GeoText dependent;
		try {
			dependent = (GeoText) new AlgoIf(construction, null, new ArrayList<>(List.of(condition)),
					new ArrayList<>(List.of(new GeoText(construction, proposal.recipe()),
							new GeoText(construction, "other")))).getOutput(0);
		} finally {
			construction.setSuppressLabelCreation(suppressed);
		}
		assertFalse(dependent.isLabelSet());
		assertFalse(dependent.isIndependent());
		assertEquals(proposal.recipe(), dependent.getTextString());
		assertThrows(IllegalArgumentException.class, () -> LocusV2PublicOperations.createProvenEndpoint(
				construction, "Pexact", locus, reference, proposal.witness(), proposal.vector(), dependent));
	}

	@Test
	void copiedCutSelectorSurvivesReopeningWhileWitnessPremiseIsLost() {
		add("witnessAvailable=true");
		createTranslatedSegment("If(witnessAvailable,0.25)");
		GeoPoint reference = add("P=Translate(K,u)");
		add("J=Point(seg,0.5)");
		GeoLocusV2 locus = add("LCut=LocusV2(Q,E,\"constructive-cuts/v1\",J)");
		GeoPoint point = publish(locus, reference, AlgoProvenLocusEndpoint2D.propose(locus, reference));
		var identity = getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(point);
		GeoBoolean available = (GeoBoolean) lookup("witnessAvailable");
		available.setValue(false);
		available.updateCascade();
		assertFalse(point.isDefined());
		String clipboard = InternalClipboard.getTextToSave(getApp(), List.of(point), text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(List.of(clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));
		var copied = copiedRecord(identity);
		var registry = getConstruction().getSpatialIdentityRegistry();
		GeoPoint pasted = (GeoPoint) registry.getGeo(copied.getId());
		assertFalse(pasted.isDefined());
		String label = pasted.getLabelSimple();
		getApp().setXML(getApp().getXML(), true);
		GeoPoint reopened = (GeoPoint) lookup(label);
		var parent = (AlgoProvenLocusEndpoint2D) reopened.getParentAlgorithm();
		assertEquals(AlgoProvenLocusEndpoint2D.Cause.PREMISE_LOST, parent.getCause());
		GeoNumeric copiedParameter = (GeoNumeric) parent.getInput(2).getParentAlgorithm().getInput(1);
		GeoBoolean copiedAvailability = (GeoBoolean) copiedParameter.getParentAlgorithm().getInput(0);
		copiedAvailability.setValue(true);
		copiedAvailability.updateCascade();
		assertEquals(AlgoProvenLocusEndpoint2D.Cause.RESOLVED, parent.getCause());
		assertEquals(copied.getId(), reopened.getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(reopened));
		assertFalse(lookup("Pexact").isDefined());
	}

	@Test
	void cutSelectorRemapReordersFreshWitnessesAndRequiresTheFullBoundaryClosure() {
		String prefix = "generator.main/component/declared-domain/domain-end/lower;";
		String suffix = "/DECLARED_CONSTRUCTIVE_CUT_V1/end";
		String retained = prefix + "geo:a+geo:b" + suffix;
		assertEquals(prefix + "geo:c+geo:z" + suffix, LocusComponentLineage2D.remapDeclaredCuts(
				"generator.main", retained, Map.of("geo:a", "geo:z", "geo:b", "geo:c")));
		assertEquals(retained, LocusComponentLineage2D.remapDeclaredCuts(
				"generator.main", retained, Map.of("geo:a", "geo:z", "geo:unrelated", "geo:c")));
		String noncanonical = prefix + "geo:b+geo:a" + suffix;
		assertEquals(noncanonical, LocusComponentLineage2D.remapDeclaredCuts(
				"generator.main", noncanonical, Map.of("geo:a", "geo:z", "geo:b", "geo:c")));
	}

	private GeoIdentityRecord copiedRecord(PersistentGeoId originalId) {
		return getConstruction().getSpatialIdentityRegistry().getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(record -> originalId.equals(record.getCopySourceId())).findFirst().orElseThrow();
	}

	private static GeoPoint publish(GeoLocusV2 locus, GeoPoint reference,
			AlgoProvenLocusEndpoint2D.Proposal proposal) {
		var construction = locus.getConstruction();
		return LocusV2PublicOperations.createProvenEndpoint(construction, "Pexact",
				locus, reference, proposal.witness(), proposal.vector(),
				new GeoText(construction, proposal.recipe()));
	}

	private GeoLocusV2 createTranslatedSegment() {
		return createTranslatedSegment("0.25");
	}

	private GeoLocusV2 createTranslatedSegment(String witnessParameter) {
		add("seg=Segment((0,0),(4,0))");
		add("E=Point(seg)");
		add("witnessParameter=" + witnessParameter);
		add("K=Point(seg,witnessParameter)");
		add("u=(0,1)");
		add("Q=Translate(E,u)");
		return add("L=LocusV2(Q,E)");
	}
}
