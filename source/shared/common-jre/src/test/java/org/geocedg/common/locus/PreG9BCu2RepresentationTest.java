/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Fidelity;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Mark;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.MarkKind;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.TurnExemption;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.Vertex;
import org.geocedg.common.euclidian.draw.LocusRenderData2D.VertexSource;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D;
import org.geocedg.common.export.ApproximationEvidence;
import org.geocedg.common.export.GeometryExportModel.Point2D;
import org.geocedg.common.export.GeometryExportModel.PolylineGeometry;
import org.geocedg.common.export.GeometryExportModel.SelectionMode;
import org.geocedg.common.export.GeometryExportPreflight;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.GeometryExportService;
import org.geocedg.common.export.SemanticCurveExportAdapter2D;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.Component;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.SourceResult;
import org.geocedg.common.export.SemanticExportClassification;
import org.geocedg.common.export.SemanticExportClassification.SourceClass;
import org.geocedg.common.export.SourceExportOutcome;
import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundaryImage2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.LocusPointFunction2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSingularParameter2D;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T10: representation of certified components (consumption design sections
 * 5 and 6; {@code locus-v2-presentation.md}, "PRE-G9B-CU2 amendment (version 2)"), on the
 * hybrid certificates of the synthetic configurations of the validation matrix (section 1),
 * published in place of the compatibility certificate as step T11 will, and on certified
 * structures injected into analytic fixtures with forced evaluation failures. Matrix rows
 * V-REP-2, V-REP-3, V-B07-2, V-AVAIL-2 (synthetic part), V-AVAIL-4, V-AVAIL-5, V-AVAIL-6,
 * V-POINT-5 (drawing part) and the synthetic parts of V-B06-1 and V-B06-2.
 */
