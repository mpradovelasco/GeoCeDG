/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionContext2D
		.ComponentAddress;

/**
 * Bounded current-state spline-pair proof, separate from discovery and identity.
 * A strict real Krawczyk inclusion proves one root in a C1 neighborhood
 * (Rump, Acta Numerica 19 (2010), Theorem 13.3). A second, class-specific
 * covering argument is required before that root's transverse-germ class is
 * UNIQUE. Neither numerical enumeration nor proximity supplies that proof.
 */
public final class SplinePairIntervalCertification2D {
	private static final int REFINEMENT_STEPS = 8;
	private final LocusPairIntersectionContext2D context;
	private final LocusPairIntersectionWorkBudget2D budget;
	private int boxesVisited;
	private int krawczykAttempts;
	private int rootCount;

	/** Current evidence for one oriented component-pair transverse-germ class. */
	public enum ClassStatus {
		UNIQUE, MULTIPLE, UNRESOLVED, ABSENT
	}

	private SplinePairIntervalCertification2D(LocusPairIntersectionContext2D context) {
		this.context = context;
		budget = context.getQuery().getPolicy().getPairWorkBudget();
	}

	/**
	 * Resolves the represented spline coefficients once for one coherent query.
	 * Unsupported generic polynomial evaluators never gain spline certificates.
	 *
	 * @param context current canonical-source pair context
	 * @return current proofs and bounded work; no durable identity allocation
	 */
	public static Result certify(LocusPairIntersectionContext2D context) {
		return new SplinePairIntervalCertification2D(
				java.util.Objects.requireNonNull(context)).run();
	}

	private Result run() {
		ArrayList<ClassCertificate> classes = new ArrayList<>();
		ArrayList<RootCertificate> roots = new ArrayList<>();
		int componentPairs = 0;
		boolean supported = true;
		for (ComponentAddress first : context.getFirstComponents()) {
			for (ComponentAddress second : context.getSecondComponents()) {
				componentPairs++;
				SplineIntervalModel2D firstModel;
				SplineIntervalModel2D secondModel;
				try {
					firstModel = SplineIntervalModel2D.capture(
							context.getFirstDefinition(), first.getBranchKey());
					secondModel = SplineIntervalModel2D.capture(
							context.getSecondDefinition(), second.getBranchKey());
				} catch (IllegalArgumentException | ArithmeticException exception) {
					firstModel = null;
					secondModel = null;
				}
				if (firstModel == null || secondModel == null) {
					supported = false;
					unresolvedClasses(classes, first, second);
					continue;
				}
				if (componentPairs > budget.getMaximumComponentPairs()) {
					unresolvedClasses(classes, first, second);
					continue;
				}
				ComponentProof proof = new ComponentProof(first, second, firstModel,
						secondModel);
				proof.compute();
				roots.addAll(proof.roots);
				classes.add(proof.classify(1));
				classes.add(proof.classify(-1));
			}
		}
		return new Result(supported, classes, roots, boxesVisited, krawczykAttempts,
				"Outward real Krawczyk plus component-product germ coverage; "
						+ "captured represented spline authority, not exact interpolation");
	}

	private static void unresolvedClasses(List<ClassCertificate> classes,
			ComponentAddress first, ComponentAddress second) {
		classes.add(new ClassCertificate(first, second, 1, ClassStatus.UNRESOLVED, null));
		classes.add(new ClassCertificate(first, second, -1, ClassStatus.UNRESOLVED, null));
	}

	private final class ComponentProof {
		private final ComponentAddress first;
		private final ComponentAddress second;
		private final SplineIntervalModel2D firstModel;
		private final SplineIntervalModel2D secondModel;
		private final double firstPeriod;
		private final double secondPeriod;
		private final int orientation;
		private final List<RootCertificate> roots = new ArrayList<>();
		private final List<Leaf> unresolved = new ArrayList<>();

		ComponentProof(ComponentAddress first, ComponentAddress second,
				SplineIntervalModel2D firstModel, SplineIntervalModel2D secondModel) {
			this.first = first;
			this.second = second;
			this.firstModel = firstModel;
			this.secondModel = secondModel;
			firstPeriod = firstModel.period(first.getInterval());
			secondPeriod = secondModel.period(second.getInterval());
			orientation = orientation(context.getFirstDefinition(), first)
					* orientation(context.getSecondDefinition(), second);
		}

