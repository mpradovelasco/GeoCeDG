/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.locus.LocusEvaluation2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusPoint2D;
import org.geocedg.common.kernel.locus.StablePathDomainProvider2D;
import org.geocedg.common.locus.G8CCharacterizationSupport.Completeness;
import org.geocedg.common.locus.G8CCharacterizationSupport.ContinuationStatus;
import org.geocedg.common.locus.G8CCharacterizationSupport.LocalIsolation;
import org.geocedg.common.locus.G8CCharacterizationSupport.OverlapStatus;
import org.geocedg.common.locus.G8CCharacterizationSupport.PairEvidence;
import org.geogebra.common.BaseUnitTest;
import org.junit.jupiter.api.Test;

/** Dual-semantic-parameter probes for the proposed G8C2 contract. */
class G8CLocusLocusCharacterizationTest extends BaseUnitTest {

	@Test
	void oneScopedSessionEvaluatesBothCurrentLocusRevisions() {
		G8AIntersectionFixtures.Fixture first = parabola("pair-first");
		G8AIntersectionFixtures.Fixture second = horizontal("pair-second");
		try (LocusEvaluationSession2D session =
				LocusEvaluationSession2D.memoizing(16)) {
			LocusEvaluation2D firstValue = first.locus().evaluate(first.branchKey(),
					1, session);
			LocusEvaluation2D secondValue = second.locus().evaluate(
					second.branchKey(), 1, session);
			assertTrue(firstValue.isValid());
			assertTrue(secondValue.isValid());
			assertEquals(firstValue.getPoint(), secondValue.getPoint());
			assertEquals(2, session.getCoherentRevisions().size());
		}
	}

	@Test
	void transversePairUsesNormalizedTwoSidedJacobianDeterminant() {
		double determinant = G8CCharacterizationSupport
				.normalizedTangentDeterminant(new LocusPoint2D(1, 2),
						new LocusPoint2D(1, 0));
		assertEquals(2 / Math.sqrt(5), Math.abs(determinant), 2E-15);
	}

	@Test
	void tangentPairIsNotDetectedBySignChangeOrRawSpeed() {
		double determinant = G8CCharacterizationSupport
				.normalizedTangentDeterminant(new LocusPoint2D(1, 0),
						new LocusPoint2D(7, 0));
		assertEquals(0, determinant, 0);
	}

	@Test
	void localIsolationRequiresUniquePairRegionAndRegularJacobian() {
		PairEvidence established = evidence(1, new LocusPoint2D(1, 2),
				new LocusPoint2D(1, 0), "root-east");
		PairEvidence twoCandidates = evidence(2, new LocusPoint2D(1, 2),
				new LocusPoint2D(1, 0), "root-east");
		PairEvidence tangent = evidence(1, new LocusPoint2D(1, 0),
				new LocusPoint2D(1, 0), "root-tangent");
		assertEquals(LocalIsolation.ESTABLISHED,
				established.localIsolation());
		assertEquals(LocalIsolation.NOT_ESTABLISHED,
				twoCandidates.localIsolation());
		assertEquals(LocalIsolation.NOT_ESTABLISHED, tangent.localIsolation());
	}

	@Test
	void geometricSourceOrderDoesNotChangeDurableToken() {
		String forward = G8CCharacterizationSupport.durablePairToken("locus-A",
				"locus-B", "root-east", "epoch-1");
		String reversed = G8CCharacterizationSupport.durablePairToken("locus-B",
				"locus-A", "root-east", "epoch-1");
		assertEquals(forward, reversed);
	}

	@Test
	void argumentReversalSwapsParameterEvidenceButNotIdentity() {
		PairEvidence forward = evidence(1, new LocusPoint2D(1, 2),
				new LocusPoint2D(1, 0), "root-east");
		PairEvidence reversed = forward.reversed();
		assertEquals(forward.durableToken(), reversed.durableToken());
		assertEquals(forward.firstIdentity(), reversed.secondIdentity());
		assertEquals(forward.firstParameter(), reversed.secondParameter(), 0);
		assertEquals(-forward.normalizedDeterminant(),
				reversed.normalizedDeterminant(), 0);
	}

