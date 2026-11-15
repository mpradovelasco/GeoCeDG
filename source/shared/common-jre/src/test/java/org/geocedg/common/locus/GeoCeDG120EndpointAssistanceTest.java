/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2 */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoProvenLocusEndpoint2D;
import org.geocedg.common.kernel.algos.SemanticMetricEndpointResolver2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
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

	private static GeoPoint publish(GeoLocusV2 locus, GeoPoint reference,
			AlgoProvenLocusEndpoint2D.Proposal proposal) {
		var construction = locus.getConstruction();
		return LocusV2PublicOperations.createProvenEndpoint(construction, "Pexact",
				locus, reference, proposal.witness(), proposal.vector(),
				new GeoText(construction, proposal.recipe()));
	}

	private GeoLocusV2 createTranslatedSegment() {
		add("seg=Segment((0,0),(4,0))");
		add("E=Point(seg)");
		add("K=Point(seg,0.25)");
		add("u=(0,1)");
		add("Q=Translate(E,u)");
		return add("L=LocusV2(Q,E)");
	}
}
