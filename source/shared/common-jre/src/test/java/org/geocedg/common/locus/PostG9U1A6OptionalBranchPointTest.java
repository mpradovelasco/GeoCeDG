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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPrincipalBranchState2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/** Focused POST-G9U1-A6 public, persistence and fail-closed scenarios. */
class PostG9U1A6OptionalBranchPointTest extends G9U0PublicSurfaceTestBase {

	@Test
	void omittedBranchRetainsConcreteSelectorAndExplicitFormIsUnchanged() {
		GeoLocusV2 locus = createLine();
		GeoNumeric parameter = add("u=0.25");
		GeoPoint omitted = add("P=Point(L,u)");
		GeoPoint explicit = add("E=Point(L,\"" + BRANCH + "\",u)");
		AlgoSemanticLocusPoint2D parent = semanticParent(omitted);
		LocusSemanticAddress2D selected = parent.getSemanticAddress();

		assertTrue(omitted.isDefined());
		assertTrue(explicit.isDefined());
		assertEquals(0.25, omitted.getInhomX(), 1E-12);
		assertEquals(BRANCH, selected.getBranchKey());
		assertTrue(LocusPrincipalBranchState2D.decode(
				parent.getBranchInput().getTextString()).matches(selected));
		assertEquals(ConstructionGeoRedefineProvider
				.PRINCIPAL_BRANCH_POINT_OUTPUT_ROLE, pointRecord(omitted)
						.getStableOutputRole());
		assertSame(parameter, parent.getParameterInput().toGeoElement());

		parameter.setValue(0.75);
		parameter.updateCascade();
		assertTrue(omitted.isDefined());
		assertEquals(0.75, omitted.getInhomX(), 1E-12);
		assertEquals(selected.getBranchKey(), parent.getSemanticAddress()
				.getBranchKey());
		assertEquals(selected.getComponentLineageKey(), parent.getSemanticAddress()
				.getComponentLineageKey());
	}

	@Test
	void zeroEligibleAddressFailsExplicitly() {
		GeoLocusV2 locus = createLine();
		assertThrows(AssertionError.class, () -> add("Outside=Point(L,3)"));
		locus.setUndefined();
		assertThrows(AssertionError.class, () -> add("Invalid=Point(L,0)"));
	}

	@Test
	void gapsAndMultipleComponentsFailExplicitly() {
		GeoLocusV2 shared = createScalarLocus("S", "v", "R", "(v,1)",
				"{false,{-2,-1,true,true},{1,2,true,true}}");
		assertNotNull(shared);
		assertThrows(AssertionError.class, () -> add("Gap=Point(S,0)"));
		LocusDefinition2D definition = shared.getSemanticDefinition();
		LocusBranch2D branch = definition.getBranches().get(0);
		LocusBranch2D sharedEndpoint = new LocusBranch2D(branch.getBranchKey(),
				branch.getDeclaredDriverDomain(), List.of(
						new LocusInterval2D(-2, 0, true, true),
						new LocusInterval2D(0, 2, true, true)),
				branch.getOrientation(), branch.getProvenance(), branch.getLineage(),
				branch.getProperties(), branch.getQuality());
		publish(shared, definition, List.of(sharedEndpoint), "shared-endpoint");
		assertThrows(AssertionError.class, () -> add("Shared=Point(S,0)"));
	}

	@Test
	void branchOrderCannotAuthorizeAndAnExistingPointNeverRetargets() {
		GeoLocusV2 locus = createLine();
		GeoPoint point = add("P=Point(L,0.5)");
		AlgoSemanticLocusPoint2D parent = semanticParent(point);
		LocusSemanticAddress2D retained = parent.getSemanticAddress();
		LocusDefinition2D original = locus.getSemanticDefinition();
		LocusBranch2D originalBranch = original.getBranches().get(0);
		LocusBranch2D other = branchLike(originalBranch, "a6/other");

		publish(locus, original, List.of(other, originalBranch), "ambiguous-a");
		parent.compute();
		assertFalse(point.isDefined());
		assertEquals(retained, parent.getSemanticAddress());
		assertThrows(AssertionError.class, () -> add("NewA=Point(L,0.5)"));

		publish(locus, original, List.of(originalBranch, other), "ambiguous-b");
		parent.compute();
		assertFalse(point.isDefined());
		assertEquals(retained, parent.getSemanticAddress());
		assertThrows(AssertionError.class, () -> add("NewB=Point(L,0.5)"));

		publish(locus, original, List.of(other), "different-singleton");
		parent.compute();
		assertFalse(point.isDefined());
		assertEquals(retained, parent.getSemanticAddress());

		publish(locus, original, List.of(originalBranch), "recovered-original");
		parent.compute();
		assertTrue(point.isDefined());
		assertEquals(retained, parent.getSemanticAddress());
	}

