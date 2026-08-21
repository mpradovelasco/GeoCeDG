/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.geocedg.common.export.ApproximationEvidence;
import org.geocedg.common.export.ComponentAddress;
import org.geocedg.common.export.DxfEncodingResult;
import org.geocedg.common.export.DxfEncodingResult.EntityEncoding;
import org.geocedg.common.export.DxfExporter;
import org.geocedg.common.export.GeometryExportModel;
import org.geocedg.common.export.GeometryExportModel.Diagnostic;
import org.geocedg.common.export.GeometryExportModel.Entity;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportRequest.SemanticDomain;
import org.geocedg.common.export.SourceExportOutcome;
import org.geocedg.common.export.SourceExportOutcome.Fidelity;
import org.geocedg.common.export.SourceExportOutcome.IdentityScope;

/** Creates the deterministic fidelity manifest paired with G9X1 DXF output. */
public final class DxfFidelityManifestWriter {

	private static final String SCHEMA =
			"org.geocedg.dxf.fidelity-manifest";
	private static final int SCHEMA_VERSION = 1;
	private final GeoCeDGBuildProvenance provenance;

	/** Uses the immutable provenance resource embedded by the Desktop build. */
	public DxfFidelityManifestWriter() {
		this(GeoCeDGBuildProvenance.load());
	}

	/**
	 * @param provenance immutable build-time provenance; useful for deterministic
	 *        tests and managed packaging
	 */
	DxfFidelityManifestWriter(GeoCeDGBuildProvenance provenance) {
		this.provenance = Objects.requireNonNull(provenance);
	}

	/**
	 * Produces either exact-only bytes or a hash-bound DXF/manifest pair according
	 * to the completed preflight decision.
	 *
	 * @param preflight strict preflight completed before destination selection
	 * @param encoding deterministic DXF text and actual handle mappings
	 * @return artifacts prepared and validated in memory
	 */
	public DxfPreparedOutput prepare(GeometryExportPreflight preflight,
			DxfEncodingResult encoding) {
		validatePreflight(preflight);
		byte[] dxfBytes = encodeAsciiDxf(Objects.requireNonNull(encoding)
				.getDxfText());
		validateEncoding(preflight.getModel(), encoding);
		if (!preflight.isSidecarRequired()) {
			return DxfPreparedOutput.exact(dxfBytes, preflight);
		}
		DxfManifestEncoding manifest = encode(preflight, encoding, dxfBytes);
		return DxfPreparedOutput.paired(dxfBytes, manifest, preflight);
	}

	/**
	 * Serializes a required or explicitly requested manifest in canonical UTF-8
	 * with LF line termination.
	 *
	 * @param preflight completed preflight decision
	 * @param encoding deterministic DXF and actual handle mappings
	 * @return validated manifest bytes bound to the DXF hash
	 */
	public DxfManifestEncoding encode(GeometryExportPreflight preflight,
			DxfEncodingResult encoding) {
		validatePreflight(preflight);
		if (!preflight.isSidecarRequired()) {
			throw new IllegalStateException(
					"An exact request did not require or request a sidecar");
		}
		Objects.requireNonNull(encoding, "DXF encoding is required");
		byte[] dxfBytes = encodeAsciiDxf(encoding.getDxfText());
		validateEncoding(preflight.getModel(), encoding);
		return encode(preflight, encoding, dxfBytes);
	}

	private DxfManifestEncoding encode(GeometryExportPreflight preflight,
			DxfEncodingResult encoding, byte[] dxfBytes) {
		if (!provenance.isEstablished()) {
			throw new IllegalStateException(
					"Auditable build repository provenance is unavailable");
		}
		String dxfSha256 = DxfHashing.sha256(dxfBytes);
		Map<String, Object> manifest = manifest(preflight, encoding, dxfSha256);
		byte[] bytes = (canonicalJson(manifest) + "\n")
				.getBytes(StandardCharsets.UTF_8);
		return new DxfManifestEncoding(bytes, dxfSha256);
	}

	private Map<String, Object> manifest(GeometryExportPreflight preflight,
			DxfEncodingResult encoding, String dxfSha256) {
		GeometryExportModel model = preflight.getModel();
		Map<String, Object> root = object();
		root.put("schema", SCHEMA);
		root.put("schema_version", SCHEMA_VERSION);
		root.put("application", application());
		root.put("dxf", dxf(model, dxfSha256));
		root.put("request", request(preflight.getRequest()));
		root.put("preflight", preflight(preflight));
		root.put("outcomes", outcomes(model, encoding));
		root.put("warnings", warnings(model));
		return root;
	}

