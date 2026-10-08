/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.AbstractVectorGraphicsIO;
import org.freehep.graphicsio.FontConstants;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.freehep.graphicsio.emf.EMFPlusGraphics2D;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.freehep.util.UserProperties;
import org.geocedg.common.export.PhysicalExportScale;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.awt.GBufferedImage;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.euclidian.Drawable;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.main.App;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.awt.GBufferedImageD;
import org.geogebra.desktop.awt.GGraphics2DD;
import org.geogebra.desktop.export.SVGExtensions;
import org.geogebra.desktop.io.MyImageIO;

/**
 * PRE-G9B-R6-plus-B picture-export service: the only renderer of the GeoCeDG
 * picture routes. Every call resolves the {@link ExportArea} of the exported
 * view and paints a fresh {@link ExportViewport}; the live view and the
 * construction are never changed.
 *
 * <p>Output mapping, from the exact world bounds:
 * <ul>
 * <li>raster: an integer grid {@code round(w*s) x round(h*s)} onto which the
 * four exact bounds are mapped; the difference between requested and effective
 * resolution is a sampling property, never a geometric change;</li>
 * <li>PDF: the exact page box in points (within 0.001 pt), margins 0 and no
 * fit-to-page, through the opt-in exact page extent of the FreeHEP writer;</li>
 * <li>SVG: a fractional viewBox equal to the area, with the physical size of
 * the same aspect ratio, and an isotropic drawing;</li>
 * <li>EMF/EMF+: device bounds quantized to the nearest unit and an isotropic
 * drawing; since PRE-G9B-R6-plus-C the {@code rclFrame} is the requested
 * output size rounded to the nearest 0.01 mm ({@code DQ-C9}).</li>
 * </ul>
 *
 * <p>PRE-G9B-R6-plus-C: with a physical construction unit every route without
 * an explicit device scale is sized by {@code fb(c) * 100 * a / b} centimetres
 * per model unit ({@link #isPhysical(App)}); zoom, DPI and the view
 * {@code printingScale} are never its authority.
 */
public final class PictureExportService implements PictureExportRoute {
	/** Creator written into vector outputs. */
	public static final String CREATOR = "GeoCeDG / FreeHEP Graphics2D Driver";
	/** POST-E2-P1-R2: PDF points per style pixel on a physical PDF (0.4 t pt strokes). */
	public static final double PHYSICAL_POINTS_PER_STYLE_PIXEL = 0.8;
	private static final double CLIPBOARD_PIXEL_BUDGET = 500000;
	private static final double CLIPBOARD_SCALE = 2;

	private final App app;
	private final ExportAreaSession session;
	private Consumer<BufferedImage> clipboardSink;

	/**
	 * @param app application whose 2D views are exported
	 * @param session export-area authority
	 * @param clipboardSink receiver of graphics-clipboard images
	 */
	public PictureExportService(App app, ExportAreaSession session,
			Consumer<BufferedImage> clipboardSink) {
		this.app = app;
		this.session = session;
		this.clipboardSink = clipboardSink;
	}

	/**
	 * @param sink receiver of graphics-clipboard images
	 */
	public void setClipboardSink(Consumer<BufferedImage> sink) {
		clipboardSink = sink;
	}

	@Override
	public boolean handles(EuclidianView view) {
		return view != null && !(view instanceof ExportViewport)
				&& view.getApplication() == app && !view.isEuclidianView3D()
				&& (view.getViewID() == App.VIEW_EUCLIDIAN
						|| view.getViewID() == App.VIEW_EUCLIDIAN2);
	}

	@Override
	public ExportArea resolve(EuclidianView view) {
		return session.resolve(view);
	}

	@Override
	public double getExportWidth(EuclidianView view) {
		return require(view).pixelWidth(view.getXscale());
	}

	@Override
	public double getExportHeight(EuclidianView view) {
		return require(view).pixelHeight(view.getYscale());
	}

