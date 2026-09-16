/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.geogebra.common.gui.SetLabels;
import org.geogebra.desktop.gui.dialog.options.OptionPanelD;

/** Closed three-preset selector for the GeoCeDG application presentation theme. */
final class GeoCeDGThemeOptionsPanel extends JPanel implements OptionPanelD, SetLabels {
	private static final long serialVersionUID = 1L;

	private final AppGeoCeDG app;
	private final JLabel label = new JLabel();
	private final JComboBox<GeoCeDGPresentationTheme> control = new JComboBox<>();
	private boolean updating;

	GeoCeDGThemeOptionsPanel(AppGeoCeDG app) {
		this.app = app;
		setLayout(new GridBagLayout());
		for (GeoCeDGPresentationTheme theme : GeoCeDGPresentationTheme.values()) {
			control.addItem(theme);
		}
		control.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public java.awt.Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean selected, boolean focused) {
				super.getListCellRendererComponent(list, value, index, selected, focused);
				if (value instanceof GeoCeDGPresentationTheme theme) {
					setText(text(theme.textKey()));
				}
				return this;
			}
		});
		control.addActionListener(event -> apply());
		add(label, constraints(0));
		GridBagConstraints controlConstraints = constraints(1);
		controlConstraints.weightx = 1;
		add(control, controlConstraints);
		setLabels();
		updateGUI();
	}

	private static GridBagConstraints constraints(int column) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = column;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.insets = new Insets(2, column == 0 ? 4 : 8, 2, 4);
		return constraints;
	}

	private void apply() {
		if (!updating
				&& control.getSelectedItem() instanceof GeoCeDGPresentationTheme theme) {
			app.setPresentationTheme(theme);
		}
	}

	@Override
	public void setLabels() {
		label.setText(text("Presentation.Theme") + ":");
		setBorder(BorderFactory.createTitledBorder(text("Presentation.ThemeGroup")));
		control.repaint();
	}

	private String text(String key) {
		return GeoCeDGProfile.getText(key, app.getLocale().getLanguage());
	}

	GeoCeDGPresentationTheme selectedTheme() {
		return (GeoCeDGPresentationTheme) control.getSelectedItem();
	}

	void selectTheme(GeoCeDGPresentationTheme theme) {
		control.setSelectedItem(theme);
	}

	@Override
	public void updateGUI() {
		updating = true;
		try {
			control.setSelectedItem(app.getPresentationTheme());
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
		// The selection applies and persists live, like every presentation preference.
	}

	@Override
	public void updateFont() {
		Font font = app.getPlainFont();
		setFont(font);
		label.setFont(font);
		control.setFont(font);
	}

	@Override
	public void setSelected(boolean flag) {
		if (flag) {
			updateGUI();
		}
	}
}
