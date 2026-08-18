/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Pure validator for frame, diagram-map, hinge and change-of-plane values. */
public final class ProjectionSystemEvaluator {
	/**
	 * Evaluates a projection-system subcontext using fresh instrumentation.
	 *
	 * @return atomic projection-system certificate
	 */
	public ProjectionSystemCertificate evaluate(
			List<ProjectionFrameDefinition> frames,
			List<DiagramMapDefinition> maps,
			List<ProjectionRelationDefinition> relations,
			NumericPolicy policy) {
		return evaluate(frames, maps, relations, policy,
				new SpatialSemanticInstrumentation());
	}

	/**
	 * Evaluates a projection-system subcontext using supplied instrumentation.
	 *
	 * @return atomic projection-system certificate
	 */
	public ProjectionSystemCertificate evaluate(
			List<ProjectionFrameDefinition> frames,
			List<DiagramMapDefinition> maps,
			List<ProjectionRelationDefinition> relations,
			NumericPolicy policy, SpatialSemanticInstrumentation instrumentation) {
		return evaluateInternal(frames, maps, relations, Collections.emptyList(),
				policy, instrumentation);
	}

	private ProjectionSystemCertificate evaluateInternal(
			List<ProjectionFrameDefinition> frames,
			List<DiagramMapDefinition> maps,
			List<ProjectionRelationDefinition> relations,
			List<ProjectionObservation> observations, NumericPolicy policy,
			SpatialSemanticInstrumentation instrumentation) {
		Objects.requireNonNull(frames);
		Objects.requireNonNull(maps);
		Objects.requireNonNull(relations);
		Objects.requireNonNull(policy);
		Objects.requireNonNull(instrumentation);
		frames = canonicalFrames(frames);
		maps = canonicalMaps(maps);
		relations = canonicalRelations(relations);
		for (ProjectionRelationDefinition relation : relations) {
			ProjectionRelationDefinition current = Objects.requireNonNull(relation);
			addFrame(frames, current.getSourceFrame());
			addFrame(frames, current.getDestinationFrame());
			addMap(maps, current.getSourceMap());
			addMap(maps, current.getDestinationMap());
		}
		frames.sort(Comparator.comparing(ProjectionFrameDefinition::getEvidenceKey));
		maps.sort(Comparator.comparing(DiagramMapDefinition::getEvidenceKey));
		instrumentation.recordProjectionSystemEvaluation();

		ProjectionSystemStatus status;
		if (frames.isEmpty() && maps.isEmpty() && relations.isEmpty()) {
			status = ProjectionSystemStatus.NOT_EVALUATED;
		} else if (frames.isEmpty() || maps.isEmpty()) {
			status = ProjectionSystemStatus.UNDEFINED;
		} else {
			status = ProjectionSystemStatus.CONSISTENT;
		}
		String commonWorldUnit = null;
		for (ProjectionFrameDefinition frame : frames) {
			Objects.requireNonNull(frame);
			instrumentation.recordFrameEvaluation();
			status = combine(status, fromDefinition(frame.validate(policy)));
			if (commonWorldUnit == null) {
				commonWorldUnit = frame.getUnit();
			} else if (!SemanticValueChecks.sameUnit(commonWorldUnit,
					frame.getUnit())) {
				status = combine(status, ProjectionSystemStatus.INCONSISTENT);
			}
		}
		String commonDiagramUnit = null;
		List<DiagramMapEvidence> mapEvidence = new ArrayList<>();
		for (DiagramMapDefinition map : maps) {
			Objects.requireNonNull(map);
			ProjectionSystemStatus mapStatus = map.validate(policy);
			status = combine(status, mapStatus);
			mapEvidence.add(new DiagramMapEvidence(map.getEvidenceKey(), map.getFamily(),
					map.getOrientation(), map.getSourceUnit(), map.getDiagramUnit(),
					map.getDeclaredScale(), map.getRevision(), mapStatus));
			if (commonWorldUnit != null && !SemanticValueChecks.sameUnit(
					commonWorldUnit, map.getSourceUnit())) {
				status = combine(status, ProjectionSystemStatus.INCONSISTENT);
			}
			if (commonDiagramUnit == null) {
				commonDiagramUnit = map.getDiagramUnit();
			} else if (!SemanticValueChecks.sameUnit(commonDiagramUnit,
					map.getDiagramUnit())) {
				status = combine(status, ProjectionSystemStatus.INCONSISTENT);
			}
		}
		for (ProjectionObservation observation : observations) {
			ProjectionFrameDefinition frame = observation.getFrame();
			DiagramMapDefinition map = observation.getDiagramMap();
			if (!SemanticValueChecks.sameUnit(frame.getUnit(), map.getSourceUnit())) {
				status = combine(status, ProjectionSystemStatus.INCONSISTENT);
			}
		}

		List<ProjectionRelationEvidence> evidence = new ArrayList<>();
		for (ProjectionRelationDefinition relation : relations) {
			ProjectionRelationEvidence relationEvidence = evaluateRelation(
					Objects.requireNonNull(relation), policy, instrumentation);
			evidence.add(relationEvidence);
			status = combine(status, relationEvidence.getStatus());
		}
		double maximumResidual = 0;
		for (ProjectionRelationEvidence item : evidence) {
			maximumResidual = Math.max(maximumResidual,
					item.getMaximumNormalizedResidual());
		}
		boolean accepted = status == ProjectionSystemStatus.CONSISTENT;
		instrumentation.recordProjectionSystemCertificate(accepted);
		return new ProjectionSystemCertificate(SpatialCapabilityStatus.SUPPORTED,
				status, mapEvidence, evidence, frames.size(), maps.size(),
				maximumResidual);
	}

