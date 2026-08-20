/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.geocedg.common.kernel.spatial.identity.EditAuthorityMode;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
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
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityGraph;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectId;
import org.geocedg.common.kernel.spatial.identity.SpatialObjectRecord;
import org.geocedg.common.kernel.spatial.identity.SpatialRecordResolution;
import org.geocedg.common.kernel.spatial.identity.SpatialResolutionState;
import org.geocedg.common.kernel.spatial.semantic.CorrespondenceStatus;
import org.geocedg.common.kernel.spatial.semantic.DiagramMapDefinition;
import org.geocedg.common.kernel.spatial.semantic.DiagramMapFamily;
import org.geocedg.common.kernel.spatial.semantic.DiagramOrientation;
import org.geocedg.common.kernel.spatial.semantic.FoldSide;
import org.geocedg.common.kernel.spatial.semantic.FrameHandedness;
import org.geocedg.common.kernel.spatial.semantic.NumericPolicy;
import org.geocedg.common.kernel.spatial.semantic.NumericalEvidenceStatus;
import org.geocedg.common.kernel.spatial.semantic.ProjectionDefinedPointEvaluator;
import org.geocedg.common.kernel.spatial.semantic.ProjectionFrameDefinition;
import org.geocedg.common.kernel.spatial.semantic.ProjectionFrameFamily;
import org.geocedg.common.kernel.spatial.semantic.ProjectionObservation;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationDefinition;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationKind;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationOrientation;
import org.geocedg.common.kernel.spatial.semantic.ProjectionRelationProvenance;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemCertificate;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemEvaluator;
import org.geocedg.common.kernel.spatial.semantic.ProjectionSystemStatus;
import org.geocedg.common.kernel.spatial.semantic.RepresentationFidelity;
import org.geocedg.common.kernel.spatial.semantic.SpatialCapabilityStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCertificateStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialCurrentnessStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialDefinitionStatus;
import org.geocedg.common.kernel.spatial.semantic.SpatialPointCertificate;
import org.geocedg.common.kernel.spatial.semantic.SpatialSemanticInstrumentation;
import org.geocedg.common.kernel.spatial.semantic.Vector2;
import org.geocedg.common.kernel.spatial.semantic.Vector3;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.kernel.kernelND.GeoVectorND;

/** ID-resolved, view-independent input snapshots for the normal-DAG pilot. */
final class SpatialSemanticInputs {
	private SpatialSemanticInputs() {
	}

	static SystemTopology systemTopology(SpatialIdentityGraph registry,
			ProjectionSystemRecord system) {
		return SystemTopology.create(registry, system);
	}

	static PointTopology pointTopology(SpatialIdentityGraph registry,
			SpatialObjectRecord object) {
		return PointTopology.create(registry, object);
	}

	static final class Snapshot {
		private final Map<SpatialIdentityId, String> revisionTuple;
		private final Map<PersistentGeoId, GeoValue> values;
		private final String valueToken;
		private final boolean currentTopology;

		private Snapshot(Map<SpatialIdentityId, String> revisionTuple,
				Map<PersistentGeoId, GeoValue> values, String valueToken,
				boolean currentTopology) {
			this.revisionTuple = revisionTuple;
			this.values = values;
			this.valueToken = valueToken;
			this.currentTopology = currentTopology;
		}

		Map<SpatialIdentityId, String> getRevisionTuple() {
			return revisionTuple;
		}

		String getValueToken() {
			return valueToken;
		}

		GeoValue value(PersistentGeoId id) {
			GeoValue value = values.get(id);
			return value == null ? GeoValue.missing() : value;
		}

		boolean isCurrentTopology() {
			return currentTopology;
		}

		boolean sameRevisionAndValues(Snapshot other) {
			return currentTopology && other.currentTopology
					&& revisionTuple.equals(other.revisionTuple)
					&& valueToken.equals(other.valueToken);
		}
	}

	private static final class TopologyOutcome {
		private String unsupportedReason;
		private String undefinedReason;
		private String inconsistentReason;

		void unsupported(String reason) {
			unsupportedReason = first(unsupportedReason, reason);
		}

		void undefined(String reason) {
			undefinedReason = first(undefinedReason, reason);
		}

		void inconsistent(String reason) {
			inconsistentReason = first(inconsistentReason, reason);
		}

		boolean isUnsupported() {
			return unsupportedReason != null;
		}

		ProjectionSystemStatus getSystemFailureStatus() {
			if (undefinedReason != null) {
				return ProjectionSystemStatus.UNDEFINED;
			}
			return inconsistentReason == null ? null
					: ProjectionSystemStatus.INCONSISTENT;
		}
	}

	private static final class PointOutcome {
		private boolean unsupported;
		private boolean undefined;

		void unsupported() {
			unsupported = true;
		}

		void undefined() {
			undefined = true;
		}
	}

	abstract static class Topology {
		final SpatialIdentityGraph registry;
		final Set<SpatialIdentityId> recordIds;
		final Set<PersistentGeoId> geoIds;
		final Set<SpatialIdentityId> structuralRecordIds;
		final String structureToken;
		final TopologyOutcome outcome;

		Topology(SpatialIdentityGraph registry, Set<SpatialIdentityId> recordIds,
				Set<PersistentGeoId> geoIds, TopologyOutcome outcome) {
			this(registry, recordIds, geoIds, outcome, Collections.emptySet());
		}

		Topology(SpatialIdentityGraph registry, Set<SpatialIdentityId> recordIds,
				Set<PersistentGeoId> geoIds, TopologyOutcome outcome,
				Set<SpatialIdentityId> structuralRecordIds) {
			this.registry = Objects.requireNonNull(registry);
			this.recordIds = immutableSortedSet(recordIds);
			this.geoIds = immutableSortedSet(geoIds);
			this.structuralRecordIds = immutableSortedSet(structuralRecordIds);
			this.structureToken = structureToken(registry, this.recordIds,
					this.structuralRecordIds);
			this.outcome = Objects.requireNonNull(outcome);
		}

		final String getStructureToken() {
			return structureToken;
		}

		final Set<SpatialIdentityId> getDependencyIdentities() {
			LinkedHashSet<SpatialIdentityId> result = new LinkedHashSet<>(recordIds);
			result.addAll(structuralRecordIds);
			result.addAll(geoIds);
			return Collections.unmodifiableSet(result);
		}

		final GeoElement[] getInputs() {
			ArrayList<GeoElement> result = new ArrayList<>();
			for (PersistentGeoId id : geoIds) {
				GeoElement geo = registry.getGeo(id);
				if (geo != null && !result.contains(geo)) {
					result.add(geo);
				}
			}
			return result.toArray(new GeoElement[0]);
		}

		final Snapshot capture() {
			TreeMap<SpatialIdentityId, String> revisions = new TreeMap<>();
			TreeSet<SpatialIdentityId> revisionIds = new TreeSet<>(recordIds);
			revisionIds.addAll(structuralRecordIds);
			for (SpatialIdentityId id : revisionIds) {
				revisions.put(id, revisionToken(registry.getRecord(id)) + ':'
						+ resolutionToken(registry.getResolution(id)) + ':'
						+ attachmentToken(registry, id));
			}
			TreeMap<PersistentGeoId, GeoValue> values = new TreeMap<>();
			StringBuilder valueToken = new StringBuilder();
			for (PersistentGeoId id : geoIds) {
				GeoValue value = GeoValue.capture(registry.getGeo(id));
				values.put(id, value);
				valueToken.append(id.toExternalForm()).append('=').append(value.token())
						.append(';');
			}
			boolean current = structureToken.equals(structureToken(registry, recordIds,
					structuralRecordIds));
			return new Snapshot(Collections.unmodifiableMap(revisions),
					Collections.unmodifiableMap(values), valueToken.toString(), current);
		}
	}

	static final class SystemTopology extends Topology {
		private final ProjectionSystemRecord system;
		private final Map<ProjectionFrameId, ProjectionFrameRecord> frames;
		private final Map<ProjectionDiagramMapId, ProjectionDiagramMapRecord> maps;
		private final List<ProjectionFrameRelationRecord> relations;

