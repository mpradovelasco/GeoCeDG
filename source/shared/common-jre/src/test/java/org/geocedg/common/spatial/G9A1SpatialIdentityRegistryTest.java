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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityKind;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialResolutionState;
import org.geocedg.common.kernel.spatial.identity.SpatialTokenSource;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoCasCell;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

class G9A1SpatialIdentityRegistryTest extends BaseUnitTest {
	private static final String PROVIDER = "g9a1.test";
	private static final String FAMILY = "POINT";
	private static final String SCHEMA = "cedg.point.orthographic";

	@Test
	void registersEveryTypedRecordAsOneConnectedClosure() {
		Closure closure = createClosure(new SpatialIdentityRegistry(
				new SequentialTokenSource(1), 4), "A", "B");

		assertEquals(10, closure.registry.size());
		assertEquals(Map.of(
				SpatialIdentityKind.GEO, 2,
				SpatialIdentityKind.SPATIAL_OBJECT, 1,
				SpatialIdentityKind.PROJECTION_FRAME, 2,
				SpatialIdentityKind.PROJECTION_SYSTEM, 1,
				SpatialIdentityKind.PROJECTION_DIAGRAM_MAP, 2,
				SpatialIdentityKind.PROJECTION_FRAME_RELATION, 1,
				SpatialIdentityKind.PROJECTION_BINDING, 1),
				closure.registry.getRecordCountsByKind());
		assertEquals(10, closure.registry.getClosureRecords(
				Collections.singletonList(closure.geoA)).size());
		assertEquals(Set.of(closure.geoA, closure.geoB),
				closure.registry.expandSemanticClosure(List.of(closure.geoA)));
		for (SpatialIdentityRecord record : closure.registry.getRecords()) {
			assertEquals(SpatialResolutionState.ACTIVE,
					closure.registry.getResolution(record.getId()).getState());
		}
		assertTrue(closure.registry.getDiagnostics().isEmpty());
	}

	@Test
	void allocationIsDeterministicForAnInjectedSourceAndCoversEveryKind() {
		SpatialIdentityRegistry first = new SpatialIdentityRegistry(
				new SequentialTokenSource(21), 4);
		SpatialIdentityRegistry second = new SpatialIdentityRegistry(
				new SequentialTokenSource(21), 4);
		Map<SpatialIdentityKind, String> firstIds = allocateEveryKind(first);
		Map<SpatialIdentityKind, String> secondIds = allocateEveryKind(second);

		assertEquals(firstIds, secondIds);
		assertEquals(SpatialIdentityKind.values().length,
				first.getInstrumentation().getAllocations());
		assertEquals(SpatialIdentityKind.values().length,
				first.getInstrumentation().getAllocationsByKind().size());
	}

	@Test
	void retriesCollisionsWithinABoundedAllocation() {
		String first = token(1);
		String second = token(2);
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry(
				new ListedTokenSource(first, first, second), 2);

		assertEquals("geo:" + first, registry.allocatePersistentGeoId().toString());
		assertEquals("object:" + second,
				registry.allocateSpatialObjectId().toString());
		assertEquals(1, registry.getInstrumentation().getCollisions());
		assertEquals(3, registry.getInstrumentation().getAllocationAttempts());
	}

	@Test
	void collisionExhaustionPublishesNothing() {
		String repeated = token(1);
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry(
				new RepeatingTokenSource(repeated), 2);
		registry.allocatePersistentGeoId();

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				registry::allocateSpatialObjectId);

