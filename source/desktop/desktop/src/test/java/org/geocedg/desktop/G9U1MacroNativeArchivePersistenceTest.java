/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityException;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.undo.UndoManager;
import org.geogebra.desktop.io.DocumentArchivePreflight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** Native archive coverage for document-owned macros over a G9 identity graph. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class G9U1MacroNativeArchivePersistenceTest {

	@Test
	void ellipseAxisInvocationSurvivesNativeSaveUndoAndTwoReopens(
			@TempDir Path temporary) throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		GeoLocusV2 spline = createIdentityBearingSpline(app);
		final PersistentGeoId splineId = persistentId(app, spline);
		GeoPoint center = (GeoPoint) G9U1TestApp.eval(app, "T=(0,0)");
		GeoPoint firstAxisEnd = (GeoPoint) G9U1TestApp.eval(app, "U=(4,0)");
		GeoPoint secondAxisEnd = (GeoPoint) G9U1TestApp.eval(app, "V=(0,2)");

		UndoManager undoManager = app.getKernel().getConstruction().getUndoManager();
		app.getKernel().initUndoInfo();
		awaitUndoState(undoManager, () -> !undoManager.undoPossible());

		Path storage = temporary.resolve("user-tools.json");
		GeoCeDGUserToolLibrary library = new GeoCeDGUserToolLibrary(app, storage);
		GeoCeDGUserToolLibrary.Package tool =
				library.install("ellipse-axis.ggt", ellipseAxisPackage());
		Macro macro = library.activate(tool.id(), "EllipseAxis");
		assertSame(app.getKernel(), macro.getKernel());

		GeoElement ellipse = app.getKernel().useMacro(new String[] {"ellipse1"}, macro,
				new GeoElement[] {center, firstAxisEnd, secondAxisEnd})[0];
		assertInstanceOf(GeoConic.class, ellipse);
		assertInstanceOf(AlgoMacro.class, ellipse.getParentAlgorithm());
		assertTrue(ellipse.isDefined());
		app.getKernel().storeUndoInfo();
		awaitUndoState(undoManager, undoManager::undoPossible);

		Path firstSave = temporary.resolve("ellipse-invoked.cedg");
		assertTrue(app.saveGeoGebraFile(firstSave.toFile()));
		assertTrue(DocumentArchivePreflight.validate(Files.readAllBytes(firstSave),
				new AppConfigGeoCeDG(true)));
		assertTrue(readZipEntry(firstSave, "geogebra_macro.xml")
				.contains("cmdName=\"EllipseAxis\""));
		assertTrue(readZipEntry(firstSave, "geogebra.xml")
				.contains("name=\"EllipseAxis\""));

		// The document must now own everything needed by AlgoMacro.
		library.remove(tool.id());
		assertTrue(new GeoCeDGUserToolLibrary(app, storage).packages().isEmpty());
		assertSame(macro, app.getKernel().getMacro("EllipseAxis"));

		AppGeoCeDG firstReopen = reopen(firstSave);
		assertPersistentMacroResult(firstReopen, splineId);
		GeoPoint reopenedAxisEnd = (GeoPoint) G9U1TestApp.lookup(firstReopen, "U");
		reopenedAxisEnd.setCoords(5, 0, 1);
		reopenedAxisEnd.updateRepaint();
		assertTrue(G9U1TestApp.lookup(firstReopen, "ellipse1").isDefined());

		Path secondSave = temporary.resolve("ellipse-second-save.cedg");
		assertTrue(firstReopen.saveGeoGebraFile(secondSave.toFile()));
		AppGeoCeDG secondReopen = reopen(secondSave);
		assertPersistentMacroResult(secondReopen, splineId);
		assertEquals(5, ((GeoPoint) G9U1TestApp.lookup(secondReopen, "U")).getInhomX(),
				0);

		app.getKernel().undo();
		awaitUndoState(undoManager, () -> app.getKernel().lookupLabel("ellipse1") == null
				&& undoManager.redoPossible());
		Path afterUndo = temporary.resolve("ellipse-after-undo.cedg");
		assertTrue(app.saveGeoGebraFile(afterUndo.toFile()));
		AppGeoCeDG reopenedAfterUndo = reopen(afterUndo);
		assertNull(reopenedAfterUndo.getKernel().lookupLabel("ellipse1"));
		assertNotNull(reopenedAfterUndo.getKernel().getMacro("EllipseAxis"));
		assertEquals(splineId, persistentId(reopenedAfterUndo,
				(GeoLocusV2) G9U1TestApp.lookup(reopenedAfterUndo, "S")));

		app.getKernel().redo();
		awaitUndoState(undoManager, () -> app.getKernel().lookupLabel("ellipse1") != null);
		assertTrue(G9U1TestApp.lookup(app, "ellipse1").isDefined());
	}

	@Test
	void explicitIdentityMergeRemainsForbidden() {
		AppGeoCeDG source = G9U1TestApp.create();
		createIdentityBearingSpline(source);
		String identityXml = source.getXML();
		AppGeoCeDG target = G9U1TestApp.create();

		SpatialIdentityException failure = assertThrows(SpatialIdentityException.class,
				() -> target.getXMLio().processXMLString(identityXml, false, false, false));
		assertTrue(failure.getMessage().contains("GENERIC_MERGE_FORBIDDEN"));
	}

	private static GeoLocusV2 createIdentityBearingSpline(AppGeoCeDG app) {
		G9U1TestApp.eval(app, "h=0");
		G9U1TestApp.eval(app, "A=(-2,h)");
		G9U1TestApp.eval(app, "B=(-2/3,h)");
		G9U1TestApp.eval(app, "C=(2/3,h)");
		G9U1TestApp.eval(app, "D=(2,h)");
		return (GeoLocusV2) G9U1TestApp.eval(app,
				"S=SplineV2({A,B,C,D},3)");
	}

	private static byte[] ellipseAxisPackage() throws Exception {
		AppGeoCeDG source = G9U1TestApp.create();
		GeoElement center = G9U1TestApp.eval(source, "O=(0,0)");
		GeoElement firstAxisEnd = G9U1TestApp.eval(source, "P=(4,0)");
		GeoElement secondAxisEnd = G9U1TestApp.eval(source, "Q=(0,2)");
		G9U1TestApp.eval(source, "axis=Line(O,P)");
		G9U1TestApp.eval(source, "perpendicular=PerpendicularLine(O,axis)");
		G9U1TestApp.eval(source, "r1=Distance(O,P)");
		G9U1TestApp.eval(source, "r2=Distance(O,Q)");
		G9U1TestApp.eval(source, "circle1=Circle(Q,Segment(P,O))");
		G9U1TestApp.eval(source, "circle2=Circle(P,Segment(Q,O))");
		G9U1TestApp.eval(source,
				"focus1=If(r1>r2,Intersect(circle1,axis,1),"
						+ "Intersect(circle2,perpendicular,1))");
		G9U1TestApp.eval(source,
				"focus2=If(r1>r2,Intersect(circle1,axis,2),"
						+ "Intersect(circle2,perpendicular,2))");
		GeoElement output = G9U1TestApp.eval(source,
				"toolEllipse=Ellipse(focus1,focus2,P)");
		Macro macro = new Macro(source.getKernel(), "EllipseAxis",
				new GeoElement[] {center, firstAxisEnd, secondAxisEnd},
				new GeoElement[] {output});
		source.getKernel().addMacro(macro);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		source.getXMLio().writeMacroStream(bytes,
				new ArrayList<>(List.of(macro)), new ArrayList<>());
		return bytes.toByteArray();
	}

	private static AppGeoCeDG reopen(Path path) {
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(path.toFile(), false));
		return reopened;
	}

	private static void assertPersistentMacroResult(AppGeoCeDG app,
			PersistentGeoId splineId) {
		Macro macro = app.getKernel().getMacro("EllipseAxis");
		assertNotNull(macro);
		assertSame(app.getKernel(), macro.getKernel());
		GeoElement ellipse = G9U1TestApp.lookup(app, "ellipse1");
		assertInstanceOf(GeoConic.class, ellipse);
		assertInstanceOf(AlgoMacro.class, ellipse.getParentAlgorithm());
		assertTrue(ellipse.isDefined());
		assertEquals(splineId, persistentId(app,
				(GeoLocusV2) G9U1TestApp.lookup(app, "S")));
	}

	private static PersistentGeoId persistentId(AppGeoCeDG app, GeoElement geo) {
		PersistentGeoId id = app.getKernel().getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
		assertNotNull(id, geo.getLabelSimple());
		return id;
	}

	private static String readZipEntry(Path archive, String name) throws Exception {
		try (ZipFile zip = new ZipFile(archive.toFile())) {
			ZipEntry entry = zip.getEntry(name);
			assertNotNull(entry, name);
			return new String(zip.getInputStream(entry).readAllBytes(),
					StandardCharsets.UTF_8);
		}
	}

	private static void awaitUndoState(UndoManager undoManager,
			BooleanSupplier expected) throws InterruptedException {
		long deadline = System.nanoTime() + 5_000_000_000L;
		boolean reached = false;
		while (!reached && System.nanoTime() < deadline) {
			synchronized (undoManager) {
				reached = expected.getAsBoolean();
			}
			if (!reached) {
				Thread.sleep(10);
			}
		}
		assertTrue(reached, "Desktop undo worker did not reach the expected state");
	}
}