	@Test
	void splineAndTransformSourcesUseTheirExactSemanticAddress() {
		add("A=(-2,0)");
		add("B=(-2/3,0)");
		add("C=(2/3,0)");
		add("D=(2,0)");
		GeoLocusV2 spline = add("S=SplineV2({A,B,C,D},3)");
		GeoPoint splinePoint = add("P=Point(S,0.5)");
		GeoLocusV2 transformed = add("T=Translate(S,(2,3))");
		GeoPoint transformedPoint = add("Q=Point(T,0.5)");
		assertTrue(splinePoint.isDefined());
		assertTrue(transformedPoint.isDefined());
		assertNotEquals(spline.getPersistentLocusId(),
				transformed.getPersistentLocusId());
		assertEquals(transformed.getPersistentLocusId(),
				semanticParent(transformedPoint).getSemanticAddress()
						.getSourceLocusId());
	}

	@Test
	void periodicSourceRetainsCanonicalAddressAndLift() {
		createPeriodicCircle();
		GeoPoint periodicPoint = add("R=Point(L,2*pi+pi/2)");

		assertTrue(periodicPoint.isDefined());
		assertEquals(1L, semanticParent(periodicPoint).getSemanticAddress()
				.getPeriodicLift());
	}

	@Test
	void saveReopenRenameAndUndoRedoRetainIdentityAndSelector() {
		createLine();
		activateUndo();
		getApp().storeUndoInfo();
		GeoPoint point = add("P=Point(L,0.5)");
		PersistentGeoId pointId = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point);
		String selector = semanticParent(point).getBranchInput().getTextString();
		getApp().storeUndoInfo();

		getKernel().undo();
		assertNull(lookup("P"));
		getKernel().redo();
		point = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		assertEquals(pointId, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point));
		assertEquals(selector, semanticParent(point).getBranchInput().getTextString());

		String xml = getApp().getXML();
		getApp().setXML(xml, true);
		point = assertInstanceOf(GeoPoint.class, requireLookup("P"));
		GeoLocusV2 locus = assertInstanceOf(GeoLocusV2.class, requireLookup("L"));
		assertTrue(point.isDefined());
		assertEquals(pointId, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point));
		assertEquals(selector, semanticParent(point).getBranchInput().getTextString());
		locus.setLabel("RenamedLocus");
		point.setLabel("RenamedPoint");
		assertTrue(point.isDefined());
		assertEquals(pointId, getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point));
	}

	@Test
	void copyRemapsSourceAndMalformedSelectorFailsClosed() {
		GeoLocusV2 locus = createLine();
		GeoPoint point = add("P=Point(L,0.5)");
		PersistentGeoId originalPointId = getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(point);
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(point), text -> text);
		paste(clipboard);

		GeoIdentityRecord copiedRecord = getConstruction()
				.getSpatialIdentityRegistry().getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance)
				.map(GeoIdentityRecord.class::cast)
				.filter(record -> originalPointId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoPoint copy = assertInstanceOf(GeoPoint.class, getConstruction()
				.getSpatialIdentityRegistry().getGeo(copiedRecord.getId()));
		assertNotEquals(locus.getPersistentLocusId(),
				semanticParent(copy).getSource().getPersistentLocusId());
		assertEquals(semanticParent(copy).getSource().getPersistentLocusId(),
				semanticParent(copy).getSemanticAddress().getSourceLocusId());

		GeoText selector = semanticParent(point).getBranchInput();
		selector.setTextString("geocedg-locus-principal-selection/v1|00");
		selector.updateCascade();
		assertFalse(point.isDefined());
	}

	private static AlgoSemanticLocusPoint2D semanticParent(GeoPoint point) {
		return assertInstanceOf(AlgoSemanticLocusPoint2D.class,
				point.getParentAlgorithm());
	}

	private GeoIdentityRecord pointRecord(GeoPoint point) {
		PersistentGeoId id = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point);
		return getConstruction().getSpatialIdentityRegistry().getGeoRecord(id);
	}

	private static LocusBranch2D branchLike(LocusBranch2D source, String key) {
		return new LocusBranch2D(key, source.getDeclaredDriverDomain(),
				source.getValidDomainComponents(), source.getOrientation(),
				key + "/semantic-v1", source.getLineage(), source.getProperties(),
				source.getQuality());
	}

	private static void publish(GeoLocusV2 locus, LocusDefinition2D template,
			List<LocusBranch2D> branches, String signature) {
		long revision = locus.getSemanticRevision() + 1;
		locus.publishSemanticDefinition(new LocusDefinition2D(
				locus.getLocusIdentity(), revision, DefinitionStatus.VALID,
				template.getProvider(), branches, template.getEvaluatorCapability(),
				template.getDeterminism(), "post-g9u1-a6/" + signature + "/"
						+ revision, locus.getInstrumentation()));
	}

	private void paste(String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), labels,
				clipboard.substring(separator));
	}
}
