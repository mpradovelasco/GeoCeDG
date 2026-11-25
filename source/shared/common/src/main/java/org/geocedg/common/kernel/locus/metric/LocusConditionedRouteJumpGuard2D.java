/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.geocedg.common.kernel.locus.LocusPoint2D;

/**
 * Chord-scaling check of a route of one conditioned region, policy
 * {@code conditioned-route-jump-guard/v1} ({@code locus-v2-metrics.md} section 28.3.1,
 * condition 8 of the admission {@code conditioned-length-scalar/v2}).
 *
 * <p>For every leaf that the estimator accepted on the route, in increasing parameter order,
 * the check starts from the quarter of the leaf with the greatest chord among the chords that
 * the estimator evaluated, and bisects it along the half with the greater chord. A run of
 * {@code m} consecutive ratios {@code c(I_{k+1}) / c(I_k)} above {@code rho_J} is the numeric
 * signature that a jump produces, and that a continuous curve whose speed is singular or
 * varies faster than the levels resolve can produce too. The check is a conservative numeric
 * detection with a bounded budget: an observed signature demonstrates neither a jump nor that
 * the continuity hypothesis H3 is false, and its absence is no proof of continuity.</p>
 *
 * <p>The check reads only the accepted leaves of the estimate and evaluations of the traced
 * point. Its evaluations are counted apart from the work budget of the estimator, are no
 * leaves and change neither the value nor the error of the estimate. Every quantity is a pure
 * number or a world-coordinate length; zoom, DPI, viewport and render policy never enter
 * it.</p>
 */
public final class LocusConditionedRouteJumpGuard2D {
	// The values of the policy are those that PRE-G9B-MAINT-B5a fixed inside the admissible
	// intervals of section 28.3.1, with their numeric justification in
	// docs/validation/pre_g9b_maint_b5a_candidate_report.md, section 3.

	/** Version of the policy, part of the evidence of an admitted route. */
	public static final String VERSION = "conditioned-route-jump-guard/v1";
	/** {@code rho_J}, ratio threshold; admissible interval [0.80, 0.90]. */
	public static final double RATIO_THRESHOLD = 0.90;
	/** {@code m}, consecutive high ratios that make the signature; admissible {3, 4}. */
	public static final int CONSECUTIVE_HIGH_RATIOS = 3;
	/** {@code L}, levels per leaf; admissible {@code m <= L <= m + 3}. */
	public static final int LEVELS_PER_LEAF = 6;
	/**
	 * {@code k} of the factor {@code 2^k} of the chord resolution
	 * {@code eps_res = max(tau_leaf, 2^k * u * M_leaf)}; admissible 4 to 12.
	 */
	public static final int RESOLUTION_FACTOR_EXPONENT = 8;
	/** {@code E_cap}, evaluations of the traced point per route; admissible [2048, 16384]. */
	public static final int EVALUATION_CAP = 16384;
	/** The statement that an admitted route records with its hypotheses. */
	public static final String STATEMENT =
			"no chord-scaling signature observed; this is not a proof of continuity";
	/** Exponent of the unit roundoff {@code u = 2^-53} of binary64. */
	private static final int UNIT_ROUNDOFF_EXPONENT = -53;

	private LocusConditionedRouteJumpGuard2D() {
	}

	/**
	 * Chord resolution of a leaf: {@code max(tau_leaf, 2^k * u * M_leaf)}.
	 *
	 * @param share share of the tolerance of the leaf
	 * @param magnitude greatest absolute coordinate of the evaluated nodes of the leaf
	 * @return the chord resolution of the leaf
	 */
	public static double resolution(double share, double magnitude) {
		return Math.max(share, Math.scalb(magnitude,
				RESOLUTION_FACTOR_EXPONENT + UNIT_ROUNDOFF_EXPONENT));
	}

	/** @return a fresh check of one route */
	static Run newRun() {
		return new Run();
	}

	/** Evaluation of the traced point at a driver parameter. */
	interface TracedPoint {
		/**
		 * @param parameter canonical driver parameter
		 * @return the traced point, or {@code null} when the evaluation fails
		 */
		LocusPoint2D at(double parameter);
	}

