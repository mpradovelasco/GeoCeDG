/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;

import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** PRE-G9B-R6-plus-E1-L: the curated library is exactly reproducible and owned. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR6PlusE1LCuratedLibraryTest {

	static final List<String> SELECTION = List.of("SquarebyDiagonal", "CirclebyD",
			"circArcbyAngle", "ellipseLength12", "IFPositiveSelectPoint", "EllipseAxis",
			"conj2mainAxesEllipse", "pointJump", "relCoor", "translationCoor", "DuctSymbol",
			"SymmSymbol");
	static final List<String> EXCLUDED = List.of("directDimension", "axisDimension",
			"sheetISOAnLand", "sheetISOAnVert", "SplineLength", "PoliLineVisibility",
			"Perimeter", "listLength", "listLength12", "postLocus", "ellipseVisibility",
			"dummyRotate");
	static final String TEMPLATE_SHA256 =
			"f62e5b7a92bcd95f10b8afda348763a57ccbd0c10dbc0c2bccc7049831ed4113";
	private static final Set<String> SVG_ELEMENTS = Set.of("svg", "g", "path", "line",
			"circle", "ellipse", "rect", "polyline", "polygon");
	private static final Pattern STYLE_VALUES = Pattern.compile(
			"<objColor r=\"\\d+\" g=\"\\d+\" b=\"\\d+\"|<lineStyle thickness=\"\\d+\"");

	@TempDir
	Path temporary;
	private Path repository;
	private GeoCeDGUserToolLibrary library;

	@BeforeEach
	void setup() throws IOException {
		repository = repositoryRoot();
		library = new GeoCeDGUserToolLibrary(G9U1TestApp.create(),
				temporary.resolve("tools.json"));
	}

	private CuratedGgtLibraryGenerator.Library generate() throws IOException {
		return CuratedGgtLibraryGenerator.generate(repository, library::definitionDigests);
	}

	@Test
	void e1l01OriginalProvenanceSourceIsUnchangedAndOnlyRead() throws Exception {
		Path template = repository.resolve(
				"models/legacy/template-v7/original/Templatev7.ggb");
		byte[] before = Files.readAllBytes(template);
		assertEquals(TEMPLATE_SHA256, CuratedGgtLibraryGenerator.sha256(before));
		generate();
		assertArrayEquals(before, Files.readAllBytes(template));
		assertTrue(Files.readString(repository.resolve(
				"models/legacy/template-v7/manifest.yml")).contains(TEMPLATE_SHA256));
	}

	@Test
	void e1l02LibraryRegeneratesByteForByte() throws Exception {
		CuratedGgtLibraryGenerator.Library first = generate();
		CuratedGgtLibraryGenerator.Library second = generate();
		assertEquals(first.files.keySet(), second.files.keySet());
		for (String path : first.files.keySet()) {
			assertArrayEquals(first.files.get(path), second.files.get(path), path);
		}
		Set<String> tracked = new TreeSet<>();
		try (Stream<Path> tools = Files.list(repository.resolve(
				CuratedGgtLibraryGenerator.TOOLS))) {
			tools.forEach(path -> tracked.add(CuratedGgtLibraryGenerator.TOOLS + "/"
					+ path.getFileName()));
		}
		tracked.add(CuratedGgtLibraryGenerator.MANIFEST);
		List<String> differences = new ArrayList<>();
		if (!tracked.equals(new TreeSet<>(first.files.keySet()))) {
			differences.add("file set " + tracked + " != " + first.files.keySet());
		}
		for (Map.Entry<String, byte[]> file : first.files.entrySet()) {
			Path trackedFile = repository.resolve(file.getKey());
			if (!Files.isRegularFile(trackedFile)
					|| !java.util.Arrays.equals(Files.readAllBytes(trackedFile),
							file.getValue())) {
				differences.add(file.getKey());
			}
		}
		if (!differences.isEmpty()) {
			Path output = repository.resolve(
					"source/desktop/desktop/build/geocedg-curated-ggt-library");
			for (Map.Entry<String, byte[]> file : first.files.entrySet()) {
				Path target = output.resolve(file.getKey());
				Files.createDirectories(target.getParent());
				Files.write(target, file.getValue());
			}
			Files.writeString(output.resolve("COMPLETE"), String.join("\n",
					first.files.keySet()) + "\n");
			fail("Curated library differs from its deterministic regeneration "
					+ differences + "; running JVM " + System.getProperty("java.runtime.version")
					+ " " + System.getProperty("java.vendor") + ", recorded "
					+ first.curation.getJSONObject("recorded_toolchain")
					+ "; regenerated set written to " + output);
		}
	}

	@Test
	void e1l03SelectionIsExactlyTheTwelveAuthorApprovedTools() throws Exception {
		CuratedGgtLibraryGenerator.Library generated = generate();
		assertEquals(SELECTION, generated.tools.stream().map(tool -> tool.command).toList());
		JSONArray manifestTools = manifest().getJSONArray("tools");
		List<String> manifestCommands = new ArrayList<>();
		for (int i = 0; i < manifestTools.length(); i++) {
			manifestCommands.add(manifestTools.getJSONObject(i).getString("command"));
			assertTrue(manifestTools.getJSONObject(i).getBoolean("shipped"));
		}
		assertEquals(SELECTION, manifestCommands);
		Set<String> lowerSelection = SELECTION.stream()
				.map(command -> command.toLowerCase(java.util.Locale.ROOT))
				.collect(Collectors.toSet());
		assertEquals(SELECTION.size(), lowerSelection.size());
		try (Stream<Path> files = Files.walk(repository.resolve(
				CuratedGgtLibraryGenerator.LIBRARY_DIRECTORY))) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				String name = file.getFileName().toString();
				String text = name.endsWith(".ggt")
						? new String(CuratedGgtLibraryGenerator.entry(
								Files.readAllBytes(file), "geogebra_macro.xml"),
								StandardCharsets.UTF_8)
						: Files.readString(file, StandardCharsets.UTF_8);
				for (String excluded : EXCLUDED) {
					assertFalse(name.contains(excluded) || text.contains("\"" + excluded + "\""),
							excluded + " appears in " + file);
				}
				if (name.endsWith(".ggt")) {
					Matcher macro = Pattern.compile("<macro cmdName=\"([^\"]+)\"").matcher(text);
					assertTrue(macro.find());
					assertEquals(name.substring(0, name.length() - 4), macro.group(1));
					assertFalse(macro.find(), "one macro per archive: " + name);
				}
			}
		}
	}

	@Test
	void e1l04ManifestMatchesFilesDigestsAndRightsRecord() throws Exception {
		JSONObject manifest = manifest();
		assertEquals(1, manifest.getInt("schemaVersion"));
		assertEquals(GeoCeDGBundledToolCatalog.LIBRARY_ID, manifest.getString("libraryId"));
		assertEquals("cedg.library.curated-ggt", manifest.getString("feature"));
		assertEquals(TEMPLATE_SHA256,
				manifest.getJSONObject("provenanceSource").getString("sha256"));
		JSONObject rights = new JSONObject(Files.readString(repository.resolve(
				"geocedg/validation/pre-g9b-r6-plus/curated-ggt-library-rights-record.json")));
		Map<String, String> rightsHashes = new LinkedHashMap<>();
		JSONArray macros = rights.getJSONArray("macros");
		for (int i = 0; i < macros.length(); i++) {
			rightsHashes.put(macros.getJSONObject(i).getString("command"),
					macros.getJSONObject(i).getString("sourceMacroSha256"));
		}
		assertEquals(SELECTION, new ArrayList<>(rightsHashes.keySet()));
		JSONArray tools = manifest.getJSONArray("tools");
		for (int i = 0; i < tools.length(); i++) {
			JSONObject tool = tools.getJSONObject(i);
			String command = tool.getString("command");
			byte[] archive = Files.readAllBytes(repository.resolve(
					CuratedGgtLibraryGenerator.LIBRARY_DIRECTORY).resolve(tool.getString("file")));
			assertEquals(CuratedGgtLibraryGenerator.sha256(archive), tool.getString("sha256"));
			assertEquals(archive.length, tool.getInt("bytes"));
			assertEquals(Map.of(command, tool.getString("definitionDigest")),
					library.definitionDigests(command + ".ggt", archive));
			assertEquals(rightsHashes.get(command),
					tool.getJSONObject("sourceMacro").getString("sha256"));
			assertEquals("experimental", tool.getString("maturity"));
			assertEquals("geocedg-software", tool.getString("licenseClass"));
			assertTrue(tool.getString("rightsState").startsWith("AUTHOR_APPROVED_INTERNAL_NC"));
			assertEquals("[\"T-EXTRACT\",\"T-ICON\",\"T-STYLE\"]",
					tool.getJSONArray("transformations").toString());
			boolean statement = List.of("EllipseAxis", "pointJump", "relCoor",
					"translationCoor").contains(command);
			assertEquals(statement, !tool.isNull("statement"), command);
		}
	}

	@Test
	void e1l05CuratedDefinitionsDifferOnlyByIconAndStyleValues() throws Exception {
		for (CuratedGgtLibraryGenerator.Tool tool : generate().tools) {
			String curated = macroOf(tool);
			assertEquals(CuratedGgtLibraryGenerator.outputLabels(tool.sourceMacroXml),
					CuratedGgtLibraryGenerator.outputLabels(curated));
			assertEquals(inputTag(tool.sourceMacroXml), inputTag(curated));
			assertEquals(normalize(tool.sourceMacroXml), normalize(curated), tool.command);
			assertFalse(curated.contains("<ggbscript") || curated.contains("<javascript"));
			Matcher start = Pattern.compile("^<macro cmdName=\"[^\"]+\" toolName=\"[^\"]*\""
					+ " toolHelp=\"[^\"]*\" iconFile=\"([^\"]*)\"").matcher(curated);
			assertTrue(start.find(), tool.command);
			assertEquals(tool.iconEntry, start.group(1));
			assertEquals(tool.sourceMacroXml.substring(0, tool.sourceMacroXml.indexOf(
					" iconFile=")), curated.substring(0, curated.indexOf(" iconFile=")),
					"command, tool name and help are verbatim");
		}
	}

	@Test
	void e1l06StylePolicyIsBlackThicknessThreeWithPreservedLineTypes() throws Exception {
		Set<String> styledCommands = new HashSet<>();
		for (CuratedGgtLibraryGenerator.Tool tool : generate().tools) {
			String curated = macroOf(tool);
			for (String label : CuratedGgtLibraryGenerator.outputLabels(curated)) {
				String block = element(curated, label);
				String source = element(tool.sourceMacroXml, label);
				String type = attribute(block, "<element type=\"([^\"]+)\"");
				if (CuratedGgtLibraryGenerator.STYLED_TYPES.contains(type)) {
					styledCommands.add(tool.command);
					assertTrue(block.contains("<objColor r=\"0\" g=\"0\" b=\"0\""), label);
					assertTrue(block.contains("<lineStyle thickness=\"3\""), label);
					String lineType = "<lineStyle thickness=\"\\d+\" type=\"(-?\\d+)\"";
					assertEquals(attribute(source, lineType), attribute(block, lineType));
					assertTrue(tool.styledOutputs.contains(label));
				} else {
					assertEquals(source, block, tool.command + " " + label);
				}
			}
		}
		assertEquals(Set.of("SquarebyDiagonal", "CirclebyD", "circArcbyAngle",
				"ellipseLength12", "EllipseAxis", "DuctSymbol", "SymmSymbol"), styledCommands);
	}

	@Test
	void e1l07ArchivesCarryOnlyTheOwnedIconAndNoThirdPartyImage() throws Exception {
		Set<String> foreignImages = new HashSet<>();
		Map<String, byte[]> templateEntries = CuratedGgtLibraryGenerator.entries(
				Files.readAllBytes(repository.resolve(
						"models/legacy/template-v7/original/Templatev7.ggb")));
		for (Map.Entry<String, byte[]> entry : templateEntries.entrySet()) {
			if (entry.getKey().toLowerCase(java.util.Locale.ROOT).endsWith(".png")) {
				foreignImages.add(CuratedGgtLibraryGenerator.sha256(entry.getValue()));
			}
		}
		try (Stream<Path> files = Files.walk(repository.resolve("source"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".png")
					&& path.toString().replace('\\', '/').contains("/src/main/resources/"))
					.toList()) {
				foreignImages.add(CuratedGgtLibraryGenerator.sha256(Files.readAllBytes(file)));
			}
		}
		assertTrue(foreignImages.size() > 100);
		for (CuratedGgtLibraryGenerator.Tool tool : generate().tools) {
			Map<String, byte[]> entries = CuratedGgtLibraryGenerator.entries(tool.archive);
			assertEquals(List.of("geogebra_macro.xml", tool.iconEntry),
					new ArrayList<>(entries.keySet()));
			assertTrue(tool.iconEntry.matches("[0-9a-f]{32}/geocedg-ggt-"
					+ Pattern.quote(tool.command) + "\\.png"));
			assertFalse(tool.iconEntry.toLowerCase(java.util.Locale.ROOT).contains("geogebra_"));
			assertFalse(foreignImages.contains(tool.pngSha256), tool.command);
			String xml = new String(entries.get("geogebra_macro.xml"), StandardCharsets.UTF_8);
			assertFalse(xml.contains("GeoGebra_button") || xml.contains("GeoGebra_icon")
					|| xml.contains("circulodiametro") || xml.contains("<javascript"));
		}
	}

	@Test
	void e1l08OwnedSvgSourcesAreInTheAllowedSubsetAndHashesAreRegistered()
			throws Exception {
		String assets = Files.readString(repository.resolve(
				"geocedg/resources/assets-manifest.yml"), StandardCharsets.UTF_8);
		JSONArray registered = new JSONObject(assets).getJSONArray("ggt_library_assets");
		Map<String, JSONObject> byCommand = new LinkedHashMap<>();
		for (int i = 0; i < registered.length(); i++) {
			byCommand.put(registered.getJSONObject(i).getString("command"),
					registered.getJSONObject(i));
		}
		assertEquals(SELECTION, new ArrayList<>(byCommand.keySet()));
		for (CuratedGgtLibraryGenerator.Tool tool : generate().tools) {
			String svg = Files.readString(repository.resolve(tool.iconSource),
					StandardCharsets.UTF_8);
			assertFalse(svg.contains("\r"), "LF only: " + tool.iconSource);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Element root = factory.newDocumentBuilder().parse(new ByteArrayInputStream(
					svg.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
			assertEquals("0 0 32 32", root.getAttribute("viewBox"));
			assertEquals("32", root.getAttribute("width"));
			assertEquals("32", root.getAttribute("height"));
			NodeList all = root.getElementsByTagName("*");
			for (int i = 0; i < all.getLength(); i++) {
				Element element = (Element) all.item(i);
				assertTrue(SVG_ELEMENTS.contains(element.getLocalName()),
						element.getLocalName() + " in " + tool.iconSource);
				for (int a = 0; a < element.getAttributes().getLength(); a++) {
					String name = element.getAttributes().item(a).getNodeName();
					assertFalse(name.contains("href") || "style".equals(name)
							|| name.startsWith("on") || "filter".equals(name), name);
				}
			}
			BufferedImage png = ImageIO.read(new ByteArrayInputStream(tool.png));
			assertEquals(32, png.getWidth());
			assertEquals(32, png.getHeight());
			JSONObject asset = byCommand.get(tool.command);
			assertEquals(tool.iconSource, asset.getJSONObject("svg_source").getString("path"));
			assertEquals(tool.iconSourceSha256,
					asset.getJSONObject("svg_source").getString("canonical_lf_sha256"));
			assertEquals(tool.iconEntry, asset.getJSONObject("png_derivative").getString("entry"));
			assertEquals(tool.pngSha256,
					asset.getJSONObject("png_derivative").getString("raw_sha256"));
			assertEquals("geocedg-documentation-or-ordinary-art",
					asset.getString("copyright_license_class"));
		}
	}

	/**
	 * E1-L boundary: no curated GGT in ordinary resources or Gradle builds, an
	 * unchanged action catalog and the experimental curated-library feature. Its
	 * original "no packaging route" clause was superseded by the author-approved
	 * PRE-G9B-R6-plus-E1-P packaging contract; since PRE-G9B-R6-plus-E1-P-X1 the
	 * method keeps its historical identifier and checks instead that the normative
	 * packaging specification owns the only, controlled curated-library route.
	 */
	@Test
	void e1l09NoPackagingRouteNoBuildChangeAndUnchangedActionCatalog() throws Exception {
		try (Stream<Path> files = Files.walk(repository.resolve("source"))) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				String path = file.toString().replace('\\', '/');
				if (path.contains("/src/main/resources/")) {
					assertFalse(path.toLowerCase(java.util.Locale.ROOT).endsWith(".ggt"), path);
				}
				if (path.endsWith(".gradle.kts") || path.endsWith(".gradle")) {
					assertFalse(Files.readString(file, StandardCharsets.UTF_8)
							.contains("models/curated"), path);
				}
			}
		}
		// PRE-G9B-R6-plus-E1-P-X1: the packaging specification, not this test, is the
		// authority of the curated route; its enforcement lives in the packaging checks.
		String packaging = Files.readString(repository.resolve(
				"geocedg/specs/packaging/windows-packaging.md"), StandardCharsets.UTF_8)
				.replaceAll("\\s+", " ");
		for (String clause : List.of("`models/curated/ggt-library/`", "`app/ggt-library/`",
				"directly below `app/ggt-library/tools/`", "membership:", "hash:", "rights:",
				"every other `.ggt` anywhere in the app-image fails the build verification",
				"any `.ggb` model", "`COMMERCIAL` excludes it")) {
			assertTrue(packaging.contains(clause), clause);
		}
		// the builder reads the library only from its canonical repository authority
		assertTrue(Files.readString(repository.resolve(
				"tools/release/build-windows-package.ps1"), StandardCharsets.UTF_8)
				.replace('\\', '/').contains("models/curated/ggt-library"));
		JSONObject profile = new JSONObject(Files.readString(repository.resolve(
				"apps/geocedg/application-profile.yml"), StandardCharsets.UTF_8));
		assertEquals(130, profile.getJSONArray("actions").length());
		String experimental = Files.readString(repository.resolve(
				"geocedg/features/experimental.yml"), StandardCharsets.UTF_8);
		int feature = experimental.indexOf("\"id\": \"cedg.library.curated-ggt\"");
		assertTrue(feature >= 0);
		String entry = experimental.substring(feature, experimental.indexOf('}', feature));
		assertTrue(entry.contains("\"maturity\": \"experimental\""));
		assertTrue(entry.contains("\"enabled_by_default\": true"));
		assertTrue(entry.contains("geocedg/specs/legacy/curated-ggt-library.md"));
		assertFalse(Files.readString(repository.resolve("geocedg/features/stable.yml"))
				.contains("cedg.library.curated-ggt"));
	}

	private static String macroOf(CuratedGgtLibraryGenerator.Tool tool) {
		assertNotNull(tool.macroXml);
		return tool.macroXml;
	}

	private static String normalize(String macro) {
		String withoutIcon = macro.replaceFirst(" iconFile=\"[^\"]*\"", " iconFile=\"\"");
		return STYLE_VALUES.matcher(withoutIcon).replaceAll("<style/>");
	}

	private static String inputTag(String macro) {
		int start = macro.indexOf("<macroInput ");
		return macro.substring(start, macro.indexOf("/>", start));
	}

	private static String element(String macro, String label) {
		Matcher start = Pattern.compile("<element type=\"[^\"]+\" label=\""
				+ Pattern.quote(label) + "\">").matcher(macro);
		assertTrue(start.find(), label);
		return macro.substring(start.start(), macro.indexOf("</element>", start.start()));
	}

	private static String attribute(String text, String regex) {
		Matcher matcher = Pattern.compile(regex).matcher(text);
		assertTrue(matcher.find(), regex);
		return matcher.group(1);
	}

	private JSONObject manifest() throws Exception {
		return new JSONObject(Files.readString(repository.resolve(
				CuratedGgtLibraryGenerator.MANIFEST), StandardCharsets.UTF_8));
	}

	static Path repositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("GeoCeDG repository root not found");
	}
}
