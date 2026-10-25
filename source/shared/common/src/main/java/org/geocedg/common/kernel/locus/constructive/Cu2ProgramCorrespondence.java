/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;

/**
 * Condition N1 of {@code cu2-certificate-transfer/v1}
 * ({@code locus-v2-certified-construction-model.md} section 12.2): the captured class-v3 program
 * of a locus is structurally the program of its constructive slice. The two are matched from the
 * traced point down: node 0 of the program is the substituted driver of the cone, every step is
 * one slice node with the same operation and the same inputs by structural position, every
 * intersection output has the same output index, selection state and label states, and every
 * captured constant is one node of the cone outside the slice. The correspondence is a bijection
 * between the steps and the slice nodes the traced point reads; otherwise it is not demonstrated.
 * It binds each captured constant to the cone node whose constructive value it represents.
 */
final class Cu2ProgramCorrespondence {

	/** One captured constant of the program and the cone node it represents. */
	static final class ConstantBinding {
		private final int programNode;
		private final int operand;
		private final Operand value;
		private final double radius;
		private final int coneNode;

		private ConstantBinding(int programNode, int operand, Operand value, double radius,
				int coneNode) {
			this.programNode = programNode;
			this.operand = operand;
			this.value = value;
			this.radius = radius;
			this.coneNode = coneNode;
		}

		/** @return the program node of the step that reads the constant */
		int getProgramNode() {
			return programNode;
		}

		/** @return the operand position, or {@code -1} for the radius of a circle step */
		int getOperand() {
			return operand;
		}

		/** @return the captured operand, or {@code null} for a captured radius */
		Operand getValue() {
			return value;
		}

		/** @return the captured radius of a circle step, otherwise {@code NaN} */
		double getRadius() {
			return radius;
		}

		/** @return the cone node the constant represents */
		int getConeNode() {
			return coneNode;
		}
	}

	private final List<ConstantBinding> constants;
	private final String failure;

	private Cu2ProgramCorrespondence(List<ConstantBinding> constants, String failure) {
		this.constants = Collections.unmodifiableList(new ArrayList<>(constants));
		this.failure = failure;
	}

	/**
	 * @param program the captured class-v3 program of the locus
	 * @param cone a cone that substitutes the driver of the slice of the locus
	 * @param traced the slice node of the traced point
	 * @return the correspondence, or why it is not demonstrated
	 */
	static Cu2ProgramCorrespondence match(CertifiedConstructionProgram2D program,
			ConstructiveCone cone, int traced) {
		Objects.requireNonNull(program);
		if (!cone.hasDriverSubstitution()) {
			return refused("the cone substitutes no driver");
		}
		Matcher matcher = new Matcher(program, cone);
		String failure = matcher.matchAll(program.getSteps().size(), traced);
		return failure == null ? new Cu2ProgramCorrespondence(matcher.constants, null)
				: refused(failure);
	}

	private static Cu2ProgramCorrespondence refused(String failure) {
		return new Cu2ProgramCorrespondence(Collections.emptyList(), failure);
	}

	/** @return whether N1 is demonstrated */
	boolean isDemonstrated() {
		return failure == null;
	}

	/** @return why N1 is not demonstrated, or {@code null} */
	String getFailure() {
		return failure;
	}

	/** @return the captured constants with the cone nodes they represent, in program order */
	List<ConstantBinding> getConstants() {
		return constants;
	}

	/** The structural walk of one program against one cone. */
	private static final class Matcher {
		private final List<Step> steps;
		private final ConstructiveCone cone;
		private final Map<Integer, Integer> nodeOfStep = new HashMap<>();
		private final Map<Integer, Integer> stepOfNode = new HashMap<>();
		private final Map<Integer, int[]> pairs = new HashMap<>();
		private final List<ConstantBinding> constants = new ArrayList<>();

		private Matcher(CertifiedConstructionProgram2D program, ConstructiveCone cone) {
			this.steps = program.getSteps();
			this.cone = cone;
		}

		/** @return {@code null} when the whole program corresponds, otherwise why not */
		private String matchAll(int programNode, int coneNode) {
			Deque<int[]> pending = new ArrayDeque<>();
			pending.push(new int[] {programNode, coneNode});
			while (!pending.isEmpty()) {
				int[] pair = pending.pop();
				String failure = match(pair[0], pair[1], pending);
				if (failure != null) {
					return failure;
				}
			}
			int outputs = 0;
			for (Step step : steps) {
				if (step.getOperation() != Operation.LINE_CIRCLE) {
					outputs++;
				}
			}
			if (nodeOfStep.size() != outputs + 1) {
				return "the program has steps that the traced point does not read";
			}
			int sliceNodes = countSliceNodes(coneNode);
			if (sliceNodes != outputs) {
				return "the slice has " + sliceNodes + " nodes and the program " + outputs
						+ " steps";
			}
			return null;
		}

