/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.util.Objects;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Bounded, selectable presentation for verbatim DXF reports and evidence.
 */
public final class DxfReportPane extends JScrollPane {
	private static final long serialVersionUID = 1L;

	private final JTextArea reportArea;

	/**
	 * Creates a read-only report whose preferred viewport is bounded by the
	 * supplied rows and columns, independently of individual line length.
	 *
	 * @param report exact report content
	 * @param rows preferred visible rows
	 * @param columns preferred visible columns
	 */
	public DxfReportPane(String report, int rows, int columns) {
		reportArea = new JTextArea(Objects.requireNonNull(report), rows, columns);
		reportArea.setEditable(false);
		reportArea.setLineWrap(true);
		reportArea.setWrapStyleWord(true);
		reportArea.setCaretPosition(0);
		setViewportView(reportArea);
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
	}

	JTextArea getReportArea() {
		return reportArea;
	}
}
