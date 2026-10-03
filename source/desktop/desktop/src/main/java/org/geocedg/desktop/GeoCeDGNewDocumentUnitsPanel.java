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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geogebra.common.gui.SetLabels;
import org.geogebra.desktop.gui.dialog.options.OptionPanelD;

/**
 * PRE-G9B-R6-plus-D1 New document units preference panel (DQ-D1-2). It stores
 * defaults only for future blank documents; a change never alters the current
 * document's unit state, XML, undo stack or saved state.
 */
final class GeoCeDGNewDocumentUnitsPanel extends JPanel implements OptionPanelD, SetLabels {
	private static final long serialVersionUID = 1L;

	private final AppGeoCeDG app;
	private final transient GeoCeDGUnitPreferences preferences;
	private final JLabel constructionLabel = new JLabel();
	private final JLabel presentationLabel = new JLabel();
	private final JLabel hint = new JLabel();
	private final JComboBox<String> construction = new JComboBox<>();
	private final JComboBox<String> presentation = new JComboBox<>();
	private boolean updating;

	GeoCeDGNewDocumentUnitsPanel(AppGeoCeDG app, GeoCeDGUnitPreferences preferences) {
		this.app = app;
		this.preferences = preferences;
		setLayout(new GridBagLayout());
		add(constructionLabel, constraints(0, 0));
		add(construction, wide(1, 0));
		add(presentationLabel, constraints(0, 1));
		add(presentation, wide(1, 1));
		GridBagConstraints hintConstraints = constraints(0, 2);
		hintConstraints.gridwidth = 2;
		add(hint, hintConstraints);
		construction.addActionListener(event -> apply());
		presentation.addActionListener(event -> apply());
		setLabels();
		updateGUI();
	}

	private static GridBagConstraints constraints(int column, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = column;
		constraints.gridy = row;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.insets = new Insets(2, column == 0 ? 4 : 8, 2, 4);
		return constraints;
	}

	private static GridBagConstraints wide(int column, int row) {
		GridBagConstraints constraints = constraints(column, row);
		constraints.weightx = 1;
		return constraints;
	}

	private void apply() {
		if (updating || construction.getSelectedIndex() < 0
				|| presentation.getSelectedIndex() < 0) {
			return;
		}
		preferences.setConstruction(GeoCeDGUnitPreferences.ConstructionDefault.values()[
				construction.getSelectedIndex()]);
		preferences.setPresentation(GeoCeDGUnitPreferences.PresentationDefault.values()[
				presentation.getSelectedIndex()]);
	}

	@Override
	public void setLabels() {
		updating = true;
		try {
			constructionLabel.setText(text("Units.Preferences.Construction"));
			presentationLabel.setText(text("Units.Preferences.Presentation"));
			hint.setText(text("Units.Preferences.Hint"));
			setBorder(BorderFactory.createTitledBorder(text("Units.Preferences.Group")));
			construction.removeAllItems();
			for (GeoCeDGUnitPreferences.ConstructionDefault value
					: GeoCeDGUnitPreferences.ConstructionDefault.values()) {
				construction.addItem(value.unit == null ? text("Units.Unspecified")
						: value.unit.token());
			}
			presentation.removeAllItems();
			for (GeoCeDGUnitPreferences.PresentationDefault value
					: GeoCeDGUnitPreferences.PresentationDefault.values()) {
				presentation.addItem(value.unit == null
						? text("Units.Dialog.FollowsConstruction") : value.unit.token());
			}
		} finally {
			updating = false;
		}
		updateGUI();
	}

	private String text(String key) {
		return GeoCeDGProfile.getText(key, app.getLocale().getLanguage());
	}

	@Override
	public void updateGUI() {
		updating = true;
		try {
			construction.setSelectedIndex(preferences.construction().ordinal());
			presentation.setSelectedIndex(preferences.presentation().ordinal());
		} finally {
			updating = false;
		}
	}

	JComboBox<String> constructionControl() {
		return construction;
	}

	JComboBox<String> presentationControl() {
		return presentation;
	}

	@Override
	public JPanel getWrappedPanel() {
		return this;
	}

	@Override
	public void applyModifications() {
		// The selection applies and persists live, like every product preference.
	}

	@Override
	public void updateFont() {
		Font font = app.getPlainFont();
		setFont(font);
		constructionLabel.setFont(font);
		presentationLabel.setFont(font);
		hint.setFont(font);
		construction.setFont(font);
		presentation.setFont(font);
	}

	@Override
	public void setSelected(boolean flag) {
		if (flag) {
			updateGUI();
		}
	}
}