		private SystemTopology(SpatialIdentityGraph registry,
				ProjectionSystemRecord system, Set<SpatialIdentityId> recordIds,
				Set<PersistentGeoId> geoIds, TopologyOutcome outcome,
				Map<ProjectionFrameId, ProjectionFrameRecord> frames,
				Map<ProjectionDiagramMapId, ProjectionDiagramMapRecord> maps,
				List<ProjectionFrameRelationRecord> relations) {
			super(registry, recordIds, geoIds, outcome);
			this.system = system;
			this.frames = frames;
			this.maps = maps;
			this.relations = relations;
		}

		static SystemTopology create(SpatialIdentityGraph registry,
				ProjectionSystemRecord system) {
			TreeSet<SpatialIdentityId> recordIds = new TreeSet<>();
			TreeSet<PersistentGeoId> geoIds = new TreeSet<>();
			LinkedHashMap<ProjectionFrameId, ProjectionFrameRecord> frames =
					new LinkedHashMap<>();
			LinkedHashMap<ProjectionDiagramMapId, ProjectionDiagramMapRecord> maps =
					new LinkedHashMap<>();
			ArrayList<ProjectionFrameRelationRecord> relations = new ArrayList<>();
			TopologyOutcome outcome = new TopologyOutcome();
			recordIds.add(system.getId());
			validateActiveV2(registry, system, outcome);
			for (ProjectionDiagramMapId mapId : system.getMapIds()) {
				recordIds.add(mapId);
				ProjectionDiagramMapRecord map = typedRecord(registry, mapId,
						ProjectionDiagramMapRecord.class);
				if (map == null) {
					outcome.undefined("Missing version-two diagram map " + mapId);
					continue;
				}
				validateActiveV2(registry, map, outcome);
				if (!system.getId().equals(map.getSystemId())) {
					outcome.inconsistent("Diagram map belongs to another system");
				}
				maps.put(mapId, map);
				addMapGeos(map, recordIds, geoIds);
				validateMapGeoRecords(registry, map, outcome);
				ProjectionFrameId frameId = map.getFrameId();
				recordIds.add(frameId);
				ProjectionFrameRecord frame = typedRecord(registry, frameId,
						ProjectionFrameRecord.class);
				if (frame == null) {
					outcome.undefined("Missing version-two projection frame " + frameId);
				} else {
					validateActiveV2(registry, frame, outcome);
					frames.put(frameId, frame);
					addFrameGeos(frame, recordIds, geoIds);
					validateFrameGeoRecords(registry, frame, outcome);
				}
				validateFrameAndMapTokens(frame, map, outcome);
			}
			for (ProjectionFrameRelationId relationId : system.getRelationIds()) {
				recordIds.add(relationId);
				ProjectionFrameRelationRecord relation = typedRecord(registry, relationId,
						ProjectionFrameRelationRecord.class);
				if (relation == null) {
					outcome.undefined("Missing version-two frame relation " + relationId);
					continue;
				}
				validateActiveV2(registry, relation, outcome);
				ProjectionDiagramMapRecord sourceMap = maps.get(
						relation.getSourceMapId());
				ProjectionDiagramMapRecord destinationMap = maps.get(
						relation.getDestinationMapId());
				if (!system.getId().equals(relation.getSystemId())
						|| sourceMap == null || destinationMap == null
						|| !sourceMap.getRelationIds().contains(relationId)
						|| !destinationMap.getRelationIds().contains(relationId)) {
					outcome.inconsistent(
							"Frame relation has incoherent system/map membership");
				}
				if (ProjectionFrameRelationRecord.CHANGE_OF_PLANE.equals(
						relation.getRelationKind()) && destinationMap != null
						&& destinationMap.getFrameUseRole()
								!= ProjectionFrameUseRole.AUXILIARY) {
					outcome.inconsistent(
							"Change-of-plane destination map must be AUXILIARY");
				}
				relations.add(relation);
				addRelationGeos(relation, recordIds, geoIds);
				validateRelationGeoRecords(registry, relation, outcome);
				validateRelationToken(relation, outcome);
			}
			for (ProjectionDiagramMapRecord map : maps.values()) {
				for (ProjectionFrameRelationId relationId : map.getRelationIds()) {
					recordIds.add(relationId);
					ProjectionFrameRelationRecord relation = typedRecord(registry,
							relationId, ProjectionFrameRelationRecord.class);
					if (!system.getRelationIds().contains(relationId)) {
						outcome.inconsistent(
								"Diagram map has incoherent relation membership");
					} else if (relation == null) {
						outcome.undefined("Diagram map relation is unresolved");
					} else if (!system.getId().equals(relation.getSystemId())
							|| !(map.getId().equals(relation.getSourceMapId())
									|| map.getId().equals(
											relation.getDestinationMapId()))) {
						outcome.inconsistent(
								"Diagram map has incoherent relation membership");
					}
				}
			}
			return new SystemTopology(registry, system, recordIds, geoIds, outcome,
					Collections.unmodifiableMap(frames),
					Collections.unmodifiableMap(maps),
					Collections.unmodifiableList(relations));
		}

		ProjectionSystemId getSystemId() {
			return system.getId();
		}

		ProjectionSystemCertificate evaluate(Snapshot snapshot,
				SpatialSemanticInstrumentation instrumentation) {
			if (!snapshot.isCurrentTopology()) {
				return invalidatedSystem();
			}
			if (outcome.isUnsupported()) {
				return unsupportedSystem();
			}
			ProjectionSystemStatus topologyFailure = outcome.getSystemFailureStatus();
			if (topologyFailure != null) {
				return systemFailure(topologyFailure);
			}
			NumericPolicy policy = policy(system);
			LinkedHashMap<ProjectionFrameId, ProjectionFrameDefinition> definitions =
					frameDefinitions(frames, snapshot);
			LinkedHashMap<ProjectionDiagramMapId, DiagramMapDefinition> mapDefinitions =
					mapDefinitions(maps, frames, snapshot);
			ProjectionSystemStatus precondition = systemPrecondition(system, maps,
					definitions, mapDefinitions, relations, snapshot, policy);
			if (precondition != ProjectionSystemStatus.CONSISTENT) {
				return systemFailure(precondition);
			}
			List<ProjectionRelationDefinition> relationDefinitions = relationDefinitions(
					relations, maps, definitions, mapDefinitions, snapshot, policy);
			if (relationDefinitions == null) {
				return systemFailure(ProjectionSystemStatus.INCONSISTENT);
			}
			return new ProjectionSystemEvaluator().evaluate(
					new ArrayList<>(definitions.values()),
					new ArrayList<>(mapDefinitions.values()), relationDefinitions, policy,
					instrumentation);
		}
	}

	static final class PointTopology extends Topology {
		private final SpatialObjectRecord object;
		private final ProjectionSystemRecord system;
		private final Map<ProjectionBindingId, ProjectionBindingRecord> bindings;
		private final Map<ProjectionFrameId, ProjectionFrameRecord> frames;
		private final Map<ProjectionDiagramMapId, ProjectionDiagramMapRecord> maps;
		private final List<ProjectionFrameRelationRecord> relations;
		private final PointOutcome pointOutcome;
		private final Set<ProjectionBindingId> undefinedBindings;

		private PointTopology(SpatialIdentityGraph registry,
				SpatialObjectRecord object, ProjectionSystemRecord system,
				Set<SpatialIdentityId> recordIds, Set<PersistentGeoId> geoIds,
				TopologyOutcome outcome,
				Map<ProjectionBindingId, ProjectionBindingRecord> bindings,
				Map<ProjectionFrameId, ProjectionFrameRecord> frames,
				Map<ProjectionDiagramMapId, ProjectionDiagramMapRecord> maps,
				List<ProjectionFrameRelationRecord> relations,
				Set<SpatialIdentityId> structuralRecordIds,
				PointOutcome pointOutcome,
				Set<ProjectionBindingId> undefinedBindings) {
			super(registry, recordIds, geoIds, outcome, structuralRecordIds);
			this.object = object;
			this.system = system;
			this.bindings = bindings;
			this.frames = frames;
			this.maps = maps;
			this.relations = relations;
			this.pointOutcome = pointOutcome;
			this.undefinedBindings = undefinedBindings;
		}

