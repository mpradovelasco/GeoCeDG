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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.AlgoSplineV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/** Public D2 singleton-germ slots through the ordinary Intersect DAG. */
final class G9S1R1SplinePairMaterializationTest extends G9U0PublicSurfaceTestBase {

	@Test
	void simpleTransversePairMaterializesWithoutGlobalCompleteness() {
		straightPair();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		assertEquals(Completeness.NOT_ESTABLISHED,
				rich.getIntersectionResult().getCompletenessEvidence().getCompleteness());
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		assertTrue(point.isDefined());
		assertEquals(0, point.getInhomX(), 1E-10);
		assertEquals(0, point.getInhomY(), 1E-10);
		assertTrue(rich.getTokenLedgerState().startsWith("5|"), rich.getTokenLedgerState());
	}

	@Test
	void projectedRankExchangePreservesBothExactTokensAndPoints() {
		rankPair();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(2, eligible(rich).size(), diagnostic(rich));
		List<LocusIntersectionSolution2D> initial = eligible(rich);
		GeoPoint first = materialize(rich, "X", initial.get(0));
		GeoPoint second = materialize(rich, "Y", initial.get(1));
		PersistentGeoId firstId = id(first);
		PersistentGeoId secondId = id(second);
		Set<String> tokens = tokens(rich);
		Map<String, Boolean> sourceSides = initial.stream().collect(Collectors.toMap(
				root -> root.getIdentity().getRootToken(), root -> targetParameter(root) < 0.5));
		for (double value : new double[] {-0.08, -0.03, 0, 0.03, 0.08, 0.1, -0.1}) {
			move("t", value);
			assertEquals(2, eligible(rich).size(), "t=" + value + "\n" + diagnostic(rich));
			assertEquals(tokens, tokens(rich));
			assertTrue(first.isDefined(), diagnostic(rich));
			assertTrue(second.isDefined(), diagnostic(rich));
			assertSame(first, lookup("X"));
			assertSame(second, lookup("Y"));
			assertEquals(firstId, id(first));
			assertEquals(secondId, id(second));
			assertEquals(value, first.getInhomX(), 1E-8);
			assertEquals(value, second.getInhomX(), 1E-8);
			for (var root : eligible(rich)) {
				assertEquals(sourceSides.get(root.getIdentity().getRootToken()),
						targetParameter(root) < 0.5);
			}
		}
	}

	@Test
	void identicalFinalDefinitionsResolveIndependentlyOfUpdatePathAndReopen() {
		straightPair();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		String initial = getApp().getXML();
		String token = selectedToken(point);
		PersistentGeoId pointId = id(point);
		List<String> expected = null;
		for (double[] path : new double[][] {{0.25}, {0.05, 0.1, 0.15, 0.2, 0.25},
				{0.4, -0.1, 0.25}, {0.1, 0.25}}) {
			getApp().setXML(initial, true);
			for (double value : path) {
				move("h", value);
				if (path.length == 2 && value == 0.1) {
					getApp().setXML(getApp().getXML(), true);
				}
			}
			GeoPoint current = (GeoPoint) requireLookup("X");
			assertTrue(current.isDefined());
			assertEquals(pointId, id(current));
			assertEquals(token, selectedToken(current));
			List<String> state = List.of(Double.toHexString(current.getInhomX()),
					Double.toHexString(current.getInhomY()),
					parameters((GeoLocusIntersectionResult) lookup("R")).toString());
			if (expected == null) {
				expected = state;
			} else {
				assertEquals(expected, state);
			}
		}
	}

	@Test
	void undoRedoRestoresActiveDormantAndReactivatedGraph() {
		activateUndo();
		straightPair();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		final String token = selectedToken(point);
		final PersistentGeoId pointId = id(point);
		getApp().storeUndoInfo();
		move("h", 9);
		getApp().storeUndoInfo();
		move("h", 0);
		getApp().storeUndoInfo();
		getKernel().undo();
		assertFalse(requireLookup("X").isDefined());
		getKernel().undo();
		assertTrue(requireLookup("X").isDefined());
		getKernel().redo();
		assertFalse(requireLookup("X").isDefined());
		getKernel().redo();
		GeoPoint restored = (GeoPoint) requireLookup("X");
		assertTrue(restored.isDefined());
		assertEquals(pointId, id(restored));
		assertEquals(token, selectedToken(restored));
	}

