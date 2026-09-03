/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JPanel;

import org.geocedg.desktop.resources.GeoCeDGToolImageResource;
import org.geogebra.common.awt.AwtFactory;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.kernel.geos.GeoImage;
import org.geogebra.common.util.StringUtil;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.awt.AwtFactoryD;
import org.geogebra.desktop.geogebra3D.util.ImageManager3D;
import org.geogebra.desktop.gui.MyImageD;
import org.geogebra.desktop.main.ScaledIcon;
import org.geogebra.desktop.util.ImageManagerD;
import org.geogebra.desktop.util.ImageResourceD;
import org.geogebra.desktop.util.LoggerD;
import org.geogebra.desktop.util.StringUtilD;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Real Desktop loading of the owned SVG, without changing legacy resources. */
class GeoCeDGToolImageResourceTest {

	private static final int[] MODES = {EuclidianConstants.MODE_LOCUS_V2,
			EuclidianConstants.MODE_LOCUS_V2_POINT,
			EuclidianConstants.MODE_LOCUS_V2_LENGTH,
			EuclidianConstants.MODE_LOCUS_V2_LENGTH_BETWEEN};

	@BeforeAll
	static void initializeDesktop() {
		StringUtil.setPrototypeIfNull(new StringUtilD());
		AwtFactory.setPrototypeIfNull(new AwtFactoryD());
		if (Log.getLogger() == null) {
			Log.setLogger(new LoggerD());
		}
	}

	@Test
	void allFourModesResolveTheOwnedResource() {
		ImageManagerD manager = new ImageManagerD(new JPanel());
		for (int size : new int[] {32, 64}) {
			manager.setMaxIconSize(size);
			for (int mode : MODES) {
				String name = EuclidianConstants.getModeTextSimple(mode);
				ImageResourceD resource = manager.getToolImageResource(name);
				assertSame(GeoCeDGToolImageResource.LOCUS_V2, resource);
				assertSame(resource, manager.getToolImageResource(
						name.toUpperCase(Locale.ROOT)));
				assertNotNull(getClass().getResource(resource.getFilename()));
			}
		}
	}

	@Test
	void bothDesktopManagersLoadOwnedColors() {
		for (ImageManagerD manager : new ImageManagerD[] {
				new ImageManagerD(new JPanel()), new ImageManager3D(new JPanel())}) {
			for (int mode : MODES) {
				ImageResourceD resource = manager.getToolImageResource(
						EuclidianConstants.getModeTextSimple(mode));
				assertOwnedRaster(manager.getImageResourceGeoGebra(resource), 64);
				assertOwnedRaster(manager.getInternalImage(resource).getImage(), 64);
			}
		}
	}

	@Test
	void borderingDoesNotEraseOrContaminateOwnedPixels() {
		ImageManagerD manager = new ImageManagerD(new JPanel());
		ImageResourceD resource = manager.getToolImageResource("LocusV2");
		Image raw = manager.getImageResource(resource);
		int[] before = pixels(raw);
		Image bordered = manager.getImageIcon(resource, Color.LIGHT_GRAY, Color.WHITE);
		assertOwnedRaster(bordered, 64);
		assertSame(bordered, manager.getImageIcon(resource, Color.LIGHT_GRAY, Color.WHITE));
		BufferedImage borderPixels = ImageManagerD.toBufferedImage(bordered);
		assertEquals(Color.LIGHT_GRAY.getRGB(), borderPixels.getRGB(0, 0));
		assertEquals(Color.WHITE.getRGB(), borderPixels.getRGB(1, 1));
		Image fresh = manager.getImageResource(resource);
		assertNotSame(raw, fresh);
		assertArrayEquals(before, pixels(raw));
		assertArrayEquals(before, pixels(fresh));
		assertArrayEquals(before, pixels(new ImageManagerD(new JPanel())
				.getImageResource(resource)));
		assertEquals(0, ImageManagerD.toBufferedImage(fresh).getRGB(0, 0) >>> 24);
	}

	@Test
	void responsiveScalingPreservesExistingPixelRatioContract() {
		ImageManagerD manager = new ImageManagerD(new JPanel());
		ImageResourceD resource = manager.getToolImageResource("LocusV2");
		manager.setMaxIconSize(32);
		Image first = manager.getImageIcon(resource);
		assertScaled(manager, first, 32, 32);
		manager.setMaxIconSize(64);
		assertSame(first, manager.getImageIcon(resource));
		assertScaled(manager, first, 64, 64);
		assertTrue(manager.updatePixelRatio(configuration(2)));
		Image highDpi = manager.getImageIcon(resource);
		assertNotSame(first, highDpi);
		assertScaled(manager, highDpi, 32, 64);
		manager.setMaxIconSize(32);
		assertScaled(manager, highDpi, 32, 64);
		assertTrue(manager.updatePixelRatio(configuration(1)));
		Image restored = manager.getImageIcon(resource);
		assertNotSame(highDpi, restored);
		assertScaled(manager, restored, 32, 32);
		assertFalse(manager.updatePixelRatio(configuration(1)));
		assertSame(restored, manager.getImageIcon(resource));
	}

