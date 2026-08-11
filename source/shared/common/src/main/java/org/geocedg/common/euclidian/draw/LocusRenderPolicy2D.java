/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.euclidian.draw;

import java.util.Objects;

import org.geogebra.common.euclidian.EuclidianView;

/** View-owned tessellation policy, excluded from semantic contracts. */
public final class LocusRenderPolicy2D {
	private static final int MIN_SAMPLES = 32;
	private static final int MAX_SAMPLES = 512;
	private final int viewId;
	private final int width;
	private final int height;
	private final long xScaleBits;
	private final long yScaleBits;
	private final int samplesPerComponent;

	/** Creates an immutable render-only policy. */
	public LocusRenderPolicy2D(int viewId, int width, int height,
			double xScale, double yScale, int samplesPerComponent) {
		if (width < 0 || height < 0 || !Double.isFinite(xScale)
				|| !Double.isFinite(yScale) || samplesPerComponent < 2) {
			throw new IllegalArgumentException("Invalid render policy");
		}
		this.viewId = viewId;
		this.width = width;
		this.height = height;
		this.xScaleBits = Double.doubleToLongBits(xScale);
		this.yScaleBits = Double.doubleToLongBits(yScale);
		this.samplesPerComponent = samplesPerComponent;
	}

	/**
	 * Derives a bounded visual sampling budget from one Euclidian view.
	 *
	 * @return immutable view-owned render policy
	 */
	public static LocusRenderPolicy2D from(EuclidianView view) {
		double scale = Math.max(Math.abs(view.getXscale()),
				Math.abs(view.getYscale()));
		int samples = (int) Math.ceil(scale / 4);
		samples = Math.max(MIN_SAMPLES, Math.min(MAX_SAMPLES, samples));
		return new LocusRenderPolicy2D(view.getViewID(), view.getWidth(),
				view.getHeight(), view.getXscale(), view.getYscale(), samples);
	}

	public int getSamplesPerComponent() {
		return samplesPerComponent;
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
				&& samplesPerComponent == that.samplesPerComponent;
	}

	@Override
	public int hashCode() {
		return Objects.hash(viewId, width, height, xScaleBits, yScaleBits,
				samplesPerComponent);
	}
}
