/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusDriverCorrespondencePoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolver.Cause;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * {@code Point(L, D)} and {@code Intersect(L, r, D)} under the constructive authority on the
 * replica of the author's model (Stage A design sections 2 to 8, with the revisions of
 * D-AUT-11; validation matrix rows V-A-*, V-MAND-*, V-DYN-* and V-AUT-CIC-1): the five
 * mandatory contacts, with {@code P in L} {@code STRICT} by transfer and, since increment E2,
 * {@code P in r} {@code STRICT} by its constructive identity certificate with the point
 * {@code H} cut, where E1 admitted it {@code RELAXED} by {@code SUBSTITUTE_GENERIC}; the
 * fail-closed facts; the explicit policy; persistence, Undo and Redo; and a drag of the driver
 * that repeats nothing.
 */
final class StructuralPointTest extends ConstructiveModelReplicaTest {

	private static final double CLOSE = 1E-9;

	@Test
	void aLegacyDocumentRequiresTheConstructiveAuthority() {
		buildAuthorReplica();
		String before = getApp().getXML();

		IllegalStateException refused = assertThrows(IllegalStateException.class,
				() -> LocusV2PublicOperations.createDriverCorrespondencePoint(getConstruction(),
						"P", (GeoLocusV2) geo("e_1"), null, (GeoPoint) geo("K")));

		assertTrue(refused.getMessage().startsWith(AuthorityPolicyAdoption.REQUIRED_CAUSE),
				"V-AUT-ADOPT-1: " + refused.getMessage());
		assertFalse(AuthorityPolicyAdoption.isConstructive(getConstruction()));
		assertEquals(before, getApp().getXML(), "nothing is created and the policy is kept");
	}

	@Test
	void theFiveMandatoryContactsUnderTheConstructiveAuthority() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoPoint m1 = (GeoPoint) geo("M_1");

		StructuralPointResolution point = resolution(add("P=Point(e_1,K)"));
		assertTrue(point.isDefined(), String.valueOf(point));
		assertEquals(RelationIntegrity.STRICT, point.getIntegrity(), "P in L by transfer");
		assertEquals(RelationIntegrityRecord.Evidence.TRANSFERRED_BY_MARGIN,
				point.getMembership().getEvidence());
		assertClose(m1, point, "J_1(K) = M_1");

