/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.MembershipStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ResidualQuantityKind;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetEvaluationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.TargetFamily;
import org.geogebra.common.kernel.geos.GeoConic;

/** Captured regular first-order adapter for one nondegenerate full conic. */
public final class NondegenerateConicIntersectionTarget2D
		implements LocusIntersectionTarget2D {
	private static final String ADAPTER_VERSION =
			"g8c1-nondegenerate-conic-normal-residual/v1";

	private final TargetFamily family;
	private final String targetIdentity;
	private final long targetUpdateStamp;
	private final double[] matrix;
	private final double characteristicScale;
	private final IntersectionResidualContract2D contract;

	/** Captures the authoritative GeoConic matrix for one query revision. */
	public NondegenerateConicIntersectionTarget2D(GeoConic conic,
			String targetIdentity, long targetUpdateStamp) {
		java.util.Objects.requireNonNull(conic);
		if (!conic.isDefined() || conic.isDegenerate() || conic.isCircle()
				|| !(conic.isEllipse() || conic.isParabola()
						|| conic.isHyperbola())) {
			throw new IllegalArgumentException(
					"G8C1 conic adapter requires ellipse, parabola or hyperbola");
		}
		this.family = conic.isEllipse() ? TargetFamily.ELLIPSE
				: conic.isParabola() ? TargetFamily.PARABOLA
						: TargetFamily.HYPERBOLA;
		this.targetIdentity = requireIdentity(targetIdentity);
		this.targetUpdateStamp = requireStamp(targetUpdateStamp);
		this.matrix = conic.getFlatMatrix().clone();
		if (matrix.length != 6) {
			throw new IllegalArgumentException("2D conic matrix must have six terms");
		}
		for (double coefficient : matrix) {
			if (!Double.isFinite(coefficient)) {
				throw new IllegalArgumentException(
						"Conic matrix coefficients must be finite");
			}
		}
		this.characteristicScale = conicScale(conic.getHalfAxes());
		this.contract = new IntersectionResidualContract2D(ADAPTER_VERSION,
				ResidualQuantityKind.FIRST_ORDER_NORMAL_LENGTH,
				"model-coordinate",
				"captured GeoConic G divided by hypot(dG/dx,dG/dy) at a "
						+ "regular point; first-order normal residual, not exact "
						+ "Euclidean distance",
				"max(1,finite-positive-canonical-half-axis)");
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
	public TargetCandidateEvaluation2D evaluateCandidateLevel(
			LocusPoint2D point) {
		RegularResidual value = regularResidual(point);
		return value.established
				? TargetCandidateEvaluation2D.established(value.normalized,
						characteristicScale,
						"Regular conic first-order level from captured matrix")
				: TargetCandidateEvaluation2D.unavailable(value.status,
						value.diagnostic);
	}

	@Override
	public TargetResidual2D evaluateResidual(LocusPoint2D point) {
		TargetResidualEvaluation2D evaluation = evaluateResidualEvidence(point);
		if (!evaluation.isEstablished()) {
			throw new IllegalArgumentException(evaluation.getDiagnostic());
		}
		return evaluation.getResidual().get();
	}

	@Override
	public TargetResidualEvaluation2D evaluateResidualEvidence(
			LocusPoint2D point) {
		RegularResidual value = regularResidual(point);
		if (!value.established) {
			return TargetResidualEvaluation2D.unavailable(value.status,
					value.diagnostic);
		}
		return TargetResidualEvaluation2D.established(
				new TargetResidual2D(value.raw, value.gradientNorm,
						value.normalized, characteristicScale, contract),
				"Independent regular conic residual verification");
	}

	@Override
	public TargetMembership2D evaluateMembership(LocusPoint2D point,
			double coordinateTolerance) {
		boolean regular = regularResidual(point).established;
		return new TargetMembership2D(regular ? MembershipStatus.MEMBER
				: MembershipStatus.NOT_ESTABLISHED, false,
				regular ? "Full nondegenerate conic has no path-domain restriction"
						: "Conic membership lacks regular residual normalization");
	}

	@Override
	public TargetContactEvidence2D evaluateContact(LocusPoint2D point,
			LocusDifferentialEvaluation2D differential) {
		if (differential.getRegularity() != Regularity.REGULAR) {
			return TargetContactEvidence2D.notEstablished(
					"Source differential is not established regular");
		}
		double gradientX = gradientX(point);
		double gradientY = gradientY(point);
		double gradientNorm = Math.hypot(gradientX, gradientY);
		LocusPoint2D tangent = differential.getDerivative();
		double speed = Math.hypot(tangent.getX(), tangent.getY());
		if (!Double.isFinite(gradientNorm) || !Double.isFinite(speed)
				|| gradientNorm == 0 || speed == 0) {
			return TargetContactEvidence2D.notEstablished(
					"Regular conic normal/source tangent normalization failed");
		}
		double indicator = (gradientX * tangent.getX()
				+ gradientY * tangent.getY()) / (gradientNorm * speed);
		return TargetContactEvidence2D.established(indicator,
				"d(first-order-conic-normal-level)/d(source-arc-length)",
				"Normalized captured conic gradient/source tangent factor");
	}

	private RegularResidual regularResidual(LocusPoint2D point) {
		if (point == null || !Double.isFinite(point.getX())
				|| !Double.isFinite(point.getY())) {
			return RegularResidual.unavailable(
					TargetEvaluationStatus.TARGET_UNDEFINED,
					"Conic evaluation point is nonfinite");
		}
		double raw = matrix[0] * point.getX() * point.getX()
				+ matrix[1] * point.getY() * point.getY() + matrix[2]
				+ 2 * matrix[3] * point.getX() * point.getY()
				+ 2 * matrix[4] * point.getX()
				+ 2 * matrix[5] * point.getY();
		double gradientNorm = Math.hypot(gradientX(point), gradientY(point));
		if (!Double.isFinite(raw) || !Double.isFinite(gradientNorm)) {
			return RegularResidual.unavailable(
					TargetEvaluationStatus.TARGET_UNDEFINED,
					"Conic value or gradient is nonfinite");
		}
		if (gradientNorm == 0) {
			return RegularResidual.unavailable(
					TargetEvaluationStatus.RESIDUAL_NORMALIZATION_UNAVAILABLE,
					"Conic gradient is zero; regular first-order residual is unavailable");
		}
		return RegularResidual.established(raw, gradientNorm,
				raw / gradientNorm);
	}

	private double gradientX(LocusPoint2D point) {
		return 2 * matrix[0] * point.getX()
				+ 2 * matrix[3] * point.getY() + 2 * matrix[4];
	}

	private double gradientY(LocusPoint2D point) {
		return 2 * matrix[1] * point.getY()
				+ 2 * matrix[3] * point.getX() + 2 * matrix[5];
	}

	private static double conicScale(double[] halfAxes) {
		double scale = 1;
		if (halfAxes != null) {
			for (double halfAxis : halfAxes) {
				if (Double.isFinite(halfAxis) && halfAxis > 0) {
					scale = Math.max(scale, halfAxis);
				}
			}
		}
		return scale;
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

	private static final class RegularResidual {
		private final boolean established;
		private final double raw;
		private final double gradientNorm;
		private final double normalized;
		private final TargetEvaluationStatus status;
		private final String diagnostic;

		private RegularResidual(boolean established, double raw,
				double gradientNorm, double normalized,
				TargetEvaluationStatus status, String diagnostic) {
			this.established = established;
			this.raw = raw;
			this.gradientNorm = gradientNorm;
			this.normalized = normalized;
			this.status = status;
			this.diagnostic = diagnostic;
		}

		static RegularResidual established(double raw, double gradientNorm,
				double normalized) {
			return new RegularResidual(true, raw, gradientNorm, normalized,
					TargetEvaluationStatus.ESTABLISHED,
					"Regular first-order residual established");
		}

		static RegularResidual unavailable(TargetEvaluationStatus status,
				String diagnostic) {
			return new RegularResidual(false, 0, 0, 0, status, diagnostic);
		}
	}
}