		static PointTopology create(SpatialIdentityGraph registry,
				SpatialObjectRecord object) {
			TreeSet<SpatialIdentityId> recordIds = new TreeSet<>();
			TreeSet<PersistentGeoId> geoIds = new TreeSet<>();
			LinkedHashMap<ProjectionBindingId, ProjectionBindingRecord> bindings =
					new LinkedHashMap<>();
			LinkedHashMap<ProjectionFrameId, ProjectionFrameRecord> frames =
					new LinkedHashMap<>();
			LinkedHashMap<ProjectionDiagramMapId, ProjectionDiagramMapRecord> maps =
					new LinkedHashMap<>();
			ArrayList<ProjectionFrameRelationRecord> relations = new ArrayList<>();
			TreeSet<SpatialIdentityId> structuralRecordIds = new TreeSet<>();
			TreeSet<ProjectionBindingId> undefinedBindings = new TreeSet<>();
			TopologyOutcome outcome = new TopologyOutcome();
			PointOutcome pointOutcome = new PointOutcome();
			recordIds.add(object.getId());
			validateActiveV2(registry, object, outcome);
			if (!SpatialObjectRecord.POINT_TYPE.equals(object.getSpatialType())
					|| object.getAuthority() != EditAuthorityMode.PROJECTION_DEFINED
					|| !SpatialObjectRecord.POINT_SCHEMA_ID.equals(object.getSchemaId())
					|| object.getSchemaVersion()
							!= SpatialObjectRecord.POINT_SCHEMA_VERSION) {
				outcome.unsupported(
						"Only the projection-defined POINT schema is admitted");
			}
			ProjectionSystemRecord system = null;
			if (object.getSystemId() != null) {
				recordIds.add(object.getSystemId());
				system = typedRecord(registry, object.getSystemId(),
						ProjectionSystemRecord.class);
			}
			if (system == null) {
				outcome.undefined("Point object has no resolved version-two system");
			} else {
				validateActiveV2(registry, system, outcome);
			}
			for (ProjectionBindingId bindingId : object.getBindingIds()) {
				ProjectionBindingRecord binding = typedRecord(registry, bindingId,
						ProjectionBindingRecord.class);
				if (binding == null) {
					structuralRecordIds.add(bindingId);
					pointOutcome.undefined();
					continue;
				}
				if (binding.getRole() != ProjectionBindingRole.DEFINING) {
					if (binding.getRole() == ProjectionBindingRole.DERIVED) {
						pointOutcome.undefined();
					}
					validatePassiveBinding(registry, object, system, binding,
							structuralRecordIds, pointOutcome);
					continue;
				}
				recordIds.add(bindingId);
				if (!object.getId().equals(binding.getObjectId())
						|| system == null
						|| !system.getId().equals(binding.getSystemId())) {
					outcome.inconsistent(
							"Point binding belongs to another object or system");
				}
				validateActiveV2(registry, binding, outcome);
				validateBindingTokens(binding, outcome);
				addGeo(binding.getProjectedPointGeoId(), recordIds, geoIds);
				GeoInputState projectedState = definingGeoState(registry,
						binding.getProjectedPointGeoId());
				if (projectedState != GeoInputState.VALID) {
					undefinedBindings.add(bindingId);
				}
				if (projectedState == GeoInputState.MISSING) {
					outcome.undefined("Defining projected identity is unresolved");
				}
				ProjectionDiagramMapRecord map = typedRecord(registry,
						binding.getDiagramMapId(), ProjectionDiagramMapRecord.class);
				ProjectionFrameRecord frame = typedRecord(registry, binding.getFrameId(),
						ProjectionFrameRecord.class);
				recordIds.add(binding.getDiagramMapId());
				recordIds.add(binding.getFrameId());
				if (map == null || frame == null || system == null) {
					outcome.undefined(
							"Defining binding has an unresolved map/frame reference");
					continue;
				}
				if (!system.getMapIds().contains(binding.getDiagramMapId())
						|| !map.getFrameId().equals(binding.getFrameId())
						|| !map.getSystemId().equals(system.getId())) {
					outcome.inconsistent(
							"Defining binding has an incoherent map/frame membership");
					continue;
				}
				validateActiveV2(registry, map, outcome);
				validateActiveV2(registry, frame, outcome);
				addMapGeos(map, recordIds, geoIds);
				addFrameGeos(frame, recordIds, geoIds);
				validateMapGeoRecords(registry, map, outcome);
				validateFrameGeoRecords(registry, frame, outcome);
				validateFrameAndMapTokens(frame, map, outcome);
				bindings.put(bindingId, binding);
				maps.put(map.getId(), map);
				frames.put(frame.getId(), frame);
			}
			if (system != null) {
				relations.addAll(closePointRelationSubcontext(registry, system, maps,
						frames, recordIds, geoIds, outcome));
			}
			return new PointTopology(registry, object, system, recordIds, geoIds,
					outcome, Collections.unmodifiableMap(bindings),
					Collections.unmodifiableMap(frames),
					Collections.unmodifiableMap(maps),
					Collections.unmodifiableList(relations),
					immutableSortedSet(structuralRecordIds), pointOutcome,
					immutableSortedSet(undefinedBindings));
		}

		SpatialObjectId getObjectId() {
			return object.getId();
		}

		ProjectionSystemId getSystemId() {
			return object.getSystemId();
		}

		SpatialPointCertificate evaluate(Snapshot snapshot,
				SpatialSemanticInstrumentation instrumentation) {
			if (!snapshot.isCurrentTopology()) {
				return invalidatedPoint();
			}
			if (outcome.isUnsupported()) {
				return unsupportedPoint();
			}
			if (system == null) {
				return pointFailure(systemFailure(ProjectionSystemStatus.UNDEFINED),
						maximumRevision(snapshot));
			}
			ProjectionSystemStatus topologyFailure = outcome.getSystemFailureStatus();
			if (topologyFailure != null) {
				return pointFailure(systemFailure(topologyFailure),
						maximumRevision(snapshot));
			}
			NumericPolicy policy = policy(system);
			LinkedHashMap<ProjectionFrameId, ProjectionFrameDefinition> definitions =
					frameDefinitions(frames, snapshot);
			LinkedHashMap<ProjectionDiagramMapId, DiagramMapDefinition> mapDefinitions =
					mapDefinitions(maps, frames, snapshot);
			ProjectionSystemStatus precondition = systemPrecondition(system, maps,
					definitions, mapDefinitions, relations, snapshot, policy);
			if (precondition != ProjectionSystemStatus.CONSISTENT) {
				return pointFailure(systemFailure(precondition),
						maximumRevision(snapshot));
			}
			List<ProjectionRelationDefinition> relationDefinitions = relationDefinitions(
					relations, maps, definitions, mapDefinitions, snapshot, policy);
			if (relationDefinitions == null) {
				return pointFailure(systemFailure(ProjectionSystemStatus.INCONSISTENT),
						maximumRevision(snapshot));
			}
			if (pointOutcome.unsupported || pointOutcome.undefined) {
				ProjectionSystemCertificate systemCertificate =
						new ProjectionSystemEvaluator().evaluate(
								new ArrayList<>(definitions.values()),
								new ArrayList<>(mapDefinitions.values()),
								relationDefinitions, policy, instrumentation);
				if (systemCertificate.getStatus()
						!= ProjectionSystemStatus.CONSISTENT) {
					return pointFailure(systemCertificate, maximumRevision(snapshot));
				}
				if (pointOutcome.unsupported) {
					return unsupportedPoint(systemCertificate, maximumRevision(snapshot));
				}
				return pointObjectFailure(systemCertificate, maximumRevision(snapshot));
			}
			ArrayList<ProjectionObservation> observations = new ArrayList<>();
			for (ProjectionBindingRecord binding : bindings.values()) {
				GeoValue projected = snapshot.value(binding.getProjectedPointGeoId());
				ProjectionDiagramMapRecord map = maps.get(binding.getDiagramMapId());
				ProjectionFrameRecord frame = frames.get(binding.getFrameId());
				SpatialDefinitionStatus definitionStatus = projected.isDiagramPoint()
						&& !undefinedBindings.contains(binding.getId())
								? SpatialDefinitionStatus.DEFINED
						: SpatialDefinitionStatus.UNDEFINED;
				observations.add(new ProjectionObservation(
						binding.getId().toExternalForm(),
						definitions.get(binding.getFrameId()),
						mapDefinitions.get(binding.getDiagramMapId()),
						projected.diagramPoint(), definitionStatus,
						observationFidelity(binding, map, frame),
						parse(CorrespondenceStatus.class, binding.getCorrespondence()),
						Math.max(binding.getRevision(), maximumRevision(snapshot))));
			}
			return new ProjectionDefinedPointEvaluator().evaluate(observations,
					relationDefinitions, policy, instrumentation);
		}
	}

