/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.geocedg.common.importer.dxf.DxfAffineTransform;
import org.geocedg.common.importer.dxf.DxfImportReport;
import org.geocedg.common.importer.dxf.DxfSplineReference;

/**
 * Swing projections of the DXF reference import: file chooser, units and transformation
 * dialog, read-only text window and the session manager. Texts come from the product
 * profile ({@code localized_text}). Thin by design: the behaviour lives in
 * {@link GeoCeDGDxfReferenceImport} and {@link GeoCeDGDxfReferenceSession}.
 */
final class GeoCeDGDxfReferenceDialogs {

	private GeoCeDGDxfReferenceDialogs() {
	}

	static @CheckForNull File chooseFile(AppGeoCeDG app) {
		JFileChooser chooser = new JFileChooser(app.getCurrentPath());
		chooser.setDialogTitle(app.layerText("DxfReference.Chooser.Title"));
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.setFileFilter(new FileNameExtensionFilter(
				app.layerText("DxfReference.Chooser.Filter"), "dxf"));
		return chooser.showOpenDialog(app.getMainComponent()) == JFileChooser.APPROVE_OPTION
				? chooser.getSelectedFile() : null;
	}

	/**
	 * Shows the recorded units and asks for the transformation: identity (default), uniform
	 * scale or affine. A singular or nonfinite choice is refused with SINGULAR_TRANSFORM.
	 */
	static @CheckForNull DxfAffineTransform askTransform(AppGeoCeDG app,
			DxfImportReport preview) {
		JPanel panel = new JPanel(new BorderLayout(6, 6));
		String units = app.layerText("DxfReference.Transform.Units",
				preview.file().insUnitsName(), preview.file().documentUnit());
		if (preview.file().unitsDiffer()) {
			units += "\n" + app.layerText("DxfReference.Transform.UnitsDiffer");
		}
		JTextArea unitsText = new JTextArea(units, 3, 48);
		unitsText.setEditable(false);
		unitsText.setLineWrap(true);
		unitsText.setWrapStyleWord(true);
		panel.add(unitsText, BorderLayout.NORTH);
		JRadioButton identity = new JRadioButton(app.layerText(
				"DxfReference.Transform.Identity"), true);
		JRadioButton scale = new JRadioButton(app.layerText("DxfReference.Transform.Scale"));
		JRadioButton affine = new JRadioButton(app.layerText("DxfReference.Transform.Affine"));
		ButtonGroup group = new ButtonGroup();
		group.add(identity);
		group.add(scale);
		group.add(affine);
		JTextField factor = new JTextField("1", 8);
		JTextField[] coefficients = new JTextField[6];
		String[] defaults = {"1", "0", "0", "1", "0", "0"};
		JPanel affinePanel = new JPanel(new GridLayout(2, 6, 4, 2));
		String[] names = {"a", "b", "c", "d", "e", "f"};
		for (int i = 0; i < 6; i++) {
			affinePanel.add(new JLabel(names[i]));
		}
		for (int i = 0; i < 6; i++) {
			coefficients[i] = new JTextField(defaults[i], 6);
			affinePanel.add(coefficients[i]);
		}
		JPanel choices = new JPanel(new GridLayout(0, 1, 2, 2));
		choices.add(identity);
		JPanel scaleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		scaleRow.add(scale);
		scaleRow.add(factor);
		choices.add(scaleRow);
		choices.add(affine);
		choices.add(affinePanel);
		panel.add(choices, BorderLayout.CENTER);
		int answer = JOptionPane.showConfirmDialog(app.getMainComponent(), panel,
				app.layerText("DxfReference.Transform.Title"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (answer != JOptionPane.OK_OPTION) {
			return null;
		}
		DxfAffineTransform result;
		try {
			if (identity.isSelected()) {
				result = DxfAffineTransform.IDENTITY;
			} else if (scale.isSelected()) {
				result = DxfAffineTransform.scale(Double.parseDouble(factor.getText().trim()));
			} else {
				double[] values = new double[6];
				for (int i = 0; i < 6; i++) {
					values[i] = Double.parseDouble(coefficients[i].getText().trim());
				}
				result = DxfAffineTransform.of(values[0], values[1], values[2], values[3],
						values[4], values[5]);
			}
		} catch (NumberFormatException exception) {
			result = null;
		}
		if (result == null) {
			showText(app, app.layerText("DxfReference.Transform.Title"),
					app.layerText("DxfReference.Transform.Singular"));
		}
		return result;
	}

	static void showText(AppGeoCeDG app, String title, String text) {
		JTextArea area = new JTextArea(text, 18, 72);
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setFont(app.getPlainFont());
		area.setCaretPosition(0);
		JOptionPane.showMessageDialog(app.getMainComponent(), new JScrollPane(area), title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	/** Modal session manager: list, inspect (read-only), show/hide, remove, import, close. */
	static void openManager(AppGeoCeDG app) {
		JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(app.getMainComponent()),
				app.layerText("DxfReference.Title"), Dialog.ModalityType.APPLICATION_MODAL);
		DefaultListModel<String> model = new DefaultListModel<>();
		JList<String> list = new JList<>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		Runnable refill = () -> {
			model.clear();
			List<GeoCeDGDxfReferenceSession.Entry> entries = app.getDxfReferenceSession()
					.entries();
			for (GeoCeDGDxfReferenceSession.Entry entry : entries) {
				DxfSplineReference record = entry.reference();
				model.addElement(record.file().fileBaseName() + " #" + record.ordinal() + " "
						+ record.handle() + " - " + record.nature().name()
						+ (entry.isVisible() ? "" : " (" + app.layerText(
								"DxfReference.Hidden") + ")"));
			}
			if (entries.isEmpty()) {
				model.addElement(app.layerText("DxfReference.Empty"));
			}
		};
		refill.run();
		JButton importButton = new JButton(app.layerText("DxfReference.Import"));
		JButton inspect = new JButton(app.layerText("DxfReference.Inspect"));
		JButton visibility = new JButton(app.layerText("DxfReference.ToggleVisibility"));
		JButton remove = new JButton(app.layerText("DxfReference.Remove"));
		JButton close = new JButton(app.layerText("DxfReference.Close"));
		importButton.addActionListener(event -> {
			File file = chooseFile(app);
			if (file != null) {
				new GeoCeDGDxfReferenceImport(app).importFile(file);
			}
			refill.run();
		});
		inspect.addActionListener(event -> {
			int index = list.getSelectedIndex();
			if (index >= 0 && index < app.getDxfReferenceSession().size()) {
				showText(app, app.layerText("DxfReference.Inspector.Title"),
						GeoCeDGDxfReferenceText.inspector(app,
								app.getDxfReferenceSession().entries().get(index)));
			}
		});
		visibility.addActionListener(event -> {
			int index = list.getSelectedIndex();
			if (index >= 0 && index < app.getDxfReferenceSession().size()) {
				app.getDxfReferenceSession().setVisible(index,
						!app.getDxfReferenceSession().entries().get(index).isVisible());
				refill.run();
				list.setSelectedIndex(index);
			}
		});
		remove.addActionListener(event -> {
			int index = list.getSelectedIndex();
			if (index >= 0 && index < app.getDxfReferenceSession().size()) {
				app.getDxfReferenceSession().remove(index);
				refill.run();
			}
		});
		close.addActionListener(event -> dialog.dispose());
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(importButton);
		buttons.add(inspect);
		buttons.add(visibility);
		buttons.add(remove);
		buttons.add(close);
		JPanel content = new JPanel(new BorderLayout(6, 6));
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		content.add(new JLabel(app.layerText("DxfReference.SessionOnlyHint")),
				BorderLayout.NORTH);
		content.add(new JScrollPane(list), BorderLayout.CENTER);
		content.add(buttons, BorderLayout.SOUTH);
		dialog.setContentPane(content);
		dialog.pack();
		dialog.setLocationRelativeTo(app.getMainComponent());
		dialog.setVisible(true);
	}
}
