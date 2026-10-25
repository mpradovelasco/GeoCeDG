/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.Cause;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.Status;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.junit.jupiter.api.Test;

/**
 * The constructive identity certificate {@code constructive-identity-certificate/v1}
 * ({@code locus-v2-constructive-authority.md} section 8) on synthetic constructions (gate
 * G-AUT-9, rows V-AUT-CIC-3 to V-AUT-CIC-6): theorems are certified and false conjectures are
 * not; the proof depends on the branch; a cut forgets and never invents; the inherited special
 * case of a point already incident is reproduced, not assumed; the membership of a limited part
 * is decided (the segment defect of the prototype); the results are typed, a cut without a
 * rigorous enclosure decides nothing, and an injected inversion of the live branch or branches
 * that cannot be told apart give no certificate; and the structural key refuses a stale proof
 * after a deletion and recreation with the same label (S4).
 */
final class ConstructiveIdentityCertifierTest extends ConstructiveModelReplicaTest {

	private static final String[] SYNTHETIC = {
			"SA=(0.37,-1.21)", "SB=(5.83,0.44)", "SP=(2.9,3.3)", "w=47°", "SC=Rotate(SP,w,SA)",
			"mAB=Midpoint(SA,SB)", "mBC=Midpoint(SB,SC)", "mCA=Midpoint(SC,SA)",
			"sAB=Line(SA,SB)", "sBC=Line(SB,SC)", "sCA=Line(SC,SA)",
			"pAB=OrthogonalLine(mAB,sAB)", "pBC=OrthogonalLine(mBC,sBC)",
			"pCA=OrthogonalLine(mCA,sCA)", "SO=Intersect(pAB,pBC)", "cc=Circle(SO,SA)",
			"gA=Line(SA,mBC)", "gB=Line(SB,mCA)", "gC=Line(SC,mAB)", "SG=Intersect(gA,gB)",
			"TA=(-2.3,0.7)", "TB=(3.1,1.9)", "TO=Midpoint(TA,TB)", "tc=Circle(TO,TA)",
			"TP=(0.4,4.6)", "tl=Line(TA,TP)", "TQ=Intersect(tc,tl,1)", "TR=Intersect(tc,tl,2)",
			"tp=OrthogonalLine(TB,tl)",
			"UA=(1.1,0.9)", "UB=(4.2,2.3)", "v=29°", "UR=Rotate(UB,v,UA)", "ul=Line(UA,UR)",
			"uf=OrthogonalLine(UB,ul)", "UF=Intersect(uf,ul)", "UM=Midpoint(UA,UB)",
			"uw=Circle(UM,UA)", "sl=Line(UA,UB)", "UX=Point(sl)", "UY=Point(ul)"};

	@Test
	void theoremsAreCertifiedAndFalseConjecturesAreNot() {
		synthetic();

		assertCertified("SO", "pCA", "S1: the circumcentre lies on the third bisector");
		assertCertified("SB", "cc", "S1: the second vertex lies on the circumcircle");
		ConstructiveIdentityCertificate rotated = certify("SC", "cc");
		assertEquals(Status.IDENTITY_CERTIFIED, rotated.getStatus(), rotated.toString());
		assertTrue(rotated.getCuts().contains("SC"), "the rotated vertex is cut");
		assertCertified("SG", "gC", "S1: the centroid lies on the third median");
		assertEquals(Status.NOT_AN_IDENTITY, certify("SG", "cc").getStatus(),
				"S1: the centroid is not on the circumcircle");
		assertCertified("UF", "uw", "S3: Thales across a non-constructible line");
		ConstructiveIdentityCertificate onPath = certify("UX", "sl");
		assertEquals(Status.IDENTITY_CERTIFIED, onPath.getStatus(), onPath.toString());
		assertEquals(1, onPath.getIndeterminateNames().stream().filter(
				name -> name.startsWith("UX")).count(), "one indeterminate on the line path");
		assertEquals(Status.NOT_AN_IDENTITY, certify("UX", "uw").getStatus(),
				"S3: the point of the line is not on the circle");
	}

