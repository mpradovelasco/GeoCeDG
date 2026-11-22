/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.geocedg.common.kernel.locus.LocusInterval2D;

/**
 * Deterministic traversal that produces a {@link LocusConditionedEvidence2D} (candidate
 * contract {@code post-r1-conditioned-use.md}, sections 3, 5 and 7).
 *
 * <p>Every node is a dyadic fraction of the declared domain, and a cell is split exactly
 * when the criteria of the policy require it, or when it is larger than the minimum step
 * and contains a parameter of the contradiction set. That rule is a predicate of the cell,
 * so the partition is the same whatever the order in which contradictions were found and
 * whatever was queried or drawn. Cells are visited in a fixed breadth-first order and each
 * distinct node is charged once to the work limit, cached or not, so the content does not
 * depend on what an earlier traversal of the same revision already evaluated.</p>
 */
public final class LocusConditionedTraversal2D {
	/** Nodes evaluated for one revision; a cache, never part of the content. */
	static final class Nodes {
		private final Map<Long, LocusConditionedSample2D> grid = new HashMap<>();
		private final Map<Double, LocusConditionedSample2D> extra = new HashMap<>();

		LocusConditionedSample2D gridNode(long index) {
			return grid.get(index);
		}
	}

	private enum Finding {
		SPAN, ABSENT, BOTH_ENDS_FAILED, FAILURE, VALIDITY, CAUSE, SELECTION, GEOMETRY
	}

	private static final class WorkExhausted extends RuntimeException {
		private static final long serialVersionUID = 1L;

		WorkExhausted() {
			super(null, null, false, false);
		}
	}

	private final String branchKey;
	private final long revision;
	private final double domainLower;
	private final double domainUpper;
	private final LocusConditionedTraversalPolicy2D policy;
	private final double[] contradictions;
	private final LocusConditionedSampler2D sampler;
	private final Nodes nodes;
	private final long gridSize;
	private final double[][] certified;
	private final Set<Object> charged = new HashSet<>();

	private LocusConditionedTraversal2D(String branchKey, long revision, double domainLower,
			double domainUpper, LocusConditionedTraversalPolicy2D policy,
			Collection<Double> contradictions, LocusConditionedSampler2D sampler, Nodes nodes,
			List<LocusInterval2D> certified) {
		if (branchKey == null || branchKey.trim().isEmpty() || revision < 0) {
			throw new IllegalArgumentException("Branch key and revision are required");
		}
		if (!Double.isFinite(domainLower) || !Double.isFinite(domainUpper)
				|| !(domainLower < domainUpper)) {
			throw new IllegalArgumentException("A finite declared domain is required");
		}
		this.branchKey = branchKey;
		this.revision = revision;
		this.domainLower = domainLower;
		this.domainUpper = domainUpper;
		this.policy = Objects.requireNonNull(policy);
		this.sampler = Objects.requireNonNull(sampler);
		this.nodes = nodes;
		this.gridSize = 1L << (policy.getMinimumStepDepth() + 1);
		this.certified = new double[certified.size()][];
		for (int index = 0; index < this.certified.length; index++) {
			LocusInterval2D interval = Objects.requireNonNull(certified.get(index));
			this.certified[index] = new double[] { interval.getLower(), interval.getUpper() };
		}
		TreeSet<Double> canonical = new TreeSet<>();
		for (Double each : contradictions) {
			canonical.add(canonicalParameter(each, domainLower, domainUpper));
		}
		this.contradictions = new double[canonical.size()];
		int i = 0;
		for (Double each : canonical) {
			this.contradictions[i++] = each;
		}
	}

	/**
	 * Traverses one branch.
	 *
	 * @param branchKey branch that the evidence describes
	 * @param revision revision of the definition
	 * @param domainLower lower end of the declared driver domain
	 * @param domainUpper upper end of the declared driver domain
	 * @param policy versioned policy
	 * @param contradictions canonical parameters with a confirmed contradiction; a set
	 * @param sampler semantic evaluator of the producer
	 * @return the content that the function, the policy and the set determine
	 */
	public static LocusConditionedEvidence2D traverse(String branchKey, long revision,
			double domainLower, double domainUpper, LocusConditionedTraversalPolicy2D policy,
			Collection<Double> contradictions, LocusConditionedSampler2D sampler) {
		return traverse(branchKey, revision, domainLower, domainUpper, policy,
				contradictions, sampler, new Nodes(), List.of());
	}