		void compute() {
			ArrayDeque<Box> queue = new ArrayDeque<>();
			for (SplineOutwardInterval2D u : spans(firstModel, first.getInterval())) {
				for (SplineOutwardInterval2D v : spans(secondModel, second.getInterval())) {
					queue.addLast(new Box(u, v, 0));
				}
			}
			while (!queue.isEmpty()) {
				Box box = queue.removeFirst();
				if (boxesVisited >= budget.getMaximumParameterBoxes()) {
					unresolved.add(new Leaf(box, 0));
					for (Box remaining : queue) {
						unresolved.add(new Leaf(remaining, 0));
					}
					break;
				}
				boxesVisited++;
				int germ = 0;
				try {
					if (coveredByRoot(box)) {
						continue;
					}
					SplineOutwardInterval2D[] residual = residual(box.u, box.v);
					if (!residual[0].containsZero() || !residual[1].containsZero()) {
						continue;
					}
					germ = firstModel.isSmooth(box.u) && secondModel.isSmooth(box.v)
							? germ(jacobian(box.u, box.v)) : 0;
					RootCertificate root = certifyBox(box);
					if (root != null) {
						addRoot(root);
						continue;
					}
					if (excludesRoot(box)) {
						continue;
					}
				} catch (ArithmeticException exception) {
					unresolved.add(new Leaf(box, 0));
					continue;
				}
				if (box.depth >= budget.getMaximumBoxDepth()
						|| !subdivide(box, queue)) {
					unresolved.add(new Leaf(box, germ));
				}
			}
		}

		private RootCertificate certifyBox(Box cell) {
			if (rootCount >= budget.getMaximumPublishedSolutions()) {
				return null;
			}
			// Prefer a proof covering neighboring computational boundaries. If
			// expansion introduces a critical point, the original cell may still
			// prove uniqueness under the identical criteria and work budget.
			Box proof = new Box(expand(cell.u, first.getInterval(), firstPeriod),
					expand(cell.v, second.getInterval(), secondPeriod), cell.depth);
			RootCertificate expanded = certifyProof(proof);
			return expanded == null ? certifyProof(cell) : expanded;
		}

		private boolean excludesRoot(Box cell) {
			if (!firstModel.isSmooth(cell.u) || !secondModel.isSmooth(cell.v)) {
				return false;
			}
			// Every zero in X also belongs to K(X). A disjoint intersection
			// excludes the cell even when residual interval dependency is too
			// wide. This is proof, not candidate-distance or tolerance merging.
			Box image = krawczyk(cell, jacobian(cell.u, cell.v));
			return image != null && (cell.u.intersection(image.u) == null
					|| cell.v.intersection(image.v) == null);
		}

		private RootCertificate certifyProof(Box proof) {
			if (!firstModel.isSmooth(proof.u) || !secondModel.isSmooth(proof.v)) {
				return null;
			}
			SplineOutwardInterval2D[][] jacobian = jacobian(proof.u, proof.v);
			int germ = germ(jacobian);
			if (germ == 0) {
				return null;
			}
			Box image = krawczyk(proof, jacobian);
			if (image == null || !proof.strictlyContains(image)) {
				return null;
			}
			for (int step = 0; step < REFINEMENT_STEPS; step++) {
				Box refined = krawczyk(image, jacobian(image.u, image.v));
				if (refined == null) {
					break;
				}
				SplineOutwardInterval2D u = image.u.intersection(refined.u);
				SplineOutwardInterval2D v = image.v.intersection(refined.v);
				if (u == null || v == null) {
					throw new ArithmeticException("Inconsistent Krawczyk enclosure");
				}
				image = new Box(u, v, proof.depth);
			}
			ChartCoordinate u = canonicalChart(image.u, proof.u, firstModel,
					first.getInterval(), firstPeriod);
			ChartCoordinate v = canonicalChart(image.v, proof.v, secondModel,
					second.getInterval(), secondPeriod);
			if (u == null || v == null) {
				return null;
			}
			return new RootCertificate(first, second, germ, u, v);
		}

