/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoLocusND;
import org.geogebra.common.kernel.algos.AlgoLocusSliderND;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLocus;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Read-only characterization of the author-supplied CeDG scientific models.
 *
 * <p>The expensive button-equivalent experiment is opt-in through
 * {@code GEOCEDG_G6A_RUN_SCIENTIFIC_BENCHMARK=1}. Normal acceptance still
 * checks that both immutable models load and retain their documented legacy
 * dependency structure.</p>
 */
class LegacyCeDGScientificModelCharacterizationTest extends BaseUnitTest {

	private static final String ORIGINAL_MODEL =
			"models/legacy/inter-cil-cono-oblique/original/"
					+ "InterCilConoOblique.ggb";
	private static final String TWO_LEVELS_MODEL =
			"models/legacy/inter-cil-cono-oblique-two-levels/original/"
					+ "InterCilConoObliqueTwoLevels.ggb";

	@Test
	@Timeout(value = 5, unit = TimeUnit.MINUTES)
	void originalModelRetainsTwoSampleDerivedPerimeterStages()
			throws IOException, ReflectiveOperationException {
		double loadMillis = loadModel(ORIGINAL_MODEL);
		GeoLocus loc9 = requireLocus("loc9");
		GeoLocus loc10 = requireLocus("loc10");

		assertSame(loc9, requireInput("L_{ωc}", loc9));
		assertSame(loc10, requireInput("L_{PAR}", loc10));
		assertEquals(10, countSequentialLoci());
		assertNotNull(lookup("button1"));
		metric("scientific-original-load-ms", loadMillis);
		metric("scientific-original-loc9-samples", loc9.getPoints().size());
		metric("scientific-original-loc10-samples", loc10.getPoints().size());
		characterizeSlice("scientific-original-loc10", loc10);
	}

	@Test
	@Timeout(value = 5, unit = TimeUnit.MINUTES)
	void twoLevelsModelRetainsMaterializedLociAndSampledPath()
			throws IOException, ReflectiveOperationException {
		double loadMillis = loadModel(TWO_LEVELS_MODEL);
		GeoLocus loc9 = requireLocus("loc9");
		GeoLocus loc11 = requireLocus("loc11");
		GeoPoint pointOnLocus = (GeoPoint) lookup("L_2");

		assertSame(loc9, requireInput("L_{ωc}", loc9));
		assertNotNull(pointOnLocus);
		assertSame(loc11, pointOnLocus.getPath());
		assertEquals(13, countSequentialLoci());
		assertFalse(containsInput(requireGeo("L_{PAR}").getParentAlgorithm(), loc11));
		metric("scientific-two-levels-load-ms", loadMillis);
		metric("scientific-two-levels-loc9-samples", loc9.getPoints().size());
		metric("scientific-two-levels-loc11-samples", loc11.getPoints().size());
		characterizeSlice("scientific-two-levels-loc11", loc11);
	}

	@Test
	@Timeout(value = 10, unit = TimeUnit.MINUTES)
	void originalFlattenStageHasOptInMeasuredCost()
			throws IOException, ReflectiveOperationException {
		if (!"1".equals(System.getenv(
				"GEOCEDG_G6A_RUN_SCIENTIFIC_BENCHMARK"))) {
			metric("scientific-flatten-benchmark", "not-requested");
			return;
		}

		double loadMillis = loadModel(ORIGINAL_MODEL);
		GeoNumeric radius = (GeoNumeric) lookup("rbCone");
		assertNotNull(radius);
		double preFlattenRecomputeMillis = updateMeasured(radius, 0.005);
		TimedLocus first = createMeasuredLocus("g6aFinal1", "N_3");
		TimedLocus second = createMeasuredLocus("g6aFinal2", "O_3");
		TimedLocus third = createMeasuredLocus("g6aFinal3", "P_3");

		double recomputeMillis = updateMeasured(radius, 0.01);

		metric("scientific-flatten-load-ms", loadMillis);
		metric("scientific-pre-flatten-recompute-ms", preFlattenRecomputeMillis);
		metric("scientific-flatten-create-1-ms", first.milliseconds());
		metric("scientific-flatten-create-2-ms", second.milliseconds());
		metric("scientific-flatten-create-3-ms", third.milliseconds());
		metric("scientific-flatten-recompute-ms", recomputeMillis);
		characterizeSlice("scientific-flatten-1", first.locus());
		characterizeSlice("scientific-flatten-2", second.locus());
		characterizeSlice("scientific-flatten-3", third.locus());
	}

	@Test
	@Timeout(value = 10, unit = TimeUnit.MINUTES)
	void twoLevelsMaterializedStateHasOptInMeasuredCost() throws IOException {
		if (!"1".equals(System.getenv(
				"GEOCEDG_G6A_RUN_SCIENTIFIC_BENCHMARK"))) {
			metric("scientific-two-levels-benchmark", "not-requested");
			return;
		}

		double loadMillis = loadModel(TWO_LEVELS_MODEL);
		GeoNumeric radius = (GeoNumeric) lookup("rbCone");
		GeoNumeric driver = (GeoNumeric) lookup("t_{ωR}");
		assertNotNull(radius);
		assertNotNull(driver);
		double radiusRecomputeMillis = updateMeasured(radius, 0.01);
		double driverRecomputeMillis = updateMeasured(driver, 0.001);

		metric("scientific-two-levels-measured-load-ms", loadMillis);
		metric("scientific-two-levels-radius-recompute-ms", radiusRecomputeMillis);
		metric("scientific-two-levels-driver-recompute-ms", driverRecomputeMillis);
	}

