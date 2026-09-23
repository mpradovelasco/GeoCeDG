/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.List;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.SemanticGeneratorDescriptor1D;

/**
 * Certified construction interval model of class v1
 * ({@code locus-v2-certified-construction-model.md}). It replays the exact-real
 * GeoGebra formula sequence of one captured program with outward intervals and
 * first derivatives. Every GeoGebra branch predicate is decided uniformly on the
 * box or the box is refused with {@link ArithmeticException}; it is then
 * unresolved and never excluded or certified.
 */
final class ConstructionIntervalModel2D implements CertifiedIntervalCurveModel2D {
	/** {@code Kernel.STANDARD_PRECISION}, the point and line predicate threshold. */
	static final double EPSILON = 1E-8;
	private static final double NORMALIZATION_MINIMUM = 1E-2;
	private static final double NORMALIZATION_MAXIMUM = 10E6;
	private static final int NORMALIZATION_LIMIT = 2_048;
	private static final double TRIGONOMETRIC_LIMIT = 1_000;
	private static final double HALF_PI_LOWER = Math.PI / 2;
	private static final double HALF_PI_UPPER = Math.nextUp(Math.PI) / 2;

	private final LocusDefinition2D definition;
	private final CertifiedConstructionProgram2D program;
	private final List<LocusSimilarityTransform2D> transforms;
	private final double[] knots;

	private ConstructionIntervalModel2D(LocusDefinition2D definition,
			CertifiedConstructionProgram2D program,
			List<LocusSimilarityTransform2D> transforms) {
		this.definition = definition;
		this.program = program;
		this.transforms = transforms;
		LocusInterval2D domain = program.getDomain();
		int spans = program.getDriver() == CertifiedConstructionProgram2D.Driver.CIRCLE
				? 8 : program.getDriver()
						== CertifiedConstructionProgram2D.Driver.SCALAR_AFFINE ? 1 : 4;
		knots = new double[spans + 1];
		knots[0] = domain.getLower();
		knots[spans] = domain.getUpper();
		for (int index = 1; index < spans; index++) {
			knots[index] = domain.getLower()
					+ (domain.getUpper() - domain.getLower()) * index / spans;
		}
	}

	/**
	 * @return model of the reconstructible root of the chain, or {@code null}
	 *         when its slice is outside class v1
	 */
	static ConstructionIntervalModel2D capture(LocusDefinition2D definition,
			SplineIntervalModel2D.SimilarityChain chain, String branch) {
		LocusDefinition2D root = chain.getRoot();
		if (!SemanticGeneratorDescriptor1D.OUTPUT_BRANCH_KEY.equals(branch)
				|| root.getBranch(branch) == null
				|| !(root.getEvaluatorCapability()
						instanceof ReconstructibleLocusEvaluator2D)) {
			return null;
		}
		return ((ReconstructibleLocusEvaluator2D) root.getEvaluatorCapability())
				.captureCertifiedProgram().map(program -> new ConstructionIntervalModel2D(
						definition, program, chain.getTransforms())).orElse(null);
	}

	/** @return the captured program; certificate material only */
	CertifiedConstructionProgram2D getProgram() {
		return program;
	}

	@Override
	public SplineOutwardInterval2D[] evaluate(SplineOutwardInterval2D parameter,
			boolean derivative) {
		if (!isSmooth(parameter)) {
			throw new ArithmeticException("Parameter box is outside the driver domain");
		}
		List<Step> steps = program.getSteps();
		Object[] nodes = new Object[steps.size() + 1];
		nodes[0] = driver(Jet.variable(parameter));
		for (int index = 0; index < steps.size(); index++) {
			nodes[index + 1] = apply(steps.get(index), nodes);
		}
		PointJet output = (PointJet) nodes[steps.size()];
		SplineOutwardInterval2D[] root = derivative
				? new SplineOutwardInterval2D[] {output.inhomX.derivative,
						output.inhomY.derivative}
				: new SplineOutwardInterval2D[] {output.inhomX.value, output.inhomY.value};
		return SplineIntervalModel2D.applySimilarities(transforms, root, derivative);
	}

	@Override
	public boolean isSmooth(SplineOutwardInterval2D parameter) {
		LocusInterval2D domain = program.getDomain();
		return parameter.lower >= domain.getLower()
				&& parameter.upper <= domain.getUpper();
	}

