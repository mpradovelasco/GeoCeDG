/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.algos.SemanticMetricEndpointResolver2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.Completeness;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.LocalIsolationStatus;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionEvidence2D;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.kernel.arithmetic.ExpressionNode;
import org.geogebra.common.kernel.arithmetic.MyVecNode;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.GeoVector;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3-X1: pairs whose construction locus is traced through a supported
 * expression point ({@code C+(1,0)}, {@code C+u}) certify and materialize their
 * transverse singleton roots through the unchanged certifier, selector, token and
 * ledger; unsupported expressions stay rich-only. Coordinates are checked only as
 * validation of already certified results.
 */
final class PreG9bR3X1ExpressionPointPairMaterializationTest
		extends G9U0PublicSurfaceTestBase {
	private static final double RADIUS = Math.hypot(2.56 - 0.78, 4.78 - 2.24);

	@Test
	void literalAndNamedVectorPairsMaterializeTheirCertifiedRoots() {
		witness();
		add("u=(1,0)");
		add("E1=C+(1,0)");
		add("p1=LocusV2(E1,C)");
		add("E2=C+u");
		add("p2=LocusV2(E2,C)");
		for (String locus : List.of("p1", "p2")) {
			assertEquals(CertifiedConstructionProgram2D.VERSION_V2, program(locus).getVersion());
			GeoLocusIntersectionResult rich = add("R" + locus + "=Intersect(d," + locus + ")");
			List<LocusIntersectionSolution2D> roots = eligible(rich);
			assertEquals(2, roots.size(), diagnostic(rich));
			// Local admissibility is not global completeness.
			assertEquals(Completeness.NOT_ESTABLISHED,
					rich.getIntersectionResult().getCompletenessEvidence().getCompleteness());
			assertTrue(rich.getTokenLedgerState().startsWith("5|"), rich.getTokenLedgerState());
			assertTrue(diagnostic(rich).contains("germ=1 status=UNIQUE"), diagnostic(rich));
			assertTrue(diagnostic(rich).contains("germ=-1 status=UNIQUE"), diagnostic(rich));
			for (int index = 0; index < roots.size(); index++) {
				LocusIntersectionSolution2D root = roots.get(index);
				LocusPairIntersectionEvidence2D pair = root.getPairEvidence().orElseThrow();
				assertEquals(LocalIsolationStatus.ESTABLISHED,
						pair.getLocalIsolation().getStatus());
				assertEquals(IdentityStatus.NEW_TOPOLOGICAL_SOLUTION,
						root.getIdentity().getIdentityStatus());
				GeoPoint point = materialize(rich, "X" + locus + index, root);
				assertTrue(point.isDefined());
				assertOnTranslatedCircle(point, 1, 0);
			}
		}
		// The literal and the vector object give the same curve; their roots agree as
		// validation evidence, while each pair keeps its own sources and tokens.
		assertNotEquals(tokens((GeoLocusIntersectionResult) requireLookup("Rp1")),
				tokens((GeoLocusIntersectionResult) requireLookup("Rp2")));
	}

	@Test
	void callerReversalYieldsTheSameCanonicalSelectors() {
		witness();
		add("E1=C+(1,0)");
		add("p1=LocusV2(E1,C)");
		GeoLocusIntersectionResult forward = add("F1=Intersect(d,p1)");
		GeoLocusIntersectionResult reverse = add("F2=Intersect(p1,d)");
		assertEquals(2, eligible(forward).size(), diagnostic(forward));
		assertEquals(2, eligible(reverse).size(), diagnostic(reverse));
		assertEquals(forward.getSourcePairIdentity(), reverse.getSourcePairIdentity());
		assertEquals(addresses(forward), addresses(reverse));
		assertEquals(selectorEvidence(forward), selectorEvidence(reverse));
		assertNotEquals(tokens(forward), tokens(reverse));
	}

	@Test
	void anUncertifiedExpressionIssuesNoPairToken() {
		witness();
		add("Ex=(x(C)/2,y(C))");
		add("ex=LocusV2(Ex,C)");
		GeoLocusIntersectionResult rich = add("Rx=Intersect(d,ex)");
		assertFalse(rich.getIntersectionResult().getFiniteSolutions().isEmpty());
		assertTrue(eligible(rich).isEmpty(), diagnostic(rich));
		assertTrue(rich.getTokenLedgerState().startsWith("4|"), rich.getTokenLedgerState());
		assertTrue(diagnostic(rich).contains("no certified interval curve model"),
				diagnostic(rich));
		assertTrue(evaluator("ex").captureCertifiedProgram().isEmpty());
	}

	@Test
	void aTangencyIsNotUpgradedToATransverseRoot() {
		witness();
		add("E1=C+(1,0)");
		add("p1=LocusV2(E1,C)");
		double top = 2.24 + RADIUS;
		add("top=SplineV2({(-3," + top + "),(1.78," + top + "),(6," + top + ")},3)");
		GeoLocusIntersectionResult rich = add("Rt=Intersect(top,p1)");
		assertTrue(eligible(rich).isEmpty(), diagnostic(rich));
		assertFalse(diagnostic(rich).contains("status=UNIQUE"), diagnostic(rich));
	}

	@Test
	void aMaterializedRootFollowsItsSlotWithoutRetargeting() {
		witness();
		add("u=(1,0)");
		add("E2=C+u");
		add("p2=LocusV2(E2,C)");
		GeoLocusIntersectionResult rich = add("R=Intersect(d,p2)");
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		final String token = selectedToken(point);
		final PersistentGeoId pointId = id(point);
		PairSemanticSlotSelector2D selector = rich.getRetainedPairSelector(token)
				.orElseThrow();
		int count = getConstruction().getGeoSetConstructionOrder().size();
		for (double[] vector : new double[][] {{1.1, 0.05}, {0.9, -0.1}, {1, 0}}) {
			moveVector("u", vector[0], vector[1]);
			assertTrue(point.isDefined(), diagnostic(rich));
			assertSame(point, lookup("X"));
			assertEquals(pointId, id(point));
			assertEquals(token, selectedToken(point));
			assertEquals(selector, rich.getRetainedPairSelector(token).orElseThrow());
			assertOnTranslatedCircle(point, vector[0], vector[1]);
		}
		moveTo("B", 2.6, 4.7);
		assertTrue(point.isDefined(), diagnostic(rich));
		assertEquals(token, selectedToken(point));
		assertEquals(count, getConstruction().getGeoSetConstructionOrder().size());
		// Losing the certified model leaves the claim dormant, never retargeted.
		moveTo("B", 0.78, 2.24);
		assertFalse(point.isDefined());
		moveTo("B", 2.56, 4.78);
		assertTrue(point.isDefined(), diagnostic(rich));
		assertEquals(token, selectedToken(point));
	}

	@Test
	void theExistingPairOccurrenceMakesAMaterializedRootAMetricEndpoint() {
		witness();
		add("E1=C+(1,0)");
		add("p1=LocusV2(E1,C)");
		GeoLocusIntersectionResult rich = add("R=Intersect(d,p1)");
		List<LocusIntersectionSolution2D> roots = eligible(rich);
		GeoPoint first = materialize(rich, "X0", roots.get(0));
		GeoPoint second = materialize(rich, "X1", roots.get(1));
		for (GeoPoint endpoint : List.of(first, second)) {
			SemanticMetricEndpointResolver2D.Resolution resolution =
					new SemanticMetricEndpointResolver2D().resolve(locus("p1"), endpoint);
			assertEquals(SemanticMetricEndpointResolver2D.Status.UNIQUE,
					resolution.getStatus(), resolution.getDiagnostic());
			assertEquals(SemanticMetricEndpointResolver2D.Family.PAIR_INTERSECTION_OCCURRENCE,
					resolution.getFamily());
		}
		double length = finite(add("L=LocusLength(p1,X0,X1)"));
		assertTrue(length > 0 && length < 2 * Math.PI * RADIUS, String.valueOf(length));
		// No new endpoint family: the traced expression point addresses its own locus
		// through the existing generator occurrence, and any other expression point
		// addresses nothing.
		SemanticMetricEndpointResolver2D.Resolution generator =
				new SemanticMetricEndpointResolver2D().resolve(locus("p1"),
						(GeoPoint) requireLookup("E1"));
		assertEquals(SemanticMetricEndpointResolver2D.Status.UNIQUE, generator.getStatus());
		assertEquals(SemanticMetricEndpointResolver2D.Family.GENERATOR_OCCURRENCE,
				generator.getFamily());
		GeoPoint other = add("Z=C+(0,1)");
		assertEquals(SemanticMetricEndpointResolver2D.Status.NO_ADDRESS,
				new SemanticMetricEndpointResolver2D().resolve(locus("p1"), other)
						.getStatus());
	}

	@Test
	void undoRedoAndReopenPreserveTheCertifiedSlot() throws Exception {
		activateUndo();
		witness();
		add("E1=C+(1,0)");
		add("p1=LocusV2(E1,C)");
		GeoLocusIntersectionResult rich = add("R=Intersect(d,p1)");
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		final String token = selectedToken(point);
		final PersistentGeoId pointId = id(point);
		final double x = point.getInhomX();
		String xml = getApp().getXML();
		assertFalse(xml.contains("certified-construction-program"));
		getApp().setXML(xml, true);
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
	void closureCopyOfALiteralExpressionLocusRecapturesAndStaysCurrent() {
		directDriver();
		GeoPoint generator = add("E=C+(1,0)");
		GeoLocusV2 ring = add("ring=LocusV2(E,C)");
		add("H=SplineV2({(-3,1),(1,2),(5,1)},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(H,ring)");
		assertEquals(2, eligible(rich).size(), diagnostic(rich));
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		String token = selectedToken(point);
		Set<String> originalGraph = identityRecords();
		paste(point);
		GeoPoint copied = (GeoPoint) copyOf(point);
		GeoLocusV2 copiedRing = (GeoLocusV2) copyOf(ring);
		GeoLocusIntersectionResult copiedRich = (GeoLocusIntersectionResult) copyOf(rich);
		GeoPoint copiedGenerator = (GeoPoint) copyOf(generator);
		for (GeoElement copy : List.of(copied, copiedRing, copyOf(requireLookup("C")),
				copiedRich, copiedGenerator)) {
			assertFalse(originalGraph.contains(id(copy).toExternalForm()),
					copy.getLabelSimple());
		}
		// The literal stays a literal node of the copied expression.
		ExpressionNode definition = copiedGenerator.getDefinition();
		assertNotNull(definition);
		assertInstanceOf(MyVecNode.class, definition.getRight().unwrap());
		assertEquals(CertifiedConstructionProgram2D.VERSION_V2,
				program(copiedRing.getLabelSimple()).getVersion());
		assertTrue(copied.isDefined(), diagnostic(copiedRich));
		String copiedToken = effectiveToken(copied);
		assertNotEquals(token, copiedToken);
		assertTrue(copiedRich.isPointAdmissible(copiedToken));
		PairSemanticSlotSelector2D selector = copiedRich.getRetainedPairSelector(
				copiedToken).orElseThrow();
		for (String identity : List.of(selector.getFirst().getSourceId(),
				selector.getSecond().getSourceId())) {
			assertFalse(originalGraph.contains(identity), identity);
		}
		assertEquals(Set.of(id(copyOf(requireLookup("C"))).toExternalForm()),
				selector.getDeclaredParticipants());
		// No cross-link: the original can move away and the copy stays current.
		moveTo("A", 40.78, 2.24);
		moveTo("B", 42.56, 4.78);
		assertFalse(point.isDefined());
		assertTrue(copied.isDefined());
		assertEquals(token, selectedToken(point));
	}

	@Test
	void closureCopyOfANamedVectorExpressionFollowsTheCopiedVector() {
		directDriver();
		GeoVector vector = add("u=(1,0)");
		GeoPoint generator = add("E=C+u");
		GeoLocusV2 ring = add("ring=LocusV2(E,C)");
		add("H=SplineV2({(-3,1),(1,2),(5,1)},3)");
		GeoLocusIntersectionResult rich = add("R=Intersect(H,ring)");
		GeoPoint point = materialize(rich, "X", eligible(rich).get(0));
		paste(point);
		GeoPoint copied = (GeoPoint) copyOf(point);
		GeoPoint copiedGenerator = (GeoPoint) copyOf(generator);
		GeoLocusV2 copiedRing = (GeoLocusV2) copyOf(ring);
		GeoLocusIntersectionResult copiedRich = (GeoLocusIntersectionResult) copyOf(rich);
		// The copied expression references the copied vector, never the original.
		GeoVector copiedVector = assertInstanceOf(GeoVector.class,
				copiedGenerator.getDefinition().getRight().unwrap());
		assertNotSame(vector, copiedVector);
		assertTrue(copied.isDefined(), diagnostic(copiedRich));
		assertEquals(CertifiedConstructionProgram2D.VERSION_V2,
				program(copiedRing.getLabelSimple()).getVersion());
		String copiedToken = effectiveToken(copied);
		double copiedX = copied.getInhomX();
		// Moving the original vector changes only the original curve.
		moveVector("u", 1.2, 0.1);
		assertTrue(point.isDefined(), diagnostic(rich));
		assertOnTranslatedCircle(point, 1.2, 0.1);
		assertEquals(copiedX, copied.getInhomX(), 1E-12);
		// Moving the copied vector moves the copied curve and its point.
		moveVector(copiedVector.getLabelSimple(), 0.8, -0.1);
		assertTrue(copied.isDefined(), diagnostic(copiedRich));
		assertEquals(copiedToken, effectiveToken(copied));
		assertOnTranslatedCircle(copied, 0.8, -0.1);
	}

	private void witness() {
		getKernel().setContinuous(false);
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("F=(-3,1)");
		add("G=(0.56,3.94)");
		add("H=(6.28,-1.36)");
		add("l1={F,G,H}");
		add("b=3");
		add("d=SplineV2(l1,b)");
	}

	private void directDriver() {
		getKernel().setContinuous(false);
		add("A=(0.78,2.24)");
		add("B=(2.56,4.78)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
	}

	/** Validation only: the point lies on the witness circle translated by v. */
	private void assertOnTranslatedCircle(GeoPoint point, double vx, double vy) {
		GeoPoint a = (GeoPoint) requireLookup("A");
		GeoPoint b = (GeoPoint) requireLookup("B");
		double radius = Math.hypot(b.getInhomX() - a.getInhomX(),
				b.getInhomY() - a.getInhomY());
		assertEquals(radius, Math.hypot(point.getInhomX() - a.getInhomX() - vx,
				point.getInhomY() - a.getInhomY() - vy), 1E-9, point.toString());
	}

	private CertifiedConstructionProgram2D program(String label) {
		return evaluator(label).captureCertifiedProgram().orElseThrow();
	}

	private ReconstructibleLocusEvaluator2D evaluator(String label) {
		LocusDefinition2D definition = locus(label).getSemanticDefinition();
		assertNotNull(definition, label);
		return assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
				definition.getEvaluatorCapability());
	}

	private GeoLocusV2 locus(String label) {
		return assertInstanceOf(GeoLocusV2.class, requireLookup(label));
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

	private static List<LocusIntersectionSolution2D> eligible(
			GeoLocusIntersectionResult rich) {
		assertNotNull(rich.getIntersectionResult());
		return rich.getIntersectionResult().getFiniteSolutions().stream().filter(root ->
				rich.isPointAdmissible(root.getIdentity().getRootToken())).toList();
	}

	private static Set<String> tokens(GeoLocusIntersectionResult rich) {
		return eligible(rich).stream().map(root -> root.getIdentity().getRootToken())
				.collect(Collectors.toSet());
	}

	private static String diagnostic(GeoLocusIntersectionResult rich) {
		return rich.getIntersectionResult() == null ? "no result"
				: rich.getIntersectionResult().getDiagnostics().toString();
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
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
				.getSelectedRootToken();
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

	private void moveTo(String label, double x, double y) {
		GeoPoint point = (GeoPoint) requireLookup(label);
		point.setCoords(x, y, 1);
		point.updateCascade();
	}

	private void moveVector(String label, double x, double y) {
		GeoVector vector = (GeoVector) requireLookup(label);
		vector.setCoords(x, y, 0);
		vector.updateCascade();
	}
}