	private Map<String, Object> application() {
		Map<String, Object> value = object();
		value.put("name", "GeoCeDG");
		value.put("version", provenance.getApplicationVersion());
		value.put("repository_commit", provenance.getRepositoryCommit());
		value.put("repository_state", lower(provenance.getRepositoryState()));
		value.put("provenance_source", lower(provenance.getResolutionSource()));
		return value;
	}

	private static Map<String, Object> dxf(GeometryExportModel model,
			String sha256) {
		Map<String, Object> value = object();
		value.put("acad_version", DxfExporter.ACAD_VERSION);
		value.put("encoding", "US-ASCII");
		value.put("line_ending", "CRLF");
		value.put("coordinate_system", model.getCoordinateSystem());
		value.put("source_unit", lower(model.getSourceUnit()));
		value.put("target_unit", lower(model.getTargetUnit()));
		value.put("sha256", sha256);
		return value;
	}

	private static Map<String, Object> request(GeometryExportRequest request) {
		Map<String, Object> value = object();
		value.put("requested_tolerance", request.getRequestedTolerance());
		List<String> guarantees = new ArrayList<>();
		request.getAllowedGuarantees().forEach(
				guarantee -> guarantees.add(lower(guarantee)));
		guarantees.sort(Comparator.naturalOrder());
		value.put("allowed_guarantees", guarantees);
		value.put("approximation_allowed", request.isApproximationAllowed());
		value.put("partial_output_allowed", request.isPartialOutputAllowed());
		value.put("sidecar_requested", request.isSidecarRequested());
		Map<String, Object> limits = object();
		limits.put("maximum_evaluations", request.getMaximumEvaluations());
		limits.put("maximum_depth", request.getMaximumDepth());
		limits.put("maximum_vertices_per_component",
				request.getMaximumVerticesPerComponent());
		limits.put("maximum_total_vertices",
				request.getMaximumTotalVertices());
		value.put("work_limits", limits);
		value.put("default_semantic_domains",
				domains(request.getDefaultSemanticDomains()));
		Map<String, Object> sourceDomains = object();
		Map<String, List<SemanticDomain>> sorted = new TreeMap<>(
				request.getSourceSemanticDomains());
		for (Map.Entry<String, List<SemanticDomain>> entry : sorted.entrySet()) {
			sourceDomains.put(entry.getKey(), domains(entry.getValue()));
		}
		value.put("source_semantic_domains", sourceDomains);
		return value;
	}

	private static List<Object> domains(List<SemanticDomain> domains) {
		List<Object> values = new ArrayList<>();
		for (SemanticDomain domain : domains) {
			Map<String, Object> value = object();
			value.put("branch_key", domain.getBranchKey());
			value.put("key", domain.getKey());
			value.put("start_parameter", domain.getStartParameter());
			value.put("end_parameter", domain.getEndParameter());
			value.put("start_closed", domain.isStartClosed());
			value.put("end_closed", domain.isEndClosed());
			value.put("increasing", domain.isIncreasing());
			values.add(value);
		}
		return values;
	}

	private static Map<String, Object> preflight(
			GeometryExportPreflight preflight) {
		Map<String, Object> value = object();
		value.put("model_version", preflight.getModel().getModelVersion());
		value.put("selection_mode",
				lower(preflight.getModel().getSelectionMode()));
		value.put("exact_components", preflight.getExactCount());
		value.put("approximate_components", preflight.getApproximateCount());
		value.put("unsupported_components", preflight.getUnsupportedCount());
		value.put("invalid_components", preflight.getInvalidCount());
		value.put("omitted_components", preflight.getOmittedCount());
		value.put("hidden_sources", preflight.getHiddenCount());
		value.put("sidecar_required", preflight.isSidecarRequired());
		value.put("complete_request_writable", preflight.isWritable());
		value.put("source_revision_current",
				preflight.isSourceRevisionCurrent());
		return value;
	}

