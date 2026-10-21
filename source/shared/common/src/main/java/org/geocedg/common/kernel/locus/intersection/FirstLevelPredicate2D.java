/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.List;
import java.util.Optional;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.exact.ExactPolynomial1D;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.geocedg.common.kernel.locus.exact.TrigonometricPolynomial2D;

/**
 * Exact polynomial form of a first-level validity predicate of a program with a circle or
 * segment driver ({@code locus-v2-certified-construction-model.md} section 10.6;
 * certification design section 4.6). The captured binary64 constants are exact rationals;
 * the driver point of a circle is linear in {@code c = cos t} and {@code s = sin t}, and
 * that of a segment is linear in {@code t}. Points that depend rationally on the driver
 * (joins, parallels, perpendiculars, meets, midpoints and the transforms of class v1) are
 * carried as polynomial homogeneous triples that mirror the formulas of
 * {@link ConstructionBoxModel2D}.
 *
 * <p>A strict-inequality predicate is multiplied by squares of the scale factors of its
 * operands, which keeps its sign wherever the operands are defined; an equality predicate
 * keeps its zero set, its sign being that of a homogeneous scale. The exact certificate then
 * decides the configuration of the captured constants. An output of a line-circle
 * intersection depends on a square root and is not first-level; nor is any vector, scalar or
 * metric value, nor the membership of a point in a bounded line.</p>
 */
final class FirstLevelPredicate2D {
	private final CertifiedConstructionProgram2D program;
	private final Object[] nodes;

	private FirstLevelPredicate2D(CertifiedConstructionProgram2D program, int lastNode) {
		this.program = program;
		nodes = new Object[lastNode + 1];
	}

	/**
	 * @return exact polynomial in {@code c = cos t} and {@code s = sin t} with the sign of
	 *         the predicate where its operands are defined, or empty when the driver is not
	 *         a circle or the predicate is not first-level
	 */
	static Optional<TrigonometricPolynomial2D> build(CertifiedConstructionProgram2D program,
			ConstructionBoxModel2D.Target target) {
		if (program.getDriver() != Driver.CIRCLE) {
			return Optional.empty();
		}
		return walk(program, target);
	}

	/**
	 * @return exact polynomial in the driver parameter {@code t} of a segment driver with
	 *         the sign of the predicate where its operands are defined, or empty when the
	 *         driver is not a segment or the predicate is not first-level
	 */
	static Optional<ExactPolynomial1D> buildSegment(CertifiedConstructionProgram2D program,
			ConstructionBoxModel2D.Target target) {
		if (program.getDriver() != Driver.SEGMENT) {
			return Optional.empty();
		}
		// The parameter t is carried by the cosine variable; no sine term can appear.
		Optional<TrigonometricPolynomial2D> form = walk(program, target);
		if (!form.isPresent()) {
			return Optional.empty();
		}
		TrigonometricPolynomial2D q = form.get();
		int degree = Math.max(q.totalDegree(), 0);
		ExactRational2D[] coefficients = new ExactRational2D[degree + 1];
		int terms = 0;
		for (int power = 0; power <= degree; power++) {
			coefficients[power] = q.coefficient(power, 0);
			terms += coefficients[power].isZero() ? 0 : 1;
		}
		if (terms != q.termCount()) {
			return Optional.empty();
		}
		return Optional.of(ExactPolynomial1D.of(coefficients));
	}

	private static Optional<TrigonometricPolynomial2D> walk(
			CertifiedConstructionProgram2D program, ConstructionBoxModel2D.Target target) {
		if (target.step < 1 || target.step > program.getSteps().size()) {
			return Optional.empty();
		}
		FirstLevelPredicate2D walk = new FirstLevelPredicate2D(program, target.step);
		try {
			walk.evaluate(target.step);
			return Optional.ofNullable(walk.predicate(target));
		} catch (NotFirstLevel refusal) {
			return Optional.empty();
		}
	}

	private void evaluate(int lastNode) {
		nodes[0] = driverPoint();
		List<Step> steps = program.getSteps();
		for (int node = 1; node <= lastNode; node++) {
			try {
				nodes[node] = apply(steps.get(node - 1));
			} catch (NotFirstLevel | ArithmeticException refusal) {
				// A step the predicate may not read; reading it refuses the predicate.
				nodes[node] = null;
			}
		}
	}

