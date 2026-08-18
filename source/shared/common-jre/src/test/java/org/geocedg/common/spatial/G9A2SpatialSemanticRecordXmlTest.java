/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.spatial;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingId;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionBindingRole;
import org.geocedg.common.kernel.spatial.identity.ProjectionDiagramMapId;
import org.geocedg.common.kernel.spatial.identity.ProjectionDiagramMapRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameId;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRelationId;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameRelationRecord;
import org.geocedg.common.kernel.spatial.identity.ProjectionFrameUseRole;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemId;
import org.geocedg.common.kernel.spatial.identity.ProjectionSystemRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialRecordXmlCodec;
import org.geocedg.common.kernel.spatial.semantic.NumericPolicy;
import org.junit.jupiter.api.Test;

class G9A2SpatialSemanticRecordXmlTest {
	private static final ProjectionFrameId FRAME_H = frameId(101);
	private static final ProjectionFrameId FRAME_V = frameId(102);
	private static final ProjectionSystemId SYSTEM = systemId(103);
	private static final ProjectionDiagramMapId MAP_H = mapId(104);
	private static final ProjectionDiagramMapId MAP_V = mapId(105);
	private static final ProjectionFrameRelationId RELATION = relationId(106);
	private static final SpatialObjectId OBJECT = objectId(107);
	private static final ProjectionBindingId BINDING_H = bindingId(108);
	private static final ProjectionBindingId BINDING_V = bindingId(109);

	@Test
	void everyVersionTwoRecordHasAStrictDeterministicRoundTrip() {
		assertRoundTrip("frame", frameRecord(FRAME_H), attributes(
				"id", FRAME_H.toExternalForm(), "semanticVersion", "2",
				"origin", geoId(1).toExternalForm(), "u", geoId(2).toExternalForm(),
				"v", geoId(3).toExternalForm(), "family", "ORTHOGRAPHIC",
				"units", "model-unit", "handedness", "RIGHT_HANDED",
				"fidelity", "EXACT", "revision", "7"));
		assertRoundTrip("system", systemRecord(), attributes(
				"id", SYSTEM.toExternalForm(), "semanticVersion", "2", "maps",
				MAP_H + " " + MAP_V, "relations", RELATION.toExternalForm(),
				"units", "model-unit", "absoluteTolerance", "1.0E-9",
				"relativeTolerance", "2.0E-9", "rankTolerance", "3.0E-12",
				"mapTolerance", "4.0E-9", "hingeTolerance", "5.0E-9",
				"conditionLimit", "1.0E10", "revision", "8"));
		assertRoundTrip("diagramMap", mapRecord(MAP_H, FRAME_H), attributes(
				"id", MAP_H.toExternalForm(), "semanticVersion", "2",
				"system", SYSTEM.toExternalForm(), "frame", FRAME_H.toExternalForm(),
				"role", "DEFINING", "family", "ORIENTED_ISOMETRY",
				"orientation", "PRESERVING", "units", "model-unit",
				"fidelity", "EXACT", "a00", geoId(10).toExternalForm(),
				"a01", geoId(11).toExternalForm(), "a10", geoId(12).toExternalForm(),
				"a11", geoId(13).toExternalForm(), "b0", geoId(14).toExternalForm(),
				"b1", geoId(15).toExternalForm(), "declaredScale",
				geoId(16).toExternalForm(), "relations", RELATION.toExternalForm(),
				"revision", "9"));
		assertRoundTrip("frameRelation", hingeRecord(), attributes(
				"id", RELATION.toExternalForm(), "semanticVersion", "2",
				"system", SYSTEM.toExternalForm(), "sourceMap", MAP_H.toExternalForm(),
				"destinationMap", MAP_V.toExternalForm(), "kind", "HINGE_UNFOLD",
				"supportStart", geoId(19).toExternalForm(), "supportEnd",
				geoId(20).toExternalForm(), "orientation", "POSITIVE", "provenance",
				"EXPLICIT_CONSTRUCTION", "foldSign", geoId(17).toExternalForm(),
				"revision", "10"));
		assertRoundTrip("binding", bindingRecord(BINDING_H, MAP_H, FRAME_H, 18),
				attributes("id", BINDING_H.toExternalForm(), "semanticVersion", "2",
						"object", OBJECT.toExternalForm(), "system", SYSTEM.toExternalForm(),
						"diagramMap", MAP_H.toExternalForm(), "frame",
						FRAME_H.toExternalForm(), "role", "DEFINING", "representation",
						"POINT", "expectedType", "POINT", "schema",
						SpatialObjectRecord.POINT_SCHEMA_ID, "schemaVersion", "1",
						"projectedPoint", geoId(18).toExternalForm(), "fidelity", "EXACT",
						"correspondence", "NOT_REQUIRED", "revision", "11"));
		assertRoundTrip("object", objectRecord(), attributes(
				"id", OBJECT.toExternalForm(), "semanticVersion", "2", "type", "POINT",
				"authority", "PROJECTION_DEFINED", "schema",
				SpatialObjectRecord.POINT_SCHEMA_ID, "schemaVersion", "1",
				"system", SYSTEM.toExternalForm(), "bindings",
				BINDING_H + " " + BINDING_V, "definitionRevision", "12",
				"topologyRevision", "13"));
	}

