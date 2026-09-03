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
import java.net.URL;

import javax.annotation.CheckForNull;
import javax.swing.ImageIcon;

import org.geogebra.desktop.gui.util.JSVGIcon;
import org.geogebra.desktop.util.ImageResourceD;

/** Desktop adapter for the registered GeoCeDG-owned tool artwork. */
public enum GeoCeDGToolImageResource implements ImageResourceD {

	LOCUS_V2;

	private static final int RASTER_SIZE = 64;

	/**
	 * @param modeName mode name after the existing lowercase normalization
	 * @return owned resource for the four declared modes, otherwise null
	 */
	public static @CheckForNull GeoCeDGToolImageResource forMode(String modeName) {
		switch (modeName) {
		case "locusv2":
		case "locusv2.point":
		case "locuslength.total":
		case "locuslength.partial":
			return LOCUS_V2;
		default:
			return null;
		}
	}

	@Override
	public String getFilename() {
		return "/org/geogebra/common/icons/svg/web/toolIcons/mode_locusv2.svg";
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
