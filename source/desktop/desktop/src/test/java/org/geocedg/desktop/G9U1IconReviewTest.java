/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Action;

import org.geocedg.desktop.resources.GeoCeDGToolImageResource;
import org.geogebra.desktop.util.ImageManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Owned asset resolution and text fallback through the single action authority. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1IconReviewTest {

	@Test
	void manifestIconsResolveWithoutChangingActionIdentityOrText() {
		assertNotNull(getClass().getResource(
				"/org/geocedg/desktop/geocedg_construction_quick_guide.md"));
		AppGeoCeDG app = G9U1TestApp.create();
		GeoCeDGActionRegistry registry = new GeoCeDGActionRegistry(app);
		for (String id : new String[] {"semantic.spline-v2.create",
				"semantic.locus-v2.point-explicit", "result.inspect-rich",
				"result.materialize-selected", "result.materialize-multiple",
				"result.materialize-all-eligible"}) {
			Action action = registry.get(id);
			assertEquals(id, action.getValue(GeoCeDGActionRegistry.ACTION_ID));
			assertNotNull(action.getValue(Action.SMALL_ICON));
			assertNotNull(action.getValue(Action.LARGE_ICON_KEY));
			assertNotNull(action.getValue(Action.NAME));
			assertNotNull(action.getValue(Action.SHORT_DESCRIPTION));
		}
		assertNull(GeoCeDGToolImageResource.forIconKey("unknown.extension"));
		assertNotNull(registry.get("document.new").getValue(Action.NAME));
		assertSame(GeoCeDGToolImageResource.LOCUS_V2,
				GeoCeDGToolImageResource.forMode("locusv2"));
	}

	@Test
	void vectorFamilyRendersAtNormalAndHighDpiWithoutColorOnlyMeaning() {
		G9U1TestApp.create();
		for (GeoCeDGToolImageResource resource : GeoCeDGToolImageResource.values()) {
			assertNotNull(getClass().getResource(resource.getFilename()));
			Image image = resource.renderImage();
			assertNotNull(image);
			for (int size : new int[] {20, 32, 40, 64}) {
				BufferedImage raster = ImageManagerD.toBufferedImage(
						ImageManagerD.getScaledImage(image, size, size));
				int darkPixels = 0;
				for (int y = 0; y < size; y++) {
					for (int x = 0; x < size; x++) {
						int pixel = raster.getRGB(x, y);
						if ((pixel >>> 24) > 128 && (pixel & 0xffffff) < 0x555555) {
							darkPixels++;
						}
					}
				}
				assertTrue(darkPixels > size, resource.name() + " visible outline");
			}
		}
	}
}
