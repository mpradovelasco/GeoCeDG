/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.geogebra.common.kernel.geos.GeoElement;

/** UI-neutral typed lifecycle facade for the projection-defined POINT pilot. */
public final class SpatialPointLifecycleService {
	private final SpatialIdentityRegistry registry;

	public SpatialPointLifecycleService(SpatialIdentityRegistry registry) {
		this.registry = Objects.requireNonNull(registry);
	}

	/** @return prepared transaction for the supplied sealed mutation */
	public SpatialLifecycleTransaction prepareMutation(
			SpatialLifecycleMutation mutation) {
		return registry.prepareLifecycleMutation(mutation);
	}

	/** @return prepared explicit POINT migration transaction */
	public SpatialLifecycleTransaction prepareMigration(
			SpatialPointMigrationPlan plan) {
		return registry.preparePointMigration(plan);
	}

	/** @return prepared explicit spatial copy transaction */
	public SpatialLifecycleTransaction prepareCopy(SpatialCopyPlan plan) {
		return registry.prepareCopy(plan);
	}

	/** @return prepared transaction adding one POINT binding */
	public SpatialLifecycleTransaction prepareBindingAdd(
			SpatialObjectRecord currentObject, ProjectionBindingRecord newBinding,
			SpatialObjectRecord updatedObject, String provenanceToken) {
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.BINDING_ADD, provenanceToken)
				.replace(currentObject, updatedObject).create(newBinding).build());
	}

	/** @return prepared transaction removing one POINT binding */
	public SpatialLifecycleTransaction prepareBindingRemove(
			SpatialObjectRecord currentObject, ProjectionBindingRecord currentBinding,
			SpatialObjectRecord updatedObject, String provenanceToken) {
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.BINDING_REMOVE, provenanceToken)
				.replace(currentObject, updatedObject).retire(currentBinding).build());
	}

	/** @return prepared transaction replacing one binding under a fresh role */
	public SpatialLifecycleTransaction prepareBindingRerole(
			SpatialObjectRecord currentObject, ProjectionBindingRecord currentBinding,
			ProjectionBindingRecord replacementBinding,
			SpatialObjectRecord updatedObject, String provenanceToken) {
		GeoIdentityRecord projectedGeo = registry.getGeoRecord(
				currentBinding.getProjectedPointGeoId());
		if (projectedGeo == null) {
			throw new IllegalArgumentException(
					"Binding re-role requires its current projected geo identity");
		}
		GeoIdentityRecord updatedGeo = projectedGeo.withBindingRoleAndRevisions(
				replacementBinding.getRole(), increment(projectedGeo.getDefinitionRevision()),
				increment(projectedGeo.getTopologyRevision()));
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.BINDING_REROLE, provenanceToken)
				.replace(currentObject, updatedObject).retire(currentBinding)
				.create(replacementBinding).replace(projectedGeo, updatedGeo).build());
	}

	/** @return prepared transaction adding one map to the current system */
	public SpatialLifecycleTransaction prepareMapAdd(ProjectionSystemRecord currentSystem,
			ProjectionDiagramMapRecord newMap, ProjectionSystemRecord updatedSystem,
			String provenanceToken) {
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.MAP_ADD, provenanceToken)
				.replace(currentSystem, updatedSystem).create(newMap).build());
	}

	/**
	 * Creates one fresh POINT-v2 frame and its fresh system map atomically.
	 *
	 * @return prepared frame-and-map addition transaction
	 */
	public SpatialLifecycleTransaction prepareFrameMapAdd(
			ProjectionSystemRecord currentSystem, ProjectionFrameRecord newFrame,
			ProjectionDiagramMapRecord newMap, ProjectionSystemRecord updatedSystem,
			String provenanceToken) {
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.MAP_ADD, provenanceToken)
				.replace(currentSystem, updatedSystem).create(newFrame).create(newMap)
				.build());
	}

	/** @return prepared transaction removing one map from the current system */
	public SpatialLifecycleTransaction prepareMapRemove(
			ProjectionSystemRecord currentSystem, ProjectionDiagramMapRecord currentMap,
			ProjectionSystemRecord updatedSystem, String provenanceToken) {
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.MAP_REMOVE, provenanceToken)
				.replace(currentSystem, updatedSystem).retire(currentMap).build());
	}

	/**
	 * Retires one system map and its now-unreferenced frame atomically.
	 *
	 * @return prepared frame-and-map removal transaction
	 */
	public SpatialLifecycleTransaction prepareFrameMapRemove(
			ProjectionSystemRecord currentSystem, ProjectionDiagramMapRecord currentMap,
			ProjectionFrameRecord currentFrame, ProjectionSystemRecord updatedSystem,
			String provenanceToken) {
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.MAP_REMOVE, provenanceToken)
				.replace(currentSystem, updatedSystem).retire(currentMap)
				.retire(currentFrame).build());
	}

	/** @return prepared same-identity map-definition change transaction */
	public SpatialLifecycleTransaction prepareMapChange(
			ProjectionSystemRecord currentSystem, ProjectionDiagramMapRecord currentMap,
			ProjectionDiagramMapRecord updatedMap, ProjectionSystemRecord updatedSystem,
			String provenanceToken) {
		if (currentMap.getFrameUseRole() != updatedMap.getFrameUseRole()) {
			throw new IllegalArgumentException(
					"Map frame-use re-role requires prepareMapRerole");
		}
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.MAP_CHANGE, provenanceToken)
				.replace(currentSystem, updatedSystem).replace(currentMap, updatedMap)
				.build());
	}

	/**
	 * Re-roles one system map under a fresh map identity and rewrites the complete
	 * affected POINT-v2 closure explicitly.
	 *
	 * @return prepared complete map re-role transaction
	 */
	public SpatialLifecycleTransaction prepareMapRerole(
			ProjectionDiagramMapRecord currentMap,
			ProjectionFrameUseRole newRole, String provenanceToken) {
		Objects.requireNonNull(currentMap);
		Objects.requireNonNull(newRole);
		if (currentMap.getSemanticVersion() != 2) {
			throw new IllegalArgumentException(
					"Map re-role admits the POINT-v2 lifecycle surface only");
		}
		if (currentMap.getFrameUseRole() == newRole) {
			throw new IllegalArgumentException("Map re-role requires a different role");
		}
		requireExactCurrentRecord(currentMap);
		ProjectionSystemRecord currentSystem = requireRecord(
				currentMap.getSystemId(), ProjectionSystemRecord.class);
		ArrayList<SpatialIdentityId> reservations = new ArrayList<>();
		try {
			ProjectionDiagramMapId freshMapId =
					registry.allocateProjectionDiagramMapId();
			reservations.add(freshMapId);

			LinkedHashMap<ProjectionBindingId, ProjectionBindingRecord> freshBindings =
					new LinkedHashMap<>();
			LinkedHashMap<SpatialObjectId, List<ProjectionBindingId>> objectBindings =
					new LinkedHashMap<>();
			LinkedHashMap<ProjectionFrameRelationId, ProjectionFrameRelationRecord>
					freshRelations = new LinkedHashMap<>();
			for (SpatialIdentityRecord record : registry.getRecords()) {
				if (record instanceof ProjectionBindingRecord) {
					ProjectionBindingRecord binding = (ProjectionBindingRecord) record;
					if (binding.getDiagramMapId().equals(currentMap.getId())) {
						ProjectionBindingId freshBindingId =
								registry.allocateProjectionBindingId();
						reservations.add(freshBindingId);
						freshBindings.put(binding.getId(),
								binding.asFreshRetargeted(freshBindingId, freshMapId));
						SpatialObjectRecord object = requireRecord(binding.getObjectId(),
								SpatialObjectRecord.class);
						objectBindings.putIfAbsent(object.getId(),
								new ArrayList<>(object.getBindingIds()));
						replaceMember(objectBindings.get(object.getId()), binding.getId(),
								freshBindingId, "object binding");
					}
				} else if (record instanceof ProjectionFrameRelationRecord) {
					ProjectionFrameRelationRecord relation =
							(ProjectionFrameRelationRecord) record;
					if (relation.getSourceMapId().equals(currentMap.getId())
							|| relation.getDestinationMapId().equals(currentMap.getId())) {
						ProjectionFrameRelationId freshRelationId =
								registry.allocateProjectionFrameRelationId();
						reservations.add(freshRelationId);
						ProjectionDiagramMapId sourceId = relation.getSourceMapId()
								.equals(currentMap.getId()) ? freshMapId
										: relation.getSourceMapId();
						ProjectionDiagramMapId destinationId = relation
								.getDestinationMapId().equals(currentMap.getId()) ? freshMapId
										: relation.getDestinationMapId();
						freshRelations.put(relation.getId(), relation.asFreshRetargeted(
								freshRelationId, sourceId, destinationId));
					}
				}
			}

			List<ProjectionFrameRelationId> freshMapRelationIds = new ArrayList<>(
					currentMap.getRelationIds());
			for (Map.Entry<ProjectionFrameRelationId, ProjectionFrameRelationRecord>
					entry : freshRelations.entrySet()) {
				replaceMember(freshMapRelationIds, entry.getKey(),
						entry.getValue().getId(), "map relation");
			}
			ProjectionDiagramMapRecord freshMap = currentMap.asFreshReroled(freshMapId,
					newRole, freshMapRelationIds);

			List<ProjectionDiagramMapId> systemMapIds = new ArrayList<>(
					currentSystem.getMapIds());
			replaceMember(systemMapIds, currentMap.getId(), freshMapId, "system map");
			List<ProjectionFrameRelationId> systemRelationIds = new ArrayList<>(
					currentSystem.getRelationIds());
			for (Map.Entry<ProjectionFrameRelationId, ProjectionFrameRelationRecord>
					entry : freshRelations.entrySet()) {
				replaceMember(systemRelationIds, entry.getKey(),
						entry.getValue().getId(), "system relation");
			}
			ProjectionSystemRecord updatedSystem =
					currentSystem.withMembershipAndRevision(systemMapIds, systemRelationIds,
							increment(currentSystem.getRevision()));

			SpatialLifecycleMutation.Builder mutation =
					SpatialLifecycleMutation.builder(
							SpatialLifecycleOperationKind.MAP_REROLE, provenanceToken)
							.replace(currentSystem, updatedSystem).retire(currentMap)
							.create(freshMap);
			for (Map.Entry<ProjectionBindingId, ProjectionBindingRecord> entry
					: freshBindings.entrySet()) {
				mutation.retire(requireRecord(entry.getKey(),
						ProjectionBindingRecord.class)).create(entry.getValue());
			}
			for (Map.Entry<SpatialObjectId, List<ProjectionBindingId>> entry
					: objectBindings.entrySet()) {
				SpatialObjectRecord object = requireRecord(entry.getKey(),
						SpatialObjectRecord.class);
				mutation.replace(object, object.withBindingsAndRevisions(entry.getValue(),
						increment(object.getDefinitionRevision()),
						increment(object.getTopologyRevision())));
			}
			LinkedHashMap<ProjectionDiagramMapId, List<ProjectionFrameRelationId>>
					peerMaps = new LinkedHashMap<>();
			for (Map.Entry<ProjectionFrameRelationId, ProjectionFrameRelationRecord>
					entry : freshRelations.entrySet()) {
				ProjectionFrameRelationRecord oldRelation = requireRecord(entry.getKey(),
						ProjectionFrameRelationRecord.class);
				mutation.retire(oldRelation).create(entry.getValue());
				collectPeerMapRewrite(peerMaps, currentMap.getId(),
						oldRelation.getSourceMapId(), entry.getKey(),
						entry.getValue().getId());
				collectPeerMapRewrite(peerMaps, currentMap.getId(),
						oldRelation.getDestinationMapId(), entry.getKey(),
						entry.getValue().getId());
			}
			for (Map.Entry<ProjectionDiagramMapId, List<ProjectionFrameRelationId>> entry
					: peerMaps.entrySet()) {
				ProjectionDiagramMapRecord peer = requireRecord(entry.getKey(),
						ProjectionDiagramMapRecord.class);
				mutation.replace(peer, peer.withRelationsAndRevision(entry.getValue(),
						increment(peer.getRevision())));
			}
			return prepareMutation(mutation.build());
		} catch (RuntimeException exception) {
			registry.abandonLifecycleReservations(reservations);
			throw exception;
		}
	}

	/**
	 * Changes one frame definition in place and advances its owning system.
	 *
	 * @return prepared frame-definition change transaction
	 */
	public SpatialLifecycleTransaction prepareFrameChange(
			ProjectionSystemRecord currentSystem, ProjectionFrameRecord currentFrame,
			ProjectionFrameRecord updatedFrame, ProjectionSystemRecord updatedSystem,
			String provenanceToken) {
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.FRAME_CHANGE, provenanceToken)
				.replace(currentSystem, updatedSystem)
				.replace(currentFrame, updatedFrame).build());
	}

	/** @return prepared transaction adding one reciprocal frame relation */
	public SpatialLifecycleTransaction prepareRelationAdd(
			ProjectionSystemRecord currentSystem,
			ProjectionFrameRelationRecord newRelation,
			ProjectionDiagramMapRecord currentSourceMap,
			ProjectionDiagramMapRecord updatedSourceMap,
			ProjectionDiagramMapRecord currentDestinationMap,
			ProjectionDiagramMapRecord updatedDestinationMap,
			ProjectionSystemRecord updatedSystem, String provenanceToken) {
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.RELATION_ADD, provenanceToken)
				.replace(currentSystem, updatedSystem)
				.replace(currentSourceMap, updatedSourceMap)
				.replace(currentDestinationMap, updatedDestinationMap)
				.create(newRelation).build());
	}

	/** @return prepared transaction removing one reciprocal frame relation */
	public SpatialLifecycleTransaction prepareRelationRemove(
			ProjectionSystemRecord currentSystem,
			ProjectionFrameRelationRecord currentRelation,
			ProjectionDiagramMapRecord currentSourceMap,
			ProjectionDiagramMapRecord updatedSourceMap,
			ProjectionDiagramMapRecord currentDestinationMap,
			ProjectionDiagramMapRecord updatedDestinationMap,
			ProjectionSystemRecord updatedSystem, String provenanceToken) {
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.RELATION_REMOVE, provenanceToken)
				.replace(currentSystem, updatedSystem)
				.replace(currentSourceMap, updatedSourceMap)
				.replace(currentDestinationMap, updatedDestinationMap)
				.retire(currentRelation).build());
	}

	/** @return prepared same-identity relation-definition change transaction */
	public SpatialLifecycleTransaction prepareRelationChange(
			ProjectionSystemRecord currentSystem,
			ProjectionFrameRelationRecord currentRelation,
			ProjectionFrameRelationRecord updatedRelation,
			ProjectionSystemRecord updatedSystem, String provenanceToken) {
		return prepareMutation(SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.RELATION_CHANGE, provenanceToken)
				.replace(currentSystem, updatedSystem)
				.replace(currentRelation, updatedRelation).build());
	}

	/**
	 * Re-roles one relation under a fresh identity while preserving its owning
	 * system and atomically rewriting both endpoint memberships.
	 *
	 * @return prepared complete relation re-role transaction
	 */
	public SpatialLifecycleTransaction prepareRelationRerole(
			ProjectionFrameRelationRecord currentRelation,
			ProjectionFrameRelationRecord replacementRelation,
			String provenanceToken) {
		Objects.requireNonNull(currentRelation);
		Objects.requireNonNull(replacementRelation);
		ArrayList<SpatialIdentityId> reservations = new ArrayList<>();
		reservations.add(replacementRelation.getId());
		try {
			if (currentRelation.getSemanticVersion() != 2
					|| replacementRelation.getSemanticVersion() != 2
					|| currentRelation.getId().equals(replacementRelation.getId())
					|| !currentRelation.getSystemId().equals(
							replacementRelation.getSystemId())
					|| !currentRelation.getSourceMapId().equals(
							replacementRelation.getSourceMapId())
					|| !currentRelation.getDestinationMapId().equals(
							replacementRelation.getDestinationMapId())) {
				throw new IllegalArgumentException(
						"Relation re-role requires fresh POINT-v2 identity with fixed endpoints");
			}
			ProjectionSystemRecord currentSystem = requireRecord(
					currentRelation.getSystemId(), ProjectionSystemRecord.class);
			ProjectionDiagramMapRecord sourceMap = requireRecord(
					currentRelation.getSourceMapId(), ProjectionDiagramMapRecord.class);
			ProjectionDiagramMapRecord destinationMap = requireRecord(
					currentRelation.getDestinationMapId(), ProjectionDiagramMapRecord.class);
			List<ProjectionFrameRelationId> systemRelations = new ArrayList<>(
					currentSystem.getRelationIds());
			replaceMember(systemRelations, currentRelation.getId(),
					replacementRelation.getId(), "system relation");
			ProjectionSystemRecord updatedSystem =
					currentSystem.withMembershipAndRevision(currentSystem.getMapIds(),
							systemRelations, increment(currentSystem.getRevision()));
			List<ProjectionFrameRelationId> sourceRelations = new ArrayList<>(
					sourceMap.getRelationIds());
			replaceMember(sourceRelations, currentRelation.getId(),
					replacementRelation.getId(), "source-map relation");
			List<ProjectionFrameRelationId> destinationRelations = new ArrayList<>(
					destinationMap.getRelationIds());
			replaceMember(destinationRelations, currentRelation.getId(),
					replacementRelation.getId(), "destination-map relation");
			SpatialLifecycleMutation.Builder mutation =
					SpatialLifecycleMutation.builder(
							SpatialLifecycleOperationKind.RELATION_REROLE, provenanceToken)
							.replace(currentSystem, updatedSystem)
							.replace(sourceMap, sourceMap.withRelationsAndRevision(
									sourceRelations, increment(sourceMap.getRevision())))
							.retire(currentRelation).create(replacementRelation);
			if (destinationMap.getId().equals(sourceMap.getId())) {
				if (!destinationRelations.equals(sourceRelations)) {
					throw new IllegalArgumentException(
							"A self-map relation has inconsistent reciprocal membership");
				}
			} else {
				mutation.replace(destinationMap,
						destinationMap.withRelationsAndRevision(destinationRelations,
								increment(destinationMap.getRevision())));
			}
			return prepareMutation(mutation.build());
		} catch (RuntimeException exception) {
			registry.abandonLifecycleReservations(reservations);
			throw exception;
		}
	}

	/**
	 * Replaces one system with a fresh, complete explicit closure.
	 *
	 * <p>Every active record affected by retirement of {@code currentSystem}
	 * must occur in {@code currentDependents}; implicit transfer or inferred
	 * invalidation is rejected during prospective-graph validation.</p>
	 *
	 * @return prepared complete system-replacement transaction
	 */
	public SpatialLifecycleTransaction prepareSystemReplacement(
			ProjectionSystemRecord currentSystem,
			ProjectionSystemRecord replacementSystem,
			Collection<? extends SpatialIdentityRecord> currentDependents,
			Collection<? extends SpatialIdentityRecord> replacementClosure,
			String provenanceToken) {
		ArrayList<SpatialIdentityId> reservations = new ArrayList<>();
		reservations.add(replacementSystem.getId());
		for (SpatialIdentityRecord replacement
				: Objects.requireNonNull(replacementClosure)) {
			reservations.add(replacement.getId());
		}
		try {
			SpatialLifecycleMutation.Builder builder =
					SpatialLifecycleMutation.builder(
							SpatialLifecycleOperationKind.SYSTEM_REPLACEMENT,
							provenanceToken)
							.retire(currentSystem).create(replacementSystem);
			for (SpatialIdentityRecord current
					: Objects.requireNonNull(currentDependents)) {
				if (current.getId().equals(currentSystem.getId())) {
					throw new IllegalArgumentException(
							"System replacement dependents must exclude the system itself");
				}
				builder.retire(current);
			}
			for (SpatialIdentityRecord replacement : replacementClosure) {
				if (replacement.getId().equals(replacementSystem.getId())) {
					throw new IllegalArgumentException(
							"Replacement closure must exclude its root system");
				}
				builder.create(replacement);
			}
			return prepareMutation(builder.build());
		} catch (RuntimeException exception) {
			registry.abandonLifecycleReservations(reservations);
			throw exception;
		}
	}

	private void collectPeerMapRewrite(
			Map<ProjectionDiagramMapId, List<ProjectionFrameRelationId>> rewrites,
			ProjectionDiagramMapId replacedMapId, ProjectionDiagramMapId peerMapId,
			ProjectionFrameRelationId oldRelationId,
			ProjectionFrameRelationId freshRelationId) {
		if (peerMapId.equals(replacedMapId)) {
			return;
		}
		ProjectionDiagramMapRecord peer = requireRecord(peerMapId,
				ProjectionDiagramMapRecord.class);
		rewrites.putIfAbsent(peerMapId, new ArrayList<>(peer.getRelationIds()));
		replaceMember(rewrites.get(peerMapId), oldRelationId, freshRelationId,
				"peer-map relation");
	}

	private <T extends SpatialIdentityRecord> T requireRecord(SpatialIdentityId id,
			Class<T> type) {
		SpatialIdentityRecord record = registry.getRecord(Objects.requireNonNull(id));
		if (!type.isInstance(record)) {
			throw new IllegalArgumentException(
					"Missing required " + type.getSimpleName() + ": " + id);
		}
		return type.cast(record);
	}

	private void requireExactCurrentRecord(SpatialIdentityRecord expected) {
		SpatialIdentityRecord current = registry.getRecord(expected.getId());
		if (current == null || !SpatialRecordXmlCodec.writeRecord(current).equals(
				SpatialRecordXmlCodec.writeRecord(expected))) {
			throw new IllegalArgumentException(
					"Lifecycle source record is stale: " + expected.getId());
		}
	}

	private static <T> void replaceMember(List<T> members, T current, T replacement,
			String role) {
		int index = members.indexOf(current);
		if (index < 0) {
			throw new IllegalArgumentException("Missing " + role + " membership");
		}
		members.set(index, replacement);
	}

	private static long increment(long revision) {
		return Math.addExact(revision, 1);
	}

	/**
	 * Explicitly supplies the exact missing typed record and optional geo handle.
	 *
	 * @return prepared explicit reference-recovery transaction
	 */
	public SpatialLifecycleTransaction prepareReferenceRecovery(
			SpatialIdentityRecord missingRecord, GeoElement geo,
			String provenanceToken) {
		SpatialLifecycleMutation.Builder builder = SpatialLifecycleMutation.builder(
				SpatialLifecycleOperationKind.REFERENCE_RECOVERY, provenanceToken)
				.validatedReferenceRecovery().create(missingRecord);
		if (missingRecord instanceof GeoIdentityRecord) {
			if (geo == null) {
				throw new IllegalArgumentException(
						"A recovered geo identity requires the explicit GeoElement handle");
			}
			builder.attach(geo, (PersistentGeoId) missingRecord.getId());
		} else if (geo != null) {
			throw new IllegalArgumentException(
					"Only a recovered geo record may carry a GeoElement handle");
		}
		return prepareMutation(builder.build());
	}
}
