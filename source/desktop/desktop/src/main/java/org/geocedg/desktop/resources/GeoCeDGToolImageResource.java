/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop.resources;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.annotation.CheckForNull;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.geogebra.desktop.gui.util.JSVGIcon;
import org.geogebra.desktop.util.ImageResourceD;

/** Desktop adapter for profile-registered owned or explicitly reused tool artwork. */
public enum GeoCeDGToolImageResource implements ImageResourceD {

	LOCUS_V2("mode_locusv2"),
	/** GeoCeDG-owned artwork of the total semantic length tool (PRE-G9B-MAINT-U2-R1). */
	LOCUS_LENGTH_TOTAL("mode_geocedg_locuslength_total"),
	/** GeoCeDG-owned artwork of the partial semantic length tool (PRE-G9B-MAINT-U2-R1). */
	LOCUS_LENGTH_PARTIAL("mode_geocedg_locuslength_partial"),
	SPLINE_V2("mode_geocedg_splinev2"),
	/** Spline V2 artwork with a closure mark, for the closed spline tool (PRE-G9B-MAINT-U2-R1). */
	SPLINE_V2_CLOSED("mode_geocedg_splinev2_closed"),
	SEMANTIC_POINT("mode_geocedg_semanticpoint"),
	RICH_RESULT("mode_geocedg_richresult"),
	MATERIALIZE("mode_geocedg_materialize"),
	ZOOM_WINDOW("/org/geogebra/common/icons_toolbar/p64/", "mode_zoom", ".png"),
	/** Inherited host list artwork, reused explicitly for the ordered list tool. */
	ORDERED_LIST("mode_list"),
	/**
	 * Inherited host artwork, reused explicitly for the working-layer mode
	 * (PRE-G9B-R6-plus-A-1): it takes a property from a clicked object. No new
	 * asset is introduced, so no asset-manifest entry changes.
	 */
	WORKING_LAYER("mode_copyvisualstyle"),
	/** GeoCeDG-owned artwork of the native aligned dimension tool (PRE-G9B-R6-plus-E2). */
	ALIGNED_DIMENSION("mode_geocedg_aligneddimension"),
	/** GeoCeDG-owned artwork of the native linear dimension tool (PRE-G9B-R6-plus-E2). */
	LINEAR_DIMENSION("mode_geocedg_lineardimension"),
	/**
	 * Inherited host rectangle artwork, reused explicitly for the ISO A sheet tool
	 * (PRE-G9B-R6-plus-E3). No new asset is introduced, so no asset-manifest entry
	 * changes.
	 */
	ISO_A_BORDER("mode_shaperectangle");

	private static final int RASTER_SIZE = 64;
	private final String filename;

	GeoCeDGToolImageResource(String resourceName) {
		this("/org/geogebra/common/icons/svg/web/toolIcons/", resourceName, ".svg");
	}

	GeoCeDGToolImageResource(String directory, String resourceName, String extension) {
		this.filename = directory + resourceName + extension;
	}

	/**
	 * Resolve the manifest's stable icon reference, not a second action catalog.
	 * @param iconKey logical icon reference from the product profile
	 * @return owned artwork or null for the existing host/text fallback
	 */
	public static @CheckForNull GeoCeDGToolImageResource forIconKey(String iconKey) {
		switch (iconKey) {
		case "geocedg.action.SplineV2Create":
		case "geocedg.action.SplineV2CreateDegree":
			return SPLINE_V2;
		case "geocedg.action.SplineV2CreateClosed":
			return SPLINE_V2_CLOSED;
		case "geocedg.action.OrderedListCreate":
			return ORDERED_LIST;
		case "geocedg.action.WorkingLayer":
			return WORKING_LAYER;
		case "geocedg.action.AlignedDimension":
			return ALIGNED_DIMENSION;
		case "geocedg.action.IsoABorder":
			return ISO_A_BORDER;
		case "geocedg.action.LinearDimension":
			return LINEAR_DIMENSION;
		case "geocedg.action.LocusV2Point":
			return SEMANTIC_POINT;
		case "geocedg.action.LocusV2Create":
			return LOCUS_V2;
		case "geocedg.action.LocusLengthTotal":
			return LOCUS_LENGTH_TOTAL;
		case "geocedg.action.LocusLengthPartial":
			return LOCUS_LENGTH_PARTIAL;
		case "geocedg.action.ResultInspect":
		case "geocedg.action.ResultMarkers":
		case "geocedg.action.SemanticCurveDefinition":
			return RICH_RESULT;
		case "geocedg.action.MaterializeSelected":
		case "geocedg.action.MaterializeMultiple":
		case "geocedg.action.MaterializeAll":
		case "geocedg.action.AutoMaterializeInitial":
			return MATERIALIZE;
		case "geocedg.action.ZoomWindow":
			return ZOOM_WINDOW;
		default:
			return null;
		}
	}

	/**
	 * @param modeName mode name after the existing lowercase normalization
	 * @return owned or explicitly reused resource for the declared GeoCeDG modes,
	 *         otherwise null
	 */
	public static @CheckForNull GeoCeDGToolImageResource forMode(String modeName) {
		switch (modeName) {
		case "locusv2.referencepoint":
			return SEMANTIC_POINT;
		case "locusv2":
		case "locusv2.point":
			return LOCUS_V2;
		case "locuslength.total":
			return LOCUS_LENGTH_TOTAL;
		case "locuslength.partial":
			return LOCUS_LENGTH_PARTIAL;
		case "splinev2":
		case "splinev2.degree":
			return SPLINE_V2;
		case "splinev2.closed":
			return SPLINE_V2_CLOSED;
		case "orderedlist":
			return ORDERED_LIST;
		case "workinglayer":
			return WORKING_LAYER;
		case "aligneddimension":
			return ALIGNED_DIMENSION;
		case "isoaborder":
			return ISO_A_BORDER;
		case "lineardimension":
			return LINEAR_DIMENSION;
		default:
			return null;
		}
	}

	@Override
	public String getFilename() {
		return filename;
	}

	/**
	 * Decode a fresh image; ImageManagerD retains ownership of its image caches.
	 * @return loaded 64-pixel image, or null if the owned resource is missing
	 */
	public @CheckForNull Image renderImage() {
		URL resource = GeoCeDGToolImageResource.class.getResource(getFilename());
		if (resource == null) {
			return null;
		}
		if (!getFilename().endsWith(".svg")) {
			try {
				return ImageIO.read(resource);
			} catch (IOException exception) {
				return null;
			}
		}
		JSVGIcon icon = new JSVGIcon(resource);
		icon.setAntiAlias(true);
		icon.setPreferredSize(new Dimension(RASTER_SIZE, RASTER_SIZE));
		BufferedImage raster = new BufferedImage(RASTER_SIZE, RASTER_SIZE,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = raster.createGraphics();
		try {
			icon.paintIcon(null, graphics, 0, 0);
		} finally {
			graphics.dispose();
		}
		// The inherited border path needs distinct source/destination images.
		// Returning a BufferedImage would let its white fill erase the artwork.
		Image image = Toolkit.getDefaultToolkit().createImage(raster.getSource());
		return new ImageIcon(image).getImage();
	}
}
