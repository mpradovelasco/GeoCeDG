/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;

import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Driver;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * Rigorous facts of the source of an oriented length that the structural certificate of
 * the length needs ({@code locus-v2-metrics.md} section 26; oriented length design section
 * 5): an outward-rounded lower bound of a chord between two certified points, which
 * certifies that the jump of the length is positive, and an upper bound of the length of a
 * closed periodic source from enclosures of its speed ({@code L <= sum of width times
 * sup |F'|}), which certifies that it is rectifiable. Both come from interval enclosures of
 * the class-v3 program of the source; neither reads a metric value.
 */
public final class RouteSourceFacts2D {
	/** Version of the method. */
	public static final String VERSION = "route-source-facts/v3";
	/** Depth of the initial dyadic cover of the length bound. */
	static final int COVER_DEPTH = 6;
	/** Deepest refinement of a box of the cover. */
	static final int MAXIMUM_DEPTH = 24;
	/** Boxes evaluated for one length bound. */
	static final int COVER_BUDGET = 2_048;
	/** Effort objective only; never a metric tolerance or a numeric guarantee. */
	private static final double RELATIVE_WIDTH_EFFORT_TARGET = 0.25;

	/** Precision effort is separate from whether the finite source cover is certified. */
	public enum PrecisionStatus {
		TARGET_REACHED, EFFORT_BUDGET, REFINEMENT_LIMIT, NO_IMPROVEMENT, NOT_AVAILABLE
	}

	private RouteSourceFacts2D() {
		// Utility class.
	}

	/**
	 * A revision-local proof partition, shared by every metric consumer of one certified
	 * component. It stores outward speed and integral enclosures, never a metric estimate.
	 * Subinterval queries reuse these cells without evaluating the construction again.
	 */
	public static final class Index {
		private final LocusInterval2D domain;
		private final ContinuousComponent component;
		private final List<Cell> cells;
		private final SplineOutwardInterval2D gapSpeed;
		private final SplineOutwardInterval2D gapIntegral;
		private final SplineOutwardInterval2D[] sums;
		private final SplineOutwardInterval2D total;
		private final int treeSize;
		private final LocusCoverageCause2D failure;
		private final int evaluatedBoxes;
		private final PrecisionStatus precisionStatus;

		private Index(LocusInterval2D domain, ContinuousComponent component, List<Cell> cells,
				SplineOutwardInterval2D gapSpeed, LocusCoverageCause2D failure, int spent) {
			this(domain, component, cells, gapSpeed, failure, spent, PrecisionStatus.NOT_AVAILABLE);
		}

		private Index(LocusInterval2D domain, ContinuousComponent component, List<Cell> cells,
				SplineOutwardInterval2D gapSpeed, LocusCoverageCause2D failure, int spent,
				PrecisionStatus precisionStatus) {
			this.domain = domain;
			this.component = component;
			this.cells = List.copyOf(cells);
			this.gapSpeed = gapSpeed;
			gapIntegral = gapSpeed == null ? SplineOutwardInterval2D.point(0)
					: new SplineOutwardInterval2D(0,
							area(gapSpeed, Math.PI, Math.nextUp(Math.PI)).upper);
			this.failure = failure;
			evaluatedBoxes = spent;
			this.precisionStatus = precisionStatus;
			int size = 1;
			while (size < cells.size()) { size <<= 1; }
			treeSize = size;
			sums = new SplineOutwardInterval2D[2 * size];
			java.util.Arrays.fill(sums, SplineOutwardInterval2D.point(0));
			for (int index = 0; index < cells.size(); index++) {
				sums[size + index] = cells.get(index).integral;
			}
			for (int index = size - 1; index > 0; index--) {
				sums[index] = positive(sums[2 * index].add(sums[2 * index + 1]));
			}
			total = positive(sums[1].add(gapIntegral));
		}

		public boolean isCertified() { return failure == null; }
		public LocusCoverageCause2D getFailureCause() { return failure; }
		public int getEvaluatedBoxes() { return evaluatedBoxes; }
		/** Refinement effort outcome, not a condition of mathematical admissibility. */
		public PrecisionStatus getPrecisionStatus() { return precisionStatus; }
		public ContinuousComponent getComponent() { return component; }

		/** The full component, with only an upper contribution for the real circle gap. */
		public double[] totalBounds() {
			return isCertified() ? bounds(total) : null;
		}

		/**
		 * Integral in increasing canonical order, optionally crossing the circle seam.
		 * Endpoints are one-sided closures of this component. No excluded interval may be
		 * skipped: a gap in the certified component makes the request unavailable.
		 */
		public double[] integral(double from, double to, boolean crossesSeam) {
			if (!isCertified() || !Double.isFinite(from) || !Double.isFinite(to)) {
				return null;
			}
			try {
				if (!crossesSeam) { return bounds(ascending(from, to)); }
				if (gapSpeed == null || from < to) { return null; }
				SplineOutwardInterval2D before = ascending(from, domain.getUpper());
				SplineOutwardInterval2D after = ascending(domain.getLower(), to);
				return before == null || after == null ? null
						: bounds(positive(before.add(gapIntegral).add(after)));
			} catch (ArithmeticException invalid) {
				return null;
			}
		}

		/** Speed on a canonical box, or on the one-sided real-circle seam enclosure. */
		public double[] speedBounds(double lower, double upper) {
			if (!isCertified() || !Double.isFinite(lower) || !Double.isFinite(upper)
					|| lower > upper) { return null; }
			if (lower >= Math.PI && upper <= Math.nextUp(Math.PI) && lower < upper) {
				return gapSpeed == null ? null : bounds(gapSpeed);
			}
			if (lower < domain.getLower() || upper > domain.getUpper()) { return null; }
			SplineOutwardInterval2D result = null;
			double cursor = lower;
			for (Cell cell : cells) {
				if (cell.upper < lower || cell.lower > upper) { continue; }
				if (cell.lower > cursor) { return null; }
				result = result == null ? cell.speed : result.hull(cell.speed);
				cursor = Math.max(cursor, Math.min(upper, cell.upper));
				if (cursor == upper && lower < upper) { break; }
			}
			return result != null && cursor == upper ? bounds(result) : null;
		}

		/** Upper correction only; binary64 2*pi is never treated as the exact period. */
		public double getSeamUpperBound() {
			return isCertified() ? gapIntegral.upper : Double.NaN;
		}

		private SplineOutwardInterval2D ascending(double lower, double upper) {
			if (lower > upper || lower < domain.getLower() || upper > domain.getUpper()) {
				return null;
			}
			if (lower == upper) {
				return coveredPoint(lower) ? SplineOutwardInterval2D.point(0) : null;
			}
			int first = -1, last = -1;
			double cursor = lower;
			for (int index = 0; index < cells.size(); index++) {
				Cell cell = cells.get(index);
				if (cell.upper <= lower || cell.lower >= upper) { continue; }
				if (cell.lower > cursor) { return null; }
				if (first < 0) { first = index; }
				last = index;
				cursor = Math.max(cursor, Math.min(upper, cell.upper));
				if (cursor == upper) { break; }
			}
			if (first < 0 || cursor != upper) { return null; }
			Cell a = cells.get(first), b = cells.get(last);
			if (first == last) { return area(a.speed, lower, upper); }
			return positive(area(a.speed, lower, a.upper)
					.add(sum(first + 1, last)).add(area(b.speed, b.lower, upper)));
		}

		private boolean coveredPoint(double parameter) {
			for (Cell cell : cells) {
				if (cell.lower <= parameter && parameter <= cell.upper) { return true; }
			}
			return false;
		}

		private SplineOutwardInterval2D sum(int lower, int upper) {
			SplineOutwardInterval2D result = SplineOutwardInterval2D.point(0);
			for (int left = lower + treeSize, right = upper + treeSize; left < right;
					left >>= 1, right >>= 1) {
				if ((left & 1) != 0) { result = result.add(sums[left++]); }
				if ((right & 1) != 0) { result = result.add(sums[--right]); }
			}
			return positive(result);
		}
	}

	private record Cell(double lower, double upper, SplineOutwardInterval2D speed,
			SplineOutwardInterval2D integral, int depth) { }

	/**
	 * Builds the fixed-budget speed proof of one positively certified component. An
	 * unknown or exhausted enclosure remains typed and cannot authorize any integral.
	 */
	public static Index buildIndex(ConstructionBoxModel2D model, LocusInterval2D domain,
			ContinuousComponent component) {
		return buildIndex(model, domain, component, COVER_BUDGET);
	}

	/** Test seam can reduce the fixed effort budget, never increase it or set a tolerance. */
	static Index buildIndex(ConstructionBoxModel2D model, LocusInterval2D domain,
			ContinuousComponent component, int budget) {
		if (budget < 1 || budget > COVER_BUDGET) {
			throw new IllegalArgumentException("Source-proof budget must be within the fixed limit");
		}
		boolean periodic = model.getProgram().getDriver() == Driver.CIRCLE;
		if (component == null || !component.getNature().certifies()
				|| periodic && (domain.getLower() != -Math.PI || domain.getUpper() != Math.PI)) {
			return new Index(domain, component, List.of(), null,
					LocusCoverageCause2D.NOT_CERTIFIED, 0);
		}
		List<Cell> cells = new ArrayList<>();
		Deque<double[]> pending = new ArrayDeque<>();
		int count = 1 << COVER_DEPTH;
		double period = domain.getUpper() - domain.getLower();
		if (!(period > 0) || !Double.isFinite(period)) {
			return new Index(domain, component, List.of(), null,
					LocusCoverageCause2D.NOT_CERTIFIED, 0);
		}
		for (LocusInterval2D piece : component.getCanonicalPieces()) {
			if (piece.getLower() < domain.getLower() || piece.getUpper() > domain.getUpper()) {
				return new Index(domain, component, List.of(), null,
						LocusCoverageCause2D.NOT_CERTIFIED, 0);
			}
			for (int index = 0; index < count; index++) {
				double lower = Math.max(piece.getLower(), index == 0 ? domain.getLower()
						: domain.getLower() + period * index / count);
				double upper = Math.min(piece.getUpper(), index == count - 1
						? domain.getUpper() : domain.getLower() + period * (index + 1) / count);
				if (lower < upper) { pending.addLast(new double[] {lower, upper, COVER_DEPTH}); }
			}
		}
		int spent = 0;
		if (pending.isEmpty()) {
			return new Index(domain, component, List.of(), null,
					LocusCoverageCause2D.NOT_CERTIFIED, 0);
		}
		try {
			while (!pending.isEmpty()) {
				double[] box = pending.removeFirst();
				if (spent == budget) {
					return new Index(domain, component, List.of(), null,
							LocusCoverageCause2D.BUDGET_EXHAUSTED, spent);
				}
				spent++;
				SplineOutwardInterval2D enclosed = speed(model, box[0], box[1]);
				if (enclosed != null) {
					cells.add(new Cell(box[0], box[1], enclosed, area(enclosed, box[0], box[1]),
							(int) box[2]));
					continue;
				}
				double middle = box[0] + (box[1] - box[0]) / 2;
				if (box[2] >= MAXIMUM_DEPTH || !(box[0] < middle && middle < box[1])) {
					return new Index(domain, component, List.of(), null,
							LocusCoverageCause2D.BUDGET_EXHAUSTED, spent);
				}
				pending.addFirst(new double[] {middle, box[1], box[2] + 1});
				pending.addFirst(new double[] {box[0], middle, box[2] + 1});
			}
			SplineOutwardInterval2D gap = null;
			if (periodic && (component.isFullLength() || component.crossesSeam())) {
				if (spent == budget) {
					return new Index(domain, component, List.of(), null,
							LocusCoverageCause2D.BUDGET_EXHAUSTED, spent);
				}
				spent++;
				gap = speed(model, Math.PI, Math.nextUp(Math.PI));
				if (gap == null) {
					return new Index(domain, component, List.of(), null,
							LocusCoverageCause2D.NOT_CERTIFIED, spent);
				}
			}
			cells.sort(Comparator.comparingDouble(Cell::lower));
			return refine(model, domain, component, cells, gap, spent, budget);
		} catch (ArithmeticException invalid) {
			return new Index(domain, component, List.of(), null,
					LocusCoverageCause2D.NOT_CERTIFIED, spent);
		}
	}

	/** Eager, revision-local refinement; a failed optional split never discards its parent. */
	private static Index refine(ConstructionBoxModel2D model, LocusInterval2D domain,
			ContinuousComponent component, List<Cell> cells, SplineOutwardInterval2D gap,
			int spent, int budget) {
		Index current = new Index(domain, component, cells, gap, null, spent);
		PriorityQueue<Cell> candidates = new PriorityQueue<>(Comparator
				.<Cell>comparingDouble(RouteSourceFacts2D::uncertainty).reversed()
				.thenComparingDouble(Cell::lower).thenComparingDouble(Cell::upper));
		candidates.addAll(cells);
		boolean limited = false;
		PrecisionStatus status;
		while (true) {
			if (effortTargetReached(current.total)) {
				status = PrecisionStatus.TARGET_REACHED;
				break;
			}
			if (candidates.isEmpty()) {
				status = limited ? PrecisionStatus.REFINEMENT_LIMIT : PrecisionStatus.NO_IMPROVEMENT;
				break;
			}
			Cell parent = candidates.remove();
			double middle = parent.lower + (parent.upper - parent.lower) / 2;
			if (parent.depth >= MAXIMUM_DEPTH || !(parent.lower < middle && middle < parent.upper)) {
				limited = true;
				continue;
			}
			// Both child evaluations must fit before an attempt starts.
			if (budget - spent < 2) {
				status = PrecisionStatus.EFFORT_BUDGET;
				break;
			}
			try {
				spent++;
				SplineOutwardInterval2D leftSpeed = speed(model, parent.lower, middle);
				spent++;
				SplineOutwardInterval2D rightSpeed = speed(model, middle, parent.upper);
				if (leftSpeed == null || rightSpeed == null) { continue; }
				leftSpeed = leftSpeed.intersection(parent.speed);
				rightSpeed = rightSpeed.intersection(parent.speed);
				if (leftSpeed == null || rightSpeed == null) { continue; }
				Cell left = new Cell(parent.lower, middle, leftSpeed,
						area(leftSpeed, parent.lower, middle), parent.depth + 1);
				Cell right = new Cell(middle, parent.upper, rightSpeed,
						area(rightSpeed, middle, parent.upper), parent.depth + 1);
				SplineOutwardInterval2D combined = positive(left.integral.add(right.integral));
				if (combined.lower < parent.integral.lower || combined.upper > parent.integral.upper
						|| combined.lower == parent.integral.lower && combined.upper == parent.integral.upper) {
					continue;
				}
				List<Cell> proposed = new ArrayList<>(cells);
				proposed.remove(parent);
				proposed.add(left);
				proposed.add(right);
				proposed.sort(Comparator.comparingDouble(Cell::lower));
				Index next = new Index(domain, component, proposed, gap, null, spent);
				if (next.total.lower < current.total.lower || next.total.upper > current.total.upper) {
					continue;
				}
				cells = proposed;
				current = next;
				candidates.add(left);
				candidates.add(right);
			} catch (ArithmeticException optionalFailure) {
				// Current still contains the independently valid finite parent proof.
			}
		}
		return new Index(domain, component, cells, gap, null, spent, status);
	}

	private static boolean effortTargetReached(SplineOutwardInterval2D total) {
		try {
			SplineOutwardInterval2D width = SplineOutwardInterval2D.point(total.upper)
					.subtract(SplineOutwardInterval2D.point(total.lower));
			SplineOutwardInterval2D target = SplineOutwardInterval2D.point(total.upper)
					.multiply(SplineOutwardInterval2D.point(RELATIVE_WIDTH_EFFORT_TARGET));
			return width.upper <= target.lower;
		} catch (ArithmeticException targetNotEnclosed) {
			return false;
		}
	}

	private static double uncertainty(Cell cell) {
		return Math.nextUp(cell.integral.upper - cell.integral.lower);
	}

	/** Chord admission is restricted to the same certified component as the origin. */
	public static double chordLowerBound(ConstructionBoxModel2D model, LocusInterval2D domain,
			ContinuousComponent component, double origin) {
		if (component == null || !component.getNature().certifies() || !component.contains(origin)) {
			return Double.NaN;
		}
		boolean periodic = model.getProgram().getDriver() == Driver.CIRCLE;
		SplineOutwardInterval2D[] base = pointEnclosure(model, origin, periodic);
		if (base == null) { return Double.NaN; }
		double best = Double.NaN;
		for (int quarter = 1; quarter <= 3; quarter++) {
			double parameter = component.getStart() + component.getLength() * quarter / 4;
			if (component.isArc() && parameter >= domain.getUpper()) {
				parameter -= domain.getUpper() - domain.getLower();
			}
			if (!component.contains(parameter)) { continue; }
			SplineOutwardInterval2D[] point = pointEnclosure(model, parameter, periodic);
			double gap = point == null ? 0 : separation(base, point);
			if (gap > 0 && !(gap <= best)) { best = gap; }
		}
		return best;
	}

	private static SplineOutwardInterval2D positive(SplineOutwardInterval2D value) {
		return new SplineOutwardInterval2D(Math.max(0, value.lower), Math.max(0, value.upper));
	}

	private static SplineOutwardInterval2D area(SplineOutwardInterval2D speed,
			double lower, double upper) {
		return positive(SplineOutwardInterval2D.point(upper)
				.subtract(SplineOutwardInterval2D.point(lower)).multiply(speed));
	}

	private static double[] bounds(SplineOutwardInterval2D value) {
		return value == null ? null : new double[] {value.lower, value.upper};
	}

	/**
	 * Lower bound of a chord of the source from its origin: the traced point is enclosed at
	 * the origin and at the three quarter-period parameters after it, and the largest
	 * outward-rounded separation between the origin enclosure and another one is returned.
	 * Every parameter is a valid point of a closed periodic source.
	 *
	 * @param model box model of the class-v3 program of the source
	 * @param domain canonical driver domain of the source
	 * @param origin canonical address of the origin
	 * @return positive lower bound of a chord, or {@code NaN} when none is certified
	 */
	public static double chordLowerBound(ConstructionBoxModel2D model, LocusInterval2D domain,
			double origin) {
		boolean periodic = model.getProgram().getDriver() == Driver.CIRCLE;
		SplineOutwardInterval2D[] base = pointEnclosure(model, origin, periodic);
		if (base == null) {
			return Double.NaN;
		}
		double period = domain.getUpper() - domain.getLower();
		double best = Double.NaN;
		for (int quarter = 1; quarter <= 3; quarter++) {
			double other = origin + period * quarter / 4;
			if (other >= domain.getUpper()) {
				other -= period;
			}
			if (!domain.contains(other, 0)) {
				continue;
			}
			SplineOutwardInterval2D[] point = pointEnclosure(model, other, periodic);
			double gap = point == null ? 0 : separation(base, point);
			if (gap > 0 && !(gap <= best)) {
				best = gap;
			}
		}
		return best;
	}

	/**
	 * Upper bound of the length of a closed periodic source over one full period of its
	 * circle driver: a cover of the circle by boxes, refined where the enclosure of the
	 * speed is not finite, with an outward enclosure of {@code sqrt(x'^2+y'^2)}.
	 *
	 * @param model box model of the class-v3 program of the source
	 * @param domain canonical driver domain of the source, the half-open circle
	 * @return finite upper bound of the length, or {@code NaN} when the cover fails
	 */
	public static double lengthUpperBound(ConstructionBoxModel2D model,
			LocusInterval2D domain) {
		if (model.getProgram().getDriver() != Driver.CIRCLE || domain.getLower() != -Math.PI
				|| domain.getUpper() != Math.PI) {
			return Double.NaN;
		}
		Deque<double[]> pending = new ArrayDeque<>();
		int count = 1 << COVER_DEPTH;
		double period = domain.getUpper() - domain.getLower();
		for (int index = count - 1; index >= 0; index--) {
			double upper = index == count - 1 ? domain.getUpper()
					: domain.getLower() + period * (index + 1) / count;
			pending.push(new double[] {domain.getLower() + period * index / count, upper,
				COVER_DEPTH});
		}
		// The seam box closes the real circle over the binary64 gap at pi.
		pending.addLast(new double[] {Math.PI, Math.nextUp(Math.PI), COVER_DEPTH});
		SplineOutwardInterval2D total = SplineOutwardInterval2D.point(0);
		int spent = 0;
		while (!pending.isEmpty()) {
			double[] box = pending.pop();
			if (++spent > COVER_BUDGET) {
				return Double.NaN;
			}
			SplineOutwardInterval2D speed = speed(model, box[0], box[1]);
			if (speed != null) {
				SplineOutwardInterval2D width = SplineOutwardInterval2D.point(box[1])
						.subtract(SplineOutwardInterval2D.point(box[0]));
				total = total.add(width.multiply(speed));
				continue;
			}
			double middle = box[0] + (box[1] - box[0]) / 2;
			if (box[2] >= MAXIMUM_DEPTH || !(box[0] < middle && middle < box[1])) {
				return Double.NaN;
			}
			pending.push(new double[] {middle, box[1], box[2] + 1});
			pending.push(new double[] {box[0], middle, box[2] + 1});
		}
		return total.upper;
	}

	/** @return Euclidean speed enclosure on a valid box, or {@code null} */
	private static SplineOutwardInterval2D speed(ConstructionBoxModel2D model, double lower,
			double upper) {
		ConstructionBoxOutcome2D outcome;
		try {
			outcome = model.evaluateCertifying(new SplineOutwardInterval2D(lower, upper),
					model.getProgram().getDriver() == Driver.CIRCLE);
		} catch (ArithmeticException exception) {
			return null;
		}
		if (outcome.kind != ConstructionBoxOutcome2D.Kind.VALID || outcome.derivative == null) {
			return null;
		}
		try {
			SplineOutwardInterval2D x = outcome.derivative[0];
			SplineOutwardInterval2D y = outcome.derivative[1];
			if (x.lower == 0 && x.upper == 0 && y.lower == 0 && y.upper == 0) {
				return SplineOutwardInterval2D.point(0);
			}
			double minX = x.containsZero() ? 0 : Math.min(Math.abs(x.lower), Math.abs(x.upper));
			double minY = y.containsZero() ? 0 : Math.min(Math.abs(y.lower), Math.abs(y.upper));
			SplineOutwardInterval2D low = SplineOutwardInterval2D.point(minX)
					.multiply(SplineOutwardInterval2D.point(minX))
					.add(SplineOutwardInterval2D.point(minY)
							.multiply(SplineOutwardInterval2D.point(minY)));
			SplineOutwardInterval2D high = SplineOutwardInterval2D.point(magnitude(x))
					.multiply(SplineOutwardInterval2D.point(magnitude(x)))
					.add(SplineOutwardInterval2D.point(magnitude(y))
							.multiply(SplineOutwardInterval2D.point(magnitude(y))));
			return new SplineOutwardInterval2D(
					Math.max(0, Math.nextDown(Math.sqrt(Math.max(0, low.lower)))),
					Math.nextUp(Math.sqrt(Math.max(0, high.upper))));
		} catch (ArithmeticException exception) {
			return null;
		}
	}

	private static double magnitude(SplineOutwardInterval2D value) {
		return Math.max(Math.abs(value.lower), Math.abs(value.upper));
	}

	/** @return enclosure of the traced point at one parameter, or {@code null} */
	private static SplineOutwardInterval2D[] pointEnclosure(ConstructionBoxModel2D model,
			double parameter, boolean periodic) {
		try {
			ConstructionBoxOutcome2D outcome = model.evaluateCertifying(
					SplineOutwardInterval2D.point(parameter), periodic);
			return outcome.kind == ConstructionBoxOutcome2D.Kind.VALID ? outcome.value : null;
		} catch (ArithmeticException exception) {
			return null;
		}
	}

	/**
	 * @return lower bound of the distance between two boxes: the larger gap of their
	 *         coordinate enclosures, rounded down; zero when they overlap in both
	 */
	private static double separation(SplineOutwardInterval2D[] first,
			SplineOutwardInterval2D[] second) {
		double gap = 0;
		for (int axis = 0; axis < 2; axis++) {
			double apart = Math.max(second[axis].lower - first[axis].upper,
					first[axis].lower - second[axis].upper);
			if (apart > 0) {
				gap = Math.max(gap, Math.nextDown(apart));
			}
		}
		return gap;
	}
}
