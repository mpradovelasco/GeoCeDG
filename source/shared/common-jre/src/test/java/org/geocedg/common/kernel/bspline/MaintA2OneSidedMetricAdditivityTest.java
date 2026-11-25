/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.geocedg.common.kernel.algos.AlgoBSpline;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluator2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.OneSidedLocusDifferentialEvaluator2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geocedg.common.math.bspline.BSplineData;
import org.geocedg.common.math.bspline.BSplineEvaluation;
import org.geocedg.common.math.bspline.BSplinePoint;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A2, option A (author decision item): the optional
 * {@link OneSidedLocusDifferentialEvaluator2D} is strictly additive. The inventory lists every
 * production file that names it; the estimator takes the derivative from below only when the
 * source declares it, so every other source (SplineV2, every built-in family and the
 * similarity image of one of them) keeps the inherited arithmetic. The bit identity of the
 * SplineV2 lengths and of their similarity images is pinned by
 * {@link MaintA2SplineV2BitIdentityTest} (BS-13 golden, keys {@code S*.length},
 * {@code T*.length}, {@code S2.partialLength}).
 */
public class MaintA2OneSidedMetricAdditivityTest extends BaseUnitTest {
	private static final String BRANCH = AlgoBSpline.BRANCH_KEY;
	private static final String LOCUS = "org/geocedg/common/kernel/locus/";

	/** Every production file naming the extension, with its role. */
	private static final Map<String, String> USERS = new TreeMap<>(Map.of(
			LOCUS + "metric/OneSidedLocusDifferentialEvaluator2D.java", "the extension",
			LOCUS + "metric/LocusMetricEstimator2D.java",
			"the only consumer: upper-end speed of a piece",
			LOCUS + "LocusSimilarityEvaluator2D.java", "wrapper: forwards the source's",
			"org/geocedg/common/kernel/bspline/BSplineSemanticEvaluator2D.java",
			"the only implementation"));

	/** Every production implementation of the differential evaluator, one-sided or not. */
	private static final Map<String, Boolean> DIFFERENTIAL_SOURCES = new TreeMap<>(Map.of(
			"org/geocedg/common/kernel/spline/SplineSemanticEvaluator2D.java", false,
			LOCUS + "LocusSimilarityEvaluator2D.java", true,
			"org/geocedg/common/kernel/bspline/BSplineSemanticEvaluator2D.java", true));

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

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

