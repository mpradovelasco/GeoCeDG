/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.export.DxfEncodingResult.EntityEncoding;
import org.geocedg.common.export.GeometryExportModel.Diagnostic;
import org.geocedg.common.export.GeometryExportModel.DiagnosticCode;
import org.geocedg.common.export.GeometryExportModel.Entity;
import org.geocedg.common.export.GeometryExportModel.Exactness;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPolygon;
import org.geogebra.common.kernel.kernelND.GeoSegmentND;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.Test;

/** Executable compatibility authority for the real versioned G5 DXF corpus. */
class G9X1G5CorpusCompatibilityTest extends BaseUnitTest {

	private final GeometryExportService service = new GeometryExportService();

	// X1-C01
	@Test
	void c01ExecutesVersionedCorpusAndRetainsExpectedEntityOrder()
			throws Exception {
		Corpus corpus = loadCorpus();
		GeometryExportModel model = execute(corpus);
		JSONObject metrics = corpus.manifest.getJSONObject("expected_metrics");

		assertEquals(corpus.expected.getString("case_id"),
				corpus.manifest.getString("id"));
		assertEquals(corpus.expected.getInt("exact_entities"),
				metrics.getInt("exact_entities"));
		assertEquals(corpus.expected.getInt("unsupported_entities"),
				metrics.getInt("unsupported_entities"));
		assertEquals(corpus.expected.getInt("exact_entities"),
				model.getEntities().size());
		assertEquals(corpus.expected.getInt("unsupported_entities"),
				unsupportedDiagnostics(model));

		DxfDocument document = DxfDocument.parse(service.exportDxf(model));
		assertEquals(strings(corpus.expected.getJSONArray("entity_types")),
				document.entityTypes());
		assertEquals(corpus.expected.getJSONObject("format")
				.getString("acad_version"), document.headerValue("$ACADVER"));
		assertEquals(Integer.toString(corpus.expected.getJSONObject("format")
				.getInt("insunits")), document.headerValue("$INSUNITS"));
	}

	// X1-C02
	@Test
	void c02ReadsCorpusInvariantsAndRetainsExactG5Geometry() throws Exception {
		Corpus corpus = loadCorpus();
		GeometryExportModel model = execute(corpus);
		DxfDocument document = DxfDocument.parse(service.exportDxf(model));
		JSONObject invariants = corpus.expected.getJSONObject("invariants");
		JSONArray segmentEnd = invariants.getJSONArray("segment_end");

		assertEquals(segmentEnd.getDouble(0),
				document.entity("LINE").doubleValue(11), 0);
		assertEquals(segmentEnd.getDouble(1),
				document.entity("LINE").doubleValue(21), 0);
		assertEquals(invariants.getDouble("circle_radius"),
				document.entity("CIRCLE").doubleValue(40), 0);
		assertEquals(invariants.getInt("polygon_vertices"),
				document.entities("LWPOLYLINE").get(0).intValue(90));
		assertEquals(invariants.getBoolean("polygon_closed") ? 1 : 0,
				document.entities("LWPOLYLINE").get(0).intValue(70));
		assertEquals(invariants.getDouble("line_direction_norm"),
				directionNorm(document.entity("XLINE")), 1E-12);
		assertEquals(invariants.getDouble("ray_direction_norm"),
				directionNorm(document.entity("RAY")), 1E-12);
		assertEquals(corpus.expected.getString("selection_mode"),
				model.getSelectionMode().name());
	}

	// X1-C03
	@Test
	void c03LegacyApiRetainsExactBytesAcrossDeterministicRerun()
			throws Exception {
		Corpus corpus = loadCorpus();
		GeometryExportModel model = execute(corpus);
		String legacyBytes = service.exportDxf(model);
		String directBytes = new DxfExporter().export(model);
		DxfEncodingResult encoded = new DxfExporter().encode(model);

		assertEquals(1, model.getModelVersion());
		assertTrue(model.getOutcomes().stream()
				.allMatch(outcome -> outcome.getFidelity() == Fidelity.EXACT));
		assertArrayEquals(legacyBytes.getBytes(StandardCharsets.UTF_8),
				directBytes.getBytes(StandardCharsets.UTF_8));
		assertArrayEquals(legacyBytes.getBytes(StandardCharsets.UTF_8),
				encoded.getDxfText().getBytes(StandardCharsets.UTF_8));

		Entity exactEntity = model.getEntities().get(0);
		GeometryExportModel approximateLegacyModel = new GeometryExportModel(
				model.getSelectionMode(), List.of(new Entity(
						exactEntity.getSourceId(), exactEntity.getSourceType(),
						exactEntity.getLabel(), exactEntity.getLayer(),
						exactEntity.getStyle(), Exactness.APPROXIMATE, 0.01,
						exactEntity.getGeometry())), List.of());
		try {
			service.exportDxf(approximateLegacyModel);
			fail("Legacy API must reject approximate entities without preflight");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage().contains("controlled preflight"));
		}

