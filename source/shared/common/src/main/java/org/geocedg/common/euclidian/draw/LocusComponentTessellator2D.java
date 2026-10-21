/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.geocedg.common.euclidian.draw.LocusRenderData2D.ComponentFidelity;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Fidelity;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Mark;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.MarkKind;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.TurnExemption;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Vertex;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.VertexSource;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundaryImage2D;
import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSingularParameter2D;

/**
 * Drawing policy {@value LocusRenderPolicy2D#VERSION} of one certified component
 * (PRE-G9B-CU2; {@code locus-v2-presentation.md}, "PRE-G9B-CU2 amendment (version 2)";
 * consumption design section 5). The component is drawn over its lifted range from
 * {@code 2^d_min} initial spans. A span is split while its chord deviation, measured without
 * clamping the projection, exceeds the tolerance; a vertex splits its two spans while the turn
 * between its chords exceeds {@code theta_max}, unless it lies in the isolation band of a
 * certified fold or is the exact vertex of a certified singular parameter. Refinement stops
 * at the resolution of the parameter, where a remaining defect is marked, never hidden.
 *
 * <p>A point that cannot be evaluated is never interpolated: the failure is located by
 * bisection up to the resolution and the path is cut there with a typed mark. An included end
 * is its evaluation, or the limit form of its certified transition; an excluded structural
 * event is its one-sided limit; any other excluded end is approached from inside and marked.
 * Near a certified discriminant transition the spans use {@code t = t* + s^2}, in which the
 * traced point is regular. A coarsening level relaxes the tolerances and bounds the depth to
 * fit the vertex budget, never crossing a boundary and never sampling uniformly.</p>
 */
final class LocusComponentTessellator2D {
	/** Halvings of the end span with which an excluded end without a limit is approached. */
	static final int OPEN_END_APPROACH_DEPTH = 24;
	/** A span between two failures is probed down to the initial span times 2^-4. */
	static final int FAILURE_PROBE_DEPTH = 4;
	/** A span shorter than the component length times 2^-52 is at the parameter resolution. */
	static final int RESOLUTION_BITS = 52;
	/** Coarsest level: only the initial partition, the required vertices and the ends. */
	static final int COARSEST_LEVEL = 8;
	/** Refinement depth below the initial partition at the first coarsening level. */
	private static final int COARSE_DEPTH = 48;
	/** Refinement depth that each further coarsening level removes. */
	private static final int DEPTH_PER_LEVEL = 6;
	/**
	 * A chord shorter than this, in pixels, is invisible and its direction is dominated by
	 * rounding: a turn is measured between the nearest chords at least this long, so that a
	 * cluster of coincident vertices is judged as the one point it draws.
	 */
	static final double TURN_RESOLUTION_PIXELS = 1e-6;

	/** Coordinate in which the midpoint of a span is taken. */
	private enum Parametrization {
		LINEAR,
		/** {@code u = origin + s^2}, near a square-root transition at the lower end. */
		SQUARE_ROOT_FROM_LOWER,
		/** {@code u = origin - s^2}, near a square-root transition at the upper end. */
		SQUARE_ROOT_FROM_UPPER
	}

	/** How an end of the component is reached. */
	private enum EndTreatment {
		/** The binary64 end belongs to the component and is evaluated. */
		EVALUATE,
		/** An excluded structural event, reached as its one-sided limit. */
		LIMIT,
		/** An excluded end without a limit, approached from inside. */
		APPROACH,
		/** The closing point of a certified closed periodic component. */
		CLOSURE
	}

	/** One vertex candidate and the span to the next one. */
	private static final class Node {
		private final double u;
		private LocusPoint2D point;
		private double sx;
		private double sy;
		private boolean valid;
		private EvaluationStatus status;
		private String diagnostic = "";
		private TurnExemption exemption = TurnExemption.NONE;
		private VertexSource source = VertexSource.EVALUATED;
		private Parametrization parametrization = Parametrization.LINEAR;
		private double origin;
		private int depth;
		private boolean accepted;
		private boolean resolved;
		private boolean located;
		private boolean turnMarked;
		private Node mid;

		Node(double u) {
			this.u = u;
		}
	}

