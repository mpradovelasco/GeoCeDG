/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.geocedg.desktop.GeoCeDGUserToolLibrary.Package;
import org.geogebra.common.euclidian.EuclidianConstants;
import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.event.AbstractEvent;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.euclidian.event.MouseEventD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * POST-E2-P3: the macros of the open document are offered in User tools → Document
 * tools; an entry activates the existing macro tool mode of its live {@link Macro},
 * the menu follows the document, library-registered macros stay in the library
 * sections, and stale entries fail explicitly. No macro registration, command
 * resolution, serialization or toolbar change.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PostE2P3DocumentToolsTest {
	private static final List<String> TEMPLATE_COMMANDS = List.of("SplineLength",
			"sheetISOAnLand", "sheetISOAnVert", "directDimension", "SquarebyDiagonal",
			"CirclebyD", "EllipseAxis", "pointJump", "PoliLineVisibility", "Perimeter",
			"axisDimension", "relCoor", "DuctSymbol", "SymmSymbol", "listLength",
			"listLength12", "postLocus", "ellipseVisibility", "translationCoor",
			"circArcbyAngle", "dummyRotate", "conj2mainAxesEllipse", "ellipseLength12",
			"IFPositiveSelectPoint");

	@TempDir
	Path temporary;
	private AppGeoCeDG app;
	private GeoCeDGUserToolLibrary library;

	@BeforeEach
	void setUp() throws IOException {
		app = G9U1TestApp.create();
		library = new GeoCeDGUserToolLibrary(app, temporary.resolve("tools.json"));
	}

	private static File template() {
		Path root = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		while (root != null && !root.resolve("models").toFile().isDirectory()) {
			root = root.getParent();
		}
		assertNotNull(root, "repository root");
		File file = root.resolve("models/legacy/template-v7/original/Templatev7.ggb").toFile();
		assertTrue(file.isFile(), file.toString());
		return file;
	}

	private JMenu documentMenu() {
		JMenu menu = new JMenu();
		new GeoCeDGUserTools(app, library).populate(menu);
		Component last = menu.getMenuComponent(menu.getMenuComponentCount() - 1);
		assertTrue(last instanceof JMenu, "the document tools submenu closes the menu");
		JMenu document = (JMenu) last;
		assertEquals(GeoCeDGUserTools.DOCUMENT_TOOLS_MENU, document.getName());
		assertEquals(app.layerText("UserTools.DocumentTools"), document.getText());
		return document;
	}

	private static List<String> commands(JMenu document) {
		List<String> commands = new ArrayList<>();
		for (int i = 0; i < document.getItemCount(); i++) {
			Object command = document.getItem(i)
					.getClientProperty(GeoCeDGUserTools.DOCUMENT_MACRO_COMMAND);
			if (command != null) {
				commands.add((String) command);
			}
		}
		return commands;
	}

	private static JMenuItem item(JMenu document, String command) {
		for (int i = 0; i < document.getItemCount(); i++) {
			if (command.equals(document.getItem(i)
					.getClientProperty(GeoCeDGUserTools.DOCUMENT_MACRO_COMMAND))) {
				return document.getItem(i);
			}
		}
		throw new AssertionError("no document tool " + command);
	}

	private void assertActiveMacro(Macro expected) {
		assertEquals(EuclidianConstants.MACRO_MODE_ID_OFFSET
				+ app.getKernel().getMacroID(expected), app.getMode());
		assertSame(expected, app.getKernel().getMacro(app.getMode()
				- EuclidianConstants.MACRO_MODE_ID_OFFSET));
	}

	// ---------------------------------------------------------------- A. Templatev7

	@Test
	void everyTemplateMacroIsListedAndActivatesItsOwnToolMode() {
		assertTrue(app.loadFile(template(), false));
		String xml = app.getXML();
		JMenu document = documentMenu();
		assertEquals(TEMPLATE_COMMANDS, commands(document));
		assertTrue(item(document, "CirclebyD").getText().startsWith("CirclebyD: Centre"),
				"the tool name is shown");
		for (String command : TEMPLATE_COMMANDS) {
			Macro macro = app.getKernel().getMacro(command);
			item(document, command).doClick(0);
			assertActiveMacro(macro);
			app.setMode(EuclidianConstants.MODE_MOVE);
		}
		assertEquals(24, app.getKernel().getMacroNumber(), "no registration change");
		assertEquals(xml, app.getXML(), "the menu changes no construction or macro");
	}

	@Test
	void aChosenTemplateToolBuildsItsResultFromGraphicalSelection() {
		assertTrue(app.loadFile(template(), false));
		EuclidianView view = app.getEuclidianView1();
		app.getEuclidianView1().setSize(new Dimension(800, 500));
		view.updateSize();
		view.setCoordSystem(400, 250, 50, 50);
		GeoElement a = G9U1TestApp.eval(app, "Pa=(1,1)");
		GeoElement b = G9U1TestApp.eval(app, "Pb=(3,2)");
		Macro square = app.getKernel().getMacro("SquarebyDiagonal");
		item(documentMenu(), "SquarebyDiagonal").doClick(0);
		assertActiveMacro(square);
		int before = app.getKernel().getConstruction().steps();
		click(view, a);
		click(view, b);
		assertTrue(app.getKernel().getConstruction().steps() > before, "the tool built");
		AlgoElement parent = null;
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (geo.getParentAlgorithm() instanceof AlgoMacro) {
				parent = geo.getParentAlgorithm();
			}
		}
		assertNotNull(parent, "an AlgoMacro output");
		assertSame(square, ((AlgoMacro) parent).getMacro());
		assertSame(a, parent.getInput(0));
		assertSame(b, parent.getInput(1));
	}

	private void click(EuclidianView view, GeoElement point) {
		int x = view.toScreenCoordX(((org.geogebra.common.kernel.geos.GeoPoint) point)
				.getInhomX());
		int y = view.toScreenCoordY(((org.geogebra.common.kernel.geos.GeoPoint) point)
				.getInhomY());
		EuclidianController controller = view.getEuclidianController();
		controller.wrapMouseMoved(event(MouseEvent.MOUSE_MOVED, x, y));
		controller.wrapMousePressed(event(MouseEvent.MOUSE_PRESSED, x, y));
		controller.wrapMouseReleased(event(MouseEvent.MOUSE_RELEASED, x, y));
	}

	private AbstractEvent event(int id, int x, int y) {
		return MouseEventD.wrapEvent(new MouseEvent(app.getEuclidianView1().getJPanel(),
				id, 1, 0, x, y, 1, false, MouseEvent.BUTTON1));
	}

	// ----------------------------------------------- B and F. lifecycle and staleness

	@Test
	void theMenuFollowsTheDocumentAndStaleEntriesFailExplicitly() throws Exception {
		JMenu empty = documentMenu();
		assertEquals(List.of(), commands(empty));
		assertEquals(1, empty.getItemCount());
		assertFalse(empty.getItem(0).isEnabled());
		assertEquals(app.layerText("UserTools.DocumentEmpty"), empty.getItem(0).getText());

		assertTrue(app.loadFile(template(), false));
		JMenu first = documentMenu();
		assertEquals(TEMPLATE_COMMANDS, commands(first));
		Macro firstSquare = app.getKernel().getMacro("SquarebyDiagonal");
		JMenuItem stale = item(first, "SquarebyDiagonal");

		// File > New keeps the kernel macros (GeoGebra lifecycle, unchanged): they stay
		// live tools of the new document and a save writes them, so they stay listed
		app.setSaved();
		app.fileNew();
		assertEquals(TEMPLATE_COMMANDS, commands(documentMenu()));
		assertSame(firstSquare, app.getKernel().getMacro("SquarebyDiagonal"));
		stale.doClick(0);
		assertActiveMacro(firstSquare);
		app.setMode(EuclidianConstants.MODE_MOVE);

		// Open replaces the macros: no entry survives and old entries fail explicitly
		assertTrue(app.loadFile(plainDocument(), false));
		assertEquals(List.of(), commands(documentMenu()), "no stale entries after Open");
		int mode = app.getMode();
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			stale.doClick(0);
			dialogs.verify(() -> JOptionPane.showMessageDialog(any(), any(), anyString(),
					anyInt()));
		}
		assertEquals(mode, app.getMode(), "a stale entry activates nothing");

		assertTrue(app.loadFile(template(), false));
		JMenu reopened = documentMenu();
		assertEquals(TEMPLATE_COMMANDS, commands(reopened));
		Macro secondSquare = app.getKernel().getMacro("SquarebyDiagonal");
		assertNotSame(firstSquare, secondSquare);
		try (MockedStatic<JOptionPane> dialogs = mockStatic(JOptionPane.class)) {
			stale.doClick(0);
			dialogs.verify(() -> JOptionPane.showMessageDialog(any(), any(), anyString(),
					anyInt()));
		}
		assertNotEquals(EuclidianConstants.MACRO_MODE_ID_OFFSET
				+ app.getKernel().getMacroID(secondSquare), app.getMode(),
				"an entry of the earlier document never activates its namesake");
		item(reopened, "SquarebyDiagonal").doClick(0);
		assertActiveMacro(secondSquare);

		// a macro removed from the kernel fails safely as well
		app.setMode(EuclidianConstants.MODE_MOVE);
		Macro ellipse = app.getKernel().getMacro("EllipseAxis");
		JMenuItem removed = item(reopened, "EllipseAxis");
		app.getKernel().removeMacro(ellipse);
		try (MockedStatic<JOptionPane> ignored = mockStatic(JOptionPane.class)) {
			removed.doClick(0);
		}
		assertEquals(EuclidianConstants.MODE_MOVE, app.getMode());
		assertFalse(commands(documentMenu()).contains("EllipseAxis"));
	}

	@Test
	void duplicateToolNamesAreDisambiguatedByTheirCommandNames() throws Exception {
		app.loadMacroFileFromByteArray(midpointPackage("SameTool", "MidA", "MidB"), false);
		JMenu document = documentMenu();
		assertEquals(List.of("MidA", "MidB"), commands(document));
		assertEquals("SameTool (MidA)", item(document, "MidA").getText());
		assertEquals("SameTool (MidB)", item(document, "MidB").getText());
		item(document, "MidB").doClick(0);
		assertActiveMacro(app.getKernel().getMacro("MidB"));
	}

	@Test
	void documentToolsSurviveAnUnreadableInstalledLibrary() throws Exception {
		app.loadMacroFileFromByteArray(midpointPackage(null, "DocOnly"), false);
		JMenu menu = new JMenu();
		new GeoCeDGUserTools(app, null).populate(menu);
		JMenu document = (JMenu) menu.getMenuComponent(menu.getMenuComponentCount() - 1);
		assertEquals(List.of("DocOnly"), commands(document));
	}

	// ------------------------------------------------- C. library coexistence

	@Test
	void equivalentAndSameNameLibraryToolsKeepTheirPrecedenceAndTheDocumentMacro()
			throws Exception {
		// document macro equivalent to an installed tool
		app.loadMacroFileFromByteArray(midpointPackage(null, "SharedMid"), false);
		Macro documentMacro = app.getKernel().getMacro("SharedMid");
		assertEquals(List.of("SharedMid"), commands(documentMenu()));
		item(documentMenu(), "SharedMid").doClick(0);
		assertActiveMacro(documentMacro);
		app.setMode(EuclidianConstants.MODE_MOVE);
		Package equivalent = library.install("shared.ggt", midpointPackage(null, "SharedMid"));
		// existing rule: the installed entry stays the only visible, enabled choice
		assertEquals(List.of(), commands(documentMenu()));
		library.select(equivalent.id(), "SharedMid");
		assertActiveMacro(documentMacro);
		assertEquals(1, app.getKernel().getMacroNumber(), "digest equivalence, no copy");
		library.remove(equivalent.id());
		assertEquals(List.of("SharedMid"), commands(documentMenu()),
				"without the installed entry the document macro is offered again");

		// same name, different definition: the library entry is unavailable
		app.setSaved();
		assertTrue(app.loadFile(plainDocument(), false));
		app.loadMacroFileFromByteArray(linePackage("Clash"), false);
		Macro clash = app.getKernel().getMacro("Clash");
		Package different = library.install("clash.ggt", midpointPackage(null, "Clash"));
		assertEquals("UserTools.DefinitionMismatch", library.unavailableReason(different));
		assertThrows(IOException.class, () -> library.select(different.id(), "Clash"));
		item(documentMenu(), "Clash").doClick(0);
		assertActiveMacro(clash);

		// a macro registered by the library in this session is a library tool
		Package libraryOnly = library.install("lib.ggt", midpointPackage(null, "LibOnly"));
		library.select(libraryOnly.id(), "LibOnly");
		assertTrue(library.isLibraryRegistered(app.getKernel().getMacro("LibOnly")));
		assertEquals(List.of("Clash"), commands(documentMenu()));
	}

	// -------------------------------------------------------------- D. persistence

	@Test
	void savingAndReopeningKeepsTheDocumentToolsAndTheMenuWritesNothing() throws Exception {
		assertTrue(app.loadFile(template(), false));
		Set<String> before = new LinkedHashSet<>(TEMPLATE_COMMANDS);
		item(documentMenu(), "relCoor").doClick(0);
		File saved = temporary.resolve("template.cedg").toFile();
		assertTrue(((GuiManagerGeoCeDG) app.getGuiManager()).saveAsTo(saved));
		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(saved, false));
		JMenu menu = new JMenu();
		new GeoCeDGUserTools(reopened, new GeoCeDGUserToolLibrary(reopened,
				temporary.resolve("tools2.json"))).populate(menu);
		JMenu document = (JMenu) menu.getMenuComponent(menu.getMenuComponentCount() - 1);
		assertEquals(new ArrayList<>(before), commands(document));
		assertEquals(24, reopened.getKernel().getMacroNumber());
	}

	// ------------------------------------------------------------------ helpers

	/** @return a saved document with one point and no macros */
	private File plainDocument() {
		File file = temporary.resolve("plain.cedg").toFile();
		if (!file.isFile()) {
			AppGeoCeDG source = G9U1TestApp.create();
			G9U1TestApp.eval(source, "P=(1,2)");
			assertTrue(((GuiManagerGeoCeDG) source.getGuiManager()).saveAsTo(file));
		}
		return file;
	}

	/**
	 * @param toolName shared tool name, or null to keep each command name
	 * @param names macro command names
	 * @return a macro archive of midpoint macros
	 */
	private static byte[] midpointPackage(String toolName, String... names) throws Exception {
		AppGeoCeDG source = G9U1TestApp.create();
		GeoElement a = G9U1TestApp.eval(source, "A=(0,0)");
		GeoElement b = G9U1TestApp.eval(source, "B=(2,0)");
		GeoElement m = G9U1TestApp.eval(source, "M=Midpoint(A,B)");
		ArrayList<Macro> macros = new ArrayList<>();
		for (String name : names) {
			Macro macro = new Macro(source.getKernel(), name, new GeoElement[] {a, b},
					new GeoElement[] {m});
			if (toolName != null) {
				macro.setToolName(toolName);
			}
			source.getKernel().addMacro(macro);
			macros.add(macro);
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		source.getXMLio().writeMacroStream(output, macros, new ArrayList<>());
		return output.toByteArray();
	}

	private static byte[] linePackage(String name) throws Exception {
		AppGeoCeDG source = G9U1TestApp.create();
		GeoElement a = G9U1TestApp.eval(source, "A=(0,0)");
		GeoElement b = G9U1TestApp.eval(source, "B=(2,0)");
		GeoElement line = G9U1TestApp.eval(source, "g=Line(A,B)");
		Macro macro = new Macro(source.getKernel(), name, new GeoElement[] {a, b},
				new GeoElement[] {line});
		source.getKernel().addMacro(macro);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		source.getXMLio().writeMacroStream(output, new ArrayList<>(List.of(macro)),
				new ArrayList<>());
		return output.toByteArray();
	}
}
