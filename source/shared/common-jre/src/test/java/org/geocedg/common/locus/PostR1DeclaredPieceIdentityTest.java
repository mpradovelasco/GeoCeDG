/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.locus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionPointV2;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusComponentLineage2D;
import org.geocedg.common.kernel.locus.LocusDeclaredPieceIdentity;
import org.geocedg.common.kernel.locus.LocusExistenceStructure2D.ContinuousComponent;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTokenLedger2D;
import org.geocedg.common.kernel.spatial.identity.GeoIdentityRecord;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geogebra.common.jre.headless.AppCommon;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.util.InternalClipboard;
import org.junit.jupiter.api.Test;

/**
 * Final corrective intervention of GeoCeDG 1.2.2, step C2 (author authorization of
 * 2026-11-22, alternative alpha; {@code locus-v2-existence-components.md} section 20,
 * {@code spline-v2-pair-materialization.md} section 11): the roots on a declared piece of a
 * driver domain are identified by what the piece declares, never by the place that the
 * piece has in its list. A token names its root or stays unresolved; a change of place
 * never gives it to another root.
 *
 * <p>The source {@code LA} is the line {@code y = 1} traced by {@code m}. The spline
 * {@code SP} crosses it going up at about 0.174, going down at about 1.5 and going up
 * again at about 2.826, so that the two rising roots have the same germ.</p>
 */
final class PostR1DeclaredPieceIdentityTest extends PreG9bR3RedefineTestBase {
	private static final double LOW = 0.174;
	private static final double HIGH = 2.826;
	private static final String EMPTY_AND_LOW =
			"{false,{-2,-1.6,true,true},{0.1,0.9,true,true}}";
	private static final String LOW_AND_HIGH =
			"{false,{0.1,0.9,true,true},{2.3,2.95,true,true}}";
	private static final String EMPTY_AND_HIGH =
			"{false,{-2,-1.6,true,true},{2.3,2.95,true,true}}";
	private static final String RESOURCES = "/org/geocedg/common/locus/post-r1/";

	private GeoLocusIntersectionResult pair(String list) {
		add("m=0");
		add("QA=(m,1)");
		add("DLA=" + list);
		GeoLocusV2 locus = add("LA=LocusV2(QA,m,DLA)");
		assertNotNull(locus);
		add("SA=(0,0)");
		add("SB=(1,4)");
		add("SC=(2,-2)");
		add("SD=(3,2)");
		add("SP=SplineV2({SA,SB,SC,SD},3)");
		return add("RAB=Intersect(LA,SP)");
	}

	private GeoLocusIntersectionResult rich(String label) {
		return (GeoLocusIntersectionResult) requireLookup(label);
	}

	/** @return the token of the published root at the abscissa, or {@code null} */
	private String tokenAt(String label, double x) {
		for (LocusIntersectionSolution2D root : rich(label).getIntersectionResult()
				.getFiniteSolutions()) {
			if (Math.abs(root.getEvaluatedPoint().getX() - x) < 1e-3) {
				return root.getIdentity().getRootToken();
			}
		}
		return null;
	}

	private GeoPoint pointOn(String label, String result, double x) {
		String token = tokenAt(result, x);
		assertNotNull(token, "a root at " + x);
		assertTrue(rich(result).isPointAdmissible(token), "admissible root at " + x);
		GeoPoint point = add(label + "=Intersect(" + result + ",\"" + token + "\")");
		assertTrue(point.isDefined());
		assertEquals(x, point.getInhomX(), 1e-3);
		return point;
	}

	private void assertAt(String label, double x, String token, String where) {
		GeoPoint point = (GeoPoint) requireLookup(label);
		assertTrue(point.isDefined(), where);
		assertEquals(x, point.getInhomX(), 1e-3, where);
		assertEquals(token, selectedToken(point), where);
	}

	private void assertUndefined(String label, String where) {
		assertFalse(requireLookup(label).isDefined(), where);
	}

	private static String selectedToken(GeoPoint point) {
		return ((AlgoLocusIntersectionPointV2) point.getParentAlgorithm())
				.getSelectedRootToken();
	}

	private void redefineList(String label, String list) {
		Outcome outcome = redefine(label, list, PreG9bR3RedefineTestBase::retainWhenAvailable);
		assertTrue(outcome.succeeded(), outcome.errors());
	}

	private void move(String label, double value) {
		GeoNumeric number = (GeoNumeric) requireLookup(label);
		number.setValue(value);
		number.updateCascade();
	}

	// ---- The demonstrated defect: a piece that the declaration moves to another place ----

