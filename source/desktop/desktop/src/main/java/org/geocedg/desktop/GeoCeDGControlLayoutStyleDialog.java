/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.geocedg.common.document.controls.ControlLayout;
import org.geocedg.common.document.controls.ControlLayout.Panel;
import org.geocedg.common.document.controls.ControlLayoutException;
import org.geogebra.desktop.gui.color.GeoGebraColorChooser;

/** Modal editor of one temporary appearance. The workspace owns publication and history. */
final class GeoCeDGControlLayoutStyleDialog {
	private final AppGeoCeDG app;
	private final Panel original;
	private final Consumer<Panel> preview;
	private final JDialog dialog;
	private final JTextField name;
	private final JLabel nameStatus = new JLabel();
	private final GeoGebraColorChooser color;
	private final JSlider transparency;
	private final JLabel transparencyLabel = new JLabel();
	private final NumberFormat percent;
	private final JButton accept;
	private Panel draft;
	private Panel result;

	GeoCeDGControlLayoutStyleDialog(AppGeoCeDG app, Window owner, Panel original,
			Consumer<Panel> preview) {
		this.app = app;
		this.original = original;
		this.preview = preview;
		percent = NumberFormat.getPercentInstance(app.getLocale());
		dialog = new JDialog(owner, text("Appearance"), Dialog.ModalityType.DOCUMENT_MODAL);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		name = new JTextField(original.name(), 28);
		color = new GeoGebraColorChooser(app);
		color.setColor(new Color(original.rgb()));
		color.updateFonts();
		color.getAccessibleContext().setAccessibleName(text("Color"));
		transparency = new JSlider(100 - ControlLayout.MAX_OPACITY,
				100 - ControlLayout.MIN_OPACITY, 100 - original.opacity());
		transparency.getAccessibleContext().setAccessibleName(text("Transparency"));
		accept = new JButton(app.getLocalization().getMenu("OK"));
		accept.addActionListener(event -> {
			result = draft;
			close();
		});
		JButton cancel = new JButton(app.getLocalization().getMenu("Cancel"));
		cancel.addActionListener(event -> close());
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttons.add(accept);
		buttons.add(cancel);

		JPanel fields = new JPanel(new GridLayout(0, 1, 2, 2));
		JLabel nameLabel = new JLabel(text("Name"));
		nameLabel.setLabelFor(name);
		fields.add(nameLabel);
		fields.add(name);
		fields.add(nameStatus);
		transparencyLabel.setLabelFor(transparency);
		fields.add(transparencyLabel);
		fields.add(transparency);
		JPanel chooser = new JPanel(new BorderLayout());
		chooser.setBorder(BorderFactory.createTitledBorder(text("Color")));
		chooser.add(color, BorderLayout.CENTER);
		JPanel content = new JPanel(new BorderLayout(8, 8));
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		content.add(fields, BorderLayout.NORTH);
		content.add(chooser, BorderLayout.CENTER);
		content.add(buttons, BorderLayout.SOUTH);
		dialog.setContentPane(content);
		dialog.getRootPane().setDefaultButton(accept);
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelPanelAppearance");
		dialog.getRootPane().getActionMap().put("cancelPanelAppearance", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				close();
			}
		});
		name.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent event) {
				refreshDraft();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				refreshDraft();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				refreshDraft();
			}
		});
		color.getSelectionModel().addChangeListener(event -> refreshDraft());
		transparency.addChangeListener(event -> refreshDraft());
	}

	private void refreshDraft() {
		int value = transparency.getValue();
		transparencyLabel.setText(text("Transparency") + " " + percent.format(value / 100.0));
		try {
			draft = original.withStyle(name.getText(), color.getColor().getRGB() & 0xffffff,
					100 - value);
			nameStatus.setText(text("DefaultNameHint"));
		} catch (ControlLayoutException invalidName) {
			draft = null;
			nameStatus.setText(app.layerText("ControlLayout.InvalidName",
					Integer.toString(ControlLayout.MAX_NAME_CODE_POINTS)));
		}
		accept.setEnabled(draft != null);
		preview.accept(draft);
	}

	Panel open() {
		refreshDraft();
		dialog.pack();
		dialog.setLocationRelativeTo(dialog.getOwner());
		SwingUtilities.invokeLater(name::requestFocusInWindow);
		dialog.setVisible(true);
		return result;
	}

	boolean isEditing(Panel panel) {
		return original == panel;
	}

	void close() {
		dialog.dispose();
	}

	private String text(String key) {
		return app.layerText("ControlLayout." + key);
	}
}
