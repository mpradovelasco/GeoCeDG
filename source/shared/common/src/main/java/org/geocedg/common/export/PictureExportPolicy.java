/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.export;

import org.geogebra.common.main.App;
import org.geogebra.common.main.App.ExportType;

/**
 * PRE-G9B-R6-plus-B picture-export rules shared by the scripting command and
 * the application routes. The rules read only the application's export
 * capabilities; they own no export state and no geometry.
 */
public final class PictureExportPolicy {

	private PictureExportPolicy() {
		// rules only
	}

	/**
	 * Author decision DQ-B1 (2026-10-02): where the product offers no animated
	 * export, an animated type is rejected explicitly instead of being a silent
	 * no-op.
	 *
	 * @param app application that would export
	 * @param type requested export type
	 * @return whether the request must be refused
	 */
	public static boolean rejectsAnimatedType(App app, ExportType type) {
		return (type == ExportType.ANIMATED_GIF || type == ExportType.WEBM)
				&& !app.isAnimatedExportAvailable();
	}
}