	private static List<Object> outcomes(GeometryExportModel model,
			DxfEncodingResult encoding) {
		List<Object> values = new ArrayList<>();
		for (SourceExportOutcome outcome : model.getOutcomes()) {
			Map<String, Object> value = object();
			value.put("source_id", outcome.getSourceId());
			value.put("id_scope", identityScope(outcome.getIdentityScope()));
			value.put("source_family", outcome.getSourceType());
			value.put("source_label", outcome.getLabel());
			value.put("source_revision", outcome.getSourceRevision());
			value.put("visible", outcome.isVisible());
			value.put("component", component(outcome.getComponentAddress()));
			value.put("fidelity", lower(outcome.getFidelity()));
			value.put("reason", lower(outcome.getReason()));
			value.put("message", outcome.getMessage());
			value.put("emitted", outcome.isEmitted());
			value.put("neutral_entity_id", outcome.getNeutralEntityId());
			EntityEncoding actual = outcome.isEmitted()
					? encoding.getEncoding(outcome.getNeutralEntityId()) : null;
			value.put("dxf_handle", actual == null ? null : actual.getHandle());
			value.put("dxf_entity_type",
					actual == null ? null : actual.getEntityType());
			value.put("approximation",
					approximation(outcome.getApproximationEvidence()));
			values.add(value);
		}
		return values;
	}

	private static Map<String, Object> component(ComponentAddress address) {
		Map<String, Object> value = object();
		value.put("branch_key", address.getBranchKey());
		value.put("component_key", address.getComponentKey());
		if (address.hasSemanticInterval()) {
			Map<String, Object> interval = object();
			interval.put("start_parameter", address.getParameterStart());
			interval.put("end_parameter", address.getParameterEnd());
			interval.put("start_included", address.isStartIncluded());
			interval.put("end_included", address.isEndIncluded());
			interval.put("increasing",
					address.getParameterEnd() > address.getParameterStart());
			value.put("semantic_interval", interval);
		} else {
			value.put("semantic_interval", null);
		}
		return value;
	}

	private static Map<String, Object> approximation(
			ApproximationEvidence evidence) {
		if (evidence == null) {
			return null;
		}
		Map<String, Object> value = object();
		value.put("method", lower(evidence.getMethod()));
		value.put("requested_tolerance", evidence.getRequestedTolerance());
		value.put("achieved_error", evidence.getAchievedError());
		value.put("guarantee", lower(evidence.getGuarantee()));
		value.put("evaluations", evidence.getEvaluations());
		value.put("subdivisions", evidence.getSubdivisions());
		value.put("segments", evidence.getSegments());
		value.put("vertices", evidence.getVertices());
		value.put("maximum_depth", evidence.getMaximumDepth());
		return value;
	}

	private static List<Object> warnings(GeometryExportModel model) {
		List<Object> values = new ArrayList<>();
		for (SourceExportOutcome outcome : model.getOutcomes()) {
			if (outcome.getFidelity() == Fidelity.APPROXIMATE) {
				values.add(warning("approximate_geometry", outcome.getSourceId(),
						outcome.getComponentAddress().getBranchKey(),
						outcome.getComponentAddress().getComponentKey(),
						"DXF geometry is an explicit export-only approximation."));
			}
			if (!outcome.isVisible()) {
				values.add(warning("hidden_source_included", outcome.getSourceId(),
						outcome.getComponentAddress().getBranchKey(),
						outcome.getComponentAddress().getComponentKey(),
						"Hidden source is included in the export snapshot."));
			}
			if (!outcome.isEmitted()) {
				values.add(warning(lower(outcome.getReason()),
						outcome.getSourceId(),
						outcome.getComponentAddress().getBranchKey(),
						outcome.getComponentAddress().getComponentKey(),
						outcome.getMessage()));
			}
		}
		for (Diagnostic diagnostic : model.getDiagnostics()) {
			values.add(warning(lower(diagnostic.getCode()),
					diagnostic.getSourceId(), null, null, diagnostic.getMessage()));
		}
		return values;
	}

	private static Map<String, Object> warning(String code, String sourceId,
			String branchKey, String componentKey, String message) {
		Map<String, Object> value = object();
		value.put("code", code);
		value.put("source_id", sourceId);
		value.put("branch_key", branchKey);
		value.put("component_key", componentKey);
		value.put("message", message);
		return value;
	}