	@Override
	public GBufferedImage exportImage(EuclidianView view, double scale,
			boolean transparent, ExportType type) {
		ExportArea area = require(view);
		double width = area.pixelWidth(view.getXscale());
		double height = area.pixelHeight(view.getYscale());
		int gridWidth = rasterSize(width * scale);
		int gridHeight = rasterSize(height * scale);
		if (gridWidth <= 0 || gridHeight <= 0) {
			return null;
		}
		try {
			GBufferedImage image = AwtFactory.getPrototype().createBufferedImage(gridWidth,
					gridHeight, transparent);
			paint(view, area, image.createGraphics(), gridWidth / width,
					gridHeight / height, scale, transparent, type);
			image.flush();
			return image;
		} catch (RuntimeException e) {
			// the host's maximum-size handling: callers report an explicit error
			Log.debug("export image " + gridWidth + "x" + gridHeight + " failed: "
					+ e.getMessage());
			return null;
		}
	}

	@Override
	public void exportPaint(EuclidianView view, GGraphics2D graphics, double scale,
			boolean transparent, ExportType type) {
		paint(view, require(view), graphics, scale, scale, scale, transparent, type);
	}

	@Override
	public void writePNG(EuclidianView view, File file, boolean transparent, double dpi,
			double scale, ExportType type) throws IOException {
		GBufferedImage image = exportImage(view, scale, transparent, type);
		if (image == null) {
			throw new IOException("picture too large for this device");
		}
		MyImageIO.write(GBufferedImageD.getAwtBufferedImage(image), "png", (float) dpi,
				file);
	}

	@Override
	public void writePDF(EuclidianView view, File file, boolean textAsShapes)
			throws IOException {
		ExportArea area = require(view);
		double pointsPerPixel = pdfPointsPerPixel(view);
		double pageWidth = area.pixelWidth(view.getXscale()) * pointsPerPixel;
		double pageHeight = area.pixelHeight(view.getYscale()) * pointsPerPixel;
		if (isPhysical(app)) {
			// C4: an unrepresentable physical page fails explicitly
			PhysicalExportScale.requirePdfPageExtent(pageWidth, "PDF page width");
			PhysicalExportScale.requirePdfPageExtent(pageHeight, "PDF page height");
		}
		PDFGraphics2D graphics = new PDFGraphics2D(file, new Dimension(
				(int) Math.ceil(pageWidth), (int) Math.ceil(pageHeight)));
		graphics.setCreator(CREATOR);
		UserProperties properties = new UserProperties();
		properties.setProperty(PDFGraphics2D.EMBED_FONTS, !textAsShapes);
		properties.setProperty(PDFGraphics2D.EMBED_FONTS_AS,
				FontConstants.EMBED_FONTS_TYPE1);
		properties.setProperty(AbstractVectorGraphicsIO.TEXT_AS_SHAPES, textAsShapes);
		// author resolution of DQ-B8: no implicit margins, no fit-to-page
		properties.setProperty(PDFGraphics2D.PAGE_MARGINS, new Insets(0, 0, 0, 0));
		properties.setProperty(PDFGraphics2D.FIT_TO_PAGE, false);
		graphics.setProperties(properties);
		graphics.setExactPageSize(pageWidth, pageHeight);
		graphics.startExport();
		// POST-E2-P1-R2: a physical document gets physical style sizes
		double styleScale = isPhysical(app) ? physicalStyleScale(pointsPerPixel) : 1;
		paint(view, area, new GGraphics2DD(graphics), pointsPerPixel, pointsPerPixel,
				pointsPerPixel, styleScale, false, textAsShapes
						? ExportType.PDF_TEXTASSHAPES : ExportType.PDF_EMBEDFONTS);
		graphics.endExport();
	}

	/**
	 * POST-E2-P1-R2 (physical-pdf-style-sizes section 2): one style pixel is
	 * {@value #PHYSICAL_POINTS_PER_STYLE_PIXEL} pt on a physical PDF, so a stroke of
	 * line thickness t is 0.4 t pt whatever the zoom, unit and drawing scale.
	 *
	 * @param pointsPerPixel PDF points per viewport pixel of the page
	 * @return viewport pixels per style pixel
	 */
	public static double physicalStyleScale(double pointsPerPixel) {
		return PHYSICAL_POINTS_PER_STYLE_PIXEL / pointsPerPixel;
	}

	/**
	 * @param view exported view
	 * @return PDF points per source-view pixel, as the host page size
	 */
	public static double pdfPointsPerPixel(EuclidianView view) {
		return ExportViewport.printingScaleOf(view) * 72 / (2.54 * view.getXscale());
	}

