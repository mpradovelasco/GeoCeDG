/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusDriverCorrespondencePoint2D;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolver.Cause;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoConicPart;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoSegment;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/**
 * The rows of the Stage A validation matrix ({@code pre_g9b_cu3_validation_matrix.md} sections
 * 3.1 to 3.3) under the constructive authority, with the revisions of D-AUT-11: provenance,
 * support, correspondence and domain of the driver parameter, membership by transfer, the
 * second incidence against the constructive target, degeneracies, persistence, determinism and
 * the exact decisions of accidental equalities. Each row names the cause that fails it closed
 * or the evidence that defines it; the five mandatory contacts are in {@code StructuralPointTest}.
 * A refutation whose premises contain a boundary substitute is not rigorous for the ideal
 * objects and leaves the relation {@code NOT_ESTABLISHED} ({@code
 * locus-v2-constructive-authority.md} section 7 (b)); since increment E2 a constructive identity
 * certificate decides it for every value of the cut when it can (section 8). The rows that the
 * preparation decided against binary64 targets are decided here against constructive ones.
 */
final class StructuralPointMatrixTest extends ConstructiveModelReplicaTest {

	private static final double CLOSE = 1E-9;

	@Test
	void anObjectWithoutAnAdmittedProvenanceIsRefused() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoPoint k = (GeoPoint) geo("K");
		add("F2=(" + k.getInhomX() + "," + k.getInhomY() + ")");
		add("K2=Dilate(K,1,B)");

