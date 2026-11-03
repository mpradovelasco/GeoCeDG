/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.MacroKernel;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.algos.AlgoCirclePointRadius;
import org.geogebra.common.kernel.algos.AlgoCircleTwoPoints;
import org.geogebra.common.kernel.algos.AlgoCircleThreePoints;
import org.geogebra.common.kernel.algos.AlgoEllipseFociLength;
import org.geogebra.common.kernel.algos.AlgoHyperbolaFociLength;
import org.geogebra.common.kernel.algos.AlgoEllipseHyperbolaFociPoint;
import org.geogebra.common.kernel.algos.AlgoParabolaPointLine;
import org.geogebra.common.kernel.advanced.AlgoParabolaParameter;
import org.geogebra.common.kernel.algos.AlgoJoinPoints;
import org.geogebra.common.kernel.algos.AlgoJoinPointsSegment;
import org.geogebra.common.kernel.algos.AlgoJoinPointsRay;
import org.geogebra.common.kernel.algos.AlgoMidpoint;
import org.geogebra.common.kernel.algos.AlgoMidpointSegment;
import org.geogebra.common.kernel.algos.AlgoDependentPoint;
import org.geogebra.common.kernel.algos.AlgoDependentVector;
import org.geogebra.common.kernel.algos.AlgoDependentNumber;
import org.geogebra.common.kernel.algos.AlgoDependentLine;
import org.geogebra.common.kernel.algos.AlgoDependentConic;
import org.geogebra.common.kernel.algos.AlgoRotate;
import org.geogebra.common.kernel.algos.AlgoRotatePoint;
import org.geogebra.common.kernel.algos.AlgoTranslate;
import org.geogebra.common.kernel.algos.AlgoDilate;
import org.geogebra.common.kernel.algos.AlgoMirror;
import org.geogebra.common.kernel.algos.AlgoConicPartCircle;
import org.geogebra.common.kernel.algos.AlgoConicPartCircumcircle;
import org.geogebra.common.kernel.algos.AlgoConicPartConicParameters;
import org.geogebra.common.kernel.algos.AlgoConicPartConicPoints;
import org.geogebra.common.kernel.algos.AlgoSemicircle;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.MySpecialDouble;
import org.geogebra.common.kernel.arithmetic.MyVecNode;
import org.geogebra.common.kernel.arithmetic.MyList;
import org.geogebra.common.kernel.arithmetic.Equation;
import org.geogebra.common.kernel.arithmetic.FunctionVariable;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoAxis;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;

/** Positive, fresh file-fidelity table for the optional native conic field. */
public final class NativeSourceFidelity2D {

	private static final Set<Class<?>> ALGORITHMS = Set.of(
			AlgoCirclePointRadius.class, AlgoCircleTwoPoints.class, AlgoCircleThreePoints.class,
			AlgoEllipseFociLength.class, AlgoHyperbolaFociLength.class,
			AlgoEllipseHyperbolaFociPoint.class, AlgoParabolaPointLine.class,
			AlgoParabolaParameter.class, AlgoJoinPoints.class, AlgoJoinPointsSegment.class,
			AlgoJoinPointsRay.class, AlgoMidpoint.class, AlgoMidpointSegment.class,
			AlgoDependentPoint.class, AlgoDependentVector.class, AlgoDependentNumber.class,
			AlgoDependentLine.class, AlgoDependentConic.class, AlgoRotate.class,
			AlgoRotatePoint.class, AlgoTranslate.class, AlgoDilate.class, AlgoMirror.class,
			AlgoConicPartCircle.class, AlgoConicPartCircumcircle.class,
			AlgoConicPartConicParameters.class, AlgoConicPartConicPoints.class, AlgoSemicircle.class);
	private static final Set<String> OPERATIONS = Set.of("NO_OPERATION", "PLUS", "MINUS",
			"MULTIPLY", "DIVIDE", "POWER", "NEGATE", "SIN", "COS", "TAN", "EXP", "LOG",
			"SQRT", "ABS", "SGN", "ARCSIN", "ARCCOS", "ARCTAN", "SINH", "COSH", "TANH");
	private static final Set<Class<?>> VALUES = Set.of(GeoPoint.class, GeoNumeric.class,
			GeoLine.class, GeoRay.class, GeoSegment.class, GeoConic.class, GeoVector.class, GeoList.class);
	private final Map<GeoElement, Boolean> memo = new IdentityHashMap<>();
	private final boolean typedMemory;
	private int remaining = 4096;

