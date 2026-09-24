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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.DurableDependencyProjection;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3 versioned durable-dependency projection and lazy migration: version
 * 1 stays the historical direct rule, version 2 is the transitive durable
 * frontier, and a record is only ever validated under its own version.
 */
class PreG9bR3DurableDependencyProjectionTest extends PreG9bR3RedefineTestBase {
	private static final DurableDependencyProjection DIRECT =
			DurableDependencyProjection.DIRECT;
	private static final DurableDependencyProjection FRONTIER =
			DurableDependencyProjection.TRANSITIVE_DURABLE_FRONTIER;

	@Test
	void projectionIsStructuralAndIgnoresLabelsCoordinatesAndRepeatedPaths() {
		variantWitness();
		GeoElement e = requireLookup("E");
		assertEquals(ids("C"), DIRECT.project(e, registry()::getPersistentGeoId));
		assertEquals(ids("C", "c"), FRONTIER.project(e, registry()::getPersistentGeoId));
		GeoIdentityRecord published = record("E");
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				published.getSchemaVersion());
		assertEquals(ids("C", "c"), published.getDependencies());
		List<GeoElement> helpers = FRONTIER.traversedHelpers(e,
				registry()::getPersistentGeoId);
		assertTrue(helpers.contains(requireLookup("D")));
		assertTrue(helpers.contains(requireLookup("f")));
		assertTrue(helpers.contains(requireLookup("P")));
		assertTrue(helpers.stream().noneMatch(registry()::isParticipating));
		assertTrue(DIRECT.traversedHelpers(e, registry()::getPersistentGeoId).isEmpty());
		assertNull(registry().getPersistentGeoId(requireLookup("D")));
		assertNull(registry().getPersistentGeoId(requireLookup("P")));

		// A diamond of identity-free paths reaches each durable ancestor once.
		add("h=Line(P,yAxis)");
		add("K=Intersect(f,h)");
		GeoElement diamond = add("R=Midpoint(D,K)");
		assertEquals(ids("c"), FRONTIER.project(diamond, registry()::getPersistentGeoId));
		GeoElement repeated = add("S=Midpoint(D,D)");
		assertEquals(ids("c"), FRONTIER.project(repeated, registry()::getPersistentGeoId));

