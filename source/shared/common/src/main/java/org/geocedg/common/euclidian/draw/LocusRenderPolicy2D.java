/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import java.util.Objects;

import org.geogebra.common.euclidian.EuclidianView;

/** View-owned tessellation policy, excluded from semantic contracts. */
public final class LocusRenderPolicy2D {
	/** Render-only sampling strategies; neither is semantic geometry. */
	public enum SamplingStrategy {
		UNIFORM_REFERENCE,
		ADAPTIVE_VISUAL
	}

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

	/** Creates an immutable render-only policy. */
	public LocusRenderPolicy2D(int viewId, int width, int height,
			double xScale, double yScale, int samplesPerComponent) {
		this(viewId, width, height, xScale, yScale, samplesPerComponent,
				SamplingStrategy.UNIFORM_REFERENCE, 0, 0);
	}

	private LocusRenderPolicy2D(int viewId, int width, int height,
			double xScale, double yScale, int samplesPerComponent,
			SamplingStrategy samplingStrategy, double visualTolerancePixels,
			int maximumAdaptiveDepth) {
		if (width < 0 || height < 0 || !Double.isFinite(xScale)
				|| !Double.isFinite(yScale) || xScale == 0 || yScale == 0
				|| samplesPerComponent < 2 || samplingStrategy == null) {
			throw new IllegalArgumentException("Invalid render policy");
		}
		if (samplingStrategy == SamplingStrategy.ADAPTIVE_VISUAL
				&& (!Double.isFinite(visualTolerancePixels)
						|| visualTolerancePixels <= 0 || maximumAdaptiveDepth < 2)) {
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
	}

	/**
	 * Creates an adaptive visual policy while retaining a uniform fallback budget.
	 *
	 * @return render-only adaptive policy
	 */
	public static LocusRenderPolicy2D adaptive(int viewId, int width, int height,
			double xScale, double yScale, int fallbackSamplesPerComponent,
			double visualTolerancePixels, int maximumAdaptiveDepth) {
		return new LocusRenderPolicy2D(viewId, width, height, xScale, yScale,
				fallbackSamplesPerComponent, SamplingStrategy.ADAPTIVE_VISUAL,
				visualTolerancePixels, maximumAdaptiveDepth);
	}

	/**
	 * Derives a bounded visual sampling budget from one Euclidian view.
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

	/** @return default bounded adaptive policy derived from one view */
	public static LocusRenderPolicy2D adaptiveFrom(EuclidianView view) {
		int samples = samplesFor(view);
		return adaptive(view.getViewID(), view.getWidth(), view.getHeight(),
				view.getXscale(), view.getYscale(), samples,
				DEFAULT_VISUAL_TOLERANCE_PIXELS, DEFAULT_MAX_ADAPTIVE_DEPTH);
	}

	private static int samplesFor(EuclidianView view) {
		double scale = Math.max(Math.abs(view.getXscale()),
				Math.abs(view.getYscale()));
		int samples = (int) Math.ceil(scale / 4);
		return Math.max(MIN_SAMPLES, Math.min(MAX_SAMPLES, samples));
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

	/**
	 * Render-only inset for open or presentation-clipped endpoints.
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
				&& maximumAdaptiveDepth == that.maximumAdaptiveDepth;
	}

	@Override
	public int hashCode() {
		return Objects.hash(viewId, width, height, xScaleBits, yScaleBits,
				samplesPerComponent, samplingStrategy, visualToleranceBits,
				maximumAdaptiveDepth);
	}
}