	/**
	 * Evaluates only the exact frame/map subcontext referenced by observations.
	 *
	 * @return atomic projection-system certificate
	 */
	public ProjectionSystemCertificate evaluateObservations(
			List<ProjectionObservation> observations,
			List<ProjectionRelationDefinition> relations, NumericPolicy policy,
			SpatialSemanticInstrumentation instrumentation) {
		Objects.requireNonNull(observations);
		List<ProjectionFrameDefinition> frames = new ArrayList<>();
		List<DiagramMapDefinition> maps = new ArrayList<>();
		for (ProjectionObservation observation : observations) {
			ProjectionObservation current = Objects.requireNonNull(observation);
			addFrame(frames, current.getFrame());
			addMap(maps, current.getDiagramMap());
		}
		return evaluateInternal(frames, maps, relations, observations, policy,
				instrumentation);
	}

	private static ArrayList<ProjectionFrameDefinition> canonicalFrames(
			List<ProjectionFrameDefinition> frames) {
		Map<String, ProjectionFrameDefinition> byKey = new LinkedHashMap<>();
		for (ProjectionFrameDefinition frame : frames) {
			putFrame(byKey, Objects.requireNonNull(frame));
		}
		return new ArrayList<>(byKey.values());
	}

	private static ArrayList<DiagramMapDefinition> canonicalMaps(
			List<DiagramMapDefinition> maps) {
		Map<String, DiagramMapDefinition> byKey = new LinkedHashMap<>();
		for (DiagramMapDefinition map : maps) {
			putMap(byKey, Objects.requireNonNull(map));
		}
		return new ArrayList<>(byKey.values());
	}

	private static ArrayList<ProjectionRelationDefinition> canonicalRelations(
			List<ProjectionRelationDefinition> relations) {
		Map<String, ProjectionRelationDefinition> byKey = new LinkedHashMap<>();
		for (ProjectionRelationDefinition relation : relations) {
			ProjectionRelationDefinition current = Objects.requireNonNull(relation);
			ProjectionRelationDefinition previous = byKey.putIfAbsent(
					current.getEvidenceKey(), current);
			if (previous != null && !previous.getEvidenceContentKey().equals(
					current.getEvidenceContentKey())) {
				throw new IllegalArgumentException("conflicting relation evidence key");
			}
		}
		ArrayList<ProjectionRelationDefinition> result = new ArrayList<>(
				byKey.values());
		result.sort(Comparator.comparing(
				ProjectionRelationDefinition::getEvidenceKey));
		return result;
	}