	@Override
	public double period(LocusInterval2D component) {
		// The circle-point canonical domain is not exactly closed under real
		// cos/sin; no periodic identification is claimed (model contract section 7).
		return 0;
	}

	@Override
	public double canonical(double parameter) {
		return definition.getProvider().canonicalize(parameter);
	}

	@Override
	public double[] getKnots() {
		return knots.clone();
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
					Jet.constant(program.getDriverConstant(1))
							.subtract(t.multiply(Jet.constant(program.getDriverConstant(2)))));
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

	private static Object apply(Step step, Object[] nodes) {
		List<Operand> operands = step.getOperands();
		switch (step.getOperation()) {
		case JOIN:
			return join(point(operands.get(0), nodes), point(operands.get(1), nodes));
		case PARALLEL:
			return parallel(point(operands.get(0), nodes), line(operands.get(1), nodes));
		case PERPENDICULAR:
			return perpendicular(point(operands.get(0), nodes),
					line(operands.get(1), nodes));
		case MEET:
			return meet(line(operands.get(0), nodes), line(operands.get(1), nodes));
		case MIDPOINT:
			PointJet first = point(operands.get(0), nodes);
			PointJet second = point(operands.get(1), nodes);
			return PointJet.unit(first.inhomX.add(second.inhomX).half(),
					first.inhomY.add(second.inhomY).half());
		default:
			return transform(step, point(operands.get(0), nodes));
		}
	}

	/** GeoVec3D.lineThroughPoints for two finite points. */
	private static LineJet join(PointJet p, PointJet q) {
		return LineJet.defined(p.inhomY.subtract(q.inhomY),
				q.inhomX.subtract(p.inhomX),
				p.inhomX.multiply(q.inhomY).subtract(p.inhomY.multiply(q.inhomX)));
	}

	/** AlgoLinePointLine: cross(P, (l.y, -l.x, 0)). */
	private static LineJet parallel(PointJet p, LineJet l) {
		return LineJet.defined(p.z.multiply(l.a), p.z.multiply(l.b),
				p.x.multiply(l.a).add(p.y.multiply(l.b)).negate());
	}

