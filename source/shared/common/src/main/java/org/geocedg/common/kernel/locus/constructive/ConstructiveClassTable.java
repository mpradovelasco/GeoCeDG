/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.DocumentAuthorityRecord;
import org.geogebra.common.kernel.algos.AlgoCirclePointRadius;
import org.geogebra.common.kernel.algos.AlgoCircleTwoPoints;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersect;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.algos.AlgoIntersectLines;
import org.geogebra.common.kernel.algos.AlgoIntersectSingle;
import org.geogebra.common.kernel.algos.AlgoJoinPoints;
import org.geogebra.common.kernel.algos.AlgoJoinPointsRay;
import org.geogebra.common.kernel.algos.AlgoJoinPointsSegment;
import org.geogebra.common.kernel.algos.AlgoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoMidpoint;
import org.geogebra.common.kernel.algos.AlgoOrthoLinePointLine;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.algos.AlgoRotate;
import org.geogebra.common.kernel.algos.AlgoRotatePoint;
import org.geogebra.common.kernel.algos.DependentAlgo;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoAngle;
import org.geogebra.common.kernel.geos.GeoAxis;
import org.geogebra.common.kernel.geos.GeoBoolean;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoLocusND;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoRay;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.plugin.Operation;

/**
 * The class table {@code constructive-class/v1} ({@code locus-v2-constructive-authority.md}
 * section 3.2). It classifies one object by the class of the algorithm that defines it, never by
 * its value, and fixes the structural positions of its inputs. It reads the construction and
 * never changes it: every inherited output keeps its behaviour under every policy (P6).
 *
 * <p>In the class: lines, segments and rays through two points; parallel and perpendicular
 * lines; line-line and line-circle intersections with the inherited selection; midpoints;
 * circles by centre and point and by centre and radius; and points on a path of the class.
 * Free points and numbers are inputs, the axes constants. Everything else is declared boundary:
 * a point or a number becomes a substitute in an exact evaluation, any other object stays
 * outside the class. The rotation of a point is a boundary whose ideal value has a rigorous
 * enclosure. The algorithms of the class keep their tolerance-based internal decisions (category
 * D1); the evaluators replace each of them by a rigorous predicate.</p>
 */
public final class ConstructiveClassTable {

	/** Version of this table. */
	public static final String VERSION = DocumentAuthorityRecord.CLASS_TABLE;

	/** Categories of the declared boundary (section 3.2, D2 to D5, and the rest). */
	public enum BoundaryCategory {
		/** D2: a boolean or number from a geometric test; it carries no integrity. */
		GEOMETRIC_TEST,
		/** D3: a conditional definition. */
		CONDITIONAL_DEFINITION,
		/** D4: a conditioned selection, including continuous-mode intersections. */
		CONDITIONED_SELECTION,
		/** D5: a numerical algorithm with tolerances or iterations. */
		NUMERICAL_ALGORITHM,
		/** An output of a locus, or an object computed from one, under its own contracts. */
		LOCUS_OUTPUT,
		/** Any other algorithm, including a point on a path outside the class. */
		OTHER_ALGORITHM,
		/** An independent object that is neither a point, a number nor an axis. */
		FREE_OBJECT
	}

	private static final Set<Commands> GEOMETRIC_TESTS = EnumSet.of(Commands.Relation,
			Commands.AreEqual, Commands.AreParallel, Commands.ArePerpendicular,
			Commands.AreCollinear, Commands.AreConcyclic, Commands.AreCongruent,
			Commands.IsInRegion, Commands.Prove, Commands.ProveDetails);
	private static final Set<Commands> CONDITIONED_SELECTIONS = EnumSet.of(Commands.Element,
			Commands.Min, Commands.Max, Commands.Sort, Commands.First, Commands.Last,
			Commands.ClosestPoint, Commands.ClosestPointRegion);
	private static final Set<Commands> NUMERICAL_ALGORITHMS = EnumSet.of(Commands.Root,
			Commands.Roots, Commands.NSolve, Commands.NSolutions, Commands.Extremum,
			Commands.ComplexRoot);

	private ConstructiveClassTable() {
	}

