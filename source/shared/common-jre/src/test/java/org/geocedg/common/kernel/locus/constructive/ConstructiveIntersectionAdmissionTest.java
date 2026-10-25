/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionExistenceEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactEvidenceClass;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetEnclosure2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetSource2D;
import org.junit.jupiter.api.Test;

/**
 * The public one-parameter intersection under {@code constructive-authority/v1}
 * ({@code locus-v2-intersections.md}, constructive authority amendment;
 * {@code locus-v2-constructive-authority.md} section 7.4): the residual of the constructive
 * configuration in interval jets, the conflict rule decided there, the re-qualification of the
 * roots G8B-R1 admits, reported by the adoption event before it is applied, and the fold contacts
 * of the author's model, which a residual bound never relaxes. Validation matrix rows
 * V-AUT-ADOPT-3, V-AUT-ADOPT-4 and V-AUT-ADOPT-8, V-B-1 and V-B-2 without M3 under both
 * policies, and V-CONF-1 and V-CONF-3 in the constructive configuration.
 */
final class ConstructiveIntersectionAdmissionTest extends ConstructiveModelReplicaTest {

	@Test
	void theRootsOfAConstructiveSliceAreRequalifiedStrictOnceAtTheAdoption() {
		activateUndo();
		constructiveCircle();
		add("g=Line((0,0.5),(1,0.5))");
		GeoLocusIntersectionResult result = add("R=Intersect(L,g)");
		getApp().storeUndoInfo();
		List<String> tokens = tokens(result);
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			assertNull(evidence(solution).getConstructiveAdmission(),
					"the legacy policy has no re-qualification");
		}

		AuthorityAdoptionImpact impact = AuthorityPolicyAdoption.adopt(getConstruction());

		assertEquals(2, impact.getEntries().size(), "V-AUT-ADOPT-8: each admitted root, once");
		for (AuthorityAdoptionImpact.Entry entry : impact.getEntries()) {
			assertEquals(IntersectionAdmissionImpact.LEGACY_ADMISSION, entry.getBefore());
			assertEquals(ConstructiveAdmission.STRICT.name(), entry.getAfter());
			assertEquals(RelationIntegrityRecord.Evidence.CERTIFIED_ENCLOSURE.name(),
					entry.getCause());
		}
		GeoLocusIntersectionResult adopted = (GeoLocusIntersectionResult) lookup("R");
		assertEquals(tokens, tokens(adopted), "V-AUT-ADOPT-3: no token changes");
		for (LocusIntersectionSolution2D solution : rich(adopted).getFiniteSolutions()) {
			IntersectionExistenceEvidence2D evidence = evidence(solution);
			assertEquals(ConstructiveResidualJetSource.CONFIGURATION, evidence.getConfiguration());
			assertEquals(ExistenceGrade.EXISTENCE_CERTIFIED, evidence.getGrade());
			assertEquals(ConstructiveAdmission.STRICT, evidence.getConstructiveAdmission());
			assertTrue(adopted.isPointAdmissible(solution.getIdentity().getRootToken()));
		}

		getApp().getUndoManager().undo();

