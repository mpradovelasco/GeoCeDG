/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geogebra.common.kernel.PathParameter;
import org.geogebra.common.kernel.algos.AlgoDilate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.algos.AlgoJoinPoints;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoMidpoint;
import org.geogebra.common.kernel.algos.AlgoMirror;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoRotate;
import org.geogebra.common.kernel.algos.AlgoRotatePoint;
import org.geogebra.common.kernel.algos.AlgoTranslate;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.geos.GeoAxis;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoVec3D;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.util.MyMath;

/**
 * Captures the class-v1 construction program of one reconstructible evaluator
 * slice. It reads only the evaluator's isolated objects, never live
 * construction state, and refuses every shape outside the class.
 */
final class CertifiedConstructionCapture2D {
	private final GeoElement state;
	private final Map<GeoElement, Integer> nodes = new IdentityHashMap<>();
	private final List<Step> steps = new ArrayList<>();

	private CertifiedConstructionCapture2D(GeoElement state) {
		this.state = state;
		nodes.put(state, 0);
	}

	/**
	 * @return class-v1 program of the isolated slice, or empty when the slice is
	 *         outside the class
	 */
	static Optional<CertifiedConstructionProgram2D> capture(
			SemanticGeneratorDescriptor1D descriptor, GeoElement dependentPoint,
			GeoElement state, GeoElement support, double[][] affine) {
		try {
			LocusInterval2D domain = descriptor.getDeclaredDomain();
			switch (descriptor.getFamily()) {
			case SCALAR_STATE:
				if (affine == null || descriptor.isPeriodic()) {
					return Optional.empty();
				}
				return Optional.of(new CertifiedConstructionProgram2D(
						Driver.SCALAR_AFFINE, new double[] {affine[0][0], affine[0][1],
								affine[1][0], affine[1][1]}, domain, List.of()));
			case CIRCLE_POINT:
				return program(Driver.CIRCLE, circle(state, support, false), domain,
						dependentPoint, state);
			case CIRCULAR_ARC_POINT:
				return program(Driver.CIRCULAR_ARC, circle(state, support, true),
						domain, dependentPoint, state);
			case SEGMENT_POINT:
				return program(Driver.SEGMENT, segment(state, support), domain,
						dependentPoint, state);
			default:
				return Optional.empty();
			}
		} catch (RuntimeException exception) {
			// Refusal, malformed programs and unexpected shapes all mean no program.
			return Optional.empty();
		}
	}

	private static Optional<CertifiedConstructionProgram2D> program(Driver driver,
			double[] constants, LocusInterval2D domain, GeoElement dependentPoint,
			GeoElement state) {
		CertifiedConstructionCapture2D walk = new CertifiedConstructionCapture2D(state);
		int output = walk.node(dependentPoint);
		if (output == 0 || output != walk.steps.size()) {
			return Optional.empty();
		}
		return Optional.of(new CertifiedConstructionProgram2D(driver, constants,
				domain, walk.steps));
	}

	private static double[] circle(GeoElement state, GeoElement support,
			boolean arc) {
		GeoConic conic = (GeoConic) support;
		boolean part = conic instanceof GeoConicPart;
		if (part != arc || conic.getType() != GeoConicNDConstants.CONIC_CIRCLE
				|| !conic.isDefined()) {
			throw new Refusal();
		}
		PathParameter parameter = requirePathState(state, support);
		int pathType = parameter.getPathType();
		if (arc ? pathType != conic.getType()
				: pathType != conic.getType()
						&& pathType != GeoConicNDConstants.CONIC_EMPTY
						&& pathType != GeoConicNDConstants.CONIC_SINGLE_POINT) {
			throw new Refusal();
		}
		double[] half = conic.getHalfAxes();
		double[] midpoint = {conic.getMidpoint().getX(), conic.getMidpoint().getY()};
		if (!(half[0] > 0) || !(half[1] > 0)
				|| Double.compare(midpoint[0], conic.getTranslationVector().getX()) != 0
				|| Double.compare(midpoint[1], conic.getTranslationVector().getY()) != 0) {
			throw new Refusal();
		}
		double[] circle = {half[0], half[1], conic.getEigenvec(0).getX(),
				conic.getEigenvec(0).getY(), conic.getEigenvec(1).getX(),
				conic.getEigenvec(1).getY(), midpoint[0], midpoint[1]};
		if (!arc) {
			return circle;
		}
		GeoConicPart conicPart = (GeoConicPart) conic;
		if (conicPart.getConicPartType() != GeoConicNDConstants.CONIC_PART_ARC) {
			throw new Refusal();
		}
		double[] result = java.util.Arrays.copyOf(circle, 11);
		result[8] = conicPart.getParameterStart();
		result[9] = conicPart.getParameterExtent();
		result[10] = conicPart.positiveOrientation() ? 1 : 0;
		return result;
	}

