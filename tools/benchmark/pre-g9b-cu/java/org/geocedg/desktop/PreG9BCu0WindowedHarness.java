/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU0 harness scenario {@code windowed}: the windowed B02/B03 reproduction with the
 * real product start-up ({@link PreG9BCu0WindowedReproduction}). It deliberately has no
 * embedded-host lifecycle extension: the window belongs to the product, not to a test host,
 * and it stays open until the test JVM ends. Run only through
 * {@code tools/benchmark/pre-g9b-cu/run-harness.ps1 -Scenario windowed -ProductRuntime}.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "windowed")
class PreG9BCu0WindowedHarness {

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		new PreG9BCu0WindowedReproduction(work, output).run();
	}
}
