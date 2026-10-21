/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import java.util.Objects;

import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.main.App;

/**
 * View-owned tessellation policy, excluded from semantic contracts.
 *
 * <p>PRE-G9B-CU2 ({@code locus-v2-presentation.md}, "PRE-G9B-CU2 amendment (version 2)";
 * decision D-CU2-10): the adaptive strategy is the versioned drawing policy {@value #VERSION}
 * of every product view. A span is accepted when its chord deviation and its turn with the
 * adjacent spans are within the limits, after the minimum depth; the turn is waived only in a
 * certified fold band and at a certified singular vertex; a vertex budget per view coarsens
 * the drawing deterministically. The uniform strategy is a reference of the tests that predate
 * version 2; no product view uses it and the adaptive strategy never falls back to it.</p>
 */
public final class LocusRenderPolicy2D {
	/** Render-only sampling strategies; neither is semantic geometry. */
	public enum SamplingStrategy {
		/** Uniform samples: a test reference that predates presentation version 2. */
		UNIFORM_REFERENCE,
		/** Drawing policy {@value LocusRenderPolicy2D#VERSION}. */
		ADAPTIVE_VISUAL
	}

	/** Version of the adaptive drawing policy of presentation version 2. */
	public static final String VERSION = "locus-render-policy/v2";
	/**
	 * Depth {@code d_min} of the initial partition of every component: {@code 2^d_min} spans
	 * (D-CU2-10, approved range 2 to 6).
	 */
	public static final int DEFAULT_MINIMUM_DEPTH = 4;
	/**
	 * Largest turn {@code theta_max} between consecutive chords outside the certified
	 * exemptions, in degrees (D-CU2-10, approved range 5 to 20).
	 */
	public static final double DEFAULT_MAXIMUM_TURN_DEGREES = 10;
	/** Vertices of one locus in one view (D-CU2-10, approved range 1 024 to 16 384). */
	public static final int DEFAULT_VERTEX_BUDGET = 4096;
	private static final int MIN_SAMPLES = 32;
	private static final int MAX_SAMPLES = 512;
	private static final double DEFAULT_VISUAL_TOLERANCE_PIXELS = 0.75;
	private static final int DEFAULT_MAX_ADAPTIVE_DEPTH = 12;
	private final int viewId;
	private final int width;
	private final int height;
	private final long xScaleBits;
	private final long yScaleBits;
	private final int samplesPerComponent;
	private final SamplingStrategy samplingStrategy;
	private final long visualToleranceBits;
	private final int maximumAdaptiveDepth;
	private final int minimumDepth;
	private final long maximumTurnBits;
	private final int vertexBudget;

	/** Creates an immutable uniform reference policy. */
	public LocusRenderPolicy2D(int viewId, int width, int height,
			double xScale, double yScale, int samplesPerComponent) {
		this(viewId, width, height, xScale, yScale, samplesPerComponent,
				SamplingStrategy.UNIFORM_REFERENCE, 0, 0, 0, 0, 0);
	}

	private LocusRenderPolicy2D(int viewId, int width, int height,
			double xScale, double yScale, int samplesPerComponent,
			SamplingStrategy samplingStrategy, double visualTolerancePixels,
			int maximumAdaptiveDepth, int minimumDepth, double maximumTurnDegrees,
			int vertexBudget) {
		if (width < 0 || height < 0 || !Double.isFinite(xScale)
				|| !Double.isFinite(yScale) || xScale == 0 || yScale == 0
				|| samplesPerComponent < 2 || samplingStrategy == null) {
			throw new IllegalArgumentException("Invalid render policy");
		}
		if (samplingStrategy == SamplingStrategy.ADAPTIVE_VISUAL
				&& (!Double.isFinite(visualTolerancePixels)
						|| visualTolerancePixels <= 0 || maximumAdaptiveDepth < 2
						|| minimumDepth < 1 || minimumDepth > 12
						|| !(maximumTurnDegrees > 0 && maximumTurnDegrees <= 180)
						|| vertexBudget < 2)) {
			throw new IllegalArgumentException("Invalid adaptive render policy");
		}
		this.viewId = viewId;
		this.width = width;
		this.height = height;
		this.xScaleBits = Double.doubleToLongBits(xScale);
		this.yScaleBits = Double.doubleToLongBits(yScale);
		this.samplesPerComponent = samplesPerComponent;
		this.samplingStrategy = samplingStrategy;
		this.visualToleranceBits = Double.doubleToLongBits(visualTolerancePixels);
		this.maximumAdaptiveDepth = maximumAdaptiveDepth;
		this.minimumDepth = minimumDepth;
		this.maximumTurnBits = Double.doubleToLongBits(maximumTurnDegrees);
		this.vertexBudget = vertexBudget;
	}

	/**
	 * Creates the adaptive drawing policy {@value #VERSION} with the versioned constants.
	 *
	 * @param fallbackSamplesPerComponent retained for the signature; version 2 never falls
	 *        back to uniform samples
	 * @param maximumAdaptiveDepth retained for the signature; version 2 refines up to the
	 *        resolution of the parameter
	 * @return render-only adaptive policy
	 */
	public static LocusRenderPolicy2D adaptive(int viewId, int width, int height,
			double xScale, double yScale, int fallbackSamplesPerComponent,
			double visualTolerancePixels, int maximumAdaptiveDepth) {
		return new LocusRenderPolicy2D(viewId, width, height, xScale, yScale,
				fallbackSamplesPerComponent, SamplingStrategy.ADAPTIVE_VISUAL,
				visualTolerancePixels, maximumAdaptiveDepth, DEFAULT_MINIMUM_DEPTH,
				DEFAULT_MAXIMUM_TURN_DEGREES, DEFAULT_VERTEX_BUDGET);
	}

