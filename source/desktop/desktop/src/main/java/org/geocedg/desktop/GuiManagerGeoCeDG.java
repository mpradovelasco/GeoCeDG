/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import org.geogebra.desktop.geogebra3D.gui.GuiManager3D;
import org.geogebra.desktop.gui.menubar.GeoGebraMenuBar;
import org.geogebra.desktop.main.AppD;

/** GeoCeDG GUI manager preserving all inherited 3D/Desktop behavior. */
final class GuiManagerGeoCeDG extends GuiManager3D {

	GuiManagerGeoCeDG(AppD app) {
		super(app);
	}

	@Override
	protected GeoGebraMenuBar newMenuBar() {
		return new GeoCeDGMenuBar(getApp());
	}
}
