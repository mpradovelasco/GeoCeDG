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
import java.util.List;
import java.util.OptionalInt;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.intersection.CertifiedAdmissionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionExistenceEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactEvidenceClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactOrder;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 T7-a on the replica of the author's model: the early exact decision of
 * {@code cu3-certified-admission/v2} in the published solutions ({@code locus-v2-intersections.md},
 * constructive admission policy v2 amendment; Gate N record). At {@code rbCone = 7.5} the contact
 * of {@code o_1} and {@code h_2} at {@code t = 0}, which version 1 left without a certificate at
 * its work limit, is the one double root of order two, with the evidence and the diagnostics of
 * version 2 and its admissibility unchanged; the decision follows the configuration through its
 * changes, Undo, Redo and a reopened document, never persisted; and a document under the legacy
 * policy keeps version 1 everywhere.
 */
final class ConstructiveEarlyExactDecisionTest extends ConstructiveModelReplicaTest {

	private static final String[] PAIRS = {"Rn=Intersect(e_1,n)", "Rc=Intersect(e_1,c_1)",
			"Rr=Intersect(e_1,r)", "Rh=Intersect(o_1,h_2)"};

	@Test
	void theContactAtTheWorkLimitOfVersionOneIsTheDoubleRoot() {
		buildAuthorReplica();
		setNumber("rbCone", 7.5);
		GeoLocusIntersectionResult legacy = add("Rh=Intersect(o_1,h_2)");
		for (LocusIntersectionSolution2D solution : legacy.getIntersectionResult()
				.getFiniteSolutions()) {
			assertEquals(CertifiedAdmissionPolicy2D.VERSION, evidence(solution).getPolicyVersion());
		}

		AuthorityPolicyAdoption.adopt(getConstruction());

		GeoLocusIntersectionResult adopted = (GeoLocusIntersectionResult) geo("Rh");
		LocusIntersectionSolution2D contact = at(adopted, 0);
		assertDoubleRoot(contact);
		assertTrue(evidence(contact).getCertificate().startsWith("ONE_DOUBLE"),
				evidence(contact).getCertificate());
		assertFalse(adopted.isPointAdmissible(token(contact)), "a contact is not admitted for"
				+ " point consumption before CU3-B");
		assertTrue(contactDiagnostics(contact).contains("Root-count certificate of "
				+ CertifiedAdmissionPolicy2D.CONSTRUCTIVE_VERSION + ": one double root"),
				contactDiagnostics(contact));
	}

	@Test
	void theDecisionFollowsTheConfigurationThroughUndoRedoAndReopen() {
		activateUndo();
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("Rh=Intersect(o_1,h_2)");
		getApp().storeUndoInfo();
		assertDoubleRoot(at((GeoLocusIntersectionResult) geo("Rh"), 0));
		setNumber("rbCone", 7.5);
		getApp().storeUndoInfo();
		assertDoubleRoot(at((GeoLocusIntersectionResult) geo("Rh"), 0));

		getApp().getUndoManager().undo();
		assertEquals(5, ((GeoNumeric) geo("rbCone")).getValue(), 0);
		assertDoubleRoot(at((GeoLocusIntersectionResult) geo("Rh"), 0));
		getApp().getUndoManager().redo();
		assertEquals(7.5, ((GeoNumeric) geo("rbCone")).getValue(), 0);
		assertDoubleRoot(at((GeoLocusIntersectionResult) geo("Rh"), 0));

		String xml = getApp().getXML();
		assertFalse(xml.contains("cu3-certified-admission"), "the policy version is never"
				+ " persisted");
		assertFalse(xml.contains("ONE_DOUBLE"), "nor any certificate");
		reload();
		assertDoubleRoot(at((GeoLocusIntersectionResult) geo("Rh"), 0));
	}

	@Test
	void everyExactExistenceIsADoubleRootUnderVersionTwoInEveryConfiguration() {
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		for (String pair : PAIRS) {
			add(pair);
		}
		GeoPoint h = (GeoPoint) geo("H");
		double t = h.getPathParameter().getT();
		GeoPoint d1 = (GeoPoint) geo("D_1");
		double x = d1.getInhomX();
		double y = d1.getInhomY();
		List<String> seen = new ArrayList<>();
		for (String configuration : new String[] {"base", "H +40", "D_1 x3", "rbCone 3",
				"rbCone 7.5", "restored"}) {
			switch (configuration) {
			case "H +40":
				moveOnPath("H", t + 40);
				break;
			case "D_1 x3":
				moveOnPath("H", t);
				place("D_1", h.getInhomX() + 3 * (x - h.getInhomX()),
						h.getInhomY() + 3 * (y - h.getInhomY()));
				break;
			case "rbCone 3":
				place("D_1", x, y);
				setNumber("rbCone", 3);
				break;
			case "rbCone 7.5":
				setNumber("rbCone", 7.5);
				break;
			case "restored":
				setNumber("rbCone", 5);
				break;
			default:
				break;
			}
			for (String pair : PAIRS) {
				GeoLocusIntersectionResult result = (GeoLocusIntersectionResult) geo(
						pair.substring(0, 2));
				for (LocusIntersectionSolution2D solution : result.getIntersectionResult()
						.getFiniteSolutions()) {
					IntersectionExistenceEvidence2D evidence = evidence(solution);
					assertEquals(CertifiedAdmissionPolicy2D.CONSTRUCTIVE_VERSION,
							evidence.getPolicyVersion(), configuration + " " + pair);
					if (evidence.getGrade() == ExistenceGrade.EXISTENCE_EXACT) {
						assertDoubleRoot(solution, evidence.getContactOrder());
						seen.add(configuration + " " + pair);
					}
				}
			}
		}
		assertTrue(seen.contains("rbCone 7.5 Rh=Intersect(o_1,h_2)"), seen.toString());
		assertTrue(seen.contains("base Rh=Intersect(o_1,h_2)"), seen.toString());
	}