	@Test
	void theProofDependsOnTheBranchAndTheSpecialCaseIsReproduced() {
		synthetic();

		ConstructiveIdentityCertificate first = certify("TQ", "tp");
		ConstructiveIdentityCertificate second = certify("TR", "tp");

		assertNotEquals(first.getStatus(), second.getStatus(), "V-AUT-CIC-3: S2 depends on the"
				+ " branch: " + first + " / " + second);
		ConstructiveIdentityCertificate thales = first.isIdentityCertified() ? first : second;
		ConstructiveIdentityCertificate other = first.isIdentityCertified() ? second : first;
		assertEquals(Status.NOT_AN_IDENTITY, other.getStatus());
		GeoElement point = lookup(other == first ? "TQ" : "TR");
		ConstructiveIdentityCertificate equal = certify(point.getLabelSimple(), "TA");
		assertEquals(Status.IDENTITY_CERTIFIED, equal.getStatus(),
				"V-AUT-CIC-5: the other intersection is TA itself, by the inherited rule");
		assertFalse(thales.getCorrespondences().isEmpty(),
				"the branch is checked against the live output");
	}

	@Test
	void aCutForgetsARelationAndNeverInventsOne() {
		synthetic();

		ConstructiveIdentityCertificate certificate = certify("UY", "ul");

		assertEquals(Status.NOT_AN_IDENTITY, certificate.getStatus(), "S3 limitation: UY is cut,"
				+ " so its own membership, a provenance relation, is not proved again");
		assertTrue(certificate.getCuts().contains("UY"));
	}

	@Test
	void theMembershipOfALimitedPartIsDecided() {
		add("P1=(0,0)");
		add("P2=(4,0)");
		add("Q1=(1,1)");
		add("Q2=(1,3)");
		add("s=Segment(P1,P2)");
		add("q=Segment(Q1,Q2)");
		add("f=Line(P1,P2)");
		add("h=Line(Q1,Q2)");
		add("X=Intersect(f,h)");
		// A point of f, so that the support of t is f for every value of the inputs.
		add("R1=Point(f)");
		place("R1", 6, 0);
		add("t=Segment(P2,R1)");
		add("u=Segment((0,1),(4,1))");

		ConstructiveIdentityCertificate inside = certify("X", "s");
		ConstructiveIdentityCertificate below = certify("X", "q");
		ConstructiveIdentityCertificate beyond = certify("X", "t");
		ConstructiveIdentityCertificate other = certify("X", "u");

		assertEquals(Status.IDENTITY_CERTIFIED, inside.getStatus(), inside.toString());
		for (ConstructiveIdentityCertificate outside : List.of(below, beyond)) {
			assertEquals(Status.CONTRADICTED, outside.getStatus(),
					"X lies on the support, outside the limited part: " + outside);
			assertEquals(Cause.NOT_ON_LIMITED_TARGET, outside.getCause());
		}
		assertEquals(Status.NOT_AN_IDENTITY, other.getStatus(), "another line: " + other);
		// The defect of the prototype: a meet of segments outside one of them is no point.
		add("Y=Intersect(s,q)");
		ConstructiveIdentityCertificate meet = certify("Y", "f");
		assertEquals(Status.INVALID, meet.getStatus(), "the ideal meet is not on q: " + meet);
		assertEquals(Cause.OBJECT_UNDEFINED, meet.getCause());
	}

