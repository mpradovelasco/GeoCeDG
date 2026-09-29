/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.SemanticMetricEndpointResolver2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionEvidence2D;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.InternalClipboard;
import org.geogebra.test.commands.ErrorAccumulator;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R2-E0/E1 objective B: certified semantic-pair exact-token
 * materialization through the ordinary public Intersect DAG (ADR 0028,
 * pair materialization section 10, construction-model contract).
 */
final class PreG9bR2E1SemanticPairMaterializationTest extends G9U0PublicSurfaceTestBase {

	@Test
	void severalDefectsMixedPairMaterializesBothCertifiedGermSlots() {
		witness();
		GeoLocusIntersectionResult rich = add("e=Intersect(d,a)");
		List<LocusIntersectionSolution2D> roots = eligible(rich);
		assertEquals(2, roots.size(), diagnostic(rich));
		// Local admissibility is not global completeness.
		assertEquals(Completeness.NOT_ESTABLISHED,
				rich.getIntersectionResult().getCompletenessEvidence().getCompleteness());
		assertTrue(rich.getTokenLedgerState().startsWith("5|"), rich.getTokenLedgerState());
		assertTrue(diagnostic(rich).contains("germ=1 status=UNIQUE"), diagnostic(rich));
		assertTrue(diagnostic(rich).contains("germ=-1 status=UNIQUE"), diagnostic(rich));
		for (LocusIntersectionSolution2D root : roots) {
			LocusPairIntersectionEvidence2D pair = root.getPairEvidence().orElseThrow();
			assertEquals(LocalIsolationStatus.ESTABLISHED, pair.getLocalIsolation().getStatus());
			assertEquals(IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
					root.getIdentity().getIdentityStatus());
		}
		Map<String, double[]> expected = Map.of("left", new double[] {-1.11661065283576,
				2.97518525763727}, "right", new double[] {1.72755008395297, 3.80966209474193});
		List<GeoPoint> points = new ArrayList<>();
		for (int index = 0; index < roots.size(); index++) {
			points.add(materialize(rich, "X" + index, roots.get(index)));
		}
		for (GeoPoint point : points) {
			assertTrue(point.isDefined());
			double[] target = point.getInhomX() < 0 ? expected.get("left") : expected.get("right");
			assertEquals(target[0], point.getInhomX(), 1E-9);
			assertEquals(target[1], point.getInhomY(), 1E-9);
		}
		// Ordinary recomputation re-certifies the same slots and creates no point.
		Set<String> tokens = tokens(rich);
		int materialized = getConstruction().getGeoSetConstructionOrder().size();
		moveTo("H", 6.2, -1.3);
		moveTo("H", 6.28, -1.36);
		assertEquals(tokens, tokens(rich));
		assertEquals(materialized, getConstruction().getGeoSetConstructionOrder().size());
		for (GeoPoint point : points) {
			assertTrue(point.isDefined(), diagnostic(rich));
		}
	}

	@Test
	void callerReversalYieldsTheSameCanonicalSelectorsAndAddresses() {
		witness();
		GeoLocusIntersectionResult forward = add("e=Intersect(d,a)");
		GeoLocusIntersectionResult reverse = add("er=Intersect(a,d)");
		assertEquals(2, eligible(forward).size(), diagnostic(forward));
		assertEquals(2, eligible(reverse).size(), diagnostic(reverse));
		assertEquals(forward.getSourcePairIdentity(), reverse.getSourcePairIdentity());
		assertEquals(addresses(forward), addresses(reverse));
		assertEquals(selectorEvidence(forward), selectorEvidence(reverse));
		// Different rich-result owners own different opaque handles.
		assertNotEquals(tokens(forward), tokens(reverse));
	}

	@Test
	void constructionByConstructionPairMaterializes() {
		witness();
		// A segment driver read only through homogeneous coordinates (parallel,
		// meet): the points of x = y/2 for y in [-3, 6].
		add("sg=Segment((-3,-3),(6,6))");
		add("Cs=Point(sg)");
		add("gs=Line(Cs,xAxis)");
		add("hs:2x-y=0");
		add("Ms=Intersect(gs,hs)");
		add("ls=LocusV2(Ms,Cs)");
		GeoLocusIntersectionResult rich = add("R=Intersect(ls,a)");
		List<LocusIntersectionSolution2D> roots = eligible(rich);
		assertEquals(2, roots.size(), diagnostic(rich));
		for (int index = 0; index < roots.size(); index++) {
			assertTrue(materialize(rich, "Y" + index, roots.get(index)).isDefined());
		}
	}

