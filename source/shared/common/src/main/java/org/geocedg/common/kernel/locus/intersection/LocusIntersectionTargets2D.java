/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MembershipStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ResidualQuantityKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoVec2D;

/** Captures G8B-authorized ordinary 2D target authority. */
public final class LocusIntersectionTargets2D {
	private static final String LINE_ADAPTER_VERSION =
			"g8b-line-distance-adapter/v1";
	private static final String CIRCLE_ADAPTER_VERSION =
			"g8b-circle-radial-distance-adapter/v1";

	private LocusIntersectionTargets2D() {
		// Factory.
	}

	/**
	 * Captures one coherent target snapshot.
	 *
	 * @return immutable target-family adapter for the captured revision
	 * @throws IllegalArgumentException for undefined or unsupported targets
	 */
	public static LocusIntersectionTarget2D capture(GeoElement target,
			String targetIdentity, long targetUpdateStamp) {
		if (target == null || !target.isDefined()) {
			throw new IllegalArgumentException("Target must be currently defined");
		}
		if (target instanceof GeoSegment) {
			return new LineTarget((GeoLine) target, TargetFamily.SEGMENT,
					targetIdentity, targetUpdateStamp);
		}
		if (target instanceof GeoRay) {
			return new LineTarget((GeoLine) target, TargetFamily.RAY,
					targetIdentity, targetUpdateStamp);
		}
		if (target instanceof GeoLine) {
			return new LineTarget((GeoLine) target, TargetFamily.LINE,
					targetIdentity, targetUpdateStamp);
		}
		if (target instanceof GeoConic && ((GeoConic) target).isCircle()) {
			return new CircleTarget((GeoConic) target, targetIdentity,
					targetUpdateStamp);
		}
		throw new IllegalArgumentException(
				"G8B supports only line, segment, ray and nondegenerate circle");
	}

	/** @return whether a GeoElement has an authorized G8B target family */
	public static boolean supports(GeoElement target) {
		return target instanceof GeoLine
				|| target instanceof GeoConic && ((GeoConic) target).isCircle();
	}

	/** @return captured-family classification, including unsupported input */
	public static TargetFamily familyOf(GeoElement target) {
		if (target instanceof GeoSegment) {
			return TargetFamily.SEGMENT;
		}
		if (target instanceof GeoRay) {
			return TargetFamily.RAY;
		}
		if (target instanceof GeoLine) {
			return TargetFamily.LINE;
		}
		if (target instanceof GeoConic && ((GeoConic) target).isCircle()) {
			return TargetFamily.CIRCLE;
		}
		return TargetFamily.UNSUPPORTED;
	}

	private static final class LineTarget implements LocusIntersectionTarget2D {
		private final TargetFamily family;
		private final String targetIdentity;
		private final long targetUpdateStamp;
		private final double a;
		private final double b;
		private final double c;
		private final double normalLength;
		private final double startX;
		private final double startY;
		private final double directionX;
		private final double directionY;
		private final double directionSquared;
		private final IntersectionResidualContract2D contract;

		LineTarget(GeoLine line, TargetFamily family, String targetIdentity,
				long targetUpdateStamp) {
			this.family = family;
			this.targetIdentity = requireIdentity(targetIdentity);
			this.targetUpdateStamp = requireStamp(targetUpdateStamp);
			a = line.getX();
			b = line.getY();
			c = line.getZ();
			normalLength = Math.hypot(a, b);
			if (!Double.isFinite(a) || !Double.isFinite(b)
					|| !Double.isFinite(c) || normalLength == 0) {
				throw new IllegalArgumentException("Invalid line coefficients");
			}
			GeoPoint start = line.getStartPoint();
			GeoPoint end = line.getEndPoint();
			if (family == TargetFamily.SEGMENT) {
				requireFinitePoint(start, "Segment start");
				requireFinitePoint(end, "Segment end");
				startX = start.getInhomX();
				startY = start.getInhomY();
				directionX = end.getInhomX() - startX;
				directionY = end.getInhomY() - startY;
			} else if (family == TargetFamily.RAY) {
				requireFinitePoint(start, "Ray start");
				startX = start.getInhomX();
				startY = start.getInhomY();
				directionX = b;
				directionY = -a;
			} else {
				startX = 0;
				startY = 0;
				directionX = 0;
				directionY = 0;
			}
			directionSquared = directionX * directionX
					+ directionY * directionY;
			if (family != TargetFamily.LINE && (!Double.isFinite(directionSquared)
					|| directionSquared == 0)) {
				throw new IllegalArgumentException("Degenerate limited line target");
			}
			contract = new IntersectionResidualContract2D(
					LINE_ADAPTER_VERSION,
					ResidualQuantityKind.MODEL_COORDINATE_DISTANCE,
					"model-coordinate",
					"signed homogeneous line equation divided by hypot(a,b)",
					"unit model-coordinate distance");
		}

		@Override
		public TargetFamily getFamily() {
			return family;
		}

		@Override
		public String getTargetIdentity() {
			return targetIdentity;
		}

		@Override
		public long getTargetUpdateStamp() {
			return targetUpdateStamp;
		}

		@Override
		public IntersectionResidualContract2D getResidualContract() {
			return contract;
		}

		@Override
		public TargetResidual2D evaluateResidual(LocusPoint2D point) {
			double raw = a * point.getX() + b * point.getY() + c;
			return new TargetResidual2D(raw, normalLength, raw / normalLength,
					1, contract);
		}

