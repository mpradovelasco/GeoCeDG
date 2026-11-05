/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Documents.M;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.classic;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.onEdt;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.save;
import static org.geogebra.common.kernel.ConstructionDefaults.DEFAULT_LOCUS;
import static org.geogebra.common.kernel.ConstructionDefaults.DEFAULT_LOCUS_V2;
import static org.geogebra.common.kernel.ConstructionDefaults.DEFAULT_SPLINE_V2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.geos.LegacyStyleDefaults;
import org.geocedg.common.kernel.geos.V2StyleDefaults;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.io.MyXMLio;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.main.AppD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class Cu4V2DefaultsArchiveTest {
	private static final String SPLINE = "SplineV2({(0,0),(1,2),(3,1),(4,0)},3)";

	@TempDir
	Path dir;

	@Test
	void documentAndObjectStylesOutrankPersonalDefaultsAcrossBothExtensions() throws Exception {
		onEdt(() -> {
			AppGeoCeDG source = app(M);
			defaults(source).getDefaultGeo(DEFAULT_LOCUS_V2).setObjColor(GColor.RED);
			defaults(source).getDefaultGeo(DEFAULT_SPLINE_V2).setObjColor(GColor.BLUE);
			GeoLocusV2 spline = (GeoLocusV2) must(source, "S=" + SPLINE);
			spline.setObjColor(GColor.BLACK);
			spline.setLabelVisible(false);
			var id = spline.getPersistentLocusId();
			Path nativePath = save(source, dir, "styled").toPath();
			Path compatibilityPath = Files.copy(nativePath, dir.resolve("styled.ggb"));
			try (ZipFile zip = new ZipFile(nativePath.toFile())) {
				assertNotNull(zip.getEntry(V2StyleDefaults.ZIP_ENTRY));
				String legacy = new String(zip.getInputStream(zip.getEntry(MyXMLio.XML_FILE_DEFAULTS_2D))
						.readAllBytes(), StandardCharsets.UTF_8);
				assertFalse(legacy.contains("default=\"81\""));
				assertFalse(legacy.contains("default=\"82\""));
			}

			AppGeoCeDG receiver = app(M);
			defaults(receiver).getDefaultGeo(DEFAULT_SPLINE_V2).setObjColor(GColor.GREEN);
			defaults(receiver).setPersonalDefaults(LegacyStyleDefaults.empty(),
					defaults(receiver).getV2DefaultsXML());
			for (Path file : new Path[]{nativePath, compatibilityPath}) {
				assertTrue(receiver.loadFile(file.toFile(), false));
				GeoLocusV2 loaded = (GeoLocusV2) receiver.getKernel().lookupLabel("S");
				assertEquals(id, loaded.getPersistentLocusId());
				assertEquals(GColor.BLACK, loaded.getObjectColor());
				assertFalse(loaded.getLabelVisible());
				assertEquals(GColor.RED, defaults(receiver).getDefaultGeo(DEFAULT_LOCUS_V2)
						.getObjectColor());
				assertEquals(GColor.BLUE, must(receiver, "T=" + SPLINE).getObjectColor());
				assertNull(receiver.getKernel().lookupLabel("SplineV2"));
				receiver.setSaved();
			}
			return null;
		});
	}

	@Test
	void discardedEntryAndLegacyDocumentsUseTheClassicFallback() throws Exception {
		onEdt(() -> {
			AppGeoCeDG source = app(M);
			defaults(source).getDefaultGeo(DEFAULT_LOCUS).setObjColor(GColor.GREEN);
			defaults(source).getDefaultGeo(DEFAULT_LOCUS_V2).setObjColor(GColor.RED);
			must(source, "A=(1,2)");
			Path nativePath = save(source, dir, "optional").toPath();
			Path stripped = dir.resolve("stripped.cedg");
			rewriteDefaults(nativePath, stripped, null);

			AppGeoCeDG receiver = app(M);
			defaults(receiver).getDefaultGeo(DEFAULT_LOCUS_V2).setObjColor(GColor.BLUE);
			defaults(receiver).setPersonalDefaults(LegacyStyleDefaults.empty(),
					defaults(receiver).getV2DefaultsXML());
			assertTrue(receiver.loadFile(nativePath.toFile(), false));
			assertEquals(GColor.RED, defaults(receiver).getDefaultGeo(DEFAULT_LOCUS_V2)
					.getObjectColor());
			assertTrue(receiver.loadFile(stripped.toFile(), false));
			assertEquals(GColor.GREEN, defaults(receiver).getDefaultGeo(DEFAULT_LOCUS_V2)
					.getObjectColor());
			assertEquals(GColor.GREEN, defaults(receiver).getDefaultGeo(DEFAULT_SPLINE_V2)
					.getObjectColor());
			assertEquals(1, receiver.getKernel().getConstruction()
					.getGeoSetConstructionOrder().size());
			assertNull(defaults(receiver).getV2DefaultsXML());

			Path compatible = Files.copy(nativePath, dir.resolve("optional.ggb"));
			AppD upstream = classic();
			assertTrue(upstream.loadFile(compatible.toFile(), false));
			assertNotNull(upstream.getKernel().lookupLabel("A"));
			assertNull(defaults(upstream).getDefaultGeo(DEFAULT_LOCUS_V2));
			assertNull(upstream.getExternalImage("geocedg_defaults_v2.png"));
			Path resaved = dir.resolve("classic-roundtrip.ggb");
			assertTrue(upstream.saveGeoGebraFile(resaved.toFile()));
			try (ZipFile zip = new ZipFile(resaved.toFile())) {
				assertNull(zip.getEntry(V2StyleDefaults.ZIP_ENTRY));
				assertNull(zip.getEntry("geocedg_defaults_v2.png"));
			}
			return null;
		});
	}

	@Test
	void malformedOptionalEntryRejectsBeforeReplacingCurrentObjectsOrDefaults() throws Exception {
		onEdt(() -> {
			AppGeoCeDG source = app(M);
			must(source, "B=(2,3)");
			Path malformed = dir.resolve("invalid.cedg");
			rewriteDefaults(save(source, dir, "source").toPath(), malformed,
					"<geogebra><defaults version=\"2\"></defaults></geogebra>");

			AppGeoCeDG receiver = app(M);
			GeoElement live = must(receiver, "A=(1,2)");
			GeoElement prototype = defaults(receiver).getDefaultGeo(DEFAULT_LOCUS_V2);
			prototype.setObjColor(GColor.BLUE);
			assertFalse(receiver.loadFile(malformed.toFile(), false));
			assertSame(live, receiver.getKernel().lookupLabel("A"));
			assertNull(receiver.getKernel().lookupLabel("B"));
			assertSame(prototype, defaults(receiver).getDefaultGeo(DEFAULT_LOCUS_V2));
			assertEquals(GColor.BLUE, prototype.getObjectColor());
			return null;
		});
	}

	private static ConstructionDefaults defaults(AppD app) {
		return app.getKernel().getConstruction().getConstructionDefaults();
	}

	private static void rewriteDefaults(Path input, Path output, String replacement)
			throws IOException {
		try (ZipFile source = new ZipFile(input.toFile());
				ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(output))) {
			var entries = source.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (!V2StyleDefaults.ZIP_ENTRY.equals(entry.getName())) {
					zip.putNextEntry(new ZipEntry(entry.getName()));
					try (var data = source.getInputStream(entry)) {
						data.transferTo(zip);
					}
					zip.closeEntry();
				}
			}
			if (replacement != null) {
				zip.putNextEntry(new ZipEntry(V2StyleDefaults.ZIP_ENTRY));
				zip.write(replacement.getBytes(StandardCharsets.UTF_8));
				zip.closeEntry();
			}
		}
	}
}
