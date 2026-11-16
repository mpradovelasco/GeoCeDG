/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.intersection;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;

/**
 * Native eligibility of the positive radial root for every finite rotation angle.
 * A bounded phase cover proves the existing inherited selector predicates. It
 * supplies no angle estimate and does not identify coincident centre objects.
 */
final class NativeRadialRayGuard2D {
	private static final int PHASE_CELLS = 64;
	private NativeRadialRayGuard2D() { }

	/** @return an absolute bound of each computed root coordinate, or NaN without proof */
	static double enclose(Operand source, Operand center,
			NativeVariableRotationGuard2D.Proof rotation, SplineOutwardInterval2D radius) {
		if (rotation == null || !(radius.lower >= 2 * ConstructionBoxModel2D.EPSILON)) {
			return Double.NaN;
		}
		try {
			double cx = center.getValue(3), cy = center.getValue(4);
			SplineOutwardInterval2D dx = point(source.getValue(0)).divide(point(source.getValue(2)))
					.subtract(point(cx));
			SplineOutwardInterval2D dy = point(source.getValue(1)).divide(point(source.getValue(2)))
					.subtract(point(cy));
			SplineOutwardInterval2D norm = square(dx).add(square(dy));
			if (!(norm.lower > 0)) { return Double.NaN; }
			SplineOutwardInterval2D rSquared = square(radius);
			SplineOutwardInterval2D cxSquared = point(cx).multiply(point(cx));
			SplineOutwardInterval2D cySquared = point(cy).multiply(point(cy));
			SplineOutwardInterval2D first = rSquared.negate().add(cxSquared);
			SplineOutwardInterval2D matrix = first.add(cySquared);
			// GeoQuadricND.setSphereNDMatrix: -r*r, then +=cx*cx, then +=cy*cy.
			// The intermediate magnitude matters when the radius dominates the centre.
			double representation = point(roundoff(cxSquared)).add(point(roundoff(cySquared)))
					.add(point(roundoff(rSquared))).add(point(roundoff(first)))
					.add(point(roundoff(matrix))).upper;
			SplineOutwardInterval2D representedRadius = rSquared.add(
					new SplineOutwardInterval2D(-representation, representation));
			if (!(representedRadius.lower > 0)) { return Double.NaN; }
			double pathLow = Math.nextDown(Math.sqrt(Math.max(0,
					point(representedRadius.lower).divide(point(norm.upper)).lower)));
			if (!(pathLow > 1E-5)) { return Double.NaN; }
			double limit = Math.nextUp(Math.PI), width = 2 * limit;
			double magnitude = 0;
			for (int index = 0; index < PHASE_CELLS; index++) {
				double lower = index == 0 ? -limit : -limit + width * index / PHASE_CELLS;
				double upper = index == PHASE_CELLS - 1 ? limit
						: -limit + width * (index + 1) / PHASE_CELLS;
				SplineOutwardInterval2D phase = new SplineOutwardInterval2D(lower, upper);
				SplineOutwardInterval2D cosine = ConstructionIntervalModel2D.cosine(phase);
				SplineOutwardInterval2D sine = ConstructionIntervalModel2D.sine(phase);
				SplineOutwardInterval2D vx = dx.multiply(cosine).subtract(dy.multiply(sine));
				SplineOutwardInterval2D vy = dx.multiply(sine).add(dy.multiply(cosine));
				var box = new InheritedLineCircleBand2D.Box();
				box.driverError = Math.max(rotation.getErrorX(), rotation.getErrorY());
				box.pointX = cx; box.pointY = cy;
				box.driverX = vx.add(point(cx)); box.driverY = vy.add(point(cy));
				box.a = vy.negate(); box.b = vx;
				box.c = point(cx).multiply(vy).subtract(point(cy).multiply(vx));
				box.norm = norm;
				box.distance = point(0);
				box.discriminant = norm.multiply(representedRadius);
				box.a2 = 0; box.a4 = -cx; box.a5 = -cy;
				box.matrixA2MagnitudeUpper = magnitude(matrix);
				box.radiusSquared = representedRadius;
				box.halfAxisLower = radius.lower;
				box.radiusRepresentationError = representation;
				box.startX = point(cx); box.startY = point(cy);
				box.startIsDriver = false;
				box.pathLow = pathLow; box.endLow = Double.POSITIVE_INFINITY;
				box.requireStrictPositiveRay = true;
				if (!InheritedLineCircleBand2D.keepsOutput(box)) { return Double.NaN; }
				magnitude = Math.max(magnitude, box.computedCoordinateMagnitudeUpper);
			}
			return Double.isFinite(magnitude) && magnitude > 0 ? magnitude : Double.NaN;
		} catch (ArithmeticException failure) {
			return Double.NaN;
		}
	}

	private static SplineOutwardInterval2D point(double value) {
		return SplineOutwardInterval2D.point(value);
	}
	private static double magnitude(SplineOutwardInterval2D value) {
		return Math.max(Math.abs(value.lower), Math.abs(value.upper));
	}
	private static double roundoff(SplineOutwardInterval2D value) {
		return point(0x1.0p-53).multiply(point(magnitude(value))).add(point(Double.MIN_VALUE)).upper;
	}
	private static SplineOutwardInterval2D square(SplineOutwardInterval2D value) {
		double lo = value.containsZero() ? 0 : Math.min(Math.abs(value.lower), Math.abs(value.upper));
		double hi = magnitude(value);
		return new SplineOutwardInterval2D(Math.max(0, Math.nextDown(lo * lo)), Math.nextUp(hi * hi));
	}
}
