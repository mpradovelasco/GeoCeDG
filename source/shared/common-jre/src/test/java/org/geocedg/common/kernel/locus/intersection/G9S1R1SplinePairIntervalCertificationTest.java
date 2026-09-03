/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.geocedg.common.kernel.locus.ExplicitNumericDomainProvider2D;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusEvaluationSession2D;
import org.geocedg.common.kernel.locus.LocusEvaluator2D;
import org.geocedg.common.kernel.locus.LocusInstrumentation2D;
import org.geocedg.common.kernel.locus.LocusInterval2D;
import org.geocedg.common.kernel.locus.LocusLineage2D;
import org.geocedg.common.kernel.locus.LocusQuality2D;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.BranchProperty;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.DefinitionStatus;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Determinism;
import org.geocedg.common.kernel.locus.LocusSemanticMetadata2D.Orientation;
import org.geocedg.common.kernel.locus.LocusSimilarityEvaluator2D;
import org.geocedg.common.kernel.locus.LocusSimilarityTransform2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D
		.ComputationStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D
		.PairUniquenessStatus;
import org.geocedg.common.kernel.locus.intersection.SplinePairIntervalCertification2D
		.ClassCertificate;
import org.geocedg.common.kernel.locus.intersection.SplinePairIntervalCertification2D
		.ClassStatus;
import org.geocedg.common.kernel.locus.intersection.SplinePairIntervalCertification2D
		.Result;
import org.geocedg.common.kernel.locus.intersection.SplinePairIntervalCertification2D
		.RootCertificate;
import org.geocedg.common.kernel.spline.SplinePolynomialModel2D;
import org.geocedg.common.kernel.spline.SplineSemanticEvaluator2D;
import org.junit.jupiter.api.Test;

/**
 * Numerical certificate tests over exactly specified represented coefficients.
 * Private-model reflection is test-fixture construction, not a product input
 * seam or a claim that the ordinary floating interpolation solve is exact.
 */
final class G9S1R1SplinePairIntervalCertificationTest {
	private static final String BRANCH = "spline-v2/main";

	@Test
	void structuralRationalCoefficientBridgeProvesExactBounds() {
		BigDecimal[] numerators = {BigDecimal.ONE, BigDecimal.ONE.negate(),
				new BigDecimal(0.1), new BigDecimal(Double.MIN_VALUE),
				new BigDecimal(Double.MIN_VALUE).negate(), BigDecimal.ZERO,
				new BigDecimal(Double.MAX_VALUE)};
		for (BigDecimal numerator : numerators) {
			for (int denominator : new int[] {1, 3, 6, 479001600}) {
				SplineOutwardInterval2D enclosure = SplineIntervalModel2D
						.encloseCoefficient(numerator, denominator);
				BigDecimal divisor = BigDecimal.valueOf(denominator);
				assertTrue(new BigDecimal(enclosure.lower).multiply(divisor)
						.compareTo(numerator) <= 0);
				assertTrue(new BigDecimal(enclosure.upper).multiply(divisor)
						.compareTo(numerator) >= 0);
			}
		}
		SplineOutwardInterval2D third = SplineIntervalModel2D
				.encloseCoefficient(BigDecimal.ONE, 3);
		assertTrue(third.lower < third.upper);
		assertThrows(ArithmeticException.class, () -> SplineIntervalModel2D
				.encloseCoefficient(new BigDecimal(Double.MAX_VALUE).multiply(
						BigDecimal.valueOf(2)), 1));
		assertThrows(ArithmeticException.class, () -> SplineIntervalModel2D
				.encloseCoefficient(BigDecimal.ONE, 0));
	}

