/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.geogebra.common.awt.GBufferedImage;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.main.App.ExportType;

/**
 * PRE-G9B-R6-plus-B single picture-export authority consumed by every host
 * picture route: the Picture dialog and its Save preview, Print Preview, the
 * graphics clipboard, the command line, {@code ExportImage} and the graphics
 * APIs. Each route renders the effective {@link ExportArea} of the exported 2D
 * view through an {@link ExportViewport}; there is no second area mechanism.
 */
public interface PictureExportRoute {
	/**
	 * @param view exported view
	 * @return whether this route renders it; the 3D view and foreign views stay
	 *         on the host path
	 */
	boolean handles(EuclidianView view);

	/**
	 * @param view exported view
	 * @return effective area of the view, or null when none is valid
	 */
	ExportArea resolve(EuclidianView view);

	/**
	 * @param view exported view
	 * @return exact export width in source-view pixels
	 * @throws ExportAreaUnavailableException when no valid area exists
	 */
	double getExportWidth(EuclidianView view);

	/**
	 * @param view exported view
	 * @return exact export height in source-view pixels
	 * @throws ExportAreaUnavailableException when no valid area exists
	 */
	double getExportHeight(EuclidianView view);

	/**
	 * Raster image of the area: the exact world bounds map onto the integer
	 * grid {@code round(width*scale) x round(height*scale)}.
	 *
	 * @param view exported view
	 * @param scale requested output pixels per source-view pixel
	 * @param transparent whether the background is transparent
	 * @param type export type
	 * @return image, or null when the device limit is exceeded
	 */
	GBufferedImage exportImage(EuclidianView view, double scale, boolean transparent,
			ExportType type);

	/**
	 * Paints the area with an isotropic scale into a graphics whose origin is
	 * the area's top-left corner (print, vector writers).
	 *
	 * @param view exported view
	 * @param graphics target graphics
	 * @param scale output units per source-view pixel
	 * @param transparent whether the background is transparent
	 * @param type export type
	 */
	void exportPaint(EuclidianView view, GGraphics2D graphics, double scale,
			boolean transparent, ExportType type);

	/**
	 * @param view exported view
	 * @param file target PNG
	 * @param transparent transparent background
	 * @param dpi resolution metadata
	 * @param scale requested output pixels per source-view pixel
	 * @param type export type
	 * @throws IOException when writing fails
	 */
	void writePNG(EuclidianView view, File file, boolean transparent, double dpi,
			double scale, ExportType type) throws IOException;

	/**
	 * PDF with the exact page box (author tolerance 0.001 pt), no margins and
	 * no fit-to-page.
	 *
	 * @param view exported view
	 * @param file target PDF
	 * @param textAsShapes whether text is written as shapes
	 * @throws IOException when writing fails
	 */
	void writePDF(EuclidianView view, File file, boolean textAsShapes) throws IOException;

	/**
	 * SVG whose fractional viewBox is the exact area, with hidden layers
	 * omitted and the {@code layer<n>} groups kept.
	 *
	 * @param view exported view
	 * @param out target stream
	 * @param textAsShapes whether text is written as shapes
	 * @param cmPerPixel physical centimetres per source-view pixel, or a
	 *            non-positive value for a pixel-sized document; with a
	 *            physical construction unit a non-positive value means the
	 *            physical scale of PRE-G9B-R6-plus-C (no explicit device scale)
	 * @param exportScale scale used by the host for embedded LaTeX resolution
	 * @param transparent transparent background
	 * @throws IOException when writing fails
	 */
	void writeSVG(EuclidianView view, OutputStream out, boolean textAsShapes,
			double cmPerPixel, double exportScale, boolean transparent) throws IOException;

	/**
	 * EMF/EMF+ with device bounds quantized to the nearest unit and an
	 * isotropic drawing. PRE-G9B-R6-plus-C (DQ-C9): the {@code rclFrame} is
	 * written from the requested output size, never from DPI, rounded to the
	 * nearest 0.01 mm, through the opt-in exact frame of the FreeHEP writer.
	 *
	 * @param view exported view
	 * @param file target EMF
	 * @param plus EMF+ records
	 * @param scale output device units per source-view pixel
	 * @param frameWidthCm requested output width in centimetres, or NaN to
	 *            keep the FreeHEP frame
	 * @param frameHeightCm requested output height in centimetres, or NaN
	 * @throws IOException when writing fails
	 */
	void writeEMF(EuclidianView view, File file, boolean plus, double scale,
			double frameWidthCm, double frameHeightCm) throws IOException;

	/**
	 * Save-dialog preview of a pending picture export.
	 *
	 * @param view exported view
	 * @param maxWidth maximal preview width
	 * @param maxHeight maximal preview height
	 * @return preview image, or null when no area is valid
	 */
	GBufferedImage previewImage(EuclidianView view, double maxWidth, double maxHeight);
}
