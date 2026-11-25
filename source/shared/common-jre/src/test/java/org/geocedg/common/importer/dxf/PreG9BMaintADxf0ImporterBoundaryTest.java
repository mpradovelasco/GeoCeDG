/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.importer.dxf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Inventory over the production sources of the importer (AGENTS section 4, contract section
 * 2): an adapter outside the kernel that imports no construction, kernel, view, export or
 * Desktop type, uses the shared pure library for evaluation, writes no file and keeps no
 * mutable static state.
 */
final class PreG9BMaintADxf0ImporterBoundaryTest {

	private static final Pattern IMPORT = Pattern.compile("(?m)^import\\s+(static\\s+)?([\\w.]+)");
	private static final Pattern MUTABLE_STATIC = Pattern.compile(
			"(?m)^\\s*(private|public|protected)?\\s*static\\s+(?!final)(?!class)(?!enum)"
					+ "(?!<)[\\w<>\\[\\], ]+\\s+\\w+\\s*(=|;)");

	private static List<Path> sources() throws IOException {
		Path directory = Paths.get("..", "common", "src", "main", "java", "org", "geocedg",
				"common", "importer", "dxf");
		assertTrue(Files.isDirectory(directory), directory.toAbsolutePath().toString());
		try (Stream<Path> files = Files.list(directory)) {
			return files.filter(path -> path.toString().endsWith(".java")).sorted()
					.collect(Collectors.toList());
		}
	}

	@Test
	void importerStaysOutsideTheKernelAndNeverWrites() throws IOException {
		List<Path> sources = sources();
		assertTrue(sources.size() >= 10);
		for (Path source : sources) {
			String text = Files.readString(source, StandardCharsets.UTF_8);
			Matcher imports = IMPORT.matcher(text);
			while (imports.find()) {
				String name = imports.group(2);
				boolean allowed = name.startsWith("java.lang.") || name.startsWith("java.util.")
						|| name.startsWith("java.nio.charset.")
						|| "java.nio.ByteBuffer".equals(name) || "java.nio.CharBuffer".equals(name)
						|| name.startsWith("java.security.")
						|| "javax.annotation.CheckForNull".equals(name)
						|| name.startsWith("org.geocedg.common.math.bspline.");
				assertTrue(allowed, source.getFileName() + " imports " + name);
			}
			for (String forbidden : new String[] {"Construction", "GeoElement",
				"org.geogebra", "org.geocedg.common.kernel", "org.geocedg.common.export",
				"FileOutputStream", "Files.write", "java.io.File"}) {
				assertFalse(text.contains(forbidden), source.getFileName() + ": " + forbidden);
			}
			assertFalse(MUTABLE_STATIC.matcher(text).find(), source + " has mutable static state");
		}
	}
}