	/**
	 * Traverses the part of one branch that no certificate covers.
	 *
	 * @param branchKey branch that the evidence describes
	 * @param revision revision of the definition
	 * @param domainLower lower end of the declared driver domain
	 * @param domainUpper upper end of the declared driver domain
	 * @param policy versioned policy
	 * @param contradictions canonical parameters with a confirmed contradiction; a set
	 * @param sampler semantic evaluator of the producer
	 * @param certified parameter intervals that a certificate of the revision covers, by a
	 *        certified component or a certified nonexistence; they are a fact of the
	 *        revision, and no cell is published inside them
	 * @return the content that the function, the policy, the set and the certificates of
	 *         the revision determine
	 */
	public static LocusConditionedEvidence2D traverse(String branchKey, long revision,
			double domainLower, double domainUpper, LocusConditionedTraversalPolicy2D policy,
			Collection<Double> contradictions, LocusConditionedSampler2D sampler,
			List<LocusInterval2D> certified) {
		return traverse(branchKey, revision, domainLower, domainUpper, policy,
				contradictions, sampler, new Nodes(), certified);
	}

	static LocusConditionedEvidence2D traverse(String branchKey, long revision,
			double domainLower, double domainUpper, LocusConditionedTraversalPolicy2D policy,
			Collection<Double> contradictions, LocusConditionedSampler2D sampler, Nodes nodes,
			List<LocusInterval2D> certified) {
		return new LocusConditionedTraversal2D(branchKey, revision, domainLower, domainUpper,
				policy, contradictions, sampler, nodes, certified).run();
	}

	/**
	 * @param parameter parameter to normalize
	 * @param domainLower lower end of the declared driver domain
	 * @param domainUpper upper end of the declared driver domain
	 * @return canonical form of a parameter of the declared domain
	 */
	static double canonicalParameter(Double parameter, double domainLower,
			double domainUpper) {
		if (parameter == null || !Double.isFinite(parameter) || parameter < domainLower
				|| parameter > domainUpper) {
			throw new IllegalArgumentException("Parameter outside the declared domain");
		}
		return parameter == 0 ? 0.0 : parameter;
	}

	private LocusConditionedEvidence2D run() {
		ArrayDeque<long[]> queue = new ArrayDeque<>();
		List<LocusConditionedCell2D> leaves = new ArrayList<>();
		long width = gridSize >> policy.getInitialDepth();
		for (long lower = 0; lower < gridSize; lower += width) {
			queue.add(new long[] { lower, lower + width });
		}
		boolean exhausted = false;
		while (!queue.isEmpty()) {
			long[] cell = queue.poll();
			if (coverage(cell[0], cell[1]) == COVERED) {
				// Certified by the revision: the certificate governs it, not this evidence.
				continue;
			}
			if (!exhausted) {
				try {
					classify(cell[0], cell[1], queue, leaves);
					continue;
				} catch (WorkExhausted limit) {
					exhausted = true;
				}
			}
			leaves.add(leaf(cell[0], cell[1], LocusConditionedCellKind2D.UNEXPLORED, null,
					Double.NaN, "work limit"));
		}
		leaves.sort(Comparator.comparingLong(LocusConditionedCell2D::getLowerIndex));
		List<Double> set = new ArrayList<>();
		for (double each : contradictions) {
			set.add(each);
		}
		LocusConditionedSample2D first = nodes.grid.get(0L);
		LocusConditionedSample2D last = nodes.grid.get(gridSize);
		boolean seam = first != null && last != null && charged.contains(0L)
				&& charged.contains(gridSize) && first.isValid() && last.isValid()
				&& first.hasEstablishedSelections() && first.hasSameSelections(last)
				&& Math.hypot(last.getX() - first.getX(), last.getY() - first.getY())
						<= policy.getMaximumChordDeviation();
		return new LocusConditionedEvidence2D(branchKey, revision, policy.getVersion(),
				domainLower, domainUpper, set, leaves, charged.size(), seam);
	}