	/** The driver point of {@code ConstructionBoxModel2D}, with exact constants. */
	private Point driverPoint() {
		TrigonometricPolynomial2D c = TrigonometricPolynomial2D.cosine();
		if (program.getDriver() == Driver.SEGMENT) {
			return Point.unit(constantTerm(0).add(c.scale(constant(3))),
					constantTerm(1).subtract(c.scale(constant(2))));
		}
		TrigonometricPolynomial2D px = c.scale(constant(0));
		TrigonometricPolynomial2D py = TrigonometricPolynomial2D.sine().scale(constant(1));
		return Point.unit(px.scale(constant(2)).add(py.scale(constant(4)))
				.add(constantTerm(6)), px.scale(constant(3)).add(py.scale(constant(5)))
				.add(constantTerm(7)));
	}

	private ExactRational2D constant(int index) {
		return ExactRational2D.ofBinary64(program.getDriverConstant(index));
	}

	private TrigonometricPolynomial2D constantTerm(int index) {
		return TrigonometricPolynomial2D.constant(constant(index));
	}

	/** @return symbolic value of the step, or {@code null} when it is not first-level */
	private Object apply(Step step) {
		List<Operand> operands = step.getOperands();
		switch (step.getOperation()) {
		case JOIN:
		case RAY:
		case SEGMENT:
			return join(inhomogeneous(operands.get(0)), inhomogeneous(operands.get(1)));
		case PARALLEL: {
			Point p = homogeneous(operands.get(0));
			Line l = line(operands.get(1));
			return new Line(p.z.multiply(l.a), p.z.multiply(l.b),
					p.x.multiply(l.a).add(p.y.multiply(l.b)).negate());
		}
		case PERPENDICULAR: {
			// The power-of-two normalization is a positive scale of the line.
			Point p = homogeneous(operands.get(0));
			Line l = line(operands.get(1));
			return new Line(p.z.multiply(l.b).negate(), p.z.multiply(l.a),
					p.x.multiply(l.b).subtract(p.y.multiply(l.a)));
		}
		case MEET: {
			Line g = line(operands.get(0));
			Line h = line(operands.get(1));
			return new Point(g.b.multiply(h.c).subtract(g.c.multiply(h.b)),
					g.c.multiply(h.a).subtract(g.a.multiply(h.c)),
					g.a.multiply(h.b).subtract(g.b.multiply(h.a)));
		}
		case MIDPOINT: {
			Point p = inhomogeneous(operands.get(0));
			Point q = inhomogeneous(operands.get(1));
			TrigonometricPolynomial2D weight = p.z.multiply(q.z);
			return new Point(p.x.multiply(q.z).add(q.x.multiply(p.z)),
					p.y.multiply(q.z).add(q.y.multiply(p.z)),
					weight.scale(ExactRational2D.of(2)));
		}
		case TRANSLATE:
		case EXPRESSION_TRANSLATE: {
			Point p = step.getOperation() == Operation.TRANSLATE
					? homogeneous(operands.get(0)) : inhomogeneous(operands.get(0));
			return new Point(p.x.add(p.z.scale(parameter(step, 0))),
					p.y.add(p.z.scale(parameter(step, 1))), p.z);
		}
		case ROTATE: {
			Point p = homogeneous(operands.get(0));
			ExactRational2D cosine = parameter(step, 0);
			ExactRational2D sine = parameter(step, 1);
			return new Point(p.x.scale(cosine).subtract(p.y.scale(sine)),
					p.x.scale(sine).add(p.y.scale(cosine)), p.z);
		}
		case ROTATE_ABOUT:
			return rotateAbout(step, homogeneous(operands.get(0)));
		case MIRROR_POINT: {
			Point p = homogeneous(operands.get(0));
			ExactRational2D two = ExactRational2D.of(2);
			return new Point(p.z.scale(two.multiply(parameter(step, 0))).subtract(p.x),
					p.z.scale(two.multiply(parameter(step, 1))).subtract(p.y), p.z);
		}
		case MIRROR_LINE:
			return mirrorLine(step, homogeneous(operands.get(0)));
		case DILATE: {
			Point p = homogeneous(operands.get(0));
			ExactRational2D factor = parameter(step, 0);
			ExactRational2D complement = ExactRational2D.ONE.subtract(factor);
			return new Point(p.x.scale(factor).add(p.z.scale(
					complement.multiply(parameter(step, 1)))), p.y.scale(factor).add(
							p.z.scale(complement.multiply(parameter(step, 2)))), p.z);
		}
		case CIRCLE_RADIUS: {
			Point center = inhomogeneous(operands.get(0));
			ExactRational2D radius = parameter(step, 0);
			return new Circle(center,
					TrigonometricPolynomial2D.constant(radius.multiply(radius)),
					TrigonometricPolynomial2D.constant(ExactRational2D.ONE), false);
		}
		case CIRCLE_THROUGH: {
			Point center = inhomogeneous(operands.get(0));
			Point through = inhomogeneous(operands.get(1));
			TrigonometricPolynomial2D dx = through.x.multiply(center.z)
					.subtract(center.x.multiply(through.z));
			TrigonometricPolynomial2D dy = through.y.multiply(center.z)
					.subtract(center.y.multiply(through.z));
			TrigonometricPolynomial2D weight = through.z.multiply(center.z);
			return new Circle(center, dx.multiply(dx).add(dy.multiply(dy)),
					weight.multiply(weight), true);
		}
		case LINE_CIRCLE:
			return new Pair(line(operands.get(0)), circle(operands.get(1)));
		default:
			// Square roots, vectors, scalars and metric inputs.
			return null;
		}
	}

