/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoLocusMetricV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.PathParameter;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoCirclePointRadius;
import org.geogebra.common.kernel.algos.AlgoCircleTwoPoints;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geogebra.common.kernel.algos.AlgoDilate;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersect;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
import org.geogebra.common.kernel.algos.AlgoJoinPoints;
import org.geogebra.common.kernel.algos.AlgoJoinPointsRay;
import org.geogebra.common.kernel.algos.AlgoJoinPointsSegment;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.algos.AlgoMidpoint;
import org.geogebra.common.kernel.algos.AlgoMirror;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoRotate;
import org.geogebra.common.kernel.algos.AlgoRotatePoint;
import org.geogebra.common.kernel.algos.AlgoTranslate;
import org.geogebra.common.kernel.algos.AlgoUnitVectorLine;
import org.geogebra.common.kernel.algos.AlgoUnitVectorVector;
import org.geogebra.common.kernel.algos.AlgoVector;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.MySpecialDouble;
import org.geogebra.common.kernel.arithmetic.MyVecNode;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.arithmetic.VectorValue;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoAxis;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.GeoVec2D;
import org.geogebra.common.kernel.geos.GeoVec3D;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.kernel.kernelND.GeoConicNDConstants;
import org.geogebra.common.util.MyMath;

/**
 * Captures the construction program of one reconstructible evaluator slice:
 * class v1, plus the class-v2 expression-point translation, and, on request, the
 * class-v3 operations ({@code locus-v2-certified-construction-model.md} section 10).
 * It reads only the evaluator's isolated objects, never live construction state, and
 * refuses every shape outside the requested class.
 */
final class CertifiedConstructionCapture2D {
	/** Refusal of a program that exceeds its versioned step limit. */
	static final String STEP_LIMIT = "step-limit-exceeded";
	/** Refusal of a shape outside the requested class. */
	static final String OUTSIDE_CLASS = "outside-class";
	/**
	 * Refusal of a driver-independent value that a metric query computed: class v3 admits
	 * a metric only through its structural certificate, never through its value
	 * ({@code locus-v2-certified-construction-model.md} section 10.1; {@code
	 * locus-v2-metrics.md} section 26.3).
	 */
	static final String METRIC_VALUE_CONSTANT = "metric-value-constant";

	private final GeoElement state;
	private final boolean classV3;
	private final Scope top = new Scope(null, null);
	private final List<Step> steps = new ArrayList<>();
	private final Map<Macro, AlgoMacro> computedInstances = new IdentityHashMap<>();
	/** Rich length of each structural metric input whose structure applies to the driver. */
	private final Map<Step, AlgoLocusBetweenMetricV2> metricSources = new IdentityHashMap<>();
	/** Captured geos whose steps are being built, innermost first, and their scopes. */
	private final Deque<GeoElement> anchorGeos = new ArrayDeque<>();
	private final Deque<Scope> anchorScopes = new ArrayDeque<>();
	/** Anchor of the next step instead of the captured geo: the rich length of an input. */
	private GeoElement pendingAnchor;
	/** Structural origin of each step, in program order. */
	private final List<StepOrigin> origins = new ArrayList<>();

	private CertifiedConstructionCapture2D(GeoElement state, boolean classV3) {
		this.state = state;
		this.classV3 = classV3;
		top.nodes.put(state, 0);
	}

	/**
	 * @return program of classes v1 and v2 of the isolated slice, or empty when the
	 *         slice is outside those classes
	 */
	static Optional<CertifiedConstructionProgram2D> capture(
			SemanticGeneratorDescriptor1D descriptor, GeoElement dependentPoint,
			GeoElement state, GeoElement support, double[][] affine) {
		return capture(descriptor, dependentPoint, state, support, affine, false);
	}

	/**
	 * @param classV3 whether the class-v3 operations are admitted; a program that needs
	 *        none of them is the program of classes v1 and v2, with the same signature
	 * @return program of the isolated slice, or empty when the slice is outside the
	 *         class
	 */
	static Optional<CertifiedConstructionProgram2D> capture(
			SemanticGeneratorDescriptor1D descriptor, GeoElement dependentPoint,
			GeoElement state, GeoElement support, double[][] affine, boolean classV3) {
		return captureWithReason(descriptor, dependentPoint, state, support, affine,
				classV3).getProgram();
	}

	/**
	 * @param classV3 whether the class-v3 operations are admitted
	 * @return program of the isolated slice, or the typed reason of its refusal:
	 *         {@value #STEP_LIMIT} when the program exceeds its versioned step limit,
	 *         {@value #OUTSIDE_CLASS} for a shape outside the class, or an unexpected shape
	 */
	static Outcome captureWithReason(SemanticGeneratorDescriptor1D descriptor,
			GeoElement dependentPoint, GeoElement state, GeoElement support,
			double[][] affine, boolean classV3) {
		try {
			LocusInterval2D domain = descriptor.getDeclaredDomain();
			switch (descriptor.getFamily()) {
			case SCALAR_STATE:
				if (affine == null || descriptor.isPeriodic()) {
					return new Outcome(Optional.empty(), OUTSIDE_CLASS, Map.of());
				}
				return new Outcome(Optional.of(new CertifiedConstructionProgram2D(
						Driver.SCALAR_AFFINE, new double[] {affine[0][0], affine[0][1],
								affine[1][0], affine[1][1]}, domain, List.of())), null,
						Map.of());
			case CIRCLE_POINT:
				return program(Driver.CIRCLE, circle(state, support, false), domain,
						dependentPoint, state, classV3);
			case CIRCULAR_ARC_POINT:
				return program(Driver.CIRCULAR_ARC, circle(state, support, true), domain,
						dependentPoint, state, classV3);
			case SEGMENT_POINT:
				return program(Driver.SEGMENT, segment(state, support), domain,
						dependentPoint, state, classV3);
			default:
				return new Outcome(Optional.empty(), OUTSIDE_CLASS, Map.of());
			}
		} catch (Refusal refusal) {
			return new Outcome(Optional.empty(), refusal.reason, Map.of());
		} catch (RuntimeException exception) {
			// Malformed programs and unexpected shapes also mean no program.
			return new Outcome(Optional.empty(),
					"unexpected-shape:" + exception.getClass().getSimpleName(), Map.of());
		}
	}