	private static Map<String, String> filesContaining(String token) throws IOException {
		Path root = productionRoot();
		Map<String, String> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(root)) {
			for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))
					::iterator) {
				String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
				if (text.contains(token)) {
					found.put(root.relativize(file).toString().replace('\\', '/'), text);
				}
			}
		}
		return found;
	}

	@Test
	void inventoryOfTheExtensionAndOfTheDifferentialSources() throws IOException {
		assertEquals(USERS.keySet(),
				filesContaining("OneSidedLocusDifferentialEvaluator2D").keySet());
		Map<String, Boolean> sources = new TreeMap<>();
		for (Map.Entry<String, String> entry
				: filesContaining("LocusDifferentialEvaluator2D").entrySet()) {
			String text = entry.getValue();
			int declaration = text.indexOf(" implements ");
			if (declaration < 0) {
				continue;
			}
			String header = text.substring(declaration, text.indexOf('{', declaration));
			if (header.contains("LocusDifferentialEvaluator2D")) {
				sources.put(entry.getKey(),
						header.contains("OneSidedLocusDifferentialEvaluator2D"));
			}
		}
		assertEquals(DIFFERENTIAL_SOURCES, sources);
	}

	private void points() {
		for (String point : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2.5)", "D=(4,0.5)",
				"E=(6,1)", "F=(7,3)", "G=(5,4)"}) {
			add(point);
		}
	}

	private static Object capability(GeoLocusV2 locus) {
		return locus.getSemanticDefinition().getEvaluatorCapability();
	}

	@Test
	void splineV2AndItsImagesDoNotDeclareTheExtension() {
		points();
		GeoLocusV2 spline = add("s=SplineV2({A,B,C,D,E},3)");
		GeoLocusV2 image = add("r=Rotate(s,pi/6,A)");
		assertTrue(capability(spline) instanceof LocusDifferentialEvaluator2D);
		assertFalse(capability(spline) instanceof OneSidedLocusDifferentialEvaluator2D);
		OneSidedLocusDifferentialEvaluator2D wrapper =
				(OneSidedLocusDifferentialEvaluator2D) capability(image);
		LocusDefinition2D definition = image.getSemanticDefinition();
		assertTrue(wrapper.supportsDifferential(definition));
		assertFalse(wrapper.supportsOneSidedDifferential(definition),
				"a non-implementing source keeps the inherited estimator path");
		assertFalse(wrapper.evaluateDifferentialFromBelow(definition, "spline-v2/main", 0.5,
				LocusEvaluationSession2D.reference()).isValid());
	}

	@Test
	void theFamilyAndItsImageGiveTheDerivativeOfTheSpanThatEnds() {
		points();
		GeoLocusV2 corner = add("t=BSpline({A,B,C,D,E,F,G},3,{0,0,0,0,1,1,1,2,2,2,2})");
		final GeoLocusV2 image = add("r=Rotate(t,pi/6,A)");
		BSplineData data = ((AlgoBSpline) corner.getParentAlgorithm()).getEvaluator().getData();
		LocusDefinition2D definition = corner.getSemanticDefinition();
		OneSidedLocusDifferentialEvaluator2D family =
				(OneSidedLocusDifferentialEvaluator2D) capability(corner);
		assertTrue(family.supportsOneSidedDifferential(definition));
		LocusEvaluationSession2D session = LocusEvaluationSession2D.reference();
		// at the multiplicity-3 knot u = 1: owner is the right span, from below the left one
		BSplinePoint left = BSplineEvaluation.firstDerivativeInSpan(data,
				data.spanKnotIndex(0), 1);
		BSplinePoint right = BSplineEvaluation.firstDerivativeInSpan(data,
				data.spanKnotIndex(1), 1);
		assertFalse(left.x() == right.x() && left.y() == right.y(), "a C^0 corner");
		LocusDifferentialEvaluation2D below = family.evaluateDifferentialFromBelow(definition,
				BRANCH, 1, session);
		LocusDifferentialEvaluation2D owner = family.evaluateDifferential(definition, BRANCH,
				1, session);
		assertEquals(left.x(), below.getDerivativeX(), 0);
		assertEquals(left.y(), below.getDerivativeY(), 0);
		assertEquals(right.x(), owner.getDerivativeX(), 0);
		assertEquals(right.y(), owner.getDerivativeY(), 0);
		// inside a span and at the domain start both agree
		for (double u : new double[] {0, 0.5, 1.5}) {
			LocusDifferentialEvaluation2D a = family.evaluateDifferentialFromBelow(definition,
					BRANCH, u, session);
			LocusDifferentialEvaluation2D b = family.evaluateDifferential(definition, BRANCH,
					u, session);
			assertEquals(b.getDerivativeX(), a.getDerivativeX(), 0);
			assertEquals(b.getDerivativeY(), a.getDerivativeY(), 0);
		}
		OneSidedLocusDifferentialEvaluator2D wrapper =
				(OneSidedLocusDifferentialEvaluator2D) capability(image);
		LocusDefinition2D imageDefinition = image.getSemanticDefinition();
		assertTrue(wrapper.supportsOneSidedDifferential(imageDefinition));
		LocusDifferentialEvaluation2D imageBelow = wrapper.evaluateDifferentialFromBelow(
				imageDefinition, BRANCH, 1, session);
		double cos = Math.cos(Math.PI / 6);
		double sin = Math.sin(Math.PI / 6);
		assertEquals(cos * left.x() - sin * left.y(), imageBelow.getDerivativeX(), 1E-12);
		assertEquals(sin * left.x() + cos * left.y(), imageBelow.getDerivativeY(), 1E-12);
		LocusMetricResult2D source = AlgoLocusMetricScalarAdapter
				.richResultOf(add("L=LocusLength(t)")).getMetricResult();
		LocusMetricResult2D rotated = AlgoLocusMetricScalarAdapter
				.richResultOf(add("LR=LocusLength(r)")).getMetricResult();
		double tolerance = source.getErrorEvidence().getEstablishedAbsoluteAmount()
				+ rotated.getErrorEvidence().getEstablishedAbsoluteAmount();
		assertEquals(source.getMetricValue().getFiniteValue().getAsDouble(),
				rotated.getMetricValue().getFiniteValue().getAsDouble(), tolerance);
	}

	@Test
	void theSeamOfAClosedCurveUsesTheLastSpanFromBelow() {
		points();
		GeoLocusV2 closed = add("cs=BSpline({A,B,C,D,A},3)");
		BSplineData data = ((AlgoBSpline) closed.getParentAlgorithm()).getEvaluator().getData();
		assertTrue(data.isExactlyClosed());
		LocusDefinition2D definition = closed.getSemanticDefinition();
		OneSidedLocusDifferentialEvaluator2D family =
				(OneSidedLocusDifferentialEvaluator2D) capability(closed);
		LocusEvaluationSession2D session = LocusEvaluationSession2D.reference();
		double end = data.domainEnd();
		BSplinePoint last = BSplineEvaluation.firstDerivativeInSpan(data,
				data.spanKnotIndex(data.spanCount() - 1), end);
		BSplinePoint first = BSplineEvaluation.firstDerivativeInSpan(data,
				data.spanKnotIndex(0), data.domainStart());
		LocusDifferentialEvaluation2D below = family.evaluateDifferentialFromBelow(definition,
				BRANCH, end, session);
		LocusDifferentialEvaluation2D owner = family.evaluateDifferential(definition, BRANCH,
				end, session);
		assertEquals(last.x(), below.getDerivativeX(), 0);
		assertEquals(last.y(), below.getDerivativeY(), 0);
		// the seam owner is the first span (b is identified with a)
		assertEquals(first.x(), owner.getDerivativeX(), 0);
		assertEquals(first.y(), owner.getDerivativeY(), 0);
	}
}