	/** GeoPoint.rotate(phi, Q) with the evaluator's own cosine and sine. */
	private Point rotateAbout(Step step, Point p) {
		ExactRational2D cosine = parameter(step, 0);
		ExactRational2D sine = parameter(step, 1);
		TrigonometricPolynomial2D qx = p.z.scale(parameter(step, 2));
		TrigonometricPolynomial2D qy = p.z.scale(parameter(step, 3));
		TrigonometricPolynomial2D dx = p.x.subtract(qx);
		return new Point(dx.scale(cosine).add(qy.subtract(p.y).scale(sine)).add(qx),
				dx.scale(sine).add(p.y.subtract(qy).scale(cosine)).add(qy), p.z);
	}

	/** GeoPoint.mirror(GeoLineND) with the evaluator's captured branch and angle. */
	private Point mirrorLine(Step step, Point p) {
		TrigonometricPolynomial2D offset = p.z.negate().scale(parameter(step, 2));
		boolean horizontalOffset = step.getParameter(3) == 1;
		TrigonometricPolynomial2D qx = horizontalOffset
				? offset.scale(ExactRational2D.ONE.divide(parameter(step, 0)))
				: TrigonometricPolynomial2D.ZERO;
		TrigonometricPolynomial2D qy = horizontalOffset ? TrigonometricPolynomial2D.ZERO
				: offset.scale(ExactRational2D.ONE.divide(parameter(step, 1)));
		ExactRational2D cosine = parameter(step, 4);
		ExactRational2D sine = parameter(step, 5);
		TrigonometricPolynomial2D u = p.x.subtract(qx);
		TrigonometricPolynomial2D w = p.y.subtract(qy);
		return new Point(u.scale(cosine).add(w.scale(sine)).add(qx),
				u.scale(sine).subtract(w.scale(cosine)).add(qy), p.z);
	}

	/** Inhomogeneous line formula of GeoVec3D.lineThroughPoints, scaled by the weights. */
	private static Line join(Point p, Point q) {
		return new Line(p.y.multiply(q.z).subtract(q.y.multiply(p.z)),
				q.x.multiply(p.z).subtract(p.x.multiply(q.z)),
				p.x.multiply(q.y).subtract(p.y.multiply(q.x)));
	}

	private TrigonometricPolynomial2D predicate(ConstructionBoxModel2D.Target target) {
		if (target.index != ConstructionBoxModel2D.STEP_PREDICATE) {
			return null;
		}
		Step step = program.getSteps().get(target.step - 1);
		Object value = nodes[target.step];
		switch (target.predicate) {
		case DISCRIMINANT:
			if (step.getOperation() != Operation.INTERSECTION_OUTPUT) {
				return null;
			}
			Object pair = nodes[step.getOperands().get(0).getNode()];
			return pair instanceof Pair ? ((Pair) pair).discriminant() : null;
		case DIVISOR:
			return value instanceof Point ? ((Point) value).z : null;
		case RADIUS:
			return value instanceof Circle && ((Circle) value).through
					? ((Circle) value).radiusNumerator : null;
		case DIRECTION:
			return value instanceof Line ? ((Line) value).normSquared() : null;
		default:
			return null;
		}
	}

	private ExactRational2D parameter(Step step, int index) {
		return ExactRational2D.ofBinary64(step.getParameter(index));
	}

