/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import org.geogebra.desktop.gui.layout.LayoutD;
import org.geogebra.desktop.gui.menubar.GeoGebraMenuBar;
import org.geogebra.desktop.main.AppD;

/** Menu additions owned exclusively by the GeoCeDG application profile. */
final class GeoCeDGMenuBar extends GeoGebraMenuBar {

	private static final long serialVersionUID = 1L;
	static final int PRODUCT_MENU_MNEMONIC = KeyEvent.VK_G;
	static final KeyStroke DXF_ACTION_ACCELERATOR = KeyStroke.getKeyStroke(
			KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
	static final String DXF_ACTION_TEXT =
			"Export 2D geometry as DXF (experimental)...";
	private final AppD app;

	GeoCeDGMenuBar(AppD app) {
		super(app, (LayoutD) app.getGuiManager().getLayout());
		this.app = app;
	}

	@Override
	public void initMenubar() {
		super.initMenubar();
		JMenu productMenu = new JMenu("GeoCeDG");
		productMenu.setMnemonic(PRODUCT_MENU_MNEMONIC);
		AbstractAction exportDxf = new AbstractAction(DXF_ACTION_TEXT) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				new GeoCeDGDxfExportController(app).showExportDialog();
			}
		};
		exportDxf.putValue(Action.ACCELERATOR_KEY, DXF_ACTION_ACCELERATOR);
		productMenu.add(exportDxf);
		int helpIndex = Math.max(0, getMenuCount() - 1);
		add(productMenu, helpIndex);
		app.setComponentOrientation(this);
	}
}
