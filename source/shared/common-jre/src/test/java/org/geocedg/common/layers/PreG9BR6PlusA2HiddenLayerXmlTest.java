/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.layers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.geocedg.common.kernel.layers.HiddenLayerMetadataException;
import org.geocedg.common.kernel.layers.HiddenLayerMetadataException.Code;
import org.geocedg.common.kernel.layers.HiddenLayerSet;
import org.geocedg.common.kernel.layers.HiddenLayersXml;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.factories.AwtFactoryCommon;
import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.UndoRedoMode;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.test.LocalizationCommonUTF;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-A-2 T-XML-WRITER, T-XML-READER, T-FAIL-CLOSED, T-IGNORED (shared),
 * T-PERSIST-NO-UNDO (shared), T-UNITS-INDEPENDENCE (shared) and T-CLASSIC (shared):
 * the document-level hidden-layer element on the real shared writer and parser.
 */
class PreG9BR6PlusA2HiddenLayerXmlTest {

	/** A product-like host that owns a hidden set and records the parse reports. */
	private static final class HiddenLayerApp extends AppCommon {
		// No field initializers: the host constructor already parses XML.
		private HiddenLayerSet document;
		private List<HiddenLayerSet> reports;

		HiddenLayerApp() {
			super(new LocalizationCommonUTF(2), new AwtFactoryCommon(),
					new AppConfigGeoCeDG(true));
		}

		@Override
		public HiddenLayerSet getDocumentHiddenLayers() {
			return document == null ? HiddenLayerSet.EMPTY : document;
		}

		@Override
		public void documentHiddenLayersParsed(HiddenLayerSet hiddenLayers) {
			reports().add(hiddenLayers);
		}

		List<HiddenLayerSet> reports() {
			if (reports == null) {
				reports = new ArrayList<>();
			}
			return reports;
		}
	}

	private static HiddenLayerApp app() {
		HiddenLayerApp app = new HiddenLayerApp();
		app.reports().clear();
		return app;
	}

	private static GeoElement add(AppCommon app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(result, command);
		return result[0].toGeoElement();
	}

	private static String element(String layers) {
		return "<geocedgHiddenLayers version=\"1\" layers=\"" + layers + "\"/>";
	}

	private static String withTopLevel(String documentXml, String child) {
		return documentXml.replace("</geogebra>", child + "\n</geogebra>");
	}

	private static String allLayers() {
		return IntStream.rangeClosed(0, 99).mapToObj(Integer::toString)
				.collect(Collectors.joining(" "));
	}

	// -------------------------------------------------------------- T-XML-WRITER

	@Test
	void theWriterEmitsOneCanonicalLineAfterTheConstructionOnlyWhenNonEmpty() {
		HiddenLayerApp app = app();
		add(app, "A=(1,2)");
		assertFalse(app.getXML().contains(HiddenLayersXml.ELEMENT), "nothing for EMPTY");
		app.document = HiddenLayerSet.of(50, 3, 7, 3);
		String xml = app.getXML();
		assertTrue(xml.contains("\n" + element("3 7 50") + "\n</geogebra>"), xml);
		int construction = xml.indexOf("</construction>");
		int hidden = xml.indexOf("<" + HiddenLayersXml.ELEMENT);
		assertTrue(construction >= 0 && construction < hidden, "after </construction>");
		assertEquals(1, xml.split("<" + HiddenLayersXml.ELEMENT, -1).length - 1);
		Locale previous = Locale.getDefault();
		try {
			for (String tag : new String[] {"ar-EG", "tr-TR", "de-DE", "hi-IN"}) {
				Locale.setDefault(Locale.forLanguageTag(tag));
				assertEquals(xml, app.getXML(), "locale-independent bytes: " + tag);
			}
		} finally {
			Locale.setDefault(previous);
		}
	}