	@Test
	void aPieceThatChangesPlaceInItsListKeepsItsRoots() {
		pair(EMPTY_AND_LOW);
		String token = selectedToken(pointOn("X", "RAB", LOW));
		assertNull(tokenAt("RAB", HIGH));

		// The piece of the root stays declared, unchanged, and becomes the first of the
		// list; another piece, with a root of the same germ, takes its place.
		redefineList("DLA", LOW_AND_HIGH);

		assertAt("X", LOW, token, "the piece of the root is still declared");
		assertEquals(token, tokenAt("RAB", LOW));
		String other = tokenAt("RAB", HIGH);
		assertNotNull(other, "the new piece has its own root");
		assertNotEquals(token, other, "the root of the new piece has its own token");
		assertTrue(rich("RAB").isPointAdmissible(other));
	}

	@Test
	void aPieceThatIsReplacedLeavesItsPointUnresolvedAndTheReturnRestoresIt() {
		pair(EMPTY_AND_LOW);
		String token = selectedToken(pointOn("X", "RAB", LOW));

		// The piece of the root is gone; the piece at its place has a root of its germ.
		redefineList("DLA", EMPTY_AND_HIGH);
		assertUndefined("X", "the piece of the root is no longer declared");
		String other = tokenAt("RAB", HIGH);
		assertNotNull(other);
		assertNotEquals(token, other, "the token was not given to the root of another piece");

		redefineList("DLA", EMPTY_AND_LOW);
		assertAt("X", LOW, token, "the declaration of the piece returned");
		assertNull(tokenAt("RAB", HIGH));
	}

	@Test
	void aPieceGovernedByNamedObjectsWithoutDurableIdentityStaysAPlaceOfItsList() {
		add("a=-2");
		add("wa=0.4");
		add("b=0.1");
		add("wb=0.8");
		pair("{false,{a,a+wa,true,true},{b,b+wb,true,true}}");
		String token = selectedToken(pointOn("X", "RAB", LOW));
		String written = rich("RAB").getTokenLedgerState();
		assertEquals(4, written.split("\\|", -1).length);

		// A slider has a label and no durable identity, and a label is no identity: the
		// piece is a place of the list, with the positional meaning that D2 approves. The
		// same place moves: its root is the root that it now contains.
		move("b", 2.3);
		move("wb", 0.65);
		assertAt("X", HIGH, token, "the place of the token moved to the other root");
		move("a", 0.1);
		move("wa", 0.8);
		assertAt("X", HIGH, token, "another place arrived at the first root");
		String other = tokenAt("RAB", LOW);
		assertNotNull(other);
		assertNotEquals(token, other);

		// Renaming a slider changes nothing, and neither does a piece declared after.
		requireLookup("b").rename("bb");
		getKernel().updateConstruction(false);
		assertAt("X", HIGH, token, "a renamed slider");
		redefineList("DLA", "{false,{a,a+wa,true,true},{bb,bb+wb,true,true},"
				+ "{5,6,true,true}}");
		assertAt("X", HIGH, token, "a piece declared after the two places");
		assertEquals(other, tokenAt("RAB", LOW));
		assertEquals(4, rich("RAB").getTokenLedgerState().split("\\|", -1).length,
				"no frame is kept for a place");
	}

	// ---- The whole declared domain keeps its frame, and a piece keeps what it had ----

	@Test
	void theWholeDeclaredDomainIsOnePlaceAndKeepsItsRootsWhenItGainsPieces() {
		pair("{false,{0.1,0.9,true,true}}");
		String token = selectedToken(pointOn("X", "RAB", LOW));
		String ledger = rich("RAB").getTokenLedgerState();
		assertTrue(ledger.startsWith("5|"), ledger);
		assertEquals(4, ledger.split("\\|", -1).length, "no frame is kept: the text of always");

		// The whole domain is one place: changing its ends changes no token.
		redefineList("DLA", "{false,{0.05,0.95,true,true}}");
		assertAt("X", LOW, token, "the whole domain with other ends");
		redefineList("DLA", "{false,{0.1,0.9,true,true}}");
		assertAt("X", LOW, token, "the whole domain as it was");

		// It gains a piece before it and a piece after it: it keeps its roots.
		redefineList("DLA", "{false,{-2,-1.6,true,true},{0.1,0.9,true,true},"
				+ "{2.3,2.95,true,true}}");
		assertAt("X", LOW, token, "the whole domain became one of three pieces");
		String other = tokenAt("RAB", HIGH);
		assertNotNull(other);
		assertNotEquals(token, other);
		assertEquals(5, rich("RAB").getTokenLedgerState().split("\\|", -1).length,
				"the frame of the whole domain is kept for its piece");

		// Another whole domain is not that piece: nothing is given to its root.
		redefineList("DLA", "{false,{2.3,2.95,true,true}}");
		assertUndefined("X", "the whole domain is another declaration");
		String whole = tokenAt("RAB", HIGH);
		assertNotNull(whole);
		assertNotEquals(token, whole);

		redefineList("DLA", "{false,{0.1,0.9,true,true}}");
		assertAt("X", LOW, token, "the declaration of the piece is the whole domain again");
	}

