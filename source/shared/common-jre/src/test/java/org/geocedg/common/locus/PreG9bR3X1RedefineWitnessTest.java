/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.PairSemanticSlotSelector2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoVector;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3-X1 on the R3 author witness: a compatible redefine of the traced
 * point into supported expression-point syntax keeps the R3 retain semantics
 * unchanged, and the spline pair {@code e = Intersect(d,a)}, rich-only on the
 * base after that redefine, stays certified, so its materialized point keeps its
 * token and follows the recomputation. Unsupported syntax fails closed.
 */
class PreG9bR3X1RedefineWitnessTest extends PreG9bR3RedefineTestBase {
	private static final double TOLERANCE = 1E-9;
	private static final String[] MATERIALIZED = {"I", "J", "K", "L"};
	/** Circle(A,B) with A=(1.6,0), B=(3.6,0) meets the redefined ellipse here. */
	private static final double[][] REDEFINED_INTERSECTIONS = {
			{1.0, -Math.sqrt(3.64)}, {1.0, Math.sqrt(3.64)},
			{1.4, -Math.sqrt(3.96)}, {1.4, Math.sqrt(3.96)}};

	@Test
	void redefiningIntoTheWitnessExpressionKeepsTheCertifiedPairAndItsPoint()
			throws Exception {
		activateUndo();
		witness();
		Map<String, PersistentGeoId> identities = new LinkedHashMap<>();
		for (String label : List.of("E", "a", "d", "e", "g", "X", "I", "J", "K", "L")) {
			identities.put(label, id(label));
		}
		GeoLocusIntersectionResult e = rich("e");
		final String token = token("X");
		final PairSemanticSlotSelector2D selector = e.getRetainedPairSelector(token)
				.orElseThrow();
		assertOnEllipse(point("X"), 0.8);
		assertEquals(CertifiedConstructionProgram2D.VERSION, program("a").getVersion());
		final GeoIdentityRecord before = record("E");
		getApp().storeUndoInfo();

		Outcome outcome = redefine("E", "Midpoint(D,C+(1,0))",
				PreG9bR3RedefineTestBase::retainWhenAvailable);
		assertEquals(SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				outcome.status());
		assertTrue(outcome.succeeded(), outcome.errors());
		assertRedefined(identities, before, token, selector, 1.3);
		final String afterXml = getApp().getXML();

		getKernel().undo();
		for (Map.Entry<String, PersistentGeoId> identity : identities.entrySet()) {
			assertEquals(identity.getValue(), id(identity.getKey()), identity.getKey());
		}
		assertEquals(before.getDefinitionRevision(), record("E").getDefinitionRevision());
		assertTrue(point("X").isDefined());
		assertEquals(token, token("X"));
		assertOnEllipse(point("X"), 0.8);
		assertEquals(CertifiedConstructionProgram2D.VERSION, program("a").getVersion());

		getKernel().redo();
		assertEquals(spatialGeoRecords(afterXml), spatialGeoRecords(getApp().getXML()));
		assertRedefined(identities, before, token, selector, 1.3);

		AppCommon reopened = openInFreshApp(afterXml);
		assertEquals(spatialGeoRecords(afterXml), spatialGeoRecords(reopened.getXML()));
		GeoPoint reopenedPoint = (GeoPoint) reopened.getKernel().lookupLabel("X");
		assertTrue(reopenedPoint.isDefined());
		assertEquals(identities.get("X"), reopened.getKernel().getConstruction()
				.getSpatialIdentityRegistry().getPersistentGeoId(reopenedPoint));
		assertEquals(token, ((AlgoLocusIntersectionPointV2) reopenedPoint
				.getParentAlgorithm()).getSelectedRootToken());
		assertOnEllipse(reopenedPoint, 1.3);
		GeoLocusIntersectionResult reopenedPair = (GeoLocusIntersectionResult) reopened
				.getKernel().lookupLabel("e");
		assertEquals(2, eligible(reopenedPair).size());
	}