	@Test
	void undoPreferencesMacroAndClipboardXmlNeverCarryTheElement() {
		HiddenLayerApp app = app();
		final GeoElement a = add(app, "A=(1,2)");
		app.document = HiddenLayerSet.of(4);
		assertTrue(app.getXML().contains(element("4")));
		assertFalse(MyXMLio.getUndoXML(app.getKernel().getConstruction(), false).toString()
				.contains(HiddenLayersXml.ELEMENT), "never in undo XML");
		assertFalse(app.getKernel().getConstruction().getCurrentUndoXML(true).toString()
				.contains(HiddenLayersXml.ELEMENT));
		assertFalse(app.getXMLio().getPreferencesXML().contains(HiddenLayersXml.ELEMENT));
		assertFalse(app.getXMLio().getFullMacroXML(new ArrayList<>())
				.contains(HiddenLayersXml.ELEMENT));
		app.reports().clear();
		InternalClipboard.duplicate(app, List.of(a));
		assertEquals(2, app.getKernel().getConstruction().getGeoSetConstructionOrder().size(),
				"the paste happened");
		assertTrue(app.reports().isEmpty(), "a paste never reports a document set");
		assertEquals(HiddenLayerSet.of(4), app.getDocumentHiddenLayers());
	}

	// -------------------------------------------------------------- T-XML-READER

	@Test
	void aDocumentParseReportsItsSetOnlyAfterTheParseCompleted() throws Exception {
		HiddenLayerApp app = app();
		add(app, "A=(1,2)");
		app.document = HiddenLayerSet.of(0, 42, 99);
		String xml = app.getXML();
		app.document = HiddenLayerSet.EMPTY;
		app.getXMLio().processXMLString(xml, true, false);
		assertEquals(List.of(HiddenLayerSet.of(0, 42, 99)), app.reports());
		assertTrue(app.getDocumentHiddenLayers().isEmpty(), "the parser never applies it");

		app.reports().clear();
		app.getXMLio().processXMLString(app.getXML(), true, false);
		assertEquals(List.of(HiddenLayerSet.EMPTY), app.reports(), "absent: the empty set");

		app.reports().clear();
		app.getXMLio().processXMLString(withTopLevel(app.getXML(), element("5")), false, false);
		assertTrue(app.reports().isEmpty(), "a merge never reads it");
	}

	@Test
	void anUnrecognizingConfigurationIgnoresAndDropsTheElement() throws Exception {
		AppCommon classic = AppCommonFactory.create();
		add(classic, "A=(1,2)");
		String xml = withTopLevel(classic.getXML(), element("3 7"));
		classic.getXMLio().processXMLString(xml, true, false);
		assertEquals(1, classic.getKernel().getConstruction().getGeoSetConstructionOrder()
				.size());
		assertFalse(classic.getXML().contains(HiddenLayersXml.ELEMENT), "dropped on re-save");
		String malformed = withTopLevel(classic.getXML(), "<geocedgHiddenLayers version=\"9\""
				+ " layers=\"x\"/>");
		classic.getXMLio().processXMLString(malformed, true, false);
		assertFalse(classic.getXML().contains(HiddenLayersXml.ELEMENT),
				"Classic is an older, non-owning reader (DQ-A2-5)");
	}

	// ------------------------------------------------------------- T-FAIL-CLOSED

