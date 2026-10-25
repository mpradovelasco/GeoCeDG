/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Operation;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D.Step;
import org.geocedg.common.kernel.locus.constructive.Cu2CertificateTransfer.Cause;
import org.geocedg.common.kernel.locus.constructive.Cu2CertificateTransfer.ClosedDecision;
import org.geocedg.common.kernel.locus.constructive.Cu2CertificateTransfer.Record;
import org.geocedg.common.kernel.locus.constructive.Cu2CertificateTransfer.Route;
import org.geocedg.common.kernel.locus.constructive.ExactQuadraticTower.Element;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D;
import org.geogebra.common.kernel.geos.GeoConic;
import org.geogebra.common.kernel.geos.GeoLine;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.junit.jupiter.api.Test;

/**
 * The transfer {@code cu2-certificate-transfer/v1} of the membership of a structural point in
 * its locus ({@code locus-v2-certified-construction-model.md} section 12.2; validation matrix
 * rows V-AUT-TRF-1 to V-AUT-TRF-6): N1, N2 and the margin route on the replica of the author's
 * model, whose constants come through a point on a path and a rotation, and the fail-closed
 * causes when a condition is not demonstrated; the closed decisions a strict consumer requests,
 * a fold by the route structure and the location of a root by the route re-decision, refused
 * with a typed cause or refuted when the constructive configuration decides otherwise. Since
 * increment E2 a premise of a fold that reads a cut is decided by a constructive identity
 * certificate for every value of the cut.
 */
final class Cu2CertificateTransferTest extends ConstructiveModelReplicaTest {

