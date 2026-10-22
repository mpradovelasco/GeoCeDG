/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusCoverageCause2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvidenceNature2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Completeness;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.Coverage;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.CoverageState;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.DomainTopology;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Regularity;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricCapabilityHierarchy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricComponentBuildException;
import org.geocedg.common.kernel.locus.metric.LocusMetricEngine2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricIndexMode;
import org.geocedg.common.kernel.locus.metric.LocusMetricInstrumentation2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricOwnerLease2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricPolicy2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResult2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricResults2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricSharedOwner2D;
import org.geocedg.common.kernel.locus.metric.MetricComputationStatus;
import org.geocedg.common.kernel.locus.metric.MetricDiagnosticCode2D;
import org.geocedg.common.kernel.locus.metric.MetricGuaranteeClass2D;
import org.geocedg.common.kernel.locus.metric.PolicyV2LocusMetricCapability2D;
import org.geocedg.common.kernel.locus.metric.TotalLocusMetricQuery;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU2 decision H10 (author resolution before step T12; {@code locus-v2-metrics.md}
 * section 26.5): the total length of a construction outside class v3 is a conditioned
 * estimate of class 3 on its declared domain. It keeps the CU1 value, error and scalar
 * admissibility, and it certifies nothing: no existence, continuity, completeness,
 * intersection, address, closure or rigorous input follows from it. Only the cause
 * {@code OPERATION_OUTSIDE_CLASS} makes a branch eligible, and a failure keeps its status.
 */
class PreG9BCu2ConditionedEstimateTest extends G9U0PublicSurfaceTestBase {

