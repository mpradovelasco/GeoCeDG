/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.geocedg.common.kernel.sheet.IsoASheet;
import org.geocedg.desktop.export.DrawingScale;

/**
 * PRE-G9B-R6-plus-E3 interaction of the ISO A sheet tool and of the ISO A export-area
 * selection. Injectable so tests drive the workflow without modal dialogs; the Swing
 * implementation uses only existing option-pane patterns and profile texts.
 */
interface GeoCeDGIsoABorderPrompt {

	/** Local presets of the sheet dialog; the export dialogs' presets are unchanged. */
	String[] SCALE_PRESETS = {"1:1", "1:2", "1:5", "1:10", "1:20", "1:50", "1:100",
			"1:200", "1:500", "2:1", "5:1", "10:1"};

	/**
	 * Sheet choice of the dialog.
	 *
	 * @param index ISO A index 0 to 10
	 * @param landscape orientation
	 * @param scale captured drawing scale
	 * @param innerFrame inner-frame option
	 * @param showLabel whether the construction label is shown
	 */
	record SheetRequest(int index, boolean landscape, DrawingScale scale,
			boolean innerFrame, boolean showLabel) {
	}

	/**
	 * ISO A export-area choice.
	 *
	 * @param x upper-left corner x (world)
	 * @param y upper-left corner y (world)
	 * @param index ISO A index
	 * @param landscape orientation
	 */
	record SelectionRequest(double x, double y, int index, boolean landscape) {
	}

	/**
	 * @param app application
	 * @param initial initial values
	 * @return the confirmed, validated choice, or null when cancelled
	 */
	SheetRequest askSheet(AppGeoCeDG app, SheetRequest initial);

	/**
	 * @param app application
	 * @param initial initial values
	 * @return the confirmed choice, or null when cancelled or invalid
	 */
	SelectionRequest askSelection(AppGeoCeDG app, SelectionRequest initial);

	/**
	 * @param app application
	 * @param message question
	 * @param title title
	 * @return whether the user answered yes
	 */
	boolean confirm(AppGeoCeDG app, String message, String title);

	/**
	 * @param app application
	 * @param message message
	 * @param title title
	 * @param action label of an explicit action, or null
	 * @return whether the explicit action was chosen
	 */
	boolean inform(AppGeoCeDG app, String message, String title, String action);

	/** @return the Swing implementation */
	static GeoCeDGIsoABorderPrompt swing() {
		return new Swing();
	}

	/** Option-pane implementation. */
	final class Swing implements GeoCeDGIsoABorderPrompt {

		@Override
		public SheetRequest askSheet(AppGeoCeDG app, SheetRequest initial) {
			SheetRequest current = initial;
			while (true) {
				JComboBox<String> size = sizes(current.index());
				JRadioButton landscape = new JRadioButton(app.layerText("IsoA.Dialog.Landscape"),
						current.landscape());
				JRadioButton portrait = new JRadioButton(app.layerText("IsoA.Dialog.Portrait"),
						!current.landscape());
				ButtonGroup orientation = new ButtonGroup();
				orientation.add(landscape);
				orientation.add(portrait);
				JComboBox<String> scale = new JComboBox<>(SCALE_PRESETS);
				scale.setEditable(true);
				scale.setSelectedItem(current.scale().toString());
				JCheckBox frame = new JCheckBox(app.layerText("IsoA.Dialog.Frame"),
						current.innerFrame());
				JLabel frameReason = new JLabel(app.layerText("IsoA.Dialog.FrameImpossible"));
				final JCheckBox label = new JCheckBox(app.layerText("IsoA.Dialog.Label"),
						current.showLabel());
				Runnable sync = () -> {
					boolean possible = IsoASheet.isFramePossible(size.getSelectedIndex(),
							landscape.isSelected());
					frame.setEnabled(possible);
					frameReason.setVisible(!possible);
					if (!possible) {
						frame.setSelected(false);
					}
				};
				size.addActionListener(event -> {
					frame.setSelected(IsoASheet.isFrameDefault(size.getSelectedIndex()));
					sync.run();
				});
				landscape.addActionListener(event -> sync.run());
				portrait.addActionListener(event -> sync.run());
				sync.run();
				JPanel panel = new JPanel(new GridBagLayout());
				int row = 0;
				row = add(panel, row, new JLabel(app.layerText("IsoA.Dialog.Size")), size);
				row = add(panel, row, landscape, portrait);
				row = add(panel, row, new JLabel(app.layerText("IsoA.Dialog.Scale")), scale);
				row = add(panel, row, frame, null);
				row = add(panel, row, frameReason, null);
				row = add(panel, row, label, null);
				add(panel, row, new JLabel(app.layerText("IsoA.Dialog.Unit",
						app.getExportScalePresentationText())), null);
				int answer = JOptionPane.showConfirmDialog(app.getMainComponent(), panel,
						app.layerText("IsoA.Dialog.Title"), JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE);
				if (answer != JOptionPane.OK_OPTION) {
					return null;
				}
				DrawingScale parsed;
				try {
					parsed = DrawingScale.parse(String.valueOf(scale.getEditor().getItem()));
				} catch (IllegalArgumentException e) {
					parsed = null;
				}
				SheetRequest chosen = new SheetRequest(size.getSelectedIndex(),
						landscape.isSelected(), parsed == null ? current.scale() : parsed,
						frame.isSelected(), label.isSelected());
				if (parsed != null) {
					return chosen;
				}
				JOptionPane.showMessageDialog(app.getMainComponent(),
						app.layerText("IsoA.Dialog.InvalidScale"),
						app.layerText("IsoA.Dialog.Title"), JOptionPane.WARNING_MESSAGE);
				current = chosen;
			}
		}

