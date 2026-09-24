/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineAssessmentStatus;
import org.geocedg.common.kernel.spatial.identity.SpatialRedefineExecutionMode;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3 D3-a on a minimal fixture derived from the author witness: the
 * ordinary participant {@code E} whose direct input {@code D} has no durable
 * identity is redefined as a compatible retain, and every dependent recomputes.
 */
class PreG9bR3RedefineWitnessTest extends PreG9bR3RedefineTestBase {
	private static final double TOLERANCE = 1E-6;
	private static final String[] MATERIALIZED = {"I", "J", "K", "L"};
	/** Circle(A,B) with A=(1.6,0), B=(3.6,0) meets the redefined ellipse here. */
	private static final double[][] REDEFINED_INTERSECTIONS = {
			{1.0, -Math.sqrt(3.64)}, {1.0, Math.sqrt(3.64)},
			{1.4, -Math.sqrt(3.96)}, {1.4, Math.sqrt(3.96)}};

	@Test
	void witnessRedefineRetainsIdentityAndRecomputesEveryDependent() throws Exception {
		activateUndo();
		fullWitness();
		Map<String, PersistentGeoId> identities = new LinkedHashMap<>();
		for (String label : List.of("E", "a", "d", "e", "g", "I", "J", "K", "L")) {
			identities.put(label, id(label));
		}
		GeoIdentityRecord before = record("E");
		assertEquals(ids("C"), before.getDependencies());
		assertNull(registry().getPersistentGeoId(requireLookup("D")));
		assertNull(registry().getPersistentGeoId(requireLookup("M")));
		getApp().storeUndoInfo();
		String beforeXml = getApp().getXML();

		Outcome outcome = redefine("E", "Midpoint(D,C+(1,0))",
				PreG9bR3RedefineTestBase::retainWhenAvailable);
		assertEquals(SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				outcome.status());
		assertTrue(outcome.succeeded(), outcome.errors());
		assertRedefinedWitness(identities, before);
		String afterXml = getApp().getXML();

		getKernel().undo();
		assertEquals(spatialGeoRecords(beforeXml), spatialGeoRecords(getApp().getXML()));
		for (Map.Entry<String, PersistentGeoId> identity : identities.entrySet()) {
			assertEquals(identity.getValue(), id(identity.getKey()), identity.getKey());
		}
		assertEquals(before.getDefinitionRevision(), record("E").getDefinitionRevision());
		getKernel().redo();
		assertEquals(spatialGeoRecords(afterXml), spatialGeoRecords(getApp().getXML()));
		assertRedefinedWitness(identities, before);

		getApp().getXMLio().processXMLString(afterXml, true, false, false);
		assertEquals(spatialGeoRecords(afterXml), spatialGeoRecords(getApp().getXML()));
		assertRedefinedWitness(identities, before);
		AppCommon reopened = openInFreshApp(afterXml);
		assertEquals(spatialGeoRecords(afterXml), spatialGeoRecords(reopened.getXML()));
	}

	@Test
	void anIdenticalDefinitionIsANoOpRetain() {
		witnessCore("(1.6,0)", "(3.6,0)");
		GeoIdentityRecord before = record("E");
		String beforeXml = getApp().getXML();

		Outcome outcome = redefine("E", "Midpoint(D,C)",
				PreG9bR3RedefineTestBase::retainWhenAvailable);
		assertEquals(SpatialRedefineAssessmentStatus.ADVANCED_RETAIN_AVAILABLE,
				outcome.status());
		assertTrue(outcome.succeeded(), outcome.errors());
		assertEquals(before.getId(), id("E"));
		assertEquals(before.getDefinitionRevision(), record("E").getDefinitionRevision());
		assertEquals(before.getTopologyRevision(), record("E").getTopologyRevision());
		assertEquals(before.getDependencies(), record("E").getDependencies());
		assertEquals(spatialGeoRecords(beforeXml), spatialGeoRecords(getApp().getXML()));
	}

	@Test
	void aRealCycleIsStillAnInvalidDag() {
		witnessCore("(1.6,0)", "(3.6,0)");
		String beforeXml = getApp().getXML();
		Outcome outcome = redefine("E", "Midpoint(D,Point(a,\"" + BRANCH + "\",0.5))",
				always(SpatialRedefineExecutionMode.ADVANCED_RETAIN));
		assertEquals(SpatialRedefineAssessmentStatus.INVALID_DAG, outcome.status());
		assertNull(outcome.assessment().getImpactReport());
		assertEquals(beforeXml, getApp().getXML());
	}

	@Test
	void aGenuineFamilyChangeStillFailsClosedWithItsOwnReason() {
		witnessCore("(1.6,0)", "(3.6,0)");
		String beforeXml = getApp().getXML();
		for (SpatialRedefineExecutionMode mode : SpatialRedefineExecutionMode.values()) {
			Outcome outcome = redefine("E", "Circle(D,1)", always(mode));
			assertEquals(SpatialRedefineAssessmentStatus.DURABLE_CONTRACT_CHANGE,
					outcome.status());
			assertEquals("Candidate changes the construction-defined geo family",
					outcome.assessment().getDetail());
			assertFalse(outcome.assessment().isIdentityPreservingUpdateAvailable());
			assertFalse(outcome.assessment().isExplicitReplacementAvailable());
			assertEquals(beforeXml, getApp().getXML());
		}
	}

