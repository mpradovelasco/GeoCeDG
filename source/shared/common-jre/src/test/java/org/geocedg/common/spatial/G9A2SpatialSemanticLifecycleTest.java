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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

class G9A2SpatialSemanticLifecycleTest extends BaseUnitTest {
	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D();
	}

	@Test
	void clipboardCopyTwiceRemapsEveryObjectAndReconstructsEachCopy()
			throws Exception {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		Set<SpatialIdentityId> originalIds = ids(registry());
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(graph.horizontalPointGeo), text -> text);

		paste(clipboard);
		paste(clipboard);

		List<SpatialObjectRecord> objectCopies = registry().getRecords().stream()
				.filter(SpatialObjectRecord.class::isInstance)
				.map(SpatialObjectRecord.class::cast)
				.filter(record -> graph.objectId.equals(record.getCopySourceId()))
				.collect(Collectors.toList());
		assertEquals(2, objectCopies.size());
		for (SpatialObjectRecord copy : objectCopies) {
			assertFalse(originalIds.contains(copy.getId()));
			assertEquals(SpatialCertificateStatus.VALID,
					getConstruction().getSpatialSemanticRuntime()
							.getSpatialPointCertificate(copy.getId())
							.getSemanticCertificate().getStatus());
			assertTrue(getConstruction().getSpatialSemanticRuntime()
					.getDerivedPoint(copy.getId()).isDefined());
		}
		for (SpatialIdentityRecord copied : registry().getRecords()) {
			if (copied.getCopySourceId() != null) {
				assertTrue(originalIds.contains(copied.getCopySourceId()));
				assertFalse(originalIds.contains(copied.getId()));
			}
		}
	}

	@Test
	void serializedUndoRedoRestoresAndWithdrawsTheExactPointIdentityGraph() {
		activateUndo();
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		getApp().storeUndoInfo();
		graph.horizontalPointGeo.remove();
		getApp().storeUndoInfo();

		assertNull(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId));

		getKernel().undo();

		assertEquals(SpatialCertificateStatus.VALID,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertNotNull(registry().getRecord(graph.objectId));
		assertEquals(graph.horizontalPointId,
				registry().getPersistentGeoId(lookup("G9A2PH")));

		getKernel().redo();

		assertNull(registry().getRecord(graph.objectId));
		assertNull(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId));
	}

	@Test
	void deleteAndSameLabelRecreateCannotReuseIssuedProjectedIdentity() {
		G9A2SpatialSemanticRuntimeTest.Graph graph = graph();
		PersistentGeoId retired = graph.horizontalPointId;

		graph.horizontalPointGeo.remove();
		GeoElement replacement = add("G9A2PH=(2,3)");

		assertNull(registry().getRecord(retired));
		assertNull(registry().getPersistentGeoId(replacement));
		GeoIdentityRecord forbiddenReuse = pointGeoRecord(retired);
		SpatialIdentityException reuse = assertThrows(SpatialIdentityException.class,
				() -> registry().registerParticipation(replacement, forbiddenReuse));
		assertEquals(SpatialIdentityDiagnostic.Code.RETIRED_ID_REUSE,
				reuse.getDiagnostic().getCode());

		PersistentGeoId fresh = registry().allocatePersistentGeoId();
		registry().registerParticipation(replacement, pointGeoRecord(fresh));
		assertNotEquals(retired, fresh);
		assertEquals(fresh, registry().getPersistentGeoId(replacement));
		assertNull(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId));
	}

	private G9A2SpatialSemanticRuntimeTest.Graph graph() {
		return G9A2SpatialSemanticRuntimeTest.Graph.create(getConstruction(), this::add);
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private static GeoIdentityRecord pointGeoRecord(PersistentGeoId id) {
		return new GeoIdentityRecord(id, "g9a2.test", "SEMANTIC_INPUT",
				SpatialObjectRecord.POINT_SCHEMA_ID, 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				"INPUT", 1, 0, 0);
	}

	private void paste(String clipboard) {
		int separator = clipboard.indexOf('\n');
		List<String> labels = new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" ")));
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), labels,
				clipboard.substring(separator));
	}

	private static Set<SpatialIdentityId> ids(SpatialIdentityRegistry registry) {
		return registry.getRecords().stream().map(SpatialIdentityRecord::getId)
				.collect(Collectors.toCollection(HashSet::new));
	}
}
