/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
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
	private static final String TOOLBAR_BUTTON = "geocedg.toolbar.button";
	private final GeoCeDGWorkspaceController workspace;
	private final AppD app;
	private final ProfileToolbar profileToolbar;

	GeoCeDGToolbarContainer(AppD app, GeoCeDGWorkspaceController workspace) {
		super(app, true);
		this.workspace = workspace;
		this.app = app;
		ToolbarD inherited = getToolbar(-1);
		removeToolbar(inherited);
		profileToolbar = new ProfileToolbar(app, workspace);
		addToolbar(profileToolbar);
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
			tools.add(GeoCeDGUserTools.createPinnedToolbar(app,
					profileToolbar.getNativeVisualReference()));
			add(tools, placement);
			revalidate();
		}
	}

	/**
	 * Apply the geometry and Swing presentation policy of an actual native toolbar
	 * button. This intentionally does not copy the model, action, icon or UI delegate.
	 * @param target GeoCeDG presentation adapter
	 * @param reference live native ToolToggleButton from the same toolbar
	 */
	static void applyNativeToolPresentation(JToggleButton target,
			JToggleButton reference) {
		if (reference == null) {
			throw new IllegalStateException("Native toolbar presentation is unavailable");
		}
		target.setFont(reference.getFont());
		target.setFocusable(reference.isFocusable());
		target.setRequestFocusEnabled(reference.isRequestFocusEnabled());
		target.setFocusPainted(reference.isFocusPainted());
		target.setBorderPainted(reference.isBorderPainted());
		target.setContentAreaFilled(reference.isContentAreaFilled());
		target.setRolloverEnabled(reference.isRolloverEnabled());
		target.setOpaque(reference.isOpaque());
		target.setBorder(reference.getBorder());
		Insets margin = reference.getMargin();
		target.setMargin(margin == null ? null
				: new Insets(margin.top, margin.left, margin.bottom, margin.right));
		target.setAlignmentX(reference.getAlignmentX());
		target.setAlignmentY(reference.getAlignmentY());
		target.setHorizontalAlignment(reference.getHorizontalAlignment());
		target.setVerticalAlignment(reference.getVerticalAlignment());
		target.setHorizontalTextPosition(reference.getHorizontalTextPosition());
		target.setVerticalTextPosition(reference.getVerticalTextPosition());
		target.setIconTextGap(reference.getIconTextGap());
		target.setComponentOrientation(reference.getComponentOrientation());
		setDimensions(target, reference.getPreferredSize(), reference.getMinimumSize(),
				reference.getMaximumSize());
	}

	private static void setDimensions(JToggleButton target, Dimension preferred,
			Dimension minimum, Dimension maximum) {
		target.setPreferredSize(new Dimension(preferred));
		target.setMinimumSize(new Dimension(minimum));
		target.setMaximumSize(new Dimension(maximum));
	}

	private static JPanel nativeFlyoutContainer(JToggleButton button,
			JToggleButton reference) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setAlignmentX(reference.getParent().getAlignmentX());
		panel.setAlignmentY(reference.getParent().getAlignmentY());
		panel.putClientProperty(TOOLBAR_BUTTON, button);
		panel.add(button);
		return panel;
	}

	static JToggleButton toolbarButton(JComponent group) {
		if (group instanceof ModeToggleMenuD menu) {
			return menu.getJToggleButton();
		}
		Object button = group.getClientProperty(TOOLBAR_BUTTON);
		if (button instanceof JToggleButton toggle) {
			return toggle;
		}
		if (group instanceof JToggleButton toggle) {
			return toggle;
		}
		throw new IllegalArgumentException("Toolbar group has no button");
	}

	static JToggleButton createNativeToolReference(AppD app) {
		ToolbarD toolbar = new ToolbarD(app);
		ModeToggleMenuD menu = new ModeToggleMenuD(app, toolbar,
				new ModeToggleButtonGroup());
		menu.addMode(EuclidianConstants.MODE_MOVE);
		return menu.getJToggleButton();
	}

	/** Native mode flyouts plus the two bounded mixed-action adapters, in profile order. */
	private static final class ProfileToolbar extends ToolbarD {
		private static final long serialVersionUID = 1L;
		private final AppD app;
		private final GeoCeDGWorkspaceController workspace;
		private final List<ModeToggleMenuD> modeMenus = new ArrayList<>();
		private final List<JToggleButton> actionFlyouts = new ArrayList<>();
		private JToggleButton nativeVisualReference;
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
			nativeVisualReference = null;
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
						if (nativeVisualReference == null) {
							throw new IllegalStateException(
									"Profile flyout precedes every native toolbar group");
						}
						JToggleButton flyout = workspace.createProfileFlyout(groupId,
								group.getString("name_key"), ids, selection,
								nativeVisualReference);
						actionFlyouts.add(flyout);
						JPanel flyoutContainer = nativeFlyoutContainer(flyout,
								nativeVisualReference);
						flyoutContainer.putClientProperty("geocedg.presentation.group.id",
								groupId);
						flyoutContainer.putClientProperty("geocedg.toolbar.action.ids",
								List.copyOf(ids));
						add(flyoutContainer);
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
			if (nativeVisualReference == null) {
				nativeVisualReference = menu.getJToggleButton();
			}
			add(menu);
		}

		JToggleButton getNativeVisualReference() {
			if (nativeVisualReference == null) {
				throw new IllegalStateException("Toolbar has no native visual reference");
			}
			return nativeVisualReference;
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