	private TimedLocus createMeasuredLocus(String label, String dependent) {
		long start = System.nanoTime();
		GeoLocus locus = add(label + "=Locus(" + dependent + ",t_ω)");
		double elapsed = elapsedMillis(start);
		assertNotNull(locus);
		metric("scientific-" + label + "-defined", locus.isDefined());
		metric("scientific-" + label + "-samples", locus.getPoints().size());
		return new TimedLocus(locus, elapsed);
	}

	private static double updateMeasured(GeoNumeric source, double increment) {
		long start = System.nanoTime();
		source.setValue(source.getDouble() + increment);
		source.updateRepaint();
		return elapsedMillis(start);
	}

	private static void characterizeSlice(String prefix, GeoLocus locus)
			throws ReflectiveOperationException {
		AlgoElement algorithm = locus.getParentAlgorithm();
		Class<?> fieldOwner = algorithm instanceof AlgoLocusSliderND
				? AlgoLocusSliderND.class : AlgoLocusND.class;
		Construction slice = readField(algorithm, fieldOwner, "macroCons");
		Set<?> originalElements = readField(algorithm, fieldOwner,
				"locusConsOrigElements");
		long nestedLocusAlgorithms = slice.getAlgoList().stream()
				.filter(algo -> algo instanceof AlgoLocusND
						|| algo instanceof AlgoLocusSliderND).count();
		String types = slice.getAlgoList().stream()
				.collect(Collectors.groupingBy(algo -> algo.getClass().getSimpleName(),
						Collectors.counting()))
				.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey())
				.map(entry -> entry.getKey() + ":" + entry.getValue())
				.collect(Collectors.joining(","));
		metric(prefix + "-slice-algorithms", slice.getAlgoList().size());
		metric(prefix + "-slice-original-elements", originalElements.size());
		metric(prefix + "-slice-locus-algorithms", nestedLocusAlgorithms);
		metric(prefix + "-slice-types", types);
	}

	private double loadModel(String repositoryRelativePath) throws IOException {
		Path path = findRepositoryRoot().resolve(repositoryRelativePath);
		long start = System.nanoTime();
		getApp().setXML(readConstructionXml(path), true);
		return elapsedMillis(start);
	}

	private static String readConstructionXml(Path model) throws IOException {
		try (ZipFile archive = new ZipFile(model.toFile())) {
			ZipEntry entry = archive.getEntry("geogebra.xml");
			assertNotNull(entry, "geogebra.xml is missing from " + model);
			try (InputStream input = archive.getInputStream(entry)) {
				return new String(input.readAllBytes(), StandardCharsets.UTF_8);
			}
		}
	}

	private static Path findRepositoryRoot() {
		Path candidate = Path.of("").toAbsolutePath().normalize();
		while (candidate != null) {
			if (Files.isRegularFile(candidate.resolve("AGENTS.md"))
					&& Files.isDirectory(candidate.resolve("models"))) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		return fail("Could not resolve the GeoCeDG repository root.");
	}

	private GeoElement requireGeo(String label) {
		GeoElement geo = lookup(label);
		assertNotNull(geo, "Missing model object " + label);
		return geo;
	}

	private GeoLocus requireLocus(String label) {
		return (GeoLocus) requireGeo(label);
	}

	private GeoElement requireInput(String ownerLabel, GeoElement expected) {
		AlgoElement algorithm = requireGeo(ownerLabel).getParentAlgorithm();
		assertNotNull(algorithm);
		return Arrays.stream(algorithm.getInput())
				.filter(input -> input == expected)
				.findFirst()
				.orElseGet(() -> fail(ownerLabel + " does not depend on "
						+ expected.getLabelSimple()));
	}

	private static boolean containsInput(AlgoElement algorithm, GeoElement expected) {
		return algorithm != null && Arrays.stream(algorithm.getInput())
				.anyMatch(input -> input == expected);
	}

	private int countSequentialLoci() {
		int count = 0;
		while (lookup("loc" + (count + 1)) instanceof GeoLocus) {
			count++;
		}
		return count;
	}

	private static double elapsedMillis(long startNanos) {
		return (System.nanoTime() - startNanos) / 1_000_000.0;
	}

	@SuppressWarnings("unchecked")
	private static <T> T readField(Object target, Class<?> fieldOwner, String name)
			throws ReflectiveOperationException {
		Field field = fieldOwner.getDeclaredField(name);
		field.setAccessible(true);
		return (T) field.get(target);
	}

	private static void metric(String name, Object value) {
		System.out.println("G6A_METRIC " + name + "=" + value);
	}

	private record TimedLocus(GeoLocus locus, double milliseconds) {
	}
}