	@Test
	void similarityImageOfAConstructionLocusMaterializes() {
		witness();
		add("alpha=30deg");
		add("aR=Rotate(a,alpha,A)");
		add("vt=Vector((0.2,-0.1))");
		add("aT=Translate(a,vt)");
		for (String image : List.of("aR", "aT")) {
			GeoLocusIntersectionResult rich = add("R" + image + "=Intersect(d," + image + ")");
			assertEquals(2, eligible(rich).size(), image + "\n" + diagnostic(rich));
		}
	}

	@Test
	void affineScalarPairMaterializesAndUncertifiableScalarIsDiagnosedRichOnly() {
		createLine();
		add("t=0");
		add("V=(0,t)");
		add("E={false,{-2,2,true,true}}");
		add("T=LocusV2(V,t,E)");
		GeoLocusIntersectionResult affine = add("R=Intersect(L,T)");
		assertEquals(1, eligible(affine).size(), diagnostic(affine));
		GeoPoint point = materialize(affine, "X", eligible(affine).get(0));
		assertEquals(0, point.getInhomX(), 1E-12);
		assertEquals(0, point.getInhomY(), 1E-12);

		add("u=0");
		add("W=(u,u^2-1)");
		add("F={false,{-2,2,true,true}}");
		add("P=LocusV2(W,u,F)");
		GeoLocusIntersectionResult parabola = add("RP=Intersect(L,P)");
		assertFalse(parabola.getIntersectionResult().getFiniteSolutions().isEmpty());
		assertTrue(eligible(parabola).isEmpty());
		assertTrue(diagnostic(parabola).contains("no certified interval curve model"),
				diagnostic(parabola));
	}

	@Test
	void everyClassV1DriverAndOperationCertifiesInARealPair() {
		getKernel().setContinuous(false);
		// Similarity chain on a circle driver: translate, rotate about the origin and
		// a point, point and line mirrors, dilation from the origin and a point.
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("v=Vector((0.5,0.3))");
		add("al=20deg");
		add("be=15deg");
		add("K=(1,2)");
		add("Q0=(0.5,1.5)");
		add("g0:y=0.2x+2");
		add("k1=0.8");
		add("k2=1.1");
		add("P1=Translate(C,v)");
		add("P2=Rotate(P1,al)");
		add("P3=Rotate(P2,be,K)");
		add("P4=Mirror(P3,Q0)");
		add("P5=Mirror(P4,g0)");
		add("P6=Dilate(P5,k1)");
		add("P7=Dilate(P6,k2,K)");
		add("chain=LocusV2(P7,C)");
		add("H1=SplineV2({(-4,3),(0,3.5),(4,3)},3)");
		GeoLocusIntersectionResult similarity = add("R1=Intersect(H1,chain)");
		assertEquals(2, eligible(similarity).size(), diagnostic(similarity));

		// Circular-arc driver with join, perpendicular and meet: the pedal of the
		// lower semicircle, one traversal of a Thales arc through the origin.
		add("Mj=(0,-1)");
		add("Aj=(-1.5,-1)");
		add("Bj=(1.5,-1)");
		add("lower=CircularArc(Mj,Aj,Bj)");
		add("Cj=Point(lower)");
		add("Kj=(0,3)");
		add("Oj=(0,0)");
		add("gj=Line(Cj,Kj)");
		add("gp=PerpendicularLine(Oj,gj)");
		add("Ej=Intersect(gj,gp)");
		add("pedal=LocusV2(Ej,Cj)");
		add("H2=SplineV2({(-2,0.2),(0,0.2),(2,0.2)},3)");
		GeoLocusIntersectionResult pedal = add("R2=Intersect(H2,pedal)");
		assertEquals(2, eligible(pedal).size(), diagnostic(pedal));

		// Circular-arc driver with a midpoint.
		add("Ma=(0,0)");
		add("Pa=(2,0)");
		add("Qa=(0,2)");
		add("arc=CircularArc(Ma,Pa,Qa)");
		add("Ca=Point(arc)");
		add("Ka=(1,1)");
		add("Ea=Midpoint(Ca,Ka)");
		add("arcLocus=LocusV2(Ea,Ca)");
		add("H3=SplineV2({(0,0),(1,1),(2,2)},3)");
		GeoLocusIntersectionResult arc = add("R3=Intersect(H3,arcLocus)");
		assertEquals(1, eligible(arc).size(), diagnostic(arc));
		GeoPoint arcPoint = materialize(arc, "Xa", eligible(arc).get(0));
		assertEquals(0.5 + Math.sqrt(0.5), arcPoint.getInhomX(), 1E-9);
		assertEquals(0.5 + Math.sqrt(0.5), arcPoint.getInhomY(), 1E-9);
	}

