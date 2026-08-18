/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
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
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRelationId;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRelationRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameUseRole;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleMutation;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleOperationKind;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleTransaction;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialPointLifecycleService;
import org.geocedg.common.kernel.spatial.runtime.ProjectionSystemPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialPointPilotCertificate;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.gui.view.algebra.EvalInfoFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/** Canonical G9A3 LIFE01--LIFE10 mutation scenarios. */
class G9A3SpatialMutationLifecycleTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D(new AppConfigGeoCeDG());
	}

	@Test
	void life01AddSecondDefiningBindingRestoresValidWithFreshIdentity() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		ProjectionBindingId retired = graph.verticalBindingId;
		ProjectionBindingRecord source = binding(retired);
		removeBinding(graph, retired, "G9A3-LIFE01-remove");
		assertStatus(graph, SpatialCertificateStatus.UNDERDETERMINED, false);

		ProjectionBindingId fresh = registry().allocateProjectionBindingId();
		ProjectionBindingRecord restored = source.asFreshReroled(fresh,
				ProjectionBindingRole.DEFINING);
		addBinding(graph, restored, "G9A3-LIFE01-add");

		assertNotEquals(retired, fresh);
		assertNull(registry().getRecord(retired));
		assertNotNull(registry().getRecord(graph.objectId));
		assertNotNull(registry().getRecord(graph.systemId));
		assertStatus(graph, SpatialCertificateStatus.VALID, true);
	}

	@Test
	void life02RemoveDefiningBindingRetiresOnlyBindingAndWithdrawsPayload() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		SpatialObjectRecord before = object(graph);

		removeBinding(graph, graph.verticalBindingId, "G9A3-LIFE02");

		SpatialObjectRecord after = object(graph);
		assertNull(registry().getRecord(graph.verticalBindingId));
		assertEquals(before.getId(), after.getId());
		assertEquals(before.getDefinitionRevision() + 1,
				after.getDefinitionRevision());
		assertEquals(before.getTopologyRevision() + 1,
				after.getTopologyRevision());
		assertNotNull(registry().getRecord(graph.systemId));
		assertStatus(graph, SpatialCertificateStatus.UNDERDETERMINED, false);
	}

	@Test
	void life03ReroleRoundTripIsFreshAtomicAndRevisioned() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		PersistentGeoId verticalPointId = binding(graph.verticalBindingId)
				.getProjectedPointGeoId();

		ProjectionBindingId presentation = rerole(graph, graph.verticalBindingId,
				ProjectionBindingRole.PRESENTATION, "G9A3-LIFE03-presentation");
		GeoIdentityRecord presentationGeo = registry().getGeoRecord(
				verticalPointId);
		assertNotNull(presentationGeo);
		assertEquals(ProjectionBindingRole.PRESENTATION,
				presentationGeo.getBindingRole());
		assertEquals(1, presentationGeo.getDefinitionRevision());
		assertEquals(1, presentationGeo.getTopologyRevision());
		assertStatus(graph, SpatialCertificateStatus.UNDERDETERMINED, false);
		ProjectionBindingId defining = rerole(graph, presentation,
				ProjectionBindingRole.DEFINING, "G9A3-LIFE03-defining");
		GeoIdentityRecord definingGeo = registry().getGeoRecord(verticalPointId);
		assertNotNull(definingGeo);

		assertNotEquals(graph.verticalBindingId, presentation);
		assertNotEquals(presentation, defining);
		assertNull(registry().getRecord(graph.verticalBindingId));
		assertNull(registry().getRecord(presentation));
		assertEquals(2, object(graph).getDefinitionRevision());
		assertEquals(2, object(graph).getTopologyRevision());
		assertEquals(ProjectionBindingRole.DEFINING, definingGeo.getBindingRole());
		assertEquals(2, definingGeo.getDefinitionRevision());
		assertEquals(2, definingGeo.getTopologyRevision());
		assertStatus(graph, SpatialCertificateStatus.VALID, true);
	}

	@Test
	void life04MixedDerivedAuthorityRejectsWithoutGraphOrRuntimeMutation() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		GeoIdentityRecord current = (GeoIdentityRecord) registry().getRecord(
				graph.horizontalPointId);
		GeoIdentityRecord incompatible = new GeoIdentityRecord(current.getId(),
				current.getProvider(), current.getFamily(), current.getSchemaId(),
				current.getSchemaVersion(), current.getAuthority(),
				ProjectionBindingRole.DERIVED, current.getStableOutputRole(),
				current.getOutputCardinality(), current.getDefinitionRevision() + 1,
				current.getTopologyRevision() + 1, current.getCopySourceId());
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMutation(SpatialLifecycleMutation.builder(
						SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE,
						"G9A3-LIFE04").replace(current, incompatible).build()));

		assertEquals(SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
				failure.getDiagnostic().getCode());
		assertEquals(before, snapshot());
		assertStatus(graph, SpatialCertificateStatus.VALID, true);
		assertBindingTargetTransferRejects(graph);
		assertAttachmentPiggybackRejects(graph);
	}

	@Test
	void life05FrameMapRelationMembershipUsesExactAtomicRevisions() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		ProjectionBindingRecord horizontal = binding(graph.horizontalBindingId);
		ProjectionDiagramMapRecord map = map(horizontal.getDiagramMapId());
		ProjectionSystemRecord system = system(graph);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
		ProjectionDiagramMapRecord initialMap = map;

		assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMutation(SpatialLifecycleMutation.builder(
						SpatialLifecycleOperationKind.MAP_CHANGE,
						"G9A3-LIFE05-missing-system")
						.replace(initialMap,
								initialMap.withRevision(initialMap.getRevision() + 1))
						.build()));
		assertEquals(before, snapshot());

		lifecycle().prepareMapChange(system, map,
				map.withRevision(map.getRevision() + 1),
				system.withRevision(system.getRevision() + 1),
				"G9A3-LIFE05-map").commit();
		map = map(horizontal.getDiagramMapId());
		system = system(graph);
		assertMapReroleCoefficientTransferRejects(graph, map, system);
		ProjectionDiagramMapId oldMapId = map.getId();
		SpatialLifecycleTransaction mapRerole = lifecycle().prepareMapRerole(map,
				ProjectionFrameUseRole.AUXILIARY, "G9A3-LIFE05-map-rerole");
		ProjectionDiagramMapRecord rerolledMap = created(mapRerole,
				ProjectionDiagramMapRecord.class);
		mapRerole.commit();
		assertNull(registry().getRecord(oldMapId));
		assertNotEquals(oldMapId, rerolledMap.getId());
		assertEquals(ProjectionFrameUseRole.AUXILIARY,
				rerolledMap.getFrameUseRole());
		assertEquals(system.getId(), system(graph).getId());
		system = system(graph);

		ProjectionFrameRecord frame = frame(horizontal.getFrameId());
		ProjectionFrameRecord changedFrame = frame.withRevision(frame.getRevision() + 1);
		lifecycle().prepareFrameChange(system, frame, changedFrame,
				system.withRevision(system.getRevision() + 1),
				"G9A3-LIFE05-frame").commit();
		assertUnusedFramePiggybackRejects(graph);
		addAndRemoveFrameMap(graph);

		getKernel().clearConstruction(true);
		G9A2SpatialSemanticRuntimeTest.Graph hinged =
				G9A2SpatialSemanticRuntimeTest.Graph.createWithHinge(
						getConstruction(), this::add, false);
		assertRelationRequiresSystemAndRoundTrips(hinged);
	}

	@Test
	void life06MapValueInvalidationAndRecoveryRunsThroughNormalDag() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		graph.one.setValue(0);
		graph.one.updateRepaint();
		getKernel().updateConstruction(false);
		assertFalse(point(graph).getSemanticCertificate().hasPayload());
		assertFalse(getConstruction().getSpatialSemanticRuntime()
				.getDerivedPoint(graph.objectId).isDefined());

		graph.one.setValue(1);
		graph.one.updateRepaint();
		getKernel().updateConstruction(false);
		assertStatus(graph, SpatialCertificateStatus.VALID, true);
	}

	@Test
	void life07HingeSupportInvalidAndValidRevisionsRecoverDeterministically() {
		G9A2SpatialSemanticRuntimeTest.Graph graph =
				G9A2SpatialSemanticRuntimeTest.Graph.createProductiveWithHinge(
						getConstruction(), this::add);
		PersistentGeoId supportId = registry().getPersistentGeoId(graph.supportEnd);
		GeoIdentityRecord original = (GeoIdentityRecord) registry().getRecord(
				supportId);
		ProjectionSystemRecord originalSystem = system(graph);
		SpatialObjectRecord originalObject = object(graph);
		ProjectionSystemPilotCertificate originalSystemCertificate =
				getConstruction().getSpatialSemanticRuntime()
						.getProjectionSystemCertificate(graph.systemId);
		SpatialPointPilotCertificate originalPointCertificate = point(graph);

		ErrorAccumulator invalidErrors = redefineSupport(graph, "G9A2HE=(0,0,0)");
		assertEquals("", invalidErrors.getErrors());
		GeoIdentityRecord invalid = (GeoIdentityRecord) registry().getRecord(supportId);
		assertEquals(original.getDefinitionRevision() + 1,
				invalid.getDefinitionRevision());
		assertEquals(original.getTopologyRevision(), invalid.getTopologyRevision());
		assertEquals(originalSystem.getRevision(), system(graph).getRevision());
		assertEquals(originalObject.getTopologyRevision(),
				object(graph).getTopologyRevision());
		assertNotSame(originalSystemCertificate,
				getConstruction().getSpatialSemanticRuntime()
						.getProjectionSystemCertificate(graph.systemId));
		assertNotSame(originalPointCertificate, point(graph));
		assertFalse(point(graph).getSemanticCertificate().hasPayload());

		ErrorAccumulator recoveredErrors = redefineSupport(graph,
				"G9A2HE=(1,0,0)");
		assertEquals("", recoveredErrors.getErrors());
		GeoIdentityRecord recoveredRecord = (GeoIdentityRecord) registry()
				.getRecord(supportId);
		assertEquals(original.getDefinitionRevision() + 2,
				recoveredRecord.getDefinitionRevision());
		assertEquals(original.getTopologyRevision(),
				recoveredRecord.getTopologyRevision());
		assertEquals(supportId, registry().getPersistentGeoId(graph.supportEnd));
		G9A3SpatialGraphSnapshot.Snapshot recovered = snapshot();
		assertStatus(graph, SpatialCertificateStatus.VALID, true);
		getKernel().updateConstruction(false);
		assertEquals(recovered, snapshot());

		String recoveredXml = getApp().getXML();
		ErrorAccumulator rejectedErrors = redefineSupport(graph,
				"G9A2HE=Line((0,0,0),(1,0,0))");
		assertTrue(!rejectedErrors.getErrors().isEmpty());
		assertEquals(recoveredXml, getApp().getXML());
		assertEquals(recovered, snapshot());
		assertEquals(supportId,
				registry().getPersistentGeoId(lookup("G9A2HE")));
	}

	private ErrorAccumulator redefineSupport(
			G9A2SpatialSemanticRuntimeTest.Graph graph, String definition) {
		ErrorAccumulator errors = new ErrorAccumulator();
		EvalInfo info = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), graph.supportEnd, true);
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				graph.supportEnd, definition, info, false, null, errors);
		return errors;
	}

	@Test
	void life08DeleteAndSameAppearanceRecreateNeverRebindsRetiredIdentity() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		PersistentGeoId oldId = graph.horizontalPointId;
		graph.horizontalPointGeo.remove();
		assertNull(registry().getRecord(oldId));
		assertNull(registry().getRecord(graph.objectId));

		GeoElement recreated = add("G9A2PH=(2,3)");
		PersistentGeoId freshId = registerPointGeo(recreated, "G9A3-LIFE08");
		assertNotEquals(oldId, freshId);
		assertEquals(freshId, registry().getPersistentGeoId(recreated));
		assertNull(registry().getRecord(graph.objectId));
	}

	@Test
	void life09TrueSystemReplacementUsesFreshCompleteClosure() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		ProjectionFrameRecord basisFrame = frame(binding(
				graph.horizontalBindingId).getFrameId());
		ProjectionFrameId unrelatedId = registry().allocateProjectionFrameId();
		ProjectionFrameRecord unrelated = basisFrame.remap(
				Map.of(basisFrame.getId(), unrelatedId), false);
		registry().registerRecords(List.of(unrelated));
		List<SpatialIdentityRecord> current = new ArrayList<>(registry().getRecords());
		List<SpatialIdentityRecord> affected = new ArrayList<>();
		for (SpatialIdentityRecord record : current) {
			if (!record.getId().equals(unrelatedId)) {
				affected.add(record);
			}
		}
		ProjectionSystemRecord oldSystem = system(graph);
		int reservedBefore = registry().getReservedIdentityCount();
		Map<SpatialIdentityId, SpatialIdentityId> hostileRemap = freshNonGeoRemap(
				affected);
		ProjectionSystemRecord hostileSystem = oldSystem.remap(
				hostileRemap, false);
		List<SpatialIdentityRecord> hostileOld = new ArrayList<>();
		List<SpatialIdentityRecord> hostileFresh = new ArrayList<>();
		for (SpatialIdentityRecord record : affected) {
			if (record instanceof GeoIdentityRecord || record.getId().equals(graph.systemId)) {
				continue;
			}
			hostileOld.add(record);
			hostileFresh.add(record.remap(hostileRemap, false));
		}
		ProjectionFrameId hostileId = registry().allocateProjectionFrameId();
		ProjectionFrameRecord hostile = unrelated.remap(
				Map.of(unrelatedId, hostileId), false);
		hostileOld.add(unrelated);
		hostileFresh.add(hostile);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
		assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareSystemReplacement(oldSystem, hostileSystem,
						hostileOld, hostileFresh, "G9A3-LIFE09-piggyback"));
		assertEquals(before, snapshot());
		assertEquals(reservedBefore, registry().getReservedIdentityCount());

		Map<SpatialIdentityId, SpatialIdentityId> remap = freshNonGeoRemap(affected);
		ProjectionSystemRecord freshSystem = oldSystem.remap(
				remap, false);
		List<SpatialIdentityRecord> oldDependents = new ArrayList<>();
		List<SpatialIdentityRecord> replacementClosure = new ArrayList<>();
		for (SpatialIdentityRecord record : affected) {
			if (record instanceof GeoIdentityRecord || record.getId().equals(graph.systemId)) {
				continue;
			}
			oldDependents.add(record);
			replacementClosure.add(record.remap(remap, false));
		}

		lifecycle().prepareSystemReplacement(oldSystem, freshSystem, oldDependents,
				replacementClosure, "G9A3-LIFE09").commit();

		assertNull(registry().getRecord(graph.systemId));
		assertNotNull(registry().getRecord(freshSystem.getId()));
		assertNotEquals(graph.systemId, freshSystem.getId());
		for (SpatialIdentityRecord retired : oldDependents) {
			assertNull(registry().getRecord(retired.getId()));
		}
		for (SpatialIdentityRecord replacement : replacementClosure) {
			assertNotNull(registry().getRecord(replacement.getId()));
		}
		assertNotNull(registry().getRecord(graph.horizontalPointId));
		assertNotNull(registry().getRecord(unrelatedId));
		assertEquals(graph.horizontalPointId,
				registry().getPersistentGeoId(graph.horizontalPointGeo));
		assertEquals(SpatialCertificateStatus.VALID,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate((SpatialObjectId) remap.get(graph.objectId))
						.getSemanticCertificate().getStatus());
	}

	@Test
	void life10ZeroThroughThreeDefiningTransitionsNeverPublishStalePayload() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		ProjectionBindingId first = rerole(graph, graph.horizontalBindingId,
				ProjectionBindingRole.PRESENTATION, "G9A3-LIFE10-zero-a");
		ProjectionBindingId second = rerole(graph, graph.verticalBindingId,
				ProjectionBindingRole.PRESENTATION, "G9A3-LIFE10-zero-b");
		assertStatus(graph, SpatialCertificateStatus.UNDERDETERMINED, false);

		first = rerole(graph, first, ProjectionBindingRole.DEFINING,
				"G9A3-LIFE10-one");
		assertStatus(graph, SpatialCertificateStatus.UNDERDETERMINED, false);
		second = rerole(graph, second, ProjectionBindingRole.DEFINING,
				"G9A3-LIFE10-two");
		assertStatus(graph, SpatialCertificateStatus.VALID, true);
		graph.one.setUndefined();
		graph.one.updateRepaint();
		getKernel().updateConstruction(false);
		assertStatus(graph, SpatialCertificateStatus.UNDEFINED, false);
		graph.one.setValue(1);
		graph.one.updateRepaint();
		getKernel().updateConstruction(false);
		assertStatus(graph, SpatialCertificateStatus.VALID, true);

		GeoElement hostilePoint = add("G9A3Life10Third=(9,9)");
		PersistentGeoId hostileId = registerPointGeo(hostilePoint,
				"G9A3-LIFE10-third");
		ProjectionBindingRecord basis = binding(first);
		ProjectionBindingId thirdId = registry().allocateProjectionBindingId();
		ProjectionBindingRecord third = new ProjectionBindingRecord(thirdId, 2,
				basis.getObjectId(), basis.getSystemId(), basis.getDiagramMapId(),
				basis.getFrameId(), ProjectionBindingRole.DEFINING, "POINT", "POINT",
				SpatialObjectRecord.POINT_SCHEMA_ID, 1, hostileId, "EXACT",
				"NOT_REQUIRED", 0);
		addBinding(graph, third, "G9A3-LIFE10-three");
		assertEquals(SpatialCertificateStatus.INCONSISTENT_PROJECTIONS,
				point(graph).getSemanticCertificate().getStatus());
		assertFalse(point(graph).getSemanticCertificate().hasPayload());

		removeBinding(graph, thirdId, "G9A3-LIFE10-remove-third");
		graph.one.setValue(0);
		graph.one.updateRepaint();
		getKernel().updateConstruction(false);
		assertStatus(graph, SpatialCertificateStatus.DEGENERATE, false);
		assertEquals(0, getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().getStalePayloadPublications());
	}

	private void addAndRemoveFrameMap(G9A2SpatialSemanticRuntimeTest.Graph graph) {
		ProjectionSystemRecord system = system(graph);
		ProjectionBindingRecord basisBinding = binding(graph.verticalBindingId);
		ProjectionFrameRecord basisFrame = frame(basisBinding.getFrameId());
		ProjectionDiagramMapRecord basisMap = map(basisBinding.getDiagramMapId());
		ProjectionFrameId frameId = registry().allocateProjectionFrameId();
		ProjectionDiagramMapId mapId = registry().allocateProjectionDiagramMapId();
		ProjectionFrameRecord newFrame = new ProjectionFrameRecord(frameId, 2,
				basisFrame.getOriginGeoId(), basisFrame.getUGeoId(), basisFrame.getVGeoId(),
				basisFrame.getFamily(), basisFrame.getUnits(), basisFrame.getHandedness(),
				basisFrame.getFidelity(), 0);
		ProjectionDiagramMapRecord newMap = copyMap(basisMap, mapId, frameId,
				ProjectionFrameUseRole.AUXILIARY, List.of(), 0);
		List<ProjectionDiagramMapId> maps = new ArrayList<>(system.getMapIds());
		maps.add(mapId);
		ProjectionSystemRecord addedSystem = system.withMembershipAndRevision(maps,
				system.getRelationIds(), system.getRevision() + 1);
		lifecycle().prepareFrameMapAdd(system, newFrame, newMap, addedSystem,
				"G9A3-LIFE05-frame-map-add").commit();

		ProjectionSystemRecord currentSystem = system(graph);
		List<ProjectionDiagramMapId> reduced = new ArrayList<>(currentSystem.getMapIds());
		reduced.remove(mapId);
		lifecycle().prepareFrameMapRemove(currentSystem, map(mapId), frame(frameId),
				currentSystem.withMembershipAndRevision(reduced,
						currentSystem.getRelationIds(), currentSystem.getRevision() + 1),
				"G9A3-LIFE05-frame-map-remove").commit();
		assertNull(registry().getRecord(mapId));
		assertNull(registry().getRecord(frameId));
		assertStatus(graph, SpatialCertificateStatus.VALID, true);
	}

	private void assertUnusedFramePiggybackRejects(
			G9A2SpatialSemanticRuntimeTest.Graph graph) {
		ProjectionSystemRecord system = system(graph);
		ProjectionBindingRecord basisBinding = binding(graph.verticalBindingId);
		ProjectionFrameRecord basisFrame = frame(basisBinding.getFrameId());
		ProjectionDiagramMapRecord basisMap = map(basisBinding.getDiagramMapId());
		ProjectionFrameId unusedFrameId = registry().allocateProjectionFrameId();
		ProjectionDiagramMapId freshMapId =
				registry().allocateProjectionDiagramMapId();
		ProjectionFrameRecord unusedFrame = basisFrame.remap(
				Map.of(basisFrame.getId(), unusedFrameId), false);
		ProjectionDiagramMapRecord freshMap = copyMap(basisMap, freshMapId,
				basisFrame.getId(), ProjectionFrameUseRole.AUXILIARY, List.of(), 0);
		List<ProjectionDiagramMapId> maps = new ArrayList<>(system.getMapIds());
		maps.add(freshMapId);
		ProjectionSystemRecord updated = system.withMembershipAndRevision(maps,
				system.getRelationIds(), system.getRevision() + 1);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();

		assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMutation(SpatialLifecycleMutation.builder(
						SpatialLifecycleOperationKind.MAP_ADD,
						"G9A3-LIFE05-unused-frame")
						.replace(system, updated).create(freshMap)
						.create(unusedFrame).build()));
		assertEquals(before, snapshot());
	}

	private void assertBindingTargetTransferRejects(
			G9A2SpatialSemanticRuntimeTest.Graph graph) {
		GeoElement hostile = add("G9A3Life04Hostile=(2,3)");
		PersistentGeoId hostileId = registerPointGeo(hostile, "G9A3-LIFE04-hostile");
		ProjectionBindingRecord current = binding(graph.horizontalBindingId);
		ProjectionBindingRecord transferred = new ProjectionBindingRecord(
				current.getId(), 2, current.getObjectId(), current.getSystemId(),
				current.getDiagramMapId(), current.getFrameId(), current.getRole(),
				current.getRepresentationType(), current.getExpectedSpatialType(),
				current.getSchemaId(), current.getSchemaVersion(), hostileId,
				current.getFidelity(), current.getCorrespondence(),
				current.getRevision() + 1, current.getCopySourceId());
		ProjectionDiagramMapRecord map = map(current.getDiagramMapId());
		ProjectionSystemRecord system = system(graph);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMutation(SpatialLifecycleMutation.builder(
						SpatialLifecycleOperationKind.MAP_CHANGE,
						"G9A3-LIFE04-target-transfer")
						.replace(current, transferred)
						.replace(map, map.withRevision(map.getRevision() + 1))
						.replace(system, system.withRevision(system.getRevision() + 1))
						.build()));

		assertEquals(SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
				failure.getDiagnostic().getCode());
		assertEquals(before, snapshot());
	}

	private void assertMapReroleCoefficientTransferRejects(
			G9A2SpatialSemanticRuntimeTest.Graph graph,
			ProjectionDiagramMapRecord currentMap,
			ProjectionSystemRecord currentSystem) {
		ProjectionBindingRecord currentBinding = binding(graph.horizontalBindingId);
		ProjectionDiagramMapId freshMapId =
				registry().allocateProjectionDiagramMapId();
		ProjectionBindingId freshBindingId = registry().allocateProjectionBindingId();
		ProjectionDiagramMapRecord hostileMap = new ProjectionDiagramMapRecord(
				freshMapId, 2, currentMap.getSystemId(), currentMap.getFrameId(),
				ProjectionFrameUseRole.AUXILIARY, currentMap.getFamily(),
				currentMap.getOrientation(), currentMap.getUnits(),
				currentMap.getFidelity(), currentMap.getA01GeoId(),
				currentMap.getA01GeoId(), currentMap.getA10GeoId(),
				currentMap.getA11GeoId(), currentMap.getB0GeoId(),
				currentMap.getB1GeoId(), currentMap.getDeclaredScaleGeoId(),
				currentMap.getRelationIds(), 0);
		ProjectionBindingRecord freshBinding = currentBinding.asFreshRetargeted(
				freshBindingId, freshMapId);
		SpatialObjectRecord object = object(graph);
		SpatialObjectRecord updatedObject = object.withBindingsAndRevisions(
				replace(object.getBindingIds(), currentBinding.getId(), freshBindingId),
				object.getDefinitionRevision() + 1, object.getTopologyRevision() + 1);
		ProjectionSystemRecord updatedSystem = currentSystem.withMembershipAndRevision(
				replaceMap(currentSystem.getMapIds(), currentMap.getId(), freshMapId),
				currentSystem.getRelationIds(), currentSystem.getRevision() + 1);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
		int reservedBefore = registry().getReservedIdentityCount() - 2;

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMutation(SpatialLifecycleMutation.builder(
						SpatialLifecycleOperationKind.MAP_REROLE,
						"G9A3-LIFE05-map-coefficient-transfer")
						.retire(currentMap).create(hostileMap)
						.retire(currentBinding).create(freshBinding)
						.replace(object, updatedObject)
						.replace(currentSystem, updatedSystem).build()));

		assertEquals(SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
				failure.getDiagnostic().getCode());
		assertEquals(before, snapshot());
		assertEquals(reservedBefore, registry().getReservedIdentityCount());
	}

	private void assertAttachmentPiggybackRejects(
			G9A2SpatialSemanticRuntimeTest.Graph graph) {
		GeoElement hostile = add("G9A3Life04AttachmentHostile=(8,8)");
		ProjectionBindingRecord basis = binding(graph.horizontalBindingId);
		ProjectionBindingId freshId = registry().allocateProjectionBindingId();
		ProjectionBindingRecord extra = basis.asFreshReroled(freshId,
				ProjectionBindingRole.PRESENTATION);
		SpatialObjectRecord object = object(graph);
		List<ProjectionBindingId> ids = new ArrayList<>(object.getBindingIds());
		ids.add(freshId);
		SpatialObjectRecord updated = object.withBindingsAndRevisions(ids,
				object.getDefinitionRevision() + 1, object.getTopologyRevision() + 1);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMutation(SpatialLifecycleMutation.builder(
						SpatialLifecycleOperationKind.BINDING_ADD,
						"G9A3-LIFE04-attachment-piggyback")
						.replace(object, updated).create(extra)
						.detach(graph.horizontalPointGeo, graph.horizontalPointId)
						.attach(hostile, graph.horizontalPointId).build()));

		assertEquals(SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
				failure.getDiagnostic().getCode());
		assertEquals(before, snapshot());
		assertEquals(graph.horizontalPointId,
				registry().getPersistentGeoId(graph.horizontalPointGeo));
		assertNull(registry().getPersistentGeoId(hostile));
	}

	private void assertRelationRequiresSystemAndRoundTrips(
			G9A2SpatialSemanticRuntimeTest.Graph graph) {
		ProjectionFrameRelationRecord relation = relation(graph.relationId);
		assertRelationEndpointTransferRejects(relation);
		assertRelationCoefficientPiggybackRejects(relation);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
		assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMutation(SpatialLifecycleMutation.builder(
						SpatialLifecycleOperationKind.RELATION_CHANGE,
						"G9A3-LIFE05-relation-missing-system")
						.replace(relation,
								relation.withRevision(relation.getRevision() + 1)).build()));
		assertEquals(before, snapshot());

		ProjectionSystemRecord system = system(graph);
		ProjectionDiagramMapRecord source = map(relation.getSourceMapId());
		ProjectionDiagramMapRecord destination = map(relation.getDestinationMapId());
		lifecycle().prepareRelationRemove(system, relation, source,
				source.withRelationsAndRevision(List.of(), source.getRevision() + 1),
				destination,
				destination.withRelationsAndRevision(List.of(),
						destination.getRevision() + 1),
				system.withMembershipAndRevision(system.getMapIds(), List.of(),
						system.getRevision() + 1), "G9A3-LIFE05-relation-remove")
				.commit();

		ProjectionFrameRelationId freshId =
				registry().allocateProjectionFrameRelationId();
		ProjectionFrameRelationRecord fresh = copyRelation(relation, freshId);
		system = system(graph);
		source = map(relation.getSourceMapId());
		destination = map(relation.getDestinationMapId());
		lifecycle().prepareRelationAdd(system, fresh, source,
				source.withRelationsAndRevision(List.of(freshId), source.getRevision() + 1),
				destination,
				destination.withRelationsAndRevision(List.of(freshId),
						destination.getRevision() + 1),
				system.withMembershipAndRevision(system.getMapIds(), List.of(freshId),
						system.getRevision() + 1), "G9A3-LIFE05-relation-add").commit();
		assertNull(registry().getRecord(graph.relationId));
		assertNotNull(registry().getRecord(freshId));

		destination = map(fresh.getDestinationMapId());
		SpatialLifecycleTransaction mapRerole = lifecycle().prepareMapRerole(
				destination, ProjectionFrameUseRole.AUXILIARY,
				"G9A3-LIFE05-relation-map-rerole");
		ProjectionFrameRelationRecord retargeted = created(mapRerole,
				ProjectionFrameRelationRecord.class);
		mapRerole.commit();
		assertNull(registry().getRecord(freshId));

		ProjectionFrameRelationId changeId =
				registry().allocateProjectionFrameRelationId();
		ProjectionFrameRelationRecord change = new ProjectionFrameRelationRecord(
				changeId, 2, retargeted.getSystemId(), retargeted.getSourceMapId(),
				retargeted.getDestinationMapId(),
				ProjectionFrameRelationRecord.CHANGE_OF_PLANE,
				retargeted.getSupportStartGeoId(), retargeted.getSupportEndGeoId(),
				retargeted.getOrientation(), retargeted.getProvenance(), null, 0);
		lifecycle().prepareRelationRerole(retargeted, change,
				"G9A3-LIFE05-relation-rerole").commit();
		assertNull(registry().getRecord(retargeted.getId()));
		assertNotNull(registry().getRecord(changeId));
		assertStatus(graph, SpatialCertificateStatus.VALID, true);
	}

	private void assertRelationCoefficientPiggybackRejects(
			ProjectionFrameRelationRecord relation) {
		ProjectionSystemRecord system = (ProjectionSystemRecord) registry().getRecord(
				relation.getSystemId());
		ProjectionDiagramMapRecord source = map(relation.getSourceMapId());
		ProjectionDiagramMapRecord destination = map(relation.getDestinationMapId());
		ProjectionDiagramMapRecord hostileSource = new ProjectionDiagramMapRecord(
				source.getId(), 2, source.getSystemId(), source.getFrameId(),
				source.getFrameUseRole(), source.getFamily(), source.getOrientation(),
				source.getUnits(), source.getFidelity(), source.getA01GeoId(),
				source.getA01GeoId(), source.getA10GeoId(), source.getA11GeoId(),
				source.getB0GeoId(), source.getB1GeoId(),
				source.getDeclaredScaleGeoId(), List.of(), source.getRevision() + 1);
		ProjectionDiagramMapRecord updatedDestination =
				destination.withRelationsAndRevision(List.of(),
						destination.getRevision() + 1);
		ProjectionSystemRecord updatedSystem = system.withMembershipAndRevision(
				system.getMapIds(), List.of(), system.getRevision() + 1);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();

		assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareRelationRemove(system, relation, source,
						hostileSource, destination, updatedDestination, updatedSystem,
						"G9A3-LIFE05-relation-coefficient-piggyback"));
		assertEquals(before, snapshot());
	}

	private void assertRelationEndpointTransferRejects(
			ProjectionFrameRelationRecord current) {
		ProjectionSystemRecord system = (ProjectionSystemRecord) registry().getRecord(
				current.getSystemId());
		ProjectionDiagramMapRecord source = map(current.getSourceMapId());
		ProjectionDiagramMapRecord destination = map(current.getDestinationMapId());
		ProjectionFrameRelationId freshId =
				registry().allocateProjectionFrameRelationId();
		ProjectionFrameRelationRecord swapped = new ProjectionFrameRelationRecord(
				freshId, 2, current.getSystemId(), current.getDestinationMapId(),
				current.getSourceMapId(), current.getRelationKind(),
				current.getSupportStartGeoId(), current.getSupportEndGeoId(),
				current.getOrientation(), current.getProvenance(),
				current.getFoldSignGeoId(), 0);
		ProjectionSystemRecord updatedSystem = system.withMembershipAndRevision(
				system.getMapIds(), replaceRelation(system.getRelationIds(),
						current.getId(), freshId), system.getRevision() + 1);
		ProjectionDiagramMapRecord updatedSource = source.withRelationsAndRevision(
				replaceRelation(source.getRelationIds(), current.getId(), freshId),
				source.getRevision() + 1);
		ProjectionDiagramMapRecord updatedDestination =
				destination.withRelationsAndRevision(replaceRelation(
						destination.getRelationIds(), current.getId(), freshId),
						destination.getRevision() + 1);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();

		assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareMutation(SpatialLifecycleMutation.builder(
						SpatialLifecycleOperationKind.RELATION_REROLE,
						"G9A3-LIFE05-relation-endpoint-transfer")
						.retire(current).create(swapped)
						.replace(system, updatedSystem)
						.replace(source, updatedSource)
						.replace(destination, updatedDestination).build()));
		assertEquals(before, snapshot());
	}

	private ProjectionBindingId rerole(G9A2SpatialSemanticRuntimeTest.Graph graph,
			ProjectionBindingId currentId, ProjectionBindingRole role, String token) {
		ProjectionBindingRecord current = binding(currentId);
		ProjectionBindingId freshId = registry().allocateProjectionBindingId();
		ProjectionBindingRecord replacement = current.asFreshReroled(freshId, role);
		SpatialObjectRecord object = object(graph);
		List<ProjectionBindingId> ids = replace(object.getBindingIds(), currentId, freshId);
		SpatialObjectRecord updated = object.withBindingsAndRevisions(ids,
				object.getDefinitionRevision() + 1, object.getTopologyRevision() + 1);
		lifecycle().prepareBindingRerole(object, current, replacement, updated, token)
				.commit();
		return freshId;
	}

	private void removeBinding(G9A2SpatialSemanticRuntimeTest.Graph graph,
			ProjectionBindingId currentId, String token) {
		ProjectionBindingRecord current = binding(currentId);
		SpatialObjectRecord object = object(graph);
		List<ProjectionBindingId> ids = new ArrayList<>(object.getBindingIds());
		ids.remove(currentId);
		SpatialObjectRecord updated = object.withBindingsAndRevisions(ids,
				object.getDefinitionRevision() + 1, object.getTopologyRevision() + 1);
		lifecycle().prepareBindingRemove(object, current, updated, token).commit();
	}

	private void addBinding(G9A2SpatialSemanticRuntimeTest.Graph graph,
			ProjectionBindingRecord binding, String token) {
		SpatialObjectRecord object = object(graph);
		List<ProjectionBindingId> ids = new ArrayList<>(object.getBindingIds());
		ids.add(binding.getId());
		SpatialObjectRecord updated = object.withBindingsAndRevisions(ids,
				object.getDefinitionRevision() + 1, object.getTopologyRevision() + 1);
		lifecycle().prepareBindingAdd(object, binding, updated, token).commit();
	}

	private Map<SpatialIdentityId, SpatialIdentityId> freshNonGeoRemap(
			List<SpatialIdentityRecord> records) {
		Map<SpatialIdentityId, SpatialIdentityId> remap = new LinkedHashMap<>();
		for (SpatialIdentityRecord record : records) {
			if (record instanceof GeoIdentityRecord) {
				continue;
			}
			switch (record.getId().getKind()) {
			case SPATIAL_OBJECT:
				remap.put(record.getId(), registry().allocateSpatialObjectId());
				break;
			case PROJECTION_FRAME:
				remap.put(record.getId(), registry().allocateProjectionFrameId());
				break;
			case PROJECTION_SYSTEM:
				remap.put(record.getId(), registry().allocateProjectionSystemId());
				break;
			case PROJECTION_DIAGRAM_MAP:
				remap.put(record.getId(), registry().allocateProjectionDiagramMapId());
				break;
			case PROJECTION_FRAME_RELATION:
				remap.put(record.getId(),
						registry().allocateProjectionFrameRelationId());
				break;
			case PROJECTION_BINDING:
				remap.put(record.getId(), registry().allocateProjectionBindingId());
				break;
			default:
				throw new AssertionError(record.getId().getKind());
			}
		}
		return remap;
	}

	private PersistentGeoId registerPointGeo(GeoElement geo, String role) {
		PersistentGeoId id = registry().allocatePersistentGeoId();
		registry().registerParticipation(geo, new GeoIdentityRecord(id, "g9a2.test",
				"SEMANTIC_INPUT", SpatialObjectRecord.POINT_SCHEMA_ID,
				SpatialObjectRecord.POINT_SCHEMA_VERSION,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				role, 1, 0, 0));
		return id;
	}

	private static List<ProjectionBindingId> replace(List<ProjectionBindingId> ids,
			ProjectionBindingId current, ProjectionBindingId replacement) {
		List<ProjectionBindingId> result = new ArrayList<>(ids);
		int index = result.indexOf(current);
		if (index < 0) {
			throw new AssertionError("Binding is absent from object membership");
		}
		result.set(index, replacement);
		return result;
	}

	private static List<ProjectionDiagramMapId> replaceMap(
			List<ProjectionDiagramMapId> ids, ProjectionDiagramMapId current,
			ProjectionDiagramMapId replacement) {
		List<ProjectionDiagramMapId> result = new ArrayList<>(ids);
		result.set(result.indexOf(current), replacement);
		return result;
	}

	private static List<ProjectionFrameRelationId> replaceRelation(
			List<ProjectionFrameRelationId> ids, ProjectionFrameRelationId current,
			ProjectionFrameRelationId replacement) {
		List<ProjectionFrameRelationId> result = new ArrayList<>(ids);
		result.set(result.indexOf(current), replacement);
		return result;
	}

	private static ProjectionDiagramMapRecord copyMap(
			ProjectionDiagramMapRecord source, ProjectionDiagramMapId id,
			ProjectionFrameId frameId, ProjectionFrameUseRole role,
			List<ProjectionFrameRelationId> relationIds, long revision) {
		return new ProjectionDiagramMapRecord(id, 2, source.getSystemId(), frameId,
				role, source.getFamily(), source.getOrientation(), source.getUnits(),
				source.getFidelity(), source.getA00GeoId(), source.getA01GeoId(),
				source.getA10GeoId(), source.getA11GeoId(), source.getB0GeoId(),
				source.getB1GeoId(), source.getDeclaredScaleGeoId(), relationIds,
				revision);
	}

	private static ProjectionFrameRelationRecord copyRelation(
			ProjectionFrameRelationRecord source, ProjectionFrameRelationId id) {
		return new ProjectionFrameRelationRecord(id, 2, source.getSystemId(),
				source.getSourceMapId(), source.getDestinationMapId(),
				source.getRelationKind(), source.getSupportStartGeoId(),
				source.getSupportEndGeoId(), source.getOrientation(),
				source.getProvenance(), source.getFoldSignGeoId(), 0);
	}

	private static <T extends SpatialIdentityRecord> T created(
			SpatialLifecycleTransaction transaction, Class<T> type) {
		for (SpatialIdentityRecord record
				: transaction.getMutation().getCreatedRecords().values()) {
			if (type.isInstance(record)) {
				return type.cast(record);
			}
		}
		throw new AssertionError("No created " + type.getSimpleName());
	}

	private G9A2SpatialSemanticRuntimeTest.Graph graph() {
		return G9A2SpatialSemanticRuntimeTest.Graph.create(getConstruction(), this::add);
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private SpatialPointLifecycleService lifecycle() {
		return new SpatialPointLifecycleService(registry());
	}

	private G9A3SpatialGraphSnapshot.Snapshot snapshot() {
		return G9A3SpatialGraphSnapshot.capture(getConstruction());
	}

	private SpatialObjectRecord object(G9A2SpatialSemanticRuntimeTest.Graph graph) {
		return (SpatialObjectRecord) registry().getRecord(graph.objectId);
	}

	private ProjectionSystemRecord system(G9A2SpatialSemanticRuntimeTest.Graph graph) {
		return (ProjectionSystemRecord) registry().getRecord(graph.systemId);
	}

	private ProjectionBindingRecord binding(ProjectionBindingId id) {
		return (ProjectionBindingRecord) registry().getRecord(id);
	}

	private ProjectionDiagramMapRecord map(ProjectionDiagramMapId id) {
		return (ProjectionDiagramMapRecord) registry().getRecord(id);
	}

	private ProjectionFrameRecord frame(ProjectionFrameId id) {
		return (ProjectionFrameRecord) registry().getRecord(id);
	}

	private ProjectionFrameRelationRecord relation(ProjectionFrameRelationId id) {
		return (ProjectionFrameRelationRecord) registry().getRecord(id);
	}

	private SpatialPointPilotCertificate point(
			G9A2SpatialSemanticRuntimeTest.Graph graph) {
		return getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId);
	}

	private void assertStatus(G9A2SpatialSemanticRuntimeTest.Graph graph,
			SpatialCertificateStatus status, boolean payload) {
		SpatialPointPilotCertificate point = point(graph);
		assertNotNull(point);
		assertEquals(status, point.getSemanticCertificate().getStatus());
		assertEquals(payload, point.getSemanticCertificate().hasPayload());
		assertEquals(0, getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().getStalePayloadPublications());
	}
}
