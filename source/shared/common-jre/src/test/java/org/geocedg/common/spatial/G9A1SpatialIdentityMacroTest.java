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

import java.util.Arrays;
import java.util.List;

import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.gui.dialog.ToolCreationDialogModel;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

class G9A1SpatialIdentityMacroTest extends BaseUnitTest {
	private static final String PROVIDER = "g9a1.test";

	@Test
	void eachRealMacroInvocationInstantiatesAFreshSemanticClosure() {
		GeoElement inputA = add("A=(1,1)");
		GeoElement inputB = add("B=(2,2)");
		GeoElement output = add("f=Line(A,B)");
		GeoIdentityRecord sourceRecord = register(output);
		createMacro("SpatialLine", output, inputA, inputB);
		Macro macro = getKernel().getMacro("SpatialLine");
		SpatialIdentityRegistry templateRegistry =
				macro.getMacroConstruction().getSpatialIdentityRegistry();
		PersistentGeoId templateId = templateRegistry.getPersistentGeoId(
				macro.getMacroOutput()[0]);

		GeoElement first = add("g=SpatialLine((1,3),(2,3))");
		GeoElement second = add("h=SpatialLine((1,4),(2,4))");
		PersistentGeoId firstId = registry().getPersistentGeoId(first);
		PersistentGeoId secondId = registry().getPersistentGeoId(second);

		assertEquals(sourceRecord.getId(), templateId);
		assertEquals(1, templateRegistry.size());
		assertSame(macro.getMacroOutput()[0], templateRegistry.getGeo(templateId));
		assertNotNull(firstId);
		assertNotNull(secondId);
		assertNotEquals(templateId, firstId);
		assertNotEquals(templateId, secondId);
		assertNotEquals(firstId, secondId);
		assertEquals(templateId, registry().getGeoRecord(firstId).getCopySourceId());
		assertEquals(templateId, registry().getGeoRecord(secondId).getCopySourceId());
	}

	@Test
	void rejectedSemanticMacroInvocationRemovesAlgorithmOutputAndUseRegistration() {
		GeoElement inputA = add("A=(1,1)");
		GeoElement inputB = add("B=(2,2)");
		GeoElement output = add("f=Line(A,B)");
		GeoIdentityRecord inputRecord = register(inputA, "POINT",
				"cedg.point.orthographic", "POINT");
		GeoIdentityRecord outputRecord = register(output);
		registry().registerRecords(List.of(new SpatialObjectRecord(
				registry().allocateSpatialObjectId(), 1, "LINE",
				EditAuthorityMode.PROJECTION_DEFINED,
				"cedg.line.orthographic", 1,
				List.of(inputRecord.getId(), outputRecord.getId()), 0, 0)));
		createMacro("RejectedSpatialLine", output, inputA, inputB);
		Macro macro = getKernel().getMacro("RejectedSpatialLine");
		GeoElement callerA = add("C=(1,3)");
		GeoElement callerB = add("D=(2,3)");
		int originalSteps = getConstruction().steps();
		String originalXml = getApp().getXML();
		String originalSection = registry().writeSpatialSection();

		assertFalse(macro.isUsed());
		GeoElement[] result = getKernel().useMacro(
				new String[] {"RejectedOutput"}, macro,
				new GeoElement[] {callerA, callerB});

		assertNull(result);
		assertNull(lookup("RejectedOutput"));
		assertFalse(macro.isUsed());
		assertEquals(originalSteps, getConstruction().steps());
		assertEquals(originalXml, getApp().getXML());
		assertEquals(originalSection, registry().writeSpatialSection());
		assertEquals(3, registry().size());
	}

	private GeoIdentityRecord register(GeoElement geo) {
		return register(geo, "LINE", "cedg.line.orthographic", "LINE");
	}

	private GeoIdentityRecord register(GeoElement geo, String family,
			String schema, String outputRole) {
		GeoIdentityRecord record = new GeoIdentityRecord(
				registry().allocatePersistentGeoId(), PROVIDER, family,
				schema, 1,
				EditAuthorityMode.PROJECTION_DEFINED, ProjectionBindingRole.DEFINING,
				outputRole, 1, 0, 0);
		registry().registerParticipation(geo, record);
		return record;
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private void createMacro(String name, GeoElement output, GeoElement... inputs) {
		ToolCreationDialogModel builder = new ToolCreationDialogModel(getApp(),
				() -> { /* no UI in this host integration test */ });
		Arrays.stream(inputs).forEach(builder::addToInput);
		builder.addToOutput(output);
		builder.createTool();
		builder.finish(getApp(), name, name, inputs.length + " inputs expected",
				false, null);
	}
}