		assertEquals(Cause.PROVENANCE_NOT_ESTABLISHED, cause(add("P1=Point(e_1,F2)")),
				"V-A-1: a free point at the coordinates of K");
		assertEquals(Cause.PROVENANCE_NOT_ESTABLISHED, cause(add("P2=Point(e_1,K2)")),
				"V-A-2: an algorithm outside the table, equal to K");
	}

	@Test
	void aPointOnThePathOfTheSupportIsAdmitted() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("G2=Point(p)");
		place("G2", -0.9380450720469029, -0.367469712127068);

		StructuralPointResolution point = resolution(add("P=Point(e_1,G2)"));
		assertTrue(point.isDefined(), "V-A-3: PATH_POINT on the support of the driver: "
				+ point);
		assertTrue(point.getPremises().get(0).startsWith("PATH_POINT"));
	}

	@Test
	void aCoincidentOrCopiedSupportIsAnotherObject() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("p2=Circle(B,rbCone)");
		add("K3=Intersect(p2,i,1)");

		assertEquals(Cause.DRIVER_SUPPORT_MISMATCH, cause(add("P=Point(e_1,K3)")),
				"V-A-4: an equal circle is not the support");
		paste(List.of(geo("p")));
		assertNotNull(lookup("p_{1}"), "the copy of the support is pasted");
		add("K4=Intersect(p_{1},i,1)");
		assertEquals(Cause.DRIVER_SUPPORT_MISMATCH, cause(add("Q=Point(e_1,K4)")),
				"V-A-5: a copy of the support is another object");
	}

	@Test
	void aScalarDriverIsAnUnsupportedFamily() {
		add("s=0");
		add("V=(s,s^2/5)");
		add("dom={false,{0,5,true,true}}");
		add("L=LocusV2(V,s,dom)");
		add("W=(1,0.2)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		assertEquals(Cause.UNSUPPORTED_DRIVER_FAMILY, cause(add("P=Point(L,W)")), "V-A-6");
	}

	@Test
	void anArcDriverIsOutsideTheConstructiveClass() {
		add("O=(0,0)");
		add("a=CircularArc(O,(2,0),(0,2))");
		add("P=Point(a)");
		add("A=(2,0)");
		add("Q=Midpoint(P,A)");
		add("L=LocusV2(Q,P)");
		((GeoConicPart) lookup("a")).setAllowOutlyingIntersections(true);
		GeoElementND[] outputs = getElements("Intersect(a,xAxis)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		for (GeoElementND output : outputs) {
			assertEquals(Cause.UNSUPPORTED_DRIVER_FAMILY, cause(add("R=Point(L,"
					+ output.getLabelSimple() + ")")), "V-A-10 under D-AUT-11: arcs are outside"
							+ " constructive-class/v1, inside the arc or not");
			lookup("R").remove();
		}
	}

	@Test
	void theCorrespondenceUsesTheExactPointNeverTheCoordinatesOfD() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoPoint driver = (GeoPoint) geo("E_1");
		double driverX = driver.getInhomX();
		double driverParameter = driver.getPathParameter().getT();

		StructuralPointResolution point = resolution(add("P=Point(e_1,K)"));
		assertEquals("0", point.getTransfer().getParameter(), "V-A-7: t*(K) = 0 exactly");
		StructuralPointResolution seam = resolution(add("S=Point(e_1,J)"));
		assertEquals("pi", seam.getTransfer().getParameter(), "t*(J) = pi, enclosed");
		assertEquals(driverX, driver.getInhomX(), 0, "V-A-7: the live driver never moves");
		assertEquals(driverParameter, driver.getPathParameter().getT(), 0);

		GeoPoint k = (GeoPoint) geo("K");
		k.setCoords(Math.nextUp(k.getInhomX()), k.getInhomY(), 1);
		StructuralPointResolution perturbed = StructuralPointResolver.resolve(
				(GeoLocusV2) geo("e_1"), k, null, ExactEvaluationBudget.V1);
		assertEquals("0", perturbed.getTransfer().getParameter(),
				"V-A-8: the binary64 coordinates of D are never the authority");
	}

	@Test
	void theDriverDomainOfASegmentIsDecidedExactly() {
		add("A0=(0,0)");
		add("B0=(4,0)");
		add("s=Segment(A0,B0)");
		add("S1=Point(s)");
		add("T=(1,2)");
		add("Q=Midpoint(S1,T)");
		add("L=LocusV2(Q,S1)");
		add("g=Line((5,-1),(5,1))");
		((GeoSegment) lookup("s")).setAllowOutlyingIntersections(true);
		add("D2=Intersect(s,g)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		StructuralPointResolution start = resolution(add("P0=Point(L,A0)"));
		StructuralPointResolution end = resolution(add("P1=Point(L,B0)"));
		assertTrue(start.isDefined(), "V-A-9: a defining point at the start: " + start);
		assertTrue(start.getPremises().get(0).startsWith("DEFINING_POINT"));
		assertEquals("0", start.getTransfer().getParameter());
		assertEquals(0.5, start.getX(), CLOSE);
		assertEquals(1, start.getY(), CLOSE);
		assertTrue(end.isDefined(), "V-A-9: a defining point at the end: " + end);
		assertEquals("1", end.getTransfer().getParameter());
		assertEquals(Cause.OUT_OF_DRIVER_DOMAIN, cause(add("P2=Point(L,D2)")),
				"V-A-9: on the support line outside the segment");
	}

	@Test
	void theSeamIsDecidedByTheComponentThatReachesIt() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("P=Point(c)");
		add("A=(2,0)");
		add("Q=Midpoint(P,A)");
		add("L=LocusV2(Q,P)");
		add("Ck=(0,1)");
		add("k=Circle(Ck,1)");
		add("w=Line(P,xAxis)");
		add("U=Intersect(k,w,1)");
		add("M=LocusV2(U,P)");
		GeoElementND[] axis = getElements("Intersect(c,xAxis)");
		GeoPoint seamPoint = outputWith(axis, -2);
		GeoPoint origin = outputWith(axis, 2);
		AuthorityPolicyAdoption.adopt(getConstruction());

		StructuralPointResolution closed = resolution(add("R=Point(L,"
				+ seamPoint.getLabelSimple() + ")"));
		assertTrue(closed.isDefined(), "V-A-11: pi in a closed periodic component: " + closed);
		assertEquals("pi", closed.getTransfer().getParameter());
		assertEquals(0, closed.getX(), CLOSE);
		assertEquals(0, closed.getY(), CLOSE);
		assertEquals(Cause.SEAM_SIDE_UNDECIDED, cause(add("S=Point(M,"
				+ seamPoint.getLabelSimple() + ")")),
				"V-A-11: the component [0, pi] of M ends at the seam");
		assertEquals(Cause.COMPONENT_BOUNDARY_UNDECIDED, cause(add("T=Point(M,"
				+ origin.getLabelSimple() + ")")),
				"V-A-14: its other end is the tangency of w and k at t = 0");
	}

	@Test
	void thePointFollowsTheDriverWhereTheLocusIs() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("P=Point(c)");
		add("A=(2,0)");
		add("Q=Midpoint(P,A)");
		add("L=LocusV2(Q,P)");
		add("D=Intersect(c,yAxis,1)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		StructuralPointResolution point = resolution(add("R=Point(L,D)"));
		GeoPoint driver = (GeoPoint) lookup("D");
		assertTrue(point.isDefined(), String.valueOf(point));
		assertEquals((driver.getInhomX() + 2) / 2, point.getX(), CLOSE,
				"V-A-12: F(t*(D)) is the point of the locus with the driver at D");
		assertEquals(driver.getInhomY() / 2, point.getY(), CLOSE);
	}

	@Test
	void aParameterOutsideTheComponentsOrAtTheirEndsFailsClosed() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("P=Point(c)");
		add("k=Circle(O,1)");
		add("v=PerpendicularLine(P,xAxis)");
		add("Q=Intersect(k,v,1)");
		add("L=LocusV2(Q,P)");
		add("D1=Intersect(c,Line((1.5,0),(1.5,1)),1)");
		add("D2=Intersect(c,Line((1,0),(1,1)),1)");
		add("D3=Intersect(c,Line((0.5,0),(0.5,1)),1)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		StructuralPointResolution outside = resolution(add("R1=Point(L,D1)"));
		assertEquals(Cause.OUTSIDE_CERTIFIED_COMPONENTS, outside.getCause(),
				"V-A-13: the locus does not exist there");
		assertTrue(outside.getTransfer().getPremises().stream()
				.anyMatch(premise -> premise.startsWith("CU2 certified absence")),
				"with the cause of CU2: " + outside.getTransfer().getPremises());
		assertEquals(Cause.COMPONENT_BOUNDARY_UNDECIDED, cause(add("R2=Point(L,D2)")),
				"V-A-14: at the transition where v touches k");
		assertTrue(resolution(add("R3=Point(L,D3)")).isDefined(), "inside a component");
	}

	@Test
	void aPointComponentIsNeverMaterialized() {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((83,19),40)");
		add("N=Point(g1)");
		add("t=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		add("lq=LocusV2(Q,N)");
		getKernel().updateConstruction(false);
		add("D=Intersect(g1,Line((83,19),(59,-13)),2)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		assertEquals(Cause.POINT_COMPONENT_NOT_MATERIALIZED, cause(add("X=Point(lq,D)")),
				"V-A-15: the external tangency of V-POINT-1 of CU2");
	}

	@Test
	void aLocalComponentDefinesThePointWhenCompletenessIsNotEstablished() {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((35,-45),40)");
		add("N=Point(g1)");
		add("t2=Ray(P,N)");
		add("R2=Intersect(p,t2,2)");
		add("lr2=LocusV2(R2,N)");
		getKernel().updateConstruction(false);
		add("D=Point(g1)");
		place("D", 67, -21);
		add("Dc=Point(g1)");
		place("Dc", 59, -13);
		AuthorityPolicyAdoption.adopt(getConstruction());

		StructuralPointResolution local = resolution(add("X=Point(lr2,D)"));
		assertTrue(local.isDefined(), "V-A-16: a certified local component (ADR 0027): "
				+ local);
		assertEquals(Cause.COMPONENT_BOUNDARY_UNDECIDED, cause(add("Y=Point(lr2,Dc)")),
				"V-A-14: the unresolved band of the coincident roots");
	}

	@Test
	void aSelectionThatIsNotReconstructedFailsClosed() {
		buildAuthorReplica();
		GeoPoint b = (GeoPoint) geo("B");
		double top = b.getInhomY() + 5;
		add("i2=Line((" + b.getInhomX() + "," + top + "),(" + (b.getInhomX() + 1) + "," + top
				+ "))");
		add("K5=Intersect(p,i2,1)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		assertEquals(Cause.SELECTION_NOT_RECONSTRUCTED, cause(add("P=Point(e_1,K5)")),
				"V-A-20: coincident roots of the line tangent to p");
	}

	@Test
	void thePointFollowsTheObjectWhenTheInheritedSelectionChanges() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("P=Point(c)");
		add("A=(2,0)");
		add("Q=Midpoint(P,A)");
		add("L=LocusV2(Q,P)");
		add("C0=(0,1)");
		add("U=(3,1)");
		add("w=Line(C0,U)");
		add("D=Intersect(c,w,1)");
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoElement output = add("R=Point(L,D)");
		GeoPoint d = (GeoPoint) lookup("D");
		double before = d.getInhomX();

		// The inherited permutation is fixed after the first computation: reversing the
		// direction of w gives output 1 the other root.
		place("U", -3, 1);

		assertNotEquals(Math.signum(before), Math.signum(d.getInhomX()),
				"the inherited selection now gives the other root to D");
		StructuralPointResolution moved = resolution(output);
		assertTrue(moved.isDefined(), String.valueOf(moved));
		assertEquals((d.getInhomX() + 2) / 2, moved.getX(), CLOSE,
				"V-A-21: the point follows the object D, never its former coordinates");
		assertEquals(d.getInhomY() / 2, moved.getY(), CLOSE);
	}

	@Test
	void aLimitedTargetDisappearsAndReappearsWithTheSameIdentity() {
		add("O=(0,0)");
		add("c=Circle(O,3)");
		add("P=Point(c)");
		add("Q=Midpoint(O,P)");
		add("L=LocusV2(Q,P)");
		add("D=Intersect(c,yAxis,2)");
		add("U=(-1,1.5)");
		add("V=(2,1.5)");
		add("s=Segment(U,V)");
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoElement output = add("X=Intersect(L,s,D)");
		StructuralPointResolution first = resolution(output);
		assertEquals(RelationIntegrity.STRICT, first.getIntegrity(), String.valueOf(first));
		final String identity = identity(output);

		place("U", 1, 1.5);
		assertEquals(Cause.NOT_ON_LIMITED_TARGET, resolution(output).getCause(),
				"V-A-19: on the support line of s outside it, decided exactly");
		place("U", -1, 1.5);

		StructuralPointResolution restored = resolution(output);
		assertTrue(restored.isDefined(), "restored: " + restored);
		assertEquals(first.getX(), restored.getX(), 0, "the same point");
		assertEquals(first.getY(), restored.getY(), 0);
		assertSame(output, lookup("X"));
		assertEquals(identity, identity(output), "the same durable identity");
	}

	@Test
	void theLimitedTargetOfTheModelFailsClosedAndReturnsWithTheSameIdentity() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoElement output = add("Q=Intersect(e_1,n,K)");
		StructuralPointResolution first = resolution(output);
		assertTrue(first.isDefined(), String.valueOf(first));
		final String identity = identity(output);
		GeoPoint d = (GeoPoint) geo("D_1");
		GeoPoint h = (GeoPoint) geo("H");
		double x = d.getInhomX();
		double y = d.getInhomY();

		place("D_1", h.getInhomX() + 3 * (x - h.getInhomX()), h.getInhomY() + 3 * (y
				- h.getInhomY()));
		StructuralPointResolution outside = resolution(output);
		assertEquals(Cause.NOT_ON_LIMITED_TARGET, outside.getCause(), "V-A-19, V-DYN-2 and"
				+ " V-AUT-DYN-1: with D_1 times 3 the constructive identity certificate decides the"
				+ " exit from n for every value of the cut H: " + outside);
		assertEquals(RelationIntegrity.CONTRADICTED, outside.getIncidence().getIntegrity());
		assertTrue(resolution(add("P=Point(e_1,K)")).isDefined(), "Point(L, D) stays defined");
		place("D_1", x, y);

		StructuralPointResolution restored = resolution(output);
		assertTrue(restored.isDefined(), "restored: " + restored);
		assertEquals(first.getX(), restored.getX(), 0, "the same point");
		assertEquals(first.getY(), restored.getY(), 0);
		assertSame(output, lookup("Q"));
		assertEquals(identity, identity(output), "the same durable identity");
	}

	@Test
	void anIncidenceInferredFromCoordinatesIsNeverEstablished() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		StructuralPointResolution point = resolution(add("P=Point(e_1,K)"));
		add("U=(" + point.getX() + "," + point.getY() + ")");
		add("V=(" + (point.getX() + 1) + "," + (point.getY() + 0.3) + ")");
		add("w=Line(U,V)");
		GeoPoint m = (GeoPoint) geo("M_1");
		add("Z=(" + m.getInhomX() + "," + (m.getInhomY() + 1E-13) + ")");
		add("w2=Line(Z,H)");

		StructuralPointResolution through = resolution(add("Q1=Intersect(e_1,w,K)"));
		assertFalse(through.isDefined(), "V-A-18: P at binary64 distance zero from w");
		StructuralPointResolution near = resolution(add("Q2=Intersect(e_1,w2,K)"));
		assertFalse(near.isDefined(), "V-A-17: a target 1e-13 from M_1 is not accepted");
		assertEquals(Cause.INCIDENCE_NOT_ESTABLISHED, near.getCause(),
				"refuted only with the substitute Cr, and no identity of the free inputs");
		assertEquals(ConstructiveIdentityCertificate.Status.NOT_AN_IDENTITY,
				near.getCertificate().getStatus(), "never a contradiction: " + near);
		assertTrue(point.isDefined(), "Point(L, D) stays defined");
	}

	@Test
	void theStructuralPointIsCopiedWithItsSources() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("P=Point(e_1,K)");
		paste(List.of(lookup("P")));

		GeoElement copy = lookup("P_{1}");
		assertNotNull(copy, "V-A-22: the copy is pasted");
		StructuralPointResolution copied = resolution(copy);
		assertTrue(copied.isDefined(), "the copy is reconstructed from the DAG: " + copied);
		assertEquals(resolution(lookup("P")).getX(), copied.getX(), CLOSE);
		assertNotEquals(identity(lookup("P")), identity(copy), "a copy is another object");
	}

	@Test
	void theFiveContactsFollowTheSweepsWithTheirIdentities() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		String[] commands = {"Q1=Intersect(e_1,n,K)", "Q2=Intersect(e_1,c_1,K)",
				"Q3=Intersect(o_1,h_2,K)", "Q4=Intersect(e_1,c_1,J)", "Q5=Intersect(e_1,r,J)"};
		List<GeoElement> outputs = new ArrayList<>();
		List<String> identities = new ArrayList<>();
		for (String command : commands) {
			GeoElement output = add(command);
			outputs.add(output);
			identities.add(identity(output));
		}
		GeoNumeric radius = (GeoNumeric) geo("rbCone");
		for (double value : new double[] {3, 4, 6, 7.5, 5}) {
			setNumber(radius, value);
			assertContacts(outputs, identities, "rbCone = " + value);
		}
		GeoNumeric angle = (GeoNumeric) geo("alpha");
		double alpha = angle.getValue();
		for (double degrees : new double[] {35, 60}) {
			setNumber(angle, Math.toRadians(degrees));
			assertContacts(outputs, identities, "alpha = " + degrees);
		}
		setNumber(angle, alpha);
		GeoPoint d = (GeoPoint) geo("D_1");
		GeoPoint h = (GeoPoint) geo("H");
		double x = d.getInhomX();
		double y = d.getInhomY();
		for (double factor : new double[] {0.5, 1.3, 1}) {
			place("D_1", h.getInhomX() + factor * (x - h.getInhomX()), h.getInhomY() + factor
					* (y - h.getInhomY()));
			assertContacts(outputs, identities, "D_1 times " + factor);
		}
	}

	@Test
	void twoResolutionsOfOneRevisionAreIdentical() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoLocusV2 locus = (GeoLocusV2) geo("e_1");
		StructuralPointResolution first = StructuralPointResolver.resolve(locus,
				(GeoPoint) geo("K"), geo("c_1"), ExactEvaluationBudget.V1);
		StructuralPointResolution second = StructuralPointResolver.resolve(locus,
				(GeoPoint) geo("K"), geo("c_1"), ExactEvaluationBudget.V1);

		assertEquals(String.valueOf(first), String.valueOf(second), "V-DET-CU3-1");
		assertEquals(String.valueOf(first.getIncidence()), String.valueOf(second.getIncidence()));
		assertEquals(String.valueOf(first.getTransfer()), String.valueOf(second.getTransfer()));
		assertEquals(first.getX(), second.getX(), 0);
		assertEquals(String.valueOf(first.getExactCounters()),
				String.valueOf(second.getExactCounters()), "G-AUT-8: deterministic counters");
		assertTrue(first.getExactCounters().getOperations() > 0);
		assertEquals(String.valueOf(first.getRelaxedCounters()),
				String.valueOf(second.getRelaxedCounters()));
		assertTrue(first.getRelaxedCounters().size() > 1,
				"SUBSTITUTE_GENERIC evaluates the base and every perturbation");
	}

	@Test
	void accidentalEqualitiesAreDecidedExactly() {
		add("O=(0,0)");
		add("c=Circle(O,3)");
		add("P=Point(c)");
		add("Q=Midpoint(O,P)");
		add("L=LocusV2(Q,P)");
		add("D=Intersect(c,yAxis,2)");
		add("g=Line((0,1.5),(1,1.5))");
		String above = Double.toString(Math.nextUp(1.5));
		add("g2=Line((0," + above + "),(1," + above + "))");
		add("c6=Circle(O,0.6)");
		add("P6=Point(c6)");
		add("Q6=Midpoint(O,P6)");
		add("L6=LocusV2(Q6,P6)");
		add("D6=Intersect(c6,yAxis,2)");
		add("F6=(0,0.2)");
		add("G6=(0,0.4)");
		add("M6=Midpoint(F6,G6)");
		add("g6=OrthogonalLine(M6,yAxis)");
		AuthorityPolicyAdoption.adopt(getConstruction());
		assertEquals(3, ((GeoPoint) lookup("D")).getInhomY(), 0, "D is the top of c");
		assertEquals(0.6, ((GeoPoint) lookup("D6")).getInhomY(), 0, "D6 is the top of c6");
		assertEquals(0.1 + 0.2, ((GeoPoint) lookup("M6")).getInhomY(), 0,
				"M6 is 0.1 + 0.2: exactly the binary64 sum, and its live value rounds it");

		StructuralPointResolution equal = resolution(add("X=Intersect(L,g,D)"));
		assertTrue(equal.isDefined(), "V-M3-5: " + equal);
		assertEquals(RelationIntegrity.STRICT, equal.getIntegrity(),
				"an exact equality with constructible premises is STRICT (class 2)");
		assertEquals(Cause.INCIDENCE_REFUTED_IN_CONSTRUCTIVE_CONFIGURATION,
				cause(add("Y=Intersect(L,g2,D)")), "V-M3-4: one ulp away is refuted exactly");
		StructuralPointResolution sum = resolution(add("Z=Intersect(L6,g6,D6)"));
		assertEquals(Cause.INCIDENCE_REFUTED_IN_CONSTRUCTIVE_CONFIGURATION, sum.getCause(),
				"V-M3-6: 0.3 against 0.1 + 0.2, equal only between non-representable values");
		assertEquals(RelationIntegrity.CONTRADICTED, sum.getIncidence().getIntegrity());
	}

	@Test
	void aCertifiedRootNearThePointIsNoProofOfThePrescribedIncidence() {
		add("O=(0,0)");
		add("c=Circle(O,3)");
		add("P=Point(c)");
		add("Q=Midpoint(O,P)");
		add("L=LocusV2(Q,P)");
		add("D=Intersect(c,yAxis,2)");
		add("U=(-1,0.500000000001)");
		add("V=(1,2.500000000001)");
		add("g=Line(U,V)");
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoLocusIntersectionResult generic = (GeoLocusIntersectionResult) add("R=Intersect(L,g)");
		boolean certifiedNear = false;
		for (LocusIntersectionSolution2D solution : generic.getIntersectionResult()
				.getFiniteSolutions()) {
			certifiedNear |= Math.hypot(solution.getEvaluatedPoint().getX(),
					solution.getEvaluatedPoint().getY() - 1.5) < 1E-11
					&& solution.getRevisionEvidence().getExistenceEvidence().getGrade()
							== ExistenceGrade.EXISTENCE_CERTIFIED;
		}

		assertTrue(certifiedNear, "a transverse root within 1e-12 of P has certified existence");
		assertEquals(Cause.INCIDENCE_REFUTED_IN_CONSTRUCTIVE_CONFIGURATION,
				cause(add("X=Intersect(L,g,D)")),
				"V-EVID-4: the certified root proves no incidence at the prescribed point");
	}

	@Test
	void anInheritedConditionIsAnUnestablishedPremiseNeverAStrictOne() {
		add("O=(0,0)");
		add("c=Circle(O,3)");
		add("P=Point(c)");
		add("Q=Midpoint(O,P)");
		add("L=LocusV2(Q,P)");
		add("D=Intersect(c,yAxis,2)");
		add("M=Midpoint(O,D)");
		add("a=1");
		add("Pc=If(a>0,(2,4),(3,-1))");
		add("A1=(0,0)");
		add("B1=(1,1)");
		add("C1=(2,2)");
		add("Pb=If(AreCollinear(A1,B1,C1),(2,4),(3,-1))");
		add("gc=Line(Pc,M)");
		add("gb=Line(Pb,M)");
		add("rb=If(AreCollinear(A1,B1,C1),Line(M,(1,1.5)),Line(M,(1,2)))");
		AuthorityPolicyAdoption.adopt(getConstruction());

		for (String[] row : new String[][] {{"Xc=Intersect(L,gc,D)", "Pc", "V-AUT-NUM-1"},
				{"Xb=Intersect(L,gb,D)", "Pb", "V-AUT-NUM-2"}}) {
			StructuralPointResolution relaxed = resolution(add(row[0]));
			assertTrue(relaxed.isDefined(), row[2] + ": " + relaxed);
			assertEquals(RelationIntegrity.RELAXED, relaxed.getIncidence().getIntegrity(),
					row[2] + ": P1 and P5, at most RELAXED through the inherited decision");
			assertTrue(relaxed.getIncidence().getUnestablishedPremises().stream()
					.anyMatch(premise -> premise.contains(row[1])), row[2] + ": "
							+ relaxed.getIncidence().getUnestablishedPremises());
		}
		StructuralPointResolution line = resolution(add("Xr=Intersect(L,rb,D)"));
		assertFalse(line.isDefined(), "V-AUT-NUM-2: a line conditioned by a boolean of D2 is"
				+ " outside the class: " + line);
	}

	@Test
	void theViewNeverChangesAResult() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoLocusV2 locus = (GeoLocusV2) geo("e_1");
		List<String> before = viewIndependentResults(locus);
		EuclidianView view = getApp().getActiveEuclidianView();

		view.setCoordSystem(17, 503, 0.37, 2.9);
		getKernel().updateConstruction(false);

		assertEquals(before, viewIndependentResults(locus), "V-VIEW-1: zoom and origin");
		view.setCoordSystem(300, 300, 4000, 4000);
		getKernel().updateConstruction(false);
		assertEquals(before, viewIndependentResults(locus), "V-VIEW-1: a large zoom");
	}

	@Test
	void aJointRecomputationIsTheConstructiveConfiguration() {
		add("O=(0,0)");
		add("U=(1,1)");
		add("W=(1,-1)");
		add("V=(-1,1)");
		add("c=Circle(O,U)");
		add("d=Circle(O,3)");
		add("P=Point(d)");
		add("q=Ray(O,P)");
		add("Q=Intersect(c,q,1)");
		add("L=LocusV2(Q,P)");
		add("r=Circle(O,W)");
		add("r4=Circle(O,V)");
		add("D=Intersect(d,xAxis,1)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		StructuralPointResolution joint = resolution(add("X=Intersect(L,r,D)"));
		assertTrue(joint.isDefined(), "V-CFG-1 under AUT-7: both radii are sqrt(2) in Sigma_C: "
				+ joint);
		assertEquals(RelationIntegrity.STRICT, joint.getIntegrity());
		assertEquals(RelationIntegrity.STRICT, resolution(add("Y=Intersect(L,c,D)"))
				.getIntegrity(), "V-CFG-2: the target is the constant of the slice");
		assertEquals(RelationIntegrity.STRICT, resolution(add("Z=Intersect(L,r4,D)"))
				.getIntegrity(), "V-CFG-4: another term with the same ideal radius");
	}

	// ---------------------------------------------------------------- helpers

	/** The structural point and the five contacts resolved afresh, without any cached state. */
	private List<String> viewIndependentResults(GeoLocusV2 locus) {
		List<String> results = new ArrayList<>();
		for (String[] contact : new String[][] {{"e_1", "K", null}, {"e_1", "K", "n"},
				{"e_1", "K", "c_1"}, {"o_1", "K", "h_2"}, {"e_1", "J", "c_1"},
				{"e_1", "J", "r"}}) {
			StructuralPointResolution resolution = StructuralPointResolver.resolve(
					contact[0].equals("e_1") ? locus : (GeoLocusV2) geo(contact[0]),
					(GeoPoint) geo(contact[1]), contact[2] == null ? null : geo(contact[2]),
					ExactEvaluationBudget.V1);
			results.add(resolution + " " + resolution.getIncidence() + " "
					+ resolution.getTransfer() + " " + resolution.getX() + " "
					+ resolution.getY());
		}
		return results;
	}

	private void assertContacts(List<GeoElement> outputs, List<String> identities,
			String step) {
		GeoPoint m1 = (GeoPoint) geo("M_1");
		for (int index = 0; index < outputs.size(); index++) {
			GeoElement output = outputs.get(index);
			StructuralPointResolution resolved = resolution(output);
			String row = "V-DYN-1, " + step + ", " + output.getLabelSimple() + ": " + resolved;
			assertTrue(resolved.isDefined(), row);
			assertEquals(RelationIntegrity.STRICT, resolved.getMembership().getIntegrity(), row);
			assertEquals(RelationIntegrity.STRICT, resolved.getIncidence().getIntegrity(),
					row + ", by the constructive identity certificate in its cell (E2)");
			assertEquals(identities.get(index), identity(output), row);
			if (index < 2) {
				assertEquals(m1.getInhomX(), resolved.getX(), CLOSE, row);
				assertEquals(m1.getInhomY(), resolved.getY(), CLOSE, row);
			}
		}
	}

	private static void setNumber(GeoNumeric number, double value) {
		number.setValue(value);
		number.updateCascade();
	}

	private static GeoPoint outputWith(GeoElementND[] outputs, double x) {
		for (GeoElementND output : outputs) {
			if (((GeoPoint) output).getInhomX() == x) {
				return (GeoPoint) output;
			}
		}
		throw new IllegalStateException("no output at x = " + x);
	}

	private void paste(List<GeoElement> selection) {
		String clipboard = InternalClipboard.getTextToSave(getApp(), selection, text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(),
				new ArrayList<>(List.of(clipboard.substring(0, separator).trim().split(" "))),
				clipboard.substring(separator + 1));
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

	private static Cause cause(GeoElement point) {
		return resolution(point).getCause();
	}
}
