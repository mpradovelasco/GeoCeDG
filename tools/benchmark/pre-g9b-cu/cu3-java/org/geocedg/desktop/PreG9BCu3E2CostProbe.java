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

import org.geocedg.common.kernel.algos.AlgoLocusDriverCorrespondencePoint2D;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.ConstructiveCone;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityBudget;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertifier;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCounters;
import org.geocedg.common.kernel.locus.constructive.ExactEvaluationBudget;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolution;
import org.geocedg.common.kernel.locus.constructive.StructuralPointResolver;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 gate G-AUT-8, increment E2, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-e2-cost.ps1}: the deterministic counters of every
 * attempt of {@code constructive-identity-certificate/v1} against the provisional ceilings of
 * {@code constructive-identity-certificate-budget/v1} for the five mandatory contacts of the
 * author model in {@code Intersect(L, r, D)} and in both scopes, for the controls, the synthetic
 * constructions S1 to S3 and a construction with an independent second root; the injection of
 * every limit; the repetition of every attempt; and the drags that recheck a proof. The kernel
 * enforces the budget, this bench only measures it, and a limit shown insufficient is presented
 * for an author decision, never raised. Timings are informative and never part of a decision.
 * It reads copies of the author model and synthetic constructions only, assigns no verdict and
 * is not part of any source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-e2-cost")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3E2CostProbe {

	private static final int TIMING_RUNS = 5;
	private static final String[][] CONTACTS = {{"SX1=Intersect(e_1,n,K)", "e_1", "K", "n"},
			{"SX2=Intersect(e_1,c_1,K)", "e_1", "K", "c_1"},
			{"SX3=Intersect(o_1,h_2,K)", "o_1", "K", "h_2"},
			{"SX4=Intersect(e_1,c_1,J)", "e_1", "J", "c_1"},
			{"SX5=Intersect(e_1,r,J)", "e_1", "J", "r"}};

	private final List<ConstructiveIdentityCounters> measured = new ArrayList<>();

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-e2-cost");
		root.put("status", "CU3 E2 GATE G-AUT-8 BENCH EVIDENCE - NO VERDICT OF ITS OWN");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner", "tools/benchmark/pre-g9b-cu/run-cu3-e2-cost.ps1");
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
		root.put("model", onEdt(() -> model(PreG9BCu0Corpus.open(
				PreG9BCu0Corpus.withoutE2(work)))));
		root.put("synthetic", onEdt(this::synthetic));
		root.put("largest_observed", largest());
		Files.writeString(output.resolve("cu3-e2-cost.json"), PreG9BCu0References.json(root),
				StandardCharsets.UTF_8);
	}

	// ---------------------------------------------------------------- author model

	private Map<String, Object> model(AppGeoCeDG app) {
		Construction construction = app.getKernel().getConstruction();
		AuthorityPolicyAdoption.adopt(construction);
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String[] contact : CONTACTS) {
			GeoElement point = PreG9BR6PlusF3Fixtures.must(app, contact[0]);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("product", measure(resolution(point).getCertificate(),
					() -> StructuralPointResolver.resolve(
							(GeoLocusV2) construction.lookupLabel(contact[1]),
							(GeoPoint) construction.lookupLabel(contact[2]),
							construction.lookupLabel(contact[3]), ExactEvaluationBudget.V1)
							.getCertificate()));
			row.put("integrity", resolution(point).getIntegrity().name());
			row.put("fold", String.valueOf(resolution(point).getFold()));
			row.put("boundary_scope", measure(attempt(construction, contact,
					ConstructiveIdentityCertificate.Scope.BOUNDARY,
					ConstructiveIdentityBudget.V1), () -> attempt(construction, contact,
							ConstructiveIdentityCertificate.Scope.BOUNDARY,
							ConstructiveIdentityBudget.V1)));
			rows.put(contact[0], row);
		}
		Map<String, Object> injections = new LinkedHashMap<>();
		injections.put("term products 1000", describe(attempt(construction, CONTACTS[0],
				ConstructiveIdentityCertificate.Scope.AUTOMATIC,
				ConstructiveIdentityBudget.restrictedTo(1000, 16, 200_000, 4096))));
		injections.put("indeterminates 4", describe(attempt(construction, CONTACTS[0],
				ConstructiveIdentityCertificate.Scope.AUTOMATIC,
				ConstructiveIdentityBudget.restrictedTo(16_000_000L, 4, 200_000, 4096))));
		injections.put("terms 50", describe(attempt(construction, CONTACTS[0],
				ConstructiveIdentityCertificate.Scope.AUTOMATIC,
				ConstructiveIdentityBudget.restrictedTo(16_000_000L, 16, 50, 4096))));
		injections.put("bits 60", describe(attempt(construction, CONTACTS[0],
				ConstructiveIdentityCertificate.Scope.AUTOMATIC,
				ConstructiveIdentityBudget.restrictedTo(16_000_000L, 16, 200_000, 60))));
		rows.put("budget injections on SX1", injections);
		GeoElement contact = construction.lookupLabel("SX1");
		StructuralPointResolution before = resolution(contact);
		double t = PreG9BCu0Corpus.pathParameter(app, "E_1");
		PreG9BCu0Corpus.movePathPoint(app, "E_1", t + 0.3);
		Map<String, Object> drag = new LinkedHashMap<>();
		drag.put("same resolution after moving E_1", before == resolution(contact));
		PreG9BCu0Corpus.movePathPoint(app, "E_1", t);
		double h = PreG9BCu0Corpus.pathParameter(app, "H");
		PreG9BCu0Corpus.movePathPoint(app, "H", h + 0.5);
		drag.put("same certificate after moving H, rechecked", before.getCertificate()
				== resolution(contact).getCertificate());
		drag.put("integrity after moving H", resolution(contact).getIntegrity().name());
		PreG9BCu0Corpus.movePathPoint(app, "H", h);
		rows.put("drags", drag);
		return rows;
	}

	private ConstructiveIdentityCertificate attempt(Construction construction,
			String[] contact, ConstructiveIdentityCertificate.Scope scope,
			ConstructiveIdentityBudget budget) {
		ConstructiveCone.Builder builder = ConstructiveCone.builder(construction);
		GeoLocusV2 locus = (GeoLocusV2) construction.lookupLabel(contact[1]);
		int target = builder.add(construction.lookupLabel(contact[3]));
		GeoPoint driver = (GeoPoint) locus.getParentAlgorithm().getInput(1);
		int support = builder.add(driver.getPath().toGeoElement());
		int point = builder.addWithDriverSubstitution(
				locus.getParentAlgorithm().getInput(0).toGeoElement(), driver,
				construction.lookupLabel(contact[2]));
		return ConstructiveIdentityCertifier.incidence(contact[0], builder.build(), point,
				target, support, scope, budget);
	}

	// ---------------------------------------------------------------- synthetic

	private Map<String, Object> synthetic() {
		Map<String, Object> rows = new LinkedHashMap<>();
		String[] triangle = {"SA=(0.37,-1.21)", "SB=(5.83,0.44)", "SP=(2.9,3.3)", "w=47°",
				"SC=Rotate(SP,w,SA)", "mAB=Midpoint(SA,SB)", "mBC=Midpoint(SB,SC)",
				"mCA=Midpoint(SC,SA)", "sAB=Line(SA,SB)", "sBC=Line(SB,SC)", "sCA=Line(SC,SA)",
				"pAB=OrthogonalLine(mAB,sAB)", "pBC=OrthogonalLine(mBC,sBC)",
				"pCA=OrthogonalLine(mCA,sCA)", "SO=Intersect(pAB,pBC)", "cc=Circle(SO,SA)",
				"gA=Line(SA,mBC)", "gB=Line(SB,mCA)", "gC=Line(SC,mAB)", "SG=Intersect(gA,gB)"};
		rows.put("S1 circumcentre on the third bisector", plain(triangle, "SO", "pCA"));
		rows.put("S1 rotated vertex on the circumcircle", plain(triangle, "SC", "cc"));
		rows.put("S1 centroid on the third median", plain(triangle, "SG", "gC"));
		rows.put("S1 false: centroid on the circumcircle", plain(triangle, "SG", "cc"));
		String[] thales = {"TA=(-2.3,0.7)", "TB=(3.1,1.9)", "TO=Midpoint(TA,TB)",
				"tc=Circle(TO,TA)", "TP=(0.4,4.6)", "tl=Line(TA,TP)", "TQ=Intersect(tc,tl,1)",
				"TR=Intersect(tc,tl,2)", "tp=OrthogonalLine(TB,tl)"};
		rows.put("S2 intersection 1 on the perpendicular", plain(thales, "TQ", "tp"));
		rows.put("S2 intersection 2 on the perpendicular", plain(thales, "TR", "tp"));
		String[] foot = {"UA=(1.1,0.9)", "UB=(4.2,2.3)", "v=29°", "UR=Rotate(UB,v,UA)",
				"ul=Line(UA,UR)", "uf=OrthogonalLine(UB,ul)", "UF=Intersect(uf,ul)",
				"UM=Midpoint(UA,UB)", "uw=Circle(UM,UA)"};
		rows.put("S3 foot on the Thales circle", plain(foot, "UF", "uw"));
		String[] roots = {"RA=(0,0)", "r1=Circle(RA,2)", "r2=Circle((5,1),3)",
				"l1=Line((-3,0.5),(3,0.7))", "l2=Line((2,-3),(7,4))", "X1=Intersect(r1,l1,1)",
				"X2=Intersect(r2,l2,1)", "lx=Line(X2,(9,9))"};
		rows.put("independent second root", plain(roots, "X1", "lx"));
		return rows;
	}

	private Map<String, Object> plain(String[] commands, String point, String target) {
		AppGeoCeDG app = PreG9BR6PlusF3Fixtures.app(PreG9BR6PlusF3Documents.M);
		for (String command : commands) {
			PreG9BR6PlusF3Fixtures.must(app, command);
		}
		app.getKernel().updateConstruction(false);
		Construction construction = app.getKernel().getConstruction();
		Callable<ConstructiveIdentityCertificate> attempt = () -> {
			ConstructiveCone.Builder builder = ConstructiveCone.builder(construction);
			int subject = builder.add(construction.lookupLabel(point));
			int object = builder.add(construction.lookupLabel(target));
			return ConstructiveIdentityCertifier.incidence(point + " on " + target,
					builder.build(), subject, object, -1, ConstructiveIdentityBudget.V1);
		};
		try {
			return measure(attempt.call(), attempt);
		} catch (Exception failure) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("failure", failure.toString());
			return row;
		}
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
			Collections.sort(times);
			row.put("deterministic_over_runs", deterministic);
			row.put("median_wall_time_ms", times.get(times.size() / 2) / 1E6);
		} catch (Exception failure) {
			row.put("repetition_failure", failure.toString());
		}
		return row;
	}

	private Map<String, Object> describe(ConstructiveIdentityCertificate certificate) {
		Map<String, Object> row = new LinkedHashMap<>();
		if (certificate == null) {
			row.put("certificate", null);
			return row;
		}
		row.put("status", certificate.getStatus().name());
		row.put("cause", certificate.getCause().name());
		row.put("scope", certificate.getScope().name());
		row.put("key", certificate.getKey());
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

	private static StructuralPointResolution resolution(GeoElement point) {
		return ((AlgoLocusDriverCorrespondencePoint2D) point.getParentAlgorithm())
				.getResolution();
	}

	private static <T> T onEdt(Callable<T> action) throws Exception {
		return PreG9BR6PlusF3Fixtures.onEdt(action);
	}
}
