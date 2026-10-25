/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusDriverCorrespondencePoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.ContactOrder;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.Scope;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.Status;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolver.Cause;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * Increment E2 in {@code Intersect(L, r, D)} on the replica of the author's model (gates G-AUT-2
 * and G-AUT-10; validation matrix rows V-AUT-CIC-1, V-AUT-RLX-2, V-AUT-ID-1 to V-AUT-ID-7,
 * V-AUT-DYN-3, V-AUT-DYN-4 and V-AUT-TRF-2): the five mandatory contacts are {@code STRICT} by
 * their constructive identity certificate, with the fold of {@code CU2} transferred by structure
 * at the stationary contacts; {@code NOT_AN_IDENTITY} withdraws the relaxed evidence; the
 * certificate follows the lifecycle of the structural key; a drag rechecks it without new
 * symbolic work; and two resolutions of one revision are identical. No identity, token or
 * selector changes: the integrity is revision evidence.
 */
final class ConstructiveIdentityIntegrationTest extends ConstructiveModelReplicaTest {

	private static final String[] CONTACTS = {"Q1=Intersect(e_1,n,K)", "Q2=Intersect(e_1,c_1,K)",
			"Q3=Intersect(o_1,h_2,K)", "Q4=Intersect(e_1,c_1,J)", "Q5=Intersect(e_1,r,J)"};
	private static final ContactOrder[] ORDERS = {ContactOrder.STATIONARY,
			ContactOrder.STATIONARY, ContactOrder.TANGENT, ContactOrder.STATIONARY,
			ContactOrder.STATIONARY};

