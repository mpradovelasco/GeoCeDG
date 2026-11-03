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
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticAddress2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;

/** Independent chart proofs; neither an R6 search result nor an H1 creation proof. */
public final class LocalContinuationEvidence2D {

	/** A deterministic represented-coordinate inverse inside a proven semantic chart. */
	public static final class Proposal {
		private final double parameter;
		private final long lift;
		private final String method;
		private final double lower;
		private final double upper;
		private final int coordinate;

		private Proposal(double parameter, long lift, String method, double lower,
				double upper, int coordinate) {
			this.parameter = parameter;
			this.lift = lift;
			this.method = method;
			this.lower = lower;
			this.upper = upper;
			this.coordinate = coordinate;
		}

		public double getParameter() { return parameter; }
		public long getLift() { return lift; }
		public String getMethod() { return method; }
		public double getLower() { return lower; }
		public double getUpper() { return upper; }
		public int getCoordinate() { return coordinate; }
	}

	/** Charges each proof pass before it runs. */
	public interface Work {
		void charge();
	}

	private LocalContinuationEvidence2D() {
	}

	/** Returns no permission when neither independent sufficient chart is established. */
	public static Proposal establish(LocusDefinition2D definition,
			LocusSemanticAddress2D current, ContinuousComponent component,
			double x, double y, LocusPointInteractionPolicy2D policy, int fixedCoordinate,
			Work work) {
		if (!(definition.getEvaluatorCapability() instanceof ReconstructibleLocusEvaluator2D)) {
			return null;
		}
		ReconstructibleLocusEvaluator2D evaluator =
				(ReconstructibleLocusEvaluator2D) definition.getEvaluatorCapability();
		work.charge();
		CertifiedConstructionProgram2D program = evaluator.captureCertifiedProgramV3().orElse(null);
		if (program == null || !evaluator.isReconstructionFaithful()) {
			return null;
		}
		Proposal affine = affineCircle(definition, current, program, x, y, policy, work);
		if (affine != null) {
			return affine;
		}
		return monotone(definition, current, component, x, y, policy, fixedCoordinate, work);
	}

