/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import javax.swing.JOptionPane;

import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.main.AppD;

/** Preflights native Desktop selection edits before the first selected object is changed. */
public final class GeoCeDGControlLayoutEdits {
	private GeoCeDGControlLayoutEdits() {
	}

	/** @return whether this edit must wait for explicit detachment */
	public static boolean rejectIncompatible(AppD app, Object[] selection) {
		var layout = app.getKernel().getConstruction().getControlLayout();
		if (!layout.isEmpty()) {
			for (Object item : selection) {
				if (item instanceof GeoElement geo && layout.slot(geo) != null) {
					if (app.isErrorDialogsActive()) {
						showRejection(app);
					}
					return true;
				}
			}
		}
		return false;
	}

	private static void showRejection(AppD app) {
		boolean spanish = "es".equals(app.getLocale().getLanguage());
		String message = app instanceof AppGeoCeDG geoCeDG
				? geoCeDG.layerText("ControlLayout.DetachFirst")
				: spanish ? "Separa el control de su panel en GeoCeDG antes de cambiar esta propiedad."
						: "Detach the control from its panel in GeoCeDG before changing this property.";
		JOptionPane.showMessageDialog(app.getMainComponent(), message,
				spanish ? "Disposición de controles" : "Control layout", JOptionPane.WARNING_MESSAGE);
	}
}
