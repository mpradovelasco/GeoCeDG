/* GeoCeDG
 * Copyright (c) 2026 GeoCeDG contributors
 * SPDX-License-Identifier: EUPL-1.2
 */

package org.geocedg.common.kernel.bspline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geocedg.common.kernel.algos.AlgoLocusMetricScalarAdapter;
import org.geocedg.common.kernel.geos.GeoLocusIntersectionResult;
import org.geocedg.common.kernel.geos.GeoLocusV2;
import org.geocedg.common.kernel.locus.LocusBranch2D;
import org.geocedg.common.kernel.locus.LocusDefinition2D;
import org.geocedg.common.kernel.locus.PiecewisePolynomialLocus2D;
import org.geocedg.common.kernel.locus.intersection.IntersectionSourceBinding2D;
import org.geocedg.common.kernel.locus.intersection.LocusIntersectionResult2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIdentity2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionPolicy2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionQuery2D;
import org.geocedg.common.kernel.locus.intersection.LocusPairIntersectionSolver2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionPolicy2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionQuery2D;
import org.geocedg.common.kernel.locus.interaction.LocusPointInteractionResolver2D;
import org.geocedg.common.main.settings.config.AppConfigGeoCeDG;
import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.BaseUnitTest;
import org.geogebra.common.jre.headless.AppCommon;
import org.junit.jupiter.api.Test;

/**
 * PRE-G9B-MAINT-A2, BS-13 (bspline-2d.md section 9.3): SplineV2 stays bit-identical through
 * the additive local-coefficient extension. The golden text was captured on the A2 base
 * before any product edit; this test recomputes the same scenarios and compares every
 * line. Coefficients, intersection, pair, point-interaction and metric results are dumped
 * with exact hexadecimal doubles; the construction XML is compared too.
 */
public class MaintA2SplineV2BitIdentityTest extends BaseUnitTest {
	/** Classpath resource holding the golden text captured on the base. */
	static final String GOLDEN = "maint-a2-splinev2-golden.txt";
	/** System property naming a file to write instead of comparing (capture on the base). */
	static final String CAPTURE_PROPERTY = "maint.a2.goldenCapture";
	private static final String[] CURVES = {"S1", "S2", "S3", "S4", "S5", "T1", "T2", "T3",
			"T4", "T5"};
	private static final String[] TARGETS = {"line", "circle", "ellipse", "parabola"};
	private static final double[][] QUERIES = {{2, 2}, {0.5, 1}, {5, 1.5}, {3.2, 2.4}};

	@Override
	public AppCommon createAppCommon() {
		return AppCommonFactory.create(new AppConfigGeoCeDG(true));
	}

	@Test
	void splineV2ConsumersAreBitIdenticalToTheBaseCapture() throws IOException {
		Map<String, String> current = capture();
		String capturePath = System.getProperty(CAPTURE_PROPERTY);
		if (capturePath != null && !capturePath.isEmpty()) {
			StringBuilder text = new StringBuilder();
			text.append("# PRE-G9B-MAINT-A2 BS-13 golden: SplineV2 consumer results captured on")
					.append(" the A2 base before any product edit\n");
			for (Map.Entry<String, String> entry : current.entrySet()) {
				text.append(entry.getKey()).append('\t').append(entry.getValue()).append('\n');
			}
			Files.write(Paths.get(capturePath), text.toString().getBytes(StandardCharsets.UTF_8));
			return;
		}
		Map<String, String> golden = readGolden();
		assertEquals(golden.keySet(), current.keySet(), "scenario inventory");
		for (Map.Entry<String, String> entry : golden.entrySet()) {
			assertEquals(entry.getValue(), current.get(entry.getKey()), entry.getKey());
		}
	}

	private Map<String, String> readGolden() throws IOException {
		Map<String, String> golden = new LinkedHashMap<>();
		try (InputStream input = MaintA2SplineV2BitIdentityTest.class
				.getResourceAsStream(GOLDEN)) {
			assertNotNull(input, GOLDEN);
			String text = new String(input.readAllBytes(), StandardCharsets.UTF_8);
			for (String line : text.split("\\R")) {
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				int tab = line.indexOf('\t');
				assertTrue(tab > 0, line);
				golden.put(line.substring(0, tab), line.substring(tab + 1));
			}
		}
		return golden;
	}

