/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

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

	/** @return workspace control generated from the catalog, not a new action authority */
	JButton createWorkspaceSelector() {
		JButton button = new JButton(registry.text(usesDocumentLayout()
				? "Workspace.DocumentLayout" : "GeoCeDG.Workspace.Construction.Name"));
		button.setFont(app.getPlainFont());
		button.setToolTipText(registry.text("Workspace.Restore"));
		button.getAccessibleContext().setAccessibleDescription(button.getToolTipText());
		button.addActionListener(event -> {
			JPopupMenu menu = new JPopupMenu();
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
			menu.show(button, 0, button.getHeight());
		});
		return button;
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

	/** @return scalable family palette including non-mode actions without fake mode IDs */
	JScrollPane createActionPalette() {
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 2));
		try {
			JSONObject catalog = GeoCeDGProfile.getCatalog();
			JSONArray families = catalog.getJSONObject("taxonomy").getJSONArray("broad_families");
			JSONArray clusters = catalog.getJSONArray("clusters");
			for (int i = 0; i < families.length(); i++) {
				JSONObject family = families.getJSONObject(i);
				String name = registry.text(family.getString("name_key"));
				JButton button = new JButton(name);
				button.setFont(app.getPlainFont());
				button.getAccessibleContext().setAccessibleName(name);
				button.putClientProperty("geocedg.family.id", family.getString("id"));
				button.addFocusListener(new FocusAdapter() {
					@Override
					public void focusGained(FocusEvent event) {
						row.scrollRectToVisible(button.getBounds());
					}
				});
				List<String> clusterIds = new ArrayList<>();
				for (int c = 0; c < clusters.length(); c++) {
					JSONObject cluster = clusters.getJSONObject(c);
					if (family.getString("id").equals(cluster.getString("broad_family_id"))) {
						clusterIds.add(cluster.getString("id"));
					}
				}
				if ("disabled-with-reason".equals(family.getString("availability"))) {
					button.setEnabled(false);
					button.setToolTipText(registry.text(family.getString("reason_key")));
				} else {
					button.addActionListener(event -> {
						registry.refresh();
						JPopupMenu popup = familyPopup(clusterIds);
						popup.show(button, 0, button.getHeight());
					});
					button.setToolTipText(name);
				}
				row.add(button);
			}
		} catch (JSONException exception) {
			throw new IllegalStateException(exception);
		}
		JScrollPane scroll = new JScrollPane(row, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(null);
		scroll.setPreferredSize(new Dimension(640, row.getPreferredSize().height + 20));
		scroll.setMinimumSize(new Dimension(80, row.getPreferredSize().height + 20));
		scroll.getAccessibleContext().setAccessibleName(registry.text("Workspace.More"));
		return scroll;
	}

	private JPopupMenu familyPopup(List<String> clusterIds) {
		JPopupMenu popup = new JPopupMenu();
		try {
			JSONObject catalog = GeoCeDGProfile.getCatalog();
			for (String id : clusterIds) {
				JSONObject cluster = GeoCeDGMenuBar.find(catalog.getJSONArray("clusters"), id);
				JSONObject taxonomy = GeoCeDGMenuBar.find(catalog.getJSONObject("taxonomy")
						.getJSONArray("operational_clusters"), id);
				JMenu group = new JMenu(registry.text(taxonomy.getString("name_key")));
				Set<String> actions = new LinkedHashSet<>();
				for (String key : List.of("toolbar_action_ids", "overflow_action_ids",
						"menu_action_ids", "context_action_ids", "inspector_action_ids",
						"settings_action_ids")) {
					actions.addAll(GeoCeDGProfile.strings(cluster.getJSONArray(key)));
				}
				ButtonGroup radios = new ButtonGroup();
				for (String action : actions) {
					group.add(GeoCeDGMenuBar.createItem(registry.get(action), radios));
				}
				popup.add(group);
			}
		} catch (JSONException exception) {
			throw new IllegalStateException(exception);
		}
		return popup;
	}

	private JPopupMenu actionPopup(Set<String> ids) {
		JPopupMenu popup = new JPopupMenu();
		ButtonGroup radios = new ButtonGroup();
		for (String id : ids) {
			popup.add(GeoCeDGMenuBar.createItem(registry.get(id), radios));
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
		return actionPopup(ids);
	}
}
