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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingId;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRelationId;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialRecordResolution;
import org.geocedg.common.kernel.spatial.identity.SpatialResolutionState;
import org.geocedg.common.kernel.spatial.runtime.SpatialPointPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialSemanticRuntime;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.io.XMLParseException;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class G9A3SpatialCompatibilityXmlTest extends BaseUnitTest {
	private static final String G9A1 = "g9a1";
	private static final String G9A2 = "g9a2";
	private static final String G9A3 = "g9a3";
	private static final ProjectionBindingId V2_MISSING_BINDING =
			ProjectionBindingId.parse(
					"binding:00000000000000000000000000000052");
	private static final ProjectionSystemId V1_SYSTEM = new ProjectionSystemId(
			"00000000000000000000000000000005");
	private static final SpatialObjectId V1_OBJECT = new SpatialObjectId(
			"00000000000000000000000000000003");
	private static final ProjectionSystemId V2_SYSTEM = new ProjectionSystemId(
			"00000000000000000000000000000021");
	private static final ProjectionFrameRelationId V2_RELATION =
			new ProjectionFrameRelationId(
					"00000000000000000000000000000041");
	private static final SpatialObjectId V2_OBJECT = new SpatialObjectId(
			"00000000000000000000000000000061");
	private static final PersistentGeoId MISSING_SUPPORT = PersistentGeoId.parse(
			"geo:0000000000000000000000000000ffff");

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D();
	}

	@Test
	void xml01LegacyFileWithoutSpatialSectionRemainsUnassociated()
			throws Exception {
		loadFixture(G9A3, "legacy-adversarial-coincident.xml");

		assertTrue(registry().isEmpty());
		for (String label : List.of("HorizontalProjection", "VerticalProjection",
				"P_h", "P_v")) {
			GeoElement geo = lookup(label);
			assertNotNull(geo);
			assertNull(registry().getPersistentGeoId(geo));
		}
		assertThat(getApp().getXML(), not(containsString("geocedgSpatial")));
		assertThat(getApp().getXML(), not(containsString("geocedgId")));
		assertForbiddenAssociationCountersRemainZero();
	}

	@Test
	void xml02VersionOneRecordsRemainInertWithoutPointSemantics()
			throws Exception {
		loadFixture(G9A2, "v1-inert-point-closure.xml");

		assertEquals(10, registry().size());
		assertTrue(registry().getRecords().stream()
				.allMatch(record -> record.getSemanticVersion() == 1));
		SpatialSemanticRuntime runtime = semanticRuntime();
		assertNull(runtime.getProjectionSystemCertificate(V1_SYSTEM));
		assertNull(runtime.getSpatialPointCertificate(V1_OBJECT));
		assertNull(runtime.getDerivedPoint(V1_OBJECT));
		assertEquals(0, runtime.getInstrumentation().getReconstructionAttempts());
	}

	@Test
	void xml03ShuffledVersionTwoClosureResolvesWithoutOrderAuthority()
			throws Exception {
		loadFixture(G9A3, "v2-shuffled-forward-closure.xml");

		assertEquals(18, registry().size());
		for (SpatialIdentityRecord record : registry().getRecords()) {
			assertEquals(SpatialResolutionState.ACTIVE,
					registry().getResolution(record.getId()).getState());
		}
		assertCanonicalPoint();
		assertForbiddenAssociationCountersRemainZero();
	}

	@Test
	void xml04MissingBindingReferenceStaysBrokenWithoutRepair()
			throws Exception {
		loadFixture(G9A3, "v2-missing-binding-reference.xml");

		assertMissingBindingReferenceState();
		assertForbiddenAssociationCountersRemainZero();
		String canonical = getApp().getXML();
		assertThat(canonical, containsString(V2_MISSING_BINDING.toExternalForm()));
		assertThat(canonical, not(containsString("<binding id=\""
				+ V2_MISSING_BINDING.toExternalForm() + "\"")));

		getApp().getXMLio().processXMLString(canonical, true, false, false);

		assertEquals(canonical, getApp().getXML());
		assertMissingBindingReferenceState();
		assertForbiddenAssociationCountersRemainZero();
	}

	@Test
	void xml05MissingRelationSupportNamesExactIdAndNeverInfersGeometry()
			throws Exception {
		Document missingFixture = parseFixture(
				G9A3, "v2-missing-relation-support.xml");
		String missingExternal = attributes(
				elements(missingFixture, "frameRelation").get(0)).get("supportEnd");
		assertEquals(MISSING_SUPPORT.toExternalForm(), missingExternal);
		String xml = readFixture(G9A3, "v2-shuffled-forward-closure.xml")
				.replace("supportEnd=\"geo:00000000000000000000000000000002\"",
						"supportEnd=\"" + missingExternal + "\"");

		getApp().getXMLio().processXMLString(xml, true, false, false);

		SpatialRecordResolution resolution = registry().getResolution(V2_RELATION);
		assertEquals(SpatialResolutionState.BROKEN, resolution.getState());
		assertTrue(resolution.getDiagnostics().stream()
				.anyMatch(diagnostic -> diagnostic.getCode()
						== SpatialIdentityDiagnostic.Code.MISSING_REFERENCE
						&& MISSING_SUPPORT.equals(diagnostic.getReference())));
		SpatialPointPilotCertificate point =
				semanticRuntime().getSpatialPointCertificate(V2_OBJECT);
		assertTrue(point == null || !point.getSemanticCertificate().hasPayload());
		assertTrue(semanticRuntime().getDerivedPoint(V2_OBJECT) == null
				|| !semanticRuntime().getDerivedPoint(V2_OBJECT).isDefined());
		assertForbiddenAssociationCountersRemainZero();
	}

	@Test
	void xml06DuplicateNativeIdentityRejectsAtomicallyWithoutRemap()
			throws Exception {
		Document fixture = parseFixture(G9A3, "v2-duplicate-semantic-id.xml");
		SpatialIdentityRegistry target = new SpatialIdentityRegistry();
		SpatialIdentityRegistry.LoadSession session = target.beginLoadSession(
				SpatialIdentityRegistry.LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		for (Element frame : elements(fixture, "frame")) {
			session.stageRecord("frame", attributes(frame));
		}

		SpatialIdentityException failure = assertThrows(
				SpatialIdentityException.class, session::commit);

		assertEquals(SpatialIdentityDiagnostic.Code.DUPLICATE_ID,
				failure.getDiagnostic().getCode());
		assertTrue(target.isEmpty());
		assertEquals(0, target.getInstrumentation().getRemaps());
	}

	@Test
	void xml07MalformedRolesFamiliesAndSchemaRejectBeforePublication()
			throws Exception {
		Document fixture = parseFixture(G9A3, "v2-malformed-role-family.xml");
		Element map = elements(fixture, "diagramMap").get(0);
		Map<String, String> invalidRole = attributes(map);
		assertStrictRecordRejection("diagramMap", invalidRole);

		Map<String, String> invalidFamily = new LinkedHashMap<>(invalidRole);
		invalidFamily.put("role", "DEFINING");
		assertStrictRecordRejection("diagramMap", invalidFamily);

		Element binding = elements(fixture, "binding").get(0);
		assertStrictRecordRejection("binding", attributes(binding));
		Map<String, String> invalidSchema = attributes(binding);
		invalidSchema.put("role", "DEFINING");
		invalidSchema.put("schema", "");
		assertStrictRecordRejection("binding", invalidSchema);
	}

	@Test
	void xml08UnknownAttributesAndFutureVersionsFailWithoutDowngrade()
			throws Exception {
		loadFixture(G9A3, "v2-shuffled-forward-closure.xml");
		G9A3SpatialGraphSnapshot.Snapshot before =
				G9A3SpatialGraphSnapshot.capture(getConstruction());

		assertFailedLoadPreservesGraph(
				readFixture(G9A2, "v2-malformed-unknown-attribute.xml"),
				SpatialIdentityDiagnostic.Code.MALFORMED_RECORD, before);
		assertFailedLoadPreservesGraph(
				readFixture(G9A2, "v2-unknown-record-version.xml"),
				SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION, before);
		assertFailedLoadPreservesGraph(
				readFixture(G9A3, "v2-future-outer-version.xml"),
				SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION, before);
		assertEquals(0, registry().getInstrumentation().getRemaps());
	}

	@Test
	void xml09NativeCollisionRejectsWhileExplicitImportRemapsWholeClosure()
			throws Exception {
		loadFixture(G9A1, "complete-forward-closure.xml");
		List<SpatialIdentityRecord> sourceRecords =
				new ArrayList<>(registry().getRecords());
		SpatialIdentityRegistry target = new SpatialIdentityRegistry();

		SpatialIdentityRegistry.LoadSession nativeLoad = target.beginLoadSession(
				SpatialIdentityRegistry.LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		stageClosure(nativeLoad, sourceRecords, "NativeA");
		nativeLoad.commit();
		String nativeSection = target.writeSpatialSection();

		SpatialIdentityRegistry.LoadSession nativeCollision =
				target.beginLoadSession(
						SpatialIdentityRegistry.LoadPurpose.NATIVE_OR_UNDO_RESTORE);
		stageClosure(nativeCollision, sourceRecords, "NativeB");
		SpatialIdentityException failure = assertThrows(
				SpatialIdentityException.class, nativeCollision::commit);
		assertEquals(SpatialIdentityDiagnostic.Code.DUPLICATE_ID,
				failure.getDiagnostic().getCode());
		assertEquals(nativeSection, target.writeSpatialSection());

		SpatialIdentityRegistry.LoadSession explicitImport = target.beginLoadSession(
				SpatialIdentityRegistry.LoadPurpose.CLIPBOARD_IMPORT);
		stageClosure(explicitImport, sourceRecords, "Import");
		Map<SpatialIdentityId, SpatialIdentityId> remap = explicitImport.commit();

		assertEquals(sourceRecords.size(), remap.size());
		assertEquals(sourceRecords.size() * 2, target.size());
		for (SpatialIdentityRecord source : sourceRecords) {
			SpatialIdentityId mappedId = remap.get(source.getId());
			assertNotNull(mappedId);
			assertNotEquals(source.getId(), mappedId);
			SpatialIdentityRecord mapped = target.getRecord(mappedId);
			assertNotNull(mapped);
			for (SpatialIdentityId sourceReference : source.getReferences()) {
				assertTrue(mapped.getReferences().contains(remap.get(sourceReference)));
			}
		}
	}

	@Test
	void xml10RepeatedReopenProducesOneCanonicalIdentityGraph()
			throws Exception {
		loadFixture(G9A3, "v2-shuffled-forward-closure.xml");
		G9A3SpatialGraphSnapshot.Snapshot first =
				G9A3SpatialGraphSnapshot.capture(getConstruction());
		String firstXml = getApp().getXML();

		getApp().getXMLio().processXMLString(firstXml, true, false, false);
		G9A3SpatialGraphSnapshot.Snapshot second =
				G9A3SpatialGraphSnapshot.capture(getConstruction());
		String secondXml = getApp().getXML();
		getApp().getXMLio().processXMLString(secondXml, true, false, false);
		G9A3SpatialGraphSnapshot.Snapshot third =
				G9A3SpatialGraphSnapshot.capture(getConstruction());

		assertEquals(first, second);
		assertEquals(second, third);
		assertEquals(firstXml, secondXml);
		assertEquals(secondXml, getApp().getXML());
	}

	private void assertCanonicalPoint() {
		SpatialSemanticRuntime runtime = semanticRuntime();
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(V2_SYSTEM)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate point =
				runtime.getSpatialPointCertificate(V2_OBJECT);
		assertNotNull(point);
		assertTrue(point.isCurrentRevision());
		assertEquals(SpatialCertificateStatus.VALID,
				point.getSemanticCertificate().getStatus());
		Vector3 value = point.getSemanticCertificate().getPoint().orElseThrow();
		assertEquals(2, value.getX(), 1e-9);
		assertEquals(3, value.getY(), 1e-9);
		assertEquals(5, value.getZ(), 1e-9);
		assertTrue(runtime.getDerivedPoint(V2_OBJECT).isDefined());
	}

	private void assertMissingBindingReferenceState() {
		assertNull(registry().getRecord(V2_MISSING_BINDING));
		SpatialRecordResolution resolution = registry().getResolution(V2_OBJECT);
		assertNotNull(resolution);
		assertEquals(SpatialResolutionState.BROKEN, resolution.getState());
		assertTrue(resolution.getDiagnostics().stream()
				.anyMatch(diagnostic -> diagnostic.getCode()
						== SpatialIdentityDiagnostic.Code.MISSING_REFERENCE
						&& V2_MISSING_BINDING.equals(diagnostic.getReference())));
		SpatialPointPilotCertificate point =
				semanticRuntime().getSpatialPointCertificate(V2_OBJECT);
		assertTrue(point == null || !point.getSemanticCertificate().hasPayload());
		assertTrue(semanticRuntime().getDerivedPoint(V2_OBJECT) == null
				|| !semanticRuntime().getDerivedPoint(V2_OBJECT).isDefined());
	}

	private void assertFailedLoadPreservesGraph(String xml,
			SpatialIdentityDiagnostic.Code expected,
			G9A3SpatialGraphSnapshot.Snapshot before) {
		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> getApp().getXMLio().processXMLString(
						xml, true, false, false));

		assertEquals(expected, failure.getDiagnostic().getCode());
		assertEquals(before, G9A3SpatialGraphSnapshot.capture(getConstruction()));
		assertFalse(getConstruction().isFileLoading());
	}

	private static void assertStrictRecordRejection(String elementName,
			Map<String, String> attributes) {
		SpatialIdentityRegistry registry = new SpatialIdentityRegistry();
		SpatialIdentityRegistry.LoadSession session = registry.beginLoadSession(
				SpatialIdentityRegistry.LoadPurpose.NATIVE_OR_UNDO_RESTORE);

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> session.stageRecord(elementName, attributes));

		assertEquals(SpatialIdentityDiagnostic.Code.MALFORMED_RECORD,
				failure.getDiagnostic().getCode());
		assertTrue(registry.isEmpty());
	}

	private void stageClosure(SpatialIdentityRegistry.LoadSession session,
			List<SpatialIdentityRecord> records, String labelPrefix) {
		int geoIndex = 0;
		for (SpatialIdentityRecord record : records) {
			session.stageRecord(record);
			if (record instanceof GeoIdentityRecord) {
				GeoElement target = add(labelPrefix + geoIndex + "="
						+ (geoIndex + 1));
				session.stageGeoAttachment(target, (PersistentGeoId) record.getId());
				geoIndex++;
			}
		}
	}

	private void assertForbiddenAssociationCountersRemainZero() {
		assertEquals(0, registry().getInstrumentation().getLabelAuthorityUses());
		assertEquals(0, registry().getInstrumentation().getCoordinateAuthorityUses());
		assertEquals(0,
				registry().getInstrumentation().getConstructionOrderAuthorityUses());
		assertEquals(0, registry().getInstrumentation().getXmlPositionAuthorityUses());
		assertEquals(0, semanticRuntime().getInstrumentation()
				.getLabelFallbackLookups());
		assertEquals(0, semanticRuntime().getInstrumentation()
				.getCoordinateAssociationAttempts());
		assertEquals(0, semanticRuntime().getInstrumentation()
				.getCreationOrderAssociationAttempts());
		assertEquals(0, semanticRuntime().getInstrumentation()
				.getXmlPositionAssociationAttempts());
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private SpatialSemanticRuntime semanticRuntime() {
		return getConstruction().getSpatialSemanticRuntime();
	}

	private void loadFixture(String group, String name) throws IOException,
			XMLParseException {
		getApp().getXMLio().processXMLString(
				readFixture(group, name), true, false, false);
	}

	private static String readFixture(String group, String name) throws IOException {
		Path path = Paths.get("src/test/resources/org/geocedg/common/spatial",
				group, name);
		return Files.readString(path, StandardCharsets.UTF_8);
	}

	private static Document parseFixture(String group, String name)
			throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setExpandEntityReferences(false);
		return factory.newDocumentBuilder()
				.parse(Paths.get("src/test/resources/org/geocedg/common/spatial",
						group, name).toFile());
	}

	private static List<Element> elements(Document document, String tagName) {
		NodeList nodes = document.getElementsByTagName(tagName);
		List<Element> result = new ArrayList<>();
		for (int index = 0; index < nodes.getLength(); index++) {
			result.add((Element) nodes.item(index));
		}
		return result;
	}

	private static Map<String, String> attributes(Element element) {
		Map<String, String> result = new LinkedHashMap<>();
		NamedNodeMap attributes = element.getAttributes();
		for (int index = 0; index < attributes.getLength(); index++) {
			Node attribute = attributes.item(index);
			result.put(attribute.getNodeName(), attribute.getNodeValue());
		}
		return result;
	}
}
