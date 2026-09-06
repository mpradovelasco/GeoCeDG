/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import javax.imageio.ImageIO;

import org.geocedg.desktop.resources.GeoCeDGBrandingResource;
import org.junit.jupiter.api.Test;

class GeoCeDGBrandingResourceTest {

	private static final String RESOURCE_ROOT =
			"/org/geocedg/desktop/branding/v1/";

	@Test
	void promotedAuthorSourcesRemainByteExact() throws IOException {
		assertSource("source/helixTopBar.png", 969, 815,
				"08ef4481b51e801bdf0842635d645bd09042b0a4473b24faca555048e3bd52c1");
		assertSource("source/helixSnapshot.png", 1197, 1591,
				"abcf272553c1b42d5eb016cdf564023439e901ed7d7e943212c220431ecf5637");
	}

	@Test
	void runtimeRolesResolveOnlyVersionedGeoCeDGResources() throws IOException {
		assertSame(GeoCeDGBrandingResource.APPLICATION_ICON,
				AppGeoCeDG.getFrameIconResource());
		assertEquals("geocedg.brand.topbar",
				GeoCeDGBrandingResource.APPLICATION_ICON.getLogicalId());
		assertEquals("geocedg.brand.startup",
				GeoCeDGBrandingResource.STARTUP_SPLASH.getLogicalId());

		URL frameIcon = GeoCeDGBrandingResource.APPLICATION_ICON.getRequiredUrl();
		URL splash = GeoCeDG.getSplashResource();
		assertTrue(frameIcon.toExternalForm().contains(RESOURCE_ROOT));
		assertTrue(splash.toExternalForm().contains(RESOURCE_ROOT));
		assertFalse(frameIcon.toExternalForm().contains("/org/geogebra/"));
		assertFalse(splash.toExternalForm().contains("/org/geogebra/"));

		BufferedImage frameImage = ImageIO.read(frameIcon);
		BufferedImage splashImage = ImageIO.read(splash);
		assertNotNull(frameImage);
		assertNotNull(splashImage);
		assertEquals(64, frameImage.getWidth());
		assertEquals(64, frameImage.getHeight());
		assertEquals(0, frameImage.getRGB(0, 0) >>> 24);
		assertEquals(361, splashImage.getWidth());
		assertEquals(480, splashImage.getHeight());
	}

	private static void assertSource(String relativePath, int width, int height,
			String expectedSha256) throws IOException {
		URL resource = GeoCeDGBrandingResourceTest.class.getResource(
				RESOURCE_ROOT + relativePath);
		assertNotNull(resource);
		BufferedImage image = ImageIO.read(resource);
		assertNotNull(image);
		assertEquals(width, image.getWidth());
		assertEquals(height, image.getHeight());
		assertTrue(image.getColorModel().hasAlpha());
		assertEquals(expectedSha256, sha256(resource));
	}

	private static String sha256(URL resource) throws IOException {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			try (InputStream input = resource.openStream()) {
				byte[] buffer = new byte[8192];
				int count;
				while ((count = input.read(buffer)) >= 0) {
					digest.update(buffer, 0, count);
				}
			}
			return HexFormat.of().formatHex(digest.digest());
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable", exception);
		}
	}
}