	/**
	 * H10-1: the public sine of H10 keeps its CU1 estimate and scalar, while its existence
	 * structure, its intersections, its addresses and the certificates that read its length
	 * stay exactly as without the estimate.
	 */
	@Test
	void aSineOutsideClassV3IsEstimatedWithoutAnyCertifyingAuthority() {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P),sin(4*pi*x(P)))");
		GeoLocusV2 sine = add("L=LocusV2(Q,P)");
		final String before = structure(sine).getSemanticSignature();
		LocusMetricResult2D rich = ((GeoLocusMetricResult) add("M=LocusLength(L)"))
				.getMetricResult();
		double reference = simpson(x -> Math.hypot(1,
				4 * Math.PI * Math.cos(4 * Math.PI * x)), 0, 1, 1 << 16);
		String message = rich.getComputationStatus() + " " + rich.getGuaranteeClass() + " "
				+ rich.getDiagnostics();
		assertEquals(MetricComputationStatus.SUCCESS, rich.getComputationStatus(), message);
		double declared = rich.getErrorEvidence().getEstablishedAbsoluteAmount();
		assertTrue(Math.abs(value(rich) - reference) <= declared, message);
		assertTrue(declared <= rich.getErrorEvidence().getRequestedTolerance().orElseThrow(),
				message);
		assertTrue(rich.isScalarAdmissible(), message);
		assertConditionedWithoutAuthority(rich, sine, "public sine");
		assertTrue(rich.getProvenance().getCapabilityVersion()
				.contains("#conditioned-estimate-"), rich.getProvenance().toString());
		assertTrue(rich.getErrorEvidence().getAssumptions().stream().anyMatch(assumption ->
				assumption.contains("declared piece [0.0, 1.0]")), message);
		// The scalar adapter publishes the same value under the CU1 admissibility.
		GeoNumeric scalar = add("b=Length(L)");
		assertTrue(scalar.isDefined());
		assertEquals(value(rich), scalar.getValue(), 0);
		// Existence, intersections and addresses are those of a locus without a total.
		assertEquals(before, structure(sine).getSemanticSignature());
		assertFalse(structure(sine).isClosedPeriodic());
		add("h:y=0");
		GeoLocusIntersectionResult crossing = add("R=Intersect(L,h)");
		assertTrue(crossing.getIntersectionResult().getFiniteSolutions().isEmpty());
		assertNotEquals(IntersectionSemanticMetadata2D.Completeness.COMPLETE,
				crossing.getIntersectionResult().getCompletenessEvidence().getCompleteness());
		GeoPoint addressed = add("X=Point(L,\"" + BRANCH + "\",0.25)");
		assertFalse(addressed.isDefined());
		// Section 26.3: the estimate is never a rigorous input. A translation by the
		// estimated length leaves the class; the same translation by a plain constant does
		// not.
		add("shift=1");
		add("Pc=P+shift*(0,1)");
		GeoLocusV2 plain = add("K=LocusV2(Pc,P)");
		assertEquals(Completeness.COMPLETE, structure(plain).getCompleteness());
		add("Pb=P+b*(0,1)");
		GeoLocusV2 reading = add("Kb=LocusV2(Pb,P)");
		assertEquals("metric-value-constant", ((ReconstructibleLocusEvaluator2D) reading
				.getSemanticDefinition().getEvaluatorCapability()).getProgramV3Refusal());
		assertEquals(Completeness.NOT_ESTABLISHED, structure(reading).getCompleteness());
		assertTrue(structure(reading).getContinuousValidComponents().isEmpty());
	}

	/**
	 * H10-2: a branch is eligible only when its existence is not established because its
	 * construction is outside class v3. Every other cause, certified nonexistence inside the
	 * declared domain and a partial certificate keep the total refused; declared pieces add
	 * up without the complement of the declared domain.
	 */
	@Test
	void onlyAConstructionOutsideTheClassIsEligible() {
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, true);
		LocusMetricResult2D whole = total("h10-whole", notEstablished(domain,
				LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS));
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, whole.getGuaranteeClass(),
				whole.getDiagnostics().toString());
		assertEquals(Math.sqrt(2), value(whole),
				whole.getErrorEvidence().getEstablishedAbsoluteAmount() + 1E-15);
		assertTrue(whole.getDiagnostics().stream().anyMatch(diagnostic ->
				diagnostic.getCode() == MetricDiagnosticCode2D.EXISTENCE_NOT_CERTIFIED));
		assertTrue(whole.getErrorEvidence().getRigorousEnclosure().isEmpty());

		LocusInterval2D first = new LocusInterval2D(0, 0.25, true, true);
		LocusInterval2D gap = new LocusInterval2D(0.25, 0.5, false, false);
		LocusInterval2D second = new LocusInterval2D(0.5, 1, true, true);
		LocusMetricResult2D pieces = total("h10-pieces", structure(domain, List.of(
				outside(first), new Coverage(gap, CoverageState.CERTIFIED_DOES_NOT_EXIST,
						LocusCoverageCause2D.PROVIDER_COMPLEMENT, LocusEvidenceNature2D.EXACT,
						"declared-gap"), outside(second))));
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, pieces.getGuaranteeClass(),
				pieces.getDiagnostics().toString());
		assertEquals(2, pieces.getContributions().size());
		assertEquals(0.75 * Math.sqrt(2), value(pieces),
				pieces.getErrorEvidence().getEstablishedAbsoluteAmount() + 1E-15);

		for (LocusCoverageCause2D cause : List.of(LocusCoverageCause2D.BUDGET_EXHAUSTED,
				LocusCoverageCause2D.DEGENERATE_CONFIGURATION,
				LocusCoverageCause2D.COMPUTATIONAL_DEPENDENCY_FAILURE,
				LocusCoverageCause2D.SELECTION_NOT_REPRODUCED,
				LocusCoverageCause2D.RECONSTRUCTION_FIDELITY_NOT_ESTABLISHED,
				LocusCoverageCause2D.UPSTREAM_NOT_ESTABLISHED,
				LocusCoverageCause2D.NOT_CERTIFIED)) {
			assertNoTotalWithoutExistence(total("h10-" + cause, notEstablished(domain,
					cause)), cause.name());
		}
		LocusInterval2D lower = new LocusInterval2D(0, 0.5, true, false);
		LocusInterval2D upper = new LocusInterval2D(0.5, 1, true, true);
		assertNoTotalWithoutExistence(total("h10-nonexistence", structure(domain, List.of(
				outside(lower), new Coverage(upper, CoverageState.CERTIFIED_DOES_NOT_EXIST,
						LocusCoverageCause2D.PREDICATE_CERTIFIED_FALSE,
						LocusEvidenceNature2D.EXACT, "certified-false")))),
				"certified nonexistence inside the declared domain");
		LocusExistenceStructure2D proved = LocusExistenceStructure2D.providerProved(
				G7BMetricFixtures.BRANCH, domain, List.of(lower), false, false,
				"h10-partial");
		assertNoTotalWithoutExistence(total("h10-partial", new LocusExistenceStructure2D(
				G7BMetricFixtures.BRANCH, domain, DomainTopology.BOUNDED, 0, List.of(
						proved.getCoverage().get(0), outside(upper)),
				proved.getContinuousValidComponents(), List.of(),
				Completeness.NOT_ESTABLISHED, "h10-partial")), "partial certificate");
	}

	/**
	 * H10-3: a whole periodic period is measured up to its closing driver position only as a
	 * recorded hypothesis. The ellipse keeps its CU1 value; a periodic scalar curve that
	 * does not close fails instead of measuring the jump; neither becomes closed.
	 */
	@Test
	void aWholePeriodClosesOnlyUnderARecordedHypothesis() {
		add("e=0");
		add("Qe=(3*cos(e),2*sin(e))");
		add("De={true,{0,2*pi,true,false}}");
		GeoLocusV2 ellipse = add("Le=LocusV2(Qe,e,De)");
		LocusMetricResult2D closed = ((GeoLocusMetricResult) add("Me=LocusLength(Le)"))
				.getMetricResult();
		assertEquals(MetricGuaranteeClass2D.ESTIMATED, closed.getGuaranteeClass(),
				closed.getDiagnostics().toString());
		assertEquals(15.86543958929059, value(closed), 2E-6);
		assertTrue(closed.isScalarAdmissible());
		assertConditionedWithoutAuthority(closed, ellipse, "ellipse");
		assertTrue(closed.getErrorEvidence().getAssumptions().stream().anyMatch(assumption ->
				assumption.startsWith("the curve closes at the seam of the periodic driver")));
		assertFalse(structure(ellipse).isClosedPeriodic());

		add("u=0");
		add("Qu=(u,u)");
		add("Du={true,{0,1,true,false}}");
		GeoLocusV2 open = add("Lu=LocusV2(Qu,u,Du)");
		LocusMetricResult2D jump = ((GeoLocusMetricResult) add("Mu=LocusLength(Lu)"))
				.getMetricResult();
		assertNotEquals(MetricComputationStatus.SUCCESS, jump.getComputationStatus(),
				jump.getDiagnostics().toString());
		assertEquals(MetricGuaranteeClass2D.NOT_ESTABLISHED, jump.getGuaranteeClass());
		assertConditionedWithoutAuthority(jump, open, "periodic curve that does not close");
		assertFalse(((GeoNumeric) add("bu=Length(Lu)")).isDefined());
	}

	/**
	 * H10-4: an excluded declared end is never evaluated: its total keeps the typed status
	 * of an improper limit that is not established, as in CU1.
	 */
	@Test
	void anExcludedDeclaredEndKeepsItsTypedStatus() {
		add("v=0");
		add("Qv=(v,v^2)");
		add("Dv={false,{0,1,true,false}}");
		GeoLocusV2 locus = add("Lv=LocusV2(Qv,v,Dv)");
		LocusMetricResult2D rich = ((GeoLocusMetricResult) add("Mv=LocusLength(Lv)"))
				.getMetricResult();
		assertEquals(MetricComputationStatus.LIMIT_NOT_ESTABLISHED,
				rich.getComputationStatus(), rich.getDiagnostics().toString());
		assertConditionedWithoutAuthority(rich, locus, "excluded end");
		assertFalse(((GeoNumeric) add("bv=Length(Lv)")).isDefined());
	}

	/**
	 * H10-5: a similarity image of a construction outside class v3 inherits its structure and
	 * is measured through its source: the image of the conditioned estimate is conditioned,
	 * with the value scaled by the length factor.
	 */
	@Test
	void similarityImagesOfAConditionedEstimateStayConditioned() {
		add("s=Segment((0,0),(1,0))");
		add("P=Point(s)");
		add("Q=(x(P),sin(2*pi*x(P)))");
		GeoLocusV2 sine = add("L=LocusV2(Q,P)");
		LocusMetricResult2D source = ((GeoLocusMetricResult) add("M=LocusLength(L)"))
				.getMetricResult();
		assertConditionedWithoutAuthority(source, sine, "source");
		GeoLocusV2 moved = add("T=Translate(L,(3,-2))");
		LocusMetricResult2D translated = ((GeoLocusMetricResult) add("MT=LocusLength(T)"))
				.getMetricResult();
		assertConditionedWithoutAuthority(translated, moved, "translation");
		assertEquals(value(source), value(translated),
				source.getErrorEvidence().getEstablishedAbsoluteAmount()
						+ translated.getErrorEvidence().getEstablishedAbsoluteAmount());
		GeoLocusV2 dilated = add("D=Dilate(L,2,(1,1))");
		LocusMetricResult2D doubled = ((GeoLocusMetricResult) add("MD=LocusLength(D)"))
				.getMetricResult();
		assertConditionedWithoutAuthority(doubled, dilated, "dilation");
		assertEquals(2 * value(source), value(doubled),
				2 * source.getErrorEvidence().getEstablishedAbsoluteAmount()
						+ doubled.getErrorEvidence().getEstablishedAbsoluteAmount());
	}

	private static LocusExistenceStructure2D structure(GeoLocusV2 locus) {
		return locus.getSemanticDefinition().getBranches().get(0).getExistenceStructure();
	}

	private static double value(LocusMetricResult2D rich) {
		return rich.getMetricValue().getFiniteValue().orElseThrow();
	}

	private static Coverage outside(LocusInterval2D interval) {
		return new Coverage(interval, CoverageState.UNRESOLVED,
				LocusCoverageCause2D.OPERATION_OUTSIDE_CLASS, LocusEvidenceNature2D.NONE,
				"h10/outside-class");
	}

	private static LocusExistenceStructure2D notEstablished(LocusInterval2D domain,
			LocusCoverageCause2D cause) {
		return LocusExistenceStructure2D.notEstablished(G7BMetricFixtures.BRANCH, domain,
				DomainTopology.BOUNDED, cause, "h10/" + cause);
	}

	private static LocusExistenceStructure2D structure(LocusInterval2D domain,
			List<Coverage> coverage) {
		return new LocusExistenceStructure2D(G7BMetricFixtures.BRANCH, domain,
				DomainTopology.BOUNDED, 0, coverage, List.of(), List.of(),
				Completeness.NOT_ESTABLISHED, "h10/fixture");
	}

	/** The diagonal (t, t) of length sqrt(2) on [0, 1], with an explicit structure. */
	private static LocusMetricResult2D total(String identity,
			LocusExistenceStructure2D structure) {
		LocusInterval2D domain = structure.getCanonicalDomain();
		DoubleFunction<LocusPoint2D> curve = t -> new LocusPoint2D(t, t);
		ExplicitNumericDomainProvider2D provider = new ExplicitNumericDomainProvider2D(
				"h10-parameter/v1", domain, Orientation.INCREASING, false, 1E-14);
		LocusBranch2D branch = new LocusBranch2D(G7BMetricFixtures.BRANCH, domain,
				structure, Orientation.INCREASING, "h10-fixture/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
		LocusDefinition2D definition = new LocusDefinition2D(identity, 1,
				DefinitionStatus.VALID, provider, List.of(branch),
				(source, semanticBranch, parameter, session) ->
						LocusEvaluation2D.valid(curve.apply(parameter), Regularity.UNKNOWN,
								semanticBranch.getQuality()),
				Determinism.POINTWISE_DETERMINISTIC, "h10-fixture/v1",
				new LocusInstrumentation2D());
		LocusMetricPolicy2D policy = LocusMetricPolicy2D.cu1PublicV2();
		LocusMetricInstrumentation2D instrumentation = new LocusMetricInstrumentation2D();
		LocusMetricSharedOwner2D owner = new LocusMetricSharedOwner2D(identity,
				instrumentation);
		try (LocusMetricOwnerLease2D lease = owner.acquireLease()) {
			return new LocusMetricEngine2D().compute(new TotalLocusMetricQuery(identity,
					definition.getSemanticRevision(), policy), definition,
					new LocusMetricCapabilityHierarchy2D(List.of(
							new PolicyV2LocusMetricCapability2D())), owner,
					LocusMetricIndexMode.LAZY_COMPONENT_REVISION, instrumentation,
					"h10-test");
		} catch (LocusMetricComponentBuildException exception) {
			return LocusMetricResults2D.failure(identity, definition.getSemanticRevision(),
					policy, exception.getComputationStatus(), Optional.empty(),
					exception.getDiagnostics());
		}
	}

	private static double simpson(DoubleUnaryOperator function, double lower,
			double upper, int cells) {
		double width = (upper - lower) / cells;
		double sum = function.applyAsDouble(lower) + function.applyAsDouble(upper);
		for (int index = 1; index < cells; index++) {
			sum += function.applyAsDouble(lower + index * width) * (index % 2 == 0 ? 2 : 4);
		}
		return sum * width / 3;
	}
}
