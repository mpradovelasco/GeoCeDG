/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import org.geocedg.common.kernel.sheet.AlgoIsoABorder;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoPointND;

/**
 * PRE-G9B-R6-plus-E3-R1: LaTeX output of the ISO A sheet label. The host converts
 * the em dash of the label (A3, em dash, 1:50) through a JLaTeXMath table into the
 * undefined {@code \emdash} (PGF/TikZ, PSTricks) or the literal word {@code emdash}
 * (Asymptote), and ignores the label's alignment, so the text falls below and to the
 * right of its anchor, outside the frame. This writes the label in LaTeX text mode
 * with {@code \textemdash{}} and its lower-right corner at the anchor, as on screen.
 * Every other text keeps the host output; the label string is the kernel's.
 */
final class IsoABorderLatexExport {

	private static final char EM_DASH = (char) 0x2014;

	private IsoABorderLatexExport() {
	}

	/**
	 * @param geo exported text
	 * @return the owning sheet when the text is its label, else null
	 */
	static AlgoIsoABorder labelOwner(GeoText geo) {
		AlgoIsoABorder owner = AlgoIsoABorder.ownerOf(geo);
		return owner != null && owner.getLabelText() == geo ? owner : null;
	}

	/**
	 * @param geo sheet label
	 * @return {x, y} of its anchor (the label's lower-right corner), or null
	 */
	static double[] anchor(GeoText geo) {
		GeoPointND start = geo.getStartPoint();
		if (start == null || !start.isDefined()) {
			return null;
		}
		double[] coords = new double[3];
		start.getInhomCoords(coords);
		return Double.isFinite(coords[0]) && Double.isFinite(coords[1])
				? new double[] {coords[0], coords[1]} : null;
	}

	/**
	 * @param text label string
	 * @return the label for LaTeX text mode
	 */
	static String teX(String text) {
		return DimensionLatexExport.escapeTeX(text)
				.replace(String.valueOf(EM_DASH), "\\textemdash{}");
	}

	/**
	 * @param text label string
	 * @return the label for an Asymptote string holding LaTeX text
	 */
	static String asymptote(String text) {
		return teX(text).replace("\"", "''");
	}
}