	@Test
	void theKeyRefusesAStaleProofAfterARecreationWithTheSameLabel() {
		synthetic();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int point = builder.add(lookup("SC"));
		int target = builder.add(lookup("cc"));
		ConstructiveCone before = builder.build();
		ConstructiveIdentityCertificate proof = ConstructiveIdentityCertifier.incidence("SC on cc",
				before, point, target, -1, ConstructiveIdentityBudget.V1);
		assertTrue(proof.isIdentityCertified(), proof.toString());
		assertTrue(ConstructiveIdentityCertifier.recheck(proof, before, point, target, -1,
				ConstructiveIdentityBudget.V1), "the same revision");

		lookup("cc").remove();
		add("cc=Circle(SO,mAB)");
		ConstructiveCone.Builder again = ConstructiveCone.builder(getConstruction());
		int newPoint = again.add(lookup("SC"));
		int newTarget = again.add(lookup("cc"));
		ConstructiveCone after = again.build();

		assertFalse(ConstructiveIdentityCertifier.recheck(proof, after, newPoint, newTarget, -1,
				ConstructiveIdentityBudget.V1), "V-AUT-ID-3 (S4): the key of the new cone differs");
		assertEquals(Status.NOT_AN_IDENTITY, ConstructiveIdentityCertifier.incidence("SC on cc",
				after, newPoint, newTarget, -1, ConstructiveIdentityBudget.V1).getStatus());
	}

	@Test
	void aRenameKeepsTheKeyAndADragOnlyRechecks() {
		synthetic();
		ConstructiveIdentityCertificate proof = certify("SO", "pCA");
		assertTrue(proof.isIdentityCertified());
		String key = proof.getKey();

		lookup("SB").rename("SB2");
		ConstructiveIdentityCertificate renamed = certify("SO", "pCA");
		assertEquals(key, renamed.getKey(), "V-AUT-ID-1: a rename keeps the key");
		((GeoPoint) lookup("SB2")).setCoords(5.9, 0.5, 1);
		lookup("SB2").updateCascade();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int point = builder.add(lookup("SO"));
		int target = builder.add(lookup("pCA"));
		ConstructiveCone moved = builder.build();
		assertTrue(ConstructiveIdentityCertifier.recheck(proof, moved, point, target, -1,
				ConstructiveIdentityBudget.V1), "a drag keeps the key and the cell");
	}

	@Test
	void anAttemptIsDeterministicAndWithinItsBudget() {
		synthetic();
		ConstructiveIdentityCertificate first = certify("SC", "cc");
		ConstructiveIdentityCertificate second = certify("SC", "cc");

		assertEquals(first.getKey(), second.getKey());
		assertEquals(first.getCellSigns(), second.getCellSigns());
		assertEquals(first.getIndeterminateNames(), second.getIndeterminateNames());
		assertEquals(first.getCounters().toString(), second.getCounters().toString());
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int point = builder.add(lookup("SC"));
		int target = builder.add(lookup("cc"));
		ConstructiveIdentityCertificate starved = ConstructiveIdentityCertifier.incidence(
				"SC on cc", builder.build(), point, target, -1,
				ConstructiveIdentityBudget.restrictedTo(10, 16, 200_000, 4096));
		assertEquals(Status.NOT_ESTABLISHED, starved.getStatus());
		assertEquals(Cause.BUDGET_EXHAUSTED, starved.getCause(), "V-AUT-CIC-6: " + starved);
	}

