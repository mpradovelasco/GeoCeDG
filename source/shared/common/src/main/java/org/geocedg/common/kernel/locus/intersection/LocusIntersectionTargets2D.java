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
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetSupportStatus;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoVec2D;
import org.geogebra.common.kernel.implicit.GeoImplicitCurve;

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
		IntersectionTargetSupport2D support = assess(target);
		if (!support.isSupported()) {
			throw new IllegalArgumentException(support.getStatus() + ": "
					+ support.getDiagnostic());
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
		if (target instanceof GeoConic) {
			return new NondegenerateConicIntersectionTarget2D((GeoConic) target,
					targetIdentity, targetUpdateStamp);
		}
		if (target instanceof GeoFunction) {
			return new BoundedFunctionGraphIntersectionTarget2D(
					(GeoFunction) target, targetIdentity, targetUpdateStamp);
		}
		if (target instanceof GeoImplicitCurve) {
			return new RegularPolynomialImplicitIntersectionTarget2D(
					(GeoImplicitCurve) target, targetIdentity, targetUpdateStamp);
		}
		throw new IllegalArgumentException(
				"Supported target assessment and capture dispatch disagree");
	}

	/** @return whether a GeoElement has an authorized G8B/G8C1 target family */
	public static boolean supports(GeoElement target) {
		return assess(target).isSupported();
	}

	/** @return captured-family classification, including unsupported input */
	public static TargetFamily familyOf(GeoElement target) {
		return assess(target).getFamily();
	}

	/**
	 * Assesses exact target support without throwing for ordinary unsupported
	 * geometry.
	 *
	 * @return typed closed adapter decision
	 */
	public static IntersectionTargetSupport2D assess(GeoElement target) {
		String targetType = target == null ? "null"
				: target.getClass().getSimpleName();
		if (target == null || !target.isDefined()) {
			return unsupported(TargetSupportStatus.TARGET_UNDEFINED, targetType,
					"Target must be currently defined");
		}
		if (target instanceof GeoSegment) {
			return supported(TargetFamily.SEGMENT, targetType,
					"Captured finite segment support line and membership");
		}
		if (target instanceof GeoRay) {
			return supported(TargetFamily.RAY, targetType,
					"Captured oriented ray support line and membership");
		}
		if (target instanceof GeoLine) {
			return supported(TargetFamily.LINE, targetType,
					"Captured normalized line equation");
		}
		if (target instanceof GeoConic && ((GeoConic) target).isCircle()) {
			return supported(TargetFamily.CIRCLE, targetType,
					"Captured nondegenerate circle geometry");
		}
		if (target instanceof GeoConic) {
			GeoConic conic = (GeoConic) target;
			if (conic.isDegenerate() || !(conic.isEllipse()
					|| conic.isParabola() || conic.isHyperbola())) {
				return unsupported(
						TargetSupportStatus.UNSUPPORTED_TARGET_SUBTYPE,
						targetType,
						"Degenerate and non-ellipse/parabola/hyperbola conics are outside G8C1");
			}
			TargetFamily family = conic.isEllipse() ? TargetFamily.ELLIPSE
					: conic.isParabola() ? TargetFamily.PARABOLA
							: TargetFamily.HYPERBOLA;
			return supported(family, targetType,
					"Captured nondegenerate canonical conic matrix");
		}
		if (target instanceof GeoFunction) {
			GeoFunction function = (GeoFunction) target;
			if (function.isBooleanFunction() || function.getFunction() == null) {
				return unsupported(
						TargetSupportStatus.UNSUPPORTED_TARGET_SUBTYPE,
						targetType,
						"G8C1 supports real single-variable function graphs only");
			}
			if (!function.hasInterval()
					|| !Double.isFinite(function.getIntervalMin())
					|| !Double.isFinite(function.getIntervalMax())
					|| function.getIntervalMin() > function.getIntervalMax()) {
				return unsupported(TargetSupportStatus.DOMAIN_NOT_EXPLICIT,
						targetType,
						"Function graph needs an explicit finite semantic "
								+ "x-domain; view bounds are forbidden");
			}
			return supported(TargetFamily.BOUNDED_FUNCTION_GRAPH, targetType,
					"Captured real function expression and explicit finite x-domain");
		}
		if (target instanceof GeoImplicitCurve) {
			GeoImplicitCurve implicit = (GeoImplicitCurve) target;
			if (implicit.getCoeff() == null) {
				return unsupported(
						TargetSupportStatus.NONPOLYNOMIAL_IMPLICIT,
						targetType,
						"G8C1 supports polynomial implicit coefficient authority only");
			}
			if (!RegularPolynomialImplicitIntersectionTarget2D
					.hasFinitePolynomialCoefficients(implicit.getCoeff())) {
				return unsupported(
						TargetSupportStatus.RESIDUAL_NORMALIZATION_UNAVAILABLE,
						targetType,
						"Implicit polynomial coefficients must be finite and nonzero");
			}
			return supported(TargetFamily.REGULAR_POLYNOMIAL_IMPLICIT,
					targetType,
					"Captured finite polynomial coefficient representation");
		}
		return unsupported(TargetSupportStatus.UNSUPPORTED_TARGET_SUBTYPE,
				targetType, "Target GeoElement has no authorized G8B/G8C1 adapter");
	}

	private static IntersectionTargetSupport2D supported(TargetFamily family,
			String targetType, String diagnostic) {
		return new IntersectionTargetSupport2D(family,
				TargetSupportStatus.SUPPORTED, targetType, diagnostic);
	}

	private static IntersectionTargetSupport2D unsupported(
			TargetSupportStatus status, String targetType, String diagnostic) {
		return new IntersectionTargetSupport2D(TargetFamily.UNSUPPORTED, status,
				targetType, diagnostic);
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
