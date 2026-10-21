/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable construction program of the certified construction class
 * ({@code locus-v2-certified-construction-model.md}). It holds only typed steps
 * and binary64 constants captured from one reconstructible evaluator slice; no
 * GeoElement, label, coordinate sample or construction order survives capture.
 * Node {@code 0} is the driver point and step {@code i} produces node
 * {@code i + 1}. The program is certificate material, never identity. Its version
 * is derived from its steps (contract section 8).
 */
public final class CertifiedConstructionProgram2D {
	/** Version of a program made only of class-v1 operations. */
	public static final String VERSION = "certified-construction-program/v1";
	/** Version of a program with an expression-point translation (section 5.2). */
	public static final String VERSION_V2 = "certified-construction-program/v2";
	/**
	 * Version of a program with a step, an operand or a size of class v3 (section 10.1,
	 * PRE-G9B-CU2).
	 */
	public static final String VERSION_V3 = "certified-construction-program/v3";
	/** Upper bound on captured steps of classes v1 and v2. */
	public static final int MAXIMUM_STEPS = 64;
	/**
	 * Versioned upper bound on the driver-dependent steps of a class-v3 program after
	 * macro expansion (section 10.9; decision D-CU2-5 approves 128 to 512).
	 */
	public static final int MAXIMUM_STEPS_V3 = 256;

	/** Driver families of class v1. */
	public enum Driver {
		/** Complete circle; constants h0,h1,e0x,e0y,e1x,e1y,mx,my. */
		CIRCLE(8),
		/** Circle arc; circle constants plus start, extent, positive(1|0). */
		CIRCULAR_ARC(11),
		/** Segment; constants sx,sy,a,b. */
		SEGMENT(4),
		/** Direct scalar affine certificate; constants ax,bx,ay,by. */
		SCALAR_AFFINE(4);

		private final int constantCount;

		Driver(int constantCount) {
			this.constantCount = constantCount;
		}
	}

	/** Kinds of program nodes; rays and segments are lines with a membership predicate. */
	public enum NodeKind {
		POINT, LINE, RAY, SEGMENT, CIRCLE,
		/** Both raw roots of a line-circle intersection with its selection rule. */
		INTERSECTION, VECTOR, SCALAR;

		/** @return whether the node is a line, possibly bounded */
		public boolean isLine() {
			return this == LINE || this == RAY || this == SEGMENT;
		}
	}

	/** Operations with their output kind, parameter count and class version. */
	public enum Operation {
		JOIN(NodeKind.LINE, 0, 1), PARALLEL(NodeKind.LINE, 0, 1),
		PERPENDICULAR(NodeKind.LINE, 0, 1), MEET(NodeKind.POINT, 0, 1),
		MIDPOINT(NodeKind.POINT, 0, 1), TRANSLATE(NodeKind.POINT, 2, 1),
		ROTATE(NodeKind.POINT, 2, 1), ROTATE_ABOUT(NodeKind.POINT, 4, 1),
		MIRROR_POINT(NodeKind.POINT, 2, 1), MIRROR_LINE(NodeKind.POINT, 6, 1),
		DILATE(NodeKind.POINT, 3, 1),
		/**
		 * Class v2 (section 5.2): {@code P + V}, {@code V + P} or {@code P - V} of an
		 * expression point; parameters ux,uy, with {@code u = -V} for a difference.
		 */
		EXPRESSION_TRANSLATE(NodeKind.POINT, 2, 2),
		/** Class v3: ray from its first point through its second. */
		RAY(NodeKind.RAY, 0, 3),
		/** Class v3: segment from its first point to its second. */
		SEGMENT(NodeKind.SEGMENT, 0, 3),
		/** Class v3: circle by centre and constant radius; parameter r. */
		CIRCLE_RADIUS(NodeKind.CIRCLE, 1, 3),
		/** Class v3: circle by centre through a point. */
		CIRCLE_THROUGH(NodeKind.CIRCLE, 0, 3),
		/**
		 * Class v3 (section 10.2): both raw roots of a line and a circle; parameters
		 * mode (0 permutation, 1 compaction), p0, p1, labelled0, labelled1.
		 */
		LINE_CIRCLE(NodeKind.INTERSECTION, 5, 3),
		/** Class v3: one indexed output of an intersection; parameter index. */
		INTERSECTION_OUTPUT(NodeKind.POINT, 1, 3),
		/** Class v3: direction of a line, unit when normalized; parameter normalize. */
		UNIT_VECTOR_LINE(NodeKind.VECTOR, 1, 3),
		/** Class v3: direction of a vector, unit when normalized; parameter normalize. */
		UNIT_VECTOR(NodeKind.VECTOR, 1, 3),
		/** Class v3: vector from the first point to the second. */
		VECTOR_POINTS(NodeKind.VECTOR, 0, 3),
		/** Class v3: scalar multiple of a vector. */
		SCALE(NodeKind.VECTOR, 0, 3),
		/** Class v3: dot product of two vectors. */
		DOT(NodeKind.SCALAR, 0, 3),
		/** Class v3: point plus or minus a vector, a unit point; parameter sign. */
		POINT_PLUS_VECTOR(NodeKind.POINT, 1, 3),
		/**
		 * Class v3 (section 10.1): a structural metric input whose value is never
		 * evidence; its descriptor names the metric.
		 */
		METRIC_INPUT(NodeKind.SCALAR, 0, 3);

