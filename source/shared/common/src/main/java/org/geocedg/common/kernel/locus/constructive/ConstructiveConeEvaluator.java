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
import java.util.TreeSet;

import org.geocedg.common.kernel.locus.constructive.ExactQuadraticTower.Element;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;

/**
 * Evaluation of a constructive cone in one number system
 * ({@code locus-v2-constructive-authority.md} sections 1 to 3; E1 implementation design section
 * 6). In the exact number system it is the exact evaluation M3 in the constructive
 * configuration: free inputs and points on paths by their definitions, declared boundary points
 * and numbers by their binary64 substitutes, every sign decided exactly. In the interval number
 * system it encloses the ideal values rigorously, the rotation of a point included, and declares
 * a sign only when it is certified; a boundary object without a rigorous enclosure leaves its
 * consumers without a value.
 *
 * <p>Every tolerance-based decision of an algorithm of the class (category D1) is replaced by a
 * strict sign: distinct points, nonparallel lines, a positive discriminant, a positive radius,
 * the membership of a limited path, the clipping of a point on a limited path. A sign that is
 * zero where the ideal object needs it nonzero makes the object invalid; a sign that the number
 * system cannot decide fails closed. Every line-circle output is the one that the inherited
 * selection rule gives with the captured selection state. Outside a substituted driver slice,
 * when requested, the live algorithm is checked against the constructive decision: a live object
 * that is undefined where the constructive one exists, or a live line-circle output that is not
 * strictly closer to the selected root than to the other, fails closed; the tolerance is never
 * accepted. Inside the slice, the captured selection state governs and an unlabelled second
 * output must lie outside the inherited coincidence band of class v3.</p>
 *
 * <p>The decisions of validity, branch and membership are recorded in evaluation order: they are
 * the sign vector, the operational cell of {@code SUBSTITUTE_GENERIC}. The checks against the
 * live algorithm are recorded apart. Nothing is persisted and the construction is never read.</p>
 *
 * @param <T> the type of a real value of the number system
 */
public final class ConstructiveConeEvaluator<T> {

	/**
	 * Conservative band of an unlabelled second output, the window factor and the rounding
	 * term of class v3 ({@code locus-v2-certified-construction-model.md} section 10.3).
	 */
	static final double COINCIDENT_WINDOW_FACTOR = 1E-3;
	/** Rounding term of the band of an unlabelled second output (section 10.3). */
	static final double COINCIDENT_ROUNDING_FACTOR = 1E-10;

	/** Typed reason why a node has no value. */
	public enum Failure {
		/** A validity predicate is false: the constructive object does not exist. */
		INVALID,
		/** The number system does not decide a sign the evaluation needs. */
		UNDECIDED,
		/** The inherited selection of a line-circle output is not reproduced. */
		SELECTION_NOT_RECONSTRUCTED,
		/** The node or one it reads is outside what this number system evaluates. */
		OUT_OF_CLASS,
		/** The live algorithm is undefined where the constructive object exists. */
		LIVE_DECISION_DISAGREES,
		/** A live line-circle output is not certified closer to the selected root. */
		BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED,
		/** A boundary substitute or a free input has no finite binary64 value. */
		SUBSTITUTE_UNDEFINED,
		/** The inherited path map does not define the ideal point. */
		PATH_MAP_UNDEFINED,
		/** The versioned budget of the exact evaluation is exhausted. */
		BUDGET_EXHAUSTED
	}

	/** Outcome of an incidence of a point with a target. */
	public enum Incidence {
		/** The point lies on the target, and on its limited part when it has one. */
		HOLDS,
		/** The residual of the point on the target support is nonzero. */
		REFUTED,
		/** The point lies on the support but outside the limited part of the target. */
		NOT_ON_LIMITED_TARGET,
		/** The incidence is not decided; the failure says why. */
		NOT_DECIDED
	}

	/** A point of the plane. */
	public static final class Point<T> {
		private final T x;
		private final T y;

		Point(T x, T y) {
			this.x = x;
			this.y = y;
		}

		/** @return the abscissa */
		public T getX() {
			return x;
		}

		/** @return the ordinate */
		public T getY() {
			return y;
		}
	}

	/**
	 * A line {@code a x + b y + c = 0}; a segment or a ray also has its start and its direction
	 * {@code (b, -a)}, the vector from its start to its end for a segment.
	 */
	public static final class Line<T> {
		private final T a;
		private final T b;
		private final T c;
		private final boolean limited;
		private final boolean ray;
		private final boolean outlying;
		private final T startX;
		private final T startY;

		Line(T a, T b, T c, boolean limited, boolean ray, boolean outlying, T startX,
				T startY) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.limited = limited;
			this.ray = ray;
			this.outlying = outlying;
			this.startX = startX;
			this.startY = startY;
		}

		/** @return the coefficient of {@code x} */
		public T getA() {
			return a;
		}

		/** @return the coefficient of {@code y} */
		public T getB() {
			return b;
		}

		/** @return the constant term */
		public T getC() {
			return c;
		}

		/** @return whether it is a segment or a ray */
		public boolean isLimited() {
			return limited;
		}

