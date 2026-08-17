/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Strict version-one parser and deterministic writer for the flat XML section. */
public final class SpatialRecordXmlCodec {
	private SpatialRecordXmlCodec() {
	}

	/**
	 * Parses one flat record after the host XML reader has decoded attributes.
	 *
	 * @return the immutable typed record
	 */
	public static SpatialIdentityRecord parseRecord(String elementName,
			Map<String, String> attributes) {
		if ("geo".equals(elementName)) {
			checkAttributes(attributes, "id", "provider", "family", "schema",
					"schemaVersion", "authority", "bindingRole", "outputRole",
					"cardinality", "definitionRevision", "topologyRevision",
					"copySource");
			return new GeoIdentityRecord(PersistentGeoId.parse(required(attributes, "id")),
					required(attributes, "provider"), required(attributes, "family"),
					required(attributes, "schema"), integer(attributes, "schemaVersion"),
					EditAuthorityMode.valueOf(required(attributes, "authority")),
					ProjectionBindingRole.valueOf(required(attributes, "bindingRole")),
					required(attributes, "outputRole"), integer(attributes, "cardinality"),
					longValue(attributes, "definitionRevision"),
					longValue(attributes, "topologyRevision"),
					optionalGeoId(attributes, "copySource"));
		}
		if ("object".equals(elementName)) {
			checkAttributes(attributes, "id", "semanticVersion", "type", "authority",
					"schema", "schemaVersion", "geos", "definitionRevision",
					"topologyRevision", "copySource");
			return new SpatialObjectRecord(SpatialObjectId.parse(required(attributes, "id")),
					integer(attributes, "semanticVersion"), required(attributes, "type"),
					EditAuthorityMode.valueOf(required(attributes, "authority")),
					required(attributes, "schema"), integer(attributes, "schemaVersion"),
					geoIds(attributes, "geos"), longValue(attributes, "definitionRevision"),
					longValue(attributes, "topologyRevision"),
					optionalObjectId(attributes, "copySource"));
		}
		if ("frame".equals(elementName)) {
			checkAttributes(attributes, "id", "semanticVersion", "geos", "revision",
					"copySource");
			return new ProjectionFrameRecord(
					ProjectionFrameId.parse(required(attributes, "id")),
					integer(attributes, "semanticVersion"), geoIds(attributes, "geos"),
					longValue(attributes, "revision"),
					optionalFrameId(attributes, "copySource"));
		}
		if ("system".equals(elementName)) {
			checkAttributes(attributes, "id", "semanticVersion", "maps", "relations",
					"geos", "revision", "copySource");
			return new ProjectionSystemRecord(
					ProjectionSystemId.parse(required(attributes, "id")),
					integer(attributes, "semanticVersion"), mapIds(attributes, "maps"),
					relationIds(attributes, "relations"), geoIds(attributes, "geos"),
					longValue(attributes, "revision"),
					optionalSystemId(attributes, "copySource"));
		}
		if ("diagramMap".equals(elementName)) {
			checkAttributes(attributes, "id", "semanticVersion", "system", "frame",
					"role", "family", "relations", "geos", "revision", "copySource");
			return new ProjectionDiagramMapRecord(
					ProjectionDiagramMapId.parse(required(attributes, "id")),
					integer(attributes, "semanticVersion"),
					ProjectionSystemId.parse(required(attributes, "system")),
					ProjectionFrameId.parse(required(attributes, "frame")),
					ProjectionFrameUseRole.valueOf(required(attributes, "role")),
					required(attributes, "family"), relationIds(attributes, "relations"),
					geoIds(attributes, "geos"), longValue(attributes, "revision"),
					optionalMapId(attributes, "copySource"));
		}
		if ("frameRelation".equals(elementName)) {
			checkAttributes(attributes, "id", "semanticVersion", "system", "sourceMap",
					"destinationMap", "kind", "geos", "revision", "copySource");
			return new ProjectionFrameRelationRecord(
					ProjectionFrameRelationId.parse(required(attributes, "id")),
					integer(attributes, "semanticVersion"),
					ProjectionSystemId.parse(required(attributes, "system")),
					ProjectionDiagramMapId.parse(required(attributes, "sourceMap")),
					ProjectionDiagramMapId.parse(required(attributes, "destinationMap")),
					required(attributes, "kind"), geoIds(attributes, "geos"),
					longValue(attributes, "revision"),
					optionalRelationId(attributes, "copySource"));
		}
		if ("binding".equals(elementName)) {
			checkAttributes(attributes, "id", "semanticVersion", "object", "system",
					"diagramMap", "frame", "role", "representation", "expectedType",
					"schema", "schemaVersion", "geos", "revision", "copySource");
			return new ProjectionBindingRecord(
					ProjectionBindingId.parse(required(attributes, "id")),
					integer(attributes, "semanticVersion"),
					SpatialObjectId.parse(required(attributes, "object")),
					ProjectionSystemId.parse(required(attributes, "system")),
					ProjectionDiagramMapId.parse(required(attributes, "diagramMap")),
					ProjectionFrameId.parse(required(attributes, "frame")),
					ProjectionBindingRole.valueOf(required(attributes, "role")),
					required(attributes, "representation"),
					required(attributes, "expectedType"), required(attributes, "schema"),
					integer(attributes, "schemaVersion"), geoIds(attributes, "geos"),
					longValue(attributes, "revision"),
					optionalBindingId(attributes, "copySource"));
		}
		throw new IllegalArgumentException("Unknown geocedgSpatial record: " + elementName);
	}