		private String match(int programNode, int coneNode, Deque<int[]> pending) {
			Integer known = nodeOfStep.get(programNode);
			if (known != null) {
				return known == coneNode ? null
						: "program node " + programNode + " reads two cone nodes";
			}
			Integer step = stepOfNode.get(coneNode);
			if (step != null && step != programNode) {
				return "cone node " + coneNode + " reads two program nodes";
			}
			nodeOfStep.put(programNode, coneNode);
			stepOfNode.put(coneNode, programNode);
			if (programNode == 0) {
				return coneNode == cone.getReplacementNode() ? null
						: "the driver of the program is not the substituted driver";
			}
			ConstructiveConeNode node = cone.getNode(coneNode);
			if (!node.isDriverSlice()) {
				return "step " + programNode + " is not a node of the slice";
			}
			Step current = steps.get(programNode - 1);
			switch (current.getOperation()) {
			case JOIN:
			case SEGMENT:
			case RAY:
			case PARALLEL:
			case PERPENDICULAR:
			case MEET:
			case MIDPOINT:
			case CIRCLE_THROUGH:
				if (!current.getOperation().name().equals(node.getOperation().name())
						|| current.getOperands().size() != node.getInputCount()) {
					return "step " + programNode + " is " + current.getOperation()
							+ " and its node " + node.getOperation();
				}
				for (int position = 0; position < node.getInputCount(); position++) {
					String failure = operand(programNode, position,
							current.getOperands().get(position), current.getOperation(),
							node.getInput(position), pending);
					if (failure != null) {
						return failure;
					}
				}
				return null;
			case CIRCLE_RADIUS:
				if (node.getOperation() != ConstructiveOperation.CIRCLE_RADIUS) {
					return "step " + programNode + " is a circle by radius and its node "
							+ node.getOperation();
				}
				ConstructiveConeNode radius = cone.getNode(node.getInput(1));
				if (radius.isDriverSlice()
						|| radius.getOperation().getShape() != ConstructiveOperation.Shape.NUMBER) {
					return "the radius of step " + programNode + " is not a constant number";
				}
				constants.add(new ConstantBinding(programNode, -1, null,
						current.getParameter(0), node.getInput(1)));
				return operand(programNode, 0, current.getOperands().get(0),
						current.getOperation(), node.getInput(0), pending);
			case INTERSECTION_OUTPUT:
				return intersectionOutput(programNode, current, node, pending);
			default:
				return "step " + programNode + " has the operation " + current.getOperation()
						+ ", outside constructive-class/v1";
			}
		}

		private String intersectionOutput(int programNode, Step output,
				ConstructiveConeNode node, Deque<int[]> pending) {
			if (node.getOperation() != ConstructiveOperation.LINE_CIRCLE_OUTPUT
					|| node.getOutputIndex() != (int) output.getParameter(0)) {
				return "step " + programNode + " is another intersection output";
			}
			int pairNode = output.getOperands().get(0).getNode();
			Step pair = steps.get(pairNode - 1);
			InheritedLineCircleSelection selection = node.getSelection();
			if (pair.getOperation() != Operation.LINE_CIRCLE || selection == null) {
				return "step " + programNode + " has no reconstructible selection";
			}
			boolean compaction = pair.getParameter(0) == 1;
			if (compaction != selection.isCompaction()
					|| !compaction && (selection.getRawRoot(0) != (int) pair.getParameter(1)
							|| selection.getRawRoot(1) != (int) pair.getParameter(2))
					|| selection.isLabelled(0) != (pair.getParameter(3) == 1)
					|| selection.isLabelled(1) != (pair.getParameter(4) == 1)) {
				return "step " + programNode + " has another selection or label state";
			}
			int[] inputs = {node.getInput(0), node.getInput(1)};
			int[] known = pairs.get(pairNode);
			if (known != null) {
				return known[0] == inputs[0] && known[1] == inputs[1] ? null
						: "two outputs of one intersection read different objects";
			}
			pairs.put(pairNode, inputs);
			for (int position = 0; position < 2; position++) {
				String failure = operand(pairNode, position, pair.getOperands().get(position),
						Operation.LINE_CIRCLE, inputs[position], pending);
				if (failure != null) {
					return failure;
				}
			}
			return null;
		}

		/** One operand: a program node to match, or a captured constant outside the slice. */
		private String operand(int programNode, int position, Operand operand,
				Operation operation, int coneInput, Deque<int[]> pending) {
			if (operand.getKind() == OperandKind.NODE) {
				pending.push(new int[] {operand.getNode(), coneInput});
				return null;
			}
			ConstructiveConeNode input = cone.getNode(coneInput);
			if (input.isDriverSlice() || coneInput == cone.getReplacementNode()) {
				return "a captured constant of step " + programNode
						+ " is a node that moves with the driver";
			}
			if (!compatible(operand.getKind(), operation, input.getOperation())) {
				return "a captured " + operand.getKind() + " of step " + programNode
						+ " is the node " + input.getOperation();
			}
			constants.add(new ConstantBinding(programNode, position, operand, Double.NaN,
					coneInput));
			return null;
		}

		/**
		 * The kind of a captured constant and the operation of its node: a point, a circle, a
		 * line where only the direction counts or where it is a whole line, and a segment or ray
		 * where an intersection reads its limited part.
		 */
		private static boolean compatible(OperandKind kind, Operation consumer,
				ConstructiveOperation node) {
			switch (kind) {
			case POINT:
				return node.getShape() == ConstructiveOperation.Shape.POINT;
			case CIRCLE:
				return node.isClassCircle();
			case LINE:
				if (consumer == Operation.PARALLEL || consumer == Operation.PERPENDICULAR) {
					return node.getShape() == ConstructiveOperation.Shape.LINE;
				}
				return node.isClassLine() && !node.isBounded();
			case RAY:
				return node == ConstructiveOperation.RAY;
			case SEGMENT:
				return node == ConstructiveOperation.SEGMENT;
			default:
				return false;
			}
		}

		/** @return the slice nodes that the traced node reads, the traced node included */
		private int countSliceNodes(int traced) {
			boolean[] seen = new boolean[cone.size()];
			Deque<Integer> stack = new ArrayDeque<>();
			stack.push(traced);
			int count = 0;
			while (!stack.isEmpty()) {
				int current = stack.pop();
				if (seen[current]) {
					continue;
				}
				seen[current] = true;
				ConstructiveConeNode node = cone.getNode(current);
				if (!node.isDriverSlice()) {
					continue;
				}
				count++;
				for (int position = 0; position < node.getInputCount(); position++) {
					stack.push(node.getInput(position));
				}
			}
			return count;
		}
	}
}
