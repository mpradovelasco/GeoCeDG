/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.dimension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geocedg.common.kernel.units.DocumentUnitSystem;
import org.geocedg.common.kernel.units.UnitDocumentOperations;
import org.geocedg.common.kernel.units.UnitMetadataException;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.error.ErrorHelper;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-E2 author-smoke follow-up B1 and B2 in the shared kernel: the initial
 * line thickness of the three dimension segments, and the dimension unit suffix policy
 * of unit-system v1.1 (section 14.2) with its versioned {@code geocedgUnits} element.
 */
class PreG9BR6PlusE2PresentationFollowUpTest extends BaseUnitTest {

	private static final UsmDefinition INCH = UsmDefinition.of(0.0254, "inch", "in");
	private static final UsmDefinition UNNAMED = UsmDefinition.of(0.0254, null, null);

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private DocumentUnitSystem units() {
		return getConstruction().getUnitSystem();
	}

	private GeoElement[] dim(String command) {
		GeoElementND[] result = getAlgebraProcessor().processAlgebraCommandNoExceptionHandling(
				command, false, ErrorHelper.silent(), false, null);
		assertNotNull(result, command);
		return Arrays.stream(result).map(GeoElementND::toGeoElement).toArray(GeoElement[]::new);
	}

	private static List<GeoSegment> segments(GeoElement[] out) {
		return List.of((GeoSegment) out[AlgoNativeDimension.DIMENSION_LINE],
				(GeoSegment) out[AlgoNativeDimension.EXTENSION_A],
				(GeoSegment) out[AlgoNativeDimension.EXTENSION_B]);
	}

	private static String text(GeoElement[] out) {
		return ((GeoText) out[AlgoNativeDimension.TEXT]).getTextString();
	}

	private String unitsXml() {
		StringBuilder sb = new StringBuilder();
		getConstruction().getConstructionXML(new XMLStringBuilder(sb), false);
		int start = sb.indexOf("<geocedgUnits");
		return start < 0 ? "" : sb.substring(start, sb.indexOf("/>", start) + 2);
	}

	private static String withConstructionChild(String documentXml, String child) {
		int start = documentXml.indexOf("<construction");
		int end = documentXml.indexOf('>', start) + 1;
		return documentXml.substring(0, end) + "\n" + child + documentXml.substring(end);
	}

	private void load(String xml) throws Exception {
		getApp().getXMLio().processXMLString(xml, true, false);
	}

	// ------------------------------------------------------------------- B1

	@Test
	void bothCommandsStartTheirThreeSegmentsAtThicknessTwo() {
		add("A=(0,0)");
		add("B=(4,3)");
		for (GeoElement[] out : List.of(dim("AlignedDimension(A,B,1)"),
				dim("LinearDimension(A,B,xAxis,1)"))) {
			for (GeoSegment segment : segments(out)) {
				assertEquals(AlgoNativeDimension.INITIAL_LINE_THICKNESS,
						segment.getLineThickness());
			}
		}
		assertEquals(2, AlgoNativeDimension.INITIAL_LINE_THICKNESS);
	}

	@Test
	void segmentStylesAreIndividualSerializedAndNeverGeometry() throws Exception {
		add("A=(0,0)");
		add("B=(4,3)");
		GeoElement[] out = dim("d=AlignedDimension(A,B,1)");
		double value = ((GeoNumeric) out[0]).getDouble();
		double[] before = coords(out);
		GeoSegment line = segments(out).get(0);
		line.setLineThickness(7);
		line.updateVisualStyleRepaint(org.geogebra.common.kernel.geos.GProperty.LINE_STYLE);
		assertEquals(value, ((GeoNumeric) out[0]).getDouble(), 0);
		assertTrue(Arrays.equals(before, coords(out)), "a style change moves nothing");
		assertEquals(2, segments(out).get(1).getLineThickness(), "independent segment styles");
		String xml = getApp().getXML();
		load(xml);
		AlgoNativeDimension reopened = AlgoNativeDimension.ownerOf(lookup("d"));
		assertEquals(7, reopened.getDimensionLine().getLineThickness());
		assertEquals(2, reopened.getExtensionA().getLineThickness());
		assertEquals(2, reopened.getExtensionB().getLineThickness());
		assertEquals(xml, getApp().getXML(), "byte-identical re-save");
	}

	private static double[] coords(GeoElement[] out) {
		double[] c = new double[12];
		int i = 0;
		for (GeoSegment s : segments(out)) {
			c[i++] = s.getStartPoint().getInhomX();
			c[i++] = s.getStartPoint().getInhomY();
			c[i++] = s.getEndPoint().getInhomX();
			c[i++] = s.getEndPoint().getInhomY();
		}
		return c;
	}

	// ------------------------------------------------------------- B2 text

