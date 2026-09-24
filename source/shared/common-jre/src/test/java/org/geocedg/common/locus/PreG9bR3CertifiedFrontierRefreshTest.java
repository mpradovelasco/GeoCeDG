/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geocedg.common.kernel.geos.GeoLocusMetricResult;
import org.geocedg.common.kernel.spatial.identity.ConstructionGeoRedefineProvider;
import org.geocedg.common.kernel.spatial.identity.DurableDependencyProjection;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-R3 decision 3: an ordinary edit of an identity-free geo that moves the
 * version-2 durable frontier of a participant is refreshed only through the
 * certified transition, and fails closed as a whole otherwise.
 */
class PreG9bR3CertifiedFrontierRefreshTest extends PreG9bR3RedefineTestBase {
	private static final double TOLERANCE = 1E-9;

	@Test
	void aFrontierNeutralEditOnlyRecomputes() throws Exception {
		witnessCore("(0,0)", "(2,0)");
		GeoIdentityRecord before = record("E");
		final String beforeXml = getApp().getXML();
		GeoPoint through = (GeoPoint) requireLookup("B");
		through.setCoords(2.5, 0, 1);
		through.updateRepaint();
		assertEquals(before, record("E"));

		Outcome ordinary = redefine("f", "Line(C,(1,3))", assessment -> null);
		assertTrue(ordinary.succeeded(), ordinary.errors());
		assertNull(ordinary.assessment());
		GeoIdentityRecord after = record("E");
		assertEquals(before.getId(), after.getId());
		assertEquals(before.getDependencies(), after.getDependencies());
		assertEquals(before.getDefinitionRevision(), after.getDefinitionRevision());
		assertEquals(before.getTopologyRevision(), after.getTopologyRevision());
		assertEquals(spatialGeoRecords(beforeXml), spatialGeoRecords(getApp().getXML()));
		GeoPoint recomputed = (GeoPoint) requireLookup("E");
		GeoPoint d = (GeoPoint) requireLookup("D");
		GeoPoint c = (GeoPoint) requireLookup("C");
		assertEquals((d.getInhomX() + c.getInhomX()) / 2, recomputed.getInhomX(),
				TOLERANCE);
		assertEquals((d.getInhomY() + c.getInhomY()) / 2, recomputed.getInhomY(),
				TOLERANCE);
		assertTrue(requireLookup("a").isDefined());
	}

