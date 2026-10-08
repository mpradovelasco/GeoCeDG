/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.geocedg.desktop.PreG9BR6CapabilityMatrix.Exclusion;
import org.geocedg.desktop.PreG9BR6CapabilityMatrix.Host;
import org.geocedg.desktop.PreG9BR6CapabilityMatrix.Inventory;
import org.geocedg.desktop.PreG9BR6CapabilityMatrix.Matrix;
import org.geocedg.desktop.PreG9BR6CapabilityMatrix.Probe;
import org.geocedg.desktop.PreG9BR6CapabilityMatrix.Row;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-R6 GGBScript capability matrix. Every row and probe of
 * {@code geocedg/validation/pre-g9b-r6/ggbscript-capability-matrix.json} names the
 * method below that executes it on its real entry surface; the completeness gate
 * checks the rows against the inventory derived from the running host.
 */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BR6CapabilityMatrixTest {

	@Test
	void completenessCoversTheDerivedInventory() {
		Matrix matrix = PreG9BR6CapabilityMatrix.load();
		Inventory inventory = inventory();

		assertEquals(PreG9BR6CapabilityMatrix.setOf("AlignedDimension", "Dilate",
				"ExportImage", "Intersect", "Length", "LinearDimension", "LocusLength", "LocusV2",
				"Mirror", "Point", "Rotate", "SplineV2", "Translate"), inventory.roles().keySet(),
				"derived inventory changed");
		assertEquals(List.of(), PreG9BR6CapabilityMatrix.violations(inventory, matrix,
				junitMethods()));
	}

	@Test
	void completenessFailsForAnUncoveredCommandFormOrCell() {
		Matrix matrix = PreG9BR6CapabilityMatrix.load();
		Inventory inventory = inventory();
		Set<String> methods = junitMethods();

		assertViolation(PreG9BR6CapabilityMatrix.violations(
				PreG9BR6CapabilityMatrix.withAddedCommand(inventory, "FutureCommand",
						List.of("[ <Point> ]")), matrix, methods),
				"MISSING_COVERAGE FutureCommand");
		assertViolation(PreG9BR6CapabilityMatrix.violations(
				PreG9BR6CapabilityMatrix.withExtraForm(inventory, "LocusV2", "[ <Future> ]"),
				matrix, methods), "FORM_UNACCOUNTED LocusV2#4");
		assertViolation(PreG9BR6CapabilityMatrix.violations(inventory,
				PreG9BR6CapabilityMatrix.withoutRow(matrix, "SplineV2#2|GGBSCRIPT|es"), methods),
				"INCOMPLETE_CELLS SplineV2#2");
		assertViolation(PreG9BR6CapabilityMatrix.violations(inventory,
				PreG9BR6CapabilityMatrix.withExclusion(matrix, new Exclusion("SplineV2",
						"SplineV2.Syntax", 1, "[ <List of Points> ]", "NO_GEOCEDG_BRANCH")),
				methods), "EXCLUSION SplineV2#1 excludes a form of a GeoCeDG-added command");
		assertViolation(PreG9BR6CapabilityMatrix.violations(inventory, matrix,
				Set.of("completenessCoversTheDerivedInventory")),
				"JUNIT LocusV2#1|ALGEBRA_INPUT|en");
	}

	@Test
	void locusV2Rows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	@Test
	void locusLengthRows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	@Test
	void splineV2Rows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	@Test
	void pointRows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	@Test
	void lengthRows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	@Test
	void intersectRows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	@Test
	void translateRows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	@Test
	void rotateRows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	@Test
	void mirrorRows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	@Test
	void dilateRows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	/** PRE-G9B-R6-plus-B: the explicit DQ-B1 refusal of an animated type. */
	@Test
	void exportImageRows(TestInfo test, @TempDir Path directory) throws Exception {
		executeRows(test, directory);
	}

	/**
	 * PRE-G9B-R6-plus-E2: the two forms of the native aligned dimension, executed in a
	 * fresh JVM so that the complete Desktop suite retains none of its hosts.
	 */
	@Test
	void alignedDimensionRows(TestInfo test, @TempDir Path directory) throws Exception {
		PreG9BR6PlusE2NativeDimensionDesktopTest.isolated(directory,
				PreG9BR6PlusE2NativeDimensionDesktopTest.MATRIX_ROWS + method(test));
	}

	/**
	 * PRE-G9B-R6-plus-E2: the four forms of the native linear dimension, executed in a
	 * fresh JVM so that the complete Desktop suite retains none of its hosts.
	 */
	@Test
	void linearDimensionRows(TestInfo test, @TempDir Path directory) throws Exception {
		PreG9BR6PlusE2NativeDimensionDesktopTest.isolated(directory,
				PreG9BR6PlusE2NativeDimensionDesktopTest.MATRIX_ROWS + method(test));
	}

	@Test
	void lookupProbesEnglish(TestInfo test, @TempDir Path directory) throws Exception {
		executeProbes(test, directory, "en");
	}

	@Test
	void lookupProbesSpanish(TestInfo test, @TempDir Path directory) throws Exception {
		executeProbes(test, directory, "es");
	}

	private static void executeRows(TestInfo test, Path directory) throws Exception {
		executeRows(method(test), directory);
	}

	/**
	 * Executes every row that names the given method of this class.
	 *
	 * @param method JUnit method named by the rows
	 * @param directory temporary document directory
	 * @throws Exception on host failure
	 */
	static void executeRows(String method, Path directory) throws Exception {
		Matrix matrix = PreG9BR6CapabilityMatrix.load();
		List<Row> rows = PreG9BR6CapabilityMatrix.rowsOf(matrix.rows(), method);
		assertFalse(rows.isEmpty(), method);
		List<String> failures = new ArrayList<>();
		for (String locale : PreG9BR6CapabilityMatrix.LOCALES) {
			try (Host host = new Host(locale, directory)) {
				for (Row row : rows) {
					if (row.locale().equals(locale)) {
						try {
							PreG9BR6CapabilityMatrix.execute(host, matrix, row);
						} catch (AssertionError failure) {
							failures.add(failure.getMessage());
						}
					}
				}
			}
		}
		assertEquals(List.of(), failures);
	}

	private static void executeProbes(TestInfo test, Path directory, String locale)
			throws Exception {
		Matrix matrix = PreG9BR6CapabilityMatrix.load();
		List<Probe> probes = PreG9BR6CapabilityMatrix.probesOf(matrix.probes(), method(test));
		assertFalse(probes.isEmpty(), method(test));
		List<String> failures = new ArrayList<>();
		try (Host host = new Host(locale, directory)) {
			for (Probe probe : probes) {
				assertEquals(locale, probe.locale(), probe.probeId());
				try {
					PreG9BR6CapabilityMatrix.execute(host, matrix, probe);
				} catch (AssertionError failure) {
					failures.add(failure.getMessage());
				}
			}
		}
		assertEquals(List.of(), failures);
	}

	private static Inventory inventory() {
		AppGeoCeDG english = PreG9BR6ScriptHarness.application(Locale.ENGLISH);
		AppGeoCeDG spanish = PreG9BR6ScriptHarness.application(PreG9BR6ScriptHarness.SPANISH);
		return PreG9BR6CapabilityMatrix.derive(english, spanish);
	}

	private static Set<String> junitMethods() {
		Set<String> methods = new TreeSet<>();
		for (Method method : PreG9BR6CapabilityMatrixTest.class.getDeclaredMethods()) {
			if (method.isAnnotationPresent(Test.class)) {
				methods.add(method.getName());
			}
		}
		return methods;
	}

	private static String method(TestInfo test) {
		return test.getTestMethod().orElseThrow().getName();
	}

	private static void assertViolation(List<String> violations, String expected) {
		assertTrue(violations.stream().anyMatch(v -> v.startsWith(expected)),
				expected + " not in " + violations);
	}
}