	private static Outcome program(Driver driver, double[] constants,
			LocusInterval2D domain, GeoElement dependentPoint, GeoElement state,
			boolean classV3) {
		CertifiedConstructionCapture2D walk =
				new CertifiedConstructionCapture2D(state, classV3);
		int output = walk.node(dependentPoint, walk.top);
		if (output == 0 || output != walk.steps.size()) {
			return new Outcome(Optional.empty(), OUTSIDE_CLASS, Map.of());
		}
		Map<Integer, AlgoLocusBetweenMetricV2> metrics = new LinkedHashMap<>();
		for (int index = 0; index < walk.steps.size(); index++) {
			AlgoLocusBetweenMetricV2 rich = walk.metricSources.get(walk.steps.get(index));
			if (rich != null) {
				metrics.put(index + 1, rich);
			}
		}
		return new Outcome(Optional.of(new CertifiedConstructionProgram2D(driver,
				constants, domain, walk.steps)), null, metrics, walk.origins);
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

	private int node(GeoElement geo, Scope scope) {
		Integer known = scope.nodes.get(geo);
		if (known != null) {
			return known;
		}
		GeoElement actual = scope.actualInput(geo);
		if (actual != null) {
			return node(actual, scope.parent);
		}
		if (!scope.dependent(geo)) {
			throw new Refusal();
		}
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent == null || geo.isGeoElement3D()) {
			throw new Refusal();
		}
		int index;
		anchorGeos.push(geo);
		anchorScopes.push(scope);
		try {
			if (classV3 && parent.getClass() == AlgoMacro.class) {
				index = expand((AlgoMacro) parent, geo, scope);
			} else if (classV3 && parent.getClass() == AlgoIntersectLineConic.class) {
				int pair = intersection((AlgoIntersectLineConic) parent, scope);
				index = add(new Step(Operation.INTERSECTION_OUTPUT,
						List.of(Operand.node(pair)), outputIndex(parent, geo)));
			} else {
				if (parent.getOutputLength() != 1 || parent.getOutput(0) != geo) {
					throw new Refusal();
				}
				index = add(step(parent, scope));
			}
		} finally {
			anchorGeos.pop();
			anchorScopes.pop();
		}
		scope.nodes.put(geo, index);
		return index;
	}

	private int add(Step step) {
		if (steps.size() >= (classV3 ? CertifiedConstructionProgram2D.MAXIMUM_STEPS_V3
				: CertifiedConstructionProgram2D.MAXIMUM_STEPS)) {
			throw new Refusal(STEP_LIMIT);
		}
		steps.add(step);
		origins.add(origin(step));
		return steps.size();
	}

	/**
	 * Structural origin of a new step ({@code locus-v2-existence-components.md} section
	 * 15.6): the captured geo of the slice whose definition produced it, followed inside an
	 * expanded macro by the construction index of each internal geo, and its operation. A
	 * structural metric input is anchored at its rich length.
	 */
	private StepOrigin origin(Step step) {
		GeoElement anchor = pendingAnchor;
		pendingAnchor = null;
		List<String> inner = new ArrayList<>();
		if (anchor == null) {
			Iterator<GeoElement> geos = anchorGeos.iterator();
			Iterator<Scope> scopes = anchorScopes.iterator();
			while (geos.hasNext() && anchor == null) {
				GeoElement geo = geos.next();
				if (scopes.next() == top) {
					anchor = geo;
				} else {
					inner.add("-m" + geo.getConstructionIndex());
				}
			}
		}
		StringBuilder path = new StringBuilder();
		for (int index = inner.size() - 1; index >= 0; index--) {
			path.append(inner.get(index));
		}
		return new StepOrigin(anchor, path.toString(), step.getOperation().name());
	}

	private Step step(AlgoElement parent, Scope scope) {
		Class<?> type = parent.getClass();
		if (type == AlgoJoinPoints.class) {
			AlgoJoinPoints join = (AlgoJoinPoints) parent;
			return new Step(Operation.JOIN, List.of(point(join.getP(), scope),
					point(join.getQ(), scope)));
		}
		if (type == AlgoLinePointLine.class) {
			AlgoLinePointLine parallel = (AlgoLinePointLine) parent;
			return new Step(Operation.PARALLEL, List.of(point(parallel.getP(), scope),
					line(parallel.getInput(1).toGeoElement(), false, scope)));
		}
		if (type == AlgoOrthoLinePointLine.class) {
			AlgoOrthoLinePointLine orthogonal = (AlgoOrthoLinePointLine) parent;
			return new Step(Operation.PERPENDICULAR, List.of(point(orthogonal.getP(),
					scope), line(orthogonal.getInput(1).toGeoElement(), false, scope)));
		}
		if (type == AlgoIntersectLines.class) {
			AlgoIntersectLines meet = (AlgoIntersectLines) parent;
			return new Step(Operation.MEET, List.of(line(meet.getg(), true, scope),
					line(meet.geth(), true, scope)));
		}
		if (type == AlgoMidpoint.class) {
			AlgoMidpoint midpoint = (AlgoMidpoint) parent;
			return new Step(Operation.MIDPOINT, List.of(point(midpoint.getP(), scope),
					point(midpoint.getQ(), scope)));
		}
		if (type == AlgoDependentPoint.class) {
			return expressionPoint((AlgoDependentPoint) parent, scope);
		}
		if (classV3) {
			Step step = classV3Step(parent, type, scope);
			if (step != null) {
				return step;
			}
		}
		return transform(parent, type, scope);
	}

