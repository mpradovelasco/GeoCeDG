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
	private boolean legacyFallback;

	GeoCeDGMenuBar(AppD app) {
		super(app, (LayoutD) app.getGuiManager().getLayout());
		this.app = app;
	}

	@Override
	public void initMenubar() {
		populateFromCatalog(GeoCeDGProfile.getCatalog());
	}

	void populateFromCatalog(JSONObject catalog) {
		legacyFallback = catalog.optInt("schema_version") == 1;
		if (legacyFallback) {
			populateLegacyFallback();
			return;
		}
		if (catalog.optInt("schema_version") != 2) {
			throw new IllegalStateException("Unsupported GeoCeDG menu profile");
		}
		registry = ((GuiManagerGeoCeDG) app.getGuiManager()).getActionRegistry();
		populateProductMenu(catalog);
	}

	private void populateLegacyFallback() {
		registry = null;
		super.initMenubar();
		JMenu diagnostic = new JMenu("GeoCeDG v1");
		diagnostic.setToolTipText(GeoCeDGProfile.getFallbackDiagnostic(
				app.getLocale().getLanguage()));
		diagnostic.getAccessibleContext().setAccessibleName(diagnostic.getText());
		diagnostic.getAccessibleContext().setAccessibleDescription(
				diagnostic.getToolTipText());
		diagnostic.setEnabled(false);
		add(diagnostic);
	}

	private void populateProductMenu(JSONObject catalog) {
		removeAll();
		try {
			JSONArray sections = catalog.getJSONArray("menu_sections");
			JSONArray presentationGroups = catalog.getJSONArray("presentation_groups");
			Set<String> projectedActions = new LinkedHashSet<>();
			for (int i = 0; i < sections.length(); i++) {
				JSONObject section = sections.getJSONObject(i);
				JMenu menu = new JMenu(registry.text(section.getString("name_key")));
				menu.putClientProperty("geocedg.menu.id", section.getString("id"));
				menu.getAccessibleContext().setAccessibleDescription(menu.getText());
				JSONArray entries = section.getJSONArray("entries");
				for (int e = 0; e < entries.length(); e++) {
					JSONObject entry = entries.getJSONObject(e);
					String kind = entry.getString("kind");
					if ("group".equals(kind) || "actions".equals(kind)) {
						JSONObject groupDefinition = find(presentationGroups,
								entry.getString("group_id"));
						if ("group".equals(kind)) {
							JMenu group = new JMenu(registry.text(
									groupDefinition.getString("name_key")));
							group.putClientProperty("geocedg.presentation.group.id",
									groupDefinition.getString("id"));
							addActions(group, groupDefinition, projectedActions);
							menu.add(group);
						} else {
							addActions(menu, groupDefinition, projectedActions);
						}
					} else if ("separator".equals(kind)) {
						menu.addSeparator();
					} else if ("workspace-switcher".equals(kind)) {
						menu.add(((GuiManagerGeoCeDG) app.getGuiManager())
								.getWorkspaceController().createWorkspaceMenu());
					} else if ("user-tools".equals(kind)) {
						claimActions(find(presentationGroups, entry.getString("group_id")),
								projectedActions);
						menu.add(GeoCeDGUserTools.createMenu(app));
					} else if ("host-views".equals(kind)) {
						menu.add(GeoCeDGHostMenuFactory.views(app));
					} else if ("sort-by".equals(kind)) {
						menu.add(GeoCeDGHostMenuFactory.sortBy(app));
					} else if ("rounding".equals(kind)) {
						menu.add(GeoCeDGHostMenuFactory.rounding(app));
					} else if ("labeling".equals(kind)) {
						menu.add(GeoCeDGHostMenuFactory.labeling(app));
					} else if ("font-size".equals(kind)) {
						menu.add(GeoCeDGHostMenuFactory.fontSize(app));
					} else if ("save-settings".equals(kind)) {
						menu.add(GeoCeDGHostMenuFactory.saveSettings(app));
					} else {
						throw new IllegalStateException("Unknown validated menu entry " + kind);
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
			if (!projectedActions.equals(registry.ids())) {
				throw new IllegalStateException("Menu projection does not match action catalog");
			}
			app.setComponentOrientation(this);
			revalidate();
			repaint();
		} catch (JSONException exception) {
			throw new IllegalStateException("Validated profile placement failed", exception);
		}
	}

	private void addActions(JMenu menu, JSONObject group, Set<String> projectedActions)
			throws JSONException {
		ButtonGroup radios = new ButtonGroup();
		for (String actionId : GeoCeDGProfile.strings(group.getJSONArray("action_ids"))) {
			if (!projectedActions.add(actionId)) {
				throw new IllegalStateException("Duplicate menu action projection " + actionId);
			}
			menu.add(createItem(registry.get(actionId), radios));
		}
	}

	private static void claimActions(JSONObject group, Set<String> projectedActions)
			throws JSONException {
		for (String actionId : GeoCeDGProfile.strings(group.getJSONArray("action_ids"))) {
			if (!projectedActions.add(actionId)) {
				throw new IllegalStateException("Duplicate menu action projection " + actionId);
			}
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
		if (legacyFallback) {
			super.updateFonts();
		} else if (registry != null) {
			registry.refresh();
			populateProductMenu(GeoCeDGProfile.getCatalog());
		}
	}

	@Override
	public void updateMenubar() {
		if (legacyFallback) {
			super.updateMenubar();
		} else if (registry != null) {
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
