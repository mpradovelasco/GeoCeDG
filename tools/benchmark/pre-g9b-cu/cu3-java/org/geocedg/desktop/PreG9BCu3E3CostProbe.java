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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.ConstructiveCone;
import org.geocedg.common.kernel.locus.constructive.ConstructiveConeEvaluator;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityBudget;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertifier;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCounters;
import org.geocedg.common.kernel.locus.constructive.ConstructiveResidualJetSource;
import org.geocedg.common.kernel.locus.constructive.Cu2CertificateTransfer;
import org.geocedg.common.kernel.locus.constructive.ExactEvaluationBudget;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geocedg.common.kernel.locus.intersection.ExactContactEvidence2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionExistenceEvidence2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionSolution2D;
import org.geocedg.common.kernel.locus.intersection.ResidualJetSource2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 increment E3, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-e3-cost.ps1}: the cost of the exact decisions of the
 * conflict rule in the constructive configuration on the author model, against the provisional
 * ceilings of {@code constructive-identity-certificate-budget/v1}: the constructive identity
 * certificate of each contact with the driver at its exact parameter, with its counters, order of
 * contact and signs, and the fold of {@code CU2} at that parameter; the exact contacts as the
 * residual source of a query gives them; the injection of every limit; the repetition of every
 * attempt; and the wall time of the adoption and of a drag that rechecks, with the same steps
 * under the legacy policy as a reference. The kernel enforces the budget, this bench only measures
 * it. Timings are informative and never part of a decision. It reads copies of the author model
 * only, assigns no verdict and is not part of any source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-e3-cost")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3E3CostProbe {

	private static final int TIMING_RUNS = 5;
	private static final String[] QUARTERS = {"0", "pi/2", "pi", "-pi/2"};
	/** Locus, target and quarter turn of the exact parameter of each contact of the model. */
	private static final Object[][] CONTACTS = {{"e_1", "n", 0}, {"e_1", "c_1", 0},
			{"o_1", "h_2", 0}, {"e_1", "c_1", 2}, {"e_1", "r", 2}};
	private static final String[] RESULTS = {"u_1=Intersect(o_1,h_2)",
			"cu3X1=Intersect(e_1,n)", "cu3X2=Intersect(e_1,c_1)", "cu3X3=Intersect(e_1,r)"};

	private final List<ConstructiveIdentityCounters> measured = new ArrayList<>();

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-e3-cost");
		root.put("status", "CU3 E3 BENCH EVIDENCE OF THE EXACT DECISIONS - NO VERDICT OF ITS OWN");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner", "tools/benchmark/pre-g9b-cu/run-cu3-e3-cost.ps1");
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.getBoolean("geocedg.cu0.productDirty"));
		root.put("generator", generator);
		Map<String, Object> budget = new LinkedHashMap<>();
		budget.put("version", ConstructiveIdentityBudget.VERSION);
		budget.put("term_products", ConstructiveIdentityBudget.MAXIMUM_TERM_PRODUCTS);
		budget.put("indeterminates", ConstructiveIdentityBudget.MAXIMUM_INDETERMINATES);
		budget.put("terms", ConstructiveIdentityBudget.MAXIMUM_TERMS);
		budget.put("bits", ConstructiveIdentityBudget.MAXIMUM_BITS);
		budget.put("status", "provisional initial ceilings (approval record section 16)");
		root.put("budget", budget);
		root.put("timing_runs", TIMING_RUNS);
		root.put("exact_decisions", onEdt(() -> decisions(PreG9BCu0Corpus.open(
				PreG9BCu0Corpus.withoutE2(work)))));
		root.put("document", onEdt(this::document));
		root.put("largest_observed", largest());
		Files.writeString(output.resolve("cu3-e3-cost.json"), PreG9BCu0References.json(root),
				StandardCharsets.UTF_8);
	}

	// ---------------------------------------------------------------- exact decisions

	private Map<String, Object> decisions(AppGeoCeDG app) throws Exception {
		Construction construction = app.getKernel().getConstruction();
		AuthorityPolicyAdoption.adopt(construction);
		Map<String, Object> rows = new LinkedHashMap<>();
		for (Object[] contact : CONTACTS) {
			String locus = (String) contact[0];
			String target = (String) contact[1];
			int quarter = (Integer) contact[2];
			Map<String, Object> row = new LinkedHashMap<>();
			Callable<ConstructiveIdentityCertificate> attempt = () -> attempt(construction,
					locus, target, quarter, ConstructiveIdentityBudget.V1);
			row.put("certificate", measure(attempt.call(), attempt));
			row.put("fold", String.valueOf(fold(construction, locus, target, quarter)));
			double center = quarter == 0 ? 0 : Math.PI;
			ResidualJetSource2D source = ConstructiveResidualJetSource.capture(
					(GeoLocusV2) construction.lookupLabel(locus),
					construction.lookupLabel(target));
			List<String> contacts = new ArrayList<>();
			if (source != null) {
				for (ExactContactEvidence2D exact : source.exactContacts(center - 1E-6,
						center + 1E-6)) {
					contacts.add(exact.getEvidence() + " " + exact.getOrder() + " at "
							+ exact.getParameter() + " [" + exact.getLower() + ","
							+ exact.getUpper() + "] curvature " + exact.getCurvature());
				}
			}
			row.put("exact_contacts_of_the_source", contacts);
			rows.put(locus + " " + target + " at " + QUARTERS[quarter], row);
		}
		Map<String, Object> injections = new LinkedHashMap<>();
		injections.put("term products 1000", describe(attempt(construction, "e_1", "c_1", 2,
				ConstructiveIdentityBudget.restrictedTo(1000, 16, 200_000, 4096))));
		injections.put("indeterminates 4", describe(attempt(construction, "e_1", "c_1", 2,
				ConstructiveIdentityBudget.restrictedTo(16_000_000L, 4, 200_000, 4096))));
		injections.put("terms 50", describe(attempt(construction, "e_1", "c_1", 2,
				ConstructiveIdentityBudget.restrictedTo(16_000_000L, 16, 50, 4096))));
		injections.put("bits 8", describe(attempt(construction, "e_1", "c_1", 2,
				ConstructiveIdentityBudget.restrictedTo(16_000_000L, 16, 200_000, 8))));
		rows.put("budget injections on e_1 c_1 at pi", injections);
		return rows;
	}

	private static ConstructiveIdentityCertificate attempt(Construction construction,
			String locusLabel, String targetLabel, int quarter,
			ConstructiveIdentityBudget budget) {
		Slice slice = slice(construction, locusLabel, targetLabel);
		return ConstructiveIdentityCertifier.incidenceAtParameter(locusLabel + " on "
				+ targetLabel + " at " + QUARTERS[quarter], slice.cone, slice.traced,
				slice.target, slice.support, quarter, slice.frame, budget);
	}

	private static Cu2CertificateTransfer.Record fold(Construction construction,
			String locusLabel, String targetLabel, int quarter) {
		Slice slice = slice(construction, locusLabel, targetLabel);
		return Cu2CertificateTransfer.foldAtParameter(
				(GeoLocusV2) construction.lookupLabel(locusLabel), slice.cone, slice.traced,
				slice.support, ConstructiveConeEvaluator.exact(slice.cone,
						ExactEvaluationBudget.V1), QUARTERS[quarter]);
	}

	/** The slice cone of a locus and a target, as the residual source of a query builds it. */
	private static Slice slice(Construction construction, String locusLabel,
			String targetLabel) {
		GeoLocusV2 locus = (GeoLocusV2) construction.lookupLabel(locusLabel);
		AlgoElement parent = locus.getParentAlgorithm();
		GeoPoint driver = (GeoPoint) parent.getInput(1);
		ConstructiveCone.Builder builder = ConstructiveCone.builder(construction);
		Slice slice = new Slice();
		slice.target = builder.add(construction.lookupLabel(targetLabel));
		slice.support = builder.add(driver.getPath().toGeoElement());
		slice.traced = builder.addSlice(parent.getInput(0).toGeoElement(), driver);
		slice.cone = builder.build();
		ConstructionBoxModel2D model = ConstructionBoxModel2D.capture(
				locus.getSemanticDefinition());
		slice.frame = new double[] {model.getProgram().getDriverConstant(2),
				model.getProgram().getDriverConstant(3), model.getProgram().getDriverConstant(4),
				model.getProgram().getDriverConstant(5)};
		return slice;
	}

	/** The cone and the nodes of one slice. */
	private static final class Slice {
		private ConstructiveCone cone;
		private int target;
		private int support;
		private int traced;
		private double[] frame;
	}

	// ---------------------------------------------------------------- whole document

	/**
	 * The adoption of the model with the four public intersections of the contacts and a drag of
	 * {@code H} that keeps the cell, with the evidence of the published solutions; the same steps
	 * under the legacy policy give the reference times.
	 */
	private Map<String, Object> document() throws Exception {
		Map<String, Object> rows = new LinkedHashMap<>();
		List<Long> adoption = new ArrayList<>();
		List<Long> constructiveDrag = new ArrayList<>();
		List<Long> legacyDrag = new ArrayList<>();
		Map<String, Object> evidence = null;
		for (int run = 0; run < 3; run++) {
			AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
			for (String command : RESULTS) {
				if (!command.startsWith("u_1")) {
					PreG9BR6PlusF3Fixtures.must(app, command);
				}
			}
			legacyDrag.add(drag(app));
			long start = System.nanoTime();
			AuthorityPolicyAdoption.adopt(app.getKernel().getConstruction());
			adoption.add(System.nanoTime() - start);
			constructiveDrag.add(drag(app));
			Map<String, Object> published = published(app);
			if (evidence != null && !evidence.equals(published)) {
				rows.put("nondeterministic_evidence", published);
			}
			evidence = published;
		}
		rows.put("published_after_adoption", evidence);
		rows.put("median_adoption_ms", median(adoption));
		rows.put("median_drag_of_H_legacy_ms", median(legacyDrag));
		rows.put("median_drag_of_H_constructive_ms", median(constructiveDrag));
		return rows;
	}

	/** Moves {@code H} on its path and back, and returns the time of the first move. */
	private static long drag(AppGeoCeDG app) {
		double h = PreG9BCu0Corpus.pathParameter(app, "H");
		long start = System.nanoTime();
		PreG9BCu0Corpus.movePathPoint(app, "H", h + 0.5);
		long elapsed = System.nanoTime() - start;
		PreG9BCu0Corpus.movePathPoint(app, "H", h);
		return elapsed;
	}

	private static Map<String, Object> published(AppGeoCeDG app) {
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String command : RESULTS) {
			String label = command.substring(0, command.indexOf('='));
			GeoElement geo = app.getKernel().lookupLabel(label);
			if (!(geo instanceof GeoLocusIntersectionResult)) {
				continue;
			}
			GeoLocusIntersectionResult result = (GeoLocusIntersectionResult) geo;
			List<String> solutions = new ArrayList<>();
			for (LocusIntersectionSolution2D solution : result.getIntersectionResult()
					.getFiniteSolutions()) {
				IntersectionExistenceEvidence2D existence = solution.getRevisionEvidence()
						.getExistenceEvidence();
				solutions.add(solution.getRevisionEvidence().getSemanticParameter() + " "
						+ solution.getClassification().getContactClass() + " "
						+ existence.getGrade() + " " + existence.getContactEvidence() + " "
						+ existence.getContactOrder() + " admissible "
						+ result.isPointAdmissible(solution.getIdentity().getRootToken()));
			}
			rows.put(command, solutions);
		}
		return rows;
	}

	// ---------------------------------------------------------------- measurement

	/**
	 * The certificate, then fresh attempts of the same revision: identical results and counters,
	 * and the median wall time of {@value #TIMING_RUNS} runs.
	 */
	private Map<String, Object> measure(ConstructiveIdentityCertificate certificate,
			Callable<ConstructiveIdentityCertificate> fresh) {
		Map<String, Object> row = describe(certificate);
		try {
			List<Long> times = new ArrayList<>();
			boolean deterministic = true;
			for (int run = 0; run < TIMING_RUNS; run++) {
				long start = System.nanoTime();
				ConstructiveIdentityCertificate again = fresh.call();
				times.add(System.nanoTime() - start);
				deterministic &= describe(again).equals(row);
			}
			row.put("deterministic_over_runs", deterministic);
			row.put("median_wall_time_ms", median(times));
		} catch (Exception failure) {
			row.put("repetition_failure", failure.toString());
		}
		return row;
	}

	private Map<String, Object> describe(ConstructiveIdentityCertificate certificate) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("status", certificate.getStatus().name());
		row.put("cause", certificate.getCause().name());
		row.put("cuts", certificate.getCuts());
		row.put("indeterminates", certificate.getIndeterminateNames());
		row.put("cell_conditions", certificate.getCellSize());
		row.put("contact_order", certificate.getContactOrder() == null ? null
				: certificate.getContactOrder().name());
		if (certificate.getContactOrder() != null) {
			row.put("residual_first_derivative_sign",
					certificate.getResidualDerivativeSign(1));
			row.put("residual_second_derivative_sign",
					certificate.getResidualDerivativeSign(2));
		}
		ConstructiveIdentityCounters counters = certificate.getCounters();
		if (counters != null) {
			measured.add(counters);
			Map<String, Object> work = new LinkedHashMap<>();
			work.put("term_products", counters.getTermProducts());
			work.put("indeterminates", counters.getIndeterminates());
			work.put("largest_terms", counters.getLargestTerms());
			work.put("largest_bits", counters.getLargestBits());
			work.put("budget", counters.getBudget().toString());
			row.put("counters", work);
		}
		return row;
	}

	/** The largest value of every counter over the attempts with the fixed budget. */
	private Map<String, Object> largest() {
		long products = 0;
		int indeterminates = 0;
		int terms = 0;
		int bits = 0;
		for (ConstructiveIdentityCounters counters : measured) {
			if (!counters.getBudget().isFixed()) {
				continue;
			}
			products = Math.max(products, counters.getTermProducts());
			indeterminates = Math.max(indeterminates, counters.getIndeterminates());
			terms = Math.max(terms, counters.getLargestTerms());
			bits = Math.max(bits, counters.getLargestBits());
		}
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("term_products", products);
		row.put("indeterminates", indeterminates);
		row.put("terms", terms);
		row.put("bits", bits);
		return row;
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