	/** @return class-v3 step of the algorithm, or {@code null} for other algorithms */
	private Step classV3Step(AlgoElement parent, Class<?> type, Scope scope) {
		if (type == AlgoJoinPointsRay.class) {
			AlgoJoinPointsRay ray = (AlgoJoinPointsRay) parent;
			requireBoundedIncidence(ray.getRay());
			return new Step(Operation.RAY, List.of(point(ray.getP(), scope),
					point(ray.getQ(), scope)));
		}
		if (type == AlgoJoinPointsSegment.class) {
			AlgoJoinPointsSegment segment = (AlgoJoinPointsSegment) parent;
			GeoSegment output = segment.getSegment();
			if (output.getClass() != GeoSegment.class) {
				throw new Refusal();
			}
			requireBoundedIncidence(output);
			return new Step(Operation.SEGMENT, List.of(point(segment.getP(), scope),
					point(segment.getQ(), scope)));
		}
		if (type == AlgoCirclePointRadius.class) {
			AlgoCirclePointRadius circle = (AlgoCirclePointRadius) parent;
			GeoElement radius = circle.getRadiusGeo();
			requireCircleOutput(parent);
			if (radius instanceof GeoSegment) {
				throw new Refusal();
			}
			double value = number(radius, scope);
			// GeoQuadricND.setSphereNDMatrix: a circle needs r >= STANDARD_PRECISION.
			if (!(value >= Kernel.STANDARD_PRECISION)) {
				throw new Refusal();
			}
			return new Step(Operation.CIRCLE_RADIUS,
					List.of(point(circle.getCenter().toGeoElement(), scope)), value);
		}
		if (type == AlgoCircleTwoPoints.class) {
			AlgoCircleTwoPoints circle = (AlgoCircleTwoPoints) parent;
			requireCircleOutput(parent);
			return new Step(Operation.CIRCLE_THROUGH,
					List.of(point(circle.getCenter().toGeoElement(), scope),
							point(circle.getExternalPoint().toGeoElement(), scope)));
		}
		if (type == AlgoIntersectSingle.class) {
			return singleIntersection((AlgoIntersectSingle) parent, scope);
		}
		if (type == AlgoUnitVectorLine.class) {
			return new Step(Operation.UNIT_VECTOR_LINE,
					List.of(line(parent.getInput(0).toGeoElement(), false, scope)),
					normalizeFlag(parent));
		}
		if (type == AlgoUnitVectorVector.class) {
			return new Step(Operation.UNIT_VECTOR,
					List.of(vector(parent.getInput(0).toGeoElement(), scope)),
					normalizeFlag(parent));
		}
		if (type == AlgoVector.class) {
			AlgoVector vector = (AlgoVector) parent;
			if (vector.getVector().getClass() != GeoVector.class) {
				throw new Refusal();
			}
			return new Step(Operation.VECTOR_POINTS,
					List.of(point(vector.getP().toGeoElement(), scope),
							point(vector.getQ().toGeoElement(), scope)));
		}
		if (type == AlgoDependentNumber.class) {
			return dotProduct((AlgoDependentNumber) parent, scope);
		}
		if (type == AlgoLocusMetricScalarAdapter.class && scope == top) {
			return metricInput((AlgoLocusMetricScalarAdapter) parent);
		}
		return null;
	}

	private static double normalizeFlag(AlgoElement unitVector) {
		return unitVector.getClassName() == Commands.UnitVector ? 1 : 0;
	}

	private static void requireCircleOutput(AlgoElement parent) {
		GeoElement output = parent.getOutput(0);
		if (output.getClass() != GeoConic.class || output.isGeoElement3D()) {
			throw new Refusal();
		}
	}

	/** A ray or segment whose intersections are bounded by its own parameter range. */
	private static void requireBoundedIncidence(GeoElement bounded) {
		boolean outlying = bounded instanceof GeoRay
				? ((GeoRay) bounded).allowOutlyingIntersections()
				: ((GeoSegment) bounded).allowOutlyingIntersections();
		if (outlying || bounded.isGeoElement3D()) {
			throw new Refusal();
		}
	}

	/**
	 * Contract section 10.2: both raw roots of a line and a complete circle, with the
	 * selection state that the algorithm itself holds. The state is read, never written.
	 *
	 * @return intersection node
	 */
	private int intersection(AlgoIntersectLineConic algorithm, Scope scope) {
		Integer known = scope.intersections.get(algorithm);
		if (known != null) {
			return known;
		}
		// A macro construction is shared by all its instances, so a selection state
		// inside it does not belong to one instance.
		if (scope != top || algorithm.getIntersectionPoints().length != 2
				|| algorithm.existingIntersections() != 0
				|| algorithm.getKernel().isContinuous()) {
			throw new Refusal();
		}
		double mode;
		double first;
		double second;
		if (algorithm.isSelectionFirstIntersectionPending()) {
			// Compaction of the roots on the paths, in raw order (firstIntersection).
			mode = 1;
			first = 0;
			second = 1;
		} else if (!algorithm.isSelectionPermutationPending()) {
			int[] permutation = algorithm.getSelectionPermutation();
			if (permutation == null || permutation.length != 2) {
				throw new Refusal();
			}
			mode = 0;
			first = permutation[0];
			second = permutation[1];
		} else {
			// The next computation sets the permutation by proximity: not established.
			throw new Refusal();
		}
		Operand line = line(algorithm.getLine(), true, scope);
		Operand circle = circleOperand(algorithm.getConic(), scope);
		int index = add(new Step(Operation.LINE_CIRCLE, List.of(line, circle), mode,
				first, second, algorithm.getOutput(0).isLabelSet() ? 1 : 0,
				algorithm.getOutput(1).isLabelSet() ? 1 : 0));
		scope.intersections.put(algorithm, index);
		return index;
	}

