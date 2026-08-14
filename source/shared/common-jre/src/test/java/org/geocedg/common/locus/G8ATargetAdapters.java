/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.implicit.GeoImplicit;
import org.geogebra.common.kernel.kernelND.GeoConicND;
import org.geogebra.common.kernel.matrix.Coords;

/** Test-private snapshots of real GeoGebra 2D target authority. */
final class G8ATargetAdapters {
	private G8ATargetAdapters() {
	}

	enum Family {
		LINE,
		SEGMENT,
		RAY,
		CIRCLE,
		CONIC,
		FUNCTION,
		IMPLICIT
	}

	interface Target2D {
		Family family();

		String identity();

		double rawResidual(LocusPoint2D point);

		double residualScale(LocusPoint2D point);

		default double normalizedResidual(LocusPoint2D point) {
			double raw = rawResidual(point);
			double scale = residualScale(point);
			if (!Double.isFinite(raw) || !Double.isFinite(scale) || scale <= 0) {
				return Double.NaN;
			}
			return raw / scale;
		}

		boolean isMember(LocusPoint2D point, double coordinateTolerance);

		double derivativeAlong(LocusPoint2D point, LocusPoint2D tangent);
	}

	static final class LineTarget implements Target2D {
		private final Family family;
		private final String identity;
		private final double a;
		private final double b;
		private final double c;
		private final GeoLine limitedPath;

		LineTarget(String identity, GeoLine line) {
			this.identity = requireIdentity(identity);
			Objects.requireNonNull(line);
			a = line.getX();
			b = line.getY();
			c = line.getZ();
			limitedPath = line instanceof GeoSegment || line instanceof GeoRay
					? line : null;
			family = line instanceof GeoSegment ? Family.SEGMENT
					: line instanceof GeoRay ? Family.RAY : Family.LINE;
			if (!Double.isFinite(a) || !Double.isFinite(b)
					|| !Double.isFinite(c) || Math.hypot(a, b) == 0) {
				throw new IllegalArgumentException("Undefined line target");
			}
		}

		LineTarget(String identity, double a, double b, double c) {
			this.identity = requireIdentity(identity);
			this.a = a;
			this.b = b;
			this.c = c;
			limitedPath = null;
			family = Family.LINE;
			if (!Double.isFinite(a) || !Double.isFinite(b)
					|| !Double.isFinite(c) || Math.hypot(a, b) == 0) {
				throw new IllegalArgumentException("Undefined line target");
			}
		}

		@Override
		public Family family() {
			return family;
		}

		@Override
		public String identity() {
			return identity;
		}

		@Override
		public double rawResidual(LocusPoint2D point) {
			return a * point.getX() + b * point.getY() + c;
		}

		@Override
		public double residualScale(LocusPoint2D point) {
			return Math.hypot(a, b);
		}

		@Override
		public boolean isMember(LocusPoint2D point,
				double coordinateTolerance) {
			if (limitedPath == null) {
				return true;
			}
			return limitedPath.respectLimitedPath(
					new Coords(point.getX(), point.getY(), 1),
					coordinateTolerance);
		}

		@Override
		public double derivativeAlong(LocusPoint2D point,
				LocusPoint2D tangent) {
			return (a * tangent.getX() + b * tangent.getY())
					/ residualScale(point);
		}
	}

	static final class ConicTarget implements Target2D {
		private final Family family;
		private final String identity;
		private final double[] matrix;
		private final int type;
		private final double coefficientNorm;

		ConicTarget(String identity, GeoConicND conic, boolean circleOnly) {
			this.identity = requireIdentity(identity);
			Objects.requireNonNull(conic);
			matrix = conic.getFlatMatrix().clone();
			type = conic.getType();
			family = circleOnly ? Family.CIRCLE : Family.CONIC;
			if (circleOnly && !conic.isCircle()) {
				throw new IllegalArgumentException("Circle adapter requires a circle");
			}
			double normSquared = 0;
			for (double coefficient : matrix) {
				if (!Double.isFinite(coefficient)) {
					throw new IllegalArgumentException("Nonfinite conic matrix");
				}
				normSquared += coefficient * coefficient;
			}
			coefficientNorm = Math.sqrt(normSquared);
			if (coefficientNorm == 0) {
				throw new IllegalArgumentException("Zero conic equation");
			}
		}