	@Test
	void everyRecognizedDefectFailsClosedAndRestoresTheEntryDocument() throws Exception {
		Object[][] defects = {
			{"<geocedgHiddenLayers version=\"2\" layers=\"3\"/>", Code.UNSUPPORTED_VERSION},
			{"<geocedgHiddenLayers layers=\"3\"/>", Code.MALFORMED_ELEMENT},
			{"<geocedgHiddenLayers version=\"one\" layers=\"3\"/>", Code.MALFORMED_ELEMENT},
			{"<geocedgHiddenLayers version=\"0\" layers=\"3\"/>", Code.MALFORMED_ELEMENT},
			{"<geocedgHiddenLayers version=\"1\" layers=\"3\" view=\"1\"/>",
					Code.MALFORMED_ELEMENT},
			{"<geocedgHiddenLayers version=\"1\"/>", Code.MALFORMED_ELEMENT},
			{element(""), Code.MALFORMED_ELEMENT},
			{element("3,7"), Code.MALFORMED_ELEMENT},
			{element("07"), Code.MALFORMED_ELEMENT},
			{element("+3"), Code.MALFORMED_ELEMENT},
			{element("-3"), Code.MALFORMED_ELEMENT},
			{element(" 3"), Code.MALFORMED_ELEMENT},
			{element("3 "), Code.MALFORMED_ELEMENT},
			{element("3  7"), Code.MALFORMED_ELEMENT},
			{element("7 3"), Code.MALFORMED_ELEMENT},
			{element("3 3"), Code.MALFORMED_ELEMENT},
			{element("100"), Code.OUT_OF_DOMAIN_LAYER},
			{element("3 99999999999"), Code.OUT_OF_DOMAIN_LAYER},
			{element(allLayers()), Code.ALL_LAYERS_HIDDEN},
			{element("3") + element("4"), Code.DUPLICATE_ELEMENT},
			{"<geocedgHiddenLayers version=\"1\" layers=\"3\"><x/></geocedgHiddenLayers>",
					Code.MALFORMED_ELEMENT},
			{"<geocedgHiddenLayers version=\"1\" layers=\"3\">text</geocedgHiddenLayers>",
					Code.MALFORMED_ELEMENT}
		};
		for (Object[] defect : defects) {
			HiddenLayerApp app = app();
			add(app, "A=(1,2)");
			app.document = HiddenLayerSet.of(5);
			String entry = app.getXML();
			String rejected = withTopLevel(entry.replace(element("5") + "\n", ""),
					(String) defect[0]);
			HiddenLayerMetadataException rejection = assertThrows(
					HiddenLayerMetadataException.class, () -> app.getXMLio()
							.processXMLString(rejected, true, false),
					(String) defect[0]);
			assertEquals(defect[1], rejection.getCode(), (String) defect[0]);
			assertEquals(entry, app.getXML(), "the entry document is restored");
			assertTrue(app.reports().isEmpty(),
					"neither the rejected parse nor its restore reports a set");
		}
	}

	@Test
	void anElementAnywhereButUnderTheRootIsMisplaced() throws Exception {
		for (String parent : new String[] {"<construction", "<euclidianView", "<gui"}) {
			HiddenLayerApp app = app();
			add(app, "A=(1,2)");
			String entry = app.getXML();
			int start = entry.indexOf(parent);
			int end = entry.indexOf('>', start) + 1;
			String xml = entry.substring(0, end) + element("3") + entry.substring(end);
			HiddenLayerMetadataException rejection = assertThrows(
					HiddenLayerMetadataException.class,
					() -> app.getXMLio().processXMLString(xml, true, false), parent);
			assertEquals(Code.MISPLACED_ELEMENT, rejection.getCode(), parent);
			assertEquals(entry, app.getXML());
		}
	}

	@Test
	void thePureReaderValidatesAgainstTheReadersDomain() {
		assertEquals(HiddenLayerSet.of(3, 9),
				HiddenLayersXml.read(java.util.Map.of("version", "1", "layers", "3 9"), 9));
		HiddenLayerMetadataException classic = assertThrows(HiddenLayerMetadataException.class,
				() -> HiddenLayersXml.read(java.util.Map.of("version", "1", "layers", "42"), 9));
		assertEquals(Code.OUT_OF_DOMAIN_LAYER, classic.getCode());
		HiddenLayerMetadataException all = assertThrows(HiddenLayerMetadataException.class,
				() -> HiddenLayersXml.read(java.util.Map.of("version", "1",
						"layers", "0 1 2 3 4 5 6 7 8 9"), 9));
		assertEquals(Code.ALL_LAYERS_HIDDEN, all.getCode());
		assertTrue(HiddenLayerSet.of(IntStream.rangeClosed(0, 99).toArray()).coversDomain(99));
		assertFalse(HiddenLayerSet.of(IntStream.rangeClosed(0, 98).toArray()).coversDomain(99));
	}

	// ----------------------------------------------------------------- T-IGNORED

