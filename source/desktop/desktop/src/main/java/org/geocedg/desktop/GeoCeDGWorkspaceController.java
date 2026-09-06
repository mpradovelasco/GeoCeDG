/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.io.layout.DockPanelData;
import org.geogebra.common.io.layout.DockSplitPaneData;
import org.geogebra.common.io.layout.Perspective;
import org.geogebra.common.main.App;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.gui.layout.LayoutD;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.GeoGebraPreferencesD;

/** Profile projections and isolated layout preferences; never construction XML. */
public final class GeoCeDGWorkspaceController {

	private final AppD app;
	private final GeoCeDGActionRegistry registry;
	private Perspective replacedDocumentLayout;
	private String lastFailure;

	GeoCeDGWorkspaceController(AppD app, GeoCeDGActionRegistry registry) {
		this.app = app;
		this.registry = registry;
	}

	/** @return default workspace or its validated profile-preference layout */
	public static Perspective loadInitialPerspective() {
		if (GeoCeDGProfile.isLegacyFallback()) {
			return GeoCeDGProfile.createInitialPerspective();
		}
		try {
			JSONObject catalog = GeoCeDGProfile.getCatalog();
			JSONObject persistence = catalog.getJSONObject("workspace_persistence");
			String fallback = persistence.getString("unavailable_fallback");
			String active = GeoGebraPreferencesD.getPref().loadPreference(
					persistence.getString("active_workspace_key"), fallback);
			if (!fallback.equals(active)) {
				active = fallback;
			}
			String saved = GeoGebraPreferencesD.getPref().loadPreference(
					persistence.getString("per_workspace_layout_key_prefix") + active, "");
			return restoreLayout(saved);
		} catch (JSONException exception) {
			throw new IllegalStateException("Invalid validated workspace preferences", exception);
		}
	}

	/** @param app product app whose current layout is saved separately from the document */
	public static void saveCurrentLayout(AppD app) {
		if (GeoCeDGProfile.isLegacyFallback()
				|| ((GuiManagerGeoCeDG) app.getGuiManager())
						.getWorkspaceController().usesDocumentLayout()) {
			return;
		}
		try {
			JSONObject catalog = GeoCeDGProfile.getCatalog();
			JSONObject persistence = catalog.getJSONObject("workspace_persistence");
			String active = catalog.getString("default_workspace_id");
			Perspective perspective = ((LayoutD) app.getGuiManager().getLayout())
					.createPerspective();
			if (isMeasurableLayout(perspective)) {
				String encoded = encodeLayout(perspective);
				GeoGebraPreferencesD.getPref().savePreference(
						persistence.getString("active_workspace_key"), active);
				GeoGebraPreferencesD.getPref().savePreference(
						persistence.getString("per_workspace_layout_key_prefix") + active,
						encoded);
			} else {
				// Unrealized/minimized Swing panes have no finite relative divider yet.
				// Keep previous preferences; do not invent ratios or reject the applied UI.
				Log.debug("Workspace preferences deferred until dock ratios are measurable");
			}
		} catch (JSONException exception) {
			throw new IllegalStateException("Cannot save workspace layout", exception);
		}
	}

