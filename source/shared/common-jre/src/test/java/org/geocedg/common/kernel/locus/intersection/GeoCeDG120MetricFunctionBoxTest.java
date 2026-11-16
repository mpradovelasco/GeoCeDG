/* GeoCeDG. Copyright (c) 2026 GeoCeDG contributors. SPDX-License-Identifier: EUPL-1.2. */
package org.geocedg.common.kernel.locus.intersection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.algos.AlgoLocusBetweenMetricV2;
import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.algos.AlgoSemanticLocusPoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D;
import org.geocedg.common.kernel.locus.metric.LocusMetricBoxEvidence2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/** Command-to-proof oracles: metric estimates never replace rigorous function enclosures. */
class GeoCeDG120MetricFunctionBoxTest extends BaseUnitTest {
	private static final double TURN = 4 * Math.PI;

	private record Fixture(GeoLocusV2 source, GeoNumeric radius, GeoNumeric origin,
			GeoPoint driver) { }

	private record Query(GeoNumeric scalar, AlgoLocusMetricScalarAdapter adapter,
			AlgoLocusBetweenMetricV2 metric) { }

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	private Fixture source(boolean declaredCut) {
		getKernel().setContinuous(false);
		GeoNumeric radius = add("radius=2");
		add("O=(0,0)");
		add("C=Circle(O,radius)");
		GeoPoint driver = assertInstanceOf(GeoPoint.class, add("E=Point(C)"));
		add("u=Vector((0,0),(0,1))");
		assertInstanceOf(GeoPoint.class, add("Q=Translate(E,u)"));
		if (declaredCut) { add("K=Point(C,0.5)"); }
		GeoLocusV2 source = add(declaredCut
				? "L=LocusV2(Q,E,\"constructive-cuts/v1\",K)"
				: "L=LocusV2(Q,E)");
		var topology = assertInstanceOf(AlgoDependentPointLocusV2.class,
				source.getParentAlgorithm()).getHybridExistenceCertificate().getStructure();
		assertEquals(LocusExistenceStructure2D.Completeness.COMPLETE,
				topology.getCompleteness());
		assertEquals(!declaredCut, topology.isClosedPeriodic());
		assertEquals(1, topology.getContinuousValidComponents().size());
		if (declaredCut) {
			var component = topology.getContinuousValidComponents().get(0);
			assertEquals(0, component.getStart());
			assertTrue(component.isFullLength());
			assertTrue(component.getLowerBoundary().isBinaryEndIncluded());
			assertFalse(component.getUpperBoundary().isBinaryEndIncluded());
		}
		move(driver, 1);
		GeoNumeric origin = add("origin=0");
		assertInstanceOf(GeoPoint.class, add("P=Point(L,\"generator.main\",origin)"));
		return new Fixture(source, radius, origin, driver);
	}

	private Query query(String label, String direction, String samePosition) {
		GeoNumeric scalar = add(label + "=LocusLength(L,P,Q,\"" + direction
				+ "\",\"strict\",\"" + samePosition + "\")");
		var adapter = assertInstanceOf(AlgoLocusMetricScalarAdapter.class,
				scalar.getParentAlgorithm());
		var metric = assertInstanceOf(AlgoLocusBetweenMetricV2.class,
				adapter.getRichInput().getParentAlgorithm());
		return new Query(scalar, adapter, metric);
	}

	private static void move(GeoPoint driver, double parameter) {
		driver.getPathParameter().setT(parameter);
		driver.getPath().pathChanged(driver);
		driver.updateCoords();
		driver.updateCascade();
	}

	private static LocusMetricBoxEvidence2D box(Query query, double lower, double upper) {
		var proof = query.metric().getFunctionBoxEvidence(lower, upper);
		assertNotNull(proof, () -> query.metric().getFunctionStructure().getEvidence());
		assertTrue(proof.getEvidence().startsWith(LocusMetricBoxEvidence2D.VERSION));
		return proof;
	}

	private static void valueContains(LocusMetricBoxEvidence2D proof, double value) {
		assertTrue(proof.getLower() <= value && value <= proof.getUpper(),
				() -> proof.getLower() + " <= " + value + " <= " + proof.getUpper());
	}

	private static void derivativeContains(LocusMetricBoxEvidence2D proof, double value) {
		assertTrue(proof.getDerivativeLower() <= value && value <= proof.getDerivativeUpper());
	}

	private static void zero(LocusMetricBoxEvidence2D proof) {
		valueContains(proof, 0);
		assertTrue(proof.getUpper() < 1E-12, "The ordinary origin must not acquire a full turn");
	}