		@Override
		public TargetMembership2D evaluateMembership(LocusPoint2D point,
				double coordinateTolerance) {
			if (family == TargetFamily.LINE) {
				return new TargetMembership2D(MembershipStatus.MEMBER, false,
						"Unbounded line has no limited-path restriction");
			}
			double offsetX = point.getX() - startX;
			double offsetY = point.getY() - startY;
			double parameter = (offsetX * directionX + offsetY * directionY)
					/ directionSquared;
			double parameterTolerance = coordinateTolerance
					/ Math.sqrt(directionSquared);
			if (family == TargetFamily.SEGMENT) {
				boolean member = parameter >= -parameterTolerance
						&& parameter <= 1 + parameterTolerance;
				boolean boundary = member && (Math.abs(parameter)
						<= parameterTolerance
						|| Math.abs(parameter - 1) <= parameterTolerance);
				return new TargetMembership2D(member ? MembershipStatus.MEMBER
						: MembershipStatus.NOT_MEMBER, boundary,
						"Captured segment parameter=" + parameter);
			}
			boolean member = parameter >= -parameterTolerance;
			return new TargetMembership2D(member ? MembershipStatus.MEMBER
					: MembershipStatus.NOT_MEMBER,
					member && Math.abs(parameter) <= parameterTolerance,
					"Captured ray parameter=" + parameter);
		}

		@Override
		public TargetContactEvidence2D evaluateContact(LocusPoint2D point,
				LocusDifferentialEvaluation2D differential) {
			if (differential.getRegularity() != Regularity.REGULAR) {
				return TargetContactEvidence2D.notEstablished(
						"Source differential is not established regular");
			}
			LocusPoint2D tangent = differential.getDerivative();
			double speed = Math.hypot(tangent.getX(), tangent.getY());
			if (!Double.isFinite(speed) || speed == 0) {
				return TargetContactEvidence2D.notEstablished(
						"Source arc-length normalization is unavailable");
			}
			double indicator = (a * tangent.getX() + b * tangent.getY())
					/ (normalLength * speed);
			return TargetContactEvidence2D.established(indicator,
					"d(line-distance)/d(source-arc-length)",
					"Normalized line-normal/source-tangent directional factor");
		}
	}

	private static final class CircleTarget
			implements LocusIntersectionTarget2D {
		private final String targetIdentity;
		private final long targetUpdateStamp;
		private final double centerX;
		private final double centerY;
		private final double radius;
		private final IntersectionResidualContract2D contract;

		CircleTarget(GeoConic circle, String targetIdentity,
				long targetUpdateStamp) {
			this.targetIdentity = requireIdentity(targetIdentity);
			this.targetUpdateStamp = requireStamp(targetUpdateStamp);
			GeoVec2D center = circle.getTranslationVector();
			double[] halfAxes = circle.getHalfAxes();
			centerX = center.getX();
			centerY = center.getY();
			radius = halfAxes[0];
			if (!Double.isFinite(centerX) || !Double.isFinite(centerY)
					|| !Double.isFinite(radius) || radius <= 0) {
				throw new IllegalArgumentException(
						"Circle target must be finite and nondegenerate");
			}
			contract = new IntersectionResidualContract2D(
					CIRCLE_ADAPTER_VERSION,
					ResidualQuantityKind.MODEL_COORDINATE_DISTANCE,
					"model-coordinate",
					"signed radial distance from captured GeoConic circle",
					"max(1,captured-radius)");
		}

		@Override
		public TargetFamily getFamily() {
			return TargetFamily.CIRCLE;
		}

		@Override
		public String getTargetIdentity() {
			return targetIdentity;
		}

		@Override
		public long getTargetUpdateStamp() {
			return targetUpdateStamp;
		}

		@Override
		public IntersectionResidualContract2D getResidualContract() {
			return contract;
		}

		@Override
		public TargetResidual2D evaluateResidual(LocusPoint2D point) {
			double radialDistance = Math.hypot(point.getX() - centerX,
					point.getY() - centerY) - radius;
			return new TargetResidual2D(radialDistance, 1, radialDistance,
					Math.max(1, radius), contract);
		}

		@Override
		public TargetMembership2D evaluateMembership(LocusPoint2D point,
				double coordinateTolerance) {
			return new TargetMembership2D(MembershipStatus.MEMBER, false,
					"A verified radial residual is circle membership evidence");
		}

		@Override
		public TargetContactEvidence2D evaluateContact(LocusPoint2D point,
				LocusDifferentialEvaluation2D differential) {
			if (differential.getRegularity() != Regularity.REGULAR) {
				return TargetContactEvidence2D.notEstablished(
						"Source differential is not established regular");
			}
			double normalX = point.getX() - centerX;
			double normalY = point.getY() - centerY;
			double normalLength = Math.hypot(normalX, normalY);
			LocusPoint2D tangent = differential.getDerivative();
			double speed = Math.hypot(tangent.getX(), tangent.getY());
			if (normalLength == 0 || speed == 0) {
				return TargetContactEvidence2D.notEstablished(
						"Circle normal or source arc-length normalization failed");
			}
			double indicator = (normalX * tangent.getX()
					+ normalY * tangent.getY()) / (normalLength * speed);
			return TargetContactEvidence2D.established(indicator,
					"d(radial-distance)/d(source-arc-length)",
					"Normalized circle-normal/source-tangent directional factor");
		}
	}

	private static String requireIdentity(String identity) {
		if (identity == null || identity.trim().isEmpty()) {
			throw new IllegalArgumentException("Target identity is required");
		}
		return identity;
	}

	private static long requireStamp(long stamp) {
		if (stamp < 0) {
			throw new IllegalArgumentException("Target update stamp is invalid");
		}
		return stamp;
	}

	private static void requireFinitePoint(GeoPoint point, String role) {
		if (point == null || !point.isDefined()
				|| !Double.isFinite(point.getInhomX())
				|| !Double.isFinite(point.getInhomY())) {
			throw new IllegalArgumentException(role + " is invalid");
		}
	}
}
