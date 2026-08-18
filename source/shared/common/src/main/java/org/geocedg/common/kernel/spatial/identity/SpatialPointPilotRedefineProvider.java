/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.identity;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.geogebra.common.kernel.geos.GeoElement;

/** Productive redefine authority for the G9A2 projection-defined POINT pilot. */
public final class SpatialPointPilotRedefineProvider
		implements SpatialRedefineProvider {
	/** Stable provider token persisted by the G9A2 point pilot. */
	public static final String PROVIDER_ID = "g9a2.point.pilot";

	private final SpatialIdentityGraph graph;

	/** Creates a provider backed only by the construction-owned durable graph. */
	public SpatialPointPilotRedefineProvider(SpatialIdentityGraph graph) {
		this.graph = Objects.requireNonNull(graph);
	}

	@Override
	public String getProviderId() {
		return PROVIDER_ID;
	}

	@Override
	public SpatialRedefineSignature describeCandidate(
			SpatialRedefineContext context, GeoElement candidate) {
		if (!isCompatibleCandidate(context, candidate)) {
			throw new IllegalArgumentException(
					"Candidate violates the durable G9A2 POINT role contract");
		}
		return context.getOldSignature();
	}

	@Override
	public boolean isTopologyPreserving(SpatialRedefineContext context,
			GeoElement candidate) {
		return isCompatibleCandidate(context, candidate);
	}

	@Override
	public SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
			describeCandidateGroup(SpatialRedefineContext context,
					List<GeoElement> candidates) {
		Objects.requireNonNull(context);
		Objects.requireNonNull(candidates);
		if (context.getOldOutputs().size() != 1 || candidates.size() != 1) {
			throw new IllegalArgumentException(
					"The G9A2 POINT pilot has exactly one persisted output role");
		}
		GeoElement candidate = Objects.requireNonNull(candidates.get(0));
		return SpatialRedefineOutputGroup.singleton(
				new SpatialRedefineCandidateOutput(candidate,
						describeCandidate(context, candidate)));
	}

	@Override
	public SpatialRedefineEffect describeEffect(SpatialRedefineContext context,
			SpatialRedefineOutputGroup<SpatialRedefineCandidateOutput>
					candidateOutputs) {
		SpatialRedefineCandidateOutput candidate = candidateOutputs.get(
				context.getTargetedStableOutputRole());
		if (candidateOutputs.size() != 1 || candidate == null
				|| !isCompatibleCandidate(context, candidate.getGeo())) {
			throw new IllegalArgumentException(
					"The G9A2 POINT redefine group is incomplete or incompatible");
		}
		SpatialRedefinePersistedOutput old = context.getOldOutputs().get(
				context.getTargetedStableOutputRole());
		// Frozen host state classifies only the operation effect. Durable
		// continuity was already established independently from typed records.
		return old != null && old.hasSameHostState(candidate)
				? SpatialRedefineEffect.NO_OP
				: SpatialRedefineEffect.DEFINITION_CHANGE;
	}

	@Override
	public SpatialRedefineDecision inspect(SpatialRedefineContext context,
			SpatialRedefineProposal proposal) {
		if (!proposal.isEffectExplicit()
				|| proposal.getCandidateOutputs().size() != 1
				|| !context.getOldOutputs().getRoles().equals(
						proposal.getCandidateOutputs().getRoles())
				|| !context.getOldSignature().isExactlyCompatibleWith(
						proposal.getSignature())
				|| !isCompatibleCandidate(context, proposal.getCandidate())) {
			return SpatialRedefineDecision.REJECT;
		}
		return proposal.isReplacementOperationSelected()
				? SpatialRedefineDecision.FRESH : SpatialRedefineDecision.RETAIN;
	}

	private boolean isCompatibleCandidate(SpatialRedefineContext context,
			GeoElement candidate) {
		if (context == null || candidate == null
				|| context.getOldOutputs().size() != 1
				|| !isPointPilotSignature(context.getOldSignature())) {
			return false;
		}
		ExpectedGeoKind expected = expectedKind(context.getOldId());
		return expected != null && expected.accepts(candidate);
	}

	private ExpectedGeoKind expectedKind(PersistentGeoId targetId) {
		EnumSet<ExpectedGeoKind> kinds = EnumSet.noneOf(ExpectedGeoKind.class);
		for (SpatialIdentityRecord record : graph.getRecords()) {
			if (record instanceof SpatialObjectRecord) {
				collectPointObjectKinds((SpatialObjectRecord) record, targetId, kinds);
			}
		}
		return kinds.size() == 1 ? kinds.iterator().next() : null;
	}

	private void collectPointObjectKinds(SpatialObjectRecord object,
			PersistentGeoId targetId, EnumSet<ExpectedGeoKind> kinds) {
		if (object.getSemanticVersion() != 2
				|| !SpatialObjectRecord.POINT_TYPE.equals(object.getSpatialType())
				|| object.getAuthority() != EditAuthorityMode.PROJECTION_DEFINED
				|| !SpatialObjectRecord.POINT_SCHEMA_ID.equals(object.getSchemaId())
				|| object.getSchemaVersion()
						!= SpatialObjectRecord.POINT_SCHEMA_VERSION) {
			return;
		}
		ProjectionSystemRecord system = typed(object.getSystemId(),
				ProjectionSystemRecord.class);
		if (system == null || system.getSemanticVersion() != 2) {
			return;
		}
		for (ProjectionBindingId bindingId : object.getBindingIds()) {
			ProjectionBindingRecord binding = typed(bindingId,
					ProjectionBindingRecord.class);
			if (binding != null && binding.getSemanticVersion() == 2
					&& object.getId().equals(binding.getObjectId())
					&& object.getSystemId().equals(binding.getSystemId())) {
				addIfEqual(targetId, binding.getProjectedPointGeoId(),
						ExpectedGeoKind.POINT_2D, kinds);
			}
		}
		for (ProjectionDiagramMapId mapId : system.getMapIds()) {
			ProjectionDiagramMapRecord map = typed(mapId,
					ProjectionDiagramMapRecord.class);
			if (map != null && map.getSemanticVersion() == 2
					&& object.getSystemId().equals(map.getSystemId())) {
				collectMapKinds(map, targetId, kinds);
			}
		}
		for (ProjectionFrameRelationId relationId : system.getRelationIds()) {
			ProjectionFrameRelationRecord relation = typed(relationId,
					ProjectionFrameRelationRecord.class);
			if (relation != null && relation.getSemanticVersion() == 2
					&& object.getSystemId().equals(relation.getSystemId())) {
				collectRelationKinds(relation, targetId, kinds);
			}
		}
	}

	private void collectMapKinds(ProjectionDiagramMapRecord map,
			PersistentGeoId targetId, EnumSet<ExpectedGeoKind> kinds) {
		addIfEqual(targetId, map.getA00GeoId(), ExpectedGeoKind.NUMBER, kinds);
		addIfEqual(targetId, map.getA01GeoId(), ExpectedGeoKind.NUMBER, kinds);
		addIfEqual(targetId, map.getA10GeoId(), ExpectedGeoKind.NUMBER, kinds);
		addIfEqual(targetId, map.getA11GeoId(), ExpectedGeoKind.NUMBER, kinds);
		addIfEqual(targetId, map.getB0GeoId(), ExpectedGeoKind.NUMBER, kinds);
		addIfEqual(targetId, map.getB1GeoId(), ExpectedGeoKind.NUMBER, kinds);
		addIfEqual(targetId, map.getDeclaredScaleGeoId(), ExpectedGeoKind.NUMBER,
				kinds);
		ProjectionFrameRecord frame = typed(map.getFrameId(),
				ProjectionFrameRecord.class);
		if (frame != null && frame.getSemanticVersion() == 2) {
			addIfEqual(targetId, frame.getOriginGeoId(),
					ExpectedGeoKind.POINT_3D, kinds);
			addIfEqual(targetId, frame.getUGeoId(), ExpectedGeoKind.VECTOR_3D,
					kinds);
			addIfEqual(targetId, frame.getVGeoId(), ExpectedGeoKind.VECTOR_3D,
					kinds);
		}
	}

	private static void collectRelationKinds(
			ProjectionFrameRelationRecord relation, PersistentGeoId targetId,
			EnumSet<ExpectedGeoKind> kinds) {
		addIfEqual(targetId, relation.getSupportStartGeoId(),
				ExpectedGeoKind.POINT_3D, kinds);
		addIfEqual(targetId, relation.getSupportEndGeoId(),
				ExpectedGeoKind.POINT_3D, kinds);
		addIfEqual(targetId, relation.getFoldSignGeoId(), ExpectedGeoKind.NUMBER,
				kinds);
	}

	private static void addIfEqual(PersistentGeoId target,
			PersistentGeoId referenced, ExpectedGeoKind kind,
			EnumSet<ExpectedGeoKind> kinds) {
		if (target.equals(referenced)) {
			kinds.add(kind);
		}
	}

	private <T extends SpatialIdentityRecord> T typed(SpatialIdentityId id,
			Class<T> type) {
		SpatialIdentityRecord record = graph.getRecord(id);
		return type.isInstance(record) ? type.cast(record) : null;
	}

	private static boolean isPointPilotSignature(
			SpatialRedefineSignature signature) {
		boolean semanticInput = "SEMANTIC_INPUT".equals(signature.getFamily())
				&& "INPUT".equals(signature.getStableOutputRole());
		boolean projectedPoint = SpatialObjectRecord.POINT_TYPE.equals(
				signature.getFamily())
				&& "PROJECTED_POINT".equals(signature.getStableOutputRole());
		return PROVIDER_ID.equals(signature.getProvider())
				&& SpatialObjectRecord.POINT_SCHEMA_ID.equals(signature.getSchemaId())
				&& signature.getSchemaVersion()
						== SpatialObjectRecord.POINT_SCHEMA_VERSION
				&& signature.getAuthority() == EditAuthorityMode.PROJECTION_DEFINED
				&& signature.getBindingRole() == ProjectionBindingRole.DEFINING
				&& signature.getOutputCardinality() == 1
				&& (semanticInput || projectedPoint);
	}

	private enum ExpectedGeoKind {
		POINT_2D {
			@Override
			boolean accepts(GeoElement geo) {
				return geo.isGeoPoint() && !geo.isGeoElement3D();
			}
		},
		POINT_3D {
			@Override
			boolean accepts(GeoElement geo) {
				return geo.isGeoPoint() && geo.isGeoElement3D();
			}
		},
		VECTOR_3D {
			@Override
			boolean accepts(GeoElement geo) {
				return geo.isGeoVector() && geo.isGeoElement3D();
			}
		},
		NUMBER {
			@Override
			boolean accepts(GeoElement geo) {
				return geo.isGeoNumeric();
			}
		};

		abstract boolean accepts(GeoElement geo);
	}
}