		private final NodeKind output;
		private final int parameterCount;
		private final int classVersion;

		Operation(NodeKind output, int parameterCount, int classVersion) {
			this.output = output;
			this.parameterCount = parameterCount;
			this.classVersion = classVersion;
		}

		/** @return whether the operation's output node is a point */
		public boolean producesPoint() {
			return output == NodeKind.POINT;
		}

		/** @return whether the operation belongs to class v2 only */
		public boolean isClassV2() {
			return classVersion == 2;
		}

		/** @return kind of the operation's output node */
		public NodeKind getOutputKind() {
			return output;
		}

		/** @return lowest class that admits the operation */
		public int getClassVersion() {
			return classVersion;
		}
	}

	/**
	 * Operand kinds: an earlier node, or a captured constant. Constants other than a
	 * point and a line belong to class v3.
	 */
	public enum OperandKind {
		NODE(null), POINT(NodeKind.POINT), LINE(NodeKind.LINE),
		/** Ray a,b,c with its start point ox,oy; direction (b,-a). */
		RAY(NodeKind.RAY),
		/** Segment a,b,c with its start point ox,oy; end at start plus (b,-a). */
		SEGMENT(NodeKind.SEGMENT),
		/** Circle of matrix (1,1,A2,0,A4,A5); values A2,A4,A5. */
		CIRCLE(NodeKind.CIRCLE),
		/** Vector x,y. */
		VECTOR(NodeKind.VECTOR),
		/** Number value. */
		SCALAR(NodeKind.SCALAR);

		private final NodeKind nodeKind;

		OperandKind(NodeKind nodeKind) {
			this.nodeKind = nodeKind;
		}
	}

	/** One operand of a step. */
	public static final class Operand {
		private final OperandKind kind;
		private final int node;
		private final double[] values;

		private Operand(OperandKind kind, int node, double... values) {
			this.kind = kind;
			this.node = node;
			this.values = finite(values.clone());
		}

		/** @return reference to an earlier node */
		public static Operand node(int index) {
			if (index < 0) {
				throw new IllegalArgumentException("Negative node index");
			}
			return new Operand(OperandKind.NODE, index);
		}

		/** @return captured constant point, homogeneous and inhomogeneous */
		public static Operand point(double x, double y, double z, double inhomX,
				double inhomY) {
			return new Operand(OperandKind.POINT, -1, x, y, z, inhomX, inhomY);
		}

		/** @return captured constant line {@code a x + b y + c = 0} */
		public static Operand line(double a, double b, double c) {
			return new Operand(OperandKind.LINE, -1, a, b, c);
		}

