/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.locus;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.geocedg.desktop.AppGeoCeDG;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.gui.app.GeoGebraFrame;
import org.geogebra.desktop.gui.app.GeoGebraFrame3D;
import org.geogebra.desktop.main.AppD;

/** Separate frame identity for the developer-only laboratory process. */
public final class LocusV2LaboratoryFrame extends GeoGebraFrame3D {
	private static final long serialVersionUID = 1L;

	/** Visible title that cannot be confused with normal GeoCeDG or Classic. */
	public static final String APPLICATION_TITLE =
			"GeoCeDG - Locus V2 Developer Laboratory";
	/** Separate Windows application identity for the opt-in developer process. */
	public static final String APPLICATION_USER_MODEL_ID =
			"org.geocedg.desktop.locus-v2-laboratory";

	@Override
	protected AppD createApplication(CommandLineArguments args, JFrame frame) {
		AppGeoCeDG app = new AppGeoCeDG(args, frame);
		SwingUtilities.invokeLater(() -> LocusV2LaboratoryController.open(app));
		return app;
	}

	@Override
	protected GeoGebraFrame copy() {
		return new LocusV2LaboratoryFrame();
	}

	@Override
	public String getApplicationTitle() {
		return APPLICATION_TITLE;
	}

	@Override
	protected String getApplicationUserModelId() {
		return APPLICATION_USER_MODEL_ID;
	}
}