	/** Drawing of the component at one coarsening level. */
	static final class Outcome {
		private final List<Vertex> vertices;
		private final List<Mark> marks;
		private final ComponentFidelity fidelity;
		private final boolean overBudget;

		Outcome(List<Vertex> vertices, List<Mark> marks, ComponentFidelity fidelity,
				boolean overBudget) {
			this.vertices = vertices;
			this.marks = marks;
			this.fidelity = fidelity;
			this.overBudget = overBudget;
		}

		List<Vertex> getVertices() {
			return vertices;
		}

		List<Mark> getMarks() {
			return marks;
		}

		ComponentFidelity getFidelity() {
			return fidelity;
		}

		/** @return whether the drawing exceeded the remaining vertex budget */
		boolean isOverBudget() {
			return overBudget;
		}
	}

	private final GeoLocusV2 locus;
	private final LocusDefinition2D definition;
	private final String branchKey;
	private final LocusExistenceStructure2D structure;
	private final ContinuousComponent component;
	private final LocusRenderPolicy2D policy;
	private final LocusEvaluationSession2D session;
	private final String componentKey;
	private final LocusInterval2D extent;
	private final boolean closed;
	private final double period;
	private final double resolution;
	private final List<Node> nodes = new ArrayList<>();
	private final List<Mark> marks = new ArrayList<>();
	private EndTreatment lowerTreatment;
	private EndTreatment upperTreatment;
	private double tolerance;
	private double maximumTurn;
	private int depthLimit;
	private int level;

	LocusComponentTessellator2D(GeoLocusV2 locus, LocusDefinition2D definition,
			String branchKey, LocusExistenceStructure2D structure,
			ContinuousComponent component, LocusRenderPolicy2D policy,
			LocusEvaluationSession2D session) {
		this.locus = locus;
		this.definition = definition;
		this.branchKey = branchKey;
		this.structure = structure;
		this.component = component;
		this.policy = policy;
		this.session = session;
		this.componentKey = componentKey(branchKey, component.getEvidenceKey());
		this.extent = component.getInterval();
		this.closed = structure.isClosedPeriodic() && component.isClosedPeriodic();
		LocusInterval2D domain = structure.getCanonicalDomain();
		this.period = structure.getTopology() == DomainTopology.PERIODIC
				? domain.getUpper() - domain.getLower() : 0;
		this.resolution = Math.scalb(extent.getUpper() - extent.getLower(), -RESOLUTION_BITS);
	}

	/** @return render key of a component of a branch */
	static String componentKey(String branchKey, String evidenceKey) {
		return branchKey + "/" + evidenceKey;
	}

	/**
	 * Draws the component at one coarsening level.
	 *
	 * @param coarsening level, {@code 0} for the policy itself
	 * @param remaining vertices left in the budget of the view
	 * @return vertices, marks and fidelity, or an over-budget outcome
	 */
	Outcome tessellate(int coarsening, int remaining) {
		nodes.clear();
		marks.clear();
		level = coarsening;
		tolerance = policy.getVisualTolerancePixels() * Math.scalb(1.0, level);
		maximumTurn = Math.min(Math.PI,
				Math.toRadians(policy.getMaximumTurnDegrees()) * Math.scalb(1.0, level));
		depthLimit = level == 0 ? Integer.MAX_VALUE
				: Math.max(0, COARSE_DEPTH - DEPTH_PER_LEVEL * (level - 1));
		double lower = extent.getLower();
		double upper = extent.getUpper();
		if (!(lower < upper)) {
			return failed(MarkKind.END_NOT_EVALUATED, "The component has no span to draw");
		}
		initialize(lower, upper);
		if (validCount() > remaining) {
			return overBudget();
		}
		boolean changed = true;
		while (changed) {
			Set<Node> splits = new LinkedHashSet<>();
			deviationPass(splits);
			turnPass(splits);
			probePass(splits);
			changed = applySplits(splits);
			changed |= locateFailureEdges();
			if (validCount() > remaining) {
				return overBudget();
			}
		}
		return emit();
	}