	@Test
	void closureCopyRemapsBothSourcesAndRebasesPairToken() {
		straightPair();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		PersistentGeoId originalId = id(point);
		String originalToken = selectedToken(point);
		String clipboard = InternalClipboard.getTextToSave(getApp(), List.of(point), text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(Arrays.asList(clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));
		var registry = getConstruction().getSpatialIdentityRegistry();
		GeoIdentityRecord copiedRecord = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(record -> originalId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoPoint copied = (GeoPoint) registry.getGeo(copiedRecord.getId());
		assertTrue(copied.isDefined());
		assertNotEquals(originalId, id(copied));
		assertNotEquals(originalToken, selectedToken(copied));
		assertEquals(point.getInhomX(), copied.getInhomX(), 0);
		assertEquals(point.getInhomY(), copied.getInhomY(), 0);
		getApp().setXML(getApp().getXML(), true);
		GeoPoint reopened = (GeoPoint) getConstruction().getSpatialIdentityRegistry()
				.getGeo(copiedRecord.getId());
		assertNotNull(reopened);
		assertTrue(reopened.isDefined());
	}

	@Test
	void reversedCallerOperandsHaveEquivalentCurrentSlotEvidence() {
		rankPair();
		GeoLocusIntersectionResult forward = add("R=Intersect(S,T)");
		GeoLocusIntersectionResult reverse = add("V=Intersect(T,S)");
		assertEquals(2, eligible(forward).size(), diagnostic(forward));
		assertEquals(2, eligible(reverse).size(), diagnostic(reverse));
		assertEquals(forward.getSourcePairIdentity(), reverse.getSourcePairIdentity());
		assertEquals(parameters(forward), parameters(reverse));
		// Different rich-result owners deliberately own different opaque handles.
		assertNotEquals(tokens(forward), tokens(reverse));
		move("t", 0.1);
		assertEquals(parameters(forward), parameters(reverse));
	}

	@Test
	void dormantPointReactivatesSameSlotWithoutAllocatingGeoPoint() {
		straightPair();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		String token = selectedToken(point);
		PersistentGeoId pointId = id(point);
		final int count = getConstruction().getGeoSetConstructionOrder().size();
		move("h", 9);
		assertFalse(point.isDefined());
		move("h", 0);
		assertTrue(point.isDefined(), diagnostic(rich));
		assertSame(point, lookup("X"));
		assertEquals(pointId, id(point));
		assertEquals(token, selectedToken(point));
		assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
	}

	@Test
	void xmlReopenPreservesPairSelectorTokenAndDormantClaim() {
		straightPair();
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		String token = selectedToken(point);
		PersistentGeoId pointId = id(point);
		move("h", 9);
		assertFalse(point.isDefined());
		getApp().setXML(getApp().getXML(), true);
		GeoPoint restored = (GeoPoint) requireLookup("X");
		assertFalse(restored.isDefined());
		assertEquals(pointId, id(restored));
		assertEquals(token, selectedToken(restored));
		move("h", 0);
		assertTrue(restored.isDefined(), diagnostic((GeoLocusIntersectionResult) lookup("R")));
		assertEquals(token, selectedToken(restored));
	}

	@Test
	void sameGermAmbiguityDoesNotBlockIndependentlyUniqueOppositeGerm() {
		add("w(x,y)=1");
		add("A=(-3,-1)");
		add("B=(-1,1)");
		add("C=(1,-1)");
		add("D=(3,1)");
		add("S=SplineV2({A,B,C,D},3,w)");
		add("E=(-4,0)");
		add("F=(-4/3,0)");
		add("G=(4/3,0)");
		add("H=(4,0)");
		add("T=SplineV2({E,F,G,H},3,w)");
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(3, rich.getIntersectionResult().getFiniteSolutions().size(), diagnostic(rich));
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		assertTrue(materialize(rich, "X", eligible(rich).get(0)).isDefined());
	}

	@Test
	void nativeDyadicKnotCrossingKeepsCurrentSingletonSlot() {
		add("h=-0.1");
		add("A=(-1,0)");
		add("B=(0,0)");
		add("C=(1,0)");
		add("S=SplineV2({A,B,C},3)");
		add("E=(h,-1)");
		add("F=(h,0)");
		add("G=(h,1)");
		add("T=SplineV2({E,F,G},3)");
		for (String label : new String[] {"S", "T"}) {
			var model = ((AlgoSplineV2) requireLookup(label).getParentAlgorithm())
					.getPolynomialModel();
			for (int span = 0; span < model.getSpanCount(); span++) {
				for (int axis = 0; axis < 2; axis++) {
					System.out.println("R1_NATIVE_KNOT " + label + " span=" + span
							+ " axis=" + axis + " "
							+ java.util.Arrays.toString(model.getCoefficients(span, axis)));
				}
			}
		}
		GeoLocusIntersectionResult rich = add("R=Intersect(S,T)");
		assertEquals(1, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		String token = selectedToken(point);
		for (double value : new double[] {-0.05, 0, 0.05, 0.1, 0, -0.1}) {
			move("h", value);
			assertEquals(1, eligible(rich).size(), diagnostic(rich));
			assertTrue(point.isDefined(), diagnostic(rich));
			assertEquals(token, selectedToken(point));
			assertEquals(value, point.getInhomX(), 1E-8);
		}
	}

	@Test
	void nativeKnotStoredPolynomialJetDefectIsExactNotTolerance() {
		add("A=(-1,0)");
		add("B=(0,0)");
		add("C=(1,0)");
		add("S=SplineV2({A,B,C},3)");
		var model = ((AlgoSplineV2) requireLookup("S").getParentAlgorithm())
				.getPolynomialModel();
		// Frozen native v1 coefficients recorded in the predecessor blocker
		// evidence. Do not rewrite that failure as though v2 had always existed.
		double epsilon = 0x1p-52;
		double[] left = {epsilon, 0, 2, -1};
		double[] right = {-epsilon, 4 * epsilon, 2 - 2 * epsilon, -1};
		BigDecimal seam = new BigDecimal(0.5);
		BigDecimal lowValue = exactPolynomial(left, seam, false);
		BigDecimal highValue = exactPolynomial(right, seam, false);
		assertEquals(0, lowValue.compareTo(new BigDecimal(Math.scalb(1.0, -55))));
		assertEquals(0, highValue.compareTo(new BigDecimal(-Math.scalb(1.0, -55))));
		BigDecimal derivativeGap = exactPolynomial(right, seam, true)
				.subtract(exactPolynomial(left, seam, true));
		assertEquals(0, derivativeGap.compareTo(new BigDecimal(Math.scalb(1.0, -53))));
		// There is a real zero on EACH side of the discontinuous join. Merging
		// them would erase distinct semantic preimages of the represented pieces.
		assertTrue(exactPolynomial(left, new BigDecimal(Math.nextDown(0.5)), false)
				.signum() < 0);
		assertTrue(exactPolynomial(right, new BigDecimal(Math.nextUp(0.5)), false)
				.signum() > 0);
		System.out.println("R1_HISTORICAL_NATIVE_JET value_jump=-2^-54 derivative_jump=2^-53"
				+ " C0=false C1=false separate_one_sided_zeros=true");
		assertEquals(2, model.getStructuralContinuityOrder());
		for (int order = 0; order <= 2; order++) {
			assertEquals(0, structuralJet(model.getExactCoefficientNumerators(0, 0),
					seam, order).compareTo(structuralJet(
							model.getExactCoefficientNumerators(1, 0), seam, order)));
		}
		System.out.println("R1_STRUCTURAL_NATIVE_JET C0=true C1=true C2=true"
				+ " authority=exact-structural-numerators toleranceGlue=false");
	}

	private static BigDecimal structuralJet(BigDecimal[] coefficients,
			BigDecimal parameter, int order) {
		BigDecimal value = BigDecimal.ZERO;
		int degree = coefficients.length - 1;
		for (int index = 0; index < coefficients.length - order; index++) {
			BigDecimal coefficient = coefficients[index];
			for (int factor = 0; factor < order; factor++) {
				coefficient = coefficient.multiply(BigDecimal.valueOf(degree - index - factor));
			}
			value = value.multiply(parameter).add(coefficient);
		}
		return value;
	}

	@Test
	void genericLocusPairStaysRichOnlyDespiteSharedInfrastructure() {
		createLine();
		add("t=0");
		add("V=(0,t)");
		add("E={false,{-2,2,true,true}}");
		add("T=LocusV2(V,t,E)");
		GeoLocusIntersectionResult rich = add("R=Intersect(L,T)");
		assertFalse(rich.getIntersectionResult().getFiniteSolutions().isEmpty());
		assertTrue(eligible(rich).isEmpty());
		assertFalse(rich.getTokenLedgerState().startsWith("5|"));
	}

	@Test
	void transformedPairHasNewContextAndCollapsedImageHasNoIsolatedSlot() {
		straightPair();
		GeoLocusIntersectionResult original = add("R=Intersect(S,T)");
		assertEquals(1, eligible(original).size(), diagnostic(original));
		add("k=-2");
		add("DS=Dilate(S,k,(1,1))");
		add("DT=Dilate(T,k,(1,1))");
		GeoLocusIntersectionResult transformed = add("V=Intersect(DS,DT)");
		assertEquals(1, eligible(transformed).size(), diagnostic(transformed));
		assertNotEquals(tokens(original), tokens(transformed));
		GeoPoint point = materialize(transformed, "X", eligible(transformed).get(0));
		assertEquals(3, point.getInhomX(), 1E-8);
		assertEquals(3, point.getInhomY(), 1E-8);
		move("k", 0);
		assertFalse(point.isDefined());
		assertTrue(eligible(transformed).isEmpty());
		move("k", -2);
		assertTrue(point.isDefined(), diagnostic(transformed));
	}

	private void straightPair() {
		getKernel().setContinuous(false);
		add("h=0");
		add("A=(-2,h)");
		add("B=(-2/3,h)");
		add("C=(2/3,h)");
		add("D=(2,h)");
		add("S=SplineV2({A,B,C,D},3)");
		add("E=(0,-2)");
		add("F=(0,-2/3)");
		add("G=(0,2/3)");
		add("H=(0,2)");
		add("T=SplineV2({E,F,G,H},3)");
	}

	private void rankPair() {
		getKernel().setContinuous(false);
		add("t=-0.1");
		add("w(x,y)=1");
		add("A=(t,-4)");
		add("B=(t,-4/3)");
		add("C=(t,4/3)");
		add("D=(t,4)");
		add("S=SplineV2({A,B,C,D},3,w)");
		add("E=(3,-6)");
		add("F=(-5/9,10/27)");
		add("G=(-5/9,-10/27)");
		add("H=(3,6)");
		add("T=SplineV2({E,F,G,H},3,w)");
	}

	private static List<LocusIntersectionSolution2D> eligible(GeoLocusIntersectionResult rich) {
		assertNotNull(rich.getIntersectionResult());
		return rich.getIntersectionResult().getFiniteSolutions().stream().filter(root ->
				rich.isPointAdmissible(root.getIdentity().getRootToken())).toList();
	}

	private static Set<String> tokens(GeoLocusIntersectionResult rich) {
		return eligible(rich).stream().map(root -> root.getIdentity().getRootToken())
				.collect(Collectors.toSet());
	}

	private static List<String> parameters(GeoLocusIntersectionResult rich) {
		return eligible(rich).stream().map(root -> {
			var pair = root.getPairEvidence().orElseThrow();
			return Double.toHexString(pair.getFirst().getSemanticParameter()) + "/"
					+ Double.toHexString(pair.getSecond().getSemanticParameter());
		}).sorted().toList();
	}

	private double targetParameter(LocusIntersectionSolution2D root) {
		var pair = root.getPairEvidence().orElseThrow();
		String target = id(requireLookup("T")).toExternalForm();
		return pair.getFirst().getLocusIdentity().equals(target)
				? pair.getFirst().getSemanticParameter() : pair.getSecond().getSemanticParameter();
	}

	private GeoPoint materialize(GeoLocusIntersectionResult rich, String label,
			LocusIntersectionSolution2D solution) {
		GeoText token = new GeoText(getConstruction(), solution.getIdentity().getRootToken());
		token.setAuxiliaryObject(true);
		token.setEuclidianVisible(false);
		return LocusV2PublicOperations.selectIntersectionPoint(getConstruction(),
				label, rich, token);
	}

	private PersistentGeoId id(GeoElement geo) {
		return getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(geo);
	}

	private static String selectedToken(GeoPoint point) {
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm()).getSelectedRootToken();
	}

	private void move(String label, double value) {
		GeoNumeric number = (GeoNumeric) requireLookup(label);
		number.setValue(value);
		number.updateCascade();
	}

	private static String diagnostic(GeoLocusIntersectionResult rich) {
		return rich.getIntersectionResult() == null ? "unpublished" : rich.getIntersectionResult()
				.getDiagnostics().stream().map(Object::toString).collect(Collectors.joining("\n"));
	}

	private static BigDecimal exactPolynomial(double[] coefficients, BigDecimal parameter,
			boolean derivative) {
		BigDecimal value = BigDecimal.ZERO;
		int degree = coefficients.length - 1;
		for (int index = 0; index < coefficients.length - (derivative ? 1 : 0); index++) {
			BigDecimal coefficient = new BigDecimal(coefficients[index]);
			if (derivative) {
				coefficient = coefficient.multiply(BigDecimal.valueOf(degree - index));
			}
			value = value.multiply(parameter).add(coefficient);
		}
		return value;
	}
}
