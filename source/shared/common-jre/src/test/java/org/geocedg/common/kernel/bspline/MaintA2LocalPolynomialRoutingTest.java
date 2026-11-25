/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.algos.AlgoBSpline;
import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocalPiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusSimilarityEvaluator2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.PolynomialSpanCoefficients2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResult2D.SearchCoverage;
import org.geocedg.common.kernel.locus.intersection.IntersectionSourceBinding2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIdentity2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionQuery2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionSolver2D;
import org.geocedg.common.kernel.locus.intersection.PublicTargetIntersectionCapability2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluation2D;
import org.geocedg.common.kernel.locus.metric.LocusDifferentialEvaluator2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A2 ({@code bspline-2d.md} section 11.3, BS-20): every polynomial consumer
 * reads the local span coefficients of a source that publishes them, through the shared
 * accessor; the similarity image forwards them; above 64 spans the one-sided polynomial
 * capability declines by its own limit and the evaluator route answers; the residual rule
 * declines an unrouted reader above {@code kappa_max}. A trap wrapper whose global
 * coefficient accessors throw proves that no consumer reads them.
 */
public class MaintA2LocalPolynomialRoutingTest extends BaseUnitTest {

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	/** Delegating local source whose global coefficient accessors are forbidden. */
	private static final class GlobalTrap implements LocusEvaluator2D,
			LocusDifferentialEvaluator2D, LocalPiecewisePolynomialLocus2D {
		private final BSplineSemanticEvaluator2D inner;

		private GlobalTrap(BSplineSemanticEvaluator2D inner) {
			this.inner = inner;
		}

		@Override
		public LocusEvaluation2D evaluate(LocusDefinition2D definition, LocusBranch2D branch,
				double parameter, LocusEvaluationSession2D session) {
			return inner.evaluate(definition, branch, parameter, session);
		}

		@Override
		public LocusDifferentialEvaluation2D evaluateDifferential(
				LocusDefinition2D definition, String branchKey, double parameter,
				LocusEvaluationSession2D session) {
			return inner.evaluateDifferential(definition, branchKey, parameter, session);
		}

		@Override
		public List<Double> getInteriorBreakpoints(String branchKey, double lower,
				double upper) {
			return inner.getInteriorBreakpoints(branchKey, lower, upper);
		}

		@Override
		public int getPolynomialSpanCount(String branchKey) {
			return inner.getPolynomialSpanCount(branchKey);
		}

		@Override
		public double getPolynomialSpanLower(String branchKey, int spanIndex) {
			return inner.getPolynomialSpanLower(branchKey, spanIndex);
		}

		@Override
		public double getPolynomialSpanUpper(String branchKey, int spanIndex) {
			return inner.getPolynomialSpanUpper(branchKey, spanIndex);
		}

		@Override
		public double[] getPolynomialCoefficients(String branchKey, int spanIndex,
				int coordinate) {
			throw new AssertionError("global coefficients read");
		}

		@Override
		public double[][] getPolynomialCoordinateCoefficients(String branchKey,
				int spanIndex) {
			throw new AssertionError("global coefficients read");
		}

		@Override
		public LocusPoint2D evaluatePolynomialDerivative(String branchKey, double parameter) {
			return inner.evaluatePolynomialDerivative(branchKey, parameter);
		}

		@Override
		public String getPolynomialCapabilitySignature() {
			return "maint-a2-trap|" + inner.getPolynomialCapabilitySignature();
		}

		@Override
		public boolean publishesLocalPolynomialCoefficients(LocusDefinition2D definition) {
			return true;
		}

		@Override
		public double[][] getLocalPolynomialCoordinateCoefficients(String branchKey,
				int spanIndex) {
			return inner.getLocalPolynomialCoordinateCoefficients(branchKey, spanIndex);
		}

		@Override
		public double getGlobalReexpansionConditioning(String branchKey) {
			return inner.getGlobalReexpansionConditioning(branchKey);
		}
	}