	static boolean isMeasurableLayout(Perspective perspective) {
		if (perspective == null || perspective.getSplitPaneData() == null) {
			return false;
		}
		for (DockSplitPaneData pane : perspective.getSplitPaneData()) {
			double divider = pane.getDividerLocation();
			if (!Double.isFinite(divider) || divider <= 0 || divider >= 1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Reapply an available declarative workspace; existing host presentation is rollback state.
	 * @param id exact workspace ID
	 * @return whether the workspace was applied without a presentation failure
	 */
	public boolean applyWorkspace(String id) {
		LayoutD layout = (LayoutD) app.getGuiManager().getLayout();
		return applyWorkspace(id, layout::applyPerspective);
	}

	boolean applyWorkspace(String id, Consumer<Perspective> apply) {
		lastFailure = null;
		try {
			JSONObject workspace = GeoCeDGMenuBar.find(
					GeoCeDGProfile.getCatalog().getJSONArray("workspaces"), id);
			if (!"available".equals(workspace.getString("availability"))) {
				return false;
			}
			Perspective next = GeoCeDGProfile.createInitialPerspective();
			LayoutD layout = (LayoutD) app.getGuiManager().getLayout();
			Perspective previous = layout.createPerspective();
			if (previous == null) {
				previous = GeoCeDGProfile.createInitialPerspective();
			}
			app.setMode(EuclidianConstants.MODE_MOVE);
			Perspective document = app instanceof AppGeoCeDG
					&& ((AppGeoCeDG) app).hasDocumentPerspective() ? app.getTmpPerspective() : null;
			Perspective previousReplacedDocument = replacedDocumentLayout;
			try {
				apply.accept(next);
				replacedDocumentLayout = document;
				saveCurrentLayout(app);
				return true;
			} catch (RuntimeException failure) {
				replacedDocumentLayout = previousReplacedDocument;
				lastFailure = failure.toString();
				Log.debug(failure);
				layout.applyPerspective(previous);
				return false;
			}
		} catch (JSONException exception) {
			throw new IllegalStateException("Invalid workspace reference", exception);
		}
	}

	String getLastFailure() {
		return lastFailure;
	}

	boolean usesDocumentLayout() {
		return app instanceof AppGeoCeDG && ((AppGeoCeDG) app).hasDocumentPerspective()
				&& replacedDocumentLayout != app.getTmpPerspective();
	}

	/** @return workspace menu generated from the catalog, not a new action authority */
	JMenu createWorkspaceMenu() {
		JMenu menu = new JMenu(registry.text("Workspace.Restore"));
		menu.setFont(app.getPlainFont());
		menu.putClientProperty("geocedg.document-layout", usesDocumentLayout());
		menu.getAccessibleContext().setAccessibleDescription(usesDocumentLayout()
				? registry.text("Workspace.DocumentLayout") : menu.getText());
		try {
			JSONArray workspaces = GeoCeDGProfile.getCatalog().getJSONArray("workspaces");
			for (int i = 0; i < workspaces.length(); i++) {
				JSONObject workspace = workspaces.getJSONObject(i);
				String id = workspace.getString("id");
				JMenuItem item = new JMenuItem(registry.text(workspace.getString("name_key")));
				item.setEnabled("available".equals(workspace.getString("availability")));
				item.setToolTipText(registry.text(workspace.getString(item.isEnabled()
						? "description_key" : "reason_key")));
				item.addActionListener(select -> {
					if (!applyWorkspace(id)) {
						JOptionPane.showMessageDialog(app.getMainComponent(),
								registry.text("Workspace.ApplyFailed"));
					}
				});
				menu.add(item);
			}
		} catch (JSONException exception) {
			throw new IllegalStateException(exception);
		}
		return menu;
	}

	static String encodeLayout(Perspective perspective) throws JSONException {
		JSONObject layout = new JSONObject();
		JSONArray panes = new JSONArray();
		for (DockSplitPaneData pane : perspective.getSplitPaneData()) {
			panes.put(new JSONObject().put("location", pane.getLocation())
					.put("divider", pane.getDividerLocation())
							.put("orientation", pane.getOrientation()));
		}
		JSONArray views = new JSONArray();
		for (DockPanelData view : perspective.getDockPanelData()) {
			views.put(new JSONObject().put("id", view.getViewId()).put("visible", view.isVisible())
					.put("location", view.getEmbeddedDef()).put("size", view.getEmbeddedSize()));
		}
		return layout.put("version", 1).put("panes", panes).put("views", views).toString();
	}

	static Perspective restoreLayout(String encoded) {
		Perspective result = GeoCeDGProfile.createInitialPerspective();
		if (encoded == null || encoded.isBlank() || encoded.length() > 32768) {
			return result;
		}
		try {
			JSONObject layout = new JSONObject(encoded);
			if (layout.getInt("version") != 1 || layout.length() != 3) {
				return result;
			}
			JSONArray panes = layout.getJSONArray("panes");
			JSONArray views = layout.getJSONArray("views");
			if (panes.length() > 16 || views.length() > 16) {
				return result;
			}
			List<DockSplitPaneData> splits = new ArrayList<>();
			for (int i = 0; i < panes.length(); i++) {
				JSONObject pane = panes.getJSONObject(i);
				double divider = pane.getDouble("divider");
				int orientation = pane.getInt("orientation");
				String location = pane.getString("location");
				if (pane.length() != 3 || !Double.isFinite(divider)
						|| divider <= 0 || divider >= 1 || orientation < 0 || orientation > 1
						|| !location.matches("([01](,[01])*)?")) {
					return result;
				}
				splits.add(new DockSplitPaneData(location, divider, orientation));
			}
			List<DockPanelData> panels = new ArrayList<>();
			Set<Integer> ids = new HashSet<>();
			Set<Integer> allowed = Set.of(App.VIEW_EUCLIDIAN, App.VIEW_ALGEBRA,
					App.VIEW_PROPERTIES, App.VIEW_CONSTRUCTION_PROTOCOL, App.VIEW_SPREADSHEET,
					App.VIEW_CAS, App.VIEW_EUCLIDIAN3D, App.VIEW_EUCLIDIAN2,
					App.VIEW_PROBABILITY_CALCULATOR, App.VIEW_DATA_ANALYSIS);
			for (int i = 0; i < views.length(); i++) {
				JSONObject view = views.getJSONObject(i);
				int id = view.getInt("id");
				String location = view.getString("location");
				int size = view.getInt("size");
				if (view.length() != 4 || !allowed.contains(id) || !ids.add(id)
						|| !location.matches("[0-3](,[0-3])*") || size < 0 || size > 16384) {
					return result;
				}
				panels.add(new DockPanelData(id, null, view.getBoolean("visible"), false,
						id == App.VIEW_PROPERTIES,
						AwtFactory.getPrototype().newRectangle(100, 100, 600, 400),
						location, size));
			}
			if (ids.contains(App.VIEW_EUCLIDIAN) && ids.contains(App.VIEW_ALGEBRA)
					&& validDockTree(splits, panels)) {
				result.setSplitPaneData(splits.toArray(new DockSplitPaneData[0]));
				result.setDockPanelData(panels.toArray(new DockPanelData[0]));
			}
		} catch (JSONException | IllegalArgumentException exception) {
			// Invalid or obsolete preferences fall back without touching a construction.
			return GeoCeDGProfile.createInitialPerspective();
		}
		return result;
	}

	private static boolean validDockTree(List<DockSplitPaneData> panes,
			List<DockPanelData> panels) {
		Set<String> nodes = new HashSet<>();
		if (panes.isEmpty() || !panes.get(0).getLocation().isEmpty()) {
			return false;
		}
		for (DockSplitPaneData pane : panes) {
			String path = pane.getLocation();
			if (!path.isEmpty() && !nodes.contains(parent(path))) {
				return false;
			}
			if (!nodes.add(path)) {
				return false;
			}
		}
		Set<String> occupied = new HashSet<>();
		for (DockPanelData panel : panels) {
			String path = panel.getEmbeddedDef().replace('3', '0').replace('2', '1');
			if (panel.isVisible() && (nodes.contains(path) || !nodes.contains(parent(path))
					|| !occupied.add(path))) {
				return false;
			}
		}
		return true;
	}

	private static String parent(String path) {
		int comma = path.lastIndexOf(',');
		return comma < 0 ? "" : path.substring(0, comma);
	}

	/** @return profile-owned mixed-action flyouts; the application menu is the superset */
	JToolBar createProductToolbar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setBorder(null);
		toolbar.getAccessibleContext().setAccessibleName(registry.text("Menu.Construction"));
		try {
			JSONObject catalog = GeoCeDGProfile.getCatalog();
			JSONArray groups = catalog.getJSONArray("presentation_groups");
			Set<String> included = new LinkedHashSet<>();
			for (String groupId : GeoCeDGProfile.strings(
					catalog.getJSONArray("toolbar_group_ids"))) {
				JSONObject group = GeoCeDGMenuBar.find(groups, groupId);
				List<String> groupActionIds = GeoCeDGProfile.strings(
						group.getJSONArray("toolbar_action_ids"));
				if ("profile-flyout".equals(
						group.optString("toolbar_rendering", "native"))) {
					if (toolbar.getComponentCount() > 0) {
						toolbar.addSeparator();
					}
					toolbar.add(createProfileFlyout(groupId,
							group.getString("name_key"), groupActionIds));
					included.addAll(groupActionIds);
					continue;
				}
				boolean groupStarted = false;
				for (String id : groupActionIds) {
					if (GeoCeDGProfile.getAction(id).mode() == null && included.add(id)) {
						if (!groupStarted && toolbar.getComponentCount() > 0) {
							toolbar.addSeparator();
						}
						groupStarted = true;
						Action action = registry.get(id);
						JButton button = new JButton(action);
						button.putClientProperty(GeoCeDGActionRegistry.ACTION_ID, id);
						button.putClientProperty("geocedg.presentation.group.id", groupId);
						button.setFont(app.getPlainFont());
						button.getAccessibleContext().setAccessibleName(
								(String) action.getValue(Action.NAME));
						button.getAccessibleContext().setAccessibleDescription(
								(String) action.getValue(Action.SHORT_DESCRIPTION));
						toolbar.add(button);
					}
				}
			}
		} catch (JSONException exception) {
			throw new IllegalStateException(exception);
		}
		return toolbar;
	}

	private JToggleButton createProfileFlyout(String groupId, String nameKey,
			List<String> actionIds) {
		String name = registry.text(nameKey);
		JToggleButton button = new JToggleButton(name + " \u25be");
		final JPopupMenu popup = actionPopup(new LinkedHashSet<>(actionIds));
		button.setFont(app.getPlainFont());
		button.setFocusable(false);
		button.setMargin(new Insets(2, 6, 2, 6));
		Dimension size = button.getPreferredSize();
		size.height = app.getScaledIconSize() + 12;
		button.setPreferredSize(size);
		button.setMinimumSize(size);
		button.setMaximumSize(size);
		button.setToolTipText(name);
		button.getAccessibleContext().setAccessibleName(name);
		button.getAccessibleContext().setAccessibleDescription(name);
		button.putClientProperty("geocedg.presentation.group.id", groupId);
		button.putClientProperty("geocedg.toolbar.action.ids",
				List.copyOf(actionIds));
		button.putClientProperty("geocedg.toolbar.popup", popup);
		button.addActionListener(event -> {
			button.setSelected(false);
			popup.show(button, 0, button.getHeight());
		});
		return button;
	}

	private JPopupMenu actionPopup(Set<String> ids) {
		JPopupMenu popup = new JPopupMenu();
		ButtonGroup radios = new ButtonGroup();
		for (String id : ids) {
			JMenuItem item = GeoCeDGMenuBar.createItem(registry.get(id), radios);
			item.setFont(app.getPlainFont());
			popup.add(item);
		}
		return popup;
	}

	/** @return context projection of the same action objects, with live availability */
	public JPopupMenu createContextMenu() {
		registry.refresh();
		Set<String> ids = new LinkedHashSet<>();
		try {
			JSONArray clusters = GeoCeDGProfile.getCatalog().getJSONArray("clusters");
			for (int i = 0; i < clusters.length(); i++) {
				ids.addAll(GeoCeDGProfile.strings(clusters.getJSONObject(i)
						.getJSONArray("context_action_ids")));
			}
		} catch (JSONException exception) {
			throw new IllegalStateException(exception);
		}
		// The normal host object context already supplies Properties. Both
		// definition aliases expose the same read-only view, so retain one route.
		ids.remove("view.properties");
		ids.remove("semantic.curve.inspect-definition");
		return actionPopup(ids);
	}
}
