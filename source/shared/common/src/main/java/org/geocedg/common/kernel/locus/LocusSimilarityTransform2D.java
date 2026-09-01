/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.Objects;

/** Immutable finite 2D similarity consumed only by semantic Locus V2 evaluation. */
public final class LocusSimilarityTransform2D {
	/** Supported ordinary 2D similarity families. */
	public enum Kind {
		TRANSLATION, ROTATION, POINT_REFLECTION, LINE_REFLECTION, DILATION
	}

	private static final String VERSION = "locus-similarity/v1";

	private final Kind kind;
	private final LocusSourceSnapshot2D values;

	private LocusSimilarityTransform2D(Kind kind, double... values) {
		this.kind = Objects.requireNonNull(kind);
		this.values = new LocusSourceSnapshot2D(values);
	}

	/** @return translation by the supplied finite vector */
	public static LocusSimilarityTransform2D translation(double x, double y) {
		return new LocusSimilarityTransform2D(Kind.TRANSLATION, x, y);
	}

	/** @return rotation through {@code angle} about the finite center */
	public static LocusSimilarityTransform2D rotation(double angle,
			double centerX, double centerY) {
		return new LocusSimilarityTransform2D(Kind.ROTATION, angle, centerX,
				centerY);
	}

	/** @return central reflection about the finite point */
	public static LocusSimilarityTransform2D pointReflection(double centerX,
			double centerY) {
		return new LocusSimilarityTransform2D(Kind.POINT_REFLECTION, centerX,
				centerY);
	}

	/**
	 * Creates reflection in {@code ax + by + c = 0}. Coefficients are normalized
	 * to one canonical oriented representative so line rescaling is not semantic.
	 *
	 * @return finite axial reflection
	 */
	public static LocusSimilarityTransform2D lineReflection(double a, double b,
			double c) {
		if (!Double.isFinite(a) || !Double.isFinite(b) || !Double.isFinite(c)) {
			throw new IllegalArgumentException("Reflection line must be finite");
		}
		double coefficientScale = Math.max(Math.abs(a), Math.abs(b));
		if (coefficientScale == 0) {
			throw new IllegalArgumentException("Reflection line must be defined");
		}
		double scaledA = a / coefficientScale;
		double scaledB = b / coefficientScale;
		double scaledNorm = Math.hypot(scaledA, scaledB);
		double normalX = scaledA / scaledNorm;
		double normalY = scaledB / scaledNorm;
		double offset = (c / coefficientScale) / scaledNorm;
		if (!Double.isFinite(offset)) {
			throw new IllegalArgumentException("Reflection line is nonfinite");
		}
		if (normalX < 0 || normalX == 0 && normalY < 0) {
			normalX = -normalX;
			normalY = -normalY;
			offset = -offset;
		}
		return new LocusSimilarityTransform2D(Kind.LINE_REFLECTION, normalX,
				normalY, offset);
	}

	/** @return uniform dilation by the finite factor about the finite center */
	public static LocusSimilarityTransform2D dilation(double factor,
			double centerX, double centerY) {
		return new LocusSimilarityTransform2D(Kind.DILATION, factor, centerX,
				centerY);
	}

	/**
	 * Applies this mapping in world coordinates. It never changes the source
	 * semantic parameter or address.
	 *
	 * @return finite transformed point
	 */
	public LocusPoint2D transform(LocusPoint2D source) {
		double x = source.getX();
		double y = source.getY();
		double transformedX;
		double transformedY;
		switch (kind) {
		case TRANSLATION:
			transformedX = x + values.get(0);
			transformedY = y + values.get(1);
			break;
		case ROTATION:
			if (values.get(0) == 0) {
				transformedX = x;
				transformedY = y;
			} else {
				double cosine = Math.cos(values.get(0));
				double sine = Math.sin(values.get(0));
				double centeredX = x - values.get(1);
				double centeredY = y - values.get(2);
				transformedX = values.get(1) + cosine * centeredX
						- sine * centeredY;
				transformedY = values.get(2) + sine * centeredX
						+ cosine * centeredY;
			}
			break;
		case POINT_REFLECTION:
			transformedX = 2 * values.get(0) - x;
			transformedY = 2 * values.get(1) - y;
			break;
		case LINE_REFLECTION:
			double signedDistance = values.get(0) * x + values.get(1) * y
					+ values.get(2);
			transformedX = x - 2 * values.get(0) * signedDistance;
			transformedY = y - 2 * values.get(1) * signedDistance;
			break;
		case DILATION:
			if (values.get(0) == 0) {
				// A collapsed image maps every source-valid address exactly to the
				// finite center. Return it before subtracting so finite extreme
				// operands cannot produce infinity and then 0 * infinity = NaN.
				transformedX = values.get(1);
				transformedY = values.get(2);
			} else if (values.get(0) == 1) {
				transformedX = x;
				transformedY = y;
			} else {
				transformedX = values.get(1)
						+ values.get(0) * (x - values.get(1));
				transformedY = values.get(2)
						+ values.get(0) * (y - values.get(2));
			}
			break;
		default:
			throw new IllegalStateException("Unsupported similarity family");
		}
		return new LocusPoint2D(transformedX, transformedY);
	}

