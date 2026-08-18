/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.Objects;

/** Immutable geometric definition of an oriented orthographic frame. */
public final class ProjectionFrameDefinition {
	private final String evidenceKey;
	private final String evidenceContentKey;
	private final ProjectionFrameFamily family;
	private final Vector3 origin;
	private final Vector3 firstAxis;
	private final Vector3 secondAxis;
	private final FrameHandedness handedness;
	private final String unit;
	private final long revision;

	/**
	 * Creates an orthographic frame definition.
	 *
	 * @param origin frame origin in world coordinates
	 * @param firstAxis first ordered unit axis
	 * @param secondAxis second ordered unit axis
	 * @param handedness declared frame handedness
	 * @param unit world-coordinate unit
	 * @param revision source revision
	 */
	public ProjectionFrameDefinition(Vector3 origin, Vector3 firstAxis,
			Vector3 secondAxis, FrameHandedness handedness, String unit,
			long revision) {
		this(ProjectionFrameFamily.ORTHOGRAPHIC, origin, firstAxis, secondAxis,
				handedness, unit, revision);
	}

	/**
	 * Creates a frame definition in an admitted family.
	 *
	 * @param family admitted projection-frame family
	 * @param origin frame origin in world coordinates
	 * @param firstAxis first ordered unit axis
	 * @param secondAxis second ordered unit axis
	 * @param handedness declared frame handedness
	 * @param unit world-coordinate unit
	 * @param revision source revision
	 */
	public ProjectionFrameDefinition(ProjectionFrameFamily family, Vector3 origin,
			Vector3 firstAxis, Vector3 secondAxis, FrameHandedness handedness,
			String unit, long revision) {
		this(SemanticEvidenceKey.frame(family, origin, firstAxis, secondAxis,
				handedness, unit, revision), family, origin, firstAxis, secondAxis,
				handedness, unit, revision);
	}

	/** Creates a frame definition with a stable semantic evidence key. */
	public ProjectionFrameDefinition(String evidenceKey,
			ProjectionFrameFamily family, Vector3 origin, Vector3 firstAxis,
			Vector3 secondAxis, FrameHandedness handedness, String unit,
			long revision) {
		this.evidenceContentKey = SemanticEvidenceKey.frame(family, origin,
				firstAxis, secondAxis, handedness, unit, revision);
		this.evidenceKey = SemanticEvidenceKey.require(evidenceKey);
		this.family = Objects.requireNonNull(family);
		this.origin = Objects.requireNonNull(origin);
		this.firstAxis = Objects.requireNonNull(firstAxis);
		this.secondAxis = Objects.requireNonNull(secondAxis);
		this.handedness = Objects.requireNonNull(handedness);
		this.unit = SemanticValueChecks.requireUnit(unit);
		this.revision = SemanticValueChecks.requireRevision(revision);
	}

	/**
	 * Creates an orthographic frame definition.
	 *
	 * @return orthographic frame definition
	 */
	public static ProjectionFrameDefinition orthographic(Vector3 origin,
			Vector3 firstAxis, Vector3 secondAxis, FrameHandedness handedness,
			String unit, long revision) {
		return new ProjectionFrameDefinition(origin, firstAxis, secondAxis,
				handedness, unit, revision);
	}

	public ProjectionFrameFamily getFamily() {
		return family;
	}

	public String getEvidenceKey() {
		return evidenceKey;
	}

	String getEvidenceContentKey() {
		return evidenceContentKey;
	}

	public Vector3 getOrigin() {
		return origin;
	}

	public Vector3 getFirstAxis() {
		return firstAxis;
	}

	public Vector3 getSecondAxis() {
		return secondAxis;
	}

	/**
	 * Returns the first row of the orthographic point projection matrix.
	 *
	 * @return first point-projection row
	 */
	public Vector3 getFirstProjectionRow() {
		return firstAxis;
	}

	/**
	 * Returns the second row of the orthographic point projection matrix.
	 *
	 * @return second point-projection row
	 */
	public Vector3 getSecondProjectionRow() {
		return secondAxis;
	}

	/**
	 * Returns a defensive copy of the two point-projection matrix rows.
	 *
	 * @return ordered point-projection rows
	 */
	public Vector3[] getProjectionRows() {
		return new Vector3[] {firstAxis, secondAxis};
	}

	public FrameHandedness getHandedness() {
		return handedness;
	}

	public String getUnit() {
		return unit;
	}

	public long getRevision() {
		return revision;
	}

	/**
	 * Validates the finite orthonormal basis without consulting a view.
	 *
	 * @return definition status of the geometric frame
	 */
	public SpatialDefinitionStatus validate(NumericPolicy policy) {
		Objects.requireNonNull(policy);
		if (!origin.isFinite() || !firstAxis.isFinite() || !secondAxis.isFinite()) {
			return SpatialDefinitionStatus.UNDEFINED;
		}
		double firstNorm = firstAxis.norm();
		double secondNorm = secondAxis.norm();
		double axisDot = firstAxis.dot(secondAxis);
		double crossNorm = firstAxis.cross(secondAxis).norm();
		if (!Double.isFinite(firstNorm) || !Double.isFinite(secondNorm)
				|| !Double.isFinite(axisDot) || !Double.isFinite(crossNorm)) {
			return SpatialDefinitionStatus.UNDEFINED;
		}
		double scale = Math.max(firstNorm, secondNorm);
		double tolerance = policy.tolerance(scale);
		if (Math.abs(firstNorm - 1) > tolerance
				|| Math.abs(secondNorm - 1) > tolerance
				|| Math.abs(axisDot) > tolerance || crossNorm <= tolerance) {
			return SpatialDefinitionStatus.DEGENERATE;
		}
		return SpatialDefinitionStatus.DEFINED;
	}

	/**
	 * Returns the normal implied by the ordered basis and declared handedness.
	 *
	 * @return handed frame normal
	 */
	public Vector3 getNormal(NumericPolicy policy) {
		if (validate(policy) != SpatialDefinitionStatus.DEFINED) {
			throw new IllegalStateException("frame is not a finite orthonormal frame");
		}
		Vector3 cross = firstAxis.cross(secondAxis);
		return (handedness == FrameHandedness.RIGHT_HANDED
				? cross : cross.scale(-1)).normalized();
	}

	/**
	 * Applies the intrinsic orthographic point projection.
	 *
	 * @return intrinsic frame coordinates
	 */
	public Vector2 project(Vector3 point) {
		Objects.requireNonNull(point);
		Vector3 relative = point.subtract(origin);
		return new Vector2(firstAxis.dot(relative), secondAxis.dot(relative));
	}

	/**
	 * Validates the frame and then applies the intrinsic point projection.
	 *
	 * @return intrinsic frame coordinates
	 */
	public Vector2 project(Vector3 point, NumericPolicy policy) {
		if (validate(policy) != SpatialDefinitionStatus.DEFINED) {
			throw new IllegalStateException("frame is not valid for projection");
		}
		return project(point);
	}
}
