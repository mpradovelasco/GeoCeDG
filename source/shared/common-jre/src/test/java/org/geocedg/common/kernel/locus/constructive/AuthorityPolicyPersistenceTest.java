/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialRecordXmlCodec;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.MacroConstruction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

/**
 * The persisted document authority policy and its explicit adoption
 * ({@code locus-v2-constructive-authority.md} sections 4 and 5): the shared-kernel part of the
 * validation matrix rows V-AUT-ADOPT-1 to V-AUT-ADOPT-7. The offer of the event by the commands
 * of {@code CU3}, the corpus of the author and the reader of the base build are checked by
 * their own tests.
 */
final class AuthorityPolicyPersistenceTest extends BaseUnitTest {

	private static final String RECORD = DocumentAuthorityRecord.CONSTRUCTIVE_V1.writeXml();

	@Test
	void aNewDocumentStartsUnderTheLegacyPolicyWithoutARecord() {
		add("A=(1,2)");

		assertEquals(AuthorityPolicy.LEGACY_REPRESENTATION, AuthorityPolicy.of(cons()));
		assertFalse(registry().hasDocumentAuthorityRecord());
		assertThat(getApp().getXML(), not(containsString(DocumentAuthorityRecord.ELEMENT)));
	}

	@Test
	void adoptionInAnEmptyDocumentIsExplicitWithAnEmptyReport() {
		AuthorityAdoptionImpact impact = AuthorityPolicyAdoption.report(cons());

		assertTrue(impact.isEmpty(), "V-AUT-ADOPT-2: empty report");
		assertFalse(AuthorityPolicyAdoption.isConstructive(cons()),
				"computing the report applies nothing");
		AuthorityAdoptionImpact applied = AuthorityPolicyAdoption.adopt(cons());

		assertTrue(applied.isEmpty());
		assertEquals(AuthorityPolicy.CONSTRUCTIVE_V1, AuthorityPolicy.of(cons()));
		String xml = getApp().getXML();
		assertThat(xml, containsString("<geocedgSpatial version=\"1\">"));
		assertThat(xml, containsString(RECORD));
		assertTrue(cons().hasSaveRelevantContent(), "an adopted policy is document metadata");
		assertThrows(IllegalStateException.class, () -> AuthorityPolicyAdoption.adopt(cons()),
				"there is one adoption event and no way back but Undo");
	}

	@Test
	void thePolicyIsWrittenBeforeTheIdentityRecordsAndReopensExactly() throws Exception {
		load(fixture("complete-forward-closure.xml"));
		AuthorityPolicyAdoption.adopt(cons());
		String section = registry().writeSpatialSection();
		String xml = getApp().getXML();

		assertTrue(section.startsWith("\t<geocedgSpatial version=\"1\">\n\t\t" + RECORD + "\n"),
				section);
		getApp().getXMLio().processXMLString(xml, true, false, false);

		assertEquals(AuthorityPolicy.CONSTRUCTIVE_V1, AuthorityPolicy.of(cons()));
		assertEquals(section, registry().writeSpatialSection(), "deterministic section");
		assertEquals(xml, getApp().getXML(), "reopening keeps the bytes");
	}

	@Test
	void undoAndRedoRestoreThePolicyExactly() {
		activateUndo();
		add("A=(1,2)");
		getApp().storeUndoInfo();
		String legacyXml = getApp().getXML();
		AuthorityPolicyAdoption.adopt(cons());
		String constructiveXml = getApp().getXML();

		getApp().getUndoManager().undo();
		assertEquals(AuthorityPolicy.LEGACY_REPRESENTATION, AuthorityPolicy.of(cons()),
				"V-AUT-ADOPT-4: Undo restores the earlier policy");
		assertEquals(legacyXml, getApp().getXML());
		getApp().getUndoManager().redo();
		assertEquals(AuthorityPolicy.CONSTRUCTIVE_V1, AuthorityPolicy.of(cons()));
		assertEquals(constructiveXml, getApp().getXML());
	}