	@Test
	void anIncompleteStableRoleGroupStillFailsClosed() {
		getKernel().setContinuous(false);
		add("A=(1.6,0)");
		add("B=(3.6,0)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("f=Line(C,xAxis)");
		GeoElement first = add("Intersect(c,f)");
		GeoElement[] outputs = first.getParentAlgorithm().getOutput();
		assertTrue(outputs.length > 1);
		assertTrue(first.rename("P"));
		assertNotNull(add("a=LocusV2(P,C)"));
		assertNotNull(registry().getPersistentGeoId(first));
		for (GeoElement output : outputs) {
			if (output != first) {
				assertNull(registry().getPersistentGeoId(output));
			}
		}
		// The group is incomplete, so the host refuses the explicit context before
		// any assessment or mutation, whether or not the frontier would change.
		String beforeXml = getApp().getXML();
		for (String candidate : List.of("Midpoint(C,C+(1,0))", "Midpoint(C,A)")) {
			Outcome outcome = redefine("P", candidate,
					always(SpatialRedefineExecutionMode.ADVANCED_RETAIN));
			assertNull(outcome.assessment(), candidate);
			assertFalse(outcome.succeeded(), candidate);
			assertEquals(beforeXml, getApp().getXML(), candidate);
		}
		GeoElement candidate = add("k=Midpoint(C,A)");
		assertEquals(SpatialRedefineAssessmentStatus.STALE_ASSESSMENT, registry()
				.assessRedefine(registry().captureRedefineContext(first), candidate,
						List.of(candidate)).getStatus());
	}

	private void fullWitness() {
		witnessCore("(1.6,0)", "(3.6,0)");
		add("F=(-3,0.5)");
		add("G=(1,1)");
		add("H=(5,0.5)");
		add("l1={F,G,H}");
		add("b=3");
		assertNotNull(add("d=SplineV2(l1,b)"));
		assertNotNull(add("e=Intersect(d,a)"));
		GeoLocusIntersectionResult g = add("g=Intersect(a,c)");
		assertNotNull(g);
		assertEquals(4, g.getIntersectionResult().getFiniteSolutions().size(),
				String.valueOf(solutionPoints(g).size()));
		for (int index = 0; index < MATERIALIZED.length; index++) {
			String token = g.getIntersectionResult().getFiniteSolutions().get(index)
					.getIdentity().getRootToken();
			assertNotNull(add(MATERIALIZED[index] + "=Intersect(g,\"" + token + "\")"));
		}
		add("M=Midpoint(D,C+(1,0))");
	}

	private void assertRedefinedWitness(Map<String, PersistentGeoId> identities,
			GeoIdentityRecord before) {
		for (Map.Entry<String, PersistentGeoId> identity : identities.entrySet()) {
			assertEquals(identity.getValue(), id(identity.getKey()), identity.getKey());
		}
		GeoIdentityRecord after = record("E");
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				after.getSchemaVersion());
		assertEquals(ids("C"), after.getDependencies());
		assertEquals(before.getDefinitionRevision() + 1, after.getDefinitionRevision());
		assertEquals(before.getTopologyRevision(), after.getTopologyRevision());

		// M is unaffected and identity-free; coinciding coordinates are no evidence.
		GeoPoint e = (GeoPoint) requireLookup("E");
		GeoPoint m = (GeoPoint) requireLookup("M");
		assertNull(registry().getPersistentGeoId(m));
		assertEquals(m.getInhomX(), e.getInhomX(), TOLERANCE);
		assertEquals(m.getInhomY(), e.getInhomY(), TOLERANCE);

		assertTrue(requireLookup("a").isDefined());
		GeoLocusIntersectionResult g = (GeoLocusIntersectionResult) requireLookup("g");
		assertSamePoints(List.of(REDEFINED_INTERSECTIONS), solutionPoints(g), TOLERANCE);
		GeoLocusIntersectionResult spline = (GeoLocusIntersectionResult) requireLookup("e");
		assertFalse(solutionPoints(spline).isEmpty());
		for (double[] point : solutionPoints(spline)) {
			assertEquals(1, Math.pow(point[0] - 1.3, 2) + point[1] * point[1] / 4,
					TOLERANCE, "a spline solution off the redefined ellipse");
		}
		for (String label : MATERIALIZED) {
			GeoPoint point = (GeoPoint) requireLookup(label);
			assertTrue(point.isDefined(), label);
			assertTrue(onRedefinedIntersection(point), label + " = " + point);
		}
	}

	private static boolean onRedefinedIntersection(GeoPoint point) {
		for (double[] expected : REDEFINED_INTERSECTIONS) {
			if (Math.abs(expected[0] - point.getInhomX()) < TOLERANCE
					&& Math.abs(expected[1] - point.getInhomY()) < TOLERANCE) {
				return true;
			}
		}
		return false;
	}
}
