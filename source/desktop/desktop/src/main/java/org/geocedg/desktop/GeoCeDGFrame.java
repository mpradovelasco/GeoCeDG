/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import javax.swing.JFrame;

import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.app.GeoGebraFrame3D;
import org.geogebra.desktop.main.AppD;

/**
 * Product frame for GeoCeDG.
 */
public final class GeoCeDGFrame extends GeoGebraFrame3D {

	private static final long serialVersionUID = 1L;

	/** Visible provisional title. */
	public static final String APPLICATION_TITLE = "GeoCeDG";
	/** Windows process identity. */
	public static final String APPLICATION_USER_MODEL_ID = "org.geocedg.desktop";

	@Override
	protected AppD createApplication(CommandLineArguments args, JFrame frame) {
		return new AppGeoCeDG(args, frame);
	}

	@Override
	protected GeoGebraFrame copy() {
		return new GeoCeDGFrame();
	}

	@Override
	public String getApplicationTitle() {
		return APPLICATION_TITLE;
	}

	@Override
	protected String getApplicationUserModelId() {
		return APPLICATION_USER_MODEL_ID;
	}

	/**
	 * @param args command line arguments
	 * @return new GeoCeDG window
	 */
	public static synchronized GeoGebraFrame createNewWindow(
			CommandLineArguments args) {
		return GeoGebraFrame.createNewWindow(args, new GeoCeDGFrame());
	}
}