		private Box krawczyk(Box box, SplineOutwardInterval2D[][] jacobian) {
			if (krawczykAttempts >= budget.getMaximumJacobianEvaluations()) {
				return null;
			}
			krawczykAttempts++;
			double a = jacobian[0][0].midpoint();
			double b = jacobian[0][1].midpoint();
			double c = jacobian[1][0].midpoint();
			double d = jacobian[1][1].midpoint();
			double determinant = a * d - b * c;
			if (!Double.isFinite(determinant) || determinant == 0) {
				return null;
			}
			double[][] inverse = {{d / determinant, -b / determinant},
					{-c / determinant, a / determinant}};
			SplineOutwardInterval2D inverseDeterminant =
					SplineOutwardInterval2D.point(inverse[0][0])
							.multiply(SplineOutwardInterval2D.point(inverse[1][1]))
							.subtract(SplineOutwardInterval2D.point(inverse[0][1])
									.multiply(SplineOutwardInterval2D.point(inverse[1][0])));
			if (inverseDeterminant.containsZero()) {
				return null;
			}
			SplineOutwardInterval2D[] center = {
					SplineOutwardInterval2D.point(box.u.midpoint()),
					SplineOutwardInterval2D.point(box.v.midpoint())};
			SplineOutwardInterval2D[] residual = residual(center[0], center[1]);
			SplineOutwardInterval2D[] offset = {
					box.u.subtract(center[0]), box.v.subtract(center[1])};
			SplineOutwardInterval2D[] image = new SplineOutwardInterval2D[2];
			for (int row = 0; row < 2; row++) {
				SplineOutwardInterval2D value = center[row];
				for (int entry = 0; entry < 2; entry++) {
					value = value.subtract(SplineOutwardInterval2D.point(inverse[row][entry])
							.multiply(residual[entry]));
				}
				for (int column = 0; column < 2; column++) {
					SplineOutwardInterval2D factor =
							SplineOutwardInterval2D.point(row == column ? 1 : 0);
					for (int entry = 0; entry < 2; entry++) {
						factor = factor.subtract(
								SplineOutwardInterval2D.point(inverse[row][entry])
										.multiply(jacobian[entry][column]));
					}
					value = value.add(factor.multiply(offset[column]));
				}
				image[row] = value;
			}
			return new Box(image[0], image[1], box.depth);
		}

		private SplineOutwardInterval2D[] residual(SplineOutwardInterval2D u,
				SplineOutwardInterval2D v) {
			SplineOutwardInterval2D[] value = rawResidual(u, v);
			if (!firstModel.isSmooth(u) || !secondModel.isSmooth(v)) {
				return value;
			}
			SplineOutwardInterval2D centerU = SplineOutwardInterval2D.point(u.midpoint());
			SplineOutwardInterval2D centerV = SplineOutwardInterval2D.point(v.midpoint());
			SplineOutwardInterval2D[] center = rawResidual(centerU, centerV);
			SplineOutwardInterval2D[][] derivative = jacobian(u, v);
			for (int row = 0; row < 2; row++) {
				// The mean-value enclosure is independent of the natural Horner
				// enclosure; their intersection is rigorous and limits dependency
				// overestimation without changing the work budget or tolerance.
				SplineOutwardInterval2D centered = center[row]
						.add(derivative[row][0].multiply(u.subtract(centerU)))
						.add(derivative[row][1].multiply(v.subtract(centerV)));
				value[row] = value[row].intersection(centered);
				if (value[row] == null) {
					throw new ArithmeticException("Inconsistent residual enclosures");
				}
			}
			return value;
		}

		private SplineOutwardInterval2D[] rawResidual(SplineOutwardInterval2D u,
				SplineOutwardInterval2D v) {
			SplineOutwardInterval2D[] a = firstModel.evaluate(u, false);
			SplineOutwardInterval2D[] b = secondModel.evaluate(v, false);
			return new SplineOutwardInterval2D[] {a[0].subtract(b[0]), a[1].subtract(b[1])};
		}

		private SplineOutwardInterval2D[][] jacobian(SplineOutwardInterval2D u,
				SplineOutwardInterval2D v) {
			SplineOutwardInterval2D[] a = firstModel.evaluate(u, true);
			SplineOutwardInterval2D[] b = secondModel.evaluate(v, true);
			return new SplineOutwardInterval2D[][] {
					{a[0], b[0].negate()}, {a[1], b[1].negate()}};
		}

		private int germ(SplineOutwardInterval2D[][] jacobian) {
			SplineOutwardInterval2D determinant = jacobian[0][0].multiply(jacobian[1][1])
					.subtract(jacobian[0][1].multiply(jacobian[1][0]));
			return determinant.lower > 0 ? orientation
					: determinant.upper < 0 ? -orientation : 0;
		}