	@Test
	void changeOfPlaneRoundTripNeverInventsAFoldSign() {
		ProjectionFrameRelationRecord relation = new ProjectionFrameRelationRecord(
				RELATION, 2, SYSTEM, MAP_H, MAP_V,
				ProjectionFrameRelationRecord.CHANGE_OF_PLANE, geoId(19), geoId(20),
				ProjectionFrameRelationRecord.POSITIVE_ORIENTATION,
				ProjectionFrameRelationRecord.EXPLICIT_CONSTRUCTION, null, 4);
		Map<String, String> attributes = attributes(
				"id", RELATION.toExternalForm(), "semanticVersion", "2",
				"system", SYSTEM.toExternalForm(), "sourceMap", MAP_H.toExternalForm(),
				"destinationMap", MAP_V.toExternalForm(), "kind", "CHANGE_OF_PLANE",
				"supportStart", geoId(19).toExternalForm(), "supportEnd",
				geoId(20).toExternalForm(), "orientation", "POSITIVE", "provenance",
				"EXPLICIT_CONSTRUCTION", "revision", "4");

		assertRoundTrip("frameRelation", relation, attributes);
		assertNull(relation.getFoldSignGeoId());
		assertThat(SpatialRecordXmlCodec.writeRecord(relation),
				not(containsString("foldSign")));
		Map<String, String> invalid = new HashMap<>(attributes);
		invalid.put("foldSign", geoId(17).toExternalForm());
		assertThrows(IllegalArgumentException.class,
				() -> SpatialRecordXmlCodec.parseRecord("frameRelation", invalid));
	}

	@Test
	void numericPolicyUsesEveryExplicitPersistedCertificateInput() {
		ProjectionSystemRecord record = systemRecord();
		NumericPolicy policy = new NumericPolicy(record.getAbsoluteTolerance(),
				record.getRelativeTolerance(), record.getRankTolerance(),
				record.getMapTolerance(), record.getHingeTolerance(),
				record.getConditionLimit());

		assertEquals(1e-9, policy.getAbsoluteTolerance());
		assertEquals(2e-9, policy.getRelativeTolerance());
		assertEquals(3e-12, policy.getRankTolerance());
		assertEquals(4e-9, policy.getMapTolerance());
		assertEquals(5e-9, policy.getHingeTolerance());
		assertEquals(1e10, policy.getConditionLimit());
	}

