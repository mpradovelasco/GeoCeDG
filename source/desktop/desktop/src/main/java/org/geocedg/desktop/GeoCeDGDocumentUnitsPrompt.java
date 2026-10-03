/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.geocedg.common.kernel.units.UnitToken;

/**
 * PRE-G9B-R6-plus-D1 single Document Units dialog (DQ-D1-1). It only collects a
 * request; validation and the one undoable document operation belong to the caller.
 * Tests inject a lambda.
 */
interface GeoCeDGDocumentUnitsPrompt {

	/**
	 * @param app application
	 * @param initial fields of the current document state
	 * @return the requested fields, or {@code null} when cancelled
	 */
	GeoCeDGDocumentUnits.Request ask(AppGeoCeDG app, GeoCeDGDocumentUnits.Request initial);

	/** Construction choices in display order; null is unspecified. */
	UnitToken[] CONSTRUCTION_CHOICES = {null, UnitToken.MM, UnitToken.CM, UnitToken.M,
			UnitToken.USM};

	/** Presentation choices in display order; null follows the construction unit. */
	UnitToken[] PRESENTATION_CHOICES = {null, UnitToken.MM, UnitToken.CM, UnitToken.M,
			UnitToken.USM};

	/**
	 * @return the Swing dialog
	 */
	static GeoCeDGDocumentUnitsPrompt dialog() {
		return (app, initial) -> {
			String language = app.getLocale().getLanguage();
			JComboBox<String> construction = new JComboBox<>();
			for (UnitToken unit : CONSTRUCTION_CHOICES) {
				construction.addItem(unit == null
						? GeoCeDGProfile.getText("Units.Unspecified", language) : unit.token());
			}
			JComboBox<String> presentation = new JComboBox<>();
			for (UnitToken unit : PRESENTATION_CHOICES) {
				presentation.addItem(unit == null ? GeoCeDGProfile.getText(
						"Units.Dialog.FollowsConstruction", language) : unit.token());
			}
			construction.setSelectedIndex(indexOf(CONSTRUCTION_CHOICES, initial.construction));
			presentation.setSelectedIndex(indexOf(PRESENTATION_CHOICES, initial.presentation));
			JCheckBox define = new JCheckBox(
					GeoCeDGProfile.getText("Units.Dialog.UsmDefine", language),
					initial.usmDefined);
			JTextField factor = new JTextField(initial.factor, 16);
			JTextField name = new JTextField(initial.name, 16);
			JTextField symbol = new JTextField(initial.symbol, 16);
			Runnable enable = () -> {
				presentation.setEnabled(construction.getSelectedIndex() != 0);
				factor.setEnabled(define.isSelected());
				name.setEnabled(define.isSelected());
				symbol.setEnabled(define.isSelected());
			};
			construction.addActionListener(event -> enable.run());
			define.addActionListener(event -> enable.run());
			enable.run();

			JPanel panel = new JPanel(new GridBagLayout());
			row(panel, 0, new JLabel(GeoCeDGProfile.getText("Units.Dialog.Construction",
					language)), construction);
			row(panel, 1, new JLabel(GeoCeDGProfile.getText("Units.Dialog.Presentation",
					language)), presentation);
			row(panel, 2, define, null);
			row(panel, 3, new JLabel(GeoCeDGProfile.getText("Units.Dialog.UsmFactor",
					language)), factor);
			row(panel, 4, new JLabel(GeoCeDGProfile.getText("Units.Dialog.UsmName",
					language)), name);
			row(panel, 5, new JLabel(GeoCeDGProfile.getText("Units.Dialog.UsmSymbol",
					language)), symbol);
			row(panel, 6, new JLabel(GeoCeDGProfile.getText("Units.Dialog.Hint", language)),
					null);
			int choice = JOptionPane.showConfirmDialog(app.getMainComponent(), panel,
					GeoCeDGProfile.getText("Units.Dialog.Title", language),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (choice != JOptionPane.OK_OPTION) {
				return null;
			}
			UnitToken constructionUnit = CONSTRUCTION_CHOICES[construction.getSelectedIndex()];
			// section 7.1: an unspecified construction unit removes the presentation unit
			UnitToken presentationUnit = constructionUnit == null ? null
					: PRESENTATION_CHOICES[presentation.getSelectedIndex()];
			return new GeoCeDGDocumentUnits.Request(constructionUnit, presentationUnit,
					define.isSelected(), factor.getText(), name.getText(), symbol.getText());
		};
	}

	private static int indexOf(UnitToken[] choices, UnitToken unit) {
		for (int i = 0; i < choices.length; i++) {
			if (choices[i] == unit) {
				return i;
			}
		}
		return 0;
	}

	private static void row(JPanel panel, int row, java.awt.Component label,
			java.awt.Component control) {
		GridBagConstraints left = new GridBagConstraints();
		left.gridx = 0;
		left.gridy = row;
		left.anchor = GridBagConstraints.LINE_START;
		left.insets = new Insets(2, 4, 2, 4);
		if (control == null) {
			left.gridwidth = 2;
		}
		panel.add(label, left);
		if (control != null) {
			GridBagConstraints right = new GridBagConstraints();
			right.gridx = 1;
			right.gridy = row;
			right.weightx = 1;
			right.fill = GridBagConstraints.HORIZONTAL;
			right.insets = new Insets(2, 8, 2, 4);
			panel.add(control, right);
		}
	}
}