		private boolean coveredByRoot(Box box) {
			for (RootCertificate root : roots) {
				if (containsPeriodic(root.proof(), box, firstPeriod, secondPeriod)) {
					return true;
				}
			}
			return false;
		}

		private void addRoot(RootCertificate candidate) {
			for (RootCertificate root : roots) {
				if (sameRoot(root, candidate)) {
					return;
				}
			}
			roots.add(candidate);
			rootCount++;
		}

		private boolean sameRoot(RootCertificate a, RootCertificate b) {
			return containsPeriodic(a.proof(), b.enclosure(), firstPeriod, secondPeriod)
					|| containsPeriodic(b.proof(), a.enclosure(), firstPeriod, secondPeriod);
		}

		ClassCertificate classify(int germ) {
			ArrayList<RootCertificate> selected = new ArrayList<>();
			for (RootCertificate root : roots) {
				if (root.germ == germ) {
					selected.add(root);
				}
			}
			for (int a = 0; a < selected.size(); a++) {
				for (int b = a + 1; b < selected.size(); b++) {
					if (distinct(selected.get(a).enclosure(), selected.get(b).enclosure(),
							firstPeriod, secondPeriod)) {
						return new ClassCertificate(first, second, germ,
								ClassStatus.MULTIPLE, null);
					}
				}
			}
			boolean complete = selected.size() <= 1;
			String witness = selected.size() > 1
					? "Certified neighborhoods lack a common-root proof" : "";
			for (Leaf leaf : unresolved) {
				if (leaf.germ != -germ && !coveredByRoot(leaf.box)) {
					complete = false;
					if (witness.isEmpty()) {
						witness = "Unresolved coverage u=" + leaf.box.u.lower + ":"
								+ leaf.box.u.upper + ",v=" + leaf.box.v.lower + ":"
								+ leaf.box.v.upper + ",germ=" + leaf.germ;
					}
				}
			}
			ClassStatus status = !complete ? ClassStatus.UNRESOLVED
					: selected.isEmpty() ? ClassStatus.ABSENT : ClassStatus.UNIQUE;
			return new ClassCertificate(first, second, germ, status,
					status == ClassStatus.UNIQUE ? selected.get(0) : null, witness);
		}
	}

	private static List<SplineOutwardInterval2D> spans(SplineIntervalModel2D model,
			LocusInterval2D component) {
		ArrayList<SplineOutwardInterval2D> spans = new ArrayList<>();
		double[] knots = model.getKnots();
		for (int span = 0; span + 1 < knots.length; span++) {
			double lower = Math.max(knots[span], component.getLower());
			double upper = Math.min(knots[span + 1], component.getUpper());
			if (lower < upper) {
				spans.add(new SplineOutwardInterval2D(lower, upper));
			}
		}
		return spans;
	}

	private static int orientation(LocusDefinition2D definition, ComponentAddress component) {
		return definition.getBranch(component.getBranchKey()).getOrientation()
				== Orientation.INCREASING ? 1 : -1;
	}

	private static boolean subdivide(Box box, ArrayDeque<Box> queue) {
		boolean first = box.u.width() >= box.v.width();
		SplineOutwardInterval2D axis = first ? box.u : box.v;
		double middle = axis.midpoint();
		if (!(axis.lower < middle && middle < axis.upper)) {
			return false;
		}
		SplineOutwardInterval2D low = new SplineOutwardInterval2D(axis.lower, middle);
		SplineOutwardInterval2D high = new SplineOutwardInterval2D(middle, axis.upper);
		queue.addLast(new Box(first ? low : box.u, first ? box.v : low, box.depth + 1));
		queue.addLast(new Box(first ? high : box.u, first ? box.v : high, box.depth + 1));
		return true;
	}

	private static SplineOutwardInterval2D expand(SplineOutwardInterval2D interval,
			LocusInterval2D component, double period) {
		double extension = interval.width() / 2;
		double lower = interval.lower - extension;
		double upper = interval.upper + extension;
		if (period == 0) {
			lower = Math.max(component.getLower(), lower);
			upper = Math.min(component.getUpper(), upper);
		}
		return new SplineOutwardInterval2D(lower, upper);
	}