		/** @return captured constant ray or segment with its start point (class v3) */
		public static Operand bounded(boolean ray, double a, double b, double c,
				double startX, double startY) {
			return new Operand(ray ? OperandKind.RAY : OperandKind.SEGMENT, -1, a, b, c,
					startX, startY);
		}

		/** @return captured constant circle of matrix (1,1,A2,0,A4,A5) (class v3) */
		public static Operand circle(double a2, double a4, double a5) {
			return new Operand(OperandKind.CIRCLE, -1, a2, a4, a5);
		}

		/** @return captured constant vector (class v3) */
		public static Operand vector(double x, double y) {
			return new Operand(OperandKind.VECTOR, -1, x, y);
		}

		/** @return captured constant number (class v3) */
		public static Operand scalar(double value) {
			return new Operand(OperandKind.SCALAR, -1, value);
		}

		public OperandKind getKind() {
			return kind;
		}

		public int getNode() {
			return node;
		}

		/** @return captured constant component */
		public double getValue(int index) {
			return values[index];
		}

		private String signature() {
			return kind == OperandKind.NODE ? "n" + node
					: kind.name().toLowerCase(java.util.Locale.ROOT) + hex(values);
		}
	}

	/** One captured step producing the next node. */
	public static final class Step {
		private final Operation operation;
		private final List<Operand> operands;
		private final double[] parameters;
		private final String descriptor;

		/** Creates one step; parameters follow the construction-model contract. */
		public Step(Operation operation, List<Operand> operands,
				double... parameters) {
			this(operation, operands, null, parameters);
		}

		/**
		 * Creates one step with a descriptor, which only a structural metric input
		 * carries (class v3).
		 */
		public Step(Operation operation, List<Operand> operands, String descriptor,
				double... parameters) {
			this.operation = Objects.requireNonNull(operation);
			this.operands = Collections.unmodifiableList(new ArrayList<>(operands));
			this.parameters = finite(parameters.clone());
			this.descriptor = descriptor;
			if (this.parameters.length != operation.parameterCount
					|| (descriptor != null) != (operation == Operation.METRIC_INPUT)) {
				throw new IllegalArgumentException("Parameter count mismatch");
			}
		}

		public Operation getOperation() {
			return operation;
		}

		public List<Operand> getOperands() {
			return operands;
		}

		/** @return captured represented parameter */
		public double getParameter(int index) {
			return parameters[index];
		}

		/** @return descriptor of a structural metric input, otherwise {@code null} */
		public String getDescriptor() {
			return descriptor;
		}
	}

	private static final Set<NodeKind> LINES =
			EnumSet.of(NodeKind.LINE, NodeKind.RAY, NodeKind.SEGMENT);
	private static final Set<NodeKind> POINTS = EnumSet.of(NodeKind.POINT);

	private final Driver driver;
	private final double[] driverConstants;
	private final LocusInterval2D domain;
	private final List<Step> steps;
	private final NodeKind[] nodeKinds;
	private final String version;

	/** Creates one validated program; the last node is the dependent point. */
	public CertifiedConstructionProgram2D(Driver driver, double[] driverConstants,
			LocusInterval2D domain, List<Step> steps) {
		this.driver = Objects.requireNonNull(driver);
		this.driverConstants = finite(driverConstants.clone());
		this.domain = Objects.requireNonNull(domain);
		this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
		if (this.driverConstants.length != driver.constantCount
				|| this.steps.size() > MAXIMUM_STEPS_V3
				|| driver == Driver.SCALAR_AFFINE && !this.steps.isEmpty()) {
			throw new IllegalArgumentException("Malformed construction program");
		}
		nodeKinds = new NodeKind[this.steps.size() + 1];
		nodeKinds[0] = NodeKind.POINT;
		int classVersion = this.steps.size() > MAXIMUM_STEPS ? 3 : 1;
		for (int index = 0; index < this.steps.size(); index++) {
			Step step = this.steps.get(index);
			classVersion = Math.max(classVersion, Math.max(step.operation.classVersion,
					validate(step, index + 1)));
			nodeKinds[index + 1] = step.operation.output;
		}
		if (nodeKinds[nodeKinds.length - 1] != NodeKind.POINT) {
			throw new IllegalArgumentException("The program output is not a point");
		}
		version = classVersion == 3 ? VERSION_V3 : classVersion == 2 ? VERSION_V2 : VERSION;
	}