	@Test
	void theResultsAreTypedAndNeverADecisionByDefault() {
		add("RA=(0,0)");
		add("r1=Circle(RA,2)");
		add("r2=Circle((5,1),3)");
		add("l1=Line((-3,0.5),(3,0.7))");
		add("l2=Line((2,-3),(7,4))");
		add("X1=Intersect(r1,l1,1)");
		add("X2=Intersect(r2,l2,1)");
		add("lx=Line(X2,(9,9))");
		add("F1=(-3,1)");
		add("F2=(3,1)");
		add("far=Line(F1,F2)");
		add("XF=Intersect(r1,far,1)");
		add("a=1");
		add("Pc=If(a>0,(2,4),(3,-1))");
		add("M=Midpoint(RA,(1,1))");
		add("gc=Line(Pc,M)");
		getKernel().updateConstruction(false);
		// The selection state settles while the line meets the circle; then it misses it.
		for (String label : new String[] {"F1", "F2"}) {
			GeoPoint end = (GeoPoint) lookup(label);
			end.setCoords(end.getInhomX(), 5, 1);
			end.updateCascade();
		}
		assertFalse(lookup("XF").isDefined());

		ConstructiveIdentityCertificate second = certify("X1", "lx");
		assertEquals(Status.NOT_ESTABLISHED, second.getStatus(), second.toString());
		assertEquals(Cause.OUT_OF_CLASS, second.getCause(),
				"V-AUT-CIC-6: a second independent square root");
		ConstructiveIdentityCertificate missing = certify("XF", "r1");
		assertEquals(Status.INVALID, missing.getStatus(), "the line misses the circle: " + missing);
		assertEquals(Cause.OBJECT_UNDEFINED, missing.getCause());
		ConstructiveIdentityCertificate conditioned = certify("M", "gc");
		assertEquals(Status.NOT_ESTABLISHED, conditioned.getStatus(), "P2: the conditioned point"
				+ " has no rigorous enclosure, so its box is the whole plane: " + conditioned);
		assertEquals(Cause.PRECONDITION_UNDECIDED, conditioned.getCause());
		assertTrue(conditioned.getCuts().contains("Pc"));
	}

	@Test
	void aLiveOutputOnTheOtherBranchOrBranchesThatCannotBeToldApartGiveNoCertificate() {
		add("A=(0,0)");
		add("c=Circle(A,(3,0))");
		add("f=Line((-5,1),(5,1))");
		GeoElementND[] outputs = getElements("Intersect(c,f)");
		double below = Math.nextDown(Math.nextDown(1.0));
		add("O=(0,0)");
		add("u=Circle(O,1)");
		add("g=Line((-2," + below + "),(2," + below + "))");
		GeoElementND[] merged = getElements("Intersect(u,g)");
		getKernel().updateConstruction(false);
		GeoPoint first = (GeoPoint) outputs[0].toGeoElement();
		GeoPoint second = (GeoPoint) outputs[1].toGeoElement();
		ConstructiveIdentityCertificate before = certify(first.getLabelSimple(), "c");
		assertEquals(Status.IDENTITY_CERTIFIED, before.getStatus(), before.toString());

		// An injected inversion: the live output is moved to the other branch.
		first.setCoords(second.getInhomX(), second.getInhomY(), 1);
		ConstructiveIdentityCertificate inverted = certify(first.getLabelSimple(), "c");
		ConstructiveIdentityCertificate near = certify(merged[0].toGeoElement().getLabelSimple(),
				"u");

		assertEquals(Cause.BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED, inverted.getCause(),
				"V-AUT-CIC-4: " + inverted);
		assertEquals(Status.NOT_ESTABLISHED, inverted.getStatus());
		assertEquals(Cause.BRANCH_CORRESPONDENCE_NOT_DEMONSTRATED, near.getCause(),
				"the inherited tolerance merges two ideal roots that the live output cannot tell"
						+ " apart: " + near);
	}

	private void synthetic() {
		for (String command : SYNTHETIC) {
			add(command);
		}
		getKernel().updateConstruction(false);
	}

	private ConstructiveIdentityCertificate certify(String point, String target) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int subject = builder.add(lookup(point));
		int object = builder.add(lookup(target));
		return ConstructiveIdentityCertifier.incidence(point + " on " + target, builder.build(),
				subject, object, -1, ConstructiveIdentityBudget.V1);
	}

	private void assertCertified(String point, String target, String message) {
		ConstructiveIdentityCertificate certificate = certify(point, target);
		assertEquals(Status.IDENTITY_CERTIFIED, certificate.getStatus(),
				message + ": " + certificate);
	}

	@SuppressWarnings("unused")
	private GeoElementND[] outputs(String command) {
		return getElements(command);
	}
}
