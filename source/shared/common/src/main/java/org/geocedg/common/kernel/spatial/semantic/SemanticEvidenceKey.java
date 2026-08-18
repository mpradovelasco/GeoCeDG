/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

/** Exact, label-independent fallback keys for pure semantic evidence. */
final class SemanticEvidenceKey {
	private SemanticEvidenceKey() {
	}

	static String require(String evidenceKey) {
		if (evidenceKey == null || evidenceKey.isEmpty()) {
			throw new IllegalArgumentException("evidenceKey must not be empty");
		}
		return evidenceKey;
	}

	static String map(DiagramMapFamily family, double a00, double a01,
			double a10, double a11, Vector2 translation, double declaredScale,
			DiagramOrientation orientation, String sourceUnit, String diagramUnit,
			long revision) {
		StringBuilder key = new StringBuilder("map");
		append(key, family);
		append(key, a00);
		append(key, a01);
		append(key, a10);
		append(key, a11);
		append(key, translation);
		append(key, declaredScale);
		append(key, orientation);
		append(key, sourceUnit);
		append(key, diagramUnit);
		append(key, revision);
		return key.toString();
	}

	static String observation(ProjectionFrameDefinition frame,
			DiagramMapDefinition map, Vector2 point,
			SpatialDefinitionStatus definitionStatus,
			RepresentationFidelity fidelity,
			CorrespondenceStatus correspondenceStatus, long revision) {
		StringBuilder key = new StringBuilder("observation");
		appendFrame(key, frame);
		append(key, map.getEvidenceKey());
		append(key, point);
		append(key, definitionStatus);
		append(key, fidelity);
		append(key, correspondenceStatus);
		append(key, revision);
		return key.toString();
	}

	static String frame(ProjectionFrameFamily family, Vector3 origin,
			Vector3 firstAxis, Vector3 secondAxis, FrameHandedness handedness,
			String unit, long revision) {
		StringBuilder key = new StringBuilder("frame");
		append(key, family);
		append(key, origin);
		append(key, firstAxis);
		append(key, secondAxis);
		append(key, handedness);
		append(key, unit);
		append(key, revision);
		return key.toString();
	}

	static String relation(ProjectionRelationKind kind,
			ProjectionFrameDefinition sourceFrame, DiagramMapDefinition sourceMap,
			ProjectionFrameDefinition destinationFrame,
			DiagramMapDefinition destinationMap, Vector3 supportStart,
			Vector3 supportEnd, ProjectionRelationOrientation orientation,
			ProjectionRelationProvenance provenance, FoldSide foldSide,
			long revision) {
		StringBuilder key = new StringBuilder("relation");
		append(key, kind);
		appendFrame(key, sourceFrame);
		append(key, sourceMap.getEvidenceKey());
		appendFrame(key, destinationFrame);
		append(key, destinationMap.getEvidenceKey());
		append(key, supportStart);
		append(key, supportEnd);
		append(key, orientation);
		append(key, provenance);
		append(key, foldSide);
		append(key, revision);
		return key.toString();
	}

	static String residual(Vector2 intrinsicResidual, double intrinsicNorm,
			double normalizedIntrinsic, Vector2 diagramResidual, double diagramNorm,
			double normalizedDiagram) {
		StringBuilder key = new StringBuilder("residual");
		append(key, intrinsicResidual);
		append(key, intrinsicNorm);
		append(key, normalizedIntrinsic);
		append(key, diagramResidual);
		append(key, diagramNorm);
		append(key, normalizedDiagram);
		return key.toString();
	}

	static String mapEvidence(DiagramMapFamily family,
			DiagramOrientation orientation, String sourceUnit, String diagramUnit,
			double declaredScale, long revision, ProjectionSystemStatus status) {
		StringBuilder key = new StringBuilder("map-evidence");
		append(key, family);
		append(key, orientation);
		append(key, sourceUnit);
		append(key, diagramUnit);
		append(key, declaredScale);
		append(key, revision);
		append(key, status);
		return key.toString();
	}

	static String relationEvidence(ProjectionRelationKind kind,
			ProjectionSystemStatus status, double lineOffsetResidual,
			double directionResidual, double supportPlaneResidual,
			double supportOrientationResidual, double foldSideDotProduct,
			double maximumNormalizedResidual) {
		StringBuilder key = new StringBuilder("relation-evidence");
		append(key, kind);
		append(key, status);
		append(key, lineOffsetResidual);
		append(key, directionResidual);
		append(key, supportPlaneResidual);
		append(key, supportOrientationResidual);
		append(key, foldSideDotProduct);
		append(key, maximumNormalizedResidual);
		return key.toString();
	}

	private static void appendFrame(StringBuilder target,
			ProjectionFrameDefinition frame) {
		append(target, frame.getFamily());
		append(target, frame.getOrigin());
		append(target, frame.getFirstAxis());
		append(target, frame.getSecondAxis());
		append(target, frame.getHandedness());
		append(target, frame.getUnit());
		append(target, frame.getRevision());
	}

	private static void append(StringBuilder target, Vector2 value) {
		append(target, value.getX());
		append(target, value.getY());
	}

	private static void append(StringBuilder target, Vector3 value) {
		append(target, value.getX());
		append(target, value.getY());
		append(target, value.getZ());
	}

	private static void append(StringBuilder target, double value) {
		append(target, Long.toHexString(Double.doubleToLongBits(value)));
	}

	private static void append(StringBuilder target, long value) {
		append(target, Long.toString(value));
	}

	private static void append(StringBuilder target, Object value) {
		String token = String.valueOf(value);
		target.append('|').append(token.length()).append(':').append(token);
	}
}