	/**
	 * Writes a complete deterministic flat section sorted by kind and ID.
	 *
	 * @return the section XML, or an empty string for an empty collection
	 */
	public static String writeSection(Collection<? extends SpatialIdentityRecord> records) {
		if (records.isEmpty()) {
			return "";
		}
		ArrayList<SpatialIdentityRecord> sorted = new ArrayList<>();
		sorted.addAll(records);
		Collections.sort(sorted, new Comparator<SpatialIdentityRecord>() {
			@Override
			public int compare(SpatialIdentityRecord first, SpatialIdentityRecord second) {
				return first.getId().compareTo(second.getId());
			}
		});
		StringBuilder xml = new StringBuilder();
		xml.append("\t<geocedgSpatial version=\"1\">\n");
		for (SpatialIdentityRecord record : sorted) {
			xml.append("\t\t").append(writeRecord(record)).append('\n');
		}
		xml.append("\t</geocedgSpatial>\n");
		return xml.toString();
	}

	/**
	 * Writes one canonical self-closing record element.
	 *
	 * @return the canonical record XML
	 */
	public static String writeRecord(SpatialIdentityRecord record) {
		StringBuilder xml = new StringBuilder("<").append(record.getXmlElementName());
		attribute(xml, "id", record.getId().toExternalForm());
		if (record instanceof GeoIdentityRecord) {
			writeGeo(xml, (GeoIdentityRecord) record);
		} else if (record instanceof SpatialObjectRecord) {
			writeObject(xml, (SpatialObjectRecord) record);
		} else if (record instanceof ProjectionFrameRecord) {
			writeFrame(xml, (ProjectionFrameRecord) record);
		} else if (record instanceof ProjectionSystemRecord) {
			writeSystem(xml, (ProjectionSystemRecord) record);
		} else if (record instanceof ProjectionDiagramMapRecord) {
			writeMap(xml, (ProjectionDiagramMapRecord) record);
		} else if (record instanceof ProjectionFrameRelationRecord) {
			writeRelation(xml, (ProjectionFrameRelationRecord) record);
		} else if (record instanceof ProjectionBindingRecord) {
			writeBinding(xml, (ProjectionBindingRecord) record);
		} else {
			throw new IllegalArgumentException("Unsupported spatial record class: "
					+ record.getClass().getName());
		}
		if (record.getCopySourceId() != null) {
			attribute(xml, "copySource", record.getCopySourceId().toExternalForm());
		}
		return xml.append("/>").toString();
	}