	@Test
	void aFailureInsideTheAdoptionMutationRestoresTheDocumentExactly() throws Exception {
		load(fixture("complete-forward-closure.xml"));
		String legacyXml = getApp().getXML();

		IllegalStateException injected = assertThrows(IllegalStateException.class,
				() -> cons().runAtomicConstructionMutation(() -> {
					registry().adoptDocumentAuthority(DocumentAuthorityRecord.CONSTRUCTIVE_V1);
					throw new IllegalStateException("injected after the policy was set");
				}));

		assertEquals("injected after the policy was set", injected.getMessage());
		assertEquals(AuthorityPolicy.LEGACY_REPRESENTATION, AuthorityPolicy.of(cons()));
		assertEquals(legacyXml, getApp().getXML(), "the snapshot restores the legacy bytes");
	}

	@Test
	void ordinaryOperationsNeverChangeThePolicy() {
		GeoElement legacyPoint = add("A=(1,2)");
		editGeoElement(legacyPoint, "(3,4)");
		reload();
		assertFalse(AuthorityPolicyAdoption.isConstructive(cons()),
				"V-AUT-ADOPT-3: editing and reopening a legacy document keeps its policy");

		AuthorityPolicyAdoption.adopt(cons());
		add("B=(5,6)");
		editGeoElement(lookup("A"), "(7,8)");
		add("g=Line(A,B)");
		reload();
		assertTrue(AuthorityPolicyAdoption.isConstructive(cons()),
				"edits, redefinitions and reopening keep the adopted policy");
	}

	@Test
	void anUnsupportedVersionFailsClosedAndLoadsNothing() throws Exception {
		String future = fixture("complete-forward-closure.xml").replace(
				"<geocedgSpatial version=\"1\">", "<geocedgSpatial version=\"1\">\n"
						+ RECORD.replace("constructive-authority/v1",
								"constructive-authority/v2"));

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> load(future));

