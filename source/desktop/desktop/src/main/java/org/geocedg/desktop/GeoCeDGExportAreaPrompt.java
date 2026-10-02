/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.GridLayout;
import java.math.BigDecimal;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.geocedg.desktop.export.ExportArea;

/**
 * PRE-G9B-R6-plus-B definition of the MANUAL export area by world bounds
 * (DQ-B4: a dialog, no drag gesture). The prompt is injectable so that tests
 * never open a modal dialog.
 */
interface GeoCeDGExportAreaPrompt {
	/**
	 * @param app application
	 * @param initial bounds prefilled from the current effective area
	 *            {@code {xmin, xmax, ymin, ymax}}
	 * @param visible bounds of the visible viewport, for the "use visible
	 *            view" button
	 * @return chosen bounds {@code {x1, x2, y1, y2}}, or null when cancelled
	 * @throws NumberFormatException when a value is not a number
	 */
	double[] ask(AppGeoCeDG app, double[] initial, double[] visible);

	/** @return the Swing dialog used by the product */
	static GeoCeDGExportAreaPrompt dialog() {
		return (app, initial, visible) -> {
			String language = app.getLocale().getLanguage();
			JTextField[] fields = new JTextField[4];
			String[] labels = { "ExportArea.Define.Xmin", "ExportArea.Define.Xmax",
					"ExportArea.Define.Ymin", "ExportArea.Define.Ymax" };
			JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
			for (int i = 0; i < fields.length; i++) {
				fields[i] = new JTextField(format(initial[i]), 12);
				panel.add(new JLabel(GeoCeDGProfile.getText(labels[i], language)));
				panel.add(fields[i]);
			}
			JButton useView = new JButton(
					GeoCeDGProfile.getText("ExportArea.Define.UseView", language));
			useView.addActionListener(event -> {
				for (int i = 0; i < fields.length; i++) {
					fields[i].setText(format(visible[i]));
				}
			});
			panel.add(new JLabel());
			panel.add(useView);
			int answer = JOptionPane.showConfirmDialog(app.getMainComponent(), panel,
					GeoCeDGProfile.getText("ExportArea.Define.Title", language),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (answer != JOptionPane.OK_OPTION) {
				return null;
			}
			double[] bounds = new double[fields.length];
			for (int i = 0; i < fields.length; i++) {
				bounds[i] = parse(fields[i].getText());
			}
			return bounds;
		};
	}

	/**
	 * @param area area
	 * @return {@code {xmin, xmax, ymin, ymax}}
	 */
	static double[] bounds(ExportArea area) {
		return new double[] { area.getXmin(), area.getXmax(), area.getYmin(),
				area.getYmax() };
	}

	/**
	 * Locale-neutral number: a decimal comma is accepted as a decimal point.
	 *
	 * @param text user text
	 * @return value
	 * @throws NumberFormatException when the text is not a finite number
	 */
	static double parse(String text) {
		double value = Double.parseDouble(text.trim().replace(',', '.'));
		if (!Double.isFinite(value)) {
			throw new NumberFormatException(text);
		}
		return value;
	}

	private static String format(double value) {
		return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
	}
}
