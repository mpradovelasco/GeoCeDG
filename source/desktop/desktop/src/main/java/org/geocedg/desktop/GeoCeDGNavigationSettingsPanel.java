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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.geocedg.desktop.GeoCeDGNavigationShortcutPreferences.DraftValidation;
import org.geocedg.desktop.GeoCeDGNavigationShortcutPreferences.ShortcutStatus;
import org.geocedg.desktop.GeoCeDGNavigationShortcutPreferences.ShortcutValidation;

/** Editable, non-authoritative presentation of the bounded A7 preferences. */
final class GeoCeDGNavigationSettingsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	static final String DRAFT_VALID_PROPERTY = "geocedg.navigation.draft.valid";
	private static final Color VALID_COLOR = new Color(0, 112, 44);
	private final GeoCeDGActionRegistry registry;
	private final GeoCeDGNavigationShortcutPreferences preferences;
	private final JTextField factor = new JTextField(8);
	private final ShortcutField zoomIn;
	private final ShortcutField zoomOut;
	private final JLabel factorValidation = new JLabel(" ");
	private final JLabel zoomInValidation = new JLabel(" ");
	private final JLabel zoomOutValidation = new JLabel(" ");
	private DraftValidation draftValidation;
	private boolean loading;

	GeoCeDGNavigationSettingsPanel(GeoCeDGActionRegistry registry) {
		this.registry = registry;
		preferences = registry.getNavigationShortcuts();
		zoomIn = new ShortcutField(this::validateDraft);
		zoomOut = new ShortcutField(this::validateDraft);
		setLayout(new GridBagLayout());
		addRow(0, "Navigation.ZoomFactor", factor, factorValidation);
		addRow(1, "Navigation.ZoomFactorIn", zoomIn, zoomInValidation);
		addRow(2, "Navigation.ZoomFactorOut", zoomOut, zoomOutValidation);
		factor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent event) {
				validateDraft();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				validateDraft();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				validateDraft();
			}
		});
		load(preferences.getConfiguration());
	}

	private void addRow(int row, String key,
			JTextField field, JLabel status) {
		int editorRow = row * 2;
		GridBagConstraints labelConstraints = constraints(0, editorRow);
		labelConstraints.fill = GridBagConstraints.NONE;
		add(new JLabel(registry.text(key)), labelConstraints);

		GridBagConstraints editorConstraints = constraints(1, editorRow);
		editorConstraints.weightx = 1;
		add(field, editorConstraints);
		field.setMinimumSize(field.getPreferredSize());

		GridBagConstraints statusConstraints = constraints(1, editorRow + 1);
		statusConstraints.gridwidth = 2;
		statusConstraints.weightx = 1;
		statusConstraints.insets = new Insets(0, 4, 5, 4);
		add(status, statusConstraints);
	}

	private static GridBagConstraints constraints(int column, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = column;
		constraints.gridy = row;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(3, 4, 3, 4);
		return constraints;
	}

	boolean applyConfiguration() {
		GeoCeDGNavigationShortcutPreferences.Result result = preferences.setConfiguration(
				factor.getText(), zoomIn.getStroke(), zoomOut.getStroke());
		validateDraft();
		return result == GeoCeDGNavigationShortcutPreferences.Result.ACCEPTED;
	}

	void showDefaults() {
		load(new GeoCeDGNavigationShortcutPreferences.Configuration(
				GeoCeDGNavigationShortcutPreferences.DEFAULT_FACTOR, null, null));
	}

	void setDraft(double proposedFactor, KeyStroke proposedIn, KeyStroke proposedOut) {
		load(new GeoCeDGNavigationShortcutPreferences.Configuration(
				proposedFactor, proposedIn, proposedOut));
	}

	boolean hasValidationError() {
		return !isDraftValid();
	}

	boolean isDraftValid() {
		return draftValidation != null && draftValidation.isValid();
	}

	String zoomInValidationText() {
		return zoomInValidation.getText();
	}

	String zoomOutValidationText() {
		return zoomOutValidation.getText();
	}

	String zoomInConflictActionId() {
		return draftValidation == null ? null
				: draftValidation.zoomIn().conflictingActionId();
	}

	private void load(GeoCeDGNavigationShortcutPreferences.Configuration configuration) {
		loading = true;
		factor.setText(Double.toString(configuration.factor()));
		zoomIn.setStroke(configuration.zoomIn());
		zoomOut.setStroke(configuration.zoomOut());
		loading = false;
		validateDraft();
	}

	private void validateDraft() {
		if (loading) {
			return;
		}
		boolean oldValid = isDraftValid();
		draftValidation = preferences.validateConfiguration(factor.getText(),
				zoomIn.getStroke(), zoomOut.getStroke());
		showFactorValidation(draftValidation.factorValid());
		showShortcutValidation(zoomInValidation, draftValidation.zoomIn());
		showShortcutValidation(zoomOutValidation, draftValidation.zoomOut());
		firePropertyChange(DRAFT_VALID_PROPERTY, oldValid, isDraftValid());
	}

	private void showFactorValidation(boolean valid) {
		factorValidation.setForeground(valid ? VALID_COLOR : Color.RED.darker());
		factorValidation.setText(registry.text(valid
				? "Navigation.ZoomFactor.Valid" : "Navigation.ZoomFactor.Invalid"));
	}

	private void showShortcutValidation(JLabel label, ShortcutValidation validation) {
		ShortcutStatus status = validation.status();
		label.setForeground(status == ShortcutStatus.AVAILABLE
				|| status == ShortcutStatus.UNASSIGNED ? VALID_COLOR : Color.RED.darker());
		switch (status) {
		case AVAILABLE:
			label.setText(registry.text("Navigation.Shortcut.Available"));
			break;
		case UNASSIGNED:
			label.setText(registry.text("Navigation.Shortcut.Unassigned"));
			break;
		case INVALID:
			label.setText(registry.text("Navigation.Shortcut.Invalid"));
			break;
		case CONFLICT:
			label.setText(registry.text("Navigation.Shortcut.ConflictWith") + " "
					+ conflictPresentation(validation.conflictingActionId()));
			break;
		default:
			throw new IllegalStateException("Unhandled navigation shortcut status");
		}
	}

	private String conflictPresentation(String id) {
		return id != null && registry.ids().contains(id)
				? String.valueOf(registry.get(id).getValue(javax.swing.Action.NAME)) : id;
	}

	private static final class ShortcutField extends JTextField {
		private static final long serialVersionUID = 1L;
		private final Runnable changed;
		private KeyStroke stroke;

		ShortcutField(Runnable changed) {
			super(20);
			this.changed = changed;
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
			changed.run();
		}
	}
}
