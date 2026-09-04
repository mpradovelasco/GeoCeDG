/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.LoadPurpose;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.LoadSession;
import org.geocedg.common.kernel.spatial.identity.SpatialLifecycleRuntime;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineCandidateParticipation;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineContext;
import org.geocedg.common.kernel.spatial.identity.SpatialTokenSource;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

/** Producer coherence only: no loader repair or new semantic-point admission. */
class G9U1ConstructionParticipationReviewTest extends BaseUnitTest {

	@Test
	void lateParticipationPreservesExistingListIdentityAndRevisions() {
		SpatialIdentityRegistry registry = registry();
		GeoElement a = add("A=(0,0)");
		add("B=(1,0)");
		GeoElement list = add("L={A,B}");
		GeoIdentityRecord original = record(registry, list, List.of());
		registry.registerConstructionParticipations(Map.of(list, original));
		GeoIdentityRecord input = record(registry, a, List.of());

		registry.registerConstructionParticipations(Map.of(a, input));

		GeoIdentityRecord actual = registry.getGeoRecord(original.getId());
		assertEquals(withDependencies(original, List.of(input.getId())), actual);
		assertSame(list, registry.getGeo(original.getId()));
		assertEquals(original.getId(), registry.getPersistentGeoId(list));
		assertEquals(7, actual.getDefinitionRevision());
		assertEquals(3, actual.getTopologyRevision());
	}

	@Test
	void singletonPublicationAndLaterBatchYieldExactReloadableDependencies() {
		SpatialIdentityRegistry registry = registry();
		GeoElement a = add("A=(0,0)");
		GeoElement b = add("B=(1,0)");
		GeoElement c = add("C=(2,0)");
		GeoElement list = add("L={A,B,C}");
		GeoIdentityRecord original = record(registry, list, List.of());
		registry.registerConstructionParticipations(Map.of(list, original));
		GeoIdentityRecord first = record(registry, c, List.of());
		registry.registerParticipation(c, first);
		GeoIdentityRecord second = record(registry, a, List.of());
		GeoIdentityRecord third = record(registry, b, List.of());
		registry.registerConstructionParticipations(Map.of(a, second, b, third));
		ArrayList<PersistentGeoId> ids = new ArrayList<>(List.of(first.getId(),
				second.getId(), third.getId()));
		Collections.sort(ids);
		assertEquals(withDependencies(original, ids),
				registry.getGeoRecord(original.getId()));

		SpatialIdentityRegistry reopened = registry();
		LoadSession load = reopened.beginLoadSession(LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		for (GeoElement geo : List.of(list, a, b, c)) {
			PersistentGeoId id = registry.getPersistentGeoId(geo);
			load.stageGeoAttachment(geo, id);
			load.stageRecord(registry.getGeoRecord(id));
		}
		load.commit();
		assertEquals(registry.writeSpatialSection(), reopened.writeSpatialSection());
	}

	@Test
	void failedRuntimeSwitchRollsBackNewInputAndExistingDerivedRecordTogether() {
		SpatialIdentityRegistry registry = registry();
		GeoElement a = add("A=(0,0)");
		GeoElement list = add("L={A}");
		GeoIdentityRecord original = record(registry, list, List.of());
		registry.registerConstructionParticipations(Map.of(list, original));
		GeoIdentityRecord input = record(registry, a, List.of());
		String before = registry.writeSpatialSection();
		boolean[] rolledBack = {false};
		registry.registerLifecycleRuntime(graph -> {
			assertEquals(original, registry.getGeoRecord(original.getId()));
			assertNull(registry.getPersistentGeoId(a));
			return new SpatialLifecycleRuntime.PreparedSwitch() {
				@Override
				public void commit() {
					assertEquals(List.of(input.getId()), registry.getGeoRecord(
							original.getId()).getDependencies());
					assertEquals(input.getId(), registry.getPersistentGeoId(a));
					throw new IllegalStateException("Injected runtime switch failure");
				}

				@Override
				public void rollback() {
					rolledBack[0] = true;
				}
			};
		});

		assertThrows(SpatialIdentityException.class,
				() -> registry.registerConstructionParticipations(Map.of(a, input)));
		assertTrue(rolledBack[0]);
		assertEquals(before, registry.writeSpatialSection());
		assertSame(original, registry.getGeoRecord(original.getId()));
		assertNull(registry.getPersistentGeoId(a));
	}

	@Test
	void malformedNewBatchCannotRefreshOrPartiallyPublishAnything() {
		SpatialIdentityRegistry registry = registry();
		GeoElement a = add("A=(0,0)");
		GeoElement list = add("L={A}");
		GeoIdentityRecord original = record(registry, list, List.of());
		registry.registerConstructionParticipations(Map.of(list, original));
		String before = registry.writeSpatialSection();
		GeoIdentityRecord invalid = record(registry, a, List.of(original.getId()));

		assertThrows(SpatialIdentityException.class,
				() -> registry.registerConstructionParticipations(Map.of(a, invalid)));
		assertEquals(before, registry.writeSpatialSection());
		assertSame(original, registry.getGeoRecord(original.getId()));
		assertNull(registry.getPersistentGeoId(a));
	}

	@Test
	void historicalMissingDependencyArchiveStillFailsClosedWithoutRepair() {
		SpatialIdentityRegistry registry = registry();
		GeoElement a = add("A=(0,0)");
		GeoElement list = add("L={A}");
		GeoIdentityRecord stale = record(registry, list, List.of());
		GeoIdentityRecord input = record(registry, a, List.of());
		LoadSession load = registry.beginLoadSession(LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		load.stageGeoAttachment(list, stale.getId());
		load.stageRecord(stale);
		load.stageGeoAttachment(a, input.getId());
		load.stageRecord(input);

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				load::commit);
		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				failure.getDiagnostic().getCode());
		assertTrue(registry.isEmpty());
	}

