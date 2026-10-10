/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.geocedg.common.euclidian.draw.DrawDimensionText;
import org.geocedg.common.kernel.dimension.AlgoNativeDimension;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;

/**
 * Bounded GeoCeDG LaTeX handling of native dimension outputs (PRE-G9B-R6-plus-E2,
 * DQ-E2-9): the dimension line gets its arrow endings and the value is written parallel
 * to the line, centred on it and lifted to its reading-up side. Every other segment and
 * text keeps the host output. The angle is derived from the model geometry and the
 * export units at export time and is never stored.
 */
final class DimensionLatexExport {

	private DimensionLatexExport() {
	}

	/**
	 * @param geo exported segment
	 * @return the owning dimension when the segment is a dimension line, else null
	 */
	static AlgoNativeDimension dimensionLineOwner(GeoElementND geo) {
		AlgoNativeDimension owner = AlgoNativeDimension.ownerOf((GeoElement) geo);
		return owner != null && owner.getDimensionLine() == geo ? owner : null;
	}

	/**
	 * @param geo exported text
	 * @return the owning dimension when the text is a dimension value, else null
	 */
	static AlgoNativeDimension dimensionTextOwner(GeoText geo) {
		AlgoNativeDimension owner = AlgoNativeDimension.ownerOf(geo);
		return owner != null && owner.isDimensionPresentationText(geo) ? owner : null;
	}

	/**
	 * @param owner dimension
	 * @param xUnit export length of one model unit along x
	 * @param yUnit export length of one model unit along y
	 * @return {anchorX, anchorY, readingAngleDegrees} in model coordinates, or null
	 */
	static double[] placement(AlgoNativeDimension owner, double xUnit, double yUnit) {
		double[] frame = owner.getDimensionTextFrame();
		if (frame == null) {
			return null;
		}
		double phi = Math.toDegrees(Math.atan2(frame[3] * yUnit, frame[2] * xUnit));
		return new double[] {frame[0], frame[1], DrawDimensionText.readableAngle(phi)};
	}

	/**
	 * POST-E3-E2-PSTRICKS-DIMENSION-ANGLE-C1: PSTricks reads the fractional digits of a
	 * rotation as a TeX integer and refuses ten or more ("Number too big"). The reading
	 * angle is written with at most six fractional digits, rounded half to even from its
	 * exact binary64 value (at most 5e-7 degrees away), as a plain decimal without
	 * exponent, trailing zeros or negative zero, independent of the locale.
	 *
	 * @param degrees finite reading angle in degrees
	 * @return the PSTricks rotation argument
	 */
	static String pstricksAngle(double degrees) {
		BigDecimal rounded = new BigDecimal(degrees).setScale(6, RoundingMode.HALF_EVEN);
		return rounded.signum() == 0 ? "0" : rounded.stripTrailingZeros().toPlainString();
	}

	/**
	 * @param text plain value text
	 * @return the text escaped for LaTeX text mode
	 */
	static String escapeTeX(String text) {
		StringBuilder sb = new StringBuilder();
		for (char c : text.toCharArray()) {
			switch (c) {
			case '\\':
				sb.append("\\textbackslash{}");
				break;
			case '{':
			case '}':
			case '$':
			case '&':
			case '#':
			case '_':
			case '%':
				sb.append('\\').append(c);
				break;
			case '^':
				sb.append("\\^{}");
				break;
			case '~':
				sb.append("\\~{}");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * @param text plain value text
	 * @return the text escaped for an Asymptote string holding LaTeX text
	 */
	static String escapeAsymptote(String text) {
		// backslashes stay literal in an Asymptote double-quoted string; a double quote
		// would end it, so it is written as two single quotes
		return escapeTeX(text).replace("\"", "''");
	}
}