	@Test
	void theFiveMandatoryContactsAreStrictByTheirCertificate() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());

		for (int index = 0; index < CONTACTS.length; index++) {
			GeoElement output = add(CONTACTS[index]);
			final String identity = identity(output);
			StructuralPointResolution resolved = resolution(output);
			String row = "G-AUT-2 " + CONTACTS[index] + ": " + resolved;
			assertTrue(resolved.isDefined(), row);
			assertEquals(RelationIntegrity.STRICT, resolved.getMembership().getIntegrity(), row);
			assertEquals(RelationIntegrity.STRICT, resolved.getIncidence().getIntegrity(), row);
			assertEquals(RelationIntegrityRecord.Evidence.CONSTRUCTIVE_IDENTITY_CERTIFICATE,
					resolved.getIncidence().getEvidence(), row);
			assertEquals(RelationIntegrity.STRICT, resolved.getIntegrity(), row);
			ConstructiveIdentityCertificate certificate = resolved.getCertificate();
			assertNotNull(certificate, row);
			assertEquals(Scope.AUTOMATIC, certificate.getScope(), "the only scope of the product");
			assertEquals(ORDERS[index], certificate.getContactOrder(), row);
			if (ORDERS[index] == ContactOrder.STATIONARY) {
				Cu2CertificateTransfer.Record fold = resolved.getFold();
				assertNotNull(fold, row);
				assertTrue(fold.isTransferred(), "V-AUT-TRF-2: the fold of CU2 at the stationary"
						+ " contact, by structure with premises certified for every value of the"
						+ " cut: " + fold);
				assertEquals(Cu2CertificateTransfer.Route.STRUCTURE, fold.getRoute());
			} else {
				assertNull(resolved.getFold(), "a regular point of the trace has no fold");
			}
			assertEquals(identity, identity(output), "the integrity never changes an identity");
		}
	}

	@Test
	void notAnIdentityWithdrawsTheRelaxedEvidence() {
		add("O=(0,0)");
		add("c=Circle(O,3)");
		add("P=Point(c)");
		add("Q=Midpoint(O,P)");
		add("L=LocusV2(Q,P)");
		add("D=Intersect(c,yAxis,2)");
		// D' equals D only for the binary64 inputs; the line through a cut and the midpoint of
		// O and D' passes through Q(D) for every value of the cut.
		add("Dp=(0,3)");
		add("V=Midpoint(O,Dp)");
		add("w=0.3");
		add("S=Rotate((2,-1),w,O)");
		add("t=Line(S,V)");
		AuthorityPolicyAdoption.adopt(getConstruction());
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int target = builder.add(lookup("t"));
		int point = builder.addWithDriverSubstitution(lookup("Q"), lookup("P"), lookup("D"));
		SubstituteGenericEvidence.Result relaxed = SubstituteGenericEvidence.incidence("Q on t",
				builder.build(), point, target, ExactEvaluationBudget.V1);
		assertEquals(RelationIntegrity.RELAXED, relaxed.getRecord().getIntegrity(),
				"SUBSTITUTE_GENERIC admits it: it holds for every perturbation of S");

		StructuralPointResolution resolved = resolution(add("X=Intersect(L,t,D)"));

		assertFalse(resolved.isDefined(), "V-AUT-RLX-2 in E2: " + resolved);
		assertEquals(Cause.INCIDENCE_NOT_ESTABLISHED, resolved.getCause());
		assertEquals(RelationIntegrity.NOT_ESTABLISHED, resolved.getIncidence().getIntegrity(),
				"the evidence is withdrawn, never left RELAXED");
		assertEquals(Status.NOT_AN_IDENTITY, resolved.getCertificate().getStatus(),
				"D and D' are other free inputs: an accidental agreement, never a contradiction");
		assertTrue(resolution(add("Y=Point(L,D)")).isDefined(), "Point(L, D) stays defined");
	}

	@Test
	void aRenameKeepsTheKeyAndARedefinitionChangesIt() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		final GeoElement output = add("Q=Intersect(e_1,n,K)");
		ConstructiveIdentityCertificate before = fresh("n");
		assertTrue(before.isIdentityCertified(), before.toString());

		geo("n").rename("n9");
		geo("k").rename("k9");
		geo("E_1").rename("E_9");
		ConstructiveIdentityCertificate renamed = fresh("n9");
		assertEquals(before.getKey(), renamed.getKey(), "V-AUT-ID-1: the target, an intermediate"
				+ " and the driver renamed");
		assertEquals(before.getCellSigns(), renamed.getCellSigns());
		geo("n9").rename("n");
		geo("k9").rename("k");
		geo("E_9").rename("E_1");

		// k is the parallel to g through H; the perpendicular to h through H is the same line.
		add("k=OrthogonalLine(H,h)");
		GeoElement redefined = lookup("Q");
		ConstructiveIdentityCertificate after = fresh("n");
		assertNotEquals(before.getKey(), after.getKey(), "V-AUT-ID-2: a redefinition changes the"
				+ " structural definition and the key");
		assertTrue(after.isIdentityCertified(), "a new proof of the same theorem: " + after);
		assertEquals(RelationIntegrity.STRICT, resolution(redefined).getIntegrity());
		assertNotNull(output);
	}

	@Test
	void aDeletionAndARecreationWithTheSameLabelGiveAnotherKey() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("Q=Intersect(e_1,n,K)");
		ConstructiveIdentityCertificate before = fresh("n");
		String identity = identity(geo("n"));

		geo("n").remove();
		assertNull(lookup("Q"), "deleting the target deletes the subject of the relation");
		add("n=Segment(H,M)");
		GeoElement again = add("Q=Intersect(e_1,n,K)");
		ConstructiveIdentityCertificate after = fresh("n");

		assertNotEquals(identity, identity(geo("n")), "another durable identity");
		assertNotEquals(before.getKey(), after.getKey(), "V-AUT-ID-3: no reassociation");
		assertTrue(after.isIdentityCertified(), after.toString());
		assertEquals(RelationIntegrity.STRICT, resolution(again).getIntegrity());
	}

	@Test
	void undoAndRedoEvaluateTheCertificateAgain() {
		activateUndo();
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("Q=Intersect(e_1,n,K)");
		getApp().storeUndoInfo();
		ConstructiveIdentityCertificate before = resolution(lookup("Q")).getCertificate();
		add("k=OrthogonalLine(H,h)");
		getApp().storeUndoInfo();
		ConstructiveIdentityCertificate after = resolution(lookup("Q")).getCertificate();
		assertNotEquals(before.getKey(), after.getKey());

		getApp().getUndoManager().undo();
		ConstructiveIdentityCertificate undone = resolution(lookup("Q")).getCertificate();
		assertNotSame(before, undone, "no stale certificate survives");
		assertSameProof(before, undone, "V-AUT-ID-4");
		getApp().getUndoManager().redo();
		assertSameProof(after, resolution(lookup("Q")).getCertificate(), "V-AUT-ID-5");
	}

	@Test
	void aReopenedDocumentRecomputesTheSameCertificate() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("Q=Intersect(e_1,n,K)");
		ConstructiveIdentityCertificate before = resolution(lookup("Q")).getCertificate();
		String xml = getApp().getXML();

		assertFalse(xml.contains("IDENTITY_CERTIFIED") || xml.contains("cic-structural-key")
				|| xml.contains("NOT_AN_IDENTITY"), "V-AUT-ID-6: no certificate is persisted");
		assertEquals(xml.indexOf(ConstructiveIdentityCertificate.VERSION),
				xml.lastIndexOf(ConstructiveIdentityCertificate.VERSION),
				"the version is named once, by the policy record of the document");
		reload();

		assertSameProof(before, resolution(lookup("Q")).getCertificate(), "V-AUT-ID-6");
	}

	@Test
	void aDuplicateTargetHasANewKeyAndANewCertificate() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("Q=Intersect(e_1,n,K)");
		add("n2=Segment(H,M)");
		GeoElement copy = add("Q2=Intersect(e_1,n2,K)");

		ConstructiveIdentityCertificate original = resolution(lookup("Q")).getCertificate();
		ConstructiveIdentityCertificate duplicate = resolution(copy).getCertificate();

		assertNotEquals(original.getKey(), duplicate.getKey(), "V-AUT-ID-7: a new identity");
		assertNotSame(original, duplicate, "never reused");
		assertTrue(duplicate.isIdentityCertified(), duplicate.toString());
	}

	@Test
	void aDragRechecksWithoutSymbolicWorkAndTwoResolutionsAreIdentical() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoElement output = add("Q=Intersect(e_1,n,K)");
		StructuralPointResolution first = resolution(output);

		place("E_1", -3.0, 4.0);
		assertSame(first, resolution(output), "V-AUT-DYN-3: the revision is unchanged");
		GeoPoint h = (GeoPoint) geo("H");
		place("H", h.getInhomX() + 0.01, h.getInhomY() + 0.01 * 8.122 / 7.573);
		StructuralPointResolution moved = resolution(output);
		assertNotSame(first, moved, "a new revision");
		assertSame(first.getCertificate(), moved.getCertificate(), "V-AUT-DYN-1: the same key and"
				+ " cell, so the proof is rechecked and kept, without a new attempt");
		assertEquals(RelationIntegrity.STRICT, moved.getIntegrity());

		GeoLocusV2 locus = (GeoLocusV2) geo("e_1");
		List<String> runs = new ArrayList<>();
		for (int run = 0; run < 2; run++) {
			StructuralPointResolution resolved = StructuralPointResolver.resolve(locus,
					(GeoPoint) geo("K"), geo("n"), ExactEvaluationBudget.V1);
			ConstructiveIdentityCertificate certificate = resolved.getCertificate();
			runs.add(resolved + " " + resolved.getIncidence() + " " + certificate + " "
					+ certificate.getKey() + " " + certificate.getCellSigns() + " "
					+ certificate.getCounters());
		}
		assertEquals(runs.get(0), runs.get(1), "V-AUT-DYN-4: integrities, causes, premises and"
				+ " counters");
	}

	// ---------------------------------------------------------------- helpers

	/** A new attempt of {@code e_1} on a target at {@code K}, as the resolver makes it. */
	private ConstructiveIdentityCertificate fresh(String target) {
		return StructuralPointResolver.resolve((GeoLocusV2) geo("e_1"), (GeoPoint) geo("K"),
				geo(target), ExactEvaluationBudget.V1).getCertificate();
	}

	private static void assertSameProof(ConstructiveIdentityCertificate expected,
			ConstructiveIdentityCertificate actual, String row) {
		assertNotNull(actual, row);
		assertEquals(expected.getStatus(), actual.getStatus(), row);
		assertEquals(expected.getKey(), actual.getKey(), row);
		assertEquals(expected.getCellSigns(), actual.getCellSigns(), row);
		assertEquals(expected.getIndeterminateNames(), actual.getIndeterminateNames(), row);
		assertEquals(expected.getContactOrder(), actual.getContactOrder(), row);
		assertEquals(String.valueOf(expected.getCounters()), String.valueOf(actual.getCounters()),
				row);
	}

	private String identity(GeoElement geo) {
		return getConstruction().getSpatialIdentityRegistry().getPersistentGeoId(geo)
				.toExternalForm();
	}

	private static StructuralPointResolution resolution(GeoElement point) {
		assertNotNull(point);
		return ((AlgoLocusDriverCorrespondencePoint2D) point.getParentAlgorithm())
				.getResolution();
	}
}
