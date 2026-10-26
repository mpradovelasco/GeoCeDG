/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.locus.constructive;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.intersection.ResidualJetEnclosure2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetSource2D;
import org.geogebra.common.kernel.Path;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The enclosures of the constructive residual sources that reuse what no box changes and what
 * the queries of one locus share (PRE-G9B-CU3 focal optimization T6), on the replica of the
 * author's model: they are bitwise the enclosures of a full evaluation of every box, in every
 * revision, also where the evaluation fails; the queries of one locus share the traced point and
 * the queries of another never do; the key follows the revision of the source, its
 * parametrization and its branch, and not a target outside the slice; a new key discards the
 * enclosures of the earlier one; the legacy configuration never reaches the memory; Undo
 * restores enclosures equal to a full evaluation; and the memory is bounded.
 */
final class ConstructiveSliceEnclosureMemoryTest extends ConstructiveModelReplicaTest {

	private static final String[][] QUERIES = {{"e_1", "n"}, {"e_1", "c_1"}, {"e_1", "r"},
			{"o_1", "h_2"}};

	@BeforeEach
	void emptyMemories() {
		ConstructiveSliceEnclosureMemory.clearAll();
	}

	@Test
	void theAcceleratedEnclosuresAreThoseOfAFullEvaluationInEveryRevision() {
		buildAuthorReplica();
		int[] undefined = new int[1];
		compareAll("base", undefined);
		double parameter = parameter("H");
		for (double delta : new double[] {-20, 0.5, 40}) {
			moveOnPath("H", parameter + delta);
			compareAll("H " + delta, undefined);
		}
		moveOnPath("H", parameter);
		double onG = parameter("B");
		moveOnPath("B", onG + 3);
		compareAll("B + 3", undefined);
		moveOnPath("B", onG);
		GeoNumeric radius = (GeoNumeric) geo("rbCone");
		for (double value : new double[] {3, 7.5}) {
			setNumber(radius, value);
			compareAll("rbCone=" + value, undefined);
		}
		setNumber(radius, 5);
		GeoPoint d1 = (GeoPoint) geo("D_1");
		GeoPoint h = (GeoPoint) geo("H");
		double x = d1.getInhomX();
		double y = d1.getInhomY();
		place("D_1", h.getInhomX() + 3 * (x - h.getInhomX()),
				h.getInhomY() + 3 * (y - h.getInhomY()));
		compareAll("D_1 x3", undefined);
		place("D_1", x, y);
		assertTrue(undefined[0] > 0, "some boxes are not enclosed, and stay so: " + undefined[0]);
	}

	@Test
	void theQueriesOfOneLocusShareTheTracedPointAndOthersNever() {
		buildAuthorReplica();
		ResidualJetSource2D first = accelerated("e_1", "c_1");
		List<double[]> boxes = boxes();
		for (double[] box : boxes) {
			first.enclose(box[0], box[1]);
		}
		ConstructiveSliceEnclosureMemory e1 = ConstructiveSliceEnclosureMemory.of(
				(GeoLocusV2) geo("e_1"));
		int kept = e1.size();
		assertTrue(kept > 0);
		assertEquals(0, e1.getReused());
		ResidualJetSource2D second = accelerated("e_1", "r");
		assertEnclosuresEqual(second, plain("e_1", "r"), boxes, "e_1 r after e_1 c_1",
				new int[1]);
		assertTrue(e1.getReused() >= boxes.size(), "the second query reuses the traced point: "
				+ e1.getReused());
		assertEquals(kept, e1.size(), "and computes no new one");

		int reusedByE1 = e1.getReused();
		assertEnclosuresEqual(accelerated("o_1", "h_2"), plain("o_1", "h_2"), boxes,
				"o_1 h_2", new int[1]);
		assertEquals(reusedByE1, e1.getReused(),
				"another locus never reads the traced point of e_1");
		assertEquals(kept, e1.size(), "nor writes into its memory");
		assertNotEquals(key("e_1", "c_1"), key("o_1", "h_2"));
	}

