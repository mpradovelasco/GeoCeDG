/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import com.formdev.flatlaf.FlatLaf;

/**
 * Applies a {@link GeoCeDGPresentationTheme} to the Swing presentation roles of the running
 * GeoCeDG process.
 *
 * <p>Only known presentation roles are written; no color is inferred from proximity to
 * another color. The installer is reached exclusively from {@code AppGeoCeDG}, so the
 * upstream Classic launcher never installs a GeoCeDG preset. The GeoCeDG and Classic
 * launchers are separate processes and separate main classes, so these global
 * {@link UIManager} entries cannot reach a Classic run.
 */
final class GeoCeDGThemeInstaller {

	/** Client-area surface shared by ordinary panels and dialogs. */
	private static final String[] FRAME_KEYS = {
			"Panel.background", "OptionPane.background", "SplitPane.background",
			"TabbedPane.background", "Viewport.background", "ScrollPane.background"
	};

	/** Supporting surface of the menu bar, toolbars and menu popups. */
	private static final String[] SURFACE_KEYS = {
			"MenuBar.background", "ToolBar.background", "PopupMenu.background",
			"MenuItem.background", "Menu.background", "CheckBoxMenuItem.background",
			"RadioButtonMenuItem.background", "RootPane.background"
	};

	/** Content surface of the list/tree/table presentation panels. */
	private static final String[] PANEL_KEYS = {
			"Tree.background", "Table.background", "TableHeader.background",
			"List.background", "Tree.textBackground"
	};

	/** Separator and component border role. */
	private static final String[] BORDER_KEYS = {
			"Separator.foreground", "Component.borderColor",
			"Component.disabledBorderColor", "Table.gridColor"
	};

	/**
	 * GeoCeDG-owned presentation roles.
	 *
	 * <p>Shared Desktop classes that historically hard-coded a color consult these keys and
	 * keep their inherited literal when the key is absent. Classic never defines them, so
	 * no GeoCeDG preset can reach an upstream Classic run through this seam.
	 */
	static final String FRAME_ROLE = "GeoCeDG.presentation.frameBackground";
	/** Supporting surface of menus, toolbars and their popups. */
	static final String SURFACE_ROLE = "GeoCeDG.presentation.surfaceBackground";
	/** Content surface of the Algebra and Construction Protocol presentation panels. */
	static final String PANEL_ROLE = "GeoCeDG.presentation.panelBackground";
	/** Separator and grid role. */
	static final String BORDER_ROLE = "GeoCeDG.presentation.borderColor";

	private static final String[] PRODUCT_ROLES = {
			FRAME_ROLE, SURFACE_ROLE, PANEL_ROLE, BORDER_ROLE
	};

	private static final Map<String, Object> BASELINE = new LinkedHashMap<>();

	private static GeoCeDGPresentationTheme installed;

	private GeoCeDGThemeInstaller() {
		// utility
	}

	/**
	 * Installs the presentation roles of one theme and refreshes the live component tree.
	 *
	 * @param theme theme to install; {@code ORIGINAL} restores the captured baseline
	 */
	static void install(GeoCeDGPresentationTheme theme) {
		applyRoles(theme);
		FlatLaf.updateUI();
	}

	/**
	 * Writes the presentation roles of one theme into {@link UIManager} without touching
	 * the live component tree, so components created afterwards inherit them directly.
	 *
	 * @param theme theme to apply; {@code ORIGINAL} restores the captured baseline
	 */
	static void applyRoles(GeoCeDGPresentationTheme theme) {
		captureBaseline();
		BASELINE.forEach(UIManager::put);
		GeoCeDGPresentationTheme.Palette palette =
				theme == null ? null : theme.palette();
		for (String role : PRODUCT_ROLES) {
			UIManager.put(role, null);
		}
		if (palette != null) {
			put(FRAME_KEYS, palette.frameColor());
			put(SURFACE_KEYS, palette.surfaceColor());
			put(PANEL_KEYS, palette.panelColor());
			put(BORDER_KEYS, palette.borderColor());
			UIManager.put(FRAME_ROLE, new ColorUIResource(palette.frameColor()));
			UIManager.put(SURFACE_ROLE, new ColorUIResource(palette.surfaceColor()));
			UIManager.put(PANEL_ROLE, new ColorUIResource(palette.panelColor()));
			UIManager.put(BORDER_ROLE, new ColorUIResource(palette.borderColor()));
		}
		installed = theme;
	}

	/** @return theme currently installed in this process, or {@code null} */
	static GeoCeDGPresentationTheme installedTheme() {
		return installed;
	}

	private static void captureBaseline() {
		if (!BASELINE.isEmpty()) {
			return;
		}
		captureBaseline(FRAME_KEYS);
		captureBaseline(SURFACE_KEYS);
		captureBaseline(PANEL_KEYS);
		captureBaseline(BORDER_KEYS);
	}

	private static void captureBaseline(String[] keys) {
		for (String key : keys) {
			BASELINE.put(key, UIManager.get(key));
		}
	}

	private static void put(String[] keys, Color color) {
		ColorUIResource resource = new ColorUIResource(color);
		for (String key : keys) {
			UIManager.put(key, resource);
		}
	}
}