	@Test
	void theMembershipOfTheMandatoryContactsTransfersByMargin() {
		buildAuthorReplica();
		String[][] cases = {{"e_1", "J_1", "K", "0"}, {"o_1", "K_1", "K", "0"},
				{"e_1", "J_1", "J", "pi"}};
		for (String[] contact : cases) {
			ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
			int traced = builder.addWithDriverSubstitution(geo(contact[1]), geo("E_1"),
					geo(contact[2]));
			ConstructiveCone cone = builder.build();

			Record record = Cu2CertificateTransfer.membership((GeoLocusV2) geo(contact[0]),
					cone, traced, ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1));

			String name = String.join("/", contact);
			assertTrue(record.isTransferred(), name + ": " + record);
			assertEquals(Route.MARGIN, record.getRoute());
			assertEquals(contact[3], record.getParameter(), name + ": exact driver parameter");
			assertNotNull(record.getFingerprint(), "N2 cites the consuming revision");
			assertNotNull(record.getComponent());
			assertTrue(record.getPremises().stream().anyMatch(p -> p.startsWith("N1:")));
			assertTrue(record.getPremises().stream().anyMatch(p -> p.startsWith("margin:")));
		}
	}

	@Test
	void anExactParameterOnAnAxisOfTheFrameTransfers() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("E=Point(c)");
		place("E", 1.2, 1.6);
		add("Q=(0.5,1)");
		add("f=Line(E,Q)");
		add("X=Intersect(f,xAxis)");
		GeoLocusV2 locus = add("L=LocusV2(X,E)");
		add("D=Intersect(c,yAxis,2)");
		getKernel().updateConstruction(false);
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int traced = builder.addWithDriverSubstitution(lookup("X"), lookup("E"), lookup("D"));
		ConstructiveCone cone = builder.build();

		Record record = Cu2CertificateTransfer.membership(locus, cone, traced,
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1));

		assertTrue(record.isTransferred(), String.valueOf(record));
		assertTrue(record.getParameter().equals("pi/2") || record.getParameter().equals("-pi/2"),
				record.getParameter());
	}

	@Test
	void aDriverAtAnInvalidParameterIsNotTransferred() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("E=Point(c)");
		place("E", 1.2, 1.6);
		add("Q=(0.5,1)");
		add("f=Line(E,Q)");
		add("X=Intersect(f,xAxis)");
		GeoLocusV2 locus = add("L=LocusV2(X,E)");
		add("h=Line(Q,xAxis)");
		add("D=Intersect(c,h,1)");
		getKernel().updateConstruction(false);
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int traced = builder.addWithDriverSubstitution(lookup("X"), lookup("E"), lookup("D"));
		ConstructiveCone cone = builder.build();

		Record record = Cu2CertificateTransfer.membership(locus, cone, traced,
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1));

		assertFalse(record.isTransferred(),
				"the line through the driver is parallel to the axis there: " + record);
		assertTrue(record.getCause() == Cause.COMPONENT_BOUNDARY_UNDECIDED
				|| record.getCause() == Cause.NOT_IN_CERTIFIED_COMPONENT
				|| record.getCause() == Cause.OPEN_MARGIN_INSUFFICIENT, record.toString());
	}

	@Test
	void aConstantWithoutARigorousEnclosureLeavesTheTransferNotEstablished() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("E=Point(c)");
		place("E", 1.2, 1.6);
		add("P=(0.5,1)");
		add("Q=P+(0,0.25)");
		assertEquals(ConstructiveOperation.BOUNDARY_POINT,
				ConstructiveClassTable.classify(lookup("Q")).getOperation());
		add("f=Line(E,Q)");
		add("X=Intersect(f,xAxis)");
		GeoLocusV2 locus = add("L=LocusV2(X,E)");
		add("D=Intersect(c,yAxis,2)");
		getKernel().updateConstruction(false);
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int traced = builder.addWithDriverSubstitution(lookup("X"), lookup("E"), lookup("D"));
		ConstructiveCone cone = builder.build();

		Record record = Cu2CertificateTransfer.membership(locus, cone, traced,
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1));

		assertFalse(record.isTransferred());
		assertEquals(Cause.OPEN_MARGIN_INSUFFICIENT, record.getCause(),
				"an error estimate never replaces the enclosure of an ideal value: " + record);
	}

	@Test
	void aHullAcrossADecisionBoundaryKeepsNoStrictSign() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("E=Point(c)");
		place("E", 1.2, 1.6);
		add("Q=(0.5,2)");
		add("f=Line(E,Q)");
		add("X=Intersect(f,xAxis)");
		add("D=(0,2)");
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int traced = builder.addWithDriverSubstitution(lookup("X"), lookup("E"), lookup("D"));
		ConstructiveCone cone = builder.build();
		Map<Integer, Object> hulls = new HashMap<>();
		hulls.put(cone.nodeOf(lookup("Q")), new ConstructiveConeEvaluator.Point<>(
				OutwardInterval.point(0.5), OutwardInterval.of(Math.nextDown(2.0),
						Math.nextUp(2.0))));
		hulls.put(cone.getReplacementNode(), new ConstructiveConeEvaluator.Point<>(
				OutwardInterval.point(0), OutwardInterval.point(2)));

		ConstructiveConeEvaluator.Result result = new ConstructiveConeEvaluator<>(cone,
				ConstructiveArithmetic.intervals(), false, Collections.emptyMap(), hulls)
						.evaluate(traced);

		assertEquals(ConstructiveConeEvaluator.Failure.UNDECIDED, result.getFailure(),
				"V-AUT-TRF-1: one ulp across the boundary of a decision keeps no strict sign");
	}

	@Test
	void aChangedSelectionStateIsNotTheProgramOfTheSlice() {
		buildAuthorReplica();
		GeoLocusV2 locus = (GeoLocusV2) geo("e_1");
		CertifiedConstructionProgram2D program = ConstructionBoxModel2D.capture(
				locus.getSemanticDefinition()).getProgram();
		List<Step> steps = new ArrayList<>(program.getSteps());
		int changed = -1;
		for (int index = 0; index < steps.size() && changed < 0; index++) {
			Step step = steps.get(index);
			if (step.getOperation() == Operation.LINE_CIRCLE) {
				boolean compaction = step.getParameter(0) == 1;
				steps.set(index, new Step(Operation.LINE_CIRCLE, step.getOperands(),
						compaction ? 0 : 1, compaction ? 1 : step.getParameter(1),
						compaction ? 0 : step.getParameter(2), step.getParameter(3),
						step.getParameter(4)));
				changed = index;
			}
		}
		double[] constants = new double[8];
		for (int index = 0; index < constants.length; index++) {
			constants[index] = program.getDriverConstant(index);
		}
		CertifiedConstructionProgram2D other = new CertifiedConstructionProgram2D(
				program.getDriver(), constants, program.getDomain(), steps);
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int traced = builder.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("K"));
		ConstructiveCone cone = builder.build();

		assertTrue(changed >= 0, "the slice of e_1 has a line-circle step");
		assertTrue(Cu2ProgramCorrespondence.match(program, cone, traced).isDemonstrated());
		Cu2ProgramCorrespondence mismatch = Cu2ProgramCorrespondence.match(other, cone, traced);
		assertFalse(mismatch.isDemonstrated(), "V-AUT-TRF-4: another selection state");
		assertTrue(mismatch.getFailure().contains("selection"), mismatch.getFailure());
	}

	@Test
	void anExhaustedBudgetIsATypedFailureAndTheSourceCertificateIsIntact() {
		buildAuthorReplica();
		GeoLocusV2 locus = (GeoLocusV2) geo("e_1");
		AlgoDependentPointLocusV2 algorithm =
				(AlgoDependentPointLocusV2) locus.getParentAlgorithm();
		HybridExistenceCertifier2D.Result before = algorithm.getHybridExistenceCertificate();
		String structure = before.getStructure().getSemanticSignature();
		String policy = before.getPolicySignature();
		String fingerprint = algorithm.getSliceFingerprint();
		String program = ConstructionBoxModel2D.capture(locus.getSemanticDefinition())
				.getProgram().getSignature();
		String xml = getApp().getXML();
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int traced = builder.addWithDriverSubstitution(geo("J_1"), geo("E_1"), geo("K"));
		ConstructiveCone cone = builder.build();

		Record exhausted = Cu2CertificateTransfer.membership(locus, cone, traced,
				ConstructiveConeEvaluator.exact(cone,
						ExactEvaluationBudget.restrictedTo(5, 8, 4096)));
		Record transferred = Cu2CertificateTransfer.membership(locus, cone, traced,
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1));

		assertEquals(Cause.BUDGET_EXHAUSTED, exhausted.getCause(), "V-AUT-TRF-5");
		assertTrue(transferred.isTransferred());
		HybridExistenceCertifier2D.Result after = algorithm.getHybridExistenceCertificate();
		assertEquals(structure, after.getStructure().getSemanticSignature(), "V-AUT-TRF-6");
		assertEquals(policy, after.getPolicySignature());
		assertEquals(fingerprint, algorithm.getSliceFingerprint());
		assertEquals(program, ConstructionBoxModel2D.capture(locus.getSemanticDefinition())
				.getProgram().getSignature());
		assertEquals(xml, getApp().getXML());
	}

	@Test
	void anotherSliceOrNoSubstitutionIsNotTheCapturedProgram() {
		buildAuthorReplica();
		ConstructiveCone.Builder other = ConstructiveCone.builder(getConstruction());
		int traced = other.addWithDriverSubstitution(geo("K_1"), geo("E_1"), geo("K"));
		ConstructiveCone cone = other.build();
		ConstructiveConeEvaluator<Element> exact =
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1);

		Record mismatch = Cu2CertificateTransfer.membership((GeoLocusV2) geo("e_1"), cone,
				traced, exact);

		assertEquals(Cause.CORRESPONDENCE_NOT_DEMONSTRATED, mismatch.getCause(),
				"the slice of o_1 is not the program of e_1: " + mismatch);
		ConstructiveCone.Builder plain = ConstructiveCone.builder(getConstruction());
		int node = plain.add(geo("J_1"));
		ConstructiveCone unsubstituted = plain.build();
		Record refused = Cu2CertificateTransfer.membership((GeoLocusV2) geo("e_1"),
				unsubstituted, node, ConstructiveConeEvaluator.exact(unsubstituted,
						ExactEvaluationBudget.V1));
		assertEquals(Cause.CORRESPONDENCE_NOT_DEMONSTRATED, refused.getCause());
	}

	@Test
	void aFoldWithConstructibleSymmetryPremisesTransfersByStructure() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("P=Point(c)");
		add("k=Circle(O,3)");
		add("v=PerpendicularLine(P,xAxis)");
		add("Q=Intersect(k,v,1)");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		GeoElementND[] ends = getElements("Intersect(c,xAxis)");
		add("Dt=Intersect(c,yAxis,2)");
		// A new line-circle intersection settles its selection state at its next computation.
		getKernel().updateConstruction(false);
		String structure = signature(locus);

		for (GeoElementND end : ends) {
			Record fold = closed(locus, "Q", "P", "c", end.getLabelSimple(),
					ClosedDecision.FOLD);
			assertTrue(fold.isTransferred(), "V-AUT-TRF-2: " + fold);
			assertEquals(Route.STRUCTURE, fold.getRoute());
			assertTrue(fold.getPremises().stream().anyMatch(p -> p.startsWith("structure: step")),
					"the perpendicular to the axis reads the driver symmetrically");
			assertTrue(fold.getPremises().stream().anyMatch(p -> p.startsWith("margin: x''")),
					"x'' = -2 cos t keeps its sign on the isolation band: " + fold.getPremises());
		}
		assertEquals(Cause.DECISION_NOT_IN_CERTIFICATE, closed(locus, "Q", "P", "c", "Dt",
				ClosedDecision.FOLD).getCause(), "no fold at pi/2");
		assertEquals(structure, signature(locus), "V-AUT-TRF-6");
	}

	@Test
	void aFoldWhosePremiseReadsACutTransfersByAConstructiveIdentityCertificate() {
		buildAuthorReplica();

		for (String point : new String[] {"K", "J"}) {
			Record fold = closed((GeoLocusV2) geo("e_1"), "J_1", "E_1", "p", point,
					ClosedDecision.FOLD);

			assertTrue(fold.isTransferred(), "V-AUT-TRF-2 in E2: the line k through H, a point"
					+ " on the line through the rotation Cr, is parallel to the vertical g for"
					+ " every value of the cut: " + fold);
			assertEquals(Route.STRUCTURE, fold.getRoute());
			assertTrue(fold.getPremises().stream().anyMatch(premise -> premise.startsWith(
					"structure:") && premise.contains(ConstructiveIdentityCertificate.VERSION)),
					fold.getPremises().toString());
		}
	}

	@Test
	void aFoldWhosePremiseIsNoIdentityOfTheCutIsNotTransferred() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("P=Point(c)");
		add("k=Circle(O,3)");
		// The rotation by zero is the identity, but a cut forgets it: the line through W and its
		// rotated point is vertical only for the binary64 values.
		add("W=(5,0)");
		add("S=Rotate((5,1),0°,W)");
		add("ell=Line(W,S)");
		add("v=Line(P,ell)");
		add("Q=Intersect(k,v,1)");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		GeoElementND[] ends = getElements("Intersect(c,xAxis)");
		// A new line-circle intersection settles its selection state at its next computation.
		getKernel().updateConstruction(false);

		Record fold = closed(locus, "Q", "P", "c", ends[0].getLabelSimple(),
				ClosedDecision.FOLD);

		assertEquals(Cause.STRUCTURAL_PREMISE_NOT_STRICT, fold.getCause(), "V-AUT-TRF-2: " + fold);
		assertTrue(fold.getMessage().contains(ConstructiveIdentityCertificate.Status.NOT_AN_IDENTITY
				.name()), fold.getMessage());
	}

	@Test
	void theLocationOfARootIsDecidedAgainExactlyOrRefuted() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("P=Point(c)");
		add("k=Circle(O,1)");
		add("v=PerpendicularLine(P,xAxis)");
		add("Q=Intersect(k,v,1)");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		add("D=Intersect(c,Line((1,0),(1,1)),1)");
		String beside = Double.toString(Math.nextUp(1.0));
		add("Db=Intersect(c,Line((" + beside + ",0),(" + beside + ",1)),1)");
		add("Di=Intersect(c,Line((0.5,0),(0.5,1)),1)");
		String structure = signature(locus);

		Record end = closed(locus, "Q", "P", "c", "D", ClosedDecision.ROOT_LOCATION);
		assertTrue(end.isTransferred(), "V-AUT-TRF-3: the end of the component is exactly at"
				+ " D, where v touches k: " + end);
		assertEquals(Route.REDECISION, end.getRoute());
		Record refuted = closed(locus, "Q", "P", "c", "Db", ClosedDecision.ROOT_LOCATION);
		assertTrue(refuted.isContradicted(), "one ulp away the exact evaluation refutes it: "
				+ refuted);
		assertEquals(Cause.CONTRADICTED_IN_CONSTRUCTIVE_CONFIGURATION, refuted.getCause());
		assertEquals(Cause.DECISION_NOT_IN_CERTIFICATE, closed(locus, "Q", "P", "c", "Di",
				ClosedDecision.ROOT_LOCATION).getCause(), "inside a component");
		for (ClosedDecision other : new ClosedDecision[] {ClosedDecision.MULTIPLICITY,
				ClosedDecision.STURM_COUNT, ClosedDecision.CRITICAL_VALUE}) {
			assertEquals(Cause.CLOSED_DECISION_NOT_REDECIDED, closed(locus, "Q", "P", "c", "D",
					other).getCause(), "V-AUT-TRF-3: no method for " + other);
		}
		assertEquals(structure, signature(locus), "V-AUT-TRF-6");
	}

	@Test
	void aPointComponentIsDecidedAgainOnlyForItsLocation() {
		add("p=Circle((0,0),25)");
		add("P=(-25,50)");
		add("g1=Circle((83,19),40)");
		add("N=Point(g1)");
		add("t=Ray(P,N)");
		getKernel().getAlgoDispatcher().intersectLineConic(new String[] {"Q", "R"},
				(GeoLine) lookup("t"), (GeoConic) lookup("p"));
		GeoLocusV2 locus = add("lq=LocusV2(Q,N)");
		getKernel().updateConstruction(false);
		add("D=Intersect(g1,Line((83,19),(59,-13)),2)");

		Record location = closed(locus, "Q", "N", "g1", "D", ClosedDecision.ROOT_LOCATION);
		assertTrue(location.isTransferred(), "the external tangency is exactly at D: "
				+ location);
		assertTrue(location.getPremises().stream().anyMatch(p -> p.contains("point component")));
		assertEquals(Cause.CLOSED_DECISION_NOT_REDECIDED, closed(locus, "Q", "N", "g1", "D",
				ClosedDecision.MULTIPLICITY).getCause(), "its isolation is another decision");
	}

	@Test
	void aRootLocationThatReadsABoundarySubstituteIsNotDecidedAgain() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("P=Point(c)");
		add("Ok=Rotate(O,pi/6,O)");
		add("k=Circle(Ok,1)");
		add("v=PerpendicularLine(P,xAxis)");
		add("Q=Intersect(k,v,1)");
		GeoLocusV2 locus = add("L=LocusV2(Q,P)");
		add("D=Intersect(c,Line((1,0),(1,1)),1)");

		Record record = closed(locus, "Q", "P", "c", "D", ClosedDecision.ROOT_LOCATION);

		assertEquals(Cause.CLOSED_DECISION_NOT_REDECIDED, record.getCause(),
				"V-AUT-TRF-3: the rotation is no constructible premise: " + record);
	}

	private Record closed(GeoLocusV2 locus, String traced, String driver, String support,
			String point, ClosedDecision decision) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(getConstruction());
		int supportNode = builder.add(geo(support));
		int tracedNode = builder.addWithDriverSubstitution(geo(traced), geo(driver), geo(point));
		ConstructiveCone cone = builder.build();
		return Cu2CertificateTransfer.closedDecision(locus, cone, tracedNode, supportNode,
				ConstructiveConeEvaluator.exact(cone, ExactEvaluationBudget.V1), decision);
	}

	private static String signature(GeoLocusV2 locus) {
		return ((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
				.getHybridExistenceCertificate().getStructure().getSemanticSignature();
	}
}