	// ---- Persistence, Undo and Redo, copy ----

	@Test
	void undoRedoAndReopeningKeepTheRootsOfEveryState() throws Exception {
		getApp().setUndoActive(true);
		pair(EMPTY_AND_LOW);
		String token = selectedToken(pointOn("X", "RAB", LOW));
		getApp().storeUndoInfo();
		redefineList("DLA", LOW_AND_HIGH);
		getApp().storeUndoInfo();
		assertAt("X", LOW, token, "after the redefinition");
		String moved = getApp().getXML();

		getKernel().undo();
		assertAt("X", LOW, token, "undo");
		assertNull(tokenAt("RAB", HIGH));
		getKernel().redo();
		assertAt("X", LOW, token, "redo");
		assertNotNull(tokenAt("RAB", HIGH));

		AppCommon reopened = openInFreshApp(moved);
		String read = reopened.getXML();
		assertEquals(read, openInFreshApp(read).getXML(), "the document is read back exactly");
		GeoPoint point = (GeoPoint) reopened.getKernel().lookupLabel("X");
		assertTrue(point.isDefined());
		assertEquals(LOW, point.getInhomX(), 1e-3);
		assertEquals(token, selectedToken(point));
	}

	@Test
	void aClosureCopyDeclaresThePiecesOfItsOriginal() {
		pair(EMPTY_AND_LOW);
		GeoPoint point = pointOn("X", "RAB", LOW);
		PersistentGeoId originalId = getConstruction().getSpatialIdentityRegistry()
				.getPersistentGeoId(point);
		String clipboard = InternalClipboard.getTextToSave(getApp(), List.of(point),
				text -> text);
		int separator = clipboard.indexOf('\n');
		InternalClipboard.pasteGeoGebraXMLInternal(getApp(), new ArrayList<>(Arrays.asList(
				clipboard.substring(0, separator).split(" "))), clipboard.substring(separator));
		var registry = getConstruction().getSpatialIdentityRegistry();
		GeoIdentityRecord copiedRecord = registry.getRecords().stream()
				.filter(GeoIdentityRecord.class::isInstance).map(GeoIdentityRecord.class::cast)
				.filter(record -> originalId.equals(record.getCopySourceId()))
				.findFirst().orElseThrow();
		GeoPoint copied = (GeoPoint) registry.getGeo(copiedRecord.getId());
		assertTrue(copied.isDefined(), "the copied point is on the root of the copied piece");
		assertEquals(point.getInhomX(), copied.getInhomX(), 0);
		assertNotEquals(selectedToken(point), selectedToken(copied));
	}

	// ---- Documents written before the pieces had a declarative frame ----

	@Test
	void aDocumentWrittenBeforeKeepsItsTokensAndFollowsItsPiecesFromThenOn()
			throws Exception {
		for (String kind : new String[] {"pair", "target"}) {
			String name = "literal-pieces-" + kind + "-written-by-9ccefdcf";
			String[] written = resource(name + ".tokens").split("\n");
			String result = "pair".equals(kind) ? "RAB" : "RS";
			getApp().setXML(resource(name + ".xml"), true);
			assertAt("X", LOW, written[0].trim(), kind + ": the token as it was written");
			assertAt("Y", HIGH, written[1].trim(), kind + ": the token as it was written");
			String ledger = rich(result).getTokenLedgerState();
			assertTrue(ledger.startsWith(written[2].trim() + "|declared-frames/v1~2~"),
					kind + ": the bindings as written, and the two frames that they cite"
							+ " kept for the pieces that the document declares");

			// The first piece of the document goes: the two pieces of the roots change
			// place, and each keeps its roots.
			redefineList("DLA", LOW_AND_HIGH);
			assertAt("X", LOW, written[0].trim(), kind + ": after the redefinition");
			assertAt("Y", HIGH, written[1].trim(), kind + ": after the redefinition");
			// The piece of X is replaced: its token is given to no other root.
			redefineList("DLA", EMPTY_AND_HIGH);
			assertUndefined("X", kind + ": the piece of the root is no longer declared");
			assertAt("Y", HIGH, written[1].trim(), kind + ": the other piece");
			redefineList("DLA", LOW_AND_HIGH);

			AppCommon reopened = openInFreshApp(getApp().getXML());
			String saved = reopened.getXML();
			assertEquals(saved, openInFreshApp(saved).getXML(), kind);
			GeoPoint read = (GeoPoint) reopened.getKernel().lookupLabel("X");
			assertTrue(read.isDefined(), kind);
			assertEquals(LOW, read.getInhomX(), 1e-3, kind);
			assertEquals(written[0].trim(), selectedToken(read), kind);
		}
	}