	@Test
	void theDirectWitnessExpressionCertifiesTheSamePair() {
		witnessCore("(1.6,0)", "(3.6,0)");
		assertTrue(redefine("E", "Midpoint(D,C+(1,0))",
				PreG9bR3RedefineTestBase::retainWhenAvailable).succeeded());
		spline();
		add("Ed=Midpoint(D,C+(1,0))");
		add("ad=LocusV2(Ed,C)");
		GeoLocusIntersectionResult direct = add("ed=Intersect(d,ad)");
		assertEquals(2, eligible(direct).size());
		assertEquals(CertifiedConstructionProgram2D.VERSION_V2, program("ad").getVersion());
		assertEquals(program("a").getSignature(), program("ad").getSignature());
	}

	@Test
	void aRedefineBetweenSupportedVectorsKeepsTheSlot() {
		witness();
		final String token = token("X");
		final PersistentGeoId pointId = id("X");
		String[][] definitions = {{"Midpoint(D,C+(1,0))", "1.3"},
				{"Midpoint(D,C+(0.6,0))", "1.1"}, {"Midpoint(D,C+u)", "1.3"}};
		add("u=(1,0)");
		for (String[] definition : definitions) {
			Outcome outcome = redefine("E", definition[0],
					PreG9bR3RedefineTestBase::retainWhenAvailable);
			assertTrue(outcome.succeeded(), definition[0] + " " + outcome.errors());
			assertEquals(ids("C"), record("E").getDependencies(), definition[0]);
			assertEquals(2, eligible(rich("e")).size(), definition[0]);
			assertTrue(point("X").isDefined(), definition[0]);
			assertEquals(pointId, id("X"));
			assertEquals(token, token("X"), definition[0]);
			assertOnEllipse(point("X"), Double.parseDouble(definition[1]));
		}
		// The named vector drives the certified curve: the point follows it.
		GeoVector u = assertInstanceOf(GeoVector.class, requireLookup("u"));
		u.setCoords(0.4, 0, 0);
		u.updateCascade();
		assertTrue(point("X").isDefined());
		assertEquals(token, token("X"));
		assertOnEllipse(point("X"), 1.0);
	}

	@Test
	void aRedefineIntoAnUnsupportedExpressionIsTruthfullyRichOnly() {
		activateUndo();
		witness();
		final String token = token("X");
		final PersistentGeoId pointId = id("X");
		final double x = point("X").getInhomX();
		getApp().storeUndoInfo();
		Outcome outcome = redefine("E", "Midpoint(D,(x(C)/2+1,y(C)))",
				PreG9bR3RedefineTestBase::retainWhenAvailable);
		assertTrue(outcome.succeeded(), outcome.errors());
		assertEquals(ids("C"), record("E").getDependencies());
		GeoLocusIntersectionResult e = rich("e");
		assertTrue(eligible(e).isEmpty());
		assertFalse(e.getIntersectionResult().getFiniteSolutions().isEmpty());
		assertTrue(e.getIntersectionResult().getDiagnostics().toString()
				.contains("no certified interval curve model"));
		// The claim stays dormant; the point is never moved to another root.
		assertFalse(point("X").isDefined());
		assertEquals(pointId, id("X"));
		assertEquals(token, token("X"));
		getKernel().undo();
		assertTrue(point("X").isDefined());
		assertEquals(token, token("X"));
		assertEquals(x, point("X").getInhomX(), 1E-12);
	}

	/**
	 * The R3 witness with one point materialized from the certified spline pair
	 * {@code e} and four points materialized from {@code g = Intersect(a,c)}.
	 */
	private void witness() {
		witnessCore("(1.6,0)", "(3.6,0)");
		spline();
		GeoLocusIntersectionResult e = add("e=Intersect(d,a)");
		List<LocusIntersectionSolution2D> roots = eligible(e);
		assertEquals(2, roots.size());
		assertNotNull(add("X=Intersect(e,\"" + roots.get(0).getIdentity().getRootToken()
				+ "\")"));
		GeoLocusIntersectionResult g = add("g=Intersect(a,c)");
		assertEquals(4, g.getIntersectionResult().getFiniteSolutions().size());
		for (int index = 0; index < MATERIALIZED.length; index++) {
			String root = g.getIntersectionResult().getFiniteSolutions().get(index)
					.getIdentity().getRootToken();
			assertNotNull(add(MATERIALIZED[index] + "=Intersect(g,\"" + root + "\")"));
		}
	}