	private void initialize(double lower, double upper) {
		lowerTreatment = closed ? EndTreatment.EVALUATE
				: treatment(component.getLowerBoundary());
		upperTreatment = closed ? EndTreatment.CLOSURE
				: treatment(component.getUpperBoundary());
		int count = 1 << policy.getMinimumDepth();
		double span = upper - lower;
		double initial = span / count;
		if (lowerTreatment == EndTreatment.APPROACH) {
			for (int halving = OPEN_END_APPROACH_DEPTH; halving >= 1; halving--) {
				nodes.add(evaluateNode(lower + Math.scalb(initial, -halving)));
			}
		} else {
			nodes.add(endNode(lower, false, lowerTreatment));
		}
		for (int index = 1; index < count; index++) {
			nodes.add(evaluateNode(lower + span * index / count));
		}
		if (upperTreatment == EndTreatment.APPROACH) {
			for (int halving = 1; halving <= OPEN_END_APPROACH_DEPTH; halving++) {
				nodes.add(evaluateNode(upper - Math.scalb(initial, -halving)));
			}
		} else {
			nodes.add(endNode(upper, true, upperTreatment));
		}
		if (lowerTreatment == EndTreatment.EVALUATE
				&& component.getLowerBoundary().isSquareRootTransition()) {
			Node first = nodes.get(0);
			first.parametrization = Parametrization.SQUARE_ROOT_FROM_LOWER;
			first.origin = lower;
		}
		if (upperTreatment == EndTreatment.EVALUATE
				&& component.getUpperBoundary().isSquareRootTransition()) {
			Node last = nodes.get(nodes.size() - 2);
			last.parametrization = Parametrization.SQUARE_ROOT_FROM_UPPER;
			last.origin = upper;
		}
		addSingularVertices();
		addFoldVertices();
	}

	private EndTreatment treatment(LocusBoundary2D end) {
		if (end.isBinaryEndIncluded()) {
			return EndTreatment.EVALUATE;
		}
		return end.getKind() == LocusBoundary2D.Kind.STRUCTURAL_EVENT ? EndTreatment.LIMIT
				: EndTreatment.APPROACH;
	}

	/**
	 * @return the end node: its evaluation, its one-sided limit, or the limit form of its
	 *         certified transition when the evaluation fails
	 */
	private Node endNode(double parameter, boolean upper, EndTreatment treatment) {
		Node node = evaluateNode(parameter);
		if (node.valid) {
			if (treatment == EndTreatment.LIMIT) {
				node.source = VertexSource.ONE_SIDED_LIMIT;
			}
			return node;
		}
		LocusBoundary2D end = upper ? component.getUpperBoundary()
				: component.getLowerBoundary();
		if (treatment == EndTreatment.EVALUATE) {
			LocusBoundaryImage2D image = definition.encloseBoundaryImage(branchKey, end);
			if (image.isEnclosed()) {
				setPoint(node, image.getPoint());
				node.source = VertexSource.LIMIT_FORM;
				return node;
			}
			node.diagnostic = node.diagnostic + "; " + image.getDiagnostic();
		}
		return node;
	}

	/** Exact vertices of the certified singular parameters inside the component. */
	private void addSingularVertices() {
		for (LocusSingularParameter2D singular : structure.getSingularParameters()) {
			double lowerLift = lift(singular.getParameterLower());
			double upperLift = lift(singular.getParameterUpper());
			if (Double.isNaN(lowerLift) || Double.isNaN(upperLift)
					|| !(nodes.get(0).u < lowerLift && upperLift < last().u)) {
				continue;
			}
			LocusBoundaryImage2D image = definition.encloseSingularImage(branchKey, singular);
			if (!image.isEnclosed()) {
				marks.add(mark(MarkKind.SINGULAR_VERTEX_NOT_EXACT, lowerLift,
						image.getDiagnostic()));
				continue;
			}
			Node vertex = null;
			for (Node node : nodes) {
				if (lowerLift <= node.u && node.u <= upperLift) {
					vertex = node;
				}
			}
			if (vertex == null) {
				vertex = new Node(lowerLift + (upperLift - lowerLift) / 2);
				insertSorted(vertex);
			}
			setPoint(vertex, image.getPoint());
			vertex.source = VertexSource.LIMIT_FORM;
			vertex.exemption = TurnExemption.SINGULAR_VERTEX;
		}
	}