	/** A reader that cannot be routed: it gates on the residual rule, as section 11.3 asks. */
	private static final class NonForwardingWrapper implements LocusEvaluator2D,
			PiecewisePolynomialLocus2D {
		private final LocusDefinition2D source;
		private final BSplineSemanticEvaluator2D inner;

		private NonForwardingWrapper(LocusDefinition2D source) {
			this.source = source;
			this.inner = (BSplineSemanticEvaluator2D) source.getEvaluatorCapability();
		}

		@Override
		public boolean supportsPiecewisePolynomial(LocusDefinition2D definition) {
			return PolynomialSpanCoefficients2D.admitsGlobalPublication(source, inner);
		}

		@Override
		public LocusEvaluation2D evaluate(LocusDefinition2D definition, LocusBranch2D branch,
				double parameter, LocusEvaluationSession2D session) {
			return inner.evaluate(definition, branch, parameter, session);
		}

		@Override
		public List<Double> getInteriorBreakpoints(String branchKey, double lower,
				double upper) {
			return inner.getInteriorBreakpoints(branchKey, lower, upper);
		}

		@Override
		public int getPolynomialSpanCount(String branchKey) {
			return inner.getPolynomialSpanCount(branchKey);
		}

		@Override
		public double getPolynomialSpanLower(String branchKey, int spanIndex) {
			return inner.getPolynomialSpanLower(branchKey, spanIndex);
		}

		@Override
		public double getPolynomialSpanUpper(String branchKey, int spanIndex) {
			return inner.getPolynomialSpanUpper(branchKey, spanIndex);
		}

		@Override
		public double[] getPolynomialCoefficients(String branchKey, int spanIndex,
				int coordinate) {
			return inner.getPolynomialCoefficients(branchKey, spanIndex, coordinate);
		}

		@Override
		public LocusPoint2D evaluatePolynomialDerivative(String branchKey, double parameter) {
			return inner.evaluatePolynomialDerivative(branchKey, parameter);
		}

		@Override
		public String getPolynomialCapabilitySignature() {
			return "maint-a2-non-forwarding|" + inner.getPolynomialCapabilitySignature();
		}
	}

	private static LocusDefinition2D rewrap(LocusDefinition2D source, String identity,
			LocusEvaluator2D evaluator) {
		return new LocusDefinition2D(identity, 1, source.getDefinitionStatus(),
				source.getProvider(), source.getBranches(), evaluator,
				source.getDeterminism(), identity + "|signature", source.getInstrumentation());
	}

	private GeoLocusV2 internal(LocusDefinition2D definition) {
		GeoLocusV2 locus = new GeoLocusV2(getConstruction(), definition.getLocusIdentity());
		locus.publishSemanticDefinition(definition);
		return locus;
	}

	private LocusIntersectionResult2D target(GeoLocusV2 source, GeoElement target) {
		return new AlgoLocusIntersectionV2(getConstruction(), source, target,
				source.getLocusIdentity() + "/pair", "maint-a2-lineage", "maint-a2-target",
				"maint-a2-topology", new PublicTargetIntersectionCapability2D(),
				new GeoElement[0]).getResult().getIntersectionResult();
	}

	private static LocusIntersectionResult2D pair(LocusDefinition2D callerFirst,
			LocusDefinition2D callerSecond) {
		LocusPairIntersectionQuery2D query = new LocusPairIntersectionQuery2D(callerFirst,
				callerSecond, "maint-a2/pair", "maint-a2/topology",
				LocusPairIntersectionPolicy2D.initial(callerFirst, callerSecond));
		LocusDefinition2D first = query.isCallerOrderCanonical() ? callerFirst : callerSecond;
		LocusDefinition2D second = query.isCallerOrderCanonical() ? callerSecond : callerFirst;
		return new LocusPairIntersectionSolver2D().intersect(query, first, second,
				new IntersectionSourceBinding2D(query), null,
				lineage -> LocusPairIdentity2D.solutionToken(query.getSourcePairIdentity(),
						query.getConstructiveIntersectionLineage(),
						query.getTopologyContext(), lineage));
	}

	private LocusDefinition2D bspline(String label, String command) {
		GeoLocusV2 locus = add(label + "=" + command);
		assertTrue(locus.isDefined(), command);
		return locus.getSemanticDefinition();
	}