	@Test
	void outwardBasicOperationsContainExactDyadicResults() {
		double[] values = {-17.25, -0.1, 0, Double.MIN_VALUE, 0.3, 128.125};
		for (double a : values) {
			for (double b : values) {
				SplineOutwardInterval2D x = SplineOutwardInterval2D.point(a);
				SplineOutwardInterval2D y = SplineOutwardInterval2D.point(b);
				BigDecimal exactA = new BigDecimal(a);
				BigDecimal exactB = new BigDecimal(b);
				contains(x.add(y), exactA.add(exactB));
				contains(x.subtract(y), exactA.subtract(exactB));
				contains(x.multiply(y), exactA.multiply(exactB));
			}
		}
	}

	@Test
	void subnormalUnderflowAndSignedZeroRemainEnclosed() {
		SplineOutwardInterval2D smallest = SplineOutwardInterval2D.point(Double.MIN_VALUE);
		SplineOutwardInterval2D half = smallest.multiply(SplineOutwardInterval2D.point(0.5));
		contains(half, new BigDecimal(Double.MIN_VALUE).multiply(new BigDecimal(0.5)));
		assertTrue(half.lower <= 0 && half.upper >= Double.MIN_VALUE);
		assertTrue(SplineOutwardInterval2D.point(-0.0).containsZero());
		assertThrows(ArithmeticException.class, () ->
				SplineOutwardInterval2D.point(Double.MAX_VALUE)
						.multiply(SplineOutwardInterval2D.point(2)));
		assertThrows(ArithmeticException.class, () ->
				SplineOutwardInterval2D.point(Double.POSITIVE_INFINITY));
	}

	@Test
	void divisionEnclosesRationalValueAndRefusesZeroDivisor() {
		SplineOutwardInterval2D third = SplineOutwardInterval2D.point(1)
				.divide(SplineOutwardInterval2D.point(3));
		assertTrue(new BigDecimal(third.lower).multiply(BigDecimal.valueOf(3))
				.compareTo(BigDecimal.ONE) <= 0);
		assertTrue(new BigDecimal(third.upper).multiply(BigDecimal.valueOf(3))
				.compareTo(BigDecimal.ONE) >= 0);
		assertThrows(ArithmeticException.class, () -> SplineOutwardInterval2D.point(1)
				.divide(new SplineOutwardInterval2D(-Double.MIN_VALUE, Double.MIN_VALUE)));
	}

	@Test
	void singletonTransverseRootHasIndependentExistenceAndClassProof() {
		Result result = certify(horizontal(), vertical(0.5));
		assertTrue(result.isSupported());
		assertEquals(1, result.getRoots().size());
		ClassCertificate unique = unique(result);
		RootCertificate root = unique.getUniqueRoot();
		assertEquals(0.5, root.getFirstParameter(), 1E-12);
		assertEquals(0.5, root.getSecondParameter(), 1E-12);
		assertTrue(root.getFirstIsolationInterval().getLower()
				< root.getFirstRootEnclosure().getLower());
		assertTrue(root.getFirstIsolationInterval().getUpper()
				> root.getFirstRootEnclosure().getUpper());
		assertTrue(result.getBoxesVisited() > 0);
		assertTrue(result.getKrawczykAttempts() > 0);
	}

	@Test
	void oppositeGermsAreIndependentlyUniqueWithoutClaimingGlobalCompleteness() {
		LocusDefinition2D parabola = spline("A", new double[] {0, 1},
				new double[][] {{0, 0, 1, 0}},
				new double[][] {{0, 1, -1, 0.1875}}, false);
		Result result = certify(parabola, target());
		assertEquals(2, result.getRoots().size());
		assertEquals(ClassStatus.UNIQUE, status(result, 1));
		assertEquals(ClassStatus.UNIQUE, status(result, -1));
	}