	/**
	 * Applies only the linear part of this similarity to a semantic derivative.
	 * Translation therefore leaves derivatives unchanged and a zero dilation
	 * returns the exact zero vector.
	 *
	 * @return finite transformed derivative
	 */
	public LocusPoint2D transformDerivative(double derivativeX,
			double derivativeY) {
		double transformedX;
		double transformedY;
		switch (kind) {
		case TRANSLATION:
			transformedX = derivativeX;
			transformedY = derivativeY;
			break;
		case ROTATION:
			double cosine = Math.cos(values.get(0));
			double sine = Math.sin(values.get(0));
			transformedX = cosine * derivativeX - sine * derivativeY;
			transformedY = sine * derivativeX + cosine * derivativeY;
			break;
		case POINT_REFLECTION:
			transformedX = -derivativeX;
			transformedY = -derivativeY;
			break;
		case LINE_REFLECTION:
			double normalComponent = values.get(0) * derivativeX
					+ values.get(1) * derivativeY;
			transformedX = derivativeX - 2 * values.get(0) * normalComponent;
			transformedY = derivativeY - 2 * values.get(1) * normalComponent;
			break;
		case DILATION:
			transformedX = values.get(0) * derivativeX;
			transformedY = values.get(0) * derivativeY;
			break;
		default:
			throw new IllegalStateException("Unsupported similarity family");
		}
		return new LocusPoint2D(transformedX, transformedY);
	}

	/**
	 * Applies this affine similarity to two equal-degree polynomial coordinate
	 * arrays. Only the constant coefficient receives the affine translation.
	 *
	 * @return defensive transformed x/y descending-power coefficients
	 */
	public double[][] transformPolynomialCoefficients(double[] sourceX,
			double[] sourceY) {
		if (sourceX == null || sourceY == null
				|| sourceX.length != sourceY.length || sourceX.length == 0) {
			throw new IllegalArgumentException(
					"Equal nonempty coordinate polynomials are required");
		}
		LocusPoint2D origin = transform(new LocusPoint2D(0, 0));
		LocusPoint2D xBasis = transform(new LocusPoint2D(1, 0));
		LocusPoint2D yBasis = transform(new LocusPoint2D(0, 1));
		double xx = xBasis.getX() - origin.getX();
		double xy = yBasis.getX() - origin.getX();
		double yx = xBasis.getY() - origin.getY();
		double yy = yBasis.getY() - origin.getY();
		double[][] transformed = new double[2][sourceX.length];
		for (int index = 0; index < sourceX.length; index++) {
			transformed[0][index] = xx * sourceX[index] + xy * sourceY[index];
			transformed[1][index] = yx * sourceX[index] + yy * sourceY[index];
		}
		int constant = sourceX.length - 1;
		transformed[0][constant] += origin.getX();
		transformed[1][constant] += origin.getY();
		return transformed;
	}

	/** @return family of this immutable transformation */
	public Kind getKind() {
		return kind;
	}

	/** @return whether the geometric image collapses while addresses remain */
	public boolean isCollapsed() {
		return kind == Kind.DILATION && values.get(0) == 0;
	}

	/** @return exact metric scale for this similarity */
	public double getLengthScale() {
		return kind == Kind.DILATION ? Math.abs(values.get(0)) : 1;
	}

	/** @return deterministic semantic content signature */
	public String getSemanticSignature() {
		return VERSION + "|" + kind + values.getSemanticSignature();
	}
}