	@Test
	void segmentDriverInhomogeneousReadIsCurrentAndCertified() {
		getKernel().setContinuous(false);
		// Retained debt TD-LOCUS-EVALUATOR-SEGMENT-DRIVER-UPDATE on its retained
		// fixture: the evaluator did not refresh a segment driver's inhomogeneous
		// coordinates, a midpoint through it evaluated the previous parameter, and the
		// floating verification refused the correct certificate. PRE-G9B-R3-X1
		// corrects the evaluator, so the certified slots now pass the unchanged
		// verification. MODEL_X1#theFloatingVerificationStillRefusesACertifiedSlotItRejects
		// keeps the refusal branch of the coherence gate pinned.
		add("sm=Segment((-2,-2),(2,2))");
		add("Cm=Point(sm)");
		add("Km=(1,-1)");
		add("Mm=Midpoint(Cm,Km)");
		add("lag=LocusV2(Mm,Cm)");
		add("Hm=SplineV2({(-1,0.2),(0,-0.3),(1.5,0.1)},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(Hm,lag)");
		List<LocusIntersectionSolution2D> roots = eligible(rich);
		assertFalse(roots.isEmpty(), diagnostic(rich));
		assertTrue(diagnostic(rich).contains("status=UNIQUE"), diagnostic(rich));
		assertFalse(diagnostic(rich).contains(
				"Certified slot refused by current evaluator/contact validation"),
				diagnostic(rich));
		for (int index = 0; index < roots.size(); index++) {
			GeoPoint point = materialize(rich, "Z" + index, roots.get(index));
			assertTrue(point.isDefined());
			// Validation only: the midpoints of (s,s) and (1,-1) lie on y = x - 1.
			assertEquals(point.getInhomX() - 1, point.getInhomY(), 1E-9);
		}
	}

	@Test
	void slicesOutsideClassV1StayRichOnlyWithDiagnostic() {
		witness();
		add("Ex=(x(C)/2,y(C))");
		add("expression=LocusV2(Ex,C)");
		add("Kl=(8,9)");
		add("lc=Line(C,Kl)");
		add("far=Circle((0,0),20)");
		add("El=Intersect(lc,far,1)");
		add("conic=LocusV2(El,C)");
		for (String locus : List.of("expression", "conic")) {
			GeoLocusIntersectionResult rich = add("R" + locus + "=Intersect(d," + locus + ")");
			assertNotNull(rich.getIntersectionResult(), locus);
			assertTrue(eligible(rich).isEmpty(), locus + "\n" + diagnostic(rich));
			assertTrue(diagnostic(rich).contains("no certified interval curve model"),
					locus + "\n" + diagnostic(rich));
		}
	}