	private static double[] segment(GeoElement state, GeoElement support) {
		GeoSegment segment = (GeoSegment) support;
		requirePathState(state, support);
		GeoPoint start = (GeoPoint) segment.getStartPoint();
		if (!(segment.getLength() > 0) || !segment.isDefined() || !start.isDefined()
				|| start.isInfinite()) {
			throw new Refusal();
		}
		return new double[] {start.getInhomX(), start.getInhomY(), segment.getX(),
				segment.getY()};
	}

	private static PathParameter requirePathState(GeoElement state,
			GeoElement support) {
		if (!(state instanceof GeoPoint) || state.getClass() != GeoPoint.class) {
			throw new Refusal();
		}
		GeoPoint point = (GeoPoint) state;
		if (!point.isPointOnPath() || point.getPath() != support
				|| !point.getKernel().usePathAndRegionParameters(point)) {
			throw new Refusal();
		}
		return point.getPathParameter();
	}

	private int node(GeoElement geo) {
		Integer known = nodes.get(geo);
		if (known != null) {
			return known;
		}
		if (!geo.isChildOf(state)) {
			throw new Refusal();
		}
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent == null || parent.getOutputLength() != 1
				|| parent.getOutput(0) != geo || geo.isGeoElement3D()) {
			throw new Refusal();
		}
		Step step = step(parent);
		if (steps.size() >= CertifiedConstructionProgram2D.MAXIMUM_STEPS) {
			throw new Refusal();
		}
		steps.add(step);
		nodes.put(geo, steps.size());
		return steps.size();
	}

	private Step step(AlgoElement parent) {
		Class<?> type = parent.getClass();
		if (type == AlgoJoinPoints.class) {
			AlgoJoinPoints join = (AlgoJoinPoints) parent;
			return new Step(Operation.JOIN, List.of(point(join.getP()),
					point(join.getQ())));
		}
		if (type == AlgoLinePointLine.class) {
			AlgoLinePointLine parallel = (AlgoLinePointLine) parent;
			return new Step(Operation.PARALLEL, List.of(point(parallel.getP()),
					line(parallel.getInput(1).toGeoElement(), false)));
		}
		if (type == AlgoOrthoLinePointLine.class) {
			AlgoOrthoLinePointLine orthogonal = (AlgoOrthoLinePointLine) parent;
			return new Step(Operation.PERPENDICULAR, List.of(point(orthogonal.getP()),
					line(orthogonal.getInput(1).toGeoElement(), false)));
		}
		if (type == AlgoIntersectLines.class) {
			AlgoIntersectLines meet = (AlgoIntersectLines) parent;
			return new Step(Operation.MEET, List.of(line(meet.getg(), true),
					line(meet.geth(), true)));
		}
		if (type == AlgoMidpoint.class) {
			AlgoMidpoint midpoint = (AlgoMidpoint) parent;
			return new Step(Operation.MIDPOINT, List.of(point(midpoint.getP()),
					point(midpoint.getQ())));
		}
		return transform(parent, type);
	}

	private Step transform(AlgoElement parent, Class<?> type) {
		List<Operand> source = List.of(pointNode(parent.getInput(0).toGeoElement()));
		if (type == AlgoTranslate.class && parent.getInputLength() == 2) {
			GeoVec3D vector = (GeoVec3D) constant(parent.getInput(1).toGeoElement());
			return new Step(Operation.TRANSLATE, source, vector.getX(), vector.getY());
		}
		if (type == AlgoRotate.class && parent.getInputLength() == 2) {
			double angle = number(parent.getInput(1).toGeoElement());
			return new Step(Operation.ROTATE, source, MyMath.cos(angle), Math.sin(angle));
		}
		if (type == AlgoRotatePoint.class && parent.getInputLength() == 3) {
			double angle = number(parent.getInput(1).toGeoElement());
			GeoPoint center = finitePoint(parent.getInput(2).toGeoElement());
			return new Step(Operation.ROTATE_ABOUT, source, MyMath.cos(angle),
					Math.sin(angle), center.getInhomX(), center.getInhomY());
		}
		if (type == AlgoMirror.class && parent.getInputLength() == 2) {
			GeoElement mirror = constant(parent.getInput(1).toGeoElement());
			if (mirror instanceof GeoPoint) {
				GeoPoint center = finitePoint(mirror);
				return new Step(Operation.MIRROR_POINT, source, center.getInhomX(),
						center.getInhomY());
			}
			GeoLine axis = definedLine(mirror);
			double angle = 2.0 * Math.atan2(-axis.getX(), axis.getY());
			return new Step(Operation.MIRROR_LINE, source, axis.getX(), axis.getY(),
					axis.getZ(), Math.abs(axis.getX()) > Math.abs(axis.getY()) ? 1 : 0,
					Math.cos(angle), Math.sin(angle));
		}
		if (type == AlgoDilate.class && (parent.getInputLength() == 2
				|| parent.getInputLength() == 3)) {
			double factor = number(parent.getInput(1).toGeoElement());
			GeoPoint center = parent.getInputLength() == 3
					? finitePoint(parent.getInput(2).toGeoElement()) : null;
			return new Step(Operation.DILATE, source, factor,
					center == null ? 0 : center.getInhomX(),
					center == null ? 0 : center.getInhomY());
		}
		throw new Refusal();
	}

	private Operand point(GeoPointND point) {
		GeoElement geo = point.toGeoElement();
		if (geo == state || geo.isChildOf(state)) {
			return pointNode(geo);
		}
		GeoPoint constant = finitePoint(geo);
		return Operand.point(constant.getX(), constant.getY(), constant.getZ(),
				constant.getInhomX(), constant.getInhomY());
	}

	private Operand pointNode(GeoElement geo) {
		if (!(geo instanceof GeoPoint) || geo.isGeoElement3D()) {
			throw new Refusal();
		}
		return Operand.node(node(geo));
	}

	private Operand line(GeoElement geo, boolean unbounded) {
		if (!(geo instanceof GeoLine) || geo.isGeoElement3D()) {
			throw new Refusal();
		}
		if (geo.isChildOf(state)) {
			if (geo.getClass() != GeoLine.class) {
				throw new Refusal();
			}
			return Operand.node(node(geo));
		}
		if (unbounded && geo.getClass() != GeoLine.class && !(geo instanceof GeoAxis)) {
			throw new Refusal();
		}
		GeoLine constant = definedLine(constant(geo));
		return Operand.line(constant.getX(), constant.getY(), constant.getZ());
	}

	private GeoElement constant(GeoElement geo) {
		if (geo == null || geo == state || geo.isChildOf(state) || !geo.isDefined()) {
			throw new Refusal();
		}
		return geo;
	}

	private GeoPoint finitePoint(GeoElement geo) {
		GeoElement constant = constant(geo);
		if (constant.getClass() != GeoPoint.class
				|| ((GeoPoint) constant).isInfinite()) {
			throw new Refusal();
		}
		return (GeoPoint) constant;
	}

	private GeoLine definedLine(GeoElement geo) {
		if (!(geo instanceof GeoLine) || geo.isGeoElement3D() || !geo.isDefined()) {
			throw new Refusal();
		}
		return (GeoLine) geo;
	}

	private double number(GeoElement geo) {
		GeoElement constant = constant(geo);
		if (!(constant instanceof NumberValue)) {
			throw new Refusal();
		}
		double value = ((NumberValue) constant).getDouble();
		if (!Double.isFinite(value)) {
			throw new Refusal();
		}
		return value;
	}

	/** Internal refusal of a shape outside class v1; never escapes capture. */
	private static final class Refusal extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private Refusal() {
			super(null, null, false, false);
		}
	}
}
