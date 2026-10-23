/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geocedg.common.kernel.locus.intersection.IntersectionEndpointProvenanceResolver2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionRootDeterministicSelector2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.GeometryKind;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.metric.BetweenPositionsMetricQuery;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPositionBinder2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricRoute2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricRouteResolver2D;
import org.geocedg.common.kernel.locus.metric.LocusSemanticPosition2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geocedg.common.kernel.locus.metric.MetricPositionBinding2D;
import org.geocedg.common.kernel.locus.metric.OpenBoundaryPolicy;
import org.geocedg.common.kernel.locus.metric.SamePositionPolicy;
import org.geocedg.common.kernel.locus.metric.TraversalDirection;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.util.AsyncOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU2 steps T9 and T11: consumers of the author model read the existence structure
 * of the published certificates (consumption design sections 3 to 5.3): the chain
 * {@code o_1 -> w_1 -> d_2 -> e_2} stays defined (V-CON-10), the excluded end of {@code e_2}
 * is its one-sided limit through the explicit query of the slice, never the image of
 * {@code t_N} (P2, precursor of V-E2-8), and a point of the evaluator of the slice inside a
 * certified gap is not published as valid (V-AVAIL-1). Step T9 ran them on the shadow
 * certificates injected into definitions with the live evaluators.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelConsumerTest {
	/** First binary64 of the bite: two simple roots 2.4e-8 apart (analysis section 3.8.3). */
	private static final double FIRST_BITE = Double.longBitsToDouble(0x400d416bb67a38bcL);
	/** Last binary64 of excCyl in penetration, just below the bite. */
	private static final double PENETRATION = Double.longBitsToDouble(0x400d416bb67a38bbL);
	private static final List<String> FIRST_FAMILY = List.of("o", "v", "w", "a_1");
	private static final List<String> SEVEN = List.of("o", "v", "w", "a_1", "e_1", "o_1",
			"e_2");

	@TempDir
	Path work;

	/**
	 * V-CON-10: the generating occurrence of the driver on {@code o_1} is addressed through
	 * the certified components of its existence structure, and {@code w_1}, {@code d_2} and
	 * {@code e_2} stay defined.
	 */
	@Test
	void theGeneratingOccurrenceChainStaysDefined() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Object> values = new LinkedHashMap<>();
			GeoLocusV2 o1 = PreG9BCu0Corpus.locus(app, "o_1");
			for (double parameter : new double[] {-Math.PI, -1, 0, 1, 3}) {
				values.put("address@" + parameter, IntersectionEndpointProvenanceResolver2D
						.addressFromRawParameter(o1.getSemanticDefinition(),
								o1.getPersistentLocusId(), PreG9BCu0Corpus.MAIN, parameter)
						!= null);
			}
			AlgoLocusBetweenMetricV2 w1 = (AlgoLocusBetweenMetricV2) app.getKernel()
					.lookupLabel("w_1").getParentAlgorithm();
			LocusMetricResult2D length = w1.getResult().getMetricResult();
			values.put("w_1", length.getComputationStatus() + "/" + length.isScalarAdmissible());
			GeoElement d2 = app.getKernel().lookupLabel("d_2");
			values.put("d_2", d2.isDefined());
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			values.put("e_2", e2.isDefined() && e2.getSemanticDefinition() != null);
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
				values.put("e_2@1", e2.evaluate(PreG9BCu0Corpus.MAIN, 1, session).getStatus()
						.name());
			}
			return values;
		});
		for (double parameter : new double[] {-Math.PI, -1, 0, 1, 3}) {
			assertEquals(true, row.get("address@" + parameter), row.toString());
		}
		assertEquals("SUCCESS/true", row.get("w_1"), row.toString());
		assertEquals(true, row.get("d_2"), row.toString());
		assertEquals(true, row.get("e_2"), row.toString());
		assertEquals("VALID", row.get("e_2@1"), row.toString());
	}

	/**
	 * P2 and precursor of V-E2-8: on the certified open arc of {@code e_2}, the start
	 * {@code s = 0} is the image of {@code t_N = 0}; the excluded end {@code s = 2 pi} is the
	 * image of no canonical parameter and the explicit query evaluates the copy of
	 * {@code w_1} at the origin with the opposite same-position policy, {@code w_1 = L}. The
	 * limit agrees with the driver just below {@code t_N} and lies the total length
	 * {@code L} of {@code o_1} away from the start.
	 */
	@Test
	void theExcludedEndOfE2IsItsOneSidedLimit() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			LocusDefinition2D certified = published(e2);
			Map<String, Object> values = new LinkedHashMap<>();
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(16)) {
				LocusEvaluation2D start = certified.evaluateComponentEnd(PreG9BCu0Corpus.MAIN,
						0, false, session);
				LocusEvaluation2D end = certified.evaluateComponentEnd(PreG9BCu0Corpus.MAIN,
						0, true, session);
				LocusEvaluation2D atOrigin = certified.evaluate(PreG9BCu0Corpus.MAIN, 0,
						session);
				LocusEvaluation2D below = certified.evaluate(PreG9BCu0Corpus.MAIN, -1e-9,
						session);
				LocusEvaluation2D again = certified.evaluate(PreG9BCu0Corpus.MAIN, 0, session);
				values.put("statuses", start.getStatus() + "/" + end.getStatus() + "/"
						+ atOrigin.getStatus() + "/" + below.getStatus());
				values.put("startIsOrigin", distance(start.getPoint(), atOrigin.getPoint()));
				values.put("endToBelow", distance(end.getPoint(), below.getPoint()));
				values.put("endToStart", distance(end.getPoint(), start.getPoint()));
				values.put("restored", distance(again.getPoint(), atOrigin.getPoint()));
			}
			AlgoLocusBetweenMetricV2 w1 = (AlgoLocusBetweenMetricV2) app.getKernel()
					.lookupLabel("w_1").getParentAlgorithm();
			values.put("total", w1.resolveJumpValue().getMetricValue().getFiniteValue()
					.getAsDouble());
			return values;
		});
		assertEquals("VALID/VALID/VALID/VALID", row.get("statuses"), row.toString());
		assertEquals(0.0, (Double) row.get("startIsOrigin"), 0, row.toString());
		assertTrue((Double) row.get("endToBelow") < 1e-6, row.toString());
		// e_2 moves by w_1 along a fixed direction: its two ends are L apart (V-E2-3).
		assertEquals((Double) row.get("total"), (Double) row.get("endToStart"), 1e-9,
				row.toString());
		assertEquals(0.0, (Double) row.get("restored"), 0,
				"the persisted policy applies again after the query " + row);
	}

	/**
	 * V-E2-8 with {@code full-cycle} (residue of the corrective mandate of 2026-10-23): the
	 * twin {@code e_9} of {@code e_2} reads the length {@code w_9} of the same positions with
	 * the same-position policy {@code full-cycle} ({@code w_1} cannot be redefined: it is the
	 * protected input of the scalar that {@code e_2} reads). {@code t_N} belongs to its arc at
	 * {@code s = 2 pi}, its end, and the start {@code s = 0} is excluded, the image of no
	 * canonical parameter. The explicit query of its one-sided limit evaluates the copy of
	 * {@code w_9} at the origin with the opposite policy, {@code w_9 = 0}: it agrees with the
	 * driver just above {@code t_N} and lies the total length {@code L} of {@code o_1} away
	 * from the included end, which is the value at {@code t_N}. The two ends have distinct
	 * structural sources.
	 */
	@Test
	void withFullCycleTheExcludedStartOfE2IsItsOneSidedLimit() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			// w_1 is the protected input of the scalar d_2 that e_2 reads, so a twin of e_2
			// reads the length of the same positions with the policy full-cycle.
			Map<String, Object> values = new LinkedHashMap<>();
			for (String command : List.of(
					"w_9=LocusLength(o_1,N_1,K_1,\"forward\",\"strict\",\"full-cycle\")",
					"d_9", "Q_9=pointJump(L_1,g_2,d_9)", "j_9=Line(Q_9,f_2)",
					"R_9=translationCoor(J_1,O_1,k,Q_9,j_9)", "e_9=LocusV2(R_9,E_1)")) {
				if ("d_9".equals(command)) {
					// The scalar of a metric result is its explicit opt-in adapter, as d_2.
					new AlgoLocusMetricScalarAdapter(app.getKernel().getConstruction(), "d_9",
							(GeoLocusMetricResult) app.getKernel().lookupLabel("w_9"));
					values.put("created d_9", "true");
					continue;
				}
				// A failing command would open a modal dialog on the event thread.
				List<String> errors = new ArrayList<>();
				GeoElementND[] created = app.getKernel().getAlgebraProcessor()
						.processAlgebraCommandNoExceptionHandling(command, false,
								recording(errors), false, null);
				values.put("created " + command.substring(0, 3), created != null
						&& created.length == 1 ? "true" : String.valueOf(errors));
			}
			if (values.values().stream().anyMatch(value -> !"true".equals(value))) {
				return values;
			}
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_9");
			LocusDefinition2D certified = published(e2);
			LocusExistenceStructure2D twin = certified.getBranch(PreG9BCu0Corpus.MAIN)
					.getExistenceStructure();
			values.put("structure", twin.getCompleteness() + " "
					+ twin.getCoverage().stream().map(item -> item.getState() + ":"
							+ item.getCause() + ":" + item.getEvidence())
							.collect(java.util.stream.Collectors.toList()));
			if (twin.getContinuousValidComponents().isEmpty()) {
				values.put("diagnostic", certificate(e2).getDiagnostic());
				return values;
			}
			ContinuousComponent arc = twin.getContinuousValidComponents().get(0);
			values.put("ends", arc.getLowerBoundary().getKind() + ":"
					+ arc.getLowerBoundary().getSource().getSide() + ":"
					+ arc.getLowerBoundary().isIncluded() + "/"
					+ arc.getUpperBoundary().getKind() + ":"
					+ arc.getUpperBoundary().getSource().getSide() + ":"
					+ arc.getUpperBoundary().isIncluded() + "/" + arc.getArcCoordinate(0));
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(16)) {
				LocusEvaluation2D start = certified.evaluateComponentEnd(PreG9BCu0Corpus.MAIN,
						0, false, session);
				LocusEvaluation2D end = certified.evaluateComponentEnd(PreG9BCu0Corpus.MAIN,
						0, true, session);
				LocusEvaluation2D atOrigin = certified.evaluate(PreG9BCu0Corpus.MAIN, 0,
						session);
				LocusEvaluation2D above = certified.evaluate(PreG9BCu0Corpus.MAIN, 1e-9,
						session);
				LocusEvaluation2D again = certified.evaluate(PreG9BCu0Corpus.MAIN, 0, session);
				values.put("statuses", start.getStatus() + "/" + end.getStatus() + "/"
						+ atOrigin.getStatus() + "/" + above.getStatus());
				values.put("endIsOrigin", distance(end.getPoint(), atOrigin.getPoint()));
				values.put("startToAbove", distance(start.getPoint(), above.getPoint()));
				values.put("endToStart", distance(end.getPoint(), start.getPoint()));
				values.put("restored", distance(again.getPoint(), atOrigin.getPoint()));
			}
			AlgoLocusBetweenMetricV2 w9 = (AlgoLocusBetweenMetricV2) app.getKernel()
					.lookupLabel("w_9").getParentAlgorithm();
			values.put("total", w9.resolveJumpValue().getMetricValue().getFiniteValue()
					.getAsDouble());
			return values;
		});
		for (String command : List.of("w_9", "d_9", "Q_9", "j_9", "R_9", "e_9")) {
			assertEquals("true", row.get("created " + command), row.toString());
		}
		assertTrue(((String) row.get("structure")).startsWith("COMPLETE ["
				+ "CERTIFIED_EXISTS:"), row.toString());
		assertEquals("STRUCTURAL_EVENT:start:false/STRUCTURAL_EVENT:end:true/"
				+ 2 * Math.PI, row.get("ends"), row.toString());
		assertEquals("VALID/VALID/VALID/VALID", row.get("statuses"), row.toString());
		assertEquals(0.0, (Double) row.get("endIsOrigin"), 0, row.toString());
		assertTrue((Double) row.get("startToAbove") < 1e-6, row.toString());
		assertEquals((Double) row.get("total"), (Double) row.get("endToStart"), 1e-9,
				row.toString());
		assertEquals(0.0, (Double) row.get("restored"), 0,
				"the persisted policy applies again after the query " + row);
	}

	/**
	 * V-CON-2 on the author model (residue of the corrective mandate of 2026-10-23): in the
	 * bite at excCyl 3.7 each locus of the first family is one arc across the seam. A route
	 * between its positions {@code 0.1} inside both sides of the seam follows the arc across
	 * it in one segment that is never wrapped, and the reverse route too; forward the other
	 * way the target is not reachable, also with {@code wrap-to-start}, which states the typed
	 * diagnostic of the topology, and {@code full-cycle} between equal positions is an invalid
	 * query.
	 */
	@Test
	void routesFollowTheBiteArcOfTheModelAcrossTheSeam() throws Exception {
		Map<String, String> rows = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7);
			Map<String, String> values = new LinkedHashMap<>();
			double below = Math.PI - 0.1;
			double above = -Math.PI + 0.1;
			for (String label : FIRST_FAMILY) {
				LocusDefinition2D definition = published(PreG9BCu0Corpus.locus(app, label));
				List<ContinuousComponent> arcs = definition.getBranch(PreG9BCu0Corpus.MAIN)
						.getExistenceStructure().getContinuousValidComponents();
				ContinuousComponent arc = arcs.get(0);
				LocusMetricRoute2D forward = route(definition, below, above,
						TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
						SamePositionPolicy.ZERO_LENGTH);
				LocusMetricRoute2D reverse = route(definition, above, below,
						TraversalDirection.REVERSE, OpenBoundaryPolicy.STRICT,
						SamePositionPolicy.ZERO_LENGTH);
				LocusMetricRoute2D backward = route(definition, above, below,
						TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
						SamePositionPolicy.ZERO_LENGTH);
				LocusMetricRoute2D wrap = route(definition, above, below,
						TraversalDirection.FORWARD, OpenBoundaryPolicy.WRAP_TO_START,
						SamePositionPolicy.ZERO_LENGTH);
				LocusMetricRoute2D cycle = route(definition, above, above,
						TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT,
						SamePositionPolicy.FULL_CYCLE);
				values.put(label, arcs.size() + "/" + arc.crossesSeam() + "/"
						+ (arc.getArcCoordinate(below) >= 0
								&& arc.getArcCoordinate(below) < arc.getArcCoordinate(above)
								&& arc.getArcCoordinate(above) <= arc.getLength())
						+ " " + forward.getRouteStatus() + "/"
						+ forward.getOrderedRouteSegments().size() + "/"
						+ (forward.getOrderedRouteSegments().get(0).getStartCanonicalParameter()
								== below) + "/"
						+ (forward.getOrderedRouteSegments().get(0).getEndCanonicalParameter()
								== above + 2 * Math.PI) + "/" + forward.isWrapped() + "/"
						+ forward.isGeometricallyConnected() + " " + reverse.getRouteStatus()
						+ "/" + reverse.isWrapped() + " " + backward.getRouteStatus() + " "
						+ wrap.getRouteStatus() + "/" + wrap.getDiagnostics().stream().anyMatch(
								diagnostic -> diagnostic.getCode()
										== MetricDiagnosticCode2D.TOPOLOGY_NOT_ESTABLISHED)
						+ " " + cycle.getRouteStatus());
			}
			return values;
		});
		for (String label : FIRST_FAMILY) {
			assertEquals("1/true/true RESOLVED/1/true/true/false/true RESOLVED/false "
					+ "TARGET_NOT_REACHABLE TARGET_NOT_REACHABLE/true INVALID_QUERY",
					rows.get(label), label + " " + rows);
		}
	}

	/**
	 * V-AVAIL-1: at the first binary64 of the bite, the evaluator of the slice of {@code o}
	 * returns a point inside the certified gap around {@code t_c} (F-CU2-1); the published
	 * locus does not publish it as valid: {@code DEPENDENCY_UNDEFINED}, certified nonexistence
	 * and the disagreement of the evaluator.
	 */
	@Test
	void anEvaluatorPointInACertifiedGapIsNotPublished() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			PreG9BCu0Corpus.setNumber(app, "excCyl", FIRST_BITE);
			GeoLocusV2 o = PreG9BCu0Corpus.locus(app, "o");
			LocusExistenceStructure2D structure = certificate(o).getStructure();
			Coverage gap = null;
			for (Coverage item : structure.getCoverage()) {
				if (item.getState() == CoverageState.CERTIFIED_DOES_NOT_EXIST
						&& item.getNature().certifies()) {
					gap = item;
				}
			}
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("gaps", gap == null ? "none" : gap.getInterval().toString());
			if (gap != null) {
				double inside = gap.getInterval().getLower() / 2
						+ gap.getInterval().getUpper() / 2;
				LocusDefinition2D certified = published(o);
				try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
					values.put("live", certified.getEvaluatorCapability().evaluate(null, null,
							inside, session).getStatus().name());
					LocusEvaluation2D evaluation = certified.evaluate(PreG9BCu0Corpus.MAIN,
							inside, session);
					values.put("certified", evaluation.getStatus().name());
					values.put("diagnostic", evaluation.getDiagnostic());
				}
			}
			return values;
		});
		assertTrue(!"none".equals(row.get("gaps")), row.toString());
		assertEquals("VALID", row.get("live"), "F-CU2-1: the evaluator gives a point " + row);
		assertEquals("DEPENDENCY_UNDEFINED", row.get("certified"), row.toString());
		String diagnostic = (String) row.get("diagnostic");
		assertTrue(diagnostic.startsWith(LocusDefinition2D.CERTIFIED_NONEXISTENCE), diagnostic);
		assertTrue(diagnostic.contains(LocusDefinition2D.EVALUATOR_DISAGREEMENT), diagnostic);
	}

	/**
	 * V-CON-1 on the author model (corrective mandate, R5 group G2): the certificate is the
	 * single closure authority. For the seven loci in penetration (excCyl 2.9 and the last
	 * binary64 before the bite) and in the bite (its first binary64 and excCyl 3.7), every
	 * consumer takes the closure decision of the published certificate: a stroke of the
	 * drawing ends at its first vertex, the strict DXF sets the closure flag (code 70) and PGF,
	 * PSTricks and Asymptote write a closed path exactly for a closed periodic component; the
	 * route between equal positions with the full-cycle policy is resolved exactly there,
	 * while the route of zero length is resolved on every component; the root at the seam of
	 * a line through the point at the seam is in the seam quarantine exactly there and
	 * interior to an arc (finding H-CU2A-4); and a semantic point moved across the seam stays
	 * defined and continuous on both, while on an arc it is undefined in the certified gap,
	 * never wrapped to the other end.
	 */
	@Test
	void theCertificateIsTheSingleClosureAuthority() throws Exception {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.onEdt(() -> PreG9BCu0Corpus.open(
				PreG9BCu0Corpus.workingCopy(PreG9BCu0Corpus.CEDG, work)));
		EuclidianView view = PreG9BR6PlusBPictureFidelityTest.sized(app.getEuclidianView1());
		Map<String, String> rows = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<String, String> values = new LinkedHashMap<>();
			int serial = 0;
			for (double excCyl : new double[] {2.9, PENETRATION, FIRST_BITE, 3.7}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", excCyl);
				PreG9BCu2AuthorModelExportTest.showOnly(app, null);
				int[] empty = PreG9BCu2AuthorModelExportTest.closedPaths(app, view);
				for (String label : SEVEN) {
					values.put(excCyl + " " + label, closure(app, view, label, empty, serial++));
				}
			}
			return values;
		});
		List<String> problems = new ArrayList<>();
		for (Map.Entry<String, String> row : rows.entrySet()) {
			String[] key = row.getKey().split(" ");
			String expected;
			if ("e_2".equals(key[1])) {
				expected = OPEN_ARC;
			} else if (Double.parseDouble(key[0]) >= FIRST_BITE && !"e_1".equals(key[1])
					&& !"o_1".equals(key[1])) {
				expected = BITE_ARC;
			} else {
				expected = CLOSED_CYCLE;
			}
			if (!expected.equals(row.getValue())) {
				problems.add(row.getKey() + ": expected " + expected + " but was "
						+ row.getValue());
			}
		}
		assertEquals(List.of(), problems);
	}

	/**
	 * V-CON-3 on the author model (corrective mandate, R5 group G2): the total length of each
	 * locus of the first family has the class of section 25.1 of {@code locus-v2-metrics.md}
	 * that its evidence supports. In penetration (excCyl 2.9) each locus is a closed cycle; its
	 * length is an estimate whose established error amount holds the difference to an
	 * independent reference, the Richardson extrapolation of inscribed polylines with 2^12 and
	 * 2^13 chords. In the bite (excCyl 3.7) each locus is an arc whose speed is unbounded at
	 * its two square-root ends; a value must meet the same reference, from polylines clustered
	 * like a cosine at the ends, and otherwise the length is not established (class 4) with
	 * the typed cause of the exhausted work budget and no value (observation O-CU2A-3).
	 */
	@Test
	void theTotalLengthsOfTheFirstFamilyHaveTheirClass() throws Exception {
		Map<String, String> rows = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, String> values = new LinkedHashMap<>();
			int serial = 0;
			for (double excCyl : new double[] {2.9, 3.7}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", excCyl);
				for (String label : FIRST_FAMILY) {
					values.put(excCyl + " " + label, totalLength(app, label, serial++));
				}
			}
			return values;
		});
		List<String> problems = new ArrayList<>();
		for (Map.Entry<String, String> row : rows.entrySet()) {
			boolean closed = row.getKey().startsWith("2.9 ");
			boolean conforms = ESTIMATE_WITHIN_REFERENCE.equals(row.getValue())
					|| !closed && NOT_ESTABLISHED_BY_BUDGET.equals(row.getValue());
			if (!conforms) {
				problems.add(row.getKey() + ": " + row.getValue());
			}
		}
		assertEquals(List.of(), problems, rows.toString());
	}

	/**
	 * V-CON-4 on the author model (corrective mandate, R5 group G2): the intersection of the
	 * arc of {@code o} in the bite with a line through its points at {@code -pi + 0.4} and
	 * {@code pi - 0.4}, on both sides of the seam. At excCyl 3.7 both roots are interior to
	 * the arc and ordered by the arc coordinate, the phase frame of the arc, never by the
	 * canonical parameter; points materialized by their tokens keep them, and their
	 * deterministic selectors, through the revision to excCyl 3.85, whose ends move with the
	 * same structural sources, and stay on their roots. A line that misses every locus of the
	 * model is never {@code EMPTY} without a {@code COMPLETE} inventory.
	 */
	@Test
	void anIntersectionWithAnArcKeepsItsTokensThroughARevision() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7);
			Map<String, Object> values = new LinkedHashMap<>();
			LocusDefinition2D definition = published(PreG9BCu0Corpus.locus(app, "o"));
			LocusPoint2D a;
			LocusPoint2D b;
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
				a = definition.evaluate(PreG9BCu0Corpus.MAIN, -Math.PI + 0.4, session).getPoint();
				b = definition.evaluate(PreG9BCu0Corpus.MAIN, Math.PI - 0.4, session).getPoint();
			}
			double tx = b.getX() - a.getX();
			double ty = b.getY() - a.getY();
			GeoLine line = new GeoLine(app.getKernel().getConstruction(), -ty, tx,
					ty * a.getX() - tx * a.getY());
			line.setLabel("cutLine");
			GeoLocusIntersectionResult cut = (GeoLocusIntersectionResult) app.getKernel()
					.getAlgebraProcessor().processAlgebraCommand("cutO=Intersect(o,cutLine)",
							false)[0];
			values.put("before", roots(cut, definition));
			List<String> tokens = tokens(cut);
			Map<String, IntersectionRootDeterministicSelector2D> selectors =
					new LinkedHashMap<>();
			for (int index = 0; index < tokens.size(); index++) {
				app.getKernel().getAlgebraProcessor().processAlgebraCommand("cutX" + index
						+ "=Intersect(cutO,\"" + tokens.get(index) + "\")", false);
				selectors.put(tokens.get(index), cut.getRetainedRootSelector(tokens.get(index))
						.orElse(null));
			}
			PreG9BCu0Corpus.setNumber(app, "excCyl", 3.85);
			values.put("after", roots(cut, published(PreG9BCu0Corpus.locus(app, "o"))));
			values.put("tokens", tokens.equals(tokens(cut)));
			List<String> points = new ArrayList<>();
			for (int index = 0; index < tokens.size(); index++) {
				String token = tokens.get(index);
				GeoPoint point = (GeoPoint) app.getKernel().lookupLabel("cutX" + index);
				LocusPoint2D root = cut.getIntersectionResult().getFiniteSolutions().stream()
						.filter(solution -> token.equals(solution.getIdentity().getRootToken()))
						.findFirst().map(LocusIntersectionSolution2D::getEvaluatedPoint)
						.orElse(null);
				points.add(point.isDefined() && root != null && point.getInhomX() == root.getX()
						&& point.getInhomY() == root.getY() && selectors.get(token) != null
						&& selectors.get(token).equals(cut.getRetainedRootSelector(token)
								.orElse(null)) ? "kept" : "lost " + token);
			}
			values.put("points", points);
			GeoLine far = new GeoLine(app.getKernel().getConstruction(), 0, 1, -1000);
			far.setLabel("farLine");
			List<String> misses = new ArrayList<>();
			int serial = 0;
			for (String label : SEVEN) {
				LocusIntersectionResult2D miss = ((GeoLocusIntersectionResult) app.getKernel()
						.getAlgebraProcessor().processAlgebraCommand("far" + serial++
								+ "=Intersect(" + label + ",farLine)", false)[0])
						.getIntersectionResult();
				misses.add(miss.getGeometryKind() + "/"
						+ miss.getCompletenessEvidence().getCompleteness());
			}
			values.put("misses", misses);
			return values;
		});
		assertEquals("FINITE [INTERIOR, INTERIOR] ordered", row.get("before"), row.toString());
		assertEquals("FINITE [INTERIOR, INTERIOR] ordered", row.get("after"), row.toString());
		assertEquals(true, row.get("tokens"), row.toString());
		assertEquals(List.of("kept", "kept"), row.get("points"), row.toString());
		for (Object miss : (List<?>) row.get("misses")) {
			// EMPTY needs a COMPLETE inventory; evaluator-only coverage is not established.
			assertTrue(!((String) miss).startsWith(GeometryKind.EMPTY.name())
					|| ((String) miss).endsWith(Completeness.COMPLETE.name()), row.toString());
			assertEquals(GeometryKind.UNRESOLVED.name() + "/"
					+ Completeness.NOT_ESTABLISHED.name(), miss, row.toString());
		}
	}

	/** Closure decisions of a closed periodic component (see {@link #closure}). */
	private static final String CLOSED_CYCLE = "certificate=1 drawing=1 dxf=[1] latex=1/1/1"
			+ " zero=RESOLVED cycle=RESOLVED seam=[PERIODIC_SEAM] crossing=defined gap=none";
	/** Closure decisions of an arc of the first family across the seam in the bite. */
	private static final String BITE_ARC = "certificate=0 drawing=0 dxf=[0] latex=0/0/0"
			+ " zero=RESOLVED cycle=INVALID_QUERY seam=[INTERIOR] crossing=defined"
			+ " gap=OUTSIDE_COMPONENTS";
	/**
	 * Closure decisions of the open arc of {@code e_2} across the seam, exported in three
	 * parts around its computational gaps.
	 */
	private static final String OPEN_ARC = "certificate=0 drawing=0 dxf=[0, 0, 0] latex=0/0/0"
			+ " zero=RESOLVED cycle=INVALID_QUERY seam=[INTERIOR] crossing=defined gap=none";
	private static final String ESTIMATE_WITHIN_REFERENCE = "SUCCESS ESTIMATED within reference";
	private static final String NOT_ESTABLISHED_BY_BUDGET =
			"LIMIT_NOT_ESTABLISHED NOT_ESTABLISHED [WORK_BUDGET_EXHAUSTED] no value";

	/**
	 * @return the closure decisions of the consumers of one locus: closed periodic components
	 *         of the certificate, strokes of the drawing that end at their first vertex,
	 *         closure flags of the strict DXF, closed paths of PGF, PSTricks and Asymptote,
	 *         routes of zero length and of a full cycle between equal positions, locations
	 *         of the roots next to the seam of a line through the point at the seam, and a
	 *         semantic point moved across the seam and into a certified gap
	 */
	private static String closure(AppGeoCeDG app, EuclidianView view, String label,
			int[] empty, int serial) {
		GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
		LocusDefinition2D definition = published(locus);
		LocusExistenceStructure2D structure = definition.getBranch(PreG9BCu0Corpus.MAIN)
				.getExistenceStructure();
		long certificate = structure.getContinuousValidComponents().stream()
				.filter(ContinuousComponent::isClosedPeriodic).count();
		LocusRenderData2D drawing = new LocusRenderCache2D().getOrBuild(locus,
				PreG9BCu0Corpus.authorPolicy());
		PreG9BCu2AuthorModelExportTest.showOnly(app, label);
		int[] shown = PreG9BCu2AuthorModelExportTest.closedPaths(app, view);
		ContinuousComponent first = structure.getContinuousValidComponents().get(0);
		double middle = definition.getProvider().canonicalize(first.getInterval().getLower() / 2
				+ first.getInterval().getUpper() / 2);
		return "certificate=" + certificate + " drawing=" + closedStrokes(drawing) + " dxf="
				+ PreG9BCu2AuthorModelExportTest.dxf(app, label) + " latex="
				+ (shown[0] - empty[0]) + "/" + (shown[1] - empty[1]) + "/"
				+ (shown[2] - empty[2]) + " zero=" + route(definition, middle,
						SamePositionPolicy.ZERO_LENGTH) + " cycle=" + route(definition, middle,
						SamePositionPolicy.FULL_CYCLE) + " seam=" + seamRoots(app, label,
						definition, serial) + " crossing=" + crossing(app, label, structure,
						size(drawing), serial);
	}

	/** @return strokes of the drawing whose last vertex is their first, bit for bit */
	private static int closedStrokes(LocusRenderData2D drawing) {
		List<LocusRenderData2D.Vertex> vertices = drawing.getVertices();
		int closed = 0;
		int start = 0;
		while (start < vertices.size()) {
			int end = start + 1;
			while (end < vertices.size() && !vertices.get(end).startsSubpath()) {
				end++;
			}
			LocusPoint2D first = vertices.get(start).getPoint();
			LocusPoint2D last = vertices.get(end - 1).getPoint();
			if (end - start > 2 && first.getX() == last.getX() && first.getY() == last.getY()) {
				closed++;
			}
			start = end;
		}
		return closed;
	}

	/** @return diagonal of the bounding box of the vertices of the drawing */
	private static double size(LocusRenderData2D drawing) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (LocusRenderData2D.Vertex vertex : drawing.getVertices()) {
			minX = Math.min(minX, vertex.getPoint().getX());
			minY = Math.min(minY, vertex.getPoint().getY());
			maxX = Math.max(maxX, vertex.getPoint().getX());
			maxY = Math.max(maxY, vertex.getPoint().getY());
		}
		return Math.hypot(maxX - minX, maxY - minY);
	}

	/** @return status of the route between two equal positions at the parameter */
	private static String route(LocusDefinition2D definition, double parameter,
			SamePositionPolicy same) {
		return new LocusMetricRouteResolver2D().resolve(new BetweenPositionsMetricQuery(
				bind(definition, parameter), bind(definition, parameter),
				TraversalDirection.FORWARD, OpenBoundaryPolicy.STRICT, same,
				LocusMetricPolicy2D.initial()), definition).getRouteStatus().name();
	}

	/** @return an error handler that records the errors of a command instead of showing them */
	private static ErrorHandler recording(List<String> errors) {
		return new ErrorHandler() {
			@Override
			public void showError(String message) {
				errors.add(String.valueOf(message));
			}

			@Override
			public void showCommandError(String command, String message) {
				errors.add(command + ": " + message);
			}

			@Override
			public String getCurrentCommand() {
				return null;
			}

			@Override
			public boolean onUndefinedVariables(String string,
					AsyncOperation<String[]> callback) {
				errors.add("undefined: " + string);
				return false;
			}

			@Override
			public void resetError() {
				// Nothing to reset.
			}
		};
	}

	private static LocusMetricRoute2D route(LocusDefinition2D definition, double start,
			double target, TraversalDirection direction, OpenBoundaryPolicy boundary,
			SamePositionPolicy same) {
		return new LocusMetricRouteResolver2D().resolve(new BetweenPositionsMetricQuery(
				bind(definition, start), bind(definition, target), direction, boundary, same,
				LocusMetricPolicy2D.initial()), definition);
	}

	private static MetricPositionBinding2D bind(LocusDefinition2D definition,
			double parameter) {
		return new LocusMetricPositionBinder2D().bind(new LocusSemanticPosition2D(
				definition.getLocusIdentity(), PreG9BCu0Corpus.MAIN,
				definition.getProvider().getProviderId(), parameter), definition);
	}

	/**
	 * Intersects the locus with the line through its point at the seam perpendicular to the
	 * chord to its point {@code 1e-4} after the seam (at the fold of {@code e_1} the chord is
	 * not zero, unlike a chord across the seam).
	 *
	 * @return locations of the roots within {@code 1e-9} of the seam
	 */
	private static String seamRoots(AppGeoCeDG app, String label,
			LocusDefinition2D definition, int serial) {
		LocusPoint2D at;
		LocusPoint2D after;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
			at = definition.evaluate(PreG9BCu0Corpus.MAIN, -Math.PI, session).getPoint();
			after = definition.evaluate(PreG9BCu0Corpus.MAIN, -Math.PI + 1e-4, session)
					.getPoint();
		}
		double norm = Math.hypot(after.getX() - at.getX(), after.getY() - at.getY());
		double nx = (after.getX() - at.getX()) / norm;
		double ny = (after.getY() - at.getY()) / norm;
		GeoLine line = new GeoLine(app.getKernel().getConstruction(), nx, ny,
				-(nx * at.getX() + ny * at.getY()));
		line.setLabel("seamLine" + serial);
		GeoElementND[] created = app.getKernel().getAlgebraProcessor().processAlgebraCommand(
				"seamCut" + serial + "=Intersect(" + label + ",seamLine" + serial + ")", false);
		List<String> locations = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : ((GeoLocusIntersectionResult) created[0])
				.getIntersectionResult().getFiniteSolutions()) {
			if (Math.PI - Math.abs(solution.getRevisionEvidence().getSemanticParameter())
					< 1e-9) {
				locations.add(solution.getClassification().getDomainLocation().name());
			}
		}
		((GeoElement) created[0]).remove();
		line.remove();
		return locations.toString();
	}

	/**
	 * Moves a semantic point of the locus across the seam, from {@code -pi - 2^-10} through
	 * {@code -pi} to {@code -pi + 2^-10}, and into the middle of a certified gap.
	 *
	 * @return {@code defined} when the point is defined at the three parameters with steps
	 *         below a hundredth of the size of the locus, otherwise the causes or the jump,
	 *         followed by the cause of the point in the gap, or {@code none}
	 */
	private static String crossing(AppGeoCeDG app, String label,
			LocusExistenceStructure2D structure, double size, int serial) {
		GeoNumeric u = new GeoNumeric(app.getKernel().getConstruction(), -Math.PI - 0x1p-10);
		u.setLabel("uSeam" + serial);
		GeoPoint point = (GeoPoint) app.getKernel().getAlgebraProcessor().processAlgebraCommand(
				"SSeam" + serial + "=Point(" + label + ",\"" + PreG9BCu0Corpus.MAIN + "\",uSeam"
						+ serial + ")", false)[0];
		AlgoSemanticLocusPoint2D algorithm = (AlgoSemanticLocusPoint2D) point
				.getParentAlgorithm();
		StringBuilder text = new StringBuilder();
		double[] previous = null;
		for (double parameter : new double[] {-Math.PI - 0x1p-10, -Math.PI,
				-Math.PI + 0x1p-10}) {
			u.setValue(parameter);
			u.updateCascade();
			if (!point.isDefined()) {
				text.append(algorithm.getResolutionCause()).append(' ');
				continue;
			}
			double[] here = {point.getInhomX(), point.getInhomY()};
			if (previous != null && Math.hypot(here[0] - previous[0], here[1] - previous[1])
					> size / 100) {
				text.append("jump ");
			}
			previous = here;
		}
		String gap = "none";
		for (Coverage item : structure.getCoverage()) {
			if (item.getState() == CoverageState.CERTIFIED_DOES_NOT_EXIST
					&& item.getNature().certifies()) {
				u.setValue(item.getInterval().getLower() / 2 + item.getInterval().getUpper() / 2);
				u.updateCascade();
				gap = point.isDefined() ? "defined" : algorithm.getResolutionCause().name();
			}
		}
		point.remove();
		u.remove();
		return (text.length() == 0 ? "defined" : text.toString().trim()) + " gap=" + gap;
	}

	/**
	 * @return {@link #ESTIMATE_WITHIN_REFERENCE} when the total length is an estimate whose
	 *         established error amount holds the difference to the reference, plus the
	 *         uncertainty of the reference; {@link #NOT_ESTABLISHED_BY_BUDGET} when it is not
	 *         established with no value and the typed cause of the work budget; otherwise
	 *         the observed result
	 */
	private static String totalLength(AppGeoCeDG app, String label, int serial) {
		LocusMetricResult2D result = ((GeoLocusMetricResult) app.getKernel()
				.getAlgebraProcessor().processAlgebraCommand("totalLength" + serial
						+ "=LocusLength(" + label + ")", false)[0]).getMetricResult();
		String observed = result.getComputationStatus() + " " + result.getGuaranteeClass()
				+ " " + result.getDiagnostics().stream().map(diagnostic -> diagnostic.getCode()
						.name()).toList();
		if (result.getComputationStatus() != MetricComputationStatus.SUCCESS) {
			return observed + (result.getMetricValue().getFiniteValue().isPresent()
					? " value" : " no value");
		}
		LocusDefinition2D definition = published(PreG9BCu0Corpus.locus(app, label));
		ContinuousComponent component = definition.getBranch(PreG9BCu0Corpus.MAIN)
				.getExistenceStructure().getContinuousValidComponents().get(0);
		double coarse = polyline(definition, component, 2048);
		double middle = polyline(definition, component, 4096);
		double fine = polyline(definition, component, 8192);
		double reference = (4 * fine - middle) / 3;
		double uncertainty = Math.abs(reference - (4 * middle - coarse) / 3);
		double value = result.getMetricValue().getFiniteValue().getAsDouble();
		double amount = result.getErrorEvidence().getEstablishedAbsoluteAmount();
		return result.getGuaranteeClass() == MetricGuaranteeClass2D.ESTIMATED
				&& Math.abs(value - reference) <= amount + uncertainty
						? ESTIMATE_WITHIN_REFERENCE : observed + " value " + value
								+ " reference " + reference + " uncertainty " + uncertainty
								+ " amount " + amount;
	}

	/**
	 * @return length of the inscribed polyline of the component: uniform samples around a
	 *         closed periodic component, closed at its first point, and samples clustered like
	 *         a cosine at the ends of an arc, where its speed is unbounded
	 */
	private static double polyline(LocusDefinition2D definition, ContinuousComponent component,
			int chords) {
		double lower = component.getInterval().getLower();
		double upper = component.getInterval().getUpper();
		boolean closed = component.isClosedPeriodic();
		double sum = 0;
		double compensation = 0;
		LocusPoint2D first = null;
		LocusPoint2D previous = null;
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(16)) {
			for (int index = 0; index <= chords; index++) {
				LocusPoint2D point;
				if (closed && index == chords) {
					point = first;
				} else {
					double parameter = index == 0 ? lower : index == chords ? upper
							: closed ? lower + (upper - lower) * index / chords
							: lower + (upper - lower)
									* (1 - Math.cos(Math.PI * index / chords)) / 2;
					LocusEvaluation2D evaluation = definition.evaluateInComponent(
							PreG9BCu0Corpus.MAIN, component.getInterval(), parameter, session);
					if (!evaluation.isValid()) {
						return Double.NaN;
					}
					point = evaluation.getPoint();
				}
				if (first == null) {
					first = point;
				}
				if (previous != null) {
					// Compensated summation keeps the rounding of the sum below the chords.
					double chord = Math.hypot(point.getX() - previous.getX(),
							point.getY() - previous.getY()) - compensation;
					double total = sum + chord;
					compensation = (total - sum) - chord;
					sum = total;
				}
				previous = point;
			}
		}
		return sum;
	}

	/**
	 * @return kind of the result, locations of its roots and whether their arc coordinates
	 *         on the component of {@code o} increase in the order of the result
	 */
	private static String roots(GeoLocusIntersectionResult cut, LocusDefinition2D definition) {
		LocusIntersectionResult2D result = cut.getIntersectionResult();
		ContinuousComponent arc = definition.getBranch(PreG9BCu0Corpus.MAIN)
				.getExistenceStructure().getContinuousValidComponents().get(0);
		List<String> locations = new ArrayList<>();
		double previous = Double.NEGATIVE_INFINITY;
		boolean ordered = true;
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			locations.add(solution.getClassification().getDomainLocation().name());
			double coordinate = arc.getArcCoordinate(solution.getRevisionEvidence()
					.getSemanticParameter());
			ordered &= coordinate > previous;
			previous = coordinate;
		}
		return result.getGeometryKind() + " " + locations + (ordered ? " ordered"
				: " unordered");
	}

	private static List<String> tokens(GeoLocusIntersectionResult cut) {
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : cut.getIntersectionResult()
				.getFiniteSolutions()) {
			tokens.add(solution.getIdentity().getRootToken());
		}
		return tokens;
	}

	/**
	 * @return the published definition, after checking that it publishes the certificate of
	 *         its revision (step T11); the definition binds the structure to its revision, so
	 *         the two compare by their content signature
	 */
	private static LocusDefinition2D published(GeoLocusV2 locus) {
		LocusDefinition2D definition = locus.getSemanticDefinition();
		assertEquals(certificate(locus).getStructure().getSemanticSignature(),
				definition.getBranch(PreG9BCu0Corpus.MAIN).getExistenceStructure()
						.getSemanticSignature());
		return definition;
	}

	private static Result certificate(GeoLocusV2 locus) {
		Result result = ((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
				.getHybridExistenceCertificate();
		if (result == null) {
			throw new IllegalStateException("No certificate");
		}
		return result;
	}

	private static double distance(LocusPoint2D first, LocusPoint2D second) {
		return Math.hypot(first.getX() - second.getX(), first.getY() - second.getY());
	}
}