	@Test
	void anInPlaceSoftRedefineOfAFrontierHelperIsFrontierNeutral() throws Exception {
		getKernel().setContinuous(false);
		add("A=(0,0)");
		add("B=(1,0)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("k=Circle(C,2)");
		add("D=Intersect(k,yAxis,1)");
		add("E=Midpoint(D,C)");
		assertNotNull(add("a=LocusV2(E,C)"));
		GeoIdentityRecord before = record("E");
		assertEquals(ids("C"), before.getDependencies());
		GeoElement helper = requireLookup("k");

		Outcome soft = redefine("k", "Circle(C,3)", assessment -> null);
		assertTrue(soft.succeeded(), soft.errors());
		// The host updated the helper in place: the DAG, hence the frontier, is
		// unchanged and no certification or revision is needed.
		assertTrue(helper == requireLookup("k"));
		assertEquals(before, record("E"));
		assertEquals(DurableDependencyProjection.TRANSITIVE_DURABLE_FRONTIER.project(
				requireLookup("E"), registry()::getPersistentGeoId),
				record("E").getDependencies());
		String xml = getApp().getXML();
		assertEquals(spatialGeoRecords(xml), spatialGeoRecords(openInFreshApp(xml)
				.getXML()));
	}

	@Test
	void aStructuralUpstreamEditRefreshesTheFrontierUnderTheSameIdentity()
			throws Exception {
		activateUndo();
		witnessCore("(0,0)", "(2,0)");
		add("P=Point(c)");
		GeoLocusMetricResult length = add("m=LocusLength(a)");
		assertNotNull(length);
		getApp().storeUndoInfo();
		GeoIdentityRecord before = record("E");
		final GeoIdentityRecord locusBefore = record("a");
		final PersistentGeoId metric = id("m");
		final String beforeXml = getApp().getXML();
		final double lengthBefore = length();
		final double[] pointBefore = coordinates("E");

		Outcome ordinary = redefine("f", "Line(P,xAxis)", assessment -> null);

		assertTrue(ordinary.succeeded(), ordinary.errors());
		assertNull(ordinary.assessment());
		assertNull(registry().getPersistentGeoId(requireLookup("P")));
		assertNull(registry().getPersistentGeoId(requireLookup("f")));
		GeoIdentityRecord after = record("E");
		assertEquals(before.getId(), after.getId());
		assertEquals(ConstructionGeoRedefineProvider.SCHEMA_VERSION_TRANSITIVE_FRONTIER,
				after.getSchemaVersion());
		assertEquals(ids("C", "c"), after.getDependencies());
		assertEquals(before.getDefinitionRevision() + 1, after.getDefinitionRevision());
		assertEquals(before.getTopologyRevision() + 1, after.getTopologyRevision());
		assertEquals(locusBefore.getDependencies(), record("a").getDependencies());
		assertEquals(metric, id("m"));
		assertTrue(requireLookup("a").isDefined());
		// E now traces the unit circle about (0, P.y/2): the metric recomputed.
		assertEquals(2 * Math.PI, length(), 1E-6);
		final double[] pointAfter = coordinates("E");
		String afterXml = getApp().getXML();

		// The live dependents equal a from-scratch evaluation of the edited document.
		AppCommon reopened = openInFreshApp(afterXml);
		assertEquals(spatialGeoRecords(afterXml), spatialGeoRecords(reopened.getXML()));
		double reopenedLength = ((GeoLocusMetricResult) reopened.getKernel()
				.lookupLabel("m")).getMetricResult().getMetricValue().getFiniteValue()
				.orElseThrow();
		assertEquals(reopenedLength, ((GeoLocusMetricResult) requireLookup("m"))
				.getMetricResult().getMetricValue().getFiniteValue().orElseThrow(),
				1E-7);

		getKernel().undo();
		assertEquals(spatialGeoRecords(beforeXml), spatialGeoRecords(getApp().getXML()));
		assertEquals(ids("C"), record("E").getDependencies());
		assertEquals(lengthBefore, length(), 1E-9);
		assertArrayEquals(pointBefore, coordinates("E"), 1E-12);
		getKernel().redo();
		assertEquals(spatialGeoRecords(afterXml), spatialGeoRecords(getApp().getXML()));
		assertEquals(ids("C", "c"), record("E").getDependencies());
		assertEquals(2 * Math.PI, length(), 1E-6);
		assertArrayEquals(pointAfter, coordinates("E"), 1E-12);
	}

	private double length() {
		return ((GeoLocusMetricResult) requireLookup("m")).getMetricResult()
				.getMetricValue().getFiniteValue().orElseThrow();
	}

	private double[] coordinates(String label) {
		GeoPoint point = (GeoPoint) requireLookup(label);
		return new double[] {point.getInhomX(), point.getInhomY()};
	}

	@Test
	void aRoleIncompleteParticipantIsNeverAutoRefreshed() {
		getKernel().setContinuous(false);
		add("A=(1.6,0)");
		add("B=(3.6,0)");
		add("c=Circle(A,B)");
		add("C=Point(c)");
		add("Q=Point(c)");
		add("f=Line(C,xAxis)");
		GeoElement first = add("Intersect(c,f)");
		assertTrue(first.getParentAlgorithm().getOutputLength() > 1);
		assertTrue(first.rename("P"));
		assertNotNull(add("a=LocusV2(P,C)"));
		GeoIdentityRecord before = record("P");
		assertEquals(ids("C", "c"), before.getDependencies());
		String beforeXml = getApp().getXML();

		// The step of P is unchanged, but P shares its parent algorithm with an
		// output that has no stable role: the refresh is not certified.
		Outcome ordinary = redefine("f", "Line(Q,xAxis)", assessment -> null);

		assertFalse(ordinary.succeeded());
		assertEquals(beforeXml, getApp().getXML());
		assertEquals(before, record("P"));
	}

	@Test
	void aTransitionOutsideTheCertifiedStepFailsClosedAsAWhole() {
		getKernel().setContinuous(false);
		add("c2=Circle((0,0),3)");
		add("K=Point(c2)");
		add("h=Line(K,xAxis)");
		add("n=7");
		add("Z=Point(h)");
		assertNotNull(add("lz=LocusV2(Z,K)"));
		GeoIdentityRecord before = record("Z");
		assertEquals(ids("K"), before.getDependencies());
		String beforeXml = getApp().getXML();

		// The path of Z changes family, so the participant's own step changes while
		// its frontier grows to {K, c2}: this is no certified refresh. The rollback
		// also undoes the host's legacy reordering of h after n.
		Outcome incompatible = redefine("h", "Circle(K,Radius(c2))", assessment -> null);

		assertFalse(incompatible.succeeded());
		assertNull(incompatible.assessment());
		assertEquals(beforeXml, getApp().getXML());
		assertEquals(before.getDependencies(), record("Z").getDependencies());
		assertEquals(before.getDefinitionRevision(), record("Z").getDefinitionRevision());

		// The same frontier growth through an unchanged step is certified.
		Outcome compatible = redefine("h", "Line(K,Center(c2))", assessment -> null);
		assertTrue(compatible.succeeded(), compatible.errors());
		GeoIdentityRecord refreshed = record("Z");
		assertEquals(before.getId(), refreshed.getId());
		assertEquals(ids("K", "c2"), refreshed.getDependencies());
		assertEquals(before.getDefinitionRevision() + 1,
				refreshed.getDefinitionRevision());
	}
}