	@Test
	void theKeyFollowsTheSourceAndNotATargetOutsideTheSlice() {
		buildAuthorReplica();
		add("Q1=(0,0)");
		add("Q2=(1,1)");
		add("f=Line(Q1,Q2)");
		String base = key("e_1", "c_1");
		assertEquals(base, key("e_1", "n"), "the same slice for every target of e_1");
		assertEquals(base, key("e_1", "r"));
		assertEquals(base, key("e_1", "f"));
		assertNotEquals(base, key("o_1", "h_2"), "another locus, another slice");

		place("Q2", 2, 1);
		assertEquals(base, key("e_1", "f"), "a target outside the slice moves: the same key");
		ResidualJetSource2D populate = accelerated("e_1", "c_1");
		List<double[]> boxes = boxes();
		for (double[] box : boxes) {
			populate.enclose(box[0], box[1]);
		}
		ConstructiveSliceEnclosureMemory e1 = ConstructiveSliceEnclosureMemory.of(
				(GeoLocusV2) geo("e_1"));
		int reused = e1.getReused();
		assertEnclosuresEqual(accelerated("e_1", "f"), plain("e_1", "f"), boxes,
				"the moved target", new int[1]);
		assertTrue(e1.getReused() > reused, "its query reuses the traced point of the slice");

		double parameter = parameter("H");
		moveOnPath("H", parameter + 0.5);
		String moved = key("e_1", "c_1");
		assertNotEquals(base, moved, "a revision of the source changes the key");
		accelerated("e_1", "c_1").enclose(boxes.get(0)[0], boxes.get(0)[1]);
		assertEquals(1, e1.size(), "a new key discards every enclosure of the earlier one");
		moveOnPath("H", parameter);

		setNumber((GeoNumeric) geo("rbCone"), 6);
		String radius = key("e_1", "c_1");
		assertNotEquals(base, radius, "another parametrization of the driver");
		setNumber((GeoNumeric) geo("rbCone"), 5);

		add("H_1=Intersect(d_1,i,2)");
		String branch = key("e_1", "c_1");
		assertNotEquals(base, branch, "another branch of an intersection of the slice");
		assertTrue(branch.contains("LINE_CIRCLE_OUTPUT") && !branch.equals(radius));
		assertEnclosuresEqual(accelerated("e_1", "c_1"), plain("e_1", "c_1"), boxes,
				"the other branch", new int[1]);
	}

	@Test
	void theBoxIsPartOfTheKey() {
		buildAuthorReplica();
		ResidualJetSource2D source = accelerated("e_1", "c_1");
		source.enclose(0.25, 0.5);
		ConstructiveSliceEnclosureMemory e1 = ConstructiveSliceEnclosureMemory.of(
				(GeoLocusV2) geo("e_1"));
		String key = key("e_1", "c_1");
		assertNotNull(e1.get(key, 0.25, 0.5));
		assertNull(e1.get(key, 0.25, Math.nextUp(0.5)), "one ulp away is another box");
		assertNull(e1.get(key, 0.25, 0.75));
		assertNull(e1.get(key + " ", 0.25, 0.5), "another key reaches nothing");
	}

	@Test
	void theLegacyConfigurationNeverReachesTheMemory() {
		buildAuthorReplica();
		add("S=Intersect(e_1,c_1)");
		getKernel().updateConstruction(false);
		assertEquals(0, ConstructiveSliceEnclosureMemory.count(),
				"legacy-representation-authority/v1 evaluates the captured configuration");
		AuthorityPolicyAdoption.adopt(getConstruction());
		assertTrue(ConstructiveSliceEnclosureMemory.of((GeoLocusV2) geo("e_1")).size() > 0,
				"the constructive configuration keeps the traced point of its boxes");
	}

	@Test
	void undoRestoresEnclosuresEqualToAFullEvaluation() {
		activateUndo();
		buildAuthorReplica();
		AuthorityPolicyAdoption.adopt(getConstruction());
		add("S=Intersect(e_1,c_1)");
		getApp().storeUndoInfo();
		List<double[]> boxes = boxes();
		int[] undefined = new int[1];
		assertEnclosuresEqual(accelerated("e_1", "c_1"), plain("e_1", "c_1"), boxes, "before",
				undefined);
		double parameter = parameter("H");
		moveOnPath("H", parameter + 0.5);
		getApp().storeUndoInfo();
		assertEnclosuresEqual(accelerated("e_1", "c_1"), plain("e_1", "c_1"), boxes, "moved",
				undefined);
		getApp().getUndoManager().undo();
		assertEnclosuresEqual(accelerated("e_1", "c_1"), plain("e_1", "c_1"), boxes, "undone",
				undefined);
		getApp().getUndoManager().redo();
		assertEnclosuresEqual(accelerated("e_1", "c_1"), plain("e_1", "c_1"), boxes, "redone",
				undefined);
	}