		String[][] contacts = {{"Q1=Intersect(e_1,n,K)", "M_1"},
				{"Q2=Intersect(e_1,c_1,K)", "M_1"}, {"Q3=Intersect(o_1,h_2,K)", null},
				{"Q4=Intersect(e_1,c_1,J)", null}, {"Q5=Intersect(e_1,r,J)", null}};
		for (String[] contact : contacts) {
			StructuralPointResolution resolved = resolution(add(contact[0]));
			assertTrue(resolved.isDefined(), contact[0] + ": " + resolved);
			assertEquals(RelationIntegrity.STRICT, resolved.getMembership().getIntegrity());
			assertEquals(RelationIntegrity.STRICT, resolved.getIncidence().getIntegrity(),
					contact[0] + ": P in r by its constructive identity certificate");
			assertEquals(RelationIntegrityRecord.Evidence.CONSTRUCTIVE_IDENTITY_CERTIFICATE,
					resolved.getIncidence().getEvidence());
			assertEquals(RelationIntegrity.STRICT, resolved.getIntegrity());
			assertEquals(List.of("H"), resolved.getCertificate().getCuts(),
					"H, on the line through the rotation Cr, is cut");
			if (contact[1] != null) {
				assertClose((GeoPoint) geo(contact[1]), resolved, contact[0]);
			}
		}
		StructuralPointResolution fold = resolution(lookup("Q3"));
		assertEquals(((GeoPoint) geo("M_1")).getInhomX(), fold.getX(), CLOSE,
				"K_1(K) lies on h_2 through M_1");
		StructuralPointResolution seam = resolution(lookup("Q4"));
		StructuralPointResolution seamOnR = resolution(lookup("Q5"));
		assertEquals(seam.getX(), seamOnR.getX(), CLOSE, "the same point of the seam");
		assertEquals(seam.getY(), seamOnR.getY(), CLOSE);
	}

	@Test
	void aPointThatIsNotOnTheSupportOfTheDriverFailsClosed() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("F=(4,-5)");
		add("p2=Circle(B,rbCone)");
		add("G=Point(p2)");

		assertEquals(Cause.PROVENANCE_NOT_ESTABLISHED, resolution(add("P=Point(e_1,F)"))
				.getCause(), "V-A-1: a free point has no provenance on the support");
		assertEquals(Cause.DRIVER_SUPPORT_MISMATCH, resolution(add("P2=Point(e_1,G)"))
				.getCause(), "V-A-2: an equal circle is another object");
		assertEquals(Cause.PROVENANCE_NOT_ESTABLISHED, resolution(add("P3=Point(e_1,E_1)"))
				.getCause(), "the driver itself moves with the driver");
	}

	@Test
	void anIncidenceThatIsNotEstablishedLeavesOnlyTheIntersectionUndefined() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());

		final StructuralPointResolution intersection = resolution(add("Q=Intersect(e_1,h,K)"));
		StructuralPointResolution point = resolution(add("P=Point(e_1,K)"));

		assertFalse(intersection.isDefined());
		assertEquals(Cause.INCIDENCE_NOT_ESTABLISHED, intersection.getCause(),
				"a refutation for the substitute is not a contradiction: " + intersection);
		assertTrue(point.isDefined(), "Point(L, D) stays defined");
	}

	@Test
	void thePointIsAnOrdinaryCommandThatReopensWithTheSameEvidence() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("P=Point(e_1,K)");
		add("Q=Intersect(e_1,n,K)");
		final StructuralPointResolution point = resolution(lookup("P"));
		final StructuralPointResolution intersection = resolution(lookup("Q"));
		String xml = getApp().getXML();

		assertTrue(xml.contains("<command name=\"Point\">"), "an ordinary command");
		assertFalse(xml.contains("TRANSFERRED_BY_MARGIN") || xml.contains("SUBSTITUTE_GENERIC"),
				"no evidence is persisted");
		reload();

		StructuralPointResolution reopenedPoint = resolution(lookup("P"));
		StructuralPointResolution reopened = resolution(lookup("Q"));
		assertTrue(AuthorityPolicyAdoption.isConstructive(getConstruction()));
		assertTrue(reopenedPoint.isDefined(), String.valueOf(reopenedPoint));
		assertEquals(point.getX(), reopenedPoint.getX(), 0);
		assertEquals(point.getY(), reopenedPoint.getY(), 0);
		assertEquals(intersection.getIntegrity(), reopened.getIntegrity());
		assertEquals(String.valueOf(intersection.getIncidence()),
				String.valueOf(reopened.getIncidence()));
	}

	@Test
	void undoAndRedoRestoreTheStructuralPoint() {
		activateUndo();
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("P=Point(e_1,K)");
		getApp().storeUndoInfo();
		double x = resolution(lookup("P")).getX();

		getApp().getUndoManager().undo();
		assertEquals(null, lookup("P"));
		getApp().getUndoManager().redo();

		StructuralPointResolution redone = resolution(lookup("P"));
		assertTrue(redone.isDefined());
		assertEquals(x, redone.getX(), 0);
		getApp().getUndoManager().undo();
		getApp().getUndoManager().undo();
		assertFalse(AuthorityPolicyAdoption.isConstructive(getConstruction()),
				"undoing the adoption restores the legacy policy");
	}

	@Test
	void aDragOfTheDriverRepeatsNothingAndAnEditRecomputes() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoElement output = add("P=Point(e_1,K)");
		StructuralPointResolution first = resolution(output);

		place("E_1", -3.0, 4.0);
		assertSame(first, resolution(output), "V-DYN-1: the revision is unchanged");
		place("H", 6.6, 13.5);

		StructuralPointResolution moved = resolution(output);
		assertTrue(moved.isDefined(), String.valueOf(moved));
		assertFalse(first == moved, "a new revision recomputes the evidence");
		assertEquals(((GeoPoint) geo("M_1")).getInhomX(), moved.getX(), CLOSE,
				"V-DYN-2: the theorem follows the construction");
	}

	private static StructuralPointResolution resolution(GeoElement point) {
		assertNotNull(point);
		return ((AlgoLocusDriverCorrespondencePoint2D) point.getParentAlgorithm())
				.getResolution();
	}

	private static void assertClose(GeoPoint expected, StructuralPointResolution actual,
			String message) {
		assertEquals(expected.getInhomX(), actual.getX(), CLOSE, message);
		assertEquals(expected.getInhomY(), actual.getY(), CLOSE, message);
	}
}