	@Test
	void seamCrossingIsUnresolvedWhileTheOppositeGermIsAdmissible() {
		witness();
		// y = 2.24 meets the ellipse at its canonical seam (real angle pi, outside the
		// half-open domain [-Math.PI, Math.PI)) and transversally at angle 0.
		add("seam=SplineV2({(-3,2.24),(0,2.24),(3,2.24)},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(seam,a)");
		List<LocusIntersectionSolution2D> roots = eligible(rich);
		assertEquals(1, roots.size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", roots.get(0));
		assertEquals((0.78 + 3.1016124838541645) / 2, point.getInhomX(), 1E-9);
		assertTrue(diagnostic(rich).contains("status=UNRESOLVED"), diagnostic(rich));
	}

	@Test
	void tangencyBlocksBothGermClassesOfItsComponentPair() {
		witness();
		double top = 2.24 + 3.1016124838541645;
		add("top=SplineV2({(-3," + top + "),(0," + top + "),(3," + top + ")},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(top,a)");
		assertTrue(eligible(rich).isEmpty(), diagnostic(rich));
		assertFalse(diagnostic(rich).contains("status=UNIQUE"), diagnostic(rich));
	}

	@Test
	void sameGermMultiplicityQuarantinesAndRecoversTheSameSlot() {
		witness();
		add("m=2");
		add("W=SplineV2({(-3,2),(-1,2),(0.39,m),(1.8,2),(3.5,2)},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(W,a)");
		assertEquals(2, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		final String token = selectedToken(point);
		final PersistentGeoId pointId = id(point);
		move("m", 7);
		assertEquals(4, rich.getIntersectionResult().getFiniteSolutions().size(),
				diagnostic(rich));
		assertTrue(eligible(rich).isEmpty(), diagnostic(rich));
		assertTrue(diagnostic(rich).contains("status=MULTIPLE"), diagnostic(rich));
		assertFalse(point.isDefined());
		move("m", 2);
		assertTrue(point.isDefined(), diagnostic(rich));
		assertSame(point, lookup("X"));
		assertEquals(pointId, id(point));
		assertEquals(token, selectedToken(point));
	}

	@Test
	void rootLossAndReturnReactivatesTheSameSlot() {
		witness();
		add("h=0");
		add("Z=SplineV2({(-3,2+h),(0,2+h),(3,2+h)},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(Z,a)");
		assertEquals(2, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		String token = selectedToken(point);
		int count = getConstruction().getGeoSetConstructionOrder().size();
		move("h", 10);
		assertTrue(rich.getIntersectionResult().getFiniteSolutions().isEmpty(),
				diagnostic(rich));
		assertFalse(point.isDefined());
		move("h", 0);
		assertTrue(point.isDefined(), diagnostic(rich));
		assertEquals(token, selectedToken(point));
		assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
	}

	@Test
	void temporaryConstructionInvalidityIsDormantAndRecovers() {
		witness();
		GeoLocusIntersectionResult rich = add("e=Intersect(d,a)");
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		String token = selectedToken(point);
		moveTo("B", 0.78, 2.24);
		assertFalse(point.isDefined());
		assertTrue(eligible(rich).isEmpty());
		moveTo("B", 2.56, 4.78);
		assertTrue(point.isDefined(), diagnostic(rich));
		assertEquals(token, selectedToken(point));
	}

	@Test
	void selfPairOfAConstructionLocusPublishesNoAdmissibleRoot() {
		witness();
		GeoLocusIntersectionResult self = add("S2=Intersect(a,a)");
		assertTrue(self.getIntersectionResult() == null
				|| self.getIntersectionResult().getFiniteSolutions().stream().noneMatch(
						root -> self.isPointAdmissible(root.getIdentity().getRootToken())));
	}

	@Test
	void reopenAndUndoRedoPreserveTheCertifiedSlot() {
		activateUndo();
		witness();
		GeoLocusIntersectionResult rich = add("e=Intersect(d,a)");
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		String token = selectedToken(point);
		PersistentGeoId pointId = id(point);
		double x = point.getInhomX();

		getApp().setXML(getApp().getXML(), true);
		GeoPoint reopened = (GeoPoint) requireLookup("X");
		assertTrue(reopened.isDefined());
		assertEquals(pointId, id(reopened));
		assertEquals(token, selectedToken(reopened));
		assertEquals(x, reopened.getInhomX(), 1E-12);

		getApp().storeUndoInfo();
		moveTo("A", 40.78, 2.24);
		moveTo("B", 42.56, 4.78);
		getApp().storeUndoInfo();
		assertFalse(requireLookup("X").isDefined());
		getKernel().undo();
		assertTrue(requireLookup("X").isDefined());
		getKernel().redo();
		assertFalse(requireLookup("X").isDefined());
		getKernel().undo();
		GeoPoint restored = (GeoPoint) requireLookup("X");
		assertTrue(restored.isDefined());
		assertEquals(pointId, id(restored));
		assertEquals(token, selectedToken(restored));

	}

	@Test
	void closureCopyRemapsSourcesAndDriverAndKeepsTheCopiedPointCurrent() {
		getKernel().setContinuous(false);
		// A point-driven locus whose slice has intermediate construction objects
		// cannot be closure-copied by the host today (retained debt
		// CLIPBOARD-COPY-POINT-DRIVEN-LOCUS-SLICE); a direct midpoint slice can.
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		GeoPoint driver = add("C=Point(c)");
		add("K=(1,1)");
		GeoPoint generator = add("M=Midpoint(C,K)");
		GeoLocusV2 ring = add("ring=LocusV2(M,C)");
		add("H=SplineV2({(-3,1),(1,2),(5,1)},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(H,ring)");
		assertEquals(2, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		final String token = selectedToken(point);
		PairSemanticSlotSelector2D original = rich.getRetainedPairSelector(token)
				.orElseThrow();
		assertEquals(Set.of(id(driver).toExternalForm()),
				original.getDeclaredParticipants());
		final String originalLedger = rich.getTokenLedgerState();
		final double length = finite(add("LX=LocusLength(ring,M,X)"));
		Set<String> originalGraph = identityRecords();

		paste(point);
		GeoPoint copied = (GeoPoint) copyOf(point);
		GeoLocusV2 copiedRing = (GeoLocusV2) copyOf(ring);
		GeoLocusIntersectionResult copiedRich = (GeoLocusIntersectionResult) copyOf(rich);
		// The copied graph receives new durable identities throughout.
		for (GeoElement copy : List.of(copied, copiedRing, copyOf(driver), copiedRich,
				copyOf(generator))) {
			assertFalse(originalGraph.contains(id(copy).toExternalForm()),
					copy.getLabelSimple());
		}
		// The copied point is current and admissible under its own copied token.
		assertTrue(copied.isDefined(), diagnostic(copiedRich));
		String copiedToken = effectiveToken(copied);
		assertNotEquals(token, copiedToken);
		assertTrue(copiedRich.isPointAdmissible(copiedToken));
		// Its selector names only copied identities: both sources and the driver.
		PairSemanticSlotSelector2D selector = copiedRich.getRetainedPairSelector(
				copiedToken).orElseThrow();
		assertEquals(Set.of(id(copyOf(driver)).toExternalForm()),
				selector.getDeclaredParticipants());
		for (String identity : List.of(selector.getFirst().getSourceId(),
				selector.getSecond().getSourceId())) {
			assertFalse(originalGraph.contains(identity), identity);
		}
		assertEquals(original.remap(Map.of(id(ring).toExternalForm(),
				id(copiedRing).toExternalForm(), id(requireLookup("H")).toExternalForm(),
				id(copyOf(requireLookup("H"))).toExternalForm()),
				Map.of(id(driver).toExternalForm(), id(copyOf(driver)).toExternalForm())),
				selector);
		// The original pair and its ledger are unchanged.
		assertTrue(point.isDefined());
		assertEquals(token, selectedToken(point));
		assertEquals(original, rich.getRetainedPairSelector(token).orElseThrow());
		assertEquals(originalLedger, rich.getTokenLedgerState());
		// Metric endpoints resolve on the copied curve through copied provenance.
		SemanticMetricEndpointResolver2D.Resolution resolution =
				new SemanticMetricEndpointResolver2D().resolve(copiedRing, copied);
		assertEquals(SemanticMetricEndpointResolver2D.Status.UNIQUE,
				resolution.getStatus(), resolution.getDiagnostic());
		assertEquals(SemanticMetricEndpointResolver2D.Family.PAIR_INTERSECTION_OCCURRENCE,
				resolution.getFamily());
		String metric = "LC=LocusLength(" + copiedRing.getLabelSimple() + ","
				+ copyOf(generator).getLabelSimple() + "," + copied.getLabelSimple() + ")";
		assertEquals(length, finite(add(metric)), 1E-9);
		// No cross-link: moving and then deleting the original leaves the copy current.
		moveTo("A", 40.78, 2.24);
		moveTo("B", 42.56, 4.78);
		assertFalse(point.isDefined());
		assertTrue(copied.isDefined());
		assertEquals(length, finite((GeoLocusMetricResult) requireLookup("LC")), 1E-9);
		requireLookup("A").remove();
		assertNull(lookup("X"));
		assertTrue(copied.isDefined());
		// XML reopen reconstructs the copied relation from the DAG and the ledger.
		String copiedLabel = copied.getLabelSimple();
		getApp().setXML(getApp().getXML(), true);
		GeoPoint reopened = (GeoPoint) requireLookup(copiedLabel);
		assertTrue(reopened.isDefined());
		assertEquals(copiedToken, effectiveToken(reopened));
		assertEquals(length, finite((GeoLocusMetricResult) requireLookup("LC")), 1E-9);
	}

	@Test
	void aCopiedLedgerThatNamesTheOriginalDriverFailsClosedOnImport() {
		getKernel().setContinuous(false);
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		GeoPoint driver = add("C=Point(c)");
		add("K=(1,1)");
		add("M=Midpoint(C,K)");
		add("ring=LocusV2(M,C)");
		add("H=SplineV2({(-3,1),(1,2),(5,1)},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(H,ring)");
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		paste(point);
		GeoLocusIntersectionResult copiedRich = (GeoLocusIntersectionResult) copyOf(rich);
		String state = copiedRich.getTokenLedgerState();
		new LocusIntersectionTokenLedger2D().importState(state);
		// Rewrite every field of the copied pair entries to name the original driver:
		// the only remaining inconsistency is the cross-link to the original graph.
		String tampered = replaceInCurrentPairEntries(state,
				id(copyOf(driver)).toExternalForm(), id(driver).toExternalForm());
		assertNotEquals(state, tampered);
		assertThrows(IllegalArgumentException.class,
				() -> new LocusIntersectionTokenLedger2D().importState(tampered));
	}

	@Test
	void compatibleAndIncompatibleRedefineFollowTheLifecycle() {
		witness();
		GeoLocusIntersectionResult rich = add("e=Intersect(d,a)");
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		String token = selectedToken(point);
		PersistentGeoId pointId = id(point);
		// Compatible redefines of upstream inputs keep the slot and point.
		for (String[] redefine : new String[][] {{"G", "(0.5,4.1)"}, {"H", "(6.2,-1.3)"},
				{"B", "(2.6,4.7)"}}) {
			assertTrue(change(requireLookup(redefine[0]), redefine[1], false).isBlank(),
					redefine[0]);
			GeoPoint kept = (GeoPoint) requireLookup("X");
			assertTrue(kept.isDefined(), redefine[0] + "\n"
					+ diagnostic((GeoLocusIntersectionResult) lookup("e")));
			assertEquals(pointId, id(kept));
			assertEquals(token, selectedToken(kept));
		}
		// Explicit replacement of a source is fresh: no label or coordinate carries
		// the old slot, and the materialized child does not survive.
		assertTrue(change(requireLookup("a"), "SplineV2(l1,b)", true).isBlank());
		assertNull(getConstruction().getSpatialIdentityRegistry().getGeo(pointId));
	}

	@Test
	void corruptedOrUnknownPairLedgerFailsClosed() {
		witness();
		GeoLocusIntersectionResult rich = add("e=Intersect(d,a)");
		materialize(rich, "X", eligible(rich).get(0));
		String state = rich.getTokenLedgerState();
		assertTrue(state.startsWith("5|"), state);
		new LocusIntersectionTokenLedger2D().importState(state);
		for (String corrupted : List.of("9" + state.substring(1), "4" + state.substring(1),
				state.substring(0, state.length() - 3), state.replace("|", "||"))) {
			assertThrows(IllegalArgumentException.class,
					() -> new LocusIntersectionTokenLedger2D().importState(corrupted),
					corrupted.substring(0, Math.min(24, corrupted.length())));
		}
	}

	private void witness() {
		getKernel().setContinuous(false);
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("f=Line(C,xAxis)");
		add("D=Intersect(f,yAxis)");
		add("E=Midpoint(D,C)");
		add("a=LocusV2(E,C)");
		add("F=(-3,1)");
		add("G=(0.56,3.94)");
		add("H=(6.28,-1.36)");
		add("l1={F,G,H}");
		add("b=3");
		add("d=SplineV2(l1,b)");
	}

	private Map<String, String> addresses(GeoLocusIntersectionResult rich) {
		String spline = id(requireLookup("d")).toExternalForm();
		Map<String, String> result = new TreeMap<>();
		for (LocusIntersectionSolution2D root : eligible(rich)) {
			LocusPairIntersectionEvidence2D pair = root.getPairEvidence().orElseThrow();
			boolean first = pair.getFirst().getLocusIdentity().equals(spline);
			double u = first ? pair.getFirst().getSemanticParameter()
					: pair.getSecond().getSemanticParameter();
			double v = first ? pair.getSecond().getSemanticParameter()
					: pair.getFirst().getSemanticParameter();
			result.put(String.format("%.12f", u), String.format("%.12f", v));
		}
		return result;
	}

	private static List<String> selectorEvidence(GeoLocusIntersectionResult rich) {
		return rich.getIntersectionResult().getDiagnostics().stream()
				.map(Object::toString).filter(text -> text.contains("D2 selector"))
				.sorted().toList();
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

	private GeoPoint materialize(GeoLocusIntersectionResult rich, String label,
			LocusIntersectionSolution2D solution) {
		GeoText token = new GeoText(getConstruction(), solution.getIdentity().getRootToken());
		token.setAuxiliaryObject(true);
		token.setEuclidianVisible(false);
		return LocusV2PublicOperations.selectIntersectionPoint(getConstruction(),
				label, rich, token);
	}

	private PersistentGeoId id(GeoElement geo) {
		PersistentGeoId id = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(geo);
		assertNotNull(id, geo.getLabelSimple());
		return id;
	}

	private static String selectedToken(GeoPoint point) {
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm()).getSelectedRootToken();
	}

	private static String effectiveToken(GeoPoint point) {
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
				.getEffectiveRootToken();
	}

	private void paste(GeoElement geo) {
		String clipboard = InternalClipboard.getTextToSave(getApp(), List.of(geo),
				text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(Arrays.asList(clipboard.substring(0, separator).split(" "))),
				clipboard.substring(separator));
	}

	/** @return the geo whose identity record names {@code original} as copy source */
	private GeoElement copyOf(GeoElement original) {
		PersistentGeoId source = id(original);
		var registry = getConstruction().getSpatialIdentityRegistry();
		return registry.getGeo(registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(record -> source.equals(record.getCopySourceId()))
				.findFirst().orElseThrow().getId());
	}

	private static String replaceInCurrentPairEntries(String state, String from,
			String to) {
		java.util.HexFormat hex = java.util.HexFormat.of();
		String[] fields = state.split("\\|", -1);
		String[] snapshot = fields[2].split("~", -1);
		for (int index = 5; index < snapshot.length; index++) {
			String[] entry = snapshot[index].split(",", -1);
			if (!"P".equals(entry[0])) {
				continue;
			}
			for (int field : new int[] {3, 5, 7}) {
				String decoded = new String(hex.parseHex(entry[field]),
						java.nio.charset.StandardCharsets.UTF_8);
				entry[field] = hex.formatHex(decoded.replace(from, to)
						.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			}
			snapshot[index] = String.join(",", entry);
		}
		fields[2] = String.join("~", snapshot);
		return String.join("|", fields);
	}

	private Set<String> identityRecords() {
		return getConstruction().getSpatialIdentityRegistry().getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.map(record -> record.getId().toExternalForm()).collect(Collectors.toSet());
	}

	private static double finite(GeoLocusMetricResult result) {
		return result.getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow(() -> new AssertionError(result.getMetricResult()
						.getComputationStatus() + " " + result.getMetricResult().getDiagnostics()));
	}

	private void move(String label, double value) {
		GeoNumeric number = (GeoNumeric) requireLookup(label);
		number.setValue(value);
		number.updateCascade();
	}

	private void moveTo(String label, double x, double y) {
		GeoPoint point = (GeoPoint) requireLookup(label);
		point.setCoords(x, y, 1);
		point.updateCascade();
	}

	private String change(GeoElement target, String definition, boolean replacement) {
		ErrorAccumulator errors = new ErrorAccumulator();
		AtomicReference<GeoElementND> result = new AtomicReference<>();
		EvalInfo info = new EvalInfo(true, true)
				.withSymbolicMode(AlgebraProcessor.getRedefinitionMode(target, getKernel()))
				.withLabelRedefinitionAllowedFor(target.getLabelSimple())
				.withSymbolic(true).withSliders(true);
		if (replacement) {
			info = info.withSpatialReplacementOperation();
		}
		getKernel().getAlgebraProcessor().changeGeoElementNoExceptionHandling(
				target, definition, info, false, result::set, errors);
		return errors.getErrors();
	}

	private static String diagnostic(GeoLocusIntersectionResult rich) {
		return rich.getIntersectionResult() == null ? "unpublished" : rich.getIntersectionResult()
				.getDiagnostics().stream().map(Object::toString).collect(Collectors.joining("\n"));
	}
}
