/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.geogebra.desktop.main.AppD;

/** Compatibility notice shared with the diagnostic Classic profile. */
public final class GeoCeDGControlLayoutNotice {
	private GeoCeDGControlLayoutNotice() {
	}

	static String message(AppD app) {
		return GeoCeDGProfile.getText("ControlLayout.LegacyWarning",
				app.getLocale().getLanguage());
	}

	/** Presents the compatibility notice for an opened document with a layout. */
	public static void show(AppD app) {
		// Direct Open during startup commits before the frame is visible.
		SwingUtilities.invokeLater(() -> {
			if (app.getMainComponent().isShowing()
					&& !app.getKernel().getConstruction().getControlLayout().isEmpty()) {
				JOptionPane.showMessageDialog(app.getMainComponent(), message(app),
						GeoCeDGProfile.getText("ControlLayout.Title",
								app.getLocale().getLanguage()), JOptionPane.WARNING_MESSAGE);
			}
		});
	}
}