	@Test
	void unknownVersionsAttributesAndMissingRequiredFieldsFailStrictly() {
		Map<String, String> frame = attributes(
				"id", FRAME_H.toExternalForm(), "semanticVersion", "2",
				"origin", geoId(1).toExternalForm(), "u", geoId(2).toExternalForm(),
				"v", geoId(3).toExternalForm(), "family", "ORTHOGRAPHIC",
				"units", "model-unit", "handedness", "RIGHT_HANDED",
				"fidelity", "EXACT", "revision", "7");
		Map<String, String> future = new HashMap<>(frame);
		future.put("semanticVersion", "999");
		Map<String, String> unknownAttribute = new HashMap<>(frame);
		unknownAttribute.put("future", "opaque");
		Map<String, String> missing = new HashMap<>(frame);
		missing.remove("origin");

		assertThrows(IllegalArgumentException.class,
				() -> SpatialRecordXmlCodec.parseRecord("frame", future));
		assertThrows(IllegalArgumentException.class,
				() -> SpatialRecordXmlCodec.parseRecord("frame", unknownAttribute));
		assertThrows(IllegalArgumentException.class,
				() -> SpatialRecordXmlCodec.parseRecord("frame", missing));
		assertThrows(IllegalArgumentException.class,
				() -> SpatialRecordXmlCodec.parseRecord("futureRecord", frame));
	}

	@Test
	void malformedFamiliesSchemasAndPoliciesCannotEnterVersionTwo() {
		assertThrows(IllegalArgumentException.class,
				() -> new ProjectionFrameRelationRecord(RELATION, 2, SYSTEM, MAP_H,
						MAP_V, "VISIBLE_LINE_GUESS", geoId(19), geoId(20),
						ProjectionFrameRelationRecord.POSITIVE_ORIENTATION,
						ProjectionFrameRelationRecord.EXPLICIT_CONSTRUCTION, null, 1));
		assertThrows(IllegalArgumentException.class,
				() -> new ProjectionFrameRelationRecord(RELATION, 2, SYSTEM, MAP_H,
						MAP_V, ProjectionFrameRelationRecord.HINGE_UNFOLD, geoId(19),
						geoId(19), ProjectionFrameRelationRecord.POSITIVE_ORIENTATION,
						ProjectionFrameRelationRecord.EXPLICIT_CONSTRUCTION, geoId(17), 1));
		assertThrows(IllegalArgumentException.class,
				() -> new ProjectionFrameRelationRecord(RELATION, 2, SYSTEM, MAP_H,
						MAP_V, ProjectionFrameRelationRecord.CHANGE_OF_PLANE, geoId(19),
						geoId(20), "CLOCKWISE",
						ProjectionFrameRelationRecord.EXPLICIT_CONSTRUCTION, null, 1));
		assertThrows(IllegalArgumentException.class,
				() -> new SpatialObjectRecord(OBJECT, 2, "LINE",
						EditAuthorityMode.PROJECTION_DEFINED,
						SpatialObjectRecord.POINT_SCHEMA_ID, 1, SYSTEM,
						List.of(BINDING_H), 1, 1));
		assertThrows(IllegalArgumentException.class,
				() -> new ProjectionSystemRecord(SYSTEM, 2, List.of(MAP_H), List.of(),
						"model-unit", 1e-9, 1e-9, 1e-12, Double.NaN, 1e-9,
						1e10, 1));
	}

	@Test
	void versionOneRecordsStayInertAndRetainTheirHistoricalShape() {
		ProjectionFrameRecord frame = new ProjectionFrameRecord(FRAME_H, 1,
				List.of(geoId(1)), 4);
		ProjectionSystemRecord system = new ProjectionSystemRecord(SYSTEM, 1,
				List.of(MAP_H), List.of(), List.of(geoId(1)), 5);

		assertNull(frame.getFamily());
		assertNull(frame.getOriginGeoId());
		assertNull(system.getUnits());
		assertEquals(1, frame.getSemanticVersion());
		assertEquals(1, system.getSemanticVersion());
		assertThat(SpatialRecordXmlCodec.writeRecord(frame),
				containsString("geos=\"" + geoId(1) + "\""));
		assertThat(SpatialRecordXmlCodec.writeRecord(frame),
				not(containsString("origin=")));
	}