	/** The classification of one object. */
	public static final class Classification {
		private final ConstructiveOperation operation;
		private final List<GeoElement> inputs;
		private final int outputIndex;
		private final InheritedLineCircleSelection selection;
		private final boolean outlyingIntersections;
		private final BoundaryCategory category;
		private final String reason;

		private Classification(ConstructiveOperation operation, List<GeoElement> inputs,
				int outputIndex, InheritedLineCircleSelection selection,
				boolean outlyingIntersections, BoundaryCategory category, String reason) {
			this.operation = operation;
			this.inputs = Collections.unmodifiableList(new ArrayList<>(inputs));
			this.outputIndex = outputIndex;
			this.selection = selection;
			this.outlyingIntersections = outlyingIntersections;
			this.category = category;
			this.reason = reason;
		}

		/** @return the operation of the object */
		public ConstructiveOperation getOperation() {
			return operation;
		}

		/** @return the input objects in their structural positions */
		public List<GeoElement> getInputs() {
			return inputs;
		}

		/** @return the output index of an intersection output, otherwise {@code -1} */
		public int getOutputIndex() {
			return outputIndex;
		}

		/**
		 * @return the inherited selection state of a line-circle output, or {@code null} when
		 *         the inherited rule decides it in a way no evaluator reproduces
		 */
		public InheritedLineCircleSelection getSelection() {
			return selection;
		}

		/** @return whether a segment or ray admits intersections outside its bounded part */
		public boolean hasOutlyingIntersections() {
			return outlyingIntersections;
		}

		/** @return the category of a boundary or outside object, otherwise {@code null} */
		public BoundaryCategory getCategory() {
			return category;
		}

		/** @return why the object is boundary or outside, otherwise {@code null} */
		public String getReason() {
			return reason;
		}
	}