	private static void addFrame(List<ProjectionFrameDefinition> frames,
			ProjectionFrameDefinition frame) {
		for (ProjectionFrameDefinition current : frames) {
			if (current.getEvidenceKey().equals(frame.getEvidenceKey())) {
				if (!current.getEvidenceContentKey().equals(
						frame.getEvidenceContentKey())) {
					throw new IllegalArgumentException("conflicting frame evidence key");
				}
				return;
			}
		}
		frames.add(frame);
	}

	private static void addMap(List<DiagramMapDefinition> maps,
			DiagramMapDefinition map) {
		for (DiagramMapDefinition current : maps) {
			if (current.getEvidenceKey().equals(map.getEvidenceKey())) {
				if (!current.getEvidenceContentKey().equals(
						map.getEvidenceContentKey())) {
					throw new IllegalArgumentException(
							"conflicting diagram-map evidence key");
				}
				return;
			}
		}
		maps.add(map);
	}

	private static void putFrame(Map<String, ProjectionFrameDefinition> byKey,
			ProjectionFrameDefinition frame) {
		ProjectionFrameDefinition previous = byKey.putIfAbsent(frame.getEvidenceKey(),
				frame);
		if (previous != null && !previous.getEvidenceContentKey().equals(
				frame.getEvidenceContentKey())) {
			throw new IllegalArgumentException("conflicting frame evidence key");
		}
	}

	private static void putMap(Map<String, DiagramMapDefinition> byKey,
			DiagramMapDefinition map) {
		DiagramMapDefinition previous = byKey.putIfAbsent(map.getEvidenceKey(), map);
		if (previous != null && !previous.getEvidenceContentKey().equals(
				map.getEvidenceContentKey())) {
			throw new IllegalArgumentException("conflicting diagram-map evidence key");
		}
	}