	private static List<ProjectionFrameRelationRecord>
			closePointRelationSubcontext(SpatialIdentityGraph registry,
					ProjectionSystemRecord system,
					Map<ProjectionDiagramMapId, ProjectionDiagramMapRecord> maps,
					Map<ProjectionFrameId, ProjectionFrameRecord> frames,
					Set<SpatialIdentityId> recordIds, Set<PersistentGeoId> geoIds,
					TopologyOutcome outcome) {
		ArrayList<ProjectionDiagramMapId> pendingMaps = new ArrayList<>(maps.keySet());
		LinkedHashSet<ProjectionDiagramMapId> processedMaps = new LinkedHashSet<>();
		LinkedHashMap<ProjectionFrameRelationId, ProjectionFrameRelationRecord>
				relations = new LinkedHashMap<>();
		for (int index = 0; index < pendingMaps.size(); index++) {
			ProjectionDiagramMapId currentMapId = pendingMaps.get(index);
			if (!processedMaps.add(currentMapId)) {
				continue;
			}
			ProjectionDiagramMapRecord currentMap = maps.get(currentMapId);
			if (currentMap == null) {
				continue;
			}
			for (ProjectionFrameRelationId relationId : currentMap.getRelationIds()) {
				recordIds.add(relationId);
				if (!system.getRelationIds().contains(relationId)) {
					outcome.inconsistent(
							"Point map has incoherent relation membership");
					continue;
				}
				ProjectionFrameRelationRecord relation = typedRecord(registry,
						relationId, ProjectionFrameRelationRecord.class);
				if (relation == null) {
					outcome.undefined("Point map relation is unresolved");
					continue;
				}
				validateActiveV2(registry, relation, outcome);
				if (!system.getId().equals(relation.getSystemId())
						|| !(currentMapId.equals(relation.getSourceMapId())
								|| currentMapId.equals(relation.getDestinationMapId()))) {
					outcome.inconsistent(
							"Point map has incoherent relation membership");
					continue;
				}
				ProjectionDiagramMapRecord sourceMap = typedRecord(registry,
						relation.getSourceMapId(), ProjectionDiagramMapRecord.class);
				ProjectionDiagramMapRecord destinationMap = typedRecord(registry,
						relation.getDestinationMapId(), ProjectionDiagramMapRecord.class);
				recordIds.add(relation.getSourceMapId());
				recordIds.add(relation.getDestinationMapId());
				if (sourceMap == null || destinationMap == null) {
					outcome.undefined("Point relation map is unresolved");
					continue;
				}
				validateActiveV2(registry, sourceMap, outcome);
				validateActiveV2(registry, destinationMap, outcome);
				if (!system.getMapIds().contains(sourceMap.getId())
						|| !system.getMapIds().contains(destinationMap.getId())
						|| !system.getId().equals(sourceMap.getSystemId())
						|| !system.getId().equals(destinationMap.getSystemId())
						|| !sourceMap.getRelationIds().contains(relationId)
						|| !destinationMap.getRelationIds().contains(relationId)) {
					outcome.inconsistent(
							"Point relation has incoherent system/map membership");
					continue;
				}
				if (ProjectionFrameRelationRecord.CHANGE_OF_PLANE.equals(
						relation.getRelationKind())
						&& destinationMap.getFrameUseRole()
								!= ProjectionFrameUseRole.AUXILIARY) {
					outcome.inconsistent(
							"Change-of-plane destination map must be AUXILIARY");
				}
				addPointRelationMap(registry, sourceMap, maps, frames, pendingMaps,
						recordIds, geoIds, outcome);
				addPointRelationMap(registry, destinationMap, maps, frames, pendingMaps,
						recordIds, geoIds, outcome);
				if (relations.putIfAbsent(relationId, relation) == null) {
					validateRelationToken(relation, outcome);
					addRelationGeos(relation, recordIds, geoIds);
					validateRelationGeoRecords(registry, relation, outcome);
				}
			}
		}
		return new ArrayList<>(relations.values());
	}

	private static void addPointRelationMap(SpatialIdentityGraph registry,
			ProjectionDiagramMapRecord map,
			Map<ProjectionDiagramMapId, ProjectionDiagramMapRecord> maps,
			Map<ProjectionFrameId, ProjectionFrameRecord> frames,
			List<ProjectionDiagramMapId> pendingMaps,
			Set<SpatialIdentityId> recordIds, Set<PersistentGeoId> geoIds,
			TopologyOutcome outcome) {
		recordIds.add(map.getId());
		if (maps.putIfAbsent(map.getId(), map) == null) {
			pendingMaps.add(map.getId());
		}
		addMapGeos(map, recordIds, geoIds);
		validateMapGeoRecords(registry, map, outcome);
		ProjectionFrameId frameId = map.getFrameId();
		recordIds.add(frameId);
		ProjectionFrameRecord frame = typedRecord(registry, frameId,
				ProjectionFrameRecord.class);
		if (frame == null) {
			outcome.undefined("Point relation frame is unresolved");
			validateFrameAndMapTokens(null, map, outcome);
			return;
		}
		validateActiveV2(registry, frame, outcome);
		frames.putIfAbsent(frameId, frame);
		addFrameGeos(frame, recordIds, geoIds);
		validateFrameGeoRecords(registry, frame, outcome);
		validateFrameAndMapTokens(frame, map, outcome);
	}

	private static RepresentationFidelity observationFidelity(
			ProjectionBindingRecord binding, ProjectionDiagramMapRecord map,
			ProjectionFrameRecord frame) {
		if (parse(RepresentationFidelity.class, binding.getFidelity())
				== RepresentationFidelity.DISCRETE
				|| parse(RepresentationFidelity.class, map.getFidelity())
						== RepresentationFidelity.DISCRETE
				|| parse(RepresentationFidelity.class, frame.getFidelity())
						== RepresentationFidelity.DISCRETE) {
			return RepresentationFidelity.DISCRETE;
		}
		return RepresentationFidelity.NUMERICAL;
	}

	static ProjectionSystemCertificate invalidatedSystem() {
		return new ProjectionSystemCertificate(SpatialCapabilityStatus.SUPPORTED,
				ProjectionSystemStatus.NOT_EVALUATED, Collections.emptyList(), 0, 0,
				Double.NaN);
	}

	static SpatialPointCertificate invalidatedPoint() {
		return new SpatialPointCertificate(SpatialCapabilityStatus.SUPPORTED,
				invalidatedSystem(), SpatialDefinitionStatus.UNDEFINED,
				SpatialCertificateStatus.UNDEFINED,
				SpatialCurrentnessStatus.INVALIDATED,
				RepresentationFidelity.NUMERICAL,
				NumericalEvidenceStatus.UNRESOLVED,
				CorrespondenceStatus.NOT_REQUIRED, 0, Optional.empty(), 0,
				new double[0], Double.NaN, Collections.emptyList(),
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"not evaluated for current revision");
	}

	private static ProjectionSystemCertificate unsupportedSystem() {
		return new ProjectionSystemCertificate(SpatialCapabilityStatus.UNSUPPORTED,
				ProjectionSystemStatus.NOT_EVALUATED, Collections.emptyList(), 0, 0,
				Double.NaN);
	}

	private static SpatialPointCertificate unsupportedPoint() {
		return new SpatialPointCertificate(SpatialCapabilityStatus.UNSUPPORTED,
				unsupportedSystem(), SpatialDefinitionStatus.UNDEFINED,
				SpatialCertificateStatus.NOT_EVALUATED,
				SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				RepresentationFidelity.NUMERICAL,
				NumericalEvidenceStatus.UNRESOLVED,
				CorrespondenceStatus.NOT_REQUIRED, 0, Optional.empty(), 0,
				new double[0], Double.NaN, Collections.emptyList(),
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"unsupported G9A2 point context");
	}