	private static Proposal affineCircle(LocusDefinition2D definition,
			LocusSemanticAddress2D current, CertifiedConstructionProgram2D program,
			double x, double y, LocusPointInteractionPolicy2D policy, Work work) {
		if (program.getDriver() != CertifiedConstructionProgram2D.Driver.CIRCLE
				|| !definition.getProvider().isPeriodic()
				|| !definition.getBranch(current.getBranchKey()).getExistenceStructure()
						.isClosedPeriodic()) {
			return null;
		}
		try {
			Affine[] nodes = new Affine[program.getSteps().size() + 1];
			nodes[0] = Affine.identity();
			int index = 1;
			for (Step step : program.getSteps()) {
				work.charge();
				nodes[index++] = apply(step, nodes);
			}
			Affine output = nodes[nodes.length - 1];
			if (output == null) {
				return null;
			}
			// Compose the proven affine output with the driver's exact trigonometric form.
			SplineOutwardInterval2D[] cosine = new SplineOutwardInterval2D[] {
					p(program.getDriverConstant(0)).multiply(p(program.getDriverConstant(2))),
					p(program.getDriverConstant(0)).multiply(p(program.getDriverConstant(3)))};
			SplineOutwardInterval2D[] sine = new SplineOutwardInterval2D[] {
					p(program.getDriverConstant(1)).multiply(p(program.getDriverConstant(4))),
					p(program.getDriverConstant(1)).multiply(p(program.getDriverConstant(5)))};
			SplineOutwardInterval2D a = output.linear(0, cosine);
			SplineOutwardInterval2D b = output.linear(0, sine);
			SplineOutwardInterval2D c = output.linear(1, cosine);
			SplineOutwardInterval2D d = output.linear(1, sine);
			SplineOutwardInterval2D determinant = a.multiply(d).subtract(b.multiply(c));
			if (determinant.containsZero()) {
				return null;
			}
			SplineOutwardInterval2D[] centre = {p(program.getDriverConstant(6)),
					p(program.getDriverConstant(7))};
			SplineOutwardInterval2D dx = p(x).subtract(output.linear(0, centre).add(output.v[2]));
			SplineOutwardInterval2D dy = p(y).subtract(output.linear(1, centre).add(output.v[5]));
			SplineOutwardInterval2D wx = d.multiply(dx).subtract(b.multiply(dy)).divide(determinant);
			SplineOutwardInterval2D wy = a.multiply(dy).subtract(c.multiply(dx)).divide(determinant);
			SplineOutwardInterval2D old = p(current.getCanonicalParameter());
			SplineOutwardInterval2D dot = wx.multiply(ConstructionIntervalModel2D.cosine(old))
					.add(wy.multiply(ConstructionIntervalModel2D.sine(old)));
			if (!(dot.lower > 0)) {
				return null;
			}
			// Class-v3 structural-totality/v1 (§10.8) owns circle seam correspondence;
			// this is not the period=0 exact-real certificate of the older interval model.
			double nominal = StrictMath.atan2(wy.midpoint(), wx.midpoint());
			SplineOutwardInterval2D angle = angleEnclosure(wx, wy, nominal);
			if (!angle.contains(p(nominal)) || angle.width() > policy.getParameterTolerance() / 2) {
				return null;
			}
			double initialCanonical = definition.getProvider().canonicalize(nominal);
			double canonical = definition.getProvider().canonicalize(initialCanonical);
			LocusInterval2D domain = definition.getProvider().getDeclaredDomain();
			double period = domain.getUpper() - domain.getLower();
			work.charge();
			// Bound the complete principal-to-canonical operation, including its first
			// rounding and any legitimate seam reduction, rather than only a second pass.
			SplineOutwardInterval2D normalization = p(canonical).subtract(p(nominal));
			SplineOutwardInterval2D exactPeriod = new SplineOutwardInterval2D(
					2 * Math.PI, 2 * Math.nextUp(Math.PI));
			if (canonical - nominal > period / 2) {
				normalization = normalization.subtract(exactPeriod);
			} else if (canonical - nominal < -period / 2) {
				normalization = normalization.add(exactPeriod);
			}
			SplineOutwardInterval2D selected = p(canonical);
			SplineOutwardInterval2D selectedDot = ConstructionIntervalModel2D.cosine(selected)
					.multiply(ConstructionIntervalModel2D.cosine(old))
					.add(ConstructionIntervalModel2D.sine(selected)
							.multiply(ConstructionIntervalModel2D.sine(old)));
			if (ConstructionIntervalModel2D.abs(normalization).upper
					> policy.getParameterTolerance() / 2 || !(selectedDot.lower > 0)) {
				return null;
			}
			double delta = canonical - current.getCanonicalParameter();
			long lift = current.getPeriodicLift();
			if (delta > period / 2) {
				lift = Math.subtractExact(lift, 1);
			} else if (delta < -period / 2) {
				lift = Math.addExact(lift, 1);
			}
			return new Proposal(canonical, lift, "STRUCTURAL_AFFINE_CIRCLE_CHART",
					current.getCanonicalParameter() - Math.PI / 2,
					current.getCanonicalParameter() + Math.PI / 2, -1);
		} catch (ArithmeticException | IllegalArgumentException exception) {
			return null;
		}
	}

	private static SplineOutwardInterval2D angleEnclosure(SplineOutwardInterval2D x,
			SplineOutwardInterval2D y, double old) {
		SplineOutwardInterval2D result = null;
		SplineOutwardInterval2D period = new SplineOutwardInterval2D(
				2 * Math.PI, 2 * Math.nextUp(Math.PI));
		for (double a : new double[] {x.lower, x.upper}) {
			for (double b : new double[] {y.lower, y.upper}) {
				double value = Math.atan2(b, a);
				SplineOutwardInterval2D corner = atan2AccuracyEnvelope(value);
				if (value - old > Math.PI) {
					corner = corner.subtract(period);
				} else if (value - old < -Math.PI) {
					corner = corner.add(period);
				}
				result = result == null ? corner : result.hull(corner);
			}
		}
		return result;
	}

	static SplineOutwardInterval2D atan2AccuracyEnvelope(double value) {
		// Math's pointwise two-ulp bound uses the exact result's spacing. Four
		// directed steps cover the finer mesh across a normal binade boundary;
		// three steps from the returned value need not enclose that real result.
		return new SplineOutwardInterval2D(
				Math.nextDown(Math.nextDown(Math.nextDown(Math.nextDown(value)))),
				Math.nextUp(Math.nextUp(Math.nextUp(Math.nextUp(value)))));
	}

