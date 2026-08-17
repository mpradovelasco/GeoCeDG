/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.geocedg.common.kernel.spatial.identity.SpatialResolutionState;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.plugin.EventListener;
import org.geogebra.common.plugin.EventType;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.test.EventAccumulator;
import org.junit.jupiter.api.Test;

class G9A1SpatialIdentityLifecycleTest extends BaseUnitTest {
	private static final String PROVIDER = "g9a1.test";
	private static final String FAMILY = "POINT";
	private static final String SCHEMA = "cedg.point.orthographic";

	@Test
	void renameAndRecomputePreserveIdentityAndProviderRevisions() {
		GeoNumeric geo = add("A=1");
		GeoIdentityRecord record = register(geo);

		geo.rename("Renamed");
		geo.setValue(7);
		geo.updateRepaint();
		getKernel().updateConstruction(false);

		assertSame(geo, lookup("Renamed"));
		assertEquals(record.getId(), registry().getPersistentGeoId(geo));
		assertEquals(record, registry().getGeoRecord(record.getId()));
	}

	@Test
	void deleteAndSameLabelRecreateNeverInferContinuity() {
		GeoElement original = add("A=1");
		GeoIdentityRecord originalRecord = register(original);

		original.remove();
		GeoElement recreated = add("A=1");

		assertNull(registry().getRecord(originalRecord.getId()));
		assertNull(registry().getPersistentGeoId(recreated));
		GeoIdentityRecord recreatedRecord = register(recreated);
		assertNotEquals(originalRecord.getId(), recreatedRecord.getId());
	}

	@Test
	void serializedUndoAndRedoRestoreExactIdentityGraph() {
		activateUndo();
		GeoElement geo = add("A=1");
		PersistentGeoId originalId = register(geo).getId();
		getApp().storeUndoInfo();
		geo.remove();
		getApp().storeUndoInfo();

		getKernel().undo();

		assertEquals(originalId, registry().getPersistentGeoId(lookup("A")));
		assertSame(lookup("A"), registry().getGeo(originalId));

		getKernel().redo();

		assertNull(lookup("A"));
		assertTrue(registry().isEmpty());

		GeoElement impostor = add("B=2");
		GeoIdentityRecord reusedRecord = new GeoIdentityRecord(originalId,
				PROVIDER, FAMILY, SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED,
				ProjectionBindingRole.DEFINING, "POINT", 1, 0, 0);
		SpatialIdentityException reuse = assertThrows(SpatialIdentityException.class,
				() -> registry().registerParticipation(impostor, reusedRecord));

		assertEquals(SpatialIdentityDiagnostic.Code.RETIRED_ID_REUSE,
				reuse.getDiagnostic().getCode());
		assertNull(registry().getPersistentGeoId(impostor));
		assertTrue(registry().isEmpty());
	}

	@Test
	void realClipboardExpandsAndRemapsTheFullProjectionClosureTwice()
			throws Exception {
		loadCompleteFixture();
		final Set<SpatialIdentityId> originalIds = ids(registry());
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(lookup("A")), text -> text);
		assertThat(clipboard, containsString("<geocedgSpatial version=\"1\">"));
		AtomicBoolean prePasteSeen = new AtomicBoolean();
		AtomicBoolean pastedGeoAbsentAtNotification = new AtomicBoolean();
		EventListener timingListener = event -> {
			if (event.type == EventType.PASTE_ELMS
					&& prePasteSeen.compareAndSet(false, true)) {
				pastedGeoAbsentAtNotification.set(lookup("A_{1}") == null);
			}
		};
		getApp().getEventDispatcher().addEventListener(timingListener);

		paste(clipboard);
		getApp().getEventDispatcher().removeEventListener(timingListener);
		assertTrue(prePasteSeen.get());
		assertTrue(pastedGeoAbsentAtNotification.get());
		paste(clipboard);