		assertEquals(SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION,
				failure.getDiagnostic().getCode());
		assertTrue(registry().isEmpty());
		assertFalse(registry().hasDocumentAuthorityRecord());
	}

	@Test
	void anUnknownAttributeOrASecondRecordIsMalformed() throws Exception {
		String unknownAttribute = fixture("complete-forward-closure.xml").replace(
				"<geocedgSpatial version=\"1\">", "<geocedgSpatial version=\"1\">\n"
						+ RECORD.replace("/>", " future=\"x\"/>"));
		String duplicated = fixture("complete-forward-closure.xml").replace(
				"<geocedgSpatial version=\"1\">", "<geocedgSpatial version=\"1\">\n"
						+ RECORD + "\n" + RECORD);

		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				assertThrows(SpatialIdentityException.class, () -> load(unknownAttribute))
						.getDiagnostic().getCode());
		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				assertThrows(SpatialIdentityException.class, () -> load(duplicated))
						.getDiagnostic().getCode());
		assertFalse(registry().hasDocumentAuthorityRecord());
	}

	@Test
	void aReaderWithoutTheRecordRejectsItAsMalformedAndNeverReadsItUnderAnotherAuthority() {
		Map<String, String> attributes = new LinkedHashMap<>();
		attributes.put("authority", DocumentAuthorityRecord.CONSTRUCTIVE_AUTHORITY);
		attributes.put("classTable", DocumentAuthorityRecord.CLASS_TABLE);
		attributes.put("relaxedAdmission", DocumentAuthorityRecord.RELAXED_ADMISSION);
		attributes.put("certifier", DocumentAuthorityRecord.CERTIFIER);
		attributes.put("transfer", DocumentAuthorityRecord.TRANSFER);

		// The record parser of the identity section rejects an unknown record name, which a load
		// turns into MALFORMED_RECORD; the reader of the base build itself is probed separately
		// for V-AUT-ADOPT-7.
		IllegalArgumentException unknown = assertThrows(IllegalArgumentException.class,
				() -> SpatialRecordXmlCodec.parseRecord(DocumentAuthorityRecord.ELEMENT,
						attributes));
		assertThat(unknown.getMessage(), containsString("Unknown geocedgSpatial record"));
	}

	@Test
	void clipboardFragmentsNeverCarryThePolicy() {
		add("A=(1,2)");
		AuthorityPolicyAdoption.adopt(cons());
		String fragment = "<geocedgSpatial version=\"1\">" + RECORD + "</geocedgSpatial>";

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> SpatialIdentityRegistry.preflightClipboardFragment(fragment));
		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				failure.getDiagnostic().getCode());
		assertThat(registry().writeClosureSection(Collections.singletonList(lookup("A"))),
				not(containsString(DocumentAuthorityRecord.ELEMENT)));
		assertTrue(AuthorityPolicyAdoption.isConstructive(cons()),
				"the destination keeps its own policy");
	}

	@Test
	void noOperationLeavesTwoAuthoritiesInOneDocument() throws Exception {
		// Labels distinct from those of the fixture: a shared label would be a redefinition.
		add("Zc=(1,2)");
		AuthorityPolicyAdoption.adopt(cons());
		String constructive = getApp().getXML();
		load(fixture("complete-forward-closure.xml"));
		String legacy = getApp().getXML();
		assertFalse(AuthorityPolicyAdoption.isConstructive(cons()),
				"a whole-document load takes the policy of the document read");

		SpatialIdentityException merge = assertThrows(SpatialIdentityException.class,
				() -> getApp().getXMLio().processXMLString(constructive, false, false, false));

		assertEquals(SpatialIdentityDiagnostic.Code.GENERIC_MERGE_FORBIDDEN,
				merge.getDiagnostic().getCode(), "V-AUT-ADOPT-5: no merge of another authority");
		assertFalse(AuthorityPolicyAdoption.isConstructive(cons()));
		assertEquals(legacy, getApp().getXML(), "the destination is unchanged");
		getKernel().clearConstruction(true);
		add("Zl=(3,4)");
		String plainLegacy = getApp().getXML();
		load(constructive);
		assertTrue(AuthorityPolicyAdoption.isConstructive(cons()));
		String adopted = getApp().getXML();
		assertThrows(SpatialIdentityException.class,
				() -> getApp().getXMLio().processXMLString(legacy, false, false, false),
				"nor the other way round");
		assertEquals(adopted, getApp().getXML());

		getApp().getXMLio().processXMLString(plainLegacy, false, false, false);

		assertTrue(AuthorityPolicyAdoption.isConstructive(cons()),
				"merged objects without identities come under the policy of the destination");
		String merged = getApp().getXML();
		assertEquals(merged.indexOf(RECORD), merged.lastIndexOf(RECORD), "one authority");
	}

	@Test
	void onlyTheRecordOfThisBuildIsAdoptedByADocumentConstruction() {
		assertThrows(IllegalArgumentException.class,
				() -> registry().adoptDocumentAuthority(null));
		SpatialIdentityRegistry detached = new SpatialIdentityRegistry();
		assertThrows(IllegalStateException.class,
				() -> detached.adoptDocumentAuthority(DocumentAuthorityRecord.CONSTRUCTIVE_V1),
				"only a document construction has a policy");
	}

	@Test
	void aMacroConstructionNeverCarriesOrAdoptsAPolicy() throws Exception {
		AuthorityPolicyAdoption.adopt(cons());
		GeoElement first = add("P=(0,0)");
		GeoElement second = add("Q=(1,1)");
		GeoElement output = add("M=Midpoint(P,Q)");
		Macro macro = new Macro(getKernel(), "midpointT", new GeoElement[] {first, second},
				new GeoElement[] {output});
		getKernel().addMacro(macro);
		Construction macroConstruction = macro.getMacroConstruction();

		assertTrue(macroConstruction instanceof MacroConstruction);
		assertEquals(AuthorityPolicy.LEGACY_REPRESENTATION,
				AuthorityPolicy.of(macroConstruction));
		assertThrows(IllegalStateException.class,
				() -> AuthorityPolicyAdoption.report(macroConstruction));
		assertThrows(IllegalStateException.class,
				() -> macroConstruction.getSpatialIdentityRegistry()
						.adoptDocumentAuthority(DocumentAuthorityRecord.CONSTRUCTIVE_V1));
		assertThat(getApp().getAllMacrosXML(),
				not(containsString(DocumentAuthorityRecord.ELEMENT)));
		add("N=midpointT((2,2),(4,4))");
		assertTrue(AuthorityPolicyAdoption.isConstructive(cons()),
				"V-AUT-ADOPT-6: using a tool keeps the policy of the destination");
	}

	private Construction cons() {
		return getConstruction();
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private void load(String xml) throws Exception {
		getApp().getXMLio().processXMLString(xml, true, false, false);
	}

	private static String fixture(String name) throws IOException {
		return new String(Files.readAllBytes(Paths.get(
				"src/test/resources/org/geocedg/common/spatial/g9a1", name)),
				StandardCharsets.UTF_8);
	}
}
