/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * PRE-G9B-R6-plus-A-1 numeric chooser of the working layer, bounded by the
 * unchanged host domain 0..9. It names a layer explicitly, so a hidden layer
 * chosen here is shown (AQ-L7).
 */
@FunctionalInterface
interface GeoCeDGWorkingLayerChooser {

	/**
	 * @param current current working layer
	 * @return chosen layer in 0..9, or null when the user cancels
	 */
	Integer choose(int current);

	/**
	 * @param app application
	 * @return the product dialog: a list of the admitted layers only, so no value
	 *         outside the domain can be entered
	 */
	static GeoCeDGWorkingLayerChooser dialog(AppGeoCeDG app) {
		return current -> {
			Integer[] layers = new Integer[GeoCeDGLayerWorkspace.MAX_LAYER
					- GeoCeDGLayerWorkspace.MIN_LAYER + 1];
			for (int i = 0; i < layers.length; i++) {
				layers[i] = GeoCeDGLayerWorkspace.MIN_LAYER + i;
			}
			Component parent = app.getMainComponent();
			Object chosen = JOptionPane.showInputDialog(parent,
					app.layerText("Workspace.Layer.ChooserMessage"),
					app.layerText("Workspace.Layer.ChooserTitle"),
					JOptionPane.QUESTION_MESSAGE, null, layers, current);
			return chosen instanceof Integer ? (Integer) chosen : null;
		};
	}
}