	/** An indexed single intersection selects one output of its source intersection. */
	private Step singleIntersection(AlgoIntersectSingle single, Scope scope) {
		AlgoIntersect source = single.getSourceIntersection();
		GeoElement index = single.getInput(2).toGeoElement();
		int selected = single.getSelectedIndex();
		if (source == null || source.getClass() != AlgoIntersectLineConic.class
				|| !(index instanceof GeoNumeric) || scope.dependent(index)
				|| selected < 0 || selected > 1) {
			throw new Refusal();
		}
		int pair = intersection((AlgoIntersectLineConic) source, scope);
		return new Step(Operation.INTERSECTION_OUTPUT, List.of(Operand.node(pair)),
				selected);
	}

	/**
	 * Expands one macro instance: its internal steps replace it, with the macro inputs
	 * bound to the instance's own inputs.
	 *
	 * @return node of the requested output
	 */
	private int expand(AlgoMacro instance, GeoElement output, Scope scope) {
		Macro macro = instance.getMacro();
		GeoElement[] inputs = macro.getMacroInput();
		GeoElement[] outputs = macro.getMacroOutput();
		if (inputs.length != instance.getInputLength()
				|| outputs.length != instance.getOutputLength()) {
			throw new Refusal();
		}
		Scope inner = scope.children.get(instance);
		if (inner == null) {
			inner = new Scope(instance, scope);
			scope.children.put(instance, inner);
		}
		return node(outputs[outputIndex(instance, output)], inner);
	}

	private static int outputIndex(AlgoElement parent, GeoElement output) {
		for (int index = 0; index < parent.getOutputLength(); index++) {
			if (parent.getOutput(index) == output) {
				return index;
			}
		}
		throw new Refusal();
	}

	/**
	 * Contract section 5.2 first; class v3 then admits a point plus or minus a vector
	 * expression, possibly scaled, with driver-dependent operands.
	 */
	private Step expressionPoint(AlgoDependentPoint expression, Scope scope) {
		try {
			return expressionTranslation(expression, scope);
		} catch (Refusal refusal) {
			if (!classV3) {
				throw refusal;
			}
		}
		ExpressionNode node = binary(expression.getExpression());
		org.geogebra.common.plugin.Operation operation = node.getOperation();
		boolean sum = operation == org.geogebra.common.plugin.Operation.PLUS;
		if (!sum && operation != org.geogebra.common.plugin.Operation.MINUS) {
			throw new Refusal();
		}
		ExpressionValue left = unwrap(node.getLeft());
		ExpressionValue right = unwrap(node.getRight());
		boolean pointOnLeft = isPoint(left);
		if (!pointOnLeft && !(sum && isPoint(right))) {
			throw new Refusal();
		}
		Operand point = point((GeoElement) (pointOnLeft ? left : right), scope);
		Operand vector = vectorExpression(pointOnLeft ? right : left, scope);
		return new Step(Operation.POINT_PLUS_VECTOR, List.of(point, vector),
				sum ? 1 : -1);
	}

	/**
	 * Contract section 5.2: exactly one {@code P + V}, {@code V + P} or
	 * {@code P - V} of a driver-dependent point and a finite constant vector.
	 */
	private Step expressionTranslation(AlgoDependentPoint expression, Scope scope) {
		ExpressionValue definition = unwrap(expression.getExpression());
		if (!(definition instanceof ExpressionNode)) {
			throw new Refusal();
		}
		ExpressionNode node = (ExpressionNode) definition;
		org.geogebra.common.plugin.Operation operation = node.getOperation();
		boolean sum = operation == org.geogebra.common.plugin.Operation.PLUS;
		if (!sum && operation != org.geogebra.common.plugin.Operation.MINUS) {
			throw new Refusal();
		}
		ExpressionValue left = unwrap(node.getLeft());
		ExpressionValue right = unwrap(node.getRight());
		boolean pointOnLeft = isPoint(left);
		if (!pointOnLeft && !(sum && isPoint(right))) {
			throw new Refusal();
		}
		double[] vector = constantVector(pointOnLeft ? right : left, scope);
		// x - y is x + (-y) exactly, so a difference translates by the negated vector.
		double ux = sum ? vector[0] : -vector[0];
		double uy = sum ? vector[1] : -vector[1];
		return new Step(Operation.EXPRESSION_TRANSLATE,
				List.of(pointNode((GeoElement) (pointOnLeft ? left : right), scope)), ux,
				uy);
	}

	private static boolean isPoint(ExpressionValue value) {
		return value != null && value.getClass() == GeoPoint.class;
	}

