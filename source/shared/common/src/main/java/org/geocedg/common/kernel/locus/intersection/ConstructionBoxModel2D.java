/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.ConstructionValidityPredicate2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.Decision;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.PredicateEvaluation;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxOutcome2D.Unresolved;

/**
 * Typed box evaluation of a certified construction program of any class
 * ({@code locus-v2-certified-construction-model.md} sections 5, 6 and 10). It replays the
 * exact-real formula sequence with outward intervals and first derivatives, like the
 * class-v1 model, and decides every validity predicate on the box instead of refusing it.
 *
 * <p>The validity predicates are the exact ones of section 10.4. An inherited tolerance
 * that restricts definedness inside their validity set (line and point definedness, the
 * circle type, a normalization branch, the suppression of an unlabelled second output at
 * coincident roots) leaves the box unresolved and is never certified existence. An
 * inherited tolerance that only extends definedness beyond the exact predicate (the
 * tangency window of the line-conic intersection, the incidence tolerance of limited
 * paths) is evaluation excess, never a boundary (finding F-CU2-1).</p>
 */
public final class ConstructionBoxModel2D {
	/** {@code Kernel.STANDARD_PRECISION}, the point and line predicate threshold. */
	static final double EPSILON = ConstructionIntervalModel2D.EPSILON;
	/**
	 * Conservative band of an unlabelled second output (section 10.3): the inherited
	 * tangency window is inside {@code D <= 4e-5 max(a^2, b^2, c^2)} for every retry
	 * tolerance and line normalization; the band takes a factor of 25 more.
	 */
	static final double COINCIDENT_WINDOW_FACTOR = 1E-3;
	/** Band term that covers the rounding of the inherited discriminant computation. */
	static final double COINCIDENT_ROUNDING_FACTOR = 1E-10;
	/**
	 * The inherited circle type needs {@code r >= STANDARD_PRECISION}; a squared radius
	 * above {@code (2 STANDARD_PRECISION)^2} certifies it.
	 */
	static final double RADIUS_SQUARED_MINIMUM = 4E-16;
	private static final double NORMALIZATION_MINIMUM = 1E-2;
	private static final double NORMALIZATION_MAXIMUM = 10E6;
	private static final int NORMALIZATION_LIMIT = 2_048;
	private static final Object UNKNOWN = new Object();

	private final CertifiedConstructionProgram2D program;
	private final String[] lineClass;
	private final boolean[] zeroVector;

	/** @param program captured program of any class */
	public ConstructionBoxModel2D(CertifiedConstructionProgram2D program) {
		this.program = program;
		List<Step> steps = program.getSteps();
		lineClass = new String[steps.size() + 1];
		zeroVector = new boolean[steps.size() + 1];
		for (int index = 0; index < steps.size(); index++) {
			Step step = steps.get(index);
			List<Operand> operands = step.getOperands();
			int node = index + 1;
			switch (step.getOperation()) {
			case JOIN:
			case RAY:
			case SEGMENT:
				lineClass[node] = "n" + node + ":0";
				break;
			case PARALLEL:
				lineClass[node] = lineClass(operands.get(1));
				break;
			case PERPENDICULAR:
				lineClass[node] = perpendicular(lineClass(operands.get(1)));
				break;
			case VECTOR_POINTS:
				zeroVector[node] = sameOperand(operands.get(0), operands.get(1));
				break;
			case SCALE:
				Operand factor = operands.get(0);
				zeroVector[node] = factor.getKind() == OperandKind.SCALAR
						&& factor.getValue(0) == 0 || isZeroVector(operands.get(1));
				break;
			default:
				break;
			}
		}
	}

	/**
	 * @param definition definition of a reconstructible dependent-point locus
	 * @return box model of the class-v3 program of its revision, or {@code null} when its
	 *         slice is outside the class
	 */
	public static ConstructionBoxModel2D capture(LocusDefinition2D definition) {
		if (definition == null || !(definition.getEvaluatorCapability()
				instanceof ReconstructibleLocusEvaluator2D)) {
			return null;
		}
		return ((ReconstructibleLocusEvaluator2D) definition.getEvaluatorCapability())
				.captureCertifiedProgramV3().map(ConstructionBoxModel2D::new).orElse(null);
	}

	/** @return the captured program */
	public CertifiedConstructionProgram2D getProgram() {
		return program;
	}

	/**
	 * Evaluates the program on one parameter box inside the driver domain.
	 *
	 * @param lower lower end of the box
	 * @param upper upper end of the box, not below {@code lower}
	 * @return typed outcome
	 */
	public ConstructionBoxOutcome2D evaluate(double lower, double upper) {
		return evaluate(new SplineOutwardInterval2D(lower, upper));
	}

	/**
	 * Evaluates the program on one parameter box inside the driver domain.
	 *
	 * @return typed outcome; never throws for a finite box
	 */
	ConstructionBoxOutcome2D evaluate(SplineOutwardInterval2D box) {
		return new Run().evaluate(box);
	}

