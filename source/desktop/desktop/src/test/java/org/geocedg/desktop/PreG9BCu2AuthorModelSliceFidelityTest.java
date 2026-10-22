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

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geocedg.common.kernel.algos.AlgoDependentPointLocusV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.CertifiedConstructionProgram2D;
import org.geocedg.common.kernel.locus.LocusCertificationWork2D;
import org.geocedg.common.kernel.locus.ReconstructibleLocusEvaluator2D;
import org.geocedg.common.kernel.locus.ReconstructionSliceFingerprint2D;
import org.geocedg.common.kernel.locus.intersection.HybridExistenceCertifier2D;
import org.geocedg.common.kernel.spatial.identity.PersistentGeoId;
import org.geocedg.common.kernel.spatial.identity.SpatialIdentityRegistry;
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
 * (V-REV-EXACT-4). The corrective descendant T13-R2 (decision D-CU2-T13-R2) adds the comparison
 * of the states of V-REV-EXACT-1 and V-REV-EXACT-2 with the model set from scratch
 * (V-REV-EXACT-5). The isolated slices are read reflectively.
 */
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu2AuthorModelSliceFidelityTest {

	/** Penetration and bite sides of the critical value of {@code excCyl}, one ulp apart. */
	private static final double PENETRATION = Double.longBitsToDouble(0x400d416bb67a38bbL);
	private static final double BITE = Double.longBitsToDouble(0x400d416bb67a38bcL);
	private static final List<String> MODEL_LOCI = List.of("o", "v", "w", "a_1", "e_1", "o_1");
	/** Loci of the model that read {@code excCyl}. */
	private static final List<String> FIRST_FAMILY = List.of("o", "v", "w", "a_1");
	/** {@code t_c} of the matrix (section 1), where the traced point is undefined. */
	private static final double T_C = 1.2767651295;

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

	/**
	 * V-REV-EXACT-5 on the states of V-REV-EXACT-1 and V-REV-EXACT-2 with the driver at
	 * {@code t = -1}: every visit of the alternation {@code bb -> bc -> bb -> bc} of
	 * {@code excCyl} has the fingerprint, the reconstructed slice, the captured program and the
	 * certificate of the model opened anew, with the driver at the same parameter and
	 * {@code excCyl} set once to the visited value, bit for bit, for every locus that reads it:
	 * the first family and the two variants of matrix section 3.5c added to the model,
	 * {@code lq} and {@code lr} of its labelled outputs and {@code lr2} of an unlabelled second
	 * output, whose traced point is undefined at {@code t_c} in the bite.
	 * Durable identities, which the variants of each document allocate anew, are named by the
	 * labels of their geos, or by the algorithm and output index of an unlabelled one.
	 */
	@Test
	void everyStateWithTheDriverAwayFromTheDoubleRootMatchesTheModelSetFromScratch()
			throws Exception {
		assertEquals(List.of(), statesAgainstScratch(-1.0));
	}

	/** V-REV-EXACT-5 as above, with the driver at {@code t_c}. */
	@Test
	void everyStateWithTheDriverAtTheDoubleRootMatchesTheModelSetFromScratch()
			throws Exception {
		assertEquals(List.of(), statesAgainstScratch(T_C));
	}

	/** @return every visit whose artifacts differ from those of the model set from scratch */
	private List<String> statesAgainstScratch(double driver) throws Exception {
		File model = PreG9BCu0Corpus.withoutE2(work);
		return PreG9BR6PlusF3Fixtures.onEdt(() -> {
			Map<Double, String> scratch = new LinkedHashMap<>();
			for (double value : new double[] {PENETRATION, BITE}) {
				scratch.put(value, artifacts(state(model, driver, value)));
			}
			// The first visit is the state that the live model starts from.
			AppGeoCeDG live = state(model, driver, PENETRATION);
			double[] visits = {PENETRATION, BITE, PENETRATION, BITE};
			List<String> problems = new ArrayList<>();
			for (int index = 0; index < visits.length; index++) {
				if (index > 0) {
					PreG9BCu0Corpus.setNumber(live, "excCyl", visits[index]);
				}
				String difference = firstDifference(scratch.get(visits[index]),
						artifacts(live));
				if (difference != null) {
					problems.add("t=" + driver + " visit " + index + " ("
							+ (visits[index] == PENETRATION ? "bb" : "bc") + "): " + difference);
				}
				if (driver == T_C && visits[index] == BITE
						&& live.getKernel().lookupLabel("R2").isDefined()) {
					problems.add("visit " + index + ": R2 is defined at t_c in the bite");
				}
			}
			return problems;
		});
	}

	/**
	 * Opens the model anew, adds the variants of matrix section 3.5c with the driver at
	 * {@code t = -1}, where every output is defined, then moves the driver and sets
	 * {@code excCyl} once.
	 */
	private static AppGeoCeDG state(File model, double driver, double value) {
		AppGeoCeDG app = PreG9BCu0Corpus.open(model);
		PreG9BCu0Corpus.movePathPoint(app, "N", -1);
		for (String command : List.of("lq=LocusV2(Q,N)", "lr=LocusV2(R,N)", "t2=Ray(P,N)",
				"R2=Intersect(p,t2,2)", "lr2=LocusV2(R2,N)")) {
			app.getKernel().getAlgebraProcessor().processAlgebraCommand(command, false);
		}
		PreG9BCu0Corpus.movePathPoint(app, "N", driver);
		PreG9BCu0Corpus.setNumber(app, "excCyl", value);
		return app;
	}

	/**
	 * @return for each locus that reads {@code excCyl}: fingerprint of the live slice,
	 *         fingerprint of the reconstructed copy and its fidelity, signature of the captured
	 *         class-v3 program or its refusal, and structure, roots and exact evidence of the
	 *         certificate, with durable identities named structurally
	 */
	private static String artifacts(AppGeoCeDG app) throws ReflectiveOperationException {
		GeoElement driver = app.getKernel().lookupLabel("N");
		StringBuilder text = new StringBuilder();
		for (String label : List.of("o", "v", "w", "a_1", "lq", "lr", "lr2")) {
			GeoLocusV2 locus = PreG9BCu0Corpus.locus(app, label);
			AlgoDependentPointLocusV2 parent =
					(AlgoDependentPointLocusV2) locus.getParentAlgorithm();
			ReconstructibleLocusEvaluator2D evaluator =
					(ReconstructibleLocusEvaluator2D) field(parent, "evaluator");
			Construction copy = PreG9BCu0References.isolatedConstruction(locus);
			GeoElement copyDriver = copy.lookupLabel("N");
			text.append(label).append(" fingerprint=").append(
					ReconstructionSliceFingerprint2D.compute(parent.getReconstructionSliceGeos(),
							List.of(driver, driver)))
					.append('\n').append(label).append(" slice=").append(
							ReconstructionSliceFingerprint2D.compute(
									copy.getGeoSetConstructionOrder(),
									List.of(copyDriver, copyDriver)))
					.append(" faithful=").append(evaluator.isReconstructionFaithful())
					.append('\n').append(label).append(" program=").append(evaluator
							.captureCertifiedProgramV3()
							.map(CertifiedConstructionProgram2D::getSignature)
							.orElse("refused " + evaluator.getProgramV3Refusal()));
			HybridExistenceCertifier2D.Result result = parent.getHybridExistenceCertificate();
			text.append('\n').append(label).append(" certificate=");
			if (result == null) {
				text.append("none");
			} else {
				text.append(result.getStructure().getSemanticSignature());
				for (HybridExistenceCertifier2D.CertifiedRoot root : result.getRoots()) {
					text.append("|root=").append(root.getSignature());
				}
				for (HybridExistenceCertifier2D.ExactEvidence evidence
						: result.getExactEvidence()) {
					text.append("|exact=").append(evidence.getSignature());
				}
			}
			text.append('\n');
		}
		return named(text.toString(), app.getKernel().getConstruction());
	}

	/**
	 * @return the text with every durable identity replaced by the label of its geo, or by the
	 *         algorithm, labelled inputs and output index of an unlabelled one
	 */
	static String named(String text, Construction construction) {
		SpatialIdentityRegistry registry = construction.getSpatialIdentityRegistry();
		Matcher matcher = Pattern.compile("geo:[0-9a-f]{32}").matcher(text);
		StringBuilder named = new StringBuilder();
		while (matcher.find()) {
			GeoElement geo = registry.getGeo(PersistentGeoId.parse(matcher.group()));
			matcher.appendReplacement(named, Matcher.quoteReplacement(geo == null
					? matcher.group() : structuralName(geo)));
		}
		matcher.appendTail(named);
		return named.toString();
	}

	private static String structuralName(GeoElement geo) {
		if (geo.isLabelSet()) {
			return geo.getLabelSimple();
		}
		AlgoElement parent = geo.getParentAlgorithm();
		if (parent == null) {
			return "unlabelled-free";
		}
		StringBuilder name = new StringBuilder(parent.getClass().getSimpleName()).append('(');
		for (GeoElement input : parent.getInput()) {
			name.append(input.isLabelSet() ? input.getLabelSimple() : "?").append(',');
		}
		return name.append(")#").append(Arrays.asList(parent.getOutput()).indexOf(geo))
				.toString();
	}

	/** @return the first line that differs, or {@code null} */
	private static String firstDifference(String expected, String actual) {
		String[] left = expected.split("\n", -1);
		String[] right = actual.split("\n", -1);
		for (int index = 0; index < Math.max(left.length, right.length); index++) {
			String scratch = index < left.length ? left[index] : "";
			String visited = index < right.length ? right[index] : "";
			if (!scratch.equals(visited)) {
				return "scratch <" + abbreviate(scratch) + "> visited <" + abbreviate(visited)
						+ ">";
			}
		}
		return null;
	}

	private static String abbreviate(String line) {
		return line.length() <= 400 ? line : line.substring(0, 400) + "...";
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
