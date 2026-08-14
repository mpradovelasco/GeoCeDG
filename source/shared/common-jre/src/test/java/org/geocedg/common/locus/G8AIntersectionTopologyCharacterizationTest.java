/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusV2Factory;
import org.geocedg.common.kernel.locus.LocusV2Mode;
import org.geocedg.common.locus.G8AIntersectionNumerics.FactorizationProof;
import org.geocedg.common.locus.G8AIntersectionNumerics.Problem;
import org.geocedg.common.locus.G8AIntersectionNumerics.RootProof;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Completeness;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.DomainLocation;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.GeometryKind;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.Result;
import org.geocedg.common.locus.G8AIntersectionSemanticModel.WorkBudget;
import org.geocedg.common.locus.G8ATargetAdapters.LineTarget;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.junit.jupiter.api.Test;

/** Branch, component, seam, cusp, collapse and discontinuity probes. */
class G8AIntersectionTopologyCharacterizationTest extends BaseUnitTest {

	@Test
	void multipleBranchesKeepDistinctBindingsAndConstructiveTokens() {
		ExplicitNumericDomainProvider2D provider = provider(-1, 1, false);
		List<LocusBranch2D> branches = List.of(
				fullBranch("upper", provider), fullBranch("lower", provider));
		GeoLocusV2 locus = LocusV2Factory.createAnalytic(LocusV2Mode.V2,
				getConstruction(), "g8a-multibranch", new GeoNumeric(getConstruction()),
				provider, branches, (source, branch, parameter, session) ->
						new LocusPoint2D(parameter,
								branch.getBranchKey().equals("upper")
										? parameter : -parameter),
				"g8a-multibranch/v1");
		Result upper = solve(locus, "upper", "upper-component", -1, 1,
				new LineTarget("axis", 0, 1, 0),
				new FactorizationProof(1,
						List.of(new RootProof("upper-root", 0, 1)), false,
						"upper h(t)=t"), false);
		Result lower = solve(locus, "lower", "lower-component", -1, 1,
				new LineTarget("axis", 0, 1, 0),
				new FactorizationProof(-1,
						List.of(new RootProof("lower-root", 0, 1)), false,
						"lower h(t)=-t"), false);
		assertEquals(upper.solutions().get(0).point(),
				lower.solutions().get(0).point());
		assertNotEquals(upper.solutions().get(0).branchKey(),
				lower.solutions().get(0).branchKey());
		assertNotEquals(upper.solutions().get(0).durableIdentity().rootToken(),
				lower.solutions().get(0).durableIdentity().rootToken());
	}

