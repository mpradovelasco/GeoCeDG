/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.units;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry.LoadPurpose;
import org.geocedg.common.kernel.units.DocumentUnitSystem;
import org.geocedg.common.kernel.units.UnitDocumentOperations;
import org.geocedg.common.kernel.units.UnitMetadataException;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R6-plus-D1 T-XML-WRITER, T-XML-READER, T-FAIL-CLOSED, T-IGNORED, T-ROUTES
 * (shared rows), T-REBUILD, T-NOTIFY and T-SAVED (shared seam): unit-system v1.0
 * sections 7 to 9 on the real shared parser and serializer.
 */
class PreG9BR6PlusD1UnitXmlTest extends BaseUnitTest {
	private static final UsmDefinition INCH = UsmDefinition.of(0.0254, "inch", "in");

	private DocumentUnitSystem units() {
		return getConstruction().getUnitSystem();
	}

	private String constructionXml() {
		StringBuilder sb = new StringBuilder();
		getConstruction().getConstructionXML(new XMLStringBuilder(sb), false);
		return sb.toString();
	}

	private static String withConstructionChild(String documentXml, String child) {
		int start = documentXml.indexOf("<construction");
		int end = documentXml.indexOf('>', start) + 1;
		return documentXml.substring(0, end) + "\n" + child + documentXml.substring(end);
	}

	private void load(String xml) throws Exception {
		getApp().getXMLio().processXMLString(xml, true, false);
	}

	// ------------------------------------------------------------------ writer

	@Test
	void emptyStateWritesNothing() {
		add("A=(1,2)");
		assertTrue(units().getState().isEmpty());
		assertThat(getApp().getXML(), not(containsString("geocedgUnits")));
		assertThat(constructionXml(), not(containsString("geocedgUnits")));
	}

	@Test
	void writerEmitsEveryGrammarCaseInTheSpecifiedOrder() {
		String[][] cases = {
			{"mm", null, null},
			{"mm", "cm", null},
			{"usm", "cm", "inch"},
			{null, null, "foot"}
		};
		String[] expected = {
			"\t<geocedgUnits version=\"1\" construction=\"mm\"/>\n",
			"\t<geocedgUnits version=\"1\" construction=\"mm\" presentation=\"cm\"/>\n",
			"\t<geocedgUnits version=\"1\" construction=\"usm\" presentation=\"cm\""
					+ " usmMetersPerUnit=\"0.0254\" usmName=\"inch\" usmSymbol=\"in\"/>\n",
			"\t<geocedgUnits version=\"1\" usmMetersPerUnit=\"0.3048\" usmSymbol=\"ft\"/>\n"
		};
		for (int i = 0; i < cases.length; i++) {
			UsmDefinition usm = cases[i][2] == null ? null : "inch".equals(cases[i][2])
					? INCH : UsmDefinition.of(0.3048, null, "ft");
			units().replace(UnitState.of(UnitToken.fromToken(cases[i][0]),
					UnitToken.fromToken(cases[i][1]), usm));
			assertThat(constructionXml(), containsString(expected[i]));
		}
	}

	@Test
	void writerEscapesNamesAndPlacesTheElementBeforeSpatialAndElements() {
		add("A=(1,2)");
		getConstruction().setWorksheetText("above", 0);
		units().replace(UnitState.of(UnitToken.USM, null,
				UsmDefinition.of(1.5, "A&B <\"q\">", "\u00b5")));
		String xml = constructionXml();
		assertThat(xml, containsString("usmName=\"A&amp;B &lt;&quot;q&quot;&gt;\""));
		int worksheet = xml.indexOf("<worksheetText");
		int unit = xml.indexOf("<geocedgUnits");
		int element = xml.indexOf("<element");
		assertTrue(worksheet >= 0 && worksheet < unit && unit < element, xml);
	}

	// ------------------------------------------------------------------ reader

	@Test
	void everyWriterCaseRoundTripsThroughAClearingLoad() throws Exception {
		add("A=(1,2)");
		UnitState[] states = {
			UnitState.of(UnitToken.MM, null, null),
			UnitState.of(UnitToken.CM, UnitToken.M, null),
			UnitState.of(UnitToken.USM, UnitToken.CM, INCH),
			UnitState.of(UnitToken.M, UnitToken.USM, INCH),
			UnitState.of(null, null, UsmDefinition.of(0.3048, null, "ft")),
			UnitState.of(UnitToken.MM, null, UsmDefinition.of(Double.MIN_VALUE, null, null))
		};
		for (UnitState state : states) {
			units().replace(state);
			String xml = getApp().getXML();
			units().replace(UnitState.EMPTY);
			load(xml);
			assertEquals(state, units().getState());
			assertEquals(xml, getApp().getXML(), "re-save is byte-identical");
		}
	}

