/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;
import org.geogebra.desktop.gui.util.JSVGImageBuilder;
import org.geogebra.desktop.gui.util.SVGImage;

/**
 * Deterministic generator of the curated GGT library (PRE-G9B-R6-plus-E1-L).
 *
 * <p>Inputs: the immutable {@code Templatev7.ggb}, the authored {@code curation.yml} and
 * the owned SVG icon sources. Outputs: one {@code .ggt} per curated tool and the
 * generated {@code library-manifest.json}. The only edits of a source macro are the
 * closed transformation set T-EXTRACT, T-ICON and T-STYLE; every edit is an exact
 * replacement that must match once.
 */
final class CuratedGgtLibraryGenerator {

	static final String LIBRARY_DIRECTORY = "models/curated/ggt-library";
	static final String CURATION = LIBRARY_DIRECTORY + "/curation.yml";
	static final String MANIFEST = LIBRARY_DIRECTORY + "/library-manifest.json";
	static final String TOOLS = LIBRARY_DIRECTORY + "/tools";
	static final int ICON_SIZE = 32;
	static final int LINE_THICKNESS = 3;
	static final LocalDateTime ARCHIVE_TIME = LocalDateTime.of(1980, 1, 1, 0, 0);
	static final Set<String> STYLED_TYPES = Set.of("line", "segment", "ray", "vector",
			"conic", "conicpart", "polygon", "polyline");
	static final List<String> TRANSFORMATIONS = List.of("T-EXTRACT", "T-ICON", "T-STYLE");
	private static final Pattern OUTPUT_LABEL = Pattern.compile(" a\\d+=\"([^\"]*)\"");
	private static final Pattern ICON_ATTRIBUTE = Pattern.compile(" iconFile=\"[^\"]*\"");
	private static final Pattern COLOR = Pattern.compile(
			"<objColor r=\"\\d+\" g=\"\\d+\" b=\"\\d+\"");
	private static final Pattern THICKNESS = Pattern.compile("<lineStyle thickness=\"\\d+\"");

	/** User-library definition digest of one archive (the product's own validation). */
	@FunctionalInterface
	interface DefinitionDigests {
		Map<String, String> of(String fileName, byte[] bytes) throws IOException;
	}

	/** One generated tool and the evidence recorded for it. */
	static final class Tool {
		final String command;
		final String sourceMacroSha256;
		final byte[] archive;
		final String archiveSha256;
		final String definitionDigest;
		final String iconSource;
		final String iconSourceSha256;
		final String iconEntry;
		final byte[] png;
		final String pngSha256;
		final List<String> styledOutputs;
		final String macroXml;
		final String sourceMacroXml;
		final JSONObject curation;

		Tool(String command, String sourceMacroSha256, byte[] archive,
				String definitionDigest, String iconSource, String iconSourceSha256,
				String iconEntry, byte[] png, List<String> styledOutputs, String macroXml,
				String sourceMacroXml, JSONObject curation) {
			this.command = command;
			this.sourceMacroSha256 = sourceMacroSha256;
			this.archive = archive;
			this.archiveSha256 = sha256(archive);
			this.definitionDigest = definitionDigest;
			this.iconSource = iconSource;
			this.iconSourceSha256 = iconSourceSha256;
			this.iconEntry = iconEntry;
			this.png = png;
			this.pngSha256 = sha256(png);
			this.styledOutputs = List.copyOf(styledOutputs);
			this.macroXml = macroXml;
			this.sourceMacroXml = sourceMacroXml;
			this.curation = curation;
		}
	}

	/** Complete generated library: repository-relative path to exact bytes. */
	static final class Library {
		final Map<String, byte[]> files;
		final List<Tool> tools;
		final JSONObject curation;

		Library(Map<String, byte[]> files, List<Tool> tools, JSONObject curation) {
			this.files = files;
			this.tools = tools;
			this.curation = curation;
		}
	}

	private CuratedGgtLibraryGenerator() {
		// utility class
	}