	public Driver getDriver() {
		return driver;
	}

	/**
	 * @return {@link #VERSION_V3} when a step, an operand or the size needs class v3,
	 *         {@link #VERSION_V2} when a step needs class v2, otherwise {@link #VERSION}
	 */
	public String getVersion() {
		return version;
	}

	/** @return captured driver constant */
	public double getDriverConstant(int index) {
		return driverConstants[index];
	}

	/** @return declared driver domain the evaluator applies */
	public LocusInterval2D getDomain() {
		return domain;
	}

	public List<Step> getSteps() {
		return steps;
	}

	/** @return whether a node is a point */
	public boolean isPointNode(int node) {
		return nodeKinds[node] == NodeKind.POINT;
	}

	/** @return kind of a node */
	public NodeKind getNodeKind(int node) {
		return nodeKinds[node];
	}

	/**
	 * @return canonical text of the selection rule of the program (section 10.2): the mode,
	 *         raw root order and label state of every line-circle intersection and the output
	 *         that every indexed step selects, in program order
	 */
	public String getSelectionSignature() {
		StringBuilder signature = new StringBuilder();
		for (int index = 0; index < steps.size(); index++) {
			Step step = steps.get(index);
			if (step.operation == Operation.LINE_CIRCLE
					|| step.operation == Operation.INTERSECTION_OUTPUT) {
				signature.append(index + 1).append(step.operation.name())
						.append(hex(step.parameters)).append(';');
			}
		}
		return signature.toString();
	}

	/** @return deterministic certificate-material signature */
	public String getSignature() {
		StringBuilder signature = new StringBuilder(version).append("|driver=")
				.append(driver.name()).append(hex(driverConstants));
		for (Step step : steps) {
			signature.append('|').append(step.operation.name()).append('(');
			for (int index = 0; index < step.operands.size(); index++) {
				signature.append(index == 0 ? "" : ",")
						.append(step.operands.get(index).signature());
			}
			signature.append(')').append(hex(step.parameters));
			if (step.descriptor != null) {
				signature.append('{').append(step.descriptor).append('}');
			}
		}
		return signature.toString();
	}

	/** @return lowest class version that admits the step */
	private int validate(Step step, int node) {
		List<Operand> operands = step.operands;
		switch (step.operation) {
		case JOIN:
		case MIDPOINT:
		case RAY:
		case SEGMENT:
		case CIRCLE_THROUGH:
		case VECTOR_POINTS:
			return requireOperands(operands, node, POINTS, POINTS);
		case PARALLEL:
		case PERPENDICULAR:
			return requireOperands(operands, node, POINTS, LINES);
		case MEET:
			return requireOperands(operands, node, LINES, LINES);
		case LINE_CIRCLE:
			requireSelection(step);
			return requireOperands(operands, node, LINES, EnumSet.of(NodeKind.CIRCLE));
		case INTERSECTION_OUTPUT:
			requireFlag(step.parameters[0]);
			if (operands.size() != 1 || operands.get(0).kind != OperandKind.NODE) {
				throw new IllegalArgumentException("An output needs an intersection node");
			}
			return requireOperands(operands, node, EnumSet.of(NodeKind.INTERSECTION));
		case UNIT_VECTOR_LINE:
			requireFlag(step.parameters[0]);
			return requireOperands(operands, node, LINES);
		case UNIT_VECTOR:
			requireFlag(step.parameters[0]);
			return requireOperands(operands, node, EnumSet.of(NodeKind.VECTOR));
		case SCALE:
			return requireOperands(operands, node, EnumSet.of(NodeKind.SCALAR),
					EnumSet.of(NodeKind.VECTOR));
		case DOT:
			return requireOperands(operands, node, EnumSet.of(NodeKind.VECTOR),
					EnumSet.of(NodeKind.VECTOR));
		case POINT_PLUS_VECTOR:
			if (Math.abs(step.parameters[0]) != 1) {
				throw new IllegalArgumentException("Malformed vector sign");
			}
			return requireOperands(operands, node, POINTS, EnumSet.of(NodeKind.VECTOR));
		case CIRCLE_RADIUS:
			if (!(step.parameters[0] > 0)) {
				throw new IllegalArgumentException("Nonpositive circle radius");
			}
			return requireOperands(operands, node, POINTS);
		case METRIC_INPUT:
			if (operands.isEmpty() || step.descriptor.isEmpty()) {
				throw new IllegalArgumentException("Malformed structural metric input");
			}
			List<Set<NodeKind>> endpoints = new ArrayList<>();
			for (int index = 0; index < operands.size(); index++) {
				endpoints.add(POINTS);
			}
			return requireOperands(operands, node, endpoints);
		default:
			return requireOperands(operands, node, POINTS);
		}
	}

