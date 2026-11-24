/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A2 ({@code bspline-2d.md} section 11.3, item 3): inventory over the
 * production sources. Every call site that reads the inherited global coefficients of a
 * {@code PiecewisePolynomialLocus2D} is listed here, with its count; each is either the
 * shared accessor's inherited branch or an implementation of the interface itself. Every
 * file that names the interface is classified. A new reader, or a new user of the type,
 * fails this test until it is routed through {@code PolynomialSpanCoefficients2D} and
 * classified. The dynamic proof is {@link MaintA2LocalPolynomialRoutingTest}.
 */
public class MaintA2GlobalCoefficientInventoryTest {
	private static final Pattern GLOBAL_READ = Pattern.compile(
			"(?<![A-Za-z#])getPolynomial(Coordinate)?Coefficients\\((?!\\s*String\\b)");
	private static final String LOCUS = "org/geocedg/common/kernel/locus/";

	/** Allowed global reads: file to exact number of call sites, with the reason. */
	private static final Map<String, Integer> ALLOWED_GLOBAL_READS = new TreeMap<>(Map.of(
			// the interface default composes the per-coordinate accessor (no consumer)
			LOCUS + "PiecewisePolynomialLocus2D.java", 2,
			// the inherited branch of the single shared accessor
			LOCUS + "PolynomialSpanCoefficients2D.java", 1,
			// interface conformance: the wrapper forwards its source's global coefficients
			// and composes its own per-coordinate accessor
			LOCUS + "LocusSimilarityEvaluator2D.java", 2,
			// interface conformance of the family: per-coordinate accessor composes the pair
			"org/geocedg/common/kernel/bspline/BSplineSemanticEvaluator2D.java", 1));

	/**
	 * Classification of every production file that names the interface (base: 13 files;
	 * A2 adds the extension, the accessor and the family evaluator).
	 */
	private static final Map<String, String> USERS_OF_THE_TYPE = new TreeMap<>(Map.ofEntries(
			Map.entry(LOCUS + "PiecewisePolynomialLocus2D.java", "the interface"),
			Map.entry(LOCUS + "LocalPiecewisePolynomialLocus2D.java", "A2 extension"),
			Map.entry(LOCUS + "PolynomialSpanCoefficients2D.java", "A2 shared accessor"),
			Map.entry(LOCUS + "LocusSimilarityEvaluator2D.java",
					"wrapper: forwards the local extension (C6)"),
			Map.entry("org/geocedg/common/kernel/spline/SplineSemanticEvaluator2D.java",
					"SplineV2 implementation (no local coefficients)"),
			Map.entry("org/geocedg/common/kernel/bspline/BSplineSemanticEvaluator2D.java",
					"A2 family implementation"),
			Map.entry(LOCUS + "intersection/PolynomialTargetIntersectionCapability2D.java",
					"coefficient reader C1/C2, routed"),
			Map.entry(LOCUS + "intersection/PiecewisePolynomialPairIntersectionCapability2D.java",
					"coefficient reader C3/C4, routed"),
			Map.entry(LOCUS + "interaction/LocusPointInteractionResolver2D.java",
					"coefficient reader C5, routed"),
			Map.entry(LOCUS + "interaction/LocusPointContinuation2D.java",
					"type and supportsPiecewisePolynomial gate only, then the resolver C5"),
			Map.entry(LOCUS + "interaction/LocusPointInteractionWorkBudget2D.java",
					"composition-depth constant only"),
			Map.entry(LOCUS + "intersection/IntersectionCapabilityContext2D.java",
					"type gate and evaluatePolynomialDerivative only"),
			Map.entry(LOCUS + "intersection/LocusPairIntersectionContext2D.java",
					"type, depth gate and evaluatePolynomialDerivative only"),
			Map.entry(LOCUS + "metric/PolicyV2LocusMetricCapability2D.java",
					"composition-depth constant only"),
			Map.entry(LOCUS + "intersection/SplineImplicitIntervalCertification2D.java",
					"certified tier tied to SplineSemanticEvaluator2D; depth constant"),
			Map.entry(LOCUS + "intersection/SplineIntervalModel2D.java",
					"certified tier tied to SplineSemanticEvaluator2D; span count only")));

	private static Path productionRoot() {
		Path here = Paths.get("").toAbsolutePath();
		for (Path candidate = here; candidate != null; candidate = candidate.getParent()) {
			Path root = candidate.resolve("source/shared/common/src/main/java");
			if (Files.isDirectory(root)) {
				return root;
			}
		}
		throw new AssertionError("shared production sources not found from " + here);
	}

	@Test
	void everyGlobalCoefficientReadIsTheAccessorOrAnImplementation() throws IOException {
		Path root = productionRoot();
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(root)) {
			for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))
					::iterator) {
				String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
				Matcher matcher = GLOBAL_READ.matcher(text);
				int count = 0;
				while (matcher.find()) {
					count++;
				}
				if (count > 0) {
					found.put(root.relativize(file).toString().replace('\\', '/'), count);
				}
			}
		}
		assertEquals(ALLOWED_GLOBAL_READS, found);
	}

	@Test
	void everyUserOfTheInterfaceIsClassified() throws IOException {
		Path root = productionRoot();
		Map<String, String> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(root)) {
			for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))
					::iterator) {
				String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
				if (text.contains("PiecewisePolynomialLocus2D")) {
					String key = root.relativize(file).toString().replace('\\', '/');
					found.put(key, USERS_OF_THE_TYPE.getOrDefault(key, "UNCLASSIFIED"));
				}
			}
		}
		assertEquals(USERS_OF_THE_TYPE.keySet(), found.keySet());
		assertTrue(found.values().stream().noneMatch("UNCLASSIFIED"::equals));
	}
}