	@Test
	void suffixPolicyChangesOnlyTheTextAndNeverTheValueOrGeometry() {
		add("A=(0,0)");
		add("B=(12,0)");
		GeoElement[] out = dim("AlignedDimension(A,B,1)");
		double[] geometry = coords(out);
		Object[][] cases = {
			{UnitState.of(UnitToken.CM, UnitToken.MM, null, false), "120"},
			{UnitState.of(UnitToken.CM, UnitToken.MM, null, true), "120 mm"},
			{UnitState.of(UnitToken.CM, UnitToken.M, null, false), "0.12"},
			{UnitState.of(UnitToken.CM, UnitToken.M, null, true), "0.12 m"},
			{UnitState.of(UnitToken.USM, null, INCH, false), "12"},
			{UnitState.of(UnitToken.USM, null, INCH, true), "12 in"},
			{UnitState.of(UnitToken.USM, null, UNNAMED, true), "12 usm"},
			{UnitState.of(null, null, null, false), "12"},
			{UnitState.EMPTY, "12"}
		};
		for (Object[] c : cases) {
			units().replace((UnitState) c[0]);
			assertEquals(c[1], text(out), String.valueOf(c[0]));
			assertEquals(12, ((GeoNumeric) out[0]).getDouble(), 0, "model-unit value");
			assertTrue(Arrays.equals(geometry, coords(out)), "geometry unchanged");
		}
	}

	@Test
	void historicalStatesWithoutPolicyShowTheSuffix() {
		UnitState legacy = UnitState.of(UnitToken.CM, UnitToken.MM, null);
		assertTrue(legacy.isDimensionUnitSuffixShown());
		assertTrue(UnitState.EMPTY.isDimensionUnitSuffixShown());
		add("A=(0,0)");
		add("B=(12,0)");
		units().replace(legacy);
		assertEquals("120 mm", text(dim("AlignedDimension(A,B,1)")));
	}

	@Test
	void everyUnitOperationKeepsThePolicyAndThePolicyKeepsTheUnits() {
		UnitState hidden = UnitState.of(UnitToken.CM, UnitToken.MM, INCH, false);
		assertFalse(hidden.withConstructionUnit(UnitToken.M).isDimensionUnitSuffixShown());
		assertFalse(hidden.withConstructionUnit(null).isDimensionUnitSuffixShown());
		assertFalse(hidden.withPresentationUnit(null).isDimensionUnitSuffixShown());
		assertFalse(hidden.withUsm(UNNAMED).isDimensionUnitSuffixShown());
		assertFalse(hidden.withoutUsm().isDimensionUnitSuffixShown());
		UnitState shown = hidden.withDimensionUnitSuffixShown(true);
		assertEquals(UnitState.of(UnitToken.CM, UnitToken.MM, INCH), shown);
		assertEquals(UnitToken.MM, hidden.effectivePresentationUnit());
		assertEquals(hidden.display(12).getValue(), shown.display(12).getValue(), 0);
		assertFalse(UnitState.of(null, null, null, false).isEmpty(), "hidden is metadata");
		assertTrue(UnitState.of(null, null, null, true).isEmpty());
	}

	@Test
	void notFiniteDisplayKeepsTheFailureMarkerWithOrWithoutTheSuffix() {
		add("A=(0,0)");
		add("B=(1E300,0)");
		GeoElement[] out = dim("AlignedDimension(A,B,1)");
		UsmDefinition tiny = UsmDefinition.of(1E-300, "tiny", "t");
		units().replace(UnitState.of(UnitToken.M, UnitToken.USM, tiny, true));
		assertEquals("? t", text(out));
		units().replace(UnitState.of(UnitToken.M, UnitToken.USM, tiny, false));
		assertEquals("?", text(out));
	}

	@Test
	void aPolicyChangeIsOneUndoStepAndRedoable() {
		getApp().setUndoActive(true);
		add("A=(0,0)");
		add("B=(12,0)");
		GeoElement[] out = dim("d=AlignedDimension(A,B,1)");
		units().replace(UnitState.of(UnitToken.CM, UnitToken.MM, null, true));
		getApp().storeUndoInfo();
		assertTrue(UnitDocumentOperations.commit(getApp(),
				units().getState().withDimensionUnitSuffixShown(false)));
		assertEquals("120", text(out));
		getApp().getKernel().undo();
		assertTrue(units().getState().isDimensionUnitSuffixShown());
		assertEquals("120 mm", ((GeoText) AlgoNativeDimension.ownerOf(lookup("d"))
				.getPresentationText()).getTextString());
		getApp().getKernel().redo();
		assertFalse(units().getState().isDimensionUnitSuffixShown());
		assertEquals("120", ((GeoText) AlgoNativeDimension.ownerOf(lookup("d"))
				.getPresentationText()).getTextString());
	}

	// ------------------------------------------------------------ B2 XML