	private Map<String, String> capture() {
		build();
		Map<String, String> result = new LinkedHashMap<>();
		for (String curve : CURVES) {
			GeoLocusV2 locus = (GeoLocusV2) lookup(curve);
			result.put(curve + ".coefficients", coefficients(locus));
		}
		for (String curve : CURVES) {
			for (String target : TARGETS) {
				String label = "I" + curve + target;
				GeoLocusIntersectionResult intersection = add(label + "=Intersect(" + curve
						+ "," + target + ")");
				result.put(curve + ".intersect." + target,
						MaintA2DeterministicDump.of(intersection.getIntersectionResult()));
			}
		}
		String[][] pairs = {{"S2", "S3"}, {"S1", "T2"}, {"S4", "T3"}, {"S2", "T1"},
				{"S5", "S1"}};
		for (String[] pair : pairs) {
			// The public command orders a pair by its random durable identities (observed on
			// the base: solution order and evaluation side change between runs), so the pair
			// solver is driven directly with fixed identities in a fixed order.
			result.put(pair[0] + "x" + pair[1] + ".pair", MaintA2DeterministicDump.of(
					pair(((GeoLocusV2) lookup(pair[0])).getSemanticDefinition()
									.withLocusIdentity("maint-a2/pair/first"),
							((GeoLocusV2) lookup(pair[1])).getSemanticDefinition()
									.withLocusIdentity("maint-a2/pair/second"))));
		}
		for (String curve : CURVES) {
			GeoLocusV2 locus = (GeoLocusV2) lookup(curve);
			for (int index = 0; index < QUERIES.length; index++) {
				result.put(curve + ".interaction." + index, MaintA2DeterministicDump.of(
						new LocusPointInteractionResolver2D().resolve(
								new LocusPointInteractionQuery2D(locus, QUERIES[index][0],
										QUERIES[index][1],
										LocusPointInteractionPolicy2D.initial(0.25)))));
			}
		}
		for (String curve : CURVES) {
			result.put(curve + ".length", MaintA2DeterministicDump.of(
					AlgoLocusMetricScalarAdapter.richResultOf(add("M" + curve
							+ "=LocusLength(" + curve + ")")).getMetricResult()));
		}
		add("PP=Point(S2,\"spline-v2/main\",0.2)");
		add("PQ=Point(S2,\"spline-v2/main\",0.85)");
		result.put("S2.partialLength", MaintA2DeterministicDump.of(
				AlgoLocusMetricScalarAdapter.richResultOf(add("MP=LocusLength(S2,PP,PQ)"))
						.getMetricResult()));
		// The identity section lists records in the order of their random identifiers.
		result.put("construction.xml", MaintA2DeterministicDump.normalize(
				getApp().getXML().replaceAll("(?s)<geocedgSpatial.*?</geocedgSpatial>", "")));
		return result;
	}

	private void build() {
		for (String definition : new String[] {"A=(0,0)", "B=(1,2)", "C=(3,2.5)",
				"D=(4,0.5)", "E=(6,1)", "F=(7,3)", "G=(5,4)", "w(x,y)=sqrt(x^2+4*y^2)+0.5",
				"S1=SplineV2({A,B,C,D},3)", "S2=SplineV2({A,B,C,D,E,F,G},3)",
				"S3=SplineV2({A,B,C,D,E,F},5)", "S4=SplineV2({A,B,C,D,E,A},3)",
				"S5=SplineV2({A,B,C,D},3,w)", "line:y=1.5", "circle=Circle((3,2),2)",
				"ellipse=Ellipse((1,1),(5,1),3)", "parabola:y=0.25x^2", "mirror:x+y=1",
				"T1=Translate(S2,Vector((1,-2)))", "T2=Rotate(S1,pi/6,A)",
				"T3=Reflect(S4,mirror)", "T4=Dilate(S3,2,B)",
				"T5=Translate(T2,Vector((-0.5,0.25)))"}) {
			assertNotNull(add(definition), definition);
		}
	}

	private static LocusIntersectionResult2D pair(LocusDefinition2D callerFirst,
			LocusDefinition2D callerSecond) {
		LocusPairIntersectionQuery2D query = new LocusPairIntersectionQuery2D(callerFirst,
				callerSecond, "maint-a2/polynomial-pair", "maint-a2/regular-topology",
				LocusPairIntersectionPolicy2D.initial(callerFirst, callerSecond));
		LocusDefinition2D first = query.isCallerOrderCanonical() ? callerFirst : callerSecond;
		LocusDefinition2D second = query.isCallerOrderCanonical() ? callerSecond : callerFirst;
		return new LocusPairIntersectionSolver2D().intersect(query, first, second,
				new IntersectionSourceBinding2D(query), null,
				lineage -> LocusPairIdentity2D.solutionToken(query.getSourcePairIdentity(),
						query.getConstructiveIntersectionLineage(),
						query.getTopologyContext(), lineage));
	}

	private static String coefficients(GeoLocusV2 locus) {
		LocusDefinition2D definition = locus.getSemanticDefinition();
		assertNotNull(definition, locus.getLabelSimple());
		PiecewisePolynomialLocus2D source =
				(PiecewisePolynomialLocus2D) definition.getEvaluatorCapability();
		List<Object> spans = new ArrayList<>();
		spans.add(source.supportsPiecewisePolynomial(definition));
		spans.add(source.getPolynomialCompositionDepth());
		spans.add(source.getPolynomialCapabilitySignature());
		for (LocusBranch2D branch : definition.getBranches()) {
			String key = branch.getBranchKey();
			int count = source.getPolynomialSpanCount(key);
			spans.add(key);
			for (int span = 0; span < count; span++) {
				spans.add(source.getPolynomialSpanLower(key, span));
				spans.add(source.getPolynomialSpanUpper(key, span));
				spans.add(source.getPolynomialCoordinateCoefficients(key, span));
				spans.add(source.getInteriorBreakpoints(key,
						source.getPolynomialSpanLower(key, 0),
						source.getPolynomialSpanUpper(key, count - 1)));
			}
		}
		return MaintA2DeterministicDump.of(spans);
	}
}