	@Test
	void aLegacyDocumentKeepsVersionOne() {
		buildAuthorReplica();
		setNumber("rbCone", 7.5);
		for (String pair : PAIRS) {
			GeoLocusIntersectionResult result = add(pair);
			for (LocusIntersectionSolution2D solution : result.getIntersectionResult()
					.getFiniteSolutions()) {
				IntersectionExistenceEvidence2D evidence = evidence(solution);
				assertEquals(CertifiedAdmissionPolicy2D.VERSION, evidence.getPolicyVersion(),
						pair);
				assertNotEquals(ExistenceGrade.EXISTENCE_EXACT, evidence.getGrade(),
						"the captured configuration decides nothing exactly");
			}
			assertFalse(diagnostics(result).contains(
					CertifiedAdmissionPolicy2D.CONSTRUCTIVE_VERSION), diagnostics(result));
		}
	}

	// ---------------------------------------------------------------- helpers

	private void setNumber(String label, double value) {
		GeoNumeric number = (GeoNumeric) geo(label);
		number.setValue(value);
		number.updateCascade();
	}

	private void moveOnPath(String label, double t) {
		GeoPoint point = (GeoPoint) geo(label);
		point.getPathParameter().setT(t);
		((Path) point.getPath()).pathChanged(point);
		point.updateCoords();
		point.updateCascade();
	}

	private static void assertDoubleRoot(LocusIntersectionSolution2D solution) {
		assertDoubleRoot(solution, ContactOrder.TWO);
	}

	private static void assertDoubleRoot(LocusIntersectionSolution2D solution,
			ContactOrder order) {
		assertEquals(ContactClass.TANGENT_ESTABLISHED,
				solution.getClassification().getContactClass(), solution.toString());
		assertEquals(OptionalInt.of(2), solution.getClassification().getEstablishedMultiplicity(),
				solution.toString());
		IntersectionExistenceEvidence2D evidence = evidence(solution);
		assertEquals(ConstructiveResidualJetSource.CONFIGURATION, evidence.getConfiguration());
		assertEquals(CertifiedAdmissionPolicy2D.CONSTRUCTIVE_VERSION, evidence.getPolicyVersion());
		assertEquals(ExistenceGrade.EXISTENCE_EXACT, evidence.getGrade(), evidence.toString());
		assertEquals(ContactEvidenceClass.CONTACT_EXISTENCE_EXACT, evidence.getContactEvidence());
		assertEquals(order, evidence.getContactOrder(), evidence.toString());
		// PRE-G9B-CU3 CU3-B1: only a contact anchored at a certified fold is admitted.
		boolean anchored = solution.getRevisionEvidence().getCurrentRootGerm()
				.filter(germ -> germ.startsWith("pre-g9b-cu3/fold-contact-germ/v1/")).isPresent();
		assertEquals(anchored ? ConstructiveAdmission.STRICT
				: ConstructiveAdmission.NOT_ESTABLISHED, evidence.getConstructiveAdmission());
	}

	private static IntersectionExistenceEvidence2D evidence(LocusIntersectionSolution2D solution) {
		return solution.getRevisionEvidence().getExistenceEvidence();
	}

	private static String token(LocusIntersectionSolution2D solution) {
		return solution.getIdentity().getRootToken();
	}

	private static LocusIntersectionSolution2D at(GeoLocusIntersectionResult result,
			double parameter) {
		List<String> seen = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getIntersectionResult()
				.getFiniteSolutions()) {
			double value = solution.getRevisionEvidence().getSemanticParameter();
			seen.add(String.valueOf(value));
			if (Math.abs(value - parameter) < 1E-12) {
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

	private static String contactDiagnostics(LocusIntersectionSolution2D solution) {
		StringBuilder text = new StringBuilder();
		for (IntersectionDiagnostic2D diagnostic : solution.getDiagnostics()) {
			text.append(diagnostic.getMessage()).append('\n');
		}
		return text.toString();
	}
}