	@Override
	public void writeSVG(EuclidianView view, OutputStream out, boolean textAsShapes,
			double cmPerPixel, double exportScale, boolean transparent) throws IOException {
		ExportArea area = require(view);
		double width = area.pixelWidth(view.getXscale());
		double height = area.pixelHeight(view.getYscale());
		// PRE-G9B-R6-plus-C (DQ-C16): without an explicit physical size a
		// physical document is sized by its unit contract and drawing scale
		double physicalCmPerPixel = cmPerPixel;
		if (!(cmPerPixel > 0) && isPhysical(app)) {
			physicalCmPerPixel = app.getPhysicalExportScale() / view.getXscale();
		}
		SVGExtensions graphics = new SVGExtensions(out,
				new Dimension((int) Math.ceil(width), (int) Math.ceil(height)),
				physicalCmPerPixel > 0 ? width * physicalCmPerPixel : -1,
				physicalCmPerPixel > 0 ? height * physicalCmPerPixel : -1);
		UserProperties properties = new UserProperties();
		properties.setProperty(SVGGraphics2D.EMBED_FONTS, !textAsShapes);
		properties.setProperty(AbstractVectorGraphicsIO.TEXT_AS_SHAPES, textAsShapes);
		graphics.setProperties(properties);
		graphics.setCreator(CREATOR);
		graphics.setExactViewBox(width, height);
		ExportViewport viewport = ExportViewport.create(view, area);
		viewport.setOutputScale(1, 1);
		try {
			// make sure LaTeX is exported at the host resolution
			app.setExporting(ExportType.SVG, exportScale);
			viewport.updateAllDrawables(true);
			graphics.startExport();
			GGraphics2D exportGraphics = new GGraphics2DD(graphics);
			viewport.exportPaintPre(exportGraphics, 1, transparent);
			graphics.startGroup("misc");
			viewport.drawActionObjectsOnShownLayers(exportGraphics);
			graphics.endGroup("misc");
			int currentLayer = 0;
			graphics.startGroup("layer" + currentLayer);
			for (Drawable drawable : viewport.getAllDrawableList()) {
				int layer = drawable.getGeoElement().getLayer();
				if (!app.isLayerShown(layer)) {
					continue;
				}
				if (layer != currentLayer) {
					graphics.endGroup("layer" + currentLayer);
					currentLayer = layer;
					graphics.startGroup("layer" + currentLayer);
				}
				graphics.draw(drawable, new GGraphics2DD(graphics));
			}
			graphics.endGroup("layer" + currentLayer);
			graphics.endExport();
			exportGraphics.resetClip();
		} finally {
			app.setExporting(ExportType.NONE, 1);
		}
	}

	@Override
	public void writeEMF(EuclidianView view, File file, boolean plus, double scale,
			double frameWidthCm, double frameHeightCm) throws IOException {
		ExportArea area = require(view);
		double exactWidth = area.pixelWidth(view.getXscale()) * scale;
		double exactHeight = area.pixelHeight(view.getYscale()) * scale;
		boolean physical = isPhysical(app);
		// C4: a physical output never takes the B minimum of one device unit
		int deviceWidth = physical
				? PhysicalExportScale.deviceExtent(exactWidth, "EMF width")
				: rasterSize(exactWidth);
		int deviceHeight = physical
				? PhysicalExportScale.deviceExtent(exactHeight, "EMF height")
				: rasterSize(exactHeight);
		// DQ-C9: the frame comes from the requested output size, never from DPI
		boolean exactFrame = Double.isFinite(frameWidthCm)
				&& Double.isFinite(frameHeightCm);
		int frameWidth = exactFrame ? PhysicalExportScale
				.emfFrameHundredthsOfMillimetre(frameWidthCm, "EMF frame width") : 0;
		int frameHeight = exactFrame ? PhysicalExportScale
				.emfFrameHundredthsOfMillimetre(frameHeightCm, "EMF frame height") : 0;
		Dimension bounds = new Dimension(deviceWidth, deviceHeight);
		VectorGraphics graphics;
		if (plus) {
			EMFPlusGraphics2D emfPlus = new EMFPlusGraphics2D(file, bounds);
			if (exactFrame) {
				emfPlus.setExactFrame(frameWidth, frameHeight);
			}
			graphics = emfPlus;
		} else {
			EMFGraphics2D emf = new EMFGraphics2D(file, bounds);
			if (exactFrame) {
				emf.setExactFrame(frameWidth, frameHeight);
			}
			graphics = emf;
		}
		graphics.setCreator(CREATOR);
		graphics.setDeviceIndependent(true);
		graphics.startExport();
		paint(view, area, new GGraphics2DD(graphics), scale, scale, scale, false,
				ExportType.EMF);
		graphics.endExport();
	}

