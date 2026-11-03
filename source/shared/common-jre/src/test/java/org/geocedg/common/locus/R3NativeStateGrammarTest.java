/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.io.DocHandler;
import org.geogebra.common.io.QDParser;
import org.junit.jupiter.api.Test;

class R3NativeStateGrammarTest extends G9U0PublicSurfaceTestBase {

	private String document() {
		add("c=Circle((.4,-.7),2)"); add("P=Point(c)");
		return getApp().getXML();
	}

	private static String tag(String xml) {
		var matcher = Pattern.compile("<conicPointState\\b[^>]*/>").matcher(xml);
		assertTrue(matcher.find(), "The ordinary supported native source must keep its exact field");
		return matcher.group();
	}

	@Test void duplicateAttributesAreRejectedBeforeTheirMapCanOverwriteThem() {
		String xml = document(), state = tag(xml);
		for (String field : new String[] {"version", "t", "pathType", "x", "y", "z",
				"tempT", "tempPathType", "animation"}) {
			String duplicate = state.replaceFirst(field + "=\"", field + "=\"99\" " + field + "=\"");
			assertThrows(Exception.class, () -> getApp().getXMLio().processXMLString(
					xml.replace(state, duplicate), true, false), field);
		}
	}

	@Test void nestedTextAndPairedNativeStateLeavesAreRejectedLexically() {
		String xml = document(), state = tag(xml);
		for (String payload : new String[] {"", "unexpected", "<unexpected/>", "<![CDATA[text]]>"}) {
			String malformed = state.substring(0, state.length() - 2) + ">" + payload
					+ "</conicPointState>";
			assertThrows(Exception.class, () -> getApp().getXMLio().processXMLString(
					xml.replace(state, malformed), true, false));
		}
	}

	@Test void classicReaderUsesTheSameStrictNewFieldGrammar() {
		String xml = document(), state = tag(xml);
		var classic = AppCommonFactory.create();
		assertDoesNotThrow(() -> classic.getXMLio().processXMLString(xml, true, false));
		String forged = state.replace("version=\"1\"", "version=\"99\" version=\"1\"");
		assertThrows(Exception.class, () -> classic.getXMLio().processXMLString(
				xml.replace(state, forged), true, false));
	}

	@Test void unrelatedDocHandlersKeepTheirHistoricalGrammarIncludingTheSameTagName()
			throws Exception {
		Map<String, String> seen = new LinkedHashMap<>();
		DocHandler legacy = new DocHandler() {
			@Override public void startElement(String name, Map<String, String> attrs) {
				if ("conicPointState".equals(name)) { seen.putAll(attrs); }
			}
			@Override public void endElement(String name) { }
			@Override public void startDocument() { }
			@Override public void endDocument() { }
			@Override public void text(String text) { }
		};
		new QDParser().parse(legacy, new StringReader(
				"<root><conicPointState version=\"99\" version=\"1\"><child/></conicPointState></root>"));
		assertEquals("1", seen.get("version"));
	}
}
