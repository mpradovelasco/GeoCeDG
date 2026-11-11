/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.geocedg.desktop.G9U1TestApp.eval;
import static org.geocedg.desktop.G9U1TestApp.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Reproduce commands directly from the two packaged user guides. */
@ExtendWith(G9U1TestApp.Lifecycle.class)
class PreG9BCu9GuideExamplesTest {

	private static final Pattern EXAMPLE = Pattern.compile("(?m)^```text\\R(.*?)^```$",
			Pattern.DOTALL);
	private static final double TOLERANCE = 1e-5;

	@Test
	void firstConstructionRespondsToAnEndpointEdit() throws IOException {
		AppGeoCeDG app = G9U1TestApp.create();
		enter(app, example("A=(0,0)\nB=(3,4)"));
		assertNumber(app, "d", 5);
		eval(app, "B=(6,8)");
		assertNumber(app, "d", 10);
	}

	@Test
	void workedLocusAndSplineUseTheirStatedDomainsAndPoints() throws IOException {
		AppGeoCeDG locus = G9U1TestApp.create();
		enter(locus, example("u=0\nG=(u,0)"));
		assertNumber(locus, "LL", 4);
		assertNumber(locus, "LP", 2);

		AppGeoCeDG spline = G9U1TestApp.create();
		enter(spline, example("h=0\nA=(-2,h)"));
		assertNumber(spline, "M", 4);
		assertNumber(spline, "MP", 2);
		eval(spline, "h=2");
		assertNumber(spline, "M", 4);
		assertNumber(spline, "MP", 2);
	}

	@Test
	void openRouteDemonstratesEveryBoundaryPolicy() throws IOException {
		AppGeoCeDG app = G9U1TestApp.create();
		enter(app, example("s=0\nQ=(s,0)"));
		enter(app, example("DefaultRoute=LocusLength(L,B,A)"));
		assertMetric(app, "T", 10);
		assertMetric(app, "DefaultRoute", 6);
		assertFalse(metric(app, "StrictRoute").getMetricResult().getMetricValue()
				.getFiniteValue().isPresent());
		assertMetric(app, "StopRoute", 2);
		assertMetric(app, "WrapRoute", 4);
		assertMetric(app, "BackRoute", 6);
	}

	@Test
	void periodicRouteDemonstratesSeamAndFullCycle() throws IOException {
		AppGeoCeDG app = G9U1TestApp.create();
		enter(app, example("periodicCircle=Circle((0,0),1)"));
		assertMetric(app, "F", 2 * Math.PI - 5);
		assertMetric(app, "R", 5);
		assertMetric(app, "Z", 0);
		assertMetric(app, "C", 2 * Math.PI);
	}

	private static void assertNumber(AppGeoCeDG app, String label, double expected) {
		assertEquals(expected, assertInstanceOf(GeoNumeric.class, lookup(app, label))
				.getDouble(), TOLERANCE, label);
	}

	private static void assertMetric(AppGeoCeDG app, String label, double expected) {
		assertEquals(expected, metric(app, label).getMetricResult().getMetricValue()
				.getFiniteValue().orElseThrow(), TOLERANCE, label);
	}

	private static GeoLocusMetricResult metric(AppGeoCeDG app, String label) {
		return assertInstanceOf(GeoLocusMetricResult.class, lookup(app, label));
	}

	private static void enter(AppGeoCeDG app, List<String> commands) {
		commands.forEach(command -> eval(app, command));
	}

	private static List<String> example(String firstLines) throws IOException {
		List<String> english = example("en", firstLines);
		assertEquals(english, example("es", firstLines), firstLines);
		return english;
	}

	private static List<String> example(String language, String firstLines) throws IOException {
		String guide = GeoCeDGActionRegistry.readUserGuide(language).replace("\r\n", "\n");
		var matcher = EXAMPLE.matcher(guide);
		while (matcher.find()) {
			String block = matcher.group(1);
			if (block.startsWith(firstLines + "\n")) {
				return block.lines().filter(line -> !line.isBlank()).toList();
			}
		}
		assertTrue(false, "Missing example in " + language + ": " + firstLines);
		return List.of();
	}
}
