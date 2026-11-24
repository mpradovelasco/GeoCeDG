/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.bspline.BSplineContractException.Code;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A2, BS-21 and the reader part of BS-24 ({@code bspline-2d.md} sections 8.1,
 * 8.3 and 10.5): every defect of the family element fails closed with its typed cause on
 * every parse route and restores the construction that existed before the parse; the
 * inherited command-error collection never absorbs it. Commands that are not the native
 * {@code BSpline} keep their previous behaviour.
 */
public class MaintA2BSplineContractCodecTest extends BaseUnitTest {
	private static final String SPLINE_HEAD = "<command name=\"SplineV2\">";
	private static final String V1 = "<geocedgBSpline contract=\"bspline-2d/v1\""
			+ " knots=\"clamped-uniform/v1\"";

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private String template() {
		for (String definition : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2)", "D=(4,0)",
				"S=SplineV2({A,B,C,D},3)"}) {
			assertNotNull(add(definition), definition);
		}
		String xml = getApp().getXML();
		assertTrue(xml.contains(SPLINE_HEAD));
		return xml;
	}

	private static String bspline(String xml, String familyElement) {
		return xml.replace(SPLINE_HEAD, "<command name=\"BSpline\">\n" + familyElement);
	}

	private void assertRejected(String name, String xml, Code expected) {
		String before = getApp().getXML();
		// The document, Undo and rollback route; a merge of an identity-bearing document is not
		// a route (the clipboard test covers fragments).
		for (boolean clear : new boolean[] {true}) {
			try {
				getApp().getXMLio().processXMLString(xml, clear, false);
				fail(name + ": accepted (clear=" + clear + ")");
			} catch (Throwable failure) {
				BSplineContractException rejection = BSplineContractException.find(failure);
				assertNotNull(rejection, name + ": " + failure);
				assertEquals(expected, rejection.getCode(), name);
			}
			assertEquals(before, getApp().getXML(), name + ": construction not restored");
		}
	}

	@Test
	void unsupportedContractVersionIsNeverReadAsV1() {
		String xml = template();
		assertRejected("v2", bspline(xml, "<geocedgBSpline contract=\"bspline-2d/v2\""
				+ " knots=\"clamped-uniform/v1\"/>"), Code.UNSUPPORTED_CONTRACT_VERSION);
		assertRejected("v2 with a new attribute", bspline(xml,
				"<geocedgBSpline contract=\"bspline-2d/v2\" knots=\"explicit\""
						+ " weights=\"w\"/>"), Code.UNSUPPORTED_CONTRACT_VERSION);
	}

	@Test
	void missingFamilyElementFailsClosed() {
		String xml = template();
		assertRejected("missing", xml.replace(SPLINE_HEAD, "<command name=\"BSpline\">"),
				Code.CONTRACT_MISSING);
	}

	@Test
	void malformedFamilyElementsFailClosed() {
		String xml = template();
		Map<String, String> cases = Map.ofEntries(
				Map.entry("unknown attribute", V1 + " extra=\"1\"/>"),
				Map.entry("missing contract", "<geocedgBSpline knots=\"explicit\"/>"),
				Map.entry("unknown knot rule", "<geocedgBSpline contract=\"bspline-2d/v1\""
						+ " knots=\"periodic/v1\"/>"),
				Map.entry("missing knot rule", "<geocedgBSpline contract=\"bspline-2d/v1\"/>"),
				Map.entry("knot rule against arity", "<geocedgBSpline contract=\"bspline-2d/v1\""
						+ " knots=\"explicit\"/>"),
				Map.entry("unknown child", V1 + "><extra/></geocedgBSpline>"),
				Map.entry("text", V1 + ">text</geocedgBSpline>"),
				Map.entry("duplicate element", V1 + "/>" + V1 + "/>"),
				Map.entry("duplicate attribute", "<geocedgBSpline contract=\"bspline-2d/v1\""
						+ " contract=\"bspline-2d/v1\" knots=\"clamped-uniform/v1\"/>"),
				Map.entry("provenance without version", V1
						+ "><approximationProvenance method=\"m\"/></geocedgBSpline>"),
				Map.entry("provenance child", V1 + "><approximationProvenance version=\"1\">"
						+ "<x/></approximationProvenance></geocedgBSpline>"),
				Map.entry("two provenance blocks", V1 + "><approximationProvenance version=\"1\"/>"
						+ "<approximationProvenance version=\"1\"/></geocedgBSpline>"));
		for (Map.Entry<String, String> entry : cases.entrySet()) {
			assertRejected(entry.getKey(), bspline(xml, entry.getValue()),
					Code.MALFORMED_CONTRACT);
		}
		// An element written after <input> is never reached: <input> already lacks it.
		assertRejected("element after input", xml.replace(SPLINE_HEAD,
				"<command name=\"BSpline\">").replace("<output a0=\"S\"/>",
						V1 + "/>\n<output a0=\"S\"/>"), Code.CONTRACT_MISSING);
	}

	@Test
	void unsupportedProvenanceVersionFailsClosed() {
		String xml = template();
		assertRejected("provenance v2", bspline(xml, V1
				+ "><approximationProvenance version=\"2\" method=\"m\"/></geocedgBSpline>"),
				Code.UNSUPPORTED_PROVENANCE_VERSION);
	}

	@Test
	void macroAndGgtRoutesDoNotSwallowTheRejection() {
		String xml = bspline(template(), "<geocedgBSpline contract=\"bspline-2d/v9\""
				+ " knots=\"clamped-uniform/v1\"/>");
		String before = getApp().getXML();
		try {
			getApp().getXMLio().processXMLString(xml, true, true);
			fail("GGT route accepted");
		} catch (Throwable failure) {
			assertNotNull(BSplineContractException.find(failure), failure.toString());
		}
		assertEquals(before, getApp().getXML());
	}

	@Test
	void clipboardImportRollsBack() {
		String xml = template();
		int start = xml.indexOf(SPLINE_HEAD);
		int end = xml.indexOf("</element>", xml.indexOf("label=\"S\"", start)) + 10;
		String fragment = bspline(xml.substring(start, end).replace("a0=\"S\"", "a0=\"S2\"")
				.replace("label=\"S\"", "label=\"S2\"").replaceAll(" geocedgId=\"[^\"]*\"", ""),
				"<geocedgBSpline"
						+ " contract=\"bspline-2d/v3\" knots=\"clamped-uniform/v1\"/>");
		String before = getApp().getXML();
		assertFalse(InternalClipboard.evalClipboardXMLAtomically(getApp(), fragment));
		assertEquals(before, getApp().getXML());
	}

	@Test
	void otherCommandsKeepThePreviousLeniency() {
		String xml = template();
		String unknownChild = xml.replace(SPLINE_HEAD, SPLINE_HEAD + "\n" + V1 + "/>");
		try {
			getApp().getXMLio().processXMLString(unknownChild, true, false);
		} catch (Exception failure) {
			fail("SplineV2 with an unknown child must load as before: " + failure);
		}
		assertTrue(((GeoLocusV2) lookup("S")).isDefined());
	}

	@Test
	void aMacroNamedBSplineKeepsItsDocumentsLoading() throws Exception {
		GeoElement first = add("A=(0,0)");
		GeoElement second = add("B=(2,2)");
		GeoElement middle = add("M=Midpoint(A,B)");
		Macro macro = new Macro(getKernel(), "BSpline", new GeoElement[] {first, second},
				new GeoElement[] {middle});
		getKernel().addMacro(macro);
		String document = "<geogebra format=\"5.0\"><construction>"
				+ "<command name=\"BSpline\"><input a0=\"A\" a1=\"B\"/>"
				+ "<output a0=\"Q\"/></command></construction></geogebra>";
		try {
			getApp().getXMLio().processXMLString(document, false, false);
		} catch (Exception failure) {
			fail("a document macro named BSpline must keep resolving: " + failure);
		}
		assertNotNull(lookup("Q"));
	}

	@Test
	void writerEmitsCanonicalElementAndPreservesProvenanceInOrder() {
		XMLStringBuilder plain = new XMLStringBuilder();
		BSplineContractXml.write(plain, BSplineContractXml.PersistedContract
				.of(BSplineContractXml.KNOTS_EXPLICIT));
		assertEquals("\t<geocedgBSpline contract=\"bspline-2d/v1\" knots=\"explicit\"/>\n",
				plain.toString());
		BSplineContractXml.CommandReader reader =
				BSplineContractXml.CommandReader.begin("BSpline", false);
		reader.startFamilyElement(new java.util.LinkedHashMap<>(Map.of(
				"contract", "bspline-2d/v1", "knots", "clamped-uniform/v1")));
		java.util.LinkedHashMap<String, String> provenance = new java.util.LinkedHashMap<>();
		provenance.put("version", "1");
		provenance.put("source", "a&b<\"c\">");
		provenance.put("method", "m");
		reader.startNested("approximationProvenance", provenance);
		assertFalse(reader.endNested("approximationProvenance"));
		assertTrue(reader.endNested("geocedgBSpline"));
		reader.beforeCommandChild("input", 0);
		reader.beforeCommandChild("output", 2);
		BSplineContractXml.PersistedContract contract = reader.getContract();
		List<Map.Entry<String, String>> attributes =
				contract.getApproximationProvenanceAttributes();
		assertEquals(List.of("version", "source", "method"),
				attributes.stream().map(Map.Entry::getKey).toList());
		XMLStringBuilder written = new XMLStringBuilder();
		BSplineContractXml.write(written, contract);
		assertEquals("\t<geocedgBSpline contract=\"bspline-2d/v1\""
				+ " knots=\"clamped-uniform/v1\">\n"
				+ "\t\t<approximationProvenance version=\"1\" source=\"a&amp;b&lt;&quot;c&quot;&gt;\""
				+ " method=\"m\"/>\n</geocedgBSpline>\n", written.toString());
	}
}
