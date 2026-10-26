/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.desktop;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.constructive.AuthorityPolicyAdoption;
import org.geocedg.common.kernel.locus.constructive.ConstructiveCone;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityBudget;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertificate;
import org.geocedg.common.kernel.locus.constructive.ConstructiveIdentityCertifier;
import org.geocedg.common.kernel.locus.constructive.PreG9BCu3RecheckAcceleration;
import org.geocedg.common.kernel.locus.intersection.ConstructionBoxModel2D;
import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.algos.AlgoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * PRE-G9B-CU3 focal optimization of the recheck (author authorization of 2026-10-26), run only
 * through {@code tools/benchmark/pre-g9b-cu/run-cu3-recheck-acceleration.ps1}: on a copy of the
 * adopted author model, for the certificate of each of the five mandatory contacts at its exact
 * parameter, the recheck with and without the recheck memory of the certificate after two moves
 * of {@code H}, two moves of {@code B}, a change of {@code rbCone} and a move of {@code D_1}:
 * outcomes, times, the signs the memory certifies and those left to the procedure of the
 * candidate, and the size of the memory. Counts are deterministic; timings are informative. It
 * changes nothing in the product, assigns no verdict and is not part of any source set.
 */
@EnabledIfSystemProperty(named = "geocedg.cu0.harness", matches = "cu3-recheck-acceleration")
@ExtendWith({G9U1TestApp.Lifecycle.class,
		PreG9BR6PlusD1DocumentUnitsTest.EmptyUnitPreferences.class,
		PreG9BR6PlusD1DocumentUnitsTest.DesktopLogger.class})
class PreG9BCu3RecheckAccelerationProbe {

	private static final String[] QUARTERS = {"0", "pi/2", "pi", "-pi/2"};
	private static final Object[][] CONTACTS = {{"e_1", "n", 0}, {"e_1", "c_1", 0},
			{"o_1", "h_2", 0}, {"e_1", "c_1", 2}, {"e_1", "r", 2}};

	@TempDir
	Path work;

	@Test
	void run() throws Exception {
		Path output = Path.of(System.getProperty("geocedg.cu0.output"));
		Files.createDirectories(output);
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("schema_version", 1);
		root.put("id", "pre-g9b-cu3-recheck-acceleration");
		root.put("status", "CU3 RECHECK ACCELERATION - NO VERDICT OF ITS OWN;"
				+ " TIMINGS ARE INFORMATIVE");
		Map<String, Object> generator = new LinkedHashMap<>();
		generator.put("runner", "tools/benchmark/pre-g9b-cu/run-cu3-recheck-acceleration.ps1");
		generator.put("source_commit", System.getProperty("geocedg.cu0.commit"));
		generator.put("source_tree", System.getProperty("geocedg.cu0.tree"));
		generator.put("product_sources_modified_relative_to_commit",
				Boolean.parseBoolean(System.getProperty("geocedg.cu0.productDirty")));
		root.put("generator", generator);
		root.put("contacts", PreG9BR6PlusF3Fixtures.onEdt(this::acceleration));
		Files.writeString(output.resolve("cu3-recheck-acceleration.json"),
				PreG9BCu0References.json(root), StandardCharsets.UTF_8);
	}

	private Map<String, Object> acceleration() throws Exception {
		AppGeoCeDG app = PreG9BCu0Corpus.open(PreG9BCu0Corpus.withoutE2(work));
		Construction construction = app.getKernel().getConstruction();
		AuthorityPolicyAdoption.adopt(construction);
		Map<String, Object> rows = new LinkedHashMap<>();
		for (Object[] contact : CONTACTS) {
			String locus = (String) contact[0];
			String target = (String) contact[1];
			int quarter = (Integer) contact[2];
			Slice slice = slice(construction, locus, target);
			ConstructiveIdentityCertificate certificate = ConstructiveIdentityCertifier
					.incidenceAtParameter(locus + " on " + target, slice.cone, slice.traced,
							slice.target, slice.support, quarter, slice.frame,
							ConstructiveIdentityBudget.V1);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("status", certificate.getStatus().name());
			for (String driven : new String[] {"H", "B"}) {
				double t = PreG9BCu0Corpus.pathParameter(app, driven);
				for (double delta : new double[] {0.5, 1.0}) {
					PreG9BCu0Corpus.movePathPoint(app, driven, t + delta);
					row.put("after moving " + driven + " by " + delta, analyze(certificate,
							construction, locus, target));
				}
				PreG9BCu0Corpus.movePathPoint(app, driven, t);
			}
			GeoNumeric radius = (GeoNumeric) construction.lookupLabel("rbCone");
			double value = radius.getValue();
			setNumber(radius, value + 0.25);
			row.put("after rbCone + 0.25", analyze(certificate, construction, locus, target));
			setNumber(radius, value);
			GeoPoint d1 = (GeoPoint) construction.lookupLabel("D_1");
			double x = d1.getInhomX();
			double y = d1.getInhomY();
			place(d1, x + 0.01, y);
			row.put("after moving D_1 by 0.01", analyze(certificate, construction, locus, target));
			place(d1, x, y);
			rows.put(locus + " " + target + " at " + QUARTERS[quarter], row);
		}
		return rows;
	}

	private static Map<String, Object> analyze(ConstructiveIdentityCertificate certificate,
			Construction construction, String locus, String target) throws Exception {
		return PreG9BCu3RecheckAcceleration.analyze(certificate,
				slice(construction, locus, target).cone);
	}

	private static void setNumber(GeoNumeric number, double value) {
		number.setValue(value);
		number.updateCascade();
	}

	private static void place(GeoPoint point, double x, double y) {
		point.setCoords(x, y, 1.0);
		point.updateCascade();
	}

	private static Slice slice(Construction construction, String locusLabel, String targetLabel) {
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

	private static final class Slice {
		private ConstructiveCone cone;
		private int target;
		private int support;
		private int traced;
		private double[] frame;
	}
}