	@Test
	void disconnectedComponentsAreSolvedSeparatelyWithoutCrossingInvalidGap() {
		ExplicitNumericDomainProvider2D provider = provider(-2, 2, false);
		LocusBranch2D branch = branch("disconnected", provider,
				List.of(new LocusInterval2D(-2, -0.5, true, true),
						new LocusInterval2D(0.5, 2, true, true)));
		GeoLocusV2 locus = locus("g8a-disconnected", provider, List.of(branch),
				(source, semanticBranch, parameter, session) ->
						new LocusPoint2D(parameter, parameter * parameter - 1));
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.reference()) {
			assertFalse(locus.evaluate("disconnected", 0, session).isValid());
		}
		FactorizationProof proof = new FactorizationProof(1,
				List.of(new RootProof("left", -1, 1),
						new RootProof("right", 1, 1)), false, "t^2-1");
		LineTarget target = new LineTarget("axis", 0, 1, 0);
		Result left = solve(locus, "disconnected", "component-left", -2, -0.5,
				target, proof, false);
		Result right = solve(locus, "disconnected", "component-right", 0.5, 2,
				target, proof, false);
		assertEquals(-1,
				left.solutions().get(0).revisionEvidence().semanticParameter());
		assertEquals(1,
				right.solutions().get(0).revisionEvidence().semanticParameter());
		assertNotEquals(left.solutions().get(0).componentKey(),
				right.solutions().get(0).componentKey());
	}

	@Test
	void isolatedAndEmptyComponentsHaveExplicitSetSemantics() {
		ExplicitNumericDomainProvider2D provider = provider(-1, 1, false);
		LocusBranch2D isolated = branch("isolated", provider,
				List.of(new LocusInterval2D(0, 0, true, true)));
		GeoLocusV2 isolatedLocus = locus("g8a-isolated", provider,
				List.of(isolated), (source, branch, parameter, session) ->
						new LocusPoint2D(0, 0));
		Result hit = solve(isolatedLocus, "isolated", "isolated-component", 0,
				0, new LineTarget("x-axis", 0, 1, 0),
				new FactorizationProof(1,
						List.of(new RootProof("isolated-root", 0, 1)), false,
						"declared isolated incidence"), false);
		assertEquals(DomainLocation.ISOLATED_COMPONENT,
				hit.solutions().get(0).classification().domainLocation());

		LocusBranch2D empty = branch("empty", provider, List.of());
		GeoLocusV2 emptyLocus = locus("g8a-empty", provider, List.of(empty),
				(source, branch, parameter, session) -> {
					throw new AssertionError("empty component must not evaluate");
				});
		Result noHit = solve(emptyLocus, "empty", "no-component", -1, 1,
				new LineTarget("x-axis", 0, 1, 0),
				new FactorizationProof(1, List.of(), false,
						"empty valid-component list"), false);
		assertEquals(GeometryKind.EMPTY, noHit.geometryKind());
		assertEquals(Completeness.COMPLETE, noHit.completeness());
		assertEquals(0, noHit.work().semanticEvaluatorCalls());
	}

	@Test
	void cuspContactRetainsEstablishedOrderWithoutRegularityFiction() {
		ExplicitNumericDomainProvider2D provider = provider(-1, 1, false);
		GeoLocusV2 locus = locus("g8a-cusp", provider,
				List.of(fullBranch("cusp", provider)),
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter * parameter,
								parameter * parameter * parameter));
		Result result = solve(locus, "cusp", "cusp-component", -1, 1,
				new LineTarget("vertical-axis", 1, 0, 0),
				new FactorizationProof(1,
						List.of(new RootProof("cusp-root", 0, 2)), false,
						"x(t)=t^2"), false);
		assertEquals(2, result.solutions().get(0).classification()
				.establishedMultiplicity());
		assertEquals(0, result.solutions().get(0).point().getX());
	}

	@Test
	void collapsedComponentOnTargetIsOverlapNotRepeatedPointSample() {
		ExplicitNumericDomainProvider2D provider = provider(-1, 1, false);
		GeoLocusV2 locus = locus("g8a-collapsed", provider,
				List.of(fullBranch("collapsed", provider)),
				(source, branch, parameter, session) -> new LocusPoint2D(0, 0));
		Result result = solve(locus, "collapsed", "collapsed-component", -1, 1,
				new LineTarget("vertical-axis", 1, 0, 0),
				new FactorizationProof(1, List.of(), true,
						"x(t) identically zero"), false);
		assertEquals(GeometryKind.OVERLAP, result.geometryKind());
		assertTrue(result.solutions().isEmpty());
		assertEquals(1, result.work().overlapComponentsDetected());
	}

	@Test
	void periodicEndpointRepresentationsCanonicalizeToOneSeamSolution() {
		double period = 2 * Math.PI;
		ExplicitNumericDomainProvider2D provider = provider(0, period, true);
		GeoLocusV2 locus = locus("g8a-periodic", provider,
				List.of(fullBranch("periodic", provider)),
				(source, branch, parameter, session) ->
						new LocusPoint2D(Math.cos(parameter), Math.sin(parameter)));
		FactorizationProof proof = new FactorizationProof(1,
				List.of(new RootProof("seam-lower", 0, 1),
						new RootProof("seam-upper", period, 1)), false,
				"cos(t)-1=0 has equivalent seam representatives");
		Result result = solve(locus, "periodic", "periodic-component", 0,
				period, new LineTarget("seam-tangent", 1, 0, -1), proof, true);
		assertEquals(1, result.solutions().size());
		assertEquals(DomainLocation.PERIODIC_SEAM,
				result.solutions().get(0).classification().domainLocation());
	}

	@Test
	void nonfiniteGapCannotBecomeCompleteEmptyOrStaleFiniteGeometry() {
		ExplicitNumericDomainProvider2D provider = provider(-1, 1, false);
		GeoLocusV2 locus = locus("g8a-nonfinite-gap", provider,
				List.of(fullBranch("gap", provider)),
				(source, branch, parameter, session) -> parameter == 0
						? new LocusPoint2D(Double.NaN, Double.NaN)
						: new LocusPoint2D(parameter, parameter));
		Result result = solve(locus, "gap", "gap-component", -1, 1,
				new LineTarget("axis", 0, 1, 0),
				new FactorizationProof(1,
						List.of(new RootProof("invalid-gap-candidate", 0, 1)),
						false, "candidate lies at invalid evaluation"), false);
		assertEquals(GeometryKind.UNRESOLVED, result.geometryKind());
		assertEquals(Completeness.INCOMPLETE, result.completeness());
		assertTrue(result.solutions().isEmpty());
	}

	private GeoLocusV2 locus(String identity,
			ExplicitNumericDomainProvider2D provider, List<LocusBranch2D> branches,
			org.geocedg.common.kernel.locus.LocusPointFunction2D function) {
		return LocusV2Factory.createAnalytic(LocusV2Mode.V2, getConstruction(),
				identity, new GeoNumeric(getConstruction()), provider, branches,
				function, identity + "/evaluator-v1");
	}

	private Result solve(GeoLocusV2 locus, String branchKey,
			String componentKey, double lower, double upper, LineTarget target,
			FactorizationProof proof, boolean periodic) {
		Problem problem = new Problem(locus.getLocusIdentity() + "+"
				+ target.identity(), locus.getLocusIdentity(),
				locus.getSemanticRevision(), target.identity(), 1, branchKey,
				branchKey + "/lineage-v1", componentKey, "topology-1", lower,
				upper, true, true, periodic, parameter -> {
					try (LocusEvaluationSession2D session =
							LocusEvaluationSession2D.reference()) {
						LocusEvaluation2D evaluation = locus.evaluate(branchKey,
								parameter, session);
						if (!evaluation.isValid()) {
							throw new IllegalStateException(evaluation.getDiagnostic());
						}
						return evaluation.getPoint();
					}
				}, target, proof,
				G8AIntersectionSemanticModel.Policy.measuredCandidate(),
				WorkBudget.measuredCandidate());
		return G8AIntersectionNumerics.analyticFactorization(problem);
	}

	private static ExplicitNumericDomainProvider2D provider(double lower,
			double upper, boolean periodic) {
		return new ExplicitNumericDomainProvider2D("g8a-topology-provider/v1",
				new LocusInterval2D(lower, upper, true, true),
				Orientation.INCREASING, periodic, 1E-14);
	}

	private static LocusBranch2D fullBranch(String key,
			ExplicitNumericDomainProvider2D provider) {
		return LocusV2Factory.fullDomainBranch(key, provider,
				"g8a-topology-lineage/v1",
				Collections.singleton(BranchProperty.FINITE));
	}

	private static LocusBranch2D branch(String key,
			ExplicitNumericDomainProvider2D provider,
			List<LocusInterval2D> components) {
		return new LocusBranch2D(key, provider.getDeclaredDomain(), components,
				Orientation.INCREASING, "g8a-topology-lineage/v1",
				LocusLineage2D.unchanged(), EnumSet.of(BranchProperty.FINITE),
				LocusQuality2D.analyticDoubleSemantic());
	}
}