	private static SpatialPointCertificate unsupportedPoint(
			ProjectionSystemCertificate systemCertificate, long revision) {
		return new SpatialPointCertificate(SpatialCapabilityStatus.UNSUPPORTED,
				systemCertificate, SpatialDefinitionStatus.UNDEFINED,
				SpatialCertificateStatus.NOT_EVALUATED,
				SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				RepresentationFidelity.NUMERICAL,
				NumericalEvidenceStatus.UNRESOLVED,
				CorrespondenceStatus.NOT_REQUIRED, revision, Optional.empty(), 0,
				new double[0], Double.NaN, Collections.emptyList(),
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"unsupported passive point binding");
	}

	private static SpatialPointCertificate pointObjectFailure(
			ProjectionSystemCertificate systemCertificate, long revision) {
		return new SpatialPointCertificate(SpatialCapabilityStatus.SUPPORTED,
				systemCertificate, SpatialDefinitionStatus.UNDEFINED,
				SpatialCertificateStatus.UNDEFINED,
				SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				RepresentationFidelity.NUMERICAL,
				NumericalEvidenceStatus.UNRESOLVED,
				CorrespondenceStatus.NOT_REQUIRED, revision, Optional.empty(), 0,
				new double[0], Double.NaN, Collections.emptyList(),
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"passive point binding is structurally incoherent");
	}

	private static ProjectionSystemCertificate systemFailure(
			ProjectionSystemStatus status) {
		return new ProjectionSystemCertificate(SpatialCapabilityStatus.SUPPORTED,
				status, Collections.emptyList(), 0, 0, Double.POSITIVE_INFINITY);
	}

	private static SpatialPointCertificate pointFailure(
			ProjectionSystemCertificate systemCertificate, long revision) {
		ProjectionSystemStatus systemStatus = systemCertificate.getStatus();
		SpatialCertificateStatus certificateStatus = systemStatus
				== ProjectionSystemStatus.DEGENERATE
						? SpatialCertificateStatus.DEGENERATE
						: SpatialCertificateStatus.UNDEFINED;
		SpatialDefinitionStatus definitionStatus;
		if (systemStatus == ProjectionSystemStatus.DEGENERATE) {
			definitionStatus = SpatialDefinitionStatus.DEGENERATE;
		} else if (systemStatus == ProjectionSystemStatus.UNDEFINED
				|| systemStatus == ProjectionSystemStatus.NOT_EVALUATED) {
			definitionStatus = SpatialDefinitionStatus.UNDEFINED;
		} else {
			definitionStatus = SpatialDefinitionStatus.DEFINED;
		}
		return new SpatialPointCertificate(SpatialCapabilityStatus.SUPPORTED,
				systemCertificate, definitionStatus,
				certificateStatus, SpatialCurrentnessStatus.FAILED_CURRENT_REVISION,
				RepresentationFidelity.NUMERICAL,
				NumericalEvidenceStatus.UNRESOLVED,
				CorrespondenceStatus.NOT_REQUIRED, revision, Optional.empty(), 0,
				new double[0], Double.NaN, Collections.emptyList(),
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"projection-system precondition");
	}

	private static LinkedHashMap<ProjectionFrameId, ProjectionFrameDefinition>
			frameDefinitions(Map<ProjectionFrameId, ProjectionFrameRecord> records,
					Snapshot snapshot) {
		LinkedHashMap<ProjectionFrameId, ProjectionFrameDefinition> result =
				new LinkedHashMap<>();
		for (ProjectionFrameRecord frame : records.values()) {
			result.put(frame.getId(), new ProjectionFrameDefinition(
					frame.getId().toExternalForm(),
					parse(ProjectionFrameFamily.class, frame.getFamily()),
					snapshot.value(frame.getOriginGeoId()).point3(),
					snapshot.value(frame.getUGeoId()).vector3(),
					snapshot.value(frame.getVGeoId()).vector3(),
					parse(FrameHandedness.class, frame.getHandedness()),
					frame.getUnits(), frame.getRevision()));
		}
		return result;
	}

	private static LinkedHashMap<ProjectionDiagramMapId, DiagramMapDefinition>
			mapDefinitions(Map<ProjectionDiagramMapId, ProjectionDiagramMapRecord> records,
					Map<ProjectionFrameId, ProjectionFrameRecord> frames,
					Snapshot snapshot) {
		LinkedHashMap<ProjectionDiagramMapId, DiagramMapDefinition> result =
				new LinkedHashMap<>();
		for (ProjectionDiagramMapRecord map : records.values()) {
			ProjectionFrameRecord frame = frames.get(map.getFrameId());
			String sourceUnit = frame == null ? map.getUnits() : frame.getUnits();
			result.put(map.getId(), new DiagramMapDefinition(
					map.getId().toExternalForm(),
					parse(DiagramMapFamily.class, map.getFamily()),
					snapshot.value(map.getA00GeoId()).number(),
					snapshot.value(map.getA01GeoId()).number(),
					snapshot.value(map.getA10GeoId()).number(),
					snapshot.value(map.getA11GeoId()).number(),
					new Vector2(snapshot.value(map.getB0GeoId()).number(),
							snapshot.value(map.getB1GeoId()).number()),
					snapshot.value(map.getDeclaredScaleGeoId()).number(),
					parse(DiagramOrientation.class, map.getOrientation()), sourceUnit,
					map.getUnits(), map.getRevision()));
		}
		return result;
	}

	private static ProjectionSystemStatus systemPrecondition(
			ProjectionSystemRecord system,
			Map<ProjectionDiagramMapId, ProjectionDiagramMapRecord> mapRecords,
			Map<ProjectionFrameId, ProjectionFrameDefinition> frames,
			Map<ProjectionDiagramMapId, DiagramMapDefinition> maps,
			List<ProjectionFrameRelationRecord> relations, Snapshot snapshot,
			NumericPolicy policy) {
		String worldUnit = null;
		for (ProjectionFrameDefinition frame : frames.values()) {
			if (worldUnit == null) {
				worldUnit = frame.getUnit();
			} else if (!worldUnit.equals(frame.getUnit())) {
				return ProjectionSystemStatus.INCONSISTENT;
			}
		}
		for (ProjectionDiagramMapRecord map : mapRecords.values()) {
			ProjectionFrameDefinition frame = frames.get(map.getFrameId());
			DiagramMapDefinition definition = maps.get(map.getId());
			if (frame == null || definition == null
					|| !frame.getUnit().equals(definition.getSourceUnit())
					|| !system.getUnits().equals(map.getUnits())
					|| !system.getUnits().equals(definition.getDiagramUnit())) {
				return ProjectionSystemStatus.INCONSISTENT;
			}
		}
		for (ProjectionFrameDefinition frame : frames.values()) {
			SpatialDefinitionStatus status = frame.validate(policy);
			if (status == SpatialDefinitionStatus.UNDEFINED) {
				return ProjectionSystemStatus.UNDEFINED;
			}
		}
		for (DiagramMapDefinition map : maps.values()) {
			if (map.validate(policy) == ProjectionSystemStatus.UNDEFINED) {
				return ProjectionSystemStatus.UNDEFINED;
			}
		}
		for (ProjectionFrameRelationRecord relation : relations) {
			if (!snapshot.value(relation.getSupportStartGeoId()).isSpatialPoint()
					|| !snapshot.value(relation.getSupportEndGeoId()).isSpatialPoint()) {
				return ProjectionSystemStatus.UNDEFINED;
			}
			if (ProjectionFrameRelationRecord.HINGE_UNFOLD.equals(
					relation.getRelationKind())) {
				double fold = snapshot.value(relation.getFoldSignGeoId()).number();
				if (!Double.isFinite(fold)) {
					return ProjectionSystemStatus.UNDEFINED;
				}
				if (Math.abs(Math.abs(fold) - 1) > policy.getHingeTolerance()) {
					return ProjectionSystemStatus.INCONSISTENT;
				}
			}
		}
		return ProjectionSystemStatus.CONSISTENT;
	}