	@Test
	void mergeMacroAndPasteContextsIgnoreTheElement() throws Exception {
		HiddenLayerApp app = app();
		add(app, "A=(1,2)");
		app.getGgbApi().evalXML("<element type=\"point\" label=\"B\"><coords x=\"3\" y=\"4\""
				+ " z=\"1\"/></element>" + element("100"));
		assertNotNull(app.getKernel().lookupLabel("B"), "evalXML continues");
		assertTrue(app.reports().isEmpty());
		String macro = "<geogebra format=\"5.0\"><macro cmdName=\"Twice\" toolName=\"Twice\""
				+ " toolHelp=\"\" iconFile=\"\" showInToolBar=\"true\" copyCaptions=\"true\">"
				+ "<macroInput a0=\"P\"/><macroOutput a0=\"Q\"/><construction>"
				+ "<element type=\"point\" label=\"P\"><coords x=\"0\" y=\"0\" z=\"1\"/>"
				+ "</element><command name=\"Dilate\"><input a0=\"P\" a1=\"2\"/>"
				+ "<output a0=\"Q\"/></command><element type=\"point\" label=\"Q\">"
				+ "<coords x=\"0\" y=\"0\" z=\"1\"/></element></construction></macro>"
				+ element("100") + "</geogebra>";
		app.getXMLio().processXMLString(macro, false, true);
		assertNotNull(app.getKernel().getMacro("Twice"), "a tool file ignores it");
		assertTrue(app.reports().isEmpty());
	}

	// ---------------------------------------------------------- T-PERSIST-NO-UNDO

	@Test
	void undoAndRedoNeverReadOrRewindTheDocumentSet() {
		HiddenLayerApp app = app();
		app.setUndoRedoMode(UndoRedoMode.GUI);
		app.setUndoActive(true);
		add(app, "A=(1,2)");
		app.storeUndoInfo();
		app.document = HiddenLayerSet.of(6);
		add(app, "B=(3,4)");
		app.storeUndoInfo();
		app.reports().clear();
		app.getKernel().undo();
		assertEquals(HiddenLayerSet.of(6), app.getDocumentHiddenLayers());
		assertTrue(app.reports().stream().allMatch(HiddenLayerSet::isEmpty),
				"the undo snapshot carries no hidden set");
		app.getKernel().redo();
		assertEquals(HiddenLayerSet.of(6), app.getDocumentHiddenLayers());
		assertTrue(app.getXML().contains(element("6")), "the full document still writes it");
	}

	// ------------------------------------------------------- T-UNITS-INDEPENDENCE

	@Test
	void unitStateAndHiddenLayersRoundTripIndependently() throws Exception {
		HiddenLayerApp app = app();
		add(app, "A=(1,2)");
		UnitState mm = UnitState.of(UnitToken.MM, null, null);
		app.getKernel().getConstruction().getUnitSystem().replace(mm);
		app.document = HiddenLayerSet.of(2, 50);
		String xml = app.getXML();
		assertTrue(xml.contains("<geocedgUnits") && xml.contains(element("2 50")));
		String undoXml = app.getKernel().getConstruction().getCurrentUndoXML(true).toString();
		assertTrue(undoXml.contains("<geocedgUnits"), "units are undoable");
		assertFalse(undoXml.contains(HiddenLayersXml.ELEMENT), "hidden layers are not");

		app.document = HiddenLayerSet.EMPTY;
		app.getKernel().getConstruction().getUnitSystem().replace(UnitState.EMPTY);
		app.reports().clear();
		app.getXMLio().processXMLString(xml, true, false);
		assertEquals(mm, app.getKernel().getConstruction().getUnitSystem().getState());
		assertEquals(List.of(HiddenLayerSet.of(2, 50)), app.reports());

		String unitsOnly = xml.replace(element("2 50") + "\n", "");
		app.reports().clear();
		app.getXMLio().processXMLString(unitsOnly, true, false);
		assertEquals(mm, app.getKernel().getConstruction().getUnitSystem().getState());
		assertEquals(List.of(HiddenLayerSet.EMPTY), app.reports());

		String layersOnly = xml.replaceAll("\\s*<geocedgUnits[^>]*/>", "");
		app.reports().clear();
		app.getXMLio().processXMLString(layersOnly, true, false);
		assertTrue(app.getKernel().getConstruction().getUnitSystem().getState().isEmpty());
		assertEquals(List.of(HiddenLayerSet.of(2, 50)), app.reports());
	}
}