class PreG9BCu2RepresentationTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";
	private static final String FIXTURE = "cu2-representation.main";
	private static final LocusInterval2D CIRCLE = new LocusInterval2D(-Math.PI, Math.PI,
			true, false);
	/** Double root of the exact synthetic critical configuration (matrix section 1). */
	private static final double CRITICAL = Math.atan2(4, 3);
	private static final double TANGENCY = Math.atan2(-4, -3);

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/**
	 * V-REP-3 (limit form): in the exact critical configuration the vertex of the certified
	 * singular parameter is the double root of the tangency, the point (15, 20), enclosed by
	 * the limit form of the certificate for both labelled outputs.
	 */
	@Test
	void theSingularVertexIsTheDoubleRootOfTheTangency() {
		critical("40");
		for (String label : List.of("lq", "lr")) {
			LocusSingularParameter2D singular = certificate(label).getStructure()
					.getSingularParameters().get(0);
			LocusBoundaryImage2D image = definition(label).encloseSingularImage(BRANCH,
					singular);
			assertTrue(image.isEnclosed(), image.toString());
			assertTrue(image.getXLower() <= 15 && 15 <= image.getXUpper(), image.toString());
			assertTrue(image.getYLower() <= 20 && 20 <= image.getYUpper(), image.toString());
			assertTrue(image.getRadius() < 1e-6, image.toString());
			assertEquals(15, image.getPoint().getX(), 1e-6);
			assertEquals(20, image.getPoint().getY(), 1e-6);
		}
	}

	/**
	 * V-AVAIL-4 (limit form): each closed end of the bite arc, a certified discriminant
	 * transition, has a limit-form image that encloses the evaluation of its binary64 end;
	 * the enclosure stays far below a pixel.
	 */
	@Test
	void aClosedTransitionHasALimitFormImage() {
		critical("40+2^(-10)");
		for (String label : List.of("lq", "lr")) {
			LocusDefinition2D definition = definition(label);
			ContinuousComponent arc = certificate(label).getStructure()
					.getContinuousValidComponents().get(0);
			for (boolean upper : new boolean[] {false, true}) {
				LocusBoundary2D end = upper ? arc.getUpperBoundary() : arc.getLowerBoundary();
				assertTrue(end.isSquareRootTransition(), end.toString());
				LocusBoundaryImage2D image = definition.encloseBoundaryImage(BRANCH, end);
				assertTrue(image.isEnclosed(), image.toString());
				assertTrue(image.getRadius() < 1e-3, image.toString());
				LocusPoint2D point = evaluate(definition, upper ? arc.getEnd() : arc.getStart());
				double margin = 1e-9;
				assertTrue(image.getXLower() - margin <= point.getX()
						&& point.getX() <= image.getXUpper() + margin, image + " " + point);
				assertTrue(image.getYLower() - margin <= point.getY()
						&& point.getY() <= image.getYUpper() + margin, image + " " + point);
			}
		}
	}

	/**
	 * Only an included certified transition has a limit form: the edge of the unresolved band
	 * around a vanishing divisor has none, and neither has an excluded end, because its limit
	 * may not exist.
	 */
	@Test
	void onlyAnIncludedCertifiedTransitionHasALimitForm() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("g=Line(N,(5,0))");
		add("S=Intersect(g,Line((0,-10),(1,-10)))");
		add("ls=LocusV2(S,N)");
		LocusExistenceStructure2D structure = certificate("ls").getStructure();
		LocusBoundary2D bandEdge = structure.getContinuousValidComponents().get(0)
				.getUpperBoundary();
		assertEquals(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE, bandEdge.getKind());
		assertNotAvailable(definition("ls").encloseBoundaryImage(BRANCH, bandEdge));
		getKernel().clearConstruction(true);
		critical("40+2^(-10)");
		LocusBoundary2D closed = certificate("lq").getStructure().getContinuousValidComponents()
				.get(0).getLowerBoundary();
		LocusBoundary2D excluded = LocusBoundary2D.enclosed(closed.getKind(),
				closed.getSource(), false, closed.getNature(), closed.getEnclosureLower(),
				closed.getEnclosureUpper());
		assertTrue(definition("lq").encloseBoundaryImage(BRANCH, closed).isEnclosed());
		assertNotAvailable(definition("lq").encloseBoundaryImage(BRANCH, excluded));
	}

	/**
	 * V-REP-3 (drawing) and V-B06-1 and V-B06-2 (synthetic parts): with the certificate of the
	 * exact critical configuration published, the drawing has one exact vertex at the singular
	 * parameter, the double root (15, 20); the spans on both sides reach it unsmoothed, its
	 * drawn turn is the corner of the one-sided tangents, and it is the only exemption. Where
	 * the ray from P is tangent to the circle of the driver, the traced point stops and turns
	 * back: a stationary point that no symmetry certifies (D-CU2-6), so its turn is refined to
	 * the resolution of the parameter and declared, never exempted; every other turn is
	 * within the policy.
	 */
	@Test
	void theSingularParameterIsAnExactUnsmoothedVertex() {
		critical("40");
		for (String label : List.of("lq", "lr")) {
			publishCertificate(label, null);
			LocusRenderData2D drawing = render(locus(label), policy(10));
			List<Vertex> vertices = drawing.getVertices();
			List<Integer> exempt = exemptions(drawing);
			assertEquals(1, exempt.size(), label + " " + exempt);
			int index = exempt.get(0);
			Vertex corner = vertices.get(index);
			assertEquals(TurnExemption.SINGULAR_VERTEX, corner.getTurnExemption());
			assertEquals(VertexSource.LIMIT_FORM, corner.getSource());
			assertEquals(15, corner.getPoint().getX(), 1e-6);
			assertEquals(20, corner.getPoint().getY(), 1e-6);
			double drawn = turnDegrees(vertices.get(index - 1).getPoint(), corner.getPoint(),
					vertices.get(index + 1).getPoint());
			double exact = cornerDegrees(definition(label), corner.getPoint(), CRITICAL);
			assertTrue(exact > LocusRenderPolicy2D.DEFAULT_MAXIMUM_TURN_DEGREES,
					label + " a corner " + exact);
			assertEquals(exact, drawn, LocusRenderPolicy2D.DEFAULT_MAXIMUM_TURN_DEGREES,
					label);
			List<Mark> unresolved = marks(drawing, MarkKind.UNCERTIFIED_TURN);
			assertEquals(1, unresolved.size(), label + " " + drawing.getMarks());
			assertEquals(List.of(MarkKind.UNCERTIFIED_TURN), drawing.getComponentFidelities()
					.get(0).getCauses());
			assertEquals(Fidelity.PARTIAL, drawing.getFidelity());
			// V-B06-1: the closed stroke is measured cyclically at its closing vertex too.
			assertEquals(1, assertTurnsBounded(drawing, 10, label), label);
		}
	}

	/**
	 * V-B07-2 and V-AVAIL-4 (drawing): with the bite certificate published, the arc is one
	 * stroke between its certified transitions; near each, at the start and at the end after
	 * the seam, the spans are taken in {@code t = t* +- s^2}, so that each end span meets the
	 * chord tolerance with the vertices of a regular span, and each end vertex is within 0.75 px
	 * of the image of its certified end; an end that the evaluator does not define is the
	 * limit form of its transition, which adds no defect to the declared fidelity.
	 */
	@Test
	void theTransitionEndsAreReachedInTheSquareRootCoordinate() {
		critical("40+2^(-10)");
		LocusDefinition2D live = definition("lq");
		ContinuousComponent arc = certificate("lq").getStructure()
				.getContinuousValidComponents().get(0);
		publishCertificate("lq", null);
		LocusRenderPolicy2D policy = policy(10);
		LocusRenderData2D drawing = render(locus("lq"), policy);
		assertEquals(1, subpaths(drawing), "one stroke across the seam");
		double lower = arc.getInterval().getLower();
		double span = (arc.getInterval().getUpper() - lower)
				/ (1 << LocusRenderPolicy2D.DEFAULT_MINIMUM_DEPTH);
		int endSpan = verticesIn(drawing, lower, lower + span);
		int regularSpan = verticesIn(drawing, lower + span, lower + 2 * span);
		assertTrue(endSpan <= regularSpan + 2, endSpan + " vs " + regularSpan);
		for (int index = 1; index < drawing.getVertices().size(); index++) {
			Vertex a = drawing.getVertices().get(index - 1);
			Vertex b = drawing.getVertices().get(index);
			if (b.getSemanticParameter() > lower + span) {
				break;
			}
			double sa = Math.sqrt(a.getSemanticParameter() - lower);
			double sb = Math.sqrt(b.getSemanticParameter() - lower);
			double s = sa + (sb - sa) / 2;
			LocusPoint2D middle = evaluate(live, lower + s * s);
			assertTrue(deviationPixels(middle, a.getPoint(), b.getPoint(), 10) <= 0.75,
					"end span chord " + index);
		}
		LocusBoundaryImage2D startImage = live.encloseBoundaryImage(BRANCH,
				arc.getLowerBoundary());
		assertTrue(distancePixels(drawing.getVertices().get(0).getPoint(),
				startImage.getPoint(), 10) <= 0.75);
		// The upper end of the arc, after the seam, in t = t* - s^2 (V-B07-2, both ends).
		double upper = arc.getInterval().getUpper();
		List<Vertex> vertices = drawing.getVertices();
		int upperEndSpan = 0;
		int upperRegularSpan = 0;
		for (Vertex vertex : vertices) {
			double lifted = lifted(vertex.getSemanticParameter(), lower);
			if (upper - span < lifted && lifted <= upper) {
				upperEndSpan++;
			} else if (upper - 2 * span < lifted && lifted <= upper - span) {
				upperRegularSpan++;
			}
		}
		assertTrue(upperEndSpan <= upperRegularSpan + 2, upperEndSpan + " vs " + upperRegularSpan);
		int checked = 0;
		for (int index = vertices.size() - 1; index > 0; index--) {
			double pa = lifted(vertices.get(index - 1).getSemanticParameter(), lower);
			double pb = lifted(vertices.get(index).getSemanticParameter(), lower);
			if (pa < upper - span) {
				break;
			}
			double sa = Math.sqrt(upper - pa);
			double sb = Math.sqrt(upper - pb);
			double s = sa + (sb - sa) / 2;
			LocusPoint2D middle = evaluate(live, upper - s * s);
			assertTrue(deviationPixels(middle, vertices.get(index - 1).getPoint(),
					vertices.get(index).getPoint(), 10) <= 0.75, "upper end span chord " + index);
			checked++;
		}
		assertTrue(checked >= 2, "upper end chords: " + checked);
		LocusBoundaryImage2D endImage = live.encloseBoundaryImage(BRANCH,
				arc.getUpperBoundary());
		assertTrue(distancePixels(last(drawing).getPoint(), endImage.getPoint(), 10) <= 0.75);

		// The evaluator does not define the boundary points: the limit form is the end.
		LocusEvaluator2D failing = failingAt(live.getEvaluatorCapability(), arc.getStart(),
				arc.getEnd());
		publishCertificate("lq", failing);
		LocusRenderData2D limit = render(locus("lq"), policy);
		Vertex first = limit.getVertices().get(0);
		Vertex last = limit.getVertices().get(limit.getVertices().size() - 1);
		assertEquals(VertexSource.LIMIT_FORM, first.getSource());
		assertEquals(VertexSource.LIMIT_FORM, last.getSource());
		assertEquals(0, distancePixels(first.getPoint(), startImage.getPoint(), 10), 1e-9);
		// The limit-form ends are evaluated ends; the only declared defect is the uncertified
		// stationary point of the traced point (see the critical configuration).
		assertEquals(List.of(MarkKind.UNCERTIFIED_TURN), limit.getComponentFidelities().get(0)
				.getCauses(), limit.getMarks().toString());
	}

	/**
	 * V-AVAIL-4 (no limit form): an included end that the evaluator does not define, with no
	 * limit form, ends the stroke at the last evaluated vertex, located by bisection up to the
	 * resolution of the parameter, with a typed mark and a partial fidelity.
	 */
	@Test
	void anUnevaluableEndWithoutLimitFormEndsAtTheLastEvaluatedVertex() {
		double start = 2;
		double end = -2;
		GeoLocusV2 locus = analytic("cu2-end-failure", bite(start, end),
				(source, branch, parameter, session) -> {
					if (parameter == start) {
						throw new IllegalStateException("forced: the end is not evaluated");
					}
					return circle(parameter);
				});
		LocusRenderData2D drawing = render(locus, policy(100));
		Vertex first = drawing.getVertices().get(0);
		assertTrue(first.getSemanticParameter() > start
				&& first.getSemanticParameter() - start < 1e-12, "" + first.getSemanticParameter());
		assertEquals(MarkKind.END_NOT_EVALUATED, drawing.getMarks().get(0).getKind());
		assertEquals(start, drawing.getMarks().get(0).getParameter());
		assertEquals(Fidelity.PARTIAL, drawing.getFidelity());
		assertEquals(List.of(MarkKind.END_NOT_EVALUATED), drawing.getComponentFidelities()
				.get(0).getCauses());
	}

	/**
	 * V-REP-2 and V-AVAIL-2 (synthetic part): a failed evaluation inside a certified
	 * component is computational: the existence does not change, the stroke is cut there with
	 * a typed mark after bisection up to the resolution of the parameter on both sides, the
	 * fidelity is partial, and there is no uniform sampling.
	 */
	@Test
	void aComputationalFailureCutsTheStrokeWithAMark() {
		GeoLocusV2 locus = analytic("cu2-computational", closed(),
				(source, branch, parameter, session) -> {
					if (parameter == 0) {
						throw new IllegalStateException("forced computational failure");
					}
					return circle(parameter);
				});
		LocusRenderData2D drawing = render(locus, policy(100));
		assertEquals(2, subpaths(drawing), "cut at the failure, never interpolated");
		double below = Double.NEGATIVE_INFINITY;
		double above = Double.POSITIVE_INFINITY;
		for (Vertex vertex : drawing.getVertices()) {
			double parameter = vertex.getSemanticParameter();
			assertTrue(parameter != 0);
			if (parameter < 0) {
				below = Math.max(below, parameter);
			} else if (parameter < above) {
				above = parameter;
			}
		}
		assertTrue(-below < 1e-12 && above < 1e-12, below + " " + above);
		List<Mark> gaps = marks(drawing, MarkKind.COMPUTATIONAL_GAP);
		assertEquals(1, gaps.size(), drawing.getMarks().toString());
		assertTrue(gaps.get(0).getDetail().startsWith(
				EvaluationStatus.EVALUATION_FAILED.name()), gaps.get(0).getDetail());
		assertEquals(Fidelity.PARTIAL, drawing.getFidelity());
		assertTrue(drawing.getVertices().size() != 33, "no uniform path of 32 samples");
		assertTrue(locus.getSemanticDefinition().getBranch(FIXTURE)
				.containsValidParameter(0, locus.getSemanticDefinition().getProvider()),
				"the existence does not change");
	}

	/**
	 * V-AVAIL-5: a small vertex budget coarsens the drawing deterministically, without
	 * crossing a boundary: the stroke keeps its ends and its single subpath; below the
	 * coarsest drawing the component fails explicitly with its cause.
	 */
	@Test
	void theVertexBudgetCoarsensWithoutCrossingBoundaries() {
		GeoLocusV2 locus = analytic("cu2-budget", bite(2, -2),
				(source, branch, parameter, session) -> circle(parameter));
		LocusRenderData2D fine = render(locus, policy(1000));
		assertEquals(0, fine.getCoarseningLevel());
		LocusRenderPolicy2D small = policy(1000).withVertexBudget(25);
		LocusRenderData2D coarse = render(locus, small);
		assertTrue(fine.getVertices().size() > 25, "" + fine.getVertices().size());
		assertTrue(coarse.getCoarseningLevel() > 0);
		assertTrue(coarse.getVertices().size() <= 25);
		assertEquals(1, subpaths(coarse));
		assertEquals(fine.getVertices().get(0).getSemanticParameter(),
				coarse.getVertices().get(0).getSemanticParameter());
		assertEquals(last(fine).getSemanticParameter(), last(coarse).getSemanticParameter());
		assertEquals(MarkKind.BUDGET_COARSENED, coarse.getMarks().get(0).getKind());
		assertEquals(fingerprint(coarse), fingerprint(render(locus, small)),
				"deterministic coarsening");
		LocusRenderData2D exhausted = render(locus, policy(1000).withVertexBudget(10));
		assertTrue(exhausted.getVertices().isEmpty());
		assertEquals(Fidelity.FAILED, exhausted.getFidelity());
		assertEquals(List.of(MarkKind.VERTEX_BUDGET_EXHAUSTED), exhausted
				.getComponentFidelities().get(0).getCauses());
	}

	/**
	 * V-AVAIL-6: partial results: a component that can be drawn is drawn with its own
	 * evaluated coordinates, and a component that cannot is listed with its cause.
	 */
	@Test
	void aComponentThatCannotBeDrawnIsListedWithItsCause() {
		GeoLocusV2 locus = analytic("cu2-partial", twoIntervals(),
				(source, branch, parameter, session) -> {
					if (parameter > 0) {
						throw new IllegalStateException("forced: the right interval fails");
					}
					return new LocusPoint2D(parameter, parameter * parameter);
				});
		LocusRenderData2D drawing = render(locus, policy(50));
		assertEquals(2, drawing.getComponentFidelities().size());
		assertEquals(Fidelity.COMPLETE, drawing.getComponentFidelities().get(0).getFidelity());
		assertEquals(Fidelity.FAILED, drawing.getComponentFidelities().get(1).getFidelity());
		assertTrue(drawing.getComponentFidelities().get(1).getCauses()
				.contains(MarkKind.END_NOT_EVALUATED));
		assertTrue(drawing.getVertices().stream().allMatch(vertex ->
				vertex.getSemanticParameter() <= -1
						&& vertex.getSource() == VertexSource.EVALUATED));
		assertEquals(Fidelity.FAILED, drawing.getFidelity());
	}

	/**
	 * V-POINT-5 (drawing part): a point component is never drawn: nothing is drawn, and the
	 * drawing declares it with its enclosure and the status of its position, with a partial
	 * fidelity.
	 */
	@Test
	void aPointComponentIsDeclaredAndNeverDrawn() {
		GeoLocusV2 locus = analytic("cu2-point", pointOnly(),
				(source, branch, parameter, session) -> circle(parameter));
		LocusRenderData2D drawing = render(locus, policy(100));
		assertTrue(drawing.getVertices().isEmpty());
		List<Mark> declared = marks(drawing, MarkKind.POINT_COMPONENT_NOT_DRAWN);
		assertEquals(1, declared.size());
		assertTrue(declared.get(0).getDetail().contains("enclosure"), declared.toString());
		assertTrue(declared.get(0).getDetail().contains("position"), declared.toString());
		assertEquals(Fidelity.PARTIAL, drawing.getFidelity());
	}

	// ---------------------------------------------------------------- exports (A2.6)

	/**
	 * V-DXF-3: the closure residual of a certified closed periodic component is measured
	 * against its closure certificate: between the start and the curve just before the
	 * closing point, never the trivial identity of the start with the canonicalized end.
	 */
	@Test
	void theClosureResidualIsMeasuredAgainstTheClosureCertificate() {
		GeoLocusV2 locus = analytic("cu2-closure", closed(),
				(source, branch, parameter, session) -> circle(parameter));
		Component component = onlyComponent(adapt(locus));
		assertTrue(polyline(component).isClosed());
		ApproximationEvidence evidence = component.getOutcome().getApproximationEvidence();
		LocusDefinition2D definition = locus.getSemanticDefinition();
		// Before the closing point by four ulps and by twice the domain tolerance (1e-14).
		double probe = Math.PI - 2e-14;
		double expected = distance(evaluate(definition, FIXTURE, -Math.PI),
				evaluate(definition, FIXTURE, probe));
		assertEquals(0, distance(evaluate(definition, FIXTURE, -Math.PI),
				evaluate(definition, FIXTURE, Math.PI)), "the trivial identity");
		assertEquals(expected, evidence.getClosureResidual());
		assertTrue(expected > 0 && expected < 1e-12, "measured: " + expected);
	}

	/**
	 * A2.6: a component with an excluded end without a limit is exported, not dropped: the
	 * end is approached from inside and declared, and the output is incomplete.
	 */
	@Test
	void anOpenEndIsExportedAndDeclared() {
		GeoLocusV2 locus = analytic("cu2-open-end", openInterval(),
				(source, branch, parameter, session) -> new LocusPoint2D(parameter, 1));
		Component component = onlyComponent(adapt(locus));
		SourceExportOutcome outcome = component.getOutcome();
		assertEquals(1, outcome.getDeclarations().size(), outcome.getDeclarations().toString());
		assertTrue(outcome.getDeclarations().get(0).startsWith("OPEN_END_APPROACHED"));
		assertTrue(outcome.isIncomplete());
		List<Point2D> vertices = polyline(component).getVertices();
		double last = vertices.get(vertices.size() - 1).getX();
		assertTrue(last < 2 && 2 - last < 1e-7, "approached: " + last);
		assertEquals(SourceClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
				SemanticExportClassification.classifySource(List.of(outcome)));
	}

	/**
	 * V-AVAIL-3 (synthetic part): an excluded structural-event end whose one-sided limit is
	 * not evaluated is exported up to the last evaluated vertex, located by bisection up to
	 * the resolution of the parameter, and declared; the output is incomplete.
	 */
	@Test
	void anUnevaluatedOneSidedLimitIsDeclared() {
		GeoLocusV2 locus = analytic("cu2-limit-failure", openFullLength(),
				(source, branch, parameter, session) -> circle(parameter));
		Component component = onlyComponent(adapt(locus));
		SourceExportOutcome outcome = component.getOutcome();
		assertEquals(1, outcome.getDeclarations().size(), outcome.getDeclarations().toString());
		assertTrue(outcome.getDeclarations().get(0).startsWith("END_NOT_EVALUATED"),
				outcome.getDeclarations().get(0));
		assertTrue(outcome.getDeclarations().get(0).contains("last evaluated vertex"));
		assertTrue(outcome.isIncomplete());
		assertFalse(polyline(component).isClosed(), "an open arc never closes");
		List<Point2D> vertices = polyline(component).getVertices();
		Point2D last = vertices.get(vertices.size() - 1);
		assertTrue(Math.hypot(last.getX() + 1, last.getY()) < 1e-9,
				"exported up to the end: " + last);
	}

	/**
	 * A2.6: the certified singular parameter of the exact critical configuration is an exact
	 * vertex of the export, the double root (15, 20).
	 */
	@Test
	void theSingularParameterIsAnExactExportVertex() {
		critical("40");
		publishCertificate("lq", null);
		Component component = onlyComponent(adapt(locus("lq")));
		assertTrue(component.getOutcome().getDeclarations().isEmpty());
		assertTrue(polyline(component).getVertices().stream().anyMatch(vertex ->
				Math.abs(vertex.getX() - 15) < 1e-6 && Math.abs(vertex.getY() - 20) < 1e-6),
				"the double root is a vertex");
	}

	/**
	 * V-POINT-5 (export part): a point component is declared with its enclosure and the
	 * status of its position, never exported as a path and never a failure: the strict DXF
	 * request stays writable with the arc beside it, the sidecar is required and the
	 * classification is incomplete.
	 */
	@Test
	void aPointComponentIsDeclaredInTheExport() {
		GeoLocusV2 locus = analytic("cu2-arc-and-point", arcAndPoint(),
				(source, branch, parameter, session) -> circle(parameter));
		SourceResult result = adapt(locus);
		assertEquals(2, result.getComponents().size());
		SourceExportOutcome declared = result.getComponents().get(1).getOutcome();
		assertEquals(SourceExportOutcome.Fidelity.DECLARED, declared.getFidelity());
		assertFalse(declared.isEmitted());
		assertTrue(declared.getMessage().contains("enclosure"), declared.getMessage());
		assertTrue(declared.getMessage().contains("position"), declared.getMessage());
		GeometryExportPreflight preflight = new GeometryExportService().preflight(
				List.of(locus), SelectionMode.CURRENT_SELECTION,
				GeometryExportRequest.builder(0.01).build());
		assertTrue(preflight.isWritable());
		assertEquals(1, preflight.getDeclaredCount());
		assertEquals(0, preflight.getOmittedCount());
		assertTrue(preflight.isSidecarRequired());
		assertEquals(SourceClass.INCOMPLETE_WITH_CERTIFIED_COMPONENTS,
				SemanticExportClassification.classifySource(result.getComponents().stream()
						.map(Component::getOutcome).collect(Collectors.toList())));
	}

	/**
	 * V-B07-4 (decision E-1, approved): exploratory evidence is drawn in its own vertex list,
	 * with the mark {@code EXPLORATORY_EVIDENCE} and a partial drawing fidelity, only inside
	 * the unresolved bands of the published certificate, never in a subpath of a certified
	 * component, and it is never exported. A locus outside class v3 (V-LVL-1) draws nothing
	 * else; the unlabelled second output of the critical configuration draws its local
	 * component and, apart, the evidence of the band around the coincident root.
	 */
	@Test
	void exploratoryEvidenceIsDrawnApartAndOnlyInsideItsBands() {
		add("s=0");
		add("X=(s,s^2)");
		add("D={false,{-2,2,true,true}}");
		add("lx=LocusV2(X,s,D)");
		LocusRenderData2D outside = render(locus("lx"), policy(40));
		assertTrue(outside.getVertices().isEmpty());
		assertFalse(outside.getExploratoryVertices().isEmpty());
		assertEquals(Fidelity.PARTIAL, outside.getFidelity());
		assertFalse(marks(outside, MarkKind.EXPLORATORY_EVIDENCE).isEmpty());
		assertExploratoryInsideBands("lx", outside);
		List<Component> declaredComponents = adapt(locus("lx")).getComponents();
		assertEquals(1, declaredComponents.size());
		Component declared = declaredComponents.get(0);
		assertFalse(declared.isEmitted());
		assertEquals(SourceExportOutcome.Reason.EXISTENCE_NOT_ESTABLISHED,
				declared.getOutcome().getReason());

		critical("40");
		add("t2=Ray(P,N)");
		add("R2=Intersect(p,t2,2)");
		add("lr2=LocusV2(R2,N)");
		LocusRenderData2D band = render(locus("lr2"), policy(40));
		assertFalse(band.getVertices().isEmpty());
		assertFalse(band.getExploratoryVertices().isEmpty());
		assertExploratoryInsideBands("lr2", band);
		LocusExistenceStructure2D structure = locus("lr2").getSemanticDefinition()
				.getBranch(BRANCH).getExistenceStructure();
		java.util.Set<String> certified = new java.util.HashSet<>();
		for (Vertex vertex : band.getVertices()) {
			certified.add(vertex.getPoint().getX() + "," + vertex.getPoint().getY());
		}
		for (Vertex vertex : band.getExploratoryVertices()) {
			assertFalse(certified.contains(vertex.getPoint().getX() + ","
					+ vertex.getPoint().getY()), "never joined to a component");
		}
		long emitted = adapt(locus("lr2")).getComponents().stream()
				.filter(Component::isEmitted).count();
		assertEquals(structure.getContinuousValidComponents().size(), emitted,
				"the evidence is never exported");
	}

	/**
	 * V-B07-4 (decision E-1, the style): drawn on a recording graphics, the exploratory evidence
	 * of a locus outside class v3 and of the unlabelled second output of the critical
	 * configuration is its own path, drawn after the certified one with the object colour at
	 * alpha 128 and the dotted stroke of the object's width, never with the opaque solid stroke
	 * of a certified component, which the unlabelled output draws first.
	 */
	@Test
	void exploratoryEvidenceIsDrawnDottedAndTranslucent() {
		add("s=0");
		add("X=(s,s^2)");
		add("D={false,{-2,2,true,true}}");
		add("lx=LocusV2(X,s,D)");
		critical("40");
		add("t2=Ray(P,N)");
		add("R2=Intersect(p,t2,2)");
		add("lr2=LocusV2(R2,N)");
		for (String label : List.of("lx", "lr2")) {
			GeoLocusV2 locus = locus(label);
			org.geogebra.common.euclidian.EuclidianView view = getApp().getEuclidianView1();
			view.setCoordSystem(400, 300, 10, 10);
			StyleRecorder graphics = new StyleRecorder();
			new org.geocedg.common.euclidian.draw.DrawLocusV2(view, locus).draw(graphics);
			int draws = graphics.colors.size();
			assertTrue(draws >= 2, label + " draws " + draws);
			int rgb = locus.getObjectColor().getARGB() | 0xFF000000;
			// The last draw is the exploratory path, with its own paint and stroke.
			org.geogebra.common.awt.GColor color = graphics.colors.get(draws - 1);
			org.geogebra.common.awt.GBasicStroke stroke = graphics.strokes.get(draws - 1);
			assertEquals(128, color.getAlpha(), label);
			assertEquals(rgb, color.getARGB() | 0xFF000000, label);
			assertTrue(stroke.getDashArray() != null, label + " a dotted stroke");
			assertEquals(java.util.Arrays.toString(org.geogebra.common.euclidian.EuclidianStatic
					.getStroke(stroke.getLineWidth(), org.geogebra.common.plugin
							.EuclidianStyleConstants.LINE_TYPE_DOTTED).getDashArray()),
					java.util.Arrays.toString(stroke.getDashArray()), label);
			assertTrue(graphics.segments.get(draws - 1) > 0, label + " evidence drawn");
			for (int index = 0; index < draws - 1; index++) {
				assertEquals(rgb, graphics.colors.get(index).getARGB() | 0xFF000000, label);
				assertTrue(graphics.colors.get(index).getAlpha() != 128, label);
				assertEquals(stroke.getLineWidth(), graphics.strokes.get(index).getLineWidth(),
						label + " the width of the object");
			}
			if ("lr2".equals(label)) {
				assertTrue(graphics.segments.get(0) > 0, "the certified stroke of lr2 is drawn");
				assertTrue(graphics.strokes.get(0).getDashArray() == null
						|| graphics.strokes.get(0).getDashArray().length == 0,
						"the certified stroke is solid");
			}
		}
	}

	/** Records the paint and stroke of each shape drawn, and its number of path segments. */
	private static final class StyleRecorder extends org.geogebra.common.awt.GGraphicsCommon {
		private final List<org.geogebra.common.awt.GColor> colors =
				new java.util.ArrayList<>();
		private final List<org.geogebra.common.awt.GBasicStroke> strokes =
				new java.util.ArrayList<>();
		private final List<Integer> segments = new java.util.ArrayList<>();
		private org.geogebra.common.awt.GPaint paint;
		private org.geogebra.common.awt.GBasicStroke stroke;

		@Override
		public void setPaint(org.geogebra.common.awt.GPaint value) {
			paint = value;
		}

		@Override
		public void setStroke(org.geogebra.common.awt.GBasicStroke value) {
			stroke = value;
		}

		@Override
		public void draw(org.geogebra.common.awt.GShape shape) {
			colors.add((org.geogebra.common.awt.GColor) paint);
			strokes.add(stroke);
			int count = 0;
			org.geogebra.common.awt.GPathIterator iterator = shape.getPathIterator(null);
			double[] coordinates = new double[6];
			while (!iterator.isDone()) {
				iterator.currentSegment(coordinates);
				count++;
				iterator.next();
			}
			segments.add(count);
		}
	}

	/**
	 * V-DXF-2: a locus outside class v3 has no certified component; its strict DXF output is a
	 * declaration ({@code EXISTENCE_NOT_ESTABLISHED}), never a path and never a failure, so
	 * the output of another source stays writable with the mandatory sidecar. A locus whose
	 * existence is not established but which has locally certified components exports each of
	 * them within the strict tolerance, incomplete, with the mandatory sidecar.
	 */
	@Test
	void aLocusWithoutEstablishedExistenceIsDeclaredWithTheMandatorySidecar() {
		add("s=0");
		add("X=(s,s^2)");
		add("D={false,{-2,2,true,true}}");
		add("lx=LocusV2(X,s,D)");
		add("u=0");
		add("Y=(u,u)");
		add("E={false,{-2,2,true,true}}");
		add("ly=LocusV2(Y,u,E)");
		GeometryExportPreflight mixed = new GeometryExportService().preflight(
				List.of(locus("lx"), locus("ly")), SelectionMode.CURRENT_SELECTION,
				GeometryExportRequest.builder(0.01).build());
		assertTrue(mixed.isWritable());
		assertTrue(mixed.isSidecarRequired());
		assertEquals(1, mixed.getDeclaredCount());
		assertEquals(1, mixed.getModel().getEntities().size());
		SourceExportOutcome declared = mixed.getModel().getOutcomes().stream()
				.filter(outcome -> "lx".equals(outcome.getLabel())).findFirst().orElseThrow();
		assertEquals(SourceExportOutcome.Fidelity.DECLARED, declared.getFidelity());
		assertEquals(SourceExportOutcome.Reason.EXISTENCE_NOT_ESTABLISHED, declared.getReason());
		assertFalse(declared.isEmitted());
		assertTrue(declared.isIncomplete());

		critical("40");
		add("t2=Ray(P,N)");
		add("R2=Intersect(p,t2,2)");
		add("lr2=LocusV2(R2,N)");
		assertEquals(Completeness.NOT_ESTABLISHED, locus("lr2").getSemanticDefinition()
				.getBranch(BRANCH).getExistenceStructure().getCompleteness());
		GeometryExportPreflight local = new GeometryExportService().preflight(
				List.of(locus("lr2")), SelectionMode.CURRENT_SELECTION,
				GeometryExportRequest.builder(0.01).build());
		assertTrue(local.isWritable());
		assertTrue(local.isSidecarRequired());
		assertFalse(local.getModel().getEntities().isEmpty());
		for (SourceExportOutcome outcome : local.getModel().getOutcomes()) {
			assertTrue(outcome.isEmitted(), outcome.getMessage());
			assertEquals(SourceExportOutcome.SemanticCoverage
					.LOCALLY_CERTIFIED_GLOBAL_NOT_ESTABLISHED, outcome.getSemanticCoverage());
			assertTrue(outcome.isIncomplete());
			assertEquals(0.01, outcome.getApproximationEvidence().getRequestedTolerance());
		}
	}

	/** Every exploratory vertex lies inside an unresolved band of the published certificate. */
	private void assertExploratoryInsideBands(String label, LocusRenderData2D drawing) {
		LocusDefinition2D definition = locus(label).getSemanticDefinition();
		LocusExistenceStructure2D structure = definition.getBranch(BRANCH)
				.getExistenceStructure();
		for (Vertex vertex : drawing.getExploratoryVertices()) {
			double parameter = definition.getProvider().canonicalize(
					vertex.getSemanticParameter());
			Coverage coverage = structure.getCoverageAt(parameter);
			assertNotNull(coverage, label + " t=" + parameter);
			assertEquals(CoverageState.UNRESOLVED, coverage.getState(), label + " t="
					+ parameter);
		}
	}

	// ---------------------------------------------------------------- fixtures

	private static LocusPoint2D evaluate(LocusDefinition2D definition, String branchKey,
			double parameter) {
		try (LocusEvaluationSession2D session = new LocusEvaluationSession2D(false, 1)) {
			LocusEvaluation2D evaluation = definition.evaluate(branchKey, parameter, session);
			assertTrue(evaluation.isValid(), evaluation.getStatus() + " "
					+ evaluation.getDiagnostic());
			return evaluation.getPoint();
		}
	}

	private static double distance(LocusPoint2D first, LocusPoint2D second) {
		return Math.hypot(first.getX() - second.getX(), first.getY() - second.getY());
	}

	private static SourceResult adapt(GeoLocusV2 locus) {
		GeometryExportRequest request = GeometryExportRequest.builder(0.01)
				.allowedGuarantees(EnumSet.of(ApproximationEvidence.Guarantee
						.ESTIMATED_ERROR)).build();
		return new SemanticCurveExportAdapter2D().adaptLocus(locus, 0, request,
				new AdaptiveCurveApproximationBuilder2D.WorkLedger(request), "cu2:",
				SemanticCurveExportAdapter2D.ALL_COMPONENTS);
	}

	private static Component onlyComponent(SourceResult result) {
		assertEquals(1, result.getComponents().size());
		assertTrue(result.getComponents().get(0).isEmitted(),
				String.valueOf(result.getComponents().get(0).getOutcome().getMessage()));
		return result.getComponents().get(0);
	}

	private static PolylineGeometry polyline(Component component) {
		return (PolylineGeometry) component.getEntity().getGeometry();
	}

	private static LocusPoint2D evaluate(LocusDefinition2D definition, double parameter) {
		try (LocusEvaluationSession2D session = new LocusEvaluationSession2D(false, 1)) {
			LocusEvaluation2D evaluation = definition.evaluate(BRANCH, parameter, session);
			assertTrue(evaluation.isValid(), evaluation.getStatus() + " "
					+ evaluation.getDiagnostic());
			return evaluation.getPoint();
		}
	}

	/** Exact synthetic critical configuration with labelled outputs (matrix section 1). */
	private void critical(String radius) {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45)," + radius + ")");
		add("N=Point(g1)");
		add("t=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		add("lr=LocusV2(R,N)");
		// Created by the command, the intersections settle their selection on recompute.
		getKernel().updateConstruction(false);
	}

	/**
	 * Publishes the certificate of a dependent locus again, keeping the live evaluator unless
	 * another is given (a test policy). Step T11 publishes the same certificate; the
	 * republication only installs the test policy.
	 */
	private void publishCertificate(String label, LocusEvaluator2D evaluator) {
		GeoLocusV2 locus = locus(label);
		LocusExistenceStructure2D structure = certificate(label).getStructure();
		LocusDefinition2D published = locus.getSemanticDefinition();
		LocusBranch2D branch = published.getBranch(BRANCH);
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
	}

	/**
	 * The live evaluator, except that it does not define the given canonical parameters and
	 * their binary64 neighbours: canonicalization may move an in-domain parameter by an ulp.
	 */
	private static LocusEvaluator2D failingAt(LocusEvaluator2D live, double... failing) {
		return new LocusEvaluator2D() {
			@Override
			public LocusEvaluation2D evaluate(LocusDefinition2D definition,
					LocusBranch2D branch, double parameter, LocusEvaluationSession2D session) {
				for (double value : failing) {
					if (Math.abs(parameter - value) <= 4 * Math.ulp(value)) {
						return LocusEvaluation2D.invalid(EvaluationStatus.DEPENDENCY_UNDEFINED,
								branch.getQuality(), "forced: the boundary point is undefined");
					}
				}
				return live.evaluate(definition, branch, parameter, session);
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

	private GeoLocusV2 analytic(String identity, LocusExistenceStructure2D structure,
			LocusPointFunction2D function) {
		ExplicitNumericDomainProvider2D provider = new ExplicitNumericDomainProvider2D(
				"cu2-representation/parameter/v1", structure.getCanonicalDomain(),
				Orientation.INCREASING, structure.getTopology() == DomainTopology.PERIODIC,
				1e-14);
		LocusBranch2D branch = new LocusBranch2D(FIXTURE, structure.getCanonicalDomain(),
				structure, Orientation.INCREASING, "cu2-representation/v1",
				LocusLineage2D.unchanged(), structure.getTopology() == DomainTopology.PERIODIC
						? EnumSet.of(BranchProperty.FINITE, BranchProperty.PERIODIC)
						: EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
		return LocusV2Factory.createAnalytic(LocusV2Mode.V2, getConstruction(), identity,
				new GeoNumeric(getConstruction(), 0), provider, List.of(branch), function,
				identity + "/v1");
	}

	private static LocusPoint2D circle(double parameter) {
		return new LocusPoint2D(Math.cos(parameter), Math.sin(parameter));
	}

	private static LocusExistenceStructure2D closed() {
		LocusBoundary2D closure = new LocusBoundary2D(LocusBoundary2D.Kind.PERIODIC_CLOSURE,
				new LocusBoundarySource2D("hybrid-certifier", "periodic-closure", "seam"),
				true, LocusEvidenceNature2D.EXACT);
		return periodic(List.of(exists(CIRCLE)), List.of(ContinuousComponent.arc(CIRCLE,
				-Math.PI, -Math.PI, true, closure, closure, Maximality.ESTABLISHED,
				LocusEvidenceNature2D.EXACT, "closed/0")), Collections.emptyList());
	}

	/** One arc across the seam between two certified transitions. */
	private static LocusExistenceStructure2D bite(double start, double end) {
		ContinuousComponent arc = ContinuousComponent.arc(CIRCLE, start, end, false,
				transition("start"), transition("end"), Maximality.ESTABLISHED,
				LocusEvidenceNature2D.EXACT, "bite/0");
		return periodic(List.of(exists(new LocusInterval2D(-Math.PI, end, true, true)),
				absent(new LocusInterval2D(end, start, false, false)),
				exists(new LocusInterval2D(start, Math.PI, true, false))), List.of(arc),
				Collections.emptyList());
	}

	/** Existence reduced to the exterior tangency {@code t* = atan2(-4, -3)} (V-POINT-1). */
	private static LocusExistenceStructure2D pointOnly() {
		LocusPointComponent2D point = new LocusPointComponent2D(Math.nextDown(TANGENCY),
				Math.nextUp(TANGENCY), new LocusBoundarySource2D("line-circle",
						"discriminant", "even-root-1"), Maximality.ESTABLISHED,
				LocusEvidenceNature2D.EXACT, "tangency/0", null);
		return periodic(List.of(
				absent(new LocusInterval2D(-Math.PI, Math.nextDown(TANGENCY), true, false)),
				new Coverage(new LocusInterval2D(Math.nextDown(TANGENCY), Math.nextUp(TANGENCY),
						true, true), CoverageState.CERTIFIED_ISOLATED_POINTS,
						LocusCoverageCause2D.ISOLATED_EVEN_ROOT, LocusEvidenceNature2D.EXACT,
						"fixture-isolated"),
				absent(new LocusInterval2D(Math.nextUp(TANGENCY), Math.PI, false, false))),
				Collections.emptyList(), List.of(point));
	}

	/** One certified interval of a bounded half-open domain, open at its excluded end. */
	private static LocusExistenceStructure2D openInterval() {
		LocusInterval2D domain = new LocusInterval2D(-2, 2, true, false);
		LocusBoundary2D open = new LocusBoundary2D(LocusBoundary2D.Kind.DOMAIN_END,
				LocusBoundarySource2D.declaredUpper(), false, LocusEvidenceNature2D.EXACT);
		return new LocusExistenceStructure2D(FIXTURE, domain, DomainTopology.BOUNDED, 0,
				List.of(exists(domain)), List.of(ContinuousComponent.interval(domain,
						domainEnd(true), open, Maximality.ESTABLISHED,
						LocusEvidenceNature2D.EXACT, "open/0")),
				Collections.emptyList(), Completeness.COMPLETE, "fixture-hybrid/v1");
	}

	/** An open arc of full length at the seam, cut by a structural event. */
	private static LocusExistenceStructure2D openFullLength() {
		LocusBoundary2D start = new LocusBoundary2D(LocusBoundary2D.Kind.STRUCTURAL_EVENT,
				new LocusBoundarySource2D("w", "ORIGIN_WRAP", "start"), true,
				LocusEvidenceNature2D.EXACT);
		LocusBoundary2D end = new LocusBoundary2D(LocusBoundary2D.Kind.STRUCTURAL_EVENT,
				new LocusBoundarySource2D("w", "ORIGIN_WRAP", "end"), false,
				LocusEvidenceNature2D.EXACT);
		return periodic(List.of(exists(CIRCLE)), List.of(ContinuousComponent.arc(CIRCLE,
				-Math.PI, -Math.PI, true, start, end, Maximality.ESTABLISHED,
				LocusEvidenceNature2D.EXACT, "open/0")), Collections.emptyList());
	}

	/** The bite arc and a point component inside its gap, at {@code t = 0}. */
	private static LocusExistenceStructure2D arcAndPoint() {
		double delta = 1e-9;
		ContinuousComponent arc = ContinuousComponent.arc(CIRCLE, 2, -2, false,
				transition("start"), transition("end"), Maximality.ESTABLISHED,
				LocusEvidenceNature2D.EXACT, "bite/0");
		LocusPointComponent2D point = new LocusPointComponent2D(-delta, delta,
				new LocusBoundarySource2D("line-circle", "discriminant", "even-root-1"),
				Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, "point/0", null);
		return periodic(List.of(exists(new LocusInterval2D(-Math.PI, -2, true, true)),
				absent(new LocusInterval2D(-2, -delta, false, false)),
				new Coverage(new LocusInterval2D(-delta, delta, true, true),
						CoverageState.CERTIFIED_ISOLATED_POINTS,
						LocusCoverageCause2D.ISOLATED_EVEN_ROOT, LocusEvidenceNature2D.EXACT,
						"fixture-isolated"),
				absent(new LocusInterval2D(delta, 2, false, false)),
				exists(new LocusInterval2D(2, Math.PI, true, false))), List.of(arc),
				List.of(point));
	}

	/** Two certified intervals of a bounded domain with certified nonexistence between. */
	private static LocusExistenceStructure2D twoIntervals() {
		LocusInterval2D domain = new LocusInterval2D(-3, 3, true, true);
		LocusInterval2D left = new LocusInterval2D(-3, -1, true, true);
		LocusInterval2D right = new LocusInterval2D(1, 3, true, true);
		return new LocusExistenceStructure2D(FIXTURE, domain, DomainTopology.BOUNDED, 0,
				List.of(exists(left), absent(new LocusInterval2D(-1, 1, false, false)),
						exists(right)),
				List.of(ContinuousComponent.interval(left, domainEnd(true), transition("left"),
						Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, "left/0"),
						ContinuousComponent.interval(right, transition("right"), domainEnd(false),
								Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, "right/0")),
				Collections.emptyList(), Completeness.COMPLETE, "fixture-hybrid/v1");
	}

	private static LocusExistenceStructure2D periodic(List<Coverage> coverage,
			List<ContinuousComponent> components, List<LocusPointComponent2D> points) {
		return new LocusExistenceStructure2D(FIXTURE, CIRCLE, DomainTopology.PERIODIC, 0,
				coverage, components, points, Completeness.COMPLETE, "fixture-hybrid/v1");
	}

	private static LocusBoundary2D transition(String side) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.CERTIFIED_TRANSITION,
				new LocusBoundarySource2D("fixture-J", "predicate", side), true,
				LocusEvidenceNature2D.EXACT);
	}

	private static LocusBoundary2D domainEnd(boolean lower) {
		return new LocusBoundary2D(LocusBoundary2D.Kind.DOMAIN_END, lower
				? LocusBoundarySource2D.declaredLower() : LocusBoundarySource2D.declaredUpper(),
				true, LocusEvidenceNature2D.EXACT);
	}

	private static Coverage exists(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.CERTIFIED_EXISTS,
				LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE, LocusEvidenceNature2D.EXACT,
				"fixture-exists");
	}

	private static Coverage absent(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.CERTIFIED_DOES_NOT_EXIST,
				LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE, LocusEvidenceNature2D.EXACT,
				"fixture-absent");
	}

	// ---------------------------------------------------------------- drawing

	private static LocusRenderPolicy2D policy(double scale) {
		return LocusRenderPolicy2D.adaptive(1, 800, 600, scale, scale, 32, 0.75, 12);
	}

	private static LocusRenderData2D render(GeoLocusV2 locus, LocusRenderPolicy2D policy) {
		return new LocusRenderCache2D().getOrBuild(locus, policy);
	}

	private static long subpaths(LocusRenderData2D drawing) {
		return drawing.getVertices().stream().filter(Vertex::startsSubpath).count();
	}

	private static Vertex last(LocusRenderData2D drawing) {
		return drawing.getVertices().get(drawing.getVertices().size() - 1);
	}

	private static List<Mark> marks(LocusRenderData2D drawing, MarkKind kind) {
		return drawing.getMarks().stream().filter(mark -> mark.getKind() == kind)
				.collect(Collectors.toList());
	}

	private static List<Integer> exemptions(LocusRenderData2D drawing) {
		List<Integer> exempt = new java.util.ArrayList<>();
		for (int index = 0; index < drawing.getVertices().size(); index++) {
			if (drawing.getVertices().get(index).getTurnExemption() != TurnExemption.NONE) {
				exempt.add(index);
			}
		}
		return exempt;
	}

	/** @return the parameter lifted by a period when it lies before the start of an arc */
	private static double lifted(double parameter, double start) {
		return parameter < start ? parameter + 2 * Math.PI : parameter;
	}

	private static int verticesIn(LocusRenderData2D drawing, double lower, double upper) {
		int count = 0;
		for (Vertex vertex : drawing.getVertices()) {
			if (lower < vertex.getSemanticParameter() && vertex.getSemanticParameter() <= upper) {
				count++;
			}
		}
		return count;
	}

	/**
	 * V-B06-1: every turn between visible chords of one subpath outside the exemptions is
	 * within the policy, except at a declared turn at the resolution of the parameter; on a
	 * closed subpath, whose last vertex is its first, the turn at the closing vertex is
	 * measured cyclically, from the last chord to the first.
	 *
	 * @return the number of closed subpaths measured cyclically
	 */
	private static int assertTurnsBounded(LocusRenderData2D drawing, double scale,
			String label) {
		List<Vertex> vertices = drawing.getVertices();
		List<Mark> unresolved = marks(drawing, MarkKind.UNCERTIFIED_TURN);
		for (int index = 1; index + 1 < vertices.size(); index++) {
			int previous = visibleNeighbour(vertices, index, -1, scale);
			int next = visibleNeighbour(vertices, index, 1, scale);
			if (previous < 0 || next < 0) {
				continue;
			}
			boolean exempt = false;
			for (int inside = previous + 1; inside < next; inside++) {
				exempt |= vertices.get(inside).getTurnExemption() != TurnExemption.NONE;
			}
			double from = vertices.get(previous).getSemanticParameter();
			double to = vertices.get(next).getSemanticParameter();
			if (exempt || unresolved.stream().anyMatch(mark -> from <= mark.getParameter()
					&& mark.getParameter() <= to)) {
				continue;
			}
			double turn = turnDegrees(vertices.get(previous).getPoint(),
					vertices.get(index).getPoint(), vertices.get(next).getPoint());
			assertTrue(!(turn > LocusRenderPolicy2D.DEFAULT_MAXIMUM_TURN_DEGREES + 1e-9),
					label + " turn " + turn + " at " + vertices.get(index).getSemanticParameter());
		}
		int closed = 0;
		int start = 0;
		while (start < vertices.size()) {
			int end = start + 1;
			while (end < vertices.size() && !vertices.get(end).startsSubpath()) {
				end++;
			}
			int last = end - 1;
			if (last > start + 1 && distancePixels(vertices.get(start).getPoint(),
					vertices.get(last).getPoint(), scale) <= 1e-6) {
				closed++;
				int previous = visibleNeighbour(vertices, last, -1, scale);
				int next = visibleNeighbour(vertices, start, 1, scale);
				boolean exempt = previous < 0 || next < 0;
				for (int inside = Math.max(previous, start) + 1; !exempt && inside <= last;
						inside++) {
					exempt = vertices.get(inside).getTurnExemption() != TurnExemption.NONE;
				}
				for (int inside = start; !exempt && inside < next; inside++) {
					exempt = vertices.get(inside).getTurnExemption() != TurnExemption.NONE;
				}
				if (!exempt) {
					double from = vertices.get(previous).getSemanticParameter();
					double to = vertices.get(next).getSemanticParameter();
					boolean declared = unresolved.stream().anyMatch(mark ->
							from <= mark.getParameter() || mark.getParameter() <= to);
					double turn = turnDegrees(vertices.get(previous).getPoint(),
							vertices.get(start).getPoint(), vertices.get(next).getPoint());
					assertTrue(declared
							|| !(turn > LocusRenderPolicy2D.DEFAULT_MAXIMUM_TURN_DEGREES + 1e-9),
							label + " closing turn " + turn);
				}
			}
			start = end;
		}
		return closed;
	}

	/**
	 * @return the nearest vertex of the same subpath whose chord from the vertex is visible
	 *         (above a millionth of a pixel), or {@code -1}
	 */
	private static int visibleNeighbour(List<Vertex> vertices, int index, int step,
			double scale) {
		LocusPoint2D point = vertices.get(index).getPoint();
		for (int current = index + step; current >= 0 && current < vertices.size();
				current += step) {
			if (step > 0 ? vertices.get(current).startsSubpath()
					: vertices.get(current + 1).startsSubpath()) {
				return -1;
			}
			if (distancePixels(vertices.get(current).getPoint(), point, scale) > 1e-6) {
				return current;
			}
		}
		return -1;
	}

	/** @return turn between the chords of a vertex, in degrees ({@code NaN} if degenerate) */
	private static double turnDegrees(LocusPoint2D a, LocusPoint2D v, LocusPoint2D b) {
		double x1 = v.getX() - a.getX();
		double y1 = v.getY() - a.getY();
		double x2 = b.getX() - v.getX();
		double y2 = b.getY() - v.getY();
		if (Math.hypot(x1, y1) == 0 || Math.hypot(x2, y2) == 0) {
			return Double.NaN;
		}
		return Math.toDegrees(Math.atan2(Math.abs(x1 * y2 - y1 * x2), x1 * x2 + y1 * y2));
	}

	/** @return the corner of the one-sided tangents at {@code t}, in degrees */
	private static double cornerDegrees(LocusDefinition2D definition, LocusPoint2D vertex,
			double parameter) {
		double step = 1e-6;
		return turnDegrees(evaluate(definition, parameter - step), vertex,
				evaluate(definition, parameter + step));
	}

	private static double deviationPixels(LocusPoint2D point, LocusPoint2D a, LocusPoint2D b,
			double scale) {
		double dx = (b.getX() - a.getX()) * scale;
		double dy = (b.getY() - a.getY()) * scale;
		double px = (point.getX() - a.getX()) * scale;
		double py = (point.getY() - a.getY()) * scale;
		double length = Math.hypot(dx, dy);
		if (length == 0) {
			return Math.hypot(px, py);
		}
		double along = (px * dx + py * dy) / length;
		double across = Math.abs(px * dy - py * dx) / length;
		return across + (along < 0 ? -along : along > length ? along - length : 0);
	}

	private static double distancePixels(LocusPoint2D a, LocusPoint2D b, double scale) {
		return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY()) * scale;
	}

	private static String fingerprint(LocusRenderData2D drawing) {
		return drawing.getVertices().stream().map(vertex -> Long.toHexString(
				Double.doubleToLongBits(vertex.getSemanticParameter())) + ":"
				+ vertex.startsSubpath()).collect(Collectors.joining("|"));
	}

	private static void assertNotAvailable(LocusBoundaryImage2D image) {
		assertFalse(image.isEnclosed(), image.toString());
		assertTrue(image.getDiagnostic().startsWith(LocusEvaluator2D.LIMIT_FORM_NOT_AVAILABLE),
				image.getDiagnostic());
	}

	private Result certificate(String label) {
		Result result = ((AlgoDependentPointLocusV2) locus(label).getParentAlgorithm())
				.getHybridExistenceCertificate();
		assertNotNull(result, label);
		return result;
	}

	private LocusDefinition2D definition(String label) {
		LocusDefinition2D definition = locus(label).getSemanticDefinition();
		assertNotNull(definition, label);
		return definition;
	}

	private GeoLocusV2 locus(String label) {
		return assertInstanceOf(GeoLocusV2.class, lookup(label), label);
	}
}
