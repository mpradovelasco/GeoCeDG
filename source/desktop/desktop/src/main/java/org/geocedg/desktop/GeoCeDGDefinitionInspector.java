/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.main.AppD;

/** Read-only constructive definition, independent of global Algebra presentation. */
public final class GeoCeDGDefinitionInspector {

	private GeoCeDGDefinitionInspector() {
	}

	/**
	 * @param geo inspected object
	 * @return its existing editable-form definition
	 */
	public static String definition(GeoElement geo) {
		return geo == null ? "" : geo.getDefinitionForInputBar();
	}

	/** @param app product application @param geo inspected object */
	public static void show(AppD app, GeoElement geo) {
		if (geo == null) {
			return;
		}
		String title = app.getLocalization().getMenu("Definition");
		JTextArea text = new JTextArea(definition(geo), 8, 48);
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		text.setFont(app.getPlainFont());
		text.getAccessibleContext().setAccessibleName(title);
		text.setCaretPosition(0);
		JOptionPane.showMessageDialog(app.getMainComponent(), new JScrollPane(text),
				title, JOptionPane.INFORMATION_MESSAGE);
	}
}