	@Test
	void identicalCoordinatesFromDistinctPairPreimagesKeepDistinctTokens() {
		String first = G8CCharacterizationSupport.durablePairToken("self-A",
				"axis-B", "preimage-negative", "epoch-1");
		String second = G8CCharacterizationSupport.durablePairToken("self-A",
				"axis-B", "preimage-positive", "epoch-1");
		assertNotEquals(first, second);
	}

	@Test
	void matchingSamplesCanOnlySuspectOverlapWithoutSemanticMap() {
		assertEquals(OverlapStatus.OVERLAP_SUSPECTED_NOT_ESTABLISHED,
				G8CCharacterizationSupport.overlapStatus(false, false, 100));
		assertEquals(OverlapStatus.OVERLAP_ESTABLISHED,
				G8CCharacterizationSupport.overlapStatus(true, true, 0));
	}

	@Test
	void reversedReparameterizationCanEstablishSameImageOverlap() {
		for (double parameter : new double[] {-1, -0.5, 0, 0.5, 1}) {
			LocusPoint2D first = new LocusPoint2D(parameter,
					parameter * parameter);
			double reversedParameter = -parameter / 2;
			LocusPoint2D second = new LocusPoint2D(-2 * reversedParameter,
					4 * reversedParameter * reversedParameter);
			assertEquals(first, second);
		}
	}

	@Test
	void periodicSeamCanonicalizationIsTwoSidedRevisionEvidence() {
		StablePathDomainProvider2D first = StablePathDomainProvider2D.circle(
				"first-angle", new LocusPoint2D(0, 0), 1, 1E-14);
		StablePathDomainProvider2D second = StablePathDomainProvider2D.circle(
				"second-angle", new LocusPoint2D(0, 0), 2, 1E-14);
		assertEquals(first.canonicalize(-Math.PI), first.canonicalize(Math.PI), 0);
		assertEquals(second.canonicalize(-Math.PI), second.canonicalize(Math.PI),
				0);
	}

	@Test
	void currentG6ProviderContractRejectsUnboundedSemanticIntervals() {
		assertThrows(IllegalArgumentException.class, () -> new LocusInterval2D(
				Double.NEGATIVE_INFINITY, 1, false, true));
		assertThrows(IllegalArgumentException.class, () -> new LocusInterval2D(-1,
				Double.POSITIVE_INFINITY, true, false));
	}

	@Test
	void optionBPairAdmissibilityIsOrthogonalToGlobalCompleteness() {
		for (Completeness completeness : Completeness.values()) {
			assertTrue(G8CCharacterizationSupport.pointAdmissible(true, true,
					LocalIsolation.ESTABLISHED, ContinuationStatus.ESTABLISHED,
					completeness));
		}
	}

	@Test
	void ambiguityAndUnisolatedTangencyRemainPointInadmissible() {
		assertTrue(!G8CCharacterizationSupport.pointAdmissible(true, true,
				LocalIsolation.ESTABLISHED, ContinuationStatus.AMBIGUOUS,
				Completeness.NOT_ESTABLISHED));
		assertTrue(!G8CCharacterizationSupport.pointAdmissible(true, true,
				LocalIsolation.NOT_ESTABLISHED, ContinuationStatus.ESTABLISHED,
				Completeness.NOT_ESTABLISHED));
	}

	private PairEvidence evidence(int rootsInRectangle,
			LocusPoint2D firstDerivative, LocusPoint2D secondDerivative,
			String lineage) {
		return G8CCharacterizationSupport.pairEvidence("locus-A", 3,
				"A-main", "A-component-0", 1, "locus-B", 5, "B-main",
				"B-component-0", 1, new LocusPoint2D(1, 1),
				new LocusPoint2D(1, 1), firstDerivative, secondDerivative,
				rootsInRectangle, lineage, "epoch-1");
	}

	private G8AIntersectionFixtures.Fixture parabola(String identity) {
		return G8AIntersectionFixtures.create(getConstruction(), identity, -2, 2,
				true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, parameter * parameter),
				parameter -> new LocusPoint2D(1, 2 * parameter));
	}

	private G8AIntersectionFixtures.Fixture horizontal(String identity) {
		return G8AIntersectionFixtures.create(getConstruction(), identity, -2, 2,
				true, true, false,
				(source, branch, parameter, session) ->
						new LocusPoint2D(parameter, 1),
				parameter -> new LocusPoint2D(1, 0));
	}
}