		List<SpatialIdentityRecord> copies = registry().getRecords().stream()
				.filter(record -> record.getCopySourceId() != null)
				.collect(Collectors.toList());
		assertEquals(20, copies.size());
		assertEquals(30, registry().size());
		for (SpatialIdentityRecord copy : copies) {
			assertTrue(originalIds.contains(copy.getCopySourceId()));
			assertFalse(originalIds.contains(copy.getId()));
			assertEquals(SpatialResolutionState.ACTIVE,
					registry().getResolution(copy.getId()).getState());
			for (SpatialIdentityId reference : copy.getReferences()) {
				assertFalse(originalIds.contains(reference));
			}
		}
		for (SpatialIdentityId originalId : originalIds) {
			assertEquals(2, copies.stream()
					.filter(copy -> originalId.equals(copy.getCopySourceId())).count());
		}
	}

	@Test
	void clipboardUndoAndRedoRestoreTheExactRemappedIds() throws Exception {
		loadCompleteFixture();
		activateUndo();
		getApp().storeUndoInfo();
		Set<SpatialIdentityId> originalIds = ids(registry());
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(lookup("A")), text -> text);

		paste(clipboard);
		Set<SpatialIdentityId> pastedIds = ids(registry());
		assertEquals(20, pastedIds.size());

		getKernel().undo();
		assertEquals(originalIds, ids(registry()));

		getKernel().redo();
		assertEquals(pastedIds, ids(registry()));
	}

	@Test
	void rejectedClipboardImportRestoresConstructionRegistryAndUndoAndStopsPaste()
			throws Exception {
		loadCompleteFixture();
		activateUndo();
		getApp().storeUndoInfo();
		String clipboard = InternalClipboard.getTextToSave(getApp(),
				Collections.singletonList(lookup("A")), text -> text);
		String rejectedClipboard = clipboard.replace(
				"object=\"object:00000000000000000000000000000003\"",
				"object=\"object:ffffffffffffffffffffffffffffffff\"");
		String originalXml = getApp().getXML();
		String originalSection = registry().writeSpatialSection();
		Set<SpatialIdentityId> originalIds = ids(registry());
		final int originalSteps = getConstruction().steps();
		final int originalUndoSize = getConstruction().getUndoManager().getHistorySize();
		EventAccumulator events = new EventAccumulator();
		getApp().getEventDispatcher().addEventListener(events);

		assertNotEquals(clipboard, rejectedClipboard);
		paste(rejectedClipboard);
		String singleQuotedAttachment = "<element type='numeric' label='Rejected' "
				+ "geocedgId = 'geo:0000000000000000000000000000abcd'>"
				+ "<value val='3'/></element>";
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				Collections.singletonList("Rejected"), singleQuotedAttachment);

		assertEquals(originalXml, getApp().getXML());
		assertEquals(originalSection, registry().writeSpatialSection());
		assertEquals(originalIds, ids(registry()));
		assertEquals(originalSteps, getConstruction().steps());
		assertEquals(originalUndoSize,
				getConstruction().getUndoManager().getHistorySize());
		assertEquals(1, lookup("A").evaluateDouble());
		assertEquals(2, lookup("B").evaluateDouble());
		assertFalse(getApp().isBlockUpdateScripts());
		assertFalse(getConstruction().isFileLoading());
		assertEquals(0, registry().getInstrumentation().getCopyRollbacks());
		assertTrue(events.getEvents().stream()
				.noneMatch(event -> event.startsWith("PASTE_ELMS ")));
		assertNull(lookup("Rejected"));
	}

	private GeoIdentityRecord register(GeoElement geo) {
		GeoIdentityRecord record = new GeoIdentityRecord(
				registry().allocatePersistentGeoId(), PROVIDER, FAMILY, SCHEMA, 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				"POINT", 1, 0, 0);
		registry().registerParticipation(geo, record);
		return record;
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private void loadCompleteFixture() throws IOException, XMLParseException {
		Path path = Paths.get("src/test/resources/org/geocedg/common/spatial/g9a1",
				"complete-forward-closure.xml");
		String xml = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		getApp().getXMLio().processXMLString(xml, true, false, false);
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