	/** A vertex inside the band of each certified fold of the component. */
	private void addFoldVertices() {
		for (LocusCertifiedFold2D fold : structure.getCertifiedFolds()) {
			boolean present = false;
			for (Node node : nodes) {
				present |= fold.containsCanonical(node.u, period);
			}
			double parameter = lift(fold.getParameterLower()
					+ (fold.getParameterUpper() - fold.getParameterLower()) / 2);
			if (!present && !Double.isNaN(parameter) && nodes.get(0).u < parameter
					&& parameter < last().u) {
				insertSorted(evaluateNode(parameter));
			}
		}
	}

	/** @return the lifted parameter of a canonical one inside the extent, or {@code NaN} */
	private double lift(double canonical) {
		double[] candidates = period > 0
				? new double[] {canonical, canonical + period, canonical - period}
				: new double[] {canonical};
		for (double candidate : candidates) {
			if (extent.getLower() <= candidate && candidate <= extent.getUpper()) {
				return candidate;
			}
		}
		return Double.NaN;
	}

	private void insertSorted(Node node) {
		int index = 0;
		while (index < nodes.size() && nodes.get(index).u < node.u) {
			index++;
		}
		if (index < nodes.size() && nodes.get(index).u == node.u) {
			return;
		}
		Node previous = index > 0 ? nodes.get(index - 1) : null;
		if (previous != null) {
			node.parametrization = previous.parametrization;
			node.origin = previous.origin;
			node.depth = previous.depth;
			previous.mid = null;
			previous.accepted = false;
		}
		nodes.add(index, node);
	}

	private void deviationPass(Set<Node> splits) {
		for (int index = 0; index + 1 < nodes.size(); index++) {
			Node a = nodes.get(index);
			Node b = nodes.get(index + 1);
			if (!a.valid || !b.valid || a.accepted || !splittable(a, b)) {
				if (a.valid && b.valid && a.resolved && !a.accepted) {
					a.accepted = true;
					if (level == 0 && chordPixels(a, b) > tolerance) {
						marks.add(mark(MarkKind.DEVIATION_AT_RESOLUTION, a.u,
								"chord " + chordPixels(a, b) + " px"));
					}
				}
				continue;
			}
			if (!a.mid.valid || deviation(a.mid, a, b) > tolerance) {
				splits.add(a);
			} else {
				a.accepted = true;
			}
		}
	}

	/**
	 * Splits the visible spans at each vertex whose turn exceeds the policy outside the
	 * exemptions; where none can be split, the turn is at the resolution of the parameter
	 * and is marked once, never exempted. On a certified closed periodic component the
	 * closing point is the vertex of the last chord and the first one.
	 */
	private void turnPass(Set<Node> splits) {
		int count = nodes.size();
		boolean cyclic = closed && count >= 3;
		for (int index = cyclic ? 0 : 1; index < count - 1; index++) {
			Node vertex = nodes.get(index);
			if (!vertex.valid) {
				continue;
			}
			int previous = visibleNeighbour(index, -1, cyclic);
			int next = visibleNeighbour(index, 1, cyclic);
			if (previous < 0 || next < 0 || exempt(previous, next, cyclic)) {
				continue;
			}
			double turn = turn(nodes.get(previous), vertex, nodes.get(next));
			if (!(turn > maximumTurn)) {
				continue;
			}
			Node leftSpan = nodes.get(index == 0 ? count - 2 : index - 1);
			boolean left = visible(leftSpan, following(leftSpan))
					&& splittable(leftSpan, following(leftSpan));
			boolean right = visible(vertex, following(vertex))
					&& splittable(vertex, following(vertex));
			if (left) {
				splits.add(leftSpan);
			}
			if (right) {
				splits.add(vertex);
			}
			if (!left && !right && level == 0
					&& !turnMarked(vertex.u, nodes.get(previous).u, nodes.get(next).u)) {
				vertex.turnMarked = true;
				marks.add(mark(MarkKind.UNCERTIFIED_TURN, vertex.u,
						Math.toDegrees(turn) + " degrees"));
			}
		}
	}

