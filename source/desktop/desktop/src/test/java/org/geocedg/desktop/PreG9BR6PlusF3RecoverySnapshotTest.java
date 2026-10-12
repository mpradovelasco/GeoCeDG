/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.app;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.defineMyMid;
import static org.geocedg.desktop.PreG9BR6PlusF3Fixtures.must;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.kernel.ConstructionDefaults;
import org.geogebra.common.kernel.geos.GeoImage;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.desktop.gui.MyImageD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6-plus-F3 Stage A: the verified native recovery snapshot (G1, FR-F3-3) and
 * the recovery copy written from it (FR-F3-6).
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusF3RecoverySnapshotTest {

	@TempDir
	Path dir;

	/** Units, hidden layer, macro, image, library JavaScript and changed defaults. */
	private static AppGeoCeDG richDocument() throws Exception {
		AppGeoCeDG app = app(UnitState.of(UnitToken.MM, null, null));
		defineMyMid(app, "Kq", false);
		must(app, "A=(1,1)");
		must(app, "B=(4,2)");
		must(app, "N=MyMid(A,B)");
		app.getKernel().lookupLabel("B").setLayer(3);
		app.getLayerWorkspace().setLayerHidden(3, true);
		app.getKernel().setLibraryJavaScript("function f3Library() { return 3; }");
		ConstructionDefaults defaults = app.getKernel().getConstruction()
				.getConstructionDefaults();
		GeoPoint point = (GeoPoint) defaults.getDefaultGeo(ConstructionDefaults.DEFAULT_POINT_FREE);
		point.setObjColor(GColor.RED);
		point.setPointSize(9);
		BufferedImage pixels = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = pixels.createGraphics();
		g.setColor(new java.awt.Color(0, 0, 210));
		g.fillRect(0, 0, 8, 8);
		g.dispose();
		app.getImageManager().addExternalImage("f3-picture.png", new MyImageD(pixels));
		GeoImage image = new GeoImage(app.getKernel().getConstruction());
		image.setImageFileName("f3-picture.png");
		image.setCorner(new GeoPoint(app.getKernel().getConstruction(), 20, 10, 1), 0);
		image.setCorner(new GeoPoint(app.getKernel().getConstruction(), 23, 10, 1), 1);
		image.setLabel("pic");
		image.updateRepaint();
		return app;
	}

	private static List<String> entries(byte[] archive) throws Exception {
		List<String> names = new ArrayList<>();
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				names.add(entry.getName());
			}
		}
		return names;
	}

	@Test
	void f3s01TheSnapshotIsACompleteVerifiedNativeArchive() throws Exception {
		AppGeoCeDG app = richDocument();
		GeoCeDGRecoverySnapshot snapshot = GeoCeDGRecoverySnapshot.capture(app, "op-1");
		assertEquals(GeoCeDGRecoverySnapshot.State.VERIFIED_HELD, snapshot.getState());
		byte[] archive = snapshot.getArchive();
		List<String> names = entries(archive);
		assertTrue(names.contains("geogebra.xml"), names.toString());
		assertTrue(names.contains("geogebra_macro.xml"), names.toString());
		assertTrue(names.contains("geogebra_javascript.js"), names.toString());
		assertTrue(names.contains("geogebra_defaults2d.xml"), names.toString());
		assertTrue(names.stream().anyMatch(name -> name.endsWith(".png")), names.toString());
		assertEquals(app.getUniqueId(), GeoCeDGRecoverySnapshot.documentId(archive));
		assertEquals(app.getUniqueId(), snapshot.getDocumentId());
		assertEquals("op-1", snapshot.getOperationId());
		assertNotNull(snapshot.getCapturedAt());
		assertEquals(app.isSaved(), snapshot.wasSaved());
		assertNull(snapshot.getCurrentFile());
		// a copy of the bytes: the holder's archive cannot be changed from outside
		archive[0] ^= 1;
		assertFalse(Arrays.equals(archive, snapshot.getArchive()));
	}

	@Test
	void f3s02TheRecoveryCopyReopensWithEveryResourceAndTheOriginalIdentity()
			throws Exception {
		AppGeoCeDG app = richDocument();
		String id = app.getUniqueId();
		GeoCeDGRecoverySnapshot snapshot = GeoCeDGRecoverySnapshot.capture(app, "op-2");
		File copy = dir.resolve("recovered.cedg").toFile();
		snapshot.writeCopy(copy.toPath(), app);
		assertArrayEquals(snapshot.getArchive(), Files.readAllBytes(copy.toPath()));
		AppGeoCeDG reopened = app(UnitState.EMPTY);
		assertTrue(reopened.loadFile(copy, false));
		assertEquals(id, reopened.getUniqueId());
		assertNotNull(reopened.getKernel().getMacro("MyMid"));
		assertNotNull(reopened.getKernel().lookupLabel("pic"));
		assertEquals(List.of(3), reopened.getLayerWorkspace().getHiddenLayers());
		assertEquals(app.getDocumentUnits().getState(), reopened.getDocumentUnits().getState());
		assertTrue(reopened.getKernel().getLibraryJavaScript().contains("f3Library"));
		assertEquals(GColor.RED, reopened.getKernel().getConstruction().getConstructionDefaults()
				.getDefaultGeo(ConstructionDefaults.DEFAULT_POINT_FREE).getObjectColor());
		// the session's file association is not moved by the copy
		assertNull(app.getCurrentFile());
	}

	@Test
	void f3s03ATamperedOrForeignArchiveIsNotVerified() throws Exception {
		AppGeoCeDG app = richDocument();
		byte[] archive = GeoCeDGRecoverySnapshot.capture(app, "op-3").getArchive();
		byte[] truncated = Arrays.copyOf(archive, archive.length / 3);
		assertThrows(GeoCeDGRecoverySnapshot.SnapshotException.class,
				() -> GeoCeDGRecoverySnapshot.verify(truncated, app, app.getUniqueId()));
		assertThrows(GeoCeDGRecoverySnapshot.SnapshotException.class,
				() -> GeoCeDGRecoverySnapshot.verify(archive, app, "another-document"));
		GeoCeDGRecoverySnapshot.verify(archive, app, app.getUniqueId());
	}

	@Test
	void f3s04OwnershipAndRelease() throws Exception {
		AppGeoCeDG app = richDocument();
		GeoCeDGRecoverySnapshot snapshot = GeoCeDGRecoverySnapshot.capture(app, "op-4");
		snapshot.retainForIsolation();
		assertEquals(GeoCeDGRecoverySnapshot.State.RETAINED_FOR_ISOLATION, snapshot.getState());
		assertNotNull(snapshot.getArchive());
		snapshot.release();
		assertEquals(GeoCeDGRecoverySnapshot.State.RELEASED, snapshot.getState());
		assertThrows(GeoCeDGRecoverySnapshot.SnapshotException.class, snapshot::getArchive);
		assertThrows(IllegalStateException.class, snapshot::retainForIsolation);
	}
}
