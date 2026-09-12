/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/** Editable, non-authoritative presentation of the bounded A7 preferences. */
final class GeoCeDGNavigationSettingsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final GeoCeDGActionRegistry registry;
	private final GeoCeDGNavigationShortcutPreferences preferences;
	private final JTextField factor = new JTextField(8);
	private final ShortcutField zoomIn = new ShortcutField();
	private final ShortcutField zoomOut = new ShortcutField();
	private final JLabel validation = new JLabel(" ");

	GeoCeDGNavigationSettingsPanel(GeoCeDGActionRegistry registry) {
		this.registry = registry;
		preferences = registry.getNavigationShortcuts();
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(3, 4, 3, 4);
		addRow(constraints, 0, "Navigation.ZoomFactor", factor);
		addRow(constraints, 1, "Navigation.ZoomFactorIn", zoomIn);
		addRow(constraints, 2, "Navigation.ZoomFactorOut", zoomOut);
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 2;
		validation.setForeground(Color.RED.darker());
		add(validation, constraints);
		load(preferences.getConfiguration());
	}

	private void addRow(GridBagConstraints constraints, int row, String key,
			JTextField field) {
		constraints.gridx = 0;
		constraints.gridy = row;
		constraints.gridwidth = 1;
		add(new JLabel(registry.text(key)), constraints);
		constraints.gridx = 1;
		add(field, constraints);
	}

	boolean applyConfiguration() {
		double proposedFactor;
		try {
			proposedFactor = Double.parseDouble(factor.getText().trim());
		} catch (NumberFormatException exception) {
			showResult(GeoCeDGNavigationShortcutPreferences.Result.INVALID_FACTOR);
			return false;
		}
		GeoCeDGNavigationShortcutPreferences.Result result = preferences.setConfiguration(
				proposedFactor, zoomIn.getStroke(), zoomOut.getStroke());
		showResult(result);
		return result == GeoCeDGNavigationShortcutPreferences.Result.ACCEPTED;
	}

	void showDefaults() {
		load(new GeoCeDGNavigationShortcutPreferences.Configuration(
				GeoCeDGNavigationShortcutPreferences.DEFAULT_FACTOR, null, null));
		validation.setText(" ");
	}

	void setDraft(double proposedFactor, KeyStroke proposedIn, KeyStroke proposedOut) {
		load(new GeoCeDGNavigationShortcutPreferences.Configuration(
				proposedFactor, proposedIn, proposedOut));
	}

	boolean hasValidationError() {
		return !validation.getText().isBlank();
	}

	private void load(GeoCeDGNavigationShortcutPreferences.Configuration configuration) {
		factor.setText(Double.toString(configuration.factor()));
		zoomIn.setStroke(configuration.zoomIn());
		zoomOut.setStroke(configuration.zoomOut());
	}

	private void showResult(GeoCeDGNavigationShortcutPreferences.Result result) {
		switch (result) {
		case INVALID_FACTOR:
			validation.setText(registry.text("Navigation.ZoomFactor.Invalid"));
			break;
		case INVALID_SHORTCUT:
			validation.setText(registry.text("Navigation.Shortcut.Invalid"));
			break;
		case CONFLICT:
			validation.setText(registry.text("Navigation.Shortcut.Conflict"));
			break;
		case ACCEPTED:
			validation.setText(" ");
			break;
		default:
			throw new IllegalStateException("Unhandled navigation preference result");
		}
	}

	private static final class ShortcutField extends JTextField {
		private static final long serialVersionUID = 1L;
		private KeyStroke stroke;

		ShortcutField() {
			super(20);
			setEditable(false);
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent event) {
					if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE
							|| event.getKeyCode() == KeyEvent.VK_DELETE) {
						setStroke(null);
					} else {
						setStroke(KeyStroke.getKeyStrokeForEvent(event));
					}
					event.consume();
				}
			});
		}

		KeyStroke getStroke() {
			return stroke;
		}

		void setStroke(KeyStroke value) {
			stroke = value;
			setText(value == null ? "" : value.toString());
		}
	}
}