	@Test
	void g9aCandidateStagingDoesNotRefreshLiveExistingRecords() {
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		GeoElement a = add("A=(0,0)");
		GeoElement list = add("L={A}");
		GeoIdentityRecord original = record(registry, list, List.of());
		registry.registerConstructionParticipations(Map.of(list, original));
		String before = registry.writeSpatialSection();
		SpatialRedefineContext context = registry.captureRedefineContext(list);
		try (SpatialRedefineCandidateParticipation scope =
				registry.beginRedefineCandidateParticipation(context)) {
			GeoIdentityRecord input = record(registry, a, List.of());
			registry.stageRedefineCandidateParticipations(Map.of(a, input));
			assertEquals(input.getId(), scope.getPersistentGeoId(a));
			assertNull(registry.getPersistentGeoId(a));
			assertSame(original, registry.getGeoRecord(original.getId()));
			assertEquals(before, registry.writeSpatialSection());
		}
		assertFalse(registry.isRedefineCandidateParticipationActive());
		assertEquals(before, registry.writeSpatialSection());
		assertNull(registry.getPersistentGeoId(a));
	}

	@Test
	void g9aSealedProviderPermissionCannotChangeCapturedDependencySignature() {
		SpatialIdentityRegistry registry = getConstruction().getSpatialIdentityRegistry();
		GeoElement a = add("A=(0,0)");
		GeoElement list = add("L={A}");
		GeoIdentityRecord original = record(registry, list, List.of());
		registry.registerConstructionParticipations(Map.of(list, original));
		String before = registry.writeSpatialSection();
		SpatialRedefineContext context = registry.captureRedefineContext(list);
		try (SpatialRedefineCandidateParticipation scope =
				registry.beginRedefineCandidateParticipation(context)) {
			scope.seal();
			GeoIdentityRecord input = record(registry, a, List.of());
			SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
					() -> registry.registerConstructionParticipations(Map.of(a, input)));
			assertEquals(SpatialIdentityDiagnostic.Code.TRANSACTION_STATE,
					failure.getDiagnostic().getCode());
			assertEquals(before, registry.writeSpatialSection());
			assertSame(original, registry.getGeoRecord(original.getId()));
			assertNull(registry.getPersistentGeoId(a));
		}
	}

	private static GeoIdentityRecord record(SpatialIdentityRegistry registry,
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

	private static GeoIdentityRecord withDependencies(GeoIdentityRecord source,
			List<PersistentGeoId> dependencies) {
		return new GeoIdentityRecord(source.getId(), source.getProvider(),
				source.getFamily(), source.getSchemaId(), source.getSchemaVersion(),
				source.getAuthority(), source.getBindingRole(), source.getStableOutputRole(),
				source.getOutputCardinality(), dependencies, source.getDefinitionRevision(),
				source.getTopologyRevision(), source.getCopySourceId());
	}

	private static SpatialIdentityRegistry registry() {
		return new SpatialIdentityRegistry(new SpatialTokenSource() {
			private int next = 1;

			@Override
			public String nextToken() {
				return String.format(java.util.Locale.ROOT, "%032x", next++);
			}
		}, 4);
	}
}