	@Test
	void aPlaceNeverTakesTheFrameThatIsKeptForADeclaredPiece() throws Exception {
		String name = "literal-pieces-pair-written-by-9ccefdcf";
		String[] written = resource(name + ".tokens").split("\n");
		getApp().setXML(resource(name + ".xml"), true);
		assertAt("X", LOW, written[0].trim(), "the token as it was written");
		// The frame of the second place of the document is kept for the piece [0.1, 0.9].
		// A piece governed by a slider now stands at that place: it is a place of the list
		// and has its own frame. The redefinition rebuilds the construction, and the frame
		// of no place is kept for a piece that the document did not declare there.
		add("s=2.3");
		redefineList("DLA", "{false,{0.1,0.9,true,true},{s,s+0.65,true,true}}");
		assertAt("X", LOW, written[0].trim(), "the declared piece keeps its roots");
		assertUndefined("Y", "the piece of the root is no longer declared");
		String place = tokenAt("RAB", HIGH);
		assertNotNull(place, "the place has its own root");
		assertNotEquals(written[0].trim(), place);
		assertNotEquals(written[1].trim(), place);
		move("s", 2.4);
		assertAt("X", LOW, written[0].trim(), "the place moved");
		assertEquals(place, tokenAt("RAB", HIGH), "the place keeps its positional meaning");
	}

	@Test
	void aDocumentWhosePiecesArePlacesIsReadAsItWasWritten() throws Exception {
		String name = "unnamed-pieces-pair-written-by-9ccefdcf";
		String[] written = resource(name + ".tokens").split("\n");
		getApp().setXML(resource(name + ".xml"), true);
		assertAt("X", LOW, written[0].trim(), "the token as it was written");
		assertAt("Y", LOW, written[1].trim(), "the token as it was written");
		assertEquals(written[2].trim(), rich("RAB").getTokenLedgerState(),
				"the ledger text of always");
		assertEquals(written[3].trim(), rich("RBA").getTokenLedgerState());
	}

	// ---- One source against a target: the same rule ----

	@Test
	void theRootsOfOneSourceAgainstATargetFollowTheDeclaredPiece() {
		add("m=0");
		add("QA=(m,1)");
		add("DLA=" + EMPTY_AND_LOW);
		add("LA=LocusV2(QA,m,DLA)");
		add("TG=Circle((1.5,1),1.326)");
		add("RS=Intersect(LA,TG)");
		String token = selectedToken(pointOn("X", "RS", LOW));
		String ledger = rich("RS").getTokenLedgerState();
		assertTrue(ledger.startsWith("4|"), ledger);

		redefineList("DLA", LOW_AND_HIGH);
		assertAt("X", LOW, token, "the piece of the root is still declared");
		String other = tokenAt("RS", HIGH);
		assertNotNull(other);
		assertNotEquals(token, other);

		redefineList("DLA", EMPTY_AND_HIGH);
		assertUndefined("X", "the piece of the root is no longer declared");
		redefineList("DLA", EMPTY_AND_LOW);
		assertAt("X", LOW, token, "the declaration of the piece returned");
	}

	// ---- The identity itself ----