	/** How one leaf ended. */
	enum LeafOutcome {
		NO_SIGNATURE, BELOW_RESOLUTION, SIGNATURE, INCONCLUSIVE
	}

	/** The decision of one leaf, as the diagnostics record it. */
	static final class Decision {
		private final int leaf;
		private final double leafLower;
		private final double leafUpper;
		private final double decisionLower;
		private final double decisionUpper;
		private final List<Double> ratios;
		private final int evaluations;
		private final String reason;

		private Decision(int leaf, double leafLower, double leafUpper, double decisionLower,
				double decisionUpper, List<Double> ratios, int evaluations, String reason) {
			this.leaf = leaf;
			this.leafLower = leafLower;
			this.leafUpper = leafUpper;
			this.decisionLower = decisionLower;
			this.decisionUpper = decisionUpper;
			this.ratios = Collections.unmodifiableList(new ArrayList<>(ratios));
			this.evaluations = evaluations;
			this.reason = reason;
		}

		@Override
		public String toString() {
			StringBuilder text = new StringBuilder();
			text.append("leaf ").append(leaf).append(" [").append(leafLower).append(", ")
					.append(leafUpper).append("], decision interval [").append(decisionLower)
					.append(", ").append(decisionUpper).append("], ratios [");
			for (int index = 0; index < ratios.size(); index++) {
				text.append(index == 0 ? "" : ", ")
						.append(String.format(Locale.ROOT, "%.6f", ratios.get(index)));
			}
			text.append("], ").append(evaluations).append(" evaluation(s) on the leaf");
			if (reason != null) {
				text.append(", ").append(reason);
			}
			return text.toString();
		}
	}

	/**
	 * Record of an admitted route: the policy version, the leaves tested, those below
	 * resolution, the evaluations spent and the statement of what the check is not.
	 */
	public static final class Record {
		private final int leavesTested;
		private final int leavesBelowResolution;
		private final long evaluations;

		private Record(int leavesTested, int leavesBelowResolution, long evaluations) {
			this.leavesTested = leavesTested;
			this.leavesBelowResolution = leavesBelowResolution;
			this.evaluations = evaluations;
		}

		/** @return the version of the policy of the check */
		public String getPolicyVersion() {
			return VERSION;
		}

		/** @return the accepted leaves of the estimate that the check decided */
		public int getLeavesTested() {
			return leavesTested;
		}

		/** @return the tested leaves that ended below the chord resolution */
		public int getLeavesBelowResolution() {
			return leavesBelowResolution;
		}

		/** @return the evaluations of the traced point that the check spent */
		public long getEvaluations() {
			return evaluations;
		}

		/** @return the statement that the absence of a signature is no proof of continuity */
		public String getStatement() {
			return STATEMENT;
		}

		@Override
		public String toString() {
			return VERSION + ": " + leavesTested + " leaf(s) tested, " + leavesBelowResolution
					+ " below resolution, " + evaluations + " evaluation(s); " + STATEMENT;
		}
	}

	/** The check of one route, over the leaves of its segments in increasing order. */
	static final class Run {
		private int leaves;
		private int tested;
		private int belowResolution;
		private int inconclusive;
		private int untested;
		private long evaluations;
		private Decision signature;
		private Decision firstInconclusive;
		private Decision exhausted;
		private String failure;

		private Run() {
		}

		/** @return whether the check has stopped: a signature, the budget or a failure */
		boolean isStopped() {
			return signature != null || exhausted != null || failure != null;
		}

