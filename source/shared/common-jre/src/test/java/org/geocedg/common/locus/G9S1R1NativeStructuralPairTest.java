/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoSplineV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.junit.jupiter.api.Test;

/**
 * Native command-backed structural spline pair gates, without reflected models.
 * Materialization assertions require the actual shared R1 certificate and ledger.
 */
final class G9S1R1NativeStructuralPairTest extends G9U0PublicSurfaceTestBase {

	@Test
	void structuralSplineRetainsHistoricalImplicitTangencyBarrier() {
		getKernel().setContinuous(false);
		add("SA=(-2,0)");
		add("SB=(-2/3,0)");
		add("SC=(2/3,0)");
		add("SD=(2,0)");
		GeoLocusV2 spline = add("S=SplineV2({SA,SB,SC,SD},3)");
		var model = ((AlgoSplineV2) spline.getParentAlgorithm()).getPolynomialModel();
		BigDecimal denominator = BigDecimal.valueOf(model.getCoefficientDenominator());
		for (int span = 0; span < model.getSpanCount(); span++) {
			for (BigDecimal coefficient : model.getExactCoefficientNumerators(span, 1)) {
				assertEquals(0, coefficient.signum());
			}
			BigDecimal[] x = model.getExactCoefficientNumerators(span, 0);
			// On [0,1], this exact lower bound proves x'(u)>3. Thus the
			// semantic equation (x(u)^2-1)^2=0 has only the two double roots;
			// no transverse intersection can exist between them or nearby.
			BigDecimal derivativeLower = x[2].subtract(x[1].abs().multiply(
					BigDecimal.valueOf(2))).subtract(x[0].abs().multiply(
					BigDecimal.valueOf(3)));
			assertTrue(derivativeLower.compareTo(denominator.multiply(
					BigDecimal.valueOf(3))) > 0);
		}
		assertTrue(model.getExactCoefficientNumerators(0, 0)[3]
				.compareTo(denominator.negate()) < 0);
		BigDecimal upperX = BigDecimal.ZERO;
		for (BigDecimal coefficient : model.getExactCoefficientNumerators(
				model.getSpanCount() - 1, 0)) {
			upperX = upperX.add(coefficient);
		}
		assertTrue(upperX.compareTo(denominator) > 0);
		GeoNumeric inner = add("innerRadius=0.25");
		GeoNumeric outer = add("outerRadius=2.25");
		add("implicit=ImplicitCurve((x^2-innerRadius)*(x^2-outerRadius)+y)");
		GeoLocusIntersectionResult rich = add("R=Intersect(S,implicit)");
		assertEquals(4, eligible(rich).size());
		List<GeoPoint> points = new java.util.ArrayList<>();
		for (LocusIntersectionSolution2D root : eligible(rich)) {
			points.add(materialize(rich, "X" + points.size(), root));
		}
		inner.setValue(1);
		outer.setValue(1);
		inner.updateCascade();
		outer.updateCascade();
		String roots = finite(rich).stream().map(root -> {
			var evidence = root.getRevisionEvidence();
			return "u=" + evidence.getSemanticParameter() + " x="
					+ root.getEvaluatedPoint().getX() + " contact="
					+ root.getClassification().getContactClass() + " isolation="
					+ evidence.getLocalIsolationStatus() + " germ="
					+ evidence.getCurrentRootGerm() + " eligible="
					+ rich.isPointAdmissible(root.getIdentity().getRootToken());
		}).collect(Collectors.joining("\n"));
		assertTrue(points.stream().anyMatch(point -> !point.isDefined()),
				roots + "\nfirstSpanX=" + java.util.Arrays.toString(model.getCoefficients(0, 0))
						+ "\ndiagnostics=" + rich.getIntersectionResult().getDiagnostics());
		assertEquals(2, finite(rich).size(), roots);
		assertTrue(eligible(rich).isEmpty(), roots);
		assertTrue(points.stream().noneMatch(GeoPoint::isDefined), roots);
		assertTrue(finite(rich).stream().allMatch(root -> root.getClassification().getContactClass()
				== org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D
						.ContactClass.TANGENT_ESTABLISHED), roots);
	}

