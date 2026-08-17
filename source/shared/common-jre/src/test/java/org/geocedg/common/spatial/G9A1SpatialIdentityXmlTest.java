/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialResolutionState;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

class G9A1SpatialIdentityXmlTest extends BaseUnitTest {
	private static final String GEO_A =
			"geo:00000000000000000000000000000001";
	private static final String BINDING =
			"binding:00000000000000000000000000000008";

	@Test
	void legacyXmlLoadsWithoutSynthesizingIdentity() throws Exception {
		loadFixture("legacy-no-identities.xml", true);

		assertTrue(registry().isEmpty());
		assertNull(registry().getPersistentGeoId(lookup("A")));
		assertThat(getApp().getXML(), not(containsString("geocedgSpatial")));
		assertThat(getApp().getXML(), not(containsString("geocedgId")));
	}

	@Test
	void realHostParserResolvesAForwardDeclaredCompleteClosure() throws Exception {
		loadFixture("complete-forward-closure.xml", true);
		PersistentGeoId geoId = PersistentGeoId.parse(GEO_A);
		GeoElement geo = lookup("A");

		assertEquals(10, registry().size());
		assertEquals(geoId, registry().getPersistentGeoId(geo));
		assertSame(geo, registry().getGeo(geoId));
		for (SpatialIdentityRecord record : registry().getRecords()) {
			assertEquals(SpatialResolutionState.ACTIVE,
					registry().getResolution(record.getId()).getState());
		}
		assertThat(getApp().getXML(), containsString(
				"geocedgId=\"" + GEO_A + "\""));
		assertThat(geo.getStyleXML(), not(containsString("geocedgId")));
	}

	@Test
	void reopenRestoresExactIdsAndDeterministicSection() throws Exception {
		GeoElement geo = add("A=1");
		PersistentGeoId geoId = registry().allocatePersistentGeoId();
		GeoIdentityRecord record = new GeoIdentityRecord(geoId, "g9a1.test",
				"POINT", "cedg.point.orthographic", 1,
				EditAuthorityMode.PROJECTION_DEFINED,
				ProjectionBindingRole.DEFINING, "POINT", 1, 0, 0);
		assertThrows(IllegalArgumentException.class,
				() -> new GeoIdentityRecord(geoId, "g9a1\ninvalid", "POINT",
						"cedg.point.orthographic", 1,
						EditAuthorityMode.PROJECTION_DEFINED,
						ProjectionBindingRole.DEFINING, "POINT", 1, 0, 0));
		assertThrows(IllegalArgumentException.class,
				() -> new GeoIdentityRecord(geoId, "g9a1.test", "POINT",
						"cedg.\u0001point", 1,
						EditAuthorityMode.PROJECTION_DEFINED,
						ProjectionBindingRole.DEFINING, "POINT\tOUTPUT", 1, 0, 0));
		registry().registerParticipation(geo, record);
		String firstSection = registry().writeSpatialSection();
		String saved = getApp().getXML();

		getApp().getXMLio().processXMLString(saved, true, false, false);

		assertEquals(firstSection, registry().writeSpatialSection());
		assertEquals(geoId,
				registry().getPersistentGeoId(lookup("A")));
		assertEquals(saved, getApp().getXML());
	}

	@Test
	void deterministicWriterIsIndependentOfFixtureRecordOrder() throws Exception {
		loadFixture("complete-forward-closure.xml", true);

		String first = registry().writeSpatialSection();
		String second = registry().writeSpatialSection();

		assertEquals(first, second);
		assertTrue(first.indexOf("<geo ") < first.indexOf("<object "));
		assertTrue(first.indexOf("<object ") < first.indexOf("<frame "));
		assertTrue(first.indexOf("<frame ") < first.indexOf("<system "));
		assertTrue(first.indexOf("<system ") < first.indexOf("<diagramMap "));
		assertTrue(first.indexOf("<diagramMap ") < first.indexOf("<frameRelation "));
		assertTrue(first.indexOf("<frameRelation ") < first.indexOf("<binding "));
	}

	@Test
	void missingReferencePublishesBrokenRecordWithoutLabelRepair() throws Exception {
		loadFixture("missing-reference.xml", true);
		ProjectionBindingId bindingId = ProjectionBindingId.parse(BINDING);

		assertEquals(SpatialResolutionState.BROKEN,
				registry().getResolution(bindingId).getState());
		assertTrue(registry().getResolution(bindingId).getDiagnostics().stream()
				.allMatch(diagnostic -> diagnostic.getCode()
						== SpatialIdentityDiagnostic.Code.MISSING_REFERENCE));
		assertEquals(0, registry().getInstrumentation().getLabelAuthorityUses());
	}

	@Test
	void malformedIdFailsAtomicallyWithStructuredDiagnostic() throws Exception {
		assertNativeLoadFailure("malformed-id.xml",
				SpatialIdentityDiagnostic.Code.MALFORMED_ID);
	}

	@Test
	void duplicateNativeIdFailsAtomically() throws Exception {
		assertNativeLoadFailure("duplicate-id.xml",
				SpatialIdentityDiagnostic.Code.DUPLICATE_ID);
	}

	@Test
	void crossKindTokenReuseFailsAtomically() throws Exception {
		assertNativeLoadFailure("cross-kind-token.xml",
				SpatialIdentityDiagnostic.Code.CROSS_KIND_TOKEN_REUSE);
	}

