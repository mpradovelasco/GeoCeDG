/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.DocumentDecisionConfiguration;
import org.geocedg.common.kernel.locus.intersection.ExactContactEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionDecisionConfiguration2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionTarget2D;
import org.geocedg.common.kernel.locus.intersection.PublicTargetIntersectionCapability2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetEnclosure2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetSource2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 author disposition of 2026-10-26, item 5, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-performance-characterization.ps1}: where the time of
 * a drag of {@code H} goes in the adopted author model, to ground optimization proposals that
 * keep every geometric guarantee. For each public intersection of the five contacts it builds an
 * internal query with the decision configuration of the document, wrapped to time and count the
 * work asked of the residual source: the capture of the source, the box enclosures of
 * {@code h}, {@code h'} and {@code h''} and the exact-contact queries. It records that work for
 * the drag of {@code H} and for a recomputation in the same revision, the boxes that the three
 * queries of {@code e_1} share, and the drag time of the document with the same commands as the
 * E3 bench as a reference. Counters are deterministic; timings are informative. It changes
 * nothing in the product, reads copies of the author model only, assigns no verdict and is not
 * part of any source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-performance-characterization")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3PerformanceCharacterizationProbe {

	private static final int RUNS = 5;
	private static final String[][] PAIRS = {{"o_1", "h_2"}, {"e_1", "n"}, {"e_1", "c_1"},
			{"e_1", "r"}};
	private static final String[] RESULTS = {"cu3X1=Intersect(e_1,n)",
			"cu3X2=Intersect(e_1,c_1)", "cu3X3=Intersect(e_1,r)"};

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-performance-characterization");
		root.put("status", "CU3 PERFORMANCE CHARACTERIZATION - NO VERDICT OF ITS OWN; TIMINGS ARE"
				+ " INFORMATIVE");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner",
				"tools/benchmark/pre-g9b-cu/run-cu3-performance-characterization.ps1");
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.getBoolean("geocedg.cu0.productDirty"));
		root.put("generator", generator);
		root.put("runs", RUNS);
		root.put("document_drag_reference", onEdt(this::reference));
		root.put("instrumented_queries", onEdt(this::instrumented));
		Files.writeString(output.resolve("cu3-performance-characterization.json"),
				PreG9BCu0References.json(root), StandardCharsets.UTF_8);
	}

	// ---------------------------------------------------------------- reference

	/** The drag of H in the adopted document with the commands of the E3 bench. */
	private Map<String, Object> reference() throws Exception {
		AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
		for (String command : RESULTS) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		List<Long> legacy = new ArrayList<>();
		for (int run = 0; run < RUNS; run++) {
			legacy.add(drag(app));
		}
		AuthorityPolicyAdoption.adopt(app.getKernel().getConstruction());
		List<Long> constructive = new ArrayList<>();
		for (int run = 0; run < RUNS; run++) {
			constructive.add(drag(app));
		}
		Map<String, Object> rows = new LinkedHashMap<>();
		rows.put("median_drag_of_H_legacy_ms", median(legacy));
		rows.put("median_drag_of_H_constructive_ms", median(constructive));
		return rows;
	}

	/** Moves H on its path and back, and returns the time of the first move. */
	private static long drag(AppGeoCeDG app) {
		double h = PreG9BCu0Corpus.pathParameter(app, "H");
		long start = System.nanoTime();
		PreG9BCu0Corpus.movePathPoint(app, "H", h + 0.5);
		long elapsed = System.nanoTime() - start;
		PreG9BCu0Corpus.movePathPoint(app, "H", h);
		return elapsed;
	}

	// ---------------------------------------------------------------- instrumented

	/**
	 * Internal queries of the four contact intersections under the constructive policy, each
	 * with a timing wrapper of the document configuration; then the drags of H.
	 */
	private Map<String, Object> instrumented() throws Exception {
		AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
		Construction construction = app.getKernel().getConstruction();
		AuthorityPolicyAdoption.adopt(construction);
		Map<String, Timing> timings = new LinkedHashMap<>();
		Map<String, AlgoLocusIntersectionV2> queries = new LinkedHashMap<>();
		for (String[] pair : PAIRS) {
			GeoLocusV2 locus = (GeoLocusV2) construction.lookupLabel(pair[0]);
			GeoElement target = construction.lookupLabel(pair[1]);
			Timing timing = new Timing(new DocumentDecisionConfiguration(construction, locus,
					target));
			String key = pair[0] + " " + pair[1];
			timings.put(key, timing);
			queries.put(key, new AlgoLocusIntersectionV2(construction, locus, target,
					"perf-pair", "perf-lineage", "perf-target", "perf-topology",
					new PublicTargetIntersectionCapability2D(timing), new GeoElement[0]));
		}
		Map<String, List<Map<String, Object>>> duringDrag = new LinkedHashMap<>();
		Map<String, List<Map<String, Object>>> recompute = new LinkedHashMap<>();
		Map<String, List<Long>> recomputeTotal = new LinkedHashMap<>();
		Map<String, Set<String>> boxes = new LinkedHashMap<>();
		for (String key : queries.keySet()) {
			duringDrag.put(key, new ArrayList<>());
			recompute.put(key, new ArrayList<>());
			recomputeTotal.put(key, new ArrayList<>());
		}
		for (int run = 0; run < RUNS; run++) {
			double h = PreG9BCu0Corpus.pathParameter(app, "H");
			for (Timing timing : timings.values()) {
				timing.reset();
			}
			PreG9BCu0Corpus.movePathPoint(app, "H", h + 0.5);
			for (Map.Entry<String, Timing> entry : timings.entrySet()) {
				duringDrag.get(entry.getKey()).add(entry.getValue().take());
			}
			for (Map.Entry<String, AlgoLocusIntersectionV2> entry : queries.entrySet()) {
				Timing timing = timings.get(entry.getKey());
				timing.reset();
				timing.recordBoxes = run == 0;
				long start = System.nanoTime();
				entry.getValue().compute();
				recomputeTotal.get(entry.getKey()).add(System.nanoTime() - start);
				recompute.get(entry.getKey()).add(timing.take());
				if (run == 0) {
					boxes.put(entry.getKey(), new LinkedHashSet<>(timing.boxes));
				}
				timing.recordBoxes = false;
			}
			PreG9BCu0Corpus.movePathPoint(app, "H", h);
		}
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String key : queries.keySet()) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("during_the_drag_of_H", summarize(duringDrag.get(key)));
			Map<String, Object> again = summarize(recompute.get(key));
			again.put("median_total_compute_ms", median(recomputeTotal.get(key)));
			row.put("recomputation_in_the_same_revision", again);
			rows.put(key, row);
		}
		rows.put("boxes_shared_by_the_queries_of_e_1", shared(boxes));
		return rows;
	}

	/** Deterministic counts of the first run and median timings over the runs. */
	private static Map<String, Object> summarize(List<Map<String, Object>> runs) {
		Map<String, Object> first = runs.get(0);
		Map<String, Object> row = new LinkedHashMap<>();
		boolean deterministic = true;
		for (String counter : new String[] {"residual_sources", "box_enclosures",
				"exact_contact_queries", "exact_contacts", "normalized_residuals"}) {
			row.put(counter, first.get(counter));
			for (Map<String, Object> run : runs) {
				deterministic &= first.get(counter).equals(run.get(counter));
			}
		}
		row.put("counts_identical_over_runs", deterministic);
		for (String time : new String[] {"capture_ns", "enclose_ns", "exact_contacts_ns"}) {
			List<Long> values = new ArrayList<>();
			for (Map<String, Object> run : runs) {
				values.add((Long) run.get(time));
			}
			row.put("median_" + time.replace("_ns", "_ms"), median(values));
		}
		return row;
	}

	private static Map<String, Object> shared(Map<String, Set<String>> boxes) {
		Map<String, Object> rows = new LinkedHashMap<>();
		List<String> queries = new ArrayList<>();
		for (String key : boxes.keySet()) {
			if (key.startsWith("e_1 ")) {
				queries.add(key);
				rows.put("distinct_boxes " + key, boxes.get(key).size());
			}
		}
		for (int i = 0; i < queries.size(); i++) {
			for (int j = i + 1; j < queries.size(); j++) {
				Set<String> common = new LinkedHashSet<>(boxes.get(queries.get(i)));
				common.retainAll(boxes.get(queries.get(j)));
				rows.put("common " + queries.get(i) + " / " + queries.get(j), common.size());
			}
		}
		Set<String> union = new LinkedHashSet<>();
		int total = 0;
		for (String key : queries) {
			union.addAll(boxes.get(key));
			total += boxes.get(key).size();
		}
		rows.put("distinct_boxes_of_the_three_queries", union.size());
		rows.put("sum_of_their_distinct_boxes", total);
		return rows;
	}

	/** Times and counts the residual sources of one query and the work asked of them. */
	private static final class Timing implements IntersectionDecisionConfiguration2D {
		private final IntersectionDecisionConfiguration2D delegate;
		private int sources;
		private int enclosures;
		private int residuals;
		private int exactQueries;
		private int exactContacts;
		private long captureNanos;
		private long encloseNanos;
		private long exactNanos;
		private boolean recordBoxes;
		private final List<String> boxes = new ArrayList<>();

		Timing(IntersectionDecisionConfiguration2D delegate) {
			this.delegate = delegate;
		}

		@Override
		public ResidualJetSource2D residualJets(LocusDefinition2D definition,
				LocusIntersectionTarget2D target) {
			long start = System.nanoTime();
			ResidualJetSource2D source = delegate.residualJets(definition, target);
			captureNanos += System.nanoTime() - start;
			if (source == null) {
				return null;
			}
			sources++;
			return new ResidualJetSource2D() {
				@Override
				public String getConfiguration() {
					return source.getConfiguration();
				}

				@Override
				public boolean isConstructive() {
					return source.isConstructive();
				}

				@Override
				public ResidualJetEnclosure2D enclose(double lower, double upper) {
					enclosures++;
					if (recordBoxes) {
						boxes.add(lower + ":" + upper);
					}
					long begin = System.nanoTime();
					ResidualJetEnclosure2D enclosure = source.enclose(lower, upper);
					encloseNanos += System.nanoTime() - begin;
					return enclosure;
				}

				@Override
				public double[] normalizedResidual(double parameter) {
					residuals++;
					return source.normalizedResidual(parameter);
				}

				@Override
				public List<ExactContactEvidence2D> exactContacts(double lower, double upper) {
					exactQueries++;
					long begin = System.nanoTime();
					List<ExactContactEvidence2D> contacts = source.exactContacts(lower, upper);
					exactNanos += System.nanoTime() - begin;
					exactContacts += contacts.size();
					return contacts;
				}
			};
		}

		@Override
		public boolean isConstructive() {
			return delegate.isConstructive();
		}

		void reset() {
			sources = 0;
			enclosures = 0;
			residuals = 0;
			exactQueries = 0;
			exactContacts = 0;
			captureNanos = 0;
			encloseNanos = 0;
			exactNanos = 0;
			boxes.clear();
		}

		Map<String, Object> take() {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("residual_sources", sources);
			row.put("box_enclosures", enclosures);
			row.put("exact_contact_queries", exactQueries);
			row.put("exact_contacts", exactContacts);
			row.put("normalized_residuals", residuals);
			row.put("capture_ns", captureNanos);
			row.put("enclose_ns", encloseNanos);
			row.put("exact_contacts_ns", exactNanos);
			return row;
		}
	}

	private static double median(List<Long> nanos) {
		List<Long> sorted = new ArrayList<>(nanos);
		Collections.sort(sorted);
		return sorted.get(sorted.size() / 2) / 1E6;
	}

	private static <T> T onEdt(Callable<T> action) throws Exception {
		return PreG9BR6PlusF3Fixtures.onEdt(action);
	}
}
