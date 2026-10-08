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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geogebra.common.gui.SetLabels;
import org.geogebra.desktop.gui.dialog.options.OptionPanelD;

/**
 * PRE-G9B-R6-plus-E2 smoke follow-up B1/B2: Layout &amp; Presentation group "Dimension
 * presentation". It stores creation preferences only: the initial line thickness of
 * dimensions created with the two tools, and whether new documents show the dimension
 * unit suffix. A change never alters the current document, its dimensions, XML, undo
 * stack or saved state; the current document's suffix policy is set in Document units.
 */
final class GeoCeDGDimensionPresentationPanel extends JPanel
		implements OptionPanelD, SetLabels {
	private static final long serialVersionUID = 1L;

	private final AppGeoCeDG app;
	private final transient GeoCeDGDimensionPreferences dimensions;
	private final transient GeoCeDGUnitPreferences units;
	private final JLabel thicknessLabel = new JLabel();
	private final JSpinner thickness = new JSpinner(new SpinnerNumberModel(
			AlgoNativeDimension.INITIAL_LINE_THICKNESS,
			GeoCeDGDimensionPreferences.MIN_LINE_THICKNESS,
			GeoCeDGDimensionPreferences.MAX_LINE_THICKNESS, 1));
	private final JCheckBox suffix = new JCheckBox();
	private final JLabel hint = new JLabel();
	private boolean updating;

	GeoCeDGDimensionPresentationPanel(AppGeoCeDG app, GeoCeDGDimensionPreferences dimensions,
			GeoCeDGUnitPreferences units) {
		this.app = app;
		this.dimensions = dimensions;
		this.units = units;
		setLayout(new GridBagLayout());
		add(thicknessLabel, constraints(0, 0));
		add(thickness, constraints(1, 0));
		GridBagConstraints suffixConstraints = constraints(0, 1);
		suffixConstraints.gridwidth = 2;
		add(suffix, suffixConstraints);
		GridBagConstraints hintConstraints = constraints(0, 2);
		hintConstraints.gridwidth = 2;
		add(hint, hintConstraints);
		thickness.addChangeListener(event -> apply());
		suffix.addActionListener(event -> apply());
		setLabels();
		updateGUI();
	}

	private static GridBagConstraints constraints(int column, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = column;
		constraints.gridy = row;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.insets = new Insets(2, column == 0 ? 4 : 8, 2, 4);
		constraints.weightx = column == 1 ? 1 : 0;
		return constraints;
	}

	private void apply() {
		if (updating) {
			return;
		}
		dimensions.setInitialLineThickness((Integer) thickness.getValue());
		units.setDimensionUnitSuffixShown(suffix.isSelected());
	}

	@Override
	public void setLabels() {
		setBorder(BorderFactory.createTitledBorder(text("Dimensions.Preferences.Group")));
		thicknessLabel.setText(text("Dimensions.Preferences.LineThickness"));
		suffix.setText(text("Dimensions.Preferences.UnitSuffix"));
		hint.setText(text("Dimensions.Preferences.Hint"));
	}

	private String text(String key) {
		return GeoCeDGProfile.getText(key, app.getLocale().getLanguage());
	}

	@Override
	public void updateGUI() {
		updating = true;
		try {
			thickness.setValue(dimensions.initialLineThickness());
			suffix.setSelected(units.dimensionUnitSuffixShown());
		} finally {
			updating = false;
		}
	}

	JSpinner thicknessControl() {
		return thickness;
	}

	JCheckBox suffixControl() {
		return suffix;
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
		thicknessLabel.setFont(font);
		thickness.setFont(font);
		suffix.setFont(font);
		hint.setFont(font);
	}

	@Override
	public void setSelected(boolean flag) {
		if (flag) {
			updateGUI();
		}
	}
}