		@Override
		public Family family() {
			return family;
		}

		@Override
		public String identity() {
			return identity;
		}

		int conicType() {
			return type;
		}

		double[] matrixSnapshot() {
			return matrix.clone();
		}

		@Override
		public double rawResidual(LocusPoint2D point) {
			return GeoConicND.evaluate(matrix, point.getX(), point.getY());
		}

		@Override
		public double residualScale(LocusPoint2D point) {
			double coordinateScale = Math.max(1,
					point.getX() * point.getX() + point.getY() * point.getY());
			return coefficientNorm * coordinateScale;
		}

		@Override
		public boolean isMember(LocusPoint2D point,
				double coordinateTolerance) {
			return Double.isFinite(normalizedResidual(point));
		}

		@Override
		public double derivativeAlong(LocusPoint2D point,
				LocusPoint2D tangent) {
			double x = point.getX();
			double y = point.getY();
			double gradientX = 2 * matrix[0] * x + 2 * matrix[3] * y
					+ 2 * matrix[4];
			double gradientY = 2 * matrix[1] * y + 2 * matrix[3] * x
					+ 2 * matrix[5];
			return (gradientX * tangent.getX()
					+ gradientY * tangent.getY()) / residualScale(point);
		}
	}

	static final class FunctionTarget implements Target2D {
		private final String identity;
		private final GeoFunction function;

		FunctionTarget(String identity, GeoFunction function) {
			this.identity = requireIdentity(identity);
			this.function = Objects.requireNonNull(function);
		}

		@Override
		public Family family() {
			return Family.FUNCTION;
		}

		@Override
		public String identity() {
			return identity;
		}

		@Override
		public double rawResidual(LocusPoint2D point) {
			return point.getY() - function.value(point.getX());
		}

		@Override
		public double residualScale(LocusPoint2D point) {
			return Math.max(1, Math.max(Math.abs(point.getY()),
					Math.abs(function.value(point.getX()))));
		}

		@Override
		public boolean isMember(LocusPoint2D point,
				double coordinateTolerance) {
			if (!Double.isFinite(rawResidual(point))) {
				return false;
			}
			return !function.hasInterval()
					|| point.getX() >= function.getIntervalMin()
							- coordinateTolerance
					&& point.getX() <= function.getIntervalMax()
							+ coordinateTolerance;
		}

		@Override
		public double derivativeAlong(LocusPoint2D point,
				LocusPoint2D tangent) {
			return Double.NaN;
		}
	}

	static final class ImplicitTarget implements Target2D {
		private final String identity;
		private final GeoImplicit implicit;

		ImplicitTarget(String identity, GeoImplicit implicit) {
			this.identity = requireIdentity(identity);
			this.implicit = Objects.requireNonNull(implicit);
		}

		@Override
		public Family family() {
			return Family.IMPLICIT;
		}

		@Override
		public String identity() {
			return identity;
		}

		@Override
		public double rawResidual(LocusPoint2D point) {
			return implicit.evaluateImplicitCurve(point.getX(), point.getY());
		}

		@Override
		public double residualScale(LocusPoint2D point) {
			return Math.max(1, Math.abs(rawResidual(point)));
		}

		@Override
		public boolean isMember(LocusPoint2D point,
				double coordinateTolerance) {
			return Double.isFinite(rawResidual(point));
		}

		@Override
		public double derivativeAlong(LocusPoint2D point,
				LocusPoint2D tangent) {
			return implicit.derivativeX(point.getX(), point.getY())
					* tangent.getX()
					+ implicit.derivativeY(point.getX(), point.getY())
							* tangent.getY();
		}
	}

	private static String requireIdentity(String identity) {
		if (identity == null || identity.trim().isEmpty()) {
			throw new IllegalArgumentException("Target identity is required");
		}
		return identity;
	}
}
