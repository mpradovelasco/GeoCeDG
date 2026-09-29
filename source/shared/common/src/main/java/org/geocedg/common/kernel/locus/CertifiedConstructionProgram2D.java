/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
	/** Upper bound on captured steps. */
	public static final int MAXIMUM_STEPS = 64;

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

	/** Operations with their output kind, parameter count and class version. */
	public enum Operation {
		JOIN(false, 0), PARALLEL(false, 0), PERPENDICULAR(false, 0),
		MEET(true, 0), MIDPOINT(true, 0), TRANSLATE(true, 2), ROTATE(true, 2),
		ROTATE_ABOUT(true, 4), MIRROR_POINT(true, 2), MIRROR_LINE(true, 6),
		DILATE(true, 3),
		/**
		 * Class v2 (section 5.2): {@code P + V}, {@code V + P} or {@code P - V} of an
		 * expression point; parameters ux,uy, with {@code u = -V} for a difference.
		 */
		EXPRESSION_TRANSLATE(true, 2, true);

		private final boolean pointOutput;
		private final int parameterCount;
		private final boolean classV2;

		Operation(boolean pointOutput, int parameterCount) {
			this(pointOutput, parameterCount, false);
		}

		Operation(boolean pointOutput, int parameterCount, boolean classV2) {
			this.pointOutput = pointOutput;
			this.parameterCount = parameterCount;
			this.classV2 = classV2;
		}

		/** @return whether the operation's output node is a point */
		public boolean producesPoint() {
			return pointOutput;
		}

		/** @return whether the operation belongs to class v2 only */
		public boolean isClassV2() {
			return classV2;
		}
	}

	/** Operand kinds: an earlier node, or a captured constant point or line. */
	public enum OperandKind {
		NODE, POINT, LINE
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

		/** Creates one step; parameters follow the construction-model contract. */
		public Step(Operation operation, List<Operand> operands,
				double... parameters) {
			this.operation = Objects.requireNonNull(operation);
			this.operands = Collections.unmodifiableList(new ArrayList<>(operands));
			this.parameters = finite(parameters.clone());
			if (this.parameters.length != operation.parameterCount) {
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
	}

	private final Driver driver;
	private final double[] driverConstants;
	private final LocusInterval2D domain;
	private final List<Step> steps;
	private final boolean[] pointNodes;
	private final String version;

	/** Creates one validated program; the last node is the dependent point. */
	public CertifiedConstructionProgram2D(Driver driver, double[] driverConstants,
			LocusInterval2D domain, List<Step> steps) {
		this.driver = Objects.requireNonNull(driver);
		this.driverConstants = finite(driverConstants.clone());
		this.domain = Objects.requireNonNull(domain);
		this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
		if (this.driverConstants.length != driver.constantCount
				|| this.steps.size() > MAXIMUM_STEPS
				|| driver == Driver.SCALAR_AFFINE && !this.steps.isEmpty()) {
			throw new IllegalArgumentException("Malformed construction program");
		}
		pointNodes = new boolean[this.steps.size() + 1];
		pointNodes[0] = true;
		boolean classV2 = false;
		for (int index = 0; index < this.steps.size(); index++) {
			Step step = this.steps.get(index);
			validate(step, index + 1);
			pointNodes[index + 1] = step.operation.producesPoint();
			classV2 |= step.operation.isClassV2();
		}
		if (!pointNodes[pointNodes.length - 1]) {
			throw new IllegalArgumentException("The program output is not a point");
		}
		version = classV2 ? VERSION_V2 : VERSION;
	}

	public Driver getDriver() {
		return driver;
	}

	/**
	 * @return {@link #VERSION_V2} when a step needs class v2, otherwise
	 *         {@link #VERSION}
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

	/** @return whether a node is a point (otherwise a line) */
	public boolean isPointNode(int node) {
		return pointNodes[node];
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
		}
		return signature.toString();
	}

	private void validate(Step step, int node) {
		List<Operand> operands = step.operands;
		switch (step.operation) {
		case JOIN:
		case MIDPOINT:
			requireOperands(operands, node, true, true);
			break;
		case PARALLEL:
		case PERPENDICULAR:
			requireOperands(operands, node, true, false);
			break;
		case MEET:
			requireOperands(operands, node, false, false);
			break;
		default:
			requireOperands(operands, node, true);
			break;
		}
	}

	private void requireOperands(List<Operand> operands, int node,
			boolean... points) {
		if (operands.size() != points.length) {
			throw new IllegalArgumentException("Operand count mismatch");
		}
		boolean dependent = false;
		for (int index = 0; index < points.length; index++) {
			Operand operand = operands.get(index);
			if (operand.kind == OperandKind.NODE) {
				if (operand.node >= node || pointNodes[operand.node] != points[index]) {
					throw new IllegalArgumentException("Operand node kind mismatch");
				}
				dependent = true;
			} else if ((operand.kind == OperandKind.POINT) != points[index]) {
				throw new IllegalArgumentException("Operand constant kind mismatch");
			}
		}
		if (!dependent) {
			throw new IllegalArgumentException("A step must depend on the driver");
		}
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