	private double currentCanonicalOrigin(Fixture fixture) {
		var origin = assertInstanceOf(AlgoSemanticLocusPoint2D.class,
				lookup("P").getParentAlgorithm()).getCurrentSemanticAddress();
		assertNotNull(origin);
		assertEquals(fixture.source().getSemanticDefinition().getProvider()
				.canonicalize(fixture.origin().getDouble()), origin.getCanonicalParameter(), 0);
		return origin.getCanonicalParameter();
	}

	@Test
	void forwardProofSeparatesOrdinaryOriginLateralClosuresAndCrossingBoxes() {
		source(false);
		Query query = query("m", "forward", "zero-length");
		assertTrue(query.scalar().isDefined());
		assertEquals(2, query.scalar().getDouble(), 1E-6);
		zero(box(query, 0, 0));
		var left = box(query, -.1, 0);
		valueContains(left, TURN - .2);
		valueContains(left, TURN);
		assertTrue(left.getLower() > TURN / 2);
		assertTrue(left.getEvidence().contains(":below"));
		derivativeContains(left, 2);
		var right = box(query, 0, .1);
		valueContains(right, 0);
		valueContains(right, .2);
		assertTrue(right.getUpper() < 1);
		assertTrue(right.getEvidence().contains(":above"));
		derivativeContains(right, 2);
		assertNull(query.metric().getFunctionBoxEvidence(-.1, .1),
				"A positive-width box must not straddle the origin wrap");
		var seam = box(query, Math.PI, Math.nextUp(Math.PI));
		valueContains(seam, 2 * Math.PI);
		derivativeContains(seam, 2);
		assertTrue(seam.getEvidence().contains("circle-seam:below"));
	}

	@Test
	void reverseProofReversesTheLateralRolesAndDerivativeWithoutChangingTheOrdinaryZero() {
		source(false);
		Query query = query("m", "reverse", "zero-length");
		assertEquals(TURN - 2, query.scalar().getDouble(), 1E-6);
		zero(box(query, 0, 0));
		var left = box(query, -.1, 0);
		valueContains(left, 0);
		valueContains(left, .2);
		assertTrue(left.getUpper() < 1);
		derivativeContains(left, -2);
		assertTrue(left.getDerivativeUpper() < 0);
		var right = box(query, 0, .1);
		valueContains(right, TURN - .2);
		valueContains(right, TURN);
		assertTrue(right.getLower() > TURN / 2);
		derivativeContains(right, -2);
		assertNull(query.metric().getFunctionBoxEvidence(-.1, .1));
	}

	@Test
	void closedSamePositionPoliciesChangeThePointValueButNotEitherRegularLimit() {
		Fixture fixture = source(false);
		move(fixture.driver(), 0);
		for (String direction : new String[] {"forward", "reverse"}) {
			Query ordinary = query("zero" + direction, direction, "zero-length");
			Query cycle = query("cycle" + direction, direction, "full-cycle");
			assertEquals(0, ordinary.scalar().getDouble(), 0);
			assertEquals(TURN, cycle.scalar().getDouble(), 1E-6);
			zero(box(ordinary, 0, 0));
			valueContains(box(cycle, 0, 0), TURN);
			var ordinaryLeft = box(ordinary, -.1, 0);
			var cycleLeft = box(cycle, -.1, 0);
			assertEquals(ordinaryLeft.getLower(), cycleLeft.getLower(), 0);
			assertEquals(ordinaryLeft.getUpper(), cycleLeft.getUpper(), 0);
			var ordinaryRight = box(ordinary, 0, .1);
			var cycleRight = box(cycle, 0, .1);
			assertEquals(ordinaryRight.getLower(), cycleRight.getLower(), 0);
			assertEquals(ordinaryRight.getUpper(), cycleRight.getUpper(), 0);
		}
	}

	@Test
	void declaredOpenTurnHasAnExcludedLimitWithoutAuthorizingAnOrdinaryFullCycle() {
		Fixture fixture = source(true);
		Query query = query("m", "forward", "zero-length");
		assertTrue(query.metric().getFunctionStructure().isCertified());
		zero(box(query, 0, 0));
		valueContains(box(query, -.1, 0), TURN);
		valueContains(box(query, 0, .1), .2);
		assertNull(query.metric().getFunctionBoxEvidence(-.1, .1));
		move(fixture.driver(), 0);
		Query forbidden = query("ordinaryCycle", "forward", "full-cycle");
		assertFalse(forbidden.scalar().isDefined());
		assertFalse(forbidden.metric().getFunctionStructure().isCertified());
		assertNull(forbidden.metric().getFunctionBoxEvidence(0, .1));
		assertEquals(0, query.scalar().getDouble(), 0);
		query.metric().setOppositeSamePositionForLimit(true);
		try {
			query.metric().compute();
			assertEquals(TURN, query.adapter().getRichInput().getMetricResult()
					.getMetricValue().getFiniteValue().orElseThrow(), 1E-6);
			zero(box(query, 0, 0));
		} finally {
			query.metric().setOppositeSamePositionForLimit(false);
			query.metric().compute();
			query.adapter().compute();
		}
		assertEquals(0, query.scalar().getDouble(), 0);
		forbidden.metric().compute();
		forbidden.adapter().compute();
		assertFalse(forbidden.scalar().isDefined(), "The limit override must not leak");
	}

