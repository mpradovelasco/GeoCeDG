/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.geocedg.common.kernel.units.UnitMetadataException;
import org.geocedg.common.kernel.units.UnitState;
import org.geocedg.common.kernel.units.UnitToken;
import org.geocedg.common.kernel.units.UsmDefinition;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.jre.io.MyXMLioJre;
import org.geogebra.common.main.AppConfig;
import org.geogebra.common.main.settings.config.AppConfigDefault;
import org.geogebra.common.util.debug.Log;
import org.geogebra.desktop.CommandLineArguments;
import org.geogebra.desktop.headless.AppDNoGui;
import org.geogebra.desktop.headless.GFileHandler;
import org.geogebra.desktop.io.AtomicDocumentFileWriter;
import org.geogebra.desktop.main.AppD;
import org.geogebra.desktop.main.LocalizationD;
import org.geogebra.desktop.main.undo.UndoManagerD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * PRE-G9B-R6-plus-D1 T-LOAD-RESTORE, T-ROUTES (Desktop rows), T-COMPAT-CORPUS and
 * T-CLASSIC: fail-closed File > Open with the previous live document untouched, the
 * archive-with-macros route, setXML, startup with a document, and the shared semantics
 * in the Classic diagnostic configuration.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BR6PlusD1UnitLoadTest {
	private static final UnitState INCHES = UnitState.of(UnitToken.USM, UnitToken.CM,
			UsmDefinition.of(0.0254, "inch", "in"));
	private static final String MACRO = "<macro cmdName=\"Twice\" toolName=\"Twice\""
			+ " toolHelp=\"\" iconFile=\"\" showInToolBar=\"true\" copyCaptions=\"true\">"
			+ "<macroInput a0=\"A\"/><macroOutput a0=\"B\"/><construction>"
			+ "<element type=\"point\" label=\"A\"><coords x=\"0\" y=\"0\" z=\"1\"/></element>"
			+ "<command name=\"Dilate\"><input a0=\"A\" a1=\"2\"/><output a0=\"B\"/></command>"
			+ "<element type=\"point\" label=\"B\"><coords x=\"0\" y=\"0\" z=\"1\"/></element>"
			+ "</construction></macro>";

	@TempDir
	Path temporaryDirectory;

	private static GuiManagerGeoCeDG gui(AppGeoCeDG app) {
		return (GuiManagerGeoCeDG) app.getGuiManager();
	}

	private static String withUnitLine(String documentXml, String element) {
		String stripped = documentXml.replaceAll("\\s*<geocedgUnits[^>]*/>", "");
		int start = stripped.indexOf("<construction");
		int end = stripped.indexOf('>', start) + 1;
		return stripped.substring(0, end) + "\n" + element + stripped.substring(end);
	}

	private static byte[] archive(String... entries) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
			for (int i = 0; i < entries.length; i += 2) {
				zip.putNextEntry(new ZipEntry(entries[i]));
				zip.write(entries[i + 1].getBytes(StandardCharsets.UTF_8));
				zip.closeEntry();
			}
		}
		return bytes.toByteArray();
	}

	private static List<String> entryNames(Path file) throws IOException {
		List<String> names = new ArrayList<>();
		try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(file))) {
			for (ZipEntry entry = zip.getNextEntry(); entry != null;
					entry = zip.getNextEntry()) {
				names.add(entry.getName());
			}
		}
		return names;
	}

	private static String constructionSection(String documentXml) {
		int start = documentXml.indexOf("<construction");
		int end = documentXml.indexOf("</construction>") + "</construction>".length();
		assertTrue(start >= 0 && end > start, "a construction section");
		return documentXml.substring(start, end);
	}

	private static List<File> recentFiles() {
		List<File> files = new ArrayList<>();
		for (int i = 0; i < AppD.getFileListSize(); i++) {
			files.add(AppD.getFromFileList(i));
		}
		return files;
	}

	/** A GeoCeDG-configured host whose next native undo-baseline commit fails. */
	private static final class CommitFailingApp extends AppD {
		private boolean failNextCommit;

		private CommitFailingApp() {
			super(new CommandLineArguments(new String[] {"--silent"}), null, new JPanel(), true,
					new LocalizationD(3), new AppConfigGeoCeDG(true));
			setErrorDialogsActive(false);
		}

		@Override
		protected void beforeNativeUndoBaselineCommit() {
			if (failNextCommit) {
				failNextCommit = false;
				throw new SecurityException("injected live-load failure");
			}
		}
	}

	private static AppDNoGui newHeadless(AppConfig config) {
		Log previousLogger = Log.getLogger();
		try {
			return new AppDNoGui(new LocalizationD(3), true, config);
		} finally {
			Log.setLogger(previousLogger);
		}
	}

	@Test
	void unitBearingDocumentsRoundTripThroughSaveAndOpen() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		G9U1TestApp.eval(app, "A=(1,2)");
		app.getDocumentUnits().replace(INCHES);
		Path file = temporaryDirectory.resolve("inches.cedg");
		assertTrue(gui(app).saveAsTo(file.toFile()));
		AppGeoCeDG reopened = G9U1TestApp.create();
		reopened.setSaved();
		assertTrue(reopened.loadFile(file.toFile(), false));
		assertEquals(INCHES, reopened.getDocumentUnits().getState());
		assertTrue(reopened.isSaved());
		assertEquals("(1, 2)", G9U1TestApp.lookup(reopened, "A")
				.toValueString(org.geogebra.common.kernel.StringTemplate.defaultTemplate));
	}

	@Test
	void anArchiveWithMacrosKeepsItsDocumentUnits() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		assertTrue(app.addMacroXML(MACRO));
		G9U1TestApp.eval(app, "P=(1,1)");
		app.getDocumentUnits().replace(UnitState.of(UnitToken.M, null, null));
		Path file = temporaryDirectory.resolve("macros.cedg");
		assertTrue(gui(app).saveAsTo(file.toFile()));
		assertTrue(entryNames(file).contains("geogebra_macro.xml"),
				"the archive really contains macros");
		AppGeoCeDG reopened = G9U1TestApp.create();
		reopened.setSaved();
		assertTrue(reopened.loadFile(file.toFile(), false));
		assertEquals(UnitState.of(UnitToken.M, null, null),
				reopened.getDocumentUnits().getState(),
				"applied although geogebra.xml is parsed with clearConstruction=false");
		assertTrue(reopened.getKernel().getMacro("Twice").getMacroConstruction()
				.getUnitSystem().getState().isEmpty());
	}

	@Test
	void recognizedDefectsFailClosedBeforeTheLiveDocumentIsTouched() throws Exception {
		AppGeoCeDG live = G9U1TestApp.create();
		live.setLocale(Locale.ENGLISH);
		G9U1TestApp.eval(live, "A=(1,2)");
		live.getDocumentUnits().replace(UnitState.of(UnitToken.CM, null, null));
		Path livePath = temporaryDirectory.resolve("live.cedg");
		assertTrue(gui(live).saveAsTo(livePath.toFile()));
		G9U1TestApp.eval(live, "B=(3,4)");
		live.setUndoActive(true);
		UndoManagerD undo = (UndoManagerD) live.getKernel().getConstruction()
				.getUndoManager();
		live.setUnsaved();
		List<String> messages = new ArrayList<>();
		live.setUnitLoadErrorSink(messages::add);
		String liveXml = live.getXML();
		File currentFile = live.getCurrentFile();
		File currentPath = live.getCurrentPath();
		String uniqueId = live.getUniqueId();
		List<File> recent = recentFiles();
		byte[] liveBytes = Files.readAllBytes(livePath);
		int history = undo.getHistorySize();
		String base = withUnitLine(live.getXML(), "");
		Object[][] defects = {
			{"<geocedgUnits version=\"2\" construction=\"mm\"/>",
				UnitMetadataException.Code.UNSUPPORTED_VERSION},
			{"<geocedgUnits version=\"1\" construction=\"mm\" scale=\"2\"/>",
				UnitMetadataException.Code.MALFORMED_ELEMENT},
			{"<geocedgUnits version=\"1\" construction=\"usm\"/>",
				UnitMetadataException.Code.INVALID_USM_FACTOR},
			{"<geocedgUnits version=\"1\" construction=\"mm\"/><geocedgUnits version=\"1\""
					+ " construction=\"m\"/>", UnitMetadataException.Code.DUPLICATE_ELEMENT}
		};
		List<byte[]> archives = new ArrayList<>();
		List<UnitMetadataException.Code> codes = new ArrayList<>();
		for (Object[] defect : defects) {
			archives.add(archive("geogebra.xml", withUnitLine(base, (String) defect[0])));
			codes.add((UnitMetadataException.Code) defect[1]);
		}
		archives.add(archive("geogebra.xml", base.replace("<construction",
				"<geocedgUnits version=\"1\" construction=\"mm\"/><construction")));
		codes.add(UnitMetadataException.Code.MISPLACED_ELEMENT);
		archives.add(archive("geogebra_macro.xml", "<geogebra format=\"5.0\">" + MACRO
				+ "</geogebra>", "geogebra.xml", withUnitLine(base,
						"<geocedgUnits version=\"3\"/>")));
		codes.add(UnitMetadataException.Code.UNSUPPORTED_VERSION);
		for (int i = 0; i < archives.size(); i++) {
			for (String extension : new String[] {".cedg", ".ggb"}) {
				Path file = temporaryDirectory.resolve("defect-" + i + extension);
				Files.write(file, archives.get(i));
				messages.clear();
				assertFalse(live.loadFile(file.toFile(), false), file.toString());
				assertArrayEquals(archives.get(i), Files.readAllBytes(file));
				assertArrayEquals(liveBytes, Files.readAllBytes(livePath));
				assertEquals(liveXml, live.getXML(), file.toString());
				assertEquals(UnitState.of(UnitToken.CM, null, null),
						live.getDocumentUnits().getState());
				assertEquals(currentFile, live.getCurrentFile());
				assertEquals(currentPath, live.getCurrentPath());
				assertEquals(uniqueId, live.getUniqueId());
				assertEquals(recent, recentFiles(), "the recent list is unchanged");
				assertFalse(live.isSaved());
				assertEquals(history, undo.getHistorySize());
				assertEquals(List.of(live.layerText("Units.LoadError." + codes.get(i).name(),
						file.getFileName().toString())), messages, file.toString());
			}
		}
		live.setLocale(Locale.forLanguageTag("es"));
		messages.clear();
		Path spanish = temporaryDirectory.resolve("defecto.cedg");
		Files.write(spanish, archives.get(2));
		assertFalse(live.loadFile(spanish.toFile(), false));
		assertEquals(List.of("No se puede abrir defecto.cedg: su unidad personalizada (usm)"
				+ " tiene un factor no v\u00e1lido o ausente."), messages);
	}

	@Test
	void aRejectedSetXmlRestoresTheEntryStateAndNamesTheDefect() {
		AppGeoCeDG app = G9U1TestApp.create();
		app.setLocale(Locale.ENGLISH);
		G9U1TestApp.eval(app, "A=(1,2)");
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, null, null));
		String xml = app.getXML();
		List<String> messages = new ArrayList<>();
		app.setUnitLoadErrorSink(messages::add);
		app.getGgbApi().setXML(withUnitLine(xml, "<geocedgUnits version=\"4\"/>"));
		assertEquals(UnitState.of(UnitToken.MM, null, null),
				app.getDocumentUnits().getState());
		assertEquals(1, app.getKernel().getConstruction().getGeoSetConstructionOrder().size());
		assertEquals(List.of(app.layerText("Units.LoadError.UNSUPPORTED_VERSION",
				app.layerText("Units.LoadError.Document"))), messages);
	}

	@Test
	void startupWithADocumentKeepsItsUnitsWhateverTheDefaults() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getDocumentUnits().replace(UnitState.of(UnitToken.M, null, null));
		Path file = temporaryDirectory.resolve("startup.cedg");
		assertTrue(gui(app).saveAsTo(file.toFile()));
		PreG9BR6PlusD1DocumentUnitsTest.MemoryStore store =
				new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore();
		store.values.put(GeoCeDGUnitPreferences.CONSTRUCTION_KEY, "mm");
		try (AutoCloseable override = GeoCeDGUnitPreferences.useStoreForTesting(store)) {
			AppGeoCeDG started = G9U1TestApp.withoutWindowDispatcher(new AppGeoCeDG(
					new CommandLineArguments(new String[] {"--silent", file.toString()}),
					new JPanel()));
			started.setErrorDialogsActive(false);
			assertEquals(UnitState.of(UnitToken.M, null, null),
					started.getDocumentUnits().getState());
		}
	}

	@Test
	void aLiveLoadFailureAfterTheUnitElementRestoresThePreviousUnits() throws Exception {
		AppGeoCeDG author = G9U1TestApp.create();
		G9U1TestApp.eval(author, "A=(1,2)");
		author.getDocumentUnits().replace(INCHES);
		Path file = temporaryDirectory.resolve("valid.cedg");
		assertTrue(gui(author).saveAsTo(file.toFile()));
		CommitFailingApp live = new CommitFailingApp();
		live.getKernel().getAlgebraProcessor().processAlgebraCommand("B=(3,4)", false);
		live.getKernel().getConstruction().getUnitSystem().replace(
				UnitState.of(UnitToken.CM, null, null));
		String construction = constructionSection(live.getXML());
		live.failNextCommit = true;
		assertFalse(live.loadFile(file.toFile(), false), "the live load fails after parsing");
		assertEquals(UnitState.of(UnitToken.CM, null, null),
				live.getKernel().getConstruction().getUnitSystem().getState());
		// the host rollback restores the construction exactly; its GUI section is not
		// D1's (a never-shown protocol navigation is written after the rollback)
		assertEquals(construction, constructionSection(live.getXML()));
		assertTrue(live.loadFile(file.toFile(), false));
		assertEquals(INCHES, live.getKernel().getConstruction().getUnitSystem().getState());
	}

	@Test
	void aFailedStartupLoadEndsBlankWithTheDefaultsAndNamesTheDefect() throws Exception {
		AppGeoCeDG author = G9U1TestApp.create();
		Path defect = temporaryDirectory.resolve("startup-defect.cedg");
		Files.write(defect, archive("geogebra.xml", withUnitLine(author.getXML(),
				"<geocedgUnits version=\"2\" construction=\"mm\"/>")));
		PreG9BR6PlusD1DocumentUnitsTest.MemoryStore store =
				new PreG9BR6PlusD1DocumentUnitsTest.MemoryStore();
		store.values.put(GeoCeDGUnitPreferences.CONSTRUCTION_KEY, "cm");
		// the host queues its error dialog on the EDT, so the static mock lives there
		List<String> shown = new CopyOnWriteArrayList<>();
		AtomicReference<MockedStatic<JOptionPane>> pane = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> {
			MockedStatic<JOptionPane> mock = Mockito.mockStatic(JOptionPane.class);
			mock.when(() -> JOptionPane.showConfirmDialog(any(), any(), any(), anyInt(),
					anyInt())).thenAnswer(invocation -> {
						shown.add(String.valueOf(invocation.getArgument(1, Object.class)));
						return JOptionPane.OK_OPTION;
					});
			pane.set(mock);
		});
		try (AutoCloseable override = GeoCeDGUnitPreferences.useStoreForTesting(store)) {
			AppGeoCeDG started = G9U1TestApp.withoutWindowDispatcher(new AppGeoCeDG(
					new CommandLineArguments(new String[] {"--silent", defect.toString()}),
					new JPanel()));
			started.setErrorDialogsActive(false);
			SwingUtilities.invokeAndWait(() -> { });
			assertEquals(UnitState.of(UnitToken.CM, null, null),
					started.getDocumentUnits().getState(),
					"a failed startup load ends in a blank document with the defaults");
			assertTrue(started.getKernel().getConstruction().getGeoSetConstructionOrder()
					.isEmpty());
			assertTrue(started.isSaved());
			assertEquals(1, shown.size(), String.valueOf(shown));
			assertTrue(shown.get(0).contains("startup-defect.cedg"), shown.get(0));
		} finally {
			SwingUtilities.invokeAndWait(() -> pane.get().close());
		}
	}

	@Test
	void aToolFileIgnoresTheElementAndLeavesTheDocumentUnits() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		app.getDocumentUnits().replace(UnitState.of(UnitToken.MM, null, null));
		String withElement = MACRO.replace("<construction>",
				"<construction><geocedgUnits version=\"9\" construction=\"km\"/>");
		Path tools = temporaryDirectory.resolve("tools.ggt");
		Files.write(tools, archive("geogebra_macro.xml", "<geogebra format=\"5.0\">"
				+ withElement + "</geogebra>"));
		assertTrue(app.loadFile(tools.toFile(), true));
		assertTrue(app.getKernel().getMacro("Twice").getMacroConstruction().getUnitSystem()
				.getState().isEmpty());
		assertEquals(UnitState.of(UnitToken.MM, null, null), app.getDocumentUnits().getState());
		assertFalse(app.getMacroXML(app.getKernel().getMacro("Twice"))
				.contains("geocedgUnits"), "tool XML never carries the element");
	}

	@Test
	void theClassicDiagnosticConfigurationSharesTheDocumentSemantics() throws Exception {
		AppGeoCeDG app = G9U1TestApp.create();
		G9U1TestApp.eval(app, "A=(1,2)");
		app.getDocumentUnits().replace(INCHES);
		Path file = temporaryDirectory.resolve("classic.cedg");
		assertTrue(gui(app).saveAsTo(file.toFile()));
		AppDNoGui classic = newHeadless(new AppConfigDefault());
		assertTrue(GFileHandler.loadXML(classic, Files.newInputStream(file), false));
		assertEquals(INCHES, classic.getKernel().getConstruction().getUnitSystem().getState());
		Path resaved = temporaryDirectory.resolve("classic-resaved.ggb");
		AtomicDocumentFileWriter.write(resaved, temporary ->
				((MyXMLioJre) classic.getXMLio()).writeGeoGebraFile(temporary.toFile()));
		AppDNoGui reread = newHeadless(new AppConfigDefault());
		assertTrue(GFileHandler.loadXML(reread, Files.newInputStream(resaved), false));
		assertEquals(INCHES, reread.getKernel().getConstruction().getUnitSystem().getState(),
				"Classic preserves the element on re-save");
		byte[] future = archive("geogebra.xml", withUnitLine(app.getXML(),
				"<geocedgUnits version=\"5\"/>"));
		AppDNoGui rejecting = newHeadless(new AppConfigDefault());
		UnitMetadataException failure = assertThrows(UnitMetadataException.class,
				() -> GFileHandler.loadXML(rejecting, new ByteArrayInputStream(future),
						false));
		assertSame(UnitMetadataException.Code.UNSUPPORTED_VERSION, failure.getCode());
		assertTrue(classic.getGuiManager() == null, "no unit UI in a headless Classic app");
	}
}
