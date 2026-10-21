/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;

/**
 * Exact structural symmetry of a class-v3 program with a circle driver
 * ({@code locus-v2-certified-construction-model.md} section 10.7; certification design
 * section 5, proof 1). A symmetry is an involution {@code sigma} of the driver domain with
 * {@code F o sigma = F}; it is verified step by step on the captured program, never assumed.
 *
 * <p>The driver point is {@code (A cos t + cx, B sin t + cy)} when its captured axes have no
 * cross terms; reflecting it in the horizontal line through the centre is {@code t -> -t},
 * and in the vertical line {@code t -> pi - t}. The versioned operation table
 * {@value #VERSION} classifies a step as fixed, its exact value being the same at {@code t}
 * and {@code sigma(t)}, when every step it reads is fixed, or when it reads the driver point
 * only through a magnitude that the reflection keeps: the line through the driver
 * perpendicular to the mirror (a parallel to a perpendicular constant line, or a
 * perpendicular to a parallel one), and the circle centred on the mirror through the driver,
 * whose constant centre lies on the mirror by exact equality of the captured binary64
 * values. The traced point is symmetric when its step is fixed. Any other use of the driver
 * point leaves the program without a symmetry.</p>
 */
final class ConstructionSymmetry2D {
	/** Version of the operation table. */
	static final String VERSION = "fold-symmetry/v1";

	/** A reflection of the driver circle in a line through its centre. */
	enum Mirror {
		/** Reflection in the horizontal line through the centre: {@code t -> -t}. */
		HORIZONTAL,
		/** Reflection in the vertical line through the centre: {@code t -> pi - t}. */
		VERTICAL
	}

	private ConstructionSymmetry2D() {
		// Utility class.
	}

	/** @return the mirrors under which the traced point of the program is invariant */
	static List<Mirror> invariantMirrors(CertifiedConstructionProgram2D program) {
		List<Mirror> mirrors = new ArrayList<>();
		// No cross terms: x depends on cos t only and y on sin t only.
		if (program.getDriver() != Driver.CIRCLE || program.getDriverConstant(3) != 0
				|| program.getDriverConstant(4) != 0 || program.getSteps().isEmpty()) {
			return mirrors;
		}
		for (Mirror mirror : Mirror.values()) {
			if (tracedPointIsFixed(program, mirror)) {
				mirrors.add(mirror);
			}
		}
		return mirrors;
	}

	private static boolean tracedPointIsFixed(CertifiedConstructionProgram2D program,
			Mirror mirror) {
		List<Step> steps = program.getSteps();
		boolean[] fixed = new boolean[steps.size() + 1];
		for (int index = 0; index < steps.size(); index++) {
			Step step = steps.get(index);
			// A metric input is a function of the driver parameter through the address of
			// its target, not of the values it reads: a length from a fixed origin is never
			// invariant under a reflection of the parameter.
			fixed[index + 1] = step.getOperation() != Operation.METRIC_INPUT
					&& (readsFixedNodesOnly(step, fixed)
							|| readsTheDriverSymmetrically(step, mirror, program));
		}
		return fixed[steps.size()];
	}

	/** Node 0, the driver point, is never fixed: it is reflected. */
	private static boolean readsFixedNodesOnly(Step step, boolean[] fixed) {
		boolean readsNode = false;
		for (Operand operand : step.getOperands()) {
			if (operand.getKind() == OperandKind.NODE) {
				readsNode = true;
				if (!fixed[operand.getNode()]) {
					return false;
				}
			}
		}
		return readsNode;
	}

	private static boolean readsTheDriverSymmetrically(Step step, Mirror mirror,
			CertifiedConstructionProgram2D program) {
		List<Operand> operands = step.getOperands();
		if (operands.size() != 2) {
			return false;
		}
		Operand first = operands.get(0);
		Operand second = operands.get(1);
		boolean horizontal = mirror == Mirror.HORIZONTAL;
		switch (step.getOperation()) {
		case PARALLEL:
			// The parallel to a constant line perpendicular to the mirror.
			return isDriver(first) && isConstantLine(second)
					&& (horizontal ? isVertical(second) : isHorizontal(second));
		case PERPENDICULAR:
			// The perpendicular to a constant line parallel to the mirror.
			return isDriver(first) && isConstantLine(second)
					&& (horizontal ? isHorizontal(second) : isVertical(second));
		case CIRCLE_THROUGH:
			// The constant centre lies on the mirror: the radius is invariant.
			return isDriver(second) && first.getKind() == OperandKind.POINT
					&& (horizontal ? first.getValue(4) == program.getDriverConstant(7)
							: first.getValue(3) == program.getDriverConstant(6));
		default:
			return false;
		}
	}

	private static boolean isDriver(Operand operand) {
		return operand.getKind() == OperandKind.NODE && operand.getNode() == 0;
	}

	private static boolean isConstantLine(Operand operand) {
		return operand.getKind() == OperandKind.LINE || operand.getKind() == OperandKind.RAY
				|| operand.getKind() == OperandKind.SEGMENT;
	}

	/** {@code a x + c = 0} with {@code a != 0}. */
	private static boolean isVertical(Operand line) {
		return line.getValue(1) == 0 && line.getValue(0) != 0;
	}

	/** {@code b y + c = 0} with {@code b != 0}. */
	private static boolean isHorizontal(Operand line) {
		return line.getValue(0) == 0 && line.getValue(1) != 0;
	}
}
