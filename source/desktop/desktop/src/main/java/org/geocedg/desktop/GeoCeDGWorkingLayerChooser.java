/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * PRE-G9B-R6-plus-A-1 numeric chooser of the working layer, bounded by the
 * product's configured layer domain (PRE-G9B-R6-plus-A-2: 0..L_MAX). It names a
 * layer explicitly, so a hidden layer chosen here is shown (AQ-L7).
 */
@FunctionalInterface
interface GeoCeDGWorkingLayerChooser {

	/**
	 * @param current current working layer
	 * @return chosen layer in the domain, or null when the user cancels
	 */
	Integer choose(int current);

	/**
	 * @param app application
	 * @return the product dialog: a list of the admitted layers only, so no value
	 *         outside the domain can be entered
	 */
	static GeoCeDGWorkingLayerChooser dialog(AppGeoCeDG app) {
		return current -> {
			int maxLayer = app.getLayerWorkspace().getMaxLayer();
			Integer[] layers = new Integer[maxLayer - GeoCeDGLayerWorkspace.MIN_LAYER + 1];
			for (int i = 0; i < layers.length; i++) {
				layers[i] = GeoCeDGLayerWorkspace.MIN_LAYER + i;
			}
			Component parent = app.getMainComponent();
			Object chosen = JOptionPane.showInputDialog(parent,
					app.layerText("Workspace.Layer.ChooserMessage",
							Integer.toString(maxLayer)),
					app.layerText("Workspace.Layer.ChooserTitle"),
					JOptionPane.QUESTION_MESSAGE, null, layers, current);
			return chosen instanceof Integer ? (Integer) chosen : null;
		};
	}
}