	private static void validatePreflight(GeometryExportPreflight preflight) {
		Objects.requireNonNull(preflight, "Export preflight is required");
		if (!preflight.isWritable()) {
			throw new IllegalStateException(
					"Strict preflight rejected the complete export request");
		}
		if (!preflight.isSourceRevisionCurrent()) {
			throw new IllegalStateException(
					"Source revision changed after export preflight");
		}
	}

	private static void validateEncoding(GeometryExportModel model,
			DxfEncodingResult encoding) {
		if (!encoding.isEncodingOf(model)) {
			throw new IllegalArgumentException(
					"DXF encoding belongs to a different export preflight model");
		}
		Map<String, EntityEncoding> actual = encoding.getEncodedEntities();
		if (actual.size() != model.getEntities().size()) {
			throw new IllegalArgumentException(
					"DXF handle mappings do not cover the neutral model");
		}
		Set<String> handles = new HashSet<>();
		List<ParsedEntity> parsed = parseEntities(encoding.getDxfText());
		if (parsed.size() != model.getEntities().size()) {
			throw new IllegalArgumentException(
					"DXF entity stream disagrees with the neutral model");
		}
		for (int ordinal = 0; ordinal < model.getEntities().size(); ordinal++) {
			Entity entity = model.getEntities().get(ordinal);
			EntityEncoding mapped = actual.get(entity.getNeutralEntityId());
			if (mapped == null) {
				throw new IllegalArgumentException(
						"Missing DXF handle for neutral entity: "
								+ entity.getNeutralEntityId());
			}
			String expectedType = expectedDxfType(entity);
			if (!expectedType.equals(mapped.getEntityType())) {
				throw new IllegalArgumentException(
						"DXF entity type disagrees with neutral geometry");
			}
			if (!mapped.getHandle().matches("[0-9A-F]+")
					|| !handles.add(mapped.getHandle())) {
				throw new IllegalArgumentException(
						"DXF handles must be unique uppercase hexadecimal values");
			}
			ParsedEntity emitted = parsed.get(ordinal);
			if (!expectedType.equals(emitted.type)
					|| !mapped.getHandle().equals(emitted.handle)) {
				throw new IllegalArgumentException(
						"DXF text disagrees with its handle mapping");
			}
		}
	}

	private static String expectedDxfType(Entity entity) {
		switch (entity.getGeometry().getType()) {
		case POINT:
			return "POINT";
		case SEGMENT:
			return "LINE";
		case RAY:
			return "RAY";
		case INFINITE_LINE:
			return "XLINE";
		case CIRCLE:
			return "CIRCLE";
		case ARC:
			return "ARC";
		case ELLIPSE:
			return "ELLIPSE";
		case POLYLINE:
			return "LWPOLYLINE";
		default:
			throw new IllegalArgumentException(
					"Unsupported neutral geometry in DXF encoding");
		}
	}

	private static List<ParsedEntity> parseEntities(String text) {
		if (!text.endsWith("\r\n")) {
			throw new IllegalArgumentException("DXF text must use CRLF records");
		}
		String[] records = text.split("\\r\\n", -1);
		if (records.length < 3 || records.length % 2 == 0
				|| !records[records.length - 1].isEmpty()) {
			throw new IllegalArgumentException("DXF text has incomplete code pairs");
		}
		List<ParsedEntity> result = new ArrayList<>();
		boolean entitiesSection = false;
		for (int index = 0; index + 3 < records.length; index += 2) {
			if (!entitiesSection && "0".equals(records[index])
					&& "SECTION".equals(records[index + 1])
					&& "2".equals(records[index + 2])
					&& "ENTITIES".equals(records[index + 3])) {
				entitiesSection = true;
				index += 2;
				continue;
			}
			if (!entitiesSection) {
				continue;
			}
			if ("0".equals(records[index])
					&& "ENDSEC".equals(records[index + 1])) {
				return result;
			}
			if (!"0".equals(records[index])) {
				continue;
			}
			String type = records[index + 1];
			String handle = null;
			int cursor = index + 2;
			while (cursor + 1 < records.length
					&& !"0".equals(records[cursor])) {
				if ("5".equals(records[cursor])) {
					handle = records[cursor + 1];
				}
				cursor += 2;
			}
			if (handle == null) {
				throw new IllegalArgumentException(
						"DXF entity is missing its handle");
			}
			result.add(new ParsedEntity(type, handle));
			index = cursor - 2;
		}
		throw new IllegalArgumentException("DXF ENTITIES section is incomplete");
	}

