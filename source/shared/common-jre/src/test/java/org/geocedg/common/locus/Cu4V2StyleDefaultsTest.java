/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.geogebra.common.kernel.ConstructionDefaults.DEFAULT_LOCUS;
import static org.geogebra.common.kernel.ConstructionDefaults.DEFAULT_LOCUS_V2;
import static org.geogebra.common.kernel.ConstructionDefaults.DEFAULT_SPLINE_V2;
import static org.geogebra.common.kernel.ConstructionDefaults.LABEL_VISIBLE_ALWAYS_OFF;
import static org.geogebra.common.kernel.ConstructionDefaults.LABEL_VISIBLE_ALWAYS_ON;
import static org.geogebra.common.kernel.ConstructionDefaults.LABEL_VISIBLE_POINTS_ONLY;
import static org.geogebra.common.kernel.ConstructionDefaults.LABEL_VISIBLE_USE_DEFAULTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.jre.io.LegacyStyleDefaultsReader;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.geos.V2StyleDefaults;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.junit.jupiter.api.Test;

class Cu4V2StyleDefaultsTest extends G9U0PublicSurfaceTestBase {
	@Test
	void appliesIndependentCurveDefaultsAndLabelPoliciesOnlyOnCreation() {
		ConstructionDefaults defaults = getConstruction().getConstructionDefaults();
		GeoElement classic = defaults.getDefaultGeo(DEFAULT_LOCUS);
		GeoElement locusPrototype = defaults.getDefaultGeo(DEFAULT_LOCUS_V2);
		GeoElement splinePrototype = defaults.getDefaultGeo(DEFAULT_SPLINE_V2);
		assertNotSame(classic, locusPrototype);
		assertNotSame(locusPrototype, splinePrototype);
		V2StyleDefaults.Style classicStyle = V2StyleDefaults.Style.of(classic);
		V2StyleDefaults.Style expectedFactoryV2 = new V2StyleDefaults.Style(GColor.BLACK,
				classicStyle.thickness(), classicStyle.lineType(), classicStyle.labelVisible(),
				classicStyle.labelMode());
		assertEquals(expectedFactoryV2, V2StyleDefaults.Style.of(locusPrototype));
		assertEquals(expectedFactoryV2, V2StyleDefaults.Style.of(splinePrototype));
		assertTrue(getConstruction().getGeoSetConstructionOrder().isEmpty());
		GeoLocusV2 factoryLocus = createLine();
		assertEquals(GColor.BLACK, factoryLocus.getObjectColor());
		GeoLocusV2 factorySpline = add("S0=SplineV2({(0,0),(1,2),(3,1),(4,0)},3)");
		assertEquals(GColor.BLACK, factorySpline.getObjectColor());
		assertNull(defaults.getV2DefaultsXML());

		locusPrototype.setObjColor(GColor.RED);
		locusPrototype.setLineThickness(9);
		locusPrototype.setLabelVisible(true);
		locusPrototype.setLabelMode(GeoElement.LABEL_NAME_VALUE);
		assertTrue(locusPrototype.isLabelVisible());
		splinePrototype.setObjColor(GColor.BLUE);
		splinePrototype.setLineType(EuclidianStyleConstants.LINE_TYPE_DOTTED);
		splinePrototype.setLabelVisible(false);
		getApp().setLabelingStyle(LABEL_VISIBLE_ALWAYS_OFF);
		GeoLocusV2 locus = add("W=LocusV2(Q,s,D)");
		assertEquals(DEFAULT_LOCUS_V2, defaults.getDefaultType(locus));
		assertEquals(GColor.RED, locus.getObjectColor());
		assertEquals(9, locus.getLineThickness());
		assertFalse(locus.getLabelVisible());
		assertFalse(add("ell=LocusLength(L)").toGeoElement().getLabelVisible());

		getApp().setLabelingStyle(LABEL_VISIBLE_ALWAYS_ON);
		GeoLocusV2 spline = add("S=SplineV2({(0,0),(1,2),(3,1),(4,0)},3)");
		assertEquals(DEFAULT_SPLINE_V2, defaults.getDefaultType(spline));
		assertEquals(GColor.BLUE, spline.getObjectColor());
		assertEquals(EuclidianStyleConstants.LINE_TYPE_DOTTED, spline.getLineType());
		assertTrue(spline.getLabelVisible());
		assertFalse(locus.getLabelVisible());

		getApp().setLabelingStyle(LABEL_VISIBLE_POINTS_ONLY);
		assertFalse(add("T=SplineV2({(0,0),(1,1),(2,1),(3,0)},3)")
				.toGeoElement().getLabelVisible());
		getApp().setLabelingStyle(LABEL_VISIBLE_USE_DEFAULTS);
		assertFalse(add("U=SplineV2({(0,0),(1,2),(2,2),(3,0)},3)")
				.toGeoElement().getLabelVisible());
		GeoLocusV2 usesDefaults = add("V=LocusV2(Q,s,D)");
		assertTrue(usesDefaults.getLabelVisible());
		assertEquals(GeoElement.LABEL_NAME_VALUE, usesDefaults.getLabelMode());
	}

	@Test
	void strictDefaultsCodecRejectsUnknownVersionTypesAndGeometry() throws Exception {
		ConstructionDefaults defaults = getConstruction().getConstructionDefaults();
		defaults.getDefaultGeo(DEFAULT_LOCUS_V2).setObjColor(GColor.RED);
		defaults.getDefaultGeo(DEFAULT_SPLINE_V2).setObjColor(GColor.BLUE);
		String xml = defaults.getV2DefaultsXML();
		assertEquals(V2StyleDefaults.Style.of(defaults.getDefaultGeo(DEFAULT_LOCUS_V2)),
				V2StyleDefaults.read(xml).get(DEFAULT_LOCUS_V2));
		assertEquals(V2StyleDefaults.Style.of(defaults.getDefaultGeo(DEFAULT_SPLINE_V2)),
				V2StyleDefaults.read(xml).get(DEFAULT_SPLINE_V2));
		for (String malformed : List.of(xml.replace("version=\"1\"", "version=\"2\""),
				xml.replace("default=\"82\"", "default=\"81\""),
				xml.replace("default=\"81\"", "default=\"83\""),
				xml.replace("version=\"1\"", "version=\"9\" version=\"1\""),
				xml.replace("</defaults>", "<command name=\"Line\"/></defaults>"),
				xml + "<geogebra/>", " ".repeat(V2StyleDefaults.MAX_BYTES + 1))) {
			assertThrows(XMLParseException.class, () -> V2StyleDefaults.read(malformed));
		}
		defaults.getDefaultGeo(DEFAULT_LOCUS).setObjColor(GColor.GREEN);
		XMLStringBuilder legacy = new XMLStringBuilder();
		defaults.getDefaultsXML(legacy, null);
		defaults.setPersonalDefaults(LegacyStyleDefaultsReader.read(legacy.toString(), defaults),
				xml.replace("version=\"1\"", "version=\"2\""));
		defaults.applyPersonalDefaults();
		assertEquals(GColor.GREEN, defaults.getDefaultGeo(DEFAULT_LOCUS_V2).getObjectColor());
		assertEquals(GColor.GREEN, defaults.getDefaultGeo(DEFAULT_SPLINE_V2).getObjectColor());
		assertTrue(getConstruction().getGeoSetConstructionOrder().isEmpty());
	}
}