	/** A driver-independent GeoVector, or an inline Cartesian literal of two numbers. */
	private double[] constantVector(ExpressionValue value, Scope scope) {
		if (value instanceof GeoElement) {
			if (value.getClass() != GeoVector.class) {
				throw new Refusal();
			}
			GeoVector vector = (GeoVector) constant((GeoElement) value, scope);
			return new double[] {vector.getX(), vector.getY()};
		}
		if (!(value instanceof MyVecNode)) {
			throw new Refusal();
		}
		MyVecNode literal = (MyVecNode) value;
		if (literal.hasPolarCoords()
				|| literal.getToStringMode() != Kernel.COORD_CARTESIAN) {
			throw new Refusal();
		}
		return new double[] {literalNumber(literal.getX()),
				literalNumber(literal.getY())};
	}

	/**
	 * Class v3: a vector geo, an inline Cartesian literal, or the product of a number
	 * and a vector with at least one driver-dependent factor.
	 */
	private Operand vectorExpression(ExpressionValue expression, Scope scope) {
		ExpressionValue value = unwrap(expression);
		if (value instanceof GeoElement) {
			return vector((GeoElement) value, scope);
		}
		if (value instanceof MyVecNode) {
			if (dependsOnDriver(value, scope)) {
				throw new Refusal();
			}
			requireNoMetricValue(value, scope);
			scope.refreshConstants();
			GeoVec2D literal = ((MyVecNode) value).getVector();
			if (((MyVecNode) value).hasPolarCoords() || !Double.isFinite(literal.getX())
					|| !Double.isFinite(literal.getY())) {
				throw new Refusal();
			}
			return Operand.vector(literal.getX(), literal.getY());
		}
		ExpressionNode product = binary(value);
		if (product.getOperation() != org.geogebra.common.plugin.Operation.MULTIPLY) {
			throw new Refusal();
		}
		ExpressionValue left = unwrap(product.getLeft());
		ExpressionValue right = unwrap(product.getRight());
		boolean scalarOnLeft = isScalar(left);
		if (scalarOnLeft == isScalar(right)) {
			throw new Refusal();
		}
		if (!dependsOnDriver(product, scope)) {
			return constantVectorValue(product, scope);
		}
		Operand scalar = scalarExpression(scalarOnLeft ? left : right, scope);
		Operand vector = vectorExpression(scalarOnLeft ? right : left, scope);
		return Operand.node(add(new Step(Operation.SCALE, List.of(scalar, vector))));
	}

	/** @return whether a geo of the expression tree depends on the driver */
	private boolean dependsOnDriver(ExpressionValue expression, Scope scope) {
		ExpressionValue value = unwrap(expression);
		if (value instanceof GeoElement) {
			return scope.dependent((GeoElement) value);
		}
		if (value instanceof ExpressionNode) {
			ExpressionNode node = (ExpressionNode) value;
			return dependsOnDriver(node.getLeft(), scope)
					|| node.getRight() != null && dependsOnDriver(node.getRight(), scope);
		}
		if (value instanceof MyVecNode) {
			MyVecNode literal = (MyVecNode) value;
			return dependsOnDriver(literal.getX(), scope)
					|| dependsOnDriver(literal.getY(), scope);
		}
		return false;
	}

	/**
	 * A maximal driver-independent subexpression is a constant: the binary64 value the
	 * evaluator itself computes for it (contract sections 2 and 3, rule 2).
	 */
	private Operand constantVectorValue(ExpressionNode expression, Scope scope) {
		requireNoMetricValue(expression, scope);
		scope.refreshConstants();
		ExpressionValue result = expression.evaluate(StringTemplate.defaultTemplate);
		if (!(result instanceof VectorValue)) {
			throw new Refusal();
		}
		GeoVec2D vector = ((VectorValue) result).getVector();
		if (!Double.isFinite(vector.getX()) || !Double.isFinite(vector.getY())) {
			throw new Refusal();
		}
		return Operand.vector(vector.getX(), vector.getY());
	}

	private static boolean isScalar(ExpressionValue value) {
		return value instanceof MyDouble
				|| value instanceof GeoElement && (value.getClass() == GeoNumeric.class
						|| value.getClass() == GeoAngle.class);
	}

	/**
	 * Class v3: a number geo, a numeric literal, or a driver-independent subexpression,
	 * captured by its value.
	 */
	private Operand scalarExpression(ExpressionValue value, Scope scope) {
		if (value instanceof GeoElement) {
			GeoElement geo = (GeoElement) value;
			if (scope.dependent(geo)) {
				return Operand.node(node(geo, scope));
			}
			return Operand.scalar(number(geo, scope));
		}
		if (value instanceof ExpressionNode && !dependsOnDriver(value, scope)) {
			requireNoMetricValue(value, scope);
			scope.refreshConstants();
			ExpressionValue result = ((ExpressionNode) value)
					.evaluate(StringTemplate.defaultTemplate);
			if (!(result instanceof NumberValue)
					|| !Double.isFinite(((NumberValue) result).getDouble())) {
				throw new Refusal();
			}
			return Operand.scalar(((NumberValue) result).getDouble());
		}
		return Operand.scalar(literalNumber(value));
	}

	/** Class v3: {@code u * v} of two vectors, their dot product. */
	private Step dotProduct(AlgoDependentNumber number, Scope scope) {
		ExpressionNode product = binary(number.getExpression());
		if (product.getOperation() != org.geogebra.common.plugin.Operation.MULTIPLY) {
			throw new Refusal();
		}
		ExpressionValue left = unwrap(product.getLeft());
		ExpressionValue right = unwrap(product.getRight());
		if (isScalar(left) || isScalar(right)) {
			throw new Refusal();
		}
		return new Step(Operation.DOT, List.of(vectorExpression(left, scope),
				vectorExpression(right, scope)));
	}

