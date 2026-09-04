/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.gui.layout.LayoutD;
import org.geogebra.desktop.gui.menubar.GeoGebraMenuBar;
import org.geogebra.desktop.main.AppD;

/** Product-only projections of the one declarative action/workspace catalog. */
final class GeoCeDGMenuBar extends GeoGebraMenuBar {

	private static final long serialVersionUID = 1L;
	static final int PRODUCT_MENU_MNEMONIC = KeyEvent.VK_G;
	static final KeyStroke DXF_ACTION_ACCELERATOR = KeyStroke.getKeyStroke(
			KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
	static final String DXF_ACTION_TEXT =
			"Export 2D geometry as DXF (experimental)...";
	private final AppD app;
	private GeoCeDGActionRegistry registry;

	GeoCeDGMenuBar(AppD app) {
		super(app, (LayoutD) app.getGuiManager().getLayout());
		this.app = app;
	}

	@Override
	public void initMenubar() {
		registry = ((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry();
		populateProductMenu();
	}

	private void populateProductMenu() {
		removeAll();
		JSONObject catalog = GeoCeDGProfile.getCatalog();
		try {
			JSONArray sections = catalog.getJSONArray("menu_sections");
			for (int i = 0; i < sections.length(); i++) {
				JSONObject section = sections.getJSONObject(i);
				JMenu menu = new JMenu(registry.text(section.getString("name_key")));
				menu.putClientProperty("geocedg.menu.id", section.getString("id"));
				menu.getAccessibleContext().setAccessibleDescription(menu.getText());
				for (String id : GeoCeDGProfile.strings(section.getJSONArray("cluster_ids"))) {
					JSONObject cluster = find(catalog.getJSONArray("clusters"), id);
					JSONObject taxonomy = find(catalog.getJSONObject("taxonomy")
							.getJSONArray("operational_clusters"), id);
					JMenu group = new JMenu(registry.text(taxonomy.getString("name_key")));
					group.putClientProperty("geocedg.cluster.id", id);
					Set<String> actions = new LinkedHashSet<>();
					for (String placement : new String[] {"toolbar_action_ids",
							"overflow_action_ids",
							"menu_action_ids", "context_action_ids", "inspector_action_ids",
							"settings_action_ids"}) {
						actions.addAll(GeoCeDGProfile.strings(cluster.getJSONArray(placement)));
					}
					ButtonGroup radios = new ButtonGroup();
					for (String actionId : actions) {
						JMenuItem item = createItem(registry.get(actionId), radios);
						group.add(item);
					}
					if (group.getItemCount() > 0) {
						menu.add(group);
					}
				}
				menu.addMenuListener(new MenuListener() {
					@Override
					public void menuSelected(MenuEvent event) {
						registry.refresh();
					}

					@Override
					public void menuDeselected(MenuEvent event) {
						// No semantic state belongs to menu visibility.
					}

					@Override
					public void menuCanceled(MenuEvent event) {
						// Cancellation creates no construction transaction.
					}
				});
				add(menu);
				GeoGebraMenuBar.setMenuFontRecursive(menu, app.getPlainFont());
			}
			app.setComponentOrientation(this);
			revalidate();
			repaint();
		} catch (JSONException exception) {
			throw new IllegalStateException("Validated profile placement failed", exception);
		}
	}

	static JMenuItem createItem(Action action, ButtonGroup radios) {
		String id = (String) action.getValue(GeoCeDGActionRegistry.ACTION_ID);
		JMenuItem item;
		if (id.startsWith("algebra.description.")) {
			item = new JRadioButtonMenuItem(action);
			radios.add(item);
		} else if (action.getValue(Action.SELECTED_KEY) != null) {
			item = new JCheckBoxMenuItem(action);
		} else {
			item = new JMenuItem(action);
		}
		item.putClientProperty(GeoCeDGActionRegistry.ACTION_ID, id);
		item.getAccessibleContext().setAccessibleDescription(
				(String) action.getValue(Action.SHORT_DESCRIPTION));
		return item;
	}

	static JSONObject find(JSONArray array, String id) throws JSONException {
		for (int i = 0; i < array.length(); i++) {
			JSONObject item = array.getJSONObject(i);
			if (id.equals(item.getString("id"))) {
				return item;
			}
		}
		throw new IllegalStateException("Unresolved profile reference " + id);
	}

	@Override
	public void updateFonts() {
		if (registry != null) {
			registry.refresh();
			populateProductMenu();
		}
	}

	@Override
	public void updateMenubar() {
		if (registry != null) {
			registry.refresh();
		}
	}

	@Override
	public void updateCPView(boolean visible) {
		updateMenubar();
	}

	@Override
	public void updateSelection() {
		updateMenubar();
	}

	@Override
	public void updateMenuFile() {
		updateMenubar();
	}

	@Override
	public void updateMenuWindow() {
		updateMenubar();
	}
}