	@Test
	void productiveCandidateEnumerationReversalPreservesExactSelectorTokenBindings() {
		LocusDefinition2D first = oppositeGermParabola();
		Publication forward = publish(first, target(), false);
		Publication reverse = publish(first, target(), true);
		assertTrue(forward.capability.submittedOrder.size() >= 2,
				"The actual polynomial capability must discover several candidates");
		ArrayList<String> reversedOrder = new ArrayList<>(forward.capability.submittedOrder);
		Collections.reverse(reversedOrder);
		assertNotEquals(forward.capability.submittedOrder, reverse.capability.submittedOrder);
		assertEquals(reversedOrder, reverse.capability.submittedOrder);
		assertEquals(2, admissibleBindings(forward.result, forward.ledger).size());
		assertEquals(admissibleBindings(forward.result, forward.ledger),
				admissibleBindings(reverse.result, reverse.ledger));
		assertEquals(forward.ledger.exportState(), reverse.ledger.exportState(),
				"The actual public resolver must bind identical selectors and opaque tokens");
	}

	@Test
	void syntheticGlobalCompleteDoesNotMaterializeActualMultipleGermClasses() {
		LocusDefinition2D first = periodicLoop(2);
		LocusDefinition2D second = horizontalTarget();
		Result proof = certify(first, second);
		assertEquals(ClassStatus.MULTIPLE, status(proof, 1));
		assertEquals(ClassStatus.MULTIPLE, status(proof, -1));
		Publication publication = publish(first, second, false);
		assertEquals(4, publication.result.getFiniteSolutions().size());
		assertEquals(Completeness.NOT_ESTABLISHED,
				publication.result.getCompletenessEvidence().getCompleteness());
		LocusIntersectionResult2D contrast = syntheticCompleteness(publication.result,
				Completeness.COMPLETE);
		assertEquals(Completeness.COMPLETE, contrast.getCompletenessEvidence().getCompleteness());
		assertEquals(publication.result.getFiniteSolutions(), contrast.getFiniteSolutions());
		assertTrue(admissibleBindings(contrast, publication.ledger).isEmpty());
		for (LocusIntersectionSolution2D root : contrast.getFiniteSolutions()) {
			assertFalse(publication.ledger.validatesCurrentToken(
					root.getIdentity().getRootToken()));
		}
	}

	@Test
	void syntheticGlobalIncompleteRetainsActuallyCertifiedLocalPairEligibility() {
		Publication publication = publish(oppositeGermParabola(), target(), false);
		assertEquals(Completeness.NOT_ESTABLISHED,
				publication.result.getCompletenessEvidence().getCompleteness());
		Map<String, String> actual = admissibleBindings(publication.result, publication.ledger);
		assertEquals(2, actual.size());
		LocusIntersectionResult2D contrast = syntheticCompleteness(publication.result,
				Completeness.INCOMPLETE);
		assertEquals(Completeness.INCOMPLETE,
				contrast.getCompletenessEvidence().getCompleteness());
		assertEquals(publication.result.getFiniteSolutions(), contrast.getFiniteSolutions());
		assertEquals(actual, admissibleBindings(contrast, publication.ledger));
	}

	@Test
	void multipleSameGermRootsDoNotBlockCertifiedOppositeSingleton() {
		LocusDefinition2D cubic = spline("A", new double[] {0, 0.5, 1},
				repeat(new double[] {0, 0, 1, 0}, 2),
				repeat(new double[] {1, -1.5, 0.6875, -0.09375}, 2), false);
		Result result = certify(cubic, target());
		assertEquals(3, result.getRoots().size());
		assertEquals(ClassStatus.MULTIPLE, status(result, 1));
		assertEquals(ClassStatus.UNIQUE, status(result, -1));
	}

	@Test
	void exactKnotGluePermitsRootProofAcrossBothSpanBoundaries() {
		Result result = certify(horizontal(), vertical(0.5));
		RootCertificate root = unique(result).getUniqueRoot();
		assertTrue(root.getFirstIsolationInterval().getLower() < 0.5);
		assertTrue(root.getFirstIsolationInterval().getUpper() > 0.5);
		assertTrue(root.getSecondIsolationInterval().getLower() < 0.5);
		assertTrue(root.getSecondIsolationInterval().getUpper() > 0.5);
	}