		/**
		 * Checks the accepted leaves of one estimate of the route.
		 *
		 * @param accepted accepted leaves in increasing parameter order, or {@code null} when
		 *        the estimate retained none
		 * @param lower lower parameter of the stretch of the estimate
		 * @param upper upper parameter of the stretch of the estimate
		 * @param point evaluation of the traced point
		 */
		void check(List<LocusMetricEstimator2D.AcceptedLeaf> accepted, double lower,
				double upper, TracedPoint point) {
			if (accepted == null) {
				// No accepted leaf can be read: nothing is decided on the stretch.
				leaves++;
				if (isStopped()) {
					untested++;
					return;
				}
				inconclusive++;
				Decision decision = new Decision(leaves, lower, upper, lower, upper,
						Collections.emptyList(), 0,
						"the estimate retained no accepted leaf for the check");
				if (firstInconclusive == null) {
					firstInconclusive = decision;
				}
				return;
			}
			for (LocusMetricEstimator2D.AcceptedLeaf leaf : accepted) {
				leaves++;
				if (isStopped()) {
					untested++;
					continue;
				}
				leaf(leaf, point);
			}
		}

		private void leaf(LocusMetricEstimator2D.AcceptedLeaf leaf, TracedPoint point) {
			double[] at = leaf.getNodeParameters();
			LocusPoint2D[] nodes = leaf.getNodePoints();
			List<Double> ratios = new ArrayList<>();
			if (at == null || nodes == null || at.length < 2) {
				inconclusive(new Decision(leaves, leaf.getLower(), leaf.getUpper(),
						leaf.getLower(), leaf.getUpper(), ratios, 0,
						"the estimator evaluated no chord of the leaf"));
				return;
			}
			// I_0: the quarter with the greatest chord among those that the estimator
			// evaluated; a tie goes to the quarter of lowest parameter.
			int quarter = 0;
			double chord = distance(nodes[0], nodes[1]);
			for (int index = 1; index + 1 < nodes.length; index++) {
				double candidate = distance(nodes[index], nodes[index + 1]);
				if (candidate > chord) {
					chord = candidate;
					quarter = index;
				}
			}
			double low = at[quarter];
			double high = at[quarter + 1];
			LocusPoint2D lowPoint = nodes[quarter];
			LocusPoint2D highPoint = nodes[quarter + 1];
			double resolution = resolution(leaf.getShare(), leaf.getMagnitude());
			int run = 0;
			int spent = 0;
			for (int level = 0; level < LEVELS_PER_LEAF; level++) {
				if (chord <= resolution) {
					tested++;
					belowResolution++;
					return;
				}
				double middle = low + (high - low) / 2;
				if (!(low < middle && middle < high)) {
					if (run > 0 || level == 0) {
						inconclusive(new Decision(leaves, leaf.getLower(), leaf.getUpper(), low,
								high, ratios, spent, "parameter resolution reached"
										+ (run > 0 ? " during a run of high ratios" : "")));
					} else {
						tested++;
					}
					return;
				}
				if (evaluations >= EVALUATION_CAP) {
					exhausted = new Decision(leaves, leaf.getLower(), leaf.getUpper(), low, high,
							ratios, spent, null);
					return;
				}
				evaluations++;
				spent++;
				LocusPoint2D middlePoint = point.at(middle);
				if (middlePoint == null) {
					failure = "the traced point could not be evaluated at " + middle
							+ " inside the stretch (leaf " + leaves + " [" + leaf.getLower()
							+ ", " + leaf.getUpper() + "])";
					return;
				}
				double first = distance(lowPoint, middlePoint);
				double second = distance(middlePoint, highPoint);
				double next;
				if (first >= second) {
					next = first;
					high = middle;
					highPoint = middlePoint;
				} else {
					next = second;
					low = middle;
					lowPoint = middlePoint;
				}
				double ratio = next / chord;
				ratios.add(ratio);
				chord = next;
				if (!Double.isFinite(ratio)) {
					inconclusive(new Decision(leaves, leaf.getLower(), leaf.getUpper(), low, high,
							ratios, spent, "a ratio was not finite"));
					return;
				}
				if (ratio > RATIO_THRESHOLD) {
					run++;
					if (run >= CONSECUTIVE_HIGH_RATIOS) {
						tested++;
						signature = new Decision(leaves, leaf.getLower(), leaf.getUpper(), low,
								high, ratios, spent, null);
						return;
					}
				} else {
					run = 0;
					if (LEVELS_PER_LEAF - 1 - level < CONSECUTIVE_HIGH_RATIOS) {
						tested++;
						return;
					}
				}
			}
			if (run > 0) {
				inconclusive(new Decision(leaves, leaf.getLower(), leaf.getUpper(), low, high,
						ratios, spent, "levels exhausted during a run of high ratios"));
			} else {
				tested++;
			}
		}