	private static List<ProjectionRelationDefinition> relationDefinitions(
			List<ProjectionFrameRelationRecord> records,
			Map<ProjectionDiagramMapId, ProjectionDiagramMapRecord> mapRecords,
			Map<ProjectionFrameId, ProjectionFrameDefinition> frames,
			Map<ProjectionDiagramMapId, DiagramMapDefinition> maps,
			Snapshot snapshot, NumericPolicy policy) {
		ArrayList<ProjectionRelationDefinition> result = new ArrayList<>();
		for (ProjectionFrameRelationRecord relation : records) {
			ProjectionDiagramMapRecord sourceMapRecord = mapRecords.get(
					relation.getSourceMapId());
			ProjectionDiagramMapRecord destinationMapRecord = mapRecords.get(
					relation.getDestinationMapId());
			if (sourceMapRecord == null || destinationMapRecord == null) {
				return null;
			}
			ProjectionFrameDefinition sourceFrame = frames.get(
					sourceMapRecord.getFrameId());
			ProjectionFrameDefinition destinationFrame = frames.get(
					destinationMapRecord.getFrameId());
			DiagramMapDefinition sourceMap = maps.get(sourceMapRecord.getId());
			DiagramMapDefinition destinationMap = maps.get(destinationMapRecord.getId());
			Vector3 supportStart = snapshot.value(
					relation.getSupportStartGeoId()).point3();
			Vector3 supportEnd = snapshot.value(
					relation.getSupportEndGeoId()).point3();
			ProjectionRelationOrientation orientation = parse(
					ProjectionRelationOrientation.class, relation.getOrientation());
			ProjectionRelationProvenance provenance = parse(
					ProjectionRelationProvenance.class, relation.getProvenance());
			if (ProjectionFrameRelationRecord.HINGE_UNFOLD.equals(
					relation.getRelationKind())) {
				double fold = snapshot.value(relation.getFoldSignGeoId()).number();
				FoldSide side = fold >= 0 ? FoldSide.SAME_DIAGRAM_SIDE
						: FoldSide.OPPOSITE_DIAGRAM_SIDE;
				if (Math.abs(Math.abs(fold) - 1) > policy.getHingeTolerance()) {
					return null;
				}
				result.add(ProjectionRelationDefinition.hingeUnfold(
						relation.getId().toExternalForm(), sourceFrame, sourceMap,
						destinationFrame, destinationMap, supportStart, supportEnd,
						orientation, provenance, side, relation.getRevision()));
			} else {
				result.add(ProjectionRelationDefinition.changeOfPlane(
						relation.getId().toExternalForm(), sourceFrame, sourceMap,
						destinationFrame, destinationMap, supportStart, supportEnd,
						orientation, provenance, relation.getRevision()));
			}
		}
		return result;
	}

	private static NumericPolicy policy(ProjectionSystemRecord system) {
		return new NumericPolicy(system.getAbsoluteTolerance(),
				system.getRelativeTolerance(), system.getRankTolerance(),
				system.getMapTolerance(), system.getHingeTolerance(),
				system.getConditionLimit());
	}

	private static long maximumRevision(Snapshot snapshot) {
		long maximum = 0;
		for (String token : snapshot.getRevisionTuple().values()) {
			String[] components = token.split(":");
			for (int index = 1; index < components.length; index++) {
				try {
					maximum = Math.max(maximum,
							Long.parseLong(components[index]));
				} catch (NumberFormatException ignored) {
					// Non-numeric tuple components are type discriminators.
				}
			}
		}
		return maximum;
	}

	private static void addFrameGeos(ProjectionFrameRecord frame,
			Set<SpatialIdentityId> recordIds, Set<PersistentGeoId> geoIds) {
		if (frame == null) {
			return;
		}
		addGeo(frame.getOriginGeoId(), recordIds, geoIds);
		addGeo(frame.getUGeoId(), recordIds, geoIds);
		addGeo(frame.getVGeoId(), recordIds, geoIds);
	}

	private static void addMapGeos(ProjectionDiagramMapRecord map,
			Set<SpatialIdentityId> recordIds, Set<PersistentGeoId> geoIds) {
		addGeo(map.getA00GeoId(), recordIds, geoIds);
		addGeo(map.getA01GeoId(), recordIds, geoIds);
		addGeo(map.getA10GeoId(), recordIds, geoIds);
		addGeo(map.getA11GeoId(), recordIds, geoIds);
		addGeo(map.getB0GeoId(), recordIds, geoIds);
		addGeo(map.getB1GeoId(), recordIds, geoIds);
		addGeo(map.getDeclaredScaleGeoId(), recordIds, geoIds);
	}

	private static void addRelationGeos(ProjectionFrameRelationRecord relation,
			Set<SpatialIdentityId> recordIds, Set<PersistentGeoId> geoIds) {
		addGeo(relation.getSupportStartGeoId(), recordIds, geoIds);
		addGeo(relation.getSupportEndGeoId(), recordIds, geoIds);
		addGeo(relation.getFoldSignGeoId(), recordIds, geoIds);
	}

	private static void addGeo(PersistentGeoId id,
			Set<SpatialIdentityId> recordIds, Set<PersistentGeoId> geoIds) {
		if (id != null) {
			recordIds.add(id);
			geoIds.add(id);
		}
	}

	private enum GeoInputState {
		VALID,
		MISSING,
		INCOHERENT_AUTHORITY
	}

	private static void validateFrameGeoRecords(SpatialIdentityGraph registry,
			ProjectionFrameRecord frame, TopologyOutcome outcome) {
		validateSystemGeoRecord(registry, frame.getOriginGeoId(),
				"projection-frame origin", outcome);
		validateSystemGeoRecord(registry, frame.getUGeoId(),
				"projection-frame first axis", outcome);
		validateSystemGeoRecord(registry, frame.getVGeoId(),
				"projection-frame second axis", outcome);
	}

	private static void validateMapGeoRecords(SpatialIdentityGraph registry,
			ProjectionDiagramMapRecord map, TopologyOutcome outcome) {
		for (PersistentGeoId id : map.getDefinitionGeoIds()) {
			validateSystemGeoRecord(registry, id,
					"projection-diagram map coefficient", outcome);
		}
	}

	private static void validateRelationGeoRecords(SpatialIdentityGraph registry,
			ProjectionFrameRelationRecord relation, TopologyOutcome outcome) {
		validateSystemGeoRecord(registry, relation.getSupportStartGeoId(),
				"relation support start", outcome);
		validateSystemGeoRecord(registry, relation.getSupportEndGeoId(),
				"relation support end", outcome);
		if (relation.getFoldSignGeoId() != null) {
			validateSystemGeoRecord(registry, relation.getFoldSignGeoId(),
					"relation fold sign", outcome);
		}
	}

	private static void validateSystemGeoRecord(SpatialIdentityGraph registry,
			PersistentGeoId id, String context, TopologyOutcome outcome) {
		GeoInputState state = definingGeoState(registry, id);
		if (state == GeoInputState.MISSING) {
			outcome.undefined("Missing active geo identity for " + context);
		} else if (state == GeoInputState.INCOHERENT_AUTHORITY) {
			outcome.inconsistent("Incompatible geo authority/role for " + context);
		}
	}

	private static GeoInputState definingGeoState(SpatialIdentityGraph registry,
			PersistentGeoId id) {
		GeoIdentityRecord record = typedRecord(registry, id, GeoIdentityRecord.class);
		SpatialRecordResolution resolution = id == null ? null
				: registry.getResolution(id);
		if (record == null || resolution == null
				|| resolution.getState() != SpatialResolutionState.ACTIVE
				|| registry.getGeo(id) == null) {
			return GeoInputState.MISSING;
		}
		if (record.getAuthority() != EditAuthorityMode.PROJECTION_DEFINED
				|| record.getBindingRole() != ProjectionBindingRole.DEFINING) {
			return GeoInputState.INCOHERENT_AUTHORITY;
		}
		return GeoInputState.VALID;
	}

	private static void validateFrameAndMapTokens(ProjectionFrameRecord frame,
			ProjectionDiagramMapRecord map, TopologyOutcome outcome) {
		if (frame != null && (parse(ProjectionFrameFamily.class, frame.getFamily()) == null
				|| parse(FrameHandedness.class, frame.getHandedness()) == null
				|| parse(RepresentationFidelity.class, frame.getFidelity()) == null)) {
			outcome.unsupported("Unsupported projection-frame semantic token");
		}
		if (parse(DiagramMapFamily.class, map.getFamily()) == null
				|| parse(DiagramOrientation.class, map.getOrientation()) == null
				|| parse(RepresentationFidelity.class, map.getFidelity()) == null) {
			outcome.unsupported("Unsupported diagram-map semantic token");
		}
	}

