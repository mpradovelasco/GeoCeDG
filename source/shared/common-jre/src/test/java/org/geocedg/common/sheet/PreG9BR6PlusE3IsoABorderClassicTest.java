/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoPolyLine;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.test.BaseAppTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-E3 {@code OTQ-E3-1}: {@code IsoABorder} is a shared-kernel command
 * that the Classic profile of this fork parses, evaluates and persists exactly as
 * GeoCeDG does, with no feature gate.
 */
class PreG9BR6PlusE3IsoABorderClassicTest extends BaseAppTestSetup {

	@BeforeEach
	void setup() {
		setupClassicApp();
	}

	@Test
	void classicParsesEvaluatesAndPersistsTheSharedCommand() {
		evaluateGeoElement("P=(0,0)");
		GeoElementND[] out = getApp().getKernel().getAlgebraProcessor()
				.processAlgebraCommand("s=IsoABorder(P,3,true,1,50,1,true)", false);
		assertNotNull(out);
		assertEquals(3, out.length);
		assertEquals(Commands.IsoABorder, out[0].getParentAlgorithm().getClassName());
		GeoPointND[] corners = ((GeoPolyLine) out[AlgoIsoABorder.PAPER]).getPoints();
		assertEquals(21000, corners[1].getInhomX(), 0);
		assertEquals(-14850, corners[2].getInhomY(), 0);
		String xml = getApp().getXML();
		assertTrue(xml.contains("<command name=\"IsoABorder\">"), xml);
		// The Classic 3D reader normalizes the 3D-visibility flags of every object on
		// load (the free point P too), so the command block and stability are compared.
		getApp().setXML(xml, true);
		String reopened = getApp().getXML();
		assertEquals(commandBlock(xml), commandBlock(reopened), "same command");
		getApp().setXML(reopened, true);
		assertEquals(reopened, getApp().getXML(), "stable from the first reload on");
		assertTrue(getApp().getKernel().lookupLabel("s").getParentAlgorithm()
				instanceof AlgoIsoABorder);
		GeoPointND[] again = ((GeoPolyLine) getApp().getKernel().lookupLabel("s")).getPoints();
		assertEquals(21000, again[1].getInhomX(), 0);
	}

	private static String commandBlock(String xml) {
		int start = xml.indexOf("<command name=\"IsoABorder\">");
		return xml.substring(start, xml.indexOf("</command>", start));
	}
}
