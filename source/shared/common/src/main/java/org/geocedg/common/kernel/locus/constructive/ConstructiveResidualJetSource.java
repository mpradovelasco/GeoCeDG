/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.Collections;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Circle;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Line;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Point;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator.Result;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetEnclosure2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetSource2D;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;

/**
 * The residual of a Locus V2 and its target in the constructive configuration {@code Sigma_C}
 * ({@code locus-v2-intersections.md}, constructive authority amendment; AUT-7): the constructive
 * cone of the slice, with the driver at every parameter of a box as the ideal point of its
 * constructive support, and the constructive target, both evaluated in interval jets, so that
 * the enclosures contain the ideal values of the constructive objects. The conflict rule, the
 * existence grades and the re-qualification of G8B-R1 decide in this configuration in a document
 * governed by {@code constructive-authority/v1}. A slice or a target outside what interval jets
 * enclose, such as a boundary substitute that moves with the driver, has no source: no
 * constructive evidence is computed for it.
 */
public final class ConstructiveResidualJetSource implements ResidualJetSource2D {
	/** Name of this decision configuration. */
	public static final String CONFIGURATION = "constructive/constructive-target";

	private final ConstructiveCone cone;
	private final int traced;
	private final int target;
	private final int driver;
	private final CertifiedConstructionProgram2D.Driver family;
	private final double[] frame;
	private final Object support;

	private ConstructiveResidualJetSource(ConstructiveCone cone, int traced, int target,
			int driver, CertifiedConstructionProgram2D.Driver family, double[] frame,
			Object support) {
		this.cone = cone;
		this.traced = traced;
		this.target = target;
		this.driver = driver;
		this.family = family;
		this.frame = frame;
		this.support = support;
	}

	/**
	 * @param locus a Locus V2 driven by a point on a circle or a segment
	 * @param targetObject the target of the query
	 * @return the residual in the constructive configuration, or {@code null} when the slice,
	 *         its driver parametrization or the target has no interval-jet enclosure
	 */
	public static ResidualJetSource2D capture(GeoLocusV2 locus, GeoElement targetObject) {
		AlgoElement parent = locus.getParentAlgorithm();
		if (!(parent instanceof AlgoDependentPointLocusV2) || parent.getInputLength() < 2
				|| !(parent.getInput(1) instanceof GeoPoint)
				|| !((GeoPoint) parent.getInput(1)).isPointOnPath()) {
			return null;
		}
		ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(
				locus.getSemanticDefinition());
		if (model == null) {
			return null;
		}
		CertifiedConstructionProgram2D program = model.getProgram();
		double[] frame = null;
		if (program.getDriver() == CertifiedConstructionProgram2D.Driver.CIRCLE) {
			frame = new double[] {program.getDriverConstant(2), program.getDriverConstant(3),
					program.getDriverConstant(4), program.getDriverConstant(5)};
			if (!isAxisFrame(frame)) {
				return null;
			}
		} else if (program.getDriver() != CertifiedConstructionProgram2D.Driver.SEGMENT) {
			return null;
		}
		GeoElement tracedObject = parent.getInput(0).toGeoElement();
		GeoPoint driverPoint = (GeoPoint) parent.getInput(1);
		GeoElement supportObject = driverPoint.getPath().toGeoElement();
		try {
			ConstructiveCone.Builder builder = ConstructiveCone.builder(locus.getConstruction());
			int targetNode = builder.add(targetObject);
			int supportNode = builder.add(supportObject);
			int tracedNode = builder.addSlice(tracedObject, driverPoint);
			ConstructiveCone cone = builder.build();
			for (ConstructiveConeNode node : cone.getNodes()) {
				ConstructiveOperation operation = node.getOperation();
				if (node.isDriverSlice() && (operation == ConstructiveOperation.BOUNDARY_POINT
						|| operation == ConstructiveOperation.BOUNDARY_NUMBER
						|| operation == ConstructiveOperation.OUTSIDE)) {
					// A boundary substitute that moves with the driver has no value at the
					// parameters of a box.
					return null;
				}
			}
			Result supportValue = new ConstructiveConeEvaluator<>(cone,
					IntervalJet.Arithmetic.INSTANCE, true, Collections.emptyMap())
							.evaluate(supportNode);
			Object value = supportValue.hasValue() ? supportValue.getValue() : null;
			boolean compatible = program.getDriver()
					== CertifiedConstructionProgram2D.Driver.CIRCLE ? value instanceof Circle
							: value instanceof Line && ((Line<?>) value).isLimited()
									&& !((Line<?>) value).isRay();
			if (!compatible) {
				return null;
			}
			return new ConstructiveResidualJetSource(cone, tracedNode, targetNode,
					cone.getReplacementNode(), program.getDriver(), frame, value);
		} catch (RuntimeException outsideTheClass) {
			return null;
		}
	}

	@Override
	public String getConfiguration() {
		return CONFIGURATION;
	}

	@Override
	public boolean isConstructive() {
		return true;
	}

	@Override
	public ResidualJetEnclosure2D enclose(double lower, double upper) {
		IntervalJet[] jet = residual(lower, upper);
		if (jet == null) {
			return null;
		}
		try {
			return new ResidualJetEnclosure2D(bounds(jet[0].getValue()),
					bounds(jet[0].getFirst()), bounds(jet[0].getSecond()));
		} catch (ArithmeticException unbounded) {
			return null;
		}
	}