	private void classify(long lower, long upper, ArrayDeque<long[]> queue,
			List<LocusConditionedCell2D> leaves) {
		long middle = lower + (upper - lower) / 2;
		List<Double> inside = contradictionsIn(parameter(lower), parameter(upper));
		boolean canSplit = upper - lower >= 4;
		if (coverage(lower, upper) == PARTLY_COVERED) {
			// The end of a certificate is located to the minimum step before any evaluation.
			// The cell of minimum step that contains it is not published: that end is a
			// boundary of the certificate, which governs it, and no cell overlaps a
			// certified coverage.
			if (canSplit) {
				queue.add(new long[] { lower, middle });
				queue.add(new long[] { middle, upper });
			}
			return;
		}
		LocusConditionedSample2D first = gridNode(lower);
		LocusConditionedSample2D last = gridNode(upper);
		LocusConditionedSample2D mid = null;
		Finding finding;
		if (isFailed(first) && isFailed(last)) {
			finding = Finding.BOTH_ENDS_FAILED;
		} else if (isFailed(first) || isFailed(last)) {
			finding = Finding.FAILURE;
		} else {
			mid = gridNode(middle);
			finding = findingOf(first, mid, last);
		}
		boolean settled = finding == Finding.SPAN || finding == Finding.ABSENT
				|| finding == Finding.BOTH_ENDS_FAILED;
		if ((!settled || !inside.isEmpty()) && canSplit) {
			queue.add(new long[] { lower, middle });
			queue.add(new long[] { middle, upper });
			return;
		}
		if (settled && finding != Finding.BOTH_ENDS_FAILED) {
			for (Double parameter : inside) {
				Finding refined = findingWithNode(finding, first, mid, last,
						extraNode(parameter));
				if (refined != finding) {
					finding = refined;
					break;
				}
			}
		}
		leaves.add(leafOf(lower, upper, finding, first, mid, last));
	}

	private static final int UNCOVERED = 0;
	private static final int PARTLY_COVERED = 1;
	private static final int COVERED = 2;

	private int coverage(long lower, long upper) {
		double low = parameter(lower);
		double high = parameter(upper);
		int found = UNCOVERED;
		for (double[] interval : certified) {
			if (interval[0] <= low && high <= interval[1]) {
				return COVERED;
			}
			if (Math.max(low, interval[0]) < Math.min(high, interval[1])) {
				found = PARTLY_COVERED;
			}
		}
		return found;
	}

	private Finding findingOf(LocusConditionedSample2D first, LocusConditionedSample2D mid,
			LocusConditionedSample2D last) {
		if (isFailed(mid)) {
			return Finding.FAILURE;
		}
		if (first.isValid() != mid.isValid() || first.isValid() != last.isValid()) {
			return Finding.VALIDITY;
		}
		if (!first.isValid()) {
			return first.getCause().equals(mid.getCause())
					&& first.getCause().equals(last.getCause()) ? Finding.ABSENT : Finding.CAUSE;
		}
		if (!first.hasEstablishedSelections() || !mid.hasEstablishedSelections()
				|| !last.hasEstablishedSelections() || !first.hasSameSelections(mid)
				|| !first.hasSameSelections(last)) {
			return Finding.SELECTION;
		}
		if (chordDeviation(first, mid, last) > policy.getMaximumChordDeviation()) {
			return Finding.GEOMETRY;
		}
		double chord = Math.hypot(last.getX() - first.getX(), last.getY() - first.getY());
		if (chord > policy.getMaximumChordDeviation()
				&& turning(first, mid, last) > policy.getMaximumTurning()) {
			return Finding.GEOMETRY;
		}
		return Finding.SPAN;
	}