	@Test
	void nativePeriodicLoopAndStraightSplineCertifyTwoCurrentSingletonGerms() {
		GeoLocusV2 loop = loop(false);
		horizontal(0.25);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertTrue(loop.getSemanticDefinition().getProvider().isPeriodic());
		assertEquals(2, ((AlgoSplineV2) loop.getParentAlgorithm())
				.getPolynomialModel().getStructuralContinuityOrder());
		assertEquals(2, finite(rich).size(), diagnostic(rich));
		assertEquals(2, eligible(rich).size(), diagnostic(rich));
		assertEquals(Completeness.NOT_ESTABLISHED,
				rich.getIntersectionResult().getCompletenessEvidence().getCompleteness());
		GeoPoint first = materialize(rich, "X", eligible(rich).get(0));
		GeoPoint second = materialize(rich, "Y", eligible(rich).get(1));
		assertTrue(first.isDefined());
		assertTrue(second.isDefined());
		assertEquals(0.25, first.getInhomY(), 1E-9);
		assertEquals(0.25, second.getInhomY(), 1E-9);
		assertNotEquals(selectedToken(first), selectedToken(second));
	}

	@Test
	void existingPairChildrenConsumeOnePublishedSnapshotWithoutAnotherSolve() {
		GeoLocusV2 first = loop(false);
		horizontal(0.25);
		GeoLocusV2 second = (GeoLocusV2) requireLookup("T");
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(2, eligible(rich).size(), diagnostic(rich));
		GeoPoint x = materialize(rich, "X", eligible(rich).get(0));
		GeoPoint y = materialize(rich, "Y", eligible(rich).get(1));
		var snapshot = rich.getIntersectionResult();
		long firstCalls = first.getSemanticDefinition().getInstrumentation().getEvaluatorCalls();
		long secondCalls = second.getSemanticDefinition().getInstrumentation().getEvaluatorCalls();
		for (int update = 0; update < 32; update++) {
			x.getParentAlgorithm().compute();
			y.getParentAlgorithm().compute();
			assertTrue(x.isDefined() && y.isDefined());
			assertSame(snapshot, rich.getIntersectionResult());
		}
		assertEquals(firstCalls,
				first.getSemanticDefinition().getInstrumentation().getEvaluatorCalls());
		assertEquals(secondCalls,
				second.getSemanticDefinition().getInstrumentation().getEvaluatorCalls());
	}

	@Test
	void nativePeriodicSeamHasOneOwnerAndRetainsPointsAcrossRegularCrossing() {
		GeoLocusV2 loop = loop(false);
		horizontal(0);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(2, eligible(rich).size(), diagnostic(rich));
		assertEquals(1, finite(rich).stream().filter(root ->
				parameter(root, loop) == 0).count(), diagnostic(rich));
		assertTrue(finite(rich).stream().allMatch(root -> parameter(root, loop) < 1));
		GeoPoint first = materialize(rich, "X", eligible(rich).get(0));
		GeoPoint second = materialize(rich, "Y", eligible(rich).get(1));
		Set<String> tokens = tokens(rich);
		PersistentGeoId firstId = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(first);
		for (double height : new double[] {0.05, 0, -0.05, -0.125, 0, 0.125}) {
			move("h", height);
			assertEquals(2, eligible(rich).size(), diagnostic(rich));
			assertEquals(tokens, tokens(rich));
			assertTrue(first.isDefined(), diagnostic(rich));
			assertTrue(second.isDefined(), diagnostic(rich));
			assertSame(first, requireLookup("X"));
			assertEquals(firstId, getConstruction().getSpatialIdentityRegistry()
					.getPersistentGeoId(first));
			assertEquals(height, first.getInhomY(), 1E-9);
			assertEquals(height, second.getInhomY(), 1E-9);
		}
	}

	@Test
	void nativeBothSourceInternalKnotRootMaterializesExactlyOnce() {
		GeoLocusV2 first = straight();
		GeoLocusV2 second = vertical(0, false);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(1, finite(rich).size(), diagnostic(rich));
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		LocusIntersectionSolution2D root = eligible(rich).get(0);
		assertEquals(0.5, parameter(root, first), 0);
		assertEquals(0.5, parameter(root, second), 0);
		GeoPoint point = materialize(rich, "X", root);
		assertTrue(point.isDefined());
		assertEquals(0, point.getInhomX(), 1E-12);
		assertEquals(0, point.getInhomY(), 1E-12);
	}