	/** A point operand in the homogeneous form its consumer reads. */
	private Point homogeneous(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			return point(operand.getNode());
		}
		return new Point(constant(operand, 0), constant(operand, 1), constant(operand, 2));
	}

	/** A point operand in the inhomogeneous form its consumer reads. */
	private Point inhomogeneous(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			return point(operand.getNode());
		}
		return Point.unit(constant(operand, 3), constant(operand, 4));
	}

	private Point point(int node) {
		Object value = nodes[node];
		if (!(value instanceof Point)) {
			throw new NotFirstLevel();
		}
		return (Point) value;
	}

	private Line line(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			Object value = nodes[operand.getNode()];
			if (!(value instanceof Line)) {
				throw new NotFirstLevel();
			}
			return (Line) value;
		}
		return new Line(constant(operand, 0), constant(operand, 1), constant(operand, 2));
	}

	/** A circle node, or a constant circle of matrix (1, 1, A2, 0, A4, A5). */
	private Circle circle(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			Object value = nodes[operand.getNode()];
			if (!(value instanceof Circle)) {
				throw new NotFirstLevel();
			}
			return (Circle) value;
		}
		ExactRational2D a2 = ExactRational2D.ofBinary64(operand.getValue(0));
		ExactRational2D a4 = ExactRational2D.ofBinary64(operand.getValue(1));
		ExactRational2D a5 = ExactRational2D.ofBinary64(operand.getValue(2));
		return new Circle(Point.unit(TrigonometricPolynomial2D.constant(a4.negate()),
				TrigonometricPolynomial2D.constant(a5.negate())),
				TrigonometricPolynomial2D.constant(a4.multiply(a4).add(a5.multiply(a5))
						.subtract(a2)),
				TrigonometricPolynomial2D.constant(ExactRational2D.ONE), false);
	}

	private static TrigonometricPolynomial2D constant(Operand operand, int index) {
		return TrigonometricPolynomial2D.constant(
				ExactRational2D.ofBinary64(operand.getValue(index)));
	}

	/**
	 * Point with polynomial homogeneous coordinates; its inhomogeneous coordinates are
	 * {@code x / z} and {@code y / z}.
	 */
	private static final class Point {
		private final TrigonometricPolynomial2D x;
		private final TrigonometricPolynomial2D y;
		private final TrigonometricPolynomial2D z;

		private Point(TrigonometricPolynomial2D x, TrigonometricPolynomial2D y,
				TrigonometricPolynomial2D z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		static Point unit(TrigonometricPolynomial2D x, TrigonometricPolynomial2D y) {
			return new Point(x, y, TrigonometricPolynomial2D.constant(ExactRational2D.ONE));
		}
	}

	/** Line coefficients, a polynomial multiple of the evaluated line. */
	private static final class Line {
		private final TrigonometricPolynomial2D a;
		private final TrigonometricPolynomial2D b;
		private final TrigonometricPolynomial2D c;

		private Line(TrigonometricPolynomial2D a, TrigonometricPolynomial2D b,
				TrigonometricPolynomial2D c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		TrigonometricPolynomial2D normSquared() {
			return a.multiply(a).add(b.multiply(b));
		}
	}

	/**
	 * Circle with a polynomial centre and squared radius {@code numerator / weight}; only a
	 * circle through a point has a radius predicate.
	 */
	private static final class Circle {
		private final Point center;
		private final TrigonometricPolynomial2D radiusNumerator;
		private final TrigonometricPolynomial2D radiusWeight;
		private final boolean through;

		private Circle(Point center, TrigonometricPolynomial2D radiusNumerator,
				TrigonometricPolynomial2D radiusWeight, boolean through) {
			this.center = center;
			this.radiusNumerator = radiusNumerator;
			this.radiusWeight = radiusWeight;
			this.through = through;
		}
	}

	/** Line and circle of an intersection. */
	private static final class Pair {
		private final Line line;
		private final Circle circle;

		private Pair(Line line, Circle circle) {
			this.line = line;
			this.circle = circle;
		}

		/**
		 * The discriminant {@code r^2 (a^2 + b^2) - (a mx + b my + c)^2} multiplied by the
		 * squares of the line scale and of the centre weight and by the radius weight, a
		 * square: {@code N z^2 (a^2 + b^2) - W (a X + b Y + c z)^2}.
		 */
		TrigonometricPolynomial2D discriminant() {
			TrigonometricPolynomial2D weight = circle.center.z;
			TrigonometricPolynomial2D distance = line.a.multiply(circle.center.x)
					.add(line.b.multiply(circle.center.y)).add(line.c.multiply(weight));
			return circle.radiusNumerator.multiply(weight).multiply(weight)
					.multiply(line.normSquared())
					.subtract(circle.radiusWeight.multiply(distance).multiply(distance));
		}
	}

	/** Internal refusal of a predicate that is not first-level. */
	private static final class NotFirstLevel extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private NotFirstLevel() {
			super(null, null, false, false);
		}
	}
}
