/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.spatial.semantic;

import java.util.Objects;

/** Immutable affine map from intrinsic frame coordinates to common diagram coordinates. */
public final class DiagramMapDefinition {
	private final String evidenceKey;
	private final String evidenceContentKey;
	private final DiagramMapFamily family;
	private final double a00;
	private final double a01;
	private final double a10;
	private final double a11;
	private final Vector2 translation;
	private final double declaredScale;
	private final DiagramOrientation orientation;
	private final String sourceUnit;
	private final String diagramUnit;
	private final long revision;

	/**
	 * Creates a persisted-family affine diagram map.
	 *
	 * @param family admitted affine-map family
	 * @param a00 first-row, first-column coefficient
	 * @param a01 first-row, second-column coefficient
	 * @param a10 second-row, first-column coefficient
	 * @param a11 second-row, second-column coefficient
	 * @param translation common-diagram translation
	 * @param declaredScale declared similarity scale
	 * @param orientation declared diagram orientation
	 * @param sourceUnit intrinsic source-coordinate unit
	 * @param diagramUnit common-diagram coordinate unit
	 * @param revision source revision
	 */
	public DiagramMapDefinition(DiagramMapFamily family, double a00, double a01,
			double a10, double a11, Vector2 translation, double declaredScale,
			DiagramOrientation orientation, String sourceUnit, String diagramUnit,
			long revision) {
		this(SemanticEvidenceKey.map(family, a00, a01, a10, a11, translation,
				declaredScale, orientation, sourceUnit, diagramUnit, revision), family,
				a00, a01, a10, a11, translation, declaredScale, orientation,
				sourceUnit, diagramUnit, revision);
	}

	/**
	 * Creates a persisted-family affine diagram map with a stable evidence key.
	 *
	 * @param evidenceKey stable semantic or durable map identity
	 */
	public DiagramMapDefinition(String evidenceKey, DiagramMapFamily family,
			double a00, double a01, double a10, double a11, Vector2 translation,
			double declaredScale, DiagramOrientation orientation, String sourceUnit,
			String diagramUnit, long revision) {
		this.evidenceContentKey = SemanticEvidenceKey.map(family, a00, a01, a10,
				a11, translation, declaredScale, orientation, sourceUnit,
				diagramUnit, revision);
		this.evidenceKey = SemanticEvidenceKey.require(evidenceKey);
		this.family = Objects.requireNonNull(family);
		this.a00 = a00;
		this.a01 = a01;
		this.a10 = a10;
		this.a11 = a11;
		this.translation = Objects.requireNonNull(translation);
		this.declaredScale = declaredScale;
		this.orientation = Objects.requireNonNull(orientation);
		this.sourceUnit = SemanticValueChecks.requireUnit(sourceUnit);
		this.diagramUnit = SemanticValueChecks.requireUnit(diagramUnit);
		this.revision = SemanticValueChecks.requireRevision(revision);
	}

	public String getEvidenceKey() {
		return evidenceKey;
	}

	String getEvidenceContentKey() {
		return evidenceContentKey;
	}

	/**
	 * Creates an oriented isometry with identical source and diagram units.
	 *
	 * @return the oriented-isometry definition
	 */
	public static DiagramMapDefinition orientedIsometry(double a00, double a01,
			double a10, double a11, Vector2 translation,
			DiagramOrientation orientation, String unit, long revision) {
		return new DiagramMapDefinition(DiagramMapFamily.ORIENTED_ISOMETRY,
				a00, a01, a10, a11, translation, 1, orientation, unit, unit,
				revision);
	}

	/**
	 * Creates an admitted unit-similarity map.
	 *
	 * @return the unit-similarity definition
	 */
	public static DiagramMapDefinition unitSimilarity(double a00, double a01,
			double a10, double a11, Vector2 translation, double declaredScale,
			DiagramOrientation orientation, String sourceUnit, String diagramUnit,
			long revision) {
		return new DiagramMapDefinition(DiagramMapFamily.UNIT_SIMILARITY,
				a00, a01, a10, a11, translation, declaredScale, orientation,
				sourceUnit, diagramUnit, revision);
	}

	public DiagramMapFamily getFamily() {
		return family;
	}

	public double getA00() {
		return a00;
	}

	public double getA01() {
		return a01;
	}

	public double getA10() {
		return a10;
	}

	public double getA11() {
		return a11;
	}

	public Vector2 getTranslation() {
		return translation;
	}

	public double getDeclaredScale() {
		return declaredScale;
	}

	public DiagramOrientation getOrientation() {
		return orientation;
	}

	public String getSourceUnit() {
		return sourceUnit;
	}

	public String getDiagramUnit() {
		return diagramUnit;
	}

	public long getRevision() {
		return revision;
	}

	/** @return determinant of the affine map's linear part */
	public double determinant() {
		return a00 * a11 - a01 * a10;
	}

