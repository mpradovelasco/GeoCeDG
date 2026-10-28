/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ContactOrder;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 increment {@code CU3-B2}: regular tangent contacts of class 1 or 2 under
 * {@code constructive-authority/v1} (CU3-B2 design sections 2 to 6 and 12; validation matrix
 * rows B2-1 to B2-4, B2-12 and B2-17 to B2-19). The mandatory case is the contact {@code u_1} of
 * {@code o_1} and {@code h_2} in the replica of the author's model, an exact double root of order
 * two at the exact driver parameter 0. Dynamic transitions of a regular tangency are not
 * reachable by moving a target (G-B2-1 record, K1); those rows are covered at the level of the
 * selector in PreG9BCu3RegularTangentGermTest.
 */
final class PreG9BCu3RegularTangentTest extends ConstructiveModelReplicaTest {

	private static final String GERM = "pre-g9b-cu3/regular-tangent-germ/v1/";

	@Test
	void b2x1TheMandatoryContactIsAnchoredAndAdmissible() {
		GeoLocusIntersectionResult result = replica(true);

		List<LocusIntersectionSolution2D> anchored = regular(rich(result));
		assertEquals(1, anchored.size(), rich(result).getDiagnostics().toString());
		LocusIntersectionSolution2D contact = anchored.get(0);
		assertEquals(ExistenceGrade.EXISTENCE_EXACT, contact.getRevisionEvidence()
				.getExistenceEvidence().getGrade(), "class 2");
		assertEquals(ContactOrder.TWO, contact.getRevisionEvidence().getExistenceEvidence()
				.getContactOrder());
		assertEquals(ConstructiveAdmission.STRICT, contact.getRevisionEvidence()
				.getExistenceEvidence().getConstructiveAdmission());
		assertTrue(result.isPointAdmissible(contact.getIdentity().getRootToken()), "B2-1");
		assertTrue(germ(contact).contains("LINE/canonical"), germ(contact));
		assertTrue(germ(contact).endsWith("positive") || germ(contact).endsWith("negative"));
	}

	@Test
	void b2x1TheLegacyPolicyAnchorsNothing() {
		GeoLocusIntersectionResult result = replica(false);

		assertTrue(regular(rich(result)).isEmpty(), "B2-1 adversarial: no anchor in a legacy"
				+ " document");
		assertTrue(admissible(result).isEmpty());
	}

	@Test
	void b2x2to4TheSideAndTheTokenDoNotDependOnTheViewOrARedrawnTarget() {
		GeoLocusIntersectionResult result = replica(true);
		String germ = germ(regular(rich(result)).get(0));
		String token = regular(rich(result)).get(0).getIdentity().getRootToken();

		getApp().getActiveEuclidianView().setCoordSystem(17, -23, 3.5, 0.25);
		getConstruction().updateConstruction(false);
		LocusIntersectionSolution2D again = regular(rich(result())).get(0);
		assertEquals(germ, germ(again), "B2-4: the view never enters the side");
		assertEquals(token, again.getIdentity().getRootToken());
	}

	@Test
	void b2x12And17TheClaimSurvivesReopenUndoAndDuplication() {
		GeoLocusIntersectionResult result = replica(true);
		String token = regular(rich(result)).get(0).getIdentity().getRootToken();
		GeoPoint point = claim(result, token);
		assertTrue(point.isDefined());
		String state = result.getTokenLedgerState();
		assertTrue(state.startsWith("6|"), "B2-12: a regular tangent entry writes ledger v6");
		assertTrue(state.contains("~T,"), "B2-12: with the entry kind T");

		getApp().setXML(getApp().getXML(), true);
		assertEquals(state, result().getTokenLedgerState(), "B2-12 and B2-22: reopen");
		assertEquals(token, regular(rich(result())).get(0).getIdentity().getRootToken());
		assertTrue(((GeoPoint) lookup("X")).isDefined());

		org.geogebra.common.util.InternalClipboard.duplicate(getApp(),
				List.of(result(), lookup("X")));
		GeoLocusIntersectionResult copy = (GeoLocusIntersectionResult) lookup("U_{1}");
		List<LocusIntersectionSolution2D> copied = regular(rich(copy));
		assertEquals(1, copied.size());
		assertNotEquals(token, copied.get(0).getIdentity().getRootToken(),
				"B2-17: fresh closure, no identity is reused");
	}

