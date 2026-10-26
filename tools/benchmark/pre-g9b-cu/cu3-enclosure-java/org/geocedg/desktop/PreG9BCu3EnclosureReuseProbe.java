/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoLocusIntersectionV2;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.DocumentDecisionConfiguration;
import org.geocedg.common.kernel.locus.constructive.PreG9BCu3EnclosureReuse;
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
 * PRE-G9B-CU3 focal optimization T6, run only through
 * {@code tools/benchmark/pre-g9b-cu/run-cu3-enclosure-reuse.ps1}: on a copy of the adopted
 * author model, the box enclosures of the four public intersections of the five contacts in one
 * drag of {@code H} and one of {@code B}, recorded with the source of each query, then analysed
 * in the same revision by {@code PreG9BCu3EnclosureReuse}: the parts of their time, the boxes the
 * queries of {@code e_1} share and the bitwise equality of their traced points, the cost of a
 * lookup and, on a product with T6, the bitwise equality of the accelerated enclosures with a
 * full evaluation and the size of the slice memories. Counts are deterministic; timings are
 * informative. It changes nothing in the product, assigns no verdict and is not part of any
 * source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-enclosure-reuse")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3EnclosureReuseProbe {

	private static final String[][] PAIRS = {{"o_1", "h_2"}, {"e_1", "n"}, {"e_1", "c_1"},
			{"e_1", "r"}};

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-enclosure-reuse");
		root.put("status", "CU3 ENCLOSURE REUSE (T6) - NO VERDICT OF ITS OWN;"
				+ " TIMINGS ARE INFORMATIVE");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner", "tools/benchmark/pre-g9b-cu/run-cu3-enclosure-reuse.ps1");
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.parseBoolean(System.getProperty("geocedg.cu0.productDirty")));
		root.put("generator", generator);
		root.put("drags", PreG9BR6PlusF3Fixtures.onEdt(this::anatomy));
		Files.writeString(output.resolve("cu3-enclosure-reuse.json"),
				PreG9BCu0References.json(root), StandardCharsets.UTF_8);
	}

	private Map<String, Object> anatomy() throws Exception {
		AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
		Construction construction = app.getKernel().getConstruction();
		AuthorityPolicyAdoption.adopt(construction);
		Map<String, Recorder> recorders = new LinkedHashMap<>();
		Map<String, GeoElement[]> loci = new LinkedHashMap<>();
		for (String[] pair : PAIRS) {
			GeoLocusV2 locus = (GeoLocusV2) construction.lookupLabel(pair[0]);
			GeoElement target = construction.lookupLabel(pair[1]);
			Recorder recorder = new Recorder(new DocumentDecisionConfiguration(construction,
					locus, target));
			String key = pair[0] + " " + pair[1];
			recorders.put(key, recorder);
			loci.put(key, new GeoElement[] {locus, target});
			new AlgoLocusIntersectionV2(construction, locus, target, "t6-pair", "t6-lineage",
					"t6-target", "t6-topology", new PublicTargetIntersectionCapability2D(recorder),
					new GeoElement[0]);
		}
		Map<String, Object> rows = new LinkedHashMap<>();
		for (String label : new String[] {"H", "B"}) {
			double t = PreG9BCu0Corpus.pathParameter(app, label);
			for (Recorder recorder : recorders.values()) {
				recorder.reset();
			}
			PreG9BCu0Corpus.movePathPoint(app, label, t + 0.5);
			Map<String, ResidualJetSource2D> sources = new LinkedHashMap<>();
			Map<String, List<double[]>> boxes = new LinkedHashMap<>();
			for (Map.Entry<String, Recorder> entry : recorders.entrySet()) {
				sources.put(entry.getKey(), entry.getValue().source);
				boxes.put(entry.getKey(), new ArrayList<>(entry.getValue().boxes));
			}
			rows.put("after moving " + label, PreG9BCu3EnclosureReuse.analyze(sources, boxes,
					loci, 5));
			PreG9BCu0Corpus.movePathPoint(app, label, t);
		}
		return rows;
	}

	/** Records the source of the last computation of one query and the boxes asked of it. */
	private static final class Recorder implements IntersectionDecisionConfiguration2D {
		private final IntersectionDecisionConfiguration2D delegate;
		private ResidualJetSource2D source;
		private final List<double[]> boxes = new ArrayList<>();

		Recorder(IntersectionDecisionConfiguration2D delegate) {
			this.delegate = delegate;
		}

		void reset() {
			source = null;
			boxes.clear();
		}

		@Override
		public ResidualJetSource2D residualJets(LocusDefinition2D definition,
				LocusIntersectionTarget2D target) {
			ResidualJetSource2D captured = delegate.residualJets(definition, target);
			if (captured == null) {
				return null;
			}
			source = captured;
			boxes.clear();
			return new ResidualJetSource2D() {
				@Override
				public String getConfiguration() {
					return captured.getConfiguration();
				}

				@Override
				public boolean isConstructive() {
					return captured.isConstructive();
				}

				@Override
				public ResidualJetEnclosure2D enclose(double lower, double upper) {
					boxes.add(new double[] {lower, upper});
					return captured.enclose(lower, upper);
				}

				@Override
				public double[] normalizedResidual(double parameter) {
					return captured.normalizedResidual(parameter);
				}

				@Override
				public List<ExactContactEvidence2D> exactContacts(double lower, double upper) {
					return captured.exactContacts(lower, upper);
				}
			};
		}

		@Override
		public boolean isConstructive() {
			return delegate.isConstructive();
		}
	}
}
