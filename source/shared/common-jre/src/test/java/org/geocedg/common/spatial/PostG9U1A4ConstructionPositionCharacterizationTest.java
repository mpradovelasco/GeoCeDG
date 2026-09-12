/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineDecision;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineSignature;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Test-only characterization of current construction ordering and rollback. */
class PostG9U1A4ConstructionPositionCharacterizationTest
		extends G9A3SpatialRedefineTestSupport {

	@Test
	void compatibleNewInstanceMayMoveAfterAnIndependentNeighbor() {
		add("A4P=(0,0)");
		add("A4Q=(1,0)");
		add("A4R=(0,1)");
		add("A4g=Line(A4Q,A4R)");
		GeoElement target = add("A4l=Line(A4P,A4Q)");
		GeoElement independentAfter = add("A4n=7");
		SpatialRedefineSignature signature = signature(PROVIDER, "LINE", "AXIS", 1);
		final GeoIdentityRecord original = register(target, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER,
				signature, SpatialRedefineDecision.RETAIN));
		int targetIndex = target.getConstructionIndex();
		int neighborIndex = independentAfter.getConstructionIndex();

		edit(target, "A4l=PerpendicularLine(A4P,A4g)");

		GeoElement actual = lookup("A4l");
		assertNotSame(target, actual);
		assertEquals(original.getId(), registry().getPersistentGeoId(actual));
		assertTrue(targetIndex < neighborIndex);
		assertTrue(independentAfter.getConstructionIndex()
				< actual.getConstructionIndex());
		for (GeoElement input : actual.getParentAlgorithm().getInput()) {
			assertTrue(input.getConstructionIndex() < actual.getConstructionIndex());
		}
	}

	@Test
	void failedRedefineRestoresExactOrderByXmlButNotJavaOrNavigationStep()
			throws Exception {
		GeoElement before = add("A4Before=1");
		GeoNumeric target = add("A4Target=2");
		GeoElement dependent = add("A4Dependent=A4Target+1");
		GeoElement after = add("A4After=9");
		SpatialRedefineSignature signature = signature(PROVIDER, "NUMERIC", "VALUE",
				1);
		final GeoIdentityRecord original = register(target, signature);
		registry().registerRedefineProvider(new CompatibleProvider(PROVIDER,
				signature, SpatialRedefineDecision.REJECT));
		int beforeIndex = before.getConstructionIndex();
		int targetIndex = target.getConstructionIndex();
		int dependentIndex = dependent.getConstructionIndex();
		int afterIndex = after.getConstructionIndex();
		String rollbackXml = getConstruction().getCurrentUndoXML(false).toString();
		getConstruction().setStep(targetIndex);
		final int navigationStep = getConstruction().getStep();
		GeoNumeric candidate = new GeoNumeric(getConstruction(), 5);

		assertThrows(SpatialIdentityException.class,
				() -> getConstruction().replace(target, candidate));

		GeoElement restoredBefore = lookup("A4Before");
		GeoNumeric restoredTarget = (GeoNumeric) lookup("A4Target");
		GeoElement restoredDependent = lookup("A4Dependent");
		GeoElement restoredAfter = lookup("A4After");
		assertEquals(rollbackXml,
				getConstruction().getCurrentUndoXML(false).toString());
		assertEquals(beforeIndex, restoredBefore.getConstructionIndex());
		assertEquals(targetIndex, restoredTarget.getConstructionIndex());
		assertEquals(dependentIndex, restoredDependent.getConstructionIndex());
		assertEquals(afterIndex, restoredAfter.getConstructionIndex());
		assertEquals(original.getId(), registry().getPersistentGeoId(restoredTarget));
		assertSame(restoredTarget,
				restoredDependent.getParentAlgorithm().getInput(0));
		assertNotSame(before, restoredBefore);
		assertNotSame(target, restoredTarget);
		assertNotSame(dependent, restoredDependent);
		assertNotSame(after, restoredAfter);
		assertNotEquals(navigationStep, getConstruction().getStep());
		assertEquals(getConstruction().steps() - 1, getConstruction().getStep());
	}

	@Test
	void saveReopenReconstructsExactOrderAndDurableIdentityWithNewInstances() {
		GeoElement before = add("A4SaveBefore=1");
		GeoNumeric target = add("A4SaveTarget=2");
		GeoElement dependent = add("A4SaveDependent=A4SaveTarget+1");
		GeoElement after = add("A4SaveAfter=9");
		SpatialRedefineSignature signature = signature(PROVIDER, "NUMERIC", "VALUE",
				1);
		PersistentGeoId targetId = register(target, signature).getId();
		int beforeIndex = before.getConstructionIndex();
		int targetIndex = target.getConstructionIndex();
		int dependentIndex = dependent.getConstructionIndex();
		int afterIndex = after.getConstructionIndex();
		String xml = getApp().getXML();

		getApp().setXML(xml, true);

		GeoElement restoredBefore = lookup("A4SaveBefore");
		GeoNumeric restoredTarget = (GeoNumeric) lookup("A4SaveTarget");
		GeoElement restoredDependent = lookup("A4SaveDependent");
		GeoElement restoredAfter = lookup("A4SaveAfter");
		assertEquals(beforeIndex, restoredBefore.getConstructionIndex());
		assertEquals(targetIndex, restoredTarget.getConstructionIndex());
		assertEquals(dependentIndex, restoredDependent.getConstructionIndex());
		assertEquals(afterIndex, restoredAfter.getConstructionIndex());
		assertEquals(targetId, registry().getPersistentGeoId(restoredTarget));
		assertSame(restoredTarget,
				restoredDependent.getParentAlgorithm().getInput(0));
		assertNotSame(before, restoredBefore);
		assertNotSame(target, restoredTarget);
		assertNotSame(dependent, restoredDependent);
		assertNotSame(after, restoredAfter);
	}
}
