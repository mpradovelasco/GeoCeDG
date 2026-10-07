/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.geogebra.common.move.ggtapi.models.json.JSONArray;
import org.geogebra.common.move.ggtapi.models.json.JSONException;
import org.geogebra.common.move.ggtapi.models.json.JSONObject;

/**
 * Read-only, installation-relative curated GGT library (PRE-G9B-R6-plus-E1-L).
 *
 * <p>The directory is never written. Its integrity is all-or-nothing: any missing,
 * extra or mismatching file or manifest field makes the whole catalog unavailable.
 */
final class GeoCeDGBundledToolCatalog {

	static final String LIBRARY_ID = "geocedg.curated-ggt-library";
	static final String MANIFEST = "library-manifest.json";
	static final String TOOLS = "tools";
	static final String LAUNCHER_PROPERTY = "jpackage.app-path";
	private static final int SCHEMA_VERSION = 1;
	private static final int MAX_TOOLS = 64;
	private static final long MAX_MANIFEST_BYTES = 1024L * 1024L;

	/** One verified shipped archive; its definition is validated by the user-tool library. */
	static final class Entry {
		private final String command;
		private final String fileName;
		private final String definitionDigest;
		private final byte[] bytes;

		Entry(String command, String fileName, String definitionDigest, byte[] bytes) {
			this.command = command;
			this.fileName = fileName;
			this.definitionDigest = definitionDigest;
			this.bytes = bytes.clone();
		}

		String command() {
			return command;
		}

		String fileName() {
			return fileName;
		}

		String definitionDigest() {
			return definitionDigest;
		}

		byte[] bytes() {
			return bytes.clone();
		}
	}

	private GeoCeDGBundledToolCatalog() {
		// utility class
	}

	/**
	 * @return {@code <launcher dir>/app/ggt-library} of a jpackage launch, or null
	 *         when the application was not started by a jpackage launcher
	 */
	static Path defaultDirectory() {
		String launcher = System.getProperty(LAUNCHER_PROPERTY);
		if (launcher == null || launcher.isBlank()) {
			return null;
		}
		Path parent = Path.of(launcher).toAbsolutePath().normalize().getParent();
		return parent == null ? null : parent.resolve("app").resolve("ggt-library");
	}

	/**
	 * @param directory installation-relative library directory, or null
	 * @return verified shipped entries in manifest order; empty when the directory is
	 *         absent
	 * @throws IOException when the directory exists but is not exactly the manifest
	 *         and its shipped archives
	 */
	static List<Entry> read(Path directory) throws IOException {
		if (directory == null || !Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
			return List.of();
		}
		if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
			throw invalid();
		}
		Set<String> top = names(directory, false);
		if (!top.equals(Set.of(MANIFEST, TOOLS))) {
			throw invalid();
		}
		Path manifestPath = directory.resolve(MANIFEST);
		Path toolsPath = directory.resolve(TOOLS);
		if (!Files.isRegularFile(manifestPath, LinkOption.NOFOLLOW_LINKS)
				|| !Files.isDirectory(toolsPath, LinkOption.NOFOLLOW_LINKS)
				|| Files.size(manifestPath) > MAX_MANIFEST_BYTES) {
			throw invalid();
		}
		List<Entry> entries = new ArrayList<>();
		Set<String> shippedFiles = new TreeSet<>();
		Set<String> unshippedFiles = new HashSet<>();
		Set<String> commandKeys = new HashSet<>();
		try {
			JSONObject manifest = new JSONObject(Files.readString(manifestPath,
					StandardCharsets.UTF_8));
			if (manifest.getInt("schemaVersion") != SCHEMA_VERSION
					|| !LIBRARY_ID.equals(manifest.getString("libraryId"))
					|| manifest.getInt("libraryVersion") < 1) {
				throw invalid();
			}
			JSONArray tools = manifest.getJSONArray("tools");
			if (tools.length() == 0 || tools.length() > MAX_TOOLS) {
				throw invalid();
			}
			for (int i = 0; i < tools.length(); i++) {
				JSONObject tool = tools.getJSONObject(i);
				String command = tool.getString("command");
				String file = tool.getString("file");
				String expectedFile = TOOLS + "/" + command + ".ggt";
				if (!command.matches("[\\p{L}][\\p{L}\\p{N}_]{0,63}")
						|| !expectedFile.equals(file)
						|| !commandKeys.add(command.toLowerCase(Locale.ROOT))) {
					throw invalid();
				}
				String name = command + ".ggt";
				if (!tool.getBoolean("shipped")) {
					unshippedFiles.add(name);
					continue;
				}
				String sha256 = tool.getString("sha256");
				String definition = tool.getString("definitionDigest");
				if (!sha256.matches("[0-9a-f]{64}") || !definition.matches("[0-9a-f]{64}")) {
					throw invalid();
				}
				Path archive = toolsPath.resolve(name);
				if (!Files.isRegularFile(archive, LinkOption.NOFOLLOW_LINKS)
						|| Files.size(archive) > GeoCeDGUserToolLibrary.MAX_BYTES) {
					throw invalid();
				}
				byte[] bytes = Files.readAllBytes(archive);
				if (!sha256.equals(sha256(bytes))) {
					throw invalid();
				}
				shippedFiles.add(name);
				entries.add(new Entry(command, name, definition, bytes));
			}
		} catch (JSONException | IllegalArgumentException exception) {
			throw new IOException("UserTools.BundledUnavailable", exception);
		}
		Set<String> present = names(toolsPath, true);
		if (entries.isEmpty() || !present.equals(shippedFiles)) {
			// Extra files, unshipped archives and missing archives all reject the catalog.
			throw invalid();
		}
		for (String name : unshippedFiles) {
			if (present.contains(name)) {
				throw invalid();
			}
		}
		return List.copyOf(entries);
	}

	private static Set<String> names(Path directory, boolean regularFilesOnly)
			throws IOException {
		Set<String> result = new TreeSet<>();
		try (Stream<Path> children = Files.list(directory)) {
			for (Path child : (Iterable<Path>) children::iterator) {
				if (regularFilesOnly && !Files.isRegularFile(child, LinkOption.NOFOLLOW_LINKS)) {
					throw invalid();
				}
				result.add(child.getFileName().toString());
			}
		}
		return result;
	}

	private static IOException invalid() {
		return new IOException("UserTools.BundledUnavailable");
	}

	static String sha256(byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}
}