	@Test
	void nonzeroFloatingKnotDefectDoesNotBecomeExactGlue() {
		LocusDefinition2D broken = spline("A", new double[] {0, 0.5, 1},
				new double[][] {{0, 0, 1, 0}, {0, 0, 1, 1E-12}},
				repeat(new double[] {0, 0, 0, 0}, 2), false);
		Result result = certify(broken, vertical(0.5));
		assertFalse(result.getClasses().stream()
				.anyMatch(value -> value.getStatus() == ClassStatus.UNIQUE));
		assertTrue(result.getClasses().stream()
				.anyMatch(value -> value.getStatus() == ClassStatus.UNRESOLVED));
	}

	@Test
	void incompleteCoverageBudgetCannotClaimUniqueClass() {
		LocusDefinition2D first = horizontal();
		LocusDefinition2D second = vertical(0.5);
		LocusPairIntersectionPolicy2D ordinary = LocusPairIntersectionPolicy2D.initial(first,
				second);
		LocusPairIntersectionWorkBudget2D tiny = new LocusPairIntersectionWorkBudget2D(
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0);
		LocusPairIntersectionPolicy2D policy = new LocusPairIntersectionPolicy2D(
				"g9s1-r1/test-small-budget", ordinary.getFirstRootTolerance(),
				ordinary.getSecondRootTolerance(), ordinary.getFirstDeduplicationTolerance(),
				ordinary.getSecondDeduplicationTolerance(), ordinary.getResidualTolerance(),
				ordinary.getTangencyTolerance(), ordinary.getCoordinateTolerance(),
				ordinary.getCommonWorkBudget(), tiny);
		Result result = certify(first, second, policy);
		assertEquals(1, result.getBoxesVisited());
		assertTrue(result.getClasses().stream()
				.allMatch(value -> value.getStatus() == ClassStatus.UNRESOLVED));
	}

	@Test
	void nestedSimilaritiesUseOriginalCapturedMapRatherThanFlattenedCoefficients() {
		LocusDefinition2D first = horizontal();
		LocusDefinition2D second = vertical(0.5);
		LocusSimilarityTransform2D[] maps = {
				LocusSimilarityTransform2D.translation(1, -2),
				LocusSimilarityTransform2D.rotation(0.25, 0, 0),
				LocusSimilarityTransform2D.lineReflection(1, 1, 0),
				LocusSimilarityTransform2D.dilation(-2, 0.5, -0.5)};
		for (int index = 0; index < maps.length; index++) {
			first = transformed(first, "A/" + index, maps[index]);
			second = transformed(second, "B/" + index, maps[index]);
			RootCertificate root = unique(certify(first, second)).getUniqueRoot();
			assertEquals(0.5, root.getFirstParameter(), 1E-10);
			assertEquals(0.5, root.getSecondParameter(), 1E-10);
		}
	}

	@Test
	void largeTranslatedFlatteningCannotFabricateAnIntervalCertificate() {
		LocusSimilarityTransform2D huge = LocusSimilarityTransform2D.translation(0x1p54, 0);
		LocusDefinition2D first = transformed(horizontal(), "A/large", huge);
		LocusDefinition2D second = transformed(vertical(0.5), "B/large", huge);
		Result result = certify(first, second);
		assertFalse(result.getClasses().stream()
				.anyMatch(value -> value.getStatus() == ClassStatus.UNIQUE));
	}

	@Test
	void collapsedImageAndGenericEvaluatorAreNotPromoted() {
		LocusDefinition2D collapsed = transformed(horizontal(), "A/collapsed",
				LocusSimilarityTransform2D.dilation(0, 0, 0));
		assertFalse(certify(collapsed, vertical(0.5)).isSupported());
		LocusDefinition2D source = horizontal();
		LocusEvaluator2D generic = (definition, branch, parameter, session) ->
				source.evaluate(BRANCH, parameter, session);
		LocusDefinition2D unsupported = withEvaluator(source, "A/generic", generic);
		assertFalse(certify(unsupported, vertical(0.5)).isSupported());
	}

