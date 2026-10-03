/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.layers;

import javax.annotation.CheckForNull;

import org.geogebra.common.main.App;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.plugin.EuclidianStyleConstants;

/**
 * PRE-G9B-R6-plus-A-2 (AQ-L1a): the admissible object-layer domain {@code 0..max} of
 * a product, read from its configuration ({@link AppConfig#getMaxLayer()}). The
 * inherited configuration keeps {@link EuclidianStyleConstants#MAX_LAYERS}; GeoCeDG
 * widens it. Layer is presentation order, never geometric truth.
 */
public final class LayerDomain {

	private LayerDomain() {
	}

	/**
	 * @param app application, may be {@code null} for a detached element
	 * @return highest admissible layer of the application's product
	 */
	public static int maxLayer(@CheckForNull App app) {
		AppConfig config = app == null ? null : app.getConfig();
		return config == null ? EuclidianStyleConstants.MAX_LAYERS : config.getMaxLayer();
	}

	/**
	 * Clamps a requested layer into the domain (DQ-A2-10): below 0 to 0, above the
	 * maximum to the maximum; no wrap.
	 *
	 * @param layer requested layer
	 * @param maxLayer highest admissible layer
	 * @return admissible layer
	 */
	public static int clamp(int layer, int maxLayer) {
		return layer < 0 ? 0 : Math.min(layer, maxLayer);
	}
}
