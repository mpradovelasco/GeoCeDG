/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.geocedg.common.euclidian.draw.LocusRenderCache2D;
import org.geocedg.common.euclidian.draw.LocusRenderData2D;
import org.geocedg.common.euclidian.draw.LocusRenderPolicy2D;
import org.geocedg.common.export.AdaptiveCurveApproximationBuilder2D;
import org.geocedg.common.export.ApproximationEvidence;
import org.geocedg.common.export.GeometryExportRequest;
import org.geocedg.common.export.SemanticCurveExportAdapter2D;
import org.geocedg.common.export.SemanticCurveExportAdapter2D.SourceResult;
import org.geocedg.common.export.SourceExportOutcome;
import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.ConstructionValidityPredicate2D;
import org.geocedg.common.kernel.locus.LocusBoundary2D;
import org.geocedg.common.kernel.locus.LocusBoundarySource2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Maximality;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPointComponent2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.EvaluationStatus;
import org.geocedg.common.kernel.locus.LocusSemanticPointCause2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.LocusSingularParameter2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.kernel.locus.exact.CircleDriverRootStructure2D;
import org.geocedg.common.kernel.locus.exact.ExactBudget1D;
import org.geocedg.common.kernel.locus.exact.ExactBudgetExhaustedException;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.geocedg.common.kernel.locus.exact.RealRootStructure1D;
import org.geocedg.common.kernel.locus.exact.TrigonometricPolynomial2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.CertifiedRoot;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.CertifiedRoot.RootKind;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.ExactEvidence;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Policy;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D.Result;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.main.UndoRedoMode;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 step T4: the hybrid existence certifier ({@code locus-existence/hybrid/v1};
 * {@code locus-v2-existence-components.md} section 15; {@code
 * locus-v2-certified-construction-model.md} sections 10.5 to 10.9) on synthetic
 * constructions, through the certificate of each revision (published since step T11). Matrix
 * rows V-TOT-1, V-PER-1 (synthetic part), V-CRIT-1 to V-CRIT-6 and V-CRIT-10, V-TR-2, V-POINT-1,
 * V-POINT-2, V-POINT-4 and V-POINT-6 (certificate parts), V-COINC-1, V-COINC-2, V-COINC-4,
 * V-COINC-5 and V-COINC-7 (certificate parts), V-LVL-1, V-LVL-3 (structure part), V-COMPL-1,
 * V-V3-3 and V-V3-4 (existence parts), and gates GC-4 and GC-8. Corrective descendant T13-R4
 * (decision D-CU2-T13-R4): V-DET-1 on the synthetic constructions and V-INV-2.
 */