	@Test
	void canonicalSourceOrderProducesIdenticalCertificates() {
		LocusDefinition2D first = horizontal();
		LocusDefinition2D second = vertical(0.5);
		Result forward = certify(first, second);
		Result reverse = certify(second, first);
		assertEquals(forward.getBoxesVisited(), reverse.getBoxesVisited());
		assertEquals(forward.getKrawczykAttempts(), reverse.getKrawczykAttempts());
		RootCertificate a = unique(forward).getUniqueRoot();
		RootCertificate b = unique(reverse).getUniqueRoot();
		assertEquals(a.getGerm(), b.getGerm());
		assertEquals(a.getFirstParameter(), b.getFirstParameter());
		assertEquals(a.getSecondParameter(), b.getSecondParameter());
		assertEquals(a.getFirstIsolationInterval(), b.getFirstIsolationInterval());
	}

	@Test
	void exactPeriodicSeamChartHasOneCanonicalRootPerGerm() {
		Result result = certify(periodicLoop(1), horizontalTarget());
		assertEquals(ClassStatus.UNIQUE, status(result, 1));
		assertEquals(ClassStatus.UNIQUE, status(result, -1));
		assertEquals(2, result.getRoots().size());
		assertTrue(result.getRoots().stream().allMatch(root ->
				root.getFirstParameter() >= 0 && root.getFirstParameter() < 1));
		// The numerical representative may lie on either canonical side of the
		// seam. The certified chart, not that arbitrary approximation side,
		// must contain the same periodic preimage without duplicating it.
		assertTrue(result.getRoots().stream()
				.anyMatch(root -> root.getFirstRootEnclosure().contains(0, 0)
						|| root.getFirstRootEnclosure().contains(1, 0)));
	}

	@Test
	void repeatedPeriodicTraversalRetainsSameGermMultiplicity() {
		Result result = certify(periodicLoop(2), horizontalTarget());
		assertEquals(ClassStatus.MULTIPLE, status(result, 1));
		assertEquals(ClassStatus.MULTIPLE, status(result, -1));
	}

	private static void contains(SplineOutwardInterval2D interval, BigDecimal exact) {
		assertTrue(new BigDecimal(interval.lower).compareTo(exact) <= 0);
		assertTrue(new BigDecimal(interval.upper).compareTo(exact) >= 0);
	}

	private static Publication publish(LocusDefinition2D first, LocusDefinition2D second,
			boolean reverseCandidates) {
		LocusPairIntersectionQuery2D query = new LocusPairIntersectionQuery2D(first, second,
				"g9s1-r1/productive-enumeration-test", "regular",
				LocusPairIntersectionPolicy2D.initial(first, second));
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		LocusIntersectionTokenLedger2D.Evaluation evaluation = ledger.begin(
				"g9s1-r1/exact-test-query-owner", query.getSourcePairIdentity(),
				query.getConstructiveIntersectionLineage(), query.getTopologyContext());
		EnumerationCapability capability = new EnumerationCapability(reverseCandidates);
		// The same provisional lineage-token route as the public parent algorithm.
		// Eligible opaque tokens are allocated only by the actual D2 resolver/ledger.
		LocusPairRootTokenSource2D provisional = lineage -> LocusPairIdentity2D.solutionToken(
				query.getSourcePairIdentity(), query.getConstructiveIntersectionLineage(),
				query.getTopologyContext(), lineage + "/appearance-epoch-1");
		LocusIntersectionResult2D result = new LocusPairIntersectionSolver2D().intersect(query,
				query.isCallerOrderCanonical() ? first : second,
				query.isCallerOrderCanonical() ? second : first,
				new IntersectionSourceBinding2D(query), capability, provisional, evaluation);
		assertEquals(ComputationStatus.SUCCESS, result.getComputationStatus());
		assertTrue(capability.invoked, "The real capability wrapper must actually be consumed");
		ledger.commit(evaluation, result);
		return new Publication(result, ledger, capability);
	}

