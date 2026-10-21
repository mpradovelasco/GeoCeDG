/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.NodeKind;
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
 *
 * <p>Each evaluated predicate is addressed by its step, its kind and an index: {@code -1}
 * for a predicate of the step itself, {@code -2} for the band of an unlabelled second
 * output, and {@code 2 k + side} for the membership of raw root {@code k} of an
 * intersection, or of the meet point in operand {@code k}, where side {@code 0} is the
 * lower bound of the parameter and side {@code 1} the upper bound of a segment.</p>
 */
public final class ConstructionBoxModel2D {
	/** Version of the structural totality table (certification design section 4.2). */
	public static final String TOTALITY_VERSION = "structural-totality/v1";
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
	/** Index of a predicate of the step itself. */
	static final int STEP_PREDICATE = -1;
	/** Index of the band of an unlabelled second output. */
	static final int COINCIDENCE_BAND = -2;
	/**
	 * Smallest cross product of two constant unit-scaled directions whose meet is total: it
	 * keeps the homogeneous coordinate of the meet above the finiteness threshold.
	 */
	private static final double TOTAL_MEET_CROSS = 1E-4;
	private static final double NORMALIZATION_MINIMUM = 1E-2;
	private static final double NORMALIZATION_MAXIMUM = 10E6;
	private static final int NORMALIZATION_LIMIT = 2_048;
	private static final Object UNKNOWN = new Object();
	/** A value that is never enclosed: a structural metric input and what it reaches. */
	private static final Object METRIC_OPAQUE = new Object();
	/** A value assumed valid whose enclosure is unbounded on the box. */
	private static final Object ASSUMED_OPAQUE = new Object();

	private final CertifiedConstructionProgram2D program;
	private final String[] lineClass;
	private final boolean[] zeroVector;
	private final boolean[] unitPoint;
	private final double[][] direction;
	private final boolean[] unitVector;
	private final boolean[] total;