		GeoLocusIntersectionResult undone = (GeoLocusIntersectionResult) lookup("R");
		assertFalse(AuthorityPolicyAdoption.isConstructive(getConstruction()));
		assertEquals(tokens, tokens(undone), "V-AUT-ADOPT-4");
		for (LocusIntersectionSolution2D solution : rich(undone).getFiniteSolutions()) {
			assertNull(evidence(solution).getConstructiveAdmission());
			assertTrue(undone.isPointAdmissible(solution.getIdentity().getRootToken()));
		}
	}

	@Test
	void aSliceOutsideTheConstructiveClassIsNotAdmittedAfterTheAdoption() {
		add("O=(0,0)");
		add("c=Circle(O,1)");
		add("P=Point(c)");
		add("Q=Dilate(P,1.5,O)");
		add("L=LocusV2(Q,P)");
		add("g=Line((0,0.75),(1,0.75))");
		GeoLocusIntersectionResult result = add("R=Intersect(L,g)");
		assertEquals(2, admissible(result), "G8B-R1 admits both roots under the legacy policy");

		AuthorityAdoptionImpact report = AuthorityPolicyAdoption.report(getConstruction());
		assertEquals(2, report.getEntries().size());
		for (AuthorityAdoptionImpact.Entry entry : report.getEntries()) {
			assertEquals(ConstructiveAdmission.NOT_ESTABLISHED.name(), entry.getAfter());
			assertEquals("NO_CONSTRUCTIVE_ENCLOSURE", entry.getCause(),
					"Dilate is outside constructive-class/v1");
		}
		AuthorityPolicyAdoption.adopt(getConstruction());

		GeoLocusIntersectionResult adopted = (GeoLocusIntersectionResult) lookup("R");
		assertEquals(2, rich(adopted).getFiniteSolutions().size(), "the roots stay in the result");
		assertEquals(0, admissible(adopted),
				"V-AUT-ADOPT-8: no admission is inherited without justification");
		assertNotNull(diagnostic(rich(adopted),
				DiagnosticCode.CONSTRUCTIVE_ADMISSION_NOT_ESTABLISHED));
		for (LocusIntersectionSolution2D solution : rich(adopted).getFiniteSolutions()) {
			assertEquals(ConstructiveAdmission.NOT_ESTABLISHED,
					evidence(solution).getConstructiveAdmission());
		}
	}

	@Test
	void theConflictRuleDecidesInTheConstructiveConfiguration() {
		constructiveCircle();
		add("k=Line((0,1.0000000000001),(1,1.0000000000001))");
		add("m=Line((0,0.9999999999999),(1,0.9999999999999))");
		AuthorityPolicyAdoption.adopt(getConstruction());

		LocusIntersectionResult2D absent = rich(add("Rk=Intersect(L,k)"));
		GeoLocusIntersectionResult near = add("Rm=Intersect(L,m)");

		assertTrue(absent.getFiniteSolutions().isEmpty(), "V-CONF-1 in Sigma_C");
		assertTrue(diagnostic(absent, DiagnosticCode.CONTRADICTED_BY_CERTIFIED_EVIDENCE)
				.getMessage().contains("CRITICAL_POINT_CERTIFIED_WITHOUT_INTERSECTION"));
		assertEquals(2, rich(near).getFiniteSolutions().size(), "V-CONF-3 in Sigma_C");
		assertEquals(2, admissible(near));
		for (LocusIntersectionSolution2D solution : rich(near).getFiniteSolutions()) {
			assertEquals(ConstructiveResidualJetSource.CONFIGURATION,
					evidence(solution).getConfiguration());
			assertEquals(ConstructiveAdmission.STRICT,
					evidence(solution).getConstructiveAdmission());
		}
	}

	@Test
	void theFoldContactsOfTheModelAreNeverRelaxedByAResidualBound() {
		buildAuthorReplica();
		GeoLocusIntersectionResult seam = add("S=Intersect(e_1,c_1)");
		LocusIntersectionSolution2D legacySeam = transverse(seam);
		assertTrue(seam.isPointAdmissible(legacySeam.getIdentity().getRootToken()),
				"V-B-2 without M3: G8B-R1 keeps the seam root under the legacy policy");
		assertEquals(ContactEvidenceClass.CONTACT_NUMERICALLY_COMPATIBLE,
				contactAtZero(seam), "V-B-1: the contact at t = 0 is class 3");
		ResidualJetSource2D source = ConstructiveResidualJetSource.capture(
				(GeoLocusV2) geo("e_1"), geo("c_1"));
		assertNotNull(source, "the slice of e_1 is enclosed in interval jets");
		ResidualJetEnclosure2D fold = source.enclose(Math.PI - 1E-6, Math.PI + 1E-6);
		assertEquals(-1, fold.sign(2), "h'' has a fixed sign at the fold of the seam");
		assertEquals(ResidualJetEnclosure2D.UNDECIDED, fold.sign(1));

		AuthorityAdoptionImpact report = AuthorityPolicyAdoption.report(getConstruction());
		assertEquals(1, report.getEntries().size());
		assertEquals(ConstructiveAdmission.NOT_ESTABLISHED.name(),
				report.getEntries().get(0).getAfter(), "the seam is a fold contact");
		AuthorityPolicyAdoption.adopt(getConstruction());

		GeoLocusIntersectionResult adopted = (GeoLocusIntersectionResult) geo("S");
		LocusIntersectionSolution2D constructiveSeam = transverse(adopted);
		assertEquals(ConstructiveAdmission.NOT_ESTABLISHED,
				evidence(constructiveSeam).getConstructiveAdmission());
		assertFalse(adopted.isPointAdmissible(constructiveSeam.getIdentity().getRootToken()),
				"a contact without a sign change is never admitted by a residual bound");
		assertEquals(ContactEvidenceClass.CONTACT_NUMERICALLY_COMPATIBLE, contactAtZero(adopted),
				"V-B-1 under the constructive authority: class 3, until the exact decision of E2");
	}

	// ---------------------------------------------------------------- helpers

	/** A circle of radius 1 and centre (1, 0) traced by a midpoint, inside the class. */
	private void constructiveCircle() {
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("P=Point(c)");
		add("A=(2,0)");
		add("Q=Midpoint(P,A)");
		add("L=LocusV2(Q,P)");
	}

	private static LocusIntersectionResult2D rich(GeoLocusIntersectionResult result) {
		assertNotNull(result);
		return result.getIntersectionResult();
	}

	private static IntersectionExistenceEvidence2D evidence(LocusIntersectionSolution2D solution) {
		return solution.getRevisionEvidence().getExistenceEvidence();
	}

	private static int admissible(GeoLocusIntersectionResult result) {
		int count = 0;
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			count += result.isPointAdmissible(solution.getIdentity().getRootToken()) ? 1 : 0;
		}
		return count;
	}

	private static List<String> tokens(GeoLocusIntersectionResult result) {
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			tokens.add(solution.getIdentity().getRootToken());
		}
		return tokens;
	}

	private static LocusIntersectionSolution2D transverse(GeoLocusIntersectionResult result) {
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			if (Math.abs(Math.abs(solution.getRevisionEvidence().getSemanticParameter())
					- Math.PI) < 1E-9) {
				return solution;
			}
		}
		throw new AssertionError("no solution at the seam");
	}

	private static ContactEvidenceClass contactAtZero(GeoLocusIntersectionResult result) {
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			if (solution.getRevisionEvidence().getSemanticParameter() == 0) {
				return evidence(solution).getContactEvidence();
			}
		}
		throw new AssertionError("no candidate at t = 0");
	}

	private static IntersectionDiagnostic2D diagnostic(LocusIntersectionResult2D result,
			DiagnosticCode code) {
		for (IntersectionDiagnostic2D diagnostic : result.getDiagnostics()) {
			if (diagnostic.getCode() == code) {
				return diagnostic;
			}
		}
		throw new AssertionError("no " + code + " in " + result.getDiagnostics());
	}
}
