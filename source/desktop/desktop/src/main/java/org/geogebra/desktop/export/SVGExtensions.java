/*
 * GeoGebra - Dynamic Mathematics for Everyone
 * Copyright (c) GeoGebra GmbH, Altenbergerstr. 69, 4040 Linz, Austria
 * https://www.geogebra.org
 * 
 * This file is licensed by GeoGebra GmbH under the EUPL 1.2 licence and
 * may be used under the EUPL 1.2 in compatible projects (see Article 5
 * and the Appendix of EUPL 1.2 for details).
 * You may obtain a copy of the licence at:
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Note: The overall GeoGebra software package is free to use for
 * non-commercial purposes only.
 * See https://www.geogebra.org/license for full licensing details
 */

package org.geogebra.desktop.export;

import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.freehep.xml.util.XMLWriter;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.util.DoubleUtil;
import org.geogebra.common.util.StringUtil;

/**
 * Adds support for grouping objects in SVG files.
 * 
 * Also now adds support for exporting size in cm from <a href=
 * "https://help.geogebra.org/topic/incorrect-sizing-when-exporting-to-svg">
 * suggestion here</a> and returning null from getTransform() (ie Identity
 * matrix).
 * 
 * Needs this line changed in SVGGraphics2D.java (was private) protected
 * PrintWriter os;
 * 
 * @author Michael Borcherds
 */

public class SVGExtensions extends org.freehep.graphicsio.svg.SVGGraphics2D {

	private double cmWidth;
	private double cmHeight;
	// GeoCeDG (2026-10-02): PRE-G9B-R6-plus-B opt-in exact viewBox; NaN keeps
	// the host integer viewBox
	private double exactViewBoxWidth = Double.NaN;
	private double exactViewBoxHeight = Double.NaN;

	protected String title;
	protected String desc;

	/**
	 * @param file file
	 * @param size pixel size
	 * @param cmWidth width in cm
	 * @param cmHeight height in cm
	 * @throws IOException TODO how?
	 */
	public SVGExtensions(OutputStream file, Dimension size, double cmWidth,
			double cmHeight) throws IOException {
		super(file, size);
		this.cmWidth = DoubleUtil.checkDecimalFraction(cmWidth);
		this.cmHeight = DoubleUtil.checkDecimalFraction(cmHeight);
	}

	/**
	 * Start a group.
	 * @param id group ID
	 */
	public void startGroup(String id) {
		os.println("<g id=\"" + id + "\">");
	}

	/**
	 * End a group.
	 * @param id group ID
	 */
	public void endGroup(String id) {
		os.println("</g><!-- " + id + " -->");
	}

	@Override
	protected void writeSize(PrintWriter os) {

		if (cmWidth > 0 && cmHeight > 0) {
			// cm
			os.println("     width=\"" + cmWidth + "cm\"");
			os.println("     height=\"" + cmHeight + "cm\"");

		} else if (hasExactViewBox()) {
			os.println("     width=\"" + exactViewBoxWidth + "px\"");
			os.println("     height=\"" + exactViewBoxHeight + "px\"");
		} else {
			super.writeSize(os);

		}

		if (hasExactViewBox()) {
			// the host header prints its integer viewBox right after this hook
			this.os = new ExactViewBoxWriter(this.os, "     viewBox=\"0 0 "
					+ exactViewBoxWidth + " " + exactViewBoxHeight + "\"");
		}

	}

	/**
	 * GeoCeDG (2026-10-02): PRE-G9B-R6-plus-B exact canvas in user units. When
	 * set before the export starts, the root viewBox (and the pixel size when no
	 * centimetre size is given) carry these fractional values instead of the
	 * integer Dimension, so the viewBox and the physical size describe the same
	 * rectangle with the same aspect ratio and no scaling is anisotropic.
	 *
	 * @param width canvas width in user units, finite and positive
	 * @param height canvas height in user units, finite and positive
	 */
	public void setExactViewBox(double width, double height) {
		if (!(width > 0) || !(height > 0) || Double.isInfinite(width)
				|| Double.isInfinite(height)) {
			throw new IllegalArgumentException(
					"exact viewBox must be finite and positive");
		}
		exactViewBoxWidth = width;
		exactViewBoxHeight = height;
	}

	private boolean hasExactViewBox() {
		return !Double.isNaN(exactViewBoxWidth);
	}

	/** Replaces the first root viewBox line of the host header, once. */
	private static final class ExactViewBoxWriter extends PrintWriter {
		private static final String VIEW_BOX_PREFIX = "     viewBox=\"";
		private final String viewBoxLine;
		private boolean replaced;

		ExactViewBoxWriter(PrintWriter target, String viewBoxLine) {
			super(target, true);
			this.viewBoxLine = viewBoxLine;
		}

		@Override
		public void println(String line) {
			if (!replaced && line != null && line.startsWith(VIEW_BOX_PREFIX)) {
				replaced = true;
				super.println(viewBoxLine);
			} else {
				super.println(line);
			}
		}
	}

	public void setElementTitle(String title) {
		this.title = title;
	}

	public void setElementDesc(String desc) {
		this.desc = desc;
	}

	@Override
	protected void appendElementTitleAndDescription(StringBuilder sb) {

		if (title != null) {
			sb.append("\n<title>");
			sb.append(XMLWriter.normalizeText(title));
			sb.append("</title>");
		}

		if (desc != null) {
			sb.append("\n<desc>");
			sb.append(XMLWriter.normalizeText(desc));
			sb.append("</desc>\n");
		}

	}

	/**
	 * @param d drawable
	 * @param g2 graphics
	 */
	public final void draw(Drawable d, GGraphics2D g2) {
		GeoElement geo = d.getGeoElement();
		// defined check needed in case the GeoList changed its size
		if (geo.isDefined()) {
			if (d.needsUpdate()) {
				d.setNeedsUpdate(false);
				d.update();
			}

			if (geo.isGeoText()) {
				setElementTitle(((GeoText) geo).getTextString());
			} else {
				setElementTitle(geo.getNameDescription());
			}

			if (geo.isIndependent()) {
				// eg a:y = 4x + 3
				// eg A =(3, 4)
				setElementDesc(geo.getAlgebraDescriptionDefault());
			} else {
				setElementDesc(geo.getLongDescription());
			}

			d.draw(g2);
		}
	}
}