	/**
	 * @param geo an object of a construction
	 * @return its classification under {@code constructive-class/v1}
	 */
	public static Classification classify(GeoElement geo) {
		Objects.requireNonNull(geo);
		if (geo.isGeoElement3D()) {
			return boundary(geo, BoundaryCategory.OTHER_ALGORITHM, "three-dimensional object");
		}
		if (isLocusOutput(geo)) {
			return boundary(geo, BoundaryCategory.LOCUS_OUTPUT, "locus");
		}
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent == null) {
			return independent(geo);
		}
		Class<?> type = parent.getClass();
		Class<?> output = geo.getClass();
		if (type == AlgoPointOnPath.class) {
			return pathPoint(geo, (AlgoPointOnPath) parent);
		}
		if (type == AlgoJoinPoints.class && output == GeoLine.class) {
			AlgoJoinPoints join = (AlgoJoinPoints) parent;
			return inClass(ConstructiveOperation.JOIN, join.getP(), join.getQ());
		}
		if (type == AlgoJoinPointsSegment.class && output == GeoSegment.class) {
			AlgoJoinPointsSegment segment = (AlgoJoinPointsSegment) parent;
			return new Classification(ConstructiveOperation.SEGMENT,
					List.of(segment.getP(), segment.getQ()), -1, null,
					((GeoSegment) geo).allowOutlyingIntersections(), null, null);
		}
		if (type == AlgoJoinPointsRay.class && output == GeoRay.class) {
			AlgoJoinPointsRay ray = (AlgoJoinPointsRay) parent;
			return new Classification(ConstructiveOperation.RAY, List.of(ray.getP(), ray.getQ()),
					-1, null, ((GeoRay) geo).allowOutlyingIntersections(), null, null);
		}
		if (type == AlgoLinePointLine.class && output == GeoLine.class) {
			AlgoLinePointLine parallel = (AlgoLinePointLine) parent;
			return inClass(ConstructiveOperation.PARALLEL, parallel.getP(),
					parallel.getInput(1).toGeoElement());
		}
		if (type == AlgoOrthoLinePointLine.class && output == GeoLine.class) {
			AlgoOrthoLinePointLine perpendicular = (AlgoOrthoLinePointLine) parent;
			return inClass(ConstructiveOperation.PERPENDICULAR, perpendicular.getP(),
					perpendicular.getInput(1).toGeoElement());
		}
		if (type == AlgoIntersectLines.class && output == GeoPoint.class) {
			AlgoIntersectLines meet = (AlgoIntersectLines) parent;
			return inClass(ConstructiveOperation.MEET, meet.getg(), meet.geth());
		}
		if (type == AlgoMidpoint.class && output == GeoPoint.class) {
			AlgoMidpoint midpoint = (AlgoMidpoint) parent;
			return inClass(ConstructiveOperation.MIDPOINT, midpoint.getP(), midpoint.getQ());
		}
		if (type == AlgoCircleTwoPoints.class && output == GeoConic.class) {
			AlgoCircleTwoPoints circle = (AlgoCircleTwoPoints) parent;
			return inClass(ConstructiveOperation.CIRCLE_THROUGH,
					circle.getCenter().toGeoElement(), circle.getExternalPoint().toGeoElement());
		}
		if (type == AlgoCirclePointRadius.class && output == GeoConic.class) {
			AlgoCirclePointRadius circle = (AlgoCirclePointRadius) parent;
			return inClass(ConstructiveOperation.CIRCLE_RADIUS,
					circle.getCenter().toGeoElement(), circle.getRadiusGeo());
		}
		if (type == AlgoIntersectLineConic.class && output == GeoPoint.class) {
			AlgoIntersectLineConic algorithm = (AlgoIntersectLineConic) parent;
			return lineCircleOutput(geo, algorithm, outputIndex(algorithm, geo));
		}
		if (type == AlgoIntersectSingle.class && output == GeoPoint.class) {
			return singleIntersection(geo, (AlgoIntersectSingle) parent);
		}
		if ((type == AlgoRotatePoint.class && parent.getInputLength() == 3
				|| type == AlgoRotate.class && parent.getInputLength() == 2)
				&& output == GeoPoint.class
				&& parent.getInput(0).toGeoElement().getClass() == GeoPoint.class) {
			List<GeoElement> inputs = new ArrayList<>();
			for (int index = 0; index < parent.getInputLength(); index++) {
				inputs.add(parent.getInput(index).toGeoElement());
			}
			return new Classification(ConstructiveOperation.ROTATE_POINT, inputs, -1, null,
					false, BoundaryCategory.OTHER_ALGORITHM, type.getSimpleName());
		}
		return boundary(geo, category(parent, geo), type.getSimpleName());
	}

	/**
	 * @param geo an object
	 * @return whether it is a locus, whose relations are governed by the locus contracts
	 */
	static boolean isLocusOutput(GeoElement geo) {
		return geo instanceof GeoLocusV2 || geo instanceof GeoLocusND;
	}

	private static Classification independent(GeoElement geo) {
		Class<?> type = geo.getClass();
		if (type == GeoAxis.class) {
			return inClass(ConstructiveOperation.AXIS);
		}
		if (type == GeoPoint.class) {
			return inClass(ConstructiveOperation.FREE_POINT);
		}
		if (type == GeoNumeric.class || type == GeoAngle.class) {
			return inClass(ConstructiveOperation.FREE_NUMBER);
		}
		return new Classification(ConstructiveOperation.OUTSIDE, List.of(), -1, null, false,
				BoundaryCategory.FREE_OBJECT, "independent " + type.getSimpleName());
	}

	/**
	 * A point on a path: in the class when the path is a line or a circle of the class. A
	 * parametrized point on a path, or a point on any other path, is a declared boundary.
	 */
	private static Classification pathPoint(GeoElement geo, AlgoPointOnPath algorithm) {
		if (geo.getClass() != GeoPoint.class || !((GeoPoint) geo).isPointOnPath()) {
			return boundary(geo, BoundaryCategory.OTHER_ALGORITHM, "AlgoPointOnPath");
		}
		GeoElement path = algorithm.getInput(0).toGeoElement();
		if (algorithm.getInputLength() != 1
				|| ((GeoPoint) geo).getPath().toGeoElement() != path) {
			return boundary(geo, BoundaryCategory.OTHER_ALGORITHM,
					"point on a path with a parameter");
		}
		ConstructiveOperation pathOperation = classify(path).getOperation();
		if (!pathOperation.isClassLine() && !pathOperation.isClassCircle()) {
			return boundary(geo, BoundaryCategory.OTHER_ALGORITHM,
					"point on a path outside the class");
		}
		return inClass(ConstructiveOperation.PATH_POINT, path);
	}

	private static Classification lineCircleOutput(GeoElement geo,
			AlgoIntersectLineConic algorithm, int output) {
		if (algorithm.getKernel().isContinuous()) {
			return boundary(geo, BoundaryCategory.CONDITIONED_SELECTION,
					"continuous-mode intersection");
		}
		return new Classification(ConstructiveOperation.LINE_CIRCLE_OUTPUT,
				List.of(algorithm.getLine(), algorithm.getConic()), output,
				InheritedLineCircleSelection.read(algorithm), false, null, null);
	}

	/** An indexed single intersection selects one output of its source intersection. */
	private static Classification singleIntersection(GeoElement geo,
			AlgoIntersectSingle single) {
		AlgoIntersect source = single.getSourceIntersection();
		if (source == null || source.getClass() != AlgoIntersectLineConic.class) {
			return boundary(geo, BoundaryCategory.OTHER_ALGORITHM, "AlgoIntersectSingle");
		}
		GeoElement index = single.getInputLength() == 3
				? single.getInput(2).toGeoElement() : null;
		if (!(index instanceof GeoNumeric) || index.getParentAlgorithm() != null) {
			return boundary(geo, BoundaryCategory.CONDITIONED_SELECTION,
					index instanceof GeoPoint ? "reference-point selection"
							: "computed intersection index");
		}
		int selected = single.getSelectedIndex();
		if (selected < 0 || selected > 1) {
			return boundary(geo, BoundaryCategory.CONDITIONED_SELECTION,
					"intersection index out of range");
		}
		return lineCircleOutput(geo, (AlgoIntersectLineConic) source, selected);
	}

	private static int outputIndex(AlgoElement parent, GeoElement output) {
		for (int index = 0; index < parent.getOutputLength(); index++) {
			if (parent.getOutput(index) == output) {
				return index;
			}
		}
		throw new IllegalStateException("The object is not an output of its parent algorithm");
	}

	private static BoundaryCategory category(AlgoElement parent, GeoElement geo) {
		for (int index = 0; index < parent.getInputLength(); index++) {
			if (isLocusOutput(parent.getInput(index).toGeoElement())) {
				return BoundaryCategory.LOCUS_OUTPUT;
			}
		}
		Object command = parent.getClassName();
		if (command == Commands.If || parent instanceof DependentAlgo
				&& containsCondition(((DependentAlgo) parent).getExpression())) {
			return BoundaryCategory.CONDITIONAL_DEFINITION;
		}
		if (GEOMETRIC_TESTS.contains(command) || geo instanceof GeoBoolean) {
			return BoundaryCategory.GEOMETRIC_TEST;
		}
		if (CONDITIONED_SELECTIONS.contains(command)) {
			return BoundaryCategory.CONDITIONED_SELECTION;
		}
		if (NUMERICAL_ALGORITHMS.contains(command)) {
			return BoundaryCategory.NUMERICAL_ALGORITHM;
		}
		return BoundaryCategory.OTHER_ALGORITHM;
	}

	private static boolean containsCondition(ExpressionNode expression) {
		return expression != null && expression.any(value -> value instanceof ExpressionNode
				&& isCondition(((ExpressionNode) value).getOperation()));
	}

	private static boolean isCondition(Operation operation) {
		return operation == Operation.IF || operation == Operation.IF_SHORT
				|| operation == Operation.IF_ELSE || operation == Operation.IF_LIST;
	}

	private static Classification inClass(ConstructiveOperation operation,
			GeoElement... inputs) {
		return new Classification(operation, List.of(inputs), -1, null, false, null, null);
	}

	private static Classification boundary(GeoElement geo, BoundaryCategory category,
			String reason) {
		ConstructiveOperation operation;
		if (geo.getClass() == GeoPoint.class) {
			operation = ConstructiveOperation.BOUNDARY_POINT;
		} else if (geo.getClass() == GeoNumeric.class || geo.getClass() == GeoAngle.class) {
			operation = ConstructiveOperation.BOUNDARY_NUMBER;
		} else {
			operation = ConstructiveOperation.OUTSIDE;
		}
		return new Classification(operation, List.of(), -1, null, false, category, reason);
	}
}
