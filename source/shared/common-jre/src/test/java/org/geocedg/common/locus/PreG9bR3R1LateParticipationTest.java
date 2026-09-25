/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessment;
import org.geocedg.common.kernel.spatial.identity.SpatialTokenSource;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3-R1 late-participation re-projection: when a geo on the projection
 * path of an already-participating record first acquires a durable identity,
 * the record's dependencies are re-projected under its own schema version inside
 * the same atomic publication. Identity, roles and both revisions are kept, no
 * migration occurs, and no redefine compatibility assessment is involved.
 */
class PreG9bR3R1LateParticipationTest extends PreG9bR3RedefineTestBase {

	@Test
	void aTraversedHelperThatBecomesDurableJoinsTheFrontierUnderTheSameIdentity()
			throws Exception {
		activateUndo();
		witnessCore("(0,0)", "(2,0)");
		GeoIdentityRecord before = record("E");
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				before.getSchemaVersion());
		assertEquals(ids("C"), before.getDependencies());
		assertNull(registry().getPersistentGeoId(requireLookup("f")));
		assertNull(registry().getPersistentGeoId(requireLookup("D")));
		Map<Object, SpatialIdentityRecord> recordsBefore = records(registry());
		getApp().storeUndoInfo();
		final String beforeXml = getApp().getXML();

		// The real semantic event: f becomes the target of a semantic intersection.
		List<SpatialRedefineAssessment> assessments = new ArrayList<>();
		AlgebraProcessor processor = getKernel().getAlgebraProcessor();
		processor.setSpatialRedefineAssessmentHandler(assessment -> {
			assessments.add(assessment);
			return null;
		});
		try {
			assertNotNull(add("h=Intersect(a,f)"));
		} finally {
			processor.setSpatialRedefineAssessmentHandler(null);
		}
		getApp().storeUndoInfo();

		assertTrue(assessments.isEmpty(), "late participation is not a redefine");
		GeoIdentityRecord reprojected = withDependencies(before, ids("C", "f"));
		assertEquals(reprojected, record("E"));
		Map<Object, SpatialIdentityRecord> recordsAfter = records(registry());
		Set<Object> added = new HashSet<>(recordsAfter.keySet());
		added.removeAll(recordsBefore.keySet());
		assertEquals(Set.of(id("f"), id("h")), added);
		for (Map.Entry<Object, SpatialIdentityRecord> entry : recordsBefore.entrySet()) {
			if (!entry.getKey().equals(before.getId())) {
				assertEquals(entry.getValue(), recordsAfter.get(entry.getKey()));
			}
		}
		final String afterXml = getApp().getXML();

		getKernel().undo();
		assertEquals(spatialGeoRecords(beforeXml), spatialGeoRecords(getApp().getXML()));
		assertEquals(before, record("E"));
		assertNull(registry().getPersistentGeoId(requireLookup("f")));
		assertNull(lookup("h"));
		getKernel().redo();
		assertEquals(spatialGeoRecords(afterXml), spatialGeoRecords(getApp().getXML()));
		assertEquals(reprojected, record("E"));