	@Test
	void theIdentityOfAPieceIsItsDeclarationAndNeverItsPlace() {
		pair(EMPTY_AND_LOW);
		var registry = getConstruction().getSpatialIdentityRegistry();
		// The driver of a locus has a durable identity; a plain slider has none.
		assertNotNull(registry.getPersistentGeoId(requireLookup("m")));
		add("free=-2");
		assertNull(registry.getPersistentGeoId(requireLookup("free")));
		GeoList durable = add("L2={false,{m,m+0.4,true,true},{3,4,true,true}}");
		GeoList moved = add("L3={false,{-5,-4,true,true},{m,m+0.4,true,true}}");
		List<String> first = LocusDeclaredPieceIdentity.of(durable, registry);
		List<String> second = LocusDeclaredPieceIdentity.of(moved, registry);
		assertEquals(first.get(0), second.get(1), "the same declaration at another place");
		assertNotEquals(first.get(0), first.get(1));
		assertTrue(first.get(0).contains("geo:"), first.get(0));
		assertNotEquals(first.get(0), LocusDeclaredPieceIdentity.of((GeoList) add(
				"L5={false,{m,m+0.5,true,true},{3,4,true,true}}"), registry).get(0),
				"another operation is another declaration");
		requireLookup("m").rename("mm");
		assertEquals(first, LocusDeclaredPieceIdentity.of(durable, registry),
				"a label is no part of a declaration");

		List<String> literal = LocusDeclaredPieceIdentity.of((GeoList) add(
				"L4=" + EMPTY_AND_LOW), registry);
		assertEquals(2, new HashSet<>(literal).size());
		assertEquals(first.get(1), LocusDeclaredPieceIdentity.of((GeoList) add(
				"L7={false,{3,4,true,true},{7,8,true,true}}"), registry).get(0),
				"a literal piece is its literal ends");

		List<String> places = LocusDeclaredPieceIdentity.of((GeoList) add(
				"L6={false,{free,free+0.4,true,true},{3,4,true,true}}"), registry);
		assertNull(places.get(0), "a piece declared by a slider is a place of its list");
		assertEquals(first.get(1), places.get(1));

		String sole = LocusDeclaredPieceIdentity.of((GeoList) add(
				"L1={false,{3,4,true,true}}"), registry).get(0);
		assertTrue(LocusDeclaredPieceIdentity.isSole(sole));
		assertFalse(LocusDeclaredPieceIdentity.isSole(first.get(1)));
		assertEquals(first.get(1), LocusDeclaredPieceIdentity.declaration(sole),
				"the whole domain declares what the piece declares");
	}

	@Test
	void theFrameOfAPieceNamesNeitherItsPlaceNorTheOtherPieces() {
		pair(EMPTY_AND_LOW);
		String before = frameOf(1);
		redefineList("DLA", LOW_AND_HIGH);
		assertEquals(before, frameOf(0), "the same piece at another place");
		assertNotEquals(before, frameOf(1));
		assertTrue(before.contains("/declared-piece/"), before);
		// The certified structure and its lineage keys are what they were.
		LocusBranch2D branch = ((GeoLocusV2) requireLookup("LA")).getSemanticDefinition()
				.getBranch(BRANCH);
		assertFalse(branch.getExistenceStructure().getSemanticSignature()
				.contains("/declared-piece/"));
		for (String key : LocusComponentLineage2D.keys(branch.getExistenceStructure())) {
			assertFalse(key.contains("/declared-piece/"), key);
		}
	}

	private String frameOf(int component) {
		LocusBranch2D branch = ((GeoLocusV2) requireLookup("LA")).getSemanticDefinition()
				.getBranch(BRANCH);
		ContinuousComponent piece = branch.getExistenceStructure()
				.getContinuousValidComponents().get(component);
		return branch.searchFrame(piece);
	}

	// ---- The kept frames of the ledger ----

	@Test
	void theKeptFramesAreWrittenAndReadStrictly() {
		pair("{false,{0.1,0.9,true,true}}");
		pointOn("X", "RAB", LOW);
		redefineList("DLA", LOW_AND_HIGH);
		String state = rich("RAB").getTokenLedgerState();
		String[] fields = state.split("\\|", -1);
		assertEquals(5, fields.length, state);
		assertTrue(fields[4].startsWith("declared-frames/v1~1~"), fields[4]);
		LocusIntersectionTokenLedger2D ledger = new LocusIntersectionTokenLedger2D();
		ledger.importState(state);
		assertEquals(state, ledger.exportState());
		String base = state.substring(0, state.lastIndexOf('|'));
		for (String malformed : new String[] {base + "|declared-frames/v2~1~" + fields[4]
				.substring(fields[4].lastIndexOf('~') + 1), base + "|declared-frames/v1~2~"
						+ fields[4].substring(fields[4].lastIndexOf('~') + 1),
				base + "|declared-frames/v1~0", state + "|", base + "|"}) {
			assertThrows(IllegalArgumentException.class, () ->
					new LocusIntersectionTokenLedger2D().importState(malformed), malformed);
		}
	}

	private static String resource(String name) throws Exception {
		try (InputStream stream = PostR1DeclaredPieceIdentityTest.class.getResourceAsStream(
				RESOURCES + name)) {
			assertNotNull(stream, name);
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
