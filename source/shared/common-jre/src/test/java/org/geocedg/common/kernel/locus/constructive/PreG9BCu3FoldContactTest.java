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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.locus.LocusV2PublicOperations;
import org.geocedg.common.kernel.locus.intersection.IntersectionDiagnostic2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ConstructiveAdmission;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.DiagnosticCode;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.ExistenceGrade;
import org.geocedg.common.kernel.locus.intersection.IntersectionSemanticMetadata2D.IdentityStatus;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoText;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-CU3 increment {@code CU3-B1}: solutions anchored at the certified folds of {@code CU2}
 * under {@code constructive-authority/v1} (CU3-B1 design; validation matrix rows B1-1 to B1-13
 * and B1-15). The folded segment traced by {@code (x(N), 3)} with {@code N} on the circle of radius
 * 2 folds at {@code t = 0} and at the seam; a vertical target near {@code x = 2} or {@code x = -2}
 * crosses it at a pair of preimages {@code t, -t} of one point. The replica of the author's model
 * gives the exact double roots of {@code e_1} and {@code c_1} at both folds.
 */
final class PreG9BCu3FoldContactTest extends ConstructiveModelReplicaTest {

	private static final String GERM = "pre-g9b-cu3/fold-contact-germ/v1/";
	private static final String AT_ZERO = "5:t->-t1:0";
	private static final String AT_PI = "5:t->-t2:pi";

	@Test
	void b1x1APairOfPreimagesNearAFoldHasOneIdentity() {
		GeoLocusIntersectionResult result = segment(1.9999, true);

		List<LocusIntersectionSolution2D> solutions = rich(result).getFiniteSolutions();
		assertEquals(1, solutions.size(), "B1-1: the pair t, -t publishes one solution");
		LocusIntersectionSolution2D anchored = solutions.get(0);
		assertTrue(germ(anchored).endsWith(AT_ZERO), germ(anchored));
		assertTrue(anchored.getRevisionEvidence().getSemanticParameter() > 0,
				"the canonical preimage is the one after the fixed point");
		assertEquals(ExistenceGrade.EXISTENCE_CERTIFIED, anchored.getRevisionEvidence()
				.getExistenceEvidence().getGrade());
		assertEquals(ConstructiveAdmission.STRICT, anchored.getRevisionEvidence()
				.getExistenceEvidence().getConstructiveAdmission());
		assertEquals(2, count(rich(result), DiagnosticCode.SUPERSEDED_BY_CERTIFIED_FOLD_PAIR));
		assertEquals(1, count(rich(result), DiagnosticCode.FOLD_CONTACT_ANCHORED));
		GeoPoint point = claim(result);
		assertEquals(1.9999, point.getInhomX(), 1E-12);
		assertEquals(3, point.getInhomY(), 1E-12);

		move(-1.9999);
		List<LocusIntersectionSolution2D> seam = rich(result()).getFiniteSolutions();
		assertEquals(1, seam.size(), "B1-6: the pair across the seam publishes one solution");
		assertTrue(germ(seam.get(0)).endsWith(AT_PI), "B1-6: the label of the seam is pi");
		assertFalse(point.isDefined(), "the anchor at 0 is not current: its claim is latent");
	}

	@Test
	void b1x2to4TheClaimedAnchorSurvivesAContactWithoutClassAndAnAbsence() {
		GeoLocusIntersectionResult result = segment(1.9999, true);
		final String token = rich(result).getFiniteSolutions().get(0).getIdentity()
				.getRootToken();
		GeoPoint point = claim(result);

		move(2);
		assertTrue(fold(rich(result())).isEmpty(), "B1-2: a contact of class 3 anchors nothing");
		assertTrue(count(rich(result()), DiagnosticCode.IDENTITY_DISCONTINUITY) > 0);
		assertFalse(point.isDefined(), "B1-15: the claimed token stays latent, never replaced");

		move(2.01);
		assertTrue(rich(result()).getFiniteSolutions().isEmpty(), "B1-4: no root");
		assertFalse(point.isDefined());

		move(1.9999);
		LocusIntersectionSolution2D back = fold(rich(result())).get(0);
		assertEquals(token, back.getIdentity().getRootToken(),
				"B1-3/B1-4: the same full selector reactivates the same token");
		assertNotEquals(IdentityStatus.AMBIGUOUS_CONTINUATION,
				back.getIdentity().getIdentityStatus());
		assertTrue(point.isDefined());
		assertEquals(1.9999, point.getInhomX(), 1E-12);
	}