	private static Map<String, String> admissibleBindings(LocusIntersectionResult2D result,
			LocusIntersectionTokenLedger2D ledger) {
		Map<String, String> bindings = new TreeMap<>();
		for (LocusIntersectionSolution2D root : result.getFiniteSolutions()) {
			String token = root.getIdentity().getRootToken();
			if (!result.findPointAdmissibleSolution(token).isPresent()) {
				continue;
			}
			assertTrue(ledger.validatesCurrentToken(token));
			LocusPairIntersectionEvidence2D pair = root.getPairEvidence().orElseThrow();
			assertEquals(PairUniquenessStatus.CERTIFIED_UNIQUE,
					pair.getLocalIsolation().getUniqueness());
			String selector = root.getIdentity().getExplicitContinuationKey().orElseThrow();
			assertFalse(bindings.containsKey(selector));
			// Parameters and coordinates are comparison evidence only. No test helper
			// supplies them to selector construction or public token allocation.
			bindings.put(selector, token + "|" + pair.getFirst().getLocusIdentity()
					+ "|" + Double.toHexString(pair.getFirst().getSemanticParameter())
					+ "|" + pair.getSecond().getLocusIdentity()
					+ "|" + Double.toHexString(pair.getSecond().getSemanticParameter())
					+ "|" + Double.toHexString(root.getEvaluatedPoint().getX())
					+ "|" + Double.toHexString(root.getEvaluatedPoint().getY()));
		}
		return bindings;
	}

	private static LocusIntersectionResult2D syntheticCompleteness(
			LocusIntersectionResult2D actual, Completeness contrast) {
		// Gate-axis contrast ONLY: this does not claim that the numerical solver
		// established global completeness. All actual local proofs, roots and
		// ledger-issued identities are retained, without upgrading their evidence.
		IntersectionCompletenessEvidence2D previous = actual.getCompletenessEvidence();
		ArrayList<IntersectionDiagnostic2D> diagnostics = new ArrayList<>(
				previous.getDiagnostics());
		diagnostics.add(new IntersectionDiagnostic2D(DiagnosticCode.COVERAGE_NOT_ESTABLISHED,
				"TEST-ONLY synthetic global-status contrast; no new numerical coverage proof"));
		IntersectionCompletenessEvidence2D synthetic = new IntersectionCompletenessEvidence2D(
				contrast, previous.getMethod(), previous.getVerifiedRootCount(),
				previous.getCoveredComponentKeys(), diagnostics);
		return new LocusIntersectionResult2D(actual.getSourceBinding(),
				actual.getComputationStatus(), synthetic, actual.getGeometryKind(),
				actual.getCurrentness(), actual.getSupportLevel(), actual.getNumericGuarantee(),
				actual.getFiniteSolutions(), actual.getOverlapEvidence(),
				actual.getUnresolvedCandidateComponentKeys(), actual.getWork(),
				actual.getDiagnostics());
	}

	private static final class Publication {
		private final LocusIntersectionResult2D result;
		private final LocusIntersectionTokenLedger2D ledger;
		private final EnumerationCapability capability;

		private Publication(LocusIntersectionResult2D result, LocusIntersectionTokenLedger2D ledger,
				EnumerationCapability capability) {
			this.result = result;
			this.ledger = ledger;
			this.capability = capability;
		}
	}

	private static final class EnumerationCapability implements LocusPairIntersectionCapability2D {
		private final PiecewisePolynomialPairIntersectionCapability2D delegate =
				new PiecewisePolynomialPairIntersectionCapability2D();
		private final boolean reverse;
		private List<String> submittedOrder = List.of();
		private boolean invoked;

		private EnumerationCapability(boolean reverse) {
			this.reverse = reverse;
		}

		@Override
		public String getCapabilityId() {
			return delegate.getCapabilityId() + "/test-enumeration-wrapper";
		}

		@Override
		public boolean supports(LocusPairIntersectionContext2D context) {
			return delegate.supports(context);
		}

