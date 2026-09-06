/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.gui.toolbar.ModeToggleButtonGroup;
import org.geogebra.desktop.gui.toolbar.ModeToggleMenuD;
import org.geogebra.desktop.gui.toolbar.ToolbarContainer;
import org.geogebra.desktop.gui.toolbar.ToolbarD;
import org.geogebra.desktop.main.AppD;

/** Compact native tool flyouts plus declared non-mode actions; menus remain primary discovery. */
final class GeoCeDGToolbarContainer extends ToolbarContainer {

	private static final long serialVersionUID = 1L;
	private final GeoCeDGWorkspaceController workspace;
	private final AppD app;

	GeoCeDGToolbarContainer(AppD app, GeoCeDGWorkspaceController workspace) {
		super(app, true);
		this.workspace = workspace;
		this.app = app;
		ToolbarD inherited = getToolbar(-1);
		removeToolbar(inherited);
		addToolbar(new ProfileToolbar(app, workspace));
	}

	@Override
	public void buildGui() {
		super.buildGui();
		if (workspace != null) {
			boolean horizontal = orientation == SwingConstants.NORTH
					|| orientation == SwingConstants.SOUTH;
			String placement = horizontal ? BorderLayout.WEST : BorderLayout.NORTH;
			Component nativeTools = ((BorderLayout) getLayout()).getLayoutComponent(placement);
			JPanel tools = new JPanel();
			tools.setLayout(new BoxLayout(tools, horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
			if (nativeTools != null) {
				remove(nativeTools);
				tools.add(nativeTools);
			}
			tools.add(GeoCeDGUserTools.createPinnedToolbar(app));
			add(tools, placement);
			revalidate();
		}
	}

	/** Native mode flyouts plus the two bounded mixed-action adapters, in profile order. */
	private static final class ProfileToolbar extends ToolbarD {
		private static final long serialVersionUID = 1L;
		private final AppD app;
		private final GeoCeDGWorkspaceController workspace;
		private final List<ModeToggleMenuD> modeMenus = new ArrayList<>();
		private final List<JToggleButton> actionFlyouts = new ArrayList<>();
		private int selectedMode = -1;

		ProfileToolbar(AppD app, GeoCeDGWorkspaceController workspace) {
			super(app);
			this.app = app;
			this.workspace = workspace;
		}

		@Override
		public void buildGui() {
			removeAll();
			modeMenus.clear();
			actionFlyouts.clear();
			ModeToggleButtonGroup selection = new ModeToggleButtonGroup();
			try {
				JSONObject catalog = GeoCeDGProfile.getCatalog();
				JSONArray groups = catalog.getJSONArray("presentation_groups");
				for (String groupId : GeoCeDGProfile.strings(
						catalog.getJSONArray("toolbar_group_ids"))) {
					JSONObject group = GeoCeDGMenuBar.find(groups, groupId);
					List<String> ids = GeoCeDGProfile.strings(
							group.getJSONArray("toolbar_action_ids"));
					if (getComponentCount() > 0) {
						addSeparator();
					}
					if ("profile-flyout".equals(
							group.optString("toolbar_rendering", "native"))) {
						JToggleButton flyout = workspace.createProfileFlyout(groupId,
								group.getString("name_key"), ids, selection);
						actionFlyouts.add(flyout);
						add(flyout);
					} else {
						addNativeGroup(groupId, ids, selection);
					}
				}
			} catch (JSONException exception) {
				throw new IllegalStateException("Invalid validated toolbar projection", exception);
			}
			setMode(app.getMode());
		}

		private void addNativeGroup(String groupId, List<String> ids,
				ModeToggleButtonGroup selection) {
			ModeToggleMenuD menu = new ModeToggleMenuD(app, this, selection);
			for (String id : ids) {
			Integer mode = GeoCeDGProfile.getAction(id).mode();
			if (mode == null) {
				throw new IllegalStateException(
						"Non-mode action in native toolbar group " + id);
				}
				menu.addMode(mode);
			}
			if (menu.getToolsCount() == 0) {
				throw new IllegalStateException("Empty native toolbar group " + groupId);
			}
			menu.putClientProperty("geocedg.presentation.group.id", groupId);
			menu.putClientProperty("geocedg.toolbar.action.ids", List.copyOf(ids));
			modeMenus.add(menu);
			add(menu);
		}

		@Override
		public int setMode(int newMode) {
			int effective = newMode == EuclidianConstants.MODE_SELECTION_LISTENER
					? EuclidianConstants.MODE_MOVE : newMode;
			for (ModeToggleMenuD menu : modeMenus) {
				if (menu.selectMode(effective)) {
					selectedMode = effective;
					return effective;
				}
			}
			for (JToggleButton flyout : actionFlyouts) {
				if (workspace.selectProfileFlyoutMode(flyout, effective)) {
					selectedMode = effective;
					return effective;
				}
			}
			selectedMode = effective;
			return effective;
		}

		@Override
		public int getSelectedMode() {
			return selectedMode;
		}

		@Override
		public int getFirstMode() {
			return modeMenus.isEmpty() ? -1 : modeMenus.get(0).getFirstMode();
		}
	}
}