	private static ChartCoordinate canonicalChart(SplineOutwardInterval2D image,
			SplineOutwardInterval2D proof, SplineIntervalModel2D model,
			LocusInterval2D component, double period) {
		double parameter = model.canonical(image.midpoint());
		if (period != 0
				&& (image.lower <= component.getLower()
						&& component.getLower() <= image.upper
						|| image.lower <= component.getUpper()
								&& component.getUpper() <= image.upper)) {
			// Use the canonical seam as the floating representative when it is
			// inside the certified root enclosure. A midpoint just below the
			// excluded endpoint can otherwise be rejected by the provider's
			// endpoint policy. This is an enclosed approximation, not an assertion
			// that the exact root is at the seam, and never defines token identity.
			// The ordinary source/residual check still must accept it.
			parameter = component.getLower();
		}
		if (!component.contains(parameter, 0)) {
			return null;
		}
		for (int cycle = period == 0 ? 0 : -1; cycle <= (period == 0 ? 0 : 1); cycle++) {
			double displacement = cycle * period;
			SplineOutwardInterval2D shift = SplineOutwardInterval2D.point(displacement);
			SplineOutwardInterval2D canonicalImage = image.add(shift);
			if (canonicalImage.lower <= parameter && parameter <= canonicalImage.upper
					&& (period != 0 || containedInComponent(canonicalImage, component))) {
				// Existence enclosures widen outward; a uniqueness neighborhood
				// must instead remain a subset of the translated proved region.
				SplineOutwardInterval2D canonicalProof = displacement == 0 ? proof
						: new SplineOutwardInterval2D(
								Math.nextUp(proof.lower + displacement),
								Math.nextDown(proof.upper + displacement));
				if (canonicalProof.contains(canonicalImage)) {
					return new ChartCoordinate(parameter, canonicalImage, canonicalProof);
				}
			}
		}
		return null;
	}

	private static boolean containedInComponent(SplineOutwardInterval2D image,
			LocusInterval2D component) {
		return (component.isLowerClosed() ? image.lower >= component.getLower()
				: image.lower > component.getLower())
				&& (component.isUpperClosed() ? image.upper <= component.getUpper()
						: image.upper < component.getUpper());
	}

