/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityInstrumentation;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.runtime.SpatialPointPilotCertificate;
import org.geocedg.common.kernel.spatial.runtime.SpatialSemanticRuntime;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.junit.jupiter.api.Test;

class G9A3SpatialNativeCompatibilityTest extends BaseUnitTest {
	private static final String CANONICAL_MODEL_SHA256 =
			"3f150eaf05731b3907b5ba3e653ec4666ca1dcc6f999f1b609de305b98b2a3be";
	private static final ProjectionSystemId CANONICAL_SYSTEM =
			new ProjectionSystemId("00000000000000000000000000000021");
	private static final SpatialObjectId CANONICAL_OBJECT =
			new SpatialObjectId("00000000000000000000000000000061");

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create3D(new AppConfigGeoCeDG());
	}

	@Test
	void compat01GeoCeDGLoadsRecomputesSavesAndReopensNativePointExactly()
			throws Exception {
		Path model = canonicalModel();
		assertEquals(CANONICAL_MODEL_SHA256, sha256(model));
		getApp().setXML(readConstructionXml(model), true);

		assertEquals(AppConfigGeoCeDG.APPLICATION_NAME,
				getApp().getConfig().getAppName());
		assertCanonicalPoint(getConstruction());
		G9A3SpatialGraphSnapshot.Snapshot before =
				G9A3SpatialGraphSnapshot.capture(getConstruction());
		String saved = getApp().getXML();

		getApp().getXMLio().processXMLString(saved, true, false, false);

		assertCanonicalPoint(getConstruction());
		assertEquals(before, G9A3SpatialGraphSnapshot.capture(getConstruction()));
		assertEquals(saved, getApp().getXML());
		assertEquals(CANONICAL_MODEL_SHA256, sha256(model));
	}

	@Test
	void compat02ForkClassicUsesSameKernelForExactNativeRoundTrip()
			throws Exception {
		String sourceXml = readConstructionXml(canonicalModel());
		getApp().setXML(sourceXml, true);
		G9A3SpatialGraphSnapshot.Snapshot geoCeDG =
				G9A3SpatialGraphSnapshot.capture(getConstruction());
		String geoCeDGSaved = getApp().getXML();

		AppCommon classic = AppCommonFactory.create3D(new AppConfigDefault());
		classic.setXML(sourceXml, true);
		Construction classicConstruction = classic.getKernel().getConstruction();
		assertCanonicalPoint(classicConstruction);
		assertEquals(geoCeDG,
				G9A3SpatialGraphSnapshot.capture(classicConstruction));
		assertEquals(geoCeDGSaved, classic.getXML());

		String classicSaved = classic.getXML();
		classic.getXMLio().processXMLString(classicSaved, true, false, false);
		assertCanonicalPoint(classicConstruction);
		assertEquals(geoCeDG,
				G9A3SpatialGraphSnapshot.capture(classicConstruction));
		assertEquals(classicSaved, classic.getXML());
	}

	@Test
	void compat03ClassicCreationStaysDisabledWhileNativeDataIsPreserved()
			throws Exception {
		Path repository = findRepositoryRoot();
		String featureManifest = Files.readString(
				repository.resolve("geocedg/features/experimental.yml"),
				StandardCharsets.UTF_8);
		int spatialFeature = featureManifest.indexOf(
				"\"id\": \"cedg.spatial.semantics\"");
		assertTrue(spatialFeature >= 0);
		int featureEnd = featureManifest.indexOf('}', spatialFeature);
		assertTrue(featureEnd > spatialFeature);
		assertThat(featureManifest.substring(spatialFeature, featureEnd),
				containsString("\"enabled_by_default\": false"));
		assertThrows(ClassNotFoundException.class, () -> Class.forName(
				"org.geocedg.common.kernel.spatial.commands.CmdSpatialPoint"));
		assertThrows(ClassNotFoundException.class, () -> Class.forName(
				"org.geocedg.common.kernel.spatial.commands."
						+ "CmdProjectionDefinedSpatialPoint"));

		AppCommon classic = AppCommonFactory.create3D(new AppConfigDefault());
		classic.setXML(readConstructionXml(canonicalModel()), true);
		Construction construction = classic.getKernel().getConstruction();
		G9A3SpatialGraphSnapshot.Snapshot before =
				G9A3SpatialGraphSnapshot.capture(construction);
		String saved = classic.getXML();
		classic.getXMLio().processXMLString(saved, true, false, false);

		assertEquals(before, G9A3SpatialGraphSnapshot.capture(construction));
		assertEquals(saved, classic.getXML());
		assertCanonicalPoint(construction);
	}

	@Test
	void compat04ExternalUpstreamBoundaryIsUnsupportedWithZeroDowngrade()
			throws Exception {
		Path repository = findRepositoryRoot();
		String corpus = Files.readString(repository.resolve(
				"docs/validation/g9a3_spatial_compatibility_corpus.json"),
				StandardCharsets.UTF_8);
		assertThat(corpus, containsString(
				"\"externalUpstreamRuntimeEvidence\": "
						+ "\"NOT_EXECUTED_OR_CLAIMED_BY_THIS_CORPUS\""));
		assertThat(corpus, containsString("\"lossyConversionAuthorized\": false"));
		assertThat(corpus, containsString(
				"PROJECT_AUTHORED_UNSUPPORTED_LOSS_SHAPE_NOT_EXTERNAL_RUNTIME_OUTPUT"));

		loadFixture("external-upstream-ordinary-control.xml");

		assertTrue(registry().isEmpty());
		assertNotNull(lookup("A"));
		assertNotNull(lookup("B"));
		assertNoConversionOrInference(registry(), semanticRuntime());
		assertThat(getApp().getXML(), not(containsString("geocedgSpatial")));
		assertThat(getApp().getXML(), not(containsString("geocedgId")));
	}

	@Test
	void compat05ForeignResaveWithoutSpatialSectionRemainsUnassociated()
			throws Exception {
		loadFixture("external-upstream-no-spatial-loss-shape.xml");

		List<String> labels = List.of("O", "X", "Ux", "Uy", "Uz", "PH", "PV");
		for (String label : labels) {
			GeoElement geo = lookup(label);
			assertNotNull(geo);
			assertNull(registry().getPersistentGeoId(geo));
		}
		assertTrue(registry().isEmpty());
		assertNoConversionOrInference(registry(), semanticRuntime());
		String saved = getApp().getXML();
		assertThat(saved, not(containsString("geocedgSpatial")));
		assertThat(saved, not(containsString("geocedgId")));

		getApp().getXMLio().processXMLString(saved, true, false, false);

		assertEquals(saved, getApp().getXML());
		assertTrue(registry().isEmpty());
		for (String label : labels) {
			GeoElement geo = lookup(label);
			assertNotNull(geo);
			assertNull(registry().getPersistentGeoId(geo));
		}
		assertNoConversionOrInference(registry(), semanticRuntime());
	}

	private static void assertCanonicalPoint(Construction construction) {
		SpatialSemanticRuntime runtime = construction.getSpatialSemanticRuntime();
		assertEquals(ProjectionSystemStatus.CONSISTENT,
				runtime.getProjectionSystemCertificate(CANONICAL_SYSTEM)
						.getSemanticCertificate().getStatus());
		SpatialPointPilotCertificate point =
				runtime.getSpatialPointCertificate(CANONICAL_OBJECT);
		assertNotNull(point);
		assertTrue(point.isCurrentRevision());
		assertEquals(SpatialCertificateStatus.VALID,
				point.getSemanticCertificate().getStatus());
		Vector3 value = point.getSemanticCertificate().getPoint().orElseThrow();
		assertEquals(2, value.getX(), 1e-9);
		assertEquals(3, value.getY(), 1e-9);
		assertEquals(5, value.getZ(), 1e-9);
		assertTrue(runtime.getDerivedPoint(CANONICAL_OBJECT).isDefined());
	}

	private static void assertNoConversionOrInference(
			SpatialIdentityRegistry registry, SpatialSemanticRuntime runtime) {
		SpatialIdentityInstrumentation identity = registry.getInstrumentation();
		assertEquals(0, identity.getAllocationAttempts());
		assertEquals(0, identity.getRemaps());
		assertEquals(0, identity.getExplicitMigrationCommits());
		assertEquals(0, identity.getLabelAuthorityUses());
		assertEquals(0, identity.getCoordinateAuthorityUses());
		assertEquals(0, identity.getConstructionOrderAuthorityUses());
		assertEquals(0, identity.getXmlPositionAuthorityUses());
		SpatialSemanticInstrumentation semantic = runtime.getInstrumentation();
		assertEquals(0, semantic.getReconstructionAttempts());
		assertEquals(0, semantic.getLabelFallbackLookups());
		assertEquals(0, semantic.getCoordinateAssociationAttempts());
		assertEquals(0, semantic.getCreationOrderAssociationAttempts());
		assertEquals(0, semantic.getXmlPositionAssociationAttempts());
	}

	private SpatialIdentityRegistry registry() {
		return getConstruction().getSpatialIdentityRegistry();
	}

	private SpatialSemanticRuntime semanticRuntime() {
		return getConstruction().getSpatialSemanticRuntime();
	}

	private void loadFixture(String name) throws Exception {
		getApp().getXMLio().processXMLString(Files.readString(Paths.get(
				"src/test/resources/org/geocedg/common/spatial/g9a3", name),
				StandardCharsets.UTF_8), true, false, false);
	}

	private static Path canonicalModel() {
		return findRepositoryRoot().resolve(
				"models/regression/g9a2-spatial-point-pilot/"
						+ "g9a2-spatial-point-pilot.ggb");
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

	private static String sha256(Path path) throws IOException {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256")
					.digest(Files.readAllBytes(path));
			StringBuilder result = new StringBuilder(hash.length * 2);
			for (byte value : hash) {
				result.append(String.format("%02x", value & 0xff));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is required by the JRE", exception);
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