	@Test
	void b1x5and6TheTwoFoldsOfTheReplicaHaveDistinctAnchorsWithoutRank() {
		buildAuthorReplica();
		add("S=Intersect(e_1,c_1)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		GeoLocusIntersectionResult result = (GeoLocusIntersectionResult) lookup("S");
		List<LocusIntersectionSolution2D> anchored = fold(rich(result));
		assertEquals(2, anchored.size(), rich(result).getDiagnostics().toString());
		Set<String> labels = new HashSet<>();
		Set<String> tokens = new HashSet<>();
		for (LocusIntersectionSolution2D solution : anchored) {
			labels.add(germ(solution).endsWith(AT_ZERO) ? "0" : germ(solution).endsWith(AT_PI)
					? "pi" : germ(solution));
			tokens.add(solution.getIdentity().getRootToken());
			assertEquals(ExistenceGrade.EXISTENCE_EXACT, solution.getRevisionEvidence()
					.getExistenceEvidence().getGrade(), "class 2 at both folds");
			assertTrue(result.isPointAdmissible(solution.getIdentity().getRootToken()),
					"B1-15: an anchored solution of class 2 is admissible");
		}
		assertEquals(Set.of("0", "pi"), labels, "B1-5 and B1-6");
		assertEquals(2, tokens.size(), "B1-5: two anchors, two tokens");
	}

	@Test
	void b1x7LosingTheSymmetryIsABarrierWithoutReassociation() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("V=(0,1)");
		add("X0=Intersect(Line(N,Line((0,0),V)),Line((0,3),(1,3)))");
		add("lx=LocusV2(X0,N)");
		add("A=(1.9999,0)");
		add("g=Line(A,Line((0,0),(0,1)))");
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoLocusIntersectionResult result = add("R=Intersect(lx,g)");
		String token = fold(rich(result)).get(0).getIdentity().getRootToken();
		GeoPoint point = claim(result);

		add("V=(0.001,1)");
		assertTrue(fold(rich(result())).isEmpty(), "B1-7: no certified fold, no anchor");
		assertFalse(point.isDefined(), "B1-7: the token is latent, never reassociated");
		for (LocusIntersectionSolution2D solution : rich(result()).getFiniteSolutions()) {
			assertNotEquals(token, solution.getIdentity().getRootToken());
		}

		add("V=(0,1)");
		assertEquals(token, fold(rich(result())).get(0).getIdentity().getRootToken());
		assertTrue(point.isDefined());
	}

