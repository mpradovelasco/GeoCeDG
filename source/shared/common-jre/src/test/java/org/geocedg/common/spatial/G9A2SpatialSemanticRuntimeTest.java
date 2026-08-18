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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialPointPilotRedefineProvider;
import org.geocedg.common.kernel.spatial.runtime.ProjectionSystemPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialPointPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialSemanticRuntime;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCapabilityStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCurrentnessStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialDefinitionStatus;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.euclidian3D.EuclidianView3DInterface;
import org.geogebra.common.geogebra3D.kernel3D.geos.GeoPoint3D;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.Algos;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.plugin.GeoClass;
import org.junit.jupiter.api.Test;

class G9A2SpatialSemanticRuntimeTest extends BaseUnitTest {
	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D();
	}

	@Test
	void completeVersionTwoGraphPublishesCurrentPointThroughNormalDag() {
		Graph graph = Graph.create(getConstruction(), this::add);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();

		assertNotNull(runtime.getSystemAlgorithm(graph.systemId));
		assertNotNull(runtime.getPointAlgorithm(graph.objectId));
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate point = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertNotNull(point);
		assertTrue(point.isCurrentRevision());
		assertEquals(SpatialCertificateStatus.VALID,
				point.getSemanticCertificate().getStatus());
		assertVectorEquals(new Vector3(2, 3, 5),
				point.getSemanticCertificate().getPoint().orElseThrow());
		assertTrue(point.getRevisionTuple().containsKey(graph.objectId));
		assertTrue(point.getRevisionTuple().containsKey(graph.systemId));
		assertFalse(point.getValueSnapshotToken().isEmpty());
		ArrayList<String> bindingKeys = new ArrayList<>(List.of(
				graph.horizontalBindingId.toExternalForm(),
				graph.verticalBindingId.toExternalForm()));
		Collections.sort(bindingKeys);
		assertEquals(bindingKeys.get(0), point.getSemanticCertificate()
				.getResidualEvidence().get(0).getEvidenceKey());
		assertEquals(bindingKeys.get(1), point.getSemanticCertificate()
				.getResidualEvidence().get(1).getEvidenceKey());
		ProjectionSystemRecord system = (ProjectionSystemRecord) graph.registry
				.getRecord(graph.systemId);
		ArrayList<String> mapKeys = new ArrayList<>(List.of(
				system.getMapIds().get(0).toExternalForm(),
				system.getMapIds().get(1).toExternalForm()));
		Collections.sort(mapKeys);
		assertEquals(mapKeys.get(0), point
				.getSemanticCertificate().getProjectionSystemCertificate()
				.getMapEvidence().get(0).getEvidenceKey());
		assertEquals(mapKeys.get(1), point
				.getSemanticCertificate().getProjectionSystemCertificate()
				.getMapEvidence().get(1).getEvidenceKey());
	}

	@Test
	void mapRevisionRecomputesOnlyReferencingSystemAndObjectCertificates()
			throws ReflectiveOperationException {
		Graph graph = Graph.create(getConstruction(), this::add);
		SiblingGraph sibling = SiblingGraph.create(graph);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		ProjectionSystemPilotCertificate referencedSystemBefore =
				runtime.getProjectionSystemCertificate(graph.systemId);
		SpatialPointPilotCertificate referencedPointBefore =
				runtime.getSpatialPointCertificate(graph.objectId);
		final ProjectionSystemPilotCertificate unrelatedSystemBefore =
				runtime.getProjectionSystemCertificate(sibling.systemId);
		final SpatialPointPilotCertificate unrelatedPointBefore =
				runtime.getSpatialPointCertificate(sibling.objectId);
		Object referencedSystemAlgorithm = runtime.getSystemAlgorithm(graph.systemId);
		Object referencedPointAlgorithm = runtime.getPointAlgorithm(graph.objectId);
		final Object unrelatedSystemAlgorithm = runtime.getSystemAlgorithm(sibling.systemId);
		final Object unrelatedPointAlgorithm = runtime.getPointAlgorithm(sibling.objectId);
		final long dependencyUpdates = runtime.getInstrumentation().getDependencyUpdates();
		ProjectionSystemRecord system = (ProjectionSystemRecord) graph.registry
				.getRecord(graph.systemId);
		ProjectionDiagramMapRecord currentMap = (ProjectionDiagramMapRecord)
				graph.registry.getRecord(system.getMapIds().get(0));
		ProjectionDiagramMapRecord revisedMap = copyMapAtRevision(currentMap,
				currentMap.getId(), graph.systemId, currentMap.getRevision() + 1);
		ProjectionSystemRecord revisedSystem = new ProjectionSystemRecord(
				system.getId(), system.getSemanticVersion(), system.getMapIds(),
				system.getRelationIds(), system.getUnits(),
				system.getAbsoluteTolerance(), system.getRelativeTolerance(),
				system.getRankTolerance(), system.getMapTolerance(),
				system.getHingeTolerance(), system.getConditionLimit(),
				system.getRevision() + 1);

		replacePublishedRecordForRevisionTest(graph.registry, revisedMap);
		replacePublishedRecordForRevisionTest(graph.registry, revisedSystem);
		runtime.onRecordsPublished(List.of(revisedMap.getId(), revisedSystem.getId()));

		ProjectionSystemPilotCertificate referencedSystemAfter =
				runtime.getProjectionSystemCertificate(graph.systemId);
		SpatialPointPilotCertificate referencedPointAfter =
				runtime.getSpatialPointCertificate(graph.objectId);
		assertNotSame(referencedSystemAlgorithm,
				runtime.getSystemAlgorithm(graph.systemId));
		assertNotSame(referencedPointAlgorithm,
				runtime.getPointAlgorithm(graph.objectId));
		assertNotEquals(referencedSystemBefore.getRevisionTuple(),
				referencedSystemAfter.getRevisionTuple());
		assertNotEquals(referencedPointBefore.getRevisionTuple(),
				referencedPointAfter.getRevisionTuple());
		assertTrue(referencedSystemAfter.getRevisionTuple().get(revisedMap.getId())
				.startsWith("map:" + revisedMap.getRevision() + ":"));
		assertTrue(referencedSystemAfter.getRevisionTuple().get(revisedSystem.getId())
				.startsWith("system:" + revisedSystem.getRevision() + ":"));
		assertTrue(referencedPointAfter.getRevisionTuple().get(revisedSystem.getId())
				.startsWith("system:" + revisedSystem.getRevision() + ":"));
		assertEquals(system.getRevision() + 1,
				((ProjectionSystemRecord) graph.registry.getRecord(graph.systemId))
						.getRevision());
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				referencedSystemAfter.getSemanticCertificate().getStatus());
		assertEquals(SpatialCertificateStatus.VALID,
				referencedPointAfter.getSemanticCertificate().getStatus());
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
		assertSame(unrelatedSystemAlgorithm,
				runtime.getSystemAlgorithm(sibling.systemId));
		assertSame(unrelatedPointAlgorithm,
				runtime.getPointAlgorithm(sibling.objectId));
		assertSame(unrelatedSystemBefore,
				runtime.getProjectionSystemCertificate(sibling.systemId));
		assertSame(unrelatedPointBefore,
				runtime.getSpatialPointCertificate(sibling.objectId));
		assertEquals(dependencyUpdates + 2,
				runtime.getInstrumentation().getDependencyUpdates());
	}

	@Test
	void dynamicValidFailureRecoveryWithdrawsAndReusesNoStalePoint() {
		Graph graph = Graph.create(getConstruction(), this::add);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		GeoPoint3D derived = runtime.getDerivedPoint(graph.objectId);
		assertPoint(derived, 2, 3, 5);

		graph.setVerticalProjection(9, 5);
		getKernel().updateConstruction(false);

		SpatialPointPilotCertificate failed = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertEquals(SpatialCertificateStatus.INCONSISTENT_PROJECTIONS,
				failed.getSemanticCertificate().getStatus());
		assertTrue(failed.isCurrentRevision());
		assertNotEquals(SpatialCurrentnessStatus.INVALIDATED,
				failed.getSemanticCertificate().getCurrentnessStatus());
		assertFalse(failed.getSemanticCertificate().hasPayload());
		assertFalse(derived.isDefined());
		assertEquals(0, runtime.getInstrumentation().getStalePayloadPublications());

		graph.setVerticalProjection(2, 5);
		getKernel().updateConstruction(false);

		assertEquals(SpatialCertificateStatus.VALID,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertSame(derived, runtime.getDerivedPoint(graph.objectId));
		assertPoint(derived, 2, 3, 5);
		assertTrue(runtime.getInstrumentation().getDerivedViewWithdrawals() >= 1);
	}

	@Test
	void derivedThreeDimensionalPointIsTransientUnlabeledAndNonEditable() {
		Graph graph = Graph.create(getConstruction(), this::add);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		GeoPoint3D derived = runtime.getDerivedPoint(graph.objectId);
		final SpatialPointPilotCertificate before = runtime.getSpatialPointCertificate(
				graph.objectId);
		final GeoPointND horizontal = (GeoPointND) graph.horizontalPointGeo;
		final PersistentGeoId verticalPointId = graph.registry.getPersistentGeoId(
				(GeoElement) graph.verticalPoint);

		assertNotNull(derived);
		assertTrue(derived.isDefined());
		assertFalse(derived.isLabelSet());
		assertNull(derived.getLabelSimple());
		assertFalse(derived.isIndependent());
		assertFalse(derived.isSelectionAllowed(null));
		assertFalse(derived.isPointerChangeable());
		assertSame(runtime.getPointAlgorithm(graph.objectId),
				derived.getParentAlgorithm());
		assertNull(graph.registry.getPersistentGeoId(derived));
		assertFalse(getApp().getXML().contains("GeoCeDGProjectionDefinedPoint"));

		derived.setCoords(91, 92, 93, 1);
		derived.updateRepaint();

		assertPoint(derived, 91, 92, 93);
		assertEquals(2, horizontal.getInhomX(), 1e-9);
		assertEquals(3, horizontal.getInhomY(), 1e-9);
		assertEquals(2, graph.verticalPoint.getInhomX(), 1e-9);
		assertEquals(5, graph.verticalPoint.getInhomY(), 1e-9);
		assertSame(before, runtime.getSpatialPointCertificate(graph.objectId));
		assertVectorEquals(new Vector3(2, 3, 5),
				before.getSemanticCertificate().getPoint().orElseThrow());
		assertEquals(EditAuthorityMode.PROJECTION_DEFINED,
				((SpatialObjectRecord) graph.registry.getRecord(graph.objectId))
						.getAuthority());
		assertEquals(EditAuthorityMode.PROJECTION_DEFINED,
				((GeoIdentityRecord) graph.registry.getRecord(graph.horizontalPointId))
						.getAuthority());
		assertEquals(EditAuthorityMode.PROJECTION_DEFINED,
				((GeoIdentityRecord) graph.registry.getRecord(verticalPointId))
						.getAuthority());

		graph.setVerticalProjection(2, 6);
		getKernel().updateConstruction(false);
		assertPoint(derived, 2, 3, 6);
		graph.setVerticalProjection(2, 5);
		getKernel().updateConstruction(false);

		assertPoint(derived, 2, 3, 5);
		assertEquals(SpatialCertificateStatus.VALID,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertEquals(0, runtime.getInstrumentation().getStalePayloadPublications());
	}

	@Test
	void presentationMatrixNeverEntersSemanticEvidenceOrRecomputesCertificates() {
		Graph graph = Graph.create(getConstruction(), this::add);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		ProjectionSystemPilotCertificate systemBefore =
				runtime.getProjectionSystemCertificate(graph.systemId);
		SpatialPointPilotCertificate before = runtime.getSpatialPointCertificate(
				graph.objectId);
		final PersistentGeoId horizontalId = graph.horizontalPointId;
		List<SpatialIdentityId> recordIds = new ArrayList<>(
				before.getRevisionTuple().keySet());
		List<SpatialIdentityRecord> records = new ArrayList<>();
		for (SpatialIdentityId recordId : recordIds) {
			records.add(graph.registry.getRecord(recordId));
		}
		List<Long> counters = instrumentationCounters(runtime);

		getApp().getEuclidianView1().setCoordSystem(430, 270, 137, 52);
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		getApp().getEuclidianView1().getSettings().setSize(997, 613);
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		// The headless view implements this repository-supported DPI seam as a no-op.
		getApp().getEuclidianView1().setPixelRatio(2);
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		getApp().getEuclidianView3D().setRotXYinDegrees(37, -21);
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		getApp().getEuclidianView3D().setProjection(
				EuclidianView3DInterface.PROJECTION_PERSPECTIVE);
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		graph.horizontalPointGeo.rename("RenamedHorizontalProjection");
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		graph.horizontalPointGeo.setLabelVisible(true);
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		graph.horizontalPointGeo.setLayer(7);
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		graph.horizontalPointGeo.setEuclidianVisible(false);
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		graph.horizontalPointGeo.setLineThickness(11);
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		getApp().removeFromEuclidianView(graph.horizontalPointGeo);
		assertPresentationInvariant(graph, runtime, systemBefore, before,
				recordIds, records, counters);

		assertEquals(horizontalId,
				graph.registry.getPersistentGeoId(graph.horizontalPointGeo));
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
		assertEquals(0, runtime.getInstrumentation().getLabelAuthorityUses());
		assertEquals(0, runtime.getInstrumentation().getLayerOrVisibilityReads());
		assertEquals(0, runtime.getInstrumentation().getViewportReads());
		assertEquals(0, runtime.getInstrumentation().getCameraTransformReads());
		assertEquals(0, runtime.getInstrumentation().getRendererReads());
	}

	@Test
	void invalidMapValueFailsSystemAndPointThenRecoversThroughTheDag() {
		Graph graph = Graph.create(getConstruction(), this::add);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		GeoPoint3D derived = runtime.getDerivedPoint(graph.objectId);

		graph.one.setValue(0);
		graph.one.updateRepaint();
		getKernel().updateConstruction(false);

		assertEquals(ProjectionSystemStatus.DEGENERATE,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertEquals(SpatialCertificateStatus.DEGENERATE,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertFalse(derived.isDefined());

		graph.one.setValue(1);
		graph.one.updateRepaint();
		getKernel().updateConstruction(false);

		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertPoint(derived, 2, 3, 5);
	}

	@Test
	void fullRankLossAndRecoveryPreserveIdentityAndWithdrawStaleDerivedPoint() {
		Graph graph = Graph.create(getConstruction(), this::add);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		GeoPoint3D derived = runtime.getDerivedPoint(graph.objectId);

		graph.setRankLoss(true);
		getKernel().updateConstruction(false);

		assertEquals(SpatialCertificateStatus.UNDERDETERMINED,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertFalse(runtime.getSpatialPointCertificate(graph.objectId)
				.getSemanticCertificate().hasPayload());
		assertFalse(derived.isDefined());
		assertNotNull(graph.registry.getRecord(graph.objectId));

		graph.setRankLoss(false);
		getKernel().updateConstruction(false);

		assertEquals(SpatialCertificateStatus.VALID,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertSame(derived, runtime.getDerivedPoint(graph.objectId));
		assertPoint(derived, 2, 3, 5);
		assertEquals(0, runtime.getInstrumentation().getStalePayloadPublications());
	}

	@Test
	void twoSourcesChangingAcrossCaptureRejectSupersededCandidateAndRecover() {
		Graph graph = Graph.createWithTopology(getConstruction(), this::add,
				TopologyMode.SNAPSHOT_MISMATCH);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		ArmedSnapshotNumeric armed = (ArmedSnapshotNumeric) graph.one;
		ArmedSnapshotNumeric armedZero = (ArmedSnapshotNumeric) graph.zero;
		long rejected = runtime.getInstrumentation()
				.getSupersededCandidateRejections();
		runtime.getPointAlgorithm(graph.objectId).invalidateCurrentRevision();

		armed.armSecondCaptureMutation();
		armedZero.armSecondCaptureMutation();
		runtime.getPointAlgorithm(graph.objectId).compute();

		SpatialPointPilotCertificate noncurrent = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertEquals(2, armed.getDouble(), 0);
		assertEquals(1, armedZero.getDouble(), 0);
		assertEquals(rejected + 1, runtime.getInstrumentation()
				.getSupersededCandidateRejections());
		assertFalse(noncurrent.isCurrentRevision());
		assertEquals(SpatialCurrentnessStatus.INVALIDATED,
				noncurrent.getSemanticCertificate().getCurrentnessStatus());
		assertFalse(noncurrent.getSemanticCertificate().hasPayload());
		assertFalse(runtime.getDerivedPoint(graph.objectId).isDefined());
		assertEquals(0, runtime.getInstrumentation().getStalePayloadPublications());

		armed.restore(1);
		armedZero.restore(0);

		assertEquals(SpatialCertificateStatus.VALID,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertTrue(runtime.getSpatialPointCertificate(graph.objectId)
				.isCurrentRevision());
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
	}

	@Test
	void derivedPointPublishesExactlyOnceToNormalDagConsumerPerRevision() {
		Graph graph = Graph.create(getConstruction(), this::add);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		DerivedPointConsumer consumer = new DerivedPointConsumer(getConstruction(),
				runtime.getDerivedPoint(graph.objectId));
		int initialUpdates = consumer.getComputeCount();
		long reconstructionAttempts = runtime.getInstrumentation()
				.getReconstructionAttempts();

		assertTrue(consumer.getValue().isDefined());
		assertEquals(10, consumer.getValue().getDouble(), 1e-9);
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
		assertNotNull(runtime.getSpatialPointCertificate(graph.objectId));
		assertEquals(initialUpdates, consumer.getComputeCount());
		assertEquals(reconstructionAttempts,
				runtime.getInstrumentation().getReconstructionAttempts());

		graph.setVerticalProjection(9, 5);

		assertEquals(initialUpdates + 1, consumer.getComputeCount());
		assertFalse(consumer.getValue().isDefined());
		assertFalse(runtime.getDerivedPoint(graph.objectId).isDefined());
		assertEquals(SpatialCertificateStatus.INCONSISTENT_PROJECTIONS,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());

		graph.setVerticalProjection(2, 5);

		assertEquals(initialUpdates + 2, consumer.getComputeCount());
		assertTrue(consumer.getValue().isDefined());
		assertEquals(10, consumer.getValue().getDouble(), 1e-9);
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
	}

	@Test
	void spatialDefinedProjectedHostsCannotAcquireProjectionAuthority() {
		Graph graph = Graph.create(getConstruction(), this::add,
				EditAuthorityMode.SPATIAL_DEFINED, ProjectionBindingRole.DEFINING);
		assertRejectedProjectedContext(graph);
	}

	@Test
	void derivedProjectedHostsCannotBecomeDefiningEvidence() {
		Graph graph = Graph.create(getConstruction(), this::add,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DERIVED);
		assertRejectedProjectedContext(graph);
	}

	@Test
	void unsupportedFrameFamilyIsNotMisreportedAsAnUndefinedSystem() {
		Graph graph = Graph.createWithTopology(getConstruction(), this::add,
				TopologyMode.UNSUPPORTED_FRAME_FAMILY);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();

		assertEquals(SpatialCapabilityStatus.UNSUPPORTED,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getCapabilityStatus());
		assertEquals(ProjectionSystemStatus.NOT_EVALUATED,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertEquals(SpatialCapabilityStatus.UNSUPPORTED,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getCapabilityStatus());
		assertEquals(SpatialCertificateStatus.NOT_EVALUATED,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertFalse(runtime.getSpatialPointCertificate(graph.objectId)
				.getSemanticCertificate().hasPayload());
	}

	@Test
	void unresolvedProjectedIdentityRemainsSupportedAndExplicitlyUndefined() {
		Graph graph = Graph.createWithTopology(getConstruction(), this::add,
				TopologyMode.MISSING_PROJECTED_IDENTITY);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();

		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate point = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertEquals(SpatialCapabilityStatus.SUPPORTED,
				point.getSemanticCertificate().getCapabilityStatus());
		assertEquals(ProjectionSystemStatus.UNDEFINED,
				point.getSemanticCertificate().getProjectionSystemCertificate().getStatus());
		assertEquals(SpatialDefinitionStatus.UNDEFINED,
				point.getSemanticCertificate().getDefinitionStatus());
		assertEquals(SpatialCertificateStatus.UNDEFINED,
				point.getSemanticCertificate().getStatus());
		assertFalse(point.getSemanticCertificate().hasPayload());
	}

	@Test
	void definingBindingWithWrongMapFrameContextIsStructurallyInconsistent()
			throws ReflectiveOperationException {
		Graph graph = Graph.create(getConstruction(), this::add);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		ProjectionBindingRecord vertical = (ProjectionBindingRecord) graph.registry
				.getRecord(graph.verticalBindingId);
		ProjectionBindingRecord horizontal = (ProjectionBindingRecord) graph.registry
				.getRecord(graph.horizontalBindingId);
		ProjectionBindingRecord wrongContext = new ProjectionBindingRecord(
				vertical.getId(), 2, vertical.getObjectId(), vertical.getSystemId(),
				horizontal.getDiagramMapId(), vertical.getFrameId(), vertical.getRole(),
				vertical.getRepresentationType(), vertical.getExpectedSpatialType(),
				vertical.getSchemaId(), vertical.getSchemaVersion(),
				vertical.getProjectedPointGeoId(), vertical.getFidelity(),
				vertical.getCorrespondence(), vertical.getRevision() + 1);

		replacePublishedRecordForRevisionTest(graph.registry, wrongContext);
		runtime.onRecordsPublished(List.of(wrongContext.getId()));

		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate point = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertEquals(SpatialCapabilityStatus.SUPPORTED,
				point.getSemanticCertificate().getCapabilityStatus());
		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				point.getSemanticCertificate().getProjectionSystemCertificate().getStatus());
		assertEquals(SpatialDefinitionStatus.DEFINED,
				point.getSemanticCertificate().getDefinitionStatus());
		assertEquals(SpatialCertificateStatus.UNDEFINED,
				point.getSemanticCertificate().getStatus());
		assertEquals(SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				point.getSemanticCertificate().getCurrentnessStatus());
		assertFalse(point.getSemanticCertificate().hasPayload());
		assertFalse(runtime.getDerivedPoint(graph.objectId).isDefined());
	}

	@Test
	void nonDefiningBindingCoexistsWithoutDrivingReconstructionOrInvalidation()
			throws ReflectiveOperationException {
		Graph graph = Graph.createWithTopology(getConstruction(), this::add,
				TopologyMode.NON_DEFINING_BINDING);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();

		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate before = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertEquals(SpatialCapabilityStatus.SUPPORTED,
				before.getSemanticCertificate().getCapabilityStatus());
		assertEquals(SpatialCertificateStatus.VALID,
				before.getSemanticCertificate().getStatus());
		assertTrue(before.getRevisionTuple().containsKey(
				graph.nonDefiningBindingId));
		final String passiveRevision = before.getRevisionTuple().get(
				graph.nonDefiningBindingId);
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
		assertTrue(((SpatialObjectRecord) graph.registry.getRecord(graph.objectId))
				.getBindingIds().contains(graph.nonDefiningBindingId));
		long reconstructionAttempts = runtime.getInstrumentation()
				.getReconstructionAttempts();
		long rankEvaluations = runtime.getInstrumentation().getRankEvaluations();
		final long certificatePublications = runtime.getInstrumentation()
				.getCertificatePublications();
		final long dependencyUpdates = runtime.getInstrumentation().getDependencyUpdates();

		graph.setNonDefiningProjection(-40, 60);

		SpatialPointPilotCertificate after = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertSame(before, after);
		assertEquals(before.getRevisionTuple(), after.getRevisionTuple());
		assertEquals(before.getValueSnapshotToken(), after.getValueSnapshotToken());
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
		assertEquals(reconstructionAttempts,
				runtime.getInstrumentation().getReconstructionAttempts());
		assertEquals(rankEvaluations,
				runtime.getInstrumentation().getRankEvaluations());
		assertEquals(certificatePublications,
				runtime.getInstrumentation().getCertificatePublications());
		assertEquals(dependencyUpdates,
				runtime.getInstrumentation().getDependencyUpdates());

		ProjectionBindingRecord passive = (ProjectionBindingRecord) graph.registry
				.getRecord(graph.nonDefiningBindingId);
		ProjectionBindingRecord revisedPassive = new ProjectionBindingRecord(
				passive.getId(), 2, passive.getObjectId(), passive.getSystemId(),
				passive.getDiagramMapId(), passive.getFrameId(), passive.getRole(),
				passive.getRepresentationType(), passive.getExpectedSpatialType(),
				passive.getSchemaId(), passive.getSchemaVersion(),
				passive.getProjectedPointGeoId(), passive.getFidelity(),
				passive.getCorrespondence(), passive.getRevision() + 1);
		replacePublishedRecordForRevisionTest(graph.registry, revisedPassive);
		runtime.onRecordsPublished(List.of(revisedPassive.getId()));

		SpatialPointPilotCertificate republished = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertNotSame(after, republished);
		assertEquals(SpatialCertificateStatus.VALID,
				republished.getSemanticCertificate().getStatus());
		assertTrue(republished.getRevisionTuple().containsKey(
				graph.nonDefiningBindingId));
		assertNotEquals(passiveRevision, republished.getRevisionTuple().get(
				graph.nonDefiningBindingId));
		assertEquals(after.getValueSnapshotToken(),
				republished.getValueSnapshotToken());
	}

	@Test
	void coherentDerivedBindingFailsOnlyTheProjectionDefinedObjectAuthority()
			throws ReflectiveOperationException {
		Graph graph = Graph.createWithTopology(getConstruction(), this::add,
				TopologyMode.NON_DEFINING_BINDING);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		ProjectionBindingRecord passive = (ProjectionBindingRecord) graph.registry
				.getRecord(graph.nonDefiningBindingId);
		GeoIdentityRecord projected = (GeoIdentityRecord) graph.registry.getRecord(
				passive.getProjectedPointGeoId());
		ProjectionBindingRecord derived = new ProjectionBindingRecord(passive.getId(), 2,
				passive.getObjectId(), passive.getSystemId(), passive.getDiagramMapId(),
				passive.getFrameId(), ProjectionBindingRole.DERIVED,
				passive.getRepresentationType(), passive.getExpectedSpatialType(),
				passive.getSchemaId(), passive.getSchemaVersion(),
				passive.getProjectedPointGeoId(), passive.getFidelity(),
				passive.getCorrespondence(), passive.getRevision() + 1);
		GeoIdentityRecord derivedProjected = new GeoIdentityRecord(projected.getId(),
				projected.getProvider(), projected.getFamily(), projected.getSchemaId(),
				projected.getSchemaVersion(), projected.getAuthority(),
				ProjectionBindingRole.DERIVED, projected.getStableOutputRole(),
				projected.getOutputCardinality(), projected.getDefinitionRevision() + 1,
				projected.getTopologyRevision() + 1);

		replacePublishedRecordForRevisionTest(graph.registry, derivedProjected);
		replacePublishedRecordForRevisionTest(graph.registry, derived);
		runtime.onRecordsPublished(List.of(derivedProjected.getId(), derived.getId()));

		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate point = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertEquals(SpatialCapabilityStatus.SUPPORTED,
				point.getSemanticCertificate().getCapabilityStatus());
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				point.getSemanticCertificate().getProjectionSystemCertificate().getStatus());
		assertEquals(SpatialDefinitionStatus.UNDEFINED,
				point.getSemanticCertificate().getDefinitionStatus());
		assertEquals(SpatialCertificateStatus.UNDEFINED,
				point.getSemanticCertificate().getStatus());
		assertEquals(SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				point.getSemanticCertificate().getCurrentnessStatus());
		assertFalse(point.getSemanticCertificate().hasPayload());
		assertFalse(runtime.getDerivedPoint(graph.objectId).isDefined());
	}

	@Test
	void inertVersionOnePassiveBindingCannotRemainAValidPoint()
			throws ReflectiveOperationException {
		Graph graph = Graph.createWithTopology(getConstruction(), this::add,
				TopologyMode.NON_DEFINING_BINDING);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		ProjectionBindingRecord passive = (ProjectionBindingRecord) graph.registry
				.getRecord(graph.nonDefiningBindingId);
		assertEquals(SpatialCertificateStatus.VALID,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		ProjectionBindingRecord inert = new ProjectionBindingRecord(passive.getId(), 1,
				passive.getObjectId(), passive.getSystemId(), passive.getDiagramMapId(),
				passive.getFrameId(), passive.getRole(), passive.getRepresentationType(),
				passive.getExpectedSpatialType(), passive.getSchemaId(),
				passive.getSchemaVersion(), passive.getProjectedGeoIds(),
				passive.getRevision() + 1);

		replacePublishedRecordForRevisionTest(graph.registry, inert);
		runtime.onRecordsPublished(List.of(inert.getId()));

		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate point = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertEquals(SpatialCapabilityStatus.UNSUPPORTED,
				point.getSemanticCertificate().getCapabilityStatus());
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				point.getSemanticCertificate().getProjectionSystemCertificate().getStatus());
		assertEquals(SpatialDefinitionStatus.UNDEFINED,
				point.getSemanticCertificate().getDefinitionStatus());
		assertEquals(SpatialCertificateStatus.NOT_EVALUATED,
				point.getSemanticCertificate().getStatus());
		assertEquals(SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				point.getSemanticCertificate().getCurrentnessStatus());
		assertFalse(point.getSemanticCertificate().hasPayload());
		assertFalse(runtime.getDerivedPoint(graph.objectId).isDefined());
	}

	@Test
	void mixedSourceFrameUnitsAreInconsistentWithoutImplicitConversion() {
		Graph graph = Graph.createWithTopology(getConstruction(), this::add,
				TopologyMode.MIXED_FRAME_UNITS);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();

		assertEquals(SpatialCapabilityStatus.SUPPORTED,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getCapabilityStatus());
		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertEquals(SpatialCertificateStatus.UNDEFINED,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertFalse(runtime.getSpatialPointCertificate(graph.objectId)
				.getSemanticCertificate().hasPayload());
		assertFalse(runtime.getDerivedPoint(graph.objectId).isDefined());
	}

	@Test
	void explicitHingeSupportIsLiveDagEvidenceWithNoStaleRecoveryPayload() {
		Graph graph = Graph.createWithHinge(getConstruction(), this::add, false);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();

		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
		long reconstructionAttempts = runtime.getInstrumentation()
				.getReconstructionAttempts();

		graph.setSupportEnd(1, 1, 0);
		getKernel().updateConstruction(false);

		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertFalse(runtime.getSpatialPointCertificate(graph.objectId)
				.getSemanticCertificate().hasPayload());
		assertFalse(runtime.getDerivedPoint(graph.objectId).isDefined());
		assertEquals(reconstructionAttempts,
				runtime.getInstrumentation().getReconstructionAttempts());

		graph.setSupportEnd(1, 0, 0);
		getKernel().updateConstruction(false);

		assertEquals(SpatialCertificateStatus.VALID,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
		assertEquals(0, runtime.getInstrumentation().getStalePayloadPublications());
	}

	@Test
	void nonPointHingeSupportProducesTypedUndefinedFailure() {
		Graph graph = Graph.createWithHinge(getConstruction(), this::add, true);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();

		assertEquals(ProjectionSystemStatus.UNDEFINED,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertFalse(runtime.getSpatialPointCertificate(graph.objectId)
				.getSemanticCertificate().hasPayload());
		assertFalse(runtime.getDerivedPoint(graph.objectId).isDefined());
	}

	@Test
	void changeOfPlaneRequiresAnExplicitAuxiliaryDestinationMap() {
		Graph graph = Graph.createWithChangeOfPlane(getConstruction(), this::add,
				true);
		ProjectionFrameRelationRecord relation = (ProjectionFrameRelationRecord)
				graph.registry.getRecord(graph.relationId);

		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime
						.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertEquals(SpatialCertificateStatus.VALID,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
		assertNull(relation.getFoldSignGeoId());
	}

	@Test
	void unboundAuxiliaryRelationGeometryRemainsInThePointDag()
			throws ReflectiveOperationException {
		Graph graph = Graph.create(getConstruction(), this::add);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		ProjectionSystemRecord system = (ProjectionSystemRecord) graph.registry
				.getRecord(graph.systemId);
		ProjectionBindingRecord horizontalBinding = (ProjectionBindingRecord)
				graph.registry.getRecord(graph.horizontalBindingId);
		ProjectionBindingRecord verticalBinding = (ProjectionBindingRecord)
				graph.registry.getRecord(graph.verticalBindingId);
		ProjectionDiagramMapRecord horizontalMap = (ProjectionDiagramMapRecord)
				graph.registry.getRecord(horizontalBinding.getDiagramMapId());
		ProjectionDiagramMapRecord verticalMap = (ProjectionDiagramMapRecord)
				graph.registry.getRecord(verticalBinding.getDiagramMapId());
		ProjectionFrameRecord verticalFrame = (ProjectionFrameRecord) graph.registry
				.getRecord(verticalBinding.getFrameId());
		ProjectionFrameRecord horizontalFrame = (ProjectionFrameRecord) graph.registry
				.getRecord(horizontalBinding.getFrameId());
		GeoPoint3D supportEnd = (GeoPoint3D) add("G9A2UnboundSupport=(1,0,0)");
		PersistentGeoId supportEndId = Graph.register(graph.registry, supportEnd);
		ProjectionFrameId auxiliaryFrameId = graph.registry.allocateProjectionFrameId();
		ProjectionDiagramMapId auxiliaryMapId =
				graph.registry.allocateProjectionDiagramMapId();
		ProjectionFrameRelationId relationId =
				graph.registry.allocateProjectionFrameRelationId();
		ProjectionFrameRecord auxiliaryFrame = new ProjectionFrameRecord(
				auxiliaryFrameId, 2, verticalFrame.getOriginGeoId(),
				verticalFrame.getUGeoId(), verticalFrame.getVGeoId(),
				verticalFrame.getFamily(), verticalFrame.getUnits(),
				verticalFrame.getHandedness(), verticalFrame.getFidelity(), 0);
		ProjectionDiagramMapRecord auxiliaryMap = new ProjectionDiagramMapRecord(
				auxiliaryMapId, 2, system.getId(), auxiliaryFrameId,
				ProjectionFrameUseRole.AUXILIARY, verticalMap.getFamily(),
				verticalMap.getOrientation(), verticalMap.getUnits(),
				verticalMap.getFidelity(), verticalMap.getA00GeoId(),
				verticalMap.getA01GeoId(), verticalMap.getA10GeoId(),
				verticalMap.getA11GeoId(), verticalMap.getB0GeoId(),
				verticalMap.getB1GeoId(), verticalMap.getDeclaredScaleGeoId(),
				List.of(relationId), 0);
		ProjectionFrameRelationRecord relation = new ProjectionFrameRelationRecord(
				relationId, 2, system.getId(), horizontalMap.getId(), auxiliaryMapId,
				ProjectionFrameRelationRecord.CHANGE_OF_PLANE,
				horizontalFrame.getOriginGeoId(), supportEndId,
				ProjectionFrameRelationRecord.POSITIVE_ORIENTATION,
				ProjectionFrameRelationRecord.EXPLICIT_CONSTRUCTION, null, 0);
		ProjectionDiagramMapRecord revisedHorizontal = new ProjectionDiagramMapRecord(
				horizontalMap.getId(), 2, system.getId(), horizontalMap.getFrameId(),
				horizontalMap.getFrameUseRole(), horizontalMap.getFamily(),
				horizontalMap.getOrientation(), horizontalMap.getUnits(),
				horizontalMap.getFidelity(), horizontalMap.getA00GeoId(),
				horizontalMap.getA01GeoId(), horizontalMap.getA10GeoId(),
				horizontalMap.getA11GeoId(), horizontalMap.getB0GeoId(),
				horizontalMap.getB1GeoId(), horizontalMap.getDeclaredScaleGeoId(),
				List.of(relationId), horizontalMap.getRevision() + 1);
		ArrayList<ProjectionDiagramMapId> mapIds = new ArrayList<>(system.getMapIds());
		mapIds.add(auxiliaryMapId);
		ProjectionSystemRecord revisedSystem = new ProjectionSystemRecord(system.getId(),
				2, mapIds, List.of(relationId), system.getUnits(),
				system.getAbsoluteTolerance(), system.getRelativeTolerance(),
				system.getRankTolerance(), system.getMapTolerance(),
				system.getHingeTolerance(), system.getConditionLimit(),
				system.getRevision() + 1);

		graph.registry.registerRecords(List.of(auxiliaryFrame, auxiliaryMap, relation));
		replacePublishedRecordForRevisionTest(graph.registry, revisedHorizontal);
		replacePublishedRecordForRevisionTest(graph.registry, revisedSystem);
		runtime.onRecordsPublished(List.of(auxiliaryFrameId, auxiliaryMapId, relationId,
				revisedHorizontal.getId(), revisedSystem.getId()));

		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate current = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertEquals(SpatialCertificateStatus.VALID,
				current.getSemanticCertificate().getStatus());
		assertTrue(current.getRevisionTuple().containsKey(auxiliaryFrameId));
		assertTrue(current.getRevisionTuple().containsKey(auxiliaryMapId));
		assertTrue(current.getRevisionTuple().containsKey(relationId));
		assertEquals(relationId.toExternalForm(), current.getSemanticCertificate()
				.getProjectionSystemCertificate().getRelationEvidence().get(0)
				.getEvidenceKey());
		assertTrue(current.getValueSnapshotToken().contains(
				supportEndId.toExternalForm()));
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);

		supportEnd.setCoords(1, 0, 1, 1);
		supportEnd.updateRepaint();
		getKernel().updateConstruction(false);

		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate broken = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				broken.getSemanticCertificate().getProjectionSystemCertificate().getStatus());
		assertEquals(SpatialCertificateStatus.UNDEFINED,
				broken.getSemanticCertificate().getStatus());
		assertFalse(broken.getSemanticCertificate().hasPayload());
		assertFalse(runtime.getDerivedPoint(graph.objectId).isDefined());
	}

	@Test
	void changeOfPlaneOnDefiningDestinationMapIsSupportedButInconsistent() {
		Graph graph = Graph.createWithChangeOfPlane(getConstruction(), this::add,
				false);
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();

		assertEquals(SpatialCapabilityStatus.SUPPORTED,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getCapabilityStatus());
		assertEquals(ProjectionSystemStatus.INCONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertEquals(SpatialCapabilityStatus.SUPPORTED,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getCapabilityStatus());
		assertEquals(SpatialCertificateStatus.UNDEFINED,
				runtime.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertFalse(runtime.getSpatialPointCertificate(graph.objectId)
				.getSemanticCertificate().hasPayload());
	}

	private static ProjectionDiagramMapRecord copyMapAtRevision(
			ProjectionDiagramMapRecord source, ProjectionDiagramMapId id,
			ProjectionSystemId systemId, long revision) {
		return new ProjectionDiagramMapRecord(id, source.getSemanticVersion(), systemId,
				source.getFrameId(), source.getFrameUseRole(), source.getFamily(),
				source.getOrientation(), source.getUnits(), source.getFidelity(),
				source.getA00GeoId(), source.getA01GeoId(), source.getA10GeoId(),
				source.getA11GeoId(), source.getB0GeoId(), source.getB1GeoId(),
				source.getDeclaredScaleGeoId(), source.getRelationIds(), revision);
	}

	@SuppressWarnings("unchecked")
	private static void replacePublishedRecordForRevisionTest(
			SpatialIdentityRegistry registry, SpatialIdentityRecord replacement)
			throws ReflectiveOperationException {
		// Records are immutable. This test emulates the already-validated atomic
		// publication of the same typed identity at its next revision without adding
		// a product-only mutation hook solely for the focused runtime test.
		Field recordsField = SpatialIdentityRegistry.class.getDeclaredField("records");
		recordsField.setAccessible(true);
		Map<SpatialIdentityId, SpatialIdentityRecord> records =
				(Map<SpatialIdentityId, SpatialIdentityRecord>) recordsField.get(registry);
		assertNotNull(records.put(replacement.getId(), replacement));
	}

	static final class SiblingGraph {
		final ProjectionSystemId systemId;
		final SpatialObjectId objectId;

		private SiblingGraph(ProjectionSystemId systemId, SpatialObjectId objectId) {
			this.systemId = systemId;
			this.objectId = objectId;
		}

		static SiblingGraph create(Graph source) {
			ProjectionSystemRecord sourceSystem = (ProjectionSystemRecord)
					source.registry.getRecord(source.systemId);
			ProjectionBindingRecord sourceHorizontalBinding =
					(ProjectionBindingRecord) source.registry.getRecord(
							source.horizontalBindingId);
			ProjectionBindingRecord sourceVerticalBinding =
					(ProjectionBindingRecord) source.registry.getRecord(
							source.verticalBindingId);
			ProjectionDiagramMapRecord sourceHorizontalMap =
					(ProjectionDiagramMapRecord) source.registry.getRecord(
							sourceHorizontalBinding.getDiagramMapId());
			ProjectionDiagramMapRecord sourceVerticalMap =
					(ProjectionDiagramMapRecord) source.registry.getRecord(
							sourceVerticalBinding.getDiagramMapId());
			ProjectionSystemId systemId = source.registry.allocateProjectionSystemId();
			ProjectionDiagramMapId horizontalMapId =
					source.registry.allocateProjectionDiagramMapId();
			ProjectionDiagramMapId verticalMapId =
					source.registry.allocateProjectionDiagramMapId();
			ProjectionBindingId horizontalBindingId =
					source.registry.allocateProjectionBindingId();
			ProjectionBindingId verticalBindingId =
					source.registry.allocateProjectionBindingId();
			SpatialObjectId objectId = source.registry.allocateSpatialObjectId();
			ProjectionSystemRecord system = new ProjectionSystemRecord(systemId, 2,
					List.of(horizontalMapId, verticalMapId), List.of(),
					sourceSystem.getUnits(), sourceSystem.getAbsoluteTolerance(),
					sourceSystem.getRelativeTolerance(), sourceSystem.getRankTolerance(),
					sourceSystem.getMapTolerance(), sourceSystem.getHingeTolerance(),
					sourceSystem.getConditionLimit(), sourceSystem.getRevision());
			ProjectionDiagramMapRecord horizontalMap = copySiblingMap(
					sourceHorizontalMap, horizontalMapId, systemId,
					sourceHorizontalMap.getRevision());
			ProjectionDiagramMapRecord verticalMap = copySiblingMap(
					sourceVerticalMap, verticalMapId, systemId,
					sourceVerticalMap.getRevision());
			ProjectionBindingRecord horizontalBinding = Graph.bindingRecord(
					horizontalBindingId, objectId, systemId, horizontalMapId,
					sourceHorizontalBinding.getFrameId(),
					sourceHorizontalBinding.getProjectedPointGeoId());
			ProjectionBindingRecord verticalBinding = Graph.bindingRecord(
					verticalBindingId, objectId, systemId, verticalMapId,
					sourceVerticalBinding.getFrameId(),
					sourceVerticalBinding.getProjectedPointGeoId());
			SpatialObjectRecord object = new SpatialObjectRecord(objectId, 2, "POINT",
					EditAuthorityMode.PROJECTION_DEFINED,
					SpatialObjectRecord.POINT_SCHEMA_ID, 1, systemId,
					List.of(horizontalBindingId, verticalBindingId), 0, 0);
			source.registry.registerRecords(List.of(system, horizontalMap, verticalMap,
					horizontalBinding, verticalBinding, object));
			return new SiblingGraph(systemId, objectId);
		}

		private static ProjectionDiagramMapRecord copySiblingMap(
				ProjectionDiagramMapRecord source, ProjectionDiagramMapId id,
				ProjectionSystemId systemId, long revision) {
			return new ProjectionDiagramMapRecord(id, source.getSemanticVersion(),
					systemId, source.getFrameId(), source.getFrameUseRole(),
					source.getFamily(), source.getOrientation(), source.getUnits(),
					source.getFidelity(), source.getA00GeoId(), source.getA01GeoId(),
					source.getA10GeoId(), source.getA11GeoId(), source.getB0GeoId(),
					source.getB1GeoId(), source.getDeclaredScaleGeoId(), List.of(),
					revision);
		}
	}

	static final class Graph {
		private static final String TEST_PROVIDER = "g9a2.test";
		private static final String UNIT = "model-unit";
		final SpatialIdentityRegistry registry;
		final ProjectionSystemId systemId;
		final SpatialObjectId objectId;
		final ProjectionBindingId horizontalBindingId;
		final ProjectionBindingId verticalBindingId;
		final ProjectionBindingId nonDefiningBindingId;
		final PersistentGeoId horizontalPointId;
		final GeoElement horizontalPointGeo;
		final GeoPointND verticalPoint;
		final GeoPointND nonDefiningPoint;
		final GeoNumeric one;
		final GeoNumeric zero;
		final GeoNumeric axisControl;
		final ProjectionFrameRelationId relationId;
		final GeoPoint3D supportEnd;

		private Graph(SpatialIdentityRegistry registry, ProjectionSystemId systemId,
				SpatialObjectId objectId, ProjectionBindingId horizontalBindingId,
				ProjectionBindingId verticalBindingId,
				ProjectionBindingId nonDefiningBindingId,
				PersistentGeoId horizontalPointId, GeoElement horizontalPointGeo,
				GeoPointND verticalPoint, GeoPointND nonDefiningPoint, GeoNumeric one,
				GeoNumeric zero, GeoNumeric axisControl,
				ProjectionFrameRelationId relationId, GeoPoint3D supportEnd) {
			this.registry = registry;
			this.systemId = systemId;
			this.objectId = objectId;
			this.horizontalBindingId = horizontalBindingId;
			this.verticalBindingId = verticalBindingId;
			this.nonDefiningBindingId = nonDefiningBindingId;
			this.horizontalPointId = horizontalPointId;
			this.horizontalPointGeo = horizontalPointGeo;
			this.verticalPoint = verticalPoint;
			this.nonDefiningPoint = nonDefiningPoint;
			this.one = one;
			this.zero = zero;
			this.axisControl = axisControl;
			this.relationId = relationId;
			this.supportEnd = supportEnd;
		}

		static Graph create(Construction construction,
				Function<String, GeoElement> add) {
			return create(construction, add, EditAuthorityMode.PROJECTION_DEFINED,
					ProjectionBindingRole.DEFINING, RelationMode.NONE,
					TopologyMode.NORMAL);
		}

		static Graph create(Construction construction,
				Function<String, GeoElement> add,
				EditAuthorityMode projectedAuthority,
				ProjectionBindingRole projectedRole) {
			return create(construction, add, projectedAuthority, projectedRole,
					RelationMode.NONE, TopologyMode.NORMAL);
		}

		static Graph createWithTopology(Construction construction,
				Function<String, GeoElement> add, TopologyMode topologyMode) {
			return create(construction, add, EditAuthorityMode.PROJECTION_DEFINED,
					ProjectionBindingRole.DEFINING, RelationMode.NONE, topologyMode);
		}

		static Graph createWithHinge(Construction construction,
				Function<String, GeoElement> add, boolean wrongSupportType) {
			return create(construction, add, EditAuthorityMode.PROJECTION_DEFINED,
					ProjectionBindingRole.DEFINING, wrongSupportType
							? RelationMode.HINGE_WRONG_SUPPORT_TYPE : RelationMode.HINGE,
					TopologyMode.NORMAL);
		}

		static Graph createProductiveWithHinge(Construction construction,
				Function<String, GeoElement> add) {
			return create(construction, add, EditAuthorityMode.PROJECTION_DEFINED,
					ProjectionBindingRole.DEFINING, RelationMode.HINGE,
					TopologyMode.NORMAL,
					SpatialPointPilotRedefineProvider.PROVIDER_ID);
		}

		static Graph createWithChangeOfPlane(Construction construction,
				Function<String, GeoElement> add, boolean auxiliaryDestination) {
			return create(construction, add, EditAuthorityMode.PROJECTION_DEFINED,
					ProjectionBindingRole.DEFINING, auxiliaryDestination
							? RelationMode.CHANGE_OF_PLANE
							: RelationMode.CHANGE_OF_PLANE_WRONG_ROLE,
					TopologyMode.NORMAL);
		}

		private static Graph create(Construction construction,
				Function<String, GeoElement> add,
				EditAuthorityMode projectedAuthority,
				ProjectionBindingRole projectedRole, RelationMode relationMode,
				TopologyMode topologyMode) {
			return create(construction, add, projectedAuthority, projectedRole,
					relationMode, topologyMode, TEST_PROVIDER);
		}

		private static Graph create(Construction construction,
				Function<String, GeoElement> add,
				EditAuthorityMode projectedAuthority,
				ProjectionBindingRole projectedRole, RelationMode relationMode,
				TopologyMode topologyMode, String providerId) {
			SpatialIdentityRegistry registry = construction.getSpatialIdentityRegistry();
			GeoElement origin = add.apply("G9A2O=(0,0,0)");
			GeoElement u = add.apply("G9A2U=Vector((0,0,0),(1,0,0))");
			GeoElement horizontalV = add.apply(
					"G9A2VH=Vector((0,0,0),(0,1,0))");
			GeoNumeric axisControl = (GeoNumeric) add.apply("G9A2Axis=0");
			GeoElement verticalV = add.apply(
					"G9A2VV=Vector((0,0,0),(0,G9A2Axis,1-G9A2Axis))");
			GeoNumeric one;
			GeoNumeric zero;
			if (topologyMode == TopologyMode.SNAPSHOT_MISMATCH) {
				one = new ArmedSnapshotNumeric(construction, 1);
				one.setLabel("G9A2One");
				zero = new ArmedSnapshotNumeric(construction, 0);
				zero.setLabel("G9A2Zero");
			} else {
				one = (GeoNumeric) add.apply("G9A2One=1");
				zero = (GeoNumeric) add.apply("G9A2Zero=0");
			}
			GeoElement horizontalPoint = add.apply("G9A2PH=(2,3)");
			GeoPointND verticalPoint = (GeoPointND) add.apply("G9A2PV=(2,5)");
			GeoPointND nonDefiningPoint =
					topologyMode == TopologyMode.NON_DEFINING_BINDING
							? (GeoPointND) add.apply("G9A2PP=(9,11)") : null;
			GeoPoint3D supportEnd = relationMode == RelationMode.NONE ? null
					: (GeoPoint3D) add.apply("G9A2HE=(1,0,0)");

			PersistentGeoId originId = register(registry, origin, providerId);
			PersistentGeoId uId = register(registry, u, providerId);
			PersistentGeoId horizontalVId = register(registry, horizontalV, providerId);
			PersistentGeoId verticalVId = register(registry, verticalV, providerId);
			register(registry, axisControl, providerId);
			PersistentGeoId oneId = register(registry, one, providerId);
			PersistentGeoId zeroId = register(registry, zero, providerId);
			PersistentGeoId horizontalPointId = register(registry, horizontalPoint,
					projectedAuthority, projectedRole, providerId);
			PersistentGeoId verticalPointId;
			if (topologyMode == TopologyMode.MISSING_PROJECTED_IDENTITY) {
				verticalPointId = registry.allocatePersistentGeoId();
			} else {
				verticalPointId = register(registry, (GeoElement) verticalPoint,
						projectedAuthority, projectedRole, providerId);
			}
			PersistentGeoId supportEndId = supportEnd == null ? null
					: register(registry, supportEnd, providerId);
			PersistentGeoId nonDefiningPointId = nonDefiningPoint == null ? null
					: register(registry, (GeoElement) nonDefiningPoint,
							EditAuthorityMode.PROJECTION_DEFINED,
							ProjectionBindingRole.PRESENTATION, providerId);

			ProjectionFrameId horizontalFrameId = registry.allocateProjectionFrameId();
			ProjectionFrameId verticalFrameId = registry.allocateProjectionFrameId();
			ProjectionSystemId systemId = registry.allocateProjectionSystemId();
			ProjectionDiagramMapId horizontalMapId =
					registry.allocateProjectionDiagramMapId();
			ProjectionDiagramMapId verticalMapId =
					registry.allocateProjectionDiagramMapId();
			SpatialObjectId objectId = registry.allocateSpatialObjectId();
			ProjectionBindingId horizontalBindingId =
					registry.allocateProjectionBindingId();
			ProjectionBindingId verticalBindingId =
					registry.allocateProjectionBindingId();
			ProjectionBindingId nonDefiningBindingId = nonDefiningPoint == null ? null
					: registry.allocateProjectionBindingId();
			ProjectionFrameRelationId relationId = relationMode == RelationMode.NONE
					? null : registry.allocateProjectionFrameRelationId();
			List<ProjectionFrameRelationId> relationIds = relationId == null
					? List.of() : List.of(relationId);

			ProjectionFrameRecord horizontalFrame = new ProjectionFrameRecord(
					horizontalFrameId, 2, originId, uId, horizontalVId,
					topologyMode == TopologyMode.UNSUPPORTED_FRAME_FAMILY
							? "PERSPECTIVE" : "ORTHOGRAPHIC",
					UNIT, "RIGHT_HANDED", "EXACT", 0);
			ProjectionFrameRecord verticalFrame = new ProjectionFrameRecord(
					verticalFrameId, 2, originId, uId, verticalVId, "ORTHOGRAPHIC",
					topologyMode == TopologyMode.MIXED_FRAME_UNITS
							? "millimetre" : UNIT,
					"RIGHT_HANDED", "EXACT", 0);
			ProjectionSystemRecord system = new ProjectionSystemRecord(systemId, 2,
					List.of(horizontalMapId, verticalMapId), relationIds, UNIT,
					1e-9, 1e-9, 1e-12, 1e-9, 1e-9, 1e10, 0);
			ProjectionDiagramMapRecord horizontalMap = mapRecord(horizontalMapId,
					systemId, horizontalFrameId, oneId, zeroId,
					ProjectionFrameUseRole.DEFINING, relationIds);
			ProjectionFrameUseRole verticalRole =
					relationMode == RelationMode.CHANGE_OF_PLANE
							? ProjectionFrameUseRole.AUXILIARY
							: ProjectionFrameUseRole.DEFINING;
			ProjectionDiagramMapRecord verticalMap = mapRecord(verticalMapId,
					systemId, verticalFrameId, oneId, zeroId, verticalRole, relationIds);
			ProjectionBindingRecord horizontalBinding = bindingRecord(
					horizontalBindingId, objectId, systemId, horizontalMapId,
					horizontalFrameId, horizontalPointId);
			ProjectionBindingRecord verticalBinding = bindingRecord(verticalBindingId,
					objectId, systemId, verticalMapId, verticalFrameId, verticalPointId,
					ProjectionBindingRole.DEFINING);
			ProjectionBindingRecord nonDefiningBinding = nonDefiningBindingId == null
					? null : bindingRecord(nonDefiningBindingId, objectId, systemId,
							horizontalMapId, horizontalFrameId, nonDefiningPointId,
							ProjectionBindingRole.PRESENTATION);
			ArrayList<ProjectionBindingId> objectBindingIds = new ArrayList<>(
					List.of(horizontalBindingId, verticalBindingId));
			if (nonDefiningBindingId != null) {
				objectBindingIds.add(nonDefiningBindingId);
			}
			SpatialObjectRecord object = new SpatialObjectRecord(objectId, 2, "POINT",
					EditAuthorityMode.PROJECTION_DEFINED,
					SpatialObjectRecord.POINT_SCHEMA_ID, 1, systemId,
					objectBindingIds, 0, 0);

			ArrayList<SpatialIdentityRecord> records = new ArrayList<>(List.of(
					horizontalFrame, verticalFrame, system, horizontalMap, verticalMap,
					horizontalBinding, verticalBinding));
			if (nonDefiningBinding != null) {
				records.add(nonDefiningBinding);
			}
			records.add(object);
			if (relationId != null) {
				boolean change = relationMode == RelationMode.CHANGE_OF_PLANE
						|| relationMode == RelationMode.CHANGE_OF_PLANE_WRONG_ROLE;
				PersistentGeoId declaredSupportEnd =
						relationMode == RelationMode.HINGE_WRONG_SUPPORT_TYPE
								? oneId : supportEndId;
				records.add(new ProjectionFrameRelationRecord(relationId, 2, systemId,
						horizontalMapId, verticalMapId,
						change ? ProjectionFrameRelationRecord.CHANGE_OF_PLANE
								: ProjectionFrameRelationRecord.HINGE_UNFOLD,
						originId, declaredSupportEnd,
						ProjectionFrameRelationRecord.POSITIVE_ORIENTATION,
						ProjectionFrameRelationRecord.EXPLICIT_CONSTRUCTION,
						change ? null : oneId, 0));
			}
			registry.registerRecords(records);
			return new Graph(registry, systemId, objectId, horizontalBindingId,
					verticalBindingId, nonDefiningBindingId, horizontalPointId,
					horizontalPoint, verticalPoint, nonDefiningPoint, one, zero, axisControl,
					relationId, supportEnd);
		}

		void setVerticalProjection(double x, double y) {
			verticalPoint.setCoords(x, y, 1);
			((GeoElement) verticalPoint).updateRepaint();
		}

		void setNonDefiningProjection(double x, double y) {
			nonDefiningPoint.setCoords(x, y, 1);
			((GeoElement) nonDefiningPoint).updateRepaint();
		}

		void setSupportEnd(double x, double y, double z) {
			supportEnd.setCoords(x, y, z, 1);
			supportEnd.updateRepaint();
		}

		void setRankLoss(boolean rankLoss) {
			verticalPoint.setCoords(2, rankLoss ? 3 : 5, 1);
			((GeoElement) verticalPoint).updateRepaint();
			axisControl.setValue(rankLoss ? 1 : 0);
			axisControl.updateRepaint();
		}

		private static PersistentGeoId register(SpatialIdentityRegistry registry,
				GeoElement geo) {
			return register(registry, geo, EditAuthorityMode.PROJECTION_DEFINED,
					ProjectionBindingRole.DEFINING, TEST_PROVIDER);
		}

		private static PersistentGeoId register(SpatialIdentityRegistry registry,
				GeoElement geo, String providerId) {
			return register(registry, geo, EditAuthorityMode.PROJECTION_DEFINED,
					ProjectionBindingRole.DEFINING, providerId);
		}

		private static PersistentGeoId register(SpatialIdentityRegistry registry,
				GeoElement geo, EditAuthorityMode authority,
				ProjectionBindingRole role) {
			return register(registry, geo, authority, role, TEST_PROVIDER);
		}

		private static PersistentGeoId register(SpatialIdentityRegistry registry,
				GeoElement geo, EditAuthorityMode authority,
				ProjectionBindingRole role, String providerId) {
			PersistentGeoId id = registry.allocatePersistentGeoId();
			registry.registerParticipation(geo, new GeoIdentityRecord(id, providerId,
					"SEMANTIC_INPUT", SpatialObjectRecord.POINT_SCHEMA_ID, 1,
					authority, role, "INPUT", 1, 0, 0));
			return id;
		}

		private static ProjectionDiagramMapRecord mapRecord(
				ProjectionDiagramMapId mapId, ProjectionSystemId systemId,
				ProjectionFrameId frameId, PersistentGeoId oneId,
				PersistentGeoId zeroId, ProjectionFrameUseRole role,
				List<ProjectionFrameRelationId> relationIds) {
			return new ProjectionDiagramMapRecord(mapId, 2, systemId, frameId,
					role, "ORIENTED_ISOMETRY",
					"PRESERVING", UNIT, "EXACT", oneId, zeroId, zeroId, oneId,
					zeroId, zeroId, oneId, relationIds, 0);
		}

		private static ProjectionBindingRecord bindingRecord(ProjectionBindingId id,
				SpatialObjectId objectId, ProjectionSystemId systemId,
				ProjectionDiagramMapId mapId, ProjectionFrameId frameId,
				PersistentGeoId pointId) {
			return bindingRecord(id, objectId, systemId, mapId, frameId, pointId,
					ProjectionBindingRole.DEFINING);
		}

		private static ProjectionBindingRecord bindingRecord(ProjectionBindingId id,
				SpatialObjectId objectId, ProjectionSystemId systemId,
				ProjectionDiagramMapId mapId, ProjectionFrameId frameId,
				PersistentGeoId pointId, ProjectionBindingRole role) {
			return new ProjectionBindingRecord(id, 2, objectId, systemId, mapId,
					frameId, role, "POINT", "POINT",
					SpatialObjectRecord.POINT_SCHEMA_ID, 1, pointId, "EXACT",
					"NOT_REQUIRED", 0);
		}

		private enum RelationMode {
			NONE,
			HINGE,
			HINGE_WRONG_SUPPORT_TYPE,
			CHANGE_OF_PLANE,
			CHANGE_OF_PLANE_WRONG_ROLE
		}
	}

	private enum TopologyMode {
		NORMAL,
		UNSUPPORTED_FRAME_FAMILY,
		MISSING_PROJECTED_IDENTITY,
		NON_DEFINING_BINDING,
		MIXED_FRAME_UNITS,
		SNAPSHOT_MISMATCH
	}

	private static final class ArmedSnapshotNumeric extends GeoNumeric {
		private boolean armed;
		private int classTypeReads;

		ArmedSnapshotNumeric(Construction construction, double value) {
			super(construction, value);
		}

		void armSecondCaptureMutation() {
			armed = true;
			classTypeReads = 0;
		}

		void restore(double value) {
			armed = false;
			setValue(value);
			updateRepaint();
		}

		@Override
		public GeoClass getGeoClassType() {
			GeoClass type = super.getGeoClassType();
			if (armed && ++classTypeReads == 2) {
				armed = false;
				setValue(getDouble() + 1);
			}
			return type;
		}
	}

	private static final class DerivedPointConsumer extends AlgoElement {
		private final GeoPoint3D source;
		private final GeoNumeric value;
		private int computeCount;

		DerivedPointConsumer(Construction construction, GeoPoint3D source) {
			super(construction, false);
			this.source = source;
			this.value = new GeoNumeric(construction);
			setInputOutput();
			setDependencies();
			compute();
		}

		@Override
		protected void setInputOutput() {
			input = new GeoElement[] {source};
			setOnlyOutput(value);
		}

		@Override
		public void compute() {
			computeCount++;
			if (!source.isDefined()) {
				value.setUndefined();
				return;
			}
			value.setValue(source.getInhomX() + source.getInhomY()
					+ source.getInhomZ());
		}

		GeoNumeric getValue() {
			return value;
		}

		int getComputeCount() {
			return computeCount;
		}

		@Override
		public Algos getClassName() {
			return Algos.Expression;
		}
	}

	private void assertRejectedProjectedContext(Graph graph) {
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		SpatialPointPilotCertificate point = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertNotNull(point);
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(graph.systemId)
						.getSemanticCertificate().getStatus());
		assertEquals(SpatialCapabilityStatus.SUPPORTED,
				point.getSemanticCertificate().getCapabilityStatus());
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				point.getSemanticCertificate().getProjectionSystemCertificate().getStatus());
		assertEquals(SpatialDefinitionStatus.UNDEFINED,
				point.getSemanticCertificate().getDefinitionStatus());
		assertEquals(SpatialCertificateStatus.UNDEFINED,
				point.getSemanticCertificate().getStatus());
		assertEquals(SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				point.getSemanticCertificate().getCurrentnessStatus());
		assertFalse(point.getSemanticCertificate().hasPayload());
		assertNotNull(runtime.getDerivedPoint(graph.objectId));
		assertFalse(runtime.getDerivedPoint(graph.objectId).isDefined());
		assertEquals(0, runtime.getInstrumentation().getReconstructionAttempts());
		assertEquals(0, runtime.getInstrumentation().getRankEvaluations());
		assertEquals(0, runtime.getInstrumentation().getCandidateObjectsBuilt());
	}

	private static void assertPresentationInvariant(Graph graph,
			SpatialSemanticRuntime runtime,
			ProjectionSystemPilotCertificate expectedSystem,
			SpatialPointPilotCertificate expectedPoint,
			List<SpatialIdentityId> recordIds,
			List<SpatialIdentityRecord> expectedRecords,
			List<Long> expectedCounters) {
		ProjectionSystemPilotCertificate actualSystem =
				runtime.getProjectionSystemCertificate(graph.systemId);
		SpatialPointPilotCertificate actualPoint = runtime.getSpatialPointCertificate(
				graph.objectId);
		assertSame(expectedSystem, actualSystem);
		assertSame(expectedPoint, actualPoint);
		assertEquals(expectedSystem.getRevisionTuple(), actualSystem.getRevisionTuple());
		assertEquals(expectedSystem.getValueSnapshotToken(),
				actualSystem.getValueSnapshotToken());
		assertEquals(expectedSystem.getSemanticCertificate()
				.getMaximumNormalizedResidual(), actualSystem.getSemanticCertificate()
						.getMaximumNormalizedResidual(), 0);
		assertEquals(expectedSystem.getSemanticCertificate().getMapEvidence().size(),
				actualSystem.getSemanticCertificate().getMapEvidence().size());
		assertEquals(expectedPoint.getRevisionTuple(), actualPoint.getRevisionTuple());
		assertEquals(expectedPoint.getValueSnapshotToken(),
				actualPoint.getValueSnapshotToken());
		assertEquals(expectedPoint.getSemanticCertificate()
				.getMaximumIntrinsicNormalizedResidual(),
				actualPoint.getSemanticCertificate()
						.getMaximumIntrinsicNormalizedResidual(), 0);
		assertEquals(expectedPoint.getSemanticCertificate()
				.getMaximumDiagramNormalizedResidual(),
				actualPoint.getSemanticCertificate()
						.getMaximumDiagramNormalizedResidual(), 0);
		assertEquals(expectedPoint.getSemanticCertificate().getResidualEvidence().size(),
				actualPoint.getSemanticCertificate().getResidualEvidence().size());
		for (int index = 0; index < recordIds.size(); index++) {
			assertSame(expectedRecords.get(index),
					graph.registry.getRecord(recordIds.get(index)));
		}
		assertEquals(expectedCounters, instrumentationCounters(runtime));
		assertPoint(runtime.getDerivedPoint(graph.objectId), 2, 3, 5);
	}

	private static List<Long> instrumentationCounters(SpatialSemanticRuntime runtime) {
		return List.of(
				runtime.getInstrumentation().getFrameEvaluations(),
				runtime.getInstrumentation().getProjectionSystemEvaluations(),
				runtime.getInstrumentation().getDiagramMapForwardEvaluations(),
				runtime.getInstrumentation().getDiagramMapInverseEvaluations(),
				runtime.getInstrumentation().getHingeConsistencyEvaluations(),
				runtime.getInstrumentation().getChangeOfPlaneConsistencyEvaluations(),
				runtime.getInstrumentation().getProjectionSystemCertificatePublications(),
				runtime.getInstrumentation().getProjectionSystemCertificateRejections(),
				runtime.getInstrumentation().getReconstructionAttempts(),
				runtime.getInstrumentation().getRankEvaluations(),
				runtime.getInstrumentation().getCandidateObjectsBuilt(),
				runtime.getInstrumentation().getReprojectionEvaluations(),
				runtime.getInstrumentation().getCertificatePublications(),
				runtime.getInstrumentation().getFailurePublications(),
				runtime.getInstrumentation().getSupersededCandidateRejections(),
				runtime.getInstrumentation().getDependencyUpdates(),
				runtime.getInstrumentation().getDerivedViewPublications(),
				runtime.getInstrumentation().getDerivedViewWithdrawals());
	}

	private static void assertPoint(GeoPoint3D point, double x, double y, double z) {
		assertNotNull(point);
		assertTrue(point.isDefined());
		assertEquals(x, point.getInhomX(), 1e-8);
		assertEquals(y, point.getInhomY(), 1e-8);
		assertEquals(z, point.getInhomZ(), 1e-8);
	}

	private static void assertVectorEquals(Vector3 expected, Vector3 actual) {
		assertEquals(expected.getX(), actual.getX(), 1e-8);
		assertEquals(expected.getY(), actual.getY(), 1e-8);
		assertEquals(expected.getZ(), actual.getZ(), 1e-8);
	}
}
