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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.ProjectionDiagramMapId;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRelationRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.SpatialCopyPlan;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleTransaction;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialPointLifecycleService;
import org.geocedg.common.kernel.spatial.identity.SpatialRecordXmlCodec;
import org.geocedg.common.kernel.spatial.identity.SpatialResolutionState;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.gui.dialog.ToolCreationDialogModel;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

/** Canonical G9A3 COPY01--COPY08 closure and remapping scenarios. */
class G9A3SpatialCopyClosureTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D(new AppConfigGeoCeDG());
	}

	@Test
	void copy01SameDocumentCompleteClosureFreshensEveryOwnedIdentity() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph(false);
		CopyResult copy = completeCopy(registry(), getConstruction(), "COPY01");

		copy.transaction.commit();

		assertCompleteCopy(copy, registry(), registry());
		assertCopiedPointIsValid(copy, graph.objectId, getConstruction());
	}

	@Test
	void copy02CrossDocumentCompleteClosureHasNoSourceReference() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph(false);
		AppCommon destination = AppCommonFactory.create3D(new AppConfigGeoCeDG());
		Construction target = destination.getKernel().getConstruction();
		CopyResult copy = completeCopy(registry(), target, "COPY02");

		copy.transaction.commit();

		assertCompleteCopy(copy, registry(), target.getSpatialIdentityRegistry());
		assertCopiedPointIsValid(copy, graph.objectId, target);
		for (SpatialIdentityRecord record : copy.created.values()) {
			for (SpatialIdentityId reference : record.getReferences()) {
				assertTrue(copy.created.containsKey(reference));
			}
		}
	}

	@Test
	void copy03DeclaredSameConstructionExternalUsesTypedContextRootsOnly() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph(false);
		PartialCopyInput input = partialInput(graph, "COPY03");
		SpatialCopyPlan plan = SpatialCopyPlan.declaredSameConstructionExternal(
				registry(), input.ownedIds, input.geoCopies, List.of(graph.systemId),
				Collections.<ProjectionDiagramMapId>emptyList(), "G9A3-COPY03");
		SpatialLifecycleTransaction transaction = lifecycle().prepareCopy(plan);

		transaction.commit();

		assertEquals(input.ownedIds.size(),
				transaction.getMutation().getCreatedRecords().size());
		SpatialObjectRecord copiedObject = copiedRecord(transaction, graph.objectId,
				SpatialObjectRecord.class);
		assertEquals(graph.systemId, copiedObject.getSystemId());
		for (ProjectionBindingId bindingId : copiedObject.getBindingIds()) {
			ProjectionBindingRecord binding = (ProjectionBindingRecord) registry()
					.getRecord(bindingId);
			assertTrue(system(graph).getMapIds().contains(binding.getDiagramMapId()));
		}
		assertForbiddenExternalRootsReject(graph);
	}

	@Test
	void copy04UndeclaredPartialClosureRejectsAtomically() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph(false);
		PartialCopyInput input = partialInput(graph, "COPY04");
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot(getConstruction());
		SpatialCopyPlan plan = SpatialCopyPlan.declaredSameConstructionExternal(
				registry(), input.ownedIds, input.geoCopies,
				Collections.<ProjectionSystemId>emptyList(),
				Collections.<ProjectionDiagramMapId>emptyList(), "G9A3-COPY04");

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareCopy(plan));

		assertEquals(SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
				failure.getDiagnostic().getCode());
		assertEquals(before, snapshot(getConstruction()));
	}

	@Test
	void copy05CrossDocumentPartialClosureRejectsWithoutDestinationMutation() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph(false);
		AppCommon destination = AppCommonFactory.create3D(new AppConfigGeoCeDG());
		Construction target = destination.getKernel().getConstruction();
		IdentityHashMap<GeoElement, GeoElement> onlyProjected = new IdentityHashMap<>();
		onlyProjected.put(graph.horizontalPointGeo,
				copyGeo(graph.horizontalPointGeo, target, "COPY05-H"));
		onlyProjected.put((GeoElement) graph.verticalPoint,
				copyGeo((GeoElement) graph.verticalPoint, target, "COPY05-V"));
		G9A3SpatialGraphSnapshot.Snapshot sourceBefore = snapshot(getConstruction());
		G9A3SpatialGraphSnapshot.Snapshot targetBefore = snapshot(target);
		SpatialCopyPlan plan = SpatialCopyPlan.completeClosure(registry(),
				target.getSpatialIdentityRegistry(), onlyProjected, "G9A3-COPY05");

		assertThrows(SpatialIdentityException.class,
				() -> target.getSpatialIdentityRegistry().prepareCopy(plan));

		assertEquals(sourceBefore, snapshot(getConstruction()));
		assertEquals(targetBefore, snapshot(target));
	}

	@Test
	void copy06RecursiveRelationClosureRemapsEndpointsFramesAndSupportGeos() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph(true);
		CopyResult copy = completeCopy(registry(), getConstruction(), "COPY06");

		copy.transaction.commit();

		assertCompleteCopy(copy, registry(), registry());
		ProjectionFrameRelationRecord relation = copiedRecord(copy.transaction,
				graph.relationId, ProjectionFrameRelationRecord.class);
		ProjectionFrameRelationRecord source = (ProjectionFrameRelationRecord) registry()
				.getRecord(graph.relationId);
		assertEquals(copy.idBySource.get(source.getSourceMapId()),
				relation.getSourceMapId());
		assertEquals(copy.idBySource.get(source.getDestinationMapId()),
				relation.getDestinationMapId());
		assertEquals(copy.idBySource.get(source.getSupportStartGeoId()),
				relation.getSupportStartGeoId());
		assertEquals(copy.idBySource.get(source.getSupportEndGeoId()),
				relation.getSupportEndGeoId());
		assertForbiddenRawExternalRoot(graph.relationId, graph, "relation");
	}

	@Test
	void copy07RepeatedPasteAndMacroInstancesAreDisjointAndCoherent() {
		GeoElement driver = add("G9A3MacroDriver=0");
		final G9A2SpatialSemanticRuntimeTest.Graph graph =
				G9A2SpatialSemanticRuntimeTest.Graph.create(getConstruction(),
						definition -> add(macroDependentDefinition(definition)));
		Macro macro = createSemanticMacroTemplate(driver);
		MacroCopyResult first = invokeMacro(macro, "COPY07A");
		MacroCopyResult second = invokeMacro(macro, "COPY07B");

		assertTrue(macro.isUsed());
		assertEquals(first.idBySource.keySet(), second.idBySource.keySet());
		assertEquals(first.created.size(), second.created.size());
		Set<SpatialIdentityId> overlap = new LinkedHashSet<>(first.created.keySet());
		overlap.retainAll(second.created.keySet());
		assertTrue(overlap.isEmpty());
		assertMacroCopy(first);
		assertMacroCopy(second);
		assertEquals(normalizedTopology(first), normalizedTopology(second));
		assertMacroPointIsCurrent(first, graph.objectId);
		assertMacroPointIsCurrent(second, graph.objectId);
	}

	@Test
	void copy08CopyReroleUndoRedoAndDeletePreserveExactBoundedGraph() {
		activateUndo();
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph(false);
		getApp().storeUndoInfo();
		CopyResult copy = completeCopy(registry(), getConstruction(), "COPY08");
		copy.transaction.commit();
		getApp().storeUndoInfo();
		G9A3SpatialGraphSnapshot.Snapshot copied = snapshot(getConstruction());

		SpatialObjectRecord object = copiedRecord(copy.transaction, graph.objectId,
				SpatialObjectRecord.class);
		ProjectionBindingRecord binding = copiedRecord(copy.transaction,
				graph.horizontalBindingId, ProjectionBindingRecord.class);
		ProjectionBindingId freshId = registry().allocateProjectionBindingId();
		ProjectionBindingRecord presentation = binding.asFreshReroled(freshId,
				ProjectionBindingRole.PRESENTATION);
		List<ProjectionBindingId> bindings = replace(object.getBindingIds(),
				binding.getId(), freshId);
		SpatialObjectRecord updated = object.withBindingsAndRevisions(bindings,
				object.getDefinitionRevision() + 1, object.getTopologyRevision() + 1);
		lifecycle().prepareBindingRerole(object, binding, presentation, updated,
				"G9A3-COPY08-rerole").commit();
		getApp().storeUndoInfo();
		G9A3SpatialGraphSnapshot.Snapshot rerolled = snapshot(getConstruction());

		getKernel().undo();
		assertEquals(copied, snapshot(getConstruction()));
		getKernel().redo();
		assertEquals(rerolled, snapshot(getConstruction()));

		GeoElement copiedProjection = registry().getGeo(
				presentation.getProjectedPointGeoId());
		assertNotNull(copiedProjection);
		copiedProjection.remove();
		assertNull(registry().getRecord(object.getId()));
		assertNotNull(registry().getRecord(graph.objectId));
		assertTrue(registry().size() <= rerolled.getRecordCount());
	}

	private void assertForbiddenExternalRootsReject(
			G9A2SpatialSemanticRuntimeTest.Graph graph) {
		ProjectionBindingRecord binding = (ProjectionBindingRecord) registry()
				.getRecord(graph.horizontalBindingId);
		for (SpatialIdentityId forbidden : List.of(graph.horizontalPointId,
				graph.objectId, graph.horizontalBindingId, binding.getFrameId())) {
			assertForbiddenRawExternalRoot(forbidden, graph,
					forbidden.getKind().name().toLowerCase());
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void assertForbiddenRawExternalRoot(SpatialIdentityId forbidden,
			G9A2SpatialSemanticRuntimeTest.Graph graph, String suffix) {
		PartialCopyInput input = partialInput(graph, "COPY03-NEG-" + suffix);
		Collection rawRoots = List.of(forbidden);
		SpatialCopyPlan plan = SpatialCopyPlan.declaredSameConstructionExternal(
				registry(), input.ownedIds, input.geoCopies, rawRoots,
				Collections.emptyList(), "G9A3-COPY03-forbidden-" + suffix);
		G9A3SpatialGraphSnapshot.Snapshot before = snapshot(getConstruction());

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> lifecycle().prepareCopy(plan));

		assertEquals(SpatialIdentityDiagnostic.Code.LIFECYCLE_EXTERNAL_REFERENCE,
				failure.getDiagnostic().getCode());
		assertEquals(before, snapshot(getConstruction()));
	}

	private CopyResult completeCopy(SpatialIdentityRegistry source,
			Construction target, String suffix) {
		IdentityHashMap<GeoElement, GeoElement> copies = new IdentityHashMap<>();
		for (SpatialIdentityRecord record : source.getRecords()) {
			if (record instanceof GeoIdentityRecord
					&& record.getCopySourceId() == null) {
				GeoElement sourceGeo = source.getGeo((PersistentGeoId) record.getId());
				copies.put(sourceGeo, copyGeo(sourceGeo, target, suffix));
			}
		}
		SpatialIdentityRegistry destination = target.getSpatialIdentityRegistry();
		SpatialCopyPlan plan = SpatialCopyPlan.completeClosure(source, destination,
				copies, "G9A3-" + suffix);
		SpatialLifecycleTransaction transaction = destination.prepareCopy(plan);
		return new CopyResult(transaction, copies);
	}

	private Macro createSemanticMacroTemplate(GeoElement driver) {
		ArrayList<GeoIdentityRecord> participants = new ArrayList<>();
		for (SpatialIdentityRecord record : registry().getRecords()) {
			if (record instanceof GeoIdentityRecord
					&& record.getCopySourceId() == null) {
				participants.add((GeoIdentityRecord) record);
			}
		}
		participants.sort(Comparator.comparing(
				record -> record.getId().toExternalForm()));
		ToolCreationDialogModel builder = new ToolCreationDialogModel(getApp(),
				() -> { /* no UI in this host integration test */ });
		builder.addToInput(driver);
		for (GeoIdentityRecord participant : participants) {
			builder.addToOutput(registry().getGeo(participant.getId()));
		}
		assertTrue(builder.createTool());
		builder.finish(getApp(), "G9A3SpatialPointTemplate",
				"G9A3SpatialPointTemplate", "One scalar input expected", false, null);
		Macro macro = getKernel().getMacro("G9A3SpatialPointTemplate");
		assertNotNull(macro);
		assertEquals(participants.size(), macro.getMacroOutput().length);
		assertEquals(registry().size(), macro.getMacroConstruction()
				.getSpatialIdentityRegistry().size());
		return macro;
	}

	private MacroCopyResult invokeMacro(Macro macro, String suffix) {
		Set<SpatialIdentityId> before = new LinkedHashSet<>();
		for (SpatialIdentityRecord record : registry().getRecords()) {
			before.add(record.getId());
		}
		GeoElement callerInput = add("G9A3MacroInput" + suffix + "=0");
		String[] labels = new String[macro.getMacroOutput().length];
		for (int index = 0; index < labels.length; index++) {
			labels[index] = "G9A3Macro" + suffix + index;
		}
		GeoElement[] output = getKernel().useMacro(
				labels, macro, new GeoElement[] {callerInput});
		assertNotNull(output);
		assertEquals(labels.length, output.length);
		SpatialIdentityRegistry template = macro.getMacroConstruction()
				.getSpatialIdentityRegistry();
		LinkedHashMap<SpatialIdentityId, SpatialIdentityId> idBySource =
				new LinkedHashMap<>();
		for (SpatialIdentityRecord record : registry().getRecords()) {
			if (!before.contains(record.getId()) && record.getCopySourceId() != null
					&& template.getRecord(record.getCopySourceId()) != null) {
				idBySource.put(record.getCopySourceId(), record.getId());
			}
		}
		assertEquals(template.size(), idBySource.size());
		return new MacroCopyResult(idBySource, registry(), template);
	}

	private static String macroDependentDefinition(String definition) {
		if ("G9A2O=(0,0,0)".equals(definition)) {
			return "G9A2O=(G9A3MacroDriver,0,0)";
		}
		if ("G9A2U=Vector((0,0,0),(1,0,0))".equals(definition)) {
			return "G9A2U=Vector((G9A3MacroDriver,0,0),"
					+ "(1+G9A3MacroDriver,0,0))";
		}
		if ("G9A2VH=Vector((0,0,0),(0,1,0))".equals(definition)) {
			return "G9A2VH=Vector((G9A3MacroDriver,0,0),"
					+ "(G9A3MacroDriver,1,0))";
		}
		if ("G9A2Axis=0".equals(definition)) {
			return "G9A2Axis=G9A3MacroDriver";
		}
		if ("G9A2One=1".equals(definition)) {
			return "G9A2One=1+G9A3MacroDriver";
		}
		if ("G9A2Zero=0".equals(definition)) {
			return "G9A2Zero=G9A3MacroDriver";
		}
		if ("G9A2PH=(2,3)".equals(definition)) {
			return "G9A2PH=(2+G9A3MacroDriver,3)";
		}
		if ("G9A2PV=(2,5)".equals(definition)) {
			return "G9A2PV=(2+G9A3MacroDriver,5)";
		}
		return definition;
	}

	private PartialCopyInput partialInput(G9A2SpatialSemanticRuntimeTest.Graph graph,
			String suffix) {
		PersistentGeoId verticalId = registry().getPersistentGeoId(
				(GeoElement) graph.verticalPoint);
		Set<SpatialIdentityId> owned = new LinkedHashSet<>(List.of(graph.objectId,
				graph.horizontalBindingId, graph.verticalBindingId,
				graph.horizontalPointId, verticalId));
		IdentityHashMap<GeoElement, GeoElement> copies = new IdentityHashMap<>();
		copies.put(graph.horizontalPointGeo,
				copyGeo(graph.horizontalPointGeo, getConstruction(), suffix + "-H"));
		copies.put((GeoElement) graph.verticalPoint,
				copyGeo((GeoElement) graph.verticalPoint, getConstruction(), suffix + "-V"));
		return new PartialCopyInput(owned, copies);
	}

	private static GeoElement copyGeo(GeoElement source, Construction target,
			String suffix) {
		GeoElement copy = source.copyInternal(target);
		copy.setLabel(source.getLabelSimple() + suffix + target.steps());
		return copy;
	}

	private void assertCompleteCopy(CopyResult copy, SpatialIdentityRegistry source,
			SpatialIdentityRegistry destination) {
		assertFalse(copy.created.isEmpty());
		for (SpatialIdentityRecord record : copy.created.values()) {
			assertNotNull(record.getCopySourceId());
			assertNotNull(source.getRecord(record.getCopySourceId()));
			assertEquals(record.getId().getKind(), record.getCopySourceId().getKind());
			assertNotEquals(record.getId(), record.getCopySourceId());
			assertNotNull(destination.getRecord(record.getId()));
			assertEquals(SpatialResolutionState.ACTIVE,
					destination.getResolution(record.getId()).getState());
		}
	}

	private static void assertCopiedPointIsValid(CopyResult copy,
			SpatialObjectId sourceObjectId, Construction target) {
		SpatialObjectId copiedId = (SpatialObjectId) copy.idBySource.get(sourceObjectId);
		assertNotNull(copiedId);
		assertEquals(SpatialCertificateStatus.VALID,
				target.getSpatialSemanticRuntime().getSpatialPointCertificate(copiedId)
						.getSemanticCertificate().getStatus());
	}

	private void assertMacroCopy(MacroCopyResult copy) {
		assertFalse(copy.created.isEmpty());
		for (Map.Entry<SpatialIdentityId, SpatialIdentityId> entry
				: copy.idBySource.entrySet()) {
			SpatialIdentityRecord record = copy.created.get(entry.getValue());
			assertNotNull(record);
			assertEquals(entry.getKey(), record.getCopySourceId());
			assertEquals(SpatialResolutionState.ACTIVE,
					registry().getResolution(record.getId()).getState());
		}
		for (SpatialIdentityRecord record : copy.created.values()) {
			if (record instanceof GeoIdentityRecord) {
				assertNotNull(registry().getGeo((PersistentGeoId) record.getId()));
			}
		}
	}

	private void assertMacroPointIsCurrent(MacroCopyResult copy,
			SpatialObjectId sourceObjectId) {
		SpatialObjectId copiedId = (SpatialObjectId) copy.idBySource.get(sourceObjectId);
		assertNotNull(copiedId);
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(copiedId).isCurrentRevision());
		assertEquals(SpatialCertificateStatus.VALID,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(copiedId)
						.getSemanticCertificate().getStatus());
	}

	private List<String> normalizedTopology(MacroCopyResult copy) {
		Map<SpatialIdentityId, SpatialIdentityId> sourceByCreated =
				new LinkedHashMap<>();
		for (Map.Entry<SpatialIdentityId, SpatialIdentityId> entry
				: copy.idBySource.entrySet()) {
			sourceByCreated.put(entry.getValue(), entry.getKey());
		}
		ArrayList<SpatialIdentityId> sourceIds = new ArrayList<>(
				copy.idBySource.keySet());
		sourceIds.sort(Comparator.comparing(SpatialIdentityId::toExternalForm));
		ArrayList<String> result = new ArrayList<>();
		for (SpatialIdentityId sourceId : sourceIds) {
			SpatialIdentityRecord sourceRecord = copy.sourceRegistry.getRecord(sourceId);
			SpatialIdentityRecord created = copy.created.get(
					copy.idBySource.get(sourceId));
			assertNotNull(sourceRecord);
			assertNotNull(created);
			assertNull(sourceRecord.getCopySourceId());
			assertEquals(sourceId, created.getCopySourceId());
			SpatialIdentityRecord normalized = created.remap(sourceByCreated, false);
			String copySourceAttribute = " copySource=\""
					+ sourceId.toExternalForm() + "\"";
			String normalizedXml = SpatialRecordXmlCodec.writeRecord(normalized);
			assertTrue(normalizedXml.contains(copySourceAttribute));
			normalizedXml = normalizedXml.replace(copySourceAttribute, "");
			String sourceXml = SpatialRecordXmlCodec.writeRecord(sourceRecord);
			assertEquals(sourceXml, normalizedXml,
					"Macro copy changed canonical record semantics for " + sourceId);
			result.add(sourceXml);
		}
		return result;
	}

	private static GeoElement targetGeoForSource(CopyResult copy,
			GeoElement source) {
		return copy.geoCopies.get(source);
	}

	private static <T extends SpatialIdentityRecord> T copiedRecord(
			SpatialLifecycleTransaction transaction, SpatialIdentityId sourceId,
			Class<T> type) {
		for (SpatialIdentityRecord record
				: transaction.getMutation().getCreatedRecords().values()) {
			if (sourceId.equals(record.getCopySourceId()) && type.isInstance(record)) {
				return type.cast(record);
			}
		}
		throw new AssertionError("No copied " + sourceId);
	}

	private static List<ProjectionBindingId> replace(List<ProjectionBindingId> source,
			ProjectionBindingId current, ProjectionBindingId replacement) {
		ArrayList<ProjectionBindingId> result = new ArrayList<>(source);
		int index = result.indexOf(current);
		if (index < 0) {
			throw new AssertionError("Binding is absent from copied object");
		}
		result.set(index, replacement);
		return result;
	}

	private G9A2SpatialSemanticRuntimeTest.Graph graph(boolean hinge) {
		return hinge
				? G9A2SpatialSemanticRuntimeTest.Graph.createWithHinge(
						getConstruction(), this::add, false)
				: G9A2SpatialSemanticRuntimeTest.Graph.create(
						getConstruction(), this::add);
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private SpatialPointLifecycleService lifecycle() {
		return new SpatialPointLifecycleService(registry());
	}

	private org.geocedg.common.kernel.spatial.identity.ProjectionSystemRecord system(
			G9A2SpatialSemanticRuntimeTest.Graph graph) {
		return (org.geocedg.common.kernel.spatial.identity.ProjectionSystemRecord)
				registry().getRecord(graph.systemId);
	}

	private static G9A3SpatialGraphSnapshot.Snapshot snapshot(
			Construction construction) {
		return G9A3SpatialGraphSnapshot.capture(construction);
	}

	private static final class PartialCopyInput {
		private final Set<SpatialIdentityId> ownedIds;
		private final IdentityHashMap<GeoElement, GeoElement> geoCopies;

		private PartialCopyInput(Set<SpatialIdentityId> ownedIds,
				IdentityHashMap<GeoElement, GeoElement> geoCopies) {
			this.ownedIds = ownedIds;
			this.geoCopies = geoCopies;
		}
	}

	private static final class CopyResult {
		private final SpatialLifecycleTransaction transaction;
		private final Map<SpatialIdentityId, SpatialIdentityRecord> created;
		private final Map<SpatialIdentityId, SpatialIdentityId> idBySource;
		private final IdentityHashMap<GeoElement, GeoElement> geoCopies;

		private CopyResult(SpatialLifecycleTransaction transaction,
				IdentityHashMap<GeoElement, GeoElement> geoCopies) {
			this.transaction = transaction;
			this.created = transaction.getMutation().getCreatedRecords();
			this.geoCopies = new IdentityHashMap<>(geoCopies);
			this.idBySource = new LinkedHashMap<>();
			for (SpatialIdentityRecord record : created.values()) {
				idBySource.put(record.getCopySourceId(), record.getId());
			}
		}
	}

	private static final class MacroCopyResult {
		private final Map<SpatialIdentityId, SpatialIdentityId> idBySource;
		private final Map<SpatialIdentityId, SpatialIdentityRecord> created;
		private final SpatialIdentityRegistry sourceRegistry;

		private MacroCopyResult(
				Map<SpatialIdentityId, SpatialIdentityId> idBySource,
				SpatialIdentityRegistry registry,
				SpatialIdentityRegistry sourceRegistry) {
			this.idBySource = new LinkedHashMap<>(idBySource);
			this.sourceRegistry = sourceRegistry;
			this.created = new LinkedHashMap<>();
			for (SpatialIdentityId createdId : idBySource.values()) {
				created.put(createdId, registry.getRecord(createdId));
			}
		}
	}
}