	private ProjectionRelationEvidence evaluateRelation(
			ProjectionRelationDefinition relation, NumericPolicy policy,
			SpatialSemanticInstrumentation instrumentation) {
		instrumentation.recordRelationEvaluation(relation.getKind());
		if ((relation.getKind() == ProjectionRelationKind.HINGE_UNFOLD
				&& relation.getFoldSide() == FoldSide.NOT_APPLICABLE)
				|| (relation.getKind() == ProjectionRelationKind.CHANGE_OF_PLANE
				&& relation.getFoldSide() != FoldSide.NOT_APPLICABLE)) {
			return new ProjectionRelationEvidence(relation.getEvidenceKey(),
					relation.getKind(),
					ProjectionSystemStatus.INCONSISTENT, Double.NaN, Double.NaN,
					Double.NaN, Double.POSITIVE_INFINITY);
		}
		ProjectionFrameDefinition sourceFrame = relation.getSourceFrame();
		ProjectionFrameDefinition destinationFrame = relation.getDestinationFrame();
		DiagramMapDefinition sourceMap = relation.getSourceMap();
		DiagramMapDefinition destinationMap = relation.getDestinationMap();

		ProjectionSystemStatus prerequisite = relationPrerequisiteStatus(sourceFrame,
				sourceMap, destinationFrame, destinationMap, policy, instrumentation);
		if (prerequisite != ProjectionSystemStatus.CONSISTENT) {
			return new ProjectionRelationEvidence(relation.getEvidenceKey(),
					relation.getKind(), prerequisite, Double.NaN, Double.NaN,
					Double.NaN, Double.POSITIVE_INFINITY);
		}

		Vector3 sourceNormal = sourceFrame.getNormal(policy);
		Vector3 destinationNormal = destinationFrame.getNormal(policy);
		Vector3 hingeDirection = sourceNormal.cross(destinationNormal);
		double hingeMagnitude = hingeDirection.norm();
		if (!Double.isFinite(hingeMagnitude)) {
			return undefinedRelation(relation);
		}
		if (hingeMagnitude <= policy.getHingeTolerance()) {
			return new ProjectionRelationEvidence(relation.getEvidenceKey(),
					relation.getKind(),
					ProjectionSystemStatus.DEGENERATE, Double.NaN, Double.NaN,
					Double.NaN, Double.POSITIVE_INFINITY);
		}
		hingeDirection = hingeDirection.scale(1 / hingeMagnitude);
		Vector3 supportStart = relation.getSupportStart();
		Vector3 supportEnd = relation.getSupportEnd();
		Vector3 supportDelta = supportEnd.subtract(supportStart);
		double supportLength = supportDelta.norm();
		if (!supportStart.isFinite() || !supportEnd.isFinite()
				|| !Double.isFinite(supportLength)) {
			return new ProjectionRelationEvidence(relation.getEvidenceKey(),
					relation.getKind(),
					ProjectionSystemStatus.UNDEFINED, Double.NaN, Double.NaN,
					Double.NaN, Double.POSITIVE_INFINITY);
		}
		if (!(supportLength > policy.getHingeTolerance())) {
			return new ProjectionRelationEvidence(relation.getEvidenceKey(),
					relation.getKind(),
					ProjectionSystemStatus.DEGENERATE, Double.NaN, Double.NaN,
					Double.NaN, Double.POSITIVE_INFINITY);
		}
		Vector3 supportDirection = supportDelta.scale(1 / supportLength);
		Vector3 expectedDirection = hingeDirection.scale(
				relation.getOrientation().getDirectionSign());
		double orientationResidual = supportDirection.subtract(
				expectedDirection).norm();
		Vector2 sourceIntrinsicStart = sourceFrame.project(supportStart);
		Vector2 sourceIntrinsicEnd = sourceFrame.project(supportEnd);
		Vector2 destinationIntrinsicStart = destinationFrame.project(supportStart);
		Vector2 destinationIntrinsicEnd = destinationFrame.project(supportEnd);
		double intrinsicScale = Math.max(1, Math.max(supportLength,
				Math.max(Math.max(sourceIntrinsicStart.norm(), sourceIntrinsicEnd.norm()),
						Math.max(destinationIntrinsicStart.norm(),
								destinationIntrinsicEnd.norm()))));
		double planeResidual = maximumPlaneResidual(sourceFrame, sourceNormal,
				destinationFrame, destinationNormal, supportStart, supportEnd);
		if (!sourceIntrinsicStart.isFinite() || !sourceIntrinsicEnd.isFinite()
				|| !destinationIntrinsicStart.isFinite()
				|| !destinationIntrinsicEnd.isFinite()
				|| !Double.isFinite(intrinsicScale)
				|| !Double.isFinite(planeResidual)
				|| !Double.isFinite(orientationResidual)) {
			return undefinedRelation(relation);
		}

		Vector2 sourcePoint = sourceMap.forward(sourceIntrinsicStart);
		instrumentation.recordDiagramMapForwardEvaluation();
		Vector2 destinationPoint = destinationMap.forward(destinationIntrinsicStart);
		instrumentation.recordDiagramMapForwardEvaluation();
		Vector2 sourceEnd = sourceMap.forward(sourceIntrinsicEnd);
		instrumentation.recordDiagramMapForwardEvaluation();
		Vector2 destinationEnd = destinationMap.forward(destinationIntrinsicEnd);
		instrumentation.recordDiagramMapForwardEvaluation();
		if (!sourcePoint.isFinite() || !destinationPoint.isFinite()
				|| !sourceEnd.isFinite() || !destinationEnd.isFinite()) {
			return undefinedRelation(relation);
		}
		Vector2 sourceDirectionRaw = sourceMap.transformDirection(
				directionInFrame(sourceFrame, supportDirection));
		Vector2 destinationDirectionRaw = destinationMap.transformDirection(
				directionInFrame(destinationFrame, supportDirection));
		if (!normalizable(sourceDirectionRaw) || !normalizable(destinationDirectionRaw)) {
			return undefinedRelation(relation);
		}
		Vector2 sourceDirection = sourceDirectionRaw.normalized();
		Vector2 destinationDirection = destinationDirectionRaw.normalized();

		double directionResidual = sourceDirection.subtract(destinationDirection).norm();
		Vector2 sourceLineNormal = leftNormal(sourceDirection);
		Vector2 destinationLineNormal = leftNormal(destinationDirection);
		// Equality is between oriented diagram lines, not corresponding support
		// endpoints: longitudinal translation or parameter scale is immaterial.
		double sourceLineOffset = Math.max(Math.abs(sourceLineNormal.dot(
				destinationPoint.subtract(sourcePoint))), Math.abs(sourceLineNormal.dot(
				destinationEnd.subtract(sourcePoint))));
		double destinationLineOffset = Math.max(Math.abs(destinationLineNormal.dot(
				sourcePoint.subtract(destinationPoint))), Math.abs(
						destinationLineNormal.dot(sourceEnd.subtract(destinationPoint))));
		double lineOffsetResidual = Math.max(sourceLineOffset,
				destinationLineOffset);
		Vector2 sourceSideRaw = sourceMap.transformDirection(
				leftNormal(directionInFrame(sourceFrame, supportDirection)));
		Vector2 destinationSideRaw = destinationMap.transformDirection(
				leftNormal(directionInFrame(destinationFrame, supportDirection)));
		if (!normalizable(sourceSideRaw) || !normalizable(destinationSideRaw)) {
			return undefinedRelation(relation);
		}
		Vector2 sourceSide = sourceSideRaw.normalized();
		Vector2 destinationSide = destinationSideRaw.normalized();
		double foldSideDot = sourceSide.dot(destinationSide);
		boolean foldAccepted = relation.getKind() == ProjectionRelationKind.CHANGE_OF_PLANE
				|| foldSideDot * relation.getFoldSide().getExpectedSign()
				>= 1 - policy.getHingeTolerance();
		double sourcePullbackResidual = sourceLineOffset
				/ sourceMap.getDeclaredScale();
		double destinationPullbackResidual = destinationLineOffset
				/ destinationMap.getDeclaredScale();
		double lineNormalized = Math.max(sourcePullbackResidual,
				destinationPullbackResidual) / policy.hingeTolerance(intrinsicScale);
		double directionNormalized = directionResidual / policy.getHingeTolerance();
		double planeNormalized = planeResidual / policy.hingeTolerance(intrinsicScale);
		double orientationNormalized = orientationResidual
				/ policy.getHingeTolerance();
		double maximum = Math.max(Math.max(lineNormalized, directionNormalized),
				Math.max(planeNormalized, orientationNormalized));
		if (!Double.isFinite(lineOffsetResidual)
				|| !Double.isFinite(directionResidual)
				|| !Double.isFinite(foldSideDot)
				|| !Double.isFinite(lineNormalized)
				|| !Double.isFinite(directionNormalized)
				|| !Double.isFinite(planeNormalized)
				|| !Double.isFinite(orientationNormalized)
				|| !Double.isFinite(maximum)) {
			return undefinedRelation(relation);
		}
		ProjectionSystemStatus status = maximum <= 1 && foldAccepted
				? ProjectionSystemStatus.CONSISTENT
				: ProjectionSystemStatus.INCONSISTENT;
		return new ProjectionRelationEvidence(relation.getEvidenceKey(),
				relation.getKind(), status,
				lineOffsetResidual, directionResidual, planeResidual,
				orientationResidual, foldSideDot, maximum);
	}