	private static final class ParsedEntity {
		private final String type;
		private final String handle;

		private ParsedEntity(String type, String handle) {
			this.type = type;
			this.handle = handle;
		}
	}

	private static byte[] encodeAsciiDxf(String text) {
		if (text == null || text.isEmpty()) {
			throw new IllegalArgumentException("DXF text is required");
		}
		for (int index = 0; index < text.length(); index++) {
			if (text.charAt(index) > 0x7f) {
				throw new IllegalArgumentException("DXF text must be ASCII");
			}
		}
		return text.getBytes(StandardCharsets.US_ASCII);
	}

	private static String identityScope(IdentityScope scope) {
		return scope == IdentityScope.PERSISTENT
				? "persistent" : "construction-revision";
	}

	private static String lower(Enum<?> value) {
		return value.name().toLowerCase(java.util.Locale.ROOT);
	}

	private static Map<String, Object> object() {
		return new LinkedHashMap<>();
	}

	private static String canonicalJson(Object value) {
		StringBuilder output = new StringBuilder();
		appendJson(output, value);
		return output.toString();
	}

	private static void appendJson(StringBuilder output, Object value) {
		if (value == null) {
			output.append("null");
		} else if (value instanceof String) {
			appendJsonString(output, (String) value);
		} else if (value instanceof Boolean || value instanceof Number) {
			appendJsonScalar(output, value);
		} else if (value instanceof Map) {
			appendJsonObject(output, (Map<?, ?>) value);
		} else if (value instanceof List) {
			appendJsonArray(output, (List<?>) value);
		} else {
			throw new IllegalArgumentException(
					"Unsupported manifest JSON value: " + value.getClass());
		}
	}

	private static void appendJsonScalar(StringBuilder output, Object value) {
		if (value instanceof Double) {
			double number = (Double) value;
			if (!Double.isFinite(number)) {
				throw new IllegalArgumentException(
						"Manifest numbers must be finite");
			}
			output.append(number == 0 ? "0.0" : Double.toString(number));
		} else if (value instanceof Float) {
			float number = (Float) value;
			if (!Float.isFinite(number)) {
				throw new IllegalArgumentException(
						"Manifest numbers must be finite");
			}
			output.append(number == 0 ? "0.0" : Float.toString(number));
		} else {
			output.append(value);
		}
	}

	private static void appendJsonObject(StringBuilder output,
			Map<?, ?> value) {
		output.append('{');
		boolean comma = false;
		for (Map.Entry<?, ?> entry : value.entrySet()) {
			if (!(entry.getKey() instanceof String)) {
				throw new IllegalArgumentException(
						"Manifest JSON object keys must be strings");
			}
			if (comma) {
				output.append(',');
			}
			appendJsonString(output, (String) entry.getKey());
			output.append(':');
			appendJson(output, entry.getValue());
			comma = true;
		}
		output.append('}');
	}

	private static void appendJsonArray(StringBuilder output, List<?> value) {
		output.append('[');
		for (int index = 0; index < value.size(); index++) {
			if (index > 0) {
				output.append(',');
			}
			appendJson(output, value.get(index));
		}
		output.append(']');
	}

	private static void appendJsonString(StringBuilder output, String value) {
		output.append('"');
		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);
			switch (character) {
			case '"':
				output.append("\\\"");
				break;
			case '\\':
				output.append("\\\\");
				break;
			case '\b':
				output.append("\\b");
				break;
			case '\f':
				output.append("\\f");
				break;
			case '\n':
				output.append("\\n");
				break;
			case '\r':
				output.append("\\r");
				break;
			case '\t':
				output.append("\\t");
				break;
			default:
				if (character < 0x20 || Character.isSurrogate(character)) {
					output.append("\\u");
					output.append(hexDigit(character >>> 12));
					output.append(hexDigit(character >>> 8));
					output.append(hexDigit(character >>> 4));
					output.append(hexDigit(character));
				} else {
					output.append(character);
				}
				break;
			}
		}
		output.append('"');
	}

	private static char hexDigit(int value) {
		return "0123456789abcdef".charAt(value & 0xf);
	}
}
