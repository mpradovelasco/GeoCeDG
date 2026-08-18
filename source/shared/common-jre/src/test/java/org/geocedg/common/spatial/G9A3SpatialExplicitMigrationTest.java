/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.ProjectionDiagramMapId;
import org.geocedg.common.kernel.spatial.identity.ProjectionDiagramMapRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameId;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameUseRole;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialCopyPlan;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityInstrumentation;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleOperationKind;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleTransaction;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialPointLifecycleService;
import org.geocedg.common.kernel.spatial.identity.SpatialPointMigrationPlan;
import org.geocedg.common.kernel.spatial.identity.SpatialPointPilotRedefineProvider;
import org.geocedg.common.kernel.spatial.runtime.SpatialPointPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialSemanticRuntime;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.junit.jupiter.api.Test;

class G9A3SpatialExplicitMigrationTest extends BaseUnitTest {
	private static final String MIGRATION_PROVENANCE =
			"g9a3.explicit-association.test";
	private static final String UNIT = "model-unit";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D(new AppConfigGeoCeDG());
	}

	@Test
	void mig01CompleteExplicitPointAssociationPersistsTypedProvenance()
			throws Exception {
		MigrationFixture fixture = MigrationFixture.create(getConstruction(), this::add,
				"G9A3M1");
		SpatialLifecycleTransaction migration = lifecycle().prepareMigration(
				fixture.plan(MIGRATION_PROVENANCE));

		assertEquals(SpatialLifecycleOperationKind.EXPLICIT_MIGRATION,
				migration.getMutation().getOperationKind());
		assertEquals(MIGRATION_PROVENANCE,
				migration.getMutation().getProvenanceToken());
		migration.commit();

		assertEquals(SpatialLifecycleTransaction.State.COMMITTED,
				migration.getState());
		assertEquals(fixture.records.size(), registry().size());
		assertEquals(1,
				registry().getInstrumentation().getExplicitMigrationCommits());
		assertAttachedExactly(fixture);
		SpatialObjectRecord object = object(fixture.objectId);
		assertEquals(SpatialObjectRecord.EXPLICIT_ASSOCIATION,
				object.getAssociationProvenance());
		assertValidPoint(semanticRuntime(), fixture.objectId);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
		String xml = getApp().getXML();

		getApp().getXMLio().processXMLString(xml, true, false, false);

		assertEquals(before, snapshot());
		assertEquals(xml, getApp().getXML());
		assertEquals(SpatialObjectRecord.EXPLICIT_ASSOCIATION,
				object(fixture.objectId).getAssociationProvenance());
		for (PersistentGeoId id : fixture.geos.keySet()) {
			GeoElement restored = registry().getGeo(id);
			assertNotNull(restored);
			assertEquals(id, registry().getPersistentGeoId(restored));
		}
		assertValidPoint(semanticRuntime(), fixture.objectId);
	}

	@Test
	void mig02NoRequestNeverAssociatesByLabelsCoordinatesLayersOrOrder()
			throws Exception {
		String legacy = Files.readString(Paths.get(
				"src/test/resources/org/geocedg/common/spatial/g9a3",
				"legacy-adversarial-coincident.xml"), StandardCharsets.UTF_8);
		getApp().getXMLio().processXMLString(legacy, true, false, false);

		for (String label : List.of("HorizontalProjection", "VerticalProjection",
				"P_h", "P_v")) {
			GeoElement geo = lookup(label);
			assertNotNull(geo);
			assertNull(registry().getPersistentGeoId(geo));
		}
		assertTrue(registry().isEmpty());
		assertNoInference(registry(), semanticRuntime());
		String saved = getApp().getXML();
		getApp().getXMLio().processXMLString(saved, true, false, false);
		assertEquals(saved, getApp().getXML());
		assertTrue(registry().isEmpty());
		assertNoInference(registry(), semanticRuntime());
	}

	@Test
	void mig03IncompleteOrAmbiguousAssociationRejectsWithoutMutation() {
		MigrationFixture fixture = MigrationFixture.create(getConstruction(), this::add,
				"G9A3M3");
		GeoElement selected = fixture.geos.values().iterator().next();
		PersistentGeoId firstId = registry().allocatePersistentGeoId();
		PersistentGeoId secondId = registry().allocatePersistentGeoId();
		SpatialPointMigrationPlan.Builder ambiguous = SpatialPointMigrationPlan.builder(
				"g9a3.mig03.ambiguous").attach(selected,
						MigrationFixture.geoRecord(firstId, "INPUT"));
		assertThrows(IllegalArgumentException.class,
				() -> ambiguous.attach(selected,
						MigrationFixture.geoRecord(secondId, "INPUT")));
		assertTrue(registry().isEmpty());

		PersistentGeoId incompleteId = registry().allocatePersistentGeoId();
		SpatialPointMigrationPlan incomplete = SpatialPointMigrationPlan.builder(
				"g9a3.mig03.incomplete").attach(selected,
						MigrationFixture.geoRecord(incompleteId, "INPUT")).build();
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
		String xml = getApp().getXML();

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMigration(incomplete));

		assertEquals(SpatialIdentityDiagnostic.Code.MIGRATION_INCOMPLETE,
				failure.getDiagnostic().getCode());
		assertEquals(before, snapshot());
		assertEquals(xml, getApp().getXML());
		assertEquals(0,
				registry().getInstrumentation().getExplicitMigrationCommits());
	}

	@Test
	void mig04StaleForeignAndDeletedHandlesRejectAtomically() throws Exception {
		assertStaleHandleRejected();
		resetConstruction();
		assertForeignHandleRejected();
		resetConstruction();
		assertDeletedHandleRejected();
	}

	@Test
	void mig05RepeatedAssociationRequestCannotDuplicateTheGraph() {
		MigrationFixture fixture = MigrationFixture.create(getConstruction(), this::add,
				"G9A3M5");
		SpatialPointMigrationPlan plan = fixture.plan("g9a3.mig05.repeat");
		lifecycle().prepareMigration(plan).commit();
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
		String xml = getApp().getXML();
		int recordCount = registry().size();

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMigration(plan));

		assertEquals(SpatialIdentityDiagnostic.Code.MIGRATION_ALREADY_ASSOCIATED,
				failure.getDiagnostic().getCode());
		assertEquals(before, snapshot());
		assertEquals(xml, getApp().getXML());
		assertEquals(recordCount, registry().size());
		assertEquals(1,
				registry().getInstrumentation().getExplicitMigrationCommits());
		assertTrue(registry().getInstrumentation().getExplicitMigrationRollbacks() >= 1);
	}

	@Test
	void mig06MigratedCopySurvivesClassicReopenWithoutFeatureCreation()
			throws Exception {
		MigrationFixture fixture = MigrationFixture.create(getConstruction(), this::add,
				"G9A3M6");
		lifecycle().prepareMigration(fixture.plan("g9a3.mig06.associate")).commit();
		Map<GeoElement, GeoElement> geoCopies = fixture.createGeoCopies(this::add,
				"G9A3M6Copy");
		lifecycle().prepareCopy(SpatialCopyPlan.completeClosure(registry(), registry(),
				geoCopies, "g9a3.mig06.copy")).commit();

		assertEquals(fixture.records.size() * 2, registry().size());
		SpatialObjectRecord copiedObject = copiedObject(fixture.objectId);
		assertEquals(SpatialObjectRecord.EXPLICIT_ASSOCIATION,
				copiedObject.getAssociationProvenance());
		assertNotEquals(fixture.objectId, copiedObject.getId());
		assertValidPoint(semanticRuntime(), fixture.objectId);
		assertValidPoint(semanticRuntime(), copiedObject.getId());
		G9A3SpatialGraphSnapshot.Snapshot expected = snapshot();
		String saved = getApp().getXML();

		AppCommon classic = AppCommonFactory.create3D(new AppConfigDefault());
		classic.getXMLio().processXMLString(saved, true, false, false);
		Construction classicConstruction = classic.getKernel().getConstruction();

		assertEquals(expected,
				G9A3SpatialGraphSnapshot.capture(classicConstruction));
		assertEquals(saved, classic.getXML());
		assertEquals(SpatialObjectRecord.EXPLICIT_ASSOCIATION,
				((SpatialObjectRecord) classicConstruction.getSpatialIdentityRegistry()
						.getRecord(fixture.objectId)).getAssociationProvenance());
		assertValidPoint(classicConstruction.getSpatialSemanticRuntime(),
				fixture.objectId);
		assertValidPoint(classicConstruction.getSpatialSemanticRuntime(),
				copiedObject.getId());
		assertThrows(ClassNotFoundException.class, () -> Class.forName(
				"org.geocedg.common.kernel.spatial.commands.CmdSpatialPoint"));
	}

	private void assertStaleHandleRejected() throws Exception {
		GeoElement stale = add("G9A3Stale=(0,0,0)");
		String sourceXml = getApp().getXML();
		getApp().getXMLio().processXMLString(sourceXml, true, false, false);
		assertNotSame(stale, lookup("G9A3Stale"));
		MigrationFixture fixture = MigrationFixture.create(getConstruction(), this::add,
				"G9A3M4Stale");
		GeoElement target = fixture.geos.values().iterator().next();
		assertMigrationFailurePreservesState(
				fixture.planReplacing(target, stale, "g9a3.mig04.stale"),
				SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE);
	}

	private void assertForeignHandleRejected() {
		MigrationFixture fixture = MigrationFixture.create(getConstruction(), this::add,
				"G9A3M4Foreign");
		AppCommon foreignApp = AppCommonFactory.create3D(new AppConfigDefault());
		GeoElement foreign = (GeoElement) foreignApp.getKernel().getAlgebraProcessor()
				.processAlgebraCommand("G9A3Foreign=(0,0,0)", false)[0];
		GeoElement target = fixture.geos.values().iterator().next();
		assertMigrationFailurePreservesState(
				fixture.planReplacing(target, foreign, "g9a3.mig04.foreign"),
				SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING);
	}

	private void assertDeletedHandleRejected() {
		MigrationFixture fixture = MigrationFixture.create(getConstruction(), this::add,
				"G9A3M4Deleted");
		SpatialPointMigrationPlan plan = fixture.plan("g9a3.mig04.deleted");
		GeoElement deleted = fixture.geos.values().iterator().next();
		deleted.remove();
		assertMigrationFailurePreservesState(plan,
				SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE);
	}

	private void assertMigrationFailurePreservesState(SpatialPointMigrationPlan plan,
			SpatialIdentityDiagnostic.Code expectedCode) {
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
		String xml = getApp().getXML();
		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMigration(plan));
		assertEquals(expectedCode, failure.getDiagnostic().getCode());
		assertEquals(before, snapshot());
		assertEquals(xml, getApp().getXML());
		assertEquals(0,
				registry().getInstrumentation().getExplicitMigrationCommits());
	}

	private void resetConstruction() throws Exception {
		getApp().getXMLio().processXMLString(
				"<geogebra format=\"5.0\"><construction/></geogebra>",
				true, false, false);
		assertTrue(registry().isEmpty());
	}

	private void assertAttachedExactly(MigrationFixture fixture) {
		for (Map.Entry<PersistentGeoId, GeoElement> attachment
				: fixture.geos.entrySet()) {
			assertEquals(attachment.getKey(),
					registry().getPersistentGeoId(attachment.getValue()));
			assertEquals(attachment.getValue(), registry().getGeo(attachment.getKey()));
		}
	}

	private SpatialObjectRecord object(SpatialObjectId id) {
		return (SpatialObjectRecord) registry().getRecord(id);
	}

	private SpatialObjectRecord copiedObject(SpatialObjectId sourceId) {
		for (SpatialIdentityRecord record : registry().getRecords()) {
			if (record instanceof SpatialObjectRecord
					&& sourceId.equals(record.getCopySourceId())) {
				return (SpatialObjectRecord) record;
			}
		}
		throw new AssertionError("Copied spatial object was not published");
	}

	private G9A3SpatialGraphSnapshot.Snapshot snapshot() {
		return G9A3SpatialGraphSnapshot.capture(getConstruction());
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private SpatialSemanticRuntime semanticRuntime() {
		return getConstruction().getSpatialSemanticRuntime();
	}

	private SpatialPointLifecycleService lifecycle() {
		return new SpatialPointLifecycleService(registry());
	}

	private static void assertValidPoint(SpatialSemanticRuntime runtime,
			SpatialObjectId objectId) {
		SpatialPointPilotCertificate certificate =
				runtime.getSpatialPointCertificate(objectId);
		assertNotNull(certificate);
		assertTrue(certificate.isCurrentRevision());
		assertEquals(SpatialCertificateStatus.VALID,
				certificate.getSemanticCertificate().getStatus());
		Vector3 point = certificate.getSemanticCertificate().getPoint().orElseThrow();
		assertEquals(2, point.getX(), 1e-9);
		assertEquals(3, point.getY(), 1e-9);
		assertEquals(5, point.getZ(), 1e-9);
		assertTrue(runtime.getDerivedPoint(objectId).isDefined());
	}

	private static void assertNoInference(SpatialIdentityRegistry registry,
			SpatialSemanticRuntime runtime) {
		SpatialIdentityInstrumentation identity = registry.getInstrumentation();
		assertEquals(0, identity.getAllocationAttempts());
		assertEquals(0, identity.getExplicitMigrationCommits());
		assertEquals(0, identity.getLabelAuthorityUses());
		assertEquals(0, identity.getCoordinateAuthorityUses());
		assertEquals(0, identity.getConstructionOrderAuthorityUses());
		assertEquals(0, identity.getXmlPositionAuthorityUses());
		SpatialSemanticInstrumentation semantic = runtime.getInstrumentation();
		assertEquals(0, semantic.getLabelFallbackLookups());
		assertEquals(0, semantic.getCoordinateAssociationAttempts());
		assertEquals(0, semantic.getCreationOrderAssociationAttempts());
		assertEquals(0, semantic.getXmlPositionAssociationAttempts());
		assertEquals(0, semantic.getLayerOrVisibilityReads());
	}

	@FunctionalInterface
	private interface GeoCreator {
		GeoElement create(String definition);
	}

	private static final class MigrationFixture {
		private final List<SpatialIdentityRecord> records;
		private final LinkedHashMap<PersistentGeoId, GeoElement> geos;
		private final SpatialObjectId objectId;

		private MigrationFixture(List<SpatialIdentityRecord> records,
				LinkedHashMap<PersistentGeoId, GeoElement> geos,
				SpatialObjectId objectId) {
			this.records = records;
			this.geos = geos;
			this.objectId = objectId;
		}

		static MigrationFixture create(Construction construction, GeoCreator create,
				String prefix) {
			SpatialIdentityRegistry registry = construction.getSpatialIdentityRegistry();
			GeoElement origin = create.create(prefix + "O=(0,0,0)");
			GeoElement u = create.create(prefix
					+ "U=Vector((0,0,0),(1,0,0))");
			GeoElement horizontalV = create.create(prefix
					+ "H=Vector((0,0,0),(0,1,0))");
			GeoElement verticalV = create.create(prefix
					+ "V=Vector((0,0,0),(0,0,1))");
			GeoElement one = create.create(prefix + "One=1");
			GeoElement zero = create.create(prefix + "Zero=0");
			GeoElement horizontalPoint = create.create(prefix + "PH=(2,3)");
			GeoElement verticalPoint = create.create(prefix + "PV=(2,5)");

			LinkedHashMap<PersistentGeoId, GeoElement> geos = new LinkedHashMap<>();
			PersistentGeoId originId = reserve(registry, geos, origin);
			PersistentGeoId uId = reserve(registry, geos, u);
			PersistentGeoId horizontalVId = reserve(registry, geos, horizontalV);
			PersistentGeoId verticalVId = reserve(registry, geos, verticalV);
			PersistentGeoId oneId = reserve(registry, geos, one);
			PersistentGeoId zeroId = reserve(registry, geos, zero);
			PersistentGeoId horizontalPointId = reserve(registry, geos, horizontalPoint);
			PersistentGeoId verticalPointId = reserve(registry, geos, verticalPoint);
			ProjectionFrameId horizontalFrameId =
					registry.allocateProjectionFrameId();
			ProjectionFrameId verticalFrameId = registry.allocateProjectionFrameId();
			ProjectionSystemId systemId = registry.allocateProjectionSystemId();
			ProjectionDiagramMapId horizontalMapId =
					registry.allocateProjectionDiagramMapId();
			ProjectionDiagramMapId verticalMapId =
					registry.allocateProjectionDiagramMapId();
			SpatialObjectId objectId = registry.allocateSpatialObjectId();
			ProjectionBindingId horizontalBindingId =
					registry.allocateProjectionBindingId();
			final ProjectionBindingId verticalBindingId =
					registry.allocateProjectionBindingId();

			List<SpatialIdentityRecord> records = new ArrayList<>();
			for (PersistentGeoId id : geos.keySet()) {
				String outputRole = id.equals(horizontalPointId)
						|| id.equals(verticalPointId) ? "PROJECTED_POINT" : "INPUT";
				records.add(geoRecord(id, outputRole));
			}
			records.add(new ProjectionFrameRecord(horizontalFrameId, 2, originId, uId,
					horizontalVId, "ORTHOGRAPHIC", UNIT, "RIGHT_HANDED", "EXACT", 0));
			records.add(new ProjectionFrameRecord(verticalFrameId, 2, originId, uId,
					verticalVId, "ORTHOGRAPHIC", UNIT, "RIGHT_HANDED", "EXACT", 0));
			records.add(new ProjectionSystemRecord(systemId, 2,
					List.of(horizontalMapId, verticalMapId), List.of(), UNIT,
					1e-9, 1e-9, 1e-12, 1e-9, 1e-9, 1e10, 0));
			records.add(map(horizontalMapId, systemId, horizontalFrameId,
					oneId, zeroId));
			records.add(map(verticalMapId, systemId, verticalFrameId, oneId, zeroId));
			records.add(binding(horizontalBindingId, objectId, systemId,
					horizontalMapId, horizontalFrameId, horizontalPointId));
			records.add(binding(verticalBindingId, objectId, systemId,
					verticalMapId, verticalFrameId, verticalPointId));
			records.add(new SpatialObjectRecord(objectId, 2,
					SpatialObjectRecord.POINT_TYPE,
					EditAuthorityMode.PROJECTION_DEFINED,
					SpatialObjectRecord.POINT_SCHEMA_ID,
					SpatialObjectRecord.POINT_SCHEMA_VERSION, systemId,
					List.of(horizontalBindingId, verticalBindingId), 0, 0));
			return new MigrationFixture(records, geos, objectId);
		}

		SpatialPointMigrationPlan plan(String provenance) {
			return planReplacing(null, null, provenance);
		}

		SpatialPointMigrationPlan planReplacing(GeoElement source,
				GeoElement replacement, String provenance) {
			SpatialPointMigrationPlan.Builder builder =
					SpatialPointMigrationPlan.builder(provenance);
			for (SpatialIdentityRecord record : records) {
				if (record instanceof GeoIdentityRecord) {
					GeoElement geo = geos.get(record.getId());
					builder.attach(geo == source ? replacement : geo,
							(GeoIdentityRecord) record);
				} else {
					builder.record(record);
				}
			}
			return builder.build();
		}

		Map<GeoElement, GeoElement> createGeoCopies(GeoCreator create,
				String prefix) {
			IdentityHashMap<GeoElement, GeoElement> result = new IdentityHashMap<>();
			List<GeoElement> source = new ArrayList<>(geos.values());
			List<GeoElement> copies = List.of(
					create.create(prefix + "O=(0,0,0)"),
					create.create(prefix + "U=Vector((0,0,0),(1,0,0))"),
					create.create(prefix + "H=Vector((0,0,0),(0,1,0))"),
					create.create(prefix + "V=Vector((0,0,0),(0,0,1))"),
					create.create(prefix + "One=1"),
					create.create(prefix + "Zero=0"),
					create.create(prefix + "PH=(2,3)"),
					create.create(prefix + "PV=(2,5)"));
			for (int index = 0; index < source.size(); index++) {
				result.put(source.get(index), copies.get(index));
			}
			return result;
		}

		static GeoIdentityRecord geoRecord(PersistentGeoId id, String outputRole) {
			String family = "PROJECTED_POINT".equals(outputRole)
					? SpatialObjectRecord.POINT_TYPE : "SEMANTIC_INPUT";
			return new GeoIdentityRecord(id,
					SpatialPointPilotRedefineProvider.PROVIDER_ID, family,
					SpatialObjectRecord.POINT_SCHEMA_ID,
					SpatialObjectRecord.POINT_SCHEMA_VERSION,
					EditAuthorityMode.PROJECTION_DEFINED,
					ProjectionBindingRole.DEFINING, outputRole, 1, 0, 0);
		}

		private static PersistentGeoId reserve(SpatialIdentityRegistry registry,
				Map<PersistentGeoId, GeoElement> geos, GeoElement geo) {
			PersistentGeoId id = registry.allocatePersistentGeoId();
			geos.put(id, geo);
			return id;
		}

		private static ProjectionDiagramMapRecord map(ProjectionDiagramMapId id,
				ProjectionSystemId systemId, ProjectionFrameId frameId,
				PersistentGeoId oneId, PersistentGeoId zeroId) {
			return new ProjectionDiagramMapRecord(id, 2, systemId, frameId,
					ProjectionFrameUseRole.DEFINING, "ORIENTED_ISOMETRY",
					"PRESERVING", UNIT, "EXACT", oneId, zeroId, zeroId, oneId,
					zeroId, zeroId, oneId, List.of(), 0);
		}

		private static ProjectionBindingRecord binding(ProjectionBindingId id,
				SpatialObjectId objectId, ProjectionSystemId systemId,
				ProjectionDiagramMapId mapId, ProjectionFrameId frameId,
				PersistentGeoId projectedPointId) {
			return new ProjectionBindingRecord(id, 2, objectId, systemId, mapId,
					frameId, ProjectionBindingRole.DEFINING,
					SpatialObjectRecord.POINT_TYPE, SpatialObjectRecord.POINT_TYPE,
					SpatialObjectRecord.POINT_SCHEMA_ID,
					SpatialObjectRecord.POINT_SCHEMA_VERSION, projectedPointId,
					"EXACT", "NOT_REQUIRED", 0);
		}
	}
}