	private static void writeGeo(StringBuilder xml, GeoIdentityRecord record) {
		attribute(xml, "provider", record.getProvider());
		attribute(xml, "family", record.getFamily());
		attribute(xml, "schema", record.getSchemaId());
		attribute(xml, "schemaVersion", record.getSchemaVersion());
		attribute(xml, "authority", record.getAuthority().name());
		attribute(xml, "bindingRole", record.getBindingRole().name());
		attribute(xml, "outputRole", record.getStableOutputRole());
		attribute(xml, "cardinality", record.getOutputCardinality());
		attribute(xml, "definitionRevision", record.getDefinitionRevision());
		attribute(xml, "topologyRevision", record.getTopologyRevision());
	}

	private static void writeObject(StringBuilder xml, SpatialObjectRecord record) {
		attribute(xml, "semanticVersion", record.getSemanticVersion());
		attribute(xml, "type", record.getSpatialType());
		attribute(xml, "authority", record.getAuthority().name());
		attribute(xml, "schema", record.getSchemaId());
		attribute(xml, "schemaVersion", record.getSchemaVersion());
		attribute(xml, "geos", join(record.getDefinitionGeoIds()));
		attribute(xml, "definitionRevision", record.getDefinitionRevision());
		attribute(xml, "topologyRevision", record.getTopologyRevision());
	}

	private static void writeFrame(StringBuilder xml, ProjectionFrameRecord record) {
		attribute(xml, "semanticVersion", record.getSemanticVersion());
		attribute(xml, "geos", join(record.getDefinitionGeoIds()));
		attribute(xml, "revision", record.getRevision());
	}

	private static void writeSystem(StringBuilder xml, ProjectionSystemRecord record) {
		attribute(xml, "semanticVersion", record.getSemanticVersion());
		attribute(xml, "maps", join(record.getMapIds()));
		attribute(xml, "relations", join(record.getRelationIds()));
		attribute(xml, "geos", join(record.getDefinitionGeoIds()));
		attribute(xml, "revision", record.getRevision());
	}

	private static void writeMap(StringBuilder xml, ProjectionDiagramMapRecord record) {
		attribute(xml, "semanticVersion", record.getSemanticVersion());
		attribute(xml, "system", record.getSystemId().toExternalForm());
		attribute(xml, "frame", record.getFrameId().toExternalForm());
		attribute(xml, "role", record.getFrameUseRole().name());
		attribute(xml, "family", record.getFamily());
		attribute(xml, "relations", join(record.getRelationIds()));
		attribute(xml, "geos", join(record.getDefinitionGeoIds()));
		attribute(xml, "revision", record.getRevision());
	}

	private static void writeRelation(StringBuilder xml,
			ProjectionFrameRelationRecord record) {
		attribute(xml, "semanticVersion", record.getSemanticVersion());
		attribute(xml, "system", record.getSystemId().toExternalForm());
		attribute(xml, "sourceMap", record.getSourceMapId().toExternalForm());
		attribute(xml, "destinationMap", record.getDestinationMapId().toExternalForm());
		attribute(xml, "kind", record.getRelationKind());
		attribute(xml, "geos", join(record.getDefinitionGeoIds()));
		attribute(xml, "revision", record.getRevision());
	}

	private static void writeBinding(StringBuilder xml, ProjectionBindingRecord record) {
		attribute(xml, "semanticVersion", record.getSemanticVersion());
		attribute(xml, "object", record.getObjectId().toExternalForm());
		attribute(xml, "system", record.getSystemId().toExternalForm());
		attribute(xml, "diagramMap", record.getDiagramMapId().toExternalForm());
		attribute(xml, "frame", record.getFrameId().toExternalForm());
		attribute(xml, "role", record.getRole().name());
		attribute(xml, "representation", record.getRepresentationType());
		attribute(xml, "expectedType", record.getExpectedSpatialType());
		attribute(xml, "schema", record.getSchemaId());
		attribute(xml, "schemaVersion", record.getSchemaVersion());
		attribute(xml, "geos", join(record.getProjectedGeoIds()));
		attribute(xml, "revision", record.getRevision());
	}

	private static String required(Map<String, String> attributes, String name) {
		String value = attributes.get(name);
		if (value == null) {
			throw new IllegalArgumentException("Missing geocedgSpatial attribute: " + name);
		}
		return value;
	}

	private static int integer(Map<String, String> attributes, String name) {
		return Integer.parseInt(required(attributes, name));
	}