		assertTrue(corpus.expected.getJSONObject("invariants")
				.getBoolean("zoom_invariant"));
		getApp().getActiveEuclidianView().setCoordSystem(411, 263, 91, 37);
		String rerunBytes = service.exportDxf(service.createModel(corpus.sources,
				SelectionMode.CURRENT_SELECTION));
		assertArrayEquals(legacyBytes.getBytes(StandardCharsets.UTF_8),
				rerunBytes.getBytes(StandardCharsets.UTF_8));
		assertTrue(legacyBytes.endsWith("\r\n"));
		assertFalse(legacyBytes.replace("\r\n", "").contains("\n"));
	}

	// X1-C04
	@Test
	void c04EncodingResultRetainsCorpusOrderAndActualG5Handles()
			throws Exception {
		Corpus corpus = loadCorpus();
		GeometryExportModel model = execute(corpus);
		DxfEncodingResult encoded = new DxfExporter().encode(model);
		DxfDocument document = DxfDocument.parse(encoded.getDxfText());
		List<String> expectedTypes = strings(
				corpus.expected.getJSONArray("entity_types"));
		List<String> neutralIds = new ArrayList<>();
		for (Entity entity : model.getEntities()) {
			neutralIds.add(entity.getNeutralEntityId());
		}

		assertEquals(neutralIds,
				new ArrayList<>(encoded.getEncodedEntities().keySet()));
		assertEquals(neutralIds.size(), document.entities.size());
		for (int index = 0; index < neutralIds.size(); index++) {
			String neutralId = neutralIds.get(index);
			EntityEncoding mapping = encoded.getEncoding(neutralId);
			String expectedHandle = Integer.toHexString(0x100 + index)
					.toUpperCase();
			assertNotNull(mapping);
			assertEquals(expectedHandle, mapping.getHandle());
			assertEquals(expectedTypes.get(index), mapping.getEntityType());
			assertEquals(expectedHandle, document.entities.get(index).value(5));
			assertEquals(expectedTypes.get(index),
					document.entities.get(index).value(0));
		}
		assertEquals(neutralIds.size(), encoded.getEncodedEntities().values()
				.stream().map(EntityEncoding::getHandle).distinct().count());

		GeoPolygon polygon = (GeoPolygon) add(
				"compatibilityPolygon=Polygon((0,0),(2,0),(0,1))");
		List<GeoElement> polygonPopulation = new ArrayList<>();
		polygonPopulation.add(polygon);
		for (GeoSegmentND segment : polygon.getSegments()) {
			polygonPopulation.add(segment.toGeoElement());
		}
		GeometryExportPreflight polygonPreflight = service.preflight(
				polygonPopulation, SelectionMode.COMPLETE_CONSTRUCTION,
				GeometryExportRequest.builder(0.01).build());
		assertTrue(polygonPreflight.isWritable());
		assertEquals(1, polygonPreflight.getExactCount());
		assertEquals(0, polygonPreflight.getOmittedCount());
		assertEquals(polygon.getSegments().length,
				polygonPreflight.getModel().getDiagnostics().stream()
						.filter(item -> item.getCode()
								== DiagnosticCode.DUPLICATE_POLYGON_SIDE)
						.count());
	}

	private GeometryExportModel execute(Corpus corpus) throws Exception {
		for (String command : corpus.commands) {
			GeoElement source = add(command);
			assertNotNull(source, "Corpus command produced no source: " + command);
			corpus.sources.add(source);
		}
		int expectedSources = corpus.expected.getInt("exact_entities")
				+ corpus.expected.getInt("unsupported_entities");
		assertEquals(expectedSources, corpus.sources.size());
		return service.createModel(corpus.sources, SelectionMode.CURRENT_SELECTION);
	}

	private static Corpus loadCorpus() throws Exception {
		Path repository = findRepositoryRoot();
		Path manifestPath = repository.resolve(
				"models/regression/g5-dxf-foundation/manifest.yml");
		JSONObject manifest = new JSONObject(
				Files.readString(manifestPath, StandardCharsets.UTF_8));
		Path construction = repository.resolve(manifest.getJSONObject("source")
				.getString("uri")).normalize();
		Path expectedPath = repository.resolve(manifest
				.getJSONArray("derived_artifacts").getJSONObject(0)
				.getString("path")).normalize();
		JSONObject expected = new JSONObject(
				Files.readString(expectedPath, StandardCharsets.UTF_8));
		List<String> commands = new ArrayList<>();
		for (String line : Files.readAllLines(construction, StandardCharsets.UTF_8)) {
			String command = line.trim();
			if (!command.isEmpty() && !command.startsWith("#")
					&& !command.startsWith("//")) {
				commands.add(command);
			}
		}
		assertFalse(commands.isEmpty(), "The executable G5 corpus is empty");
		return new Corpus(manifest, expected, commands);
	}

	private static int unsupportedDiagnostics(GeometryExportModel model) {
		int unsupported = 0;
		for (Diagnostic diagnostic : model.getDiagnostics()) {
			if (diagnostic.getCode() == DiagnosticCode.UNSUPPORTED) {
				unsupported++;
			}
		}
		return unsupported;
	}

	private static List<String> strings(JSONArray array) throws Exception {
		List<String> values = new ArrayList<>();
		for (int index = 0; index < array.length(); index++) {
			values.add(array.getString(index));
		}
		return values;
	}

	private static double directionNorm(DxfEntity entity) {
		return Math.hypot(entity.doubleValue(11), entity.doubleValue(21));
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

	private static final class Corpus {
		private final JSONObject manifest;
		private final JSONObject expected;
		private final List<String> commands;
		private final List<GeoElement> sources = new ArrayList<>();

		private Corpus(JSONObject manifest, JSONObject expected,
				List<String> commands) {
			this.manifest = manifest;
			this.expected = expected;
			this.commands = commands;
		}
	}

	private static final class DxfDocument {
		private final List<DxfPair> pairs;
		private final List<DxfEntity> entities;

		private DxfDocument(List<DxfPair> pairs, List<DxfEntity> entities) {
			this.pairs = pairs;
			this.entities = entities;
		}

		private static DxfDocument parse(String text) {
			String[] lines = text.split("\\r?\\n");
			assertEquals(0, lines.length % 2);
			List<DxfPair> pairs = new ArrayList<>();
			for (int index = 0; index < lines.length; index += 2) {
				pairs.add(new DxfPair(Integer.parseInt(lines[index]),
						lines[index + 1]));
			}
			List<DxfEntity> entities = new ArrayList<>();
			boolean inEntities = false;
			List<DxfPair> current = null;
			for (int index = 0; index < pairs.size(); index++) {
				DxfPair pair = pairs.get(index);
				if (pair.code == 0 && "SECTION".equals(pair.value)
						&& index + 1 < pairs.size()
						&& "ENTITIES".equals(pairs.get(index + 1).value)) {
					inEntities = true;
					index++;
					continue;
				}
				if (inEntities && pair.code == 0 && "ENDSEC".equals(pair.value)) {
					if (current != null) {
						entities.add(new DxfEntity(current));
					}
					break;
				}
				if (inEntities && pair.code == 0) {
					if (current != null) {
						entities.add(new DxfEntity(current));
					}
					current = new ArrayList<>();
				}
				if (inEntities && current != null) {
					current.add(pair);
				}
			}
			return new DxfDocument(pairs, entities);
		}

		private String headerValue(String variable) {
			for (int index = 0; index < pairs.size() - 1; index++) {
				if (pairs.get(index).code == 9
						&& variable.equals(pairs.get(index).value)) {
					return pairs.get(index + 1).value;
				}
			}
			throw new AssertionError("Missing header variable " + variable);
		}

		private List<String> entityTypes() {
			List<String> types = new ArrayList<>();
			for (DxfEntity entity : entities) {
				types.add(entity.value(0));
			}
			return types;
		}

		private DxfEntity entity(String type) {
			return entities(type).get(0);
		}

		private List<DxfEntity> entities(String type) {
			List<DxfEntity> result = new ArrayList<>();
			for (DxfEntity entity : entities) {
				if (type.equals(entity.value(0))) {
					result.add(entity);
				}
			}
			assertFalse(result.isEmpty(), "Missing DXF entity " + type);
			return result;
		}
	}

	private static final class DxfEntity {
		private final List<DxfPair> pairs;

		private DxfEntity(List<DxfPair> pairs) {
			this.pairs = pairs;
		}

		private String value(int code) {
			for (DxfPair pair : pairs) {
				if (pair.code == code) {
					return pair.value;
				}
			}
			throw new AssertionError("Missing entity group " + code);
		}

		private int intValue(int code) {
			return Integer.parseInt(value(code));
		}

		private double doubleValue(int code) {
			return Double.parseDouble(value(code));
		}
	}

	private static final class DxfPair {
		private final int code;
		private final String value;

		private DxfPair(int code, String value) {
			this.code = code;
			this.value = value;
		}
	}
}