	@Override
	public GBufferedImage previewImage(EuclidianView view, double maxWidth,
			double maxHeight) {
		ExportArea area = resolve(view);
		if (area == null) {
			return null;
		}
		double scale = Math.min(maxWidth / area.pixelWidth(view.getXscale()),
				maxHeight / area.pixelHeight(view.getYscale()));
		return exportImage(view, scale, false, ExportType.PNG);
	}

	/**
	 * Graphics clipboard (Ctrl+Shift+C and the menu route): the same area and
	 * service as the Picture surface, at the host clipboard scale.
	 *
	 * @param view exported view
	 * @return whether an image was handed to the clipboard sink
	 */
	public boolean copyToClipboard(EuclidianView view) {
		ExportArea area = require(view);
		double pixels = area.pixelWidth(view.getXscale())
				* area.pixelHeight(view.getYscale());
		double scale = pixels > CLIPBOARD_PIXEL_BUDGET
				? CLIPBOARD_SCALE * Math.sqrt(CLIPBOARD_PIXEL_BUDGET / pixels)
				: CLIPBOARD_SCALE;
		GBufferedImage image = exportImage(view, scale, false, ExportType.PNG);
		if (image == null) {
			return false;
		}
		clipboardSink.accept(GBufferedImageD.getAwtBufferedImage(image));
		return true;
	}

	/**
	 * @param app exporting application
	 * @return whether the application exports with the physical unit contract
	 *         of PRE-G9B-R6-plus-C
	 */
	public static boolean isPhysical(App app) {
		double scale = app.getPhysicalExportScale();
		return scale > 0 && Double.isFinite(scale);
	}

	/**
	 * PRE-G9B-R6-plus-C (C4): explicit limits of a physical raster before it
	 * is written; nothing applies to a device-scale route.
	 *
	 * @param app exporting application
	 * @param view exported view
	 * @param scale output pixels per source-view pixel
	 */
	public static void requirePhysicalRaster(App app, EuclidianView view,
			double scale) {
		if (!isPhysical(app)) {
			return;
		}
		PhysicalExportScale.deviceExtent(app.getExportFrameWidth(view) * scale,
				"Picture width");
		PhysicalExportScale.deviceExtent(app.getExportFrameHeight(view) * scale,
				"Picture height");
	}

	/**
	 * Integer raster or device grid of an exact extent: the nearest integer,
	 * at least one.
	 *
	 * @param exact exact extent in output units
	 * @return grid size, or 0 when not representable
	 */
	public static int rasterSize(double exact) {
		if (!Double.isFinite(exact) || exact > Integer.MAX_VALUE) {
			return 0;
		}
		return (int) Math.max(1, Math.round(exact));
	}

	private void paint(EuclidianView view, ExportArea area, GGraphics2D graphics,
			double scaleX, double scaleY, double exportScale, boolean transparent,
			ExportType type) {
		paint(view, area, graphics, scaleX, scaleY, exportScale, 1, transparent, type);
	}

	private void paint(EuclidianView view, ExportArea area, GGraphics2D graphics,
			double scaleX, double scaleY, double exportScale, double styleScale,
			boolean transparent, ExportType type) {
		ExportViewport viewport = ExportViewport.create(view, area, styleScale);
		viewport.setOutputScale(scaleX, scaleY);
		viewport.exportPaint(graphics, exportScale, transparent, type);
	}

	private ExportArea require(EuclidianView view) {
		ExportArea area = session.resolve(view);
		if (area == null) {
			throw new ExportAreaUnavailableException(
					"no valid export area for view " + view.getViewID());
		}
		return area;
	}
}