	/** AlgoOrthoLinePointLine: cross(P, (l.x, l.y, 0)), then normalization. */
	private static LineJet perpendicular(PointJet p, LineJet l) {
		Jet[] coefficients = {p.z.multiply(l.b).negate(), p.z.multiply(l.a),
				p.x.multiply(l.b).subtract(p.y.multiply(l.a))};
		if (uniformlyTrue(allBelow(coefficients, EPSILON, true),
				allBelow(coefficients, EPSILON, false))) {
			// All three isZero: GeoGebra returns the coefficients unscaled.
			return LineJet.defined(coefficients[0], coefficients[1], coefficients[2]);
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
		return LineJet.defined(coefficients[0], coefficients[1], coefficients[2]);
	}

	/** AlgoIntersectLines: cross(g, h), then GeoPoint.updateCoords. */
	private static PointJet meet(LineJet g, LineJet h) {
		return PointJet.normalized(g.b.multiply(h.c).subtract(g.c.multiply(h.b)),
				g.c.multiply(h.a).subtract(g.a.multiply(h.c)),
				g.a.multiply(h.b).subtract(g.b.multiply(h.a)));
	}

	private static PointJet transform(Step step, PointJet p) {
		switch (step.getOperation()) {
		case TRANSLATE:
			return PointJet.normalized(
					p.x.add(Jet.constant(step.getParameter(0)).multiply(p.z)),
					p.y.add(Jet.constant(step.getParameter(1)).multiply(p.z)), p.z);
		case ROTATE:
			Jet cosine = Jet.constant(step.getParameter(0));
			Jet sine = Jet.constant(step.getParameter(1));
			return PointJet.normalized(
					p.x.multiply(cosine).subtract(p.y.multiply(sine)),
					p.x.multiply(sine).add(p.y.multiply(cosine)), p.z);
		case ROTATE_ABOUT:
			return rotateAbout(step, p);
		case MIRROR_POINT:
			Jet two = Jet.constant(2);
			return PointJet.normalized(
					two.multiply(p.z).multiply(Jet.constant(step.getParameter(0)))
							.subtract(p.x),
					two.multiply(p.z).multiply(Jet.constant(step.getParameter(1)))
							.subtract(p.y), p.z);
		case MIRROR_LINE:
			return mirrorLine(step, p);
		case DILATE:
			Jet factor = Jet.constant(step.getParameter(0));
			Jet complement = Jet.constant(1).subtract(factor);
			return PointJet.normalized(
					factor.multiply(p.x).add(complement.multiply(
							Jet.constant(step.getParameter(1))).multiply(p.z)),
					factor.multiply(p.y).add(complement.multiply(
							Jet.constant(step.getParameter(2))).multiply(p.z)), p.z);
		default:
			throw new ArithmeticException("Unsupported operation");
		}
	}

	/** GeoPoint.rotate(phi, Q) with the evaluator's own cosine and sine. */
	private static PointJet rotateAbout(Step step, PointJet p) {
		Jet cosine = Jet.constant(step.getParameter(0));
		Jet sine = Jet.constant(step.getParameter(1));
		Jet qx = p.z.multiply(Jet.constant(step.getParameter(2)));
		Jet qy = p.z.multiply(Jet.constant(step.getParameter(3)));
		Jet dx = p.x.subtract(qx);
		return PointJet.normalized(
				dx.multiply(cosine).add(qy.subtract(p.y).multiply(sine)).add(qx),
				dx.multiply(sine).add(p.y.subtract(qy).multiply(cosine)).add(qy), p.z);
	}

	/** GeoPoint.mirror(GeoLineND) with the evaluator's captured branch and angle. */
	private static PointJet mirrorLine(Step step, PointJet p) {
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
		return PointJet.normalized(u.multiply(cosine).add(w.multiply(sine)).add(qx),
				u.multiply(sine).subtract(w.multiply(cosine)).add(qy), p.z);
	}

	private static PointJet point(Operand operand, Object[] nodes) {
		if (operand.getKind() == OperandKind.NODE) {
			return (PointJet) nodes[operand.getNode()];
		}
		return PointJet.constant(operand.getValue(0), operand.getValue(1),
				operand.getValue(2), operand.getValue(3), operand.getValue(4));
	}

	private static LineJet line(Operand operand, Object[] nodes) {
		if (operand.getKind() == OperandKind.NODE) {
			return (LineJet) nodes[operand.getNode()];
		}
		return LineJet.defined(Jet.constant(operand.getValue(0)),
				Jet.constant(operand.getValue(1)), Jet.constant(operand.getValue(2)));
	}

	/**
	 * @return whether every magnitude is below the threshold on the whole box
	 *         ({@code everywhere}), or could be below it somewhere
	 */
	private static boolean allBelow(Jet[] values, double threshold,
			boolean everywhere) {
		boolean all = true;
		for (Jet value : values) {
			SplineOutwardInterval2D magnitude = abs(value.value);
			all &= everywhere ? magnitude.upper < threshold : magnitude.lower < threshold;
		}
		return all;
	}

	/**
	 * @return whether every magnitude is above the threshold on the whole box
	 *         ({@code everywhere}), or could be above it somewhere
	 */
	private static boolean allAbove(Jet[] values, double threshold,
			boolean everywhere) {
		boolean all = true;
		for (Jet value : values) {
			SplineOutwardInterval2D magnitude = abs(value.value);
			all &= everywhere ? magnitude.lower > threshold : magnitude.upper > threshold;
		}
		return all;
	}

	/**
	 * @param certain the condition holds for every parameter of the box
	 * @param possible the condition may hold for some parameter of the box
	 * @return the uniform decision
	 */
	private static boolean uniformlyTrue(boolean certain, boolean possible) {
		if (certain) {
			return true;
		}
		if (!possible) {
			return false;
		}
		throw new ArithmeticException("GeoGebra predicate is not uniform on the box");
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

	static SplineOutwardInterval2D abs(SplineOutwardInterval2D value) {
		if (value.lower >= 0) {
			return value;
		}
		if (value.upper <= 0) {
			return value.negate();
		}
		return new SplineOutwardInterval2D(0, Math.max(-value.lower, value.upper));
	}

	/** @return rigorous outward enclosure of cosine over the interval */
	static SplineOutwardInterval2D cosine(SplineOutwardInterval2D argument) {
		return trigonometric(argument, false);
	}

	/** @return rigorous outward enclosure of sine over the interval */
	static SplineOutwardInterval2D sine(SplineOutwardInterval2D argument) {
		return trigonometric(argument, true);
	}

	private static SplineOutwardInterval2D trigonometric(
			SplineOutwardInterval2D argument, boolean sine) {
		if (Math.abs(argument.lower) > TRIGONOMETRIC_LIMIT
				|| Math.abs(argument.upper) > TRIGONOMETRIC_LIMIT) {
			throw new ArithmeticException("Trigonometric argument outside the bound");
		}
		double first = sine ? StrictMath.sin(argument.lower)
				: StrictMath.cos(argument.lower);
		double second = sine ? StrictMath.sin(argument.upper)
				: StrictMath.cos(argument.upper);
		// StrictMath is within one ulp; two adjacent values widen it outward.
		double low = Math.nextDown(Math.nextDown(Math.min(first, second)));
		double high = Math.nextUp(Math.nextUp(Math.max(first, second)));
		long start = (long) Math.floor(argument.lower / HALF_PI_LOWER) - 2;
		long end = (long) Math.ceil(argument.upper / HALF_PI_LOWER) + 2;
		for (long multiple = start; multiple <= end; multiple++) {
			boolean odd = Math.floorMod(multiple, 2L) == 1;
			if (odd != sine) {
				continue;
			}
			// Outward enclosure of the critical point multiple * pi / 2.
			double lower = Math.nextDown(multiple
					* (multiple >= 0 ? HALF_PI_LOWER : HALF_PI_UPPER));
			double upper = Math.nextUp(multiple
					* (multiple >= 0 ? HALF_PI_UPPER : HALF_PI_LOWER));
			if (upper < argument.lower || lower > argument.upper) {
				continue;
			}
			long half = Math.floorDiv(sine ? multiple - 1 : multiple, 2L);
			double extreme = Math.floorMod(half, 2L) == 0 ? 1 : -1;
			low = Math.min(low, extreme);
			high = Math.max(high, extreme);
		}
		return new SplineOutwardInterval2D(Math.max(-1, low), Math.min(1, high));
	}

	/** Value and first derivative with respect to the driver parameter. */
	private static final class Jet {
		private final SplineOutwardInterval2D value;
		private final SplineOutwardInterval2D derivative;

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

		Jet cos() {
			return new Jet(cosine(value), sine(value).negate().multiply(derivative));
		}

		Jet sin() {
			return new Jet(sine(value), cosine(value).multiply(derivative));
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

		/** GeoPoint.setCoords followed by updateCoords, decided on the box. */
		static PointJet normalized(Jet x, Jet y, Jet z) {
			if (z.isUnitConstant()) {
				return unit(x, y);
			}
			SplineOutwardInterval2D size = abs(z.value);
			boolean finite = size.lower > EPSILON
					|| size.lower > Math.nextUp(abs(x.value).upper * EPSILON)
					|| size.lower > Math.nextUp(abs(y.value).upper * EPSILON);
			if (!finite) {
				throw new ArithmeticException("Point finiteness is not uniform");
			}
			if (z.value.upper < 0) {
				return normalized(x.negate(), y.negate(), z.negate());
			}
			if (!(z.value.lower > 0)) {
				throw new ArithmeticException("Homogeneous sign is not uniform");
			}
			return new PointJet(x, y, z, x.divide(z), y.divide(z));
		}
	}

	/** Line coefficients certified defined for GeoGebra on the whole box. */
	private static final class LineJet {
		private final Jet a;
		private final Jet b;
		private final Jet c;

		private LineJet(Jet a, Jet b, Jet c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		static LineJet defined(Jet a, Jet b, Jet c) {
			if (!(nonZero(a.value) || nonZero(b.value))) {
				throw new ArithmeticException("Line definedness is not uniform");
			}
			return new LineJet(a, b, c);
		}

		private static boolean nonZero(SplineOutwardInterval2D value) {
			return value.lower >= EPSILON || value.upper <= -EPSILON;
		}
	}
}