	@Test
	void nativeOneSourceKnotRootUsesStructuralChartWithoutDuplicatePoint() {
		GeoLocusV2 first = straight();
		GeoLocusV2 second = vertical(0, true);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(1, finite(rich).size(), diagnostic(rich));
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		LocusIntersectionSolution2D root = eligible(rich).get(0);
		assertEquals(0.5, parameter(root, first), 0);
		double targetParameter = parameter(root, second);
		double[] targetKnots = ((AlgoSplineV2) second.getParentAlgorithm())
				.getPolynomialModel().getKnots();
		assertTrue(targetParameter > targetKnots[1] && targetParameter < targetKnots[2]);
		assertTrue(materialize(rich, "X", root).isDefined());
	}

	@Test
	void nativeOnlySecondCallerSourceKnotHasTheSameCertifiedSemanticRoot() {
		GeoLocusV2 atKnot = straight();
		GeoLocusV2 betweenKnots = vertical(0, true);
		GeoLocusIntersectionResult reverse = add("R=Intersect(T,S)");
		assertEquals(1, finite(reverse).size(), diagnostic(reverse));
		assertEquals(1, eligible(reverse).size(), diagnostic(reverse));
		LocusIntersectionSolution2D root = eligible(reverse).get(0);
		assertEquals(0.5, parameter(root, atKnot), 0);
		double[] knots = ((AlgoSplineV2) betweenKnots.getParentAlgorithm())
				.getPolynomialModel().getKnots();
		assertTrue(parameter(root, betweenKnots) > knots[1]
				&& parameter(root, betweenKnots) < knots[2]);
		GeoLocusIntersectionResult forward = add("V=Intersect(S,T)");
		assertEquals(selectors(forward), selectors(reverse));
		assertEquals(parameters(forward), parameters(reverse));
		assertTrue(materialize(reverse, "X", root).isDefined());
	}

	@Test
	void nativeDistinctRootsOnOppositeSidesOfOneKnotAreNeverProximityMerged() {
		getKernel().setContinuous(false);
		add("A=(-1,1)");
		add("B=(0,-1/4096)");
		add("C=(1,1)");
		GeoLocusV2 curved = add("S=SplineV2({A,B,C},3)");
		horizontal(0);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(2, finite(rich).size(), diagnostic(rich));
		assertEquals(2, eligible(rich).size(), diagnostic(rich));
		List<Double> parameters = finite(rich).stream()
				.map(root -> parameter(root, curved)).sorted().toList();
		assertTrue(parameters.get(0) < 0.5 && parameters.get(1) > 0.5);
		assertTrue(parameters.get(0) > 0.49 && parameters.get(1) < 0.51);
		assertEquals(2, tokens(rich).size());
		GeoPoint first = materialize(rich, "X", eligible(rich).get(0));
		GeoPoint second = materialize(rich, "Y", eligible(rich).get(1));
		assertTrue(first.isDefined() && second.isDefined());
		assertNotEquals(selectedToken(first), selectedToken(second));
		assertTrue(first.getInhomX() * second.getInhomX() < 0);
		assertEquals(0, first.getInhomY(), 1E-9);
		assertEquals(0, second.getInhomY(), 1E-9);
	}

	@Test
	void nativeNearTangencyDepthExhaustionKeepsTwoDistinctRichOnlyPreimages() {
		// Preserve DEV03's stronger closeness fixture. Its depth-16 cells still
		// touch the zero-Jacobian minimum, so numerical discovery does not
		// establish either required class certificate. Do not enlarge budgets
		// or confuse the two preimages merely because their parameters are near.
		add("A=(-1,1)");
		add("B=(0,-1/65536)");
		add("C=(1,1)");
		GeoLocusV2 curved = add("S=SplineV2({A,B,C},3)");
		horizontal(0);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(2, finite(rich).size(), diagnostic(rich));
		List<Double> parameters = finite(rich).stream()
				.map(root -> parameter(root, curved)).sorted().toList();
		assertTrue(parameters.get(0) < 0.5 && parameters.get(1) > 0.5);
		assertTrue(parameters.get(0) > 0.49 && parameters.get(1) < 0.51);
		assertTrue(eligible(rich).isEmpty(), diagnostic(rich));
		assertTrue(diagnostic(rich).contains("status=UNRESOLVED"), diagnostic(rich));
	}

	@Test
	void nativeNearKnotMotionCannotRetireCurrentSingletonToken() {
		straight();
		vertical(-0x1p-18, false);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		String token = selectedToken(point);
		for (double coordinate : new double[] {-0x1p-20, 0, 0x1p-20, 0x1p-18, 0}) {
			move("h", coordinate);
			assertEquals(1, finite(rich).size(), diagnostic(rich));
			assertEquals(1, eligible(rich).size(), diagnostic(rich));
			assertTrue(point.isDefined(), diagnostic(rich));
			assertEquals(token, selectedToken(point));
			assertEquals(coordinate, point.getInhomX(), 1E-10);
		}
	}