	/** Adds one confirmed parameter as a node of a cell at the minimum step. */
	private Finding findingWithNode(Finding finding, LocusConditionedSample2D first,
			LocusConditionedSample2D mid, LocusConditionedSample2D last,
			LocusConditionedSample2D added) {
		if (isFailed(added)) {
			return Finding.FAILURE;
		}
		if (finding == Finding.ABSENT) {
			if (added.isValid()) {
				return Finding.VALIDITY;
			}
			return added.getCause().equals(first.getCause()) ? finding : Finding.CAUSE;
		}
		if (!added.isValid()) {
			return Finding.VALIDITY;
		}
		if (!added.hasEstablishedSelections() || !first.hasSameSelections(added)) {
			return Finding.SELECTION;
		}
		return distanceToPolyline(added, first, mid, last) > policy
				.getMaximumChordDeviation() ? Finding.GEOMETRY : finding;
	}

	private LocusConditionedCell2D leafOf(long lower, long upper, Finding finding,
			LocusConditionedSample2D first, LocusConditionedSample2D mid,
			LocusConditionedSample2D last) {
		switch (finding) {
		case SPAN:
			return new LocusConditionedCell2D(lower, upper, parameter(lower), parameter(upper),
					LocusConditionedCellKind2D.SPAN, spanClass(lower, upper, first, mid, last),
					chordDeviation(first, mid, last), "", new double[] {first.getX(),
							first.getY(), mid.getX(), mid.getY(), last.getX(), last.getY()});
		case ABSENT:
			return leaf(lower, upper, LocusConditionedCellKind2D.ABSENT_BAND, null,
					Double.NaN, first.getCause());
		case VALIDITY:
			return leaf(lower, upper, LocusConditionedCellKind2D.TRANSITION_BAND, null,
					Double.NaN, causeOf(first, mid, last));
		case SELECTION:
			return leaf(lower, upper, LocusConditionedCellKind2D.SELECTION_BAND, null,
					Double.NaN, "");
		case GEOMETRY:
			return leaf(lower, upper, LocusConditionedCellKind2D.DISCONTINUITY_BAND, null,
					Double.NaN, "");
		default:
			// A failed node, or absences with different causes: neither is evidence of
			// absence, so the cell stays unresolved.
			return leaf(lower, upper, LocusConditionedCellKind2D.FAILURE_BAND, null,
					Double.NaN, causeOf(first, mid, last));
		}
	}

	private LocusConditionedSpanClass2D spanClass(long lower, long upper,
			LocusConditionedSample2D first, LocusConditionedSample2D mid,
			LocusConditionedSample2D last) {
		boolean any = false;
		for (LocusConditionedSample2D sample : List.of(first, mid, last)) {
			for (LocusSelectionTestimony2D each : sample.getTestimony()) {
				any = true;
				if (!each.hasSeparation()
						|| !(each.getSeparation() > policy.getMarginThreshold())) {
					return LocusConditionedSpanClass2D.RESOLUTION_ONLY;
				}
			}
		}
		if (!any) {
			return LocusConditionedSpanClass2D.RESOLUTION_ONLY;
		}
		return sampler.boundsSeparation(parameter(lower), parameter(upper))
				? LocusConditionedSpanClass2D.MARGIN_BOUNDED
				: LocusConditionedSpanClass2D.MARGIN_ESTIMATED;
	}

	private LocusConditionedCell2D leaf(long lower, long upper,
			LocusConditionedCellKind2D kind, LocusConditionedSpanClass2D spanClass,
			double deviation, String cause) {
		return new LocusConditionedCell2D(lower, upper, parameter(lower), parameter(upper),
				kind, spanClass, deviation, cause);
	}

	private static String causeOf(LocusConditionedSample2D first,
			LocusConditionedSample2D mid, LocusConditionedSample2D last) {
		if (!first.isValid()) {
			return first.getCause();
		}
		if (mid != null && !mid.isValid()) {
			return mid.getCause();
		}
		return last.getCause();
	}

	private static boolean isFailed(LocusConditionedSample2D sample) {
		return sample.getOutcome() == LocusConditionedSample2D.Outcome.FAILED;
	}

	private double parameter(long index) {
		if (index == gridSize) {
			return domainUpper;
		}
		return domainLower + (domainUpper - domainLower) * ((double) index / gridSize);
	}