	private static Affine apply(Step step, Affine[] nodes) {
		List<Operand> operands = step.getOperands();
		switch (step.getOperation()) {
		case VECTOR_POINTS:
			return operand(operands.get(1), nodes).add(operand(operands.get(0), nodes).scale(-1));
		case POINT_PLUS_VECTOR:
			return operand(operands.get(0), nodes)
					.add(operand(operands.get(1), nodes).scale(step.getParameter(0)));
		case SCALE:
			Operand scalar = operands.get(0);
			return scalar.getKind() == OperandKind.SCALAR
					? operand(operands.get(1), nodes).scale(scalar.getValue(0)) : null;
		case MIDPOINT:
			return operand(operands.get(0), nodes).add(operand(operands.get(1), nodes)).scale(0.5);
		case TRANSLATE:
		case EXPRESSION_TRANSLATE:
			return operand(operands.get(0), nodes)
					.add(Affine.constant(step.getParameter(0), step.getParameter(1)));
		case DILATE:
			return operand(operands.get(0), nodes).scale(step.getParameter(0))
					.add(Affine.constant(step.getParameter(1), step.getParameter(2))
							.scale(p(1).subtract(p(step.getParameter(0)))));
		default:
			return null;
		}
	}

	private static Affine operand(Operand operand, Affine[] nodes) {
		if (operand.getKind() == OperandKind.NODE) {
			Affine result = nodes[operand.getNode()];
			if (result == null) {
				throw new IllegalArgumentException("No structural affine expression");
			}
			return result;
		}
		if (operand.getKind() == OperandKind.POINT) {
			return Affine.constant(operand.getValue(3), operand.getValue(4));
		}
		if (operand.getKind() == OperandKind.VECTOR) {
			return Affine.constant(operand.getValue(0), operand.getValue(1));
		}
		throw new IllegalArgumentException("Operand is outside the affine chart table");
	}

	private static Proposal monotone(LocusDefinition2D definition,
			LocusSemanticAddress2D current, ContinuousComponent component, double x,
			double y, LocusPointInteractionPolicy2D policy, int fixedCoordinate, Work work) {
		work.charge();
		CertifiedIntervalCurveModel2D model = CertifiedIntervalCurveModel2D.capture(
				definition, current.getBranchKey());
		if (model == null) {
			return null;
		}
		double old = current.getCanonicalParameter();
		LocusInterval2D piece = null;
		for (LocusInterval2D candidate : component.getCanonicalPieces()) {
			if (candidate.contains(old, 0)) {
				if (piece != null) {
					return null;
				}
				piece = candidate;
			}
		}
		if (piece == null) {
			return null;
		}
		double radius = (piece.getUpper() - piece.getLower()) / 8;
		for (int attempt = 0; attempt < policy.getWorkBudget().getMaximumRefinementIterations();
				attempt++, radius /= 2) {
			double lower = Math.max(piece.getLower(), old - radius);
			double upper = Math.min(piece.getUpper(), old + radius);
			if (!piece.isLowerClosed() && lower == piece.getLower()) {
				lower = Math.nextUp(lower);
			}
			if (!piece.isUpperClosed() && upper == piece.getUpper()) {
				upper = Math.nextDown(upper);
			}
			if (!(lower < upper)) {
				return null;
			}
			try {
				work.charge();
				SplineOutwardInterval2D box = new SplineOutwardInterval2D(lower, upper);
				if (!model.isSmooth(box)) {
					continue;
				}
				SplineOutwardInterval2D[] derivative = model.evaluate(box, true);
				double xMagnitude = minimumMagnitude(derivative[0]);
				double yMagnitude = minimumMagnitude(derivative[1]);
				int coordinate = fixedCoordinate >= 0 ? fixedCoordinate
						: Math.max(xMagnitude, yMagnitude) == 0 ? -1
								: xMagnitude >= yMagnitude ? 0 : 1;
				if (coordinate < 0) {
					continue;
				}
				if (derivative[coordinate].containsZero()) {
					continue;
				}
				double parameter = invertCoordinate(model, coordinate, coordinate == 0 ? x : y,
						lower, upper, derivative[coordinate], policy, work);
				if (!Double.isFinite(parameter)) {
					return null;
				}
				double canonical = definition.getProvider().canonicalize(parameter);
				if (Math.abs(canonical - parameter) > policy.getParameterTolerance() / 2
						|| canonical < lower || canonical > upper) {
					return null;
				}
				return new Proposal(canonical, current.getPeriodicLift(),
						"CERTIFIED_MONOTONE_COORDINATE_CHART", lower, upper, coordinate);
			} catch (ArithmeticException | IllegalArgumentException exception) {
				// A smaller complete chart may decide all predicates; no sampling substitutes for it.
			}
		}
		return null;
	}