class PreG9BCu2HybridCertificationTest extends BaseUnitTest {
	private static final String BRANCH = "generator.main";
	/** Double root of the exact synthetic critical configuration (matrix section 1). */
	private static final double CRITICAL = Math.atan2(4, 3);
	/** Isolated root of the synthetic external tangency (matrix section 1). */
	private static final double EXTERNAL = Math.atan2(-4, -3);
	/** Transition bracket width of the default policy: {@code 2^-40} periods. */
	private static final double DELTA = Math.scalb(2 * Math.PI, -40);
	/**
	 * V-INV-2: factor that makes every constant of matrix section 1 a multiple of 125, so
	 * that scales from {@code 10^-3} to {@code 10^3} are exact in binary64.
	 */
	private static final BigDecimal UNIT = BigDecimal.valueOf(125);
	/** The configurations with a second output on the ray (decision D-CU2-T13-R7). */
	private static final List<Configuration> UNLABELLED = List.of(Configuration.CRITICAL,
			Configuration.BITE, Configuration.PENETRATION);
	private static final MathContext REFERENCE = new MathContext(60, RoundingMode.HALF_EVEN);
	/** Pi to 60 digits, for the enclosure of the seam. */
	private static final BigDecimal PI = new BigDecimal(
			"3.14159265358979323846264338327950288419716939937510582097494");

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/**
	 * V-TOT-1: a program of structurally total steps is certified on the whole circle without
	 * any box.
	 */
	@Test
	void structuralTotalityDecidesWithoutAnyBox() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("M=Midpoint(N,(3,4))");
		add("lm=LocusV2(M,N)");
		add("T=Translate(N,Vector((1,2)))");
		add("lt=LocusV2(T,N)");
		for (String label : List.of("lm", "lt")) {
			long boxes = work(label, LocusCertificationWork2D.EXISTENCE_BOX);
			Result result = certificate(label);
			LocusExistenceStructure2D structure = result.getStructure();
			assertEquals(HybridExistenceCertifier2D.CONTRACT, structure.getCertifierContract());
			assertEquals(Completeness.COMPLETE, structure.getCompleteness(), label);
			assertTrue(structure.isClosedPeriodic(), label);
			assertEquals(LocusEvidenceNature2D.EXACT,
					structure.getContinuousValidComponents().get(0).getNature());
			assertEquals(0, result.getExistenceBoxes(), label);
			assertEquals(boxes, work(label, LocusCertificationWork2D.EXISTENCE_BOX), label);
		}
	}

	/**
	 * V-CRIT-1, V-CRIT-4, V-COINC-1 and V-PER-1 (synthetic part): the exact critical
	 * configuration is one closed periodic component with a singular parameter of
	 * multiplicity 2 at {@code atan2(4, 3)}, with neither a transition nor a band; the boxes
	 * that wrap the seam certify the closure.
	 */
	@Test
	void theCriticalConfigurationIsOneClosedComponentWithASingularParameter() {
		critical("40");
		for (String label : List.of("lq", "lr")) {
			Result result = certificate(label);
			LocusExistenceStructure2D structure = result.getStructure();
			assertEquals(Completeness.COMPLETE, structure.getCompleteness(), label);
			assertTrue(structure.isClosedPeriodic(), label);
			assertEquals(1, structure.getContinuousValidComponents().size(), label);
			assertEquals(0, result.getCertifiedTransitions(), label);
			assertEquals(0, result.getUnresolvedBands(), label);
			for (Coverage coverage : structure.getCoverage()) {
				assertEquals(CoverageState.CERTIFIED_EXISTS, coverage.getState(), label);
			}
			LocusSingularParameter2D singular = structure.getSingularParameters().get(0);
			assertEquals(1, structure.getSingularParameters().size(), label);
			assertEquals(2, singular.getMultiplicity());
			assertEquals(LocusEvidenceNature2D.EXACT, singular.getNature());
			assertEnclosed(CRITICAL, singular.getParameterLower(),
					singular.getParameterUpper());
			assertExactWithinBudget(result);
		}
		// V-CRIT-6, live part: Q = R at the singular parameter.
		moveDriver((GeoPoint) lookup("N"), CRITICAL);
		assertEquals(((GeoPoint) lookup("Q")).getInhomX(), ((GeoPoint) lookup("R"))
				.getInhomX(), 1e-6);
	}

	/**
	 * V-CRIT-2, V-COINC-4 and V-COINC-5 (labelled outputs): below the critical radius there is
	 * no real root and the component is closed; above it two simple roots bound one arc that
	 * crosses the seam, with closed certified ends whose enclosures contain the exact roots,
	 * and the gap between them is certified nonexistence.
	 */
	@Test
	void theNeighboursOfTheCriticalRadiusArePenetrationAndBite() {
		for (int k : new int[] {10, 30, 47}) {
			critical("40-2^(-" + k + ")");
			for (String label : List.of("lq", "lr")) {
				Result result = certificate(label);
				LocusExistenceStructure2D structure = result.getStructure();
				assertEquals(Completeness.COMPLETE, structure.getCompleteness(), label + k);
				assertTrue(structure.isClosedPeriodic(), label + k);
				assertTrue(structure.getSingularParameters().isEmpty(), label + k);
				assertTrue(result.getRoots().isEmpty(), label + k);
			}
			getKernel().clearConstruction(true);
			critical("40+2^(-" + k + ")");
			for (String label : List.of("lq", "lr")) {
				Result result = certificate(label);
				LocusExistenceStructure2D structure = result.getStructure();
				assertEquals(Completeness.COMPLETE, structure.getCompleteness(), label + k);
				assertFalse(structure.isClosedPeriodic(), label + k);
				assertEquals(2, result.getCertifiedTransitions(), label + k);
				ContinuousComponent arc = structure.getContinuousValidComponents().get(0);
				assertEquals(1, structure.getContinuousValidComponents().size());
				assertTrue(arc.crossesSeam(), label + k);
				assertEquals(Maximality.ESTABLISHED, arc.getMaximality());
				LocusBoundary2D start = arc.getLowerBoundary();
				LocusBoundary2D end = arc.getUpperBoundary();
				for (LocusBoundary2D boundary : List.of(start, end)) {
					assertEquals(LocusBoundary2D.Kind.CERTIFIED_TRANSITION, boundary.getKind());
					assertTrue(boundary.isIncluded(), "a discriminant root is a closed end");
					assertEquals(LocusEvidenceNature2D.EXACT, boundary.getNature());
					assertTrue(boundary.getEnclosureUpper() - boundary.getEnclosureLower()
							<= DELTA);
				}
				// The arc ends just before the gap and starts just after it.
				assertTrue(end.getEnclosureUpper() < CRITICAL);
				assertTrue(CRITICAL < start.getEnclosureLower());
				assertEquals(end.getEnclosureLower(), arc.getEnd());
				assertEquals(start.getEnclosureUpper(), arc.getStart());
				Coverage gap = coverageAt(structure, CRITICAL);
				assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST, gap.getState());
				assertEquals(LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE, gap.getCause());
				assertTrue(gap.getEvidence().endsWith("DISCRIMINANT_NEGATIVE"));
				assertExactWithinBudget(result);
			}
			getKernel().clearConstruction(true);
		}
	}

	/**
	 * V-CRIT-5, V-POINT-1, V-POINT-4 and V-POINT-6 (certificate parts): the external tangency
	 * is a point component, never an interval: an enclosure of {@code atan2(-4, -3)}, closed,
	 * maximal between certified nonexistence, with the certified position (15, 20); the cover
	 * is complete, and no binary64 parameter selects the point.
	 */
	@Test
	void anExternalTangencyIsAPointComponent() {
		tangency("(83,19)", "Ray");
		Result result = certificate("lq");
		LocusExistenceStructure2D structure = result.getStructure();
		assertEquals(Completeness.COMPLETE, structure.getCompleteness());
		assertTrue(structure.getContinuousValidComponents().isEmpty());
		assertEquals(1, structure.getPointComponents().size());
		LocusPointComponent2D point = structure.getPointComponents().get(0);
		assertEnclosed(EXTERNAL, point.getParameterLower(), point.getParameterUpper());
		assertTrue(point.getParameterUpper() - point.getParameterLower() <= DELTA);
		assertEquals(Maximality.ESTABLISHED, point.getMaximality());
		assertEquals("DISCRIMINANT", point.getSource().getPredicate());
		assertEquals(LocusPointComponent2D.PositionState.CERTIFIED_ENCLOSURE,
				point.getPositionState());
		double[] position = point.getPositionEnclosure();
		assertEnclosed(15, position[0], position[1]);
		assertEnclosed(20, position[2], position[3]);
		assertEquals(0, point.getLength());
		assertNull(structure.getComponentContaining(EXTERNAL));
		assertNull(structure.getComponentContaining(Math.nextUp(EXTERNAL)));
		assertEquals(CoverageState.CERTIFIED_ISOLATED_POINTS,
				coverageAt(structure, EXTERNAL).getState());
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				coverageAt(structure, EXTERNAL + 0.01).getState());
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				coverageAt(structure, EXTERNAL - 0.01).getState());
	}

	/**
	 * V-POINT-2 and V-POINT-6: two point components, one at the seam, with distinct sources and
	 * certified nonexistence between them; neither joins the other across the seam.
	 */
	@Test
	void twoPointComponentsAreSeparatedOneAtTheSeam() {
		tangency("(15,70)", "Line");
		LocusExistenceStructure2D structure = certificate("lq").getStructure();
		assertEquals(Completeness.COMPLETE, structure.getCompleteness());
		assertTrue(structure.getContinuousValidComponents().isEmpty());
		List<LocusPointComponent2D> points = structure.getPointComponents();
		assertEquals(2, points.size());
		LocusPointComponent2D seam = points.get(0);
		assertEquals(-Math.nextUp(Math.PI), seam.getParameterLower());
		assertEquals(-Math.PI, seam.getParameterUpper());
		assertEnclosed(EXTERNAL, points.get(1).getParameterLower(),
				points.get(1).getParameterUpper());
		assertNotEquals(seam.getSource(), points.get(1).getSource());
		for (LocusPointComponent2D point : points) {
			assertEquals(Maximality.ESTABLISHED, point.getMaximality());
		}
		double[] position = seam.getPositionEnclosure();
		assertEnclosed(-25, position[0], position[1]);
		assertEnclosed(0, position[2], position[3]);
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				coverageAt(structure, 0.5 * (EXTERNAL - Math.PI)).getState());
		assertEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST,
				coverageAt(structure, 3).getState());
	}

	/**
	 * V-POINT-5 ({@code Point(L, u)}) and V-POINT-4 (the binary64 neighbours of {@code t*}),
	 * corrective mandate, Level B: no binary64 parameter reaches the tangency. A semantic point
	 * whose parameter lies in the enclosure of the point component is undefined with the typed
	 * cause {@code POINT_COMPONENT}, and one outside it, in certified nonexistence, with
	 * {@code OUTSIDE_COMPONENTS} ({@code locus-v2-existence-components.md} section 15.8); it
	 * binds no metric position and accepts no address, and the evaluator publishes no valid
	 * point at any of those parameters. Before the correction the point was undefined without
	 * any cause.
	 */
	@Test
	void aSemanticPointNearTheTangencyIsUndefinedWithATypedCause() {
		tangency("(83,19)", "Ray");
		LocusPointComponent2D component = structure("lq").getPointComponents().get(0);
		double lower = component.getParameterLower();
		double upper = component.getParameterUpper();
		GeoNumeric parameter = new GeoNumeric(getConstruction(), EXTERNAL);
		parameter.setLabel("tStar");
		GeoPoint point = add("A=Point(lq,\"" + BRANCH + "\",tStar)");
		AlgoSemanticLocusPoint2D parent = assertInstanceOf(AlgoSemanticLocusPoint2D.class,
				point.getParentAlgorithm());
		LocusDefinition2D definition = locus("lq").getSemanticDefinition();
		Map<Double, LocusSemanticPointCause2D> expected = new LinkedHashMap<>();
		for (double inside : new double[] {lower, Math.nextUp(lower), Math.nextDown(EXTERNAL),
				EXTERNAL, Math.nextUp(EXTERNAL), Math.nextDown(upper), upper}) {
			expected.put(inside, LocusSemanticPointCause2D.POINT_COMPONENT);
		}
		for (double outside : new double[] {Math.nextDown(lower), Math.nextUp(upper),
				lower - 0x1.0p-30, upper + 0x1.0p-30, EXTERNAL - 0.01, EXTERNAL + 0.01}) {
			expected.put(outside, LocusSemanticPointCause2D.OUTSIDE_COMPONENTS);
		}
		for (Map.Entry<Double, LocusSemanticPointCause2D> entry : expected.entrySet()) {
			double value = entry.getKey();
			parameter.setValue(value);
			parameter.updateCascade();
			String where = "u=" + Double.toHexString(value);
			assertFalse(point.isDefined(), where);
			assertEquals(entry.getValue(), parent.getResolutionCause(), where);
			assertNull(parent.getSelectorStatus(), where);
			assertNull(parent.getCurrentSemanticAddress(), where);
			assertNull(parent.getSemanticAddress(), where);
			assertNull(parent.getMetricPositionBinding(), where);
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
				assertFalse(definition.evaluate(BRANCH, value, session).isValid(), where);
			}
		}
	}

	/**
	 * V-POINT-6 (intersections and dependent loci) and V-POINT-5 (total length and drawing) on
	 * the constructions of V-POINT-1 and V-POINT-2, the external tangency and the line of the
	 * two point components: an intersection with a line far from the points or through
	 * (15, 20) is neither {@code EMPTY} nor complete, since no position is excluded
	 * rigorously; a locus traced by a point of that support declares the limitation of its
	 * point components and is not {@code COMPLETE}; the total length is exactly 0 with a
	 * structural guarantee; the drawing has no vertex and a partial fidelity, and declares each
	 * point component with a typed mark that gives its enclosure and the state of its position.
	 */
	@Test
	void pointComponentsAreNeverExcludedOrPromotedByConsumers() {
		for (String[] configuration : new String[][] {{"(83,19)", "Ray"}, {"(15,70)", "Line"}}) {
			tangency(configuration[0], configuration[1]);
			String context = configuration[0] + " " + configuration[1];
			int points = structure("lq").getPointComponents().size();
			assertTrue(points >= 1, context);
			assertTrue(structure("lq").getContinuousValidComponents().isEmpty(), context);
			for (String target : List.of("target:y=500", "target=Line((15,20),(16,21))")) {
				add(target);
				GeoLocusIntersectionResult result = add("X=Intersect(lq,target)");
				LocusIntersectionResult2D rich = result.getIntersectionResult();
				assertNotEquals(IntersectionSemanticMetadata2D.GeometryKind.EMPTY,
						rich.getGeometryKind(), context + " " + target);
				assertNotEquals(IntersectionSemanticMetadata2D.Completeness.COMPLETE,
						rich.getCompletenessEvidence().getCompleteness(), context + " " + target);
				lookup("X").remove();
				lookup("target").remove();
			}
			add("u0=0");
			add("S=Point(lq,\"" + BRANCH + "\",u0)");
			add("Y=S+(1,0)");
			GeoLocusV2 dependent = add("ly=LocusV2(Y,S)");
			LocusExistenceStructure2D limited = dependent.getSemanticDefinition()
					.getBranches().get(0).getExistenceStructure();
			assertNotEquals(Completeness.COMPLETE, limited.getCompleteness(), context);
			assertTrue(limited.getCoverage().stream().anyMatch(coverage ->
					coverage.getCause() == LocusCoverageCause2D.UPSTREAM_NOT_ESTABLISHED
							&& coverage.getEvidence().endsWith(
									"upstream-point-components-not-composed")),
					context + " " + limited);
			GeoLocusMetricResult length = add("lengthQ=LocusLength(lq)");
			LocusMetricResult2D total = length.getMetricResult();
			assertEquals(MetricComputationStatus.SUCCESS, total.getComputationStatus(),
					context + " " + total.getDiagnostics());
			assertEquals(0.0, total.getMetricValue().getFiniteValue().orElse(Double.NaN),
					context);
			assertEquals(MetricGuaranteeClass2D.STRUCTURAL, total.getGuaranteeClass(), context);
			LocusRenderData2D drawing = new LocusRenderCache2D().getOrBuild(locus("lq"),
					LocusRenderPolicy2D.adaptive(1, 800, 600, 10, 10, 32, 0.75, 12));
			assertTrue(drawing.getVertices().isEmpty(), context);
			assertEquals(LocusRenderData2D.Fidelity.PARTIAL, drawing.getFidelity(), context);
			List<LocusRenderData2D.Mark> declared = drawing.getMarks().stream().filter(mark ->
					mark.getKind() == LocusRenderData2D.MarkKind.POINT_COMPONENT_NOT_DRAWN)
					.collect(Collectors.toList());
			assertEquals(points, declared.size(), context + " " + drawing.getMarks());
			for (LocusRenderData2D.Mark mark : declared) {
				assertTrue(mark.getDetail().contains("enclosure")
						&& mark.getDetail().contains("position"), mark.getDetail());
			}
			getKernel().clearConstruction(true);
		}
	}

	/**
	 * V-POINT-6 (similarity images), corrective mandate, Level B: the image of the external
	 * tangency by each public similarity keeps the point component of its source with its
	 * parameter enclosure, structural source, maximality, proof, evidence and lineage keys and
	 * every other fact of the structure, and transforms its certified position enclosure
	 * ({@code locus-v2-similarity-transformations.md}, scope note A2.5). The image enclosure
	 * holds the 60-digit image of the exact position (15, 20) under the exact similarity of
	 * the captured inputs, is no wider than the transformed source enclosure up to rounding,
	 * no longer holds the source position, and is the one the export declares. Before the
	 * correction every image kept the source position.
	 */
	@Test
	void aSimilarityImageTransformsThePositionOfAPointComponent() {
		tangency("(83,19)", "Ray");
		add("it=Translate(lq,Vector((1,0)))");
		add("ir=Rotate(lq,pi/2,(3,4))");
		add("ip=Mirror(lq,(1,2))");
		add("il=Mirror(lq,Line((0,1),(1,3)))");
		add("id=Dilate(lq,-3,(2,-1))");
		add("iz=Dilate(lq,0,(2,-1))");
		LocusExistenceStructure2D source = structure("lq");
		LocusPointComponent2D original = source.getPointComponents().get(0);
		double[] box = original.getPositionEnclosure();
		BigDecimal angle = new BigDecimal(((NumberValue) lookup("ir").getParentAlgorithm()
				.getInput()[1]).getDouble());
		BigDecimal cosine = series(angle, true, REFERENCE);
		BigDecimal sine = series(angle, false, REFERENCE);
		Map<String, BigDecimal[]> images = new LinkedHashMap<>();
		images.put("it", new BigDecimal[] {BigDecimal.valueOf(16), BigDecimal.valueOf(20)});
		images.put("ir", new BigDecimal[] {BigDecimal.valueOf(3).add(BigDecimal.valueOf(12)
				.multiply(cosine)).subtract(BigDecimal.valueOf(16).multiply(sine)),
				BigDecimal.valueOf(4).add(BigDecimal.valueOf(12).multiply(sine))
						.add(BigDecimal.valueOf(16).multiply(cosine))});
		images.put("ip", new BigDecimal[] {BigDecimal.valueOf(-13), BigDecimal.valueOf(-16)});
		// The reflection in 2x - y + 1 = 0 of (15, 20) is (31/5, 122/5).
		images.put("il", new BigDecimal[] {new BigDecimal("6.2"), new BigDecimal("24.4")});
		images.put("id", new BigDecimal[] {BigDecimal.valueOf(-37), BigDecimal.valueOf(-64)});
		images.put("iz", new BigDecimal[] {BigDecimal.valueOf(2), BigDecimal.valueOf(-1)});
		Map<String, Double> scales = Map.of("it", 1.0, "ir", 1.0, "ip", 1.0, "il", 1.0,
				"id", 3.0, "iz", 0.0);
		String sourceDeclaration = declaration(locus("lq"));
		assertTrue(sourceDeclaration.contains(java.util.Arrays.toString(box)),
				sourceDeclaration);
		for (Map.Entry<String, BigDecimal[]> image : images.entrySet()) {
			String label = image.getKey();
			LocusExistenceStructure2D structure = locus(label).getSemanticDefinition()
					.getBranch(BRANCH).getExistenceStructure();
			assertEquals(1, structure.getPointComponents().size(), label);
			LocusPointComponent2D point = structure.getPointComponents().get(0);
			assertEquals(source.withPointComponents(structure.getPointComponents())
					.getSemanticSignature(), structure.getSemanticSignature(), label);
			assertEquals(original.getParameterLower(), point.getParameterLower(), label);
			assertEquals(original.getParameterUpper(), point.getParameterUpper(), label);
			assertEquals(original.getSource().getSignature(), point.getSource().getSignature(),
					label);
			assertEquals(original.getMaximality(), point.getMaximality(), label);
			assertEquals(original.getNature(), point.getNature(), label);
			assertEquals(original.getEvidenceKey(), point.getEvidenceKey(), label);
			assertEquals(LocusComponentLineage2D.of(source, original),
					LocusComponentLineage2D.of(structure, point), label);
			assertEquals(LocusPointComponent2D.PositionState.CERTIFIED_ENCLOSURE,
					point.getPositionState(), label);
			double[] position = point.getPositionEnclosure();
			BigDecimal[] reference = image.getValue();
			assertTrue(encloses(position[0], position[1], reference[0])
					&& encloses(position[2], position[3], reference[1]), label + " "
					+ java.util.Arrays.toString(position) + " does not hold (" + reference[0]
					+ ", " + reference[1] + ")");
			assertFalse(encloses(position[0], position[1], BigDecimal.valueOf(15))
					&& encloses(position[2], position[3], BigDecimal.valueOf(20)),
					label + " keeps the source position");
			double spread = scales.get(label) * ((box[1] - box[0]) + (box[3] - box[2]))
					+ 1e-12;
			assertTrue(position[1] - position[0] <= spread
					&& position[3] - position[2] <= spread, label + " "
					+ java.util.Arrays.toString(position));
			String declared = declaration(locus(label));
			assertTrue(declared.contains(java.util.Arrays.toString(position)), declared);
			assertFalse(declared.contains(java.util.Arrays.toString(box)), declared);
		}
	}

	/**
	 * V-POINT-6 (similarity images), soundness of the transported enclosure: for random boxes
	 * from {@code 2^-20} to {@code 2^30}, translations, rotations, point reflections,
	 * reflections in lines given by unnormalized coefficients (subnormal quotients included)
	 * and dilations, the enclosure holds the 60-digit image of every corner of the box under
	 * the exact similarity of the captured inputs: the exact cosine and sine of the binary64
	 * angle, and the exact reflection in the line of the original coefficients. It is no wider
	 * than the transformed box up to a relative {@code 10^-12}, and a box whose image
	 * overflows has no enclosure.
	 */
	@Test
	void theTransformedEnclosureHoldsTheExactImage() {
		Random random = new Random(20261023);
		List<String> problems = new ArrayList<>();
		int checked = 0;
		for (int trial = 0; trial < 3000; trial++) {
			double scale = Math.scalb(1.0, random.nextInt(51) - 20);
			double[] box = new double[4];
			for (int axis = 0; axis < 2; axis++) {
				double centre = (random.nextDouble() - 0.5) * scale;
				double width = trial % 7 == 0 ? 0 : random.nextDouble() * scale * 0x1.0p-10;
				box[2 * axis] = centre;
				box[2 * axis + 1] = centre + width;
			}
			double a = (random.nextDouble() - 0.5) * scale;
			double b = (random.nextDouble() - 0.5) * scale;
			BigDecimal[] values = {new BigDecimal(a), new BigDecimal(b),
					new BigDecimal((random.nextDouble() - 0.5) * scale)};
			LocusSimilarityTransform2D transform;
			double factor = 1;
			switch (trial % 5) {
			case 0:
				transform = LocusSimilarityTransform2D.translation(a, b);
				break;
			case 1:
				double angle = trial % 10 == 1 ? Math.PI / 2 * (1 + random.nextInt(4))
						: (random.nextDouble() - 0.5) * 16;
				values[2] = new BigDecimal(angle);
				transform = LocusSimilarityTransform2D.rotation(angle, a, b);
				break;
			case 2:
				transform = LocusSimilarityTransform2D.pointReflection(a, b);
				break;
			case 3:
				// Unnormalized coefficients; every fourth line has a subnormal quotient.
				double small = trial % 4 == 3 ? 1e-300 * (random.nextDouble() + 0.5) : a;
				values[0] = new BigDecimal(small);
				values[1] = new BigDecimal(b * 1e10);
				transform = LocusSimilarityTransform2D.lineReflection(small, b * 1e10,
						values[2].doubleValue());
				break;
			default:
				factor = (random.nextDouble() - 0.5) * 8;
				values[2] = new BigDecimal(factor);
				transform = LocusSimilarityTransform2D.dilation(factor, a, b);
				break;
			}
			double[] image = transform.transformEnclosure(box);
			String context = transform.getSemanticSignature() + " box="
					+ java.util.Arrays.toString(box);
			if (image == null) {
				problems.add(context + ": no enclosure");
				continue;
			}
			for (int corner = 0; corner < 4; corner++) {
				BigDecimal[] exact = exactImage(trial % 5, values,
						new BigDecimal(box[corner / 2]), new BigDecimal(box[2 + corner % 2]));
				if (!encloses(image[0], image[1], exact[0])
						|| !encloses(image[2], image[3], exact[1])) {
					problems.add(context + ": " + java.util.Arrays.toString(image)
							+ " misses (" + exact[0] + ", " + exact[1] + ")");
				}
				checked++;
			}
			double magnitude = 1 + (Math.abs(factor) + 1) * (Math.max(Math.abs(box[0]),
					Math.max(Math.abs(box[3]), Math.abs(box[1]))) + Math.abs(a) + Math.abs(b)
					+ Math.abs(values[2].doubleValue()));
			double spread = Math.abs(factor) * ((box[1] - box[0]) + (box[3] - box[2]))
					+ 1e-12 * magnitude;
			if (image[1] - image[0] > spread || image[3] - image[2] > spread) {
				problems.add(context + ": wide " + java.util.Arrays.toString(image));
			}
		}
		assertEquals(List.of(), problems.subList(0, Math.min(10, problems.size())));
		assertEquals(4 * 3000, checked);
		assertNull(LocusSimilarityTransform2D.dilation(10, 0, 0).transformEnclosure(
				new double[] {0, Double.MAX_VALUE, 0, 1}));
		assertNull(LocusSimilarityTransform2D.translation(Double.MAX_VALUE, 0)
				.transformEnclosure(new double[] {Double.MAX_VALUE, Double.MAX_VALUE, 0, 0}));
	}

	/**
	 * @return 60-digit image of the point under the exact similarity of the captured inputs:
	 *         translation by {@code (v0, v1)}, rotation through {@code v2} about
	 *         {@code (v0, v1)}, point reflection in {@code (v0, v1)}, reflection in the line
	 *         {@code v0 x + v1 y + v2 = 0}, or dilation by {@code v2} about {@code (v0, v1)}
	 */
	private static BigDecimal[] exactImage(int family, BigDecimal[] values, BigDecimal x,
			BigDecimal y) {
		MathContext context = REFERENCE;
		switch (family) {
		case 0:
			return new BigDecimal[] {x.add(values[0]), y.add(values[1])};
		case 1:
			BigDecimal cosine = series(values[2], true, context);
			BigDecimal sine = series(values[2], false, context);
			BigDecimal dx = x.subtract(values[0]);
			BigDecimal dy = y.subtract(values[1]);
			return new BigDecimal[] {values[0].add(cosine.multiply(dx, context), context)
					.subtract(sine.multiply(dy, context), context),
					values[1].add(sine.multiply(dx, context), context)
							.add(cosine.multiply(dy, context), context)};
		case 2:
			return new BigDecimal[] {values[0].multiply(BigDecimal.valueOf(2)).subtract(x),
					values[1].multiply(BigDecimal.valueOf(2)).subtract(y)};
		case 3:
			BigDecimal norm = values[0].multiply(values[0]).add(values[1].multiply(values[1]));
			BigDecimal distance = values[0].multiply(x).add(values[1].multiply(y))
					.add(values[2]).multiply(BigDecimal.valueOf(2)).divide(norm, context);
			return new BigDecimal[] {x.subtract(distance.multiply(values[0], context), context),
					y.subtract(distance.multiply(values[1], context), context)};
		default:
			return new BigDecimal[] {values[0].add(values[2].multiply(x.subtract(values[0]),
					context), context), values[1].add(values[2].multiply(y.subtract(values[1]),
							context), context)};
		}
	}

	/**
	 * V-CON-3 and section 15.3 of {@code locus-v2-existence-components.md} (corrective
	 * mandate, Level B): an included end of a certified arc across the seam belongs to the
	 * arc and is evaluated, through {@code evaluateComponentEnd} and through the lifted ends of
	 * {@code evaluateInComponent}, at its own binary64 parameter, as the live construction
	 * with the driver there, bit for bit. The periodic canonicalization of the provider,
	 * {@code -pi + (t + pi)}, moves most ends a few ulps off the arc, whose membership is
	 * exact; before the correction those ends failed with OUT_OF_DOMAIN and the total length
	 * of the arc with NUMERICAL_FAILURE. The total length is now an estimate or a typed budget
	 * limitation (section 25), never an evaluation failure at a certified end.
	 */
	@Test
	void theIncludedEndsOfAnArcAcrossTheSeamAreEvaluatedAtTheirOwnParameter() {
		int moved = 0;
		for (String radius : List.of("40+2^(-30)", "40+2^(-10)", "40+2^(-4)", "41", "45")) {
			critical(radius);
			add("lengthQ=LocusLength(lq)");
			GeoPoint driver = (GeoPoint) lookup("N");
			for (String label : List.of("lq", "lr")) {
				LocusDefinition2D definition = locus(label).getSemanticDefinition();
				ContinuousComponent arc = definition.getBranch(BRANCH).getExistenceStructure()
						.getContinuousValidComponents().get(0);
				assertTrue(arc.crossesSeam(), radius + " " + label);
				GeoPoint live = (GeoPoint) lookup(label.equals("lq") ? "Q" : "R");
				for (boolean upper : List.of(false, true)) {
					double end = upper ? arc.getEnd() : arc.getStart();
					String context = radius + " " + label + (upper ? " end " : " start ")
							+ Double.toHexString(end);
					assertTrue((upper ? arc.getUpperBoundary() : arc.getLowerBoundary())
							.isBinaryEndIncluded(), context);
					moved += definition.getProvider().canonicalize(end) != end ? 1 : 0;
					LocusEvaluation2D atEnd;
					LocusEvaluation2D inComponent;
					try (LocusEvaluationSession2D session = LocusEvaluationSession2D
							.memoizing(8)) {
						atEnd = definition.evaluateComponentEnd(BRANCH, end, upper, session);
						inComponent = definition.evaluateInComponent(BRANCH, arc.getInterval(),
								upper ? arc.getInterval().getUpper()
										: arc.getInterval().getLower(), session);
					}
					moveDriver(driver, end);
					assertTrue(atEnd.isValid() && inComponent.isValid() && live.isDefined(),
							context + " " + atEnd.getDiagnostic() + " "
									+ inComponent.getDiagnostic());
					for (LocusEvaluation2D evaluation : List.of(atEnd, inComponent)) {
						assertEquals(Double.doubleToRawLongBits(live.getInhomX()),
								Double.doubleToRawLongBits(evaluation.getPoint().getX()), context);
						assertEquals(Double.doubleToRawLongBits(live.getInhomY()),
								Double.doubleToRawLongBits(evaluation.getPoint().getY()), context);
					}
				}
			}
			LocusMetricResult2D length = ((GeoLocusMetricResult) lookup("lengthQ"))
					.getMetricResult();
			String status = radius + " " + length.getComputationStatus() + " "
					+ length.getDiagnostics();
			assertTrue(length.getComputationStatus() == MetricComputationStatus.SUCCESS
					|| length.getDiagnostics().stream().allMatch(diagnostic ->
							diagnostic.getCode() == MetricDiagnosticCode2D.WORK_BUDGET_EXHAUSTED),
					status);
			assertTrue(length.getDiagnostics().stream().noneMatch(diagnostic ->
					diagnostic.getCode() == MetricDiagnosticCode2D.EVALUATION_FAILED), status);
			getKernel().clearConstruction(true);
		}
		assertTrue(moved >= 8, "ends that the canonicalization moves: " + moved);
	}

	/**
	 * V-CON-3 (synthetic part): the total length of the closed loci {@code lq} and {@code lr}
	 * of the exact critical configuration. Both traced points stay on the circle {@code p} of
	 * radius 25; as the ray from {@code P} turns between its two tangents to the circle of the
	 * driver, each sweeps its arc of {@code p} between the point on the other tangent and the
	 * double root (15, 20) and back, so each length is {@code 2 * 25 * dphi}, with {@code dphi}
	 * computed to 60 digits from the tangent points (square roots only) and an arccosine. The
	 * product publishes an estimate (class 3 of section 25.1 of {@code locus-v2-metrics.md})
	 * whose established error amount holds the difference to that reference.
	 */
	@Test
	void theTotalLengthsOfTheCriticalLociMatchTheExactSweptArcs() {
		critical("40");
		add("lengthQ=LocusLength(lq)");
		add("lengthR=LocusLength(lr)");
		MathContext context = REFERENCE;
		BigDecimal px = BigDecimal.valueOf(-25);
		BigDecimal py = BigDecimal.valueOf(50);
		BigDecimal dx = BigDecimal.valueOf(60);
		BigDecimal dy = BigDecimal.valueOf(-95);
		BigDecimal radius = BigDecimal.valueOf(40);
		BigDecimal distance = dx.multiply(dx).add(dy.multiply(dy)).sqrt(context);
		BigDecimal ratio = radius.divide(distance, context);
		BigDecimal along = ratio.negate();
		BigDecimal across = BigDecimal.ONE.subtract(ratio.multiply(ratio, context)).sqrt(context);
		BigDecimal ex = dx.divide(distance, context);
		BigDecimal ey = dy.divide(distance, context);
		BigDecimal[] near = null;
		BigDecimal[] far = null;
		for (int sign : new int[] {1, -1}) {
			// Tangent point T = C + r (along e + sign across e_perp) of the circle of the driver.
			BigDecimal cx = px.add(dx);
			BigDecimal cy = py.add(dy);
			BigDecimal sideways = across.multiply(BigDecimal.valueOf(sign));
			BigDecimal tx = cx.add(radius.multiply(along.multiply(ex).subtract(sideways
					.multiply(ey)), context));
			BigDecimal ty = cy.add(radius.multiply(along.multiply(ey).add(sideways.multiply(ex)),
					context));
			BigDecimal ux = tx.subtract(px);
			BigDecimal uy = ty.subtract(py);
			BigDecimal norm = ux.multiply(ux).add(uy.multiply(uy)).sqrt(context);
			ux = ux.divide(norm, context);
			uy = uy.divide(norm, context);
			BigDecimal b = px.multiply(ux).add(py.multiply(uy));
			BigDecimal discriminant = b.multiply(b, context).subtract(BigDecimal.valueOf(2500));
			if (discriminant.abs().compareTo(new BigDecimal("1e-40")) < 0) {
				continue; // the tangent of the double root (15, 20)
			}
			BigDecimal root = discriminant.sqrt(context);
			BigDecimal nearLambda = b.negate().subtract(root);
			BigDecimal farLambda = b.negate().add(root);
			near = new BigDecimal[] {px.add(nearLambda.multiply(ux, context)),
				py.add(nearLambda.multiply(uy, context))};
			far = new BigDecimal[] {px.add(farLambda.multiply(ux, context)),
				py.add(farLambda.multiply(uy, context))};
		}
		assertNotNull(near, "one tangent of the driver circle is not tangent to p");
		Map<String, BigDecimal> references = new LinkedHashMap<>();
		references.put("lengthQ", sweptLength(near, context));
		references.put("lengthR", sweptLength(far, context));
		for (Map.Entry<String, BigDecimal> reference : references.entrySet()) {
			LocusMetricResult2D length = ((GeoLocusMetricResult) lookup(reference.getKey()))
					.getMetricResult();
			String status = reference.getKey() + " " + length.getComputationStatus() + " "
					+ length.getGuaranteeClass() + " " + length.getDiagnostics();
			assertEquals(MetricComputationStatus.SUCCESS, length.getComputationStatus(), status);
			assertEquals(MetricGuaranteeClass2D.ESTIMATED, length.getGuaranteeClass(), status);
			BigDecimal value = new BigDecimal(length.getMetricValue().getFiniteValue()
					.orElseThrow());
			BigDecimal error = value.subtract(reference.getValue()).abs();
			double amount = length.getErrorEvidence().getEstablishedAbsoluteAmount();
			assertTrue(error.compareTo(new BigDecimal(amount)) <= 0, status + " value " + value
					+ " reference " + reference.getValue() + " error " + error + " amount "
					+ amount);
		}
	}

	/**
	 * @return {@code 2 * 25 * dphi}, {@code dphi} the angle between the point and the double
	 *         root (15, 20) on the circle of radius 25
	 */
	private static BigDecimal sweptLength(BigDecimal[] point, MathContext context) {
		BigDecimal cosine = point[0].multiply(BigDecimal.valueOf(15)).add(point[1]
				.multiply(BigDecimal.valueOf(20))).divide(BigDecimal.valueOf(625), context);
		BigDecimal angle = new BigDecimal(Math.acos(cosine.doubleValue()));
		for (int step = 0; step < 8; step++) {
			angle = angle.add(series(angle, true, context).subtract(cosine).divide(
					series(angle, false, context), context), context);
		}
		return angle.multiply(BigDecimal.valueOf(50), context);
	}

	/**
	 * V-CON-7: the images of the bite of {@code lq} and of its closed penetration by the four
	 * public commands, a reflection in a point and in a line included, keep the published
	 * structure of the source: its signature (coverage, maximality, completeness, components
	 * and their ends), its closure, its component keys and its transported lineage. The image
	 * is built by the command from the published certificate, not from the source branches.
	 */
	@Test
	void similarityImagesOfABiteKeepItsStructure() {
		List<String> commands = List.of("Translate(lq,Vector((1,0)))", "Rotate(lq,pi/3,(3,4))",
				"Mirror(lq,(1,2))", "Mirror(lq,Line((0,1),(1,3)))", "Dilate(lq,-3,(2,-1))");
		for (String radius : List.of("40+2^(-10)", "40-2^(-10)")) {
			critical(radius);
			LocusBranch2D source = locus("lq").getSemanticDefinition().getBranch(BRANCH);
			LocusExistenceStructure2D structure = source.getExistenceStructure();
			assertEquals(radius.contains("+"), !structure.isClosedPeriodic(), radius);
			for (int index = 0; index < commands.size(); index++) {
				GeoLocusV2 image = add("im" + index + "=" + commands.get(index));
				LocusBranch2D branch = image.getSemanticDefinition().getBranch(BRANCH);
				LocusExistenceStructure2D transported = branch.getExistenceStructure();
				String context = radius + " " + commands.get(index);
				assertEquals(structure.getSemanticSignature(), transported.getSemanticSignature(),
						context);
				assertEquals(structure.isClosedPeriodic(), transported.isClosedPeriodic(),
						context);
				assertEquals(structure.getCompleteness(), transported.getCompleteness(), context);
				assertEquals(LocusComponentLineage2D.keys(structure),
						LocusComponentLineage2D.keys(transported), context);
				assertEquals(source.getLineage(), branch.getLineage(), context);
				for (int component = 0; component < structure.getContinuousValidComponents()
						.size(); component++) {
					assertEquals(structure.getContinuousValidComponents().get(component)
							.getMaximality(), transported.getContinuousValidComponents()
									.get(component).getMaximality(), context);
				}
			}
			getKernel().clearConstruction(true);
		}
	}

	/** @return the message with which the export declares the point component of a locus */
	private static String declaration(GeoLocusV2 locus) {
		GeometryExportRequest request = GeometryExportRequest.builder(0.01)
				.allowedGuarantees(EnumSet.of(ApproximationEvidence.Guarantee.ESTIMATED_ERROR))
				.build();
		SourceResult result = new SemanticCurveExportAdapter2D().adaptLocus(locus, 0, request,
				new AdaptiveCurveApproximationBuilder2D.WorkLedger(request), "cu2:",
				SemanticCurveExportAdapter2D.ALL_COMPONENTS);
		List<String> declared = result.getComponents().stream()
				.map(SemanticCurveExportAdapter2D.Component::getOutcome)
				.filter(outcome -> outcome.getFidelity() == SourceExportOutcome.Fidelity.DECLARED)
				.map(SourceExportOutcome::getMessage).collect(Collectors.toList());
		assertEquals(1, declared.size(), locus.getLabelSimple() + " " + declared);
		return declared.get(0);
	}

	private static boolean encloses(double lower, double upper, BigDecimal value) {
		return new BigDecimal(lower).compareTo(value) <= 0
				&& value.compareTo(new BigDecimal(upper)) <= 0;
	}

	/**
	 * V-COINC-2, V-COINC-5 and V-COINC-7 (certificate parts) and V-LVL-3 (structure part): an
	 * unlabelled second output has a typed unresolved band around the coincident root that
	 * contains every undefined sample of the live construction and of the copy; its locus is
	 * not established, with a local component on both sides of the band, which a local
	 * consumer accepts away from the band. Since decision D-CU2-T13-R7 (H-T13-B) the band is
	 * the rigorous one of the inherited computation, which stays near the coincident roots.
	 */
	@Test
	void anUnlabelledSecondOutputLeavesLocalComponentsAroundABand() {
		for (String radius : List.of("40", "40+2^(-30)")) {
			critical(radius);
			Result result = certificate("lr2");
			LocusExistenceStructure2D structure = result.getStructure();
			assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness(), radius);
			int bands = 0;
			for (Coverage coverage : structure.getCoverage()) {
				if (coverage.getState() == CoverageState.UNRESOLVED) {
					bands++;
					assertEquals(LocusCoverageCause2D.UNLABELLED_COINCIDENT_OUTPUT,
							coverage.getCause(), radius);
					assertTrue(Math.abs(coverage.getInterval().getLower() - CRITICAL) < 1e-4
							&& Math.abs(coverage.getInterval().getUpper() - CRITICAL) < 1e-4,
							radius + " " + coverage);
				}
			}
			assertTrue(bands > 0, radius);
			assertEquals(CoverageState.CERTIFIED_EXISTS,
					coverageAt(structure, CRITICAL - 0.01).getState(), radius);
			assertNotEquals(CoverageState.CERTIFIED_EXISTS,
					coverageAt(structure, CRITICAL).getState(), radius);
			ContinuousComponent arc = structure.getContinuousValidComponents().get(0);
			assertEquals(1, structure.getContinuousValidComponents().size(), radius);
			assertEquals(Maximality.LOCAL_CERTIFIED_SUBINTERVAL, arc.getMaximality());
			assertEquals(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
					arc.getLowerBoundary().getKind());
			assertEquals(LocusBoundary2D.Kind.UNRESOLVED_BAND_EDGE,
					arc.getUpperBoundary().getKind());
			assertSame(arc, structure.getComponentContaining(CRITICAL + 0.2));
			assertSame(arc, structure.getComponentContaining(CRITICAL - 0.2));
			assertNull(structure.getComponentContaining(CRITICAL));
			for (CertifiedRoot root : result.getRoots()) {
				assertTrue(root.isInsideBand(), root.toString());
			}
			assertNotNull(result.getExploration());
			assertEquals(LocusEvidenceNature2D.EXPLORATORY,
					result.getExploration().getNature());
			LocusDefinition2D definition = locus("lr2").getSemanticDefinition();
			GeoPoint driver = (GeoPoint) lookup("N");
			GeoPoint live = (GeoPoint) lookup("R2");
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
				for (int sample = -100; sample <= 100; sample++) {
					double parameter = CRITICAL + sample * 1e-10;
					moveDriver(driver, parameter);
					boolean copy = definition.evaluate(BRANCH, parameter, session).isValid();
					CoverageState state = coverageAt(structure, parameter).getState();
					if (live.isDefined() != copy) {
						// The certificate states nothing where the live point and the copy differ.
						assertEquals(CoverageState.UNRESOLVED, state, "t=" + parameter);
					} else if (!copy) {
						assertNotEquals(CoverageState.CERTIFIED_EXISTS, state, "t=" + parameter);
					}
				}
			}
			getKernel().clearConstruction(true);
		}
	}

	/**
	 * V-TR-2: end inclusion follows the predicate: closed at a ray parameter, a segment
	 * parameter and a discriminant root, open at a vanishing divisor, whose inherited
	 * finiteness test leaves an unresolved band around the root.
	 */
	@Test
	void endInclusionFollowsThePredicate() {
		add("cc=Circle((0,0),5)");
		add("d=Circle((3,0),3)");
		add("A=Point(d)");
		moveDriver((GeoPoint) lookup("A"), 0);
		add("B=(-10,0)");
		add("r=Ray(A,B)");
		add("s=Segment(A,B)");
		add("Y=Intersect(cc,r,1)");
		add("Ys=Intersect(cc,s,1)");
		getKernel().updateConstruction(false);
		add("ly=LocusV2(Y,A)");
		add("lys=LocusV2(Ys,A)");
		getKernel().updateConstruction(false);
		assertClosedTransitions(certificate("ly"), "RAY_PARAMETER-member-0");
		assertClosedTransitions(certificate("lys"), "SEGMENT_PARAMETER-member-0");
		getKernel().clearConstruction(true);
		critical("40+2^(-10)");
		assertClosedTransitions(certificate("lq"), "DISCRIMINANT");
		getKernel().clearConstruction(true);
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("g=Line(N,(5,0))");
		add("S=Intersect(g,Line((0,-10),(1,-10)))");
		add("ls=LocusV2(S,N)");
		Result divisor = certificate("ls");
		assertEquals(Completeness.NOT_ESTABLISHED, divisor.getStructure().getCompleteness());
		List<CertifiedRoot> roots = divisor.getRoots();
		assertEquals(2, roots.size(), roots.toString());
		for (CertifiedRoot root : roots) {
			assertEquals(RootKind.OPEN_EVENT, root.getKind());
			assertFalse(root.isIncluded(), "a vanishing divisor is an open end");
			assertTrue(root.isInsideBand());
			assertEquals(LocusEvidenceNature2D.EXACT, root.getNature());
		}
		assertEnclosed(0, roots.get(1).getLower(), roots.get(1).getUpper());
		assertEquals(CoverageState.UNRESOLVED,
				coverageAt(divisor.getStructure(), 0).getState());
		for (ContinuousComponent component : divisor.getStructure()
				.getContinuousValidComponents()) {
			assertEquals(Maximality.LOCAL_CERTIFIED_SUBINTERVAL, component.getMaximality());
		}
	}

	/**
	 * V-COMPL-1: a box budget exhausted before the domain is covered never gives a complete
	 * certificate, even though every explored box is valid; the uncovered region is explicit.
	 */
	@Test
	void anExhaustedBudgetIsNeverComplete() {
		critical("39");
		LocusDefinition2D definition = locus("lq").getSemanticDefinition();
		ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(definition);
		Result complete = HybridExistenceCertifier2D.certify(model,
				definition.getBranch(BRANCH).getDeclaredDriverDomain(), true, BRANCH, null,
				Policy.DEFAULT, null);
		assertEquals(Completeness.COMPLETE, complete.getStructure().getCompleteness());
		Result exhausted = HybridExistenceCertifier2D.certify(model,
				definition.getBranch(BRANCH).getDeclaredDriverDomain(), true, BRANCH, null,
				Policy.DEFAULT.withBoxBudget(16), null);
		LocusExistenceStructure2D structure = exhausted.getStructure();
		assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness());
		assertEquals(16, exhausted.getExistenceBoxes());
		boolean uncovered = false;
		for (Coverage coverage : structure.getCoverage()) {
			assertNotEquals(CoverageState.CERTIFIED_DOES_NOT_EXIST, coverage.getState());
			uncovered |= coverage.getCause() == LocusCoverageCause2D.BUDGET_EXHAUSTED;
		}
		assertTrue(uncovered);
		assertNotEquals(complete.getPolicySignature(), exhausted.getPolicySignature());
	}

	/**
	 * V-LVL-1, V-V3-3 and V-PER-2: a conic-conic intersection, a reference-point intersection
	 * and a random value that depends on the driver are outside class v3: the certificate is
	 * not established, has no component, claims no closure, and only exploratory evidence
	 * describes the locus.
	 */
	@Test
	void outOfClassLociHaveOnlyExploratoryEvidence() {
		add("e1=Ellipse((0,0),(4,0),5)");
		add("c=Circle((0,0),3)");
		add("N=Point(c)");
		add("k=Circle(N,5)");
		add("W=Intersect(e1,k,1)");
		add("lw=LocusV2(W,N)");
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45),40)");
		add("M=Point(g1)");
		add("t=Ray(P,M)");
		add("Rz=Intersect(p,t,(10,10))");
		add("lz=LocusV2(Rz,M)");
		add("X=N+(RandomUniform(0,1)*x(N),0)");
		add("lx=LocusV2(X,N)");
		getKernel().updateConstruction(false);
		for (String label : List.of("lw", "lz", "lx")) {
			Result result = certificate(label);
			LocusExistenceStructure2D structure = result.getStructure();
			assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness(), label);
			assertTrue(structure.getContinuousValidComponents().isEmpty(), label);
			assertFalse(structure.isClosedPeriodic(), label);
			assertEquals(LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS,
					structure.getCoverage().get(0).getCause(), label);
			assertNotNull(result.getExploration(), label);
			assertTrue(result.getExploration().getWork() > 0, label);
			assertFalse(result.getExploration().getNature().certifies());
			// Since step T11 the certificate is the published structure: never a fallback.
			LocusExistenceStructure2D published = locus(label).getSemanticDefinition()
					.getBranch(BRANCH).getExistenceStructure();
			assertEquals(HybridExistenceCertifier2D.CONTRACT,
					published.getCertifierContract(), label);
			assertEquals(structure.getCoverage().get(0).getEvidence(),
					published.getCoverage().get(0).getEvidence(), label);
			assertSame(result.getExploration(), locus(label).getExploratoryEvidence(BRANCH),
					label);
		}
	}

	/**
	 * V-LVL-1 (no authority over the graph or the identity): the exploratory evidence of
	 * {@code lw} recomputed with another exploratory policy (more samples, fewer bisections)
	 * differs, while the certificate it accompanies is identical bit for bit; and a revision of
	 * {@code lw} keeps its persistent identity, its dependencies, the signature of its
	 * definition and its component keys, none of which the evidence reaches. The document
	 * never stores the evidence.
	 */
	@Test
	void exploratoryEvidenceHasNoAuthorityOverTheDefinition() {
		add("e1=Ellipse((0,0),(4,0),5)");
		add("c=Circle((0,0),3)");
		add("N=Point(c)");
		add("k=Circle(N,5)");
		add("W=Intersect(e1,k,1)");
		add("lw=LocusV2(W,N)");
		getKernel().updateConstruction(false);
		GeoLocusV2 locus = locus("lw");
		Result published = certificate("lw");
		LocusDefinition2D definition = locus.getSemanticDefinition();
		ReconstructibleLocusEvaluator2D evaluator =
				(ReconstructibleLocusEvaluator2D) definition.getEvaluatorCapability();
		Coverage unresolved = published.getStructure().getCoverage().get(0);
		assertTrue(unresolved.getEvidence().startsWith("hybrid/v1:"), unresolved.getEvidence());
		String reason = unresolved.getEvidence().substring("hybrid/v1:".length());
		Map<String, Result> results = new LinkedHashMap<>();
		for (Policy policy : List.of(Policy.DEFAULT, Policy.DEFAULT.withExploration(96, 6))) {
			try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(64)) {
				results.put(policy.getSignature(), HybridExistenceCertifier2D.notEstablished(
						published.getStructure().getCanonicalDomain(), true, BRANCH,
						unresolved.getCause(), reason, parameter -> evaluator.evaluate(null, null,
								parameter, session).isValid(), policy, null));
			}
		}
		List<Result> recertified = new ArrayList<>(results.values());
		assertEquals(published.getStructure().getSemanticSignature(),
				recertified.get(0).getStructure().getSemanticSignature());
		assertEquals(recertified.get(0).getStructure().getSemanticSignature(),
				recertified.get(1).getStructure().getSemanticSignature());
		assertNotEquals(recertified.get(0).getExploration().getPolicyVersion(),
				recertified.get(1).getExploration().getPolicyVersion());
		assertNotEquals(recertified.get(0).getExploration().getWork(),
				recertified.get(1).getExploration().getWork());
		assertFalse(recertified.get(1).getExploration().getNature().certifies());

		// Nothing that identifies or defines the locus carries the evidence.
		PersistentGeoId identity = locus.getPersistentLocusId();
		String signature = definition.getBranch(BRANCH).getSemanticSignature();
		List<GeoElement> inputs = List.of(locus.getParentAlgorithm().getInput());
		for (String part : List.of(published.getExploration().getPolicyVersion(),
				published.getExploration().getMethod())) {
			assertFalse(signature.contains(part), part);
			assertFalse(evaluator.getEvaluatorSignature().contains(part), part);
			assertFalse(getApp().getXML().contains(part), part);
		}
		getKernel().updateConstruction(false);
		assertEquals(identity, locus("lw").getPersistentLocusId());
		assertEquals(inputs, List.of(locus("lw").getParentAlgorithm().getInput()));
		assertEquals(signature, locus("lw").getSemanticDefinition().getBranch(BRANCH)
				.getSemanticSignature());
		assertEquals(LocusComponentLineage2D.keys(published.getStructure()),
				LocusComponentLineage2D.keys(certificate("lw").getStructure()));
	}

	/**
	 * V-V3-4, existence part: a slice above the versioned step limit is not established, and
	 * the refusal is typed.
	 */
	@Test
	void theStepLimitRefusalIsTyped() {
		add("c=Circle((0,0),3)");
		add("N=Point(c)");
		add("v=Vector((0.001,0))");
		String previous = "N";
		for (int index = 1; index <= CertifiedConstructionProgram2D.MAXIMUM_STEPS_V3 + 1;
				index++) {
			add("T" + index + "=Translate(" + previous + ",v)");
			previous = "T" + index;
		}
		add("outside=LocusV2(" + previous + ",N)");
		LocusExistenceStructure2D structure = certificate("outside").getStructure();
		assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness());
		Coverage coverage = structure.getCoverage().get(0);
		assertEquals(LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS, coverage.getCause());
		assertTrue(coverage.getEvidence().endsWith("step-limit-exceeded"),
				coverage.getEvidence());
	}

	/**
	 * V-CRIT-3: with exact rational data the certificate separates roots {@code 2^-40} and
	 * {@code 2^-100} apart into disjoint enclosures, or reports its exhausted budget; it never
	 * merges them into one root.
	 */
	@Test
	void exactCertificatesSeparateArbitrarilyCloseRoots() {
		for (int k : new int[] {80, 200}) {
			ExactRational2D radius = ExactRational2D.of(40).add(ExactRational2D.of(
					BigInteger.ONE, BigInteger.ONE.shiftLeft(k)));
			TrigonometricPolynomial2D discriminant = criticalDiscriminant(radius);
			try {
				CircleDriverRootStructure2D structure = CircleDriverRootStructure2D.classify(
						discriminant, ExactRational2D.ofBinary64(DELTA / 4),
						ExactBudget1D.DEFAULT);
				List<CircleDriverRootStructure2D.Root> roots = structure.roots();
				assertEquals(2, roots.size(), "k=" + k);
				for (CircleDriverRootStructure2D.Root root : roots) {
					assertEquals(1, root.multiplicity());
				}
				assertTrue(roots.get(0).uUpper().compareTo(roots.get(1).uLower()) < 0,
						"disjoint isolating intervals for k=" + k);
			} catch (ExactBudgetExhaustedException exhausted) {
				// A typed exhaustion decides nothing and is acceptable.
				assertNotNull(exhausted.getMessage());
			}
		}
	}

	/**
	 * V-CRIT-10: certificates, exact evidence and roots are identical bit for bit on a new
	 * construction with the same state. Since step T8 a boundary source names its operation
	 * by the durable identity of its geo, which a new construction allocates anew; those
	 * identities are the only part compared up to renaming.
	 */
	@Test
	void certificatesAreDeterministic() {
		List<String> first = signatures();
		getKernel().clearConstruction(true);
		assertEquals(withoutDurableIdentities(first),
				withoutDurableIdentities(signatures()));
		assertTrue(first.stream().anyMatch(text -> text.contains("geo:")),
				"the sources name durable operation identities");
	}

	private static List<String> withoutDurableIdentities(List<String> signatures) {
		List<String> result = new ArrayList<>();
		for (String signature : signatures) {
			result.add(signature.replaceAll("geo:[0-9a-f]{32}", "geo:*"));
		}
		return result;
	}

	/**
	 * V-DET-1 (synthetic constructions of the matrix; corrective descendant T13-R4): two runs,
	 * each on a new document loaded from the same saved construction and taken through the
	 * same changes, then undone, redone and reopened, give results identical bit for bit at
	 * every step: the semantic revision, the slice fingerprint and the published branch of
	 * every locus (coverage, components with their ends, brackets and sources, singular
	 * parameters, point components, folds and the branch lineage), its roots, exact evidence,
	 * stationary points and box count, its lineage keys and transitions, the events of its
	 * slice (settlement outcome, fidelity report with its departures, captured selection
	 * rule), every image of a locus, every point of a locus and the structure of every metric
	 * with its events. The saved document carries the durable identities, so nothing is
	 * normalized. The constructions are the critical radius with labelled and unlabelled
	 * outputs, started at the double root in its first initialization and taken off it, and
	 * with similarity images; its penetration taken to a bite and back (split and merge) with a
	 * point of the locus selected by its parameter; a copy rebuilt with the driver in a gap and
	 * settled; the external tangency taken from an empty cover to a point, an arc and the point
	 * again (changes of dimension), its images by the similarities with their transformed
	 * position enclosures and a semantic point at its tangency with its typed cause (corrective
	 * mandate), two point components, the band of an unlabelled output, a
	 * fold, an origin wrap, the ends of a ray, a segment and a vanishing divisor, a structurally
	 * total locus, a locus outside the class and the bite started with the driver in its gap. A
	 * random value is not deterministic by definition and is left out.
	 */
	@Test
	void twoRunsOfTheSyntheticConstructionsGiveIdenticalResults() {
		Map<String, Runnable> builders = new LinkedHashMap<>();
		Map<String, List<Change>> changes = new LinkedHashMap<>();
		builders.put("critical", () -> critical("40"));
		builders.put("split and merge", () -> {
			add("rr=40-2^(-10)");
			critical("rr");
			// A point of the locus selected by its parameter (component selectors).
			add("v=0.3");
			add("A=Point(lq,\"" + BRANCH + "\",v)");
		});
		changes.put("split and merge", List.of(Change.radius(40 + Math.scalb(1.0, -10)),
				Change.radius(40 + Math.scalb(1.0, -4)), Change.radius(40 - Math.scalb(1.0, -10))));
		builders.put("first initialization at the double root", () -> {
			critical("40");
			moveDriver((GeoPoint) lookup("N"), CRITICAL);
		});
		changes.put("first initialization at the double root",
				List.of(Change.driver(CRITICAL + 0.1), Change.driver(CRITICAL + 0.2)));
		builders.put("settlement in a gap", () -> {
			add("rr=0");
			add("C=(0,rr)");
			add("k=Circle(C,5)");
			add("g1=Circle((0,0),20)");
			add("N=Point(g1)");
			add("m=Line(N,N+(1,0))");
			moveDriver((GeoPoint) lookup("N"), 0);
			getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"S", "T"},
					(GeoLine) lookup("m"), (GeoConic) lookup("k"));
			moveDriver((GeoPoint) lookup("N"), 1e-4);
			add("ls=LocusV2(S,N)");
		});
		changes.put("settlement in a gap", List.of(Change.driver(Math.PI / 2),
				Change.radius(0.5), Change.radius(0.6)));
		builders.put("similarity images", () -> {
			add("rr=40");
			critical("rr");
			add("img=Translate(lq,(1,0))");
			add("dil=Dilate(lr,2,(0,0))");
		});
		changes.put("similarity images", List.of(Change.radius(40 + Math.scalb(1.0, -10))));
		builders.put("change of dimension", () -> {
			add("rr=40-2^(-30)");
			add("p=Circle((0,0),25)");
			add("P=(-25,50)");
			add("g1=Circle((83,19),rr)");
			add("N=Point(g1)");
			moveDriver((GeoPoint) lookup("N"), EXTERNAL + Math.PI);
			add("t=Ray(P,N)");
			getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
					(GeoLine) lookup("t"), (GeoConic) lookup("p"));
			add("lq=LocusV2(Q,N)");
			getKernel().updateConstruction(false);
		});
		changes.put("change of dimension", List.of(Change.radius(40.0),
				Change.radius(40 + Math.scalb(1.0, -30)), Change.radius(40.0)));
		builders.put("point components", () -> tangency("(15,70)", "Line"));
		builders.put("images and a point of a point component", () -> {
			tangency("(83,19)", "Ray");
			// Before the images, whose literal arguments take the first free labels.
			GeoNumeric parameter = new GeoNumeric(getConstruction(), EXTERNAL);
			parameter.setLabel("tStar");
			add("A=Point(lq,\"" + BRANCH + "\",tStar)");
			add("it=Translate(lq,Vector((1,0)))");
			add("ir=Rotate(lq,pi/2,(3,4))");
			add("il=Mirror(lq,Line((0,1),(1,3)))");
			add("id=Dilate(lq,-3,(2,-1))");
		});
		builders.put("band", () -> critical("40+2^(-30)"));
		builders.put("fold", () -> {
			add("c=Circle((0,0),2)");
			add("N=Point(c)");
			add("X=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
			add("lx=LocusV2(X,N)");
		});
		builders.put("origin wrap", () -> {
			add("c=Circle((0,0),2)");
			add("N=Point(c)");
			add("K=N+(1,0)");
			add("o=LocusV2(K,N)");
			add("v=0");
			add("A=Point(o,\"" + BRANCH + "\",v)");
			add("w=LocusLength(o,A,K)");
			add("d=Length(o,A,K)");
			add("R=N-d*(1,0)");
			add("e=LocusV2(R,N)");
		});
		builders.put("ray, segment and divisor ends", () -> {
			add("cc=Circle((0,0),5)");
			add("d=Circle((3,0),3)");
			add("A=Point(d)");
			moveDriver((GeoPoint) lookup("A"), 0);
			add("B=(-10,0)");
			add("r=Ray(A,B)");
			add("s=Segment(A,B)");
			add("Y=Intersect(cc,r,1)");
			add("Ys=Intersect(cc,s,1)");
			add("ly=LocusV2(Y,A)");
			add("lys=LocusV2(Ys,A)");
			add("c=Circle((0,0),2)");
			add("N=Point(c)");
			add("g=Line(N,(5,0))");
			add("S=Intersect(g,Line((0,-10),(1,-10)))");
			add("ls=LocusV2(S,N)");
		});
		builders.put("totality and outside the class", () -> {
			add("c=Circle((0,0),2)");
			add("N=Point(c)");
			add("M=Midpoint(N,(3,4))");
			add("lm=LocusV2(M,N)");
			add("e1=Ellipse((0,0),(4,0),5)");
			add("k=Circle(N,5)");
			add("W=Intersect(e1,k,1)");
			add("lw=LocusV2(W,N)");
		});
		builders.put("started in the gap", () -> {
			critical("40+2^(-10)");
			moveDriver((GeoPoint) lookup("N"), CRITICAL);
		});
		StringBuilder compared = new StringBuilder();
		for (Map.Entry<String, Runnable> builder : builders.entrySet()) {
			builder.getValue().run();
			String document = getApp().getXML();
			getKernel().clearConstruction(true);
			List<Change> steps = changes.getOrDefault(builder.getKey(), List.of());
			String first = run(document, steps);
			assertEquals(first, run(document, steps), builder.getKey());
			compared.append(first);
		}
		for (String part : List.of("geo:", "|coverage=", "|component=", "|singular=",
				"|point=", "|fold=", "TRANSITION[", "OPEN_EVENT[", "|coefficients=",
				"ORIGIN_WRAP", "OPERATION_OUTSIDE_CLASS", "UNLABELLED_COINCIDENT_OUTPUT",
				"SPLIT[", "MERGED[", "DIMENSION_CHANGED[", "settlement=settled",
				"2LINE_CIRCLE[0x1.0p0", " image revision=", "A point=", "undo 0", "redo 0",
				"reopened", "cause=RESOLVED", "cause=POINT_COMPONENT")) {
			assertTrue(compared.indexOf(part) >= 0, "the runs compare " + part);
		}
		for (String event : List.of("SELECTION_SETTLEMENT=[1-9]", "SELECTION_DEPARTURE=[1-9]",
				"CERTIFICATE_COMPUTATION=[1-9]")) {
			assertTrue(Pattern.compile(event).matcher(compared).find(),
					"the runs compare the event " + event);
		}
	}

	/**
	 * One run of V-DET-1: a new document loaded from the saved construction, then each change
	 * in turn, every change undone and redone, and the document reopened; the results after
	 * loading and after each operation.
	 */
	private static String run(String document, List<Change> changes) {
		AppCommon app = AppCommonFactory.create(new AppConfigGeoCeDG(true));
		app.setXML(document, true);
		app.setUndoRedoMode(UndoRedoMode.GUI);
		app.setUndoActive(true);
		app.storeUndoInfo();
		StringBuilder text = new StringBuilder(results(app, "loaded"));
		for (Change change : changes) {
			change.action.accept(app);
			app.storeUndoInfo();
			text.append(results(app, change.name));
		}
		for (int step = 0; step < changes.size(); step++) {
			app.getKernel().undo();
			text.append(results(app, "undo " + step));
		}
		for (int step = 0; step < changes.size(); step++) {
			app.getKernel().redo();
			text.append(results(app, "redo " + step));
		}
		app.setXML(app.getXML(), true);
		return text.append(results(app, "reopened")).toString();
	}

	/** A named change of a run of V-DET-1. */
	private static final class Change {
		private final String name;
		private final Consumer<AppCommon> action;

		private Change(String name, Consumer<AppCommon> action) {
			this.name = name;
			this.action = action;
		}

		/** The free radius {@code rr} set to the value. */
		static Change radius(double value) {
			return new Change("rr=" + Double.toHexString(value), app -> {
				GeoNumeric number = (GeoNumeric) app.getKernel().lookupLabel("rr");
				number.setValue(value);
				number.updateCascade();
				app.getKernel().updateConstruction(false);
			});
		}

		/** The driver {@code N} moved to the parameter. */
		static Change driver(double parameter) {
			return new Change("N at " + Double.toHexString(parameter), app -> moveDriver(
					(GeoPoint) app.getKernel().lookupLabel("N"), parameter));
		}
	}

	/** @return every result of the document that a second run must reproduce bit for bit */
	private static String results(AppCommon app, String step) {
		StringBuilder text = new StringBuilder("step ").append(step).append('\n');
		for (GeoElement geo : app.getKernel().getConstruction().getGeoSetConstructionOrder()) {
			if (geo.getParentAlgorithm() instanceof AlgoDependentPointLocusV2) {
				AlgoDependentPointLocusV2 algorithm =
						(AlgoDependentPointLocusV2) geo.getParentAlgorithm();
				GeoLocusV2 locus = (GeoLocusV2) geo;
				LocusBranch2D branch = locus.getSemanticDefinition() == null ? null
						: locus.getSemanticDefinition().getBranch(BRANCH);
				text.append(geo.getLabelSimple()).append(" revision=")
						.append(locus.getSemanticRevision()).append(" fingerprint=")
						.append(ReconstructionSliceFingerprint2D.compute(
								algorithm.getReconstructionSliceGeos(), List.of(
										sliceRoot(algorithm, "state"),
										sliceRoot(algorithm, "coordinate"))))
						.append("\n branch=").append(branch == null ? "none"
								: branch.getSemanticSignature());
				Result result = algorithm.getHybridExistenceCertificate();
				if (result != null) {
					text.append("\n roots=").append(result.getRoots().stream()
							.map(CertifiedRoot::getSignature).collect(Collectors.joining(";")))
							.append("\n exact=").append(result.getExactEvidence().stream()
									.map(ExactEvidence::getSignature)
									.collect(Collectors.joining(";")))
							.append("\n stationary=").append(result.getStationaryPoints().stream()
									.map(HybridExistenceCertifier2D.CertifiedStationaryPoint
											::getSignature)
									.collect(Collectors.joining(";")))
							.append("\n boxes=").append(result.getExistenceBoxes())
							.append("\n keys=").append(LocusComponentLineage2D.keys(
									result.getStructure()))
							.append("\n lineage=").append(algorithm.getHybridLineage());
				}
				if (locus.getSemanticDefinition() != null
						&& locus.getSemanticDefinition().getEvaluatorCapability()
								instanceof ReconstructibleLocusEvaluator2D) {
					// The events of the slice: settlement, departures and the captured rule.
					ReconstructibleLocusEvaluator2D evaluator = (ReconstructibleLocusEvaluator2D)
							locus.getSemanticDefinition().getEvaluatorCapability();
					text.append("\n settlement=").append(evaluator.getSettlementOutcome()
							.getToken()).append(" fidelity=").append(evaluator.getFidelityReport())
							.append("\n rule=").append(evaluator.captureCertifiedProgramV3()
									.map(CertifiedConstructionProgram2D::getSelectionSignature)
									.orElse("refused " + evaluator.getProgramV3Refusal()));
				}
				// The work of certification counts its events: settlements, departures.
				text.append("\n work=");
				for (LocusCertificationWork2D kind : LocusCertificationWork2D.values()) {
					text.append(kind.name()).append('=').append(locus.getInstrumentation()
							.getCertificationWork(kind)).append(' ');
				}
				text.append('\n');
			} else if (geo instanceof GeoLocusV2) {
				// An image of a locus, by a similarity or another construction on it.
				GeoLocusV2 image = (GeoLocusV2) geo;
				text.append(geo.getLabelSimple()).append(" image revision=")
						.append(image.getSemanticRevision());
				if (image.getSemanticDefinition() != null) {
					for (LocusBranch2D branch : image.getSemanticDefinition().getBranches()) {
						text.append("\n branch=").append(branch.getSemanticSignature());
					}
				}
				text.append('\n');
			} else if (geo.getParentAlgorithm() instanceof AlgoSemanticLocusPoint2D) {
				GeoPoint point = (GeoPoint) geo;
				AlgoSemanticLocusPoint2D parent =
						(AlgoSemanticLocusPoint2D) geo.getParentAlgorithm();
				text.append(geo.getLabelSimple()).append(" point=").append(point.isDefined()
						? Double.toHexString(point.getInhomX()) + ","
								+ Double.toHexString(point.getInhomY()) : "undefined")
						.append(" cause=").append(parent.getResolutionCause())
						.append(" selector=").append(parent.getSelectorStatus()).append('\n');
			} else if (geo.getParentAlgorithm() instanceof AlgoLocusBetweenMetricV2) {
				text.append(geo.getLabelSimple()).append(" metric=").append(
						((AlgoLocusBetweenMetricV2) geo.getParentAlgorithm())
								.getFunctionStructure().getSignature()).append('\n');
			}
		}
		return text.toString();
	}

	/** @return the driver state or coordinate that the slice of a locus starts from */
	private static GeoElement sliceRoot(AlgoDependentPointLocusV2 algorithm, String name) {
		try {
			java.lang.reflect.Field field = AlgoDependentPointLocusV2.class
					.getDeclaredField(name);
			field.setAccessible(true);
			return (GeoElement) field.get(algorithm);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	/**
	 * V-INV-2 (corrective descendant T13-R4): with every constant a multiple of 125, the
	 * critical radius, its bite and penetration at {@code 40 +- 2^-10}, the external
	 * tangency and the line of the two point components of matrix section 1 are
	 * translated by {@code 10^3} and scaled from {@code 10^-3} to {@code 10^3} as a whole,
	 * exactly in binary64: the typed inputs and the captured constants are the exact
	 * transformed constants, bit for bit. The structure is the same: completeness, coverage
	 * states and causes, components with their maximality, ends and boundary sources
	 * (operation, predicate and side), singular parameters and point components; the durable
	 * operation identities of different constructions are compared through the labels of their
	 * geos. The brackets contain the transformed references, proved with 60-digit arithmetic
	 * on the captured constants: {@code atan2(4, 3)} and {@code atan2(-4, -3)} by the monotony
	 * of the cosine, the seam by 60 digits of pi, the simple roots of the bite by a sign change
	 * of the discriminant; the position of each point component encloses the transformed
	 * point (15, 20) or, at the seam, (-25, 0), and every certified piece agrees with the
	 * discriminant at its samples.
	 */
	@Test
	void translationsAndScalesOfTheWholeConstructionKeepTheStructure() {
		Map<Configuration, Map<String, String>> reference = referenceDescriptors();
		List<Transform> transforms = new ArrayList<>();
		transforms.add(Transform.of(UNIT, "1", "1000", "1000"));
		transforms.add(Transform.of(UNIT, "1", "-1000", "1000"));
		for (String scale : List.of("0.001", "0.01", "0.1", "10", "100", "1000")) {
			transforms.add(Transform.of(UNIT, scale, "0", "0"));
		}
		transforms.add(Transform.of(UNIT, "0.001", "1000", "1000"));
		transforms.add(Transform.of(UNIT, "1000", "-1000", "1000"));
		List<String> problems = new ArrayList<>();
		for (Transform transform : transforms) {
			for (Configuration configuration : Configuration.values()) {
				String context = configuration + " " + transform;
				build(configuration, transform);
				assertExact(context, configuration, transform, inputs());
				double[] constants = captured();
				assertCapturedExact(context, configuration, transform, constants);
				for (String label : configuration.loci) {
					assertEquals(Completeness.COMPLETE, structure(label).getCompleteness(),
							context + " " + label);
					assertEquals(reference.get(configuration).get(label), descriptor(label),
							context + " " + label);
					references(context + " " + label, label, configuration, constants,
							transform, problems);
				}
				getKernel().clearConstruction(true);
			}
		}
		assertEquals(List.of(), problems);
	}

	/**
	 * V-INV-2, captured constants: in the units of matrix section 1, scaled by {@code 10^-3}
	 * or translated by {@code (1000.1, -999.7)}, the constants are rounded when they are
	 * captured, and the exact certificate classifies them as captured. The test reads the
	 * captured constants back, builds their discriminant with exact rationals and classifies
	 * its roots; the certificate has exactly the transitions, singular parameters and point
	 * components of that classification, each transition bracket shows a sign change of the
	 * 60-digit discriminant of the captured constants, and every certified piece agrees with
	 * that discriminant at its samples. Independently of any classification, the 60-digit
	 * discriminant changes sign at 4096 samples only where the certificate changes state, and
	 * shows a double root of the right side in every singular parameter and point component.
	 * The bite and the penetration, whose margin is far above the rounding, keep their
	 * structure.
	 */
	@Test
	void theExactCertificateClassifiesTheTransformedConstantsAsCaptured() {
		Map<Configuration, Map<String, String>> reference = referenceDescriptors();
		List<String> problems = new ArrayList<>();
		int changes = 0;
		for (Transform transform : List.of(Transform.of(BigDecimal.ONE, "0.001", "0", "0"),
				Transform.of(BigDecimal.ONE, "1", "1000.1", "-999.7"))) {
			for (Configuration configuration : Configuration.values()) {
				String context = configuration + " " + transform;
				build(configuration, transform);
				double[] constants = captured();
				List<CircleDriverRootStructure2D.Root> roots = classify(constants);
				for (String label : configuration.loci) {
					LocusExistenceStructure2D structure = structure(label);
					assertEquals(Completeness.COMPLETE, structure.getCompleteness(),
							context + " " + label);
					long ends = structure.getContinuousValidComponents().stream()
							.flatMap(component -> java.util.stream.Stream.of(
									component.getLowerBoundary(), component.getUpperBoundary()))
							.filter(end -> end.getKind()
									== LocusBoundary2D.Kind.CERTIFIED_TRANSITION)
							.count();
					assertEquals(count(roots, RealRootStructure1D.RootKind.TRANSITION), ends,
							context + " " + label + roots);
					assertEquals(count(roots, RealRootStructure1D.RootKind.SINGULAR_INTERIOR),
							structure.getSingularParameters().size(), context + " " + label);
					assertEquals(count(roots, RealRootStructure1D.RootKind.ISOLATED_POINT),
							structure.getPointComponents().size(), context + " " + label);
					references(context + " " + label, label, configuration, constants, null,
							problems);
					changes += signChangesAreCertified(context + " " + label, label,
							configuration, constants, problems);
					if (configuration == Configuration.BITE
							|| configuration == Configuration.PENETRATION) {
						assertEquals(reference.get(configuration).get(label), descriptor(label),
								context + " " + label);
					}
				}
				getKernel().clearConstruction(true);
			}
		}
		assertEquals(List.of(), problems);
		assertTrue(changes > 0, "sign changes of the discriminant found: " + changes);
	}

	/**
	 * V-INV-2, extreme cases: translated by {@code 10^6} or scaled by {@code 10^6}, exactly
	 * in binary64, a certificate may fail closed ({@code L-CU1-TRANSLATION-RESOLUTION}); a
	 * complete one has the structure of the untransformed configuration, and whatever a
	 * certificate states, complete or not, agrees with the 60-digit reference: every
	 * certified piece at its samples, every transition bracket by a sign change, and every
	 * singular parameter and point component by its enclosure of the transformed reference.
	 */
	@Test
	void transformationsOfOrderTenToTheSixFailClosedOrKeepTheStructure() {
		Map<Configuration, Map<String, String>> reference = referenceDescriptors();
		List<String> problems = new ArrayList<>();
		for (Transform transform : List.of(Transform.of(UNIT, "1", "1000000", "1000000"),
				Transform.of(UNIT, "1000000", "0", "0"))) {
			for (Configuration configuration : Configuration.values()) {
				String context = configuration + " " + transform;
				build(configuration, transform);
				assertExact(context, configuration, transform, inputs());
				double[] constants = captured();
				assertCapturedExact(context, configuration, transform, constants);
				for (String label : configuration.loci) {
					if (structure(label).getCompleteness() == Completeness.COMPLETE) {
						assertEquals(reference.get(configuration).get(label), descriptor(label),
								context + " " + label);
					}
					references(context + " " + label, label, configuration, constants,
							transform, problems);
				}
				getKernel().clearConstruction(true);
			}
		}
		assertEquals(List.of(), problems);
	}

	/**
	 * V-INV-2 for the unlabelled second output (decision D-CU2-T13-R7, precision B(i); finding
	 * H-T13-B): the critical, bite and penetration configurations traced through the second
	 * output of {@code Intersect(p, t2, 2)}, whose inherited suppression the band of
	 * {@link InheritedLineCircleBand2D} encloses. The original transformations in the units of
	 * matrix section 1 are mandatory and those with every constant a multiple of 125
	 * complementary; the scale {@code 10^-3} with the translation (10^3, 10^3) in the units of
	 * the matrix is the case of
	 * {@link #theUnlabelledSecondOutputFailsClosedWhereTheInheritedComputationIsNotDecisive}.
	 * Where the captured constants keep the kinds of the roots of the untransformed
	 * discriminant the certificate keeps its structure; every statement agrees with the
	 * 60-digit reference of the captured constants and every band has the typed cause of the
	 * inherited suppression. With every constant a multiple of 125 the bands stay within a
	 * width of {@code 10^-4}, instead of growing with the distance of the line from the
	 * origin.
	 */
	@Test
	void theUnlabelledSecondOutputKeepsItsStructureUnderTheTransforms() {
		Map<Configuration, List<RealRootStructure1D.RootKind>> kinds = new LinkedHashMap<>();
		final Map<Configuration, String> reference = unlabelledReferences(kinds);
		List<Transform> transforms = new ArrayList<>();
		for (BigDecimal unit : List.of(BigDecimal.ONE, UNIT)) {
			transforms.add(Transform.of(unit, "1", "1000", "1000"));
			transforms.add(Transform.of(unit, "1", "-1000", "1000"));
			for (String scale : List.of("0.001", "0.01", "0.1", "10", "100", "1000")) {
				transforms.add(Transform.of(unit, scale, "0", "0"));
			}
			transforms.add(Transform.of(unit, "1000", "-1000", "1000"));
		}
		transforms.add(Transform.of(UNIT, "0.001", "1000", "1000"));
		transforms.add(Transform.of(UNIT, "1", "1000000", "1000000"));
		transforms.add(Transform.of(UNIT, "1000000", "0", "0"));
		transforms.add(Transform.of(BigDecimal.ONE, "0.001953125", "1024", "1024"));
		transforms.add(Transform.of(BigDecimal.ONE, "0.01", "1000", "1000"));
		transforms.add(Transform.of(BigDecimal.ONE, "1", "1000.1", "-999.7"));
		List<String> problems = new ArrayList<>();
		int kept = 0;
		for (Transform transform : transforms) {
			for (Configuration configuration : UNLABELLED) {
				String context = configuration + " " + transform + " lr2";
				buildUnlabelled(configuration, transform);
				double[] constants = captured("lr2");
				LocusExistenceStructure2D structure = structure("lr2");
				double width = 0;
				for (Coverage coverage : structure.getCoverage()) {
					if (coverage.getState() == CoverageState.UNRESOLVED) {
						width += coverage.getInterval().getUpper()
								- coverage.getInterval().getLower();
						if (coverage.getCause()
								!= LocusCoverageCause2D.UNLABELLED_COINCIDENT_OUTPUT) {
							problems.add(context + ": a band without the typed cause " + coverage);
						}
					}
				}
				if (kinds(classify(constants)).equals(kinds.get(configuration))) {
					assertEquals(reference.get(configuration), descriptor("lr2"), context);
					kept++;
				}
				if (transform.unit.equals(UNIT) && width > 1e-4) {
					problems.add(context + ": bands of width " + width);
				}
				references(context, "lr2", configuration, constants, null, problems);
				getKernel().clearConstruction(true);
			}
		}
		assertEquals(List.of(), problems);
		// Rounded constants of the critical configuration may split its double root.
		assertTrue(kept >= 0.9 * UNLABELLED.size() * transforms.size(),
				"structures kept: " + kept);
	}

	/**
	 * V-INV-2 for the unlabelled second output, the mandatory transformation where the
	 * inherited computation is not decisive (decision D-CU2-T13-R7, precision B(i)): scaled by
	 * {@code 10^-3} and translated by (10^3, 10^3) in the units of matrix section 1, the live
	 * construction suppresses the output at parameters where two roots lie ahead of P (finding
	 * H-T13-B), so no correct certificate keeps the untransformed structure. The certificate
	 * fails closed with the typed cause of the inherited suppression, never with another
	 * certified structure, and states nothing that the 60-digit reference of the captured
	 * constants contradicts; {@link #everyCertifiedSampleOfTheUnlabelledSecondOutputIsDefined}
	 * covers what it certifies.
	 */
	@Test
	void theUnlabelledSecondOutputFailsClosedWhereTheInheritedComputationIsNotDecisive() {
		Transform transform = Transform.of(BigDecimal.ONE, "0.001", "1000", "1000");
		List<String> problems = new ArrayList<>();
		for (Configuration configuration : UNLABELLED) {
			String context = configuration + " " + transform + " lr2";
			buildUnlabelled(configuration, transform);
			double[] constants = captured("lr2");
			LocusExistenceStructure2D structure = structure("lr2");
			assertEquals(Completeness.NOT_ESTABLISHED, structure.getCompleteness(), context);
			for (Coverage coverage : structure.getCoverage()) {
				if (coverage.getState() == CoverageState.UNRESOLVED) {
					assertEquals(LocusCoverageCause2D.UNLABELLED_COINCIDENT_OUTPUT,
							coverage.getCause(), context);
				}
			}
			references(context, "lr2", configuration, constants, null, problems);
			GeoPoint driver = (GeoPoint) lookup("N");
			GeoPoint live = (GeoPoint) lookup("R2");
			int suppressed = 0;
			for (int sample = 0; sample < 2000; sample++) {
				double t = CRITICAL - 0.1 + 0.2 * (sample + 0.5) / 2000;
				BigDecimal[] value = discriminantAt(constants, t);
				if (value[0].signum() > 0 && value[1].signum() > 0) {
					moveDriver(driver, t);
					if (!live.isDefined()) {
						suppressed++;
					}
				}
			}
			assertTrue(suppressed > 0, context + ": no live suppression ahead of P");
			getKernel().clearConstruction(true);
		}
		assertEquals(List.of(), problems);
	}

	/**
	 * Finding H-T13-B, adversarial (decision D-CU2-T13-R7): wherever the certificate states
	 * that the unlabelled second output exists, the live construction defines it and the
	 * published evaluation is a point, at uniform samples, densely around the coincident root
	 * and at the binary64 neighbours of every band edge, under the transformations that change
	 * the inherited computation.
	 */
	@Test
	void everyCertifiedSampleOfTheUnlabelledSecondOutputIsDefined() {
		List<Transform> transforms = List.of(Transform.of(UNIT, "1", "0", "0"),
				Transform.of(BigDecimal.ONE, "1", "1000", "1000"),
				Transform.of(UNIT, "0.001", "1000", "1000"),
				Transform.of(BigDecimal.ONE, "0.001", "1000", "1000"),
				Transform.of(BigDecimal.ONE, "0.01", "1000", "1000"),
				Transform.of(BigDecimal.ONE, "0.001953125", "1024", "1024"),
				Transform.of(BigDecimal.ONE, "1", "1000.1", "-999.7"),
				Transform.of(UNIT, "1", "1000000", "1000000"),
				Transform.of(UNIT, "1000000", "0", "0"));
		List<String> problems = new ArrayList<>();
		int certified = 0;
		for (Transform transform : transforms) {
			for (Configuration configuration : UNLABELLED) {
				String context = configuration + " " + transform + " lr2";
				buildUnlabelled(configuration, transform);
				LocusExistenceStructure2D structure = structure("lr2");
				LocusDefinition2D definition = locus("lr2").getSemanticDefinition();
				GeoPoint driver = (GeoPoint) lookup("N");
				GeoPoint live = (GeoPoint) lookup("R2");
				try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
					for (double t : samples(structure)) {
						if (coverageAt(structure, t).getState() != CoverageState.CERTIFIED_EXISTS) {
							continue;
						}
						certified++;
						moveDriver(driver, t);
						if (!live.isDefined()
								|| !definition.evaluate(BRANCH, t, session).isValid()) {
							problems.add(context + ": certified and undefined at t=" + t);
						}
					}
				}
				getKernel().clearConstruction(true);
			}
		}
		assertTrue(certified > 0);
		assertEquals(List.of(), problems);
	}

	/**
	 * Decision D-CU2-T13-R7: the closure of a band of the inherited coincidence suppression at
	 * its band resolution only withholds certified existence. In the critical configuration
	 * with every constant a multiple of 125, translated by (10^3, 10^3), the margin of the band
	 * predicate near its edges is comparable to the rounding of the enclosures and valid cells
	 * alternate with band cells; the certificate closes them, counts the closed runs and their
	 * width, which is below the band resolution per run and, observed, far below. One band
	 * with the typed cause surrounds the coincident root, nothing near it is certified
	 * nonexistent, the live output is defined inside the band, and one local component carries
	 * both sides.
	 */
	@Test
	void theClosureOfACoincidenceBandOnlyWithholdsCertifiedExistence() {
		buildUnlabelled(Configuration.CRITICAL, Transform.of(UNIT, "1", "1000", "1000"));
		Result result = certificate("lr2");
		LocusExistenceStructure2D structure = result.getStructure();
		long runs = work("lr2", LocusCertificationWork2D.COINCIDENCE_BAND_CLOSURE);
		double width = result.getClosedCoincidenceWidth();
		assertTrue(runs > 0, "closed runs");
		assertTrue(width > 0 && width < runs * Math.scalb(2 * Math.PI,
				-Policy.DEFAULT.getBandDepth()), "closed width " + width);
		// Observed about 6e-10; a regression guard far above it.
		assertTrue(width < 1e-6, "closed width " + width);
		Coverage band = null;
		for (Coverage coverage : structure.getCoverage()) {
			LocusInterval2D interval = coverage.getInterval();
			boolean near = interval.getUpper() > CRITICAL - 1e-4
					&& interval.getLower() < CRITICAL + 1e-4;
			assertFalse(near && coverage.getState() == CoverageState.CERTIFIED_DOES_NOT_EXIST,
					coverage.toString());
			if (coverage.getState() == CoverageState.UNRESOLVED) {
				assertNull(band, "a single band: " + coverage);
				band = coverage;
				assertEquals(LocusCoverageCause2D.UNLABELLED_COINCIDENT_OUTPUT,
						coverage.getCause());
				assertTrue(near, coverage.toString());
			}
		}
		assertNotNull(band);
		assertEquals(1, structure.getContinuousValidComponents().size());
		GeoPoint driver = (GeoPoint) lookup("N");
		GeoPoint live = (GeoPoint) lookup("R2");
		int defined = 0;
		for (int sample = 1; sample < 64; sample++) {
			moveDriver(driver, band.getInterval().getLower() + (band.getInterval().getUpper()
					- band.getInterval().getLower()) * sample / 64);
			if (live.isDefined()) {
				defined++;
			}
		}
		assertTrue(defined > 0, "the live output exists inside the band");
	}

	/**
	 * Decision D-CU2-T13-R7: the inherited incidence test reads the midpoint and the half axis
	 * of the circle, not its matrix. The capture keeps the half axis with the matrix entries,
	 * bit for bit, with the midpoint of the matrix exactly; translated far from its size, the
	 * square of the half axis differs from the radius of the matrix.
	 */
	@Test
	void theCapturedCircleCarriesTheHalfAxisOfTheInheritedIncidenceTest() {
		for (Transform transform : List.of(Transform.of(BigDecimal.ONE, "1", "0", "0"),
				Transform.of(BigDecimal.ONE, "0.001", "1000", "1000"))) {
			buildUnlabelled(Configuration.CRITICAL, transform);
			GeoConic circle = (GeoConic) lookup("p");
			CertifiedConstructionProgram2D program = ((ReconstructibleLocusEvaluator2D) locus("lr2")
					.getSemanticDefinition().getEvaluatorCapability()).captureCertifiedProgramV3()
					.orElseThrow();
			CertifiedConstructionProgram2D.Operand operand = null;
			for (CertifiedConstructionProgram2D.Step step : program.getSteps()) {
				if (step.getOperation() == CertifiedConstructionProgram2D.Operation.LINE_CIRCLE) {
					operand = step.getOperands().get(1);
				}
			}
			assertNotNull(operand, program.getSignature());
			double[] matrix = circle.getFlatMatrix();
			assertEquals(CertifiedConstructionProgram2D.OperandKind.CIRCLE, operand.getKind());
			assertEquals(matrix[2], operand.getValue(0));
			assertEquals(matrix[4], operand.getValue(1));
			assertEquals(matrix[5], operand.getValue(2));
			assertEquals(circle.getHalfAxis(0), operand.getValue(3));
			assertEquals(-matrix[4], circle.getMidpoint().getX());
			assertEquals(-matrix[5], circle.getMidpoint().getY());
			BigDecimal a4 = new BigDecimal(matrix[4]);
			BigDecimal a5 = new BigDecimal(matrix[5]);
			BigDecimal halfAxis = new BigDecimal(operand.getValue(3));
			int gap = halfAxis.multiply(halfAxis).subtract(a4.multiply(a4).add(a5.multiply(a5))
					.subtract(new BigDecimal(matrix[2]))).signum();
			assertEquals(transform.translationX.signum() != 0, gap != 0, transform.toString());
			getKernel().clearConstruction(true);
		}
	}

	/**
	 * Finding H-T13-C and precision F-CU2-1 (decision D-CU2-T13-R7): where a root is certified
	 * to exist but the inherited computation leaves the labelled output undefined (regime S3:
	 * every retry fails the incidence test), the certificate keeps the mathematical existence
	 * and the published evaluation fails with the typed computational cause, never a point and
	 * never certified nonexistence; where the inherited computation declares the line tangent
	 * and its foot passes the incidence test (regime S1) the published point is that foot, an
	 * effective value within {@code sqrt(1e-5) R} of an exact root; and a consumer that needs
	 * every point reports a numerical failure. The frozen candidate behaves the same.
	 */
	@Test
	void aCertifiedRootThatTheInheritedComputationLosesIsAComputationalFailure() {
		int failures = 0;
		for (Transform transform : List.of(Transform.of(BigDecimal.ONE, "0.001", "1000", "1000"),
				Transform.of(BigDecimal.ONE, "0.001953125", "1024", "1024"))) {
			for (Configuration configuration : UNLABELLED) {
				String context = configuration + " " + transform;
				build(configuration, transform);
				double[] constants = captured();
				double radius = Math.sqrt(constants[11] * constants[11]
						+ constants[12] * constants[12] - constants[10]);
				for (String label : configuration.loci) {
					LocusExistenceStructure2D structure = structure(label);
					assertEquals(Completeness.COMPLETE, structure.getCompleteness(),
							context + " " + label);
					LocusDefinition2D definition = locus(label).getSemanticDefinition();
					GeoPoint driver = (GeoPoint) lookup("N");
					GeoPoint live = (GeoPoint) lookup(label.substring(1).toUpperCase());
					try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(8)) {
						for (int sample = 0; sample < 1000; sample++) {
							double t = CRITICAL - 0.05 + 0.1 * (sample + 0.5) / 1000;
							if (coverageAt(structure, t).getState()
									!= CoverageState.CERTIFIED_EXISTS) {
								continue;
							}
							moveDriver(driver, t);
							LocusEvaluation2D evaluation = definition.evaluate(BRANCH, t, session);
							String where = context + " " + label + " t=" + t;
							if (evaluation.isValid()) {
								assertTrue(live.isDefined(), where);
								double distance = distanceToNearestRoot((GeoLine) lookup("t"),
										constants, evaluation.getPoint().getX(),
										evaluation.getPoint().getY());
								assertTrue(distance <= 1.05 * Math.sqrt(1e-5) * radius,
										where + ": " + distance / radius);
							} else {
								assertFalse(live.isDefined(), where);
								assertEquals(EvaluationStatus.EVALUATION_FAILED,
										evaluation.getStatus(), where);
								assertTrue(evaluation.getDiagnostic().startsWith(
										LocusDefinition2D.COMPUTATIONAL_FAILURE), where + ": "
										+ evaluation.getDiagnostic());
								failures++;
							}
						}
					}
				}
				if (configuration == Configuration.CRITICAL) {
					add("lengthQ=LocusLength(lq)");
					LocusMetricResult2D length = ((GeoLocusMetricResult) lookup("lengthQ"))
							.getMetricResult();
					assertEquals(MetricComputationStatus.NUMERICAL_FAILURE,
							length.getComputationStatus(), context);
					assertTrue(length.getDiagnostics().toString().contains(
							LocusDefinition2D.COMPUTATIONAL_FAILURE), context + " "
							+ length.getDiagnostics());
				}
				getKernel().clearConstruction(true);
			}
		}
		assertTrue(failures > 0, "computational failures: " + failures);
	}

	/**
	 * @return descriptors of the unlabelled second output of the untransformed critical, bite
	 *         and penetration configurations, which agree in both units; the kinds of the
	 *         roots of their discriminants go to {@code kinds}
	 */
	private Map<Configuration, String> unlabelledReferences(
			Map<Configuration, List<RealRootStructure1D.RootKind>> kinds) {
		Map<Configuration, String> descriptors = new LinkedHashMap<>();
		for (Configuration configuration : UNLABELLED) {
			for (BigDecimal unit : List.of(UNIT, BigDecimal.ONE)) {
				buildUnlabelled(configuration, Transform.of(unit, "1", "0", "0"));
				String descriptor = descriptor("lr2");
				assertEquals(descriptors.getOrDefault(configuration, descriptor), descriptor,
						configuration + " lr2 in both units");
				descriptors.put(configuration, descriptor);
				kinds.put(configuration, kinds(classify(captured("lr2"))));
				getKernel().clearConstruction(true);
			}
		}
		return descriptors;
	}

	private static List<RealRootStructure1D.RootKind> kinds(
			List<CircleDriverRootStructure2D.Root> roots) {
		return roots.stream().map(CircleDriverRootStructure2D.Root::kind)
				.collect(Collectors.toList());
	}

	/**
	 * @return uniform samples of the circle, samples around the critical parameter and the
	 *         binary64 neighbours of every band edge, inside the domain
	 */
	private static List<Double> samples(LocusExistenceStructure2D structure) {
		List<Double> samples = new ArrayList<>();
		for (int index = 0; index < 720; index++) {
			samples.add(-Math.PI + 2 * Math.PI * (index + 0.5) / 720);
		}
		for (int index = 0; index < 2000; index++) {
			samples.add(CRITICAL - 0.05 + 0.1 * (index + 0.5) / 2000);
		}
		for (Coverage coverage : structure.getCoverage()) {
			if (coverage.getState() != CoverageState.UNRESOLVED) {
				continue;
			}
			for (double edge : new double[] {coverage.getInterval().getLower(),
				coverage.getInterval().getUpper()}) {
				double below = edge;
				double above = edge;
				for (int step = 0; step < 16; step++) {
					below = Math.nextDown(below);
					above = Math.nextUp(above);
					samples.add(below);
					samples.add(above);
				}
				for (int power = 10; power <= 40; power += 2) {
					samples.add(edge - Math.scalb(1.0, -power));
					samples.add(edge + Math.scalb(1.0, -power));
				}
			}
		}
		samples.removeIf(t -> !(t >= -Math.PI && t < Math.PI));
		return samples;
	}

	/**
	 * @return distance from a point to the nearest exact intersection of a binary64 line with
	 *         the circle of the captured matrix entries, with 60 digits
	 */
	private static double distanceToNearestRoot(GeoLine line, double[] constants, double x,
			double y) {
		BigDecimal a = new BigDecimal(line.getX());
		BigDecimal b = new BigDecimal(line.getY());
		BigDecimal c = new BigDecimal(line.getZ());
		BigDecimal centreX = new BigDecimal(constants[11]).negate();
		BigDecimal centreY = new BigDecimal(constants[12]).negate();
		BigDecimal squared = centreX.multiply(centreX).add(centreY.multiply(centreY))
				.subtract(new BigDecimal(constants[10]));
		BigDecimal norm = a.multiply(a).add(b.multiply(b));
		BigDecimal distance = a.multiply(centreX).add(b.multiply(centreY)).add(c);
		BigDecimal discriminant = squared.multiply(norm).subtract(distance.multiply(distance));
		BigDecimal root = discriminant.signum() > 0 ? discriminant.sqrt(REFERENCE)
				: BigDecimal.ZERO;
		BigDecimal footX = centreX.subtract(distance.multiply(a).divide(norm, REFERENCE));
		BigDecimal footY = centreY.subtract(distance.multiply(b).divide(norm, REFERENCE));
		double nearest = Double.POSITIVE_INFINITY;
		for (int sign : new int[] {-1, 1}) {
			BigDecimal offset = root.multiply(BigDecimal.valueOf(sign));
			BigDecimal rootX = footX.add(offset.multiply(b).divide(norm, REFERENCE));
			BigDecimal rootY = footY.subtract(offset.multiply(a).divide(norm, REFERENCE));
			nearest = Math.min(nearest, Math.hypot(
					new BigDecimal(x).subtract(rootX).doubleValue(),
					new BigDecimal(y).subtract(rootY).doubleValue()));
		}
		return nearest;
	}

	/**
	 * @return descriptors of the untransformed configurations (every constant a multiple of
	 *         125), which agree with those in the units of matrix section 1
	 */
	private Map<Configuration, Map<String, String>> referenceDescriptors() {
		Map<Configuration, Map<String, String>> descriptors = new LinkedHashMap<>();
		List<String> problems = new ArrayList<>();
		for (Configuration configuration : Configuration.values()) {
			Map<String, String> loci = new LinkedHashMap<>();
			for (BigDecimal unit : List.of(UNIT, BigDecimal.ONE)) {
				Transform identity = Transform.of(unit, "1", "0", "0");
				build(configuration, identity);
				assertExact(configuration.name(), configuration, identity, inputs());
				double[] constants = captured();
				assertCapturedExact(configuration.name(), configuration, identity, constants);
				for (String label : configuration.loci) {
					assertEquals(Completeness.COMPLETE, structure(label).getCompleteness(),
							configuration + " " + label);
					String descriptor = descriptor(label);
					assertEquals(loci.getOrDefault(label, descriptor), descriptor,
							configuration + " " + label + " in both units");
					loci.put(label, descriptor);
					references(configuration + " " + label, label, configuration, constants,
							identity, problems);
				}
				getKernel().clearConstruction(true);
			}
			descriptors.put(configuration, loci);
		}
		assertEquals(List.of(), problems);
		return descriptors;
	}

	/**
	 * The synthetic configuration with the driver at its starting parameter and the labelled
	 * outputs traced by its loci, every constant transformed. At the tangencies the second
	 * output is undefined when it is created, so only {@code lq} traces it there.
	 */
	private void build(Configuration configuration, Transform transform) {
		drivenLine(configuration, transform, "t");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		for (String label : configuration.loci) {
			add(label + "=LocusV2(" + label.substring(1).toUpperCase() + ",N)");
		}
		getKernel().updateConstruction(false);
	}

	/**
	 * The synthetic configuration with only the unlabelled second output {@code R2} of
	 * {@code Intersect(p, t2, 2)}, traced by {@code lr2} (decision D-CU2-T13-R7).
	 */
	private void buildUnlabelled(Configuration configuration, Transform transform) {
		drivenLine(configuration, transform, "t2");
		add("R2=Intersect(p,t2,2)");
		add("lr2=LocusV2(R2,N)");
		getKernel().updateConstruction(false);
	}

	/** The circle, the point P, the driver at its starting parameter and the line or ray. */
	private void drivenLine(Configuration configuration, Transform transform, String line) {
		BigDecimal zero = BigDecimal.ZERO;
		add("p=Circle((" + plain(transform.x(zero)) + "," + plain(transform.y(zero)) + "),"
				+ plain(transform.length(BigDecimal.valueOf(25))) + ")");
		add("P=(" + plain(transform.x(BigDecimal.valueOf(-25))) + ","
				+ plain(transform.y(BigDecimal.valueOf(50))) + ")");
		add("g1=Circle((" + plain(transform.x(configuration.centreX)) + ","
				+ plain(transform.y(configuration.centreY)) + "),"
				+ plain(transform.length(configuration.radius)) + ")");
		add("N=Point(g1)");
		moveDriver((GeoPoint) lookup("N"), configuration.driver);
		add(line + "=" + configuration.line + "(P,N)");
	}

	/**
	 * @return typed inputs: centre and radius of {@code p}, the point {@code P}, centre and
	 *         radius of {@code g1}
	 */
	private double[] inputs() {
		GeoElement[] circle = lookup("p").getParentAlgorithm().getInput();
		GeoElement[] driverCircle = lookup("g1").getParentAlgorithm().getInput();
		GeoPoint point = (GeoPoint) lookup("P");
		return new double[] {((GeoPoint) circle[0]).getInhomX(),
			((GeoPoint) circle[0]).getInhomY(), ((NumberValue) circle[1]).getDouble(),
			point.getInhomX(), point.getInhomY(), ((GeoPoint) driverCircle[0]).getInhomX(),
			((GeoPoint) driverCircle[0]).getInhomY(), ((NumberValue) driverCircle[1]).getDouble()};
	}

	/**
	 * @return the constants that the class-v3 program of {@code lq} captured, which the
	 *         certificate decides ({@code locus-v2-certified-construction-model.md} section
	 *         10.6): the driver circle by its half axes, eigenvectors and midpoint (0 to 7),
	 *         the point P of the ray or line (8, 9) and the circle p by its matrix entries A2,
	 *         A4 and A5 (10 to 12); the conic derives them from the typed inputs with rounding
	 */
	private double[] captured() {
		return captured("lq");
	}

	/** @return the constants of {@link #captured()} from the program of another locus */
	private double[] captured(String label) {
		CertifiedConstructionProgram2D program = ((ReconstructibleLocusEvaluator2D) locus(label)
				.getSemanticDefinition().getEvaluatorCapability()).captureCertifiedProgramV3()
				.orElseThrow();
		double[] constants = new double[13];
		for (int index = 0; index < 8; index++) {
			constants[index] = program.getDriverConstant(index);
		}
		int found = 0;
		for (CertifiedConstructionProgram2D.Step step : program.getSteps()) {
			List<CertifiedConstructionProgram2D.Operand> operands = step.getOperands();
			if ((step.getOperation() == CertifiedConstructionProgram2D.Operation.RAY
					|| step.getOperation() == CertifiedConstructionProgram2D.Operation.JOIN)
					&& operands.get(0).getKind() == CertifiedConstructionProgram2D.OperandKind
							.POINT) {
				constants[8] = operands.get(0).getValue(3);
				constants[9] = operands.get(0).getValue(4);
				found++;
			} else if (step.getOperation()
					== CertifiedConstructionProgram2D.Operation.LINE_CIRCLE
					&& operands.get(1).getKind() == CertifiedConstructionProgram2D.OperandKind
							.CIRCLE) {
				for (int index = 0; index < 3; index++) {
					constants[10 + index] = operands.get(1).getValue(index);
				}
				found++;
			}
		}
		assertEquals(2, found, program.getSignature());
		return constants;
	}

	/**
	 * The captured constants are the exact transformed constants, bit for bit: the half axes
	 * are the radius of {@code g1}, the eigenvectors the axes, the midpoint its centre, then
	 * the point P and the matrix entries {@code A2 = cx^2 + cy^2 - r^2}, {@code A4 = -cx} and
	 * {@code A5 = -cy} of {@code p}.
	 */
	private static void assertCapturedExact(String context, Configuration configuration,
			Transform transform, double[] constants) {
		BigDecimal zero = BigDecimal.ZERO;
		BigDecimal radius = transform.length(configuration.radius);
		BigDecimal a4 = transform.x(zero).negate();
		BigDecimal a5 = transform.y(zero).negate();
		BigDecimal circle = transform.length(BigDecimal.valueOf(25));
		BigDecimal[] exact = {radius, radius, BigDecimal.ONE, zero, zero, BigDecimal.ONE,
			transform.x(configuration.centreX), transform.y(configuration.centreY),
			transform.x(BigDecimal.valueOf(-25)), transform.y(BigDecimal.valueOf(50)),
			a4.multiply(a4).add(a5.multiply(a5)).subtract(circle.multiply(circle)), a4, a5};
		for (int index = 0; index < exact.length; index++) {
			assertEquals(0, new BigDecimal(constants[index]).compareTo(exact[index]),
					context + " captured constant " + index + " " + constants[index]);
		}
	}

	/** The typed inputs are the exact transformed constants, bit for bit. */
	private static void assertExact(String context, Configuration configuration,
			Transform transform, double[] constants) {
		BigDecimal[] exact = {transform.x(BigDecimal.ZERO), transform.y(BigDecimal.ZERO),
			transform.length(BigDecimal.valueOf(25)), transform.x(BigDecimal.valueOf(-25)),
			transform.y(BigDecimal.valueOf(50)), transform.x(configuration.centreX),
			transform.y(configuration.centreY), transform.length(configuration.radius)};
		for (int index = 0; index < exact.length; index++) {
			assertEquals(0, new BigDecimal(constants[index]).compareTo(exact[index]),
					context + " constant " + index + " " + constants[index]);
		}
	}

	/**
	 * @return the structure of a certificate: completeness, coverage states and causes,
	 *         components with their ends and boundary sources, singular parameters, point
	 *         components and folds, without parameter values; durable operation identities
	 *         are replaced by the labels of their geos
	 */
	private String descriptor(String label) {
		Map<String, String> names = new LinkedHashMap<>();
		SpatialIdentityRegistry registry = getKernel().getConstruction()
				.getSpatialIdentityRegistry();
		for (GeoElement geo : getKernel().getConstruction().getGeoSetConstructionOrder()) {
			PersistentGeoId id = registry.getPersistentGeoId(geo);
			if (id != null && geo.isLabelSet()) {
				names.put(id.toExternalForm(), geo.getLabelSimple());
			}
		}
		LocusExistenceStructure2D structure = structure(label);
		StringBuilder text = new StringBuilder(structure.getCompleteness().name())
				.append("|closed=").append(structure.isClosedPeriodic());
		String previous = null;
		for (Coverage coverage : structure.getCoverage()) {
			// Adjacent pieces of one state and cause differ only in the proof that decided
			// them, which is not structure.
			String piece = "|coverage=" + coverage.getState() + "/" + coverage.getCause();
			if (!piece.equals(previous)) {
				text.append(piece);
			}
			previous = piece;
		}
		for (ContinuousComponent component : structure.getContinuousValidComponents()) {
			text.append("|component=arc:").append(component.isArc()).append(",seam:")
					.append(component.crossesSeam()).append(',')
					.append(component.getMaximality()).append(',')
					.append(component.getNature());
			for (LocusBoundary2D end : List.of(component.getLowerBoundary(),
					component.getUpperBoundary())) {
				text.append(",end=").append(end.getKind()).append(':')
						.append(end.isIncluded()).append(':').append(end.getNature())
						.append(':').append(source(end.getSource(), names));
			}
		}
		for (LocusSingularParameter2D singular : structure.getSingularParameters()) {
			text.append("|singular=").append(singular.getMultiplicity()).append(':')
					.append(singular.getNature()).append(':')
					.append(source(singular.getSource(), names));
		}
		for (LocusPointComponent2D point : structure.getPointComponents()) {
			text.append("|point=").append(point.getMaximality()).append(':')
					.append(point.getPositionState()).append(':')
					.append(source(point.getSource(), names));
		}
		return text.append("|folds=").append(structure.getCertifiedFolds().size()).toString();
	}

	private static String source(LocusBoundarySource2D source, Map<String, String> names) {
		if (source == null) {
			return "none";
		}
		String origin = source.getOrigin();
		for (Map.Entry<String, String> name : names.entrySet()) {
			origin = origin.replace(name.getKey(), name.getValue());
		}
		return origin + "/" + source.getPredicate() + "/" + source.getSide();
	}

	/**
	 * Records every statement of a certificate that the 60-digit reference of the captured
	 * constants contradicts: a certified piece at one of its samples, a transition bracket
	 * without a sign change of the discriminant and, for an exact transformation, a singular
	 * parameter or point component that does not enclose the transformed reference: the
	 * double root at {@code atan2(4, 3)}, or the point (15, 20) at {@code atan2(-4, -3)} or,
	 * for the line of the point pair, the point (-25, 0) at the seam.
	 *
	 * @param transform the exact transformation, or {@code null} for rounded constants
	 */
	private void references(String context, String label, Configuration configuration,
			double[] constants, Transform transform, List<String> problems) {
		LocusExistenceStructure2D structure = structure(label);
		for (Coverage coverage : structure.getCoverage()) {
			CoverageState state = coverage.getState();
			if (state != CoverageState.CERTIFIED_EXISTS
					&& state != CoverageState.CERTIFIED_DOES_NOT_EXIST) {
				continue;
			}
			LocusInterval2D interval = coverage.getInterval();
			for (int sample = 1; sample <= 5; sample++) {
				double t = interval.getLower()
						+ (interval.getUpper() - interval.getLower()) * sample / 6;
				BigDecimal[] value = discriminantAt(constants, t);
				// A line has both roots; a ray only those ahead of P.
				boolean exists = value[0].signum() >= 0
						&& (!"Ray".equals(configuration.line) || value[1].signum() > 0);
				if (exists != (state == CoverageState.CERTIFIED_EXISTS)) {
					problems.add(context + ": " + state + " at t=" + t + " where D=" + value[0]
							+ " and the ray test is " + value[1]);
				}
			}
		}
		for (ContinuousComponent component : structure.getContinuousValidComponents()) {
			for (LocusBoundary2D end : List.of(component.getLowerBoundary(),
					component.getUpperBoundary())) {
				if (end.getKind() == LocusBoundary2D.Kind.CERTIFIED_TRANSITION
						&& discriminantAt(constants, end.getEnclosureLower())[0].signum()
								* discriminantAt(constants, end.getEnclosureUpper())[0]
										.signum() >= 0) {
					problems.add(context + ": no sign change of the discriminant across "
							+ end);
				}
			}
		}
		if (transform == null) {
			return;
		}
		for (LocusSingularParameter2D singular : structure.getSingularParameters()) {
			if (!enclosesCosine(singular.getParameterLower(), singular.getParameterUpper(),
					true)) {
				problems.add(context + ": the singular parameter does not enclose atan2(4, 3) "
						+ singular);
			}
		}
		for (LocusPointComponent2D point : structure.getPointComponents()) {
			// The seam point of the line is the exact -pi, inside [-nextUp(pi), -pi].
			boolean seam = "Line".equals(configuration.line)
					&& new BigDecimal(point.getParameterLower()).compareTo(PI.negate()) <= 0
					&& PI.negate().compareTo(new BigDecimal(point.getParameterUpper())) <= 0;
			if (!seam && !enclosesCosine(point.getParameterLower(), point.getParameterUpper(),
					false)) {
				problems.add(context + ": the point component does not enclose atan2(-4, -3) "
						+ point);
			}
			double[] position = point.getPositionEnclosure();
			BigDecimal x = transform.x(BigDecimal.valueOf(seam ? -25 : 15));
			BigDecimal y = transform.y(BigDecimal.valueOf(seam ? 0 : 20));
			if (position == null || new BigDecimal(position[0]).compareTo(x) > 0
					|| x.compareTo(new BigDecimal(position[1])) > 0
					|| new BigDecimal(position[2]).compareTo(y) > 0
					|| y.compareTo(new BigDecimal(position[3])) > 0) {
				problems.add(context + ": the position does not enclose (" + x + ", " + y + ") "
						+ point);
			}
		}
	}

	/**
	 * Records what the 60-digit discriminant of the captured constants contradicts,
	 * independently of any root classification: sampled at 4096 parameters, it changes sign
	 * with the roots ahead of P (on a ray) only where the certificate changes state or
	 * certifies a boundary; and at both ends of the enclosure of every singular parameter (a
	 * double root inside an arc) it is not negative with a slope from negative to positive, and
	 * of every point component (a double root inside nonexistence) not positive with a slope
	 * from positive to negative.
	 *
	 * @return number of sign changes found at the samples
	 */
	private int signChangesAreCertified(String context, String label,
			Configuration configuration, double[] constants, List<String> problems) {
		LocusExistenceStructure2D structure = structure(label);
		boolean ray = "Ray".equals(configuration.line);
		int samples = 4096;
		int changes = 0;
		double previous = -Math.PI;
		BigDecimal[] before = discriminantAt(constants, previous);
		for (int index = 1; index < samples; index++) {
			double t = -Math.PI + 2 * Math.PI * index / samples;
			BigDecimal[] value = discriminantAt(constants, t);
			if (before[0].signum() * value[0].signum() < 0
					&& (!ray || before[1].signum() > 0 && value[1].signum() > 0)) {
				changes++;
				if (!changesIn(structure, previous, t)) {
					problems.add(context + ": the discriminant changes sign in (" + previous
							+ ", " + t + ") where the certificate states no change");
				}
			}
			previous = t;
			before = value;
		}
		for (LocusSingularParameter2D singular : structure.getSingularParameters()) {
			if (!doubleRoot(constants, singular.getParameterLower(),
					singular.getParameterUpper(), 1)) {
				problems.add(context + ": no double root of the discriminant with an arc on both"
						+ " sides in " + singular);
			}
		}
		for (LocusPointComponent2D point : structure.getPointComponents()) {
			if (!doubleRoot(constants, point.getParameterLower(), point.getParameterUpper(),
					-1)) {
				problems.add(context + ": no double root of the discriminant with nonexistence"
						+ " on both sides in " + point);
			}
		}
		return changes;
	}

	/** @return whether the certificate changes state, or certifies a boundary, in the range */
	private static boolean changesIn(LocusExistenceStructure2D structure, double lower,
			double upper) {
		for (Coverage coverage : structure.getCoverage()) {
			LocusInterval2D interval = coverage.getInterval();
			boolean meets = interval.getLower() <= upper && lower <= interval.getUpper();
			if (meets && coverage.getState() != CoverageState.CERTIFIED_EXISTS
					&& coverage.getState() != CoverageState.CERTIFIED_DOES_NOT_EXIST) {
				return true;
			}
		}
		return coverageAt(structure, lower).getState()
				!= coverageAt(structure, upper).getState();
	}

	/**
	 * @return whether the 60-digit discriminant has, at both binary64 ends, the sign of
	 *         {@code side} or zero, and a slope of sign {@code -side} at the lower end and
	 *         {@code side} at the upper one: a double root with that sign on both sides
	 */
	private static boolean doubleRoot(double[] constants, double lower, double upper,
			int side) {
		BigDecimal[] low = slopeAt(constants, lower);
		BigDecimal[] high = slopeAt(constants, upper);
		return low[0].signum() * side >= 0 && high[0].signum() * side >= 0
				&& low[1].signum() * side <= 0 && high[1].signum() * side >= 0;
	}

	/**
	 * @return the 60-digit discriminant of the line from P through the driver at {@code t}
	 *         against {@code p}, and its derivative in {@code t}, from the captured constants
	 */
	private static BigDecimal[] slopeAt(double[] constants, double t) {
		BigDecimal parameter = new BigDecimal(t);
		BigDecimal[] k = new BigDecimal[constants.length];
		for (int index = 0; index < constants.length; index++) {
			k[index] = new BigDecimal(constants[index]);
		}
		BigDecimal cosine = series(parameter, true, REFERENCE);
		BigDecimal sine = series(parameter, false, REFERENCE);
		BigDecimal along = k[0].multiply(cosine, REFERENCE);
		BigDecimal across = k[1].multiply(sine, REFERENCE);
		BigDecimal alongSlope = k[0].multiply(sine, REFERENCE).negate();
		BigDecimal acrossSlope = k[1].multiply(cosine, REFERENCE);
		BigDecimal dx = along.multiply(k[2], REFERENCE).add(across.multiply(k[4], REFERENCE),
				REFERENCE).add(k[6], REFERENCE).subtract(k[8], REFERENCE);
		BigDecimal dy = along.multiply(k[3], REFERENCE).add(across.multiply(k[5], REFERENCE),
				REFERENCE).add(k[7], REFERENCE).subtract(k[9], REFERENCE);
		BigDecimal dxSlope = alongSlope.multiply(k[2], REFERENCE)
				.add(acrossSlope.multiply(k[4], REFERENCE), REFERENCE);
		BigDecimal dySlope = alongSlope.multiply(k[3], REFERENCE)
				.add(acrossSlope.multiply(k[5], REFERENCE), REFERENCE);
		BigDecimal wx = k[11].negate().subtract(k[8]);
		BigDecimal wy = k[12].negate().subtract(k[9]);
		BigDecimal squared = k[11].multiply(k[11]).add(k[12].multiply(k[12])).subtract(k[10]);
		BigDecimal cross = dx.multiply(wy, REFERENCE).subtract(dy.multiply(wx, REFERENCE),
				REFERENCE);
		BigDecimal crossSlope = dxSlope.multiply(wy, REFERENCE)
				.subtract(dySlope.multiply(wx, REFERENCE), REFERENCE);
		BigDecimal discriminant = squared.multiply(dx.multiply(dx, REFERENCE)
				.add(dy.multiply(dy, REFERENCE), REFERENCE), REFERENCE)
				.subtract(cross.multiply(cross, REFERENCE), REFERENCE);
		BigDecimal slope = squared.multiply(dx.multiply(dxSlope, REFERENCE)
				.add(dy.multiply(dySlope, REFERENCE), REFERENCE), REFERENCE)
				.subtract(cross.multiply(crossSlope, REFERENCE), REFERENCE)
				.multiply(BigDecimal.valueOf(2), REFERENCE);
		return new BigDecimal[] {discriminant, slope};
	}

	/**
	 * @return whether {@code [lower, upper]} encloses {@code atan2(4, 3)}, where the cosine
	 *         decreases through 3/5, or {@code atan2(-4, -3)}, where it increases through
	 *         -3/5, proved with the 60-digit cosine of the binary64 ends
	 */
	private static boolean enclosesCosine(double lower, double upper, boolean critical) {
		BigDecimal low = series(new BigDecimal(lower), true, REFERENCE);
		BigDecimal high = series(new BigDecimal(upper), true, REFERENCE);
		if (critical) {
			BigDecimal value = new BigDecimal("0.6");
			return 0 < lower && upper < Math.PI / 2 && low.compareTo(value) >= 0
					&& value.compareTo(high) >= 0;
		}
		BigDecimal value = new BigDecimal("-0.6");
		return -Math.PI < lower && upper < -Math.PI / 2 && low.compareTo(value) <= 0
				&& value.compareTo(high) <= 0;
	}

	/**
	 * @return the 60-digit discriminant of the line from P through the driver at {@code t}
	 *         against {@code p}, and the direction test of the ray (positive when the ray
	 *         points to the circle), from the captured constants
	 */
	private static BigDecimal[] discriminantAt(double[] constants, double t) {
		BigDecimal parameter = new BigDecimal(t);
		BigDecimal[] k = new BigDecimal[constants.length];
		for (int index = 0; index < constants.length; index++) {
			k[index] = new BigDecimal(constants[index]);
		}
		// The driver as GeoConicND.pathChangedWithoutCheckEllipse places it.
		BigDecimal along = k[0].multiply(series(parameter, true, REFERENCE), REFERENCE);
		BigDecimal across = k[1].multiply(series(parameter, false, REFERENCE), REFERENCE);
		BigDecimal nx = along.multiply(k[2], REFERENCE).add(across.multiply(k[4], REFERENCE),
				REFERENCE).add(k[6], REFERENCE);
		BigDecimal ny = along.multiply(k[3], REFERENCE).add(across.multiply(k[5], REFERENCE),
				REFERENCE).add(k[7], REFERENCE);
		BigDecimal dx = nx.subtract(k[8], REFERENCE);
		BigDecimal dy = ny.subtract(k[9], REFERENCE);
		// The circle x^2 + y^2 + 2 A4 x + 2 A5 y + A2 = 0: centre (-A4, -A5).
		BigDecimal wx = k[11].negate().subtract(k[8]);
		BigDecimal wy = k[12].negate().subtract(k[9]);
		BigDecimal squared = k[11].multiply(k[11]).add(k[12].multiply(k[12])).subtract(k[10]);
		BigDecimal cross = dx.multiply(wy, REFERENCE).subtract(dy.multiply(wx, REFERENCE),
				REFERENCE);
		BigDecimal discriminant = squared.multiply(dx.multiply(dx, REFERENCE)
				.add(dy.multiply(dy, REFERENCE), REFERENCE), REFERENCE)
				.subtract(cross.multiply(cross, REFERENCE), REFERENCE);
		return new BigDecimal[] {discriminant,
			dx.multiply(wx, REFERENCE).add(dy.multiply(wy, REFERENCE), REFERENCE)};
	}

	/**
	 * @return roots of the discriminant of the captured constants, built by the test with
	 *         exact rationals and classified with their multiplicities
	 */
	private static List<CircleDriverRootStructure2D.Root> classify(double[] constants) {
		ExactRational2D[] k = new ExactRational2D[constants.length];
		for (int index = 0; index < constants.length; index++) {
			k[index] = ExactRational2D.ofBinary64(constants[index]);
		}
		TrigonometricPolynomial2D nx = TrigonometricPolynomial2D.constant(k[6])
				.add(TrigonometricPolynomial2D.cosine().scale(k[0].multiply(k[2])))
				.add(TrigonometricPolynomial2D.sine().scale(k[1].multiply(k[4])));
		TrigonometricPolynomial2D ny = TrigonometricPolynomial2D.constant(k[7])
				.add(TrigonometricPolynomial2D.cosine().scale(k[0].multiply(k[3])))
				.add(TrigonometricPolynomial2D.sine().scale(k[1].multiply(k[5])));
		TrigonometricPolynomial2D px = TrigonometricPolynomial2D.constant(k[8]);
		TrigonometricPolynomial2D py = TrigonometricPolynomial2D.constant(k[9]);
		TrigonometricPolynomial2D a = py.subtract(ny);
		TrigonometricPolynomial2D b = nx.subtract(px);
		TrigonometricPolynomial2D c = px.multiply(ny).subtract(py.multiply(nx));
		TrigonometricPolynomial2D distance = a.scale(k[11].negate()).add(b.scale(
				k[12].negate())).add(c);
		ExactRational2D squared = k[11].multiply(k[11]).add(k[12].multiply(k[12]))
				.subtract(k[10]);
		TrigonometricPolynomial2D discriminant = a.multiply(a).add(b.multiply(b))
				.scale(squared).subtract(distance.multiply(distance));
		return CircleDriverRootStructure2D.classify(discriminant,
				ExactRational2D.ofBinary64(DELTA / 4), ExactBudget1D.DEFAULT).roots();
	}

	private static long count(List<CircleDriverRootStructure2D.Root> roots,
			RealRootStructure1D.RootKind kind) {
		return roots.stream().filter(root -> root.kind() == kind).count();
	}

	private static String plain(BigDecimal value) {
		return value.stripTrailingZeros().toPlainString();
	}

	/**
	 * The synthetic configurations of matrix section 1 that V-INV-2 transforms: rays through
	 * the driver, and the line of the two point components.
	 */
	private enum Configuration {
		CRITICAL("35", "-45", "40", 2.5, "Ray", "lq", "lr"),
		BITE("35", "-45", "40.0009765625", 2.5, "Ray", "lq", "lr"),
		PENETRATION("35", "-45", "39.9990234375", 2.5, "Ray", "lq", "lr"),
		TANGENCY("83", "19", "40", EXTERNAL, "Ray", "lq"),
		POINT_PAIR("15", "70", "40", EXTERNAL, "Line", "lq");

		private final BigDecimal centreX;
		private final BigDecimal centreY;
		private final BigDecimal radius;
		private final double driver;
		private final String line;
		private final List<String> loci;

		Configuration(String centreX, String centreY, String radius, double driver,
				String line, String... loci) {
			this.centreX = new BigDecimal(centreX);
			this.centreY = new BigDecimal(centreY);
			this.radius = new BigDecimal(radius);
			this.driver = driver;
			this.line = line;
			this.loci = List.of(loci);
		}
	}

	/** A uniform scale and a translation of the whole construction, with exact data. */
	private static final class Transform {
		private final BigDecimal unit;
		private final BigDecimal scale;
		private final BigDecimal translationX;
		private final BigDecimal translationY;

		private Transform(BigDecimal unit, BigDecimal scale, BigDecimal translationX,
				BigDecimal translationY) {
			this.unit = unit;
			this.scale = scale;
			this.translationX = translationX;
			this.translationY = translationY;
		}

		static Transform of(BigDecimal unit, String scale, String translationX,
				String translationY) {
			return new Transform(unit, new BigDecimal(scale), new BigDecimal(translationX),
					new BigDecimal(translationY));
		}

		BigDecimal x(BigDecimal value) {
			return length(value).add(translationX);
		}

		BigDecimal y(BigDecimal value) {
			return length(value).add(translationY);
		}

		BigDecimal length(BigDecimal value) {
			return value.multiply(unit).multiply(scale);
		}

		@Override
		public String toString() {
			return "unit " + unit + ", scale " + scale + ", translation (" + translationX + ", "
					+ translationY + ")";
		}
	}

	/**
	 * GC-2 and GC-3: the certificate is computed once per revision, when the revision is
	 * published (step T11), and shared; reading it, moving the driver or recomputing does
	 * not recompute or change it; a change of a non-driver input publishes exactly one
	 * revision of each locus it affects, with exactly one certificate computation each.
	 */
	@Test
	void theShadowCertificateIsComputedOncePerRevision() {
		add("rr=40+2^(-10)");
		critical("rr");
		long certificates = work("lq", LocusCertificationWork2D.CERTIFICATE_COMPUTATION);
		long boxes = work("lq", LocusCertificationWork2D.EXISTENCE_BOX);
		Result result = certificate("lq");
		assertTrue(result.getExistenceBoxes() > 0);
		assertTrue(boxes >= result.getExistenceBoxes(), "computed when published");
		assertEquals(boxes, work("lq", LocusCertificationWork2D.EXISTENCE_BOX));
		assertEquals(certificates,
				work("lq", LocusCertificationWork2D.CERTIFICATE_COMPUTATION));
		assertSame(result, certificate("lq"));
		getKernel().updateConstruction(false);
		assertSame(result, certificate("lq"), "an equivalent recompute keeps it");
		moveDriver((GeoPoint) lookup("N"), 2.5);
		moveDriver((GeoPoint) lookup("N"), CRITICAL);
		assertSame(result, certificate("lq"));
		assertEquals(boxes, work("lq", LocusCertificationWork2D.EXISTENCE_BOX));
		assertEquals(certificates, work("lq", LocusCertificationWork2D.CERTIFICATE_COMPUTATION));
		Map<String, long[]> counted = new LinkedHashMap<>();
		for (String label : List.of("lq", "lr", "lr2")) {
			counted.put(label, new long[] {locus(label).getSemanticRevision(),
				work(label, LocusCertificationWork2D.CERTIFICATE_COMPUTATION)});
		}
		GeoNumeric radius = (GeoNumeric) lookup("rr");
		radius.setValue(39);
		radius.updateCascade();
		Result changed = certificate("lq");
		assertNotEquals(result.getStructure().getSemanticSignature(),
				changed.getStructure().getSemanticSignature());
		assertTrue(changed.getStructure().isClosedPeriodic());
		for (Map.Entry<String, long[]> entry : counted.entrySet()) {
			String label = entry.getKey();
			assertEquals(entry.getValue()[0] + 1, locus(label).getSemanticRevision(), label);
			assertEquals(entry.getValue()[1] + 1,
					work(label, LocusCertificationWork2D.CERTIFICATE_COMPUTATION), label);
		}
	}

	/**
	 * Structure of step T4: an enclosed root keeps the binary64 end of its component closed at
	 * the safe inner bound while stating the inclusion of the exact root; the bracket is
	 * certified boundary coverage, compatible with completeness; a singular parameter lies in
	 * certified existence.
	 */
	@Test
	void enclosedRootsKeepTheStructureConsistent() {
		final LocusInterval2D domain = new LocusInterval2D(0, 1, true, true);
		LocusBoundarySource2D source = new LocusBoundarySource2D("step3", "DISCRIMINANT",
				"lower");
		LocusBoundary2D closed = LocusBoundary2D.enclosed(
				LocusBoundary2D.Kind.CERTIFIED_TRANSITION, source, true,
				LocusEvidenceNature2D.EXACT, 0.25, 0.25 + 1e-12);
		LocusBoundary2D open = LocusBoundary2D.enclosed(LocusBoundary2D.Kind.STRUCTURAL_EVENT,
				new LocusBoundarySource2D("step5", "DIVISOR", "upper"), false,
				LocusEvidenceNature2D.RIGOROUS_INTERVAL, 0.75 - 1e-12, 0.75);
		assertTrue(closed.isIncluded() && closed.isBinaryEndIncluded());
		assertFalse(open.isIncluded());
		assertTrue(open.isBinaryEndIncluded());
		assertTrue(closed.getSignature().contains(":enclosure["));
		assertNotEquals(closed, LocusBoundary2D.enclosed(
				LocusBoundary2D.Kind.CERTIFIED_TRANSITION, source, true,
				LocusEvidenceNature2D.EXACT, 0.25, 0.25 + 2e-12));
		assertThrows(IllegalArgumentException.class, () -> LocusBoundary2D.enclosed(
				LocusBoundary2D.Kind.DOMAIN_END, source, true, LocusEvidenceNature2D.EXACT,
				0.25, 0.3));
		assertThrows(IllegalArgumentException.class, () -> LocusBoundary2D.enclosed(
				LocusBoundary2D.Kind.CERTIFIED_TRANSITION, source, true,
				LocusEvidenceNature2D.EXPLORATORY, 0.25, 0.3));
		assertThrows(IllegalArgumentException.class, () -> LocusBoundary2D.enclosed(
				LocusBoundary2D.Kind.CERTIFIED_TRANSITION, source, true,
				LocusEvidenceNature2D.EXACT, 0.25, 0.25));
		double start = 0.25 + 1e-12;
		double end = 0.75 - 1e-12;
		assertThrows(IllegalArgumentException.class, () -> ContinuousComponent.interval(
				new LocusInterval2D(start, end, false, true), closed, open,
				Maximality.ESTABLISHED, LocusEvidenceNature2D.EXACT, "c0"));
		ContinuousComponent component = ContinuousComponent.interval(
				new LocusInterval2D(start, end, true, true), closed, open,
				Maximality.ESTABLISHED, LocusEvidenceNature2D.RIGOROUS_INTERVAL, "c0");
		List<Coverage> coverage = List.of(
				coverageItem(0, 0.25, true, true, CoverageState.CERTIFIED_DOES_NOT_EXIST,
						LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE),
				coverageItem(0.25, start, false, false, CoverageState.CERTIFIED_BOUNDARY,
						LocusCoverageCause2D.CERTIFIED_BOUNDARY_ENCLOSURE),
				coverageItem(start, end, true, true, CoverageState.CERTIFIED_EXISTS,
						LocusCoverageCause2D.PREDICATES_CERTIFIED_TRUE),
				coverageItem(end, 0.75, false, false, CoverageState.CERTIFIED_BOUNDARY,
						LocusCoverageCause2D.CERTIFIED_BOUNDARY_ENCLOSURE),
				coverageItem(0.75, 1, true, true, CoverageState.CERTIFIED_DOES_NOT_EXIST,
						LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE));
		LocusSingularParameter2D inside = new LocusSingularParameter2D(0.5, 0.5 + 1e-12,
				new LocusBoundarySource2D("step3", "DISCRIMINANT", "root-1"), 2,
				LocusEvidenceNature2D.EXACT);
		LocusExistenceStructure2D structure = new LocusExistenceStructure2D(BRANCH, domain,
				LocusExistenceStructure2D.DomainTopology.BOUNDED, 0, coverage,
				List.of(component), List.of(), List.of(inside), Completeness.COMPLETE,
				HybridExistenceCertifier2D.CONTRACT);
		assertEquals(List.of(inside), structure.getSingularParameters());
		assertTrue(structure.getSemanticSignature().contains("|singular="));
		LocusSingularParameter2D outside = new LocusSingularParameter2D(0.9, 0.9 + 1e-12,
				inside.getSource(), 2, LocusEvidenceNature2D.EXACT);
		assertThrows(IllegalArgumentException.class, () -> new LocusExistenceStructure2D(
				BRANCH, domain, LocusExistenceStructure2D.DomainTopology.BOUNDED, 0, coverage,
				List.of(component), List.of(), List.of(outside), Completeness.COMPLETE,
				HybridExistenceCertifier2D.CONTRACT));
		assertThrows(IllegalArgumentException.class, () -> new LocusSingularParameter2D(0.5,
				0.6, inside.getSource(), 3, LocusEvidenceNature2D.EXACT));
		assertFalse(LocusCoverageCause2D.CERTIFIED_BOUNDARY_ENCLOSURE.admits(
				CoverageState.CERTIFIED_EXISTS));
	}

	/**
	 * The exact first-level polynomials have the sign of the box predicates they stand for:
	 * the discriminant of a line through the driver, and the divisor of a meet carried
	 * through every transform of class v1.
	 */
	@Test
	void firstLevelPolynomialsHaveTheSignOfTheBoxPredicates() {
		critical("40+2^(-10)");
		add("c=Circle((0,0),2)");
		add("M=Point(c)");
		add("S=Intersect(Line(M,(5,0)),Line((0,-10),(1,-10)))");
		add("S1=Rotate(S,20deg,(1,2))");
		add("S2=Mirror(S1,y=0.2x+2)");
		add("S3=Dilate(S2,1.1,(1,2))");
		add("S4=Mirror(S3,(1,1))");
		add("ls=LocusV2(S4,M)");
		int compared = 0;
		for (String label : List.of("lq", "ls")) {
			ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(
					locus(label).getSemanticDefinition());
			CertifiedConstructionProgram2D program = model.getProgram();
			for (int step = 1; step <= program.getSteps().size(); step++) {
				for (ConstructionValidityPredicate2D predicate : List.of(
						ConstructionValidityPredicate2D.DISCRIMINANT,
						ConstructionValidityPredicate2D.DIVISOR)) {
					ConstructionBoxModel2D.Target target = new ConstructionBoxModel2D.Target(
							step, predicate, ConstructionBoxModel2D.STEP_PREDICATE);
					TrigonometricPolynomial2D form = FirstLevelPredicate2D.build(program,
							target).orElse(null);
					if (form == null) {
						continue;
					}
					for (int sample = -50; sample < 50; sample++) {
						double t = (sample + 0.37) * Math.PI / 50;
						ConstructionBoxModel2D.PredicateEnclosure box = model.enclose(
								SplineOutwardInterval2D.point(t), target, true);
						if (box == null || box.value.containsZero()) {
							continue;
						}
						int exact = form.evaluate(ExactRational2D.ofBinary64(Math.cos(t)),
								ExactRational2D.ofBinary64(Math.sin(t))).signum();
						String context = label + " step" + step + " " + predicate + " t=" + t;
						if (predicate.isEquality()) {
							// A normalized homogeneous divisor may change sign: only zeros count.
							assertNotEquals(0, exact, context);
						} else {
							assertEquals(box.value.lower > 0 ? 1 : -1, exact, context);
						}
						compared++;
					}
				}
			}
		}
		assertTrue(compared > 300, "compared " + compared);
	}

	/**
	 * The centred form of a certifying evaluation is sound: on boxes of every width, the
	 * discriminant enclosures it decides with contain a 60-digit reference, and a box it
	 * certifies outside the band of the unlabelled output keeps the live output defined at
	 * every sample (decision D-CU2-T13-R7).
	 */
	@Test
	void theCentredFormEnclosesTheReference() {
		critical("40+2^(-10)");
		GeoPoint driver = (GeoPoint) lookup("N");
		GeoPoint live = (GeoPoint) lookup("R2");
		int kept = 0;
		for (String label : List.of("lq", "lr2")) {
			ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(
					locus(label).getSemanticDefinition());
			Random random = new Random(20261021);
			for (int trial = 0; trial < 400; trial++) {
				double width = Math.scalb(1.0, -2 - random.nextInt(40));
				double lower = trial % 2 == 0 ? CRITICAL - 0.08 + 0.16 * random.nextDouble()
						: -Math.PI + 2 * Math.PI * random.nextDouble();
				SplineOutwardInterval2D box = new SplineOutwardInterval2D(lower,
						lower + width);
				ConstructionBoxOutcome2D outcome = model.evaluateCertifying(box, true);
				for (ConstructionBoxOutcome2D.PredicateEvaluation evaluation
						: outcome.getEvaluations()) {
					if (evaluation.getPredicate() != ConstructionValidityPredicate2D.DISCRIMINANT
							|| evaluation.getRoot() != ConstructionBoxModel2D.STEP_PREDICATE) {
						continue;
					}
					for (double fraction : new double[] {0, 0.25, 0.5, 0.75, 1}) {
						BigDecimal value = referenceLine(lower + width * fraction)[3];
						assertTrue(new BigDecimal(evaluation.getValueLower()).compareTo(value)
								<= 0 && value.compareTo(new BigDecimal(
										evaluation.getValueUpper())) <= 0,
								label + " box=" + lower + "+" + width + " D=" + value);
					}
				}
				if ("lr2".equals(label)
						&& outcome.kind == ConstructionBoxOutcome2D.Kind.VALID) {
					for (double fraction : new double[] {0, 0.25, 0.5, 0.75, 1}) {
						moveDriver(driver, lower + width * fraction);
						assertTrue(live.isDefined(), label + " box=" + lower + "+" + width);
						kept++;
					}
				}
			}
		}
		assertTrue(kept > 0, "samples of certified boxes of lr2: " + kept);
	}

	/**
	 * @return 60-digit line {@code (a, b, c)} through P = (-25, 50) and the driver on the
	 *         circle of centre (35, -45) and radius {@code 40 + 2^-10}, and its discriminant
	 *         against the circle of centre (0, 0) and radius 25
	 */
	private static BigDecimal[] referenceLine(double parameter) {
		MathContext context = new MathContext(60, RoundingMode.HALF_EVEN);
		BigDecimal t = new BigDecimal(parameter);
		BigDecimal radius = new BigDecimal(40 + Math.scalb(1.0, -10));
		BigDecimal nx = new BigDecimal(35).add(radius.multiply(series(t, true, context),
				context));
		BigDecimal ny = new BigDecimal(-45).add(radius.multiply(series(t, false, context),
				context));
		BigDecimal a = new BigDecimal(50).subtract(ny);
		BigDecimal b = nx.add(new BigDecimal(25));
		BigDecimal c = new BigDecimal(-25).multiply(ny, context)
				.subtract(new BigDecimal(50).multiply(nx, context));
		BigDecimal discriminant = a.multiply(a, context).add(b.multiply(b, context))
				.multiply(new BigDecimal(625), context).subtract(c.multiply(c, context));
		return new BigDecimal[] {a, b, c, discriminant};
	}

	private static BigDecimal series(BigDecimal x, boolean cosine, MathContext context) {
		BigDecimal square = x.multiply(x, context);
		BigDecimal term = cosine ? BigDecimal.ONE : x;
		BigDecimal sum = term;
		BigDecimal limit = new BigDecimal("1e-70");
		for (int k = cosine ? 2 : 3; term.abs().compareTo(limit) > 0; k += 2) {
			term = term.multiply(square, context).negate()
					.divide(BigDecimal.valueOf((long) k * (k - 1)), context);
			sum = sum.add(term, context);
		}
		return sum;
	}

	private static Coverage coverageItem(double lower, double upper, boolean lowerClosed,
			boolean upperClosed, CoverageState state, LocusCoverageCause2D cause) {
		return new Coverage(new LocusInterval2D(lower, upper, lowerClosed, upperClosed), state,
				cause, LocusEvidenceNature2D.EXACT, "test");
	}

	private List<String> signatures() {
		List<String> signatures = new ArrayList<>();
		for (String radius : List.of("40", "40+2^(-30)")) {
			critical(radius);
			for (String label : List.of("lq", "lr2")) {
				Result result = certificate(label);
				signatures.add(result.getStructure().getSemanticSignature());
				signatures.add(result.getExactEvidence().stream()
						.map(ExactEvidence::getSignature).collect(Collectors.joining(";")));
				signatures.add(result.getRoots().stream().map(CertifiedRoot::getSignature)
						.collect(Collectors.joining(";")));
				signatures.add(result.getExistenceBoxes() + "");
			}
			getKernel().clearConstruction(true);
		}
		tangency("(83,19)", "Ray");
		signatures.add(certificate("lq").getStructure().getSemanticSignature());
		getKernel().clearConstruction(true);
		return signatures;
	}

	private void assertClosedTransitions(Result result, String predicate) {
		LocusExistenceStructure2D structure = result.getStructure();
		assertEquals(Completeness.COMPLETE, structure.getCompleteness(), predicate);
		assertEquals(2, result.getCertifiedTransitions(), predicate);
		ContinuousComponent component = structure.getContinuousValidComponents().get(0);
		for (LocusBoundary2D boundary : List.of(component.getLowerBoundary(),
				component.getUpperBoundary())) {
			assertEquals(LocusBoundary2D.Kind.CERTIFIED_TRANSITION, boundary.getKind(),
					predicate);
			assertEquals(predicate, boundary.getSource().getPredicate());
			assertTrue(boundary.isIncluded(), predicate + " is a closed end");
			assertTrue(boundary.hasEnclosure());
			assertTrue(boundary.getEnclosureUpper() - boundary.getEnclosureLower() <= DELTA);
		}
	}

	private static void assertExactWithinBudget(Result result) {
		assertFalse(result.getExactEvidence().isEmpty());
		for (ExactEvidence evidence : result.getExactEvidence()) {
			// GC-8: the exact certificate stays within its bit and bisection budgets.
			assertTrue(evidence.getMaximumCoefficientBits()
					<= ExactBudget1D.DEFAULT.maximumCoefficientBits(), evidence.toString());
			assertTrue(evidence.getMaximumRootBisections()
					<= ExactBudget1D.DEFAULT.maximumBisectionsPerRoot(), evidence.toString());
		}
		// GC-4: the boxes stay within the versioned budget.
		assertTrue(result.getExistenceBoxes() <= Policy.DEFAULT.getBoxBudget());
	}

	private static void assertEnclosed(double expected, double lower, double upper) {
		assertTrue(lower <= expected && expected <= upper,
				expected + " not in [" + lower + ", " + upper + "]");
	}

	private static Coverage coverageAt(LocusExistenceStructure2D structure,
			double parameter) {
		for (Coverage coverage : structure.getCoverage()) {
			if (coverage.getInterval().contains(parameter, 0)) {
				return coverage;
			}
		}
		throw new AssertionError("No coverage at " + parameter);
	}

	/**
	 * The discriminant of the ray from P = (-25, 50) through the driver on the circle of
	 * centre (35, -45) and the given radius, against the circle of centre (0, 0) and radius
	 * 25, with exact rational data.
	 */
	private static TrigonometricPolynomial2D criticalDiscriminant(ExactRational2D radius) {
		TrigonometricPolynomial2D nx = TrigonometricPolynomial2D.constant(ExactRational2D
				.of(35)).add(TrigonometricPolynomial2D.cosine().scale(radius));
		TrigonometricPolynomial2D ny = TrigonometricPolynomial2D.constant(ExactRational2D
				.of(-45)).add(TrigonometricPolynomial2D.sine().scale(radius));
		TrigonometricPolynomial2D px = TrigonometricPolynomial2D.constant(ExactRational2D
				.of(-25));
		TrigonometricPolynomial2D py = TrigonometricPolynomial2D.constant(ExactRational2D
				.of(50));
		TrigonometricPolynomial2D a = py.subtract(ny);
		TrigonometricPolynomial2D b = nx.subtract(px);
		TrigonometricPolynomial2D c = px.multiply(ny).subtract(py.multiply(nx));
		return a.multiply(a).add(b.multiply(b)).scale(ExactRational2D.of(625))
				.subtract(c.multiply(c));
	}

	/** Exact synthetic critical configuration with labelled and unlabelled outputs. */
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
		add("t2=Ray(P,N)");
		add("R2=Intersect(p,t2,2)");
		add("lr2=LocusV2(R2,N)");
		// Created by the command, the intersections settle their selection on recompute.
		getKernel().updateConstruction(false);
	}

	/**
	 * Synthetic tangency of the line from P = (-25, 50) through the driver with the circle of
	 * centre (0, 0) and radius 25; the driver starts at the tangency, where the traced point
	 * is defined, so the locus is published (H01 is step T11).
	 */
	private void tangency(String center, String line) {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle(" + center + ",40)");
		add("N=Point(g1)");
		moveDriver((GeoPoint) lookup("N"), EXTERNAL);
		add("t=" + line + "(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		getKernel().updateConstruction(false);
	}

	private static void moveDriver(GeoPoint driver, double parameter) {
		driver.getPathParameter().setT(parameter);
		driver.getPath().pathChanged(driver);
		driver.updateCoords();
		driver.updateCascade();
	}

	private Result certificate(String label) {
		Result result = ((AlgoDependentPointLocusV2) locus(label).getParentAlgorithm())
				.getHybridExistenceCertificate();
		assertNotNull(result, label);
		return result;
	}

	private LocusExistenceStructure2D structure(String label) {
		return certificate(label).getStructure();
	}

	private long work(String label, LocusCertificationWork2D kind) {
		return locus(label).getInstrumentation().getCertificationWork(kind);
	}

	private GeoLocusV2 locus(String label) {
		return assertInstanceOf(GeoLocusV2.class, lookup(label), label);
	}
}