	@Test
	void unsupportedFutureVersionFailsAtomicallyBeforeRecordParsing() throws Exception {
		String unknownSectionAttribute = readFixture("complete-forward-closure.xml")
				.replace("<geocedgSpatial version=\"1\">",
						"<geocedgSpatial version=\"1\" future=\"opaque\">");
		SpatialIdentityException malformed = assertThrows(
				SpatialIdentityException.class,
				() -> getApp().getXMLio().processXMLString(
						unknownSectionAttribute, true, false, false));
		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				malformed.getDiagnostic().getCode());
		assertTrue(registry().isEmpty());
		assertFalse(getConstruction().isFileLoading());
		String freeText = readFixture("complete-forward-closure.xml")
				.replace("<geocedgSpatial version=\"1\">",
						"<geocedgSpatial version=\"1\">unexpected");
		SpatialIdentityException textFailure = assertThrows(
				SpatialIdentityException.class,
				() -> getApp().getXMLio().processXMLString(
						freeText, true, false, false));
		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				textFailure.getDiagnostic().getCode());
		assertTrue(registry().isEmpty());
		assertFalse(getConstruction().isFileLoading());
		String nestedRecord = readFixture("complete-forward-closure.xml")
				.replaceFirst("revision=\"0\"/>",
						"revision=\"0\"><geo/></binding>");
		SpatialIdentityException nestingFailure = assertThrows(
				SpatialIdentityException.class,
				() -> getApp().getXMLio().processXMLString(
						nestedRecord, true, false, false));
		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				nestingFailure.getDiagnostic().getCode());
		assertTrue(registry().isEmpty());
		assertFalse(getConstruction().isFileLoading());
		assertNativeLoadFailure("future-version.xml",
				SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION);
	}

	@Test
	void unsupportedPerRecordVersionFailsWithoutPublishingOrLeakingLoadState()
			throws Exception {
		String before = getConstruction().getCurrentUndoXML(true).toString();
		String unsupportedRecord = readFixture("complete-forward-closure.xml")
				.replaceFirst("semanticVersion=\"1\"",
						"semanticVersion=\"999\"");

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> getApp().getXMLio().processXMLString(unsupportedRecord,
						true, false, false));

		assertEquals(SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION,
				failure.getDiagnostic().getCode());
		assertEquals(before,
				getConstruction().getCurrentUndoXML(true).toString());
		assertNull(lookup("A"));
		assertTrue(registry().isEmpty());
		assertFalse(getConstruction().isFileLoading());
	}

	@Test
	void identityBearingGenericMergeRollsBackEarlierHostMutationExactly()
			throws Exception {
		loadFixture("complete-forward-closure.xml", true);
		activateUndo();
		getApp().storeUndoInfo();
		PersistentGeoId originalId = registry().getPersistentGeoId(lookup("A"));
		String originalSection = registry().writeSpatialSection();
		String originalXml = getApp().getXML();
		int originalUndoSize = getConstruction().getUndoManager().getHistorySize();
		String mutatingMerge = moveSpatialSectionAfterElements(
				readFixture("complete-forward-closure.xml"))
				.replace("<value val=\"1.0\"/>", "<value val=\"999.0\"/>");

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> getApp().getXMLio().processXMLString(mutatingMerge,
						false, false, false));

		assertEquals(SpatialIdentityDiagnostic.Code.GENERIC_MERGE_FORBIDDEN,
				failure.getDiagnostic().getCode());
		assertEquals(originalXml, getApp().getXML());
		assertEquals(originalSection, registry().writeSpatialSection());
		assertEquals(1, lookup("A").evaluateDouble());
		assertEquals(originalId, registry().getPersistentGeoId(lookup("A")));
		assertSame(lookup("A"), registry().getGeo(originalId));
		assertEquals(originalUndoSize,
				getConstruction().getUndoManager().getHistorySize());
		assertFalse(getConstruction().isFileLoading());
	}

	private void assertNativeLoadFailure(String fixture,
			SpatialIdentityDiagnostic.Code expectedCode) throws Exception {
		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> loadFixture(fixture, true));

		assertEquals(expectedCode, failure.getDiagnostic().getCode());
		assertTrue(registry().isEmpty());
		assertFalse(getConstruction().isFileLoading());
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private void loadFixture(String name, boolean clear) throws IOException,
			XMLParseException {
		getApp().getXMLio().processXMLString(readFixture(name), clear, false, false);
	}

	private static String readFixture(String name) throws IOException {
		Path path = Paths.get("src/test/resources/org/geocedg/common/spatial/g9a1",
				name);
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}

	private static String moveSpatialSectionAfterElements(String xml) {
		String closingTag = "    </geocedgSpatial>";
		int sectionStart = xml.indexOf("    <geocedgSpatial");
		int sectionEnd = xml.indexOf(closingTag, sectionStart) + closingTag.length();
		String section = xml.substring(sectionStart, sectionEnd);
		String withoutSection = xml.substring(0, sectionStart)
				+ xml.substring(sectionEnd);
		int constructionEnd = withoutSection.indexOf("  </construction>");
		return withoutSection.substring(0, constructionEnd) + section + "\n"
				+ withoutSection.substring(constructionEnd);
	}
}