	/**
	 * @return index of the nearest valid vertex in the direction whose chord from the vertex
	 *         is visible, without crossing a failure; {@code -1} when there is none
	 */
	private int visibleNeighbour(int index, int step, boolean cyclic) {
		Node vertex = nodes.get(index);
		int count = nodes.size();
		int current = index;
		for (int steps = 0; steps < count; steps++) {
			current += step;
			if (cyclic) {
				// The closing point, the last node, is the first one.
				current = Math.floorMod(current, count - 1);
			} else if (current < 0 || current >= count) {
				return -1;
			}
			if (current == index) {
				return -1;
			}
			Node node = nodes.get(current);
			if (!node.valid) {
				return -1;
			}
			if (Math.hypot(node.sx - vertex.sx, node.sy - vertex.sy)
					> TURN_RESOLUTION_PIXELS) {
				return current;
			}
		}
		return -1;
	}

	/** @return whether a vertex from {@code from} to {@code to} has a certified exemption */
	private boolean exempt(int from, int to, boolean cyclic) {
		int count = nodes.size();
		int current = from;
		while (true) {
			current = cyclic ? Math.floorMod(current + 1, count - 1) : current + 1;
			if (current == to) {
				return false;
			}
			if (nodes.get(current).exemption != TurnExemption.NONE) {
				return true;
			}
		}
	}

	private static boolean visible(Node a, Node b) {
		return b != null && Math.hypot(b.sx - a.sx, b.sy - a.sy) > TURN_RESOLUTION_PIXELS;
	}

	/** @return whether a vertex of the same unresolved turn is already marked */
	private boolean turnMarked(double parameter, double from, double to) {
		for (Node node : nodes) {
			if (node.turnMarked && Math.min(from, to) <= node.u && node.u <= Math.max(from, to)
					&& parameter >= Math.min(from, to) && parameter <= Math.max(from, to)) {
				return true;
			}
		}
		return false;
	}

	/** Probes the span between two failed evaluations for a valid island. */
	private void probePass(Set<Node> splits) {
		double probe = Math.scalb((last().u - nodes.get(0).u)
				/ (1 << policy.getMinimumDepth()), -FAILURE_PROBE_DEPTH);
		for (int index = 0; index + 1 < nodes.size(); index++) {
			Node a = nodes.get(index);
			Node b = nodes.get(index + 1);
			if (!a.valid && !b.valid && b.u - a.u > probe && splittable(a, b)) {
				splits.add(a);
			}
		}
	}

	/**
	 * @return whether the span from {@code a} can be split, with its midpoint evaluated; a
	 *         span at the resolution of the parameter or at the depth of the level cannot
	 */
	private boolean splittable(Node a, Node b) {
		if (a.resolved || b == null) {
			return false;
		}
		double middle = midpoint(a, b.u, a.u, b.u);
		if (!(a.u < middle && middle < b.u) || b.u - a.u <= resolution) {
			a.resolved = true;
			return false;
		}
		if (a.depth >= depthLimit) {
			return false;
		}
		if (a.mid == null) {
			a.mid = evaluateNode(middle);
		}
		return true;
	}

	private Node following(Node node) {
		int index = nodes.indexOf(node);
		return index >= 0 && index + 1 < nodes.size() ? nodes.get(index + 1) : null;
	}

	private boolean applySplits(Set<Node> splits) {
		if (splits.isEmpty()) {
			return false;
		}
		List<Node> refined = new ArrayList<>(nodes.size() + splits.size());
		for (Node node : nodes) {
			refined.add(node);
			if (splits.contains(node) && node.mid != null) {
				Node middle = node.mid;
				node.mid = null;
				node.depth++;
				node.accepted = false;
				middle.parametrization = node.parametrization;
				middle.origin = node.origin;
				middle.depth = node.depth;
				refined.add(middle);
			}
		}
		nodes.clear();
		nodes.addAll(refined);
		return true;
	}

