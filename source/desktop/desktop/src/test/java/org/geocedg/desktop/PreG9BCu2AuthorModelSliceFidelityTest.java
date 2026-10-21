/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geogebra.common.io.XMLStringBuilder;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.algos.AlgoIntersectLineConic;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.geogebra.common.kernel.geos.GeoVec3D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU2 step T1, D-CU2-20-R focal characterization on the author model at the base:
 * one-ulp changes of {@code excCyl} away from the degeneracy (V-REV-EXACT-1), the exactness
 * of every independent input read by the slices of the model loci, and the selection state
 * of their line-conic intersections (F-CU2-8, F-CU2-14), read through the read-only accessors
 * of step T3 (D-CU2-17). Step T3 adds the fidelity validation of every model slice
 * (V-REV-EXACT-4). The isolated slices are read reflectively.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelSliceFidelityTest {

	/** Penetration and bite sides of the critical value of {@code excCyl}, one ulp apart. */
	private static final double PENETRATION = Double.longBitsToDouble(0x400d416bb67a38bbL);
	private static final double BITE = Double.longBitsToDouble(0x400d416bb67a38bcL);
	private static final List<String> MODEL_LOCI = List.of("o", "v", "w", "a_1", "e_1", "o_1");

	@TempDir
	Path work;

	/**
	 * V-REV-EXACT-1 at the base, driver at {@code t = -1}: each one-ulp change publishes one
	 * revision with an exact slice copy, and the fingerprint returns with the value.
	 */
	@Test
	void oneUlpChangesOfExcCylPublishRevisionsWithExactSliceCopies() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			PreG9BCu0Corpus.movePathPoint(app, "N", -1.0);
			GeoLocusV2 o = PreG9BCu0Corpus.locus(app, "o");
			// The first programmatic value replaces the typed literal of excCyl; the
			// alternation below then compares states with the same representation.
			PreG9BCu0Corpus.setNumber(app, "excCyl", PENETRATION);
			List<Long> revisionDeltas = new ArrayList<>();
			List<String> fingerprints = new ArrayList<>();
			List<Boolean> exactCopies = new ArrayList<>();
			fingerprints.add(fingerprint(app, o, "N"));
			long valueChanges = o.getInstrumentation().getSliceChanges(
					ReconstructionSliceFingerprint2D.Change.VALUES);
			for (double value : new double[] {BITE, PENETRATION, BITE}) {
				long revision = o.getSemanticRevision();
				PreG9BCu0Corpus.setNumber(app, "excCyl", value);
				revisionDeltas.add(o.getSemanticRevision() - revision);
				fingerprints.add(fingerprint(app, o, "N"));
				exactCopies.add(exact(app.getKernel().lookupLabel("excCyl")).equals(
						exact(copy(o, "excCyl"))));
			}
			Map<String, Object> values = new LinkedHashMap<>();
			values.put("revision_deltas", revisionDeltas);
			values.put("fingerprints", fingerprints);
			values.put("exact_copies", exactCopies);
			values.put("value_changes", o.getInstrumentation().getSliceChanges(
					ReconstructionSliceFingerprint2D.Change.VALUES) - valueChanges);
			return values;
		});
		assertEquals(List.of(1L, 1L, 1L), row.get("revision_deltas"), row.toString());
		assertEquals(3L, row.get("value_changes"), row.toString());
		assertEquals(List.of(true, true, true), row.get("exact_copies"), row.toString());
		List<?> fingerprints = (List<?>) row.get("fingerprints");
		assertNotEquals(fingerprints.get(0), fingerprints.get(1), row.toString());
		assertEquals(fingerprints.get(0), fingerprints.get(2), row.toString());
		assertEquals(fingerprints.get(1), fingerprints.get(3), row.toString());
	}

	/**
	 * Every independent input read by the slices of the model loci, labelled or inline, is
	 * reproduced bit for bit by the reconstruction at the base.
	 */
	@Test
	void everyIndependentInputOfTheModelSlicesIsReproducedExactly() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			Map<String, Object> values = new LinkedHashMap<>();
			for (String label : MODEL_LOCI) {
				values.put(label, compareIndependentInputs(app,
						PreG9BCu0Corpus.locus(app, label)));
			}
			return values;
		});
		int compared = 0;
		for (String label : MODEL_LOCI) {
			Map<?, ?> locus = (Map<?, ?>) row.get(label);
			assertEquals(List.of(), locus.get("mismatches"), row.toString());
			compared += (Integer) locus.get("labelled") + (Integer) locus.get("inline");
		}
		assertTrue(compared > 0, row.toString());
	}

	/**
	 * F-CU2-8 and F-CU2-14: the selection state of each line-conic intersection of the model
	 * slices is not serialized, is initialized again by closeness in the copy, and equals the
	 * live state at the base, including the permutation [1, 0] of {@code J/K}.
	 */
	@Test
	void lineConicSelectionStatesOfTheModelSlicesAreReproduced() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			Map<String, Object> values = new LinkedHashMap<>();
			for (String label : MODEL_LOCI) {
				GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
				Map<String, String> states = new LinkedHashMap<>();
				for (AlgoElement live : sliceAlgorithms(locus)) {
					if (!(live instanceof AlgoIntersectLineConic)) {
						continue;
					}
					GeoElement output = live.getOutput(0);
					XMLStringBuilder xml = new XMLStringBuilder(new StringBuilder());
					live.getXML(xml, false);
					assertFalse(xml.toString().contains("permutation"), xml.toString());
					AlgoIntersectLineConic copy = (AlgoIntersectLineConic) copy(locus,
							output.getLabelSimple()).getParentAlgorithm();
					int[] livePermutation = ((AlgoIntersectLineConic) live)
							.getSelectionPermutation();
					int[] copyPermutation = copy.getSelectionPermutation();
					assertArrayEquals(livePermutation, copyPermutation,
							label + " " + output.getLabelSimple());
					states.put(output.getLabelSimple(), Arrays.toString(livePermutation));
				}
				values.put(label, states);
			}
			return values;
		});
		assertTrue(row.toString().contains("[1, 0]"), row.toString());
	}

	/**
	 * V-REV-EXACT-4 on the complete author model: the slice of every locus, including the
	 * nested composition of {@code e_2}, is validated and faithful, with no failure.
	 */
	@Test
	void everySliceOfTheCompleteModelIsReconstructedFaithfully() throws Exception {
		Map<String, Object> row = PreG9BR6PlusF3Fixtures.onEdt(() -> {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.workingCopy(
					PreG9BCu0Corpus.CEDG, work));
			Map<String, Object> values = new LinkedHashMap<>();
			for (String label : List.of("o", "v", "w", "a_1", "e_1", "o_1", "e_2")) {
				GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
				AlgoDependentPointLocusV2 parent =
						(AlgoDependentPointLocusV2) locus.getParentAlgorithm();
				ReconstructibleLocusEvaluator2D evaluator = (ReconstructibleLocusEvaluator2D)
						field(parent, "evaluator");
				Map<String, Object> state = new LinkedHashMap<>();
				state.put("report", evaluator.getFidelityReport().toString());
				state.put("failures", locus.getInstrumentation().getCertificationWork(
						LocusCertificationWork2D.FIDELITY_FAILURE));
				for (ReconstructionSliceFingerprint2D.Change change
						: ReconstructionSliceFingerprint2D.Change.values()) {
					state.put(change.name(), locus.getInstrumentation()
							.getSliceChanges(change));
				}
				state.put("revision", locus.getSemanticRevision());
				values.put(label, state);
			}
			return values;
		});
		for (Object value : row.values()) {
			Map<?, ?> state = (Map<?, ?>) value;
			assertTrue(state.get("report").toString().startsWith("faithful"), row.toString());
			assertEquals(0L, state.get("failures"), row.toString());
		}
	}

	private static Map<String, Object> compareIndependentInputs(AppGeoCeDG app,
			GeoLocusV2 locus) throws ReflectiveOperationException {
		Construction isolated = PreG9BCu0References.isolatedConstruction(locus);
		List<String> mismatches = new ArrayList<>();
		int labelled = 0;
		for (GeoElement copy : isolated.getGeoSetConstructionOrder()) {
			if (!copy.isIndependent() || !copy.isLabelSet()) {
				continue;
			}
			GeoElement live = app.getKernel().lookupLabel(copy.getLabelSimple());
			if (live == null) {
				continue;
			}
			labelled++;
			if (!exact(live).equals(exact(copy))) {
				mismatches.add(copy.getLabelSimple() + ": live=" + exact(live) + " copy="
						+ exact(copy));
			}
		}
		int inline = 0;
		for (AlgoElement live : sliceAlgorithms(locus)) {
			GeoElement output = live.getOutput(0);
			if (!output.isLabelSet()) {
				continue;
			}
			AlgoElement copy = copy(locus, output.getLabelSimple()).getParentAlgorithm();
			for (int index = 0; index < live.getInput().length; index++) {
				GeoElement input = live.getInput()[index];
				if (!input.isIndependent() || input.isLabelSet()) {
					continue;
				}
				inline++;
				GeoElement copyInput = copy.getInput()[index];
				if (!exact(input).equals(exact(copyInput))) {
					mismatches.add(output.getLabelSimple() + " input " + index + ": live="
							+ exact(input) + " copy=" + exact(copyInput));
				}
			}
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("labelled", labelled);
		result.put("inline", inline);
		result.put("mismatches", mismatches);
		return result;
	}

	private static List<AlgoElement> sliceAlgorithms(GeoLocusV2 locus) {
		List<AlgoElement> algorithms = new ArrayList<>();
		AlgoDependentPointLocusV2 parent = (AlgoDependentPointLocusV2) locus
				.getParentAlgorithm();
		for (GeoElement geo : parent.getReconstructionSliceGeos()) {
			AlgoElement algorithm = geo.getParentAlgorithm();
			if (algorithm != null && !algorithms.contains(algorithm)) {
				algorithms.add(algorithm);
			}
		}
		return algorithms;
	}

	private static String fingerprint(AppGeoCeDG app, GeoLocusV2 locus, String driver) {
		GeoElement root = app.getKernel().lookupLabel(driver);
		return ReconstructionSliceFingerprint2D.compute(
				((AlgoDependentPointLocusV2) locus.getParentAlgorithm())
						.getReconstructionSliceGeos(), List.of(root, root));
	}

	private static GeoElement copy(GeoLocusV2 locus, String label)
			throws ReflectiveOperationException {
		for (GeoElement geo : PreG9BCu0References.isolatedConstruction(locus)
				.getGeoSetConstructionOrder()) {
			if (label.equals(geo.getLabelSimple())) {
				return geo;
			}
		}
		throw new IllegalStateException("No slice copy of " + label);
	}

	private static String exact(GeoElement geo) {
		if (geo instanceof GeoNumeric number) {
			return "num:" + Double.toHexString(number.getValue());
		}
		if (geo instanceof GeoPoint point) {
			return "pt:" + Double.toHexString(point.getX()) + ","
					+ Double.toHexString(point.getY()) + "," + Double.toHexString(point.getZ());
		}
		if (geo instanceof GeoVec3D vector) {
			return "vec:" + Double.toHexString(vector.getX()) + ","
					+ Double.toHexString(vector.getY()) + ","
					+ Double.toHexString(vector.getZ());
		}
		XMLStringBuilder xml = new XMLStringBuilder(new StringBuilder());
		geo.getXML(false, xml);
		return geo.getClass().getSimpleName() + ":" + xml;
	}

	private static Object field(Object target, String name)
			throws ReflectiveOperationException {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(target);
			} catch (NoSuchFieldException absent) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}
}