	/**
	 * Class v3 (contract section 10.1): the length between two positions of a locus
	 * contributes its structural certificate and never its value. Only the
	 * driver-dependent endpoints are operands; the source locus does not depend on the
	 * driver position. The structure of the length applies to the driver of the program
	 * when its origin is fixed and its target is the generator occurrence of a source
	 * locus driven by the same driver point ({@code locus-v2-metrics.md} section 26.1).
	 */
	private Step metricInput(AlgoLocusMetricScalarAdapter adapter) {
		AlgoElement rich = adapter.getRichInput().getParentAlgorithm();
		if (rich == null || rich.getClass() != AlgoLocusBetweenMetricV2.class) {
			throw new Refusal();
		}
		List<Operand> endpoints = new ArrayList<>();
		StringBuilder descriptor = new StringBuilder("LocusLength");
		boolean[] dependent = new boolean[3];
		for (int index = 1; index <= 2; index++) {
			GeoElement endpoint = rich.getInput(index).toGeoElement();
			dependent[index] = top.dependent(endpoint);
			descriptor.append(index == 1 ? "|start=" : "|target=")
					.append(dependent[index] ? "node" : "constant");
			if (dependent[index]) {
				endpoints.add(pointNode(endpoint, top));
			}
		}
		for (int index = 3; index < rich.getInputLength(); index++) {
			GeoElement token = rich.getInput(index).toGeoElement();
			if (!(token instanceof GeoText) || top.dependent(token)) {
				throw new Refusal();
			}
			descriptor.append("|policy=").append(((GeoText) token).getTextString());
		}
		Step step = new Step(Operation.METRIC_INPUT, endpoints, descriptor.toString());
		// The events of the length belong to the rich length, not to its scalar view.
		pendingAnchor = rich.getOutput(0).toGeoElement();
		AlgoElement source = rich.getInput(0).toGeoElement().getParentAlgorithm();
		if (!dependent[1] && dependent[2] && source instanceof AlgoDependentPointLocusV2
				&& source.getInput(0).toGeoElement() == rich.getInput(2).toGeoElement()
				&& source.getInput(1).toGeoElement() == state) {
			metricSources.put(step, (AlgoLocusBetweenMetricV2) rich);
		}
		return step;
	}

	/**
	 * Refuses a constant whose value a metric query computed, inside a macro through the
	 * actual inputs of its instance.
	 */
	private void requireNoMetricValue(GeoElement geo, Scope scope) {
		if (scope.metricDerived(geo)) {
			throw new Refusal(METRIC_VALUE_CONSTANT);
		}
	}

	/** Refuses an expression that reads a value a metric query computed. */
	private void requireNoMetricValue(ExpressionValue expression, Scope scope) {
		ExpressionValue value = unwrap(expression);
		if (value instanceof GeoElement) {
			requireNoMetricValue((GeoElement) value, scope);
		} else if (value instanceof ExpressionNode) {
			ExpressionNode node = (ExpressionNode) value;
			requireNoMetricValue(node.getLeft(), scope);
			if (node.getRight() != null) {
				requireNoMetricValue(node.getRight(), scope);
			}
		} else if (value instanceof MyVecNode) {
			requireNoMetricValue(((MyVecNode) value).getX(), scope);
			requireNoMetricValue(((MyVecNode) value).getY(), scope);
		}
	}

	private static boolean isMetricAlgorithm(AlgoElement algorithm) {
		return algorithm instanceof AlgoLocusMetricV2
				|| algorithm instanceof AlgoLocusBetweenMetricV2
				|| algorithm instanceof AlgoLocusMetricScalarAdapter;
	}

	private static double literalNumber(ExpressionValue component) {
		ExpressionValue value = unwrap(component);
		if (value == null || value.getClass() != MyDouble.class
				&& value.getClass() != MySpecialDouble.class) {
			throw new Refusal();
		}
		return value.evaluateDouble();
	}

	private static ExpressionNode binary(ExpressionValue expression) {
		ExpressionValue value = unwrap(expression);
		if (!(value instanceof ExpressionNode)) {
			throw new Refusal();
		}
		return (ExpressionNode) value;
	}

	/** Removes the wrappers whose evaluation returns their left value unchanged. */
	private static ExpressionValue unwrap(ExpressionValue value) {
		ExpressionValue current = value;
		while (current instanceof ExpressionNode) {
			ExpressionNode node = (ExpressionNode) current;
			if (!node.isLeaf() && node.getOperation()
					!= org.geogebra.common.plugin.Operation.NO_OPERATION) {
				return node;
			}
			current = node.getLeft();
		}
		return current;
	}