	@Test
	void b1x8AnotherRootInTheBoxLeavesNoAnchor() {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("X0=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
		add("lx=LocusV2(X0,N)");
		add("C=(1.99985,3)");
		add("Q=(1.9999,3)");
		add("k=Circle(C,Q)");
		AuthorityPolicyAdoption.adopt(getConstruction());
		GeoLocusIntersectionResult result = add("R=Intersect(lx,k)");

		assertTrue(fold(rich(result)).isEmpty(), "B1-8: two points near the fold, no anchor");
		assertTrue(count(rich(result), DiagnosticCode.IDENTITY_DISCONTINUITY)
				+ count(rich(result), DiagnosticCode.FOLD_CONTACT_LOST) > 0,
				rich(result).getDiagnostics().toString());
		assertEquals(0, count(rich(result), DiagnosticCode.SUPERSEDED_BY_CERTIFIED_FOLD_PAIR),
				"B1-8: nothing is superseded without an anchor");
	}

	@Test
	void b1x9TheAdoptionSupersedesTheTransverseTokensOfAPairAndUndoRestoresThem() {
		activateUndo();
		GeoLocusIntersectionResult legacy = segment(1.9999, false);
		List<String> transverse = tokens(rich(legacy));
		assertEquals(2, transverse.size(), "the legacy policy publishes both preimages");
		GeoPoint point = claim(legacy);
		getApp().storeUndoInfo();

		AuthorityPolicyAdoption.adopt(getConstruction());
		assertEquals(1, fold(rich(result())).size());
		assertFalse(tokens(rich(result())).contains(transverse.get(0)),
				"B1-9: never fused or redirected");
		assertEquals(2, count(rich(result()), DiagnosticCode.SUPERSEDED_BY_CERTIFIED_FOLD_PAIR));
		assertFalse(point.isDefined(), "B1-9: the claim on a superseded token is latent");

		getApp().getUndoManager().undo();
		assertFalse(AuthorityPolicyAdoption.isConstructive(getConstruction()));
		assertEquals(transverse, tokens(rich(result())), "B1-9: undo restores the tokens");
		assertTrue(((GeoPoint) lookup("X")).isDefined());
	}

	@Test
	void b1x10UndoAndRedoRestoreTheLedgerAndItsClaims() {
		activateUndo();
		GeoLocusIntersectionResult result = segment(1.9999, true);
		String token = fold(rich(result)).get(0).getIdentity().getRootToken();
		claim(result);
		getApp().storeUndoInfo();
		move(2.01);
		getApp().storeUndoInfo();
		assertFalse(((GeoPoint) lookup("X")).isDefined());

		getApp().getUndoManager().undo();
		assertEquals(token, fold(rich(result())).get(0).getIdentity().getRootToken());
		assertTrue(((GeoPoint) lookup("X")).isDefined(), "B1-10: undo");
		getApp().getUndoManager().redo();
		assertFalse(((GeoPoint) lookup("X")).isDefined(), "B1-10: redo");
	}

	@Test
	void b1x11to13TheLedgerWritesVersionSixOnlyWithAnAnchoredEntry() {
		GeoLocusIntersectionResult result = segment(1.9999, true);
		String token = fold(rich(result)).get(0).getIdentity().getRootToken();
		claim(result);
		String state = result.getTokenLedgerState();
		assertTrue(state.startsWith("6|"), "B1-11: an anchored entry needs version 6");
		assertTrue(state.contains("~F,"), "B1-11: the entry-kind discriminator");

		String xml = getApp().getXML();
		getApp().setXML(xml, true);
		// Re-evaluation only raises the monotone high-water mark of incarnations.
		assertEquals(entries(state), entries(result().getTokenLedgerState()),
				"B1-11: round trip of the entries");
		assertEquals(token, fold(rich(result())).get(0).getIdentity().getRootToken());
		assertTrue(((GeoPoint) lookup("X")).isDefined());

		assertThrows(RuntimeException.class,
				() -> result().restoreTokenLedgerState("5" + state.substring(1)),
				"B1-13: a fold-contact entry is not a version-5 entry");
		assertThrows(RuntimeException.class,
				() -> result().restoreTokenLedgerState("7" + state.substring(1)),
				"B1-13: an unknown version fails closed, as version 6 does for older readers");
	}

	@Test
	void b1x12ALegacyDocumentAnchorsNothingAndKeepsItsLedgerVersion() {
		GeoLocusIntersectionResult legacy = segment(1.9999, false);
		claim(legacy);

		assertTrue(fold(rich(legacy)).isEmpty(), "B1-12: no anchor under the legacy policy");
		assertEquals(2, rich(legacy).getFiniteSolutions().size());
		assertTrue(legacy.getTokenLedgerState().startsWith("4|"),
				"B1-12: the ledger keeps version 4");
	}

	@Test
	void b1x11ReopeningRaisesOnlyTheHighWaterMarkOfIncarnations() {
		GeoLocusIntersectionResult result = segment(1.9999, true);
		claim(result);
		List<String> anchoredTokens = tokens(rich(result));
		String first = result.getTokenLedgerState();

		getApp().setXML(getApp().getXML(), true);
		String second = result().getTokenLedgerState();
		getApp().setXML(getApp().getXML(), true);
		String third = result().getTokenLedgerState();
		assertEquals(entries(first), entries(second), "the entries survive each reopening");
		assertEquals(entries(second), entries(third));
		assertTrue(highWater(first) <= highWater(second) && highWater(second) <= highWater(third),
				"the high-water mark is monotone and only reserves incarnations");
		assertEquals(anchoredTokens, tokens(rich(result())), "durable identity unchanged");

		move(-1.9999);
		List<String> later = tokens(rich(result()));
		assertEquals(1, later.size());
		assertFalse(anchoredTokens.contains(later.get(0)),
				"a later allocation is fresh and never reuses an identity");
		move(1.9999);
		assertEquals(anchoredTokens, tokens(rich(result())), "the claimed anchor reactivates");
		assertTrue(((GeoPoint) lookup("X")).isDefined());
	}

	@Test
	void b1x14DuplicationMintsFreshIdentities() {
		GeoLocusIntersectionResult result = segment(1.9999, true);
		GeoPoint point = claim(result);
		String token = tokens(rich(result)).get(0);
		int count = getConstruction().getGeoSetConstructionOrder().size();

		org.geogebra.common.util.InternalClipboard.duplicate(getApp(),
				List.of(result, point));

		assertTrue(getConstruction().getGeoSetConstructionOrder().size() > count);
		GeoLocusIntersectionResult copy = (GeoLocusIntersectionResult) lookup("R_{1}");
		List<LocusIntersectionSolution2D> anchored = fold(rich(copy));
		assertEquals(1, anchored.size(), "B1-14: the copy anchors at the same fold");
		assertNotEquals(token, anchored.get(0).getIdentity().getRootToken(),
				"B1-14: fresh closure, no identity is reused");
		assertTrue(((GeoPoint) lookup("X_{1}")).isDefined());
		assertEquals(List.of(token), tokens(rich(result())), "the original keeps its identity");
		assertTrue(point.isDefined());
	}

	@Test
	void b1x16RedefinitionsTreatAnchoredAndTransverseSelectorsAlike() {
		for (boolean adopt : new boolean[] {true, false}) {
			getApp().getKernel().clearConstruction(true);
			GeoLocusIntersectionResult result = segment(adopt ? 1.9995 : 1.99, adopt);
			assertEquals(adopt, !fold(rich(result)).isEmpty());
			claim(result);
			String token = rich(result).getFiniteSolutions().stream()
					.map(solution -> solution.getIdentity().getRootToken())
					.filter(result::isPointAdmissible).findFirst().orElseThrow();

			redefine("g", "Line(A,Line((0,0),(0.0001,1)))");
			assertTrue(tokens(rich(result())).contains(token),
					"B1-16: a redefined target keeps the selector and the token, adopt=" + adopt);
			assertTrue(((GeoPoint) lookup("X")).isDefined());

			redefine("g", "Line(A,Line((0,0),(0.01,1)))");
			assertFalse(tokens(rich(result())).contains(token));
			assertFalse(((GeoPoint) lookup("X")).isDefined(),
					"B1-16: no root, the claim is latent, adopt=" + adopt);

			redefine("g", "Line(A,Line((0,0),(0,1)))");
			assertTrue(tokens(rich(result())).contains(token),
					"B1-16: never reassociated, the same selector reactivates, adopt=" + adopt);
			assertTrue(((GeoPoint) lookup("X")).isDefined());
		}
	}

	@Test
	void b1x18TheRegularTangencyOfTheReplicaStaysOutsideCu3B1() {
		buildAuthorReplica();
		add("U=Intersect(o_1,h_2)");
		AuthorityPolicyAdoption.adopt(getConstruction());

		GeoLocusIntersectionResult result = (GeoLocusIntersectionResult) lookup("U");
		assertTrue(fold(rich(result)).isEmpty(), "o_1 has no fold: CU3-B2, not authorized");
		for (LocusIntersectionSolution2D solution : rich(result).getFiniteSolutions()) {
			assertFalse(result.isPointAdmissible(solution.getIdentity().getRootToken()),
					"a regular tangency is not admitted by CU3-B1");
		}
	}

	// ---------------------------------------------------------------- helpers

	private void redefine(String label, String definition) {
		getApp().getKernel().getAlgebraProcessor().changeGeoElement(lookup(label), definition,
				true, false, org.geogebra.common.main.error.ErrorHelper.silent(), null);
		assertTrue(lookup(label).getDefinitionForInputBar().replace(" ", "")
				.endsWith(definition), "redefined as " + definition);
	}

	private static long highWater(String state) {
		return Long.parseLong(state.split("\\|")[1]);
	}

	private GeoLocusIntersectionResult segment(double a, boolean adopt) {
		add("c=Circle((0,0),2)");
		add("N=Point(c)");
		add("X0=Intersect(Line(N,Line((0,0),(0,1))),Line((0,3),(1,3)))");
		add("lx=LocusV2(X0,N)");
		add("A=(" + a + ",0)");
		add("g=Line(A,Line((0,0),(0,1)))");
		if (adopt) {
			AuthorityPolicyAdoption.adopt(getConstruction());
		}
		return add("R=Intersect(lx,g)");
	}

	private void move(double a) {
		add("A=(" + a + ",0)");
	}

	private GeoLocusIntersectionResult result() {
		return (GeoLocusIntersectionResult) lookup("R");
	}

	/** Materializes the first admissible solution as the dependent point X. */
	private GeoPoint claim(GeoLocusIntersectionResult result) {
		String token = rich(result).getFiniteSolutions().stream()
				.map(solution -> solution.getIdentity().getRootToken())
				.filter(result::isPointAdmissible).findFirst().orElseThrow();
		return LocusV2PublicOperations.selectIntersectionPoint(getConstruction(), "X", result,
				new GeoText(getConstruction(), token));
	}

	private static LocusIntersectionResult2D rich(GeoLocusIntersectionResult result) {
		return result.getIntersectionResult();
	}

	private static String germ(LocusIntersectionSolution2D solution) {
		return solution.getRevisionEvidence().getCurrentRootGerm().orElse("");
	}

	private static List<LocusIntersectionSolution2D> fold(LocusIntersectionResult2D result) {
		List<LocusIntersectionSolution2D> anchored = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			if (germ(solution).startsWith(GERM)) {
				anchored.add(solution);
			}
		}
		return anchored;
	}

	private static List<String> tokens(LocusIntersectionResult2D result) {
		List<String> tokens = new ArrayList<>();
		for (LocusIntersectionSolution2D solution : result.getFiniteSolutions()) {
			tokens.add(solution.getIdentity().getRootToken());
		}
		return tokens;
	}

	/** @return the state without its high-water mark of incarnations */
	private static String entries(String state) {
		return state.replaceFirst("^(\\d+)\\|\\d+\\|", "$1||");
	}

	private static int count(LocusIntersectionResult2D result, DiagnosticCode code) {
		int count = 0;
		for (IntersectionDiagnostic2D diagnostic : result.getDiagnostics()) {
			count += diagnostic.getCode() == code ? 1 : 0;
		}
		return count;
	}
}