	static Library generate(Path repository, DefinitionDigests digests) throws IOException {
		try {
			JSONObject curation = new JSONObject(Files.readString(
					repository.resolve(CURATION), StandardCharsets.UTF_8));
			JSONObject source = curation.getJSONObject("provenance_source");
			byte[] template = Files.readAllBytes(repository.resolve(source.getString("path")));
			require(sha256(template).equals(source.getString("sha256")),
					"Templatev7.ggb differs from the recorded provenance hash");
			byte[] macroBytes = entry(template, "geogebra_macro.xml");
			require(sha256(macroBytes).equals(source.getString("macro_xml_sha256")),
					"template geogebra_macro.xml differs from the recorded hash");
			String macros = new String(macroBytes, StandardCharsets.UTF_8);
			String prefix = macros.substring(0, macros.indexOf("<macro "));
			String suffix = macros.substring(macros.lastIndexOf("</macro>") + "</macro>".length());
			Map<String, byte[]> files = new LinkedHashMap<>();
			List<Tool> tools = new ArrayList<>();
			JSONArray entries = curation.getJSONArray("tools");
			for (int i = 0; i < entries.length(); i++) {
				JSONObject item = entries.getJSONObject(i);
				Tool tool = generateTool(repository, item, macros, prefix, suffix, digests);
				tools.add(tool);
				files.put(TOOLS + "/" + tool.command + ".ggt", tool.archive);
			}
			files.put(MANIFEST, manifest(curation, tools).getBytes(StandardCharsets.UTF_8));
			return new Library(files, tools, curation);
		} catch (JSONException exception) {
			throw new IOException("Malformed curation.yml", exception);
		}
	}

	private static Tool generateTool(Path repository, JSONObject item, String macros,
			String prefix, String suffix, DefinitionDigests digests)
			throws IOException, JSONException {
		String command = item.getString("command");
		String sourceCommand = item.getString("source_command");
		String marker = "<macro cmdName=\"" + sourceCommand + "\"";
		int start = macros.indexOf(marker);
		require(start >= 0 && start == macros.lastIndexOf(marker),
				"source macro must occur exactly once: " + sourceCommand);
		int end = macros.indexOf("</macro>", start) + "</macro>".length();
		String sourceMacro = macros.substring(start, end);
		String sourceSha = sha256(sourceMacro.getBytes(StandardCharsets.UTF_8));
		require(sourceSha.equals(item.getString("source_macro_sha256")),
				"source macro differs from the rights record: " + sourceCommand);
		require(!sourceMacro.contains("<ggbscript") && !sourceMacro.contains("<javascript"),
				"selected macro carries a script element: " + sourceCommand);

		String iconSource = item.getString("icon_source");
		String svg = new String(Files.readAllBytes(repository.resolve(iconSource)),
				StandardCharsets.UTF_8).replace("\r\n", "\n");
		String svgSha = sha256(svg.getBytes(StandardCharsets.UTF_8));
		byte[] png = rasterize(svg);
		String iconEntry = md5(png) + "/geocedg-ggt-" + command + ".png";

		// T-ICON: the start tag only.
		int tagEnd = sourceMacro.indexOf('>') + 1;
		String startTag = sourceMacro.substring(0, tagEnd);
		String iconTag = replaceOnce(startTag, ICON_ATTRIBUTE,
				" iconFile=\"" + iconEntry + "\"", "iconFile of " + command);
		String macro = iconTag + sourceMacro.substring(tagEnd);

		// T-STYLE: black and GeoGebra lineThickness 3 on linear outputs; line type kept.
		List<String> styled = new ArrayList<>();
		for (String label : outputLabels(macro)) {
			Matcher element = Pattern.compile("<element type=\"([^\"]+)\" label=\""
					+ Pattern.quote(label) + "\">").matcher(macro);
			require(element.find(), "output element missing: " + command + " " + label);
			int blockStart = element.start();
			String type = element.group(1);
			require(!element.find(), "output element repeated: " + command + " " + label);
			if (!STYLED_TYPES.contains(type)) {
				continue;
			}
			int blockEnd = macro.indexOf("</element>", blockStart);
			String block = macro.substring(blockStart, blockEnd);
			String restyled = replaceOnce(block, COLOR, "<objColor r=\"0\" g=\"0\" b=\"0\"",
					"objColor of " + command + " " + label);
			restyled = replaceOnce(restyled, THICKNESS,
					"<lineStyle thickness=\"" + LINE_THICKNESS + "\"",
					"lineStyle of " + command + " " + label);
			macro = macro.substring(0, blockStart) + restyled + macro.substring(blockEnd);
			styled.add(label);
		}
		String xml = prefix + macro + suffix;
		byte[] archive = archive(xml.getBytes(StandardCharsets.UTF_8), iconEntry, png);
		Map<String, String> definition = digests.of(command + ".ggt", archive);
		require(definition.size() == 1 && definition.containsKey(command),
				"curated archive must define exactly " + command);
		return new Tool(command, sourceSha, archive, definition.get(command), iconSource,
				svgSha, iconEntry, png, styled, macro, sourceMacro, item);
	}