	private NativeSourceFidelity2D(boolean typedMemory) {
		this.typedMemory = typedMemory;
	}

	/**
	 * The table is deliberately independent of transactional typed restoration.
	 * Unknown native procedures keep their historical readable file payload.
	 * @return whether the pertinent defining closure has a positive file witness
	 */
	public static boolean isFaithfulSource(GeoElement source) {
		return new NativeSourceFidelity2D(false).geo(source, 0);
	}

	/** @return one fresh memo confined to a single pure XML serialization pass */
	public static NativeSourceFidelity2D fileSerializationPass() {
		return new NativeSourceFidelity2D(false);
	}

	/** @return positive fidelity in this pass; never retain this memo across edits */
	public boolean isFaithful(GeoElement source) {
		return geo(source, 0);
	}

	/** @return whether the source closure is supported by the typed rollback table */
	public static boolean isRecoverableSource(GeoElement source) {
		return new NativeSourceFidelity2D(true).geo(source, 0);
	}

	static NativeSourceFidelity2D restorationPass() {
		return new NativeSourceFidelity2D(true);
	}

	private boolean geo(GeoElement value, int depth) {
		if (value == null || depth > 256 || --remaining < 0) {
			return false;
		}
		Boolean known = memo.get(value);
		if (known != null) {
			return known;
		}
		memo.put(value, false); // Cycles are not a positive reconstruction witness.
		if (value.getClass() == GeoAxis.class) {
			GeoAxis axis = (GeoAxis) value;
			boolean horizontal = value == value.getConstruction().getXAxis();
			boolean vertical = value == value.getConstruction().getYAxis();
			// Built-in axes are reconstructed by the kernel, not by a serialized
			// mutable line origin. Require their whole native constructor geometry.
			boolean exact = (horizontal || vertical)
					&& same(axis.x, horizontal ? 0 : -1) && same(axis.y, horizontal ? 1 : 0)
					&& same(axis.z, 0) && axis.isDefined()
					&& axisPoint(axis.getStartPoint(), 0, 0)
					&& axisPoint(axis.getEndPoint(), horizontal ? 1 : 0, horizontal ? 0 : 1);
			memo.put(value, exact);
			return exact;
		}
		AlgoElement parent = value.getParentAlgorithm();
		boolean expressionProcedure = parent == null
				|| parent.getClass().getSimpleName().startsWith("AlgoDependent");
		if (!(VALUES.contains(value.getClass())
				|| typedMemory && value.getClass() == GeoConicPart.class)
				|| expressionProcedure && !expression(value.getDefinition(), depth + 1)) {
			return false;
		}
		if (parent != null && parent.getClass() == AlgoPointOnPath.class
				&& parent.getInputLength() == 1) {
			if (!(value instanceof GeoPoint)
					|| !(typedMemory ? ((GeoPoint) value).canCaptureNativeRollbackState()
							: ((GeoPoint) value).hasLocallyCoherentNativeConicPointState())) {
				return false;
			}
		} else if (parent != null && !(parent.getClass() == AlgoPointOnPath.class
				&& parent.getInputLength() == 2)
				&& !ALGORITHMS.contains(parent.getClass())) {
			return false;
		}
		if (parent == null && !independent(value)) {
			return false;
		}
		if (parent != null) {
			for (GeoElement input : parent.getInput()) {
				if (!geo(input, depth + 1)) {
					return false;
				}
			}
		}
		if (value instanceof GeoList) {
			GeoList list = (GeoList) value;
			for (int index = 0; index < list.size(); index++) {
				if (!geo(list.get(index), depth + 1)) {
					return false;
				}
			}
		}
		memo.put(value, true);
		return true;
	}