	private void points() {
		for (String point : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2.5)", "D=(4,0.5)",
				"E=(6,1)", "F=(7,3)", "G=(5,4)"}) {
			add(point);
		}
	}

	private static void assertSamePoints(List<LocusIntersectionSolution2D> expected,
			List<LocusIntersectionSolution2D> actual, double tolerance) {
		assertEquals(expected.size(), actual.size());
		for (int index = 0; index < expected.size(); index++) {
			assertEquals(expected.get(index).getEvaluatedPoint().getX(),
					actual.get(index).getEvaluatedPoint().getX(), tolerance);
			assertEquals(expected.get(index).getEvaluatedPoint().getY(),
					actual.get(index).getEvaluatedPoint().getY(), tolerance);
		}
	}

	@Test
	void targetPairInteractionAndSimilarityReadOnlyLocalCoefficients() {
		points();
		LocusDefinition2D curve = bspline("b", "BSpline({A,B,C,D,E,F,G},3)");
		LocusDefinition2D other = bspline("c", "BSpline({G,F,E,D,C},2)");
		LocusDefinition2D trap = rewrap(curve, "maint-a2/trap",
				new GlobalTrap((BSplineSemanticEvaluator2D) curve.getEvaluatorCapability()));
		LocusDefinition2D otherTrap = rewrap(other, "maint-a2/trap-other",
				new GlobalTrap((BSplineSemanticEvaluator2D) other.getEvaluatorCapability()));
		GeoElement circle = add("circle=Circle((3,2),2)");

		LocusIntersectionResult2D routed = target(internal(trap), circle);
		LocusIntersectionResult2D reference = target((GeoLocusV2) lookup("b"), circle);
		assertTrue(routed.getWork().getPolynomialSpansExamined() > 0);
		assertFalse(routed.getFiniteSolutions().isEmpty());
		assertSamePoints(reference.getFiniteSolutions(), routed.getFiniteSolutions(), 0);

		LocusIntersectionResult2D pairRouted = pair(trap, otherTrap);
		assertFalse(pairRouted.getFiniteSolutions().isEmpty());

		// The resolver reads only a public locus; its routing is proved statically
		// (MaintA2GlobalCoefficientInventoryTest) and here by the local search it reports.
		LocusPoint2D onCurve = ((BSplineSemanticEvaluator2D) curve.getEvaluatorCapability())
				.evaluate(curve, curve.getBranches().get(0), 2.25, null).getPoint();
		LocusPointInteractionResult2D interaction = new LocusPointInteractionResolver2D()
				.resolve(new LocusPointInteractionQuery2D((GeoLocusV2) lookup("b"),
						onCurve.getX() + 0.01, onCurve.getY(),
						LocusPointInteractionPolicy2D.initial(0.25)));
		assertEquals(SearchCoverage.ALL_EXPLICIT_POLYNOMIAL_SPANS,
				interaction.getSearchCoverage(), interaction.getDiagnostic());
		assertEquals(1, interaction.getCandidates().size(), interaction.getDiagnostic());

		LocusSimilarityEvaluator2D similarity = new LocusSimilarityEvaluator2D(trap,
				LocusSimilarityTransform2D.translation(1, -2));
		LocusDefinition2D image = rewrap(trap, "maint-a2/trap-image", similarity);
		assertTrue(similarity.publishesLocalPolynomialCoefficients(image));
		LocusIntersectionResult2D imageResult = target(internal(image), circle);
		assertTrue(imageResult.getWork().getPolynomialSpansExamined() > 0);
	}

	@Test
	void aSplineV2SourceAndItsImageAreReadThroughTheInheritedGlobalPath() {
		points();
		GeoLocusV2 spline = add("s=SplineV2({A,B,C,D},3)");
		LocusDefinition2D definition = spline.getSemanticDefinition();
		PiecewisePolynomialLocus2D source =
				(PiecewisePolynomialLocus2D) definition.getEvaluatorCapability();
		PolynomialSpanCoefficients2D form = PolynomialSpanCoefficients2D.read(definition,
				source, "spline-v2/main", 0);
		assertFalse(form.isLocal());
		assertEquals(java.util.Arrays.deepToString(source.getPolynomialCoordinateCoefficients(
				"spline-v2/main", 0)), java.util.Arrays.deepToString(form.getCoordinates()));
		assertTrue(PolynomialSpanCoefficients2D.admitsGlobalPublication(definition, source));
		GeoLocusV2 image = add("t=Translate(s,Vector((1,1)))");
		LocusSimilarityEvaluator2D similarity = (LocusSimilarityEvaluator2D) image
				.getSemanticDefinition().getEvaluatorCapability();
		assertFalse(similarity.publishesLocalPolynomialCoefficients(
				image.getSemanticDefinition()));
	}

	@Test
	void aboveSixtyFourSpansTheEvaluatorRouteAnswers() {
		GeoLocusV2 many = add("m=BSpline(Sequence((k,sin(k)),k,1,70),3)");
		GeoLocusV2 few = add("f=BSpline(Sequence((k,sin(k)),k,1,40),3)");
		assertEquals(67, ((AlgoBSpline) many.getParentAlgorithm()).getEvaluator()
				.getPolynomialSpanCount(AlgoBSpline.BRANCH_KEY));
		add("axis:y=0");
		GeoLocusIntersectionResult manyRoots = add("R1=Intersect(m,axis)");
		GeoLocusIntersectionResult fewRoots = add("R2=Intersect(f,axis)");
		assertEquals(0, manyRoots.getIntersectionResult().getWork()
				.getPolynomialSpansExamined(), "the polynomial capability declined by its limit");
		assertFalse(manyRoots.getIntersectionResult().getFiniteSolutions().isEmpty(),
				"the evaluator capability answered");
		assertTrue(fewRoots.getIntersectionResult().getWork().getPolynomialSpansExamined() > 0);
	}

	@Test
	void theResidualRuleDeclinesAnUnroutedReaderAboveKappaMax() {
		assertEquals("POLYNOMIAL_PUBLICATION_ILL_CONDITIONED",
				PolynomialSpanCoefficients2D.POLYNOMIAL_PUBLICATION_ILL_CONDITIONED);
		assertEquals(65536, PolynomialSpanCoefficients2D.KAPPA_MAX_CANDIDATE, 0);
		points();
		LocusDefinition2D near = bspline("n", "BSpline({A,B,C,D,E},3)");
		LocusDefinition2D far = bspline("f",
				"BSpline({A,B,C,D,E},3,{1E6,1E6,1E6,1E6,1E6+1,1E6+2,1E6+2,1E6+2,1E6+2})");
		BSplineSemanticEvaluator2D nearEvaluator =
				(BSplineSemanticEvaluator2D) near.getEvaluatorCapability();
		BSplineSemanticEvaluator2D farEvaluator =
				(BSplineSemanticEvaluator2D) far.getEvaluatorCapability();
		assertTrue(nearEvaluator.getGlobalReexpansionConditioning(AlgoBSpline.BRANCH_KEY)
				<= PolynomialSpanCoefficients2D.KAPPA_MAX_CANDIDATE);
		assertTrue(farEvaluator.getGlobalReexpansionConditioning(AlgoBSpline.BRANCH_KEY)
				> PolynomialSpanCoefficients2D.KAPPA_MAX_CANDIDATE);
		assertTrue(PolynomialSpanCoefficients2D.admitsGlobalPublication(near, nearEvaluator));
		assertFalse(PolynomialSpanCoefficients2D.admitsGlobalPublication(far, farEvaluator));
		GeoElement line = add("level:y=1.5");
		LocusDefinition2D nearUnrouted = rewrap(near, "maint-a2/unrouted-near",
				new NonForwardingWrapper(near));
		LocusDefinition2D farUnrouted = rewrap(far, "maint-a2/unrouted-far",
				new NonForwardingWrapper(far));
		assertTrue(target(internal(nearUnrouted), line).getWork()
				.getPolynomialSpansExamined() > 0);
		LocusIntersectionResult2D declined = target(internal(farUnrouted), line);
		assertEquals(0, declined.getWork().getPolynomialSpansExamined(),
				"the unrouted reader declines the polynomial capability");
		assertNotNull(declined.getComputationStatus());
		// the family itself, read through the accessor, keeps its polynomial capability
		assertTrue(farEvaluator.supportsPiecewisePolynomial(far));
		assertTrue(target((GeoLocusV2) lookup("f"), line).getWork()
				.getPolynomialSpansExamined() > 0);
	}
}
