/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

/**
 * Import limits of {@code dxf-spline-reference/v1}. The defaults are the candidate values of
 * the contract (open decision Q-DXF-1, adopted by the author as initial values revisable after
 * characterization) plus the declared tessellation work limit of section 8.
 */
public final class DxfImportLimits {

	/** Candidate {@code FILE_BYTES_MAX}: 64 MiB. */
	public static final long FILE_BYTES_MAX = 64L * 1024 * 1024;
	/** Candidate {@code SPLINE_ENTITIES_MAX}. */
	public static final int SPLINE_ENTITIES_MAX = 10_000;
	/** Candidate {@code P_MAX_IMPORT}, aligned with the family. */
	public static final int P_MAX_IMPORT = 7;
	/** Candidate {@code N_MAX_IMPORT}: largest number of control points. */
	public static final int N_MAX_IMPORT = 1024;
	/** Candidate {@code W_RATIO_MAX}: largest {@code max w / min w}. */
	public static final double W_RATIO_MAX = 1e6;
	/** Candidate {@code RENDER_REL_TOL}: chordal tolerance relative to the control box. */
	public static final double RENDER_REL_TOL = 1e-4;
	/**
	 * Declared tessellation work limit: at most this many curve evaluations per record, plus
	 * the two ends of each span, which are always evaluated (at most {@code 2 N_MAX_IMPORT}).
	 * When it is reached the remaining intervals are drawn as chords and the record is marked
	 * incomplete.
	 */
	public static final int RENDER_MAX_EVALUATIONS = 200_000;
	/** Deepest bisection of one span interval during tessellation. */
	public static final int RENDER_MAX_DEPTH = 24;
	/** Bisection levels every span receives before the flatness test applies. */
	public static final int RENDER_MIN_DEPTH = 3;

	/** The contract's candidate limits. */
	public static final DxfImportLimits DEFAULT = new DxfImportLimits(FILE_BYTES_MAX,
			SPLINE_ENTITIES_MAX, RENDER_MAX_EVALUATIONS);

	private final long fileBytesMax;
	private final int splineEntitiesMax;
	private final int renderMaxEvaluations;

	private DxfImportLimits(long fileBytesMax, int splineEntitiesMax,
			int renderMaxEvaluations) {
		this.fileBytesMax = fileBytesMax;
		this.splineEntitiesMax = splineEntitiesMax;
		this.renderMaxEvaluations = renderMaxEvaluations;
	}

	/**
	 * Smaller limits for characterization and tests; the product uses {@link #DEFAULT}.
	 *
	 * @param fileBytesMax largest admitted file size, at most {@link #FILE_BYTES_MAX}
	 * @param splineEntitiesMax largest admitted SPLINE count, at most
	 *            {@link #SPLINE_ENTITIES_MAX}
	 * @param renderMaxEvaluations tessellation work limit per record, at least 2, at most
	 *            {@link #RENDER_MAX_EVALUATIONS}
	 * @return the limits
	 */
	public static DxfImportLimits reduced(long fileBytesMax, int splineEntitiesMax,
			int renderMaxEvaluations) {
		if (fileBytesMax < 0 || fileBytesMax > FILE_BYTES_MAX || splineEntitiesMax < 0
				|| splineEntitiesMax > SPLINE_ENTITIES_MAX || renderMaxEvaluations < 2
				|| renderMaxEvaluations > RENDER_MAX_EVALUATIONS) {
			throw new IllegalArgumentException("limits outside the contract values");
		}
		return new DxfImportLimits(fileBytesMax, splineEntitiesMax, renderMaxEvaluations);
	}

	/**
	 * @return largest admitted file size in bytes
	 */
	public long fileBytesMax() {
		return fileBytesMax;
	}

	/**
	 * @return largest admitted number of candidate SPLINE entities
	 */
	public int splineEntitiesMax() {
		return splineEntitiesMax;
	}

	/**
	 * @return tessellation work limit per record (curve evaluations)
	 */
	public int renderMaxEvaluations() {
		return renderMaxEvaluations;
	}
}
