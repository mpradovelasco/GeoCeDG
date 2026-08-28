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

import org.geocedg.common.main.feature.RuntimeFeatureService;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.main.App;
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
	private JMenu productMenu;

	GeoCeDGMenuBar(AppD app) {
		super(app, (LayoutD) app.getGuiManager().getLayout());
		this.app = app;
	}

	@Override
	public void initMenubar() {
		super.initMenubar();
		productMenu = new JMenu("GeoCeDG");
		populateProductMenu();
		int helpIndex = Math.max(0, getMenuCount() - 1);
		add(productMenu, helpIndex);
		app.setComponentOrientation(this);
	}

	@Override
	public void updateFonts() {
		super.updateFonts();
		if (productMenu != null) {
			populateProductMenu();
			GeoGebraMenuBar.setMenuFontRecursive(productMenu,
					app.getPlainFont());
			app.setComponentOrientation(productMenu);
		}
	}

	private void populateProductMenu() {
		productMenu.removeAll();
		productMenu.setText("GeoCeDG");
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
		if (RuntimeFeatureService.mayCreateLocusV2(
				app.getKernel().getConstruction())) {
			productMenu.addSeparator();
			productMenu.add(createModeAction("LocusV2.Tool", "LocusV2.Help",
					KeyEvent.VK_V, EuclidianConstants.MODE_LOCUS_V2));
			productMenu.add(createModeAction("LocusV2.Point.Tool",
					"LocusV2.Point.Help", KeyEvent.VK_P,
					EuclidianConstants.MODE_LOCUS_V2_POINT));
			productMenu.add(createModeAction("LocusLength.Total.Tool",
					"LocusLength.Total.Help", KeyEvent.VK_T,
					EuclidianConstants.MODE_LOCUS_V2_LENGTH));
			productMenu.add(createModeAction("LocusLength.Partial.Tool",
					"LocusLength.Partial.Help", KeyEvent.VK_A,
					EuclidianConstants.MODE_LOCUS_V2_LENGTH_BETWEEN));
			productMenu.add(createInspectorAction());
		}
	}

	private AbstractAction createModeAction(String textKey, String helpKey,
			int mnemonic, int mode) {
		AbstractAction action = new AbstractAction(app.getLocalization()
				.getMenu(textKey)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				app.setActiveView(App.VIEW_EUCLIDIAN);
				app.setMode(mode);
			}
		};
		action.putValue(Action.MNEMONIC_KEY, mnemonic);
		action.putValue(Action.SHORT_DESCRIPTION,
				app.getLocalization().getMenu(helpKey));
		return action;
	}

	private AbstractAction createInspectorAction() {
		AbstractAction action = new AbstractAction(app.getLocalization()
				.getMenu("LocusV2.Results.Inspect")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				EuclidianController controller = app.getEuclidianView1()
						.getEuclidianController();
				if (controller instanceof GeoCeDGEuclidianController) {
					((GeoCeDGEuclidianController) controller)
							.inspectRichResultSelection();
				}
			}
		};
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		action.putValue(Action.SHORT_DESCRIPTION, app.getLocalization()
				.getMenu("LocusV2.Results.Help"));
		return action;
	}
}