		@Override
		public SelectionRequest askSelection(AppGeoCeDG app, SelectionRequest initial) {
			JTextField x = new JTextField(Double.toString(initial.x()), 12);
			JTextField y = new JTextField(Double.toString(initial.y()), 12);
			JComboBox<String> size = sizes(initial.index());
			JRadioButton landscape = new JRadioButton(app.layerText("IsoA.Dialog.Landscape"),
					initial.landscape());
			JRadioButton portrait = new JRadioButton(app.layerText("IsoA.Dialog.Portrait"),
					!initial.landscape());
			ButtonGroup orientation = new ButtonGroup();
			orientation.add(landscape);
			orientation.add(portrait);
			JPanel corner = new JPanel();
			corner.add(x);
			corner.add(y);
			JPanel panel = new JPanel(new GridBagLayout());
			int row = 0;
			row = add(panel, row, new JLabel(app.layerText("IsoA.Selection.Corner")), corner);
			row = add(panel, row, new JLabel(app.layerText("IsoA.Dialog.Size")), size);
			row = add(panel, row, landscape, portrait);
			add(panel, row, new JLabel(app.layerText("IsoA.Selection.Note",
					app.getDrawingScale().toString(), app.getExportScalePresentationText())),
					null);
			int answer = JOptionPane.showConfirmDialog(app.getMainComponent(), panel,
					app.layerText("IsoA.Selection.Title"), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (answer != JOptionPane.OK_OPTION) {
				return null;
			}
			try {
				return new SelectionRequest(parse(x.getText()), parse(y.getText()),
						size.getSelectedIndex(), landscape.isSelected());
			} catch (NumberFormatException e) {
				return new SelectionRequest(Double.NaN, Double.NaN, size.getSelectedIndex(),
						landscape.isSelected());
			}
		}

		private static double parse(String text) {
			return Double.parseDouble(text.trim().replace(',', '.'));
		}

		@Override
		public boolean confirm(AppGeoCeDG app, String message, String title) {
			return JOptionPane.showConfirmDialog(app.getMainComponent(), message, title,
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
					== JOptionPane.YES_OPTION;
		}

		@Override
		public boolean inform(AppGeoCeDG app, String message, String title, String action) {
			if (action == null) {
				JOptionPane.showMessageDialog(app.getMainComponent(), message, title,
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			Object[] options = {action, app.layerText("IsoA.Dialog.Close")};
			return JOptionPane.showOptionDialog(app.getMainComponent(), message, title,
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
					options[1]) == 0;
		}

		private static JComboBox<String> sizes(int selected) {
			String[] names = new String[IsoASheet.MAX_INDEX + 1];
			for (int n = 0; n <= IsoASheet.MAX_INDEX; n++) {
				names[n] = "A" + n + "  (" + IsoASheet.shortSideMm(n) + " " + (char) 0xd7
						+ " " + IsoASheet.longSideMm(n) + " mm)";
			}
			JComboBox<String> box = new JComboBox<>(names);
			box.setSelectedIndex(selected);
			return box;
		}

		private static int add(JPanel panel, int row, java.awt.Component first,
				java.awt.Component second) {
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = row;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(2, 2, 2, 6);
			c.gridx = 0;
			c.gridwidth = second == null ? 2 : 1;
			panel.add(first, c);
			if (second != null) {
				c.gridx = 1;
				panel.add(second, c);
			}
			return row + 1;
		}
	}
}