	@Test
	void b2x12OlderReadersRejectTheEntryKind() {
		GeoLocusIntersectionResult result = replica(true);
		claim(result, regular(rich(result)).get(0).getIdentity().getRootToken());
		String state = result.getTokenLedgerState();

		assertThrows(RuntimeException.class,
				() -> result().restoreTokenLedgerState("5" + state.substring(1)),
				"B2-12: a version 5 reader rejects the regular tangent entry");
	}

	@Test
	void b2x18And19TheFoldContactsAndTheOtherSolutionsAreUnchanged() {
		buildAuthorReplica();
		add("S=Intersect(e_1,c_1)");
		add("U=Intersect(o_1,h_2)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		GeoLocusIntersectionResult folds = (GeoLocusIntersectionResult) lookup("S");
		assertTrue(regular(rich(folds)).isEmpty(), "B2-18: a fold contact is never regular");
		int foldAnchors = 0;
		for (LocusIntersectionSolution2D solution : rich(folds).getFiniteSolutions()) {
			foldAnchors += germ(solution).startsWith("pre-g9b-cu3/fold-contact-germ/v1/") ? 1 : 0;
		}
		assertEquals(2, foldAnchors, "B2-18: the two fold anchors of CU3-B1 stay");
		for (LocusIntersectionSolution2D solution : rich(result()).getFiniteSolutions()) {
			boolean anchored = germ(solution).startsWith(GERM);
			assertEquals(anchored, result().isPointAdmissible(solution.getIdentity()
					.getRootToken()), "B2-19: only the anchored contact is admissible");
		}
	}

	@Test
	void b2x21NoContactOfAFreeTargetIsAnchored() {
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("O=(0,0)");
		add("c=Circle(O,2)");
		add("N=Point(c)");
		add("P0=Rotate(N,pi/2,O)");
		add("lc=LocusV2(P0,N)");
		add("h=Line((0,2),(1,2))");
		GeoLocusIntersectionResult free = add("V=Intersect(lc,h)");
		assertTrue(regular(rich(free)).isEmpty(), "K1: a free target gives class 3, no anchor");
		assertTrue(admissible(free).isEmpty());

		add("V0=(0.001,1)");
		add("X0=Intersect(Line(N,Line((0,0),V0)),Line((0,3),(1,3)))");
		add("ls=LocusV2(X0,N)");
		add("g=Line((2.003,0),(2.003,1))");
		GeoLocusIntersectionResult singular = add("W=Intersect(ls,g)");
		assertTrue(regular(rich(singular)).isEmpty(),
				"B2-21: a turning point of the source is never a regular tangent contact");
		for (LocusIntersectionSolution2D solution : rich(singular).getFiniteSolutions()) {
			assertNotEquals(ContactOrder.TWO, solution.getRevisionEvidence()
					.getExistenceEvidence().getContactOrder());
		}
	}

	// ---------------------------------------------------------------- helpers

	private GeoLocusIntersectionResult replica(boolean adopt) {
		buildAuthorReplica();
		add("U=Intersect(o_1,h_2)");
		if (adopt) {
			AuthorityPolicyAdoption.adopt(getConstruction());
		}
		return result();
	}

	private GeoLocusIntersectionResult result() {
		return (GeoLocusIntersectionResult) lookup("U");
	}

	private GeoPoint claim(GeoLocusIntersectionResult result, String token) {
		return LocusV2PublicOperations.selectIntersectionPoint(getConstruction(), "X", result,
				new GeoText(getConstruction(), token));
	}

	private static LocusIntersectionResult2D rich(GeoLocusIntersectionResult result) {
		return result.getIntersectionResult();
	}

	private static String germ(LocusIntersectionSolution2D solution) {
		return solution.getRevisionEvidence().getCurrentRootGerm().orElse("");
	}

	private static List<LocusIntersectionSolution2D> regular(LocusIntersectionResult2D result) {
		List<LocusIntersectionSolution2D> anchored = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			if (germ(solution).startsWith(GERM)) {
				anchored.add(solution);
			}
		}
		return anchored;
	}

	private static List<String> admissible(GeoLocusIntersectionResult result) {
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			String token = solution.getIdentity().getRootToken();
			if (token != null && result.isPointAdmissible(token)) {
				tokens.add(token);
			}
		}
		assertFalse(tokens.contains(null));
		return tokens;
	}
}