	private String lineClass(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			return lineClass[operand.getNode()];
		}
		// A constant line: identical coefficient bits are the same direction.
		return "c" + Double.doubleToLongBits(operand.getValue(0)) + ","
				+ Double.doubleToLongBits(operand.getValue(1)) + ":0";
	}

	private static String perpendicular(String lineClass) {
		if (lineClass == null) {
			return null;
		}
		return lineClass.substring(0, lineClass.length() - 1)
				+ (lineClass.endsWith(":0") ? "1" : "0");
	}

	private boolean isZeroVector(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			return zeroVector[operand.getNode()];
		}
		return operand.getKind() == OperandKind.VECTOR && operand.getValue(0) == 0
				&& operand.getValue(1) == 0;
	}

	/** Exact structural identity of two operands: the same node or the same constant. */
	private static boolean sameOperand(Operand first, Operand second) {
		if (first.getKind() != second.getKind()) {
			return false;
		}
		if (first.getKind() == OperandKind.NODE) {
			return first.getNode() == second.getNode();
		}
		int count = first.getKind() == OperandKind.POINT ? 5 : 2;
		for (int index = 0; index < count; index++) {
			if (Double.doubleToLongBits(first.getValue(index))
					!= Double.doubleToLongBits(second.getValue(index))) {
				return false;
			}
		}
		return true;
	}

	/** One evaluation on one box. */
	private final class Run {
		private final List<PredicateEvaluation> evaluations = new ArrayList<>();
		private final List<String> selection = new ArrayList<>();
		private Object[] nodes;
		private int node;

		ConstructionBoxOutcome2D evaluate(SplineOutwardInterval2D box) {
			LocusInterval2D domain = program.getDomain();
			if (box.lower < domain.getLower() || box.upper > domain.getUpper()) {
				return ConstructionBoxOutcome2D.unresolved(Unresolved.OUTSIDE_DOMAIN, -1,
						null, "Parameter box is outside the driver domain", evaluations);
			}
			List<Step> steps = program.getSteps();
			nodes = new Object[steps.size() + 1];
			try {
				nodes[0] = driver(Jet.variable(box));
			} catch (ArithmeticException exception) {
				return ConstructionBoxOutcome2D.unresolved(Unresolved.ENCLOSURE, 0, null,
						String.valueOf(exception.getMessage()), evaluations);
			}
			Stop invalid = null;
			Stop unresolved = null;
			for (int index = 0; index < steps.size(); index++) {
				node = index + 1;
				Step step = steps.get(index);
				if (readsUnknown(step)) {
					nodes[node] = UNKNOWN;
					continue;
				}
				try {
					nodes[node] = apply(step);
				} catch (Stop stop) {
					nodes[node] = UNKNOWN;
					if (stop.invalid && invalid == null) {
						invalid = stop.at(node);
					} else if (!stop.invalid && unresolved == null) {
						unresolved = stop.at(node);
					}
				} catch (ArithmeticException exception) {
					nodes[node] = UNKNOWN;
					if (unresolved == null) {
						unresolved = new Stop(Unresolved.ENCLOSURE, null,
								String.valueOf(exception.getMessage())).at(node);
					}
				}
			}
			if (invalid != null) {
				return ConstructionBoxOutcome2D.invalid(invalid.step, invalid.predicate,
						invalid.detail, evaluations);
			}
			if (unresolved != null) {
				return ConstructionBoxOutcome2D.unresolved(unresolved.reason,
						unresolved.step, unresolved.predicate, unresolved.detail,
						evaluations);
			}
			PointJet output = (PointJet) nodes[steps.size()];
			return ConstructionBoxOutcome2D.valid(
					new SplineOutwardInterval2D[] {output.inhomX.value,
							output.inhomY.value},
					new SplineOutwardInterval2D[] {output.inhomX.derivative,
							output.inhomY.derivative},
					String.join(",", selection), evaluations);
		}

		private boolean readsUnknown(Step step) {
			for (Operand operand : step.getOperands()) {
				if (operand.getKind() == OperandKind.NODE
						&& nodes[operand.getNode()] == UNKNOWN) {
					return true;
				}
			}
			return false;
		}

		private PointJet driver(Jet t) {
			switch (program.getDriver()) {
			case CIRCLE:
				return circlePoint(t);
			case CIRCULAR_ARC:
				Jet reduced = program.getDriverConstant(10) == 1 ? t
						: Jet.constant(1).subtract(t);
				return circlePoint(Jet.constant(program.getDriverConstant(8)).add(reduced
						.multiply(Jet.constant(program.getDriverConstant(9)))));
			case SEGMENT:
				return PointJet.unit(
						Jet.constant(program.getDriverConstant(0))
								.add(t.multiply(Jet.constant(program.getDriverConstant(3)))),
						Jet.constant(program.getDriverConstant(1)).subtract(
								t.multiply(Jet.constant(program.getDriverConstant(2)))));
			case SCALAR_AFFINE:
				return PointJet.unit(
						t.multiply(Jet.constant(program.getDriverConstant(0)))
								.add(Jet.constant(program.getDriverConstant(1))),
						t.multiply(Jet.constant(program.getDriverConstant(2)))
								.add(Jet.constant(program.getDriverConstant(3))));
			default:
				throw new ArithmeticException("Unsupported driver");
			}
		}

		/** GeoConicND.pathChangedWithoutCheckEllipse followed by coordsEVtoRW. */
		private PointJet circlePoint(Jet angle) {
			Jet px = Jet.constant(program.getDriverConstant(0)).multiply(angle.cos());
			Jet py = Jet.constant(program.getDriverConstant(1)).multiply(angle.sin());
			Jet x = px.multiply(Jet.constant(program.getDriverConstant(2)))
					.add(py.multiply(Jet.constant(program.getDriverConstant(4))))
					.add(Jet.constant(program.getDriverConstant(6)));
			Jet y = px.multiply(Jet.constant(program.getDriverConstant(3)))
					.add(py.multiply(Jet.constant(program.getDriverConstant(5))))
					.add(Jet.constant(program.getDriverConstant(7)));
			return PointJet.unit(x, y);
		}

		private Object apply(Step step) {
			List<Operand> operands = step.getOperands();
			switch (step.getOperation()) {
			case JOIN:
				requireDistinct(operands);
				return join(point(operands.get(0)), point(operands.get(1)), null);
			case RAY:
			case SEGMENT:
				requireDistinct(operands);
				PointJet start = point(operands.get(0));
				return join(start, point(operands.get(1)),
						new Bound(step.getOperation() == Operation.RAY, start.inhomX,
								start.inhomY));
			case PARALLEL:
				return parallel(point(operands.get(0)), line(operands.get(1)));
			case PERPENDICULAR:
				return perpendicular(point(operands.get(0)), line(operands.get(1)));
			case MEET:
				return meet(operands);
			case MIDPOINT:
				PointJet first = point(operands.get(0));
				PointJet second = point(operands.get(1));
				return PointJet.unit(first.inhomX.add(second.inhomX).half(),
						first.inhomY.add(second.inhomY).half());
			case CIRCLE_RADIUS:
				PointJet center = point(operands.get(0));
				Jet radius = Jet.constant(step.getParameter(0));
				return new CircleJet(center.inhomX, center.inhomY, radius.multiply(radius));
			case CIRCLE_THROUGH:
				return circleThrough(operands);
			case LINE_CIRCLE:
				return new PairJet(line(operands.get(0)), circle(operands.get(1)), step);
			case INTERSECTION_OUTPUT:
				return output((PairJet) nodes[operands.get(0).getNode()],
						(int) step.getParameter(0));
			case UNIT_VECTOR_LINE:
				return lineDirection(line(operands.get(0)), step.getParameter(0) == 1);
			case UNIT_VECTOR:
				return vectorDirection(operands.get(0), step.getParameter(0) == 1);
			case VECTOR_POINTS:
				PointJet from = point(operands.get(0));
				PointJet to = point(operands.get(1));
				return new VectorJet(to.inhomX.subtract(from.inhomX),
						to.inhomY.subtract(from.inhomY));
			case SCALE:
				Jet factor = scalar(operands.get(0));
				VectorJet scaled = vector(operands.get(1));
				return new VectorJet(factor.multiply(scaled.x), factor.multiply(scaled.y));
			case DOT:
				VectorJet left = vector(operands.get(0));
				VectorJet right = vector(operands.get(1));
				return left.x.multiply(right.x).add(left.y.multiply(right.y));
			case POINT_PLUS_VECTOR:
				PointJet base = point(operands.get(0));
				VectorJet offset = vector(operands.get(1));
				return step.getParameter(0) > 0
						? PointJet.unit(base.inhomX.add(offset.x), base.inhomY.add(offset.y))
						: PointJet.unit(base.inhomX.subtract(offset.x),
								base.inhomY.subtract(offset.y));
			case METRIC_INPUT:
				throw new Stop(Unresolved.METRIC_VALUE, null,
						"The structural metric input contributes no value");
			default:
				return transform(step, point(operands.get(0)));
			}
		}

		private void requireDistinct(List<Operand> operands) {
			if (sameOperand(operands.get(0), operands.get(1))) {
				throw Stop.invalid(ConstructionValidityPredicate2D.DIRECTION,
						"The two points are the same operand");
			}
		}

		/** GeoVec3D.lineThroughPoints for two finite points. */
		private LineJet join(PointJet p, PointJet q, Bound bound) {
			return defined(p.inhomY.subtract(q.inhomY), q.inhomX.subtract(p.inhomX),
					p.inhomX.multiply(q.inhomY).subtract(p.inhomY.multiply(q.inhomX)),
					bound);
		}

		/** AlgoLinePointLine: cross(P, (l.y, -l.x, 0)). */
		private LineJet parallel(PointJet p, LineJet l) {
			return defined(p.z.multiply(l.a), p.z.multiply(l.b),
					p.x.multiply(l.a).add(p.y.multiply(l.b)).negate(), null);
		}

		/** AlgoOrthoLinePointLine: cross(P, (l.x, l.y, 0)), then normalization. */
		private LineJet perpendicular(PointJet p, LineJet l) {
			Jet[] coefficients = {p.z.multiply(l.b).negate(), p.z.multiply(l.a),
					p.x.multiply(l.b).subtract(p.y.multiply(l.a))};
			if (uniformlyTrue(allBelow(coefficients, EPSILON, true),
					allBelow(coefficients, EPSILON, false))) {
				// All three isZero: GeoGebra returns the coefficients unscaled.
				return defined(coefficients[0], coefficients[1], coefficients[2], null);
			}
			for (int iteration = 0; uniformlyTrue(
					allBelow(coefficients, NORMALIZATION_MINIMUM, true),
					allBelow(coefficients, NORMALIZATION_MINIMUM, false)); iteration++) {
				requireIterations(iteration);
				coefficients = scale(coefficients, 2);
			}
			for (int iteration = 0; uniformlyTrue(
					allAbove(coefficients, NORMALIZATION_MAXIMUM, true),
					allAbove(coefficients, NORMALIZATION_MAXIMUM, false)); iteration++) {
				requireIterations(iteration);
				coefficients = scale(coefficients, 0.5);
			}
			return defined(coefficients[0], coefficients[1], coefficients[2], null);
		}

		/**
		 * GeoGebra line definedness, {@code |a| >= EPSILON} or {@code |b| >= EPSILON}, on
		 * the whole box; the exact predicate is a nonzero direction.
		 */
		private LineJet defined(Jet a, Jet b, Jet c, Bound bound) {
			if (LineJet.nonZero(a.value) || LineJet.nonZero(b.value)) {
				record(ConstructionValidityPredicate2D.DIRECTION, -1,
						a.square().add(b.square()), Decision.TRUE);
				return new LineJet(a, b, c, bound);
			}
			Jet norm = a.square().add(b.square());
			if (!a.value.containsZero() || !b.value.containsZero()) {
				record(ConstructionValidityPredicate2D.DIRECTION, -1, norm,
						Decision.TOLERANCE);
				throw new Stop(Unresolved.TOLERANCE,
						ConstructionValidityPredicate2D.DIRECTION,
						"Line definedness depends on the inherited threshold");
			}
			record(ConstructionValidityPredicate2D.DIRECTION, -1, norm, Decision.UNDECIDED);
			throw new Stop(Unresolved.PREDICATE, ConstructionValidityPredicate2D.DIRECTION,
					"Line direction is not decided");
		}

		/** AlgoIntersectLines: cross(g, h), GeoPoint.updateCoords, then the bounds. */
		private PointJet meet(List<Operand> operands) {
			if (lineClass(operands.get(0)) != null
					&& lineClass(operands.get(0)).equals(lineClass(operands.get(1)))) {
				throw Stop.invalid(ConstructionValidityPredicate2D.DIVISOR,
						"The lines are parallel by construction");
			}
			LineJet g = line(operands.get(0));
			LineJet h = line(operands.get(1));
			PointJet point = normalized(g.b.multiply(h.c).subtract(g.c.multiply(h.b)),
					g.c.multiply(h.a).subtract(g.a.multiply(h.c)),
					g.a.multiply(h.b).subtract(g.b.multiply(h.a)));
			requireMember(g, point.inhomX, point.inhomY, -1);
			requireMember(h, point.inhomX, point.inhomY, -1);
			return point;
		}

		/** GeoPoint.setCoords followed by updateCoords, decided on the box. */
		private PointJet normalized(Jet x, Jet y, Jet z) {
			if (z.isUnitConstant()) {
				return PointJet.unit(x, y);
			}
			SplineOutwardInterval2D size = ConstructionIntervalModel2D.abs(z.value);
			boolean finite = size.lower > EPSILON
					|| size.lower > Math.nextUp(
							ConstructionIntervalModel2D.abs(x.value).upper * EPSILON)
					|| size.lower > Math.nextUp(
							ConstructionIntervalModel2D.abs(y.value).upper * EPSILON);
			if (!finite) {
				boolean undecided = z.value.containsZero();
				record(ConstructionValidityPredicate2D.DIVISOR, -1, z,
						undecided ? Decision.UNDECIDED : Decision.TOLERANCE);
				throw new Stop(undecided ? Unresolved.PREDICATE : Unresolved.TOLERANCE,
						ConstructionValidityPredicate2D.DIVISOR,
						"Point finiteness is not certified");
			}
			record(ConstructionValidityPredicate2D.DIVISOR, -1, z, Decision.TRUE);
			// A negative z negates (x, y, z); a unit z then needs no division (class v1).
			return z.value.upper < 0 ? divided(x.negate(), y.negate(), z.negate())
					: divided(x, y, z);
		}

		private PointJet divided(Jet x, Jet y, Jet z) {
			return z.isUnitConstant() ? PointJet.unit(x, y)
					: new PointJet(x, y, z, x.divide(z), y.divide(z));
		}

		private PointJet transform(Step step, PointJet p) {
			switch (step.getOperation()) {
			case TRANSLATE:
				return translate(step, p);
			case EXPRESSION_TRANSLATE:
				return translate(step, PointJet.unit(p.inhomX, p.inhomY));
			case ROTATE:
				Jet cosine = Jet.constant(step.getParameter(0));
				Jet sine = Jet.constant(step.getParameter(1));
				return normalized(p.x.multiply(cosine).subtract(p.y.multiply(sine)),
						p.x.multiply(sine).add(p.y.multiply(cosine)), p.z);
			case ROTATE_ABOUT:
				return rotateAbout(step, p);
			case MIRROR_POINT:
				Jet two = Jet.constant(2);
				return normalized(
						two.multiply(p.z).multiply(Jet.constant(step.getParameter(0)))
								.subtract(p.x),
						two.multiply(p.z).multiply(Jet.constant(step.getParameter(1)))
								.subtract(p.y), p.z);
			case MIRROR_LINE:
				return mirrorLine(step, p);
			case DILATE:
				Jet factor = Jet.constant(step.getParameter(0));
				Jet complement = Jet.constant(1).subtract(factor);
				return normalized(
						factor.multiply(p.x).add(complement.multiply(
								Jet.constant(step.getParameter(1))).multiply(p.z)),
						factor.multiply(p.y).add(complement.multiply(
								Jet.constant(step.getParameter(2))).multiply(p.z)), p.z);
			default:
				throw new ArithmeticException("Unsupported operation");
			}
		}

		/** AlgoTranslate: (x + vx z, y + vy z, z), then GeoPoint.updateCoords. */
		private PointJet translate(Step step, PointJet p) {
			return normalized(
					p.x.add(Jet.constant(step.getParameter(0)).multiply(p.z)),
					p.y.add(Jet.constant(step.getParameter(1)).multiply(p.z)), p.z);
		}

		/** GeoPoint.rotate(phi, Q) with the evaluator's own cosine and sine. */
		private PointJet rotateAbout(Step step, PointJet p) {
			Jet cosine = Jet.constant(step.getParameter(0));
			Jet sine = Jet.constant(step.getParameter(1));
			Jet qx = p.z.multiply(Jet.constant(step.getParameter(2)));
			Jet qy = p.z.multiply(Jet.constant(step.getParameter(3)));
			Jet dx = p.x.subtract(qx);
			return normalized(
					dx.multiply(cosine).add(qy.subtract(p.y).multiply(sine)).add(qx),
					dx.multiply(sine).add(p.y.subtract(qy).multiply(cosine)).add(qy), p.z);
		}

		/** GeoPoint.mirror(GeoLineND) with the evaluator's captured branch and angle. */
		private PointJet mirrorLine(Step step, PointJet p) {
			Jet offset = p.z.negate().multiply(Jet.constant(step.getParameter(2)));
			boolean horizontalOffset = step.getParameter(3) == 1;
			Jet qx = horizontalOffset
					? offset.divide(Jet.constant(step.getParameter(0))) : Jet.constant(0);
			Jet qy = horizontalOffset ? Jet.constant(0)
					: offset.divide(Jet.constant(step.getParameter(1)));
			Jet cosine = Jet.constant(step.getParameter(4));
			Jet sine = Jet.constant(step.getParameter(5));
			Jet u = p.x.subtract(qx);
			Jet w = p.y.subtract(qy);
			return normalized(u.multiply(cosine).add(w.multiply(sine)).add(qx),
					u.multiply(sine).subtract(w.multiply(cosine)).add(qy), p.z);
		}

		/** Circle by centre through a point; its exact predicate is a nonzero radius. */
		private CircleJet circleThrough(List<Operand> operands) {
			if (sameOperand(operands.get(0), operands.get(1))) {
				throw Stop.invalid(ConstructionValidityPredicate2D.RADIUS,
						"The centre and the point are the same operand");
			}
			PointJet center = point(operands.get(0));
			PointJet through = point(operands.get(1));
			Jet squared = through.inhomX.subtract(center.inhomX).square()
					.add(through.inhomY.subtract(center.inhomY).square());
			if (squared.value.lower > RADIUS_SQUARED_MINIMUM) {
				record(ConstructionValidityPredicate2D.RADIUS, -1, squared, Decision.TRUE);
				return new CircleJet(center.inhomX, center.inhomY, squared);
			}
			boolean positive = squared.value.lower > 0;
			record(ConstructionValidityPredicate2D.RADIUS, -1, squared,
					positive ? Decision.TOLERANCE : Decision.UNDECIDED);
			throw new Stop(positive ? Unresolved.TOLERANCE : Unresolved.PREDICATE,
					ConstructionValidityPredicate2D.RADIUS,
					positive ? "The inherited circle type needs a radius above its threshold"
							: "Circle radius is not decided");
		}

		/**
		 * One output of a line-circle intersection (section 10.2). The raw roots are the
		 * intersection points ordered by their parameter along the algorithmic direction
		 * {@code (b, -a)}; the output is selected by the captured rule.
		 */
		private PointJet output(PairJet pair, int index) {
			LineJet line = pair.line;
			CircleJet circle = pair.circle;
			Jet norm = line.a.square().add(line.b.square());
			Jet distance = line.a.multiply(circle.mx).add(line.b.multiply(circle.my))
					.add(line.c);
			Jet discriminant = circle.radiusSquared.multiply(norm)
					.subtract(distance.square());
			requirePositive(ConstructionValidityPredicate2D.DISCRIMINANT, -1, discriminant);
			Jet root = discriminant.sqrt();
			Jet footX = circle.mx.subtract(distance.multiply(line.a).divide(norm));
			Jet footY = circle.my.subtract(distance.multiply(line.b).divide(norm));
			Jet offsetX = root.multiply(line.b).divide(norm);
			Jet offsetY = root.multiply(line.a).divide(norm).negate();
			int selected;
			if (pair.compaction) {
				selected = -1;
				int count = 0;
				for (int raw = 0; raw < 2 && selected < 0; raw++) {
					Decision member = membership(line, circle, root, raw);
					if (member == Decision.UNDECIDED) {
						throw new Stop(Unresolved.PREDICATE, line.bound.predicate(),
								"The roots on the path are not decided");
					}
					if (member == Decision.TRUE) {
						if (count == index) {
							selected = raw;
						}
						count++;
					}
				}
				if (selected < 0) {
					throw Stop.invalid(line.bound.predicate(),
							"Fewer roots lie on the path than the output index");
				}
			} else {
				selected = pair.permutation[index];
				if (line.bound != null) {
					Decision member = membership(line, circle, root, selected);
					if (member == Decision.FALSE) {
						throw Stop.invalid(line.bound.predicate(),
								"The selected root is not on the path");
					}
					if (member == Decision.UNDECIDED) {
						throw new Stop(Unresolved.PREDICATE, line.bound.predicate(),
								"The selected root is not decided on the path");
					}
				}
			}
			if (index == 1 && !pair.labelled[1]) {
				requireOutsideCoincidenceBand(line, discriminant, distance, norm, circle);
			}
			selection.add("n" + node + ":r" + selected);
			double sign = selected == 0 ? -1 : 1;
			Jet signed = Jet.constant(sign);
			return PointJet.unit(footX.add(signed.multiply(offsetX)),
					footY.add(signed.multiply(offsetY)));
		}

		/**
		 * Membership of a raw root in the bounded line: the parameter
		 * {@code (X - O).(b, -a) = (M - O).(b, -a) +- sqrt(D)}.
		 */
		private Decision membership(LineJet line, CircleJet circle, Jet root, int raw) {
			if (line.bound == null) {
				return Decision.TRUE;
			}
			Jet base = circle.mx.subtract(line.bound.x).multiply(line.b)
					.subtract(circle.my.subtract(line.bound.y).multiply(line.a));
			Jet parameter = raw == 0 ? base.subtract(root) : base.add(root);
			return decideMember(line, parameter, raw);
		}

		/** Section 10.3: the inherited suppression may undefine an unlabelled output. */
		private void requireOutsideCoincidenceBand(LineJet line, Jet discriminant,
				Jet distance, Jet norm, CircleJet circle) {
			SplineOutwardInterval2D scale = line.a.square().value
					.hull(line.b.square().value).hull(line.c.square().value);
			SplineOutwardInterval2D rounding = distance.square().value
					.add(circle.radiusSquared.value.multiply(norm.value));
			boolean outside = discriminant.value.lower
					> COINCIDENT_WINDOW_FACTOR * Math.nextUp(scale.upper)
					&& discriminant.value.lower
							> COINCIDENT_ROUNDING_FACTOR * Math.nextUp(rounding.upper);
			if (!outside) {
				record(ConstructionValidityPredicate2D.DISCRIMINANT, 1, discriminant,
						Decision.TOLERANCE);
				throw new Stop(Unresolved.UNLABELLED_COINCIDENT_OUTPUT,
						ConstructionValidityPredicate2D.DISCRIMINANT,
						"An unlabelled second output may be suppressed at coincident roots");
			}
		}

		/** The meet point lies on each bounded operand. */
		private void requireMember(LineJet line, Jet x, Jet y, int root) {
			if (line.bound == null) {
				return;
			}
			Jet parameter = x.subtract(line.bound.x).multiply(line.b)
					.subtract(y.subtract(line.bound.y).multiply(line.a));
			Decision member = decideMember(line, parameter, root);
			if (member == Decision.FALSE) {
				throw Stop.invalid(line.bound.predicate(),
						"The point is not on the bounded line");
			}
			if (member == Decision.UNDECIDED) {
				throw new Stop(Unresolved.PREDICATE, line.bound.predicate(),
						"Bounded-line membership is not decided");
			}
		}

		/**
		 * @param parameter {@code (X - O).(b, -a)}; a ray needs it nonnegative, a segment
		 *        needs it inside {@code [0, a^2 + b^2]}
		 */
		private Decision decideMember(LineJet line, Jet parameter, int root) {
			ConstructionValidityPredicate2D predicate = line.bound.predicate();
			Decision lower = sign(parameter);
			if (line.bound.ray || lower == Decision.FALSE) {
				record(predicate, root, parameter, lower);
				return lower;
			}
			Jet remaining = line.a.square().add(line.b.square()).subtract(parameter);
			Decision upper = sign(remaining);
			Decision both = upper == Decision.FALSE ? Decision.FALSE
					: lower == Decision.TRUE && upper == Decision.TRUE ? Decision.TRUE
							: Decision.UNDECIDED;
			record(predicate, root, lower == Decision.TRUE ? remaining : parameter, both);
			return both;
		}

		/** AlgoUnitVectorLine: (b, -a), divided by its length when normalized. */
		private VectorJet lineDirection(LineJet line, boolean normalize) {
			if (!normalize) {
				return new VectorJet(line.b, line.a.negate());
			}
			Jet length = line.a.square().add(line.b.square()).sqrt();
			return new VectorJet(line.b.divide(length), line.a.negate().divide(length));
		}

		/** AlgoUnitVectorVector: the vector, divided by its length when normalized. */
		private VectorJet vectorDirection(Operand operand, boolean normalize) {
			if (isZeroVector(operand)) {
				throw Stop.invalid(ConstructionValidityPredicate2D.DIRECTION,
						"The vector is zero by construction");
			}
			VectorJet source = vector(operand);
			if (!normalize) {
				return source;
			}
			Jet squared = source.x.square().add(source.y.square());
			requirePositive(ConstructionValidityPredicate2D.DIRECTION, -1, squared);
			Jet length = squared.sqrt();
			return new VectorJet(source.x.divide(length), source.y.divide(length));
		}

		/** A strict-inequality predicate is decided by its enclosure. */
		private void requirePositive(ConstructionValidityPredicate2D predicate, int root,
				Jet value) {
			Decision decision = sign(value);
			if (predicate.isEquality() && decision == Decision.FALSE) {
				decision = Decision.UNDECIDED;
			}
			record(predicate, root, value, decision);
			if (decision == Decision.FALSE) {
				throw Stop.invalid(predicate, "Certified negative on the box");
			}
			if (decision == Decision.UNDECIDED) {
				throw new Stop(Unresolved.PREDICATE, predicate,
						"Predicate is not decided on the box");
			}
		}

		private Decision sign(Jet value) {
			return value.value.lower > 0 ? Decision.TRUE
					: value.value.upper < 0 ? Decision.FALSE : Decision.UNDECIDED;
		}

		private void record(ConstructionValidityPredicate2D predicate, int root, Jet value,
				Decision decision) {
			evaluations.add(new PredicateEvaluation(node, predicate, root, value.value,
					value.derivative, decision));
		}

		private PointJet point(Operand operand) {
			if (operand.getKind() == OperandKind.NODE) {
				return (PointJet) nodes[operand.getNode()];
			}
			return PointJet.constant(operand.getValue(0), operand.getValue(1),
					operand.getValue(2), operand.getValue(3), operand.getValue(4));
		}

		private LineJet line(Operand operand) {
			switch (operand.getKind()) {
			case NODE:
				return (LineJet) nodes[operand.getNode()];
			case RAY:
			case SEGMENT:
				return new LineJet(Jet.constant(operand.getValue(0)),
						Jet.constant(operand.getValue(1)), Jet.constant(operand.getValue(2)),
						new Bound(operand.getKind() == OperandKind.RAY,
								Jet.constant(operand.getValue(3)),
								Jet.constant(operand.getValue(4))));
			default:
				return new LineJet(Jet.constant(operand.getValue(0)),
						Jet.constant(operand.getValue(1)), Jet.constant(operand.getValue(2)),
						null);
			}
		}

		/** A circle node, or a constant circle of matrix (1, 1, A2, 0, A4, A5). */
		private CircleJet circle(Operand operand) {
			if (operand.getKind() == OperandKind.NODE) {
				return (CircleJet) nodes[operand.getNode()];
			}
			Jet a4 = Jet.constant(operand.getValue(1));
			Jet a5 = Jet.constant(operand.getValue(2));
			return new CircleJet(a4.negate(), a5.negate(),
					a4.square().add(a5.square()).subtract(Jet.constant(operand.getValue(0))));
		}

		private VectorJet vector(Operand operand) {
			if (operand.getKind() == OperandKind.NODE) {
				return (VectorJet) nodes[operand.getNode()];
			}
			return new VectorJet(Jet.constant(operand.getValue(0)),
					Jet.constant(operand.getValue(1)));
		}

		private Jet scalar(Operand operand) {
			if (operand.getKind() == OperandKind.NODE) {
				return (Jet) nodes[operand.getNode()];
			}
			return Jet.constant(operand.getValue(0));
		}
	}

	/**
	 * @return whether every magnitude is below the threshold on the whole box
	 *         ({@code everywhere}), or could be below it somewhere
	 */
	private static boolean allBelow(Jet[] values, double threshold, boolean everywhere) {
		boolean all = true;
		for (Jet value : values) {
			SplineOutwardInterval2D magnitude = ConstructionIntervalModel2D.abs(value.value);
			all &= everywhere ? magnitude.upper < threshold : magnitude.lower < threshold;
		}
		return all;
	}

	/**
	 * @return whether every magnitude is above the threshold on the whole box
	 *         ({@code everywhere}), or could be above it somewhere
	 */
	private static boolean allAbove(Jet[] values, double threshold, boolean everywhere) {
		boolean all = true;
		for (Jet value : values) {
			SplineOutwardInterval2D magnitude = ConstructionIntervalModel2D.abs(value.value);
			all &= everywhere ? magnitude.lower > threshold : magnitude.upper > threshold;
		}
		return all;
	}

	/** A normalization branch that is not uniform leaves the box unresolved. */
	private static boolean uniformlyTrue(boolean certain, boolean possible) {
		if (certain) {
			return true;
		}
		if (!possible) {
			return false;
		}
		throw new Stop(Unresolved.TOLERANCE, ConstructionValidityPredicate2D.DIRECTION,
				"The inherited line normalization is not uniform on the box");
	}

	private static void requireIterations(int iteration) {
		if (iteration >= NORMALIZATION_LIMIT) {
			throw new ArithmeticException("Line normalization did not settle");
		}
	}

	private static Jet[] scale(Jet[] values, double factor) {
		Jet[] result = new Jet[values.length];
		for (int index = 0; index < values.length; index++) {
			result[index] = values[index].multiply(Jet.constant(factor));
		}
		return result;
	}

	/** Typed stop of one step: certified invalid, or unresolved with its reason. */
	private static final class Stop extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final boolean invalid;
		private final Unresolved reason;
		private final ConstructionValidityPredicate2D predicate;
		private final String detail;
		private int step = -1;

		private Stop(Unresolved reason, ConstructionValidityPredicate2D predicate,
				String detail) {
			this(false, reason, predicate, detail);
		}

		private Stop(boolean invalid, Unresolved reason,
				ConstructionValidityPredicate2D predicate, String detail) {
			super(null, null, false, false);
			this.invalid = invalid;
			this.reason = reason;
			this.predicate = predicate;
			this.detail = detail;
		}

		static Stop invalid(ConstructionValidityPredicate2D predicate, String detail) {
			return new Stop(true, null, predicate, detail);
		}

		Stop at(int node) {
			step = node;
			return this;
		}
	}

	/** Bound of a ray or segment: its start point; its direction is {@code (b, -a)}. */
	private static final class Bound {
		private final boolean ray;
		private final Jet x;
		private final Jet y;

		private Bound(boolean ray, Jet x, Jet y) {
			this.ray = ray;
			this.x = x;
			this.y = y;
		}

		ConstructionValidityPredicate2D predicate() {
			return ray ? ConstructionValidityPredicate2D.RAY_PARAMETER
					: ConstructionValidityPredicate2D.SEGMENT_PARAMETER;
		}
	}

	/** Line coefficients certified defined on the whole box, with an optional bound. */
	private static final class LineJet {
		private final Jet a;
		private final Jet b;
		private final Jet c;
		private final Bound bound;

		private LineJet(Jet a, Jet b, Jet c, Bound bound) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.bound = bound;
		}

		private static boolean nonZero(SplineOutwardInterval2D value) {
			return value.lower >= EPSILON || value.upper <= -EPSILON;
		}
	}

	/** Circle by its centre and squared radius. */
	private static final class CircleJet {
		private final Jet mx;
		private final Jet my;
		private final Jet radiusSquared;

		private CircleJet(Jet mx, Jet my, Jet radiusSquared) {
			this.mx = mx;
			this.my = my;
			this.radiusSquared = radiusSquared;
		}
	}

	/** Line and circle of an intersection with its captured selection rule. */
	private static final class PairJet {
		private final LineJet line;
		private final CircleJet circle;
		private final boolean compaction;
		private final int[] permutation;
		private final boolean[] labelled;

		private PairJet(LineJet line, CircleJet circle, Step step) {
			this.line = line;
			this.circle = circle;
			compaction = step.getParameter(0) == 1;
			permutation = new int[] {(int) step.getParameter(1),
					(int) step.getParameter(2)};
			labelled = new boolean[] {step.getParameter(3) == 1,
					step.getParameter(4) == 1};
		}

		@Override
		public String toString() {
			return "pair:" + (compaction ? "compaction" : Arrays.toString(permutation))
					+ ":" + Arrays.toString(labelled);
		}
	}

	/** Vector components. */
	private static final class VectorJet {
		private final Jet x;
		private final Jet y;

		private VectorJet(Jet x, Jet y) {
			this.x = x;
			this.y = y;
		}
	}

	/** Normalized homogeneous point with its inhomogeneous coordinates. */
	private static final class PointJet {
		private final Jet x;
		private final Jet y;
		private final Jet z;
		private final Jet inhomX;
		private final Jet inhomY;

		private PointJet(Jet x, Jet y, Jet z, Jet inhomX, Jet inhomY) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.inhomX = inhomX;
			this.inhomY = inhomY;
		}

		/** A point GeoGebra sets with {@code setCoords(x, y, 1)}. */
		static PointJet unit(Jet x, Jet y) {
			return new PointJet(x, y, Jet.constant(1), x, y);
		}

		static PointJet constant(double x, double y, double z, double inhomX,
				double inhomY) {
			return new PointJet(Jet.constant(x), Jet.constant(y), Jet.constant(z),
					Jet.constant(inhomX), Jet.constant(inhomY));
		}
	}

	/** Value and first derivative with respect to the driver parameter. */
	static final class Jet {
		final SplineOutwardInterval2D value;
		final SplineOutwardInterval2D derivative;

		private Jet(SplineOutwardInterval2D value, SplineOutwardInterval2D derivative) {
			this.value = value;
			this.derivative = derivative;
		}

		static Jet constant(double value) {
			return new Jet(SplineOutwardInterval2D.point(value),
					SplineOutwardInterval2D.point(0));
		}

		static Jet variable(SplineOutwardInterval2D value) {
			return new Jet(value, SplineOutwardInterval2D.point(1));
		}

		boolean isUnitConstant() {
			return value.lower == 1 && value.upper == 1 && derivative.lower == 0
					&& derivative.upper == 0;
		}

		Jet add(Jet other) {
			return new Jet(value.add(other.value), derivative.add(other.derivative));
		}

		Jet subtract(Jet other) {
			return new Jet(value.subtract(other.value),
					derivative.subtract(other.derivative));
		}

		Jet negate() {
			return new Jet(value.negate(), derivative.negate());
		}

		Jet multiply(Jet other) {
			return new Jet(value.multiply(other.value),
					derivative.multiply(other.value).add(value.multiply(other.derivative)));
		}

		Jet divide(Jet other) {
			SplineOutwardInterval2D quotient = value.divide(other.value);
			SplineOutwardInterval2D numerator = derivative.multiply(other.value)
					.subtract(value.multiply(other.derivative));
			return new Jet(quotient,
					numerator.divide(other.value.multiply(other.value)));
		}

		Jet half() {
			return multiply(constant(0.5));
		}

		/** Square with a nonnegative enclosure; derivative {@code 2 v v'}. */
		Jet square() {
			SplineOutwardInterval2D squared;
			if (value.lower >= 0 || value.upper <= 0) {
				squared = value.multiply(value);
				if (squared.lower < 0) {
					squared = new SplineOutwardInterval2D(0, squared.upper);
				}
			} else {
				double magnitude = Math.max(-value.lower, value.upper);
				squared = new SplineOutwardInterval2D(0, Math.nextUp(magnitude * magnitude));
			}
			return new Jet(squared, value.multiply(derivative).multiply(
					SplineOutwardInterval2D.point(2)));
		}

		/**
		 * Square root of a positive enclosure; {@code Math.sqrt} is correctly rounded, so
		 * one adjacent value each way encloses it. Derivative {@code v' / (2 sqrt v)}.
		 */
		Jet sqrt() {
			if (!(value.lower > 0)) {
				throw new ArithmeticException("Square root of an enclosure that is not positive");
			}
			SplineOutwardInterval2D root = new SplineOutwardInterval2D(
					Math.max(0, Math.nextDown(Math.sqrt(value.lower))),
					Math.nextUp(Math.sqrt(value.upper)));
			return new Jet(root, derivative.divide(root.multiply(
					SplineOutwardInterval2D.point(2))));
		}

		Jet cos() {
			return new Jet(ConstructionIntervalModel2D.cosine(value),
					ConstructionIntervalModel2D.sine(value).negate().multiply(derivative));
		}

		Jet sin() {
			return new Jet(ConstructionIntervalModel2D.sine(value),
					ConstructionIntervalModel2D.cosine(value).multiply(derivative));
		}
	}
}
