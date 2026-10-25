/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.ContactOrder;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.Scope;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate.Status;
import org.geocedg.common.kernel.locus.exact.ExactRational2D;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.junit.jupiter.api.Test;

/**
 * The constructive identity certificate on the five mandatory contacts of the replica of the
 * author's model (gates G-AUT-2 and G-AUT-10; validation matrix rows V-AUT-CIC-1, V-AUT-CIC-2
 * and V-AUT-DYN-1, proposal section 5.5): each contact is an identity in its cell in the
 * automatic scope and in the boundary scope, with {@code H} cut and the order of contact and the
 * sign of {@code h''} of the authority study; the controls C1 to C5 and a target one ulp away
 * are not certified; the decision box contains the ideal values; and at every position of the
 * study a proof is rechecked in its cell, made inapplicable by a change of its constants or of
 * its cell, and replaced by a new proof where one exists.
 */
final class ConstructiveIdentityContactTest extends ConstructiveModelReplicaTest {

	/** Locus, target, driver point, order of contact and sign of h'' of study section 1.7. */
	private static final Object[][] CONTACTS = {
			{"e_1", "n", "K", ContactOrder.STATIONARY, -1},
			{"e_1", "c_1", "K", ContactOrder.STATIONARY, -1},
			{"o_1", "h_2", "K", ContactOrder.TANGENT, 1},
			{"e_1", "c_1", "J", ContactOrder.STATIONARY, -1},
			{"e_1", "r", "J", ContactOrder.STATIONARY, 1}};
	private static final List<String> AUTOMATIC_INDETERMINATES = List.of("A.x", "A.y", "B.y",
			"rbCone", "H.x", "H.y", "D_1.x", "D_1.y");
	private static final List<String> BOUNDARY_INDETERMINATES = List.of("B.y", "H.x", "H.y");

	@Test
	void theFiveContactsAreIdentitiesInTheirCellInBothScopes() {
		buildAuthorReplica();

		for (Object[] contact : CONTACTS) {
			for (Scope scope : Scope.values()) {
				ConstructiveIdentityCertificate certificate = contact(contact, scope);
				String row = "V-AUT-CIC-1 " + scope + ": " + certificate;
				assertEquals(Status.IDENTITY_CERTIFIED, certificate.getStatus(), row);
				assertEquals(scope, certificate.getScope());
				assertEquals(List.of("H"), certificate.getCuts(), "H is cut: its path reads Cr");
				assertTrue((scope == Scope.AUTOMATIC ? AUTOMATIC_INDETERMINATES
						: BOUNDARY_INDETERMINATES).containsAll(
								certificate.getIndeterminateNames()), row);
				assertTrue(certificate.getIndeterminateNames().containsAll(
						List.of("B.y", "H.x", "H.y")), row);
				assertEquals(contact[3], certificate.getContactOrder(), row);
				assertEquals(0, certificate.getResidualDerivativeSign(1),
						"h' is the zero element: " + row);
				assertEquals(contact[4], certificate.getResidualDerivativeSign(2),
						"the sign of h'' of the study: " + row);
				assertFalse(certificate.getCorrespondences().isEmpty(),
						"the branches outside the slice correspond to the live algorithm");
				assertTrue(certificate.getCounters().getTermProducts()
						<= ConstructiveIdentityBudget.MAXIMUM_TERM_PRODUCTS);
			}
		}
	}

	@Test
	void theDecisionBoxContainsTheIdealValueOfEveryIndeterminate() {
		buildAuthorReplica();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int target = builder.add(geo("c_1"));
		int support = builder.add(geo("p"));
		int point = builder.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("K"));
		ConstructiveCone cone = builder.build();
		ConstructiveIdentityCertificate certificate = ConstructiveIdentityCertifier.incidence(
				"e_1 on c_1 at K", cone, point, target, support, ConstructiveIdentityBudget.V1);
		ConstructiveConeEvaluator<OutwardInterval> ideal =
				ConstructiveConeEvaluator.intervals(cone);

