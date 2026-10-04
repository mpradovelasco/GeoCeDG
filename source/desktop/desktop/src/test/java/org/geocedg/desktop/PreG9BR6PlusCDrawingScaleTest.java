/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.awaitStore;
import static org.geocedg.desktop.PreG9BR6PlusA1LayerWorkspaceTest.undo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;

import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.desktop.export.DrawingScale;
import org.geocedg.desktop.export.DrawingScaleControl;
import org.geogebra.common.gui.dialog.ToolCreationDialogModel;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * PRE-G9B-R6-plus-C T-SCALE-VALUE, T-SCALE-LIFECYCLE and T-NO-SERIALIZATION: the
 * session drawing scale {@code a:b} (DQ-C6) and its per-window lifecycle (DQ-C7):
 * reset to 1:1 only at the successful-transition events E1-E6, unchanged by tool
 * and macro processing, merges, undo and failed or cancelled transitions, never
 * serialized, never an undo step and never a modification.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusCDrawingScaleTest {
	private static final DrawingScale HALF = DrawingScale.of(1, 2);

	@TempDir
	Path temporary;

	// ----------------------------------------------------------- T-SCALE-VALUE

	@Test
	void theValueIsAReducedPositiveIntegerPairWithExplicitRefusals() {
		assertEquals("1:2", DrawingScale.parse("2:4").toString());
		assertEquals(DrawingScale.parse("2:4"), DrawingScale.parse(" 1 : 2 "));
		assertEquals(DrawingScale.of(5, 10).hashCode(), DrawingScale.of(1, 2).hashCode());
		assertSame(DrawingScale.ONE_TO_ONE, DrawingScale.parse("7:7"));
		assertEquals("1:1", DrawingScale.parse("3000000000:3000000000").toString(),
				"terms are bounded after reduction");
		assertEquals("1000000000:1", DrawingScale.parse("1000000000:1").toString());
		for (String invalid : Arrays.asList("0:1", "1:0", "-1:2", "1:-2", "1:2:3", "a:b",
				"1.5:2", "", ":", "1:", ":2", "1/2", "+1:2", "1:2000000000",
				"12345678901234567890:1", "١:2")) {
			assertThrows(IllegalArgumentException.class, () -> DrawingScale.parse(invalid),
					invalid);
		}
		assertThrows(IllegalArgumentException.class, () -> DrawingScale.parse(null));
		assertEquals(Arrays.asList("1:1", "1:2", "1:5", "1:10", "2:1", "5:1"),
				DrawingScale.PRESETS.stream().map(DrawingScale::toString).toList());
		assertTrue(DrawingScale.ONE_TO_ONE.isOneToOne());
		assertEquals(1, HALF.getNumerator());
		assertEquals(2, HALF.getDenominator());
	}

	@Test
	void theControlAcceptsReducedEntriesAndRefusesInvalidOnesWithoutClamping() {
		AppGeoCeDG app = G9U1TestApp.create();
		AtomicInteger changes = new AtomicInteger();
		DrawingScaleControl control = new DrawingScaleControl(app, "scale", "unit",
				"refused", changes::incrementAndGet);
		assertEquals("1:1", control.getShownText());
		assertTrue(control.commit("10:20"));
		assertEquals(HALF, app.getDrawingScale());
		assertEquals("1:2", control.getShownText(), "shown in normal form");
		assertEquals(1, changes.get());
		assertFalse(control.commit("1:0"));
		assertEquals("refused", control.getStatusText());
		assertEquals(HALF, app.getDrawingScale(), "the previous scale is kept");
		assertEquals("1:2", control.getShownText());
		assertFalse(control.commit("1:2000000001"), "no silent clamp");
		assertEquals(HALF, app.getDrawingScale());
		assertEquals(1, changes.get());
	}

	// ------------------------------------------------------- T-NO-SERIALIZATION

	@Test
	void theScaleIsSessionStateOnly() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		eval(app, "A=(1,1)");
		UndoManagerD undo = undo(app);
		awaitStore(app, undo);
		app.setSaved();
		String xml = app.getXML();
		String undoXml = app.getKernel().getConstruction().getCurrentUndoXML(true)
				.toString();
		boolean canUndo = app.getKernel().undoPossible();
		AtomicInteger notified = new AtomicInteger();
		app.addDrawingScaleListener(notified::incrementAndGet);
		app.setDrawingScale(DrawingScale.parse("1:50"));
		assertEquals(1, notified.get());
		assertTrue(app.isSaved(), "a scale change never marks the document modified");
		assertEquals(xml, app.getXML(), "never serialized");
		assertEquals(undoXml, app.getKernel().getConstruction().getCurrentUndoXML(true)
				.toString(), "never in undo XML");
		assertEquals(canUndo, app.getKernel().undoPossible(), "no undo point");
		assertFalse(app.getXML().contains("1:50"));
		app.setDrawingScale(DrawingScale.parse("2:100"));
		assertEquals(1, notified.get(), "an equivalent pair is the same value");
		Path saved = temporary.resolve("session.cedg");
		assertTrue(app.saveGeoGebraFile(saved.toFile()));
		String archive = new String(Files.readAllBytes(saved), StandardCharsets.ISO_8859_1);
		assertFalse(archive.contains("1:50"));
		assertEquals(DrawingScale.parse("1:50"), app.getDrawingScale(),
				"saving keeps the session value");
	}

	@Test
	void everyWindowHasItsOwnValueStartingAtOneToOne() {
		AppGeoCeDG first = G9U1TestApp.create();
		AppGeoCeDG second = G9U1TestApp.create();
		first.setDrawingScale(HALF);
		assertEquals(HALF, first.getDrawingScale());
		assertEquals(DrawingScale.ONE_TO_ONE, second.getDrawingScale());
		assertNotSame(first, second);
	}

	// ---------------------------------------------------- T-SCALE-LIFECYCLE E1-E6

	@Test
	void e1ACompletedNewResetsAndACancelledNewKeeps() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setDrawingScale(HALF);
		app.fileNew();
		assertEquals(DrawingScale.ONE_TO_ONE, app.getDrawingScale(), "completed New");

		AppGeoCeDG dirty = G9U1TestApp.create();
		eval(dirty, "A=(1,1)");
		dirty.setUnsaved();
		dirty.setDrawingScale(HALF);
		assertFalse(dirty.isSaved());
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			dialogs.when(() -> JOptionPane.showOptionDialog(any(), any(), any(), anyInt(),
					anyInt(), any(), any(), any())).thenReturn(2);
			dirty.fileNew();
		}
		assertEquals(HALF, dirty.getDrawingScale(), "a cancelled New keeps the scale");
		assertTrue(dirty.getKernel().lookupLabel("A") != null);
	}

	@Test
	void e2E4NativeOpenAndLoadXmlReset() throws Exception {
		AppGeoCeDG source = G9U1TestApp.create();
		eval(source, "A=(1,1)");
		File file = temporary.resolve("native.cedg").toFile();
		assertTrue(source.saveGeoGebraFile(file));

		AppGeoCeDG app = G9U1TestApp.create();
		app.setDrawingScale(HALF);
		assertTrue(app.loadFile(file, false));
		assertEquals(DrawingScale.ONE_TO_ONE, app.getDrawingScale(), "native Open");

		app.setDrawingScale(HALF);
		assertTrue(app.loadXML(source.getXML()));
		assertEquals(DrawingScale.ONE_TO_ONE, app.getDrawingScale(), "loadXML(String)");

		app.setDrawingScale(HALF);
		app.getGgbApi().openFile(file.toURI().toString());
		assertEquals(DrawingScale.ONE_TO_ONE, app.getDrawingScale(), "API openFile");
	}

	@Test
	void e3NonNativeReplacementsReset() throws Exception {
		AppGeoCeDG source = G9U1TestApp.create();
		eval(source, "A=(1,1)");
		File file = temporary.resolve("replacement.cedg").toFile();
		assertTrue(source.saveGeoGebraFile(file));
		byte[] archive = Files.readAllBytes(file.toPath());
		String base64 = java.util.Base64.getEncoder().encodeToString(archive);
		Path bin = temporary.resolve("replacement.bin");
		Files.write(bin, archive);
		Path html = temporary.resolve("replacement.html");
		Files.write(html, ("<html><body><param name='ggbBase64' value='" + base64
				+ "'/></body></html>").getBytes(StandardCharsets.UTF_8));
		AppGeoCeDG app = G9U1TestApp.create();
		Runnable[] routes = {() -> app.getGgbApi().setBase64(base64),
				() -> app.loadBase64File(html.toFile()),
				() -> app.getGgbApi().openFile(bin.toUri().toString()),
				() -> app.loadXML(bin.toFile(), false)};
		String[] names = {"setBase64", ".html", "openFile non-native", "loadXML(File)"};
		for (int index = 0; index < routes.length; index++) {
			app.setDrawingScale(HALF);
			routes[index].run();
			assertEquals(DrawingScale.ONE_TO_ONE, app.getDrawingScale(), names[index]);
			assertTrue(app.getKernel().lookupLabel("A") != null, names[index]);
		}
	}

	@Test
	void e5TheApiSetXmlResetsAndAToolReplacementReloadDoesNot() {
		AppGeoCeDG app = G9U1TestApp.create();
		eval(app, "A=(1,1)");
		eval(app, "B=(2,2)");
		eval(app, "f=Line(A,B)");
		String xml = app.getXML();
		app.setDrawingScale(HALF);
		app.getGgbApi().setXML(xml);
		assertEquals(DrawingScale.ONE_TO_ONE, app.getDrawingScale(), "API setXML");

		app.setDrawingScale(HALF);
		app.setXML(app.getXML(), true);
		assertEquals(HALF, app.getDrawingScale(),
				"the clearing reload of a tool replacement carries no marker");

		assertTrue(buildTool(app, "WorkLine"), "first tool");
		app.setDrawingScale(HALF);
		assertTrue(buildTool(app, "WorkLine"), "replacement through the shared model");
		assertEquals(HALF, app.getDrawingScale(), "tool replacement keeps the scale");
	}

	@Test
	void e6AResetToABlankDocumentResetsAndAFileResetRelyOnTheLoad() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setDrawingScale(HALF);
		app.getGgbApi().reset();
		assertEquals(DrawingScale.ONE_TO_ONE, app.getDrawingScale(), "blank reset");

		AppGeoCeDG source = G9U1TestApp.create();
		eval(source, "A=(1,1)");
		File file = temporary.resolve("reset.cedg").toFile();
		assertTrue(source.saveGeoGebraFile(file));
		AppGeoCeDG withFile = G9U1TestApp.create();
		assertTrue(withFile.loadFile(file, false));
		withFile.setDrawingScale(HALF);
		withFile.reset();
		assertEquals(DrawingScale.ONE_TO_ONE, withFile.getDrawingScale(),
				"the reload of the current file is the transition");
	}

	@Test
	void mergesUndoClearsAndFailedLoadsKeepTheScale() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		eval(app, "A=(1,1)");
		UndoManagerD undo = undo(app);
		awaitStore(app, undo);
		app.setDrawingScale(HALF);
		app.getGgbApi().evalXML("<element type=\"point\" label=\"E\"><coords x=\"1\" "
				+ "y=\"1\" z=\"1\"/></element>");
		assertEquals(HALF, app.getDrawingScale(), "evalXML");
		app.setXML("<geogebra format=\"5.0\"><construction><element type=\"point\" "
				+ "label=\"F\"><coords x=\"2\" y=\"2\" z=\"1\"/></element></construction>"
				+ "</geogebra>", false);
		assertEquals(HALF, app.getDrawingScale(), "non-clearing setXML");
		eval(app, "G=(3,3)");
		awaitStore(app, undo);
		app.getKernel().undo();
		assertEquals(HALF, app.getDrawingScale(), "undo");
		app.getKernel().redo();
		assertEquals(HALF, app.getDrawingScale(), "redo");
		eval(app, "A=(5,5)");
		assertEquals(HALF, app.getDrawingScale(), "redefine");
		app.setSaved();
		assertTrue(app.clearConstruction());
		assertEquals(HALF, app.getDrawingScale(),
				"clearConstruction alone is never a reset point");
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			assertFalse(app.loadXML("<geogebra format=\"5.0\"><construction><broken"),
					"a failed load");
		}
		assertEquals(HALF, app.getDrawingScale(), "failed load");
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, null, null));
		assertEquals(HALF, app.getDrawingScale(), "a unit change keeps the scale");
	}

	@Test
	void preferenceReloadsKeepTheScale() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setDrawingScale(HALF);
		app.setXML("<geogebra format=\"5.0\"><gui><font size=\"16\"/></gui></geogebra>",
				true);
		assertEquals(HALF, app.getDrawingScale(),
				"a clearing preference XML without the API marker");
		org.geogebra.desktop.main.GeoGebraPreferencesD.getPref().loadXMLPreferences(app);
		assertEquals(HALF, app.getDrawingScale(), "installed-preferences reload");
	}

	private static boolean buildTool(AppGeoCeDG app, String name) {
		ToolCreationDialogModel builder = new ToolCreationDialogModel(app, () -> {
			// no dialog to update
		});
		builder.addToInput(app.getKernel().lookupLabel("A"));
		builder.addToInput(app.getKernel().lookupLabel("B"));
		builder.addToOutput(app.getKernel().lookupLabel("f"));
		builder.createTool();
		return builder.finish(app, name, name, "two points", false, null);
	}
}
