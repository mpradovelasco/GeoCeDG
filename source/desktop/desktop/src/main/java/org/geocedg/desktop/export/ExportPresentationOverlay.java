/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.EuclidianView;

/**
 * PRE-G9B-MAINT-A-DXF0-R1: non-semantic presentation drawn by the picture-export service on
 * top of the construction objects of an export viewport. An overlay reads only presentation
 * state of the exported source view; it never changes the construction, its drawables or the
 * live view, and it is never the authority of any geometric value. An overlay with nothing to
 * draw for a source leaves every output byte-identical to an export without overlay.
 */
public interface ExportPresentationOverlay {

	/**
	 * @param source live view being exported
	 * @return whether {@link #paint} would draw anything for this source
	 */
	boolean hasContent(EuclidianView source);

	/**
	 * @param source live view being exported
	 * @param viewport export viewport whose coordinate system maps model to output units
	 * @param graphics output graphics, already scaled and clipped to the export area
	 */
	void paint(EuclidianView source, ExportViewport viewport, GGraphics2D graphics);
}