		@Override
		public LocusPairIntersectionCandidateSet2D isolate(LocusPairIntersectionContext2D context) {
			invoked = true;
			LocusPairIntersectionCandidateSet2D actual = delegate.isolate(context);
			ArrayList<LocusPairIntersectionCandidate2D> candidates =
					new ArrayList<>(actual.getCandidates());
			if (reverse) {
				Collections.reverse(candidates);
			}
			submittedOrder = candidates.stream().map(candidate ->
					candidate.getSolutionLineageKey() + "|"
							+ Double.toHexString(candidate.getFirstParameter()) + "|"
							+ Double.toHexString(candidate.getSecondParameter())).toList();
			return new LocusPairIntersectionCandidateSet2D(actual.getCompleteness(),
					actual.getCompletenessMethod(), actual.getGeometryKind(),
					actual.getSupportLevel(),
					actual.getNumericGuarantee(), actual.getCoveredComponentPairKeys(), candidates,
					actual.getOverlapEvidence(), actual.getDiagnostics());
		}
	}

	private static ClassCertificate unique(Result result) {
		ClassCertificate found = result.getClasses().stream()
				.filter(value -> value.getStatus() == ClassStatus.UNIQUE)
				.findFirst().orElse(null);
		assertNotNull(found, result.getDiagnostic());
		return found;
	}

	private static ClassStatus status(Result result, int germ) {
		ClassCertificate certificate = result.getClasses().stream()
				.filter(value -> value.getGerm() == germ).findFirst().orElseThrow();
		System.out.println("G9S1_R1_INTERVAL germ=" + germ + " status="
				+ certificate.getStatus() + " boxes=" + result.getBoxesVisited()
				+ " attempts=" + result.getKrawczykAttempts() + " roots="
				+ result.getRoots().size() + " diagnostic=" + certificate.getDiagnostic());
		return certificate.getStatus();
	}

	private static Result certify(LocusDefinition2D first, LocusDefinition2D second) {
		return certify(first, second, LocusPairIntersectionPolicy2D.initial(first, second));
	}

	private static Result certify(LocusDefinition2D first, LocusDefinition2D second,
			LocusPairIntersectionPolicy2D policy) {
		LocusPairIntersectionQuery2D query = new LocusPairIntersectionQuery2D(first, second,
				"g9s1-r1/certificate-test", "regular", policy);
		try (LocusEvaluationSession2D session = LocusEvaluationSession2D.memoizing(1024)) {
			return SplinePairIntervalCertification2D.certify(new LocusPairIntersectionContext2D(
					query, query.isCallerOrderCanonical() ? first : second,
					query.isCallerOrderCanonical() ? second : first, session,
					new LocusPairIntersectionInstrumentation2D(query.getPolicy())));
		}
	}

	private static LocusDefinition2D horizontal() {
		return spline("A", new double[] {0, 0.5, 1},
				repeat(new double[] {0, 0, 1, 0}, 2),
				repeat(new double[] {0, 0, 0, 0}, 2), false);
	}

	private static LocusDefinition2D vertical(double x) {
		return spline("B", new double[] {0, 0.5, 1},
				repeat(new double[] {0, 0, 0, x}, 2),
				repeat(new double[] {0, 0, 1, -0.5}, 2), false);
	}

	private static LocusDefinition2D target() {
		return spline("B", new double[] {0, 1}, new double[][] {{0, 0, 1, 0}},
				new double[][] {{0, 0, 0, 0}}, false);
	}

	private static LocusDefinition2D oppositeGermParabola() {
		return spline("A", new double[] {0, 1}, new double[][] {{0, 0, 1, 0}},
				new double[][] {{0, 1, -1, 0.1875}}, false);
	}

	private static LocusDefinition2D horizontalTarget() {
		return spline("B", new double[] {0, 1}, new double[][] {{0, 0, 4, -2}},
				new double[][] {{0, 0, 0, 0}}, false);
	}

	private static LocusDefinition2D transformed(LocusDefinition2D source,
			String identity, LocusSimilarityTransform2D transform) {
		return withEvaluator(source, identity, new LocusSimilarityEvaluator2D(source, transform));
	}