	private static boolean independent(GeoElement value) {
		if (value instanceof GeoNumeric) {
			return !((GeoNumeric) value).isRandom()
					&& serializable(((GeoNumeric) value).getDouble());
		}
		if (value instanceof GeoPoint) {
			GeoPoint point = (GeoPoint) value;
			return serializable(point.x) && serializable(point.y) && serializable(point.z)
					&& (point.z == 1 || Double.isNaN(point.x) && Double.isNaN(point.y)
							&& Double.isNaN(point.z));
		}
		if (value instanceof GeoLine) {
			GeoLine line = (GeoLine) value;
			return line.getStartPoint() == null && line.getEndPoint() == null
					&& serializable(line.x) && serializable(line.y) && serializable(line.z);
		}
		if (value.getClass() == GeoConic.class) {
			GeoConic conic = (GeoConic) value;
			if (!conic.isDefined()) {
				return false;
			}
			double[] matrix = conic.getMatrix().clone();
			for (int index = 0; index < matrix.length; index++) {
				if (!Double.isFinite(matrix[index])) {
					return false;
				}
				// This is exactly XMLStringBuilder.attr(double)'s value encoding.
				int integral = (int) matrix[index];
				matrix[index] = Double.parseDouble(integral == matrix[index]
						? Integer.toString(integral) : Double.toString(matrix[index]));
			}
			GeoConic cold = new GeoConic(new MacroKernel(value.getKernel()).getConstruction());
			cold.setMatrix(matrix);
			return NativePathState2D.capture(conic).matches(cold);
		}
		if (value instanceof GeoVector) {
			GeoVector vector = (GeoVector) value;
			return serializable(vector.x) && serializable(vector.y) && serializable(vector.z);
		}
		return value instanceof GeoList;
	}

	private static boolean serializable(double value) {
		return !Double.isInfinite(value)
				&& Double.doubleToLongBits(value) != Double.doubleToLongBits(-0.0);
	}

	private static boolean same(double first, double second) {
		return Double.doubleToLongBits(first) == Double.doubleToLongBits(second);
	}

	private static boolean axisPoint(GeoPoint point, double x, double y) {
		return point != null && point.isDefined() && same(point.x, x)
				&& same(point.y, y) && same(point.z, 1);
	}

	private boolean expression(ExpressionValue value, int depth) {
		if (value == null) {
			return true;
		}
		if (depth > 256) {
			return false;
		}
		ExpressionValue node = value.unwrap();
		if (node == null) {
			return false;
		}
		if (node instanceof GeoElement) {
			return geo((GeoElement) node, depth + 1);
		}
		if (node.getClass() == FunctionVariable.class) {
			return true;
		}
		if (node.getClass() == MyDouble.class || node.getClass() == MySpecialDouble.class) {
			MyDouble literal = (MyDouble) node;
			if (literal.isAngle() || !serializable(literal.getDouble())) {
				return false;
			}
			try {
				return Double.doubleToLongBits(literal.getDouble()) == Double.doubleToLongBits(
						Double.parseDouble(literal.toString(StringTemplate.xmlTemplate)));
			} catch (NumberFormatException failure) {
				return false;
			}
		}
		if (node.getClass() == ExpressionNode.class
				&& !OPERATIONS.contains(((ExpressionNode) node).getOperation().name())) {
			return false;
		}
		if (node.getClass() != ExpressionNode.class && node.getClass() != MyVecNode.class
				&& node.getClass() != MyList.class && node.getClass() != Equation.class) {
			return false;
		}
		for (int index = 0; index < node.getChildCount(); index++) {
			if (!expression(node.getChild(index), depth + 1)) {
				return false;
			}
		}
		return true;
	}
}