		/** @return whether it is a ray */
		public boolean isRay() {
			return ray;
		}

		/** @return whether its intersections ignore its limited part */
		public boolean hasOutlyingIntersections() {
			return outlying;
		}

		/** @return the abscissa of the start of a segment or ray, otherwise {@code null} */
		public T getStartX() {
			return startX;
		}

		/** @return the ordinate of the start of a segment or ray, otherwise {@code null} */
		public T getStartY() {
			return startY;
		}
	}

	/** A circle by its centre and squared radius. */
	public static final class Circle<T> {
		private final T centerX;
		private final T centerY;
		private final T radiusSquared;

		Circle(T centerX, T centerY, T radiusSquared) {
			this.centerX = centerX;
			this.centerY = centerY;
			this.radiusSquared = radiusSquared;
		}

		/** @return the abscissa of the centre */
		public T getCenterX() {
			return centerX;
		}

		/** @return the ordinate of the centre */
		public T getCenterY() {
			return centerY;
		}

		/** @return the squared radius */
		public T getRadiusSquared() {
			return radiusSquared;
		}
	}

	/** One decided sign. */
	public static final class Decision {
		private final int node;
		private final String predicate;
		private final int sign;

		Decision(int node, String predicate, int sign) {
			this.node = node;
			this.predicate = predicate;
			this.sign = sign;
		}

		/** @return the node that needed the decision */
		public int getNode() {
			return node;
		}

		/** @return the predicate */
		public String getPredicate() {
			return predicate;
		}

		/** @return its sign */
		public int getSign() {
			return sign;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Decision)) {
				return false;
			}
			Decision that = (Decision) other;
			return node == that.node && sign == that.sign && predicate.equals(that.predicate);
		}

		@Override
		public int hashCode() {
			return Objects.hash(node, predicate, sign);
		}

		@Override
		public String toString() {
			return "n" + node + ":" + predicate + "=" + sign;
		}
	}

	/** The value of one node, or the typed reason why it has none. */
	public static final class Result {
		private final Object value;
		private final Failure failure;
		private final int failureNode;
		private final String message;

		Result(Object value, Failure failure, int failureNode, String message) {
			this.value = value;
			this.failure = failure;
			this.failureNode = failureNode;
			this.message = message;
		}

		/** @return whether the node has a value */
		public boolean hasValue() {
			return failure == null;
		}

		/** @return a {@link Point}, {@link Line}, {@link Circle} or a real, or {@code null} */
		public Object getValue() {
			return value;
		}

		/** @return the failure, or {@code null} */
		public Failure getFailure() {
			return failure;
		}

		/** @return the node where the failure arose, or {@code -1} */
		public int getFailureNode() {
			return failureNode;
		}

		/** @return a description of the failure, or {@code null} */
		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {
			return failure == null ? "value" : failure + " at n" + failureNode + ": " + message;
		}
	}

	/** The decision of an incidence of a point with a target. */
	public static final class IncidenceResult {
		private final Incidence incidence;
		private final Result failure;
		private final List<Integer> substitutes;

		IncidenceResult(Incidence incidence, Result failure, List<Integer> substitutes) {
			this.incidence = incidence;
			this.failure = failure;
			this.substitutes = Collections.unmodifiableList(new ArrayList<>(substitutes));
		}

		/** @return the outcome */
		public Incidence getIncidence() {
			return incidence;
		}

		/** @return the failure of an undecided incidence, or {@code null} */
		public Result getFailure() {
			return failure;
		}

		/**
		 * @return the boundary substitutes the decision used, in cone order; a decision with
		 *         substitutes holds for the substitutes and, by itself, never for the ideal
		 *         objects
		 */
		public List<Integer> getSubstitutes() {
			return substitutes;
		}

		@Override
		public String toString() {
			return incidence + (failure == null ? "" : " (" + failure + ")")
					+ (substitutes.isEmpty() ? "" : " substitutes " + substitutes);
		}
	}

	private final ConstructiveCone cone;
	private final ConstructiveArithmetic<T> arithmetic;
	private final boolean checkLive;
	private final Map<Integer, ExactRational2D[]> overrides;
	private final Map<Integer, Object> fixed;
	private final Object[] values;
	private final Result[] failures;
	private final boolean[] done;
	private final boolean[] substitute;
	private final List<Decision> decisions = new ArrayList<>();
	private final List<Decision> liveChecks = new ArrayList<>();
	private final Map<Long, RawRoots<T>> rawRoots = new HashMap<>();
	private Result exhausted;

	/**
	 * @param cone the cone
	 * @param arithmetic the number system
	 * @param checkLive whether the live algorithm is checked outside a substituted driver slice
	 * @param substitutes exact values that replace the binary64 substitutes of some boundary
	 *        nodes, by node index: two coordinates of a point or one number
	 */
	public ConstructiveConeEvaluator(ConstructiveCone cone, ConstructiveArithmetic<T> arithmetic,
			boolean checkLive, Map<Integer, ExactRational2D[]> substitutes) {
		this(cone, arithmetic, checkLive, substitutes, Collections.emptyMap());
	}

	/**
	 * @param cone the cone
	 * @param arithmetic the number system
	 * @param checkLive whether the live algorithm is checked outside a substituted driver slice
	 * @param substitutes exact values that replace the binary64 substitutes of some boundary
	 *        nodes, by node index: two coordinates of a point or one number
	 * @param values values of the number system that some nodes take instead of their own,
	 *        by node index: a {@link Point}, {@link Line}, {@link Circle} or a real; the
	 *        ancestors of such a node are not evaluated for it
	 */
	public ConstructiveConeEvaluator(ConstructiveCone cone, ConstructiveArithmetic<T> arithmetic,
			boolean checkLive, Map<Integer, ExactRational2D[]> substitutes,
			Map<Integer, Object> values) {
		this.cone = Objects.requireNonNull(cone);
		this.arithmetic = Objects.requireNonNull(arithmetic);
		this.checkLive = checkLive;
		this.overrides = new HashMap<>(substitutes);
		this.fixed = new HashMap<>(values);
		this.values = new Object[cone.size()];
		failures = new Result[cone.size()];
		done = new boolean[cone.size()];
		substitute = new boolean[cone.size()];
	}

	/**
	 * @param cone the cone
	 * @param budget the budget of this exact evaluation
	 * @return the exact evaluation M3 at the base configuration, with the live checks
	 */
	public static ConstructiveConeEvaluator<Element> exact(ConstructiveCone cone,
			ExactEvaluationBudget budget) {
		return new ConstructiveConeEvaluator<>(cone,
				ConstructiveArithmetic.exact(new ExactQuadraticTower(budget)), true,
				Collections.emptyMap());
	}

	/**
	 * @param cone the cone
	 * @return the rigorous enclosure of the ideal values, with the live checks
	 */
	public static ConstructiveConeEvaluator<OutwardInterval> intervals(ConstructiveCone cone) {
		return new ConstructiveConeEvaluator<>(cone, ConstructiveArithmetic.intervals(), true,
				Collections.emptyMap());
	}

	/** @return the number system */
	public ConstructiveArithmetic<T> getArithmetic() {
		return arithmetic;
	}

	/**
	 * @return the deterministic counters of an exact evaluation, or {@code null} for an
	 *         interval evaluation
	 */
	public ExactEvaluationCounters getExactCounters() {
		return arithmetic instanceof ConstructiveArithmetic.Exact
				? ((ConstructiveArithmetic.Exact) arithmetic).getTower().counters() : null;
	}

	/**
	 * @param node node index
	 * @return its value, or the failure of the first node of its cone that has none
	 */
	public Result evaluate(int node) {
		ensure(node);
		return failures[node] != null ? failures[node]
				: new Result(values[node], null, -1, null);
	}

	/**
	 * Decides whether a point lies on a target: a line, a segment or ray (also on its limited
	 * part, unless it admits outlying intersections) or a circle.
	 *
	 * @param point node of the point
	 * @param target node of the target
	 * @return the decision with the substitutes it used
	 */
	public IncidenceResult incidence(int point, int target) {
		ensure(point);
		ensure(target);
		List<Integer> used = substitutesOf(point, target);
		for (int node : new int[] {point, target}) {
			if (failures[node] != null) {
				return new IncidenceResult(Incidence.NOT_DECIDED, failures[node], used);
			}
		}
		if (!(values[point] instanceof Point)) {
			return notDecided(Failure.OUT_OF_CLASS, point, "the subject is not a point", used);
		}
		@SuppressWarnings("unchecked")
		Point<T> p = (Point<T>) values[point];
		try {
			if (values[target] instanceof Line) {
				@SuppressWarnings("unchecked")
				Line<T> line = (Line<T>) values[target];
				int residual = signOf(target, residual(line, p));
				if (residual != 0) {
					return new IncidenceResult(Incidence.REFUTED, null, used);
				}
				if (line.limited && !line.outlying && !onLimitedPart(line, p)) {
					return new IncidenceResult(Incidence.NOT_ON_LIMITED_TARGET, null, used);
				}
				return new IncidenceResult(Incidence.HOLDS, null, used);
			}
			if (values[target] instanceof Circle) {
				@SuppressWarnings("unchecked")
				Circle<T> circle = (Circle<T>) values[target];
				T dx = sub(p.x, circle.centerX);
				T dy = sub(p.y, circle.centerY);
				int residual = signOf(target, sub(add(mul(dx, dx), mul(dy, dy)),
						circle.radiusSquared));
				return new IncidenceResult(residual == 0 ? Incidence.HOLDS : Incidence.REFUTED,
						null, used);
			}
			return notDecided(Failure.OUT_OF_CLASS, target, "the target is not a line or circle",
					used);
		} catch (Stop stop) {
			return notDecided(stop.failure, stop.node, stop.getMessage(), used);
		} catch (ExactEvaluationBudgetExhaustedException exhaustion) {
			exhausted = new Result(null, Failure.BUDGET_EXHAUSTED, target,
					exhaustion.getMessage());
			return new IncidenceResult(Incidence.NOT_DECIDED, exhausted, used);
		}
	}

	/** @return the decided signs of validity, branch and membership, in evaluation order */
	public List<Decision> getDecisions() {
		return Collections.unmodifiableList(new ArrayList<>(decisions));
	}

	/** @return the checks against the live algorithm, in evaluation order */
	public List<Decision> getLiveChecks() {
		return Collections.unmodifiableList(new ArrayList<>(liveChecks));
	}

	/**
	 * @param node node index
	 * @return whether its value was computed from a boundary substitute
	 */
	public boolean dependsOnSubstitute(int node) {
		ensure(node);
		for (int ancestor : ancestors(node)) {
			if (substitute[ancestor]) {
				return true;
			}
		}
		return false;
	}

	private IncidenceResult notDecided(Failure failure, int node, String message,
			List<Integer> used) {
		return new IncidenceResult(Incidence.NOT_DECIDED,
				new Result(null, failure, node, message), used);
	}

	private List<Integer> substitutesOf(int... nodes) {
		TreeSet<Integer> used = new TreeSet<>();
		for (int node : nodes) {
			for (int ancestor : ancestors(node)) {
				if (substitute[ancestor]) {
					used.add(ancestor);
				}
			}
		}
		return new ArrayList<>(used);
	}

	/** @return the node and all its ancestors in the cone, in increasing order */
	private List<Integer> ancestors(int node) {
		boolean[] seen = new boolean[cone.size()];
		Deque<Integer> stack = new ArrayDeque<>();
		stack.push(node);
		List<Integer> result = new ArrayList<>();
		while (!stack.isEmpty()) {
			int current = stack.pop();
			if (seen[current]) {
				continue;
			}
			seen[current] = true;
			result.add(current);
			if (fixed.containsKey(current)) {
				continue;
			}
			ConstructiveConeNode coneNode = cone.getNode(current);
			for (int position = 0; position < coneNode.getInputCount(); position++) {
				stack.push(coneNode.getInput(position));
			}
		}
		Collections.sort(result);
		return result;
	}

	private void ensure(int node) {
		if (done[node]) {
			return;
		}
		for (int current : ancestors(node)) {
			if (!done[current]) {
				compute(current);
			}
		}
	}

	private void compute(int index) {
		done[index] = true;
		if (exhausted != null) {
			failures[index] = exhausted;
			return;
		}
		if (fixed.containsKey(index)) {
			values[index] = fixed.get(index);
			return;
		}
		ConstructiveConeNode node = cone.getNode(index);
		for (int position = 0; position < node.getInputCount(); position++) {
			Result input = failures[node.getInput(position)];
			if (input != null) {
				failures[index] = input;
				return;
			}
		}
		try {
			values[index] = apply(node);
		} catch (Stop stop) {
			failures[index] = new Result(null, stop.failure, stop.node, stop.getMessage());
		} catch (ExactEvaluationBudgetExhaustedException exhaustion) {
			exhausted = new Result(null, Failure.BUDGET_EXHAUSTED, index, exhaustion.getMessage());
			failures[index] = exhausted;
		}
	}

	private Object apply(ConstructiveConeNode node) {
		int index = node.getIndex();
		switch (node.getOperation()) {
		case FREE_POINT:
			return livePoint(node, "free point");
		case FREE_NUMBER:
			return liveNumber(node, "free number");
		case AXIS:
			return new Line<>(binary(node, 0), binary(node, 1), binary(node, 2), false, false,
					false, null, null);
		case PATH_POINT:
			return pathPoint(node);
		case JOIN:
		case SEGMENT:
		case RAY:
			return join(node);
		case PARALLEL: {
			Point<T> p = point(node, 0);
			Line<T> l = line(node, 1);
			T c = arithmetic.negate(add(mul(l.a, p.x), mul(l.b, p.y)));
			return live(node, new Line<>(l.a, l.b, c, false, false, false, null, null));
		}
		case PERPENDICULAR: {
			Point<T> p = point(node, 0);
			Line<T> l = line(node, 1);
			return live(node, new Line<>(arithmetic.negate(l.b), l.a,
					sub(mul(p.x, l.b), mul(p.y, l.a)), false, false, false, null, null));
		}
		case MEET:
			return meet(node);
		case LINE_CIRCLE_OUTPUT:
			return lineCircleOutput(node);
		case MIDPOINT: {
			Point<T> p = point(node, 0);
			Point<T> q = point(node, 1);
			T half = arithmetic.ofBinary64(0.5);
			return live(node, new Point<>(mul(add(p.x, q.x), half), mul(add(p.y, q.y), half)));
		}
		case CIRCLE_THROUGH: {
			Point<T> center = point(node, 0);
			Point<T> through = point(node, 1);
			T dx = sub(through.x, center.x);
			T dy = sub(through.y, center.y);
			T squared = add(mul(dx, dx), mul(dy, dy));
			requirePositive(index, "radius", squared);
			return live(node, new Circle<>(center.x, center.y, squared));
		}
		case CIRCLE_RADIUS:
			return circleRadius(node);
		case ROTATE_POINT:
			return arithmetic.isExact() ? substitute(node) : rotation(node);
		case BOUNDARY_POINT:
		case BOUNDARY_NUMBER:
			if (!arithmetic.isExact()) {
				throw new Stop(Failure.OUT_OF_CLASS, index,
						"a boundary object without a rigorous enclosure");
			}
			return substitute(node);
		default:
			throw new Stop(Failure.OUT_OF_CLASS, index, "an object outside the class: "
					+ node.getReason());
		}
	}

	private Line<T> join(ConstructiveConeNode node) {
		Point<T> p = point(node, 0);
		Point<T> q = point(node, 1);
		T a = sub(p.y, q.y);
		T b = sub(q.x, p.x);
		T c = sub(mul(p.x, q.y), mul(p.y, q.x));
		requirePositive(node.getIndex(), "distinct-points", add(mul(a, a), mul(b, b)));
		boolean limited = node.getOperation().isBounded();
		return live(node, new Line<>(a, b, c, limited,
				node.getOperation() == ConstructiveOperation.RAY, node.hasOutlyingIntersections(),
				limited ? p.x : null, limited ? p.y : null));
	}

	private Point<T> meet(ConstructiveConeNode node) {
		int index = node.getIndex();
		Line<T> g = line(node, 0);
		Line<T> h = line(node, 1);
		T x = sub(mul(g.b, h.c), mul(g.c, h.b));
		T y = sub(mul(g.c, h.a), mul(g.a, h.c));
		T z = sub(mul(g.a, h.b), mul(g.b, h.a));
		if (decide(index, "nonparallel", z) == 0) {
			throw new Stop(Failure.INVALID, index, "parallel lines");
		}
		Point<T> point = new Point<>(arithmetic.divide(x, z), arithmetic.divide(y, z));
		for (int position = 0; position < 2; position++) {
			Line<T> line = position == 0 ? g : h;
			if (line.limited && !line.outlying
					&& !member(index, "meet-member-" + position, line, point)) {
				throw new Stop(Failure.INVALID, index, "the point is not on the limited line");
			}
		}
		return live(node, point);
	}

	/** A circle by centre and radius: a positive number, or the length of a segment. */
	private Circle<T> circleRadius(ConstructiveConeNode node) {
		int index = node.getIndex();
		Point<T> center = point(node, 0);
		ConstructiveOperation radiusOperation = cone.getNode(node.getInput(1)).getOperation();
		T squared;
		if (radiusOperation == ConstructiveOperation.SEGMENT) {
			Line<T> segment = line(node, 1);
			squared = add(mul(segment.a, segment.a), mul(segment.b, segment.b));
			requirePositive(index, "radius", squared);
		} else if (radiusOperation.getShape() == ConstructiveOperation.Shape.NUMBER) {
			@SuppressWarnings("unchecked")
			T radius = (T) values[node.getInput(1)];
			requirePositive(index, "radius", radius);
			squared = mul(radius, radius);
		} else {
			throw new Stop(Failure.OUT_OF_CLASS, index, "a radius that is not a number or segment");
		}
		return live(node, new Circle<>(center.x, center.y, squared));
	}

	/** One output of a line-circle intersection with the inherited selection rule. */
	private Point<T> lineCircleOutput(ConstructiveConeNode node) {
		int index = node.getIndex();
		InheritedLineCircleSelection selection = node.getSelection();
		if (selection == null) {
			throw new Stop(Failure.SELECTION_NOT_RECONSTRUCTED, index,
					"the inherited selection state is not reconstructible");
		}
		Line<T> line = line(node, 0);
		Circle<T> circle = circle(node, 1);
		RawRoots<T> roots = rawRoots(node, line, circle);
		int output = node.getOutputIndex();
		boolean limited = line.limited && !line.outlying;
		int selected;
		if (selection.isCompaction()) {
			selected = -1;
			int count = 0;
			for (int raw = 0; raw < 2 && selected < 0; raw++) {
				if (!limited || member(index, "compaction-member-" + raw, line, roots.roots[raw])) {
					if (count == output) {
						selected = raw;
					}
					count++;
				}
			}
			if (selected < 0) {
				throw new Stop(Failure.INVALID, index,
						"fewer roots lie on the path than the index");
			}
		} else {
			selected = selection.getRawRoot(output);
			if (limited && !member(index, "selected-member", line, roots.roots[selected])) {
				throw new Stop(Failure.INVALID, index, "the selected root is not on the path");
			}
		}
		if (node.isDriverSlice() && output == 1 && !selection.isLabelled(1)) {
			requireOutsideCoincidenceBand(index, line, circle, roots);
		}
		Point<T> result = roots.roots[selected];
		if (checkLive && !node.isDriverSlice()) {
			requireLiveDefined(node);
			requireCorrespondence(node, result, roots.roots[1 - selected]);
		}
		return result;
	}

	/**
	 * The two raw roots of a line and a circle, ordered by their parameter along the direction
	 * {@code (b, -a)} of the line, computed once for each pair of input nodes.
	 */
	private RawRoots<T> rawRoots(ConstructiveConeNode node, Line<T> line, Circle<T> circle) {
		long key = (long) node.getInput(0) * cone.size() + node.getInput(1);
		RawRoots<T> known = rawRoots.get(key);
		if (known != null) {
			return known;
		}
		int index = node.getIndex();
		T norm = add(mul(line.a, line.a), mul(line.b, line.b));
		T distance = add(add(mul(line.a, circle.centerX), mul(line.b, circle.centerY)), line.c);
		T discriminant = sub(mul(circle.radiusSquared, norm), mul(distance, distance));
		int sign = decide(index, "discriminant", discriminant);
		if (sign < 0) {
			throw new Stop(Failure.INVALID, index, "the line does not meet the circle");
		}
		if (sign == 0) {
			throw new Stop(Failure.SELECTION_NOT_RECONSTRUCTED, index, "coincident exact roots");
		}
		T root = arithmetic.sqrt(discriminant);
		T footX = sub(circle.centerX, arithmetic.divide(mul(distance, line.a), norm));
		T footY = sub(circle.centerY, arithmetic.divide(mul(distance, line.b), norm));
		T offsetX = arithmetic.divide(mul(root, line.b), norm);
		T offsetY = arithmetic.negate(arithmetic.divide(mul(root, line.a), norm));
		@SuppressWarnings("unchecked")
		Point<T>[] roots = new Point[] {new Point<>(sub(footX, offsetX), sub(footY, offsetY)),
				new Point<>(add(footX, offsetX), add(footY, offsetY))};
		RawRoots<T> result = new RawRoots<>(roots, discriminant, distance, norm);
		rawRoots.put(key, result);
		return result;
	}

	/**
	 * Section 10.3 of the certified model: the inherited suppression may undefine an unlabelled
	 * second output when its roots nearly coincide; inside a substituted slice no live output
	 * exists to check, so the output must lie outside the conservative band of class v3.
	 */
	private void requireOutsideCoincidenceBand(int index, Line<T> line, Circle<T> circle,
			RawRoots<T> roots) {
		T window = arithmetic.ofBinary64(COINCIDENT_WINDOW_FACTOR);
		List<T> terms = List.of(mul(line.a, line.a), mul(line.b, line.b), mul(line.c, line.c));
		boolean outside = true;
		for (int term = 0; term < terms.size() && outside; term++) {
			outside = decideOrSelection(index, "band-window-" + term,
					sub(roots.discriminant, mul(window, terms.get(term)))) > 0;
		}
		T rounding = add(mul(roots.distance, roots.distance),
				mul(circle.radiusSquared, roots.norm));
		if (outside) {
			outside = decideOrSelection(index, "band-rounding", sub(roots.discriminant,
					mul(arithmetic.ofBinary64(COINCIDENT_ROUNDING_FACTOR), rounding))) > 0;
		}
		if (!outside) {
			throw new Stop(Failure.SELECTION_NOT_RECONSTRUCTED, index,
					"an unlabelled second output inside the coincidence band");
		}
	}

	private int decideOrSelection(int index, String predicate, T value) {
		int sign = arithmetic.sign(value);
		if (sign == ConstructiveArithmetic.UNDECIDED) {
			throw new Stop(Failure.SELECTION_NOT_RECONSTRUCTED, index,
					predicate + " is not decided");
		}
		decisions.add(new Decision(index, predicate, sign));
		return sign;
	}

	/**
	 * The point of the ideal path that the inherited path map assigns to the persisted
	 * coordinates: the orthogonal projection on a line, clipped to the limited part of a
	 * segment or a ray, and the radial projection on a circle.
	 */
	private Point<T> pathPoint(ConstructiveConeNode node) {
		int index = node.getIndex();
		if (node.isDriverSlice()) {
			throw new Stop(Failure.OUT_OF_CLASS, index,
					"a point on a path that moves with the driver");
		}
		Point<T> persisted = livePoint(node, "persisted point on a path");
		Object path = values[node.getInput(0)];
		if (path instanceof Line) {
			@SuppressWarnings("unchecked")
			Line<T> line = (Line<T>) path;
			T norm = add(mul(line.a, line.a), mul(line.b, line.b));
			T t = arithmetic.negate(arithmetic.divide(add(add(line.c, mul(line.a, persisted.x)),
					mul(line.b, persisted.y)), norm));
			Point<T> projection = new Point<>(add(persisted.x, mul(t, line.a)),
					add(persisted.y, mul(t, line.b)));
			if (!line.limited) {
				return projection;
			}
			T parameter = parameter(line, projection);
			if (decide(index, "path-start", parameter) < 0) {
				return new Point<>(line.startX, line.startY);
			}
			if (!line.ray && decide(index, "path-end", sub(norm, parameter)) < 0) {
				return new Point<>(add(line.startX, line.b),
						sub(line.startY, line.a));
			}
			return projection;
		}
		if (path instanceof Circle) {
			@SuppressWarnings("unchecked")
			Circle<T> circle = (Circle<T>) path;
			T dx = sub(persisted.x, circle.centerX);
			T dy = sub(persisted.y, circle.centerY);
			T squared = add(mul(dx, dx), mul(dy, dy));
			if (decide(index, "path-off-centre", squared) == 0) {
				throw new Stop(Failure.PATH_MAP_UNDEFINED, index,
						"a point on a circle path at its centre");
			}
			T scale = arithmetic.sqrt(arithmetic.divide(circle.radiusSquared, squared));
			return new Point<>(add(circle.centerX, mul(dx, scale)),
					add(circle.centerY, mul(dy, scale)));
		}
		throw new Stop(Failure.OUT_OF_CLASS, index, "a path outside the class");
	}

	private Point<T> rotation(ConstructiveConeNode node) {
		int index = node.getIndex();
		Point<T> p = point(node, 0);
		ConstructiveConeNode angleNode = cone.getNode(node.getInput(1));
		if (angleNode.getOperation() != ConstructiveOperation.FREE_NUMBER) {
			throw new Stop(Failure.OUT_OF_CLASS, index,
					"a rotation by an angle that is not a free number");
		}
		double angle = liveValue(angleNode, 0, "angle");
		T cos = arithmetic.cos(angle);
		T sin = arithmetic.sin(angle);
		if (cos == null || sin == null) {
			throw new Stop(Failure.OUT_OF_CLASS, index, "no rigorous trigonometric value");
		}
		T zero = arithmetic.ofBinary64(0);
		Point<T> center = node.getInputCount() > 2 ? point(node, 2) : new Point<>(zero, zero);
		T dx = sub(p.x, center.x);
		T dy = sub(p.y, center.y);
		Point<T> rotated = new Point<>(add(center.x, sub(mul(cos, dx), mul(sin, dy))),
				add(center.y, add(mul(sin, dx), mul(cos, dy))));
		if (checkLive && !node.isDriverSlice()) {
			requireLiveDefined(node);
		}
		return rotated;
	}

	/** A boundary substitute: its binary64 state or the exact value that replaces it. */
	private Object substitute(ConstructiveConeNode node) {
		int index = node.getIndex();
		if (node.isDriverSlice()) {
			throw new Stop(Failure.OUT_OF_CLASS, index,
					"a boundary object that moves with the driver has no substitute");
		}
		substitute[index] = true;
		ExactRational2D[] replaced = overrides.get(index);
		boolean point = node.getOperation().getShape() == ConstructiveOperation.Shape.POINT;
		if (replaced != null) {
			return point ? new Point<>(arithmetic.ofRational(replaced[0]),
					arithmetic.ofRational(replaced[1])) : arithmetic.ofRational(replaced[0]);
		}
		return point ? livePoint(node, "boundary point") : liveNumber(node, "boundary number");
	}

	private Point<T> livePoint(ConstructiveConeNode node, String what) {
		double x = liveValue(node, 0, what);
		double y = liveValue(node, 1, what);
		double z = liveValue(node, 2, what);
		if (z == 0) {
			throw new Stop(Failure.SUBSTITUTE_UNDEFINED, node.getIndex(), what + " at infinity");
		}
		ExactRational2D denominator = ExactRational2D.ofBinary64(z);
		return new Point<>(arithmetic.ofRational(ExactRational2D.ofBinary64(x).divide(denominator)),
				arithmetic.ofRational(ExactRational2D.ofBinary64(y).divide(denominator)));
	}

	private T liveNumber(ConstructiveConeNode node, String what) {
		return arithmetic.ofBinary64(liveValue(node, 0, what));
	}

	private T binary(ConstructiveConeNode node, int position) {
		return arithmetic.ofBinary64(liveValue(node, position, "constant"));
	}

	private static double liveValue(ConstructiveConeNode node, int position, String what) {
		double[] state = node.getLiveState();
		if (!node.isLiveDefined() || state.length <= position
				|| !Double.isFinite(state[position])) {
			throw new Stop(Failure.SUBSTITUTE_UNDEFINED, node.getIndex(), what
					+ " without a finite binary64 value");
		}
		return state[position];
	}

	/** Outside a substituted slice, a live object must be defined where the ideal one is. */
	private <V> V live(ConstructiveConeNode node, V value) {
		if (checkLive && !node.isDriverSlice()) {
			requireLiveDefined(node);
		}
		return value;
	}

	private void requireLiveDefined(ConstructiveConeNode node) {
		if (!node.isLiveDefined()) {
			liveChecks.add(new Decision(node.getIndex(), "live-defined", 0));
			throw new Stop(Failure.LIVE_DECISION_DISAGREES, node.getIndex(),
					"the live object is undefined where the constructive one exists");
		}
		liveChecks.add(new Decision(node.getIndex(), "live-defined", 1));
	}

	/** The live output must be strictly closer to the selected root than to the other. */
	private void requireCorrespondence(ConstructiveConeNode node, Point<T> selected,
			Point<T> other) {
		int index = node.getIndex();
		double x = node.getLiveState(0);
		double y = node.getLiveState(1);
		double z = node.getLiveState(2);
		if (!(Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)) || z == 0) {
			liveChecks.add(new Decision(index, "branch-correspondence", 0));
			throw new Stop(Failure.LIVE_DECISION_DISAGREES, index,
					"the live output is not a finite point");
		}
		ExactRational2D denominator = ExactRational2D.ofBinary64(z);
		T liveX = arithmetic.ofRational(ExactRational2D.ofBinary64(x).divide(denominator));
		T liveY = arithmetic.ofRational(ExactRational2D.ofBinary64(y).divide(denominator));
		T margin = sub(squaredDistance(other, liveX, liveY),
				squaredDistance(selected, liveX, liveY));
		int sign = arithmetic.sign(margin);
		liveChecks.add(new Decision(index, "branch-correspondence", sign));
		if (sign != 1) {
			throw new Stop(Failure.BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED, index,
					"the live output is not certified closer to the selected root");
		}
	}

	private T squaredDistance(Point<T> point, T x, T y) {
		T dx = sub(point.x, x);
		T dy = sub(point.y, y);
		return add(mul(dx, dx), mul(dy, dy));
	}

	/** Membership of a point in the limited part of a segment or ray, end points included. */
	private boolean member(int index, String predicate, Line<T> line, Point<T> point) {
		T parameter = parameter(line, point);
		if (decide(index, predicate + "-start", parameter) < 0) {
			return false;
		}
		if (line.ray) {
			return true;
		}
		T norm = add(mul(line.a, line.a), mul(line.b, line.b));
		return decide(index, predicate + "-end", sub(norm, parameter)) >= 0;
	}

	/** The membership of an incidence decision, not recorded in the sign vector. */
	private boolean onLimitedPart(Line<T> line, Point<T> point) {
		T parameter = parameter(line, point);
		int start = signOf(-1, parameter);
		if (start < 0) {
			return false;
		}
		if (line.ray) {
			return true;
		}
		T norm = add(mul(line.a, line.a), mul(line.b, line.b));
		return signOf(-1, sub(norm, parameter)) >= 0;
	}

	/** @return {@code (X - start) . (b, -a)}, zero at the start, {@code a^2 + b^2} at the end */
	private T parameter(Line<T> line, Point<T> point) {
		return sub(mul(sub(point.x, line.startX), line.b),
				mul(sub(point.y, line.startY), line.a));
	}

	private T residual(Line<T> line, Point<T> point) {
		return add(add(mul(line.a, point.x), mul(line.b, point.y)), line.c);
	}

	private int signOf(int index, T value) {
		int sign = arithmetic.sign(value);
		if (sign == ConstructiveArithmetic.UNDECIDED) {
			throw new Stop(Failure.UNDECIDED, index, "the incidence is not decided");
		}
		return sign;
	}

	private void requirePositive(int index, String predicate, T value) {
		if (decide(index, predicate, value) <= 0) {
			throw new Stop(Failure.INVALID, index, predicate + " is not positive");
		}
	}

	private int decide(int index, String predicate, T value) {
		int sign = arithmetic.sign(value);
		if (sign == ConstructiveArithmetic.UNDECIDED) {
			throw new Stop(Failure.UNDECIDED, index, predicate + " is not decided");
		}
		decisions.add(new Decision(index, predicate, sign));
		return sign;
	}

	@SuppressWarnings("unchecked")
	private Point<T> point(ConstructiveConeNode node, int position) {
		Object value = values[node.getInput(position)];
		if (!(value instanceof Point)) {
			throw new Stop(Failure.OUT_OF_CLASS, node.getIndex(), "an input that is not a point");
		}
		return (Point<T>) value;
	}

	@SuppressWarnings("unchecked")
	private Line<T> line(ConstructiveConeNode node, int position) {
		Object value = values[node.getInput(position)];
		if (!(value instanceof Line)) {
			throw new Stop(Failure.OUT_OF_CLASS, node.getIndex(), "an input that is not a line");
		}
		return (Line<T>) value;
	}

	@SuppressWarnings("unchecked")
	private Circle<T> circle(ConstructiveConeNode node, int position) {
		Object value = values[node.getInput(position)];
		if (!(value instanceof Circle)) {
			throw new Stop(Failure.OUT_OF_CLASS, node.getIndex(), "an input that is not a circle");
		}
		return (Circle<T>) value;
	}

	private T add(T a, T b) {
		return arithmetic.add(a, b);
	}

	private T sub(T a, T b) {
		return arithmetic.subtract(a, b);
	}

	private T mul(T a, T b) {
		return arithmetic.multiply(a, b);
	}

	/** Both raw roots of one line and one circle. */
	private static final class RawRoots<T> {
		private final Point<T>[] roots;
		private final T discriminant;
		private final T distance;
		private final T norm;

		RawRoots(Point<T>[] roots, T discriminant, T distance, T norm) {
			this.roots = roots;
			this.discriminant = discriminant;
			this.distance = distance;
			this.norm = norm;
		}
	}

	/** Internal typed stop of the evaluation of one node. */
	private static final class Stop extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final Failure failure;
		private final int node;

		Stop(Failure failure, int node, String message) {
			super(message, null, false, false);
			this.failure = failure;
			this.node = node;
		}
	}
}