	private static LocusDefinition2D withEvaluator(LocusDefinition2D source,
			String identity, LocusEvaluator2D evaluator) {
		return new LocusDefinition2D(identity, 1, DefinitionStatus.VALID,
				source.getProvider(), source.getBranches(), evaluator,
				Determinism.POINTWISE_DETERMINISTIC, identity + "/captured",
				new LocusInstrumentation2D());
	}

	private static LocusDefinition2D spline(String identity, double[] knots,
			double[][] x, double[][] y, boolean periodic) {
		double[][][] coefficients = new double[x.length][2][];
		for (int span = 0; span < x.length; span++) {
			coefficients[span][0] = x[span].clone();
			coefficients[span][1] = y[span].clone();
		}
		SplinePolynomialModel2D model;
		try {
			Constructor<SplinePolynomialModel2D> constructor = SplinePolynomialModel2D.class
					.getDeclaredConstructor(int.class, double[].class, double[][][].class,
							boolean.class);
			constructor.setAccessible(true);
			model = constructor.newInstance(3, knots, coefficients, periodic);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Cannot construct exact represented test coefficients",
					exception);
		}
		LocusInterval2D domain = new LocusInterval2D(0, 1, true, !periodic);
		ExplicitNumericDomainProvider2D provider = new ExplicitNumericDomainProvider2D(
				identity + "/parameter", domain, Orientation.INCREASING, periodic, 0);
		EnumSet<BranchProperty> properties = EnumSet.of(BranchProperty.FINITE);
		if (periodic) {
			properties.add(BranchProperty.PERIODIC);
		}
		LocusBranch2D branch = new LocusBranch2D(BRANCH, domain, List.of(domain),
				Orientation.INCREASING, "exact-represented-coefficient-test",
				LocusLineage2D.unchanged(), properties, LocusQuality2D.analyticDoubleSemantic());
		SplineSemanticEvaluator2D evaluator = new SplineSemanticEvaluator2D(BRANCH, model);
		return new LocusDefinition2D(identity, 1, DefinitionStatus.VALID, provider,
				List.of(branch), evaluator, Determinism.POINTWISE_DETERMINISTIC,
				evaluator.getEvaluatorSignature(), new LocusInstrumentation2D());
	}

	private static double[][] repeat(double[] coefficients, int count) {
		double[][] result = new double[count][];
		for (int index = 0; index < count; index++) {
			result[index] = coefficients.clone();
		}
		return result;
	}

	private static LocusDefinition2D periodicLoop(int traversals) {
		int spans = 4 * traversals;
		double[] knots = new double[spans + 1];
		double[][] x = new double[spans][4];
		double[][] y = new double[spans][4];
		for (int span = 0; span < spans; span++) {
			knots[span] = span / (double) spans;
			double[] qx = substitute(new double[] {0.5, -1.5, 0, 1}, spans, -span);
			double[] qy = substitute(new double[] {-0.5, 0, 1.5, 0}, spans, -span);
			for (int power = 0; power < 4; power++) {
				switch (span % 4) {
				case 0:
					x[span][power] = qx[power];
					y[span][power] = qy[power];
					break;
				case 1:
					x[span][power] = -qy[power];
					y[span][power] = qx[power];
					break;
				case 2:
					x[span][power] = -qx[power];
					y[span][power] = -qy[power];
					break;
				default:
					x[span][power] = qy[power];
					y[span][power] = -qx[power];
					break;
				}
			}
		}
		knots[spans] = 1;
		return spline("A", knots, x, y, true);
	}

	private static double[] substitute(double[] polynomial, double scale, double offset) {
		double a = polynomial[0];
		double b = polynomial[1];
		double c = polynomial[2];
		double d = polynomial[3];
		return new double[] {a * scale * scale * scale,
				3 * a * scale * scale * offset + b * scale * scale,
				3 * a * scale * offset * offset + 2 * b * scale * offset + c * scale,
				a * offset * offset * offset + b * offset * offset + c * offset + d};
	}
}