	@Test
	void legacyAndUnmatchedNamesKeepPngPaths() {
		ImageManagerD manager = new ImageManagerD(new JPanel());
		for (int size : new int[] {32, 64}) {
			manager.setMaxIconSize(size);
			for (String name : new String[] {"Point", "Locus", "LocusV2.Other",
					"LocusLength.Unknown"}) {
				ImageResourceD resource = manager.getToolImageResource(name);
				assertEquals("/org/geogebra/common/icons_toolbar/p" + size
						+ "/mode_" + name.toLowerCase(Locale.ROOT) + ".png",
						resource.getFilename());
				assertFalse(resource instanceof GeoCeDGToolImageResource);
			}
			assertNotNull(manager.getImageResource(manager.getToolImageResource("Point")));
			assertNotNull(manager.getImageResource(manager.getToolImageResource("Locus")));
		}
	}

	@Test
	void toolbarLoadsOwnedRasterWithItsBackground() {
		AppGeoCeDG app = enabledApp();
		app.getImageManager().setMaxIconSize(64);
		app.getImageManager().updatePixelRatio(configuration(1));
		for (int mode : MODES) {
			ScaledIcon icon = app.getToolBarImage(
					EuclidianConstants.getModeTextSimple(mode), Color.LIGHT_GRAY);
			assertNotNull(icon);
			assertEquals(64, icon.getIconWidth());
			assertOwnedRaster(icon.getImage(), 64);
		}
	}

	@Test
	void toolImageCallbackCreatesTheSameRealRaster() {
		AppGeoCeDG app = enabledApp();
		for (int mode : MODES) {
			AtomicReference<String> filename = new AtomicReference<>();
			app.getGuiManager().getToolImageURL(mode, null, filename::set);
			assertNotNull(filename.get());
			assertTrue(filename.get().endsWith("/tool.png"));
			MyImageD image = (MyImageD) app.getImageManager().getExternalImage(filename.get());
			assertNotNull(image);
			assertOwnedRaster(image.getImage(), 64);
			assertEquals(filename.get(), app.getImageManager().createImage(
					app.getImageManager().getToolImageResource(
							EuclidianConstants.getModeTextSimple(mode)), app));
		}
	}

	@Test
	void toolImageCommandCreatesAnImageForEachOwnedMode() {
		AppGeoCeDG app = enabledApp();
		for (int mode : MODES) {
			String label = "image" + mode;
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(
					label + "=ToolImage(" + mode + ")", false);
			GeoImage geo = (GeoImage) app.getKernel().lookupLabel(label);
			assertNotNull(geo);
			MyImageD image = (MyImageD) app.getImageManager()
					.getExternalImage(geo.getImageFileName());
			assertNotNull(image);
			assertOwnedRaster(image.getImage(), 64);
		}
	}

	private static GraphicsConfiguration configuration(double ratio) {
		GraphicsConfiguration configuration = mock(GraphicsConfiguration.class);
		when(configuration.getDefaultTransform()).thenReturn(
				AffineTransform.getScaleInstance(ratio, ratio));
		return configuration;
	}

	private static AppGeoCeDG enabledApp() {
		AppGeoCeDG app = new AppGeoCeDG(new CommandLineArguments(new String[] {
				"--silent", "--enableLocusV2=true"}), new JPanel());
		app.setErrorDialogsActive(false);
		return app;
	}

	private static void assertScaled(ImageManagerD manager, Image source,
			int logicalSize, int physicalSize) {
		ScaledIcon icon = manager.getResponsiveScaledIcon(source, manager.getMaxIconSize());
		assertEquals(logicalSize, icon.getIconWidth());
		assertEquals(logicalSize, icon.getIconHeight());
		assertOwnedRaster(icon.getImage(), physicalSize);
	}

	private static void assertOwnedRaster(Image image, int size) {
		assertNotNull(image);
		assertEquals(size, image.getWidth(null));
		assertEquals(size, image.getHeight(null));
		BufferedImage raster = ImageManagerD.toBufferedImage(image);
		assertTrue(containsColor(raster, new Color(0x4b6fff)), "Owned blue must survive");
		assertTrue(containsColor(raster, new Color(0xd43b3b)), "Owned red must survive");
	}

	private static boolean containsColor(BufferedImage image, Color expected) {
		for (int pixel : pixels(image)) {
			Color actual = new Color(pixel, true);
			if (actual.getAlpha() >= 200
					&& Math.abs(actual.getRed() - expected.getRed()) < 25
					&& Math.abs(actual.getGreen() - expected.getGreen()) < 25
					&& Math.abs(actual.getBlue() - expected.getBlue()) < 25) {
				return true;
			}
		}
		return false;
	}

	private static int[] pixels(Image image) {
		BufferedImage raster = ImageManagerD.toBufferedImage(image);
		return raster.getRGB(0, 0, raster.getWidth(), raster.getHeight(),
				null, 0, raster.getWidth());
	}
}