	private static double invertCoordinate(CertifiedIntervalCurveModel2D model,
			int coordinate, double target, double lower, double upper,
			SplineOutwardInterval2D derivative,
			LocusPointInteractionPolicy2D policy, Work work) {
		boolean increasing = derivative.lower > 0;
		work.charge();
		SplineOutwardInterval2D left = model.evaluate(p(lower), false)[coordinate];
		work.charge();
		SplineOutwardInterval2D right = model.evaluate(p(upper), false)[coordinate];
		// Strict interior inclusion establishes a root by continuity and the monotone chart.
		if (!(increasing ? left.upper < target && target < right.lower
				: right.upper < target && target < left.lower)) {
			return Double.NaN;
		}
		for (int iteration = 0;
				iteration < policy.getWorkBudget().getMaximumRefinementIterations(); iteration++) {
			double middle = lower + (upper - lower) / 2;
			if (upper - lower <= policy.getParameterTolerance()) {
				return middle;
			}
			if (middle == lower || middle == upper) {
				return Double.NaN;
			}
			work.charge();
			SplineOutwardInterval2D value = model.evaluate(p(middle), false)[coordinate];
			if (value.lower <= target && target <= value.upper) {
				// The mean value theorem bounds the root parameter, not just world residual.
				SplineOutwardInterval2D error = p(target).subtract(value).divide(derivative);
				SplineOutwardInterval2D root = p(middle).add(error).intersection(
						new SplineOutwardInterval2D(lower, upper));
				if (root == null || root.lower == lower && root.upper == upper) {
					return Double.NaN;
				}
				lower = root.lower;
				upper = root.upper;
				continue;
			}
			if (increasing ? value.upper < target : value.lower > target) {
				lower = middle;
			} else {
				upper = middle;
			}
		}
		return Double.NaN;
	}

	private static double minimumMagnitude(SplineOutwardInterval2D value) {
		return value.containsZero() ? 0 : Math.min(Math.abs(value.lower), Math.abs(value.upper));
	}

	private static SplineOutwardInterval2D p(double value) {
		return SplineOutwardInterval2D.point(value);
	}

	private static final class Affine {
		private final SplineOutwardInterval2D[] v;

		private Affine(SplineOutwardInterval2D[] values) { v = values; }
		private static Affine identity() {
			return new Affine(new SplineOutwardInterval2D[] {p(1), p(0), p(0), p(0), p(1), p(0)});
		}
		private static Affine constant(double x, double y) {
			return new Affine(new SplineOutwardInterval2D[] {p(0), p(0), p(x), p(0), p(0), p(y)});
		}
		private Affine add(Affine other) {
			SplineOutwardInterval2D[] result = new SplineOutwardInterval2D[6];
			for (int index = 0; index < result.length; index++) {
				result[index] = v[index].add(other.v[index]);
			}
			return new Affine(result);
		}
		private Affine scale(double factor) {
			return scale(p(factor));
		}
		private Affine scale(SplineOutwardInterval2D factor) {
			SplineOutwardInterval2D[] result = new SplineOutwardInterval2D[6];
			for (int index = 0; index < result.length; index++) {
				result[index] = v[index].multiply(factor);
			}
			return new Affine(result);
		}
		private SplineOutwardInterval2D linear(int coordinate, SplineOutwardInterval2D[] input) {
			int offset = coordinate * 3;
			return v[offset].multiply(input[0]).add(v[offset + 1].multiply(input[1]));
		}
	}
}