	@Test
	void theMemoryIsBounded() {
		buildAuthorReplica();
		ConstructiveSliceEnclosureMemory memory = ConstructiveSliceEnclosureMemory.of(
				(GeoLocusV2) geo("e_1"));
		for (int box = 0; box < ConstructiveSliceEnclosureMemory.MAXIMUM_BOXES + 100; box++) {
			memory.put("bounded", box, box, ConstructiveSliceEnclosureMemory.FAILED);
		}
		assertEquals(ConstructiveSliceEnclosureMemory.MAXIMUM_BOXES, memory.size());
		assertNull(memory.get("bounded", ConstructiveSliceEnclosureMemory.MAXIMUM_BOXES + 50,
				ConstructiveSliceEnclosureMemory.MAXIMUM_BOXES + 50),
				"a box beyond the bound is computed again, never invented");
	}

	// ---------------------------------------------------------------- helpers

	private void compareAll(String revision, int[] undefined) {
		ConstructiveSliceEnclosureMemory.clearAll();
		List<double[]> boxes = boxes();
		for (String[] query : QUERIES) {
			assertEnclosuresEqual(accelerated(query[0], query[1]), plain(query[0], query[1]),
					boxes, revision + " " + query[0] + " " + query[1], undefined);
		}
		// A second pass reads the shared traced points of the first.
		for (String[] query : QUERIES) {
			assertEnclosuresEqual(accelerated(query[0], query[1]), plain(query[0], query[1]),
					boxes, revision + " again " + query[0] + " " + query[1], undefined);
		}
	}

	private static void assertEnclosuresEqual(ResidualJetSource2D accelerated,
			ResidualJetSource2D plain, List<double[]> boxes, String context, int[] undefined) {
		assertNotNull(accelerated, context);
		assertNotNull(plain, context);
		for (double[] box : boxes) {
			ResidualJetEnclosure2D fast = accelerated.enclose(box[0], box[1]);
			ResidualJetEnclosure2D full = plain.enclose(box[0], box[1]);
			String where = context + " on [" + box[0] + ", " + box[1] + "]";
			if (full == null) {
				assertNull(fast, where);
				undefined[0]++;
				continue;
			}
			assertNotNull(fast, where);
			assertArrayEquals(full.getValue(), fast.getValue(), 0, where);
			assertArrayEquals(full.getFirst(), fast.getFirst(), 0, where);
			assertArrayEquals(full.getSecond(), fast.getSecond(), 0, where);
			if (box[0] == box[1]) {
				double[] normalized = plain.normalizedResidual(box[0]);
				double[] again = accelerated.normalizedResidual(box[0]);
				if (normalized == null) {
					assertNull(again, where);
				} else {
					assertArrayEquals(normalized, again, 0, where);
				}
			}
		}
	}

	/** Boxes of several widths over the parameter circle, point boxes and the seam. */
	private static List<double[]> boxes() {
		List<double[]> boxes = new ArrayList<>();
		for (double width : new double[] {2 * Math.PI / 8, 2 * Math.PI / 64, 1E-3, 1E-7}) {
			for (int step = 0; step < 24; step++) {
				double lower = -Math.PI + step * (2 * Math.PI / 24);
				boxes.add(new double[] {lower, lower + width});
			}
		}
		for (int step = 0; step < 24; step++) {
			double point = -Math.PI + (step + 0.5) * (2 * Math.PI / 24);
			boxes.add(new double[] {point, point});
		}
		boxes.add(new double[] {Math.PI - 1E-6, Math.PI + 1E-6});
		boxes.add(new double[] {-1E-6, 1E-6});
		boxes.add(new double[] {-Math.PI, Math.PI});
		return boxes;
	}

	private ResidualJetSource2D accelerated(String locus, String target) {
		return ConstructiveResidualJetSource.capture((GeoLocusV2) geo(locus), geo(target),
				new ExactContactMemory(), ConstructiveIdentityBudget.V1, true);
	}

	private ResidualJetSource2D plain(String locus, String target) {
		return ConstructiveResidualJetSource.capture((GeoLocusV2) geo(locus), geo(target),
				new ExactContactMemory(), ConstructiveIdentityBudget.V1, false);
	}

	private String key(String locus, String target) {
		ConstructiveResidualJetSource source = (ConstructiveResidualJetSource) accelerated(locus,
				target);
		assertNotNull(source);
		return source.getSliceKey();
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
}