	/**
	 * @param budget vertices of one locus in this view
	 * @return the same adaptive policy with another vertex budget
	 */
	public LocusRenderPolicy2D withVertexBudget(int budget) {
		return withConstants(minimumDepth, getMaximumTurnDegrees(), budget);
	}

	/**
	 * @param depth depth {@code d_min} of the initial partition
	 * @param turnDegrees largest turn between consecutive chords outside the exemptions
	 * @param budget vertices of one locus in this view
	 * @return the same adaptive policy with other drawing constants
	 */
	public LocusRenderPolicy2D withConstants(int depth, double turnDegrees, int budget) {
		if (samplingStrategy != SamplingStrategy.ADAPTIVE_VISUAL) {
			throw new IllegalStateException("Only the adaptive policy has drawing constants");
		}
		return new LocusRenderPolicy2D(viewId, width, height, getXScale(), getYScale(),
				samplesPerComponent, samplingStrategy, getVisualTolerancePixels(),
				maximumAdaptiveDepth, depth, turnDegrees, budget);
	}

	/**
	 * Derives the drawing policy of one Euclidian view.
	 *
	 * @return immutable view-owned render policy
	 */
	public static LocusRenderPolicy2D from(EuclidianView view) {
		return adaptiveFrom(view);
	}

	/** @return bounded uniform reference policy derived from one view */
	public static LocusRenderPolicy2D uniformFrom(EuclidianView view) {
		int samples = samplesFor(view);
		return new LocusRenderPolicy2D(view.getViewID(), view.getWidth(),
				view.getHeight(), view.getXscale(), view.getYscale(), samples);
	}

	/**
	 * @return default adaptive policy derived from one view; while the view is exported at a
	 *         higher output resolution, the visual tolerance follows the output pixels
	 *         (PRE-G9B-R6-plus-B, 2026-10-02)
	 */
	public static LocusRenderPolicy2D adaptiveFrom(EuclidianView view) {
		double resolution = exportResolution(view);
		int samples = samplesFor(view, resolution);
		return adaptive(view.getViewID(), view.getWidth(), view.getHeight(),
				view.getXscale(), view.getYscale(), samples,
				DEFAULT_VISUAL_TOLERANCE_PIXELS / resolution, DEFAULT_MAX_ADAPTIVE_DEPTH);
	}

	private static int samplesFor(EuclidianView view) {
		return samplesFor(view, 1);
	}

	private static int samplesFor(EuclidianView view, double resolution) {
		double scale = Math.max(Math.abs(view.getXscale()),
				Math.abs(view.getYscale())) * resolution;
		int samples = (int) Math.ceil(scale / 4);
		return Math.max(MIN_SAMPLES, Math.min(MAX_SAMPLES, samples));
	}

	/**
	 * @return output pixels per view pixel while the view's application is
	 *         exporting, at least 1; render-only, never semantic
	 */
	private static double exportResolution(EuclidianView view) {
		App app = view.getApplication();
		if (app == null || !app.isExporting()) {
			return 1;
		}
		double scale = app.getExportScale();
		return Double.isFinite(scale) && scale > 1 ? scale : 1;
	}

	public int getSamplesPerComponent() {
		return samplesPerComponent;
	}

	public SamplingStrategy getSamplingStrategy() {
		return samplingStrategy;
	}

	public double getXScale() {
		return Double.longBitsToDouble(xScaleBits);
	}

	public double getYScale() {
		return Double.longBitsToDouble(yScaleBits);
	}

	public double getVisualTolerancePixels() {
		return Double.longBitsToDouble(visualToleranceBits);
	}

	public int getMaximumAdaptiveDepth() {
		return maximumAdaptiveDepth;
	}

	/** @return depth {@code d_min} of the initial partition of a component */
	public int getMinimumDepth() {
		return minimumDepth;
	}

	/** @return largest turn between consecutive chords outside the exemptions, in degrees */
	public double getMaximumTurnDegrees() {
		return Double.longBitsToDouble(maximumTurnBits);
	}

	/** @return vertices of one locus in this view */
	public int getVertexBudget() {
		return vertexBudget;
	}

	/** @return version of the drawing policy, or the name of the uniform reference */
	public String getVersion() {
		return samplingStrategy == SamplingStrategy.ADAPTIVE_VISUAL ? VERSION
				: "uniform-reference/v1";
	}

	/**
	 * Render-only inset for open or presentation-clipped endpoints of the uniform
	 * reference.
	 *
	 * @return dimensionless fraction of the component parameter span
	 */
	public double getEndpointInsetFraction() {
		return 1.0 / (samplesPerComponent + 1.0);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LocusRenderPolicy2D)) {
			return false;
		}
		LocusRenderPolicy2D that = (LocusRenderPolicy2D) other;
		return viewId == that.viewId && width == that.width
				&& height == that.height && xScaleBits == that.xScaleBits
				&& yScaleBits == that.yScaleBits
				&& samplesPerComponent == that.samplesPerComponent
				&& samplingStrategy == that.samplingStrategy
				&& visualToleranceBits == that.visualToleranceBits
				&& maximumAdaptiveDepth == that.maximumAdaptiveDepth
				&& minimumDepth == that.minimumDepth
				&& maximumTurnBits == that.maximumTurnBits
				&& vertexBudget == that.vertexBudget;
	}

	@Override
	public int hashCode() {
		return Objects.hash(viewId, width, height, xScaleBits, yScaleBits,
				samplesPerComponent, samplingStrategy, visualToleranceBits,
				maximumAdaptiveDepth, minimumDepth, maximumTurnBits, vertexBudget);
	}
}
