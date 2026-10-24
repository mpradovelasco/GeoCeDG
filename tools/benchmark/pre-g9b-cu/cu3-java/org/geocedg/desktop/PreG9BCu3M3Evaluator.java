/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operand;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.OperandKind;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.desktop.PreG9BCu3M3Tower.Num;

/**
 * PRE-G9B-CU3 gate G-M3 prototype, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-m3-gate.ps1}: exact evaluation of a captured
 * class-v3 construction program ({@code locus-v2-certified-construction-model.md} section 10)
 * with the driver replaced by an exact point, in the tower of {@link PreG9BCu3M3Tower}. Every
 * value carries its first two derivatives with respect to the driver parameter when the driver
 * jet has them (exact jets). Every formula and every validity predicate mirrors the class-v3
 * interval model {@code ConstructionBoxModel2D}, decided exactly: a strict inequality that the
 * box model certifies by an enclosure is decided here by an exact sign; a predicate that
 * depends on an inherited tolerance window is decided conservatively and otherwise fails
 * closed. It is not part of any source set.
 */
final class PreG9BCu3M3Evaluator {

	/** Typed reason why the exact evaluation produces no point. */
	enum Failure {
		/** A validity predicate is false at the exact point: the output does not exist. */
		INVALID,
		/** The selection of an intersection output is not reconstructed exactly. */
		SELECTION_NOT_RECONSTRUCTED,
		/** An operation, or a decision near an inherited tolerance, leaves the class. */
		OUT_OF_CLASS
	}

	/** The evaluation stops with a typed failure. */
	static final class Stop extends RuntimeException {
		private static final long serialVersionUID = 1L;
		final Failure failure;
		final int node;

		Stop(Failure failure, int node, String message) {
			super(failure + " at node " + node + ": " + message);
			this.failure = failure;
			this.node = node;
		}
	}

	/** A value with its first and second derivatives, or without derivatives. */
	final class J {
		final Num v;
		final Num d1;
		final Num d2;

		J(Num v, Num d1, Num d2) {
			this.v = v;
			this.d1 = d1;
			this.d2 = d2;
		}

		boolean hasDerivatives() {
			return d1 != null;
		}

		J add(J o) {
			if (!any(o)) {
				return new J(v.add(o.v), null, null);
			}
			return new J(v.add(o.v), first().add(o.first()), second().add(o.second()));
		}

		J subtract(J o) {
			return add(o.negate());
		}

		J negate() {
			return new J(v.negate(), d1 == null ? null : d1.negate(),
					d2 == null ? null : d2.negate());
		}

		J multiply(J o) {
			if (!any(o)) {
				return new J(v.multiply(o.v), null, null);
			}
			Num first = first().multiply(o.v).add(v.multiply(o.first()));
			Num second = second().multiply(o.v).add(first().multiply(o.first())
					.multiply(tower.of(2))).add(v.multiply(o.second()));
			return new J(v.multiply(o.v), first, second);
		}

		J square() {
			return multiply(this);
		}

		J divide(J o) {
			Num quotient = v.divide(o.v);
			if (!any(o)) {
				return new J(quotient, null, null);
			}
			Num first = first().subtract(quotient.multiply(o.first())).divide(o.v);
			Num second = second().subtract(first.multiply(o.first()).multiply(tower.of(2)))
					.subtract(quotient.multiply(o.second())).divide(o.v);
			return new J(quotient, first, second);
		}

		/** @return the first derivative, zero for a constant without derivatives */
		private Num first() {
			return d1 == null ? tower.zero() : d1;
		}

		private Num second() {
			return d2 == null ? tower.zero() : d2;
		}

		private boolean any(J o) {
			return d1 != null || o.d1 != null;
		}

		J sqrt() {
			Num root = tower.sqrt(v);
			if (d1 == null) {
				return new J(root, null, null);
			}
			if (root.signum() == 0) {
				throw new Stop(Failure.OUT_OF_CLASS, node, "Derivative of a square root at zero");
			}
			Num twice = root.add(root);
			Num first = d1.divide(twice);
			Num second = d2.subtract(first.square().multiply(tower.of(2))).divide(twice);
			return new J(root, first, second);
		}

