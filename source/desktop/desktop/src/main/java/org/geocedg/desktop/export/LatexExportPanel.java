/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * PRE-G9B-R6-plus-C product panel of the PSTricks, PGF/TikZ and Asymptote
 * dialogs: the engineering scale or the non-physical device statement
 * ({@code DQ-C8}), the semantic-curve tolerance in model units ({@code DQ-C4})
 * and the non-modal export report of each generation ({@code DQ-C12}).
 */
public final class LatexExportPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JTextField tolerance;
	private final JTextArea report;
	private final LatexSemanticExportSupport support;

	/**
	 * @param scaleComponent scale control or device statement
	 * @param toleranceCaption caption of the tolerance field
	 * @param reportCaption caption of the report
	 * @param support exporter semantic support, or null for a host exporter
	 */
	public LatexExportPanel(JComponent scaleComponent, String toleranceCaption,
			String reportCaption, LatexSemanticExportSupport support) {
		super(new BorderLayout());
		this.support = support;
		JPanel rows = new JPanel();
		rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
		rows.add(scaleComponent);
		JPanel toleranceRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tolerance = new JTextField(
				Double.toString(LatexSemanticExportSupport.DEFAULT_TOLERANCE), 10);
		tolerance.setName("geocedg.latex.tolerance");
		toleranceRow.add(new JLabel(toleranceCaption));
		toleranceRow.add(tolerance);
		rows.add(toleranceRow);
		add(rows, BorderLayout.NORTH);
		report = new JTextArea(5, 60);
		report.setName("geocedg.latex.report");
		report.setEditable(false);
		JPanel reportPanel = new JPanel(new BorderLayout());
		reportPanel.add(new JLabel(reportCaption), BorderLayout.NORTH);
		reportPanel.add(new JScrollPane(report), BorderLayout.CENTER);
		add(reportPanel, BorderLayout.CENTER);
		if (support != null) {
			tolerance.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					applyTolerance();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					applyTolerance();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					applyTolerance();
				}
			});
			support.setResultListener(result -> report.setText(result.reportText()));
		}
	}

	private void applyTolerance() {
		support.setTolerance(parseTolerance(tolerance.getText()));
	}

	/**
	 * @param text dialog entry
	 * @return positive finite tolerance, or NaN for any other entry
	 */
	static double parseTolerance(String text) {
		try {
			double value = Double.parseDouble(text.trim());
			return value > 0 && Double.isFinite(value) ? value : Double.NaN;
		} catch (NumberFormatException | NullPointerException e) {
			return Double.NaN;
		}
	}

	/** @return the tolerance field */
	public JTextField getToleranceField() {
		return tolerance;
	}

	/** @return the report text */
	public String getReportText() {
		return report.getText();
	}
}