	@Test
	void anyChildPositionIsAcceptedAndNonCanonicalFactorsNormalize() throws Exception {
		add("A=(1,2)");
		String xml = getApp().getXML().replace("</construction>",
				"<geocedgUnits version=\"1\" construction=\"usm\" usmMetersPerUnit=\"0.02540\"/>"
						+ "\n</construction>");
		load(xml);
		assertEquals(0.0254, units().getState().getUsm().getMetresPerUnit());
		assertThat(getApp().getXML(), containsString("usmMetersPerUnit=\"0.0254\""));
		load(withConstructionChild(getApp().getXML().replaceAll(
				"\\s*<geocedgUnits[^>]*/>", ""), "<geocedgUnits version=\"1\"/>"));
		assertTrue(units().getState().isEmpty(), "a bare element denotes EMPTY");
		assertThat(getApp().getXML(), not(containsString("geocedgUnits")));
	}

	// ------------------------------------------------------------- fail closed

	@Test
	void everyRecognizedDefectFailsClosedAndRestoresTheEntryState() throws Exception {
		add("A=(1,2)");
		units().replace(UnitState.of(UnitToken.CM, null, null));
		String original = getApp().getXML();
		String base = original.replaceAll("\\s*<geocedgUnits[^>]*/>", "");
		Object[][] cases = {
			{"<geocedgUnits version=\"3\" construction=\"mm\"/>",
				UnitMetadataException.Code.UNSUPPORTED_VERSION},
			{"<geocedgUnits version=\"99\" future=\"x\" construction=\"km\"/>",
				UnitMetadataException.Code.UNSUPPORTED_VERSION},
			{"<geocedgUnits construction=\"mm\"/>", UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"one\"/>", UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"0\"/>", UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"1\" construction=\"mm\" scale=\"1\"/>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"1\" construction=\"MM\"/>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"1\" presentation=\"mm\"/>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"1\" usmMetersPerUnit=\"abc\"/>",
				UnitMetadataException.Code.INVALID_USM_FACTOR},
			{"<geocedgUnits version=\"1\" usmMetersPerUnit=\"0\"/>",
				UnitMetadataException.Code.INVALID_USM_FACTOR},
			{"<geocedgUnits version=\"1\" usmMetersPerUnit=\"-1\"/>",
				UnitMetadataException.Code.INVALID_USM_FACTOR},
			{"<geocedgUnits version=\"1\" usmMetersPerUnit=\"0,0254\"/>",
				UnitMetadataException.Code.INVALID_USM_FACTOR},
			{"<geocedgUnits version=\"1\" construction=\"mm\" usmName=\"inch\"/>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"1\" usmMetersPerUnit=\"1\" usmSymbol=\" in\"/>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"1\" construction=\"usm\"/>",
				UnitMetadataException.Code.INVALID_USM_FACTOR},
			{"<geocedgUnits version=\"1\" construction=\"mm\" presentation=\"usm\"/>",
				UnitMetadataException.Code.INVALID_USM_FACTOR},
			{"<geocedgUnits version=\"1\" construction=\"mm\"><x/></geocedgUnits>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"1\" construction=\"mm\">text</geocedgUnits>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"1\" construction=\"mm\"/>\n"
					+ "<geocedgUnits version=\"1\" construction=\"mm\"/>",
				UnitMetadataException.Code.DUPLICATE_ELEMENT}
		};
		for (Object[] c : cases) {
			String xml = withConstructionChild(base, (String) c[0]);
			UnitMetadataException failure = assertThrows(UnitMetadataException.class,
					() -> load(xml), (String) c[0]);
			assertEquals(c[1], failure.getCode(), (String) c[0]);
			assertEquals(original, getApp().getXML(), "entry snapshot restored: " + c[0]);
			assertEquals(UnitState.of(UnitToken.CM, null, null), units().getState());
		}
	}

