/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */
package org.geocedg.common.kernel.locus.intersection;

import java.util.function.Consumer;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.NumericGuarantee;

/**
 * M08-S evidence in the captured represented configuration, not a STRICT incidence
 * certificate. Reuses the existing private interval models; no inverse/minimum claim.
 */
public final class M08LocalAddressEvidence2D {
	/** A full captured-model pass, not a semantic evaluator miss or scalar FLOP. */
	public enum Work { CAPTURE, SMOOTHNESS, VALUE_ENCLOSURE, FIRST_JET_ENCLOSURE }

	/** Independent local proof outcome, never an R6 admission state. */
	public enum Status {
		ESTABLISHED, MODEL_UNAVAILABLE, DOMAIN_BOUNDARY, MODEL_UNRESOLVED,
		REGULARITY_NOT_ESTABLISHED, OUTSIDE_RADIUS, PROXIMITY_NOT_ESTABLISHED
	}

	private final Status status;
	private final SplineOutwardInterval2D[] value;
	private final SplineOutwardInterval2D[] derivative;
	private final SplineOutwardInterval2D neighborhood;
	private final double distanceSquaredUpper;

	private M08LocalAddressEvidence2D(Status status, SplineOutwardInterval2D[] value,
			SplineOutwardInterval2D[] derivative, SplineOutwardInterval2D neighborhood,
			double distanceSquaredUpper) {
		this.status = status;
		this.value = value;
		this.derivative = derivative;
		this.neighborhood = neighborhood;
		this.distanceSquaredUpper = distanceSquaredUpper;
	}

	/**
	 * Proves a point bound and a regular C1 interior neighborhood. An adjacent-double
	 * parameter box is proof material, not a tolerance, identity or distance minimum.
	 * The caller must establish active-authority fidelity and component ownership.
	 * @return immutable evidence or a typed refusal
	 */
	public static M08LocalAddressEvidence2D establish(LocusDefinition2D definition,
			String branch, double parameter, LocusInterval2D component,
			double targetX, double targetY, double radius) {
		return establish(definition, branch, parameter, component, targetX, targetY,
				radius, work -> { });
	}

	/** Same evidence, charging each interval pass before execution to its owner. */
	public static M08LocalAddressEvidence2D establish(LocusDefinition2D definition,
			String branch, double parameter, LocusInterval2D component,
			double targetX, double targetY, double radius, Consumer<Work> charge) {
		if (!Double.isFinite(parameter) || !Double.isFinite(targetX)
				|| !Double.isFinite(targetY) || !Double.isFinite(radius) || radius <= 0) {
			throw new IllegalArgumentException("Finite point placement policy required");
		}
		double lower = Math.nextDown(parameter);
		double upper = Math.nextUp(parameter);
		if (!(component.getLower() < lower && upper < component.getUpper())) {
			return refused(Status.DOMAIN_BOUNDARY);
		}
		try {
			charge.accept(Work.CAPTURE);
			CertifiedIntervalCurveModel2D model =
					CertifiedIntervalCurveModel2D.capture(definition, branch);
			if (model == null) {
				return refused(Status.MODEL_UNAVAILABLE);
			}
			SplineOutwardInterval2D neighborhood = new SplineOutwardInterval2D(lower, upper);
			charge.accept(Work.SMOOTHNESS);
			if (!model.isSmooth(neighborhood)) {
				return refused(Status.MODEL_UNRESOLVED);
			}
			charge.accept(Work.VALUE_ENCLOSURE);
			SplineOutwardInterval2D[] value = model.evaluate(
					SplineOutwardInterval2D.point(parameter), false);
			charge.accept(Work.FIRST_JET_ENCLOSURE);
			SplineOutwardInterval2D[] derivative = model.evaluate(neighborhood, true);
			if (derivative[0].containsZero() && derivative[1].containsZero()) {
				return refused(Status.REGULARITY_NOT_ESTABLISHED);
			}
			SplineOutwardInterval2D dx = value[0].subtract(SplineOutwardInterval2D.point(targetX));
			SplineOutwardInterval2D dy = value[1].subtract(SplineOutwardInterval2D.point(targetY));
			SplineOutwardInterval2D distance = dx.multiply(dx).add(dy.multiply(dy));
			SplineOutwardInterval2D radiusSquared = SplineOutwardInterval2D.point(radius)
					.multiply(SplineOutwardInterval2D.point(radius));
			if (!(distance.upper < radiusSquared.lower)) {
				return refused(distance.lower > radiusSquared.upper ? Status.OUTSIDE_RADIUS
						: Status.PROXIMITY_NOT_ESTABLISHED);
			}
			return new M08LocalAddressEvidence2D(Status.ESTABLISHED, value,
					derivative, neighborhood, distance.upper);
		} catch (ArithmeticException | IllegalArgumentException exception) {
			return refused(Status.MODEL_UNRESOLVED);
		}
	}

	private static M08LocalAddressEvidence2D refused(Status status) {
		return new M08LocalAddressEvidence2D(status, null, null, null, Double.NaN);
	}

	public Status getStatus() {
		return status;
	}

	public boolean isEstablished() {
		return status == Status.ESTABLISHED;
	}

	/** @return proof grade of the represented-model enclosure, not of Java coordinates */
	public NumericGuarantee getNumericGuarantee() {
		return isEstablished() ? NumericGuarantee.CERTIFIED_ERROR_BOUND
				: NumericGuarantee.FLOATING_POINT_UNCERTIFIED;
	}

	/** @return whether the independent floating forward value is inside its model enclosure */
	public boolean encloses(LocusPoint2D point) {
		return isEstablished() && point != null && value[0].lower <= point.getX()
				&& point.getX() <= value[0].upper && value[1].lower <= point.getY()
				&& point.getY() <= value[1].upper;
	}

	public double getValueLower(int coordinate) {
		return value[coordinate].lower;
	}

	public double getValueUpper(int coordinate) {
		return value[coordinate].upper;
	}

	public double getDerivativeLower(int coordinate) {
		return derivative[coordinate].lower;
	}

	public double getDerivativeUpper(int coordinate) {
		return derivative[coordinate].upper;
	}

	public double getNeighborhoodLower() {
		return neighborhood.lower;
	}

	public double getNeighborhoodUpper() {
		return neighborhood.upper;
	}

	public double getDistanceSquaredUpper() {
		return distanceSquaredUpper;
	}
}