	private List<Double> contradictionsIn(double lower, double upper) {
		List<Double> inside = new ArrayList<>();
		for (double each : contradictions) {
			if (lower <= each && each <= upper) {
				inside.add(each);
			}
		}
		return inside;
	}

	private LocusConditionedSample2D gridNode(long index) {
		charge(index);
		LocusConditionedSample2D sample = nodes.grid.get(index);
		if (sample == null) {
			sample = Objects.requireNonNull(sampler.sample(parameter(index)));
			nodes.grid.put(index, sample);
		}
		return sample;
	}

	private LocusConditionedSample2D extraNode(Double parameter) {
		charge(parameter);
		LocusConditionedSample2D sample = nodes.extra.get(parameter);
		if (sample == null) {
			sample = Objects.requireNonNull(sampler.sample(parameter));
			nodes.extra.put(parameter, sample);
		}
		return sample;
	}

	private void charge(Object node) {
		if (!charged.contains(node)) {
			if (charged.size() >= policy.getWorkLimit()) {
				throw new WorkExhausted();
			}
			charged.add(node);
		}
	}

	/**
	 * Decides whether one evaluation is excluded by the kind of a published cell (candidate
	 * contract section 7, point 4): an invalid evaluation, another selection or a jump above
	 * the limits in a span; a valid evaluation in an absence or a failure band.
	 */
	static boolean contradicts(LocusConditionedCell2D cell, LocusConditionedSample2D sample,
			Nodes nodes, LocusConditionedTraversalPolicy2D policy) {
		switch (cell.getKind()) {
		case ABSENT_BAND:
		case FAILURE_BAND:
			return sample.isValid();
		case SPAN:
			if (!sample.isValid()) {
				return true;
			}
			LocusConditionedSample2D first = nodes.gridNode(cell.getLowerIndex());
			LocusConditionedSample2D mid = nodes.gridNode(
					cell.getLowerIndex() + (cell.getUpperIndex() - cell.getLowerIndex()) / 2);
			LocusConditionedSample2D last = nodes.gridNode(cell.getUpperIndex());
			if (!sample.hasEstablishedSelections() || !first.hasSameSelections(sample)) {
				return true;
			}
			return distanceToPolyline(sample, first, mid, last) > policy
					.getMaximumChordDeviation();
		default:
			return false;
		}
	}

	private static double chordDeviation(LocusConditionedSample2D first,
			LocusConditionedSample2D mid, LocusConditionedSample2D last) {
		return distanceToSegment(mid.getX(), mid.getY(), first.getX(), first.getY(),
				last.getX(), last.getY(), false);
	}

	private static double turning(LocusConditionedSample2D first,
			LocusConditionedSample2D mid, LocusConditionedSample2D last) {
		double ux = mid.getX() - first.getX();
		double uy = mid.getY() - first.getY();
		double vx = last.getX() - mid.getX();
		double vy = last.getY() - mid.getY();
		if ((ux == 0 && uy == 0) || (vx == 0 && vy == 0)) {
			return 0;
		}
		return Math.abs(Math.atan2(ux * vy - uy * vx, ux * vx + uy * vy));
	}

	private static double distanceToPolyline(LocusConditionedSample2D point,
			LocusConditionedSample2D first, LocusConditionedSample2D mid,
			LocusConditionedSample2D last) {
		return Math.min(
				distanceToSegment(point.getX(), point.getY(), first.getX(), first.getY(),
						mid.getX(), mid.getY(), true),
				distanceToSegment(point.getX(), point.getY(), mid.getX(), mid.getY(),
						last.getX(), last.getY(), true));
	}

	private static double distanceToSegment(double px, double py, double ax, double ay,
			double bx, double by, boolean clamp) {
		double dx = bx - ax;
		double dy = by - ay;
		double length = Math.hypot(dx, dy);
		if (length == 0) {
			return Math.hypot(px - ax, py - ay);
		}
		if (!clamp) {
			return Math.abs(dx * (ay - py) - (ax - px) * dy) / length;
		}
		double t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / (length * length)));
		return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
	}
}