	/**
	 * Locates each edge between a valid and a failed evaluation by bisection, up to the
	 * resolution of the parameter or the depth of the level: the last valid point closes the
	 * path and the first failed one opens the cut.
	 */
	private boolean locateFailureEdges() {
		boolean changed = false;
		for (int index = 0; index + 1 < nodes.size(); index++) {
			Node a = nodes.get(index);
			Node b = nodes.get(index + 1);
			if (a.valid == b.valid || a.located) {
				continue;
			}
			a.located = true;
			double low = a.u;
			double high = b.u;
			Node lowNode = a;
			Node highNode = b;
			int steps = 0;
			while (steps < depthLimit - a.depth) {
				double middle = midpoint(a, b.u, low, high);
				if (!(low < middle && middle < high) || high - low <= resolution) {
					break;
				}
				Node probe = evaluateNode(middle);
				if (probe.valid == a.valid) {
					low = middle;
					lowNode = probe;
				} else {
					high = middle;
					highNode = probe;
				}
				steps++;
			}
			List<Node> inserted = new ArrayList<>();
			if (lowNode != a) {
				inserted.add(lowNode);
			}
			if (highNode != b) {
				inserted.add(highNode);
			}
			for (Node node : inserted) {
				node.parametrization = a.parametrization;
				node.origin = a.origin;
				node.depth = a.depth;
			}
			// The span across the edge is the cut: never split, never drawn.
			lowNode.located = true;
			lowNode.resolved = true;
			a.mid = null;
			a.accepted = false;
			nodes.addAll(index + 1, inserted);
			index += inserted.size();
			changed |= !inserted.isEmpty();
		}
		return changed;
	}

	private Outcome emit() {
		List<Vertex> vertices = new ArrayList<>();
		boolean pending = true;
		for (Node node : nodes) {
			if (!node.valid) {
				pending = true;
				continue;
			}
			vertices.add(new Vertex(node.point, node.u, pending, node.exemption, node.source));
			pending = false;
		}
		markFailures();
		if (lowerTreatment == EndTreatment.APPROACH) {
			marks.add(mark(MarkKind.OPEN_END_APPROACHED, nodes.get(0).u,
					"excluded end " + extent.getLower() + " not reached"));
		}
		if (upperTreatment == EndTreatment.APPROACH) {
			marks.add(mark(MarkKind.OPEN_END_APPROACHED, last().u,
					"excluded end " + extent.getUpper() + " not reached"));
		}
		if (level > 0) {
			marks.add(mark(MarkKind.BUDGET_COARSENED, Double.NaN, "coarsening level " + level));
		}
		if (vertices.isEmpty()) {
			return failed(MarkKind.END_NOT_EVALUATED, "No point of the component was evaluated");
		}
		List<MarkKind> causes = new ArrayList<>();
		for (Mark mark : marks) {
			if (!causes.contains(mark.getKind())) {
				causes.add(mark.getKind());
			}
		}
		return new Outcome(vertices, new ArrayList<>(marks), new ComponentFidelity(
				componentKey, causes.isEmpty() ? Fidelity.COMPLETE : Fidelity.PARTIAL, causes,
				vertices.size()), false);
	}

	/** One mark per run of failed evaluations: an end not evaluated, or a gap. */
	private void markFailures() {
		int index = 0;
		while (index < nodes.size()) {
			if (nodes.get(index).valid) {
				index++;
				continue;
			}
			int start = index;
			boolean computational = false;
			while (index < nodes.size() && !nodes.get(index).valid) {
				computational |= nodes.get(index).status == EvaluationStatus.EVALUATION_FAILED;
				index++;
			}
			Node first = nodes.get(start);
			boolean atEnd = !closed && (start == 0 || index == nodes.size());
			MarkKind kind = atEnd ? MarkKind.END_NOT_EVALUATED
					: computational ? MarkKind.COMPUTATIONAL_GAP : MarkKind.EVALUATION_GAP;
			marks.add(mark(kind, first.u, first.status + ": " + first.diagnostic));
		}
	}

	private Outcome failed(MarkKind cause, String detail) {
		List<Mark> failure = new ArrayList<>(marks);
		failure.add(mark(cause, Double.NaN, detail));
		return new Outcome(new ArrayList<>(), failure, new ComponentFidelity(componentKey,
				Fidelity.FAILED, List.of(cause), 0), false);
	}

	private Outcome overBudget() {
		return new Outcome(new ArrayList<>(), new ArrayList<>(), null, true);
	}