	private static boolean containsPeriodic(Box container, Box inside,
			double firstPeriod, double secondPeriod) {
		for (int first = firstPeriod == 0 ? 0 : -1;
				first <= (firstPeriod == 0 ? 0 : 1); first++) {
			for (int second = secondPeriod == 0 ? 0 : -1;
					second <= (secondPeriod == 0 ? 0 : 1); second++) {
				if (container.u.contains(inside.u.add(
						SplineOutwardInterval2D.point(first * firstPeriod)))
						&& container.v.contains(inside.v.add(
								SplineOutwardInterval2D.point(second * secondPeriod)))) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean distinct(Box a, Box b, double firstPeriod, double secondPeriod) {
		for (int first = firstPeriod == 0 ? 0 : -1;
				first <= (firstPeriod == 0 ? 0 : 1); first++) {
			for (int second = secondPeriod == 0 ? 0 : -1;
					second <= (secondPeriod == 0 ? 0 : 1); second++) {
				if (!a.u.disjoint(b.u.add(SplineOutwardInterval2D.point(first * firstPeriod)))
						&& !a.v.disjoint(b.v.add(
								SplineOutwardInterval2D.point(second * secondPeriod)))) {
					return false;
				}
			}
		}
		return true;
	}

	/** Immutable complete query-local certificate outcome. */
	public static final class Result {
		private final boolean supported;
		private final List<ClassCertificate> classes;
		private final List<RootCertificate> roots;
		private final int boxesVisited;
		private final int krawczykAttempts;
		private final String diagnostic;

		Result(boolean supported, List<ClassCertificate> classes,
				List<RootCertificate> roots, int boxesVisited, int krawczykAttempts,
				String diagnostic) {
			this.supported = supported;
			this.classes = Collections.unmodifiableList(new ArrayList<>(classes));
			this.roots = Collections.unmodifiableList(new ArrayList<>(roots));
			this.boxesVisited = boxesVisited;
			this.krawczykAttempts = krawczykAttempts;
			this.diagnostic = diagnostic;
		}

		public boolean isSupported() {
			return supported;
		}

		public List<ClassCertificate> getClasses() {
			return classes;
		}

		public List<RootCertificate> getRoots() {
			return roots;
		}

		public int getBoxesVisited() {
			return boxesVisited;
		}

		public int getKrawczykAttempts() {
			return krawczykAttempts;
		}

		public String getDiagnostic() {
			return diagnostic;
		}
	}

	/** A proof about one germ class, not a statement of global completeness. */
	public static final class ClassCertificate {
		private final ComponentAddress first;
		private final ComponentAddress second;
		private final int germ;
		private final ClassStatus status;
		private final RootCertificate uniqueRoot;
		private final String diagnostic;

		ClassCertificate(ComponentAddress first, ComponentAddress second, int germ,
				ClassStatus status, RootCertificate uniqueRoot) {
			this(first, second, germ, status, uniqueRoot, "");
		}

		ClassCertificate(ComponentAddress first, ComponentAddress second, int germ,
				ClassStatus status, RootCertificate uniqueRoot, String diagnostic) {
			this.first = first;
			this.second = second;
			this.germ = germ;
			this.status = status;
			this.uniqueRoot = uniqueRoot;
			this.diagnostic = diagnostic;
		}

		public String getFirstBranchKey() {
			return first.getBranchKey();
		}

		public String getSecondBranchKey() {
			return second.getBranchKey();
		}

		public String getFirstComponentKey() {
			return first.getComponentKey();
		}

		public String getSecondComponentKey() {
			return second.getComponentKey();
		}

		public int getGerm() {
			return germ;
		}

		public ClassStatus getStatus() {
			return status;
		}

		public RootCertificate getUniqueRoot() {
			return uniqueRoot;
		}

		public String getDiagnostic() {
			return diagnostic;
		}
	}

	/** Current root enclosure and a separately certified uniqueness neighborhood. */
	public static final class RootCertificate {
		private final ComponentAddress first;
		private final ComponentAddress second;
		private final int germ;
		private final ChartCoordinate u;
		private final ChartCoordinate v;

		RootCertificate(ComponentAddress first, ComponentAddress second, int germ,
				ChartCoordinate u, ChartCoordinate v) {
			this.first = first;
			this.second = second;
			this.germ = germ;
			this.u = u;
			this.v = v;
		}

		public String getFirstBranchKey() {
			return first.getBranchKey();
		}

		public String getSecondBranchKey() {
			return second.getBranchKey();
		}

		public String getFirstComponentKey() {
			return first.getComponentKey();
		}

		public String getSecondComponentKey() {
			return second.getComponentKey();
		}

		public int getGerm() {
			return germ;
		}

		public double getFirstParameter() {
			return u.parameter;
		}

		public double getSecondParameter() {
			return v.parameter;
		}

		public LocusInterval2D getFirstRootEnclosure() {
			return interval(u.enclosure);
		}

		public LocusInterval2D getSecondRootEnclosure() {
			return interval(v.enclosure);
		}

		public LocusInterval2D getFirstIsolationInterval() {
			return interval(u.proof);
		}

		public LocusInterval2D getSecondIsolationInterval() {
			return interval(v.proof);
		}

		private Box proof() {
			return new Box(u.proof, v.proof, 0);
		}

		private Box enclosure() {
			return new Box(u.enclosure, v.enclosure, 0);
		}

		private static LocusInterval2D interval(SplineOutwardInterval2D interval) {
			return new LocusInterval2D(interval.lower, interval.upper, true, true);
		}
	}

	private static final class ChartCoordinate {
		private final double parameter;
		private final SplineOutwardInterval2D enclosure;
		private final SplineOutwardInterval2D proof;

		ChartCoordinate(double parameter, SplineOutwardInterval2D enclosure,
				SplineOutwardInterval2D proof) {
			this.parameter = parameter;
			this.enclosure = enclosure;
			this.proof = proof;
		}
	}

	private static final class Box {
		private final SplineOutwardInterval2D u;
		private final SplineOutwardInterval2D v;
		private final int depth;

		Box(SplineOutwardInterval2D u, SplineOutwardInterval2D v, int depth) {
			this.u = u;
			this.v = v;
			this.depth = depth;
		}

		boolean strictlyContains(Box other) {
			return u.strictlyContains(other.u) && v.strictlyContains(other.v);
		}
	}

	private static final class Leaf {
		private final Box box;
		private final int germ;

		Leaf(Box box, int germ) {
			this.box = box;
			this.germ = germ;
		}
	}
}
