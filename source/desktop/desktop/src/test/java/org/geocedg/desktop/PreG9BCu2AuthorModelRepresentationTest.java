/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.geocedg.common.euclidian.draw.DrawLocusV2;
import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Fidelity;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Mark;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.MarkKind;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.TurnExemption;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Vertex;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.VertexSource;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBoundaryImage2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusCertifiedFold2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusDriverDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.MetricWorkBudget2D;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.desktop.euclidian.EuclidianViewD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU2 step T10: drawing policy {@value LocusRenderPolicy2D#VERSION} on the author
 * model, in the author's view, with the certificates that step T11 publishes. Matrix rows
 * V-B06-1, V-B06-2, V-B06-3, V-REP-1 and V-AVAIL-2 (model part). The CU0 references of {@code pins.B06} and {@code pins.B07} are
 * inverted in step T12. Corrective descendant T13-R4 (decision D-CU2-T13-R4): V-INV-1 in the
 * real Graphics View.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelRepresentationTest {
	private static final List<String> CLOSED = List.of("o", "v", "w", "a_1", "e_1", "o_1");
	private static final List<String> FIRST_FAMILY = List.of("o", "v", "w", "a_1");
	private static final double SCALE = PreG9BCu0Corpus.AUTHOR_SCALE;
	private static final double THETA = LocusRenderPolicy2D.DEFAULT_MAXIMUM_TURN_DEGREES;

	@TempDir
	Path work;

	/**
	 * V-B06-1 and V-B06-2: at the author's parameters and in a bite, every turn between
	 * visible chords outside an exemption is within {@code theta_max}, the turn at the closing
	 * vertex of each closed stroke included, measured cyclically from its last chord to its
	 * first; the exemptions are the bands of the two certified folds of {@code e_1} and nothing
	 * else; every drawing is complete, one stroke per certified component, closed exactly for
	 * the closed periodic components.
	 */
	@Test
	void turnsAreBoundedOutsideTheCertifiedFolds() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Object> values = new LinkedHashMap<>();
			for (String label : CLOSED) {
				LocusRenderData2D closed = publishAndRender(app, label, null,
						PreG9BCu0Corpus.authorPolicy());
				values.put(label, summary(closed) + "/" + closedStrokes(closed));
			}
			GeoLocusV2 e1 = PreG9BCu0Corpus.locus(app, "e_1");
			List<LocusCertifiedFold2D> folds = certificate(e1).getStructure()
					.getCertifiedFolds();
			LocusRenderData2D drawing = new LocusRenderCache2D().getOrBuild(e1,
					PreG9BCu0Corpus.authorPolicy());
			values.put("e_1.exempt", exemptOutsideFolds(drawing, folds) + "/"
					+ exemptNear(drawing, folds.get(0)) + "/" + exemptNear(drawing, folds.get(1)));
			PreG9BCu0Corpus.setNumber(app, "excCyl", 3.85);
			for (String label : FIRST_FAMILY) {
				LocusRenderData2D bite = publishAndRender(app, label, null,
						PreG9BCu0Corpus.authorPolicy());
				values.put("bite." + label, summary(bite) + "/" + closedStrokes(bite));
			}
			return values;
		});
		// Each closed stroke is measured cyclically at its closing vertex too (V-B06-1).
		for (String label : CLOSED) {
			assertEquals("COMPLETE/0/1/true/1", row.get(label), label + " " + row);
		}
		for (String label : FIRST_FAMILY) {
			assertEquals("COMPLETE/0/1/true/0", row.get("bite." + label), label + " " + row);
		}
		assertEquals("0/true/true", row.get("e_1.exempt"), row.toString());
	}

	/**
	 * V-B06-3: the same view gives identical vertices; another zoom or output resolution
	 * changes the vertices, never the ends, the closure or the number of strokes. Checked on the
	 * six closed loci at the author's parameters and on the first family in the bites at
	 * excCyl 3.7 and 3.85 and at the first binary64 of the bite ({@code bc}), whose arcs cross
	 * the seam.
	 */
	@Test
	void drawingsAreDeterministicAndTheirTopologyViewInvariant() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Object> values = new LinkedHashMap<>();
			for (String label : CLOSED) {
				values.put(label, invariance(app, label));
			}
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			for (double excCyl : new double[] {3.7, 3.85,
				Double.longBitsToDouble(0x400d416bb67a38bcL)}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", excCyl);
				for (String label : FIRST_FAMILY) {
					values.put(label + "@" + excCyl, invariance(app, label));
				}
			}
			return values;
		});
		for (Map.Entry<String, Object> entry : row.entrySet()) {
			assertEquals("true/true/true/true", entry.getValue(), entry.getKey() + " " + row);
		}
		assertEquals(CLOSED.size() + 3 * FIRST_FAMILY.size(), row.size(), row.toString());
	}

	/**
	 * @return {@code same/zoom/resolution/refined}: identical vertices with the author's view
	 *         again, the same topology with twice the zoom and with a quarter of the chord
	 *         tolerance, and at least as many vertices with the zoom
	 */
	private static String invariance(AppGeoCeDG app, String label) {
		LocusRenderPolicy2D author = PreG9BCu0Corpus.authorPolicy();
		LocusRenderPolicy2D zoom = LocusRenderPolicy2D.adaptive(1,
				PreG9BCu0Corpus.AUTHOR_WIDTH, PreG9BCu0Corpus.AUTHOR_HEIGHT, 2 * SCALE,
				2 * SCALE, 32, 0.75, 12);
		LocusRenderPolicy2D dpi = LocusRenderPolicy2D.adaptive(1,
				PreG9BCu0Corpus.AUTHOR_WIDTH, PreG9BCu0Corpus.AUTHOR_HEIGHT, SCALE, SCALE,
				32, 0.75 / 4, 12);
		LocusRenderData2D first = publishAndRender(app, label, null, author);
		GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
		LocusRenderData2D again = new LocusRenderCache2D().getOrBuild(locus, author);
		LocusRenderData2D zoomed = new LocusRenderCache2D().getOrBuild(locus, zoom);
		LocusRenderData2D fine = new LocusRenderCache2D().getOrBuild(locus, dpi);
		return (fingerprint(first).equals(fingerprint(again))) + "/"
				+ topology(first).equals(topology(zoomed)) + "/"
				+ topology(first).equals(topology(fine)) + "/"
				+ (zoomed.getVertices().size() >= first.getVertices().size());
	}

	/**
	 * V-B07-1 (step T11; inverts {@code pins.B07} of CU0) and V-B07-2 (model part): in the
	 * bites at excCyl 3.7, 3.85 and 3.9 each locus of the first family is drawn as one open
	 * stroke along its certified arc across the seam, whose end vertices lie within 0.75 px of
	 * the images of the certified ends, which enclose the analytic ends (until CU2 the stroke
	 * stopped 6 to 24 px short), and both included ends evaluate (finding H-CU2A-3); at each end
	 * the last span, taken in the square-root coordinate {@code t = t* +- s^2}, meets the chord
	 * tolerance with no more vertices than a regular span, plus two.
	 */
	@Test
	void aBiteIsDrawnToTheImagesOfItsCertifiedEnds() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			Map<String, Object> values = new LinkedHashMap<>();
			for (double excCyl : new double[] {3.7, 3.85, 3.9}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", excCyl);
				for (String label : FIRST_FAMILY) {
					GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
					ContinuousComponent arc = certificate(locus).getStructure()
							.getContinuousValidComponents().get(0);
					LocusDefinition2D definition = locus.getSemanticDefinition();
					LocusPoint2D start = componentEnd(definition, arc, false);
					LocusPoint2D end = componentEnd(definition, arc, true);
					boolean evaluated;
					try (LocusEvaluationSession2D session = LocusEvaluationSession2D
							.memoizing(4)) {
						evaluated = definition.evaluateComponentEnd(PreG9BCu0Corpus.MAIN,
								arc.getStart(), false, session).isValid()
								&& definition.evaluateComponentEnd(PreG9BCu0Corpus.MAIN,
										arc.getEnd(), true, session).isValid();
					}
					List<Vertex> vertices = new LocusRenderCache2D().getOrBuild(locus,
							PreG9BCu0Corpus.authorPolicy()).getVertices();
					values.put(label + "@" + excCyl, arc.crossesSeam() + "/"
							+ vertices.stream().filter(Vertex::startsSubpath).count() + "/"
							+ (distance(vertices.get(0).getPoint(), start) * SCALE <= 0.75) + "/"
							+ (distance(vertices.get(vertices.size() - 1).getPoint(), end)
									* SCALE <= 0.75) + "/" + evaluated + "/"
							+ squareRootEndSpans(definition, arc, vertices));
				}
			}
			return values;
		});
		for (Map.Entry<String, Object> entry : row.entrySet()) {
			assertEquals("true/1/true/true/true/true", entry.getValue(),
					entry.getKey() + " " + row);
		}
	}

	/**
	 * V-B07-2: at each end of the arc, the vertices of the last span of the minimum partition
	 * are no more than those of the most refined regular span of the arc plus two (a span taken
	 * in the parameter would refine to the resolution there), and every chord there deviates
	 * at most 0.75 px from the curve at its midpoint in the square-root coordinate.
	 */
	private static String squareRootEndSpans(LocusDefinition2D definition,
			ContinuousComponent arc, List<Vertex> vertices) {
		double lower = arc.getInterval().getLower();
		double upper = arc.getInterval().getUpper();
		int spans = 1 << LocusRenderPolicy2D.DEFAULT_MINIMUM_DEPTH;
		double span = (upper - lower) / spans;
		int[] counts = new int[spans];
		for (Vertex vertex : vertices) {
			double lifted = lifted(vertex.getSemanticParameter(), lower);
			int index = (int) Math.min(spans - 1, Math.max(0, Math.ceil((lifted - lower) / span)
					- 1));
			counts[index] += lifted > lower ? 1 : 0;
		}
		int regular = 0;
		for (int index = 1; index + 1 < spans; index++) {
			regular = Math.max(regular, counts[index]);
		}
		if (counts[0] > regular + 2 || counts[spans - 1] > regular + 2) {
			return "vertices " + java.util.Arrays.toString(counts);
		}
		for (int index = 1; index < vertices.size(); index++) {
			double pa = lifted(vertices.get(index - 1).getSemanticParameter(), lower);
			double pb = lifted(vertices.get(index).getSemanticParameter(), lower);
			double middle;
			if (pb <= lower + span) {
				double sa = Math.sqrt(pa - lower);
				double sb = Math.sqrt(pb - lower);
				middle = lower + Math.pow(sa + (sb - sa) / 2, 2);
			} else if (pa >= upper - span) {
				double sa = Math.sqrt(upper - pa);
				double sb = Math.sqrt(upper - pb);
				middle = upper - Math.pow(sa + (sb - sa) / 2, 2);
			} else {
				continue;
			}
			LocusPoint2D curve = evaluate(definition, middle);
			LocusPoint2D a = vertices.get(index - 1).getPoint();
			LocusPoint2D b = vertices.get(index).getPoint();
			double dx = b.getX() - a.getX();
			double dy = b.getY() - a.getY();
			double length = Math.hypot(dx, dy);
			double across = length == 0 ? distance(curve, a)
					: Math.abs((curve.getX() - a.getX()) * dy - (curve.getY() - a.getY()) * dx)
							/ length;
			if (across * SCALE > 0.75) {
				return "chord " + index + " deviates " + across * SCALE + " px";
			}
		}
		return "true";
	}

	/** @return the parameter lifted by a period when it lies before the start of an arc */
	private static double lifted(double parameter, double start) {
		return parameter < start ? parameter + 2 * Math.PI : parameter;
	}

	/**
	 * V-B07-3 (F-CU2-2): near the critical value, at excCyl 3.657, 3.66 and the first
	 * binary64 of the bite, the real gap between the two transitions is never bridged: one
	 * stroke along the arc, open, with no vertex inside the certified nonexistence.
	 */
	@Test
	void noStrokeBridgesTheRealGapNearTheCriticalValue() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1);
			Map<String, Object> values = new LinkedHashMap<>();
			for (double excCyl : new double[] {3.657, 3.66,
				Double.longBitsToDouble(0x400d416bb67a38bcL)}) {
				PreG9BCu0Corpus.setNumber(app, "excCyl", excCyl);
				for (String label : FIRST_FAMILY) {
					GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
					LocusExistenceStructure2D structure = certificate(locus).getStructure();
					List<Vertex> vertices = new LocusRenderCache2D().getOrBuild(locus,
							PreG9BCu0Corpus.authorPolicy()).getVertices();
					LocusDriverDomainProvider2D provider = locus.getSemanticDefinition()
							.getProvider();
					long inGap = vertices.stream().filter(vertex -> structure.getCoverage()
							.stream().anyMatch(item -> item.getState()
									== CoverageState.CERTIFIED_DOES_NOT_EXIST
									&& item.getInterval().contains(provider.canonicalize(
											vertex.getSemanticParameter()), 0))).count();
					values.put(label + "@" + excCyl, structure.getContinuousValidComponents()
							.size() + "/" + vertices.stream().filter(Vertex::startsSubpath)
									.count() + "/" + inGap + "/" + (distance(vertices.get(0)
											.getPoint(), vertices.get(vertices.size() - 1)
											.getPoint()) > 0));
				}
			}
			return values;
		});
		for (Map.Entry<String, Object> entry : row.entrySet()) {
			assertEquals("1/1/0/true", entry.getValue(), entry.getKey() + " " + row);
		}
	}

	/**
	 * V-REP-1: the excluded end of {@code e_2} at {@code t_N} is drawn to its one-sided limit
	 * {@code w_1(t_N^-) = L}, within the chord tolerance of the curve just below it; the
	 * included start is the image of {@code t_N}; there is no closing segment.
	 */
	@Test
	void theExcludedEndOfE2IsItsOneSidedLimit() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			LocusRenderData2D drawing = publishAndRender(app, "e_2", null,
					PreG9BCu0Corpus.authorPolicy());
			Map<String, Object> values = new LinkedHashMap<>();
			Vertex first = drawing.getVertices().get(0);
			Vertex last = drawing.getVertices().get(drawing.getVertices().size() - 1);
			LocusPoint2D below = evaluate(e2.getSemanticDefinition(), -1e-9);
			LocusPoint2D origin = evaluate(e2.getSemanticDefinition(), 0);
			values.put("ends", first.getSource() + "/" + last.getSource());
			values.put("limitToBelow", distance(last.getPoint(), below) * SCALE <= 0.75);
			values.put("startIsOrigin", distance(first.getPoint(), origin) == 0);
			values.put("noClosure", distance(last.getPoint(), first.getPoint()) * SCALE > 100);
			values.put("summary", summary(drawing));
			return values;
		});
		assertEquals("EVALUATED/ONE_SIDED_LIMIT", row.get("ends"), row.toString());
		assertEquals(true, row.get("limitToBelow"), row.toString());
		assertEquals(true, row.get("startIsOrigin"), row.toString());
		assertEquals(true, row.get("noClosure"), row.toString());
		assertEquals("COMPLETE/0/1/true", row.get("summary"), row.toString());
	}

	/**
	 * V-AVAIL-2 (model part) and V-E2-5: a test policy whose work budget makes the length
	 * {@code w_1} fail in the slice of {@code e_2} at one driver value: that point is
	 * {@code EVALUATION_FAILED}, the existence does not change, and the stroke is cut there
	 * with a computational mark and a partial fidelity. The failure is local, since the
	 * budget is part of the key of the metric index: 32 other drivers of the same revision are
	 * evaluated, and the injection causes exactly one gap. The refinement around it reaches
	 * drivers where the length estimate of the product fails without any injection
	 * ({@code PARAMETER_RESOLUTION_REACHED}, the estimator of {@code CU1}; recorded as a
	 * finding of the corrective mandate), whose gaps the unchanged definition shows as well. A
	 * failure is a computational gap, never nonexistence, never an interpolation.
	 */
	@Test
	void aFailingLengthCutsTheStrokeOfE2() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			GeoLocusV2 e2 = PreG9BCu0Corpus.locus(app, "e_2");
			ReconstructibleLocusEvaluator2D live = evaluator(e2);
			AlgoLocusBetweenMetricV2 length = sliceLength(live);
			double failing = Math.PI / 2;
			LocusEvaluator2D policy = failingLength(live, length, failing);
			final LocusExistenceStructure2D structure = certificate(e2).getStructure();
			LocusDefinition2D unchanged = e2.getSemanticDefinition();
			LocusRenderData2D drawing = publishAndRender(app, "e_2", policy,
					PreG9BCu0Corpus.authorPolicy());
			Map<String, Object> values = new LinkedHashMap<>();
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
				values.put("status", e2.getSemanticDefinition().evaluate(PreG9BCu0Corpus.MAIN,
						failing, session).getStatus());
			}
			int valid = 0;
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
				for (int sample = 0; sample < 32; sample++) {
					double parameter = -Math.PI + 2 * Math.PI * (sample + 0.5) / 32;
					valid += e2.getSemanticDefinition().evaluate(PreG9BCu0Corpus.MAIN, parameter,
							session).isValid() ? 1 : 0;
				}
			}
			values.put("valid", valid);
			List<Mark> gaps = drawing.getMarks().stream().filter(mark ->
					mark.getKind() == MarkKind.COMPUTATIONAL_GAP).collect(Collectors.toList());
			// The gaps that the injection causes: those whose driver the unchanged definition
			// evaluates.
			int caused = 0;
			List<String> independent = new ArrayList<>();
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
				for (Mark gap : gaps) {
					LocusEvaluation2D without = unchanged.evaluate(PreG9BCu0Corpus.MAIN,
							gap.getParameter(), session);
					if (without.isValid()) {
						caused++;
					} else {
						independent.add(Double.toHexString(gap.getParameter()) + ":"
								+ without.getDiagnostic());
					}
				}
			}
			values.put("gaps", caused);
			values.put("independent", independent.stream().allMatch(text ->
					text.contains("COMPUTATIONAL_FAILURE")) + " " + independent);
			values.put("onlyGaps", gaps.size() == drawing.getMarks().size());
			values.put("gapAt", gaps.isEmpty() ? "none"
					: Math.abs(gaps.get(0).getParameter() - failing) <= 4 * Math.ulp(failing));
			values.put("strokes", strokes(drawing) == gaps.size() + 1);
			values.put("fidelity", drawing.getFidelity());
			values.put("existence", structure.getSemanticSignature().equals(
					e2.getSemanticDefinition().getBranch(PreG9BCu0Corpus.MAIN)
							.getExistenceStructure().getSemanticSignature()));
			return values;
		});
		assertEquals("EVALUATION_FAILED", String.valueOf(row.get("status")), row.toString());
		assertEquals(1, row.get("gaps"), row.toString());
		assertTrue(String.valueOf(row.get("independent")).startsWith("true"), row.toString());
		assertEquals(32, row.get("valid"), row.toString());
		assertEquals(true, row.get("onlyGaps"), row.toString());
		assertEquals(true, row.get("gapAt"), row.toString());
		assertEquals(true, row.get("strokes"), row.toString());
		assertEquals(Fidelity.PARTIAL, row.get("fidelity"), row.toString());
		assertEquals(true, row.get("existence"), row.toString());
	}

	/**
	 * V-INV-1 (corrective descendant T13-R4): the zoom, the output resolution and the size of
	 * the Graphics View are presentation parameters. The complete author model, with the
	 * synthetic point components and the band of an unlabelled second output of matrix
	 * section 1 added to it, is drawn in its real view, which is resized, zoomed and moved,
	 * and exported at 2 and 4 times its resolution, in the author's state, at excCyl 3.7, at
	 * the first binary64 of the bite and with rbCone 8. Every change draws the loci of the
	 * view again for the new presentation: their render caches build or their strokes move on
	 * screen; every visible locus is drawn. The mathematical data of every locus, which lie in the
	 * projection planes of the construction, stay the same objects and bits: the published
	 * certificate, the semantic revision and the slice fingerprint, the roots and exact
	 * evidence, and the points of the branch at 16 parameters; no certificate is computed, no
	 * existence box is evaluated and no slice is built.
	 */
	@Test
	void zoomOutputResolutionAndViewSizeChangeNoCertificate() throws Exception {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG opened = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			synthetic(opened);
			return opened;
		});
		EuclidianView view = PreG9BR6PlusBPictureFidelityTest.sized(app.getEuclidianView1());
		List<String> steps = List.of("size 1200x800", "size 400x300", "zoom 500",
				"zoom 5", "zoom 50", "export x2", "export x4");
		Map<String, Runnable> states = new LinkedHashMap<>();
		states.put("author", () -> { });
		states.put("excCyl=3.7", () -> PreG9BCu0Corpus.setNumber(app, "excCyl", 3.7));
		states.put("bite", () -> PreG9BCu0Corpus.setNumber(app, "excCyl",
				Double.longBitsToDouble(0x400d416bb67a38bcL)));
		states.put("rbCone=8", () -> PreG9BCu0Corpus.setNumber(app, "rbCone", 8));
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			List<String> loci = new ArrayList<>(CLOSED);
			loci.add("e_2");
			loci.add("spq");
			loci.add("slr2");
			Map<String, Object> values = new LinkedHashMap<>();
			for (Map.Entry<String, Runnable> state : states.entrySet()) {
				state.getValue().run();
				view.updateAllDrawables(true);
				Map<String, Result> certificates = new LinkedHashMap<>();
				List<String> drawn = new ArrayList<>();
				List<String> visible = new ArrayList<>();
				for (String label : loci) {
					GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
					certificates.put(label, certificate(locus));
					if (view.getDrawableFor(locus) instanceof DrawLocusV2) {
						drawn.add(label);
					}
					if (locus.isEuclidianVisible()) {
						visible.add(label);
					}
				}
				final String mathematics = mathematics(app, loci);
				final String work = certificationWork(app, loci);
				values.put(state.getKey() + ".drawn", drawn.containsAll(visible)
						&& drawn.containsAll(FIRST_FAMILY) && drawn.contains("spq")
						&& drawn.contains("slr2") ? "all visible" : drawn + " of " + visible);
				Map<String, Runnable> changes = new LinkedHashMap<>();
				changes.put(steps.get(0), () -> resize(view, 1200, 800));
				changes.put(steps.get(1), () -> resize(view, 400, 300));
				changes.put(steps.get(2), () -> view.setCoordSystem(300, 400, 500, 500));
				changes.put(steps.get(3), () -> view.setCoordSystem(10, 10, 5, 5));
				changes.put(steps.get(4), () -> view.setCoordSystem(50, 200, 50, 50));
				changes.put(steps.get(5), () -> view.getExportImage(2));
				changes.put(steps.get(6), () -> view.getExportImage(4));
				String previous = presentation(view, app, drawn);
				for (Map.Entry<String, Runnable> change : changes.entrySet()) {
					change.getValue().run();
					view.updateAllDrawables(true);
					String key = state.getKey() + " " + change.getKey();
					String now = presentation(view, app, drawn);
					values.put(key + ".presentation", !now.equals(previous));
					previous = now;
					boolean same = true;
					for (String label : loci) {
						same &= certificates.get(label) == certificate(PreG9BCu0Corpus.locus(app,
								label));
					}
					values.put(key + ".certificates", same);
					values.put(key + ".mathematics", mathematics.equals(mathematics(app, loci)));
					values.put(key + ".work", work.equals(certificationWork(app, loci)));
				}
			}
			return values;
		});
		for (String state : states.keySet()) {
			assertEquals("all visible", row.get(state + ".drawn"), state + " " + row);
			for (String step : steps) {
				String key = state + " " + step;
				assertEquals(true, row.get(key + ".presentation"), key + " " + row);
				assertEquals(true, row.get(key + ".certificates"), key + " " + row);
				assertEquals(true, row.get(key + ".mathematics"), key + " " + row);
				assertEquals(true, row.get(key + ".work"), key + " " + row);
			}
		}
	}

	// ---------------------------------------------------------------- helpers

	/**
	 * Adds the synthetic constructions of matrix section 1 to the model, with labels that the
	 * model does not use: the indexed first output of the line of the two point components
	 * ({@code spq}) and the indexed second output of a ray of the critical configuration, with
	 * its band ({@code slr2}).
	 */
	private static void synthetic(AppGeoCeDG app) {
		for (String command : List.of("ps=Circle((0,0),25)", "Ps=(-25,50)",
				"gs=Circle((15,70),40)", "Ns=Point(gs)", "gs2=Circle((35,-45),40)",
				"Ns2=Point(gs2)")) {
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
		}
		PreG9BCu0Corpus.movePathPoint(app, "Ns", Math.atan2(-4, -3));
		PreG9BCu0Corpus.movePathPoint(app, "Ns2", 0.3);
		for (String command : List.of("ts=Line(Ps,Ns)", "Qs=Intersect(ps,ts,1)",
				"spq=LocusV2(Qs,Ns)", "ts2=Ray(Ps,Ns2)", "Rs2=Intersect(ps,ts2,2)",
				"slr2=LocusV2(Rs2,Ns2)")) {
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
		}
		app.getKernel().updateConstruction(false);
	}

	private static void resize(EuclidianView view, int width, int height) {
		((EuclidianViewD) view).getJPanel().setSize(width, height);
		view.updateSize();
	}

	/** @return render cache builds, last build size and screen bounds of each drawn locus */
	private static String presentation(EuclidianView view, AppGeoCeDG app, List<String> drawn) {
		StringBuilder text = new StringBuilder();
		for (String label : drawn) {
			DrawLocusV2 drawable = (DrawLocusV2) view.getDrawableFor(
					PreG9BCu0Corpus.locus(app, label));
			GRectangle bounds = drawable.getBounds();
			text.append(label).append(':').append(drawable.getRenderCache().getBuilds())
					.append('/').append(drawable.getRenderCache().getLastBuildVertices())
					.append('/').append(bounds == null ? "none" : bounds.getX() + ","
							+ bounds.getY() + "," + bounds.getWidth() + "," + bounds.getHeight())
					.append(';');
		}
		return text.toString();
	}

	/**
	 * @return the mathematical data of each locus: semantic revision, slice fingerprint,
	 *         certificate, roots, exact evidence and the points of the branch at 16 parameters
	 */
	private static String mathematics(AppGeoCeDG app, List<String> loci)
			throws ReflectiveOperationException {
		StringBuilder text = new StringBuilder();
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
			for (String label : loci) {
				GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
				AlgoDependentPointLocusV2 algorithm =
						(AlgoDependentPointLocusV2) locus.getParentAlgorithm();
				Result result = certificate(locus);
				text.append(label).append(" revision=").append(locus.getSemanticRevision())
						.append(" fingerprint=").append(ReconstructionSliceFingerprint2D.compute(
								algorithm.getReconstructionSliceGeos(),
								List.of(sliceRoot(algorithm, "state"),
										sliceRoot(algorithm, "coordinate"))))
						.append(" certificate=").append(result.getStructure()
								.getSemanticSignature())
						.append(" roots=").append(result.getRoots())
						.append(" exact=").append(result.getExactEvidence()).append(" points=");
				for (int index = 0; index < 16; index++) {
					LocusPoint2D point = PreG9BCu0Corpus.point(locus,
							-Math.PI + 2 * Math.PI * (index + 0.25) / 16, session);
					text.append(point == null ? "undefined" : Double.toHexString(point.getX())
							+ "," + Double.toHexString(point.getY())).append(';');
				}
				text.append('\n');
			}
		}
		return text.toString();
	}

	/** @return certificate computations, existence boxes and slice builds of each locus */
	private static String certificationWork(AppGeoCeDG app, List<String> loci) {
		StringBuilder text = new StringBuilder();
		for (String label : loci) {
			GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
			text.append(label).append('=').append(locus.getInstrumentation()
					.getCertificationWork(LocusCertificationWork2D.CERTIFICATE_COMPUTATION))
					.append('/').append(locus.getInstrumentation().getCertificationWork(
							LocusCertificationWork2D.EXISTENCE_BOX))
					.append('/').append(locus.getInstrumentation().getDependencySliceBuilds())
					.append(';');
		}
		return text.toString();
	}

	private static GeoElement sliceRoot(AlgoDependentPointLocusV2 algorithm, String name)
			throws ReflectiveOperationException {
		Field field = AlgoDependentPointLocusV2.class.getDeclaredField(name);
		field.setAccessible(true);
		return (GeoElement) field.get(algorithm);
	}

	/**
	 * Publishes the certificate of a locus again, with the live evaluator unless another is
	 * given (a test policy), and draws it. Step T11 publishes the same certificate; the
	 * republication only installs the test policy.
	 */
	private static LocusRenderData2D publishAndRender(AppGeoCeDG app, String label,
			LocusEvaluator2D evaluator, LocusRenderPolicy2D policy) {
		GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
		LocusExistenceStructure2D structure = certificate(locus).getStructure();
		LocusDefinition2D published = locus.getSemanticDefinition();
		LocusBranch2D branch = published.getBranch(PreG9BCu0Corpus.MAIN);
		LocusBranch2D certified = new LocusBranch2D(branch.getBranchKey(),
				branch.getDeclaredDriverDomain(), structure, branch.getOrientation(),
				branch.getProvenance(), branch.getLineage(), branch.getProperties(),
				branch.getQuality());
		locus.publishSemanticDefinition(new LocusDefinition2D(published.getLocusIdentity(),
				published.getSemanticRevision() + 1, published.getDefinitionStatus(),
				published.getProvider(), List.of(certified), evaluator == null
						? published.getEvaluatorCapability() : evaluator,
				published.getDeterminism(), "t10-published|" + structure.getCertifierContract(),
				locus.getInstrumentation()));
		return new LocusRenderCache2D().getOrBuild(locus, policy);
	}

	/**
	 * The live evaluator of {@code e_2}, with a work budget of its slice length that fails
	 * at one driver value: a test policy of matrix row V-AVAIL-2.
	 */
	private static LocusEvaluator2D failingLength(ReconstructibleLocusEvaluator2D live,
			AlgoLocusBetweenMetricV2 length, double failing) throws ReflectiveOperationException {
		Field field = AlgoLocusBetweenMetricV2.class.getDeclaredField("policy");
		field.setAccessible(true);
		LocusMetricPolicy2D policy = (LocusMetricPolicy2D) field.get(length);
		LocusMetricPolicy2D starved = policy.withWorkBudget(new MetricWorkBudget2D(3, 1, 1));
		return new LocusEvaluator2D() {
			@Override
			public LocusEvaluation2D evaluate(LocusDefinition2D definition,
					LocusBranch2D branch, double parameter, LocusEvaluationSession2D session) {
				if (Math.abs(parameter - failing) > 4 * Math.ulp(failing)) {
					return live.evaluate(definition, branch, parameter, session);
				}
				try {
					field.set(length, starved);
					return live.evaluate(definition, branch, parameter, session);
				} catch (IllegalAccessException exception) {
					throw new IllegalStateException(exception);
				} finally {
					try {
						field.set(length, policy);
					} catch (IllegalAccessException exception) {
						throw new IllegalStateException(exception);
					}
				}
			}

			@Override
			public LocusEvaluation2D evaluateOneSidedLimit(LocusDefinition2D definition,
					LocusBranch2D branch, ContinuousComponent component, boolean upperEnd,
					LocusEvaluationSession2D session) {
				return live.evaluateOneSidedLimit(definition, branch, component, upperEnd,
						session);
			}

			@Override
			public LocusBoundaryImage2D encloseBoundaryImage(LocusDefinition2D definition,
					LocusBranch2D branch, double parameterLower, double parameterUpper,
					LocusBoundarySource2D source) {
				return live.encloseBoundaryImage(definition, branch, parameterLower,
						parameterUpper, source);
			}
		};
	}

	/**
	 * @return {@code fidelity/marks/strokes/turns}, the last true when every turn between
	 *         visible chords outside the exemptions is within {@code theta_max}
	 */
	private static String summary(LocusRenderData2D drawing) {
		return drawing.getFidelity() + "/" + drawing.getMarks().size() + "/" + strokes(drawing)
				+ "/" + (maximumTurn(drawing) <= THETA + 1e-9);
	}

	private static long strokes(LocusRenderData2D drawing) {
		return drawing.getVertices().stream().filter(Vertex::startsSubpath).count();
	}

	/** @return strokes whose last vertex is their first, within a millionth of a pixel */
	private static int closedStrokes(LocusRenderData2D drawing) {
		List<Vertex> vertices = drawing.getVertices();
		int closed = 0;
		int start = 0;
		while (start < vertices.size()) {
			int end = start + 1;
			while (end < vertices.size() && !vertices.get(end).startsSubpath()) {
				end++;
			}
			if (end - 1 > start + 1 && distance(vertices.get(start).getPoint(),
					vertices.get(end - 1).getPoint()) * SCALE <= 1e-6) {
				closed++;
			}
			start = end;
		}
		return closed;
	}

	/**
	 * @return largest turn between visible chords outside the exemptions, in degrees; on a
	 *         closed stroke, whose last vertex is its first, the turn at the closing vertex is
	 *         measured cyclically, from the last chord to the first (V-B06-1)
	 */
	private static double maximumTurn(LocusRenderData2D drawing) {
		List<Vertex> vertices = drawing.getVertices();
		double maximum = 0;
		for (int index = 1; index + 1 < vertices.size(); index++) {
			int previous = visibleNeighbour(vertices, index, -1);
			int next = visibleNeighbour(vertices, index, 1);
			if (previous < 0 || next < 0) {
				continue;
			}
			boolean exempt = false;
			for (int inside = previous + 1; inside < next; inside++) {
				exempt |= vertices.get(inside).getTurnExemption() != TurnExemption.NONE;
			}
			if (!exempt) {
				maximum = Math.max(maximum, turnDegrees(vertices.get(previous).getPoint(),
						vertices.get(index).getPoint(), vertices.get(next).getPoint()));
			}
		}
		int start = 0;
		while (start < vertices.size()) {
			int end = start + 1;
			while (end < vertices.size() && !vertices.get(end).startsSubpath()) {
				end++;
			}
			int last = end - 1;
			if (last > start + 1 && distance(vertices.get(start).getPoint(),
					vertices.get(last).getPoint()) * SCALE <= 1e-6) {
				int previous = visibleNeighbour(vertices, last, -1);
				int next = visibleNeighbour(vertices, start, 1);
				boolean exempt = previous < 0 || next < 0;
				for (int inside = Math.max(previous, start) + 1; !exempt && inside <= last;
						inside++) {
					exempt = vertices.get(inside).getTurnExemption() != TurnExemption.NONE;
				}
				for (int inside = start; !exempt && inside < next; inside++) {
					exempt = vertices.get(inside).getTurnExemption() != TurnExemption.NONE;
				}
				if (!exempt) {
					maximum = Math.max(maximum, turnDegrees(vertices.get(previous).getPoint(),
							vertices.get(start).getPoint(), vertices.get(next).getPoint()));
				}
			}
			start = end;
		}
		return maximum;
	}

	private static int visibleNeighbour(List<Vertex> vertices, int index, int step) {
		LocusPoint2D point = vertices.get(index).getPoint();
		for (int current = index + step; current >= 0 && current < vertices.size();
				current += step) {
			if (step > 0 ? vertices.get(current).startsSubpath()
					: vertices.get(current + 1).startsSubpath()) {
				return -1;
			}
			if (distance(vertices.get(current).getPoint(), point) * SCALE > 1e-6) {
				return current;
			}
		}
		return -1;
	}

	private static double turnDegrees(LocusPoint2D a, LocusPoint2D v, LocusPoint2D b) {
		double x1 = v.getX() - a.getX();
		double y1 = v.getY() - a.getY();
		double x2 = b.getX() - v.getX();
		double y2 = b.getY() - v.getY();
		return Math.toDegrees(Math.atan2(Math.abs(x1 * y2 - y1 * x2), x1 * x2 + y1 * y2));
	}

	/** @return exempt vertices outside every fold band, which must be none */
	private static long exemptOutsideFolds(LocusRenderData2D drawing,
			List<LocusCertifiedFold2D> folds) {
		return drawing.getVertices().stream().filter(vertex ->
				vertex.getTurnExemption() != TurnExemption.NONE
						&& (vertex.getTurnExemption() != TurnExemption.FOLD_BAND
								|| folds.stream().noneMatch(fold -> fold.containsCanonical(
										vertex.getSemanticParameter(), 2 * Math.PI))))
				.count();
	}

	private static boolean exemptNear(LocusRenderData2D drawing, LocusCertifiedFold2D fold) {
		return drawing.getVertices().stream().anyMatch(vertex ->
				vertex.getTurnExemption() == TurnExemption.FOLD_BAND
						&& fold.containsCanonical(vertex.getSemanticParameter(), 2 * Math.PI));
	}

	private static String fingerprint(LocusRenderData2D drawing) {
		return drawing.getVertices().stream().map(vertex ->
				Long.toHexString(Double.doubleToLongBits(vertex.getPoint().getX())) + ","
						+ Long.toHexString(Double.doubleToLongBits(vertex.getPoint().getY()))
						+ "," + vertex.startsSubpath()).collect(Collectors.joining("|"));
	}

	/** @return strokes, the parameters of every stroke end and whether the ends meet */
	private static String topology(LocusRenderData2D drawing) {
		List<String> ends = new ArrayList<>();
		List<Vertex> vertices = drawing.getVertices();
		for (int index = 0; index < vertices.size(); index++) {
			Vertex vertex = vertices.get(index);
			boolean strokeEnd = index + 1 == vertices.size()
					|| vertices.get(index + 1).startsSubpath();
			if (vertex.startsSubpath() || strokeEnd) {
				ends.add(Double.toHexString(vertex.getSemanticParameter()));
			}
		}
		boolean closed = !vertices.isEmpty() && distance(vertices.get(0).getPoint(),
				vertices.get(vertices.size() - 1).getPoint()) == 0;
		return strokes(drawing) + ":" + ends + ":" + closed;
	}

	/**
	 * @return the image of one end of a certified component: its evaluation or, where the
	 *         evaluator does not define the binary64 end, the limit-form enclosure of the
	 *         certified transition that the representation uses (consumption design 5.2).
	 *         Until the corrective mandate (finding H-CU2A-3) the evaluation of an included end
	 *         of an arc across the seam failed whenever the canonicalization moved it off the arc
	 */
	private static LocusPoint2D componentEnd(LocusDefinition2D definition,
			ContinuousComponent component, boolean upper) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
			LocusEvaluation2D evaluation = definition.evaluateComponentEnd(PreG9BCu0Corpus.MAIN,
					upper ? component.getEnd() : component.getStart(), upper, session);
			if (evaluation.isValid()) {
				return evaluation.getPoint();
			}
		}
		LocusBoundaryImage2D image = definition.encloseBoundaryImage(PreG9BCu0Corpus.MAIN,
				upper ? component.getUpperBoundary() : component.getLowerBoundary());
		if (!image.isEnclosed()) {
			throw new IllegalStateException(image.getDiagnostic());
		}
		return image.getPoint();
	}

	private static LocusPoint2D evaluate(LocusDefinition2D definition, double parameter) {
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(4)) {
			LocusEvaluation2D evaluation = definition.evaluate(PreG9BCu0Corpus.MAIN, parameter,
					session);
			if (!evaluation.isValid()) {
				throw new IllegalStateException(evaluation.getStatus() + " "
						+ evaluation.getDiagnostic());
			}
			return evaluation.getPoint();
		}
	}

	private static double distance(LocusPoint2D first, LocusPoint2D second) {
		return Math.hypot(first.getX() - second.getX(), first.getY() - second.getY());
	}

	private static Result certificate(GeoLocusV2 locus) {
		Result result = ((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
				.getHybridExistenceCertificate();
		if (result == null) {
			throw new IllegalStateException("No certificate");
		}
		return result;
	}

	private static ReconstructibleLocusEvaluator2D evaluator(GeoLocusV2 locus)
			throws ReflectiveOperationException {
		Field field = AlgoDependentPointLocusV2.class.getDeclaredField("evaluator");
		field.setAccessible(true);
		return (ReconstructibleLocusEvaluator2D) field.get(locus.getParentAlgorithm());
	}

	/** @return the rich length of the slice copy behind the metric input of the program */
	@SuppressWarnings("unchecked")
	private static AlgoLocusBetweenMetricV2 sliceLength(ReconstructibleLocusEvaluator2D evaluator)
			throws ReflectiveOperationException {
		Field field = ReconstructibleLocusEvaluator2D.class.getDeclaredField(
				"programV3MetricSources");
		field.setAccessible(true);
		Map<Integer, AlgoLocusBetweenMetricV2> sources =
				(Map<Integer, AlgoLocusBetweenMetricV2>) field.get(evaluator);
		return sources.values().iterator().next();
	}
}
