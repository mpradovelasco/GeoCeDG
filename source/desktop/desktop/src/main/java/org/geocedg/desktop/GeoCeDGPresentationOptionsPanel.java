/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geocedg.desktop.GeoCeDGPresentationPreferences.Category;
import org.geogebra.common.gui.SetLabels;
import org.geogebra.desktop.gui.dialog.options.OptionPanelD;

/** Compact Advanced-options panel for the six independent S4 preferences. */
final class GeoCeDGPresentationOptionsPanel extends JPanel
		implements OptionPanelD, SetLabels {
	private static final long serialVersionUID = 1L;
	private final AppGeoCeDG app;
	private final Map<Category, JLabel> labels = new EnumMap<>(Category.class);
	private final Map<Category, JComboBox<Integer>> controls = new EnumMap<>(Category.class);
	private boolean updating;

	GeoCeDGPresentationOptionsPanel(AppGeoCeDG app) {
		this.app = app;
		setLayout(new GridBagLayout());
		int row = 0;
		for (Category category : Category.values()) {
			JLabel label = new JLabel();
			JComboBox<Integer> control = new JComboBox<>();
			for (int size : GeoCeDGPresentationPreferences.supportedSizes(category)) {
				control.addItem(size);
			}
			control.addActionListener(event -> apply(category, control));
			GridBagConstraints labelConstraints = constraints(0, row);
			labelConstraints.anchor = GridBagConstraints.LINE_START;
			add(label, labelConstraints);
			GridBagConstraints controlConstraints = constraints(1, row++);
			controlConstraints.anchor = GridBagConstraints.LINE_START;
			controlConstraints.weightx = 1;
			add(control, controlConstraints);
			labels.put(category, label);
			controls.put(category, control);
		}
		setLabels();
		updateGUI();
	}

	private static GridBagConstraints constraints(int column, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = column;
		constraints.gridy = row;
		constraints.insets = new Insets(2, column == 0 ? 4 : 8, 2, 4);
		return constraints;
	}

	private void apply(Category category, JComboBox<Integer> control) {
		if (!updating && control.getSelectedItem() instanceof Integer size) {
			app.setPresentationSize(category, size);
		}
	}

	@Override
	public void setLabels() {
		labels.get(Category.GENERAL_UI_FONT)
				.setText(text("Presentation.GeneralUIFont") + ":");
		labels.get(Category.MENU_FONT).setText(text("Presentation.MenuFont") + ":");
		labels.get(Category.TOOLBAR_ICON).setText(text("Presentation.ToolbarIcon") + ":");
		labels.get(Category.ALGEBRA_FONT).setText(text("Presentation.AlgebraFont") + ":");
		labels.get(Category.CONSTRUCTION_PROTOCOL_FONT)
				.setText(text("Presentation.ConstructionProtocolFont") + ":");
		labels.get(Category.GRAPHICS_FONT).setText(text("Presentation.GraphicsFont") + ":");
		setBorder(BorderFactory.createTitledBorder(text("Presentation.Sizes")));
	}

	private String text(String key) {
		return GeoCeDGProfile.getText(key, app.getLocale().getLanguage());
	}

	void selectSize(Category category, int size) {
		controls.get(category).setSelectedItem(size);
	}

	int selectedSize(Category category) {
		return (Integer) controls.get(category).getSelectedItem();
	}

	@Override
	public void updateGUI() {
		updating = true;
		try {
			controls.forEach((category, control) ->
					control.setSelectedItem(app.getPresentationSize(category)));
		} finally {
			updating = false;
		}
	}

	@Override
	public JPanel getWrappedPanel() {
		return this;
	}

	@Override
	public void applyModifications() {
		// Every valid selection applies and persists live.
	}

	@Override
	public void updateFont() {
		Font font = app.getPlainFont();
		setFont(font);
		labels.values().forEach(label -> label.setFont(font));
		controls.values().forEach(control -> control.setFont(font));
	}

	@Override
	public void setSelected(boolean flag) {
		if (flag) {
			updateGUI();
		}
	}

}
