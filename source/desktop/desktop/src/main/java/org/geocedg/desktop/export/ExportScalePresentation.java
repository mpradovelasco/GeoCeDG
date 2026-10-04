/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import javax.swing.JComponent;

import org.geogebra.common.export.pstricks.GeoGebraExport;

/**
 * PRE-G9B-R6-plus-C engineering-scale presentation that the upstream picture,
 * print-preview and LaTeX dialogs obtain from {@code AppD} (author decisions
 * {@code DQ-C6} and {@code DQ-C8}). The host returns no presentation and keeps
 * every upstream dialog.
 *
 * <p>With a physical construction unit the session {@link DrawingScale} is the
 * sole engineering sizing authority: no device-pixel or fixed-size mode is
 * offered and pixel dimensions are derived. Without a construction unit the host
 * device controls stay, labelled as a non-physical device scale.
 */
public interface ExportScalePresentation {

	/** @return whether the document has a physical construction unit */
	boolean isPhysical();

	/**
	 * @return {@code fb(effC) * 100 * a / b} centimetres per model unit, or NaN
	 *         without a construction unit
	 */
	double centimetresPerUnit();

	/**
	 * @param onChange called after an accepted drawing-scale change
	 * @return the {@code a:b} control with its construction-unit statement
	 */
	JComponent createScaleControl(Runnable onChange);

	/** @return statement that the document has no construction unit */
	JComponent createDeviceModeStatement();

	/**
	 * @param hostKey upstream menu key of a device-scale mode
	 *        ({@code ScaleInCentimeter}, {@code FixedSize}, {@code SizeInPixels})
	 * @param hostLabel upstream label
	 * @return the label of that mode as a non-physical device scale
	 */
	String deviceScaleLabel(String hostKey, String hostLabel);

	/** @return printed scale caption of a physical document */
	String printScaleTitle();

	/**
	 * @param exporter LaTeX exporter of the dialog
	 * @param onScaleChange called after an accepted drawing-scale change
	 * @return the product panel: scale, unit statement, semantic tolerance and the
	 *         export report
	 */
	JComponent createLatexPanel(GeoGebraExport exporter, Runnable onScaleChange);
}
