/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.conditioned;

/**
 * Versioned policy of the conditioned traversal (candidate contract
 * {@code post-r1-conditioned-use.md}, section 3.4). Its limits are world quantities of the
 * locus that publishes the evidence, in model units and radians. Zoom, DPI, viewport and
 * render policy never enter it. The version is part of the identity of the evidence, so two
 * policies with different values must have different versions.
 */
public final class LocusConditionedTraversalPolicy2D {
	/** Version of the first policy. */
	public static final String VERSION_1 = "conditioned-traversal/v1";

	private final String version;
	private final int initialDepth;
	private final int minimumStepDepth;
	private final double maximumChordDeviation;
	private final double maximumTurning;
	private final double marginThreshold;
	private final int workLimit;

	/**
	 * @param version version of the policy
	 * @param initialDepth the traversal starts from {@code 2^initialDepth} equal cells
	 * @param minimumStepDepth the minimum step is {@code 2^-minimumStepDepth} of the domain
	 * @param maximumChordDeviation chord-deviation limit between nodes, in model units
	 * @param maximumTurning turning limit between nodes, in radians
	 * @param marginThreshold separation above which a span is of the estimated-margin class
	 * @param workLimit largest number of evaluations of one traversal
	 */
	public LocusConditionedTraversalPolicy2D(String version, int initialDepth,
			int minimumStepDepth, double maximumChordDeviation, double maximumTurning,
			double marginThreshold, int workLimit) {
		if (version == null || version.trim().isEmpty() || !version.equals(version.trim())) {
			throw new IllegalArgumentException("Policy version must be canonical");
		}
		if (initialDepth < 0 || minimumStepDepth < initialDepth || minimumStepDepth > 60) {
			throw new IllegalArgumentException("Invalid traversal depths");
		}
		if (!(maximumChordDeviation > 0) || !(maximumTurning > 0) || !(marginThreshold >= 0)
				|| !Double.isFinite(maximumChordDeviation) || !Double.isFinite(maximumTurning)
				|| !Double.isFinite(marginThreshold)) {
			throw new IllegalArgumentException("Invalid traversal limits");
		}
		if (workLimit < 3) {
			throw new IllegalArgumentException("The work limit admits no cell");
		}
		this.version = version;
		this.initialDepth = initialDepth;
		this.minimumStepDepth = minimumStepDepth;
		this.maximumChordDeviation = maximumChordDeviation;
		this.maximumTurning = maximumTurning;
		this.marginThreshold = marginThreshold;
		this.workLimit = workLimit;
	}

	/**
	 * @return the first policy: 32 initial cells, minimum step {@code 2^-20}, chord deviation
	 *         {@code 1e-3} model units, turning 0.35 rad, margin threshold {@code 1e-3} in
	 *         the parameter of the source of the selected root, and a work limit of 1024
	 *         evaluations. The work limit was fixed when increment I0 closed, from the
	 *         measurements of the real protocol on the regression model (257 to 385
	 *         evaluations per locus and revision; {@code
	 *         docs/architecture/post_r1_locus_real_change_performance.md}, section 6). The
	 *         closed list of eligible absence causes of this version is {@link
	 *         LocusConditionedAbsenceCause2D}.
	 */
	public static LocusConditionedTraversalPolicy2D v1() {
		return new LocusConditionedTraversalPolicy2D(VERSION_1, 5, 20, 1e-3, 0.35, 1e-3,
				1024);
	}

	/** @return version of the policy */
	public String getVersion() {
		return version;
	}

	/** @return the traversal starts from {@code 2^initialDepth} equal cells */
	public int getInitialDepth() {
		return initialDepth;
	}

	/** @return the minimum step is {@code 2^-minimumStepDepth} of the declared domain */
	public int getMinimumStepDepth() {
		return minimumStepDepth;
	}

	/** @return chord-deviation limit between nodes, in model units */
	public double getMaximumChordDeviation() {
		return maximumChordDeviation;
	}

	/** @return turning limit between nodes, in radians */
	public double getMaximumTurning() {
		return maximumTurning;
	}

	/** @return separation above which a span is of the estimated-margin class */
	public double getMarginThreshold() {
		return marginThreshold;
	}

	/** @return largest number of evaluations of one traversal */
	public int getWorkLimit() {
		return workLimit;
	}
}
