/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityDiagnostic;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
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
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.junit.jupiter.api.Test;

class G9A2SpatialSemanticHostXmlTest extends BaseUnitTest {
	private static final ProjectionSystemId V1_SYSTEM = new ProjectionSystemId(
			"00000000000000000000000000000005");
	private static final SpatialObjectId V1_OBJECT = new SpatialObjectId(
			"00000000000000000000000000000003");
	private static final ProjectionSystemId CANONICAL_SYSTEM =
			new ProjectionSystemId("00000000000000000000000000000021");
	private static final SpatialObjectId CANONICAL_OBJECT =
			new SpatialObjectId("00000000000000000000000000000061");

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D();
	}

	@Test
	void versionOneClosureLoadsInertWithoutRuntimeAssociation() throws Exception {
		loadFixture("v1-inert-point-closure.xml");

		assertEquals(10,
				getConstruction().getSpatialIdentityRegistry().getRecords().size());
		assertNull(getConstruction().getSpatialSemanticRuntime()
				.getProjectionSystemCertificate(V1_SYSTEM));
		assertNull(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(V1_OBJECT));
		assertNull(getConstruction().getSpatialSemanticRuntime()
				.getDerivedPoint(V1_OBJECT));
		assertEquals(0, getConstruction().getSpatialSemanticRuntime()
				.getInstrumentation().getReconstructionAttempts());
	}

	@Test
	void completeVersionTwoHingeGraphReopensByteStableWithExactIds()
			throws Exception {
		final G9A2SpatialSemanticRuntimeTest.Graph graph =
				G9A2SpatialSemanticRuntimeTest.Graph.createWithHinge(
						getConstruction(), this::add, false);
		String saved = getApp().getXML();

		assertThat(saved, containsString("semanticVersion=\"2\""));
		assertThat(saved, containsString("supportStart=\"geo:"));
		assertThat(saved, containsString("supportEnd=\"geo:"));
		assertThat(saved, containsString("orientation=\"POSITIVE\""));
		assertThat(saved,
				containsString("provenance=\"EXPLICIT_CONSTRUCTION\""));
		assertThat(saved, containsString("foldSign=\"geo:"));

		getApp().getXMLio().processXMLString(saved, true, false, false);

		assertEquals(SpatialCertificateStatus.VALID,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertEquals(graph.objectId,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId).getObjectId());
		assertEquals(graph.systemId,
				getConstruction().getSpatialSemanticRuntime()
						.getProjectionSystemCertificate(graph.systemId).getSystemId());
		assertTrue(getConstruction().getSpatialSemanticRuntime()
				.getDerivedPoint(graph.objectId).isDefined());
		assertEquals(saved, getApp().getXML());
	}

	@Test
	void unknownVersionAndUnknownAttributeFailAtomically() throws Exception {
		assertNativeLoadFailure("v2-malformed-unknown-attribute.xml",
				SpatialIdentityDiagnostic.Code.MALFORMED_RECORD);
		assertNativeLoadFailure("v2-unknown-record-version.xml",
				SpatialIdentityDiagnostic.Code.UNSUPPORTED_VERSION);
	}

	@Test
	void reopenedCurrentFailuresRemainTypedAndCarryNoDerivedPayload()
			throws Exception {
		G9A2SpatialSemanticRuntimeTest.Graph graph =
				G9A2SpatialSemanticRuntimeTest.Graph.create(getConstruction(), this::add);
		graph.setVerticalProjection(9, 5);
		getKernel().updateConstruction(false);
		String inconsistentXml = getApp().getXML();

		getApp().getXMLio().processXMLString(inconsistentXml, true, false, false);

		assertEquals(SpatialCertificateStatus.INCONSISTENT_PROJECTIONS,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertFalse(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId)
				.getSemanticCertificate().hasPayload());
		assertFalse(getConstruction().getSpatialSemanticRuntime()
				.getDerivedPoint(graph.objectId).isDefined());

		GeoPointND vertical = (GeoPointND) lookup("G9A2PV");
		vertical.setCoords(2, 3, 1);
		((GeoElement) vertical).updateRepaint();
		GeoNumeric axis = (GeoNumeric) lookup("G9A2Axis");
		axis.setValue(1);
		axis.updateRepaint();
		getKernel().updateConstruction(false);
		String underdeterminedXml = getApp().getXML();

		getApp().getXMLio().processXMLString(underdeterminedXml, true, false, false);

		assertEquals(SpatialCertificateStatus.UNDERDETERMINED,
				getConstruction().getSpatialSemanticRuntime()
						.getSpatialPointCertificate(graph.objectId)
						.getSemanticCertificate().getStatus());
		assertFalse(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(graph.objectId)
				.getSemanticCertificate().hasPayload());
		assertFalse(getConstruction().getSpatialSemanticRuntime()
				.getDerivedPoint(graph.objectId).isDefined());
	}

	@Test
	void canonicalGgbLoadsAndReopensAsTheCertifiedThreeMapPointPilot()
			throws Exception {
		Path repository = findRepositoryRoot();
		Path model = repository.resolve(
				"models/regression/g9a2-spatial-point-pilot/"
						+ "g9a2-spatial-point-pilot.ggb");
		String decimalReference = Files.readString(repository.resolve(
				"geocedg/validation/spatial/g9a2/point-reference-values.json"),
				StandardCharsets.UTF_8);
		assertThat(decimalReference,
				containsString("\"id\": \"general-two-frame\""));
		assertThat(decimalReference,
				containsString("\"id\": \"below-relative-threshold\""));
		assertThat(decimalReference,
				containsString("\"parameterT\": \"1E-12\""));
		assertThat(decimalReference,
				containsString("\"id\": \"above-relative-threshold\""));
		assertThat(decimalReference,
				containsString("\"parameterT\": \"8E-12\""));
		getApp().setXML(readConstructionXml(model), true);

		assertCanonicalPointPilot();
		String saved = getApp().getXML();
		getApp().getXMLio().processXMLString(saved, true, false, false);

		assertCanonicalPointPilot();
		assertEquals(saved, getApp().getXML());
	}

	private void assertCanonicalPointPilot() {
		SpatialSemanticRuntime runtime = getConstruction().getSpatialSemanticRuntime();
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(CANONICAL_SYSTEM)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate point = runtime.getSpatialPointCertificate(
				CANONICAL_OBJECT);
		assertNotNull(point);
		assertEquals(CANONICAL_OBJECT, point.getObjectId());
		assertEquals(CANONICAL_SYSTEM, point.getSystemId());
		assertEquals(CANONICAL_SYSTEM,
				runtime.getProjectionSystemCertificate(CANONICAL_SYSTEM).getSystemId());
		assertNotNull(getConstruction().getSpatialIdentityRegistry()
				.getRecord(CANONICAL_SYSTEM));
		assertNotNull(getConstruction().getSpatialIdentityRegistry()
				.getRecord(CANONICAL_OBJECT));
		assertTrue(point.isCurrentRevision());
		assertEquals(SpatialCertificateStatus.VALID,
				point.getSemanticCertificate().getStatus());
		Vector3 value = point.getSemanticCertificate().getPoint().orElseThrow();
		assertEquals(2, value.getX(), 1e-9);
		assertEquals(3, value.getY(), 1e-9);
		assertEquals(5, value.getZ(), 1e-9);
		assertEquals(2, runtime.getDerivedPoint(CANONICAL_OBJECT).getInhomX(), 1e-9);
		assertEquals(3, runtime.getDerivedPoint(CANONICAL_OBJECT).getInhomY(), 1e-9);
		assertEquals(5, runtime.getDerivedPoint(CANONICAL_OBJECT).getInhomZ(), 1e-9);
	}

	private void assertNativeLoadFailure(String name,
			SpatialIdentityDiagnostic.Code expectedCode) throws Exception {
		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> loadFixture(name));

		assertEquals(expectedCode, failure.getDiagnostic().getCode());
		assertTrue(getConstruction().getSpatialIdentityRegistry().isEmpty());
		assertNull(getConstruction().getSpatialSemanticRuntime()
				.getSpatialPointCertificate(V1_OBJECT));
		assertFalse(getConstruction().isFileLoading());
	}

	private void loadFixture(String name) throws IOException, XMLParseException {
		getApp().getXMLio().processXMLString(readFixture(name), true, false, false);
	}

	private static String readFixture(String name) throws IOException {
		Path path = Paths.get("src/test/resources/org/geocedg/common/spatial/g9a2",
				name);
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}

	private static String readConstructionXml(Path model) throws IOException {
		try (ZipFile archive = new ZipFile(model.toFile())) {
			ZipEntry entry = archive.getEntry("geogebra.xml");
			assertNotNull(entry, "geogebra.xml is missing from " + model);
			try (InputStream input = archive.getInputStream(entry)) {
				return new String(input.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
	}

	private static Path findRepositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))
					&& Files.isDirectory(candidate.resolve("models"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		return fail("Could not resolve the GeoCeDG repository root.");
	}
}