	@Test
	void nativePeriodicOperandsRemainSymmetricAndNegativeTransformHasNewContext() {
		loop(false);
		horizontal(0.25);
		GeoLocusIntersectionResult forward = add("R=Intersect(S,T)");
		GeoLocusIntersectionResult reverse = add("V=Intersect(T,S)");
		assertEquals(2, eligible(forward).size(), diagnostic(forward));
		assertEquals(2, eligible(reverse).size(), diagnostic(reverse));
		assertEquals(forward.getSourcePairIdentity(), reverse.getSourcePairIdentity());
		assertEquals(selectors(forward), selectors(reverse));
		assertEquals(parameters(forward), parameters(reverse));
		assertNotEquals(tokens(forward), tokens(reverse));
		add("DS=Dilate(S,-2,(0,0))");
		add("DT=Dilate(T,-2,(0,0))");
		GeoLocusIntersectionResult transformed = add("W=Intersect(DS,DT)");
		assertEquals(2, eligible(transformed).size(), diagnostic(transformed));
		assertNotEquals(forward.getSourcePairIdentity(), transformed.getSourcePairIdentity());
		assertNotEquals(tokens(forward), tokens(transformed));
		for (int index = 0; index < eligible(transformed).size(); index++) {
			GeoPoint point = materialize(transformed, "X" + index,
					eligible(transformed).get(index));
			assertTrue(point.isDefined());
			assertEquals(-0.5, point.getInhomY(), 1E-8);
		}
	}

	@Test
	void nativePeriodicTangencyDoesNotAcquireTransverseMaterializationAuthority() {
		loop(false);
		horizontal(1);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertTrue(requireLookup("S").isDefined());
		assertTrue(requireLookup("T").isDefined());
		assertTrue(eligible(rich).isEmpty(), diagnostic(rich));
		assertFalse(rich.getTokenLedgerState().startsWith("5|"), rich.getTokenLedgerState());
	}