	/**
	 * Validates invertibility, declared orientation, scale and map family.
	 *
	 * @return system status of this map definition
	 */
	public ProjectionSystemStatus validate(NumericPolicy policy) {
		Objects.requireNonNull(policy);
		if (!Double.isFinite(a00) || !Double.isFinite(a01)
				|| !Double.isFinite(a10) || !Double.isFinite(a11)
				|| !translation.isFinite() || !Double.isFinite(declaredScale)) {
			return ProjectionSystemStatus.UNDEFINED;
		}
		if (!(declaredScale > 0)) {
			return ProjectionSystemStatus.DEGENERATE;
		}
		double firstColumnNorm = Math.hypot(a00, a10);
		double secondColumnNorm = Math.hypot(a01, a11);
		double matrixScale = Math.max(firstColumnNorm, secondColumnNorm);
		double determinant = determinant();
		if (!Double.isFinite(determinant) || !Double.isFinite(matrixScale)) {
			return ProjectionSystemStatus.UNDEFINED;
		}
		if (determinant == 0 || firstColumnNorm == 0 || secondColumnNorm == 0) {
			return ProjectionSystemStatus.DEGENERATE;
		}
		if (Math.signum(determinant) != orientation.getDeterminantSign()) {
			return ProjectionSystemStatus.INCONSISTENT;
		}
		double relativeTolerance = policy.getMapTolerance();
		double columnDot = a00 * a01 + a10 * a11;
		double normalizedDot = Math.abs(columnDot)
				/ (firstColumnNorm * secondColumnNorm);
		double normalizedScaleDifference = Math.abs(
				firstColumnNorm - secondColumnNorm) / matrixScale;
		if (!Double.isFinite(normalizedDot)
				|| normalizedDot > relativeTolerance
				|| normalizedScaleDifference > relativeTolerance) {
			return ProjectionSystemStatus.INCONSISTENT;
		}
		if (family == DiagramMapFamily.ORIENTED_ISOMETRY) {
			double tolerance = policy.mapTolerance(1);
			if (Math.abs(firstColumnNorm - 1) > tolerance
					|| Math.abs(declaredScale - 1) > tolerance
					|| !SemanticValueChecks.sameUnit(sourceUnit, diagramUnit)) {
				return ProjectionSystemStatus.INCONSISTENT;
			}
		} else if (Math.abs(firstColumnNorm - declaredScale)
				/ Math.max(firstColumnNorm, declaredScale) > relativeTolerance) {
			return ProjectionSystemStatus.INCONSISTENT;
		}
		return ProjectionSystemStatus.CONSISTENT;
	}

	/**
	 * Applies the declared affine map without consulting any display transform.
	 *
	 * @return common-diagram point
	 */
	public Vector2 forward(Vector2 intrinsicPoint) {
		Objects.requireNonNull(intrinsicPoint);
		return new Vector2(a00 * intrinsicPoint.getX() + a01 * intrinsicPoint.getY(),
				a10 * intrinsicPoint.getX() + a11 * intrinsicPoint.getY())
				.add(translation);
	}

	/**
	 * Validates and applies the declared affine map.
	 *
	 * @return common-diagram point
	 */
	public Vector2 forward(Vector2 intrinsicPoint, NumericPolicy policy) {
		ensureValid(policy);
		return forward(intrinsicPoint);
	}

	/**
	 * Applies the inverse affine map; callers should validate the map first.
	 *
	 * @return intrinsic frame point
	 */
	public Vector2 inverse(Vector2 diagramPoint) {
		Objects.requireNonNull(diagramPoint);
		double determinant = determinant();
		if (!Double.isFinite(determinant) || determinant == 0) {
			throw new IllegalStateException("diagram map is not invertible");
		}
		Vector2 shifted = diagramPoint.subtract(translation);
		return new Vector2((a11 * shifted.getX() - a01 * shifted.getY()) / determinant,
				(-a10 * shifted.getX() + a00 * shifted.getY()) / determinant);
	}

	/**
	 * Validates and applies the inverse affine map.
	 *
	 * @return intrinsic frame point
	 */
	public Vector2 inverse(Vector2 diagramPoint, NumericPolicy policy) {
		ensureValid(policy);
		return inverse(diagramPoint);
	}

	/**
	 * Applies only the linear part, for induced line and direction evidence.
	 *
	 * @return transformed common-diagram direction
	 */
	public Vector2 transformDirection(Vector2 intrinsicDirection) {
		Objects.requireNonNull(intrinsicDirection);
		return new Vector2(a00 * intrinsicDirection.getX()
				+ a01 * intrinsicDirection.getY(),
				a10 * intrinsicDirection.getX() + a11 * intrinsicDirection.getY());
	}

	private void ensureValid(NumericPolicy policy) {
		if (validate(policy) != ProjectionSystemStatus.CONSISTENT) {
			throw new IllegalStateException("diagram map is not valid for evaluation");
		}
	}
}