	/** @return a failed outcome of a component that does not fit the vertex budget */
	Outcome exhausted() {
		marks.clear();
		return failed(MarkKind.VERTEX_BUDGET_EXHAUSTED,
				"The component does not fit the vertex budget at its coarsest");
	}

	private Node evaluateNode(double u) {
		Node node = new Node(u);
		LocusEvaluation2D evaluation = locus.evaluateForRender(branchKey, extent, u, session);
		if (evaluation.isValid() && Double.isFinite(evaluation.getPoint().getX())
				&& Double.isFinite(evaluation.getPoint().getY())) {
			setPoint(node, evaluation.getPoint());
		} else {
			node.status = evaluation.isValid() ? EvaluationStatus.NON_FINITE
					: evaluation.getStatus();
			node.diagnostic = evaluation.getDiagnostic();
		}
		for (LocusCertifiedFold2D fold : structure.getCertifiedFolds()) {
			if (fold.containsCanonical(u, period)) {
				node.exemption = TurnExemption.FOLD_BAND;
			}
		}
		return node;
	}

	private void setPoint(Node node, LocusPoint2D point) {
		node.point = point;
		node.sx = point.getX() * policy.getXScale();
		node.sy = point.getY() * policy.getYScale();
		node.valid = true;
		node.status = null;
	}

	private int validCount() {
		int count = 0;
		for (Node node : nodes) {
			count += node.valid ? 1 : 0;
		}
		return count;
	}

	private Node last() {
		return nodes.get(nodes.size() - 1);
	}

	private Mark mark(MarkKind kind, double parameter, String detail) {
		return new Mark(kind, componentKey, parameter, detail);
	}

	/** @return midpoint of {@code [low, high]} in the coordinate of the span from {@code a} */
	private static double midpoint(Node a, double spanUpper, double low, double high) {
		switch (a.parametrization) {
		case SQUARE_ROOT_FROM_LOWER: {
			double sLow = Math.sqrt(Math.max(0, low - a.origin));
			double sHigh = Math.sqrt(Math.max(0, high - a.origin));
			double s = sLow + (sHigh - sLow) / 2;
			return a.origin + s * s;
		}
		case SQUARE_ROOT_FROM_UPPER: {
			double sLow = Math.sqrt(Math.max(0, a.origin - low));
			double sHigh = Math.sqrt(Math.max(0, a.origin - high));
			double s = sHigh + (sLow - sHigh) / 2;
			return a.origin - s * s;
		}
		default:
			return low + (high - low) / 2;
		}
	}

	private static double chordPixels(Node a, Node b) {
		return Math.hypot(b.sx - a.sx, b.sy - a.sy);
	}

	/**
	 * Chord deviation without clamping the projection: the distance to the line of the chord
	 * plus the distance from the projection to the chord, so that a span whose chord nearly
	 * vanishes never counts as flat.
	 */
	static double deviation(double px, double py, double ax, double ay, double bx,
			double by) {
		double dx = bx - ax;
		double dy = by - ay;
		double length = Math.hypot(dx, dy);
		if (length == 0) {
			return Math.hypot(px - ax, py - ay);
		}
		double along = ((px - ax) * dx + (py - ay) * dy) / length;
		double across = Math.abs((px - ax) * dy - (py - ay) * dx) / length;
		double outside = along < 0 ? -along : along > length ? along - length : 0;
		return across + outside;
	}

	private static double deviation(Node point, Node a, Node b) {
		return deviation(point.sx, point.sy, a.sx, a.sy, b.sx, b.sy);
	}

	/** @return turn between the chords of a vertex in radians, or {@code NaN} without one */
	static double turn(double ax, double ay, double vx, double vy, double bx, double by) {
		double x1 = vx - ax;
		double y1 = vy - ay;
		double x2 = bx - vx;
		double y2 = by - vy;
		if (Math.hypot(x1, y1) == 0 || Math.hypot(x2, y2) == 0) {
			return Double.NaN;
		}
		return Math.atan2(Math.abs(x1 * y2 - y1 * x2), x1 * x2 + y1 * y2);
	}

	private static double turn(Node a, Node vertex, Node b) {
		return turn(a.sx, a.sy, vertex.sx, vertex.sy, b.sx, b.sy);
	}
}
