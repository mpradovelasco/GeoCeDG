/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.metric.LocusConditionedRouteJumpGuard2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-MAINT-B5a on the author models ({@code locus-v2-metrics.md} section 28.3.1,
 * validation): every conditioned route measured in the baseline of the candidate report on
 * the hole loci {@code w_2} and {@code a_3} of the author model and on the transported contour
 * {@code trHole} of its derived variant (each region, at four fixed fractions of it) is still a
 * number under {@code conditioned-length-scalar/v2}, with the value of the baseline taken on
 * {@code 228580e96} before the change, bit for bit, and the evaluations of the check recorded:
 * four per leaf, no leaf below resolution, no diagnostic of the check. The number is what the
 * Algebra view shows.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BMaintB5aAuthorModelDesktopTest {
	/** Fractions of a region: {lower, upper}; an upper fraction 1 is the end of the region. */
	private static final double[][] FRACTIONS = {{0.3, 0.7}, {0, 1}, {0.05, 0.5}, {0.5, 0.95}};

	@TempDir
	Path work;

	/** One baseline route: locus, region, fraction index, value, accepted leaves. */
	private static final class Expected {
		final String locus;
		final int region;
		final int fraction;
		final double value;
		final int leaves;

		Expected(String locus, int region, int fraction, double value, int leaves) {
			this.locus = locus;
			this.region = region;
			this.fraction = fraction;
			this.value = value;
			this.leaves = leaves;
		}
	}

	private static List<Expected> original() {
		List<Expected> routes = new ArrayList<>();
		routes.add(new Expected("w_2", 0, 0, 1.3140010177874024, 64));
		routes.add(new Expected("w_2", 0, 1, 4.403406152729751, 484));
		routes.add(new Expected("w_2", 0, 2, 1.611286008028409, 89));
		routes.add(new Expected("w_2", 0, 3, 1.7337395600177379, 83));
		routes.add(new Expected("w_2", 1, 0, 1.3140010177897226, 64));
		routes.add(new Expected("w_2", 1, 1, 4.403406152731525, 484));
		routes.add(new Expected("w_2", 1, 2, 1.7337395600195493, 83));
		routes.add(new Expected("w_2", 1, 3, 1.6112860080284777, 89));
		routes.add(new Expected("a_3", 0, 0, 1.5782792147298492, 64));
		routes.add(new Expected("a_3", 0, 1, 4.905283302467904, 512));
		routes.add(new Expected("a_3", 0, 2, 1.972502214948997, 87));
		routes.add(new Expected("a_3", 0, 3, 1.863701339683841, 89));
		routes.add(new Expected("a_3", 1, 0, 1.578279214732765, 64));
		routes.add(new Expected("a_3", 1, 1, 4.905283302470005, 512));
		routes.add(new Expected("a_3", 1, 2, 1.8637013396860989, 89));
		routes.add(new Expected("a_3", 1, 3, 1.9725022149490639, 87));
		return routes;
	}

	private static List<Expected> transport() {
		List<Expected> routes = new ArrayList<>();
		routes.add(new Expected("trHole", 0, 0, 3.9170454073767407, 141));
		routes.add(new Expected("trHole", 0, 1, 9.326280652849112, 383));
		routes.add(new Expected("trHole", 0, 2, 3.888767914654724, 123));
		routes.add(new Expected("trHole", 0, 3, 4.40098182220109, 194));
		return routes;
	}

	@Test
	void theRoutesOfTheHoleLociStayNumbers() throws Exception {
		assertEquals(List.of(), measure(PostR1Corpus.original(), original()));
	}

	@Test
	void theRoutesOfTheTransportedContourStayNumbers() throws Exception {
		assertEquals(List.of(), measure(PostR1Corpus.transport(), transport()));
	}

	/**
	 * The order of the consumers of one revision of {@code w_2} (the subtotal over its
	 * regions, a route and a second length on the same route) changes nothing in what the
	 * route publishes: the state of a route stretch is cached apart from the state of a whole
	 * region.
	 */
	@Test
	void theRouteOfTheHoleLocusDoesNotDependOnTheOrderOfItsConsumers() throws Exception {
		List<String> totalFirst = consumersInOrder(true);
		List<String> routeFirst = consumersInOrder(false);
		assertEquals(totalFirst, routeFirst);
		assertEquals(totalFirst.get(0), totalFirst.get(1), "two lengths on the same route");
		assertEquals("true 1.3140010177874024 " + LocusConditionedRouteJumpGuard2D.VERSION
				+ ": 64 leaf(s) tested, 0 below resolution, 256 evaluation(s); "
				+ LocusConditionedRouteJumpGuard2D.STATEMENT, totalFirst.get(0)
						.substring(0, totalFirst.get(0).indexOf(" [")));
	}

	private List<String> consumersInOrder(boolean totalFirst) throws Exception {
		return PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.original(),
					Files.createDirectories(work.resolve(totalFirst ? "a" : "b"))));
			GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, "w_2");
			LocusInterval2D region = locus.getConditionedEvidence(PostR1Corpus.MAIN)
					.getRegions().get(0);
			double width = region.getUpper() - region.getLower();
			PostR1Corpus.command(app, "BOA=Point(w_2,\"" + PostR1Corpus.MAIN + "\","
					+ (region.getLower() + 0.3 * width) + ")");
			PostR1Corpus.command(app, "BOB=Point(w_2,\"" + PostR1Corpus.MAIN + "\","
					+ (region.getLower() + 0.7 * width) + ")");
			if (totalFirst) {
				PostR1Corpus.command(app, "boT=LocusLength(w_2)");
			}
			PostR1Corpus.command(app, "boL=LocusLength(w_2,BOA,BOB)");
			PostR1Corpus.command(app, "boM=LocusLength(w_2,BOA,BOB)");
			if (!totalFirst) {
				PostR1Corpus.command(app, "boT=LocusLength(w_2)");
			}
			List<String> outcomes = new ArrayList<>();
			for (String label : new String[] {"boL", "boM"}) {
				GeoNumeric number = (GeoNumeric) app.getKernel().lookupLabel(label);
				LocusMetricResult2D rich = AlgoLocusMetricScalarAdapter.richResultOf(number)
						.getMetricResult();
				outcomes.add(rich.isScalarAdmissible() + " " + number.getValue() + " "
						+ rich.getConditionedRoute().flatMap(route -> route.getJumpGuard())
								.map(Object::toString).orElse("no check") + " "
						+ rich.getDiagnostics());
			}
			outcomes.add(String.valueOf(AlgoLocusMetricScalarAdapter.richResultOf(
					app.getKernel().lookupLabel("boT")).getMetricResult().getDiagnostics()));
			return outcomes;
		});
	}

	/** @return the routes that differ from the baseline, with what was measured */
	private List<String> measure(Path versioned, List<Expected> routes) throws Exception {
		return PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(versioned, work));
			List<String> differences = new ArrayList<>();
			int id = 0;
			for (Expected expected : routes) {
				GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, expected.locus);
				LocusInterval2D region = locus.getConditionedEvidence(PostR1Corpus.MAIN)
						.getRegions().get(expected.region);
				double[] fraction = FRACTIONS[expected.fraction];
				double width = region.getUpper() - region.getLower();
				double a = fraction[0] == 0 ? region.getLower()
						: region.getLower() + fraction[0] * width;
				double b = fraction[1] == 1 ? region.getUpper()
						: region.getLower() + fraction[1] * width;
				id++;
				PostR1Corpus.command(app, "BFA" + id + "=Point(" + expected.locus + ",\""
						+ PostR1Corpus.MAIN + "\"," + a + ")");
				PostR1Corpus.command(app, "BFB" + id + "=Point(" + expected.locus + ",\""
						+ PostR1Corpus.MAIN + "\"," + b + ")");
				GeoNumeric number = (GeoNumeric) PostR1Corpus.command(app, "bfL" + id
						+ "=LocusLength(" + expected.locus + ",BFA" + id + ",BFB" + id + ")")[0];
				LocusMetricResult2D rich = AlgoLocusMetricScalarAdapter.richResultOf(number)
						.getMetricResult();
				String measured = rich.isScalarAdmissible() + " " + number.isDefined() + " "
						+ number.getValue() + " " + rich.getConditionedRoute().map(route ->
								route.getPolicyVersion() + " " + route.getJumpGuard().map(
										LocusConditionedRouteJumpGuard2D.Record::toString)
										.orElse("no check")).orElse("no route");
				String wanted = "true true " + expected.value + " conditioned-length-scalar/v2 "
						+ LocusConditionedRouteJumpGuard2D.VERSION + ": " + expected.leaves
						+ " leaf(s) tested, 0 below resolution, " + 4 * expected.leaves
						+ " evaluation(s); " + LocusConditionedRouteJumpGuard2D.STATEMENT;
				if (!wanted.equals(measured)) {
					differences.add(expected.locus + " region " + expected.region
							+ " fraction " + expected.fraction + ": " + measured + " "
							+ rich.getDiagnostics());
				}
			}
			return differences;
		});
	}
}
