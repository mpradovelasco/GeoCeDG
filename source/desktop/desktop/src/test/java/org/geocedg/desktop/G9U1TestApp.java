/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JPanel;

import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.util.LoggerD;

/** Shared desktop test host; commands still use the real application dispatcher. */
final class G9U1TestApp {

	private G9U1TestApp() {
	}

	static AppGeoCeDG create() {
		return create(true);
	}

	static AppGeoCeDG create(boolean enabled) {
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(
				new String[] {"--silent", "--enableLocusV2=" + enabled}), new JPanel());
		app.setErrorDialogsActive(false);
		return app;
	}

	static GeoElement eval(AppGeoCeDG app, String command) {
		GeoElementND[] result = app.getKernel().getAlgebraProcessor()
				.processAlgebraCommand(command, false);
		assertNotNull(result, command);
		assertTrue(result.length > 0, command);
		return result[0].toGeoElement();
	}

	static GeoElement lookup(AppGeoCeDG app, String label) {
		GeoElement result = app.getKernel().lookupLabel(label);
		assertNotNull(result, label);
		return result;
	}
}