	private static boolean normalizable(Vector2 vector) {
		double norm = vector.norm();
		return vector.isFinite() && Double.isFinite(norm) && norm > 0;
	}

	private static ProjectionRelationEvidence undefinedRelation(
			ProjectionRelationDefinition relation) {
		return new ProjectionRelationEvidence(relation.getEvidenceKey(),
				relation.getKind(), ProjectionSystemStatus.UNDEFINED,
				Double.NaN, Double.NaN, Double.NaN, Double.POSITIVE_INFINITY);
	}

	private ProjectionSystemStatus relationPrerequisiteStatus(
			ProjectionFrameDefinition sourceFrame, DiagramMapDefinition sourceMap,
			ProjectionFrameDefinition destinationFrame,
			DiagramMapDefinition destinationMap, NumericPolicy policy,
			SpatialSemanticInstrumentation instrumentation) {
		instrumentation.recordFrameEvaluation();
		ProjectionSystemStatus status = fromDefinition(sourceFrame.validate(policy));
		instrumentation.recordFrameEvaluation();
		status = combine(status, fromDefinition(destinationFrame.validate(policy)));
		status = combine(status, sourceMap.validate(policy));
		status = combine(status, destinationMap.validate(policy));
		if (!SemanticValueChecks.sameUnit(sourceFrame.getUnit(),
				sourceMap.getSourceUnit())
				|| !SemanticValueChecks.sameUnit(destinationFrame.getUnit(),
						destinationMap.getSourceUnit())
				|| !SemanticValueChecks.sameUnit(sourceFrame.getUnit(),
						destinationFrame.getUnit())
				|| !SemanticValueChecks.sameUnit(sourceMap.getDiagramUnit(),
						destinationMap.getDiagramUnit())) {
			status = combine(status, ProjectionSystemStatus.INCONSISTENT);
		}
		return status;
	}