		J half() {
			return multiply(constant(tower.of(0.5)));
		}

		int sign() {
			return v.signum();
		}
	}

	/** Point with homogeneous and inhomogeneous coordinates; {@code unit} when z is 1. */
	final class P {
		final J x;
		final J y;
		final J z;
		final J inhomX;
		final J inhomY;
		final boolean unit;

		P(J x, J y, J z, J inhomX, J inhomY, boolean unit) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.inhomX = inhomX;
			this.inhomY = inhomY;
			this.unit = unit;
		}
	}

	/** Line {@code a x + b y + c = 0}, possibly bounded as a ray or a segment. */
	final class L {
		final J a;
		final J b;
		final J c;
		final boolean bounded;
		final boolean ray;
		final J startX;
		final J startY;

		L(J a, J b, J c, boolean bounded, boolean ray, J startX, J startY) {
			this.a = a;
			this.b = b;
			this.c = c;
			this.bounded = bounded;
			this.ray = ray;
			this.startX = startX;
			this.startY = startY;
		}
	}

	/** Circle by centre and squared radius. */
	final class C {
		final J mx;
		final J my;
		final J radiusSquared;

		C(J mx, J my, J radiusSquared) {
			this.mx = mx;
			this.my = my;
			this.radiusSquared = radiusSquared;
		}
	}

	/** Both raw roots of a line and a circle with the captured selection rule. */
	final class Pair {
		final L line;
		final C circle;
		final boolean compaction;
		final int[] permutation;
		final boolean[] labelled;

		Pair(L line, C circle, Step step) {
			this(line, circle, step.getParameter(0) == 1,
					new int[] {(int) step.getParameter(1), (int) step.getParameter(2)},
					new boolean[] {step.getParameter(3) == 1, step.getParameter(4) == 1});
		}

		Pair(L line, C circle, boolean compaction, int[] permutation, boolean[] labelled) {
			this.line = line;
			this.circle = circle;
			this.compaction = compaction;
			this.permutation = permutation.clone();
			this.labelled = labelled.clone();
		}
	}

	/**
	 * One output of a line-circle intersection outside a program (the algorithm of a driver
	 * point {@code D}), with the exact form of the inherited selection rule.
	 */
	P lineCircleOutput(L line, C circle, boolean compaction, int[] permutation,
			boolean[] labelled, int index) {
		node = -1;
		return output(new Pair(line, circle, compaction, permutation, labelled), index);
	}

	L lineOf(J a, J b, J c, boolean bounded, boolean ray, J startX, J startY) {
		return new L(a, b, c, bounded, ray, startX, startY);
	}

	C circleOf(J mx, J my, J radiusSquared) {
		return new C(mx, my, radiusSquared);
	}

	/** Exact GeoGebra formulas reused by the reconstruction of a subgraph. */
	L joinOf(P p, P q, boolean bounded, boolean ray) {
		node = -1;
		return join(p, q, bounded, ray);
	}

	L parallelOf(P p, L l) {
		node = -1;
		return defined(p.z.multiply(l.a), p.z.multiply(l.b),
				p.x.multiply(l.a).add(p.y.multiply(l.b)).negate(), false, false, null, null);
	}

	L perpendicularOf(P p, L l) {
		node = -1;
		return perpendicular(p, l);
	}

	P meetOf(L g, L h) {
		node = -1;
		return meet(g, h);
	}

	P midpointOf(P first, P second) {
		return unit(first.inhomX.add(second.inhomX).half(),
				first.inhomY.add(second.inhomY).half());
	}

	C circleThroughOf(P center, P through) {
		node = -1;
		J squared = through.inhomX.subtract(center.inhomX).square()
				.add(through.inhomY.subtract(center.inhomY).square());
		if (!greater(squared.v, radiusSquaredMinimum)) {
			throw new Stop(Failure.OUT_OF_CLASS, node, "Radius at the inherited threshold");
		}
		return new C(center.inhomX, center.inhomY, squared);
	}

	/** Vector. */
	final class V {
		final J x;
		final J y;

		V(J x, J y) {
			this.x = x;
			this.y = y;
		}
	}

	/** Record of one exact intersection selection, for the correspondence check. */
	static final class Selection {
		final int node;
		final int selectedRaw;
		final Num[] roots;

		Selection(int node, int selectedRaw, Num[] roots) {
			this.node = node;
			this.selectedRaw = selectedRaw;
			this.roots = roots;
		}
	}

	private final PreG9BCu3M3Tower tower;
	private final Num epsilon;
	private final Num normalizationMinimum;
	private final Num normalizationMaximum;
	private final Num radiusSquaredMinimum;
	private final Num coincidentWindowFactor;
	private final Num coincidentRoundingFactor;
	private Object[] nodes;
	private int node;
	/** Exact points of every point node, for the correspondence with live points. */
	final List<P> pointNodes = new ArrayList<>();
	/** Exact selections of every line-circle output, in program order. */
	final List<Selection> selections = new ArrayList<>();

	PreG9BCu3M3Evaluator(PreG9BCu3M3Tower tower) {
		this.tower = tower;
		epsilon = tower.of(1E-8);
		normalizationMinimum = tower.of(1E-2);
		normalizationMaximum = tower.of(10E6);
		radiusSquaredMinimum = tower.of(4E-16);
		coincidentWindowFactor = tower.of(1E-3);
		coincidentRoundingFactor = tower.of(1E-10);
	}

	J constant(Num value) {
		return new J(value, null, null);
	}

	J constant(double value) {
		return constant(tower.of(value));
	}

	J jet(Num value, Num first, Num second) {
		return new J(value, first, second);
	}

	P unit(J x, J y) {
		return new P(x, y, constant(tower.one()), x, y, true);
	}

	/**
	 * Evaluates the program with node 0 replaced by the exact driver point.
	 *
	 * @param driver exact unit driver point, with or without derivatives
	 * @return the exact traced point, the program's last node
	 */
	P evaluate(CertifiedConstructionProgram2D program, P driver) {
		List<Step> steps = program.getSteps();
		nodes = new Object[steps.size() + 1];
		nodes[0] = driver;
		pointNodes.clear();
		selections.clear();
		pointNodes.add(driver);
		for (int index = 0; index < steps.size(); index++) {
			node = index + 1;
			nodes[node] = apply(steps.get(index));
			pointNodes.add(nodes[node] instanceof P ? (P) nodes[node] : null);
		}
		return (P) nodes[nodes.length - 1];
	}

	private Object apply(Step step) {
		List<Operand> operands = step.getOperands();
		switch (step.getOperation()) {
		case JOIN:
			requireDistinct(operands);
			return join(point(operands.get(0)), point(operands.get(1)), false, false);
		case RAY:
		case SEGMENT:
			requireDistinct(operands);
			return join(point(operands.get(0)), point(operands.get(1)), true,
					step.getOperation() == Operation.RAY);
		case PARALLEL: {
			P p = point(operands.get(0));
			L l = line(operands.get(1));
			return defined(p.z.multiply(l.a), p.z.multiply(l.b),
					p.x.multiply(l.a).add(p.y.multiply(l.b)).negate(), false, false, null, null);
		}
		case PERPENDICULAR:
			return perpendicular(point(operands.get(0)), line(operands.get(1)));
		case MEET:
			return meet(line(operands.get(0)), line(operands.get(1)));
		case MIDPOINT: {
			P first = point(operands.get(0));
			P second = point(operands.get(1));
			return unit(first.inhomX.add(second.inhomX).half(),
					first.inhomY.add(second.inhomY).half());
		}
		case CIRCLE_RADIUS: {
			P center = point(operands.get(0));
			J radius = constant(step.getParameter(0));
			return new C(center.inhomX, center.inhomY, radius.square());
		}
		case CIRCLE_THROUGH:
			return circleThrough(operands);
		case LINE_CIRCLE:
			return new Pair(line(operands.get(0)), circle(operands.get(1)), step);
		case INTERSECTION_OUTPUT:
			return output((Pair) nodes[operands.get(0).getNode()], (int) step.getParameter(0));
		case UNIT_VECTOR_LINE: {
			L l = line(operands.get(0));
			V direction = new V(l.b, l.a.negate());
			return step.getParameter(0) == 1 ? normalizeVector(direction) : direction;
		}
		case UNIT_VECTOR: {
			V source = vector(operands.get(0));
			return step.getParameter(0) == 1 ? normalizeVector(source) : source;
		}
		case VECTOR_POINTS: {
			P from = point(operands.get(0));
			P to = point(operands.get(1));
			return new V(to.inhomX.subtract(from.inhomX), to.inhomY.subtract(from.inhomY));
		}
		case SCALE: {
			J factor = scalar(operands.get(0));
			V scaled = vector(operands.get(1));
			return new V(factor.multiply(scaled.x), factor.multiply(scaled.y));
		}
		case DOT: {
			V left = vector(operands.get(0));
			V right = vector(operands.get(1));
			return left.x.multiply(right.x).add(left.y.multiply(right.y));
		}
		case POINT_PLUS_VECTOR: {
			P base = point(operands.get(0));
			V offset = vector(operands.get(1));
			return step.getParameter(0) > 0
					? unit(base.inhomX.add(offset.x), base.inhomY.add(offset.y))
					: unit(base.inhomX.subtract(offset.x), base.inhomY.subtract(offset.y));
		}
		case METRIC_INPUT:
			throw new Stop(Failure.OUT_OF_CLASS, node, "A structural metric input has no exact value");
		default:
			return transform(step, point(operands.get(0)));
		}
	}

	private void requireDistinct(List<Operand> operands) {
		Operand first = operands.get(0);
		Operand second = operands.get(1);
		if (first.getKind() == OperandKind.NODE && second.getKind() == OperandKind.NODE
				&& first.getNode() == second.getNode()) {
			throw new Stop(Failure.INVALID, node, "The two points are the same operand");
		}
	}

	private L join(P p, P q, boolean bounded, boolean ray) {
		return defined(p.inhomY.subtract(q.inhomY), q.inhomX.subtract(p.inhomX),
				p.inhomX.multiply(q.inhomY).subtract(p.inhomY.multiply(q.inhomX)), bounded, ray,
				bounded ? p.inhomX : null, bounded ? p.inhomY : null);
	}

	/** GeoGebra line definedness: {@code |a| >= EPSILON} or {@code |b| >= EPSILON}. */
	private L defined(J a, J b, J c, boolean bounded, boolean ray, J startX, J startY) {
		if (atLeast(a.v, epsilon) || atLeast(b.v, epsilon)) {
			return new L(a, b, c, bounded, ray, startX, startY);
		}
		if (a.sign() == 0 && b.sign() == 0) {
			throw new Stop(Failure.INVALID, node, "Zero line direction");
		}
		throw new Stop(Failure.OUT_OF_CLASS, node, "Line definedness depends on the inherited threshold");
	}

	/** AlgoOrthoLinePointLine: cross(P, (l.a, l.b, 0)), then the power-of-two normalization. */
	private L perpendicular(P p, L l) {
		J[] coefficients = {p.z.multiply(l.b).negate(), p.z.multiply(l.a),
				p.x.multiply(l.b).subtract(p.y.multiply(l.a))};
		if (allBelow(coefficients, epsilon)) {
			return defined(coefficients[0], coefficients[1], coefficients[2], false, false, null,
					null);
		}
		for (int iteration = 0; allBelow(coefficients, normalizationMinimum); iteration++) {
			requireIterations(iteration);
			coefficients = scale(coefficients, 2);
		}
		for (int iteration = 0; allAbove(coefficients, normalizationMaximum); iteration++) {
			requireIterations(iteration);
			coefficients = scale(coefficients, 0.5);
		}
		return defined(coefficients[0], coefficients[1], coefficients[2], false, false, null, null);
	}

	private P meet(L g, L h) {
		P point = normalized(g.b.multiply(h.c).subtract(g.c.multiply(h.b)),
				g.c.multiply(h.a).subtract(g.a.multiply(h.c)),
				g.a.multiply(h.b).subtract(g.b.multiply(h.a)));
		requireMember(g, point.inhomX, point.inhomY);
		requireMember(h, point.inhomX, point.inhomY);
		return point;
	}

	/** GeoPoint finiteness and the sign of z, decided exactly (section 5.1). */
	private P normalized(J x, J y, J z) {
		Num size = abs(z.v);
		boolean finite = greater(size, epsilon) || greater(size, abs(x.v).multiply(epsilon))
				|| greater(size, abs(y.v).multiply(epsilon));
		if (!finite) {
			if (z.sign() == 0) {
				throw new Stop(Failure.INVALID, node, "Point at infinity");
			}
			throw new Stop(Failure.OUT_OF_CLASS, node, "Point finiteness depends on the inherited threshold");
		}
		if (z.sign() < 0) {
			x = x.negate();
			y = y.negate();
			z = z.negate();
		}
		return new P(x, y, z, x.divide(z), y.divide(z), false);
	}

	private P transform(Step step, P p) {
		switch (step.getOperation()) {
		case TRANSLATE:
			return normalized(p.x.add(constant(step.getParameter(0)).multiply(p.z)),
					p.y.add(constant(step.getParameter(1)).multiply(p.z)), p.z);
		case EXPRESSION_TRANSLATE: {
			P u = unit(p.inhomX, p.inhomY);
			return normalized(u.x.add(constant(step.getParameter(0))),
					u.y.add(constant(step.getParameter(1))), u.z);
		}
		case ROTATE: {
			J cosine = constant(step.getParameter(0));
			J sine = constant(step.getParameter(1));
			return normalized(p.x.multiply(cosine).subtract(p.y.multiply(sine)),
					p.x.multiply(sine).add(p.y.multiply(cosine)), p.z);
		}
		case ROTATE_ABOUT: {
			J cosine = constant(step.getParameter(0));
			J sine = constant(step.getParameter(1));
			J qx = p.z.multiply(constant(step.getParameter(2)));
			J qy = p.z.multiply(constant(step.getParameter(3)));
			J dx = p.x.subtract(qx);
			return normalized(dx.multiply(cosine).add(qy.subtract(p.y).multiply(sine)).add(qx),
					dx.multiply(sine).add(p.y.subtract(qy).multiply(cosine)).add(qy), p.z);
		}
		case MIRROR_POINT: {
			J two = constant(2);
			return normalized(two.multiply(p.z).multiply(constant(step.getParameter(0)))
					.subtract(p.x), two.multiply(p.z).multiply(constant(step.getParameter(1)))
					.subtract(p.y), p.z);
		}
		case MIRROR_LINE: {
			J offset = p.z.negate().multiply(constant(step.getParameter(2)));
			boolean horizontalOffset = step.getParameter(3) == 1;
			J qx = horizontalOffset ? offset.divide(constant(step.getParameter(0)))
					: constant(0);
			J qy = horizontalOffset ? constant(0)
					: offset.divide(constant(step.getParameter(1)));
			J cosine = constant(step.getParameter(4));
			J sine = constant(step.getParameter(5));
			J u = p.x.subtract(qx);
			J w = p.y.subtract(qy);
			return normalized(u.multiply(cosine).add(w.multiply(sine)).add(qx),
					u.multiply(sine).subtract(w.multiply(cosine)).add(qy), p.z);
		}
		case DILATE: {
			J factor = constant(step.getParameter(0));
			J complement = constant(1).subtract(factor);
			return normalized(factor.multiply(p.x).add(complement.multiply(
					constant(step.getParameter(1))).multiply(p.z)),
					factor.multiply(p.y).add(complement.multiply(
							constant(step.getParameter(2))).multiply(p.z)), p.z);
		}
		default:
			throw new Stop(Failure.OUT_OF_CLASS, node, "Unsupported operation "
					+ step.getOperation());
		}
	}

	private C circleThrough(List<Operand> operands) {
		requireDistinct(operands);
		P center = point(operands.get(0));
		P through = point(operands.get(1));
		J squared = through.inhomX.subtract(center.inhomX).square()
				.add(through.inhomY.subtract(center.inhomY).square());
		if (greater(squared.v, radiusSquaredMinimum)) {
			return new C(center.inhomX, center.inhomY, squared);
		}
		if (squared.sign() == 0) {
			throw new Stop(Failure.INVALID, node, "Zero radius");
		}
		throw new Stop(Failure.OUT_OF_CLASS, node, "The inherited circle type depends on its threshold");
	}

	/** One output of a line-circle intersection with its exact selection (section 10.2). */
	private P output(Pair pair, int index) {
		L line = pair.line;
		C circle = pair.circle;
		J norm = line.a.square().add(line.b.square());
		J distance = line.a.multiply(circle.mx).add(line.b.multiply(circle.my)).add(line.c);
		J discriminant = circle.radiusSquared.multiply(norm).subtract(distance.square());
		int sign = discriminant.sign();
		if (sign < 0) {
			throw new Stop(Failure.INVALID, node, "Negative discriminant");
		}
		if (sign == 0) {
			throw new Stop(Failure.SELECTION_NOT_RECONSTRUCTED, node, "Coincident exact roots");
		}
		J root = discriminant.sqrt();
		J footX = circle.mx.subtract(distance.multiply(line.a).divide(norm));
		J footY = circle.my.subtract(distance.multiply(line.b).divide(norm));
		J offsetX = root.multiply(line.b).divide(norm);
		J offsetY = root.multiply(line.a).divide(norm).negate();
		int selected;
		if (pair.compaction) {
			selected = -1;
			int count = 0;
			for (int raw = 0; raw < 2 && selected < 0; raw++) {
				if (member(line, circle, root, raw)) {
					if (count == index) {
						selected = raw;
					}
					count++;
				}
			}
			if (selected < 0) {
				throw new Stop(Failure.INVALID, node, "Fewer roots lie on the path than the index");
			}
		} else {
			selected = pair.permutation[index];
			if (line.bounded && !member(line, circle, root, selected)) {
				throw new Stop(Failure.INVALID, node, "The selected root is not on the path");
			}
		}
		if (index == 1 && !pair.labelled[1]) {
			Num scale = max(line.a.v.square(), max(line.b.v.square(), line.c.v.square()));
			Num rounding = distance.v.square().add(circle.radiusSquared.v.multiply(norm.v));
			if (!greater(discriminant.v, coincidentWindowFactor.multiply(scale))
					|| !greater(discriminant.v, coincidentRoundingFactor.multiply(rounding))) {
				throw new Stop(Failure.SELECTION_NOT_RECONSTRUCTED, node,
						"An unlabelled second output inside the coincidence band");
			}
		}
		Num rootX0 = footX.v.subtract(offsetX.v);
		Num rootY0 = footY.v.subtract(offsetY.v);
		Num rootX1 = footX.v.add(offsetX.v);
		Num rootY1 = footY.v.add(offsetY.v);
		selections.add(new Selection(node, selected, new Num[] {rootX0, rootY0, rootX1, rootY1}));
		J signed = constant(selected == 0 ? -1 : 1);
		return unit(footX.add(signed.multiply(offsetX)), footY.add(signed.multiply(offsetY)));
	}

	/** Membership of a raw root in a bounded line, decided exactly. */
	private boolean member(L line, C circle, J root, int raw) {
		if (!line.bounded) {
			return true;
		}
		J base = circle.mx.subtract(line.startX).multiply(line.b)
				.subtract(circle.my.subtract(line.startY).multiply(line.a));
		J parameter = raw == 0 ? base.subtract(root) : base.add(root);
		return memberParameter(line, parameter);
	}

	private void requireMember(L line, J x, J y) {
		if (!line.bounded) {
			return;
		}
		J parameter = x.subtract(line.startX).multiply(line.b)
				.subtract(y.subtract(line.startY).multiply(line.a));
		if (!memberParameter(line, parameter)) {
			throw new Stop(Failure.INVALID, node, "The point is not on the bounded line");
		}
	}

	/** @param parameter {@code (X - O).(b, -a)}: nonnegative for a ray, in [0, a^2+b^2] */
	private boolean memberParameter(L line, J parameter) {
		if (parameter.sign() < 0) {
			return false;
		}
		if (line.ray) {
			return true;
		}
		return line.a.v.square().add(line.b.v.square()).subtract(parameter.v).signum() >= 0;
	}

	private V normalizeVector(V source) {
		J squared = source.x.square().add(source.y.square());
		if (squared.sign() <= 0) {
			throw new Stop(Failure.INVALID, node, "Zero vector");
		}
		J length = squared.sqrt();
		return new V(source.x.divide(length), source.y.divide(length));
	}

	private P point(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			return (P) nodes[operand.getNode()];
		}
		J z = constant(operand.getValue(2));
		boolean unit = operand.getValue(2) == 1;
		return new P(constant(operand.getValue(0)), constant(operand.getValue(1)), z,
				constant(operand.getValue(3)), constant(operand.getValue(4)), unit);
	}

	private L line(Operand operand) {
		switch (operand.getKind()) {
		case NODE:
			return (L) nodes[operand.getNode()];
		case RAY:
		case SEGMENT:
			return new L(constant(operand.getValue(0)), constant(operand.getValue(1)),
					constant(operand.getValue(2)), true, operand.getKind() == OperandKind.RAY,
					constant(operand.getValue(3)), constant(operand.getValue(4)));
		default:
			return new L(constant(operand.getValue(0)), constant(operand.getValue(1)),
					constant(operand.getValue(2)), false, false, null, null);
		}
	}

	/** A circle node, or a constant circle of matrix (1, 1, A2, 0, A4, A5). */
	private C circle(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			return (C) nodes[operand.getNode()];
		}
		J a4 = constant(operand.getValue(1));
		J a5 = constant(operand.getValue(2));
		return new C(a4.negate(), a5.negate(),
				a4.square().add(a5.square()).subtract(constant(operand.getValue(0))));
	}

	private V vector(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			return (V) nodes[operand.getNode()];
		}
		return new V(constant(operand.getValue(0)), constant(operand.getValue(1)));
	}

	private J scalar(Operand operand) {
		if (operand.getKind() == OperandKind.NODE) {
			return (J) nodes[operand.getNode()];
		}
		return constant(operand.getValue(0));
	}

	private boolean allBelow(J[] values, Num threshold) {
		for (J value : values) {
			if (!greater(threshold, abs(value.v))) {
				return false;
			}
		}
		return true;
	}

	private boolean allAbove(J[] values, Num threshold) {
		for (J value : values) {
			if (!greater(abs(value.v), threshold)) {
				return false;
			}
		}
		return true;
	}

	private J[] scale(J[] values, double factor) {
		J multiplier = constant(factor);
		J[] result = new J[values.length];
		for (int index = 0; index < values.length; index++) {
			result[index] = values[index].multiply(multiplier);
		}
		return result;
	}

	private void requireIterations(int iteration) {
		if (iteration >= 2048) {
			throw new Stop(Failure.OUT_OF_CLASS, node, "Normalization does not terminate");
		}
	}

	private Num abs(Num value) {
		return value.signum() < 0 ? value.negate() : value;
	}

	private Num max(Num first, Num second) {
		return first.subtract(second).signum() >= 0 ? first : second;
	}

	private boolean greater(Num left, Num right) {
		return left.subtract(right).signum() > 0;
	}

	private boolean atLeast(Num value, Num threshold) {
		return abs(value).subtract(threshold).signum() >= 0;
	}
}