	private static long longValue(Map<String, String> attributes, String name) {
		return Long.parseLong(required(attributes, name));
	}

	private static List<PersistentGeoId> geoIds(Map<String, String> attributes,
			String name) {
		ArrayList<PersistentGeoId> result = new ArrayList<>();
		for (String external : splitIds(required(attributes, name))) {
			result.add(PersistentGeoId.parse(external));
		}
		return result;
	}

	private static List<ProjectionDiagramMapId> mapIds(Map<String, String> attributes,
			String name) {
		ArrayList<ProjectionDiagramMapId> result = new ArrayList<>();
		for (String external : splitIds(required(attributes, name))) {
			result.add(ProjectionDiagramMapId.parse(external));
		}
		return result;
	}

	private static List<ProjectionFrameRelationId> relationIds(
			Map<String, String> attributes, String name) {
		ArrayList<ProjectionFrameRelationId> result = new ArrayList<>();
		for (String external : splitIds(required(attributes, name))) {
			result.add(ProjectionFrameRelationId.parse(external));
		}
		return result;
	}

	private static List<String> splitIds(String value) {
		String trimmed = value.trim();
		return trimmed.isEmpty() ? Collections.<String>emptyList()
				: Arrays.asList(trimmed.split("\\s+"));
	}

	private static PersistentGeoId optionalGeoId(Map<String, String> attributes,
			String name) {
		return attributes.containsKey(name) ? PersistentGeoId.parse(attributes.get(name))
				: null;
	}

	private static SpatialObjectId optionalObjectId(Map<String, String> attributes,
			String name) {
		return attributes.containsKey(name) ? SpatialObjectId.parse(attributes.get(name))
				: null;
	}

	private static ProjectionFrameId optionalFrameId(Map<String, String> attributes,
			String name) {
		return attributes.containsKey(name) ? ProjectionFrameId.parse(attributes.get(name))
				: null;
	}

	private static ProjectionSystemId optionalSystemId(Map<String, String> attributes,
			String name) {
		return attributes.containsKey(name) ? ProjectionSystemId.parse(attributes.get(name))
				: null;
	}

	private static ProjectionDiagramMapId optionalMapId(Map<String, String> attributes,
			String name) {
		return attributes.containsKey(name)
				? ProjectionDiagramMapId.parse(attributes.get(name)) : null;
	}

	private static ProjectionFrameRelationId optionalRelationId(
			Map<String, String> attributes, String name) {
		return attributes.containsKey(name)
				? ProjectionFrameRelationId.parse(attributes.get(name)) : null;
	}

	private static ProjectionBindingId optionalBindingId(Map<String, String> attributes,
			String name) {
		return attributes.containsKey(name)
				? ProjectionBindingId.parse(attributes.get(name)) : null;
	}

	private static void checkAttributes(Map<String, String> attributes,
			String... allowedNames) {
		Set<String> allowed = new HashSet<>(Arrays.asList(allowedNames));
		for (String name : attributes.keySet()) {
			if (!allowed.contains(name)) {
				throw new IllegalArgumentException(
						"Unknown geocedgSpatial attribute: " + name);
			}
		}
	}

	private static String join(List<? extends SpatialIdentityId> ids) {
		StringBuilder joined = new StringBuilder();
		for (SpatialIdentityId id : ids) {
			if (joined.length() > 0) {
				joined.append(' ');
			}
			joined.append(id.toExternalForm());
		}
		return joined.toString();
	}

	private static void attribute(StringBuilder xml, String name, Object value) {
		xml.append(' ').append(name).append("=\"").append(escape(String.valueOf(value)))
				.append('"');
	}

	private static String escape(String value) {
		StringBuilder escaped = new StringBuilder(value.length());
		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);
			switch (character) {
			case '&':
				escaped.append("&amp;");
				break;
			case '<':
				escaped.append("&lt;");
				break;
			case '>':
				escaped.append("&gt;");
				break;
			case '"':
				escaped.append("&quot;");
				break;
			case '\'':
				escaped.append("&apos;");
				break;
			default:
				escaped.append(character);
			}
		}
		return escaped.toString();
	}
}