	private static double maximumPlaneResidual(
			ProjectionFrameDefinition sourceFrame, Vector3 sourceNormal,
			ProjectionFrameDefinition destinationFrame, Vector3 destinationNormal,
			Vector3 supportStart, Vector3 supportEnd) {
		double sourceStart = Math.abs(sourceNormal.dot(
				supportStart.subtract(sourceFrame.getOrigin())));
		double sourceEnd = Math.abs(sourceNormal.dot(
				supportEnd.subtract(sourceFrame.getOrigin())));
		double destinationStart = Math.abs(destinationNormal.dot(
				supportStart.subtract(destinationFrame.getOrigin())));
		double destinationEnd = Math.abs(destinationNormal.dot(
				supportEnd.subtract(destinationFrame.getOrigin())));
		return Math.max(Math.max(sourceStart, sourceEnd),
				Math.max(destinationStart, destinationEnd));
	}

	private static Vector2 directionInFrame(ProjectionFrameDefinition frame,
			Vector3 direction) {
		return new Vector2(frame.getFirstAxis().dot(direction),
				frame.getSecondAxis().dot(direction));
	}

	private static Vector2 leftNormal(Vector2 direction) {
		return new Vector2(-direction.getY(), direction.getX());
	}

	private static ProjectionSystemStatus fromDefinition(
			SpatialDefinitionStatus status) {
		switch (status) {
		case DEFINED:
			return ProjectionSystemStatus.CONSISTENT;
		case DEGENERATE:
			return ProjectionSystemStatus.DEGENERATE;
		case UNDEFINED:
		default:
			return ProjectionSystemStatus.UNDEFINED;
		}
	}

	private static ProjectionSystemStatus combine(ProjectionSystemStatus first,
			ProjectionSystemStatus second) {
		if (first == ProjectionSystemStatus.UNDEFINED
				|| second == ProjectionSystemStatus.UNDEFINED) {
			return ProjectionSystemStatus.UNDEFINED;
		}
		if (first == ProjectionSystemStatus.DEGENERATE
				|| second == ProjectionSystemStatus.DEGENERATE) {
			return ProjectionSystemStatus.DEGENERATE;
		}
		if (first == ProjectionSystemStatus.INCONSISTENT
				|| second == ProjectionSystemStatus.INCONSISTENT) {
			return ProjectionSystemStatus.INCONSISTENT;
		}
		if (first == ProjectionSystemStatus.CONSISTENT
				|| second == ProjectionSystemStatus.CONSISTENT) {
			return ProjectionSystemStatus.CONSISTENT;
		}
		return ProjectionSystemStatus.NOT_EVALUATED;
	}
}
