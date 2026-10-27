/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.intersection.ExactContactEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionExistenceEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactEvidenceClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactOrder;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetSource2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 increment E3, piece E3.1, on the replica of the author's model under
 * {@code constructive-authority/v1}: the form one double root at an exact point of the conflict
 * rule, decided in {@code Sigma_C} by the constructive identity certificate with the driver at
 * the exact parameter of its circle, with the fold of {@code CU2} transferred by structure (the
 * parity certificate by exact symmetry: {@code h'(t0) = 0} and the order {@code FOLD_CONTACT}).
 * The root of the seam of {@code e_1} and {@code c_1}, published as transverse, is reclassified
 * as the fold contact at {@code pi}; the contacts at {@code t = 0} are class 2 with their order;
 * the legacy policy keeps CU3.4 unchanged; the adoption report names the reclassification
 * before it is applied; Undo restores the legacy result and token; a decision that the
 * certificate does not give, by budget or because the incidence is not an identity, changes
 * nothing; a later revision rechecks the certificate of its cell. Validation matrix rows V-B-2,
 * V-CONF-5, V-AUT-ADOPT-4 and V-AUT-ADOPT-8, and the regressions of G-AUT-8 and G-AUT-10 for
 * the new decisions.
 */
final class ConstructiveExactContactTest extends ConstructiveModelReplicaTest {

	@Test
	void theSeamIsReclassifiedAsTheFoldContactAtPi() {
		activateUndo();
		buildAuthorReplica();
		GeoLocusIntersectionResult seam = add("S=Intersect(e_1,c_1)");
		getApp().storeUndoInfo();
		LocusIntersectionSolution2D legacySeam = at(seam, Math.PI);
		assertEquals(ContactClass.TRANSVERSE_ESTABLISHED,
				legacySeam.getClassification().getContactClass(),
				"V-B-2 under the legacy policy: CU3.4 unchanged");
		String legacyToken = token(legacySeam);
		assertTrue(seam.isPointAdmissible(legacyToken));
		final double[] legacyPoint = point(legacySeam);

		AuthorityAdoptionImpact report = AuthorityPolicyAdoption.report(getConstruction());
		assertEquals(1, report.getEntries().size(), report.toString());
		assertEquals(ConstructiveAdmission.NOT_ESTABLISHED.name(),
				report.getEntries().get(0).getAfter());
		assertEquals(DiagnosticCode.CONTRADICTED_BY_CERTIFIED_EVIDENCE.name(),
				report.getEntries().get(0).getCause(),
				"V-AUT-ADOPT-8: the report names the reclassification before it is applied");

		AuthorityPolicyAdoption.adopt(getConstruction());

		GeoLocusIntersectionResult adopted = (GeoLocusIntersectionResult) geo("S");
		LocusIntersectionSolution2D foldAtPi = at(adopted, Math.PI);
		assertDoubleRoot(foldAtPi, ContactOrder.FOLD_CONTACT);
		assertTrue(adopted.isPointAdmissible(token(foldAtPi)),
				"PRE-G9B-CU3 CU3-B1: the fold contact gets a token anchored at its fold");
		assertFalse(adopted.isPointAdmissible(legacyToken),
				"the transverse token is no longer admissible and is never reassociated");
		assertEquals(legacyPoint[0], point(foldAtPi)[0], 1E-12, "the same point");
		assertEquals(legacyPoint[1], point(foldAtPi)[1], 1E-12, "the same point");
		assertDoubleRoot(at(adopted, 0), ContactOrder.FOLD_CONTACT);
		assertEquals(2, adopted.getIntersectionResult().getFiniteSolutions().size(),
				"V-CONF-5: the seam and the contact at t = 0, nothing else");
		assertTrue(diagnostics(adopted).contains("One solution anchored at the fold anchor"
				+ " (t->-t, pi), ONE_DOUBLE"), diagnostics(adopted));

		getApp().getUndoManager().undo();

		GeoLocusIntersectionResult undone = (GeoLocusIntersectionResult) geo("S");
		LocusIntersectionSolution2D restored = at(undone, Math.PI);
		assertEquals(ContactClass.TRANSVERSE_ESTABLISHED,
				restored.getClassification().getContactClass(), "V-AUT-ADOPT-4");
		assertEquals(legacyToken, token(restored));
		assertTrue(undone.isPointAdmissible(legacyToken));
	}

	@Test
	void theContactsAtZeroAreClassTwoWithTheirOrderOfContact() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());

		assertDoubleRoot(at(add("Rn=Intersect(e_1,n)"), 0), ContactOrder.FOLD_CONTACT);
		assertDoubleRoot(at(add("Rh=Intersect(o_1,h_2)"), 0), ContactOrder.TWO);
	}

	@Test
	void theConstructiveConfigurationDecidesTheContactAtTheExactParameter() {
		buildAuthorReplica();
		ResidualJetSource2D source = ConstructiveResidualJetSource.capture(
				(GeoLocusV2) geo("e_1"), geo("c_1"));
		assertNotNull(source);

		List<ExactContactEvidence2D> atPi = source.exactContacts(Math.PI - 1E-6,
				Math.PI + 1E-6);

		assertEquals(1, atPi.size(), atPi.toString());
		ExactContactEvidence2D contact = atPi.get(0);
		assertEquals("pi", contact.getParameter());
		assertEquals(ContactEvidenceClass.CONTACT_EXISTENCE_EXACT, contact.getEvidence());
		assertEquals(ContactOrder.FOLD_CONTACT, contact.getOrder(), contact.toString());
		assertEquals(-1, contact.getCurvature(), "h'' < 0 at the seam, as in the study");
		assertTrue(contact.getLower() <= Math.PI && Math.nextUp(Math.PI) <= contact.getUpper(),
				"an outward enclosure of pi");
		List<ExactContactEvidence2D> below = source.exactContacts(-Math.PI - 1E-6,
				-Math.PI + 1E-6);
		assertEquals(1, below.size(), "the copy of the parameter at -pi");
		assertEquals("pi", below.get(0).getParameter());
		assertTrue(below.get(0).getUpper() <= -Math.PI);
		assertTrue(source.exactContacts(1, 1.2).isEmpty(), "no exact parameter in the box");
		assertTrue(source.exactContacts(-Math.PI / 2 - 1E-6, -Math.PI / 2 + 1E-6).isEmpty(),
				"F(-pi/2) is not on c_1: no identity, no contact");
	}

	@Test
	void aDecisionThatTheCertificateDoesNotGiveChangesNothing() {
		buildAuthorReplica();
		ResidualJetSource2D starved = ConstructiveResidualJetSource.capture(
				(GeoLocusV2) geo("e_1"), geo("c_1"), new ExactContactMemory(),
				ConstructiveIdentityBudget.restrictedTo(1_000, 16, 200_000, 4_096));
		assertTrue(starved.exactContacts(Math.PI - 1E-6, Math.PI + 1E-6).isEmpty(),
				"G-AUT-8: an exhausted budget decides nothing");
		add("D_2=(8.871653198789899,7.82698977806856)");
		add("c_2=Circle(H,D_2)");
		ResidualJetSource2D apart = ConstructiveResidualJetSource.capture(
				(GeoLocusV2) geo("e_1"), geo("c_2"));
		assertTrue(apart.exactContacts(Math.PI - 1E-6, Math.PI + 1E-6).isEmpty(),
				"a circle that misses F(pi) by its binary64 radius: not an identity");
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoLocusIntersectionResult near = add("S2=Intersect(e_1,c_2)");
		for (LocusIntersectionSolution2D solution : near.getIntersectionResult()
				.getFiniteSolutions()) {
			assertFalse(evidence(solution).getGrade() == ExistenceGrade.EXISTENCE_EXACT,
					"V-CONF-4: no exact existence without the identity");
		}
	}

	@Test
	void aLaterRevisionRechecksTheCertificateOfItsCell() {
		buildAuthorReplica();
		ExactContactMemory memory = new ExactContactMemory();
		assertEquals(1, ConstructiveResidualJetSource.capture((GeoLocusV2) geo("e_1"),
				geo("c_1"), memory, ConstructiveIdentityBudget.V1).exactContacts(
						Math.PI - 1E-6, Math.PI + 1E-6).size());
		ConstructiveIdentityCertificate certificate = memory.certificate(2);
		assertNotNull(certificate);
		assertTrue(certificate.isIdentityCertified(), certificate.toString());
		GeoPoint h = (GeoPoint) geo("H");
		place("H", h.getInhomX() + 1E-3, h.getInhomY() + 1E-3 * slope("j"));

		assertEquals(1, ConstructiveResidualJetSource.capture((GeoLocusV2) geo("e_1"),
				geo("c_1"), memory, ConstructiveIdentityBudget.V1).exactContacts(
						Math.PI - 1E-6, Math.PI + 1E-6).size());
		assertSame(certificate, memory.certificate(2),
				"G-AUT-10: the same proof within its cell, without symbolic work");
	}

	// ---------------------------------------------------------------- helpers

	private double slope(String line) {
		org.geogebra.common.kernel.geos.GeoLine geo =
				(org.geogebra.common.kernel.geos.GeoLine) geo(line);
		return -geo.getX() / geo.getY();
	}

	private static void assertDoubleRoot(LocusIntersectionSolution2D solution,
			ContactOrder order) {
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				solution.getClassification().getContactClass(), solution.toString());
		assertEquals(OptionalInt.of(2), solution.getClassification().getEstablishedMultiplicity(),
				solution.toString());
		IntersectionExistenceEvidence2D evidence = evidence(solution);
		assertEquals(ConstructiveResidualJetSource.CONFIGURATION, evidence.getConfiguration());
		assertEquals(ExistenceGrade.EXISTENCE_EXACT, evidence.getGrade(), evidence.toString());
		assertEquals(ContactEvidenceClass.CONTACT_EXISTENCE_EXACT, evidence.getContactEvidence());
		assertEquals(order, evidence.getContactOrder(), evidence.toString());
		boolean anchored = solution.getRevisionEvidence().getCurrentRootGerm()
				.filter(germ -> germ.startsWith("pre-g9b-cu3/fold-contact-germ/v1/")).isPresent();
		assertEquals(anchored ? ConstructiveAdmission.STRICT
				: ConstructiveAdmission.NOT_ESTABLISHED,
				evidence.getConstructiveAdmission(), "PRE-G9B-CU3 CU3-B1: a contact is admitted"
						+ " for point consumption only when it is anchored at a certified fold");
	}

	private static IntersectionExistenceEvidence2D evidence(LocusIntersectionSolution2D solution) {
		return solution.getRevisionEvidence().getExistenceEvidence();
	}

	private static String token(LocusIntersectionSolution2D solution) {
		return solution.getIdentity().getRootToken();
	}

	private static double[] point(LocusIntersectionSolution2D solution) {
		return new double[] {solution.getEvaluatedPoint().getX(),
				solution.getEvaluatedPoint().getY()};
	}

	/** @return the solution at the parameter, modulo the period at the seam */
	private static LocusIntersectionSolution2D at(GeoLocusIntersectionResult result,
			double parameter) {
		List<String> seen = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getIntersectionResult()
				.getFiniteSolutions()) {
			double value = solution.getRevisionEvidence().getSemanticParameter();
			seen.add(String.valueOf(value));
			if (parameter == 0 ? Math.abs(value) < 1E-12
					: Math.abs(Math.abs(value) - Math.abs(parameter)) < 1E-12) {
				return solution;
			}
		}
		throw new AssertionError("no solution at " + parameter + " in " + seen);
	}

	private static String diagnostics(GeoLocusIntersectionResult result) {
		StringBuilder text = new StringBuilder();
		for (IntersectionDiagnostic2D diagnostic : result.getIntersectionResult()
				.getDiagnostics()) {
			text.append(diagnostic.getCode()).append(": ").append(diagnostic.getMessage())
					.append('\n');
		}
		return text.toString();
	}
}