	private void spline() {
		add("F=(-3,0.5)");
		add("G=(1,1)");
		add("H=(5,0.5)");
		add("l1={F,G,H}");
		add("b=3");
		assertNotNull(add("d=SplineV2(l1,b)"));
	}

	private void assertRedefined(Map<String, PersistentGeoId> identities,
			GeoIdentityRecord before, String token, PairSemanticSlotSelector2D selector,
			double centre) {
		for (Map.Entry<String, PersistentGeoId> identity : identities.entrySet()) {
			assertEquals(identity.getValue(), id(identity.getKey()), identity.getKey());
		}
		// R3 compatible-retain semantics are unchanged.
		GeoIdentityRecord after = record("E");
		assertEquals(ids("C"), after.getDependencies());
		assertEquals(before.getDefinitionRevision() + 1, after.getDefinitionRevision());
		assertEquals(before.getTopologyRevision(), after.getTopologyRevision());
		// The pair stays certified under class v2 and keeps the same slot.
		assertEquals(CertifiedConstructionProgram2D.VERSION_V2, program("a").getVersion());
		GeoLocusIntersectionResult e = rich("e");
		assertEquals(2, eligible(e).size(), e.getIntersectionResult().getDiagnostics()
				.toString());
		assertTrue(e.getTokenLedgerState().startsWith("5|"));
		GeoPoint x = point("X");
		assertTrue(x.isDefined());
		assertEquals(token, token("X"));
		assertEquals(selector, e.getRetainedPairSelector(token).orElseThrow());
		assertOnEllipse(x, centre);
		LocusIntersectionSolution2D bound = e.getIntersectionResult()
				.findPointAdmissibleSolution(token).orElseThrow();
		assertEquals(bound.getEvaluatedPoint().getX(), x.getInhomX(), TOLERANCE);
		assertEquals(bound.getEvaluatedPoint().getY(), x.getInhomY(), TOLERANCE);
		// g and its materialized roots are unaffected by the pair capability.
		GeoLocusIntersectionResult g = rich("g");
		assertSamePoints(List.of(REDEFINED_INTERSECTIONS), solutionPoints(g), 1E-6);
		for (String label : MATERIALIZED) {
			GeoPoint point = point(label);
			assertTrue(point.isDefined(), label);
			assertOnEllipse(point, centre);
		}
	}

	/** Validation only: (x - centre)^2 + y^2 / 4 = 1. */
	private static void assertOnEllipse(GeoPoint point, double centre) {
		double dx = point.getInhomX() - centre;
		assertEquals(1, dx * dx + point.getInhomY() * point.getInhomY() / 4, TOLERANCE,
				point.toString());
	}

	private CertifiedConstructionProgram2D program(String label) {
		GeoLocusV2 locus = assertInstanceOf(GeoLocusV2.class, requireLookup(label));
		return assertInstanceOf(ReconstructibleLocusEvaluator2D.class,
				locus.getSemanticDefinition().getEvaluatorCapability())
				.captureCertifiedProgram().orElseThrow();
	}

	private GeoLocusIntersectionResult rich(String label) {
		return assertInstanceOf(GeoLocusIntersectionResult.class, requireLookup(label));
	}

	private GeoPoint point(String label) {
		return assertInstanceOf(GeoPoint.class, requireLookup(label));
	}

	private String token(String label) {
		GeoElement point = requireLookup(label);
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
				.getSelectedRootToken();
	}

	private static List<LocusIntersectionSolution2D> eligible(
			GeoLocusIntersectionResult rich) {
		assertNotNull(rich.getIntersectionResult());
		return rich.getIntersectionResult().getFiniteSolutions().stream().filter(root ->
				rich.isPointAdmissible(root.getIdentity().getRootToken())).toList();
	}
}
