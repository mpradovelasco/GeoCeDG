/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.ProjectionDiagramMapRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRelationRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityInstrumentation;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleMutation;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleOperationKind;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleTransaction;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialPointLifecycleService;
import org.geocedg.common.kernel.spatial.identity.SpatialResolutionState;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.junit.jupiter.api.Test;

/** Canonical G9A3 AUTH01--AUTH03 lifecycle-authority scenarios. */
class G9A3SpatialLifecycleInstrumentationTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D(new AppConfigGeoCeDG());
	}

	@Test
	void auth01AuthorityHardZerosAndTransactionCountersMatchExactOutcomes() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		SpatialIdentityInstrumentation identity = registry().getInstrumentation();
		SpatialSemanticInstrumentation semantic = semanticInstrumentation();
		identity.reset();
		semantic.reset();

		ProjectionBindingId committedId = rerole(graph, graph.horizontalBindingId,
				ProjectionBindingRole.PRESENTATION, "G9A3-AUTH01-commit", true);
		long semanticEvaluations = semantic.getFrameEvaluations()
				+ semantic.getProjectionSystemEvaluations()
				+ semantic.getReconstructionAttempts();
		assertTrue(semanticEvaluations > 0);

		ProjectionBindingRecord committed = binding(committedId);
		SpatialObjectRecord currentObject = object(graph);
		ProjectionBindingId rejectedId = registry().allocateProjectionBindingId();
		ProjectionBindingRecord rejected = committed.asFreshReroled(rejectedId,
				ProjectionBindingRole.DEFINING);
		SpatialObjectRecord hostileObject = currentObject.withBindingsAndRevisions(
				currentObject.getBindingIds(), currentObject.getDefinitionRevision() + 1,
				currentObject.getTopologyRevision() + 1);
		G9A3SpatialGraphSnapshot.Snapshot beforeReject = snapshot();
		assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareBindingRerole(currentObject, committed,
						rejected, hostileObject, "G9A3-AUTH01-reject"));
		assertEquals(beforeReject, snapshot());
		assertEquals(semanticEvaluations, semantic.getFrameEvaluations()
				+ semantic.getProjectionSystemEvaluations()
				+ semantic.getReconstructionAttempts());

		ProjectionBindingId rolledBackId = registry().allocateProjectionBindingId();
		ProjectionBindingRecord rolledBack = committed.asFreshReroled(rolledBackId,
				ProjectionBindingRole.DEFINING);
		SpatialObjectRecord rollbackObject = currentObject.withBindingsAndRevisions(
				replace(currentObject.getBindingIds(), committedId, rolledBackId),
				currentObject.getDefinitionRevision() + 1,
				currentObject.getTopologyRevision() + 1);
		SpatialLifecycleTransaction transaction = lifecycle().prepareBindingRerole(
				currentObject, committed, rolledBack, rollbackObject,
				"G9A3-AUTH01-rollback");
		assertEquals(beforeReject, snapshot());
		transaction.rollback();
		assertEquals(beforeReject, snapshot());
		assertNull(registry().getRecord(rolledBackId));
		assertEquals(0, registry().getReservedIdentityCount());

		assertEquals(3, identity.getLifecyclePreparationAttempts());
		assertEquals(2, identity.getLifecyclePrepared());
		assertEquals(1, identity.getLifecyclePreflightRejects());
		assertEquals(1, identity.getLifecycleCommits());
		assertEquals(1, identity.getLifecycleRollbacks());
		assertEquals(1, identity.getLifecycleRecordCreates());
		assertEquals(2, identity.getLifecycleRecordReplacements());
		assertEquals(1, identity.getLifecycleRecordRetirements());
		assertTrue(identity.getLifecycleResolutionChanges() >= 2);
		assertHardZeroAuthorities(identity, semantic);
	}

	@Test
	void auth02ForbiddenScopeAndGenericSpecialOperationBypassesAreSealed() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
		List<SpatialLifecycleOperationKind> sealedKinds = List.of(
				SpatialLifecycleOperationKind.SEMANTIC_NO_OP,
				SpatialLifecycleOperationKind.COMPATIBLE_DEFINITION_CHANGE,
				SpatialLifecycleOperationKind.ADMITTED_TOPOLOGY_CHANGE,
				SpatialLifecycleOperationKind.TRUE_REPLACEMENT,
				SpatialLifecycleOperationKind.EXPLICIT_MIGRATION,
				SpatialLifecycleOperationKind.COMPLETE_CLOSURE_COPY,
				SpatialLifecycleOperationKind.DECLARED_EXTERNAL_COPY,
				SpatialLifecycleOperationKind.REFERENCE_RECOVERY);
		for (SpatialLifecycleOperationKind kind : sealedKinds) {
			SpatialIdentityException failure = assertThrows(
					SpatialIdentityException.class,
					() -> lifecycle().prepareMutation(SpatialLifecycleMutation.builder(
							kind, "G9A3-AUTH02-direct-" + kind.name()).build()));
			assertEquals(SpatialIdentityDiagnostic.Code.LIFECYCLE_SCOPE_VIOLATION,
					failure.getDiagnostic().getCode());
			assertEquals(before, snapshot());
		}

		for (SpatialIdentityRecord record : registry().getRecords()) {
			assertTrue(record instanceof GeoIdentityRecord
					|| record instanceof SpatialObjectRecord
					|| record instanceof ProjectionFrameRecord
					|| record instanceof ProjectionSystemRecord
					|| record instanceof ProjectionDiagramMapRecord
					|| record instanceof ProjectionFrameRelationRecord
					|| record instanceof ProjectionBindingRecord);
			if (record instanceof SpatialObjectRecord) {
				assertEquals("POINT", ((SpatialObjectRecord) record).getSpatialType());
			}
		}
		assertEquals(SpatialCertificateStatus.VALID,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertEquals(0, registry().getReservedIdentityCount());
	}

	@Test
	void auth03HostileReroleSequenceRemainsBoundedReciprocalAndCurrent() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		int baselineSize = registry().size();
		ProjectionBindingId currentId = graph.horizontalBindingId;
		for (int iteration = 0; iteration < 12; iteration++) {
			String hostileToken = "G9A3-AUTH03-hostile-" + iteration;
			ProjectionBindingRecord current = binding(currentId);
			SpatialObjectRecord currentObject = object(graph);
			ProjectionBindingId hostileId = registry().allocateProjectionBindingId();
			ProjectionBindingRecord hostile = current.asFreshReroled(hostileId,
					ProjectionBindingRole.PRESENTATION);
			SpatialObjectRecord unchangedMembership = currentObject
					.withBindingsAndRevisions(currentObject.getBindingIds(),
							currentObject.getDefinitionRevision() + 1,
							currentObject.getTopologyRevision() + 1);
			G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
			assertThrows(SpatialIdentityException.class,
					() -> lifecycle().prepareBindingRerole(currentObject, current,
							hostile, unchangedMembership,
							hostileToken));
			assertEquals(before, snapshot());
			assertEquals(0, registry().getReservedIdentityCount());

			currentId = rerole(graph, currentId, ProjectionBindingRole.PRESENTATION,
					"G9A3-AUTH03-presentation-" + iteration, true);
			currentId = rerole(graph, currentId, ProjectionBindingRole.DEFINING,
					"G9A3-AUTH03-defining-" + iteration, true);
			assertEquals(baselineSize, registry().size());
			assertReciprocalActiveGraph();
		}

		G9A3SpatialGraphSnapshot.Snapshot finalState = snapshot();
		getKernel().updateConstruction(false);
		assertEquals(finalState, snapshot());
		assertEquals(SpatialCertificateStatus.VALID,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId).isCurrentRevision());
		assertEquals(0, registry().getReservedIdentityCount());
		assertEquals(0, semanticInstrumentation().getStalePayloadPublications());
	}

	private ProjectionBindingId rerole(
			G9A2SpatialSemanticRuntimeTest.Graph graph,
			ProjectionBindingId currentId, ProjectionBindingRole newRole,
			String token, boolean commit) {
		ProjectionBindingRecord current = binding(currentId);
		ProjectionBindingId freshId = registry().allocateProjectionBindingId();
		ProjectionBindingRecord replacement = current.asFreshReroled(freshId,
				newRole);
		SpatialObjectRecord currentObject = object(graph);
		SpatialObjectRecord replacementObject = currentObject.withBindingsAndRevisions(
				replace(currentObject.getBindingIds(), currentId, freshId),
				currentObject.getDefinitionRevision() + 1,
				currentObject.getTopologyRevision() + 1);
		SpatialLifecycleTransaction transaction = lifecycle().prepareBindingRerole(
				currentObject, current, replacement, replacementObject, token);
		if (commit) {
			transaction.commit();
		}
		return freshId;
	}

	private void assertReciprocalActiveGraph() {
		for (SpatialIdentityRecord record : registry().getRecords()) {
			assertEquals(SpatialResolutionState.ACTIVE,
					registry().getResolution(record.getId()).getState());
			for (org.geocedg.common.kernel.spatial.identity.SpatialIdentityId reference
					: record.getReferences()) {
				assertNotNull(registry().getRecord(reference));
			}
			if (record instanceof SpatialObjectRecord) {
				SpatialObjectRecord object = (SpatialObjectRecord) record;
				for (ProjectionBindingId bindingId : object.getBindingIds()) {
					assertEquals(object.getId(),
							((ProjectionBindingRecord) registry().getRecord(bindingId))
									.getObjectId());
				}
			}
		}
	}

	private static void assertHardZeroAuthorities(
			SpatialIdentityInstrumentation identity,
			SpatialSemanticInstrumentation semantic) {
		assertEquals(0, identity.getLabelAuthorityUses());
		assertEquals(0, identity.getCoordinateAuthorityUses());
		assertEquals(0, identity.getConstructionOrderAuthorityUses());
		assertEquals(0, identity.getXmlPositionAuthorityUses());
		assertEquals(0, identity.getOutputOrdinalAuthorityUses());
		assertEquals(0, identity.getJavaInstanceAuthorityUses());
		assertEquals(0, identity.getViewportAuthorityUses());
		assertEquals(0, identity.getDpiAuthorityUses());
		assertEquals(0, identity.getCameraAuthorityUses());
		assertEquals(0, identity.getRendererAuthorityUses());
		assertEquals(0, identity.getScreenStateAuthorityUses());
		assertEquals(0, semantic.getLabelFallbackLookups());
		assertEquals(0, semantic.getCoordinateAssociationAttempts());
		assertEquals(0, semantic.getCreationOrderAssociationAttempts());
		assertEquals(0, semantic.getXmlPositionAssociationAttempts());
		assertEquals(0, semantic.getOutputIndexAssociationAttempts());
		assertEquals(0, semantic.getJavaReferenceIdentityAssumptions());
		assertEquals(0, semantic.getVisibleDiagramAssociationAttempts());
		assertEquals(0, semantic.getStalePayloadPublications());
		assertEquals(0, semantic.getMixedAuthorityRevisionPublications());
		assertEquals(0, semantic.getHiddenGraphRecomputations());
		assertEquals(0, semantic.getRenderCacheReads());
		assertEquals(0, semantic.getRendererReads());
		assertEquals(0, semantic.getViewportReads());
		assertEquals(0, semantic.getScreenCoordinateReads());
		assertEquals(0, semantic.getDpiReads());
		assertEquals(0, semantic.getCameraTransformReads());
		assertEquals(0, semantic.getLayerOrVisibilityReads());
	}

	private static List<ProjectionBindingId> replace(
			List<ProjectionBindingId> source, ProjectionBindingId current,
			ProjectionBindingId replacement) {
		ArrayList<ProjectionBindingId> result = new ArrayList<>(source);
		int index = result.indexOf(current);
		if (index < 0) {
			throw new AssertionError("Binding is absent from object membership");
		}
		result.set(index, replacement);
		return result;
	}

	private G9A2SpatialSemanticRuntimeTest.Graph graph() {
		return G9A2SpatialSemanticRuntimeTest.Graph.create(
				getConstruction(), this::add);
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private SpatialPointLifecycleService lifecycle() {
		return new SpatialPointLifecycleService(registry());
	}

	private SpatialSemanticInstrumentation semanticInstrumentation() {
		return getConstruction().getSpatialSemanticRuntime().getInstrumentation();
	}

	private SpatialObjectRecord object(
			G9A2SpatialSemanticRuntimeTest.Graph graph) {
		return (SpatialObjectRecord) registry().getRecord(graph.objectId);
	}

	private ProjectionBindingRecord binding(ProjectionBindingId id) {
		return (ProjectionBindingRecord) registry().getRecord(id);
	}

	private G9A3SpatialGraphSnapshot.Snapshot snapshot() {
		return G9A3SpatialGraphSnapshot.capture(getConstruction());
	}
}