		assertEquals(Status.IDENTITY_CERTIFIED, certificate.getStatus(), certificate.toString());
		assertEquals(AUTOMATIC_INDETERMINATES, sorted(certificate.getIndeterminateNames()));
		for (ConstructiveIdentityCertificate.Indeterminate indeterminate
				: certificate.getIndeterminates()) {
			OutwardInterval box = indeterminate.getInterval();
			ConstructiveConeNode node = cone.getNode(indeterminate.getNode());
			OutwardInterval[] enclosure = ConstructiveIdentityCertifier.idealEnclosure(ideal,
					node.getIndex());
			if (enclosure != null) {
				OutwardInterval coordinate = enclosure[indeterminate.getCoordinate()];
				assertTrue(box.getLower() <= coordinate.getLower()
						&& coordinate.getUpper() <= box.getUpper(), "E1(d): the rigorous"
								+ " enclosure of the ideal value of " + indeterminate.getName()
								+ " lies in the box " + box);
			} else {
				assertEquals(box.getLower(), box.getUpper(), 0,
						"a free input is its exact value: " + indeterminate.getName());
			}
			assertTrue(box.contains(ExactRational2D.ofBinary64(node.getLiveState()[
					indeterminate.getCoordinate()])), "and its live value");
		}
	}

	@Test
	void theControlsAndATargetOneUlpAwayAreNotCertified() {
		buildAuthorReplica();
		GeoPoint h = (GeoPoint) geo("H");
		GeoPoint m = (GeoPoint) geo("M");
		GeoPoint m1 = (GeoPoint) geo("M_1");
		final GeoPoint i = (GeoPoint) geo("I");
		final GeoPoint d1 = (GeoPoint) geo("D_1");
		// C1: the binary64 representations of n and h_2, through free points at the live values.
		add("Hb=" + point(h.getInhomX(), h.getInhomY()));
		add("Mb=" + point(m.getInhomX(), m.getInhomY()));
		add("nb=Segment(Hb,Mb)");
		add("Ub=" + point(m1.getInhomX(), m1.getInhomY()));
		add("Vb=" + point(m1.getInhomX(), m1.getInhomY() + 1));
		add("hb=Line(Ub,Vb)");
		// C2: the other intersection of c_1 with the support line of n.
		add("nl=Line(H,M)");
		GeoElementND[] both = getElements("Intersect(c_1,nl)");
		GeoPoint first = (GeoPoint) both[0].toGeoElement();
		GeoPoint second = (GeoPoint) both[1].toGeoElement();
		(distance(first, m1) > distance(second, m1) ? first : second).rename("M2");
		add("h3=Line(M2,k)");
		// C3: I on the constant vertical line through its live value.
		add("V1=" + point(i.getInhomX(), 0));
		add("V2=" + point(i.getInhomX(), 1));
		add("vb=Line(V1,V2)");
		// A target one ulp away: the circle c_1 through a point one ulp from D_1.
		add("Du=" + point(Math.nextUp(d1.getInhomX()), d1.getInhomY()));
		add("cu=Circle(H,Du)");
		getKernel().updateConstruction(false);

		for (Scope scope : Scope.values()) {
			Map<String, ConstructiveIdentityCertificate> controls = new LinkedHashMap<>();
			controls.put("C1 n", contact("e_1", "nb", "K", scope));
			controls.put("C1 h_2", contact("o_1", "hb", "K", scope));
			controls.put("C2", contact("o_1", "h3", "K", scope));
			controls.put("C3", plain("I", "vb", scope));
			controls.put("C5", contact("e_1", "n", "J", scope));
			controls.put("one ulp", contact("e_1", "cu", "K", scope));
			for (Map.Entry<String, ConstructiveIdentityCertificate> control
					: controls.entrySet()) {
				assertEquals(Status.NOT_AN_IDENTITY, control.getValue().getStatus(),
						"V-AUT-CIC-2 " + scope + " " + control.getKey() + ": "
								+ control.getValue());
			}
		}
		assertEquals(i.getInhomX(), ((GeoPoint) geo("V1")).getInhomX(), 0,
				"C3: the residual vanishes at the live values");
		ConstructiveIdentityCertificate wide = widened(CONTACTS[0], 5);
		assertEquals(Status.NOT_ESTABLISHED, wide.getStatus(), "C4: a box of half-width 5,"
				+ " where H can reach D_1: " + wide);
		assertEquals(ConstructiveIdentityCertificate.Cause.PRECONDITION_UNDECIDED,
				wide.getCause());
	}

	@Test
	void theDriverCircleIsDecidedByItsDefinitionNeverByItsMatrix() {
		buildAuthorReplica();
		double[] matrix = ((GeoConic) geo("p")).getFlatMatrix();
		ExactRational2D a2 = ExactRational2D.ofBinary64(matrix[2]);
		ExactRational2D a4 = ExactRational2D.ofBinary64(matrix[4]);
		ExactRational2D a5 = ExactRational2D.ofBinary64(matrix[5]);
		ExactRational2D squared = a4.multiply(a4).add(a5.multiply(a5)).subtract(a2);

		assertNotEquals(0, squared.compareTo(ExactRational2D.of(25)),
				"V-CFG-6: A4^2 + A5^2 - A2 of the captured matrix of p is not rbCone^2");
		for (Object[] contact : CONTACTS) {
			assertEquals(Status.IDENTITY_CERTIFIED, contact(contact, Scope.BOUNDARY).getStatus(),
					"the circle of the class, Circle(B, rbCone), decides: no conflict");
		}
	}

	@Test
	void accidentalEqualitiesHoldOnlyForTheirConstants() {
		add("O=(0,0)");
		add("ra=1.5");
		add("ca=Circle(O,ra)");
		add("X=Intersect(ca,yAxis,2)");
		add("f=Line((-2,1.5),(2,1.5))");
		add("T=(0.1,0)");
		add("cb=Circle((0.3,0),0.2)");
		getKernel().updateConstruction(false);
		assertEquals(1.5, ((GeoPoint) lookup("X")).getInhomY(), 0, "X is the top of ca");

		assertEquals(Status.IDENTITY_CERTIFIED, plain("X", "f", Scope.BOUNDARY).getStatus(),
				"V-M3-5: equal for the exact constants, which enter the key");
		assertEquals(Status.NOT_AN_IDENTITY, plain("X", "f", Scope.AUTOMATIC).getStatus(),
				"an accidental equality is no identity of the free inputs");
		for (Scope scope : Scope.values()) {
			assertEquals(Status.NOT_AN_IDENTITY, plain("T", "cb", scope).getStatus(),
					"V-M3-6: 0.3 - 0.2 against 0.1 between binary64 values, " + scope);
		}
	}

	@Test
	void aProofIsRecheckedInItsCellAndReplacedOutsideIt() {
		buildAuthorReplica();
		Map<String, ConstructiveIdentityCertificate> automatic = new LinkedHashMap<>();
		Map<String, ConstructiveIdentityCertificate> boundary = new LinkedHashMap<>();
		for (Object[] contact : CONTACTS) {
			automatic.put(name(contact), contact(contact, Scope.AUTOMATIC));
			boundary.put(name(contact), contact(contact, Scope.BOUNDARY));
		}
		List<String> all = new ArrayList<>(automatic.keySet());
		List<String> none = List.of();

		assertRechecks("base", automatic, all, boundary, all);
		GeoNumeric radius = (GeoNumeric) geo("rbCone");
		for (double value : new double[] {3, 4, 6, 7.5}) {
			setNumber(radius, value);
			assertRechecks("rbCone=" + value, automatic, all, boundary, none);
		}
		setNumber(radius, 5);
		GeoNumeric angle = (GeoNumeric) geo("alpha");
		double alpha = angle.getValue();
		for (double degrees : new double[] {35, 60}) {
			setNumber(angle, Math.toRadians(degrees));
			assertRechecks("alpha=" + degrees, automatic, all, boundary, all);
		}
		setNumber(angle, alpha);
		GeoPoint d1 = (GeoPoint) geo("D_1");
		GeoPoint h = (GeoPoint) geo("H");
		double x = d1.getInhomX();
		double y = d1.getInhomY();
		for (double factor : new double[] {0.5, 1.3, 3}) {
			place("D_1", h.getInhomX() + factor * (x - h.getInhomX()),
					h.getInhomY() + factor * (y - h.getInhomY()));
			assertRechecks("D_1 x" + factor, automatic, factor == 3
					? List.of("e_1 c_1 K", "e_1 c_1 J") : all, boundary, none);
		}
		assertNewProofs("D_1 x3", Status.CONTRADICTED, Status.IDENTITY_CERTIFIED, Status.INVALID,
				Status.IDENTITY_CERTIFIED, Status.CONTRADICTED);
		place("D_1", x, y);
		assertRechecks("restored", automatic, all, boundary, all);
		double parameter = parameter("H");
		for (double delta : new double[] {-20, 20}) {
			moveOnPath("H", parameter + delta);
			assertRechecks("H " + delta, automatic, all, boundary, all);
		}
		moveOnPath("H", parameter + 40);
		List<String> atJ = List.of("e_1 c_1 J", "e_1 r J");
		assertRechecks("H +40, another cell", automatic, atJ, boundary, atJ);
		assertNewProofs("H +40", Status.CONTRADICTED, Status.IDENTITY_CERTIFIED, Status.INVALID,
				Status.IDENTITY_CERTIFIED, Status.IDENTITY_CERTIFIED);
		moveOnPath("H", parameter);
		double onG = parameter("B");
		for (double delta : new double[] {-3, 3}) {
			moveOnPath("B", onG + delta);
			assertRechecks("B " + delta, automatic, all, boundary, all);
		}
		moveOnPath("B", onG);
		assertRechecks("restored again", automatic, all, boundary, all);
	}

	@Test
	void aRealTransitionOfASelectionStateChangesTheKey() {
		add("A0=(0,0)");
		add("c0=Circle(A0,3)");
		add("f0=Line((-5,1),(5,1))");
		add("X0=Intersect(c0,f0,1)");
		getKernel().updateConstruction(false);
		ConstructiveIdentityCertificate pending = plain("X0", "c0", Scope.AUTOMATIC);
		assertTrue(pending.isIdentityCertified(), pending.toString());

		// Both outputs of the shared intersection are labelled now: its label state, which
		// decides the effective outputs, is another one.
		getElements("Intersect(c0,f0)");
		getKernel().updateConstruction(false);
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int point = builder.add(lookup("X0"));
		int target = builder.add(lookup("c0"));
		ConstructiveCone settled = builder.build();
		ConstructiveIdentityCertificate again = plain("X0", "c0", Scope.AUTOMATIC);

		assertFalse(ConstructiveIdentityCertifier.recheck(pending, settled, point, target, -1,
				ConstructiveIdentityBudget.V1), "V-AUT-DYN-2: the key covers the selection state");
		assertNotEquals(pending.getKey(), again.getKey());
		assertTrue(again.isIdentityCertified(), "a new proof in the new state: " + again);
	}

	// ---------------------------------------------------------------- helpers

	private void assertRechecks(String position,
			Map<String, ConstructiveIdentityCertificate> automatic, List<String> automaticHolds,
			Map<String, ConstructiveIdentityCertificate> boundary, List<String> boundaryHolds) {
		for (Object[] contact : CONTACTS) {
			String name = name(contact);
			assertEquals(automaticHolds.contains(name), recheck(automatic.get(name), contact),
					"V-AUT-DYN-1 automatic proof of " + name + " at " + position);
			assertEquals(boundaryHolds.contains(name), recheck(boundary.get(name), contact),
					"V-AUT-DYN-1 boundary proof of " + name + " at " + position);
		}
	}

	private void assertNewProofs(String position, Status... expected) {
		for (int index = 0; index < CONTACTS.length; index++) {
			ConstructiveIdentityCertificate proof = contact(CONTACTS[index], Scope.AUTOMATIC);
			assertEquals(expected[index], proof.getStatus(), "new proof of "
					+ name(CONTACTS[index]) + " at " + position + ": " + proof);
		}
	}

	private boolean recheck(ConstructiveIdentityCertificate certificate, Object[] contact) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int target = builder.add(geo((String) contact[1]));
		int support = builder.add(geo("p"));
		int point = builder.addWithDriverSubstitution(traced((String) contact[0]), geo("E_1"),
				geo((String) contact[2]));
		return ConstructiveIdentityCertifier.recheck(certificate, builder.build(), point, target,
				support, ConstructiveIdentityBudget.V1);
	}

	private ConstructiveIdentityCertificate contact(Object[] contact, Scope scope) {
		return contact((String) contact[0], (String) contact[1], (String) contact[2], scope);
	}

	private ConstructiveIdentityCertificate contact(String locus, String target, String driver,
			Scope scope) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int targetNode = builder.add(geo(target));
		int support = builder.add(geo("p"));
		int point = builder.addWithDriverSubstitution(traced(locus), geo("E_1"), geo(driver));
		return ConstructiveIdentityCertifier.incidence(locus + " on " + target + " at " + driver,
				builder.build(), point, targetNode, support, scope, ConstructiveIdentityBudget.V1);
	}

	private ConstructiveIdentityCertificate widened(Object[] contact, double halfWidth) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int target = builder.add(geo((String) contact[1]));
		int support = builder.add(geo("p"));
		int point = builder.addWithDriverSubstitution(traced((String) contact[0]), geo("E_1"),
				geo((String) contact[2]));
		return ConstructiveIdentityCertifier.incidence("C4", builder.build(), point, target,
				support, Scope.AUTOMATIC, ConstructiveIdentityBudget.V1, halfWidth);
	}

	private ConstructiveIdentityCertificate plain(String point, String target, Scope scope) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int subject = builder.add(geo(point));
		int object = builder.add(geo(target));
		return ConstructiveIdentityCertifier.incidence(point + " on " + target, builder.build(),
				subject, object, -1, scope, ConstructiveIdentityBudget.V1);
	}

	private GeoElement traced(String locus) {
		return geo("e_1".equals(locus) ? "J_1" : "K_1");
	}

	private static String name(Object[] contact) {
		return contact[0] + " " + contact[1] + " " + contact[2];
	}

	private double parameter(String label) {
		return ((GeoPoint) geo(label)).getPathParameter().getT();
	}

	/** Moves a point on its path to a parameter, as a drag along the path does. */
	private void moveOnPath(String label, double t) {
		GeoPoint point = (GeoPoint) geo(label);
		point.getPathParameter().setT(t);
		((Path) point.getPath()).pathChanged(point);
		point.updateCoords();
		point.updateCascade();
	}

	private static void setNumber(GeoNumeric number, double value) {
		number.setValue(value);
		number.updateCascade();
	}

	private static String point(double x, double y) {
		return "(" + x + "," + y + ")";
	}

	private static double distance(GeoPoint a, GeoPoint b) {
		return Math.hypot(a.getInhomX() - b.getInhomX(), a.getInhomY() - b.getInhomY());
	}

	private static List<String> sorted(List<String> names) {
		List<String> order = new ArrayList<>(names);
		order.sort((a, b) -> Integer.compare(AUTOMATIC_INDETERMINATES.indexOf(a),
				AUTOMATIC_INDETERMINATES.indexOf(b)));
		return order;
	}
}