	@Override
	public double[] normalizedResidual(double parameter) {
		IntervalJet[] jet = residual(parameter, parameter);
		if (jet == null) {
			return null;
		}
		try {
			return bounds(jet[1].getValue());
		} catch (ArithmeticException exception) {
			return null;
		}
	}

	/**
	 * @return {@code {h, normalized residual}} on the box, or {@code null} when the slice or
	 *         the target is not valid on it
	 */
	@SuppressWarnings("unchecked")
	private IntervalJet[] residual(double lower, double upper) {
		try {
			IntervalJet parameter = IntervalJet.of(OutwardInterval.of(lower, upper),
					OutwardInterval.point(1), OutwardInterval.point(0));
			Point<IntervalJet> driverPoint = driverPoint(parameter);
			if (driverPoint == null) {
				return null;
			}
			Map<Integer, Object> fixed = Collections.singletonMap(driver, driverPoint);
			ConstructiveConeEvaluator<IntervalJet> evaluator = new ConstructiveConeEvaluator<>(
					cone, IntervalJet.Arithmetic.INSTANCE, true, Collections.emptyMap(), fixed);
			Result point = evaluator.evaluate(traced);
			Result object = evaluator.evaluate(target);
			if (!point.hasValue() || !object.hasValue()
					|| !(point.getValue() instanceof Point)) {
				return null;
			}
			Point<IntervalJet> p = (Point<IntervalJet>) point.getValue();
			if (object.getValue() instanceof Line) {
				Line<IntervalJet> line = (Line<IntervalJet>) object.getValue();
				IntervalJet h = line.getA().multiply(p.getX()).add(line.getB().multiply(p.getY()))
						.add(line.getC());
				IntervalJet norm = line.getA().multiply(line.getA())
						.add(line.getB().multiply(line.getB())).sqrt();
				return new IntervalJet[] {h, h.divide(norm)};
			}
			if (object.getValue() instanceof Circle) {
				Circle<IntervalJet> circle = (Circle<IntervalJet>) object.getValue();
				IntervalJet dx = p.getX().subtract(circle.getCenterX());
				IntervalJet dy = p.getY().subtract(circle.getCenterY());
				IntervalJet squared = dx.multiply(dx).add(dy.multiply(dy));
				IntervalJet h = squared.subtract(circle.getRadiusSquared());
				OutwardInterval radial = squared.getValue().sqrt()
						.subtract(circle.getRadiusSquared().getValue().sqrt());
				return new IntervalJet[] {h, IntervalJet.constant(radial)};
			}
			return null;
		} catch (ArithmeticException | IllegalArgumentException undefined) {
			return null;
		}
	}

	/**
	 * The point of the constructive support at the parameter, with the parametrization of the
	 * captured driver: {@code m + r (cos t e0 + sin t e1)} on a circle of the axis frame,
	 * {@code start + t (end - start)} on a segment.
	 */
	@SuppressWarnings("unchecked")
	private Point<IntervalJet> driverPoint(IntervalJet parameter) {
		OutwardInterval box = parameter.getValue();
		if (family == CertifiedConstructionProgram2D.Driver.CIRCLE) {
			OutwardInterval[] trig = DriverParameterEnclosure.cosSin(box.getLower(),
					box.getUpper());
			if (trig == null) {
				return null;
			}
			IntervalJet cos = IntervalJet.of(trig[0], trig[1].negate(), trig[0].negate());
			IntervalJet sin = IntervalJet.of(trig[1], trig[0], trig[1].negate());
			Circle<IntervalJet> circle = (Circle<IntervalJet>) support;
			IntervalJet radius = circle.getRadiusSquared().sqrt();
			IntervalJet along = cos.multiply(constant(frame[0]))
					.add(sin.multiply(constant(frame[2])));
			IntervalJet across = cos.multiply(constant(frame[1]))
					.add(sin.multiply(constant(frame[3])));
			return new Point<>(circle.getCenterX().add(radius.multiply(along)),
					circle.getCenterY().add(radius.multiply(across)));
		}
		Line<IntervalJet> segment = (Line<IntervalJet>) support;
		return new Point<>(segment.getStartX().add(parameter.multiply(segment.getB())),
				segment.getStartY().subtract(parameter.multiply(segment.getA())));
	}

	/** @return whether the frame is an exact orthonormal frame of the axes */
	private static boolean isAxisFrame(double[] frame) {
		for (double component : frame) {
			if (component != 0 && component != 1 && component != -1) {
				return false;
			}
		}
		double cross = frame[0] * frame[3] - frame[1] * frame[2];
		return frame[0] * frame[2] + frame[1] * frame[3] == 0 && (cross == 1 || cross == -1);
	}

	private static IntervalJet constant(double value) {
		return IntervalJet.constant(OutwardInterval.point(value));
	}

	private static double[] bounds(OutwardInterval interval) {
		if (!interval.isFinite()) {
			throw new ArithmeticException("Unbounded enclosure");
		}
		return new double[] {interval.getLower(), interval.getUpper()};
	}
}