	/** @param program captured program of any class */
	public ConstructionBoxModel2D(CertifiedConstructionProgram2D program) {
		this.program = program;
		List<Step> steps = program.getSteps();
		lineClass = new String[steps.size() + 1];
		zeroVector = new boolean[steps.size() + 1];
		unitPoint = new boolean[steps.size() + 1];
		direction = new double[steps.size() + 1][];
		unitVector = new boolean[steps.size() + 1];
		total = new boolean[steps.size() + 1];
		unitPoint[0] = true;
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
				direction[node] = unitPoint(operands.get(0))
						? direction(operands.get(1), false) : null;
				total[node] = definedDirection(direction[node]);
				break;
			case PERPENDICULAR:
				lineClass[node] = perpendicular(lineClass(operands.get(1)));
				direction[node] = unitPoint(operands.get(0))
						? direction(operands.get(1), true) : null;
				total[node] = definedDirection(direction[node]);
				break;
			case MEET:
				total[node] = totalMeet(operands);
				break;
			case MIDPOINT:
			case EXPRESSION_TRANSLATE:
			case POINT_PLUS_VECTOR:
				unitPoint[node] = true;
				total[node] = true;
				break;
			case INTERSECTION_OUTPUT:
				unitPoint[node] = true;
				break;
			case TRANSLATE:
			case ROTATE:
			case ROTATE_ABOUT:
			case MIRROR_POINT:
			case MIRROR_LINE:
			case DILATE:
				unitPoint[node] = unitPoint(operands.get(0));
				total[node] = unitPoint[node];
				break;
			case UNIT_VECTOR_LINE:
				unitVector[node] = step.getParameter(0) == 1;
				total[node] = true;
				break;
			case UNIT_VECTOR:
				unitVector[node] = step.getParameter(0) == 1;
				total[node] = operands.get(0).getKind() == OperandKind.NODE
						&& unitVector[operands.get(0).getNode()];
				break;
			case VECTOR_POINTS:
				zeroVector[node] = sameOperand(operands.get(0), operands.get(1));
				total[node] = true;
				break;
			case SCALE:
				Operand factor = operands.get(0);
				zeroVector[node] = factor.getKind() == OperandKind.SCALAR
						&& factor.getValue(0) == 0 || isZeroVector(operands.get(1));
				total[node] = true;
				break;
			case DOT:
				total[node] = true;
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
	 * @return whether every step is structurally total ({@link #TOTALITY_VERSION}): the
	 *         program is then defined and continuous for every driver parameter, without
	 *         any box computation (certification design section 4.2, authority 1)
	 */
	public boolean isStructurallyTotal() {
		for (int node = 1; node < total.length; node++) {
			if (!total[node]) {
				return false;
			}
		}
		return true;
	}

	/** @return whether the step that produces the node is structurally total */
	boolean isTotal(int node) {
		return total[node];
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
		return new Run(false, Mode.FIRST_ORDER, null, List.of()).evaluate(box);
	}

	/**
	 * Evaluates the program on a box of the real driver parameter. A circle driver is a
	 * function of the point of its circle, so a box may wrap the seam and extend one period
	 * beyond the domain on either side (section 10.8); other drivers keep their domain.
	 *
	 * @return typed outcome
	 */
	ConstructionBoxOutcome2D evaluatePeriodic(SplineOutwardInterval2D box) {
		return new Run(true, Mode.FIRST_ORDER, null, List.of()).evaluate(box);
	}

	/**
	 * Evaluates the program on a box for existence certification: every decision of a
	 * validity predicate, an inherited tolerance or the band of an unlabelled output also
	 * reads the centred form of its value, which is sound wherever the value enclosure is,
	 * and much tighter on small boxes near a threshold.
	 *
	 * @param periodic whether the box may wrap the seam of a circle driver
	 * @return typed outcome
	 */
	ConstructionBoxOutcome2D evaluateCertifying(SplineOutwardInterval2D box,
			boolean periodic) {
		return new Run(periodic, Mode.FIRST_ORDER, null, List.of(), true).evaluate(box);
	}

	/**
	 * Encloses one validity predicate and its first two derivatives on a box on which every
	 * earlier step is certified valid.
	 *
	 * @param periodic whether the box may wrap the seam of a circle driver
	 * @return enclosure, or {@code null} when the predicate is not reached on the box
	 */
	PredicateEnclosure enclose(SplineOutwardInterval2D box, Target target,
			boolean periodic) {
		Run run = new Run(periodic, Mode.SECOND_ORDER, target, List.of());
		try {
			run.evaluate(box);
		} catch (Captured captured) {
			return new PredicateEnclosure(captured.jet.value, captured.jet.derivative,
					captured.jet.second);
		}
		return null;
	}

	/**
	 * Encloses the traced point and its first two derivatives on a box on which the program
	 * is valid ({@code locus-v2-certified-construction-model.md} section 10.7: second-order
	 * enclosures serve the isolation and non-degeneracy of stationary points only).
	 *
	 * @param periodic whether the box may wrap the seam of a circle driver
	 * @return {@code {value, first, second}}, each with the x and y enclosures, or
	 *         {@code null} when the box is not valid or a derivative is not enclosed
	 */
	SplineOutwardInterval2D[][] encloseTraced(SplineOutwardInterval2D box, boolean periodic) {
		Run run = new Run(periodic, Mode.SECOND_ORDER, null, List.of());
		if (run.evaluate(box).kind != ConstructionBoxOutcome2D.Kind.VALID
				|| run.traced == null || run.traced.inhomX.second == null
				|| run.traced.inhomY.second == null) {
			return null;
		}
		Jet x = run.traced.inhomX;
		Jet y = run.traced.inhomY;
		return new SplineOutwardInterval2D[][] {{x.value, y.value},
			{x.derivative, y.derivative}, {x.second, y.second}};
	}

	/**
	 * Evaluates the program on a box assuming that one validity predicate holds: its value
	 * is restricted to its valid set, and every other predicate is decided by value
	 * enclosures only. A valid outcome certifies existence on the part of the box where the
	 * assumed predicate holds; its value enclosure may be unavailable.
	 *
	 * @param periodic whether the box may wrap the seam of a circle driver
	 * @return typed outcome
	 */
	ConstructionBoxOutcome2D evaluateAssuming(SplineOutwardInterval2D box, Target assumed,
			boolean periodic) {
		return evaluateAssuming(box, List.of(assumed), periodic);
	}

	/**
	 * Evaluates the program on a box assuming that each of several validity predicates
	 * holds, each certified elsewhere; see {@link #evaluateAssuming(SplineOutwardInterval2D,
	 * Target, boolean)}.
	 *
	 * @param periodic whether the box may wrap the seam of a circle driver
	 * @return typed outcome
	 */
	ConstructionBoxOutcome2D evaluateAssuming(SplineOutwardInterval2D box,
			List<Target> assumed, boolean periodic) {
		return new Run(periodic, Mode.VALUE_ONLY, null, assumed).evaluate(box);
	}

	/**
	 * @return whether a sign change of the predicate is a validity transition of the
	 *         program: a strict-inequality predicate of the step itself, or the membership of
	 *         a selected root or of a meet point; never the band of an unlabelled output nor
	 *         the membership of a root that a compaction selects, whose change of sign changes
	 *         the selected root instead
	 */
	boolean isTransitionTarget(Target target) {
		if (target.predicate.isEquality() || target.index == COINCIDENCE_BAND
				|| target.step < 1 || target.step > program.getSteps().size()) {
			return false;
		}
		Step step = program.getSteps().get(target.step - 1);
		if (step.getOperation() != Operation.INTERSECTION_OUTPUT
				|| target.predicate == ConstructionValidityPredicate2D.DISCRIMINANT) {
			return true;
		}
		Step pair = program.getSteps().get(step.getOperands().get(0).getNode() - 1);
		return pair.getParameter(0) != 1;
	}

	private boolean unitPoint(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			return program.getNodeKind(operand.getNode()) == NodeKind.POINT
					&& unitPoint[operand.getNode()];
		}
		return operand.getKind() == OperandKind.POINT && operand.getValue(2) == 1;
	}

	/** @return constant direction (a, b) of a line operand, or {@code null} */
	private double[] direction(Operand operand, boolean perpendicular) {
		double[] base;
		if (operand.getKind() == OperandKind.NODE) {
			base = direction[operand.getNode()];
		} else {
			base = new double[] {operand.getValue(0), operand.getValue(1)};
		}
		if (base == null) {
			return null;
		}
		return perpendicular ? new double[] {-base[1], base[0]} : base;
	}

	private static boolean definedDirection(double[] direction) {
		return direction != null
				&& (Math.abs(direction[0]) >= EPSILON || Math.abs(direction[1]) >= EPSILON);
	}

	/**
	 * A meet of two unbounded lines with constant, non-parallel, unit-scaled directions has
	 * a constant homogeneous coordinate above the finiteness threshold.
	 */
	private boolean totalMeet(List<Operand> operands) {
		double[][] directions = new double[2][];
		for (int index = 0; index < 2; index++) {
			Operand operand = operands.get(index);
			boolean unbounded = operand.getKind() == OperandKind.NODE
					? program.getNodeKind(operand.getNode()) == NodeKind.LINE
					: operand.getKind() == OperandKind.LINE;
			directions[index] = unbounded ? direction(operand, false) : null;
			if (directions[index] == null) {
				return false;
			}
		}
		double cross = directions[0][0] * directions[1][1]
				- directions[0][1] * directions[1][0];
		return Math.abs(cross) >= TOTAL_MEET_CROSS;
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

	/** Order of the jets of one evaluation. */
	private enum Mode {
		VALUE_ONLY, FIRST_ORDER, SECOND_ORDER
	}

	/** Address of one validity predicate: its step, its kind and its index. */
	static final class Target {
		final int step;
		final ConstructionValidityPredicate2D predicate;
		final int index;

		Target(int step, ConstructionValidityPredicate2D predicate, int index) {
			this.step = step;
			this.predicate = predicate;
			this.index = index;
		}

		boolean matches(int node, ConstructionValidityPredicate2D kind, int at) {
			return step == node && predicate == kind && index == at;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Target)) {
				return false;
			}
			Target target = (Target) other;
			return step == target.step && predicate == target.predicate
					&& index == target.index;
		}

		@Override
		public int hashCode() {
			return (step * 31 + predicate.ordinal()) * 31 + index;
		}

		@Override
		public String toString() {
			return "step" + step + ":" + predicate + ":" + index;
		}
	}

	/** Enclosure of a predicate value and of its first two derivatives on a box. */
	static final class PredicateEnclosure {
		final SplineOutwardInterval2D value;
		final SplineOutwardInterval2D first;
		/** Second derivative, or {@code null} when it is not enclosed. */
		final SplineOutwardInterval2D second;

		PredicateEnclosure(SplineOutwardInterval2D value, SplineOutwardInterval2D first,
				SplineOutwardInterval2D second) {
			this.value = value;
			this.first = first;
			this.second = second;
		}
	}

	/** One evaluation on one box. */
	private final class Run {
		private final boolean periodic;
		private final Mode mode;
		private final Target capture;
		private final List<Target> assumed;
		private final boolean centered;
		private final List<PredicateEvaluation> evaluations = new ArrayList<>();
		private final List<String> selection = new ArrayList<>();
		private Object[] nodes;
		private int node;
		/** Radius of the box around its midpoint, for the centred form. */
		private SplineOutwardInterval2D radius;
		/** Traced point of a valid box. */
		private PointJet traced;

		Run(boolean periodic, Mode mode, Target capture, List<Target> assumed) {
			this(periodic, mode, capture, assumed, false);
		}

		Run(boolean periodic, Mode mode, Target capture, List<Target> assumed,
				boolean centered) {
			this.periodic = periodic;
			this.mode = mode;
			this.capture = capture;
			this.assumed = assumed;
			this.centered = centered;
		}

		/**
		 * @return the value enclosure, intersected in a certifying evaluation with the
		 *         centred form {@code centre + derivative * [-radius, radius]}
		 */
		private SplineOutwardInterval2D tight(Jet jet) {
			if (!centered || jet.center == null || jet.derivative == null) {
				return jet.value;
			}
			try {
				SplineOutwardInterval2D both = jet.value.intersection(
						jet.center.add(jet.derivative.multiply(radius)));
				return both == null ? jet.value : both;
			} catch (ArithmeticException exception) {
				return jet.value;
			}
		}

		/** @return the jet with its tightest sound value enclosure */
		private Jet tightened(Jet jet) {
			SplineOutwardInterval2D value = tight(jet);
			return value == jet.value ? jet : jet.withValue(value);
		}

		ConstructionBoxOutcome2D evaluate(SplineOutwardInterval2D box) {
			LocusInterval2D domain = program.getDomain();
			double reach = periodic && program.getDriver() == Driver.CIRCLE
					? domain.getUpper() - domain.getLower() : 0;
			if (box.lower < domain.getLower() - reach
					|| box.upper > domain.getUpper() + reach) {
				return ConstructionBoxOutcome2D.unresolved(Unresolved.OUTSIDE_DOMAIN, -1,
						null, STEP_PREDICATE, "Parameter box is outside the driver domain",
						evaluations);
			}
			List<Step> steps = program.getSteps();
			nodes = new Object[steps.size() + 1];
			double middle = box.midpoint();
			double half = Math.nextUp(Math.max(middle - box.lower, box.upper - middle));
			radius = new SplineOutwardInterval2D(-half, half);
			try {
				nodes[0] = driver(Jet.variable(box, mode, centered));
			} catch (ArithmeticException exception) {
				return ConstructionBoxOutcome2D.unresolved(Unresolved.ENCLOSURE, 0, null,
						STEP_PREDICATE, String.valueOf(exception.getMessage()), evaluations);
			}
			Stop invalid = null;
			Stop unresolved = null;
			for (int index = 0; index < steps.size(); index++) {
				node = index + 1;
				Step step = steps.get(index);
				Object opaque = readsOpaque(step);
				if (opaque == UNKNOWN) {
					nodes[node] = UNKNOWN;
					continue;
				}
				if (opaque != null) {
					if (total[node]) {
						nodes[node] = opaque;
					} else {
						nodes[node] = UNKNOWN;
						if (unresolved == null) {
							unresolved = (opaque == METRIC_OPAQUE
									? new Stop(Unresolved.METRIC_VALUE, null, STEP_PREDICATE,
											"A step that is not total reads a metric value")
									: new Stop(Unresolved.ENCLOSURE, null, STEP_PREDICATE,
											"A step that is not total reads an unbounded value"))
									.at(node);
						}
					}
					continue;
				}
				try {
					nodes[node] = apply(step);
				} catch (Opaque opaqueStep) {
					nodes[node] = ASSUMED_OPAQUE;
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
						unresolved = new Stop(Unresolved.ENCLOSURE, null, STEP_PREDICATE,
								String.valueOf(exception.getMessage())).at(node);
					}
				}
			}
			if (invalid != null) {
				return ConstructionBoxOutcome2D.invalid(invalid.step, invalid.predicate,
						invalid.index, invalid.detail, evaluations);
			}
			if (unresolved != null) {
				return ConstructionBoxOutcome2D.unresolved(unresolved.reason,
						unresolved.step, unresolved.predicate, unresolved.index,
						unresolved.detail, evaluations);
			}
			Object output = nodes[steps.size()];
			if (output == METRIC_OPAQUE) {
				return ConstructionBoxOutcome2D.unresolved(Unresolved.METRIC_VALUE,
						steps.size(), null, STEP_PREDICATE,
						"The structural metric input contributes no value", evaluations);
			}
			if (output == ASSUMED_OPAQUE) {
				return ConstructionBoxOutcome2D.valid(null, null,
						String.join(",", selection), evaluations);
			}
			PointJet point = (PointJet) output;
			traced = point;
			return ConstructionBoxOutcome2D.valid(
					new SplineOutwardInterval2D[] {point.inhomX.value, point.inhomY.value},
					point.inhomX.derivative == null ? null : new SplineOutwardInterval2D[] {
						point.inhomX.derivative, point.inhomY.derivative},
					String.join(",", selection), evaluations);
		}

		/** @return {@code null}, {@link #UNKNOWN} or the opaque marker the step reads */
		private Object readsOpaque(Step step) {
			Object found = null;
			for (Operand operand : step.getOperands()) {
				if (operand.getKind() != OperandKind.NODE) {
					continue;
				}
				Object value = nodes[operand.getNode()];
				if (value == UNKNOWN) {
					return UNKNOWN;
				}
				if (value == METRIC_OPAQUE || value == ASSUMED_OPAQUE && found == null) {
					found = value;
				}
			}
			return found;
		}

		private boolean isAssumed(ConstructionValidityPredicate2D predicate, int index) {
			for (Target target : assumed) {
				if (target.matches(node, predicate, index)) {
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
				return METRIC_OPAQUE;
			default:
				return transform(step, point(operands.get(0)));
			}
		}

		private void requireDistinct(List<Operand> operands) {
			if (sameOperand(operands.get(0), operands.get(1))) {
				throw Stop.invalid(ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE,
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
			SplineOutwardInterval2D aBounds = tight(a);
			SplineOutwardInterval2D bBounds = tight(b);
			if (LineJet.nonZero(aBounds) || LineJet.nonZero(bBounds)) {
				record(ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE,
						a.square().add(b.square()), Decision.TRUE);
				return new LineJet(a, b, c, bound);
			}
			Jet norm = a.square().add(b.square());
			if (isAssumed(ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE)) {
				record(ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE, norm,
						Decision.TRUE);
				throw new Opaque();
			}
			if (!aBounds.containsZero() || !bBounds.containsZero()) {
				record(ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE, norm,
						Decision.TOLERANCE);
				throw new Stop(Unresolved.TOLERANCE,
						ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE,
						"Line definedness depends on the inherited threshold");
			}
			record(ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE, norm,
					Decision.UNDECIDED);
			throw new Stop(Unresolved.PREDICATE, ConstructionValidityPredicate2D.DIRECTION,
					STEP_PREDICATE, "Line direction is not decided");
		}

		/** AlgoIntersectLines: cross(g, h), GeoPoint.updateCoords, then the bounds. */
		private PointJet meet(List<Operand> operands) {
			if (lineClass(operands.get(0)) != null
					&& lineClass(operands.get(0)).equals(lineClass(operands.get(1)))) {
				throw Stop.invalid(ConstructionValidityPredicate2D.DIVISOR, STEP_PREDICATE,
						"The lines are parallel by construction");
			}
			LineJet g = line(operands.get(0));
			LineJet h = line(operands.get(1));
			PointJet point = normalized(g.b.multiply(h.c).subtract(g.c.multiply(h.b)),
					g.c.multiply(h.a).subtract(g.a.multiply(h.c)),
					g.a.multiply(h.b).subtract(g.b.multiply(h.a)));
			requireMember(g, point.inhomX, point.inhomY, 0);
			requireMember(h, point.inhomX, point.inhomY, 2);
			return point;
		}

		/** GeoPoint.setCoords followed by updateCoords, decided on the box. */
		private PointJet normalized(Jet x, Jet y, Jet z) {
			if (z.isUnitConstant()) {
				return PointJet.unit(x, y);
			}
			Jet divisor = tightened(z);
			SplineOutwardInterval2D size = ConstructionIntervalModel2D.abs(divisor.value);
			boolean finite = size.lower > EPSILON
					|| size.lower > Math.nextUp(
							ConstructionIntervalModel2D.abs(x.value).upper * EPSILON)
					|| size.lower > Math.nextUp(
							ConstructionIntervalModel2D.abs(y.value).upper * EPSILON);
			if (!finite) {
				if (isAssumed(ConstructionValidityPredicate2D.DIVISOR, STEP_PREDICATE)) {
					record(ConstructionValidityPredicate2D.DIVISOR, STEP_PREDICATE, z,
							Decision.TRUE);
					throw new Opaque();
				}
				boolean undecided = divisor.value.containsZero();
				record(ConstructionValidityPredicate2D.DIVISOR, STEP_PREDICATE, z,
						undecided ? Decision.UNDECIDED : Decision.TOLERANCE);
				throw new Stop(undecided ? Unresolved.PREDICATE : Unresolved.TOLERANCE,
						ConstructionValidityPredicate2D.DIVISOR, STEP_PREDICATE,
						"Point finiteness is not certified");
			}
			record(ConstructionValidityPredicate2D.DIVISOR, STEP_PREDICATE, z, Decision.TRUE);
			// A negative z negates (x, y, z); a unit z then needs no division (class v1).
			return divisor.value.upper < 0
					? divided(x.negate(), y.negate(), divisor.negate())
					: divided(x, y, divisor);
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
				throw Stop.invalid(ConstructionValidityPredicate2D.RADIUS, STEP_PREDICATE,
						"The centre and the point are the same operand");
			}
			PointJet center = point(operands.get(0));
			PointJet through = point(operands.get(1));
			Jet squared = through.inhomX.subtract(center.inhomX).square()
					.add(through.inhomY.subtract(center.inhomY).square());
			SplineOutwardInterval2D bounds = tight(squared);
			if (bounds.lower > RADIUS_SQUARED_MINIMUM
					|| isAssumed(ConstructionValidityPredicate2D.RADIUS, STEP_PREDICATE)) {
				record(ConstructionValidityPredicate2D.RADIUS, STEP_PREDICATE, squared,
						Decision.TRUE);
				return new CircleJet(center.inhomX, center.inhomY, squared);
			}
			boolean positive = bounds.lower > 0;
			record(ConstructionValidityPredicate2D.RADIUS, STEP_PREDICATE, squared,
					positive ? Decision.TOLERANCE : Decision.UNDECIDED);
			throw new Stop(positive ? Unresolved.TOLERANCE : Unresolved.PREDICATE,
					ConstructionValidityPredicate2D.RADIUS, STEP_PREDICATE,
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
			Jet root;
			if (isAssumed(ConstructionValidityPredicate2D.DISCRIMINANT, STEP_PREDICATE)) {
				record(ConstructionValidityPredicate2D.DISCRIMINANT, STEP_PREDICATE,
						discriminant, Decision.TRUE);
				root = discriminant.clampedSquareRoot();
			} else {
				requirePositive(ConstructionValidityPredicate2D.DISCRIMINANT, STEP_PREDICATE,
						discriminant);
				root = tightened(discriminant).sqrt();
			}
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
								undecidedMember(raw), "The roots on the path are not decided");
					}
					if (member == Decision.TRUE) {
						if (count == index) {
							selected = raw;
						}
						count++;
					}
				}
				if (selected < 0) {
					throw Stop.invalid(line.bound.predicate(), 2,
							"Fewer roots lie on the path than the output index");
				}
			} else {
				selected = pair.permutation[index];
				if (line.bound != null) {
					Decision member = membership(line, circle, root, selected);
					if (member == Decision.FALSE) {
						throw Stop.invalid(line.bound.predicate(), failedMember(selected),
								"The selected root is not on the path");
					}
					if (member == Decision.UNDECIDED) {
						throw new Stop(Unresolved.PREDICATE, line.bound.predicate(),
								undecidedMember(selected),
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

		/** @return index of the membership predicate of a raw root left undecided */
		private int undecidedMember(int raw) {
			return memberIndex(raw, Decision.UNDECIDED);
		}

		/** @return index of the membership predicate of a raw root certified false */
		private int failedMember(int raw) {
			return memberIndex(raw, Decision.FALSE);
		}

		private int memberIndex(int raw, Decision decision) {
			for (int side = 1; side >= 0; side--) {
				int index = 2 * raw + side;
				for (PredicateEvaluation evaluation : evaluations) {
					if (evaluation.step == node && evaluation.root == index
							&& evaluation.decision == decision) {
						return index;
					}
				}
			}
			return 2 * raw;
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
			return decideMember(line, parameter, 2 * raw);
		}

		/** Section 10.3: the inherited suppression may undefine an unlabelled output. */
		private void requireOutsideCoincidenceBand(LineJet line, Jet discriminant,
				Jet distance, Jet norm, CircleJet circle) {
			SplineOutwardInterval2D scale = line.a.square().value
					.hull(line.b.square().value).hull(line.c.square().value);
			SplineOutwardInterval2D rounding = distance.square().value
					.add(circle.radiusSquared.value.multiply(norm.value));
			SplineOutwardInterval2D margin = tight(discriminant);
			boolean outside = margin.lower
					> COINCIDENT_WINDOW_FACTOR * Math.nextUp(scale.upper)
					&& margin.lower
							> COINCIDENT_ROUNDING_FACTOR * Math.nextUp(rounding.upper);
			if (!outside) {
				record(ConstructionValidityPredicate2D.DISCRIMINANT, COINCIDENCE_BAND,
						discriminant, Decision.TOLERANCE);
				throw new Stop(Unresolved.UNLABELLED_COINCIDENT_OUTPUT,
						ConstructionValidityPredicate2D.DISCRIMINANT, COINCIDENCE_BAND,
						"An unlabelled second output may be suppressed at coincident roots");
			}
		}

		/** The meet point lies on each bounded operand. */
		private void requireMember(LineJet line, Jet x, Jet y, int index) {
			if (line.bound == null) {
				return;
			}
			Jet parameter = x.subtract(line.bound.x).multiply(line.b)
					.subtract(y.subtract(line.bound.y).multiply(line.a));
			Decision member = decideMember(line, parameter, index);
			if (member == Decision.FALSE) {
				throw Stop.invalid(line.bound.predicate(), memberIndex(index / 2,
						Decision.FALSE), "The point is not on the bounded line");
			}
			if (member == Decision.UNDECIDED) {
				throw new Stop(Unresolved.PREDICATE, line.bound.predicate(),
						memberIndex(index / 2, Decision.UNDECIDED),
						"Bounded-line membership is not decided");
			}
		}

		/**
		 * @param parameter {@code (X - O).(b, -a)}; a ray needs it nonnegative, a segment
		 *        needs it inside {@code [0, a^2 + b^2]}
		 * @param index index of the lower side; the upper side of a segment is the next
		 */
		private Decision decideMember(LineJet line, Jet parameter, int index) {
			ConstructionValidityPredicate2D predicate = line.bound.predicate();
			Decision lower = isAssumed(predicate, index) ? Decision.TRUE : sign(parameter);
			record(predicate, index, parameter, lower);
			if (line.bound.ray || lower == Decision.FALSE) {
				return lower;
			}
			Jet remaining = line.a.square().add(line.b.square()).subtract(parameter);
			Decision upper = isAssumed(predicate, index + 1) ? Decision.TRUE
					: sign(remaining);
			record(predicate, index + 1, remaining, upper);
			return upper == Decision.FALSE ? Decision.FALSE
					: lower == Decision.TRUE && upper == Decision.TRUE ? Decision.TRUE
							: Decision.UNDECIDED;
		}

		/** AlgoUnitVectorLine: (b, -a), divided by its length when normalized. */
		private VectorJet lineDirection(LineJet line, boolean normalize) {
			if (!normalize) {
				return new VectorJet(line.b, line.a.negate());
			}
			Jet length = tightened(line.a.square().add(line.b.square())).sqrt();
			return new VectorJet(line.b.divide(length), line.a.negate().divide(length));
		}

		/** AlgoUnitVectorVector: the vector, divided by its length when normalized. */
		private VectorJet vectorDirection(Operand operand, boolean normalize) {
			if (isZeroVector(operand)) {
				throw Stop.invalid(ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE,
						"The vector is zero by construction");
			}
			VectorJet source = vector(operand);
			if (!normalize) {
				return source;
			}
			Jet squared = source.x.square().add(source.y.square());
			if (isAssumed(ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE)
					&& !(tight(squared).lower > 0)) {
				record(ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE, squared,
						Decision.TRUE);
				throw new Opaque();
			}
			requirePositive(ConstructionValidityPredicate2D.DIRECTION, STEP_PREDICATE,
					squared);
			Jet length = tightened(squared).sqrt();
			return new VectorJet(source.x.divide(length), source.y.divide(length));
		}

		/** A strict-inequality predicate is decided by its enclosure. */
		private void requirePositive(ConstructionValidityPredicate2D predicate, int index,
				Jet value) {
			Decision decision = sign(value);
			if (predicate.isEquality() && decision == Decision.FALSE) {
				decision = Decision.UNDECIDED;
			}
			record(predicate, index, value, decision);
			if (decision == Decision.FALSE) {
				throw Stop.invalid(predicate, index, "Certified negative on the box");
			}
			if (decision == Decision.UNDECIDED) {
				throw new Stop(Unresolved.PREDICATE, predicate, index,
						"Predicate is not decided on the box");
			}
		}

		private Decision sign(Jet value) {
			SplineOutwardInterval2D bounds = tight(value);
			return bounds.lower > 0 ? Decision.TRUE
					: bounds.upper < 0 ? Decision.FALSE : Decision.UNDECIDED;
		}

		private void record(ConstructionValidityPredicate2D predicate, int index, Jet value,
				Decision decision) {
			if (capture != null && capture.matches(node, predicate, index)) {
				throw new Captured(value);
			}
			evaluations.add(new PredicateEvaluation(node, predicate, index, tight(value),
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
				STEP_PREDICATE, "The inherited line normalization is not uniform on the box");
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
		private final int index;
		private final String detail;
		private int step = -1;

		private Stop(Unresolved reason, ConstructionValidityPredicate2D predicate, int index,
				String detail) {
			this(false, reason, predicate, index, detail);
		}

		private Stop(boolean invalid, Unresolved reason,
				ConstructionValidityPredicate2D predicate, int index, String detail) {
			super(null, null, false, false);
			this.invalid = invalid;
			this.reason = reason;
			this.predicate = predicate;
			this.index = index;
			this.detail = detail;
		}

		static Stop invalid(ConstructionValidityPredicate2D predicate, int index,
				String detail) {
			return new Stop(true, null, predicate, index, detail);
		}

		Stop at(int node) {
			step = node;
			return this;
		}
	}

	/** A step assumed valid whose value is not bounded on the box. */
	private static final class Opaque extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private Opaque() {
			super(null, null, false, false);
		}
	}

	/** The enclosure of the targeted predicate, which ends its evaluation. */
	private static final class Captured extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final transient Jet jet;

		private Captured(Jet jet) {
			super(null, null, false, false);
			this.jet = jet;
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

	/**
	 * Value and derivatives with respect to the driver parameter. The first derivative is
	 * absent in a value-only evaluation and the second outside a second-order one; an
	 * operation with an absent derivative in an operand has none.
	 *
	 * <p>A certifying evaluation also carries the centre: an enclosure of the same formula
	 * at the midpoint of the box. The mean value theorem then encloses the value on the box
	 * by the centre plus the derivative enclosure times the radius of the box, a centred form
	 * whose overestimation does not grow with the cancellation of the formula.</p>
	 */
	static final class Jet {
		final SplineOutwardInterval2D value;
		final SplineOutwardInterval2D derivative;
		final SplineOutwardInterval2D second;
		/** Enclosure of the value at the midpoint of the box, or {@code null}. */
		final SplineOutwardInterval2D center;

		private Jet(SplineOutwardInterval2D value, SplineOutwardInterval2D derivative,
				SplineOutwardInterval2D second) {
			this(value, derivative, second, null);
		}

		private Jet(SplineOutwardInterval2D value, SplineOutwardInterval2D derivative,
				SplineOutwardInterval2D second, SplineOutwardInterval2D center) {
			this.value = value;
			this.derivative = derivative;
			this.second = second;
			this.center = center;
		}

		static Jet constant(double value) {
			SplineOutwardInterval2D point = SplineOutwardInterval2D.point(value);
			return new Jet(point, SplineOutwardInterval2D.point(0),
					SplineOutwardInterval2D.point(0), point);
		}

		static Jet variable(SplineOutwardInterval2D value, Mode mode) {
			return variable(value, mode, false);
		}

		/** @param centered whether the variable carries its midpoint as centre */
		static Jet variable(SplineOutwardInterval2D value, Mode mode, boolean centered) {
			return new Jet(value,
					mode == Mode.VALUE_ONLY ? null : SplineOutwardInterval2D.point(1),
					mode == Mode.SECOND_ORDER ? SplineOutwardInterval2D.point(0) : null,
					centered && mode != Mode.VALUE_ONLY
							? SplineOutwardInterval2D.point(value.midpoint()) : null);
		}

		/** @return the same jet with a tighter enclosure of its value */
		Jet withValue(SplineOutwardInterval2D tighter) {
			return new Jet(tighter, derivative, second, center);
		}

		/** Applies an operation to two centres; a centre the operation rejects is dropped. */
		private static SplineOutwardInterval2D centre(SplineOutwardInterval2D first,
				SplineOutwardInterval2D second,
				BinaryOperator<SplineOutwardInterval2D> operation) {
			if (first == null || second == null) {
				return null;
			}
			try {
				return operation.apply(first, second);
			} catch (ArithmeticException exception) {
				return null;
			}
		}

		boolean isUnitConstant() {
			return value.lower == 1 && value.upper == 1 && derivative != null
					&& derivative.lower == 0 && derivative.upper == 0;
		}

		Jet add(Jet other) {
			return new Jet(value.add(other.value),
					derivative == null || other.derivative == null ? null
							: derivative.add(other.derivative),
					second == null || other.second == null ? null : second.add(other.second),
					centre(center, other.center, SplineOutwardInterval2D::add));
		}

		Jet subtract(Jet other) {
			return new Jet(value.subtract(other.value),
					derivative == null || other.derivative == null ? null
							: derivative.subtract(other.derivative),
					second == null || other.second == null ? null
							: second.subtract(other.second),
					centre(center, other.center, SplineOutwardInterval2D::subtract));
		}

		Jet negate() {
			return new Jet(value.negate(), derivative == null ? null : derivative.negate(),
					second == null ? null : second.negate(),
					center == null ? null : center.negate());
		}

		Jet multiply(Jet other) {
			boolean first = derivative != null && other.derivative != null;
			SplineOutwardInterval2D product = value.multiply(other.value);
			SplineOutwardInterval2D firstDerivative = first
					? derivative.multiply(other.value).add(value.multiply(other.derivative))
					: null;
			SplineOutwardInterval2D secondDerivative = first && second != null
					&& other.second != null
					? second.multiply(other.value)
							.add(derivative.multiply(other.derivative)
									.multiply(SplineOutwardInterval2D.point(2)))
							.add(value.multiply(other.second))
					: null;
			return new Jet(product, firstDerivative, secondDerivative,
					centre(center, other.center, SplineOutwardInterval2D::multiply));
		}

		Jet divide(Jet other) {
			SplineOutwardInterval2D quotient = value.divide(other.value);
			SplineOutwardInterval2D centered =
					centre(center, other.center, SplineOutwardInterval2D::divide);
			if (derivative == null || other.derivative == null) {
				return new Jet(quotient, null, null, centered);
			}
			SplineOutwardInterval2D numerator = derivative.multiply(other.value)
					.subtract(value.multiply(other.derivative));
			SplineOutwardInterval2D firstDerivative =
					numerator.divide(other.value.multiply(other.value));
			SplineOutwardInterval2D secondDerivative = second == null || other.second == null
					? null : second.subtract(firstDerivative.multiply(other.derivative)
							.multiply(SplineOutwardInterval2D.point(2)))
							.subtract(quotient.multiply(other.second)).divide(other.value);
			return new Jet(quotient, firstDerivative, secondDerivative, centered);
		}

		Jet half() {
			return multiply(constant(0.5));
		}

		/** Square with a nonnegative enclosure; derivatives {@code 2 v v'} and so on. */
		Jet square() {
			SplineOutwardInterval2D squared = squared(value);
			SplineOutwardInterval2D two = SplineOutwardInterval2D.point(2);
			return new Jet(squared,
					derivative == null ? null : value.multiply(derivative).multiply(two),
					derivative == null || second == null ? null
							: derivative.multiply(derivative).add(value.multiply(second))
									.multiply(two),
					center == null ? null : squared(center));
		}

		private static SplineOutwardInterval2D squared(SplineOutwardInterval2D value) {
			if (value.lower >= 0 || value.upper <= 0) {
				SplineOutwardInterval2D squared = value.multiply(value);
				return squared.lower < 0 ? new SplineOutwardInterval2D(0, squared.upper)
						: squared;
			}
			double magnitude = Math.max(-value.lower, value.upper);
			return new SplineOutwardInterval2D(0, Math.nextUp(magnitude * magnitude));
		}

		/**
		 * Square root of a positive enclosure; {@code Math.sqrt} is correctly rounded, so
		 * one adjacent value each way encloses it. Derivative {@code v' / (2 sqrt v)}.
		 */
		Jet sqrt() {
			if (!(value.lower > 0)) {
				throw new ArithmeticException("Square root of an enclosure that is not positive");
			}
			SplineOutwardInterval2D root = root(value);
			SplineOutwardInterval2D centered = center == null || !(center.lower > 0) ? null
					: root(center);
			if (derivative == null) {
				return new Jet(root, null, null, centered);
			}
			SplineOutwardInterval2D twice = root.multiply(SplineOutwardInterval2D.point(2));
			SplineOutwardInterval2D firstDerivative = derivative.divide(twice);
			SplineOutwardInterval2D secondDerivative = second == null ? null
					: second.subtract(firstDerivative.multiply(firstDerivative)
							.multiply(SplineOutwardInterval2D.point(2))).divide(twice);
			return new Jet(root, firstDerivative, secondDerivative, centered);
		}

		private static SplineOutwardInterval2D root(SplineOutwardInterval2D value) {
			return new SplineOutwardInterval2D(
					Math.max(0, Math.nextDown(Math.sqrt(value.lower))),
					Math.nextUp(Math.sqrt(value.upper)));
		}

		/**
		 * Square root of the part of the enclosure where the value is nonnegative, for a
		 * predicate assumed to hold; it has no derivatives.
		 */
		Jet clampedSquareRoot() {
			if (value.upper < 0) {
				throw new ArithmeticException("The assumed predicate fails on the whole box");
			}
			double lower = Math.max(0, value.lower);
			return new Jet(new SplineOutwardInterval2D(
					Math.max(0, Math.nextDown(Math.sqrt(lower))),
					Math.nextUp(Math.sqrt(value.upper))), null, null);
		}

		Jet cos() {
			SplineOutwardInterval2D cosine = ConstructionIntervalModel2D.cosine(value);
			SplineOutwardInterval2D sine = ConstructionIntervalModel2D.sine(value);
			SplineOutwardInterval2D centered = center == null ? null
					: ConstructionIntervalModel2D.cosine(center);
			if (derivative == null) {
				return new Jet(cosine, null, null, centered);
			}
			return new Jet(cosine, sine.negate().multiply(derivative),
					second == null ? null : cosine.negate().multiply(derivative)
							.multiply(derivative).subtract(sine.multiply(second)), centered);
		}

		Jet sin() {
			SplineOutwardInterval2D sine = ConstructionIntervalModel2D.sine(value);
			SplineOutwardInterval2D cosine = ConstructionIntervalModel2D.cosine(value);
			SplineOutwardInterval2D centered = center == null ? null
					: ConstructionIntervalModel2D.sine(center);
			if (derivative == null) {
				return new Jet(sine, null, null, centered);
			}
			return new Jet(sine, cosine.multiply(derivative),
					second == null ? null : sine.negate().multiply(derivative)
							.multiply(derivative).add(cosine.multiply(second)), centered);
		}
	}
}