	@Test
	void misplacedElementsFailClosedInADocument() throws Exception {
		add("A=(1,2)");
		String original = getApp().getXML();
		String topLevel = original.replace("<construction",
				"<geocedgUnits version=\"1\" construction=\"mm\"/>\n<construction");
		assertEquals(UnitMetadataException.Code.MISPLACED_ELEMENT,
				assertThrows(UnitMetadataException.class, () -> load(topLevel)).getCode());
		assertEquals(original, getApp().getXML());
		int element = original.indexOf("<element");
		int elementEnd = original.indexOf('>', element) + 1;
		String nested = original.substring(0, elementEnd)
				+ "<geocedgUnits version=\"1\" construction=\"mm\"/>"
				+ original.substring(elementEnd);
		assertEquals(UnitMetadataException.Code.MISPLACED_ELEMENT,
				assertThrows(UnitMetadataException.class, () -> load(nested)).getCode());
		assertEquals(original, getApp().getXML());
	}

	// ---------------------------------------------------------- ignored contexts

	@Test
	void nonClearingParsesIgnoreTheElementEvenWhenMalformed() throws Exception {
		add("A=(1,2)");
		units().replace(UnitState.of(UnitToken.MM, null, null));
		String fragment = "<geocedgUnits version=\"7\" construction=\"km\"/>"
				+ "<element type=\"point\" label=\"R\"><coords x=\"3\" y=\"4\" z=\"1\"/>"
				+ "</element>";
		getApp().getGgbApi().evalXML(fragment);
		assertNotNull(lookup("R"));
		assertEquals(UnitState.of(UnitToken.MM, null, null), units().getState());
		String merge = "<geogebra format=\"5.0\"><construction>"
				+ "<geocedgUnits version=\"1\" construction=\"m\"/>"
				+ "<element type=\"point\" label=\"S\"><coords x=\"1\" y=\"1\" z=\"1\"/>"
				+ "</element></construction></geogebra>";
		getApp().getXMLio().processXMLString(merge, false, false);
		assertNotNull(lookup("S"));
		assertEquals(UnitState.of(UnitToken.MM, null, null), units().getState());
		assertTrue(InternalClipboard.evalClipboardXMLAtomically(getApp(),
				"<geocedgUnits version=\"1\" construction=\"cm\"/>"
						+ "<element type=\"point\" label=\"T\"><coords x=\"2\" y=\"2\""
						+ " z=\"1\"/></element>"));
		assertNotNull(lookup("T"));
		assertEquals(UnitState.of(UnitToken.MM, null, null), units().getState(),
				"paste never changes the target unit");
	}

	@Test
	void macroConstructionsNeverCarryAndAlwaysIgnoreTheElement() throws Exception {
		units().replace(UnitState.of(UnitToken.M, null, INCH));
		String macro = "<macro cmdName=\"Unit\" toolName=\"Unit\" toolHelp=\"\""
				+ " iconFile=\"\" showInToolBar=\"true\" copyCaptions=\"true\">"
				+ "<macroInput a0=\"A\"/><macroOutput a0=\"B\"/><construction>"
				+ "<geocedgUnits version=\"9\" construction=\"km\"/>"
				+ "<element type=\"point\" label=\"A\"><coords x=\"0\" y=\"0\" z=\"1\"/>"
				+ "</element><command name=\"Dilate\"><input a0=\"A\" a1=\"2\"/>"
				+ "<output a0=\"B\"/></command><element type=\"point\" label=\"B\">"
				+ "<coords x=\"0\" y=\"0\" z=\"1\"/></element></construction></macro>";
		assertTrue(getApp().addMacroXML(macro));
		assertNotNull(getKernel().getMacro("Unit"));
		assertThat(getApp().getMacroXML(getKernel().getMacro("Unit")),
				not(containsString("geocedgUnits")));
		assertTrue(getKernel().getMacro("Unit").getMacroConstruction().getUnitSystem()
				.getState().isEmpty());
		assertEquals(UnitState.of(UnitToken.M, null, INCH), units().getState());
	}

	@Test
	void preferencesXmlNeverContainsUnitState() {
		units().replace(UnitState.of(UnitToken.USM, UnitToken.MM, INCH));
		assertThat(getApp().getXMLio().getPreferencesXML(), not(containsString("Units")));
	}

	// ----------------------------------------------------- operations and undo

