/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.conditioned.LocusConditionedEvidence2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricConditionedRoute2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricCoverage;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * Step C4 of the corrections after block 3B (decision DN-2 of the author, 2026-11-22;
 * {@code locus-v2-metrics.md} section 28.3) on the author model: the length between two
 * points of one conditioned region of the hole locus {@code w_2} is a number, usable in a
 * formula, that follows a real change of {@code excCyl}, is undefined across the hole and
 * is read back from the native document. It certifies nothing.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PostR1ConditionedLengthDesktopTest {
	@TempDir
	Path work;

	@Test
	void theLengthOfARouteOfAHoleLocusIsANumberThatFollowsARealChange() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<String, Object> out = new LinkedHashMap<>();
			AppGeoCeDG app = PreG9BCu0Corpus.open(PostR1Corpus.workingCopy(
					PostR1Corpus.original(), work));
			GeoLocusV2 hole = PreG9BCu0Corpus.locus(app, "w_2");
			LocusConditionedEvidence2D evidence = hole.getConditionedEvidence(
					PostR1Corpus.MAIN);
			out.put("regions", evidence.getRegions().size());
			PostR1Corpus.command(app, "CUP=Point(w_2,\"" + PostR1Corpus.MAIN + "\","
					+ at(evidence, 0, 0.3) + ")");
			PostR1Corpus.command(app, "CUQ=Point(w_2,\"" + PostR1Corpus.MAIN + "\","
					+ at(evidence, 0, 0.7) + ")");
			PostR1Corpus.command(app, "CUR=Point(w_2,\"" + PostR1Corpus.MAIN + "\","
					+ at(evidence, 1, 0.5) + ")");
			GeoNumeric length = (GeoNumeric) PostR1Corpus.command(app,
					"cuL=LocusLength(w_2,CUP,CUQ)")[0];
			LocusMetricResult2D rich = AlgoLocusMetricScalarAdapter.richResultOf(length)
					.getMetricResult();
			out.put("defined", length.isDefined());
			out.put("equals the rich value", length.getValue() == rich.getMetricValue()
					.getFiniteValue().orElse(Double.NaN));
			out.put("coverage", rich.getCoverage());
			out.put("class", rich.getGuaranteeClass());
			out.put("error within tolerance", rich.getErrorEvidence()
					.getEstablishedAbsoluteAmount() <= rich.getErrorEvidence()
							.getRequestedTolerance().orElse(Double.NaN));
			LocusMetricConditionedRoute2D route = rich.getConditionedRoute().orElse(null);
			out.put("route", route != null && route.getSemanticRevision()
					== hole.getSemanticRevision() && route.getEvidenceContentIdentity()
							.equals(evidence.getContentIdentity()));
			GeoNumeric formula = (GeoNumeric) PostR1Corpus.command(app, "cuF=2*cuL+1")[0];
			out.put("formula", formula.isDefined()
					&& formula.getValue() == 2 * length.getValue() + 1);
			out.put("still not established", hole.getSemanticDefinition().getBranch(
					PostR1Corpus.MAIN).getExistenceStructure().getCompleteness());
			// Across the hole: two regions, no route, no number.
			GeoElement across = (GeoElement) PostR1Corpus.command(app,
					"cuX=LocusLength(w_2,CUP,CUR)")[0];
			out.put("across the hole", across.isDefined());
			// The total over the two regions is a subtotal, not a number.
			GeoElement total = (GeoElement) PostR1Corpus.command(app,
					"cuT=LocusLength(w_2)")[0];
			out.put("total", total.isDefined());

			double before = length.getValue();
			double eccentricity = ((GeoNumeric) app.getKernel().lookupLabel("excCyl"))
					.getValue();
			long revision = hole.getSemanticRevision();
			PreG9BCu0Corpus.setNumber(app, "excCyl", eccentricity + 0.05);
			out.put("changed revision", hole.getSemanticRevision() != revision);
			out.put("changed defined", length.isDefined());
			out.put("changed value", length.getValue());
			out.put("changed route revision", AlgoLocusMetricScalarAdapter.richResultOf(
					length).getMetricResult().getConditionedRoute().map(current ->
							current.getSemanticRevision() == hole.getSemanticRevision())
					.orElse(false));
			out.put("changed formula", formula.getValue() == 2 * length.getValue() + 1);
			PreG9BCu0Corpus.setNumber(app, "excCyl", eccentricity);
			out.put("restored", Math.abs(length.getValue() - before));

			File saved = work.resolve("conditioned-length.cedg").toFile();
			out.put("saved", app.saveGeoGebraFile(saved));
			AppGeoCeDG reopened = PreG9BCu0Corpus.open(saved);
			GeoNumeric loaded = (GeoNumeric) reopened.getKernel().lookupLabel("cuL");
			out.put("reopened defined", loaded.isDefined());
			out.put("reopened value", Math.abs(loaded.getValue() - before));
			out.put("reopened across", reopened.getKernel().lookupLabel("cuX").isDefined());
			out.put("before", before);
			return out;
		});
		assertEquals(2, row.get("regions"), row.toString());
		assertEquals(Boolean.TRUE, row.get("defined"), row.toString());
		assertEquals(Boolean.TRUE, row.get("equals the rich value"), row.toString());
		assertEquals(MetricCoverage.INCOMPLETE, row.get("coverage"), row.toString());
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, row.get("class"), row.toString());
		assertEquals(Boolean.TRUE, row.get("error within tolerance"), row.toString());
		assertEquals(Boolean.TRUE, row.get("route"), row.toString());
		assertEquals(Boolean.TRUE, row.get("formula"), row.toString());
		assertEquals(Completeness.NOT_ESTABLISHED, row.get("still not established"),
				row.toString());
		assertEquals(Boolean.FALSE, row.get("across the hole"), row.toString());
		assertEquals(Boolean.FALSE, row.get("total"), row.toString());
		assertEquals(Boolean.TRUE, row.get("changed revision"), row.toString());
		assertEquals(Boolean.TRUE, row.get("changed defined"), row.toString());
		assertNotEquals(row.get("before"), row.get("changed value"), row.toString());
		assertEquals(Boolean.TRUE, row.get("changed route revision"), row.toString());
		assertEquals(Boolean.TRUE, row.get("changed formula"), row.toString());
		assertTrue((Double) row.get("restored") < 1e-9, row.toString());
		assertEquals(Boolean.TRUE, row.get("saved"), row.toString());
		assertEquals(Boolean.TRUE, row.get("reopened defined"), row.toString());
		assertTrue((Double) row.get("reopened value") < 1e-9, row.toString());
		assertEquals(Boolean.FALSE, row.get("reopened across"), row.toString());
	}

	private static double at(LocusConditionedEvidence2D evidence, int region,
			double fraction) {
		double lower = evidence.getRegions().get(region).getLower();
		double upper = evidence.getRegions().get(region).getUpper();
		return lower + fraction * (upper - lower);
	}
}