	private static void validateRelationToken(
			ProjectionFrameRelationRecord relation, TopologyOutcome outcome) {
		if (parse(ProjectionRelationKind.class, relation.getRelationKind()) == null
				|| parse(ProjectionRelationOrientation.class,
						relation.getOrientation()) == null
				|| parse(ProjectionRelationProvenance.class,
						relation.getProvenance()) == null) {
			outcome.unsupported("Unsupported projection-relation semantic token");
		}
	}

	private static void validatePassiveBinding(SpatialIdentityGraph registry,
			SpatialObjectRecord object, ProjectionSystemRecord system,
			ProjectionBindingRecord binding,
			Set<SpatialIdentityId> structuralRecordIds, PointOutcome outcome) {
		structuralRecordIds.add(binding.getId());
		if (binding.getSemanticVersion() != 2 || !hasAdmittedBindingTokens(binding)) {
			outcome.unsupported();
			return;
		}
		SpatialRecordResolution bindingResolution = registry.getResolution(
				binding.getId());
		if (bindingResolution == null
				|| bindingResolution.getState() != SpatialResolutionState.ACTIVE) {
			outcome.undefined();
		}
		if (!object.getId().equals(binding.getObjectId()) || system == null
				|| !system.getId().equals(binding.getSystemId())) {
			outcome.undefined();
		}

		ProjectionDiagramMapRecord map = typedRecord(registry,
				binding.getDiagramMapId(), ProjectionDiagramMapRecord.class);
		ProjectionFrameRecord frame = typedRecord(registry, binding.getFrameId(),
				ProjectionFrameRecord.class);
		structuralRecordIds.add(binding.getDiagramMapId());
		structuralRecordIds.add(binding.getFrameId());
		if (map == null || frame == null) {
			outcome.undefined();
		} else {
			if (map.getSemanticVersion() != 2 || frame.getSemanticVersion() != 2) {
				outcome.unsupported();
			}
			SpatialRecordResolution mapResolution = registry.getResolution(map.getId());
			SpatialRecordResolution frameResolution = registry.getResolution(frame.getId());
			if (mapResolution == null || frameResolution == null
					|| mapResolution.getState() != SpatialResolutionState.ACTIVE
					|| frameResolution.getState() != SpatialResolutionState.ACTIVE) {
				outcome.undefined();
			}
			if (system == null || !system.getMapIds().contains(map.getId())
					|| !system.getId().equals(map.getSystemId())
					|| !map.getFrameId().equals(frame.getId())
					|| !binding.getFrameId().equals(frame.getId())) {
				outcome.undefined();
			}
		}

		PersistentGeoId projectedId = binding.getProjectedPointGeoId();
		structuralRecordIds.add(projectedId);
		GeoIdentityRecord projected = typedRecord(registry, projectedId,
				GeoIdentityRecord.class);
		SpatialRecordResolution projectedResolution = registry.getResolution(projectedId);
		if (projected == null || projectedResolution == null
				|| projectedResolution.getState() != SpatialResolutionState.ACTIVE
				|| registry.getGeo(projectedId) == null) {
			outcome.undefined();
		} else if (projected.getAuthority() != object.getAuthority()
				|| projected.getBindingRole() != binding.getRole()) {
			outcome.undefined();
		}
	}

	private static void validateBindingTokens(ProjectionBindingRecord binding,
			TopologyOutcome outcome) {
		if (!hasAdmittedBindingTokens(binding)) {
			outcome.unsupported("Unsupported projection-defined point binding");
		}
	}

	private static boolean hasAdmittedBindingTokens(ProjectionBindingRecord binding) {
		return SpatialObjectRecord.POINT_TYPE.equals(binding.getRepresentationType())
				&& SpatialObjectRecord.POINT_TYPE.equals(
						binding.getExpectedSpatialType())
				&& SpatialObjectRecord.POINT_SCHEMA_ID.equals(binding.getSchemaId())
				&& binding.getSchemaVersion()
						== SpatialObjectRecord.POINT_SCHEMA_VERSION
				&& parse(RepresentationFidelity.class, binding.getFidelity()) != null
				&& parse(CorrespondenceStatus.class,
						binding.getCorrespondence()) != null;
	}

	private static void validateActiveV2(SpatialIdentityGraph registry,
			SpatialIdentityRecord record, TopologyOutcome outcome) {
		if (record == null) {
			outcome.undefined("Required semantic record is missing");
			return;
		}
		if (record.getSemanticVersion() != 2) {
			outcome.unsupported("Only version-two semantic records are executable");
			return;
		}
		SpatialRecordResolution resolution = registry.getResolution(record.getId());
		if (resolution == null || resolution.getState() != SpatialResolutionState.ACTIVE) {
			outcome.undefined("Semantic record is not actively resolved");
		}
	}

	private static String first(String current, String candidate) {
		return current == null ? candidate : current;
	}

	private static <T extends SpatialIdentityRecord> T typedRecord(
			SpatialIdentityGraph registry, SpatialIdentityId id, Class<T> type) {
		SpatialIdentityRecord record = registry.getRecord(id);
		return type.isInstance(record) ? type.cast(record) : null;
	}

