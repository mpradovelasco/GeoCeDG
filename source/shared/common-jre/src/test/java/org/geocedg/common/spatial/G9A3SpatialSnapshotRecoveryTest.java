/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameUseRole;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleTransaction;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialPointLifecycleService;
import org.geocedg.common.kernel.spatial.identity.SpatialPointMigrationPlan;
import org.geocedg.common.kernel.spatial.identity.SpatialPointPilotRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineContext;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineDecision;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProposal;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineSignature;
import org.geocedg.common.kernel.spatial.runtime.SpatialPointPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialSemanticRuntime;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.gui.view.algebra.EvalInfoFactory;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.error.ErrorLogger;
import org.geogebra.common.plugin.GeoClass;
import org.geogebra.common.util.AsyncOperation;
import org.junit.jupiter.api.Test;

class G9A3SpatialSnapshotRecoveryTest extends BaseUnitTest {
	private static final String SNAPSHOT_PROVIDER = "g9a3.snapshot.numeric";
	private static final String SNAPSHOT_SCHEMA = "cedg.g9a3.snapshot.numeric";
	private static final String FIRST_POINT_UNIT = "model-unit";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D();
	}

	@Test
	void snap01TwoSaveReopenCyclesPreserveExactGraphAndXml() throws Exception {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		G9A3SpatialGraphSnapshot.Snapshot first = snapshot();
		String firstXml = getApp().getXML();

		getApp().getXMLio().processXMLString(firstXml, true, false, false);
		G9A3SpatialGraphSnapshot.Snapshot second = snapshot();
		String secondXml = getApp().getXML();
		getApp().getXMLio().processXMLString(secondXml, true, false, false);
		G9A3SpatialGraphSnapshot.Snapshot third = snapshot();

		assertEquals(first, second);
		assertEquals(second, third);
		assertEquals(firstXml, secondXml);
		assertEquals(secondXml, getApp().getXML());
		assertValidPoint(graph.objectId);
	}

	@Test
	void snap02UndoRedoRestoresReroleAndDeleteSnapshotsExactly() {
		activateUndo();
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		getApp().storeUndoInfo();
		final G9A3SpatialGraphSnapshot.Snapshot base = snapshot();

		ProjectionBindingId replacementId = reroleVerticalBinding(graph);
		getApp().storeUndoInfo();
		final G9A3SpatialGraphSnapshot.Snapshot reroled = snapshot();
		assertNull(registry().getRecord(graph.verticalBindingId));
		assertNotNull(registry().getRecord(replacementId));
		SpatialPointPilotCertificate failed = semanticRuntime()
				.getSpatialPointCertificate(graph.objectId);
		assertTrue(failed == null || !failed.getSemanticCertificate().hasPayload());

		graph.horizontalPointGeo.remove();
		getApp().storeUndoInfo();
		final G9A3SpatialGraphSnapshot.Snapshot deleted = snapshot();
		assertNull(registry().getRecord(graph.objectId));

		getKernel().undo();
		assertEquals(reroled, snapshot());
		getKernel().undo();
		assertEquals(base, snapshot());
		assertValidPoint(graph.objectId);
		getKernel().redo();
		assertEquals(reroled, snapshot());
		getKernel().redo();
		assertEquals(deleted, snapshot());
	}

	@Test
	void snap03RetainedRedefineAndFreshReplacementRemainReversible()
			throws Exception {
		graph();
		GeoNumeric initial = add("G9A3SnapshotValue=1");
		GeoIdentityRecord original = registerSnapshotNumeric(initial);
		registry().registerRedefineProvider(new SnapshotNumericProvider());

		editGeoElement(initial, "G9A3SnapshotValue=2");

		GeoNumeric retained = (GeoNumeric) lookup("G9A3SnapshotValue");
		assertEquals(original.getId(), registry().getPersistentGeoId(retained));
		assertEquals(1,
				registry().getGeoRecord(original.getId()).getDefinitionRevision());
		G9A3SpatialGraphSnapshot.Snapshot retainedSnapshot = snapshot();
		String retainedXml = getApp().getXML();

		getApp().getXMLio().processXMLString(retainedXml, true, false, false);
		assertEquals(retainedSnapshot, snapshot());
		assertEquals(retainedXml, getApp().getXML());
		activateUndo();
		getApp().storeUndoInfo();

		GeoElement reopened = lookup("G9A3SnapshotValue");
		EvalInfo replacementInfo = EvalInfoFactory.getEvalInfoForRedefinition(
				getKernel(), reopened, true).withSpatialReplacementOperation();
		CapturingErrorHandler errors = new CapturingErrorHandler();
		getAlgebraProcessor().changeGeoElementNoExceptionHandling(reopened,
				"G9A3SnapshotValue=3", replacementInfo, false, null, errors);
		assertFalse(errors.hasError());
		getApp().storeUndoInfo();

		PersistentGeoId replacementId = registry().getPersistentGeoId(
				lookup("G9A3SnapshotValue"));
		assertNotNull(replacementId);
		assertNotEquals(original.getId(), replacementId);
		G9A3SpatialGraphSnapshot.Snapshot freshSnapshot = snapshot();

		getKernel().undo();
		assertEquals(retainedSnapshot, snapshot());
		assertEquals(original.getId(), registry().getPersistentGeoId(
				lookup("G9A3SnapshotValue")));
		getKernel().redo();
		assertEquals(freshSnapshot, snapshot());
		assertEquals(replacementId, registry().getPersistentGeoId(
				lookup("G9A3SnapshotValue")));
	}

	@Test
	void snap04InvalidReopenHasNoPayloadAndTypedRepairRecovers()
			throws Exception {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		graph.setVerticalProjection(9, 5);
		getKernel().updateConstruction(false);
		SpatialPointPilotCertificate invalid = semanticRuntime()
				.getSpatialPointCertificate(graph.objectId);
		assertNotNull(invalid);
		assertEquals(SpatialCertificateStatus.INCONSISTENT_PROJECTIONS,
				invalid.getSemanticCertificate().getStatus());
		assertFalse(invalid.getSemanticCertificate().hasPayload());
		assertFalse(semanticRuntime().getDerivedPoint(graph.objectId).isDefined());
		G9A3SpatialGraphSnapshot.Snapshot invalidSnapshot = snapshot();
		String invalidXml = getApp().getXML();

		getApp().getXMLio().processXMLString(invalidXml, true, false, false);

		assertEquals(invalidSnapshot, snapshot());
		invalid = semanticRuntime().getSpatialPointCertificate(graph.objectId);
		assertFalse(invalid.getSemanticCertificate().hasPayload());
		assertFalse(semanticRuntime().getDerivedPoint(graph.objectId).isDefined());
		ProjectionBindingRecord vertical = (ProjectionBindingRecord) registry()
				.getRecord(graph.verticalBindingId);
		GeoPointND exactProjectedHandle = (GeoPointND) registry().getGeo(
				vertical.getProjectedPointGeoId());
		exactProjectedHandle.setCoords(2, 5, 1);
		((GeoElement) exactProjectedHandle).updateRepaint();
		getKernel().updateConstruction(false);
		G9A3SpatialGraphSnapshot.Snapshot recovered = snapshot();
		String recoveredXml = getApp().getXML();

		assertValidPoint(graph.objectId);
		getApp().getXMLio().processXMLString(recoveredXml, true, false, false);
		assertEquals(recovered, snapshot());
		assertEquals(recoveredXml, getApp().getXML());
		assertValidPoint(graph.objectId);
	}

	@Test
	void snap05FailedLoadRestoresRegistryDagPayloadAndXmlExactly() throws Exception {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot();
		String beforeXml = getApp().getXML();
		String future = beforeXml.replace("<geocedgSpatial version=\"1\">",
				"<geocedgSpatial version=\"999\">");
		assertNotEquals(beforeXml, future);

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> getApp().getXMLio().processXMLString(
						future, true, false, false));

		assertEquals(SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION,
				failure.getDiagnostic().getCode());
		assertEquals(before, snapshot());
		assertEquals(beforeXml, getApp().getXML());
		assertFalse(getConstruction().isFileLoading());
		assertValidPoint(graph.objectId);
		assertFirstPointRuntimeFailureDoesNotLeakUsedTypes();
	}

	private void assertFirstPointRuntimeFailureDoesNotLeakUsedTypes()
			throws Exception {
		getApp().getXMLio().processXMLString(
				"<geogebra format=\"5.0\"><construction/></geogebra>",
				true, false, false);
		FirstPointInputs inputs = FirstPointInputs.create(this::add);
		String entryXml = getApp().getXML();
		G9A3SpatialGraphSnapshot.Snapshot entrySnapshot = snapshot();
		Set<GeoClass> entryTypes = usedGeoTypes();
		assertFalse(entryTypes.contains(GeoClass.POINT3D));
		assertFalse(getConstruction().requires3D());

		FirstPointMigration abandonedPlan = inputs.plan(registry(),
				"G9A3-SNAP05-abandoned-runtime-prepare");
		SpatialLifecycleTransaction abandoned = new SpatialPointLifecycleService(
				registry()).prepareMigration(abandonedPlan.plan);
		assertEquals(entryXml, getApp().getXML());
		assertEquals(entrySnapshot, snapshot());
		assertEquals(entryTypes, usedGeoTypes());
		assertFalse(getConstruction().requires3D());
		assertNull(semanticRuntime().getSystemAlgorithm(abandonedPlan.systemId));
		assertNull(semanticRuntime().getPointAlgorithm(abandonedPlan.objectId));
		abandoned.rollback();
		assertEquals(entryXml, getApp().getXML());
		assertEquals(entrySnapshot, snapshot());
		assertEquals(entryTypes, usedGeoTypes());
		assertFalse(getConstruction().requires3D());

		FirstPointMigration failingPlan = inputs.plan(registry(),
				"G9A3-SNAP05-runtime-activation-failure");
		SpatialLifecycleTransaction failing = new SpatialPointLifecycleService(
				registry()).prepareMigration(failingPlan.plan);
		Object instrumentation = semanticRuntime().getInstrumentation();
		Field dependencyUpdates = instrumentation.getClass()
				.getDeclaredField("dependencyUpdates");
		dependencyUpdates.setAccessible(true);
		long originalDependencyUpdates = dependencyUpdates.getLong(instrumentation);
		dependencyUpdates.setLong(instrumentation, Long.MAX_VALUE);
		try {
			SpatialIdentityException failure = assertThrows(
					SpatialIdentityException.class, failing::commit);
			assertEquals(SpatialIdentityDiagnostic.Code.LIFECYCLE_RUNTIME_FAILURE,
					failure.getDiagnostic().getCode());
			assertEquals(entryXml, getApp().getXML());
			assertEquals(entrySnapshot, snapshot());
			assertEquals(entryTypes, usedGeoTypes());
			assertFalse(usedGeoTypes().contains(GeoClass.POINT3D));
			assertFalse(getConstruction().requires3D());
			assertNull(semanticRuntime().getSystemAlgorithm(failingPlan.systemId));
			assertNull(semanticRuntime().getPointAlgorithm(failingPlan.objectId));
		} finally {
			dependencyUpdates.setLong(instrumentation, originalDependencyUpdates);
		}
		// ceID allocation is intentionally not asserted: it is ephemeral and
		// non-authoritative, while the live DAG/order/type disposition is exact.
	}

	@SuppressWarnings("unchecked")
	private Set<GeoClass> usedGeoTypes() throws ReflectiveOperationException {
		Field usedGeos = Construction.class.getDeclaredField("usedGeos");
		usedGeos.setAccessible(true);
		return new TreeSet<>((Set<GeoClass>) usedGeos.get(getConstruction()));
	}

	private ProjectionBindingId reroleVerticalBinding(
			G9A2SpatialSemanticRuntimeTest.Graph graph) {
		SpatialObjectRecord currentObject = (SpatialObjectRecord) registry()
				.getRecord(graph.objectId);
		ProjectionBindingRecord currentBinding = (ProjectionBindingRecord) registry()
				.getRecord(graph.verticalBindingId);
		ProjectionBindingId replacementId = registry().allocateProjectionBindingId();
		ProjectionBindingRecord replacementBinding = new ProjectionBindingRecord(
				replacementId, 2, currentBinding.getObjectId(),
				currentBinding.getSystemId(), currentBinding.getDiagramMapId(),
				currentBinding.getFrameId(), ProjectionBindingRole.PRESENTATION,
				currentBinding.getRepresentationType(),
				currentBinding.getExpectedSpatialType(), currentBinding.getSchemaId(),
				currentBinding.getSchemaVersion(),
				currentBinding.getProjectedPointGeoId(), currentBinding.getFidelity(),
				currentBinding.getCorrespondence(), 0);
		List<ProjectionBindingId> bindings = new ArrayList<>(
				currentObject.getBindingIds());
		bindings.set(bindings.indexOf(graph.verticalBindingId), replacementId);
		SpatialObjectRecord updatedObject = new SpatialObjectRecord(
				currentObject.getId(), 2, currentObject.getSpatialType(),
				currentObject.getAuthority(), currentObject.getSchemaId(),
				currentObject.getSchemaVersion(), currentObject.getSystemId(), bindings,
				currentObject.getDefinitionRevision() + 1,
				currentObject.getTopologyRevision() + 1);

		new SpatialPointLifecycleService(registry()).prepareBindingRerole(
				currentObject, currentBinding, replacementBinding, updatedObject,
				"g9a3.snap02.rerole").commit();
		return replacementId;
	}

	private GeoIdentityRecord registerSnapshotNumeric(GeoNumeric numeric) {
		PersistentGeoId id = registry().allocatePersistentGeoId();
		GeoIdentityRecord record = new GeoIdentityRecord(id, SNAPSHOT_PROVIDER,
				"NUMERIC", SNAPSHOT_SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				"VALUE", 1, 0, 0);
		registry().registerParticipation(numeric, record);
		return record;
	}

	private G9A2SpatialSemanticRuntimeTest.Graph graph() {
		return G9A2SpatialSemanticRuntimeTest.Graph.create(
				getConstruction(), this::add);
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

	private void assertValidPoint(
			org.geocedg.common.kernel.spatial.identity.SpatialObjectId objectId) {
		SpatialPointPilotCertificate point = semanticRuntime()
				.getSpatialPointCertificate(objectId);
		assertNotNull(point);
		assertTrue(point.isCurrentRevision());
		assertEquals(SpatialCertificateStatus.VALID,
				point.getSemanticCertificate().getStatus());
		assertTrue(point.getSemanticCertificate().hasPayload());
		assertTrue(semanticRuntime().getDerivedPoint(objectId).isDefined());
	}

	private static SpatialRedefineSignature numericSignature() {
		return new SpatialRedefineSignature(SNAPSHOT_PROVIDER, "NUMERIC",
				SNAPSHOT_SCHEMA, 1, EditAuthorityMode.PROJECTION_DEFINED,
				ProjectionBindingRole.DEFINING, "VALUE", 1);
	}

	private static final class FirstPointInputs {
		private final GeoElement origin;
		private final GeoElement u;
		private final GeoElement horizontalV;
		private final GeoElement verticalV;
		private final GeoElement one;
		private final GeoElement zero;
		private final GeoElement horizontalPoint;
		private final GeoElement verticalPoint;

		private FirstPointInputs(GeoElement origin, GeoElement u,
				GeoElement horizontalV, GeoElement verticalV, GeoElement one,
				GeoElement zero, GeoElement horizontalPoint,
				GeoElement verticalPoint) {
			this.origin = origin;
			this.u = u;
			this.horizontalV = horizontalV;
			this.verticalV = verticalV;
			this.one = one;
			this.zero = zero;
			this.horizontalPoint = horizontalPoint;
			this.verticalPoint = verticalPoint;
		}

		static FirstPointInputs create(Function<String, GeoElement> add) {
			return new FirstPointInputs(
					add.apply("G9A3S5O=(0,0)"),
					add.apply("G9A3S5U=Vector((0,0),(1,0))"),
					add.apply("G9A3S5H=Vector((0,0),(0,1))"),
					add.apply("G9A3S5V=Vector((0,0),(0,1))"),
					add.apply("G9A3S5One=1"),
					add.apply("G9A3S5Zero=0"),
					add.apply("G9A3S5PH=(2,3)"),
					add.apply("G9A3S5PV=(2,5)"));
		}

		FirstPointMigration plan(SpatialIdentityRegistry registry,
				String provenance) {
			PersistentGeoId originId = registry.allocatePersistentGeoId();
			PersistentGeoId uId = registry.allocatePersistentGeoId();
			PersistentGeoId horizontalVId = registry.allocatePersistentGeoId();
			PersistentGeoId verticalVId = registry.allocatePersistentGeoId();
			PersistentGeoId oneId = registry.allocatePersistentGeoId();
			PersistentGeoId zeroId = registry.allocatePersistentGeoId();
			PersistentGeoId horizontalPointId = registry.allocatePersistentGeoId();
			PersistentGeoId verticalPointId = registry.allocatePersistentGeoId();
			ProjectionFrameId horizontalFrameId = registry.allocateProjectionFrameId();
			ProjectionFrameId verticalFrameId = registry.allocateProjectionFrameId();
			ProjectionSystemId systemId = registry.allocateProjectionSystemId();
			ProjectionDiagramMapId horizontalMapId =
					registry.allocateProjectionDiagramMapId();
			ProjectionDiagramMapId verticalMapId =
					registry.allocateProjectionDiagramMapId();
			ProjectionBindingId horizontalBindingId =
					registry.allocateProjectionBindingId();
			ProjectionBindingId verticalBindingId =
					registry.allocateProjectionBindingId();
			SpatialObjectId objectId = registry.allocateSpatialObjectId();
			SpatialPointMigrationPlan.Builder builder = SpatialPointMigrationPlan
					.builder(provenance)
					.attach(origin, geoRecord(originId, "INPUT"))
					.attach(u, geoRecord(uId, "INPUT"))
					.attach(horizontalV, geoRecord(horizontalVId, "INPUT"))
					.attach(verticalV, geoRecord(verticalVId, "INPUT"))
					.attach(one, geoRecord(oneId, "INPUT"))
					.attach(zero, geoRecord(zeroId, "INPUT"))
					.attach(horizontalPoint,
							geoRecord(horizontalPointId, "PROJECTED_POINT"))
					.attach(verticalPoint,
							geoRecord(verticalPointId, "PROJECTED_POINT"));
			builder.record(new ProjectionFrameRecord(horizontalFrameId, 2,
					originId, uId, horizontalVId, "ORTHOGRAPHIC", FIRST_POINT_UNIT,
					"RIGHT_HANDED", "EXACT", 0));
			builder.record(new ProjectionFrameRecord(verticalFrameId, 2,
					originId, uId, verticalVId, "ORTHOGRAPHIC", FIRST_POINT_UNIT,
					"RIGHT_HANDED", "EXACT", 0));
			builder.record(new ProjectionSystemRecord(systemId, 2,
					List.of(horizontalMapId, verticalMapId), List.of(),
					FIRST_POINT_UNIT, 1e-9, 1e-9, 1e-12, 1e-9, 1e-9, 1e10, 0));
			builder.record(map(horizontalMapId, systemId, horizontalFrameId,
					oneId, zeroId));
			builder.record(map(verticalMapId, systemId, verticalFrameId,
					oneId, zeroId));
			builder.record(binding(horizontalBindingId, objectId, systemId,
					horizontalMapId, horizontalFrameId, horizontalPointId));
			builder.record(binding(verticalBindingId, objectId, systemId,
					verticalMapId, verticalFrameId, verticalPointId));
			builder.record(new SpatialObjectRecord(objectId, 2,
					SpatialObjectRecord.POINT_TYPE,
					EditAuthorityMode.PROJECTION_DEFINED,
					SpatialObjectRecord.POINT_SCHEMA_ID,
					SpatialObjectRecord.POINT_SCHEMA_VERSION, systemId,
					List.of(horizontalBindingId, verticalBindingId), 0, 0));
			return new FirstPointMigration(builder.build(), systemId, objectId);
		}

		private static GeoIdentityRecord geoRecord(PersistentGeoId id,
				String outputRole) {
			return new GeoIdentityRecord(id,
					SpatialPointPilotRedefineProvider.PROVIDER_ID,
					"PROJECTED_POINT".equals(outputRole)
							? SpatialObjectRecord.POINT_TYPE : "SEMANTIC_INPUT",
					SpatialObjectRecord.POINT_SCHEMA_ID,
					SpatialObjectRecord.POINT_SCHEMA_VERSION,
					EditAuthorityMode.PROJECTION_DEFINED,
					ProjectionBindingRole.DEFINING, outputRole, 1, 0, 0);
		}

		private static ProjectionDiagramMapRecord map(ProjectionDiagramMapId id,
				ProjectionSystemId systemId, ProjectionFrameId frameId,
				PersistentGeoId oneId, PersistentGeoId zeroId) {
			return new ProjectionDiagramMapRecord(id, 2, systemId, frameId,
					ProjectionFrameUseRole.DEFINING, "ORIENTED_ISOMETRY",
					"PRESERVING", FIRST_POINT_UNIT, "EXACT", oneId, zeroId,
					zeroId, oneId, zeroId, zeroId, oneId, List.of(), 0);
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

	private static final class FirstPointMigration {
		private final SpatialPointMigrationPlan plan;
		private final ProjectionSystemId systemId;
		private final SpatialObjectId objectId;

		private FirstPointMigration(SpatialPointMigrationPlan plan,
				ProjectionSystemId systemId, SpatialObjectId objectId) {
			this.plan = plan;
			this.systemId = systemId;
			this.objectId = objectId;
		}
	}

	private static final class SnapshotNumericProvider
			implements SpatialRedefineProvider {
		@Override
		public String getProviderId() {
			return SNAPSHOT_PROVIDER;
		}

		@Override
		public SpatialRedefineSignature describeCandidate(
				SpatialRedefineContext context, GeoElement candidate) {
			return candidate instanceof GeoNumeric ? numericSignature()
					: new SpatialRedefineSignature(SNAPSHOT_PROVIDER, "INCOMPATIBLE",
							SNAPSHOT_SCHEMA, 1,
							EditAuthorityMode.PROJECTION_DEFINED,
							ProjectionBindingRole.DEFINING, "VALUE", 1);
		}

		@Override
		public boolean isTopologyPreserving(SpatialRedefineContext context,
				GeoElement candidate) {
			return candidate instanceof GeoNumeric;
		}

		@Override
		public SpatialRedefineDecision inspect(SpatialRedefineContext context,
				SpatialRedefineProposal proposal) {
			if (!(proposal.getCandidate() instanceof GeoNumeric)) {
				return SpatialRedefineDecision.REJECT;
			}
			return ((GeoNumeric) proposal.getCandidate()).getDouble() == 3
					? SpatialRedefineDecision.FRESH : SpatialRedefineDecision.RETAIN;
		}
	}

	private static final class CapturingErrorHandler implements ErrorLogger {
		private Throwable error;
		private String message;

		@Override
		public void showError(String text) {
			message = text;
		}

		@Override
		public void showCommandError(String command, String text) {
			message = text;
		}

		@Override
		public String getCurrentCommand() {
			return null;
		}

		@Override
		public boolean onUndefinedVariables(String variables,
				AsyncOperation<String[]> callback) {
			return false;
		}

		@Override
		public void resetError() {
			error = null;
			message = null;
		}

		@Override
		public void log(Throwable throwable) {
			error = throwable;
		}

		boolean hasError() {
			return error != null || message != null;
		}
	}
}