	@Test
	void nativeOverlappingSplineImagesNeverProduceIsolatedPairTokens() {
		straight();
		add("E=(-2,0)");
		add("F=(0,0)");
		add("G=(2,0)");
		add("T=SplineV2({E,F,G},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertTrue(requireLookup("S").isDefined());
		assertTrue(requireLookup("T").isDefined());
		assertTrue(eligible(rich).isEmpty(), diagnostic(rich));
		assertFalse(rich.getTokenLedgerState().startsWith("5|"), rich.getTokenLedgerState());
	}

	@Test
	void nativeRepeatedPeriodicTraversalHasFourRootsButNoSingletonGerm() {
		loop(true);
		horizontal(0.25);
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertTrue(requireLookup("S").isDefined());
		assertEquals(4, finite(rich).size(), diagnostic(rich));
		assertTrue(eligible(rich).isEmpty(), diagnostic(rich));
		assertEquals(4, finite(rich).stream().map(root -> parameter(root,
				(GeoLocusV2) requireLookup("S"))).distinct().count());
		assertFalse(rich.getTokenLedgerState().startsWith("5|"), rich.getTokenLedgerState());
	}

	@Test
	void nativeCollapsedPeriodicImageInvalidatesAndRecoversOnlyExistingPoints() {
		loop(false);
		horizontal(0.25);
		add("k=1");
		add("DS=Dilate(S,k,(0,0))");
		add("DT=Dilate(T,k,(0,0))");
		GeoLocusIntersectionResult rich = add("R=Intersect(DS,DT)");
		assertEquals(2, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		final String token = selectedToken(point);
		final int count = getConstruction().getGeoSetConstructionOrder().size();
		move("k", 0);
		assertTrue(requireLookup("DS").isDefined());
		assertTrue(requireLookup("DT").isDefined());
		assertFalse(point.isDefined());
		assertTrue(eligible(rich).isEmpty(), diagnostic(rich));
		move("k", 1);
		assertTrue(point.isDefined(), diagnostic(rich));
		assertEquals(token, selectedToken(point));
		assertSame(point, requireLookup("X"));
		assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
	}

	private GeoLocusV2 loop(boolean repeated) {
		getKernel().setContinuous(false);
		add("A=(1,0)");
		add("B=(0,1)");
		add("C=(-1,0)");
		add("D=(0,-1)");
		return add(repeated ? "S=SplineV2({A,B,C,D,A,B,C,D,A},3)"
				: "S=SplineV2({A,B,C,D,A},3)");
	}

	private GeoLocusV2 straight() {
		getKernel().setContinuous(false);
		add("A=(-1,0)");
		add("B=(0,0)");
		add("C=(1,0)");
		return add("S=SplineV2({A,B,C},3)");
	}

	private GeoLocusV2 vertical(double coordinate, boolean fourPoints) {
		add("h=" + coordinate);
		add("E=(h,-2)");
		add(fourPoints ? "F=(h,-2/3)" : "F=(h,0)");
		add(fourPoints ? "G=(h,2/3)" : "G=(h,2)");
		if (fourPoints) {
			add("H=(h,2)");
		}
		return add(fourPoints ? "T=SplineV2({E,F,G,H},3)" : "T=SplineV2({E,F,G},3)");
	}

	private void horizontal(double coordinate) {
		add("h=" + coordinate);
		add("E=(-2,h)");
		add("F=(0,h)");
		add("G=(2,h)");
		add("T=SplineV2({E,F,G},3)");
	}

	private void move(String label, double value) {
		GeoNumeric numeric = (GeoNumeric) requireLookup(label);
		numeric.setValue(value);
		numeric.updateCascade();
	}

	private GeoPoint materialize(GeoLocusIntersectionResult rich, String label,
			LocusIntersectionSolution2D solution) {
		GeoText token = new GeoText(getConstruction(), solution.getIdentity().getRootToken());
		return LocusV2PublicOperations.selectIntersectionPoint(
				getConstruction(), label, rich, token);
	}

	private static List<LocusIntersectionSolution2D> finite(GeoLocusIntersectionResult rich) {
		assertNotNull(rich.getIntersectionResult());
		return rich.getIntersectionResult().getFiniteSolutions();
	}

	private static List<LocusIntersectionSolution2D> eligible(GeoLocusIntersectionResult rich) {
		return finite(rich).stream().filter(root ->
				rich.isPointAdmissible(root.getIdentity().getRootToken())).toList();
	}

	private static Set<String> tokens(GeoLocusIntersectionResult rich) {
		return eligible(rich).stream().map(root -> root.getIdentity().getRootToken())
				.collect(Collectors.toSet());
	}

	private static Set<String> selectors(GeoLocusIntersectionResult rich) {
		return eligible(rich).stream().map(root ->
				root.getIdentity().getExplicitContinuationKey().orElseThrow())
				.collect(Collectors.toSet());
	}

	private static List<String> parameters(GeoLocusIntersectionResult rich) {
		return eligible(rich).stream().map(root -> {
			var pair = root.getPairEvidence().orElseThrow();
			return Double.toHexString(pair.getFirst().getSemanticParameter()) + "/"
					+ Double.toHexString(pair.getSecond().getSemanticParameter());
		}).sorted().toList();
	}

	private static double parameter(LocusIntersectionSolution2D root, GeoLocusV2 source) {
		var pair = root.getPairEvidence().orElseThrow();
		String sourceId = source.getPersistentLocusId().toExternalForm();
		assertTrue(pair.getFirst().getLocusIdentity().equals(sourceId)
				|| pair.getSecond().getLocusIdentity().equals(sourceId));
		return pair.getFirst().getLocusIdentity().equals(sourceId)
				? pair.getFirst().getSemanticParameter() : pair.getSecond().getSemanticParameter();
	}

	private static String selectedToken(GeoPoint point) {
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm()).getSelectedRootToken();
	}

	private static String diagnostic(GeoLocusIntersectionResult rich) {
		return rich.getIntersectionResult() == null ? "unpublished" : rich.getIntersectionResult()
				.getDiagnostics().stream().map(Object::toString).collect(Collectors.joining("\n"))
				+ "\nRoots: " + finite(rich).stream().map(root -> {
					var pair = root.getPairEvidence().orElseThrow();
					return pair.getFirst().getSemanticParameter() + "/"
							+ pair.getSecond().getSemanticParameter() + " "
							+ root.getIdentity().getIdentityStatus() + " eligible="
							+ rich.isPointAdmissible(root.getIdentity().getRootToken());
				}).collect(Collectors.joining(", "));
	}
}