	private static <E extends Enum<E>> E parse(Class<E> type, String token) {
		if (token == null) {
			return null;
		}
		try {
			return Enum.valueOf(type, token);
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private static <T extends SpatialIdentityId> Set<T> immutableSortedSet(
			Set<T> source) {
		return Collections.unmodifiableSet(new LinkedHashSet<>(new TreeSet<>(source)));
	}

	private static String structureToken(SpatialIdentityGraph registry,
			Set<SpatialIdentityId> ids,
			Set<SpatialIdentityId> structuralRecordIds) {
		StringBuilder result = new StringBuilder();
		for (SpatialIdentityId id : ids) {
			result.append(id.toExternalForm()).append('=')
					.append(recordToken(registry.getRecord(id))).append('@')
					.append(resolutionToken(registry.getResolution(id))).append('@')
					.append(attachmentToken(registry, id)).append(';');
		}
		for (SpatialIdentityId id : structuralRecordIds) {
			result.append("structural:").append(id.toExternalForm()).append('=')
					.append(recordToken(registry.getRecord(id))).append('@')
					.append(resolutionToken(registry.getResolution(id))).append('@')
					.append(attachmentToken(registry, id)).append(';');
		}
		return result.toString();
	}

	private static String resolutionToken(SpatialRecordResolution resolution) {
		return resolution == null ? "MISSING" : resolution.getState().name();
	}

	private static String attachmentToken(SpatialIdentityGraph registry,
			SpatialIdentityId id) {
		if (!(id instanceof PersistentGeoId)) {
			return "NOT_APPLICABLE";
		}
		return registry.getGeo((PersistentGeoId) id) == null
				? "DETACHED" : "ATTACHED";
	}

	private static String revisionToken(SpatialIdentityRecord record) {
		if (record == null) {
			return "missing:0";
		}
		if (record instanceof GeoIdentityRecord) {
			GeoIdentityRecord geo = (GeoIdentityRecord) record;
			return "geo:" + geo.getDefinitionRevision() + ':'
					+ geo.getTopologyRevision();
		}
		if (record instanceof ProjectionFrameRecord) {
			return "frame:" + ((ProjectionFrameRecord) record).getRevision();
		}
		if (record instanceof ProjectionDiagramMapRecord) {
			return "map:" + ((ProjectionDiagramMapRecord) record).getRevision();
		}
		if (record instanceof ProjectionFrameRelationRecord) {
			return "relation:"
					+ ((ProjectionFrameRelationRecord) record).getRevision();
		}
		if (record instanceof ProjectionSystemRecord) {
			return "system:" + ((ProjectionSystemRecord) record).getRevision();
		}
		if (record instanceof ProjectionBindingRecord) {
			return "binding:" + ((ProjectionBindingRecord) record).getRevision();
		}
		if (record instanceof SpatialObjectRecord) {
			SpatialObjectRecord object = (SpatialObjectRecord) record;
			return "object:" + object.getDefinitionRevision() + ':'
					+ object.getTopologyRevision();
		}
		return record.getXmlElementName() + ":0";
	}

	private static String recordToken(SpatialIdentityRecord record) {
		if (record == null) {
			return "missing";
		}
		StringBuilder result = new StringBuilder(record.getXmlElementName())
				.append(':').append(record.getSemanticVersion()).append(':')
				.append(revisionToken(record)).append(':');
		if (record instanceof GeoIdentityRecord) {
			GeoIdentityRecord geo = (GeoIdentityRecord) record;
			result.append(geo.getProvider()).append(':').append(geo.getFamily())
					.append(':').append(geo.getSchemaId()).append(':')
					.append(geo.getSchemaVersion()).append(':').append(geo.getAuthority())
					.append(':').append(geo.getBindingRole()).append(':')
					.append(geo.getStableOutputRole()).append(':')
					.append(geo.getOutputCardinality());
			if (geo.getAuthority() == EditAuthorityMode.CONSTRUCTION_DEFINED) {
				appendIds(result, geo.getDependencies());
			}
		} else if (record instanceof ProjectionFrameRecord) {
			ProjectionFrameRecord frame = (ProjectionFrameRecord) record;
			appendIds(result, frame.getDefinitionGeoIds());
			result.append(frame.getFamily()).append(':').append(frame.getUnits())
					.append(':').append(frame.getHandedness()).append(':')
					.append(frame.getFidelity());
		} else if (record instanceof ProjectionDiagramMapRecord) {
			ProjectionDiagramMapRecord map = (ProjectionDiagramMapRecord) record;
			result.append(map.getSystemId()).append(':').append(map.getFrameId())
					.append(':').append(map.getFrameUseRole()).append(':')
					.append(map.getFamily()).append(':').append(map.getOrientation())
					.append(':').append(map.getUnits()).append(':')
					.append(map.getFidelity());
			appendIds(result, map.getDefinitionGeoIds());
			appendIds(result, map.getRelationIds());
		} else if (record instanceof ProjectionFrameRelationRecord) {
			ProjectionFrameRelationRecord relation =
					(ProjectionFrameRelationRecord) record;
			result.append(relation.getSystemId()).append(':')
					.append(relation.getSourceMapId()).append(':')
					.append(relation.getDestinationMapId()).append(':')
					.append(relation.getRelationKind()).append(':')
					.append(relation.getSupportStartGeoId()).append(':')
					.append(relation.getSupportEndGeoId()).append(':')
					.append(relation.getOrientation()).append(':')
					.append(relation.getProvenance()).append(':')
					.append(relation.getFoldSignGeoId());
		} else if (record instanceof ProjectionSystemRecord) {
			ProjectionSystemRecord system = (ProjectionSystemRecord) record;
			appendIds(result, system.getMapIds());
			appendIds(result, system.getRelationIds());
			result.append(system.getUnits()).append(':')
					.append(Double.doubleToLongBits(system.getAbsoluteTolerance()))
					.append(':').append(Double.doubleToLongBits(
							system.getRelativeTolerance()))
					.append(':').append(Double.doubleToLongBits(system.getRankTolerance()))
					.append(':').append(Double.doubleToLongBits(system.getMapTolerance()))
					.append(':').append(Double.doubleToLongBits(system.getHingeTolerance()))
					.append(':').append(Double.doubleToLongBits(system.getConditionLimit()));
		} else if (record instanceof ProjectionBindingRecord) {
			ProjectionBindingRecord binding = (ProjectionBindingRecord) record;
			result.append(binding.getObjectId()).append(':').append(binding.getSystemId())
					.append(':').append(binding.getDiagramMapId()).append(':')
					.append(binding.getFrameId()).append(':').append(binding.getRole())
					.append(':').append(binding.getRepresentationType()).append(':')
					.append(binding.getExpectedSpatialType()).append(':')
					.append(binding.getSchemaId()).append(':')
					.append(binding.getSchemaVersion()).append(':')
					.append(binding.getProjectedPointGeoId()).append(':')
					.append(binding.getFidelity()).append(':')
					.append(binding.getCorrespondence());
		} else if (record instanceof SpatialObjectRecord) {
			SpatialObjectRecord object = (SpatialObjectRecord) record;
			result.append(object.getSpatialType()).append(':')
					.append(object.getAuthority()).append(':').append(object.getSchemaId())
					.append(':').append(object.getSchemaVersion()).append(':')
					.append(object.getSystemId());
			appendIds(result, object.getBindingIds());
		}
		return result.toString();
	}

	private static void appendIds(StringBuilder target,
			List<? extends SpatialIdentityId> ids) {
		target.append('[');
		for (SpatialIdentityId id : ids) {
			target.append(id.toExternalForm()).append(',');
		}
		target.append(']');
	}

	private static final class GeoValue {
		private enum Kind {
			POINT,
			VECTOR,
			NUMBER,
			UNDEFINED,
			WRONG_TYPE,
			MISSING
		}

		private final Kind kind;
		private final double first;
		private final double second;
		private final double third;
		private final boolean threeDimensional;
		private final String hostType;

		private GeoValue(Kind kind, double first, double second, double third,
				boolean threeDimensional, String hostType) {
			this.kind = kind;
			this.first = first;
			this.second = second;
			this.third = third;
			this.threeDimensional = threeDimensional;
			this.hostType = hostType;
		}

		static GeoValue capture(GeoElement geo) {
			if (geo == null) {
				return missing();
			}
			String type = geo.getGeoClassType().name();
			if (!geo.isDefined()) {
				return new GeoValue(Kind.UNDEFINED, Double.NaN, Double.NaN, Double.NaN,
						geo.isGeoElement3D(), type);
			}
			if (geo instanceof GeoNumeric) {
				return new GeoValue(Kind.NUMBER, ((GeoNumeric) geo).getDouble(), 0, 0,
						false, type);
			}
			if (geo instanceof GeoPointND) {
				GeoPointND point = (GeoPointND) geo;
				return new GeoValue(Kind.POINT, point.getInhomX(), point.getInhomY(),
						point.getInhomZ(), geo.isGeoElement3D(), type);
			}
			if (geo instanceof GeoVectorND) {
				GeoVectorND vector = (GeoVectorND) geo;
				return new GeoValue(Kind.VECTOR, vector.getX(), vector.getY(),
						vector.getZ(), geo.isGeoElement3D(), type);
			}
			return new GeoValue(Kind.WRONG_TYPE, Double.NaN, Double.NaN, Double.NaN,
					geo.isGeoElement3D(), type);
		}

		static GeoValue missing() {
			return new GeoValue(Kind.MISSING, Double.NaN, Double.NaN, Double.NaN,
					false, "MISSING");
		}

		Vector3 point3() {
			return isSpatialPoint() ? new Vector3(first, second, third)
					: new Vector3(Double.NaN, Double.NaN, Double.NaN);
		}

		Vector3 vector3() {
			return isSpatialVector() ? new Vector3(first, second, third)
					: new Vector3(Double.NaN, Double.NaN, Double.NaN);
		}

		boolean isSpatialPoint() {
			return kind == Kind.POINT && threeDimensional
					&& Double.isFinite(first) && Double.isFinite(second)
					&& Double.isFinite(third);
		}

		boolean isSpatialVector() {
			return kind == Kind.VECTOR && threeDimensional
					&& Double.isFinite(first) && Double.isFinite(second)
					&& Double.isFinite(third);
		}

		boolean isDiagramPoint() {
			return kind == Kind.POINT && !threeDimensional
					&& Double.isFinite(first) && Double.isFinite(second);
		}

		Vector2 diagramPoint() {
			return isDiagramPoint() ? new Vector2(first, second)
					: new Vector2(Double.NaN, Double.NaN);
		}

		double number() {
			return kind == Kind.NUMBER ? first : Double.NaN;
		}

		String token() {
			return kind.name() + ':' + hostType + ':' + threeDimensional + ':'
					+ Long.toHexString(Double.doubleToLongBits(first)) + ':'
					+ Long.toHexString(Double.doubleToLongBits(second)) + ':'
					+ Long.toHexString(Double.doubleToLongBits(third));
		}
	}
}
