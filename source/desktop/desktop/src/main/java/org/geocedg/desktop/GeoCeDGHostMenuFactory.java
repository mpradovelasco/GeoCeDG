/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.geogebra.common.gui.view.algebra.AlgebraView.SortMode;
import org.geogebra.common.main.App;
import org.geogebra.common.main.settings.LabelVisibility;
import org.geogebra.common.properties.impl.general.RoundingIndexProperty;
import org.geogebra.common.util.Util;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.GeoGebraPreferencesD;

/**
 * Product-filtered projections of existing host view and preference state.
 * This class introduces no preference store and no construction authority.
 */
final class GeoCeDGHostMenuFactory {

	static final String HOST_CONTROL_ID = "geocedg.host.control.id";
	private static final List<SortMode> SORT_MODES = List.of(SortMode.DEPENDENCY,
			SortMode.TYPE, SortMode.ORDER, SortMode.LAYER);

	private GeoCeDGHostMenuFactory() {
	}

	static JMenu views(AppD app) {
		JMenu menu = menu(app, "Views");
		Map<JCheckBoxMenuItem, Integer> items = new LinkedHashMap<>();
		items.put(addView(app, menu, "Algebra", App.VIEW_ALGEBRA), App.VIEW_ALGEBRA);
		items.put(addView(app, menu, "Graphics2", App.VIEW_EUCLIDIAN2),
				App.VIEW_EUCLIDIAN2);
		items.put(addView(app, menu, "Spreadsheet", App.VIEW_SPREADSHEET),
				App.VIEW_SPREADSHEET);
		items.put(addView(app, menu, "CAS", App.VIEW_CAS), App.VIEW_CAS);
		items.put(addView(app, menu, "Properties", App.VIEW_PROPERTIES),
				App.VIEW_PROPERTIES);
		refreshOnOpen(menu, () -> items.forEach((item, viewId) ->
				item.setSelected(app.getGuiManager().showView(viewId))));
		return menu;
	}

	static JMenu sortBy(AppD app) {
		JMenu menu = menu(app, "SortBy");
		ButtonGroup group = new ButtonGroup();
		Map<JRadioButtonMenuItem, SortMode> items = new LinkedHashMap<>();
		for (SortMode mode : SORT_MODES) {
			JRadioButtonMenuItem item = radio(app,
					app.getLocalization().getMenu(mode.toString()),
					"sort-by." + mode.name(),
					app.getSettings().getAlgebra().getTreeMode() == mode);
			item.addActionListener(event -> app.getSettings().getAlgebra().setTreeMode(mode));
			group.add(item);
			menu.add(item);
			items.put(item, mode);
		}
		refreshOnOpen(menu, () -> {
			SortMode selected = app.getSettings().getAlgebra().getTreeMode();
			items.forEach((item, mode) -> item.setSelected(selected == mode));
		});
		return menu;
	}

	static JMenu rounding(AppD app) {
		JMenu menu = menu(app, "Rounding");
		RoundingIndexProperty property = new RoundingIndexProperty(app,
				app.getLocalization());
		ButtonGroup group = new ButtonGroup();
		Map<JRadioButtonMenuItem, Integer> items = new LinkedHashMap<>();
		List<Integer> values = property.getValues();
		String[] names = property.getValueNames();
		for (int i = 0; i < values.size(); i++) {
			int index = i;
			JRadioButtonMenuItem item = radio(app, names[i], "rounding." + i,
					property.getValue().equals(values.get(i)));
			item.addActionListener(event -> property.setIndex(index));
			group.add(item);
			menu.add(item);
			items.put(item, values.get(i));
		}
		refreshOnOpen(menu, () -> {
			Integer selected = property.getValue();
			items.forEach((item, value) -> item.setSelected(selected.equals(value)));
		});
		return menu;
	}

	static JMenu labeling(AppD app) {
		JMenu menu = menu(app, "Labeling");
		ButtonGroup group = new ButtonGroup();
		Map<JRadioButtonMenuItem, LabelVisibility> items = new LinkedHashMap<>();
		LabelVisibility selected = app.getSettings().getLabelSettings()
				.getLabelVisibilityForMenu();
		for (LabelVisibility visibility : List.of(LabelVisibility.Automatic,
				LabelVisibility.AlwaysOn, LabelVisibility.AlwaysOff,
				LabelVisibility.PointsOnly)) {
			JRadioButtonMenuItem item = radio(app,
					app.getLocalization().getMenu(visibility.getTransKey()),
					"labeling." + visibility.name(), selected == visibility);
			item.addActionListener(event -> app.setLabelingStyle(visibility.getValue()));
			group.add(item);
			menu.add(item);
			items.put(item, visibility);
		}
		refreshOnOpen(menu, () -> {
			LabelVisibility current = app.getSettings().getLabelSettings()
					.getLabelVisibilityForMenu();
			items.forEach((item, visibility) -> item.setSelected(current == visibility));
		});
		return menu;
	}

	static JMenu fontSize(AppD app) {
		JMenu menu = menu(app, "FontSize");
		ButtonGroup group = new ButtonGroup();
		Map<JRadioButtonMenuItem, Integer> items = new LinkedHashMap<>();
		for (int i = 0; i < Util.menuFontSizesLength(); i++) {
			int size = Util.menuFontSizes(i);
			JRadioButtonMenuItem item = radio(app,
					app.getLocalization().getPlain("Apt", Integer.toString(size)),
					"font-size." + size, app.getFontSize() == size);
			item.addActionListener(event -> app.setFontSize(size, true));
			group.add(item);
			menu.add(item);
			items.put(item, size);
		}
		refreshOnOpen(menu, () -> items.forEach((item, size) ->
				item.setSelected(app.getFontSize() == size)));
		return menu;
	}

	static JMenuItem saveSettings(AppD app) {
		JMenuItem item = item(app, app.getLocalization().getMenu("Settings.Save"),
				"save-settings");
		item.addActionListener(event -> GeoGebraPreferencesD.getPref()
				.saveXMLPreferences(app));
		return item;
	}

	private static JCheckBoxMenuItem addView(AppD app, JMenu menu, String key, int viewId) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(app.getLocalization().getMenu(key),
				app.getGuiManager().showView(viewId));
		configure(app, item, "view." + viewId);
		item.addActionListener(event -> app.getGuiManager().setShowView(item.isSelected(),
				viewId));
		menu.add(item);
		return item;
	}

	private static void refreshOnOpen(JMenu menu, Runnable refresh) {
		menu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent event) {
				refresh.run();
			}

			@Override
			public void menuDeselected(MenuEvent event) {
				// Presentation mirrors host state only while it is opened.
			}

			@Override
			public void menuCanceled(MenuEvent event) {
				// Cancellation does not change host state.
			}
		});
	}

	private static JMenu menu(AppD app, String key) {
		JMenu menu = new JMenu(app.getLocalization().getMenu(key));
		menu.setFont(app.getPlainFont());
		menu.getAccessibleContext().setAccessibleDescription(menu.getText());
		return menu;
	}

	private static JRadioButtonMenuItem radio(AppD app, String text, String id,
			boolean selected) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(text, selected);
		configure(app, item, id);
		return item;
	}

	private static JMenuItem item(AppD app, String text, String id) {
		JMenuItem item = new JMenuItem(text);
		configure(app, item, id);
		return item;
	}

	private static void configure(AppD app, JMenuItem item, String id) {
		item.setFont(app.getPlainFont());
		item.putClientProperty(HOST_CONTROL_ID, id);
		item.getAccessibleContext().setAccessibleDescription(item.getText());
	}
}