	private Step transform(AlgoElement parent, Class<?> type, Scope scope) {
		List<Operand> source = List.of(pointNode(parent.getInput(0).toGeoElement(),
				scope));
		if (type == AlgoTranslate.class && parent.getInputLength() == 2) {
			GeoVec3D vector = (GeoVec3D) constant(parent.getInput(1).toGeoElement(),
					scope);
			return new Step(Operation.TRANSLATE, source, vector.getX(), vector.getY());
		}
		if (type == AlgoRotate.class && parent.getInputLength() == 2) {
			double angle = number(parent.getInput(1).toGeoElement(), scope);
			return new Step(Operation.ROTATE, source, MyMath.cos(angle), Math.sin(angle));
		}
		if (type == AlgoRotatePoint.class && parent.getInputLength() == 3) {
			double angle = number(parent.getInput(1).toGeoElement(), scope);
			GeoPoint center = finitePoint(parent.getInput(2).toGeoElement(), scope);
			return new Step(Operation.ROTATE_ABOUT, source, MyMath.cos(angle),
					Math.sin(angle), center.getInhomX(), center.getInhomY());
		}
		if (type == AlgoMirror.class && parent.getInputLength() == 2) {
			GeoElement mirror = constant(parent.getInput(1).toGeoElement(), scope);
			if (mirror instanceof GeoPoint) {
				GeoPoint center = finitePoint(mirror, scope);
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
			double factor = number(parent.getInput(1).toGeoElement(), scope);
			GeoPoint center = parent.getInputLength() == 3
					? finitePoint(parent.getInput(2).toGeoElement(), scope) : null;
			return new Step(Operation.DILATE, source, factor,
					center == null ? 0 : center.getInhomX(),
					center == null ? 0 : center.getInhomY());
		}
		throw new Refusal();
	}

	private Operand point(GeoElement geo, Scope scope) {
		if (scope.dependent(geo)) {
			return pointNode(geo, scope);
		}
		GeoPoint constant = finitePoint(geo, scope);
		return Operand.point(constant.getX(), constant.getY(), constant.getZ(),
				constant.getInhomX(), constant.getInhomY());
	}

	private Operand pointNode(GeoElement geo, Scope scope) {
		GeoElement resolved = scope.resolve(geo);
		if (!(resolved instanceof GeoPoint) || resolved.isGeoElement3D()) {
			throw new Refusal();
		}
		return Operand.node(node(geo, scope));
	}

	/**
	 * @param meet whether the line is an operand of an intersection, whose bound decides
	 *        incidence
	 */
	private Operand line(GeoElement geo, boolean meet, Scope scope) {
		GeoElement resolved = scope.resolve(geo);
		if (!(resolved instanceof GeoLine) || resolved.isGeoElement3D()) {
			throw new Refusal();
		}
		Class<?> type = resolved.getClass();
		if (scope.dependent(geo)) {
			if (type != GeoLine.class && !(classV3
					&& (type == GeoRay.class || type == GeoSegment.class))) {
				throw new Refusal();
			}
			return Operand.node(node(geo, scope));
		}
		GeoLine constant = definedLine(constant(geo, scope));
		if (meet && classV3 && (type == GeoRay.class || type == GeoSegment.class)) {
			requireBoundedIncidence(constant);
			GeoPoint start = (GeoPoint) constant.getStartPoint();
			if (start == null || !start.isDefined() || start.isInfinite()) {
				throw new Refusal();
			}
			return Operand.bounded(type == GeoRay.class, constant.getX(),
					constant.getY(), constant.getZ(), start.getInhomX(), start.getInhomY());
		}
		if (meet && type != GeoLine.class && !(constant instanceof GeoAxis)) {
			throw new Refusal();
		}
		return Operand.line(constant.getX(), constant.getY(), constant.getZ());
	}

	/** Class v3: a circle node, or a constant complete circle of circle matrix form. */
	private Operand circleOperand(GeoElement geo, Scope scope) {
		if (scope.dependent(geo)) {
			return Operand.node(node(geo, scope));
		}
		GeoElement constant = constant(geo, scope);
		if (constant.getClass() != GeoConic.class
				|| ((GeoConic) constant).getType() != GeoConicNDConstants.CONIC_CIRCLE) {
			throw new Refusal();
		}
		double[] matrix = ((GeoConic) constant).getFlatMatrix();
		if (matrix[0] != 1 || matrix[1] != 1 || matrix[3] != 0) {
			throw new Refusal();
		}
		return Operand.circle(matrix[2], matrix[4], matrix[5]);
	}

	/** Class v3: a vector node or a constant vector. */
	private Operand vector(GeoElement geo, Scope scope) {
		GeoElement resolved = scope.resolve(geo);
		if (resolved.getClass() != GeoVector.class) {
			throw new Refusal();
		}
		if (scope.dependent(geo)) {
			return Operand.node(node(geo, scope));
		}
		GeoVector constant = (GeoVector) constant(geo, scope);
		return Operand.vector(constant.getX(), constant.getY());
	}

	private GeoElement constant(GeoElement geo, Scope scope) {
		if (geo == null || scope.dependent(geo)) {
			throw new Refusal();
		}
		if (classV3) {
			requireNoMetricValue(geo, scope);
		}
		GeoElement value = scope.constantValue(geo);
		if (value == state || !value.isDefined()) {
			throw new Refusal();
		}
		return value;
	}

	private GeoPoint finitePoint(GeoElement geo, Scope scope) {
		GeoElement constant = constant(geo, scope);
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

	private double number(GeoElement geo, Scope scope) {
		GeoElement constant = constant(geo, scope);
		if (!(constant instanceof NumberValue)) {
			throw new Refusal();
		}
		double value = ((NumberValue) constant).getDouble();
		if (!Double.isFinite(value)) {
			throw new Refusal();
		}
		return value;
	}

	/**
	 * Binding of geos to the walk: the slice itself, or one expanded macro instance whose
	 * macro inputs stand for the instance's own inputs.
	 */
	private final class Scope {
		private final AlgoMacro instance;
		private final Scope parent;
		private final Map<GeoElement, Integer> nodes = new IdentityHashMap<>();
		private final Map<AlgoElement, Integer> intersections = new IdentityHashMap<>();
		private final Map<GeoElement, Boolean> dependence = new IdentityHashMap<>();
		private final Map<GeoElement, Boolean> metric = new IdentityHashMap<>();
		private final Map<AlgoMacro, Scope> children = new IdentityHashMap<>();

		private Scope(AlgoMacro instance, Scope parent) {
			this.instance = instance;
			this.parent = parent;
		}

		/** @return the instance input a macro input stands for, otherwise null */
		private GeoElement actualInput(GeoElement geo) {
			if (instance == null) {
				return null;
			}
			GeoElement[] inputs = instance.getMacro().getMacroInput();
			for (int index = 0; index < inputs.length; index++) {
				if (inputs[index] == geo) {
					return instance.getInput(index).toGeoElement();
				}
			}
			return null;
		}

		/** @return the geo whose value and kind stand for the given one */
		private GeoElement resolve(GeoElement geo) {
			GeoElement actual = actualInput(geo);
			return actual == null ? geo : parent.resolve(actual);
		}

		/**
		 * Class v1 keeps its construction-graph rule. Class v3 follows values: the value
		 * of a locus output does not depend on the driver position, so elements derived
		 * from one are classified by their other inputs, as the revision fingerprint does.
		 */
		private boolean dependent(GeoElement geo) {
			GeoElement actual = actualInput(geo);
			if (actual != null) {
				return parent.dependent(actual);
			}
			if (geo == state) {
				return true;
			}
			if (!classV3) {
				return geo.isChildOf(state);
			}
			Boolean known = dependence.get(geo);
			if (known != null) {
				return known;
			}
			boolean result = false;
			AlgoElement algorithm = geo.getParentAlgorithm();
			if (algorithm != null && !ReconstructionExactState2D.isLocusOutput(geo)) {
				for (int index = 0; index < algorithm.getInputLength() && !result;
						index++) {
					result = dependent(algorithm.getInput(index).toGeoElement());
				}
			}
			dependence.put(geo, result);
			return result;
		}

		/**
		 * @return whether a metric query computed the value of the geo or of one of its
		 *         ancestors, followed through locus outputs and macro instance inputs
		 */
		private boolean metricDerived(GeoElement geo) {
			GeoElement actual = actualInput(geo);
			if (actual != null) {
				return parent.metricDerived(actual);
			}
			Boolean known = metric.get(geo);
			if (known != null) {
				return known;
			}
			metric.put(geo, false);
			AlgoElement algorithm = geo.getParentAlgorithm();
			boolean result = algorithm != null && isMetricAlgorithm(algorithm);
			for (int index = 0; algorithm != null && !result
					&& index < algorithm.getInputLength(); index++) {
				result = metricDerived(algorithm.getInput(index).toGeoElement());
			}
			metric.put(geo, result);
			return result;
		}

		/**
		 * @return the geo holding a constant's value; inside a macro the shared macro
		 *         construction is first set to this instance's own state
		 */
		private GeoElement constantValue(GeoElement geo) {
			GeoElement actual = actualInput(geo);
			if (actual != null) {
				return parent.constantValue(actual);
			}
			refreshConstants();
			return geo;
		}

		/** Sets the shared macro construction to this instance's own state. */
		private void refreshConstants() {
			if (instance != null
					&& computedInstances.get(instance.getMacro()) != instance) {
				instance.compute();
				computedInstances.put(instance.getMacro(), instance);
			}
		}
	}

	/** Internal refusal of a shape outside the requested class; never escapes capture. */
	private static final class Refusal extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final String reason;

		private Refusal() {
			this(OUTSIDE_CLASS);
		}

		private Refusal(String reason) {
			super(null, null, false, false);
			this.reason = reason;
		}
	}

	/**
	 * Structural origin of one step: the captured geo of the slice that anchors it, the path
	 * of construction indices inside expanded macros, and its operation.
	 */
	static final class StepOrigin {
		private final GeoElement anchor;
		private final String macroPath;
		private final String operation;

		private StepOrigin(GeoElement anchor, String macroPath, String operation) {
			this.anchor = anchor;
			this.macroPath = macroPath;
			this.operation = operation;
		}

		/** @return captured geo of the slice, or {@code null} */
		GeoElement getAnchor() {
			return anchor;
		}

		/** @return construction indices inside expanded macros, as {@code -m<k>} tokens */
		String getMacroPath() {
			return macroPath;
		}

		/** @return operation of the step */
		String getOperation() {
			return operation;
		}
	}

	/** Typed outcome of a capture: the program, or the reason of its refusal. */
	static final class Outcome {
		private final Optional<CertifiedConstructionProgram2D> program;
		private final String refusal;
		private final Map<Integer, AlgoLocusBetweenMetricV2> metricSources;
		private final List<StepOrigin> origins;

		private Outcome(Optional<CertifiedConstructionProgram2D> program, String refusal,
				Map<Integer, AlgoLocusBetweenMetricV2> metricSources) {
			this(program, refusal, metricSources, List.of());
		}

		private Outcome(Optional<CertifiedConstructionProgram2D> program, String refusal,
				Map<Integer, AlgoLocusBetweenMetricV2> metricSources,
				List<StepOrigin> origins) {
			this.program = program;
			this.refusal = refusal;
			this.metricSources = Collections.unmodifiableMap(metricSources);
			this.origins = Collections.unmodifiableList(new ArrayList<>(origins));
		}

		/**
		 * @return the rich length, in the isolated slice, of each structural metric input
		 *         node whose structural certificate applies to the driver of the program
		 */
		Map<Integer, AlgoLocusBetweenMetricV2> getMetricSources() {
			return metricSources;
		}

		/** @return structural origin of each step of the program, in program order */
		List<StepOrigin> getOrigins() {
			return origins;
		}

		/** @return captured program, or empty when refused */
		Optional<CertifiedConstructionProgram2D> getProgram() {
			return program;
		}

		/** @return typed reason of a refusal, or {@code null} for a captured program */
		String getRefusal() {
			return refusal;
		}
	}
}