		AppCommon reopened = openInFreshApp(afterXml);
		assertEquals(spatialGeoRecords(afterXml), spatialGeoRecords(reopened.getXML()));
		SpatialIdentityRegistry reopenedRegistry =
				reopened.getKernel().getConstruction().getSpatialIdentityRegistry();
		assertEquals(reprojected, reopenedRegistry.getGeoRecord(reopenedRegistry
				.getPersistentGeoId(reopened.getKernel().lookupLabel("E"))));
	}

	@Test
	void aLatePublicationKeepsBothRevisionsAndARejectedOneRefreshesNothing() {
		SpatialIdentityRegistry registry = standaloneRegistry();
		getKernel().setContinuous(false);
		GeoElement c = add("C=(1,1)");
		GeoElement f = add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		GeoElement e = add("E=Midpoint(D,C)");
		GeoIdentityRecord durable = constructionRecord(registry, c, List.of());
		registry.registerConstructionParticipations(Map.of(c, durable));
		GeoIdentityRecord participant = constructionRecord(registry, e,
				List.of(durable.getId()));
		registry.registerConstructionParticipations(Map.of(e, participant));
		String before = registry.writeSpatialSection();

		// An invalid publication of the traversed helper is rejected as a whole.
		GeoIdentityRecord invalid = constructionRecord(registry, f,
				List.of(participant.getId()));
		assertThrows(SpatialIdentityException.class,
				() -> registry.registerConstructionParticipations(Map.of(f, invalid)));
		assertEquals(before, registry.writeSpatialSection());
		assertSame(participant, registry.getGeoRecord(participant.getId()));
		assertNull(registry.getPersistentGeoId(f));

		// A valid one re-projects the frontier and keeps both non-zero revisions.
		GeoIdentityRecord helper = constructionRecord(registry, f,
				List.of(durable.getId()));
		registry.registerConstructionParticipations(Map.of(f, helper));
		List<PersistentGeoId> frontier = new ArrayList<>(
				List.of(durable.getId(), helper.getId()));
		Collections.sort(frontier);
		GeoIdentityRecord reprojected = registry.getGeoRecord(participant.getId());
		assertEquals(withDependencies(participant, frontier), reprojected);
		assertEquals(7, reprojected.getDefinitionRevision());
		assertEquals(3, reprojected.getTopologyRevision());
		assertSame(e, registry.getGeo(participant.getId()));
	}

	@Test
	void aVersionOneRecordReprojectsOnlyItsDirectInputsAndIsNeverMigrated()
			throws Exception {
		loadMidpointFixture();
		GeoIdentityRecord historical = record("E");
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_DIRECT,
				historical.getSchemaVersion());
		assertEquals(ids("C"), historical.getDependencies());

		// A durable helper outside the direct inputs never enters a version-1 record.
		assertNotNull(add("h=Intersect(a,f)"));
		assertNotNull(registry().getPersistentGeoId(requireLookup("f")));
		assertEquals(historical, record("E"));

		// A newly durable direct input is re-projected under DIRECT, still version 1.
		assertNotNull(add("k=LocusV2(D,C)"));
		GeoIdentityRecord direct = withDependencies(historical, ids("C", "D"));
		assertEquals(direct, record("E"));

		AppCommon reopened = openInFreshApp(getApp().getXML());
		SpatialIdentityRegistry reopenedRegistry =
				reopened.getKernel().getConstruction().getSpatialIdentityRegistry();
		assertEquals(direct, reopenedRegistry.getGeoRecord(reopenedRegistry
				.getPersistentGeoId(reopened.getKernel().lookupLabel("E"))));
	}

	/** @return every identity record keyed by its identity */
	private static Map<Object, SpatialIdentityRecord> records(
			SpatialIdentityRegistry registry) {
		LinkedHashMap<Object, SpatialIdentityRecord> records = new LinkedHashMap<>();
		for (SpatialIdentityRecord record : registry.getRecords()) {
			records.put(record.getId(), record);
		}
		return records;
	}

	private static GeoIdentityRecord withDependencies(GeoIdentityRecord source,
			List<PersistentGeoId> dependencies) {
		return new GeoIdentityRecord(source.getId(), source.getProvider(),
				source.getFamily(), source.getSchemaId(), source.getSchemaVersion(),
				source.getAuthority(), source.getBindingRole(), source.getStableOutputRole(),
				source.getOutputCardinality(), dependencies, source.getDefinitionRevision(),
				source.getTopologyRevision(), source.getCopySourceId());
	}

	private static GeoIdentityRecord constructionRecord(SpatialIdentityRegistry registry,
			GeoElement geo, List<PersistentGeoId> dependencies) {
		return new GeoIdentityRecord(registry.allocatePersistentGeoId(),
				ConstructionGeoRedefineProvider.PROVIDER_ID,
				ConstructionGeoRedefineProvider.familyFor(geo),
				ConstructionGeoRedefineProvider.SCHEMA_ID,
				ConstructionGeoRedefineProvider.SCHEMA_VERSION,
				EditAuthorityMode.CONSTRUCTION_DEFINED,
				ProjectionBindingRole.NOT_APPLICABLE,
				ConstructionGeoRedefineProvider.STABLE_OUTPUT_ROLE, 1, dependencies, 7, 3);
	}

	private static SpatialIdentityRegistry standaloneRegistry() {
		return new SpatialIdentityRegistry(new SpatialTokenSource() {
			private int next = 1;

			@Override
			public String nextToken() {
				return String.format(Locale.ROOT, "%032x", next++);
			}
		}, 4);
	}
}