	@Test
	void anInteriorOriginOfAnOpenTurnRemainsOutsideTheFullTurnProofClass() {
		Fixture fixture = source(true);
		fixture.origin().setValue(.4);
		fixture.origin().updateCascade();
		Query query = query("m", "forward", "zero-length");
		assertTrue(query.scalar().isDefined(), "The ordinary partial route is still admissible");
		assertEquals(1.2, query.scalar().getDouble(), 1E-6);
		var structure = query.metric().getFunctionStructure();
		assertFalse(structure.isCertified());
		assertTrue(structure.getEvidence().startsWith("open-full-arc-premises-not-established"));
		assertNull(query.metric().getFunctionBoxEvidence(.5, .7));
		fixture.origin().setValue(0);
		fixture.origin().updateCascade();
		assertTrue(query.metric().getFunctionStructure().isCertified());
		valueContains(box(query, .5, .7), 1);
		valueContains(box(query, .5, .7), 1.4);
	}

	@Test
	void proofBoundsFollowRadiusRevisionAndOriginAndRecoverAfterDegeneration() {
		Fixture fixture = source(false);
		Query query = query("m", "forward", "zero-length");
		var before = box(query, 1, 1);
		valueContains(before, 2);
		long initialRevision = fixture.source().getSemanticRevision();
		fixture.radius().setValue(3);
		fixture.radius().updateCascade();
		assertNotEquals(initialRevision, fixture.source().getSemanticRevision());
		var enlarged = box(query, 1, 1);
		valueContains(enlarged, 3);
		assertTrue(enlarged.getLower() > before.getUpper(), "A stale radius proof would miss this");
		assertTrue(enlarged.getEvidence().contains(
				"revision=" + fixture.source().getSemanticRevision()));
		fixture.origin().setValue(.4);
		fixture.origin().updateCascade();
		// The proof binds the currently resolved canonical address, including the
		// provider's periodic normalization, rather than the raw literal's bit pattern.
		double canonicalOrigin = currentCanonicalOrigin(fixture);
		var shifted = box(query, 1, 1);
		valueContains(shifted, 3 * (1 - canonicalOrigin));
		assertTrue(shifted.getUpper() < enlarged.getLower(), "A stale origin key would miss this");
		assertTrue(shifted.getEvidence().contains("|origin="
				+ Double.toHexString(canonicalOrigin) + "|"));
		fixture.radius().setValue(0);
		fixture.radius().updateCascade();
		assertNull(query.metric().getFunctionBoxEvidence(.5, .7));
		fixture.radius().setValue(2);
		fixture.radius().updateCascade();
		move(fixture.driver(), 1);
		valueContains(box(query, 1, 1), 2 * (1 - currentCanonicalOrigin(fixture)));
		derivativeContains(box(query, 1, 1), 2);
	}

	@Test
	void changingThePublicScalarCannotReplaceTheIndependentProofRange() {
		source(false);
		Query query = query("m", "forward", "zero-length");
		var before = box(query, .2, .4);
		valueContains(before, .4);
		valueContains(before, .8);
		assertTrue(before.getUpper() < query.scalar().getDouble(),
				"The requested proof box is not the current ordinary metric value");
		double richValue = query.adapter().getRichInput().getMetricResult()
				.getMetricValue().getFiniteValue().orElseThrow();
		query.scalar().setValue(1E6);
		var after = box(query, .2, .4);
		assertEquals(before.getLower(), after.getLower(), 0);
		assertEquals(before.getUpper(), after.getUpper(), 0);
		assertEquals(before.getDerivativeLower(), after.getDerivativeLower(), 0);
		assertEquals(before.getDerivativeUpper(), after.getDerivativeUpper(), 0);
		assertEquals(richValue, query.adapter().getRichInput().getMetricResult()
				.getMetricValue().getFiniteValue().orElseThrow(), 0);
		assertTrue(after.getUpper() < 1, "The scalar output is not proof authority");
		query.adapter().compute();
		assertEquals(richValue, query.scalar().getDouble(), 0);
	}
}
