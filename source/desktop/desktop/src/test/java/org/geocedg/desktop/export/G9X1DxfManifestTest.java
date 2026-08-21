/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.export.ApproximationEvidence;
import org.geocedg.common.export.ApproximationEvidence.Guarantee;
import org.geocedg.common.export.DxfEncodingResult;
import org.geocedg.common.export.G9X1GeometryExportAdapter;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.export.SourceExportOutcome;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geocedg.desktop.export.DxfPairedOutputWriter.CollisionPolicy;
import org.geocedg.desktop.export.DxfWriteException.Stage;
import org.geocedg.desktop.export.GeoCeDGBuildProvenance.RepositoryState;
import org.geocedg.desktop.export.GeoCeDGBuildProvenance.ResolutionSource;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class G9X1DxfManifestTest extends BaseUnitTest {

	private static final String REPOSITORY_COMMIT =
			"0123456789abcdef0123456789abcdef01234567";
	private static final GeoCeDGBuildProvenance PROVENANCE =
			new GeoCeDGBuildProvenance("test-1.0", REPOSITORY_COMMIT,
					RepositoryState.DIRTY, ResolutionSource.GRADLE_PROPERTY);
	private final GeometryExportService service = new GeometryExportService();
	private final DxfFidelityManifestWriter writer =
			new DxfFidelityManifestWriter(PROVENANCE);
	@TempDir
	Path temporaryDirectory;

	@Test
	void m01EmitsVersionedBuildAndDxfProvenance() { // X1-M01
		Prepared prepared = prepare(add("f(x)=x^2"), approximateRequest(false));
		String manifest = manifest(prepared.output);

		assertTrue(manifest.startsWith(
				"{\"schema\":\"org.geocedg.dxf.fidelity-manifest\","));
		assertContains(manifest, "\"schema_version\":1");
		assertContains(manifest, "\"name\":\"GeoCeDG\"");
		assertContains(manifest, "\"version\":\"test-1.0\"");
		assertContains(manifest,
				"\"repository_commit\":\"" + REPOSITORY_COMMIT + "\"");
		assertContains(manifest, "\"repository_state\":\"dirty\"");
		assertContains(manifest, "\"provenance_source\":\"gradle_property\"");
		assertContains(manifest, "\"acad_version\":\"AC1015\"");
		assertContains(manifest, "\"encoding\":\"US-ASCII\"");
		assertContains(manifest, "\"line_ending\":\"CRLF\"");
		assertContains(manifest,
				"\"coordinate_system\":\"GEOGEBRA_CARTESIAN_2D_WORLD\"");
		assertContains(manifest, "\"source_unit\":\"unitless\"");
		assertContains(manifest, "\"target_unit\":\"unitless\"");
		GeoCeDGBuildProvenance unavailable = new GeoCeDGBuildProvenance(
				"test-1.0", "UNAVAILABLE", RepositoryState.UNAVAILABLE,
				ResolutionSource.UNAVAILABLE);
		assertThrows(IllegalStateException.class,
				() -> new DxfFidelityManifestWriter(unavailable).prepare(
						prepared.preflight, prepared.encoding));
	}

	@Test
	void m02DisclosesPersistentOrConstructionRevisionIdentityScope() { // X1-M02
		Prepared prepared = prepare(add("f(x)=x^2"), approximateRequest(false));
		String manifest = manifest(prepared.output);

		for (SourceExportOutcome outcome : prepared.preflight.getModel()
				.getOutcomes()) {
			String scope = outcome.getIdentityScope()
					== SourceExportOutcome.IdentityScope.PERSISTENT
							? "persistent" : "construction-revision";
			assertContains(manifest, "\"source_id\":\""
					+ outcome.getSourceId() + "\",\"id_scope\":\""
					+ scope + "\"");
		}
	}

	@Test
	void m03MapsNeutralEntitiesToActualDxfHandlesAndTypes() { // X1-M03
		GeoPoint point = (GeoPoint) add("A=(1,2)");
		GeoElement function = add("f(x)=x^2");
		GeometryExportRequest request = approximateRequest(false);
		Prepared prepared = prepare(Arrays.asList(point, function),
				request);
		String manifest = manifest(prepared.output);

		assertEquals(2, prepared.encoding.getEncodedEntities().size());
		for (SourceExportOutcome outcome : prepared.preflight.getModel()
				.getOutcomes()) {
			DxfEncodingResult.EntityEncoding actual = prepared.encoding
					.getEncoding(outcome.getNeutralEntityId());
			assertNotNull(actual);
			assertContains(manifest, "\"neutral_entity_id\":\""
					+ outcome.getNeutralEntityId() + "\"");
			assertContains(manifest,
					"\"dxf_handle\":\"" + actual.getHandle() + "\"");
			assertContains(manifest, "\"dxf_entity_type\":\""
					+ actual.getEntityType() + "\"");
			assertContains(prepared.encoding.getDxfText(),
					"\r\n5\r\n" + actual.getHandle() + "\r\n");
		}

		point.setCoords(9, 8, 1);
		point.updateCascade();
		GeometryExportPreflight changedPreflight = service.preflight(
				Arrays.asList(point, function), SelectionMode.CURRENT_SELECTION,
				request);
		DxfEncodingResult changedEncoding = service.encode(changedPreflight);
		assertEquals(prepared.encoding.getEncodedEntities().keySet(),
				changedEncoding.getEncodedEntities().keySet());
		for (String neutralId : prepared.encoding.getEncodedEntities().keySet()) {
			assertEquals(prepared.encoding.getEncoding(neutralId).getEntityType(),
					changedEncoding.getEncoding(neutralId).getEntityType());
		}
		assertFalse(prepared.encoding.getDxfText()
				.equals(changedEncoding.getDxfText()));
		assertThrows(IllegalArgumentException.class,
				() -> writer.prepare(changedPreflight, prepared.encoding));
		assertTrue(writer.prepare(changedPreflight, changedEncoding).hasManifest());
	}

	@Test
	void m04RecordsTheCompleteDeterministicRequestPolicy() { // X1-M04
		GeoElement source = add("f(x)=x^2");
		String sourceId = G9X1GeometryExportAdapter.requestSourceId(source, 0);
		GeometryExportRequest request = GeometryExportRequest.builder(0.125)
				.allowedGuarantees(EnumSet.of(Guarantee.ESTIMATED_ERROR,
						Guarantee.FLOATING_POINT_UNCERTIFIED))
				.addDefaultSemanticDomain(new SemanticDomain("curve-main",
						"default-domain", -2, 3, true, false))
				.addSourceSemanticDomain(sourceId, new SemanticDomain(
						"function", "source-domain", 7, 5, true, true))
				.maximumEvaluations(321)
				.maximumDepth(9)
				.maximumVerticesPerComponent(123)
				.maximumTotalVertices(456)
				.allowApproximation(true)
				.allowPartialOutput(false)
				.requestSidecar(true)
				.build();
		Prepared prepared = prepare(source, request);
		String manifest = manifest(prepared.output);

		assertContains(manifest, "\"requested_tolerance\":0.125");
		assertContains(manifest, "\"allowed_guarantees\":["
				+ "\"estimated_error\",\"floating_point_uncertified\"]");
		assertContains(manifest, "\"approximation_allowed\":true");
		assertContains(manifest, "\"partial_output_allowed\":false");
		assertContains(manifest, "\"sidecar_requested\":true");
		assertContains(manifest, "\"maximum_evaluations\":321");
		assertContains(manifest, "\"maximum_depth\":9");
		assertContains(manifest, "\"maximum_vertices_per_component\":123");
		assertContains(manifest, "\"maximum_total_vertices\":456");
		assertContains(manifest, "\"branch_key\":\"curve-main\"");
		assertContains(manifest, "\"key\":\"default-domain\"");
		assertContains(manifest, "\"branch_key\":\"function\"");
		assertContains(manifest, "\"key\":\"source-domain\"");
		assertContains(manifest, "\"start_parameter\":7.0");
		assertContains(manifest, "\"end_parameter\":5.0");
		assertContains(manifest, "\"increasing\":false");
	}

	@Test
	void m05RecordsApproximationMethodAndWorkEvidence() { // X1-M05
		Prepared prepared = prepare(add("f(x)=x^2"), approximateRequest(false));
		String manifest = manifest(prepared.output);
		SourceExportOutcome outcome = prepared.preflight.getModel().getOutcomes()
				.get(0);
		ApproximationEvidence evidence = outcome.getApproximationEvidence();

		assertEquals(Fidelity.APPROXIMATE, outcome.getFidelity());
		assertNotNull(evidence);
		assertContains(manifest,
				"\"method\":\"oriented_dyadic_refinement\"");
		assertContains(manifest, "\"requested_tolerance\":"
				+ evidence.getRequestedTolerance());
		assertContains(manifest,
				"\"achieved_error\":" + evidence.getAchievedError());
		assertContains(manifest, "\"guarantee\":\"estimated_error\"");
		assertContains(manifest,
				"\"evaluations\":" + evidence.getEvaluations());
		assertContains(manifest,
				"\"subdivisions\":" + evidence.getSubdivisions());
		assertContains(manifest, "\"segments\":" + evidence.getSegments());
		assertContains(manifest, "\"vertices\":" + evidence.getVertices());
		assertContains(manifest,
				"\"maximum_depth\":" + evidence.getMaximumDepth());
	}

	@Test
	void m06RecordsStructuredWarningsAndEveryOmission() { // X1-M06
		GeoElement hiddenFunction = add("f(x)=x^2");
		hiddenFunction.setEuclidianVisible(false);
		Prepared prepared = prepare(hiddenFunction, approximateRequest(false));
		String manifest = manifest(prepared.output);

		assertContains(manifest, "\"code\":\"approximate_geometry\"");
		assertContains(manifest, "\"code\":\"hidden_source_included\"");
		assertContains(manifest, "\"visible\":false");
		SourceExportOutcome approximate = prepared.preflight.getModel()
				.getOutcomes().get(0);
		assertContains(manifest, "\"code\":\"approximate_geometry\","
				+ "\"source_id\":\"" + approximate.getSourceId() + "\","
				+ "\"branch_key\":\"function\"");

		GeometryExportPreflight rejected = service.preflight(
				Collections.singletonList(add("n=1")),
				SelectionMode.CURRENT_SELECTION, approximateRequest(false));
		assertFalse(rejected.isWritable());
		assertEquals(1, rejected.getOmittedCount());
		assertFalse(rejected.getModel().getOutcomes().get(0).isEmitted());
		assertNotNull(rejected.getModel().getOutcomes().get(0).getReason());
		assertThrows(IllegalStateException.class,
				() -> writer.prepare(rejected, prepared.encoding));
	}

	@Test
	void m07BindsTheManifestToTheDxfSha256() { // X1-M07
		Prepared prepared = prepare(add("f(x)=x^2"), approximateRequest(false));
		String manifest = manifest(prepared.output);
		String expectedHash = DxfHashing.sha256(prepared.output.getDxfBytes());

		assertEquals(expectedHash, prepared.output.getDxfSha256());
		assertEquals(expectedHash,
				prepared.output.getManifest().getDxfSha256());
		assertContains(manifest, "\"sha256\":\"" + expectedHash + "\"");
		byte[] changedDxf = prepared.output.getDxfBytes();
		changedDxf[0] = changedDxf[0] == '0' ? (byte) '1' : (byte) '0';
		assertThrows(IllegalArgumentException.class, () -> DxfPreparedOutput
				.paired(changedDxf, prepared.output.getManifest()));
	}

	@Test
	void m08SerializesDxfAndManifestDeterministically() { // X1-M08
		GeoElement source = add("f(x)=x^2");
		GeometryExportRequest request = approximateRequest(false);
		Prepared first = prepare(source, request);
		Prepared second = prepare(source, request);

		assertArrayEquals(first.output.getDxfBytes(), second.output.getDxfBytes());
		assertArrayEquals(first.output.getManifest().getBytes(),
				second.output.getManifest().getBytes());
		assertEquals(first.encoding.getEncodedEntities().keySet(),
				second.encoding.getEncodedEntities().keySet());
	}

	@Test
	void m09PermitsSidecarOmissionForWhollyExactOutput() { // X1-M09
		Prepared prepared = prepare(add("A=(1,2)"), exactRequest(false));

		assertTrue(prepared.preflight.isWritable());
		assertEquals(1, prepared.preflight.getExactCount());
		assertEquals(0, prepared.preflight.getApproximateCount());
		assertFalse(prepared.preflight.isSidecarRequired());
		assertFalse(prepared.output.hasManifest());
	}

	@Test
	void m10MakesSidecarMandatoryForFidelityReduction() throws Exception { // X1-M10
		GeoElement function = add("f(x)=x^2");
		Prepared prepared = prepare(function, approximateRequest(false));

		assertEquals(1, prepared.preflight.getApproximateCount());
		assertTrue(prepared.preflight.isSidecarRequired());
		assertTrue(prepared.output.hasManifest());
		assertNotNull(prepared.output.getManifest());

		add("publicationRevisionMarker=1");
		Path destination = temporaryDirectory.resolve("stale-publication.dxf");
		DxfWriteException stale = assertThrows(DxfWriteException.class,
				() -> new DxfPairedOutputWriter().write(destination,
						prepared.output, CollisionPolicy.FAIL_IF_EXISTS));
		assertEquals(Stage.VALIDATE_TEMPORARIES, stale.getStage());
		assertTrue(stale.isRollbackComplete());
		assertFalse(Files.exists(destination));
		assertFalse(Files.exists(DxfPairedOutputWriter.manifestPath(destination)));
		try (java.util.stream.Stream<Path> paths = Files.list(temporaryDirectory)) {
			assertFalse(paths.anyMatch(path -> path.getFileName().toString()
					.contains(".geocedg-")));
		}
	}

	private Prepared prepare(GeoElement source, GeometryExportRequest request) {
		return prepare(Collections.singletonList(source), request);
	}

	private Prepared prepare(List<GeoElement> sources,
			GeometryExportRequest request) {
		GeometryExportPreflight preflight = service.preflight(sources,
				SelectionMode.CURRENT_SELECTION, request);
		DxfEncodingResult encoding = service.encode(preflight);
		return new Prepared(preflight, encoding,
				writer.prepare(preflight, encoding));
	}

	private static GeometryExportRequest exactRequest(
			boolean sidecarRequested) {
		return GeometryExportRequest.builder(0.05)
				.allowApproximation(true)
				.allowPartialOutput(false)
				.requestSidecar(sidecarRequested)
				.build();
	}

	private static GeometryExportRequest approximateRequest(
			boolean sidecarRequested) {
		return GeometryExportRequest.builder(0.05)
				.addDefaultSemanticDomain(new SemanticDomain("bounded", -1, 1,
						true, true))
				.allowApproximation(true)
				.allowPartialOutput(false)
				.requestSidecar(sidecarRequested)
				.build();
	}

	private static String manifest(DxfPreparedOutput output) {
		assertTrue(output.hasManifest());
		String text = new String(output.getManifest().getBytes(),
				StandardCharsets.UTF_8);
		assertTrue(text.endsWith("\n"));
		assertFalse(text.contains("\r"));
		return text;
	}

	private static void assertContains(String actual, String expected) {
		assertTrue(actual.contains(expected), () -> "Missing: " + expected
				+ "\nActual: " + actual);
	}

	private static final class Prepared {
		private final GeometryExportPreflight preflight;
		private final DxfEncodingResult encoding;
		private final DxfPreparedOutput output;

		private Prepared(GeometryExportPreflight preflight,
				DxfEncodingResult encoding, DxfPreparedOutput output) {
			this.preflight = preflight;
			this.encoding = encoding;
			this.output = output;
		}
	}
}
