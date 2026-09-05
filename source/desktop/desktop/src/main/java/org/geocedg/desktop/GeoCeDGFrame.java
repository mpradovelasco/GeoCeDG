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

	/** Visible versioned product title. */
	public static final String APPLICATION_TITLE = GeoCeDGProductInfo.applicationTitle();
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
	public void setTitle(String title) {
		String effectiveTitle = title;
		if (app != null) {
			if (APPLICATION_TITLE.equals(title)) {
				effectiveTitle = GeoCeDGProductInfo.windowTitle(null);
			} else if (app.getCurrentFile() != null
					&& app.getCurrentFile().getName().equals(title)) {
				effectiveTitle = GeoCeDGProductInfo.windowTitle(title);
			}
		}
		super.setTitle(effectiveTitle);
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