	private static void requireSelection(Step step) {
		double mode = step.parameters[0];
		double first = step.parameters[1];
		double second = step.parameters[2];
		requireFlag(step.parameters[3]);
		requireFlag(step.parameters[4]);
		boolean permutation = mode == 0 && (first == 0 && second == 1
				|| first == 1 && second == 0);
		boolean compaction = mode == 1 && first == 0 && second == 1;
		if (!permutation && !compaction) {
			throw new IllegalArgumentException("Malformed intersection selection");
		}
	}

	private static void requireFlag(double value) {
		if (value != 0 && value != 1) {
			throw new IllegalArgumentException("Malformed flag parameter");
		}
	}

	@SafeVarargs
	private final int requireOperands(List<Operand> operands, int node,
			Set<NodeKind>... kinds) {
		return requireOperands(operands, node, List.of(kinds));
	}

	/** @return lowest class version that admits the operand kinds */
	private int requireOperands(List<Operand> operands, int node,
			List<Set<NodeKind>> kinds) {
		if (operands.size() != kinds.size()) {
			throw new IllegalArgumentException("Operand count mismatch");
		}
		boolean dependent = false;
		int classVersion = 1;
		for (int index = 0; index < kinds.size(); index++) {
			Operand operand = operands.get(index);
			NodeKind kind;
			if (operand.kind == OperandKind.NODE) {
				if (operand.node >= node) {
					throw new IllegalArgumentException("Operand node kind mismatch");
				}
				kind = nodeKinds[operand.node];
				dependent = true;
			} else {
				kind = operand.kind.nodeKind;
				if (operand.kind != OperandKind.POINT && operand.kind != OperandKind.LINE) {
					classVersion = 3;
				}
			}
			if (!kinds.get(index).contains(kind)) {
				throw new IllegalArgumentException(operand.kind == OperandKind.NODE
						? "Operand node kind mismatch" : "Operand constant kind mismatch");
			}
			if (kind == NodeKind.RAY || kind == NodeKind.SEGMENT) {
				classVersion = 3;
			}
		}
		if (!dependent) {
			throw new IllegalArgumentException("A step must depend on the driver");
		}
		return classVersion;
	}

	private static double[] finite(double[] values) {
		for (double value : values) {
			if (!Double.isFinite(value)) {
				throw new IllegalArgumentException("Nonfinite captured constant");
			}
		}
		return values;
	}

	private static String hex(double[] values) {
		StringBuilder result = new StringBuilder("[");
		for (int index = 0; index < values.length; index++) {
			result.append(index == 0 ? "" : ",").append(Double.toHexString(values[index]));
		}
		return result.append(']').toString();
	}
}