	@Test
	void writerEmitsVersionTwoOnlyForTheHiddenPolicy() {
		units().replace(UnitState.of(UnitToken.CM, UnitToken.MM, null, false));
		assertEquals("<geocedgUnits version=\"2\" construction=\"cm\" presentation=\"mm\""
				+ " dimensionUnitSuffix=\"hidden\"/>", unitsXml());
		units().replace(UnitState.of(null, null, null, false));
		assertEquals("<geocedgUnits version=\"2\" dimensionUnitSuffix=\"hidden\"/>",
				unitsXml());
		units().replace(UnitState.of(UnitToken.USM, null, INCH, false));
		assertEquals("<geocedgUnits version=\"2\" construction=\"usm\""
				+ " usmMetersPerUnit=\"0.0254\" usmName=\"inch\" usmSymbol=\"in\""
				+ " dimensionUnitSuffix=\"hidden\"/>", unitsXml());
		units().replace(UnitState.of(UnitToken.CM, UnitToken.MM, null, true));
		assertEquals("<geocedgUnits version=\"1\" construction=\"cm\" presentation=\"mm\"/>",
				unitsXml(), "the shown policy writes the version-1 element unchanged");
		units().replace(UnitState.EMPTY);
		assertEquals("", unitsXml());
	}

	@Test
	void readerAcceptsVersionTwoAndRoundTripsByteIdentically() throws Exception {
		add("A=(1,2)");
		String base = getApp().getXML().replaceAll("\\s*<geocedgUnits[^>]*/>", "");
		load(withConstructionChild(base, "<geocedgUnits version=\"2\" construction=\"cm\""
				+ " presentation=\"mm\" dimensionUnitSuffix=\"hidden\"/>"));
		assertEquals(UnitState.of(UnitToken.CM, UnitToken.MM, null, false), units().getState());
		String saved = getApp().getXML();
		load(saved);
		assertEquals(saved, getApp().getXML());
		load(withConstructionChild(base, "<geocedgUnits version=\"2\""
				+ " dimensionUnitSuffix=\"hidden\"/>"));
		assertEquals(UnitState.of(null, null, null, false), units().getState());
		load(withConstructionChild(base, "<geocedgUnits version=\"2\" construction=\"mm\""
				+ " dimensionUnitSuffix=\"shown\"/>"));
		assertEquals(UnitState.of(UnitToken.MM, null, null), units().getState());
		assertEquals("<geocedgUnits version=\"1\" construction=\"mm\"/>", unitsXml(),
				"the canonical form of the shown policy is the version-1 element");
	}

	@Test
	void versionOneDocumentsLoadWithTheHistoricalSuffixAndKeepTheirBytes() throws Exception {
		add("A=(0,0)");
		add("B=(12,0)");
		dim("d=AlignedDimension(A,B,1)");
		units().replace(UnitState.of(UnitToken.CM, UnitToken.MM, null));
		String versionOne = getApp().getXML();
		assertTrue(versionOne.contains(
				"<geocedgUnits version=\"1\" construction=\"cm\" presentation=\"mm\"/>"));
		load(versionOne);
		assertTrue(units().getState().isDimensionUnitSuffixShown());
		assertEquals("120 mm", ((GeoText) AlgoNativeDimension.ownerOf(lookup("d"))
				.getPresentationText()).getTextString());
		assertEquals(versionOne, getApp().getXML(), "no silent migration");
	}

	@Test
	void malformedVersionTwoElementsFailClosed() throws Exception {
		add("A=(1,2)");
		units().replace(UnitState.of(UnitToken.CM, null, null));
		String original = getApp().getXML();
		String base = original.replaceAll("\\s*<geocedgUnits[^>]*/>", "");
		Object[][] cases = {
			{"<geocedgUnits version=\"2\" construction=\"mm\"/>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"2\" dimensionUnitSuffix=\"off\"/>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"2\" dimensionUnitSuffix=\"HIDDEN\"/>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"1\" construction=\"mm\" dimensionUnitSuffix=\"hidden\"/>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"3\" dimensionUnitSuffix=\"hidden\"/>",
				UnitMetadataException.Code.UNSUPPORTED_VERSION}
		};
		for (Object[] c : cases) {
			String xml = withConstructionChild(base, (String) c[0]);
			UnitMetadataException failure = assertThrows(UnitMetadataException.class,
					() -> load(xml), (String) c[0]);
			assertEquals(c[1], failure.getCode(), (String) c[0]);
			assertEquals(original, getApp().getXML(), "entry snapshot restored: " + c[0]);
		}
	}

	@Test
	void thePolicyAloneIsPresentationAndNeverSaveRelevant() {
		units().replace(UnitState.of(null, null, null, false));
		assertFalse(units().getState().isEmpty(), "it is written with the document");
		assertFalse(units().hasPersistentMetadata(), "DQ-D1-5: presentation never counts");
		units().replace(UnitState.of(UnitToken.MM, null, null, false));
		assertTrue(units().hasPersistentMetadata(), "a unit selection still counts");
	}
}