	static List<String> outputLabels(String macro) throws IOException {
		int start = macro.indexOf("<macroOutput ");
		require(start >= 0, "macroOutput missing");
		String tag = macro.substring(start, macro.indexOf("/>", start));
		List<String> labels = new ArrayList<>();
		Matcher label = OUTPUT_LABEL.matcher(tag);
		while (label.find()) {
			labels.add(label.group(1));
		}
		return labels;
	}

	static byte[] rasterize(String svg) throws IOException {
		SVGImage image = JSVGImageBuilder.fromContent(svg);
		// Unsupported or unparsable content falls back to a 24 x 24 placeholder.
		require(image.getWidth() == ICON_SIZE && image.getHeight() == ICON_SIZE,
				"icon must render at 32 x 32");
		BufferedImage raster = new BufferedImage(ICON_SIZE, ICON_SIZE,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = raster.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_PURE);
			graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
					RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			image.paint(graphics, 0, 0, 1.0, 1.0);
		} finally {
			graphics.dispose();
		}
		int painted = 0;
		for (int y = 0; y < ICON_SIZE; y++) {
			for (int x = 0; x < ICON_SIZE; x++) {
				if ((raster.getRGB(x, y) >>> 24) != 0) {
					painted++;
				}
			}
		}
		require(painted > 0, "icon rendered empty");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		require(ImageIO.write(raster, "png", output), "PNG writer unavailable");
		return output.toByteArray();
	}

	static byte[] archive(byte[] macroXml, String iconEntry, byte[] png) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
			zip.setMethod(ZipOutputStream.STORED);
			put(zip, "geogebra_macro.xml", macroXml);
			put(zip, iconEntry, png);
		}
		return output.toByteArray();
	}

	private static void put(ZipOutputStream zip, String name, byte[] data) throws IOException {
		ZipEntry entry = new ZipEntry(name);
		CRC32 crc = new CRC32();
		crc.update(data);
		entry.setMethod(ZipEntry.STORED);
		entry.setSize(data.length);
		entry.setCompressedSize(data.length);
		entry.setCrc(crc.getValue());
		entry.setTimeLocal(ARCHIVE_TIME);
		zip.putNextEntry(entry);
		zip.write(data);
		zip.closeEntry();
	}

	static byte[] entry(byte[] archive, String name) throws IOException {
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (name.equals(entry.getName())) {
					return zip.readAllBytes();
				}
			}
		}
		throw new IOException("archive entry missing: " + name);
	}

	static Map<String, byte[]> entries(byte[] archive) throws IOException {
		Map<String, byte[]> result = new LinkedHashMap<>();
		try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				result.put(entry.getName(), zip.readAllBytes());
			}
		}
		return result;
	}

	private static String manifest(JSONObject curation, List<Tool> tools)
			throws JSONException {
		final JSONObject source = curation.getJSONObject("provenance_source");
		final JSONObject toolchain = curation.getJSONObject("recorded_toolchain");
		StringBuilder json = new StringBuilder();
		json.append("{\n");
		field(json, 1, "schemaVersion", 1, true);
		field(json, 1, "libraryId", curation.getString("library_id"), true);
		field(json, 1, "libraryVersion", curation.getInt("library_version"), true);
		field(json, 1, "feature", curation.getString("feature"), true);
		field(json, 1, "notice", "GENERATED by CuratedGgtLibraryGenerator v1 "
				+ "(PRE-G9B-R6-plus-E1-L); never hand-edit; regenerate with "
				+ "tools/legacy/curate-ggt-library.ps1", true);
		json.append("  \"provenanceSource\": {\n");
		field(json, 2, "path", source.getString("path"), true);
		field(json, 2, "sha256", source.getString("sha256"), true);
		field(json, 2, "macroXmlSha256", source.getString("macro_xml_sha256"), false);
		json.append("  },\n");
		json.append("  \"rightsRecord\": {\n");
		field(json, 2, "path", curation.getString("rights_record"), true);
		field(json, 2, "version", curation.getInt("rights_record_version"), false);
		json.append("  },\n");
		json.append("  \"recordedToolchain\": {\n");
		field(json, 2, "javaRuntimeVersion", toolchain.getString("java_runtime_version"), true);
		field(json, 2, "javaVendor", toolchain.getString("java_vendor"), false);
		json.append("  },\n");
		json.append("  \"rasterizer\": {\n");
		field(json, 2, "route", "JSVGImageBuilder.fromContent / SVGImage.paint (EchoSVG)",
				true);
		field(json, 2, "size", ICON_SIZE, true);
		field(json, 2, "imageType", "TYPE_INT_ARGB", true);
		field(json, 2, "hints", "antialias on, render quality, stroke pure, "
				+ "color render quality; scale 1.0", true);
		field(json, 2, "encoder", "javax.imageio PNG", false);
		json.append("  },\n");
		json.append("  \"archive\": {\n");
		field(json, 2, "entries", "geogebra_macro.xml, owned icon", true);
		field(json, 2, "method", "STORED", true);
		field(json, 2, "dosTime", ARCHIVE_TIME.toString(), false);
		json.append("  },\n");
		field(json, 1, "stylePolicy",
				curation.getJSONObject("style_policy").getString("id"), true);
		json.append("  \"tools\": [\n");
		for (int i = 0; i < tools.size(); i++) {
			Tool tool = tools.get(i);
			final JSONObject item = tool.curation;
			json.append("    {\n");
			field(json, 3, "command", tool.command, true);
			field(json, 3, "file", "tools/" + tool.command + ".ggt", true);
			field(json, 3, "sha256", tool.archiveSha256, true);
			field(json, 3, "bytes", tool.archive.length, true);
			field(json, 3, "definitionDigest", tool.definitionDigest, true);
			field(json, 3, "definitionDigestVersion", 1, true);
			json.append("      \"sourceMacro\": {\n");
			field(json, 4, "package", "template-v7", true);
			field(json, 4, "command", item.getString("source_command"), true);
			field(json, 4, "sha256", tool.sourceMacroSha256, false);
			json.append("      },\n");
			json.append("      \"transformations\": ").append(array(TRANSFORMATIONS))
					.append(",\n");
			List<String> labels = new ArrayList<>();
			for (String label : tool.styledOutputs) {
				labels.add(unescapeXml(label));
			}
			json.append("      \"styledOutputs\": ").append(array(labels)).append(",\n");
			json.append("      \"icon\": {\n");
			field(json, 4, "source", tool.iconSource, true);
			field(json, 4, "sourceSha256CanonicalLf", tool.iconSourceSha256, true);
			field(json, 4, "entry", tool.iconEntry, true);
			field(json, 4, "pngSha256", tool.pngSha256, true);
			field(json, 4, "width", ICON_SIZE, true);
			field(json, 4, "height", ICON_SIZE, false);
			json.append("      },\n");
			field(json, 3, "maturity", item.getString("maturity"), true);
			field(json, 3, "rightsState", item.getString("rights_state"), true);
			field(json, 3, "licenseClass", item.getString("license_class"), true);
			field(json, 3, "shipped", item.getBoolean("shipped"), true);
			json.append("      \"statement\": ").append(item.isNull("statement") ? "null"
					: quote(item.getString("statement"))).append("\n");
			json.append(i + 1 < tools.size() ? "    },\n" : "    }\n");
		}
		json.append("  ]\n");
		json.append("}\n");
		return json.toString();
	}

	private static void field(StringBuilder json, int depth, String name, Object value,
			boolean comma) {
		json.append("  ".repeat(depth)).append(quote(name)).append(": ")
				.append(value instanceof String ? quote((String) value) : String.valueOf(value))
				.append(comma ? ",\n" : "\n");
	}

	/** GeoGebra object labels as users see them; the XML attribute text is escaped. */
	static String unescapeXml(String attribute) {
		return attribute.replace("&apos;", "'").replace("&quot;", "\"")
				.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
	}

	private static String array(List<String> values) {
		List<String> quoted = new ArrayList<>();
		for (String value : values) {
			quoted.add(quote(value));
		}
		return "[" + String.join(", ", quoted) + "]";
	}

	private static String quote(String value) {
		StringBuilder quoted = new StringBuilder("\"");
		for (char character : value.toCharArray()) {
			if (character == '"' || character == '\\') {
				quoted.append('\\').append(character);
			} else if (character < 0x20) {
				quoted.append(String.format("\\u%04x", (int) character));
			} else {
				quoted.append(character);
			}
		}
		return quoted.append('"').toString();
	}

	private static String replaceOnce(String text, Pattern pattern, String replacement,
			String what) throws IOException {
		Matcher matcher = pattern.matcher(text);
		require(matcher.find(), "no match for " + what);
		int start = matcher.start();
		int end = matcher.end();
		require(!matcher.find(), "repeated match for " + what);
		return text.substring(0, start) + replacement + text.substring(end);
	}

	private static void require(boolean condition, String message) throws IOException {
		if (!condition) {
			throw new IOException(message);
		}
	}

	static String sha256(byte[] bytes) {
		return digest("SHA-256", bytes);
	}

	private static String md5(byte[] bytes) {
		return digest("MD5", bytes);
	}

	private static String digest(String algorithm, byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance(algorithm).digest(bytes));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}
}
