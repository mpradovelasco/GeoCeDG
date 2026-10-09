/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.geocedg.desktop.GeoCeDGUserToolLibrary.Package;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.kernel.Macro;
import org.geogebra.common.kernel.algos.AlgoMacro;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/** PRE-G9B-R6-plus-E1-L: read-only, installation-relative, fail-closed bundled catalog. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR6PlusE1LBundledCatalogTest {

	/** Pinned meaning of each short hover tip (EN, ES lower-case fragments). */
	private static final Map<String, String[]> TIP_MEANING = new LinkedHashMap<>();

	static {
		TIP_MEANING.put("SquarebyDiagonal", new String[] {"square", "cuadrado"});
		TIP_MEANING.put("CirclebyD", new String[] {"circle from its center and diameter",
				"circunferencia"});
		TIP_MEANING.put("circArcbyAngle", new String[] {"circular arc", "arco de circunferencia"});
		TIP_MEANING.put("ellipseLength12", new String[] {"elliptic arc", "arco de elipse"});
		TIP_MEANING.put("IFPositiveSelectPoint", new String[] {"sign of a value", "signo"});
		TIP_MEANING.put("EllipseAxis", new String[] {"perpendicular semiaxes",
				"semiejes perpendiculares"});
		TIP_MEANING.put("conj2mainAxesEllipse", new String[] {"rytz", "rytz"});
		TIP_MEANING.put("pointJump", new String[] {"signed distance", "distancia con signo"});
		TIP_MEANING.put("relCoor", new String[] {"signed coordinate", "coordenada con signo"});
		TIP_MEANING.put("translationCoor", new String[] {"transfers a signed coordinate",
				"transfiere una coordenada"});
		TIP_MEANING.put("DuctSymbol", new String[] {"duct or tube symbol", "conducto o tubo"});
		TIP_MEANING.put("SymmSymbol", new String[] {"symmetry mark", "marca de simetr"});
	}

	@TempDir
	Path temporary;
	private Path repository;
	private Path preferences;
	private Path storage;
	private Path sidecar;
	private AppGeoCeDG app;

	@BeforeEach
	void setup() throws IOException {
		repository = PreG9BR6PlusE1LCuratedLibraryTest.repositoryRoot();
		preferences = Files.createDirectories(temporary.resolve("preferences"));
		storage = preferences.resolve("geocedg.properties.user-tools-v1.json");
		sidecar = GeoCeDGUserTools.bundledPinPath(storage);
		app = G9U1TestApp.create();
	}

	private Path stage(String name) throws IOException {
		Path directory = temporary.resolve(name).resolve("app").resolve("ggt-library");
		Files.createDirectories(directory.resolve("tools"));
		Path library = repository.resolve(CuratedGgtLibraryGenerator.LIBRARY_DIRECTORY);
		Files.copy(library.resolve("library-manifest.json"),
				directory.resolve("library-manifest.json"));
		try (Stream<Path> tools = Files.list(library.resolve("tools"))) {
			for (Path tool : tools.toList()) {
				Files.copy(tool, directory.resolve("tools").resolve(tool.getFileName()));
			}
		}
		return directory;
	}

	private GeoCeDGUserToolLibrary open(AppGeoCeDG host, Path directory) throws IOException {
		return new GeoCeDGUserToolLibrary(host, storage, directory, sidecar);
	}

	private static Package bundled(GeoCeDGUserToolLibrary library, String command) {
		return library.bundledPackages().stream()
				.filter(tool -> tool.commands().contains(command)).findFirst().orElseThrow();
	}

	@Test
	void e1lCatalog01AbsentLibraryIsEmptyAndWritesNothing() throws Exception {
		GeoCeDGUserToolLibrary none = open(app, null);
		GeoCeDGUserToolLibrary missing = open(app, temporary.resolve("absent/app/ggt-library"));
		for (GeoCeDGUserToolLibrary library : List.of(none, missing)) {
			assertTrue(library.bundledPackages().isEmpty());
			assertNull(library.bundledFailure());
			assertTrue(library.packages().isEmpty());
		}
		assertEquals(List.of(), listing(preferences));
		// A Gradle or IDE launch is not a jpackage launch: no installation library.
		assertNull(System.getProperty(GeoCeDGBundledToolCatalog.LAUNCHER_PROPERTY));
		assertNull(GeoCeDGBundledToolCatalog.defaultDirectory());
		String previous = System.getProperty(GeoCeDGBundledToolCatalog.LAUNCHER_PROPERTY);
		try {
			System.setProperty(GeoCeDGBundledToolCatalog.LAUNCHER_PROPERTY,
					temporary.resolve("GeoCeDG/GeoCeDG.exe").toString());
			assertEquals(temporary.resolve("GeoCeDG/app/ggt-library").toAbsolutePath()
					.normalize(), GeoCeDGBundledToolCatalog.defaultDirectory());
		} finally {
			if (previous == null) {
				System.clearProperty(GeoCeDGBundledToolCatalog.LAUNCHER_PROPERTY);
			} else {
				System.setProperty(GeoCeDGBundledToolCatalog.LAUNCHER_PROPERTY, previous);
			}
		}
	}

	@Test
	void e1lCatalog02StagedLibraryListsTheTwelveReadOnlyToolsAndActivatesEach()
			throws Exception {
		Path directory = stage("installed");
		Map<String, String> before = digests(directory);
		GeoCeDGUserToolLibrary library = open(app, directory);
		assertNull(library.bundledFailure());
		assertEquals(PreG9BR6PlusE1LCuratedLibraryTest.SELECTION, library.bundledPackages()
				.stream().map(tool -> tool.commands().get(0)).toList());
		assertTrue(library.packages().isEmpty());
		String construction = app.getXML();
		for (Package tool : library.bundledPackages()) {
			String command = tool.commands().get(0);
			assertTrue(tool.isBundled());
			assertNotNull(tool.bundledIcon(command));
			assertEquals(32, tool.bundledIcon(command).sourceWidth());
			assertNull(library.unavailableReason(tool), command);
			Macro macro = library.activate(tool.id(), command);
			assertSame(app.getKernel(), macro.getKernel());
			assertTrue(macro.getIconFileName().matches("[0-9a-f]{32}/geocedg-ggt-"
					+ command + "\\.png"));
		}
		assertEquals(12, app.getKernel().getMacroNumber());
		assertEquals(construction, app.getXML());
		assertEquals(before, digests(directory));
		assertEquals(List.of(), listing(preferences));
	}

	@Test
	void e1lCatalog03IntegrityIsWholeCatalogFailClosed() throws Exception {
		Map<String, Consumer<Path>> defects = new LinkedHashMap<>();
		defects.put("extra-tool", directory -> write(directory.resolve("tools/Extra.ggt"),
				new byte[] {1}));
		defects.put("extra-top", directory -> write(directory.resolve("README.md"),
				"x".getBytes(StandardCharsets.UTF_8)));
		defects.put("missing", directory -> delete(directory.resolve("tools/CirclebyD.ggt")));
		defects.put("hash", directory -> {
			Path file = directory.resolve("tools/SymmSymbol.ggt");
			byte[] bytes = read(file);
			bytes[bytes.length - 30] ^= 1;
			write(file, bytes);
		});
		defects.put("digest", directory -> editManifest(directory, tool -> {
			if ("pointJump".equals(tool.getString("command"))) {
				tool.put("definitionDigest", "0".repeat(64));
			}
		}));
		defects.put("unshipped-present", directory -> editManifest(directory, tool -> {
			if ("relCoor".equals(tool.getString("command"))) {
				tool.put("shipped", false);
			}
		}));
		defects.put("library-id", directory -> {
			try {
				Path manifest = directory.resolve("library-manifest.json");
				write(manifest, Files.readString(manifest).replace(
						GeoCeDGBundledToolCatalog.LIBRARY_ID, "other.library")
						.getBytes(StandardCharsets.UTF_8));
			} catch (IOException exception) {
				throw new IllegalStateException(exception);
			}
		});
		defects.put("malformed", directory -> write(directory.resolve("library-manifest.json"),
				"{".getBytes(StandardCharsets.UTF_8)));
		for (Map.Entry<String, Consumer<Path>> defect : defects.entrySet()) {
			Path directory = stage(defect.getKey());
			defect.getValue().accept(directory);
			GeoCeDGUserToolLibrary library = open(app, directory);
			assertTrue(library.bundledPackages().isEmpty(), defect.getKey());
			assertEquals("UserTools.BundledUnavailable", library.bundledFailure(),
					defect.getKey());
		}
		assertEquals(0, app.getKernel().getMacroNumber());
		assertEquals(List.of(), listing(preferences));
	}

	@Test
	void e1lCatalog04CuratedToolsConstructWithTheAuthorStyle() throws Exception {
		GeoCeDGUserToolLibrary library = open(app, stage("installed"));
		Macro circle = library.activate(bundled(library, "CirclebyD").id(), "CirclebyD");
		GeoPoint center = (GeoPoint) G9U1TestApp.eval(app, "A=(1,1)");
		GeoNumeric diameter = (GeoNumeric) G9U1TestApp.eval(app, "d=4");
		GeoElement conic = app.getKernel().useMacro(new String[] {"c"}, circle,
				new GeoElement[] {center, diameter})[0];
		assertInstanceOf(GeoConic.class, conic);
		assertInstanceOf(AlgoMacro.class, conic.getParentAlgorithm());
		assertEquals(2, ((GeoConic) conic).getHalfAxis(0), 1e-12);
		assertBlackThree(conic);

		Macro square = library.activate(bundled(library, "SquarebyDiagonal").id(),
				"SquarebyDiagonal");
		GeoPoint first = (GeoPoint) G9U1TestApp.eval(app, "P=(0,0)");
		GeoPoint second = (GeoPoint) G9U1TestApp.eval(app, "Q=(4,0)");
		GeoElement[] outputs = app.getKernel().useMacro(
				new String[] {"s1", "s2", "s3", "s4", "V1", "V2"}, square,
				new GeoElement[] {first, second});
		int segments = 0;
		for (GeoElement output : outputs) {
			if (output instanceof GeoSegment) {
				segments++;
				assertBlackThree(output);
				assertEquals(Math.sqrt(8), ((GeoSegment) output).getLength(), 1e-12);
			}
		}
		assertEquals(4, segments);
	}

	@Test
	void e1lCatalog05UserPackagesAndDocumentsKeepPrecedence() throws Exception {
		Path directory = stage("installed");
		GeoCeDGUserToolLibrary library = open(app, directory);
		Package userCircle = library.install("my-circle.ggt", variantCircle());
		assertFalse(userCircle.isBundled());
		Package bundledCircle = bundled(library, "CirclebyD");
		assertEquals("UserTools.BundledShadowed", library.unavailableReason(bundledCircle));
		IOException shadowed = assertThrows(IOException.class,
				() -> library.activate(bundledCircle.id(), "CirclebyD"));
		assertEquals("UserTools.BundledShadowed", shadowed.getMessage());
		assertNotNull(library.activate(userCircle.id(), "CirclebyD"));
		assertNull(library.unavailableReason(bundled(library, "EllipseAxis")));

		// A document that owns a different same-name definition wins over the bundle.
		AppGeoCeDG document = G9U1TestApp.create();
		document.getXMLio().processXMLString(macroXml(variantCircle()), false, true, false);
		GeoCeDGUserToolLibrary documentLibrary = new GeoCeDGUserToolLibrary(document,
				temporary.resolve("other-store.json"), directory, null);
		assertEquals("UserTools.DefinitionMismatch",
				documentLibrary.unavailableReason(bundled(documentLibrary, "CirclebyD")));

		// A document holding the curated definition itself is adopted, never duplicated.
		AppGeoCeDG adopted = G9U1TestApp.create();
		byte[] curated = Files.readAllBytes(directory.resolve("tools/CirclebyD.ggt"));
		adopted.getXMLio().processXMLString(macroXml(curated), false, true, false);
		Macro embedded = adopted.getKernel().getMacro("CirclebyD");
		GeoCeDGUserToolLibrary adoptedLibrary = new GeoCeDGUserToolLibrary(adopted,
				temporary.resolve("third-store.json"), directory, null);
		Package adoptedCircle = bundled(adoptedLibrary, "CirclebyD");
		assertNull(adoptedLibrary.unavailableReason(adoptedCircle));
		assertSame(embedded, adoptedLibrary.activate(adoptedCircle.id(), "CirclebyD"));
		assertEquals(1, adopted.getKernel().getMacroNumber());
	}

	@Test
	void e1lCatalog06ExistingUserStoresAreReadAndKeptByteForByte() throws Exception {
		byte[] user = variantCircle();
		String id = CuratedGgtLibraryGenerator.sha256(user);
		String ggt = Base64.getEncoder().encodeToString(user);
		Map<String, String> stores = new LinkedHashMap<>();
		stores.put("v1", "{\"version\":1,\"packages\":[{\"name\":\"user.ggt\",\"sha256\":\""
				+ id + "\",\"ggt\":\"" + ggt + "\",\"pinned\":[\"CirclebyD\"]}]}");
		stores.put("v2", "{\"version\":2,\"packages\":[{\"name\":\"user.ggt\",\"sha256\":\""
				+ id + "\",\"ggt\":\"" + ggt + "\",\"pinned\":[{\"command\":\"CirclebyD\","
				+ "\"group\":\"Mine\",\"order\":3}]}]}");
		GeoCeDGUserToolLibrary writer = new GeoCeDGUserToolLibrary(app,
				temporary.resolve("v3-source.json"));
		writer.pin(writer.install("user.ggt", user).id(), "CirclebyD", true);
		stores.put("v3", Files.readString(temporary.resolve("v3-source.json")));
		Path directory = stage("installed");
		for (Map.Entry<String, String> store : stores.entrySet()) {
			Files.deleteIfExists(sidecar);
			Files.writeString(storage, store.getValue(), StandardCharsets.UTF_8);
			final byte[] before = Files.readAllBytes(storage);
			GeoCeDGUserToolLibrary library = open(app, directory);
			assertEquals(1, library.packages().size(), store.getKey());
			assertEquals("CirclebyD", library.pinnedCommands().get(0).command());
			Package ellipse = bundled(library, "EllipseAxis");
			library.activate(ellipse.id(), "EllipseAxis");
			// Pinning and unpinning a bundled tool touch only the sidecar.
			library.pin(ellipse.id(), "EllipseAxis", true);
			library.pin(ellipse.id(), "EllipseAxis", false);
			library.refresh();
			assertArrayEquals(before, Files.readAllBytes(storage), store.getKey());
			assertTrue(Files.exists(sidecar), store.getKey());
		}
	}

	@Test
	void e1lCatalog07BundledPinsLiveOnlyInTheUserSideSidecar() throws Exception {
		Files.writeString(sidecar, "{\"version\":1,\"libraryId\":\""
				+ GeoCeDGBundledToolCatalog.LIBRARY_ID + "\",\"pinned\":[{\"command\":"
				+ "\"FutureTool\",\"group\":\"\",\"order\":7}]}", StandardCharsets.UTF_8);
		Path directory = stage("installed");
		final Map<String, String> before = digests(directory);
		GeoCeDGUserToolLibrary library = open(app, directory);
		Package ellipse = bundled(library, "EllipseAxis");
		assertTrue(library.pinnedCommands().isEmpty());
		library.pin(ellipse.id(), "EllipseAxis", true);
		JSONObject written = new JSONObject(Files.readString(sidecar));
		assertEquals(1, written.getInt("version"));
		assertEquals(GeoCeDGBundledToolCatalog.LIBRARY_ID, written.getString("libraryId"));
		JSONArray pins = written.getJSONArray("pinned");
		assertEquals(2, pins.length());
		assertEquals("EllipseAxis", pins.getJSONObject(0).getString("command"));
		// The next free order skips the order kept by the dormant entry.
		assertEquals(8, pins.getJSONObject(0).getInt("order"));
		assertEquals("FutureTool", pins.getJSONObject(1).getString("command"));
		library.setPinGroup(ellipse.id(), "EllipseAxis", "Library");
		library.setPinGroup(ellipse.id(), "EllipseAxis", "");
		assertFalse(Files.exists(storage), "a bundled pin never creates the user store");
		JSONArray regrouped = new JSONObject(Files.readString(sidecar)).getJSONArray("pinned");
		assertEquals(2, regrouped.length());
		assertEquals("FutureTool", regrouped.getJSONObject(1).getString("command"));
		assertEquals(7, regrouped.getJSONObject(1).getInt("order"));
		GeoCeDGUserToolLibrary reloaded = open(app, directory);
		assertEquals(List.of("EllipseAxis"), reloaded.pinnedCommands().stream()
				.map(GeoCeDGUserToolLibrary.PinnedCommand::command).toList());
		assertEquals("UserTools.BundledReadOnly", assertThrows(IOException.class,
				() -> library.setPinIcon(ellipse.id(), "EllipseAxis", "x.png", png()))
				.getMessage());
		assertEquals("UserTools.BundledReadOnly", assertThrows(IOException.class,
				() -> library.remove(ellipse.id())).getMessage());
		Package square = bundled(library, "SquarebyDiagonal");
		assertEquals("UserTools.BundledReadOnly", assertThrows(IOException.class,
				() -> library.pin(square.id(), "SquarebyDiagonal", "x.png", png()))
				.getMessage());
		assertEquals(before, digests(directory));
	}

	@Test
	void e1lCatalog08UnreadableSidecarDisablesOnlyBundledPins() throws Exception {
		Files.writeString(sidecar, "{", StandardCharsets.UTF_8);
		GeoCeDGUserToolLibrary library = open(app, stage("installed"));
		assertEquals(12, library.bundledPackages().size());
		Package ellipse = bundled(library, "EllipseAxis");
		assertEquals("UserTools.BundledReadOnly", assertThrows(IOException.class,
				() -> library.pin(ellipse.id(), "EllipseAxis", true)).getMessage());
		assertEquals("{", Files.readString(sidecar));
		assertNotNull(library.activate(ellipse.id(), "EllipseAxis"));
		assertFalse(library.install("user.ggt", variantCircle()).isBundled());
	}

	@Test
	void e1lCatalog09DocumentsStoreTheMacroAndIconNameButNoIconBytes() throws Exception {
		Path directory = stage("installed");
		GeoCeDGUserToolLibrary library = open(app, directory);
		Package circleTool = bundled(library, "CirclebyD");
		Macro circle = library.activate(circleTool.id(), "CirclebyD");
		GeoElement conic = app.getKernel().useMacro(new String[] {"c"}, circle,
				new GeoElement[] {G9U1TestApp.eval(app, "A=(0,0)"),
						G9U1TestApp.eval(app, "d=6")})[0];
		assertTrue(conic.isDefined());
		Path saved = temporary.resolve("bundled.cedg");
		assertTrue(app.saveGeoGebraFile(saved.toFile()));
		Map<String, byte[]> entries = CuratedGgtLibraryGenerator.entries(
				Files.readAllBytes(saved));
		String macros = new String(entries.get("geogebra_macro.xml"), StandardCharsets.UTF_8);
		assertTrue(macros.contains("cmdName=\"CirclebyD\""));
		assertTrue(macros.contains("iconFile=\"" + circle.getIconFileName() + "\""));
		assertFalse(entries.containsKey(circle.getIconFileName()));

		AppGeoCeDG reopened = G9U1TestApp.create();
		assertTrue(reopened.loadFile(saved.toFile(), false));
		Macro embedded = reopened.getKernel().getMacro("CirclebyD");
		assertNotNull(embedded);
		assertEquals(circle.getIconFileName(), embedded.getIconFileName());
		GeoElement reloaded = G9U1TestApp.lookup(reopened, "c");
		assertInstanceOf(AlgoMacro.class, reloaded.getParentAlgorithm());
		assertBlackThree(reloaded);
		GeoCeDGUserToolLibrary reopenedLibrary = new GeoCeDGUserToolLibrary(reopened,
				temporary.resolve("reopened-store.json"), directory, null);
		Package reopenedTool = bundled(reopenedLibrary, "CirclebyD");
		assertNull(reopenedLibrary.unavailableReason(reopenedTool));
		assertSame(embedded, reopenedLibrary.activate(reopenedTool.id(), "CirclebyD"));
	}

	@Test
	void e1lCatalog10ManagerAndMenuPresentTheReadOnlySection() throws Exception {
		Path directory = stage("installed");
		GeoCeDGUserToolLibrary library = open(app, directory);
		GeoCeDGUserTools presentation = new GeoCeDGUserTools(app, library);
		JMenu menu = new JMenu();
		presentation.populate(menu);
		List<String> items = new ArrayList<>();
		for (int i = 0; i < menu.getItemCount(); i++) {
			JMenuItem item = menu.getItem(i);
			items.add(item == null ? "|" : item.getText());
		}
		GeoCeDGActionRegistry registry = ((GuiManagerGeoCeDG) app.getGuiManager())
				.getActionRegistry();
		String section = registry.text("UserTools.Bundled");
		int header = items.indexOf(section);
		assertTrue(header > 0, items.toString());
		assertFalse(menu.getItem(header).isEnabled());
		// POST-E2-P3: the menu ends with a separator and the Document tools submenu
		int end = items.size() - 2;
		assertEquals("|", items.get(end));
		assertEquals(registry.text("UserTools.DocumentTools"), items.get(end + 1));
		assertEquals(PreG9BR6PlusE1LCuratedLibraryTest.SELECTION,
				items.subList(header + 1, end));
		// R1 (author UX rule): the hover tip says what the tool constructs or computes,
		// from the GeoCeDG-owned bilingual profile texts; caveats stay in extended help.
		JSONObject texts = new JSONObject(Files.readString(repository.resolve(
				"apps/geocedg/application-profile.yml"), StandardCharsets.UTF_8))
				.getJSONObject("localized_text");
		for (int i = header + 1; i < end; i++) {
			JMenuItem item = menu.getItem(i);
			String command = item.getText();
			assertNotNull(item.getIcon());
			assertTrue(item.isEnabled());
			JSONObject tip = texts.getJSONObject("UserTools.BundledTip." + command);
			String english = tip.getString("en").strip();
			String spanish = tip.getString("es").strip();
			assertFalse(english.isEmpty() || spanish.isEmpty() || english.equals(spanish),
					command);
			assertEquals(registry.text("UserTools.BundledTip." + command), item.getToolTipText());
			assertTrue(item.getToolTipText().equals(english)
					|| item.getToolTipText().equals(spanish), command);
			for (String generic : List.of("GeoCeDG tool", "Utility tool", "Planar convenience",
					section, "GeoCeDG")) {
				assertFalse(english.contains(generic) || spanish.contains(generic)
						|| item.getToolTipText().contains(generic), command + " " + generic);
			}
			String[] meaning = TIP_MEANING.get(command);
			assertTrue(english.toLowerCase(java.util.Locale.ROOT).contains(meaning[0]),
					command + " en");
			assertTrue(spanish.toLowerCase(java.util.Locale.ROOT).contains(meaning[1]),
					command + " es");
		}
		assertEquals(PreG9BR6PlusE1LCuratedLibraryTest.SELECTION,
				new ArrayList<>(TIP_MEANING.keySet()));
		for (String command : List.of("pointJump", "relCoor", "translationCoor")) {
			String hover = menu.getItem(items.indexOf(command)).getToolTipText();
			for (String heavy : List.of("projection", "authority", "frame", "proyecci",
					"autoridad", "sistema de referencia")) {
				assertFalse(hover.contains(heavy), command + " " + heavy);
			}
			JSONObject caveat = texts.getJSONObject("UserTools.BundledNote." + command);
			assertTrue(caveat.getString("en").contains("does not establish a spatial projection"));
			assertTrue(caveat.getString("es").contains("no establece"));
		}
		assertTrue(texts.getJSONObject("UserTools.BundledNote.EllipseAxis").getString("en")
				.contains("perpendicular to OB"));
		assertEquals(12, presentation.managedPackages().length);

		Package ellipse = bundled(library, "EllipseAxis");
		library.pin(ellipse.id(), "EllipseAxis", true);
		JPanel pins = new JPanel();
		presentation.populatePins(pins);
		assertEquals(1, pins.getComponentCount());
		assertEquals("bundled", ((JToggleButton) pins.getComponent(0))
				.getClientProperty("geocedg.userTool.icon.source"));
		assertTrue(((JToggleButton) pins.getComponent(0)).getToolTipText()
				.endsWith(registry.text("UserTools.BundledTip.EllipseAxis")));
		JPanel manager = new JPanel();
		presentation.populateManagerPins(manager, ellipse);
		JComponent row = (JComponent) manager.getComponent(0);
		JButton icon = (JButton) row.getComponent(row.getComponentCount() - 1);
		assertFalse(icon.isEnabled(), "custom icons are user-package only");

		Path broken = stage("broken");
		Files.delete(broken.resolve("tools/DuctSymbol.ggt"));
		JMenu failed = new JMenu();
		new GeoCeDGUserTools(app, open(app, broken)).populate(failed);
		// POST-E2-P3: the separator and Document tools follow the bundled section
		assertEquals(registry.text("UserTools.DocumentTools"),
				failed.getItem(failed.getItemCount() - 1).getText());
		JMenuItem last = failed.getItem(failed.getItemCount() - 3);
		assertFalse(last.isEnabled());
		assertEquals(registry.text("UserTools.BundledUnavailable"), last.getText());
	}

	private static void assertBlackThree(GeoElement element) {
		assertEquals(3, element.getLineThickness(), element.getLabelSimple());
		GColor color = element.getObjectColor();
		assertEquals(0, color.getRed() + color.getGreen() + color.getBlue(),
				element.getLabelSimple());
	}

	private byte[] variantCircle() throws IOException {
		byte[] curated = Files.readAllBytes(repository.resolve(
				CuratedGgtLibraryGenerator.TOOLS + "/CirclebyD.ggt"));
		String xml = macroXml(curated).replace("toolHelp=\"", "toolHelp=\"user variant: ");
		return CuratedGgtLibraryGenerator.archive(xml.getBytes(StandardCharsets.UTF_8),
				"user/icon.png", png());
	}

	private static String macroXml(byte[] archive) throws IOException {
		return new String(CuratedGgtLibraryGenerator.entry(archive, "geogebra_macro.xml"),
				StandardCharsets.UTF_8);
	}

	private static byte[] png() throws IOException {
		return CuratedGgtLibraryGenerator.rasterize("<svg xmlns=\"http://www.w3.org/2000/svg\""
				+ " width=\"32\" height=\"32\" viewBox=\"0 0 32 32\"><circle cx=\"16\" cy=\"16\""
				+ " r=\"8\" fill=\"#000000\"/></svg>");
	}

	@FunctionalInterface
	private interface ToolEdit {
		void apply(JSONObject tool) throws Exception;
	}

	private static void editManifest(Path directory, ToolEdit edit) {
		try {
			Path path = directory.resolve("library-manifest.json");
			JSONObject manifest = new JSONObject(Files.readString(path));
			JSONArray tools = manifest.getJSONArray("tools");
			for (int i = 0; i < tools.length(); i++) {
				edit.apply(tools.getJSONObject(i));
			}
			write(path, manifest.toString().getBytes(StandardCharsets.UTF_8));
		} catch (Exception exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static Map<String, String> digests(Path directory) throws IOException {
		Map<String, String> result = new LinkedHashMap<>();
		try (Stream<Path> files = Files.walk(directory)) {
			for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
				result.put(directory.relativize(file).toString(),
						CuratedGgtLibraryGenerator.sha256(Files.readAllBytes(file)));
			}
		}
		return result;
	}

	private static List<String> listing(Path directory) throws IOException {
		try (Stream<Path> files = Files.list(directory)) {
			return files.map(path -> path.getFileName().toString()).sorted().toList();
		}
	}

	private static byte[] read(Path path) {
		try {
			return Files.readAllBytes(path);
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static void write(Path path, byte[] bytes) {
		try {
			Files.write(path, bytes);
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static void delete(Path path) {
		try {
			Files.delete(path);
		} catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}
}