		assertEquals(SpatialIdentityDiagnostic.Code.ALLOCATION_EXHAUSTED,
				failure.getDiagnostic().getCode());
		assertTrue(registry.isEmpty());
		assertEquals(2, registry.getInstrumentation().getCollisions());
	}

	@Test
	void duplicateAndCrossKindTokensFailAtomically() {
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry(
				new SequentialTokenSource(1), 4);
		GeoElement geo = add("A=1");
		PersistentGeoId geoId = registry.allocatePersistentGeoId();
		GeoIdentityRecord geoRecord = geoRecord(geoId);
		registry.registerParticipation(geo, geoRecord);

		SpatialIdentityException duplicate = assertThrows(SpatialIdentityException.class,
				() -> registry.registerRecords(List.of(geoRecord)));
		ProjectionFrameRecord frame = new ProjectionFrameRecord(
				new ProjectionFrameId(geoId.getRawToken()), 1, List.of(geoId), 0);
		SpatialIdentityException crossKind = assertThrows(SpatialIdentityException.class,
				() -> registry.registerRecords(List.of(frame)));

		assertEquals(SpatialIdentityDiagnostic.Code.DUPLICATE_ID,
				duplicate.getDiagnostic().getCode());
		assertEquals(SpatialIdentityDiagnostic.Code.CROSS_KIND_TOKEN_REUSE,
				crossKind.getDiagnostic().getCode());
		assertEquals(1, registry.size());
		assertSame(geo, registry.getGeo(geoId));
	}

	@Test
	void missingTypedReferenceRemainsExplicitlyBroken() {
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry(
				new SequentialTokenSource(1), 4);
		SpatialObjectId objectId = registry.allocateSpatialObjectId();
		PersistentGeoId missingGeo = new PersistentGeoId(token(99));
		SpatialObjectRecord object = new SpatialObjectRecord(objectId, 1, FAMILY,
				EditAuthorityMode.PROJECTION_DEFINED, SCHEMA, 1,
				List.of(missingGeo), 0, 0);

		registry.registerRecords(List.of(object));

		assertEquals(SpatialResolutionState.BROKEN,
				registry.getResolution(objectId).getState());
		assertEquals(SpatialIdentityDiagnostic.Code.MISSING_REFERENCE,
				registry.getResolution(objectId).getDiagnostics().get(0).getCode());
		assertEquals(missingGeo,
				registry.getResolution(objectId).getDiagnostics().get(0).getReference());
	}

	@Test
	void clipboardImportsRemapTheCompleteClosureOnEveryPaste() {
		Closure source = createClosure(new SpatialIdentityRegistry(
				new SequentialTokenSource(1), 4), "A", "B");
		SpatialIdentityRegistry target = new SpatialIdentityRegistry(
				new SequentialTokenSource(101), 4);
		GeoElement firstA = add("C=3");
		GeoElement firstB = add("D=4");
		Map<SpatialIdentityId, SpatialIdentityId> first = importClosure(
				target, source, firstA, firstB);
		GeoElement secondA = add("E=5");
		GeoElement secondB = add("F=6");
		Map<SpatialIdentityId, SpatialIdentityId> second = importClosure(
				target, source, secondA, secondB);

		assertEquals(source.registry.size(), first.size());
		assertEquals(source.registry.size(), second.size());
		assertTrue(Collections.disjoint(new HashSet<>(first.values()),
				new HashSet<>(second.values())));
		assertWholeClosureRemapped(source, target, first);
		assertWholeClosureRemapped(source, target, second);
		assertSame(firstA, target.getGeo((PersistentGeoId) first.get(source.geoAId)));
		assertSame(secondB, target.getGeo((PersistentGeoId) second.get(source.geoBId)));
		assertEquals(2, target.getInstrumentation().getCopyCommits());
	}

	@Test
	void incompleteClipboardClosureCannotPublishAPartialRemap() {
		Closure source = createClosure(new SpatialIdentityRegistry(
				new SequentialTokenSource(1), 4), "A", "B");
		SpatialIdentityRegistry target = new SpatialIdentityRegistry(
				new SequentialTokenSource(101), 4);
		SpatialIdentityRegistry.LoadSession session = target.beginLoadSession(
				SpatialIdentityRegistry.LoadPurpose.CLIPBOARD_IMPORT);
		for (SpatialIdentityRecord record : source.registry.getRecords()) {
			if (record instanceof ProjectionBindingRecord) {
				session.stageRecord(record);
			}
		}

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				session::commit);

		assertEquals(SpatialIdentityDiagnostic.Code.INCOMPLETE_CLOSURE,
				failure.getDiagnostic().getCode());
		assertTrue(target.isEmpty());
	}

	@Test
	void macroInvocationsReceiveFreshCompleteClosures() {
		Closure template = createClosure(new SpatialIdentityRegistry(
				new SequentialTokenSource(1), 4), "A", "B");
		SpatialIdentityRegistry target = new SpatialIdentityRegistry(
				new SequentialTokenSource(101), 4);
		GeoElement firstA = add("C=3");
		GeoElement firstB = add("D=4");
		Map<SpatialIdentityId, SpatialIdentityId> first = instantiateMacro(
				target, template, firstA, firstB);
		GeoElement secondA = add("E=5");
		GeoElement secondB = add("F=6");
		Map<SpatialIdentityId, SpatialIdentityId> second = instantiateMacro(
				target, template, secondA, secondB);

		assertEquals(template.registry.size(), first.size());
		assertEquals(template.registry.size(), second.size());
		assertTrue(Collections.disjoint(new HashSet<>(first.values()),
				new HashSet<>(second.values())));
		assertWholeClosureRemapped(template, target, first);
		assertWholeClosureRemapped(template, target, second);
	}

	@Test
	void incompleteMacroMapFailsInsteadOfReferringToTemplateGeos() {
		Closure template = createClosure(new SpatialIdentityRegistry(
				new SequentialTokenSource(1), 4), "A", "B");
		SpatialIdentityRegistry target = new SpatialIdentityRegistry(
				new SequentialTokenSource(101), 4);
		SpatialIdentityRegistry.MacroInstantiationSession session =
				target.beginMacroInstantiation(template.registry, false);
		session.map(template.geoA, add("C=3"), true);

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				session::commit);

		assertEquals(SpatialIdentityDiagnostic.Code.MACRO_MAP_INCOMPLETE,
				failure.getDiagnostic().getCode());
		assertTrue(target.isEmpty());
	}

	@Test
	void deletionRetiresDependentsAndSameLabelRecreationIsFresh() {
		Closure closure = createClosure(new SpatialIdentityRegistry(
				new SequentialTokenSource(1), 4), "A", "B");

		Set<SpatialIdentityId> retired = closure.registry.retireGeo(closure.geoA);
		closure.geoA.remove();
		GeoElement recreated = add("A=1");
		PersistentGeoId replacementId = closure.registry.allocatePersistentGeoId();
		closure.registry.registerParticipation(recreated, geoRecord(replacementId));

		assertTrue(retired.contains(closure.geoAId));
		assertNull(closure.registry.getGeo(closure.geoAId));
		assertNotEquals(closure.geoAId, replacementId);
		assertSame(recreated, closure.registry.getGeo(replacementId));
		assertEquals(1, closure.registry.getInstrumentation().getDeleteCommits());
	}

	@Test
	void retiredTokenIsNeverAllocatedAgainWithinTheConstructionLifetime() {
		String retiredToken = token(1);
		String freshToken = token(2);
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry(
				new ListedTokenSource(retiredToken, retiredToken, freshToken), 2);
		GeoElement oldTarget = add("A=1");
		PersistentGeoId oldId = registry.allocatePersistentGeoId();
		registry.registerParticipation(oldTarget, geoRecord(oldId));

		registry.retireGeo(oldTarget);
		GeoElement restoredTarget = add("B=2");
		SpatialIdentityRegistry.LoadSession restore = registry.beginLoadSession(
				SpatialIdentityRegistry.LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		restore.stageRecord(geoRecord(oldId));
		restore.stageGeoAttachment(restoredTarget, oldId);
		restore.commit();
		assertSame(restoredTarget, registry.getGeo(oldId));
		assertEquals(oldId, registry.getPersistentGeoId(restoredTarget));

		registry.clear();
		PersistentGeoId replacement = registry.allocatePersistentGeoId();

		assertEquals("geo:" + freshToken, replacement.toString());
		assertNotEquals(oldId, replacement);
		assertEquals(1, registry.getInstrumentation().getCollisions());
	}

	@Test
	void recordImplementationMustMatchItsTypedIdKind() {
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry(
				new SequentialTokenSource(1), 4);
		SpatialIdentityRecord wrongKind = new WrongKindRecord(
				registry.allocatePersistentGeoId());

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> registry.registerRecords(List.of(wrongKind)));

		assertEquals(SpatialIdentityDiagnostic.Code.RECORD_KIND_MISMATCH,
				failure.getDiagnostic().getCode());
		assertTrue(registry.isEmpty());
	}

	@Test
	void productiveRegistryRejectsUnserializableAndForeignGeoAttachments() {
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry(
				getConstruction(), new SequentialTokenSource(1), 4);
		GeoNumeric unlabeled = new GeoNumeric(getConstruction());
		PersistentGeoId unlabeledId = registry.allocatePersistentGeoId();

		SpatialIdentityException unlabeledFailure = assertThrows(
				SpatialIdentityException.class,
				() -> registry.registerParticipation(unlabeled,
						geoRecord(unlabeledId)));

		assertEquals(SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE,
				unlabeledFailure.getDiagnostic().getCode());
		GeoNumeric foreign = new GeoNumeric(
				AppCommonFactory.create().getKernel().getConstruction(), 7);
		foreign.setLabel("Foreign");
		PersistentGeoId foreignId = registry.allocatePersistentGeoId();
		SpatialIdentityException foreignFailure = assertThrows(
				SpatialIdentityException.class,
				() -> registry.registerParticipation(foreign, geoRecord(foreignId)));
		assertEquals(SpatialIdentityDiagnostic.Code.GEO_ATTACHMENT_MISSING,
				foreignFailure.getDiagnostic().getCode());
		SpatialIdentityRegistry constructionRegistry =
				getConstruction().getSpatialIdentityRegistry();
		PersistentGeoId axisId = constructionRegistry.allocatePersistentGeoId();
		SpatialIdentityException axisFailure = assertThrows(
				SpatialIdentityException.class,
				() -> constructionRegistry.registerParticipation(
						getConstruction().getXAxis(), geoRecord(axisId)));
		assertEquals(SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE,
				axisFailure.getDiagnostic().getCode());
		assertFalse(getApp().getXML().contains(axisId.toExternalForm()));
		GeoCasCell casCell = new GeoCasCell(getConstruction());
		getConstruction().addToConstructionList(casCell, false);
		assertTrue(casCell.setInput("CasTwin:=7"));
		casCell.computeOutput();
		GeoElement casTwin = casCell.getTwinGeo();
		assertNotNull(casTwin);
		assertTrue(casCell.setLabelOfTwinGeo());
		getConstruction().addToConstructionList(casTwin, true);
		assertTrue(casTwin.isLabelSet());
		assertTrue(getConstruction().isInConstructionList(casTwin));
		assertSame(casCell, casTwin.getCorrespondingCasCell());
		PersistentGeoId casTwinId = constructionRegistry.allocatePersistentGeoId();
		SpatialIdentityException casTwinFailure = assertThrows(
				SpatialIdentityException.class,
				() -> constructionRegistry.registerParticipation(
						casTwin, geoRecord(casTwinId)));
		assertEquals(SpatialIdentityDiagnostic.Code.GEO_NOT_SERIALIZABLE,
				casTwinFailure.getDiagnostic().getCode());
		assertFalse(getApp().getXML().contains(casTwinId.toExternalForm()));
		reload();
		assertTrue(getConstruction().getSpatialIdentityRegistry().isEmpty());
		assertTrue(registry.isEmpty());
	}

	@Test
	void forbiddenAuthorityCountersAreHardZero() {
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry(
				new SequentialTokenSource(1), 4);
		registry.allocatePersistentGeoId();

		assertEquals(0, registry.getInstrumentation().getLabelAuthorityUses());
		assertEquals(0, registry.getInstrumentation().getCoordinateAuthorityUses());
		assertEquals(0,
				registry.getInstrumentation().getConstructionOrderAuthorityUses());
		assertEquals(0, registry.getInstrumentation().getXmlPositionAuthorityUses());
		assertEquals(0, registry.getInstrumentation().getOutputOrdinalAuthorityUses());
		assertEquals(0, registry.getInstrumentation().getViewportAuthorityUses());
		assertEquals(0, registry.getInstrumentation().getDpiAuthorityUses());
		assertEquals(0, registry.getInstrumentation().getCameraAuthorityUses());
		assertEquals(0, registry.getInstrumentation().getJavaInstanceAuthorityUses());
		assertEquals(0, registry.getInstrumentation().getRendererAuthorityUses());
		assertEquals(0, registry.getInstrumentation().getScreenStateAuthorityUses());
		assertEquals(0, registry.getInstrumentation().getProjectionEvaluations());
		assertEquals(0, registry.getInstrumentation().getReconstructionEvaluations());
		assertEquals(0, registry.getInstrumentation().getDiagramMapEvaluations());
		assertEquals(0, registry.getInstrumentation().getHingeEvaluations());
		assertEquals(0, registry.getInstrumentation().getCertificatePublications());
		assertEquals(0,
				registry.getInstrumentation().getStaleSpatialPayloadPublications());
	}

	private Closure createClosure(SpatialIdentityRegistry registry,
			String firstLabel, String secondLabel) {
		GeoElement geoA = add(firstLabel + "=1");
		GeoElement geoB = add(secondLabel + "=2");
		PersistentGeoId geoAId = registry.allocatePersistentGeoId();
		PersistentGeoId geoBId = registry.allocatePersistentGeoId();
		SpatialObjectId objectId = registry.allocateSpatialObjectId();
		ProjectionFrameId frameAId = registry.allocateProjectionFrameId();
		ProjectionSystemId systemId = registry.allocateProjectionSystemId();
		ProjectionDiagramMapId mapAId = registry.allocateProjectionDiagramMapId();
		ProjectionFrameRelationId relationId =
				registry.allocateProjectionFrameRelationId();
		ProjectionBindingId bindingId = registry.allocateProjectionBindingId();
		ProjectionFrameId frameBId = registry.allocateProjectionFrameId();
		ProjectionDiagramMapId mapBId = registry.allocateProjectionDiagramMapId();

		registry.registerParticipation(geoA, geoRecord(geoAId));
		registry.registerParticipation(geoB, geoRecord(geoBId));
		registry.registerRecords(List.of(
				new SpatialObjectRecord(objectId, 1, FAMILY,
						EditAuthorityMode.PROJECTION_DEFINED, SCHEMA, 1,
						List.of(geoAId), 0, 0),
				new ProjectionFrameRecord(frameAId, 1, List.of(geoAId), 0),
				new ProjectionFrameRecord(frameBId, 1, List.of(geoBId), 0),
				new ProjectionSystemRecord(systemId, 1, List.of(mapAId, mapBId),
						List.of(relationId), List.of(geoAId, geoBId), 0),
				new ProjectionDiagramMapRecord(mapAId, 1, systemId, frameAId,
						ProjectionFrameUseRole.DEFINING, "ORIENTED_ISOMETRY",
						List.of(relationId), List.of(geoAId), 0),
				new ProjectionDiagramMapRecord(mapBId, 1, systemId, frameBId,
						ProjectionFrameUseRole.AUXILIARY, "ORIENTED_ISOMETRY",
						List.of(relationId), List.of(geoBId), 0),
				new ProjectionFrameRelationRecord(relationId, 1, systemId, mapAId,
						mapBId, "HINGE_UNFOLD", List.of(geoAId), 0),
				new ProjectionBindingRecord(bindingId, 1, objectId, systemId, mapAId,
						frameAId, ProjectionBindingRole.DEFINING, "POINT", FAMILY,
						SCHEMA, 1, List.of(geoAId), 0)));
		return new Closure(registry, geoA, geoB, geoAId, geoBId);
	}

	private static GeoIdentityRecord geoRecord(PersistentGeoId id) {
		return new GeoIdentityRecord(id, PROVIDER, FAMILY, SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				"POINT", 1, 0, 0);
	}

	private Map<SpatialIdentityId, SpatialIdentityId> importClosure(
			SpatialIdentityRegistry target, Closure source,
			GeoElement targetA, GeoElement targetB) {
		SpatialIdentityRegistry.LoadSession session = target.beginLoadSession(
				SpatialIdentityRegistry.LoadPurpose.CLIPBOARD_IMPORT);
		for (SpatialIdentityRecord record : source.registry.getRecords()) {
			session.stageRecord(record);
		}
		session.stageElementAttachment(targetA, source.geoAId);
		session.stageElementAttachment(targetB, source.geoBId);
		return session.commit();
	}

	private Map<SpatialIdentityId, SpatialIdentityId> instantiateMacro(
			SpatialIdentityRegistry target, Closure template,
			GeoElement targetA, GeoElement targetB) {
		SpatialIdentityRegistry.MacroInstantiationSession session =
				target.beginMacroInstantiation(template.registry, false);
		session.map(template.geoA, targetA, true);
		session.map(template.geoB, targetB, true);
		return session.commit();
	}

	private static void assertWholeClosureRemapped(Closure source,
			SpatialIdentityRegistry target,
			Map<SpatialIdentityId, SpatialIdentityId> remap) {
		for (SpatialIdentityRecord original : source.registry.getRecords()) {
			SpatialIdentityRecord copy = target.getRecord(remap.get(original.getId()));
			assertEquals(original.getId(), copy.getCopySourceId());
			for (SpatialIdentityId reference : original.getReferences()) {
				assertTrue(copy.getReferences().contains(remap.get(reference)));
				assertFalse(copy.getReferences().contains(reference));
			}
		}
	}

	private static Map<SpatialIdentityKind, String> allocateEveryKind(
			SpatialIdentityRegistry registry) {
		Map<SpatialIdentityKind, String> result =
				new EnumMap<>(SpatialIdentityKind.class);
		for (SpatialIdentityKind kind : SpatialIdentityKind.values()) {
			result.put(kind, registry.allocate(kind).toString());
		}
		return result;
	}

	private static String token(int value) {
		return String.format("%032x", value);
	}

	private static final class Closure {
		private final SpatialIdentityRegistry registry;
		private final GeoElement geoA;
		private final GeoElement geoB;
		private final PersistentGeoId geoAId;
		private final PersistentGeoId geoBId;

		private Closure(SpatialIdentityRegistry registry, GeoElement geoA,
				GeoElement geoB, PersistentGeoId geoAId, PersistentGeoId geoBId) {
			this.registry = registry;
			this.geoA = geoA;
			this.geoB = geoB;
			this.geoAId = geoAId;
			this.geoBId = geoBId;
		}
	}

	private static final class SequentialTokenSource implements SpatialTokenSource {
		private int next;

		private SequentialTokenSource(int first) {
			next = first;
		}

		@Override
		public String nextToken() {
			return token(next++);
		}
	}

	private static final class ListedTokenSource implements SpatialTokenSource {
		private final List<String> tokens = new ArrayList<>();

		private ListedTokenSource(String... tokens) {
			Collections.addAll(this.tokens, tokens);
		}

		@Override
		public String nextToken() {
			return tokens.remove(0);
		}
	}

	private static final class RepeatingTokenSource implements SpatialTokenSource {
		private final String token;

		private RepeatingTokenSource(String token) {
			this.token = token;
		}

		@Override
		public String nextToken() {
			return token;
		}
	}

	private static final class WrongKindRecord implements SpatialIdentityRecord {
		private final PersistentGeoId id;

		private WrongKindRecord(PersistentGeoId id) {
			this.id = id;
		}

		@Override
		public SpatialIdentityId getId() {
			return id;
		}

		@Override
		public int getSemanticVersion() {
			return SpatialIdentityRegistry.XML_VERSION;
		}

		@Override
		public String getXmlElementName() {
			return "wrongKind";
		}

		@Override
		public List<SpatialIdentityId> getReferences() {
			return List.of();
		}

		@Override
		public SpatialIdentityId getCopySourceId() {
			return null;
		}

		@Override
		public SpatialIdentityRecord remap(
				Map<SpatialIdentityId, SpatialIdentityId> remap,
				boolean recordImmediateCopySource) {
			return this;
		}
	}
}