		// Labels, coordinates and construction order are never evidence.
		List<PersistentGeoId> before = FRONTIER.project(e, registry()::getPersistentGeoId);
		requireLookup("D").rename("Dx");
		requireLookup("f").rename("fx");
		GeoPoint centre = (GeoPoint) requireLookup("A");
		centre.setCoords(0.75, -0.5, 1);
		centre.updateRepaint();
		assertEquals(before, FRONTIER.project(e, registry()::getPersistentGeoId));
		assertEquals(before, FRONTIER.project(e, registry()::getPersistentGeoId));
		assertEquals(published, record("E"));
	}

	@Test
	void projectionRuleIsNamedOnlyByAKnownSchemaVersion() {
		assertEquals(DIRECT, DurableDependencyProjection.forSchemaVersion(
				ConstructionGeoRedefineProvider.SCHEMA_VERSION_DIRECT));
		assertEquals(FRONTIER, DurableDependencyProjection.forSchemaVersion(
				ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER));
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				ConstructionGeoRedefineProvider.SCHEMA_VERSION);
		for (int unknown : new int[] {0, 3, -1}) {
			assertFalse(DurableDependencyProjection.isSupportedSchemaVersion(unknown));
			assertThrows(IllegalArgumentException.class,
					() -> DurableDependencyProjection.forSchemaVersion(unknown));
		}
	}

	@Test
	void historicalDirectRecordsLoadValidateAndSaveUnchanged() throws Exception {
		String fixture = loadMidpointFixture();
		List<String> historical = spatialGeoRecords(fixture);
		assertEquals(4, historical.size());
		assertEquals(4, constructionRecords().size());
		for (GeoIdentityRecord record : constructionRecords()) {
			assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_DIRECT,
					record.getSchemaVersion());
		}
		GeoElement e = requireLookup("E");
		assertEquals(ids("C"), record("E").getDependencies());
		assertEquals(DIRECT.project(e, registry()::getPersistentGeoId),
				record("E").getDependencies());
		assertEquals(historical, spatialGeoRecords(getApp().getXML()));

		// Ordinary reopen and save never migrate.
		String saved = getApp().getXML();
		getApp().getXMLio().processXMLString(saved, true, false, false);
		assertEquals(historical, spatialGeoRecords(getApp().getXML()));
		AppCommon reopened = openInFreshApp(getApp().getXML());
		assertEquals(historical, spatialGeoRecords(reopened.getXML()));

		// Neither does moving a free point nor an ordinary edit upstream.
		GeoPoint centre = (GeoPoint) requireLookup("A");
		centre.setCoords(0.25, 0.5, 1);
		centre.updateRepaint();
		assertEquals(historical, spatialGeoRecords(getApp().getXML()));
		Outcome ordinary = redefine("f", "Line(C,(1,3))", assessment -> null);
		assertTrue(ordinary.succeeded(), ordinary.errors());
		assertNull(ordinary.assessment());
		assertEquals(historical, spatialGeoRecords(getApp().getXML()));
		assertTrue(requireLookup("a").isDefined());
	}

	@Test
	void eachRecordIsValidatedOnlyUnderItsOwnSchemaVersion() throws Exception {
		variantWitness();
		String current = getApp().getXML();
		PersistentGeoId e = id("E");
		String directSet = externalForms(ids("C"));
		assertNotEquals(directSet, externalForms(record("E").getDependencies()));

		// A version-1 record with the direct set is valid history and stays v1.
		String historical = withRecordAttribute(withRecordAttribute(current, e,
				"schemaVersion", "1"), e, "dependencies", directSet);
		AppCommon opened = openInFreshApp(historical);
		SpatialIdentityRegistry openedRegistry = opened.getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		GeoIdentityRecord loaded = openedRegistry.getGeoRecord(e);
		assertNotNull(loaded);
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_DIRECT,
				loaded.getSchemaVersion());
		assertEquals(ids("C"), loaded.getDependencies());
		assertEquals(spatialGeoRecords(historical), spatialGeoRecords(opened.getXML()));

		// The frontier under a version-1 label and the direct set under a version-2
		// label are both malformed: neither rule is ever read as the other.
		assertLoadRejected(withRecordAttribute(current, e, "schemaVersion", "1"),
				SpatialIdentityDiagnostic.Code.MALFORMED_RECORD);
		assertLoadRejected(withRecordAttribute(current, e, "dependencies", directSet),
				SpatialIdentityDiagnostic.Code.MALFORMED_RECORD);
		// A future rule is rejected cleanly instead of being downgraded.
		assertLoadRejected(withRecordAttribute(current, e, "schemaVersion", "3"),
				SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION);
	}

	@Test
	void anExplicitRedefineMigratesOneHistoricalRecordAtomicallyEvenWithAnEqualSet()
			throws Exception {
		activateUndo();
		loadMidpointFixture();
		getApp().storeUndoInfo();
		final String historicalXml = getApp().getXML();
		GeoIdentityRecord before = record("E");
		GeoElement e = requireLookup("E");
		assertEquals(DIRECT.project(e, registry()::getPersistentGeoId),
				FRONTIER.project(e, registry()::getPersistentGeoId));

		Outcome identical = redefine("E", "Midpoint(D,C)",
				PreG9bR3RedefineTestBase::retainWhenAvailable);
		assertEquals(SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				identical.status());
		assertTrue(identical.succeeded(), identical.errors());
		GeoIdentityRecord upgraded = record("E");
		assertEquals(before.getId(), upgraded.getId());
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				upgraded.getSchemaVersion());
		assertEquals(before.getDependencies(), upgraded.getDependencies());
		assertEquals(before.getDefinitionRevision(), upgraded.getDefinitionRevision());
		assertEquals(before.getTopologyRevision(), upgraded.getTopologyRevision());
		for (String untouched : List.of("C", "c", "a")) {
			assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_DIRECT,
					record(untouched).getSchemaVersion(), untouched);
		}
		String mixed = getApp().getXML();

		getKernel().undo();
		assertEquals(spatialGeoRecords(historicalXml), spatialGeoRecords(getApp().getXML()));
		getKernel().redo();
		assertEquals(spatialGeoRecords(mixed), spatialGeoRecords(getApp().getXML()));

		// The mixed document reopens with every record under its own rule.
		AppCommon reopened = openInFreshApp(mixed);
		assertEquals(spatialGeoRecords(mixed), spatialGeoRecords(reopened.getXML()));

		Outcome changed = redefine("E", "Midpoint(D,C+(1,0))",
				PreG9bR3RedefineTestBase::retainWhenAvailable);
		assertTrue(changed.succeeded(), changed.errors());
		GeoIdentityRecord definitionChange = record("E");
		assertEquals(before.getId(), definitionChange.getId());
		assertEquals(before.getDefinitionRevision() + 1,
				definitionChange.getDefinitionRevision());
		assertEquals(before.getTopologyRevision(), definitionChange.getTopologyRevision());
		assertEquals(ids("C"), definitionChange.getDependencies());
	}

	@Test
	void aDefinitionChangeMigratesTheHistoricalRecordInTheSameEvent() throws Exception {
		loadMidpointFixture();
		GeoIdentityRecord before = record("E");

		Outcome changed = redefine("E", "Midpoint(D,C+(1,0))",
				PreG9bR3RedefineTestBase::retainWhenAvailable);
		assertEquals(SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				changed.status());
		assertTrue(changed.succeeded(), changed.errors());
		GeoIdentityRecord after = record("E");
		assertEquals(before.getId(), after.getId());
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				after.getSchemaVersion());
		assertEquals(before.getDefinitionRevision() + 1, after.getDefinitionRevision());
		assertEquals(before.getTopologyRevision(), after.getTopologyRevision());
		assertEquals(before.getDependencies(), after.getDependencies());
		assertTrue(requireLookup("a").isDefined());
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_DIRECT,
				record("a").getSchemaVersion());
	}

	@Test
	void aRejectedOrCancelledEventLeavesTheHistoricalRecordUntouched() throws Exception {
		loadMidpointFixture();
		String historical = getApp().getXML();
		GeoIdentityRecord before = record("E");

		Outcome cancelled = redefine("E", "Midpoint(D,C+(1,0))", assessment -> null);
		assertEquals(SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				cancelled.status());
		assertEquals(historical, getApp().getXML());

		Outcome family = redefine("E", "Circle(D,1)",
				always(SpatialRedefineExecutionMode.ADVANCED_RETAIN));
		assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
				family.status());
		assertEquals(historical, getApp().getXML());

		Outcome cycle = redefine("E", "Midpoint(D,Point(a,\"" + BRANCH + "\",0.5))",
				always(SpatialRedefineExecutionMode.ADVANCED_RETAIN));
		assertEquals(SpatialRedefineAssessmentStatus.INVALID_DAG, cycle.status());
		assertEquals(historical, getApp().getXML());
		assertEquals(before, record("E"));
	}

	@Test
	void copiesAndRecreationsPublishVersionTwoWithAFreshCoherentFrontier()
			throws Exception {
		loadMidpointFixture();
		Set<PersistentGeoId> original = new TreeSet<>();
		for (GeoIdentityRecord record : constructionRecords()) {
			original.add(record.getId());
		}
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				new ArrayList<>(getConstruction().getGeoSetConstructionOrder()),
				text -> text);
		paste(clipboard);
		List<GeoIdentityRecord> copies = new ArrayList<>();
		for (GeoIdentityRecord record : constructionRecords()) {
			if (!original.contains(record.getId())) {
				copies.add(record);
			}
		}
		assertEquals(original.size(), copies.size());
		for (GeoIdentityRecord copy : copies) {
			assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
					copy.getSchemaVersion());
			assertTrue(original.contains(copy.getCopySourceId()));
			GeoElement geo = registry().getGeo(copy.getId());
			assertNotNull(geo);
			assertEquals(FRONTIER.project(geo, registry()::getPersistentGeoId),
					copy.getDependencies());
			assertTrue(copy.getDependencies().stream().noneMatch(original::contains),
					"a copy never depends on the historical source identities");
		}
		for (PersistentGeoId id : original) {
			assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_DIRECT,
					record(id).getSchemaVersion());
		}
		assertEquals(spatialGeoRecords(getApp().getXML()),
				spatialGeoRecords(openInFreshApp(getApp().getXML()).getXML()));

		// Deleting and recreating the participant publishes a new identity.
		PersistentGeoId deleted = id("E");
		requireLookup("E").remove();
		assertNull(registry().getGeo(deleted));
		add("E=Midpoint(D,C)");
		GeoLocusV2 recreated = add("a=LocusV2(E,C)");
		assertNotNull(recreated);
		assertNotEquals(deleted, id("E"));
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				record("E").getSchemaVersion());
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				record("a").getSchemaVersion());
		assertEquals(FRONTIER.project(requireLookup("E"), registry()::getPersistentGeoId),
				record("E").getDependencies());
		assertEquals(spatialGeoRecords(getApp().getXML()),
				spatialGeoRecords(openInFreshApp(getApp().getXML()).getXML()));
	}

	/** The author witness with the direct and transitive projections different. */
	private void variantWitness() {
		getKernel().setContinuous(false);
		add("A=(0,0)");
		add("B=(2,0)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("P=Point(c)");
		add("f=Line(P,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("E=Midpoint(D,C)");
		assertNotNull(add("a=LocusV2(E,C)"));
	}

	private void paste(String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(List.of(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), labels,
				clipboard.substring(separator));
	}
}
