/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.math.bspline;

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
 * Inventory over the production sources of the package (bspline-2d section 12.1): no
 * construction, kernel, view or Desktop import, no mutable static state, no fused
 * multiply-add and no parallelism.
 */
final class BSplineLibraryBoundaryTest {

	private static final Pattern IMPORT = Pattern.compile("(?m)^import\\s+(static\\s+)?([\\w.]+)");
	private static final Pattern MUTABLE_STATIC = Pattern.compile(
			"(?m)^\\s*(private|public|protected)?\\s*static\\s+(?!final)(?!class)(?!enum)"
					+ "(?!<)[\\w<>\\[\\], ]+\\s+\\w+\\s*(=|;)");

	private static List<Path> sources() throws IOException {
		Path directory = Paths.get("..", "common", "src", "main", "java", "org", "geocedg",
				"common", "math", "bspline");
		assertTrue(Files.isDirectory(directory), directory.toAbsolutePath().toString());
		try (Stream<Path> files = Files.list(directory)) {
			return files.filter(path -> path.toString().endsWith(".java"))
					.sorted().collect(Collectors.toList());
		}
	}

	@Test
	void productionSourcesStayPure() throws IOException {
		List<Path> sources = sources();
		assertTrue(sources.size() >= 15);
		for (Path source : sources) {
			String text = Files.readString(source, StandardCharsets.UTF_8);
			Matcher imports = IMPORT.matcher(text);
			while (imports.find()) {
				String name = imports.group(2);
				boolean allowed = name.startsWith("java.lang.") || name.startsWith("java.math.")
						|| name.startsWith("java.util.") || name.startsWith("java.nio.charset.")
						|| name.startsWith("java.security.")
						|| "javax.annotation.CheckForNull".equals(name);
				assertTrue(allowed, source.getFileName() + " imports " + name);
			}
			assertFalse(text.contains("Math.fma("), source.toString());
			assertFalse(text.contains(".parallel") || text.contains("parallelStream")
					|| text.contains("Thread"), source.toString());
			assertFalse(text.contains("Construction") || text.contains("GeoElement"),
					source.toString());
			assertFalse(MUTABLE_STATIC.matcher(text).find(), source + " has mutable static state");
		}
	}
}