	private static ProjectionFrameRecord frameRecord(ProjectionFrameId id) {
		return new ProjectionFrameRecord(id, 2, geoId(1), geoId(2), geoId(3),
				"ORTHOGRAPHIC", "model-unit", "RIGHT_HANDED", "EXACT", 7);
	}

	private static ProjectionSystemRecord systemRecord() {
		return new ProjectionSystemRecord(SYSTEM, 2, List.of(MAP_H, MAP_V),
				List.of(RELATION), "model-unit", 1e-9, 2e-9, 3e-12,
				4e-9, 5e-9, 1e10, 8);
	}

	private static ProjectionDiagramMapRecord mapRecord(ProjectionDiagramMapId id,
			ProjectionFrameId frame) {
		return new ProjectionDiagramMapRecord(id, 2, SYSTEM, frame,
				ProjectionFrameUseRole.DEFINING, "ORIENTED_ISOMETRY", "PRESERVING",
				"model-unit", "EXACT", geoId(10), geoId(11), geoId(12), geoId(13),
				geoId(14), geoId(15), geoId(16), List.of(RELATION), 9);
	}

	private static ProjectionFrameRelationRecord hingeRecord() {
		return new ProjectionFrameRelationRecord(RELATION, 2, SYSTEM, MAP_H, MAP_V,
				ProjectionFrameRelationRecord.HINGE_UNFOLD, geoId(19), geoId(20),
				ProjectionFrameRelationRecord.POSITIVE_ORIENTATION,
				ProjectionFrameRelationRecord.EXPLICIT_CONSTRUCTION, geoId(17), 10);
	}

	private static ProjectionBindingRecord bindingRecord(ProjectionBindingId id,
			ProjectionDiagramMapId map, ProjectionFrameId frame, int pointIndex) {
		return new ProjectionBindingRecord(id, 2, OBJECT, SYSTEM, map, frame,
				ProjectionBindingRole.DEFINING, "POINT", "POINT",
				SpatialObjectRecord.POINT_SCHEMA_ID, 1, geoId(pointIndex), "EXACT",
				"NOT_REQUIRED", 11);
	}

	private static SpatialObjectRecord objectRecord() {
		return new SpatialObjectRecord(OBJECT, 2, "POINT",
				EditAuthorityMode.PROJECTION_DEFINED,
				SpatialObjectRecord.POINT_SCHEMA_ID, 1, SYSTEM,
				List.of(BINDING_H, BINDING_V), 12, 13);
	}

	private static void assertRoundTrip(String elementName,
			SpatialIdentityRecord expected, Map<String, String> attributes) {
		SpatialIdentityRecord parsed = SpatialRecordXmlCodec.parseRecord(
				elementName, attributes);
		assertEquals(expected.getClass(), parsed.getClass());
		assertEquals(SpatialRecordXmlCodec.writeRecord(expected),
				SpatialRecordXmlCodec.writeRecord(parsed));
	}

	private static Map<String, String> attributes(String... entries) {
		Map<String, String> result = new HashMap<>();
		for (int index = 0; index < entries.length; index += 2) {
			result.put(entries[index], entries[index + 1]);
		}
		return result;
	}

	private static PersistentGeoId geoId(int index) {
		return new PersistentGeoId(token(index));
	}

	private static ProjectionFrameId frameId(int index) {
		return new ProjectionFrameId(token(index));
	}

	private static ProjectionSystemId systemId(int index) {
		return new ProjectionSystemId(token(index));
	}

	private static ProjectionDiagramMapId mapId(int index) {
		return new ProjectionDiagramMapId(token(index));
	}

	private static ProjectionFrameRelationId relationId(int index) {
		return new ProjectionFrameRelationId(token(index));
	}

	private static SpatialObjectId objectId(int index) {
		return new SpatialObjectId(token(index));
	}

	private static ProjectionBindingId bindingId(int index) {
		return new ProjectionBindingId(token(index));
	}

	private static String token(int index) {
		return String.format("%032x", index);
	}
}