	@Test
	void eachOperationIsOneUndoPointAndUndoRedoRestoreExactly() throws Exception {
		add("A=(1,2)");
		activateUndo();
		getApp().storeUndoInfo();
		int history = getConstruction().getUndoManager().getHistorySize();
		UnitState first = UnitState.of(UnitToken.MM, null, null);
		assertTrue(UnitDocumentOperations.commit(getApp(), first));
		assertEquals(history + 1, getConstruction().getUndoManager().getHistorySize());
		assertFalse(UnitDocumentOperations.commit(getApp(), first), "a no-op stores nothing");
		assertEquals(history + 1, getConstruction().getUndoManager().getHistorySize());
		UnitState second = first.withUsm(INCH).withConstructionUnit(UnitToken.USM);
		assertTrue(UnitDocumentOperations.commit(getApp(), second), "define + select");
		assertEquals(history + 2, getConstruction().getUndoManager().getHistorySize());
		getKernel().undo();
		assertEquals(first, units().getState());
		getKernel().undo();
		assertTrue(units().getState().isEmpty());
		getKernel().redo();
		assertEquals(first, units().getState());
		getKernel().redo();
		assertEquals(second, units().getState());
	}

	@Test
	void redefineAndRollbackRoutesCarryTheState() throws Exception {
		add("A=(1,2)");
		add("B=A+(1,0)");
		UnitState state = UnitState.of(UnitToken.CM, UnitToken.MM, INCH);
		units().replace(state);
		editGeoElement(lookup("A"), "(5,6)");
		assertEquals(state, units().getState(), "redefine rebuild");
		assertThrows(IllegalStateException.class,
				() -> getConstruction().runAtomicConstructionMutation(() -> {
					add("Q=(9,9)");
					throw new IllegalStateException("mutation failed");
				}));
		assertEquals(state, units().getState(), "atomic rollback restore");
		assertFalse(InternalClipboard.evalClipboardXMLAtomically(getApp(),
				"<element type=\"point\" label=\"P\"><coords x=\"1\" y=\"1\" z=\"1\"/>"
						+ "</element>", null, () -> {
							throw new IllegalStateException("paste failed");
						}));
		assertEquals(state, units().getState(), "paste rollback restore");
		getConstruction().processXML(getApp().getXMLio().getUndoXML(getConstruction(),
				false).toString(), false, null);
		assertEquals(state, units().getState(), "construction processXML");
	}

	@Test
	void notificationFiresOnEveryChangeAndNeverOnANoOp() throws Exception {
		AtomicInteger notifications = new AtomicInteger();
		units().addListener(notifications::incrementAndGet);
		UnitState state = UnitState.of(UnitToken.MM, null, null);
		units().replace(state);
		assertEquals(1, notifications.get());
		units().replace(state);
		assertEquals(1, notifications.get());
		String xml = getApp().getXML();
		getKernel().clearConstruction(true);
		assertEquals(2, notifications.get(), "reset");
		load(xml);
		assertEquals(3, notifications.get(), "application by a clearing load");
		assertEquals(state, units().getState());
	}

	// ------------------------------------------------------------- saved state

	@Test
	void saveRelevantContentIsANarrowSeamBesideIsStarted() {
		boolean started = getConstruction().isStarted();
		assertSame(started, getConstruction().hasSaveRelevantContent(),
				"without unit metadata the seam equals isStarted");
		units().replace(UnitState.of(UnitToken.MM, null, null));
		assertSame(started, getConstruction().isStarted(), "isStarted keeps its meaning");
		assertTrue(getConstruction().hasSaveRelevantContent());
		getApp().setUnsaved();
		assertFalse(getApp().isSaved(), "unit metadata is save-relevant content");
		getApp().setSaved();
		assertTrue(getApp().isSaved());
		units().replace(UnitState.EMPTY);
		assertSame(started, getConstruction().hasSaveRelevantContent());
		add("A=(1,2)");
		assertTrue(getConstruction().isStarted());
		assertTrue(getConstruction().hasSaveRelevantContent());
	}

	@Test
	void undoSnapshotCarriesTheElement() {
		units().replace(UnitState.of(UnitToken.M, null, null));
		assertThat(getApp().getXMLio().getUndoXML(getConstruction(), false).toString(),
				containsString("<geocedgUnits version=\"1\" construction=\"m\"/>"));
		getConstruction().setNextSpatialIdentityLoadPurpose(LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		assertSame(LoadPurpose.NATIVE_OR_UNDO_RESTORE,
				getConstruction().peekSpatialIdentityLoadPurpose());
		assertSame(LoadPurpose.NATIVE_OR_UNDO_RESTORE,
				getConstruction().peekSpatialIdentityLoadPurpose(), "peek never consumes");
		getConstruction().clearNextSpatialIdentityLoadPurpose();
	}
}
