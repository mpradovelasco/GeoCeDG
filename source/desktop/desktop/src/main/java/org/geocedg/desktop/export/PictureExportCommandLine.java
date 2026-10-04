/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.export;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.main.App.ExportType;
import org.geogebra.common.util.debug.Log;

/**
 * PRE-G9B-R6-plus-B command-line {@code --export} through the common picture
 * service and the same {@link ExportArea} (author decision AQ-X5 of
 * 2026-10-02). When no valid area or output can be produced the export fails
 * explicitly with a non-zero status and leaves no empty file (DQ-B9).
 */
public final class PictureExportCommandLine {
	/** Exit status of a successful export. */
	public static final int SUCCESS = 0;
	/** Exit status of a refused or failed export. */
	public static final int FAILURE = 1;

	private PictureExportCommandLine() {
	}

	/**
	 * @param route picture route
	 * @param view exported 2D view
	 * @param file target file
	 * @param dpi requested resolution, as the host option
	 * @param maxSize optional host {@code --maxSize} value, or null
	 * @return process exit status
	 */
	public static int export(PictureExportRoute route, EuclidianView view, File file,
			int dpi, String maxSize) {
		ExportArea area = route.resolve(view);
		if (area == null) {
			return fail(null, "no valid export area for " + file);
		}
		double width = area.pixelWidth(view.getXscale());
		double height = area.pixelHeight(view.getYscale());
		// the opened document's view is not laid out yet: its printing scale is
		// derived from its scale, never 0 (DQ-B9)
		double printingScale = ExportViewport.printingScaleOf(view);
		double exportScale = printingScale * dpi / 2.54 / view.getXscale();
		int effectiveDpi = dpi;
		if (maxSize != null && !maxSize.isEmpty()) {
			double max = Integer.parseInt(maxSize);
			exportScale = Math.min(max / width, max / height);
			effectiveDpi = (int) (exportScale * view.getXscale() * 2.54 / printingScale);
		}
		if (!(exportScale > 0) || Double.isInfinite(exportScale)) {
			return fail(null, "invalid export scale " + exportScale + " for " + file);
		}
		String extension = extension(file);
		if (!"png".equals(extension) && !"pdf".equals(extension)
				&& !"emf".equals(extension) && !"svg".equals(extension)) {
			return fail(null, "unsupported export format: " + extension);
		}
		File target = file.getAbsoluteFile();
		File temporary = null;
		try {
			// written beside the target and moved only when valid, so a failure
			// never leaves an empty file and never removes an earlier one
			if ("png".equals(extension)) {
				// PRE-G9B-R6-plus-C (C4): a physical raster below one pixel or
				// beyond the integer range fails explicitly
				PictureExportService.requirePhysicalRaster(view.getApplication(),
						view, exportScale);
			}
			temporary = File.createTempFile(".geocedg-export-", "." + extension,
					target.getParentFile());
			// DQ-C9: the EMF frame is the output size of the printing scale,
			// physical when the document has a construction unit
			write(route, view, temporary, extension, effectiveDpi, exportScale,
					width / view.getXscale() * printingScale,
					height / view.getXscale() * printingScale);
			if (temporary.length() == 0) {
				return fail(temporary, "export produced no output: " + file);
			}
			Files.move(temporary.toPath(), target.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException | RuntimeException e) {
			return fail(temporary, "export failed: " + e.getMessage());
		}
		Log.debug("Graphics View exported successfully to " + target);
		return SUCCESS;
	}

	private static void write(PictureExportRoute route, EuclidianView view, File file,
			String extension, int dpi, double exportScale, double frameWidthCm,
			double frameHeightCm) throws IOException {
		switch (extension) {
		case "png":
			route.writePNG(view, file, true, dpi, exportScale, ExportType.PNG);
			break;
		case "pdf":
			route.writePDF(view, file, true);
			break;
		case "emf":
			route.writeEMF(view, file, true, exportScale, frameWidthCm,
					frameHeightCm);
			break;
		default:
			try (OutputStream out = Files.newOutputStream(file.toPath())) {
				route.writeSVG(view, out, true, -1, exportScale, true);
			}
			break;
		}
	}

	/**
	 * @return message of the explicit {@code --exportAnimation} rejection
	 */
	public static String animationRejectedMessage() {
		return "--exportAnimation is not available: Animated GIF is outside "
				+ "the GeoCeDG export surface";
	}

	private static String extension(File file) {
		String name = file.getName();
		int dot = name.lastIndexOf('.');
		return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

	private static int fail(File temporary, String message) {
		// only the temporary output of this run is removed
		if (temporary != null && temporary.isFile()) {
			try {
				Files.delete(temporary.toPath());
			} catch (IOException e) {
				Log.debug("could not delete " + temporary + ": " + e.getMessage());
			}
		}
		Log.error(message);
		System.err.println("GeoCeDG --export: " + message);
		return FAILURE;
	}
}