		private void inconclusive(Decision decision) {
			tested++;
			inconclusive++;
			if (firstInconclusive == null) {
				firstInconclusive = decision;
			}
		}

		/** @return whether the route satisfies condition 8 */
		boolean admits() {
			return !isStopped() && inconclusive == 0;
		}

		/** @return the record of an admitted route */
		Record record() {
			return new Record(tested, belowResolution, evaluations);
		}

		/** @return the evaluations spent */
		long getEvaluations() {
			return evaluations;
		}

		/**
		 * @return the diagnostics of a route that the check does not admit, the first of
		 *         which is {@code CONDITIONED_ROUTE_NOT_ADMISSIBLE}
		 */
		List<MetricDiagnostic2D> refusal() {
			List<MetricDiagnostic2D> diagnostics = new ArrayList<>();
			String spent = "; " + evaluations + " evaluation(s) spent by the check";
			if (failure != null) {
				diagnostics.add(notAdmissible("an evaluation of the check failed inside the "
						+ "stretch"));
				diagnostics.add(new MetricDiagnostic2D(MetricDiagnosticCode2D.EVALUATION_FAILED,
						VERSION + ": " + failure + "; a failed evaluation inside the stretch "
								+ "is a failure of the result, never a value" + spent));
				return diagnostics;
			}
			if (signature != null) {
				diagnostics.add(notAdmissible("a chord-scaling signature was observed"));
				diagnostics.add(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.CHORD_SCALING_JUMP_SIGNATURE, VERSION
								+ ": a numeric signature compatible with a discontinuity or "
								+ "with a singular variation of speed was observed in the "
								+ "direction of the chord, inside the interval of the decision ("
								+ signature + "; threshold " + RATIO_THRESHOLD + " over "
								+ CONSECUTIVE_HIGH_RATIOS + " consecutive levels). The "
								+ "continuity that H3 requires is not established by this "
								+ "check and, by conservative policy, the scalar is not "
								+ "admitted. It demonstrates neither a jump nor that H3 is "
								+ "false" + spent));
				return diagnostics;
			}
			diagnostics.add(notAdmissible(exhausted != null
					? "the chord-scaling check was not completed within its budget"
					: "the chord-scaling check could not decide a leaf"));
			if (inconclusive > 0) {
				diagnostics.add(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.CHORD_SCALING_INCONCLUSIVE, VERSION
								+ ": the check could not decide on " + inconclusive
								+ " leaf(s); the first: " + firstInconclusive + spent));
			}
			if (exhausted != null) {
				diagnostics.add(new MetricDiagnostic2D(
						MetricDiagnosticCode2D.CHORD_SCALING_BUDGET_EXHAUSTED, VERSION
								+ ": the budget of " + EVALUATION_CAP + " evaluations per "
								+ "route was spent before every leaf was decided, at "
								+ exhausted + "; " + untested + " further leaf(s) left "
								+ "untested; nothing is concluded about the leaves left "
								+ "untested" + spent));
			}
			return diagnostics;
		}

		private static MetricDiagnostic2D notAdmissible(String why) {
			return new MetricDiagnostic2D(MetricDiagnosticCode2D.CONDITIONED_ROUTE_NOT_ADMISSIBLE,
					"Condition 8 of " + LocusMetricConditionedRoute2D.VERSION + " ("
							+ VERSION + "): " + Objects.requireNonNull(why) + "; the length "
							+ "stays a rich result with its estimate and is no number");
		}
	}

	private static double distance(LocusPoint2D first, LocusPoint2D second) {
		return Math.hypot(first.getX() - second.getX(), first.getY() - second.getY());
	}
}
